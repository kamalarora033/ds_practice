package com.ericsson.fdp.business.adapter.impl;

import java.util.HashMap;
import java.util.Map;

import org.apache.camel.cdi.CdiCamelContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ericsson.fdp.business.adapter.Adapter;
import com.ericsson.fdp.business.bean.TelnetAdapterRequest;
import com.ericsson.fdp.core.exception.ExecutionFailedException;
import com.ericsson.fdp.dao.enums.ExternalSystem;

/**
 * This class implements the adapter for http interface.
 * 
 * @author Ericsson
 * 
 * @param <T>
 *            The parameter type which the adapter uses.
 */
public class DefaultAdapter<T> implements Adapter {

	/**
	 * The fdp request, which is used to connect to appropriate external system
	 * for that circle.
	 */
	private final TelnetAdapterRequest httpAdapterRequest;

	/**
	 * The external system to be used.
	 */
	private final ExternalSystem externalSystem;

	/**
	 * The request to be executed.
	 */
	private final T httpRequest;

	/** The context. */
	private CdiCamelContext context;

	// Thread local variable containing each thread's ID
	private static final ThreadLocal<String> requestIdLocal = new ThreadLocal<String>();

	private static final Logger LOGGER = LoggerFactory.getLogger(DefaultAdapter.class);

	/**
	 * The default constructor.
	 * 
	 * @param circleCodeToSet
	 *            The circle code to set.
	 * @param externalSystem
	 *            The external system to set.
	 * @param httpRequestToSet
	 *            The http request to set.
	 */
	public DefaultAdapter(final TelnetAdapterRequest httpAdapterRequest, final ExternalSystem externalSystem,
			final T httpRequestToSet) {
		this.httpAdapterRequest = httpAdapterRequest;
		this.externalSystem = externalSystem;
		this.httpRequest = httpRequestToSet;
	}

	@Override
	public Map<String, Object> callClient() throws ExecutionFailedException {
		Map<String,Object> temp=new HashMap<String, Object>();
		temp.put("COMMAND_OUTPUT", null);
		temp.put("RESPONSE_CODE","-200");
		return temp;

	}

	@Override
	public String toString() {
		return " Telnet adapter , external system :- " + externalSystem.name() + " circle "
				+ httpAdapterRequest.getCircleCode() + " request string :- " + httpRequest.toString();

	}
}
