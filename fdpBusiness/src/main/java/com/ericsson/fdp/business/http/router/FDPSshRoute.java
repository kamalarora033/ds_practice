package com.ericsson.fdp.business.http.router;

import java.util.List;

import javax.annotation.Resource;
import javax.ejb.Singleton;
import javax.inject.Inject;

import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.cdi.CdiCamelContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ericsson.fdp.business.constants.BusinessConstants;
import com.ericsson.fdp.business.route.controller.ChoiceDefinitionStorage;
import com.ericsson.fdp.business.route.controller.LoadBalancerStorage;
import com.ericsson.fdp.business.route.processor.LogOutgoingIpProcessor;
import com.ericsson.fdp.business.telnet.ema.TelnetProcessor;
import com.ericsson.fdp.common.enums.BusinessModuleType;
import com.ericsson.fdp.common.enums.EmaProtocolType;
import com.ericsson.fdp.dao.dto.FDPEMAConfigDTO;
import com.ericsson.fdp.dao.fdpadmin.FDPCircleDAO;
import com.ericsson.fdp.dao.fdpadmin.FDPEMAConfigDAO;
import com.ericsson.fdp.route.cdi.context.provider.CdiCamelContextProvider;
import com.ericsson.fdp.route.processors.LogRetryProcessor;
import com.ericsson.fdp.route.processors.RouteCompletionProcessor;

/**
 * The Class FDPSshRoute resposible for creating Telnet and SSH routes as per
 * the protocol specified per circle wise.
 */
@Singleton
public class FDPSshRoute {

	/** The cdi camel context provider. */
	@Inject
	private CdiCamelContextProvider cdiCamelContextProvider;

	/** The context. */
	private CdiCamelContext context;

	/** The fdp circle dao. */
	@Inject
	private FDPCircleDAO fdpCircleDAO;

	/** The fdp ema config dao. */
	@Inject
	private FDPEMAConfigDAO fdpEmaConfigDAO;

	/** The load balancer storage. */
	@Resource(lookup = "java:app/fdpBusiness-1.0/LoadBalancerStorage")
	private LoadBalancerStorage loadBalancerStorage;

	/** The choice definition storage. */
	@Resource(lookup = "java:app/fdpBusiness-1.0/ChoiceDefinitionStorage")
	private ChoiceDefinitionStorage choiceDefinitionStorage;

	/** The Constant LOGGER. */
	private static final Logger LOGGER = LoggerFactory.getLogger(FDPSshRoute.class);

	/** The log outgoing ip processor. */
	@Inject
	private LogOutgoingIpProcessor logOutgoingIpProcessor;

	/** The log retry processor. */
	@Inject
	private LogRetryProcessor logRetryProcessor;
	
	 @Inject
	 private RouteCompletionProcessor completionProcessor;
	 

	/**
	 * Following createRoutes to existing camel context.
	 * 
	 * @throws Exception
	 *             the exception
	 */
	public void createRoutes() throws Exception {
		context = cdiCamelContextProvider.getContext();
		context.addRoutes(createSshRoutes());
	}

