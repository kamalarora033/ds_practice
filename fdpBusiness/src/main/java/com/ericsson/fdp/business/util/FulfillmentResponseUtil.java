package com.ericsson.fdp.business.util;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.XML;

import com.ericsson.fdp.business.charging.impl.AIRCharging;
import com.ericsson.fdp.business.charging.value.ChargingValueImpl;
import com.ericsson.fdp.business.command.AbstractCommand;
import com.ericsson.fdp.business.command.impl.NonTransactionCommand;
import com.ericsson.fdp.business.command.param.impl.CommandParamOutput;
import com.ericsson.fdp.business.constants.BusinessConstants;
import com.ericsson.fdp.business.dto.ListMe2uDTO;
import com.ericsson.fdp.business.dto.ListProductDTO;
import com.ericsson.fdp.business.dto.Me2uProductDTO;
import com.ericsson.fdp.business.dto.ProductDTO;
import com.ericsson.fdp.business.enums.AuxRequestParam;
import com.ericsson.fdp.business.enums.Command;
import com.ericsson.fdp.business.enums.ConfigurationKey;
import com.ericsson.fdp.business.enums.DisplayArea;
import com.ericsson.fdp.business.enums.RSCommandOutputParamEnum;
import com.ericsson.fdp.business.enums.RequestMetaValuesKey;
import com.ericsson.fdp.business.enums.TariffEnquiryNotificationOptions;
import com.ericsson.fdp.business.enums.ivr.FulfillmentActionTypes;
import com.ericsson.fdp.business.enums.ivr.FulfillmentAttributesType;
import com.ericsson.fdp.business.enums.ivr.commandservice.IVRCommandEnum;
import com.ericsson.fdp.business.exception.NotificationFailedException;
import com.ericsson.fdp.business.product.Product;
import com.ericsson.fdp.business.product.impl.BaseProduct;
import com.ericsson.fdp.business.request.impl.FDPMetadataResponseImpl;
import com.ericsson.fdp.business.request.impl.FulfillmentRequestImpl;
import com.ericsson.fdp.business.response.fulfillment.xml.AccountAfterRefillDetails;
import com.ericsson.fdp.business.response.fulfillment.xml.ActiveBundles;
import com.ericsson.fdp.business.response.fulfillment.xml.Attribute;
import com.ericsson.fdp.business.response.fulfillment.xml.Attributes;
import com.ericsson.fdp.business.response.fulfillment.xml.Balance;
import com.ericsson.fdp.business.response.fulfillment.xml.BalanceEnquiry;
import com.ericsson.fdp.business.response.fulfillment.xml.Balances;
import com.ericsson.fdp.business.response.fulfillment.xml.Bundle;
import com.ericsson.fdp.business.response.fulfillment.xml.Consumer;
import com.ericsson.fdp.business.response.fulfillment.xml.Consumers;
import com.ericsson.fdp.business.response.fulfillment.xml.GetCommandResponse;
import com.ericsson.fdp.business.response.fulfillment.xml.Offer;
import com.ericsson.fdp.business.response.fulfillment.xml.ProductBuy;
import com.ericsson.fdp.business.response.fulfillment.xml.ProductDetail;
import com.ericsson.fdp.business.response.fulfillment.xml.Provider;
import com.ericsson.fdp.business.response.fulfillment.xml.Providers;
import com.ericsson.fdp.business.response.fulfillment.xml.SharedAccount;
import com.ericsson.fdp.business.response.fulfillment.xml.Tariff;
import com.ericsson.fdp.business.response.fulfillment.xml.TariffEnquiry;
import com.ericsson.fdp.business.response.fulfillment.xml.Tariffs;
import com.ericsson.fdp.business.response.fulfillment.xml.ViewProduct;
import com.ericsson.fdp.business.tariffenquiry.configimport.constants.TariffConstants;
import com.ericsson.fdp.business.vo.FDPTariffEnquiryCsvAttributeNotificationMapVO;
import com.ericsson.fdp.common.constants.FDPConstant;
import com.ericsson.fdp.common.enums.DynamicMenuAdditionalInfoKey;
import com.ericsson.fdp.common.enums.ExternalSystem;
import com.ericsson.fdp.common.enums.ModuleType;
import com.ericsson.fdp.common.enums.Status;
import com.ericsson.fdp.common.enums.TariffEnquiryAttributeKeysEnum;
import com.ericsson.fdp.common.enums.TariffEnquiryOption;
import com.ericsson.fdp.common.util.DateUtil;
import com.ericsson.fdp.common.util.StringUtil;
import com.ericsson.fdp.core.cache.AppCacheSubStore;
import com.ericsson.fdp.core.command.FDPCommand;
import com.ericsson.fdp.core.command.param.CommandParam;
import com.ericsson.fdp.core.display.ResponseMessage;
import com.ericsson.fdp.core.entity.FDPTariffValues;
import com.ericsson.fdp.core.exception.ExecutionFailedException;
import com.ericsson.fdp.core.logging.FDPLogger;
import com.ericsson.fdp.core.node.FDPNode;
import com.ericsson.fdp.core.request.FDPRequest;
import com.ericsson.fdp.core.request.FDPResponse;
import com.ericsson.fdp.core.serviceprovisioning.bean.FDPAppBag;
import com.ericsson.fdp.core.serviceprovisioning.bean.FDPMetaBag;
import com.ericsson.fdp.core.utils.ApplicationConfigUtil;
import com.ericsson.fdp.dao.enums.ProductAdditionalInfoEnum;
import com.ericsson.fdp.dao.enums.SPNotificationType;

import ch.qos.logback.classic.Logger;

/**
 * This is the utility class for preparing FulFillment Service responses.
 * 
 * @author Ericsson
 * 
 */
public class FulfillmentResponseUtil {

	/**
	 * This method will send back the tariff enquiry response.
	 * 
	 * @param fdpRequest
	 * @return
	 * @throws ExecutionFailedException
	 */
	public static TariffEnquiry getTariffEnquiryResponse(final FDPRequest fdpRequest, final FDPResponse fdpResponse,
			final Logger logger) throws ExecutionFailedException {
		final TariffEnquiry tariffEnquiry = new TariffEnquiry();
		Tariffs tariffs = new Tariffs();
		Map<String, Tariff> tariffMap = prepareTariffFromRequest(fdpRequest, fdpResponse, logger);
		List<Tariff> tariffList = new ArrayList<Tariff>();
		for (final Map.Entry<String, Tariff> entry : tariffMap.entrySet()) {
			tariffList.add(entry.getValue());
		}
		tariffs.setTariffs(tariffList);
		tariffEnquiry.setTariffs(tariffs);
		return tariffEnquiry;
	}

