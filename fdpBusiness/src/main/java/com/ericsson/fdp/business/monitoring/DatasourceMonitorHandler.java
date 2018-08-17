package com.ericsson.fdp.business.monitoring;

import com.ericsson.fdp.business.monitoring.component.ComponentChecker;

/**
 * The Class DatasourceMonitorHandler is responsible for monitoring DB
 * connection.
 */
// @Named("DatasourceMonitorHandler")
public class DatasourceMonitorHandler extends AbstractMonitorHandler {

	/** The component checker. */
	private ComponentChecker componentChecker;

	@Override
	public boolean process() {
		boolean processed = false;
		if (componentChecker != null && componentChecker.checkComponent()) {
			if (getNextHandler() != null) {
				return getNextHandler().process();
			}
			processed = true;
		}
		return processed;
	}

	/**
	 * Gets the component checker.
	 * 
	 * @return the component checker
	 */
	public ComponentChecker getComponentChecker() {
		return componentChecker;
	}

	/**
	 * Sets the component checker.
	 * 
	 * @param componentChecker
	 *            the new component checker
	 */
	public void setComponentChecker(final ComponentChecker componentChecker) {
		this.componentChecker = componentChecker;
	}

}
