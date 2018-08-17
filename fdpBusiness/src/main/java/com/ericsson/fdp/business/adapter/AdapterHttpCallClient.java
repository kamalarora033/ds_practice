package com.ericsson.fdp.business.adapter;

import java.util.Map;

import com.ericsson.fdp.business.bean.HttpAdapterRequest;
import com.ericsson.fdp.core.exception.ExecutionFailedException;
import com.ericsson.fdp.core.request.FDPRequest;
import com.ericsson.fdp.dao.enums.ExternalSystem;

/**
 * The Interface AdapterHttpCallClient.
 */
public interface AdapterHttpCallClient {

	/**
	 * Call client response on the basis of external system type and required
	 * parameter definition.
	 * 
	 * @param httpRequest
	 *            the http request
	 * @param httpAdapterRequest
	 *            the http adapter request
	 * @param endpoint
	 *            the endpoint
	 * @param externalSystemType
	 *            the external system type
	 * @param requestId
	 *            the request id
	 * @param circleName
	 *            the circle name
	 * @param moduleName
	 *            the module name
	 * @param appCacheSubStoreKey
	 *            the app cache sub store key
	 * @param fdpRequest
	 *            the fdp request
	 * @return the map
	 * @throws ExecutionFailedException
	 *             the execution failed exception
	 */
	Map<String, Object> httpCallClient(String httpRequest, HttpAdapterRequest httpAdapterRequest,
			ExternalSystem externalSystemType, FDPRequest fdpRequest) throws ExecutionFailedException;
}
