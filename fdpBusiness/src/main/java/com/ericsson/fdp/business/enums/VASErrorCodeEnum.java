package com.ericsson.fdp.business.enums;


/**
 * The Enum VASErrorCodeEnum is used to map vas error code and error messages.
 */
public enum VASErrorCodeEnum {

	/** The msisdn. */
	MSISDN("msisdn","1100","Required Parameter msisdn is Missing or Invalid."),
	
	/** The system type. */
	SYSTEM_TYPE("systemtype","1100","Required Parameter systemType is Missing or Invalid."),
	
	/** The channel type. */
	CHANNEL_TYPE("channeltype","1100","Required Parameter channelType is Missing or Invalid."),
	
	/** The input. */
	INPUT("input","1100","Required Parameter input is Missing."),
	
	/** The unsupported channel type. */
	UNSUPPORTED_CHANNEL_TYPE("unsupported_channel_type","1100","Unsupported Channel Type."),
	
	CIRCLEID("circleId","1100","Required Parameter circleId is Missing or Invalid.");
	
	/** The key. */
	private String key;
	
	/** The error code. */
	private String errorCode;
	
	/** The error message. */
	private String errorMessage;
	
	/**
	 * Instantiates a new vAS error code enum.
	 *
	 * @param key the key
	 * @param errorCode the error code
	 * @param errorMessage the error message
	 */
	private VASErrorCodeEnum(String key , String errorCode , String errorMessage) {
		this.key = key;
		this.errorCode = errorCode;
		this.errorMessage = errorMessage;
	}
	
	/**
	 * Gets the key.
	 *
	 * @return the key
	 */
	public String getKey() {
		return key;
	}
	
	/**
	 * Sets the key.
	 *
	 * @param key the new key
	 */
	public void setKey(String key) {
		this.key = key;
	}
	
	/**
	 * Gets the error code.
	 *
	 * @return the error code
	 */
	public String getErrorCode() {
		return errorCode;
	}
	
	/**
	 * Sets the error code.
	 *
	 * @param errorCode the new error code
	 */
	public void setErrorCode(String errorCode) {
		this.errorCode = errorCode;
	}
	
	/**
	 * Gets the error message.
	 *
	 * @return the error message
	 */
	public String getErrorMessage() {
		return errorMessage;
	}
	
	/**
	 * Sets the error message.
	 *
	 * @param errorMessage the new error message
	 */
	public void setErrorMessage(String errorMessage) {
		this.errorMessage = errorMessage;
	}
	
	/**
	 * Gets the error code and error message.
	 *
	 * @param key the key
	 * @return the error code and error message
	 */
	public static VASErrorCodeEnum getErrorCodeAndErrorMessage(final String key) {
		VASErrorCodeEnum vasErrorCodeEnum  = null;
		for(final VASErrorCodeEnum errorCodeEnum : values()) {
			if(errorCodeEnum.getKey().equalsIgnoreCase(key)) {
				vasErrorCodeEnum = errorCodeEnum;
				break;
			}
		}
		return  vasErrorCodeEnum;
	}
	
}
