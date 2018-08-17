package com.ericsson.fdp.business.policy.policyrule.impl;

import com.ericsson.fdp.business.enums.AuxRequestParam;
import com.ericsson.fdp.business.enums.PolicyStatus;
import com.ericsson.fdp.business.request.FDPPolicyResponse;
import com.ericsson.fdp.business.request.impl.FDPPolicyResponseImpl;
import com.ericsson.fdp.core.exception.ExecutionFailedException;
import com.ericsson.fdp.core.node.FDPNode;
import com.ericsson.fdp.core.request.FDPRequest;
import com.ericsson.fdp.core.request.FDPResponse;
import com.ericsson.fdp.dao.enums.EntityType;

/**
 * This class implements the policy rule.
 * 
 * @author Ericsson
 * 
 */
public class ProvUTUpgradePolicyRuleImpl extends PolicyRuleImpl {

	/**
	 * 
	 */
	private static final long serialVersionUID = 5999259668460963311L;

	@Override
	public FDPPolicyResponse validatePolicyRule(final Object input, final FDPRequest fdpRequest, final Object... params)
			throws ExecutionFailedException {
		if (params != null && params[2] instanceof FDPNode) {
			super.addAuxRequestParamValue(AuxRequestParam.CONSUMER_LIMIT_UPGRADE_VALUE,
					((FDPNode) params[2]).getAdditionalInfo(EntityType.NEW_LIMIT.getEntityType()));
			super.addAuxRequestParamValue(AuxRequestParam.CURRENT_SHARED_ACCOUNT_VALUE, params[2]);
		}
		return new FDPPolicyResponseImpl(PolicyStatus.SUCCESS, null, null, false, null);
	}

	@Override
	public FDPResponse displayRule(final FDPRequest fdpRequest) throws ExecutionFailedException {
		return null;
	}
}
