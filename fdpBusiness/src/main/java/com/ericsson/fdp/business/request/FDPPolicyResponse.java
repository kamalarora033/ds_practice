package com.ericsson.fdp.business.request;

import java.util.List;

import com.ericsson.fdp.FDPCacheable;
import com.ericsson.fdp.business.enums.PolicyStatus;
import com.ericsson.fdp.core.display.ResponseMessage;

/**
 * The interface defines the request that will be used for execution.
 * 
 * @author Ericsson
 * 
 */
public interface FDPPolicyResponse extends FDPCacheable {

	/**
	 * The policy status.
	 * 
	 * @return the policy status.
	 */
	PolicyStatus getPolicyStatus();

	/**
	 * The result string.
	 * 
	 * @return the result string.
	 */
	String getResultString();

	/**
	 * The return number of the policy.
	 * 
	 * @return the policy retry number.
	 */
	Long policyRetryNumber();

	boolean terminateSession();

	List<ResponseMessage> getResponseMessages();
	
	/**
	 * Next Rule to be executed.
	 */
	Integer getNextRuleIndex();

	void setNextRuleIndex(Integer index);

}
