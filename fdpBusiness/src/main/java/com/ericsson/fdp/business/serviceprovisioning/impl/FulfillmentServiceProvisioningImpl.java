package com.ericsson.fdp.business.serviceprovisioning.impl;

import java.util.Map;

import javax.ejb.Stateless;

import com.ericsson.fdp.business.enums.AuxRequestParam;
import com.ericsson.fdp.business.request.impl.FDPMetadataResponseImpl;
import com.ericsson.fdp.business.request.impl.FulfillmentRequestImpl;
import com.ericsson.fdp.core.exception.ExecutionFailedException;
import com.ericsson.fdp.core.request.FDPRequest;
import com.ericsson.fdp.core.request.FDPResponse;

/**
 * This class implements the service provisioning.
 * 
 * @author Ericsson
 * 
 */
@Stateless
public class FulfillmentServiceProvisioningImpl extends ServiceProvisioningImpl {

	/**
	 * The class serial version UID.
	 */
	private static final long serialVersionUID = -8192994061872106205L;

	@Override
	public FDPResponse executeServiceProvisioning(final FDPRequest fdpRequest) throws ExecutionFailedException {
		FDPResponse fdpResponse = super.executeServiceProvisioning(fdpRequest);
		return addMetadataToResponse(fdpResponse, fdpRequest);
	}

	/**
	 * This method will prepare the meta response for handling tariff enquiry
	 * meta details.
	 * 
	 * @param fdpResponse
	 * @return
	 * @throws ExecutionFailedException
	 */
	private FDPResponse addMetadataToResponse(final FDPResponse fdpResponse, final FDPRequest fdpRequest)
			throws ExecutionFailedException {
		final FDPMetadataResponseImpl metaResponse = new FDPMetadataResponseImpl(fdpResponse.getExecutionStatus(),
				fdpResponse.isTerminateSession(), fdpResponse.getResponseString(), fdpResponse.getResponseError());
		
		if (fdpRequest instanceof FulfillmentRequestImpl) {
			FulfillmentRequestImpl fulfillmentRequestImpl = (FulfillmentRequestImpl) fdpRequest;
			final Map<AuxRequestParam, Object> auxRequestParamMap = fulfillmentRequestImpl.getAuxReqParamMap();
			for (Map.Entry<AuxRequestParam, Object> requestParam : auxRequestParamMap.entrySet()) {
					metaResponse.putAuxiliaryRequestParameter(requestParam.getKey(), requestParam.getValue());
				}
			}
		 
		return metaResponse;
	}
}
