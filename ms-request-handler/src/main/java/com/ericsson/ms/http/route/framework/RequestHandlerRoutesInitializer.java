package com.ericsson.ms.http.route.framework;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;

import org.apache.camel.CamelContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.ericsson.ms.common.util.ReqHandlerLoggerUtil;
import com.ericsson.ms.http.route.contextprovider.CdiCamelContextProvider;
import com.ericsson.ms.http.route.offline.ActiveMQDirectRouteBuilder;
import com.ericsson.ms.http.route.offline.OfflineEventNotificationRouteBuilder;

/**
 * The Startup class for all north bound route
 * 
 * @author Ericsson
 *
 */
@Component
public class RequestHandlerRoutesInitializer {

	private static final Logger logger = LoggerFactory.getLogger(RequestHandlerRoutesInitializer.class);

	private static final String INITIALIZE_ROUTE = "initializeRoutes()";

	@Autowired
	private CdiCamelContextProvider objCdiCamelContextProvider;

	@Autowired
	private OfflineEventNotificationRouteBuilder offlineEventNotificationRouteBuilder;

	@Autowired
	private ActiveMQDirectRouteBuilder activeMQDirectRouteBuilder;

	/**
	 * The Apache camel context
	 */
	private CamelContext objCamelContext;

	/**
	 * Spring bean post construct method
	 */
	@PostConstruct
	public void init() {
		objCamelContext = objCdiCamelContextProvider.getContext();
		initializeRoutes();
	}

	/**
	 * Initialize all North bound camel routes
	 */
	public void initializeRoutes() {
		try {
			ReqHandlerLoggerUtil.info(logger, getClass(), INITIALIZE_ROUTE, "Starting camel routes ...............");
			objCamelContext.addRoutes(offlineEventNotificationRouteBuilder);
			objCamelContext.addRoutes(activeMQDirectRouteBuilder);

			objCamelContext.start();
			ReqHandlerLoggerUtil.info(logger, getClass(), INITIALIZE_ROUTE,
					"Successfully started camel routes ..............");
		} catch (Exception e) {
			ReqHandlerLoggerUtil.error(logger, getClass(), INITIALIZE_ROUTE, "Error while creating camel route", e);
		}

	}

	/**
	 * Spring bean pre destroy method
	 */
	@PreDestroy
	public void destroy() {
		try {
			ReqHandlerLoggerUtil.info(logger, getClass(), "destroy()", "Stopping camel routes ...............");
			objCamelContext.stop();
		} catch (Exception e) {
			ReqHandlerLoggerUtil.error(logger, getClass(), "destroy()", "Error while stopping camel context", e);
		}
	}

}
