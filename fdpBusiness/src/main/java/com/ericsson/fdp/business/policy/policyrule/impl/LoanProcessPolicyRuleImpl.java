package com.ericsson.fdp.business.policy.policyrule.impl;

import java.util.Collections;
import java.util.Map;

import ch.qos.logback.classic.Logger;

import com.ericsson.fdp.FDPCacheable;
import com.ericsson.fdp.business.charging.value.ChargingValue;
import com.ericsson.fdp.business.codResponse.CodResponse;
import com.ericsson.fdp.business.enums.AuxRequestParam;
import com.ericsson.fdp.business.enums.Command;
import com.ericsson.fdp.business.enums.ConfigurationKey;
import com.ericsson.fdp.business.enums.PolicyStatus;
import com.ericsson.fdp.business.enums.RequestMetaValuesKey;
import com.ericsson.fdp.business.enums.TLVOptions;
import com.ericsson.fdp.business.enums.mcarbon.loan.CreditStatus;
import com.ericsson.fdp.business.exception.NotificationFailedException;
import com.ericsson.fdp.business.mcarbon.response.MCarbonLoanResponse;
import com.ericsson.fdp.business.request.FDPPolicyResponse;
import com.ericsson.fdp.business.request.impl.FDPPolicyResponseImpl;
import com.ericsson.fdp.business.request.impl.FDPResponseImpl;
import com.ericsson.fdp.business.util.ClassUtil;
import com.ericsson.fdp.business.util.LoggerUtil;
import com.ericsson.fdp.business.util.NotificationUtil;
import com.ericsson.fdp.business.util.RequestUtil;
import com.ericsson.fdp.business.util.ResponseUtil;
import com.ericsson.fdp.common.constants.FDPConstant;
import com.ericsson.fdp.common.enums.DynamicMenuAdditionalInfoKey;
import com.ericsson.fdp.common.enums.ExternalSystem;
import com.ericsson.fdp.common.enums.ModuleType;
import com.ericsson.fdp.common.enums.Status;
import com.ericsson.fdp.common.vo.FDPCircle;
import com.ericsson.fdp.core.command.FDPCommand;
import com.ericsson.fdp.core.command.param.CommandParam;
import com.ericsson.fdp.core.exception.ExecutionFailedException;
import com.ericsson.fdp.core.logging.FDPLogger;
import com.ericsson.fdp.core.node.FDPNode;
import com.ericsson.fdp.core.request.FDPRequest;
import com.ericsson.fdp.core.request.FDPResponse;
import com.ericsson.fdp.core.request.impl.FDPSMPPRequestImpl;
import com.ericsson.fdp.core.serviceprovisioning.bean.FDPMetaBag;
import com.ericsson.fdp.core.utils.ApplicationConfigUtil;

public class LoanProcessPolicyRuleImpl extends AbstractPolicyRule {

	/**
	 * serial version id.
	 */
	private static final long serialVersionUID = 3456789907432345561L;
	
	private static final String ACCOUNT_VALUE_1 = "accountValue1";
	
	/**
	 * The circle logger.
	 */
	private Logger circleLogger = null;


