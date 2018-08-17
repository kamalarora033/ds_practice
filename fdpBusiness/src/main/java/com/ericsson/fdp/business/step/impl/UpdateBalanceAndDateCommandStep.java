package com.ericsson.fdp.business.step.impl;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.LinkedHashMap;
import java.util.Map;

import ch.qos.logback.classic.Logger;

import com.ericsson.fdp.FDPCacheable;
import com.ericsson.fdp.business.command.param.impl.CommandParamInput;
import com.ericsson.fdp.business.enums.AuxRequestParam;
import com.ericsson.fdp.business.enums.Command;
import com.ericsson.fdp.business.enums.ConfigurationKey;
import com.ericsson.fdp.business.util.ChargingUtil;
import com.ericsson.fdp.business.util.CommandUtil;
import com.ericsson.fdp.business.util.FulfillmentUtil;
import com.ericsson.fdp.business.util.LoggerUtil;
import com.ericsson.fdp.business.util.MVELUtil;
import com.ericsson.fdp.business.util.Me2uUtil;
import com.ericsson.fdp.business.util.ServiceProvisioningUtil;
import com.ericsson.fdp.common.constants.FDPConstant;
import com.ericsson.fdp.common.enums.FDPCSAttributeParam;
import com.ericsson.fdp.common.enums.FDPCSAttributeValue;
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

/**
 * This class is written specifically to handle time2share use case
 * 
 * @author esiasan
 *
 */
public class UpdateBalanceAndDateCommandStep extends CommandStep{

	private static final long serialVersionUID = 6387695260953652498L;

	public UpdateBalanceAndDateCommandStep(final FDPCommand fdpCommand, final String commandDisplayNameToSet, final Long stepId,
			final String stepName) {
		super(fdpCommand, commandDisplayNameToSet, stepId, stepName);
	}
	
	/**
	 * This method will modify the UpdateBalanceAndDate(Main) command
	 * on certain conditions 
	 *
	 * @param fdpRequest
	 *            the request.
	 * @throws ExecutionFailedException
	 *             Exception if any.
	 */
	protected void updateCommand(final FDPRequest fdpRequest) throws ExecutionFailedException {
		super.updateCommand(fdpRequest);
		updateCommandForTime2Share(fdpRequest);
	}

	/**
	 * This method will update the UpdateBalanceAndDate command by removing the da parameters 
	 * in case they are in use during transaction
	 * 
	 * @param fdpRequest
	 * @throws ExecutionFailedException 
	 */
	private void updateCommandForTime2Share(FDPRequest fdpRequest) throws ExecutionFailedException {
		Object isTime2shareObj = fdpRequest.getAuxiliaryRequestParameter(AuxRequestParam.IS_TIME2SHARE);
		if(null != isTime2shareObj){
			if(true == Boolean.parseBoolean(isTime2shareObj.toString())){
				Integer accToShareFrom = Integer.parseInt(fdpRequest.getAuxiliaryRequestParameter(AuxRequestParam.TIME2SHARE_ACCOUNT_TO_SHARE_FROM).toString());
				Integer accToShareTo = Integer.parseInt(fdpRequest.getAuxiliaryRequestParameter(AuxRequestParam.TIME2SHARE_ACCOUNT_TO_SHARE_TO).toString());
				if(((FDPConstant.ZERO == accToShareFrom) && isCommandForSender(fdpRequest))){
					 CommandUtil.removeCommandParam(fdpCommand, fdpCommand.getInputParam("dedicatedAccountUpdateInformation"));
				}else if(isCommandForRecipient(fdpRequest)){
					boolean daSettlementFlag =  doRecipientDaSettlement(fdpRequest, fdpCommand, FDPConstant.ZERO == accToShareTo);
					if(!daSettlementFlag && FDPConstant.ZERO == accToShareTo){
						 CommandUtil.removeCommandParam(fdpCommand, fdpCommand.getInputParam("dedicatedAccountUpdateInformation"));
					}
				}
			}
		}
	}
	
	/**
	 * This method will return true in case the command is for sender
	 * @param fdpRequest
	 * @return
	 */
	private boolean isCommandForSender(FDPRequest fdpRequest){		
		return isSubscriberParamValue(FDPConstant.subscriberNumber);
	}

	/**
	 * This method will return true in case the command is for sender
	 * @param fdpRequest
	 * @return
	 */
	private boolean isCommandForRecipient(FDPRequest fdpRequest){
		return isSubscriberParamValue(AuxRequestParam.ME2U_SUBSCRIBER_TO_CREDIT.getName());
	}
	
