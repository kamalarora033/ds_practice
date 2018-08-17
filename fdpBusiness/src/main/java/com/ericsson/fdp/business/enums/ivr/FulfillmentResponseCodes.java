package com.ericsson.fdp.business.enums.ivr;

import com.ericsson.fdp.common.constants.FDPConstant;
import com.ericsson.fdp.common.enums.ExternalSystem;

/**
 * The Enum IVRResponseCodes.
 * 
 * @author Ericsson
 */
public enum FulfillmentResponseCodes {

	/** The parameter missing. */
	PARAMETER_MISSING("Parameter Missing", 1100, FDPConstant.CIS, FulfillmentStatus.FAILURE,
			"Required Parameter %s is missing."),

	/** The invalid msisdn. */
	INVALID_PARAMETER("Parameter is not valid", 1102, FDPConstant.CIS, FulfillmentStatus.FAILURE, "%s is not valid."),

	/** The invalid circle request. */
	INVALID_CIRCLE_REQUEST("If Request comes from different circle then the one for which msisdn is configured", 1103,
			FDPConstant.FDP, FulfillmentStatus.FAILURE, "msisdn %s is not valid for circle %s."),

	/** The authentication failed. */
	AUTHENTICATION_FAILED("IVR user or password doesn’t match", 1104, FDPConstant.CIS, FulfillmentStatus.FAILURE,
			"Failed to Authenticate user %s."),

	/** The success. */
	SUCCESS("For Success.", 0, FDPConstant.CIS, FulfillmentStatus.SUCCESS, "Success."),

	/** The service provisioning failed. */
	FDP_EXCEPTION("If any exception occur while executing CIS flow.", 1106, FDPConstant.CIS, FulfillmentStatus.FAILURE,
			"Some exception occured."),

	/** The external system error. */
	EXTERNAL_SYSTEM_ERROR("If any error come on any external System like AIR.", null, ExternalSystem.AIR.name(),
			FulfillmentStatus.FAILURE, "%s"),

	/** The configuration error. */
	CONFIGURATION_ERROR("configuration missing", 1107, FDPConstant.CIS,FulfillmentStatus.FAILURE, "configuration of the %s product is missing"),
	
	INVALID_OPTIONAL_PARAMETER_ERROR("Parameter is not valid ", 1102, FDPConstant.CIS,FulfillmentStatus.FAILURE, " %s can not be negative"),
	
	FAF_MSISDN_INVALID("Faf Msisdn is not valid",1301, FDPConstant.CIS, FulfillmentStatus.FAILURE, "%s is not valid."),
	
	/** The Async command authentication error. */
	INVALID_AUTHENTICATION("Invalid Authetication ", 1200, FDPConstant.CIS,
			FulfillmentStatus.FAILURE, "Invalid Username or Password"),
			
	/** The Async command  error. */
	ASYNC_SPNOTFOUND ("Transaction ID not Found ", 1201, FDPConstant.CIS,
	FulfillmentStatus.FAILURE, "Transaction ID not Found"),
	
	FAF_MAX_ADD_FAILURE("FAF msisdn add request failed", 1302,FDPConstant.CIS, FulfillmentStatus.FAILURE, "Msisdn: %s faf add request failed."),
	
	FAF_DUPLICATE_MSISDN("You cannot add subscriber number for FAF", 1303, FDPConstant.CIS,FulfillmentStatus.FAILURE, "you can't add own number for FAF %s"),
	
	FAF_MSISDN_ALREADY_REGISTER("You are already register for FAF service",1304, FDPConstant.CIS, FulfillmentStatus.FAILURE,"You are already register for FAF %s"),
	
	FAF_MSISDN_IS_NOT_REGISTER("You are not register for FAF service",1305,FDPConstant.CIS,FulfillmentStatus.FAILURE,"Msisdn: %s is not registered for Friends & Family service. Dial *226# for register."),
	
