/**
 * Ericsson template
 */
package com.ericsson.fdp.business.codResponse;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;
import javax.xml.datatype.XMLGregorianCalendar;

import com.ericsson.fdp.FDPCacheable;



/**
 * <p>Java class for anonymous complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType>
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="status" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *          &lt;element name="error" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *          &lt;element name="msisdn" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *         &lt;element name="product_id" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *         &lt;element name="product_mrp" type="{http://www.w3.org/2001/XMLSchema}double"/>
 *         &lt;element name="purchase_timestamp" type="{http://www.w3.org/2001/XMLSchema}date"/>
 *         &lt;element name="expiry_timestamp" type="{http://www.w3.org/2001/XMLSchema}date"/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
/*@XmlType(name = "", propOrder = {"codReqId",
    "status","error","msisdn","productId","productMRP","purchaseTimestamp","expiryTimestamp"
})*/
@XmlRootElement(name = "codresponse")
public class CodResponse implements FDPCacheable{

	/**
	 * 
	 */
	private static final long serialVersionUID = 4933385521432293999L;

	@XmlElement(required = true)
    private String status;
    
    @XmlElement(name = "error", required = true)
	private Error error;
    
    @XmlElement(required = true)
    private Integer codReqId;
    

	@XmlElement(name = "msisdn", required = true)
	private String msisdn;
	
	@XmlElement(name = "product_id", required = true)
	private String productId;
	
	@XmlElement(name = "product_mrp", required = true)
	private Double productMRP;
	
	
	@XmlElement(name = "purchase_timestamp", required = true)
	private XMLGregorianCalendar purchaseTimestamp;
	
	@XmlElement(name = "expiry_timestamp", required = true)
	private XMLGregorianCalendar expiryTimestamp;
    
    
    /**
	 * @param error the error to set
	 */
	public void setError(Error error) {
		this.error = error;
	}

	/**
     * Gets the value of the status property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getStatus() {
        return status;
    }

    /**
     * Sets the value of the status property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setStatus(String value) {
        this.status = value;
    }
    public Error getError() {
		return error;
	}
    /**
	 * @return the msisdn
	 */
	public String getMsisdn() {
		return msisdn;
	}

	/**
	 * @param msisdn the msisdn to set
	 */
	public void setMsisdn(String msisdn) {
		this.msisdn = msisdn;
	}

	/**
	 * @return the productId
	 */
	public String getProductId() {
		return productId;
	}

	/**
	 * @param productId the productId to set
	 */
	public void setProductId(String productId) {
		this.productId = productId;
	}

	/**
	 * @return the productMRP
	 */
	public Double getProductMRP() {
		return productMRP;
	}

	/**
	 * @param productMRP the productMRP to set
	 */
	public void setProductMRP(Double productMRP) {
		this.productMRP = productMRP;
	}

	/**
	 * @return the purchaseTimestamp
	 */
	public XMLGregorianCalendar getPurchaseTimestamp() {
		return purchaseTimestamp;
	}

	/**
	 * @param purchaseTimestamp the purchaseTimestamp to set
	 */
	public void setPurchaseTimestamp(XMLGregorianCalendar purchaseTimestamp) {
		this.purchaseTimestamp = purchaseTimestamp;
	}

	/**
	 * @return the expiryTimestamp
	 */
	public XMLGregorianCalendar getExpiryTimestamp() {
		return expiryTimestamp;
	}

	/**
	 * @param expiryTimestamp the expiryTimestamp to set
	 */
	public void setExpiryTimestamp(XMLGregorianCalendar expiryTimestamp) {
		this.expiryTimestamp = expiryTimestamp;
	}

	/**
	 * @return the codReqId
	 */
	public Integer getCodReqId() {
		return codReqId;
	}

	/**
	 * @param codReqId the codReqId to set
	 */
	public void setCodReqId(Integer codReqId) {
		this.codReqId = codReqId;
	}
	

}
