package com.ericsson.fdp.business.http.adapter.impl;

import java.util.HashMap;
import java.util.Map;

import org.apache.camel.Endpoint;
import org.apache.camel.Exchange;
import org.apache.camel.ExchangePattern;
import org.apache.camel.Message;
import org.apache.camel.ProducerTemplate;
import org.apache.camel.cdi.CdiCamelContext;
import org.apache.camel.component.direct.DirectEndpoint;
import org.apache.camel.spi.Synchronization;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ericsson.fdp.business.adapter.impl.AbstractAdapterSOAPCallClient;
import com.ericsson.fdp.business.bean.SOAPAdapterRequest;
import com.ericsson.fdp.business.constants.BusinessConstants;
import com.ericsson.fdp.business.constants.FDPCommandConstants;
import com.ericsson.fdp.business.util.AbilityCommandUtil;
import com.ericsson.fdp.common.constants.FDPConstant;
import com.ericsson.fdp.core.cache.AppCacheSubStore;
import com.ericsson.fdp.core.config.utils.PropertyUtils;
import com.ericsson.fdp.core.exception.ExecutionFailedException;
import com.ericsson.fdp.core.request.FDPRequest;
import com.ericsson.fdp.core.serviceprovisioning.bean.FDPAppBag;
import com.ericsson.fdp.core.utils.ApplicationConfigUtil;
import com.ericsson.fdp.dao.enums.ExternalSystem;
import com.ericsson.fdp.route.constant.RoutingConstant;

/**
 * The Class HttpCallClientAbility used to request Ability External System and
 * gets response from it.
 */
public class SOAPCallClientAbility extends AbstractAdapterSOAPCallClient {

	/** The Constant LOGGER. */
	private static final Logger LOGGER = LoggerFactory.getLogger(SOAPCallClientAbility.class);

	/** The context. */
	private CdiCamelContext context;

	private static Boolean syncFlag = Boolean.valueOf(PropertyUtils.getProperty(RoutingConstant.ABILITY_SYNC_FLAG));
	
	@Override
	public Map<String, Object> soapCallClient(final String soapRequest, final SOAPAdapterRequest soapAdapterRequest,
			final ExternalSystem externalSystemType, final FDPRequest fdpRequest) throws ExecutionFailedException {
		try {
			Map<String, Object> responseMap = null;
			context = ApplicationConfigUtil.getCdiCamelContextProvider().getContext();
			
			LOGGER.debug("Invoking callClient().... with adapter request as " + soapAdapterRequest);
			Endpoint endpoint = getEndpoint();
			LOGGER.debug("Http Request to be post on endpoint :" + endpoint.getEndpointUri());
			final Exchange exchange = endpoint.createExchange();
			final Message in = exchange.getIn();
			String requestId = soapAdapterRequest.getRequestId();
			Long tansctionID = fdpRequest.getOriginTransactionID();
			String completeString  = getHeader(tansctionID).toString().concat(soapRequest).concat(getFooter().toString());
            LOGGER.info("Ability Soap request : {}", completeString);
			in.setHeader(Exchange.CONTENT_LENGTH, soapRequest.length());
			exchange.setProperty(BusinessConstants.CIRCLE_CODE, soapAdapterRequest.getCircleCode());
			exchange.getIn().setHeader(BusinessConstants.CIRCLE_CODE,soapAdapterRequest.getCircleCode());
			exchange.setProperty(BusinessConstants.EXTERNAL_SYSTEM_TYPE, externalSystemType.name());
			exchange.setProperty(BusinessConstants.REQUEST_ID, requestId);
			in.setBody(completeString);
			exchange.setPattern(ExchangePattern.InOut);

			String responseCode= null;
			String outputXML = null;
			ProducerTemplate producerTemplate = exchange.getContext().createProducerTemplate();
			
			LOGGER.debug("Ability Sync flag :"+syncFlag);
			
			if(syncFlag){
				producerTemplate.send(endpoint, exchange);
				final Message out = exchange.getOut();
				outputXML = out.getBody(String.class);
                if (outputXML == null) {
                    responseCode = BusinessConstants.HTTP_ADAPTER_ERROR_CODE;
                    LOGGER.error("Problem occurred while getting response from Ability :" + exchange.getProperty(Exchange.EXCEPTION_CAUGHT));
                    LOGGER.error("Problem occurred while getting response from Ability ip:"
                            + exchange.getProperty(BusinessConstants.OUTGOING_CIRCLE_CODE_IP_PORT));
                } else {
                    responseCode = out.getHeader(BusinessConstants.RESPONSE_CODE).toString();
                }
			}
			else{
				producerTemplate.asyncCallback(endpoint, exchange, new Synchronization() {
					@Override
					public void onFailure(Exchange exchange) {
						LOGGER.debug("Ability Command got for in onFailure :{}");
						Message message = exchange.getIn();
						if(message != null)
							AbilityCommandUtil.writeCSVFileFromResponseXML(exchange.getIn().getBody(String.class));
					}
					@Override
					public void onComplete(Exchange exchange) {
						LOGGER.debug("Ability Command got for in onComplete :{}");
					}
				});
				
				outputXML = (String)ApplicationConfigUtil.getApplicationConfigCache().getValue(new FDPAppBag(AppCacheSubStore.CONFIGURATION_MAP, FDPConstant.ABILITY_SUCCESS_RESPONSE));
				responseCode = (outputXML != null && !outputXML.trim().isEmpty()) ? "200" : BusinessConstants.HTTP_ADAPTER_ERROR_CODE;
			}
			LOGGER.debug("Ability Command response :", outputXML);
			responseMap = new HashMap();
			responseMap.put(FDPCommandConstants.ABILITY_RESPONSE_CODE_PATH,responseCode);
			responseMap.put(BusinessConstants.RESPONSE_CODE, responseCode);;
			responseMap.put(BusinessConstants.COMMAND_OUTPUT, outputXML);
			return responseMap;
		} catch (final Exception e) {
			LOGGER.error(e.getMessage(), e);
		}
		return null;
	}

