package com.ericsson.fdp.business.response.fulfillment.xml;

import java.io.Serializable;
import java.util.List;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement(name = "productDetails")
@XmlAccessorType(XmlAccessType.FIELD)
public class ProductDetails implements Serializable{

	private static final long serialVersionUID = 4712909356692421070L;

	@XmlElement(name = "productName")
	private String productName;
	
	@XmlElement(name = "offerDetails")
	private List<OfferDetails> offerDetails;
	
	@XmlElement(name = "daDetails")
	private List<DaDetails> daDetails;
	
	public ProductDetails(){}
	
	ProductDetails(String productName, List<OfferDetails> offerDetails,
			List<DaDetails> daDetails){
		this.productName = productName;
		this.offerDetails = offerDetails;
		this.daDetails = daDetails;
	}

	public String getProductName() {
		return productName;
	}

	public void setProductName(String productName) {
		this.productName = productName;
	}

	public List<OfferDetails> getOfferDetails() {
		return offerDetails;
	}

	public void setOfferDetails(List<OfferDetails> offerDetails) {
		this.offerDetails = offerDetails;
	}

	public List<DaDetails> getDaDetails() {
		return daDetails;
	}

	public void setDaDetails(List<DaDetails> daDetails) {
		this.daDetails = daDetails;
	}
	
}
