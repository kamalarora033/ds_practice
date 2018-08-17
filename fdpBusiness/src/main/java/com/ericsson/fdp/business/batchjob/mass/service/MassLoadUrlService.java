package com.ericsson.fdp.business.batchjob.mass.service;

import javax.ejb.Remote;

import com.ericsson.fdp.dao.exception.FDPConcurrencyException;

@Remote
public interface MassLoadUrlService {

	/**
	 * 
	 * @throws FDPConcurrencyException
	 */
	public void massProvisionExecution() throws FDPConcurrencyException;
}
