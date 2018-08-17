package com.ericsson.fdp.business.step.execution.impl.tariffEnquiry;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.ejb.Stateless;
import javax.inject.Inject;

import ch.qos.logback.classic.Logger;

import com.ericsson.fdp.business.attributesfilter.AttributeFilter;
import com.ericsson.fdp.business.constants.FDPStepResponseConstants;
import com.ericsson.fdp.business.enums.AuxRequestParam;
import com.ericsson.fdp.business.enums.RequestMetaValuesKey;
import com.ericsson.fdp.business.enums.ServiceStepOptions;
import com.ericsson.fdp.business.enums.TariffEnquiryNotificationOptions;
import com.ericsson.fdp.business.request.impl.FDPStepResponseImpl;
import com.ericsson.fdp.business.step.execution.FDPExecutionService;
import com.ericsson.fdp.business.step.execution.impl.tariffEnquiry.valueCalculation.TariffEnquiryIdCalculator;
import com.ericsson.fdp.business.step.execution.impl.tariffEnquiry.valueCalculation.TariffEnquiryValueCalculator;
import com.ericsson.fdp.business.tariffenquiry.configimport.constants.TariffConstants;
import com.ericsson.fdp.business.util.LoggerUtil;
import com.ericsson.fdp.business.util.TariffEnquiryUtil;
import com.ericsson.fdp.business.vo.FDPTariffEnquiryCsvAttributeNotificationMapVO;
import com.ericsson.fdp.common.constants.FDPConstant;
import com.ericsson.fdp.common.enums.DynamicMenuAdditionalInfoKey;
import com.ericsson.fdp.common.enums.ModuleType;
import com.ericsson.fdp.common.enums.Status;
import com.ericsson.fdp.common.enums.TariffEnquiryAttributeKeysEnum;
import com.ericsson.fdp.common.enums.TariffEnquiryCalculationOptions;
import com.ericsson.fdp.common.enums.TariffEnquiryOption;
import com.ericsson.fdp.common.enums.TariffEnquiryOptionValues;
import com.ericsson.fdp.core.cache.AppCacheSubStore;
import com.ericsson.fdp.core.entity.FDPTariffValues;
import com.ericsson.fdp.core.exception.ExecutionFailedException;
import com.ericsson.fdp.core.logging.FDPLogger;
import com.ericsson.fdp.core.node.FDPNode;
import com.ericsson.fdp.core.request.FDPRequest;
import com.ericsson.fdp.core.request.FDPStepResponse;
import com.ericsson.fdp.core.serviceprovisioning.bean.FDPAppBag;
import com.ericsson.fdp.core.serviceprovisioning.bean.FDPMetaBag;
import com.ericsson.fdp.core.utils.ApplicationConfigUtil;

/**
 * This class is used to find the tariff enquiry.
 * 
 * @author Ericsson
 * 
 */
@Stateless
public class TariffEnquiryServiceImpl implements FDPExecutionService {

	/**
	 * Dependency Injection for TariffEnquiryIdCalculator
	 */
	@Inject
	private TariffEnquiryIdCalculator tariffEnquiryIdCalculator;

	/**
	 * Dependency Injection for TariffEnquiryValueCalculator
	 */
	@Inject
	private TariffEnquiryValueCalculator tariffEnquiryValueCalculator;

	@Inject
	private AttributeFilter tariffOptionFilter;
	
	@Override
	@SuppressWarnings("unchecked")
	public FDPStepResponse executeService(final FDPRequest fdpRequest, final Object... additionalInformations)
			throws ExecutionFailedException {
		final Logger circleLogger = LoggerUtil.getSummaryLoggerFromRequest(fdpRequest);
		FDPLogger.debug(circleLogger, getClass(), "executeService()",
				"Executing Service for requestId:" + fdpRequest.getRequestId());
		if (additionalInformations != null && additionalInformations[0] != null
				&& additionalInformations[0] instanceof Map<?, ?>) {
			Map<ServiceStepOptions, String> additionalInfo = (Map<ServiceStepOptions, String>) additionalInformations[0];
			FDPLogger.debug(circleLogger, getClass(), "executeService()", "additionalInfo:" + additionalInfo);
			String requestedAttributes = additionalInfo.get(ServiceStepOptions.TARIFF_ENQUIRY_ATTRIBUTES);
			if((null != requestedAttributes) && (requestedAttributes.length() >0)) {
				return getResponseForRequestedAttribute(requestedAttributes, fdpRequest, circleLogger);	
			} else {
				throw new ExecutionFailedException("TARIFF_ENQUIRY_ATTRIBUTES configuration missing with DM.");
			}
		} else {
			throw new ExecutionFailedException("Input parameters are missing.");
		}
	}