	/**
	 * This method prepares the Tariff Enquiry Success Response.
	 * 
	 * @param fdpRequest
	 * @param fdpResponse
	 * @param logger
	 * @return
	 * @throws ExecutionFailedException
	 */
	@SuppressWarnings("unchecked")
	private static Map<String, Tariff> prepareTariffFromRequest(final FDPRequest fdpRequest,
			final FDPResponse fdpResponse, final Logger logger) throws ExecutionFailedException {
		Map<String, Tariff> tariffMap = new HashMap<String, Tariff>();
		FDPLogger.debug(logger, FulfillmentResponseUtil.class, "prepareTariffFromRequest()", "Entered for requestId:"
				+ fdpRequest.getRequestId());
		try {
			FDPMetadataResponseImpl metadataResponseImpl = (FDPMetadataResponseImpl) fdpResponse;
			if (null == metadataResponseImpl.getAuxiliaryRequestParameter(AuxRequestParam.TARIFF_VALUES)) {
				throw new ExecutionFailedException("Tariff Details not found in Metadata-response");
			}
			for (final Map<TariffEnquiryNotificationOptions, String> notificationValue : (List<Map<TariffEnquiryNotificationOptions, String>>) metadataResponseImpl
					.getAuxiliaryRequestParameter(AuxRequestParam.TARIFF_VALUES)) {
				if ((null != notificationValue)
						&& (null != notificationValue.get(TariffEnquiryNotificationOptions.TARIFF_TYPE))) {
					final String name = notificationValue.get(TariffEnquiryNotificationOptions.NAME);
					final String value = notificationValue.get(TariffEnquiryNotificationOptions.VALUE);
					final String validity = notificationValue.get(TariffEnquiryNotificationOptions.VALIDITY);
					final String attributeType = notificationValue.get(TariffEnquiryNotificationOptions.ATTRIBUTE_TYPE);
					final String tariffType = notificationValue.get(TariffEnquiryNotificationOptions.TARIFF_TYPE);
					final String attributeId = notificationValue.get(TariffEnquiryNotificationOptions.ATTRIBUTE_ID);
					FDPLogger.debug(logger, FulfillmentResponseUtil.class, "prepareTariffFromRequest()",
							"Processing for requestId:" + fdpRequest.getRequestId() + ", name:" + name + ", value:"
									+ value + ", validity:" + validity + ", attributeType:" + attributeType
									+ ", tariffType:" + tariffType + ", attributeId:" + attributeId);
					if (null == tariffMap.get(tariffType)) {
						addNewTariffValues(fdpRequest, logger, tariffMap, name, value, validity, attributeType,
								tariffType, attributeId, metadataResponseImpl);
					} else {
						getOldTariffValues(fdpRequest, logger, tariffMap, name, value, validity, attributeType,
								tariffType, attributeId, metadataResponseImpl);
					}
				}
			}
		} catch (NumberFormatException ne) {
			throw new ExecutionFailedException("Unable to parse into Integer", ne);
		}
		handleServiceClassNotPresentInDB(fdpRequest, tariffMap);
		return tariffMap;
	}

	/**
	 * This method populates the old existing Tariff details in response.
	 * 
	 * @param fdpRequest
	 * @param logger
	 * @param tariffMap
	 * @param name
	 * @param value
	 * @param validity
	 * @param attributeType
	 * @param tariffType
	 * @param attributeId
	 * @param fdpMetadataResponseImpl
	 * @throws ExecutionFailedException
	 */
	private static void getOldTariffValues(final FDPRequest fdpRequest, final Logger logger,
			Map<String, Tariff> tariffMap, final String name, final String value, final String validity,
			final String attributeType, final String tariffType, final String attributeId,
			final FDPMetadataResponseImpl fdpMetadataResponseImpl) throws ExecutionFailedException {
		Tariff tariff;
		tariff = tariffMap.get(tariffType);
		FDPLogger.debug(logger, FulfillmentResponseUtil.class, "getOldTariffValues()",
				"For requestId:" + fdpRequest.getRequestId() + ", found map for tariffType:" + tariffType);
		if (TariffEnquiryOption.SC.getOptionId().equals(attributeType)) {
			tariff.setMainTariff(getMainTariff(fdpRequest, tariffType, logger, attributeId, fdpMetadataResponseImpl));
		} else {
			Attributes attributes = tariff.getAttributes();
			if (null == attributes) {
				attributes = new Attributes();
				attributes.setAttributes(new ArrayList<Attribute>());
			}
			List<Attribute> list = attributes.getAttributes();
			Attribute attribute = getAttribute(name, value, validity, attributeType, attributeId);
			FDPLogger.debug(logger, FulfillmentResponseUtil.class, "getOldTariffValues()", "Adding attribute::"
					+ attribute + ", for requestId::" + fdpRequest.getRequestId());
			list.add(attribute);
		}
	}

	/**
	 * This method populates the new Tariff details in response.
	 * 
	 * @param fdpRequest
	 * @param logger
	 * @param tariffMap
	 * @param name
	 * @param value
	 * @param validity
	 * @param attributeType
	 * @param tariffType
	 * @param attributeId
	 * @param fdpMetadataResponseImpl
	 * @throws ExecutionFailedException
	 */
	private static void addNewTariffValues(final FDPRequest fdpRequest, final Logger logger,
			Map<String, Tariff> tariffMap, final String name, final String value, final String validity,
			final String attributeType, final String tariffType, final String attributeId,
			final FDPMetadataResponseImpl fdpMetadataResponseImpl) throws ExecutionFailedException {
		Tariff tariff;
		FDPLogger.debug(logger, FulfillmentResponseUtil.class, "addNewTariffValues()",
				"For requestId:" + fdpRequest.getRequestId() + ", creating map for tariffType:" + tariffType);
		tariff = new Tariff();
		tariff.setTariffType(tariffType);
		tariff.setMainTariff(getDefaultTariffFoundText(fdpRequest));
		if (TariffEnquiryOption.SC.getOptionId().equals(attributeType)) {
			tariff.setMainTariff(getMainTariff(fdpRequest, tariffType, logger, attributeId, fdpMetadataResponseImpl));
		} else {
			Attributes attributes = new Attributes();
			List<Attribute> attributeList = new ArrayList<Attribute>();
			Attribute attribute = getAttribute(name, value, validity, attributeType, attributeId);
			FDPLogger.debug(logger, FulfillmentResponseUtil.class, "addNewTariffValues()", "Adding attribute:"
					+ attribute + ", for requestId:" + fdpRequest.getRequestId());
			attributeList.add(attribute);
			attributes.setAttributes(attributeList);
			tariff.setAttributes(attributes);
		}
		tariffMap.put(tariffType, tariff);
	}

	/**
	 * This method prepares the tariff attributes.
	 * 
	 * @param notificationValue
	 * @return
	 * @throws ExecutionFailedException
	 */
	private static Attribute getAttribute(final String name, final String value, final String validity,
			final String attributeType, final String attributeId) throws ExecutionFailedException {
		Attribute attribute = new Attribute();
		attribute.setName(name);
		attribute.setValue(getValue(value));
		attribute.setValidity(getDateForXmlResponse(validity));
		attribute.setType(FulfillmentAttributesType.getFulfillmentAttributesType(attributeType));
		attribute.setId(Integer.parseInt(attributeId));
		return attribute;
	}

