package com.ericsson.fdp.business.rule.impl;

import java.util.List;

import ch.qos.logback.classic.Logger;

import com.ericsson.fdp.business.FDPRollbackable;
import com.ericsson.fdp.business.FDPStepRollback;
import com.ericsson.fdp.business.NoRollbackOnFailure;
import com.ericsson.fdp.business.charging.ProductChargingStep;
import com.ericsson.fdp.business.constants.FDPStepResponseConstants;
import com.ericsson.fdp.business.enums.AuxRequestParam;
import com.ericsson.fdp.business.enums.ExecutionStatus;
import com.ericsson.fdp.business.enums.RequestMetaValuesKey;
import com.ericsson.fdp.business.exception.RollbackException;
import com.ericsson.fdp.business.exception.RuleException;
import com.ericsson.fdp.business.exception.StepException;
import com.ericsson.fdp.business.fuzzy.FuzzyCodeHandler;
import com.ericsson.fdp.business.product.Product;
import com.ericsson.fdp.business.request.impl.FDPResponseImpl;
import com.ericsson.fdp.business.request.impl.FDPStepResponseImpl;
import com.ericsson.fdp.business.rule.Rule;
import com.ericsson.fdp.business.step.FDPStep;
import com.ericsson.fdp.business.step.impl.AttachProvisioningStep;
import com.ericsson.fdp.business.step.impl.OfflineNotificationStep;
import com.ericsson.fdp.business.util.LoggerUtil;
import com.ericsson.fdp.business.util.RequestUtil;
import com.ericsson.fdp.business.util.ResponseUtil;
import com.ericsson.fdp.business.util.ServiceProvisioningUtil;
import com.ericsson.fdp.common.constants.FDPConstant;
import com.ericsson.fdp.common.enums.ExternalSystem;
import com.ericsson.fdp.common.enums.Status;
import com.ericsson.fdp.core.cache.AppCacheSubStore;
import com.ericsson.fdp.core.command.FDPCommand;
import com.ericsson.fdp.core.dsm.framework.service.FDPCache;
import com.ericsson.fdp.core.exception.ExecutionFailedException;
import com.ericsson.fdp.core.logging.FDPLogger;
import com.ericsson.fdp.core.request.FDPRequest;
import com.ericsson.fdp.core.request.FDPResponse;
import com.ericsson.fdp.core.request.FDPStepResponse;
import com.ericsson.fdp.core.request.ResponseError;
import com.ericsson.fdp.core.request.impl.FDPRequestImpl;
import com.ericsson.fdp.core.serviceprovisioning.bean.FDPAppBag;
import com.ericsson.fdp.core.utils.ApplicationConfigUtil;
import com.ericsson.fdp.dao.dto.FDPResultCodesDTO;

/*
 Feature Name: User can purchase bundle for self and others
 Changes: execute() and executeRollback() method updated to support buy for other case.
 Date: 28-10-2015
 Singnum Id:ESIASAN
 */

/**
 * This class defines the implementation of the rule.
 * 
 * @author Ericsson
 * 
 */
public class RuleImpl implements Rule {

	/**
	 *
	 */
	private static final long serialVersionUID = 770177094085979190L;
	/** The list of steps that are to be executed for this rule. */
	private List<FDPStep> fdpSteps;

	/**
	 * This defines if rollback is to be performed or not for this rule.
	 */
	private boolean performRollback;
	
	/** This defines the case when need to send the Async Notification by skipping the configured SP notifications **/
	protected boolean isAsyncNotificationCase = false;

	/**
	 * The constructor to create fdp rule.
	 * 
	 * @param fdpStepsToSet
	 *            The steps to set.
	 * @param performRollbackToSet
	 *            True, if rollback is to be performed, false otherwise.
	 */
	public RuleImpl(final List<FDPStep> fdpStepsToSet,
			final boolean performRollbackToSet) {
		this.fdpSteps = fdpStepsToSet;
		this.performRollback = performRollbackToSet;
	}

