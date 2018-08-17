package com.ericsson.fdp.business.http.router;

import java.io.Serializable;
import java.util.List;

import javax.ejb.Remote;

import org.apache.camel.builder.RouteBuilder;

import com.ericsson.fdp.core.exception.ExecutionFailedException;
import com.ericsson.fdp.dao.dto.FDPMCLoanConfigDTO;

/**
 * The Interface MCLoanHttpRoutes.
 * 
 * @author ericsson
 */
@Remote
public interface MCLoanHttpRoutes extends Serializable {

	/**
	 * Creates the routes for m carbon.
	 */
	RouteBuilder createRoutesForMCLoan(FDPMCLoanConfigDTO fdpMCLoanConfigDTO, String circleCode) throws ExecutionFailedException;
	
	/**
	 * Used to stop end route for the MCLOAN
	 * @param fdpMCLoanConfigDTO
	 * @throws ExecutionFailedException
	 */
	void stopRouteforMcLoan(FDPMCLoanConfigDTO fdpMCLoanConfigDTO) throws Exception;
	
	/**
	 * Creates the LB routes for m carbon.
	 */
	RouteBuilder createLbRouteForMCLoan(List<FDPMCLoanConfigDTO> mcLoanDTOList,	String circleCode);
	
	void stopLbRouteForMcLoan(String circleCode) throws Exception;

	void stopAllRoutesForMcLoan(String circleCode) throws Exception;

	void startAllRoutesForMcLoan(String circleCode) throws Exception;

	void startLbRouteForMcLoan(String circleCode) throws Exception;

}
