package com.ericsson.fdp.business.cache.datageneration.service.impl;

import java.util.List;

import javax.annotation.Resource;
import javax.ejb.Stateless;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ericsson.fdp.FDPCacheable;
import com.ericsson.fdp.business.cache.datageneration.service.Handset4GDataService;
import com.ericsson.fdp.business.vo.Handset4GDetailVO;
import com.ericsson.fdp.common.enums.ModuleType;
import com.ericsson.fdp.common.exception.FDPServiceException;
import com.ericsson.fdp.common.vo.FDPCircle;
import com.ericsson.fdp.core.dsm.framework.service.FDPCache;
import com.ericsson.fdp.core.entity.service.EntityService;
import com.ericsson.fdp.core.serviceprovisioning.bean.FDPMetaBag;
import com.ericsson.fdp.dao.dto.UpdateCacheDTO;
import com.ericsson.fdp.dao.dto.faf.ServiceClassHandset4G;
import com.ericsson.fdp.dao.dto.sharebaseoffer.FDP4GHandsetDTO;
import com.ericsson.fdp.dao.enums.ActionTypeEnum;

/**
 * The Class Handset4GDataServiceImpl.
 * 
 * @author Ericsson
 * 
 */
@Stateless(mappedName = "Handset4GServiceImpl")
public class Handset4GDataServiceImpl implements Handset4GDataService {

	/** The Constant LOGGER. */
	private static final Logger LOGGER = LoggerFactory.getLogger(Handset4GDataServiceImpl.class);

	/** The fdp cache. */
	@Resource(lookup = "java:app/fdpBusiness-1.0/MetaDataCache")
	private FDPCache<FDPMetaBag, FDPCacheable> fdpCache;

	/** The entity service. */
	@Resource(lookup = "java:app/fdpCoreServices-1.0/EntityServiceImpl")
	private EntityService entityService;

	@Override
	public boolean updateMetaCache(UpdateCacheDTO updateCacheDTO) throws FDPServiceException {
		Long id;
		if (updateCacheDTO != null) {
			id = updateCacheDTO.getId();

			if (ActionTypeEnum.DELETE.equals(updateCacheDTO.getAction())
					&& ModuleType.HANDSET_4G.equals(updateCacheDTO.getModuleType())
					&& null != updateCacheDTO.getUiObjectDTO()) {
				ServiceClassHandset4G serviceClassHandset4G = new ServiceClassHandset4G(
						((ServiceClassHandset4G) updateCacheDTO.getUiObjectDTO()).getHandsetId(),
						((ServiceClassHandset4G) updateCacheDTO.getUiObjectDTO()).getHandsetName());
				try {
					return fdpCache.removeKey(
							new FDPMetaBag(updateCacheDTO.getCircle(), ModuleType.HANDSET_4G, serviceClassHandset4G));
				} catch (Exception e) {
					LOGGER.error("Cache not updated successfully for Handset 4G.", e);
				}
			}
			return initializeUpdate4GCache(id, updateCacheDTO, updateCacheDTO.getCircle());
		}
		return false;
	}

	@Override
	public boolean initializeMetaCache(FDPCircle fdpCircle) throws FDPServiceException {
		if (fdpCircle == null) {
			throw new FDPServiceException("Handset 4G cache initialization: Circle cannot be null");
		} else {
			return this.initializeUpdate4GCache(null, null, fdpCircle);
		}
	}

	@Override
	public ModuleType getModuleType() {
		return ModuleType.HANDSET_4G;
	}

	/**
	 * Initialize update FaF Offer cache.
	 * 
	 * @param id
	 *            the id
	 * @param fdpCircle
	 *            the fdp circle
	 * @return true, if successful
	 */
	private boolean initializeUpdate4GCache(final Long id, UpdateCacheDTO updateCacheDTO, final FDPCircle fdpCircle) {

		final List<FDP4GHandsetDTO> handset4gList;

		if (id == null) {
			handset4gList = entityService.get4gHandsetListByCircle(fdpCircle);
		} else {
			handset4gList = entityService.get4GHandsetList(id);
		}

		for (final FDP4GHandsetDTO fDP4GHandsetDTO : handset4gList) {
			LOGGER.debug("Updating cache for Handset 4G  : {}", fDP4GHandsetDTO.getHandsetId());
			this.initializeUpdate4GHandset(fDP4GHandsetDTO, updateCacheDTO, fdpCircle);
			LOGGER.debug("Cache updated for Handset 4G : {}", fDP4GHandsetDTO.getHandsetId());
		}

		return true;
	}

	private void initializeUpdate4GHandset(final FDP4GHandsetDTO fDP4GHandsetDTO, UpdateCacheDTO updateCacheDTO,
			final FDPCircle fdpCircle) {
		final FDPCircle circle = fdpCircle;

		final Handset4GDetailVO handset4GVO = new Handset4GDetailVO(fDP4GHandsetDTO);

		ServiceClassHandset4G serviceClassHandset4G = new ServiceClassHandset4G(fDP4GHandsetDTO.getHandsetId(),
				fDP4GHandsetDTO.getHandsetName());
		FDPMetaBag metaBag = new FDPMetaBag(circle, ModuleType.HANDSET_4G, serviceClassHandset4G);
		fdpCache.putValue(metaBag, handset4GVO);
	}
}
