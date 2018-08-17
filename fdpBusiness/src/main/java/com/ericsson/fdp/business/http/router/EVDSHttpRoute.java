package com.ericsson.fdp.business.http.router;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.annotation.Resource;
import javax.inject.Inject;

import org.apache.camel.Exchange;
import org.apache.camel.ExchangePattern;
import org.apache.camel.Processor;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.cdi.CdiCamelContext;
import org.apache.camel.component.http.HttpComponent;
import org.apache.camel.model.ChoiceDefinition;
import org.apache.camel.model.LoadBalanceDefinition;
import org.apache.commons.httpclient.HostConfiguration;
import org.apache.commons.httpclient.MultiThreadedHttpConnectionManager;
import org.apache.commons.httpclient.params.HttpConnectionManagerParams;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ericsson.fdp.business.constants.BusinessConstants;
import com.ericsson.fdp.business.https.evds.HTTPSServerDetailsDTO;
import com.ericsson.fdp.business.https.evds.HttpsProcessor;
import com.ericsson.fdp.business.route.controller.ChoiceDefinitionStorage;
import com.ericsson.fdp.business.route.processor.HttpHeaderProcessor;
import com.ericsson.fdp.business.route.processor.LogOutgoingIpProcessor;
import com.ericsson.fdp.common.enums.ExternalSystem;
import com.ericsson.fdp.core.config.utils.PropertyUtils;
import com.ericsson.fdp.core.utils.ApplicationConfigUtil;
import com.ericsson.fdp.dao.dto.FDPEVDSConfigDTO;
import com.ericsson.fdp.dao.fdpadmin.FDPCircleDAO;
import com.ericsson.fdp.dao.fdpadmin.FDPEVDSConfigDAO;
import com.ericsson.fdp.route.cdi.context.provider.CdiCamelContextProvider;
import com.ericsson.fdp.route.processors.LogRetryProcessor;
import com.ericsson.fdp.route.processors.RouteCompletionProcessor;

public class EVDSHttpRoute {

	private static final String evdsType = PropertyUtils
			.getProperty("evds.protocol.type");

	/** The cdi camel context provider. */
	@Inject
	private CdiCamelContextProvider cdiCamelContextProvider;

	/** The context. */
	private CdiCamelContext context;

	/** The fdp circle dao. */
	@Inject
	private FDPCircleDAO fdpCircleDAO;

	/** The fdp air config dao. */
	@Inject
	private FDPEVDSConfigDAO fdpEVDSConfigDAO;

	/** The logOutgoingIpProcessor is used to log out ip addresses for exchange */
	@Inject
	private LogOutgoingIpProcessor logOutgoingIpProcessor;

	/**
	 * The headerProcessor is used to process the headers at run time mapped by
	 * ip and port.
	 */
	@Inject
	private HttpHeaderProcessor headerProcessor;

	@Inject
	private LogRetryProcessor logRetryProcessor;
	
    @Inject
    private RouteCompletionProcessor completionProcessor;

	/** The Constant LOGGER. */
	private static final Logger LOGGER = LoggerFactory
			.getLogger(EVDSHttpRoute.class);

	@Resource(lookup = "java:app/fdpBusiness-1.0/ChoiceDefinitionStorage")
	private ChoiceDefinitionStorage choiceDefinitionStorage;

	/**
	 * Creates the routes.
	 */
	public void createRoutes() throws Exception {
		context = cdiCamelContextProvider.getContext();
		context.getProperties().put(Exchange.LOG_DEBUG_BODY_MAX_CHARS, "5000");
		context.addRoutes(createEvdsRoutes());
	}

