package com.ericsson.fdp.business.response.fulfillment.xml;

import java.io.Serializable;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement(name="tariff")
@XmlAccessorType(XmlAccessType.FIELD)
public class Tariff implements Serializable{

	/**
	 * The class serial version UID
	 */
	private static final long serialVersionUID = 8902782716602601570L;

	@XmlElement(name="tariffType")
	private String tariffType;
	
	@XmlElement(name="attributes")
	private Attributes attributes;
	
	@XmlElement(name="mainTariff")
	private String mainTariff;

	public String getTariffType() {
		return tariffType;
	}

	public void setTariffType(String tariffType) {
		this.tariffType = tariffType;
	}

	public Attributes getAttributes() {
		return attributes;
	}

	public void setAttributes(Attributes attributes) {
		this.attributes = attributes;
	}

	public String getMainTariff() {
		return mainTariff;
	}

	public void setMainTariff(String mainTariff) {
		this.mainTariff = mainTariff;
	}
}
