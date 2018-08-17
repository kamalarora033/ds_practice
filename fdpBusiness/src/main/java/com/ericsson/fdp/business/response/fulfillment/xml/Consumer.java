package com.ericsson.fdp.business.response.fulfillment.xml;

import java.io.Serializable;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

/**
 * This is the JAXB object for consumer tag in response xml.
 * 
 * @author Ericsson
 * 
 */
@XmlRootElement(name="consumer")
@XmlAccessorType(XmlAccessType.FIELD)
public class Consumer implements Serializable{

	/**
	 * The class serial version UID
	 */
	private static final long serialVersionUID = -6820880858380858236L;

	@XmlElement(name="msisdn")
	private String msisdn;
	
	@XmlElement(name="usage")
	private String usage;
	
	@XmlElement(name="threshold")
	private String threshold;
	
	@XmlElement(name="unit")
	private String unit;

	/**
	 * @return the msisdn
	 */
	public String getMsisdn() {
		return msisdn;
	}

	/**
	 * @param msisdn the msisdn to set
	 */
	public void setMsisdn(String msisdn) {
		this.msisdn = msisdn;
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
}
