package com.ericsson.fdp.business.util;

import java.util.HashMap;
import java.util.Map;

import ch.qos.logback.classic.Logger;

import com.ericsson.fdp.business.enums.AuxRequestParam;
import com.ericsson.fdp.common.constants.FDPConstant;
import com.ericsson.fdp.common.enums.TariffEnquiryAttributeKeysEnum;
import com.ericsson.fdp.common.enums.TariffEnquiryOption;
import com.ericsson.fdp.core.exception.ExecutionFailedException;
import com.ericsson.fdp.core.logging.FDPLogger;
import com.ericsson.fdp.core.request.FDPRequest;

/**
 * This class is a utility class that works for Tariff-Enquiry.
 * 
 * @author Ericsson
 */
public class TariffEnquiryUtil {

	/**
	 * Instantiates a new command Tariff Enquiry util.
	 */
	private TariffEnquiryUtil() {

	}

	/**
	 * This method will updated the value in the FDP request
	 * 
	 * @param fdpRequest
	 *            the FDP request
	 * @param tariffOptions
	 *            the tariff options map
	 * @param tariffEnquiryOption
	 *            the tariff options
	 */
	@SuppressWarnings("unchecked")
	public static void updateValuesInRequest(final FDPRequest fdpRequest, final Map<String, String[]> tariffOptions,
			final TariffEnquiryOption tariffEnquiryOption) {

		Map<TariffEnquiryOption, Map<String, String[]>> valuesAlreadyUpdated = (Map<TariffEnquiryOption, Map<String, String[]>>) fdpRequest
				.getAuxiliaryRequestParameter(AuxRequestParam.TEMP_TARIFF_ENQUIRY_VALUES);
		if (valuesAlreadyUpdated == null) {
			valuesAlreadyUpdated = new HashMap<TariffEnquiryOption, Map<String, String[]>>();
		}
		if (valuesAlreadyUpdated.get(tariffEnquiryOption) == null) {
			valuesAlreadyUpdated.put(tariffEnquiryOption, new HashMap<String, String[]>());
		}

		Map<String, String[]> tariffOptionsAlreadyFound = valuesAlreadyUpdated.get(tariffEnquiryOption);
		tariffOptionsAlreadyFound.putAll(tariffOptions);
		RequestUtil.putAuxiliaryValueInRequest(fdpRequest, AuxRequestParam.TEMP_TARIFF_ENQUIRY_VALUES,
				valuesAlreadyUpdated);
	}

	/**
	 * This method will create a key for fetching cached values for tariff
	 * enquiry parameters.
	 * 
	 * @param fdpNode
	 *            the FDP node
	 * @param tariffEnquiryOption
	 *            the tariff enquiry option
	 * @return key
	 * 
	 */
	public static String createKey(final TariffEnquiryOption tariffEnquiryOption, Logger circleLogger, final TariffEnquiryAttributeKeysEnum attributeEnum) throws ExecutionFailedException{
		String key = null;
		if(null != attributeEnum) {
			key = attributeEnum.getAttributeKey()+tariffEnquiryOption.getOptionId();
		}
		if(null == key) {
			FDPLogger.debug(circleLogger,TariffEnquiryUtil.class , "createKey()", "Got NULL Key for OptionType:"+tariffEnquiryOption.getName());
			throw new ExecutionFailedException("Got NULL Key for OptionType:"+tariffEnquiryOption.getName());
		}
		return key;
	}
	
	/**
	 * This method will generate the tariff Enquiry Attributes Enum(AttributesName) as key to be mapped with database.
	 * @param type
	 * @param span
	 * @param network
	 * @return
	 */
	public static String prepareTariffEnquiryAttributesEnumKeyGenerator(final String type, final String span, final String network) {
		StringBuffer sb = new StringBuffer();
		sb.append(type);
		sb.append(FDPConstant.TARIFF_ENQUIRY_ATTRIBUTE_ENUM_SEPERATOR);
		sb.append(span);
		if(network != null) {
			sb.append(FDPConstant.TARIFF_ENQUIRY_ATTRIBUTE_ENUM_SEPERATOR);
			sb.append(network);
		}
		return sb.toString().toUpperCase();
	}
}
