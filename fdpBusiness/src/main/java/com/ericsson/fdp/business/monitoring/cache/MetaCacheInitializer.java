package com.ericsson.fdp.business.monitoring.cache;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import javax.annotation.Resource;
import javax.ejb.Stateless;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ericsson.fdp.FDPCacheable;
import com.ericsson.fdp.business.cache.datageneration.service.AIRRechargeDataService;
import com.ericsson.fdp.business.cache.datageneration.service.AirConfigCacheService;
import com.ericsson.fdp.business.cache.datageneration.service.AsyncCommandCacheService;
import com.ericsson.fdp.business.cache.datageneration.service.CommandDataService;
import com.ericsson.fdp.business.cache.datageneration.service.DMDataService;
import com.ericsson.fdp.business.cache.datageneration.service.ErrorCodesNotificationDataService;
import com.ericsson.fdp.business.cache.datageneration.service.FAFOfferService;
import com.ericsson.fdp.business.cache.datageneration.service.FDPProductCouponCodeCacheService;
import com.ericsson.fdp.business.cache.datageneration.service.FuzzyCodeCacheService;
import com.ericsson.fdp.business.cache.datageneration.service.Handset4GDataService;
import com.ericsson.fdp.business.cache.datageneration.service.NotificationDataService;
import com.ericsson.fdp.business.cache.datageneration.service.OfferAttributeDataService;
import com.ericsson.fdp.business.cache.datageneration.service.PolicyDataService;
import com.ericsson.fdp.business.cache.datageneration.service.ProductAttributeMapService;
import com.ericsson.fdp.business.cache.datageneration.service.ProductCategoryExpressionService;
import com.ericsson.fdp.business.cache.datageneration.service.ProductChargingDiscount;
import com.ericsson.fdp.business.cache.datageneration.service.ProductDataService;
import com.ericsson.fdp.business.cache.datageneration.service.ProductNameDataService;
import com.ericsson.fdp.business.cache.datageneration.service.RollBackCommandCacheService;
import com.ericsson.fdp.business.cache.datageneration.service.SPDataService;
import com.ericsson.fdp.business.cache.datageneration.service.TariffEnquiryAttributesExpressionService;
import com.ericsson.fdp.business.cache.datageneration.service.TariffEnquiryAttributesService;
import com.ericsson.fdp.business.cache.datageneration.service.TariffService;
import com.ericsson.fdp.business.cache.datageneration.service.TimeToShareOfferService;
import com.ericsson.fdp.business.cache.impl.MetaDataCacheInitializationRunnableService;
import com.ericsson.fdp.business.util.MetaCacheInitializerUtil;
import com.ericsson.fdp.common.dto.TrapError;
import com.ericsson.fdp.common.enums.ModuleType;
import com.ericsson.fdp.common.enums.trap.TrapErrorCodes;
import com.ericsson.fdp.common.enums.trap.TrapSeverity;
import com.ericsson.fdp.common.exception.FDPServiceException;
import com.ericsson.fdp.common.logging.Event;
import com.ericsson.fdp.common.vo.FDPCircle;
import com.ericsson.fdp.core.dsm.framework.service.CacheLoadState;
import com.ericsson.fdp.core.dsm.framework.service.FDPCache;
import com.ericsson.fdp.core.entity.service.EntityService;
import com.ericsson.fdp.core.logging.FDPLoggerFactory;
import com.ericsson.fdp.core.serviceprovisioning.bean.FDPMetaBag;
import com.ericsson.fdp.core.utils.SNMPUtil;

@Stateless
public class MetaCacheInitializer extends AbstractCacheInitializer {

	private static final Logger LOGGER = LoggerFactory.getLogger(MetaCacheInitializer.class);

	@Resource(lookup = "java:app/fdpBusiness-1.0/OfferAttributeDataServiceImpl")
	private OfferAttributeDataService offerattributedataservice;
	
/*	@Resource(lookup = "java:app/fdpBusiness-1.0/PAMAttributesDataServiceImpl")
	private PAMAttributesDataService pamAttributesDataService;*/
	
