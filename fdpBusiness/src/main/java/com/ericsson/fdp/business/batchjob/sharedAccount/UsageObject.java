package com.ericsson.fdp.business.batchjob.sharedAccount;

/**
 * This class stores the usage value and corresponding notification string.
 * 
 * @author Ericsson
 * 
 */
public class UsageObject implements Comparable<UsageObject> {

	/** The notification string to be sent. */
	private String notificationString;

	/** The usage value */
	private Long usageValue;

	/**
	 * The constructor for usage object.
	 * 
	 * @param notificationString
	 *            the string to be used in notification.
	 * @param usageValue
	 *            the usage value to be used.
	 */
	public UsageObject(String notificationString, Long usageValue) {
		this.notificationString = notificationString;
		this.usageValue = usageValue;
	}

	/**
	 * @return the notificationString
	 */
	public String getNotificationString() {
		return notificationString;
	}

	/**
	 * @param notificationString
	 *            the notificationString to set
	 */
	public void setNotificationString(String notificationString) {
		this.notificationString = notificationString;
	}

	/**
	 * @return the usageValue
	 */
	public Long getUsageValue() {
		return usageValue;
	}

	/**
	 * @param usageValue
	 *            the usageValue to set
	 */
	public void setUsageValue(Long usageValue) {
		this.usageValue = usageValue;
	}

	@Override
	public int compareTo(UsageObject o) {
		return this.usageValue.compareTo(o.getUsageValue());
	}

}
