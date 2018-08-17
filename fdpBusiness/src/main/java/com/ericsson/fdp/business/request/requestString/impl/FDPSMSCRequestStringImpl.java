package com.ericsson.fdp.business.request.requestString.impl;

import com.ericsson.fdp.common.constants.FDPConstant;
import com.ericsson.fdp.core.node.FDPNode;
import com.ericsson.fdp.core.request.requestString.FDPRequestString;

/**
 * This class implements the SMSC request string.
 * 
 * @author Ericsson
 * 
 */
public class FDPSMSCRequestStringImpl implements FDPRequestString {

	/**
	 * 
	 */
	private static final long serialVersionUID = -3115371783388847820L;

	/**
	 * The destination address string.
	 */
	private String destinationAddress;

	/**
	 * The request string.
	 */
	private String messageString;

	/**
	 * The compiled string.
	 */
	private String compiledString;

	/**
	 * The compiled string with space.
	 */
	private String compiledStringWithSpace;

	/**
	 * The destination address for the request.
	 */
	private String desitinationAddressOfRequest;

	/**
	 * The constructor for ussd request string.
	 * 
	 * @param requestStringToSet
	 *            the request string to set.
	 * @param destinationAddressToSet
	 *            the destinationa address to set.
	 */
	public FDPSMSCRequestStringImpl(final String destinationAddressToSet, final String messageStringToSet) {
		this.destinationAddress = destinationAddressToSet;
		this.messageString = messageStringToSet;
	}

	/**
	 * The constructor for ussd request string.
	 * 
	 * @param requestStringToSet
	 *            the request string to set.
	 * @param destinationAddressToSet
	 *            the destinationa address to set.
	 */
	public FDPSMSCRequestStringImpl(final String destinationAddressToSet, final String messageStringToSet,
			final String destinationAddressOfRequest) {
		this(destinationAddressToSet, messageStringToSet);
		this.desitinationAddressOfRequest = destinationAddressOfRequest;
	}

	@Override
	public String getRequestString() {
		if (compiledString == null) {
			compiledString = (destinationAddress == null ? FDPConstant.EMPTY_STRING : destinationAddress)
					+ (messageString == null ? FDPConstant.EMPTY_STRING : destinationAddress == null ? messageString
							: FDPConstant.SPACE + messageString);
			// compiledString = compiledString.replaceAll(FDPConstant.SPACE,
			// FDPConstant.EMPTY_STRING);
		}
		return compiledString.toLowerCase();
	}

	public String getRequestStringWithSpace() {
		if (compiledStringWithSpace == null) {
			compiledStringWithSpace = destinationAddress
					+ (messageString == null ? FDPConstant.EMPTY_STRING : messageString);
		}
		return compiledStringWithSpace.toLowerCase();
	}

	@Override
	public String getNodeString() {
		return desitinationAddressOfRequest.toLowerCase();
	}

	@Override
	public String getActionString(final FDPNode fdpNode) {
		/*return (fdpNode == null) ? FDPConstant.EMPTY_STRING : fdpNode.getFullyQualifiedPath()
				.replace(getDesitinationAddressOfRequest(), FDPConstant.EMPTY_STRING).trim();*/
		return (fdpNode == null) ? FDPConstant.EMPTY_STRING : fdpNode.getFullyQualifiedPath();
	}

	@Override
	public String getActionString() {
		return messageString.toLowerCase();
	}

	@Override
	public String toString() {
		return "request string is " + destinationAddress + " message string" + messageString;
	}

	/**
	 * @return the desitinationAddressOfRequest
	 */
	public String getDesitinationAddressOfRequest() {
		return desitinationAddressOfRequest;
	}

	/**
	 * @param desitinationAddressOfRequest
	 *            the desitinationAddressOfRequest to set
	 */
	public void setDesitinationAddressOfRequest(String desitinationAddressOfRequest) {
		this.desitinationAddressOfRequest = desitinationAddressOfRequest;
	}
}
