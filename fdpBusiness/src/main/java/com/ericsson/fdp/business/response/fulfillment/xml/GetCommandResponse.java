package com.ericsson.fdp.business.response.fulfillment.xml;

import java.io.Serializable;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement(name="getCommandResponse")
@XmlAccessorType(XmlAccessType.FIELD)
public class GetCommandResponse implements Serializable {

	/**
	 *  The class serial version UID.
	 */
	private static final long serialVersionUID = -9077975640867377972L;

	@XmlElement(name="methodResponse" , required=true)
	private String methodResponse;
	
	@XmlElement(name="getServicesDtlsResponse", required=true)
	private String getServicesDtlsResponse;
	
	@XmlAttribute(name="type")
	private String type;
	
	@XmlAttribute(name="name")
	private String name;

	/**
	 * @return the methodResponse
	 */
	public String getMethodResponse() {
		return methodResponse;
	}

	/**
	 * @param methodResponse the methodResponse to set
	 */
	public void setMethodResponse(String methodResponse) {
		this.methodResponse = methodResponse;
	}

	/**
	 * @return the getServicesDtlsResponse
	 */
	public String getGetServicesDtlsResponse() {
		return getServicesDtlsResponse;
	}

	/**
	 * @param getServicesDtlsResponse the getServicesDtlsResponse to set
	 */
	public void setGetServicesDtlsResponse(String getServicesDtlsResponse) {
		this.getServicesDtlsResponse = getServicesDtlsResponse;
	}

	/**
	 * @return the type
	 */
	public String getType() {
		return type;
	}

	/**
	 * @param type the type to set
	 */
	public void setType(String type) {
		this.type = type;
	}

	/**
	 * @return the name
	 */
	public String getName() {
		return name;
	}

	/**
	 * @param name the name to set
	 */
	public void setName(String name) {
		this.name = name;
	}
}
