package com.ericsson.fdp.business.cache.datageneration.service.impl;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.Map.Entry;

import javax.annotation.Resource;
import javax.ejb.Stateless;

import org.dozer.DozerBeanMapper;
import org.dozer.Mapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ericsson.fdp.FDPCacheable;
import com.ericsson.fdp.business.cache.datageneration.service.OfferAttributeDataService;
import com.ericsson.fdp.business.exception.ExpressionFailedException;
import com.ericsson.fdp.business.vo.FDPOfferAttributeVO;
import com.ericsson.fdp.business.vo.FDPOfferAttributeVOList;
import com.ericsson.fdp.business.vo.PAMOfferAttributesVO;
import com.ericsson.fdp.common.enums.ModuleType;
import com.ericsson.fdp.common.exception.FDPServiceException;
import com.ericsson.fdp.common.vo.FDPCircle;
import com.ericsson.fdp.core.dsm.framework.service.FDPCache;
import com.ericsson.fdp.core.entity.service.EntityService;
import com.ericsson.fdp.core.serviceprovisioning.bean.FDPMetaBag;
import com.ericsson.fdp.dao.dto.FDPOfferAttributeDTO;
import com.ericsson.fdp.dao.dto.UpdateCacheDTO;
import com.ericsson.fdp.dao.enums.ActionTypeEnum;

@Stateless(mappedName = "OfferAttributeDataService")
public class OfferAttributeDataServiceImpl implements OfferAttributeDataService{

	/** The Constant LOGGER. */
	private static final Logger LOGGER = LoggerFactory.getLogger(OfferAttributeDataServiceImpl.class);

	private static final Mapper MAPPER = new DozerBeanMapper();
	
	@Resource(lookup = "java:app/fdpCoreServices-1.0/EntityServiceImpl")
	private EntityService entityService;

	@Resource(lookup = "java:app/fdpBusiness-1.0/MetaDataCache")
	private FDPCache<FDPMetaBag, FDPCacheable> fdpCache;
	
	@Override
	public boolean updateMetaCache(UpdateCacheDTO updateCacheDTO)
			throws FDPServiceException {
		// spId in case of OTHERS and productId in Product

		Long id = null;
		if (updateCacheDTO != null) {
			id = updateCacheDTO.getId();
			if (ActionTypeEnum.DELETE.equals(updateCacheDTO.getAction())) {
						return fdpCache.removeKey(new FDPMetaBag(updateCacheDTO.getCircle(), ModuleType.OFFER_ATTRIBUTE,
								updateCacheDTO.getId()));
						
			}
		}
		try {
			updateCache(id, updateCacheDTO.getCircle());
		} catch (ExpressionFailedException e) {
			/*System.out.println("Failed in loading Cache "+ ModuleType.OFFER_ATTRIBUTE+
								updateCacheDTO.getId());*/
			LOGGER.info("Failed in loading Cache "+ ModuleType.OFFER_ATTRIBUTE+updateCacheDTO.getId());
		}
		return true;
	}

	@Override
	public boolean initializeMetaCache(FDPCircle fdpCircle)
			throws FDPServiceException {

			try {
				this.updateCache(null, fdpCircle);
			} catch (ExpressionFailedException e) {
				LOGGER.error(e.getMessage());
				throw new FDPServiceException(e);
			}
		
		return true;
	
	}

	private void updateCache(Long id, FDPCircle fdpCircle) throws ExpressionFailedException{
		// TODO Auto-generated method stub
		 List<FDPOfferAttributeDTO> fdpofferAttributeDTOlst = new ArrayList<FDPOfferAttributeDTO>();
		/* if(id!=null)
		 {
			 fdpofferAttributeDTOlst=entityService.getOfferAttribute(id,fdpCircle.getCircleId());
		 }*/
		if(fdpCircle!=null)
		{
			fdpofferAttributeDTOlst=entityService.getOfferAttribute(fdpCircle);
		}
		else
		{
			List<FDPCircle> fdpactivecircleList=entityService.getActiveCircleVOs();
			for (Iterator iterator = fdpactivecircleList.iterator(); iterator
					.hasNext();) {
				FDPCircle fdpCircletmp = (FDPCircle) iterator.next();
				fdpofferAttributeDTOlst.addAll(entityService.getOfferAttribute(fdpCircletmp));
			}
		}
		initializeUpdateOfferAttribute(fdpofferAttributeDTOlst,fdpCircle);
	}

