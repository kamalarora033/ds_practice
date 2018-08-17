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
import com.ericsson.fdp.dao.dto.FDPAIRConfigDTO;
import com.ericsson.fdp.dao.fdpadmin.FDPAIRConfigDAO;
import com.ericsson.fdp.dao.fdpadmin.FDPCircleDAO;
import com.ericsson.fdp.route.cdi.context.provider.CdiCamelContextProvider;
import com.ericsson.fdp.route.processors.LogRetryProcessor;
import com.ericsson.fdp.route.processors.RouteCompletionProcessor;
import com.ericsson.fdp.route.processors.RouteFailureProcessor;

/**
 * The Class AIRHttpRoute used to AIR routes over the Http Protocol and uses camel http component.
 * 
 * @author Ericsson
 */
@Singleton(name = "AIRHttpRoute")
public class AIRHttpRoute {

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
    private FDPAIRConfigDAO fdpAirConfigDAO;

    /** The Constant LOGGER. */
    private static final Logger LOGGER = LoggerFactory.getLogger(AIRHttpRoute.class);

    /** The logOutgoingIpProcessor is used to log out ip addresses for exchange */
    @Inject
    private LogOutgoingIpProcessor logOutgoingIpProcessor;

    /**
     * The headerProcessor is used to process the headers at run time mapped by ip and port.
     */
    @Inject
    private HttpHeaderProcessor headerProcessor;

    @Inject
    private LogRetryProcessor logRetryProcessor;

    @Inject
    private RouteCompletionProcessor completionProcessor;
    
