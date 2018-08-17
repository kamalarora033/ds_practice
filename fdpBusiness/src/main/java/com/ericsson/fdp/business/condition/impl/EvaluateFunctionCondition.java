package com.ericsson.fdp.business.condition.impl;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.regex.Pattern;

import javax.annotation.Resource;

import com.ericsson.fdp.FDPCacheable;
import com.ericsson.fdp.business.cache.MetaDataCache;
import com.ericsson.fdp.business.command.param.impl.CommandParamInput;
import com.ericsson.fdp.business.condition.AbstractCondition;
import com.ericsson.fdp.business.enums.Function;
import com.ericsson.fdp.business.util.ClassUtil;
import com.ericsson.fdp.business.vo.Handset4GDetailVO;
import com.ericsson.fdp.common.enums.ModuleType;
import com.ericsson.fdp.common.exception.ConditionFailedException;
import com.ericsson.fdp.core.dsm.framework.service.FDPCache;
import com.ericsson.fdp.core.exception.EvaluationFailedException;
import com.ericsson.fdp.core.request.FDPRequest;
import com.ericsson.fdp.core.serviceprovisioning.bean.FDPMetaBag;
import com.ericsson.fdp.dao.enums.FDPCommandParameterConditionEnum;

/**
 * This class is used to implement the function based evaluation of command
 * parameters.
 * 
 * @author Ericsson
 * 
 */
public class EvaluateFunctionCondition extends AbstractCondition {

	/**
	 * The serial version id.
	 */
	private static final long serialVersionUID = 6630863916669627202L;
	/** The list of possible values for the right hand operator. */
	private CommandParamInput paramFunctionInput;

	/**
	 * The meta data cache.
	 */
	@Resource(lookup = "java:app/fdpBusiness-1.0/MetaDataCache")
	private static FDPCache<FDPMetaBag, FDPCacheable> fdpMetaDataCache;

	/**
	 * This Constructor will evaluate the function value based on
	 * CommandParameterConditionEnum
	 * 
	 * @param paramFunctionInput
	 * @param conditionEnum
	 */

	public EvaluateFunctionCondition(final CommandParamInput paramFunctionInput,
			FDPCommandParameterConditionEnum conditionEnum) {
		super();
		if (paramFunctionInput == null) {
			throw new IllegalArgumentException("Found 'null' or invalid type argument for Condition");
		}

		super.setConditionType(conditionEnum);
		this.paramFunctionInput = paramFunctionInput;
	}

	@Override
	public String toString() {
		return " paramFunctionInput '" + paramFunctionInput.toString(true) + "'";
	}

	@Override
	public boolean evaluateConditionValueForProvidedInput(Object leftOperandValue, Object[] rightOperandValues)
			throws ConditionFailedException {
		if (rightOperandValues == null || rightOperandValues.length == 0) {
			throw new ConditionFailedException("Operand value is null/ empty and cannot be processed.");
		}
		try {
			Object leftOperandToCheck;
			
			Class<?> classToConvert = getCommandParameterDataType().getClazz();
			leftOperandToCheck = ClassUtil.getPrimitiveValueReturnNotNullObject(leftOperandValue,
					classToConvert);
			
			Object rightOperandToCheck = rightOperandValues[0];
			rightOperandToCheck = ClassUtil.getPrimitiveValueReturnNotNullObject(rightOperandToCheck, classToConvert);
			// Evaluate the Rule
			if (rightOperandToCheck instanceof String && leftOperandToCheck instanceof String) {
				String secondOperand = (String) leftOperandToCheck;
				if (secondOperand.length() == 0) {
					throw new ConditionFailedException("Operand value is null/ empty and cannot be processed.");
				}
				if (rightOperandToCheck.toString().equalsIgnoreCase(secondOperand)) {
					return true;
				}
				return false;
			} else if (rightOperandToCheck instanceof Comparable && leftOperandToCheck instanceof Comparable) {
				if (((Comparable) rightOperandToCheck).compareTo(leftOperandToCheck) == 0) {
					return true;
				}
				return false;
			} else if (rightOperandValues[0] instanceof List<?> && leftOperandValue instanceof List<?>) {
				List<?> possibleValuesToUse = (List<?>) rightOperandValues[0];
				List<?> leftOperandsList = (List<?>) leftOperandValue;
				for (Object object : possibleValuesToUse) {
					for (Object left : leftOperandsList) {
						if (object.toString().equals(left.toString())) {
							return true;
						}
					}
				}
				return false;
			} else if (rightOperandValues[0] instanceof List<?>) {
				List<?> possibleValuesToUse = (List<?>) rightOperandValues[0];
				for (Object object : possibleValuesToUse) {
					if (object.toString().equalsIgnoreCase(leftOperandValue.toString())) {
						return true;
					}
				}
				return false;
			} else if (leftOperandToCheck instanceof List<?>) {
				List<?> leftOperandsList = (List<?>) leftOperandValue;
				for (Object left : leftOperandsList) {
					left = ClassUtil.getPrimitiveValueReturnNotNullObject(left, classToConvert);
					if (rightOperandToCheck.toString().equals(left.toString())) {
						return true;
					}
				}
				return true;
			} else {
				if (rightOperandToCheck.equals(leftOperandToCheck)) {
					return true;
				}
				return false;
			}
		} catch (EvaluationFailedException e) {
			throw new ConditionFailedException("The specified operands cannot be compared as they are not valid", e);
		}
	}

