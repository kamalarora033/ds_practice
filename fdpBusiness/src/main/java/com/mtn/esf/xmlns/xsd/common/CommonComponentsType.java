
package com.mtn.esf.xmlns.xsd.common;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlSchemaType;
import javax.xml.bind.annotation.XmlType;
import javax.xml.datatype.XMLGregorianCalendar;


/**
 * <p>Java class for CommonComponentsType complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="CommonComponentsType">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="IMSINum" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="MSISDNNum" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="ProcessingNumber" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *         &lt;element name="OrderDateTime" type="{http://www.w3.org/2001/XMLSchema}dateTime" minOccurs="0"/>
 *         &lt;element name="OpCoID" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="SenderID" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "CommonComponentsType", propOrder = {
    "imsiNum",
    "msisdnNum",
    "processingNumber",
    "orderDateTime",
    "opCoID",
    "senderID"
})
public class CommonComponentsType {

    @XmlElement(name = "IMSINum")
    protected String imsiNum;
    @XmlElement(name = "MSISDNNum")
    protected String msisdnNum;
    @XmlElement(name = "ProcessingNumber", required = true)
    protected String processingNumber;
    @XmlElement(name = "OrderDateTime")
    @XmlSchemaType(name = "dateTime")
    protected XMLGregorianCalendar orderDateTime;
    @XmlElement(name = "OpCoID")
    protected String opCoID;
    @XmlElement(name = "SenderID", required = true)
    protected String senderID;

    /**
     * Gets the value of the imsiNum property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getIMSINum() {
        return imsiNum;
    }

    /**
     * Sets the value of the imsiNum property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setIMSINum(String value) {
        this.imsiNum = value;
    }

    /**
     * Gets the value of the msisdnNum property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getMSISDNNum() {
        return msisdnNum;
    }

    /**
     * Sets the value of the msisdnNum property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setMSISDNNum(String value) {
        this.msisdnNum = value;
    }

    /**
     * Gets the value of the processingNumber property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getProcessingNumber() {
        return processingNumber;
    }

    /**
     * Sets the value of the processingNumber property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setProcessingNumber(String value) {
        this.processingNumber = value;
    }

    /**
     * Gets the value of the orderDateTime property.
     * 
     * @return
     *     possible object is
     *     {@link XMLGregorianCalendar }
     *     
     */
    public XMLGregorianCalendar getOrderDateTime() {
        return orderDateTime;
    }

    /**
     * Sets the value of the orderDateTime property.
     * 
     * @param value
     *     allowed object is
     *     {@link XMLGregorianCalendar }
     *     
     */
    public void setOrderDateTime(XMLGregorianCalendar value) {
        this.orderDateTime = value;
    }

    /**
     * Gets the value of the opCoID property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getOpCoID() {
        return opCoID;
    }

    /**
     * Sets the value of the opCoID property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setOpCoID(String value) {
        this.opCoID = value;
    }

    /**
     * Gets the value of the senderID property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getSenderID() {
        return senderID;
    }

    /**
     * Sets the value of the senderID property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setSenderID(String value) {
        this.senderID = value;
    }

}
