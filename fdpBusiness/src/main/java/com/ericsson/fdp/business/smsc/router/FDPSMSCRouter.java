package com.ericsson.fdp.business.smsc.router;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.Resource;
import javax.ejb.Singleton;
import javax.inject.Inject;
import javax.jms.ConnectionFactory;

import org.apache.camel.Exchange;
import org.apache.camel.RoutesBuilder;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.cdi.CdiCamelContext;
import org.apache.camel.component.jms.JmsComponent;
import org.apache.camel.model.ChoiceDefinition;
import org.apache.camel.model.LoadBalanceDefinition;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ericsson.fdp.business.constants.BusinessConstants;
import com.ericsson.fdp.business.route.controller.ChoiceDefinitionStorage;
import com.ericsson.fdp.business.route.controller.LoadBalancerStorage;
import com.ericsson.fdp.business.route.predicate.CircleCheckerPredicate;
import com.ericsson.fdp.business.route.processor.LogOutgoingIpProcessor;
import com.ericsson.fdp.business.route.processor.LogTransceiverProcessor;
import com.ericsson.fdp.business.route.processor.ProcessErrorMessages;
import com.ericsson.fdp.business.route.processor.RequestProcessor;
import com.ericsson.fdp.business.smsc.throtller.ThrotllerWaterGate;
import com.ericsson.fdp.business.smsc.throtller.ThrottlingScope;
import com.ericsson.fdp.common.util.RouteUtil;
import com.ericsson.fdp.core.cache.AppCacheSubStore;
import com.ericsson.fdp.core.cache.service.SMPPServerMappingService;
import com.ericsson.fdp.core.config.utils.PropertyUtils;
import com.ericsson.fdp.core.dsm.framework.service.FDPCache;
import com.ericsson.fdp.core.serviceprovisioning.bean.FDPAppBag;
import com.ericsson.fdp.core.utils.ApplicationUtil;
import com.ericsson.fdp.dao.dto.FDPSMSCConfigDTO;
import com.ericsson.fdp.dao.dto.SMPPServerMappingDTO;
import com.ericsson.fdp.dao.entity.ExternalSystemType;
import com.ericsson.fdp.dao.fdpadmin.FDPCircleDAO;
import com.ericsson.fdp.route.cdi.context.provider.CdiCamelContextProvider;
import com.ericsson.fdp.route.constant.RoutingConstant;
import com.ericsson.fdp.route.processors.CircleFinder;
import com.ericsson.fdp.route.processors.IncomingRequestIPProcessor;
import com.ericsson.fdp.route.processors.LogRetryProcessor;
import com.ericsson.fdp.route.processors.OutBoundQueueProcessor;
import com.ericsson.fdp.route.processors.ReceiptProcessor;
import com.ericsson.fdp.route.processors.RequestIdGenerator;
import com.ericsson.fdp.route.processors.ResponseProcessor;
import com.ericsson.fdp.smpp.util.SMPPUtil;

/**
 * The Class FDPSMSCRouter is used to configure the routes from multiple SMSCs
 * to FDP.
 * 
 * @author Ericsson
 */
@Singleton(name = "FDPSMSCRouter")
public class FDPSMSCRouter {

	/** The cdi camel context provider. */
	@Inject
	private CdiCamelContextProvider cdiCamelContextProvider;

	/** The circle finder. */
	@Inject
	private CircleFinder circleFinder;

	/** The request id generator. */
	@Inject
	private RequestIdGenerator requestIdGenerator;

	/** The in message processor. */
	@Inject
	private RequestProcessor requestProcessor;

	/** The process error messages. */
	@Inject
	private ProcessErrorMessages processErrorMessages;

	/** The route policy. */
	@Inject
	private ThrotllerWaterGate routePolicyMainRoute, routePolicyCircleRoute;

	/** The connection Factory. */
	@Resource(mappedName = "java:jboss/activemq/ConnectionFactory")
	private ConnectionFactory connectionFactory;

	/** The Constant LOGGER. */
	private static final Logger LOGGER = LoggerFactory
			.getLogger(FDPSMSCRouter.class);

	/** The fdpsmsc config dao. */
	@Resource(lookup = "java:app/fdpCoreServices-1.0/SMPPServerMappingServiceImpl")
	private SMPPServerMappingService serverMappingService;

	/** The fdp circle dao. */
	@Inject
	private FDPCircleDAO fdpCircleDAO;

	/** The receipt processor. */
	@Inject
	private ReceiptProcessor receiptProcessor;

	/** The incoming request processor. */
	@Inject
	private IncomingRequestIPProcessor incomingRequestIPProcessor;

	/** The out bound queue processor. */
	@Inject
	private OutBoundQueueProcessor outBoundQueueProcessor;

	/** The response processor. */
	@Inject
	private ResponseProcessor responseProcessor;

	/** The log Outgoing Ip Processor. */
	@Inject
	private LogOutgoingIpProcessor logOutgoingIpProcessor;

	/** The log retry processor. */
	@Inject
	private LogRetryProcessor logRetryProcessor;

	/** The log transceiver processor. */
	@Inject
	private LogTransceiverProcessor logTransceiverProcessor;

	/** The load balancer storage. */
	@Resource(lookup = "java:app/fdpBusiness-1.0/LoadBalancerStorage")
	private LoadBalancerStorage loadBalancerStorage;

	/** The load balancer storage. */
	@Resource(lookup = "java:app/fdpCoreServices-1.0/ApplicationConfigCache")
	private FDPCache<FDPAppBag, Object> applicationConfigCache;

