package com.ericsson.fdp.business.http.router;

import javax.inject.Inject;

import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.cdi.CdiCamelContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ericsson.fdp.business.route.processor.RequestIdGeneratorForVASExternalSystems;
import com.ericsson.fdp.business.route.processor.VASServiceRequestProcessor;
import com.ericsson.fdp.business.route.processor.VASWhiteListIpProcessor;
import com.ericsson.fdp.route.cdi.context.provider.CdiCamelContextProvider;

/**
 * The Class VASServiceRoute expose service to VAS external system.
 * 
 * @author Ericsson
 */
public class VASServiceRoute {

	/** The context. */
	private CdiCamelContext context;

	/** The cdi camel context provider. */
	@Inject
	private CdiCamelContextProvider cdiCamelContextProvider;

	/** The logger. */
	private static final Logger LOGGER = LoggerFactory.getLogger(VASServiceRoute.class);

	/** The vas service request processor. */
	@Inject
	private VASServiceRequestProcessor vasServiceRequestProcessor;

	/** The vas white list IP processor. */
	@Inject
	private VASWhiteListIpProcessor vasWhiteListIpProcessor;

	/** The request id generator. */
	@Inject
	private RequestIdGeneratorForVASExternalSystems requestIdGenerator;

	/**
	 * Creates the routes.
	 *
	 * @throws Exception the exception
	 */
	public void createRoutes() throws Exception {

		context = cdiCamelContextProvider.getContext();
		context.addRoutes(createVASRoute());
		LOGGER.debug("create AirRechargeRoute createRoutes() called.");
	}

	/**
	 * Creates the vas route.
	 *
	 * @return the route builder
	 */
	private RouteBuilder createVASRoute() {
		return new RouteBuilder() {

			@Override
			public void configure() throws Exception {
				from("servlet:///MOPush?servletName=VAS&matchOnUriPrefix=false").routeId("vas_service_route")
						.process(requestIdGenerator).process(vasWhiteListIpProcessor).process(vasServiceRequestProcessor);
			}
		};
	}

}
