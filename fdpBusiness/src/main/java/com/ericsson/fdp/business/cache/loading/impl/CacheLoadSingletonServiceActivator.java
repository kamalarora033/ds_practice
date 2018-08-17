package com.ericsson.fdp.business.cache.loading.impl;

import org.jboss.msc.service.ServiceActivator;
import org.jboss.msc.service.ServiceActivatorContext;
import org.jboss.msc.service.ServiceRegistryException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The Class CacheLoadSingletonServiceActivator.
 * 
 * @author Ericsson
 */
public class CacheLoadSingletonServiceActivator implements ServiceActivator {

	/** The Constant LOGGER. */
	private static final Logger LOGGER = LoggerFactory.getLogger(CacheLoadSingletonServiceActivator.class);
	
	@Override
	public void activate(final ServiceActivatorContext context) throws ServiceRegistryException {
		/*LOGGER.info("| activate() | Executing Service Activator (Cache Load)");
		final CacheLoadSingletonServiceImpl service = new CacheLoadSingletonServiceImpl();
		final SingletonService<String> singleton =
				new SingletonService<String>(service, CacheLoadSingletonServiceImpl.SINGLETON_SERVICE_NAME);
		final ServiceBuilder<String> builder =
				singleton
						.build(new DelegatingServiceContainer(context.getServiceTarget(), context.getServiceRegistry()));
		builder.setInitialMode(ServiceController.Mode.ACTIVE);
		builder.addDependency(ServerEnvironmentService.SERVICE_NAME, ServerEnvironment.class, service.env);
		builder.install();
		LOGGER.info("| activate() | Service Activator Executed Successfully (Cache Load)");*/
	}
	
	/*@SuppressWarnings("unchecked")
	private CacheLoadSingletonStateHolder getServiceStateHolder() throws ServiceRegistryException {
		CacheLoadSingletonStateHolder holder = null;
		try{
			ServiceController<CacheLoadServiceState> controller = (ServiceController<CacheLoadServiceState>) CurrentServiceContainer
					.getServiceContainer().getService(CacheLoadSingletonStateHolderImpl.SINGLETON_SERVICE_NAME);
			
			if(controller == null){
				throw new FDPException("ServiceController<CacheLoadServiceState> controller is null");
			}
			Service<CacheLoadServiceState> service = controller.getService();
			if(service instanceof AsynchronousService){
				holder = ((AsynchronousService)service).;
			} else{
				throw new FDPException("Invalid Type of holder: "+service.getClass().getName());
			}
		} catch(Exception e){
			LOGGER.error("Error while getting CacheLoadSingletonStateHolder", e);
			throw new ServiceRegistryException(e);
		}
		return holder;
	}*/

}
