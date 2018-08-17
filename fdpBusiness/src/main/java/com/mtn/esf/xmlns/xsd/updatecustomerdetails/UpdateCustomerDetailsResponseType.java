
package com.mtn.esf.xmlns.xsd.updatecustomerdetails;

import java.util.ArrayList;
import java.util.List;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;
import com.mtn.esf.xmlns.xsd.common.CommonComponentsType;


/**
 * <p>Java class for UpdateCustomerDetailsResponseType complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="UpdateCustomerDetailsResponseType">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="CommonComponents" type="{http://xmlns.esf.mtn.com/xsd/Common}CommonComponentsType"/>
 *         &lt;element name="StatusInfo" type="{http://xmlns.esf.mtn.com/xsd/UpdateCustomerDetails}StatusInfoType"/>
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
@XmlType(name = "UpdateCustomerDetailsResponseType", propOrder = {
    "commonComponents",
    "statusInfo",
    "customReference"
})
public class UpdateCustomerDetailsResponseType {

    @XmlElement(name = "CommonComponents", required = true)
    protected CommonComponentsType commonComponents;
    @XmlElement(name = "StatusInfo", required = true)
    protected StatusInfoType statusInfo;
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
     * Gets the value of the statusInfo property.
     * 
     * @return
     *     possible object is
     *     {@link StatusInfoType }
     *     
     */
    public StatusInfoType getStatusInfo() {
        return statusInfo;
    }

    /**
     * Sets the value of the statusInfo property.
     * 
     * @param value
     *     allowed object is
     *     {@link StatusInfoType }
     *     
     */
    public void setStatusInfo(StatusInfoType value) {
        this.statusInfo = value;
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
