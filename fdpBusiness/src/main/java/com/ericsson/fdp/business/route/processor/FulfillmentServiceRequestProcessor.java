package com.ericsson.fdp.business.route.processor;

import java.util.List;
import java.util.Map;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;

import org.apache.camel.Exchange;
import org.apache.camel.Message;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ericsson.fdp.FDPCacheable;
import com.ericsson.fdp.business.constants.BusinessConstants;
import com.ericsson.fdp.business.dto.Me2uProductDTO;
import com.ericsson.fdp.business.enums.AuxRequestParam;
import com.ericsson.fdp.business.enums.Command;
import com.ericsson.fdp.business.enums.ConfigurationKey;
import com.ericsson.fdp.business.enums.ProductActionTypeEnum;
import com.ericsson.fdp.business.enums.RequestMetaValuesKey;
import com.ericsson.fdp.business.enums.ivr.FulfillmentActionTypes;
import com.ericsson.fdp.business.enums.ivr.FulfillmentParameters;
import com.ericsson.fdp.business.enums.ivr.commandservice.IVRCommandEnum;
import com.ericsson.fdp.business.menu.impl.IVRDynamicMenuImpl;
import com.ericsson.fdp.business.node.FDPServiceProvisioningNode;
import com.ericsson.fdp.business.product.Product;
import com.ericsson.fdp.business.product.impl.ProductNameCacheImpl;
import com.ericsson.fdp.business.request.impl.FDPMetadataResponseImpl;
import com.ericsson.fdp.business.request.impl.FulfillmentRequestImpl;
import com.ericsson.fdp.business.route.processor.ivr.AbstractFulfillmentProcessor;
import com.ericsson.fdp.business.step.execution.impl.rsdeprovision.AbstractRSActiveAccounts;
import com.ericsson.fdp.business.util.Data2ShareService;
import com.ericsson.fdp.business.util.FulfillmentUtil;
import com.ericsson.fdp.business.util.LoggerUtil;
import com.ericsson.fdp.business.util.RequestUtil;
import com.ericsson.fdp.business.util.ServiceProvisioningUtil;
import com.ericsson.fdp.common.constants.FDPConstant;
import com.ericsson.fdp.common.enums.ChannelType;
import com.ericsson.fdp.common.enums.ExternalSystem;
import com.ericsson.fdp.common.enums.ModuleType;
import com.ericsson.fdp.common.util.StringUtil;
import com.ericsson.fdp.common.vo.FDPCircle;
import com.ericsson.fdp.core.command.FDPCommand;
import com.ericsson.fdp.core.config.utils.PropertyUtils;
import com.ericsson.fdp.core.exception.ExecutionFailedException;
import com.ericsson.fdp.core.logging.FDPLogger;
import com.ericsson.fdp.core.node.FDPNode;
import com.ericsson.fdp.core.request.FDPRequest;
import com.ericsson.fdp.core.request.FDPResponse;
import com.ericsson.fdp.core.request.impl.FDPRequestImpl;
import com.ericsson.fdp.core.serviceprovisioning.bean.FDPMetaBag;
import com.ericsson.fdp.core.utils.ApplicationConfigUtil;
import com.ericsson.fdp.dao.enums.DBActionClassName;
import com.ericsson.fdp.dao.enums.FDPServiceProvSubType;
import com.ericsson.fdp.dao.enums.ProductAdditionalInfoEnum;
import com.ericsson.fdp.route.enumeration.FDPRouteHeaders;

/**
 * The Class FullfillmentServiceRequestProcessor is used to process the user
 * input and returns response to invoker.
 * 
 * @author Ericsson
 */
public class FulfillmentServiceRequestProcessor extends AbstractFulfillmentProcessor {

	/** The FDPDynamicMenu **/
	@Resource(lookup = "java:app/fdpBusiness-1.0/IVRDynamicMenuImpl")
	private IVRDynamicMenuImpl fdpDynamicMenu;
	
	private final String data2shareLookUp = DBActionClassName.DATA2SHARE_SERVICE.getLookUpClass();
	
	private final String ejbLookupName = DBActionClassName.RS_DE_PROVISIONING_SERVICE.getLookUpClass();

	private static final Logger LOGGER = LoggerFactory.getLogger(FulfillmentServiceRequestProcessor.class);
	/** The Constant COUNTRY_CODE. */
	private static final String COUNTRY_CODE = PropertyUtils.getProperty("COUNTRY_CODE");
	private static final Integer MSISDN_LENGTH = Integer.parseInt(PropertyUtils.getProperty("fdp.msisdn.length"));
	private static final String PRODUCT_NAME_ID_MAPPING = PropertyUtils.getProperty("PRODUCT_NAME_ID_MAPPING");
	/*@Resource(lookup = "java:app/fdpCoreServices-1.0/ApplicationConfigCache")
	private FDPCache<FDPAppBag, Object> applicationConfigCache;*/

