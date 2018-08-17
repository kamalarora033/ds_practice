package com.ericsson.fdp.business.request.impl;

import java.util.List;

import com.ericsson.fdp.business.enums.PolicyStatus;
import com.ericsson.fdp.business.request.FDPPolicyResponse;
import com.ericsson.fdp.core.display.ResponseMessage;

/**
 * The class defines the policy response.
 * 
 * @author Ericsson
 * 
 */
public class FDPPolicyResponseImpl implements FDPPolicyResponse {

	/**
	 * 
	 */
	private static final long serialVersionUID = -404962855554195060L;

	/**
	 * The policy status.
	 */
	private final PolicyStatus policyStatus;

	/**
	 * The result string.
	 */
	private final String resultString;

	/**
	 * The retry number.
	 */
	private final Long retryNumber;

	private final boolean terminateSession;

	private final List<ResponseMessage> responseMessages;
	
	/**
	 * Next Rule to be executed.
	 */
	private Integer nextRuleIndex;

	/**
	 * The constructor for policy response.
	 * 
	 * @param policyStatus
	 *            the policy status.
	 * @param resultString
	 *            the result string.
	 * @param retryNumber
	 *            the retry number.
	 */
	public FDPPolicyResponseImpl(final PolicyStatus policyStatus, final String resultString, final Long retryNumber,
			final boolean terminateSession, final List<ResponseMessage> responseMessages) {
		this.policyStatus = policyStatus;
		this.resultString = resultString;
		this.retryNumber = retryNumber;
		this.terminateSession = terminateSession;
		this.responseMessages = responseMessages;
	}

	@Override
	public PolicyStatus getPolicyStatus() {
		return policyStatus;
	}

	@Override
	public String getResultString() {
		return resultString;
	}

	@Override
	public Long policyRetryNumber() {
		return retryNumber;
	}

	@Override
	public boolean terminateSession() {
		return terminateSession;
	}

	@Override
	public List<ResponseMessage> getResponseMessages() {
		return responseMessages;
	}

	@Override
	public Integer getNextRuleIndex() {
		return nextRuleIndex;
	}
	
	@Override
	public void setNextRuleIndex(Integer index) {
		this.nextRuleIndex = index;
	}

}
