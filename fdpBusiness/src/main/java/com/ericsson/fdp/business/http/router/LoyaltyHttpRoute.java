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
import com.ericsson.fdp.core.dsm.framework.service.FDPCache;
import com.ericsson.fdp.core.serviceprovisioning.bean.FDPAppBag;
import com.ericsson.fdp.core.utils.ApplicationConfigUtil;
import com.ericsson.fdp.dao.dto.FDPExternalSystemDTO;
import com.ericsson.fdp.dao.dto.FDPLoyaltyConfigDTO;
import com.ericsson.fdp.dao.fdpadmin.FDPCircleDAO;
import com.ericsson.fdp.dao.fdpadmin.FDPLoyaltyConfigDAO;
import com.ericsson.fdp.route.cdi.context.provider.CdiCamelContextProvider;
import com.ericsson.fdp.route.processors.LogRetryProcessor;
import com.ericsson.fdp.route.processors.RouteCompletionProcessor;

/**
 * The Class LoyaltyHttpRoute used to Loyalty routes over the Http Protocol and uses
 * camel http component.
 * 
 * @author Ericsson
 */

@Singleton
public class LoyaltyHttpRoute {

	/** The cdi camel context provider. */
	@Inject
	private CdiCamelContextProvider cdiCamelContextProvider;

	/** The context. */
	private CdiCamelContext context;

	/** The fdp circle dao. */
	@Inject
	private FDPCircleDAO fdpCircleDAO;

	/** The fdp loyalty config dao. */
	@Inject
	private FDPLoyaltyConfigDAO fdpLoyaltyConfigDAO;

	/** The Constant LOGGER. */
	private static final Logger LOGGER = LoggerFactory.getLogger(LoyaltyHttpRoute.class);

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
    

	/** The request cache. */
	@Resource(lookup = "java:app/fdpCoreServices-1.0/ApplicationConfigCache")
	private FDPCache<FDPAppBag, Object> applicationConfigCache;

	@Resource(lookup = "java:app/fdpBusiness-1.0/LoadBalancerStorage")
	private LoadBalancerStorage loadBalancerStorage;

	@Resource(lookup = "java:app/fdpBusiness-1.0/ChoiceDefinitionStorage")
	private ChoiceDefinitionStorage choiceDefinitionStorage;

	/**
	 * Creates the routes.
	 */
	public void createRoutes() throws Exception {

		context = cdiCamelContextProvider.getContext();
		context.addRoutes(createLoyaltyHttpRoutes());
	}

