package com.ericsson.fdp.business.fulfillment.service.impl;

import java.util.Arrays;

import javax.ejb.Stateless;

import com.ericsson.fdp.business.enums.AuxRequestParam;
import com.ericsson.fdp.business.enums.RequestMetaValuesKey;
import com.ericsson.fdp.business.node.FDPServiceProvisioningNode;
import com.ericsson.fdp.business.product.Product;
import com.ericsson.fdp.business.request.impl.FulfillmentRequestImpl;
import com.ericsson.fdp.business.util.SharedAccountUtil;
import com.ericsson.fdp.core.exception.ExecutionFailedException;
import com.ericsson.fdp.core.request.FDPRequest;
import com.ericsson.fdp.core.request.FDPResponse;
import com.ericsson.fdp.dao.enums.FDPServiceProvSubType;
import com.ericsson.fdp.dao.enums.ProductAdditionalInfoEnum;

@Stateless
public class AcceptConsumerFulFillmentServiceImpl extends AbstractFDPFulFillmentServiceImpl {

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
			final String offerId = getOfferId(fdpRequest, productId);
			updateSPInRequestForProductId(productIdLong, FDPServiceProvSubType.SHARED_ACCOUNT_ACCEPT_CONSUMER,
					fulfillmentRequestImpl);
			nullCheckForSharedRID(fulfillmentRequestImpl);
			fulfillmentRequestImpl.putAuxiliaryRequestParameter(AuxRequestParam.CONSUMER_MSISDN,
					fulfillmentRequestImpl.getConsumerMsisdn());
			fulfillmentRequestImpl.putAuxiliaryRequestParameter(AuxRequestParam.OFFER_ID, offerId);
			fulfillmentRequestImpl.putAuxiliaryRequestParameter(AuxRequestParam.CURRENT_SHARED_ACCOUNT_VALUE,
					Arrays.asList(String.valueOf(fulfillmentRequestImpl.getDbId())));
			fulfillmentRequestImpl.putAuxiliaryRequestParameter(AuxRequestParam.VALID_ID,
					fulfillmentRequestImpl.getDbId());
			fdpResponse = executeSP(fulfillmentRequestImpl);
		}
		return fdpResponse;
	}

	/**
	 * This method offerId associated with Product.
	 * 
	 * @param fdpRequest
	 * @param productId
	 * @return
	 * @throws ExecutionFailedException
	 */
	private String getOfferId(final FDPRequest fdpRequest, final String productId) throws ExecutionFailedException {
		String offerID = null;
		Product product = SharedAccountUtil.getProduct(fdpRequest.getCircle(), productId);
		if (null == product) {
			throw new ExecutionFailedException("Unable to find Product for ProductId:" + productId);
		}
		offerID = product.getAdditionalInfo(ProductAdditionalInfoEnum.SHARED_ACC_OFFER_ID_MAPPING);

		if (null == offerID) {
			throw new ExecutionFailedException("OfferID not configured with productId:" + productId);
		}
		return offerID;
	}
	
	/**
	 * This method checks the sharedRID null in request.
	 * This check is introduced because of AUTO approval feature during ADD service.
	 * 
	 * @param fulfillmentRequestImpl
	 * @throws ExecutionFailedException
	 */
	private void nullCheckForSharedRID(final FulfillmentRequestImpl fulfillmentRequestImpl) throws ExecutionFailedException {
		if(null == fulfillmentRequestImpl.getDbId()) {
			throw new ExecutionFailedException("Found sharedRID NULL in request.");
		}
	}

}