    @Inject
    private RouteFailureProcessor failureProcessor ;

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
        context.addRoutes(createRouteFailureRoute());
        context.addRoutes(createAirHttpRoutes());
    }


    /**
     * This method is responsible for creating the routes.
     * 
     * @return the route builder
     */
    private RouteBuilder createAirHttpRoutes() {
        return new RouteBuilder() {
            @SuppressWarnings("unchecked")
            @Override
            public void configure() throws Exception {
                final HttpComponent http = context.getComponent(BusinessConstants.HTTP_COMPONENT, HttpComponent.class);

                http.setHttpConnectionManager(getHttpConnectionManager());
                final boolean autostartup = true;
                /**
                 * Getting maximumRedeiveryAttempt and maximumRedeliveryInterval from Property file.
                 */

                final List<String> circleCodeList = getAllCircleCodes();

                final ChoiceDefinition routeDefinitionAIR = from(BusinessConstants.HTTP_COMPONENT_AIR_ENDPOINT)
                        .routeId(BusinessConstants.MAIN_ROUTE_UNDERSCORE + ExternalSystem.AIR.name()).autoStartup(autostartup).choice();
                final Map<String, Object> choiceDefinitionMap = choiceDefinitionStorage.getchoiceDefinitionMap();
                choiceDefinitionMap.put(ExternalSystem.AIR.name(), routeDefinitionAIR);
                for (int i = 0; circleCodeList != null && i < circleCodeList.size(); i++) {

                    final String routeId = BusinessConstants.SUB_ROUTE_UNDERSCORE + circleCodeList.get(i) + BusinessConstants.UNDERSCORE
                            + ExternalSystem.AIR.name();
                    routeDefinitionAIR.when(header(BusinessConstants.CIRCLE_CODE).isEqualTo(circleCodeList.get(i))).routeId(routeId)
                            .to(BusinessConstants.HTTP_COMPONENT_AIR_ENDPOINT + "_" + circleCodeList.get(i)).endChoice();

                    final List<FDPAIRConfigDTO> fdpAIRConfigDTOList = getEndpointURLsForAIRByCircleCode(circleCodeList.get(i));

                    // Parsing FDPAIRConfigDTO List into Endpoint store into
                    // Array
                    final String[] endpoints = getCircleEndPointsForLB(fdpAIRConfigDTOList, circleCodeList.get(i));

                    final int fdpAIRConfigDTOListSize = fdpAIRConfigDTOList.size();
                    /**
                     * Sub Route Defining routes for End Point to Circles AIR Servers , if we find two or more AIR URLs for the Circle then
                     * load-balancer will manages the load in round-robin manner in case of fail-over otherwise there will no load-balancer
                     * in case of single AIR Servers.
                     */
                    LoadBalanceDefinition loadBalanceDefinition = null;
                    if (fdpAIRConfigDTOList != null && fdpAIRConfigDTOListSize > 1) {

                        loadBalanceDefinition = from(
                                BusinessConstants.HTTP_COMPONENT_AIR_ENDPOINT + BusinessConstants.UNDERSCORE + circleCodeList.get(i))
                                .routeId(ExternalSystem.AIR.name() + BusinessConstants.UNDERSCORE + circleCodeList.get(i))
                                .setExchangePattern(ExchangePattern.InOut).loadBalance()
                                .failover(fdpAIRConfigDTOListSize - 1, false, true, java.lang.Exception.class);
                        for (final String endpoint : endpoints) {

                            loadBalanceDefinition.routeId(endpoint).to(endpoint);
                        }
                    } else {
                        if (fdpAIRConfigDTOList != null && fdpAIRConfigDTOListSize == 1) {
                            from(BusinessConstants.HTTP_COMPONENT_AIR_ENDPOINT + "_" + circleCodeList.get(i))
                                    .setExchangePattern(ExchangePattern.InOut)
                                    .routeId(ExternalSystem.AIR.name() + BusinessConstants.UNDERSCORE + circleCodeList.get(i))
                                    .to(endpoints);
                        }
                    }
                    final String circleCode = circleCodeList.get(i);
                    for (int j = 0; fdpAIRConfigDTOList != null && j < fdpAIRConfigDTOListSize; j++) {

                        final FDPAIRConfigDTO fdpAIRConfigDTO = fdpAIRConfigDTOList.get(j);
                        final List<String> routeIdList = new ArrayList<String>();
                        /**
                         * Manages the re-delivery attempt and re-delivery delay.
                         */
                        final String circleCodeEndpoint = circleCode + fdpAIRConfigDTO.getExternalSystemId() + BusinessConstants.UNDERSCORE
                                + fdpAIRConfigDTO.getLogicalName();
                        final String ipAddress = fdpAIRConfigDTO.getIpAddress().getValue();
                        final String circleCodeIpaddressPort = circleCode + BusinessConstants.COLON + ipAddress + BusinessConstants.COLON
                                + String.valueOf(fdpAIRConfigDTO.getPort());
                        final String routeId2 = BusinessConstants.SUB_ROUTE_UNDERSCORE + circleCodeEndpoint + BusinessConstants.UNDERSCORE
                                + ExternalSystem.AIR.name();

                        routeIdList.add(routeId2);
                        from(BusinessConstants.HTTP_COMPONENT_AIR_ENDPOINT + circleCodeEndpoint)
                                .autoStartup(autostartup)
                                .routeId(routeId2)
                                .onCompletion().onCompleteOnly().process(completionProcessor).end()
                                .setProperty(BusinessConstants.OUTGOING_IP_ADDRESS, constant(ipAddress))
                                .setProperty(BusinessConstants.OUTGOING_CIRCLE_CODE_IP_PORT, constant(circleCodeIpaddressPort))
                                .process(logOutgoingIpProcessor)
                                .process(headerProcessor)
                                .onException(java.lang.Exception.class, java.net.ConnectException.class,
                                        java.net.SocketTimeoutException.class,
                                        org.apache.camel.component.http.HttpOperationFailedException.class).handled(false)
                                .maximumRedeliveries(fdpAIRConfigDTO.getNumberOfRetry())
                                .redeliveryDelay(fdpAIRConfigDTO.getRetryInterval()).onRedelivery(logRetryProcessor)
                                .to(BusinessConstants.COMPONENT_EXCEPTION_ENDPOINT).end()
                                .to(getAIREndpointURLForCircle(fdpAIRConfigDTO));
                    }
                }
            }


            private MultiThreadedHttpConnectionManager getHttpConnectionManager() {
                final int maxTotalConnection = readIntPropertyValue("http.maxtotalconnections", "200");
                final int defaultMaxConnectionPerHost = readIntPropertyValue("http.default.max.total.connections.per.host", "5");
                final int maxConnectionPerHost = readIntPropertyValue("http.max.connection.per.host", "20");
                final int httpSoTimeOut = readIntPropertyValue("http.socket.timeout", "5000");
                final int connectionIdleCloseTime = readIntPropertyValue("http.close.idle.time", "6000");
                final int connectionTimeout = readIntPropertyValue("http.connection.timeout", "5000");

                final HttpConnectionManagerParams httpConnectionManagerParams = new HttpConnectionManagerParams();
                httpConnectionManagerParams.setMaxTotalConnections(maxTotalConnection);
                httpConnectionManagerParams.setDefaultMaxConnectionsPerHost(defaultMaxConnectionPerHost);
                httpConnectionManagerParams.setMaxConnectionsPerHost(HostConfiguration.ANY_HOST_CONFIGURATION, maxConnectionPerHost);
                httpConnectionManagerParams.setConnectionTimeout(connectionTimeout);
                httpConnectionManagerParams.setSoTimeout(httpSoTimeOut);

                final MultiThreadedHttpConnectionManager httpConnectionManager = new MultiThreadedHttpConnectionManager();
                httpConnectionManager.closeIdleConnections(connectionIdleCloseTime);
                httpConnectionManager.setParams(httpConnectionManagerParams);
                return httpConnectionManager;
            }


            private Integer readIntPropertyValue(final String propertyName, final String defaultValue) {
                String propertyValue = PropertyUtils.getProperty(propertyName);
                propertyValue = (null == propertyValue || "".equals(propertyValue)) ? defaultValue : propertyValue;
                return Integer.valueOf(propertyValue);
            }
        };
    }
    

    private RouteBuilder createRouteFailureRoute() {
        return new RouteBuilder() {
            @Override
            public void configure() throws Exception {
                from(BusinessConstants.COMPONENT_EXCEPTION_ENDPOINT).process(failureProcessor).end();
            }
        };
    }
    


    /**
     * Call client.
     * 
     * @throws Exception the exception
     */
    public void callClient() throws Exception {

        final String httpRequest = "<?xml version=\"1.0\"?>\n<methodCall>\n<methodName>"
                + "GetAccountDetails</methodName>\n<params>\n<param>\n<value>\n<struct>"
                + "\n<member>\n<name>originNodeType</name>\n<value>\n<string>EXT</string>\n"
                + "</value>\n</member>\n<member>\n<name>originHostName</name>\n<value>\n"
                + "<string>jaiprakash13540</string>\n</value>\n</member>\n<member>\n"
                + "<name>originTransactionID</name>\n<value>\n<string>123</string>"
                + "\n</value>\n</member>\n<member>\n<name>originTimeStamp</name>"
                + "\n<value>\n<dateTime.iso8601>20130610T11:20:59+0530</dateTime.iso8601>\n</value>"
                + "\n</member>\n<member>\n<name>subscriberNumberNAI</name>\n<value>\n<i4>2</i4>\n</value>"
                + "\n</member>\n<member>\n<name>subscriberNumber</name>\n<value>\n<string>9971224554</string>"
                + "\n</value>\n</member>\n<member>\n<name>requestPamInformationFlag</name>"
                + "\n<value>\n<boolean>1</boolean>\n</value>\n</member>\n</struct>\n</value>" + "\n</param>\n</params>\n</methodCall>";
        // final String httpRequest1 = "GetAccountDetails";

        final Endpoint endpoint = context.getEndpoint(BusinessConstants.HTTP_COMPONENT_AIR_ENDPOINT, DirectEndpoint.class);
        final Exchange exchange = endpoint.createExchange();
        exchange.setPattern(ExchangePattern.InOut);
        final Message in = exchange.getIn();
        in.setHeader(Exchange.HTTP_PROTOCOL_VERSION, "AirSampleApp/HandleAir");
        in.setHeader(Exchange.CONTENT_TYPE, "text/xml");
        in.setHeader(Exchange.HTTP_METHOD, "POST");
        in.setHeader(Exchange.HTTP_METHOD, BusinessConstants.HTTP_METHOD);
        in.setHeader(Exchange.CONTENT_LENGTH, httpRequest.length());
        in.setHeader("Accept", "text/xml");
        in.setHeader("Connection", "Keep-alive");
        exchange.setProperty(BusinessConstants.CIRCLE_CODE, "DEL");
        exchange.setProperty(BusinessConstants.EXTERNAL_SYSTEM_TYPE, "AIR");
        String requestId = "123";
        exchange.setProperty(BusinessConstants.REQUEST_ID, requestId);
        in.setBody(httpRequest);
        String outputXML = "";
        final Producer producer = endpoint.createProducer();
        producer.process(exchange);
        final Message out = exchange.getOut();
        outputXML = out.getBody(String.class);
        final String responseCode = out.getHeader("camelhttpresponsecode", String.class);
        final Logger circleLoggerRequest = FDPLoggerFactory.getRequestLogger("Delhi", BusinessModuleType.AIR_SOUTH.name());
        if (responseCode != null && "200".equals(responseCode) && outputXML != null) {
            FDPLogger.info(
                    circleLoggerRequest,
                    getClass(),
                    "process()",
                    new StringBuilder(BusinessConstants.REQUEST_ID).append(FDPConstant.LOGGER_KEY_VALUE_DELIMITER).append(requestId)
                            .append(FDPConstant.LOGGER_DELIMITER).append(FDPConstant.IFRESMODE)
                            .append(FDPConstant.LOGGER_KEY_VALUE_DELIMITER).append(FDPConstant.RESULT_SUCCESS).toString());
        } else {
            final Throwable caused = exchange.getProperty(Exchange.EXCEPTION_CAUGHT, Throwable.class);
            int statusCode = 0;
            String statusText = null;
            if (caused instanceof HttpOperationFailedException) {
                statusCode = ((HttpOperationFailedException) caused).getStatusCode();
                statusText = ((HttpOperationFailedException) caused).getStatusText();
            } else {
                statusText = caused.getMessage();
            }
            FDPLogger.info(
                    circleLoggerRequest,
                    getClass(),
                    "process()",
                    new StringBuilder(BusinessConstants.REQUEST_ID).append(FDPConstant.LOGGER_KEY_VALUE_DELIMITER).append(requestId)
                            .append(FDPConstant.LOGGER_DELIMITER).append(FDPConstant.IFRESMODE)
                            .append(FDPConstant.LOGGER_KEY_VALUE_DELIMITER).append(FDPConstant.RESULT_FAILURE)
                            .append(FDPConstant.LOGGER_DELIMITER).append(FDPConstant.IFRESRSN)
                            .append(FDPConstant.LOGGER_KEY_VALUE_DELIMITER).append(statusText).toString());
            final String outGoingcircleCodeIPaddressPort = exchange.getProperty(BusinessConstants.OUTGOING_CIRCLE_CODE_IP_PORT,
                    String.class);
            final StringBuilder stringBuilder = new StringBuilder();
            stringBuilder.append(BusinessConstants.REQUEST_ID).append(FDPConstant.LOGGER_KEY_VALUE_DELIMITER).append(requestId)
                    .append(FDPConstant.LOGGER_DELIMITER);
            if (outGoingcircleCodeIPaddressPort != null) {
                stringBuilder.append(FDPConstant.CHARGING_NODE_IP).append(FDPConstant.LOGGER_KEY_VALUE_DELIMITER)
                        .append(outGoingcircleCodeIPaddressPort).append(FDPConstant.LOGGER_DELIMITER + FDPConstant.INTERFACE_TYPE)
                        .append(FDPConstant.LOGGER_KEY_VALUE_DELIMITER).append("AIR").append(FDPConstant.LOGGER_DELIMITER)
                        .append(FDPConstant.CHARGING_NODE).append(FDPConstant.LOGGER_KEY_VALUE_DELIMITER).append("AIR_DELHI")
                        .append(FDPConstant.LOGGER_DELIMITER).append(FDPConstant.IFRESRSN).append(FDPConstant.LOGGER_KEY_VALUE_DELIMITER)
                        .append("Error in exchange is ").append(exchange.getException());
            } else {
                stringBuilder.append("Could not get out goind circle code and ip.");
            }
            stringBuilder.append(" Response code found ").append(statusCode).append(" with status text :").append(statusText);

            // responseCode = BusinessConstants.HTTP_ADAPTER_ERROR_CODE;
            FDPLogger.error(circleLoggerRequest, getClass(), "process()", stringBuilder.toString());
        }
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
     * @param circleCode the circle code
     * @return the endpoint ur ls for air by circle code
     */
    private List<FDPAIRConfigDTO> getEndpointURLsForAIRByCircleCode(final String circleCode) {
        return fdpAirConfigDAO.getAIREndpointByCircleCode(circleCode);
    }


    /**
     * Get Circlewise endpoints to weld load balancer.
     * 
     * @param fdpAIRConfigDTOList contains all air configurations
     * @param circleCode the circle code
     * @return the circle end points for lb
     */
    private String[] getCircleEndPointsForLB(final List<FDPAIRConfigDTO> fdpAIRConfigDTOList, final String circleCode) {
        final int noOfEndpoints = fdpAIRConfigDTOList.size();

        final List<String> airCircleEndpointList = new ArrayList<String>();
        for (int i = 0; i < noOfEndpoints; i++) {
            final FDPAIRConfigDTO fdpAIRConfigDTO = fdpAIRConfigDTOList.get(i);
            final String circleCodeEndpoint = circleCode + fdpAIRConfigDTO.getExternalSystemId() + BusinessConstants.UNDERSCORE
                    + fdpAIRConfigDTO.getLogicalName();
            airCircleEndpointList.add(BusinessConstants.HTTP_COMPONENT_AIR_ENDPOINT + circleCodeEndpoint);
        }
        return airCircleEndpointList.toArray(new String[airCircleEndpointList.size()]);

    }


    /**
     * This method is used to return AIR URL.
     * 
     * @param fdpAIRConfigDTO the fdp air config dto
     * @return AIR Url
     */
    private String getAIREndpointURLForCircle(final FDPAIRConfigDTO fdpAIRConfigDTO) {
        final String airUrl = BusinessConstants.HTTP_COMPONENT_TYPE + fdpAIRConfigDTO.getIpAddress().getValue() + BusinessConstants.COLON
                + fdpAIRConfigDTO.getPort() + BusinessConstants.QUERY_STRING_SEPARATOR + BusinessConstants.HTTP_CLIENT_SO_TIMEOUT
                + BusinessConstants.EQUALS + fdpAIRConfigDTO.getResponseTimeout();
        LOGGER.debug("Air Url:" + airUrl);
        return airUrl;
    }
}
