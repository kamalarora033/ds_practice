package com.ericsson.fdp.business.step.execution.impl.tariffEnquiry.valueCalculation;

import java.util.HashMap;
import java.util.Map;

import javax.annotation.Resource;

import ch.qos.logback.classic.Logger;

import com.ericsson.fdp.business.convertor.Convertor;
import com.ericsson.fdp.business.enums.AuxRequestParam;
import com.ericsson.fdp.business.enums.ConfigurationKey;
import com.ericsson.fdp.business.enums.ConversionOption;
import com.ericsson.fdp.business.enums.TariffEnquiryNotificationOptions;
import com.ericsson.fdp.business.tariffenquiry.command.impl.TariffEnquiryDisplayFormatImpl;
import com.ericsson.fdp.business.tariffenquiry.configimport.constants.TariffConstants;
import com.ericsson.fdp.business.util.NotificationUtil;
import com.ericsson.fdp.business.util.RequestUtil;
import com.ericsson.fdp.business.util.TariffEnquiryUtil;
import com.ericsson.fdp.common.constants.FDPConstant;
import com.ericsson.fdp.common.enums.ModuleType;
import com.ericsson.fdp.common.enums.Status;
import com.ericsson.fdp.common.enums.TariffEnquiryAttributeKeysEnum;
import com.ericsson.fdp.common.enums.TariffEnquiryCalculationOptions;
import com.ericsson.fdp.common.enums.TariffEnquiryOption;
import com.ericsson.fdp.common.enums.TariffEnquiryOptionValues;
import com.ericsson.fdp.common.util.StringUtil;
import com.ericsson.fdp.common.vo.FDPCircle;
import com.ericsson.fdp.core.cache.AppCacheSubStore;
import com.ericsson.fdp.core.dsm.framework.service.FDPCache;
import com.ericsson.fdp.core.entity.FDPTariffValues;
import com.ericsson.fdp.core.exception.ExecutionFailedException;
import com.ericsson.fdp.core.logging.FDPLogger;
import com.ericsson.fdp.core.request.FDPRequest;
import com.ericsson.fdp.core.serviceprovisioning.bean.FDPAppBag;
import com.ericsson.fdp.core.serviceprovisioning.bean.FDPMetaBag;
import com.ericsson.fdp.core.utils.ApplicationConfigUtil;

public class TariffEnquiryValueCalculator {

	@Resource(lookup = "java:app/fdpCoreServices-1.0/ApplicationConfigCache")
	private FDPCache<FDPAppBag, Object> applicationConfigCache;

	public Map<TariffEnquiryNotificationOptions, String> getTariffDetails(TariffEnquiryOption tariffEnquiryOption,
			String intersectedValue, FDPRequest fdpRequest, Logger circleLogger,final TariffEnquiryAttributeKeysEnum tariffEnquiryAttributeKeysEnum) throws ExecutionFailedException {
		Map<TariffEnquiryNotificationOptions, String> outputMap = new HashMap<TariffEnquiryNotificationOptions, String>();
		TariffEnquiryOptionValues tariffEnquiryOptionValues = TariffEnquiryOptionValues
				.getTariffEnquiryOptionValue(tariffEnquiryOption);
		FDPLogger.debug(circleLogger, getClass(), "getTariffDetails()",
				"Entered getTariffDetails for tariffEnquiryOptionValues:"
						+ tariffEnquiryOptionValues.getTariffEnquiryOption().getName() + ", for requestId:"
						+ fdpRequest.getRequestId());
		outputMap.put(TariffEnquiryNotificationOptions.NAME,
				getName(tariffEnquiryOptionValues, intersectedValue, fdpRequest, circleLogger,tariffEnquiryAttributeKeysEnum));
		outputMap.put(TariffEnquiryNotificationOptions.VALUE,
				getValue(tariffEnquiryOptionValues, intersectedValue, fdpRequest, circleLogger,outputMap,tariffEnquiryAttributeKeysEnum));
		outputMap.put(TariffEnquiryNotificationOptions.VALIDITY,
				getValidity(tariffEnquiryOptionValues, intersectedValue, fdpRequest, circleLogger));
		outputMap.put(TariffEnquiryNotificationOptions.UNIT,
				getUnit(tariffEnquiryOptionValues, intersectedValue, fdpRequest, circleLogger));
		outputMap.put(TariffEnquiryNotificationOptions.ATTRIBUTE_TYPE, tariffEnquiryOption.getOptionId());
		outputMap.put(TariffEnquiryNotificationOptions.TARIFF_TYPE, tariffEnquiryAttributeKeysEnum.getAttributeName());
		outputMap.put(TariffEnquiryNotificationOptions.ATTRIBUTE_ID, intersectedValue);
		FDPLogger.debug(circleLogger, getClass(), "getTariffDetails()", "Returning tariff Details map:" + outputMap
				+ ", for requestId:" + fdpRequest.getRequestId());
		return outputMap;
	}

