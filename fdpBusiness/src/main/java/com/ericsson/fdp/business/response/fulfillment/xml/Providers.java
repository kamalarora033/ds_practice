package com.ericsson.fdp.business.response.fulfillment.xml;

import java.io.Serializable;
import java.util.List;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;


@XmlRootElement(name="providers")
@XmlAccessorType(XmlAccessType.FIELD)
public class Providers implements Serializable{

	/**
	 * The class serial version UID
	 */
	private static final long serialVersionUID = 1773591245210488213L;

	@XmlElement(name="provider")
	private List<Provider> providers;

	public List<Provider> getProducers() {
		return providers;
	}

	public void setProducers(List<Provider> providers) {
		this.providers = providers;
	}
}
