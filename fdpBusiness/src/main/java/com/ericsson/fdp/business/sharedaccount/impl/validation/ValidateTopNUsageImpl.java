package com.ericsson.fdp.business.sharedaccount.impl.validation;

import java.util.HashMap;
import java.util.Map;

import javax.ejb.Stateless;

import com.ericsson.fdp.business.sharedaccount.AbstractSharedAccountService;
import com.ericsson.fdp.business.sharedaccount.constans.SharedAccountConstants;
import com.ericsson.fdp.core.exception.ExecutionFailedException;
import com.ericsson.fdp.core.request.FDPRequest;
import com.ericsson.fdp.dao.dto.sharedaccount.ProductAddInfoAttributeDTO;
import com.ericsson.fdp.dao.enums.SharedAccountResponseType;

/**
 * The Class ValidateTopNUsageImpl.
 */
@Stateless
public class ValidateTopNUsageImpl extends AbstractSharedAccountService {

	@Override
	public Map<SharedAccountResponseType, Object> executeSharedAccountService(final FDPRequest fdpRequest,
			final Object... params) throws ExecutionFailedException {
		// actor is provider so providerMSISDN is in fdpRequest.
		final Long providerMSISDN = fdpRequest.getSubscriberNumber();
		// provider additional info for different offer id's.
		final ProductAddInfoAttributeDTO sharedAccInfo = getProviderAddInfo((Long) params[0]);

		final Map<SharedAccountResponseType, Object> statusDataMap = new HashMap<SharedAccountResponseType, Object>();
		statusDataMap.put(SharedAccountResponseType.PROVIDER_MSISDN, providerMSISDN);
		validateProvider(providerMSISDN, sharedAccInfo, statusDataMap);
		if (!statusDataMap.containsKey(SharedAccountResponseType.ERROR_CODE)) {
			statusDataMap.put(SharedAccountResponseType.STATUS, SharedAccountConstants.SUCCESS);
			statusDataMap.put(SharedAccountResponseType.SHARED_ACCOUNT_OFFER_ID,
					sharedAccInfo.getSharedAccOfferIdMapping());
		}
		return statusDataMap;
	}

}