	/**
	 * This method will fetch the tariff unit value
	 * 
	 * @param tariffEnquiryOption
	 *            the tariff option
	 * @param intersectedValue
	 *            the intersected value between cache and request values
	 * @param fdpRequest
	 *            the FDP request
	 * @return value return the unit type.
	 */
	private String getUnit(TariffEnquiryOptionValues tariffEnquiryOption, String intersectedValue,
			FDPRequest fdpRequest, Logger circleLogger) {
		// TODO Auto-generated method stub
		// TODO: This is still unknown and needs to be clarified by Manmeet.
		return FDPConstant.EMPTY_STRING;
	}

	/**
	 * This method returns the tariff validity
	 * 
	 * @param tariffEnquiryOption
	 *            the tariff option
	 * @param intersectedValue
	 *            the intersected value between cache and request values
	 * @param fdpRequest
	 *            the FDP request
	 * @return value the tariff validity
	 * @throws ExecutionFailedException
	 *             the execution exception.
	 */
	public String getValidity(TariffEnquiryOptionValues tariffEnquiryOption, String intersectedValue,
			FDPRequest fdpRequest, Logger circleLogger) throws ExecutionFailedException {
		String value = null;
		if (TariffEnquiryCalculationOptions.REGULAR.equals(tariffEnquiryOption.getTariffEnquiryCalculationOptions())) {
			value = getValueAndValidity(tariffEnquiryOption.getTariffEnquiryOption(), intersectedValue, fdpRequest, 1);
		} else {
			value = FDPConstant.EMPTY_STRING;
		}
		return value;
	}

