package com.ericsson.fdp.business.ivr.responsegenerator.impl;

import java.io.StringReader;
import java.io.StringWriter;
import java.util.List;
import java.util.Map;

import javax.xml.bind.JAXBException;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.w3c.dom.Document;
import org.xml.sax.InputSource;

import com.ericsson.fdp.FDPCacheable;
import com.ericsson.fdp.business.command.impl.NonTransactionCommand;
import com.ericsson.fdp.business.dto.ListMe2uDTO;
import com.ericsson.fdp.business.dto.ListProductDTO;
import com.ericsson.fdp.business.enums.AuxRequestParam;
import com.ericsson.fdp.business.enums.Command;
import com.ericsson.fdp.business.enums.RequestMetaValuesKey;
import com.ericsson.fdp.business.enums.ivr.FulfillmentActionTypes;
import com.ericsson.fdp.business.enums.ivr.FulfillmentResponseCodes;
import com.ericsson.fdp.business.enums.ivr.FulfillmentResponseTypes;
import com.ericsson.fdp.business.enums.ivr.FulfillmentStatus;
import com.ericsson.fdp.business.enums.ivr.FulfillmentSystemTypes;
import com.ericsson.fdp.business.enums.ivr.IVRResponseTypes;
import com.ericsson.fdp.business.ivr.responsegenerator.IVRResponseGenerator;
import com.ericsson.fdp.business.request.impl.FDPMetadataResponseImpl;
import com.ericsson.fdp.business.request.impl.FulfillmentRequestImpl;
import com.ericsson.fdp.business.response.fulfillment.xml.FulfillmentResponse;
import com.ericsson.fdp.business.response.fulfillment.xml.GetCommandResponse;
import com.ericsson.fdp.business.response.fulfillment.xml.ProductBuy;
import com.ericsson.fdp.business.response.fulfillment.xml.ProductDetail;
import com.ericsson.fdp.business.response.fulfillment.xml.Products;
import com.ericsson.fdp.business.response.fulfillment.xml.Response;
import com.ericsson.fdp.business.response.fulfillment.xml.ResponseData;
import com.ericsson.fdp.business.response.fulfillment.xml.SharedAccount;
import com.ericsson.fdp.business.response.fulfillment.xml.SubscribedProducts;
import com.ericsson.fdp.business.response.fulfillment.xml.TariffEnquiry;
import com.ericsson.fdp.business.response.fulfillment.xml.ViewProduct;
import com.ericsson.fdp.business.util.FulfillmentResponseUtil;
import com.ericsson.fdp.business.util.FulfillmentUtil;
import com.ericsson.fdp.business.util.LoggerUtil;
import com.ericsson.fdp.business.util.ServiceProvisioningUtil;
import com.ericsson.fdp.common.constants.FDPConstant;
import com.ericsson.fdp.common.enums.Status;
import com.ericsson.fdp.common.util.xml.XmlUtil;
import com.ericsson.fdp.common.vo.FDPCircle;
import com.ericsson.fdp.core.cache.AppCacheSubStore;
import com.ericsson.fdp.core.command.FDPCommand;
import com.ericsson.fdp.core.command.param.CommandParam;
import com.ericsson.fdp.core.exception.ExecutionFailedException;
import com.ericsson.fdp.core.logging.FDPLogger;
import com.ericsson.fdp.core.request.FDPRequest;
import com.ericsson.fdp.core.request.FDPResponse;
import com.ericsson.fdp.core.request.ResponseError;
import com.ericsson.fdp.core.serviceprovisioning.bean.FDPAppBag;
import com.ericsson.fdp.core.utils.ApplicationConfigUtil;
import com.ericsson.fdp.dao.enums.FDPServiceProvSubType;
import com.ericsson.fdp.dao.enums.SPNotificationType;

import ch.qos.logback.classic.Logger;

public class IVRResponseGeneratorImpl implements IVRResponseGenerator {

	@Override
	public String generateIVRResponse(final FDPRequest fdpRequest, final FDPResponse fdpResponse)
			throws ExecutionFailedException {
		final Logger circleLogger = LoggerUtil.getSummaryLoggerFromRequest(fdpRequest);
		FDPLogger.debug(circleLogger, getClass(), "generateIVRResponse()",
				"Entered with requestId:" + fdpRequest.getRequestId());
		checkRequestInstance(fdpRequest);
		FulfillmentRequestImpl fulfillmentRequest = (FulfillmentRequestImpl) fdpRequest;
		FulfillmentActionTypes actionTypes = fulfillmentRequest.getActionTypes();
		String xmlIvrResponse = null;
		if ((null == actionTypes)) {
			throw new ExecutionFailedException("Action Type not set in request for requestId:"
					+ fdpRequest.getRequestId());
		} else {
			FDPLogger.debug(circleLogger, getClass(), "generateIVRResponse()", "executing for fulfillmentServiceType:"
					+ actionTypes.getValue() + ", with request status:" + fdpRequest.getExecutionStatus()
					+ ", for requestId:" + fdpRequest.getRequestId());
			if (Status.SUCCESS.equals(fdpResponse.getExecutionStatus()) || Status.PENDING_ON_MM.equals(fdpResponse.getExecutionStatus())) {
				xmlIvrResponse = getIvrXmlResponseString(fdpRequest, fdpResponse, actionTypes.getSuccessResponse(),
						circleLogger);
			} else {
				xmlIvrResponse = getIvrXmlResponseString(fdpRequest, fdpResponse, actionTypes.getFailResponse(),
						circleLogger);
			}
			FDPLogger.debug(circleLogger, getClass(), "generateIVRResponse()", "Exiting with response :"
					+ xmlIvrResponse + ", for requestId:" + fdpRequest.getRequestId());
			FDPLogger.info(circleLogger, getClass(), "generateIVRResponse()", "CMDRESP:"
					+ xmlIvrResponse);
		}
		return xmlIvrResponse;
	}

