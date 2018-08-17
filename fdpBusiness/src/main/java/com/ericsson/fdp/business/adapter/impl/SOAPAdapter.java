package com.ericsson.fdp.business.adapter.impl;

import java.util.Map;

import com.ericsson.fdp.business.adapter.Adapter;
import com.ericsson.fdp.business.bean.SOAPAdapterRequest;
import com.ericsson.fdp.core.exception.ExecutionFailedException;
import com.ericsson.fdp.core.request.FDPRequest;
import com.ericsson.fdp.dao.enums.ExternalSystem;
import com.ericsson.fdp.vas.enums.SOAPCallClientEnum;

/**
 * This class implements the adapter for http interface.
 * 
 * @author Ericsson
 * 
 * @param <T>
 *            The parameter type which the adapter uses.
 */
public class SOAPAdapter<T> implements Adapter {

	/**
	 * The fdp request, which is used to connect to appropriate external system
	 * for that circle.
	 */
	private final SOAPAdapterRequest soapAdapterRequest;

	/**
	 * The external system to be used.
	 */
	private final ExternalSystem externalSystem;

	/**
	 * The request to be executed.
	 */
	private final T soapRequest;

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
	public SOAPAdapter(final SOAPAdapterRequest soapAdapterRequest, final ExternalSystem externalSystem,
			final T soapRequestToSet, final FDPRequest fdpRequest) {
		this.soapAdapterRequest = soapAdapterRequest;
		this.externalSystem = externalSystem;
		this.soapRequest = soapRequestToSet;
		this.fdpRequest = fdpRequest;

	}

	@Override
	public Map<String, Object> callClient() throws ExecutionFailedException {
		AbstractAdapterSOAPCallClient adapterSOAPCallClient = SOAPCallClientEnum.getObject(externalSystem.name());
		Map<String, Object> responseMap = adapterSOAPCallClient.soapCallClient(soapRequest.toString(),
				soapAdapterRequest, externalSystem, fdpRequest);
		return responseMap;
	}

	@Override
	public String toString() {
		return "SOAPAdapter [soapAdapterRequest=" + soapAdapterRequest + ", externalSystem=" + externalSystem
				+ ", sapRequest=" + soapRequest + ", fdpRequest=" + fdpRequest + "]";
	}
}
