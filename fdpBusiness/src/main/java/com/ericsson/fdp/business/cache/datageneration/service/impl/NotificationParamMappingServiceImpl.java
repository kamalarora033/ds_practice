package com.ericsson.fdp.business.cache.datageneration.service.impl;

import java.util.List;

import javax.annotation.Resource;
import javax.ejb.Stateless;
import javax.inject.Inject;

import com.ericsson.fdp.FDPCacheable;
import com.ericsson.fdp.business.cache.datageneration.service.NotificationParamMappingService;
import com.ericsson.fdp.business.vo.FDPNotificationParamMapping;
import com.ericsson.fdp.common.enums.ModuleType;
import com.ericsson.fdp.common.exception.FDPServiceException;
import com.ericsson.fdp.common.vo.FDPCircle;
import com.ericsson.fdp.core.dsm.framework.service.FDPCache;
import com.ericsson.fdp.core.serviceprovisioning.bean.FDPMetaBag;
import com.ericsson.fdp.dao.dto.NotificationParamDTO;
import com.ericsson.fdp.dao.dto.UpdateCacheDTO;
import com.ericsson.fdp.dao.fdpadmin.FDPNotificationParamDAO;

@Stateless(mappedName = "NotificationParamMappingServiceImpl")
public class NotificationParamMappingServiceImpl implements NotificationParamMappingService {

	@Inject
	private  FDPNotificationParamDAO fdpNotificationParamDAO;
	
	@Resource(lookup = "java:app/fdpBusiness-1.0/MetaDataCache")
	private FDPCache<FDPMetaBag, FDPCacheable> fdpCache;
	
	@Override
	public boolean updateMetaCache(UpdateCacheDTO updateCacheDTO)
			throws FDPServiceException {
		
		updateCache(updateCacheDTO.getId(),updateCacheDTO.getCircle());
		return false;
	}

	@Override
	public boolean initializeMetaCache(FDPCircle fdpCircle)
			throws FDPServiceException {
		updateCache(null,fdpCircle);
		return false;
	}

	@Override
	public ModuleType getModuleType() {
		
		return ModuleType.NOTIFICATION_PARAM_MAPPING;
	}
	
	public void updateCache(final Long notificationParamid , final FDPCircle fdpCircle){
	/*	List<NotificationParamDTO> notificationParamDTOs = fdpNotificationParamDAO.getNotificationParamList(null);
		NotificationParamDTO duplicateNotificationParamDTO = null;
		for(NotificationParamDTO notificationParamDTOvo:notificationParamDTOs){
			if(notificationParamid.equals(notificationParamDTOvo.getNotificationParamId())){
				duplicateNotificationParamDTO = notificationParamDTOvo;
				break;
			}
		}
		FDPNotificationParamMapping fdpNotificationParamMapping = null;
		if(null != notificationParamid){
			fdpCache.removeKey(new FDPMetaBag(fdpCircle, ModuleType.NOTIFICATION_PARAM_MAPPING, notificationParamid));
			fdpNotificationParamMapping = new FDPNotificationParamMapping();
			fdpNotificationParamMapping.setNotificationParamId(duplicateNotificationParamDTO.getNotificationParamId());
			fdpNotificationParamMapping.setParamName(duplicateNotificationParamDTO.getParamName());
			fdpNotificationParamMapping.setParamValue(duplicateNotificationParamDTO.getParamValue());
			fdpNotificationParamMapping.setFeedType(duplicateNotificationParamDTO.getFeedType());
			fdpNotificationParamMapping.setPrimitiveType(duplicateNotificationParamDTO.getPrimitiveType());
			fdpNotificationParamMapping.setParamDisplay(duplicateNotificationParamDTO.getParamDisplay());
			fdpCache.putValue(new FDPMetaBag(fdpCircle, ModuleType.NOTIFICATION_PARAM_MAPPING,
					duplicateNotificationParamDTO.getNotificationParamId()), fdpNotificationParamMapping);
			System.out.println("inside cache======>>>>>"+fdpNotificationParamMapping.getParamDisplay()+fdpNotificationParamMapping.getFeedType());
		}else{
		for(NotificationParamDTO notificationParamDTO:notificationParamDTOs){
			
			fdpNotificationParamMapping = new FDPNotificationParamMapping();
			fdpNotificationParamMapping.setParamName(notificationParamDTO.getParamName());
			fdpNotificationParamMapping.setParamValue(notificationParamDTO.getParamValue());
			fdpNotificationParamMapping.setFeedType(notificationParamDTO.getFeedType());
			fdpNotificationParamMapping.setPrimitiveType(notificationParamDTO.getPrimitiveType());
			fdpNotificationParamMapping.setParamDisplay(notificationParamDTO.getParamDisplay());
			
			fdpCache.putValue(new FDPMetaBag(fdpCircle, ModuleType.NOTIFICATION_PARAM_MAPPING,
					notificationParamDTO.getNotificationParamId()), fdpNotificationParamMapping);
			System.out.println("inside cache======>>>>>"+fdpNotificationParamMapping.getParamDisplay()+fdpNotificationParamMapping.getFeedType());
		}
		}*/
	}
}
