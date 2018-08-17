package com.ericsson.fdp.business.bean;

import com.ericsson.fdp.core.entity.FDPEMADetail;

/**
 * The Class HttpAdapterRequest which used for getting necessary parameter for
 * calling Http Adapter.
 */
public class TelnetAdapterRequest extends SSHAdapterRequest {

	private static final long serialVersionUID = 696299437363163488L;

	public TelnetAdapterRequest(String requestId, String circleCode, String circleName, FDPEMADetail emaDetail, String commandName, String logValue) {
		super(requestId, circleCode, circleName, emaDetail, commandName, logValue);
	}

}
