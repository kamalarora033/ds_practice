package com.ericsson.fdp.business.builder;

import java.util.concurrent.ExecutorService;

import com.ericsson.fdp.core.exception.ExecutionFailedException;
import com.ericsson.fdp.core.request.FDPRequest;
import com.ericsson.fdp.dao.enums.ExternalSystem;

public interface IThreadBuilder {
	ExecutorService	getExecutorService(FDPRequest fdpRequest, ExternalSystem externalsystem);

	ExecutorService getThreadPoolExecutor(FDPRequest fdpRequest, ExternalSystem externalsystem) throws ExecutionFailedException;
}