	@Override
	public void process(Exchange exchange) throws Exception {
		FDPResponse fdpResponse = null;
		final FulfillmentParameters param = FulfillmentParameters.INVOCATOR_NAME;
		final String iname = exchange.getIn().getHeader(param.getValue(), String.class).toUpperCase();
		Message in = exchange.getIn();
		final FDPCircle fdpCircle = exchange.getIn().getHeader(FDPRouteHeaders.FDP_CIRCLE.getValue(), FDPCircle.class);
		// Changes done to handle product name as an input parameter as well Product DM code as input parameter value
		String input = in.getHeader(BusinessConstants.INPUT, String.class);
		String UTValue =exchange.getIn().getHeader(BusinessConstants.UTValue, String.class);
		UTValue = (UTValue == null || UTValue == "") ? "0" : UTValue;
		String productAction = null;
		String productName = null;
		if (PRODUCT_NAME_ID_MAPPING != null && PRODUCT_NAME_ID_MAPPING.equalsIgnoreCase("Y")) {
			//String productName = in.getHeader(BusinessConstants.INPUT, String.class);
//			String productAction = null;
//			String productName = null;
			String[] productNameWithAction = input.split(FDPConstant.UNDERSCORE);
			
			if (productNameWithAction.length > 1) {
				productAction = ProductActionTypeEnum.getValueFromProductAction(productNameWithAction[0]);
				if (productAction != null) {
					productName = input.substring(productAction.length() + 1);
				}
			}
			if (productAction == null) {
				productAction = "NACT";
				productName = input;
			}
			
			input = getProductDMCode(fdpCircle, productName, productAction);
			
			in.setHeader(BusinessConstants.INPUT, input);
			exchange.setIn(in);
		}
		//final String input = in.getHeader(BusinessConstants.INPUT, String.class);
		
		String action = getAction(exchange);
		
		FulfillmentRequestImpl fullfillmentRequestImpl = createFDPIVRRequestObject(in);
		// Below code commented to fix RS deprovisioning issue
		/* 
		 * if (null != action && action.equals(FulfillmentActionTypes.RS_DEPROVISION_PRODUCT.getValue())) {
			productAction = FDPConstant.DACT;
			Product prodInform = RequestUtil.getProductById(fullfillmentRequestImpl, input);
			input = prodInform.getProductName();
			input = getProductDMCode(fdpCircle, input, productAction);
			fullfillmentRequestImpl.setRequestString(new FDPIVRRequestStringImpl(input));
			action = null;
			//productName = input;
		}*/
		fullfillmentRequestImpl.putAuxiliaryRequestParameter(AuxRequestParam.CONSUMER_LIMIT_UPGRADE_VALUE , UTValue);
		final String productId = getProductID(fdpCircle, input, iname, action);
		updateNonMandatoryParam(exchange, fullfillmentRequestImpl, productId);
	//	RequestUtil.updateSIMLangaugeInRequest(fullfillmentRequestImpl,applicationConfigCache);

		if (input.contains(FDPConstant.DACT)) {
			fullfillmentRequestImpl.setExternalSystemToCharge(ExternalSystem.AIR);
		}
		getProvisioningActionConstraint(fullfillmentRequestImpl);
		if (input.equalsIgnoreCase(FDPConstant.VIEW_SUBS_HISTORY)) {
			action = "VIEW_SUBS_HISTORY";
		}else if(input.equalsIgnoreCase(IVRCommandEnum.VIEWHISTORY.getIvrName())) {
			action = IVRCommandEnum.VIEWHISTORY.getIvrName();
		}
		
		updateRequestAuxParamsForUCUT(exchange, fullfillmentRequestImpl);
		preProcessingOfLogs(fullfillmentRequestImpl, exchange, productId, input, iname);
		if (null != action) {
			LOGGER.debug("Found Action:" + action);
			final FulfillmentActionTypes fulfillmentActionTypes = FulfillmentActionTypes
					.getFulfillmentActionTypesByName(action);
			if ((null == fulfillmentActionTypes) || (null == fulfillmentActionTypes.getJndiLookupName())) {
				throw new ExecutionFailedException("Not a valid action type in action request parameter");
			}
			fullfillmentRequestImpl.setActionTypes(fulfillmentActionTypes);
			updateRequestParameters(fullfillmentRequestImpl, in);
			updateRequestAuxParams(exchange, fullfillmentRequestImpl);
			fdpResponse = executeFulFillmentService(fullfillmentRequestImpl, fulfillmentActionTypes.getJndiLookupName());
		} else {
			LOGGER.debug("Found RequestString:" + fullfillmentRequestImpl.getRequestString());
			updateRequestForIVRCommandInputs(exchange, fullfillmentRequestImpl);
			fdpResponse = fdpDynamicMenu.executeDynamicMenu(fullfillmentRequestImpl, in, exchange);
		}

		if (null != exchange.getIn().getHeader("IVR_HISTORY_MARKER")) {
			return;
		}
		if (null == fdpResponse) {
			throw new ExecutionFailedException("Found NULL Response while executing fulfillment Service request");
		}

		Message out = exchange.getOut();
		exchange.setProperty(Exchange.CONTENT_TYPE, "text/xml");
		String fulfillmentResponse = null;
		if (!(fdpResponse instanceof FDPMetadataResponseImpl)) {
			throw new ExecutionFailedException("Response is not of type FDPMetadataResponseImpl.");
		}
		FDPMetadataResponseImpl fdpMetadataResponseImpl = (FDPMetadataResponseImpl) fdpResponse;
		fulfillmentResponse = fdpMetadataResponseImpl.getFulfillmentResponse();
		out.setBody(fulfillmentResponse);
		printIVRTrafficOutLogger(in, fulfillmentResponse);
	}

