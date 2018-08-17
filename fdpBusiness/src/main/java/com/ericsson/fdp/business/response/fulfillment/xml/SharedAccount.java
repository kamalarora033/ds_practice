package com.ericsson.fdp.business.response.fulfillment.xml;

import java.io.Serializable;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

/**
 * This is the JAXB object for Shared Account Tag.
 * 
 * @author Ericsson
 * 
 */
@XmlRootElement(name="sharedAccount")
@XmlAccessorType(XmlAccessType.FIELD)
public class SharedAccount implements Serializable{

	/**
	 * The class serial version UID
	 */
	private static final long serialVersionUID = -21704133197356243L;
	
	@XmlElement(name="notificationText", required=true)
	private String notificationText;

	@XmlElement(name="sharedRID")
	private String sharedRID;
	
	@XmlElement(name="usage")
	private String usage;
	
	@XmlElement(name="threshold")
	private String threshold;
	
	@XmlElement(name="unit")
	private String unit;
	
	@XmlElement(name="consumers")
	private Consumers consumers;
	
	@XmlElement(name="providers")
	private Providers providers;
	
	/**
	 * @return the notificationText
	 */
	public String getNotificationText() {
		return notificationText;
	}

	/**
	 * @param notificationText the notificationText to set
	 */
	public void setNotificationText(String notificationText) {
		this.notificationText = notificationText;
	}

	/**
	 * @return the sharedRID
	 */
	public String getSharedRID() {
		return sharedRID;
	}

	/**
	 * @param sharedRID the sharedRID to set
	 */
	public void setSharedRID(String sharedRID) {
		this.sharedRID = sharedRID;
	}

	/**
	 * @return the usage
	 */
	public String getUsage() {
		return usage;
	}

	/**
	 * @param usage the usage to set
	 */
	public void setUsage(String usage) {
		this.usage = usage;
	}

	/**
	 * @return the threshold
	 */
	public String getThreshold() {
		return threshold;
	}

	/**
	 * @param threshold the threshold to set
	 */
	public void setThreshold(String threshold) {
		this.threshold = threshold;
	}

	/**
	 * @return the unit
	 */
	public String getUnit() {
		return unit;
	}

	/**
	 * @param unit the unit to set
	 */
	public void setUnit(String unit) {
		this.unit = unit;
	}

	/**
	 * @return the consumers
	 */
	public Consumers getConsumers() {
		return consumers;
	}

	/**
	 * @param consumers the consumers to set
	 */
	public void setConsumers(Consumers consumers) {
		this.consumers = consumers;
	}

	/**
	 * @return the providers
	 */
	public Providers getProducers() {
		return providers;
	}

	/**
	 * @param providers the providers to set
	 */
	public void setProducers(Providers providers) {
		this.providers = providers;
	}
}
