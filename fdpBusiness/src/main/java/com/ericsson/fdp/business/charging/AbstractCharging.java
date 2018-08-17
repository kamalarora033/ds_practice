package com.ericsson.fdp.business.charging;

import ch.qos.logback.classic.Logger;

import com.ericsson.fdp.FDPCacheable;
import com.ericsson.fdp.business.FDPRollbackable;
import com.ericsson.fdp.business.charging.impl.DABasedAirCharging;
import com.ericsson.fdp.business.charging.value.ChargingValue;
import com.ericsson.fdp.business.command.AbstractCommand;
import com.ericsson.fdp.business.command.param.impl.CommandParamInput;
import com.ericsson.fdp.business.enums.AuxRequestParam;
import com.ericsson.fdp.business.enums.ParamTransformationType;
import com.ericsson.fdp.business.enums.RequestMetaValuesKey;
import com.ericsson.fdp.business.exception.RollbackException;
import com.ericsson.fdp.business.mcoupons.CmsCoupon;
import com.ericsson.fdp.business.product.Product;
import com.ericsson.fdp.business.util.ChargingUtil;
import com.ericsson.fdp.business.util.ClassUtil;
import com.ericsson.fdp.business.util.CommandUtil;
import com.ericsson.fdp.business.util.LoggerUtil;
import com.ericsson.fdp.business.util.ProductUtil;
import com.ericsson.fdp.business.util.TransformationUtil;
import com.ericsson.fdp.business.vo.CommandExecutionStatus;
import com.ericsson.fdp.common.constants.FDPConstant;
import com.ericsson.fdp.common.constants.FDPLoggerConstants;
import com.ericsson.fdp.common.enums.ExternalSystem;
import com.ericsson.fdp.common.enums.ModuleType;
import com.ericsson.fdp.common.enums.Status;
import com.ericsson.fdp.core.command.FDPCommand;
import com.ericsson.fdp.core.command.param.CommandParam;
import com.ericsson.fdp.core.exception.EvaluationFailedException;
import com.ericsson.fdp.core.exception.ExecutionFailedException;
import com.ericsson.fdp.core.logging.FDPLogger;
import com.ericsson.fdp.core.request.FDPRequest;
import com.ericsson.fdp.core.request.ResponseError;
import com.ericsson.fdp.core.serviceprovisioning.bean.FDPMetaBag;
import com.ericsson.fdp.core.utils.ApplicationConfigUtil;

/*
Feature Name: DA Based Charging
Changes: Handling for DA Based Charging
Date: 18-12-2015
Singnum Id:ESIASAN
*/

/**
 * This class defines the command to be used for charging step. This class also
 * defines the execution flow for the charging step.
 * 
 * @author Ericsson
 * 
 */
public abstract class AbstractCharging implements ProductCharging {

	private static final long serialVersionUID = -5351477486888303541L;

	/**
	 * The command to be used for charging.
	 */
	private String commandDisplayName;
	
	/**
	 * The charging command to be used.
	 */
	protected FDPCommand fdpCommand;

	/**
	 * The subscriber number to charge.
	 */
	private CommandParamInput subscriberNumberToCharge;
	
	/**
	 * The discount applicable
	 */
	protected Discount chargingDiscount;

	/**
	 * The external system to be used for charging
	 */
	protected ExternalSystem externalSystem;
	
	/**
	 * The ID of discount that needs to be applied to charging
	 */
	protected Long discountId;
	
	public Long getDiscountId() {
		return discountId;
	}

	public void setDiscountId(Long discountId) {
		this.discountId = discountId;
	}

	/**
	 * @return the commandDisplayName
	 */
	public String getCommandDisplayName() {
		return commandDisplayName;
	}
	
	/**
	 * This method is used to set the command display name.
	 * 
	 * @param commandDisplayNameToSet
	 *            the command display name to set.
	 */
	public void setCommandDisplayName(final String commandDisplayNameToSet) {
		this.commandDisplayName = commandDisplayNameToSet;
	}
	