	/**
	 * This method will generate the Date from response.
	 * 
	 * @param dateString
	 * @return
	 * @throws ExecutionFailedException
	 */
	private static Date getDateForXmlResponse(final String dateString) throws ExecutionFailedException {
		Date responseDate = null;
		try {
			if(!StringUtil.isNullOrEmpty(dateString)) {
				responseDate = DateUtil.convertStringToCalendarDate(dateString, FDPConstant.DATE_PATTERN).getTime();
			}
		} catch (ParseException e) {
			//throw new ExecutionFailedException("Unable to parse from date string:" + dateString, e);
		}
		return responseDate;
	}

	/**
	 * This method will fetch the Main Tariff Details.
	 * 
	 * @param fdpRequest
	 * @param attributeType
	 * @param logger
	 * @param serviceClass
	 * @return
	 * @throws ExecutionFailedException
	 */
	@SuppressWarnings("unchecked")
	private static String getMainTariff(final FDPRequest fdpRequest, final String attributeType, final Logger logger,
			final String serviceClass, final FDPMetadataResponseImpl fdpMetadataResponseImpl)
			throws ExecutionFailedException {
		String mainTariff = null;
		String mainTariffStatus = null;
		TariffEnquiryAttributeKeysEnum attributeKeysEnum = TariffEnquiryAttributeKeysEnum
				.getEnumFromAttributeName(attributeType);
		if (null == attributeKeysEnum) {
			throw new ExecutionFailedException("Attribute key type not found for type:" + attributeType);
		}
		String key = TariffEnquiryUtil.createKey(TariffEnquiryOption.SC, logger, attributeKeysEnum);
		FDPTariffValues tariffValues = (FDPTariffValues) ApplicationConfigUtil.getMetaDataCache().getValue(
				new FDPMetaBag(fdpRequest.getCircle(), ModuleType.TARIFF_ENQUIRY_ATTRIBUTES, key));
		if (null == tariffValues) {
			throw new ExecutionFailedException("Tariff Cache not found for in database for key:" + key);
		}

		Map<String, Map<String, String>> valueMap = (Map<String, Map<String, String>>) tariffValues.getTariffValue();
		Map<String, String> valueNstatus = valueMap.get(serviceClass);
		if (null == valueNstatus) {
			throw new ExecutionFailedException("Main Tariff Rates not found in database against service class:"
					+ serviceClass);
		}
		mainTariffStatus = valueNstatus.get(TariffConstants.TARIFF_SERVICE_CLASS_MAP_STATUS);
		mainTariff = valueNstatus.get(TariffConstants.TARIFF_SERVICE_CLASS_MAP_VALUE);
		if (Status.FAILURE.name().equalsIgnoreCase(mainTariffStatus)) {
			FDPLogger.debug(logger, FulfillmentResponseUtil.class, "getMainTariff()",
					"Service class fail found in Tariff Details for serviceClass:" + serviceClass + ", requestID:"
							+ fdpRequest.getRequestId());
			mainTariff = getMainTariffFailureValue(mainTariff, fdpRequest, fdpMetadataResponseImpl, logger);
		}
		FDPLogger.debug(logger, FulfillmentResponseUtil.class, "getMainTariff()", "Returning main tariff:" + mainTariff
				+ ",for serviceclass:" + serviceClass + ", requestId:" + fdpRequest.getRequestId());
		return mainTariff;
	}

	/**
	 * This method prepares the main tariff failure notification text.
	 * 
	 * @param mainTariff
	 * @param fdpRequest
	 * @return
	 */
	@SuppressWarnings("unchecked")
	private static String getMainTariffFailureValue(String mainTariff, FDPRequest fdpRequest,
			final FDPMetadataResponseImpl fdpMetadataResponseImpl, final Logger circleLogger)
			throws ExecutionFailedException {
		String mainTariffNotificationText = null;
		Map<String, Integer> fdpAttributeWithNotification = (Map<String, Integer>) fdpMetadataResponseImpl
				.getAuxiliaryRequestParameter(AuxRequestParam.FAILURE_REASON);
		Long notificationIdFromCache = 0L;
		if (null != fdpAttributeWithNotification && fdpAttributeWithNotification.size() == 1) {
			for (String entrykey : fdpAttributeWithNotification.keySet()) {
				notificationIdFromCache = Long.valueOf(fdpAttributeWithNotification.get(entrykey).toString());
				break;
			}
		}
		Long notificationId = notificationIdFromCache - fdpRequest.getCircle().getCircleId();
		try {
			mainTariffNotificationText = NotificationUtil.createNotificationText(fdpRequest, notificationId,
					circleLogger);
		} catch (NotificationFailedException e) {
			throw new ExecutionFailedException("Unable to prepare Fail Main Tariff Notification Text");
		}
		return mainTariffNotificationText;
	}

