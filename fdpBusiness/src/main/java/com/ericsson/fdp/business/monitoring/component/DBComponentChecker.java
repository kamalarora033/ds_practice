package com.ericsson.fdp.business.monitoring.component;

import com.ericsson.fdp.common.util.ConnectionUtils;

/**
 * The Class DBComponentChecker is responsible for checking whether DB is up or
 * not.
 */
// @Named("DBComponentChecker")
public class DBComponentChecker implements ComponentChecker {

	@Override
	public boolean checkComponent() {
		return ConnectionUtils.isDBConnectionAvailable();
	}

}
