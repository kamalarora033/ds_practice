package com.ericsson.fdp.business.util;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;

import com.ericsson.fdp.business.batchjob.reportGeneration.service.CSVFileHandlerService;
import com.ericsson.fdp.business.command.AbstractCommand;
import com.ericsson.fdp.business.command.param.impl.CommandParamInput;
import com.ericsson.fdp.business.dto.reportGeneration.CSVFileDataDTO;
import com.ericsson.fdp.business.enums.Command;
import com.ericsson.fdp.business.enums.FailureCommandTypeEnum;
import com.ericsson.fdp.business.enums.RequestMetaValuesKey;
import com.ericsson.fdp.business.product.Product;
import com.ericsson.fdp.common.constants.FDPConstant;
import com.ericsson.fdp.common.constants.JNDILookupConstant;
import com.ericsson.fdp.core.command.param.CommandParam;
import com.ericsson.fdp.core.config.utils.PropertyUtils;
import com.ericsson.fdp.core.logging.FDPLogger;
import com.ericsson.fdp.core.request.FDPRequest;
import com.ericsson.fdp.dao.enums.CommandDefinition;
import com.ericsson.fdp.dao.enums.CommandParameterType;

import ch.qos.logback.classic.Logger;

/**
 * This is the utility class for failure command Report
 * 
 * @author GUR36857
 *
 */
public class FailureCommandReportGenerationUtil {

	private static CSVFileHandlerService csvFileHandlerService;

	/**
	 * 
	 */
	private FailureCommandReportGenerationUtil() {
	}

	/**
	 * This method would write the CSV file for the failure commands
	 * 
	 * @param fdpRequest
	 * @param command
	 * @param logger
	 */
	public static void writeFailureCommandCSV(final FDPRequest fdpRequest, final AbstractCommand command,
			final Logger logger) {

		FailureCommandTypeEnum commandTypeEnum;

		if (command.getCommandDisplayName().equalsIgnoreCase(Command.ROLLBACK.getCommandDisplayName()))
			return;

		if (command.getCommandType() != null && command.getCommandType() == CommandDefinition.GET)
			commandTypeEnum = FailureCommandTypeEnum
					.getFailureCommandTypeEnumForGETCommands(command.getCommandDisplayName());
		else
			commandTypeEnum = FailureCommandTypeEnum.getFailureCommandTypeEnum(command.getCommandDisplayName());

		if (commandTypeEnum != null && PropertyUtils.getProperty(commandTypeEnum.getPropertyFilePath()) != null) {
			if (null == csvFileHandlerService) {
				Context initialContext;
				try {
					initialContext = new InitialContext();
					csvFileHandlerService = (CSVFileHandlerService) initialContext
							.lookup(JNDILookupConstant.CSV_HANDLER_SERVICE_LOOK_UP);
				} catch (NamingException e) {
					FDPLogger.error(logger, FailureCommandReportGenerationUtil.class, "writeFailureCommandCSV()",
							LoggerUtil.getRequestAppender(fdpRequest) + "Could not write command", e);
				}
			}

			new GenerateReportThread(fdpRequest, command, commandTypeEnum, logger).start();

		}

	}

	/**
	 * This Thread would create the report in case of failure
	 * 
	 * @author GUR36857
	 *
	 */
	private static class GenerateReportThread extends Thread {

		final FDPRequest fdpRequest;
		final AbstractCommand command;
		final FailureCommandTypeEnum commandTypeEnum;
		final Logger logger;

		/**
		 * 
		 * @param fdpRequest
		 * @param command
		 * @param commandTypeEnum
		 * @param logger
		 */
		public GenerateReportThread(final FDPRequest fdpRequest, final AbstractCommand command,
				final FailureCommandTypeEnum commandTypeEnum, final Logger logger) {
			this.fdpRequest = fdpRequest;
			this.command = command;
			this.commandTypeEnum = commandTypeEnum;
			this.logger = logger;
		}

		@Override
		public void run() {

			FDPLogger.info(logger, getClass(), "GenerateReportThread(): Executing Thread --> ",
					fdpRequest.getRequestId());

			CSVFileDataDTO csvFileDataDTO = new CSVFileDataDTO();
			csvFileDataDTO.setCommaSepHeaders(commandTypeEnum.getReportHeaders());
			String fileName = PropertyUtils.getProperty(commandTypeEnum.getPropertyFilePath()).trim();
			if (command.isRollbackCommand())
				fileName = fileName.substring(0, fileName.indexOf(FDPConstant.DOT)) + "_ROLLBACK"
						+ fileName.substring(fileName.indexOf(FDPConstant.DOT), fileName.length());

			fileName = fileName.substring(0, fileName.indexOf(FDPConstant.DOT)) + FDPConstant.UNDERSCORE
					+ getFileFormat().format(new Date())
					+ fileName.substring(fileName.indexOf(FDPConstant.DOT), fileName.length());

			FDPLogger.info(logger, getClass(), "GenerateReportThread(): Executing Thread --> ",
					fdpRequest.getRequestId() + " Writing into file --> " + fileName);

			csvFileDataDTO.setFileName(fileName);

			addCommandCSVFields(fdpRequest, csvFileDataDTO, commandTypeEnum, command);
			csvFileHandlerService.exportCSVFile(csvFileDataDTO);
		}

