package com.ericsson.fdp.business.convertor.conversion;

import com.ericsson.fdp.FDPCacheable;
import com.ericsson.fdp.core.exception.ExecutionFailedException;
import com.ericsson.fdp.core.request.FDPRequest;

public interface ConversionRules extends FDPCacheable {

	boolean evaluateConversion(String input, FDPRequest fdpRequest) throws ExecutionFailedException;

	public String execute(String input, FDPRequest fdpRequest) throws ExecutionFailedException;

}
