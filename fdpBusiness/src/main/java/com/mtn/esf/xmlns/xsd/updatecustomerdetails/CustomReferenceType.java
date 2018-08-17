
package com.mtn.esf.xmlns.xsd.updatecustomerdetails;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for CustomReferenceType complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="CustomReferenceType">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="RefType" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *         &lt;element name="RefValue" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "CustomReferenceType", propOrder = {
    "refType",
    "refValue"
})
public class CustomReferenceType {

    @XmlElement(name = "RefType", required = true)
    protected String refType;
    @XmlElement(name = "RefValue", required = true)
    protected String refValue;

    /**
     * Gets the value of the refType property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getRefType() {
        return refType;
    }

    /**
     * Sets the value of the refType property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setRefType(String value) {
        this.refType = value;
    }

    /**
     * Gets the value of the refValue property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getRefValue() {
        return refValue;
    }

    /**
     * Sets the value of the refValue property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setRefValue(String value) {
        this.refValue = value;
    }

}