	private void updateNonMandatoryParam(Exchange exchange, FulfillmentRequestImpl fullfillmentRequestImpl,
			String productId) throws ExecutionFailedException {

		final String beneficiary = getMsisdn(exchange.getIn(), FulfillmentParameters.BENEFICIARY_NUMBER.getValue());// exchange.getIn().getHeader(FulfillmentParameters.BENEFICIARY_NUMBER.getValue(),String.class);
		
		final String voucherCode = exchange.getIn().getHeader(FulfillmentParameters.TIME4U_VOUCHER_ID.getValue(),String.class);
		
		final String paymentMode = exchange.getIn().getHeader(FulfillmentParameters.PAYMENT_MODE.getValue(),
				String.class);
		final String input = exchange.getIn().getHeader(FulfillmentParameters.INPUT.getValue(),
				String.class);
		final String transferAmt = exchange.getIn().getHeader(FulfillmentParameters.TRANSFER.getValue(),
				String.class);
		final String srcActType = exchange.getIn().getHeader(FulfillmentParameters.SRC_ACCOUNT_TYPE.getValue(),
				String.class);
		final String dstActType = exchange.getIn().getHeader(FulfillmentParameters.DST_ACCOUNT_TYPE.getValue(),
				String.class);
		final String autorenewal = exchange.getIn()
				.getHeader(FulfillmentParameters.AUTO_RENEW.getValue(), String.class);
		final String action = exchange.getIn().getHeader(FulfillmentParameters.ACTION.getValue(), String.class);
		final String splitno = exchange.getIn().getHeader(FulfillmentParameters.SPLIT_NUMBER.getValue(), String.class);
		final String sendsms = exchange.getIn().getHeader(FulfillmentParameters.SEND_SMS.getValue(), String.class);
		final String sendflash = exchange.getIn().getHeader(FulfillmentParameters.SEND_FLASH.getValue(), String.class);
		final String productcost = exchange.getIn().getHeader(FulfillmentParameters.PRODUCT_COST.getValue(),
				String.class);
		final String skiprscharging = exchange.getIn().getHeader(FulfillmentParameters.SKIP_RS_CHARING.getValue(),
				String.class);
		final String skipcharging = exchange.getIn().getHeader(FulfillmentParameters.SKIP_CHARGING.getValue(),
				String.class);
		final String transaction_id = exchange.getIn().getHeader(FulfillmentParameters.TRANSACTION_ID.getValue(),
				String.class);
		final String expiryNotificationFlag = exchange.getIn().getHeader(
				FulfillmentParameters.EXPIRY_NOTIFICATION_FLAG.getValue(), String.class);
		final String fafMsisdn = exchange.getIn().getHeader(FulfillmentParameters.FAF_MSISDN.getValue(), String.class);

		final String oldFafMsisdn = exchange.getIn().getHeader(FulfillmentParameters.OLD_FAF_MSISDN.getValue(),
				String.class);
		// --START--artf699590
		final FDPCacheable fdpCacheable = RequestUtil.getProductById(fullfillmentRequestImpl, productId);
		
		final String validity =exchange.getIn().getHeader(FulfillmentParameters.OFFER_VALIDITY.getValue(), String.class);
		
		final String alternateNumber = getMsisdn(exchange.getIn(), FulfillmentParameters.ALTERNATE_NUMBER.getValue());
		
		if(null != alternateNumber && !alternateNumber.isEmpty())
			fullfillmentRequestImpl.putAuxiliaryRequestParameter(AuxRequestParam.NUMBER_RESERVATION_ALTERNATE_NUMBER, alternateNumber);
		
		boolean isBuyForOtherEnabled = false;
		if (fdpCacheable instanceof Product) {
			final Product product = (Product) fdpCacheable;
			isBuyForOtherEnabled = (null != product.getAdditionalInfo(ProductAdditionalInfoEnum.PRODUCT_FOR_OTHER)) ? Boolean
					.parseBoolean(product.getAdditionalInfo(ProductAdditionalInfoEnum.PRODUCT_FOR_OTHER)) : false;
		}
		if (null != beneficiary) {
			if (isBuyForOtherEnabled && !input.contains(FDPConstant.DATA2Share) && !input.contains(FDPConstant.ME2U_PREFIX)) {
				fullfillmentRequestImpl.putAuxiliaryRequestParameter(AuxRequestParam.BENEFICIARY_MSISDN, beneficiary);
			} 
			else {
				if(input.contains(FDPConstant.DATA2Share) && input.contains(FDPConstant.ME2U_PREFIX)) {
					final String clientTransactionID = exchange.getIn().getHeader(FulfillmentParameters.CLIENT_TRANSACTION_ID.getValue(),
							String.class);
					
					fullfillmentRequestImpl.setOriginTransactionID(clientTransactionID==null?null:Long.parseLong(clientTransactionID));
					
					fullfillmentRequestImpl.putAuxiliaryRequestParameter(AuxRequestParam.isData2Share, FDPConstant.TRUE);
					
					int offerId = 0;
		
					// Executing GBAD command
					if (ejbLookupName == null) {
						throw new ExecutionFailedException("Could not find ejb class for execution" + ejbLookupName);
					}
					try {
						FDPCommand fdpCommandGBAD = null;
						final Object beanObject = ApplicationConfigUtil.getBean(ejbLookupName);

						if (null != beanObject && (beanObject instanceof AbstractRSActiveAccounts)) {
							final AbstractRSActiveAccounts accountService = (AbstractRSActiveAccounts) beanObject;
							fdpCommandGBAD = accountService.isCommandExecuted(fullfillmentRequestImpl, Command.GET_BALANCE_AND_DATE.getCommandDisplayName());
							
							final Object data2shareObj = ApplicationConfigUtil.getBean(data2shareLookUp);
							if(data2shareObj instanceof Data2ShareService) {
								final Data2ShareService data2ShareService = (Data2ShareService) data2shareObj;
								final Map<String, String> configurationMap = fullfillmentRequestImpl.getCircle().getConfigurationKeyValueMap();
								String data2ShareOfferId = configurationMap.get(ConfigurationKey.ME2U_DATA2SHARE_OFFERID.getAttributeName());
								if(null == data2ShareOfferId)
									data2ShareOfferId = "0";
								offerId = data2ShareService.getOfferIdOnProductId(productId, data2ShareOfferId);
								
								List<Me2uProductDTO> me2UProductDTO = data2ShareService.getData2ShareProducts(fdpCommandGBAD, fullfillmentRequestImpl);
								for(Me2uProductDTO me2uProdDTO : me2UProductDTO) {
									if(me2uProdDTO.getOfferID().equals(offerId+"")) {
										fullfillmentRequestImpl.putAuxiliaryRequestParameter(AuxRequestParam.SELECTED_Me2UDATA2SHARE_PRODUCT, me2uProdDTO);
										break;
									}
								}
								// Checking transfer value < data pack value
								Object selectedMe2uObject = fullfillmentRequestImpl.getAuxiliaryRequestParameter(AuxRequestParam.SELECTED_Me2UDATA2SHARE_PRODUCT);
								Boolean dataAmtCheck = transferAmt.matches("[0-9]+");
								if(dataAmtCheck) {
									if(null != transferAmt && null != selectedMe2uObject) {
										Me2uProductDTO selectedMe2uProdDTO = (Me2uProductDTO) selectedMe2uObject;
										if(Long.parseLong(transferAmt) > Long.parseLong(selectedMe2uProdDTO.getAvailableBalance())) {
											productId = 0+"";
										}
									}
								}
								else
									productId = 0+"";
							}
						}
					}
					catch(Exception e) {
						throw new ExecutionFailedException("Something went wrong in executing or setting parameters of GBAD command for product Id "+productId);
					}
					
				}
				fullfillmentRequestImpl.putAuxiliaryRequestParameter(AuxRequestParam.ME2U_SUBSCRIBER_TO_CREDIT, beneficiary);
				fullfillmentRequestImpl.putAuxiliaryRequestParameter(AuxRequestParam.BENEFICIARY_MSISDN,beneficiary);
				fullfillmentRequestImpl.putAuxiliaryRequestParameter(AuxRequestParam.ME2U_AMT_TO_TRANSFER, transferAmt);
				if(input.contains(FDPConstant.TIME2SHARE))  {
					fullfillmentRequestImpl.putAuxiliaryRequestParameter(AuxRequestParam.ME2U_AMT_TO_BE_RECIEVED, transferAmt);
					fullfillmentRequestImpl.putAuxiliaryRequestParameter(AuxRequestParam.TIME2SHARE_ACCOUNT_TO_SHARE_FROM, srcActType);
					fullfillmentRequestImpl.putAuxiliaryRequestParameter(AuxRequestParam.TIME2SHARE_ACCOUNT_TO_SHARE_TO, dstActType);
					fullfillmentRequestImpl.putAuxiliaryRequestParameter(AuxRequestParam.BENEFICIARY_MSISDN, null);
					fullfillmentRequestImpl.putAuxiliaryRequestParameter(AuxRequestParam.IS_TIME2SHARE, true);
				}
				fullfillmentRequestImpl.putAuxiliaryRequestParameter(AuxRequestParam.ME2U_TRANS_CHARGES, 0l);
			}
		}
		
		if(true == isVoucherCodeValid(fullfillmentRequestImpl,voucherCode)){
			  fullfillmentRequestImpl.putAuxiliaryRequestParameter(AuxRequestParam.TIME4U_VOUCHER_ID, voucherCode);							
		}

		// --END-- artf699590
		if (null != paymentMode && !paymentMode.isEmpty()) {
			if(paymentMode.contains("_")){
				String payMode[] = paymentMode.split("_");
				String paysrc = payMode[0];
				String tpin = payMode[1];
				
			externalSytemOnPaymentMode(fullfillmentRequestImpl, paysrc);
			fullfillmentRequestImpl.putAuxiliaryRequestParameter(AuxRequestParam.PAYMENT_MODE, paysrc);
			fullfillmentRequestImpl.putAuxiliaryRequestParameter(AuxRequestParam.TPIN, tpin);
			}
			else{
				externalSytemOnPaymentMode(fullfillmentRequestImpl, paymentMode);
				fullfillmentRequestImpl.putAuxiliaryRequestParameter(AuxRequestParam.PAYMENT_MODE, paymentMode);
			}
			}/*
		 * else{
		 * fullfillmentRequestImpl.setExternalSystemToCharge(ExternalSystem
		 * .AIR); }
		 */
		if (null != autorenewal) {
			fullfillmentRequestImpl.putAuxiliaryRequestParameter(AuxRequestParam.AUTO_RENEW, autorenewal);
		}

		if (null != action) {
			fullfillmentRequestImpl.putAuxiliaryRequestParameter(AuxRequestParam.ACTION, action);
		}
		if (null != splitno) {
			fullfillmentRequestImpl.putAuxiliaryRequestParameter(AuxRequestParam.SPLIT_NUMBER, splitno);
		}
		if (null != sendsms) {
			fullfillmentRequestImpl.putAuxiliaryRequestParameter(AuxRequestParam.SEND_SMS, sendsms);
		}
		if (null != sendflash) {
			fullfillmentRequestImpl.putAuxiliaryRequestParameter(AuxRequestParam.SEND_FLASH, sendflash);
		}
		if (null != productcost && productcost.length()>0) {
			fullfillmentRequestImpl.putAuxiliaryRequestParameter(AuxRequestParam.PRODUCT_COST, productcost);
		}
		if (null != skiprscharging) {
			fullfillmentRequestImpl.putAuxiliaryRequestParameter(AuxRequestParam.SKIP_RS_CHARING, skiprscharging);
		}
		if (null != skipcharging) {
			if (skipcharging.equalsIgnoreCase("true"))
				fullfillmentRequestImpl.putAuxiliaryRequestParameter(AuxRequestParam.SKIP_CHARGING, true);
			else
				fullfillmentRequestImpl.putAuxiliaryRequestParameter(AuxRequestParam.SKIP_CHARGING, false);
		}
		if (null != transaction_id) {
			fullfillmentRequestImpl.putAuxiliaryRequestParameter(AuxRequestParam.TRANSACTION_ID, transaction_id);
		}
		if (null != expiryNotificationFlag) {
			fullfillmentRequestImpl.putAuxiliaryRequestParameter(AuxRequestParam.EXPIRY_NOTIFICATION_FLAG,
					expiryNotificationFlag);
		}
		if (null != fafMsisdn) {
			fullfillmentRequestImpl.putAuxiliaryRequestParameter(AuxRequestParam.FAF_NUMBER, fafMsisdn);
			fullfillmentRequestImpl.putAuxiliaryRequestParameter(AuxRequestParam.FAF_MSISDN_ADD, fafMsisdn);
			fullfillmentRequestImpl.putAuxiliaryRequestParameter(AuxRequestParam.FAF_EXECUTE_COMMAND_AGAIN, "true");
		}
		if (null != oldFafMsisdn) {
			fullfillmentRequestImpl.putAuxiliaryRequestParameter(AuxRequestParam.FAF_NUMBER_GAD, oldFafMsisdn);
			fullfillmentRequestImpl.putAuxiliaryRequestParameter(AuxRequestParam.FAF_MSISDN_DELETE, oldFafMsisdn);
			fullfillmentRequestImpl.putAuxiliaryRequestParameter(AuxRequestParam.FAF_EXECUTE_COMMAND_AGAIN, "true");
		}

		//Parameter to be used for Time4U amount.
		if (!StringUtil.isNullOrEmpty(
				exchange.getIn().getHeader(FulfillmentParameters.TIME4_PRODUCT_COST.getValue(), String.class))) {
			final Integer cost = exchange.getIn().getHeader(FulfillmentParameters.TIME4_PRODUCT_COST.getValue(), Integer.class);
			FulfillmentUtil.updateParameterForTime4U(fullfillmentRequestImpl, cost);
		}
		
		if(null != validity){
			fullfillmentRequestImpl.putAuxiliaryRequestParameter(AuxRequestParam.OFFER_VALIDITY, validity);
		}
		
		if (!StringUtil.isNullOrEmpty(
				exchange.getIn().getHeader(FulfillmentParameters.VIEW_PRODUCT_TYPE.getValue(), String.class))) {
			String viewProductType = exchange.getIn().getHeader(FulfillmentParameters.VIEW_PRODUCT_TYPE.getValue(), String.class);
			fullfillmentRequestImpl.putAuxiliaryRequestParameter(AuxRequestParam.VIEW_PRODUCT_TYPE, viewProductType);
		}

	}

