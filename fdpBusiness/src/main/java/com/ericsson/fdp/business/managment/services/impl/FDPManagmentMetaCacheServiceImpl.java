package com.ericsson.fdp.business.managment.services.impl;

import java.util.HashMap;
import java.util.Map;

import com.ericsson.fdp.business.cache.datageneration.service.FAFOfferService;
import com.ericsson.fdp.business.cache.datageneration.service.AIRRechargeDataService;
import com.ericsson.fdp.business.cache.datageneration.service.CommandDataService;
import com.ericsson.fdp.business.cache.datageneration.service.DMDataService;
import com.ericsson.fdp.business.cache.datageneration.service.ErrorCodesNotificationDataService;
import com.ericsson.fdp.business.cache.datageneration.service.FDPProductCouponCodeCacheService;
import com.ericsson.fdp.business.cache.datageneration.service.NotificationDataService;
import com.ericsson.fdp.business.cache.datageneration.service.ProductCategoryExpressionService;
import com.ericsson.fdp.business.cache.datageneration.service.ProductDataService;
import com.ericsson.fdp.business.cache.datageneration.service.ProductNameDataService;
import com.ericsson.fdp.business.cache.datageneration.service.SPDataService;
import com.ericsson.fdp.business.cache.datageneration.service.TariffEnquiryAttributesExpressionService;
import com.ericsson.fdp.business.cache.datageneration.service.TariffEnquiryAttributesService;
import com.ericsson.fdp.business.cache.datageneration.service.TimeToShareOfferService;
import com.ericsson.fdp.common.enums.ModuleType;
import com.ericsson.fdp.common.exception.FDPException;
import com.ericsson.fdp.common.exception.FDPServiceException;
import com.ericsson.fdp.common.vo.FDPCircle;
import com.ericsson.fdp.core.cache.AppCacheSubStore;
import com.ericsson.fdp.core.exception.ExecutionFailedException;
import com.ericsson.fdp.core.serviceprovisioning.bean.FDPAppBag;
import com.ericsson.fdp.core.serviceprovisioning.bean.FDPMetaBag;
import com.ericsson.fdp.core.utils.ApplicationConfigUtil;
import com.ericsson.fdp.managment.constants.FDPManagmentConstants;
import com.ericsson.fdp.managment.enums.FDPManagmentParams;
import com.ericsson.fdp.managment.services.impl.FDPAbstractManagmentService;

public class FDPManagmentMetaCacheServiceImpl extends FDPAbstractManagmentService {

	@Override
	public Map<Object, Object> preServiceExecutor(Map<FDPManagmentParams, String> requestMap)
			throws ExecutionFailedException {
		boolean isValid = true;
		final Map<Object, Object> map = new HashMap<Object, Object>();
		Object key = requestMap.get(FDPManagmentParams.KEY);
		final String module = requestMap.get(FDPManagmentParams.CACHE_MODULE);
		final String circleCode = requestMap.get(FDPManagmentParams.CIRCLE_CODE);

		if (null == key) {
			isValid = false;
			map.put(requestMap.get(FDPManagmentParams.KEY.getParamName()), FDPManagmentConstants.NOT_A_VALID_INPUT_TEXT);
		}

		final FDPCircle fdpCircle = (FDPCircle) getAppCache().getValue(
				new FDPAppBag(AppCacheSubStore.CIRCLE_CODE_CIRCLE_NAME_MAP, circleCode));
		if (null == fdpCircle) {
			isValid = false;
			map.put(requestMap.get(FDPManagmentParams.CIRCLE_CODE.getParamName()), FDPManagmentConstants.NOT_A_VALID_INPUT_TEXT);
		}

		final ModuleType moduleType = ModuleType.getMetaCacheStore(module);
		if (null == moduleType) {
			isValid = false;
			map.put(requestMap.get(FDPManagmentParams.CACHE_MODULE.getParamName()), FDPManagmentConstants.NOT_A_VALID_INPUT_TEXT);
		}

		if (isValid) {
			final FDPMetaBag fdpMetaBag = new FDPMetaBag(fdpCircle, ModuleType.getMetaCacheStore(module), key);
			Object cacheValue = getMetaCache().getValue(fdpMetaBag);
			map.put(key, cacheValue);
		}
		return map;
	}

