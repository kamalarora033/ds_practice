package com.ericsson.fdp.business.decorator.impl;

import java.util.Collections;
import java.util.List;
import java.util.Map;

import com.ericsson.fdp.business.comparator.FooterPriorityComparator;
import com.ericsson.fdp.business.decorator.FDPDecorator;
import com.ericsson.fdp.business.display.impl.DisplayObjectImpl;
import com.ericsson.fdp.business.enums.ConfigurationKey;
import com.ericsson.fdp.business.enums.DisplayArea;
import com.ericsson.fdp.business.enums.DynamicMenuPaginationKey;
import com.ericsson.fdp.business.enums.TLVOptions;
import com.ericsson.fdp.business.node.AbstractNode;
import com.ericsson.fdp.business.util.PaginationUtil;
import com.ericsson.fdp.common.enums.ChannelType;
import com.ericsson.fdp.common.vo.FDPCircle;
import com.ericsson.fdp.core.display.DisplayObject;
import com.ericsson.fdp.core.exception.ExecutionFailedException;
import com.ericsson.fdp.core.node.FDPNode;

/**
 * This class is a decorator to be used to decorate the text with a new line
 * character at the end.
 * 
 * @author Ericsson
 * 
 */
public class FooterDecorator implements FDPDecorator {

	/**
	 * The decorator which is further to be decorated with this decorator.
	 */
	private final FDPDecorator fdpDecorator;

	/**
	 * The current node.
	 */
	private final FDPNode currentNode;

	/**
	 * The circle for which this decorator will work.
	 */
	private final FDPCircle fdpCircle;

	/**
	 * The channel on which this decorator will work.
	 */
	private final ChannelType channel;

	/**
	 * The constructor.
	 * 
	 * @param fdpDecoratorToSet
	 *            The fdp decorator to be used.
	 * @param fdpNode
	 *            the current node.
	 * @param fdpCircle
	 *            the fdp circle.
	 * @param channel
	 *            the channel on which this decorator will work.
	 */
	public FooterDecorator(final FDPDecorator fdpDecoratorToSet, final FDPNode fdpNode, final FDPCircle fdpCircle,
			final ChannelType channel) {
		this.fdpDecorator = fdpDecoratorToSet;
		this.currentNode = fdpNode;
		this.fdpCircle = fdpCircle;
		this.channel = channel;
	}

	@Override
	public DisplayObject display() throws ExecutionFailedException {
		final DisplayObject displayObject = fdpDecorator.display();
		if (displayObject instanceof DisplayObjectImpl) {
			final DisplayObjectImpl displayObjectImpl = (DisplayObjectImpl) displayObject;
			displayObjectImpl.setCurrDisplayText(
					generateDisplayText(displayObject.getCurrDisplayText(DisplayArea.FOOTER)), DisplayArea.FOOTER);
			updateTLVs(displayObject);
		}
		return displayObject;
	}

	private void updateTLVs(final DisplayObject displayObject) {
		final List<TLVOptions> tlvOptions = displayObject.getResponseMessage().getTLVOptions();
		if (!displayObject.getCurrDisplayText(DisplayArea.FOOTER).isEmpty()) {
			tlvOptions.add(TLVOptions.SESSION_CONTINUE);
			tlvOptions.remove(TLVOptions.SESSION_TERMINATE);
		}
	}

	/**
	 * This method is used to generate the footer text.
	 * 
	 * @param currentDisplayText
	 *            the current header text.
	 * @return the text.
	 */
	private String generateDisplayText(final String currentDisplayText) {
		String footerText = currentDisplayText;
		if (currentNode instanceof AbstractNode) {
			final AbstractNode abstractMenuNode = (AbstractNode) currentNode;
			final StringBuffer headerTextBuffer = new StringBuffer();
			headerTextBuffer.append(currentDisplayText);
			if (abstractMenuNode.getPaginationOptions() != null && !abstractMenuNode.getPaginationOptions().isEmpty()) {
				final List<DynamicMenuPaginationKey> footers = abstractMenuNode.getPaginationOptions();
				Collections.sort(footers, new FooterPriorityComparator());
				boolean isFirst = true;
				for (final DynamicMenuPaginationKey dynamicMenuPaginationKey : footers) {
					headerTextBuffer.append(getPaginationString(dynamicMenuPaginationKey, !isFirst));
					isFirst = false;
				}
			}
			footerText = headerTextBuffer.toString();
		}
		return footerText;
	}

	/**
	 * This method is used to get the pagination text.
	 * 
	 * @param dynamicMenuPaginationKey
	 *            the pagination key.
	 * @param addSeperatorFirst
	 *            true if seperator is to be added first.
	 * @return the text formed.
	 */
	private String getPaginationString(final DynamicMenuPaginationKey dynamicMenuPaginationKey,
			final boolean addSeperatorFirst) {
		final Map<String, String> circleConfig = fdpCircle.getConfigurationKeyValueMap();
		final StringBuffer stringBuffer = new StringBuffer();
		if (addSeperatorFirst) {
			stringBuffer.append(PaginationUtil.getSeperator(fdpCircle));
		}
		switch (channel) {
		case USSD:
			stringBuffer.append(circleConfig.get(dynamicMenuPaginationKey.getConfigurationKeyForUSSD().name()))
					.append(circleConfig.get(ConfigurationKey.FOOTER_CODE_TEXT_SEPERATOR.name()))
					.append(circleConfig.get(dynamicMenuPaginationKey.getConfigurationTextKeyForUSSD().name()));
			break;
		case SMS:
			stringBuffer.append(circleConfig.get(dynamicMenuPaginationKey.getConfigurationKeyForSMS().name()))
					.append(circleConfig.get(ConfigurationKey.FOOTER_CODE_TEXT_SEPERATOR.name()))
					.append(circleConfig.get(dynamicMenuPaginationKey.getConfigurationTextKeyForSMS().name()));
			break;
		default:
			break;
		}

		return stringBuffer.toString();
	}
}
