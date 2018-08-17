/*
 * 
 */
package com.ericsson.fdp.business.enums.ivr;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.ericsson.fdp.business.constants.BusinessConstants;
import com.ericsson.fdp.core.config.utils.PropertyUtils;

/**
 * The Enum FulfillmentRouteEnum.
 * 
 * @author Ericsson
 */
public enum FulfillmentRouteEnum {

	/** The ivr command service. */
	IVR_COMMAND_SERVICE("ivr_command_service_route", "IVR_COMMAND_SERVICE_USERNAME", "IVR_COMMAND_SERVICE_PASSWORD",
			"COMMAND_SERVICE_WHITELISTED_IP", "COMMAND_SERVICE_WHITELISTED_INAME",
			"servlet:///commands?servletName=COMMANDSERVICE&matchOnUriPrefix=false", Arrays.asList(
					FulfillmentParameters.INPUT, FulfillmentParameters.MSISDN, FulfillmentParameters.CIRCLE_CODE,
					FulfillmentParameters.USERNAME, FulfillmentParameters.PASSWORD,
					FulfillmentParameters.INVOCATOR_NAME), PropertyUtils
					.getProperty(BusinessConstants.THROTTLER_IVR_PRODUCTBUY_MAX_TPS), PropertyUtils
					.getProperty(BusinessConstants.THROTTLER_IVR_PRODUCTBUY_RESUME_PERCENTAGE)),

	/** The ivr product buy service. */
	IVR_PRODUCT_BUY_SERVICE("ivr_service_route", "IVR_PRODUCT_BUY_SERVICE_USERNAME",
			"IVR_PRODUCT_BUY_SERVICE_PASSWORD", "PRODUCT_BUY_SERVICE_WHITELISTED_IP",
			"PRODUCT_BUY_SERVICE_WHITELISTED_INAME",
			"servlet:///ProductBuy?servletName=COMMANDSERVICE&matchOnUriPrefix=false", Arrays.asList(
					FulfillmentParameters.INPUT, FulfillmentParameters.MSISDN, FulfillmentParameters.CIRCLE_CODE,
					FulfillmentParameters.USERNAME, FulfillmentParameters.PASSWORD,
					FulfillmentParameters.INVOCATOR_NAME, FulfillmentParameters.ACTION), PropertyUtils
					.getProperty(BusinessConstants.THROTTLER_IVR_COMMANDSERVICE_MAX_TPS), PropertyUtils
					.getProperty(BusinessConstants.THROTTLER_IVR_COMMANDSERVICE_RESUME_PERCENTAGE)),

	/** The fulfillment service. */
	FULFILLMENT_SERVICE("fulfillment_service_route", "FULFILLMENT_SERVICE_USERNAME", "FULFILLMENT_SERVICE_PASSWORD",
			"FULFILLMENT_SERVICE_WHITELISTED_IP", "FULFILLMENT_SERVICE_WHITELISTED_INAME",
			"servlet:///fulfillmentService?servletName=FulfillmentService&matchOnUriPrefix=false", Arrays.asList(
					FulfillmentParameters.INPUT, FulfillmentParameters.MSISDN,
					FulfillmentParameters.USERNAME, FulfillmentParameters.PASSWORD,
					FulfillmentParameters.INVOCATOR_NAME), PropertyUtils
					.getProperty(BusinessConstants.THROTTLER_FULFILLMENT_SERVICE_MAX_TPS), PropertyUtils
					.getProperty(BusinessConstants.THROTTLER_FULFILLMENT_SERVICE_RESUME_PERCENTAGE)),
					
				//	servlet:///MobileMoneyHttpService?servletName=MobileMoneyHttpService&matchOnUriPrefix=false
						
	
	ASYCREQUEST_ROUTE("aync_service_route", "ASYNC_SERVICE_USERNAME", "ASYNC_SERVICE_PASSWORD",
					"ASYC_SERVICE_WHITELISTED_IP", "FULFILLMENT_SERVICE_WHITELISTED_INAME",
					"servlet:///FDPAsyncHttpService?servletName=FDPAsyncHttpService&matchOnUriPrefix=false", Arrays.asList(
									FulfillmentParameters.USERNAME, FulfillmentParameters.PASSWORD,FulfillmentParameters.TRANSACTION_ID), 
									PropertyUtils.getProperty(BusinessConstants.THROTTLER_FULFILLMENT_SERVICE_MAX_TPS), 
									PropertyUtils
									.getProperty(BusinessConstants.THROTTLER_FULFILLMENT_SERVICE_RESUME_PERCENTAGE)),
									
					
					/*,

	ABILITY_SERVICE("ability_service_route", "ABILITY_SERVICE_USERNAME", "ABILITY_SERVICE_PASSWORD",
			"ABILITY_SERVICE_WHITELISTED_IP", "ABILITY_SERVICE_WHITELISTED_INAME",
			"servlet:///abilityservice?servletName=AbilityService&matchOnUriPrefix=false", Arrays.asList(
					FulfillmentParameters.INPUT, FulfillmentParameters.MSISDN,// FulfillmentParameters.COUNTRY_CODE,
					FulfillmentParameters.USERNAME, FulfillmentParameters.PASSWORD, FulfillmentParameters.CHANNEL),
			PropertyUtils.getProperty(BusinessConstants.THROTTLER_FULFILLMENT_SERVICE_MAX_TPS), PropertyUtils
					.getProperty(BusinessConstants.THROTTLER_FULFILLMENT_SERVICE_RESUME_PERCENTAGE))*/

