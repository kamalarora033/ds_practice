package com.ericsson.fdp.business.mcoupons;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

/**
 * The main body to create the response of getCouponList request.
 *
 */
@XmlRootElement(name = "cmscouponResponse")
public class CmsCouponListResponse {

	private CmsCoupon[] cmsCoupon;
	private Integer cmscouponResponseLength;
	private Integer responseCode;
	
	public CmsCoupon[] getCmsCoupon() {
		return cmsCoupon;
	}
	
	@XmlElement(name = "cmscouponInstance")
	public void setCmsCoupon(CmsCoupon[] cmsCoupon) {
		this.cmsCoupon = cmsCoupon;
	}
	public Integer getCmscouponResponseLength() {
		return cmscouponResponseLength;
	}
	
	@XmlElement(name = "cmscouponResponseLength")
	public void setCmscouponResponseLength(Integer cmscouponResponseLength) {
		this.cmscouponResponseLength = cmscouponResponseLength;
	}

	public Integer getResponseCode() {
		return responseCode;
	}
	
	@XmlElement(name = "responseCode")
	public void setResponseCode(Integer responseCode) {
		this.responseCode = responseCode;
	}
	
	
}