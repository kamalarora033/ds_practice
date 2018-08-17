package com.ericsson.fdp.business.response.fulfillment.xml;

import java.io.Serializable;
import java.util.List;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement(name="tariffs")
@XmlAccessorType(XmlAccessType.FIELD)
public class Tariffs implements Serializable {

	/**
	 * The class serial version UID
	 */
	private static final long serialVersionUID = 5510480380202424009L;
	
	@XmlElement(name="tariff")
	private List<Tariff> tariffs;

	/**
	 * @return the tariffs
	 */
	public List<Tariff> getTariffs() {
		return tariffs;
	}

	/**
	 * @param tariffs the tariffs to set
	 */
	public void setTariffs(List<Tariff> tariffs) {
		this.tariffs = tariffs;
	}
	
	
}