	/**
	 * This method will prepare the IVR XML based on service type Response.
	 * 
	 * @param fdpRequest
	 * @param ivrResponseTypes
	 * @return
	 * @throws ExecutionFailedException
	 */
	private String getIvrXmlResponseString(final FDPRequest fdpRequest, final FDPResponse fdpResponse,
			final IVRResponseTypes ivrResponseTypes, final Logger circleLogger) throws ExecutionFailedException {
		FDPLogger.debug(circleLogger, getClass(), "getIvrXmlResponseString()",
				"Entered requestId:" + fdpRequest.getRequestId() + ", with ivrResponseTypes:" + ivrResponseTypes);
		FulfillmentResponse fulfillmentResponse = new FulfillmentResponse();
		fulfillmentResponse.setRequestId(fdpRequest.getRequestId());
		fulfillmentResponse.setSystemType(FulfillmentSystemTypes.CIS);
		String xmlIvrResponse = null;
		switch (ivrResponseTypes) {
		case TARIFF_SUCCESS_RESPONSE:
			xmlIvrResponse = prepareTariffEnquirySuccessResponse(fdpRequest, fdpResponse, circleLogger,
					fulfillmentResponse);
			break;
		case BALANCE_ENQUIRY_SUCCESS_RESPONSE:
			xmlIvrResponse = prepareBalanceCheckSuccessResponse(fdpRequest, fdpResponse, circleLogger,
					fulfillmentResponse);
			break;
		case RECURRING_SERVICE_SUCCESS_RESPONSE:
			xmlIvrResponse = prepareDeprovisionSuccessResponse(fdpRequest, fdpResponse, circleLogger,
					fulfillmentResponse);
			break;
		case PRODUCT_BUY_SUCCESS_RESPONSE:
			xmlIvrResponse = prepareProductBuySuccessResponse(fdpRequest, fdpResponse, circleLogger,
					fulfillmentResponse);
			break;
		case COMMAND_SUCCESS_RESPONSE:
			xmlIvrResponse = prepareCommandSuccessResponse(fdpRequest, fdpResponse, circleLogger, fulfillmentResponse);
			break;
		case RECURRING_SERVICE_FAIL_RESPONSE:
		case FULFILLMENT_FAIL_RESPONSE:
			xmlIvrResponse = prepareFulfillmentFailResponse(fdpRequest, circleLogger, fulfillmentResponse, fdpResponse);
			break;
		case ADD_SHARED_CONSUMER_SUCCESS_RESPONSE:
		case REMOVE_SHARED_CONSUMER_SUCCESS_RESPONSE:
		case VIEW_ALL_SHARED_CONSUMER_SUCCESS_RESPONSE:
		case VIEW_SHARED_USAGE_CONSUMER_SUCCESS_RESPONSE:
		case ACCEPT_SHARED_CONSUMER_SUCCESS_RESPONSE:
		case VIEW_ALL_SHARED_PROVIDER_SUCCESS_RESPONSE:
			xmlIvrResponse = prepareSharedAccountSuccessResponse(fdpRequest, circleLogger, fulfillmentResponse,
					fdpResponse);
			break;
		case PRODUCT_BUY_FAIL_RESPONSE:
			xmlIvrResponse = prepareFulfillmentProductFailResponse(fdpRequest, circleLogger, fulfillmentResponse,
					fdpResponse);
			break;
		case OFFLINE_NOTIFICAITON_SUCESS_RESPONSE:
			xmlIvrResponse = prepareOffLineNotificationSucessResponse(fdpRequest, fdpResponse, circleLogger,
					fulfillmentResponse);
			break;
		case VIEW_HISTORY:
			xmlIvrResponse = prepareViewHistorySuccessResponse(fdpRequest, fdpResponse, circleLogger,
					fulfillmentResponse);
			break;
		case OPT_IN_SUCCESS_RESPONSE:
			xmlIvrResponse = prepareOptInOptOutSuccessResponse(fdpRequest, fdpResponse, circleLogger,
					fulfillmentResponse, FulfillmentActionTypes.OPT_IN);
			break;
		case OPT_OUT_SUCCESS_RESPONSE:
			xmlIvrResponse = prepareOptInOptOutSuccessResponse(fdpRequest, fdpResponse, circleLogger,
					fulfillmentResponse, FulfillmentActionTypes.OPT_OUT);
			break;
		case OPT_IN_FAILURE_RESPONSE:
			xmlIvrResponse = prepareOptInOptOutFailureResponse(fdpRequest, fdpResponse, circleLogger,
					fulfillmentResponse, FulfillmentActionTypes.OPT_IN);
			break;
		case OPT_OUT_FAILURE_RESPONSE:
			xmlIvrResponse = prepareOptInOptOutFailureResponse(fdpRequest, fdpResponse, circleLogger,
					fulfillmentResponse, FulfillmentActionTypes.OPT_OUT);
			break;
		case BUNDLE_PRICE_SUCCESS_RESPONSE:
			xmlIvrResponse = prepareBundlePriceSuccessResponse(fdpRequest, fdpResponse, circleLogger,
					fulfillmentResponse, FulfillmentActionTypes.BUNDLE_PRICE);
			break;
		case BUNDLE_PRICE_FAILURE_RESPONSE:
			xmlIvrResponse = prepareBundlePriceFailureResponse(fdpRequest, fdpResponse, circleLogger,
					fulfillmentResponse, FulfillmentActionTypes.BUNDLE_PRICE);
			break;
		case VIEW_SUBS_HISTORY_SUCCESS_RESPONSE:
			xmlIvrResponse = prepareViewSubsHistorySuccessResponse(fdpRequest, fdpResponse, circleLogger,
					fulfillmentResponse);
			break;

		case FAF_SUCCESS_RESPONSE:
			xmlIvrResponse = prepareFAFActionSuccessResponse(fdpRequest, fdpResponse, circleLogger,
					fulfillmentResponse, FulfillmentActionTypes.FAF);
			break;

		case FAF_FAILURE_RESPONSE:
			xmlIvrResponse = prepareFAFActionFailureResponse(fdpRequest, fdpResponse, circleLogger,
					fulfillmentResponse, FulfillmentActionTypes.FAF);
			break;
			
		case ME2ULIST_SUCCESS_RESPONSE:
			xmlIvrResponse = prepareMe2uListSuccessResponse(fdpRequest, fdpResponse, circleLogger,
					fulfillmentResponse, FulfillmentActionTypes.ME2ULIST);
			break;
		case ABILITY_SUCCESS_RESPONSE:
			xmlIvrResponse = prepareAbilitySuccessResponse(fdpRequest, fdpResponse, circleLogger,
					fulfillmentResponse, FulfillmentActionTypes.ABILITY_SYNC_UP);
			break;
			
		case SIM_CHANGE_SUCCESS_RESPONSE:
			xmlIvrResponse = prepareHandsetBasedChargingFulfillmentSuccessResponse(fdpRequest, fdpResponse, circleLogger,
					fulfillmentResponse, FulfillmentActionTypes.SIM_CHANGE);
			
			break;
		case SIM_CHANGE_FAILURE_RESPONSE:
			xmlIvrResponse = prepareHandsetBasedChargingFulfillmentFailureResponse(fdpRequest, fdpResponse, circleLogger,
					fulfillmentResponse, FulfillmentActionTypes.SIM_CHANGE);
			break;
			
		case DEVICE_CHANGE_SUCCESS_RESPONSE:
		case DEVICE_CHANGE_FAILURE_RESPONSE:
			xmlIvrResponse = prepareHandsetBasedChargingFulfillmentFailureResponse(fdpRequest, fdpResponse, circleLogger,
					fulfillmentResponse, FulfillmentActionTypes.DEVICE_CHANGE);
			break;
		case VIEW_ACTIVE_BUNDLES_SUCCESS_RESPONSE:
			xmlIvrResponse = prepareActiveBundlesSuccessResponse(fdpRequest, fdpResponse, fulfillmentResponse);
			break;
		case VIEW_SUBS_HISTORY_FAILURE_RESPONSE:
			xmlIvrResponse = prepareActiveBundlesFailureResponse(fdpRequest, fdpResponse, fulfillmentResponse);
			break;
		default:
			throw new ExecutionFailedException("Not a valid fulfillment response type");
		}
		return xmlIvrResponse;
	}

