package com.ericsson.fdp.business.sharedaccount.service;

import javax.ejb.Remote;

import com.ericsson.fdp.business.request.FDPCheckConsumerResponse;
import com.ericsson.fdp.common.exception.FDPServiceException;
import com.ericsson.fdp.core.exception.ExecutionFailedException;
import com.ericsson.fdp.core.request.FDPRequest;

/**
 * The Interface CheckConsumerService.
 * 
 * @author Ericsson
 */
//@Remote
public interface CheckConsumerService {

	/**
	 * Check pre paid consumer.
	 * 
	 * @param request
	 *            the request
	 * @return the fDP check consumer response
	 * @throws FDPServiceException
	 *             the fDP service exception
	 */
	FDPCheckConsumerResponse checkPrePaidConsumer(FDPRequest request) throws ExecutionFailedException;

}
