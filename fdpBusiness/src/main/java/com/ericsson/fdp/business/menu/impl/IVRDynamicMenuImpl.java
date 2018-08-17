package com.ericsson.fdp.business.menu.impl;

import java.util.List;
import java.util.Map;

import javax.annotation.Resource;
import javax.ejb.Stateless;
import javax.inject.Inject;

import org.apache.camel.Exchange;
import org.apache.camel.Message;
import com.ericsson.fdp.common.enums.ProductType;
import com.ericsson.fdp.FDPCacheable;
import com.ericsson.fdp.business.dto.Me2uProductDTO;
import com.ericsson.fdp.business.enums.AuxRequestParam;
import com.ericsson.fdp.business.enums.Command;
import com.ericsson.fdp.business.enums.ConfigurationKey;
import com.ericsson.fdp.business.enums.RequestMetaValuesKey;
import com.ericsson.fdp.business.enums.TLVOptions;
import com.ericsson.fdp.business.enums.ivr.FulfillmentActionServiceProvMappingEnum;
import com.ericsson.fdp.business.enums.ivr.FulfillmentActionTypes;
import com.ericsson.fdp.business.enums.ivr.FulfillmentParameters;
import com.ericsson.fdp.business.enums.ivr.FulfillmentResponseCodes;
import com.ericsson.fdp.business.enums.ivr.FulfillmentResponseTypes;
import com.ericsson.fdp.business.enums.ivr.FulfillmentSystemTypes;
import com.ericsson.fdp.business.enums.ivr.commandservice.IVRCommandEnum;
import com.ericsson.fdp.business.ivr.responsegenerator.IVRResponseGenerator;
import com.ericsson.fdp.business.node.FDPServiceProvisioningNode;
import com.ericsson.fdp.business.node.impl.ProductNode;
import com.ericsson.fdp.business.product.Product;
import com.ericsson.fdp.business.product.impl.BaseProduct;
import com.ericsson.fdp.business.request.impl.FDPMetadataResponseImpl;
import com.ericsson.fdp.business.request.impl.FulfillmentRequestImpl;
import com.ericsson.fdp.business.route.processor.FulfillmentServiceRequestProcessor;
import com.ericsson.fdp.business.serviceprovisioning.ServiceProvisioning;
import com.ericsson.fdp.business.util.CommandUtil;
import com.ericsson.fdp.business.util.DynamicMenuUtil;
import com.ericsson.fdp.business.util.FnfUtil;
import com.ericsson.fdp.business.util.FulfillmentUtil;
import com.ericsson.fdp.business.util.LoggerUtil;
import com.ericsson.fdp.business.util.Me2uUtil;
import com.ericsson.fdp.business.util.RequestUtil;
import com.ericsson.fdp.business.util.ServiceProvisioningUtil;
import com.ericsson.fdp.common.constants.FDPConstant;
import com.ericsson.fdp.common.enums.ChannelType;
import com.ericsson.fdp.common.enums.ModuleType;
import com.ericsson.fdp.common.enums.Status;
import com.ericsson.fdp.common.enums.Visibility;
import com.ericsson.fdp.common.util.StringUtil;
import com.ericsson.fdp.common.vo.FDPCircle;
import com.ericsson.fdp.core.command.FDPCommand;
import com.ericsson.fdp.core.config.utils.PropertyUtils;
import com.ericsson.fdp.core.dsm.framework.service.FDPCache;
import com.ericsson.fdp.core.exception.EvaluationFailedException;
import com.ericsson.fdp.core.exception.ExecutionFailedException;
import com.ericsson.fdp.core.logging.FDPLogger;
import com.ericsson.fdp.core.node.FDPNode;
import com.ericsson.fdp.core.request.FDPRequest;
import com.ericsson.fdp.core.request.FDPResponse;
import com.ericsson.fdp.core.request.ResponseError;
import com.ericsson.fdp.core.request.impl.FDPRequestImpl;
import com.ericsson.fdp.core.serviceprovisioning.bean.FDPAppBag;
import com.ericsson.fdp.core.serviceprovisioning.bean.FDPMetaBag;
import com.ericsson.fdp.core.utils.ApplicationConfigUtil;
import com.ericsson.fdp.core.utils.CircleCodeFinder;

/*
 Feature Name: User can purchase bundle for self and others
 Changes: executeNode() method updated to validate dynamic menu constraints, for beneficiary msisdn.
 Date: 28-10-2015
 Singnum Id:ESIASAN
 */

/**
 * This class implements the dynamic menu.
 * 
 * @author Ericsson
 * 
 */
@Stateless
public class IVRDynamicMenuImpl extends DynamicMenuImpl {

	@Inject
	private IVRResponseGenerator ivrResponseGenerator;

	/** The fdp service provisioning. */
	@Resource(lookup = "java:app/fdpBusiness-1.0/FulfillmentServiceProvisioningImpl")
	private ServiceProvisioning fdpFulfillmentServiceProvisioning;
	
	@Resource(lookup = "java:app/fdpCoreServices-1.0/ApplicationConfigCache")
	private FDPCache<FDPAppBag, Object> applicationConfigCache;

