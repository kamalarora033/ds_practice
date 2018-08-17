package com.ericsson.fdp.business.http.router;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.annotation.Resource;
import javax.ejb.DependsOn;
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
import org.apache.camel.model.ChoiceDefinition;
import org.apache.camel.model.LoadBalanceDefinition;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ericsson.fdp.business.constants.BusinessConstants;
import com.ericsson.fdp.business.route.controller.ChoiceDefinitionStorage;
import com.ericsson.fdp.business.route.controller.LoadBalancerStorage;
import com.ericsson.fdp.business.route.processor.HttpHeaderProcessor;
import com.ericsson.fdp.business.route.processor.LogOutgoingIpProcessor;
import com.ericsson.fdp.common.enums.ExternalSystem;
import com.ericsson.fdp.dao.dto.FDPRSConfigDTO;
import com.ericsson.fdp.dao.fdpadmin.FDPCircleDAO;
import com.ericsson.fdp.dao.fdpadmin.FDPRSConfigDAO;
import com.ericsson.fdp.route.cdi.context.provider.CdiCamelContextProvider;
import com.ericsson.fdp.route.processors.LogRetryProcessor;
import com.ericsson.fdp.route.processors.RouteCompletionProcessor;

/**
 * The Class RSHttpRoute used to RS routes over the Http Protocol and uses camel
 * http component.
 * 
 * @author Ericsson
 */
@Singleton(name = "RSHttpRoute")
@DependsOn(value = "FDPSMSCRouter")
public class RSHttpRoute {

	/** The cdi camel context provider. */
	@Inject
	private CdiCamelContextProvider cdiCamelContextProvider;

	/** The context. */
	private CdiCamelContext context;

	/** The fdp circle dao. */
	@Inject
	private FDPCircleDAO fdpCircleDAO;

	/** The fdp rs config dao. */
	@Inject
	private FDPRSConfigDAO fdpRsConfigDAO;

	/** The Constant LOGGER. */
	private static final Logger LOGGER = LoggerFactory.getLogger(RSHttpRoute.class);

	/** The logOutgoingIpProcessor is used to log out ip addresses for exchange */
	@Inject
	private LogOutgoingIpProcessor logOutgoingIpProcessor;
	
    @Inject
    private RouteCompletionProcessor completionProcessor;
    
	/**
	 * The headerProcessor is used to process the headers at run time mapped by
	 * ip and port.
	 */
	@Inject
	private HttpHeaderProcessor headerProcessor;

	@Inject
	private LogRetryProcessor logRetryProcessor;

	@Resource(lookup = "java:app/fdpBusiness-1.0/LoadBalancerStorage")
	private LoadBalancerStorage loadBalancerStorage;

	@Resource(lookup = "java:app/fdpBusiness-1.0/ChoiceDefinitionStorage")
	private ChoiceDefinitionStorage choiceDefinitionStorage;

	/**
	 * Creates the routes.
	 */
	public void createRoutes() throws Exception {

		context = cdiCamelContextProvider.getContext();
		context.addRoutes(createRsHttpRoutes());
		LOGGER.debug("create Routes of RS Called...");
		// callClient();
	}

