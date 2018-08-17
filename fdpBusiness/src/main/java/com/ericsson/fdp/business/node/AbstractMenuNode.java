package com.ericsson.fdp.business.node;

import java.util.List;
import java.util.Map;

import com.ericsson.fdp.business.enums.DynamicMenuPaginationKey;

/**
 * This class stores the common variables that are shared across the menu nodes.
 * 
 * @author Ericsson
 * 
 */
public abstract class AbstractMenuNode extends AbstractNode {

	/**
	 * The serial version id.
	 */
	private static final long serialVersionUID = 1239926279841631245L;

	/**
	 * This variable is used to store the marketing message for the node.
	 */
	private String marketingMessage;

	/**
	 * This is used to check if the marketing message is to be concatenated or
	 * used separately.
	 */
	private boolean concatenateMarketingMessage;

	/**
	 * The header to be used.
	 */
	private String header;
	// Add for French support on 19/12/16
	private Map<Integer,String> otherLangHeader;
	/**
	 * Gets the marketing message.
	 *
	 * Gets the marketing message.
	 *
	 * @return the marketingMessage
	 */
	public String getMarketingMessage() {
		return marketingMessage;
	}

	/**
	 * Sets the marketing message.
	 *
	 * @param marketingMessage the marketingMessage to set
	 */
	public void setMarketingMessage(String marketingMessage) {
		this.marketingMessage = marketingMessage;
	}

	/**
	 * Checks if is concatenate marketing message.
	 *
	 * @return the concatenateMarketingMessage
	 */
	public boolean isConcatenateMarketingMessage() {
		return concatenateMarketingMessage;
	}

	/**
	 * Sets the concatenate marketing message.
	 *
	 * @param concatenateMarketingMessage the concatenateMarketingMessage to set
	 */
	public void setConcatenateMarketingMessage(boolean concatenateMarketingMessage) {
		this.concatenateMarketingMessage = concatenateMarketingMessage;
	}

	/**
	 * Gets the header.
	 *
	 * @return the header
	 */
	public String getHeader() {
		return header;
	}

	/**
	 * Sets the header.
	 *
	 * @param header the header to set
	 */
	public void setHeader(String header) {
		this.header = header;
	}
	
	/**
	 * @return the otherLangHeader
	 */
	public Map<Integer, String> getOtherLangHeader() {
		return otherLangHeader;
	}

	/**
	 * @param otherLangHeader the otherLangHeader to set
	 */
	public void setOtherLangHeader(Map<Integer, String> otherLangHeader) {
		this.otherLangHeader = otherLangHeader;
	}

}
