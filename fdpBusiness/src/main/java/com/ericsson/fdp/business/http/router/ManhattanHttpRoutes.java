package com.ericsson.fdp.business.http.router;

import javax.ejb.Remote;

import org.apache.camel.builder.RouteBuilder;

import com.ericsson.fdp.core.exception.ExecutionFailedException;
import com.ericsson.fdp.dao.dto.FDPVASConfigDTO;


/**
 * The Interface ManhattanHttpRoutes.
 */
@Remote
public interface ManhattanHttpRoutes {

	/**
	 * Creates the routes for m carbon.
	 *
	 * @param fdpVASConfigDTO the fdp m carbon config dto
	 * @param circleCode the circle code
	 * @return the route builder
	 * @throws ExecutionFailedException the execution failed exception
	 */
	RouteBuilder createRoutesForManhattan(FDPVASConfigDTO fdpvasConfigDTO, String circleCode)
			throws ExecutionFailedException;
	
}
