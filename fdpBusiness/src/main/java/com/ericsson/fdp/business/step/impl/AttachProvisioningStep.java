package com.ericsson.fdp.business.step.impl;

import java.util.List;

import ch.qos.logback.classic.Logger;

import com.ericsson.fdp.FDPCacheable;
import com.ericsson.fdp.business.FDPRollbackable;
import com.ericsson.fdp.business.constants.FDPStepResponseConstants;
import com.ericsson.fdp.business.enums.AuxRequestParam;
import com.ericsson.fdp.business.exception.RollbackException;
import com.ericsson.fdp.business.exception.StepException;
import com.ericsson.fdp.business.request.impl.FDPStepResponseImpl;
import com.ericsson.fdp.business.step.FDPStep;
import com.ericsson.fdp.business.util.AsyncCommandUtil;
import com.ericsson.fdp.business.util.LoggerUtil;
import com.ericsson.fdp.business.util.RequestUtil;
import com.ericsson.fdp.business.util.ResponseUtil;
import com.ericsson.fdp.business.vo.FDPAsycCommandVO;
import com.ericsson.fdp.common.constants.FDPConstant;
import com.ericsson.fdp.common.enums.ModuleType;
import com.ericsson.fdp.core.command.FDPCommand;
import com.ericsson.fdp.core.dsm.framework.service.FDPCache;
import com.ericsson.fdp.core.exception.ExecutionFailedException;
import com.ericsson.fdp.core.logging.FDPLogger;
import com.ericsson.fdp.core.request.FDPRequest;
import com.ericsson.fdp.core.request.FDPStepResponse;
import com.ericsson.fdp.core.request.impl.FDPRequestImpl;
import com.ericsson.fdp.core.serviceprovisioning.bean.FDPMetaBag;
import com.ericsson.fdp.core.serviceprovisioning.bean.FDPRequestBag;
import com.ericsson.fdp.core.utils.ApplicationConfigUtil;

/**
 * The class defines the attach provisioning step.
 * 
 * @author Ericsson
 * 
 */
public class AttachProvisioningStep implements FDPRollbackable, FDPStep {

	/**
	 * 
	 */
	private static final long serialVersionUID = -3239884225899597801L;

	/** The attach provisioning condition steps. */
	private List<AttachProvisioningConditionStep> attachProvisioning;

	/** The step which was executed from the condition steps. */
	private AttachProvisioningConditionStep stepExecuted;

	/** The step which was executed from the condition steps. */
	// private AttachProvisioningConditionStep stepFailed;

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
	 * The constructor for the attach provisioning step.
	 * 
	 * @param attachProvisioningToSet
	 *            The attach provisioning condition to set.
	 * @param stepId
	 *            the step id
	 * @param stepName
	 *            the step name
	 */
	public AttachProvisioningStep(
			final List<AttachProvisioningConditionStep> attachProvisioningToSet,
			final Long stepId, final String stepName) {
		this.attachProvisioning = attachProvisioningToSet;
		this.stepId = stepId;
		this.stepName = stepName;
	}