	@Override
	public Map<Object, Object> executeService(Map<FDPManagmentParams, String> requestMap)
			throws ExecutionFailedException {
		boolean isValid = true;
		final Map<Object, Object> map = new HashMap<Object, Object>();
		final String module = requestMap.get(FDPManagmentParams.CACHE_MODULE);
		final String circleCode = requestMap.get(FDPManagmentParams.CIRCLE_CODE);
		try {
			final FDPCircle fdpCircle = (FDPCircle) getAppCache().getValue(
					new FDPAppBag(AppCacheSubStore.CIRCLE_CODE_CIRCLE_NAME_MAP, circleCode));
			if (null == fdpCircle) {
				isValid = false;
				map.put(FDPManagmentParams.CIRCLE_CODE.getParamName(), FDPManagmentConstants.NOT_A_VALID_INPUT_TEXT);
			}

			final ModuleType moduleType = ModuleType.getMetaCacheStore(module);
			if (null == moduleType) {
				isValid = false;
				map.put(FDPManagmentParams.CACHE_MODULE.getParamName(), FDPManagmentConstants.NOT_A_VALID_INPUT_TEXT);
			}

			if (isValid) {
				reloadCacheForCircle(fdpCircle, moduleType);
				map.put(module, fdpCircle.getCircleName() + " reloaded sucessfully.");
			}
		} catch (FDPException e) {
			e.printStackTrace();
			throw new ExecutionFailedException("Got FDPException during reloading meta cache, Actual Error:", e);
		} catch (Exception e) {
			e.printStackTrace();
			throw new ExecutionFailedException("Got Exception during reloading meta cache, Actual Error:", e);
		}
		return map;
	}

	@Override
	public String getResponseMessage(Object response) throws ExecutionFailedException {
		return convertFromGsonToString(response);
	}

	@Override
	public String exceptionNotification(Map<FDPManagmentParams, String> requestMap) {
		final StringBuffer stringBuffer = new StringBuffer();
		stringBuffer.append("Possible Values for module are ");
		for (final ModuleType subStore : ModuleType.values()) {
			if (subStore.isSeperateStore()) {
				stringBuffer.append(FDPManagmentConstants.NEW_LINE);
				stringBuffer.append(subStore.name());
			}
		}
		return stringBuffer.toString();
	}