	/** The choice definition storage. */
	@Resource(lookup = "java:app/fdpBusiness-1.0/ChoiceDefinitionStorage")
	private ChoiceDefinitionStorage choiceDefinitionStorage;
	
	final static String isGeoredEnabled = PropertyUtils.getProperty(BusinessConstants.IS_GEORED_ENABLED);

	/**
	 * Creates the routes.
	 * @param Boolean
	 * 		 the enableRX
	 * @throws Exception
	 *             the exception
	 */
	public void createRoutes(Boolean enableRX) throws Exception {

		final String serverName = ApplicationUtil.getServerName();

		final List<String> circleCodesList = getAllCircleCodes();
		final String[] circleCodes = convertCircleCodeListToArray(circleCodesList);
		final List<CircleCheckerPredicate> circleCheckerPredicates = getCircleCheckerPredicates(circleCodes);
		LOGGER.debug("List of Circles : {}", circleCodesList);

		final List<SMPPServerMappingDTO> circleSMSCMapForSMS = getSMSCDetailsForSMS(serverName);
		// final List<SMPPServerMappingDTO> circleSMSCMapForUSSD =
		// getSMSCDetailsForUSSD(serverName);
		LOGGER.debug("circleSMSCMapForSMS {}", circleSMSCMapForSMS);
		// LOGGER.debug("circleSMSCMapForUSSD {}", circleSMSCMapForUSSD);
		final CdiCamelContext context = cdiCamelContextProvider.getContext();

		// String ussdBindMode = PropertyUtils.getProperty("ussd.bind.mode");
		// ussdBindMode = (ussdBindMode != null && !"".equals(ussdBindMode)) ?
		// ussdBindMode
		// : BusinessConstants.BIND_MODE_TRX;
		String smscBindMode = PropertyUtils.getProperty("smsc.bind.mode");
		smscBindMode = (smscBindMode != null && !"".equals(smscBindMode)) ? smscBindMode
				: BusinessConstants.BIND_MODE_TXRX;
		final Map<String, String> bindModeMap = new HashMap<String, String>();
		// bindModeMap.put(BusinessConstants.SERVICE_TYPE_USSD, ussdBindMode);
		bindModeMap.put(BusinessConstants.SERVICE_TYPE_SMS, smscBindMode);
		context.getProperties().put(Exchange.LOG_DEBUG_BODY_MAX_CHARS, "5000");

		try
		{
			if (isGeoredEnabled != null && isGeoredEnabled.equalsIgnoreCase(BusinessConstants.YES)) {
				
				if (enableRX) {
					context.addRoutes(from(circleSMSCMapForSMS,  /*circleSMSCMapForUSSD*/ 
					null, bindModeMap, serverName, circleCheckerPredicates));
				} else if (isSiteActive() && !enableRX){
					createTxRxRoute(context, circleSMSCMapForSMS, serverName, bindModeMap, circleCodes, circleCheckerPredicates);
				} else if (!isSiteActive() && !enableRX) {
					context.addComponent(RoutingConstant.JMS_COMPONENT_NAME, JmsComponent.jmsComponent(connectionFactory));
					context.addRoutes(
							to(circleSMSCMapForSMS, /* circleSMSCMapForUSSD */null, bindModeMap, serverName, circleCodes));
					context.addRoutes(createUSSDRoutes(circleCodes));
					context.addRoutes(createUSSDOutLinkRoutes(circleCodes));
				}
			} else {
				createTxRxRoute(context, circleSMSCMapForSMS, serverName, bindModeMap, circleCodes, circleCheckerPredicates);
			}
		} catch(Exception e)
		{
			LOGGER.error("SMS ROUTE Connection error",e);
		}
		LOGGER.debug("Bind Mode Map:{}", bindModeMap);

	}

	private RoutesBuilder createUSSDRoutes(final String[] circleCodes) {
		return new RouteBuilder() {
			@Override
			public void configure() throws Exception {
				for (final String circleCode : circleCodes) {
					from(SMPPUtil.getUSSDInQueue(circleCode)).process(
							requestProcessor);
				}
			}
		};
	}

	private RoutesBuilder createUSSDOutLinkRoutes(final String[] circleCodes) {
		return new RouteBuilder() {
			@Override
			public void configure() throws Exception {
				for (final String circleCode : circleCodes) {
					from(SMPPUtil.getUSSDOutQueueEndpoint(circleCode)).to(
							SMPPUtil.getUSSDOutQueue(circleCode));
				}
			}
		};
	}

	/**
	 * Gets the circle checker predicates.
	 * 
	 * @param circleCodes
	 *            the circle codes
	 * @return the circle checker predicates
	 */
	private List<CircleCheckerPredicate> getCircleCheckerPredicates(
			final String[] circleCodes) {
		final List<CircleCheckerPredicate> checkerPredicates = new ArrayList<CircleCheckerPredicate>();
		for (final String circleCode : circleCodes) {
			checkerPredicates.add(new CircleCheckerPredicate(circleCode));
		}
		return checkerPredicates;
	}

