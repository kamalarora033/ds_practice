package com.ericsson.fdp.business.display.impl;

import java.util.ArrayList;
import java.util.List;

import com.ericsson.fdp.business.enums.DisplayArea;
import com.ericsson.fdp.core.display.DisplayObject;
import com.ericsson.fdp.core.display.ResponseMessage;
import com.ericsson.fdp.core.node.FDPNode;

/**
 * This class defines the display object which is to be shown to the user.
 * 
 * @author Ericsson
 * 
 */
public class DisplayObjectImpl implements DisplayObject {
	/**
	 * 
	 */
	private static final long serialVersionUID = 3240838071079448775L;
	/**
	 * The evaluated nodes for this menu.
	 */
	private List<FDPNode> nodesToDisplay;

	/**
	 * The current node.
	 */
	private FDPNode currentNode;

	/**
	 * The response message.
	 */
	private ResponseMessage responseMessage;

	public DisplayObjectImpl(final FDPNode currentNode, final ResponseMessage responseMessage) {
		super();
		this.currentNode = currentNode;
		this.responseMessage = responseMessage;
	}

	public DisplayObjectImpl(final FDPNode currentNode, List<FDPNode> nodesToDisplay,
			final ResponseMessage responseMessage) {
		this(currentNode, responseMessage);
		this.nodesToDisplay = nodesToDisplay;
	}

	@Override
	public List<FDPNode> getNodesToDisplay() {
		return nodesToDisplay;
	}

	/**
	 * This method adds the evaluated node.
	 * 
	 * @param nodesToDisplayToAdd
	 *            the display node to add
	 */
	public void addNodesToDisplay(final FDPNode nodesToDisplayToAdd) {
		if (this.nodesToDisplay == null) {
			this.nodesToDisplay = new ArrayList<FDPNode>();
		}
		this.nodesToDisplay.add(nodesToDisplayToAdd);
	}

	@Override
	public String toString() {
		return "current display text " + responseMessage + " nodes to display " + nodesToDisplay;
	}

	@Override
	public FDPNode getCurrentNode() {
		return currentNode;
	}

	@Override
	public ResponseMessage getResponseMessage() {
		return responseMessage;
	}

	/**
	 * This method is used to add display text in the response message.
	 * 
	 * @param displayString
	 *            the display string.
	 * @param displayArea
	 *            the display area.
	 */
	public void setCurrDisplayText(final String displayString, final DisplayArea displayArea) {
		if (responseMessage instanceof ResponseMessageImpl) {
			ResponseMessageImpl responseMessageImpl = (ResponseMessageImpl) responseMessage;
			responseMessageImpl.setCurrDisplayText(displayString, displayArea);
		}
	}

	@Override
	public String getCurrDisplayText(final DisplayArea displayArea) {
		return responseMessage != null ? responseMessage.getCurrDisplayText(displayArea) : null;
	}
}