	/** The fdp cache. */
	@Resource(lookup = "java:app/fdpBusiness-1.0/MetaDataCache")
	private FDPCache<FDPMetaBag, FDPCacheable> fdpCache;

	@Resource(lookup = "java:app/fdpBusiness-1.0/SPDataServiceImpl")
	private SPDataService spDataService;

	@Resource(lookup = "java:app/fdpBusiness-1.0/CommandDataServiceImpl")
	private CommandDataService commandDataService;

	@Resource(lookup = "java:app/fdpBusiness-1.0/ProductDataServiceImpl")
	private ProductDataService productDataService;
	
	// add on 20 sept
	@Resource(lookup = "java:app/fdpBusiness-1.0/ProductNameDataServiceImpl")
	private ProductNameDataService productNameDataService;

	@Resource(lookup = "java:app/fdpBusiness-1.0/DMDataServiceImpl")
	private DMDataService dmDataService;

	@Resource(lookup = "java:app/fdpBusiness-1.0/NotificationDataServiceImpl")
	private NotificationDataService notificationDataServiceImpl;

	@Resource(lookup = "java:app/fdpBusiness-1.0/PolicyDataServiceImpl")
	private PolicyDataService policyDataService;

	@Resource(lookup = "java:app/fdpBusiness-1.0/RollBackCommandCacheServiceImpl")
	private RollBackCommandCacheService rollBackCommandCacheService;

	@Resource(lookup = "java:app/fdpBusiness-1.0/AIRRechargeDataServiceImpl")
	private AIRRechargeDataService airRechargeDataService;

	@Resource(lookup = "java:app/fdpBusiness-1.0/TariffEnquiryDisplayFormatCacheImpl")
	private TariffService tariffService;

	@Resource(lookup = "java:app/fdpBusiness-1.0/TariffEnquiryAttributesCacheImpl")
	private TariffEnquiryAttributesService tariffEnquiryAttributesService;

	/** The error codes notification data service. */
	@Resource(lookup = "java:global/fdpBusiness-ear/fdpBusiness-1.0/ErrorCodesNotificationDataServiceImpl")
	ErrorCodesNotificationDataService errorCodesNotificationDataService;

	@Resource(lookup = "java:global/fdpBusiness-ear/fdpBusiness-1.0/TariffEnquiryAttributesExpressionCacheImpl")
	private TariffEnquiryAttributesExpressionService tariffEnquiryAttributesExpressionService;
	
	@Resource(lookup ="java:global/fdpBusiness-ear/fdpBusiness-1.0/FDPProductCouponCodeCacheServiceImpl")
	private FDPProductCouponCodeCacheService productCouponCodeService;
	
	
	@Resource(lookup = "java:app/fdpBusiness-1.0/ProductCategoryExpressionServiceImpl")
	private ProductCategoryExpressionService productCategoryExpressionService;

	@Resource(lookup = "java:app/fdpBusiness-1.0/FuzzyCodeCacheServiceImpl")
	private FuzzyCodeCacheService fuzzyCodeCacheService;
	
	@Resource(lookup = "java:app/fdpBusiness-1.0/AirConfigCacheImpl")
	private AirConfigCacheService airConfigCache;
	
	/** The entity service. */
	@Resource(lookup = "java:app/fdpCoreServices-1.0/EntityServiceImpl")
	private EntityService entityService;

	/** The circle list. */
	private List<FDPCircle> circleList;

	@Resource(lookup = "java:app/fdpBusiness-1.0/ProductChargingDiscountImpl")
	private ProductChargingDiscount productChargingDiscount;

	@Resource(lookup = "java:global/fdpBusiness-ear/fdpBusiness-1.0/FAFOfferServiceImpl")
	private FAFOfferService fafOfferService;	
	@Resource(lookup = "java:global/fdpBusiness-ear/fdpBusiness-1.0/TimeToShareOfferServiceImpl")
	private TimeToShareOfferService timeToShareOfferService;