	/**
	 * This method prepares the Buy-Product success-full response.
	 * 
	 * @param fdpRequest
	 * @param fdpResponse
	 * @param logger
	 * @return
	 * @throws ExecutionFailedException
	 */
	public static ProductBuy getProductBuyResponse(final FDPRequest fdpRequest, final FDPResponse fdpResponse,
			final Logger logger) throws ExecutionFailedException {
		ProductBuy productBuy = new ProductBuy();
		Integer amountCharged =0;
		final Product product = (Product) fdpRequest.getValueFromRequest(RequestMetaValuesKey.PRODUCT);
		if (null == product) {
			throw new ExecutionFailedException("Unable to find PRODUCT from request.");
		}
		ChargingValueImpl chargingValueImpl = (ChargingValueImpl) fdpRequest
				.getValueFromRequest(RequestMetaValuesKey.CHARGING_STEP);
		if (null != chargingValueImpl) {
			amountCharged = Integer.valueOf(chargingValueImpl.getChargingValue().toString());
		}
		productBuy.setProductId(product.getProductId());
		productBuy.setProductName(product.getProductName());
		productBuy.setProductType(product.getProductType().getName());
		if (amountCharged < 0) {
			amountCharged = -amountCharged;
		}

		Object me2uAmtTransfer = fdpRequest.getAuxiliaryRequestParameter(AuxRequestParam.ME2U_AMT_TO_TRANSFER);
		if (null != me2uAmtTransfer) {
			if(productBuy.getProductType().contains(FDPConstant.DATA2Share)) {
				Object selectedMe2uData2shrProd = fdpRequest.getAuxiliaryRequestParameter(AuxRequestParam.SELECTED_Me2UDATA2SHARE_PRODUCT);
				if(null != selectedMe2uData2shrProd) {
					Me2uProductDTO selectedData2shareProd = (Me2uProductDTO) selectedMe2uData2shrProd;
					if(Integer.parseInt(selectedData2shareProd.getAvailableBalance()) >= Integer.parseInt(me2uAmtTransfer.toString())) {
						productBuy.setMsisdn(fdpRequest.getIncomingSubscriberNumber().toString());
						productBuy.setBeneficiaryMsisdn(fdpRequest.getAuxiliaryRequestParameter(AuxRequestParam.ME2U_SUBSCRIBER_TO_CREDIT).toString());
						productBuy.setTransfer(Integer.parseInt((fdpRequest.getAuxiliaryRequestParameter(AuxRequestParam.ME2U_AMT_TO_TRANSFER).toString())));
						Object transactionalCharges = fdpRequest.getAuxiliaryRequestParameter(AuxRequestParam.TRANSACTIONAL_CHARGES);
						productBuy.setAmtCharged(Integer.parseInt(transactionalCharges==null?0+"":transactionalCharges.toString()));
					}
				}
				else {
					productBuy.setErrorDescription("Transfer amount is greater than available data");
				}
			}
			if(productBuy.getProductType().contains(FDPConstant.TIME2SHARE)) {
				productBuy.setMsisdn(fdpRequest.getIncomingSubscriberNumber().toString());
				productBuy.setBeneficiaryMsisdn(fdpRequest.getAuxiliaryRequestParameter(AuxRequestParam.ME2U_SUBSCRIBER_TO_CREDIT).toString());
				productBuy.setTransfer(Integer.parseInt((fdpRequest.getAuxiliaryRequestParameter(AuxRequestParam.ME2U_AMT_TO_TRANSFER).toString())));
			}
		}
		else 
			productBuy.setAmtCharged(amountCharged);
		
		final String suppressNotificationText = FulfillmentUtil.getNotificationIdForCustomFulfillmentNotificationText(fdpRequest, SPNotificationType.SUCCESS, LoggerUtil.getSummaryLoggerFromRequest(fdpRequest));
		final String notificationText = (null == suppressNotificationText) ? getResponseNotificationText(fdpResponse) : suppressNotificationText;
		productBuy.setNotification(notificationText);
		//updateForAccountAfterRefill(fdpRequest, productBuy);
		return productBuy;
	}

	/**
	 * Modified By evasaty
	 * 24/02/16
	 * artf702259 :  Refill command fails when Account before and After Flag are enabled. 
	 

	private static void updateForAccountAfterRefill(FDPRequest fdpRequest, ProductBuy productBuy) {
		StringBuilder resultText = new StringBuilder("");
		FDPCommand refillResponse = fdpRequest.getExecutedCommand(Command.REFILL.getCommandDisplayName());
		if(refillResponse != null){
			String responseText = ((AbstractCommand)refillResponse).getCommandResponse();
			String rsultText;
			try {
				Source xmlInput = new StreamSource(new StringReader(responseText));
				StringWriter stringWriter = new StringWriter();
				StreamResult xmlOutput = new StreamResult(stringWriter);
				TransformerFactory transformerFactory = TransformerFactory
						.newInstance();
				Transformer transformer = transformerFactory.newTransformer();
				transformer.setOutputProperty(OutputKeys.INDENT, "yes");
				transformer.transform(xmlInput, xmlOutput);
				System.out.println(xmlOutput.getWriter().toString());
				rsultText = xmlOutput.getWriter().toString();
			} catch (Exception e) {
				throw new RuntimeException(e); 
			}
			String[] lines = rsultText.split(System.getProperty("line.separator"));
			for(int i = 0; i < lines.length; i++){
				if(lines[i].contains("<name>accountAfterRefill</name>")){
					for(int count = 1; count != 0;){
						i++;
						if(lines[i].contains("</member>")){
							count--;
						}else if(lines[i].contains("<member>")){
							count++;
						}
						if(count!=0)
							resultText.append(lines[i]);
						
					}
					break;
				}
			}	
		}
		if(resultText.length() > 0){		
			productBuy.setAccountAfterRefill(resultText.toString());
		}
	}*/
	
	private static void updateForAccountAfterRefill(FDPRequest fdpRequest, ProductBuy productBuy) {
		FDPCommand executedCommand = fdpRequest.getExecutedCommand(Command.REFILL.getCommandDisplayName());
		if(executedCommand != null){
			List<Offer> offers = evaluateOffersDetails(executedCommand);
			if(null != offers && !offers.isEmpty()){
				productBuy.setAccountAfterRefill(new AccountAfterRefillDetails(offers));
			}
		}
	}
	
	/**
	 * This method will parse all offers details from response.
	 * 
	 * @param fdpCommand
	 * @param map
	 */
	private static List<Offer> evaluateOffersDetails(final FDPCommand executedCommand) {
		String pathkey = null;
        final String paramterName = "accountAfterRefill.offerInformationList";
        final String offerId  = "offerID";
        final String expiryDate = "expiryDateTime";
        final String startDate="startDateTime";
        List<Offer> offers = new ArrayList<Offer>();
        for (int i = 0; executedCommand.getOutputParam(pathkey = (paramterName + FDPConstant.PARAMETER_SEPARATOR
                + i + FDPConstant.PARAMETER_SEPARATOR +offerId)) != null; i++) {
            final String expiryDate_Value = paramterName + FDPConstant.PARAMETER_SEPARATOR
                    + i + FDPConstant.PARAMETER_SEPARATOR + expiryDate;
            final String startDate_value=paramterName+ FDPConstant.PARAMETER_SEPARATOR
                    + i + FDPConstant.PARAMETER_SEPARATOR + startDate;            
            
            final String userOfferId = executedCommand.getOutputParam(pathkey).getValue().toString();
            final String userOfferStartDate = ((GregorianCalendar)executedCommand.getOutputParam(startDate_value).getValue()).getTime().toString();
            final String userOfferExpiryDate = ((GregorianCalendar)executedCommand.getOutputParam(expiryDate_Value).getValue()).getTime().toString();
            offers.add(new Offer(userOfferId, userOfferStartDate, userOfferExpiryDate));
        }
        return offers;
	}

	/**
	 * This method will fetch the notification text from response.
	 * 
	 * @param fdpResponse
	 * @return
	 * @throws ExecutionFailedException
	 */
	public static String getResponseNotificationText(final FDPResponse fdpResponse) throws ExecutionFailedException {
		String notificationText = null;
		final List<ResponseMessage> messageList = fdpResponse.getResponseString();
		if(null != messageList) {
			for (final ResponseMessage message : messageList) {
				notificationText = message.getCurrDisplayText(DisplayArea.COMPLETE);
				break;
			}
		}
		return notificationText;
	}

