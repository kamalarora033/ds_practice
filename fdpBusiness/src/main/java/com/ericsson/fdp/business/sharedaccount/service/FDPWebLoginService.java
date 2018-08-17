package com.ericsson.fdp.business.sharedaccount.service;

import javax.ejb.Remote;

import com.ericsson.fdp.common.exception.FDPServiceException;
import com.ericsson.fdp.dao.dto.FDPOtpDTO;
import com.ericsson.fdp.dao.exception.FDPDataNotFoundException;

/**
 * The Interface FDPWebLoginService.
 */
@Remote
public interface FDPWebLoginService {

	/**
	 * Generate otp.
	 * 
	 * @param msisdnNumber
	 *            the msisdn number
	 * @return the string
	 */
	String generateOTP(final Long msisdnNumber);

	/**
	 * Gets the oTP login details.
	 * 
	 * @param msisdnNumber
	 *            the msisdn number
	 * @return the oTP login details
	 * @throws FDPDataNotFoundException
	 */
	FDPOtpDTO getOTPLoginDetails(final Long msisdnNumber) throws FDPDataNotFoundException;

	/**
	 * Sets the oTP expired.
	 * 
	 * @param userMsisdn
	 *            the new oTP expired
	 * @throws FDPServiceException
	 */
	void setOTPExpired(Long userMsisdn) throws FDPServiceException;
}