	private StringBuilder getHeader(Long tansctionID){
		
		StringBuilder sb = new StringBuilder();
		
		sb.append("<soapenv:Envelope xmlns:soapenv=\"http://schemas.xmlsoap.org/soap/envelope/\" xmlns:com=\"http://schema.concierge.com\">");
		sb.append("<soapenv:Header/>");
		sb.append("<soapenv:Body>");
		sb.append("<com:clientRequest>");
		sb.append("<EaiEnvelope xmlns=\"http://schema.concierge.com/Envelope\" xmlns:ser=\"http://schema.concierge.com/Services\">");
		sb.append("<ApplicationName>"+PropertyUtils.getProperty("APPLICATION_NAME").trim()+"</ApplicationName>");
		sb.append("<Domain>"+PropertyUtils.getProperty("SOAP_DOMAIN").trim()+"</Domain>");
		sb.append("<Service>"+PropertyUtils.getProperty("SOAP_SERVICE").trim()+"</Service>");
		sb.append("<Language>"+PropertyUtils.getProperty("SOAP_LANGUAGE").trim()+"</Language>");
		sb.append("<UserId>"+PropertyUtils.getProperty("SOAP_USER_ID").trim()+"</UserId>");
		sb.append("<Sender>"+PropertyUtils.getProperty("SOAP_SENDER").trim()+"</Sender>");
		sb.append("<MessageId>"+tansctionID+"</MessageId>");
		sb.append("<Payload>");
		sb.append("<ser:Services>");
		sb.append("<ser:Request>");
        sb.append("<ser:Operation_Name>" + PropertyUtils.getProperty("SOAP_OPERATION_NAME").trim() + "</ser:Operation_Name>");
		sb.append("<ser:ChangeServicesRequest>");
		sb.append("<ser:request>");
		sb.append("<EVENT xmlns=\"\">");
		return sb;
	}
	
	
	private StringBuilder getFooter(){
		StringBuilder sb = new StringBuilder();
		sb.append("</EVENT>");
		sb.append("</ser:request>");
		sb.append("</ser:ChangeServicesRequest>");
		sb.append("</ser:Request>");
		sb.append("</ser:Services>");
		sb.append("</Payload>");
		sb.append("</EaiEnvelope>");
		sb.append("</com:clientRequest>");
		sb.append("</soapenv:Body>");
		sb.append("</soapenv:Envelope>");
		return sb;
	}
	
	/**
	 * Gets the endpoint.
	 * 
	 * @param externalSystem
	 *            the external system
	 * @return the endpoint
	 * @throws ExecutionFailedException
	 *             the execution failed exception
	 */
	private Endpoint getEndpoint() throws ExecutionFailedException {
		Endpoint endpoint = null;
		String endPointName = BusinessConstants.HTTP_COMPONENT_ABILITY_ENDPOINT;
		endpoint = context.getEndpoint(endPointName, DirectEndpoint.class);
		return endpoint;
	}
	
}
