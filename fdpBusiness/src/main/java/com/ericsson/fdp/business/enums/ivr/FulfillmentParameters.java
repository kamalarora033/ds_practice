/*
 * 
 */
package com.ericsson.fdp.business.enums.ivr;

import com.ericsson.fdp.common.constants.FDPConstant;

/**
 * The Enum IVRParameters.
 * 
 * @author Ericsson
 */
public enum FulfillmentParameters {

	/** The input. */
	INPUT("input", true, null),
	
	AUTO_RENEW("autorenewal",false,null),
	
	PAYMENT_MODE("paysrc",false,null),
	/**The country code*/
	COUNTRY_CODE("COUNTRYCODE",false,null),

	/** The channeltype. */
	CHANNEL("channel", true, null),
	
	/** The msisdn. */
	MSISDN("msisdn", true, FDPConstant.MSISDN_REGEX),

	/** The circle code. */
	CIRCLE_CODE("circleCode", true, null),

	/** The username. */
	USERNAME("username", true, null),

	/** The password. */
	PASSWORD("password", true, null),

	/** The channeltype. */
	CHANNEL_TYPE("channelType", false, null),

	/** The system type. */
	SYSTEM_TYPE("systemType", true, null),

	/** The invocator name. */
	INVOCATOR_NAME("iname", true, null/* "(?i)(web|ivr)" */),

	/** The invocator name. */
	ACTION("action", false, null/* "(?i)(PBY|PDCT)" */),
	
	/** The consumerMsisdn used in case of shared account **/
	CONSUMER_MSISDN("consumerMsisdn",true,FDPConstant.MSISDN_TEN_DIGIT_REGEX),
	
	/** The consumerName used in case of shared account**/
	CONSUMER_NAME("consumerName",false,null),
	
	/** The autoApprove used in case of shared account **/
	AUTO_APPROVE_SHARED("autoApprove",false,FDPConstant.YES_NO_REGEX),
	
	TRANSACTION_ID("transaction_id", true, null),
	
	/** The database id used in case of shared account **/
	DATABASE_ID("sharedRID",true,FDPConstant.SHARED_RID_REGEX),
	
	/** The providerMsisdn in case of shared account detach consumer **/
	PROVIDER_MSISDN("providerMsisdn",true,FDPConstant.MSISDN_TEN_DIGIT_REGEX),
	
	OFFER_ID("offerID",true,null),
	
	OFFER_TYPE("offerType",true,null),
	
	EXPIRY_DATE("expiryDate",true,"^[0-9]*$"),
	
	SKIP_RS_CHARING("skiprscharging",false,null),

	/** PROVIDER_OFFFER_ID **/
	PROVIDER_OFFER_ID("offerProviderID",false,null),
	
	BENEFICIARY_NUMBER("benmsisdn",false,null),
	
	SEND_SMS("sendsms",false,null),
	
	SEND_FLASH("sendflash",false,null),
	
	SPLIT_NUMBER("splitno",false,null),
	
	PRODUCT_COST("productcost",false,null),
	
	SKIP_CHARGING("skipcharging",false,null),
	
	EXPIRY_NOTIFICATION_FLAG("expirynotificationflag",false,null),
	
	FAF_MSISDN("fafmsisdn",false,null),
	
	TRANSFER("transfer", false, null),
	
	ACCOUNT_TYPE("accountType", false, null),

	BUNDLE_TYPE("bundleType", false, null),
	
	PIN("pin", false, null),
	
	OLD_FAF_MSISDN("oldfafMsisdn",false,null),
	
	SRC_ACCOUNT_TYPE("srcAccountType", false, null),
	
	DST_ACCOUNT_TYPE("dstAccountType", false, null),
	
	EXTERNAL_REFERENCE("EXTERNAL_REFERENCE", false, null),
	
	EXTERNAL_APPLICATION("EXTERNAL_APPLICATION", false, null),
	
