package com.ericsson.fdp.business.monitoring.component;

import com.ericsson.fdp.common.util.SystemUtils;
import com.ericsson.fdp.dao.dto.FDPExternalSystemDTO;

/**
 * The Class CGWComponentChecker monitors CGW System.
 *
 * @author Ericsson
 */
// @Named("CGWComponentChecker")
public class CGWComponentChecker extends AbstractExternalSystemChecker {

	@Override
	protected boolean monitorComponent(FDPExternalSystemDTO fdpExternalSystemDTO) {
		return SystemUtils.pingServer(fdpExternalSystemDTO.getIpAddress().getValue());
	}

}