	/**
	 * From.
	 * 
	 * @param circleSMSCMapForSMS
	 *            the circle smsc map for sms
	 * @param circleSMSCMapForUSSD
	 *            the circle smsc map for ussd
	 * @param bindModeMap
	 *            the bind mode map
	 * @param serverName
	 *            the server name
	 * @param circleCheckerPredicates
	 *            the circle checker predicates
	 * @return the route builder
	 */
	private RouteBuilder from(
			final List<SMPPServerMappingDTO> circleSMSCMapForSMS,
			final List<SMPPServerMappingDTO> circleSMSCMapForUSSD,
			final Map<String, String> bindModeMap, final String serverName,
			final List<CircleCheckerPredicate> circleCheckerPredicates) {

		return new RouteBuilder() {

			@Override
			public void configure() throws Exception {

				routePolicyMainRoute.setScope(ThrottlingScope.Route);
				routePolicyMainRoute
						.setMaxInflightExchanges(Integer.valueOf(PropertyUtils
								.getProperty("throtller.maxInflightExchanges")));

				routePolicyCircleRoute.setScope(ThrottlingScope.Route);
				routePolicyCircleRoute
						.setMaxInflightExchanges(Integer.valueOf(PropertyUtils
								.getProperty("throtller.maxInflightExchangesForCircle")));

				final boolean autostartup = true;
				final String concurrentConsumers = PropertyUtils
						.getProperty("route.main.concurrentconsumer.rx");
				for (final String serviceType : bindModeMap.keySet()) {

					final String bindMode = bindModeMap.get(serviceType);
					if ("TRX".equals(bindMode)) {
						LOGGER.debug("Service Type:" + serviceType);
						if (BusinessConstants.SERVICE_TYPE_SMS
								.equals(serviceType)) {
							/**
							 * Defining routes SMS SMSCs to the end point Rx
							 */
							for (final SMPPServerMappingDTO smppServerMappingDTO : circleSMSCMapForSMS) {
								final String endpoint = getSMSCEndPointsTRx(
										smppServerMappingDTO, serverName);
								if (endpoint != null && !"".equals(endpoint)) {
									final String routeId = smppServerMappingDTO
											.getLogicalName()
											+ BusinessConstants.UNDERSCORE
											+ serverName;
									from(endpoint)
											.process(incomingRequestIPProcessor)
											.setHeader(
													BusinessConstants.BIND_MODE_TYPE,
													constant(BusinessConstants.BIND_NODE_TYPE_TRANSCEIVER))
											.routeId(routeId)
											.setHeader(
													BusinessConstants.BIND_MODE,
													constant(BusinessConstants.BIND_MODE_TRX))
											.autoStartup(autostartup)
											.to(BusinessConstants.CAMEL_COMPONENT_TYPE
													+ "Rx?concurrentConsumers="
													+ concurrentConsumers);
								}
							}

						}

					}
					if ("TXRX".equals(bindMode)) {

						if (BusinessConstants.SERVICE_TYPE_SMS
								.equals(serviceType)) {
							/**
							 * Defining routes SMS SMSCs to the end point Rx
							 */
							for (final SMPPServerMappingDTO smppServerMappingDTO : circleSMSCMapForSMS) {								
								final String endpoint = getSMSCEndPointsRx(
										smppServerMappingDTO, serverName);
								if (endpoint != null && !"".equals(endpoint)) {
									final String routeId = smppServerMappingDTO
											.getLogicalName()
											+ BusinessConstants.UNDERSCORE
											+ serverName;
									from(endpoint)
											.process(incomingRequestIPProcessor)
											.setHeader(
													BusinessConstants.BIND_MODE_TYPE,
													constant(BusinessConstants.BIND_NODE_TYPE_RECEIVER))
											.routeId(routeId)
											.setHeader(
													BusinessConstants.BIND_MODE,
													constant(BusinessConstants.BIND_MODE_TXRX))
											.autoStartup(autostartup)
											.to(BusinessConstants.CAMEL_COMPONENT_TYPE
													+ "Rx?concurrentConsumers="
													+ concurrentConsumers);
								}
							}
						}
					}
				}

				final ChoiceDefinition routeDefinition = from(
						BusinessConstants.CAMEL_COMPONENT_TYPE
								+ "Rx?concurrentConsumers="
								+ concurrentConsumers)
						.routeId(
								BusinessConstants.MAIN_ROUTE
										+ BusinessConstants.ROUTE_RX)
						.routePolicy(routePolicyMainRoute)
						.process(requestIdGenerator).process(circleFinder)
						.choice();

				final Map<String, Object> choiceDefinitionMap = choiceDefinitionStorage
						.getchoiceDefinitionMap();
				choiceDefinitionMap.put(ExternalSystemType.SMSC_TYPE.name(),
						routeDefinition);

				for (final CircleCheckerPredicate circleCheckerPredicate : circleCheckerPredicates) {
					routeDefinition
							.when(circleCheckerPredicate)
							.to(BusinessConstants.CAMEL_COMPONENT_TYPE
									+ "Rx"
									+ circleCheckerPredicate.getCircleCode()
									+ "?concurrentConsumers="
									+ PropertyUtils
											.getProperty("route.subroute.concurrentconsumer.rx"));
				}

				for (final CircleCheckerPredicate circleCheckerPredicate : circleCheckerPredicates) {
					from(
							BusinessConstants.CAMEL_COMPONENT_TYPE
									+ "Rx"
									+ circleCheckerPredicate.getCircleCode()
									+ "?concurrentConsumers="
									+ PropertyUtils
											.getProperty("route.subroute.concurrentconsumer.rx"))

							.routePolicy(routePolicyCircleRoute)
							.routeId(
									BusinessConstants.ROUTE_STR
											+ circleCheckerPredicate
													.getCircleCode()
											+ BusinessConstants.ROUTE_RX)
							.process(requestProcessor);
					/**
					 * .to(BusinessConstants.CIRCLE_QUEUE_PREFIX +
					 * circleCheckerPredicate.getCircleCode() +
					 * BusinessConstants.CIRCLE_QUEUE_SUFFIX_INBOUND);
					 * 
					 * from( BusinessConstants.CIRCLE_QUEUE_PREFIX +
					 * circleCheckerPredicate.getCircleCode() +
					 * BusinessConstants.CIRCLE_QUEUE_SUFFIX_INBOUND)
					 * .process(requestProcessor);
					 **/
				}
			}
		};

	}