	/**
	 * FOR COD
	 * Checks for the Low balance and displays the Notification accordingly.
	 * If Balance is sufficient to buy the product then returns null.
	 */
	@Override
	public FDPResponse displayRule(FDPRequest fdpRequest) throws ExecutionFailedException {
		FDPResponse fdpResponse = null;
		String notificationText = null;
		if(isLowBalance(fdpRequest)){
			LoggerUtil.generatePolicyBehaviourLogsForLowBalance(fdpRequest, "Y");
			Float loanAmount = getEligibleLoanAmount(fdpRequest);
		/**	if(loanAmount != null){
				if(fdpRequest instanceof FDPSMPPRequestImpl){
					//((FDPSMPPRequestImpl)fdpRequest).putAuxiliaryRequestParameter(AuxRequestParam.MC_LOAN_PURCHASE_INPUT, loanAmount);
					//((FDPSMPPRequestImpl)fdpRequest).putAuxiliaryRequestParameter(AuxRequestParam.MC_LOAN_DISPLAY_TEXT,AuxRequestParam.MC_LOAN_DISPLAY_TEXT.name());
				}
			}else {
				if(fdpRequest instanceof FDPSMPPRequestImpl){
					((FDPSMPPRequestImpl)fdpRequest).putAuxiliaryRequestParameter(AuxRequestParam.MC_LOAN_PURCHASE_INPUT, FDPConstant.EMPTY_STRING);
			//((FDPSMPPRequestImpl)fdpRequest).putAuxiliaryRequestParameter(AuxRequestParam.MC_LOAN_DISPLAY_TEXT,FDPConstant.EMPTY_STRING);
				}
			} **/

			if(null != fdpRequest && (fdpRequest instanceof FDPSMPPRequestImpl) && null == loanAmount){
				((FDPSMPPRequestImpl)fdpRequest).putAuxiliaryRequestParameter(AuxRequestParam.MC_LOAN_PURCHASE_INPUT, FDPConstant.EMPTY_STRING);
				((FDPSMPPRequestImpl)fdpRequest).putAuxiliaryRequestParameter(AuxRequestParam.MC_LOAN_PURCHASE_PROCESSING_FEE,  FDPConstant.EMPTY_STRING);
				return null;
				
			}else{
					LoggerUtil.generatePolicyBehaviourLogsForLoanEligibility(fdpRequest, loanAmount.toString());
				 }


					notificationText = getEligibilityNotification(fdpRequest);
				
				if(notificationText == null){
					throw new ExecutionFailedException("Loan Eligibility notification not configured.");
				}
				else{
					fdpResponse = new FDPResponseImpl(Status.SUCCESS, false, ResponseUtil.createResponseMessageInList(
							fdpRequest.getChannel(), notificationText, TLVOptions.SESSION_CONTINUE));
				}
			}
		
		else{
			LoggerUtil.generatePolicyBehaviourLogsForLowBalance(fdpRequest, "N");
		}
		
		return fdpResponse;
	}

	
	/**
	 * Validates the subscriber action if he has chosen to avail Loan or COD.
	 */
	@Override
	public FDPPolicyResponse validatePolicyRule(Object input, FDPRequest fdpRequest, Object... otherParams)
			throws ExecutionFailedException {
		FDPPolicyResponse response = new FDPPolicyResponseImpl(PolicyStatus.SKIP_EXECUTION, null, null, true, null);
		LoggerUtil.generatePolicyBehaviourLogsForUserBehaviourLoanCod(fdpRequest, AuxRequestParam.CONFIRMATION_FOR_LOAN_COD.getName());
		if (input == null) {
			throw new ExecutionFailedException("Cannot validate policy value");
		} else if (input != null) {
			final Map<String, String> configurationMap = fdpRequest.getCircle().getConfigurationKeyValueMap();
			if(fdpRequest.getAuxiliaryRequestParameter(AuxRequestParam.MC_LOAN_PURCHASE_PROCESSING_FEE)!=null){
			final String validResponsesForProductBuy = configurationMap
					.get(ConfigurationKey.PRODUCT_BUY_VALID_RESPONSES.getAttributeName());
			boolean isValid = false;
			if (validResponsesForProductBuy != null && !validResponsesForProductBuy.isEmpty()) {
				final String[] validResponses = validResponsesForProductBuy.split(FDPConstant.COMMA);
				for (final String validResponse : validResponses) {
					if (validResponse.equalsIgnoreCase(input.toString())) {
						isValid = true;
						// LoggerUtil.generateProductBehaviourLogsForUserBehaviour(fdpRequest,
						// ProductBehaviourStatus.CONFIRMED);
						break;
					}
				}

			}
			if (isValid) {
				if(!processSubscriberLoanAmount(fdpRequest)){
					LoggerUtil.generatePolicyBehaviourLogsForLoanPurchase(fdpRequest, Status.FAILURE.getStatusText());
					String notifText = getLoanFailureNotification(fdpRequest);
					if(notifText == null){
						throw new ExecutionFailedException("Loan Failure notification not configured.");
					}
					response = new FDPPolicyResponseImpl(PolicyStatus.FAILURE, notifText, null, true,
							ResponseUtil.createResponseMessageInList(fdpRequest.getChannel(), notifText,
									TLVOptions.SESSION_TERMINATE));
				}
				return response;
			}
			}
				final String validResponseForCOD=configurationMap.get(ConfigurationKey.PRODUCT_BUY_VALID_RESPONSES_FOR_COD.getAttributeName());
				boolean isValid = false;
				if (validResponseForCOD != null && !validResponseForCOD.isEmpty()) {
					final String[] validResponsescod = validResponseForCOD.split(FDPConstant.COMMA);
					for (final String validResponsecod : validResponsescod) {
						if (validResponsecod.equalsIgnoreCase(input.toString())) {
							isValid = true;
							break;
							//response = new FDPPolicyResponseImpl(PolicyStatus.SUCCESS, null, null, true, null);
							//response.setNextRuleIndex(CouponPolicyIndex.PRODUCT_BUY_APPLY_COUPON_RULE.getIndex());
						}
					}

				}
				if (!isValid) {
					response = getResponseForInvalidInput(input, fdpRequest, configurationMap);
				}else {
					if(!sendCODRequestToOFFLineSystem(fdpRequest)){
					String notifText = getCODFailureNotification(fdpRequest);
					if(notifText == null){
						throw new ExecutionFailedException("COD Failure notification not configured.");
					}
					response = new FDPPolicyResponseImpl(PolicyStatus.FAILURE, notifText, null, true,
							ResponseUtil.createResponseMessageInList(fdpRequest.getChannel(), notifText,
									TLVOptions.SESSION_TERMINATE));
				}
					else{
				String notifText = getCODSuccessNotification(fdpRequest);
				if(notifText == null){
					throw new ExecutionFailedException("COD Success notification not configured.");
				}
				response = new FDPPolicyResponseImpl(PolicyStatus.FAILURE, notifText, null, true,
						ResponseUtil.createResponseMessageInList(fdpRequest.getChannel(), notifText,
								TLVOptions.SESSION_TERMINATE));
				}	
			}
		}
		return response;
	}
	
