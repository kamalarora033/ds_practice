package com.ericsson.fdp.business.policy.policyrule.impl;

import java.util.List;
import java.util.Map;

import com.ericsson.fdp.FDPCacheable;
import com.ericsson.fdp.business.charging.value.ChargingValue;
import com.ericsson.fdp.business.charging.value.ChargingValueImpl;
import com.ericsson.fdp.business.enums.AuxRequestParam;
import com.ericsson.fdp.business.enums.PolicyStatus;
import com.ericsson.fdp.business.enums.RequestMetaValuesKey;
import com.ericsson.fdp.business.enums.TLVOptions;
import com.ericsson.fdp.business.enums.mcoupon.CouponPolicyIndex;
import com.ericsson.fdp.business.policy.policyrule.PolicyRule;
import com.ericsson.fdp.business.product.Product;
import com.ericsson.fdp.business.request.FDPPolicyResponse;
import com.ericsson.fdp.business.mcoupons.CmsCoupon;
import com.ericsson.fdp.business.request.impl.FDPPolicyResponseImpl;
import com.ericsson.fdp.business.request.impl.FDPResponseImpl;
import com.ericsson.fdp.business.util.RequestUtil;
import com.ericsson.fdp.business.util.ResponseUtil;
import com.ericsson.fdp.business.vo.ProductCouponMapCacheDTO;
import com.ericsson.fdp.common.enums.ExternalSystem;
import com.ericsson.fdp.common.enums.ModuleType;
import com.ericsson.fdp.common.enums.Status;
import com.ericsson.fdp.core.exception.ExecutionFailedException;
import com.ericsson.fdp.core.request.FDPRequest;
import com.ericsson.fdp.core.request.FDPResponse;
import com.ericsson.fdp.core.request.impl.FDPRequestImpl;
import com.ericsson.fdp.core.serviceprovisioning.bean.FDPMetaBag;
import com.ericsson.fdp.core.utils.ApplicationConfigUtil;

public class ApplyCouponPolicyRuleImpl extends AbstractPolicyRule implements PolicyRule {

	/**
	 * 
	 */
	private static final long serialVersionUID = 323223232323212311L;

	@Override
	public FDPResponse displayRule(FDPRequest fdpRequest)
			throws ExecutionFailedException {
		FDPResponse fdpResponse = null;
		if (this.getPolicyRuleText() != null) {
			fdpResponse = new FDPResponseImpl(Status.SUCCESS, false, ResponseUtil.createResponseMessageInList(
					fdpRequest.getChannel(), this.getPolicyRuleText(), TLVOptions.SESSION_CONTINUE));
		}
		else{
			fdpResponse = new FDPResponseImpl(Status.SUCCESS, false, ResponseUtil.createResponseMessageInList(
					fdpRequest.getChannel(), "Enter coupon code", TLVOptions.SESSION_CONTINUE));
		}
		return fdpResponse;
	}

	@SuppressWarnings("unchecked")
	@Override
	public FDPPolicyResponse validatePolicyRule(Object input, FDPRequest fdpRequest, Object... otherParams)
			throws ExecutionFailedException {
		Object couponListObject = fdpRequest.getAuxiliaryRequestParameter(AuxRequestParam.CONSUMER_MCOUPON_LIST);
		List<CmsCoupon> couponList = null;
		FDPPolicyResponse response = new FDPPolicyResponseImpl(PolicyStatus.SUCCESS, null, null, true, null);
		if(couponListObject != null && couponListObject instanceof List<?>){
			couponList = (List<CmsCoupon>) couponListObject;
			CmsCoupon couponFound = null;
			for(CmsCoupon coupon : couponList){
				if(IsValidCoupon(coupon, (String)input, fdpRequest)){
					couponFound = coupon;
					break;
				}
			}
			if(couponFound != null){
				((FDPRequestImpl)fdpRequest).putAuxiliaryRequestParameter(AuxRequestParam.CONSUMER_APPLIED_COUPON, couponFound);
				((FDPRequestImpl)fdpRequest).putAuxiliaryRequestParameter(AuxRequestParam.CONSUMER_APPLIED_COUPON_CODE, couponFound.getCouponCode());
				applyCouponAndUpdateCharging(fdpRequest);
			}
			else{
				response = new FDPPolicyResponseImpl(PolicyStatus.GOTO_RULE, null, null, true, null);
				response.setNextRuleIndex(CouponPolicyIndex.PRODUCT_BUY_WITH_COUPON_POLICY_RULE.getIndex());
			}
		}
		else{
			throw new ExecutionFailedException("Coupons Not available in Request Object.");
		}
		return response;
	}
	
