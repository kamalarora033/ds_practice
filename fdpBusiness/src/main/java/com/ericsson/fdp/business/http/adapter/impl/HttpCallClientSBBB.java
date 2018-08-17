package com.ericsson.fdp.business.http.adapter.impl;

import java.util.HashMap;
import java.util.Map;

import org.apache.camel.Endpoint;
import org.apache.camel.Exchange;
import org.apache.camel.Message;
import org.apache.camel.Producer;
import org.apache.camel.cdi.CdiCamelContext;
import org.apache.camel.component.direct.DirectEndpoint;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ericsson.fdp.business.adapter.impl.AbstractAdapterHttpCallClient;
import com.ericsson.fdp.business.bean.HttpAdapterRequest;
import com.ericsson.fdp.business.constants.BusinessConstants;
import com.ericsson.fdp.business.enums.Command;
import com.ericsson.fdp.business.enums.ConfigurationKey;
import com.ericsson.fdp.business.enums.RequestMetaValuesKey;
import com.ericsson.fdp.business.enums.ivr.FulfillmentActionTypes;
import com.ericsson.fdp.business.node.impl.SpecialMenuNode;
import com.ericsson.fdp.business.request.impl.FulfillmentRequestImpl;
import com.ericsson.fdp.business.sharedaccount.constans.SharedAccountConstants;
import com.ericsson.fdp.business.util.FulfillmentUtil;
import com.ericsson.fdp.business.util.MVELUtil;
import com.ericsson.fdp.common.constants.FDPConstant;
import com.ericsson.fdp.common.enums.FDPCSAttributeParam;
import com.ericsson.fdp.common.enums.FDPCSAttributeValue;
import com.ericsson.fdp.core.entity.ExternalSystemDetail;
import com.ericsson.fdp.core.exception.ExecutionFailedException;
import com.ericsson.fdp.core.logging.FDPLogger;
import com.ericsson.fdp.core.node.FDPNode;
import com.ericsson.fdp.core.request.FDPRequest;
import com.ericsson.fdp.core.utils.ApplicationConfigUtil;
import com.ericsson.fdp.dao.enums.ExternalSystem;

public class HttpCallClientSBBB extends AbstractAdapterHttpCallClient {
	
	/** The Constant LOGGER. */
	private static final Logger LOGGER = LoggerFactory.getLogger(HttpCallClientSBBB.class);

	
	@Override
	public Map<String, Object> httpCallClient(final String httpRequest, final HttpAdapterRequest httpAdapterRequest,
			final ExternalSystem externalSystemType, final FDPRequest fdpRequest) throws ExecutionFailedException {
		Map<String, Object> responseMap = null;
		try {
			
			LOGGER.debug("Invoking callClient().... with adapter request as " + httpAdapterRequest);
			final ExternalSystemDetail externalSystemDetail = httpAdapterRequest.getExternalSystemDetail();
			final String camelCircleEndpoint = new StringBuilder(BusinessConstants.CAMEL_DIRECT).append(
					externalSystemDetail.getEndPoint()).append(BusinessConstants.UNDERSCORE).append(httpAdapterRequest.getCommandName()).toString();
			LOGGER.debug("Endpoint got from Request Adapter :" + camelCircleEndpoint);
			CdiCamelContext context = ApplicationConfigUtil.getCdiCamelContextProvider().getContext();
			final Endpoint vasEndpoint = context.getEndpoint(camelCircleEndpoint, DirectEndpoint.class);
			final Exchange exchange = vasEndpoint.createExchange();
			final Message in = exchange.getIn();
			exchange.setProperty(BusinessConstants.CIRCLE_CODE, httpAdapterRequest.getCircleCode());
			exchange.setProperty(BusinessConstants.EXTERNAL_SYSTEM_TYPE, ExternalSystem.SBBB.name());
			String requestId = httpAdapterRequest.getRequestId();
			exchange.setProperty(BusinessConstants.REQUEST_ID, requestId);
			String msisdn = String.valueOf(fdpRequest.getSubscriberNumber());
			LOGGER.debug("Subscriber from fdpRequest : {}", msisdn);
			exchange.setProperty(BusinessConstants.MSISDN, msisdn);
			String compliedURI = updateForSelfDeleteConsumerRequest(getCompiledHttpUri(httpRequest.toString(), externalSystemDetail.getUserName(),
					externalSystemDetail.getPassword()), fdpRequest);
			final FDPNode fdpNode = (FDPNode) fdpRequest.getValueFromRequest(RequestMetaValuesKey.NODE);
			if (null != fdpNode && fdpNode instanceof SpecialMenuNode) {
				final SpecialMenuNode specialMenuNode = (SpecialMenuNode) fdpNode;
				
				switch (specialMenuNode.getSpecialMenuNodeType()) {
			
				case SHARED_BUNDLE_DELETE_CONSUMER:
				case SHARED_BUNDLE_DELETE_SELF_CONSUMER:
					compliedURI = getURIWithUCTUTValuesForDeleteAction(fdpRequest, compliedURI);
					break;
				case SHARED_BUNDLE_UPDATE_CONSUMER_DATA:
					compliedURI = getURIWithUCTUTValuesForUpdataDataAction(fdpRequest, compliedURI);
					break;
				default:
					break;
					
				}
			}
			
			LOGGER.debug("Command need to fire :" + compliedURI);
			in.setHeader(Exchange.HTTP_QUERY, compliedURI);
			final Producer producer = vasEndpoint.createProducer();
			producer.process(exchange);
			final Message out = exchange.getOut();
			String outputXML = out.getBody(String.class);
			String responseCode = out.getHeader(BusinessConstants.HTTP_RESPONSE_CODE, String.class);
			responseMap = new HashMap<String, Object>();
			String circleName = httpAdapterRequest.getCircleName();
			Logger logger = getLogger(circleName, externalSystemType);
			if (responseCode != null && "200".equals(responseCode) && outputXML != null) {
				postProcessOfLogsSuccess(logger, requestId, exchange.getExchangeId());
			} else {
				postProcessOfLogsFailure(logger, requestId, exchange);
				postProcessForErrorLogs(exchange, externalSystemType, requestId, logger);
				responseCode = BusinessConstants.HTTP_ADAPTER_ERROR_CODE;
			}
			responseMap.put(BusinessConstants.RESPONSE_CODE, responseCode);
			responseMap.put(BusinessConstants.COMMAND_OUTPUT, outputXML);
			return responseMap;
		} catch (final ExecutionFailedException e) {
			LOGGER.error(e.getMessage(), e);
		} catch (final Exception e) {
			LOGGER.error(e.getMessage(), e);
			e.printStackTrace();
		}
		return responseMap;
	}