	@Resource(lookup = "java:global/fdpBusiness-ear/fdpBusiness-1.0/ProductAttributeMapServiceImpl")
	private ProductAttributeMapService productAttributeMapService;
	
	@Resource(lookup = "java:global/fdpBusiness-ear/fdpBusiness-1.0/FDPAsyncCommandCacheImpl")
	private AsyncCommandCacheService asycCommandCacheService;
	
	@Resource(lookup = "java:global/fdpBusiness-ear/fdpBusiness-1.0/Handset4GDataServiceImpl")
	private Handset4GDataService handset4GDataService;

	@Override
	public boolean initializeCache() {
		boolean isMetaCacheInitialized = false;
		LOGGER.info("Initializing MetaCache...");
		long startTime = System.currentTimeMillis();
		try {

			
			circleList = entityService.getActiveCircleVOs();
			ExecutorService executorService = Executors.newFixedThreadPool(5);

			executorService.execute(new MetaDataCacheInitializationRunnableService(spDataService, getCircles(),
					ModuleType.SP_PRODUCT));

			executorService.execute(new MetaDataCacheInitializationRunnableService(productDataService, getCircles(),
					ModuleType.PRODUCT));
			// Added on 20 sept
			executorService.execute(new MetaDataCacheInitializationRunnableService(productNameDataService, getCircles(),
					ModuleType.PRODUCT_NAME_ID_MAP));
			//

			executorService.execute(new MetaDataCacheInitializationRunnableService(dmDataService, getCircles(),
					ModuleType.DM));
			
			executorService.execute(new MetaDataCacheInitializationRunnableService(productChargingDiscount, getCircles(),
					ModuleType.PRODUCT_CHARGING_DISCOUNT));

			executorService.execute(new MetaDataCacheInitializationRunnableService(offerattributedataservice,getCircles(),
					ModuleType.OFFER_ATTRIBUTE));
			//executorService.execute(new MetaDataCacheInitializationRunnableService(pamAttributesDataService, getCircles(), ModuleType.PAM_OFFER_ATTRIBUTE));
			
			executorService.execute(new MetaDataCacheInitializationRunnableService(fuzzyCodeCacheService,getCircles(),
					ModuleType.FUZZY_CODES));
			
			
	      executorService.execute(new MetaDataCacheInitializationRunnableService(airConfigCache,getCircles(),
					ModuleType.AIR_CONFIG));
			
	      executorService.execute(new MetaDataCacheInitializationRunnableService(fafOfferService, getCircles(),
					ModuleType.FAF_OFFER));
	      
	      executorService.execute(new MetaDataCacheInitializationRunnableService(timeToShareOfferService, getCircles(),
					ModuleType.TIME_TO_SHARE));
	      
	      executorService.execute(new MetaDataCacheInitializationRunnableService(asycCommandCacheService,getCircles(),ModuleType.ASYNC_COMMANDS));
	      
		  executorService.execute(new MetaDataCacheInitializationRunnableService(productAttributeMapService, getCircles(), 
	    		  	ModuleType.PRODUCT_ATTRIBUTE_MAP));
		  
	      executorService.execute(new MetaDataCacheInitializationRunnableService(handset4GDataService, getCircles(),
					ModuleType.HANDSET_4G));
	      
			executorService.execute(new Runnable() {

				@Override
				public void run() {
					Thread.currentThread().setName("Thread-Other-Modules");
					try {
						long startTime = System.currentTimeMillis();
						MetaCacheInitializerUtil.initializeCacheForModule(commandDataService, ModuleType.COMMAND,
								getCircles());

						long initializeUpdateCommandCache = System.currentTimeMillis();

					
						MetaCacheInitializerUtil.initializeCacheForModule(notificationDataServiceImpl,
								ModuleType.NOTIFICATION, getCircles());

						long initializeUpdateNotificationCache = System.currentTimeMillis();
						
						policyDataService.initializeUpdatePolicyCache();
						long initializeUpdatePolicyCache = System.currentTimeMillis();
						
						MetaCacheInitializerUtil.initializeCacheForModule(airRechargeDataService,
								ModuleType.AIR_RECHARGE, getCircles());
						long initializeUpdateAIRRechargeCache = System.currentTimeMillis();
						
						rollBackCommandCacheService.initializeRollBackCommandCache();
						long initializeRollBackCommandCache = System.currentTimeMillis();
						
						MetaCacheInitializerUtil.initializeCacheForModule(tariffService,
								ModuleType.UNIT_DISPLAY_FORMAT, getCircles());
						long initializeTariffEnquiryDisplayformatCache = System.currentTimeMillis();
						
						MetaCacheInitializerUtil.initializeCacheForModule(tariffEnquiryAttributesService,
								ModuleType.TARIFF_ENQUIRY_ATTRIBUTES, getCircles());
						long initializeTariffEnquiryAttributesCache = System.currentTimeMillis();
						
						MetaCacheInitializerUtil.initializeCacheForModule(errorCodesNotificationDataService,
								ModuleType.RESPONSE_CODE_NOTIFICATION_MAPPING, getCircles());
						long initializeUpdateResponseCodeNotificationCache = System.currentTimeMillis();
						
						MetaCacheInitializerUtil.initializeCacheForModule(tariffEnquiryAttributesExpressionService,
								ModuleType.TARIFF_ENQUIRY_ATTRIBUTE_EXPRESSION, getCircles());
						long initializeTariffEnquiryAttributesExpressionCache = System.currentTimeMillis();
						
						
						MetaCacheInitializerUtil.initializeCacheForModule(productCouponCodeService,
								ModuleType.PRODUCT_COUPON_CODE, getCircles());
						long initializeProductCouponServiceCache = System.currentTimeMillis();
						
												
						MetaCacheInitializerUtil.initializeCacheForModule(productCategoryExpressionService,
								ModuleType.PRODUCT_CATEGORY, getCircles());
						long initializeProductCategoryExpressionCache = System.currentTimeMillis();
						

						
					} catch (FDPServiceException e) {
						throw new RuntimeException("Could not initialize meta cache ", e);
					}
				}
			});

			executorService.shutdown();

			while (!executorService.isTerminated()) {
				//Waiting for termination.
			}

			FDPLoggerFactory.getGenerateAlarmLogger().warn(
					"",
					new Event(TrapSeverity.CLEAR, new TrapError(TrapErrorCodes.FAILED_TO_INITILIZE_META_CACHE),
							SNMPUtil.getIPAddess()));
			isMetaCacheInitialized = true;
		} catch (Exception e) {
			isMetaCacheInitialized = false;
			FDPLoggerFactory.getGenerateAlarmLogger().warn(
					"",
					new Event(TrapSeverity.CRITICAL, new TrapError(TrapErrorCodes.FAILED_TO_INITILIZE_META_CACHE),
							SNMPUtil.getIPAddess()));
			LOGGER.error("Failed to initialize MetaCache", e);
			throw new RuntimeException("Failed to initialize MetaCache", e);
		}
		long endTime = System.currentTimeMillis();
		
		LOGGER.info("MetaCache Initialized successfully...");
		return isMetaCacheInitialized;
	}

	private List<FDPCircle> getCircles() {
		if (this.circleList == null) {
			this.circleList = entityService.getActiveCircleVOs();
		}
		return this.circleList;
	}

	@Override
	public boolean isCacheNeedsLoading() {
		return fdpCache.isCacheNeedsLoading();
	}

	@Override
	public CacheLoadState setCacheStatus(CacheLoadState newStatus) {
		CacheLoadState oldState = fdpCache.setCacheStatus(newStatus);
		LOGGER.info("MetaCache State Change from "+oldState+" to "+newStatus);
		return oldState;
	}

}
