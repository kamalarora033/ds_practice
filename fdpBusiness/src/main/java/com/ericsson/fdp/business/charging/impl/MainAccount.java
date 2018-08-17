package com.ericsson.fdp.business.charging.impl;

import com.ericsson.fdp.business.charging.Account;

/*
Feature Name: DA Based Charging
Changes: MainAccount class created which will be used while DA Based Air charging
Date: 18-12-2015
Singnum Id:ESIASAN
*/

public class MainAccount implements Account{

	private static final long serialVersionUID = 7518992172050558605L;

	private Long accountValue;	
	private Integer priority;
	
	public MainAccount(Integer priority){
		this.priority = priority;
	}
	
	public MainAccount(Long accountValue, Integer priority) {
		this.accountValue = accountValue;
		this.priority = priority;
	}
		
	public Long getAccountValue() {
		return accountValue;
	}

	public void setAccountValue(Long accountValue) {
		this.accountValue = accountValue;
	}
	
	public Integer getPriority() {
		return priority;
	}

	public void setPriority(Integer priority) {
		this.priority = priority;
	}

}
