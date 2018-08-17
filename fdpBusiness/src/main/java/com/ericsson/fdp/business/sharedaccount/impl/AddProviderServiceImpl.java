package com.ericsson.fdp.business.sharedaccount.impl;

import java.text.ParseException;
import java.util.Calendar;
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
import com.ericsson.fdp.common.util.DateUtil;
import com.ericsson.fdp.core.cache.AppCacheSubStore;
import com.ericsson.fdp.core.dsm.framework.service.FDPCache;
import com.ericsson.fdp.core.exception.ExecutionFailedException;
import com.ericsson.fdp.core.request.FDPRequest;
import com.ericsson.fdp.core.serviceprovisioning.bean.FDPAppBag;
import com.ericsson.fdp.dao.dto.product.ProductDTO;
import com.ericsson.fdp.dao.dto.sharedaccount.ProductAddInfoAttributeDTO;
import com.ericsson.fdp.dao.dto.sharedaccount.SharedAccGpDTO;
import com.ericsson.fdp.dao.dto.sharedaccount.SharedAccountConsumerDTO;
import com.ericsson.fdp.dao.enums.ConsumerUpgradeType;
import com.ericsson.fdp.dao.enums.ProductAdditionalInfoEnum;
import com.ericsson.fdp.dao.enums.SharedAccountErrorCodeEnum;
import com.ericsson.fdp.dao.enums.SharedAccountResponseType;
import com.ericsson.fdp.dao.enums.StepNameEnum;
import com.ericsson.fdp.dao.fdpadmin.FDPProductAdditionalInfoDAO;
import com.ericsson.fdp.dao.fdpadmin.FDPSharedAccountConsumerDAO;
import com.ericsson.fdp.dao.fdpadmin.FDPSharedAccountGroupDAO;

@Stateless
public class AddProviderServiceImpl extends AbstractSharedAccountService {

	@Inject
	private FDPSharedAccountGroupDAO fdpSharedAccountGroupDAO;

	@Inject
	private FDPSharedAccountConsumerDAO fdpSharedAccountConsumerDAO;

	@Inject
	private FDPProductAdditionalInfoDAO fdpProductAdditionalInfoDAO;

	@Resource(lookup = JNDILookupConstant.APPLICATION_CONFIG_CACHE_JNDI_NAME)
	private FDPCache<FDPAppBag, Object> applicationConfigCache;