	/**
	 * To.
	 * 
	 * @param circleSMSCMapForSMS
	 *            the circle smsc map for sms
	 * @param circleSMSCMapForUSSD
	 *            the circle smsc map for ussd
	 * @param bindModeMap
	 *            the bind mode map
	 * @param serverName
	 *            the server name
	 * @param circleCodes
	 *            the circle codes
	 * @return the route builder
	 */
	@SuppressWarnings("unchecked")
	private RouteBuilder to(
			final List<SMPPServerMappingDTO> circleSMSCMapForSMS,
			final List<SMPPServerMappingDTO> circleSMSCMapForUSSD,
			final Map<String, String> bindModeMap, final String serverName,
			final String[] circleCodes) {

		return new RouteBuilder() {

			@Override
			public void configure() throws Exception {

				/* Error Handling through the Dead Letter Channel */

				// errorHandler(deadLetterChannel(BusinessConstants.DEAD_LETTER_QUEUE_NAME));

				/**
				 * Exception handling using Dead Letter Channel and forward to
				 * Dead Letter Queue
				 */
				// onException(RuntimeException.class).handled(true).to(BusinessConstants.DEAD_LETTER_QUEUE_NAME);

				final int maximumRedeiveryAttempt = Integer
						.valueOf(PropertyUtils
								.getProperty("route.maximum.redelivery.attempt"));
				final int maximumRedeliveryInterval = Integer
						.valueOf(PropertyUtils
								.getProperty("route.redelivery.delay"));
				final int registeredDelivery = Integer
						.valueOf(PropertyUtils
								.getProperty("smsc.route.registeredDelivery"));

				/**
				 * Creating routes for exception messages into queue.
				 */
				/*
				 * for (String circleCode : circleCodes) {
				 * from(BusinessConstants.DEAD_LETTER_QUEUE_NAME) .routeId(
				 * BusinessConstants.ROUTE + circleCode +
				 * BusinessConstants.UNDERSCORE +
				 * BusinessConstants.DEAD_LETTER_CHANNEL_ROUTE)
				 * .process(processErrorMessages)
				 * .to(BusinessConstants.CIRCLE_QUEUE_PREFIX + circleCode +
				 * BusinessConstants.CIRCLE_QUEUE_SUFFIX_OUTBOUND); }
				 */
				/**
				 * Filter outgoing messages on basis of bind mode.
				 */
				for (final String serviceType : bindModeMap.keySet()) {
					final String bindMode = bindModeMap.get(serviceType);
					final String concurrentConsumers = PropertyUtils
							.getProperty("SMSC_QUEUE_CONCURRENT_CONSUMERS");
					for (final String circleCode : circleCodes) {
						from(
								SMPPUtil.getSMSOutQueue(circleCode)
										+ "?concurrentConsumers="
										+ concurrentConsumers
										+ "&asyncConsumer=true")
								.onException(
										org.jsmpp.InvalidResponseException.class,
										org.jsmpp.extra.ResponseTimeoutException.class,
										org.jsmpp.extra.NegativeResponseException.class,
										java.net.ConnectException.class,
										java.io.IOException.class,
										java.lang.Exception.class,
										java.net.ConnectException.class,
										java.net.SocketTimeoutException.class,
										org.apache.camel.component.smpp.SmppException.class
										)
								.maximumRedeliveries(maximumRedeiveryAttempt)
								.redeliveryDelay(maximumRedeliveryInterval)
								.onRedelivery(logRetryProcessor)
								.end()
								.choice()
								.when(header(BusinessConstants.BIND_MODE)
										.isEqualTo(
												BusinessConstants.BIND_MODE_TRX))
								.process(responseProcessor)
								// .process(logTransceiverProcessor)
								// .process(logOutgoingIpProcessorTRX)
								.process(outBoundQueueProcessor)
								.process(receiptProcessor)
								.endChoice()

								.when(header(BusinessConstants.BIND_MODE)
										.isEqualTo(
												BusinessConstants.BIND_MODE_TXRX))
								// .otherwise()
								.choice()
								.when(header(BusinessConstants.SERVICE_TYPE)
										.isEqualTo(
												BusinessConstants.SERVICE_TYPE_SMS))
								.log("Got Message in SMS(SMS) Case")
								.to(BusinessConstants.CAMEL_COMPONENT_TYPE
										+ serverName
										+ BusinessConstants.ROUTE_TX_SMS + "?concurrentConsumers="
												+ concurrentConsumers)
								.when(header(BusinessConstants.SERVICE_TYPE)
										.isEqualTo(
												BusinessConstants.SERVICE_TYPE_USSD))
								.log("Got Message in USSD Case")
								.to(BusinessConstants.CAMEL_COMPONENT_TYPE
										+ serverName
										+ BusinessConstants.ROUTE_TX_USSD);

					}
					// from("jms:queue:DEL_SMSQueueOut").process(transceiverProcessor);
					/**
					 * Creating routes for Transmitter processor.
					 */
					if ("TXRX".equals(bindMode)) {

						if (BusinessConstants.SERVICE_TYPE_SMS
								.equals(serviceType)) {
							/**
							 * SMS(WAP) Sub Route Defining routes for End Point
							 * Tx1 to Circles SMSC's , if we find two or more
							 * SMSC's for the Circle then load-balancer will
							 * manages the load in round-robin manner in case of
							 * fail-over otherwise there will no load-balancer
							 * in case of single SMSC.
							 */
							if (circleSMSCMapForSMS != null
									&& !circleSMSCMapForSMS.isEmpty()) {

								final int smscCount = getSMSCCountForUSSDOrSMS(circleSMSCMapForSMS);
								if (smscCount > 1) {
									final LoadBalanceDefinition fromSmsDefinition = from(
											BusinessConstants.CAMEL_COMPONENT_TYPE
													+ serverName
													+ BusinessConstants.ROUTE_TX_SMS)
											.log("Message came to Load balancer.")
											.routeId(
													serverName
															+ BusinessConstants.UNDERSCORE
															+ BusinessConstants.ROUTE_TX_SMS)
											.routeId(
													serverName
															+ BusinessConstants.UNDERSCORE
															+ BusinessConstants.ROUTE_TX_SMS)
											.autoStartup(true)
											.loadBalance()
											.failover(
													1,
													false,
													true,
													org.jsmpp.PDUException.class,
													org.jsmpp.InvalidResponseException.class,
													org.jsmpp.extra.ResponseTimeoutException.class,
													org.jsmpp.extra.NegativeResponseException.class,
													java.net.ConnectException.class,
													java.io.IOException.class);
									final Map<String, Object> loadBalancerMap = loadBalancerStorage
											.getLoadBalancerMap();
									final String lbKey = ExternalSystemType.SMSC_TYPE
											.name()
											+ BusinessConstants.COLON
											+ serverName;
									loadBalancerMap.put(lbKey,
											fromSmsDefinition);
									for (final FDPSMSCConfigDTO fdpsmscConfigDTO : circleSMSCMapForSMS) {
										if (fdpsmscConfigDTO
												.getBindNode()
												.trim()
												.equals(BusinessConstants.BIND_NODE_TYPE_TRANSMITTER)) {

											final List<String> routeIdList = new ArrayList<String>();
											final String routeIdLBVirtualEndpoint = "virtual"
													+ BusinessConstants.UNDERSCORE
													+ fdpsmscConfigDTO
															.getLogicalName()
													+ BusinessConstants.UNDERSCORE
													+ serverName;
											fromSmsDefinition
													.routeId(
															routeIdLBVirtualEndpoint)
													.to(BusinessConstants.CAMEL_COMPONENT_TYPE
															+ serverName
															+ fdpsmscConfigDTO
																	.getLogicalName())
													.end();

											final String smsUrl = getSMSCEndPointsTx(fdpsmscConfigDTO, registeredDelivery);
											final String ipAddress = fdpsmscConfigDTO
													.getIp();
											final String circleCodeIpaddressPort = serverName
													+ BusinessConstants.COLON
													+ ipAddress
													+ BusinessConstants.COLON
													+ String.valueOf(fdpsmscConfigDTO
															.getPort())
													+ BusinessConstants.COLON
													+ fdpsmscConfigDTO
															.getBindSystemId();
											final String routeId = fdpsmscConfigDTO
													.getLogicalName()
													+ BusinessConstants.UNDERSCORE
													+ serverName;
											routeIdList
													.add(routeIdLBVirtualEndpoint);
											routeIdList.add(routeId);
											from(
													BusinessConstants.CAMEL_COMPONENT_TYPE
															+ serverName
															+ fdpsmscConfigDTO
															.getLogicalName()).autoStartup(true)
													.setProperty(
															BusinessConstants.OUTGOING_IP_ADDRESS,
															constant(ipAddress))
													.setProperty(
															BusinessConstants.OUTGOING_CIRCLE_CODE_IP_PORT,
															constant(circleCodeIpaddressPort))
													.routeId(routeId)
													.process(responseProcessor)
													.process(
															logOutgoingIpProcessor)
													.onException(
															org.jsmpp.InvalidResponseException.class,
															org.jsmpp.extra.ResponseTimeoutException.class,
															org.jsmpp.extra.NegativeResponseException.class,
															java.net.ConnectException.class,
															java.io.IOException.class,
															java.lang.Exception.class,
															java.net.ConnectException.class,
															java.net.SocketTimeoutException.class,
															org.apache.camel.component.smpp.SmppException.class)

													.maximumRedeliveries(
															maximumRedeiveryAttempt)
													.redeliveryDelay(
															maximumRedeliveryInterval)
													.onRedelivery(
															logRetryProcessor)
													.end().to(smsUrl);
													//.process(receiptProcessor);
											storeRouteId(fdpsmscConfigDTO,
													serverName, routeIdList);
										}

									}
								} else {
									for (final FDPSMSCConfigDTO fdpsmscConfigDTO : circleSMSCMapForSMS) {
										if (fdpsmscConfigDTO
												.getBindNode()
												.trim()
												.equals(BusinessConstants.BIND_NODE_TYPE_TRANSMITTER)) {
											final String ipAddress = fdpsmscConfigDTO
													.getIp();
											final String circleCodeIpaddressPort = serverName
													+ BusinessConstants.COLON
													+ ipAddress
													+ BusinessConstants.COLON
													+ String.valueOf(fdpsmscConfigDTO
															.getPort())
													+ BusinessConstants.COLON
													+ fdpsmscConfigDTO
															.getBindSystemId();
											final String smsUrl = getSMSCEndPointsTx(fdpsmscConfigDTO, registeredDelivery);
											final String routeId = fdpsmscConfigDTO
													.getLogicalName()
													+ BusinessConstants.UNDERSCORE
													+ serverName;
											final List<String> routeIdList = new ArrayList<String>();
											from(
													BusinessConstants.CAMEL_COMPONENT_TYPE
															+ serverName
															+ BusinessConstants.ROUTE_TX_SMS)
													.autoStartup(true)
													.setProperty(
															BusinessConstants.OUTGOING_IP_ADDRESS,
															constant(ipAddress))
													.setProperty(
															BusinessConstants.OUTGOING_CIRCLE_CODE_IP_PORT,
															constant(circleCodeIpaddressPort))
													.routeId(routeId)
													.process(responseProcessor)
													.process(
															logOutgoingIpProcessor)
													.onException(
															org.jsmpp.InvalidResponseException.class,
															org.jsmpp.extra.ResponseTimeoutException.class,
															org.jsmpp.extra.NegativeResponseException.class,
															java.net.ConnectException.class,
															java.io.IOException.class,
															java.lang.Exception.class,
															java.net.ConnectException.class,
															java.net.SocketTimeoutException.class,
															org.apache.camel.component.smpp.SmppException.class)
													.maximumRedeliveries(
															maximumRedeiveryAttempt)
													.redeliveryDelay(
															maximumRedeliveryInterval)
													.onRedelivery(
															logRetryProcessor)
													.end().to(smsUrl);
													//.process(receiptProcessor);
											routeIdList.add(routeId);
											storeRouteId(fdpsmscConfigDTO,
													serverName, routeIdList);
										}
									}
								}
							}

						}
					}
				}
			}
		};

	}

