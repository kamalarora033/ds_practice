package com.ericsson.fdp.business.cache.datageneration.service.impl;

import java.util.ArrayList;
import java.util.List;

import javax.annotation.Resource;
import javax.ejb.Stateless;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ericsson.fdp.FDPCacheable;
import com.ericsson.fdp.business.cache.datageneration.SPCacheUtil;
import com.ericsson.fdp.business.cache.datageneration.service.SPDataService;
import com.ericsson.fdp.business.notification.impl.ServiceProvisioningNotificationImpl;
import com.ericsson.fdp.business.rule.Rule;
import com.ericsson.fdp.business.serviceprovisioning.rule.ServiceProvisioningRule;
import com.ericsson.fdp.business.step.FDPStep;
import com.ericsson.fdp.common.enums.ModuleType;
import com.ericsson.fdp.common.exception.FDPServiceException;
import com.ericsson.fdp.common.vo.FDPCircle;
import com.ericsson.fdp.core.dsm.framework.service.FDPCache;
import com.ericsson.fdp.core.entity.service.EntityService;
import com.ericsson.fdp.core.serviceprovisioning.bean.FDPMetaBag;
import com.ericsson.fdp.dao.dto.UpdateCacheDTO;
import com.ericsson.fdp.dao.dto.serviceprov.ServiceProvDTO;
import com.ericsson.fdp.dao.dto.serviceprov.ServiceProvNotificationDTO;
import com.ericsson.fdp.dao.dto.serviceprov.ServiceProvProductDTO;
import com.ericsson.fdp.dao.dto.serviceprov.StepChargingDTO;
import com.ericsson.fdp.dao.dto.serviceprov.StepCommandDTO;
import com.ericsson.fdp.dao.dto.serviceprov.StepConstraintDTO;
import com.ericsson.fdp.dao.dto.serviceprov.StepDTO;
import com.ericsson.fdp.dao.dto.serviceprov.StepDataBaseDTO;
import com.ericsson.fdp.dao.dto.serviceprov.StepNotificationDTO;
import com.ericsson.fdp.dao.dto.serviceprov.StepNotificationOtherDTO;
import com.ericsson.fdp.dao.dto.serviceprov.StepSPServiceDTO;
import com.ericsson.fdp.dao.dto.serviceprov.StepValidationDTO;
import com.ericsson.fdp.dao.enums.ActionTypeEnum;
import com.ericsson.fdp.dao.enums.FDPServiceProvSubType;
import com.ericsson.fdp.dao.enums.FDPServiceProvType;
import com.ericsson.fdp.dao.util.ServiceProvDataConverterUtil;

/**
 * The Class SPDataServiceImpl.
 *
 * @author Ericsson
 */
@Stateless(mappedName = "sPDataService")
public class SPDataServiceImpl implements SPDataService {

	/** The Constant LOGGER. */
	private static final Logger LOGGER = LoggerFactory.getLogger(SPDataServiceImpl.class);

	/** The fdp cache. */
	@Resource(lookup = "java:app/fdpBusiness-1.0/MetaDataCache")
	private FDPCache<FDPMetaBag, FDPCacheable> fdpCache;

	/** The entity service. */
	@Resource(lookup = "java:app/fdpCoreServices-1.0/EntityServiceImpl")
	private EntityService entityService;

	/**
	 * Inits the.
	 */
	// @PostConstruct
	public void init() {
		// initializeUpdateSPCache(null);
	}

