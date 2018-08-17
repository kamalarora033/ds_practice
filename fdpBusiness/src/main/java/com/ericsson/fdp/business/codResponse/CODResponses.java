/**
 * 
 */
package com.ericsson.fdp.business.codResponse;

import java.util.List;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;

/**
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlRootElement(name = "codresponses")
@XmlType(name = "", propOrder = {"codResponse", "error"})
public class CODResponses {
	
	/**
	 * Field codResponse.
	 */
	@XmlElement(name = "codresponse", required = true)
	private List<CodResponse> codResponse;
	
	/**
	 * Field error.
	 */
	@XmlElement(name = "error", required = true)
	private Error error;

	

	/**
	 * Method getError.
	 * @return Error
	 */
	public Error getError() {
		return error;
	}

	/**
	 * Method setError.
	 * @param error Error
	 */
	public void setError(Error error) {
		this.error = error;
	}

	/**
	
	 * @return the codResponse */
	public List<CodResponse> getCodResponse() {
		return codResponse;
	}

	/**
	 * @param codResponse the codResponse to set
	 */
	public void setCodResponse(List<CodResponse> codResponse) {
		this.codResponse = codResponse;
	}
	
}