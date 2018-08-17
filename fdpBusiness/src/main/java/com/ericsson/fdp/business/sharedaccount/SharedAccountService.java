package com.ericsson.fdp.business.sharedaccount;

import java.util.Map;

import javax.ejb.Remote;

import com.ericsson.fdp.core.exception.ExecutionFailedException;
import com.ericsson.fdp.core.request.FDPRequest;
import com.ericsson.fdp.dao.enums.SharedAccountResponseType;

@Remote
public interface SharedAccountService {

	Map<SharedAccountResponseType, Object> executeSharedAccountService(FDPRequest fdpRequest, Object... params)
			throws ExecutionFailedException;

}
