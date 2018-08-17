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
import com.ericsson.fdp.business.response.fulfillment.xml.Provider;
import com.ericsson.fdp.business.util.ResponseUtil;
import com.ericsson.fdp.common.constants.FDPConstant;
import com.ericsson.fdp.common.enums.Status;
import com.ericsson.fdp.core.exception.ExecutionFailedException;
import com.ericsson.fdp.core.request.FDPRequest;
import com.ericsson.fdp.core.request.FDPResponse;
import com.ericsson.fdp.dao.dto.sharedaccount.SharedAccGpDTO;
import com.ericsson.fdp.dao.fdpadmin.FDPSharedAccountConsumerDAO;

@Stateless
public class ViewAllProviderFulFillmentServiceImpl extends AbstractFDPFulFillmentServiceImpl{

	/** The shared account consumer dao. */
	@Inject
	private FDPSharedAccountConsumerDAO sharedAccountConsumerDAO;
	
	@Override
	protected FDPResponse executeService(FDPRequest fdpRequest, Object... additionalInformations)
			throws ExecutionFailedException {
		FDPResponse fdpResponse = new FDPMetadataResponseImpl(Status.FAILURE, true, null);
		final FulfillmentRequestImpl fulfillmentRequestImpl = (FulfillmentRequestImpl) fdpRequest;
		Long productId  = null;
		List<SharedAccGpDTO> providerList = null;
		final FDPServiceProvisioningNode fdpServiceProvisioningNode = getNodeProductSP(fulfillmentRequestImpl);
		if (((null != fdpServiceProvisioningNode) && (null != fdpServiceProvisioningNode
				.getEntityIdForCache(RequestMetaValuesKey.PRODUCT)))
				|| FDPConstant.ALL.equals(fulfillmentRequestImpl.getRequestString())) {
			if(null != fdpServiceProvisioningNode) {
				productId = Long.valueOf(fdpServiceProvisioningNode.getEntityIdForCache(RequestMetaValuesKey.PRODUCT));
			}
			providerList = getAllProvidersByConsumerMsisdn(
					fulfillmentRequestImpl.getSubscriberNumber(), productId);
			prepareSuccessResponse(fdpRequest, fdpResponse, providerList);
		} else {
			fdpResponse = handleNodeNotFound(fulfillmentRequestImpl,fdpRequest);
		}
		
		return fdpResponse;
	}

	/**
	 * This method interacts with DAO layer and fetches the list of providers
	 * for a consumer.
	 * 
	 * @param consumerMSISDN
	 * @param providerOfferId
	 * @return
	 */
	private List<SharedAccGpDTO> getAllProvidersByConsumerMsisdn(final Long consumerMSISDN, final Long productId) {
		return sharedAccountConsumerDAO.getAllProvidersByConsumerMsisdn(consumerMSISDN, productId);
	}
	
	/**
	 * This method prepares the list of consumers
	 * @param fdpRequest
	 * @param fdpResponse
	 * @param consumerList
	 * @throws ExecutionFailedException
	 */
	private void prepareSuccessResponse(final FDPRequest fdpRequest, final FDPResponse fdpResponse,
			final List<SharedAccGpDTO> providerList) throws ExecutionFailedException {
		List<Provider> providers  = null;
		FDPMetadataResponseImpl fdpMetadataResponseImpl = (FDPMetadataResponseImpl) fdpResponse;
		String responseText = "No Provider Exists";
		int numberOfProvider = 0;
		if(null != providerList) {
			providers = new ArrayList<Provider>();
			for (final SharedAccGpDTO provider : providerList) {
				final Provider providerJaxb = new Provider();
				providerJaxb.setMsisdn(String.valueOf(provider.getGroupProviderMSISDN()));
				providerJaxb.setOfferId(String.valueOf(provider.getOfferId()));
				providers.add(providerJaxb);
				numberOfProvider++;
			}
			if(null != providers && providers.size() >0) {
				fdpMetadataResponseImpl.putAuxiliaryRequestParameter(AuxRequestParam.PROVIDER_MSISDN_LIST, providers);
				responseText="Total "+numberOfProvider+" provider found";
			} 
		} 
		
		fdpMetadataResponseImpl.setExecutionStatus(Status.SUCCESS);
		fdpMetadataResponseImpl.addResponseString(ResponseUtil.createResponseMessage(fdpRequest.getChannel(),
				responseText, TLVOptions.SESSION_TERMINATE));
	}
}