	/**
	 * 1. Pre-paid Check 2. Based on input, find Dynamic Menu code mapping from
	 * ProductAlias cache. 3. Based on Dynamic Menu code from mapping found, get
	 * Node. 4. Node visible on Menu 5. Active for Test Check 6. Execute Node
	 * finally.
	 */
	public FDPResponse executeDynamicMenu(
			final FulfillmentRequestImpl dynamicMenuRequest, Message in,
			Exchange exchange) throws ExecutionFailedException {
		FDPLogger.debug(
				getCircleLogger(dynamicMenuRequest),
				getClass(),
				"executeDynamicMenu()",
				LoggerUtil.getRequestAppender(dynamicMenuRequest)
						+ "The request message is : "
						+ dynamicMenuRequest.getRequestString());
		LoggerUtil.generatePreLogsForUserBehaviour(dynamicMenuRequest);
		RequestUtil.updateRequestValues(dynamicMenuRequest);
		updateTransactionId(dynamicMenuRequest);
		RequestUtil.updateSIMLangaugeInRequest(dynamicMenuRequest,applicationConfigCache);
		FDPResponse fdpResponse = null;
		// note here dynamicMenuRequest.getRequestString() is the input we are
		// sending in the request
		final IVRCommandEnum ivrCommand = IVRCommandEnum
				.getIVRCommandEnum(dynamicMenuRequest.getRequestString());

		/*
		 * final String requestString = dynamicMenuRequest.getRequestString();
		 * if (null != requestString &&
		 * requestString.startsWith(FDPConstant.FAF_ADD_PREFIX)) { FnfUtil
		 * fnfUtilObj = new FnfUtil(); String fafMsisdn = (String)
		 * dynamicMenuRequest
		 * .getAuxiliaryRequestParameter(AuxRequestParam.FAF_NUMBER); Boolean
		 * addNewFafNo = false; try { addNewFafNo =
		 * fnfUtilObj.isAddFafNumberAllowed(dynamicMenuRequest, fafMsisdn);
		 * 
		 * if (!addNewFafNo) {
		 * 
		 * fdpResponse = maxFafNoReached(dynamicMenuRequest); responseCode =
		 * "1106"; responseErrorString = "you have added maximum faf no.";
		 * responseError = new ResponseError(responseCode, responseErrorString,
		 * null, systemType); fdpResponse = new
		 * FDPMetadataResponseImpl(Status.FAILURE, true, null, responseError);
		 * 
		 * fdpMetadataResponseImpl = (FDPMetadataResponseImpl) fdpResponse;
		 * fdpMetadataResponseImpl.setFulfillmentResponse(responseErrorString);
		 * FDPLogger.info(getCircleLogger(dynamicMenuRequest), getClass(),
		 * "executeDynamicMenu() ",
		 * LoggerUtil.getRequestAppender(dynamicMenuRequest) +
		 * "Executing add FAF max no limit reached"); return fdpResponse; } }
		 * catch (FafFailedException ex) { responseCode = "1106";
		 * responseErrorString =
		 * "You cannot add FAF no. Please try again later"; responseError = new
		 * ResponseError(responseCode, responseErrorString, null, systemType);
		 * fdpResponse = new FDPMetadataResponseImpl(Status.FAILURE, true, null,
		 * responseError); fdpMetadataResponseImpl = (FDPMetadataResponseImpl)
		 * fdpResponse;
		 * fdpMetadataResponseImpl.setFulfillmentResponse(responseErrorString);
		 * 
		 * FDPLogger.error(getCircleLogger(dynamicMenuRequest), getClass(),
		 * "executeDynamicMenu() ",
		 * LoggerUtil.getRequestAppender(dynamicMenuRequest) +
		 * "Exception in executing add FAF max no limit reached"); fdpResponse =
		 * maxFafNoReached(dynamicMenuRequest); return fdpResponse; }
		 * 
		 * 
		 * }
		 */if (null != ivrCommand) {

			// todo if ivr command equals to viewhistory

			if (ivrCommand.equals(IVRCommandEnum.VIEWHISTORY)) {
				FulfillmentServiceRequestProcessor fulfillmentrequestprocessor = new FulfillmentServiceRequestProcessor();
				// in.getHeader(FulfillmentParameters.ACTION.getValue(),
				// String.class)
				exchange.getIn().setHeader(
						FulfillmentParameters.ACTION.getValue(),
						IVRCommandEnum.VIEWHISTORY.getIvrName());
				try {
					fulfillmentrequestprocessor.process(exchange);
					exchange.getIn().setHeader("IVR_HISTORY_MARKER", "TRUE");
					return null;
				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				// FDPLogger.debug("Found Action:" + action);
				/*
				 * final FulfillmentActionTypes fulfillmentActionTypes =
				 * FulfillmentActionTypes
				 * .getFulfillmentActionTypesByName(ivrCommand.getIvrName()); if
				 * ((null == fulfillmentActionTypes) || (null ==
				 * fulfillmentActionTypes.getJndiLookupName())) { throw new
				 * ExecutionFailedException
				 * ("Not a valid action type in action request parameter"); }
				 * dynamicMenuRequest.setActionTypes(fulfillmentActionTypes);
				 * 
				 * updateRequestParameters(dynamicMenuRequest, in);
				 * 
				 * fdpResponse = executeFulFillmentService(dynamicMenuRequest,
				 * fulfillmentActionTypes.getJndiLookupName());
				 */
			} else {
				fdpResponse = getCachedCommandAndExecute(dynamicMenuRequest,
						ivrCommand);
			}
			//Log UserBehaviour Information
			LoggerUtil.generatePostLogsForUserBehaviour(dynamicMenuRequest, null, false);
			/*
			 * LOGGER.debug("Found Action:" + action); final
			 * FulfillmentActionTypes fulfillmentActionTypes =
			 * FulfillmentActionTypes
			 * .getFulfillmentActionTypesByName(ivrCommand
			 * .getname("viewhistory")); if ((null == fulfillmentActionTypes) ||
			 * (null == fulfillmentActionTypes.getJndiLookupName())) { throw new
			 * ExecutionFailedException
			 * ("Not a valid action type in action request parameter"); }
			 * fullfillmentRequestImpl.setActionTypes(fulfillmentActionTypes);
			 * updateRequestParameters(fullfillmentRequestImpl, in);
			 * updateRequestAuxParams(exchange, fullfillmentRequestImpl);
			 * fdpResponse = executeFulFillmentService(fullfillmentRequestImpl,
			 * fulfillmentActionTypes.getJndiLookupName());
			 */
		} else {
			fdpResponse = executeFulfillmentForDMNode(dynamicMenuRequest);
		}
		// Generate Xml Response using JAXB.
		String xmlResponse = ivrResponseGenerator.generateIVRResponse(
				dynamicMenuRequest, fdpResponse);
		FDPMetadataResponseImpl fdpMetadataResponseImpl = checkFDPMetadataResponseImplType(fdpResponse);
		decorateIVRResponse(dynamicMenuRequest, fdpMetadataResponseImpl,
				xmlResponse);
		return fdpResponse;
	}

	private void updateRequestParameters(
			FulfillmentRequestImpl dynamicMenuRequest, Message in) {
		dynamicMenuRequest.setChannel(ChannelType.USSD);
		dynamicMenuRequest.setConsumerMsisdn(getMsisdn(in));
		dynamicMenuRequest.setCommandInputParams(
				FulfillmentParameters.TRANSACTION_ID, in.getHeader(
						FulfillmentParameters.TRANSACTION_ID.getValue(),
						String.class));

	}

	protected String getMsisdn(final Message in) {
		String msisdn = in.getHeader(FulfillmentParameters.MSISDN.getValue(),
				String.class);
		if (msisdn != null
				&& msisdn.length() == Integer.parseInt(PropertyUtils
						.getProperty("fdp.msisdn.length"))) {
			msisdn = new StringBuilder(
					PropertyUtils.getProperty("COUNTRY_CODE")).append(msisdn)
					.toString();
		}
		return msisdn;
	}

	/**
	 * This method will execute the node.
	 * 
	 * @param dynamicMenuRequest
	 * @param fdpNode
	 * @return
	 * @throws ExecutionFailedException
	 */
	private FDPResponse executeNode(
			final FulfillmentRequestImpl dynamicMenuRequest,
			final FDPNode fdpNode) throws ExecutionFailedException {
		boolean nodeValue = false;
		boolean nodeValueOther = true;
		FDPResponse fdpResponse = null;

		final String requestString = dynamicMenuRequest.getRequestString();
		
		if(null == requestString)
			return fdpResponse; 
		
		if(requestString.contains(FDPConstant.NUMBER_RESERVATION_PREFIX)){
			if (null !=dynamicMenuRequest.getAuxiliaryRequestParameter(AuxRequestParam.NUMBER_RESERVATION_ALTERNATE_NUMBER)){
				String  alternateNumber = dynamicMenuRequest.getAuxiliaryRequestParameter(AuxRequestParam.NUMBER_RESERVATION_ALTERNATE_NUMBER).toString();
				 dynamicMenuRequest.putAuxiliaryRequestParameter(AuxRequestParam.NUMBER_RESERVATION_ALTERNATE_NUMBER, alternateNumber);
			}
			else
				return new FDPMetadataResponseImpl(Status.FAILURE, true, null, new ResponseError(FulfillmentResponseCodes.INVALID_MSISDN.getResponseCode().toString(),
						FulfillmentResponseCodes.INVALID_MSISDN.getDescription(), null, FulfillmentSystemTypes.CIS.getValue()));
		}
		
		// Validate transaction limits in case of time2share product
		// TODO : This code is a patch that needs to be properly taken care of
		if(requestString.contains(FDPConstant.TIME2SHARE)){
			String amtToTransfer = dynamicMenuRequest.getAuxiliaryRequestParameter(AuxRequestParam.ME2U_AMT_TO_TRANSFER).toString();
			boolean areTransLimitValid = true;
			try{
				areTransLimitValid = Me2uUtil.areTransAmtLimitsValid(dynamicMenuRequest, Long.parseLong(amtToTransfer));
			}catch(EvaluationFailedException ex){
				throw new ExecutionFailedException(ex.getMessage(), ex);
			}finally{
				if(!areTransLimitValid){
					fdpResponse = new FDPMetadataResponseImpl(Status.FAILURE, true, null, new ResponseError(FulfillmentResponseCodes.TIME2SHARE_TRANS_LIMITS_REACHED.getResponseCode().toString(),
							FulfillmentResponseCodes.TIME2SHARE_TRANS_LIMITS_REACHED.getDescription(), null, FulfillmentSystemTypes.CIS.getValue()));
					return fdpResponse;
				}
			}
		}
		
		if(requestString.contains(FDPConstant.DATA2Share) && requestString.contains(FDPConstant.ME2U_PREFIX)){
			Me2uProductDTO me2uProdDTO = (Me2uProductDTO) dynamicMenuRequest.getAuxiliaryRequestParameter(AuxRequestParam.SELECTED_Me2UDATA2SHARE_PRODUCT);
			String amtToTransfer = dynamicMenuRequest.getAuxiliaryRequestParameter(AuxRequestParam.ME2U_AMT_TO_TRANSFER).toString();
			Boolean dataAmtCheck = amtToTransfer.matches("[0-9]+");
			if(null == me2uProdDTO) {
				fdpResponse = new FDPMetadataResponseImpl(Status.FAILURE, true, null, new ResponseError(FulfillmentResponseCodes.SUBSCRIBER_NOT_ELIGIBLE.getResponseCode().toString(),
						FulfillmentResponseCodes.SUBSCRIBER_NOT_ELIGIBLE.getDescription(), null, FulfillmentSystemTypes.CIS.getValue()));
				return fdpResponse;
			}
			if(!dataAmtCheck) {
				fdpResponse = new FDPMetadataResponseImpl(Status.FAILURE, true, null, new ResponseError(FulfillmentResponseCodes.INVALID_DATA_AMOUNT.getResponseCode().toString(),
						FulfillmentResponseCodes.INVALID_DATA_AMOUNT.getDescription(), null, FulfillmentSystemTypes.CIS.getValue()));
				return fdpResponse;
			}
			if(Long.parseLong(amtToTransfer) > Long.parseLong(me2uProdDTO.getAvailableBalance())) {
				fdpResponse = new FDPMetadataResponseImpl(Status.FAILURE, true, null, new ResponseError(FulfillmentResponseCodes.DATA2SHARE_DATA_LIMIT_REACHED.getResponseCode().toString(),
						FulfillmentResponseCodes.DATA2SHARE_DATA_LIMIT_REACHED.getDescription(), null, FulfillmentSystemTypes.CIS.getValue()));
				return fdpResponse;
			}
			
			String benMSISDN = dynamicMenuRequest.getAuxiliaryRequestParameter(AuxRequestParam.ME2U_SUBSCRIBER_TO_CREDIT).toString();
			String incomingMSISDN = dynamicMenuRequest.getIncomingSubscriberNumber().toString();
			if(benMSISDN.equals(incomingMSISDN)) {
				fdpResponse = new FDPMetadataResponseImpl(Status.FAILURE, true, null, new ResponseError(FulfillmentResponseCodes.DATA2SHARE_SAME_SUBSCRIBER.getResponseCode().toString(),
						FulfillmentResponseCodes.DATA2SHARE_SAME_SUBSCRIBER.getDescription(), null, FulfillmentSystemTypes.CIS.getValue()));
				return fdpResponse;
			}
			
			if(!isBeneficiaryMsisdnValid(dynamicMenuRequest, benMSISDN)){
				fdpResponse = new FDPMetadataResponseImpl(Status.FAILURE, true, null, new ResponseError(FulfillmentResponseCodes.INVALID_MSISDN.getResponseCode().toString(),
						FulfillmentResponseCodes.INVALID_MSISDN.getDescription(), null, FulfillmentSystemTypes.CIS.getValue()));
				return fdpResponse;
			}
			
			Product product = (Product) me2uProdDTO.getProduct();
			BaseProduct baseProduct = (BaseProduct) product;
			if(null != baseProduct.getCharges()) {
				if(Long.parseLong(baseProduct.getCharges()) > Long.parseLong(me2uProdDTO.getAccountValue())) {
					fdpResponse = new FDPMetadataResponseImpl(Status.FAILURE, true, null, new ResponseError(FulfillmentResponseCodes.LOW_BALANCE.getResponseCode().toString(),
							FulfillmentResponseCodes.LOW_BALANCE.getDescription(), null, FulfillmentSystemTypes.CIS.getValue()));
					return fdpResponse;
				}
			}
		}
				
		// Check Is user register for FAF service.
		// Check Added if configuration is done for FAF or Magic 
				final Map<String, String> configurationMap = dynamicMenuRequest.getCircle().getConfigurationKeyValueMap();
				final String validConfigurationForFafOrMagicNumber = configurationMap
						.get(ConfigurationKey.TYPE_FAF_OR_MAGIC_NUMBER.getAttributeName());
				
				
				//Magic No request configuration check added by EAGARSH
				if(null != requestString &&  requestString.startsWith(FDPConstant.FAF_ADD_PREFIX) && (!StringUtil.isNullOrEmpty(validConfigurationForFafOrMagicNumber) && validConfigurationForFafOrMagicNumber.equalsIgnoreCase(ProductType.MAGIC_NUMBER.getName()))){
					Map<Integer, String> fafListResponseMap =  FnfUtil.getFriendsAndFamilyList(dynamicMenuRequest);
					if(!fafListResponseMap.isEmpty()){
						// If magic no is already added.
						return duplicateMsisdnAndMaxFafResponse(
									dynamicMenuRequest,
									FulfillmentResponseCodes.MAGIC_NO_ALREADY_ADDED);
					}
				
						String fafMsisdnAddInput = dynamicMenuRequest.getAuxiliaryRequestParameter(AuxRequestParam.FAF_MSISDN_ADD).toString();
						//Check for Same MSISDN validation.
						Boolean sameMsisdnValidation;
						if(fafMsisdnAddInput.startsWith(FDPConstant.FAF_INTERNATIONAL_PREFIX)){
							sameMsisdnValidation = dynamicMenuRequest.getSubscriberNumber().toString().endsWith(fafMsisdnAddInput.substring(2));
						}else{
							sameMsisdnValidation = dynamicMenuRequest.getSubscriberNumber().toString().endsWith(fafMsisdnAddInput);
						}
						
						
						
						if(!sameMsisdnValidation){
							if(!FnfUtil.validateMagicMsisdn(fafMsisdnAddInput,dynamicMenuRequest)){
								//Enter Magic Number format is incorrect
								return duplicateMsisdnAndMaxFafResponse(
										dynamicMenuRequest,
										FulfillmentResponseCodes.INVALID_ENTER_MAGIC_NO);
							}
								//Call GAD command for friend No
								final boolean bolGadResponse = FnfUtil.validationAndexecutionForMagicNumber(dynamicMenuRequest);
								if(!bolGadResponse){
									// If FAFAdd msisdn is not Onnet
									return duplicateMsisdnAndMaxFafResponse(
											dynamicMenuRequest,
											FulfillmentResponseCodes.MAGIC_NO_NOT_ONNET);
									
								}
						}else{
							//Response for If Subscriber number and Magic Number are same.
							return duplicateMsisdnAndMaxFafResponse(
									dynamicMenuRequest,
									FulfillmentResponseCodes.SAME_MAGICNO_MSISDN);
							
						}
					
				}
			
		if ((requestString.startsWith(FDPConstant.FAF_ADD_PREFIX)
						|| requestString
								.startsWith(FDPConstant.FAF_MODIFY_PREFIX)
						|| requestString
								.startsWith(FDPConstant.FAF_DELETE_PREFIX)
						|| requestString
								.startsWith(FDPConstant.FAF_REGISTER_PREFIX)
						|| requestString
								.startsWith(FDPConstant.FAF_VIEW_PREFIX) || requestString
							.startsWith(FDPConstant.FAF_UNREGISTER_PREFIX)) && (!StringUtil.isNullOrEmpty(validConfigurationForFafOrMagicNumber) && validConfigurationForFafOrMagicNumber.equalsIgnoreCase(ProductType.FAF_NUMBER.getName()))) {
		
			Integer bolIsAlreadyRegister;
			if (requestString.startsWith(FDPConstant.FAF_REGISTER_PREFIX)) {
				bolIsAlreadyRegister = FnfUtil
						.IsSubscriberAlreadyRegisterForFaf(dynamicMenuRequest);
				if(bolIsAlreadyRegister == FDPConstant.FAF_MSISDN_IS_NOT_REGISTER){
					//check offerID is configured for FAF register.
					Integer offerID = FnfUtil.getFafOfferIdForUpdateOffer(dynamicMenuRequest);
					if(offerID == 0){
						fdpResponse = duplicateMsisdnAndMaxFafResponse(
								dynamicMenuRequest,
								FulfillmentResponseCodes.FAF_MSISDN_BASE_OFFER_NOT_FOUND);
						return fdpResponse;
					}
				}
				if (bolIsAlreadyRegister == FDPConstant.FAF_MSISDN_ALREADY_REGISTER) {
					fdpResponse = duplicateMsisdnAndMaxFafResponse(
							dynamicMenuRequest,
							FulfillmentResponseCodes.FAF_MSISDN_ALREADY_REGISTER);
					return fdpResponse;
				}
				if (bolIsAlreadyRegister == FDPConstant.SERVICE_CLASS_IS_NOT_ELIGIBLE  ) {
					fdpResponse = duplicateMsisdnAndMaxFafResponse(
							dynamicMenuRequest,
							FulfillmentResponseCodes.SERVICE_CLASS_IS_NOT_ELIGIBLE);
					return fdpResponse;
				}
				if (bolIsAlreadyRegister == FDPConstant.FAF_SC_BO_NOT_FOUND || bolIsAlreadyRegister == FDPConstant.FAF_MSISDN_BASE_OFFER_NOT_FOUND) {
					fdpResponse = duplicateMsisdnAndMaxFafResponse(
							dynamicMenuRequest,
							FulfillmentResponseCodes.FAF_MSISDN_BASE_OFFER_NOT_FOUND);
					return fdpResponse;
				}
			} else if (requestString
					.startsWith(FDPConstant.FAF_UNREGISTER_PREFIX)) {
				bolIsAlreadyRegister = FnfUtil
						.IsSubscriberAlreadyRegisterForFaf(dynamicMenuRequest);
				if (FDPConstant.FAF_MSISDN_IS_NOT_REGISTER == bolIsAlreadyRegister || FDPConstant.SERVICE_CLASS_IS_NOT_ELIGIBLE == bolIsAlreadyRegister || 
						 FDPConstant.FAF_SC_BO_NOT_FOUND == bolIsAlreadyRegister || FDPConstant.FAF_MSISDN_BASE_OFFER_NOT_FOUND == bolIsAlreadyRegister) {
					fdpResponse = duplicateMsisdnAndMaxFafResponse(dynamicMenuRequest,FulfillmentResponseCodes.FAF_MSISDN_IS_NOT_REGISTER);
					return fdpResponse;
				}
			}else {
				bolIsAlreadyRegister = FnfUtil
						.IsSubscriberAlreadyRegisterForFaf(dynamicMenuRequest);
				if (FDPConstant.FAF_MSISDN_IS_NOT_REGISTER == bolIsAlreadyRegister || FDPConstant.SERVICE_CLASS_IS_NOT_ELIGIBLE == bolIsAlreadyRegister || 
						 FDPConstant.FAF_SC_BO_NOT_FOUND == bolIsAlreadyRegister || FDPConstant.FAF_MSISDN_BASE_OFFER_NOT_FOUND == bolIsAlreadyRegister) {
					fdpResponse = duplicateMsisdnAndMaxFafResponse(dynamicMenuRequest,FulfillmentResponseCodes.FAF_MSISDN_IS_NOT_ELIGIBLE);
					return fdpResponse;
				}
			}

		}

		if ((requestString.startsWith(FDPConstant.FAF_ADD_PREFIX)
						|| requestString
								.startsWith(FDPConstant.FAF_MODIFY_PREFIX) || requestString
							.startsWith(FDPConstant.FAF_DELETE_PREFIX)) && (null != validConfigurationForFafOrMagicNumber && !validConfigurationForFafOrMagicNumber.isEmpty() && validConfigurationForFafOrMagicNumber.equalsIgnoreCase(ProductType.FAF_NUMBER.getName()))) {
			fdpResponse = sameMsisdnCheck(dynamicMenuRequest, requestString);
			if (null != fdpResponse) {
				return fdpResponse;
			}
			// If subscriber enter same msisdn for updateFaf request.

			Integer isMaxAllowed = 0;
			// Check for is FAF reach maximum for ADD, UPDATE
			FnfUtil fnfUtilObj = new FnfUtil();
			if (requestString.startsWith(FDPConstant.FAF_ADD_PREFIX) && (null != validConfigurationForFafOrMagicNumber && !validConfigurationForFafOrMagicNumber.isEmpty() && validConfigurationForFafOrMagicNumber.equalsIgnoreCase(ProductType.FAF_NUMBER.getName()))) {
				if (fdpNode != null) {
					try {
						nodeValue = RequestUtil.evaluateNodeValue(
								dynamicMenuRequest, fdpNode);
						final FDPServiceProvisioningNode serviceProvisioningNode = (FDPServiceProvisioningNode) fdpNode;
						FDPCacheable product = null;
						FDPCacheable serviceProvisioning = null;
						FDPCacheable[] cachedspandProduct = null;
						cachedspandProduct = ServiceProvisioningUtil
								.getProductAndSP(dynamicMenuRequest,
										serviceProvisioningNode);
						product = (Product) cachedspandProduct[0];
						serviceProvisioning = cachedspandProduct[1];
						RequestUtil.updateProductInRequest(dynamicMenuRequest,
								serviceProvisioning, product);
					} catch (EvaluationFailedException e) {
						FDPLogger
								.debug(getCircleLogger(dynamicMenuRequest),
										getClass(),
										"executeNode()",
										LoggerUtil
												.getRequestAppender(dynamicMenuRequest)
												+ "The request message is : "
												+ dynamicMenuRequest
														.getRequestString());
					}
					// need to remove code.

					/*
					 * fdpResponse = checkIsMaxAllowedToAdd(dynamicMenuRequest,
					 * fnfUtilObj); if (null != fdpResponse) { return
					 * fdpResponse; }
					 */
					isMaxAllowed = FnfUtil
							.isAddMoreFafNumberToAdd(dynamicMenuRequest);
					if (isMaxAllowed != FDPConstant.FAF_MAX_ADD_TRUE) {
						if(isMaxAllowed == FDPConstant.FAF_MAX_ADD_FAILURE){
							fdpResponse = duplicateMsisdnAndMaxFafResponse(
									dynamicMenuRequest,
									FulfillmentResponseCodes.FAF_MAX_ADD_FAILURE);
						}else if(isMaxAllowed == FDPConstant.FAF_MAX_ONNET_LIMIT_REACHED){
							fdpResponse = duplicateMsisdnAndMaxFafResponse(
									dynamicMenuRequest,
									FulfillmentResponseCodes.FAF_MSISDN_ONNET_LIMIT_REACHED);
						}else if(isMaxAllowed == FDPConstant.FAF_MAX_OFFNET_LIMIT_REACHED){
							fdpResponse = duplicateMsisdnAndMaxFafResponse(
									dynamicMenuRequest,
									FulfillmentResponseCodes.FAF_MSISDN_OFFNET_LIMIT_REACHED);
						}else if(isMaxAllowed == FDPConstant.FAF_MAX_INTERNATIONAL_LIMIT_REACHED){
							fdpResponse = duplicateMsisdnAndMaxFafResponse(
									dynamicMenuRequest,
									FulfillmentResponseCodes.FAF_MSISDN_INTERNATIONAL_LIMIT_REACHED);
						}
						
						return fdpResponse;
					}

				}
			}

			if (requestString.startsWith(FDPConstant.FAF_MODIFY_PREFIX)) {

				// Drop1
				/*
				 * fdpResponse = checkIsMaxAllowedToModify(dynamicMenuRequest,
				 * fnfUtilObj); if (null != fdpResponse) { return fdpResponse; }
				 */
				isMaxAllowed = FnfUtil
						.isAddMoreFafNumberToModify(dynamicMenuRequest);
				if (isMaxAllowed !=  FDPConstant.FAF_MAX_ADD_TRUE) {
					if(isMaxAllowed == FDPConstant.FAF_MAX_ADD_FAILURE){
						fdpResponse = duplicateMsisdnAndMaxFafResponse(
								dynamicMenuRequest,
								FulfillmentResponseCodes.FAF_MAX_ADD_FAILURE);
					}else if(isMaxAllowed == FDPConstant.FAF_MAX_ONNET_LIMIT_REACHED){
						fdpResponse = duplicateMsisdnAndMaxFafResponse(
								dynamicMenuRequest,
								FulfillmentResponseCodes.FAF_MSISDN_ONNET_LIMIT_REACHED);
					}else if(isMaxAllowed == FDPConstant.FAF_MAX_OFFNET_LIMIT_REACHED){
						fdpResponse = duplicateMsisdnAndMaxFafResponse(
								dynamicMenuRequest,
								FulfillmentResponseCodes.FAF_MSISDN_OFFNET_LIMIT_REACHED); 
					}else if(isMaxAllowed == FDPConstant.FAF_MAX_INTERNATIONAL_LIMIT_REACHED){
						fdpResponse = duplicateMsisdnAndMaxFafResponse(
								dynamicMenuRequest,
								FulfillmentResponseCodes.FAF_MSISDN_INTERNATIONAL_LIMIT_REACHED);
					}else if(isMaxAllowed == FDPConstant.FAF_MSISDN_DELETE_NOT_FOUND){
						fdpResponse = duplicateMsisdnAndMaxFafResponse(
								dynamicMenuRequest,
								FulfillmentResponseCodes.FAF_MSISDN_DELETE_NOT_FOUND);
					}else if(isMaxAllowed == FDPConstant.FAF_ADD_MSISDN_DETAILS_NOT_FOUND){
						fdpResponse = duplicateMsisdnAndMaxFafResponse(
								dynamicMenuRequest,
								FulfillmentResponseCodes.FAF_ADD_MSISDN_DETAILS_NOT_FOUND);
					}else if(isMaxAllowed == FDPConstant.FAF_LIST_IS_EMPTY){
						fdpResponse = duplicateMsisdnAndMaxFafResponse(
								dynamicMenuRequest,
								FulfillmentResponseCodes.FAF_LIST_IS_EMPTY);
					}else if(isMaxAllowed == FDPConstant.FAF_SC_BO_NOT_FOUND){
						fdpResponse = duplicateMsisdnAndMaxFafResponse(
								dynamicMenuRequest,
								FulfillmentResponseCodes.FAF_MSISDN_BASE_OFFER_NOT_FOUND);
					}
					
					return fdpResponse;
				}
			} if (requestString.startsWith(FDPConstant.FAF_DELETE_PREFIX)) {
				String fafMsisdnDelete = dynamicMenuRequest.getAuxiliaryRequestParameter(AuxRequestParam.FAF_MSISDN_ADD).toString();
				if(null != fafMsisdnDelete && fafMsisdnDelete.startsWith(FDPConstant.FAF_INTERNATIONAL_PREFIX)){
					dynamicMenuRequest.putAuxiliaryRequestParameter(AuxRequestParam.FAF_MSISDN_DELETE_INTERNATIONAL,"1");
				}
			}
		}

		if (fdpNode == null) {
			fdpResponse = handleNodeNotFound(dynamicMenuRequest);
		} else {
			try {
				nodeValue = RequestUtil.evaluateNodeValue(dynamicMenuRequest,
						fdpNode);
				// updated node in fdp request
				RequestUtil.updateMetaValuesInRequest(dynamicMenuRequest,
						RequestMetaValuesKey.NODE, fdpNode);
				Object beneficiaryMsisdnObject = dynamicMenuRequest
						.getAuxiliaryRequestParameter(AuxRequestParam.BENEFICIARY_MSISDN);
				if (null != beneficiaryMsisdnObject) {
					nodeValueOther = RequestUtil
							.isProductBuyValidForBeneficiaryMsisdn(
									(FDPRequest) dynamicMenuRequest,
									beneficiaryMsisdnObject.toString());
				}
				if (nodeValue && nodeValueOther) {

					if (fdpNode instanceof FDPServiceProvisioningNode) {
						FDPLogger
								.debug(getCircleLogger(dynamicMenuRequest),
										getClass(),
										"executeNode()",
										LoggerUtil
												.getRequestAppender(dynamicMenuRequest)
												+ "Executing Service provisioning node");
						final FDPServiceProvisioningNode serviceProvisioningNode = (FDPServiceProvisioningNode) fdpNode;
						// Code commented to skip policy execution..
						/*
						 * fdpResponse =
						 * serviceProvisioningNode.executePolicy(dynamicMenuRequest
						 * ); if (fdpResponse == null && !(fdpNode.getPolicy()
						 * instanceof FDPExecutesServiceProv)) {
						 * RequestUtil.putPolicyRuleValuesInRequest
						 * (dynamicMenuRequest, serviceProvisioningNode);
						 * fdpResponse =
						 * ServiceProvisioningUtil.executeServiceProvisioning
						 * (serviceProvisioningNode, dynamicMenuRequest,
						 * fdpFulfillmentServiceProvisioning); } else if
						 * (dynamicMenuRequest instanceof FDPRequestImpl &&
						 * fdpNode.getPolicy().isPolicyExecution()) {
						 * ((FDPRequestImpl)
						 * dynamicMenuRequest).setPolicyExecution(true); }
						 */
						fdpResponse = ServiceProvisioningUtil
								.executeServiceProvisioning(
										serviceProvisioningNode,
										dynamicMenuRequest,
										fdpFulfillmentServiceProvisioning);
						updateRequestWithFulFillmentResponseType(
								dynamicMenuRequest, serviceProvisioningNode);
					} else {
						throw new ExecutionFailedException(
								"Found node is not a product node.");
					}
				} else {
					throw new ExecutionFailedException(
							"Node evaluation failed for node");
				}
			} catch (final EvaluationFailedException e) {
				FDPLogger.error(getCircleLogger(dynamicMenuRequest),
						getClass(), "executeNode()",
						LoggerUtil.getRequestAppender(dynamicMenuRequest)
								+ "The dynamic menu could not be displayed", e);
				throw new ExecutionFailedException(
						"The dynamic menu could not be displayed", e);
			}
		}
		return fdpResponse;
	}

	/**
	 * The method is for checking maximum limit for onnet, offnet, international
	 * when user come to modify FAF.
	 * 
	 * @param dynamicMenuRequest
	 * @param fnfUtilObj
	 * @return
	 * @throws ExecutionFailedException
	 */
	/*private FDPResponse checkIsMaxAllowedToModify(
			FulfillmentRequestImpl dynamicMenuRequest, FnfUtil fnfUtilObj)
			throws ExecutionFailedException {
		FDPResponse fdpResponse = null;
		String fafMsisdn = (String) dynamicMenuRequest
				.getAuxiliaryRequestParameter(AuxRequestParam.FAF_NUMBER);
		String fafMsisdnDelete = (String) dynamicMenuRequest
				.getAuxiliaryRequestParameter(AuxRequestParam.FAF_MSISDN_DELETE);
		try {
			Boolean modifyAllowed = fnfUtilObj.isFafModifyAllowed(
					dynamicMenuRequest, fafMsisdn, fafMsisdnDelete);
			if (!modifyAllowed) {
				fdpResponse = duplicateMsisdnAndMaxFafResponse(
						dynamicMenuRequest,
						FulfillmentResponseCodes.FAF_MSISDN_MAX_ADDED);
				FDPLogger.info(getCircleLogger(dynamicMenuRequest), getClass(),
						"executeDynamicMenu() ",
						LoggerUtil.getRequestAppender(dynamicMenuRequest)
								+ "Executing Modify FAF max no limit reached");
			}
		} catch (FafFailedException e) {
			FDPLogger
					.error(getCircleLogger(dynamicMenuRequest),
							getClass(),
							"executeDynamicMenu() ",
							LoggerUtil.getRequestAppender(dynamicMenuRequest)
									+ "Exception in executing modify FAF max no limit reached");
			fdpResponse = duplicateMsisdnAndMaxFafResponse(dynamicMenuRequest,
					FulfillmentResponseCodes.FAF_MSISDN_MAX_ADDED);
			return fdpResponse;
		}
		return fdpResponse;
	}

*/	/**
	 * The method is for checking maximum limit for onnet, offnet, international
	 * when user come to add FAF
	 * 
	 * @param dynamicMenuRequest
	 * @param fnfUtilObj
	 * @return
	 * @throws ExecutionFailedException
	 */
	/*private FDPResponse checkIsMaxAllowedToAdd(
			FulfillmentRequestImpl dynamicMenuRequest, FnfUtil fnfUtilObj)
			throws ExecutionFailedException {
		FDPResponse fdpResponse = null;

		String fafMsisdn = (String) dynamicMenuRequest
				.getAuxiliaryRequestParameter(AuxRequestParam.FAF_NUMBER);
		Boolean addNewFafNo = false;

		try {
			addNewFafNo = fnfUtilObj.isAddFafNumberAllowed(dynamicMenuRequest,
					fafMsisdn);
			if (!addNewFafNo) {
				fdpResponse = duplicateMsisdnAndMaxFafResponse(
						dynamicMenuRequest,
						FulfillmentResponseCodes.FAF_MSISDN_MAX_ADDED);
				FDPLogger.info(getCircleLogger(dynamicMenuRequest), getClass(),
						"executeDynamicMenu() ",
						LoggerUtil.getRequestAppender(dynamicMenuRequest)
								+ "Executing add FAF max no limit reached");
				return fdpResponse;
			}
		} catch (FafFailedException ex) {
			FDPLogger
					.error(getCircleLogger(dynamicMenuRequest),
							getClass(),
							"executeDynamicMenu() ",
							LoggerUtil.getRequestAppender(dynamicMenuRequest)
									+ "Exception in executing add FAF max no limit reached");
			fdpResponse = duplicateMsisdnAndMaxFafResponse(dynamicMenuRequest,
					FulfillmentResponseCodes.FAF_MSISDN_MAX_ADDED);
			return fdpResponse;
		}
		return fdpResponse;
	}
*/
	/**
	 * The method is for subscriber number and FAF number are not same.
	 * 
	 * @param dynamicMenuRequest
	 * @param requestString
	 * @return
	 */
	private FDPResponse sameMsisdnCheck(
			FulfillmentRequestImpl dynamicMenuRequest, String requestString) {
		FDPResponse fdpResponse = null;
		String fafMsisdn = (String) dynamicMenuRequest
				.getAuxiliaryRequestParameter(AuxRequestParam.FAF_NUMBER);
		final Map<String, String> configurationMap = dynamicMenuRequest
				.getCircle().getConfigurationKeyValueMap();
		String isSameFafMsisdn = configurationMap.get(
				ConfigurationKey.FAF_SAME_MSISDN_CHECK.getAttributeName())
				.toString();
		if (!StringUtil.isNullOrEmpty(isSameFafMsisdn)
				&& FDPConstant.FAF_SAME_MSISDN_FLAG
						.equalsIgnoreCase(isSameFafMsisdn)) {
			Boolean isSameMsisdnForFaf = FnfUtil.isBothMsisdnSame(
					dynamicMenuRequest.getSubscriberNumber().toString(),
					fafMsisdn);
			if (isSameMsisdnForFaf) {
				FDPLogger.info(getCircleLogger(dynamicMenuRequest), getClass(),
						"executeDynamicMenu() ",
						LoggerUtil.getRequestAppender(dynamicMenuRequest)
								+ "Subscriber enter same msisndn for FAF "
								+ requestString);
				fdpResponse = duplicateMsisdnAndMaxFafResponse(
						dynamicMenuRequest,
						FulfillmentResponseCodes.FAF_DUPLICATE_MSISDN);
				return fdpResponse;
			}
		}

		return fdpResponse;

	}

	/**
	 * This method will set the response type.
	 * 
	 * @param fdpMetadataResponseImpl
	 * @param fdpServiceProvisioningNode
	 * @throws ExecutionFailedException
	 */
	private void updateRequestWithFulFillmentResponseType(
			final FulfillmentRequestImpl fulfillmentRequestImpl,
			final FDPServiceProvisioningNode fdpServiceProvisioningNode)
			throws ExecutionFailedException {
		if (fdpServiceProvisioningNode instanceof ProductNode) {
			fulfillmentRequestImpl
					.setResponseTypes(FulfillmentResponseTypes.PRODUCT);
			ProductNode productNode = (ProductNode) fdpServiceProvisioningNode;
			FulfillmentActionTypes fulfillmentActionTypes = FulfillmentActionServiceProvMappingEnum
					.getFulfillmentActionTypes(productNode
							.getServiceProvSubType());
			if (null == fulfillmentActionTypes) {
				throw new ExecutionFailedException(
						"Node action Type not found.");
			}
			fulfillmentRequestImpl.setActionTypes(fulfillmentActionTypes);
		} else {
			throw new ExecutionFailedException("Operation not supported");
		}
	}

	/**
	 * This method will prepare the request string as
	 * "IVR"+"SPACE"+actualRequestString.
	 * 
	 * @param fdpRequest
	 * @return
	 * @throws ExecutionFailedException
	 */
	private String getIVRRequestString(final FulfillmentRequestImpl fdpRequest)
			throws ExecutionFailedException {

		return fdpRequest.getIname() + FDPConstant.SPACE
				+ fdpRequest.getRequestString();
	}

	/**
	 * This is the method for casting response into meta response, copied from
	 * request.
	 * 
	 * @param fdpResponse
	 * @return
	 * @throws ExecutionFailedException
	 */
	private FDPMetadataResponseImpl checkFDPMetadataResponseImplType(
			final FDPResponse fdpResponse) throws ExecutionFailedException {
		FDPMetadataResponseImpl fdpMetadataResponseImpl = null;
		if (fdpResponse instanceof FDPMetadataResponseImpl) {
			fdpMetadataResponseImpl = (FDPMetadataResponseImpl) fdpResponse;
		}

		if (null == fdpMetadataResponseImpl) {
			throw new ExecutionFailedException(
					"Error while casting fdpResponse into FDPMetadataResponseImpl");
		}
		return fdpMetadataResponseImpl;
	}

	/**
	 * This method will decorate the response object.
	 * 
	 * @param fdpMetadataResponseImpl
	 * @param fulfillmentResponse
	 */
	private void decorateIVRResponse(
			final FulfillmentRequestImpl fulfillmentRequestImpl,
			final FDPMetadataResponseImpl fdpMetadataResponseImpl,
			final String fulfillmentResponse) {
		if (fdpMetadataResponseImpl.getResponseString() != null
				&& !fdpMetadataResponseImpl.getResponseString().isEmpty()
				&& fdpMetadataResponseImpl
						.getResponseString()
						.get(fdpMetadataResponseImpl.getResponseString().size() - 1)
						.getTLVOptions().contains(TLVOptions.SESSION_TERMINATE)) {
			fdpMetadataResponseImpl.setTerminateSession(true);
		}
		if (!StringUtil.isNullOrEmpty(fulfillmentResponse)) {
			fdpMetadataResponseImpl.setFulfillmentResponse(fulfillmentResponse);
			fdpMetadataResponseImpl.setTerminateSession(true);
		}

		if (FulfillmentActionTypes.GET_COMMANDS.equals(fulfillmentRequestImpl
				.getActionTypes())) {
			String respone = fdpMetadataResponseImpl.getFulfillmentResponse();
			respone = respone.replaceAll("&lt;", "<").replaceAll("&gt;", ">");
			fdpMetadataResponseImpl.setFulfillmentResponse(respone);
		}
	}

	/**
	 * This method will fetch command from cache and execute the same.
	 * 
	 * @param fdpRequest
	 * @param ivrCommandEnum
	 * @throws ExecutionFailedException
	 */
	private FDPResponse getCachedCommandAndExecute(
			final FulfillmentRequestImpl fdpRequest,
			final IVRCommandEnum ivrCommandEnum)
			throws ExecutionFailedException {
		FDPMetadataResponseImpl fdpResponse = new FDPMetadataResponseImpl(
				Status.FAILURE, true, null);
		fdpRequest.setIvrCommandEnum(ivrCommandEnum);
		fdpRequest.setActionTypes(FulfillmentActionTypes.GET_COMMANDS);
		fdpRequest.setResponseTypes(FulfillmentResponseTypes.OTHERS);
		final FDPCacheable cachedCommand = ApplicationConfigUtil
				.getMetaDataCache().getValue(
						new FDPMetaBag(fdpRequest.getCircle(),
								ModuleType.COMMAND, ivrCommandEnum.getCommand()
										.getCommandDisplayName()));
		if (cachedCommand != null && cachedCommand instanceof FDPCommand) {
			FDPCommand fdpCommand = (FDPCommand) cachedCommand;
			fdpCommand = CommandUtil.getExectuableFDPCommand(fdpCommand);
			FulfillmentUtil.preCommandExecutor(fdpRequest, ivrCommandEnum,
					fdpCommand, getCircleLogger(fdpRequest));
			final Status status = fdpCommand.execute(fdpRequest);
			fdpRequest.addExecutedCommand(fdpCommand);
			if (Status.SUCCESS.equals(status)) {
				fdpResponse.setExecutionStatus(Status.SUCCESS);
			} else {
				fdpResponse.setResponseError(fdpCommand.getResponseError());
			}
		} else {
			FDPLogger.debug(
					getCircleLogger(fdpRequest),
					getClass(),
					"getCachedCommandAndExecute()",
					LoggerUtil.getRequestAppender(fdpRequest)
							+ ivrCommandEnum.getCommand()
							+ " Command Not Configured for Circle "
							+ fdpRequest.getCircle().getCircleCode());
		}
		return fdpResponse;
	}

	/**
	 * This method will perform operation to be done on DM nodes.
	 * 
	 * @param dynamicMenuRequest
	 * @return
	 * @throws ExecutionFailedException
	 */
	private FDPResponse executeFulfillmentForDMNode(
			final FulfillmentRequestImpl dynamicMenuRequest)
			throws ExecutionFailedException {
		FDPResponse fdpResponse;
		FDPNode fdpNode = null;
		// boolean isPrepaidConsumer = false;
		String requestString = getIVRRequestString(dynamicMenuRequest);
		// Pre-paid Check
		// fdpResponse = checkConsumerIsPrePaid(dynamicMenuRequest);
		dynamicMenuRequest.setActionTypes(FulfillmentActionTypes.PRODUCT_BUY);
		dynamicMenuRequest.setResponseTypes(FulfillmentResponseTypes.PRODUCT);

		/*
		 * Feature Name: Allow postpaid and blackberry user to use CIS system
		 * Changes: Remove postpaid/prepaid validation Date: 27-oct-2015 Singnum
		 * Id: ECDGGIQ
		 */

		// finding Dynamic Menu Code mapping and DM node.
		// if (fdpResponse == null) {
		// isPrepaidConsumer = true;
		fdpNode = DynamicMenuUtil.getAndUpdateFDPNode(
				dynamicMenuRequest.getCircle(), requestString,
				dynamicMenuRequest);
		/*
		 * } else { FDPResponseImpl fdpResponseImpl = (FDPResponseImpl)
		 * fdpResponse; fdpResponseImpl.setExecutionStatus(Status.FAILURE);
		 * FDPMetadataResponseImpl fdpMetadataResponseImpl = new
		 * FDPMetadataResponseImpl(fdpResponseImpl.getExecutionStatus(),
		 * fdpResponseImpl.isTerminateSession(),
		 * fdpResponseImpl.getResponseString
		 * (),fdpResponseImpl.getResponseError()); fdpResponse =
		 * fdpMetadataResponseImpl; }
		 */
		// if (fdpResponse == null && isPrepaidConsumer) {
		// Check for HIDE_FROM_MENU
		if (fdpNode != null
				&& (!Visibility.VISIBLE_FOR_MENU
						.equals(fdpNode.getVisibility()) && !Visibility.VISIBLE_FOR_DIRECT
						.equals(fdpNode.getVisibility()))) {
			FDPLogger.debug(getCircleLogger(dynamicMenuRequest), getClass(),
					"executeFulfillmentForDMNode()",
					LoggerUtil.getRequestAppender(dynamicMenuRequest)
							+ "NODE not Visible " + fdpNode.getVisibility());
			fdpNode = null;
		}
		// Check for ACTIVE_FOR_TEST
		if (null != fdpNode
				&& !DynamicMenuUtil.isWhiteListedForActiveForTestDMNode(
						dynamicMenuRequest, fdpNode)) {
			FDPLogger
					.debug(getCircleLogger(dynamicMenuRequest),
							getClass(),
							"executeFulfillmentForDMNode()",
							LoggerUtil.getRequestAppender(dynamicMenuRequest)
									+ "Only Whitelisted Msisdn are allowed for ACTIVE_FOR_TEST DM nodes.");
			fdpNode = null;
		}

		// Call GAD Command for FAF and set FAFINDICATOR, ChargingIndicator
		/*
		 * if (dynamicMenuRequest.getRequestString().contains(FDPConstant.
		 * FAF_DELETE_PREFIX)) { executeGADCommandForFAF(dynamicMenuRequest); }
		 */
		fdpResponse = executeNode(dynamicMenuRequest, fdpNode);
		//Log UserBehaviour Information 
		LoggerUtil.generatePostLogsForUserBehaviour(dynamicMenuRequest, fdpNode, false);
		// }
		return fdpResponse;
	}

	/**
	 * This method will handle special response to be sent in case of when node
	 * not found on DM.
	 * 
	 * @param fulfillmentRequestImpl
	 * @return
	 * @throws ExecutionFailedException
	 */
	private FDPResponse handleNodeNotFound(
			final FulfillmentRequestImpl fulfillmentRequestImpl)
			throws ExecutionFailedException {
		FDPResponse fdpResponse = null;
		FulfillmentResponseCodes fulfillmentResponseCodes = FulfillmentResponseCodes.INVALID_PARAMETER;
		final String responseCode = String.valueOf(fulfillmentResponseCodes
				.getResponseCode());
		Object[] parameters = new Object[1];
		parameters[0] = fulfillmentRequestImpl.getRequestString();
		final String responseErrorString = getResponseDesciption(
				fulfillmentResponseCodes.getDescription(), parameters);
		final String systemType = FulfillmentSystemTypes.FDP.getValue();
		final ResponseError responseError = new ResponseError(responseCode,
				responseErrorString, null, systemType);
		fdpResponse = new FDPMetadataResponseImpl(Status.FAILURE, true, null,
				responseError);
		return fdpResponse;
	}

	private FDPResponse duplicateMsisdnAndMaxFafResponse(
			final FulfillmentRequestImpl fulfillmentRequestImpl,
			FulfillmentResponseCodes fulfillmentResponseCode) {
		FDPResponse fdpResponse = null;
		FulfillmentResponseCodes fulfillmentResponseCodes = fulfillmentResponseCode;
		final String responseCode = String.valueOf(fulfillmentResponseCodes
				.getResponseCode());
		Object[] parameters = new Object[1];
		parameters[0] = fulfillmentRequestImpl.getSubscriberNumber();
				//fulfillmentRequestImpl.getRequestString();
		final String responseErrorString = getResponseDesciption(
				fulfillmentResponseCodes.getDescription(), parameters);
		final String systemType = FulfillmentSystemTypes.CIS.getValue();
		final ResponseError responseError = new ResponseError(responseCode,
				responseErrorString, null, systemType);
		fdpResponse = new FDPMetadataResponseImpl(Status.FAILURE, true, null,
				responseError);
		return fdpResponse;
		/*
		 * responseCode = "1106"; responseErrorString =
		 * "you have added maximum faf no."; responseError = new
		 * ResponseError(responseCode, responseErrorString, null, systemType);
		 * fdpResponse = new FDPMetadataResponseImpl(Status.FAILURE, true, null,
		 * responseError);
		 * 
		 * fdpMetadataResponseImpl = (FDPMetadataResponseImpl) fdpResponse;
		 * fdpMetadataResponseImpl.setFulfillmentResponse(responseErrorString);
		 * FDPLogger.info(getCircleLogger(dynamicMenuRequest), getClass(),
		 * "executeDynamicMenu() ",
		 * LoggerUtil.getRequestAppender(dynamicMenuRequest) +
		 * "Executing add FAF max no limit reached"); return
		 * fdpMetadataResponseImpl;
		 */

	}

	/**
	 * This method will prepare the response description based on error code
	 * type.
	 * 
	 * @param description
	 * @param parameters
	 * @return
	 */
	private static String getResponseDesciption(final String description,
			final Object[] parameters) {
		return parameters.length > 0 ? String.format(description, parameters)
				: description;
	}
	
	/**
	 * This method will validate the input beneficiary number on AIR interface and do number series check
	 * @param beneficiaryMsisdn
	 * @return
	 * @throws ExecutionFailedException 
	 */
	private boolean isBeneficiaryMsisdnValid(FDPRequest fdpRequest, String beneficiaryMsisdn) throws ExecutionFailedException {
		Boolean isBeneficiaryMsisdnValid = false;

		try {
			FDPCircle benMsisdnCircle = CircleCodeFinder.getFDPCircleByMsisdn(beneficiaryMsisdn, ApplicationConfigUtil.getApplicationConfigCache());
			if(null != benMsisdnCircle && benMsisdnCircle.getCircleName().contentEquals(fdpRequest.getCircle().getCircleName())){
				((FDPRequestImpl)fdpRequest).putAuxiliaryRequestParameter(AuxRequestParam.BENEFICIARY_MSISDN, beneficiaryMsisdn);	
				ServiceProvisioningUtil.updateSubscriberInRequestForBeneficiary(fdpRequest, true, LoggerUtil.getSummaryLoggerFromRequest(fdpRequest));
				CommandUtil.executeCommand(fdpRequest, Command.GET_BALANCE_AND_DATE, true);
				ServiceProvisioningUtil.updateSubscriberInRequestForBeneficiary(fdpRequest, false, LoggerUtil.getSummaryLoggerFromRequest(fdpRequest));
				((FDPRequestImpl)fdpRequest).putAuxiliaryRequestParameter(AuxRequestParam.BENEFICIARY_MSISDN, null);
				FDPCommand fdpCommand = fdpRequest.getExecutedCommand(Command.GET_BALANCE_AND_DATE.getCommandDisplayName());
				if(!fdpCommand.getResponseError().getResponseCode().contentEquals("102")){
					isBeneficiaryMsisdnValid = true;
				}
			}else{
				FDPLogger.info(LoggerUtil.getSummaryLoggerFromRequest(fdpRequest), getClass(), "isBeneficiaryMsisdnValid", 
						"Request is invalid for beneficiary msisdn : " + beneficiaryMsisdn);

			}
		}
		catch(Exception e) {System.out.println("ERROR :: INVALID BEN MSISDN");}
		return isBeneficiaryMsisdnValid;
	}

	// Execute GAD command and set FAFIndicator
	/*
	 * private void executeGADCommandForFAF(FulfillmentRequestImpl
	 * dynamicMenuRequest) throws ExecutionFailedException {
	 * 
	 * String fafNumber = (String)
	 * dynamicMenuRequest.getAuxiliaryRequestParameter
	 * (AuxRequestParam.FAF_NUMBER); if (!StringUtil.isNullOrEmpty(fafNumber) &&
	 * fafNumber.length() > 0) { String fafIndicator = null; String
	 * fafChargingIndicator = null; final Map<String, String> configurationMap =
	 * dynamicMenuRequest.getCircle().getConfigurationKeyValueMap();
	 * fafChargingIndicator =
	 * configurationMap.get(ConfigurationKey.FAF_CHARGING_INDICATOR
	 * .getAttributeName());
	 * 
	 * if (fafNumber.startsWith("00")) { fafIndicator =
	 * configurationMap.get(ConfigurationKey
	 * .FAF_INDICATOR_INTERNATIONAL.getAttributeName()); } else {
	 * CommandUtil.executeCommand(dynamicMenuRequest, Command.GETACCOUNTDETAILS,
	 * true); FDPCommand fdpCommand =
	 * dynamicMenuRequest.getExecutedCommand(Command.GETACCOUNTDETAILS
	 * .getCommandName()); String responseCode =
	 * fdpCommand.getOutputParam("responseCode").toString();
	 * 
	 * System.out.print("Response Code == " + responseCode); if
	 * (responseCode.equals(FDPConstant.GAD_SUCCESS_RESPONSE0) ||
	 * responseCode.equals(FDPConstant.GAD_SUCCESS_RESPONSE1) ||
	 * responseCode.equals(FDPConstant.GAD_SUCCESS_RESPONSE2)) { fafIndicator =
	 * configurationMap
	 * .get(ConfigurationKey.FAF_INDICATOR_ONNET.getAttributeName()); } else if
	 * (responseCode.equals(FDPConstant.GAD_SUCCESS_RESPONSE102)) { fafIndicator
	 * =
	 * configurationMap.get(ConfigurationKey.FAF_INDICATOR_OFFNET.getAttributeName
	 * ()); } else { // GAD command fails
	 * FDPLogger.debug(getCircleLogger(dynamicMenuRequest), getClass(),
	 * "executeGADCommandForFAF()",
	 * LoggerUtil.getRequestAppender(dynamicMenuRequest) +
	 * "response code for FAF" + responseCode); } }
	 * dynamicMenuRequest.putAuxiliaryRequestParameter
	 * (AuxRequestParam.FAF_INDICATOR, fafIndicator);
	 * dynamicMenuRequest.putAuxiliaryRequestParameter
	 * (AuxRequestParam.FAF_CHARGING_INDICATOR, fafChargingIndicator);
	 * 
	 * }
	 * 
	 * }
	 */

}