	/**
	 * This method will generate the command successful response.
	 * 
	 * @param fdpRequest
	 * @param fdpResponse
	 * @param logger
	 * @return
	 * @throws ExecutionFailedException
	 */
	public static GetCommandResponse getCommandSuccessResponse(final FDPRequest fdpRequest,
			final FDPResponse fdpResponse, final Logger logger) throws ExecutionFailedException {
		GetCommandResponse commandResponse = new GetCommandResponse();
		FulfillmentRequestImpl fulfillmentRequestImpl = (FulfillmentRequestImpl) fdpRequest;
		String response = null;
		FDPCommand fdpCommand = fdpRequest.getExecutedCommand(fulfillmentRequestImpl.getIvrCommandEnum().getCommand()
				.getCommandDisplayName());
		if (null != fdpCommand && fdpCommand instanceof AbstractCommand) {
			AbstractCommand abstractCommand = (AbstractCommand) fdpCommand;
			response = abstractCommand.getCommandResponse();
			commandResponse.setType(abstractCommand.getCommandExecutionType().name());
			commandResponse.setName(fulfillmentRequestImpl.getIvrCommandEnum().getIvrName());
			prepareResponseCommandWise(commandResponse, fulfillmentRequestImpl.getIvrCommandEnum(), response,
					fdpCommand);
		} else {
			throw new ExecutionFailedException("Unable to get response for the command from fdpRequest");
		}
		return commandResponse;
	}

	/**
	 * This method will set the response.
	 * 
	 * @param commandResponse
	 * @param commandEnum
	 * @param commandXmlResponse
	 * @throws ExecutionFailedException
	 */
	private static void prepareResponseCommandWise(final GetCommandResponse commandResponse,
			final IVRCommandEnum commandEnum, final String commandXmlResponse, final FDPCommand fdpCommand)
			throws ExecutionFailedException {
		final String responseString = convertCommandResponseExecutionTypeWise(commandXmlResponse, commandEnum,
				fdpCommand);
		if (StringUtil.isNullOrEmpty(responseString)) {
			throw new ExecutionFailedException("Unable to convert xml response to string response.");
		}
		switch (commandEnum.getCommand()) {
		case GETACCOUNTDETAILS:
		case GET_BALANCE_AND_DATE:
		case UPDATE_OFFER:
		case DELETEOFFER:
		case SBB_UPDATE_OFFER:
		case SBB_DELETE_OFFER:
		case SBB_UPDATE_UCUT:
		case SBB_DELETE_UCUT:
			commandResponse.setMethodResponse(responseString);
			break;
		case GET_SERVICES_DETAILS_REQUEST:
			commandResponse.setGetServicesDtlsResponse(responseString);
			break;
		default:
			throw new ExecutionFailedException("Not a valid Command in request input parameter");
		}
	}

	/**
	 * This method will convert the response to string.
	 * 
	 * @param commandXmlResponse
	 * @param commandEnum
	 * @return
	 * @throws ExecutionFailedException
	 */
	private static String convertCommandResponseExecutionTypeWise(final String commandXmlResponse,
			final IVRCommandEnum commandEnum, final FDPCommand fdpCommand) throws ExecutionFailedException {
		String responeString = null;
		switch (fdpCommand.getCommandExecutionType()) {
		case UCIP:
		case RS:
		case ACIP:
			responeString = convertXmlTypeResponse(commandXmlResponse, commandEnum);
			break;
		default:
			throw new ExecutionFailedException("Not a valid command execution Type");
		}
		return responeString;
	}

	/**
	 * This method will convert xml response to string.
	 * 
	 * @param commandXmlResponse
	 * @param commandEnum
	 * @return
	 * @throws ExecutionFailedException
	 */
	private static String convertXmlTypeResponse(final String commandXmlResponse, final IVRCommandEnum commandEnum)
			throws ExecutionFailedException {
		String commandStringResponse = null;
		try {
			final JSONObject xmlJSONObj = XML.toJSONObject(commandXmlResponse);
			final JSONObject methodResponse = (JSONObject) xmlJSONObj.get(commandEnum.getResponseXmlTagName());
			commandStringResponse = XML.toString(methodResponse);
		} catch (final JSONException e) {
			throw new ExecutionFailedException("The input xml is not valid.", e);
		}
		return commandStringResponse;
	}

	/**
	 * This method will generate the response for shared account actions services.
	 * 
	 * @param fdpResponse
	 * @return
	 * @throws ExecutionFailedException
	 */
	public static SharedAccount getSharedAccountResponse(final FDPRequest fdpRequest , final FDPResponse fdpResponse) throws ExecutionFailedException {
		final FDPMetadataResponseImpl fdpMetadataResponseImpl = (FDPMetadataResponseImpl) fdpResponse;
		final FulfillmentRequestImpl fulfillmentRequestImpl = (FulfillmentRequestImpl) fdpRequest;
		final SharedAccount sharedAccount = new SharedAccount();
		if (!(FulfillmentActionTypes.VIEW_SHARED_USAGE.equals(fulfillmentRequestImpl.getActionTypes()) || FulfillmentActionTypes.VIEW_ALL_SHARED_USAGE
				.equals(fulfillmentRequestImpl.getActionTypes()))) {
			sharedAccount.setNotificationText(fdpMetadataResponseImpl.getResponseString().get(0)
					.getCurrDisplayText(DisplayArea.COMPLETE));
		}
		if ((!fulfillmentRequestImpl.getAutoApprove())
				&& (null != (fdpMetadataResponseImpl.getAuxiliaryRequestParameter(AuxRequestParam.SHARED_DBID)))) {
			final String sharedRID = fdpMetadataResponseImpl.getAuxiliaryRequestParameter(AuxRequestParam.SHARED_DBID).toString();
			sharedAccount.setSharedRID(sharedRID);
		}
		updateResponseWithSharedAccountUC(sharedAccount,fdpResponse,fulfillmentRequestImpl);
		updateConsumersList(sharedAccount,fdpResponse);
		updateProduerList(sharedAccount, fdpResponse);
		return sharedAccount;
	}

/**
	 * This method checks for the null value in attributes tags.
	 * 
	 * @param value
	 * @return
	 */
	private static String getValue(final String value) {
		String outputValue = null;
		if(!StringUtil.isNullOrEmpty(value)) {
			outputValue = value;
		}
		return outputValue;
	}
	
	/**
	 * This method add consumer details to the response.
	 * 
	 * @param sharedAccount
	 * @param fdpResponse
	 * @throws ExecutionFailedException
	 */
	private static void updateResponseWithSharedAccountUC(final SharedAccount sharedAccount,
			final FDPResponse fdpResponse, final FulfillmentRequestImpl fulfillmentRequestImpl)
			throws ExecutionFailedException {
		final FDPMetadataResponseImpl fdpMetadataResponseImpl = (FDPMetadataResponseImpl) fdpResponse;
		final Consumer consumer = (Consumer) fdpMetadataResponseImpl
				.getAuxiliaryRequestParameter(AuxRequestParam.VIEW_UCUT_RESPONSE);
		if (null != consumer
				&& FulfillmentActionTypes.VIEW_SHARED_USAGE.equals(fulfillmentRequestImpl.getActionTypes())) {
			final Consumers consumers = new Consumers();
			List<Consumer> consumerList = new ArrayList<Consumer>();
			consumerList.add(consumer);
			consumers.setConsumers(consumerList);
			sharedAccount.setConsumers(consumers);
		}

		if (null != consumer
				&& FulfillmentActionTypes.VIEW_ALL_SHARED_USAGE.equals(fulfillmentRequestImpl.getActionTypes())) {
			sharedAccount.setThreshold(consumer.getThreshold());
			sharedAccount.setUsage(consumer.getUsage());
			sharedAccount.setUnit(consumer.getUnit());
		}
	}
	
