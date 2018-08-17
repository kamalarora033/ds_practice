package com.ericsson.fdp.business.charging.impl;

/**
 * @author esiasan
 * 
 */
public class Offer {

	private String offerId;
	private String usageCounterId;
	private String usageThresholdId;
	private String globalUCUTId;
	private Boolean isExpired;

	public Offer() {
	}

	public Offer(String offerId, String usageCounterId,
			String usageThresholdId, String globalUCUTId) {
		this.offerId = offerId;
		this.usageCounterId = usageCounterId;
		this.usageThresholdId = usageThresholdId;
		this.globalUCUTId = globalUCUTId;
	}

	public String getOfferId() {
		return offerId;
	}

	public void setOfferId(String offerId) {
		this.offerId = offerId;
	}

	public Boolean isExpired() {
		return isExpired;
	}

	public void setExpired(Boolean isExpired) {
		this.isExpired = isExpired;
	}

	public String getUsageCounterId() {
		return usageCounterId;
	}

	public void setUsageCounterId(String usageCounterId) {
		this.usageCounterId = usageCounterId;
	}

	public String getUsageThresholdId() {
		return usageThresholdId;
	}

	public void setUsageThresholdId(String usageThresholdId) {
		this.usageThresholdId = usageThresholdId;
	}

	public String getGlobalUCUTId() {
		return globalUCUTId;
	}

	public void setGlobalUCUTId(String globalUCUTId) {
		this.globalUCUTId = globalUCUTId;
	}

}