	/**
	 * @return the fdpCommand
	 */
	public FDPCommand getFdpCommand() {
		return fdpCommand;
	}

	/**
	 * @param fdpCommand
	 *            the fdpCommand to set
	 */
	public void setFdpCommand(final FDPCommand fdpCommand) {
		this.fdpCommand = fdpCommand;
	}

	/**
	 * @return the subscriberNumberToCharge
	 */
	public CommandParamInput getSubscriberNumberToCharge() {
		return subscriberNumberToCharge;
	}

	/**
	 * @param subscriberNumberToCharge
	 *            the subscriberNumberToCharge to set
	 */
	public void setSubscriberNumberToCharge(final CommandParamInput subscriberNumberToCharge) {
		this.subscriberNumberToCharge = subscriberNumberToCharge;
	}
	
	/**
	 * @param chargingDiscount the chargingDiscount to set
	 */
	public void setChargingDiscount(Discount chargingDiscount) {
		this.chargingDiscount = chargingDiscount;
	}

	@Override
	public ExternalSystem getExternalSystem() {
		return this.externalSystem;
	}

	@Override
	public abstract ChargingValue evaluateParameterValue(FDPRequest fdpRequest) throws ExecutionFailedException;
	
	
	@Override
	public CommandExecutionStatus execute(final FDPRequest fdpRequest, final Object... otherParams)
			throws ExecutionFailedException {
		// TODO: Use the value of the instance and then set it in fdp
		// command before execution.
		final Logger circleLogger = LoggerUtil.getSummaryLoggerFromRequest(fdpRequest);
		boolean executed = false;
		CommandExecutionStatus commandExecutionStatus = new CommandExecutionStatus(Status.SUCCESS, 0, null, null, null);
		fdpCommand = updateCommand(fdpRequest);				
		if (fdpCommand != null) {
			fdpCommand = (!DABasedAirCharging.class.isInstance(this) && externalSystem.equals(ExternalSystem.AIR)) ? getSpecificChargingCommand(
					fdpCommand, true) : fdpCommand;			
			executed = executeCharging(fdpRequest,fdpCommand);
		}
		if (!executed) {
			if (fdpCommand == null) {
				commandExecutionStatus = new CommandExecutionStatus(Status.FAILURE, 1, "Command not found", "NA", null);
				FDPLogger.info(circleLogger, getClass(), "execute()", LoggerUtil.getRequestAppender(fdpRequest) + "RSN"
						+ FDPConstant.LOGGER_KEY_VALUE_DELIMITER + "Command not found " + commandDisplayName);
			} else {
				if ((fdpCommand instanceof AbstractCommand)
						&& (((AbstractCommand) fdpCommand).getCommandResponse() != null)) {
					commandExecutionStatus = CommandUtil.checkForCommandStatus(fdpCommand);
					FDPLogger.info(circleLogger, getClass(), "execute()", LoggerUtil.getRequestAppender(fdpRequest)
							+ "RSN" + FDPConstant.LOGGER_KEY_VALUE_DELIMITER + commandExecutionStatus);
				} else if (fdpCommand.getResponseError() != null) {
					ResponseError responseError = fdpCommand.getResponseError();
					ExternalSystem externalSystem = ExternalSystem.valueOf(responseError.getSystemType());
					Integer responseCode = Integer.parseInt(responseError.getResponseCode());
					commandExecutionStatus = new CommandExecutionStatus(Status.FAILURE, responseCode,
							responseError.getResponseErrorString(), responseError.getErrorType(), externalSystem);
					FDPLogger.info(circleLogger, getClass(), "execute()", LoggerUtil.getRequestAppender(fdpRequest)
							+ "RSN" + FDPConstant.LOGGER_KEY_VALUE_DELIMITER
							+ fdpCommand.getResponseError().getResponseErrorString());
				} else {
					commandExecutionStatus = new CommandExecutionStatus(Status.FAILURE, 1, null, null, null);
					FDPLogger.info(circleLogger, getClass(), "execute()", LoggerUtil.getRequestAppender(fdpRequest)
							+ "RSN" + FDPConstant.LOGGER_KEY_VALUE_DELIMITER + "External System is down");
				}
			}
		}
		return commandExecutionStatus;
	}
	
