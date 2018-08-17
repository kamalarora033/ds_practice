package com.ericsson.fdp.business.mdb;

import javax.annotation.Resource;
import javax.ejb.ActivationConfigProperty;
import javax.ejb.MessageDriven;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageListener;
import javax.jms.ObjectMessage;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ericsson.fdp.FDPCacheable;
import com.ericsson.fdp.business.cache.MetaDataService;
import com.ericsson.fdp.business.cache.datageneration.service.AIRRechargeDataService;
import com.ericsson.fdp.business.cache.datageneration.service.CommandDataService;
import com.ericsson.fdp.business.cache.datageneration.service.DMDataService;
import com.ericsson.fdp.business.cache.datageneration.service.DataToShareOfferService;
import com.ericsson.fdp.business.cache.datageneration.service.ErrorCodesNotificationDataService;
import com.ericsson.fdp.business.cache.datageneration.service.FAFOfferService;
import com.ericsson.fdp.business.cache.datageneration.service.FDPDynamicMenuAliasCodeService;
import com.ericsson.fdp.business.cache.datageneration.service.FDPProductCouponCodeCacheService;
import com.ericsson.fdp.business.cache.datageneration.service.Handset4GDataService;
import com.ericsson.fdp.business.cache.datageneration.service.NotificationDataService;
import com.ericsson.fdp.business.cache.datageneration.service.OfferAttributeDataService;
import com.ericsson.fdp.business.cache.datageneration.service.ProductAttributeMapService;
import com.ericsson.fdp.business.cache.datageneration.service.ProductCategoryExpressionService;
import com.ericsson.fdp.business.cache.datageneration.service.ProductChargingDiscount;
import com.ericsson.fdp.business.cache.datageneration.service.ProductDataService;
import com.ericsson.fdp.business.cache.datageneration.service.ProductNameDataService;
import com.ericsson.fdp.business.cache.datageneration.service.SPDataService;
import com.ericsson.fdp.business.cache.datageneration.service.TariffEnquiryAttributesExpressionService;
import com.ericsson.fdp.business.cache.datageneration.service.TimeToShareOfferService;
import com.ericsson.fdp.common.enums.ModuleType;
import com.ericsson.fdp.common.exception.FDPServiceException;
import com.ericsson.fdp.core.dsm.framework.CacheQueueConstants;
import com.ericsson.fdp.core.dsm.framework.service.FDPCache;
import com.ericsson.fdp.core.serviceprovisioning.bean.FDPMetaBag;
import com.ericsson.fdp.dao.dto.FDPCacheRequest;
import com.ericsson.fdp.dao.dto.UpdateCacheDTO;

/**
 * The Class FDPCachePublisherMDB is responsible putting metadata into Metadata
 * cache.
 */
@MessageDriven(mappedName = CacheQueueConstants.JMS_META_CACHE_QUEUE, activationConfig = {
		@ActivationConfigProperty(propertyName = "acknowledgeMode", propertyValue = "Auto-acknowledge"),
		@ActivationConfigProperty(propertyName = "destination", propertyValue = "queue/metaCacheQueue"),
		@ActivationConfigProperty(propertyName = "destinationType", propertyValue = "javax.jms.Queue") })
public class FDPCachePublisherMDB implements MessageListener {

	private static final Logger LOGGER = LoggerFactory.getLogger(FDPCachePublisherMDB.class);

	/** The fdp cache. */
	@Resource(lookup = "java:app/fdpBusiness-1.0/MetaDataCache")
	private FDPCache<FDPMetaBag, FDPCacheable> fdpCache;

	@Resource(lookup = "java:app/fdpBusiness-1.0/SPDataServiceImpl")
	private SPDataService spDataService;

	@Resource(lookup = "java:app/fdpBusiness-1.0/CommandDataServiceImpl")
	private CommandDataService commandDataService;

