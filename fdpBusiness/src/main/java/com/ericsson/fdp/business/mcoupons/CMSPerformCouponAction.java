/*------------------------------------------------------------------------------
 *
 * File: CMSPerformCouponAction.java
 * Author: Saurabh Rai (esaurai)
 *
 *******************************************************************************
 * COPYRIGHT Ericsson 2014
 *
 * The copyright to the computer program(s) herein is the property of
 * Ericsson Inc. The programs may be used and/or copied only with written
 * permission from Ericsson Inc. or in accordance with the terms and
 * conditions stipulated in the agreement/contract under which the
 * program(s) have been supplied.
 *******************************************************************************
 *----------------------------------------------------------------------------*/
package com.ericsson.fdp.business.mcoupons;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import com.ericsson.fdp.FDPCacheable;

/**
 * This is a JAXB Annotated class used to generate the perfect XML. We have
 * provided the root elements and name of the child elements.
 * 
 */
@XmlRootElement(name = "cmscoupon")
public class CMSPerformCouponAction implements FDPCacheable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1123112314444223123L;

	String couponCode;
	
	String msisdn;
	
	String couponType;

	String couponStatus;

	String couponSubtype;

	int couponValue;

	String couponExpiry;
	
	Integer responseCode;

	public String getCouponType() {

		return couponType;
	}

	@XmlElement(name = "coupontype")
	public void setCouponType(String couponType) {
		this.couponType = couponType;
	}

	public String getCouponStatus() {
		return couponStatus;
	}

	@XmlElement(name = "couponstatus")
	public void setCouponStatus(String couponStatus) {
		this.couponStatus = couponStatus;
	}

	public String getCouponSubtype() {
		return couponSubtype;
	}

	@XmlElement(name = "couponsubtype")
	public void setCouponSubtype(String couponSubtype) {
		this.couponSubtype = couponSubtype;
	}

	public int getCouponValue() {
		return couponValue;
	}

	@XmlElement(name = "couponvalue")
	public void setCouponValue(int couponValue) {
		this.couponValue = couponValue;
	}

	public String getCouponExpiry() {
		return couponExpiry;
	}

	@XmlElement(name = "couponexpiry")
	public void setCouponExpiry(String couponExpiry) {
		this.couponExpiry = couponExpiry;
	}

	public String getCouponCode() {
		return couponCode;
	}

	@XmlElement(name = "couponcode")
	public void setCouponCode(String couponCode) {
		this.couponCode = couponCode;
	}

	public String getMsisdn() {
		return msisdn;
	}

	@XmlElement(name = "msisdn")
	public void setMsisdn(String msisdn) {
		this.msisdn = msisdn;
	}

	public Integer getResponseCode() {
		return responseCode;
	}

	@XmlElement(name = "responseCode")
	public void setResponseCode(Integer responseCode) {
		this.responseCode = responseCode;
	}
	
}
