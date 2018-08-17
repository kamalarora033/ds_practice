package com.ericsson.fdp.business.request.impl;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.ericsson.fdp.business.enums.AuxRequestParam;
import com.ericsson.fdp.common.constants.FDPConstant;
import com.ericsson.fdp.common.enums.Status;
import com.ericsson.fdp.core.display.ResponseMessage;
import com.ericsson.fdp.core.request.ResponseError;

public class FDPMetadataResponseImpl extends FDPResponseImpl {

	/**
	 * The class serial version UID.
	 */
	private static final long serialVersionUID = 4692145353618608757L;

	private Map<AuxRequestParam, Object> auxiliaryRequestParameters;
	
	private String fulfillmentResponse;
	
	public FDPMetadataResponseImpl(final Status executionStatusToSet, final boolean terminateSessionToSet,
			final List<ResponseMessage> responseString) {
		super(executionStatusToSet, terminateSessionToSet, responseString);
	}

	public FDPMetadataResponseImpl(final Status executionStatusToSet, final boolean terminateSessionToSet,
			final List<ResponseMessage> responseString, final ResponseError responseError) {
		super(executionStatusToSet, terminateSessionToSet, responseString, responseError);
	}

	/**
	 * This method is used to put values in auxiliary response parameter.
	 * 
	 * @param key
	 *            The key to be put.
	 * @param value
	 *            The value to be put.
	 */
	public void putAuxiliaryRequestParameter(final AuxRequestParam key, final Object value) {
		if (auxiliaryRequestParameters == null) {
			auxiliaryRequestParameters = new HashMap<AuxRequestParam, Object>(
					FDPConstant.DEFAULT_INTIAL_CAPACITY_META_VALUES);
		}
		auxiliaryRequestParameters.put(key, value);
	}

	public Object getAuxiliaryRequestParameter(final AuxRequestParam key) {
		return auxiliaryRequestParameters == null ? null : auxiliaryRequestParameters.get(key);
	}

	/**
	 * @return the fulfillmentResponse
	 */
	public String getFulfillmentResponse() {
		return fulfillmentResponse;
	}

	/**
	 * @param fulfillmentResponse the fulfillmentResponse to set
	 */
	public void setFulfillmentResponse(String fulfillmentResponse) {
		this.fulfillmentResponse = fulfillmentResponse;
	}

}
