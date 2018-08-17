package com.ericsson.fdp.business.response.fulfillment.xml;

import java.io.Serializable;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement(name="balanceEnquiry")
@XmlAccessorType(XmlAccessType.FIELD)
public class BalanceEnquiry implements Serializable {

	/**
	 * This class serial version UID.
	 */
	private static final long serialVersionUID = 8058996820309256628L;
	
	@XmlElement(name = "balances")
	private Balances balances;

	@XmlElement(name = "notification")
	private String notification;

	/**
	 * @return the balances
	 */
	public Balances getBalances() {
		return balances;
	}

	/**
	 * @param balances the balances to set
	 */
	public void setBalances(Balances balances) {
		this.balances = balances;
	}

	/**
	 * @return the notification
	 */
	public String getNotification() {
		return notification;
	}

	/**
	 * @param notification the notification to set
	 */
	public void setNotification(String notification) {
		this.notification = notification;
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return "BalanceEnquiry [balances=" + balances + ", notification="
				+ notification + "]";
	}
}
