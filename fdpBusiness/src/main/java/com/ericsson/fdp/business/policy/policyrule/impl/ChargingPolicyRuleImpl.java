/**
 * 
 */
package com.ericsson.fdp.business.policy.policyrule.impl;

import com.ericsson.fdp.business.enums.PolicyStatus;
import com.ericsson.fdp.business.request.FDPPolicyResponse;
import com.ericsson.fdp.business.request.impl.FDPPolicyResponseImpl;
import com.ericsson.fdp.core.exception.ExecutionFailedException;
import com.ericsson.fdp.core.request.FDPRequest;
import com.ericsson.fdp.core.request.FDPResponse;

/**
 * The Class ChargingPolicyRuleImpl.
 * 
 * @author Ericsson
 */
public class ChargingPolicyRuleImpl extends AbstractPolicyRule {

	/** The Constant serialVersionUID. */
	private static final long serialVersionUID = -1686975420055073356L;

	@Override
	public FDPPolicyResponse validatePolicyRule(final Object input, final FDPRequest fdpRequest,
			final Object... otherParams) throws ExecutionFailedException {
		final FDPPolicyResponse response = new FDPPolicyResponseImpl(PolicyStatus.EXECUTE_SP, null, null, false, null);
		// ResponseUtil.createResponseMessageInList(fdpRequest.getChannel(),
		// policyValid
		// .getResultString(), policyValid.continueSession() ?
		// TLVOptions.SESSION_CONTINUE
		// : TLVOptions.SESSION_TERMINATE)
		return response;
	}

	@Override
	public FDPResponse displayRule(final FDPRequest fdpRequest) throws ExecutionFailedException {
		// TODO Auto-generated method stub
		return null;
	}

}