	/**
	 * This method will get value against tariff details.
	 * 
	 * @param tariffEnquiryOption
	 *            the tariff option
	 * @param intersectedValue
	 *            the intersected value from cache and request
	 * @param fdpRequest
	 *            the FDP request
	 * @return value returns the tariff value
	 * @throws ExecutionFailedException
	 *             the execution exception
	 */
	@SuppressWarnings("unchecked")
	private String getValue(TariffEnquiryOptionValues tariffEnquiryOption, String intersectedValue,
			FDPRequest fdpRequest, Logger circleLogger, Map<TariffEnquiryNotificationOptions, String> outputMap,final TariffEnquiryAttributeKeysEnum tariffEnquiryAttributeKeysEnum) throws ExecutionFailedException {
		String value = null;
		FDPLogger.debug(
				circleLogger,
				getClass(),
				"getValue()",
				"Entered getName for tariffEnquiryOptionValues:"
						+ tariffEnquiryOption.getTariffEnquiryOption().getName() + ", for requestId:"
						+ fdpRequest.getRequestId());
		String status = Status.SUCCESS.name();
		if (TariffEnquiryCalculationOptions.REGULAR.equals(tariffEnquiryOption.getTariffEnquiryCalculationOptions())) {
			value = getAttributeTariffValue(fdpRequest, tariffEnquiryOption.getTariffEnquiryOption(), circleLogger,
					tariffEnquiryAttributeKeysEnum, intersectedValue);
			if (null == value) {
				value = getValueAndValidity(tariffEnquiryOption.getTariffEnquiryOption(), intersectedValue, fdpRequest,
						0);
			FDPLogger.debug(circleLogger, getClass(), "getValue()", "Got value:" + value + ", for requestId:"
					+ fdpRequest.getRequestId() + ", intersected value:" + intersectedValue);
			if (null != tariffEnquiryOption.getUnitType() && isTariffValueConversionAllowedForChannel(fdpRequest)) {
				String unitDisplayKey = prepareUnitConversionkey(tariffEnquiryOption, intersectedValue, fdpRequest);
				FDPLogger.debug(circleLogger, getClass(), "getValue()", "Got unit-conversion-display Key:"
						+ unitDisplayKey + ", for requestId:" + fdpRequest.getRequestId() + ", intersected value:"
						+ intersectedValue);
					if (unitDisplayKey != null) {
					value = performValueUnitConversion(fdpRequest, unitDisplayKey, value, circleLogger);
					FDPLogger.debug(circleLogger, getClass(), "getValue()", "Got convetered value:" + value
							+ ", for requestId:" + fdpRequest.getRequestId() + ", intersected value:"
							+ intersectedValue);
				}
			}
			}
		} else if (TariffEnquiryOption.SC.equals(tariffEnquiryOption.getTariffEnquiryOption())) {
			String key = TariffEnquiryUtil.createKey(TariffEnquiryOption.SC, circleLogger,tariffEnquiryAttributeKeysEnum);
			FDPLogger.debug(circleLogger, getClass(), "getValue()",
					"Got Key:" + key + ", for requestId:" + fdpRequest.getRequestId() + ", intersected value:"
							+ intersectedValue);
			FDPTariffValues tariffValues = (FDPTariffValues) ApplicationConfigUtil.getMetaDataCache().getValue(
					new FDPMetaBag(fdpRequest.getCircle(), ModuleType.TARIFF_ENQUIRY_ATTRIBUTES, key));
			FDPLogger.debug(circleLogger, getClass(), "getValue()", "Got TariffValue from Cache:" + tariffValues
					+ ", for requestId:" + fdpRequest.getRequestId() + ", intersected value:" + intersectedValue);
			if (tariffValues == null) {
				status = Status.FAILURE.name();
				value = TariffConstants.DEFAULT_TARIFF_NOT_FOUND;
				FDPLogger.debug(
						circleLogger,
						getClass(),
						"getValue()",
						"Tariff Cached Value not Found for SC, Setting EMPTY String for requestId:"
								+ fdpRequest.getRequestId() + ", intersected value:" + intersectedValue);
			} else {
				Map<String,Map<String,String>> valueMap = (Map<String,Map<String,String>>) tariffValues.getTariffValue();
				Map<String,String> valueNstatus = valueMap.get(intersectedValue);
				value = valueNstatus.get(TariffConstants.TARIFF_SERVICE_CLASS_MAP_VALUE);
				status = valueNstatus.get(TariffConstants.TARIFF_SERVICE_CLASS_MAP_STATUS);
			}
		}
		FDPLogger.debug(
				circleLogger,
				getClass(),
				"getValue()",
				"Exiting getValue for tariffEnquiryOptionValues:"
						+ tariffEnquiryOption.getTariffEnquiryOption().getName() + ", for requestId:"
						+ fdpRequest.getRequestId() + ", with Value:" + value+", and status:"+status);
		outputMap.put(TariffEnquiryNotificationOptions.STATUS, status);
		return value;
	}

