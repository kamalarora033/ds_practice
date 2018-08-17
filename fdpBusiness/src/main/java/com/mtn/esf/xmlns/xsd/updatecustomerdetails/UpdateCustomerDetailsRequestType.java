
package com.mtn.esf.xmlns.xsd.updatecustomerdetails;

import java.util.ArrayList;
import java.util.List;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;
import com.mtn.esf.xmlns.xsd.common.CommonComponentsType;


/**
 * <p>Java class for UpdateCustomerDetailsRequestType complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="UpdateCustomerDetailsRequestType">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="CommonComponents" type="{http://xmlns.esf.mtn.com/xsd/Common}CommonComponentsType"/>
 *         &lt;element name="Narration" type="{http://www.w3.org/2001/XMLSchema}anyType" minOccurs="0"/>
 *         &lt;element name="Identification" type="{http://xmlns.esf.mtn.com/xsd/UpdateCustomerDetails}IdentificationType" maxOccurs="unbounded"/>
 *         &lt;choice>
 *           &lt;element name="ChangeSubscriberCreditLimit" type="{http://xmlns.esf.mtn.com/xsd/UpdateCustomerDetails}ChangeSubscriberCreditLimtStatusType"/>
 *           &lt;element name="ChangeSubscriberLifeCycleStatus" type="{http://xmlns.esf.mtn.com/xsd/UpdateCustomerDetails}ChangeSubscriberLifeCycleStatusType"/>
 *           &lt;element name="ChangeSubscriberBlacklistRechargeStatus" type="{http://xmlns.esf.mtn.com/xsd/UpdateCustomerDetails}ChangeSubscriberBlacklistRechargeStatusType"/>
 *         &lt;/choice>
 *         &lt;element name="CustomReference" type="{http://xmlns.esf.mtn.com/xsd/UpdateCustomerDetails}CustomReferenceType" maxOccurs="unbounded" minOccurs="0"/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "UpdateCustomerDetailsRequestType", propOrder = {
    "commonComponents",
    "narration",
    "identification",
    "changeSubscriberCreditLimit",
    "changeSubscriberLifeCycleStatus",
    "changeSubscriberBlacklistRechargeStatus",
    "customReference"
})
public class UpdateCustomerDetailsRequestType {

    @XmlElement(name = "CommonComponents", required = true)
    protected CommonComponentsType commonComponents;
    @XmlElement(name = "Narration")
    protected Object narration;
    @XmlElement(name = "Identification", required = true)
    protected List<IdentificationType> identification;
    @XmlElement(name = "ChangeSubscriberCreditLimit")
    protected ChangeSubscriberCreditLimtStatusType changeSubscriberCreditLimit;
    @XmlElement(name = "ChangeSubscriberLifeCycleStatus")
    protected ChangeSubscriberLifeCycleStatusType changeSubscriberLifeCycleStatus;
    @XmlElement(name = "ChangeSubscriberBlacklistRechargeStatus")
    protected ChangeSubscriberBlacklistRechargeStatusType changeSubscriberBlacklistRechargeStatus;
    @XmlElement(name = "CustomReference")
    protected List<CustomReferenceType> customReference;

    /**
     * Gets the value of the commonComponents property.
     * 
     * @return
     *     possible object is
     *     {@link CommonComponentsType }
     *     
     */
    public CommonComponentsType getCommonComponents() {
        return commonComponents;
    }

    /**
     * Sets the value of the commonComponents property.
     * 
     * @param value
     *     allowed object is
     *     {@link CommonComponentsType }
     *     
     */
    public void setCommonComponents(CommonComponentsType value) {
        this.commonComponents = value;
    }

    /**
     * Gets the value of the narration property.
     * 
     * @return
     *     possible object is
     *     {@link Object }
     *     
     */
    public Object getNarration() {
        return narration;
    }

    /**
     * Sets the value of the narration property.
     * 
     * @param value
     *     allowed object is
     *     {@link Object }
     *     
     */
    public void setNarration(Object value) {
        this.narration = value;
    }

    /**
     * Gets the value of the identification property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the identification property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getIdentification().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link IdentificationType }
     * 
     * 
     */
    public List<IdentificationType> getIdentification() {
        if (identification == null) {
            identification = new ArrayList<IdentificationType>();
        }
        return this.identification;
    }

    /**
     * Gets the value of the changeSubscriberCreditLimit property.
     * 
     * @return
     *     possible object is
     *     {@link ChangeSubscriberCreditLimtStatusType }
     *     
     */
    public ChangeSubscriberCreditLimtStatusType getChangeSubscriberCreditLimit() {
        return changeSubscriberCreditLimit;
    }

    /**
     * Sets the value of the changeSubscriberCreditLimit property.
     * 
     * @param value
     *     allowed object is
     *     {@link ChangeSubscriberCreditLimtStatusType }
     *     
     */
    public void setChangeSubscriberCreditLimit(ChangeSubscriberCreditLimtStatusType value) {
        this.changeSubscriberCreditLimit = value;
    }

    /**
     * Gets the value of the changeSubscriberLifeCycleStatus property.
     * 
     * @return
     *     possible object is
     *     {@link ChangeSubscriberLifeCycleStatusType }
     *     
     */
    public ChangeSubscriberLifeCycleStatusType getChangeSubscriberLifeCycleStatus() {
        return changeSubscriberLifeCycleStatus;
    }

    /**
     * Sets the value of the changeSubscriberLifeCycleStatus property.
     * 
     * @param value
     *     allowed object is
     *     {@link ChangeSubscriberLifeCycleStatusType }
     *     
     */
    public void setChangeSubscriberLifeCycleStatus(ChangeSubscriberLifeCycleStatusType value) {
        this.changeSubscriberLifeCycleStatus = value;
    }

    /**
     * Gets the value of the changeSubscriberBlacklistRechargeStatus property.
     * 
     * @return
     *     possible object is
     *     {@link ChangeSubscriberBlacklistRechargeStatusType }
     *     
     */
    public ChangeSubscriberBlacklistRechargeStatusType getChangeSubscriberBlacklistRechargeStatus() {
        return changeSubscriberBlacklistRechargeStatus;
    }

    /**
     * Sets the value of the changeSubscriberBlacklistRechargeStatus property.
     * 
     * @param value
     *     allowed object is
     *     {@link ChangeSubscriberBlacklistRechargeStatusType }
     *     
     */
    public void setChangeSubscriberBlacklistRechargeStatus(ChangeSubscriberBlacklistRechargeStatusType value) {
        this.changeSubscriberBlacklistRechargeStatus = value;
    }

    /**
     * Gets the value of the customReference property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the customReference property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getCustomReference().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link CustomReferenceType }
     * 
     * 
     */
    public List<CustomReferenceType> getCustomReference() {
        if (customReference == null) {
            customReference = new ArrayList<CustomReferenceType>();
        }
        return this.customReference;
    }

}