	/**
	 * This method will return the bundle price success response.
	 * 
	 * @param fdpRequest
	 * @param fdpResponse
	 * @param circleLogger
	 * @param fulfillmentResponse
	 * @return
	 */
	private String prepareBundlePriceSuccessResponse(final FDPRequest fdpRequest, final FDPResponse fdpResponse,
			final Logger circleLogger, FulfillmentResponse fulfillmentResponse, FulfillmentActionTypes action)
			throws ExecutionFailedException {
		setResponseSuccessParameters(fulfillmentResponse);
		ResponseData responseData = new ResponseData();
		ProductDetail bundlePrice = null;
		responseData.setAction(FulfillmentActionTypes.BUNDLE_PRICE);
		responseData.setCircleCode(fdpRequest.getCircle().getCircleCode());
		responseData.setResponseType(FulfillmentResponseTypes.PRODUCT);
		bundlePrice = FulfillmentResponseUtil.getBundlePriceResponse(fdpRequest, fdpResponse, circleLogger);
		responseData.setProductDetail(bundlePrice);
		fulfillmentResponse.setResponseData(responseData);
		updateMsisdn(fdpRequest, fulfillmentResponse);
		return getXmlStringFromObject(fulfillmentResponse);
	}

	/**
	 * This method creates failure response for bundle price.
	 * 
	 * @param fdpRequest
	 * @param fdpResponse
	 * @param circleLogger
	 * @param fulfillmentResponse
	 * @param bundlePrice
	 * @return
	 * @throws ExecutionFailedException
	 */
	private String prepareBundlePriceFailureResponse(FDPRequest fdpRequest, FDPResponse fdpResponse,
			Logger circleLogger, FulfillmentResponse fulfillmentResponse, FulfillmentActionTypes bundlePrice)
			throws ExecutionFailedException {
		ResponseData responseData = new ResponseData();
		responseData.setAction(FulfillmentActionTypes.BUNDLE_PRICE);
		responseData.setInputCode(((FulfillmentRequestImpl) fdpRequest).getRequestString());
		Response response = new Response();
		response.setStatus(fdpResponse.getExecutionStatus().getStatusText());
		ResponseError errorResponse = (ResponseError) fdpResponse.getResponseError();
		//To set custom notification for SP.
		final String customNotificationtext = FulfillmentUtil.getCustomFulfillmentNotificationText(fdpRequest, circleLogger, fulfillmentResponse, fdpResponse);
		final String description = (null != customNotificationtext) ? customNotificationtext : errorResponse.getResponseErrorString(); 
		response.setDescription(description);
		responseData.setResponse(response);
		fulfillmentResponse.setResponseData(responseData);
		return getXmlStringFromObject(fulfillmentResponse);
	}

	private String prepareViewHistorySuccessResponse(FDPRequest fdpRequest, FDPResponse fdpResponse,
			Logger circleLogger, FulfillmentResponse fulfillmentResponse) throws ExecutionFailedException {
		FDPMetadataResponseImpl fdpMetadataResponse = (FDPMetadataResponseImpl) fdpResponse;
		ResponseData responseData = new ResponseData();
		responseData.setAction(FulfillmentActionTypes.VIEW_HISTORY);
		responseData.setCircleCode(fdpRequest.getCircle().getCircleCode());
		responseData.setResponseType(FulfillmentResponseTypes.PRODUCT);
		Object subscribedProductsListObj = fdpMetadataResponse
				.getAuxiliaryRequestParameter(AuxRequestParam.VIEW_HISTORY_OFFER_DETAILS);
		if (null != subscribedProductsListObj && subscribedProductsListObj instanceof SubscribedProducts) {
			SubscribedProducts subscribedProducts = (SubscribedProducts) subscribedProductsListObj;
			ProductBuy productBuy = new ProductBuy();
			productBuy.setSubscribedProducts(subscribedProducts);
			responseData.setProductBuy(productBuy);
		}
		setResponseSuccessParameters(fulfillmentResponse);
		fulfillmentResponse.setResponseData(responseData);
		updateMsisdn(fdpRequest, fulfillmentResponse);
		return getXmlStringFromObject(fulfillmentResponse);
	}

	/**
	 * This method generates the Optin/optout reponse xml in success
	 * 
	 * @param fdpRequest
	 * @param fdpResponse
	 * @param circleLogger
	 * @param fulfillmentResponse
	 * @param actionType
	 * @return
	 * @throws ExecutionFailedException
	 */
	private String prepareOptInOptOutSuccessResponse(FDPRequest fdpRequest, FDPResponse fdpResponse,
			Logger circleLogger, FulfillmentResponse fulfillmentResponse, FulfillmentActionTypes actionType)
			throws ExecutionFailedException {
		ResponseData responseData = new ResponseData();
		responseData.setAction(actionType);
		responseData.setInputCode(((FulfillmentRequestImpl) fdpRequest).getRequestString());
		Response response = new Response();
		response.setStatus(fdpResponse.getExecutionStatus().getStatusText());
		response.setDescription(((FDPMetadataResponseImpl) fdpResponse).getFulfillmentResponse());
		responseData.setResponse(response);
		fulfillmentResponse.setResponseData(responseData);
		return getXmlStringFromObject(fulfillmentResponse);
	}

	/**
	 * This method generates the Optin/optout reponse xml in failure
	 * 
	 * @param fdpRequest
	 * @param fdpResponse
	 * @param circleLogger
	 * @param fulfillmentResponse
	 * @param actionType
	 * @return
	 * @throws ExecutionFailedException
	 */
	private String prepareOptInOptOutFailureResponse(FDPRequest fdpRequest, FDPResponse fdpResponse,
			Logger circleLogger, FulfillmentResponse fulfillmentResponse, FulfillmentActionTypes actionType)
			throws ExecutionFailedException {
		ResponseData responseData = new ResponseData();
		responseData.setAction(actionType);
		responseData.setInputCode(((FulfillmentRequestImpl) fdpRequest).getRequestString());
		Response response = new Response();
		response.setStatus(fdpResponse.getExecutionStatus().getStatusText());
		//To set Custom Notification from SP.
		final String customNotificationText = FulfillmentUtil.getCustomFulfillmentNotificationText(fdpRequest, circleLogger, fulfillmentResponse, fdpResponse);
		final String description = (null != customNotificationText) ? customNotificationText : ((FDPMetadataResponseImpl) fdpResponse).getFulfillmentResponse();
		response.setDescription(description);
		responseData.setResponse(response);
		fulfillmentResponse.setResponseData(responseData);
		return getXmlStringFromObject(fulfillmentResponse);
	}

