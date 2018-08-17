package com.ericsson.fdp.business.https.evds;

public class HTTPSServerDetailsDTO {

	String ip;
	Integer port;
	String context;
	//int retry;
//	boolean active;
	String useragent;
	int timeout;
	String acceptlanguage;
	boolean isenabled;
	String logicalname;
	
		
	public String getLogicalname() {
		return logicalname;
	}
	public void setLogicalname(String logicalname) {
		this.logicalname = logicalname;
	}
	public boolean isIsenabled() {
		return isenabled;
	}
	public void setIsenabled(boolean isenabled) {
		this.isenabled = isenabled;
	}
	public String getAcceptlanguage() {
		return acceptlanguage;
	}
	public void setAcceptlanguage(String acceptlanguage) {
		this.acceptlanguage = acceptlanguage;
	}
	public String getUseragent() {
		return useragent;
	}
	public void setUseragent(String useragent) {
		this.useragent = useragent;
	}
	public String getIp() {
		return ip;
	}
	public void setIp(String ip) {
		this.ip = ip;
	}

	public Integer getPort() {
		return port;
	}
	public void setPort(Integer port) {
		this.port = port;
	}
	public String getContext() {
		return context;
	}
	public void setContext(String context) {
		this.context = context;
	}

	
	public int getTimeout() {
		return timeout;
	}
	public void setTimeout(int timeout) {
		this.timeout = timeout;
	}
	
	
}
