package com.ericsson.fdp.business.policy.policyrule.impl;

import java.util.List;
import java.util.Map;

import javax.naming.NamingException;

import com.ericsson.fdp.business.display.impl.DisplayObjectImpl;
import com.ericsson.fdp.business.enums.PolicyStatus;
import com.ericsson.fdp.business.request.FDPPolicyResponse;
import com.ericsson.fdp.business.request.impl.FDPPolicyResponseImpl;
import com.ericsson.fdp.business.sharedaccount.SharedAccountService;
import com.ericsson.fdp.core.node.FDPNode;
import com.ericsson.fdp.core.request.FDPRequest;
import com.ericsson.fdp.core.request.FDPResponse;
import com.ericsson.fdp.core.utils.ApplicationConfigUtil;
import com.ericsson.fdp.business.util.RequestUtil;
import com.ericsson.fdp.business.util.ResponseUtil;
import com.ericsson.fdp.core.exception.ExecutionFailedException;
import com.ericsson.fdp.dao.enums.DBActionClassName;
import com.ericsson.fdp.dao.enums.SharedAccountResponseType;

/**
 * This class implements the policy rule.
 * 
 * @author Ericsson
 * 
 */
public class PendingRequestPolicyRuleImpl extends PolicyRuleImpl {

	/**
	 * 
	 */
	private static final long serialVersionUID = 5999259668460963311L;

	private final String ejbLookupName = DBActionClassName.PENDING_REQUEST.getLookUpClass();

	@Override
	public FDPPolicyResponse validatePolicyRule(final Object input, final FDPRequest fdpRequest,
			final Object... otherParams) throws ExecutionFailedException {
		return new FDPPolicyResponseImpl(PolicyStatus.SUCCESS, null, null, true, null);
	}

	@Override
	public FDPResponse displayRule(final FDPRequest fdpRequest) throws ExecutionFailedException {
		if (ejbLookupName == null) {
			throw new ExecutionFailedException("Could not find ejb class for execution" + ejbLookupName);
		}
		try {
			final Object beanObject = ApplicationConfigUtil.getBean(ejbLookupName);
			if (beanObject instanceof SharedAccountService) {
				final SharedAccountService sharedAccountService = (SharedAccountService) beanObject;
				final Map<SharedAccountResponseType, Object> stepExecutedValue = sharedAccountService
						.executeSharedAccountService(fdpRequest);
				return createNodesFromOutput(stepExecutedValue.get(SharedAccountResponseType.NODES), fdpRequest);
			} else {
				throw new ExecutionFailedException("Ejb name not valid" + ejbLookupName);
			}
		} catch (final NamingException e) {
			throw new ExecutionFailedException("The bean could not be found " + ejbLookupName, e);
		}
	}

	private FDPResponse createNodesFromOutput(final Object object, final FDPRequest fdpRequest)
			throws ExecutionFailedException {
		if (object instanceof List<?>) {
			@SuppressWarnings("unchecked")
			final List<FDPNode> fdpNode = (List<FDPNode>) object;
			return RequestUtil.createResponseFromDisplayObject(
					new DisplayObjectImpl(null, fdpNode, ResponseUtil.getDisplayNodeMessage(fdpRequest.getChannel())),
					fdpRequest, null);
		}
		return null;
	}
}
