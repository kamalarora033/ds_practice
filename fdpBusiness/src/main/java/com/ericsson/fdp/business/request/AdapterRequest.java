package com.ericsson.fdp.business.request;

import java.io.Serializable;

/**
 * 
 * The adapter request.
 * 
 * @author Ericsson
 * 
 */
public interface AdapterRequest extends Serializable {

	/**
	 * Gets the request id.
	 * 
	 * @return the request id
	 */
	public String getRequestId();

	/**
	 * Gets the circle code.
	 * 
	 * @return the circle code
	 */
	public String getCircleCode();

	/**
	 * Gets the circle name.
	 * 
	 * @return the circle name
	 */
	public String getCircleName();

}
