package com.ericsson.fdp.business.node;

import java.util.Map;

import com.ericsson.fdp.business.enums.AuxRequestParam;
import com.ericsson.fdp.business.enums.RequestMetaValuesKey;
import com.ericsson.fdp.core.exception.ExecutionFailedException;
import com.ericsson.fdp.core.request.FDPRequest;
import com.ericsson.fdp.core.request.FDPResponse;

/**
 * This interface defines the service provisioning nodes.
 * 
 * @author Ericsson
 * 
 */
public interface FDPServiceProvisioningNode {
	/**
	 * This method returns the entity id.
	 * 
	 * @return the entity id.
	 */
	String getEntityIdForCache(RequestMetaValuesKey key);

	/**
	 * This method is used to execute the policy related to the node.
	 * 
	 * @param fdpRequest
	 *            the request for which the policy is to be executed.
	 * @return the response formed.
	 */
	FDPResponse executePolicy(FDPRequest fdpRequest) throws ExecutionFailedException;

	/**
	 * This method is used to get the rule values.
	 * 
	 * @return the rule values.
	 */
	Map<AuxRequestParam, Object> getPolicyRuleValues();

}
