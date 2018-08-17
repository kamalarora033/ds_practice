package com.ericsson.fdp.business.convertor.impl;

import java.util.List;

import com.ericsson.fdp.business.convertor.Convertor;
import com.ericsson.fdp.business.convertor.conversion.ConversionRules;
import com.ericsson.fdp.core.exception.ExecutionFailedException;
import com.ericsson.fdp.core.request.FDPRequest;

public class ConverterImpl implements Convertor {

	/**
	 * 
	 */
	private static final long serialVersionUID = 9201943873977256459L;

	private List<ConversionRules> conversionRules;

	public ConverterImpl(List<ConversionRules> conversionRules) {
		this.conversionRules = conversionRules;
	}

	@Override
	public String execute(String input, Object... otherParams) throws ExecutionFailedException {

		String value = null;
		FDPRequest fdpRequest = null;
		if (otherParams != null && otherParams.length > 0) {
			fdpRequest = (FDPRequest) otherParams[0];
		}
		for (ConversionRules conversionRule : this.conversionRules) {
			if (conversionRule.evaluateConversion(input, fdpRequest)) {
				value = conversionRule.execute(input, fdpRequest);
			}
		}
		return value;
	}

	public List<ConversionRules> getConversionRules() {
		return conversionRules;
	}

	public void setConversionRules(List<ConversionRules> conversionRules) {
		this.conversionRules = conversionRules;
	}

}
