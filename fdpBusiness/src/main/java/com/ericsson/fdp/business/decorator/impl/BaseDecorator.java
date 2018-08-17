package com.ericsson.fdp.business.decorator.impl;

import java.util.Collections;
import java.util.List;

import com.ericsson.fdp.business.comparator.PriorityComparator;
import com.ericsson.fdp.business.decorator.FDPDecorator;
import com.ericsson.fdp.business.display.impl.DisplayObjectImpl;
import com.ericsson.fdp.business.enums.DisplayArea;
import com.ericsson.fdp.core.display.DisplayObject;
import com.ericsson.fdp.core.node.FDPNode;
import com.ericsson.fdp.core.request.FDPRequest;

/**
 * This class is a base decorator to be used to decorate the text.
 * 
 * @author Ericsson
 * 
 */
public class BaseDecorator implements FDPDecorator {
	/**
	 * The display object to be decorated.
	 */
	private final DisplayObject displayObject;

	/**
	 * The seperator to be used.
	 */
	private final String seperator;

	private FDPRequest fdpRequest;
	/**
	 * The constructor for base decorator.
	 * 
	 * @param displayObjectToSet
	 *            The display object to decorate.
	 * @param separatorToSet
	 *            The seperator to be used.
	 */
	public BaseDecorator(final DisplayObject displayObjectToSet, final String separatorToSet, final FDPRequest fdpRequest) {
		this.displayObject = displayObjectToSet;
		this.seperator = separatorToSet;
		this.fdpRequest = fdpRequest;
	}

	@Override
	public DisplayObject display() {
		if (displayObject instanceof DisplayObjectImpl) {
			final List<FDPNode> nodes = this.displayObject.getNodesToDisplay();
			Collections.sort(nodes, new PriorityComparator());
			final DisplayObjectImpl displayObjectImpl = (DisplayObjectImpl) displayObject;
			displayObjectImpl.setCurrDisplayText(generateDisplayText(nodes,fdpRequest), DisplayArea.MIDDLE);
		}
		return displayObject;
	}

	/**
	 * This method is used to generate the display text.
	 * 
	 * @param nodes
	 *            The nodes for which the display text is to be created.
	 * @return The display text.
	 */
	protected String generateDisplayText(final List<FDPNode> nodes , final FDPRequest fdpRequest) {
		final StringBuffer displayText = new StringBuffer();
		int nodeCount = 1;
		for (final FDPNode node : nodes) {
			//displayText.append(nodeCount).append(seperator).append(node.getDisplayName());
			displayText.append(nodeCount).append(seperator).append(node.getOtherLanguageText(fdpRequest));
			nodeCount++;
		}
		return displayText.toString();
	}

}
