package com.ericsson.fdp.business.batchjob.airconfigreload.service;
import javax.ejb.Remote;

import com.ericsson.fdp.dao.exception.FDPConcurrencyException;

@Remote
public interface AirConfigReloadJob {

	public void execute() throws FDPConcurrencyException;
}
