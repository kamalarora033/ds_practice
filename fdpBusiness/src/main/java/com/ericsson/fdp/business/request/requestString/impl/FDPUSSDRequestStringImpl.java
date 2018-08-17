package com.ericsson.fdp.business.request.requestString.impl;

import com.ericsson.fdp.common.constants.FDPConstant;
import com.ericsson.fdp.core.node.FDPNode;
import com.ericsson.fdp.core.request.requestString.FDPRequestString;

/**
 * This class implements the USSD request string.
 * 
 * @author Ericsson
 * 
 */
public class FDPUSSDRequestStringImpl implements FDPRequestString {

	/**
	 * 
	 */
	private static final long serialVersionUID = 6590798948065875217L;
	/**
	 * The request string.
	 */
	private String requestString;

	/**
	 * The constructor for ussd request string.
	 * 
	 * @param requestStringToSet
	 *            the request string to set.
	 */
	public FDPUSSDRequestStringImpl(final String requestStringToSet) {
		this.requestString = requestStringToSet;
	}

	@Override
	public String getRequestString() {
		return requestString;
	}

	@Override
	public String getNodeString() {
		String nodeString = null;
		if (requestString.contains(FDPConstant.USSD_STRING_START_CHAR) && requestString.lastIndexOf(FDPConstant.USSD_STRING_START_CHAR) > 0) {
			nodeString = requestString.substring(0, requestString.indexOf(FDPConstant.USSD_STRING_START_CHAR, 1)).concat(
					FDPConstant.USSD_STRING_END_CHAR);
		} else if (requestString.contains(FDPConstant.USSD_STRING_START_CHAR) && requestString.lastIndexOf(FDPConstant.USSD_STRING_START_CHAR) == 0){
			nodeString = requestString;
		}
		return nodeString;
	}

	@Override
	public String getActionString(final FDPNode fdpNode) {
		return (fdpNode == null) ? FDPConstant.EMPTY_STRING : fdpNode.getFullyQualifiedPath();
	}
	
	@Override
	public String getActionString() {
		return requestString;
	}

	@Override
	public String toString() {
		return "request string is " + requestString;
	}

}