	/**
	 * This method attaches usageThreshold and counter values of Provider and consumer in URI for Delete Consumer Action.
	 */
	private String getURIWithUCTUTValuesForDeleteAction(final FDPRequest fdpRequest, String compliedURI) {
		if (fdpRequest.getExecutedCommand(Command.GET_USAGE_THRESHOLDS_AND_COUNTERS_CONSUMER.getCommandDisplayName()) != null) {
			String usageCounterId = null;
			String usageThresholdId = null;
			Map<String, Object> uMap = MVELUtil.evaluateUCUTDetailsForUser(fdpRequest, fdpRequest.getExecutedCommand(
					Command.GET_USAGE_THRESHOLDS_AND_COUNTERS_PROVIDER.getCommandDisplayName()));
			usageCounterId = FulfillmentUtil.getNotificationTextFromConfiguration(fdpRequest,ConfigurationKey.SHARED_BONUS_BUNDLE_PROVIDER_UC_ID) == null ? SharedAccountConstants.PROVIDER_UC_ID
			: FulfillmentUtil.getNotificationTextFromConfiguration(fdpRequest,ConfigurationKey.SHARED_BONUS_BUNDLE_PROVIDER_UC_ID);
			usageThresholdId =  FulfillmentUtil.getNotificationTextFromConfiguration(fdpRequest,ConfigurationKey.SHARED_BONUS_BUNDLE_PROVIDER_UT_ID) == null ? SharedAccountConstants.PROVIDER_UT_ID
					: FulfillmentUtil.getNotificationTextFromConfiguration(fdpRequest,ConfigurationKey.SHARED_BONUS_BUNDLE_PROVIDER_UT_ID);
			compliedURI = attachUCUTValues(compliedURI, usageCounterId, usageThresholdId, uMap,
					SharedAccountConstants.PROVIDER_USER);
			
			uMap = MVELUtil.evaluateUCUTDetailsForUser(fdpRequest, fdpRequest.getExecutedCommand(
					Command.GET_USAGE_THRESHOLDS_AND_COUNTERS_CONSUMER.getCommandDisplayName()));
			usageCounterId =  FulfillmentUtil.getNotificationTextFromConfiguration(fdpRequest,ConfigurationKey.SHARED_BONUS_BUNDLE_CONSUMER_UC_ID) == null ? SharedAccountConstants.CONSUMER_UC_ID
					: FulfillmentUtil.getNotificationTextFromConfiguration(fdpRequest,ConfigurationKey.SHARED_BONUS_BUNDLE_CONSUMER_UC_ID);
			usageThresholdId = FulfillmentUtil.getNotificationTextFromConfiguration(fdpRequest,ConfigurationKey.SHARED_BONUS_BUNDLE_CONSUMER_UT_ID) == null ? SharedAccountConstants.CONSUMER_UT_ID
					: FulfillmentUtil.getNotificationTextFromConfiguration(fdpRequest,ConfigurationKey.SHARED_BONUS_BUNDLE_CONSUMER_UT_ID);
			compliedURI = attachUCUTValues(compliedURI, usageCounterId, usageThresholdId, uMap,
					SharedAccountConstants.CONSUMER);
		}
		return compliedURI;
	}
	
