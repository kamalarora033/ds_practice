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
import com.ericsson.fdp.dao.enums.ConsumerRequestType;
import com.ericsson.fdp.dao.enums.SharedAccountErrorCodeEnum;
import com.ericsson.fdp.dao.enums.SharedAccountResponseType;
import com.ericsson.fdp.dao.fdpadmin.FDPProductAdditionalInfoDAO;
import com.ericsson.fdp.dao.fdpadmin.FDPSharedAccountConsumerDAO;
import com.ericsson.fdp.dao.fdpadmin.FDPSharedAccountReqDAO;

/**
 * The Class ValidateAddConsumerServiceImpl.
 * 
 * @author Ericsson
 */
@Stateless
public class ValidateAddConsumerServiceImpl extends AbstractSharedAccountService {

	/** The fdp shared account req dao. */
	@Inject
	private FDPSharedAccountReqDAO fdpSharedAccountReqDao;

	/** The fdp product additional info dao. */
	@Inject
	private FDPProductAdditionalInfoDAO fdpProductAdditionalInfoDAO;

	/** The fdp shared account consumer dao. */
	@Inject
	private FDPSharedAccountConsumerDAO fdpSharedAccountConsumerDAO;

	/** The application cache. */
	@Resource(lookup = JNDILookupConstant.APPLICATION_CONFIG_CACHE_JNDI_NAME)
	private FDPCache<FDPAppBag, Object> applicationConfigCache;

	@Override
	public Map<SharedAccountResponseType, Object> executeSharedAccountService(final FDPRequest fdpRequest,
			final Object... params) throws ExecutionFailedException {

		final Long productId = (Long) params[0];
		final Long consumerMsisdn = Long.parseLong((((String) fdpRequest
				.getAuxiliaryRequestParameter(AuxRequestParam.CONSUMER_MSISDN))));
		final ProductAddInfoAttributeDTO productAddInfo = getProviderAddInfo(productId);
		final List<SharedAccGpDTO> accConGroupList = getSharedAccountGroup(fdpRequest.getSubscriberNumber(),
				productAddInfo.getShrAccOfferId());
		SharedAccountErrorCodeEnum errorCode = null;
		final SharedAccountRequestDTO shrAccDto = fdpSharedAccountReqDao.getPendingConsumerRequest(productId,
				fdpRequest.getSubscriberNumber(), fdpRequest.getCircle().getCircleId(),
				ConsumerRequestType.ADD_CONSUMER, consumerMsisdn);
		if (shrAccDto == null) {
			if (accConGroupList != null && !accConGroupList.isEmpty()) {
				if (accConGroupList.size() > 1) {
					errorCode = SharedAccountErrorCodeEnum.AMBIGUOUS_CONSUMER;
				} else {
					final SharedAccGpDTO accShareDto = accConGroupList.get(0);
					final Integer totalUserForConsumerLimit = fdpSharedAccountConsumerDAO
							.getConsumerCountBySharedAccGroupId(accShareDto.getSharedAccID())
							+ fdpSharedAccountReqDao.getPendingConsumerList(productId,
									fdpRequest.getSubscriberNumber(), fdpRequest.getCircle().getCircleId(),
									ConsumerRequestType.ADD_CONSUMER).size();
					if (productAddInfo.getNoOfConsumer() > totalUserForConsumerLimit) {

						final SharedAccountConsumerDTO accShCos = fdpSharedAccountConsumerDAO.getConsumer(
								consumerMsisdn, productAddInfo.getProviderOfferIdMapping());
						if (accShCos != null) {
							errorCode = SharedAccountErrorCodeEnum.WRONG_RECIVER_MSISDN;
						}
					} else {
						errorCode = SharedAccountErrorCodeEnum.CONSUMER_LIMIT_EXCEED;
					}
				}
			} else {
				errorCode = SharedAccountErrorCodeEnum.PROVIDER_NOT_EXIST;
			}
		} else {
			errorCode = SharedAccountErrorCodeEnum.CONSUMER_REQ_ALREADY_EXIST;
		}

		final Map<SharedAccountResponseType, Object> statusDataMap = new HashMap<SharedAccountResponseType, Object>();
		if (errorCode != null) {
			statusDataMap.put(SharedAccountResponseType.STATUS, SharedAccountConstants.FAILURE);
			statusDataMap.put(SharedAccountResponseType.CONSUMER_MSISDN, consumerMsisdn);
			statusDataMap.put(SharedAccountResponseType.PROVIDER_MSISDN, fdpRequest.getSubscriberNumber());
			statusDataMap.put(SharedAccountResponseType.ERROR_CODE, errorCode.getErrorCode().toString());
			statusDataMap.put(SharedAccountResponseType.ERROR_MESSAGE, errorCode.getErrorMessage());
			statusDataMap.put(SharedAccountResponseType.ERROR_VALUE, errorCode.getErrorMessage());
			statusDataMap.put(SharedAccountResponseType.EXTERNAL_SYSTEM_TYPE, FDPConstant.FDP);
		} else {
			statusDataMap.put(SharedAccountResponseType.STATUS, SharedAccountConstants.SUCCESS);
			statusDataMap.put(SharedAccountResponseType.CONSUMER_MSISDN, consumerMsisdn);
			statusDataMap.put(SharedAccountResponseType.PROVIDER_MSISDN, fdpRequest.getSubscriberNumber());
		}

		return statusDataMap;
	}

}
