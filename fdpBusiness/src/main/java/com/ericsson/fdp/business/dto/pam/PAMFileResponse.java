/**
 * 
 */
package com.ericsson.fdp.business.dto.pam;

import java.io.Serializable;

/**
 * The Class PAMFileResponse.
 * 
 * @author Ericsson
 */
public class PAMFileResponse implements Serializable {

	/** The Constant serialVersionUID. */
	private static final long serialVersionUID = 5597972955280077123L;

	/** The pam file. */
	private PAMRecord pamFile;

	/** The get account details check. */
	private boolean getAccountDetailsCheck;

	/** The PAM status. */
	private String pamStatus;
	
	/**
	 * The failure description
	 */
	private String failureDescription;
	
	
	/**
	 * The Request ID.
	 */
	private String requestID;

	
	/**
	 * The response code from AIR.
	 */
	private Integer responseCode;
	
	/**
	 *  pam service id from config file 
	 */
	private String pamServiceId;

	

	/**
	 * Instantiates a new pAM file response.
	 */
	public PAMFileResponse() {

	}

	/**
	 * Instantiates a new pAM file response.
	 * 
	 * @param pamFile
	 *            the pam file
	 * @param getAccountDetailsCheck
	 *            the get account details check
	 * @param pamStatus
	 *            pamStatus
	 */
	public PAMFileResponse(final PAMRecord pamFile, final boolean getAccountDetailsCheck, final String pamStatus) {
		super();
		this.pamFile = pamFile;
		this.getAccountDetailsCheck = getAccountDetailsCheck;
		this.pamStatus = pamStatus;
	}
	
	/**
	 * Instantiates a new pAM file response.
	 * 
	 * @param pamFile
	 *            the pam file
	 * @param getAccountDetailsCheck
	 *            the get account details check
	 * @param pamStatus
	 *            pamStatus
	 */
	public PAMFileResponse(final PAMRecord pamFile, final boolean getAccountDetailsCheck, final String pamStatus,final String failureDescription) {
		super();
		this.pamFile = pamFile;
		this.getAccountDetailsCheck = getAccountDetailsCheck;
		this.pamStatus = pamStatus;
		this.failureDescription=failureDescription;
	}
	
	/**
	 * @param pamFile
	 * @param getAccountDetailsCheck
	 * @param pamStatus
	 * @param failureDescription
	 * @param serverIP
	 */
	public PAMFileResponse(final PAMRecord pamFile, final boolean getAccountDetailsCheck, final String pamStatus,final String failureDescription,final String requestID) {
		super();
		this.pamFile = pamFile;
		this.getAccountDetailsCheck = getAccountDetailsCheck;
		this.pamStatus = pamStatus;
		this.failureDescription=failureDescription;
		this.requestID=requestID;
	}
	
	
	/**
	 * @param pamFile
	 * @param getAccountDetailsCheck
	 * @param pamStatus
	 * @param failureDescription
	 * @param serverIP
	 * @param responseCode
	 */
	public PAMFileResponse(final PAMRecord pamFile, final boolean getAccountDetailsCheck, final String pamStatus,final String failureDescription, final String requestID, final Integer responseCode,final String pamServiceId) {
		super();
		this.pamFile = pamFile;
		this.getAccountDetailsCheck = getAccountDetailsCheck;
		this.pamStatus = pamStatus;
		this.failureDescription=failureDescription;
		this.requestID=requestID;
		this.responseCode=responseCode;
		this.pamServiceId=pamServiceId;
		
	}

	/**
	 * Gets the pam file.
	 * 
	 * @return the pamFile
	 */
	public PAMRecord getPamFile() {
		return pamFile;
	}

	/**
	 * Sets the pam file.
	 * 
	 * @param pamFile
	 *            the pamFile to set
	 */
	public void setPamFile(final PAMRecord pamFile) {
		this.pamFile = pamFile;
	}

	/**
	 * Checks if is gets the account details check.
	 * 
	 * @return the getAccountDetailsCheck
	 */
	public boolean isGetAccountDetailsCheck() {
		return getAccountDetailsCheck;
	}

	/**
	 * Sets the gets the account details check.
	 * 
	 * @param getAccountDetailsCheck
	 *            the getAccountDetailsCheck to set
	 */
	public void setGetAccountDetailsCheck(final boolean getAccountDetailsCheck) {
		this.getAccountDetailsCheck = getAccountDetailsCheck;
	}

	/**
	 * Gets the pAM status.
	 * 
	 * @return the pAMStatus
	 */
	public String getPAMStatus() {
		return pamStatus;
	}

	/**
	 * Sets the pAM status.
	 * 
	 * @param pAMStatus
	 *            the pAMStatus to set
	 */
	/**
	 * @param pAMStatus
	 */
	public void setPAMStatus(final String pAMStatus) {
		pamStatus = pAMStatus;
	}

	
	/**
	 * @return
	 */
	public String getFailureDescription() {
		return failureDescription;
	}

	/**
	 * @param failureDescription
	 */
	public void setFailureDescription(String failureDescription) {
		this.failureDescription = failureDescription;
	}

	/**
	 * @return
	 */
	public String getRequestID() {
		return requestID;
	}

	/**
	 * @param requestID
	 */
	public void setRequestID(String requestID) {
		this.requestID = requestID;
	}

	/**
	 * @return
	 */
	public Integer getResponseCode() {
		return responseCode;
	}

	/**
	 * @param responseCode
	 */
	public void setResponseCode(Integer responseCode) {
		this.responseCode = responseCode;
	}
	
	/**
	 * @return
	 */
	public String getPamServiceId() {
		return pamServiceId;
	}
	
	/**
	 * @param pamServiceId
	 */
	public void setPamServiceId(String pamServiceId) {
		this.pamServiceId = pamServiceId;
	}
	
	@Override
	public String toString() {
		return  pamFile + "," + this.failureDescription+ ","+ this.requestID+"," +this.responseCode+","+ this.pamServiceId;
	}
	
	
}
