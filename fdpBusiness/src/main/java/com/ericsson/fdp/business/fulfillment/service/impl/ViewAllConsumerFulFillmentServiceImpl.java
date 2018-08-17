package com.ericsson.fdp.business.fulfillment.service.impl;

import java.util.ArrayList;
import java.util.List;

import javax.ejb.Stateless;
import javax.inject.Inject;

import com.ericsson.fdp.business.enums.AuxRequestParam;
import com.ericsson.fdp.business.enums.RequestMetaValuesKey;
import com.ericsson.fdp.business.enums.TLVOptions;
import com.ericsson.fdp.business.node.FDPServiceProvisioningNode;
import com.ericsson.fdp.business.request.impl.FDPMetadataResponseImpl;
import com.ericsson.fdp.business.request.impl.FulfillmentRequestImpl;
import com.ericsson.fdp.business.response.fulfillment.xml.Consumer;
import com.ericsson.fdp.business.util.ResponseUtil;
import com.ericsson.fdp.common.enums.Status;
import com.ericsson.fdp.core.exception.ExecutionFailedException;
import com.ericsson.fdp.core.request.FDPRequest;
import com.ericsson.fdp.core.request.FDPResponse;
import com.ericsson.fdp.dao.dto.sharedaccount.SharedAccountConsumerDTO;
import com.ericsson.fdp.dao.fdpadmin.FDPSharedAccountConsumerDAO;

@Stateless
public class ViewAllConsumerFulFillmentServiceImpl extends AbstractFDPFulFillmentServiceImpl{

	/** The shared account consumer dao. */
	@Inject
	private FDPSharedAccountConsumerDAO sharedAccountConsumerDAO;
	
	@Override
	protected FDPResponse executeService(FDPRequest fdpRequest, Object... additionalInformations)
			throws ExecutionFailedException {
		FDPResponse fdpResponse = new FDPMetadataResponseImpl(Status.FAILURE, true, null);
		final FulfillmentRequestImpl fulfillmentRequestImpl = (FulfillmentRequestImpl) fdpRequest;
		final FDPServiceProvisioningNode fdpServiceProvisioningNode = getNodeProductSP(fulfillmentRequestImpl);
		if (null != fdpServiceProvisioningNode
				&& null != fdpServiceProvisioningNode.getEntityIdForCache(RequestMetaValuesKey.PRODUCT)) {
			Long productId = Long.valueOf(fdpServiceProvisioningNode.getEntityIdForCache(RequestMetaValuesKey.PRODUCT));
			List<SharedAccountConsumerDTO> consumerList = getConsumersListForSharedAccount(
					fdpRequest.getSubscriberNumber(), productId);
			prepareSuccessResponse(fdpRequest, fdpResponse, consumerList);
		} else {
			fdpResponse = handleNodeNotFound(fulfillmentRequestImpl,fdpRequest);
		}
		return fdpResponse;
	}
	
	
	/**
	 * This method fetches the consumer list added with status 4(Completed) only.
	 * @param providerMobNo
	 * @param productId
	 * @return
	 */
	private List<SharedAccountConsumerDTO> getConsumersListForSharedAccount(final Long providerMobNo,
			final Long productId) {
		final List<SharedAccountConsumerDTO> consumerList = sharedAccountConsumerDAO
				.getSharedAccountConsumersByProviderMsisdnAndProductId(providerMobNo, productId);
		return consumerList;
	}
	
	/**
	 * This method prepares the list of consumers
	 * @param fdpRequest
	 * @param fdpResponse
	 * @param consumerList
	 * @throws ExecutionFailedException
	 */
	private void prepareSuccessResponse(final FDPRequest fdpRequest, final FDPResponse fdpResponse,
			final List<SharedAccountConsumerDTO> consumerList) throws ExecutionFailedException {
		List<Consumer> consumers  = null;
		FDPMetadataResponseImpl fdpMetadataResponseImpl = (FDPMetadataResponseImpl) fdpResponse;
		String responseText = "No Consumer Exists";
		int numberOfConsumer = 0;
		if(null != consumerList) {
			consumers = new ArrayList<Consumer>();
			for (final SharedAccountConsumerDTO consumer : consumerList) {
				final Consumer consumerJaxb = new Consumer();
				consumerJaxb.setMsisdn(consumer.getConsumerMsisdn());
				consumers.add(consumerJaxb);
				numberOfConsumer++;
			}
			if(null != consumers && consumers.size() >0) {
				fdpMetadataResponseImpl.putAuxiliaryRequestParameter(AuxRequestParam.CONSUMER_MSISDN_LIST, consumers);
				responseText="Total "+numberOfConsumer+" consumer found";
			} 
		} 
		
		fdpMetadataResponseImpl.setExecutionStatus(Status.SUCCESS);
		fdpMetadataResponseImpl.addResponseString(ResponseUtil.createResponseMessage(fdpRequest.getChannel(),
				responseText, TLVOptions.SESSION_TERMINATE));
	}
}
