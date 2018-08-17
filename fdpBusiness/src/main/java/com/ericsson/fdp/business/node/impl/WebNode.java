package com.ericsson.fdp.business.node.impl;

import com.ericsson.fdp.business.node.AbstractNode;
import com.ericsson.fdp.core.request.FDPRequest;

/**
 * The Class WebNode.
 */
public class WebNode extends AbstractNode {

	/** The Constant serialVersionUID. */
	private static final long serialVersionUID = 3632712193730528502L;

	@Override
	public String generateNodeInfo(FDPRequest dynamicMenuRequest) {
		return "WebNode";
	}

}