	REGISTER_FAF_OFFER_ID_NOT_FOUND("FAF offer is not found to register user",1306,FDPConstant.CIS,FulfillmentStatus.FAILURE,"OfferID is not found for Msisdn: %s."),
	
	TIME2SHARE_TRANS_LIMITS_REACHED("Transaction limits reached for subscriber", 1307, FDPConstant.CIS,FulfillmentStatus.FAILURE, "Transaction limits reached for subscriber"),

	FAF_MSISDN_IS_NOT_ELIGIBLE("You are not register for FAF service",1308,FDPConstant.CIS,FulfillmentStatus.FAILURE,"Msisdn: %s is not eligible for Friends & Family service."),
 
	//FAF_MAX_ADD_FAILURE("FAF msisdn add reqest failed")	
	FAF_MSISDN_ONNET_LIMIT_REACHED("FAF onnet limit reached",1309,FDPConstant.CIS,FulfillmentStatus.FAILURE,"Msisdn: %s maximum limit reached for Onnet number"),
	
	FAF_MSISDN_OFFNET_LIMIT_REACHED("FAF offnet limit reached",1309,FDPConstant.CIS,FulfillmentStatus.FAILURE,"Msisdn: %s maximum limit reached for Offnet number"),
	
	FAF_MSISDN_INTERNATIONAL_LIMIT_REACHED("FAF offnet limit reached",1309,FDPConstant.CIS,FulfillmentStatus.FAILURE,"Msisdn: %s maximum limit reached for International number"),
	
	FAF_MSISDN_DELETE_NOT_FOUND("FAF delete msisdn details is not found",1310,FDPConstant.CIS,FulfillmentStatus.FAILURE,"Delete FAF msisdn details is not found."),
	
	FAF_ADD_MSISDN_DETAILS_NOT_FOUND("FAF modify request failed",1311,FDPConstant.CIS,FulfillmentStatus.FAILURE,"FAF add msisdn details is not found in system."),
	
	FAF_LIST_IS_EMPTY("FAF list is empty",1312,FDPConstant.CIS,FulfillmentStatus.FAILURE,"FAF list is empty for modify"),
	
	FAF_MSISDN_BASE_OFFER_NOT_FOUND("Base offerID is not found for FAF.",1313,FDPConstant.CIS,FulfillmentStatus.FAILURE,"Subscriber base offerID is not configured for FAF."),
	
	SERVICE_CLASS_IS_NOT_ELIGIBLE("Suscriber is not eligible for faf service.",1314,FDPConstant.CIS,FulfillmentStatus.FAILURE,"Suscriber: %s is not eligible for faf service."),
	
	DATA2SHARE_DATA_LIMIT_REACHED("Data Limit is reached",1309,FDPConstant.CIS,FulfillmentStatus.FAILURE,"Sorry ! You are trying to transfer more than your Max data Limit"),
	
	DATA2SHARE_SAME_SUBSCRIBER("Same subscriber is not valid",1309,FDPConstant.CIS,FulfillmentStatus.FAILURE,"Yello! You cannot send MTN internet Me2U to yourself"),
	
	INVALID_MSISDN("Invalid Msisdn", 1309,FDPConstant.CIS,FulfillmentStatus.FAILURE, "Invalid Receipient's Number"),
	
	LOW_BALANCE("Low Balance", 1309,FDPConstant.CIS,FulfillmentStatus.FAILURE, "User cannot proceed as subscriber has low balance"), 
	
	INVALID_DATA_AMOUNT("Invalid Data Amount", 1309,FDPConstant.CIS,FulfillmentStatus.FAILURE, "Invalid Data Amount"),
	ASYNC_SP_TRANSACTION_ID ("Application error on Mobile Money", 1312, FDPConstant.CIS,
			FulfillmentStatus.FAILURE, "Application error on Mobile Money"),
	SUBSCRIBER_NOT_ELIGIBLE("Subscriber is not eligible to share the data", 1309,FDPConstant.CIS,FulfillmentStatus.FAILURE, "Subscriber is not eligible to share the data"),
    
