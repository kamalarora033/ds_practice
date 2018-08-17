package com.ericsson.fdp.business.response.fulfillment.xml;

import java.io.Serializable;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement(name = "daDetails")
@XmlAccessorType(XmlAccessType.FIELD)
public class DaDetails implements Serializable{

	private static final long serialVersionUID = 128242602729091610L;

	@XmlElement(name = "daId")
	private String daId;
	
	@XmlElement(name = "daValue")
	private String daValue;
	
	@XmlElement(name = "startDate")
	private String startDate;
	
	@XmlElement(name = "endDate")
	private String endDate;
	
	@XmlElement(name = "unitType")
	private String unitType;
	
	public DaDetails(){}
	
	public DaDetails(String daId, String daValue, String startDate, String endDate, String unitType){
		this.daId = daId;
		this.daValue = daValue;
		this.startDate = startDate;
		this.endDate = endDate;
		this.unitType = unitType;	
	}

	public String getDaId() {
		return daId;
	}

	public void setDaId(String daId) {
		this.daId = daId;
	}

	public String getDaValue() {
		return daValue;
	}

	public void setDaValue(String daValue) {
		this.daValue = daValue;
	}

	public String getStartDate() {
		return startDate;
	}

	public void setStartDate(String startDate) {
		this.startDate = startDate;
	}

	public String getEndDate() {
		return endDate;
	}

	public void setEndDate(String endDate) {
		this.endDate = endDate;
	}

	public String getUnitType() {
		return unitType;
	}

	public void setUnitType(String unitType) {
		this.unitType = unitType;
	}
}
