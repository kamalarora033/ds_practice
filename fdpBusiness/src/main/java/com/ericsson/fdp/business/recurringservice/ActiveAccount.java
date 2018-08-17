package com.ericsson.fdp.business.recurringservice;

import java.util.List;

import javax.ejb.Remote;

import com.ericsson.fdp.common.vo.FDPCCAdminVO;
import com.ericsson.fdp.core.exception.ExecutionFailedException;

@Remote
public interface ActiveAccount {
	/**
	 * This method is used to fetching the Active RS product list corresponding
	 * to a msisdn.
	 * 
	 * @param msisdn
	 * @return
	 * @throws ExecutionFailedException
	 */
	List<FDPCCAdminVO> executeActiveAccountService(final String msisdn) throws ExecutionFailedException;

}