	/** The consumer limit reached */
	CONSUMER_LIMIT_REACHED("Consumer limit reached.",2001,FDPConstant.FDP,FulfillmentStatus.FAILURE,"Consumer Limit Reached."),
	
	/** CONSNUMER_MSISDN_NOT_VALID **/
	CONSNUMER_MSISDN_NOT_VALID("Consumer msisdn not valid",2002,FDPConstant.FDP,FulfillmentStatus.FAILURE,"Consumer msisdn not valid."),
	
	/** CONSUMER_DELETE_NOT_ALLOWED **/
	CONSUMER_DELETE_NOT_ALLOWED("Deletion of Consumer not allowed",2003,FDPConstant.FDP,FulfillmentStatus.FAILURE,"Deletion of Consumer not allowed"),
	
	/** MSISDN_NOT_PROVIDER_TYPE **/
	MSISDN_NOT_PROVIDER_TYPE("Msisdn is not a provider msisdn",2004,FDPConstant.FDP,FulfillmentStatus.FAILURE,"Msisdn is not a provider msisdn"),
	
	/** CONSUMER_ALREADY_PRESENT **/
	CONSUMER_ALREADY_PRESENT("Consumer msisdn already present",2005,FDPConstant.FDP,FulfillmentStatus.FAILURE,"Consumer msisdn already present"),
	
	/** INVALID_SUBSCRIBER_TYPE **/
	INVALID_SUBSCRIBER_TYPE("Invalid Subscriber type", 1108, FDPConstant.FDP, FulfillmentStatus.FAILURE, "Invalid Subscriber type"),
	
	MAGIC_NO_ALREADY_ADDED("You have already added Magic No.",1401,FDPConstant.CIS,FulfillmentStatus.FAILURE,"Msisdn: %s is already added magic No"),
	
	MAGIC_NO_NOT_ONNET("Enter number is not onnet number.", 1402, FDPConstant.CIS,FulfillmentStatus.FAILURE, "Enter number is not onnet number"),
	
	SAME_MAGICNO_MSISDN("You cannot add Subscriber Number as Magic number.", 1403, FDPConstant.CIS,FulfillmentStatus.FAILURE, "You cannot add Magic number as Subscriber Number %s"),
	
	INVALID_ENTER_MAGIC_NO("Enter Magic number is invalid.", 1404, FDPConstant.CIS,FulfillmentStatus.FAILURE, "Enter Magic number is invalid.");

	


	/** The reason. */
	private String reason;

	/** The response code. */
	private Integer responseCode;

	/** The system type. */
	private String systemType;

	/** The status. */
	private FulfillmentStatus status;

	/** The description. */
	private String description;

	/**
	 * Instantiates a new iVR response codes.
	 * 
	 * @param reason
	 *            the reason
	 * @param responseCode
	 *            the response code
	 * @param systemType
	 *            the system type
	 * @param status
	 *            the status
	 * @param description
	 *            the description
	 */
	private FulfillmentResponseCodes(final String reason, final Integer responseCode, final String systemType,
			final FulfillmentStatus status, final String description) {
		this.reason = reason;
		this.responseCode = responseCode;
		this.systemType = systemType;
		this.status = status;
		this.description = description;
	}

	/**
	 * Gets the reason.
	 * 
	 * @return the reason
	 */
	public String getReason() {
		return reason;
	}

	/**
	 * Gets the response code.
	 * 
	 * @return the responseCode
	 */
	public Integer getResponseCode() {
		return responseCode;
	}

	/**
	 * Gets the system type.
	 * 
	 * @return the systemType
	 */
	public String getSystemType() {
		return systemType;
	}

	/**
	 * Gets the status.
	 * 
	 * @return the status
	 */
	public FulfillmentStatus getStatus() {
		return status;
	}

	/**
	 * Gets the description.
	 * 
	 * @return the description
	 */
	public String getDescription() {
		return description;
	}

}
