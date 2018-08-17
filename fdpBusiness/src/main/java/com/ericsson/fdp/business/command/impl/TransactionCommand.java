package com.ericsson.fdp.business.command.impl;

import java.io.File;
import java.io.FileWriter;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import com.ericsson.fdp.business.FDPRollbackable;
import com.ericsson.fdp.business.command.AbstractCommand;
import com.ericsson.fdp.business.enums.AuxRequestParam;
import com.ericsson.fdp.business.enums.ConfigurationKey;
import com.ericsson.fdp.business.exception.RollbackException;
import com.ericsson.fdp.business.util.ApplicationBusinessConfigUtil;
import com.ericsson.fdp.business.util.LoggerUtil;
import com.ericsson.fdp.business.util.PostLogsUtil;
import com.ericsson.fdp.common.constants.FDPConstant;
import com.ericsson.fdp.common.constants.FDPLoggerConstants;
import com.ericsson.fdp.common.enums.Status;
import com.ericsson.fdp.core.command.FDPCommand;
import com.ericsson.fdp.core.config.utils.PropertyUtils;
import com.ericsson.fdp.core.exception.ExecutionFailedException;
import com.ericsson.fdp.core.logging.FDPLogger;
import com.ericsson.fdp.core.request.FDPRequest;
import com.ericsson.fdp.dao.enums.CommandExecutionType;

import ch.qos.logback.classic.Logger;

//these are the command where CommandType = ADD, UPDATE,DELETE
/**
 * This class is used to implement the transactional commands such as ADD,
 * UPDATE and DELETE.
 * 
 * @author Ericsson.
 * 
 */
public class TransactionCommand extends AbstractCommand implements FDPRollbackable {

	/**
	 * 
	 */
	private static final long serialVersionUID = -5125686304197093669L;
	private final String ROLLBACK_COMMAND_FILE_NAME = "ROLLBACK_COMMAND_FILE_NAME";
	private final String ROLLBACK_COMMAND_FILE_FORMAT = "ROLLBACK_COMMAND_FILE_FORMAT";
	private final String ROLLBACK_COMMAND_FILE_PATH = "ROLLBACK_COMMAND_FILE_PATH";
	private final String HOURLY = "HOURLY";
	private final String DEFAULT_FILE_PATH = "/tmp";
	private final String DEFAULR_ROLLBACK_COMMAND_FILE_NAME = "rollback_retry_failure_command";

	/**
	 * The constructor for creating transaction command.
	 * 
	 * @param commandDisplayName
	 *            The command display name to set.
	 */
	public TransactionCommand(final String commandDisplayName) {
		super.setCommandDisplayName(commandDisplayName);
	}

