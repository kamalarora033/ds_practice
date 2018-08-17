package com.ericsson.fdp.business.http.router;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.annotation.Resource;
import javax.ejb.Singleton;
import javax.inject.Inject;

import org.apache.camel.Endpoint;
import org.apache.camel.Exchange;
import org.apache.camel.ExchangePattern;
import org.apache.camel.Message;
import org.apache.camel.Producer;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.cdi.CdiCamelContext;
import org.apache.camel.component.direct.DirectEndpoint;
import org.apache.camel.component.http.SSLContextParametersSecureProtocolSocketFactory;
import org.apache.camel.model.ChoiceDefinition;
import org.apache.camel.model.LoadBalanceDefinition;
import org.apache.camel.util.jsse.KeyManagersParameters;
import org.apache.camel.util.jsse.KeyStoreParameters;
import org.apache.camel.util.jsse.SSLContextParameters;
import org.apache.commons.httpclient.protocol.Protocol;
import org.apache.commons.httpclient.protocol.ProtocolSocketFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ericsson.fdp.business.constants.BusinessConstants;
import com.ericsson.fdp.business.route.controller.ChoiceDefinitionStorage;
import com.ericsson.fdp.business.route.controller.LoadBalancerStorage;
import com.ericsson.fdp.business.route.processor.HttpHeaderProcessor;
import com.ericsson.fdp.business.route.processor.LogOutgoingIpProcessor;
import com.ericsson.fdp.business.util.HttpUtil;
import com.ericsson.fdp.common.enums.ExternalSystem;
import com.ericsson.fdp.core.config.utils.PropertyUtils;
import com.ericsson.fdp.core.dsm.framework.service.FDPCache;
import com.ericsson.fdp.core.serviceprovisioning.bean.FDPAppBag;
import com.ericsson.fdp.dao.dto.FDPCGWConfigDTO;
import com.ericsson.fdp.dao.fdpadmin.FDPCGWConfigDAO;
import com.ericsson.fdp.dao.fdpadmin.FDPCircleDAO;
import com.ericsson.fdp.route.cdi.context.provider.CdiCamelContextProvider;
import com.ericsson.fdp.route.processors.LogRetryProcessor;
import com.ericsson.fdp.route.processors.RouteCompletionProcessor;
import com.ericsson.fdp.route.processors.RouteFailureProcessor;

/**
 * The Class CGWHttpRoute used to CGW routes over the Http Protocol and uses
 * camel http component.
 */
@Singleton(name = "CGWHttpRoute")
public class CGWHttpRoute {

	/** The cdi camel context provider. */
	@Inject
	private CdiCamelContextProvider cdiCamelContextProvider;

	/** The context. */
	private CdiCamelContext context;

	/** The fdp circle dao. */
	@Inject
	private FDPCircleDAO fdpCircleDAO;

	/** The fdp cgw config dao. */
	@Inject
	private FDPCGWConfigDAO fdpCGWConfigDAO;

	/** The Constant LOGGER. */
	private static final Logger LOGGER = LoggerFactory.getLogger(CGWHttpRoute.class);

	/** The logOutgoingIpProcessor is used to log out ip addresses for exchange */
	@Inject
	private LogOutgoingIpProcessor logOutgoingIpProcessor;

	@Inject
	LogRetryProcessor logRetryProcessor;
	
    @Inject
    private RouteCompletionProcessor completionProcessor;
    
	/**
	 * The headerProcessor is used to process the headers at run time mapped by
	 * ip and port.
	 */
	@Inject
	private HttpHeaderProcessor headerProcessor;

	@Resource(lookup = "java:app/fdpBusiness-1.0/LoadBalancerStorage")
	private LoadBalancerStorage loadBalancerStorage;

	@Resource(lookup = "java:app/fdpBusiness-1.0/ChoiceDefinitionStorage")
	private ChoiceDefinitionStorage choiceDefinitionStorage;

	/**
	 * Creates the routes.
	 */
	public void createRoutes() throws Exception {

		context = cdiCamelContextProvider.getContext();

		context.addRoutes(createCGWHttpRoutes());
		// callClient();
	}

