package com.ericsson.fdp.business.response.fulfillment.xml;

import java.io.Serializable;
import java.util.List;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement(name="attributes")
@XmlAccessorType(XmlAccessType.FIELD)
public class Attributes implements Serializable {

	/**
	 * The class serial version UID
	 */
	private static final long serialVersionUID = -6907826171772433471L;
	@XmlElement(name="attribute")
	private List<Attribute> attributes;

	public List<Attribute> getAttributes() {
		return attributes;
	}

	public void setAttributes(List<Attribute> attributes) {
		this.attributes = attributes;
	}
}
