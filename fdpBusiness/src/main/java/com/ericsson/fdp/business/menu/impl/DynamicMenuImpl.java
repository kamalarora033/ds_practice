package com.ericsson.fdp.business.menu.impl;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Map;

import javax.annotation.Resource;
import javax.ejb.Stateless;
import javax.inject.Inject;
import javax.jms.Queue;

import com.ericsson.fdp.FDPCacheable;
import com.ericsson.fdp.business.enums.AuxRequestParam;
import com.ericsson.fdp.business.enums.Command;
import com.ericsson.fdp.business.enums.ConfigurationKey;
import com.ericsson.fdp.business.enums.DynamicMenuPaginationKey;
import com.ericsson.fdp.business.enums.RequestMetaValuesKey;
import com.ericsson.fdp.business.enums.TLVOptions;
import com.ericsson.fdp.business.exception.NotificationFailedException;
import com.ericsson.fdp.business.menu.FDPDynamicMenu;
import com.ericsson.fdp.business.menu.FDPDynamicMenuAliasCode;
import com.ericsson.fdp.business.node.FDPServiceProvisioningNode;
import com.ericsson.fdp.business.node.impl.ExitMenuNode;
import com.ericsson.fdp.business.node.impl.ReturnMenuNode;
import com.ericsson.fdp.business.policy.FDPExecutesServiceProv;
import com.ericsson.fdp.business.product.Product;
import com.ericsson.fdp.business.request.FDPCheckConsumerResponse;
import com.ericsson.fdp.business.request.impl.FDPResponseImpl;
import com.ericsson.fdp.business.request.requestString.impl.FDPUSSDRequestStringImpl;
import com.ericsson.fdp.business.serviceprovisioning.ServiceProvisioning;
import com.ericsson.fdp.business.sharedaccount.service.CheckConsumerService;
import com.ericsson.fdp.business.transaction.TransactionSequenceGeneratorService;
import com.ericsson.fdp.business.util.CommandUtil;
import com.ericsson.fdp.business.util.CouponConsumptionUtil;
import com.ericsson.fdp.business.util.DynamicMenuUtil;
import com.ericsson.fdp.business.util.LoggerUtil;
import com.ericsson.fdp.business.util.NotificationUtil;
import com.ericsson.fdp.business.util.PaginationUtil;
import com.ericsson.fdp.business.util.RequestUtil;
import com.ericsson.fdp.business.util.ResponseUtil;
import com.ericsson.fdp.business.util.ServiceProvisioningUtil;
import com.ericsson.fdp.business.util.TariffEnquiryNotificationUtil;
import com.ericsson.fdp.common.constants.FDPConstant;
import com.ericsson.fdp.common.constants.JNDILookupConstant;
import com.ericsson.fdp.common.enums.ChannelType;
import com.ericsson.fdp.common.enums.ExternalSystem;
import com.ericsson.fdp.common.enums.Status;
import com.ericsson.fdp.common.enums.Visibility;
import com.ericsson.fdp.common.vo.FDPCircle;
import com.ericsson.fdp.core.cache.AppCacheSubStore;
import com.ericsson.fdp.core.command.FDPCommand;
import com.ericsson.fdp.core.dsm.framework.CacheQueueConstants;
import com.ericsson.fdp.core.dsm.framework.service.FDPCache;
import com.ericsson.fdp.core.dsm.framework.service.impl.FDPMetaCacheProducer;
import com.ericsson.fdp.core.exception.EvaluationFailedException;
import com.ericsson.fdp.core.exception.ExecutionFailedException;
import com.ericsson.fdp.core.logging.FDPLogger;
import com.ericsson.fdp.core.logging.FDPLoggerFactory;
import com.ericsson.fdp.core.node.FDPNode;
import com.ericsson.fdp.core.request.FDPRequest;
import com.ericsson.fdp.core.request.FDPResponse;
import com.ericsson.fdp.core.request.FDPSMPPRequest;
import com.ericsson.fdp.core.request.impl.FDPRequestImpl;
import com.ericsson.fdp.core.request.impl.FDPSMPPRequestImpl;
import com.ericsson.fdp.core.request.requestString.FDPRequestString;
import com.ericsson.fdp.core.serviceprovisioning.bean.FDPAppBag;
import com.ericsson.fdp.core.serviceprovisioning.bean.FDPMetaBag;
import com.ericsson.fdp.dao.dto.UpdateRequestDTO;
import com.ericsson.fdp.dao.enums.ProductAdditionalInfoEnum;

import ch.qos.logback.classic.Logger;

/**
 * This class implements the dynamic menu.
 * 
 * @author Ericsson
 * 
 */
@Stateless
public class DynamicMenuImpl implements FDPDynamicMenu {

	/** The fdp meta data cache. */
	@Resource(lookup = "java:app/fdpBusiness-1.0/MetaDataCache")
	private FDPCache<FDPMetaBag, FDPCacheable> fdpMetaDataCache;

	/** The fdp dynamic menu alias code. */
	@Resource(lookup = JNDILookupConstant.DYNAMIC_NODE_ALIAS_SERVICE_LOOK_UP)
	protected FDPDynamicMenuAliasCode fdpDynamicMenuAliasCode;

	/** The fdp service provisioning. */
	@Resource(lookup = "java:app/fdpBusiness-1.0/ServiceProvisioningImpl")
	private ServiceProvisioning fdpServiceProvisioning;
	
	@Inject
	FDPMetaCacheProducer fdpMetaCacheProducer;
	
	@Resource(mappedName = CacheQueueConstants.JMS_MMDEBIT_QUEUE)
	private Queue mmDebitQueue;

	/**
	 * The ussd dynamic menu impl. This class deals with the ussd requests.
	 */
	@Inject
	private USSDDynamicMenuImpl ussdDynamicMenuImpl;

	/**
	 * The smsc dynamic menu impl. This class deals with the sms requests.
	 */
	@Inject
	private SMSCDynamicMenuImpl smscDynamicMenuImpl;

