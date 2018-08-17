/**
 * 
 */
package com.ericsson.fdp.business.dto.pam;

import java.io.Serializable;

/**
 * The Class PAMFile.
 * 
 * @author Ericsson
 */
public class PAMRecord implements Serializable {

	/** The Constant serialVersionUID. */
	private static final long serialVersionUID = -3363078102933567117L;

	/** The msisdn. */
	private String msisdn;

	/** The offer id. */
	private String offerId;

	/** The expiry date. */
	private String expiryDate;
	

	/**
	 * Instantiates a new pAM file.
	 */
	public PAMRecord() {
		
	}

	/**
	 * Instantiates a new pAM file.
	 *
	 * @param msisdn the msisdn
	 * @param offerId the offer id
	 * @param expiryDate the expiry date
	 */
	public PAMRecord(final String msisdn, final String offerId, final String expiryDate) {
		super();
		this.msisdn = msisdn;
		this.offerId = offerId;
		this.expiryDate = expiryDate;
	}

	/**
	 * Gets the msisdn.
	 *
	 * @return the msisdn
	 */
	public String getMsisdn() {
		return msisdn;
	}

	/**
	 * Sets the msisdn.
	 *
	 * @param msisdn the new msisdn
	 */
	public void setMsisdn(final String msisdn) {
		this.msisdn = msisdn;
	}

	/**
	 * Gets the offer id.
	 * 
	 * @return the offerId
	 */
	public String getOfferId() {
		return offerId;
	}

	/**
	 * Sets the offer id.
	 * 
	 * @param offerId
	 *            the offerId to set
	 */
	public void setOfferId(final String offerId) {
		this.offerId = offerId;
	}

	/**
	 * Gets the expiry date.
	 * 
	 * @return the expiryDate
	 */
	public String getExpiryDate() {
		return expiryDate;
	}

	/**
	 * Sets the expiry date.
	 * 
	 * @param expiryDate
	 *            the expiryDate to set
	 */
	public void setExpiryDate(final String expiryDate) {
		this.expiryDate = expiryDate;
	}

	@Override
	public String toString() {
		return msisdn + "," + offerId+"," + expiryDate;
	}
	
	
}
