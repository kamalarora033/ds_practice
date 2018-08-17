package com.ericsson.fdp.business.http.router;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.annotation.Resource;
import javax.ejb.Singleton;
import javax.inject.Inject;
import javax.jms.ConnectionFactory;

import org.apache.camel.ExchangePattern;
import org.apache.camel.RoutesBuilder;
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
import com.ericsson.fdp.business.route.processor.AbilityRequestProcessor;
import com.ericsson.fdp.business.route.processor.AbilitySyncRequestProcessor;
import com.ericsson.fdp.business.route.processor.LogOutgoingIpProcessor;
import com.ericsson.fdp.common.enums.ExternalSystem;
import com.ericsson.fdp.core.config.utils.PropertyUtils;
import com.ericsson.fdp.core.dsm.framework.service.FDPCache;
import com.ericsson.fdp.core.serviceprovisioning.bean.FDPAppBag;
import com.ericsson.fdp.dao.dto.FDPAbilityConfigDTO;
import com.ericsson.fdp.dao.fdpadmin.FDPAbilityConfigDAO;
import com.ericsson.fdp.dao.fdpadmin.FDPCircleDAO;
import com.ericsson.fdp.route.cdi.context.provider.CdiCamelContextProvider;
import com.ericsson.fdp.route.processors.LogRetryProcessor;
import com.ericsson.fdp.route.processors.RouteCompletionProcessor;

/**
 * The Class AbilityHttpRoute used to AIR routes over the Http Protocol and uses
 * camel http component.
 * 
 * @author Ericsson
 */
@Singleton(name = "AbilityHttpRoute")
public class AbilityHttpRoute {

	/** The cdi camel context provider. */
	@Inject
	private CdiCamelContextProvider cdiCamelContextProvider;

	/** The context. */
	private CdiCamelContext context;

	/** The fdp circle dao. */
	@Inject
	private FDPCircleDAO fdpCircleDAO;

	/** The fdp ability config dao. */
	@Inject
	private FDPAbilityConfigDAO fdpAbilityConfigDAO;

	/** The Constant LOGGER. */
	private static final Logger LOGGER = LoggerFactory.getLogger(AbilityHttpRoute.class);

	/**
	 * The headerProcessor is used to process the headers at run time mapped by
	 * ip and port.
	 */
	@Inject
	private LogRetryProcessor logRetryProcessor;
	
	
	@Inject
    private LogOutgoingIpProcessor logOutgoingIpProcessor;
	
	@Inject
	private RouteCompletionProcessor completionProcessor;
	   

	/** The request cache. */
	@Resource(lookup = "java:app/fdpCoreServices-1.0/ApplicationConfigCache")
	private FDPCache<FDPAppBag, Object> applicationConfigCache;

	@Resource(lookup = "java:app/fdpBusiness-1.0/LoadBalancerStorage")
	private LoadBalancerStorage loadBalancerStorage;

	@Resource(lookup = "java:app/fdpBusiness-1.0/ChoiceDefinitionStorage")
	private ChoiceDefinitionStorage choiceDefinitionStorage;
	
	@Inject
	AbilityRequestProcessor abilityRequestProcessor;
	
	@Inject
	AbilitySyncRequestProcessor abilityRequestProcessorNew;
	
	@Resource(mappedName = "java:jboss/activemq/ConnectionFactory")
	private ConnectionFactory connectionFactory;
	
	/**
	 * Creates the routes.
	 */
	public void createRoutes() throws Exception {
		
		context = cdiCamelContextProvider.getContext();
		context.addRoutes(createSyncRoutes());
	}

