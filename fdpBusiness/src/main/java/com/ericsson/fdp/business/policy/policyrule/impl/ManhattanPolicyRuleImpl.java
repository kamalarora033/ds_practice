/**
 * 
 */
package com.ericsson.fdp.business.policy.policyrule.impl;

import ch.qos.logback.classic.Logger;

import com.ericsson.fdp.FDPCacheable;
import com.ericsson.fdp.business.command.AbstractCommand;
import com.ericsson.fdp.business.enums.Command;
import com.ericsson.fdp.business.enums.ConfigurationKey;
import com.ericsson.fdp.business.enums.TLVOptions;
import com.ericsson.fdp.business.exception.NotificationFailedException;
import com.ericsson.fdp.business.request.FDPPolicyResponse;
import com.ericsson.fdp.business.request.impl.FDPResponseImpl;
import com.ericsson.fdp.business.util.LoggerUtil;
import com.ericsson.fdp.business.util.ManhattanCommandUtil;
import com.ericsson.fdp.business.util.NotificationUtil;
import com.ericsson.fdp.business.util.ResponseUtil;
import com.ericsson.fdp.business.util.TariffEnquiryNotificationUtil;
import com.ericsson.fdp.common.enums.ModuleType;
import com.ericsson.fdp.common.enums.Status;
import com.ericsson.fdp.core.command.FDPCommand;
import com.ericsson.fdp.core.exception.ExecutionFailedException;
import com.ericsson.fdp.core.logging.FDPLogger;
import com.ericsson.fdp.core.request.FDPRequest;
import com.ericsson.fdp.core.request.FDPResponse;
import com.ericsson.fdp.core.serviceprovisioning.bean.FDPMetaBag;
import com.ericsson.fdp.core.utils.ApplicationConfigUtil;

/**
 * The Class ManhattanPolicyRuleImpl.
 * 
 * @author Ericsson
 */
public class ManhattanPolicyRuleImpl extends AbstractPolicyRule {

	/**
	 *  The class serial version UID
	 */
	private static final long serialVersionUID = 4440595627359030720L;
	private static final String MANHATTAN = "MANHATTAN";
	
	@Override
	public FDPResponse displayRule(final FDPRequest fdpRequest) throws ExecutionFailedException {
		LoggerUtil.generatePolicyBehaviourLogsForUserBehaviour(fdpRequest, MANHATTAN);
		final Logger logger = LoggerUtil.getSummaryLoggerFromRequest(fdpRequest);
		FDPLogger.debug(logger, getClass(), "displayRule()", "Entered ManhattanPolicyRuleImpl");
		
		final FDPCacheable fdpCommandCached = ApplicationConfigUtil.getMetaDataCache().getValue(
				new FDPMetaBag(fdpRequest.getCircle(), ModuleType.COMMAND, Command.MANHATTAN_COMMAND
						.getCommandDisplayName()));
		FDPResponse response = null;
		AbstractCommand fdpCommand = null;
		if (null != fdpCommandCached && fdpCommandCached instanceof AbstractCommand) {
			fdpCommand = (AbstractCommand) fdpCommandCached;
			if (Status.SUCCESS.equals(fdpCommand.execute(fdpRequest))) {
				response = prepareSuccessResponse(fdpCommand, fdpRequest, logger);
			} else {
				response = failureResponse(fdpRequest, logger, response);
			}
		}
		FDPLogger.debug(logger, getClass(), "displayRule()", "Returning with response:"+response);
		return response;
	}

	/**
	 * This method is used to prepare failure response.
	 * 
	 * @param fdpRequest
	 * @param logger
	 * @param response
	 * @return
	 * @throws ExecutionFailedException
	 */
	private FDPResponse failureResponse(final FDPRequest fdpRequest,
			final Logger logger, FDPResponse response)
			throws ExecutionFailedException {
		FDPLogger.debug(logger, getClass(), "displayRule()", Command.MANHATTAN_COMMAND.getCommandName()+" command is Fail.");
		Logger circleLogger = LoggerUtil.getSummaryLoggerFromRequest(fdpRequest);
		try {
			String manhattanFailureNotificationtext = prepareFailureResponse(fdpRequest);
			if (null == manhattanFailureNotificationtext) {
				FDPLogger
						.debug(logger,
								getClass(),
								"failureResponse()",
								"Notification not configured for command MANHATTAN going with default circle configuration.");
				manhattanFailureNotificationtext = prepareDefaultFailureResponse(fdpRequest);
			}
			response = new FDPResponseImpl(Status.SUCCESS, true, MANHATTAN,ResponseUtil.createResponseMessageInList(
					fdpRequest.getChannel(),manhattanFailureNotificationtext,
					TLVOptions.SESSION_TERMINATE));
		} catch (NotificationFailedException ne) {
			FDPLogger.error(circleLogger, getClass(), "failureResponse()",
					"Error while creating MANHATTAN Error Message with Actual Error:" + ne.getMessage(),ne);
			throw new ExecutionFailedException("Error while creating MANHATTAN Error Message with Actual Error:"
					+ ne.getMessage(), ne);
		}
		return response;
	}

	@Override
	public FDPPolicyResponse validatePolicyRule(final Object input, final FDPRequest fdpRequest,
			final Object... otherParams) throws ExecutionFailedException {
		return null;
	}
	
	/**
	 * This method success response.
	 * 
	 * @param fdpCommand
	 * @param fdpRequest
	 * @param logger
	 * @return
	 */
	private FDPResponse prepareSuccessResponse(
			final AbstractCommand fdpCommand, final FDPRequest fdpRequest,
			final Logger logger) {
		final String commandResponse = fdpCommand
				.getOutputParam(ManhattanCommandUtil.RESPONSE_MSG).getValue()
				.toString();
		FDPLogger.debug(logger, getClass(), "prepareSuccessResponse()",
				Command.MANHATTAN_COMMAND.getCommandName()
						+ " command is success with commandResponse:"
						+ commandResponse);
		return new FDPResponseImpl(Status.SUCCESS, false,MANHATTAN,
				ResponseUtil.createResponseMessageInList(
						fdpRequest.getChannel(), commandResponse,
						TLVOptions.FLASH));
	}

	/**
	 * This method prepares the failure notification configured with command.
	 * 
	 * @param fdpRequest
	 * @return
	 * @throws ExecutionFailedException
	 * @throws NotificationFailedException 
	 */
	private String prepareFailureResponse(final FDPRequest fdpRequest) throws ExecutionFailedException, NotificationFailedException {
		final Logger circleLogger = LoggerUtil.getSummaryLoggerFromRequest(fdpRequest);
		FDPCommand lastExecutedCommand = fdpRequest.getLastExecutedCommand();
		Long notificationIdUpdated = lastExecutedCommand == null ? null : NotificationUtil.getNotificationIdForCommand(fdpRequest.getCircle(), lastExecutedCommand);
		return TariffEnquiryNotificationUtil.createNotificationText(fdpRequest, notificationIdUpdated, circleLogger);
	}
	
	/**
	 * This method prepares the failure notification configured with circle configuration.
	 * @param fdpRequest
	 * @return
	 */
	private String prepareDefaultFailureResponse(final FDPRequest fdpRequest) {
		String manhattanDefaultFailureResponse = null;
		manhattanDefaultFailureResponse = fdpRequest
				.getCircle()
				.getConfigurationKeyValueMap()
				.get(ConfigurationKey.MANHATTAN_FAIL_DEFAULT_NOTIFICATION_TEXT
						.getAttributeName());
		return manhattanDefaultFailureResponse;
	}
}
