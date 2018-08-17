package com.ericsson.fdp.business.service;

import javax.ejb.Local;

import com.ericsson.fdp.dao.dto.ExchangeMessageResponse;

/**
 * The Interface DynamicMenuItegrator.
 * 
 * @author Ericsson
 */
@Local
public interface DynamicMenuItegrator {

	/**
	 * Send submit sm in out.
	 * 
	 * @param exchangeMessageResponse
	 *            the exchange message response
	 * @throws Exception
	 *             the exception
	 */
	public void sendSubmitSmInOut(final ExchangeMessageResponse exchangeMessageResponse) throws Exception;
}