	/**
	 * This method is used to get response for invalid input.
	 * 
	 * @param input
	 *            the input.
	 * @param fdpRequest
	 *            the request.
	 * @param configurationMap
	 *            the configuration map.
	 * @return the response.
	 * @throws ExecutionFailedException
	 *             Exception in execution.
	 */
	private FDPPolicyResponse getResponseForInvalidInput(final Object input, final FDPRequest fdpRequest,
			final Map<String, String> configurationMap) throws ExecutionFailedException {
		FDPPolicyResponse response = new FDPPolicyResponseImpl(PolicyStatus.SUCCESS, null, null, true, null);
		String responseString = configurationMap.get(ConfigurationKey.INVALID_INPUT_STRING.getAttributeName());
		if (responseString == null || responseString.isEmpty()) {
			responseString = "Input invalid";
		}
		final String invalidResponsesForProductBuy = configurationMap
				.get(ConfigurationKey.PRODUCT_BUY_INVALID_RESPONSES.getAttributeName());
		boolean isInValid = false;
		if (invalidResponsesForProductBuy != null && !invalidResponsesForProductBuy.isEmpty()) {
			final String[] invalidResponses = invalidResponsesForProductBuy.split(FDPConstant.COMMA);
			for (final String invalidResponse : invalidResponses) {
				if (invalidResponse.equalsIgnoreCase(input.toString())) {
					isInValid = true;
					final String rejectedString = configurationMap.get(ConfigurationKey.REJECTED_INPUT_STRING
							.getAttributeName());
					final String resultString = (rejectedString == null) ? responseString : rejectedString;
					response = new FDPPolicyResponseImpl(PolicyStatus.FAILURE, resultString, null, true,
							ResponseUtil.createResponseMessageInList(fdpRequest.getChannel(), resultString,
									TLVOptions.SESSION_TERMINATE));
					break;
				}
			}
		}
		if (!isInValid) {
			response = getResponse(responseString, fdpRequest, ClassUtil.getLongValue(configurationMap
					.get(ConfigurationKey.PRODUCT_RETRY_NUMBER.getAttributeName())));
		}
		return response;
	}
	
