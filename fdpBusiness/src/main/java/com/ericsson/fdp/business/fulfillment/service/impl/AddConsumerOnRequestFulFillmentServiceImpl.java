package com.ericsson.fdp.business.fulfillment.service.impl;

import javax.annotation.Resource;
import javax.ejb.Stateless;

import com.ericsson.fdp.business.fulfillment.service.FDPFulfillmentService;
import com.ericsson.fdp.business.node.FDPServiceProvisioningNode;
import com.ericsson.fdp.business.request.impl.FDPMetadataResponseImpl;
import com.ericsson.fdp.business.request.impl.FulfillmentRequestImpl;
import com.ericsson.fdp.common.enums.Status;
import com.ericsson.fdp.core.exception.ExecutionFailedException;
import com.ericsson.fdp.core.request.FDPRequest;
import com.ericsson.fdp.core.request.FDPResponse;

@Stateless
public class AddConsumerOnRequestFulFillmentServiceImpl extends AbstractFDPFulFillmentServiceImpl {

	/** The FDPDynamicMenu **/
	@Resource(lookup = "java:app/fdpBusiness-1.0/AddConsumerFulFillmentServiceImpl")
	private FDPFulfillmentService fdpFulfillmentService;
	
	@Override
	protected FDPResponse executeService(FDPRequest fdpRequest, Object... additionalInformations)
			throws ExecutionFailedException {
		
		FDPResponse fdpResponse = new FDPMetadataResponseImpl(Status.FAILURE, true, null);
		final FulfillmentRequestImpl fulfillmentRequestImpl = (FulfillmentRequestImpl) fdpRequest;
		final FDPServiceProvisioningNode fdpServiceProvisioningNode = getNodeProductSP(fulfillmentRequestImpl);
		if (null == fdpServiceProvisioningNode) {
			fdpResponse = handleNodeNotFound(fulfillmentRequestImpl,fdpRequest);
		}
		
		final Long consumerMsisdn = fulfillmentRequestImpl.getSubscriberNumber();
		final String providerMsisdn = fulfillmentRequestImpl.getProviderMsisdn();
		fulfillmentRequestImpl.setSubscriberNumber(Long.valueOf(providerMsisdn));
		fulfillmentRequestImpl.setConsumerMsisdn(consumerMsisdn.toString());
		fulfillmentRequestImpl.setConsumerName(consumerMsisdn.toString());
		
		fdpResponse = fdpFulfillmentService.execute(fulfillmentRequestImpl);
		
		fulfillmentRequestImpl.setSubscriberNumber(consumerMsisdn);
		fulfillmentRequestImpl.setConsumerMsisdn(null);
		fulfillmentRequestImpl.setConsumerName(null);
		return fdpResponse;
	}

}
