package com.ericsson.fdp.business.http.router;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.annotation.Resource;
import javax.ejb.Singleton;
import javax.inject.Inject;

import org.apache.camel.ExchangePattern;
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
import com.ericsson.fdp.business.route.controller.ChoiceDefinitionStorage;
import com.ericsson.fdp.business.route.controller.LoadBalancerStorage;
import com.ericsson.fdp.business.route.processor.HttpHeaderProcessor;
import com.ericsson.fdp.business.route.processor.LogOutgoingIpProcessor;
import com.ericsson.fdp.common.enums.ExternalSystem;
import com.ericsson.fdp.core.config.utils.PropertyUtils;
import com.ericsson.fdp.dao.dto.FDPADCConfigDTO;
import com.ericsson.fdp.dao.fdpadmin.FDPADCConfigDAO;
import com.ericsson.fdp.dao.fdpadmin.FDPCircleDAO;
import com.ericsson.fdp.route.cdi.context.provider.CdiCamelContextProvider;
import com.ericsson.fdp.route.processors.LogRetryProcessor;
import com.ericsson.fdp.route.processors.RouteCompletionProcessor;

/**
 * The Class ADCHttpRoute used to ADC routes over the Http Protocol and uses camel
 * http component.
 * 
 * @author Ericsson
 */
@Singleton(name = "ADCHttpRoute")
public class ADCHttpRoute {

	/** The cdi camel context provider. */
	@Inject
	private CdiCamelContextProvider cdiCamelContextProvider;

	/** The context. */
	private CdiCamelContext context;

	/** The fdp circle dao. */
	@Inject
	private FDPCircleDAO fdpCircleDAO;

	/** The fdp ADC config dao. */
	@Inject
	private FDPADCConfigDAO fdpADCConfigDAO;

