package com.ericsson.fdp.business.node;

import com.ericsson.fdp.FDPCacheable;
import com.ericsson.fdp.core.exception.ExecutionFailedException;
import com.ericsson.fdp.core.node.FDPNode;
import com.ericsson.fdp.core.request.FDPRequest;

/**
 * The interface defines the methods that have to be implemented by nodes.
 * 
 * @author Ericsson
 * 
 */
public interface FDPCustomNode extends FDPNode, FDPServiceProvisioningNode {

	FDPCacheable getServiceProvisioning(FDPRequest fdpRequest) throws ExecutionFailedException;

}
