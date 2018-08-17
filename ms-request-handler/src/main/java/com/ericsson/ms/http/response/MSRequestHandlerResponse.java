package com.ericsson.ms.http.response;

import java.io.Serializable;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;

/**
 * The Request handler response
 * 
 * @author Ericsson
 */
@XmlRootElement(name = "requestHandler")
@XmlAccessorType(XmlAccessType.FIELD)
public class MSRequestHandlerResponse implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = -1247832019211925750L;

	/** The Status Field */
	private String status;
	/** The Response Code Field */
	private Integer responseCode;
	/** The Response Desc Field */
	private String responseDesc;

	/**
	 * @return the status
	 */
	public String getStatus() {
		return status;
	}

	/**
	 * @param status
	 *            the status to set
	 */
	public void setStatus(String status) {
		this.status = status;
	}

	/**
	 * @return the responseCode
	 */
	public Integer getResponseCode() {
		return responseCode;
	}

	/**
	 * @param responseCode
	 *            the responseCode to set
	 */
	public void setResponseCode(Integer responseCode) {
		this.responseCode = responseCode;
	}

	/**
	 * @return the responseDesc
	 */
	public String getResponseDesc() {
		return responseDesc;
	}

	/**
	 * @param responseDesc
	 *            the responseDesc to set
	 */
	public void setResponseDesc(String responseDesc) {
		this.responseDesc = responseDesc;
	}

}
