package com.ericsson.fdp.business.decorator.impl;

import com.ericsson.fdp.business.decorator.FDPDecorator;
import com.ericsson.fdp.business.display.impl.DisplayObjectImpl;
import com.ericsson.fdp.core.display.DisplayObject;
import com.ericsson.fdp.core.exception.ExecutionFailedException;
import com.ericsson.fdp.core.node.FDPNode;

/**
 * This class is a decorator to be used to decorate the text with a root menu at
 * the end.
 * 
 * @author Ericsson
 * 
 */
public class RootNodeDecorator implements FDPDecorator {

	/**
	 * The decorator which is further to be decorated with this decorator.
	 */
	private final FDPDecorator fdpDecorator;

	/**
	 * The root node string.
	 */
	private final FDPNode fdpNode;

	/**
	 * The constructor.
	 * 
	 * @param fdpNode
	 *            the fdpNode to add.
	 * @param fdpDecoratorToSet
	 *            The fdp decorator to be used.
	 */
	public RootNodeDecorator(final FDPDecorator fdpDecoratorToSet, final FDPNode fdpNode) {
		this.fdpDecorator = fdpDecoratorToSet;
		this.fdpNode = fdpNode;
	}

	@Override
	public DisplayObject display() throws ExecutionFailedException {
		final DisplayObject displayObject = fdpDecorator.display();
		if (displayObject instanceof DisplayObjectImpl) {
			final DisplayObjectImpl displayObjectImpl = (DisplayObjectImpl) displayObject;
			if (fdpNode != null) {
				FDPNode parentNode = fdpNode;
				while (parentNode.getParent() != null) {
					parentNode = parentNode.getParent();
				}
				if (!parentNode.equals(fdpNode)) {
					displayObjectImpl.addNodesToDisplay(parentNode);
				}
			} else {
				throw new ExecutionFailedException("Could not find root node to add");
			}
		}
		return displayObject;
	}
}
