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
import org.apache.camel.RoutesBuilder;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.cdi.CdiCamelContext;
import org.apache.camel.model.ChoiceDefinition;
import org.apache.camel.model.LoadBalanceDefinition;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ericsson.fdp.business.constants.BusinessConstants;
import com.ericsson.fdp.business.enums.Command;
import com.ericsson.fdp.business.route.controller.ChoiceDefinitionStorage;
import com.ericsson.fdp.business.route.processor.HttpHeaderProcessor;
import com.ericsson.fdp.business.route.processor.LogOutgoingIpProcessor;
import com.ericsson.fdp.business.route.processor.MobileMoneyContextRedirect;
import com.ericsson.fdp.business.route.processor.MobileMoneyDebitRequestProcessor;
import com.ericsson.fdp.business.route.processor.MomoExpiredTransactionProcessor;
import com.ericsson.fdp.common.constants.FDPConstant;
import com.ericsson.fdp.common.constants.QueueConstant;
import com.ericsson.fdp.common.enums.BusinessModuleType;
import com.ericsson.fdp.common.enums.ExternalSystem;
import com.ericsson.fdp.core.dsm.framework.MomoTransactionExpiredRequest;
import com.ericsson.fdp.core.dsm.framework.service.impl.FDPCircleCacheProducer;
import com.ericsson.fdp.core.exception.ExecutionFailedException;
import com.ericsson.fdp.core.logging.FDPLogger;
import com.ericsson.fdp.core.logging.FDPLoggerFactory;
import com.ericsson.fdp.core.request.FDPSMPPRequest;
import com.ericsson.fdp.core.request.impl.FDPRequestImpl;
import com.ericsson.fdp.core.serviceprovisioning.bean.FDPRequestBag;
import com.ericsson.fdp.core.utils.ApplicationConfigUtil;
import com.ericsson.fdp.dao.dto.FDPCacheRequest;
import com.ericsson.fdp.dao.dto.FDPMobileMoneyConfigDTO;
import com.ericsson.fdp.dao.fdpadmin.FDPCircleDAO;
import com.ericsson.fdp.dao.fdpadmin.FDPMobileMoneyConfigDAO;
import com.ericsson.fdp.route.cdi.context.provider.CdiCamelContextProvider;
import com.ericsson.fdp.route.processors.LogRetryProcessor;
import com.ericsson.fdp.route.processors.RouteCompletionProcessor;
import com.ericsson.fdp.dao.dto.UpdateRequestDTO;

@Singleton(name = "FDPMobileMoneyRoute")
public class FDPMobileMoneyRoute {

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
	private MobileMoneyContextRedirect mobilemoneyContext;

	/** The Constant LOGGER. */
	private static final Logger LOGGER = LoggerFactory
			.getLogger(FDPMobileMoneyRoute.class);
	
	/**
	 * The headerProcessor is used to process the headers at run time mapped by
	 * ip and port.
	 */
	@Inject
	private HttpHeaderProcessor headerProcessor;
	
	/* Dummy Debit response to set incase of read timeout */
	private String dummyDebitResponse = "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?>"
			+ "<ns0:debitresponse xmlns:ns0=\"http://www.ericsson.com/em/emm/financial/v1_0\">"
            + "<transactionid>${transactionId}</transactionid>"
            + "<status>PENDING</status>"
            + "</ns0:debitresponse>";

	/**
	 * Creates the routes.
	 */
	public void createRoutes() throws Exception {

		context = cdiCamelContextProvider.getContext();
		context.getProperties().put("http.keepAlive", "false");
		context.addRoutes(createMobileMoneyHttpsRoutes());
		context.addRoutes(createMomoExpiredTransactionPushRoute());
		context.addRoutes(createMomoExpiredTransProcessingRoute());
		context.addRoutes(createMMDebitRequestProcessingRoute());
	}
	