	/**
	 * This method attaches usageThreshold and counter values of  consumer in URI for Upgrade Consumer Data Action.
	 */
	private String getURIWithUCTUTValuesForUpdataDataAction(final FDPRequest fdpRequest, String compliedURI) {
		if (fdpRequest.getExecutedCommand(Command.GET_USAGE_THRESHOLDS_AND_COUNTERS_CONSUMER.getCommandDisplayName()) != null) {
			String usageCounterId = null;
			String usageThresholdId = null;
			Map<String, Object> uMap = MVELUtil.evaluateUCUTDetailsForUser(fdpRequest, fdpRequest.getExecutedCommand(
					Command.GET_USAGE_THRESHOLDS_AND_COUNTERS_CONSUMER.getCommandDisplayName()));
			
			usageCounterId =  FulfillmentUtil.getNotificationTextFromConfiguration(fdpRequest,ConfigurationKey.SHARED_BONUS_BUNDLE_CONSUMER_UC_ID) == null ? SharedAccountConstants.CONSUMER_UC_ID
					: FulfillmentUtil.getNotificationTextFromConfiguration(fdpRequest,ConfigurationKey.SHARED_BONUS_BUNDLE_CONSUMER_UC_ID);
			usageThresholdId = FulfillmentUtil.getNotificationTextFromConfiguration(fdpRequest,ConfigurationKey.SHARED_BONUS_BUNDLE_CONSUMER_UT_ID) == null ? SharedAccountConstants.CONSUMER_UT_ID
					: FulfillmentUtil.getNotificationTextFromConfiguration(fdpRequest,ConfigurationKey.SHARED_BONUS_BUNDLE_CONSUMER_UT_ID);
			compliedURI = attachConsumerUCUTValues(compliedURI, usageCounterId, usageThresholdId, uMap);
		}
		return compliedURI;
	}

	/**
	 * This method attaches usageThreshold and counter values of Provider and consumer in URI.
	 */
	private String attachUCUTValues(String compliedURI, String usageCounterId, String usageThresholdId,
			Map<String, Object> uMap, String user) {
		Integer usageCounter = null;
		Integer usageThreshold = null;
		if(uMap!=null && uMap.containsKey(FDPCSAttributeValue.UC.name()+ FDPConstant.UNDERSCORE + usageCounterId + FDPConstant.UNDERSCORE 
		+ FDPCSAttributeParam.VALUE.name().toString()) && uMap.containsKey(FDPCSAttributeValue.UT.name()+ FDPConstant.UNDERSCORE + usageThresholdId + FDPConstant.UNDERSCORE+ FDPCSAttributeParam.VALUE.name()))
		{	
			usageCounter = Integer.parseInt((String) uMap.get(FDPCSAttributeValue.UC.name()
							+ FDPConstant.UNDERSCORE + usageCounterId + FDPConstant.UNDERSCORE 
							+ FDPCSAttributeParam.VALUE.name().toString()));
			usageThreshold = Integer.parseInt((String) uMap.get(FDPCSAttributeValue.UT.name()
							+ FDPConstant.UNDERSCORE + usageThresholdId + FDPConstant.UNDERSCORE
							+ FDPCSAttributeParam.VALUE.name()));
			compliedURI += BusinessConstants.AMPERSAND_PARAMETER_APPENDER; 
			if (SharedAccountConstants.CONSUMER.equals(user)) {
				compliedURI += SharedAccountConstants.CONSUMER_COUNTER_VALUE_PARAM + BusinessConstants.EQUALS + usageCounter 
					+ BusinessConstants.AMPERSAND_PARAMETER_APPENDER + SharedAccountConstants.CONSUMER_THRESHOLD_VALUE_PARAM 
					+ BusinessConstants.EQUALS + usageThreshold;
			} else {
				compliedURI += SharedAccountConstants.PROVIDER_COUNTER_VALUE_PARAM + BusinessConstants.EQUALS + usageCounter 
					+ BusinessConstants.AMPERSAND_PARAMETER_APPENDER + SharedAccountConstants.PROVIDER_THRESHOLD_VALUE_PARAM 
					+ BusinessConstants.EQUALS + usageThreshold;
			}
		}
		return compliedURI;
	}
	
