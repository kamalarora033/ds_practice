package com.ericsson.fdp.business.monitoring.component;

import com.ericsson.fdp.dao.dto.FDPExternalSystemDTO;

/**
 * The Class AIRComponentChecker monitors AIR System.
 * 
 * @author Ericsson
 */
// @Named("AIRComponentChecker")
public class AIRComponentChecker extends AbstractExternalSystemChecker {

	@Override
	public boolean monitorComponent(final FDPExternalSystemDTO airComponent) {
		// return SystemUtils.pingServer(airComponent.getIpAddress());
		return true;
	}

}