	SMSC_ACTIVATION_SERVICE("smsc_activation_service_route", null, null, null, null, 
			"servlet:///fdpSMSCActivationService?servletName=FDPSMSCActivationService&matchOnUriPrefix=false", null, "10", "100");
	
	/** The route id. */
	private String routeId;

	/** The user name key. */
	private String userNameKey;

	/** The password key. */
	private String passwordKey;

	/** The white list ip key. */
	private String whiteListIpKey;

	/** The white list i name key. */
	private String whiteListINameKey;

	/** The from url. */
	private String fromURL;

	/** The parameter list. */
	private List<FulfillmentParameters> parameterList;

	/** The max in flight number. */
	private String maxInFlightNumber;

	/** The max infligh percentage. */
	private String maxInflighPercentage;

	/** The route map. */
	private static Map<String, FulfillmentRouteEnum> routeMap;

	/**
	 * Instantiates a new iVR route enum.
	 *
	 * @param routeId
	 *            the route id
	 * @param userNameKey
	 *            the user name key
	 * @param passwordKey
	 *            the password key
	 * @param whiteListIpKey
	 *            the white list ip key
	 * @param whiteListINameKey
	 *            the white list i name key
	 * @param fromURL
	 *            the from url
	 * @param parameterList
	 *            the parameter list
	 * @param maxInFlightNumber
	 *            the max in flight number
	 * @param maxInflighPercentage
	 *            the max infligh percentage
	 */
	private FulfillmentRouteEnum(final String routeId, final String userNameKey, final String passwordKey,
			final String whiteListIpKey, final String whiteListINameKey, final String fromURL,
			final List<FulfillmentParameters> parameterList, final String maxInFlightNumber,
			final String maxInflighPercentage) {
		this.routeId = routeId;
		this.userNameKey = userNameKey;
		this.passwordKey = passwordKey;
		this.whiteListIpKey = whiteListIpKey;
		this.whiteListINameKey = whiteListINameKey;
		this.fromURL = fromURL;
		this.parameterList = parameterList;
		this.maxInFlightNumber = maxInFlightNumber;
		this.maxInflighPercentage = maxInflighPercentage;
	}

	/**
	 * Gets the route id.
	 * 
	 * @return the routeId
	 */
	public String getRouteId() {
		return routeId;
	}

	/**
	 * Gets the user name key.
	 * 
	 * @return the userNameKey
	 */
	public String getUserNameKey() {
		return userNameKey;
	}

	/**
	 * Gets the password key.
	 * 
	 * @return the passwordKey
	 */
	public String getPasswordKey() {
		return passwordKey;
	}

	/**
	 * Gets the white list ip key.
	 * 
	 * @return the white list ip key
	 */
	public String getWhiteListIpKey() {
		return whiteListIpKey;
	}

	/**
	 * Gets the white list i name key.
	 * 
	 * @return the white list i name key
	 */
	public String getWhiteListINameKey() {
		return whiteListINameKey;
	}

	/**
	 * Gets the from url.
	 * 
	 * @return the fromURL
	 */
	public String getFromURL() {
		return fromURL;
	}

	/**
	 * Gets the enum from route id.
	 * 
	 * @param routeId
	 *            the route id
	 * @return the enum from route id
	 */
	public static FulfillmentRouteEnum getEnumFromRouteId(final String routeId) {
		if (routeMap == null) {
			routeMap = new HashMap<String, FulfillmentRouteEnum>();
			for (final FulfillmentRouteEnum val : values()) {
				routeMap.put(val.getRouteId(), val);
			}
		}
		return routeMap.get(routeId);
	}

	/**
	 * Gets the parameter list.
	 * 
	 * @return the parameter list
	 */
	public List<FulfillmentParameters> getParameterList() {
		return parameterList;
	}

	/**
	 * Gets the max in flight number.
	 *
	 * @return the max in flight number
	 */
	public String getMaxInFlightNumber() {
		return maxInFlightNumber;
	}

	/**
	 * Gets the max infligh percentage.
	 *
	 * @return the max infligh percentage
	 */
	public String getMaxInflighPercentage() {
		return maxInflighPercentage;
	}

}