	/**
	 * This method is used to get the response for the requested attributes.
	 * 
	 * @param requestedAttributes
	 *            the requested attributes.
	 * @param fdpRequest
	 *            the request.
	 * @param circleLogger
	 *            the logger.
	 * @return stepResponseImpl the response.
	 * @throws ExecutionFailedException
	 *             Exception in execution.
	 */
	private FDPStepResponse getResponseForRequestedAttribute(final String requestedAttributes,
			final FDPRequest fdpRequest, Logger circleLogger) throws ExecutionFailedException {
		String[] requestedTariffOptions = requestedAttributes.split(FDPConstant.TARIFF_ENQUIRY_OPTION_SEPERATOR);
		List<TariffEnquiryOption> tariffEnquiryOptions = new ArrayList<TariffEnquiryOption>();
		for (String string : requestedTariffOptions) {
			tariffEnquiryOptions.add(TariffEnquiryOption.getTariffEnquiryOption(string));
			FDPLogger.debug(circleLogger, getClass(), "getResponseForRequestedAttribute()", "Got [" + string
					+ "] from request Attributes for requestId:" + fdpRequest.getRequestId());
		}
		List<Map<TariffEnquiryNotificationOptions, String>> tariffEnquiryValues = new ArrayList<Map<TariffEnquiryNotificationOptions, String>>();
		Map<AuxRequestParam, Object> tariffValues = new HashMap<AuxRequestParam, Object>();
		for (final TariffEnquiryAttributeKeysEnum tariffEnquiryAttributeKeysEnum : getDMConfiguredTariffOptions(
				fdpRequest, circleLogger)) {
			FDPLogger.debug(circleLogger, getClass(), "getResponseForRequestedAttribute()",
					"Processing for TariffEnquiryAttributeKeysEnum:" + tariffEnquiryAttributeKeysEnum+" , for requestId:"+fdpRequest.getRequestId());
			updateTariffValues(tariffEnquiryOptions, tariffEnquiryValues, fdpRequest, circleLogger, tariffValues,
					tariffEnquiryAttributeKeysEnum);
		}
		FDPStepResponseImpl stepResponseImpl = new FDPStepResponseImpl();
		stepResponseImpl.addStepResponseValue(FDPStepResponseConstants.STATUS_KEY, true);
		tariffValues.put(AuxRequestParam.TARIFF_VALUES, tariffEnquiryValues);
		stepResponseImpl.addStepResponseValue(FDPStepResponseConstants.TARIFF_VALUES, tariffEnquiryValues);
		stepResponseImpl.addStepResponseValue(FDPStepResponseConstants.AUX_PARAM_VALUES, tariffValues);
		return stepResponseImpl;
	}