	/**
	 * This method will perform Unit Value Conversion base on configuration.
	 * 
	 * @param fdpRequest
	 * @param unitDisplayKey
	 * @param value
	 * @return
	 * @throws ExecutionFailedException
	 */
	private String performValueUnitConversion(final FDPRequest fdpRequest, final String unitDisplayKey,
			final String value, Logger circleLogger) throws ExecutionFailedException {
		FDPAppBag fdpAppBag = new FDPAppBag(AppCacheSubStore.COMMAND_TARIFF_UNIT_MAPPING, unitDisplayKey);
		String unitDisplayFromCache = (String) applicationConfigCache.getValue(fdpAppBag);
		FDPLogger
				.debug(circleLogger, getClass(), "Entered performValueUnitConversion()", "Got value from Cache:"
						+ unitDisplayFromCache + ", for requestId:" + fdpRequest.getRequestId() + ", for Key:"
						+ unitDisplayKey);
		String convertedValue = null;
		if (unitDisplayFromCache != null) {
			String conversionConfiguration = getConversionAllowedConfiguration(fdpRequest);
			FDPLogger.debug(
					circleLogger,
					getClass(),
					"performValueUnitConversion()",
					"Conversion-Allowed-Configuration:" + conversionConfiguration + ", for requestId:"
							+ fdpRequest.getRequestId());
			ConversionOption conversionOption = ConversionOption
					.getConversionOptionValueForConfiguration(conversionConfiguration);
			TariffEnquiryDisplayFormatImpl tariffEnquiryDisplayFormatImpl = (TariffEnquiryDisplayFormatImpl) ApplicationConfigUtil
					.getMetaDataCache()
					.getValue(
							new FDPMetaBag(fdpRequest.getCircle(), ModuleType.UNIT_DISPLAY_FORMAT, unitDisplayFromCache));
			FDPLogger.debug(circleLogger, getClass(), "performValueUnitConversion()", "Got Cached Value:"
					+ tariffEnquiryDisplayFormatImpl + ",against Key:" + unitDisplayFromCache + ", for requestId:"
					+ fdpRequest.getRequestId());
			if (null != tariffEnquiryDisplayFormatImpl && null != conversionOption) {
				Convertor convertor = (tariffEnquiryDisplayFormatImpl.getDisplayFormat()).get(conversionOption);
				String tempconvertedValue = convertor.execute(value, fdpRequest);
				convertedValue = (null == tempconvertedValue) ? value : tempconvertedValue;
			}
		}
		FDPLogger.debug(circleLogger, getClass(), "Exited performValueUnitConversion()", "With converted-value"
				+ convertedValue + ", for requestId:" + fdpRequest.getRequestId());
		return convertedValue;
	}

	/**
	 * This method will provide the configuration for Value Unit conversion.
	 * 
	 * 1=> Means No-Conversion(DEFAULT behavior, Even if Configuration not set).
	 * 0=> Means Do-Conversion
	 * 
	 * @return
	 */
	private String getConversionAllowedConfiguration(final FDPRequest fdpRequest) {
		String value = null;
		try {
			value = fdpRequest.getCircle().getConfigurationKeyValueMap()
					.get(ConfigurationKey.TARIFF_ENQUIRY_UNIT_CONVERSION_ALLOW.getAttributeName());
		} catch (Exception e) {
			value = "no";
		}
		return ((value == null) ? "no" : value);
	}

	/**
	 * This method will create the key to be used from Cache for Value Unit
	 * Conversion.
	 * 
	 * @param tariffEnquiryOption
	 * @param intersectedValue
	 * @param fdpRequest
	 * @return
	 */
	private String prepareUnitConversionkey(TariffEnquiryOptionValues tariffEnquiryOption, String intersectedValue,
			FDPRequest fdpRequest) {
		String unitTypeValue = getValueAndValidity(tariffEnquiryOption.getTariffEnquiryOption(), intersectedValue,
				fdpRequest, 2);
		return tariffEnquiryOption.getCommandName() + FDPConstant.PARAMETER_SEPARATOR
				+ tariffEnquiryOption.getUnitType() + FDPConstant.PARAMETER_SEPARATOR + unitTypeValue;
	}

