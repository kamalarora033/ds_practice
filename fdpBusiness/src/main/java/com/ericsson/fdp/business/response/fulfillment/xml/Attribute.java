package com.ericsson.fdp.business.response.fulfillment.xml;

import java.io.Serializable;
import java.util.Date;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import com.ericsson.fdp.business.enums.ivr.FulfillmentAttributesType;

@XmlRootElement(name="attribute")
@XmlAccessorType(XmlAccessType.FIELD)
public class Attribute implements Serializable {

	/**
	 * The class serial version UID
	 */
	private static final long serialVersionUID = -7772171505517665360L;

	@XmlElement(name="type")
	private FulfillmentAttributesType type = null;
	
	@XmlElement(name="id")
	private Integer id;
	
	@XmlElement(name="name")
	private String name;
	
	@XmlElement(name="value")
	private String value;
	
	@XmlElement(name="validity")
	private Date validity;

	/**
	 * @return the type
	 */
	public FulfillmentAttributesType getType() {
		return type;
	}

	/**
	 * @param type the type to set
	 */
	public void setType(FulfillmentAttributesType type) {
		this.type = type;
	}

	/**
	 * @return the id
	 */
	public Integer getId() {
		return id;
	}

	/**
	 * @param id the id to set
	 */
	public void setId(Integer id) {
		this.id = id;
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

	/**
	 * @return the value
	 */
	public String getValue() {
		return value;
	}

	/**
	 * @param value the value to set
	 */
	public void setValue(String value) {
		this.value = value;
	}

	/**
	 * @return the validity
	 */
	public Date getValidity() {
		return validity;
	}

	/**
	 * @param validity the validity to set
	 */
	public void setValidity(Date validity) {
		this.validity = validity;
	}

	@Override
	public String toString() {
		return "Attribute [type=" + type + ", id=" + id + ", name=" + name + ", value=" + value + ", validity="
				+ validity + "]";
	}
}