		/**
		 * This method would add create the column data list
		 * 
		 * @param fdpRequest
		 * @param csvFileDataDTO
		 * @param commandTypeEnum
		 * @param command
		 */
		private static void addCommandCSVFields(final FDPRequest fdpRequest, CSVFileDataDTO csvFileDataDTO,
				FailureCommandTypeEnum commandTypeEnum, AbstractCommand command) {

			final List<List<String>> dataList = new ArrayList<>(1);
			List<String> data = new ArrayList<>(csvFileDataDTO.getCommaSepHeaders().split(FDPConstant.COMMA).length);
			Product product = (Product) fdpRequest.getValueFromRequest(RequestMetaValuesKey.PRODUCT);

			data.add(new SimpleDateFormat("yyyy-MM-dd HH:mm:ss:SSS").format(new Date()));
			data.add(fdpRequest.getRequestId());
			data.add(String.valueOf(fdpRequest.getSubscriberNumber()));
			data.add(product.getProductType().getName());
			data.add(String.valueOf(product.getProductId()));
			data.add(product.getProductName());
			data.add(command.getCommandExecutionType().getCommandToDisplay());
			data.add(command.getCommandDisplayName());
			data.add("\"" + PostLogsUtil.getCommandString(command.getCommandExecutionType(), command, fdpRequest)
					.replaceAll("\n", "") + "\"");
			data.add(command.getResponseError().getResponseCode());

			if (commandTypeEnum == FailureCommandTypeEnum.MONETARY)
				addMonetaryCommandCSVFields(data, command.getInputParam());

			dataList.add(data);
			csvFileDataDTO.setDataList(dataList);

		}

		/**
		 * This method will get the Rollback command file format
		 * 
		 * @return
		 */
		private SimpleDateFormat getFileFormat() {
			final String fileFormat = PropertyUtils.getProperty("FAILURE_COMMAND_FILE_FORMAT") == null ? "ddMMyyyy_HH"
					: PropertyUtils.getProperty("FAILURE_COMMAND_FILE_FORMAT").trim();
			return new SimpleDateFormat(fileFormat);
		}

		/**
		 * This method would add the monetary specific columns
		 * 
		 * @param data
		 * @param inputParam
		 */
		private static void addMonetaryCommandCSVFields(List<String> data, List<CommandParam> inputParam) {
			boolean isUbad = true;
			String amount = FDPConstant.NOT_APPLICABLE;
			List<String> daList = new ArrayList<>(3);
			for (CommandParam commandParam : inputParam)
				if (commandParam instanceof CommandParamInput && commandParam.getName() != null) {
					final CommandParamInput input = (CommandParamInput) commandParam;
					
					//for Mobile money and Loyalty
					if("amount".equalsIgnoreCase(input.getName()) || "Request".equalsIgnoreCase(input.getName())){
						getAmount(input, data);
						isUbad = false;
						break;
					}
					
					if (ChargingUtil.ADJUSTMENT_AMOUNT_VALUE.equalsIgnoreCase(input.getName()))
						amount = input.getValue().toString();

					if (ChargingUtil.DEDICATED_ACCOUNT_UPDATE_INFORMATION.equalsIgnoreCase(input.getName())) {
						for (int i = 0; i < 3; i++) {
							if (i < input.getChilderen().size())
								daList.add(addMonetaryCommandDAChilds(input.getChilderen().get(i)));
							else
								daList.add(addMonetaryCommandDAChilds(null));
						}

						break;
					}

				}
			if (daList.isEmpty()) {
				for (int i = 0; i < 3; i++)
					daList.add(addMonetaryCommandDAChilds(null));
			}
			if (isUbad)
				data.add(amount);
			data.addAll(daList);
		}

		/**
		 * This method would add the DA's for monetary command
		 * 
		 * @param childParam
		 * @return The DA
		 */
		private static String addMonetaryCommandDAChilds(CommandParam childParam) {

			String daID = FDPConstant.NOT_APPLICABLE;
			String daType = FDPConstant.NOT_APPLICABLE;
			String daValue = FDPConstant.NOT_APPLICABLE;
			if (childParam != null)
				for (CommandParam childParameters : childParam.getChilderen()) {
					if (ChargingUtil.DEDICATED_ACCOUNT_ID.equals(childParameters.getName()))
						daID = childParameters.getValue().toString();
					if (ChargingUtil.ADJUSTMENT_AMOUNT_VALUE.equals(childParameters.getName()))
						daValue = childParameters.getValue().toString();
					if (ChargingUtil.DEDICATE_ACCOUNT_TYPE.equals(childParameters.getName()))
						daType = childParameters.getValue().toString();
				}

			return daID + FDPConstant.HASH + daType + FDPConstant.HASH + daValue;
		}
	}
	
	/**
	 * This method will return the charging amount
	 * @param commandParam
	 */
	private static void getAmount(CommandParam commandParam, List<String> data) {
		List<CommandParam> childs = commandParam.getChilderen();
		 for (CommandParam childParam : childs) {
             if (childParam instanceof CommandParamInput && childParam.getName() != null) {
            	 if (childParam.getType().equals(CommandParameterType.STRUCT) || childParam.getType().equals(CommandParameterType.ARRAY))
            		 getAmount(childParam, data);
            	 else if ("amount".equalsIgnoreCase(childParam.getName()) || "NoOfPoints".equalsIgnoreCase(childParam.getName())){
            		data.add(childParam.getValue().toString());
            	 } 
             }
         }
	}

}
