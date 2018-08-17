package com.ericsson.fdp.business.monitoring;

/**
 * The Interface MonitorHandler.
 * 
 * @author Ericsson.
 */
public interface MonitorHandler {
	
	/**
	 * Gets the next handler in Chain.
	 *
	 * @return the next handler
	 */
	MonitorHandler getNextHandler();
	
	/**
	 * Process monitor handler.
	 *
	 * @return true, if successful
	 */
	boolean process();

}
