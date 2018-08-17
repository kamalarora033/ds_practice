/**
 * 
 */
package com.ericsson.fdp.business.policy.policyrule.impl;

import ch.qos.logback.classic.Logger;

import com.ericsson.fdp.FDPCacheable;
import com.ericsson.fdp.business.command.AbstractCommand;
import com.ericsson.fdp.business.constants.BusinessConstants;
import com.ericsson.fdp.business.enums.Command;
import com.ericsson.fdp.business.enums.TLVOptions;
import com.ericsson.fdp.business.exception.NotificationFailedException;
import com.ericsson.fdp.business.request.FDPPolicyResponse;
import com.ericsson.fdp.business.request.impl.FDPResponseImpl;
import com.ericsson.fdp.business.util.LoggerUtil;
import com.ericsson.fdp.business.util.NotificationUtil;
import com.ericsson.fdp.business.util.ResponseUtil;
import com.ericsson.fdp.common.enums.ModuleType;
import com.ericsson.fdp.common.enums.Status;
import com.ericsson.fdp.core.exception.ExecutionFailedException;
import com.ericsson.fdp.core.logging.FDPLogger;
import com.ericsson.fdp.core.request.FDPRequest;
import com.ericsson.fdp.core.request.FDPResponse;
import com.ericsson.fdp.core.serviceprovisioning.bean.FDPMetaBag;
import com.ericsson.fdp.core.utils.ApplicationConfigUtil;

/**
 * The Class MCarbonPolicyRuleImpl.
 * 
 * @author Ericsson
 */
public class MCarbonPolicyRuleImpl extends AbstractPolicyRule {

	/** The Constant serialVersionUID. */
	private static final long serialVersionUID = 6278941448972065289L;
	private static final String MCARBON = "MCARBON";
	@Override
	public FDPResponse displayRule(final FDPRequest fdpRequest) throws ExecutionFailedException {
		LoggerUtil.generatePolicyBehaviourLogsForUserBehaviour(fdpRequest, "MCARBON");
		FDPResponse response = null;
		final FDPCacheable fdpCommandCached = ApplicationConfigUtil.getMetaDataCache().getValue(
				new FDPMetaBag(fdpRequest.getCircle(), ModuleType.COMMAND, Command.M_CARBON_EXECUTE
						.getCommandDisplayName()));
		AbstractCommand fdpCommand = null;
		if (fdpCommandCached != null && fdpCommandCached instanceof AbstractCommand) {
			fdpCommand = (AbstractCommand) fdpCommandCached;
			if (fdpCommand.execute(fdpRequest).equals(Status.SUCCESS)) {
				final String commandResponse = fdpCommand.getCommandResponse();
				response = new FDPResponseImpl(Status.SUCCESS, false, MCARBON,ResponseUtil.createResponseMessageInList(
						fdpRequest.getChannel(), commandResponse, TLVOptions.FLASH));
			} else {
				Logger circleLogger = LoggerUtil.getSummaryLoggerFromRequest(fdpRequest);
				try {
					Long notificationId = Long.valueOf(BusinessConstants.HTTP_ADAPTER_ERROR_CODE)
							- fdpRequest.getCircle().getCircleId();
					response = new FDPResponseImpl(Status.SUCCESS, true, MCARBON,ResponseUtil.createResponseMessageInList(
							fdpRequest.getChannel(),
							NotificationUtil.createNotificationText(fdpRequest, notificationId, circleLogger),
							TLVOptions.SESSION_TERMINATE));
				} catch (NotificationFailedException ne) {
					FDPLogger.error(circleLogger, getClass(), "displayRule()",
							"Error while creating MCarbon Error Message with Actual Error:" + ne.getMessage(),ne);
					throw new ExecutionFailedException("Error while creating MCarbon Error Message with Actual Error:"
							+ ne.getMessage(), ne);
				}
			}
		} else {
			throw new ExecutionFailedException(Command.M_CARBON_EXECUTE.getCommandDisplayName() + " command not found.");
		}
		return response;
	}

	@Override
	public FDPPolicyResponse validatePolicyRule(final Object input, final FDPRequest fdpRequest,
			final Object... otherParams) throws ExecutionFailedException {
		return null;
	}

}
