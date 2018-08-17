package com.ericsson.fdp.business.tariffenquiry.command.impl;

import com.ericsson.fdp.FDPCacheable;

public class TariffEnquiryAttributesImpl implements FDPCacheable{
	
	/**
	 * The Class Serial Version UID
	 */
	private static final long serialVersionUID = -4653965969018586098L;

	/**
	 * The Value.
	 */
	private Object value = null;
	
	public TariffEnquiryAttributesImpl(final Object value) {
		this.value = value;
	}

	/**
	 * @return the value
	 */
	public Object getValue() {
		return value;
	}

}