	/**
	 * This method is responsible for creating the routes.
	 * 
	 * @return the route builder
	 */
	private RouteBuilder createCGWHttpRoutes() {
		return new RouteBuilder() {

			@SuppressWarnings("unchecked")
			@Override
			public void configure() throws Exception {

				/**
				 * Getting maximumRedeiveryAttempt and maximumRedeliveryInterval
				 * from Property file.
				 */
				final int maximumRedeiveryAttempt = Integer.valueOf(PropertyUtils
						.getProperty("route.maximum.redelivery.attempt"));
				final int maximumRedeliveryInterval = Integer.valueOf(PropertyUtils
						.getProperty("route.redelivery.delay"));

				final boolean autostartup = true;

				/*
				 * errorHandler(defaultErrorHandler().maximumRedeliveries(
				 * maximumRedeiveryAttempt).maximumRedeliveryDelay(
				 * maximumRedeliveryInterval));
				 * 
				 * onException(java.net.ConnectException.class).maximumRedeliveries
				 * (maximumRedeiveryAttempt)
				 * .maximumRedeliveryDelay(maximumRedeliveryInterval);
				 * onException(java.io.IOException.class).maximumRedeliveries(
				 * maximumRedeiveryAttempt)
				 * .maximumRedeliveryDelay(maximumRedeliveryInterval);
				 */

				final List<String> circleCodeList = getAllCircleCodes();

				final ChoiceDefinition routeDefinitionCGW = from(BusinessConstants.HTTP_COMPONENT_CGW_ENDPOINT)
						.routeId(BusinessConstants.MAIN_ROUTE_UNDERSCORE + ExternalSystem.CGW.name())
						.setExchangePattern(ExchangePattern.InOut).autoStartup(autostartup).choice();
				final Map<String, Object> choiceDefinitionMap = choiceDefinitionStorage.getchoiceDefinitionMap();
				choiceDefinitionMap.put(ExternalSystem.CGW.name(), routeDefinitionCGW);
				for (int i = 0; circleCodeList != null && i < circleCodeList.size(); i++) {

					routeDefinitionCGW
							.when(header(BusinessConstants.CIRCLE_CODE).isEqualTo(circleCodeList.get(i)))
							.routeId(
									BusinessConstants.SUB_ROUTE_UNDERSCORE + circleCodeList.get(i)
											+ ExternalSystem.CGW.name())
							.to(BusinessConstants.HTTP_COMPONENT_CGW_ENDPOINT + "_" + circleCodeList.get(i))
							.endChoice();

					// Get List of the FDPCGWConfigDTOs from DAO using
					// getEndpointURLsForCGWByCircleCode()
					final List<FDPCGWConfigDTO> fdpCGWConfigDTOList = getEndpointURLsForCGWByCircleCode(circleCodeList
							.get(i));

					// Parsing FDPCGWConfigDTO List into Endpoint store into
					// Array
					final String[] endpoints = getCircleEndPointsForLB(fdpCGWConfigDTOList, circleCodeList.get(i));

					/**
					 * Sub Route Defining routes for End Point to Circles CGW
					 * Servers , if we find two or more CGW URLs for the Circle
					 * then load-balancer will manages the load in round-robin
					 * manner in case of fail-over otherwise there will no
					 * load-balancer in case of single CGW Servers.
					 */

					final int fdpCGWConfigDTOListSize = fdpCGWConfigDTOList.size();
					LoadBalanceDefinition loadBalanceDefinition = null;
					if (fdpCGWConfigDTOList != null && fdpCGWConfigDTOListSize > 1) {
						loadBalanceDefinition = from(
								BusinessConstants.HTTP_COMPONENT_CGW_ENDPOINT + "_" + circleCodeList.get(i))
								.setExchangePattern(ExchangePattern.InOut).loadBalance()
								.failover(fdpCGWConfigDTOListSize - 1, false, true, java.net.ConnectException.class);
						for (final String endpoint : endpoints) {
							loadBalanceDefinition.routeId(endpoint).to(endpoint);
						}
					} else {
						if (fdpCGWConfigDTOList != null && fdpCGWConfigDTOListSize == 1) {
							from(BusinessConstants.HTTP_COMPONENT_CGW_ENDPOINT + "_" + circleCodeList.get(i))
									.setExchangePattern(ExchangePattern.InOut).routeId(endpoints[0]).to(endpoints);
						}
					}
					final String circleCode = circleCodeList.get(i);
					for (int j = 0; fdpCGWConfigDTOList != null && j < fdpCGWConfigDTOListSize; j++) {

						final FDPCGWConfigDTO fdpCGWConfigDTO = fdpCGWConfigDTOList.get(j);
						final List<String> routeIdList = new ArrayList<String>();

						/**
						 * routes virtual endpoints and Physical server
						 * endpoints. And manages the maximum re-delivery
						 * attempt and re-delivery delay between current attempt
						 * and next re-delivery attempt.
						 */
						final String circleCodeEndpoint = HttpUtil.getCgwSubRouteId(fdpCGWConfigDTO, circleCode);
						final String ipAddress = fdpCGWConfigDTO.getIpAddress().getValue();
						final String circleCodeIpaddressPort = circleCode + BusinessConstants.COLON + ipAddress
								+ BusinessConstants.COLON + String.valueOf(fdpCGWConfigDTO.getPort());
						final String routeId2 = BusinessConstants.SUB_ROUTE_UNDERSCORE + circleCodeEndpoint
								+ ExternalSystem.CGW.name();
						// routeIdList.add(BusinessConstants.HTTP_COMPONENT_CGW_ENDPOINT
						// + circleCodeEndpoint);
						routeIdList.add(routeId2);

						if (fdpCGWConfigDTO.getHttps()) {

							final KeyStoreParameters ksp = new KeyStoreParameters();
							ksp.setResource(fdpCGWConfigDTO.getCertificateLocation());
							ksp.setPassword(fdpCGWConfigDTO.getCertificatePassword());

							final KeyManagersParameters kmp = new KeyManagersParameters();
							kmp.setKeyStore(ksp);
							kmp.setKeyPassword(fdpCGWConfigDTO.getPassword());

							final SSLContextParameters scp = new SSLContextParameters();
							scp.setKeyManagers(kmp);

							final ProtocolSocketFactory factory = new SSLContextParametersSecureProtocolSocketFactory(
									scp);

							Protocol.registerProtocol("https", new Protocol("https", factory, 443));

							from(BusinessConstants.HTTP_COMPONENT_CGW_ENDPOINT + circleCodeEndpoint)
							.autoStartup(autostartup)
									.routeId(routeId2)
									.onCompletion().onCompleteOnly().process(completionProcessor).end()
									.setProperty(BusinessConstants.OUTGOING_IP_ADDRESS, constant(ipAddress))
									.setProperty(BusinessConstants.OUTGOING_CIRCLE_CODE_IP_PORT,
											constant(circleCodeIpaddressPort))
									.process(logOutgoingIpProcessor)
									.process(headerProcessor)
									.onException(java.lang.Exception.class, java.net.ConnectException.class,
											java.net.SocketTimeoutException.class,
											org.apache.camel.component.http.HttpOperationFailedException.class)
											.handled(false)
									.maximumRedeliveries(maximumRedeiveryAttempt)
									.redeliveryDelay(maximumRedeliveryInterval).onRedelivery(logRetryProcessor)
									.to(BusinessConstants.COMPONENT_EXCEPTION_ENDPOINT).end()
									.to(getCGWEndpointURLForCircle(fdpCGWConfigDTO, fdpCGWConfigDTO.getHttps()));
						} else {
							from(BusinessConstants.HTTP_COMPONENT_CGW_ENDPOINT + circleCodeEndpoint)
							.autoStartup(autostartup)
									.routeId(routeId2)
									.onCompletion().onCompleteOnly().process(completionProcessor).end()
									.setProperty(BusinessConstants.OUTGOING_IP_ADDRESS, constant(ipAddress))
									.setProperty(BusinessConstants.OUTGOING_CIRCLE_CODE_IP_PORT,
											constant(circleCodeIpaddressPort))
									.process(logOutgoingIpProcessor)
									.process(headerProcessor)
									.onException(java.net.ConnectException.class,
											org.apache.camel.component.http.HttpOperationFailedException.class)
											.handled(false)
									.maximumRedeliveries(maximumRedeiveryAttempt)
									.redeliveryDelay(maximumRedeliveryInterval).onRedelivery(logRetryProcessor)
									.to(BusinessConstants.COMPONENT_EXCEPTION_ENDPOINT).end()
									.to(getCGWEndpointURLForCircle(fdpCGWConfigDTO, fdpCGWConfigDTO.getHttps()));
						}
						// addRouteIdInCache(fdpCGWConfigDTO, routeIdList,
						// circleCode, loadBalanceDefinition);
					}
				}
			}
		};
	}