	EXTERNAL_USER("EXTERNAL_USER", false, null),
	
	REQUESTED_APPLICATION("REQUESTED_APPLICATION", false, null),
	
	OPERATION_NAME("OPERATION_NAME", false, null),
	
	ENTITY_ID("ENTITY_ID", false, null),
	
	OFFER_CODE("OFFER_CODE", false, null),
	
	SUBSCRIPTION_FLAG("SUBSCRIPTION_FLAG", false, null),
	
	ACTIVATION_DATE("ACTIVATION_DATE", false, null),
	
	EXPIRY_DATE_ABILITY("EXPIRY_DATE", false, null),
	
	OFFER_CHARGE("OFFER_CHARGE", false, null),
	
	ORDER_STATUS("ORDER_STATUS", false, null),
	
	VALIDITY("validity", false, null),

	/**
	 * The time4u voucher id  
	 */
	TIME4U_VOUCHER_ID("voucher_id",false,null),
	/**
	 * The amtCharged.
	 */
	AMOUNT_CHARGED("amtCharged",false,null),
	
	CLIENT_TRANSACTION_ID("clientTransactionId", false, null),
	
	/** Parameter to take Product Cost in case of Time4U **/
	TIME4_PRODUCT_COST("time4u_cost",false,null),
	
	OFFER_VALIDITY("validity", false, null),
	
	/** Parameter to take threshold_id in case of SBB **/
    THRESHOLD_ID("threshold_id", true, null),
    
	/** Parameter to take counter_id in case of SBB **/
    COUNTER_ID("counter_id", true, null),
    
	/** Parameter to take threshold_value in case of SBB **/
    THRESHOLD_VALUE("threshold_value", false, null),
    
	/** Parameter to take counter_value in case of SBB **/
    COUNTER_VALUE("counter_value", false, null),
	
	/** Parameter to take old MSISDN value in case of SIM Change request - Handset Based Charging from DMC **/
	OLD_MSISDN("OLD_MSISDN",true,null),
	
	/** Parameter to take IMEI value in case of Device Change and SIM Change request - Handset Based Charging from DMC **/
	OLD_IMEI("OLD_IMEI", true, null),
	
	/** Parameter to take Device Type value in case of Device Change and SIM Change request - Handset Based Charging from DMC **/
	OLD_DEV_TYPE("OLD_DEV_TYPE", true, null),
	
	/** Parameter to take new IMEI value in case of Device Change request - Handset Based Charging from DMC **/
	NEW_IMEI("NEW_IMEI", true, null),
	
	/** Parameter to take new Device Type value in case of Device Change request - Handset Based Charging from DMC **/
	NEW_DEV_TYPE("NEW_DEV_TYPE", true, null),
	
	/** Parameter to take Alternate Number value for  Number Reservation**/
	ALTERNATE_NUMBER("alternate_number", false, null),
	
	/** Parameter to take product type value for view subscriber history**/
	VIEW_PRODUCT_TYPE("viewProducTtype", false, null);
	
	/** The value. */
	private String value;

	/** The is mandatory. */
	private boolean isMandatory;

	/** The validation regex. */
	private String validationRegex;
	
	/**
	 * Instantiates a new iVR parameters.
	 * 
	 * @param value
	 *            the value
	 */
	private FulfillmentParameters(final String value, final boolean isMandatory, final String validationRegex) {
		this.value = value;
		this.isMandatory = isMandatory;
		this.validationRegex = validationRegex;
	}

	/**
	 * Gets the value.
	 * 
	 * @return the value
	 */
	public String getValue() {
		return value;
	}

	/**
	 * Checks if is mandatory.
	 * 
	 * @return true, if is mandatory
	 */
	public boolean isMandatory() {
		return isMandatory;
	}

	/**
	 * Gets the validation regex.
	 * 
	 * @return the validation regex
	 */
	public String getValidationRegex() {
		return validationRegex;
	}

}