	/**
	 * Gets the product id.
	 * 
	 * @param fdpCircle
	 *            the fdp circle
	 * @param inputIVR
	 *            the input ivr
	 * @return the product id
	 * @throws ExecutionFailedException
	 *             the execution failed exception
	 */
	private String getProductID(final FDPCircle fdpCircle, final String inputIVR, String iname, String action)
			throws ExecutionFailedException {
		String productId = null;
		if (null != action) {
			final FulfillmentActionTypes fulfillmentActionTypes = FulfillmentActionTypes
					.getFulfillmentActionTypesByName(action);
			if (null != fulfillmentActionTypes
					&& fulfillmentActionTypes.getValue().equals(FulfillmentActionTypes.PRODUCT_BUY.getValue())) {
				productId = inputIVR;
			}
			if (null != fulfillmentActionTypes
					&& fulfillmentActionTypes.getValue().equals(FulfillmentActionTypes.RS_DEPROVISION_PRODUCT.getValue())) {
				productId = inputIVR;
			}
		}
		if (null == productId) {
			String cachedDMIVRCode = iname + FDPConstant.SPACE + inputIVR;
			// final String cachedDMIVRCode = channel + FDPConstant.SPACE +
			// inputIVR;
			FDPLogger.debug(getCircleRequestLogger(fdpCircle), getClass(), "getProductID()",
					"Fetching for IVR DM Code:" + cachedDMIVRCode);
			final FDPCacheable fdpCacheable = ApplicationConfigUtil.getMetaDataCache().getValue(
					new FDPMetaBag(fdpCircle, ModuleType.DM, cachedDMIVRCode));
			FDPNode fdpNode = RequestUtil.checkInstanceForNode(fdpCacheable);
			if (null != fdpNode && fdpNode instanceof FDPServiceProvisioningNode) {
				final FDPServiceProvisioningNode serviceProvisioningNode = (FDPServiceProvisioningNode) fdpNode;
				productId = serviceProvisioningNode.getEntityIdForCache(RequestMetaValuesKey.PRODUCT);
			}
		}
		return productId;
	}