	/**
	 * Returns count for USSD and SMS SMSCs in the each Circle. Gets the sMSC
	 * count for USSD and SMS.
	 * 
	 * @param circleCode
	 *            the circle code
	 * @param circleSMSCMap
	 *            the circle smsc map
	 * @param serviceType
	 *            the service type
	 * @return the sMSC count for USSD and SMS.
	 */
	private int getSMSCCountForUSSDOrSMS(
			final List<SMPPServerMappingDTO> smppServerMappingDTOList) {
		int count = 0;
		if (smppServerMappingDTOList != null) {
			for (final FDPSMSCConfigDTO fdpsmscConfigDTO : smppServerMappingDTOList) {
				if (fdpsmscConfigDTO.getBindNode().contains(
						BusinessConstants.BIND_NODE_TYPE_TRANSMITTER)
						&& BusinessConstants.SERVICE_TYPE_SMS
								.equals(fdpsmscConfigDTO.getServiceType())) {
					count++;
				}
			}
		}
		return count;
	}

	/**
	 * Returns List of SMS SMSCs Urls Gets the SMSC end points Tx.
	 * 
	 * @param fdpsmscConfigDTO
	 *            the fdpsmsc config dto
	 * @return the sMSC end points Tx.
	 */
	private String getSMSCEndPointsTx(final FDPSMSCConfigDTO fdpsmscConfigDTO, Integer registeredDelivery) {

		final String smscUrl = BusinessConstants.SMPP_COMPONENT
				+ fdpsmscConfigDTO.getBindSystemId().trim()
				+ BusinessConstants.AT_THE_RATE
				+ fdpsmscConfigDTO.getIp()
				+ BusinessConstants.COLON
				+ fdpsmscConfigDTO.getPort()
				+ BusinessConstants.QUERY_STRING_SEPARATOR
				+ BusinessConstants.PASSWORD
				+ BusinessConstants.EQUALS
				+ fdpsmscConfigDTO.getBindSystemPassword()
				+ BusinessConstants.AMPERSAND_PARAMETER_APPENDER
				+ BusinessConstants.ENQUIRE_LINK_TIMER
				+ BusinessConstants.EQUALS
				+ fdpsmscConfigDTO.getLinkCheckPeriod()
				+ BusinessConstants.AMPERSAND_PARAMETER_APPENDER
				+ BusinessConstants.TRANSACTION_TIMER
				+ BusinessConstants.EQUALS
				+ fdpsmscConfigDTO.getResponseTimeout()
				+ BusinessConstants.AMPERSAND_PARAMETER_APPENDER
				+ BusinessConstants.SYSTEM_TYPE
				+ BusinessConstants.EQUALS
				+ "consumer"// smscDetail.getBindNode()
				+ BusinessConstants.AMPERSAND_PARAMETER_APPENDER
				+ BusinessConstants.SERVICE_TYPE + BusinessConstants.EQUALS
				+ fdpsmscConfigDTO.getServiceType()
				+ BusinessConstants.AMPERSAND_PARAMETER_APPENDER
				+ BusinessConstants.SOURCE_ADDRESS_TON
				+ BusinessConstants.EQUALS + fdpsmscConfigDTO.getBindTON()
				+ BusinessConstants.AMPERSAND_PARAMETER_APPENDER
				+ BusinessConstants.SOURCE_ADD_NPI + BusinessConstants.EQUALS
				+ fdpsmscConfigDTO.getBindNPI() + "&bindType=2"
				+ BusinessConstants.AMPERSAND_PARAMETER_APPENDER
				+ BusinessConstants.REGISTERED_DELIVERY
				+ BusinessConstants.EQUALS + registeredDelivery;
		LOGGER.debug("SMSC Url : producer :" + smscUrl);
		return smscUrl;
	}

