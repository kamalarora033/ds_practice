package com.ericsson.fdp.business.route.processor.ivr.impl;

import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ericsson.fdp.business.enums.ivr.FulfillmentParameters;
import com.ericsson.fdp.business.enums.ivr.FulfillmentResponseCodes;
import com.ericsson.fdp.business.route.processor.ivr.AbstractFulfillmentProcessor;
import com.ericsson.fdp.common.util.ClassUtils;
import com.ericsson.fdp.common.util.StringUtil;
import com.ericsson.fdp.core.cache.AppCacheSubStore;
import com.ericsson.fdp.core.exception.ExecutionFailedException;
import com.ericsson.fdp.core.utils.ApplicationCacheUtil;

public class MobileMoneyAuthenticationProcessor extends AbstractFulfillmentProcessor {

	/** The Constant LOGGER. */
	private static final Logger LOGGER = LoggerFactory.getLogger(MobileMoneyAuthenticationProcessor.class);

	private static final String MM_USERNAME_KEY = "MOBILE_MONEY_USER";
	private static final String MM_USERNAME_PASSWORD = "MOBILE_MONEY_PASSWORD";

	@Override
	public void process(Exchange exchange) throws Exception {
		LOGGER.info("User authentication Started for Mobile Money ");
		String username =exchange.getIn().getHeader(FulfillmentParameters.USERNAME.getValue(), String.class);
		String  password= exchange.getIn().getHeader(FulfillmentParameters.PASSWORD.getValue(), String.class);

		if (!isValidValueForKey(MM_USERNAME_KEY,username)
				|| !isValidValueForKey(MM_USERNAME_PASSWORD,  encodePassword(password))) {
			exchange.getOut().setHeader(Exchange.HTTP_RESPONSE_CODE, "401");
			sendResponse(exchange, FulfillmentResponseCodes.AUTHENTICATION_FAILED, username);
		}
		LOGGER.info("User authentication Completed for Mobile Money ");
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
	protected boolean isValidValueForKey(final String key, final String value) throws ExecutionFailedException {
		boolean result = false;
		if (key != null && value != null) {
			result = ApplicationCacheUtil.checkValueInAppCache(AppCacheSubStore.CONFIGURATION_MAP, key, value);
		} else {
			throw new ExecutionFailedException(StringUtil.concat("Either Validation Key = ", key, " or Value = ",
					value, " found NULL."));
		}
		return result;
	}

}
