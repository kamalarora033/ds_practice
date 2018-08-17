package com.ericsson.fdp.business.cache.datageneration.service.impl;

import java.util.List;

import javax.annotation.Resource;
import javax.ejb.Stateless;
import javax.inject.Inject;

import com.ericsson.fdp.FDPCacheable;
import com.ericsson.fdp.business.cache.BatchJobCommandsForCache;
import com.ericsson.fdp.business.cache.datageneration.CommandCacheUtil;
import com.ericsson.fdp.business.cache.datageneration.service.CommandDataService;
import com.ericsson.fdp.common.constants.JNDILookupConstant;
import com.ericsson.fdp.common.enums.ModuleType;
import com.ericsson.fdp.common.exception.FDPServiceException;
import com.ericsson.fdp.common.vo.FDPCircle;
import com.ericsson.fdp.core.command.FDPCommand;
import com.ericsson.fdp.core.dsm.framework.service.FDPCache;
import com.ericsson.fdp.core.entity.service.EntityService;
import com.ericsson.fdp.core.serviceprovisioning.bean.FDPMetaBag;
import com.ericsson.fdp.dao.dto.CommandDTO;
import com.ericsson.fdp.dao.dto.UpdateCacheDTO;
import com.ericsson.fdp.dao.fdpadmin.FDPServiceProvDAO;

/**
 * The Class CommandDataServiceImpl.
 *
 * @author Ericsson
 */
@Stateless(mappedName = "commandDataService")
public class CommandDataServiceImpl implements CommandDataService {

	/** The fdp product service prov dao. */
	@Inject
	private FDPServiceProvDAO fdpProductServiceProvDAO;

	/** The fdp cache. */
	@Resource(lookup = "java:app/fdpBusiness-1.0/MetaDataCache")
	private FDPCache<FDPMetaBag, FDPCacheable> fdpCache;

	/** The entity service. */
	@Resource(lookup = "java:app/fdpCoreServices-1.0/EntityServiceImpl")
	private EntityService entityService;

	/** The application config cache. */
	@Resource(lookup = JNDILookupConstant.APPLICATION_CONFIG_CACHE_JNDI_NAME)
	private FDPCache<Object, Object> applicationConfigCache;

	@Override
	public boolean updateMetaCache(final UpdateCacheDTO updateCacheDTO) throws FDPServiceException {
		if (updateCacheDTO.getUiObjectDTO() instanceof CommandDTO) {
			CommandDTO updateCommandDTO = (CommandDTO) updateCacheDTO.getUiObjectDTO();
			FDPCommand command = CommandCacheUtil.commandDTOToFDPCommand(updateCommandDTO);
			initializeUpdateCommand(command, updateCacheDTO.getCircle());
		}
		return true;
	}

	@Override
	public boolean initializeMetaCache(final FDPCircle fdpCircle) throws FDPServiceException {
		if (fdpCircle == null) {
			throw new FDPServiceException("Circle cannot be null");
		} else {
			for (FDPCommand command : CommandCacheUtil.commandDTOListToFDPCommandList(entityService
					.getAllSavedCommands(fdpCircle.getCircleId()))) {
				initializeUpdateCommand(command, fdpCircle);
			}
			List<FDPCommand> batchJobCommands = BatchJobCommandsForCache.loadDefaultBatchJobCommands();
			for (FDPCommand command : batchJobCommands) {
				initializeUpdateCommand(command, new FDPCircle(-1L, "ALL", "ALL"));
			}
		}
		return true;
	}

	/**
	 * Initialize Or updates the whole Command and its parameters.
	 *
	 * @param command
	 *            the command
	 * @param fdpCircle
	 *            FDPCircle.
	 */
	protected void initializeUpdateCommand(final FDPCommand command, final FDPCircle fdpCircle) {
		FDPMetaBag metaBag = new FDPMetaBag(fdpCircle, ModuleType.COMMAND, command.getCommandDisplayName());
		fdpCache.removeKey(metaBag);
		fdpCache.putValue(metaBag, command);
	}

	@Override
	public ModuleType getModuleType() {
		return ModuleType.COMMAND;
	}
}
