package com.ericsson.fdp.business.sharedaccount.impl.validation;

import java.util.HashMap;
import java.util.Map;

import javax.ejb.Stateless;

import com.ericsson.fdp.business.enums.AuxRequestParam;
import com.ericsson.fdp.business.sharedaccount.AbstractSharedAccountService;
import com.ericsson.fdp.business.sharedaccount.constans.SharedAccountConstants;
import com.ericsson.fdp.core.exception.ExecutionFailedException;
import com.ericsson.fdp.core.request.FDPRequest;
import com.ericsson.fdp.dao.dto.sharedaccount.ProductAddInfoAttributeDTO;
import com.ericsson.fdp.dao.dto.sharedaccount.SharedAccountConsumerDTO;
import com.ericsson.fdp.dao.enums.SharedAccountResponseType;

/**
 * The Class ValidateDetachConsumerServiceImpl.
 */
@Stateless
public class ValidateDetachConsumerServiceImpl extends AbstractSharedAccountService {

	@Override
	public Map<SharedAccountResponseType, Object> executeSharedAccountService(final FDPRequest fdpRequest,
			final Object... params) throws ExecutionFailedException {
		// actor is consumer so providerMSISDN is in auxRequestParam.
		final Long providerMSISDN = Long.valueOf(fdpRequest.getAuxiliaryRequestParameter(
				AuxRequestParam.PROVIDER_MSISDN).toString());
		// actor is consumer so consumerMSISDN is in fdpRequest.
		final Long consumerMSISDN = fdpRequest.getSubscriberNumber();

		// provider additional info for different offer id's.
		final ProductAddInfoAttributeDTO sharedAccInfo = getProviderAddInfo((Long) params[0]);

		final Map<SharedAccountResponseType, Object> statusDataMap = new HashMap<SharedAccountResponseType, Object>();
		final SharedAccountConsumerDTO consumerDTO = validateProviderAndConsumer(providerMSISDN, consumerMSISDN,
				sharedAccInfo, statusDataMap);
		if (!statusDataMap.containsKey(SharedAccountResponseType.ERROR_CODE)) {
			statusDataMap.put(SharedAccountResponseType.STATUS, SharedAccountConstants.SUCCESS);
			statusDataMap.put(SharedAccountResponseType.DB_ID, consumerDTO.getSharedAccountConsumerId());
		}
		return statusDataMap;
	}
}