	/**
	 * Creates the ssh and telnet routes as per Protocol type as MML or CAI.
	 * 
	 * @return the route builder
	 */
	private RouteBuilder createSshRoutes() {
		return new RouteBuilder() {

			@Override
			public void configure() throws Exception {

				final List<String> circleCodeList = getAllCircleCodes();

				for (final String circleCode : circleCodeList) {
					final List<FDPEMAConfigDTO> endpointListForCircle = getEndpointURLsForEMAByCircleCode(circleCode);
					for (final FDPEMAConfigDTO fdpemaConfigDTO : endpointListForCircle) {
						final String fromEndpoint = getDirectEndpointForCircle(fdpemaConfigDTO, circleCode);
						LOGGER.debug("FromEndpoint:" + fromEndpoint);
						final String toEndpoint = getEMAEndpointURLForCircle(fdpemaConfigDTO);
						LOGGER.debug("ToEndpoint:" + toEndpoint);
						final String routeId = compiledRouteId(fdpemaConfigDTO, circleCode);
						final String ipAddress = fdpemaConfigDTO.getIpAddress().getValue();
						final String circleCodeIpaddressPort = circleCode + BusinessConstants.COLON + ipAddress
								+ BusinessConstants.COLON + String.valueOf(fdpemaConfigDTO.getPort());

						final int retryInterval = fdpemaConfigDTO.getRetryInterval();
						final int numberOfRetry = fdpemaConfigDTO.getNumberOfRetries();
						TelnetProcessor telnetProcessor = new TelnetProcessor(fdpemaConfigDTO);
						/*if (EmaProtocolType.TELNET.name().equals(fdpemaConfigDTO.getProtocolType())) {
							

							// from(fromEndpoint+"?sync=true")
							from(fromEndpoint + "?sync=true").autoStartup(autostartup).routeId(routeId)
									.setExchangePattern(ExchangePattern.InOut)
									.setProperty(BusinessConstants.OUTGOING_IP_ADDRESS, constant(ipAddress))
									.setProperty(BusinessConstants.OUTGOING_CIRCLE_CODE_IP_PORT,
											constant(circleCodeIpaddressPort))
									.setProperty(BusinessConstants.LOGICAL_NAME,
											constant(fdpemaConfigDTO.getLogicalName()))
									.setProperty(BusinessConstants.MODULE_NAME,
											constant(BusinessModuleType.EMA_SOUTH.name()))
									.process(logOutgoingIpProcessor).onException(java.lang.Exception.class)
									.maximumRedeliveries(numberOfRetry).redeliveryDelay(retryInterval)
									.onRedelivery(logRetryProcessor).end().process(new Processor() {

										@Override
										public void process(Exchange exchange) throws Exception {
											exchange.getContext();
											
										}
									}).process(telnetProcessor);
							// .to(toEndpoint);
						} else { */

                        from(fromEndpoint).routeId(routeId)
									.setProperty(BusinessConstants.OUTGOING_IP_ADDRESS, constant(ipAddress))
									.setProperty(BusinessConstants.OUTGOING_CIRCLE_CODE_IP_PORT,
											constant(circleCodeIpaddressPort))
									.setProperty(BusinessConstants.LOGICAL_NAME,
											constant(fdpemaConfigDTO.getLogicalName()))
									.setProperty(BusinessConstants.MODULE_NAME,
											constant(BusinessModuleType.EMA_SOUTH.name()))
									.process(logOutgoingIpProcessor).onException(java.lang.Exception.class)
									.handled(false)
									.maximumRedeliveries(numberOfRetry).redeliveryDelay(retryInterval)
									.onRedelivery(logRetryProcessor)
									.to(BusinessConstants.COMPONENT_EXCEPTION_ENDPOINT).end()
									.process(telnetProcessor);
						//}
					}
				}
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
	 * Gets the endpoint ur ls for ema by circle code.
	 * 
	 * @param circleCode
	 *            the circle code
	 * @return the endpoint ur ls for ema by circle code
	 */
	private List<FDPEMAConfigDTO> getEndpointURLsForEMAByCircleCode(final String circleCode) {
		return fdpEmaConfigDAO.getEMAByCircleCode(circleCode);
	}

	/**
	 * Gets the direct endpoint for circle.
	 * 
	 * @param fdpemaConfigDTO
	 *            the fdpema config dto
	 * @param circleCode
	 *            the circle code
	 * @return the direct endpoint for circle
	 */
	private String getDirectEndpointForCircle(final FDPEMAConfigDTO fdpemaConfigDTO, final String circleCode) {
		final String endpoint = BusinessConstants.CAMEL_DIRECT + circleCode + BusinessConstants.UNDERSCORE
				+ fdpemaConfigDTO.getInterfaceType() + BusinessConstants.UNDERSCORE + fdpemaConfigDTO.getProtocolType()
				+ BusinessConstants.UNDERSCORE + fdpemaConfigDTO.getLogicalName();
		return endpoint;
	}

	/**
	 * Compiled route id.
	 * 
	 * @param fdpemaConfigDTO
	 *            the fdpema config dto
	 * @param circleCode
	 *            the circle code
	 * @return the string
	 */
	private String compiledRouteId(final FDPEMAConfigDTO fdpemaConfigDTO, final String circleCode) {
		final String routeId = circleCode + BusinessConstants.UNDERSCORE + fdpemaConfigDTO.getInterfaceType()
				+ BusinessConstants.UNDERSCORE + fdpemaConfigDTO.getProtocolType() + BusinessConstants.UNDERSCORE
				+ fdpemaConfigDTO.getLogicalName();
		return routeId;
	}

	/**
	 * Gets the eMA endpoint url for circle.
	 * 
	 * @param fdpEMAConfigDTO
	 *            the fdp ema config dto
	 * @return the eMA endpoint url for circle
	 */
	private String getEMAEndpointURLForCircle(final FDPEMAConfigDTO fdpEMAConfigDTO) {
		final String protocolType = fdpEMAConfigDTO.getProtocolType().toUpperCase();
		String emaUrl = null;
		LOGGER.debug("Protocol Type: {}", protocolType);
		if (protocolType.equals(EmaProtocolType.SSH.name())) {
			emaUrl = BusinessConstants.EMA_SSH_COMPONENT_TYPE + fdpEMAConfigDTO.getUserName() + BusinessConstants.COLON
					+ fdpEMAConfigDTO.getPassword() + BusinessConstants.AT_THE_RATE
					+ fdpEMAConfigDTO.getIpAddress().getValue() + BusinessConstants.COLON + fdpEMAConfigDTO.getPort()
					+ BusinessConstants.QUERY_STRING_SEPARATOR + "timeout=" + fdpEMAConfigDTO.getTimeout()
					+ "&synchronous=true";
			// + BusinessConstants.AMPERSAND_PARAMETER_APPENDER +
			// BusinessConstants.USE_FIXED_DELAY + "true&delay=1000";
		}
		if (protocolType.equals(EmaProtocolType.TELNET.name())) {
			emaUrl = BusinessConstants.EMA_TELNET_COMPONENT_TYPE.trim()
					+ fdpEMAConfigDTO.getIpAddress().getValue().trim() + BusinessConstants.COLON.trim()
					+ fdpEMAConfigDTO.getPort().toString().trim() + BusinessConstants.NETTY_PART_ENDPOINT_URL.trim();
			// + "&sync=true&keepAlive=true";
			// + "&disconnect=true";
		}
		LOGGER.debug("EMA Url: {} ", emaUrl);
		
		return emaUrl;
	}
}
