package com.ericsson.fdp.business.response.fulfillment.xml;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement(name="tariffEnquiry")
@XmlAccessorType(XmlAccessType.FIELD)
public class TariffEnquiry {

	/** The tariffs **/
	@XmlElement(name="tariffs")
	private Tariffs tariffs;

	/**
	 * @return the tariffs
	 */
	public Tariffs getTariffs() {
		return tariffs;
	}

	/**
	 * @param tariffs the tariffs to set
	 */
	public void setTariffs(Tariffs tariffs) {
		this.tariffs = tariffs;
	}
	
	
	
}