	@Resource(lookup = "java:app/fdpBusiness-1.0/ProductDataServiceImpl")
	private ProductDataService productDataService;
	
	@Resource(lookup = "java:app/fdpBusiness-1.0/ProductNameDataServiceImpl")
	private ProductNameDataService productNameDataService;

	@Resource(lookup = "java:app/fdpBusiness-1.0/DMDataServiceImpl")
	private DMDataService dmDataService;

	@Resource(lookup = "java:app/fdpBusiness-1.0/AIRRechargeDataServiceImpl")
	private AIRRechargeDataService airRechargeDataService;

	@Resource(lookup = "java:app/fdpBusiness-1.0/NotificationDataServiceImpl")
	private NotificationDataService notificationDataService;

	@Resource(lookup="java:app/fdpBusiness-1.0/FDPDynamicMenuAliasCodeServiceImpl")
	private FDPDynamicMenuAliasCodeService dynamicMenuAliasCodeService;

	/** The error codes notification data service. */
	@Resource(lookup = "java:global/fdpBusiness-ear/fdpBusiness-1.0/ErrorCodesNotificationDataServiceImpl")
	private ErrorCodesNotificationDataService errorCodesNotificationDataService;

	@Resource(lookup ="java:global/fdpBusiness-ear/fdpBusiness-1.0/TariffEnquiryAttributesExpressionCacheImpl")
	private TariffEnquiryAttributesExpressionService tariffEnquiryAttributesExpressionService;
	
	@Resource(lookup = "java:app/fdpBusiness-1.0/ProductCategoryExpressionServiceImpl")
	private ProductCategoryExpressionService productCategoryExpressionService;
	
	@Resource(lookup ="java:global/fdpBusiness-ear/fdpBusiness-1.0/FDPProductCouponCodeCacheServiceImpl")
	private FDPProductCouponCodeCacheService productCouponCodeService;
	
	/*@Resource(lookup = "java:global/fdpBusiness-ear/fdpBusiness-1.0/NotificationParamMappingServiceImpl")
	private NotificationParamMappingService notificationParamMappingService;*/

	@Resource(lookup = "java:app/fdpBusiness-1.0/ProductChargingDiscountImpl")
	private ProductChargingDiscount productChargingDiscount;
	
	@Resource(lookup="java:app/fdpBusiness-1.0/OfferAttributeDataServiceImpl")
	private OfferAttributeDataService offerAttributeserive;
	
	@Resource(lookup = "java:global/fdpBusiness-ear/fdpBusiness-1.0/FAFOfferServiceImpl")
	private FAFOfferService fafOfferService;
	

	@Resource(lookup = "java:global/fdpBusiness-ear/fdpBusiness-1.0/TimeToShareOfferServiceImpl")
	private TimeToShareOfferService timeToShareOfferService;
	
	@Resource(lookup = "java:global/fdpBusiness-ear/fdpBusiness-1.0/DataToShareOfferServiceImpl")
	private DataToShareOfferService dataToShareOfferService;
	
	/*@Resource(lookup="java:app/fdpBusiness-1.0/PAMAttributesDataServiceImpl")
	private PAMAttributesDataService pamAttributesDataService;*/
	
	@Resource(lookup = "java:global/fdpBusiness-ear/fdpBusiness-1.0/ProductAttributeMapServiceImpl")
	private ProductAttributeMapService productAttributeMapService;
	
	@Resource(lookup = "java:global/fdpBusiness-ear/fdpBusiness-1.0/Handset4GDataServiceImpl")
	private Handset4GDataService handset4GDataService;
	