	/**
	 * This method add the consumer details in case of consumer list.
	 * 
	 * @param sharedAccount
	 * @param fdpResponse
	 * @throws ExecutionFailedException
	 */
	@SuppressWarnings("unchecked")
	private static void updateConsumersList(final SharedAccount sharedAccount, final FDPResponse fdpResponse)
			throws ExecutionFailedException {
		final FDPMetadataResponseImpl fdpMetadataResponseImpl = (FDPMetadataResponseImpl) fdpResponse;
		final Consumers consumers = new Consumers();
		final List<Consumer> consumerList = (List<Consumer>) fdpMetadataResponseImpl
				.getAuxiliaryRequestParameter(AuxRequestParam.CONSUMER_MSISDN_LIST);
		if (null != consumerList && consumerList.size() > 0) {
			consumers.setConsumers(consumerList);
			sharedAccount.setConsumers(consumers);
		}
	}
	
	/**
	 * This method create Main Tariff Not Found notification text.
	 * 
	 * @param fdpRequest
	 * @param fdpResponse
	 * @return
	 * @throws ExecutionFailedException
	 */
	@SuppressWarnings("unchecked")
	private static String getDefaultTariffNotFoundText(final FDPRequest fdpRequest)
			throws ExecutionFailedException {
		String tnfText = null;
		final Logger circleLogger = LoggerUtil.getSummaryLoggerFromRequest(fdpRequest);
		String key = AppCacheSubStore.TARIFF_ENQUIRY_CSV_ATTRIBUTES_NOTI_MAP.getSubStore();
		Map<String, FDPTariffEnquiryCsvAttributeNotificationMapVO> attributeMap = (Map<String, FDPTariffEnquiryCsvAttributeNotificationMapVO>) ApplicationConfigUtil
				.getApplicationConfigCache().getValue(
						new FDPAppBag(AppCacheSubStore.TARIFF_ENQUIRY_CSV_ATTRIBUTES_NOTI_MAP, key));
		if (null == attributeMap) {
			FDPLogger.debug(circleLogger, FulfillmentResponseUtil.class, "getTariffNotFoundText()",
					"CSV-Attribute Cache not Found.");
			throw new ExecutionFailedException("CSV-Attribute Cache not Found.");
		}
		FDPTariffEnquiryCsvAttributeNotificationMapVO attributeNotificationMapVO = attributeMap.get(TariffConstants.DEFAULT_TARIFF_NOT_FOUND);
		if (null == attributeNotificationMapVO) {
			FDPLogger.debug(circleLogger, FulfillmentResponseUtil.class, "getTariffNotFoundText()",
					"CSV-Attribute Cache value not found for Key:" + TariffConstants.DEFAULT_TARIFF_NOT_FOUND);
			throw new ExecutionFailedException("CSV-Attribute Cache value not found for Key:" + TariffConstants.DEFAULT_TARIFF_NOT_FOUND);
		}
		Long notificationId =  attributeNotificationMapVO.getNotificationId() - fdpRequest.getCircle().getCircleId();
		try {
			tnfText= NotificationUtil.createNotificationText(fdpRequest, notificationId, circleLogger);
		} catch (NotificationFailedException ne) {
			throw new ExecutionFailedException("Unable to create notification for Tariff Not Found notification",ne);
		}
		return (null == tnfText ? "Tariff Not Found":tnfText);
				}
	
	/**
	 * This method add tariff Enquiry respone for all the tariff option configured on the node.
	 * @param fdpRequest
	 * @param tariffMap
	 * @throws ExecutionFailedException
	 */
	private static void handleServiceClassNotPresentInDB(final FDPRequest fdpRequest, final Map<String, Tariff> tariffMap) throws ExecutionFailedException {
				final Logger circleLogger = LoggerUtil.getSummaryLoggerFromRequest(fdpRequest);
		Tariff tariff = null;
		final List<TariffEnquiryAttributeKeysEnum> tariffAttributeList = getDMConfiguredTariffOptions(fdpRequest, circleLogger);
		if(null != tariffAttributeList) {
			for(final TariffEnquiryAttributeKeysEnum tariffEnquiryAttributeKeysEnum : tariffAttributeList) {
				if(null == tariffMap.get(tariffEnquiryAttributeKeysEnum.getAttributeName())) {
					tariff = new Tariff();
					tariff.setTariffType(tariffEnquiryAttributeKeysEnum.getAttributeName());
					tariff.setMainTariff(getDefaultTariffNotFoundText(fdpRequest));
					tariffMap.put(tariffEnquiryAttributeKeysEnum.getAttributeName(), tariff);
				}
			}
		}
	}
	
	/**
	 * This method gets the configured tariff attributes option list for the requested NODE.
	 * 
	 * @param fdpRequest
	 * @param circleLogger
	 * @return
	 * @throws ExecutionFailedException
	 */
	@SuppressWarnings("unchecked")
	private static List<TariffEnquiryAttributeKeysEnum> getDMConfiguredTariffOptions(final FDPRequest fdpRequest,
			Logger circleLogger) throws ExecutionFailedException {
		List<TariffEnquiryAttributeKeysEnum> tariffEnquiryAttributeKeysEnums = null;
		FDPNode fdpNode = (FDPNode) fdpRequest.getValueFromRequest(RequestMetaValuesKey.NODE);
		if (null != fdpNode) {
			tariffEnquiryAttributeKeysEnums = (List<TariffEnquiryAttributeKeysEnum>) fdpNode
					.getAdditionalInfo(DynamicMenuAdditionalInfoKey.TARIFF_ENQUIRY_ADDITIONAL_INFO_DATA.name());
			if (null == tariffEnquiryAttributeKeysEnums || tariffEnquiryAttributeKeysEnums.size() == 0) {
				FDPLogger.debug(circleLogger, FulfillmentResponseUtil.class, "getDMConfiguredTariffOptions()", "TariffEnquiry Multiselect Combination not configured in DM");
				throw new ExecutionFailedException("TariffEnquiry Multiselect Combination not configured in DM");
			}
			} else {
			FDPLogger.debug(circleLogger, FulfillmentResponseUtil.class, "getDMConfiguredTariffOptions()",
					"Not able to get Node from request");
			throw new ExecutionFailedException("Not able to get Node from request");
			}
		FDPLogger.debug(circleLogger, FulfillmentResponseUtil.class, "getDMConfiguredTariffOptions()", "Configured TariffOption List:"
				+ tariffEnquiryAttributeKeysEnums);
		return tariffEnquiryAttributeKeysEnums;
	}
	