	/**
	 * This method is used to update the URL parameters into the request Object.
	 * 
	 * @param fulfillmentRequestImpl
	 * @param message
	 * @throws ExecutionFailedException
	 */
	private void updateRequestParameters(final FulfillmentRequestImpl fulfillmentRequestImpl, final Message message)
			throws ExecutionFailedException {
		final FulfillmentActionTypes actionTypes = fulfillmentRequestImpl.getActionTypes();
		fulfillmentRequestImpl.setRequestAction(actionTypes.name());
		fulfillmentRequestImpl.setAutoApprove(getAutoApproveParameter(message));
		switch (actionTypes) {
		case ADD_SHARED_CONSUMER:
			fulfillmentRequestImpl.setConsumerMsisdn(message.getHeader(
					FulfillmentParameters.CONSUMER_MSISDN.getValue(), String.class));
			fulfillmentRequestImpl.setConsumerName(message.getHeader(FulfillmentParameters.CONSUMER_NAME.getValue(),
					String.class));
			break;
		case REMOVE_SHARED_CONSUMER:
		case VIEW_ALL_SHARED_CONSUMER:
		case VIEW_SHARED_USAGE:
			fulfillmentRequestImpl.setConsumerMsisdn(message.getHeader(
					FulfillmentParameters.CONSUMER_MSISDN.getValue(), String.class));
			break;
		case ACCEPT_SHARED_CONSUMER:
			fulfillmentRequestImpl.setDbId(message.getHeader(FulfillmentParameters.DATABASE_ID.getValue(),
					Integer.class));
			break;
		case DETACH_CONSUMER:
		case ADD_CONSUMER_ON_REQUEST:
			fulfillmentRequestImpl.setProviderMsisdn(message.getHeader(
					FulfillmentParameters.PROVIDER_MSISDN.getValue(), String.class));
			break;
		case VIEW_HISTORY:
		case PRODUCT_BUY:
			/**
			 * modified by evasaty bug fix : artf700062
			 */
			fulfillmentRequestImpl.setChannel(ChannelType.valueOf(fulfillmentRequestImpl.getIname()));
			fulfillmentRequestImpl.setConsumerMsisdn(getMsisdn(message, FulfillmentParameters.MSISDN.getValue()));
			fulfillmentRequestImpl.setCommandInputParams(FulfillmentParameters.TRANSACTION_ID,
					message.getHeader(FulfillmentParameters.TRANSACTION_ID.getValue(), String.class));
			final String isSkipRsCharging = (String) message.getHeader(
					FulfillmentParameters.SKIP_RS_CHARING.getValue(), String.class);
			if (null != isSkipRsCharging && FDPConstant.CAPITAL_Y.equals(isSkipRsCharging)) {
				fulfillmentRequestImpl.setCommandInputParams(FulfillmentParameters.SKIP_RS_CHARING,
						FDPConstant.CAPITAL_Y);
			} else {
				fulfillmentRequestImpl.setIname(message.getHeader(FulfillmentParameters.INVOCATOR_NAME.getValue().toUpperCase(),
						String.class));
			}
			break;
		case OPT_IN:
			fulfillmentRequestImpl.setConsumerMsisdn(getMsisdn(message, FulfillmentParameters.MSISDN.getValue()));
			break;
		case OPT_OUT:
			fulfillmentRequestImpl.setConsumerMsisdn(getMsisdn(message, FulfillmentParameters.MSISDN.getValue()));
			break;
		case ME2ULIST:
			fulfillmentRequestImpl.setProviderMsisdn(message.getHeader(
					FulfillmentParameters.PROVIDER_MSISDN.getValue(), String.class));
			break;
		case ABILITY_SYNC_UP:
			fulfillmentRequestImpl.setCommandInputParams(FulfillmentParameters.EXTERNAL_REFERENCE, 
					message.getHeader(FulfillmentParameters.EXTERNAL_REFERENCE.getValue(), String.class));
			fulfillmentRequestImpl.setCommandInputParams(FulfillmentParameters.EXTERNAL_APPLICATION, 
					message.getHeader(FulfillmentParameters.EXTERNAL_APPLICATION.getValue(), String.class));
			fulfillmentRequestImpl.setCommandInputParams(FulfillmentParameters.EXTERNAL_USER, 
					message.getHeader(FulfillmentParameters.EXTERNAL_USER.getValue(), String.class));
			fulfillmentRequestImpl.setCommandInputParams(FulfillmentParameters.REQUESTED_APPLICATION, 
					message.getHeader(FulfillmentParameters.REQUESTED_APPLICATION.getValue(), String.class));
			fulfillmentRequestImpl.setCommandInputParams(FulfillmentParameters.OPERATION_NAME, 
					message.getHeader(FulfillmentParameters.OPERATION_NAME.getValue(), String.class));
			fulfillmentRequestImpl.setCommandInputParams(FulfillmentParameters.ENTITY_ID, 
					message.getHeader(FulfillmentParameters.ENTITY_ID.getValue(), String.class));
			fulfillmentRequestImpl.setCommandInputParams(FulfillmentParameters.OFFER_CODE, 
					message.getHeader(FulfillmentParameters.OFFER_CODE.getValue(), String.class));
			fulfillmentRequestImpl.setCommandInputParams(FulfillmentParameters.SUBSCRIPTION_FLAG, 
					message.getHeader(FulfillmentParameters.SUBSCRIPTION_FLAG.getValue(), String.class));
			fulfillmentRequestImpl.setCommandInputParams(FulfillmentParameters.ACTIVATION_DATE, 
					message.getHeader(FulfillmentParameters.ACTIVATION_DATE.getValue(), String.class));
			fulfillmentRequestImpl.setCommandInputParams(FulfillmentParameters.EXPIRY_DATE_ABILITY, 
					message.getHeader(FulfillmentParameters.EXPIRY_DATE_ABILITY.getValue(), String.class));
			fulfillmentRequestImpl.setCommandInputParams(FulfillmentParameters.OFFER_CHARGE, 
					message.getHeader(FulfillmentParameters.OFFER_CHARGE.getValue(), String.class));
			fulfillmentRequestImpl.setCommandInputParams(FulfillmentParameters.ORDER_STATUS, 
					message.getHeader(FulfillmentParameters.ORDER_STATUS.getValue(), String.class));
			break;
		case SIM_CHANGE:
			Long newMSISDN = message.getHeader(FulfillmentParameters.MSISDN.getValue(), Long.class);
			if (newMSISDN != null && newMSISDN.toString().length() == MSISDN_LENGTH) {
				fulfillmentRequestImpl.setSubscriberNumber(newMSISDN);
			}
			fulfillmentRequestImpl.putAuxiliaryRequestParameter(AuxRequestParam.IMEI, message.getHeader(FulfillmentParameters.OLD_IMEI.getValue(), String.class));
			fulfillmentRequestImpl.putAuxiliaryRequestParameter(AuxRequestParam.OLD_MSISDN, message.getHeader(FulfillmentParameters.OLD_MSISDN.getValue(), Long.class));
			fulfillmentRequestImpl.putAuxiliaryRequestParameter(AuxRequestParam.DEVICE_TYPE, message.getHeader(FulfillmentParameters.OLD_DEV_TYPE.getValue(), String.class));
			break;
		case DEVICE_CHANGE:
			Long msisdn = message.getHeader(FulfillmentParameters.MSISDN.getValue(), Long.class);
			if (msisdn != null && msisdn.toString().length() == MSISDN_LENGTH) {
				fulfillmentRequestImpl.setSubscriberNumber(msisdn);
			} 
			fulfillmentRequestImpl.putAuxiliaryRequestParameter(AuxRequestParam.IMEI, message.getHeader(FulfillmentParameters.NEW_IMEI.getValue(), String.class));
			fulfillmentRequestImpl.putAuxiliaryRequestParameter(AuxRequestParam.OLD_IMEI, message.getHeader(FulfillmentParameters.OLD_IMEI.getValue(), String.class));
			fulfillmentRequestImpl.putAuxiliaryRequestParameter(AuxRequestParam.DEVICE_TYPE, message.getHeader(FulfillmentParameters.NEW_DEV_TYPE.getValue(), String.class));
			fulfillmentRequestImpl.putAuxiliaryRequestParameter(AuxRequestParam.OLD_DEVICE_TYPE, message.getHeader(FulfillmentParameters.OLD_DEV_TYPE.getValue(), String.class));
			break;	
		default:
			break;
		}
	}

