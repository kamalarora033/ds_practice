package com.ericsson.fdp.business.monitoring.component;

/**
 * The Interface ExternalSystemMonitorChain.
 */
public interface ExternalSystemMonitorChain {
	
	/**
	 * get next external system in chain.
	 *
	 * @return the external system monitor chain
	 */
	ExternalSystemMonitorChain next();
	
	/**
	 * Monitor component.
	 *
	 * @param circle the circle
	 * @return true, if successful
	 */
	boolean monitorComponent(String circle);

}