	/**
	 * Gets the SMSC end points Rx(consumer).
	 * 
	 * @param circleSmscMap
	 *            the circle smsc map
	 * @param serverName
	 *            the server name
	 * @return the sMSC end points Rx.
	 */
	private String getSMSCEndPointsRx(final FDPSMSCConfigDTO fdpsmscConfigDTO,
			final String serverName) {

		String smscUrl = null;
		if (fdpsmscConfigDTO.getBindNode().trim()
				.equals(BusinessConstants.BIND_NODE_TYPE_RECEIVER)) {

			smscUrl = BusinessConstants.SMPP_COMPONENT
					+ fdpsmscConfigDTO.getBindSystemId().trim()
					+ BusinessConstants.AT_THE_RATE
					+ fdpsmscConfigDTO.getIp()
					+ BusinessConstants.COLON
					+ fdpsmscConfigDTO.getPort()
					+ BusinessConstants.QUERY_STRING_SEPARATOR
					+ BusinessConstants.PASSWORD
					+ BusinessConstants.EQUALS
					+ fdpsmscConfigDTO.getBindSystemPassword()
					+ BusinessConstants.AMPERSAND_PARAMETER_APPENDER
					+ BusinessConstants.ENQUIRE_LINK_TIMER
					+ BusinessConstants.EQUALS
					+ fdpsmscConfigDTO.getLinkCheckPeriod()
					+ BusinessConstants.AMPERSAND_PARAMETER_APPENDER
					+ BusinessConstants.TRANSACTION_TIMER
					+ BusinessConstants.EQUALS
					+ fdpsmscConfigDTO.getResponseTimeout()
					+ BusinessConstants.AMPERSAND_PARAMETER_APPENDER
					+ BusinessConstants.SYSTEM_TYPE
					+ BusinessConstants.EQUALS
					+ "consumer"// smscDetail.getBindNode()
					+ BusinessConstants.AMPERSAND_PARAMETER_APPENDER
					+ BusinessConstants.SERVICE_TYPE + BusinessConstants.EQUALS
					+ fdpsmscConfigDTO.getServiceType()
					+ BusinessConstants.AMPERSAND_PARAMETER_APPENDER
					+ BusinessConstants.SOURCE_ADDRESS_TON
					+ BusinessConstants.EQUALS + fdpsmscConfigDTO.getBindTON()
					+ BusinessConstants.AMPERSAND_PARAMETER_APPENDER
					+ BusinessConstants.SOURCE_ADD_NPI
					+ BusinessConstants.EQUALS + fdpsmscConfigDTO.getBindNPI()
					+ "&bindType=1";
			final List<String> routeIdList = new ArrayList<String>();
			routeIdList.add(fdpsmscConfigDTO.getLogicalName()
					+ BusinessConstants.UNDERSCORE + serverName);
			storeRouteId(fdpsmscConfigDTO, serverName, routeIdList);
			LOGGER.debug("SMSC Url : consumer :" + smscUrl);
		}
		return smscUrl;

	}

