package com.ericsson.fdp.business.request.impl;

import java.util.ArrayList;
import java.util.List;

import com.ericsson.fdp.common.enums.Status;
import com.ericsson.fdp.core.display.ResponseMessage;
import com.ericsson.fdp.core.request.FDPResponse;
import com.ericsson.fdp.core.request.ResponseError;

/**
 * The class defines the service provisioning response.
 * 
 * @author Ericsson
 * 
 */
public class FDPResponseImpl implements FDPResponse {

	/**
	 * 
	 */
	private static final long serialVersionUID = -1848167520885819507L;

	/**
	 * The final execution status.
	 */
	private Status executionStatus;

	/**
	 * True if session is to be terminated after this response.
	 */
	private boolean terminateSession;

	/**
	 * Display the value in case of external system.
	 */
	private String systemType;
	
	/**
	 * The response string to be sent to the user.
	 */
	private List<ResponseMessage> responseString;
	
	/**
	 * The error in case required.
	 */
	private ResponseError responseError;

	/**
	 * The constructor.
	 * 
	 * @param executionStatusToSet
	 *            The execution status.
	 * @param terminateSessionToSet
	 *            True, if terminate session.
	 * @param responseStringToSet
	 *            The response string.
	 */
	public FDPResponseImpl(final Status executionStatusToSet, final boolean terminateSessionToSet,
			final List<ResponseMessage> responseString) {
		this.executionStatus = executionStatusToSet;
		this.terminateSession = terminateSessionToSet;
		this.responseString = responseString;
	}
	
	/**
	 * The constructor.
	 * 
	 * @param executionStatusToSet
	 *            The execution status.
	 * @param terminateSessionToSet
	 *            True, if terminate session.
	 * @param responseStringToSet
	 *            The response string.
	 * @param responseError
	 *            response error
	 */
	public FDPResponseImpl(final Status executionStatusToSet, final boolean terminateSessionToSet,
			final List<ResponseMessage> responseString, final ResponseError responseError) {
		this.executionStatus = executionStatusToSet;
		this.terminateSession = terminateSessionToSet;
		this.responseString = responseString;
		this.responseError = responseError;
	}
	/**
	 * The constructor.
	 * 
	 * @param executionStatusToSet
	 *            The execution status.
	 * @param terminateSessionToSet
	 *            True, if terminate session.
	 * @param responseStringToSet
	 *            The response string.
	 * @param systemType
	 *            The External System type.           
	 */
	
	public FDPResponseImpl(final Status executionStatusToSet, final boolean terminateSessionToSet, final String systemType,
			final List<ResponseMessage> responseString) {
		this.executionStatus = executionStatusToSet;
		this.terminateSession = terminateSessionToSet;
		this.responseString = responseString;
		this.systemType = systemType;
	}
	/**
	 * @param executionStatusToSet
	 *            execution status to set.
	 */
	public void setExecutionStatus(final Status executionStatusToSet) {
		this.executionStatus = executionStatusToSet;
	}

	@Override
	public Status getExecutionStatus() {
		return executionStatus;
	}

	@Override
	public boolean isTerminateSession() {
		return terminateSession;
	}

	/**
	 * @param terminateSessionToSet
	 *            the terminateSession to set
	 */
	public void setTerminateSession(final boolean terminateSessionToSet) {
		this.terminateSession = terminateSessionToSet;
	}

	@Override
	public List<ResponseMessage> getResponseString() {
		return responseString;
	}

	/**
	 * @param responseStringToSet
	 *            the responseString to set
	 */
	public void setResponseString(final List<ResponseMessage> responseStringToSet) {
		this.responseString = responseStringToSet;
	}

	/**
	 * @param responseMessage
	 *            the response message to add.
	 */
	public void addResponseString(final ResponseMessage responseMessage) {
		if (this.responseString == null) {
			responseString = new ArrayList<ResponseMessage>();
		}
		responseString.add(responseMessage);
	}

	@Override
	public String toString() {
		return "Execution status :- " + executionStatus.name() + " terminate session :- " + terminateSession
				+ " response string :- " + responseString + " system type :- " + systemType;
	}
	
	/**
	 * @return the responseError
	 */
	public ResponseError getResponseError() {
		return responseError;
	}

	/**
	 * @param responseError the responseError to set
	 */
	public void setResponseError(ResponseError responseError) {
		this.responseError = responseError;
	}
	
	public String getSystemType() {
		return systemType;
	}

	public void setSystemType(String systemType) {
		this.systemType = systemType;
	}
}
