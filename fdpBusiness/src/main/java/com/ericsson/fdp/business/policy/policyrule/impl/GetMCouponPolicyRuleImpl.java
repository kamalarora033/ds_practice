package com.ericsson.fdp.business.policy.policyrule.impl;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;

import com.ericsson.fdp.FDPCacheable;
import com.ericsson.fdp.business.charging.FDPChargingSystem;
import com.ericsson.fdp.business.charging.value.ChargingValue;
import com.ericsson.fdp.business.enums.AuxRequestParam;
import com.ericsson.fdp.business.enums.Command;
import com.ericsson.fdp.business.enums.PolicyStatus;
import com.ericsson.fdp.business.enums.RequestMetaValuesKey;
import com.ericsson.fdp.business.enums.mcoupon.CouponPolicyIndex;
import com.ericsson.fdp.business.mcoupons.CMSPerformCouponAction;
import com.ericsson.fdp.business.mcoupons.CmsCoupon;
import com.ericsson.fdp.business.mcoupons.CmsCouponListResponse;
import com.ericsson.fdp.business.policy.policyrule.PolicyRule;
import com.ericsson.fdp.business.product.Product;
import com.ericsson.fdp.business.request.FDPPolicyResponse;
import com.ericsson.fdp.business.request.impl.FDPPolicyResponseImpl;
import com.ericsson.fdp.business.util.RequestUtil;
import com.ericsson.fdp.business.vo.ProductCouponMapCacheDTO;
import com.ericsson.fdp.common.enums.ModuleType;
import com.ericsson.fdp.common.enums.Status;
import com.ericsson.fdp.core.command.FDPCommand;
import com.ericsson.fdp.core.command.param.CommandParam;
import com.ericsson.fdp.core.exception.ExecutionFailedException;
import com.ericsson.fdp.core.request.FDPRequest;
import com.ericsson.fdp.core.request.FDPResponse;
import com.ericsson.fdp.core.request.impl.FDPRequestImpl;
import com.ericsson.fdp.core.serviceprovisioning.bean.FDPMetaBag;
import com.ericsson.fdp.core.utils.ApplicationConfigUtil;
import com.ericsson.fdp.dao.entity.FDPProduct;

public class GetMCouponPolicyRuleImpl extends AbstractPolicyRule implements PolicyRule {

	/**
	 * 
	 */
	private static final long serialVersionUID = 165645645645645645L;
	
	
	/**
	 * Amount for zero charging.
	 */
	protected static final String ZERO_CHARGING_AMOUNT = "0";
	

	@Override
	public FDPResponse displayRule(FDPRequest fdpRequest)
			throws ExecutionFailedException {
		return null;
	}

	@Override
	public FDPPolicyResponse validatePolicyRule(Object input, FDPRequest fdpRequest, Object... otherParams)
			throws ExecutionFailedException {
		
		Product product = null;
		List<CmsCoupon> couponList = null;
		
		FDPPolicyResponse response = null;
		product = RequestUtil.getMetaValueFromRequest(fdpRequest, RequestMetaValuesKey.PRODUCT,	Product.class);
		
		//If charging amount is not zero then get coupons.
		if(!isChargingZero(fdpRequest)){
			couponList = getSubscriberCouponsFromCMS(fdpRequest);
		}
		else{
			response = new FDPPolicyResponseImpl(PolicyStatus.SKIP_EXECUTION, null, null, false, null);
		}
		
		if(response == null){
			response = new FDPPolicyResponseImpl(PolicyStatus.GOTO_RULE, null, null, false, null);
			
			if(couponList != null && couponList.size() > 0 && product != null){
				if(fdpRequest instanceof FDPRequestImpl){
					((FDPRequestImpl)fdpRequest).putAuxiliaryRequestParameter(AuxRequestParam.CONSUMER_MCOUPON_LIST, couponList);
				}
				if(validateCouponListWithProduct(fdpRequest, product, couponList)){
					response.setNextRuleIndex(CouponPolicyIndex.PRODUCT_BUY_WITH_COUPON_POLICY_RULE.getIndex());
				}
				else{
					response.setNextRuleIndex(CouponPolicyIndex.PRODUCT_BUY_POLICY_RULE.getIndex());
				}
			}
			else{
				response.setNextRuleIndex(CouponPolicyIndex.PRODUCT_BUY_POLICY_RULE.getIndex());
			}
		}
		
		return response;
	}
	
