package com.ericsson.fdp.business.policy.policyrule.impl;

import com.ericsson.fdp.business.request.FDPPolicyResponse;
import com.ericsson.fdp.core.exception.ExecutionFailedException;
import com.ericsson.fdp.core.request.FDPRequest;
import com.ericsson.fdp.core.request.FDPResponse;
import com.ericsson.fdp.dao.enums.FDPServiceProvSubType;

public class SharedBonusBundleDeletePolicyImpl extends PolicyRuleImpl {

	/**
	 * The Class serial version UID.
	 */
	private static final long serialVersionUID = 5925932638071380889L;

	@Override
	public FDPResponse displayRule(final FDPRequest fdpRequest) throws ExecutionFailedException {
		FDPResponse fdpResponse = CommonSharedBonusBundlePolicyUtil.displayRule(fdpRequest);
		return fdpResponse;
	}

	@Override
	public final FDPPolicyResponse validatePolicyRule(final Object input, final FDPRequest fdpRequest,
			final Object... otherParams) throws ExecutionFailedException {

		FDPPolicyResponse response = CommonSharedBonusBundlePolicyUtil.validatePolicyRule(input, fdpRequest,
				FDPServiceProvSubType.SHARED_BUNDLE_DELETE_CONSUMER);
			return response;
	}
 }
