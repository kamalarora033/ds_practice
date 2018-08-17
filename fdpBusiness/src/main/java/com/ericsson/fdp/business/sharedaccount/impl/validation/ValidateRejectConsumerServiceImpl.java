package com.ericsson.fdp.business.sharedaccount.impl.validation;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.ejb.Stateless;
import javax.inject.Inject;

import com.ericsson.fdp.business.enums.AuxRequestParam;
import com.ericsson.fdp.business.sharedaccount.AbstractSharedAccountService;
import com.ericsson.fdp.business.sharedaccount.constans.SharedAccountConstants;
import com.ericsson.fdp.core.exception.ExecutionFailedException;
import com.ericsson.fdp.core.request.FDPRequest;
import com.ericsson.fdp.dao.dto.sharedaccount.SharedAccountRequestDTO;
import com.ericsson.fdp.dao.enums.ConsumerStatusEnum;
import com.ericsson.fdp.dao.enums.SharedAccountErrorCodeEnum;
import com.ericsson.fdp.dao.enums.SharedAccountResponseType;
import com.ericsson.fdp.dao.fdpadmin.FDPSharedAccountReqDAO;

@Stateless
public class ValidateRejectConsumerServiceImpl extends AbstractSharedAccountService {

	@Inject
	private FDPSharedAccountReqDAO fdpSharedAccountReqDao;

	@SuppressWarnings("unchecked")
	@Override
	public Map<SharedAccountResponseType, Object> executeSharedAccountService(final FDPRequest fdpRequest,
			final Object... params) throws ExecutionFailedException {

		final SharedAccountRequestDTO consumerDto = fdpSharedAccountReqDao.getAddConsumerDtoByAccReqNo(
				new Long(((List<String>) fdpRequest
						.getAuxiliaryRequestParameter(AuxRequestParam.CURRENT_SHARED_ACCOUNT_VALUE)).get(0)),
				fdpRequest.getSubscriberNumber(), ConsumerStatusEnum.PENDING.getStatusCode(), fdpRequest.getCircle()
						.getCircleId());
		final Map<SharedAccountResponseType, Object> statusDataMap = new HashMap<SharedAccountResponseType, Object>();
		if (consumerDto != null) {
			statusDataMap.put(SharedAccountResponseType.STATUS, SharedAccountConstants.SUCCESS);
			statusDataMap.put(SharedAccountResponseType.CONSUMER_MSISDN, fdpRequest.getSubscriberNumber());
			statusDataMap.put(SharedAccountResponseType.PROVIDER_MSISDN, consumerDto.getSenderMsisdn());
			statusDataMap.put(SharedAccountResponseType.DB_ID, consumerDto.getAccReqNumber());
			statusDataMap.put(SharedAccountResponseType.PARENT_ID, consumerDto.getId());

		} else {

			statusDataMap.put(SharedAccountResponseType.STATUS, SharedAccountConstants.FAILURE);
			statusDataMap.put(SharedAccountResponseType.CONSUMER_MSISDN, fdpRequest.getSubscriberNumber());
			// statusDataMap.put(SharedAccountResponseType.PROVIDER_MSISDN,
			// Long.parseLong(((List<String>)fdpRequest.getAuxiliaryRequestParameter(AuxRequestParam.PROVIDER_MSISDN)).get(0)));
			statusDataMap.put(SharedAccountResponseType.ERROR_CODE, SharedAccountErrorCodeEnum.CONSUMER_NOT_EXIST.getErrorCode().toString());
			statusDataMap.put(SharedAccountResponseType.ERROR_MESSAGE,
					SharedAccountErrorCodeEnum.CONSUMER_NOT_EXIST.getErrorMessage());

		}
		return statusDataMap;
	}

}
