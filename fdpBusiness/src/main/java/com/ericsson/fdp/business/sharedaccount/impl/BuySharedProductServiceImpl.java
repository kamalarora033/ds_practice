package com.ericsson.fdp.business.sharedaccount.impl;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.Resource;
import javax.ejb.Stateless;
import javax.inject.Inject;

import com.ericsson.fdp.business.enums.AuxRequestParam;
import com.ericsson.fdp.business.sharedaccount.AbstractSharedAccountService;
import com.ericsson.fdp.business.sharedaccount.constans.SharedAccountConstants;
import com.ericsson.fdp.common.constants.JNDILookupConstant;
import com.ericsson.fdp.core.cache.AppCacheSubStore;
import com.ericsson.fdp.core.dsm.framework.service.FDPCache;
import com.ericsson.fdp.core.exception.ExecutionFailedException;
import com.ericsson.fdp.core.request.FDPRequest;
import com.ericsson.fdp.core.serviceprovisioning.bean.FDPAppBag;
import com.ericsson.fdp.dao.dto.sharedaccount.BuySharedProductDTO;
import com.ericsson.fdp.dao.dto.sharedaccount.ProductAddInfoAttributeDTO;
import com.ericsson.fdp.dao.dto.sharedaccount.SharedAccGpDTO;
import com.ericsson.fdp.dao.enums.SharedAccountErrorCodeEnum;
import com.ericsson.fdp.dao.enums.SharedAccountGroupStatus;
import com.ericsson.fdp.dao.enums.SharedAccountResponseType;
import com.ericsson.fdp.dao.exception.FDPConcurrencyException;
import com.ericsson.fdp.dao.fdpadmin.FDPProductAdditionalInfoDAO;
import com.ericsson.fdp.dao.fdpadmin.FDPSharedAccountGroupDAO;
import com.ericsson.fdp.dao.fdpadmin.FDPWebUserProductAdditionalInfoDAO;
import com.ericsson.fdp.dao.fdpadmin.FDPWebUserProductDAO;

@Stateless
public class BuySharedProductServiceImpl extends AbstractSharedAccountService {

	@Inject
	private FDPWebUserProductDAO fdpWebUserProductDAO;

	@Inject
	private FDPProductAdditionalInfoDAO fdpProductAdditionalInfoDAO;

	@Inject
	private FDPWebUserProductAdditionalInfoDAO fdpWebUserProductAdditionalInfoDAO;

	@Inject
	private FDPSharedAccountGroupDAO fdpSharedAccountGroupDAO;

	@Resource(lookup = JNDILookupConstant.APPLICATION_CONFIG_CACHE_JNDI_NAME)
	private FDPCache<FDPAppBag, Object> applicationConfigCache;

	@SuppressWarnings("unchecked")
	@Override
	public Map<SharedAccountResponseType, Object> executeSharedAccountService(final FDPRequest fdpRequest,
			final Object... params) throws ExecutionFailedException {

		final Map<SharedAccountResponseType, Object> statusDataMap = new HashMap<SharedAccountResponseType, Object>();
		final ProductAddInfoAttributeDTO productAddInfo = getProviderAddInfo((Long) params[0]);

		try {
			final BuySharedProductDTO buySharedProductDto = getSharedAccountDTO(fdpRequest, (Long) params[0],
					productAddInfo);
			final Long webUserProductId = fdpWebUserProductDAO.saveWebUserProduct(buySharedProductDto);
			statusDataMap.put(SharedAccountResponseType.PROVIDER_MSISDN, fdpRequest.getSubscriberNumber());
			final SharedAccountErrorCodeEnum errorCode = saveUpdateSharedAccGroup(fdpRequest, (Long) params[0],
					productAddInfo, webUserProductId);
			if (errorCode == null) {
				statusDataMap.put(SharedAccountResponseType.STATUS, SharedAccountConstants.SUCCESS);
				statusDataMap.put(SharedAccountResponseType.DB_ID, webUserProductId);
				if (fdpRequest.getAuxiliaryRequestParameter(AuxRequestParam.CURRENT_SHARED_ACCOUNT_VALUE) != null
						&& ((List<String>) fdpRequest
								.getAuxiliaryRequestParameter(AuxRequestParam.CURRENT_SHARED_ACCOUNT_VALUE)).get(0) != null) {
					statusDataMap
							.put(SharedAccountResponseType.CONSUMER_MSISDN,
									new Long(
											((List<String>) fdpRequest
													.getAuxiliaryRequestParameter(AuxRequestParam.CURRENT_SHARED_ACCOUNT_VALUE))
													.get(0)));
				}
			} else {
				statusDataMap.put(SharedAccountResponseType.STATUS, SharedAccountConstants.FAILURE);
				statusDataMap.put(SharedAccountResponseType.ERROR_CODE, errorCode.getErrorCode().toString());
				statusDataMap.put(SharedAccountResponseType.ERROR_MESSAGE, errorCode.getErrorMessage());
			}
		} catch (final FDPConcurrencyException e) {
			throw new ExecutionFailedException("Can not buy Product", e);
		}
		return statusDataMap;
	}

