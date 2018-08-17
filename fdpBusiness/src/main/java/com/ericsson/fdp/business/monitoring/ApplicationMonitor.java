package com.ericsson.fdp.business.monitoring;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.ejb.DependsOn;
import javax.ejb.Singleton;
import javax.ejb.Startup;
import javax.inject.Inject;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ch.qos.logback.classic.LoggerContext;

import com.ericsson.fdp.business.monitoring.component.AIRComponentChecker;
import com.ericsson.fdp.business.monitoring.component.CGWComponentChecker;
import com.ericsson.fdp.business.monitoring.component.ComponentChecker;
import com.ericsson.fdp.business.monitoring.component.SMSCComponentChecker;
import com.ericsson.fdp.business.monitoring.component.USSDComponentChecker;
import com.ericsson.fdp.route.cdi.context.provider.CdiCamelContextProvider;

/**
 * The Class ApplicationMonitor is responsible for initializing Cache, check DB
 * whether it is up or not, and lastly start routes for circles.
 * 
 * @author Ericsson
 */
@Startup
@Singleton
@DependsOn(value = "CdiCamelContextProvider")
public class ApplicationMonitor {

	/** The logger. */
	private static final Logger LOGGER = LoggerFactory.getLogger(ApplicationMonitor.class);

	/** The external system monitor handler. */
	@Inject
	// @Named("ExternalSystemMonitorHandler")
	private ExternalSystemMonitorHandler externalSystemMonitorHandler;

	/** The fdp datasource monitor handler. */
	@Inject
	// @Named("DatasourceMonitorHandler")
	private DatasourceMonitorHandler fdpDatasourceMonitorHandler;

	/** The db component checker. */
	@Inject
	// @Named("DBComponentChecker")
	private ComponentChecker dbComponentChecker;

	/** The air component checker. */
	@Inject
	// @Named("AIRComponentChecker")
	private AIRComponentChecker airComponentChecker;

	/** The cgw component checker. */
	@Inject
	// @Named("CGWComponentChecker")
	private CGWComponentChecker cgwComponentChecker;

	/** The smsc component checker. */
	@Inject
	// @Named("SMSCComponentChecker")
	private SMSCComponentChecker smscComponentChecker;

	/** The ussd component checker. */
	@Inject
	// @Named("USSDComponentChecker")
	private USSDComponentChecker ussdComponentChecker;

	/** The cdi camel context provider. */
	@Inject
	private CdiCamelContextProvider cdiCamelContextProvider;

	/**
	 * Starting point for initializing application.
	 */
	@PostConstruct
	public void startApplication() {
		boolean isApplicationStarted = false;
		initializeChain();
		isApplicationStarted = fdpDatasourceMonitorHandler.process();
		if (isApplicationStarted) {
			LOGGER.info("Application started successfully");
		} else {
			LOGGER.warn("Unable to start application successfully");
		}
	}

	/**
	 * Responsible for Initializing chain.
	 */
	private void initializeChain() {
		// TODO Auto-generated method stub
		// set DatabaseComponent
		fdpDatasourceMonitorHandler.setComponentChecker(dbComponentChecker);
		// next Component is External System
		// Only checking USSD Component as of now
		externalSystemMonitorHandler.setFdpExternalSystemMonitorChain(airComponentChecker);
		/*
		 * smscComponentChecker.setNext(airComponentChecker);
		 * airComponentChecker.setNext(ussdComponentChecker);
		 */
		/*
		 * fdpCacheMonitorHandler.setNextHandler(externalSystemMonitorHandler);
		 * fdpCacheMonitorHandler.setFdpCacheInitializer(metaCacheInitializer);
		 */
		fdpDatasourceMonitorHandler.setNextHandler(externalSystemMonitorHandler);
	}

	/**
	 * Stop application.
	 */
	@PreDestroy
	public void stopApplication() {
		final LoggerContext loggerContext = (LoggerContext) LoggerFactory.getILoggerFactory();
		loggerContext.stop();
	}

}
