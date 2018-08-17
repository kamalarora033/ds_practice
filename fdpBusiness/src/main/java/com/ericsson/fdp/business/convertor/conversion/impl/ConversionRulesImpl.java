package com.ericsson.fdp.business.convertor.conversion.impl;

import com.ericsson.fdp.business.condition.Condition;
import com.ericsson.fdp.business.convertor.conversion.ConversionRules;
import com.ericsson.fdp.business.exception.NotificationFailedException;
import com.ericsson.fdp.business.util.TariffEnquiryUnitNotificationUtil;
import com.ericsson.fdp.business.vo.FDPTariffUnitVO;
import com.ericsson.fdp.common.exception.ConditionFailedException;
import com.ericsson.fdp.core.exception.ExecutionFailedException;
import com.ericsson.fdp.core.request.FDPRequest;

public class ConversionRulesImpl implements ConversionRules {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1921072036257165521L;

	/**
	 * The Condition.
	 */
	private Condition condition;

	private FDPTariffUnitVO fdpTariffUnitVO;

	public ConversionRulesImpl(Condition condition, FDPTariffUnitVO fdpTariffUnitVO) {
		this.condition = condition;
		this.fdpTariffUnitVO = fdpTariffUnitVO;
	}

	@Override
	public String execute(String input, FDPRequest fdpRequest) throws ExecutionFailedException {
		try {
			return TariffEnquiryUnitNotificationUtil.createNotificationText(input, fdpTariffUnitVO, fdpRequest);
		} catch (NotificationFailedException e) {
			throw new ExecutionFailedException("Could not create text.");
		}
	}

	@Override
	public boolean evaluateConversion(String input, FDPRequest fdpRequest) throws ExecutionFailedException {
		try {
			return condition.evaluate(input, fdpRequest, false);
		} catch (ConditionFailedException e) {
			throw new ExecutionFailedException("Could not evaluate condition", e);
		}
	}

}