	private boolean IsValidCoupon(CmsCoupon coupon, String InputCouponCode, FDPRequest fdpRequest) throws ExecutionFailedException{
		boolean isValid = false;
		Product product = RequestUtil.getMetaValueFromRequest(fdpRequest, RequestMetaValuesKey.PRODUCT,	Product.class);
		
		if(coupon.getCouponCode().equals(InputCouponCode)){
			switch(coupon.getCouponTypeName()){
			case GLOBAL:
				isValid = true;
				break;
			case PRODUCT_LINKED:
				isValid = isMatchingWithProduct(fdpRequest, product, coupon);
				break;
			}
			//isValid = true;
		}
		return isValid;
	}
	
	private boolean isMatchingWithProduct(FDPRequest request, Product product, CmsCoupon coupon) throws ExecutionFailedException{
		Long productId = product.getProductId();
		boolean matched = false;
		List<String> productLinkedLogicalNameList = null;
		FDPCacheable fdpCacheDto = ApplicationConfigUtil.getMetaDataCache().getValue(new FDPMetaBag(request.getCircle(), ModuleType.PRODUCT_COUPON_MAP, null));
		
		if(fdpCacheDto != null && fdpCacheDto instanceof ProductCouponMapCacheDTO){
			ProductCouponMapCacheDTO productCouponCacheDto = (ProductCouponMapCacheDTO)fdpCacheDto;
			productLinkedLogicalNameList = productCouponCacheDto.getProductCouponMap().get(productId);
		}
		
		if(productLinkedLogicalNameList != null){
			for(String logicalName : productLinkedLogicalNameList){
				if(logicalName.equalsIgnoreCase(coupon.getProductCode())){
					matched = true;
					break;
				}
			}
		}
		
		return matched;
	}
	
	private void applyCouponAndUpdateCharging(FDPRequest fdpRequest) throws ExecutionFailedException{
		CmsCoupon appliedCoupon = (CmsCoupon)fdpRequest.getAuxiliaryRequestParameter(AuxRequestParam.CONSUMER_APPLIED_COUPON);
		switch(appliedCoupon.getCouponSubtypeName()){
		case MRPDIS_PERCAMT:
		case MRPDIS_ABSAMT:
			final ChargingValue chargingValue = RequestUtil.getMetaValueFromRequest(fdpRequest, RequestMetaValuesKey.CHARGING_STEP, ChargingValue.class);
			Integer updatedChargingValue = getDiscountedAmount(appliedCoupon, chargingValue);
			((ChargingValueImpl)chargingValue).setChargingValue(updatedChargingValue.toString());
			break;
		case EXTRABENEFIT_ABSAMT:
			if(fdpRequest instanceof FDPRequestImpl){
				((FDPRequestImpl)fdpRequest).putAuxiliaryRequestParameter(AuxRequestParam.EXTERNAL_DATA_VALUE_FOR_EXTRA_ABSAMT, appliedCoupon.getCouponValue());
			}
			break;
		case EXTRABENEFIT_PERCAMT:
			if(fdpRequest instanceof FDPRequestImpl){
				((FDPRequestImpl)fdpRequest).putAuxiliaryRequestParameter(AuxRequestParam.EXTERNAL_DATA_VALUE_FOR_EXTRA_PERCAMT, appliedCoupon.getCouponValue());
			}
			break;
		}
	}
	
	private Integer getDiscountedAmount(CmsCoupon coupon, ChargingValue chargingValue) throws ExecutionFailedException{
		Integer updatedChargingAmount = 0;
		Integer chargingAmount = Integer.parseInt(chargingValue.getChargingValue().toString());
		Integer discountValue = coupon.getCouponValue();
		
		//Make discount value negative in case of AIR charging.
		if(chargingValue.getExternalSystemToUse().equals(ExternalSystem.AIR)){
			discountValue = - coupon.getCouponValue();
		}
		
		switch(coupon.getCouponBenefitNature()){
		case FIX:
			updatedChargingAmount = chargingAmount - discountValue;
			break;
		case PER:
			updatedChargingAmount = chargingAmount - (chargingAmount * coupon.getCouponValue() / 100);
			break;
		default:
				throw new ExecutionFailedException("Coupon Benefit Nature not recognized.");
		}
		
		//Make sure the charging amount should not be greater than 0 in case of AIR
		if(chargingValue.getExternalSystemToUse().equals(ExternalSystem.AIR)){
			updatedChargingAmount = updatedChargingAmount > 0 ? 0 : updatedChargingAmount;
		}
		else{
			updatedChargingAmount = updatedChargingAmount < 0 ? 0 : updatedChargingAmount;
		}
		
		return updatedChargingAmount;
	}
}
