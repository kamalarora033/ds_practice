package com.ericsson.fdp.business.attributesfilter.impl;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import ch.qos.logback.classic.Logger;

import com.ericsson.fdp.business.attributesfilter.AttributeFilter;
import com.ericsson.fdp.business.enums.AuxRequestParam;
import com.ericsson.fdp.business.exception.ExpressionFailedException;
import com.ericsson.fdp.business.expression.Expression;
import com.ericsson.fdp.business.tariffenquiry.configimport.constants.TariffConstants;
import com.ericsson.fdp.business.util.LoggerUtil;
import com.ericsson.fdp.business.util.RequestUtil;
import com.ericsson.fdp.business.vo.FDPTariffAttributesExpression;
import com.ericsson.fdp.common.enums.ModuleType;
import com.ericsson.fdp.common.enums.TariffEnquiryOption;
import com.ericsson.fdp.common.enums.TariffEnquiryOptionValues;
import com.ericsson.fdp.core.exception.ExecutionFailedException;
import com.ericsson.fdp.core.logging.FDPLogger;
import com.ericsson.fdp.core.request.FDPRequest;
import com.ericsson.fdp.core.serviceprovisioning.bean.FDPMetaBag;
import com.ericsson.fdp.core.utils.ApplicationConfigUtil;

public class AttributeFilterImpl implements AttributeFilter {

	@Override
	@SuppressWarnings("unchecked")
	public List<String> filter(final List<String> tariffEnquiryValuesForUser,
			final TariffEnquiryOption tariffEnquiryOption, final FDPRequest fdpRequest) throws ExecutionFailedException {
		List<String> filteredTariffOptions = null;
		try {
			final Logger circleLogger = LoggerUtil.getSummaryLoggerFromRequest(fdpRequest);
			Expression expression = expressionFromCache(fdpRequest, tariffEnquiryOption, circleLogger);
			Map<TariffEnquiryOption, Map<String, String[]>> tariffValuesMap = (Map<TariffEnquiryOption, Map<String, String[]>>) fdpRequest
					.getAuxiliaryRequestParameter(AuxRequestParam.TEMP_TARIFF_ENQUIRY_VALUES);
			if ((null != expression) && (null != tariffValuesMap)) {
				FDPLogger.debug(circleLogger, getClass(), "filter()", "Got Expression from Cache:" + expression
						+ " ,for requestId:" + fdpRequest.getRequestId() + ", expression:" + expression);
				Map<String, String[]> otherValueMap = tariffValuesMap.get(tariffEnquiryOption);
				if (null != otherValueMap) {
					filteredTariffOptions = new ArrayList<String>();
					for (final String intersectedValue : tariffEnquiryValuesForUser) {
						FDPLogger.debug(circleLogger, getClass(), "filter()", "Evaluating expression for value:"
								+ intersectedValue + " , for requestId:" + fdpRequest.getRequestId());
						String otherValue[] = otherValueMap.get(intersectedValue);
						updateAUXValuesInRequestForFilterExecution(fdpRequest, tariffEnquiryOption, otherValue,
								intersectedValue, circleLogger);
						boolean isEvaluated = expression.evaluateExpression(fdpRequest);
						if (!isEvaluated) {
							FDPLogger.debug(circleLogger, getClass(), "filter()",
									"Adding to filteredList after sucessFull Expression Evalution:" + intersectedValue);
							filteredTariffOptions.add(intersectedValue);
						}
					}
				}
			}

			if (null == filteredTariffOptions) {
				FDPLogger.debug(circleLogger, getClass(), "filter()", "No Configuration Found.");
				filteredTariffOptions = tariffEnquiryValuesForUser;
			}
		} catch (ExpressionFailedException e) {
			throw new ExecutionFailedException("Expression Evaluation Failed due to error:" + e.getMessage(), e);
		}
		return filteredTariffOptions;
	}

