
package com.mtn.esf.xmlns.xsd.updatecustomerdetails;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for ActivationMethodLangType complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="ActivationMethodLangType">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="LangType">
 *           &lt;simpleType>
 *             &lt;restriction base="{http://www.w3.org/2001/XMLSchema}string">
 *             &lt;/restriction>
 *           &lt;/simpleType>
 *         &lt;/element>
 *         &lt;element name="LangValue" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "ActivationMethodLangType", propOrder = {
    "langType",
    "langValue"
})
public class ActivationMethodLangType {

    @XmlElement(name = "LangType", required = true)
    protected String langType;
    @XmlElement(name = "LangValue", required = true)
    protected String langValue;

    /**
     * Gets the value of the langType property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getLangType() {
        return langType;
    }

    /**
     * Sets the value of the langType property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setLangType(String value) {
        this.langType = value;
    }

    /**
     * Gets the value of the langValue property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getLangValue() {
        return langValue;
    }

    /**
     * Sets the value of the langValue property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setLangValue(String value) {
        this.langValue = value;
    }

}
