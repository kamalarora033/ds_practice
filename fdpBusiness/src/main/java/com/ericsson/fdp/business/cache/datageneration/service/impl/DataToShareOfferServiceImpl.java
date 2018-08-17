package com.ericsson.fdp.business.cache.datageneration.service.impl;

import java.util.List;

import javax.annotation.Resource;
import javax.ejb.Stateless;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ericsson.fdp.FDPCacheable;
import com.ericsson.fdp.business.cache.datageneration.service.TimeToShareOfferService;
import com.ericsson.fdp.business.tts.TimeToShareOffer;
import com.ericsson.fdp.business.tts.impl.TimeToShare;
import com.ericsson.fdp.common.enums.ModuleType;
import com.ericsson.fdp.common.exception.FDPServiceException;
import com.ericsson.fdp.common.vo.FDPCircle;
import com.ericsson.fdp.core.dsm.framework.service.FDPCache;
import com.ericsson.fdp.core.entity.service.EntityService;
import com.ericsson.fdp.core.serviceprovisioning.bean.FDPMetaBag;
import com.ericsson.fdp.dao.dto.UpdateCacheDTO;
import com.ericsson.fdp.dao.dto.sharebaseoffer.ShareBaseOfferDTO;
import com.ericsson.fdp.dao.enums.ActionTypeEnum;
import com.ericsson.fdp.business.cache.datageneration.service.DataToShareOfferService;
/**
 * The Class TimeToShareOfferServiceImpl.
 * 
 * @author Ericsson
 * 
 */
@Stateless(mappedName = "DataToShareOfferServiceImpl")
public class DataToShareOfferServiceImpl implements DataToShareOfferService {

	/** The Constant LOGGER. */
	private static final Logger LOGGER = LoggerFactory.getLogger(DataToShareOfferServiceImpl.class);

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
				// delete the cache object
				if (ModuleType.DATA_TO_SHARE.equals(updateCacheDTO.getModuleType())) {
					return fdpCache.removeKey(new FDPMetaBag(updateCacheDTO
							.getCircle(), ModuleType.DATA_TO_SHARE, id));
				}
			}
			return initializeUpdateFaFCache(id, updateCacheDTO.getCircle());
		}
		return false;
	}

	
	@Override
	public boolean initializeMetaCache(FDPCircle fdpCircle)
			throws FDPServiceException {
		if (fdpCircle == null) {
			throw new FDPServiceException("Data to Share cache initialization: Circle cannot be null");
		} else {
			return this.initializeUpdateFaFCache(null, fdpCircle);
		}
	}

	@Override
	public ModuleType getModuleType() {
		return ModuleType.TIME_TO_SHARE;
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
	private boolean initializeUpdateFaFCache(final Long id,
			final FDPCircle fdpCircle) {

		final List<ShareBaseOfferDTO> shareBaseOfferList;

		if (id == null) {
			shareBaseOfferList = entityService.getTTSOfferListByCircle(fdpCircle);
		} else {
			shareBaseOfferList = entityService.getTTSOfferList(id);
		}

		for (final ShareBaseOfferDTO shareBaseOfferDTO : shareBaseOfferList) {
			LOGGER.debug("Updating cache for Data to Share : {}",
					shareBaseOfferDTO.getMe2u_share_products_id());
			this.initializeUpdateTTSOffer(shareBaseOfferDTO,
					fdpCircle);
			LOGGER.debug("Cache updated for Data to Share : {}",
					shareBaseOfferDTO.getMe2u_share_products_id());
		}

		return true;
	}

	
	private void initializeUpdateTTSOffer(final ShareBaseOfferDTO shareBaseOfferDTO, final FDPCircle fdpCircle) {
		final FDPCircle circle = fdpCircle;
		
		final TimeToShareOffer offer=new TimeToShare(shareBaseOfferDTO);
		
		FDPMetaBag metaBag = new FDPMetaBag(circle, ModuleType.DATA_TO_SHARE,shareBaseOfferDTO.getMe2u_share_products_id());
		
		fdpCache.putValue(metaBag, offer);
		
		//System.out.println("####################:" + fdpCache.getValue(metaBag));
	}
}
