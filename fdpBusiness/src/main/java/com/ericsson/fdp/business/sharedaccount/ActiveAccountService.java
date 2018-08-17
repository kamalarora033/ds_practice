package com.ericsson.fdp.business.sharedaccount;

import java.util.List;

import javax.ejb.Remote;

import com.ericsson.fdp.business.vo.FDPActiveServicesVO;
import com.ericsson.fdp.core.exception.ExecutionFailedException;
import com.ericsson.fdp.core.request.FDPRequest;

@Remote
public interface ActiveAccountService {

	List<FDPActiveServicesVO> executeActiveAccountService(FDPRequest fdpRequest, Object... params)
			throws ExecutionFailedException;
	
	public List<String> getRsDeProvisionList(final FDPRequest fdpRequest) throws ExecutionFailedException;

}
