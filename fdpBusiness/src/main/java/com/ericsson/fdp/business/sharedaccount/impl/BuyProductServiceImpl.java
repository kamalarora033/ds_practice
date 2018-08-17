package com.ericsson.fdp.business.sharedaccount.impl;

import java.util.HashMap;
import java.util.Map;

import javax.annotation.Resource;
import javax.ejb.Stateless;
import javax.inject.Inject;

import com.ericsson.fdp.business.enums.AuxRequestParam;
import com.ericsson.fdp.business.enums.ConfigurationKey;
import com.ericsson.fdp.business.sharedaccount.AbstractSharedAccountService;
import com.ericsson.fdp.business.sharedaccount.constans.SharedAccountConstants;
import com.ericsson.fdp.common.constants.JNDILookupConstant;
import com.ericsson.fdp.core.cache.AppCacheSubStore;
import com.ericsson.fdp.core.dsm.framework.service.FDPCache;
import com.ericsson.fdp.core.exception.ExecutionFailedException;
import com.ericsson.fdp.core.request.FDPRequest;
import com.ericsson.fdp.core.serviceprovisioning.bean.FDPAppBag;
import com.ericsson.fdp.dao.dto.sharedaccount.BuySharedProductDTO;
import com.ericsson.fdp.dao.enums.SharedAccountResponseType;
import com.ericsson.fdp.dao.fdpadmin.FDPProductAdditionalInfoDAO;
import com.ericsson.fdp.dao.fdpadmin.FDPSharedAccountGroupDAO;
import com.ericsson.fdp.dao.fdpadmin.FDPWebUserProductAdditionalInfoDAO;
import com.ericsson.fdp.dao.fdpadmin.FDPWebUserProductDAO;

@Stateless
public class BuyProductServiceImpl extends AbstractSharedAccountService {

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

	@Override
	public Map<SharedAccountResponseType, Object> executeSharedAccountService(final FDPRequest fdpRequest,
			final Object... params) throws ExecutionFailedException {
		final Map<SharedAccountResponseType, Object> statusDataMap = new HashMap<SharedAccountResponseType, Object>();
		statusDataMap.put(SharedAccountResponseType.STATUS, SharedAccountConstants.SUCCESS);
		statusDataMap.put(SharedAccountResponseType.PROVIDER_MSISDN, fdpRequest.getSubscriberNumber());
		if (checkDatabaseConfiguration(fdpRequest)) {
			final BuySharedProductDTO buySharedProductDto = getSharedAccountDTO(fdpRequest, (Long) params[0]);
			final Long webUserProductId = fdpWebUserProductDAO.saveWebUserProduct(buySharedProductDto);
			statusDataMap.put(SharedAccountResponseType.DB_ID, webUserProductId);
		}
		return statusDataMap;
	}

	/**
	 * This method is used to check if database configuration is required or
	 * not.
	 * 
	 * @param fdpRequest
	 *            the request.
	 * @return true if insertion is required.
	 */
	private boolean checkDatabaseConfiguration(final FDPRequest fdpRequest) {
		final Map<String, String> confMap = fdpRequest.getCircle().getConfigurationKeyValueMap();
		String databaseInsertionRequired = null;
		boolean insertionRequired = false;
		switch (fdpRequest.getChannel()) {
		case SMS:
			databaseInsertionRequired = confMap.get(ConfigurationKey.PRODUCT_DATABASE_SAVE_REQ_SMS.getAttributeName());
			break;

		case USSD:
			databaseInsertionRequired = confMap.get(ConfigurationKey.PRODUCT_DATABASE_SAVE_REQ_USSD.getAttributeName());
			break;

		case WEB:
			databaseInsertionRequired = confMap.get(ConfigurationKey.PRODUCT_DATABASE_SAVE_REQ_WEB.getAttributeName());
			break;

		default:
			break;

		}
		if (databaseInsertionRequired != null && databaseInsertionRequired.equalsIgnoreCase(Boolean.TRUE.toString())) {
			insertionRequired = true;
		}
		return insertionRequired;
	}

	private BuySharedProductDTO getSharedAccountDTO(final FDPRequest fdpRequest, final Long productId) {

		final BuySharedProductDTO buySharedProductDto = new BuySharedProductDTO();
		buySharedProductDto.setProductId(productId);
		final Object consumerMsisdn = fdpRequest.getAuxiliaryRequestParameter(AuxRequestParam.CONSUMER_MSISDN);
		Object boughtBy = fdpRequest.getAuxiliaryRequestParameter(AuxRequestParam.BOUGHT_BY);
		if (boughtBy == null) {
			boughtBy = fdpRequest.getSubscriberNumber().toString();
		}
		buySharedProductDto.setBoughtFor((consumerMsisdn == null) ? fdpRequest.getSubscriberNumber() : Long
				.valueOf(consumerMsisdn.toString()));
		buySharedProductDto.setBoughtBy(boughtBy.toString());
		buySharedProductDto.setGroupProviderMsisdn(fdpRequest.getSubscriberNumber());
		final FDPAppBag fdpAppBag = new FDPAppBag();
		fdpAppBag.setSubStore(AppCacheSubStore.CONFIGURATION_MAP);
		fdpAppBag.setKey(SharedAccountConstants.DEFAULT_ACCOUNT_NAME);
		buySharedProductDto.setAccountName(applicationConfigCache.getValue(fdpAppBag).toString());

		return buySharedProductDto;

	}
}
