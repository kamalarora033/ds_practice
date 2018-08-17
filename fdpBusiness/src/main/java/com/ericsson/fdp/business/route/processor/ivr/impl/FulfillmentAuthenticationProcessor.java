package com.ericsson.fdp.business.route.processor.ivr.impl;

import java.util.Map;

import org.apache.camel.Exchange;
import org.apache.camel.Message;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ericsson.fdp.business.enums.ConfigurationKey;
import com.ericsson.fdp.business.enums.ivr.FulfillmentParameters;
import com.ericsson.fdp.business.enums.ivr.FulfillmentResponseCodes;
import com.ericsson.fdp.business.enums.ivr.FulfillmentRouteEnum;
import com.ericsson.fdp.business.route.processor.ivr.AbstractFulfillmentProcessor;
import com.ericsson.fdp.common.constants.FDPConstant;
import com.ericsson.fdp.common.constants.FDPLoggerConstants;
import com.ericsson.fdp.common.util.ClassUtils;
import com.ericsson.fdp.common.util.StringUtil;
import com.ericsson.fdp.common.vo.FDPCircle;
import com.ericsson.fdp.core.cache.AppCacheSubStore;
import com.ericsson.fdp.core.exception.ExecutionFailedException;
import com.ericsson.fdp.core.logging.FDPLogger;
import com.ericsson.fdp.core.utils.ApplicationCacheUtil;
import com.ericsson.fdp.core.utils.ApplicationConfigUtil;
import com.ericsson.fdp.core.utils.CircleCodeFinder;
import com.ericsson.fdp.route.enumeration.FDPRouteHeaders;

/**
 * IVRAuthenticationProcessor checks the authorization based on the username and
 * password parameters in the IVR request and send the response back if any of
 * the mandatory parameter is missing.
 * 
 * @author Ericsson
 */
public class FulfillmentAuthenticationProcessor extends AbstractFulfillmentProcessor {

	/** The Constant LOGGER. */
	private static final Logger LOGGER = LoggerFactory.getLogger(FulfillmentAuthenticationProcessor.class);
	