	/** The check consumer service. */
	@Resource(lookup = "java:app/fdpBusiness-1.0/CheckConsumerServiceImpl")
	private CheckConsumerService checkConsumerService;
	
	@Resource(lookup = JNDILookupConstant.TRANSACTION_ID_GENERATOR_SERVICE)
	private TransactionSequenceGeneratorService generatorService;
	
	@Resource(lookup = "java:app/fdpCoreServices-1.0/ApplicationConfigCache")
	private FDPCache<FDPAppBag, Object> applicationConfigCache;

	/**
	 * The circle logger.
	 */
	private Logger circleLogger = null;

	/**
	 * This method is sued to get the circle logger.
	 * 
	 * @param fdpRequest
	 *            the request to get the circle logger.
	 * @return the circle logger.
	 */
	
	protected Logger getCircleLogger(final FDPRequest fdpRequest) {
		if (circleLogger == null) {
			circleLogger = LoggerUtil.getSummaryLoggerFromRequest(fdpRequest);
		}
		return circleLogger;
	}

	@Override
	public FDPResponse executeDynamicMenu(final FDPSMPPRequest dynamicMenuRequest) throws ExecutionFailedException {
		FDPResponse fdpResponse = null;
		try {
		FDPLogger.debug(
				getCircleLogger(dynamicMenuRequest),
				getClass(),
				"executeDynamicMenu()",
				LoggerUtil.getRequestAppender(dynamicMenuRequest) + "The request message is : "
						+ dynamicMenuRequest.getRequestString());
		LoggerUtil.generatePreLogsForUserBehaviour(dynamicMenuRequest);
		RequestUtil.updateRequestValues(dynamicMenuRequest);
		FDPNode fdpNode = null;
			
		updateTransactionId(dynamicMenuRequest);
		//boolean isPrepaidConsumer = false;
		boolean isDirectProductBuy = false;
		boolean isDirectLeg = false;
		// If this is the response of last served string then check if
		// pagination is present.
		if (dynamicMenuRequest.getLastServedString() != null) {
			fdpResponse = checkIsPaginationRequest(dynamicMenuRequest);
			if (fdpResponse == null) {
				fdpNode = ussdDynamicMenuImpl.getNodeForPreviousRequestAndUpdateRequestValue(dynamicMenuRequest);
			}
			//isPrepaidConsumer = true;
		} else {
			isDirectLeg = true;
			//if the DirectProductBuy
			isDirectProductBuy = true;
			
			/*
			Feature Name: Allow postpaid and blackberry user to use CIS system
			Changes: Remove postpaid/prepaid validation
			Date: 27-oct-2015
			Singnum Id: ECDGGIQ
			Comment :/* As per the latest discussion with Sunil and Asish Todiya , we are not going to 
			 * check subscriber type , we will simply handle it using constraints */
			
			/*fdpCheckConsumerResp = checkConsumerService
					.checkPrePaidConsumer(dynamicMenuRequest);*/
			
			//fdpResponse = checkConsumerIsPrePaid(dynamicMenuRequest);
			//if (fdpResponse == null) {		
				//isPrepaidConsumer = true;
				// check whether new request is a alias of some other code..
			RequestUtil.updateSIMLangaugeInRequest(dynamicMenuRequest,applicationConfigCache);
				fdpNode = fdpDynamicMenuAliasCode.getFDPDynamicMenuAliasCode(dynamicMenuRequest);

				if (fdpNode == null) {
					// For a new request find the node.
					fdpNode = DynamicMenuUtil.getAndUpdateFDPNode(dynamicMenuRequest.getCircle(),
							dynamicMenuRequest.getRequestString(), dynamicMenuRequest);
				}
			//}
		}
		/*FDPLogger.debug(getCircleLogger(dynamicMenuRequest), getClass(), "executeDynamicMenu()",
				LoggerUtil.getRequestAppender(dynamicMenuRequest) + " is consumer prepaid : " + isPrepaidConsumer);*/
		
		/*FDPLogger.debug(getCircleLogger(dynamicMenuRequest), getClass(), "executeDynamicMenu()",
				LoggerUtil.getRequestAppender(dynamicMenuRequest) + " is consumer type : " + fdpCheckConsumerResp.getSubscriberType());
		*/

		//if (fdpResponse == null && isPrepaidConsumer) {
		if (fdpResponse == null ) {
			if (fdpNode == null) {
				fdpNode = getFlexiNode(dynamicMenuRequest);
			}
			// Leaf nodes........
			if (fdpNode != null	&& isDirectProductBuy && (!Visibility.VISIBLE_FOR_MENU.equals(fdpNode.getVisibility()) && !Visibility.VISIBLE_FOR_DIRECT.equals(fdpNode
					.getVisibility()))) {
				FDPLogger.debug(getCircleLogger(dynamicMenuRequest), getClass(), "executeDynamicMenu()",
						LoggerUtil.getRequestAppender(dynamicMenuRequest) + "NODE not Visible "+fdpNode.getVisibility());
				fdpNode = null;
			}
			
			if(null != fdpNode && isDirectProductBuy && !DynamicMenuUtil.isWhiteListedForActiveForTestDMNode(dynamicMenuRequest,fdpNode)) {
				FDPLogger.debug(getCircleLogger(dynamicMenuRequest), getClass(), "executeDynamicMenu()",
						LoggerUtil.getRequestAppender(dynamicMenuRequest) + "Only Whitelisted Msisdn are allowed for ACTIVE_FOR_TEST DM nodes.");

				fdpNode = null;
			}
			
			fdpResponse = getResponse(dynamicMenuRequest, fdpNode);
		}
		LoggerUtil.generatePostLogsForUserBehaviour(dynamicMenuRequest, fdpNode, isDirectLeg);
		DynamicMenuUtil.updateValueInCache(dynamicMenuRequest, fdpNode);
		FDPLogger.debug(getCircleLogger(dynamicMenuRequest), getClass(), "executeDynamicMenu()",
				LoggerUtil.getRequestAppender(dynamicMenuRequest) + "The response formed is :- " + fdpResponse);
		fdpResponse = RequestUtil.addResponseDecorators(fdpResponse);
		/*if(StringUtil.isNullOrEmpty(fdpResponse.getResponseString().get(0).getCurrDisplayText(DisplayArea.COMPLETE)) || (
				dynamicMenuRequest.getLastServedString()!=null && dynamicMenuRequest.getLastServedString().contains(fdpResponse.getResponseString().get(0).getCurrDisplayText(DisplayArea.COMPLETE))))
				{
			throw new Exception();
				}*/
		} catch (final Exception e) {
			fdpResponse = getFDPOnFailureNode(dynamicMenuRequest, e);
		}
		return fdpResponse;
		// return new FDPResponseImpl(Status.SUCCESS, true, "Text Message.");
	}

