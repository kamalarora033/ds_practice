package com.ericsson.fdp.business.sharedaccount;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.annotation.Resource;
import javax.inject.Inject;

import com.ericsson.fdp.business.sharedaccount.constans.SharedAccountConstants;
import com.ericsson.fdp.business.util.SharedAccountUtil;
import com.ericsson.fdp.common.constants.FDPConstant;
import com.ericsson.fdp.common.constants.JNDILookupConstant;
import com.ericsson.fdp.core.dsm.framework.service.FDPCache;
import com.ericsson.fdp.core.request.FDPRequest;
import com.ericsson.fdp.core.serviceprovisioning.bean.FDPAppBag;
import com.ericsson.fdp.dao.dto.sharedaccount.ProductAddInfoAttributeDTO;
import com.ericsson.fdp.dao.dto.sharedaccount.SharedAccGpDTO;
import com.ericsson.fdp.dao.dto.sharedaccount.SharedAccountConsumerDTO;
import com.ericsson.fdp.dao.dto.sharedaccount.SharedAccountRequestDTO;
import com.ericsson.fdp.dao.enums.ConsumerRequestType;
import com.ericsson.fdp.dao.enums.ConsumerStatusEnum;
import com.ericsson.fdp.dao.enums.SharedAccountErrorCodeEnum;
import com.ericsson.fdp.dao.enums.SharedAccountGroupStatus;
import com.ericsson.fdp.dao.enums.SharedAccountResponseType;
import com.ericsson.fdp.dao.exception.FDPConcurrencyException;
import com.ericsson.fdp.dao.fdpadmin.FDPProductAdditionalInfoDAO;
import com.ericsson.fdp.dao.fdpadmin.FDPSharedAccountConsumerDAO;
import com.ericsson.fdp.dao.fdpadmin.FDPSharedAccountGroupDAO;
import com.ericsson.fdp.dao.fdpadmin.FDPSharedAccountReqDAO;

/**
 * The Class AbstractSharedAccountService.
 * 
 * @author Ericsson
 * 
 */
public abstract class AbstractSharedAccountService implements SharedAccountService {

	/** The fdp shared account consumer dao. */
	@Inject
	private FDPSharedAccountConsumerDAO fdpSharedAccountConsumerDAO;

	/** The fdp shared account group dao. */
	@Inject
	private FDPSharedAccountGroupDAO fdpSharedAccountGroupDAO;

	/** The fdp product additional info dao. */
	@Inject
	private FDPProductAdditionalInfoDAO fdpProductAdditionalInfoDAO;

	/** The fdp shared account req dao. */
	@Inject
	private FDPSharedAccountReqDAO fdpSharedAccountReqDao;

	/** The application cache. */
	@Resource(lookup = JNDILookupConstant.APPLICATION_CONFIG_CACHE_JNDI_NAME)
	private FDPCache<FDPAppBag, Object> applicationConfigCache;

	/**
	 * Gets the country code.
	 * 
	 * @param msisdn
	 *            the msisdn
	 * @return the country code
	 */
	public Long getCountryCode(Long msisdn) {

		Long reciverCountryCode;
		if (msisdn.toString().length() != 12) {
			reciverCountryCode = 0l;
		} else {
			reciverCountryCode = new Long(msisdn.toString().substring(0, 2));
		}

		return reciverCountryCode;
	}

	/**
	 * Gets the shared account group.
	 * 
	 * @param groupProviderMsisdn
	 *            the group provider msisdn
	 * @param offerId
	 *            the offer id
	 * @return the shared account group
	 */
	public List<SharedAccGpDTO> getSharedAccountGroup(Long groupProviderMsisdn, Long offerId) {

		List<SharedAccGpDTO> accConGroupList = fdpSharedAccountGroupDAO.getSharedAccGroup(groupProviderMsisdn, offerId,
				SharedAccountGroupStatus.ACTIVE);

		return accConGroupList;
	}

	/**
	 * Gets the provider add info.
	 * 
	 * @param productId
	 *            the product id
	 * @return the provider add info
	 */
	public ProductAddInfoAttributeDTO getProviderAddInfo(Long productId) {
		Map<Integer, String> productInfoValueMap = fdpProductAdditionalInfoDAO
				.getProductAdditionalInfoMapById(productId);
		return SharedAccountUtil.getProductAdditionalInfo(productInfoValueMap);
		
	}