	@Override
	public boolean performRollback(final FDPRequest fdpRequest) throws RollbackException {
		final Logger circleLogger = LoggerUtil.getSummaryLoggerFromRequest(fdpRequest);
		FDPLogger.debug(circleLogger, getClass(), "performRollback()", LoggerUtil.getRequestAppender(fdpRequest)
				+ "Performing rollback for command " + getCommandDisplayName());
		boolean rollbackPerformed = true;
		List<FDPCommand> rollbackCommands = null;
		try {
			final Map<String, String> configurationMap = fdpRequest.getCircle().getConfigurationKeyValueMap();
			final String paramDisplay = configurationMap.get(ConfigurationKey.PP_REPORT_PARAM_DISPLAY.getAttributeName());
			rollbackCommands = ApplicationBusinessConfigUtil
					.getRollbackCommandsForCommand(getCommandDisplayName());			
			for (final FDPCommand rollbackCommand : rollbackCommands) {
				AbstractCommand abstractCommand = (AbstractCommand)rollbackCommand;
				AbstractCommand transactionCommand = (AbstractCommand)abstractCommand.cloneObject(this);
				FDPLogger.info(
						circleLogger,
						getClass(),
						"performRollback()",
						LoggerUtil.getRequestAppender(fdpRequest) + "Executing rollback command "
								+ rollbackCommand.getCommandDisplayName());
				abstractCommand.setRollbackCommand(true);
				
				rollbackPerformed = rollbackPerformed
						&& (Status.SUCCESS.equals(rollbackCommand.execute(fdpRequest, this)));
				
				if(null != paramDisplay && paramDisplay.equalsIgnoreCase(FDPLoggerConstants.TRUE)){
					LoggerUtil.generateLogsForRollbackCommand(fdpRequest, rollbackCommand);
				}
				
				//Rollback retry shall be performed for UCIP/ACIP commands 
				if (rollbackCommand.getCommandExecutionType() == CommandExecutionType.UCIP || rollbackCommand.getCommandExecutionType() == CommandExecutionType.ACIP) {
					if (fdpRequest.getAuxiliaryRequestParameter(AuxRequestParam.LAST_EXECUTED_ROLLBACK_COMMAND) != null) {
						FDPCommand fdpCommand = (AbstractCommand)fdpRequest.getAuxiliaryRequestParameter(AuxRequestParam.LAST_EXECUTED_ROLLBACK_COMMAND);
						String responseCode = getConfigurationMapValue(fdpRequest, ConfigurationKey.ROLLBACK_RETRY_RESPONSE_CODE);
						List<String> responseCodeList = null;
						if (responseCode != null) {
							responseCodeList = new ArrayList<>(Arrays.asList(responseCode.split(FDPConstant.COMMA)));
						}
						
						if (responseCodeList != null && responseCodeList.contains(fdpCommand.getResponseError().getResponseCode())) {
							Integer retryCount = getConfigurationMapValue(fdpRequest, ConfigurationKey.ROLLBACK_RETRY_COUNT) == null ?
								0 : Integer.valueOf(getConfigurationMapValue(fdpRequest, ConfigurationKey.ROLLBACK_RETRY_COUNT));
							
							Long retryInterval = getConfigurationMapValue(fdpRequest, ConfigurationKey.ROLLBACK_RETRY_INTERVAL) == null ?
									0 : Long.valueOf(getConfigurationMapValue(fdpRequest, ConfigurationKey.ROLLBACK_RETRY_INTERVAL));
							
							for (int count = 1; count <= retryCount; count++) {
								try {
									Thread.sleep(retryInterval);
								} catch (InterruptedException e) {
									e.printStackTrace();
								}
								FDPLogger.info(
										circleLogger,
										getClass(),
										"performRollback()",
										LoggerUtil.getRequestAppender(fdpRequest) + "Rollback Retry Number " + count  + " for command "
												+ rollbackCommand.getCommandDisplayName());
								AbstractCommand newTransactionCommand = (AbstractCommand)abstractCommand.cloneObject(transactionCommand);
								if (Status.SUCCESS.equals(rollbackCommand.execute(fdpRequest, newTransactionCommand))) {
									rollbackPerformed = true;
								}
								
								fdpCommand = (AbstractCommand)fdpRequest.getAuxiliaryRequestParameter(AuxRequestParam.LAST_EXECUTED_ROLLBACK_COMMAND);
								
								if (responseCodeList.contains(fdpCommand.getResponseError().getResponseCode())) {
									continue;
								} else {
									break;
								}
							}
						}
					}	
				} 
				 
				/*if (!rollbackPerformed) {
					writeRollbackCommand(fdpRequest, circleLogger);
				}*/
				transactionCommand = null;
			}
			
		} catch (final ExecutionFailedException e) {
			FDPLogger.error(circleLogger, getClass(), "performRollback()", LoggerUtil.getRequestAppender(fdpRequest)
					+ "Execution for the command failed.", e);
			//writeRollbackCommand(fdpRequest, circleLogger);
			throw new RollbackException("Execution for the command failed.", e);
		}
		FDPLogger.debug(circleLogger, getClass(), "performRollback()", LoggerUtil.getRequestAppender(fdpRequest)
				+ "Rollback performed " + rollbackPerformed);
		
		return rollbackPerformed;

	}
	
	/**
	 * This method will return the value of input configuration key as defined in fdpCircle
	 * @return
	 */
	private String getConfigurationMapValue(FDPRequest fdpRequest, ConfigurationKey key){
		final Map<String, String> configurationMap = fdpRequest.getCircle().getConfigurationKeyValueMap();
		return configurationMap.get(key.getAttributeName());		
	}
	
