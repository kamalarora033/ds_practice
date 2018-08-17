package com.ericsson.fdp.business.enums;

/**
 * The Enum Command.
 */
public enum Command {

	/** The getaccountdetails. */
	GETACCOUNTDETAILS("GetAccountDetails", "GetAccountDetails"),

	/** The get offers. */
	GET_OFFERS("GetOffers", "GetOffers"),

	/** The deletePAM. */
	DELETE_PAM("DeletePeriodicAccountManagementData", "DeletePeriodicAccountManagementData"),

	DELETEOFFER("DeleteOffer", "DeleteOffer"),

	GET_USAGE_THRESHOLDS_AND_COUNTERS("GetUsageThresholdsAndCounters", "GetUsageThresholdsAndCounters"),

	DELETE_OFFER_FOR_BATCH_JOB("DeleteOffer", "DeleteOfferForBatchJob"),

	GET_USAGE_THRESHOLDS_AND_COUNTERS_FOR_BATCH_JOB("GetUsageThresholdsAndCounters", "GetUsageThresholdsAndCountersForBatchJob"),

	UPDATE_USAGE_THRESHOLDS_AND_COUNTERS_FOR_BATCH_JOB("UpdateUsageThresholdsAndCounters", "UpdateUsageThresholdsAndCountersForBatchJob"),

	GET_USAGE_THRESHOLDS_AND_COUNTERS_FOR_CONSUMER("GetUsageThresholdsAndCounters", "GetUsageThresholdsAndCountersForConsumer"),

	GET_OFFER_FOR_SHARED_ACC("GetOffers", "GetOffersForSharedAcc"),

	UPDATE_USAGE_THRESHOLDS_AND_COUNTERS("UpdateUsageThresholdsAndCounters", "Shared account"),

	M_CARBON_EXECUTE("EXECUTE", "EXECUTE"),

	/** The get balance and date. */
	GET_BALANCE_AND_DATE("GetBalanceAndDate", "GetBalanceAndDate"),
	
	/** The get balance and date. */
	GET_SERVICES_DETAILS_REQUEST("GetServicesDtlsRequest", "GetServicesDtlsRequest"),
	
	/** The  manhattan Command */
	MANHATTAN_COMMAND("EXECUTE_MANHATTAN", "EXECUTE_MANHATTAN"),
	
	/** Command to get Coupon List from CMS */
	CMS_GET_SUBSCRIBER_COPOUNS("GetSubscriberCoupons","Get Subscriber Coupons"),
	
	/** Command to set Subscriber Coupon as consumed */
	CMS_CONSUME_SUBSCRIBER_COUPON("ConsumeSubscriberCoupon", "Consume Subscriber Coupon"),
	
	/** Command to get subscriber's eligibility for loan */
	MCARBON_GET_LOAN_ELIGIBILITY("LOAN_ELIGIBILITY", "Loan Eligibility"),
	
	/** Command to provide subscriber loan */
	MCARBON_AVAIL_SUBSCRIBER_LOAN("LOAN_PURCHASE", "Loan Purchase"),

	
	/** Command to send COD request to OFFLINE */
	COD_SEND_REQUEST_TO_OFFLINE("codrequest", "codrequest"),
/*	PCRF command*/
	
	REMOVEPCRFSEVICE("REMOVE_PCRF_SEVICE_V2","REMOVE_PCRF_SEVICE_V2"),
	
	ADD_PCRF_SEVICE_V2("ADD_PCRF_SEVICE_V2","ADD_PCRF_SEVICE_V2"),
	ADD_PCRF_SEVICE_V3("ADD_PCRF_SEVICE_V3","ADD_PCRF_SEVICE_V3"),
	/** Command to update Offer**/
	UPDATE_OFFER("UpdateOffer","Offer Attributes"),
	
	REFILL("Refill", "Perform Refill"),

	VIEWHISTORY("VIEWHISTORY", "ViewHistory"),
	
	GET_HLR("GET HLR","GET HLR"),
	
	CHECK_SERVICE("CHECK SERVICE","CHECK SERVICE"),
	
	SERVICE_DEACTIVATE_CAI("SERVICE_DEACTIVATE_CAI","SERVICE_DEACTIVATE_CAI"),
	
	SERVICE_ACTIVATE_CAI("SERVICE_ACTIVATE_CAI","SERVICE_ACTIVATE_CAI"), 
	
	SPR("SingleProvisioningRequest", "SingleProvisioningRequest"),
	
	REFUND("Balance/Date Update", "Balance/Date Update(Main)"),
	
	REMOVEBLACKBERRYBUNDLE("REMOVE_BLACKBERRY_BUNDLE","REMOVE_BLACKBERRY_BUNDLE"),
	
	REMOVE_BB_BUNDLE("REMOVE_BB_BUNDLE","REMOVE_BB_BUNDLE"),
	
	ADD_BB_BUNDLE("ADD_BB_BUNDLE","ADD_BB_BUNDLE"),
	
	REMOVECLIRSERVICE("REMOVE_CLIR_SERVICE","REMOVE_CLIR_SERVICE"),
	
	REMOVETOLLFREESERVICE("REMOVE_TOLL_FREESERVICE","REMOVE_TOLL_FREESERVICE"),
	
	YO_DE_REGISTRATION("YO_DE_REGISTRATION","YO_DE_REGISTRATION"),
	
	REMOVEUNLIMITEDDATABUNDLE("REMOVE_UNLIMITED_DATABUNDLE","REMOVE_UNLIMITED_DATABUNDLE"), 
	