	/**
	 * This method is responsible for creating the routes.
	 * 
	 * @return the route builder
	 */
	private RouteBuilder createRsHttpRoutes() {
		return new RouteBuilder() {
			@SuppressWarnings("unchecked")
			@Override
			public void configure() throws Exception {

				final boolean autostartup = true;

				/**
				 * Getting maximumRedeiveryAttempt and maximumRedeliveryInterval
				 * from Property file.
				 */
                /*
                 * final int maximumRedeiveryAttempt = Integer.valueOf(PropertyUtils .getProperty("route.maximum.redelivery.attempt"));
                 * final int maximumRedeliveryInterval = Integer.valueOf(PropertyUtils .getProperty("route.redelivery.delay"));
                 */

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

				final ChoiceDefinition routeDefinitionRS = from(BusinessConstants.HTTP_COMPONENT_RS_ENDPOINT)
						.routeId(BusinessConstants.MAIN_ROUTE_UNDERSCORE + ExternalSystem.RS.name())
						.setHeader("user-agent", constant(BusinessConstants.USER_AGENT_STRING_RS))
						.autoStartup(autostartup).choice();
				final Map<String, Object> choiceDefinitionMap = choiceDefinitionStorage.getchoiceDefinitionMap();
				choiceDefinitionMap.put(ExternalSystem.RS.name(), routeDefinitionRS);
				for (int i = 0; circleCodeList != null && i < circleCodeList.size(); i++) {

					routeDefinitionRS
							.when(header(BusinessConstants.CIRCLE_CODE).isEqualTo(circleCodeList.get(i)))
							.routeId(
									BusinessConstants.SUB_ROUTE_UNDERSCORE + circleCodeList.get(i)
											+ BusinessConstants.UNDERSCORE + ExternalSystem.RS.name())
							.to(BusinessConstants.HTTP_COMPONENT_RS_ENDPOINT + BusinessConstants.UNDERSCORE
									+ circleCodeList.get(i)).endChoice().removeHeader(BusinessConstants.CIRCLE_CODE)
							.removeHeader("breadcrumbId");

					// Get List of the FDPRSConfigDTOs from DAO using
					// getEndpointURLsForRSByCircleCode()
					final List<FDPRSConfigDTO> fdpRSConfigDTOList = getEndpointURLsForRSByCircleCode(circleCodeList
							.get(i));

					// Parsing FDPRSConfigDTO List into Endpoint store into
					// Array
					final String[] endpoints = getCircleEndPointsForLB(fdpRSConfigDTOList, circleCodeList.get(i));

					final int fdpRSConfigDTOListSize = fdpRSConfigDTOList.size();
					/**
					 * Sub Route Defining routes for End Point to Circles RS
					 * Servers , if we find two or more RS URLs for the Circle
					 * then load-balancer will manages the load in round-robin
					 * manner in case of fail-over otherwise there will no
					 * load-balancer in case of single RS Servers.
					 */
					LoadBalanceDefinition loadBalanceDefinition = null;
					if (fdpRSConfigDTOList != null && fdpRSConfigDTOListSize > 1) {
						loadBalanceDefinition = from(
								BusinessConstants.HTTP_COMPONENT_RS_ENDPOINT + BusinessConstants.UNDERSCORE
										+ circleCodeList.get(i)).setExchangePattern(ExchangePattern.InOut)
								.loadBalance()
								.failover(fdpRSConfigDTOListSize - 1, false, true, java.net.ConnectException.class);
						for (final String endpoint : endpoints) {
							loadBalanceDefinition.routeId(endpoint).to(endpoint);
						}
					} else {
						if (fdpRSConfigDTOList != null && fdpRSConfigDTOListSize == 1) {
							from(
									BusinessConstants.HTTP_COMPONENT_RS_ENDPOINT + BusinessConstants.UNDERSCORE
											+ circleCodeList.get(i)).setExchangePattern(ExchangePattern.InOut)
									.routeId(endpoints[0]).to(endpoints);
						}
					}
					final String circleCode = circleCodeList.get(i);
					for (int j = 0; fdpRSConfigDTOList != null && j < fdpRSConfigDTOListSize; j++) {

						final FDPRSConfigDTO fdpRSConfigDTO = fdpRSConfigDTOList.get(j);
						final List<String> routeIdList = new ArrayList<String>();
						/**
						 * Manages the re-delivery attempt and re-delivery
						 * delay.
						 */
						final String circleCodeEndpoint = circleCode + fdpRSConfigDTO.getExternalSystemId()
								+ BusinessConstants.UNDERSCORE + fdpRSConfigDTO.getLogicalName();
						final String ipAddress = fdpRSConfigDTO.getIpAddress().getValue();
						final String circleCodeIpaddressPort = circleCode + BusinessConstants.COLON + ipAddress
								+ BusinessConstants.COLON + String.valueOf(fdpRSConfigDTO.getPort());
						final String routeId2 = BusinessConstants.SUB_ROUTE_UNDERSCORE + circleCodeEndpoint
								+ BusinessConstants.UNDERSCORE + ExternalSystem.RS.name();

						routeIdList.add(routeId2);
						from(BusinessConstants.HTTP_COMPONENT_RS_ENDPOINT + circleCodeEndpoint)
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
                                .maximumRedeliveries(fdpRSConfigDTO.getNumberOfRetry()).redeliveryDelay(fdpRSConfigDTO.getRetryInterval())
                                .onRedelivery(logRetryProcessor)
								.to(BusinessConstants.COMPONENT_EXCEPTION_ENDPOINT).end()
								.to(getRSEndpointURLForCircle(fdpRSConfigDTO));
					}
				}
			}
		};
	}

