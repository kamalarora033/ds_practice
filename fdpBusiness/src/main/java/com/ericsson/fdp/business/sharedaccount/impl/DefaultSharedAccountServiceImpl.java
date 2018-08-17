package com.ericsson.fdp.business.sharedaccount.impl;

import java.util.HashMap;
import java.util.Map;

import javax.ejb.Stateless;

import com.ericsson.fdp.business.sharedaccount.AbstractSharedAccountService;
import com.ericsson.fdp.business.sharedaccount.constans.SharedAccountConstants;
import com.ericsson.fdp.core.exception.ExecutionFailedException;
import com.ericsson.fdp.core.request.FDPRequest;
import com.ericsson.fdp.dao.enums.SharedAccountResponseType;
import com.ericsson.fdp.dao.enums.StepNameEnum;

@Stateless
public class DefaultSharedAccountServiceImpl extends AbstractSharedAccountService {

	@Override
	public Map<SharedAccountResponseType, Object> executeSharedAccountService(final FDPRequest fdpRequest,
			final Object... params) throws ExecutionFailedException {

		final Map<SharedAccountResponseType, Object> statusDataMap = new HashMap<SharedAccountResponseType, Object>();
		statusDataMap.put(SharedAccountResponseType.STATUS, SharedAccountConstants.SUCCESS);
		statusDataMap.put(SharedAccountResponseType.PROVIDER_MSISDN, fdpRequest.getValueFromStep(
				StepNameEnum.VALIDATION_STEP.getValue(), SharedAccountResponseType.PROVIDER_MSISDN.name()));
		statusDataMap.put(SharedAccountResponseType.CONSUMER_MSISDN, fdpRequest.getValueFromStep(
				StepNameEnum.VALIDATION_STEP.getValue(), SharedAccountResponseType.CONSUMER_MSISDN.name()));
		return statusDataMap;
	}
}
