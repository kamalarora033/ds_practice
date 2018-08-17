package com.ericsson.fdp.business.route.processor.ivr.impl;

import org.apache.camel.Exchange;
import org.apache.camel.Message;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ericsson.fdp.business.enums.ivr.FulfillmentParameters;
import com.ericsson.fdp.business.enums.ivr.FulfillmentResponseCodes;
import com.ericsson.fdp.business.enums.ivr.FulfillmentRouteEnum;
import com.ericsson.fdp.business.route.processor.ivr.AbstractFulfillmentProcessor;
import com.ericsson.fdp.common.constants.FDPConstant;
import com.ericsson.fdp.common.util.ClassUtils;
import com.ericsson.fdp.common.util.StringUtil;
import com.ericsson.fdp.common.vo.FDPCircle;
import com.ericsson.fdp.core.cache.AppCacheSubStore;
import com.ericsson.fdp.core.exception.ExecutionFailedException;
import com.ericsson.fdp.core.logging.FDPLogger;
import com.ericsson.fdp.core.utils.ApplicationCacheUtil;
import com.ericsson.fdp.route.enumeration.FDPRouteHeaders;

public class AsyncAuthenticationProcessor extends AbstractFulfillmentProcessor{

	/** The Constant LOGGER. */
	private static final Logger LOGGER = LoggerFactory.getLogger(AsyncAuthenticationProcessor.class);
	
	
	@Override
	public void process(Exchange exchange) throws Exception {
		
		final Message in = exchange.getIn();
		final FulfillmentRouteEnum ivrRoute = getRouteInfo(exchange);
		final String username = in.getHeader(FulfillmentParameters.USERNAME.getValue(), String.class);
		final String password = in.getHeader(FulfillmentParameters.PASSWORD.getValue(), String.class);
		String requestId = in.getHeader(FDPRouteHeaders.REQUEST_ID.getValue(), String.class);
		//final String circleCode = in.getHeader(FulfillmentParameters.CIRCLE_CODE.getValue(), String.class);
		LOGGER.debug("User Authentication username = {} and password = {}.", username, password);
	
		if (!isValidValueForKey(ivrRoute.getPasswordKey(), password, in.getHeader("iname").toString())
				|| !isValidValueForKey(ivrRoute.getUserNameKey(), username, in.getHeader("iname").toString())) {
			sendResponse(exchange, FulfillmentResponseCodes.AUTHENTICATION_FAILED, username);
			FDPCircle circle=null;
				FDPLogger.error(getCircleRequestLogger(circle), getClass(), "process()", "Invalid login name OR password");
				
				sendResponse(exchange, FulfillmentResponseCodes.INVALID_AUTHENTICATION, null,null);				
			}
		LOGGER.debug("User Authentication SUCCESS.");
	}


/**
 * Checks if is valid value for key.
 * 
 * @param key
 *            the key
 * @param value
 *            the value
 * @return true, if is valid value for key
 * @throws ExecutionFailedException
 */
protected boolean isValidValueForKey(final String key, final String value, final String iname) throws ExecutionFailedException {
	boolean result = false;
	String inameKey = getKeyForIname(key,iname);
	if (inameKey != null && value != null) {
		//result = ApplicationCacheUtil.checkValueInAppCache(AppCacheSubStore.CONFIGURATION_MAP, modifiedKey, value);
		final Object objectVal = ApplicationCacheUtil.getValueFromApplicationCache(AppCacheSubStore.CONFIGURATION_MAP, inameKey);
		if (objectVal != null) {
		final String ePassword = encodePassword(objectVal.toString());
			result = value.equals(ePassword);
		} else {
			FDPLogger.error(LOGGER, getClass(), "unable to get " + inameKey + " from " + AppCacheSubStore.CONFIGURATION_MAP, "");
			throw new ExecutionFailedException("unable to get " + inameKey + " from " + AppCacheSubStore.CONFIGURATION_MAP);
		}
	} else {
		FDPLogger.error(LOGGER, getClass(), StringUtil.concat("Either Validation Key = ", inameKey, " or Value = ",
				value, " found NULL."), "");
		throw new ExecutionFailedException(StringUtil.concat("Either Validation Key = ", inameKey, " or Value = ",
				value, " found NULL."));
	}
	return result;
}



/**
 * Encode password.
 * 
 * @param password
 *            the password
 * @return the string
 */
private String encodePassword(final String password) {
	return password == null ? null : ClassUtils.getMD5Hash(password);
}


/**
 * Creates username and password key on the basis of iname and key.
 * @param key
 * @param iname
 * @return
 * 
 * evasaty
 */
private String getKeyForIname(String key, String iname) {
	String modifiedKey = key;
	if(null!=iname && null!=key){
		if(key.contains(FDPConstant.USERNAME)){
			modifiedKey = FDPConstant.FULFILLMENT+ FDPConstant.UNSERSCORE+ iname+ FDPConstant.UNSERSCORE+ FDPConstant.USERNAME;
			LOGGER.debug(StringUtil.concat("Username Key for ", iname, " == ",  modifiedKey));
		}
		else if (key.contains(FDPConstant.PASSWORD)){
			modifiedKey = FDPConstant.FULFILLMENT+ FDPConstant.UNSERSCORE+ iname+ FDPConstant.UNSERSCORE+ FDPConstant.PASSWORD;
			LOGGER.debug(StringUtil.concat("Password Key for ", iname, " == ",  modifiedKey));
		}
	}
	return modifiedKey;
}

}