	private String prepareDeprovisionSuccessResponse(FDPRequest fdpRequest, FDPResponse fdpResponse,
			Logger circleLogger, FulfillmentResponse fulfillmentResponse) throws ExecutionFailedException {

		setResponseSuccessParameters(fulfillmentResponse);
		ResponseData responseData = new ResponseData();
		responseData.setAction(FulfillmentActionTypes.RS_DEPROVISION_PRODUCT);
		responseData.setCircleCode(fdpRequest.getCircle().getCircleCode());
		responseData.setInputCode(((FulfillmentRequestImpl) fdpRequest).getRequestString());
		responseData.setResponseType(FulfillmentResponseTypes.PRODUCT);
		/*
		 * final BalanceEnquiry balanceEnquiry =
		 * FulfillmentResponseUtil.getBalancesResponse(fdpRequest, fdpResponse);
		 * if(null != balanceEnquiry) {
		 * responseData.setBalanceEnquiry(balanceEnquiry); } else { ProductBuy
		 * productBuy =
		 * FulfillmentResponseUtil.getProductBuyResponse(fdpRequest,
		 * fdpResponse, circleLogger); responseData.setProductBuy(productBuy); }
		 */
		ProductBuy productBuy = FulfillmentResponseUtil.getProductBuyResponse(fdpRequest, fdpResponse, circleLogger);
		updateRefundAmt(fdpRequest, productBuy);
		responseData.setProductBuy(productBuy);
		fulfillmentResponse.setResponseData(responseData);
		updateMsisdn(fdpRequest, fulfillmentResponse);
		return getXmlStringFromObject(fulfillmentResponse);

	}

	/**
	 * This method updates the refund amount in response xml in case of refund
	 * 
	 * @param fdpRequest
	 * @param productBuy
	 */
	private void updateRefundAmt(FDPRequest fdpRequest, ProductBuy productBuy) {
		String refundAmt = "0";
		if (ServiceProvisioningUtil.isProductSpTypeValid(fdpRequest, FDPServiceProvSubType.RS_DEPROVISION_PRODUCT)) {
			FDPCommand refundCmd = fdpRequest.getExecutedCommand(Command.REFUND.getCommandDisplayName());
			if (null != refundCmd) {
				CommandParam amtRefundedParam = refundCmd.getInputParam("adjustmentAmountRelative");
				if (null != amtRefundedParam && null != amtRefundedParam.getValue()) {
					refundAmt = amtRefundedParam.getValue().toString();
				}
			}
		}
		productBuy.setAmtRefunded(refundAmt);
	}

	/**
	 * This method prepares the success full response for Tariff Enquiry.
	 * 
	 * @param fdpRequest
	 * @param circleLogger
	 * @param fulfillmentResponse
	 * @return
	 * @throws ExecutionFailedException
	 */
	private String prepareTariffEnquirySuccessResponse(final FDPRequest fdpRequest, final FDPResponse fdpResponse,
			final Logger circleLogger, FulfillmentResponse fulfillmentResponse) throws ExecutionFailedException {
		FDPLogger.debug(circleLogger, getClass(), "prepareTariffEnquirySuccessResponse()", "Entered with requestId:"
				+ fdpRequest.getRequestId());
		setResponseSuccessParameters(fulfillmentResponse);
		ResponseData responseData = new ResponseData();
		responseData.setAction(FulfillmentActionTypes.TARIFF_ENQUIRY);
		responseData.setCircleCode(fdpRequest.getCircle().getCircleCode());
		responseData.setInputCode(((FulfillmentRequestImpl) fdpRequest).getRequestString());
		responseData.setResponseType(FulfillmentResponseTypes.PRODUCT);
		TariffEnquiry tariffEnquiry = FulfillmentResponseUtil.getTariffEnquiryResponse(fdpRequest, fdpResponse,
				circleLogger);
		if (null == tariffEnquiry) {
			throw new ExecutionFailedException("Tariff-Enquiry not found");
		}
		responseData.setTariffEnquiry(tariffEnquiry);
		fulfillmentResponse.setResponseData(responseData);
		return getXmlStringFromObject(fulfillmentResponse);
	}

	/**
	 * This method prepares the tariff enquiry fail response.
	 * 
	 * @param fdpRequest
	 * @param circleLogger
	 * @param fulfillmentResponse
	 * @return
	 * @throws ExecutionFailedException
	 */
	private String prepareFulfillmentFailResponse(final FDPRequest fdpRequest, final Logger circleLogger,
			FulfillmentResponse fulfillmentResponse, final FDPResponse fdpResponse) throws ExecutionFailedException {
		String responseCode = null;
		String description = null;
		FulfillmentSystemTypes systemType = null;
		final ResponseError responseError = fdpResponse.getResponseError();
		String errorDescription = null;
		if (null != responseError) {
			responseCode = responseError.getResponseCode();
			if (null == responseError.getResponseErrorString() && "102".equals(responseCode)) {
				description = "Subscriber not found.";
			} else {
				description = (null != responseError.getResponseErrorString() ? responseError.getResponseErrorString()
						: FulfillmentResponseCodes.FDP_EXCEPTION.getDescription());
			}
			systemType = (null != FulfillmentSystemTypes.getFulfillmentSystemForValue(responseError.getSystemType()) ? FulfillmentSystemTypes
					.getFulfillmentSystemForValue(responseError.getSystemType()) : FulfillmentSystemTypes.CIS);
		//	fulfillmentResponse.setSystemType(systemType);
			fulfillmentResponse.setSystemType(systemType.equals(FulfillmentSystemTypes.FDP)?FulfillmentSystemTypes.CIS:systemType);
		} else {
			responseCode = String.valueOf(FulfillmentResponseCodes.FDP_EXCEPTION.getResponseCode());
			description = FulfillmentResponseCodes.FDP_EXCEPTION.getDescription();
		}
		//Setting Custom Notification Text.
		final String customNotificationText = FulfillmentUtil.getCustomFulfillmentNotificationText(fdpRequest, circleLogger, fulfillmentResponse, fdpResponse);
		description = (null != customNotificationText) ? customNotificationText : description;
		final String suppressNotificationText = FulfillmentUtil.getNotificationIdForCustomFulfillmentNotificationText(fdpRequest, SPNotificationType.FAILURE, LoggerUtil.getSummaryLoggerFromRequest(fdpRequest));
		//if(StringUtils.isBlank(description)){
			description = (null == suppressNotificationText) ? (FulfillmentResponseUtil.getResponseNotificationText(fdpResponse)==null?description:FulfillmentResponseUtil.getResponseNotificationText(fdpResponse)) : suppressNotificationText;
		//}
		errorDescription = new StringBuilder(FDPConstant.REQUEST_ID).append(FDPConstant.LOGGER_KEY_VALUE_DELIMITER)
				.append(fdpRequest.getRequestId()).append(FDPConstant.LOGGER_KEY_VALUE_DELIMITER).append("ERROR_CODE")
				.append(FDPConstant.LOGGER_KEY_VALUE_DELIMITER).append(responseCode.toString())
				.append(FDPConstant.LOGGER_KEY_VALUE_DELIMITER).append(description).toString();
		fulfillmentResponse.setDescription(description);
		fulfillmentResponse.setResponseCode(responseCode);
		fulfillmentResponse.setStatus(FulfillmentStatus.FAILURE);
		FDPLogger.error(circleLogger, getClass(), "prepareFulfillmentFailResponse()", errorDescription);
		return getXmlStringFromObject(fulfillmentResponse);
	}

