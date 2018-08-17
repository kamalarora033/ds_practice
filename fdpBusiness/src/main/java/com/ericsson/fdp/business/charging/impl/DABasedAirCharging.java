package com.ericsson.fdp.business.charging.impl;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import ch.qos.logback.classic.Logger;

import com.ericsson.fdp.FDPCacheable;
import com.ericsson.fdp.business.charging.Account;
import com.ericsson.fdp.business.charging.FDPChargingSystem;
import com.ericsson.fdp.business.charging.value.ChargingValue;
import com.ericsson.fdp.business.charging.value.ChargingValueImpl;
import com.ericsson.fdp.business.command.param.impl.CommandParamInput;
import com.ericsson.fdp.business.enums.AuxRequestParam;
import com.ericsson.fdp.business.enums.ChargingCalculatorKey;
import com.ericsson.fdp.business.enums.Command;
import com.ericsson.fdp.business.enums.RequestMetaValuesKey;
import com.ericsson.fdp.business.product.Product;
import com.ericsson.fdp.business.util.ChargingUtil;
import com.ericsson.fdp.business.util.FulfillmentUtil;
import com.ericsson.fdp.business.util.LoggerUtil;
import com.ericsson.fdp.business.vo.ErrorCodesNotificationVO;
import com.ericsson.fdp.common.constants.FDPConstant;
import com.ericsson.fdp.common.constants.FDPLoggerConstants;
import com.ericsson.fdp.common.enums.ExternalSystem;
import com.ericsson.fdp.common.enums.ModuleType;
import com.ericsson.fdp.common.enums.ParameterFeedType;
import com.ericsson.fdp.common.enums.Status;
import com.ericsson.fdp.core.command.FDPCommand;
import com.ericsson.fdp.core.command.param.CommandParam;
import com.ericsson.fdp.core.exception.ExecutionFailedException;
import com.ericsson.fdp.core.logging.FDPLogger;
import com.ericsson.fdp.core.request.FDPRequest;
import com.ericsson.fdp.core.request.impl.FDPRequestImpl;
import com.ericsson.fdp.core.serviceprovisioning.bean.FDPMetaBag;
import com.ericsson.fdp.core.utils.ApplicationConfigUtil;

/*
Feature Name: DA Based Charging
Changes: DABasedAirCharging class created which will handle DA Based Air charging
Date: 18-12-2015
Singnum Id:ESIASAN
*/

/**
 * This class will handle the Main + DA Based Charging.
 * 
 * @author ESIASAN
 *
 */
public class DABasedAirCharging extends FixedCharging {
	
	private static final long serialVersionUID = 1L;

	private List<Account> chargingDetails;
	
	private boolean isPartialChargingAllowed = false;
	
	private Map<ChargingCalculatorKey,Object> chargingMap = null;
	
	public DABasedAirCharging(
			FDPChargingSystem<? extends Object> chargingValue,
			String commandDisplayName,
			CommandParamInput subscriberNumberToCharge, final List<Account> chargingDetails, final boolean isPartialChargingAllowed , final ExternalSystem externalSystem) {
		super(chargingValue, commandDisplayName, subscriberNumberToCharge, externalSystem);
		this.chargingDetails = chargingDetails;
		this.isPartialChargingAllowed = isPartialChargingAllowed;
	}
	
	public DABasedAirCharging(
			FDPChargingSystem<? extends Object> chargingValue,
			String commandDisplayName,
			CommandParamInput subscriberNumberToCharge, final List<Account> chargingDetails, final boolean isPartialChargingAllowed , final ExternalSystem externalSystem, final Long discountId) {
		this(chargingValue, commandDisplayName, subscriberNumberToCharge, chargingDetails, isPartialChargingAllowed, externalSystem);
		this.discountId = discountId;
	}

	/**
	 * @return the daChargingDetails
	 */
	public List<Account> getChargingDetails() {
		return chargingDetails;
	}

	/**
	 * @param daChargingDetails the daChargingDetails to set
	 */
	public void setDaChargingDetails(List<Account> chargingDetails) {
		this.chargingDetails = chargingDetails;
	}

	/**
	 * @return the isPartialChargingAllowed
	 */
	public boolean isPartialChargingAllowed() {
		return isPartialChargingAllowed;
	}

	/**
	 * @param isPartialChargingAllowed the isPartialChargingAllowed to set
	 */
	public void setPartialChargingAllowed(boolean isPartialChargingAllowed) {
		this.isPartialChargingAllowed = isPartialChargingAllowed;
	}
	