	@Override
	public Map<SharedAccountResponseType, Object> executeSharedAccountService(final FDPRequest fdpRequest,
			final Object... params) throws ExecutionFailedException {
		// actor is provider so newProviderMSISDN is in auxRequestParam.
		final Long newProviderMSISDN = Long.valueOf(fdpRequest.getAuxiliaryRequestParameter(
				AuxRequestParam.PROVIDER_MSISDN).toString());
		// actor is provider so providerMSISDN is in fdpRequest.
		final Long providerMSISDN = fdpRequest.getSubscriberNumber();
		final Long productID = (Long) params[0];
		// provider additional info for different offer id's.
		final ProductAddInfoAttributeDTO sharedAccInfo = getProviderAddInfo(productID);

		final Map<SharedAccountResponseType, Object> statusDataMap = new HashMap<SharedAccountResponseType, Object>();
		statusDataMap.put(SharedAccountResponseType.CONSUMER_MSISDN, providerMSISDN);
		statusDataMap.put(SharedAccountResponseType.PROVIDER_MSISDN, newProviderMSISDN);
		final List<Long> productIds = fdpProductAdditionalInfoDAO.getProductIdByInfoKeyAndValue(
				ProductAdditionalInfoEnum.SHARED_ACC_OFFER_ID.getKey().toString(), sharedAccInfo
						.getProviderOfferIdMapping().toString());
		if (productIds == null || productIds.isEmpty()) {
			statusDataMap.put(SharedAccountResponseType.STATUS, SharedAccountConstants.FAILURE);
			statusDataMap.put(SharedAccountResponseType.ERROR_CODE,
					SharedAccountErrorCodeEnum.NO_PRODUCT_FOR_SHARED_ACC_MAPPING_ID.getErrorCode().toString());
			statusDataMap.put(SharedAccountResponseType.ERROR_MESSAGE,
					SharedAccountErrorCodeEnum.NO_PRODUCT_FOR_SHARED_ACC_MAPPING_ID.getErrorMessage());
		} else {
			// add provider
			final Long newProductId = productIds.get(0);
			final ProductAddInfoAttributeDTO newSharedAccInfo = getProviderAddInfo(newProductId);
			final SharedAccGpDTO providerDTO = new SharedAccGpDTO();
			FDPAppBag fdpAppBag = new FDPAppBag();
			fdpAppBag.setSubStore(AppCacheSubStore.CONFIGURATION_MAP);
			fdpAppBag.setKey(SharedAccountConstants.DEFAULT_GROUP_NAME);
			providerDTO.setGroupName(applicationConfigCache.getValue(fdpAppBag).toString());
			providerDTO.setGroupProviderName(newProviderMSISDN.toString());
			providerDTO.setGroupProviderMSISDN(newProviderMSISDN);
			providerDTO.setGroupLimit(newSharedAccInfo.getGroupLimit());
			providerDTO.setProduct(new ProductDTO(newProductId));
			providerDTO.setOfferId(newSharedAccInfo.getShrAccOfferId());
			providerDTO.setGroupThresholdUnit(newSharedAccInfo.getGroupThresholdUnit());
			providerDTO.setWebProductId((Long) fdpRequest.getValueFromStep(StepNameEnum.VALIDATION_STEP.getValue(),
					SharedAccountResponseType.WEB_PRODUCT_ID.name()));
			providerDTO.setParentSharedAccGpId((Long) fdpRequest.getValueFromStep(
					StepNameEnum.VALIDATION_STEP.getValue(), SharedAccountResponseType.DB_ID.name()));
			final Long newGroupId = fdpSharedAccountGroupDAO.saveSharedAccountGroup(providerDTO,
					providerMSISDN.toString());
			// add consumer
			final SharedAccountConsumerDTO acceptConsumerDTO = new SharedAccountConsumerDTO();
			acceptConsumerDTO.setConsumerMsisdn(newProviderMSISDN.toString());
			acceptConsumerDTO.setConsumerName(newProviderMSISDN.toString());
			fdpAppBag = new FDPAppBag();
			fdpAppBag.setSubStore(AppCacheSubStore.CONFIGURATION_MAP);
			fdpAppBag.setKey(SharedAccountConstants.UPGRADE_EXPIRE_YEARS_COUNT);

			Calendar currentDate = Calendar.getInstance();
			try {
				currentDate = DateUtil.getDateTimeFromFDPDateTimeFormat(FDPConstant.DATE_MAX);
			} catch (final ParseException e) {
				currentDate.add(Calendar.YEAR, new Integer(100));
			}
			acceptConsumerDTO.setUpgradeExpiredDate(currentDate);
			acceptConsumerDTO.setUpgradeType(ConsumerUpgradeType.PERMANENT);
			acceptConsumerDTO.setProviderGroupId(newGroupId);
			acceptConsumerDTO.setOfferId(sharedAccInfo.getSharedAccOfferIdMapping());
			acceptConsumerDTO.setConsumerNewLimit(Integer.parseInt(newSharedAccInfo.getConsumerLimit().toString()));
			acceptConsumerDTO.setConsumerThresholdUnit(newSharedAccInfo.getConsumerThresholdUnit());
			fdpSharedAccountConsumerDAO.saveConsumer(acceptConsumerDTO);
			statusDataMap.put(SharedAccountResponseType.STATUS, SharedAccountConstants.SUCCESS);
		}
		return statusDataMap;
	}
}
