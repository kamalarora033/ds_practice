package com.ericsson.fdp.business.decorator.impl;

import com.ericsson.fdp.business.decorator.FDPDecorator;
import com.ericsson.fdp.business.display.impl.DisplayObjectImpl;
import com.ericsson.fdp.business.enums.DisplayArea;
import com.ericsson.fdp.business.node.AbstractMenuNode;
import com.ericsson.fdp.common.constants.FDPConstant;
import com.ericsson.fdp.core.display.DisplayObject;
import com.ericsson.fdp.core.exception.ExecutionFailedException;
import com.ericsson.fdp.core.node.FDPNode;
import com.ericsson.fdp.dao.enums.LanguageType;

/**
 * This class is a decorator to be used to decorate the text with a new line
 * character at the end.
 * 
 * @author Ericsson
 * 
 */
public class HeaderDecorator implements FDPDecorator {

	/**
	 * The decorator which is further to be decorated with this decorator.
	 */
	private final FDPDecorator fdpDecorator;

	/**
	 * The current node.
	 */
	private final FDPNode currentNode;
	
	private final LanguageType languageType;

	/**
	 * The constructor.
	 * 
	 * @param fdpDecoratorToSet
	 *            The fdp decorator to be used.
	 * @param fdpNode
	 *            the current node.
	 */
	public HeaderDecorator(final FDPDecorator fdpDecoratorToSet, final FDPNode fdpNode, final LanguageType languageType) {
		this.fdpDecorator = fdpDecoratorToSet;
		this.currentNode = fdpNode;
		this.languageType = languageType;
	}

	@Override
	public DisplayObject display() throws ExecutionFailedException {
		final DisplayObject displayObject = fdpDecorator.display();
		if (displayObject instanceof DisplayObjectImpl) {
			final DisplayObjectImpl displayObjectImpl = (DisplayObjectImpl) displayObject;
			displayObjectImpl.setCurrDisplayText(
					generateDisplayText(currentNode, displayObject.getCurrDisplayText(DisplayArea.HEADER),languageType),
					DisplayArea.HEADER);
		}
		return displayObject;
	}

	/**
	 * This method is used to generate the header text.
	 * 
	 * @param currentNode
	 *            the current node for which the header text is to be generated.
	 * @param currentDisplayText
	 *            the current header text.
	 * @return the text.
	 */
	private String generateDisplayText(final FDPNode currentNode, final String currentDisplayText, LanguageType languageType) {
		String headerText = currentDisplayText;
		if (currentNode instanceof AbstractMenuNode) {
			final AbstractMenuNode abstractMenuNode = (AbstractMenuNode) currentNode;
			final StringBuilder headerTextBuffer = new StringBuilder();
			headerTextBuffer.append(currentDisplayText);
			if (abstractMenuNode.isConcatenateMarketingMessage() && abstractMenuNode.getMarketingMessage() != null) {
				headerTextBuffer.append(abstractMenuNode.getMarketingMessage()).append(FDPConstant.NEWLINE);
			}
			if (abstractMenuNode.getHeader() != null && !abstractMenuNode.getHeader().isEmpty()) {
				StringBuilder header = getHeaderText(abstractMenuNode,languageType);
				headerTextBuffer.append(header.append(FDPConstant.NEWLINE));
			}
			headerText = headerTextBuffer.toString();
		}
		return headerText;
	}

	//Add for French support on 19/12/16
	private StringBuilder getHeaderText(AbstractMenuNode abstractMenuNode, LanguageType simLang) {
		StringBuilder headerTextBuffer = new StringBuilder();
		if(null != simLang){
			switch(simLang){
			case ENGLISH : if (abstractMenuNode.getHeader() != null && !abstractMenuNode.getHeader().isEmpty())
				headerTextBuffer.append(abstractMenuNode.getHeader());

			break;
			case FRENCH :  if (abstractMenuNode.getOtherLangHeader() != null && !abstractMenuNode.getOtherLangHeader().isEmpty())
				headerTextBuffer.append(abstractMenuNode.getOtherLangHeader().get(LanguageType.FRENCH.getValue()));	
			break;
			}
		}
		return headerTextBuffer;
	}
}