	/**
	 * This method is used to get the response from the node.
	 * 
	 * @param dynamicMenuRequest
	 *            the request.
	 * @param fdpNode
	 *            the node.
	 * @return the response.
	 * @throws ExecutionFailedException
	 *             Exception in execution.
	 */
	private FDPResponse getResponse(final FDPSMPPRequest dynamicMenuRequest, final FDPNode fdpNode)
			throws ExecutionFailedException {
		// if node is not found and not flexi recharge case.
		FDPResponse fdpResponse = null;
		if (fdpNode == null) {
			if(dynamicMenuRequest.getLastServedString()==null){
				final Map<String, String> configurationMap = dynamicMenuRequest.getCircle().getConfigurationKeyValueMap();
				Object redirectFlag= configurationMap.get(ConfigurationKey.REDIRECT_TO_MAIN_MENU_ON_INCORRECT_USSD.getAttributeName());
				if(redirectFlag!=null && redirectFlag.toString().equalsIgnoreCase("true")){
				fdpResponse=redirectToMainMenu(dynamicMenuRequest.getRequestStringInterface(),
						dynamicMenuRequest.getCircle(),dynamicMenuRequest);
				}
				if(fdpResponse==null)
					  fdpResponse = DynamicMenuUtil.getHelpTextResponse(dynamicMenuRequest);
			}else{
				//Case for Auto Renewal Subscriber come 2nd time with input 2 or invalid
				String subscriberAutoRenewalInput = ((FDPSMPPRequestImpl)dynamicMenuRequest).getRequestString();
				final Product product = (Product) dynamicMenuRequest.getValueFromRequest(RequestMetaValuesKey.PRODUCT);
				  if(ChannelType.USSD.equals(dynamicMenuRequest.getChannel()) && (FDPConstant.TRUE.equalsIgnoreCase(product.getAdditionalInfo(ProductAdditionalInfoEnum.IS_AUTO_RENEWAL)))
						  && null != dynamicMenuRequest.getAuxiliaryRequestParameter(AuxRequestParam.AUTO_RENEWAL_SESSION_TERMINATE) && 
						  FDPConstant.TRUE.equalsIgnoreCase(dynamicMenuRequest.getAuxiliaryRequestParameter(AuxRequestParam.AUTO_RENEWAL_SESSION_TERMINATE).toString())){
					  
					  String updatedUserInput = subscriberAutoRenewalInput.substring((subscriberAutoRenewalInput.length()-2),(subscriberAutoRenewalInput.length()-1));
					  String autoRenewalConfimationInput = RequestUtil.getConfigurationKeyValue(dynamicMenuRequest, ConfigurationKey.AUTO_RENEWAL_CONFIRMATION);
					  if(null != autoRenewalConfimationInput && autoRenewalConfimationInput.equalsIgnoreCase(updatedUserInput)){
						//Call for MPR command for Auto Renewal.
						  CommandUtil.executeCommandForAutoRenewal(dynamicMenuRequest);
						  
						  NotificationUtil.sendOfflineNotification(
								  dynamicMenuRequest, RequestUtil.getConfigurationKeyValue(dynamicMenuRequest, ConfigurationKey.AUTO_RENEWAL_CONFIRMATION_TEXT_SMS));
						  return new FDPResponseImpl(Status.SUCCESS, true, ResponseUtil.createResponseMessageInList(dynamicMenuRequest.getChannel(),
									RequestUtil.getConfigurationKeyValue(dynamicMenuRequest, ConfigurationKey.AUTO_RENEWAL_CONFIRMATION_TEXT), TLVOptions.SESSION_TERMINATE));
							
					  }else if(null != updatedUserInput && FDPConstant.ADHOC_KEY_PRESS.equalsIgnoreCase(updatedUserInput)){
						  //If input is 2
						  String helpText = dynamicMenuRequest.getAuxiliaryRequestParameter(AuxRequestParam.ADHOC_FLASH_TEXT).toString();
							FDPLogger.debug(
									getCircleLogger(dynamicMenuRequest),
									getClass(),
									"getResponse()",
									LoggerUtil.getRequestAppender(dynamicMenuRequest) + helpText);
							if(null != helpText)
							return new FDPResponseImpl(Status.SUCCESS, true, ResponseUtil.createResponseMessageInList(dynamicMenuRequest.getChannel(),
									helpText, TLVOptions.SESSION_TERMINATE));
							else
								new FDPResponseImpl(Status.SUCCESS, true, ResponseUtil.createResponseMessageInList(dynamicMenuRequest.getChannel(),
										"Thanks for buying MTN bundle", TLVOptions.SESSION_TERMINATE));

					  }else{
						  //For Invalid input.
						  return new FDPResponseImpl(Status.SUCCESS, true, ResponseUtil.createResponseMessageInList(dynamicMenuRequest.getChannel(),
									"Enter input is not valid.", TLVOptions.SESSION_TERMINATE));
					  }
					 
				  }
				  else{
						String helpText = "The Input is not valid.";
						FDPLogger.debug(
								getCircleLogger(dynamicMenuRequest),
								getClass(),
								"getResponse()",
								LoggerUtil.getRequestAppender(dynamicMenuRequest) + helpText);
						fdpResponse= new FDPResponseImpl(Status.SUCCESS, true, ResponseUtil.createResponseMessageInList(dynamicMenuRequest.getChannel(),
								helpText, TLVOptions.SESSION_TERMINATE));
					}
			}
						
		} else {
			
			//Case for Auto Renewal Subscriber come 2nd time
			if(ChannelType.USSD.equals(dynamicMenuRequest.getChannel()) && null != dynamicMenuRequest.getAuxiliaryRequestParameter(AuxRequestParam.AUTO_RENEWAL_SESSION_TERMINATE) && 
					FDPConstant.TRUE.equals(dynamicMenuRequest.getAuxiliaryRequestParameter(AuxRequestParam.AUTO_RENEWAL_SESSION_TERMINATE).toString()))
			{	
				String subscriberAutoRenewalInput = ((FDPSMPPRequestImpl)dynamicMenuRequest).getRequestString();
				String autoRenewalConfimationInput = RequestUtil.getConfigurationKeyValue(dynamicMenuRequest, ConfigurationKey.AUTO_RENEWAL_CONFIRMATION);
				if(null != autoRenewalConfimationInput && autoRenewalConfimationInput.equalsIgnoreCase(subscriberAutoRenewalInput)){
					//Call for MPR command for Auto Renewal.
					CommandUtil.executeCommandForAutoRenewal(dynamicMenuRequest);
					//Offline Notification Text for AutoRenewal Confirmation true
					NotificationUtil.sendOfflineNotification(
							dynamicMenuRequest, RequestUtil.getConfigurationKeyValue(dynamicMenuRequest, ConfigurationKey.AUTO_RENEWAL_CONFIRMATION_TEXT_SMS));
					return new FDPResponseImpl(Status.SUCCESS, true, ResponseUtil.createResponseMessageInList(dynamicMenuRequest.getChannel(),
							RequestUtil.getConfigurationKeyValue(dynamicMenuRequest, ConfigurationKey.AUTO_RENEWAL_CONFIRMATION_TEXT), TLVOptions.SESSION_TERMINATE));
					
				}else if(null != subscriberAutoRenewalInput && FDPConstant.ADHOC_KEY_PRESS.equalsIgnoreCase(subscriberAutoRenewalInput)){
					//If input is "2"
					String helpText = dynamicMenuRequest.getAuxiliaryRequestParameter(AuxRequestParam.ADHOC_FLASH_TEXT).toString();
					  if(null != helpText)
						  return new FDPResponseImpl(Status.SUCCESS, true, ResponseUtil.createResponseMessageInList(dynamicMenuRequest.getChannel(),
							helpText, TLVOptions.SESSION_TERMINATE));
					  else
						  return new FDPResponseImpl(Status.SUCCESS, true, ResponseUtil.createResponseMessageInList(dynamicMenuRequest.getChannel(),
									"Thanks for Buying MTN Bundle.", TLVOptions.SESSION_TERMINATE));
				}else{
					//For Invalid input
					 return new FDPResponseImpl(Status.SUCCESS, true, ResponseUtil.createResponseMessageInList(dynamicMenuRequest.getChannel(),
								"Enter Input is not valid.", TLVOptions.SESSION_TERMINATE));
				}
				  
				
			}
			
			FDPLogger.debug(
					getCircleLogger(dynamicMenuRequest),
					getClass(),
					"getResponse()",
					LoggerUtil.getRequestAppender(dynamicMenuRequest) + "Node to be executed found. "
							+ fdpNode.getDisplayName());
			// execute node.
			fdpResponse = executeNode(dynamicMenuRequest, fdpNode);
		}
		return fdpResponse;
	}
	
