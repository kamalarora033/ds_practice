package com.ericsson.fdp.business.vo;

import javax.persistence.Column;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToOne;

import com.ericsson.fdp.FDPCacheable;
import com.ericsson.fdp.dao.entity.FDPCommandName;
import com.ericsson.fdp.dao.entity.FDPNotifications;

public class FDPAsycCommandVO implements FDPCacheable {

	private static final long serialVersionUID = 1L;

	private Long asyncCommandID;
	// private String asyncHandlerclass;
	private String commanddiscplayname;
	private Long notificationid;
	private String transactionparam;
	private String transactionparamtype;
	private boolean notificationstatus;


	
	
	public boolean isNotificationstatus() {
		return notificationstatus;
	}

	public void setNotificationstatus(boolean notificationstatus) {
		this.notificationstatus = notificationstatus;
	}

	public String getTransactionparam() {
		return transactionparam;
	}

	public void setTransactionparam(String transactionparam) {
		this.transactionparam = transactionparam;
	}

	public String getTransactionparamtype() {
		return transactionparamtype;
	}

	public void setTransactionparamtype(String transactionparamtype) {
		this.transactionparamtype = transactionparamtype;
	}

	public Long getAsyncCommandID() {
		return asyncCommandID;
	}

	public void setAsyncCommandID(Long asyncCommandID) {
		this.asyncCommandID = asyncCommandID;
	}

	/*
	 * public String getAsyncHandlerclass() { return asyncHandlerclass; } public
	 * void setAsyncHandlerclass(String asyncHandlerclass) {
	 * this.asyncHandlerclass = asyncHandlerclass; }
	 */
	public String getCommanddiscplayname() {
		return commanddiscplayname;
	}

	public void setCommanddiscplayname(String commanddiscplayname) {
		this.commanddiscplayname = commanddiscplayname;
	}

	public Long getNotificationid() {
		return notificationid;
	}

	public void setNotificationid(Long notificationid) {
		this.notificationid = notificationid;
	}

}