	private void storeRouteId(final FDPSMSCConfigDTO fdpsmscConfigDTO,
			final String serverName, final List<String> routeIdList) {
		final FDPAppBag appBag = new FDPAppBag();
		appBag.setSubStore(AppCacheSubStore.SMS_USSD_ROUTE_DETAIL);
		final String key = RouteUtil.prepareExternalSystemAppBagKey(serverName,
				fdpsmscConfigDTO.getIp(), fdpsmscConfigDTO.getPort(),
				fdpsmscConfigDTO.getBindSystemId(),
				fdpsmscConfigDTO.getBindNode());
		//System.out.println("Key:" + key);
		appBag.setKey(key);
		FDPSMSCConfigDTO configDTO = (FDPSMSCConfigDTO) applicationConfigCache
				.getValue(appBag);
		if (configDTO == null) {
			configDTO = fdpsmscConfigDTO;
		}
		configDTO.setRouteId(routeIdList);
		//System.out.println("Key:" + key + "Value " + configDTO.toString());
		applicationConfigCache.putValue(appBag, configDTO);
	}

	/**
	 * Gets the SMSC end points TRx(Transceiver).
	 * 
	 * @param circleSmscList
	 *            the circle smsc map
	 * @param serverName
	 *            the server name
	 * @return the sMSC end points Rx.
	 */
	private String getSMSCEndPointsTRx(final FDPSMSCConfigDTO fdpsmscConfigDTO,
			final String serverName) {

		String smscUrl = null;
		if (fdpsmscConfigDTO.getBindNode().trim()
				.equals(BusinessConstants.BIND_NODE_TYPE_TRANSCEIVER)) {
			smscUrl = BusinessConstants.SMPP_COMPONENT
					+ fdpsmscConfigDTO.getBindSystemId().trim()
					+ BusinessConstants.AT_THE_RATE
					+ fdpsmscConfigDTO.getIp()
					+ BusinessConstants.COLON
					+ fdpsmscConfigDTO.getPort()
					+ BusinessConstants.QUERY_STRING_SEPARATOR
					+ BusinessConstants.PASSWORD
					+ BusinessConstants.EQUALS
					+ fdpsmscConfigDTO.getBindSystemPassword()
					+ BusinessConstants.AMPERSAND_PARAMETER_APPENDER
					+ BusinessConstants.ENQUIRE_LINK_TIMER
					+ BusinessConstants.EQUALS
					+ fdpsmscConfigDTO.getLinkCheckPeriod()
					+ BusinessConstants.AMPERSAND_PARAMETER_APPENDER
					+ BusinessConstants.TRANSACTION_TIMER
					+ BusinessConstants.EQUALS
					+ fdpsmscConfigDTO.getResponseTimeout()
					+ BusinessConstants.AMPERSAND_PARAMETER_APPENDER
					+ BusinessConstants.SYSTEM_TYPE
					+ BusinessConstants.EQUALS
					+ "consumer"// smscDetail.getBindNode()
					+ BusinessConstants.AMPERSAND_PARAMETER_APPENDER
					+ BusinessConstants.SERVICE_TYPE + BusinessConstants.EQUALS
					+ fdpsmscConfigDTO.getServiceType()
					+ BusinessConstants.AMPERSAND_PARAMETER_APPENDER
					+ BusinessConstants.SOURCE_ADDRESS_TON
					+ BusinessConstants.EQUALS + fdpsmscConfigDTO.getBindTON()
					+ BusinessConstants.AMPERSAND_PARAMETER_APPENDER
					+ BusinessConstants.SOURCE_ADD_NPI
					+ BusinessConstants.EQUALS + fdpsmscConfigDTO.getBindNPI()
					+ "&bindType=9";
			final List<String> routeIdList = new ArrayList<String>();
			routeIdList.add(fdpsmscConfigDTO.getLogicalName()
					+ BusinessConstants.UNDERSCORE + serverName);
			storeRouteId(fdpsmscConfigDTO, serverName, routeIdList);
			LOGGER.debug("SMSC Url : Transciver :" + smscUrl);
		}
		return smscUrl;
	}

