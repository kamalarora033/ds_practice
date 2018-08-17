package com.ericsson.fdp.business.response.fulfillment.xml;

import java.io.Serializable;
import java.util.List;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement(name = "products")
@XmlAccessorType(XmlAccessType.FIELD)
public class SubscribedProducts implements Serializable {

	private static final long serialVersionUID = 3717565693825212518L;
	
	@XmlElement(name = "productDetails")
	private List<ProductDetails> productDetails;

	public SubscribedProducts() {  }
	
	public SubscribedProducts(List<ProductDetails> productDetails){
		this.productDetails = productDetails;
	}
	
	public List<ProductDetails> getProductDetails() {
		return productDetails;
	}

	public void setProductDetails(List<ProductDetails> productDetails) {
		this.productDetails = productDetails;
	}
	
	@Override
	public String toString() {
		return "product [productDetail=" + productDetails.toString() + "]";
	}

}