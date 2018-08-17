package com.ericsson.fdp.business.http.adapter.impl;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ericsson.fdp.business.adapter.impl.AbstractAdapterHttpCallClient;
import com.ericsson.fdp.business.bean.HttpAdapterRequest;
import com.ericsson.fdp.business.constants.BusinessConstants;
import com.ericsson.fdp.business.enums.AuxRequestParam;
import com.ericsson.fdp.business.enums.Command;
import com.ericsson.fdp.business.enums.ConfigurationKey;
import com.ericsson.fdp.common.constants.FDPConstant;
import com.ericsson.fdp.common.util.StringUtil;
import com.ericsson.fdp.core.exception.ExecutionFailedException;
import com.ericsson.fdp.core.request.FDPRequest;
import com.ericsson.fdp.dao.enums.ExternalSystem;

public class HttpsCallClientCIS extends AbstractAdapterHttpCallClient {

	/** The Constant LOGGER. */
	private static final Logger LOGGER = LoggerFactory
			.getLogger(HttpsCallClientCIS.class);

	private static String DEFAULT_RESULTCODE = "9000";

	@Override
	public Map<String, Object> httpCallClient(String httpRequest,
			HttpAdapterRequest httpAdapterRequest,
			ExternalSystem externalSystemType, FDPRequest fdpRequest)
			throws ExecutionFailedException {
		Map<String, Object> responseMap = null;
		try {
			String cisOutputXml = null;
			if (httpRequest.contains(Command.GET_FAF_MSISDN_TYPE
					.getCommandName())) {
				cisOutputXml = getFafMsisdnTypeResponseXml(httpRequest,
						fdpRequest);
			} else if (httpRequest.contains(Command.ROLLBACK.getCommandName())) {
				cisOutputXml = getRollBackResponseXml(fdpRequest);
			} else if (httpRequest.contains(Command.OVERRIDE_NOTIFICATION
					.getCommandName())) {
				cisOutputXml = getOverrideNotificationResponseXml(httpRequest,
						fdpRequest);
			}
			responseMap = new HashMap<String, Object>();
			responseMap.put(BusinessConstants.RESPONSE_CODE, "200");
			responseMap.put(BusinessConstants.COMMAND_OUTPUT, cisOutputXml);
		} catch (final Exception e) {
			LOGGER.error(e.getMessage(), e);
		}
		return responseMap;
	}

	/**
	 * This method will return the response of CIS command
	 * "OverrideNotification"
	 * 
	 * @return
	 * @throws ExecutionFailedException
	 */
	private String getOverrideNotificationResponseXml(String httpRequest,
			FDPRequest fdpRequest) throws ExecutionFailedException {
		final StringBuilder outputxmlFormat = new StringBuilder();
		outputxmlFormat.append("<?xml version=\"1.0\"?>").append("\n")
				.append("<methodCall>").append("<responseCode>");
		// outputxmlFormat.append(overrideSPNotifications(httpRequest,
		// fdpRequest) ? "0" :
		// "1").append("</responseCode>").append("</methodCall>");
		outputxmlFormat.append("0").append("</responseCode>")
				.append("</methodCall>");
		return outputxmlFormat.toString();
	}

	/**
	 * This method will return the response of CIS command "Rollback"
	 * 
	 * @return
	 */
	/*
	 * private String getRollBackResponseXml() { final StringBuilder
	 * outputxmlFormat = new StringBuilder();
	 * outputxmlFormat.append("<?xml version=\"1.0\"?>"
	 * ).append("\n").append("<methodCall>")
	 * .append("<responseCode>").append("0"
	 * ).append("</responseCode>").append("</methodCall>"); return
	 * outputxmlFormat.toString(); }
	 */

	/**
	 * This method will return the response of CIS command "Rollback" will be
	 * handling rollback return code for CIS
	 * 
	 * @return
	 */
	private String getRollBackResponseXml(FDPRequest fdpRequest) {
		final Map<String, String> configurationMap = fdpRequest.getCircle()
				.getConfigurationKeyValueMap();
		String responseCode = configurationMap
				.get(ConfigurationKey.ROLLBACK_RESPONSE_CODE.getAttributeName());
		if (null == responseCode) {
			responseCode = DEFAULT_RESULTCODE;
		}
		final StringBuilder outputxmlFormat = new StringBuilder();
		outputxmlFormat.append("<?xml version=\"1.0\"?>").append("\n")
				.append("<methodCall>").append("<responseCode>")
				.append(responseCode).append("</responseCode>")
				.append("</methodCall>");
		return outputxmlFormat.toString();
	}

	/**
	 * This method will return the response of CIS command "GetFaFMsisdnType"
	 * 
	 * @param httpRequest
	 * @param fdpRequest
	 */
	private String getFafMsisdnTypeResponseXml(String httpRequest,
			FDPRequest fdpRequest) {
		if (httpRequest.contains("subscriberNumber")) {
			return getSystemTimeDetails(httpRequest, fdpRequest);
		} else {
			return getMsisdnInternationOrOnNet(httpRequest, fdpRequest);
		}

	}

