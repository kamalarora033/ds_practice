/**
 * 
 */
package com.ericsson.fdp.business.policy.impl;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ericsson.fdp.business.enums.RequestMetaValuesKey;
import com.ericsson.fdp.business.node.FDPServiceProvisioningNode;
import com.ericsson.fdp.business.policy.FDPExecutesServiceProv;
import com.ericsson.fdp.business.util.ApplicationBusinessConfigUtil;
import com.ericsson.fdp.business.util.ServiceProvisioningUtil;
import com.ericsson.fdp.core.exception.EvaluationFailedException;
import com.ericsson.fdp.core.exception.ExecutionFailedException;
import com.ericsson.fdp.core.policy.Policy;
import com.ericsson.fdp.core.request.FDPRequest;
import com.ericsson.fdp.core.request.FDPResponse;

/**
 * The Class ServiceProvPolicyImpl.
 * 
 * @author Ericsson
 */
public class ServiceProvPolicyImpl extends AbstractPolicy implements Policy, FDPExecutesServiceProv {

	/** The Constant serialVersionUID. */
	private static final long serialVersionUID = -3737953746996251269L;

	/** The Constant LOGGER. */
	private static final Logger LOGGER = LoggerFactory.getLogger(ServiceProvPolicyImpl.class);

	@Override
	public FDPResponse executeServiceProv(final FDPRequest request) throws ExecutionFailedException {

		FDPResponse fdpResponse = null;
		try {
			fdpResponse = ServiceProvisioningUtil.executeServiceProvisioning(
					(FDPServiceProvisioningNode) request.getValueFromRequest(RequestMetaValuesKey.NODE), request,
					ApplicationBusinessConfigUtil.getServiceProvisioning());
		} catch (final EvaluationFailedException e) {
			LOGGER.error("Exception Occured.", e);
			throw new ExecutionFailedException(e.getMessage(), e);
		}
		return fdpResponse;
	}

}
