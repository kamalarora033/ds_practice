package com.ericsson.fdp.business.decorator.impl;

import java.util.ArrayList;
import java.util.Arrays;

import com.ericsson.fdp.business.decorator.FDPDecorator;
import com.ericsson.fdp.business.display.impl.DisplayObjectImpl;
import com.ericsson.fdp.business.display.impl.ResponseMessageImpl;
import com.ericsson.fdp.business.enums.DisplayArea;
import com.ericsson.fdp.common.constants.FDPConstant;
import com.ericsson.fdp.core.display.DisplayObject;
import com.ericsson.fdp.core.display.ResponseMessage;
import com.ericsson.fdp.core.node.FDPNode;

/**
 * This class is a base decorator to be used to decorate the text.
 * 
 * @author Ericsson
 * 
 */
public class BaseResponseDecorator implements FDPDecorator {
	/**
	 * The display object to be decorated.
	 */
	private ResponseMessage displayObject;

	private FDPNode currentNode;

	/**
	 * The constructor for base decorator.
	 * 
	 * @param displayObjectToSet
	 *            The display object to decorate.
	 * @param separatorToSet
	 *            The seperator to be used.
	 */
	public BaseResponseDecorator(final ResponseMessage displayObjectToSet, final FDPNode currentNode) {
		this.displayObject = displayObjectToSet;
		this.currentNode = currentNode;
		if (displayObject instanceof ResponseMessageImpl) {
			ResponseMessageImpl responseMessageImpl = (ResponseMessageImpl) displayObjectToSet;
			responseMessageImpl.setCurrDisplayText(
					displayObject.getCurrDisplayText(DisplayArea.COMPLETE) + FDPConstant.NEWLINE, DisplayArea.MIDDLE);
			responseMessageImpl.setCurrDisplayText(
					null, DisplayArea.COMPLETE);
		}
	}

	@Override
	public DisplayObject display() {
		return new DisplayObjectImpl(currentNode, new ArrayList<FDPNode>(Arrays.asList(currentNode)), displayObject);
	}

}