	/**
	 * Validate provider and consumer. This method will update statusDataMap
	 * with consumer and provider msisdn. if validation failed then set status
	 * and error_code in statusDataMap to failed and error_code respectively.
	 * 
	 * @param providerMSISDN
	 *            the provider msisdn
	 * @param consumerMSISDN
	 *            the consumer msisdn
	 * @param sharedAccInfo
	 *            the shared acc info
	 * @param statusDataMap
	 *            the status data map
	 * @return the shared account consumer dto if valid consumer otherwise null.
	 */
	public SharedAccountConsumerDTO validateProviderAndConsumer(Long providerMSISDN, Long consumerMSISDN,
			ProductAddInfoAttributeDTO sharedAccInfo, Map<SharedAccountResponseType, Object> statusDataMap) {
		SharedAccountConsumerDTO result = null;
		statusDataMap.put(SharedAccountResponseType.CONSUMER_MSISDN, consumerMSISDN);
		statusDataMap.put(SharedAccountResponseType.PROVIDER_MSISDN, providerMSISDN);
		SharedAccGpDTO sharedAccountGroup = validateProvider(providerMSISDN, sharedAccInfo, statusDataMap);
		if (sharedAccountGroup != null) {
			result = validateConsumer(consumerMSISDN, sharedAccountGroup.getSharedAccID(), sharedAccInfo, statusDataMap);
		}
		return result;
	}

	/**
	 * Validate provider and if validation failed then set status and error_code
	 * in statusDataMap to failed and error_code respectively.
	 * 
	 * @param providerMSISDN
	 *            the provider msisdn
	 * @param sharedAccInfo
	 *            the shared acc info
	 * @param statusDataMap
	 *            the status data map
	 * @return the db Id of the valid provider shared account group otherwise
	 *         null.
	 */
	public SharedAccGpDTO validateProvider(Long providerMSISDN, ProductAddInfoAttributeDTO sharedAccInfo,
			Map<SharedAccountResponseType, Object> statusDataMap) {
		SharedAccGpDTO result;
		/* getProviderList */
		List<SharedAccGpDTO> sharedAccList = getSharedAccountGroup(providerMSISDN, sharedAccInfo.getShrAccOfferId());

		/* Provider Validation */
		if (sharedAccList == null || sharedAccList.isEmpty()) { // no Provider
			statusDataMap.put(SharedAccountResponseType.STATUS, SharedAccountConstants.FAILURE);
			statusDataMap.put(SharedAccountResponseType.ERROR_CODE, SharedAccountErrorCodeEnum.PROVIDER_NOT_EXIST.getErrorCode().toString());
			statusDataMap.put(SharedAccountResponseType.ERROR_MESSAGE, SharedAccountErrorCodeEnum.PROVIDER_NOT_EXIST.getErrorMessage());
			statusDataMap.put(SharedAccountResponseType.ERROR_VALUE, SharedAccountErrorCodeEnum.PROVIDER_NOT_EXIST.getErrorMessage());
			statusDataMap.put(SharedAccountResponseType.EXTERNAL_SYSTEM_TYPE, FDPConstant.FDP);
			result = null;
		} else if (sharedAccList.size() > 1) { // more than one providers
			statusDataMap.put(SharedAccountResponseType.STATUS, SharedAccountConstants.FAILURE);
			statusDataMap.put(SharedAccountResponseType.ERROR_CODE, SharedAccountErrorCodeEnum.AMBIGUOUS_PROVIDER.getErrorCode().toString());
			statusDataMap.put(SharedAccountResponseType.ERROR_MESSAGE, SharedAccountErrorCodeEnum.AMBIGUOUS_PROVIDER.getErrorMessage());
			statusDataMap.put(SharedAccountResponseType.ERROR_VALUE, SharedAccountErrorCodeEnum.AMBIGUOUS_PROVIDER.getErrorMessage());
			statusDataMap.put(SharedAccountResponseType.EXTERNAL_SYSTEM_TYPE, FDPConstant.FDP);
			result = null;
		} else {
			result = sharedAccList.get(0);
		}
		return result;
	}

	/**
	 * Validate consumer and if validation failed then set status and error_code
	 * in statusDataMap to failed and error_code respectively.
	 * 
	 * @param consumerMSISDN
	 *            the consumer msisdn
	 * @param sharedAccountGroupId
	 *            the shared account group id
	 * @param sharedAccInfo
	 *            the shared acc info
	 * @param statusDataMap
	 *            the status data map
	 * @return the shared account consumer dto if valid consumer otherwise null
	 */
	public SharedAccountConsumerDTO validateConsumer(Long consumerMSISDN, Long sharedAccountGroupId,
			ProductAddInfoAttributeDTO sharedAccInfo, Map<SharedAccountResponseType, Object> statusDataMap) {
		SharedAccountConsumerDTO result;
		/* getConsumerList */
		List<SharedAccountConsumerDTO> saConsumerDtoList = fdpSharedAccountConsumerDAO.getSharedAccountConsumer(
				consumerMSISDN, sharedAccInfo.getProviderOfferIdMapping(), sharedAccountGroupId);
		/* Consumer Validation */
		if (saConsumerDtoList == null || saConsumerDtoList.isEmpty()) { // no
																		// consumer
			statusDataMap.put(SharedAccountResponseType.STATUS, SharedAccountConstants.FAILURE);
			statusDataMap.put(SharedAccountResponseType.ERROR_CODE, SharedAccountErrorCodeEnum.CONSUMER_NOT_EXIST.getErrorCode().toString());
			statusDataMap.put(SharedAccountResponseType.ERROR_MESSAGE, SharedAccountErrorCodeEnum.CONSUMER_NOT_EXIST.getErrorMessage());
			statusDataMap.put(SharedAccountResponseType.ERROR_VALUE, SharedAccountErrorCodeEnum.CONSUMER_NOT_EXIST.getErrorMessage());
			statusDataMap.put(SharedAccountResponseType.EXTERNAL_SYSTEM_TYPE, FDPConstant.FDP);
			result = null;
		} else if (saConsumerDtoList.size() > 1) { // more than one
			// consumers
			statusDataMap.put(SharedAccountResponseType.STATUS, SharedAccountConstants.FAILURE);
			statusDataMap.put(SharedAccountResponseType.ERROR_CODE, SharedAccountErrorCodeEnum.AMBIGUOUS_CONSUMER.getErrorCode().toString());
			statusDataMap.put(SharedAccountResponseType.ERROR_MESSAGE, SharedAccountErrorCodeEnum.AMBIGUOUS_CONSUMER.getErrorMessage());
			statusDataMap.put(SharedAccountResponseType.ERROR_VALUE, SharedAccountErrorCodeEnum.AMBIGUOUS_CONSUMER.getErrorMessage());
			statusDataMap.put(SharedAccountResponseType.EXTERNAL_SYSTEM_TYPE, FDPConstant.FDP);
			result = null;
		} else {
			result = saConsumerDtoList.get(0);
		}
		return result;
	}

