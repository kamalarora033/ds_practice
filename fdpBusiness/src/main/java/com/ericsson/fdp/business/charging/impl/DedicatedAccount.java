package com.ericsson.fdp.business.charging.impl;

import com.ericsson.fdp.business.charging.Account;

/*
Feature Name: DA Based Charging
Changes: DedicatedAccount class created which will be used for DA Based Charging
Date: 18-12-2015
Singnum Id:ESIASAN
*/

public class DedicatedAccount implements Account{

	private static final long serialVersionUID = 6438412211783196679L;
	private String dedicatedAccountId;
	private Long accountValue;	
	private Integer priority;
	
	public DedicatedAccount(Integer priority){
		this.priority = priority;
	}
	
	public DedicatedAccount(String dedicatedAccountId, Long accountValue, Integer priority) {
		this.dedicatedAccountId = dedicatedAccountId;
		this.accountValue = accountValue;
		this.priority = priority;
	}
	/**
	 * @return the dedicatedAccountId
	 */
	public String getDedicatedAccountId() {
		return dedicatedAccountId;
	}
	/**
	 * @param dedicatedAccountId the dedicatedAccountId to set
	 */
	public void setDedicatedAccountId(String dedicatedAccountId) {
		this.dedicatedAccountId = dedicatedAccountId;
	}
	/**
	 * @return the accountValue
	 */
	public Long getAccountValue() {
		return accountValue;
	}
	/**
	 * @param accountValue the accountValue to set
	 */
	public void setAccountValue(Long accountValue) {
		this.accountValue = accountValue;
	}
	/**
	 * @return the priority
	 */
	public Integer getPriority() {
		return priority;
	}
	/**
	 * @param priority the priority to set
	 */
	public void setPriority(Integer priority) {
		this.priority = priority;
	}

}
