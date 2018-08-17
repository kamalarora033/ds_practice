package com.ericsson.fdp.business.sharedaccount.impl.validation;

import java.util.HashMap;
import java.util.Map;

import javax.ejb.Stateless;
import javax.inject.Inject;

import com.ericsson.fdp.business.enums.AuxRequestParam;
import com.ericsson.fdp.business.sharedaccount.AbstractSharedAccountService;
import com.ericsson.fdp.business.util.SharedAccountUtil;
import com.ericsson.fdp.core.exception.ExecutionFailedException;
import com.ericsson.fdp.core.request.FDPRequest;
import com.ericsson.fdp.dao.dto.sharedaccount.ProductAddInfoAttributeDTO;
import com.ericsson.fdp.dao.dto.sharedaccount.SharedAccountConsumerDTO;
import com.ericsson.fdp.dao.enums.SharedAccountResponseType;
import com.ericsson.fdp.dao.fdpadmin.FDPSharedAccountConsumerDAO;

/**
 * The Class ValidateViewUsageThresholdConsumerServiceImpl.
 */
@Stateless
public class ValidateViewUsageThresholdConsumerServiceImpl extends AbstractSharedAccountService {

	/** The fdp shared account consumer dao. */
	@Inject
	private FDPSharedAccountConsumerDAO fdpSharedAccountConsumerDAO;

	@Override
	public Map<SharedAccountResponseType, Object> executeSharedAccountService(final FDPRequest fdpRequest,
			final Object... params) throws ExecutionFailedException {
		// actor is provider so consumerMSISDN is in auxRequestParam.
		final Long consumerMSISDN = Long.valueOf(fdpRequest.getAuxiliaryRequestParameter(
				AuxRequestParam.CONSUMER_MSISDN).toString());
		// actor is provider so providerMSISDN is in fdpRequest.
		final Long providerMSISDN = fdpRequest.getSubscriberNumber();
		// provider additional info for different offer id's.
		final ProductAddInfoAttributeDTO sharedAccInfo = getProviderAddInfo((Long) params[0]);

		final Map<SharedAccountResponseType, Object> statusDataMap = new HashMap<SharedAccountResponseType, Object>();
		validateProviderAndConsumer(providerMSISDN, consumerMSISDN,
				sharedAccInfo, statusDataMap);
		if (!statusDataMap.containsKey(SharedAccountResponseType.ERROR_CODE)) {
			/*statusDataMap.putAll(SharedAccountUtil.getUsageValueForConsumer(providerMSISDN, consumerMSISDN,
					sharedAccInfo.getConsumerThresholdUnit(), fdpRequest, false));*/
			statusDataMap.putAll(SharedAccountUtil.getUsageValueForAllConsumers(providerMSISDN,
					sharedAccInfo.getUsageCounterID(), sharedAccInfo.getConsumerThresholdUnit(), fdpRequest, false,sharedAccInfo,false));
		}
		return statusDataMap;
	}

}
