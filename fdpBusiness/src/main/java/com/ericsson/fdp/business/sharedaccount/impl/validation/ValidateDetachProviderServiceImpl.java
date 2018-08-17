package com.ericsson.fdp.business.sharedaccount.impl.validation;

import java.util.HashMap;
import java.util.Map;

import javax.ejb.Stateless;
import javax.inject.Inject;

import com.ericsson.fdp.business.sharedaccount.AbstractSharedAccountService;
import com.ericsson.fdp.business.sharedaccount.constans.SharedAccountConstants;
import com.ericsson.fdp.core.exception.ExecutionFailedException;
import com.ericsson.fdp.core.request.FDPRequest;
import com.ericsson.fdp.dao.dto.sharedaccount.SharedAccGpDTO;
import com.ericsson.fdp.dao.enums.SharedAccountResponseType;
import com.ericsson.fdp.dao.fdpadmin.FDPSharedAccountGroupDAO;

@Stateless
public class ValidateDetachProviderServiceImpl extends AbstractSharedAccountService {

	@Inject
	private FDPSharedAccountGroupDAO fdpSharedAccountGroupDAO;

	@Override
	public Map<SharedAccountResponseType, Object> executeSharedAccountService(final FDPRequest fdpRequest,
			final Object... params) throws ExecutionFailedException {

		final Map<SharedAccountResponseType, Object> statusDataMap = new HashMap<SharedAccountResponseType, Object>();
		final SharedAccGpDTO shrAccGrpDto = validateProvider(fdpRequest.getSubscriberNumber(),
				getProviderAddInfo((Long) params[0]), statusDataMap);
		if (shrAccGrpDto != null) {
			statusDataMap.put(SharedAccountResponseType.STATUS, SharedAccountConstants.SUCCESS);
			statusDataMap.put(SharedAccountResponseType.PROVIDER_MSISDN, shrAccGrpDto.getGroupProviderMSISDN());
			statusDataMap.put(SharedAccountResponseType.DB_ID, shrAccGrpDto.getSharedAccID());

		}
		return statusDataMap;
	}

}
