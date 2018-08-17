package com.ericsson.fdp.business.sharedaccount.impl.validation;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.Resource;
import javax.ejb.Stateless;
import javax.inject.Inject;

import com.ericsson.fdp.business.enums.AuxRequestParam;
import com.ericsson.fdp.business.sharedaccount.AbstractSharedAccountService;
import com.ericsson.fdp.business.sharedaccount.constans.SharedAccountConstants;
import com.ericsson.fdp.common.constants.FDPConstant;
import com.ericsson.fdp.common.constants.JNDILookupConstant;
import com.ericsson.fdp.core.dsm.framework.service.FDPCache;
import com.ericsson.fdp.core.exception.ExecutionFailedException;
import com.ericsson.fdp.core.request.FDPRequest;
import com.ericsson.fdp.core.serviceprovisioning.bean.FDPAppBag;
import com.ericsson.fdp.dao.dto.sharedaccount.ProductAddInfoAttributeDTO;
import com.ericsson.fdp.dao.dto.sharedaccount.SharedAccGpDTO;
import com.ericsson.fdp.dao.dto.sharedaccount.SharedAccountConsumerDTO;
import com.ericsson.fdp.dao.dto.sharedaccount.SharedAccountRequestDTO;
import com.ericsson.fdp.dao.enums.ConsumerStatusEnum;
import com.ericsson.fdp.dao.enums.SharedAccountErrorCodeEnum;
import com.ericsson.fdp.dao.enums.SharedAccountGroupStatus;
import com.ericsson.fdp.dao.enums.SharedAccountResponseType;
import com.ericsson.fdp.dao.fdpadmin.FDPSharedAccountConsumerDAO;
import com.ericsson.fdp.dao.fdpadmin.FDPSharedAccountGroupDAO;
import com.ericsson.fdp.dao.fdpadmin.FDPSharedAccountReqDAO;

@Stateless
public class ValidateAcceptConsumerServiceImpl extends AbstractSharedAccountService {

	@Inject
	private FDPSharedAccountReqDAO fdpSharedAccountReqDao;

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

		@SuppressWarnings("unchecked")
		final SharedAccountRequestDTO consumerDto = fdpSharedAccountReqDao.getAddConsumerDtoByAccReqNo(
				new Long(((List<String>) fdpRequest
						.getAuxiliaryRequestParameter(AuxRequestParam.CURRENT_SHARED_ACCOUNT_VALUE)).get(0)),
				fdpRequest.getSubscriberNumber(), ConsumerStatusEnum.PENDING.getStatusCode(), fdpRequest.getCircle()
						.getCircleId());
		SharedAccountErrorCodeEnum errorCode = null;
		SharedAccGpDTO parentGroup = null;
		final Map<SharedAccountResponseType, Object> statusDataMap = new HashMap<SharedAccountResponseType, Object>();
		if (consumerDto != null) {
			final ProductAddInfoAttributeDTO proAddInfoDto = getProviderAddInfo((Long) params[0]);
			final SharedAccountConsumerDTO accConDto = fdpSharedAccountConsumerDAO.getConsumer(
					fdpRequest.getSubscriberNumber(), proAddInfoDto.getProviderOfferIdMapping());
			if (accConDto == null) {

				final List<SharedAccGpDTO> acceptConsumerList = fdpSharedAccountGroupDao.getSharedAccGroup(
						consumerDto.getSenderMsisdn(), proAddInfoDto.getShrAccOfferId(),
						SharedAccountGroupStatus.ACTIVE);
				if (acceptConsumerList == null || acceptConsumerList.isEmpty() || acceptConsumerList.size() > 1) {
					errorCode = SharedAccountErrorCodeEnum.AMBIGUOUS_PROVIDER;
				} else {
					parentGroup = acceptConsumerList.get(0);
				}
			} else {
				errorCode = SharedAccountErrorCodeEnum.CONSUMER_ALREADY_EXIST;
			}

		} else {
			errorCode = SharedAccountErrorCodeEnum.ACCOUNT_REQ_NO_NOT_EXIST;
		}
		if (errorCode != null) {
			statusDataMap.put(SharedAccountResponseType.STATUS, SharedAccountConstants.FAILURE);
			statusDataMap.put(SharedAccountResponseType.CONSUMER_MSISDN, fdpRequest.getSubscriberNumber());
			if (consumerDto != null) {
				statusDataMap.put(SharedAccountResponseType.PROVIDER_MSISDN, consumerDto.getSenderMsisdn());
			}
			statusDataMap.put(SharedAccountResponseType.ERROR_CODE, errorCode.getErrorCode().toString());
			statusDataMap.put(SharedAccountResponseType.ERROR_MESSAGE, errorCode.getErrorMessage());
			statusDataMap.put(SharedAccountResponseType.ERROR_VALUE, errorCode.getErrorMessage());
			statusDataMap.put(SharedAccountResponseType.EXTERNAL_SYSTEM_TYPE, FDPConstant.FDP);

		} else {
			statusDataMap.put(SharedAccountResponseType.STATUS, SharedAccountConstants.SUCCESS);
			statusDataMap.put(SharedAccountResponseType.CONSUMER_MSISDN, fdpRequest.getSubscriberNumber());
			statusDataMap.put(SharedAccountResponseType.PROVIDER_MSISDN, consumerDto.getSenderMsisdn());
			statusDataMap.put(SharedAccountResponseType.PARENT_ID, parentGroup.getSharedAccID());
			statusDataMap.put(SharedAccountResponseType.DB_ID, consumerDto.getId());
			statusDataMap.put(SharedAccountResponseType.CONSUMER_NAME, consumerDto.getConsumerAddInfo());
		}

		return statusDataMap;
	}

}
