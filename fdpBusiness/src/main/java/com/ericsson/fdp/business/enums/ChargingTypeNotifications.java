package com.ericsson.fdp.business.enums;

import com.ericsson.fdp.dao.enums.ProductAdditionalInfoEnum;

/**
 * This enum defines the charging type notifications.
 *
 * @author Ericsson
 *
 */
public enum ChargingTypeNotifications {

 /*Buy product 		- Zero charging  1
	Buy product 		- Positive charging 2
	De - provisioning  	- Zero charging 3
	De - provisioning 	- Positive charging  4
	
	
	Buy product 		- Cancel Notification 5
	De - provisioning 	- Cancel Notification  6 */
	
	/**
	 * Zero charging.
	 */
	ZERO_CHARGING_BUY_PRODUCT(1),
	/**
	 * Positive charging
	 */
	POSITIVE_CHARGING_BUY_PRODUCT(2),
	 
	
	ZERO_CHARGING_DEPROVISIONING(3),
	/**
	 * Positive charging
	 */
	POSITIVE_CHARGING_DEPROVISIONING(4),
	
	//changes for double confirmation notification for product buy
	CANCEL_NOTIFICATION_BUY_PRODUCT(5),
	
	//changes for double confirmation notification for de-provisioning
	CANCEL_NOTIFICATION_DEPROVISIONING(6),
	/**
	 * TIME4U_INPUT_VOUCHER_PIN
	 */
	TIME4U_INPUT_VOUCHER_PIN(ProductAdditionalInfoEnum.TIME4U_INPUT_VOUCHER_PIN.getKey()),
	
	/** TIME4U_INPUT_AMOUNT **/
	TIME4U_INPUT_AMOUNT(ProductAdditionalInfoEnum.TIME4U_INPUT_AMOUNT.getKey()),
	
	/** USER_THRESHOLD_POLICY **/
	USER_THRESHOLD_POLICY(ProductAdditionalInfoEnum.USER_THRESHOLD_POLICY.getKey());
	
	private ChargingTypeNotifications(final Integer chargingType) {
		this.chargingType = chargingType;
	}

	/** The charging type. */
	private Integer chargingType;

	/**
	 * @return the chargingType
	 */
	public Integer getChargingType() {
		return chargingType;
	}

	/**
	 * @param chargingType the chargingType to set
	 */
	public void setChargingType(final Integer chargingType) {
		this.chargingType = chargingType;
	}
}
