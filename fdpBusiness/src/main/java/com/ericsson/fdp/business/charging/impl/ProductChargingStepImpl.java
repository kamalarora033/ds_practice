package com.ericsson.fdp.business.charging.impl;

import java.util.List;

import ch.qos.logback.classic.Logger;

import com.ericsson.fdp.FDPCacheable;
import com.ericsson.fdp.business.FDPStepRollback;
import com.ericsson.fdp.business.charging.Discount;
import com.ericsson.fdp.business.charging.ProductCharging;
import com.ericsson.fdp.business.charging.ProductChargingStep;
import com.ericsson.fdp.business.constants.FDPStepResponseConstants;
import com.ericsson.fdp.business.enums.AuxRequestParam;
import com.ericsson.fdp.business.enums.RequestMetaValuesKey;
import com.ericsson.fdp.business.exception.RollbackException;
import com.ericsson.fdp.business.exception.StepException;
import com.ericsson.fdp.business.product.Product;
import com.ericsson.fdp.business.request.impl.FDPStepResponseImpl;
import com.ericsson.fdp.business.util.AsyncCommandUtil;
import com.ericsson.fdp.business.util.ChargingUtil;
import com.ericsson.fdp.business.util.LoggerUtil;
import com.ericsson.fdp.business.util.ProductUtil;
import com.ericsson.fdp.business.util.RequestUtil;
import com.ericsson.fdp.business.vo.CommandExecutionStatus;
import com.ericsson.fdp.common.constants.FDPConstant;
import com.ericsson.fdp.common.enums.ChannelType;
import com.ericsson.fdp.common.enums.Status;
import com.ericsson.fdp.core.exception.ExecutionFailedException;
import com.ericsson.fdp.core.logging.FDPLogger;
import com.ericsson.fdp.core.request.FDPRequest;
import com.ericsson.fdp.core.request.FDPStepResponse;
import com.ericsson.fdp.core.request.impl.FDPRequestImpl;
import com.ericsson.fdp.dao.enums.ChargingType;

/*
Feature Name: AIR + MOBILE MONEY + LOYALITY Charging
Changes: Added filter in product charging based upon selected payment method
Date: 18-12-2015
Singnum Id:ESIASAN
*/

/**
 * This class implements the product charging step. This class provides the
 * implementation to execute product charging.
 * 
 * @author Ericsson
 * 
 */
public class ProductChargingStepImpl implements ProductChargingStep, FDPStepRollback {

	private static final long serialVersionUID = 1010852212632201911L;
	
	/** The step id. */
	private final Long stepId;
	
	/** The step name. */
	private final String stepName;

	/** The charging type. */
	private ChargingType chargingType;

	private int stepFailed;

	private boolean stepExecuted = true;
	
	private List<ProductCharging> productChargingSteps = null;
	
	private Logger circleLogger;
	
	private Discount chargingDiscount = null;

	private boolean executed;

	/**
	 * Instantiates a new product charging step impl.
	 * 
	 * @param stepId
	 *            the step id
	 * @param stepName
	 *            the step name
	 */
	public ProductChargingStepImpl(final Long stepId, final String stepName, final ChargingType chargingType) {
		this.stepId = stepId;
		this.stepName = stepName;
		this.setChargingType(chargingType);
	}

	public ProductChargingStepImpl(final Long stepId, final String stepName, final ChargingType chargingType, final Discount chargingDiscount) {
		this(stepId, stepName, chargingType);
		this.chargingDiscount = chargingDiscount;
	}
	@Override
	public FDPStepResponse executeStep(final FDPRequest fdpRequest) throws StepException {
		circleLogger = LoggerUtil.getSummaryLoggerFromRequest(fdpRequest);
		FDPLogger.debug(circleLogger, getClass(), "executeStep()", LoggerUtil.getRequestAppender(fdpRequest)
				+ "Execting product charging step.");
		productChargingSteps = ProductUtil.getExecutableFDPProductCharging(getProductChargingSteps(fdpRequest));
		ChargingUtil.updateRequestForDefaultChargingFixed(fdpRequest, productChargingSteps);
		productChargingSteps = ChargingUtil.getEligibleChargings(productChargingSteps, fdpRequest, circleLogger);
		return executeStep(fdpRequest, productChargingSteps);
	}

