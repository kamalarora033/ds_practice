package com.ericsson.fdp.business.sharedaccount.impl;

import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;

import javax.ejb.Stateless;
import javax.inject.Inject;

import com.ericsson.fdp.business.sharedaccount.AbstractSharedAccountService;
import com.ericsson.fdp.business.sharedaccount.constans.SharedAccountConstants;
import com.ericsson.fdp.common.vo.FDPCircle;
import com.ericsson.fdp.core.exception.ExecutionFailedException;
import com.ericsson.fdp.core.request.FDPRequest;
import com.ericsson.fdp.dao.dto.sharedaccount.SharedAccountDTO;
import com.ericsson.fdp.dao.dto.sharedaccount.SharedAccountRequestDTO;
import com.ericsson.fdp.dao.enums.ConsumerRequestType;
import com.ericsson.fdp.dao.enums.ConsumerStatusEnum;
import com.ericsson.fdp.dao.enums.SharedAccountGroupStatus;
import com.ericsson.fdp.dao.enums.SharedAccountResponseType;
import com.ericsson.fdp.dao.enums.StepNameEnum;
import com.ericsson.fdp.dao.exception.FDPConcurrencyException;
import com.ericsson.fdp.dao.fdpadmin.FDPSharedAccountGroupDAO;
import com.ericsson.fdp.dao.fdpadmin.FDPSharedAccountReqDAO;

@Stateless
public class DeleteAccountServiceImpl extends AbstractSharedAccountService {

	@Inject
	private FDPSharedAccountGroupDAO fdpSharedAccountGroupDao;

	@Inject
	private FDPSharedAccountReqDAO fdpSharedAccountReqDao;

	@Inject
	private FDPSharedAccountReqDAO accountReqDAO;

	@Override
	public Map<SharedAccountResponseType, Object> executeSharedAccountService(final FDPRequest fdpRequest,
			final Object... params) throws ExecutionFailedException {

		final Long sharAccReqId = (Long) fdpRequest.getValueFromStep(StepNameEnum.VALIDATION_STEP.getValue(),
				SharedAccountResponseType.DB_ID.name());
		final Map<SharedAccountResponseType, Object> statusDataMap = new HashMap<SharedAccountResponseType, Object>();
		try {
			fdpSharedAccountGroupDao.changeShredAccGroupStatus(sharAccReqId, SharedAccountGroupStatus.DORMENT);
			removeSharedAccountRequestForProvider(accountReqDAO, fdpRequest, params);
			final SharedAccountRequestDTO addConsumerDto = (SharedAccountRequestDTO) getSharedAccountDTO(fdpRequest,
					sharAccReqId);
			fdpSharedAccountReqDao.save(addConsumerDto);
			statusDataMap.put(SharedAccountResponseType.STATUS, SharedAccountConstants.SUCCESS);
			statusDataMap.put(SharedAccountResponseType.PROVIDER_MSISDN, fdpRequest.getSubscriberNumber());

		} catch (final FDPConcurrencyException e) {
			throw new ExecutionFailedException("Could not update request", e);
		}
		return statusDataMap;
	}

	private SharedAccountDTO getSharedAccountDTO(final FDPRequest fdpRequest, final Long productId) {

		final SharedAccountRequestDTO addConsumerDto = new SharedAccountRequestDTO();
		addConsumerDto.setCircle(fdpRequest.getCircle());
		addConsumerDto.setConsumerRequestType(ConsumerRequestType.DELETE_ACCOUNT);
		addConsumerDto.setConsumerStatus(ConsumerStatusEnum.REJECTED);
		addConsumerDto.setEntityId(productId);
		addConsumerDto.setSenderMsisdn(fdpRequest.getSubscriberNumber());
		addConsumerDto.setSenderCountryCode(fdpRequest.getSubscriberNumberNAI());
		addConsumerDto.setRequestId(fdpRequest.getRequestId());
		addConsumerDto.setReciverMsisdn(fdpRequest.getSubscriberNumber());
		addConsumerDto.setReciverCountryCode(getCountryCode(fdpRequest.getSubscriberNumber()));
		addConsumerDto.setChannelType(fdpRequest.getChannel().getName());
		final Calendar currentDate = Calendar.getInstance();
		final FDPCircle circle = fdpRequest.getCircle();
		circle.getConfigurationKeyValueMap().get(SharedAccountConstants.EXPIRE_HOURS);
		currentDate.add(Calendar.HOUR,
				Integer.parseInt(circle.getConfigurationKeyValueMap().get(SharedAccountConstants.EXPIRE_HOURS)));
		addConsumerDto.setExpiredOn(currentDate);
		addConsumerDto.setAccReqNumber(getSharedAccReqNum(fdpRequest));
		return addConsumerDto;

	}

}