	/**
	 * Call client.
	 * 
	 * @throws Exception
	 *             the exception
	 */
	public void callClient() throws Exception {
		LOGGER.debug("Call Client Of RS Called...");

		LOGGER.debug("RS Dummy Request Starts:");
		final String httpRequest = "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?>"
				+ "\n<SingleProvisioningRequest>" + "\n<userName>ab</userName>" + "\n<password>ab</password>"
				+ "\n<vendorId>1</vendorId>" + "\n<serviceId>5232330</serviceId>" + "\n<msisdn>918527948299</msisdn>"
				+ "\n<isAdvanceRenewal>0</isAdvanceRenewal>" + "\n<renewalCount>10</renewalCount>"
				+ "\n</SingleProvisioningRequest>";
		final Endpoint endpoint = context.getEndpoint(BusinessConstants.HTTP_COMPONENT_RS_ENDPOINT,
				DirectEndpoint.class);
		final Exchange exchange = endpoint.createExchange();
		exchange.setPattern(ExchangePattern.InOut);
		final Message in = exchange.getIn();
		in.setHeader(Exchange.HTTP_PROTOCOL_VERSION, BusinessConstants.HTTP_VERSION_RS);
		in.setHeader(Exchange.CONTENT_TYPE, BusinessConstants.HTTP_CONTENT_TYPE_RS);
		in.setHeader(Exchange.HTTP_METHOD, "POST");
		in.setHeader(Exchange.HTTP_METHOD, BusinessConstants.HTTP_METHOD);
		in.setHeader(Exchange.CONTENT_LENGTH, httpRequest.length());
		in.setHeader("Accept", BusinessConstants.HEADER_ACCEPT_VALUE_RS);
		in.setHeader("Host", "10.2.48.124:8080");
		exchange.setProperty(BusinessConstants.CIRCLE_CODE, "DEL");
		exchange.setProperty(BusinessConstants.EXTERNAL_SYSTEM_TYPE, "RS");
		exchange.setProperty(BusinessConstants.REQUEST_ID, "789");
		in.setBody(httpRequest);
		// in.setHeader(Exchange.HTTP_QUERY, "input=" +
		// httpRequest1.toString());
		String outputXML = "";
		final Producer producer = endpoint.createProducer();
		// producer.start();
		producer.process(exchange);
		final Message out = exchange.getOut();
		outputXML = out.getBody(String.class);
		final String responseCode = out.getHeader("camelhttpresponsecode", String.class);
		LOGGER.debug("RS outputXML :" + outputXML);
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
	 * Gets the endpoint ur ls for rs by circle code.
	 * 
	 * @param circleCode
	 *            the circle code
	 * @return the endpoint ur ls for rs by circle code
	 */
	private List<FDPRSConfigDTO> getEndpointURLsForRSByCircleCode(final String circleCode) {
		return fdpRsConfigDAO.getRSEndpointByCircleCode(circleCode);
	}

	/**
	 * Get Circlewise endpoints to weld load balancer.
	 * 
	 * @param fdpRSConfigDTOList
	 *            contains all rs configurations
	 * @param circleCode
	 *            the circle code
	 * @return the circle end points for lb
	 */
	private String[] getCircleEndPointsForLB(final List<FDPRSConfigDTO> fdpRSConfigDTOList, final String circleCode) {
		final int noOfEndpoints = fdpRSConfigDTOList.size();

		final List<String> rsCircleEndpointList = new ArrayList<String>();
		for (int i = 0; i < noOfEndpoints; i++) {
			final FDPRSConfigDTO fdpRSConfigDTO = fdpRSConfigDTOList.get(i);
			final String circleCodeEndpoint = circleCode + fdpRSConfigDTO.getExternalSystemId()
					+ BusinessConstants.UNDERSCORE + fdpRSConfigDTO.getLogicalName();
			rsCircleEndpointList.add(BusinessConstants.HTTP_COMPONENT_RS_ENDPOINT + circleCodeEndpoint);
		}
		return rsCircleEndpointList.toArray(new String[rsCircleEndpointList.size()]);

	}

	/**
	 * This method is used to return RS URL.
	 * 
	 * @param fdpRSConfigDTO
	 *            the fdp rs config dto
	 * @return RS Url
	 */
	private String getRSEndpointURLForCircle(final FDPRSConfigDTO fdpRSConfigDTO) {
		final String rsUrl = BusinessConstants.HTTP_COMPONENT_TYPE + fdpRSConfigDTO.getIpAddress().getValue()
 + BusinessConstants.COLON
                + fdpRSConfigDTO.getPort() + BusinessConstants.QUERY_STRING_SEPARATOR + BusinessConstants.HTTP_CLIENT_SO_TIMEOUT
                + BusinessConstants.EQUALS + fdpRSConfigDTO.getResponseTimeout();
		LOGGER.debug("RS Url:" + rsUrl);
		return rsUrl;
	}
}