	protected FDPStepResponse executeStep(final FDPRequest fdpRequest,
			final List<ProductCharging> productChargingSteps)
			throws StepException {
		boolean isAsync = false;
		boolean stepExecuted = true;
		circleLogger = LoggerUtil.getSummaryLoggerFromRequest(fdpRequest);
		CommandExecutionStatus commandExecutionStatus = null;
		//added by rahul to skip charging in case of balance enquiry
		 ProductUtil.setSkipChargingForBalanceEnquiry(fdpRequest);
		for (final ProductCharging productChargingStep : productChargingSteps) {
			FDPLogger.debug(circleLogger, getClass(), "executeStep()",
					LoggerUtil.getRequestAppender(fdpRequest)
							+ "Executing product charging step :- "
							+ productChargingStep);
			try {
				Object skipRsCharing = fdpRequest
						.getAuxiliaryRequestParameter(AuxRequestParam.SKIP_RS_CHARGING);
				Object skipCharging = fdpRequest
						.getAuxiliaryRequestParameter(AuxRequestParam.SKIP_CHARGING);
				if (null == skipCharging
						|| (skipCharging instanceof Boolean && Boolean.FALSE
								.equals((Boolean) skipCharging))) {
					if (productChargingStep instanceof RecurringCharging
							&& null != skipRsCharing
							&& skipRsCharing instanceof Boolean) {
						Boolean isSkipingRsCharing = (Boolean) skipRsCharing;
						if (isSkipingRsCharing) {
							commandExecutionStatus = new CommandExecutionStatus(
									Status.SUCCESS, 0, null, null, null);
						}
					} else {
						commandExecutionStatus = productChargingStep
								.execute(fdpRequest);
					}
					if (!Status.SUCCESS.equals(commandExecutionStatus.getStatus())) {
						FDPLogger.debug(circleLogger,
										getClass(),
										"executeStep()",
										LoggerUtil
												.getRequestAppender(fdpRequest)
												+ "Could not execute product charging step :- "
												+ productChargingStep);
						stepExecuted = false;
						break;
					} 
					//Code changes for allowing Async cache store of SP for Async Mobile Money Interface Start
					else if (Status.SUCCESS.equals(commandExecutionStatus.getStatus())
							&& AsyncCommandUtil.checkIsAsyncCommand(fdpRequest,	circleLogger)) {
						isAsync = true;
						stepFailed++;
						this.setStepExecuted(stepExecuted);
						((FDPRequestImpl) fdpRequest)
								.putAuxiliaryRequestParameter(
										AuxRequestParam.ASYNC_REQUESTID,
										fdpRequest.getRequestId());

						AsyncCommandUtil.storeInAsyncCache(fdpRequest,	circleLogger);
						break;
					}
					stepFailed++;
					
				}
				//Code changes for allowing Async cache store of SP for Async Mobile Money Interface End
				else {
					commandExecutionStatus = new CommandExecutionStatus(
							Status.SUCCESS, 0, null, null, null);
				}
				// fdpRequest.setStepFailed(fdpRequest.getStepFailed()+1);
			} catch (final ExecutionFailedException e) {
				stepExecuted = false;
				FDPLogger.error(circleLogger, getClass(), "executeStep()",
						LoggerUtil.getRequestAppender(fdpRequest)
								+ "The charging could not be completed.", e);
				throw new StepException("The charging could not be completed.",
						e);
			}
		}
		String externalSys = null;
		com.ericsson.fdp.common.enums.ExternalSystem externalSystem = commandExecutionStatus
				.getExternalSystem();
		if (externalSystem == null) {
			externalSys = FDPConstant.EXTERNAL_SYSTEM_FDP;
		} else {
			externalSys = commandExecutionStatus.getExternalSystem().name();
		}
		FDPStepResponse chargingStepResponse = (commandExecutionStatus != null) ? RequestUtil
				.createStepResponse(stepExecuted, commandExecutionStatus
						.getCode().toString(), commandExecutionStatus
						.getDescription(), commandExecutionStatus
						.getErrorType(), externalSys) : RequestUtil
				.createStepResponse(stepExecuted);
				//Code changes for allowing Async cache store of SP for Async Mobile Money Interface Start
		FDPStepResponseImpl fdpStepResponseImpl = (FDPStepResponseImpl) chargingStepResponse;
		fdpStepResponseImpl.addStepResponseValue(
				FDPStepResponseConstants.IS_CURRENT_COMMAND_ASYNC,
				isAsync);
		//Code changes for allowing Async cache store of SP for Async Mobile Money Interface End
		return chargingStepResponse;
	}
	
	
	/**
	 * This method is used to get the product charging steps from the request.
	 * 
	 * @param fdpRequest
	 *            The request object.
	 * @return The product charging steps.
	 */
	private List<ProductCharging> getProductChargingSteps(final FDPRequest fdpRequest) {
		List<ProductCharging> productChargingStep = null;
		final FDPCacheable product = fdpRequest.getValueFromRequest(RequestMetaValuesKey.PRODUCT);
		final Product fdpProduct = (Product) product;
		if(null == productChargingSteps) {
			if (product instanceof Product) {
				
				productChargingStep = fdpProduct.getProductCharging(fdpRequest.getChannel(), chargingType);
			}
		} else {
			productChargingStep = productChargingSteps;
		}
		//ehlnopu for other channel type 
		productChargingStep = productChargingStep == null ? fdpProduct.getProductCharging(ChannelType.OTHERS, chargingType) : productChargingStep;
		return productChargingStep;
	}

