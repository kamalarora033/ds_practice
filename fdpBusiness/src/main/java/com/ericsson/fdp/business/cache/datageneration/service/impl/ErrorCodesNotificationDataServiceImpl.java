package com.ericsson.fdp.business.cache.datageneration.service.impl;

import java.util.ArrayList;
import java.util.List;

import javax.annotation.Resource;
import javax.ejb.Stateless;
import javax.inject.Inject;

import com.ericsson.fdp.FDPCacheable;
import com.ericsson.fdp.business.cache.datageneration.service.ErrorCodesNotificationDataService;
import com.ericsson.fdp.business.vo.ErrorCodesNotificationVO;
import com.ericsson.fdp.common.constants.FDPConstant;
import com.ericsson.fdp.common.enums.ModuleType;
import com.ericsson.fdp.common.exception.FDPServiceException;
import com.ericsson.fdp.common.vo.FDPCircle;
import com.ericsson.fdp.core.dsm.framework.service.FDPCache;
import com.ericsson.fdp.core.serviceprovisioning.bean.FDPMetaBag;
import com.ericsson.fdp.dao.dto.FDPCircleDTO;
import com.ericsson.fdp.dao.dto.FDPCommandResultCodeMappingDTO;
import com.ericsson.fdp.dao.dto.FDPResultCodeNotificationMappingDTO;
import com.ericsson.fdp.dao.dto.FDPResultCodesDTO;
import com.ericsson.fdp.dao.dto.UpdateCacheDTO;
import com.ericsson.fdp.dao.enums.ResultCodeNotificationMappingEntityType;
import com.ericsson.fdp.dao.fdpadmin.FDPCommandResultCodeMappingDAO;
import com.ericsson.fdp.dao.fdpadmin.FDPResultCodesDAO;
import com.ericsson.fdp.dao.fdpbusiness.FDPResultCodeNotificationMappingDAO;

/**
 * The Class ErrorCodesNotificationDataServiceImpl.
 */
@Stateless(mappedName = "errorCodesNotificationDataService")
public class ErrorCodesNotificationDataServiceImpl implements ErrorCodesNotificationDataService {

	/** The fdp cache. */
	@Resource(lookup = "java:app/fdpBusiness-1.0/MetaDataCache")
	private FDPCache<FDPMetaBag, FDPCacheable> fdpCache;

	/** The rc notification mapping dao. */
	@Inject
	FDPResultCodeNotificationMappingDAO rcNotificationMappingDAO;

	/** The fdp result codes dao. */
	@Inject
	private FDPResultCodesDAO fdpResultCodesDAO;

	/** The fdp command result code mapping dao. */
	@Inject
	private FDPCommandResultCodeMappingDAO fdpCommandResultCodeMappingDAO;

	@Override
	public boolean updateMetaCache(final UpdateCacheDTO updateCacheDTO) throws FDPServiceException {
		if (updateCacheDTO == null) {
			throw new FDPServiceException("UpdateCacheDTO cannot be null");
		} else {
			ModuleType moduleType = updateCacheDTO.getModuleType();
			ResultCodeNotificationMappingEntityType entityType = null;
			if (ModuleType.RESPONSE_CODE_NOTIFICATION_MAPPING.equals(moduleType)) {
				entityType = ResultCodeNotificationMappingEntityType.RESPONSE_CODE;
			} else if (ModuleType.FAULT_CODE_NOTIFICATION_MAPPING.equals(moduleType)) {
				entityType = ResultCodeNotificationMappingEntityType.FAULT_CODE;
			}
			this.putDataIntoCache(rcNotificationMappingDAO.getCircleResultCodeNotificationMappings(
					entityType.getValue(), updateCacheDTO.getCircle().getCircleId()), moduleType);
		}
		return true;
	}

