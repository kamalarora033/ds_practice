package com.ericsson.fdp.business.route.processor;

import java.util.HashMap;
import java.util.Map;

import javax.annotation.Resource;
import javax.naming.NamingException;
import javax.servlet.http.HttpServletRequest;
import javax.xml.bind.JAXBException;

import org.apache.camel.Exchange;
import org.apache.camel.Message;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ericsson.fdp.FDPCacheable;
import com.ericsson.fdp.business.constants.BusinessConstants;
import com.ericsson.fdp.business.enums.ExternalSystemActionServiceProvisingMapping;
import com.ericsson.fdp.business.enums.RequestMetaValuesKey;
import com.ericsson.fdp.business.enums.ivr.FulfillmentParameters;
import com.ericsson.fdp.business.enums.ivr.FulfillmentResponseCodes;
import com.ericsson.fdp.business.enums.ivr.commandservice.FulfillmentResponseKey;
import com.ericsson.fdp.business.expression.Expression;
import com.ericsson.fdp.business.node.FDPServiceProvisioningNode;
import com.ericsson.fdp.business.product.Product;
import com.ericsson.fdp.business.product.impl.SharedAccountProduct;
import com.ericsson.fdp.business.request.FDPCheckConsumerResponse;
import com.ericsson.fdp.business.route.processor.ivr.AbstractFulfillmentProcessor;
import com.ericsson.fdp.business.serviceprovisioning.ServiceProvisioning;
import com.ericsson.fdp.business.sharedaccount.service.CheckConsumerService;
import com.ericsson.fdp.business.util.RequestUtil;
import com.ericsson.fdp.business.util.ServiceProvisioningUtil;
import com.ericsson.fdp.common.constants.FDPConstant;
import com.ericsson.fdp.common.enums.ChannelType;
import com.ericsson.fdp.common.enums.ModuleType;
import com.ericsson.fdp.common.enums.Status;
import com.ericsson.fdp.common.util.StringUtil;
import com.ericsson.fdp.common.vo.FDPCircle;
import com.ericsson.fdp.core.command.FDPCommand;
import com.ericsson.fdp.core.exception.EvaluationFailedException;
import com.ericsson.fdp.core.exception.ExecutionFailedException;
import com.ericsson.fdp.core.logging.FDPLogger;
import com.ericsson.fdp.core.node.FDPNode;
import com.ericsson.fdp.core.request.FDPRequest;
import com.ericsson.fdp.core.request.FDPResponse;
import com.ericsson.fdp.core.request.ResponseError;
import com.ericsson.fdp.core.request.impl.FDPRequestImpl;
import com.ericsson.fdp.core.serviceprovisioning.bean.FDPMetaBag;
import com.ericsson.fdp.core.utils.ApplicationConfigUtil;
import com.ericsson.fdp.dao.enums.FDPServiceProvSubType;
import com.ericsson.fdp.route.constant.RoutingConstant;
import com.ericsson.fdp.route.enumeration.FDPRouteHeaders;
import com.google.gson.Gson;

// TODO: Auto-generated Javadoc
/**
 * The Class ValidateUserProcessor.
 */
public class IVRServiceRequestProcessor extends AbstractFulfillmentProcessor {

	/*	*//** The transaction sequence dao. */
	/*
	 * @Inject private TransactionSequenceDAO transactionSequenceDAO;
	 */

	/** The fdp service provisioning. */
	@Resource(lookup = "java:app/fdpBusiness-1.0/ServiceProvisioningImpl")
	private ServiceProvisioning serviceProvising;
	
	@Resource(lookup = "java:app/fdpBusiness-1.0/CheckConsumerServiceImpl")
	private CheckConsumerService checkConsumerService;
	
