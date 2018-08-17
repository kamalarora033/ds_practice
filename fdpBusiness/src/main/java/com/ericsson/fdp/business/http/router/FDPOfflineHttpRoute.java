package com.ericsson.fdp.business.http.router;

import java.io.Serializable;
import java.util.List;

import javax.ejb.Remote;

import org.apache.camel.builder.RouteBuilder;

import com.ericsson.fdp.core.exception.ExecutionFailedException;
import com.ericsson.fdp.dao.dto.FDPOfflineConfigDTO;

/**
 * The Interface MCLoanHttpRoutes.
 * 
 * @author ericsson
 */
@Remote
public interface FDPOfflineHttpRoute extends Serializable {

	/**
	 * Creates the routes for m carbon.
	 */
	RouteBuilder createFDPOfflineHttpRoutes(final FDPOfflineConfigDTO fdpOfflineConfigDTO, final String circleCode) throws ExecutionFailedException;
	
	/**
	 * Used to stop end route for the MCLOAN
	 * @param fdpMCLoanConfigDTO
	 * @throws ExecutionFailedException
	 */
	void stopRouteforOffline(FDPOfflineConfigDTO fdpOfflineConfigDTO) throws Exception;
	
	/**
	 * Creates the LB routes for m carbon.
	 */
	RouteBuilder createLbRouteForOffline(List<FDPOfflineConfigDTO> offlineDTOList,	String circleCode);
	
	void stopLbRouteForOffline(String circleCode) throws Exception;

	void stopAllRoutesForOffline(String circleCode) throws Exception;

	void startAllRoutesForOffline(String circleCode) throws Exception;

	void startLbRouteForOffline(String circleCode) throws Exception;

}