		/**
	 * This method add the consumer details in case of consumer list.
	 * 
	 * @param sharedAccount
	 * @param fdpResponse
	 * @throws ExecutionFailedException
	 */
	@SuppressWarnings("unchecked")
	private static void updateProduerList(final SharedAccount sharedAccount, final FDPResponse fdpResponse)
			throws ExecutionFailedException {
		final FDPMetadataResponseImpl fdpMetadataResponseImpl = (FDPMetadataResponseImpl) fdpResponse;
		final Providers providers = new Providers();
		final List<Provider> producerList = (List<Provider>) fdpMetadataResponseImpl
				.getAuxiliaryRequestParameter(AuxRequestParam.PROVIDER_MSISDN_LIST);
		if (null != producerList && producerList.size() > 0) {
			providers.setProducers(producerList);
			sharedAccount.setProducers(providers);
		}
	}
	
	@SuppressWarnings("unchecked")
	private static String getDefaultTariffFoundText(final FDPRequest fdpRequest)
			throws ExecutionFailedException {
		String tnfText = null;
		final Logger circleLogger = LoggerUtil.getSummaryLoggerFromRequest(fdpRequest);
		String key = AppCacheSubStore.TARIFF_ENQUIRY_CSV_ATTRIBUTES_NOTI_MAP.getSubStore();
		Map<String, FDPTariffEnquiryCsvAttributeNotificationMapVO> attributeMap = (Map<String, FDPTariffEnquiryCsvAttributeNotificationMapVO>) ApplicationConfigUtil
				.getApplicationConfigCache().getValue(
						new FDPAppBag(AppCacheSubStore.TARIFF_ENQUIRY_CSV_ATTRIBUTES_NOTI_MAP, key));
		if (null == attributeMap) {
			FDPLogger.debug(circleLogger, FulfillmentResponseUtil.class, "getTariffNotFoundText()",
					"CSV-Attribute Cache not Found.");
			throw new ExecutionFailedException("CSV-Attribute Cache not Found.");
		}
		FDPTariffEnquiryCsvAttributeNotificationMapVO attributeNotificationMapVO = attributeMap.get(TariffConstants.DEFAULT_TARIFF_NOT_FOUND);
		if (null == attributeNotificationMapVO) {
			FDPLogger.debug(circleLogger, FulfillmentResponseUtil.class, "getTariffNotFoundText()",
					"CSV-Attribute Cache value not found for Key:" + TariffConstants.DEFAULT_TARIFF_NOT_FOUND);
			throw new ExecutionFailedException("CSV-Attribute Cache value not found for Key:" + TariffConstants.DEFAULT_TARIFF_NOT_FOUND);
		}
		Long notificationId =  attributeNotificationMapVO.getNotificationId() - fdpRequest.getCircle().getCircleId();
		try {
			tnfText= NotificationUtil.createNotificationTextForTariff(fdpRequest, notificationId, circleLogger);
		} catch (NotificationFailedException ne) {
			throw new ExecutionFailedException("Unable to create notification for Tariff Not Found notification",ne);
		}
		return (null == tnfText ? "Tariff Not Found":tnfText);
	}
	
	/**
	 * This method prepares the balance enquiry response.
	 * 
	 * @param fdpRequest
	 * @return
	 * @throws ExecutionFailedException 
	 */
	@SuppressWarnings("unchecked")
	public static BalanceEnquiry getBalancesResponse(final FDPRequest fdpRequest, final FDPResponse fdpResponse) throws ExecutionFailedException {
		BalanceEnquiry balanceEnquiry = null; 
		final Map<String, String> balanceEqnuiryMap = (Map<String, String>) fdpRequest.getAuxiliaryRequestParameter(AuxRequestParam.MVEL_INPUT);
		if(null != balanceEqnuiryMap) {
			balanceEnquiry = new BalanceEnquiry();
			final Balances balances = new Balances();
			final String notificationText = getResponseNotificationText(fdpResponse);
			if(!StringUtil.isNullOrEmpty(notificationText)) {
				balanceEnquiry.setNotification(notificationText);
			}
			final List<Balance> balancelist = new ArrayList<Balance>();
			for(final Map.Entry<String, String> entry : balanceEqnuiryMap.entrySet()) {
				Balance balance = new Balance();
				balance.setBalanceKey(entry.getKey());
				balance.setBalanceValue(entry.getValue());
				balancelist.add(balance);
				balances.setBalance(balancelist);
			}
			balanceEnquiry.setBalances(balances);
		}
		return balanceEnquiry;
	}

	/**
	 * This method prepares the Bundle Price success response along with other product details.
	 * 
	 * @param fdpRequest
	 * @param fdpResponse
	 * @param logger
	 * @return
	 * @throws ExecutionFailedException
	 */
	public static ProductDetail getBundlePriceResponse(final FDPRequest fdpRequest, final FDPResponse fdpResponse,
			final Logger logger) throws ExecutionFailedException {
		ProductDetail productDetail = new ProductDetail();
		Long amountCharged =0L;
		final BaseProduct product = (BaseProduct) fdpRequest.getValueFromRequest(RequestMetaValuesKey.PRODUCT);
		productDetail.setId(product.getProductId());
		productDetail.setType(product.getProductType());
		ChargingValueImpl chargingValueImpl = (ChargingValueImpl) fdpRequest
				.getValueFromRequest(RequestMetaValuesKey.CHARGING_STEP);
		if (null != chargingValueImpl) {
			amountCharged = ((AIRCharging)chargingValueImpl.getChargingValue()).getChargingValue();
		}
		if (amountCharged < 0){
			amountCharged = (long) (amountCharged * FDPConstant.MINUS_ONE_LONG);
		}
		productDetail.setamount(amountCharged);
		productDetail.setName(product.getProductName());
		productDetail.setValidity(product.getRecurringOrValidityValue()+ FDPConstant.SPACE+ product.getRecurringOrValidityUnit());
		productDetail.setBuyForOther(product.getAdditionalInfo(ProductAdditionalInfoEnum.PRODUCT_FOR_OTHER));
		
		return productDetail;
	}

