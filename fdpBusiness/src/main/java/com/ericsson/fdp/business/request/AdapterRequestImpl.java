package com.ericsson.fdp.business.request;

/**
 * The adapter request implementation.
 * 
 * @author Ericsson
 * 
 */
public class AdapterRequestImpl implements AdapterRequest {

	/**
	 * 
	 */
	private static final long serialVersionUID = -4496991127719444381L;

	/** The request id. */
	private String requestId;

	/** The circle code. */
	private String circleCode;

	/** The circle name. */
	private String circleName;

	/**
	 * The command to log.
	 */
	private String logValue;

	/**
	 * Instantiates a new http adapter request.
	 * 
	 * @param requestId
	 *            the request id
	 * @param circleCode
	 *            the circle code
	 * @param circleName
	 *            the circle name
	 */
	public AdapterRequestImpl(String requestId, String circleCode, String circleName, String logValue) {
		super();
		this.requestId = requestId;
		this.circleCode = circleCode;
		this.circleName = circleName;
		this.logValue = logValue;
	}

	/**
	 * Gets the request id.
	 * 
	 * @return the request id
	 */
	public String getRequestId() {
		return requestId;
	}

	/**
	 * Gets the circle code.
	 * 
	 * @return the circle code
	 */
	public String getCircleCode() {
		return circleCode;
	}

	/**
	 * Gets the circle name.
	 * 
	 * @return the circle name
	 */
	public String getCircleName() {
		return circleName;
	}

	/**
	 * Sets the request id.
	 * 
	 * @param requestId
	 *            the new request id
	 */
	public void setRequestId(String requestId) {
		this.requestId = requestId;
	}

	/**
	 * Sets the circle code.
	 * 
	 * @param circleCode
	 *            the new circle code
	 */
	public void setCircleCode(String circleCode) {
		this.circleCode = circleCode;
	}

	/**
	 * Sets the circle name.
	 * 
	 * @param circleName
	 *            the new circle name
	 */
	public void setCircleName(String circleName) {
		this.circleName = circleName;
	}

	/**
	 * @return the logValue
	 */
	public String getLogValue() {
		return logValue;
	}

	/**
	 * @param logValue
	 *            the logValue to set
	 */
	public void setLogValue(String logValue) {
		this.logValue = logValue;
	}

}
