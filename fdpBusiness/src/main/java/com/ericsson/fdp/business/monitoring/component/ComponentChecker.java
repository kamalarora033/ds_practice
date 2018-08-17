package com.ericsson.fdp.business.monitoring.component;

/**
 * The Interface ComponentChecker checks respective components.
 */
public interface ComponentChecker {
	
	/**
	 * Check component if it is working or not.
	 *
	 * @return true, if successful
	 */
	boolean checkComponent();

}