	UPDATE_BALACEANDATE_MAIN("Balance/Date Update(Main)","Balance/Date Update(Main)"),
	
	UPDATE_FAF("FaF List","FaF List"),
	
	GET_FAF_MSISDN_TYPE("GetFaFMsisdnType","GetFaFMsisdnType"),
	
	GET_FAF_LIST("GetFaFList","GetFaFList"),
	
	UPDATE_ACCUMULATORS("UpdateAccumulators", "UpdateAccumulators"),
	
	GET_ACCUMULATORS("GetAccumulators", "GetAccumulators"),
	
	ABILITY_SYNC_UP("AbilitySyncUp","AbilitySyncUp"),
 
	RunPeriodicAccountManagement("RunPeriodicAccountManagement","RunPeriodicAccountManagement"), 
	
	GETCISDETAILS("GetCISDetails","GetCISDetails"),
	
	ROLLBACK("Rollback", "Rollback"),

	/** Command to SBB update Offer**/
	SBB_UPDATE_OFFER("SBB_UpdateOffer","SBB_UpdateOffer"),
	
	/** Command to SBB delete Offer**/
	SBB_DELETE_OFFER("SBB_DeleteOffer","SBB_DeleteOffer"),
	
	/** Command to SBB delete Offer**/
	SBB_UPDATE_UCUT("UpdateUCUT", "SBB_UpdateUCUT"),
	
	/** Command to SBB delete UC/UT**/
	SBB_DELETE_UCUT("DeleteUsageThresholds", "SBB_DeleteUsageThresholds"),

	/** Command to get Usage threshold and counter of consumer. **/
	GET_USAGE_THRESHOLDS_AND_COUNTERS_CONSUMER("GetUsageThresholdsAndCounters", "GetUsageThresholdsAndCountersConsumer"),

	/** Command to get Usage threshold and counter of provider. **/
	GET_USAGE_THRESHOLDS_AND_COUNTERS_PROVIDER("GetUsageThresholdsAndCounters", "GetUsageThresholdsAndCountersProvider"),
	
	/** Command to perform REFILL operation. **/
	SBB_REFILL("Refill", "SBB Perform Refill"),
	
	QUERY_SUSBSCRIBER_HANDSET("querysubscriberhandset.php","QuerySubscriberHandset"),
	
	/** GET_SERVICES_CUM_DETAILS_REQUEST. */
	GET_SERVICES_CUM_DETAILS_REQUEST("GetServicesDtlsRequest", "GetServicesDetailsRequest"),
	
	
	OVERRIDE_NOTIFICATION("OverrideNotification", "OverrideNotification"),
	
	REFUND_MM("MM REFUND","MM REFUND"),
	
	/** Command to send COD request to OFFLINE */
	CREATE_CUSTOMER_ORDER_REQUEST("CreateCustomerOrderRequest", "CreateCustomerOrderRequest"),
	
	MPR("MultiProvisioningRequest", "MultiProvisioningRequest"),
	
	VAS_ADD_REMOVE("VAS(ADD/REMOVE)","VAS(ADD/REMOVE)"),
	
	UPDATE_BALACEANDATE("Balance/Date Update","Balance/Date Update"),
	
	LOYALTY_REDEEM("LYT_CLAIM_REDEEM_POINTS_PRC","LYT_CLAIM_REDEEM_POINTS_PRC"),
	LOYALTY_REDEEM_ROLLBACK("LYT_POINTS_ROLL_BACK_REDEMPTION","LYT_POINTS_ROLL_BACK_REDEMPTION"),
	UPDATE_SERVICE_CLASS("UpdateServiceClass","Service Class"),
	GET_PIN_DETAIL("GetPinDetail", "GetPinDetail"),
	UPDATE_PIN_DETAIL("UpdatePinDetail", "UpdatePinDetail"),
	GET_ME2U_SUBSCRIBER("GetMe2USubscriber", "GetMe2USubscriber"),
	UPDATE_ACCUMULATORS_AUTO_RENEWAL("UpdateAccumulators(AutoRenewal)", "UpdateAccumulators(AutoRenewal)"),
	GET_TRANSACTION_STATUS("gettransactionstatusrequest", "GETTransactionStatus"),
	/** The GetActiveBundlesDetailsRequest constant */
	GET_ACTIVE_BUNDLES_DETAILS_REQUEST("GetActiveBundlesDetailsRequest","GetActiveBundlesDetailsRequest"),
	GET_4G_HANDSET_ADC("GET_4G_HANDSET_ADC", "GET_4G_HANDSET_ADC"),
	ADD_PCRF_QUOTA("ADD_PCRF_QUOTA","ADD_PCRF_QUOTA"),
	MM_DEBIT("MM DEBIT", "MM DEBIT");
	
	
	
	/**
	 * Instantiates a new command.
	 * 
	 * @param commandName
	 *            the command name
	 * @param commandDisplayName
	 *            the command display name
	 */
	private Command(final String commandName, final String commandDisplayName) {
		this.commandName = commandName;
		this.commandDisplayName = commandDisplayName;
	}

	/** The command name. */
	private String commandName;

	/** The command display name. */
	private String commandDisplayName;

	/**
	 * Gets the command name.
	 * 
	 * @return the command name
	 */
	public String getCommandName() {
		return commandName;
	}

	/**
	 * Gets the command display name.
	 * 
	 * @return the command display name
	 */
	public String getCommandDisplayName() {
		return commandDisplayName;
	}

}