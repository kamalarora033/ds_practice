package com.ericsson.fdp.business.request.impl;

import java.util.HashMap;
import java.util.Map;

import com.ericsson.fdp.core.request.FDPStepResponse;

/**
 * This class is the implementation of the step response impl.
 * 
 * @author Ericsson
 * 
 */
public class FDPStepResponseImpl implements FDPStepResponse {

	private static final long serialVersionUID = 6115273307681154116L;
	
	private Map<String, Object> responseValueMap;

	@Override
	public Object getStepResponseValue(String key) {
		return responseValueMap == null ? null : responseValueMap.get(key);
	}

	/**
	 * This method is used to add a step response value.
	 * 
	 * @param key
	 *            the key for the response value.
	 * @param value
	 *            the value.
	 * @return the previous value associated with the key.
	 */
	public Object addStepResponseValue(String key, Object value) {
		if (responseValueMap == null) {
			this.responseValueMap = new HashMap<String, Object>();
		}
		return this.responseValueMap.put(key, value);
	}
	
	public void addStepResponse(FDPStepResponse stepResponse) {
		if (stepResponse instanceof FDPStepResponseImpl) {
			if (responseValueMap == null) {
				this.responseValueMap = new HashMap<String, Object>();
			}
			this.responseValueMap.putAll(((FDPStepResponseImpl) stepResponse).getResponseValueMap());
		}
	}

	public Map<String, Object> getResponseValueMap() {
		return responseValueMap;
	}


}