	/*
	 * (non-Javadoc)
	 * 
	 * @see org.apache.camel.Processor#process(org.apache.camel.Exchange)
	 */
	@Override
	public void process(final Exchange exchange) throws Exception {
		FDPResponse fdpResponse = null;		
		ResponseError responseError = null;
		Status status = null;
		final Message in = exchange.getIn();
		final String requestId = in.getHeader(FDPRouteHeaders.REQUEST_ID.getValue(), String.class);
		exchange.setProperty(RoutingConstant.REQUEST_ID, requestId);
		final String msisdn = in.getHeader(BusinessConstants.MSISDN, String.class);
		final String input = in.getHeader(BusinessConstants.INPUT, String.class);
		final FDPCircle fdpCircle = exchange.getIn().getHeader(FDPRouteHeaders.FDP_CIRCLE.getValue(), FDPCircle.class);
		final String ivrUrl = getRequestedUrl(exchange.getIn());
		final String incomingIP = exchange.getIn().getHeader(FDPRouteHeaders.INCOMING_IP.getValue(), String.class);
		final FDPRequest fdpRequest = createFDPRequest(msisdn, fdpCircle);
		if (null != requestId && fdpRequest instanceof FDPRequestImpl) {
			FDPRequestImpl fdpRequestImpl = (FDPRequestImpl) fdpRequest;
			fdpRequestImpl.setRequestId(requestId);
		}
		
		preProcessingOfLogs(fdpRequest, incomingIP, exchange);

		final String productId = getProductID(fdpCircle, input);
		if (productId == null) {
			sendResponse(exchange, FulfillmentResponseCodes.INVALID_PARAMETER, input);
			String errorDescription = new StringBuilder(FDPConstant.REQUEST_ID)
									.append(FDPConstant.LOGGER_KEY_VALUE_DELIMITER)
									.append(fdpRequest.getRequestId())
									.append(FDPConstant.LOGGER_DELIMITER)
									.append(FDPConstant.ERROR_CODE)
									.append(FDPConstant.LOGGER_KEY_VALUE_DELIMITER)
									.append(FulfillmentResponseCodes.INVALID_PARAMETER.getResponseCode().toString())
									.append(FDPConstant.LOGGER_DELIMITER)
									.append(FDPConstant.ERROR_DESC)
									.append(FDPConstant.LOGGER_KEY_VALUE_DELIMITER)
									.append(String.format(FulfillmentResponseCodes.INVALID_PARAMETER.getDescription(), input,
											fdpCircle.getCircleCode())).toString();
			FDPLogger.error(getCircleRequestLogger(fdpCircle), getClass(), "process()", errorDescription);
			printTPProductLog(exchange, fdpRequest, input, ivrUrl);
			printPostRequestLogs(exchange);
			logStatusOfResponse(fdpRequest, fdpResponse, exchange);
		} else {
			final FDPCheckConsumerResponse fdpCheckConsumerResp = checkConsumerService.checkPrePaidConsumer(fdpRequest);			
			//if(fdpCheckConsumerResp.isPrePaidConsumer()) {
			if (fdpCheckConsumerResp.getExecutionStatus().toString().equals(Status.SUCCESS.toString())){
				executeSPForProduct(fdpRequest, productId, input, ivrUrl, exchange);
			} else {	
				for(FDPCommand fdpCommand:fdpCheckConsumerResp.getExecutedCommands()){
					status = fdpCommand.getExecutionStatus();
					responseError = fdpCommand.getResponseError();
					if(responseError != null && status != null){								
						final Map<String, String> responseMap = new HashMap<String, String>();
						responseMap.put(FulfillmentResponseKey.RESPONSE_CODE.getValue(),responseError.getResponseCode());
						responseMap.put(FulfillmentResponseKey.SYSTEM_TYPE.getValue(),responseError.getSystemType());
						responseMap.put(FulfillmentResponseKey.STATUS.getValue(), status.getStatusText());
						responseMap.put(FulfillmentResponseKey.RESPONSE_DESCRIPTION.getValue(),responseError.getResponseErrorString());				
						final Message out = exchange.getOut();
						final String response = new Gson().toJson(responseMap);
						out.setBody(response);
						exchange.setProperty(Exchange.ROUTE_STOP, Boolean.TRUE);
						printIVRTrafficOutLogger(in, response);
						break;
					}
				}					
			}
		}
	}