	private String getSystemTimeDetails(String httpRequest,
			FDPRequest fdpRequest) {
		String msisdn = (String) fdpRequest.getSubscriberNumber().toString();
		String systemtime = getSystemDetials();
		final StringBuilder outputxmlFormat = new StringBuilder();
		outputxmlFormat.append("<?xml version=\"1.0\"?>").append("\n")
				.append("<methodCall>").append("<internationalMsisdn>")
				.append(msisdn).append("</internationalMsisdn>")
				.append("<getSystemTime>").append(systemtime)
				.append("</getSystemTime>").append("</methodCall>");
		return outputxmlFormat.toString();
	}

	private String getMsisdnInternationOrOnNet(String httpRequest,
			FDPRequest fdpRequest) {
		String FAF_NUMBER = (String) fdpRequest
				.getAuxiliaryRequestParameter(AuxRequestParam.FAF_MSISDN_ADD);
		String response = null;
		String systemtime = getSystemDetials();
		if (!StringUtil.isNullOrEmpty(FAF_NUMBER)
				&& FAF_NUMBER.startsWith(FDPConstant.FAF_INTERNATIONAL_PREFIX)) {
			response = "true";
		} else {
			response = "false";
		}
		String paySrc = (String) fdpRequest
				.getAuxiliaryRequestParameter(AuxRequestParam.PAYMENT_MODE);
		if (paySrc == null && fdpRequest.getExternalSystemToCharge()!=null)  {
			paySrc = getpaySrcFromExternalSystem(fdpRequest
					.getExternalSystemToCharge().name());
		}
		String iname = fdpRequest.getChannel().getName();
		String provisioningAction = null;
		provisioningAction = (String) fdpRequest
				.getAuxiliaryRequestParameter(AuxRequestParam.PROVISIONING_ACTION);

		if (provisioningAction == null) {
			provisioningAction = "SUBSCRIPTION";
		}

		Boolean isBuyForOther;
		if (!StringUtil
				.isNullOrEmpty((String) fdpRequest
						.getAuxiliaryRequestParameter(AuxRequestParam.BENEFICIARY_NUMBER))
				|| !StringUtil
						.isNullOrEmpty((String) fdpRequest
								.getAuxiliaryRequestParameter(AuxRequestParam.BENEFICIARY_MSISDN))) {
			isBuyForOther = true;
		} else {
			isBuyForOther = false;
		}
		final StringBuilder outputxmlFormat = new StringBuilder();
		String defaultOfferID = "1";
		outputxmlFormat.append("<?xml version=\"1.0\"?>").append("\n")
				.append("<methodCall>").append("<internationalMsisdn>")
				.append(response).append("</internationalMsisdn>")
				.append("<iname>").append(iname).append("</iname>")
				.append("<paysrc>").append(paySrc).append("</paysrc>")
				.append("<getSystemTime>").append(systemtime)
				.append("</getSystemTime>").append("<defaultOfferID>")
				.append(defaultOfferID).append("</defaultOfferID>")
				.append("<provisioningAction>").append(provisioningAction)
				.append("</provisioningAction>").append("<buyForOther>")
				.append(isBuyForOther).append("</buyForOther>")
				.append("<getCurrentSystemTime>")
				.append(getCurrentSystemTime())
				.append("</getCurrentSystemTime>")
				.append("<WeekDay>").append(getDayOfWeek()).append("</WeekDay>").append("</methodCall>");
		return outputxmlFormat.toString();
	}

	private String getSystemDetials() {

		DateFormat dateFormat = new SimpleDateFormat(
				FDPConstant.FDP_DB_SAVE_DATE_PATTERN);
		Date date = new Date();
		return dateFormat.format(date);
	}

	private String getpaySrcFromExternalSystem(String system) {
		String paysrc = null;
		switch (system.toUpperCase().trim()) {
		case "AIR":
			paysrc = "CS";
			break;
		case "LOYALTY":
			paysrc = "Loyalty";
			break;
		case "MM":
			paysrc = "MO";
			break;
		case "EVDS":
			paysrc = "EVDS";
			break;
		default:
			break;
		}
		return paysrc;

	}

	/**
	 * This method will return the current system time in HH:MM:SS
	 * 
	 * @return
	 */
	private String getCurrentSystemTime() {

		DateFormat dateFormat = new SimpleDateFormat(
				FDPConstant.FDP_DB_SAVE_DATE_PATTERN);
		Date date = new Date();
		return dateFormat.format(date);
	}

	private String getDayOfWeek() {
		Calendar now = Calendar.getInstance();
		String[] strDays = new String[] { "sunday", "monday", "tuesday",
				"wednesday", "thursday", "friday", "saturday" };
		return strDays[now.get(Calendar.DAY_OF_WEEK) - 1];
	}

}
