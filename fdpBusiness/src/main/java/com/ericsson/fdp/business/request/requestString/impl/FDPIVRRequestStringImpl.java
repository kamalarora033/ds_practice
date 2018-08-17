package com.ericsson.fdp.business.request.requestString.impl;

import com.ericsson.fdp.core.node.FDPNode;
import com.ericsson.fdp.core.request.requestString.FDPRequestString;

/**
 * The Class FDPIVRRequestStringImpl is used to get the input from User.
 */
public class FDPIVRRequestStringImpl implements FDPRequestString {

	/** The Constant serialVersionUID. */
	private static final long serialVersionUID = -7525912298603734256L;

	/** The request string. */
	private String requestString;

	/**
	 * Instantiates a new fDPIVR request string impl.
	 * 
	 * @param requestStringToSet
	 *            the request string to set
	 */
	public FDPIVRRequestStringImpl(final String requestStringToSet) {
		this.requestString = requestStringToSet;
	}

	@Override
	public String getRequestString() {
		return requestString;
	}

	@Override
	public String getNodeString() {
		return requestString;
	}

	@Override
	public String getActionString(FDPNode dynamicMenuRequest) {
		return requestString;
	}

	@Override
	public String getActionString() {
		return requestString;
	}

}