	@Override
	public boolean performRollback(final FDPRequest fdpRequest) throws RollbackException {
		boolean stepExecuted = true;
		circleLogger = LoggerUtil.getSummaryLoggerFromRequest(fdpRequest);
		FDPLogger.debug(circleLogger, getClass(), "performRollback()", LoggerUtil.getRequestAppender(fdpRequest)
				+ "Execting rollback for product charging step.");
		final List<ProductCharging> productChargingSteps = getProductChargingSteps(fdpRequest);
		for (final ProductCharging productChargingStep : productChargingSteps) {
			FDPLogger.debug(circleLogger, getClass(), "performRollback()", LoggerUtil.getRequestAppender(fdpRequest)
					+ "Executing product charging step :- " + productChargingStep);
			if (!productChargingStep.performRollback(fdpRequest)) {
				FDPLogger.error(circleLogger, getClass(), "performRollback()", LoggerUtil.getRequestAppender(fdpRequest)
						+ "Could not execute product charging step rollback :- " + productChargingStep);
				stepExecuted = false;
				break;
			}
		}
		return stepExecuted;
	}

	@Override
	public String toString() {
		return " product charging step impl.";
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

	/**
	 * @return the chargingType
	 */
	public ChargingType getChargingType() {
		return chargingType;
	}

	/**
	 * @param chargingType
	 *            the chargingType to set
	 */
	public void setChargingType(final ChargingType chargingType) {
		this.chargingType = chargingType;
	}

	@Override
	public boolean performStepRollback(FDPRequest fdpRequest) throws RollbackException {
		boolean stepExecuted = true;
		if (!this.stepExecuted) {
			circleLogger = LoggerUtil.getSummaryLoggerFromRequest(fdpRequest);
			FDPLogger.debug(circleLogger, getClass(), "performStepRollback()", LoggerUtil.getRequestAppender(fdpRequest)
					+ "Execting rollback for product charging step.");
			final List<ProductCharging> productChargingSteps = getProductChargingSteps(fdpRequest);
			for (int rollbackId = stepFailed - 1; rollbackId >= 0; rollbackId--) {
				ProductCharging productChargingStep = productChargingSteps.get(rollbackId);
				FDPLogger.debug(circleLogger, getClass(), "performStepRollback()", LoggerUtil.getRequestAppender(fdpRequest)
						+ "Executing product charging step :- " + productChargingStep);
				if (!productChargingStep.performRollback(fdpRequest)) {
					FDPLogger.error(circleLogger, getClass(), "performStepRollback()",
							LoggerUtil.getRequestAppender(fdpRequest) + "Could not execute product charging step rollback :- "
									+ productChargingStep);
					stepExecuted = false;
					break;
				}
			}
		}
		return stepExecuted;
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