	/**
	 * This method is used to get response.
	 * 
	 * @param responseString
	 *            the response string.
	 * @param retryNumber
	 *            the retry number.
	 * @return the response formed.
	 * @throws ExecutionFailedException
	 *             Exception in execution.
	 */
	private FDPPolicyResponse getResponse(final String responseString, final FDPRequest fdpRequest,
			final Long retryNumber) throws ExecutionFailedException {
		FDPPolicyResponse fdpPolicyResponse = new FDPPolicyResponseImpl(PolicyStatus.FAILURE, responseString, null,
				true, ResponseUtil.createResponseMessageInList(fdpRequest.getChannel(), responseString,
						TLVOptions.SESSION_TERMINATE));
		if (retryNumber != null && retryNumber > 0) {
			final Long currentRetry = (Long) fdpRequest
					.getAuxiliaryRequestParameter(AuxRequestParam.POLICY_RETRY_VALUE);
			if (currentRetry == null || currentRetry + 1 <= retryNumber) {
				final String notText = getEligibilityNotification(fdpRequest);
				fdpPolicyResponse = new FDPPolicyResponseImpl(PolicyStatus.RETRY_POLICY, notText,
						currentRetry == null ? 1 : currentRetry + 1, false, ResponseUtil.createResponseMessageInList(
								fdpRequest.getChannel(), notText, TLVOptions.SESSION_CONTINUE));
			}
		}
		return fdpPolicyResponse;
	}
	
	
	/**
	 * this method is used to get the loan amount sufficient to buy product.
	 * this method will return null if the subscriber is not eligible for the loan.
	 * @param fdpRequest
	 * @return
	 * @throws ExecutionFailedException
	 */
	public Float getEligibleLoanAmount(FDPRequest request) throws ExecutionFailedException{
		Float loanAmount = null;
		MCarbonLoanResponse mcLoanResponse = executeMCLoanCommand(request, Command.MCARBON_GET_LOAN_ELIGIBILITY);
		
		if(mcLoanResponse != null){
			Collections.sort(mcLoanResponse.getFee().getProcessingFeeList());
			
			Integer chargingAmount = getProductChargingAmount(request);
			
			Integer subscriberBalance = getAccountBalanceofSubscriber(request);
			
			Integer minBalanceLeftPostLoan = getMinBalancePostLoan(request);
			
			for(MCarbonLoanResponse.ProcessingFee processingFee : mcLoanResponse.getFee().getProcessingFeeList()){
				if(processingFee.getValue() != null){
					if(((subscriberBalance - minBalanceLeftPostLoan) + processingFee.getType()*100) >= chargingAmount){
						loanAmount = processingFee.getType();
						if(request instanceof FDPSMPPRequestImpl){
							((FDPSMPPRequestImpl)request).putAuxiliaryRequestParameter(AuxRequestParam.MC_LOAN_PURCHASE_INPUT, loanAmount.intValue());
							((FDPSMPPRequestImpl)request).putAuxiliaryRequestParameter(AuxRequestParam.MC_LOAN_PURCHASE_PROCESSING_FEE, processingFee.getValue());
						}
						LoggerUtil.generatePolicyBehaviourLogsForLoanEligibility(request, loanAmount.toString());
						break;
					}
				}
			}
			
			if(loanAmount == null){
				LoggerUtil.generatePolicyBehaviourLogsForLoanEligibility(request, "N");
			}
		}
		return loanAmount;
	}
	
	/**
	 * This method is used to execute command on M-Carbon to provide loan.
	 * returns true if the loan process executed successfully else returns false.
	 * @param fdpRequest
	 * @param amount
	 * @return
	 * @throws ExecutionFailedException
	 */
	private boolean processSubscriberLoanAmount(FDPRequest fdpRequest) throws ExecutionFailedException{
		boolean status = false;
		MCarbonLoanResponse mcLoanResponse = null;
		
		mcLoanResponse = executeMCLoanCommand(fdpRequest, Command.MCARBON_AVAIL_SUBSCRIBER_LOAN);
		
		/*if(mcLoanResponse != null){
			if(mcLoanResponse.getCreditStatus().equals(CreditStatus.SUCCESS)){
				status = true;
			}
		}*/
		
		return (null != mcLoanResponse) ? CreditStatus.SUCCESS.equals(mcLoanResponse.getCreditStatus()) : status;
	}
	
	/**
	 * This method is used to to send COD request to OFFLine SYstem.
	 * returns true if the COD process executed successfully else returns false.
	 * @param fdpRequest
	 * @return
	 * @throws ExecutionFailedException
	 */
	private boolean sendCODRequestToOFFLineSystem(FDPRequest fdpRequest) throws ExecutionFailedException{
		boolean status = false;
		//change the type of CODOFFlineResoonse form String to the object sent by offilene
		String OFFLineCODRequestCommand=Command.COD_SEND_REQUEST_TO_OFFLINE.getCommandDisplayName();
		CodResponse CODOFFlineResponse= executeOfflineCommand(fdpRequest,OFFLineCODRequestCommand);
		
		if(CODOFFlineResponse!=null)
		{
			if(CODOFFlineResponse.getStatus()!=null){
			if(CODOFFlineResponse.getStatus().equalsIgnoreCase(FDPConstant.RESULT_SUCCESS.toLowerCase()))
			{
				status=true;
			}else{
				status=false;
			}
			}//check for response and set status as true
		}
		
		return status;
	}
	
