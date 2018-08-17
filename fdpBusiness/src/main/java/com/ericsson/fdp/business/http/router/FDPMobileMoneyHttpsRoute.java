package com.ericsson.fdp.business.http.router;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.annotation.Resource;
import javax.ejb.Singleton;
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
import com.ericsson.fdp.business.https.mobileMoney.HttpsProcessorMobileMoney;
import com.ericsson.fdp.business.route.controller.ChoiceDefinitionStorage;
import com.ericsson.fdp.business.route.processor.HttpHeaderProcessor;
import com.ericsson.fdp.business.route.processor.LogOutgoingIpProcessor;
import com.ericsson.fdp.common.enums.ExternalSystem;
import com.ericsson.fdp.core.config.utils.PropertyUtils;
import com.ericsson.fdp.core.utils.ApplicationConfigUtil;
import com.ericsson.fdp.dao.dto.FDPMobileMoneyConfigDTO;
import com.ericsson.fdp.dao.fdpadmin.FDPCircleDAO;
import com.ericsson.fdp.dao.fdpadmin.FDPMobileMoneyConfigDAO;
import com.ericsson.fdp.route.cdi.context.provider.CdiCamelContextProvider;
import com.ericsson.fdp.route.processors.LogRetryProcessor;
import com.ericsson.fdp.route.processors.RouteCompletionProcessor;

/**
 * Session Bean implementation class FDPMobileMoneyHttpsRoute
 */
@Singleton(name = "FDPMobileMoneyHttpsRoute")

public class FDPMobileMoneyHttpsRoute {

	/** The cdi camel context provider. */
	@Inject
	private CdiCamelContextProvider cdiCamelContextProvider;

	/** The context. */
	private CdiCamelContext context;

	/** The logOutgoingIpProcessor is used to log out ip addresses for exchange */
	@Inject
	private LogOutgoingIpProcessor logOutgoingIpProcessor;

	@Inject
	private LogRetryProcessor logRetryProcessor;
	
	@Inject
	private RouteCompletionProcessor completionProcessor;
	 

	/** The fdp circle dao. */
	@Inject
	private FDPCircleDAO fdpCircleDAO;

	@Resource(lookup = "java:app/fdpBusiness-1.0/ChoiceDefinitionStorage")
	private ChoiceDefinitionStorage choiceDefinitionStorage;

	/** The fdp air config dao. */
	@Inject
	private FDPMobileMoneyConfigDAO fdpMobileMoneyConfigDAO;
	
	@Inject
	private HttpsProcessorMobileMoney mobilemoneyContext;
	
	/** The Constant LOGGER. */
	private static final Logger LOGGER = LoggerFactory
			.getLogger(FDPMobileMoneyHttpsRoute.class);

	/**
	 * The headerProcessor is used to process the headers at run time mapped by
	 * ip and port.
	 */
	@Inject
	private HttpHeaderProcessor headerProcessor;

	/**
	 * Creates the routes.
	 */
	public void createRoutes() throws Exception {

		context = cdiCamelContextProvider.getContext();
		context.getProperties().put("http.keepAlive", "false");
		context.addRoutes(createMobileMoneyHttpsRoutes());
	}