	/**
	 * This method gets the Auto Approve parameter from Request.
	 * 
	 * @param message
	 * @return
	 */
	private Boolean getAutoApproveParameter(final Message message) {
		Boolean autoApprove = true;
		final String inputAutoApprove = message.getHeader(FulfillmentParameters.AUTO_APPROVE_SHARED.getValue(),
				String.class);
		if (null != inputAutoApprove && (inputAutoApprove.matches("(n|N)"))) {
			autoApprove = false;
		}
		return autoApprove;
	}

	/**
	 * Gets the msisdn.
	 * 
	 * @param in
	 *            the in
	 * @return the msisdn
	 */
	protected String getMsisdn(final Message in, String FulfilmentParamvalue) {
		String msisdn = in.getHeader(FulfilmentParamvalue, String.class);
		if (msisdn != null && msisdn.length() == MSISDN_LENGTH) {
			msisdn = new StringBuilder(COUNTRY_CODE).append(msisdn).toString();
		}
		return msisdn;
	}

	/**
	 * This method populates the request from Input from URL.
	 * 
	 * @param exchange
	 * @param fdpRequest
	 */
	private void updateRequestForIVRCommandInputs(final Exchange exchange, final FulfillmentRequestImpl fdpRequest) {
		final IVRCommandEnum commandEnum = getInputIVRCommand(exchange);
		if ((null != commandEnum) && (null != commandEnum.getParameterList())) {
			List<FulfillmentParameters> parameters = commandEnum.getParameterList();
			for (final FulfillmentParameters parameter : parameters) {
				fdpRequest.setCommandInputParams(parameter,
						exchange.getIn().getHeader(parameter.getValue(), String.class));
			}
		}
	}

