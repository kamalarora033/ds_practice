package com.ericsson.fdp.business.fulfillment.service.impl;

import javax.annotation.Resource;
import javax.ejb.Stateless;

import com.ericsson.fdp.business.enums.AuxRequestParam;
import com.ericsson.fdp.business.enums.RequestMetaValuesKey;
import com.ericsson.fdp.business.fulfillment.service.FDPFulfillmentService;
import com.ericsson.fdp.business.node.FDPServiceProvisioningNode;
import com.ericsson.fdp.business.request.impl.FDPMetadataResponseImpl;
import com.ericsson.fdp.business.request.impl.FulfillmentRequestImpl;
import com.ericsson.fdp.common.enums.Status;
import com.ericsson.fdp.core.exception.ExecutionFailedException;
import com.ericsson.fdp.core.request.FDPRequest;
import com.ericsson.fdp.core.request.FDPResponse;
import com.ericsson.fdp.dao.enums.FDPServiceProvSubType;

@Stateless
public class AddConsumerFulFillmentServiceImpl extends AbstractFDPFulFillmentServiceImpl {

	/** The FDPDynamicMenu **/
	@Resource(lookup = "java:app/fdpBusiness-1.0/AcceptConsumerFulFillmentServiceImpl")
	private FDPFulfillmentService fdpFulfillmentService;
	
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
			updateSPInRequestForProductId(productIdLong, FDPServiceProvSubType.SHARED_ACCOUNT_ADD_CONSUMER,
					fulfillmentRequestImpl);
			final String consumerName = (null != fulfillmentRequestImpl.getConsumerName() ? fulfillmentRequestImpl
					.getConsumerName() : fulfillmentRequestImpl.getConsumerMsisdn());
			fulfillmentRequestImpl.putAuxiliaryRequestParameter(AuxRequestParam.CONSUMER_NAME,
					consumerName);
			fulfillmentRequestImpl.putAuxiliaryRequestParameter(AuxRequestParam.CONSUMER_MSISDN,
					fulfillmentRequestImpl.getConsumerMsisdn());
			fulfillmentRequestImpl.putAuxiliaryRequestParameter(AuxRequestParam.PROVIDER_MSISDN,
					fulfillmentRequestImpl.getSubscriberNumber());
			fdpResponse = executeSP(fulfillmentRequestImpl);

			if ((fulfillmentRequestImpl.getAutoApprove()) && (null != fdpResponse)
					&& (Status.SUCCESS.equals(fdpResponse.getExecutionStatus()))) {
				updateSharedRIDInRequestForAccept(fulfillmentRequestImpl, fdpResponse);
				final Long orignalSubscriberMsisdn = fulfillmentRequestImpl.getSubscriberNumber();
				fulfillmentRequestImpl.setSubscriberNumber(Long.valueOf(fulfillmentRequestImpl.getConsumerMsisdn()));
				fdpResponse = fdpFulfillmentService.execute(fulfillmentRequestImpl);
				fulfillmentRequestImpl.setSubscriberNumber(orignalSubscriberMsisdn);
			}

		}
		return fdpResponse;
	}

	/**
	 * This method fetches the database ID from response.
	 * 
	 * @param fdpMetadataResponseImpl
	 * @return
	 * @throws ExecutionFailedException
	 */
	private void updateSharedRIDInRequestForAccept(final FDPRequest fdpRequest,
			final FDPResponse fdpResponse) throws ExecutionFailedException {
		Integer sharedRID = null;
		checkInstanceBefore(fdpRequest, fdpResponse);
		final FulfillmentRequestImpl fulfillmentRequestImpl = (FulfillmentRequestImpl) fdpRequest;
		final FDPMetadataResponseImpl fdpMetadataResponseImpl = (FDPMetadataResponseImpl) fdpResponse;
		sharedRID = Integer.valueOf(fdpMetadataResponseImpl.getAuxiliaryRequestParameter(AuxRequestParam.SHARED_DBID)
				.toString());
		if (null == sharedRID) {
			throw new ExecutionFailedException("Unable to get Database-ID for shared account");
		} else {
			fulfillmentRequestImpl.setDbId(sharedRID);
		}
	}
}
