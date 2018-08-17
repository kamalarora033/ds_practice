package com.ericsson.fdp.business.sharedaccount.impl.validation;

import java.util.HashMap;
import java.util.Map;

import javax.ejb.Stateless;

import com.ericsson.fdp.business.enums.AuxRequestParam;
import com.ericsson.fdp.business.sharedaccount.AbstractSharedAccountService;
import com.ericsson.fdp.business.sharedaccount.constans.SharedAccountConstants;
import com.ericsson.fdp.core.exception.ExecutionFailedException;
import com.ericsson.fdp.core.request.FDPRequest;
import com.ericsson.fdp.dao.enums.SharedAccountResponseType;

/**
 * The Class ValidateTopNUsageImpl.
 */
@Stateless
public class DefaultFNFValidationImpl extends AbstractSharedAccountService {

	@Override
	public Map<SharedAccountResponseType, Object> executeSharedAccountService(final FDPRequest fdpRequest,
			final Object... params) throws ExecutionFailedException {
		// actor is provider so providerMSISDN is in fdpRequest.
		final Long providerMSISDN = fdpRequest.getSubscriberNumber();
		final Map<SharedAccountResponseType, Object> statusDataMap = new HashMap<SharedAccountResponseType, Object>();
		statusDataMap.put(SharedAccountResponseType.PROVIDER_MSISDN, providerMSISDN);
		statusDataMap.put(SharedAccountResponseType.STATUS, SharedAccountConstants.SUCCESS);
		final Long consumerMsisdn = Long.parseLong((((String) fdpRequest
				.getAuxiliaryRequestParameter(AuxRequestParam.CONSUMER_MSISDN))));
		statusDataMap.put(SharedAccountResponseType.CONSUMER_MSISDN, consumerMsisdn);
		return statusDataMap;
	}

}
