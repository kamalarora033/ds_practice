package com.ericsson.fdp.business.cache.loading.impl;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import javax.ejb.EJB;
import javax.ejb.Singleton;
import javax.ejb.Startup;
import javax.naming.NamingException;

import org.jboss.as.server.CurrentServiceContainer;
import org.jboss.as.server.ServerEnvironment;
import org.jboss.msc.service.Service;
import org.jboss.msc.service.ServiceController;
import org.jboss.msc.service.ServiceName;
import org.jboss.msc.service.StartContext;
import org.jboss.msc.service.StartException;
import org.jboss.msc.service.StopContext;
import org.jboss.msc.value.InjectedValue;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ericsson.fdp.business.monitoring.cache.MetaCacheInitializer;
import com.ericsson.fdp.common.constants.FDPConstant;
import com.ericsson.fdp.common.exception.FDPException;
import com.ericsson.fdp.core.cache.service.AppCacheInitializerService;
import com.ericsson.fdp.core.dsm.framework.service.CacheLoadState;
import com.ericsson.fdp.core.utils.ApplicationConfigUtil;

/**
 * The Class CacheLoadSingletonServiceImpl.
 * 
 * @author Ericsson
 */
@Singleton
@Startup
public class CacheLoadSingletonServiceImpl {

	/** The Constant LOGGER. */
	private static final Logger LOGGER = LoggerFactory.getLogger(CacheLoadSingletonServiceImpl.class);

	/** The Constant SINGLETON_SERVICE_NAME. *//*
	public static final ServiceName SERVICE_NAME = ServiceName.JBOSS.append("myservice1");

	*//** The env. *//*
	final InjectedValue<ServerEnvironment> env = new InjectedValue<ServerEnvironment>();*/
	
	/** The app cache initializer service. */
	/*@Resource(lookup = "java:app/fdpCoreServices-1.0/AppCacheInitializerServiceImpl")
	private AppCacheInitializerService appCacheInitializerService;
	
	@Resource(lookup = "java:app/fdpBusiness-1.0/MetaCacheInitializer")
	private MetaCacheInitializer metaCacheInitializer;*/
	
	@EJB
	private AppCacheInitializerService appCacheInitializerService;
	
	@EJB
	private MetaCacheInitializer metaCacheInitializer;

	/*@Override
	public void start(final StartContext context) throws StartException {
		LOGGER.info("Cache Load Service Called.");
		CacheLoadSingletonStateHolder holder = this.getServiceStateHolder();
		try {
			
			try {
				Thread.sleep(10000);
			} catch (final InterruptedException e) {
				e.printStackTrace();
			}
			
			final AppCacheInitializerService appCacheInitializerService = (AppCacheInitializerService) ApplicationConfigUtil
					.getBean("java:global/fdpBusiness-ear/fdpCoreServices-1.0/AppCacheInitializerServiceImpl");
			final MetaCacheInitializer metaCacheInitializer = (MetaCacheInitializer) ApplicationConfigUtil
					.getBean("java:global/fdpBusiness-ear/fdpBusiness-1.0/MetaCacheInitializer");
			
			if (appCacheInitializerService.isCacheNeedsLoading()) {
				appCacheInitializerService.setCacheStatus(CacheLoadState.INITIALIZING);
				LOGGER.info("Loading APP Cache");
				appCacheInitializerService.initializeCache();
				appCacheInitializerService.setCacheStatus(CacheLoadState.INITIALIZED);
			} else if(this.getSystemPropertyFlag(FDPConstant.APPCACHE_FORCE_RELOAD)){
				appCacheInitializerService.setCacheStatus(CacheLoadState.INITIALIZING);
				System.out.println("Forcely Loading APP Cache");
				LOGGER.info("Forcely Loading APP Cache");
				appCacheInitializerService.initializeCache();
				appCacheInitializerService.setCacheStatus(CacheLoadState.INITIALIZED);
			} else {
				LOGGER.info("Not Loading APP Cache");
				appCacheInitializerService.setCacheStatus(CacheLoadState.INITIALIZED);
			}
			
			if (metaCacheInitializer.isCacheNeedsLoading()) {
				metaCacheInitializer.setCacheStatus(CacheLoadState.INITIALIZING);
				LOGGER.info("Loading META Cache");
				metaCacheInitializer.initializeCache();
				metaCacheInitializer.setCacheStatus(CacheLoadState.INITIALIZED);
			} else if(this.getSystemPropertyFlag(FDPConstant.METACACHE_FORCE_RELOAD)){
				metaCacheInitializer.setCacheStatus(CacheLoadState.INITIALIZING);
				System.out.println("Forcely Loading META Cache");
				LOGGER.info("Forcely Loading META Cache");
				metaCacheInitializer.initializeCache();
				metaCacheInitializer.setCacheStatus(CacheLoadState.INITIALIZED);
			} else {
				LOGGER.info("Not Loading META Cache");
				metaCacheInitializer.setCacheStatus(CacheLoadState.INITIALIZED);
			}
		} catch (final FDPException e) {
			holder.changeStateTo(CacheLoadState.FAILED);
			LOGGER.error("Exception occured while loading cache : ", e);
			throw new StartException(e);
		} catch (final NamingException e) {
			holder.changeStateTo(CacheLoadState.FAILED);
			LOGGER.error("Exception occured while loading cache : ", e);
			throw new StartException(e);
		} catch(Exception e){
			holder.changeStateTo(CacheLoadState.FAILED);
			LOGGER.error("Exception occured while loading cache : ", e);
			throw new StartException(e);
		}
	}*/
	