	@Override
	public void process(final Exchange exchange) throws Exception {
		final Message in = exchange.getIn();
		final FulfillmentRouteEnum ivrRoute = getRouteInfo(exchange);
		final String username = in.getHeader(FulfillmentParameters.USERNAME.getValue(), String.class);
		final String password = in.getHeader(FulfillmentParameters.PASSWORD.getValue(), String.class);
		final String input = in.getHeader(FulfillmentParameters.INPUT.getValue(),String.class);
		
		String requestId = in.getHeader(FDPRouteHeaders.REQUEST_ID.getValue(), String.class);
		//final String circleCode = in.getHeader(FulfillmentParameters.CIRCLE_CODE.getValue(), String.class);
		LOGGER.debug("User Authentication username = {} and password = {}.", username, password);
		final String msisdn = (String) in.getHeader("MSISDN");
		FDPCircle fdpCircle = exchange.getIn().getHeader(FDPRouteHeaders.FDP_CIRCLE.getValue(), FDPCircle.class);
		if(null == fdpCircle) {
			/*final FDPCache<FDPAppBag, Object> applicationConfigCache = ApplicationConfigUtil
					.getApplicationConfigCache();*/
			/*if(FDPApplicationFeaturesEnum.isFeatureAllowed(FDPApplicationFeaturesEnum.FEATURE_IS_IMSI_BASED_CIRCLE_CHECK_ALLOW,applicationConfigCache)) {
				fdpCircle = CircleCodeFinder.getFDPCircleByCode(circleCode, applicationConfigCache);
			} else {*/
				fdpCircle = CircleCodeFinder.getFDPCircleByMsisdn(msisdn,
						ApplicationConfigUtil.getApplicationConfigCache());
			//}
		}
		String errorDescription = null;
		if (null == fdpCircle) {
			errorDescription = new StringBuilder(FDPConstant.REQUEST_ID)
							.append(FDPConstant.LOGGER_KEY_VALUE_DELIMITER)
							.append(requestId)
							.append(FDPConstant.LOGGER_DELIMITER)
							.append(FDPConstant.ERROR_CODE)
							.append(FDPConstant.LOGGER_KEY_VALUE_DELIMITER)
							.append(FulfillmentResponseCodes.INVALID_CIRCLE_REQUEST.getResponseCode().toString())
							.append(FDPConstant.LOGGER_DELIMITER)
							.append(FDPConstant.ERROR_DESC)
							.append(FDPConstant.LOGGER_KEY_VALUE_DELIMITER)
							.append(String.format(FulfillmentResponseCodes.INVALID_CIRCLE_REQUEST.getDescription(), msisdn
									)).toString();
			LOGGER.error(errorDescription);

			/*
			 * LOGGER.error(new
			 * StringBuilder(FulfillmentResponseCodes.INVALID_CIRCLE_REQUEST
			 * .getResponseCode().toString())
			 * .append(FDPConstant.LOGGER_KEY_VALUE_DELIMITER)
			 * .append(String.format
			 * (FulfillmentResponseCodes.INVALID_CIRCLE_REQUEST
			 * .getDescription(),msisdn , circleCode)) .toString());
			 */
			sendResponse(exchange, FulfillmentResponseCodes.INVALID_CIRCLE_REQUEST, msisdn, null);
		} else {
			errorDescription = new StringBuilder(FDPConstant.MSISDN)
							.append(FDPConstant.LOGGER_KEY_VALUE_DELIMITER)
							.append(msisdn)
							.append(FDPConstant.LOGGER_DELIMITER)
							.append(FDPConstant.ERROR_CODE)
							.append(FDPConstant.LOGGER_KEY_VALUE_DELIMITER)
							.append(FulfillmentResponseCodes.AUTHENTICATION_FAILED.getResponseCode().toString())
							.append(FDPConstant.LOGGER_DELIMITER)
							.append(FDPConstant.ERROR_DESC)
							.append(FDPConstant.LOGGER_KEY_VALUE_DELIMITER)
							.append(String.format(FulfillmentResponseCodes.AUTHENTICATION_FAILED.getDescription(), username,
									fdpCircle.getCircleCode())).toString();
			if (!isValidValueForKey(ivrRoute.getPasswordKey(), password, in.getHeader("iname").toString())
					|| !isValidValueForKey(ivrRoute.getUserNameKey(), username, in.getHeader("iname").toString())) {
				sendResponse(exchange, FulfillmentResponseCodes.AUTHENTICATION_FAILED, username);
					FDPLogger.error(getCircleRequestLogger(fdpCircle), getClass(), "process()", errorDescription);
			}
			String infoDescription=null; 
			infoDescription=new StringBuilder(FDPConstant.REQUEST_ID)
			.append(FDPConstant.LOGGER_KEY_VALUE_DELIMITER)
			.append(requestId)
			.append(FDPConstant.LOGGER_DELIMITER).toString();
			final Map<String, String> configurationMap = fdpCircle.getConfigurationKeyValueMap();
			final String paramDisplay = configurationMap.get(ConfigurationKey.PP_REPORT_PARAM_DISPLAY.getAttributeName());
			if(null != paramDisplay && paramDisplay.equalsIgnoreCase(FDPLoggerConstants.TRUE)){
				FDPLogger.info(getCircleRequestLogger(fdpCircle), getClass(), "process()", "CMDREQ: input="+input+";MSISDN="+msisdn+";username="+username+";password="+";iname="+in.getHeader("iname").toString());
			}else{
							FDPLogger.info(getCircleRequestLogger(fdpCircle), getClass(), "process()", infoDescription+"CMDREQ:"+FDPLoggerConstants.SKIPPED);
			}
		}
		LOGGER.debug("User Authentication SUCCESS.");
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
			if(key.contains(FDPConstant.FULFILLMENT) && key.contains(FDPConstant.USERNAME)){
				modifiedKey = FDPConstant.FULFILLMENT+ FDPConstant.UNSERSCORE+ iname.toUpperCase()+ FDPConstant.UNSERSCORE+ FDPConstant.USERNAME;// Fixed for the incident Id: 17821
				LOGGER.debug(StringUtil.concat("Username Key for ", iname, " == ",  modifiedKey));
			}
			else if (key.contains(FDPConstant.FULFILLMENT) && key.contains(FDPConstant.PASSWORD)){
				modifiedKey = FDPConstant.FULFILLMENT+ FDPConstant.UNSERSCORE+ iname.toUpperCase()+ FDPConstant.UNSERSCORE+ FDPConstant.PASSWORD;// Fixed for the incident Id: 17821
				LOGGER.debug(StringUtil.concat("Password Key for ", iname, " == ",  modifiedKey));
			}
		}
		return modifiedKey;
	}
}
