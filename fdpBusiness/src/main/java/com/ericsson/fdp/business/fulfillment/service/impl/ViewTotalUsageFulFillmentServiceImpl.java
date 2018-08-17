package com.ericsson.fdp.business.fulfillment.service.impl;

import javax.ejb.Stateless;

import com.ericsson.fdp.business.enums.AuxRequestParam;
import com.ericsson.fdp.business.enums.RequestMetaValuesKey;
import com.ericsson.fdp.business.node.FDPServiceProvisioningNode;
import com.ericsson.fdp.business.request.impl.FDPMetadataResponseImpl;
import com.ericsson.fdp.business.request.impl.FulfillmentRequestImpl;
import com.ericsson.fdp.business.response.fulfillment.xml.Consumer;
import com.ericsson.fdp.common.enums.Status;
import com.ericsson.fdp.core.exception.ExecutionFailedException;
import com.ericsson.fdp.core.request.FDPRequest;
import com.ericsson.fdp.core.request.FDPResponse;
import com.ericsson.fdp.dao.enums.FDPServiceProvSubType;
import com.ericsson.fdp.dao.enums.SharedAccountResponseType;
import com.ericsson.fdp.dao.enums.StepNameEnum;

/**
 * This is the view total usage service of shared account.
 * 
 * @author Ericsson
 * 
 */
@Stateless
public class ViewTotalUsageFulFillmentServiceImpl extends AbstractFDPFulFillmentServiceImpl{

	@Override
	protected FDPResponse executeService(FDPRequest fdpRequest, Object... additionalInformations)
			throws ExecutionFailedException {
		FDPResponse fdpResponse = null;
		final FulfillmentRequestImpl fulfillmentRequestImpl = (FulfillmentRequestImpl) fdpRequest;
		final FDPServiceProvisioningNode fdpServiceProvisioningNode = getNodeProductSP(fulfillmentRequestImpl);
		if (null == fdpServiceProvisioningNode) {
			fdpResponse = handleNodeNotFound(fulfillmentRequestImpl,fdpRequest);
		}
		if(null == fdpResponse
				&& (null != fdpServiceProvisioningNode.getEntityIdForCache(RequestMetaValuesKey.PRODUCT))) {
			final String productId = fdpServiceProvisioningNode.getEntityIdForCache(RequestMetaValuesKey.PRODUCT);
			final Long productIdLong = Long.valueOf(productId);
			updateSPInRequestForProductId(productIdLong,
					FDPServiceProvSubType.SHARED_ACCOUNT_VIEW_TOTAL_USAGE_UC, fulfillmentRequestImpl);
			fulfillmentRequestImpl.putAuxiliaryRequestParameter(AuxRequestParam.CONSUMER_MSISDN,
					fulfillmentRequestImpl.getConsumerMsisdn());
			fulfillmentRequestImpl.putAuxiliaryRequestParameter(AuxRequestParam.PROVIDER_MSISDN,
					fulfillmentRequestImpl.getSubscriberNumber());
			fdpResponse = executeSP(fulfillmentRequestImpl);
			if(null != fdpResponse && Status.SUCCESS.equals(fdpResponse.getExecutionStatus())) {
				final Object consumerUsage = fdpRequest.getValueFromStep(StepNameEnum.VALIDATION_STEP.getValue(),
						SharedAccountResponseType.CONSUMER_LIMIT.name());
				final Object consumerLimitUnit = fdpRequest.getValueFromStep(StepNameEnum.VALIDATION_STEP.getValue(),
						SharedAccountResponseType.CONSUMER_THRESHOLD_UNIT.name());
				final Consumer consumer = new Consumer();
				consumer.setMsisdn(fulfillmentRequestImpl.getConsumerMsisdn());
				consumer.setUsage(String.valueOf(consumerUsage));
				consumer.setUnit(String.valueOf(consumerLimitUnit));
				updateSPInRequestForProductId(productIdLong,
						FDPServiceProvSubType.SHARED_ACCOUNT_VIEW_TOTAL_USAGE_UT, fulfillmentRequestImpl);
				fdpResponse = executeSP(fulfillmentRequestImpl);
				final Object consumerThreshold = fdpRequest.getValueFromStep(StepNameEnum.VALIDATION_STEP.getValue(),
							SharedAccountResponseType.CONSUMER_LIMIT.name());
				if(null != consumerThreshold) {
					consumer.setThreshold(String.valueOf(consumerThreshold));
				}
				fdpResponse = new FDPMetadataResponseImpl(Status.SUCCESS, true, null);
				updateConsumerInResponse(fdpResponse, consumer);
			}
		}
		return fdpResponse;
	}
}
