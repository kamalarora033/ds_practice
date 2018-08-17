package com.ericsson.fdp.business.sharedaccount.impl;

import java.util.HashMap;
import java.util.Map;

import javax.ejb.Stateless;
import javax.inject.Inject;

import com.ericsson.fdp.business.enums.AuxRequestParam;
import com.ericsson.fdp.business.sharedaccount.AbstractSharedAccountService;
import com.ericsson.fdp.business.sharedaccount.constans.SharedAccountConstants;
import com.ericsson.fdp.core.exception.ExecutionFailedException;
import com.ericsson.fdp.core.request.FDPRequest;
import com.ericsson.fdp.dao.enums.SharedAccountErrorCodeEnum;
import com.ericsson.fdp.dao.enums.SharedAccountResponseType;
import com.ericsson.fdp.dao.enums.StepNameEnum;
import com.ericsson.fdp.dao.exception.FDPConcurrencyException;
import com.ericsson.fdp.dao.fdpadmin.FDPSharedAccountConsumerDAO;

@Stateless
public class DetachConsumerServiceImpl extends AbstractSharedAccountService {

	@Inject
	private FDPSharedAccountConsumerDAO fdpSharedAccountConsumerDAO;

	@Override
	public Map<SharedAccountResponseType, Object> executeSharedAccountService(final FDPRequest fdpRequest,
			final Object... params) throws ExecutionFailedException {
		// actor is consumer so providerMSISDN is in auxRequestParam.
		final Long providerMSISDN = Long.valueOf(fdpRequest.getAuxiliaryRequestParameter(
				AuxRequestParam.PROVIDER_MSISDN).toString());
		// actor is consumer so consumerMSISDN is in fdpRequest.
		final Long consumerMSISDN = fdpRequest.getSubscriberNumber();
		final Map<SharedAccountResponseType, Object> statusDataMap = new HashMap<SharedAccountResponseType, Object>();
		statusDataMap.put(SharedAccountResponseType.CONSUMER_MSISDN, consumerMSISDN);
		statusDataMap.put(SharedAccountResponseType.PROVIDER_MSISDN, providerMSISDN);
		try {
			fdpSharedAccountConsumerDAO.deleteSharedAccountConsumer((Long) fdpRequest.getValueFromStep(
					StepNameEnum.VALIDATION_STEP.getValue(), SharedAccountResponseType.DB_ID.name()));
			statusDataMap.put(SharedAccountResponseType.STATUS, SharedAccountConstants.SUCCESS);
		} catch (final FDPConcurrencyException ex) {
			throw new ExecutionFailedException(SharedAccountErrorCodeEnum.CONSUMER_NOT_EXIST.toString(), ex);
		}
		return statusDataMap;
	}

}