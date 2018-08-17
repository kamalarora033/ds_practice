package com.ericsson.fdp.business.policy.policyrule.impl;

import com.ericsson.fdp.business.enums.PolicyStatus;
import com.ericsson.fdp.business.request.FDPPolicyResponse;
import com.ericsson.fdp.business.request.impl.FDPPolicyResponseImpl;
import com.ericsson.fdp.core.exception.ExecutionFailedException;
import com.ericsson.fdp.core.request.FDPRequest;
import com.ericsson.fdp.core.request.FDPResponse;

/**
 * The Class DoubleConfirmationPolicyRuleImpl.
 * 
 * @author Ericsson
 */
public class DoubleConfirmationPolicyRuleImpl extends AbstractPolicyRule {

	/** The Constant serialVersionUID. */
	private static final long serialVersionUID = -3909816776829631039L;

	@Override
	public FDPResponse displayRule(final FDPRequest fdpRequest) throws ExecutionFailedException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public FDPPolicyResponse validatePolicyRule(final Object input, final FDPRequest fdpRequest,
			final Object... otherParams) throws ExecutionFailedException {
		return new FDPPolicyResponseImpl(PolicyStatus.SUCCESS, null, null, false, null);
	}

	@Override
	public boolean isRuleNoInputRequired() {
		return true;
	}

}