	/**
	 * This method is used to execute command on FDPOFFLine  to send COD request to OFFLine SYstem.
	 * returns true if the COD process executed successfully else returns false.
	 * @param fdpRequest
	 * @param command display name
	 * @return
	 * @throws ExecutionFailedException
	 */
	private CodResponse executeOfflineCommand(FDPRequest fdpRequest, final String commandDisplayName)throws ExecutionFailedException
	{
		CodResponse OFFLineResponse=null;
		FDPCommand codfdpCommand = null;
		FDPCircle fdpCircle = fdpRequest.getCircle();
		
		final FDPCacheable fdpCommandCached = ApplicationConfigUtil.getMetaDataCache().getValue(
				new FDPMetaBag(fdpCircle, ModuleType.COMMAND, commandDisplayName));
		
		FDPLogger.debug(getCircleLogger(fdpRequest),getClass(),
				"send COD request to offline",
				LoggerUtil.getRequestAppender(fdpRequest) + "The subscriber number is : "
						+ fdpRequest.getSubscriberNumber()+"  command is "
						+ fdpCommandCached + " and circleCode is "+fdpRequest.getCircle().getCircleCode());
		
		if (fdpCommandCached != null && fdpCommandCached instanceof FDPCommand) {
			codfdpCommand = (FDPCommand) fdpCommandCached;
			
			Status codCommandStatus=codfdpCommand.execute(fdpRequest);
			FDPLogger.debug(getCircleLogger(fdpRequest),getClass(),
					"send COD request to offline",
					LoggerUtil.getRequestAppender(fdpRequest) + " The status of command executed is "
							+ codCommandStatus + " circle is "+fdpRequest.getCircle().getCircleCode());
			if(codCommandStatus.equals(Status.SUCCESS)){
				Object commandOutParam=codfdpCommand.getOutputParams().get("FDPOFFLINE".toLowerCase());
				FDPLogger.debug(getCircleLogger(fdpRequest),getClass(),
						" COD response from offline",
						LoggerUtil.getRequestAppender(fdpRequest) + " The COD Response in command Param object is  "
								+ commandOutParam + " circle is "+fdpRequest.getCircle().getCircleCode());
				if (commandOutParam!=null)
				{
					CommandParam oCommandParam=(CommandParam)commandOutParam;
					if(oCommandParam.getValue() != null && oCommandParam.getValue() instanceof CodResponse){
						OFFLineResponse = (CodResponse)oCommandParam.getValue();
					}
				}
			}
			
		}
		return OFFLineResponse;
	}
	/**
	 * This method is used to execute command on M-Carbon's Loan interface.
	 * @param fdpRequest
	 * @param command
	 * @return
	 * @throws ExecutionFailedException
	 */
	private MCarbonLoanResponse executeMCLoanCommand(FDPRequest fdpRequest, Command command) throws ExecutionFailedException{
		MCarbonLoanResponse mcLoanResponse = null;
		FDPCommand mcLoanEligibilityCommand = null;
		final String getCouponCommandName = command.getCommandDisplayName();
		final FDPCacheable fdpCommandCached = ApplicationConfigUtil.getMetaDataCache().getValue(
				new FDPMetaBag(fdpRequest.getCircle(), ModuleType.COMMAND, getCouponCommandName));
		
		if(fdpCommandCached != null && fdpCommandCached instanceof FDPCommand){
			mcLoanEligibilityCommand = (FDPCommand)fdpCommandCached;
			Status commandStatus = mcLoanEligibilityCommand.execute(fdpRequest);
			if(commandStatus.equals(Status.SUCCESS)){
				Object mcLoanResponseObject = mcLoanEligibilityCommand.getOutputParams().get("MCLOAN".toLowerCase());
				if(mcLoanResponseObject != null){
					CommandParam commandParam = (CommandParam) mcLoanResponseObject;
					if(commandParam.getValue() != null && commandParam.getValue() instanceof MCarbonLoanResponse){
						mcLoanResponse = (MCarbonLoanResponse)commandParam.getValue();
					}
				}
			}
		}
		
		return mcLoanResponse;
	}
	
