package com.ericsson.fdp.business.response.fulfillment.xml;

import java.io.Serializable;
import java.util.List;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement(name = "offerDetails")
@XmlAccessorType(XmlAccessType.FIELD)
public class AccountAfterRefillDetails implements Serializable{

	private static final long serialVersionUID = 2131961940568479000L;

	@XmlElement(name="offer")
	private List<Offer> offers;
	
	public AccountAfterRefillDetails(){}
	
	public AccountAfterRefillDetails(List<Offer> offers){
		this.offers = offers;
	}


	public List<Offer> getOffers() {
		return offers;
	}

	public void setOffers(List<Offer> offers) {
		this.offers = offers;
	}
	
}
