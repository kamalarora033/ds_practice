package com.ericsson.fdp.business.convertor.displayConvertor.impl;

import java.util.List;
import java.util.Map;

import com.ericsson.fdp.business.convertor.displayConvertor.UnitDisplayFormat;
import com.ericsson.fdp.business.enums.DisplayType;
import com.ericsson.fdp.business.exception.NotificationFailedException;
import com.ericsson.fdp.business.operation.Operation;
import com.ericsson.fdp.business.util.DisplayTokenUtil;
import com.ericsson.fdp.business.util.OperationUtil;
import com.ericsson.fdp.business.vo.FDPDisplayTokenVO;
import com.ericsson.fdp.core.cache.AppCacheSubStore;
import com.ericsson.fdp.core.exception.EvaluationFailedException;
import com.ericsson.fdp.core.exception.ExecutionFailedException;
import com.ericsson.fdp.core.request.FDPRequest;
import com.ericsson.fdp.core.serviceprovisioning.bean.FDPAppBag;
import com.ericsson.fdp.core.utils.ApplicationConfigUtil;

public class UnitDisplayFormatImpl implements UnitDisplayFormat {

	/**
	 * The Class SerialVersionUID
	 */
	private static final long serialVersionUID = 4401367214351472484L;
	
	/**
	 * The Precision.
	 */
	private int precision;

	/**
	 * The Operation List.
	 */
	private List<Operation> operations;

	private String tokenName;

	public UnitDisplayFormatImpl(int precision, List<Operation> operations, String tokenName) {
		this.precision = precision;
		this.operations = operations;
		this.tokenName = tokenName;
	}

	/**
	 * @return the precision
	 */
	public int getPrecision() {
		return precision;
	}

	/**
	 * @param precision
	 *            the precision to set
	 */
	public void setPrecision(int precision) {
		this.precision = precision;
	}

	/**
	 * @return the operations
	 */
	public List<Operation> getOperations() {
		return operations;
	}

	/**
	 * @param operations
	 *            the operations to set
	 */
	public void setOperations(List<Operation> operations) {
		this.operations = operations;
	}

	/**
	 * @return the tokenName
	 */
	public String getTokenName() {
		return tokenName;
	}

	/**
	 * @param tokenName
	 *            the tokenName to set
	 */
	public void setTokenName(String tokenName) {
		this.tokenName = tokenName;
	}

	@Override
	public String evaluateValue(String input, FDPRequest fdpRequest, Object... otherParams)
			throws EvaluationFailedException {
		Long value = Long.valueOf(input);
		double doublVal = Double.valueOf(value);
		for (Operation operation : this.operations) {
			doublVal = operation.evaluate(doublVal);
		}

		return getNotificationValue(doublVal, fdpRequest);
	}

	@SuppressWarnings("unchecked")
	private String getNotificationValue(double doublVal, FDPRequest fdpRequest) throws EvaluationFailedException {
		String value = getPrecisionValue(doublVal).toString();
		try {
			Map<DisplayType, FDPDisplayTokenVO> displayMap = (Map<DisplayType, FDPDisplayTokenVO>) ApplicationConfigUtil
					.getApplicationConfigCache().getValue(
							new FDPAppBag(AppCacheSubStore.UNIT_CONVERSION_FORMATS, tokenName));
			if (displayMap == null) {
				throw new EvaluationFailedException("Conversion cannot be done for " + tokenName);
			}
			FDPDisplayTokenVO notificationVO = displayMap.get(DisplayType.PLUARAL_DISPLAY);
			// TODO: remove hardcoding.
			if (doublVal == 1.0) {
				notificationVO = displayMap.get(DisplayType.SINGULAR_DISPLAY);
			}
			return DisplayTokenUtil.createNotificationText(value, notificationVO, fdpRequest);
		} catch (ExecutionFailedException e) {
			throw new EvaluationFailedException("Could not execute unit conversion", e);
		} catch (NotificationFailedException e) {
			throw new EvaluationFailedException("Could not execute unit conversion", e);
		}
	}

	private Object getPrecisionValue(double doublVal) {
		Object value = (int) doublVal;
		if (precision > 0) {
			value = OperationUtil.convertToPrecision(doublVal, precision);
		}
		return value;
	}
}