	/**
	 * This method is used to update the command.
	 * 
	 * @param fdpRequest
	 *            the request object.
	 * @throws ExecutionFailedException
	 *             Exception in execution.
	 */
	protected FDPCommand updateCommand(final FDPRequest fdpRequest) throws ExecutionFailedException {
		final FDPCacheable fdpCommandCached = ApplicationConfigUtil.getMetaDataCache().getValue(
				new FDPMetaBag(fdpRequest.getCircle(), ModuleType.COMMAND, commandDisplayName));
		FDPCommand fdpCommand = null;
		if (fdpCommandCached instanceof FDPCommand) {
			fdpCommand = (FDPCommand) fdpCommandCached;
			fdpCommand = CommandUtil.getExectuableFDPCommand(fdpCommand);
		}
		return fdpCommand;
	}

	/**
	 * This method is used to execute charging.
	 * 
	 * @param fdpRequest
	 *            the request object.
	 * @return true if charging done.
	 * @throws ExecutionFailedException
	 *             Exception if execution fails.
	 */
	protected boolean executeCharging(final FDPRequest fdpRequest, final FDPCommand fdpCommand) throws ExecutionFailedException {
		boolean executed = false;
		final ChargingValue chargingValue = evaluateParameterValue(fdpRequest);
		final Logger circleLogger = LoggerUtil.getSummaryLoggerFromRequest(fdpRequest);
		if (!chargingValue.isChargingRequired()) {
			Product fdpProduct = null;
			final FDPCacheable product = fdpRequest.getValueFromRequest(RequestMetaValuesKey.PRODUCT);
			if (product instanceof Product) {
				fdpProduct = (Product) product;
			}
			final String productName = fdpProduct == null ? "Product undefined" : fdpProduct.getProductName();
			FDPLogger.debug(circleLogger, getClass(), "executeCharging()", LoggerUtil.getRequestAppender(fdpRequest)
					+ "Skipping charging step, as zero charging is defined for the product - " + productName
					+ " MSISDN - " + fdpRequest.getSubscriberNumber() + " Channel Name - "
					+ fdpRequest.getChannel().getName());
			FDPLogger.info(circleLogger, getClass(), "executeCharging()", LoggerUtil.getRequestAppender(fdpRequest)+ "AMTCHG" + FDPConstant.LOGGER_KEY_VALUE_DELIMITER + "0"+
					FDPConstant.LOGGER_DELIMITER+FDPLoggerConstants.CHARGING_NODE_NAME+FDPConstant.LOGGER_KEY_VALUE_DELIMITER+FDPLoggerConstants.NOT_APPLICABLE
					+ FDPConstant.LOGGER_DELIMITER + FDPLoggerConstants.CORRELATION_ID + FDPConstant.LOGGER_KEY_VALUE_DELIMITER + fdpRequest.getOriginTransactionID().toString());
			executed = true;
		} else {
			updateSubscriberNumberAndChargingValueToCharge(fdpRequest, chargingValue);
			Object chargingAmtObj = null;
			if(chargingValue.getChargingAmount()!= null){
				chargingAmtObj = chargingValue.getChargingAmount();
			}
			else{
				chargingAmtObj = chargingValue.getChargingValue();
			}
			FDPLogger.info(circleLogger, getClass(), "executeCharging()", LoggerUtil.getRequestAppender(fdpRequest)+ "AMTCHG" + FDPConstant.LOGGER_KEY_VALUE_DELIMITER +chargingAmtObj +
					FDPConstant.LOGGER_DELIMITER+FDPLoggerConstants.CHARGING_NODE_NAME + FDPConstant.LOGGER_KEY_VALUE_DELIMITER + fdpRequest.getExternalSystemToCharge()
					+ FDPConstant.LOGGER_DELIMITER + FDPLoggerConstants.CORRELATION_ID + FDPConstant.LOGGER_KEY_VALUE_DELIMITER + fdpRequest.getOriginTransactionID().toString());
			final Status status = fdpCommand.execute(fdpRequest);
			FDPLogger.info(circleLogger, getClass(), "executeCharging()", LoggerUtil.getRequestAppender(fdpRequest)
					+ "CHRGRSLT" + FDPConstant.LOGGER_KEY_VALUE_DELIMITER + status.name() + FDPConstant.LOGGER_DELIMITER + "AMTCHGS"
					+ FDPConstant.LOGGER_KEY_VALUE_DELIMITER + chargingAmtObj
					+ FDPConstant.LOGGER_DELIMITER + FDPLoggerConstants.CORRELATION_ID + FDPConstant.LOGGER_KEY_VALUE_DELIMITER + fdpRequest.getOriginTransactionID().toString());
			/*
			 * for printing logs related to applied coupon 
			 */
			
			CmsCoupon oCmsCoupon=(CmsCoupon)fdpRequest.getAuxiliaryRequestParameter(AuxRequestParam.CONSUMER_APPLIED_COUPON);
			if(oCmsCoupon!=null)
			{
				FDPLogger.info(circleLogger, getClass(), " after executeCharging()", LoggerUtil.getRequestAppender(fdpRequest)
						+ "APPCPN" + FDPConstant.LOGGER_KEY_VALUE_DELIMITER + oCmsCoupon.getCouponCode());
				
				FDPLogger.info(circleLogger, getClass(), " after executeCharging()", LoggerUtil.getRequestAppender(fdpRequest)
						+ "CPNTYPE" + FDPConstant.LOGGER_KEY_VALUE_DELIMITER + oCmsCoupon.getCouponSubtypeName().name());
				
				FDPLogger.info(circleLogger, getClass(), "after executeCharging()", LoggerUtil.getRequestAppender(fdpRequest)
						+ "CPNVAL" + FDPConstant.LOGGER_KEY_VALUE_DELIMITER + oCmsCoupon.getCouponValue());
			
			}
			
			if(fdpRequest.getAuxiliaryRequestParameter(AuxRequestParam.MC_LOAN_PURCHASE_INPUT) != null){
				FDPLogger.info(circleLogger, getClass(), "after executeCharging()", LoggerUtil.getRequestAppender(fdpRequest)
						+ "LOANAMT" + FDPConstant.LOGGER_KEY_VALUE_DELIMITER + fdpRequest.getAuxiliaryRequestParameter(AuxRequestParam.MC_LOAN_PURCHASE_INPUT));
			}
			executed = status.equals(Status.SUCCESS);
		}
		return executed;
	}

