package com.ericsson.fdp.business.response.fulfillment.xml;

import java.io.Serializable;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import com.ericsson.fdp.business.enums.ivr.FulfillmentStatus;
import com.ericsson.fdp.business.enums.ivr.FulfillmentSystemTypes;

/**
 * The Class FulfillmentResponse.
 */
@XmlRootElement(name = "fulfillmentService")
@XmlAccessorType(XmlAccessType.FIELD)
public class FulfillmentResponse implements Serializable {

	/** The Constant serialVersionUID. */
	private static final long serialVersionUID = 8187590646169500379L;

	/** The response code. */
	@XmlElement(name = "responseCode", required = true)
	private String responseCode;

	/** The status. */
	@XmlElement(name = "status", required = true)
	private FulfillmentStatus status;

	/** The system type. */
	@XmlElement(name = "systemType", required = true)
	private FulfillmentSystemTypes systemType;

	/** The description. */
	@XmlElement(name = "responseDescription", required = true)
	private String description;

	/** The request id. */
	@XmlElement(name = "requestId", required = true)
	private String requestId;
	
	@XmlElement(name="msisdn")
	private String msisdn;
	
	@XmlElement(name="product_id")
	private String product_id;
	
	public String getProduct_id() {
		return product_id;
	}

	public void setProduct_id(String product_id) {
		this.product_id = product_id;
	}

	public String getmsisdn() {
		return msisdn;
	}

	public void setmsisdn(String msisdn) {
		this.msisdn = msisdn;
	}

	@XmlElement(name = "responseData")
	private ResponseData responseData;

	/**
	 * @return the responseCode
	 */
	public String getResponseCode() {
		return responseCode;
	}

	/**
	 * @param responseCode
	 *            the responseCode to set
	 */
	public void setResponseCode(String responseCode) {
		this.responseCode = responseCode;
	}

	/**
	 * @return the status
	 */
	public FulfillmentStatus getStatus() {
		return status;
	}

	/**
	 * @param status
	 *            the status to set
	 */
	public void setStatus(FulfillmentStatus status) {
		this.status = status;
	}

	/**
	 * @return the systemType
	 */
	public FulfillmentSystemTypes getSystemType() {
		return systemType;
	}

	/**
	 * @param systemType
	 *            the systemType to set
	 */
	public void setSystemType(FulfillmentSystemTypes systemType) {
		this.systemType = systemType;
	}

	/**
	 * @return the description
	 */
	public String getDescription() {
		return description;
	}

	/**
	 * @param description
	 *            the description to set
	 */
	public void setDescription(String description) {
		this.description = description;
	}

	/**
	 * @return the requestId
	 */
	public String getRequestId() {
		return requestId;
	}

	/**
	 * @param requestId
	 *            the requestId to set
	 */
	public void setRequestId(String requestId) {
		this.requestId = requestId;
	}

	/**
	 * @return the responseData
	 */
	public ResponseData getResponseData() {
		return responseData;
	}

	/**
	 * @param responseData
	 *            the responseData to set
	 */
	public void setResponseData(ResponseData responseData) {
		this.responseData = responseData;
	}

}