	/**
	 * This method will be used to update the tariff values.
	 * 
	 * @param tariffEnquiryOptions
	 *            the options for which the values will be updated.
	 * @param tariffEnquiryValues
	 *            the enquiry values.
	 * @param fdpRequest
	 *            the request.
	 * @param circleLogger
	 *            the logger.
	 * @throws ExecutionFailedException
	 *             Exception in execution.
	 */
	private void updateTariffValues(final List<TariffEnquiryOption> tariffEnquiryOptions,
			final List<Map<TariffEnquiryNotificationOptions, String>> tariffEnquiryValues, final FDPRequest fdpRequest,
			Logger circleLogger, final Map<AuxRequestParam, Object> tariffValues, final TariffEnquiryAttributeKeysEnum tariffEnquiryAttributeKeysEnum) throws ExecutionFailedException {
		for (TariffEnquiryOption tariffEnquiryOption : tariffEnquiryOptions) {
			FDPLogger.debug(circleLogger, getClass(), "updateTariffValues()", "Processing Tariff Option:["
					+ tariffEnquiryOption.getName() + "] , for requestId:"+fdpRequest.getRequestId());
			Set<String> tariffEnquiryCachedValues = getTariffValuesFromCache(tariffEnquiryOption, fdpRequest,
					circleLogger, tariffEnquiryAttributeKeysEnum);
			if (null == tariffEnquiryCachedValues || tariffEnquiryCachedValues.size() == 0) {
				FDPLogger.debug(circleLogger, getClass(), "updateTariffValues()",
						"Cache Value not found for Option type:" + tariffEnquiryOption.getName()+", for requestId:"+fdpRequest.getRequestId());
				continue;
			}
			
			List<String> tariffEnquiryValuesForUser = tariffEnquiryIdCalculator.getTariffDetails(tariffEnquiryOption,
					fdpRequest, circleLogger);
			if (null == tariffEnquiryValuesForUser || tariffEnquiryValuesForUser.size() == 0) {
				FDPLogger.debug(circleLogger, getClass(), "updateTariffValues()",
						"User Tariff Value not found for Option type:" + tariffEnquiryOption.getName()+", for requestId:"+fdpRequest.getRequestId());
				continue;
			}
			
			List<String> intersectedValues = getIntersection(tariffEnquiryCachedValues, tariffEnquiryValuesForUser);
			FDPLogger.debug(circleLogger, getClass(), "updateTariffValues()", "Got CachedValues:["
					+ tariffEnquiryCachedValues + "], UserValues:[" + tariffEnquiryValuesForUser
					+ "], IntersectedValues:[" + intersectedValues + "] for requestId:" + fdpRequest.getRequestId());
			if (TariffEnquiryOption.SC.equals(tariffEnquiryOption)
					&& isServiceClassIntersectionEmpty(intersectedValues, circleLogger, tariffValues)) {
				break;
			}
			
			intersectedValues = tariffOptionFilter.filter(intersectedValues, tariffEnquiryOption, fdpRequest);
			FDPLogger.debug(circleLogger, getClass(), "updateTariffValues()",
					"Filtered Value:" + intersectedValues + ", for TariffOption-Type:" + tariffEnquiryOption.getName()
							+ ", for requestId:" + fdpRequest.getRequestId());
			processIntersectedValues(tariffEnquiryValues, fdpRequest, circleLogger, tariffValues, tariffEnquiryOption,
					intersectedValues, tariffEnquiryAttributeKeysEnum);
		}
	}
	
	/**
	 * This method will update common tariff values from cache-values and
	 * user-values.
	 * 
	 * @param tariffEnquiryCachedValues
	 *            the tariffEnquiry values from cache.
	 * @param tariffEnquiryValuesForUser
	 *            the tariffEnquiry for user.
	 * @return list of String
	 */
	private List<String> getIntersection(final Set<String> tariffEnquiryCachedValues,
			final List<String> tariffEnquiryValuesForUser) {
		List<String> intersectedValues = new ArrayList<String>();
		if (tariffEnquiryCachedValues != null && tariffEnquiryValuesForUser != null) {
			for (String string : tariffEnquiryValuesForUser) {
				if (tariffEnquiryCachedValues.contains(string)) {
					intersectedValues.add(string);
				}
			}
		}
		return intersectedValues;
	}