	/**
	 * This method generates Object to xml String.
	 * 
	 * @param fulfillmentResponse
	 * @return
	 * @throws ExecutionFailedException
	 */
	private String getXmlStringFromObject(final FulfillmentResponse fulfillmentResponse)
			throws ExecutionFailedException {
		String xmlString = null;
		try {
			xmlString = XmlUtil.getXmlUsingMarshaller(fulfillmentResponse);
		} catch (JAXBException e) {
			throw new ExecutionFailedException("Unable to convert into XML using JAXB.", e);
		}
		return xmlString;
	}

	/**
	 * This method will return the product buy success response.
	 * 
	 * @param fdpRequest
	 * @param circleLogger
	 * @param fulfillmentResponse
	 * @return
	 * @throws ExecutionFailedException
	 */
	private String prepareProductBuySuccessResponse(final FDPRequest fdpRequest, final FDPResponse fdpResponse,
			final Logger circleLogger, FulfillmentResponse fulfillmentResponse) throws ExecutionFailedException {
		setResponseSuccessParameters(fulfillmentResponse);
		ResponseData responseData = new ResponseData();
		Object me2uAmtTransfer = fdpRequest.getAuxiliaryRequestParameter(AuxRequestParam.ME2U_AMT_TO_TRANSFER);
		if(null != me2uAmtTransfer)
			responseData.setAction(FulfillmentActionTypes.PRODUCT_TRANSFER);
		else
			responseData.setAction(FulfillmentActionTypes.PRODUCT_BUY);
		responseData.setCircleCode(fdpRequest.getCircle().getCircleCode());
		responseData.setInputCode(((FulfillmentRequestImpl) fdpRequest).getRequestString());
		responseData.setResponseType(FulfillmentResponseTypes.PRODUCT);
		/*
		 * final BalanceEnquiry balanceEnquiry =
		 * FulfillmentResponseUtil.getBalancesResponse(fdpRequest, fdpResponse);
		 * if(null != balanceEnquiry) {
		 * responseData.setBalanceEnquiry(balanceEnquiry); } else { ProductBuy
		 * productBuy =
		 * FulfillmentResponseUtil.getProductBuyResponse(fdpRequest,
		 * fdpResponse, circleLogger); responseData.setProductBuy(productBuy); }
		 */
		ProductBuy productBuy = FulfillmentResponseUtil.getProductBuyResponse(fdpRequest, fdpResponse, circleLogger);
		responseData.setProductBuy(productBuy);
		fulfillmentResponse.setResponseData(responseData);
		updateMsisdn(fdpRequest, fulfillmentResponse);
		return getXmlStringFromObject(fulfillmentResponse);
	}

	/**
	 * This method will generate the command execute successful response.
	 * 
	 * @param fdpRequest
	 * @param fdpResponse
	 * @param circleLogger
	 * @param fulfillmentResponse
	 * @return
	 * @throws ExecutionFailedException
	 */
	private String prepareCommandSuccessResponse(final FDPRequest fdpRequest, final FDPResponse fdpResponse,
			final Logger circleLogger, FulfillmentResponse fulfillmentResponse) throws ExecutionFailedException {
		setResponseSuccessParameters(fulfillmentResponse);
		ResponseData responseData = new ResponseData();
		responseData.setAction(FulfillmentActionTypes.GET_COMMANDS);
		responseData.setCircleCode(fdpRequest.getCircle().getCircleCode());
		responseData.setInputCode(((FulfillmentRequestImpl) fdpRequest).getRequestString());
		responseData.setResponseType(FulfillmentResponseTypes.OTHERS);
		GetCommandResponse commandResponse = FulfillmentResponseUtil.getCommandSuccessResponse(fdpRequest, fdpResponse,
				circleLogger);
		responseData.setGetCommandResponse(commandResponse);
		fulfillmentResponse.setResponseData(responseData);
		return getXmlStringFromObject(fulfillmentResponse);
	}

	/**
	 * This method set the response parameters needed for successfull response.
	 * 
	 * @param fulfillmentResponse
	 */
	private void setResponseSuccessParameters(final FulfillmentResponse fulfillmentResponse) {
		fulfillmentResponse.setDescription(FulfillmentResponseCodes.SUCCESS.getDescription());
		fulfillmentResponse.setResponseCode(String.valueOf(FulfillmentResponseCodes.SUCCESS.getResponseCode()));
		fulfillmentResponse.setStatus(FulfillmentStatus.SUCCESS);
		fulfillmentResponse.setSystemType(FulfillmentSystemTypes.CIS);
	}

	/**
	 * This method checks the request instance .
	 * 
	 * @param fdpRequest
	 * @throws ExecutionFailedException
	 */
	private void checkRequestInstance(final FDPRequest fdpRequest) throws ExecutionFailedException {
		if (!(fdpRequest instanceof FulfillmentRequestImpl)) {
			throw new ExecutionFailedException("Request is not of FulfillmentRequestImpl Type.");
		}
	}

	/**
	 * This method prepares the shared account success response.
	 * 
	 * @param fdpResponse
	 * @param logger
	 * @param fulfillmentResponse
	 * @return
	 * @throws ExecutionFailedException
	 */
	private String prepareSharedAccountSuccessResponse(final FDPRequest fdpRequest, final Logger logger,
			final FulfillmentResponse fulfillmentResponse, final FDPResponse fdpResponse)
			throws ExecutionFailedException {
		final FulfillmentRequestImpl fulfillmentRequestImpl = (FulfillmentRequestImpl) fdpRequest;
		setResponseSuccessParameters(fulfillmentResponse);
		final ResponseData responseData = new ResponseData();
		responseData.setResponseType(FulfillmentResponseTypes.PRODUCT);
		responseData.setAction(fulfillmentRequestImpl.getActionTypes());
		responseData.setInputCode(fulfillmentRequestImpl.getRequestString());
		responseData.setCircleCode(fulfillmentRequestImpl.getCircle().getCircleCode());
		final SharedAccount sharedAccount = FulfillmentResponseUtil.getSharedAccountResponse(fdpRequest, fdpResponse);
		responseData.setSharedAccount(sharedAccount);
		fulfillmentResponse.setResponseData(responseData);
		return getXmlStringFromObject(fulfillmentResponse);
	}

