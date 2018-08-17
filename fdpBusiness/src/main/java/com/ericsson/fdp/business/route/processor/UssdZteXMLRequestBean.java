package com.ericsson.fdp.business.route.processor;

public class UssdZteXMLRequestBean {
	
	private String transactionId;
	private String transactionTime;
	private String msisdn;
	private String ussdServiceCode;
	private String ussdRequestString;
	private String xmlResponse;
	
	
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
	
	public String getMsisdn() {
		return msisdn;
	}
	
	public void setMsisdn(String msisdn) {
		this.msisdn = msisdn;
	}
	
	public String getUssdServiceCode() {
		return ussdServiceCode;
	}
	
	public void setUssdServiceCode(String ussdServiceCode) {
		this.ussdServiceCode = ussdServiceCode;
	}
	
	public String getUssdRequestString() {
		return ussdRequestString;
	}
	
	public void setUssdRequestString(String ussdRequestString) {
		this.ussdRequestString = ussdRequestString;
	}
	
	public String getXmlResponse() {
		return xmlResponse;
	}
	
	public void setXmlResponse(String xmlResponse) {
		this.xmlResponse = xmlResponse;
	}
	

}