	/**
	 * This method is used to update the charging number.
	 * 
	 * @param fdpRequest
	 *            the request.
	 * @param chargingValue
	 *            the charging value.
	 * @throws ExecutionFailedException
	 *             exception if execution fails.
	 */
	protected void updateSubscriberNumberAndChargingValueToCharge(final FDPRequest fdpRequest,
			final ChargingValue chargingValue) throws ExecutionFailedException {
		if (subscriberNumberToCharge != null) {
			try {
				subscriberNumberToCharge.evaluateValue(fdpRequest);
				final Object subsToCharge = ClassUtil.getPrimitiveValueReturnNotNullObject(
						subscriberNumberToCharge.getValue(), Long.class);
				if (subsToCharge instanceof Long) {
					ProductUtil.updateValuesInRequest(fdpRequest, chargingValue, (Long) subsToCharge);
				} else {
					throw new ExecutionFailedException("Could not find subscriber to charge");
				}
			} catch (final EvaluationFailedException e) {
				throw new ExecutionFailedException("Could not find parameter value.", e);
			}
		} else {
			ProductUtil.updateValuesInRequest(fdpRequest, chargingValue, fdpRequest.getSubscriberNumber());
		}

	}

	@Override
	public boolean performRollback(final FDPRequest fdpRequest) throws RollbackException {
		boolean rollbackDone = true;
		try {
			if (fdpCommand == null) {
				fdpCommand =updateCommand(fdpRequest);
			}
			final ChargingValue chargingValue = evaluateParameterValue(fdpRequest);
			if (fdpCommand instanceof FDPRollbackable) {
				if (chargingValue.isChargingRequired()) {
					final FDPRollbackable rollbackCommand = (FDPRollbackable) fdpCommand;
					rollbackDone = rollbackCommand.performRollback(fdpRequest);
					Status status = rollbackDone?Status.SUCCESS:Status.FAILURE;
					Object chargingAmtObj = null;
					if(chargingValue.getChargingAmount()!= null){
						chargingAmtObj = chargingValue.getChargingAmount();
					}
					else{
						chargingAmtObj = chargingValue.getChargingValue();
					}
					chargingAmtObj = -1 * Float.valueOf(chargingAmtObj.toString());
					
					Logger circleLogger = LoggerUtil.getSummaryLoggerFromRequest(fdpRequest);
					FDPLogger.info(circleLogger, getClass(), "performRollback()", LoggerUtil.getRequestAppender(fdpRequest)
							+ "CHRGRSLTRB" + FDPConstant.LOGGER_KEY_VALUE_DELIMITER + status.name() + FDPConstant.LOGGER_DELIMITER + "AMTCHGSRB"
							+ FDPConstant.LOGGER_KEY_VALUE_DELIMITER + chargingAmtObj);
				}
			} else {
				throw new RollbackException("Could not find command to rollback" + commandDisplayName);
			}
		} catch (final ExecutionFailedException e) {
			throw new RollbackException("Could not find command to update", e);
		}
		return rollbackDone;
	}
	