	/**
	 * This method attaches usageThreshold and counter values of  consumer in URI.
	 */
	private String attachConsumerUCUTValues(String compliedURI, String usageCounterId, String usageThresholdId,
			Map<String, Object> uMap) {
		Integer usageCounter = null;
		Integer usageThreshold = null;
		if(uMap!=null && uMap.containsKey(FDPCSAttributeValue.UC.name()
			+ FDPConstant.UNDERSCORE + usageCounterId + FDPConstant.UNDERSCORE + FDPCSAttributeParam.VALUE.name().toString()) && uMap.containsKey(FDPCSAttributeValue.UT.name()+ FDPConstant.UNDERSCORE + usageThresholdId + FDPConstant.UNDERSCORE
						+ FDPCSAttributeParam.VALUE.name()))
		  {
			usageCounter = Integer.parseInt((String) uMap.get(FDPCSAttributeValue.UC.name()
							+ FDPConstant.UNDERSCORE + usageCounterId + FDPConstant.UNDERSCORE 
							+ FDPCSAttributeParam.VALUE.name().toString()));
			usageThreshold = Integer.parseInt((String) uMap.get(FDPCSAttributeValue.UT.name()
							+ FDPConstant.UNDERSCORE + usageThresholdId + FDPConstant.UNDERSCORE
							+ FDPCSAttributeParam.VALUE.name()));
			compliedURI += BusinessConstants.AMPERSAND_PARAMETER_APPENDER; 
		
			compliedURI += SharedAccountConstants.CONSUMER_COUNTER_VALUE_PARAM + BusinessConstants.EQUALS + usageCounter 
					+ BusinessConstants.AMPERSAND_PARAMETER_APPENDER + SharedAccountConstants.CONSUMER_THRESHOLD_VALUE__EXISTING_PARAM 
					+ BusinessConstants.EQUALS + usageThreshold;
			}	
	
		return compliedURI;
	}
	/**
	 * Post process for error logs.
	 * 
	 * @param exchange
	 *            the exchange
	 * @param externalSystemType
	 *            the external system type
	 * @param requestId
	 *            the request id
	 * @param logger
	 *            the logger
	 */
	private void postProcessForErrorLogs(final Exchange exchange, final ExternalSystem externalSystemType,
			final String requestId, final Logger logger) {
		final String outGoingcircleCodeIPaddressPort = exchange.getProperty(
				BusinessConstants.OUTGOING_CIRCLE_CODE_IP_PORT, String.class);
		final StringBuilder stringBuilder = new StringBuilder();
		stringBuilder.append(BusinessConstants.REQUEST_ID).append(FDPConstant.LOGGER_KEY_VALUE_DELIMITER)
				.append(requestId).append(FDPConstant.LOGGER_DELIMITER);
		if (outGoingcircleCodeIPaddressPort != null) {
			final String logicalName = exchange.getProperty(BusinessConstants.LOGICAL_NAME, String.class);
			stringBuilder.append(FDPConstant.CHARGING_NODE_IP).append(FDPConstant.LOGGER_KEY_VALUE_DELIMITER)
			.append(outGoingcircleCodeIPaddressPort)
			.append(FDPConstant.LOGGER_DELIMITER + FDPConstant.INTERFACE_TYPE)
			.append(FDPConstant.LOGGER_KEY_VALUE_DELIMITER).append(externalSystemType)
			.append(FDPConstant.LOGGER_DELIMITER).append(FDPConstant.CHARGING_NODE)
			.append(FDPConstant.LOGGER_KEY_VALUE_DELIMITER).append(logicalName)
			.append(FDPConstant.LOGGER_DELIMITER).append(FDPConstant.TYPE_OF_LOG)
			.append(FDPConstant.LOGGER_KEY_VALUE_DELIMITER).append(FDPConstant.EXCEPTION)
			.append(FDPConstant.LOGGER_DELIMITER).append(FDPConstant.IFRESRSN)
			.append(FDPConstant.LOGGER_KEY_VALUE_DELIMITER).append("Error in exchange is ")
			.append(exchange.getException()).append(FDPConstant.LOGGER_DELIMITER).append(FDPConstant.REPORTTYPE)
			.append(FDPConstant.LOGGER_KEY_VALUE_DELIMITER).append(FDPConstant.EXTEXCEPTIONREPORT);
		} else {
			stringBuilder.append("Could not get out goind circle code and ip.");
		}
		FDPLogger.error(logger, getClass(), "process()", stringBuilder.toString());
	}
	
