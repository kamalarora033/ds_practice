package com.ericsson.fdp.business.response.fulfillment.xml;

import java.io.Serializable;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement(name = "offer")
@XmlAccessorType(XmlAccessType.FIELD)
public class Offer implements Serializable{

	private static final long serialVersionUID = -8918157017225481936L;

	@XmlElement(name = "offerId")
	private String offerId;
	
	@XmlElement(name = "offerStartDate")
	private String offerStartDate;
	
	@XmlElement(name = "offerExpiryDate")
	private String offerExpiryDate;
	
	public Offer(){}
	
	public Offer(String offerId, String offerStartDate, String offerExpiryDate){
		this.offerId = offerId;
		this.offerStartDate = offerStartDate;
		this.offerExpiryDate = offerExpiryDate;
	}

	public String getOfferId() {
		return offerId;
	}

	public void setOfferId(String offerId) {
		this.offerId = offerId;
	}

	public String getOfferStartDate() {
		return offerStartDate;
	}

	public void setOfferStartDate(String offerStartDate) {
		this.offerStartDate = offerStartDate;
	}

	public String getOfferExpiryDate() {
		return offerExpiryDate;
	}

	public void setOfferExpiryDate(String offerExpiryDate) {
		this.offerExpiryDate = offerExpiryDate;
	}
	
}