	@Override
	public boolean initializeMetaCache(final FDPCircle fdpCircle) throws FDPServiceException {
		if (fdpCircle == null) {
			throw new FDPServiceException("Circle cannot be null");
		} else {
			this.putDataIntoCache(rcNotificationMappingDAO.getCircleResultCodeNotificationMappings(
					ResultCodeNotificationMappingEntityType.FAULT_CODE.getValue(), fdpCircle.getCircleId()),
					ModuleType.FAULT_CODE_NOTIFICATION_MAPPING);
			putDataIntoCache(rcNotificationMappingDAO.getCircleResultCodeNotificationMappings(
					ResultCodeNotificationMappingEntityType.RESPONSE_CODE.getValue(), fdpCircle.getCircleId()),
					ModuleType.RESPONSE_CODE_NOTIFICATION_MAPPING);
		}
		return true;
	}

	/**
	 * Put data into cache.
	 *
	 * @param mappings
	 *            the mappings
	 * @param moduleType
	 *            the module type
	 */
	private void putDataIntoCache(final List<FDPResultCodeNotificationMappingDTO> mappings, final ModuleType moduleType) {

		List<FDPResultCodesDTO> resultCodes = new ArrayList<FDPResultCodesDTO>();
		List<FDPCommandResultCodeMappingDTO> commandResultCodes = new ArrayList<FDPCommandResultCodeMappingDTO>();

		if (ModuleType.RESPONSE_CODE_NOTIFICATION_MAPPING.equals(moduleType)) {
			commandResultCodes = fdpCommandResultCodeMappingDAO.getCommandResultCodeMapping();
		} else if (ModuleType.FAULT_CODE_NOTIFICATION_MAPPING.equals(moduleType)) {
			resultCodes = fdpResultCodesDAO.getResultCodes();
		}

		for (FDPResultCodeNotificationMappingDTO mapping : mappings) {
			FDPCircleDTO circleDTO = mapping.getCircleId();
			FDPCircle circleVO = new FDPCircle(circleDTO.getCircleId(), circleDTO.getCircleCode(),
					circleDTO.getCircleName());

			String key = getKeyForCurrentMapping(mapping, resultCodes, commandResultCodes);

			FDPMetaBag metaBag = new FDPMetaBag(circleVO, moduleType, key);
			ErrorCodesNotificationVO errorCodeNotificationVO = new ErrorCodesNotificationVO();
			errorCodeNotificationVO.setNotificationId(mapping.getFdpNotification().getNotificationsId());

			fdpCache.putValue(metaBag, errorCodeNotificationVO);
		}
	}

	/**
	 * Gets the key for current mapping.
	 *
	 * @param mapping
	 *            the mapping
	 * @param resultCodes
	 *            the result codes
	 * @param commandResultCodes
	 *            the command result codes
	 * @return the key for current mapping
	 */
	private String getKeyForCurrentMapping(final FDPResultCodeNotificationMappingDTO mapping,
			final List<FDPResultCodesDTO> resultCodes, final List<FDPCommandResultCodeMappingDTO> commandResultCodes) {

		String key = "";
		if (ResultCodeNotificationMappingEntityType.RESPONSE_CODE.getValue().equals(mapping.getEntityType())) {
			for (FDPCommandResultCodeMappingDTO commandResultCode : commandResultCodes) {
				if (commandResultCode.getCommandResultCodeMapping().equals(mapping.getEntityId())) {
					key = commandResultCode.getCommandNameId().getCommandNameToDisplay()
							+ FDPConstant.PARAMETER_SEPARATOR
							+ commandResultCode.getResultCodeId().getResultCodeValue();
					break;
				}
			}
		} else if (ResultCodeNotificationMappingEntityType.FAULT_CODE.getValue().equals(mapping.getEntityType())) {

			for (FDPResultCodesDTO resultCode : resultCodes) {
				if (resultCode.getResultCodesId().equals(mapping.getEntityId())) {
					key = resultCode.getExternalSystemInterface() + FDPConstant.PARAMETER_SEPARATOR
							+ resultCode.getResultCodeValue();
					break;
				}
			}
		}
		return key;
	}

	@Override
	public ModuleType getModuleType() {
		return ModuleType.RESPONSE_CODE_NOTIFICATION_MAPPING;
	}
}
