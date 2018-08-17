/**
 * 
 */
package com.ericsson.fdp.business.dto.pam;

import java.io.Serializable;
import java.util.List;

/**
 * The Class PAMCleanUpResponse.
 *
 * @author Ericsson
 */
public class PAMCleanUpResponse implements Serializable {
	
	/** The Constant serialVersionUID. */
	private static final long serialVersionUID = -2154787201117699008L;
	
	/** The pam file response. */
	private List<PAMFileResponse> pamFileResponse;
	
	/** The records processed. */
	private Long recordsProcessed;
	
	/** The records failed. */
	private Long recordsFailed;
	
	private Boolean isNewFile;
	
	/**
	 * Instantiates a new pAM clean up response.
	 */
	public PAMCleanUpResponse() {
	};

	/**
	 * Instantiates a new pAM clean up response.
	 * 
	 * @param recordsProcessed
	 *            the records processed
	 * @param recordsFailed
	 *            the records failed
	 * @param pamFileResponseList
	 *            the pam file response list
	 */
	public PAMCleanUpResponse(long recordsProcessed, long recordsFailed,
			List<PAMFileResponse> pamFileResponseList) {
		this.recordsFailed = recordsFailed;
		this.recordsProcessed = recordsProcessed;
		this.pamFileResponse = pamFileResponseList;
	}

	/**
	 * Gets the pam file response.
	 *
	 * @return the pam file response
	 */
	public List<PAMFileResponse> getPamFileResponse() {
		return pamFileResponse;
	}
	
	/**
	 * Sets the pam file response.
	 *
	 * @param pamFileResponse the new pam file response
	 */
	public void setPamFileResponse(final List<PAMFileResponse> pamFileResponse) {
		this.pamFileResponse = pamFileResponse;
	}
	
	/**
	 * Gets the records processed.
	 *
	 * @return the records processed
	 */
	public Long getRecordsProcessed() {
		return recordsProcessed;
	}
	
	/**
	 * Sets the records processed.
	 *
	 * @param recordsProcessed the new records processed
	 */
	public void setRecordsProcessed(final Long recordsProcessed) {
		this.recordsProcessed = recordsProcessed;
	}
	
	/**
	 * Gets the records failed.
	 *
	 * @return the records failed
	 */
	public Long getRecordsFailed() {
		return recordsFailed;
	}
	
	/**
	 * Sets the records failed.
	 *
	 * @param recordsFailed the new records failed
	 */
	public void setRecordsFailed(final Long recordsFailed) {
		this.recordsFailed = recordsFailed;
	}

	public boolean isNewFile() {
		return isNewFile;
	}

	public void setNewFile(Boolean isNewFile) {
		this.isNewFile = isNewFile;
	}
	
	

}