	/**
	 * This method prepares the tariff enquiry fail response.
	 * 
	 * @param fdpRequest
	 * @param circleLogger
	 * @param fulfillmentResponse
	 * @return
	 * @throws ExecutionFailedException
	 */
	private String prepareFulfillmentProductFailResponse(final FDPRequest fdpRequest, final Logger circleLogger,
			FulfillmentResponse fulfillmentResponse, final FDPResponse fdpResponse) throws ExecutionFailedException {
		fulfillmentGenericFailResponse(fdpRequest, circleLogger, fulfillmentResponse, fdpResponse);
		updateMsisdn(fdpRequest, fulfillmentResponse);
		updateProductID(fdpRequest, fulfillmentResponse);
		return getXmlStringFromObject(fulfillmentResponse);
	}

	/**
	 * This method updates the msisdn Id in productBuy response from
	 * FDP-OFFLine.
	 * 
	 * @param fdpRequest
	 * @param productBuy
	 * @throws ExecutionFailedException
	 */
	private static void updateMsisdn(final FDPRequest fdpRequest, final FulfillmentResponse fulfillmentResponse)
			throws ExecutionFailedException {
		final FulfillmentRequestImpl fulfillmentRequest = (FulfillmentRequestImpl) fdpRequest;
		if (null != fulfillmentRequest.getTransaction_id()) {
			fulfillmentResponse.setmsisdn(fulfillmentRequest.getConsumerMsisdn());
		}
	}

	/**
	 * This method updates the product id in productBuy response from
	 * FDP-OFFLine.
	 * 
	 * @param fdpRequest
	 * @param productBuy
	 * @throws ExecutionFailedException
	 */
	private static void updateProductID(final FDPRequest fdpRequest, final FulfillmentResponse fulfillmentResponse)
			throws ExecutionFailedException {
		final FulfillmentRequestImpl fulfillmentRequest = (FulfillmentRequestImpl) fdpRequest;
		if (null != fulfillmentRequest.getTransaction_id()) {
			fulfillmentResponse.setProduct_id(fulfillmentRequest.getRequestString());
		}
	}

	/**
	 * This method generates fulfillement generic fail response.
	 * 
	 * @param fdpRequest
	 * @param circleLogger
	 * @param fulfillmentResponse
	 * @param fdpResponse
	 * @throws ExecutionFailedException
	 */
	private static void fulfillmentGenericFailResponse(final FDPRequest fdpRequest, final Logger circleLogger,
			FulfillmentResponse fulfillmentResponse, final FDPResponse fdpResponse) throws ExecutionFailedException {
		String responseCode = null;
		String description = null;
		FulfillmentSystemTypes systemType = null;
		final ResponseError responseError = fdpResponse.getResponseError();
		if (null != responseError) {
			responseCode = responseError.getResponseCode();
			description = (null != responseError.getResponseErrorString() ? responseError.getResponseErrorString()
					: FulfillmentResponseCodes.FDP_EXCEPTION.getDescription());
			if (FulfillmentSystemTypes.getFulfillmentSystemForValue(responseError.getSystemType()) != null
					&& !FulfillmentSystemTypes.getFulfillmentSystemForValue(responseError.getSystemType()).equals(
							FulfillmentSystemTypes.FDP))

			{
				systemType = (null != FulfillmentSystemTypes
						.getFulfillmentSystemForValue(responseError.getSystemType()) ? FulfillmentSystemTypes
						.getFulfillmentSystemForValue(responseError.getSystemType()) : FulfillmentSystemTypes.CIS);
			} else {
				systemType = (null != FulfillmentSystemTypes
						.getFulfillmentSystemForValue(responseError.getSystemType()) ? FulfillmentSystemTypes.CIS
						: FulfillmentSystemTypes.CIS);
			}
			fulfillmentResponse.setSystemType(systemType.equals(FulfillmentSystemTypes.FDP)?FulfillmentSystemTypes.CIS:systemType);
		} else {
			responseCode = String.valueOf(FulfillmentResponseCodes.FDP_EXCEPTION.getResponseCode());
			description = FulfillmentResponseCodes.FDP_EXCEPTION.getDescription();
		}
		final String suppressNotificationText = FulfillmentUtil.getNotificationIdForCustomFulfillmentNotificationText(fdpRequest,
				SPNotificationType.FAILURE, LoggerUtil.getSummaryLoggerFromRequest(fdpRequest));
		description = (null == suppressNotificationText) ? FulfillmentResponseUtil.getResponseNotificationText(fdpResponse) : suppressNotificationText;
		fulfillmentResponse.setDescription(description);
		fulfillmentResponse.setResponseCode(responseCode);
		fulfillmentResponse.setStatus(FulfillmentStatus.FAILURE);
	}

	/**
	 * This method will send off-line notification success response.
	 * 
	 * @param fdpRequest
	 * @param fdpResponse
	 * @param circleLogger
	 * @param fulfillmentResponse
	 * @return
	 * @throws ExecutionFailedException
	 */
	private String prepareOffLineNotificationSucessResponse(final FDPRequest fdpRequest, final FDPResponse fdpResponse,
			final Logger circleLogger, FulfillmentResponse fulfillmentResponse) throws ExecutionFailedException {
		setResponseSuccessParameters(fulfillmentResponse);
		ResponseData responseData = new ResponseData();
		responseData.setAction(FulfillmentActionTypes.SEND_OFFLINE_NOTIFICATION);
		responseData.setCircleCode(fdpRequest.getCircle().getCircleCode());
		responseData.setInputCode(((FulfillmentRequestImpl) fdpRequest).getRequestString());
		responseData.setResponseType(FulfillmentResponseTypes.OTHERS);
		fulfillmentResponse.setResponseData(responseData);
		return getXmlStringFromObject(fulfillmentResponse);
	}

