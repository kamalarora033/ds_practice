package com.ericsson.fdp.business.sharedaccount.impl;

import java.util.HashMap;
import java.util.Map;

import javax.ejb.Stateless;
import javax.inject.Inject;

import com.ericsson.fdp.business.sharedaccount.AbstractSharedAccountService;
import com.ericsson.fdp.business.sharedaccount.constans.SharedAccountConstants;
import com.ericsson.fdp.core.exception.ExecutionFailedException;
import com.ericsson.fdp.core.request.FDPRequest;
import com.ericsson.fdp.dao.enums.SharedAccountResponseType;
import com.ericsson.fdp.dao.enums.StepNameEnum;
import com.ericsson.fdp.dao.exception.FDPConcurrencyException;
import com.ericsson.fdp.dao.fdpadmin.FDPSharedAccountConsumerDAO;
import com.ericsson.fdp.dao.fdpadmin.FDPSharedAccountGroupDAO;
import com.ericsson.fdp.dao.fdpadmin.FDPSharedAccountReqDAO;

@Stateless
public class DetachProviderServiceImpl extends AbstractSharedAccountService {

	@Inject
	private FDPSharedAccountGroupDAO fdpSharedAccountGroupDAO;

	@Inject
	private FDPSharedAccountConsumerDAO fdpSharedAccountConsumerDAO;

	@Inject
	private FDPSharedAccountReqDAO accountReqDAO;

	@Override
	public Map<SharedAccountResponseType, Object> executeSharedAccountService(final FDPRequest fdpRequest,
			final Object... params) throws ExecutionFailedException {

		final Long saredAccGroupId = (Long) fdpRequest.getValueFromStep(StepNameEnum.VALIDATION_STEP.getValue(),
				SharedAccountResponseType.DB_ID.name());
		final Map<SharedAccountResponseType, Object> statusDataMap = new HashMap<SharedAccountResponseType, Object>();
		try {
			fdpSharedAccountGroupDAO.updateSharedAccountGroupParent(saredAccGroupId, null, fdpRequest
					.getSubscriberNumber().toString());
			fdpSharedAccountConsumerDAO.updateConsumerParentGroup(saredAccGroupId, null, fdpRequest
					.getSubscriberNumber().toString());
			removeSharedAccountRequestForProvider(accountReqDAO, fdpRequest, params);
			fdpSharedAccountGroupDAO.delete(fdpSharedAccountGroupDAO.get(saredAccGroupId));
		} catch (final FDPConcurrencyException e) {
			throw new ExecutionFailedException("Could not update request", e);
		}
		statusDataMap.put(SharedAccountResponseType.STATUS, SharedAccountConstants.SUCCESS);
		statusDataMap.put(SharedAccountResponseType.DB_ID, saredAccGroupId);
		statusDataMap.put(SharedAccountResponseType.PROVIDER_MSISDN, fdpRequest.getSubscriberNumber());
		return statusDataMap;
	}
}
