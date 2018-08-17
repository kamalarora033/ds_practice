package com.ericsson.fdp.business.fulfillment.service.impl;

import javax.ejb.Stateless;

import com.ericsson.fdp.business.enums.AuxRequestParam;
import com.ericsson.fdp.business.enums.RequestMetaValuesKey;
import com.ericsson.fdp.business.node.FDPServiceProvisioningNode;
import com.ericsson.fdp.business.request.impl.FDPMetadataResponseImpl;
import com.ericsson.fdp.business.request.impl.FulfillmentRequestImpl;
import com.ericsson.fdp.common.enums.Status;
import com.ericsson.fdp.core.exception.ExecutionFailedException;
import com.ericsson.fdp.core.request.FDPRequest;
import com.ericsson.fdp.core.request.FDPResponse;
import com.ericsson.fdp.dao.enums.FDPServiceProvSubType;

@Stateless
public class DetachConsumerFulFillmentServiceImpl extends AbstractFDPFulFillmentServiceImpl {

	@Override
	protected FDPResponse executeService(FDPRequest fdpRequest, Object... additionalInformations)
			throws ExecutionFailedException {

		FDPResponse fdpResponse = new FDPMetadataResponseImpl(Status.FAILURE, true, null);
		final FulfillmentRequestImpl fulfillmentRequestImpl = (FulfillmentRequestImpl) fdpRequest;
		final FDPServiceProvisioningNode fdpServiceProvisioningNode = getNodeProductSP(fulfillmentRequestImpl);
		if (null == fdpServiceProvisioningNode) {
			fdpResponse = handleNodeNotFound(fulfillmentRequestImpl,fdpRequest);
		}

		if (null != fdpServiceProvisioningNode.getEntityIdForCache(RequestMetaValuesKey.PRODUCT)) {
			final String productId = fdpServiceProvisioningNode.getEntityIdForCache(RequestMetaValuesKey.PRODUCT);
			final Long productIdLong = Long.valueOf(productId);
			final Long consumerMsisdn = fulfillmentRequestImpl.getSubscriberNumber();
			final Long providerMsisdn = Long.valueOf(fulfillmentRequestImpl.getProviderMsisdn());
			updateSPInRequestForProductId(productIdLong, FDPServiceProvSubType.SHARED_ACCOUNT_REMOVE_CONSUMER,
					fulfillmentRequestImpl);
			
			// Updating Consumer Msisdn in request aux and provider msisdn in
			// request.
			fulfillmentRequestImpl.putAuxiliaryRequestParameter(AuxRequestParam.CONSUMER_MSISDN, consumerMsisdn);
			fulfillmentRequestImpl.setSubscriberNumber(providerMsisdn);

			fdpResponse = executeSP(fulfillmentRequestImpl);

			// Reverting back the updated values.
			fulfillmentRequestImpl.setSubscriberNumber(consumerMsisdn);
		}
		return fdpResponse;
	}

}
