package com.ericsson.fdp.business.rule;

import java.io.FileNotFoundException;

import com.ericsson.fdp.FDPCacheable;
import com.ericsson.fdp.business.exception.RuleException;
import com.ericsson.fdp.core.request.FDPRequest;
import com.ericsson.fdp.core.request.FDPResponse;

/**
 * This class defines the rules which can be exceuted.
 * 
 * @author Ericsson
 * 
 */
public interface Rule extends FDPCacheable {

	/**
	 * This method executes the rule.
	 * 
	 * @param fdpRequest
	 *            The request for which the rule is to be executed.
	 * @return The response after execution.
	 * @throws RuleException
	 *             Exception, if it occurs while executing the rule.
	 * @throws FileNotFoundException 
	 */
	FDPResponse execute(FDPRequest fdpRequest) throws RuleException, FileNotFoundException;
}
