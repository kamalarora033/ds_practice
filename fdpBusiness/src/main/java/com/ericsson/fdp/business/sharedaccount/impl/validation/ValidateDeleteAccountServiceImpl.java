package com.ericsson.fdp.business.sharedaccount.impl.validation;

import java.util.HashMap;
import java.util.Map;

import javax.ejb.Stateless;

import com.ericsson.fdp.business.sharedaccount.AbstractSharedAccountService;
import com.ericsson.fdp.business.sharedaccount.constans.SharedAccountConstants;
import com.ericsson.fdp.core.exception.ExecutionFailedException;
import com.ericsson.fdp.core.request.FDPRequest;
import com.ericsson.fdp.dao.dto.sharedaccount.ProductAddInfoAttributeDTO;
import com.ericsson.fdp.dao.dto.sharedaccount.SharedAccGpDTO;
import com.ericsson.fdp.dao.enums.SharedAccountResponseType;

@Stateless
public class ValidateDeleteAccountServiceImpl extends AbstractSharedAccountService {

	@Override
	public Map<SharedAccountResponseType, Object> executeSharedAccountService(final FDPRequest fdpRequest,
			final Object... params) throws ExecutionFailedException {

		final ProductAddInfoAttributeDTO sharedAccInfo = getProviderAddInfo((Long) params[0]);
		final Map<SharedAccountResponseType, Object> statusDataMap = new HashMap<SharedAccountResponseType, Object>();
		final SharedAccGpDTO result = validateProvider(fdpRequest.getSubscriberNumber(), sharedAccInfo, statusDataMap);
		if (result != null) {
			statusDataMap.put(SharedAccountResponseType.STATUS, SharedAccountConstants.SUCCESS);
			statusDataMap.put(SharedAccountResponseType.DB_ID, result.getSharedAccID());
			statusDataMap.put(SharedAccountResponseType.SHARED_ACCOUNT_OFFER_ID, sharedAccInfo.getShrAccOfferId());
			statusDataMap.put(SharedAccountResponseType.PROVIDER_MSISDN, fdpRequest.getSubscriberNumber());

		}
		return statusDataMap;
	}

}
