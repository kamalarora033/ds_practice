package com.ericsson.fdp.business.recurringservice;

import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

import javax.annotation.Resource;
import javax.ejb.Stateless;

import org.apache.commons.collections.CollectionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ericsson.fdp.business.fulfillment.service.impl.AbstractFDPFulFillmentServiceImpl;
import com.ericsson.fdp.business.transaction.TransactionSequenceGeneratorService;
import com.ericsson.fdp.business.util.RequestUtil;
import com.ericsson.fdp.common.constants.JNDILookupConstant;
import com.ericsson.fdp.common.enums.ChannelType;
import com.ericsson.fdp.core.exception.ExecutionFailedException;
import com.ericsson.fdp.core.request.FDPRequest;
import com.ericsson.fdp.core.request.FDPResponse;
import com.ericsson.fdp.core.request.impl.FDPRequestImpl;
import com.ericsson.fdp.dao.enums.FDPServiceProvSubType;
@Stateless
public class PAMDeprovisioningForCCWebServiceImpl extends AbstractFDPFulFillmentServiceImpl implements
		DeprovisioningForCCWebService {

	/** The PAMDeprovisioningForCCWebServiceImpl LOGGER. */
	private static final Logger LOGGER = LoggerFactory.getLogger(PAMDeprovisioningForCCWebServiceImpl.class);

	@Resource(lookup = JNDILookupConstant.TRANSACTION_ID_GENERATOR_SERVICE)
	private TransactionSequenceGeneratorService generatorService;

	@Override
	public String executeDeprovisioning(final List<Long> productIds, final String msisdn) throws ExecutionFailedException {
		LOGGER.info("Inside executeDeprovisioning of PAMDeprovisioningForCCWebServiceImpl with productIds and msisdn {} and {} :",productIds,msisdn);
		FDPResponse responseOfExecution = null;
		FDPRequestImpl fdpRequest = RequestUtil.createFDPRequest(msisdn, ChannelType.WEB);
		final Long transactionId = generateTransactionId();
		fdpRequest.setRequestId("ChannelType.WEB" +"_" + transactionId + "_"
				+ ThreadLocalRandom.current().nextLong());
		fdpRequest.setOriginTransactionID(transactionId);
		if (CollectionUtils.isNotEmpty(productIds)) {
			for (Long productId : productIds) {
				responseOfExecution = executeService(fdpRequest, productId);
			}
		}
		LOGGER.info("Exiting from executeDeprovisioning of PAMDeprovisioningForCCWebServiceImpl with response",responseOfExecution);
		return responseOfExecution.getExecutionStatus().getStatusText();
	}

	/**
	 * This method is used to generate the transaction id to be used
	 *
	 * @return the transaction id.
	 */
	private Long generateTransactionId() {
		return generatorService.generateTransactionId();
	}

	@Override
	protected FDPResponse executeService(final FDPRequest fdpRequest, final Object... additionalInformations)
			throws ExecutionFailedException {
		Long productIdLong = null;
		FDPResponse fdpResponse = null;
		if (null != additionalInformations) {
			if (additionalInformations[0] instanceof Long) {
				productIdLong = (Long) additionalInformations[0];
			}
		}
		updateSPInRequestForProductId(productIdLong, FDPServiceProvSubType.PAM_DEPROVISION_PRODUCT, fdpRequest);
		fdpResponse = executeSP(fdpRequest);
		return fdpResponse;
	}

}