	@PostConstruct
	public void loadCache() throws FDPException {
		LOGGER.info("Cache Load Service Called.");
		
		/*AppCacheInitializerService appCacheInitializerService;
		MetaCacheInitializer metaCacheInitializer;
		
		try {
			appCacheInitializerService = (AppCacheInitializerService) ApplicationConfigUtil
					.getBean("java:global/fdpBusiness-ear/fdpCoreServices-1.0/AppCacheInitializerServiceImpl");
			metaCacheInitializer = (MetaCacheInitializer) ApplicationConfigUtil
					.getBean("java:global/fdpBusiness-ear/fdpBusiness-1.0/MetaCacheInitializer");
		} catch (NamingException e1) {
			LOGGER.error("Error while lookup of EJB.", e1);
			throw new FDPException(e1);
		}*/
		
		try {
			if (appCacheInitializerService.isCacheNeedsLoading()) {
				appCacheInitializerService.setCacheStatus(CacheLoadState.INITIALIZING);
				LOGGER.info("Loading APP Cache");
				appCacheInitializerService.initializeCache();
				appCacheInitializerService.setCacheStatus(CacheLoadState.INITIALIZED);
			} else if(this.getSystemPropertyFlag(FDPConstant.APPCACHE_FORCE_RELOAD)){
				appCacheInitializerService.setCacheStatus(CacheLoadState.INITIALIZING);
				System.out.println("Forcely Loading APP Cache");
				LOGGER.info("Forcely Loading APP Cache");
				appCacheInitializerService.initializeCache();
				appCacheInitializerService.setCacheStatus(CacheLoadState.INITIALIZED);
			} else {
				LOGGER.info("Not Loading APP Cache");
				appCacheInitializerService.setCacheStatus(CacheLoadState.INITIALIZED);
			}
		} catch (final FDPException e) {
			appCacheInitializerService.setCacheStatus(CacheLoadState.FAILED);
			LOGGER.error("Exception occured while loading cache : ", e);
			throw e;
		} catch(Exception e){
			appCacheInitializerService.setCacheStatus(CacheLoadState.FAILED);
			LOGGER.error("Exception occured while loading cache : ", e);
			throw new FDPException(e);
		}
		
		try {
			if (metaCacheInitializer.isCacheNeedsLoading()) {
				metaCacheInitializer.setCacheStatus(CacheLoadState.INITIALIZING);
				LOGGER.info("Loading META Cache");
				metaCacheInitializer.initializeCache();
				metaCacheInitializer.setCacheStatus(CacheLoadState.INITIALIZED);
			} else if(this.getSystemPropertyFlag(FDPConstant.METACACHE_FORCE_RELOAD)){
				metaCacheInitializer.setCacheStatus(CacheLoadState.INITIALIZING);
				System.out.println("Forcely Loading META Cache");
				LOGGER.info("Forcely Loading META Cache");
				metaCacheInitializer.initializeCache();
				metaCacheInitializer.setCacheStatus(CacheLoadState.INITIALIZED);
			} else {
				LOGGER.info("Not Loading META Cache");
				metaCacheInitializer.setCacheStatus(CacheLoadState.INITIALIZED);
			}
		} catch(Exception e){
			metaCacheInitializer.setCacheStatus(CacheLoadState.FAILED);
			LOGGER.error("Exception occured while loading cache : ", e);
			throw new FDPException(e);
		}
	}

	/*@Override
	public void stop(final StopContext context) {
		// Do Nothing
	}

	@Override
	public String getValue() throws IllegalStateException, IllegalArgumentException {
		// Do Nothing
		return null;
	}*/
	
	private boolean getSystemPropertyFlag(String key){
		boolean result = false;
		String value = System.getProperty(key);
		
		if(value != null){
			try{
				result = Boolean.parseBoolean(value);
			} catch(Exception ex){
				System.out.println("Invalid Boolean Type for System Property: "+key+" Value: "+value);
				LOGGER.error("Invalid Boolean Type for System Property: "+key+" Value: "+value);
			}
		}
		
		return result;
	}
}
