package com.ericsson.fdp.business.response.fulfillment.xml;

import java.io.Serializable;
import java.util.List;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement(name = "products")
@XmlAccessorType(XmlAccessType.FIELD)
public class Products implements Serializable{

	/**
	 * 
	 */
	private static final long serialVersionUID = 2131961940568477280L;
	
	@XmlElement(name="productDetails")
	private List<ViewProduct> productDetails;

	public List<ViewProduct> getProductDetails() {
		return productDetails;
	}

	public void setProductDetails(List<ViewProduct> productDetails) {
		this.productDetails = productDetails;
	}

}