	/**
	 * Returns true if charging amount is zero.
	 * @param request
	 * @return
	 * @throws ExecutionFailedException
	 */
	private boolean isChargingZero(FDPRequest request) throws ExecutionFailedException{
		final ChargingValue chargingAmount = RequestUtil.getMetaValueFromRequest(request,
				RequestMetaValuesKey.CHARGING_STEP, ChargingValue.class);
		return ZERO_CHARGING_AMOUNT.equals(chargingAmount.getChargingValue().toString());
	}
	
	
	private boolean validateCouponListWithProduct(FDPRequest request, Product product, List<CmsCoupon> couponList) throws ExecutionFailedException{
		Long productId = product.getProductId();
		boolean matched = false;
		List<String> productLinkedLogicalNameList = null;
		FDPCacheable fdpCacheDto = ApplicationConfigUtil.getMetaDataCache().getValue(new FDPMetaBag(request.getCircle(), ModuleType.PRODUCT_COUPON_MAP, null));
		
		if(fdpCacheDto != null && fdpCacheDto instanceof ProductCouponMapCacheDTO){
			ProductCouponMapCacheDTO productCouponCacheDto = (ProductCouponMapCacheDTO)fdpCacheDto;
			productLinkedLogicalNameList = productCouponCacheDto.getProductCouponMap().get(productId);
		}
		
		
		loop: for(CmsCoupon coupon : couponList){
			switch(coupon.getCouponTypeName()){
			case PRODUCT_LINKED:
				if(checkInListString(coupon.getProductCode(), productLinkedLogicalNameList)){
					matched = true;
					break loop;
				}
				break;
			case GLOBAL:
				matched = true;
				break loop;
			}
		}
		
		
		return matched;
	}
	
	private boolean checkInListString(String logicalCode, List<String> logicalCodeStringList){
		if(logicalCodeStringList == null)
			return false;
		
		for(String str : logicalCodeStringList){
			if(str.equalsIgnoreCase(logicalCode))
				return true;
		}
		return false;
	}
	
	
	private List<CmsCoupon> getSubscriberCouponsFromCMS(FDPRequest request) throws ExecutionFailedException{
		List<CmsCoupon> cmsCouponList = null;
		FDPCommand cmsFdpCommand = null;
		final String getCouponCommandName = Command.CMS_GET_SUBSCRIBER_COPOUNS.getCommandDisplayName();
		final FDPCacheable fdpCommandCached = ApplicationConfigUtil.getMetaDataCache().getValue(
				new FDPMetaBag(request.getCircle(), ModuleType.COMMAND, getCouponCommandName));
		
		if(fdpCommandCached != null && fdpCommandCached instanceof FDPCommand){
			cmsFdpCommand = (FDPCommand)fdpCommandCached;
			Status commandStatus = cmsFdpCommand.execute(request);
			if(commandStatus.equals(Status.SUCCESS)){
				Object cmsCouponListParamObject = cmsFdpCommand.getOutputParams().get("USER_MCOUPON_LIST".toLowerCase());
				if(cmsCouponListParamObject != null && cmsCouponListParamObject instanceof CommandParam){
					CommandParam commandParam = (CommandParam) cmsCouponListParamObject;
					if(commandParam.getValue() != null && commandParam.getValue() instanceof CmsCoupon[]){
						cmsCouponList = new ArrayList<CmsCoupon>();
						CmsCoupon[] cmsCouponArray = (CmsCoupon[]) commandParam.getValue();
						for(CmsCoupon coupon : cmsCouponArray){
							cmsCouponList.add(coupon);
						}
					}
				}
			}
		}
		
		return cmsCouponList;
	}

}