	/**
	 * This method returns all available circles. Gets the all circle codes.
	 * 
	 * @return the all circle codes
	 */
	private List<String> getAllCircleCodes() {
		return fdpCircleDAO.getAllCircleCodes();
	}

	/**
	 * This method returns all SMSCs details of circles by circleCode passing as
	 * an input parameter. Gets the SMSC by circle code.
	 * 
	 * @param circleCodesList
	 *            the circle codes list
	 * @return the sMSC by circle code
	 */
	// private List<FDPSMSCConfigDTO> getSMSCByCircleCode(final String
	// circleCode,
	// final String serviceType) {
	// return fdpsmscConfigDAO.getSMSCByCircleCode(circleCode, serviceType);
	// }

	/**
	 * This method is used to return circleCode array from circleCodes list as
	 * an input parameter Convert circle code list to array.
	 * 
	 * @param circleCodesList
	 *            the circle codes list
	 * @return the string[]
	 */
	private String[] convertCircleCodeListToArray(
			final List<String> circleCodesList) {

		final String[] circleCodes = new String[circleCodesList.size()];
		for (int i = 0; i < circleCodesList.size(); i++) {
			circleCodes[i] = circleCodesList.get(i);

		}
		return circleCodes;
	}

	/**
	 * Gets the SMSC details for SMS.
	 * 
	 * @param serverName
	 *            the server name
	 * @return the sMSC details for SMS
	 */
	private List<SMPPServerMappingDTO> getSMSCDetailsForSMS(
			final String serverName) {
		return serverMappingService.getSMSCServerMappings(
				ExternalSystemType.SMSC_TYPE, serverName);
	}

	/**
	 * Gets the SMSC details for ussd.
	 * 
	 * @param serverName
	 *            the server name
	 * @return the sMSC details for ussd
	 */
	private List<SMPPServerMappingDTO> getSMSCDetailsForUSSD(
			final String serverName) {
		return serverMappingService.getSMSCServerMappings(
				ExternalSystemType.USSD_TYPE, serverName);
	}
	
	/**
	 * This method will check whether the CIS site is active or not by making DNS query of GTM/LTM 
	 * @return
	 */
	private boolean isSiteActive() {
		final String trafficManagerFQDN = PropertyUtils.getProperty(BusinessConstants.ACTIVE_TRAFFIC_MANAGER_FQDN);
		final String trafficManagerIp = PropertyUtils.getProperty(BusinessConstants.ACTIVE_TRAFFIC_MANAGER_IP);
		InetAddress inetAddress = null;
		if (trafficManagerFQDN != null) {
			try {
				inetAddress = InetAddress.getByName(trafficManagerFQDN);
				if (inetAddress.getHostAddress().equals(trafficManagerIp)) {
					return true;
				}
			} catch (UnknownHostException e) {
				LOGGER.info("Host IP address is not defined");
				e.printStackTrace();
			}
		}
		
		return false;
	}
	
	/**
	 * Create Tx and RX route
	 * @param context
	 * @param circleSMSCMapForSMS
	 * @param serverName
	 * @param bindModeMap
	 * @param circleCodes
	 * @param circleCheckerPredicates
	 */
	private void createTxRxRoute(CdiCamelContext context, List<SMPPServerMappingDTO> circleSMSCMapForSMS, String serverName, 
			Map<String, String> bindModeMap, String[] circleCodes, List<CircleCheckerPredicate> circleCheckerPredicates) {
		
		try {
			context.addComponent(RoutingConstant.JMS_COMPONENT_NAME, JmsComponent.jmsComponent(connectionFactory));
			context.addRoutes(
					to(circleSMSCMapForSMS, /* circleSMSCMapForUSSD */null, bindModeMap, serverName, circleCodes));
			context.addRoutes(createUSSDRoutes(circleCodes));
			context.addRoutes(createUSSDOutLinkRoutes(circleCodes));
			context.addRoutes(from(circleSMSCMapForSMS, /* circleSMSCMapForUSSD */
					null, bindModeMap, serverName, circleCheckerPredicates));
		} catch (Exception e) {
			LOGGER.error("SMS ROUTE Connection error",e);
		}
	}
}
