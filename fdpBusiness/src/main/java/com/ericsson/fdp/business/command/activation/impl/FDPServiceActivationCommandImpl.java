package com.ericsson.fdp.business.command.activation.impl;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Map;

import javax.inject.Inject;

import ch.qos.logback.classic.Logger;

import com.ericsson.fdp.FDPCacheable;
import com.ericsson.fdp.business.command.AbstractCommand;
import com.ericsson.fdp.business.enums.AuxRequestParam;
import com.ericsson.fdp.business.enums.Command;
import com.ericsson.fdp.business.util.CAICommandUtil;
import com.ericsson.fdp.business.util.CommandUtil;
import com.ericsson.fdp.business.util.LoggerUtil;
import com.ericsson.fdp.common.enums.ModuleType;
import com.ericsson.fdp.common.enums.Status;
import com.ericsson.fdp.core.command.FDPCommand;
import com.ericsson.fdp.core.command.param.CommandParam;
import com.ericsson.fdp.core.config.utils.PropertyUtils;
import com.ericsson.fdp.core.exception.ExecutionFailedException;
import com.ericsson.fdp.core.logging.FDPLogger;
import com.ericsson.fdp.core.request.FDPRequest;
import com.ericsson.fdp.core.request.impl.FDPRequestImpl;
import com.ericsson.fdp.core.serviceprovisioning.bean.FDPMetaBag;
import com.ericsson.fdp.core.utils.ApplicationConfigUtil;