	/**
	 * This method will fetch tariff name as per attributes saved.
	 * 
	 * @param tariffEnquiryOption
	 *            the tariff option
	 * @param intersectedValue
	 *            the intersected value from cache and request
	 * @param fdpRequest
	 *            the FDP request
	 * @return value the tariff name
	 * @throws ExecutionFailedException
	 *             the execution exception
	 */
	private String getName(TariffEnquiryOptionValues tariffEnquiryOption, String intersectedValue,
			FDPRequest fdpRequest, Logger circleLogger, final TariffEnquiryAttributeKeysEnum tariffEnquiryAttributeKeysEnum) throws ExecutionFailedException {
		String value = null;
		FDPLogger.debug(
				circleLogger,
				getClass(),
				"getName()",
				"Entered getName for tariffEnquiryOptionValues:"
						+ tariffEnquiryOption.getTariffEnquiryOption().getName() + ", for requestId:"
						+ fdpRequest.getRequestId()+" , intersectedValue:"+intersectedValue);
		FDPCircle fdpCircleWithCSAttr = (FDPCircle) ApplicationConfigUtil.getApplicationConfigCache().getValue(
				new FDPAppBag(AppCacheSubStore.CS_ATTRIBUTES, fdpRequest.getCircle().getCircleCode()));
		Map<String, Map<String, String>> csAttrMap = fdpCircleWithCSAttr.getCsAttributesKeyValueMap();
		if (null != csAttrMap) {
			FDPLogger.debug(circleLogger, getClass(), "getName()", "Got CS-Attributes Key-Value Map:" + csAttrMap
					+ ", for requestId:" + fdpRequest.getRequestId());
			Map<String, String> csAttrClass = csAttrMap.get(tariffEnquiryOption.getCsAttrName());
			if (csAttrClass != null) {
				value = csAttrClass.get(intersectedValue);
				FDPLogger.debug(circleLogger, getClass(), "getName()", "Got CS-Attributes Map:" + csAttrClass
						+ ", for requestId:" + fdpRequest.getRequestId() + ", for intersected value:"
						+ intersectedValue + ", value:" + value);
			}
		}
		if (TariffEnquiryOption.SC.equals(tariffEnquiryOption.getTariffEnquiryOption()) && value == null) {
			value = prepareServiceClassBalanceEnquiryNotificationText(fdpRequest,tariffEnquiryAttributeKeysEnum,circleLogger);
		}
		FDPLogger.debug(
				circleLogger,
				getClass(),
				"getName()",
				"Exiting getName for tariffEnquiryOptionValues:"
						+ tariffEnquiryOption.getTariffEnquiryOption().getName() + ", for requestId:"
						+ fdpRequest.getRequestId() + ", with Name:" + value);
		return (value == null) ? FDPConstant.EMPTY_STRING : value;
	}

	@SuppressWarnings("unchecked")
	private String getValueAndValidity(TariffEnquiryOption tariffEnquiryOption, String intersectedValue,
			FDPRequest fdpRequest, int position) {
		String value = FDPConstant.EMPTY_STRING;
		Map<TariffEnquiryOption, Map<String, String[]>> tariffValuesMap = (Map<TariffEnquiryOption, Map<String, String[]>>) fdpRequest
				.getAuxiliaryRequestParameter(AuxRequestParam.TEMP_TARIFF_ENQUIRY_VALUES);
		if (tariffValuesMap != null) {
			Map<String, String[]> otherValueMap = tariffValuesMap.get(tariffEnquiryOption);
			if (otherValueMap != null) {
				String otherValue[] = otherValueMap.get(intersectedValue);
				if (otherValue != null && otherValue.length > position) {
					if (otherValue[position] != null) {
						value = otherValue[position];
					}
				}
			}
		}
		return value;
	}
	
	/**
	 * This method updates the value of TYPE,SPAN,NETWORK in AUX-PARAM and create notification text.
	 * 
	 * @param fdpRequest
	 * @return
	 * @throws ExecutionFailedException
	 */
	private String prepareServiceClassBalanceEnquiryNotificationText(final FDPRequest fdpRequest,
			final TariffEnquiryAttributeKeysEnum tariffEnquiryAttributeKeysEnum, Logger circleLogger)
			throws ExecutionFailedException {
		String name = null;
		try {
			String type = (null != tariffEnquiryAttributeKeysEnum.getType()) ? (tariffEnquiryAttributeKeysEnum
					.getType().getNotificationDisplayText()) : FDPConstant.EMPTY_STRING;
			String span = (null != tariffEnquiryAttributeKeysEnum.getSpan()) ? (tariffEnquiryAttributeKeysEnum
					.getSpan().getNotificationDisplayText()) : FDPConstant.EMPTY_STRING;
			String network = (null != tariffEnquiryAttributeKeysEnum.getNetwork()) ? (tariffEnquiryAttributeKeysEnum
					.getNetwork().getNotificationDisplayText()) : FDPConstant.EMPTY_STRING;
			RequestUtil.putAuxiliaryValueInRequest(fdpRequest, AuxRequestParam.TARIFF_TYPE, type);
			RequestUtil.putAuxiliaryValueInRequest(fdpRequest, AuxRequestParam.TARIFF_SPAN, span);
			RequestUtil.putAuxiliaryValueInRequest(fdpRequest, AuxRequestParam.TARIFF_NETWORK, network);
			final Long notificationId = FDPConstant.TARIFF_SERVICE_CLASS_DETAILS_NOTIFICATION_ID
					- fdpRequest.getCircle().getCircleId();
			name = NotificationUtil.createNotificationText(fdpRequest, notificationId, circleLogger);
		} catch (Exception e) {
			throw new ExecutionFailedException(
					"Error while creating Service class Balance Enquiry Notification Text, Actual Error:"
							+ e.getMessage(), e);
		}
		name = (null == name) ? FDPConstant.EMPTY_STRING : name;
		return name;
	}
	