	/**
	 * This method is responsible for creating the routes.
	 * 
	 * @return the route builder
	 */
	private RouteBuilder createEvdsRoutes() {
		return new RouteBuilder() {
			@SuppressWarnings("unchecked")
			@Override
			public void configure() throws Exception {

				final HttpComponent http = context.getComponent(
						BusinessConstants.HTTP_COMPONENT, HttpComponent.class);
				http.setHttpConnectionManager(getHttpConnectionManager());

				// Create a direct main route for
				// EVDS(direct:nettyEVDS(routeId:mainroute_EVDS|autoStartup:true))
				final ChoiceDefinition routeDefinitionEVDS = from(
						BusinessConstants.HTTP_COMPONENT_EVDS_ENDPOINT)
						.routeId(
								BusinessConstants.MAIN_ROUTE_UNDERSCORE
										+ ExternalSystem.EVDS.name())
						.process(new Processor() {

							@Override
							public void process(Exchange exchange)
									throws Exception {
								
							}
						}).autoStartup(true).choice();

				// Add routeDefinitionEVDS to choiceDefinitionMap
				final Map<String, Object> choiceDefinitionMap = choiceDefinitionStorage
						.getchoiceDefinitionMap();
				choiceDefinitionMap.put(ExternalSystem.EVDS.name(),
						routeDefinitionEVDS);

				// Get all circle codes
				final List<String> circleCodeList = getAllCircleCodes();

				// Iterate over all circle codes and add the corresponding
				// routes
				for (int i = 0; circleCodeList != null
						&& i < circleCodeList.size(); i++) {

					// Create EVDS subroute id for the current circle id
					final String routeId = BusinessConstants.SUB_ROUTE_UNDERSCORE
							+ circleCodeList.get(i)
							+ BusinessConstants.UNDERSCORE
							+ ExternalSystem.EVDS.name();

					// Route the mainroute messages to their corresponding
					// circle subroute
					routeDefinitionEVDS
							.when(header(BusinessConstants.CIRCLE_CODE)
									.isEqualTo(circleCodeList.get(i)))
							.routeId(routeId)
							/*.process(new Processor() {

								@Override
								public void process(Exchange exchange)
										throws Exception {
									System.out.println(exchange
											+ ">>>>>>>EVDS 0");

								}
							})*/
							.to(BusinessConstants.HTTP_COMPONENT_EVDS_ENDPOINT
									+ "_" + circleCodeList.get(i)).endChoice();

					// Get the EVDS node connection details for the current
					// circle
					final List<FDPEVDSConfigDTO> fdpEVDSConfigDTOList = getEndpointURLsForEVDSByCircleCode(circleCodeList
							.get(i));
					LOGGER.info("size of fdpEVDSConfigDTOList :::::::"+fdpEVDSConfigDTOList.size() );

					// Create subroute endpoints within a circle for load
					// balancing the request
					final String[] endpoints = getCircleEndPointsForLB(
							fdpEVDSConfigDTOList, circleCodeList.get(i));

					/**
					 * Sub Route Defining routes for End Point to Circles AIR
					 * Servers , if we find two or more AIR URLs for the Circle
					 * then load-balancer will manages the load in round-robin
					 * manner in case of fail-over otherwise there will no
					 * load-balancer in case of single AIR Servers.
					 */
					LoadBalanceDefinition loadBalanceDefinition = null;
					if (fdpEVDSConfigDTOList != null
							&& fdpEVDSConfigDTOList.size() > 1) {
						loadBalanceDefinition = from(
								BusinessConstants.HTTP_COMPONENT_EVDS_ENDPOINT
										+ BusinessConstants.UNDERSCORE
										+ circleCodeList.get(i))
								.routeId(
										ExternalSystem.EVDS.name()
												+ BusinessConstants.UNDERSCORE
												+ circleCodeList.get(i))
								.process(new Processor() {

									@Override
									public void process(Exchange exchange)
											throws Exception {
										
									}
								})
								.setExchangePattern(ExchangePattern.InOut)
								.loadBalance()
								.failover(fdpEVDSConfigDTOList.size() - 1,
										false, true, java.lang.Exception.class);
						for (final String endpoint : endpoints) {
							loadBalanceDefinition.routeId(endpoint)
									.to(endpoint);
						}
					} else {
						if (fdpEVDSConfigDTOList != null
								&& fdpEVDSConfigDTOList.size() == 1) {
							// this method will map the evds http server to
							// httpserver detail

							HTTPSServerDetailsDTO httpsserverdetailsdto = mappEVDSHTTPSSeverDetails(fdpEVDSConfigDTOList
									.get(0));
							List<HTTPSServerDetailsDTO> httpsserverdetailsdtolst = new ArrayList<>();
							httpsserverdetailsdtolst.add(httpsserverdetailsdto);
							HttpsProcessor httpsprocessor = new HttpsProcessor(
									httpsserverdetailsdtolst);

							from(
									BusinessConstants.HTTP_COMPONENT_EVDS_ENDPOINT
											+ "_" + circleCodeList.get(i))
									.setExchangePattern(ExchangePattern.InOut)
									.routeId(
											ExternalSystem.EVDS.name()
													+ BusinessConstants.UNDERSCORE
													+ circleCodeList.get(i))
					                 .process(httpsprocessor);

							// .to(endpoints);
						}

					}

					// Iterate over EVDS nodes and connect circle subroute them
					for (int j = 0; fdpEVDSConfigDTOList != null
							&& j < fdpEVDSConfigDTOList.size(); j++) {

						// Get the current EVDS node
						final FDPEVDSConfigDTO fdpEVDSConfigDTO = fdpEVDSConfigDTOList
								.get(j);

						// Route list to store list of route id for the current
						// circle
						final List<String> routeIdList = new ArrayList<String>();

						final String circleCodeEndpoint = circleCodeList.get(i)
								+ fdpEVDSConfigDTO.getExternalSystemId()
								+ BusinessConstants.UNDERSCORE
								+ fdpEVDSConfigDTO.getLogicalName();
						final String ipAddress = fdpEVDSConfigDTO
								.getIpAddress().getValue();
						final String circleCodeIpaddressPort = circleCodeList
								.get(i)
								+ BusinessConstants.COLON
								+ ipAddress
								+ BusinessConstants.COLON
								+ String.valueOf(fdpEVDSConfigDTO.getPort());
						final String routeId2 = BusinessConstants.SUB_ROUTE_UNDERSCORE
								+ circleCodeEndpoint
								+ BusinessConstants.UNDERSCORE
								+ ExternalSystem.EVDS.name();

						routeIdList.add(routeId2);
						HTTPSServerDetailsDTO httpsserverdetailsdto = mappEVDSHTTPSSeverDetails(fdpEVDSConfigDTOList.get(j));
						List<HTTPSServerDetailsDTO> httpsserverdetailsdtolst = new ArrayList<>();
						httpsserverdetailsdtolst.add(httpsserverdetailsdto);
						HttpsProcessor httpsprocessor = new HttpsProcessor(httpsserverdetailsdtolst);

						from(
								BusinessConstants.HTTP_COMPONENT_EVDS_ENDPOINT
										+ circleCodeEndpoint)
								.autoStartup(true)
								.routeId(routeId2)
								.onCompletion().onCompleteOnly().process(completionProcessor).end()
								.setProperty(
										BusinessConstants.OUTGOING_IP_ADDRESS,
										constant(ipAddress))
								.setProperty(
										BusinessConstants.OUTGOING_CIRCLE_CODE_IP_PORT,
										constant(circleCodeIpaddressPort))
								.process(logOutgoingIpProcessor)
								.process(headerProcessor)
								.process(httpsprocessor)
								.onException(
										java.lang.Exception.class,
										java.net.ConnectException.class,
										java.net.SocketTimeoutException.class,
										org.apache.camel.component.http.HttpOperationFailedException.class)
								.handled(false)
								.maximumRedeliveries(
										fdpEVDSConfigDTO.getNumberOfRetry())
								.redeliveryDelay(
										fdpEVDSConfigDTO.getRetryInterval())
								.onRedelivery(logRetryProcessor)
								.to(BusinessConstants.COMPONENT_EXCEPTION_ENDPOINT)
								.end();
								//.to(getEVDSEndpointURLForCircle(fdpEVDSConfigDTO));

					}
				}
			}

			private MultiThreadedHttpConnectionManager getHttpConnectionManager() {
				/*
				 * KeyStoreParameters ksp = new KeyStoreParameters();
				 * ksp.setResource("mtnzambia-evd-pem.crt");
				 * ksp.setPassword("keystorePassword"); KeyManagersParameters
				 * kmp = new KeyManagersParameters(); kmp.setKeyStore(ksp);
				 * kmp.setKeyPassword("keyPassword");
				 * 
				 * SSLContextParameters scp = new SSLContextParameters();
				 * scp.setKeyManagers(kmp);
				 * 
				 * ProtocolSocketFactory factory = new
				 * SSLContextParametersSecureProtocolSocketFactory(scp);
				 * Protocol.registerProtocol("https", new Protocol( "https",
				 * factory, 443));
				 */
				final int maxTotalConnection = readIntPropertyValue(
						"http.maxtotalconnections", "200");
				final int defaultMaxConnectionPerHost = readIntPropertyValue(
						"http.default.max.total.connections.per.host", "5");
				final int maxConnectionPerHost = readIntPropertyValue(
						"http.max.connection.per.host", "20");
				final int httpSoTimeOut = readIntPropertyValue(
						"http.socket.timeout", "10000");
				final int connectionIdleCloseTime = readIntPropertyValue(
						"http.close.idle.time", "6000");
				final int connectionTimeout = readIntPropertyValue(
						"http.connection.timeout", "10000");

				final HttpConnectionManagerParams httpConnectionManagerParams = new HttpConnectionManagerParams();
				httpConnectionManagerParams
						.setMaxTotalConnections(maxTotalConnection);
				httpConnectionManagerParams
						.setDefaultMaxConnectionsPerHost(defaultMaxConnectionPerHost);
				httpConnectionManagerParams.setMaxConnectionsPerHost(
						HostConfiguration.ANY_HOST_CONFIGURATION,
						maxConnectionPerHost);
				httpConnectionManagerParams
						.setConnectionTimeout(connectionTimeout);
				httpConnectionManagerParams.setSoTimeout(httpSoTimeOut);

				final MultiThreadedHttpConnectionManager httpConnectionManager = new MultiThreadedHttpConnectionManager();
				httpConnectionManager
						.closeIdleConnections(connectionIdleCloseTime);
				httpConnectionManager.setParams(httpConnectionManagerParams);
				return httpConnectionManager;
			}

			private Integer readIntPropertyValue(final String propertyName,
					final String defaultValue) {
				String propertyValue = PropertyUtils.getProperty(propertyName);
				propertyValue = (null == propertyValue || ""
						.equals(propertyValue)) ? defaultValue : propertyValue;
				return Integer.valueOf(propertyValue);
			}
		};
	}