	/**
	 * This method will fetch the Expression from Cache.
	 * 
	 * @param tariffEnquiryOption
	 * @return
	 * @throws ExecutionFailedException
	 */
	private Expression expressionFromCache(final FDPRequest fdpRequest, final TariffEnquiryOption tariffEnquiryOption,
			final Logger circleLogger) throws ExecutionFailedException {
		final String cachekey = tariffEnquiryOption.getName() + TariffConstants.TARIFF_ATTRIBUTES_EXPRESSION_KEY;
		FDPLogger.debug(circleLogger, getClass(), "expressionFromCache()", "Tariff-Expression Cache Key:" + cachekey
				+ ", for requestId:" + fdpRequest.getRequestId());
		FDPTariffAttributesExpression fdpTariffAttributesExpression = (FDPTariffAttributesExpression) ApplicationConfigUtil
				.getMetaDataCache()
				.getValue(
						new FDPMetaBag(fdpRequest.getCircle(), ModuleType.TARIFF_ENQUIRY_ATTRIBUTE_EXPRESSION, cachekey));
		Expression expression = null;
		if (null != fdpTariffAttributesExpression) {
			expression = fdpTariffAttributesExpression.getExpression();
		} else {
			FDPLogger.debug(circleLogger, getClass(), "expressionFromCache()", "No Cache Value Found for OptionType:"
					+ tariffEnquiryOption.getName() + ", for requestId:" + fdpRequest.getRequestId());
		}
		return expression;
	}

	/**
	 * This method will update the AUX values in request.
	 * 
	 * Intersectedvalue => TARIFF_NAME ParamValue => TARIFF_VALUE
	 * ParamExpiry/Valadity => TARIFF_VALIDITY ParamUnitType => TARIFF_UNIT
	 * 
	 * @param tariffEnquiryOption
	 * @param otherValue
	 * @throws ExecutionFailedException
	 */
	private void updateAUXValuesInRequestForFilterExecution(final FDPRequest fdpRequest,
			final TariffEnquiryOption tariffEnquiryOption, final String[] otherValue, final String intersectedValue,
			final Logger circleLogger) throws ExecutionFailedException {
		TariffEnquiryOptionValues tariffEnquiryOptionValues = TariffEnquiryOptionValues
				.getTariffEnquiryOptionValue(tariffEnquiryOption);
		if (null != tariffEnquiryOptionValues) {
			RequestUtil.putAuxiliaryValueInRequest(fdpRequest, AuxRequestParam.TARIFF_NAME, intersectedValue);
			FDPLogger.debug(circleLogger, getClass(), "updateAUXValuesInRequestForFilterExecution()",
					"Updating TARIFF_NAME=" + intersectedValue);
			if (null != tariffEnquiryOptionValues.getParameterValue()) {
				if (otherValue == null || otherValue.length < 1 || otherValue[0] == null) {
					throw new ExecutionFailedException("Other value[Param-Value] not found for TariffOption :"
							+ tariffEnquiryOption.getName());
				}
				RequestUtil.putAuxiliaryValueInRequest(fdpRequest, AuxRequestParam.TARIFF_VALUE, otherValue[0]);
				FDPLogger.debug(circleLogger, getClass(), "updateAUXValuesInRequestForFilterExecution()",
						"Updating TARIFF_VALUE=" + otherValue[0]);
			}
			if (null != tariffEnquiryOptionValues.getParameterExpiry()) {
				if (otherValue == null || otherValue.length < 2 || otherValue[1] == null) {
					throw new ExecutionFailedException("Other value[Param-Expiry] not found for TariffOption :"
							+ tariffEnquiryOption.getName());
				}
				RequestUtil.putAuxiliaryValueInRequest(fdpRequest, AuxRequestParam.TARIFF_VALIDITY, otherValue[1]);
				FDPLogger.debug(circleLogger, getClass(), "updateAUXValuesInRequestForFilterExecution()",
						"Updating TARIFF_VALIDITY=" + otherValue[1]);
			}
			if (null != tariffEnquiryOptionValues.getUnitType()) {
				if (otherValue == null || otherValue.length < 3 || otherValue[2] == null) {
					throw new ExecutionFailedException("Other value[Param-UnitType] not found for TariffOption :"
							+ tariffEnquiryOption.getName());
				}
				RequestUtil.putAuxiliaryValueInRequest(fdpRequest, AuxRequestParam.TARIFF_UNIT, otherValue[2]);
				FDPLogger.debug(circleLogger, getClass(), "updateAUXValuesInRequestForFilterExecution()",
						"Updating TARIFF_UNIT=" + otherValue[2]);
			}
		}
	}

}
