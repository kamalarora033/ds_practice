package com.ericsson.fdp.business.bean;

import com.ericsson.fdp.business.request.AdapterRequestImpl;
import com.ericsson.fdp.core.entity.FDPEMADetail;

/**
 * The Class HttpAdapterRequest which used for getting necessary parameter for
 * calling Http Adapter.
 */
public class SSHAdapterRequest extends AdapterRequestImpl {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1340215623971233493L;

	/**
	 * The end point.
	 */
	private String endPoint;
	
	private FDPEMADetail emaDetail;
	
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
	 * @param endPoint
	 *            the endpoint on which this is to be executed.
	 */
	public SSHAdapterRequest(String requestId, String circleCode, String circleName, FDPEMADetail emaDetail, String commandName, String logValue) {
		super(requestId, circleCode, circleName, logValue);
		//this.endPoint = endPoint;
		this.emaDetail = emaDetail;
		this.commandName = commandName;
	}

	/**
	 * @return the endpoint.
	 */
	public String getEndPoint() {
		return endPoint;
	}

	/**
	 * This method is used to set the endpoint.
	 * 
	 * @param endPoint
	 *            the endpoint.
	 */
	public void setEndpoint(final String endPoint) {
		this.endPoint = endPoint;
	}

	public FDPEMADetail getEmaDetail() {
		return emaDetail;
	}

	public void setEmaDetail(FDPEMADetail emaDetail) {
		this.emaDetail = emaDetail;
	}

	public String getCommandName() {
		return commandName;
	}

	public void setCommandName(String commandName) {
		this.commandName = commandName;
	}
}
