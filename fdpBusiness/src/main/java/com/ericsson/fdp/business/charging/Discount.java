package com.ericsson.fdp.business.charging;

import java.io.Serializable;

import com.ericsson.fdp.FDPCacheable;
import com.ericsson.fdp.business.exception.ExpressionFailedException;
import com.ericsson.fdp.core.request.FDPRequest;

/**
 * Interface for Discount.
 * 
 * @author ESIASAN
 *
 */
public interface Discount extends Serializable,FDPCacheable {

	/**
	 * Checks the eligibility for discount.
	 * 
	 * @param fdpRequest
	 * @return
	 */
	boolean isDiscountApplicable(final FDPRequest fdpRequest) throws ExpressionFailedException;
 
	/**
	 * Calculate the Discount.
	 * 
	 * @param actualPrice
	 * @return
	 */
	Long calculateDiscount(final Long actualPrice);
}