	/**
	 * Provide mapping between DTOs
	 * 
	 * @param fdpevdsConfigDTO
	 * @return
	 */
	private HTTPSServerDetailsDTO mappEVDSHTTPSSeverDetails(
			FDPEVDSConfigDTO fdpevdsConfigDTO) {
		HTTPSServerDetailsDTO httpsServerDetailsdto = new HTTPSServerDetailsDTO();
		httpsServerDetailsdto.setContext(fdpevdsConfigDTO.getContextPath());
		httpsServerDetailsdto.setIp(fdpevdsConfigDTO.getIpAddress().getValue());
	   // httpsServerDetailsdto.setAcceptlanguage(fdpevdsConfigDTO.getacceptlanguage);
		httpsServerDetailsdto.setLogicalname(fdpevdsConfigDTO.getLogicalName());
		httpsServerDetailsdto.setPort(fdpevdsConfigDTO.getPort());
		httpsServerDetailsdto.setTimeout(fdpevdsConfigDTO.getResponseTimeout());
		httpsServerDetailsdto.setIsenabled(fdpevdsConfigDTO.getIsActive());
		httpsServerDetailsdto.setUseragent(fdpevdsConfigDTO.getUserAgent());
		return httpsServerDetailsdto;

	}

	/**
	 * Get Circlewise endpoints for load balancer.
	 * 
	 * @param fdpAIRConfigDTOList
	 *            contains all air configurations
	 * @param circleCode
	 *            the circle code
	 * @return the circle end points for load balancer
	 */
	private String[] getCircleEndPointsForLB(
			final List<FDPEVDSConfigDTO> fdpEVDSConfigDTOList,
			final String circleCode) {
		final List<String> evdsCircleEndpointList = new ArrayList<String>();
		for (int i = 0; i < fdpEVDSConfigDTOList.size(); i++) {
			final FDPEVDSConfigDTO fdpEVDSConfigDTO = fdpEVDSConfigDTOList
					.get(i);
			final String circleCodeEndpoint = circleCode
					+ fdpEVDSConfigDTO.getExternalSystemId()
					+ BusinessConstants.UNDERSCORE
					+ fdpEVDSConfigDTO.getLogicalName();
			evdsCircleEndpointList
					.add(BusinessConstants.HTTP_COMPONENT_EVDS_ENDPOINT
							+ circleCodeEndpoint);
		}
		return evdsCircleEndpointList.toArray(new String[evdsCircleEndpointList
				.size()]);

	}

