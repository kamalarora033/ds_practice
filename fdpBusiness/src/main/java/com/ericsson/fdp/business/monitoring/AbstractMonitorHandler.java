package com.ericsson.fdp.business.monitoring;

/**
 * The Class AbstractMonitorHandler.
 */
public abstract class AbstractMonitorHandler implements MonitorHandler {
	
	/** The next handler in chain. */
	private MonitorHandler nextHandlerInChain; 

	
	@Override
	public MonitorHandler getNextHandler() {
		return nextHandlerInChain;
	}
	
	/**
	 * Sets the next handler.
	 *
	 * @param nextHandlerInChain the new next handler
	 */
	public void setNextHandler(MonitorHandler nextHandlerInChain) {
		this.nextHandlerInChain = nextHandlerInChain;
	}

	@Override
	public abstract boolean process();

}
