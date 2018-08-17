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
import org.apache.camel.component.http.HttpComponent;
import org.apache.camel.component.http.HttpOperationFailedException;
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
import com.ericsson.fdp.common.constants.FDPConstant;
import com.ericsson.fdp.common.enums.BusinessModuleType;
import com.ericsson.fdp.common.enums.ExternalSystem;
import com.ericsson.fdp.core.config.utils.PropertyUtils;
import com.ericsson.fdp.core.dsm.framework.service.FDPCache;
import com.ericsson.fdp.core.logging.FDPLogger;
import com.ericsson.fdp.core.logging.FDPLoggerFactory;
import com.ericsson.fdp.core.serviceprovisioning.bean.FDPAppBag;

import com.ericsson.fdp.dao.dto.FDPDMCConfigDTO;

import com.ericsson.fdp.dao.fdpadmin.FDPCircleDAO;
import com.ericsson.fdp.dao.fdpadmin.FDPDMCConfigDAO;
import com.ericsson.fdp.route.cdi.context.provider.CdiCamelContextProvider;
import com.ericsson.fdp.route.processors.LogRetryProcessor;

/**
 * The Class DMCHttpRoute used to DMC routes over the Http Protocol and uses
 * camel http component.
 * 
 * @author Ericsson
 */
@Singleton(name = "DMCHttpRoutes")
//@DependsOn(value = { "FDPSMSCRouter", "ApplicationMonitor" })
//@Startup
public class DMCHttpRoutes {

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
	private FDPDMCConfigDAO fdpDmcConfigDAO;