	/**
	 * Returns true if subscriber's main account balance is lower than the charging amount.
	 * @param fdpRequest
	 * @return
	 */
	private boolean isLowBalance(FDPRequest fdpRequest) throws ExecutionFailedException{
		
		Integer chargingAmount = getProductChargingAmount(fdpRequest);
		
		Integer subscriberBalance = getAccountBalanceofSubscriber(fdpRequest);
		
		return subscriberBalance < chargingAmount;
	}
	
	/**
	 * This method returns the Account Balance 1 of Subscriber.
	 * @param fdpRequest
	 * @return
	 * @throws ExecutionFailedException
	 */
	private Integer getAccountBalanceofSubscriber(FDPRequest fdpRequest) throws ExecutionFailedException{
		FDPCommand getAccDetailCmd = fdpRequest.getExecutedCommand(Command.GETACCOUNTDETAILS.getCommandDisplayName());
		if(getAccDetailCmd == null){
			throw new ExecutionFailedException("Get Account Details not found in executed commands of FDPRequest.");
		}
		CommandParam accoutBalanceParam = getAccDetailCmd.getOutputParam(ACCOUNT_VALUE_1);
		if(accoutBalanceParam == null){
			throw new ExecutionFailedException("Subscriber Account Balance 1 not found in the command output parameters.");
		}
		
		return Integer.parseInt(accoutBalanceParam.getValue().toString());
	}
	
	/**
	 * this method returns the charging amount of the product being purchased.
	 * @param fdpRequest
	 * @return
	 * @throws ExecutionFailedException
	 */
	private Integer getProductChargingAmount(FDPRequest fdpRequest) throws ExecutionFailedException{
		final ChargingValue chargingValue = RequestUtil.getMetaValueFromRequest(fdpRequest, RequestMetaValuesKey.CHARGING_STEP, ChargingValue.class);
		Integer chargingAmount = Integer.parseInt(chargingValue.getChargingValue().toString());
		
		//Make charging positive in case of AIR.
		if(chargingValue.getExternalSystemToUse().equals(ExternalSystem.AIR)){
			chargingAmount = - chargingAmount;
		}
		
		return chargingAmount;
	}
	
	private Integer getMinBalancePostLoan(FDPRequest fdpRequest) throws ExecutionFailedException{
		String minBalanceString = fdpRequest.getCircle().getConfigurationKeyValueMap().get(ConfigurationKey.MIN_BALANCE_POST_LOAN.getAttributeName());
		Integer minBalanceInteger = null;
		
		if(minBalanceString != null){
			try{
				minBalanceInteger = Integer.parseInt(minBalanceString);
			} catch(NumberFormatException ex){
				throw new ExecutionFailedException(ConfigurationKey.MIN_BALANCE_POST_LOAN.getAttributeName()+"'s value cannot be recognized as a valid integer.", ex);
			}
		}
		else{
			minBalanceInteger = 0;
		}
		return minBalanceInteger;
	}
	
	/**
	 * This method returns the notification for the loan eligibility.
	 * @param request
	 * @return
	 * @throws ExecutionFailedException
	 */
	private String getEligibilityNotification(FDPRequest request) throws ExecutionFailedException{
		Logger circleLogger = LoggerUtil.getSummaryLoggerFromRequest(request);
		String notifMsg = null;
		Long notifId = null;
		FDPNode node = (FDPNode) request.getValueFromRequest(RequestMetaValuesKey.NODE);
		notifId = Long.parseLong((String)node.getAdditionalInfo(DynamicMenuAdditionalInfoKey.LOW_BALANCE_ELIGIBILITY_NOTIFICATION_ID.name()));
		if(notifId != null){
			try {
				notifMsg = NotificationUtil.createNotificationText(request, notifId, circleLogger);
			} catch (NotificationFailedException e) {
				throw new ExecutionFailedException("Error while creating loan eligibility notification.", e);
			}
		}
		return notifMsg;
	}
	
