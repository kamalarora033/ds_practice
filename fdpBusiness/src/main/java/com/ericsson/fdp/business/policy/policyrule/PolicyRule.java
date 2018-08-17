package com.ericsson.fdp.business.policy.policyrule;

import java.io.Serializable;
import java.util.Map;

import com.ericsson.fdp.business.enums.AuxRequestParam;
import com.ericsson.fdp.business.request.FDPPolicyResponse;
import com.ericsson.fdp.core.exception.ExecutionFailedException;
import com.ericsson.fdp.core.request.FDPRequest;
import com.ericsson.fdp.core.request.FDPResponse;

/**
 * This interface defines the policy rule.
 * 
 * @author Ericsson
 * 
 */
public interface PolicyRule extends Serializable {

	/**
	 * This method is used to display the rule.
	 * 
	 * @return the response to be sent to the user.
	 */
	FDPResponse displayRule(FDPRequest fdpRequest) throws ExecutionFailedException;

	/**
	 * This method is used to get the param associated with this rule.
	 * 
	 * @return the param associated against which the data is to be stored in
	 *         request.
	 */
	AuxRequestParam getAuxiliaryParam();

	/**
	 * This method is used to validate the input against the policy rule.
	 * 
	 * @param input
	 *            the input to be validated.
	 * @param otherParams
	 *            the other parameters if required.
	 * @param fdpRequest
	 *            the request object to use.
	 * @return the failure string, if validation fails, null otherwise.
	 * @throws ExecutionFailedException
	 *             Exception in execution.
	 */
	FDPPolicyResponse validatePolicyRule(Object input, FDPRequest fdpRequest, Object... otherParams)
			throws ExecutionFailedException;

	/**
	 * This method is used to get the map of the rule values.
	 * 
	 * @return map of rule values.
	 */
	Map<? extends AuxRequestParam, ? extends Object> getRuleValues();
	
	
	boolean isRuleNoInputRequired();

}