public class FDPServiceActivationCommandImpl extends
		FDPDefaultActivationCommandImpl {
	private Logger logger;

	private static File file;

	@Inject
	private PropertyUtils propertyUtils;

	private FileWriter filewriter;

	public FDPServiceActivationCommandImpl(FDPCommand fdpCommand) {
		super(fdpCommand);
		this.file = new File(
				propertyUtils.getProperty("EMA_ROLLBACK_LOGFILE_PATH"));

	}

	@Override
	protected Status process(FDPRequest input, Object... otherParams)
			throws ExecutionFailedException {
		FDPCommand checkActiveServiceCommand;
		FDPCommand checkIMSICommand;
		FDPCommand fdpActivationCommand = this.getFdpCommand();
		FDPCommand deprovCommand;
		FDPRequestImpl fdprequestimpl;
		String imsi;
		Status retunstatus;
		logger = LoggerUtil.getSummaryLoggerFromRequest(input);
		// get IMSI from HLR command
		checkIMSICommand = getCommand(input, Command.GET_HLR);
		logIFNull(checkIMSICommand);
		if (checkIMSICommand.execute(input, otherParams) == Status.SUCCESS) {
			imsi = (String) checkIMSICommand.getOutputParam("IMSI").getValue();
			fdprequestimpl = (FDPRequestImpl) input;
			fdprequestimpl.putAuxiliaryRequestParameter(AuxRequestParam.IMSI,
					imsi);
			checkActiveServiceCommand = getCommand(input, Command.CHECK_SERVICE);
			logIFNull(checkActiveServiceCommand);
			if (checkActiveServiceCommand.execute(input, otherParams) == Status.SUCCESS) {
				deprovCommand = getCommand(input,
						Command.SERVICE_DEACTIVATE_CAI);
				logIFNull(deprovCommand);
				if (deprovCommand.execute(input, otherParams) == Status.SUCCESS) {
					logIFNull(fdpActivationCommand);
					retunstatus = fdpActivationCommand.execute(input,
							otherParams);
					if (retunstatus == Status.FAILURE) {
						writetoRollback(fdpActivationCommand, input);
					}
				} else {
					retunstatus = deprovCommand.getExecutionStatus();
					writetoRollback(deprovCommand, input);
				}
			} else {
				retunstatus = fdpActivationCommand.execute(input, otherParams);
				writetoRollback(checkActiveServiceCommand, input);
			}
		}

		else {
			retunstatus = checkIMSICommand.getExecutionStatus();
			writetoRollback(checkIMSICommand, input);
		}
		
		return retunstatus;
	}

	private void writetoRollback(FDPCommand checkIMSICommand, FDPRequest input)
			throws ExecutionFailedException {
		try {
			filewriter = new FileWriter(file);
			filewriter.append("Activation");
			if (checkIMSICommand.getCommandDisplayName().equals(
					Command.GET_HLR.getCommandDisplayName())) {
				filewriter.append(System.getProperty("line.separator"));
				filewriter.append(CAICommandUtil.toCAIFormat(getCommand(input,
						Command.GET_HLR)));
				filewriter.append(System.getProperty("line.separator"));
				filewriter.append(CAICommandUtil.toCAIFormat(getCommand(input,
						Command.CHECK_SERVICE)));
				filewriter.append(System.getProperty("line.separator"));
				filewriter.append(CAICommandUtil.toCAIFormat(getCommand(input,
						Command.SERVICE_DEACTIVATE_CAI)));
				filewriter.append(System.getProperty("line.separator"));
				filewriter.append(CAICommandUtil.toCAIFormat(getCommand(input,
						Command.SERVICE_ACTIVATE_CAI)));
			} else if (checkIMSICommand.getCommandDisplayName().equals(
					Command.CHECK_SERVICE.getCommandDisplayName())) {
				filewriter.append(System.getProperty("line.separator"));
				filewriter.append(CAICommandUtil.toCAIFormat(getCommand(input,
						Command.CHECK_SERVICE)));
				filewriter.append(System.getProperty("line.separator"));
				filewriter.append(CAICommandUtil.toCAIFormat(getCommand(input,
						Command.SERVICE_DEACTIVATE_CAI)));
				filewriter.append(System.getProperty("line.separator"));
				filewriter.append(CAICommandUtil.toCAIFormat(getCommand(input,
						Command.SERVICE_ACTIVATE_CAI)));

			} else if (checkIMSICommand.getCommandDisplayName().equals(
					Command.SERVICE_DEACTIVATE_CAI.getCommandDisplayName())) {
				filewriter.append(System.getProperty("line.separator"));
				filewriter.append(CAICommandUtil.toCAIFormat(getCommand(input,
						Command.SERVICE_DEACTIVATE_CAI)));
				filewriter.append(System.getProperty("line.separator"));
				filewriter.append(CAICommandUtil.toCAIFormat(getCommand(input,
						Command.SERVICE_ACTIVATE_CAI)));

			} else if (checkIMSICommand.getCommandDisplayName().equals(
					Command.SERVICE_ACTIVATE_CAI.getCommandDisplayName())) {
				filewriter.append(System.getProperty("line.separator"));
				filewriter.append(CAICommandUtil.toCAIFormat(getCommand(input,
						Command.SERVICE_ACTIVATE_CAI)));

			}
			filewriter.append("END");
			filewriter.flush();
		} catch (IOException e) {
			throw new ExecutionFailedException(
					"Blackberry Rollback file not found");
		}
	}

	/**
	 * Description Command log if command not found in Cache
	 * 
	 * @param checkIMSICommand
	 */

	private void logIFNull(FDPCommand checkIMSICommand) {
		if (checkIMSICommand != null) {
			FDPLogger.error(logger, getClass(), "process()",
					"Command Not Found :" + checkIMSICommand.getCommandName());
		} else {
			FDPLogger.info(logger, getClass(), "process()", "Command  Found :"
					+ checkIMSICommand.getCommandName());
		}

	}

	private FDPCommand getCommand(FDPRequest fdpRequest, Command command)
			throws ExecutionFailedException {
		FDPCommand fdpCommand = null;
		final FDPCacheable fdpCommandCached = ApplicationConfigUtil
				.getMetaDataCache().getValue(
						new FDPMetaBag(fdpRequest.getCircle(),
								ModuleType.COMMAND, command
										.getCommandDisplayName()));
		if (null != fdpCommandCached
				&& fdpCommandCached instanceof AbstractCommand) {
			fdpCommand = (FDPCommand) fdpCommandCached;
		}
		return fdpCommand;
	}

}