	private FDPResponse redirectToMainMenu(final FDPRequestString fdpRequestString, final FDPCircle fdpCircle, final FDPSMPPRequest dynamicMenuRequest){
		FDPResponse	fdpResponse=null;
		FDPCacheable fdpCacheable;
		try {
			fdpCacheable = (fdpRequestString != null) ? RequestUtil.getRootNode(fdpRequestString.getNodeString(),
					fdpCircle) : null;
		} catch (ExecutionFailedException e) {
			return fdpResponse;
		}
		final FDPNode fdpNode = (FDPNode) fdpCacheable;
		if(fdpNode!=null){
		final Logger circleLogger = LoggerUtil.getSummaryLoggerFromRequest(dynamicMenuRequest);
		FDPLogger.debug(circleLogger, DynamicMenuUtil.class, "getResponse()",
				LoggerUtil.getRequestAppender(dynamicMenuRequest) + "No defined node found. Redirecting to main menu.");
		// execute node.
			try {
				fdpResponse = executeNode(dynamicMenuRequest, fdpNode);
			} catch (ExecutionFailedException e) {
				return fdpResponse;
			}
			}
					return fdpResponse;
	}

	/**
	 * This method is used to update the transaction id.
	 * 
	 * @param dynamicMenuRequest
	 *            the dynamic menu request.
	 */
	protected void updateTransactionId(final FDPSMPPRequest dynamicMenuRequest) {
	//	final Long transactionIdnew = generateTransactionId();
		final Long transactionIdnew=generateTransactionID(Long.toString(dynamicMenuRequest.getSubscriberNumber()));
		if (dynamicMenuRequest instanceof FDPRequestImpl) {
			final FDPRequestImpl requestImpl = (FDPRequestImpl) dynamicMenuRequest;
			requestImpl.setOriginTransactionID(transactionIdnew);
			FDPLogger.debug(getCircleLogger(dynamicMenuRequest), getClass(), "executeDynamicMenu()",
					LoggerUtil.getRequestAppender(dynamicMenuRequest) + "Transaction id has been set as "
							+ transactionIdnew);
		}
	}

	
	private static Long generateTransactionID(String msisdn){
		String transactionid = null;
		try {
			transactionid = (System.nanoTime()+msisdn+
					InetAddress.getLocalHost().toString().replaceAll(".","")
					);
		} catch (UnknownHostException e) {
			transactionid=(System.nanoTime()
					+msisdn+
					System.currentTimeMillis());
		}
		transactionid=transactionid.length()>18?transactionid.substring(transactionid.length()-19, transactionid.length()-1):transactionid;
		  return Long.parseLong(transactionid);
		}
	
