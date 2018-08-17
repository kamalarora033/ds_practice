package com.ericsson.fdp.business.enums;

/**
 * The status of policy.
 * 
 * @author Ericsson
 * 
 */
public enum PolicyStatus {

	/**
	 * Policy succeeded.
	 */
	SUCCESS,
	/**
	 * Policy failed.
	 */
	FAILURE,
	/**
	 * Policy value not found.
	 */
	POLICY_VALUE_NOT_FOUND,
	/**
	 * Policy is to be retried.
	 */
	RETRY_POLICY,

	EXECUTE_SP,
	
	/**
	 * Jump to Rule defined in nextRuleIndex of fdpPolicyResponse.
	 */
	GOTO_RULE,
	
	/**
	 * Finish Rule Execution
	 */
	SKIP_EXECUTION;

}
