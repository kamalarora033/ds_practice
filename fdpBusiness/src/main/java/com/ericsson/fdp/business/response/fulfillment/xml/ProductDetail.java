package com.ericsson.fdp.business.response.fulfillment.xml;

import java.io.Serializable;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import com.ericsson.fdp.common.enums.ProductType;

@XmlRootElement(name = "ProductDetail")
@XmlAccessorType(XmlAccessType.FIELD)
public class ProductDetail implements Serializable{

	private static final long serialVersionUID = 2131961940568479000L;

	@XmlElement(name="Id")
	private Long Id;
	
	@XmlElement(name="Type")
	private ProductType type;
		
	@XmlElement(name="Name")
	private String name;
	
	@XmlElement(name="Validity")
	private String validity;
	
	@XmlElement(name="BuyForOther")
	private String buyForOther;
	
	@XmlElement(name="amount")
	private Long amount;
	
	/**
	 * @return the id
	 */
	public Long getId() {
		return Id;
	}

	/**
	 * @param id the id to set
	 */
	public void setId(Long id) {
		Id = id;
	}

	/**
	 * @return the type
	 */
	public ProductType getType() {
		return type;
	}

	/**
	 * @param productType the type to set
	 */
	public void setType(ProductType productType) {
		this.type = productType;
	}

	/**
	 * @return the validity
	 */
	public String getValidity() {
		return validity;
	}

	/**
	 * @param validity the validity to set
	 */
	public void setValidity(String validity) {
		this.validity = validity;
	}

	/**
	 * @return the buyForOther
	 */
	public String getBuyForOther() {
		return buyForOther;
	}

	/**
	 * @param buyForOther the buyForOther to set
	 */
	public void setBuyForOther(String buyForOther) {
		this.buyForOther = buyForOther;
	}

	/**
	 * @return the amount
	 */
	public Long getamount() {
		return amount;
	}

	/**
	 * @param amountCharged the amount to set
	 */
	public void setamount(Long amountCharged) {
		this.amount = amountCharged;
	}

	/**
	 * @return the name
	 */
	public String getName() {
		return name;
	}

	/**
	 * @param name the name to set
	 */
	public void setName(String name) {
		this.name = name;
	}
}