	/**
	 * This method will update the AUX Params in the request.
	 * 
	 * @param exchange
	 * @param fdpRequest
	 */
	private void updateRequestAuxParams(final Exchange exchange, final FulfillmentRequestImpl fdpRequest) {
		final String isSkipRsCharging = (String) exchange.getIn().getHeader(
				FulfillmentParameters.SKIP_RS_CHARING.getValue(), String.class);
		if (null != isSkipRsCharging && FDPConstant.CAPITAL_Y.equals(isSkipRsCharging)) {
			fdpRequest.putAuxiliaryRequestParameter(AuxRequestParam.SKIP_RS_CHARGING, Boolean.TRUE);

		}
	}

	/**
	 * This method will update the AUX Params for threshold/counter id in the request.
	 * 
	 * @param exchange
	 * @param fdpRequest
	 */
	private void updateRequestAuxParamsForUCUT(final Exchange exchange, final FulfillmentRequestImpl fdpRequest) {
		final String threshold_id = exchange.getIn().getHeader(FulfillmentParameters.THRESHOLD_ID.getValue(),
				String.class);
		final String counter_id = exchange.getIn().getHeader(FulfillmentParameters.COUNTER_ID.getValue(),
				String.class);
		if (threshold_id != null) {
			fdpRequest.putAuxiliaryRequestParameter(AuxRequestParam.THRESHOLD_ID, threshold_id);
		}
		if (counter_id != null) {
			fdpRequest.putAuxiliaryRequestParameter(AuxRequestParam.COUNTER_ID, counter_id);
		}
	}
	
	/**
	 * Preprocesing Log for Bug ID artf550987
	 */
	private void preProcessingOfLogs(final FDPRequest fdpRequest, final Exchange exchange, String productId,
			String input, final String iname) throws ExecutionFailedException {
		// RID:USSD_14.98.242.242_6faa92a4-b30|INIP:127.0.0.1|LNAME:DELHI_SAKET|CH:USSD|MSISDN:9711406956
		final StringBuilder appenderValue = new StringBuilder();
		appenderValue.append(FDPConstant.REQUEST_ID).append(FDPConstant.LOGGER_KEY_VALUE_DELIMITER)
				.append(fdpRequest.getRequestId()).append(FDPConstant.LOGGER_DELIMITER).append(FDPConstant.INCOMING_IP)
				.append(FDPConstant.LOGGER_KEY_VALUE_DELIMITER)
				.append(exchange.getIn().getHeader(FDPRouteHeaders.INCOMING_IP.getValue()))
				.append(FDPConstant.LOGGER_DELIMITER).append(BusinessConstants.LOGICAL_NAME)
				.append(FDPConstant.LOGGER_KEY_VALUE_DELIMITER)
				.append(exchange.getIn().getHeader(FulfillmentParameters.INVOCATOR_NAME.getValue(), String.class))
				.append(FDPConstant.LOGGER_DELIMITER).append(FDPConstant.CHANNEL_TYPE)
				.append(FDPConstant.LOGGER_KEY_VALUE_DELIMITER)
				.append(exchange.getIn().getHeader(FulfillmentParameters.INVOCATOR_NAME.getValue(), String.class))
				.append(FDPConstant.LOGGER_DELIMITER).append(FDPConstant.MSISDN)
				.append(FDPConstant.LOGGER_KEY_VALUE_DELIMITER).append(fdpRequest.getSubscriberNumber());
				//.append(FDPConstant.LOGGER_DELIMITER).append("CHCODE").append(FDPConstant.LOGGER_KEY_VALUE_DELIMITER)
				//.append(getRequestedUrl(exchange.getIn()));

		if (productId != null) {
			appenderValue.append(FDPConstant.LOGGER_DELIMITER).append(FDPConstant.PRODUCT_ID)
					.append(FDPConstant.LOGGER_KEY_VALUE_DELIMITER).append(productId);
		} else {
			appenderValue.append(FDPConstant.LOGGER_DELIMITER).append(FDPConstant.INPUT_IVR)
					.append(FDPConstant.LOGGER_KEY_VALUE_DELIMITER).append(input);
		}
		FDPLogger.info(getCircleRequestLogger(exchange), getClass(), "preProcessingOfLogs()", appenderValue.toString());
	}