	public static List<ViewProduct> getProductsResponse (
			FDPRequest fdpRequest, FDPResponse fdpResponse, Logger circleLogger, ListProductDTO listProduct) throws ExecutionFailedException {
		
		List<ViewProduct> viewProductList = new ArrayList<ViewProduct>();
	
		List<ProductDTO> productDTOs = listProduct.getProductDTOList();
		
		if(productDTOs != null && !productDTOs.isEmpty()) {
			for(int i=0; i<productDTOs.size(); i++) {
				ProductDTO productDTO = productDTOs.get(i);
				if(productDTO != null){
					ViewProduct viewProduct = new ViewProduct();
					viewProduct.setProductName(productDTO.getProductName());
					viewProduct.setPrice(productDTO.getProductPrice());
					viewProduct.setPaymentMode(productDTO.getPaySrc());
					viewProduct.setChannel(productDTO.getSrcChannel());
					viewProduct.setActivationDate(productDTO.getActivationDate());
					viewProduct.setExpiryDate(productDTO.getExpiryDate());
					viewProduct.setActivatedBy(productDTO.getActivatedBy());
					viewProductList.add(viewProduct);
				}
				else {
					throw new ExecutionFailedException("Unable to find products");
				}
			}
		}
		
		return viewProductList;
	}
	
	/**
	 * This method prepares the Me2u data share success response along with product share details.
	 * 
	 * @param fdpRequest
	 * @param fdpResponse
	 * @param logger
	 * @return
	 * @throws ExecutionFailedException
	 */
	public static List<ViewProduct> getMe2uDataShareResponse(FDPRequest fdpRequest, FDPResponse fdpResponse, Logger circleLogger, ListMe2uDTO listProduct) throws ExecutionFailedException {

		List<ViewProduct> viewProductList = new ArrayList<ViewProduct>();
		
		List<Me2uProductDTO> listMe2uProduct = listProduct.getMe2uList();
		
		if (listMe2uProduct != null && !listMe2uProduct.isEmpty()) {
			for(int i=0; i<listMe2uProduct.size(); i++) {
				Me2uProductDTO me2uProductDTO = listMe2uProduct.get(i);
				if (me2uProductDTO != null) {
					ViewProduct viewProduct = new ViewProduct();
					Product product = me2uProductDTO.getProduct();
					viewProduct.setProductId(product.getProductId()+"");
					viewProduct.setProductName(product.getProductName());
					viewProduct.setAvailableBalance(me2uProductDTO.getAvailableBalance()+" MB");
					viewProduct.setExpiryDate(me2uProductDTO.getExpiryDateAPI());
					viewProduct.setProductCode(fdpRequest.getCircle().getCircleCode()+FDPConstant.UNDERSCORE+
							product.getProductType()+FDPConstant.UNDERSCORE+product.getProductId());
					viewProductList.add(viewProduct);
				}
				else {
					throw new ExecutionFailedException("Unable to find products");
				}
			}
		}
	
		return viewProductList;
	}
	
	/**
	 * 
	 * @param fdpRequest
	 * @param activeBundleCommand
	 * @return the ActiveBundle
	 * @throws ExecutionFailedException
	 */
	public static ActiveBundles getActiveBundlesResponse(FDPRequest fdpRequest , FDPCommand activeBundleCommand)throws ExecutionFailedException{
		ActiveBundles activeBundles = null;
		List<Bundle> bundlesList = null ;
		final JSONObject xmlJSONObj = XML.toJSONObject(((NonTransactionCommand)activeBundleCommand).getCommandResponse());
		final JSONObject methodResponse = (JSONObject) xmlJSONObj.get("GetActiveBundlesDetailsResponse");
		final JSONObject objectForMember = methodResponse.has("activebundles")?(JSONObject) methodResponse.get("activebundles"):null;
		
		if (objectForMember != null) {
			activeBundles = new ActiveBundles();
			
			
			final Object serviceObject = objectForMember.get("bundle");
			if (serviceObject instanceof JSONArray) {
				final JSONArray jsonArray = (JSONArray) serviceObject;
				bundlesList  = new ArrayList<>(jsonArray.length());
				
				for (int arrayIndex = 0; arrayIndex < jsonArray.length(); arrayIndex++) {
					final JSONObject jsonObject = (JSONObject) jsonArray.get(arrayIndex);				
					setRSCommandOutputParams( fdpRequest,bundlesList, jsonObject);
				}
			} else if (serviceObject instanceof JSONObject) {
				bundlesList = new ArrayList<>(1);
				final JSONObject jsonObject = (JSONObject) serviceObject;						
				setRSCommandOutputParams(fdpRequest , bundlesList,jsonObject);
			}
			if(bundlesList!=null)
				activeBundles.setBundles(bundlesList);
		}
		
		return activeBundles;
		
	}
	/**
	 * 
	 * @param fdpRequest
	 * @param bundlesList
	 * @param jsonObject
	 */
	private static void setRSCommandOutputParams(FDPRequest fdpRequest , List<Bundle> bundlesList , final JSONObject jsonObject) {
		Bundle bundle = new Bundle();		
		for(RSCommandOutputParamEnum rsCommandParam : RSCommandOutputParamEnum.values()){
			if(jsonObject.has(rsCommandParam.getParamName())){	
				if(rsCommandParam == RSCommandOutputParamEnum.SERVICENAME)
					bundle.setServiceName(jsonObject.get(rsCommandParam.getParamName()).toString());
				if(rsCommandParam == RSCommandOutputParamEnum.SERVICEDESCRIPTION)
					bundle.setServiceDescription(jsonObject.get(rsCommandParam.getParamName()).toString());
				if(rsCommandParam == RSCommandOutputParamEnum.SUBSCRIPTIONDATE)
					bundle.setSubscriptionDate(jsonObject.get(rsCommandParam.getParamName()).toString());
				if(rsCommandParam == RSCommandOutputParamEnum.SHORTCODE)
					bundle.setShortCode(jsonObject.get(rsCommandParam.getParamName()).toString());
				if(rsCommandParam == RSCommandOutputParamEnum.CHARGEMODE)
					bundle.setChargeMode(jsonObject.get(rsCommandParam.getParamName()).toString());
				if(rsCommandParam == RSCommandOutputParamEnum.PAYMENTCHANNEL)
					bundle.setPaymentChannel(jsonObject.get(rsCommandParam.getParamName()).toString());
				if(rsCommandParam == RSCommandOutputParamEnum.CHARGEAMOUNT){
					bundle.setChargeAmount(jsonObject.get(rsCommandParam.getParamName()).toString());
					if(bundle.getPaymentChannel() !=null && !bundle.getPaymentChannel().equalsIgnoreCase(ExternalSystem.Loyalty.toString())){
						String conversationFactor = fdpRequest.getCircle().getConfigurationKeyValueMap().get(ConfigurationKey.CS_CONVERSION_FACTOR.getAttributeName());
						if(conversationFactor!=null)
							bundle.setChargeAmount(Float.toString(Float.valueOf(jsonObject.get(rsCommandParam.getParamName()).toString())/ Long.parseLong(conversationFactor)));
						}

				}
				if(rsCommandParam == RSCommandOutputParamEnum.RENEWALDATE)
					bundle.setRenewalDate(jsonObject.get(rsCommandParam.getParamName()).toString());
			}

		}	
		bundlesList.add(bundle);
	}

}
