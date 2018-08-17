package com.ericsson.fdp.business.http.router;

import javax.annotation.PostConstruct;
import javax.ejb.Singleton;
import javax.ejb.Startup;
import javax.inject.Inject;

import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.cdi.CdiCamelContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ericsson.fdp.business.route.processor.AirRechargeRequestProcessor;
import com.ericsson.fdp.business.route.processor.WhiteListIpProcessor;
import com.ericsson.fdp.route.cdi.context.provider.CdiCamelContextProvider;

/**
 * The Class AirRechargeRoute.
 */
@Singleton
@Startup
public class AirRechargeRoute {

	/** The context. */
	private CdiCamelContext context;

	/** The cdi camel context provider. */
	@Inject
	private CdiCamelContextProvider cdiCamelContextProvider;

	/** The logger. */
	private static final Logger LOGGER = LoggerFactory.getLogger(AirRechargeRoute.class);

	/** The air recharge processor. */
	@Inject
	private AirRechargeRequestProcessor airRechargeProcessor;

	/** The white list ip processor. */
	@Inject
	private WhiteListIpProcessor whiteListIpProcessor;

	/**
	 * Creates the routes.
	 * 
	 * @throws Exception
	 *             the exception
	 */
	@PostConstruct
	public void createRoutes() throws Exception {

		context = cdiCamelContextProvider.getContext();
		context.addRoutes(createAirRechargeRoute());
		LOGGER.debug("create AirRechargeRoute createRoutes() called.");
	}

	/**
	 * Creates the air recharge route.
	 * 
	 * @return the route builder
	 */
	private RouteBuilder createAirRechargeRoute() {
		return new RouteBuilder() {

			@Override
			public void configure() throws Exception {
				from("servlet://request?servletName=AirRecharge&matchOnUriPrefix=true")
						.routeId("air_recharge_route").autoStartup(true).process(whiteListIpProcessor)
						.process(airRechargeProcessor);
			}
		};
	}

}
