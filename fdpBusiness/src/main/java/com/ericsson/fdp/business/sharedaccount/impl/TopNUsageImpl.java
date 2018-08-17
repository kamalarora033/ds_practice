package com.ericsson.fdp.business.sharedaccount.impl;

import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;

import javax.ejb.Stateless;
import javax.inject.Inject;

import com.ericsson.fdp.business.enums.AuxRequestParam;
import com.ericsson.fdp.business.sharedaccount.AbstractSharedAccountService;
import com.ericsson.fdp.business.sharedaccount.constans.SharedAccountConstants;
import com.ericsson.fdp.core.exception.ExecutionFailedException;
import com.ericsson.fdp.core.request.FDPRequest;
import com.ericsson.fdp.dao.dto.sharedaccount.SharedAccountRequestDTO;
import com.ericsson.fdp.dao.enums.ConsumerRequestType;
import com.ericsson.fdp.dao.enums.ConsumerStatusEnum;
import com.ericsson.fdp.dao.enums.SharedAccountResponseType;
import com.ericsson.fdp.dao.fdpadmin.FDPSharedAccountReqDAO;

@Stateless
public class TopNUsageImpl extends AbstractSharedAccountService {

	@Inject
	private FDPSharedAccountReqDAO fdpSharedAccountReqDao;

	@Override
	public Map<SharedAccountResponseType, Object> executeSharedAccountService(final FDPRequest fdpRequest,
			final Object... params) throws ExecutionFailedException {
		// actor is provider so providerMSISDN is in fdpRequest.
		final Long providerMSISDN = fdpRequest.getSubscriberNumber();

		final Long countryCode = getCountryCode(providerMSISDN);
		final Map<SharedAccountResponseType, Object> statusDataMap = new HashMap<SharedAccountResponseType, Object>();
		statusDataMap.put(SharedAccountResponseType.PROVIDER_MSISDN, providerMSISDN);
		statusDataMap.put(SharedAccountResponseType.CONSUMER_MSISDN, providerMSISDN);
		final SharedAccountRequestDTO reqDTO = new SharedAccountRequestDTO();
		reqDTO.setAccReqNumber(getSharedAccReqNum(fdpRequest));
		reqDTO.setCircle(fdpRequest.getCircle());
		reqDTO.setChannelType(fdpRequest.getChannel().getName());
		reqDTO.setConsumerAddInfo(fdpRequest.getAuxiliaryRequestParameter(AuxRequestParam.N_VALUE).toString());
		reqDTO.setConsumerRequestType(ConsumerRequestType.TOP_N_USAGE);
		reqDTO.setConsumerStatus(ConsumerStatusEnum.ACTIVE);
		reqDTO.setCreatedBy("System");
		reqDTO.setEntityId((Long) params[0]);
		reqDTO.setExpiredOn(Calendar.getInstance());
		reqDTO.setId(null);
		reqDTO.setReciverCountryCode(countryCode);
		reqDTO.setReciverMsisdn(fdpRequest.getSubscriberNumber());
		reqDTO.setRequestId(fdpRequest.getRequestId());
		reqDTO.setSenderCountryCode(countryCode);
		reqDTO.setSenderMsisdn(fdpRequest.getSubscriberNumber());
		reqDTO.setSharedAccountReqId(null);
		fdpSharedAccountReqDao.save(reqDTO);
		statusDataMap.put(SharedAccountResponseType.STATUS, SharedAccountConstants.SUCCESS);
		return statusDataMap;
	}
}