	/**
	 * This method is used to create the response for the nodes.
	 * 
	 * @param dynamicMenuRequest
	 *            The request object.
	 * @return the response object.
	 * @throws ExecutionFailedException
	 *             Exception, if node could not be executed.
	 */
	private FDPNode getFlexiNode(final FDPSMPPRequest dynamicMenuRequest) throws ExecutionFailedException {
		FDPNode fdpNode = null;
		if (ChannelType.USSD.equals(dynamicMenuRequest.getChannel())) {
			fdpNode = ussdDynamicMenuImpl.checkIfFlexiRechargeOrSharedAccount(dynamicMenuRequest);
		} else if (ChannelType.SMS.equals(dynamicMenuRequest.getChannel())) {
			fdpNode = smscDynamicMenuImpl.checkSMSCase(dynamicMenuRequest);
		}
		return fdpNode;
	}

	/**
	 * This method checks if the request is for pagination.
	 * 
	 * @param dynamicMenuRequest
	 *            The request.
	 * @return true, if it is for pagination.
	 * @throws ExecutionFailedException
	 *             Exception in execution.
	 */
	public FDPResponse checkIsPaginationRequest(final FDPSMPPRequest dynamicMenuRequest)
			throws ExecutionFailedException {
		FDPResponse fdpResponse = null;
		final DynamicMenuPaginationKey dynamicMenuPaginationKey = PaginationUtil.getPaginationOptionsForRequest(
				dynamicMenuRequest.getRequestString(), dynamicMenuRequest.getChannel(), dynamicMenuRequest.getCircle());
		if (dynamicMenuPaginationKey != null
				&& dynamicMenuRequest.getLastDisplayObject() != null
				&& PaginationUtil.checkIfPaginationOnRequest(
						dynamicMenuRequest.getLastDisplayObject().getCurrentNode(), dynamicMenuPaginationKey)) {
			getCircleLogger(dynamicMenuRequest).debug("Request is for pagination case " + dynamicMenuPaginationKey);
			boolean isPrevious = false;
			String displayString = null;
			switch (dynamicMenuPaginationKey) {
			/*case PREVIOUS:
				isPrevious = true;
				break;*/
			case MORE:
				displayString = PaginationUtil.handlePagination(!isPrevious, dynamicMenuRequest);
				break;
			case RETURN_TO_MAIN_MENU_STATUS:
				FDPNode fdpNode = PaginationUtil.getMainMenuNode(dynamicMenuRequest);
				RequestUtil.putAuxiliaryValueInRequest(dynamicMenuRequest, AuxRequestParam.PAGINATION_REQUEST,
						Boolean.TRUE);
				fdpResponse = executeNode(dynamicMenuRequest, fdpNode);
				DynamicMenuUtil.updateValueInCache(dynamicMenuRequest, fdpNode);
				break;
			case PREVIOUS:
			case RETURN_TO_PREVIOUS_MENU_STATUS:
				fdpNode = PaginationUtil.getPreviousMenuNode(dynamicMenuRequest);
				RequestUtil.putAuxiliaryValueInRequest(dynamicMenuRequest, AuxRequestParam.PAGINATION_REQUEST,
						Boolean.TRUE);
				fdpResponse = executeNode(dynamicMenuRequest, fdpNode);
				DynamicMenuUtil.updateValueInCache(dynamicMenuRequest, fdpNode);
				break;
			case EXIT_STATUS:
				fdpResponse = new FDPResponseImpl(Status.SUCCESS, true, ResponseUtil.createResponseMessageInList(
						dynamicMenuRequest.getChannel(),
						DynamicMenuUtil.getExitMenuResponse(dynamicMenuRequest.getCircle()),
						TLVOptions.SESSION_TERMINATE));
				break;
			default:
				throw new ExecutionFailedException("Could not handle pagination request for "
						+ dynamicMenuPaginationKey);
			}
			if (displayString != null) {
				fdpResponse = new FDPResponseImpl(Status.SUCCESS, false, ResponseUtil.createResponseMessageInList(
						dynamicMenuRequest.getChannel(), displayString, TLVOptions.SESSION_CONTINUE));
			}
		}
		return fdpResponse;

	}