	/**
	 * This method is used to return EVDS endpoint URL.
	 * 
	 * @param fdpEVDSConfigDTO
	 *            the fdp EVDS config dto
	 * @return EVDS Url
	 */
	private String getEVDSEndpointURLForCircle(
			final FDPEVDSConfigDTO fdpEVDSConfigDTO) {
		final String evdsUrl = BusinessConstants.HTTP_COMPONENT_TYPE
				+ fdpEVDSConfigDTO.getIpAddress().getValue()
				+ BusinessConstants.COLON + fdpEVDSConfigDTO.getPort()
				+ BusinessConstants.FORWARD_SLASH
				+ fdpEVDSConfigDTO.getContextPath()
				+ BusinessConstants.QUERY_STRING_SEPARATOR
				+ BusinessConstants.HTTP_CLIENT_SO_TIMEOUT
				+ BusinessConstants.EQUALS
				+ fdpEVDSConfigDTO.getResponseTimeout();
		LOGGER.debug("EVDS Url:" + evdsUrl);
		return evdsUrl;
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
	 * Gets the endpoint urls for EVDS by circle code.
	 * 
	 * @param circleCode
	 *            the circle code
	 * @return the endpoint urls for EVDS by circle code
	 */
	private List<FDPEVDSConfigDTO> getEndpointURLsForEVDSByCircleCode(
			final String circleCode) {
		return fdpEVDSConfigDAO.getEVDSEndpointByCircleCode(circleCode);
	}

	/**
	 * This method stops all EVDS routes
	 * 
	 * @throws Exception
	 */
	public void stopAllRoutes() throws Exception {
		context = ApplicationConfigUtil.getCdiCamelContextProvider()
				.getContext();
		context.stopRoute(BusinessConstants.MAIN_ROUTE_UNDERSCORE
				+ ExternalSystem.EVDS.name());
		context.removeRoute(BusinessConstants.MAIN_ROUTE_UNDERSCORE
				+ ExternalSystem.EVDS.name());
	}

	/**
	 * This method stops and removes the EVDS subroute
	 * 
	 * @param fdpMMConfigDTO
	 * @throws Exception
	 */
	public void stopRoute(FDPEVDSConfigDTO fdpEvdsConfigDTO) throws Exception {
		context = ApplicationConfigUtil.getCdiCamelContextProvider()
				.getContext();
		final String circleCodeEndpoint = fdpEvdsConfigDTO.getCircleDTO()
				.getCircleCode()
				+ fdpEvdsConfigDTO.getExternalSystemId()
				+ BusinessConstants.UNDERSCORE
				+ fdpEvdsConfigDTO.getLogicalName();

		final String routeId = BusinessConstants.SUB_ROUTE_UNDERSCORE
				+ circleCodeEndpoint + BusinessConstants.UNDERSCORE
				+ ExternalSystem.EVDS.name();
		context.stopRoute(routeId);
		context.removeRoute(routeId);
	}

}