	/**
	 * This method will reload the meta cache for each store.
	 * 
	 * @param fdpCircle
	 * @param moduleType
	 * @return
	 * @throws FDPServiceException
	 */
	private boolean reloadCacheForCircle(final FDPCircle fdpCircle, final ModuleType moduleType)
			throws FDPServiceException {
		boolean isReload = false;
		try {
			switch (moduleType) {
			case SP_PRODUCT:
			case SP_OTHERS:
				final SPDataService spDataService = (SPDataService) ApplicationConfigUtil
						.getBean("java:app/fdpBusiness-1.0/SPDataServiceImpl");
				spDataService.initializeMetaCache(fdpCircle);
				break;
			case DM:
			case DYNAMIC_MENU_CODE_ALIAS:
				final DMDataService dmDataService = (DMDataService) ApplicationConfigUtil
						.getBean("java:app/fdpBusiness-1.0/DMDataServiceImpl");
				dmDataService.initializeMetaCache(fdpCircle);
				break;
			case PRODUCT:
			case PRODUCT_ALIAS:
				final ProductDataService productDataService = (ProductDataService) ApplicationConfigUtil
						.getBean("java:app/fdpBusiness-1.0/ProductDataServiceImpl");
				productDataService.initializeMetaCache(fdpCircle);
				break;
			case PRODUCT_NAME_ID_MAP:
				final ProductNameDataService productNameDataService = (ProductNameDataService) ApplicationConfigUtil
						.getBean("java:app/fdpBusiness-1.0/ProductNameDataServiceImpl");
				productNameDataService.initializeMetaCache(fdpCircle);
				break;
			case COMMAND:
				final CommandDataService commandDataService = (CommandDataService) ApplicationConfigUtil
						.getBean("java:app/fdpBusiness-1.0/CommandDataServiceImpl");
				commandDataService.initializeMetaCache(fdpCircle);
				break;
			case AIR_RECHARGE:
				final AIRRechargeDataService airRechargeDataService = (AIRRechargeDataService) ApplicationConfigUtil
						.getBean("java:app/fdpBusiness-1.0/AIRRechargeDataServiceImpl");
				airRechargeDataService.initializeMetaCache(fdpCircle);
				break;
			case NOTIFICATION:
				final NotificationDataService notificationDataServiceImpl = (NotificationDataService) ApplicationConfigUtil
						.getBean("java:app/fdpBusiness-1.0/NotificationDataServiceImpl");
				notificationDataServiceImpl.initializeMetaCache(fdpCircle);
				break;
			case TARIFF_ENQUIRY_ATTRIBUTES:
				final TariffEnquiryAttributesService tariffEnquiryAttributesService = (TariffEnquiryAttributesService) ApplicationConfigUtil
						.getBean("java:app/fdpBusiness-1.0/TariffEnquiryAttributesCacheImpl");
				tariffEnquiryAttributesService.initializeMetaCache(fdpCircle);
				break;
			case FAULT_CODE_NOTIFICATION_MAPPING:
			case RESPONSE_CODE_NOTIFICATION_MAPPING:
				final ErrorCodesNotificationDataService errorCodesNotificationDataService = (ErrorCodesNotificationDataService) ApplicationConfigUtil
						.getBean("java:global/fdpBusiness-ear/fdpBusiness-1.0/ErrorCodesNotificationDataServiceImpl");
				errorCodesNotificationDataService.initializeMetaCache(fdpCircle);
				break;
			case TARIFF_ENQUIRY_ATTRIBUTE_EXPRESSION:
				final TariffEnquiryAttributesExpressionService tariffEnquiryAttributesExpressionService = (TariffEnquiryAttributesExpressionService) ApplicationConfigUtil
						.getBean("java:global/fdpBusiness-ear/fdpBusiness-1.0/TariffEnquiryAttributesExpressionCacheImpl");
				tariffEnquiryAttributesExpressionService.initializeMetaCache(fdpCircle);
				break;
			case PRODUCT_CATEGORY:
				final ProductCategoryExpressionService productCategoryExpressionService = (ProductCategoryExpressionService) ApplicationConfigUtil
						.getBean("java:app/fdpBusiness-1.0/ProductCategoryExpressionServiceImpl");
				productCategoryExpressionService.initializeMetaCache(fdpCircle);
				break;
			case PRODUCT_COUPON_CODE:
			case PRODUCT_COUPON_MAP:
				final FDPProductCouponCodeCacheService productCouponCodeService = (FDPProductCouponCodeCacheService) ApplicationConfigUtil
						.getBean("java:global/fdpBusiness-ear/fdpBusiness-1.0/FDPProductCouponCodeCacheServiceImpl");
				productCouponCodeService.initializeMetaCache(fdpCircle);
				break;
				
			case FAF_OFFER:
				final FAFOfferService fafOfferService = (FAFOfferService) ApplicationConfigUtil
						.getBean("java:global/fdpBusiness-ear/fdpBusiness-1.0/FAFOfferServiceImpl");
				fafOfferService.initializeMetaCache(fdpCircle);
				break;
			case TIME_TO_SHARE:
				final TimeToShareOfferService timeToShareService = (TimeToShareOfferService) ApplicationConfigUtil
						.getBean("java:global/fdpBusiness-ear/fdpBusiness-1.0/TimeToShareOfferServiceImpl");
				timeToShareService.initializeMetaCache(fdpCircle);
				break;
			default:
				break;
			}
		} catch (Exception e) {
			e.printStackTrace();
			throw new FDPServiceException("Unable to load Meta Cache , Actual Error:", e);
		}
		return isReload;
	}
}
