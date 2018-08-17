package com.ericsson.fdp.business.sharedaccount.impl.validation;

import java.util.HashMap;
import java.util.Map;

import javax.ejb.Stateless;
import javax.inject.Inject;

import com.ericsson.fdp.business.enums.AuxRequestParam;
import com.ericsson.fdp.business.sharedaccount.AbstractSharedAccountService;
import com.ericsson.fdp.business.sharedaccount.constans.SharedAccountConstants;
import com.ericsson.fdp.core.exception.ExecutionFailedException;
import com.ericsson.fdp.core.request.FDPRequest;
import com.ericsson.fdp.dao.dto.sharedaccount.ProductAddInfoAttributeDTO;
import com.ericsson.fdp.dao.dto.sharedaccount.SharedAccGpDTO;
import com.ericsson.fdp.dao.enums.ConsumerRequestType;
import com.ericsson.fdp.dao.enums.SharedAccountErrorCodeEnum;
import com.ericsson.fdp.dao.enums.SharedAccountResponseType;
import com.ericsson.fdp.dao.fdpadmin.FDPSharedAccountConsumerDAO;
import com.ericsson.fdp.dao.fdpadmin.FDPSharedAccountReqDAO;

@Stateless
public class ValidateAddProviderServiceImpl extends AbstractSharedAccountService {

	@Inject
	private FDPSharedAccountReqDAO fdpSharedAccountReqDao;

	@Inject
	private FDPSharedAccountConsumerDAO fdpSharedAccountConsumerDAO;

	@Override
	public Map<SharedAccountResponseType, Object> executeSharedAccountService(final FDPRequest fdpRequest,
			final Object... params) throws ExecutionFailedException {
		// actor is provider so newProviderMSISDN is in auxRequestParam.
		final Long newProviderMSISDN = Long.parseLong(fdpRequest.getAuxiliaryRequestParameter(
				AuxRequestParam.PROVIDER_MSISDN).toString());
		// actor is provider so providerMSISDN is in fdpRequest.
		final Long providerMSISDN = fdpRequest.getSubscriberNumber();
		final Long productID = (Long) params[0];
		// provider additional info for different offer id's.
		final ProductAddInfoAttributeDTO sharedAccInfo = getProviderAddInfo(productID);

		final Map<SharedAccountResponseType, Object> statusDataMap = new HashMap<SharedAccountResponseType, Object>();
		SharedAccountErrorCodeEnum errorCode = null;
		final SharedAccGpDTO providerDTO = validateProvider(providerMSISDN, sharedAccInfo, statusDataMap);
		if (!statusDataMap.containsKey(SharedAccountResponseType.ERROR_CODE)) {
			if (isExistingProvider(providerMSISDN, sharedAccInfo.getSharedAccOfferIdMapping())) {
				errorCode = SharedAccountErrorCodeEnum.PROVIDER_ALREADY_EXIST;
			} else {
				if (isExistingConsumer(newProviderMSISDN, sharedAccInfo.getProviderOfferIdMapping())) {
					errorCode = SharedAccountErrorCodeEnum.CONSUMER_ALREADY_EXIST;
				} else { // row count+requests count must be less than equal to
							// total product limit
					final Integer totalUserForConsumerLimit = fdpSharedAccountConsumerDAO
							.getConsumerCountBySharedAccGroupId(providerDTO.getSharedAccID())
							+ fdpSharedAccountReqDao.getPendingConsumerList(productID, providerMSISDN,
									fdpRequest.getCircle().getCircleId(), ConsumerRequestType.ADD_CONSUMER).size();
					if (totalUserForConsumerLimit > sharedAccInfo.getNoOfConsumer()) {
						errorCode = SharedAccountErrorCodeEnum.CONSUMER_LIMIT_EXCEED;
					}
				}
			}
			statusDataMap.put(SharedAccountResponseType.CONSUMER_MSISDN, newProviderMSISDN);
			statusDataMap.put(SharedAccountResponseType.PROVIDER_MSISDN, providerMSISDN);
			if (errorCode != null) {
				statusDataMap.put(SharedAccountResponseType.STATUS, SharedAccountConstants.FAILURE);
				statusDataMap.put(SharedAccountResponseType.ERROR_CODE, errorCode.getErrorCode().toString());
				statusDataMap.put(SharedAccountResponseType.ERROR_MESSAGE, errorCode.getErrorMessage());
			} else {
				statusDataMap.put(SharedAccountResponseType.STATUS, SharedAccountConstants.SUCCESS);
				statusDataMap.put(SharedAccountResponseType.DB_ID, providerDTO.getSharedAccID());
				statusDataMap.put(SharedAccountResponseType.WEB_PRODUCT_ID, providerDTO.getWebProductId());
			}
		}
		return statusDataMap;
	}

}
