package com.ericsson.fdp.business.cache.datageneration.service.impl;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import javax.annotation.Resource;
import javax.ejb.Stateless;
import javax.inject.Inject;

import org.slf4j.LoggerFactory;

import com.ericsson.fdp.FDPCacheable;
import com.ericsson.fdp.business.cache.datageneration.service.NotificationDataService;
import com.ericsson.fdp.business.enums.NotificationForAllCircle;
import com.ericsson.fdp.business.util.NotificationCacheUtil;
import com.ericsson.fdp.business.vo.FDPNotificationVO;
import com.ericsson.fdp.common.enums.ModuleType;
import com.ericsson.fdp.common.exception.FDPServiceException;
import com.ericsson.fdp.common.vo.FDPCircle;
import com.ericsson.fdp.core.dsm.framework.service.FDPCache;
import com.ericsson.fdp.core.serviceprovisioning.bean.FDPMetaBag;
import com.ericsson.fdp.dao.dto.FDPNotificationDTO;
import com.ericsson.fdp.dao.dto.UpdateCacheDTO;
import com.ericsson.fdp.dao.enums.ActionTypeEnum;
import com.ericsson.fdp.dao.enums.LanguageType;
import com.ericsson.fdp.dao.fdpadmin.FDPCircleDAO;
import com.ericsson.fdp.dao.fdpadmin.FDPCircleNotificationDAO;

/**
 * The Class NotificationDataServiceImpl.
 */
@Stateless(mappedName = "notificationDataService")
public class NotificationDataServiceImpl implements NotificationDataService {

	/** The fdp cache. */
	@Resource(lookup = "java:app/fdpBusiness-1.0/MetaDataCache")
	private FDPCache<FDPMetaBag, FDPCacheable> fdpCache;

	/** The fdp circle notification dao. */
	@Inject
	private FDPCircleNotificationDAO fdpCircleNotificationDAO;

	@Inject
	private FDPCircleDAO circleDAO;

	@Override
	public boolean updateMetaCache(final UpdateCacheDTO updateCacheDTO) throws FDPServiceException {
		if (updateCacheDTO.getUiObjectDTO() instanceof FDPNotificationDTO) {
			FDPNotificationDTO notification = (FDPNotificationDTO) updateCacheDTO.getUiObjectDTO();
			try {
				if (ActionTypeEnum.DELETE.equals(updateCacheDTO.getAction())) {
					FDPMetaBag metaBag = new FDPMetaBag(updateCacheDTO.getCircle(), ModuleType.NOTIFICATION,
							notification.getNotificationsId());
					fdpCache.removeKey(metaBag);
				} else {
					final FDPNotificationDTO notificationUI = (FDPNotificationDTO) updateCacheDTO.getUiObjectDTO();
					final Map<Long,Map<LanguageType,FDPNotificationDTO>> notificationMap = fdpCircleNotificationDAO
							.getCircleNotificationsWithParamForId(updateCacheDTO.getCircle().getCircleId(),notificationUI.getNotificationsId());
					for(final Map.Entry<Long,Map<LanguageType,FDPNotificationDTO>> notificationList : notificationMap.entrySet()) {
						initializeUpdateNotification(NotificationCacheUtil.notificationDTOToNotificationVO(notificationList.getValue()),
								updateCacheDTO.getCircle());
					}
				}
			} catch (IOException e) {
				LoggerFactory.getLogger(this.getClass()).error(e.getMessage(), e);
				new RuntimeException(e.getMessage(), e);
			}
		}
		return true;
	}

	@Override
	public boolean initializeMetaCache(final FDPCircle fdpCircle) throws FDPServiceException {
		final Map<Long,Map<LanguageType,FDPNotificationDTO>> notificationMap = fdpCircleNotificationDAO.getAllCircleNotificationsWithParam(fdpCircle.getCircleId());
		try {
			for(final Map.Entry<Long,Map<LanguageType,FDPNotificationDTO>> notificationList : notificationMap.entrySet()) {
				initializeUpdateNotification(NotificationCacheUtil.notificationDTOToNotificationVO(notificationList.getValue()),
						fdpCircle);
			}
		} catch (IOException e) {
			LoggerFactory.getLogger(this.getClass()).error(e.getMessage(), e);
			new RuntimeException(e.getMessage(), e);
		}
		return true;
	}

	/**
	 * Initialize Or updates the whole Service provisioning rule and its
	 * children.
	 *
	 * @param notification
	 *            the notification
	 * @param fdpCircle
	 *            FDPCircle.
	 */
	protected void initializeUpdateNotification(final FDPNotificationVO notification, final FDPCircle fdpCircle) {
		if(null!= notification){
			if (NotificationForAllCircle.containsId(notification.getNotificationId())) {
				updateNotificationForAllCircles(notification);
			} else {
				FDPMetaBag metaBag = new FDPMetaBag(fdpCircle, ModuleType.NOTIFICATION, notification.getNotificationId());
				fdpCache.putValue(metaBag, notification);
			}	
			//Added by Rahul to update other language as well
			if(null!=notification.getOtherLangNotificationMap() && notification.getOtherLangNotificationMap().size()>0)
			for (final Entry<LanguageType, FDPNotificationVO> notificationList : notification.getOtherLangNotificationMap().entrySet())
			{
				this.initializeUpdateNotification(notificationList.getValue(), fdpCircle);
			}
		}
	}

	/**
	 * This method is used to update notifications for all circle.
	 *
	 * @param notification
	 *            the notification.
	 */
	protected void updateNotificationForAllCircles(final FDPNotificationVO notification) {
		List<FDPCircle> fdpCircleList = circleDAO.getAllCircleConfiguration();
		for (FDPCircle circle : fdpCircleList) {
			FDPMetaBag metaBag = new FDPMetaBag(circle, ModuleType.NOTIFICATION, notification.getNotificationId());
			fdpCache.putValue(metaBag, notification);
		}
	}

	@Override
	public ModuleType getModuleType() {
		return ModuleType.NOTIFICATION;
	}
}
