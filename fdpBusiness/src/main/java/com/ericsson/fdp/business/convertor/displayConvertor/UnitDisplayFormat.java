package com.ericsson.fdp.business.convertor.displayConvertor;

import com.ericsson.fdp.FDPCacheable;
import com.ericsson.fdp.core.exception.EvaluationFailedException;
import com.ericsson.fdp.core.request.FDPRequest;

public interface UnitDisplayFormat extends FDPCacheable {

	String evaluateValue(String input, FDPRequest fdpRequest, Object... otherParams) throws EvaluationFailedException;

}
