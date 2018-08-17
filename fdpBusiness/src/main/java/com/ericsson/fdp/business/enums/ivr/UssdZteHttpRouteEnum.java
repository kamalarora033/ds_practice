/*
 * 
 */
package com.ericsson.fdp.business.enums.ivr;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.ericsson.fdp.business.constants.BusinessConstants;
import com.ericsson.fdp.core.config.utils.PropertyUtils;

/**
 * The Enum UssdHttpRouteEnum.
 * 
 * @author Ericsson
 */
public enum UssdZteHttpRouteEnum {

/** The ussd http service. */
	USSD_HTTP_SERVICE_ZTE("ussd_http_service_route_zte",
			"USSD_HTTP_SERVICE_WHITELISTED_IP_ZTE",
			"servlet:///httpService?servletName=UssdZteHttpService&matchOnUriPrefix=false",PropertyUtils
					.getProperty(BusinessConstants.THROTTLER_FULFILLMENT_SERVICE_MAX_TPS), PropertyUtils
					.getProperty(BusinessConstants.THROTTLER_FULFILLMENT_SERVICE_RESUME_PERCENTAGE));

	/** The route id. */
	private String routeId;

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
	private static Map<String, UssdZteHttpRouteEnum> routeMap;

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
	private UssdZteHttpRouteEnum(final String routeId,
			final String whiteListIpKey,final String fromURL,
			 final String maxInFlightNumber,
			final String maxInflighPercentage) {
		this.routeId = routeId;
		this.whiteListIpKey = whiteListIpKey;
		this.fromURL = fromURL;
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
	public static UssdZteHttpRouteEnum getEnumFromRouteId(final String routeId) {
		if (routeMap == null) {
			routeMap = new HashMap<String, UssdZteHttpRouteEnum>();
			for (final UssdZteHttpRouteEnum val : values()) {
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