	/**
	 * Prints the tp product log.
	 * 
	 * @param exchange
	 *            the exchange
	 * @param fdpRequest
	 *            the fdp request
	 * @param inputIVR
	 *            the input ivr
	 * @param ivrUrl
	 *            the ivr url
	 * @throws ExecutionFailedException
	 *             the execution failed exception
	 */
	private void printTPProductLog(Exchange exchange, FDPRequest fdpRequest, String inputIVR, String ivrUrl)
			throws ExecutionFailedException {
		// RID:USSD_14.98.242.242_6faa92a4-b309-fc5a-44dc-c0c2a05b15c5|ACTN:*123*1#|
		// CHCODE:*123*1#|TP:Menu
		StringBuilder printTpProductLog = new StringBuilder(logBehaviorReport(fdpRequest, inputIVR, ivrUrl));
		printTpProductLog.append(FDPConstant.LOGGER_DELIMITER).append(FDPConstant.TYPE)
				.append(FDPConstant.LOGGER_KEY_VALUE_DELIMITER).append(FDPConstant.Product);
		FDPLogger.info(getCircleRequestLogger(exchange), getClass(), "generatePostProcessingLogs()",
				printTpProductLog.toString());
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
	 * Handle fdp response.
	 * 
	 * @param exchange
	 *            the exchange
	 * @param fdpResponse
	 *            the fdp response
	 * @param fdpRequest
	 *            the fdp request
	 * @throws ExecutionFailedException
	 *             the execution failed exception
	 * @throws JAXBException
	 */
	private void handleFDPResponse(final Exchange exchange, final FDPResponse fdpResponse, final FDPRequest fdpRequest)
			throws ExecutionFailedException, JAXBException {
		if (fdpResponse == null) {
			throw new ExecutionFailedException("fdpResponse is null");
		} else {
			logDebugMessageInRequestLogs(exchange, getClass(), "executeRequest()", fdpResponse.toString());
			if (fdpResponse.getExecutionStatus().equals(Status.SUCCESS)) {
				sendResponse(exchange, FulfillmentResponseCodes.SUCCESS);
			} else {
				setErrorResponse(exchange, fdpResponse.getResponseError());
			}
		}
	}

	/**
	 * Log status of response.
	 * 
	 * @param fdpRequest
	 *            the fdp request
	 * @param fdpResponse
	 *            the fdp response
	 * @param exchange
	 *            the exchange
	 * @throws ExecutionFailedException
	 *             the execution failed exception
	 */
	private void logStatusOfResponse(final FDPRequest fdpRequest, final FDPResponse fdpResponse, Exchange exchange)
			throws ExecutionFailedException {
		// RID:USSD_14.98.242.242_6faa92a4-b30|INRESULT:Success
		String status = null;
		if (fdpResponse == null) {
			status = Status.FAILURE.getStatusText();
		} else {
			status = fdpResponse.getExecutionStatus().getStatusText();
		}

		final StringBuilder appenderValue2 = new StringBuilder();
		appenderValue2.append(FDPConstant.REQUEST_ID).append(FDPConstant.LOGGER_KEY_VALUE_DELIMITER)
				.append(fdpRequest.getRequestId()).append(FDPConstant.LOGGER_DELIMITER).append(FDPConstant.INRESULT)
				.append(FDPConstant.LOGGER_KEY_VALUE_DELIMITER).append(status);
		FDPLogger.info(getCircleRequestLogger(exchange), getClass(), "generatePostProcessingLogs()",
				appenderValue2.toString());
	}

	/**
	 * Log behavior report.
	 * 
	 * @param fdpRequest
	 *            the fdp request
	 * @param inputIVR
	 *            the input ivr
	 * @param ivrUrl
	 *            the ivr url
	 * @return the string
	 */
	private String logBehaviorReport(final FDPRequest fdpRequest, final String inputIVR, final String ivrUrl) {
		// RID:USSD_14.98.242.242_6faa92a4-b30|INIP:127.0.0.1|LNAME:DELHI_SAKET|CH:USSD|MSISDN:9711406956
		// what should be the behavior field over here..
		final StringBuilder appenderValue = new StringBuilder();
		appenderValue.append(FDPConstant.REQUEST_ID).append(FDPConstant.LOGGER_KEY_VALUE_DELIMITER)
				.append(fdpRequest.getRequestId()).append(FDPConstant.LOGGER_DELIMITER).append("ACTN")
				.append(FDPConstant.LOGGER_KEY_VALUE_DELIMITER).append(ivrUrl).append(FDPConstant.LOGGER_DELIMITER);
				//.append("CHCODE").append(FDPConstant.LOGGER_KEY_VALUE_DELIMITER).append(ivrUrl);
		// FDPLogger.debug(getCircleLogger(fdpRequest),getClass(),"logUserBehaviorReport()",appenderValue.toString());
		return appenderValue.toString();
	}

	/**
	 * Pre processing of logs.
	 * 
	 * @param fdpRequest
	 *            the fdp request
	 * @param incomingIP
	 *            the incoming ip
	 * @param exchange
	 *            the exchange
	 * @throws ExecutionFailedException
	 *             the execution failed exception
	 */
	private void preProcessingOfLogs(final FDPRequest fdpRequest, final String incomingIP, final Exchange exchange)
			throws ExecutionFailedException {
		// RID:USSD_14.98.242.242_6faa92a4-b30|INIP:127.0.0.1|LNAME:DELHI_SAKET|CH:USSD|MSISDN:9711406956
		final StringBuilder appenderValue = new StringBuilder();
		appenderValue.append(FDPConstant.REQUEST_ID).append(FDPConstant.LOGGER_KEY_VALUE_DELIMITER)
				.append(fdpRequest.getRequestId()).append(FDPConstant.LOGGER_DELIMITER).append(FDPConstant.INCOMING_IP)
				.append(FDPConstant.LOGGER_KEY_VALUE_DELIMITER).append(incomingIP).append(FDPConstant.LOGGER_DELIMITER)
				.append(BusinessConstants.LOGICAL_NAME).append(FDPConstant.LOGGER_KEY_VALUE_DELIMITER)
				.append(exchange.getIn().getHeader(FDPRouteHeaders.CHANNEL_NAME.getValue(), String.class))
				.append(FDPConstant.LOGGER_DELIMITER).append(FDPConstant.CHANNEL_TYPE)
				.append(FDPConstant.LOGGER_KEY_VALUE_DELIMITER)
				.append(exchange.getIn().getHeader(FDPRouteHeaders.CHANNEL_NAME.getValue(), String.class))
				.append(FDPConstant.LOGGER_DELIMITER).append(FDPConstant.MSISDN)
				.append(FDPConstant.LOGGER_KEY_VALUE_DELIMITER).append(fdpRequest.getSubscriberNumber());
		FDPLogger.info(getCircleRequestLogger(exchange), getClass(), "preProcessingOfLogs()", appenderValue.toString());

		// Log RID and SID RID:USSD_14.98.242.242_6faa92a4-b30|SID:123456
		final StringBuilder ridAndSid = new StringBuilder();
		ridAndSid.append(FDPConstant.REQUEST_ID).append(FDPConstant.LOGGER_KEY_VALUE_DELIMITER)
				.append(fdpRequest.getRequestId()).append(FDPConstant.LOGGER_DELIMITER).append(FDPConstant.SESSION_ID)
				.append(FDPConstant.LOGGER_KEY_VALUE_DELIMITER).append(fdpRequest.getRequestId());

		FDPLogger.info(getCircleRequestLogger(exchange), getClass(), "preProcessingOfLogs()", ridAndSid.toString());
	}

	/**
	 * Execute sp for product.
	 * 
	 * @param fdpRequest
	 *            the fdp request
	 * @param productId
	 *            the product id
	 * @param inputIVR
	 *            the input ivr
	 * @param ivrUrl
	 *            the ivr url
	 * @param exchange
	 *            the exchange
	 * @return the fDP response
	 * @throws ExecutionFailedException
	 *             the execution failed exception
	 * @throws EvaluationFailedException
	 *             the evaluation failed exception
	 * @throws JAXBException
	 */
	private void executeSPForProduct(final FDPRequest fdpRequest, final String productId, final String inputIVR,
            final String ivrUrl, final Exchange exchange) throws ExecutionFailedException, EvaluationFailedException,
            JAXBException,Exception {
     FDPResponse fdpResponse = null;
     boolean error = true;
     Object[] serviceProv = getServiceProvisioning(exchange, productId, fdpRequest);
		if (serviceProv[1] != null && null != serviceProv[2] && serviceProv[2] instanceof Product) {
			error = false;
			Product product = ((Product) serviceProv[2]);
			Expression expression = product.getConstraintForChannel(ChannelType.WEB);
			if (expression == null || expression.evaluateExpression(fdpRequest)){
	            logProductInfo(fdpRequest, (FDPServiceProvSubType) serviceProv[0], inputIVR, ivrUrl, exchange);
	            FDPLogger.debug(getCircleRequestLogger(exchange), getClass(), "executeSPForProduct()",
	                         "executing serviceProvising for productId " + productId);
	            fdpResponse = serviceProvising.executeServiceProvisioning(fdpRequest);
	            handleFDPResponse(exchange, fdpResponse, fdpRequest);
			}else{
				error = true;
			}
     } if (error) {
    	 	/*final String msisdn = (String) exchange.getIn().getHeader("MSISDN");
            final FDPCircle fdpCircle = CircleCodeFinder.getFDPCircleByMsisdn(msisdn,
                         ApplicationConfigUtil.getApplicationConfigCache());*/
            final FDPCircle fdpCircle = fdpRequest.getCircle();
			String errorDescription = new StringBuilder(FDPConstant.REQUEST_ID)
									.append(FDPConstant.LOGGER_KEY_VALUE_DELIMITER)
									.append(fdpRequest.getRequestId())
									.append(FDPConstant.LOGGER_DELIMITER)
									.append(FDPConstant.ERROR_CODE)
									.append(FDPConstant.LOGGER_KEY_VALUE_DELIMITER)
									.append(FulfillmentResponseCodes.CONFIGURATION_ERROR.getResponseCode().toString())
									.append(FDPConstant.LOGGER_DELIMITER)
									.append(FDPConstant.ERROR_DESC)
									.append(FDPConstant.LOGGER_KEY_VALUE_DELIMITER)
									.append(String.format(FulfillmentResponseCodes.CONFIGURATION_ERROR.getDescription(), inputIVR))
									.toString();
            FDPLogger.error(getCircleRequestLogger(fdpCircle), getClass(), "executeSPForProduct()", errorDescription);
            sendResponse(exchange, FulfillmentResponseCodes.CONFIGURATION_ERROR, inputIVR);
     }                                                                                
     printPostRequestLogs(exchange);
     logStatusOfResponse(fdpRequest, fdpResponse, exchange);
}

	/**
	 * Gets the service provisioning.
	 * 
	 * @param exchange
	 *            the exchange
	 * @param productId
	 *            the product id
	 * @param fdpRequest
	 *            the fdp request
	 * @return the service provisioning
	 * @throws NumberFormatException
	 *             the number format exception
	 * @throws ExecutionFailedException
	 *             the execution failed exception
	 * @throws EvaluationFailedException
	 *             the evaluation failed exception
	 */
	private Object[] getServiceProvisioning(final Exchange exchange, final String productId, final FDPRequest fdpRequest)
			throws NumberFormatException, ExecutionFailedException, EvaluationFailedException {
		Object[] obj = new Object[2];
		FDPCacheable[] fdpCacheAble = null;
		FDPServiceProvSubType serviceProv = null;
		String action = exchange.getIn().getHeader(FulfillmentParameters.ACTION.getValue(), String.class);

		switch (ExternalSystemActionServiceProvisingMapping.getFDPServiceProvSubType(action)) {
		case RS_DEPROVISION_PRODUCT:
		case PAM_DEPROVISION_PRODUCT:
			fdpCacheAble = ServiceProvisioningUtil.getProductAndSP(fdpRequest, Long.valueOf(productId),
					ExternalSystemActionServiceProvisingMapping.PRODUCT_DEACTIVATION_RS.getServiceProvtype());
			serviceProv = ExternalSystemActionServiceProvisingMapping.PRODUCT_DEACTIVATION_RS.getServiceProvtype();
			if (fdpCacheAble.length != 2 || fdpCacheAble[1] == null) {
				fdpCacheAble = ServiceProvisioningUtil.getProductAndSP(fdpRequest, Long.valueOf(productId),
						ExternalSystemActionServiceProvisingMapping.PRODUCT_DEACTIVATION_PAM.getServiceProvtype());
				serviceProv = ExternalSystemActionServiceProvisingMapping.PRODUCT_DEACTIVATION_PAM.getServiceProvtype();
			}
			break;
		case PRODUCT_BUY:
		default:
			serviceProv = ExternalSystemActionServiceProvisingMapping.BUY.getServiceProvtype();
			fdpCacheAble = ServiceProvisioningUtil.getProductAndSP(fdpRequest, Long.valueOf(productId), serviceProv);
		}
		obj = new Object[] { serviceProv, fdpCacheAble[1],fdpCacheAble[0] };
		return obj;
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
	private String getProductID(final FDPCircle fdpCircle, final String inputIVR) throws ExecutionFailedException {
		String productId = null;
		FDPNode fdpNode = null;
		final String cachedDMIVRCode = BusinessConstants.IVR_CHANNEL + FDPConstant.SPACE + inputIVR;
		FDPLogger.debug(getCircleRequestLogger(fdpCircle), getClass(), "getProductID()", "Fetching for IVR DM Code:"
				+ cachedDMIVRCode);
		final FDPCacheable fdpCacheable = ApplicationConfigUtil.getMetaDataCache().getValue(
				new FDPMetaBag(fdpCircle, ModuleType.DM, cachedDMIVRCode));
		fdpNode = RequestUtil.checkInstanceForNode(fdpCacheable);
		if (null != fdpNode && fdpNode instanceof FDPServiceProvisioningNode) {
			final FDPServiceProvisioningNode serviceProvisioningNode = (FDPServiceProvisioningNode) fdpNode;
			productId = serviceProvisioningNode.getEntityIdForCache(RequestMetaValuesKey.PRODUCT);
		}
		return productId;
	}

	/**
	 * Creates the fdp request.
	 * 
	 * @param msisdn
	 *            the msisdn
	 * @param fdpCircle
	 *            the fdp circle
	 * @return the fDP request
	 * @throws ExecutionFailedException
	 *             the execution failed exception
	 * @throws NamingException
	 *             the naming exception
	 */
	public FDPRequest createFDPRequest(final String msisdn, final FDPCircle fdpCircle)
			throws ExecutionFailedException, NamingException {
		final Long transactionNumber = generateTransactionId();
		return RequestUtil.getRequest(msisdn, transactionNumber, fdpCircle, ChannelType.IVR);
	}

	/**
	 * Log product info.
	 * 
	 * @param fdpRequest
	 *            the fdp request
	 * @param serviceProv
	 *            the service prov
	 * @param inputIVR
	 *            the input ivr
	 * @param ivrUrl
	 *            the ivr url
	 * @param exchange
	 *            the exchange
	 * @throws ExecutionFailedException
	 *             the execution failed exception
	 */
	public void logProductInfo(final FDPRequest fdpRequest, final FDPServiceProvSubType serviceProv,
			final String inputIVR, final String ivrUrl, final Exchange exchange) throws ExecutionFailedException {
		final StringBuilder userBehaviourLoggString = new StringBuilder();

		Product product = null;
		final FDPCacheable fdpCacheable = fdpRequest.getValueFromRequest(RequestMetaValuesKey.PRODUCT);
		if (fdpCacheable instanceof Product) {
			product = (Product) fdpCacheable;
		}
		final String userBehaviourReport = logBehaviorReport(fdpRequest, inputIVR, ivrUrl);
		userBehaviourLoggString.append(userBehaviourReport).append(FDPConstant.LOGGER_DELIMITER)
				.append(FDPConstant.TYPE).append(FDPConstant.LOGGER_KEY_VALUE_DELIMITER).append(FDPConstant.Product)
				.append(FDPConstant.LOGGER_DELIMITER).append(FDPConstant.USE_CASE_PRODUCT)
				.append(FDPConstant.LOGGER_KEY_VALUE_DELIMITER).append(serviceProv.getText())
				.append(FDPConstant.LOGGER_DELIMITER).append(FDPConstant.PRODUCT_ID)
				.append(FDPConstant.LOGGER_KEY_VALUE_DELIMITER);

		if (product == null) {
			userBehaviourLoggString.append("Undefined");
		} else {
			userBehaviourLoggString.append(product.getProductId()).append(FDPConstant.LOGGER_DELIMITER)
					.append(FDPConstant.PRODUCT_NAME).append(FDPConstant.LOGGER_KEY_VALUE_DELIMITER)
					.append(product.getProductName()).append(FDPConstant.LOGGER_DELIMITER)
					.append(FDPConstant.PRODUCT_CHANNEL_NAME).append(FDPConstant.LOGGER_KEY_VALUE_DELIMITER)
					.append(product.getProductNameForChannel(fdpRequest.getChannel()))
					.append(FDPConstant.LOGGER_DELIMITER).append(FDPConstant.PRODUCT_TYPE)
					.append(FDPConstant.LOGGER_KEY_VALUE_DELIMITER).append(product.getProductType())
					.append(FDPConstant.LOGGER_DELIMITER).append(FDPConstant.PRODUCT_USSD_NAME)
					.append(FDPConstant.LOGGER_KEY_VALUE_DELIMITER)
					.append(product.getProductNameForChannel(ChannelType.USSD)).append(FDPConstant.LOGGER_DELIMITER)
					.append(FDPConstant.PRODUCT_SMS_NAME).append(FDPConstant.LOGGER_KEY_VALUE_DELIMITER)
					.append(product.getProductNameForChannel(ChannelType.SMS)).append(FDPConstant.LOGGER_DELIMITER)
					.append(FDPConstant.PRODUCT_WEB_NAME).append(FDPConstant.LOGGER_KEY_VALUE_DELIMITER)
					.append(product.getProductNameForChannel(ChannelType.WEB)).append(FDPConstant.LOGGER_DELIMITER)
					.append(FDPConstant.PRODUCT_CONSTRAINT).append(FDPConstant.LOGGER_KEY_VALUE_DELIMITER)
					.append(product.getConstraintForChannel(fdpRequest.getChannel()));
			if (product instanceof SharedAccountProduct) {
				final SharedAccountProduct sharedAccountProduct = (SharedAccountProduct) product;
				userBehaviourLoggString
						.append(FDPConstant.LOGGER_DELIMITER)
						.append(FDPConstant.PRODUCT_META)
						.append(FDPConstant.LOGGER_KEY_VALUE_DELIMITER)
						.append(StringUtil.convertMapToStringWithDelimiter(sharedAccountProduct.getProductMetaData(),
								FDPConstant.LOGGER_KEY_VALUE_VALUE_DELIMITER));
			}
		}
		FDPLogger.info(getCircleRequestLogger(exchange), getClass(), "generateUserBehaviorInfo()",
				userBehaviourLoggString.toString());
	}
}
