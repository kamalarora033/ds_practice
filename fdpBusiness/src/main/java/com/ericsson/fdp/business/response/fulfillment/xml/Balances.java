package com.ericsson.fdp.business.response.fulfillment.xml;

import java.io.Serializable;
import java.util.List;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;


@XmlRootElement(name="balances")
@XmlAccessorType(XmlAccessType.FIELD)
public class Balances implements Serializable{

	/**
	 * Class serial version UID
	 */
	private static final long serialVersionUID = 1L;

	@XmlElement(name="balance")
	private List<Balance> balance;

	/**
	 * @return the balance
	 */
	public List<Balance> getBalance() {
		return balance;
	}

	/**
	 * @param balance the balance to set
	 */
	public void setBalance(List<Balance> balance) {
		this.balance = balance;
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return "Balances [balance=" + balance + "]";
	}
}