	/**
	 * Get External System on Payment Mode
	 */
	private void externalSytemOnPaymentMode(FulfillmentRequestImpl fullfillmentRequestImpl, String paymentMode) {

		if (paymentMode.equalsIgnoreCase(FDPConstant.PAYMENY_MODE_AIR)) {
			fullfillmentRequestImpl.setExternalSystemToCharge(ExternalSystem.AIR);
		} else if (paymentMode.equalsIgnoreCase(FDPConstant.PAYMENY_MODE_MOBILE_MONEY)) {
			fullfillmentRequestImpl.setExternalSystemToCharge(ExternalSystem.MM);
		} else if (paymentMode.equalsIgnoreCase(FDPConstant.PAYMENY_MODE_LOYALTY)) {
			fullfillmentRequestImpl.setExternalSystemToCharge(ExternalSystem.Loyalty);
		} else if (paymentMode.equalsIgnoreCase(FDPConstant.PAYMENY_MODE_EVDS)) {
			fullfillmentRequestImpl.setExternalSystemToCharge(ExternalSystem.EVDS);
		}
	}

	/**
	 * Gets the requested url.
	 * 
	 * @param in
	 *            the in
	 * @return the requested url
	 */
	private String getRequestedUrl(final Message in) {
		HttpServletRequest request = in.getBody(HttpServletRequest.class);
		StringBuffer requestURL = request.getRequestURL();
		String queryString = request.getQueryString();
		if (queryString == null) {
			requestURL.toString();
		} else {
			requestURL.append(BusinessConstants.QUERY_STRING_SEPARATOR).append(queryString).toString();
		}
		return requestURL.toString();
	}
	
	/**
	 * Get provisioning action on provisioning rule type
	 */
	private void getProvisioningActionConstraint(FulfillmentRequestImpl fullfillmentRequestImpl) {
		FDPServiceProvSubType serviceProvSubType = ServiceProvisioningUtil.getFDPServiceProvSubType(fullfillmentRequestImpl); 
		if(FDPServiceProvSubType.PRODUCT_BUY.equals(serviceProvSubType) || FDPServiceProvSubType.FAF_REGISTER.equals(serviceProvSubType))
		{
			fullfillmentRequestImpl.putAuxiliaryRequestParameter(AuxRequestParam.PROVISIONING_ACTION,FDPConstant.SUBSCRIPTION);
		}
		else if(FDPServiceProvSubType.PRODUCT_BUY_RECURRING.equals(serviceProvSubType) || FDPServiceProvSubType.PRODUCT_BUY_RECURRING.equals(serviceProvSubType))
		{
			fullfillmentRequestImpl.putAuxiliaryRequestParameter(AuxRequestParam.PROVISIONING_ACTION, FDPConstant.RENEWAL);
		}
		else if(FDPServiceProvSubType.RS_DEPROVISION_PRODUCT.equals(serviceProvSubType) || FDPServiceProvSubType.FAF_UNREGISTER.equals(serviceProvSubType))
		{
			fullfillmentRequestImpl.putAuxiliaryRequestParameter(AuxRequestParam.PROVISIONING_ACTION,FDPConstant.UNSUBSCRIPTION);
		}
	}
	
	/**
	 * Gets the product id.
	 * 
	 * @param fdpCircle
	 *            the fdp circle
	 * @param inputIVR
	 *            the input ivr
	 * @return the product id
	 * @throws ExecutionFailedException
	 *             the execution failed exception
	 */
	
	private String getProductDMCode(final FDPCircle fdpCircle, final String productName, String productAction)
			throws ExecutionFailedException {
		String productDMCode = null;
		
		FDPLogger.debug(getCircleRequestLogger(fdpCircle), getClass(), "getProductDMCode()",
				"Fetching DM Code for Product Name:" + productName);
		
		final FDPCacheable fdpCacheable = ApplicationConfigUtil.getMetaDataCache().getValue(
				new FDPMetaBag(fdpCircle, ModuleType.PRODUCT_NAME_ID_MAP, productName));
		
		//FDPNode fdpNode = RequestUtil.checkInstanceForNode(fdpCacheable);
		
		if (null != fdpCacheable && fdpCacheable instanceof ProductNameCacheImpl) {
			final ProductNameCacheImpl productNameCache = (ProductNameCacheImpl) fdpCacheable;
			productDMCode = productNameCache.getProductIdValue();
		}
		
		//productDMCode = productAction + FDPConstant.UNDERSCORE + productDMCode;
		//Bug Fix : artf785361(Rajeev Ranjan) 4-oct-16
		if (productDMCode != null && !productDMCode.isEmpty()) {
			productDMCode = productAction + FDPConstant.UNDERSCORE + productDMCode;
		} else {
			productDMCode = productName;
		}
		return productDMCode;
	}
	
	private boolean isVoucherCodeValid(FDPRequest fdpRequest, String voucherCode) throws ExecutionFailedException {
		Boolean isVoucherCodeValid = false;
		String allowedLengthMinStr = PropertyUtils.getProperty(BusinessConstants.TIME_4_U_VOUCHER_CODE_LENGTH_MIN);
		String allowedLengthMaxStr = PropertyUtils.getProperty(BusinessConstants.TIME_4_U_VOUCHER_CODE_LENGTH_MAX);
		
		if(!StringUtil.isNullOrEmpty(allowedLengthMaxStr) && !StringUtil.isNullOrEmpty(allowedLengthMinStr)){
			final Integer allowedLengthMin = Integer.parseInt(allowedLengthMinStr.trim());
			final Integer allowedLengthMax = Integer.parseInt(allowedLengthMaxStr.trim());
			
			String regexCond = "[0-9]+";
			if(null != voucherCode && voucherCode.length() >= allowedLengthMin && voucherCode.length() <= allowedLengthMax && voucherCode.matches(regexCond)){
				((FDPRequestImpl)fdpRequest).putAuxiliaryRequestParameter(AuxRequestParam.TIME4U_VOUCHER_ID, voucherCode);
				isVoucherCodeValid = true;
			}else{
				FDPLogger.info(LoggerUtil.getSummaryLoggerFromRequest(fdpRequest), getClass(), "isVoucherCodeValid", 
						"Invalid voucher code in the request: " + voucherCode);
			}			
		}else{
			FDPLogger.info(LoggerUtil.getSummaryLoggerFromRequest(fdpRequest), getClass(), "isVoucherCodeValid", 
					"Error in fetching min length = "+allowedLengthMinStr+" and max length ="+ allowedLengthMaxStr +"from property file ");		
		}

		return isVoucherCodeValid;
	}
}