	/**
	 * This method will check whether subscriberNumber param of the command contains
	 * the specified input value
	 *  
	 * @param subscribernumber
	 * @return
	 */
	private boolean isSubscriberParamValue(String value) {
		CommandParam subscriberNumberParam = fdpCommand.getInputParam(FDPConstant.subscriberNumber);
		if(subscriberNumberParam instanceof CommandParamInput){
			CommandParamInput subscriberNumberParamInput = (CommandParamInput) subscriberNumberParam;
			if(null != subscriberNumberParamInput.getDefinedValue()){
				if(subscriberNumberParamInput.getDefinedValue().toString().equals(value)){
					return true;
				}
			}
		}
		return false;
	}
	
	/**
	 * This method will update the "UpdateBalanceAndDate(Main)" command for beneficiary in case of Time2share where
	 * recipient will recieve the amount only after his/her settlement Da are taken care of
	 * 
	 * @param fdpCommand
	 * @throws ExecutionFailedException 
	 */
	private boolean doRecipientDaSettlement(FDPRequest fdpRequest, FDPCommand fdpCommand, boolean isTransferToMainAccount) throws ExecutionFailedException {
		boolean daSettlementDone = false;
		// settlementDAs map will contain only those settlement DAs that have some value other that 0 associated with it
		Map<String, Long> settlementDAs = getSettlementDaValuesMap(fdpRequest);
		if(!settlementDAs.isEmpty()){
			Object amtToRecieveObj = fdpRequest.getAuxiliaryRequestParameter(AuxRequestParam.ME2U_AMT_TO_BE_RECIEVED);
			Long amtToRecieveInNgwee = Long.parseLong(amtToRecieveObj.toString());
			Long amtToRecieveInCsAmt = Me2uUtil.convertNgweeToCSAmt(fdpRequest, amtToRecieveInNgwee);
			for(Map.Entry<String, Long> settlementDA : settlementDAs.entrySet()){
				Long amtToDeduct = 0l;
				if(settlementDA.getValue() >= amtToRecieveInCsAmt){
					amtToDeduct = -1 * amtToRecieveInCsAmt;
					settlementDA.setValue(settlementDA.getValue() - amtToRecieveInCsAmt);
					amtToRecieveInCsAmt = 0l;
				}else{
					amtToDeduct = -1 * settlementDA.getValue();
					amtToRecieveInCsAmt -= settlementDA.getValue();
					settlementDA.setValue(0l);
				}
				final CommandParam commandParam = fdpCommand.getInputParam("dedicatedAccountUpdateInformation");
				if(commandParam != null && commandParam instanceof CommandParamInput && commandParam.getChilderen() != null 
						&& null != commandParam.getChilderen().get(0)) {
					CommandParamInput daUpdateCpi = (CommandParamInput)commandParam;	
					daUpdateCpi.getChilderen().add(createCommandParamForDACharging((CommandParamInput)commandParam.getChilderen().get(0),
							settlementDA.getKey(), amtToDeduct.toString()));
					daSettlementDone = true;
				}
				if(amtToRecieveInCsAmt == 0l || isTransferToMainAccount){
					commandParam.getChilderen().remove(0);
					break;
				}
			}
			// update the amount that recipient is to recieve after settlement DA deductions
			((FDPRequestImpl)fdpRequest).putAuxiliaryRequestParameter(AuxRequestParam.ME2U_AMT_TO_BE_RECIEVED, Me2uUtil.convertCSToNgweeAmt(fdpRequest, amtToRecieveInCsAmt));
		}
		return daSettlementDone;
	}	
			
	/**
	 * This method will return the map of DAs that need to be settled with their current values		
	 * @param fdpRequest
	 * @return
	 * @throws ExecutionFailedException 
	 */
	private Map<String, Long> getSettlementDaValuesMap(FDPRequest fdpRequest) throws ExecutionFailedException {
		Map<String, Long> settlementDaMap = new LinkedHashMap<String, Long>();
		final Map<String, String> configurationMap = fdpRequest.getCircle().getConfigurationKeyValueMap();
		String settlementDaKeyValue = configurationMap.get(ConfigurationKey.ME2U_RECIPIENT_SETTLEMENT_DAS.getAttributeName());
		if(null != settlementDaKeyValue && !settlementDaKeyValue.isEmpty()){
			String settlementDAs[] = settlementDaKeyValue.split(FDPConstant.COMMA);
			executeCommand(fdpRequest, Command.GET_BALANCE_AND_DATE, false, true);
			Map<String, Object> daMap = MVELUtil.evaluateDetailsForUser(fdpRequest.getExecutedCommandForBeneficiary(Command.GET_BALANCE_AND_DATE.getCommandDisplayName()));
			for(int i = 0; i < settlementDAs.length; i++){
				Object daValueObj = daMap.get(FDPCSAttributeValue.DA.name() + FDPConstant.UNDERSCORE +
						settlementDAs[i] + FDPConstant.UNDERSCORE + FDPCSAttributeParam.VALUE.name());
				if(null != daValueObj && Long.parseLong(daValueObj.toString()) > 0l){
					settlementDaMap.put(settlementDAs[i], Long.parseLong(daValueObj.toString()));
				}
			}
		}
		return settlementDaMap;
	}
	