	@Override
	public void onMessage(final Message message) {
		ObjectMessage objectMessage = (ObjectMessage) message;
		try {
			FDPCacheRequest fdpCacheRequest = (FDPCacheRequest) objectMessage.getObject();
			if (fdpCacheRequest instanceof UpdateCacheDTO) {
				UpdateCacheDTO updateCacheDTO = (UpdateCacheDTO) fdpCacheRequest;
				MetaDataService metaDataService = null;
				if (updateCacheDTO.getModuleType().equals(ModuleType.SP_OTHERS)
						|| updateCacheDTO.getModuleType().equals(ModuleType.SP_PRODUCT)) {
					metaDataService = spDataService;
				} else if (updateCacheDTO.getModuleType().equals(ModuleType.PRODUCT)) {
					metaDataService = productDataService;
				}else if (updateCacheDTO.getModuleType().equals(ModuleType.PRODUCT_NAME_ID_MAP)) {
						metaDataService = productNameDataService;
				} else if (updateCacheDTO.getModuleType().equals(ModuleType.DM)) {
					metaDataService = dmDataService;
				} else if (updateCacheDTO.getModuleType().equals(ModuleType.COMMAND)) {
					metaDataService = commandDataService;
				} else if (updateCacheDTO.getModuleType().equals(ModuleType.AIR_RECHARGE)) {
					metaDataService = airRechargeDataService;
				} else if (updateCacheDTO.getModuleType().equals(ModuleType.NOTIFICATION)) {
					metaDataService = notificationDataService;
				} else if (updateCacheDTO.getModuleType().equals(ModuleType.RESPONSE_CODE_NOTIFICATION_MAPPING)) {
					metaDataService = errorCodesNotificationDataService;
				} else if (updateCacheDTO.getModuleType().equals(ModuleType.FAULT_CODE_NOTIFICATION_MAPPING)) {
					metaDataService = errorCodesNotificationDataService;
				}else if (updateCacheDTO.getModuleType().equals(ModuleType.TARIFF_ENQUIRY_ATTRIBUTE_EXPRESSION)) {
					metaDataService = tariffEnquiryAttributesExpressionService;
				}else if (updateCacheDTO.getModuleType().equals(ModuleType.PRODUCT_CATEGORY)) {
					metaDataService = productCategoryExpressionService;
				}else if (updateCacheDTO.getModuleType().equals(ModuleType.PRODUCT_COUPON_CODE)) {
					metaDataService = productCouponCodeService;
				}/*else if (updateCacheDTO.getModuleType().equals(ModuleType.NOTIFICATION_PARAM_MAPPING)) {
					metaDataService = notificationParamMappingService;
				}*/else if (updateCacheDTO.getModuleType().equals(ModuleType.PRODUCT_CHARGING_DISCOUNT)) {
					metaDataService = productChargingDiscount;
				}else if (updateCacheDTO.getModuleType().equals(ModuleType.FAF_OFFER)) {
					metaDataService = fafOfferService;
				}else if (updateCacheDTO.getModuleType().equals(ModuleType.TIME_TO_SHARE)) {
					metaDataService = timeToShareOfferService;
				}else if (updateCacheDTO.getModuleType().equals(ModuleType.DATA_TO_SHARE)) {
						metaDataService = dataToShareOfferService;
			}else if (updateCacheDTO.getModuleType().equals(ModuleType.OFFER_ATTRIBUTE)) {
				metaDataService = offerAttributeserive;
			}
			else if(updateCacheDTO.getModuleType().equals(ModuleType.PRODUCT_ATTRIBUTE_MAP))
			{
				metaDataService = productAttributeMapService;
			}else if(updateCacheDTO.getModuleType().equals(ModuleType.HANDSET_4G))
			{
				metaDataService = handset4GDataService;
			}		
				
				
			/*else if(updateCacheDTO.getModuleType().equals(ModuleType.PAM_OFFER_ATTRIBUTE))
			{
				metaDataService = pamAttributesDataService;
			}*/

				metaDataService.updateMetaCache(updateCacheDTO);
			}
		} catch (JMSException e) {
			LOGGER.error("unable to process message coming from MetaCache Queue",e);
		} catch (FDPServiceException e) {
			LOGGER.error("unable to update product cache message coming from MetaCache Queue",e);
		}

	}
}