	private SharedAccountErrorCodeEnum saveUpdateSharedAccGroup(final FDPRequest fdpRequest, final Long productId,
			final ProductAddInfoAttributeDTO productAddInfo, final Long webUserProductId)
			throws FDPConcurrencyException {
		SharedAccountErrorCodeEnum errorCode = null;
		final List<SharedAccGpDTO> acceptConsumerList = fdpSharedAccountGroupDAO.getSharedAccGroup(
				fdpRequest.getSubscriberNumber(), productAddInfo.getShrAccOfferId(), null);
		if (acceptConsumerList != null && !acceptConsumerList.isEmpty()) {
			final SharedAccGpDTO shareAccGroup = acceptConsumerList.get(0);
			if (shareAccGroup.getStatus().equals(SharedAccountGroupStatus.DORMENT)) {
				errorCode = SharedAccountErrorCodeEnum.ACCOUNT_DORMENT;
			}
		} else {
			final BuySharedProductDTO buySharedProductDto = getSharedAccountDTO(fdpRequest, productId, productAddInfo);
			buySharedProductDto.setWebProductId(webUserProductId);
			fdpSharedAccountGroupDAO.saveSharedAccountGroup(buySharedProductDto,
					buySharedProductDto.getGroupProviderName());
		}
		return errorCode;
	}

	private BuySharedProductDTO getSharedAccountDTO(final FDPRequest fdpRequest, final Long productId,
			final ProductAddInfoAttributeDTO productAddInfo) {

		final BuySharedProductDTO buySharedProductDto = new BuySharedProductDTO();
		buySharedProductDto.setProductId(productId);
		buySharedProductDto.setBoughtFor(fdpRequest.getSubscriberNumber());
		Object boughtBy = fdpRequest.getAuxiliaryRequestParameter(AuxRequestParam.BOUGHT_BY);
		if (boughtBy == null) {
			boughtBy = fdpRequest.getSubscriberNumber().toString();
		}
		buySharedProductDto.setBoughtBy(boughtBy.toString());
		buySharedProductDto.setGroupProviderName(fdpRequest.getSubscriberNumber().toString());
		buySharedProductDto.setGroupProviderMsisdn(fdpRequest.getSubscriberNumber());
		final FDPAppBag fdpAppBag = new FDPAppBag();
		fdpAppBag.setSubStore(AppCacheSubStore.CONFIGURATION_MAP);
		fdpAppBag.setKey(SharedAccountConstants.DEFAULT_ACCOUNT_NAME);
		buySharedProductDto.setAccountName(applicationConfigCache.getValue(fdpAppBag).toString());
		fdpAppBag.setSubStore(AppCacheSubStore.CONFIGURATION_MAP);
		fdpAppBag.setKey(SharedAccountConstants.DEFAULT_GROUP_NAME);
		buySharedProductDto.setGroupName(applicationConfigCache.getValue(fdpAppBag).toString());
		buySharedProductDto.setGroupLimit(productAddInfo.getGroupLimit());
		buySharedProductDto.setOfferId(productAddInfo.getShrAccOfferId());
		buySharedProductDto.setProviderThresholdUnit(productAddInfo.getGroupThresholdUnit());

		return buySharedProductDto;

	}
}
