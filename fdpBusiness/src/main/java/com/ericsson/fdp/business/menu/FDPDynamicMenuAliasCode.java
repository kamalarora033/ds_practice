package com.ericsson.fdp.business.menu;

import com.ericsson.fdp.core.exception.ExecutionFailedException;
import com.ericsson.fdp.core.node.FDPNode;
import com.ericsson.fdp.core.request.FDPRequest;

/**
 * The Interface FDPDynamicMenuAliasCode.
 */
public interface FDPDynamicMenuAliasCode {
	
	/**
	 * Gets the fDP dynamic menu alias code.
	 *
	 * @param dynamicMenuRequest the dynamic menu request
	 * @return the fDP dynamic menu alias code
	 * @throws ExecutionFailedException 
	 */
	public FDPNode getFDPDynamicMenuAliasCode(FDPRequest dynamicMenuRequest) throws ExecutionFailedException;
}
