package com.ericsson.fdp.business.adapter.impl;

import java.util.Map;

import com.ericsson.fdp.business.adapter.Adapter;
import com.ericsson.fdp.business.bean.HttpAdapterRequest;
import com.ericsson.fdp.core.exception.ExecutionFailedException;
import com.ericsson.fdp.core.request.FDPRequest;
import com.ericsson.fdp.dao.enums.ExternalSystem;
import com.ericsson.fdp.vas.enums.HttpCallClientEnum;

/**
 * This class implements the adapter for http interface.
 * 
 * @author Ericsson
 * 
 * @param <T>
 *            The parameter type which the adapter uses.
 */
public class HTTPAdapter<T> implements Adapter {

	/**
	 * The fdp request, which is used to connect to appropriate external system
	 * for that circle.
	 */
	private final HttpAdapterRequest httpAdapterRequest;

	/**
	 * The external system to be used.
	 */
	private final ExternalSystem externalSystem;

	/**
	 * The request to be executed.
	 */
	private final T httpRequest;

	private FDPRequest fdpRequest;

	/**
	 * The default constructor.
	 * 
	 * @param circleCodeToSet
	 *            The circle code to set.
	 * @param externalSystem
	 *            The external system to set.
	 * @param httpRequestToSet
	 *            The http request to set.
	 * @param fdpRequest
	 */
	public HTTPAdapter(final HttpAdapterRequest httpAdapterRequest, final ExternalSystem externalSystem,
			final T httpRequestToSet, final FDPRequest fdpRequest) {
		this.httpAdapterRequest = httpAdapterRequest;
		this.externalSystem = externalSystem;
		this.httpRequest = httpRequestToSet;
		this.fdpRequest = fdpRequest;

	}

	@Override
	public Map<String, Object> callClient() throws ExecutionFailedException {
		AbstractAdapterHttpCallClient adapterHttpCallClient = HttpCallClientEnum.getObject(externalSystem.name());
		Map<String, Object> responseMap = adapterHttpCallClient.httpCallClient(httpRequest.toString(),
				httpAdapterRequest, externalSystem, fdpRequest);
		return responseMap;
	}

	@Override
	public String toString() {
		return "HTTPAdapter [httpAdapterRequest=" + httpAdapterRequest + ", externalSystem=" + externalSystem
				+ ", httpRequest=" + httpRequest + ", fdpRequest=" + fdpRequest + "]";
	}
}
