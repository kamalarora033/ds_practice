/**
 * 
 */
package com.ericsson.fdp.business.constants;

/**
 * 
 *  The Class USSDXmlZTEConstants contains all constants that will be used by
 * Xml Rpc Request/Response.
 * 
 * @author Ericsson
 *
 */
public class USSDXmlZTEConstants {

	//constants for RequestXml/ ResponseXml

	public static final String TRANSACTION_ID = "TransactionId";

	public static final String TRANSACTION_TIME = "TransactionTime";

	public static final String MSISDN = "MSISDN";

	public static final String USSD_SERVICE_CODE = "USSDServiceCode";

	public static final String USSD_REQUEST_STRING = "USSDRequestString";
	
	public static final String CALLED_NUMBER = "CalledNumber";

	public static final String XML_RESPONSE = "response";

	//constants for RequestXml/ ResponseXml

	public static final String USSD_RESPONSE_STRING = "USSDResponseString";

	public static final String ACTION = "action";

	public static final String FAULT_STRING = "faultString";

	public static final String FAULT_CODE = "faultCode";

	// Constants for XML RPC Request/Response Tags
	
	public static final String NAME_TAG = "name";
	
	public static final String VALUE_TAG = "value";
	
	public static final String MEMBER_TAG = "member";
	
	// Response Codes
	
	public static final String SUCCESS = "0";
	
	public static final String FAILURE = "1";
	
	public static final String MANDATORY_FIELD_MISSING = "Missing mandatory parameter";
	
	public static final String APPLICATION_BUSY_CODE = "2000";
	
	public static final String APPLICATION_BUSY = "Application Busy";
	
	// Response for Ussd Body
	
	public static final String DATA_OUT_OF_BOUNDS_RESPONSE = "DATA_OUT_OF_BOUNDS";
	
	public static final String MANDATORY_FIELD_MISSING_RESPONSE = "MANDATORY_FIELD_MISSING";
	
	public static final String MANDATORY_FIELD_MISSING_CODE = "4001";
	
}
