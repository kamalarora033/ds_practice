package com.ericsson.fdp.business.policy.policyrule.impl;

import java.util.Map;

import com.ericsson.fdp.FDPCacheable;
import com.ericsson.fdp.business.charging.value.ChargingValue;
import com.ericsson.fdp.business.enums.AuxRequestParam;
import com.ericsson.fdp.business.enums.ChargingTypeNotifications;
import com.ericsson.fdp.business.enums.ConfigurationKey;
import com.ericsson.fdp.business.enums.RequestMetaValuesKey;
import com.ericsson.fdp.business.enums.TLVOptions;
import com.ericsson.fdp.business.mcoupons.CmsCoupon;
import com.ericsson.fdp.business.product.Product;
import com.ericsson.fdp.business.product.ProductCouponCache;
import com.ericsson.fdp.business.request.impl.FDPResponseImpl;
import com.ericsson.fdp.business.util.RequestUtil;
import com.ericsson.fdp.business.util.ResponseUtil;
import com.ericsson.fdp.common.constants.FDPConstant;
import com.ericsson.fdp.common.enums.ModuleType;
import com.ericsson.fdp.common.enums.Status;
import com.ericsson.fdp.core.exception.ExecutionFailedException;
import com.ericsson.fdp.core.request.FDPRequest;
import com.ericsson.fdp.core.request.FDPResponse;
import com.ericsson.fdp.core.request.impl.FDPRequestImpl;
import com.ericsson.fdp.core.serviceprovisioning.bean.FDPMetaBag;
import com.ericsson.fdp.core.utils.ApplicationConfigUtil;
import com.ericsson.fdp.dao.dto.product.ProductCouponDTO;

public class CouponConfirmationPolicyRuleImpl extends ProductPolicyRuleImpl{

	/**
	 * 
	 */
	private static final long serialVersionUID = 22309876543231L;
	
	@Override
	public FDPResponse displayRule(FDPRequest fdpRequest) throws ExecutionFailedException {
		CmsCoupon appliedCoupon = (CmsCoupon)fdpRequest.getAuxiliaryRequestParameter(AuxRequestParam.CONSUMER_APPLIED_COUPON);
		
		if(appliedCoupon == null){
			throw new ExecutionFailedException("Applied Coupon Not found in FDPRequest.");
		}
		
		FDPResponse fdpResponse = null;
		final String notificationText = this.getNotificationText(fdpRequest);
		
		String couponConfirmationText = "";
		
		switch(appliedCoupon.getCouponTypeName()){
		case GLOBAL:
			couponConfirmationText = getCouponConfirmationTextForGlobal(fdpRequest, appliedCoupon);
			break;
		case PRODUCT_LINKED:
			couponConfirmationText = getCouponConfirmationTextForProductLinked(fdpRequest, appliedCoupon);
			break;
		}
		
		if (notificationText != null) {
			fdpResponse = new FDPResponseImpl(Status.SUCCESS, false, ResponseUtil.createResponseMessageInList(
					fdpRequest.getChannel(),couponConfirmationText + FDPConstant.NEWLINE + notificationText, TLVOptions.SESSION_CONTINUE));
		}
		
		return fdpResponse;
	}
	
	
	private String getCouponConfirmationTextForProductLinked(FDPRequest fdpRequest, CmsCoupon appliedCoupon) throws ExecutionFailedException{
		StringBuilder notificationText = new StringBuilder("");
		
		final FDPCacheable fdpLogicalNameCached = ApplicationConfigUtil.getMetaDataCache().getValue(
				new FDPMetaBag(fdpRequest.getCircle(), ModuleType.PRODUCT_COUPON_CODE, null));
		
		if(fdpLogicalNameCached != null && fdpLogicalNameCached instanceof ProductCouponCache){
			ProductCouponCache logicalNameCache = (ProductCouponCache)fdpLogicalNameCached;
			 ProductCouponDTO dto = logicalNameCache.getProductCouponMap().get(appliedCoupon.getProductCode().toLowerCase());
			 if(dto != null){
				 notificationText.append(dto.getNotifSuccess());
			 }
		}
		
		return notificationText.toString();
	}
	
	private String getCouponConfirmationTextForGlobal(FDPRequest fdpRequest, CmsCoupon appliedCoupon) throws ExecutionFailedException{
		StringBuilder textBuilder = new StringBuilder("");
		
		if(appliedCoupon == null){
			throw new ExecutionFailedException("Applied Coupon Not found in FDPRequest.");
		}
		final String couponValueMacro = "${Value}";
		final Map<String, String> configurationMap = fdpRequest.getCircle().getConfigurationKeyValueMap();
		String key = null;
		switch(appliedCoupon.getCouponSubtypeName()){
		case EXTRABENEFIT_ABSAMT:
			key = ConfigurationKey.COUPON_CONF_TEXT_FOR_EXTRA_BENEFIT_ABSAMT.getAttributeName();
			break;
		case EXTRABENEFIT_PERCAMT:
			key = ConfigurationKey.COUPON_CONF_TEXT_FOR_EXTRA_BENEFIT_PERCAMT.getAttributeName();
			break;
		case MRPDIS_ABSAMT:
			key = ConfigurationKey.COUPON_CONF_TEXT_FOR_MRPDIS_ABSAMT.getAttributeName();
			break;
		case MRPDIS_PERCAMT:
			key = ConfigurationKey.COUPON_CONF_TEXT_FOR_MRPDIS_PERCAMT.getAttributeName();
		}
		
		if(key != null){
			textBuilder.append(configurationMap.get(key));
		}
		return textBuilder.toString().replace(couponValueMacro, appliedCoupon.getCouponValue().toString());
	}
	
	@Override
	protected String getNotificationText(final FDPRequest fdpRequest) throws ExecutionFailedException {
		final Product product = RequestUtil.getMetaValueFromRequest(fdpRequest, RequestMetaValuesKey.PRODUCT,
				Product.class);
		final ChargingValue chargingAmount = RequestUtil.getMetaValueFromRequest(fdpRequest,
				RequestMetaValuesKey.CHARGING_STEP, ChargingValue.class);
		String textString = super.getNotificationText(product, chargingAmount, fdpRequest);
		if(textString == null){
			textString = super.getNotificationTextForChargingType(product, fdpRequest, ChargingTypeNotifications.POSITIVE_CHARGING_BUY_PRODUCT);
		}
		return textString;
	}

}