	/**
	 * This method is used to execute a node.
	 * 
	 * @param dynamicMenuRequest
	 *            The request.
	 * @param fdpNode
	 *            The node to be executed.
	 * @return The response from executing the node.
	 * @throws ExecutionFailedException
	 *             Exception, if the execution fails.
	 */
	private FDPResponse executeNode(final FDPSMPPRequest dynamicMenuRequest, final FDPNode fdpNode)
			throws ExecutionFailedException {
		if (fdpNode == null) {
			throw new ExecutionFailedException("The node is not defined which is to be executed");
		}
		boolean nodeValue = false;
		FDPResponse fdpResponse = null;
		try {
			nodeValue = RequestUtil.evaluateNodeValue(dynamicMenuRequest, fdpNode);
			if (nodeValue) {
				RequestUtil.updateMetaValuesInRequest(dynamicMenuRequest, RequestMetaValuesKey.NODE, fdpNode);
				if (fdpNode instanceof FDPServiceProvisioningNode) {
					FDPLogger.debug(getCircleLogger(dynamicMenuRequest), getClass(), "executeNode()",
							LoggerUtil.getRequestAppender(dynamicMenuRequest) + "Executing Service provisioning node");
					final FDPServiceProvisioningNode serviceProvisioningNode = (FDPServiceProvisioningNode) fdpNode;
					fdpResponse = serviceProvisioningNode.executePolicy(dynamicMenuRequest);
					if (fdpResponse == null && !(fdpNode.getPolicy(dynamicMenuRequest) instanceof FDPExecutesServiceProv)) {
						RequestUtil.putPolicyRuleValuesInRequest(dynamicMenuRequest, serviceProvisioningNode);
						
						// Changes done for special handling if charging node is Mobile Money
						if ( dynamicMenuRequest.getExternalSystemToCharge() == ExternalSystem.MM && ChannelType.USSD.equals(dynamicMenuRequest.getChannel()) 
								&& Boolean.valueOf(RequestUtil.getConfigurationKeyValue(dynamicMenuRequest, ConfigurationKey.ASYNC_MM_DEBIT_PROCESSING))) {
							fdpResponse = getMobileMoneyAsynResponse(dynamicMenuRequest, serviceProvisioningNode, fdpServiceProvisioning);
						} else {
							fdpResponse = ServiceProvisioningUtil.executeServiceProvisioning(serviceProvisioningNode,
									dynamicMenuRequest, fdpServiceProvisioning);
						}
						
						
						//Consume Coupon if applied by User
						CouponConsumptionUtil.checkAndSendCouponConsumeRequest(dynamicMenuRequest, fdpResponse, getCircleLogger(dynamicMenuRequest));
						
						fdpResponse = ServiceProvisioningUtil.decorate(fdpResponse, fdpNode, dynamicMenuRequest);
					} else if (dynamicMenuRequest instanceof FDPRequestImpl && fdpNode.getPolicy(dynamicMenuRequest).isPolicyExecution(dynamicMenuRequest)) {
						((FDPRequestImpl) dynamicMenuRequest).setPolicyExecution(true);
					}
				} else {
					fdpResponse = executeOtherNode(dynamicMenuRequest, fdpNode);
				}
			} else {
				fdpResponse = RequestUtil.getHelpText(dynamicMenuRequest.getRequestStringInterface(),
						dynamicMenuRequest.getCircle(), dynamicMenuRequest.getChannel());
				FDPLogger.debug(
						getCircleLogger(dynamicMenuRequest),
						getClass(),
						"getHelpText()",
						LoggerUtil.getRequestAppender(dynamicMenuRequest) + "Help text found "
								+ fdpResponse.getResponseString());
			}
		} catch (final EvaluationFailedException e) {
			FDPLogger.error(getCircleLogger(dynamicMenuRequest), getClass(), "executeNode()",
					LoggerUtil.getRequestAppender(dynamicMenuRequest) + "The dynamic menu could not be displayed", e);
			throw new ExecutionFailedException("The dynamic menu could not be displayed", e);
		}
		return fdpResponse;
	}


	/**
	 * This method is used to execute the nodes which are not service
	 * provisioning.
	 * 
	 * @param dynamicMenuRequest
	 *            The request.
	 * @param fdpNode
	 *            The node to be executed.
	 * @return The response after execution.
	 * @throws ExecutionFailedException
	 *             Exception, if the node could not be executed.
	 */
	protected FDPResponse executeOtherNode(final FDPSMPPRequest dynamicMenuRequest, final FDPNode fdpNode)
			throws ExecutionFailedException {
		if (fdpNode instanceof ExitMenuNode) {
			FDPLogger.debug(getCircleLogger(dynamicMenuRequest), getClass(), "executeOtherNode()",
					LoggerUtil.getRequestAppender(dynamicMenuRequest) + "Exit menu case");
			return new FDPResponseImpl(Status.SUCCESS, true, ResponseUtil.createResponseMessageInList(
					dynamicMenuRequest.getChannel(),
					DynamicMenuUtil.getExitMenuResponse(dynamicMenuRequest.getCircle()), TLVOptions.SESSION_TERMINATE));
		} else if (fdpNode instanceof ReturnMenuNode) {
			FDPLogger.debug(getCircleLogger(dynamicMenuRequest), getClass(), "executeOtherNode()",
					LoggerUtil.getRequestAppender(dynamicMenuRequest) + "Return menu case.");
			boolean parentFound = false;
			final ReturnMenuNode returnMenuNode = (ReturnMenuNode) fdpNode;
			final FDPNode parentNode = returnMenuNode.getParent();
			if (parentNode != null) {
				final FDPNode grandParentNode = parentNode.getParent();
				if (grandParentNode != null) {
					parentFound = true;
					FDPLogger.debug(getCircleLogger(dynamicMenuRequest), getClass(), "executeOtherNode()",
							LoggerUtil.getRequestAppender(dynamicMenuRequest)
									+ "Return menu case found grand parent as " + grandParentNode.getDisplayName());
					return executeNode(dynamicMenuRequest, grandParentNode);
				}
			}
			if (!parentFound) {
				final FDPResponse fdpResponse = RequestUtil.getHelpText(dynamicMenuRequest.getRequestStringInterface(),
						dynamicMenuRequest.getCircle(), dynamicMenuRequest.getChannel());
				FDPLogger.debug(
						getCircleLogger(dynamicMenuRequest),
						getClass(),
						"getHelpText()",
						LoggerUtil.getRequestAppender(dynamicMenuRequest) + "Help text found "
								+ fdpResponse.getResponseString());
				return fdpResponse;
			}
		}
		return RequestUtil.createResponseFromDisplayObject(fdpNode.displayNode(dynamicMenuRequest), dynamicMenuRequest,
				fdpNode);
	}

