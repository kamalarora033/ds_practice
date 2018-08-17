package com.ericsson.fdp.business.batchjob.pam.service;

import java.util.List;

import javax.ejb.Remote;

import com.ericsson.fdp.business.vo.FDPPamActiveServicesVO;
import com.ericsson.fdp.core.exception.ExecutionFailedException;
import com.ericsson.fdp.core.request.FDPRequest;

/**
 * The Interface PAMActiveServices.
 */
@Remote
public interface PAMActiveServices {
	
	/**
	 * Gets the pam services id.
	 *
	 * @param fdpRequest the fdp request
	 * @return the pam services id
	 * @throws ExecutionFailedException the execution failed exception
	 */
	public List<FDPPamActiveServicesVO> getProductsFromPamServicesID(FDPRequest fdpRequest) throws ExecutionFailedException;

}
