package com.ericsson.fdp.business.cache.datageneration.service.impl;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import javax.annotation.Resource;
import javax.ejb.Stateless;
import javax.inject.Inject;

import com.ericsson.fdp.FDPCacheable;
import com.ericsson.fdp.business.cache.datageneration.service.TariffEnquiryAttributesService;
import com.ericsson.fdp.business.util.TariffEnquiryAttributesUtil;
import com.ericsson.fdp.common.enums.ModuleType;
import com.ericsson.fdp.common.exception.FDPServiceException;
import com.ericsson.fdp.common.vo.FDPCircle;
import com.ericsson.fdp.core.dsm.framework.service.FDPCache;
import com.ericsson.fdp.core.entity.FDPTariffValues;
import com.ericsson.fdp.core.entity.service.EntityService;
import com.ericsson.fdp.core.serviceprovisioning.bean.FDPMetaBag;
import com.ericsson.fdp.dao.dto.UpdateCacheDTO;
import com.ericsson.fdp.dao.dto.tariffenquiry.FDPTariffEnquiryAttributeDTO;
import com.ericsson.fdp.dao.tariffenquiry.FDPTariffEnquiryAttributesDAO;

/**
 * The Class TariffEnquiryAttributesCacheImpl.
 */
@Stateless(mappedName = "TariffEnquiryAttributesCacheImpl")
public class TariffEnquiryAttributesCacheImpl implements TariffEnquiryAttributesService {

	/** The fdp cache. */
	@Resource(lookup = "java:app/fdpBusiness-1.0/MetaDataCache")
	private FDPCache<FDPMetaBag, FDPCacheable> fdpCache;

	/** The FDPTariffEnquiryAttributesDAO. */
	@Inject
	private FDPTariffEnquiryAttributesDAO tariffEnquiryAttributesDAO;

	/** The entity service. */
	@Resource(lookup = "java:app/fdpCoreServices-1.0/EntityServiceImpl")
	private EntityService entityService;

	@Override
	public boolean updateMetaCache(final UpdateCacheDTO updateCacheDTO) throws FDPServiceException {
		if (updateCacheDTO == null || updateCacheDTO.getCircle() == null) {
			throw new FDPServiceException("UpdateCacheDTO cannot be null");
		} else {
			this.initializeTariffEnquiryAttributesCache(updateCacheDTO.getCircle().getCircleId());
		}
		return true;
	}

	@Override
	public boolean initializeMetaCache(final FDPCircle fdpCircle) throws FDPServiceException {
		if (fdpCircle == null) {
			throw new FDPServiceException("Circle cannot be null");
		} else {
			this.initializeTariffEnquiryAttributesCache(fdpCircle.getCircleId());
		}
		return true;
	}

	/**
	 * Initialize tariff enquiry attributes cache.
	 *
	 * @param circleId
	 *            the circle id
	 */
	private void initializeTariffEnquiryAttributesCache(final Long circleId) {
		List<FDPTariffEnquiryAttributeDTO> attributes = tariffEnquiryAttributesDAO.getCircleTariffAttributes(circleId);

		Map<FDPCircle, Map<String, Object>> cacheMap = new HashMap<FDPCircle, Map<String, Object>>();

		if (null != attributes && attributes.size() > 0) {
			for (final FDPTariffEnquiryAttributeDTO attributeDTO : attributes) {
				TariffEnquiryAttributesUtil.populateCacheMap(attributeDTO, cacheMap);
			}

			if (cacheMap.size() > 0) {
				for (final Entry<FDPCircle, Map<String, Object>> perCirleMap : cacheMap.entrySet()) {
					FDPCircle fdpCircle = perCirleMap.getKey();
					Map<String, Object> perCircleCache = perCirleMap.getValue();
					for (final Entry<String, Object> cache : perCircleCache.entrySet()) {
						String key = cache.getKey();
						FDPTariffValues value = new FDPTariffValues(cache.getValue());
						insertToCache(key, fdpCircle, value);
					}
				}
			}

		}

	}

	/**
	 * This method will finally put the VALUE in cache against the KEY.
	 *
	 * @param key
	 * @param fdpCircle
	 * @param attributesImpl
	 */
	private void insertToCache(final String key, final FDPCircle fdpCircle, final FDPTariffValues attributesImpl) {
		FDPMetaBag metaBag = new FDPMetaBag(fdpCircle, ModuleType.TARIFF_ENQUIRY_ATTRIBUTES, key);
		fdpCache.putValue(metaBag, attributesImpl);
	}

	@Override
	public ModuleType getModuleType() {
		return ModuleType.TARIFF_ENQUIRY_ATTRIBUTES;
	}
}
