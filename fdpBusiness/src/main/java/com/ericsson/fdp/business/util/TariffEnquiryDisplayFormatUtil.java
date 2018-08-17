package com.ericsson.fdp.business.util;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import com.ericsson.fdp.business.command.param.impl.CommandParamInput;
import com.ericsson.fdp.business.condition.Condition;
import com.ericsson.fdp.business.condition.impl.EqualsCondition;
import com.ericsson.fdp.business.condition.impl.GreaterThanCondition;
import com.ericsson.fdp.business.condition.impl.LessThanCondition;
import com.ericsson.fdp.business.condition.impl.NoCondition;
import com.ericsson.fdp.business.condition.impl.RangeCondition;
import com.ericsson.fdp.business.convertor.Convertor;
import com.ericsson.fdp.business.convertor.conversion.ConversionRules;
import com.ericsson.fdp.business.convertor.conversion.impl.ConversionRulesImpl;
import com.ericsson.fdp.business.convertor.displayConvertor.UnitDisplayFormat;
import com.ericsson.fdp.business.convertor.displayConvertor.impl.UnitDisplayFormatImpl;
import com.ericsson.fdp.business.convertor.impl.ConverterImpl;
import com.ericsson.fdp.business.enums.ConversionOption;
import com.ericsson.fdp.business.enums.TariffEnquiryConditionProvider;
import com.ericsson.fdp.business.enums.TariffEnquiryOperationType;
import com.ericsson.fdp.business.operation.Operation;
import com.ericsson.fdp.business.operation.impl.DivOperation;
import com.ericsson.fdp.business.operation.impl.ModOperation;
import com.ericsson.fdp.business.operation.impl.MultOperation;
import com.ericsson.fdp.business.operation.impl.NoOperation;
import com.ericsson.fdp.business.vo.FDPTariffUnitVO;
import com.ericsson.fdp.common.constants.FDPConstant;
import com.ericsson.fdp.common.enums.ParameterFeedType;
import com.ericsson.fdp.dao.dto.tariffenquiry.FDPDisplayFormatDTO;
import com.ericsson.fdp.dao.enums.CommandParameterDataType;
import com.ericsson.fdp.dao.enums.CommandParameterType;

/**
 * This is Util class for Tariff Enquiry Display Format Cache.
 * 
 * @author Ericsson
 * 
 */
public class TariffEnquiryDisplayFormatUtil {

	/**
	 * This method puts cache data into a temporary map per Circle. <CIRCLE_ID,
	 * <DISPLAY_UNIT_VALUE, <CONVERSION_OPTION,CONVERTOR> > >
	 * 
	 * @param displayformat
	 * @param conversionOption
	 * @param cacheMap
	 */
	public static void createUpdateCacheMap(final FDPDisplayFormatDTO displayformat,
			final ConversionOption conversionOption,
			final Map<String, Map<String, Map<ConversionOption, Convertor>>> cacheMap) {
		String circleId = displayformat.getFdpCircle().getCircleId().toString();
		Map<String, Map<ConversionOption, Convertor>> circleCacheMap = cacheMap.get(circleId);
		String key = displayformat.getFdpUnitType().getUnitValue().toString();
		ConversionRules conversionRules = prepareConversionRules(displayformat);
		if (null != key && conversionRules != null && circleId != null) {
			if (null == circleCacheMap) {
				perCircleMap(conversionOption, cacheMap, circleId, key, conversionRules);
			} else {
				Map<ConversionOption, Convertor> cacheValue = circleCacheMap.get(key);
				if (null == cacheValue) {
					perUnitDisplayValueMap(conversionOption, circleCacheMap, key, conversionRules);
				} else {
					addConversionOptionMap(conversionOption, conversionRules, cacheValue);
				}
			}
		}
	}

	/**
	 * This method will add ConversionList against ConversionOption.
	 * 
	 * @param conversionOption
	 * @param conversionRules
	 * @param cacheValue
	 */
	private static void addConversionOptionMap(final ConversionOption conversionOption,
			ConversionRules conversionRules, Map<ConversionOption, Convertor> cacheValue) {
		ConverterImpl convertor = (ConverterImpl) cacheValue.get(conversionOption);
		if (null == convertor) {
			List<ConversionRules> conversionList = new LinkedList<ConversionRules>();
			conversionList.add(conversionRules);
			convertor = new ConverterImpl(conversionList);
			cacheValue.put(conversionOption, convertor);
		} else {
			List<ConversionRules> conversionList = convertor.getConversionRules();
			if (null != conversionList) {
				conversionList.add(conversionRules);
			}
		}
	}

	/**
	 * This method will add UnitDisplayValue Map for each Circle.
	 * 
	 * @param conversionOption
	 * @param circleCacheMap
	 * @param key
	 * @param conversionRules
	 */
	private static void perUnitDisplayValueMap(final ConversionOption conversionOption,
			Map<String, Map<ConversionOption, Convertor>> circleCacheMap, String key, ConversionRules conversionRules) {
		List<ConversionRules> conversionList = new LinkedList<ConversionRules>();
		conversionList.add(conversionRules);
		Convertor convertor = new ConverterImpl(conversionList);
		Map<ConversionOption, Convertor> conversionMap = new HashMap<ConversionOption, Convertor>();
		conversionMap.put(conversionOption, convertor);
		circleCacheMap.put(key, conversionMap);
	}