	private RouteBuilder createMobileMoneyHttpsRoutes() {
		
		return new RouteBuilder() {
			@SuppressWarnings("unchecked")
			@Override
			public void configure() throws Exception {
				final List<String> circleCodeList = getAllCircleCodes();
				final boolean autostartup = true;
				final HttpComponent http = context.getComponent(
						BusinessConstants.HTTP_COMPONENT, HttpComponent.class);
				http.setHttpConnectionManager(getHttpConnectionManager());
				final ChoiceDefinition routeDefinitionAIR = from(
						BusinessConstants.DIRECT_COMPONENT_MOBILEMONEY_ENDPOINT)
						.routeId(
								BusinessConstants.MAIN_ROUTE_UNDERSCORE
										+ ExternalSystem.MM.name())
						.autoStartup(autostartup).choice();

				final Map<String, Object> choiceDefinitionMap = choiceDefinitionStorage
						.getchoiceDefinitionMap();

				choiceDefinitionMap.put(ExternalSystem.MM.name(),
						routeDefinitionAIR);

				for (String circlecode : circleCodeList) {
					final String routeId = BusinessConstants.SUB_ROUTE_UNDERSCORE
							+ circlecode
							+ BusinessConstants.UNDERSCORE
							+ ExternalSystem.MM.name();

				
					routeDefinitionAIR
							.when(header(BusinessConstants.CIRCLE_CODE)
									.isEqualTo(circlecode))
							.routeId(routeId)
							.to(BusinessConstants.DIRECT_COMPONENT_MOBILEMONEY_ENDPOINT
									+ "_" + circlecode).endChoice();
					final List<FDPMobileMoneyConfigDTO> fdpMobileMoneyConfigDTOList = getendpointurlsformobliemoneybycirclecode(circlecode);
					
					final String[] endpoints = getCircleEndPointsForLB(
							fdpMobileMoneyConfigDTOList, circlecode);

					final int fdpMobileMoneyConfigDTOListSize = fdpMobileMoneyConfigDTOList
							.size();

					/**
					 * Sub Route Defining routes for End Point to Circles AIR
					 * Servers , if we find two or more AIR URLs for the Circle
					 * then load-balancer will manages the load in round-robin
					 * manner in case of fail-over otherwise there will no
					 * load-balancer in case of single AIR Servers.
					 */
					LoadBalanceDefinition loadBalanceDefinition = null;

					if (fdpMobileMoneyConfigDTOList != null
							&& fdpMobileMoneyConfigDTOListSize > 1) {
						loadBalanceDefinition = from(
								BusinessConstants.DIRECT_COMPONENT_MOBILEMONEY_ENDPOINT
										+ BusinessConstants.UNDERSCORE
										+ circlecode)
								.routeId(
										ExternalSystem.MM.name()
												+ BusinessConstants.UNDERSCORE
												+ circlecode)
								.setExchangePattern(ExchangePattern.InOut)
								.loadBalance()
								.failover(fdpMobileMoneyConfigDTOListSize - 1,
										false, true, java.lang.Exception.class);

						for (final String endpoint : endpoints) {

							loadBalanceDefinition.routeId(endpoint)
									.to(endpoint);
						}
					} else {
						if (fdpMobileMoneyConfigDTOList != null
								&& fdpMobileMoneyConfigDTOListSize == 1) {
							
							HTTPSServerDetailsDTO httpsserverdetailsdto = fdpMobileMoneyConfigDTOList(fdpMobileMoneyConfigDTOList.get(0));
							List<HTTPSServerDetailsDTO> httpsserverdetailsdtolst = new ArrayList<>();
							httpsserverdetailsdtolst.add(httpsserverdetailsdto);
							HttpsProcessorMobileMoney httpsprocessor = new HttpsProcessorMobileMoney(
									httpsserverdetailsdtolst);
							
									from(
									BusinessConstants.DIRECT_COMPONENT_MOBILEMONEY_ENDPOINT
											+ "_" + circlecode)
									.setExchangePattern(ExchangePattern.InOut)
									.routeId(
											ExternalSystem.MM.name()
													+ BusinessConstants.UNDERSCORE
													+ circlecode).process(httpsprocessor);//to(endpoints);
						}
					}

					for (int j = 0; fdpMobileMoneyConfigDTOList != null
							&& j < fdpMobileMoneyConfigDTOListSize; j++) {
						final FDPMobileMoneyConfigDTO fdpMobileMoneyConfigDTO = fdpMobileMoneyConfigDTOList
								.get(j);
						final List<String> routeIdList = new ArrayList<String>();

						final String circleCodeEndpoint = circlecode
								+ fdpMobileMoneyConfigDTO.getExternalSystemId()
								+ BusinessConstants.UNDERSCORE
								+ fdpMobileMoneyConfigDTO.getLogicalName();
						final String ipAddress = fdpMobileMoneyConfigDTO
								.getIpAddress().getValue();
						final String circleCodeIpaddressPort = circlecode
								+ BusinessConstants.COLON
								+ ipAddress
								+ BusinessConstants.COLON
								+ String.valueOf(fdpMobileMoneyConfigDTO
										.getPort());

						final String routeId2 = BusinessConstants.SUB_ROUTE_UNDERSCORE
								+ circleCodeEndpoint
								+ BusinessConstants.UNDERSCORE
								+ ExternalSystem.MM.name();

						routeIdList.add(routeId2);
						
						HTTPSServerDetailsDTO httpsserverdetailsdto = fdpMobileMoneyConfigDTOList(fdpMobileMoneyConfigDTOList.get(j));
						List<HTTPSServerDetailsDTO> httpsserverdetailsdtolst = new ArrayList<>();
						httpsserverdetailsdtolst.add(httpsserverdetailsdto);
						HttpsProcessorMobileMoney httpsprocessor = new HttpsProcessorMobileMoney(httpsserverdetailsdtolst);

						from(
								BusinessConstants.DIRECT_COMPONENT_MOBILEMONEY_ENDPOINT
										+ circleCodeEndpoint)
								.autoStartup(autostartup)
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
								.process(mobilemoneyContext)
								.process(httpsprocessor)
								.onException(
										java.lang.Exception.class,
										java.net.ConnectException.class,
										java.net.SocketTimeoutException.class,
										org.apache.camel.component.http.HttpOperationFailedException.class)
										.handled(false)
								// .onException(java.lang.Exception.class)
								.maximumRedeliveries(
										fdpMobileMoneyConfigDTO
												.getNumberOfRetry())
								.redeliveryDelay(
										fdpMobileMoneyConfigDTO
												.getRetryInterval())
								.onRedelivery(logRetryProcessor)
								.to(BusinessConstants.COMPONENT_EXCEPTION_ENDPOINT).end();
								/*.process(mobilemoneyContext)
								.to(getMobileMoneyEndpointURLForCircle(fdpMobileMoneyConfigDTO));*/

					}

				}

			}
			
			private MultiThreadedHttpConnectionManager getHttpConnectionManager() {
				
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
			private HTTPSServerDetailsDTO fdpMobileMoneyConfigDTOList(
					FDPMobileMoneyConfigDTO fdpmmConfigDTO) {
				HTTPSServerDetailsDTO httpsServerDetailsdto = new HTTPSServerDetailsDTO();
				httpsServerDetailsdto.setContext(fdpmmConfigDTO.getContextPath());
				httpsServerDetailsdto.setIp(fdpmmConfigDTO.getIpAddress().getValue());
			   // httpsServerDetailsdto.setAcceptlanguage(fdpevdsConfigDTO.getacceptlanguage);
				httpsServerDetailsdto.setLogicalname(fdpmmConfigDTO.getLogicalName());
				httpsServerDetailsdto.setPort(fdpmmConfigDTO.getPort());
				httpsServerDetailsdto.setTimeout(fdpmmConfigDTO.getResponseTimeout());
				httpsServerDetailsdto.setIsenabled(fdpmmConfigDTO.getIsActive());
				httpsServerDetailsdto.setUseragent(fdpmmConfigDTO.getUserAgent());
				return httpsServerDetailsdto;

			}

			private String[] getCircleEndPointsForLB(
					List<FDPMobileMoneyConfigDTO> fdpMobileMoneyConfigDTOList,
					String circleName) {

				final int noOfEndpoints = fdpMobileMoneyConfigDTOList.size();

				final List<String> mobileMoneyCircleEndpointList = new ArrayList<String>();
				for (int i = 0; i < noOfEndpoints; i++) {

					final FDPMobileMoneyConfigDTO fdomobilemoneydto = fdpMobileMoneyConfigDTOList
							.get(i);

					final String circleCodeEndpoint = circleName
							+ fdomobilemoneydto.getExternalSystemId()
							+ BusinessConstants.UNDERSCORE
							+ fdomobilemoneydto.getLogicalName();

					
					mobileMoneyCircleEndpointList
							.add(BusinessConstants.DIRECT_COMPONENT_MOBILEMONEY_ENDPOINT
									+ circleCodeEndpoint);
				}
				return mobileMoneyCircleEndpointList
						.toArray(new String[mobileMoneyCircleEndpointList
								.size()]);

			}

	/**
	 * This method is used to return all available circle codes.
	 * 
	 * @return circle codes.
	 */
	private List<String> getAllCircleCodes() {
		return fdpCircleDAO.getAllCircleCodes();
	}

	private List<FDPMobileMoneyConfigDTO> getendpointurlsformobliemoneybycirclecode(
			final String circlecode) {
		return fdpMobileMoneyConfigDAO
				.getmobilemoneyenfpointbycirclecode(circlecode);
	}

	/**
	 * This method is used to return AIR URL.
	 * 
	 * @param fdpMobileMoneyConfigDTO
	 *            the fdp Mobile Money config dto
	 * @return Mobile Money Url
	 *//*
	private String getMobileMoneyEndpointURLForCircle(
			final FDPMobileMoneyConfigDTO fdpmobilemoneyConfigDTO) {
		final String mobilemoneyUrl = BusinessConstants.HTTP_COMPONENT_TYPE
				+ fdpmobilemoneyConfigDTO.getIpAddress().getValue()
				+ BusinessConstants.COLON + fdpmobilemoneyConfigDTO.getPort()+"?throwExceptionOnFailure=false"
				;
		LOGGER.debug("Mobile money  Url:" + mobilemoneyUrl);
		return mobilemoneyUrl;
	}*/

	public void stopAllRoutes() throws Exception {
		context = ApplicationConfigUtil.getCdiCamelContextProvider()
				.getContext();
		context.stopRoute(BusinessConstants.MAIN_ROUTE_UNDERSCORE
				+ ExternalSystem.MM.name());
		context.removeRoute(BusinessConstants.MAIN_ROUTE_UNDERSCORE
				+ ExternalSystem.MM.name());

	}

	public void stopRoute(FDPMobileMoneyConfigDTO fdpMMConfigDTO) throws Exception {
		context = ApplicationConfigUtil.getCdiCamelContextProvider()
				.getContext();
		final String circleCodeEndpoint = fdpMMConfigDTO.getCircleDTO().getCircleCode()
				+ fdpMMConfigDTO.getExternalSystemId()
				+ BusinessConstants.UNDERSCORE
				+ fdpMMConfigDTO.getLogicalName();
		
		final String routeId2 = BusinessConstants.SUB_ROUTE_UNDERSCORE
				+ circleCodeEndpoint
				+ BusinessConstants.UNDERSCORE
				+ ExternalSystem.MM.name();
		
		context.stopRoute(routeId2);
		context.removeRoute(routeId2);
	}
}