	/** The Constant LOGGER. */
	private static final Logger LOGGER = LoggerFactory.getLogger(DMCHttpRoutes.class);

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
		context.addRoutes(createDmcHttpRoutes());
	}

	/**
	 * This method is responsible for creating the routes.
	 * 
	 * @return the route builder
	 */
	private RouteBuilder createDmcHttpRoutes() {
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

				final ChoiceDefinition routeDefinitionDMC = from(BusinessConstants.HTTP_COMPONENT_DMC_ENDPOINT)
						.routeId(BusinessConstants.MAIN_ROUTE_UNDERSCORE + ExternalSystem.DMC.name())
						.autoStartup(autostartup).choice();
				final Map<String, Object> choiceDefinitionMap = choiceDefinitionStorage.getchoiceDefinitionMap();
				choiceDefinitionMap.put(ExternalSystem.DMC.name(), routeDefinitionDMC);
				for (int i = 0; circleCodeList != null && i < circleCodeList.size(); i++) {

					final String routeId = BusinessConstants.SUB_ROUTE_UNDERSCORE + circleCodeList.get(i)
							+ BusinessConstants.UNDERSCORE + ExternalSystem.DMC.name();
					routeDefinitionDMC.when(header(BusinessConstants.CIRCLE_CODE).isEqualTo(circleCodeList.get(i)))
							.routeId(routeId)
							.to(BusinessConstants.HTTP_COMPONENT_DMC_ENDPOINT + "_" + circleCodeList.get(i))
							.endChoice();

					final List<FDPDMCConfigDTO> fdpDMCConfigDTOList = getEndpointURLsForDMCByCircleCode(circleCodeList
							.get(i));

					// Parsing FDPAIRConfigDTO List into Endpoint store into
					// Array
					final String[] endpoints = getCircleEndPointsForLB(fdpDMCConfigDTOList, circleCodeList.get(i));

					final int fdpDMCConfigDTOListSize = fdpDMCConfigDTOList.size();
					/**
					 * Sub Route Defining routes for End Point to Circles AIR
					 * Servers , if we find two or more AIR URLs for the Circle
					 * then load-balancer will manages the load in round-robin
					 * manner in case of fail-over otherwise there will no
					 * load-balancer in case of single AIR Servers.
					 */
					LoadBalanceDefinition loadBalanceDefinition = null;
					if (fdpDMCConfigDTOList != null && fdpDMCConfigDTOListSize > 1) {

						loadBalanceDefinition = from(
								BusinessConstants.HTTP_COMPONENT_DMC_ENDPOINT + BusinessConstants.UNDERSCORE
										+ circleCodeList.get(i))
								.routeId(
										ExternalSystem.DMC.name() + BusinessConstants.UNDERSCORE
												+ circleCodeList.get(i)).setExchangePattern(ExchangePattern.InOut)
								.loadBalance()
								.failover(fdpDMCConfigDTOListSize - 1, false, true, java.lang.Exception.class);
						for (final String endpoint : endpoints) {

							loadBalanceDefinition.routeId(endpoint).to(endpoint);
						}
					} else {
						if (fdpDMCConfigDTOList != null && fdpDMCConfigDTOListSize == 1) {
							from(BusinessConstants.HTTP_COMPONENT_DMC_ENDPOINT + "_" + circleCodeList.get(i))
									.setExchangePattern(ExchangePattern.InOut)
									.routeId(
											ExternalSystem.DMC.name() + BusinessConstants.UNDERSCORE
													+ circleCodeList.get(i)).to(endpoints);
						}
					}
					final String circleCode = circleCodeList.get(i);
					for (int j = 0; fdpDMCConfigDTOList != null && j < fdpDMCConfigDTOListSize; j++) {

						final FDPDMCConfigDTO fdpDMCConfigDTO = fdpDMCConfigDTOList.get(j);
						final List<String> routeIdList = new ArrayList<String>();
						/**
						 * Manages the re-delivery attempt and re-delivery
						 * delay.
						 */
						final String circleCodeEndpoint = circleCode + fdpDMCConfigDTO.getExternalSystemId()
								+ BusinessConstants.UNDERSCORE + fdpDMCConfigDTO.getLogicalName();
						final String ipAddress = fdpDMCConfigDTO.getIpAddress().getValue();
						final String circleCodeIpaddressPort = circleCode + BusinessConstants.COLON + ipAddress
								+ BusinessConstants.COLON + String.valueOf(fdpDMCConfigDTO.getPort());
						final String routeId2 = BusinessConstants.SUB_ROUTE_UNDERSCORE + circleCodeEndpoint
								+ BusinessConstants.UNDERSCORE + ExternalSystem.DMC.name();

						// routeIdList.add(BusinessConstants.HTTP_COMPONENT_AIR_ENDPOINT
						// + circleCodeEndpoint);
						routeIdList.add(routeId2);
						from(BusinessConstants.HTTP_COMPONENT_DMC_ENDPOINT + circleCodeEndpoint)
								.routeId(routeId2)
								.setProperty(BusinessConstants.OUTGOING_IP_ADDRESS, constant(ipAddress))
								.setProperty(BusinessConstants.OUTGOING_CIRCLE_CODE_IP_PORT,
										constant(circleCodeIpaddressPort))
								.process(logOutgoingIpProcessor)
								.process(headerProcessor)
								.autoStartup(autostartup)
								.onException(java.lang.Exception.class, java.net.ConnectException.class,
										java.net.SocketTimeoutException.class,
										org.apache.camel.component.http.HttpOperationFailedException.class)
								// .onException(java.lang.Exception.class)
								.maximumRedeliveries(fdpDMCConfigDTO.getNumberOfRetry())
								.redeliveryDelay(fdpDMCConfigDTO.getRetryInterval()).onRedelivery(logRetryProcessor)
								.end().to(getDMCEndpointURLForCircle(fdpDMCConfigDTO));

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
	 * Call client.
	 * 
	 * @throws Exception
	 *             the exception
	 */
	

	/**
	 * This method is used to return all available circle codes.
	 * 
	 * @return circle codes.
	 */
	private List<String> getAllCircleCodes() {
		return fdpCircleDAO.getAllCircleCodes();
	}

	/**
	 * Gets the endpoint ur ls for air by circle code.
	 * 
	 * @param circleCode
	 *            the circle code
	 * @return the endpoint ur ls for air by circle code
	 */
	private List<FDPDMCConfigDTO> getEndpointURLsForDMCByCircleCode(final String circleCode) {
		return fdpDmcConfigDAO.getDMCEndpointByCircleCode(circleCode);
	}

	/**
	 * Get Circlewise endpoints to weld load balancer.
	 * 
	 * @param fdpAIRConfigDTOList
	 *            contains all air configurations
	 * @param circleCode
	 *            the circle code
	 * @return the circle end points for lb
	 */
	private String[] getCircleEndPointsForLB(final List<FDPDMCConfigDTO> fdpDMCConfigDTOList, final String circleCode) {
		final int noOfEndpoints = fdpDMCConfigDTOList.size();

		final List<String> dmcCircleEndpointList = new ArrayList<String>();
		for (int i = 0; i < noOfEndpoints; i++) {
			final FDPDMCConfigDTO fdpDMCConfigDTO = fdpDMCConfigDTOList.get(i);
			final String circleCodeEndpoint = circleCode + fdpDMCConfigDTO.getExternalSystemId()
					+ BusinessConstants.UNDERSCORE + fdpDMCConfigDTO.getLogicalName();
			dmcCircleEndpointList.add(BusinessConstants.HTTP_COMPONENT_DMC_ENDPOINT + circleCodeEndpoint);
		}
		return dmcCircleEndpointList.toArray(new String[dmcCircleEndpointList.size()]);

	}

	/**
	 * This method is used to return DMC URL.
	 * 
	 * @param fdpDMCConfigDTO
	 *            the fdp dmc config dto
	 * @return DMC Url
	 */
	private String getDMCEndpointURLForCircle(final FDPDMCConfigDTO fdpDMCConfigDTO) {
		final String dmcrUrl = BusinessConstants.HTTP_COMPONENT_TYPE + fdpDMCConfigDTO.getIpAddress().getValue()
				+ BusinessConstants.COLON + fdpDMCConfigDTO.getPort() + BusinessConstants.QUERY_STRING_SEPARATOR
				+ BusinessConstants.HTTP_CLIENT_SO_TIMEOUT + BusinessConstants.EQUALS + fdpDMCConfigDTO.getResponseTimeout();
		LOGGER.debug("Dmc Url:" + dmcrUrl);
		return dmcrUrl;
	}
}