	@Override
	public FDPResponse execute(final FDPRequest fdpRequest)
			throws RuleException {
		Logger circleLogger = LoggerUtil
				.getSummaryLoggerFromRequest(fdpRequest);
		FDPResponseImpl fdpServiceProvisioningResponse = new FDPResponseImpl(
				Status.SUCCESS, true, null);
		int stepCount = 0;
		logValue(fdpRequest, circleLogger, "Forward", "Start");
		boolean initiateRollback = false;
		boolean initiateskipStepsMM = false;
		for (; stepCount < fdpSteps.size(); stepCount++) {
			FDPStep fdpStep = fdpSteps.get(stepCount);
			FDPLogger.debug(circleLogger, getClass(), "execute()",
					LoggerUtil.getRequestAppender(fdpRequest)
							+ "Executing step :- " + fdpStep);
			try {
				// Execute each step.
				if (!(fdpStep instanceof ProductChargingStep || fdpStep instanceof OfflineNotificationStep)) {
					ServiceProvisioningUtil
							.updateSubscriberInRequestForBeneficiary(
									fdpRequest, true, circleLogger);
				}
				FDPStepResponse fdpStepResponse = fdpStep
						.executeStep(fdpRequest);
				RequestUtil.putStepResponseInRequest(fdpStepResponse,
						fdpRequest, fdpStep.getStepName());
				ServiceProvisioningUtil.decorateSPResponse(fdpServiceProvisioningResponse, fdpStep, fdpStepResponse);
				if (!RequestUtil.checkExecutionStatus(fdpStepResponse, fdpStep)) {
					FDPLogger.debug(circleLogger, getClass(), "execute()",
							LoggerUtil.getRequestAppender(fdpRequest)
									+ "Step failed :- " + fdpStep);
					fdpServiceProvisioningResponse
							.setExecutionStatus(Status.FAILURE);
					fdpServiceProvisioningResponse
							.setResponseError(new ResponseError(
									(String) fdpStepResponse
											.getStepResponseValue(FDPStepResponseConstants.ERROR_CODE),
									(String) fdpStepResponse
											.getStepResponseValue(FDPStepResponseConstants.ERROR_VALUE),
									(String) fdpStepResponse
											.getStepResponseValue(FDPStepResponseConstants.ERROR_TYPE),
									(String) fdpStepResponse
											.getStepResponseValue(FDPStepResponseConstants.EXTERNAL_SYSTEM_TYPE)));
					initiateRollback = true;
					//initiateRollback = intitateRollbackCheck(fdpRequest);
					if (!initiateRollback)
						fdpServiceProvisioningResponse
								.setExecutionStatus(Status.SUCCESS);

					break;
				} /* Commented for Mobile money by eagarsh artf780189 obsolete code
				    else if ((fdpRequest.getExternalSystemToCharge() != null) ? (fdpRequest
						.getExternalSystemToCharge().equals(ExternalSystem.MM) && (Boolean) fdpStepResponse
						.getStepResponseValue("STATUS"))
						// &&
						// ((Boolean)fdpRequest.getAuxiliaryRequestParameter(AuxRequestParam.SKIP_CHARGING)!=false)
						&& ((FDPRequestImpl) fdpRequest)
								.getAuxiliaryRequestParameter(AuxRequestParam.MM_SKIP_CHARGING) == null: false) {
					Object skipCharging = ((FDPRequestImpl) fdpRequest)
							.getAuxiliaryRequestParameter(AuxRequestParam.SKIP_CHARGING);
					if (skipCharging == null) {
						((FDPRequestImpl) fdpRequest)
								.putAuxiliaryRequestParameter(
										AuxRequestParam.MM_SKIP_CHARGING,
										Boolean.TRUE);
						initiateskipStepsMM = true;
					} else if ((Boolean) skipCharging == false) {
						((FDPRequestImpl) fdpRequest)
								.putAuxiliaryRequestParameter(
										AuxRequestParam.MM_SKIP_CHARGING,
										Boolean.TRUE);
						initiateskipStepsMM = true;
					}

				}*/ else if (ResponseUtil.isReponseContainsAsyn(fdpStepResponse)){
					isAsyncNotificationCase = true;
					FDPStepResponseImpl fdpStepResponseImpl = (FDPStepResponseImpl) fdpStepResponse;
					fdpStepResponseImpl.addStepResponseValue(FDPStepResponseConstants.IS_CURRENT_COMMAND_ASYNC, Boolean.FALSE);
					fdpServiceProvisioningResponse.setExecutionStatus(Status.PENDING_ON_MM);
					break;
				}
			} catch (StepException e) {
				if (fdpStep instanceof NoRollbackOnFailure) {
					FDPLogger.error(circleLogger, getClass(), "execute()",
							LoggerUtil.getRequestAppender(fdpRequest)
									+ "Step could not be executed", e);

					// IMP:For EMA rollback put the log here with some syntax
					// that will not execute the rollback
					continue;
				}
				// If exception occurred while executing step, rollback.
				// initiateRollback = true;

				// Handling for errorCode 100
				//initiateRollback = intitateRollbackCheck(fdpRequest);
				initiateRollback = true;
				fdpServiceProvisioningResponse
						.setExecutionStatus(Status.FAILURE);
				FDPLogger.error(circleLogger, getClass(), "execute()",
						LoggerUtil.getRequestAppender(fdpRequest)
								+ "Step could not be executed", e);
				throw new RuleException("Step could not be executed", e);
			} finally {
				if (initiateRollback && performRollback) {
					logValue(fdpRequest, circleLogger, "Forward", "End");
					logValue(fdpRequest, circleLogger, "Rollback", "Start");
					FDPLogger.debug(circleLogger, getClass(), "execute()",
							LoggerUtil.getRequestAppender(fdpRequest)
									+ "Executing rollback");
					try {
						executeRollback(stepCount, fdpRequest);
						logValue(fdpRequest, circleLogger, "Rollback", "End");
						break;
					} catch (RollbackException e) {
						// If exception occurred while rollback, inform caller.
						FDPLogger.error(circleLogger, getClass(), "execute()",
								LoggerUtil.getRequestAppender(fdpRequest)
										+ "Rollback could not be executed", e);
						throw new RuleException(
								"Rollback could not be executed", e);
					}
				} /*Commented code for Mobile money by eagarsh artf780189 obsolete code
				else if (initiateskipStepsMM) {
					fdpServiceProvisioningResponse
							.setExecutionStatus(Status.SUCESS_ON_MM);

					return fdpServiceProvisioningResponse;
				}*/ 
				else if(null !=fdpRequest.getAuxiliaryRequestParameter(AuxRequestParam.BENEFICIARY_MSISDN))
					ServiceProvisioningUtil.updateSubscriberInRequestForBeneficiary(fdpRequest, true, circleLogger);
				else {
					ServiceProvisioningUtil
							.updateSubscriberInRequestForBeneficiary(
									fdpRequest, false, circleLogger);
				}
			}
		}
		if (!(initiateRollback && performRollback)) {
			logValue(fdpRequest, circleLogger, "Forward", "End");
		}
		return fdpServiceProvisioningResponse;
	}