	/**
	 * Call client.
	 * 
	 * @return the string
	 * @throws Exception
	 *             the exception
	 */
	public void callClient() throws Exception {

		LOGGER.debug("CGW Dummy Request Starts:");
		final String httpRequest = "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?>" + "\n<DebitRequest>"
				+ "\n<msisdn>918527948299</msisdn>" + "\n<vendorId_serviceId>1_5232330</vendorId_serviceId>"
				+ "\n<sessionId>30965509041069386</sessionId>" + "\n<opType>1</opType>" + "\n<reqOrgin>0</reqOrgin>"
				+ "\n<circleId>7</circleId>" + "\n</DebitRequest>";

		final String httpRequest1 = "GetAccountDetails for CGW";
		final Endpoint endpoint = context.getEndpoint(BusinessConstants.HTTP_COMPONENT_CGW_ENDPOINT,
				DirectEndpoint.class);
		final Exchange exchange = endpoint.createExchange();
		exchange.setPattern(ExchangePattern.InOut);
		final Message in = exchange.getIn();
		// in.setHeader(Exchange.HTTP_PROTOCOL_VERSION,
		// BusinessConstants.HTTP_VERSION_CGW);
		in.setHeader(Exchange.CONTENT_TYPE, "text/xml");
		in.setHeader(Exchange.HTTP_METHOD, "POST");
		in.setHeader(Exchange.HTTP_METHOD, BusinessConstants.HTTP_METHOD);
		in.setHeader(Exchange.CONTENT_LENGTH, httpRequest.length());
		in.setHeader(BusinessConstants.USER_AGENT, BusinessConstants.USER_AGENT_STRING_CGW);
		in.setHeader("Host", "10.89.41.145:1095");
		in.setHeader("Accept", "text/xml");
		exchange.setProperty(BusinessConstants.CIRCLE_CODE, "KL");
		exchange.setProperty(BusinessConstants.EXTERNAL_SYSTEM_TYPE, "CGW");
		exchange.setProperty(BusinessConstants.REQUEST_ID, "456");
		in.setBody(httpRequest);
		in.setHeader(Exchange.HTTP_QUERY, "input=" + httpRequest1);
		String outputXML = "";
		final Producer producer = endpoint.createProducer();
		// producer.start();
		producer.process(exchange);
		final Message out = exchange.getOut();
		outputXML = out.getBody(String.class);
		final String responseCode = out.getHeader("camelhttpresponsecode", String.class);
		LOGGER.debug("CGW outputXML :" + outputXML);
		if (outputXML != null) {
			LOGGER.debug("Response Code :  | Response XML from callClient :" + responseCode, outputXML);
		} else {
			LOGGER.debug("Response Code :  | output xml is null ");
		}
		// producer.stop();
	}