	/**
	 * This function will create a route which will read data from queue and process it after 
	 * timeout happens for Mobile Money Debit pending request 
	 * @return
	 */
	private RouteBuilder createMomoExpiredTransactionPushRoute() {
		return new RouteBuilder() {
			@Override
			public void configure() throws Exception {
				from(QueueConstant.JMS_QUEUE + QueueConstant.MM_DEBIT_PENDING_QUEUE + "?concurrentConsumers=10")
				.process(new Processor() {
					@Override
					public void process(Exchange exchange) throws Exception {
						FDPRequestImpl fdpCacheable = null;
						FDPRequestBag key = null;
						try {
							LOGGER.debug("Inside createMomoExpiredTransactionPushRoute() method");
							if(null != exchange.getIn().getBody())  {
								UpdateRequestDTO  updateRequestDTO = (UpdateRequestDTO)exchange.getIn().getBody();
								LOGGER.debug("MM Debit TransactionId: " + updateRequestDTO.getTransactionId());
								key = new FDPRequestBag(updateRequestDTO.getTransactionId());
								fdpCacheable = (FDPRequestImpl) ApplicationConfigUtil.getRequestCacheForMMWeb().getValue(key);
								LOGGER.debug("Request Id of MM Debit Pending Request: " +fdpCacheable.getRequestId());
								if (fdpCacheable != null) {
									MomoTransactionExpiredRequest momoRequest = new MomoTransactionExpiredRequest(key.getRequestId(), fdpCacheable);
									pushToMomoExpiredTransQueue(momoRequest);
									exchange.getIn().setBody(momoRequest);
								}
							}
							
						} finally {
							if (fdpCacheable != null) {
								ApplicationConfigUtil.getRequestCacheForMMWeb().removeKey(key);
							}
						}
						
					}
				});
			}
			
		};
	}
	
	/**
	 * This function pushes the expired transactions to queue for processing
	 * @param fdpCacheableObject
	 */
	public static void pushToMomoExpiredTransQueue(final FDPCacheRequest fdpCacheableObject) {
		try{
			final FDPCircleCacheProducer circleCacheProducer = ApplicationConfigUtil.getFDPCircleCacheProducer();
			circleCacheProducer.pushToQueue(fdpCacheableObject, QueueConstant.JMS_QUEUE + QueueConstant.MOMO_EXPIRED_TRANSACTION_QUEUE);
		} catch (final ExecutionFailedException e) {
			e.printStackTrace();
		}
	}

	/**
	 * This function creates the route that processes expired mobile money transactions.
	 * @return
	 */
	private RoutesBuilder createMomoExpiredTransProcessingRoute() {
		return new RouteBuilder() {
			@Override
			public void configure() throws Exception {
				from(QueueConstant.JMS_QUEUE + QueueConstant.MOMO_EXPIRED_TRANSACTION_QUEUE + "?concurrentConsumers=10")
				.process(new MomoExpiredTransactionProcessor());
			}
		};
	}
	
	/**
	 * This function creates the route that processes mobile money debit request.
	 * @return
	 */
	private RoutesBuilder createMMDebitRequestProcessingRoute() {
		return new RouteBuilder() {
			@Override
			public void configure() throws Exception {
				from(QueueConstant.JMS_QUEUE + QueueConstant.MM_DEBIT_QUEUE + "?concurrentConsumers=10")
				.process(new MobileMoneyDebitRequestProcessor());
			}
		};
	}
	