	private void logValue(final FDPRequest fdpRequest,
			final Logger circleLogger, final String provMod,
			final String provAct) {
		FDPLogger.info(circleLogger, getClass(), "execute()",
				LoggerUtil.getRequestAppender(fdpRequest) + "PROVMOD"
						+ FDPConstant.LOGGER_KEY_VALUE_DELIMITER + provMod
						+ FDPConstant.LOGGER_DELIMITER + "PROVACT"
						+ FDPConstant.LOGGER_KEY_VALUE_DELIMITER + provAct);
	}

	/**
	 * This method is used to rollback the steps that have been executed.
	 * 
	 * @param stepCount
	 *            The step count from which the rollback is to be done.
	 * @param fdpRequest
	 *            The request object.
	 * @throws RollbackException
	 *             Exception, if it occurs while rollback.
	 */
	private void executeRollback(final int stepCount,
			final FDPRequest fdpRequest) throws RollbackException {
		Logger circleLogger = LoggerUtil
				.getSummaryLoggerFromRequest(fdpRequest);
		if (fdpRequest instanceof FDPRequestImpl) {
			FDPRequestImpl fdpRequestImpl = (FDPRequestImpl) fdpRequest;
			fdpRequestImpl.setExecutionStatus(ExecutionStatus.BACKWARD);
		}
		if (executeStepRollback(stepCount, fdpRequest)) {
		//	logMMRoleBack(fdpRequest);
			for (int stepToRollBack = stepCount - 1; stepToRollBack >= 0; stepToRollBack--) {
				FDPStep fdpStep = fdpSteps.get(stepToRollBack);
				FDPLogger.debug(circleLogger, getClass(), "executeRollback()",
						LoggerUtil.getRequestAppender(fdpRequest)
								+ "Rollbacking step " + fdpStep);
				// If the step can be rollback-ed, rollback the step.
				if (fdpStep instanceof FDPRollbackable) {
					try {
						if (!(fdpStep instanceof ProductChargingStep)) {
							ServiceProvisioningUtil
									.updateSubscriberInRequestForBeneficiary(
											fdpRequest, true, circleLogger);
						}
						if (!((FDPRollbackable) fdpStep)
								.performRollback(fdpRequest)) {
							break;
						}
					} finally {
						ServiceProvisioningUtil
								.updateSubscriberInRequestForBeneficiary(
										fdpRequest, false, circleLogger);
					}

				}
			}
		}
	}

