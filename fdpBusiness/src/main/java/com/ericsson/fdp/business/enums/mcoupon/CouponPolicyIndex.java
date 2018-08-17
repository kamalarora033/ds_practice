package com.ericsson.fdp.business.enums.mcoupon;

public enum CouponPolicyIndex {
	
	GET_COUPON_LIST_POLICY_RULE(0), PRODUCT_BUY_POLICY_RULE(1), PRODUCT_BUY_WITH_COUPON_POLICY_RULE(2),
	PRODUCT_BUY_APPLY_COUPON_RULE(3), PRODUCT_BUY_COUPON_CONFIRMATION_RULE(4), LOAN_PROCESS_POLICY_RULE(5);
	
	private Integer index;
	
	private CouponPolicyIndex(Integer index){
		this.index = index;
	}
	
	public Integer getIndex(){
		return index;
	}
}