	private RouteBuilder createMobileMoneyHttpsRoutes() {

		return new RouteBuilder() {
			@SuppressWarnings("unchecked")
			@Override
			public void configure() throws Exception {
				final List<String> circleCodeList = getAllCircleCodes();
				final boolean autostartup = true;
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
					// Parsing FDPAIRConfigDTO List into Endpoint store into
					// Array
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
									from(
									BusinessConstants.DIRECT_COMPONENT_MOBILEMONEY_ENDPOINT
											+ "_" + circlecode)
									.setExchangePattern(ExchangePattern.InOut)
									.autoStartup(autostartup)
									.routeId(
											ExternalSystem.MM.name()
													+ BusinessConstants.UNDERSCORE
													+ circlecode).to(endpoints);
						}
					}

                    for (int j = 0; fdpMobileMoneyConfigDTOList != null && j < fdpMobileMoneyConfigDTOListSize; j++) {
                        final FDPMobileMoneyConfigDTO fdpMobileMoneyConfigDTO = fdpMobileMoneyConfigDTOList.get(j);
                        final List<String> routeIdList = new ArrayList<String>();

                        final String circleCodeEndpoint = circlecode + fdpMobileMoneyConfigDTO.getExternalSystemId()
                                + BusinessConstants.UNDERSCORE + fdpMobileMoneyConfigDTO.getLogicalName();
                        final String ipAddress = fdpMobileMoneyConfigDTO.getIpAddress().getValue();
                        final String circleCodeIpaddressPort = circlecode + BusinessConstants.COLON + ipAddress + BusinessConstants.COLON
                                + String.valueOf(fdpMobileMoneyConfigDTO.getPort());

                        final String routeId2 = BusinessConstants.SUB_ROUTE_UNDERSCORE + circleCodeEndpoint + BusinessConstants.UNDERSCORE
                                + ExternalSystem.MM.name();

                        routeIdList.add(routeId2);
                        from(BusinessConstants.DIRECT_COMPONENT_MOBILEMONEY_ENDPOINT + circleCodeEndpoint)
                        .autoStartup(autostartup)
                                .routeId(routeId2)
                                .onCompletion().onCompleteOnly().process(completionProcessor).end()
                                .setProperty(BusinessConstants.OUTGOING_IP_ADDRESS, constant(ipAddress))
                                .setProperty(BusinessConstants.OUTGOING_CIRCLE_CODE_IP_PORT, constant(circleCodeIpaddressPort))
                                .process(logOutgoingIpProcessor)
                                .process(headerProcessor)
                                .onException( java.net.SocketTimeoutException.class).process(new Processor() {
                                	@Override
                					public void process(Exchange exchange) throws Exception {
                                		if (((String)exchange.getProperty(BusinessConstants.MM_COMMAND_NAME))
                                				.equalsIgnoreCase(Command.MM_DEBIT.getCommandDisplayName())) {
                                			Logger logger = FDPLoggerFactory.getRequestLogger(
                                					(String)exchange.getProperty(BusinessConstants.CIRCLE_NAME), BusinessModuleType.MM.name());
                                			FDPLogger.info(logger, getClass(), "process()",
                                					new StringBuilder(BusinessConstants.REQUEST_ID).append(FDPConstant.LOGGER_KEY_VALUE_DELIMITER)
                                							.append(exchange.getProperty(BusinessConstants.REQUEST_ID)).append(FDPConstant.LOGGER_DELIMITER)
                                							.append("Read Timeout received for Mobile Money MM DEBIT command").toString());
                                			exchange.getOut().setBody(dummyDebitResponse.replace("${transactionId}", 
                                    				(String)exchange.getProperty(BusinessConstants.REQUEST_ID)));
                                    		exchange.getOut().setHeader(BusinessConstants.HTTP_RESPONSE_CODE, "200");
                        				}           
                                	}
                                }).end()
                                .onException(java.lang.Exception.class, java.net.ConnectException.class,
                                        org.apache.camel.component.http.HttpOperationFailedException.class)
                                        .handled(false)
                                .maximumRedeliveries(fdpMobileMoneyConfigDTO.getNumberOfRetry())
                                .redeliveryDelay(fdpMobileMoneyConfigDTO.getRetryInterval()).onRedelivery(logRetryProcessor)
                                .to(BusinessConstants.COMPONENT_EXCEPTION_ENDPOINT).end()
                                .process(mobilemoneyContext).to(getMobileMoneyEndpointURLForCircle(fdpMobileMoneyConfigDTO));

                    }

				}

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
	 */
	private String getMobileMoneyEndpointURLForCircle(
			final FDPMobileMoneyConfigDTO fdpmobilemoneyConfigDTO) {
		final String mobilemoneyUrl = BusinessConstants.HTTP_COMPONENT_TYPE
				+ fdpmobilemoneyConfigDTO.getIpAddress().getValue()
				+ BusinessConstants.COLON + fdpmobilemoneyConfigDTO.getPort()+ BusinessConstants.QUERY_STRING_SEPARATOR
				+ BusinessConstants.HTTP_CLIENT_SO_TIMEOUT + BusinessConstants.EQUALS + fdpmobilemoneyConfigDTO.getResponseTimeout()
				+ BusinessConstants.AMPERSAND_PARAMETER_APPENDER+"throwExceptionOnFailure=false";
		LOGGER.debug("Mobile money  Url:" + mobilemoneyUrl);
		return mobilemoneyUrl;
	}
	
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