	/**
	 * Check consumer is pre paid.
	 * 
	 * @param dynamicMenuRequest
	 *            the dynamic menu request
	 * @return the fDP response
	 * @throws ExecutionFailedException
	 *             the execution failed exception
	 */
	protected FDPResponse checkConsumerIsPrePaid(final FDPRequest dynamicMenuRequest) throws ExecutionFailedException {
		FDPResponse fdpResponse = null;
		try {
			final FDPCheckConsumerResponse fdpCheckConsumerResp = checkConsumerService
					.checkPrePaidConsumer(dynamicMenuRequest);
			RequestUtil.addExecutedCommandsToRequestImpl(dynamicMenuRequest, fdpCheckConsumerResp);
			final String notificationText = getNotificationTextForConsumer(dynamicMenuRequest, fdpCheckConsumerResp);
			if (notificationText != null) {
				fdpResponse = new FDPResponseImpl(Status.SUCCESS, false, ResponseUtil.createResponseMessageInList(
						dynamicMenuRequest.getChannel(), notificationText, TLVOptions.SESSION_TERMINATE), fdpCheckConsumerResp.getResponseError());
			}
		} catch (final NotificationFailedException e) {
			FDPLogger.error(FDPLoggerFactory.getCircleAdminLogger(dynamicMenuRequest.getCircle().getCircleName()),
					this.getClass(), "checkConsumerIsPrePaid", "Could not create notification text", e);
			throw new ExecutionFailedException("Could not create notification text", e);
		}
		return fdpResponse;
	}
	
	
	/**
	 * Gets the notification text for consumer.
	 * 
	 * @param dynamicMenuRequest
	 *            the dynamic menu request
	 * @param fdpCheckConsumerResp
	 *            the fdp check consumer resp
	 * @return the notification text for consumer
	 * @throws NotificationFailedException
	 *             the notification failed exception
	 */
	private String getNotificationTextForConsumer(final FDPRequest dynamicMenuRequest,
			final FDPCheckConsumerResponse fdpCheckConsumerResp) throws NotificationFailedException,
			ExecutionFailedException {
		String notificationText = null;
		if (Status.SUCCESS.equals(fdpCheckConsumerResp.getExecutionStatus())) {
			if (!fdpCheckConsumerResp.isPrePaidConsumer()) {
				notificationText = getNotificationTextForPostPaidConsumer(dynamicMenuRequest);
			}
		} else {
			notificationText = getNotificationTextForFailure(dynamicMenuRequest);
		}
		FDPLogger.debug(getCircleLogger(dynamicMenuRequest), getClass(), "getNotificationTextForConsumer()",
				LoggerUtil.getRequestAppender(dynamicMenuRequest) + "The notification Text is : " + notificationText);
		return notificationText;
	}

	/**
	 * Gets the notification text for failure.
	 * 
	 * @param dynamicMenuRequest
	 *            the dynamic menu request
	 * @return the notification text for failure
	 * @throws NotificationFailedException
	 *             the notification failed exception
	 */
	private String getNotificationTextForFailure(final FDPRequest dynamicMenuRequest)
			throws NotificationFailedException, ExecutionFailedException {
		String notificationText = null;
		final FDPCommand lastExecutedCommand = dynamicMenuRequest.getLastExecutedCommand();
		final FDPCircle fdpCircle = dynamicMenuRequest.getCircle();
		final Long notId = NotificationUtil.getNotificationIdForCommand(fdpCircle, lastExecutedCommand);
		FDPLogger.debug(getCircleLogger(dynamicMenuRequest), getClass(), "getNotificationTextForFailure()",
				LoggerUtil.getRequestAppender(dynamicMenuRequest) + " The notification id : " + notId);
		if (notId == null) {
			notificationText = FDPConstant.REQUEST_COULD_NOT_PROCESSED;
		} else {
			notificationText = NotificationUtil.createNotificationText(dynamicMenuRequest, notId, circleLogger);
		}
		return notificationText;
	}

	/**
	 * Gets the notification text for post paid consumer.
	 * 
	 * @param dynamicMenuRequest
	 *            the dynamic menu request
	 * @param fdpCircle
	 *            the fdp circle
	 * @return the notification text for post paid consumer
	 */
	private String getNotificationTextForPostPaidConsumer(final FDPRequest dynamicMenuRequest) {
		String notificationText = null;
		final FDPCircle fdpCircle = dynamicMenuRequest.getCircle();
		switch (dynamicMenuRequest.getChannel()) {
		case USSD:
			notificationText = fdpCircle.getConfigurationKeyValueMap().get(
					ConfigurationKey.NOTIFICATION_POSTPAID_CONSUMER_ERROR_CODE_USSD.getAttributeName());
			break;
		case SMS:
			notificationText = fdpCircle.getConfigurationKeyValueMap().get(
					ConfigurationKey.NOTIFICATION_POSTPAID_CONSUMER_ERROR_CODE_SMS.getAttributeName());
			break;
		case IVR:
			notificationText = fdpCircle.getConfigurationKeyValueMap().get(
					ConfigurationKey.NOTIFICATION_POSTPAID_CONSUMER_ERROR_CODE_IVR.getAttributeName());
		default:
			break;
		}
		return (null == notificationText ? FDPConstant.DEFAULT_POSTPAID_NOTIFICATION_TEXT : notificationText);
	}
	