	private void initializeUpdateOfferAttribute(List<FDPOfferAttributeDTO> fdpofferAttributeDTOlst,FDPCircle fdpCircle)
	{
		FDPMetaBag metaBag ;
		FDPMetaBag metaBagPam;
		List<FDPOfferAttributeVO> fdpOfferAttributeVOList = new ArrayList<FDPOfferAttributeVO>();
	//	metaBag= new FDPMetaBag(fdpCircle, ModuleType.OFFER_ATTRIBUTE_LIST, null);
		//fdpCache.putValue(metaBag, list);
		for (FDPOfferAttributeDTO fdpOfferAttributeDTO : fdpofferAttributeDTOlst) {
			
			try {
			FDPOfferAttributeVO fdpOfferAttributeVO = new FDPOfferAttributeVO();
			List<Integer> daIds = new ArrayList<>();
	
			//fdpOfferAttributeVO = MAPPER.map(fdpOfferAttributeDTO, FDPOfferAttributeVO.class);
			fdpOfferAttributeVO.setCirclecode(fdpOfferAttributeDTO.getCirclecode());
			fdpOfferAttributeVO.setFdp_offer_attribute_id(fdpOfferAttributeDTO.getFdp_offer_attribute_id());
			fdpOfferAttributeVO.setFdp_timer_offer_id(fdpOfferAttributeDTO.getFdp_timer_offer_id());
			fdpOfferAttributeVO.setNotification_uc_ut(fdpOfferAttributeDTO.getNotification_uc_ut());
			fdpOfferAttributeVO.setPamIndicators(fdpOfferAttributeDTO.getPamIndicators());
			fdpOfferAttributeVO.setPamPrefixes(fdpOfferAttributeDTO.getPamPrefixes());
			fdpOfferAttributeVO.setProductid(fdpOfferAttributeDTO.getProductid());
			fdpOfferAttributeVO.setUc_id(fdpOfferAttributeDTO.getUc_id());
			fdpOfferAttributeVO.setUt_id(fdpOfferAttributeDTO.getUt_id());
			if(fdpOfferAttributeDTO.getDa_id()!=null && !fdpOfferAttributeDTO.getDa_id().equals("")){
				if(fdpOfferAttributeDTO.getDa_id().indexOf('|')!=-1){
	
					String[] da_ids = fdpOfferAttributeDTO.getDa_id().split("\\|");
					if(null != da_ids){
						for(String da_id1 : da_ids){
							if(null!=da_id1 && !da_id1.equals(""))
								daIds.add(Integer.parseInt(da_id1));
						}
					}
				}else{
					daIds.add(Integer.parseInt(fdpOfferAttributeDTO.getDa_id()));
				}
			}else{
				daIds.add(null);
			}
			
			fdpOfferAttributeVO.setDa_id(daIds);
			fdpOfferAttributeVOList.add(fdpOfferAttributeVO);
			
			metaBag= new FDPMetaBag(fdpCircle, ModuleType.OFFER_ATTRIBUTE, fdpOfferAttributeDTO.getFdp_timer_offer_id());
			fdpCache.putValue(metaBag, fdpOfferAttributeVO);
			
			PAMOfferAttributesVO attributesVO = null;
			String pamIndicator = fdpOfferAttributeDTO.getPamIndicators();
			if(null != pamIndicator) {
				String[] pamIndicatorArr = pamIndicator.split("\\|");
				for(String pamIndi : pamIndicatorArr) {
					if(null != pamIndi && !pamIndi.equals("")) {
						metaBagPam = new FDPMetaBag(fdpCircle , ModuleType.PAM_OFFER_ATTRIBUTE, pamIndi);
						attributesVO = new PAMOfferAttributesVO();
						attributesVO.putValue(Integer.parseInt(pamIndi), fdpOfferAttributeDTO);
						fdpCache.putValue(metaBagPam, attributesVO);
					}
				}
			}
			}
			catch(Exception e) {
				//System.out.println("Problem occur in cache for timer offer id :: "+fdpOfferAttributeDTO.getFdp_timer_offer_id());
				LOGGER.info("Problem occur in cache for timer offer id :: "+fdpOfferAttributeDTO.getFdp_timer_offer_id());
			}
		}
		
		FDPOfferAttributeVOList fdpOfferAttributeVOObject = new FDPOfferAttributeVOList();
		fdpOfferAttributeVOObject.setFdpOfferAttributeVO(fdpOfferAttributeVOList);
		metaBag= new FDPMetaBag(fdpCircle, ModuleType.OFFER_ATTRIBUTE_LIST, null);
		fdpCache.putValue(metaBag, fdpOfferAttributeVOObject);
		
	}
	
	
	@Override
	public ModuleType getModuleType() {
		// TODO Auto-generated method stub
		 return ModuleType.OFFER_ATTRIBUTE;
	}

	public List<FDPOfferAttributeDTO> getFDPOffers(FDPCircle FDPCircle)
	{
		return entityService.getOfferAttribute(FDPCircle);
	}
	
}