	@Override
	protected Object[] evaluateRightOperands(FDPRequest fdpRequest) throws ConditionFailedException {
		try {
			this.paramFunctionInput.evaluateValue(fdpRequest);
		} catch (EvaluationFailedException e) {
			throw new ConditionFailedException("Could not evaluate value ", e);
		}
		Object[] rightOperands = evaluateValue(fdpRequest, this.paramFunctionInput.getValue());
		return rightOperands;
	}

	private Object[] evaluateValue(FDPRequest fdpRequest, Object value) {
		Object list = null;
		if (value != null) {
			switch (Function.getFunction((String) value)) {
			case MATCH_HANDSET_OFFERS:
				list = findHandsetOffersFromCache();
				break;
			case CHECK_4G_HANDSET:
				list = findHandsetListFromCache();
				break;
			case CHECK_4G_HANDSET_MODEL:
				list = findHandsetBrandListFromCache();
				break;
			case CHECK_FREQUENCY_SUPPORT:
				list = findFrequencySupprotListFromCache();
				break;
			default:
				list = value;
			}
		}
		Object[] rightOperands = { list };
		return rightOperands;
	}

	/**
	 * This method is to provide an overview on how to use this functionality.
	 * It computes the right operand values through functions in eval function
	 * dropdown in Product Constraint GUI
	 * 
	 * @param fdpRequest
	 * @return
	 */
	
	
	
	private List<Integer> findHandsetOffersFromCache() {
		Integer[] in = { 12, 23, 45 };
		return Arrays.asList(in);
	}

	private List<String> findHandsetListFromCache() {
		final Map<Object, Object> handsetMap = MetaDataCache.getAllValue(ModuleType.HANDSET_4G);
		List<String> handsetList = new ArrayList<>();

		for (Entry<Object, Object> entry : handsetMap.entrySet()) {
			Handset4GDetailVO handset4gDetail = (Handset4GDetailVO) entry.getValue();
			handsetList.add(splitHandsetBrand(handset4gDetail.getfDP4GHandsetDTO().getHandsetName()));
		}
		return handsetList;
	}

	private List<String> findHandsetBrandListFromCache() {
		final Map<Object, Object> handsetMap = MetaDataCache.getAllValue(ModuleType.HANDSET_4G);
		List<String> handsetList = new ArrayList<>();

		for (Entry<Object, Object> entry : handsetMap.entrySet()) {
			Handset4GDetailVO handset4gDetail = (Handset4GDetailVO) entry.getValue();
			handsetList.add(splitHandsetModel(handset4gDetail.getfDP4GHandsetDTO().getHandsetName()));
		}
		return handsetList;
	}

	private String splitHandsetBrand(final String handsetName) {
		if (handsetName.contains("$"))
			return handsetName.substring(0, handsetName.indexOf('$'));
		return handsetName;
	}

	private String splitHandsetModel(final String handsetName) {
		if (handsetName.contains("$")) {
			final int startindex = handsetName.indexOf('$') + 1;
			return handsetName.substring(startindex, handsetName.length());
		}
		return handsetName;
	}
	
	private List<String> findFrequencySupprotListFromCache(){
		final Map<Object, Object> handsetMap = MetaDataCache.getAllValue(ModuleType.HANDSET_4G);
		List<String> handsetList = new ArrayList<>();

		for (Entry<Object, Object> entry : handsetMap.entrySet()) {
			Handset4GDetailVO handset4gDetail = (Handset4GDetailVO) entry.getValue();
			handsetList.add(handset4gDetail.getfDP4GHandsetDTO().getHandsetName());
		}
		return handsetList;
	}

}
