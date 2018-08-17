package com.ericsson.fdp.business.mcoupons;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import com.ericsson.fdp.FDPCacheable;
import com.ericsson.fdp.business.enums.mcoupon.CouponBenefitNature;
import com.ericsson.fdp.business.enums.mcoupon.CouponSubType;

@XmlRootElement(name = "cmscouponInstance")
public class CmsCoupon implements FDPCacheable{

	
	private static final long serialVersionUID = 219298928189323431L;
	
	private String subsMsisdn;
	private String couponCode;
	private long couponTempId;
	private CouponType couponTypeName;
	private CouponSubType couponSubtypeName;
	private String clientTranId;
	private String productCode;
	private String couponBenefitCatg;
	private CouponBenefitNature couponBenefitNature;
	private Integer couponValue;
	private Integer couponValueMax;
	private String couponExpiryDate;
	private int usageLimit;
	private int usageAvailed;
	private String couponStatus;
	private String lastUsage;
	private String createdOn;
	
	
	public CmsCoupon(){}
	
	public long getCouponTempId() {
		return this.couponTempId;
	}

	@XmlElement(name = "couponTempId")
	public void setCouponTempId(long couponTempId) {
		this.couponTempId = couponTempId;
	}

	
	public String getClientTranId() {
		return this.clientTranId;
	}

	@XmlElement(name = "clientTranId")
	public void setClientTranId(String clientTranId) {
		this.clientTranId = clientTranId;
	}

	
	public String getProductCode() {
		return this.productCode;
	}

	@XmlElement(name = "productCode")
	public void setProductCode(String productCode) {
		this.productCode = productCode;
	}

	
	public String getCouponBenefitCatg() {
		return this.couponBenefitCatg;
	}

	@XmlElement(name = "couponBenefitCatg")
	public void setCouponBenefitCatg(String couponBenefitCatg) {
		this.couponBenefitCatg = couponBenefitCatg;
	}

	
	public CouponBenefitNature getCouponBenefitNature() {
		return this.couponBenefitNature;
	}

	@XmlElement(name = "couponBenefitNature")
	public void setCouponBenefitNature(CouponBenefitNature couponBenefitNature) {
		this.couponBenefitNature = couponBenefitNature;
	}

	
	public Integer getCouponValue() {
		return this.couponValue;
	}

	@XmlElement(name = "couponValue")
	public void setCouponValue(Integer couponValue) {
		this.couponValue = couponValue;
	}

	
	public Integer getCouponValueMax() {
		return this.couponValueMax;
	}

	@XmlElement(name = "couponValueMax")
	public void setCouponValueMax(Integer couponValueMax) {
		this.couponValueMax = couponValueMax;
	}

	
	public String getCouponExpiryDate() {
		return this.couponExpiryDate;
	}

	@XmlElement(name = "couponExpiryDate")
	public void setCouponExpiryDate(String couponExpiryDate) {
		this.couponExpiryDate = couponExpiryDate;
	}

	
	public int getUsageLimit() {
		return this.usageLimit;
	}

	@XmlElement(name = "usageLimit")
	public void setUsageLimit(int usageLimit) {
		this.usageLimit = usageLimit;
	}

	
	public int getUsageAvailed() {
		return this.usageAvailed;
	}

	@XmlElement(name = "usageAvailed")
	public void setUsageAvailed(int usageAvailed) {
		this.usageAvailed = usageAvailed;
	}

	
	public String getCouponStatus() {
		return this.couponStatus;
	}

	@XmlElement(name = "couponStatus")
	public void setCouponStatus(String couponStatus) {
		this.couponStatus = couponStatus;
	}

	
	public String getLastUsage() {
		return this.lastUsage;
	}

	@XmlElement(name = "lastUsage")
	public void setLastUsage(String lastUsage) {
		this.lastUsage = lastUsage;
	}

	public String getCreatedOn() {
		return this.createdOn;
	}

	@XmlElement(name = "createdOn")
	public void setCreatedOn(String createdOn) {
		this.createdOn = createdOn;
	}


	public String getSubsMsisdn() {
		return subsMsisdn;
	}

	@XmlElement(name = "subsMsisdn")
	public void setSubsMsisdn(String subsMsisdn) {
		this.subsMsisdn = subsMsisdn;
	}

	
	public String getCouponCode() {
		return couponCode;
	}

	@XmlElement(name = "couponCode")
	public void setCouponCode(String couponCode) {
		this.couponCode = couponCode;
	}

	public CouponType getCouponTypeName() {
		return couponTypeName;
	}

	@XmlElement(name = "couponTypeName")
	public void setCouponTypeName(CouponType couponTypeName) {
		this.couponTypeName = couponTypeName;
	}

	public CouponSubType getCouponSubtypeName() {
		return couponSubtypeName;
	}

	@XmlElement(name = "couponSubtypeName")
	public void setCouponSubtypeName(CouponSubType couponSubtypeName) {
		this.couponSubtypeName = couponSubtypeName;
	}


	public CmsCoupon(String subsMsisdn, String couponCode, long couponTempId,
			CouponType couponTypeName, CouponSubType couponSubtypeName,
			String clientTranId, String productCode, String couponBenefitCatg,
			CouponBenefitNature couponBenefitNature, int couponValue, 
			Integer couponValueMax, String couponExpiryDate, int usageLimit,
			int usageAvailed, String couponStatus, String lastUsage,
			String createdOn) {
		super();
		this.subsMsisdn = subsMsisdn;
		this.couponCode = couponCode;
		this.couponTempId = couponTempId;
		this.couponTypeName = couponTypeName;
		this.couponSubtypeName = couponSubtypeName;
		this.clientTranId = clientTranId;
		this.productCode = productCode;
		this.couponBenefitCatg = couponBenefitCatg;
		this.couponBenefitNature = couponBenefitNature;
		this.couponValue = couponValue;
		this.couponValueMax = couponValueMax;
		this.couponExpiryDate = couponExpiryDate;
		this.usageLimit = usageLimit;
		this.usageAvailed = usageAvailed;
		this.couponStatus = couponStatus;
		this.lastUsage = lastUsage;
		this.createdOn = createdOn;
	}

	
}