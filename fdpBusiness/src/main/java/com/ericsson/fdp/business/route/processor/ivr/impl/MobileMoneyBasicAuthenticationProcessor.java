package com.ericsson.fdp.business.route.processor.ivr.impl;
/**
 * This class is for basic authentication username/password 
 */
import javax.xml.bind.DatatypeConverter;

import org.apache.camel.Exchange;
import org.apache.camel.Message;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ericsson.fdp.business.enums.ivr.FulfillmentResponseCodes;
import com.ericsson.fdp.business.route.processor.ivr.AbstractFulfillmentProcessor;
import com.ericsson.fdp.common.util.ClassUtils;
import com.ericsson.fdp.common.util.StringUtil;
import com.ericsson.fdp.core.cache.AppCacheSubStore;
import com.ericsson.fdp.core.exception.ExecutionFailedException;
import com.ericsson.fdp.core.utils.ApplicationCacheUtil;
/**
 * 
 * @author euyybcr
 *
 */
public class MobileMoneyBasicAuthenticationProcessor extends AbstractFulfillmentProcessor {

	private static final Logger LOGGER = LoggerFactory.getLogger(MobileMoneyAuthenticationProcessor.class);

	private static final String MM_USERNAME_KEY = "MOBILE_MONEY_USER";
	private static final String MM_USERNAME_PASSWORD = "MOBILE_MONEY_PASSWORD";
	private static final String ACCESS_TOKEN = "accessToken";
	private static final String AUTHORIZATION = "Authorization";
	
	@Override
	public void process(Exchange exchange) throws Exception {
		// TODO Auto-generated method stub
		LOGGER.info("User authentication Started for Mobile Money ");	
		Message message = exchange.getIn();
		boolean headerExist = false;
		String authorizationHeader = null;
		if(null!=message.getHeader(ACCESS_TOKEN,String.class))
		{
			authorizationHeader = message.getHeader(ACCESS_TOKEN,String.class).trim();
			headerExist = true;
		}
		else if(null!=message.getHeader(AUTHORIZATION,String.class))
		{
		 authorizationHeader = message.getHeader(AUTHORIZATION,String.class).trim();
		 headerExist = true;
		}		
		if(authorizationHeader!=null)
		{
			
			if(authorizationHeader.startsWith("Basic ")||authorizationHeader.startsWith("Basic"))
			{
				authorizationHeader = authorizationHeader.substring("Basic".length()).trim();
			}
			String accessToken = new String(DatatypeConverter.parseBase64Binary(authorizationHeader));
			if(accessToken.contains(":"))
			{
				String userDetails[] = accessToken.split(":");
				if (!isValidValueForKey(MM_USERNAME_KEY,userDetails[0])
						|| !isValidValueForKey(MM_USERNAME_PASSWORD,  encodePassword(userDetails[1]))) {
					exchange.getOut().setHeader(Exchange.HTTP_RESPONSE_CODE, "401");
					sendResponse(exchange, FulfillmentResponseCodes.AUTHENTICATION_FAILED, userDetails[0]);
				}
			}
		}
		if(!headerExist)
		{
			exchange.getOut().setHeader(Exchange.HTTP_RESPONSE_CODE, "401");
			sendResponse(exchange, FulfillmentResponseCodes.INVALID_PARAMETER);
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