	/**
	 * MM Rollback Log ehlnopu code to be removed
	 * */
	private void logMMRoleBack(FDPRequest fdpRequest) {
		// TODO Auto-generated method stub
		if (!(fdpRequest.getLastExecutedCommand().getCommandDisplayName()
				.equals(FDPConstant.MM_CHARGING_COMMAND))
				&& fdpRequest.getExternalSystemToCharge().equals(
						ExternalSystem.MM)) {
			Logger circleLogger = LoggerUtil
					.getSummaryLoggerFromRequest(fdpRequest);

			FDPLogger
					.error(circleLogger,
							getClass(),
							" Rollback ",
							LoggerUtil.getRequestAppender(fdpRequest)
									+ "PROV FAIL MM> Subscriber :"
									+ fdpRequest.getSubscriberNumber()
									+ ",Beneficiary"
									+ (fdpRequest
											.getAuxiliaryRequestParameter(AuxRequestParam.BENEFICIARY_MSISDN) != null ? fdpRequest
											.getAuxiliaryRequestParameter(AuxRequestParam.BENEFICIARY_MSISDN)
											: "none")
									+ ",FOR PRODUCT"
									+ ((Product) fdpRequest
											.getValueFromRequest(RequestMetaValuesKey.PRODUCT))
											.getProductName());
		}
	}

	/**
	 * This method is used to execute step rollback.
	 * 
	 * @param stepCount
	 *            the step count for which rollback is to be done.
	 * @param fdpRequest
	 *            the request object.
	 * @return true of successfull, false otherwise
	 * @throws RollbackException
	 *             Exception if any.
	 */
	// change by sachin.
	/*
	 * private boolean executeStepRollback(final int stepCount, final FDPRequest
	 * fdpRequest) throws RollbackException { boolean executeRollback = true; if
	 * (stepCount < fdpSteps.size()) { FDPStep fdpStep =
	 * fdpSteps.get(stepCount); if (fdpStep instanceof FDPStepRollback ||
	 * fdpStep instanceof AttachProvisioningStep) { executeRollback =
	 * ((FDPStepRollback) fdpStep).performStepRollback(fdpRequest); } } return
	 * executeRollback; }
	 */

	private boolean executeStepRollback(final int stepCount,
			final FDPRequest fdpRequest) throws RollbackException {
		boolean executeRollback = true;
		if (stepCount < fdpSteps.size()) {
			FDPStep fdpStep = fdpSteps.get(stepCount);
			if (fdpStep instanceof FDPStepRollback) {
				executeRollback = ((FDPStepRollback) fdpStep).performStepRollback(fdpRequest);
			} else if (fdpStep instanceof AttachProvisioningStep) {
				executeRollback = ((AttachProvisioningStep) fdpStep).performRollback(fdpRequest);
			}
		}
		return executeRollback;
	}

	@Override
	public String toString() {
		return " ruleImpl. Steps defined are :- " + fdpSteps;
	}

	public List<FDPStep> getFdpSteps() {
		return fdpSteps;
	}

	/**
	 * This method will check the Error codes with command in app cache and
	 * execute the Error Logic
	 * */
	private boolean intitateRollbackCheck(FDPRequest fdpRequest) {
		// TODO Auto-generated method stub
		
		try {
			
			FDPCommand fdpcommand = fdpRequest.getLastExecutedCommand();
			if(fdpcommand!=null)
			{
			FDPAppBag appBag = new FDPAppBag();
			final FDPCache<FDPAppBag, Object> configCache = ApplicationConfigUtil
					.getApplicationConfigCache();

		
			Integer fdpFuzzyCode = Integer.parseInt(fdpcommand
					.getResponseError().getResponseCode());
			appBag.setSubStore(AppCacheSubStore.COMMAND_RESULT_CODES_MAPPING);
			appBag.setKey(fdpcommand.getCommandDisplayName() + FDPConstant.DOT
					+ fdpcommand.getResponseError().getResponseCode());
			FDPResultCodesDTO fdpresultCodeDto = (FDPResultCodesDTO) configCache
					.getValue(new FDPAppBag(
							AppCacheSubStore.COMMAND_RESULT_CODES_MAPPING,
							(fdpcommand.getCommandDisplayName()
									+ FDPConstant.PARAMETER_SEPARATOR + fdpcommand
									.getResponseError().getResponseCode())));

			if (fdpresultCodeDto != null && fdpresultCodeDto.isFuzzy())
				return new FuzzyCodeHandler().handleFuzzyCode(fdpRequest,
						fdpcommand, fdpresultCodeDto, fdpFuzzyCode);
			
			}
		
		} catch (ExecutionFailedException e) {
			e.printStackTrace();
		}
		
		
		
		return true;
	}

}
