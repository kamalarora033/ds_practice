package com.ericsson.fdp.business.decorator.impl;

import java.util.List;

import com.ericsson.fdp.business.decorator.FDPDecorator;
import com.ericsson.fdp.business.display.impl.DisplayObjectImpl;
import com.ericsson.fdp.business.enums.DisplayArea;
import com.ericsson.fdp.core.display.DisplayObject;
import com.ericsson.fdp.core.exception.ExecutionFailedException;
import com.ericsson.fdp.core.node.FDPNode;

/**
 * This class is a decorator to be used to split the text for SMS.
 * 
 * @author Ericsson
 * 
 */
public class SplitDecorator implements FDPDecorator {

	/**
	 * The decorator which is further to be decorated with this decorator.
	 */
	private final FDPDecorator fdpDecorator;

	/**
	 * The constructor.
	 * 
	 * @param fdpDecoratorToSet
	 *            The fdp decorator to be used.
	 */
	public SplitDecorator(final FDPDecorator fdpDecoratorToSet) {
		this.fdpDecorator = fdpDecoratorToSet;
	}

	@Override
	public DisplayObject display() throws ExecutionFailedException {
		final DisplayObject displayObject = fdpDecorator.display();
		if (displayObject instanceof DisplayObjectImpl) {
			final DisplayObjectImpl displayObjectImpl = (DisplayObjectImpl) displayObject;
			displayObjectImpl.setCurrDisplayText(generateDisplayText(displayObject.getNodesToDisplay()),
					DisplayArea.MIDDLE);
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
	private String generateDisplayText(final List<FDPNode> nodes) {
		final StringBuffer displayText = new StringBuffer();
		// TODO: return the split text.
		return displayText.toString();
	}
}
