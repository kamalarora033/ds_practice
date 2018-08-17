package com.ericsson.fdp.business.constants;

/**
 * This class is used to store the constants that are used by the command in
 * execution.
 * 
 * @author Ericsson
 * 
 */
public class FDPCommandConstants {

	/**
	 * The fault code path for ACIP commands.
	 */
	public static final String ACIP_FAULT_CODE_PATH = "faultCode";

	/**
	 * The fault code path for UCIP commands.
	 */
	public static final String UCIP_FAULT_CODE_PATH = "faultCode";
	
	/**
	 * The fault code path for UCIP commands.
	 */
	public static final String MM_FAULT_CODE_PATH = "status";
	
	/**
	 * The error code path for MM commands.
	 */
	public static final String MM_ERROR_CODE_PATH = "_errorcode";
	
	/**
	 * The error code path for MM commands value.
	 */
	public static final String MM_ERROR_CODE_PATH_VALUE = "_value";
	
	/**
	 * The error code path for MM name.
	 */
	public static final String MM_ERROR_CODE_PATH_NAME = "_name";
	
	
	

	/**
	 * The response code path for ACIP commands.
	 */
	public static final String ACIP_RESPONSE_CODE_PATH = "responseCode";

	/**
	 * The response code path for UCIP commands.
	 */
	public static final String UCIP_RESPONSE_CODE_PATH = "responseCode";
	
	/**
	 * The response code path for CIS commands.
	 */
	public static final String CIS_RESPONSE_CODE_PATH = "responseCode";

	/**
	 * The response code path for UCIP commands.
	 */
	public static final String CMS_RESPONSE_CODE_PATH = "responseCode";
	/**
	 * The response code path for RS commands.
	 */
	public static final String RS_RESPONSE_CODE_PATH = "resultCode";

	/**
	 * The response code path for CGW commands.
	 */
	public static final String CGW_RESPONSE_CODE_PATH = "returnCode";

	/** The Constant ACIP_FAULT_CODE_DESC_PATH. */
	public static final String ACIP_FAULT_CODE_DESC_PATH = "faultString";

	/** The Constant UCIP_FAULT_CODE_DESC_PATH. */
	public static final String UCIP_FAULT_CODE_DESC_PATH = "faultString";
	
	/** The Constant GET_ACCOUNT_DETAILS_PAM_ID. */
	public static final String GET_ACCOUNT_DETAILS_PAM_ID = "paminformationList";
	
	/**
	 * The response code path for LOYALTY commands.
	 */
	public static final String LOYALTY_RESPONSE_CODE_PATH = "RESPONSE_ERROR_CODE";
	
	/**
	 * The fault code path for LOYALTY commands.
	 */
	//SuccessFlag
	//public static final String LOYALTY_FAULT_CODE_PATH = "REQUEST_STATUS";
	
	public static final String LOYALTY_FAULT_CODE_PATH = "responseoutput.responseoutput.SuccessFlag";
	
	/** The Constant LOYALTY_FAULT_CODE_DESC_PATH. */
	public static final String LOYALTY_FAULT_CODE_DESC_PATH = "responseoutput.responseoutput.ErrorDesc1";
	
	/** The response code path for EVDS commands **/
	public static final String EVDS_RESPONSE_CODE_PATH = "resultCode";
	
	/** The response code path for ABILITY commands.*/
	public static final String ABILITY_RESPONSE_CODE_PATH = "responseCode";
	
	/**The fault code path for ABILITY commands.*/
	public static final String ABILITY_FAULT_CODE_PATH = "faultCode";
	
	/** The Constant LOYALTY_FAULT_CODE_DESC_PATH. */
	public static final String ABILITY_FAULT_CODE_DESC_PATH = "faultString";
	
	/** The response code path for EVDS commands **/
	public static final String EVDS_HTTP_RESPONSE_CODE_PATH = "resultCode";

	public static final String MM_ERROR_CODE = "errorcode";

	public static final String MM_REQUESTTOKEN = "request";

	public static final String MM_RESPONSETOKEN = "response>";
	
	
	
	public static final String RS_RESPONCODE_IFINVALIDRESPONSE = "909";
	
	public static final String ABILITY_REQUEST_STATUS = "REQUEST_STATUS";
	public static final String ABILITY_RESPONSE_ERROR_CODE = "RESPONSE_ERROR_CODE";
	public static final String ABILITY_SUCCESS_RESPONSE="ABILITY_SUCCESS_RESPONSE";	

}
