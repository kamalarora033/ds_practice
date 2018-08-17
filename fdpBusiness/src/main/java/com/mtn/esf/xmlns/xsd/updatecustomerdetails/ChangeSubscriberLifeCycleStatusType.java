
package com.mtn.esf.xmlns.xsd.updatecustomerdetails;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for ChangeSubscriberLifeCycleStatusType complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="ChangeSubscriberLifeCycleStatusType">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="ActivationMethod" type="{http://www.w3.org/2001/XMLSchema}integer" minOccurs="0"/>
 *         &lt;element name="AreaCode" minOccurs="0">
 *           &lt;simpleType>
 *             &lt;restriction base="{http://www.w3.org/2001/XMLSchema}string">
 *             &lt;/restriction>
 *           &lt;/simpleType>
 *         &lt;/element>
 *         &lt;element name="ActivationMethodLang" type="{http://xmlns.esf.mtn.com/xsd/UpdateCustomerDetails}ActivationMethodLangType" maxOccurs="unbounded" minOccurs="0"/>
 *         &lt;element name="RechargeCardNum" minOccurs="0">
 *           &lt;simpleType>
 *             &lt;restriction base="{http://www.w3.org/2001/XMLSchema}string">
 *             &lt;/restriction>
 *           &lt;/simpleType>
 *         &lt;/element>
 *         &lt;element name="RechargeAmt" type="{http://www.w3.org/2001/XMLSchema}integer" minOccurs="0"/>
 *         &lt;element name="AdditionalInfo" type="{http://xmlns.esf.mtn.com/xsd/UpdateCustomerDetails}AdditionalInfoType" maxOccurs="unbounded" minOccurs="0"/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "ChangeSubscriberLifeCycleStatusType", propOrder = {
    "activationMethod",
    "areaCode",
    "activationMethodLang",
    "rechargeCardNum",
    "rechargeAmt",
    "additionalInfo"
})
public class ChangeSubscriberLifeCycleStatusType {

    @XmlElement(name = "ActivationMethod")
    protected BigInteger activationMethod;
    @XmlElement(name = "AreaCode")
    protected String areaCode;
    @XmlElement(name = "ActivationMethodLang")
    protected List<ActivationMethodLangType> activationMethodLang;
    @XmlElement(name = "RechargeCardNum")
    protected String rechargeCardNum;
    @XmlElement(name = "RechargeAmt")
    protected BigInteger rechargeAmt;
    @XmlElement(name = "AdditionalInfo")
    protected List<AdditionalInfoType> additionalInfo;

    /**
     * Gets the value of the activationMethod property.
     * 
     * @return
     *     possible object is
     *     {@link BigInteger }
     *     
     */
    public BigInteger getActivationMethod() {
        return activationMethod;
    }

    /**
     * Sets the value of the activationMethod property.
     * 
     * @param value
     *     allowed object is
     *     {@link BigInteger }
     *     
     */
    public void setActivationMethod(BigInteger value) {
        this.activationMethod = value;
    }

    /**
     * Gets the value of the areaCode property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getAreaCode() {
        return areaCode;
    }

    /**
     * Sets the value of the areaCode property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setAreaCode(String value) {
        this.areaCode = value;
    }

    /**
     * Gets the value of the activationMethodLang property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the activationMethodLang property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getActivationMethodLang().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link ActivationMethodLangType }
     * 
     * 
     */
    public List<ActivationMethodLangType> getActivationMethodLang() {
        if (activationMethodLang == null) {
            activationMethodLang = new ArrayList<ActivationMethodLangType>();
        }
        return this.activationMethodLang;
    }

    /**
     * Gets the value of the rechargeCardNum property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getRechargeCardNum() {
        return rechargeCardNum;
    }

    /**
     * Sets the value of the rechargeCardNum property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setRechargeCardNum(String value) {
        this.rechargeCardNum = value;
    }

    /**
     * Gets the value of the rechargeAmt property.
     * 
     * @return
     *     possible object is
     *     {@link BigInteger }
     *     
     */
    public BigInteger getRechargeAmt() {
        return rechargeAmt;
    }

    /**
     * Sets the value of the rechargeAmt property.
     * 
     * @param value
     *     allowed object is
     *     {@link BigInteger }
     *     
     */
    public void setRechargeAmt(BigInteger value) {
        this.rechargeAmt = value;
    }

    /**
     * Gets the value of the additionalInfo property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the additionalInfo property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getAdditionalInfo().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link AdditionalInfoType }
     * 
     * 
     */
    public List<AdditionalInfoType> getAdditionalInfo() {
        if (additionalInfo == null) {
            additionalInfo = new ArrayList<AdditionalInfoType>();
        }
        return this.additionalInfo;
    }

}
