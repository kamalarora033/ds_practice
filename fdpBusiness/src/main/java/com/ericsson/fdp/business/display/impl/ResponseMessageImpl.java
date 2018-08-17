package com.ericsson.fdp.business.display.impl;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.ericsson.fdp.business.enums.DisplayArea;
import com.ericsson.fdp.business.enums.TLVOptions;
import com.ericsson.fdp.common.constants.FDPConstant;
import com.ericsson.fdp.common.enums.ChannelType;
import com.ericsson.fdp.core.display.ResponseMessage;

/**
 * The message interface implementation.
 * 
 * @author Ericsson
 * 
 */
public class ResponseMessageImpl implements ResponseMessage {

	/**
	 * 
	 */
	private static final long serialVersionUID = -7193111282850673587L;

	/**
	 * The default number of display areas.
	 */
	private static final Integer DEFAULT_DISPLAY_AREAS = 4;

	/**
	 * The channel on which this message is to be sent.
	 */
	private ChannelType channelType;

	/**
	 * The delay in the message.
	 */
	private Long delay;

	/**
	 * The priority for the message.
	 */
	private Long priority;

	/**
	 * The tlv options.
	 */
	private List<TLVOptions> tlvOptions;

	/**
	 * The display text currently to be displayed to the user.
	 */
	private Map<DisplayArea, String> currDisplayText = new HashMap<DisplayArea, String>(DEFAULT_DISPLAY_AREAS);

	/**
	 * The constructor.
	 * 
	 * @param channelType
	 *            the channel type.
	 * @param delay
	 *            the delay.
	 * @param tlvOptions
	 *            the tlv options.
	 */
	public ResponseMessageImpl(final ChannelType channelType, final Long delay, final List<TLVOptions> tlvOptions) {
		this.channelType = channelType;
		this.delay = delay;
		this.tlvOptions = tlvOptions;
	}

	/**
	 * The constructor.
	 * 
	 * @param channelType
	 *            the channel type.
	 * @param delay
	 *            the delay.
	 * @param tlvOptions
	 *            the tlv options.
	 */
	public ResponseMessageImpl(final ChannelType channelType, final Long delay, final List<TLVOptions> tlvOptions,
			final String displayText) {
		this(channelType, delay, tlvOptions);
		this.currDisplayText.put(DisplayArea.COMPLETE, displayText);
	}

	@Override
	public ChannelType getChannelForMessage() {
		return channelType;
	}

	@Override
	public String getCurrDisplayText(final DisplayArea displayArea) {
		String displayText = (currDisplayText.get(displayArea) == null) ? FDPConstant.EMPTY_STRING : currDisplayText
				.get(displayArea);
		if (currDisplayText.get(displayArea) == null && DisplayArea.COMPLETE.equals(displayArea)) {
			displayText = getCurrDisplayText(DisplayArea.HEADER) + getCurrDisplayText(DisplayArea.MIDDLE)
					+ getCurrDisplayText(DisplayArea.FOOTER);
		}
		return displayText;
	}

	@Override
	public Long getDelayForMessage() {
		return delay;
	}

	@Override
	public Long getPriorityForMessage() {
		return priority;
	}

	@Override
	public List<TLVOptions> getTLVOptions() {
		return tlvOptions;
	}

	/**
	 * @param currDisplayTextToSet
	 *            the currDisplayText to set
	 */
	public void setCurrDisplayText(final String currDisplayTextToSet, final DisplayArea displayArea) {
		this.currDisplayText.put(displayArea, currDisplayTextToSet);
	}

	/**
	 * @param channelType
	 *            the channelType to set
	 */
	public void setChannelType(final ChannelType channelType) {
		this.channelType = channelType;
	}

	/**
	 * @param delay
	 *            the delay to set
	 */
	public void setDelay(final Long delay) {
		this.delay = delay;
	}

	/**
	 * @param priority
	 *            the priority to set
	 */
	public void setPriority(final Long priority) {
		this.priority = priority;
	}

	/**
	 * @param tlvOptions
	 *            the tlvOptions to set
	 */
	public void setTlvOptions(final List<TLVOptions> tlvOptions) {
		this.tlvOptions = tlvOptions;
	}

	/**
	 * This method is used to add the tlv option.
	 * 
	 * @param tlvOption
	 *            the tlv option to add.
	 */
	public void addTLVOption(TLVOptions tlvOption) {
		if (this.tlvOptions == null) {
			this.tlvOptions = new ArrayList<TLVOptions>();
		}
		this.tlvOptions.add(tlvOption);
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return "ResponseMessageImpl [channelType=" + channelType + ", delay=" + delay + ", priority=" + priority
				+ ", tlvOptions=" + tlvOptions + ", currDisplayText=" + currDisplayText +  "Complete display :-" + getCurrDisplayText(DisplayArea.COMPLETE) + "]";
	}

}
