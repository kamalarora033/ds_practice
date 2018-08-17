package com.ericsson.fdp.business.decorator.impl;

import com.ericsson.fdp.business.decorator.FDPDecorator;
import com.ericsson.fdp.business.display.impl.DisplayObjectImpl;
import com.ericsson.fdp.business.display.impl.ResponseMessageImpl;
import com.ericsson.fdp.business.enums.ConfigurationKey;
import com.ericsson.fdp.business.enums.TLVOptions;
import com.ericsson.fdp.business.node.AbstractMenuNode;
import com.ericsson.fdp.business.util.ResponseUtil;
import com.ericsson.fdp.common.enums.ChannelType;
import com.ericsson.fdp.common.vo.FDPCircle;
import com.ericsson.fdp.core.display.DisplayObject;
import com.ericsson.fdp.core.exception.ExecutionFailedException;
import com.ericsson.fdp.core.node.FDPNode;

/**
 * This class is a decorator to be used to decorate the text which permits
 * pagination.
 * 
 * @author Ericsson
 * 
 */
public class FlashDecorator implements FDPDecorator {

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
	 * @param displayObject
	 *            The fdp decorator to be used.
	 * @param fdpNode
	 *            the current node.
	 * @param fdpCircle
	 *            the fdp circle.
	 * @param channel
	 *            the channel on which this decorator will work.
	 */
	public FlashDecorator(final FDPNode fdpNode, final FDPCircle fdpCircle, final ChannelType channel) {
		this.currentNode = fdpNode;
		this.fdpCircle = fdpCircle;
		this.channel = channel;
	}

	@Override
	public DisplayObject display() throws ExecutionFailedException {
		DisplayObject displayObjectCreated = null;
		if (currentNode instanceof AbstractMenuNode) {
			final AbstractMenuNode abstractMenuNode = (AbstractMenuNode) currentNode;
			if (!abstractMenuNode.isConcatenateMarketingMessage() && abstractMenuNode.getMarketingMessage() != null
					&& !abstractMenuNode.getMarketingMessage().isEmpty()) {
				displayObjectCreated = getDisplayObject(abstractMenuNode.getMarketingMessage());
			}
		}
		return displayObjectCreated;
	}

	/**
	 * This method creates the display object based on the marketing message.
	 * 
	 * @param marketingMessage
	 *            the marketing message.
	 * @return the marketing message.
	 */
	private DisplayObject getDisplayObject(final String marketingMessage) {
		final ResponseMessageImpl responseMessageImpl = (ResponseMessageImpl) ResponseUtil.createResponseMessage(
				channel, marketingMessage, TLVOptions.SESSION_TERMINATE);
		if (ChannelType.USSD.equals(channel)) {
			responseMessageImpl.addTLVOption(TLVOptions.FLASH);
			final String delay = fdpCircle.getConfigurationKeyValueMap().get(
					ConfigurationKey.FLASH_DELAY_FOR_USSD.getAttributeName());
			final Long delayAsLong = getDelay(delay);
			responseMessageImpl.setDelay(delayAsLong);
		}
		return new DisplayObjectImpl(currentNode, responseMessageImpl);
	}

	/**
	 * This method is used to get the delay value as long.
	 * 
	 * @param delay
	 *            the delay value.
	 * @return the delay value.
	 */
	private Long getDelay(final String delay) {
		Long delayAsLong = null;
		if (delay != null) {
			delayAsLong = Long.parseLong(delay);
		}
		return delayAsLong;
	}
}
