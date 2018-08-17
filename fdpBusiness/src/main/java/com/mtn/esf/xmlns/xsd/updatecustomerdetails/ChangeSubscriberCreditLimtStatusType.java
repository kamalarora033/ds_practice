
package com.mtn.esf.xmlns.xsd.updatecustomerdetails;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for ChangeSubscriberCreditLimtStatusType complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="ChangeSubscriberCreditLimtStatusType">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="CreditLimitValue" type="{http://www.w3.org/2001/XMLSchema}long"/>
 *         &lt;element name="CreditLevel" type="{http://www.w3.org/2001/XMLSchema}integer" minOccurs="0"/>
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
@XmlType(name = "ChangeSubscriberCreditLimtStatusType", propOrder = {
    "creditLimitValue",
    "creditLevel",
    "additionalInfo"
})
public class ChangeSubscriberCreditLimtStatusType {

    @XmlElement(name = "CreditLimitValue")
    protected long creditLimitValue;
    @XmlElement(name = "CreditLevel")
    protected BigInteger creditLevel;
    @XmlElement(name = "AdditionalInfo")
    protected List<AdditionalInfoType> additionalInfo;

    /**
     * Gets the value of the creditLimitValue property.
     * 
     */
    public long getCreditLimitValue() {
        return creditLimitValue;
    }

    /**
     * Sets the value of the creditLimitValue property.
     * 
     */
    public void setCreditLimitValue(long value) {
        this.creditLimitValue = value;
    }

    /**
     * Gets the value of the creditLevel property.
     * 
     * @return
     *     possible object is
     *     {@link BigInteger }
     *     
     */
    public BigInteger getCreditLevel() {
        return creditLevel;
    }

    /**
     * Sets the value of the creditLevel property.
     * 
     * @param value
     *     allowed object is
     *     {@link BigInteger }
     *     
     */
    public void setCreditLevel(BigInteger value) {
        this.creditLevel = value;
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