	@Override
	public boolean executeCharging(final FDPRequest fdpRequest, final FDPCommand fdpCommand) throws ExecutionFailedException {
		final Logger circleLogger = LoggerUtil.getSummaryLoggerFromRequest(fdpRequest);
		boolean executed = false;
			final ChargingValue chargingValue = evaluateParameterValue(fdpRequest);
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
				executed = calculateCharging(fdpRequest, chargingValue) ?executeMainDACharging(fdpRequest, fdpCommand, circleLogger, chargingValue) : false;
			}
		return executed;
	}
	
	/**
	 * This method updates the charging command as per the deduction calculated
	 * @param fdpCommand
	 * @param chargingValue
	 * @param chargingMap
	 * @throws ExecutionFailedException
	 */
	private void updateCommandForMainAndDA(FDPCommand fdpCommand,
			ChargingValue chargingValue, Map<ChargingCalculatorKey, Object> chargingMap) throws ExecutionFailedException{

		if(chargingMap.containsKey(ChargingCalculatorKey.MAIN_ACCOUNT_DEDUCTION_DETAIL)){
			((ChargingValueImpl)chargingValue).setChargingValue((Long)chargingMap.get(ChargingCalculatorKey.MAIN_ACCOUNT_DEDUCTION_DETAIL));
		}else{
			fdpCommand = getSpecificChargingCommand(fdpCommand, false);
		}
		
		if(null != chargingMap.get(ChargingCalculatorKey.DA_DEDUCTION_DETAILS) && chargingMap.containsKey(ChargingCalculatorKey.DA_DEDUCTION_DETAILS)){
			final CommandParam commandParam = fdpCommand.getInputParam("dedicatedAccountUpdateInformation");
			if(commandParam != null && commandParam instanceof CommandParamInput && commandParam.getChilderen() != null && null != commandParam.getChilderen().get(0)) {
				CommandParamInput daUpdateCpi = (CommandParamInput)commandParam;
				final List<CommandParam> daParamInputs = updatedDAChildNodes(daUpdateCpi.getChilderen(), chargingMap);
				if(null != daParamInputs && daParamInputs.size()>0) {
					daUpdateCpi.getChilderen().clear();
					daUpdateCpi.getChilderen().addAll(daParamInputs);
				}
			}else{
				throw new ExecutionFailedException("Command : " + fdpCommand.getCommandDisplayName() + " not configured for DA.");
			}
		}else{
			fdpCommand = getSpecificChargingCommand(fdpCommand, true);
		}
	}

	/**
	 * Creates copy of Param Input and prepare parameter for DA based charging.
	 * 
	 * @param commandParamInput
	 * @param dedicatedAccountId
	 * @param value
	 * @return
	 * @throws ExecutionFailedException
	 */
	private CommandParam createCommandParamForDACharging(final CommandParamInput commandParamInput, final String dedicatedAccountId, final String value) throws ExecutionFailedException {
		//final CommandParamInput paramInput = commandParamInput.clone(); // Working incorrectly
		final CommandParamInput paramInput = (CommandParamInput) this.cloneObject(commandParamInput);
		for(CommandParam childParam : paramInput.getChilderen()){
			if(ChargingUtil.DEDICATED_ACCOUNT_ID.equals(childParam.getName())){
				FulfillmentUtil.updateValueInInputCmdObject(CommandParamInput.class, "commandParameterSource",
						childParam, ParameterFeedType.INPUT);
				FulfillmentUtil.updateValueInInputCmdObject(CommandParamInput.class, FDPConstant.DEFINED_VALUE_TEXT,
						childParam, dedicatedAccountId);
			}
			if(ChargingUtil.ADJUSTMENT_AMOUNT_VALUE.equals(childParam.getName())){
				FulfillmentUtil.updateValueInInputCmdObject(CommandParamInput.class, "commandParameterSource",
						childParam, ParameterFeedType.INPUT);
				FulfillmentUtil.updateValueInInputCmdObject(CommandParamInput.class, FDPConstant.DEFINED_VALUE_TEXT,
						childParam, value);
			}
		}
		return paramInput;
	}
	
	/**
	 * This method creates a clone of an object
	 * @param commandParamInput
	 * @return
	 * @throws ExecutionFailedException
	 */
	private Object cloneObject(final Object objectToClone) throws ExecutionFailedException{
		Object clonedObject = null;	
		try{
			// Make copy of command param
			ByteArrayOutputStream baos = new ByteArrayOutputStream();
			new ObjectOutputStream(baos).writeObject(objectToClone);
			ByteArrayInputStream bais = new ByteArrayInputStream(baos.toByteArray());
			clonedObject = new ObjectInputStream(bais).readObject();
		}catch(Exception e){
			throw new ExecutionFailedException("");
		}
		return clonedObject;
	}
	
	/**
	 * Update Command for DA Charging.
	 * 
	 * @param daChildNodes
	 * @param chargingMap
	 * @return
	 * @throws ExecutionFailedException
	 */
	@SuppressWarnings("unchecked")
	private List<CommandParam> updatedDAChildNodes(List<CommandParam> daChildNodes, Map<ChargingCalculatorKey, Object> chargingMap) throws ExecutionFailedException{
		List<DedicatedAccount> accounts = (List<DedicatedAccount>) chargingMap.get(ChargingCalculatorKey.DA_DEDUCTION_DETAILS);
		CommandParamInput daChildNode = (CommandParamInput)daChildNodes.get(0);
		List<CommandParam> daParamInputs = new ArrayList<CommandParam>();
		for(final DedicatedAccount account : accounts) {
			CommandParam commandParam = createCommandParamForDACharging(daChildNode, account.getDedicatedAccountId(), account.getAccountValue().toString());
			daParamInputs.add(commandParam);
		}
		return daParamInputs;
	}

	/**
	 * This method executes the charging for Main and DA
	 * @param fdpRequest
	 * @param fdpCommand
	 * @param circleLogger
	 * @param chargingValue
	 * @return
	 * @throws ExecutionFailedException
	 */
	private boolean executeMainDACharging(final FDPRequest fdpRequest,
			final FDPCommand fdpCommand, final Logger circleLogger,
			final ChargingValue chargingValue) throws ExecutionFailedException {
		boolean executed = false;
		updateSubscriberNumberAndChargingValueToCharge(fdpRequest, chargingValue);
		//Object chargingAmtObj = chargingValue.getChargingAmount()!= null ? chargingValue.getChargingAmount() : chargingValue.getChargingValue();
		Object chargingAmtObj = getChargingValuesAsString(fdpRequest);
		FDPLogger.info(circleLogger, getClass(), "executeMainDACharging()", LoggerUtil.getRequestAppender(fdpRequest)+ "AMTCHG" + FDPConstant.LOGGER_KEY_VALUE_DELIMITER +chargingAmtObj);
		final Status status = fdpCommand.execute(fdpRequest);
		FDPLogger.info(circleLogger, getClass(), "executeMainDACharging()", LoggerUtil.getRequestAppender(fdpRequest)
				+ "CHRGRSLT" + FDPConstant.LOGGER_KEY_VALUE_DELIMITER + status.name() + FDPConstant.LOGGER_DELIMITER + "AMTCHGS"
				+ FDPConstant.LOGGER_KEY_VALUE_DELIMITER + chargingAmtObj
				+ FDPConstant.LOGGER_DELIMITER + FDPLoggerConstants.CORRELATION_ID + FDPConstant.LOGGER_KEY_VALUE_DELIMITER + fdpRequest.getOriginTransactionID().toString());
		if(status.equals(Status.SUCCESS)){
			((ChargingValueImpl)chargingValue).setChargingValue((Long)chargingMap.get(ChargingCalculatorKey.PRODUCT_AMOUNT_TO_CHARGE));
			executed = true;
		}
		return executed;
	}
	
	/**
	 * This method return deduction details as string
	 * @param fdpRequest
	 * @return
	 */
	private Object getChargingValuesAsString(FDPRequest fdpRequest) {
		StringBuffer buffer = new StringBuffer();
		if(chargingMap.containsKey(ChargingCalculatorKey.MAIN_ACCOUNT_DEDUCTION_DETAIL)){
			buffer.append("MAIN" + FDPConstant.LOGGER_KEY_VALUE_DELIMITER + chargingMap.get(ChargingCalculatorKey.MAIN_ACCOUNT_DEDUCTION_DETAIL).toString() + FDPConstant.LOGGER_DELIMITER);
		}
		if(chargingMap.containsKey(ChargingCalculatorKey.DA_DEDUCTION_DETAILS)){
			@SuppressWarnings("unchecked")
			List<DedicatedAccount> accounts = (List<DedicatedAccount>) chargingMap.get(ChargingCalculatorKey.DA_DEDUCTION_DETAILS);
			for(final DedicatedAccount account : accounts) {
				buffer.append("DAID" + FDPConstant.LOGGER_KEY_VALUE_DELIMITER + account.getDedicatedAccountId()
						+ FDPConstant.LOGGER_METHOD_PARAMETER_SEPA + "AMTCHG"
						+ FDPConstant.LOGGER_KEY_VALUE_DELIMITER + account.getAccountValue() + FDPConstant.LOGGER_DELIMITER);
			}
		}
		return buffer.toString();
	}
	
	/**
	 * This method calculates the charging as per product definition
	 * @param fdpRequest
	 * @param productAmount
	 * @return
	 * @throws ExecutionFailedException
	 */
	/*private Map<ChargingCalculatorKey, Object> calculateCharging(final FDPRequest fdpRequest, final Long productAmount) throws ExecutionFailedException {
		Map<ChargingCalculatorKey,Object> chargingCalulatorMap = new HashMap<ChargingCalculatorKey,Object>();
		chargingCalulatorMap.put(ChargingCalculatorKey.PRODUCT_AMOUNT_TO_CHARGE, (-1 * productAmount));
		chargingCalulatorMap.put(ChargingCalculatorKey.REMAINING_AMOUNT_TO_CHARGE, (-1 * productAmount));
		chargingCalulatorMap = new ChargingCalculatorImpl().calculateCharging(fdpRequest, isPartialChargingAllowed, chargingCalulatorMap, chargingDetails);
		Long remAmtToCharge = (Long) chargingCalulatorMap.get(ChargingCalculatorKey.REMAINING_AMOUNT_TO_CHARGE);
		if(remAmtToCharge > 0L){
			throw new ExecutionFailedException(isPartialChargingAllowed ? "Insufficient Balance for Partial Charging": "Insufficient Balance for Non Partial Charging");
		}
		return chargingCalulatorMap;
	}*/
	
	/**
	 * This method calculates the charging as per product definition
	 * @param fdpRequest
	 * @param productAmount
	 * @return
	 * @throws ExecutionFailedException
	 */
	private boolean calculateCharging(final FDPRequest fdpRequest,  final ChargingValue chargingValue) throws ExecutionFailedException {
		boolean flag = false;
		final Long productAmount = Long.valueOf(chargingValue.getChargingValue().toString());
		chargingMap = new HashMap<ChargingCalculatorKey,Object>();
		chargingMap.put(ChargingCalculatorKey.PRODUCT_AMOUNT_TO_CHARGE, (-1 * productAmount));
		chargingMap.put(ChargingCalculatorKey.REMAINING_AMOUNT_TO_CHARGE, (-1 * productAmount));
		chargingMap = new ChargingCalculatorImpl().calculateCharging(fdpRequest, isPartialChargingAllowed, chargingMap, chargingDetails);
		Long remAmtToCharge = (Long) chargingMap.get(ChargingCalculatorKey.REMAINING_AMOUNT_TO_CHARGE);
		if(remAmtToCharge > 0L){
			updateBelowMinBalanceNotification(fdpRequest);
		} else {
			flag = true;
			updateCommandForMainAndDA(fdpCommand, chargingValue, chargingMap);
		}
		return flag;
	}

	/**
     * Method to set Below min balance notification.
     * 
      * @param fdpRequest
     * @throws ExecutionFailedException 
      */
     private void updateBelowMinBalanceNotification(FDPRequest fdpRequest) throws ExecutionFailedException {
            if(!updateBelowMinBalanceNotification(fdpRequest,Command.UPDATE_BALACEANDATE_MAIN)) {
                   updateBelowMinBalanceNotification(fdpRequest,Command.REFUND);
            }
     }
     
     /**
     * Method to update the Notification from command.
     * 
      * @param fdpRequest
     * @param command
     * @return
     * @throws ExecutionFailedException
     */
     private boolean updateBelowMinBalanceNotification(FDPRequest fdpRequest, final Command command) throws ExecutionFailedException {
            boolean isFound = false;
            String key = command.getCommandDisplayName()
                         + FDPConstant.PARAMETER_SEPARATOR
                         + "124";
            FDPMetaBag metaBag = new FDPMetaBag(fdpRequest.getCircle(),
                         ModuleType.RESPONSE_CODE_NOTIFICATION_MAPPING, key);
            final FDPCacheable fdpCommandCached = ApplicationConfigUtil.getMetaDataCache().getValue(metaBag);
            if(fdpCommandCached instanceof ErrorCodesNotificationVO) {
                   final ErrorCodesNotificationVO errorCodeNotificationVO = (ErrorCodesNotificationVO) fdpCommandCached;
                   ((FDPRequestImpl)fdpRequest).putAuxiliaryRequestParameter(AuxRequestParam.NOTIFICATION_TO_OVERRIDE, errorCodeNotificationVO.getNotificationId());
                   isFound = true;
            }
            return isFound;
     }

}