	private RoutesBuilder createSyncRoutes() {
			return new RouteBuilder(){
				@Override
				public void configure() throws Exception {
					
					final HttpComponent http = context.getComponent(BusinessConstants.HTTP_COMPONENT, HttpComponent.class);
					http.setHttpConnectionManager(getHttpConnectionManager());
					
					final boolean autostartup = true;
					final List<String> circleCodeList = getAllCircleCodes();
					final ChoiceDefinition routeDefinitionAbility = from(BusinessConstants.HTTP_COMPONENT_ABILITY_ENDPOINT)
							.routeId(BusinessConstants.MAIN_ROUTE_UNDERSCORE + ExternalSystem.Ability.name())
							.autoStartup(autostartup).choice();
					final Map<String, Object> choiceDefinitionMap = choiceDefinitionStorage.getchoiceDefinitionMap();
					choiceDefinitionMap.put(ExternalSystem.Ability.name(), routeDefinitionAbility);
					
					for (int i = 0; circleCodeList != null && i < circleCodeList.size(); i++) {

						final String routeId = BusinessConstants.SUB_ROUTE_UNDERSCORE + circleCodeList.get(i)
								+ BusinessConstants.UNDERSCORE + ExternalSystem.Ability.name();
						routeDefinitionAbility.when(header(BusinessConstants.CIRCLE_CODE).isEqualTo(circleCodeList.get(i)))
								.routeId(routeId)
								.to(BusinessConstants.HTTP_COMPONENT_ABILITY_ENDPOINT + "_" + circleCodeList.get(i))
								.endChoice();
						
						final List<FDPAbilityConfigDTO> fdpAbilityConfigDTOList = getEndpointURLsForAbilityByCircleCode(circleCodeList.get(i));
						
						final String[] endpoints = getCircleEndPointsForLB(fdpAbilityConfigDTOList, circleCodeList.get(i));
						final int fdpAbilityConfigDTOListSize = fdpAbilityConfigDTOList.size();
						
						LoadBalanceDefinition loadBalanceDefinition = null;
						if (fdpAbilityConfigDTOList != null && fdpAbilityConfigDTOListSize > 1) {

							loadBalanceDefinition = from(
									BusinessConstants.HTTP_COMPONENT_ABILITY_ENDPOINT + BusinessConstants.UNDERSCORE
											+ circleCodeList.get(i))
									.routeId(
											ExternalSystem.Ability.name() + BusinessConstants.UNDERSCORE
													+ circleCodeList.get(i)).setExchangePattern(ExchangePattern.InOut)
									.loadBalance()
									.failover(fdpAbilityConfigDTOListSize - 1, false, true, java.lang.Exception.class);
							for (final String endpoint : endpoints) {
								loadBalanceDefinition.routeId(endpoint).to(endpoint);
							}
						} else {
							if (fdpAbilityConfigDTOList != null && fdpAbilityConfigDTOListSize == 1) {
								from(BusinessConstants.HTTP_COMPONENT_ABILITY_ENDPOINT + "_" + circleCodeList.get(i))
								.setExchangePattern(ExchangePattern.InOut)
										.routeId(
												ExternalSystem.Ability.name() + BusinessConstants.UNDERSCORE
														+ circleCodeList.get(i))
														.to(endpoints);
							}
						}
						
						final String circleCode = circleCodeList.get(i);
						for (int j = 0; fdpAbilityConfigDTOList != null && j < fdpAbilityConfigDTOListSize; j++) {
							
							final FDPAbilityConfigDTO fdpAbilityConfigDTO = fdpAbilityConfigDTOList.get(j);
							final List<String> routeIdList = new ArrayList<String>();
							/**
							 * Manages the re-delivery attempt and re-delivery
							 * delay.
							 */
							final String circleCodeEndpoint = circleCode + fdpAbilityConfigDTO.getExternalSystemId()
									+ BusinessConstants.UNDERSCORE + fdpAbilityConfigDTO.getLogicalName();
							final String ipAddress = fdpAbilityConfigDTO.getIpAddress().getValue();
							final String circleCodeIpaddressPort = circleCode + BusinessConstants.COLON + ipAddress
									+ BusinessConstants.COLON + String.valueOf(fdpAbilityConfigDTO.getPort());
							final String routeId2 = BusinessConstants.SUB_ROUTE_UNDERSCORE + circleCodeEndpoint
									+ BusinessConstants.UNDERSCORE + ExternalSystem.Ability.name();
							
							
							routeIdList.add(routeId2);
							from(BusinessConstants.HTTP_COMPONENT_ABILITY_ENDPOINT + circleCodeEndpoint)
							.autoStartup(autostartup)
							.routeId(routeId2)
							 .onCompletion().onCompleteOnly().process(completionProcessor).end()
							.setProperty(BusinessConstants.OUTGOING_IP_ADDRESS, constant(ipAddress))
							.setProperty(BusinessConstants.OUTGOING_CIRCLE_CODE_IP_PORT,
									constant(circleCodeIpaddressPort))
                                .process(logOutgoingIpProcessor)
//							.process(headerProcessor)
							.onException(Exception.class)
							.handled(false)
							.maximumRedeliveries(fdpAbilityConfigDTO.getNumberOfRetry())
							.redeliveryDelay(fdpAbilityConfigDTO.getRetryInterval()).onRedelivery(logRetryProcessor)
							.to(BusinessConstants.COMPONENT_EXCEPTION_ENDPOINT).end()
							.to(getAbilityEndpointURLForCircle(fdpAbilityConfigDTO)).process(abilityRequestProcessorNew);
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
	 * Gets the endpoint ur ls for air by circle code.
	 * 
	 * @param circleCode
	 *            the circle code
	 * @return the endpoint ur ls for air by circle code
	 */
	private List<FDPAbilityConfigDTO> getEndpointURLsForAbilityByCircleCode(final String circleCode) {
		return fdpAbilityConfigDAO.getAbilityEndpointByCircleCode(circleCode);
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
	private String[] getCircleEndPointsForLB(final List<FDPAbilityConfigDTO> fdpAbilityConfigDTOList, final String circleCode) {
		final int noOfEndpoints = fdpAbilityConfigDTOList.size();

		final List<String> airCircleEndpointList = new ArrayList<String>();
		for (int i = 0; i < noOfEndpoints; i++) {
			final FDPAbilityConfigDTO fdpAbilityConfigDTO = fdpAbilityConfigDTOList.get(i);
			final String circleCodeEndpoint = circleCode + fdpAbilityConfigDTO.getExternalSystemId()
					+ BusinessConstants.UNDERSCORE + fdpAbilityConfigDTO.getLogicalName();
			airCircleEndpointList.add(BusinessConstants.HTTP_COMPONENT_ABILITY_ENDPOINT + circleCodeEndpoint);
		}
		return airCircleEndpointList.toArray(new String[airCircleEndpointList.size()]);

	}

	/**
	 * This method is used to return AIR URL.
	 * 
	 * @param fdpAIRConfigDTO
	 *            the fdp air config dto
	 * @return AIR Url
	 */
	private String getAbilityEndpointURLForCircle(final FDPAbilityConfigDTO fdpAbilityConfigDTO) {
		final String abilityUrl = BusinessConstants.HTTP_COMPONENT_TYPE + fdpAbilityConfigDTO.getIpAddress().getValue()
				+ BusinessConstants.COLON + fdpAbilityConfigDTO.getPort()+"/"+ fdpAbilityConfigDTO.getContextPath()+ BusinessConstants.QUERY_STRING_SEPARATOR
				+ BusinessConstants.HTTP_CLIENT_SO_TIMEOUT + BusinessConstants.EQUALS + fdpAbilityConfigDTO.getResponseTimeout();
		LOGGER.debug("Ability Url:" + abilityUrl);
		return abilityUrl;
	}
}
