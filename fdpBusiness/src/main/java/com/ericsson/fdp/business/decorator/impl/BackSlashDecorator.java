package com.ericsson.fdp.business.decorator.impl;

import java.util.List;

import com.ericsson.fdp.business.decorator.FDPDecorator;
import com.ericsson.fdp.business.display.impl.DisplayObjectImpl;
import com.ericsson.fdp.business.enums.DisplayArea;
import com.ericsson.fdp.common.constants.FDPConstant;
import com.ericsson.fdp.core.cache.AppCacheSubStore;
import com.ericsson.fdp.core.display.DisplayObject;
import com.ericsson.fdp.core.exception.ExecutionFailedException;
import com.ericsson.fdp.core.node.FDPNode;
import com.ericsson.fdp.core.request.FDPRequest;
import com.ericsson.fdp.core.serviceprovisioning.bean.FDPAppBag;
import com.ericsson.fdp.core.utils.ApplicationConfigUtil;

/**
 * This class is a decorator to be used to decorate the text with a new line
 * character at the end.
 * 
 * @author Ericsson
 * 
 */
public class BackSlashDecorator implements FDPDecorator {

	/**
	 * The decorator which is further to be decorated with this decorator.
	 */
	private final FDPDecorator fdpDecorator;
	
	private final FDPRequest fdpRequest;

	/**
	 * The constructor.
	 * 
	 * @param fdpDecoratorToSet
	 *            The fdp decorator to be used.
	 */
	public BackSlashDecorator(final FDPDecorator fdpDecoratorToSet, final FDPRequest fdpRequest) {
		this.fdpDecorator = fdpDecoratorToSet;
		this.fdpRequest = fdpRequest;
	}

	@Override
	public DisplayObject display() throws ExecutionFailedException {
		final DisplayObject displayObject = fdpDecorator.display();
		if (displayObject instanceof DisplayObjectImpl) {
			final DisplayObjectImpl displayObjectImpl = (DisplayObjectImpl) displayObject;
			displayObjectImpl.setCurrDisplayText(generateDisplayText(displayObject.getNodesToDisplay(), fdpRequest),
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
	 * @throws ExecutionFailedException
	 */
	private String generateDisplayText(final List<FDPNode> nodes, final FDPRequest fdpRequest) throws ExecutionFailedException {
		final StringBuffer displayText = new StringBuffer();
		int nodeCount = 1;
		for (final FDPNode node : nodes) {
			displayText
					.append(nodeCount)
					.append((String) ApplicationConfigUtil.getApplicationConfigCache().getValue(
							new FDPAppBag(AppCacheSubStore.CONFIGURATION_MAP, FDPConstant.USSD_MENU_SEPARATOR)))
					//.append(node.getDisplayName()).append(FDPConstant.NEWLINE);
					  .append(node.getOtherLanguageText(fdpRequest)).append(FDPConstant.NEWLINE);
			nodeCount++;
		}
		return displayText.toString();
	}
}
