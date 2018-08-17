package com.ericsson.fdp.business.step.impl;

import ch.qos.logback.classic.Logger;

import com.ericsson.fdp.business.NoRollbackOnFailure;
import com.ericsson.fdp.business.command.param.impl.CommandParamInput;
import com.ericsson.fdp.business.exception.NotificationFailedException;
import com.ericsson.fdp.business.exception.StepException;
import com.ericsson.fdp.business.notification.impl.ServiceProvisioningNotificationImpl;
import com.ericsson.fdp.business.step.FDPStep;
import com.ericsson.fdp.business.util.ClassUtil;
import com.ericsson.fdp.business.util.LoggerUtil;
import com.ericsson.fdp.business.util.RequestUtil;
import com.ericsson.fdp.common.enums.Status;
import com.ericsson.fdp.core.exception.EvaluationFailedException;
import com.ericsson.fdp.core.logging.FDPLogger;
import com.ericsson.fdp.core.request.FDPRequest;
import com.ericsson.fdp.core.request.FDPStepResponse;

/**
 * The class defines the offline notification step.
 * 
 * @author Ericsson
 * 
 */
public class OfflineNotificationStep implements FDPStep, NoRollbackOnFailure {

	/**
	 * 
	 */
	private static final long serialVersionUID = 4651269313327868403L;

	/**
	 * The command param input.
	 */
	private CommandParamInput commandParamInput;

	private ServiceProvisioningNotificationImpl fdpNotification;

	/**
	 * The step name.
	 */
	private String stepName;

	/**
	 * The step id.
	 */
	private Long stepId;

	private boolean executed;

	/**
	 * Instantiates a new offline notification step.
	 * 
	 * @param commandParameterInput
	 *            the command parameter input
	 * @param fdpNotification
	 *            the fdp notification
	 * @param stepId
	 *            the step id
	 * @param stepName
	 *            the step name
	 */
	public OfflineNotificationStep(final CommandParamInput commandParameterInput,
			final ServiceProvisioningNotificationImpl fdpNotification, final Long stepId, final String stepName) {
		this.commandParamInput = commandParameterInput;
		this.fdpNotification = fdpNotification;
		this.stepId = stepId;
		this.stepName = stepName;
	}

	@Override
	public FDPStepResponse executeStep(final FDPRequest fdpRequest) throws StepException {
		try {
			commandParamInput.evaluateValue(fdpRequest);
			Object msisdnToSend = commandParamInput.getValue();
			String notificationText = fdpNotification.createNotificationText(fdpRequest, Status.SUCCESS);
			sendOfflineNotification((Long) ClassUtil.getPrimitiveValueReturnNotNullObject(msisdnToSend,
					commandParamInput.getPrimitiveValue().getClazz()), fdpRequest, notificationText);
		} catch (EvaluationFailedException e) {
			throw new StepException("The parameter value could not be found ", e);
		} catch (NotificationFailedException e) {
			throw new StepException("The notification could not be created ", e);
		}

		return RequestUtil.createStepResponse(true);
	}

	/**
	 * This method is used to send offline notification.
	 * 
	 * @param msisdn
	 *            the msisdn to which the notification is to be sent.
	 * @param fdpRequest
	 *            the request object.
	 * @param notificationText
	 *            the text to be sent.
	 */
	public void sendOfflineNotification(final Long msisdn, final FDPRequest fdpRequest, final String notificationText) {
		Runnable runnable = new Runnable() {

			@Override
			public void run() {
				try {
					fdpNotification.sendNotification(msisdn, fdpNotification.getChannelType(), fdpRequest.getCircle(),
							notificationText, fdpRequest.getRequestId());
				} catch (NotificationFailedException e) {
					Logger logger = LoggerUtil.getSummaryLoggerFromRequest(fdpRequest);
					FDPLogger.error(logger, this.getClass(), "sendOfflineNotification()",
							LoggerUtil.getRequestAppender(fdpRequest) + "Notification could not be sent", e);
				}
			}
		};
		Thread thread = new Thread(runnable);
		thread.start();
	}

	@Override
	public String getStepName() {
		return stepName;
	}

	/**
	 * This method is used to get the step id.
	 * 
	 * @return the step id.
	 */
	public Long getStepId() {
		return stepId;
	}
	
	@Override
	public boolean isStepExecuted() {
		return executed;
	}


	@Override
	public void setStepExecuted(boolean stepexecuted) {
		this.executed=stepexecuted;
	}
	
}