	/** The Constant LOGGER. */
	private static final Logger LOGGER = LoggerFactory.getLogger(ADCHttpRoute.class);

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
		context.addRoutes(createADCHttpRoutes());
		LOGGER.debug("create Routes of ADC Called...");
		// callClient();
	}

	/**
	 * This method is responsible for creating the routes.
	 * 
	 * @return the route builder
	 */
	private RouteBuilder createADCHttpRoutes() {
		return new RouteBuilder() {
			@SuppressWarnings("unchecked")
			@Override
			public void configure() throws Exception {
				
				//final HttpComponent http = context.getComponent(BusinessConstants.HTTP_COMPONENT, HttpComponent.class);

               // http.setHttpConnectionManager(getHttpConnectionManager());

				final boolean autostartup = true;

				final List<String> circleCodeList = getAllCircleCodes();

				final ChoiceDefinition routeDefinitionADC = from(BusinessConstants.HTTP_COMPONENT_ADC_ENDPOINT)
						.routeId(BusinessConstants.MAIN_ROUTE_UNDERSCORE + ExternalSystem.ADC.name())
						.setHeader("user-agent", constant(BusinessConstants.USER_AGENT_STRING_ADC))
						.autoStartup(autostartup).choice();
				final Map<String, Object> choiceDefinitionMap = choiceDefinitionStorage.getchoiceDefinitionMap();
				choiceDefinitionMap.put(ExternalSystem.ADC.name(), routeDefinitionADC);
				for (int i = 0; circleCodeList != null && i < circleCodeList.size(); i++) {

					routeDefinitionADC
							.when(header(BusinessConstants.CIRCLE_CODE).isEqualTo(circleCodeList.get(i)))
							.routeId(
									BusinessConstants.SUB_ROUTE_UNDERSCORE + circleCodeList.get(i)
											+ BusinessConstants.UNDERSCORE + ExternalSystem.ADC.name())
							.to(BusinessConstants.HTTP_COMPONENT_ADC_ENDPOINT + BusinessConstants.UNDERSCORE
									+ circleCodeList.get(i)).endChoice().removeHeader(BusinessConstants.CIRCLE_CODE)
							.removeHeader("breadcrumbId");

					// Get List of the FDPADCConfigDTOs from DAO using
					// getEndpointURLsForRSByCircleCode()
					final List<FDPADCConfigDTO> fdpADCConfigDTOList = getEndpointURLsForADCByCircleCode(circleCodeList
							.get(i));

					// Parsing FDPADCConfigDTO List into Endpoint store into
					// Array
					final String[] endpoints = getCircleEndPointsForLB(fdpADCConfigDTOList, circleCodeList.get(i));

					final int fdpADCConfigDTOListSize = fdpADCConfigDTOList.size();
					/**
					 * Sub Route Defining routes for End Point to Circles ADC
					 * Servers , if we find two or more ADC URLs for the Circle
					 * then load-balancer will manages the load in round-robin
					 * manner in case of fail-over otherwise there will no
					 * load-balancer in case of single ADC Servers.
					 */
					LoadBalanceDefinition loadBalanceDefinition = null;
					if (fdpADCConfigDTOList != null && fdpADCConfigDTOListSize > 1) {
						loadBalanceDefinition = from(
								BusinessConstants.HTTP_COMPONENT_ADC_ENDPOINT + BusinessConstants.UNDERSCORE
										+ circleCodeList.get(i)).setExchangePattern(ExchangePattern.InOut)
								.loadBalance()
								.failover(fdpADCConfigDTOListSize - 1, false, true, java.net.ConnectException.class);
						for (final String endpoint : endpoints) {
							loadBalanceDefinition.routeId(endpoint).to(endpoint);
						}
					} else {
						if (fdpADCConfigDTOList != null && fdpADCConfigDTOListSize == 1) {
							from(
									BusinessConstants.HTTP_COMPONENT_ADC_ENDPOINT + BusinessConstants.UNDERSCORE
											+ circleCodeList.get(i)).setExchangePattern(ExchangePattern.InOut)
									.routeId(endpoints[0]).to(endpoints);
						}
					}
					final String circleCode = circleCodeList.get(i);
					for (int j = 0; fdpADCConfigDTOList != null && j < fdpADCConfigDTOListSize; j++) {

						final FDPADCConfigDTO fdpADCConfigDTO = fdpADCConfigDTOList.get(j);
						 
						final List<String> routeIdList = new ArrayList<String>();
						/**
						 * Manages the re-delivery attempt and re-delivery
						 * delay.
						 */
						final String circleCodeEndpoint = circleCode + fdpADCConfigDTO.getExternalSystemId()
								+ BusinessConstants.UNDERSCORE + fdpADCConfigDTO.getLogicalName();
						final String ipAddress = fdpADCConfigDTO.getIpAddress().getValue();
						final String circleCodeIpaddressPort = circleCode + BusinessConstants.COLON + ipAddress
								+ BusinessConstants.COLON + String.valueOf(fdpADCConfigDTO.getPort());
						final String routeId2 = BusinessConstants.SUB_ROUTE_UNDERSCORE + circleCodeEndpoint
								+ BusinessConstants.UNDERSCORE + ExternalSystem.ADC.name();

						routeIdList.add(routeId2);
						from(BusinessConstants.HTTP_COMPONENT_ADC_ENDPOINT + circleCodeEndpoint)
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
                                .maximumRedeliveries(fdpADCConfigDTO.getNumberOfRetry()).redeliveryDelay(fdpADCConfigDTO.getRetryInterval())
                                .onRedelivery(logRetryProcessor)
								.to(BusinessConstants.COMPONENT_EXCEPTION_ENDPOINT).end()
								.to(getADCEndpointURLForCircle(fdpADCConfigDTO));
					}
				}
			}
			
			/* private MultiThreadedHttpConnectionManager getHttpConnectionManager(FDPADCConfigDTO fdpADCConfigDTO) {
	               // final int maxTotalConnection = readIntPropertyValue("http.maxtotalconnections", "200");
	                final int defaultMaxConnectionPerHost = readIntPropertyValue("http.default.max.total.connections.per.host", "50");
	                //final int maxConnectionPerHost = readIntPropertyValue("http.max.connection.per.host", "200");
	                final int connectionIdleCloseTime = readIntPropertyValue("http.close.idle.time", "6000");

	                
	                final HttpConnectionManagerParams httpConnectionManagerParams = new HttpConnectionManagerParams();
	                httpConnectionManagerParams.setMaxTotalConnections(fdpADCConfigDTO.getNumberOfSession());
	                httpConnectionManagerParams.setDefaultMaxConnectionsPerHost(defaultMaxConnectionPerHost);
	                httpConnectionManagerParams.setMaxConnectionsPerHost(HostConfiguration.ANY_HOST_CONFIGURATION, fdpADCConfigDTO.getNumberOfSession());

	                final MultiThreadedHttpConnectionManager httpConnectionManager = new MultiThreadedHttpConnectionManager();
	                httpConnectionManager.closeIdleConnections(connectionIdleCloseTime);
	                httpConnectionManager.setParams(httpConnectionManagerParams);
	                return httpConnectionManager;
	            }*/


	            private Integer readIntPropertyValue(final String propertyName, final String defaultValue) {
	                String propertyValue = PropertyUtils.getProperty(propertyName);
	                propertyValue = (null == propertyValue || "".equals(propertyValue)) ? defaultValue : propertyValue;
	                return Integer.valueOf(propertyValue);
	            }
		};
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
	 * Gets the endpoint ur ls for ADC by circle code.
	 * 
	 * @param circleCode
	 *            the circle code
	 * @return the endpoint ur ls for ADC by circle code
	 */
	private List<FDPADCConfigDTO> getEndpointURLsForADCByCircleCode(final String circleCode) {
		return fdpADCConfigDAO.getADCEndpointByCircleCode(circleCode);
	}

	/**
	 * Get Circlewise endpoints to weld load balancer.
	 * 
	 * @param fdpADCConfigDTOList
	 *            contains all ADC configurations
	 * @param circleCode
	 *            the circle code
	 * @return the circle end points for lb
	 */
	private String[] getCircleEndPointsForLB(final List<FDPADCConfigDTO> fdpADCConfigDTOList, final String circleCode) {
		final int noOfEndpoints = fdpADCConfigDTOList.size();

		final List<String> ADCCircleEndpointList = new ArrayList<String>();
		for (int i = 0; i < noOfEndpoints; i++) {
			final FDPADCConfigDTO fdpADCConfigDTO = fdpADCConfigDTOList.get(i);
			final String circleCodeEndpoint = circleCode + fdpADCConfigDTO.getExternalSystemId()
					+ BusinessConstants.UNDERSCORE + fdpADCConfigDTO.getLogicalName();
			ADCCircleEndpointList.add(BusinessConstants.HTTP_COMPONENT_ADC_ENDPOINT + circleCodeEndpoint);
		}
		return ADCCircleEndpointList.toArray(new String[ADCCircleEndpointList.size()]);

	}

	/**
	 * This method is used to return ADC URL.
	 * 
	 * @param fdpADCConfigDTO
	 *            the fdp ADC config dto
	 * @return ADC Url
	 */
	private String getADCEndpointURLForCircle(final FDPADCConfigDTO fdpADCConfigDTO) {
		final String ADCUrl = BusinessConstants.HTTP_COMPONENT_TYPE + fdpADCConfigDTO.getIpAddress().getValue()
 + BusinessConstants.COLON
                + fdpADCConfigDTO.getPort() + BusinessConstants.QUERY_STRING_SEPARATOR + BusinessConstants.HTTP_CLIENT_SO_TIMEOUT
                + BusinessConstants.EQUALS + fdpADCConfigDTO.getResponseTimeout();
		LOGGER.debug("ADC Url:" + ADCUrl);
		return ADCUrl;
	}
}