	/**
	 * This method will add new map entry per Circle in cacheMap.
	 * 
	 * @param conversionOption
	 * @param cacheMap
	 * @param circleId
	 * @param key
	 * @param conversionRules
	 */
	private static void perCircleMap(final ConversionOption conversionOption,
			final Map<String, Map<String, Map<ConversionOption, Convertor>>> cacheMap, String circleId, String key,
			ConversionRules conversionRules) {
		List<ConversionRules> conversionList = new LinkedList<ConversionRules>();
		conversionList.add(conversionRules);
		Convertor convertor = new ConverterImpl(conversionList);
		Map<ConversionOption, Convertor> conversionMap = new HashMap<ConversionOption, Convertor>();
		conversionMap.put(conversionOption, convertor);
		Map<String, Map<ConversionOption, Convertor>> circleCache = new HashMap<String, Map<ConversionOption, Convertor>>();
		circleCache.put(key, conversionMap);
		cacheMap.put(circleId, circleCache);
	}

	/**
	 * This method will prepare ConversionRules.
	 * 
	 * @param fdpDisplayFormatDTO
	 * @return
	 */
	private static ConversionRules prepareConversionRules(final FDPDisplayFormatDTO fdpDisplayFormatDTO) {
		Condition condition = prepareCondition(fdpDisplayFormatDTO);
		Map<String, UnitDisplayFormat> unitDisplayFormatMap = formatParser(fdpDisplayFormatDTO.getFormat());
		ConversionRules conversionRule = null;
		if (null != condition && null != unitDisplayFormatMap && unitDisplayFormatMap.size() > 0) {
			String text = prepareText(fdpDisplayFormatDTO.getFormat());
			if ( text != null) {
				FDPTariffUnitVO fdpTariffUnitVO = new FDPTariffUnitVO(unitDisplayFormatMap, text, text);
				conversionRule = new ConversionRulesImpl(condition, fdpTariffUnitVO);
			}
		}
		return conversionRule;
	}

	/**
	 * This method will prepare text.
	 * 
	 * @param format
	 * @return
	 */
	public static String prepareText(String format) {
		String tempFormat = format;
		boolean isContainsMoreOperation = true;
		String text = null;
		int i = 1;

		if (format == null || !format.contains("@")) {
			isContainsMoreOperation = false;
		} else {
			text = "${a1}";
		}

		while (isContainsMoreOperation) {
			tempFormat = tempFormat.substring(1, tempFormat.length());
			int fromIndex = tempFormat.indexOf("]");
			int toIndex = tempFormat.indexOf("@");
			if (toIndex > 1) {
				String formatSeperator = tempFormat.substring(fromIndex + 1, toIndex);
				i++;
				String key = "${a" + i + "}";
				text = text + formatSeperator + key;
				tempFormat = tempFormat.substring(toIndex + 1, tempFormat.length());
			}
			if (!tempFormat.contains("@")) {
				isContainsMoreOperation = false;
			}
		}
		return text;
	}

	/**
	 * This method will return condition as per below configuration. 1=>
	 * No-Operation Condition 2=> Less-Than Condition 3=> Greater-Than Condition
	 * 4=> Equal-To Condition 5=> Greater-Than and Less-Than Condition
	 * 
	 * @param fdpDisplayFormatDTO
	 * @return
	 */
	private static Condition prepareCondition(final FDPDisplayFormatDTO fdpDisplayFormatDTO) {
		Condition condition = null;
		TariffEnquiryConditionProvider conditionType = TariffEnquiryConditionProvider
				.getTariffEnquiryConditionType(fdpDisplayFormatDTO.getConditionType());
		CommandParamInput commandParamInput = null;
		switch (conditionType) {
		case NOP:
			NoCondition noCondition = new NoCondition();
			noCondition.setCommandParameterDataType(CommandParameterDataType.LONG);
			condition = noCondition;
			break;
		case LT:
			commandParamInput = new CommandParamInput(ParameterFeedType.INPUT, Long.parseLong(fdpDisplayFormatDTO
					.getOperand()));
			commandParamInput.setType(CommandParameterType.PARAM_IDENTIFIER);
			LessThanCondition lessThanCondition = new LessThanCondition(commandParamInput);
			lessThanCondition.setCommandParameterDataType(CommandParameterDataType.LONG);
			condition = lessThanCondition;
			break;
		case GT:
			commandParamInput = new CommandParamInput(ParameterFeedType.INPUT, Long.parseLong(fdpDisplayFormatDTO
					.getOperand()));
			commandParamInput.setType(CommandParameterType.PARAM_IDENTIFIER);
			GreaterThanCondition greaterThanCondition = new GreaterThanCondition(commandParamInput);
			greaterThanCondition.setCommandParameterDataType(CommandParameterDataType.LONG);
			condition = greaterThanCondition;
			break;
		case EQ:
			commandParamInput = new CommandParamInput(ParameterFeedType.INPUT, Long.parseLong(fdpDisplayFormatDTO
					.getOperand()));
			commandParamInput.setType(CommandParameterType.PARAM_IDENTIFIER);
			EqualsCondition equalCondition = new EqualsCondition(commandParamInput);
			equalCondition.setCommandParameterDataType(CommandParameterDataType.LONG);
			condition = equalCondition;
			break;
		case GTLT:
			String[] range = fdpDisplayFormatDTO.getOperand().split(FDPConstant.COMMA);
			CommandParamInput commandParamInputFrom = new CommandParamInput(ParameterFeedType.INPUT,
					Long.parseLong(range[0].trim()));
			commandParamInputFrom.setType(CommandParameterType.PARAM_IDENTIFIER);
			CommandParamInput commandParamInputTo = new CommandParamInput(ParameterFeedType.INPUT,
					Long.parseLong(range[1].trim()));
			commandParamInputTo.setType(CommandParameterType.PARAM_IDENTIFIER);
			RangeCondition rangeCondition = new RangeCondition(commandParamInputFrom, commandParamInputTo);
			rangeCondition.setCommandParameterDataType(CommandParameterDataType.LONG);
			condition = rangeCondition;
			break;
		}
		return condition;
	}

