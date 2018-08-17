package com.ericsson.fdp.business.monitoring.component;

import com.ericsson.fdp.common.util.SystemUtils;
import com.ericsson.fdp.dao.dto.FDPExternalSystemDTO;

/**
 * The Class RSComponentChecker.
 * @author Ericsson
 */
public class RSComponentChecker extends AbstractExternalSystemChecker {

	/* (non-Javadoc)
	 * @see com.ericsson.fdp.business.monitoring.component.AbstractExternalSystemChecker#monitorComponent(com.ericsson.fdp.dao.dto.FDPExternalSystemDTO)
	 */
	@Override
	protected boolean monitorComponent(FDPExternalSystemDTO fdpExternalSystemDTO) {
		return SystemUtils.pingServer(fdpExternalSystemDTO.getIpAddress().getValue());
	}

}