	/**
	 * The method returns the COD notification if the balance of subscriber is less than cost of product.
	 * @param request
	 * @return String
	 * @throws ExecutionFailedException
	 */
	private String getCODAndLoanNotification(final FDPRequest request) throws ExecutionFailedException{
		String notifMsg = null;
		Long notifId = null;
		final Logger circleLogger = LoggerUtil.getSummaryLoggerFromRequest(request);
		FDPNode node = (FDPNode) request.getValueFromRequest(RequestMetaValuesKey.NODE);
		notifId = Long.parseLong((String)node.getAdditionalInfo(DynamicMenuAdditionalInfoKey.LOW_BALANCE_ELIGIBILITY_NOTIFICATION_ID.name()));
		if(null != notifId){
			try {
				notifMsg = NotificationUtil.createNotificationText(request, notifId, circleLogger);
			} catch (NotificationFailedException e) {
				throw new ExecutionFailedException("Error while creating loan eligibility notification.", e);
			}
		}
		return notifMsg;
	}


        /**
	 * This method returns the notification on loan failure.
	 * @param request
	 * @return
	 * @throws ExecutionFailedException
	 */
	private String getLoanFailureNotification(FDPRequest request) throws ExecutionFailedException{
		Logger circleLogger = LoggerUtil.getSummaryLoggerFromRequest(request);
		String notifMsg = null;
		Long notifId = null;
		FDPNode node = (FDPNode) request.getValueFromRequest(RequestMetaValuesKey.NODE);
		notifId = Long.parseLong((String)node.getAdditionalInfo(DynamicMenuAdditionalInfoKey.LOAN_PROCESSING_FAILURE_NOTIFICATION_ID.name()));
		if(notifId != null){
			try {
				notifMsg = NotificationUtil.createNotificationText(request, notifId, circleLogger);
			} catch (NotificationFailedException e) {
				throw new ExecutionFailedException("Error while creating loan failure notification.", e);
			}
		}
		return notifMsg;
	}
	
	/**
	 * The method returns the COD Success notification if COD request is submitted successfully to OFFLine System
	 * @param request
	 * @return String
	 * @throws ExecutionFailedException
	 */
	private String getCODSuccessNotification(FDPRequest request) throws ExecutionFailedException{
		Logger circleLogger = LoggerUtil.getSummaryLoggerFromRequest(request);
		String notifMsg = null;
		Long notifId = null;
		FDPNode node = (FDPNode) request.getValueFromRequest(RequestMetaValuesKey.NODE);
		notifId = Long.parseLong((String)node.getAdditionalInfo(DynamicMenuAdditionalInfoKey.CASH_ON_DELIVERY_SUCCESS_NOTIFICATION_ID.name()));
		if(notifId != null){
			try {
				notifMsg = NotificationUtil.createNotificationText(request, notifId, circleLogger);
			} catch (NotificationFailedException e) {
				throw new ExecutionFailedException("Error while creating COD Success notification.", e);
			}
		}
		return notifMsg;
	}
	
	/**
	 * The method returns the COD Failure notification if COD request is not submitted successfully to OFFLine System
	 * @param request
	 * @return String
	 * @throws ExecutionFailedException
	 */
	private String getCODFailureNotification(FDPRequest request) throws ExecutionFailedException{
		Logger circleLogger = LoggerUtil.getSummaryLoggerFromRequest(request);
		String notifMsg = null;
		Long notifId = null;
		FDPNode node = (FDPNode) request.getValueFromRequest(RequestMetaValuesKey.NODE);
		notifId = Long.parseLong((String)node.getAdditionalInfo(DynamicMenuAdditionalInfoKey.CASH_ON_DELIVERY_FAILURE_NOTIFICATION_ID.name()));
		if(notifId != null){
			try {
				notifMsg = NotificationUtil.createNotificationText(request, notifId, circleLogger);
			} catch (NotificationFailedException e) {
				throw new ExecutionFailedException("Error while creating loan failure notification.", e);
			}
		}
		return notifMsg;
	}
	
	/**
	 * This method is sued to get the circle logger.
	 * 
	 * @param fdpRequest
	 *            the request to get the circle logger.
	 * @return the circle logger.
	 */
	private Logger getCircleLogger(final FDPRequest fdpRequest) {
		if (circleLogger == null) {
			circleLogger = LoggerUtil.getSummaryLoggerFromRequest(fdpRequest);
		}
		return circleLogger;
	}
	
@Override
	public boolean isRuleNoInputRequired() {
		return true;
	}
}
