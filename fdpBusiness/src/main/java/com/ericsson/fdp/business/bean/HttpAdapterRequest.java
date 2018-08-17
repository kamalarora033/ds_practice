package com.ericsson.fdp.business.bean;

import com.ericsson.fdp.business.request.AdapterRequestImpl;
import com.ericsson.fdp.core.entity.ExternalSystemDetail;

/**
 * The Class HttpAdapterRequest which used for getting necessary parameter for
 * calling Http Adapter.
 * 
 * @author Ericsson
 */
public class HttpAdapterRequest extends AdapterRequestImpl {

	/** The Constant serialVersionUID. */
	private static final long serialVersionUID = 1340215623971233493L;

	/** The external system detail. */
	private ExternalSystemDetail externalSystemDetail;
	
	/** The commandName **/
	private String commandName;

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
	public HttpAdapterRequest(final String requestId, final String circleCode, final String circleName,
			final String logValue) {
		super(requestId, circleCode, circleName, logValue);
	}

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
	public HttpAdapterRequest(final String requestId, final String circleCode, final String circleName,
			final ExternalSystemDetail externalSystemDetail) {
		super(requestId, circleCode, circleName, null);
		this.externalSystemDetail = externalSystemDetail;
	}

	public ExternalSystemDetail getExternalSystemDetail() {
		return externalSystemDetail;
	}

	public String getCommandName() {
		return commandName;
	}
	
	public HttpAdapterRequest(final String requestId, final String circleCode, final String circleName,
			final ExternalSystemDetail externalSystemDetail, final String commandName) {
		super(requestId, circleCode, circleName, null);
		this.externalSystemDetail = externalSystemDetail;
		this.commandName = commandName;
	}
}