	@Override
	public boolean updateMetaCache(final UpdateCacheDTO updateCacheDTO) throws FDPServiceException {
		// spId in case of OTHERS and productId in Product
		Long id = null;
		if (updateCacheDTO != null) {
			id = updateCacheDTO.getId();
			if (ActionTypeEnum.DELETE.equals(updateCacheDTO.getAction())) {
				// delete the cache object(both for product and other)
				if (ModuleType.SP_PRODUCT.equals(updateCacheDTO.getModuleType())) {
					if (updateCacheDTO.getSpSubType() != null) {
						return fdpCache.removeKey(new FDPMetaBag(updateCacheDTO.getCircle(), ModuleType.SP_PRODUCT,
								ServiceProvDataConverterUtil.getKeyForServiceProvMetaBag(id,
										FDPServiceProvType.PRODUCT, updateCacheDTO.getSpSubType())));
					} else {
						return removeAllSpForProduct(updateCacheDTO);
					}
				} else {
					return fdpCache.removeKey(new FDPMetaBag(updateCacheDTO.getCircle(), ModuleType.SP_OTHERS,
							ServiceProvDataConverterUtil
									.getKeyForServiceProvMetaBag(id, FDPServiceProvType.OTHER, null)));
				}

			}

			return initializeUpdateSPCache(id, updateCacheDTO.getCircle());
		}
		return false;
	}

	@Override
	public boolean initializeMetaCache(final FDPCircle fdpCircle) throws FDPServiceException {
		if (fdpCircle == null) {
			throw new FDPServiceException("SP cache initialization: Circle cannot be null");
		} else {
			return this.initializeUpdateSPCache(null, fdpCircle);
		}
	}

	/**
	 * Initialize update sp cache.
	 *
	 * @param id
	 *            the id
	 * @param fdpCircle
	 *            the fdp circle
	 * @return true, if successful
	 */
	private boolean initializeUpdateSPCache(final Long id, final FDPCircle fdpCircle) {

		final List<ServiceProvDTO> activeServiceProvList;
		if (id == null) {
			activeServiceProvList = entityService.getServiceProvisionings(fdpCircle);
		} else {
			activeServiceProvList = entityService.getServiceProvisionings(id);
		}

		for (final ServiceProvDTO serviceProvDTO : activeServiceProvList) {
			LOGGER.debug("Updating cache for SP : {}", serviceProvDTO.getName());
			try{
				this.initializeUpdateSPRule(serviceProvDTO, serviceProvDTO.getFDPCircle());
				LOGGER.debug("Cache updated for SP : {}", serviceProvDTO.getName());
			}catch(Exception ex){
				LOGGER.debug("Cache not updated for SP : {}", serviceProvDTO.getName());
			}
		}

		return true;
	}

	private boolean removeAllSpForProduct(final UpdateCacheDTO updateCacheDTO) {
		boolean removed = false;
		for (final FDPServiceProvSubType fdpServiceProvType : FDPServiceProvSubType.values()) {
			removed = fdpCache.removeKey(new FDPMetaBag(updateCacheDTO.getCircle(), ModuleType.SP_PRODUCT,
					ServiceProvDataConverterUtil.getKeyForServiceProvMetaBag(updateCacheDTO.getId(),
							FDPServiceProvType.PRODUCT, fdpServiceProvType)))
					| removed;
		}
		return removed;
	}

