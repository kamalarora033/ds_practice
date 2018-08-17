package com.ericsson.fdp.business.cache.datageneration.service.impl;

import java.util.List;

import javax.annotation.Resource;
import javax.ejb.Stateless;
import javax.inject.Inject;

import com.ericsson.fdp.FDPCacheable;
import com.ericsson.fdp.business.cache.datageneration.service.FDPDynamicMenuAliasCodeService;
import com.ericsson.fdp.business.vo.NodeAliasCode;
import com.ericsson.fdp.common.enums.ChannelType;
import com.ericsson.fdp.common.enums.ModuleType;
import com.ericsson.fdp.common.vo.FDPCircle;
import com.ericsson.fdp.core.dsm.framework.service.FDPCache;
import com.ericsson.fdp.core.serviceprovisioning.bean.FDPMetaBag;
import com.ericsson.fdp.dao.dto.UpdateCacheDTO;
import com.ericsson.fdp.dao.dto.product.DynamicMenuAliasCodesDTO;
import com.ericsson.fdp.dao.enums.ActionTypeEnum;
import com.ericsson.fdp.dao.fdpadmin.FDPDynamicMenuAliasCodeDAO;

/**
 * The Class FDPDynamicMenuAliasCodeServiceImpl.
 */
@Stateless(mappedName = "FDPDynamicMenuAliasCodeServiceImpl")
public class FDPDynamicMenuAliasCodeServiceImpl implements FDPDynamicMenuAliasCodeService {

	/** The fdp cache. */
	@Resource(lookup = "java:app/fdpBusiness-1.0/MetaDataCache")
	private FDPCache<FDPMetaBag, FDPCacheable> fdpCache;

	/** The FDPDynamicMenuAliasCodeDAO. */
	@Inject
	private FDPDynamicMenuAliasCodeDAO dynamicMenuAliasCodeDAO;

	@Override
	public void initializeDynamicMenuAliasCodeCache(final UpdateCacheDTO updateCacheDTO) {
		if (updateCacheDTO == null || updateCacheDTO.getUiObjectDTO() == null) {
			// when updateCacheDTO is null then initialize complete cache.
			initializeOrRefreshCache(updateCacheDTO);
		} else {
			// when updateCacheDTO.getUiObjectDTO() is present either insert or
			// delete the value from cache.
			if (updateCacheDTO.getUiObjectDTO() != null
					&& updateCacheDTO.getUiObjectDTO() instanceof DynamicMenuAliasCodesDTO) {
				DynamicMenuAliasCodesDTO dynamicMenuAliasCodesDTO = (DynamicMenuAliasCodesDTO) updateCacheDTO
						.getUiObjectDTO();

				if (ActionTypeEnum.DELETE.equals(updateCacheDTO.getAction())) {
					String aliasCode = dynamicMenuAliasCodesDTO.getAlias();
					aliasCode =	"SMS".equalsIgnoreCase(ChannelType.valueOf(dynamicMenuAliasCodesDTO.getChannelName()).toString()) ? aliasCode.toLowerCase()
							: aliasCode;

					FDPMetaBag metaBag = new FDPMetaBag(updateCacheDTO.getCircle(), ModuleType.DYNAMIC_MENU_CODE_ALIAS,
							aliasCode);
					fdpCache.removeKey(metaBag);
				} else {
					if (dynamicMenuAliasCodesDTO.getPreviousAlias() != null ){
						String previousAliasCode = dynamicMenuAliasCodesDTO.getPreviousAlias();
						previousAliasCode =	"SMS".equalsIgnoreCase(ChannelType.valueOf(dynamicMenuAliasCodesDTO.getChannelName()).toString()) ? previousAliasCode.toLowerCase()
								: previousAliasCode;

						FDPMetaBag metaBag = new FDPMetaBag(updateCacheDTO.getCircle(), ModuleType.DYNAMIC_MENU_CODE_ALIAS,
								previousAliasCode);
						fdpCache.removeKey(metaBag);
					}

					String aliasCode = dynamicMenuAliasCodesDTO.getAlias();
					aliasCode =	"SMS".equalsIgnoreCase(ChannelType.valueOf(dynamicMenuAliasCodesDTO.getChannelName()).toString()) ? aliasCode.toLowerCase()
							: aliasCode;
					String code = dynamicMenuAliasCodesDTO.getCode();
					code =	"SMS".equalsIgnoreCase(ChannelType.valueOf(dynamicMenuAliasCodesDTO.getChannelName()).toString()) ? code.toLowerCase()
							: code;

					FDPMetaBag metaBag = new FDPMetaBag(updateCacheDTO.getCircle(), ModuleType.DYNAMIC_MENU_CODE_ALIAS,
							dynamicMenuAliasCodesDTO.getAlias());

					NodeAliasCode nodeAliasCode = new NodeAliasCode();
					nodeAliasCode.setActualCode(code);
					nodeAliasCode.setChannelType(ChannelType.valueOf(dynamicMenuAliasCodesDTO.getChannelName()));
					nodeAliasCode.setFdpCircle(dynamicMenuAliasCodesDTO.getCircleId());

					nodeAliasCode.setNodeAliasCode(aliasCode);
					fdpCache.putValue(metaBag, nodeAliasCode);
				}
			} else {
				// updateCacheDTO.getUiObjectDTO() is null and fdpCircle,
				// moduleName is present then refresh that circle cache.
				initializeOrRefreshCache(updateCacheDTO);
			}
		}
	}

