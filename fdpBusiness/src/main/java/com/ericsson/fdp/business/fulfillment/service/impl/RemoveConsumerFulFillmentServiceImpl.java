package com.ericsson.fdp.business.fulfillment.service.impl;

import javax.ejb.Stateless;

import com.ericsson.fdp.business.enums.AuxRequestParam;
import com.ericsson.fdp.business.enums.RequestMetaValuesKey;
import com.ericsson.fdp.business.node.FDPServiceProvisioningNode;
import com.ericsson.fdp.business.request.impl.FulfillmentRequestImpl;
import com.ericsson.fdp.core.exception.ExecutionFailedException;
import com.ericsson.fdp.core.request.FDPRequest;
import com.ericsson.fdp.core.request.FDPResponse;
import com.ericsson.fdp.dao.enums.FDPServiceProvSubType;

@Stateless
public class RemoveConsumerFulFillmentServiceImpl extends AbstractFDPFulFillmentServiceImpl{

	@Override
	protected FDPResponse executeService(FDPRequest fdpRequest, Object... additionalInformations)
			throws ExecutionFailedException {
		FDPResponse fdpResponse = null;
		final FulfillmentRequestImpl fulfillmentRequestImpl = (FulfillmentRequestImpl) fdpRequest;
		final FDPServiceProvisioningNode fdpServiceProvisioningNode = getNodeProductSP(fulfillmentRequestImpl);
		if (null == fdpServiceProvisioningNode) {
			fdpResponse = handleNodeNotFound(fulfillmentRequestImpl,fdpRequest);
		}
		if (null == fdpResponse
				&& (null != fdpServiceProvisioningNode.getEntityIdForCache(RequestMetaValuesKey.PRODUCT))) {
			final String productId = fdpServiceProvisioningNode.getEntityIdForCache(RequestMetaValuesKey.PRODUCT);
			final Long productIdLong = Long.valueOf(productId);
			updateSPInRequestForProductId(productIdLong,
					FDPServiceProvSubType.SHARED_ACCOUNT_REMOVE_CONSUMER, fulfillmentRequestImpl);
			fulfillmentRequestImpl.putAuxiliaryRequestParameter(AuxRequestParam.CONSUMER_MSISDN,
					fulfillmentRequestImpl.getConsumerMsisdn());
			fdpResponse = executeSP(fulfillmentRequestImpl);
		}
		return fdpResponse;
	}

}