	/**
	 * This method gets the Charging system command for charging (main or da based).
	 * @param fdpCommand
	 * @param isMainCharging
	 * @return
	 */
	protected FDPCommand getSpecificChargingCommand(final FDPCommand fdpCommand, final boolean isMainCharging) {
        final String parameterToRemove = isMainCharging ? "dedicatedAccountUpdateInformation"
                : "adjustmentAmountRelative";
        final CommandParam commandParam = fdpCommand.getInputParam(parameterToRemove);
        fdpCommand.getInputParam().remove(commandParam);
        return fdpCommand;
    }
	
	@Override
	public String toString() {
		return " charging command is :- " + commandDisplayName;
	}
	
	/**
	 * Method gets the discount from cache.
	 * 
	 * @param fdpRequest
	 * @param productId
	 * @return
	 * @throws ExecutionFailedException
	 */
	protected Discount getDiscountFromCache(final FDPRequest fdpRequest) throws ExecutionFailedException {
		final Product product = (Product) fdpRequest.getValueFromRequest(RequestMetaValuesKey.PRODUCT);
		Discount discount = null;
		if(null != product) {
			final Long productId = product.getProductId();
			final FDPCacheable fdpDiscountCached = ApplicationConfigUtil.getMetaDataCache().getValue(
					new FDPMetaBag(fdpRequest.getCircle(), ModuleType.PRODUCT_CHARGING_DISCOUNT, productId));
			if (fdpDiscountCached instanceof Discount) {
				discount = (Discount) fdpDiscountCached;
			}
		}
		return discount;
	}

	public void setExternalSystem(ExternalSystem externalSystem) {
		this.externalSystem = externalSystem;
	}
	
}