	/**
	 * Initialize or refresh cache.
	 *
	 * @param updateCacheDTO
	 *            the update cache dto
	 */
	private void initializeOrRefreshCache(final UpdateCacheDTO updateCacheDTO) {

		if (updateCacheDTO == null) {
			// /initialize the complete cache...
			List<DynamicMenuAliasCodesDTO> dynamicMenuAliasCodesDTOs = dynamicMenuAliasCodeDAO
					.getDynamicMenuAliasCode();
			populateAliasCodeInCache(dynamicMenuAliasCodesDTOs);
		} else {
			// /refresh specific circle cache.
			FDPMetaBag fdpMetaBag = new FDPMetaBag(updateCacheDTO.getCircle(), updateCacheDTO.getModuleType(), null);
			fdpCache.removeSubStore(fdpMetaBag);

			List<DynamicMenuAliasCodesDTO> dynamicMenuAliasCodesDTOs = dynamicMenuAliasCodeDAO
					.getDynamicMenuAliasCodeForCircle(updateCacheDTO.getCircle());
			populateAliasCodeInCache(dynamicMenuAliasCodesDTOs);
		}

	}

	/**
	 * Populate alias code in cache.
	 *
	 * @param dynamicMenuAliasCodesDTOs
	 *            the dynamic menu alias codes dt os
	 */
	private void populateAliasCodeInCache(final List<DynamicMenuAliasCodesDTO> dynamicMenuAliasCodesDTOs) {
		ModuleType moduleId = ModuleType.DYNAMIC_MENU_CODE_ALIAS;
		for (DynamicMenuAliasCodesDTO dynamicMenuAliasCodesDTO : dynamicMenuAliasCodesDTOs) {
			FDPCircle fdpCircle = dynamicMenuAliasCodesDTO.getCircleId();
			String aliasCode = dynamicMenuAliasCodesDTO.getAlias();
			aliasCode =	"SMS".equalsIgnoreCase(dynamicMenuAliasCodesDTO.getChannelName()) ? aliasCode.toLowerCase()
					: aliasCode;
			String code = dynamicMenuAliasCodesDTO.getCode();
			code =	"SMS".equalsIgnoreCase(dynamicMenuAliasCodesDTO.getChannelName()) ? code.toLowerCase()
					: code;

			NodeAliasCode nodeAliasCode = new NodeAliasCode();
			nodeAliasCode.setActualCode(code);
			nodeAliasCode.setChannelType(ChannelType.valueOf(dynamicMenuAliasCodesDTO.getChannelName()));
			nodeAliasCode.setFdpCircle(fdpCircle);

			nodeAliasCode.setNodeAliasCode(aliasCode);
			FDPMetaBag fdpMetaBag = new FDPMetaBag(fdpCircle, moduleId, aliasCode);
			fdpCache.putValue(fdpMetaBag, nodeAliasCode);
		}

	}

}
