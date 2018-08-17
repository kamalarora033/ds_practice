package com.ericsson.fdp.business.response.fulfillment.xml;

import java.io.Serializable;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement(name = "productBuy")
@XmlAccessorType(XmlAccessType.FIELD)
public class ProductBuy implements Serializable {

	/**
	 * The class serial version UID
	 */
	private static final long serialVersionUID = 1074776404803267131L;

	@XmlElement(name = "productId")
	private Long productId;

	@XmlElement(name = "productName")
	private String productName;

	@XmlElement(name = "productType")
	private String productType;

	@XmlElement(name = "amtCharged")
	private Integer amtCharged;
	
	@XmlElement(name = "amtRefunded")
	private String amtRefunded;
	
	@XmlElement(name="msisdn")
	private String msisdn;

	@XmlElement(name="beneficiaryMsisdn")
	private String beneficiaryMsisdn;
	
	@XmlElement(name="transfer")
	private Integer transfer;

	@XmlElement(name = "notification")
	private String notification;

	@XmlElement(name="referenceTxnId")
	private String referenceTxnId;
	
	@XmlElement(name="errorDescription")
	private String errorDescription;
	
	@XmlElement(name="offerDetails")
	private AccountAfterRefillDetails accountAfterRefill;
	
	@XmlElement(name = "products")
	private Products products;
	
	@XmlElement(name = "products")
	private SubscribedProducts subscribedProducts;
	
	public SubscribedProducts getSubscribedProducts() {
		return subscribedProducts;
	}

	public void setSubscribedProducts(SubscribedProducts subscribedProducts) {
		this.subscribedProducts = subscribedProducts;
	}

	public Products getProducts() {
		return products;
	}

	public void setProducts(Products products) {
		this.products = products;
	}
	
	public Long getProductId() {
		return productId;
	}

	public void setProductId(Long productId) {
		this.productId = productId;
	}

	public String getProductName() {
		return productName;
	}

	public void setProductName(String productName) {
		this.productName = productName;
	}

	public String getProductType() {
		return productType;
	}

	public void setProductType(String productType) {
		this.productType = productType;
	}

	public Integer getAmtCharged() {
		return amtCharged;
	}

	public void setAmtCharged(Integer amtCharged) {
		this.amtCharged = amtCharged;
	}

	public String getAmtRefunded() {
		return amtRefunded;
	}

	public void setAmtRefunded(String amtRefunded) {
		this.amtRefunded = amtRefunded;
	}

	public String getMsisdn() {
		return msisdn;
	}

	public void setMsisdn(String msisdn) {
		this.msisdn = msisdn;
	}

	public String getBeneficiaryMsisdn() {
		return beneficiaryMsisdn;
	}

	public void setBeneficiaryMsisdn(String beneficiaryMsisdn) {
		this.beneficiaryMsisdn = beneficiaryMsisdn;
	}

	public Integer getTransfer() {
		return transfer;
	}

	public void setTransfer(Integer transfer) {
		this.transfer = transfer;
	}
	
	public String getNotification() {
		return notification;
	}

	public void setNotification(String notification) {
		this.notification = notification;
	}

	public String getReferenceTxnId() {
		return referenceTxnId;
	}

	public void setReferenceTxnId(String referenceTxnId) {
		this.referenceTxnId = referenceTxnId;
	}

	@Override
	public String toString() {
		return "ProductBuy [productId=" + productId + ", productName="
				+ productName + ", productType=" + productType
				+ ", amtCharged=" + amtCharged + ", notification="
				+ notification + ", referenceTxnId=" + referenceTxnId + "]";
	}

	public AccountAfterRefillDetails getAccountAfterRefill() {
		return accountAfterRefill;
	}

	public void setAccountAfterRefill(AccountAfterRefillDetails accountAfterRefill) {
		this.accountAfterRefill = accountAfterRefill;
	}

	public String getErrorDescription() {
		return errorDescription;
	}

	public void setErrorDescription(String errorDescription) {
		this.errorDescription = errorDescription;
	}
	
	
}