	/**
	 * To Fetch Tariff Values from Cache.
	 * 
	 * @param tariffEnquiryOption
	 * @param fdpRequest
	 * @return
	 * @throws ExecutionFailedException
	 */
	@SuppressWarnings("unchecked")
	private Set<String> getTariffValuesFromCache(TariffEnquiryOption tariffEnquiryOption, FDPRequest fdpRequest, Logger circleLogger, final TariffEnquiryAttributeKeysEnum tariffEnquiryAttributeKeysEnum)
			throws ExecutionFailedException {
		String keyToBeUsed = TariffEnquiryUtil.createKey(tariffEnquiryOption, circleLogger,tariffEnquiryAttributeKeysEnum);
		FDPTariffValues tariffValues = (FDPTariffValues) ApplicationConfigUtil.getMetaDataCache().getValue(
				new FDPMetaBag(fdpRequest.getCircle(), ModuleType.TARIFF_ENQUIRY_ATTRIBUTES, keyToBeUsed));
		if (tariffValues == null) {
			return new HashSet<String>();
		}
		if (TariffEnquiryCalculationOptions.REGULAR.equals(TariffEnquiryOptionValues.getTariffEnquiryOptionValue(
				tariffEnquiryOption).getTariffEnquiryCalculationOptions())) {
			return (tariffValues != null) ? (Set<String>) tariffValues.getTariffValue() : null;
		} else {
			return ((Map<String, Map<String, String>>) tariffValues.getTariffValue()).keySet();
		}
	}

	/**
	 * This method will update the AUX PARAMS if failed service_class found.
	 * 
	 * @param tariffDetailMap
	 * @param tariffEnquiryOption
	 * @throws ExecutionFailedException
	 */
	@SuppressWarnings("unchecked")
	private void updateAuxParamForFailedSCTariffs(final String value, Logger circleLogger,
			final Map<AuxRequestParam, Object> tariffValues) throws ExecutionFailedException {

		String key = AppCacheSubStore.TARIFF_ENQUIRY_CSV_ATTRIBUTES_NOTI_MAP.getSubStore();
		Map<String, FDPTariffEnquiryCsvAttributeNotificationMapVO> attributeMap = (Map<String, FDPTariffEnquiryCsvAttributeNotificationMapVO>) ApplicationConfigUtil
				.getApplicationConfigCache().getValue(
						new FDPAppBag(AppCacheSubStore.TARIFF_ENQUIRY_CSV_ATTRIBUTES_NOTI_MAP, key));
		if (null == attributeMap) {
			FDPLogger.debug(circleLogger, getClass(), "updateAuxParamForFailedSCTariffs()",
					"CSV-Attribute Cache not Found.");
			throw new ExecutionFailedException("CSV-Attribute Cache not Found.");
		}
		FDPTariffEnquiryCsvAttributeNotificationMapVO attributeNotificationMapVO = attributeMap.get(value);
		if (null == attributeNotificationMapVO) {
			FDPLogger.debug(circleLogger, getClass(), "updateAuxParamForFailedSCTariffs()",
					"CSV-Attribute Cache value not found for Key:" + value);
			throw new ExecutionFailedException("CSV-Attribute Cache value not found for Key:" + value);
		}
		Map<String, Integer> fdpAttributeWithNotification = new HashMap<String, Integer>();
		fdpAttributeWithNotification.put(attributeNotificationMapVO.getFdpAtribute(),
				attributeNotificationMapVO.getNotificationId());
		tariffValues.put(AuxRequestParam.FAILURE_REASON, fdpAttributeWithNotification);
		FDPLogger.debug(circleLogger, getClass(), "updateAuxParamForFailedSCTariffs()",
				"Update AUX_FAILURE_REASON with value:" + attributeNotificationMapVO.getFdpAtribute());
	}

	/**
	 * This method checks if SC intersection is Empty then send
	 * Default-Tariff-Not-Found notification.
	 * 
	 * @param intersectedValues
	 * @param circleLogger
	 * @param tariffValues
	 * @return
	 * @throws ExecutionFailedException
	 */
	private boolean isServiceClassIntersectionEmpty(final List<String> intersectedValues, final Logger circleLogger,
			final Map<AuxRequestParam, Object> tariffValues) throws ExecutionFailedException {
		boolean isSCIntersectionEmpty = false;
		if (null != intersectedValues && intersectedValues.size() == 0) {
			String defaultTariffNotFound = TariffConstants.DEFAULT_TARIFF_NOT_FOUND;
			updateAuxParamForFailedSCTariffs(defaultTariffNotFound, circleLogger, tariffValues);
			isSCIntersectionEmpty = true;
		}
		return isSCIntersectionEmpty;
	}
	
