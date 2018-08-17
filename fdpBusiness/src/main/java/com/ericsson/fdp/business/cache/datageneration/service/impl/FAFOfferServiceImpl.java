package com.ericsson.fdp.business.cache.datageneration.service.impl;

import java.util.List;

import javax.annotation.Resource;
import javax.ejb.Stateless;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ericsson.fdp.FDPCacheable;
import com.ericsson.fdp.business.cache.datageneration.service.FAFOfferService;
import com.ericsson.fdp.business.fnf.Offer;
import com.ericsson.fdp.business.fnf.impl.FnFOffer;
import com.ericsson.fdp.common.enums.ModuleType;
import com.ericsson.fdp.common.exception.FDPServiceException;
import com.ericsson.fdp.common.vo.FDPCircle;
import com.ericsson.fdp.core.dsm.framework.service.FDPCache;
import com.ericsson.fdp.core.entity.service.EntityService;
import com.ericsson.fdp.core.serviceprovisioning.bean.FDPMetaBag;
import com.ericsson.fdp.dao.dto.UpdateCacheDTO;
import com.ericsson.fdp.dao.dto.faf.ServiceClassOffer;
import com.ericsson.fdp.dao.dto.sharebaseoffer.FDPFnFOfferDetailsDTO;
import com.ericsson.fdp.dao.enums.ActionTypeEnum;

/**
 * The Class FAFOfferServiceImpl.
 * 
 * @author Ericsson
 * 
 */
@Stateless(mappedName = "FAFOfferServiceImpl")
public class FAFOfferServiceImpl implements FAFOfferService {

	/** The Constant LOGGER. */
	private static final Logger LOGGER = LoggerFactory.getLogger(FAFOfferServiceImpl.class);

	/** The fdp cache. */
	@Resource(lookup = "java:app/fdpBusiness-1.0/MetaDataCache")
	private FDPCache<FDPMetaBag, FDPCacheable> fdpCache;

	/** The entity service. */
	@Resource(lookup = "java:app/fdpCoreServices-1.0/EntityServiceImpl")
	private EntityService entityService;



@Override
public boolean updateMetaCache(final UpdateCacheDTO updateCacheDTO) throws FDPServiceException {
	Long id = null;
	if (updateCacheDTO != null) {
		id = updateCacheDTO.getId();

		if (ActionTypeEnum.DELETE.equals(updateCacheDTO.getAction())) {
			// delete the cache object(both for product and other)
			if (ModuleType.FAF_OFFER.equals(updateCacheDTO.getModuleType())) {
				if (null != updateCacheDTO.getUiObjectDTO()) {
						ServiceClassOffer serviceClassOffer = new ServiceClassOffer(
								((ServiceClassOffer) updateCacheDTO.getUiObjectDTO()).getServiceClass(),
								((ServiceClassOffer) updateCacheDTO.getUiObjectDTO()).getBaseOfferId());
						try {
							return fdpCache.removeKey(new FDPMetaBag(updateCacheDTO.getCircle(), ModuleType.FAF_OFFER,
									serviceClassOffer));
						} catch (Exception e) {
							LOGGER.error("Cache not updated successfully.", e);
					}
				}
			}
		}
		return initializeUpdateFaFCache(id, updateCacheDTO, updateCacheDTO.getCircle());
	}
	return false;
}

	@Override
	public boolean initializeMetaCache(FDPCircle fdpCircle) throws FDPServiceException {
		if (fdpCircle == null) {
			throw new FDPServiceException("FaF cache initialization: Circle cannot be null");
		} else {
			return this.initializeUpdateFaFCache(null, null, fdpCircle);
		}
	}

	@Override
	public ModuleType getModuleType() {
		return ModuleType.FAF_OFFER;
	}

	@Override
	public void refreshDataCache() {
		// TODO Auto-generated method stub

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
	private boolean initializeUpdateFaFCache(final Long id, UpdateCacheDTO updateCacheDTO, final FDPCircle fdpCircle) {

		final List<FDPFnFOfferDetailsDTO> fnFOfferList;

		if (id == null) {
			fnFOfferList = entityService.getFnFOfferListByCircle(fdpCircle);
		} else {
			fnFOfferList = entityService.getFnFOfferList(id);
		}

		for (final FDPFnFOfferDetailsDTO fnFOfferDTO : fnFOfferList) {
			LOGGER.debug("Updating cache for FaF Offer : {}", fnFOfferDTO.getFnfOfferId());
			this.initializeUpdateFnFOffer(fnFOfferDTO, updateCacheDTO, fdpCircle);
			LOGGER.debug("Cache updated for FaF Offer : {}", fnFOfferDTO.getFnfOfferId());
		}

		return true;
	}

	private void initializeUpdateFnFOffer(final FDPFnFOfferDetailsDTO fnFOfferDTO, UpdateCacheDTO updateCacheDTO,
			final FDPCircle fdpCircle) {
		final FDPCircle circle = fdpCircle;

		final Offer offer = new FnFOffer(fnFOfferDTO);

		if (null != updateCacheDTO) {
			if (null != updateCacheDTO.getUiObjectDTO()) {
				ServiceClassOffer serviceClassOffer = new ServiceClassOffer(
						((ServiceClassOffer) updateCacheDTO.getUiObjectDTO()).getServiceClass(),
						((ServiceClassOffer) updateCacheDTO.getUiObjectDTO()).getBaseOfferId());
				try {
					fdpCache.removeKey(new FDPMetaBag(updateCacheDTO.getCircle(), ModuleType.FAF_OFFER,
							serviceClassOffer));
				} catch (Exception e) {
					LOGGER.error("Cache not updated successfully.", e);
				}
			}
		}
		ServiceClassOffer serviceClassOfferObj = new ServiceClassOffer(fnFOfferDTO.getServiceClass(),
				fnFOfferDTO.getBaseOfferId());
		FDPMetaBag metaBag = new FDPMetaBag(circle, ModuleType.FAF_OFFER, serviceClassOfferObj);
		fdpCache.putValue(metaBag, offer);
	}
}
