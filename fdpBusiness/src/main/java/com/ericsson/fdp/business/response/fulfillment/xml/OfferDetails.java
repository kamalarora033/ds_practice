package com.ericsson.fdp.business.response.fulfillment.xml;

import java.io.Serializable;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement(name = "offerDetails")
@XmlAccessorType(XmlAccessType.FIELD)
public class OfferDetails  implements Serializable{
	
	private static final long serialVersionUID = 4923310164070890293L;

	@XmlElement(name = "offerId")
	private String offerId;
	
	@XmlElement(name = "expiryDate")
	private String expiryDate;

	public String getOfferId() {
		return offerId;
	}

	public void setOfferId(String offerId) {
		this.offerId = offerId;
	}

	public String getExpiryDate() {
		return expiryDate;
	}

	public void setExpiryDate(String expiryDate) {
		this.expiryDate = expiryDate;
	}
	
}
