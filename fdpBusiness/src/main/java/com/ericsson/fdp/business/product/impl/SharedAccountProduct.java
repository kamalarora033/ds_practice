package com.ericsson.fdp.business.product.impl;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.ericsson.fdp.business.charging.ProductCharging;
import com.ericsson.fdp.business.enums.SharedType;
import com.ericsson.fdp.business.enums.State;
import com.ericsson.fdp.business.expression.Expression;
import com.ericsson.fdp.common.enums.ChannelType;
import com.ericsson.fdp.common.enums.ProductType;
import com.ericsson.fdp.dao.enums.ChargingType;

/**
 * This class implements the shared account product.
 * 
 * @author Ericsson
 * 
 */
public class SharedAccountProduct extends BaseProduct {

	/**
	 * 
	 */
	private static final long serialVersionUID = 66046301717825613L;

	/**
	 * The number of consumers.
	 */
	private Integer noOfConsumer;

	/**
	 * The shared type of the product.
	 */
	private SharedType sharedType;

	/**
	 * The validity days.
	 */
	private String validityPeriodInDays;

	/**
	 * This map contains product meta data.
	 */
	private Map<String, String> productMetaData;

	/**
	 * Instantiates a new shared account product.
	 * 
	 * @param productId
	 *            the product id value
	 * @param productName
	 *            the name of the product
	 * @param constraints
	 *            a map of product constraints with key as {@link ChannelType}
	 *            and value as an {@link Expression} object.
	 * @param charging
	 *            a map of product charging with key as {@link ChannelType} and
	 *            value as list of {@link ProductCharging} objects
	 * @param noOfConsumer
	 *            the no of consumer
	 * @param validityPeriodInDays
	 *            the validity period in days
	 * @param productNotificationMap 
	 */
	public SharedAccountProduct(Long productId, String productName, Map<ChannelType, Expression> constraints,
			Map<ChannelType, Map<ChargingType, List<ProductCharging>>> charging, Integer noOfConsumer,
			SharedType sharedType, String validityPeriodInDays, ProductType productType,
			Map<ChannelType, String> channelTypeNameToSet, Integer reccuringTimes, State productState, Map<String, Long> productNotificationMap) {
		super(productId, productName, constraints, charging, productType, channelTypeNameToSet, reccuringTimes,
				productState, productNotificationMap);
		this.noOfConsumer = noOfConsumer;
		this.sharedType = sharedType;
		this.validityPeriodInDays = validityPeriodInDays;
	}

	/**
	 * @return the noOfConsumer
	 */
	public Integer getNoOfConsumer() {
		return noOfConsumer;
	}

	/**
	 * @param noOfConsumerToSet
	 *            the noOfConsumer to set
	 */
	public void setNoOfConsumer(final Integer noOfConsumerToSet) {
		this.noOfConsumer = noOfConsumerToSet;
	}

	/**
	 * @return the sharedType
	 */
	public SharedType getSharedType() {
		return sharedType;
	}

	/**
	 * @param sharedTypeToSet
	 *            the sharedType to set
	 */
	public void setSharedType(final SharedType sharedTypeToSet) {
		this.sharedType = sharedTypeToSet;
	}

	/**
	 * @return the validityPeriodInDays
	 */
	public String getValidityPeriodInDays() {
		return validityPeriodInDays;
	}

	/**
	 * @param validityPeriodInDaysToSet
	 *            the validityPeriodInDays to set
	 */
	public void setValidityPeriodInDays(final String validityPeriodInDaysToSet) {
		this.validityPeriodInDays = validityPeriodInDaysToSet;
	}

	/**
	 * This method is used to get the product meta data.
	 * 
	 * @return the product meta data.
	 */
	public Map<String, String> getProductMetaData() {
		return productMetaData;
	}

	/**
	 * This method is used to get the product meta data.
	 * 
	 * @param key
	 *            the key for the meta data.
	 * @param value
	 *            the value of the meta data.
	 */
	public void addProductMetaData(String key, String value) {
		if (productMetaData == null) {
			productMetaData = new HashMap<String, String>();
		}
		productMetaData.put(key, value);

	}

}