	/**
	 * This method is used to return all available circle codes.
	 * 
	 * @return circle codes.
	 */
	private List<String> getAllCircleCodes() {
		return fdpCircleDAO.getAllCircleCodes();
	}

	/**
	 * Gets the endpoint ur ls for cgw by circle code.
	 * 
	 * @param circleCode
	 *            the circle code
	 * @return the endpoint ur ls for cgw by circle code
	 */
	private List<FDPCGWConfigDTO> getEndpointURLsForCGWByCircleCode(final String circleCode) {
		return fdpCGWConfigDAO.getCGWEndpointByCircleCode(circleCode);
	}

	/**
	 * Get Circlewise endpoints to weld load balancer.
	 * 
	 * @param fdpCGWConfigDTOList
	 *            contains all cgw configurations.
	 * @param circleCode
	 *            the circle code.
	 * @return the circle end points for lb.
	 */
	private String[] getCircleEndPointsForLB(final List<FDPCGWConfigDTO> fdpCGWConfigDTOList, final String circleCode) {
		final int noOfEndpoints = fdpCGWConfigDTOList.size();

		final List<String> cgwCircleEndpointList = new ArrayList<String>();
		for (int i = 0; i < noOfEndpoints; i++) {
			final FDPCGWConfigDTO fdpCGWConfigDTO = fdpCGWConfigDTOList.get(i);
			final String circleCodeEndpoint = circleCode + fdpCGWConfigDTO.getExternalSystemId()
					+ BusinessConstants.UNDERSCORE + fdpCGWConfigDTO.getLogicalName();
			cgwCircleEndpointList.add(BusinessConstants.HTTP_COMPONENT_CGW_ENDPOINT + circleCodeEndpoint);
		}
		return cgwCircleEndpointList.toArray(new String[cgwCircleEndpointList.size()]);

	}

	/**
	 * This method is used to return CGW URL.
	 * 
	 * @param fdpCGWConfigDTO
	 *            the fdp cgw config dto
	 * @return CGW Url
	 */
	private String getCGWEndpointURLForCircle(final FDPCGWConfigDTO fdpCGWConfigDTO, final boolean isHttps) {
		String cgwUrl = "";
		if (isHttps) {
			cgwUrl = BusinessConstants.HTTPS_COMPONENT_TYPE + fdpCGWConfigDTO.getIpAddress().getValue()
					+ BusinessConstants.COLON + fdpCGWConfigDTO.getPort() + BusinessConstants.FORWARD_SLASH;
		} else {
			cgwUrl = BusinessConstants.HTTP_COMPONENT_TYPE + fdpCGWConfigDTO.getIpAddress().getValue()
					+ BusinessConstants.COLON + fdpCGWConfigDTO.getPort() + BusinessConstants.FORWARD_SLASH;
		}
		LOGGER.debug("CGW Url:" + cgwUrl);
		return cgwUrl;
	}
}
