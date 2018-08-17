package com.ericsson.fdp.business.response.fulfillment.xml;

import java.io.Serializable;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement(name="provider")
@XmlAccessorType(XmlAccessType.FIELD)
public class Provider implements Serializable{

	/**
	 * The class seial version UID
	 */
	private static final long serialVersionUID = -3758290828357047137L;

	@XmlElement(name="msisdn")
	private String msisdn;
	
	@XmlElement(name="offerId")
	private String offerId;

	public String getMsisdn() {
		return msisdn;
	}

	public void setMsisdn(String msisdn) {
		this.msisdn = msisdn;
	}

	public String getOfferId() {
		return offerId;
	}

	public void setOfferId(String offerId) {
		this.offerId = offerId;
	}
}
