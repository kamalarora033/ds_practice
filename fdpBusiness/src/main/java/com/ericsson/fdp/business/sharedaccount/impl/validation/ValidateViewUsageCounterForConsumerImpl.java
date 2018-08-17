package com.ericsson.fdp.business.sharedaccount.impl.validation;

import java.util.HashMap;
import java.util.Map;

import javax.annotation.Resource;
import javax.ejb.Stateless;

import com.ericsson.fdp.business.enums.AuxRequestParam;
import com.ericsson.fdp.business.sharedaccount.AbstractSharedAccountService;
import com.ericsson.fdp.business.transaction.TransactionSequenceGeneratorService;
import com.ericsson.fdp.business.util.SharedAccountUtil;
import com.ericsson.fdp.common.constants.JNDILookupConstant;
import com.ericsson.fdp.core.exception.ExecutionFailedException;
import com.ericsson.fdp.core.request.FDPRequest;
import com.ericsson.fdp.dao.dto.sharedaccount.ProductAddInfoAttributeDTO;
import com.ericsson.fdp.dao.enums.SharedAccountResponseType;

/**
 * The Class ValidateViewUsageCounterForConsumerImpl.
 */
@Stateless
public class ValidateViewUsageCounterForConsumerImpl extends AbstractSharedAccountService {

/*	@Inject
	private TransactionSequenceDAO transactionSequenceDAO;
*/	
	@Resource(lookup = JNDILookupConstant.TRANSACTION_ID_GENERATOR_SERVICE)
	private TransactionSequenceGeneratorService generatorService;

	@Override
	public Map<SharedAccountResponseType, Object> executeSharedAccountService(final FDPRequest fdpRequest,
			final Object... params) throws ExecutionFailedException {
		//System.out.println("In ValidateViewUsageCounterForConsumerImpl method executeSharedAccountService");
		// actor is provider so consumerMSISDN is in auxRequestParam.
		final Long consumerMSISDN = Long.valueOf(fdpRequest.getAuxiliaryRequestParameter(
				AuxRequestParam.CONSUMER_MSISDN).toString());
		//System.out.println("consumerMSISDN  = " + consumerMSISDN);
		// actor is provider so providerMSISDN is in fdpRequest.
		final Long providerMSISDN = fdpRequest.getSubscriberNumber();
		// provider additional info for different offer id's.
		final ProductAddInfoAttributeDTO sharedAccInfo = getProviderAddInfo((Long) params[0]);

		final Map<SharedAccountResponseType, Object> statusDataMap = new HashMap<SharedAccountResponseType, Object>();
		validateProviderAndConsumer(providerMSISDN, consumerMSISDN, sharedAccInfo, statusDataMap);
		if (!statusDataMap.containsKey(SharedAccountResponseType.ERROR_CODE)) {
			/*statusDataMap.putAll(SharedAccountUtil.getUsageValueForConsumer(providerMSISDN, consumerMSISDN,
					sharedAccInfo.getConsumerThresholdUnit(), fdpRequest, true));*/
			statusDataMap.putAll(SharedAccountUtil.getUsageValueForAllConsumers(providerMSISDN,
					sharedAccInfo.getUsageCounterID(), sharedAccInfo.getConsumerThresholdUnit(), fdpRequest, true,sharedAccInfo,false));
		}
		return statusDataMap;
	}

	/**
	 * This method is used to generate the transaction id to be used.
	 * 
	 * @return the transaction id.
	 */
	private Long generateTransactionId() {
		return generatorService.generateTransactionId();
	}

}