	/**
	 * Gets the shared acc req num.
	 * 
	 * @param fdpRequest
	 *            the fdp request
	 * @return the shared acc req num
	 */
	public Long getSharedAccReqNum(FDPRequest fdpRequest) {
		Long maxAccReqNo = fdpSharedAccountReqDao.getMaxAccReqNo(fdpRequest.getCircle(),
				ConsumerRequestType.ADD_CONSUMER);
		Long sharAccReqNum = null;
		if ((maxAccReqNo + 1l) <= 100000) {
			sharAccReqNum = 100000l;
		} else if ((maxAccReqNo + 1l) <= 999999) {
			sharAccReqNum = maxAccReqNo + 1l;
		} else {
			List<Long> accReqNoList = fdpSharedAccountReqDao.getAccReqListNotPending(fdpRequest.getCircle());
			sharAccReqNum = accReqNoList.get(0);
		}
		return sharAccReqNum;
	}

	/**
	 * Checks if is existing consumer.
	 * 
	 * @param consumerMSISDN
	 *            the consumer msisdn
	 * @param offerId
	 *            the offer id
	 * @return the boolean
	 */
	protected Boolean isExistingConsumer(Long consumerMSISDN, Long offerId) {
		Boolean result;
		/* getConsumerList */
		List<SharedAccountConsumerDTO> saConsumerDtoList = fdpSharedAccountConsumerDAO.getSharedAccountConsumer(
				consumerMSISDN, offerId);
		/* Consumer Validation */
		if (saConsumerDtoList == null || saConsumerDtoList.isEmpty()) { // no
																		// consumer
			result = false;
		} else {
			result = true;
		}
		return result;
	}

	/**
	 * Checks if is existing provider.
	 * 
	 * @param providerMSISDN
	 *            the provider msisdn
	 * @param offerId
	 *            the offer id
	 * @return the boolean
	 */
	protected Boolean isExistingProvider(Long providerMSISDN, Long offerId) {
		Boolean result;
		/* getProviderList */
		List<SharedAccGpDTO> sharedAccList = getSharedAccountGroup(providerMSISDN, offerId);
		/* Provider Validation */
		if (sharedAccList == null || sharedAccList.isEmpty()) { // no Provider
			result = false;
		} else {
			result = true;
		}
		return result;
	}
	
	/**
	 * Removes the shared account request for provider.
	 * 
	 * @param accountReqDAO
	 *            the account req dao
	 * @param fdpRequest
	 *            the fdp request
	 * @param params
	 *            the params
	 * @throws FDPConcurrencyException
	 *             the fDP concurrency exception
	 */
	protected void removeSharedAccountRequestForProvider(final FDPSharedAccountReqDAO accountReqDAO, final FDPRequest fdpRequest, final Object... params) throws FDPConcurrencyException {
		List<SharedAccountRequestDTO> sharedAccountRequestDTOs = accountReqDAO.getPendingConsumerList(
				(Long) params[0], fdpRequest.getSubscriberNumber(), fdpRequest.getCircle().getCircleId(),
				ConsumerRequestType.ADD_CONSUMER);
		if (sharedAccountRequestDTOs != null && !sharedAccountRequestDTOs.isEmpty()) {
			List<Long> sharedAccountsToUpdate = new ArrayList<Long>();
			for (SharedAccountRequestDTO requestDTO : sharedAccountRequestDTOs) {
				sharedAccountsToUpdate.add(requestDTO.getId());
			}
			accountReqDAO.updateSharedAccountRequestStatus(sharedAccountsToUpdate, ConsumerStatusEnum.EXPIRED.getStatusCode());
		}
	}
}