	/**
	 * This method creates map of UnitDisplayFormat against operation key to be
	 * put in FDPTariffUnitVO.
	 * 
	 * @param format
	 * @return
	 */
	private static Map<String, UnitDisplayFormat> formatParser(final String format) {
		String tempFormat = format;
		String mapKey = null;
		int i = 1;
		boolean isContainsMoreOperation = true;

		if (format == null || !format.contains("@")) {
			isContainsMoreOperation = false;
		}

		Map<String, UnitDisplayFormat> unitDisplayFormatMap = new HashMap<String, UnitDisplayFormat>();
		while (isContainsMoreOperation) {
			int tokenIndex = tempFormat.indexOf("@");
			int startIndex = tempFormat.indexOf("[");
			int lastIndex = tempFormat.indexOf("]");
			String token = tempFormat.substring(tokenIndex + 1, tokenIndex + 2);
			String singleOperation = tempFormat.substring(startIndex + 1, lastIndex);
			UnitDisplayFormat unitDisplayFormat = prepareOpertionList(token, singleOperation);
			mapKey = "a"+String.valueOf(i);
			unitDisplayFormatMap.put(mapKey, unitDisplayFormat);
			i++;
			tempFormat = tempFormat.substring(lastIndex + 1, tempFormat.length());
			if (!tempFormat.contains("@")) {
				isContainsMoreOperation = false;
			}
		}
		return unitDisplayFormatMap;
	}

	/**
	 * This method UnitDisplayFormat Object per operation to be put in
	 * FDPTariffUnitVO against operation Key.
	 * 
	 * @param tokenName
	 * @param singleOperation
	 * @return
	 */
	private static UnitDisplayFormat prepareOpertionList(final String tokenName, final String singleOperation) {
		List<Operation> operationsList = new LinkedList<Operation>();
		String tempSingleOperation = singleOperation;
		int maxPrecision = 0;
		int precision = 0;
		String value = null;
		String[] operations = tempSingleOperation.split(FDPConstant.COMMA);
		if (null != operations) {
			for (String ops : operations) {
				ops = ops.trim();
				String[] values = ops.split(FDPConstant.SPACE);
				precision = 0;
				value = null;
				if (null != values && values.length > 1) {
					value = values[1].trim();
					if (values.length == 3) {
						precision = Integer.parseInt(values[2]);
						if (maxPrecision < precision) {
							maxPrecision = precision;
						}
					}
				}
				Operation operation = prepareOperation(values[0].trim(), value, precision);
				if (null != operation) {
					operationsList.add(operation);
				}
			}
		}
		UnitDisplayFormat unitDisplayFormatImpl = new UnitDisplayFormatImpl(maxPrecision, operationsList, tokenName);
		return unitDisplayFormatImpl;
	}

	/**
	 * This method will prepare Operation from FORMAT after parsing.
	 * 
	 * @param operationType
	 * @param value
	 * @param precision
	 * @return
	 */
	private static Operation prepareOperation(final String operationType, final String value, final int precision) {
		Operation operation = null;
		TariffEnquiryOperationType operationTyp = TariffEnquiryOperationType
				.getTariffEnquiryOperationType(operationType);
		switch (operationTyp) {
		case NOP:
			operation = new NoOperation();
			break;
		case DIV:
			operation = new DivOperation(Double.parseDouble(value), precision);
			break;
		case MOD:
			operation = new ModOperation(Long.parseLong(value));
			break;
		case MUL:
			operation = new MultOperation(Double.parseDouble(value), precision);
			break;
		}
		return operation;
	}
}