	/**
	 * This method is used to check the tariff value conversion configuration
	 * for channel.
	 * 
	 * @param channelType
	 * @return
	 * @throws ExecutionFailedException
	 */
	private boolean isTariffValueConversionAllowedForChannel(final FDPRequest fdpRequest)
			throws ExecutionFailedException {
		boolean isAllowed = false;
		try {
			final String value = fdpRequest.getCircle().getConfigurationKeyValueMap()
					.get(ConfigurationKey.TARIFF_ENQUIRY_VALUE_CONVERSION_ALLOWED_CHANNELS.getAttributeName());
			isAllowed = (!StringUtil.isNullOrEmpty(value) ? value.toUpperCase()
					.contains(fdpRequest.getChannel().name()) : false);
		} catch (Exception e) {

		}
		return isAllowed;
	}
	
	/**
	 * This method fetches the attribute tariff values from cache.
	 * 
	 * @param tariffEnquiryOption
	 * @param circleLogger
	 * @param attributeEnum
	 * @return
	 * @throws ExecutionFailedException
	 */
	@SuppressWarnings("unchecked")
	private String getAttributeTariffValue(final FDPRequest fdpRequest, final TariffEnquiryOption tariffEnquiryOption,
			final Logger circleLogger, final TariffEnquiryAttributeKeysEnum attributeEnum, final String intersectedValue)
			throws ExecutionFailedException {
		String attributeTariffValue = null;
		final StringBuffer key = new StringBuffer();
		key.append(attributeEnum.getAttributeKey());
		key.append(tariffEnquiryOption.getAttributeTariffValueId());
		if (key.length() > 0) {
			FDPLogger.debug(
					circleLogger,
					getClass(),
					"getAttributeTariffValue()",
					"Prepared Key :"
							+ key + "intersectedValue:"+intersectedValue+", for requestId:"
							+ fdpRequest.getRequestId());
			FDPTariffValues tariffValues = (FDPTariffValues) ApplicationConfigUtil.getMetaDataCache().getValue(
					new FDPMetaBag(fdpRequest.getCircle(), ModuleType.TARIFF_ENQUIRY_ATTRIBUTES, key));
			if (null != tariffValues) {
				Map<String, Map<String, String>> valueMap = (Map<String, Map<String, String>>) tariffValues
						.getTariffValue();
				if (null != valueMap) {
					Map<String, String> valueNstatus = valueMap.get(intersectedValue);
					if(null != valueNstatus) {
						//System.out.println("##################### valueNstatus:"+valueNstatus);
						String status = valueNstatus.get(TariffConstants.TARIFF_SERVICE_CLASS_MAP_STATUS);
						if(null != status && Status.SUCCESS.name().equals(status)) {
					attributeTariffValue = valueNstatus.get(TariffConstants.TARIFF_SERVICE_CLASS_MAP_VALUE);
				}
			}
		}
			}
		}
		FDPLogger.debug(
				circleLogger,
				getClass(),
				"getAttributeTariffValue()",
				"Found Value:"
						+ attributeTariffValue + "intersectedValue:"+intersectedValue+", for requestId:"
						+ fdpRequest.getRequestId());
		return attributeTariffValue;
	}
}