	/**
	 * This method prepare the root node response in case of general exception.
	 * @param fdpRequest
	 * @return
	 */
	private FDPResponse getFDPOnFailureNode(final FDPSMPPRequest fdpRequest, final Exception exception) {
		FDPResponse fdpResponse = null;
		String nodeText = null;
		try {
			nodeText = (String)applicationConfigCache.getValue(new FDPAppBag(AppCacheSubStore.CONFIGURATION_MAP, FDPConstant.FDP_ON_GENERIC_FAILURE_FAILURE_MENU_NODE));
			nodeText = (null == nodeText) ? "*111#" : nodeText.trim();
			
			// Return the failure response in case of exception occurs during rollback 
			/*if (fdpRequest.getAuxiliaryRequestParameter(AuxRequestParam.LAST_EXECUTED_ROLLBACK_COMMAND) != null) {
				fdpResponse = getFDPGenericFailureNotification(fdpRequest, exception);
				return fdpResponse;
			}*/
			
			// This code is to set skip charging flag back to false if multiple product buy done in same
			// USSD session
			Object skipCharging = fdpRequest.getAuxiliaryRequestParameter(AuxRequestParam.SKIP_CHARGING);
			if(null != skipCharging && skipCharging instanceof Boolean && Boolean.TRUE.equals((Boolean) skipCharging)){
				((FDPRequestImpl) fdpRequest).putAuxiliaryRequestParameter(AuxRequestParam.SKIP_CHARGING, Boolean.FALSE);
			}
			
			String retrunToPrvNode = RequestUtil.getNotificationText(fdpRequest, ConfigurationKey.RETURN_TO_PREVIOUS_MENU_ON_EXCEPTION, "true");
			if (Boolean.parseBoolean(retrunToPrvNode))
				nodeText = ((FDPRequestImpl)fdpRequest).getLastServedString(); 
			else 
				((FDPSMPPRequestImpl)fdpRequest).setRequestString(new FDPUSSDRequestStringImpl(nodeText));
			/**Modified by EVASATY */
			//nodeText = nodeText.equals(((FDPRequestImpl)fdpRequest).getLastServedString())?nodeText:((FDPRequestImpl)fdpRequest).getLastServedString();
			FDPLogger.error(getCircleLogger(fdpRequest), getClass(), "getFDPOnFailureNode()",
					LoggerUtil.getRequestAppender(fdpRequest) + "Preparing response for nodeText:"+nodeText,exception);
			
			if(nodeText != null ) {
				FDPNode fdpNode = DynamicMenuUtil.getAndUpdateFDPNode(fdpRequest.getCircle(),
						nodeText, fdpRequest);
				if (fdpNode != null	&& (!Visibility.VISIBLE_FOR_MENU.equals(fdpNode.getVisibility()) && !Visibility.VISIBLE_FOR_DIRECT.equals(fdpNode
						.getVisibility()))) {
					fdpNode = null;
				}
				if(null != fdpNode && !DynamicMenuUtil.isWhiteListedForActiveForTestDMNode(fdpRequest,fdpNode)) {
					FDPLogger.debug(getCircleLogger(fdpRequest), getClass(), "getFDPOnFailureNode()",
							LoggerUtil.getRequestAppender(fdpRequest) + "Only Whitelisted Msisdn are allowed for ACTIVE_FOR_TEST DM nodes.");
					fdpNode = null;
				}
				
				fdpResponse = getResponse(fdpRequest, fdpNode);
				LoggerUtil.generatePostLogsForUserBehaviour(fdpRequest, fdpNode, false);
				DynamicMenuUtil.updateValueInCache(fdpRequest, fdpNode);
				/*List<ResponseMessage> responseMessages = fdpResponse.getResponseString();
				responseMessages.remove(0);*/
				FDPLogger.debug(getCircleLogger(fdpRequest), getClass(), "getFDPOnFailureNode()",
						LoggerUtil.getRequestAppender(fdpRequest) + "The response formed is :- " + fdpResponse);
				fdpResponse = RequestUtil.addResponseDecorators(fdpResponse);
			}
		} catch (final Exception e) {
			FDPLogger.error(getCircleLogger(fdpRequest), getClass(), "getFDPOnFailureNode()",
					LoggerUtil.getRequestAppender(fdpRequest) + "Unable to prepare response for nodeText:"+nodeText,e);
			fdpResponse = getFDPGenericFailureNotification(fdpRequest, exception);
		}
		return fdpResponse;
	}
	
	/**
	 * This method will prepare the generic failure reponse.
	 * @param dynamicMenuRequest
	 * @param exception
	 * @return
	 */
	private FDPResponse getFDPGenericFailureNotification(final FDPSMPPRequest dynamicMenuRequest, final Exception exception) {
		FDPResponse fdpResponse;
		String text = (String)applicationConfigCache.getValue(new FDPAppBag(AppCacheSubStore.CONFIGURATION_MAP, FDPConstant.FDP_GENERIC_FAILURE_TEXT));
		text = (null ==text) ? "Invalid input, please try again" : text;
		fdpResponse = new FDPResponseImpl(Status.SUCCESS, true, ResponseUtil.createResponseMessageInList(
				dynamicMenuRequest.getChannel(), text,
				TLVOptions.SESSION_TERMINATE));
		FDPLogger.error(getCircleLogger(dynamicMenuRequest), getClass(), "getFDPGenericFailureNotification()",
				LoggerUtil.getRequestAppender(dynamicMenuRequest) + "The response formed when error occured is :- " + fdpResponse,exception);
		return fdpResponse;
	}
	
	/**
	 * This method will put the request to queue and send the default MM notification to subscriber 
	 * @param dynamicMenuRequest
	 * @return
	 */
	private FDPResponse getMobileMoneyAsynResponse(final FDPSMPPRequest dynamicMenuRequest, final FDPServiceProvisioningNode serviceProvisioningNode, 
			ServiceProvisioning serviceProvisioning) {
		FDPResponse fdpResponse;
		String notificationText;
		Long delay = null;
		circleLogger = getCircleLogger(dynamicMenuRequest);
		try {
			notificationText = TariffEnquiryNotificationUtil.createNotificationText(dynamicMenuRequest, 
					CommandUtil.getNotificationIdAsycCommand(dynamicMenuRequest, Command.MM_DEBIT.getCommandDisplayName()), circleLogger);
			delay = Long.valueOf(RequestUtil.getConfigurationKeyValue(dynamicMenuRequest, ConfigurationKey.MM_DEBIT_PROCESSING_DELAY));
		} catch (NotificationFailedException | ExecutionFailedException e) {
			FDPLogger.error(circleLogger, getClass(), "getMobileMoneyAsynResponse()", LoggerUtil.getRequestAppender(dynamicMenuRequest) + e.getMessage());
			notificationText = "Error occurs while processing the reuqquest";
		}
		
		fdpResponse = new FDPResponseImpl(Status.SUCCESS, true, ResponseUtil.createResponseMessageInList(dynamicMenuRequest.getChannel(), notificationText,
				TLVOptions.SESSION_TERMINATE));
		
		fdpMetaCacheProducer.pushToQueue(new UpdateRequestDTO(dynamicMenuRequest, serviceProvisioningNode, serviceProvisioning), mmDebitQueue, delay);
		
		return fdpResponse;
	}
}