	@Override
	public FDPStepResponse executeStep(final FDPRequest fdpRequest)
			throws StepException {
		Logger circleLogger = LoggerUtil
				.getSummaryLoggerFromRequest(fdpRequest);
		FDPStepResponse stepResponse = null;
		for (AttachProvisioningConditionStep fdpAttachProvisioningStep : attachProvisioning) {
			// condition is provided to check wheather step is executed used in async commands
			if (!fdpAttachProvisioningStep.isStepExecuted()) {
				FDPLogger.debug(circleLogger, getClass(), "executeStep()",
						LoggerUtil.getRequestAppender(fdpRequest)
								+ "Executing attach service provisioning step "
								+ fdpAttachProvisioningStep);
				stepResponse = fdpAttachProvisioningStep
						.executeStep(fdpRequest);
				if (RequestUtil.checkExecutionStatus(stepResponse)) {
					FDPLogger
							.debug(circleLogger,
									getClass(),
									"executeStep()",
									LoggerUtil.getRequestAppender(fdpRequest)
											+ "Attach Service provisioning step executed."
											+ fdpAttachProvisioningStep);
					stepExecuted = fdpAttachProvisioningStep;

					// set the step as executed used in async flow for contraint
					// step on
					// service provisioning
					fdpAttachProvisioningStep.setStepExecuted(true);
					if (ResponseUtil.isReponseContainsAsyn(stepResponse)) {
						((FDPRequestImpl) fdpRequest)
								.putAuxiliaryRequestParameter(
										AuxRequestParam.ASYNC_REQUESTID,
										fdpRequest.getRequestId());
						// when it is async type of request what need to be
						// done after
						// is handled by reflection and use the class
						// defined in
						storeInAsyncCache(fdpRequest, circleLogger);

						break;
					}

					break;
				} else if (RequestUtil
						.checkExecutionStatus(stepResponse
								.getStepResponseValue(FDPStepResponseConstants.COMMAND_STATUS_KEY))) {
					FDPLogger
							.debug(circleLogger,
									getClass(),
									"executeStep()",
									LoggerUtil.getRequestAppender(fdpRequest)
											+ "Attach Service provisioning step executed."
											+ fdpAttachProvisioningStep);

					// stepFailed = fdpAttachProvisioningStep;

					// set the step as executed used in async flow for contraint
					// step on
					// service provisioning
					stepExecuted = fdpAttachProvisioningStep;
					fdpAttachProvisioningStep.setStepExecuted(true);
					// Note this is repetead code should put it in any Util
					// class
					if (ResponseUtil.isReponseContainsAsyn(stepResponse)) {
						((FDPRequestImpl) fdpRequest)
								.putAuxiliaryRequestParameter(
										AuxRequestParam.ASYNC_REQUESTID,
										fdpRequest.getRequestId());
						// when it is async type of request what need to be
						// done after
						// is handled by reflection and use the class
						// defined in
						storeInAsyncCache(fdpRequest, circleLogger);

						break;
					}
					break;
				}
			}
			else if(fdpAttachProvisioningStep.isStepExecuted())
			{
				break;
			}
		}
		if (stepExecuted == null) {
			FDPLogger.debug(circleLogger, getClass(), "executeStep()",
					LoggerUtil.getRequestAppender(fdpRequest)
							+ "No step was executed");
			return RequestUtil.createStepResponse(false, stepResponse, fdpRequest);
			
		}
		if (stepResponse != null
				&& !((Boolean) stepResponse.getStepResponseValue("STATUS"))) {
			return RequestUtil.createStepResponse(false, stepResponse, fdpRequest);
		}
		FDPStepResponse fdpStepResponse = RequestUtil.createStepResponse(true, stepResponse, fdpRequest);
		FDPStepResponseImpl fdpStepResponseImpl = (FDPStepResponseImpl) fdpStepResponse;
		fdpStepResponseImpl.addStepResponseValue(FDPStepResponseConstants.IS_CURRENT_COMMAND_ASYNC,ResponseUtil.isReponseContainsAsyn(stepResponse));
		return fdpStepResponse;
	}