	/**
	 * This method is responsible for creating the routes.
	 * 
	 * @return the route builder
	 */
	private RouteBuilder createLoyaltyHttpRoutes() {
		return new RouteBuilder() {
			@SuppressWarnings("unchecked")
			@Override
			public void configure() throws Exception {

				final HttpComponent http = context.getComponent(BusinessConstants.HTTP_COMPONENT, HttpComponent.class);
				http.setHttpConnectionManager(getHttpConnectionManager());
				final boolean autostartup = true;
				/**
				 * Getting maximumRedeiveryAttempt and maximumRedeliveryInterval
				 * from Property file.
				 */

				final List<String> circleCodeList = getAllCircleCodes();

				final ChoiceDefinition routeDefinitionLoyalty = from(BusinessConstants.HTTP_COMPONENT_LOYALTY_ENDPOINT)
						.routeId(BusinessConstants.MAIN_ROUTE_UNDERSCORE + ExternalSystem.Loyalty.name())
						.autoStartup(autostartup).choice();
				final Map<String, Object> choiceDefinitionMap = choiceDefinitionStorage.getchoiceDefinitionMap();
				choiceDefinitionMap.put(ExternalSystem.Loyalty.name(), routeDefinitionLoyalty);
				for (int i = 0; circleCodeList != null && i < circleCodeList.size(); i++) {

					final String routeId = BusinessConstants.SUB_ROUTE_UNDERSCORE + circleCodeList.get(i)
							+ BusinessConstants.UNDERSCORE + ExternalSystem.Loyalty.name();
					routeDefinitionLoyalty.when(header(BusinessConstants.CIRCLE_CODE).isEqualTo(circleCodeList.get(i)))
							.routeId(routeId)
							.to(BusinessConstants.HTTP_COMPONENT_LOYALTY_ENDPOINT + "_" + circleCodeList.get(i))
							.endChoice();

					final List<FDPLoyaltyConfigDTO> fdpLoyaltyConfigDTOList = getEndpointURLsForLoyaltyByCircleCode(circleCodeList
							.get(i));

					// Parsing FDPLoyaltyConfigDTO List into Endpoint store into
					// Array
					final String[] endpoints = getCircleEndPointsForLB(fdpLoyaltyConfigDTOList, circleCodeList.get(i));

					final int fdpLoyaltyConfigDTOListSize = fdpLoyaltyConfigDTOList.size();
					/**
					 * Sub Route Defining routes for End Point to Circles Loyalty
					 * Servers , if we find two or more Loyalty URLs for the Circle
					 * then load-balancer will manages the load in round-robin
					 * manner in case of fail-over otherwise there will no
					 * load-balancer in case of single Loyalty Servers.
					 */
					
					LoadBalanceDefinition loadBalanceDefinition = null;
					if (fdpLoyaltyConfigDTOList != null && fdpLoyaltyConfigDTOListSize > 1) {

						loadBalanceDefinition = from(
								BusinessConstants.HTTP_COMPONENT_LOYALTY_ENDPOINT + BusinessConstants.UNDERSCORE
										+ circleCodeList.get(i))
								.routeId(
										ExternalSystem.Loyalty.name() + BusinessConstants.UNDERSCORE
												+ circleCodeList.get(i)).setExchangePattern(ExchangePattern.InOut)
								.loadBalance()
								.failover(fdpLoyaltyConfigDTOListSize - 1, false, true, java.lang.Exception.class);
						for (final String endpoint : endpoints) {

							loadBalanceDefinition.routeId(endpoint).to(endpoint);
						}
					} else {
						if (fdpLoyaltyConfigDTOList != null && fdpLoyaltyConfigDTOListSize == 1) {
							from(BusinessConstants.HTTP_COMPONENT_LOYALTY_ENDPOINT + "_" + circleCodeList.get(i))
									.setExchangePattern(ExchangePattern.InOut)
									.routeId(
											ExternalSystem.Loyalty.name() + BusinessConstants.UNDERSCORE
													+ circleCodeList.get(i)).to(endpoints);
						}
					}
					final String circleCode = circleCodeList.get(i);
					for (int j = 0; fdpLoyaltyConfigDTOList != null && j < fdpLoyaltyConfigDTOListSize; j++) {

						final FDPLoyaltyConfigDTO fdpLoyaltyConfigDTO = fdpLoyaltyConfigDTOList.get(j);
						final List<String> routeIdList = new ArrayList<>();
						/**
						 * Manages the re-delivery attempt and re-delivery
						 * delay.
						 */
						final String circleCodeEndpoint = circleCode + fdpLoyaltyConfigDTO.getExternalSystemId()
								+ BusinessConstants.UNDERSCORE + fdpLoyaltyConfigDTO.getLogicalName();
						final String ipAddress = fdpLoyaltyConfigDTO.getIpAddress().getValue();
						final String circleCodeIpaddressPort = circleCode + BusinessConstants.COLON + ipAddress
								+ BusinessConstants.COLON + String.valueOf(fdpLoyaltyConfigDTO.getPort());
						final String routeId2 = BusinessConstants.SUB_ROUTE_UNDERSCORE + circleCodeEndpoint
								+ BusinessConstants.UNDERSCORE + ExternalSystem.Loyalty.name();

						// routeIdList.add(BusinessConstants.HTTP_COMPONENT_Loyalty_ENDPOINT
						// + circleCodeEndpoint);
						routeIdList.add(routeId2);
						from(BusinessConstants.HTTP_COMPONENT_LOYALTY_ENDPOINT + circleCodeEndpoint)
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
								.maximumRedeliveries(fdpLoyaltyConfigDTO.getNumberOfRetry())
								.redeliveryDelay(fdpLoyaltyConfigDTO.getRetryInterval()).onRedelivery(logRetryProcessor)
								.to(BusinessConstants.COMPONENT_EXCEPTION_ENDPOINT).end()
								.to(getLoyaltyEndpointURLForCircle(fdpLoyaltyConfigDTO));

					}
				}
			}

			private MultiThreadedHttpConnectionManager getHttpConnectionManager() {
				final int maxTotalConnection = readIntPropertyValue("http.maxtotalconnections","200");
				final int defaultMaxConnectionPerHost = readIntPropertyValue("http.default.max.total.connections.per.host","5");
				final int maxConnectionPerHost = readIntPropertyValue("http.max.connection.per.host","20");
				final int httpSoTimeOut = readIntPropertyValue("http.socket.timeout","10000");
				final int connectionIdleCloseTime = readIntPropertyValue("http.close.idle.time","6000");
				final int connectionTimeout = readIntPropertyValue("http.connection.timeout","10000");

				final HttpConnectionManagerParams httpConnectionManagerParams = new HttpConnectionManagerParams();
				httpConnectionManagerParams.setMaxTotalConnections(maxTotalConnection);
				httpConnectionManagerParams.setDefaultMaxConnectionsPerHost(defaultMaxConnectionPerHost);
				httpConnectionManagerParams.setMaxConnectionsPerHost(HostConfiguration.ANY_HOST_CONFIGURATION,
						maxConnectionPerHost);
				httpConnectionManagerParams.setConnectionTimeout(connectionTimeout);
				httpConnectionManagerParams.setSoTimeout(httpSoTimeOut);
				
				
				final MultiThreadedHttpConnectionManager httpConnectionManager = new MultiThreadedHttpConnectionManager();
				httpConnectionManager.closeIdleConnections(connectionIdleCloseTime);
				httpConnectionManager.setParams(httpConnectionManagerParams);
				return httpConnectionManager;
			}

			private Integer readIntPropertyValue(final String propertyName, final String defaultValue) {
				String propertyValue = PropertyUtils.getProperty(propertyName);
				propertyValue = (null == propertyValue || "".equals(propertyValue))? defaultValue : propertyValue;
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
	 * Gets the endpoint ur ls for loyalty by circle code.
	 * 
	 * @param circleCode
	 *            the circle code
	 * @return the endpoint ur ls for loyalty by circle code
	 */
	private List<FDPLoyaltyConfigDTO> getEndpointURLsForLoyaltyByCircleCode(final String circleCode) {
		return fdpLoyaltyConfigDAO.getLoyaltyEndpointByCircleCode(circleCode);
	}

	/**
	 * Get Circlewise endpoints to weld load balancer.
	 * 
	 * @param fdpLoyaltyConfigDTOList
	 *            contains all loyalty configurations
	 * @param circleCode
	 *            the circle code
	 * @return the circle end points for lb
	 */
	private String[] getCircleEndPointsForLB(final List<FDPLoyaltyConfigDTO> fdpLoyaltyConfigDTOList, final String circleCode) {
		final int noOfEndpoints = fdpLoyaltyConfigDTOList.size();

		final List<String> loyaltyCircleEndpointList = new ArrayList<>();
		for (int i = 0; i < noOfEndpoints; i++) {
			final FDPLoyaltyConfigDTO fdpLoyaltyConfigDTO = fdpLoyaltyConfigDTOList.get(i);
			final String circleCodeEndpoint = circleCode + fdpLoyaltyConfigDTO.getExternalSystemId()
					+ BusinessConstants.UNDERSCORE + fdpLoyaltyConfigDTO.getLogicalName();
			loyaltyCircleEndpointList.add(BusinessConstants.HTTP_COMPONENT_LOYALTY_ENDPOINT + circleCodeEndpoint);
		}
		return loyaltyCircleEndpointList.toArray(new String[loyaltyCircleEndpointList.size()]);

	}

	/**
	 * This method is used to return Loyalty URL.
	 * 
	 * @param fdpLoyaltyConfigDTO
	 *            the fdp loyalty config dto
	 * @return Loyalty Url
	 */
	private String getLoyaltyEndpointURLForCircle(final FDPLoyaltyConfigDTO fdpLoyaltyConfigDTO) {
		return (new StringBuilder()).append(BusinessConstants.HTTP_COMPONENT_TYPE).append(fdpLoyaltyConfigDTO.getIpAddress().getValue())
				.append(BusinessConstants.COLON).append(fdpLoyaltyConfigDTO.getPort()).append("/").append(fdpLoyaltyConfigDTO.getContextPath()+BusinessConstants.QUERY_STRING_SEPARATOR)
				.append(BusinessConstants.HTTP_CLIENT_SO_TIMEOUT).append(BusinessConstants.EQUALS).append(fdpLoyaltyConfigDTO.getResponseTimeout()).toString();
	}

	public void stopAllRoutes() throws Exception {
		context = ApplicationConfigUtil.getCdiCamelContextProvider()
				.getContext();
		context.stopRoute(ExternalSystem.MM + "_MAIN");
		context.removeRoute(ExternalSystem.MM + "_MAIN");

		
	}

	public void stopRoute(FDPExternalSystemDTO newFDPExternalSystemDTO) throws Exception {

		context = ApplicationConfigUtil.getCdiCamelContextProvider()
				.getContext();
		final String circleCodeEndpoint = newFDPExternalSystemDTO.getCircleDTO().getCircleCode()
				+ newFDPExternalSystemDTO.getExternalSystemId()
				+ BusinessConstants.UNDERSCORE
				+ newFDPExternalSystemDTO.getLogicalName();
		
		final String routeId2 = BusinessConstants.SUB_ROUTE_UNDERSCORE
				+ circleCodeEndpoint
				+ BusinessConstants.UNDERSCORE
				+ ExternalSystem.Loyalty.name();
		
		context.stopRoute(routeId2);
		context.removeRoute(routeId2);
	
	}
}