	private String getCompiledHttpUri(final String command, final String userName, final String password) {
		return new StringBuilder(command).append(BusinessConstants.AMPERSAND_PARAMETER_APPENDER)
				.append(BusinessConstants.TEXT_USER).append(BusinessConstants.EQUALS).append(userName).append(BusinessConstants.AMPERSAND_PARAMETER_APPENDER)
				.append(BusinessConstants.PASSWORD).append(BusinessConstants.EQUALS).append(password)
				.toString();
	}
	
	/*private Map<String, Object> simulator(Map<String,Object> responseMap, String httpRequest, Endpoint vasEndpoint) {
		String responseCode = "200";
		StringBuilder stringBuilder = null;
		responseMap = new HashMap<String, Object>();
		System.out.println("HttpRequest String:::::::::"+httpRequest.toString());
		String path = null;
		if(vasEndpoint.getEndpointUri().toString().contains("GetDetails")) {
			path="/home/eashtod/OFFICE/FDP/ResponseFiles/SBB/getDetails.xml";
		}
		if (vasEndpoint.getEndpointUri().toString().contains("AddConsumer")) {
			path = "/home/eashtod/OFFICE/FDP/ResponseFiles/SBB/addSubscriber.xml";
		}
		if(vasEndpoint.getEndpointUri().toString().contains("DeleteConsumer")) {
			path = "/home/eashtod/OFFICE/FDP/ResponseFiles/SBB/deleteMember.xml";
		}
		try {
			System.out.println("Response Path::::::"+path);
			 BufferedReader reader = new BufferedReader(new
			 FileReader(path));
			 String line = null;
			 stringBuilder = new StringBuilder();
			 String ls = System.getProperty("line.separator");
			
			 while ((line = reader.readLine()) != null) {
			 stringBuilder.append(line);
			 stringBuilder.append(ls);
			 }
			 reader.close();	
		} catch (final Exception e) {
			e.printStackTrace();
		}
		
		responseMap.put(BusinessConstants.RESPONSE_CODE, responseCode);
		responseMap.put(BusinessConstants.COMMAND_OUTPUT, stringBuilder.toString());
		return responseMap;
	}*/

	/**
	 * This method will append extra is_self=TRUE in case of SHARED BUNDLE SELF DELETE request.
	 * 
	 * @param command
	 * @param fdpRequest
	 * @return
	 */
	private String updateForSelfDeleteConsumerRequest(final String command, final FDPRequest fdpRequest) {
		String updatedCommmand = null;
		if (fdpRequest instanceof FulfillmentRequestImpl) {
			final FulfillmentRequestImpl fulfillmentRequestImpl = (FulfillmentRequestImpl) fdpRequest;
			updatedCommmand = (FulfillmentActionTypes.SHARED_BONUS_BUNDLE_DELETE_CONSUMER.equals(fulfillmentRequestImpl
					.getActionTypes()) && FulfillmentUtil.isConsumerSelfDeleteRequest(fdpRequest)) ? new StringBuilder(
					command).append(BusinessConstants.AMPERSAND_PARAMETER_APPENDER)
					.append(BusinessConstants.SBBBINPUT_PARAM_IS_SELF).toString() : command;
		} else {
			final FDPNode fdpNode = (FDPNode) fdpRequest.getValueFromRequest(RequestMetaValuesKey.NODE);
			if (null != fdpNode && fdpNode instanceof SpecialMenuNode) {
				final SpecialMenuNode specialMenuNode = (SpecialMenuNode) fdpNode;
				switch (specialMenuNode.getSpecialMenuNodeType()) {
				case SHARED_BUNDLE_DELETE_SELF_CONSUMER:
					updatedCommmand = new StringBuilder(command).append(BusinessConstants.AMPERSAND_PARAMETER_APPENDER)
							.append(BusinessConstants.SBBBINPUT_PARAM_IS_SELF).toString();
					break;
				default:
					updatedCommmand = command;
					break;
				}
			}
		}
		return (null == updatedCommmand) ? command : updatedCommmand;
	}
}
