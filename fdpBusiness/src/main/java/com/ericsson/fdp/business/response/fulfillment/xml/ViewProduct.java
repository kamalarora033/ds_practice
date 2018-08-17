package com.ericsson.fdp.business.response.fulfillment.xml;

import java.io.Serializable;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement(name = "productDetails")
@XmlAccessorType(XmlAccessType.FIELD)
public class ViewProduct implements Serializable{

	/**
	 * 
	 */
	private static final long serialVersionUID = 2131961940568479320L;
	
	@XmlElement(name="productId")
	private String productId;
	
	@XmlElement(name="productName")
	private String productName;
	
	@XmlElement(name="price")
	private String price;
	
	@XmlElement(name="paymentMode")
	private String paymentMode;
	
	@XmlElement(name="srcChannel")
	private String channel;
	
	@XmlElement(name="activationDate")
	private String activationDate;

	@XmlElement(name="expiryDate")
	private String expiryDate;
	
	@XmlElement(name="activatedBy")
	private String activatedBy;

	@XmlElement(name="availableBalance")
	private String availableBalance;
	
	@XmlElement(name="productCode")
	private String productCode;
	
	
	public String getProductCode() {
		return productCode;
	}

	public void setProductCode(String productCode) {
		this.productCode = productCode;
	}

	public String getProductName() {
		return productName;
	}

	public void setProductName(String productName) {
		this.productName = productName;
	}

	public String getPrice() {
		return price;
	}

	public void setPrice(String price) {
		this.price = price;
	}

	public String getPaymentMode() {
		return paymentMode;
	}

	public void setPaymentMode(String paymentMode) {
		this.paymentMode = paymentMode;
	}

	public String getChannel() {
		return channel;
	}

	public void setChannel(String channel) {
		this.channel = channel;
	}

	public String getActivationDate() {
		return activationDate;
	}

	public void setActivationDate(String activationDate) {
		this.activationDate = activationDate;
	}

	public String getExpiryDate() {
		return expiryDate;
	}

	public void setExpiryDate(String expiryDate) {
		this.expiryDate = expiryDate;
	}

	public String getActivatedBy() {
		return activatedBy;
	}

	public void setActivatedBy(String activatedBy) {
		this.activatedBy = activatedBy;
	}
	
	public String getAvailableBalance() {
		return availableBalance;
	}

	public void setAvailableBalance(String availableBalance) {
		this.availableBalance = availableBalance;
	}
	
	public String getProductId() {
		return productId;
	}

	public void setProductId(String productId) {
		this.productId = productId;
	}

}
