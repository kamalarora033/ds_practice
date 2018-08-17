package com.ericsson.fdp.business.tariffenquiry.command.impl;

import java.util.Map;

import com.ericsson.fdp.FDPCacheable;
import com.ericsson.fdp.business.convertor.Convertor;
import com.ericsson.fdp.business.enums.ConversionOption;

public class TariffEnquiryDisplayFormatImpl implements FDPCacheable{

	/**
	 *  The serialVersionUID for class.
	 */
	private static final long serialVersionUID = 2745563665774515508L;
	
	Map<ConversionOption,Convertor> displayFormat = null;
	
	public TariffEnquiryDisplayFormatImpl(Map<ConversionOption,Convertor> displayFormat) {
		this.displayFormat = displayFormat;
	}

	/**
	 * @return the displayFormat
	 */
	public Map<ConversionOption, Convertor> getDisplayFormat() {
		return displayFormat;
	}

	/**
	 * @param displayFormat the displayFormat to set
	 */
	public void setDisplayFormat(Map<ConversionOption, Convertor> displayFormat) {
		this.displayFormat = displayFormat;
	}
}
