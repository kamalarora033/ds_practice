package com.ericsson.fdp.business.charging.impl;

import com.ericsson.fdp.business.charging.Discount;
import com.ericsson.fdp.business.exception.ExpressionFailedException;
import com.ericsson.fdp.business.expression.Expression;
import com.ericsson.fdp.common.enums.DiscountTypes;
import com.ericsson.fdp.core.request.FDPRequest;

/*
Feature Name: Discounted Charging
Changes: ChargingDiscount class created which will handke discounts
Date: 18-12-2015
Singnum Id:ESIASAN
*/

/**
 * Default Implementation for discount.
 * 
 * @author ESIASAN
 *
 */
public class ChargingDiscount implements Discount {

	/**
	 *  Class serial version UID.
	 */
	private static final long serialVersionUID = 1L;
	
	Expression expression;
	
	DiscountTypes discountType;
	
	Long discountValue;
	
	public ChargingDiscount (final Expression expression, final DiscountTypes discountType, final Long discountValue) {
		this.expression = expression;
		this.discountType = discountType;
		this.discountValue = discountValue;
	}

	@Override
	public boolean isDiscountApplicable(FDPRequest fdpRequest) throws ExpressionFailedException {
		return (expression == null) ? true : expression.evaluateExpression(fdpRequest);
	}

	@Override
	public Long calculateDiscount(Long actualPrice) {
		Long discountedValue = actualPrice;
		switch (discountType) {
			case FIXED_DISCOUNT:
				discountedValue = discountValue < actualPrice ? (actualPrice - discountValue) : 0L;	
				break;
			case PERCENTAGE_DISCOUNT:
				Long discount = (discountValue * actualPrice)/100;
				discountedValue = discount < actualPrice ? (actualPrice - discount) : 0L;	
				break;
			default:
				break;
		}
		return discountedValue;
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return "ChargingDiscount [expression=" + expression + ", discountType="
				+ discountType + ", discountValue=" + discountValue + "]";
	}
}
