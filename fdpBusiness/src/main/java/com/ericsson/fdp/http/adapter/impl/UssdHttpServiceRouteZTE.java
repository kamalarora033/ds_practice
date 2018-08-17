package com.ericsson.fdp.http.adapter.impl;

import javax.inject.Inject;

import org.apache.camel.ExchangePattern;
import org.apache.camel.LoggingLevel;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.cdi.CdiCamelContext;
import org.apache.camel.impl.ThrottlingInflightRoutePolicy;
import org.apache.camel.model.RouteDefinition;
import org.apache.camel.util.CamelLogger;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ericsson.fdp.business.enums.ivr.UssdZteHttpRouteEnum;
import com.ericsson.fdp.business.route.processor.UssdHttpRequestProcessorZTE;
import com.ericsson.fdp.business.route.processor.UssdHttpWhiteListIpProcessor;
import com.ericsson.fdp.business.route.processor.ivr.impl.UssdHttpExceptionProcessor;
import com.ericsson.fdp.route.cdi.context.provider.CdiCamelContextProvider;


public class UssdHttpServiceRouteZTE {
	
	private static final Logger LOGGER = LoggerFactory.getLogger(UssdHttpServiceRoute.class);
		
	/** The cdi camel context provider. */
	@Inject
	private CdiCamelContextProvider cdiCamelContextProvider;

	/** The context. */
	//private CdiCamelContext context;

	/** The request id generator. */
	@Inject
	private UssdHttpWhiteListIpProcessor ussdHttpWhiteListIpProcessor;
	
	
	
	/** The in message processor. */
	
	/** The throttling inflight route policy. */
	private ThrottlingInflightRoutePolicy throttlingInflightRoutePolicy = null;
	
	@Inject
	private UssdHttpRequestProcessorZTE requestProcessor;
	
	@Inject
	private UssdHttpExceptionProcessor ussdHttpExceptionProcessor;


	
	
	/**
	 * Creates the routes.
	 * 
	 * @throws Exception
	 *             the exception
	 */
	public void createRoutes() throws Exception {
		final CdiCamelContext context = cdiCamelContextProvider.getContext();
		for (final UssdZteHttpRouteEnum httpService : UssdZteHttpRouteEnum.values()) {
			LOGGER.debug("creating {} route.", httpService.getRouteId());
			context.addRoutes(createHttpServiceRoute(httpService));
			LOGGER.debug("{} route created.", httpService.getRouteId());
		}
	}
	
	/**
	 * Creates the ivr service route.
	 * 
	 * @param ivr
	 *            the ivr
	 * @return the route builder
	 */
	private RouteBuilder createHttpServiceRoute(final UssdZteHttpRouteEnum httpService){

		throttlingInflightRoutePolicy = new ThrottlingInflightRoutePolicy();
		final RouteBuilder routeBuilder = getRouteBuilder();
		routeBuilder.onException(Exception.class).process(ussdHttpExceptionProcessor).end();
		final RouteDefinition route = routeBuilder.from(httpService.getFromURL()).routeId(httpService.getRouteId());
		// As per confirmation from Manmeet,expecting configuration to be always
		// OK.
		// So no need to handle NumberFormatException
		throttlingInflightRoutePolicy.setMaxInflightExchanges(Integer.parseInt(httpService.getMaxInFlightNumber()));
		throttlingInflightRoutePolicy.setResumePercentOfMax(Integer.parseInt(httpService.getMaxInflighPercentage()));
		CamelLogger camelLogger = throttlingInflightRoutePolicy.getLogger();
		camelLogger.setLevel(LoggingLevel.WARN);
		throttlingInflightRoutePolicy.setLogger(camelLogger);
		route.routePolicy(throttlingInflightRoutePolicy);
		//final String routeKey = HttpUtil.getUSSDHttpKey(BusinessConstants.USSD_HTTP_ROUTE_UNDERSCORE);

		switch (httpService) {
		case USSD_HTTP_SERVICE_ZTE:
			route
			//.process(fulfillmentWhiteListIpProcessor)
			.setExchangePattern(ExchangePattern.InOut)
			.convertBodyTo(String.class)
			//.setHeader(RoutingConstant.ROUTE_ID, constant(routeKey))
			//.process(ussdHttpWhiteListIpProcessor)
			.process(requestProcessor);
					//.process(commandServiceRequestIdGenerator).process(fulfillmentMandatoryParameterProcessor)
					//.process(fulfillmentAuthenticationProcessor).process(fulfillmentCircleAndMSISDNValidateProcessor)
					//.process(ivrCommandServiceProcessor);
			break;
				}
		return routeBuilder;
	
	}


	
	/**
	 * Gets the route builder.
	 * 
	 * @return the route builder
	 */
	private RouteBuilder getRouteBuilder() {
		return new RouteBuilder() {
			@Override
			public void configure() throws Exception {
			}
		};
	}



}