	/**
	 * This method will process Rollback command which gets failed during execution 
	 * @param fdpRequest
	 * @param circleLogger
	 * @param fdpCommand
	 * @param rollbackCommand
	 */
	private void writeRollbackCommand(final FDPRequest fdpRequest, final Logger circleLogger) {
		
		ExecutorService executor = Executors.newSingleThreadExecutor();
		executor.execute(new Runnable() {
			@Override
			public void run() {
				writeCommand(fdpRequest, circleLogger);

			}
		});
	}
	
	/**
	 * This method will write Rollback command to file
	 * @param fdpRequest
	 * @param circleLogger
	 * @param fdpCommand
	 * @param rollbackCommand
	 */
	private void writeCommand(final FDPRequest fdpRequest, final Logger circleLogger) {
		AbstractCommand fdpCommand = (AbstractCommand)fdpRequest.getAuxiliaryRequestParameter(AuxRequestParam.LAST_EXECUTED_ROLLBACK_COMMAND);
		if (fdpCommand != null) {
			String commandRequest = PostLogsUtil.getCommandString(fdpCommand.getCommandExecutionType(), fdpCommand, fdpRequest);
			String stringTowrite = LoggerUtil.getRequestAppender(fdpRequest) + FDPLoggerConstants.CORRELATION_ID + FDPConstant.LOGGER_KEY_VALUE_DELIMITER + 
					fdpRequest.getOriginTransactionID().toString() + FDPConstant.LOGGER_DELIMITER + "RBCMDREQ = " + "{" + commandRequest + "}" +
					System.lineSeparator();
			
			try (FileWriter fileWriter = new FileWriter(getFilePathWithName(), true)) {

				fileWriter.append(stringTowrite);
				
				FDPLogger.info(
						circleLogger,
						getClass(),
						"writeCommand()",
						LoggerUtil.getRequestAppender(fdpRequest) + "Rollback command " + fdpCommand.getCommandDisplayName() +
						" Successfully writen into " + getFilePathWithName() + " for " + FDPLoggerConstants.CORRELATION_ID + FDPConstant.LOGGER_KEY_VALUE_DELIMITER + 
						fdpRequest.getOriginTransactionID().toString());
			} catch (Exception e) {
				FDPLogger.info(
						circleLogger,
						getClass(),
						"writeCommand()",
						LoggerUtil.getRequestAppender(fdpRequest) + "Exception occurs while wrting Rollback command " + fdpCommand.getCommandDisplayName() +
						" into " + getFilePathWithName() + " for " + FDPLoggerConstants.CORRELATION_ID + FDPConstant.LOGGER_KEY_VALUE_DELIMITER + 
						fdpRequest.getOriginTransactionID().toString());
			}
		}
	}
	
	/**
	 * Gets the Rollback command file path
	 * @return
	 */
	private String getFilePath() {
		final String filePath = PropertyUtils.getProperty(ROLLBACK_COMMAND_FILE_PATH).trim();

		if (null == filePath)
			return DEFAULT_FILE_PATH;

		return filePath;
	}

	/**
	 * Get the Rollback command file name
	 * @return
	 */
	private String getFileName() {
		final String filename = PropertyUtils.getProperty(ROLLBACK_COMMAND_FILE_NAME).trim();

		if (null == filename)
			return DEFAULR_ROLLBACK_COMMAND_FILE_NAME;

		return filename;
	}

	/**
	 * This method will get the Rollback command file format 
	 * @return
	 */
	private SimpleDateFormat getFileFormat() {
		final String fileFormat = PropertyUtils.getProperty(ROLLBACK_COMMAND_FILE_FORMAT).trim();

		if (HOURLY.equalsIgnoreCase(fileFormat))
			return new SimpleDateFormat("ddMMyyyy_HH");

		return new SimpleDateFormat("ddMMyyyy");
	}

	/**
	 * This method will get the Rollback command absolute file path with file name
	 * @return
	 */
	private String getFilePathWithName() {
		Date date = new Date();

		return getFilePath() + File.separator + getFileName() + FDPConstant.UNDERSCORE + getFileFormat().format(date)
				+FDPConstant.DOT + FDPConstant.LOG;
	}
}
