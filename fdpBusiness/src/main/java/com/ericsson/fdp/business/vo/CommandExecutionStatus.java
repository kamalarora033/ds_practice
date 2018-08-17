package com.ericsson.fdp.business.vo;

import com.ericsson.fdp.common.enums.ExternalSystem;
import com.ericsson.fdp.common.enums.Status;

/**
 * This class stores the command execution status.
 * 
 * @author Ericsson
 * 
 */
public class CommandExecutionStatus {

	/**
	 * The status of the command.
	 */
	private Status status;

	/**
	 * The code for the error if any.
	 */
	private Integer code;

	/**
	 * The error description.
	 */
	private String description;

	/**
	 * The error type, faultCode or responseCode.
	 */
	private String errorType;
	
	private ExternalSystem externalSystem;

	/**
	 * The constructor for command execution status.
	 * 
	 * @param status
	 *            the status.
	 */
	public CommandExecutionStatus(final Status status) {
		super();
		this.status = status;
	}

	/**
	 * The constructor for command execution status.
	 */
	public CommandExecutionStatus() {
		super();
	}

	/**
	 * The constructor for command execution status.
	 */
	public CommandExecutionStatus(final Status status, final Integer code, final String description,
			final String errorType,final ExternalSystem externalSystem) {
		super();
		this.status = status;
		this.code = code;
		this.description = description;
		this.errorType = errorType;
		this.externalSystem = externalSystem;
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
	public void setStatus(Status status) {
		this.status = status;
	}

	/**
	 * @return the code
	 */
	public Integer getCode() {
		return code;
	}

	/**
	 * @param code
	 *            the code to set
	 */
	public void setCode(Integer code) {
		this.code = code;
	}

	/**
	 * @return the description
	 */
	public String getDescription() {
		return description;
	}

	/**
	 * @param description
	 *            the description to set
	 */
	public void setDescription(String description) {
		this.description = description;
	}

	/**
	 * @return the errorType
	 */
	public String getErrorType() {
		return errorType;
	}

	/**
	 * @param errorType
	 *            the errorType to set
	 */
	public void setErrorType(String errorType) {
		this.errorType = errorType;
	}

	@Override
	public String toString() {
		return "execution status:- " + status.name() + " error type :- " + errorType + " error code :- " + code
				+ " error desc :- " + description;
	}

	public String toString(boolean modify) {
		return "status-" + status.name() + ";error code-" + code + ";error desc-" + description;
	}

	/**
	 * @return the externalSystem
	 */
	public ExternalSystem getExternalSystem() {
		return externalSystem;
	}

}