	/**
	 * store complete command in cache Note : this method should be put in util
	 * class
	 * 
	 * @param fdpRequest
	 * @param circleLogger
	 */
	private void storeInAsyncCache(FDPRequest fdpRequest, Logger circleLogger) {
		FDPMetaBag metaBag = new FDPMetaBag(fdpRequest.getCircle(),
				ModuleType.ASYNC_COMMANDS, fdpRequest.getLastExecutedCommand()
						.getCommandDisplayName());
		FDPCache<FDPMetaBag, FDPCacheable> fdpCache = null;
		String transactionid = null;
		try {
			fdpCache = ApplicationConfigUtil.getMetaDataCache();
			FDPAsycCommandVO asynccommandvo = (FDPAsycCommandVO) fdpCache
					.getValue(metaBag);

			if (asynccommandvo != null) {
				if (asynccommandvo.getTransactionparamtype().trim()
						.equalsIgnoreCase(FDPConstant.INPUT)) {
					if(fdpRequest
							.getLastExecutedCommand()!=null && fdpRequest
									.getLastExecutedCommand().getInputParam()!=null){
					transactionid = fdpRequest
							.getLastExecutedCommand()
							.getInputParam(asynccommandvo.getTransactionparam())
							.getValue().toString();
					FDPRequestBag fdprequestBag = new FDPRequestBag(
							transactionid);
					ApplicationConfigUtil.getRequestCacheForMMWeb().putValue(
							fdprequestBag, fdpRequest);
					}

				} else if (asynccommandvo.getTransactionparamtype().trim()
						.equalsIgnoreCase(FDPConstant.OUTPUT)) {
					if(fdpRequest
							.getLastExecutedCommand()!=null && fdpRequest
									.getLastExecutedCommand().getOutputParams()!=null){
				transactionid = fdpRequest
						.getLastExecutedCommand()
						.getOutputParam(
								asynccommandvo.getTransactionparam())!=null?fdpRequest
							.getLastExecutedCommand()
							.getOutputParam(
									asynccommandvo.getTransactionparam())
							.getValue().toString():null;
					
					if(null==transactionid)
					{
						throw new ExecutionFailedException("No Output Parameter in response");
					}
				
					FDPRequestBag fdprequestBag = new FDPRequestBag(
							transactionid);
					ApplicationConfigUtil.getRequestCacheForMMWeb().putValue(
							fdprequestBag, fdpRequest);
					}

				}
			}
			// return true;
		} catch (ExecutionFailedException e) {
			FDPLogger.error(circleLogger, getClass(), "checkIsAsyncCommand()",
					"Not able to find Meta Cache");
		}
		// return false;

	}

	/**
	 * Check wheather command is async or not .Note : this method should be put
	 * in util class
	 * 
	 * @param fdpRequest
	 * @param circleLogger
	 * @return
	 */
	private boolean checkIsAsyncCommand(FDPRequest fdpRequest,
			Logger circleLogger) {
		if (fdpRequest.getLastExecutedCommand() != null) {
			FDPMetaBag metaBag = new FDPMetaBag(fdpRequest.getCircle(),
					ModuleType.ASYNC_COMMANDS, fdpRequest
							.getLastExecutedCommand().getCommandDisplayName());
			FDPCache<FDPMetaBag, FDPCacheable> fdpCache = null;
			try {
				fdpCache = ApplicationConfigUtil.getMetaDataCache();
				if (fdpCache.getValue(metaBag) != null)
					return true;
			} catch (ExecutionFailedException e) {
				FDPLogger.error(circleLogger, getClass(),
						"checkIsAsyncCommand()", "Not able to find Meta Cache");
			}
		}
		return false;
	}

	@Override
	public boolean performRollback(final FDPRequest fdpRequest)
			throws RollbackException {
		Logger circleLogger = LoggerUtil
				.getSummaryLoggerFromRequest(fdpRequest);
		AttachProvisioningConditionStep stepToRollback = stepExecuted;
		FDPLogger.debug(circleLogger, getClass(), "performRollback()",
				LoggerUtil.getRequestAppender(fdpRequest)
						+ "Performing rollback for step " + stepToRollback);
		return stepToRollback != null ? stepToRollback
				.performRollback(fdpRequest) : true;
	}

	@Override
	public String toString() {
		return " attach provisioning step. Attach provisioning steps :- "
				+ attachProvisioning;
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

	public List<AttachProvisioningConditionStep> getAttachProvisioning() {
		return attachProvisioning;
	}

	@Override
	public boolean isStepExecuted() {
		return executed;
	}

	@Override
	public void setStepExecuted(boolean stepexecuted) {
		this.executed = stepexecuted;
	}
	
}
