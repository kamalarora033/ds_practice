package com.ericsson.fdp.business.response.fulfillment.xml;

import java.io.Serializable;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement(name="balance")
@XmlAccessorType(XmlAccessType.FIELD)
public class Balance implements Serializable{

	/**
	 * Class serial version UID.
	 */
	private static final long serialVersionUID = 1L;

	@XmlElement(name = "balanceKey")
	private String balanceKey;
	
	@XmlElement(name = "balanceValue")
	private String balanceValue;

	/**
	 * @return the balanceKey
	 */
	public String getBalanceKey() {
		return balanceKey;
	}

	/**
	 * @param balanceKey the balanceKey to set
	 */
	public void setBalanceKey(String balanceKey) {
		this.balanceKey = balanceKey;
	}

	/**
	 * @return the balanceValue
	 */
	public String getBalanceValue() {
		return balanceValue;
	}

	/**
	 * @param balanceValue the balanceValue to set
	 */
	public void setBalanceValue(String balanceValue) {
		this.balanceValue = balanceValue;
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return "Balance [balanceKey=" + balanceKey + ", balanceValue="
				+ balanceValue + "]";
	}
}
