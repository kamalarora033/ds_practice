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
import com.ericsson.fdp.dao.dto.FDPCMSConfigDTO;
import com.ericsson.fdp.dao.fdpadmin.FDPCMSConfigDAO;
import com.ericsson.fdp.dao.fdpadmin.FDPCircleDAO;
import com.ericsson.fdp.route.cdi.context.provider.CdiCamelContextProvider;
import com.ericsson.fdp.route.processors.LogRetryProcessor;
import com.ericsson.fdp.route.processors.RouteCompletionProcessor;

/**
 * The Class CMSHttpRoute used to create CMS routes over the Http Protocol and uses
 * camel http component.
 * 
 * @author eahmaim
 */
@Singleton(name = "CMSHttpRoute")
public class CMSHttpRoute {

	/** The cdi camel context provider. */
	@Inject
	private CdiCamelContextProvider cdiCamelContextProvider;

	/** The context. */
	private CdiCamelContext context;

	/** The fdp circle dao. */
	@Inject
	private FDPCircleDAO fdpCircleDAO;
	
	/** The fdp cms config dao. */
	@Inject
	private FDPCMSConfigDAO fdpCMSConfigDAO;
	
	
	/** The Constant LOGGER. */
	private static final Logger LOGGER = LoggerFactory.getLogger(CMSHttpRoute.class);

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
		context.addRoutes(createCMSHttpRoutes());
	}
	/**
	 * This method is responsible for creating the routes.
	 * 
	 * @return the route builder
	 */
	private RouteBuilder createCMSHttpRoutes() {
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
				
				final int maximumRedeliveryInterval = Integer.valueOf(PropertyUtils
						.getProperty("route.redelivery.delay"));

				final List<String> circleCodeList = getAllCircleCodes();
				
				final ChoiceDefinition routeDefinitionCMS = from(BusinessConstants.HTTP_COMPONENT_CMS_ENDPOINT)
				.routeId(BusinessConstants.MAIN_ROUTE_UNDERSCORE + ExternalSystem.CMS.name())
				.autoStartup(autostartup).choice();
				
				final Map<String, Object> choiceDefinitionMap = choiceDefinitionStorage.getchoiceDefinitionMap();
				choiceDefinitionMap.put(ExternalSystem.CMS.name(), routeDefinitionCMS);
				
				for (int i = 0; circleCodeList != null && i < circleCodeList.size(); i++) {

					final String routeId = BusinessConstants.SUB_ROUTE_UNDERSCORE + circleCodeList.get(i)
							+ BusinessConstants.UNDERSCORE + ExternalSystem.CMS.name();
					routeDefinitionCMS.when(header(BusinessConstants.CIRCLE_CODE).isEqualTo(circleCodeList.get(i)))
					.routeId(routeId)
					.to(BusinessConstants.HTTP_COMPONENT_CMS_ENDPOINT + "_" + circleCodeList.get(i))
					.endChoice();
					
					final List<FDPCMSConfigDTO> fdpCMSConfigDTOList = getEndpointURLsForCMSByCircleCode(circleCodeList
							.get(i));
					final String[] endpoints = getCircleEndPointsForLB(fdpCMSConfigDTOList, circleCodeList.get(i));
					
					final int fdpCMSConfigDTOListSize = fdpCMSConfigDTOList.size();
					/**
					 * Sub Route Defining routes for End Point to Circles CMS
					 * Servers , if we find two or more CMS URLs for the Circle
					 * then load-balancer will manages the load in round-robin
					 * manner in case of fail-over otherwise there will no
					 * load-balancer in case of single CMS Servers.
					 */
					
					LoadBalanceDefinition loadBalanceDefinition = null;
					if (fdpCMSConfigDTOList != null && fdpCMSConfigDTOListSize > 1) {

						loadBalanceDefinition = from(
								BusinessConstants.HTTP_COMPONENT_CMS_ENDPOINT + BusinessConstants.UNDERSCORE
										+ circleCodeList.get(i))
								.routeId(
										ExternalSystem.CMS.name() + BusinessConstants.UNDERSCORE
												+ circleCodeList.get(i)).setExchangePattern(ExchangePattern.InOut)
								.loadBalance()
								.failover(fdpCMSConfigDTOListSize - 1, false, true, java.lang.Exception.class);
						for (final String endpoint : endpoints) {

							loadBalanceDefinition.routeId(endpoint).to(endpoint);
						}
					}
					else{

						if (fdpCMSConfigDTOList != null && fdpCMSConfigDTOListSize == 1) {
							from(BusinessConstants.HTTP_COMPONENT_CMS_ENDPOINT + BusinessConstants.UNDERSCORE + circleCodeList.get(i))
									.setExchangePattern(ExchangePattern.InOut)
									.routeId(
											ExternalSystem.CMS.name() + BusinessConstants.UNDERSCORE
													+ circleCodeList.get(i)).to(endpoints);
						}
					
					}
					final String circleCode = circleCodeList.get(i);
					for (int j = 0; fdpCMSConfigDTOList != null && j < fdpCMSConfigDTOListSize; j++) {

						final FDPCMSConfigDTO fdpCMSConfigDTO = fdpCMSConfigDTOList.get(j);
						final List<String> routeIdList = new ArrayList<String>();
						/**
						 * Manages the re-delivery attempt and re-delivery
						 * delay.
						 */
						final String circleCodeEndpoint = circleCode + fdpCMSConfigDTO.getExternalSystemId()
								+ BusinessConstants.UNDERSCORE + fdpCMSConfigDTO.getLogicalName();
						final String ipAddress = fdpCMSConfigDTO.getIpAddress().getValue();
						final String circleCodeIpaddressPort = circleCode + BusinessConstants.COLON + ipAddress
								+ BusinessConstants.COLON + String.valueOf(fdpCMSConfigDTO.getPort());
						final String routeId2 = BusinessConstants.SUB_ROUTE_UNDERSCORE + circleCodeEndpoint
								+ BusinessConstants.UNDERSCORE + ExternalSystem.CMS.name();

						// routeIdList.add(BusinessConstants.HTTP_COMPONENT_CMS_ENDPOINT
						// + circleCodeEndpoint);
						routeIdList.add(routeId2);
						from(BusinessConstants.HTTP_COMPONENT_CMS_ENDPOINT + circleCodeEndpoint)
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
								.maximumRedeliveries(fdpCMSConfigDTO.getNumberOfRetry())
								.redeliveryDelay(maximumRedeliveryInterval).onRedelivery(logRetryProcessor)
								 .to(BusinessConstants.COMPONENT_EXCEPTION_ENDPOINT).end()
								.to(getCMSEndpointURLForCircle(fdpCMSConfigDTO));
					
				}
			}
		
			}	
	
			/*
			 * This method is used to set the http configuration in the HttpConnectionManager(
			 */
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
	 * Gets the endpoint ur ls for CMS by circle code.
	 * 
	 * @param circleCode
	 *            the circle code
	 * @return the endpoint ur ls for CMS by circle code
	 */
	private List<FDPCMSConfigDTO> getEndpointURLsForCMSByCircleCode(final String circleCode) {
		return fdpCMSConfigDAO.getCMSEndpointByCircleCode(circleCode);
	}
	
	/**
	 * Get Circlewise endpoints to weld load balancer.
	 * 
	 * @param fdpCMSConfigDTOList
	 *            contains all CMS configurations
	 * @param circleCode
	 *            the circle code
	 * @return the circle end points for lb
	 */
	private String[] getCircleEndPointsForLB(final List<FDPCMSConfigDTO> fdpCMSConfigDTOList, final String circleCode) {
		final int noOfEndpoints = fdpCMSConfigDTOList.size();

		final List<String> CMSCircleEndpointList = new ArrayList<String>();
		for (int i = 0; i < noOfEndpoints; i++) {
			final FDPCMSConfigDTO fdpCMSConfigDTO = fdpCMSConfigDTOList.get(i);
			final String circleCodeEndpoint = circleCode + fdpCMSConfigDTO.getExternalSystemId()
					+ BusinessConstants.UNDERSCORE + fdpCMSConfigDTO.getLogicalName();
			CMSCircleEndpointList.add(BusinessConstants.HTTP_COMPONENT_CMS_ENDPOINT + circleCodeEndpoint);
		}
		return CMSCircleEndpointList.toArray(new String[CMSCircleEndpointList.size()]);

	}
	
	/**
	 * This method is used to return CMS URL.
	 * 
	 * @param fdpCMSConfigDTO
	 *            the fdp CMS config dto
	 * @return CMS Url
	 */
	private String getCMSEndpointURLForCircle(final FDPCMSConfigDTO fdpCMSConfigDTO) {
		final String CMSUrl = BusinessConstants.HTTP_COMPONENT_TYPE + fdpCMSConfigDTO.getIpAddress().getValue()
				+ BusinessConstants.COLON + fdpCMSConfigDTO.getPort() + BusinessConstants.QUERY_STRING_SEPARATOR
				+ BusinessConstants.HTTP_CLIENT_SO_TIMEOUT + BusinessConstants.EQUALS + "0";
		LOGGER.debug("CMS Url:" + CMSUrl);
		return CMSUrl;
	}
}
