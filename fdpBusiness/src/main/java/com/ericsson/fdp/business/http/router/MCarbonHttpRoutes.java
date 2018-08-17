package com.ericsson.fdp.business.http.router;

import java.io.Serializable;

import javax.ejb.Remote;

import org.apache.camel.builder.RouteBuilder;

import com.ericsson.fdp.core.exception.ExecutionFailedException;
import com.ericsson.fdp.dao.dto.FDPVASConfigDTO;

/**
 * The Interface MCarbonHttpRoutes.
 * 
 * @author Ericsson
 */
@Remote
public interface MCarbonHttpRoutes extends Serializable{

	/**
	 * Creates the routes for m carbon.
	 */
	RouteBuilder createRoutesForMCarbon(final FDPVASConfigDTO fdpMCarbonConfigDTO, final String circleCode)
			throws ExecutionFailedException;

}