	/**
	 * This method performs the tariff value calculations for the configured TariffOptions and user's attached values.
	 * 
	 * @param tariffEnquiryValues
	 * @param fdpRequest
	 * @param circleLogger
	 * @param tariffValues
	 * @param tariffEnquiryOption
	 * @param intersectedValues
	 * @throws ExecutionFailedException
	 */
	private void processIntersectedValues(
			final List<Map<TariffEnquiryNotificationOptions, String>> tariffEnquiryValues, final FDPRequest fdpRequest,
			Logger circleLogger, final Map<AuxRequestParam, Object> tariffValues,
			TariffEnquiryOption tariffEnquiryOption, List<String> intersectedValues,final TariffEnquiryAttributeKeysEnum tariffEnquiryAttributeKeysEnum) throws ExecutionFailedException {
		for (String intersectedValue : intersectedValues) {
			Map<TariffEnquiryNotificationOptions, String> tariffDetailMap = tariffEnquiryValueCalculator
					.getTariffDetails(tariffEnquiryOption, intersectedValue, fdpRequest, circleLogger,tariffEnquiryAttributeKeysEnum);
			if (null != tariffDetailMap) {
				tariffEnquiryValues.add(tariffDetailMap);
				FDPLogger.debug(circleLogger, getClass(), "processIntersectedValues()",
						"Adding Tariff-Intersected-Values to Map:" + tariffDetailMap + ", for OptionType:"
								+ tariffEnquiryOption);
				String tariffStatus = tariffDetailMap.get(TariffEnquiryNotificationOptions.STATUS);
				if (TariffEnquiryOption.SC.equals(tariffEnquiryOption)
						&& Status.FAILURE.name().equalsIgnoreCase(tariffStatus)) {
					FDPLogger.debug(circleLogger, getClass(), "processIntersectedValues()",
							"Failed SC Tariff Found for intersected-value:" + intersectedValue);
					String tariffValue = tariffDetailMap.get(TariffEnquiryNotificationOptions.VALUE);
					updateAuxParamForFailedSCTariffs(tariffValue, circleLogger, tariffValues);
				}
			}
		}
	}

	/**
	 * This method returns the list of all TariffOptions Configured.
	 * @param fdpRequest
	 * @return
	 * @throws ExecutionFailedException
	 */
	@SuppressWarnings("unchecked")
	private List<TariffEnquiryAttributeKeysEnum> getDMConfiguredTariffOptions(final FDPRequest fdpRequest,
			Logger circleLogger) throws ExecutionFailedException {
		List<TariffEnquiryAttributeKeysEnum> tariffEnquiryAttributeKeysEnums = null;
		FDPNode fdpNode = (FDPNode) fdpRequest.getValueFromRequest(RequestMetaValuesKey.NODE);
		if (null != fdpNode) {
			tariffEnquiryAttributeKeysEnums = (List<TariffEnquiryAttributeKeysEnum>) fdpNode
					.getAdditionalInfo(DynamicMenuAdditionalInfoKey.TARIFF_ENQUIRY_ADDITIONAL_INFO_DATA.name());
			if (null == tariffEnquiryAttributeKeysEnums || tariffEnquiryAttributeKeysEnums.size() == 0) {
				FDPLogger.debug(circleLogger, getClass(), "getDMConfiguredTariffOptions()", "TariffEnquiry Multiselect Combination not configured in DM");
				throw new ExecutionFailedException("TariffEnquiry Multiselect Combination not configured in DM");
			}
		} else {
			FDPLogger.debug(circleLogger, getClass(), "getDMConfiguredTariffOptions()",
					"Not able to get Node from request");
			throw new ExecutionFailedException("Not able to get Node from request");
		}
		FDPLogger.debug(circleLogger, getClass(), "getDMConfiguredTariffOptions()", "Configured TariffOption List:"
				+ tariffEnquiryAttributeKeysEnums);
		return tariffEnquiryAttributeKeysEnums;
	}

	@Override
	public FDPStepResponse performRollback(FDPRequest fdpRequest,
			Map<ServiceStepOptions, String> additionalInformation) {
		// TODO Auto-generated method stub
		return null;
	}
}