	/**
	 * This method will return the view subscriber history success response.
	 * 
	 * @param fdpRequest
	 * @param circleLogger
	 * @param fulfillmentResponse
	 * @return
	 * @throws ExecutionFailedException
	 */
	private String prepareViewSubsHistorySuccessResponse(final FDPRequest fdpRequest, final FDPResponse fdpResponse,
			final Logger circleLogger, FulfillmentResponse fulfillmentResponse) throws ExecutionFailedException {
		setResponseSuccessParameters(fulfillmentResponse);
		ResponseData responseData = new ResponseData();
		responseData.setAction(FulfillmentActionTypes.VIEW_SUBS_HISTORY);
		responseData.setCircleCode(fdpRequest.getCircle().getCircleCode());
		responseData.setInputCode(((FulfillmentRequestImpl) fdpRequest).getRequestString());
		responseData.setResponseType(FulfillmentResponseTypes.PRODUCT);

		ListProductDTO listProduct = null;
		List<ViewProduct> viewProductsList = null;
		FDPCacheable listProductDTO = fdpRequest.getValueFromRequest(RequestMetaValuesKey.PRODUCTLIST);
		if (listProductDTO instanceof ListProductDTO) {
			listProduct = (ListProductDTO) listProductDTO;
			viewProductsList = FulfillmentResponseUtil.getProductsResponse(fdpRequest, fdpResponse, circleLogger,
					listProduct);
		} else {
			throw new ExecutionFailedException("Unable to find products");
		}

		// Getting Service class name from serviceClassId
		String serviceClassNo = listProduct.getServiceClass();
		final FDPCircle fdpCircleWithCSAttr = (FDPCircle) ApplicationConfigUtil.getApplicationConfigCache().getValue(
				new FDPAppBag(AppCacheSubStore.CS_ATTRIBUTES, fdpRequest.getCircle().getCircleCode()));
		final Map<String, Map<String, String>> csAttrMap = fdpCircleWithCSAttr.getCsAttributesKeyValueMap();
		final Map<String, String> csAttrClass = csAttrMap
				.get("com.ericsson.fdp.beans.profile.AccountDetailsBean.serviceClassOriginal");
		if (csAttrMap != null) {
			String serviceClassDisplayName = csAttrClass.get(serviceClassNo);
			if (serviceClassDisplayName != null)
				serviceClassNo = serviceClassDisplayName;
		}
		responseData.setServiceClass(serviceClassNo);
		Products products = new Products();
		products.setProductDetails(viewProductsList);
		responseData.setProducts(products);
		fulfillmentResponse.setResponseData(responseData);

		return getXmlStringFromObject(fulfillmentResponse);
	}

	/**
	 * The method will return FAF Failure response
	 * @param fdpRequest
	 * @param fdpResponse
	 * @param circleLogger
	 * @param fulfillmentResponse
	 * @param fafAction
	 * @return
	 * @throws ExecutionFailedException
	 */
	private String prepareFAFActionFailureResponse(FDPRequest fdpRequest, FDPResponse fdpResponse, Logger circleLogger,
			FulfillmentResponse fulfillmentResponse, FulfillmentActionTypes fafAction) throws ExecutionFailedException {
		ResponseData responseData = new ResponseData();
		responseData.setAction(FulfillmentActionTypes.FAF);
		responseData.setInputCode(((FulfillmentRequestImpl) fdpRequest).getRequestString());
		Response response = new Response();
		response.setStatus(fdpResponse.getExecutionStatus().getStatusText());
		ResponseError errorResponse = (ResponseError) fdpResponse.getResponseError();
		//To get custom notification text from SP.
		final String customNotificationText = FulfillmentUtil.getCustomFulfillmentNotificationText(fdpRequest, circleLogger, fulfillmentResponse, fdpResponse);
		final String description = (null != customNotificationText) ? customNotificationText : errorResponse.getResponseErrorString(); 
		response.setDescription(description);
		responseData.setResponse(response);
		fulfillmentResponse.setResponseData(responseData);
		return getXmlStringFromObject(fulfillmentResponse);
	}

	/**
	 * This method will return FAF Success response.
	 * @param fdpRequest
	 * @param fdpResponse
	 * @param circleLogger
	 * @param fulfillmentResponse
	 * @param fafAction
	 * @return
	 * @throws ExecutionFailedException
	 */
	private String prepareFAFActionSuccessResponse(FDPRequest fdpRequest, FDPResponse fdpResponse, Logger circleLogger,
			FulfillmentResponse fulfillmentResponse, FulfillmentActionTypes fafAction) throws ExecutionFailedException {

		setResponseSuccessParameters(fulfillmentResponse);
		ResponseData responseData = new ResponseData();
		responseData.setAction(FulfillmentActionTypes.FAF);
		responseData.setInputCode(((FulfillmentRequestImpl) fdpRequest).getRequestString());
		responseData.setCircleCode(fdpRequest.getCircle().getCircleCode());
		responseData.setResponseType(FulfillmentResponseTypes.PRODUCT);
		
		responseData.setFafMsisdn((String) fdpRequest.getAuxiliaryRequestParameter(AuxRequestParam.FAF_NUMBER));
		responseData.setMsisdn(Long.toString(fdpRequest.getIncomingSubscriberNumber()));
		final String notificationText = FulfillmentResponseUtil.getResponseNotificationText(fdpResponse);
		responseData.setNotification(notificationText);

		fulfillmentResponse.setResponseData(responseData);

		return getXmlStringFromObject(fulfillmentResponse);
	}
	
	/**
	 * This method generates the Me2uList reponse xml in success
	 * @param fdpRequest
	 * @param fdpResponse
	 * @param circleLogger
	 * @param fulfillmentResponse
	 * @param actionType
	 * @return
	 * @throws ExecutionFailedException
	 */
	private String prepareMe2uListSuccessResponse(final FDPRequest fdpRequest, final FDPResponse fdpResponse,
			final Logger circleLogger, FulfillmentResponse fulfillmentResponse, FulfillmentActionTypes action) throws ExecutionFailedException {
		setResponseSuccessParameters(fulfillmentResponse);
		ResponseData responseData = new ResponseData();
		responseData.setAction(FulfillmentActionTypes.ME2ULIST);
		responseData.setCircleCode(fdpRequest.getCircle().getCircleCode());
		responseData.setResponseType(FulfillmentResponseTypes.PRODUCT);
		ListMe2uDTO listProduct = null;
		List<ViewProduct> viewProductsList = null;
		FDPCacheable listProductDTO = fdpRequest.getValueFromRequest(RequestMetaValuesKey.ME2U_PRODUCT_LIST);
		if(listProductDTO instanceof ListMe2uDTO){
			listProduct = (ListMe2uDTO) listProductDTO;
			viewProductsList = FulfillmentResponseUtil.getMe2uDataShareResponse(fdpRequest, fdpResponse, circleLogger, listProduct);
		}
		else{
			throw new ExecutionFailedException("Unable to find products");
		}
		
		Products products = new Products();
		ProductBuy productBuy = new ProductBuy();
		
		if(null != viewProductsList && !viewProductsList.isEmpty()) {
			products.setProductDetails(viewProductsList);
		}
		else {
			productBuy.setNotification("No activated shared data bundle");
		}
		
		productBuy.setProducts(products);
		responseData.setProductBuy(productBuy);
		fulfillmentResponse.setResponseData(responseData);
		
		return getXmlStringFromObject(fulfillmentResponse);
	}

	
	private String prepareAbilitySuccessResponse(final FDPRequest fdpRequest, final FDPResponse fdpResponse,
			final Logger circleLogger, FulfillmentResponse fulfillmentResponse, FulfillmentActionTypes action) throws ExecutionFailedException {
		setResponseSuccessParameters(fulfillmentResponse);
		ResponseData responseData = new ResponseData();
		responseData.setAction(FulfillmentActionTypes.ABILITY_SYNC_UP);
		responseData.setCircleCode(fdpRequest.getCircle().getCircleCode());
		fulfillmentResponse.setResponseData(responseData);
		
		return getXmlStringFromObject(fulfillmentResponse);
	}
	