	/**
	 * This function executes the command based on beneficiary flag
	 * @param fdpRequest
	 * @param cmdToExecute
	 * @param forceExecution
	 * @param forBeneficiary
	 * @return
	 * @throws ExecutionFailedException 
	 */
	public static Status executeCommand(FDPRequest fdpRequest, Command cmdToExecute, boolean forceExecution, boolean forBeneficiary) throws ExecutionFailedException{
		updateSubscriberInRequestForBeneficiary(fdpRequest, forBeneficiary, LoggerUtil.getSummaryLoggerFromRequest(fdpRequest));
		FDPCommand fdpCmdToExecute = (forceExecution) ? null :  ((forBeneficiary) ? fdpRequest.getExecutedCommandForBeneficiary(cmdToExecute.getCommandDisplayName())
				: fdpRequest.getExecutedCommand(cmdToExecute.getCommandDisplayName()));
		Status status = Status.FAILURE;
		if(fdpCmdToExecute == null){
			final FDPCacheable cachedCommand = ApplicationConfigUtil.getMetaDataCache().getValue(new FDPMetaBag(fdpRequest.getCircle(), ModuleType.COMMAND, cmdToExecute.getCommandDisplayName()));
			if (cachedCommand instanceof FDPCommand) {
				fdpCmdToExecute = (FDPCommand) cachedCommand;
				status = fdpCmdToExecute.execute(fdpRequest);
				if (!status.equals(Status.SUCCESS)) {
					throw new ExecutionFailedException(cmdToExecute.getCommandDisplayName() + " could not be executed");
				}else{
					if(forBeneficiary){
						fdpRequest.addExecutedCommandForBeneficiary(fdpCmdToExecute);
					}else{
						fdpRequest.addExecutedCommand(fdpCmdToExecute);
					}
					status = Status.SUCCESS;
				}
			} else {
				throw new ExecutionFailedException(cmdToExecute.getCommandDisplayName() + " not configured in cache");
			}			
		}
		updateSubscriberInRequestForBeneficiary(fdpRequest, false, LoggerUtil.getSummaryLoggerFromRequest(fdpRequest));
		return status;
	}
	
	/**
	 * This method updates the msisdn in fdpRequest based on input param toUdpateBeneficiary
	 * @param fdpRequest
	 * @param toUdpateBeneficiary
	 * @param logger
	 */
	public static void updateSubscriberInRequestForBeneficiary(final FDPRequest fdpRequest, final boolean toUdpateBeneficiary, final Logger logger) {
		if(toUdpateBeneficiary) {
			((FDPRequestImpl)fdpRequest).putAuxiliaryRequestParameter(AuxRequestParam.REQUESTOR_MSISDN, fdpRequest.getSubscriberNumber());;
			Object beneficiaryMsisdnObject = fdpRequest.getAuxiliaryRequestParameter(AuxRequestParam.ME2U_SUBSCRIBER_TO_CREDIT);
			if(null != beneficiaryMsisdnObject){
				FDPLogger.debug(logger, ServiceProvisioningUtil.class, "updateSubscriberInRequestForBeneficiary", "Updating Subscriber Number in request from "
						+ fdpRequest.getSubscriberNumber() + " to " + beneficiaryMsisdnObject.toString());
				((FDPRequestImpl)fdpRequest).setSubscriberNumber(Long.valueOf(beneficiaryMsisdnObject.toString()));
			}
		} else {
			if(fdpRequest.getAuxiliaryRequestParameter(AuxRequestParam.REQUESTOR_MSISDN) != null){
				((FDPRequestImpl)fdpRequest).setSubscriberNumber((Long)fdpRequest.getAuxiliaryRequestParameter(AuxRequestParam.REQUESTOR_MSISDN));
			}
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
}
