package com.ericsson.fdp.business.request.impl;

import java.io.File;

import com.ericsson.fdp.business.request.ImportFileResponse;
import com.ericsson.fdp.common.enums.Status;

/**
 * This class implements the import file response.
 * 
 * @author Ericsson
 * 
 */
public class ImportFileResponseImpl implements ImportFileResponse {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1063626718431821489L;

	/**
	 * The status of the import.
	 */
	private Status status;

	/**
	 * The count of successful values.
	 */
	private Long successfulValues;

	/**
	 * The count of failed values.
	 */
	private Long failureValues;

	/**
	 * The output file.
	 */
	private File outputFile;

	@Override
	public Status getExecutionStatus() {
		return status;
	}

	@Override
	public Long getSucessfullValues() {
		return successfulValues;
	}

	@Override
	public Long getFailureValues() {
		return failureValues;
	}

	@Override
	public File getFailedOutputFile() {
		return outputFile;
	}

	/**
	 * @return the status
	 */
	public Status getStatus() {
		return status;
	}

	/**
	 * @param status
	 *            the status to set
	 */
	public void setStatus(final Status status) {
		this.status = status;
	}

	/**
	 * @param successfulValues
	 *            the successfulValues to set
	 */
	public void setSuccessfulValues(final Long successfulValues) {
		this.successfulValues = successfulValues;
	}

	/**
	 * @param outputFile
	 *            the outputFile to set
	 */
	public void setOutputFile(final File outputFile) {
		this.outputFile = outputFile;
	}

	/**
	 * @param failureValues
	 *            the failureValues to set
	 */
	public void setFailureValues(final Long failureValues) {
		this.failureValues = failureValues;
	}

}
