package com.ericsson.fdp.business.command.activation.impl;

import com.ericsson.fdp.business.command.activation.FDPActivationCommand;
import com.ericsson.fdp.business.constants.MMLCommandConstants;
import com.ericsson.fdp.business.constants.NotificationConstants;
import com.ericsson.fdp.business.enums.AuxRequestParam;
import com.ericsson.fdp.business.enums.ConfigurationKey;
import com.ericsson.fdp.business.exception.NotificationFailedException;
import com.ericsson.fdp.business.util.CacheAccessUtil;
import com.ericsson.fdp.business.util.LoggerUtil;
import com.ericsson.fdp.business.util.NotificationUtil;
import com.ericsson.fdp.business.util.RequestUtil;
import com.ericsson.fdp.common.enums.Status;
import com.ericsson.fdp.core.command.FDPCommand;
import com.ericsson.fdp.core.exception.ExecutionFailedException;
import com.ericsson.fdp.core.logging.FDPLogger;
import com.ericsson.fdp.core.request.FDPRequest;

/**
 * This class is used to provide a default interceptor.
 * 
 * @author Ericsson
 * 
 */
public class FDPDefaultActivationCommandImpl implements FDPActivationCommand {

	/**
	 * The command that is to be intercepted.
	 */
	private final FDPCommand fdpCommand;

	/**
	 * The command to be used.
	 * 
	 * @param fdpCommand
	 *            the command.
	 */
	public FDPDefaultActivationCommandImpl(final FDPCommand fdpCommand) {
		this.fdpCommand = fdpCommand;
	}

	@Override
	public final Status execute(final FDPRequest input, final Object... otherParams) throws ExecutionFailedException {
		Status status = Status.SUCCESS;
		if (preProcess(input) && Status.SUCCESS.equals(status = process(input, otherParams))) {
			postProcess();
		}
		return status;
	}

	/**
	 * This method is used to provide the functionality of processing the
	 * command.
	 * 
	 * @param input
	 *            the request.
	 * @param otherParams
	 *            other parameters as required.
	 * @return the status of execution.
	 * @throws ExecutionFailedException
	 *             Exception in execution.
	 */
	protected Status process(final FDPRequest input, final Object... otherParams) throws ExecutionFailedException {
		final Status status = fdpCommand.execute(input, otherParams);
		input.addExecutedCommand(fdpCommand);
		return status;
	}

	/**
	 * This method is used to provide post process functionality.
	 * 
	 * @throws ExecutionFailedException
	 *             Exception in execution.
	 */
	protected void postProcess() throws ExecutionFailedException {
	}

	/**
	 * This method is used to provide pre-processing functionality.
	 * 
	 * @param fdpRequest
	 *            the request.
	 * @return true, if processing is to be done, false otherwise.
	 * @throws ExecutionFailedException
	 *             Exception in execution.
	 */
	protected boolean preProcess(final FDPRequest fdpRequest) throws ExecutionFailedException {
		return true;
	}

	/**
	 * @return the fdpCommand
	 */
	public FDPCommand getFdpCommand() {
		return fdpCommand;
	}

	/**
	 * This method is used to check if the circle is ICR circle, and if the
	 * number is whitelisted.
	 * 
	 * @param fdpRequest
	 *            the request object.
	 * 
	 * @return true, if the number is allowed.
	 */
	protected boolean checkForICRCircleQualified(final FDPRequest fdpRequest) {
		boolean isIcrCircleQual = true;
		final String icrCircleOption = fdpRequest.getCircle().getConfigurationKeyValueMap()
				.get(ConfigurationKey.ICR_CIRCLE.getAttributeName());
		final Boolean checkIcrOption = fdpRequest.getAuxiliaryRequestParameter(AuxRequestParam.ACTIVATION_ICR_MODE) != null ? (Boolean) fdpRequest
				.getAuxiliaryRequestParameter(AuxRequestParam.ACTIVATION_ICR_MODE) : Boolean.TRUE;
		FDPLogger.debug(LoggerUtil.getSummaryLoggerFromRequest(fdpRequest), getClass(), "checkForICRCircleQuailified",
				LoggerUtil.getRequestAppender(fdpRequest) + " icr circle to be checked " + checkIcrOption);
		if (checkIcrOption) {
			if (icrCircleOption != null
					&& MMLCommandConstants.ICR_CIRCLE_YES_OPTIONS.contains(icrCircleOption.toLowerCase())) {
				isIcrCircleQual = CacheAccessUtil.doesICRSubscriberExistInCache(fdpRequest.getCircle().getCircleCode(),
						fdpRequest.getIncomingSubscriberNumber().toString());
			}
		}
		if (!isIcrCircleQual) {
			sendNotification(fdpRequest);
		}
		return isIcrCircleQual;
	}

	/**
	 * This method is used to send notification.
	 * 
	 * @param fdpRequest
	 *            the request object.
	 */
	private void sendNotification(final FDPRequest fdpRequest) {
		final Boolean notificationSent = (fdpRequest
				.getAuxiliaryRequestParameter(AuxRequestParam.ICR_NOTIFICATION_SENT) == null) ? Boolean.FALSE
				: (Boolean) fdpRequest.getAuxiliaryRequestParameter(AuxRequestParam.ICR_NOTIFICATION_SENT);
		if (!notificationSent) {
			try {
				NotificationUtil.sendOfflineNotification(fdpRequest, NotificationUtil.createNotificationText(
						fdpRequest, (NotificationConstants.NOTIFICATION_FOR_NON_ICR_CIRCLE - fdpRequest.getCircle()
								.getCircleId()), LoggerUtil.getSummaryLoggerFromRequest(fdpRequest)), true);
				RequestUtil.putAuxiliaryValueInRequest(fdpRequest, AuxRequestParam.ICR_NOTIFICATION_SENT, Boolean.TRUE);
			} catch (final NotificationFailedException e) {
				FDPLogger.error(LoggerUtil.getSummaryLoggerFromRequest(fdpRequest), getClass(),
						"checkForICRCircleQuailified", LoggerUtil.getRequestAppender(fdpRequest)
								+ " notification could not be sent ", e);
			}
		}
	}
}