	/**
	 * This method will return the balance Check success response.
	 * 
	 * @param fdpRequest
	 * @param circleLogger
	 * @param fulfillmentResponse
	 * @return
	 * @throws ExecutionFailedException
	 */
	private String prepareBalanceCheckSuccessResponse(final FDPRequest fdpRequest, final FDPResponse fdpResponse,
			final Logger circleLogger, FulfillmentResponse fulfillmentResponse) throws ExecutionFailedException {
		setResponseSuccessParameters(fulfillmentResponse);
		ResponseData responseData = new ResponseData();
		responseData.setAction(FulfillmentActionTypes.BALANCE_CHECK);
		responseData.setCircleCode(fdpRequest.getCircle().getCircleCode());
		responseData.setInputCode(((FulfillmentRequestImpl) fdpRequest).getRequestString());
		responseData.setResponseType(FulfillmentResponseTypes.PRODUCT);
		/*
		 * final BalanceEnquiry balanceEnquiry =
		 * FulfillmentResponseUtil.getBalancesResponse(fdpRequest, fdpResponse);
		 * if(null != balanceEnquiry) {
		 * responseData.setBalanceEnquiry(balanceEnquiry); } else { ProductBuy
		 * productBuy =
		 * FulfillmentResponseUtil.getProductBuyResponse(fdpRequest,
		 * fdpResponse, circleLogger); responseData.setProductBuy(productBuy); }
		 */
		ProductBuy productBuy = FulfillmentResponseUtil.getProductBuyResponse(fdpRequest, fdpResponse, circleLogger);
		responseData.setProductBuy(productBuy);
		fulfillmentResponse.setResponseData(responseData);
		updateMsisdn(fdpRequest, fulfillmentResponse);
		return getXmlStringFromObject(fulfillmentResponse);
	}
	
		/**
	 * This method generates the Handset based charging reponse xml in success
	 * 
	 * @param fdpRequest
	 * @param fdpResponse
	 * @param circleLogger
	 * @param fulfillmentResponse
	 * @param actionType
	 * @return
	 * @throws ExecutionFailedException
	 */
	private String prepareHandsetBasedChargingFulfillmentSuccessResponse(FDPRequest fdpRequest, FDPResponse fdpResponse,
			Logger circleLogger, FulfillmentResponse fulfillmentResponse, FulfillmentActionTypes actionType)
			throws ExecutionFailedException {
		ResponseData responseData = new ResponseData();
		responseData.setAction(actionType);
		responseData.setInputCode(((FulfillmentRequestImpl) fdpRequest).getRequestString());
		Response response = new Response();
		response.setStatus(fdpResponse.getExecutionStatus().getStatusText());
		response.setDescription(((FDPMetadataResponseImpl) fdpResponse).getFulfillmentResponse());
		responseData.setResponse(response);
		fulfillmentResponse.setResponseData(responseData);
		return getXmlStringFromObject(fulfillmentResponse);
	}

	/**
	 * This method generates the Handset based charging reponse xml in failure
	 * 
	 * @param fdpRequest
	 * @param fdpResponse
	 * @param circleLogger
	 * @param fulfillmentResponse
	 * @param actionType
	 * @return
	 * @throws ExecutionFailedException
	 */
	private String prepareHandsetBasedChargingFulfillmentFailureResponse(FDPRequest fdpRequest, FDPResponse fdpResponse,
			Logger circleLogger, FulfillmentResponse fulfillmentResponse, FulfillmentActionTypes actionType)
			throws ExecutionFailedException {
		ResponseData responseData = new ResponseData();
		responseData.setAction(actionType);
		responseData.setInputCode(((FulfillmentRequestImpl) fdpRequest).getRequestString());
		Response response = new Response();
		response.setStatus(fdpResponse.getExecutionStatus().getStatusText());
		response.setDescription(((FDPMetadataResponseImpl) fdpResponse).getFulfillmentResponse());
		responseData.setResponse(response);
		fulfillmentResponse.setResponseData(responseData);
		return getXmlStringFromObject(fulfillmentResponse);
	}

	/**
	 * This method generate the Active bundles xml response 
	 * @param fdpRequest
	 * @param fdpResponse	
	 * @param fulfillmentResponse
	 * @return The Fulfillment response
	 * @throws ExecutionFailedException
	 */
	private String prepareActiveBundlesSuccessResponse(final FDPRequest fdpRequest, final FDPResponse fdpResponse,
			 FulfillmentResponse fulfillmentResponse) throws ExecutionFailedException {		
		FDPCommand activeBundleCommand = fdpRequest.getExecutedCommand(Command.GET_ACTIVE_BUNDLES_DETAILS_REQUEST.getCommandDisplayName());
		if(activeBundleCommand!=null && activeBundleCommand instanceof NonTransactionCommand){			
			fulfillmentResponse.setDescription(fdpResponse.getResponseError().getResponseErrorString());
			fulfillmentResponse.setResponseCode(String.valueOf(FulfillmentResponseCodes.SUCCESS.getResponseCode()));
			fulfillmentResponse.setStatus(FulfillmentStatus.SUCCESS);
			fulfillmentResponse.setSystemType(FulfillmentSystemTypes.CIS);
			ResponseData responseData = new ResponseData();
			responseData.setActiveBundles(FulfillmentResponseUtil.getActiveBundlesResponse(fdpRequest, activeBundleCommand));
			fulfillmentResponse.setResponseData(responseData);			
		}
		return getXmlStringFromObject(fulfillmentResponse);
	}
	
	/**
	 * This method generate the Active bundles xml response 
	 * @param fdpRequest
	 * @param fdpResponse	
	 * @param fulfillmentResponse
	 * @return The Fulfillment response
	 * @throws ExecutionFailedException
	 */
	private String prepareActiveBundlesFailureResponse(final FDPRequest fdpRequest, final FDPResponse fdpResponse,
			 FulfillmentResponse fulfillmentResponse) throws ExecutionFailedException {
		/*fulfillmentResponse.setDescription();
		fulfillmentResponse.setResponseCode(fdpResponse.getResponseError().getResponseCode());*/
		if(fdpResponse.getResponseError()!=null){
			fulfillmentResponse.setDescription(fdpResponse.getResponseError().getResponseErrorString());
			fulfillmentResponse.setResponseCode(fdpResponse.getResponseError().getResponseCode());
		}
		
		fulfillmentResponse.setStatus(FulfillmentStatus.FAILURE);
		fulfillmentResponse.setSystemType(FulfillmentSystemTypes.CIS);
		return getXmlStringFromObject(fulfillmentResponse);
	}
	
}
