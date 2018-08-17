package com.ericsson.fdp.business.route.processor;

public class UssdZteXMLResponseBean {
	
	private String transactionId;
	private String transactionTime;
	private String ussdResponseString;
	private String action;
	private String faultCode;
	private String faultString;
		
	public String getFaultString() {
		return faultString;
	}

	public void setFaultString(String faultString) {
		this.faultString = faultString;
	}

	public String getFaultCode() {
		return faultCode;
	}

	public void setFaultCode(String faultCode) {
		this.faultCode = faultCode;
	}

	public String getTransactionId() {
		return transactionId;
	}
	
	public void setTransactionId(String transactionId) {
		this.transactionId = transactionId;
	}
	
	public String getTransactionTime() {
		return transactionTime;
	}
	
	public void setTransactionTime(String transactionTime) {
		this.transactionTime = transactionTime;
	}
	
	public String getUssdResponseString() {
		return ussdResponseString;
	}

	public void setUssdResponseString(String ussdResponseString) {
		this.ussdResponseString = ussdResponseString;
	}

	public String getAction() {
		return action;
	}

	public void setAction(String action) {
		this.action = action;
	}
	
	

}
