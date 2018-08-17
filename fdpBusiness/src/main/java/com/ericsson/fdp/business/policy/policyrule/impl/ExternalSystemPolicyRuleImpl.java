package com.ericsson.fdp.business.policy.policyrule.impl;

import com.ericsson.fdp.business.enums.AuxRequestParam;
import com.ericsson.fdp.business.enums.PolicyStatus;
import com.ericsson.fdp.business.node.AbstractNode;
import com.ericsson.fdp.business.request.FDPPolicyResponse;
import com.ericsson.fdp.business.request.impl.FDPPolicyResponseImpl;
import com.ericsson.fdp.business.util.RequestUtil;
import com.ericsson.fdp.common.enums.FDPNodeAddInfoKeys;
import com.ericsson.fdp.core.exception.ExecutionFailedException;
import com.ericsson.fdp.core.request.FDPRequest;
import com.ericsson.fdp.core.request.FDPResponse;

/**
 * The Class ExternalSystemPolicyRuleImpl.
 * 
 * @author Ericsson
 */
public class ExternalSystemPolicyRuleImpl extends AbstractPolicyRule {

	/** The Constant serialVersionUID. */
	private static final long serialVersionUID = -5181948773096369205L;

	@Override
	public FDPResponse displayRule(final FDPRequest fdpRequest) throws ExecutionFailedException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public FDPPolicyResponse validatePolicyRule(final Object input, final FDPRequest fdpRequest,
			final Object... otherParams) throws ExecutionFailedException {
		for(final Object object : otherParams) {
			
		}
		if (otherParams[2] != null && otherParams[2] instanceof AbstractNode) {
			final AbstractNode dmNode = (AbstractNode) otherParams[2];
			final Object logicalName = dmNode.getAdditionalInfo(FDPNodeAddInfoKeys.EXTERNAL_SYSTEM_LOGICAL_NAME.name());
			if (logicalName != null) {
				RequestUtil.putAuxiliaryValueInRequest(fdpRequest, AuxRequestParam.EXTERNAL_SYSTEM_LOGICAL_NAME,
						logicalName);
			} else {
				throw new ExecutionFailedException("EXTERNAL_SYSTEM_LOGICAL_NAME not found.");
			}
		} else {
			throw new ExecutionFailedException("DM Node not found in Request.");
		}
		return new FDPPolicyResponseImpl(PolicyStatus.SUCCESS, null, null, false, null);
	}

}
