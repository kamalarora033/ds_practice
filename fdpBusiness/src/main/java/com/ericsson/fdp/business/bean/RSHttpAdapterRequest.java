package com.ericsson.fdp.business.bean;

import com.ericsson.fdp.core.entity.ExternalSystemDetail;

/**
 * The Class HttpAdapterRequest which used for getting necessary parameter for
 * calling Http Adapter.
 * 
 * @author Ericsson
 */
public class RSHttpAdapterRequest extends HttpAdapterRequest {

	/**
	 * 
	 */
	private static final long serialVersionUID = -6213459688342009686L;
	private String commandName;

	public String getCommandName() {
		return commandName;
	}

	public void setCommandName(String commandName) {
		this.commandName = commandName;
	}

	public RSHttpAdapterRequest(String requestId, String circleCode, String circleName,
			ExternalSystemDetail externalSystemDetail, String commandName) {
		super(requestId, circleCode, circleName, externalSystemDetail);
		this.commandName = commandName;
	}

}
