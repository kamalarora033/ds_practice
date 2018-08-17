package com.ericsson.fdp.business.response.fulfillment.xml;

import java.io.Serializable;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

/**
 * 
 * @author GUR36857
 *
 */
@XmlRootElement(name = "bundle")
@XmlAccessorType(XmlAccessType.FIELD)
public class Bundle implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	@XmlElement(name = "serviceName")
	private String serviceName;
	
	@XmlElement(name = "serviceDescription")
	private String serviceDescription;
	
	@XmlElement(name = "subscriptionDate")
	private String subscriptionDate;
	
	@XmlElement(name = "shortCode")
	private String shortCode;
	
	@XmlElement(name = "chargeMode")
	private String chargeMode;
	
	@XmlElement(name = "paymentChannel")
	private String paymentChannel;
	
	@XmlElement(name = "chargeAmount")
	private String chargeAmount;
	
	@XmlElement(name = "renewalDate")
	private String renewalDate;

    /**
     * Gets the value of the serviceName property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getServiceName() {
        return serviceName;
    }

    /**
     * Sets the value of the serviceName property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setServiceName(String value) {
        this.serviceName = value;
    }

    /**
     * Gets the value of the serviceDescription property.
     * 
     * @return
     *     possible object is
     *     {@link Object }
     *     
     */
    public String getServiceDescription() {
        return serviceDescription;
    }

    /**
     * Sets the value of the serviceDescription property.
     * 
     * @param value
     *     allowed object is
     *     {@link Object }
     *     
     */
    public void setServiceDescription(String value) {
        this.serviceDescription = value;
    }

    /**
     * Gets the value of the subscriptionDate property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getSubscriptionDate() {
        return subscriptionDate;
    }

    /**
     * Sets the value of the subscriptionDate property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setSubscriptionDate(String value) {
        this.subscriptionDate = value;
    }

    /**
     * Gets the value of the shortCode property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getShortCode() {
        return shortCode;
    }

    /**
     * Sets the value of the shortCode property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setShortCode(String value) {
        this.shortCode = value;
    }

    /**
     * Gets the value of the chargeMode property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getChargeMode() {
        return chargeMode;
    }

    /**
     * Sets the value of the chargeMode property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setChargeMode(String value) {
        this.chargeMode = value;
    }    

    /**
     * Gets the value of the chargeMode property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
	public String getPaymentChannel() {
		return paymentChannel;
	}

	 /**
     * Sets the value of the chargeMode property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
	public void setPaymentChannel(String paymentChannel) {
		this.paymentChannel = paymentChannel;
	}

	/**
     * Gets the value of the chargeAmount property.
     * 
     */
    public String getChargeAmount() {
        return chargeAmount;
    }

    /**
     * Sets the value of the chargeAmount property.
     * 
     */
    public void setChargeAmount(String value) {
        this.chargeAmount = value;
    }

    /**
     * Gets the value of the renewalDate property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getRenewalDate() {
        return renewalDate;
    }

    /**
     * Sets the value of the renewalDate property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setRenewalDate(String value) {
        this.renewalDate = value;
    }
}
