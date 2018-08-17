package com.ericsson.fdp.business.response.fulfillment.xml;

import java.io.Serializable;
import java.util.List;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement(name="consumers")
@XmlAccessorType(XmlAccessType.FIELD)
public class Consumers implements Serializable{

	/**
	 * The class serial version UID
	 */
	private static final long serialVersionUID = -5874345084446589918L;

	@XmlElement(name="consumer")
	private List<Consumer> consumers;

	/**
	 * @return the consumers
	 */
	public List<Consumer> getConsumers() {
		return consumers;
	}

	/**
	 * @param consumers the consumers to set
	 */
	public void setConsumers(List<Consumer> consumers) {
		this.consumers = consumers;
	}
	
}
