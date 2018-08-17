package com.ericsson.fdp.business.sharedaccount.impl;

import java.text.ParseException;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;

import javax.annotation.Resource;
import javax.ejb.Stateless;
import javax.inject.Inject;

import com.ericsson.fdp.business.sharedaccount.AbstractSharedAccountService;
import com.ericsson.fdp.business.sharedaccount.constans.SharedAccountConstants;
import com.ericsson.fdp.common.constants.FDPConstant;
import com.ericsson.fdp.common.constants.JNDILookupConstant;
import com.ericsson.fdp.common.util.DateUtil;
import com.ericsson.fdp.core.dsm.framework.service.FDPCache;
import com.ericsson.fdp.core.exception.ExecutionFailedException;
import com.ericsson.fdp.core.request.FDPRequest;
import com.ericsson.fdp.core.serviceprovisioning.bean.FDPAppBag;
import com.ericsson.fdp.dao.dto.sharedaccount.ProductAddInfoAttributeDTO;
import com.ericsson.fdp.dao.dto.sharedaccount.SharedAccountConsumerDTO;
import com.ericsson.fdp.dao.entity.FDPSharedAccountReq;
import com.ericsson.fdp.dao.enums.ConsumerStatusEnum;
import com.ericsson.fdp.dao.enums.ConsumerUpgradeType;
import com.ericsson.fdp.dao.enums.SharedAccountResponseType;
import com.ericsson.fdp.dao.enums.StepNameEnum;
import com.ericsson.fdp.dao.exception.FDPConcurrencyException;
import com.ericsson.fdp.dao.fdpadmin.FDPSharedAccountConsumerDAO;
import com.ericsson.fdp.dao.fdpadmin.FDPSharedAccountGroupDAO;
import com.ericsson.fdp.dao.fdpadmin.FDPSharedAccountReqDAO;
import com.ericsson.fdp.dao.fdpadmin.FDPWebUserProductDAO;

@Stateless
public class AcceptConsumerServiceImpl extends AbstractSharedAccountService {

	@Inject
	private FDPSharedAccountReqDAO fdpSharedAccountReqDao;

	@Inject
	private FDPWebUserProductDAO fdpWebUserProductDao;

	@Inject
	private FDPSharedAccountGroupDAO fdpSharedAccountGroupDao;

	@Inject
	private FDPSharedAccountConsumerDAO fdpSharedAccountConsumerDAO;

	/** The application cache. */
	@Resource(lookup = JNDILookupConstant.APPLICATION_CONFIG_CACHE_JNDI_NAME)
	private FDPCache<FDPAppBag, Object> applicationConfigCache;

	@Override
	public Map<SharedAccountResponseType, Object> executeSharedAccountService(final FDPRequest fdpRequest,
			final Object... params) throws ExecutionFailedException {

		final Map<SharedAccountResponseType, Object> statusDataMap = new HashMap<SharedAccountResponseType, Object>();
		final Long parentGroupId = (Long) fdpRequest.getValueFromStep(StepNameEnum.VALIDATION_STEP.getValue(),
				SharedAccountResponseType.PARENT_ID.name());
		final SharedAccountConsumerDTO acceptConsumerDTO = getAcceptConsumerDto(parentGroupId, fdpRequest,
				(Long) params[0]);
		final Long shareAccConsumerId = fdpSharedAccountConsumerDAO.saveConsumer(acceptConsumerDTO);
		FDPSharedAccountReq shareAccGroupId;
		try {
			shareAccGroupId = fdpSharedAccountReqDao.updateSharedRequestStatus((Long) fdpRequest.getValueFromStep(
					StepNameEnum.VALIDATION_STEP.getValue(), SharedAccountResponseType.DB_ID.name()),
					ConsumerStatusEnum.COMPLETED, fdpRequest.getSubscriberNumber().toString());
		} catch (final FDPConcurrencyException e) {
			throw new ExecutionFailedException("Could not update request", e);
		}
		statusDataMap.put(SharedAccountResponseType.STATUS, SharedAccountConstants.SUCCESS);
		statusDataMap.put(SharedAccountResponseType.DB_ID, shareAccConsumerId);
		statusDataMap.put(SharedAccountResponseType.PARENT_ID, shareAccGroupId.getSharedAccountReqId());
		statusDataMap.put(SharedAccountResponseType.PROVIDER_MSISDN, fdpRequest.getSubscriberNumber());
		statusDataMap.put(SharedAccountResponseType.CONSUMER_MSISDN, fdpRequest.getValueFromStep(
				StepNameEnum.VALIDATION_STEP.getValue(), SharedAccountResponseType.PROVIDER_MSISDN.name()));

		return statusDataMap;
	}

	private SharedAccountConsumerDTO getAcceptConsumerDto(final Long parentGroupId, final FDPRequest fdpRequest,
			final Long productId) {

		final ProductAddInfoAttributeDTO proAddInfoDto = getProviderAddInfo(productId);
		final SharedAccountConsumerDTO acceptConsumerDTO = new SharedAccountConsumerDTO();
		acceptConsumerDTO.setConsumerMsisdn(fdpRequest.getSubscriberNumber().toString());
		acceptConsumerDTO.setConsumerName(fdpRequest.getSubscriberNumber().toString());
		Calendar currentDate = Calendar.getInstance();
		try {
			currentDate = DateUtil.getDateTimeFromFDPDateTimeFormat(FDPConstant.DATE_MAX);
		} catch (final ParseException e) {
			currentDate.add(Calendar.YEAR, new Integer(100));
		}
		acceptConsumerDTO.setUpgradeExpiredDate(currentDate);
		acceptConsumerDTO.setUpgradeType(ConsumerUpgradeType.PERMANENT);
		acceptConsumerDTO.setProviderGroupId(parentGroupId);
		acceptConsumerDTO.setOfferId(proAddInfoDto.getProviderOfferIdMapping());
		acceptConsumerDTO.setConsumerNewLimit(Integer.parseInt(proAddInfoDto.getConsumerLimit().toString()));
		acceptConsumerDTO.setConsumerThresholdUnit(proAddInfoDto.getConsumerThresholdUnit());
		final String consumerName = fdpRequest.getValueFromStep(StepNameEnum.VALIDATION_STEP.getValue(),
				SharedAccountResponseType.CONSUMER_NAME.name()).toString();
		acceptConsumerDTO.setConsumerName(consumerName == null ? fdpRequest.getSubscriberNumber().toString()
				: consumerName);
		acceptConsumerDTO.setDefaultThresholdCounterId(proAddInfoDto.getCommonUsageThresholdCounterID());
		return acceptConsumerDTO;
	}
}
