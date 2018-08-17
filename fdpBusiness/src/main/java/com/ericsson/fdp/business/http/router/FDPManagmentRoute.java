package com.ericsson.fdp.business.http.router;

import javax.inject.Inject;

import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.cdi.CdiCamelContext;
import org.apache.camel.model.RouteDefinition;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ericsson.fdp.business.managment.processor.FDPManagmentOnExceptionProcessor;
import com.ericsson.fdp.business.managment.processor.FDPManagmentProcessor;
import com.ericsson.fdp.managment.enums.FDPManagmentRouteEnum;
import com.ericsson.fdp.route.cdi.context.provider.CdiCamelContextProvider;

public class FDPManagmentRoute {
	
	/** The context. */
	private CdiCamelContext context;

	/** The cdi camel context provider. */
	@Inject
	private CdiCamelContextProvider cdiCamelContextProvider;
	
	@Inject
	private FDPManagmentProcessor fdpManagmentProcessor;
	
	@Inject
	private FDPManagmentOnExceptionProcessor fdpManagmentOnExceptionProcessor;

	/** The logger. */
	private static final Logger LOGGER = LoggerFactory.getLogger(FDPManagmentRoute.class);

	public void createRoutes() throws Exception {
		context = cdiCamelContextProvider.getContext();
		for(final FDPManagmentRouteEnum fdpManagmentRoute : FDPManagmentRouteEnum.values())
		context.addRoutes(createManagmentRoute(fdpManagmentRoute));
		LOGGER.debug("create FDPManagmentRoute createRoutes() called.");
		
	}
	
	private RouteBuilder createManagmentRoute(final FDPManagmentRouteEnum fdpManagmentRouteEnum) {
		final RouteBuilder routeBuilder = getRouteBuilder();
		routeBuilder.onException(Exception.class).process(fdpManagmentOnExceptionProcessor).end();
		final RouteDefinition routeDefinition = routeBuilder.from(fdpManagmentRouteEnum.getUrl()).routeId(fdpManagmentRouteEnum.getRouteId());
		routeDefinition.process(fdpManagmentProcessor);
		return routeBuilder;
	}
	
	private RouteBuilder getRouteBuilder() {
		return new RouteBuilder() {
			@Override
			public void configure() throws Exception {
			}
		};
	}

}