	/**
	 * Initialize Or updates the whole Service provisioning rule and its
	 * children.
	 *
	 * @param serviceProvDTO
	 *            service provisioning rule DTO>
	 * @param fdpCircle
	 *            FDPCircle.
	 */
	private void initializeUpdateSPRule(final ServiceProvDTO serviceProvDTO, final FDPCircle fdpCircle) {
		final List<FDPStep> steps = new ArrayList<FDPStep>();
		final FDPCircle circle = fdpCircle;
		ServiceProvisioningNotificationImpl fdpServiceProvisioningNotification = null;
		final List<StepDTO> serviceProvStepsList = serviceProvDTO.getServiceProvStepList();
		if (serviceProvStepsList != null && !serviceProvStepsList.isEmpty()) {
			for (final StepDTO step : serviceProvStepsList) {
				if (step instanceof StepCommandDTO) {
					final StepCommandDTO cmdStep = (StepCommandDTO) step;
					if (FDPServiceProvType.OTHER.equals(((ServiceProvProductDTO) serviceProvDTO).getSpType())) {
						SPCacheUtil.populateCommandStepForOthers(steps, cmdStep);
					} else {
						SPCacheUtil.populateCommandStep(steps, cmdStep,LOGGER);
					}
				} else if (step instanceof StepConstraintDTO) {
					SPCacheUtil.populateConstraintStep(steps, (StepConstraintDTO) step,LOGGER);
				} else if (step instanceof StepValidationDTO) {
					// populate validation step
					SPCacheUtil.populateValidationStep(steps, (StepValidationDTO) step,
							((ServiceProvProductDTO) serviceProvDTO).getProductId());
				} else if (step instanceof StepChargingDTO) {
					SPCacheUtil.populateChargingStep(steps, (StepChargingDTO) step, ((ServiceProvProductDTO) serviceProvDTO)
							.getSpSubType().getChargingType());
				} else if (step instanceof StepDataBaseDTO) {
					// populates database step here
					SPCacheUtil.populateDatabaseStep(steps, (StepDataBaseDTO) step,
							((ServiceProvProductDTO) serviceProvDTO).getProductId());
				} else if (step instanceof StepNotificationOtherDTO) {
					// populates other notifications here
					SPCacheUtil.populateOfflineNotificationStep(steps, (StepNotificationOtherDTO) step);
				} else if (step instanceof StepNotificationDTO) {
					// populates notifications here
					final StepNotificationDTO notificationStep = (StepNotificationDTO) step;
					final List<ServiceProvNotificationDTO> spNotifications = notificationStep.getSpNotificationList();
					fdpServiceProvisioningNotification = (ServiceProvisioningNotificationImpl) SPCacheUtil
							.getPopulateNotifications(spNotifications);
				} else if (step instanceof StepSPServiceDTO) {
					// populates other notifications here
					SPCacheUtil.populateSPServiceStep(steps, (StepSPServiceDTO) step);
				}
			}
		}

		final Rule fdpRule = new ServiceProvisioningRule(steps, fdpServiceProvisioningNotification,
				serviceProvDTO.isRollBack(),serviceProvDTO);
		FDPMetaBag metaBag;
		if (FDPServiceProvType.OTHER.equals(((ServiceProvProductDTO) serviceProvDTO).getSpType())) {
			metaBag = new FDPMetaBag(circle, ModuleType.SP_OTHERS,
					ServiceProvDataConverterUtil.getKeyForServiceProvMetaBag(serviceProvDTO.getId(),
							FDPServiceProvType.OTHER, null));
		} else {
			final ServiceProvProductDTO spProductDTO = (ServiceProvProductDTO) serviceProvDTO;
			metaBag = new FDPMetaBag(circle, ModuleType.SP_PRODUCT,
					ServiceProvDataConverterUtil.getKeyForServiceProvMetaBag(spProductDTO.getProductId(),
							FDPServiceProvType.PRODUCT, spProductDTO.getSpSubType()));
		}
		fdpCache.putValue(metaBag, fdpRule);
	}

	

	@Override
	public void refreshDataCache() {

		entityService.clear();
		final List<Long> activeServiceProvListForRefresh = entityService.getSPForRefresh();

		for (Long long1 : activeServiceProvListForRefresh) {
			final List<ServiceProvDTO> activeServiceProvList2 = entityService.getServiceProvisionings(long1);
			for (final ServiceProvDTO serviceProvDTO : activeServiceProvList2) {
				LOGGER.debug("Updating cache for SP : {}", serviceProvDTO.getName());
				/*if (serviceProvDTO.getName().contains("UPE_Voice_1481")) {
					System.out.println("Important");
				}*/
				this.initializeUpdateSPRule(serviceProvDTO, serviceProvDTO.getFDPCircle());
				LOGGER.debug("Cache updated for SP : {}", serviceProvDTO.getName());
			}
		}


	}

	@Override
	public ModuleType getModuleType() {
		return ModuleType.SP_PRODUCT;
	}
}
