package com.ericsson.fdp.AirConfig.pojo;

import java.util.List;

import com.ericsson.fdp.FDPCacheable;
import com.ericsson.fdp.AirConfig.parser.RefillCategoryEnum;

/**
 * Pojo for Refill data
 * */
public class Refill implements FDPCacheable{

	private String refillname;
	private String refillID;
	private RefillCategoryEnum refillcatgory;
	private List<String> serviceids;
	private String offerid;
	
	
	public String getOfferid() {
		return offerid;
	}

	public void setOfferid(String offerid) {
		this.offerid = offerid;
	}

	public String getRefillname() {
		return refillname;
	}

	public void setRefillname(String refillname) {
		this.refillname = refillname;
	}

	public String getRefillID() {
		return refillID;
	}

	public void setRefillID(String refillID) {
		this.refillID = refillID;
	}

	public RefillCategoryEnum getRefillcatgory() {
		return refillcatgory;
	}

	public void setRefillcatgory(RefillCategoryEnum refillcatgory) {
		this.refillcatgory = refillcatgory;
	}

	public List<String> getServiceids() {
		return serviceids;
	}

	public void setServiceids(List<String> serviceids) {
		this.serviceids = serviceids;
	}

}
