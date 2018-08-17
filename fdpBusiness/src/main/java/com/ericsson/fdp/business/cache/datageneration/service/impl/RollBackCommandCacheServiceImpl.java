package com.ericsson.fdp.business.cache.datageneration.service.impl;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.Resource;
import javax.ejb.Stateless;
import javax.inject.Inject;

import com.ericsson.fdp.FDPCacheable;
import com.ericsson.fdp.business.cache.datageneration.CommandCacheUtil;
import com.ericsson.fdp.business.cache.datageneration.service.RollBackCommandCacheService;
import com.ericsson.fdp.business.command.impl.NonTransactionCommand;
import com.ericsson.fdp.business.command.param.impl.CommandParamInput;
import com.ericsson.fdp.business.command.rollback.bean.RollBackCacheBean;
import com.ericsson.fdp.common.enums.ModuleType;
import com.ericsson.fdp.common.exception.FDPServiceException;
import com.ericsson.fdp.common.vo.FDPCircle;
import com.ericsson.fdp.core.command.FDPCommand;
import com.ericsson.fdp.core.command.param.CommandParam;
import com.ericsson.fdp.core.dsm.framework.service.FDPCache;
import com.ericsson.fdp.core.serviceprovisioning.bean.FDPMetaBag;
import com.ericsson.fdp.dao.dto.CommandDTO;
import com.ericsson.fdp.dao.dto.ParameterDTO;
import com.ericsson.fdp.dao.enums.CommandParameterType;
import com.ericsson.fdp.dao.fdpadmin.FDPCommandDAO;

@Stateless(mappedName = "RollBackCommandCacheServiceImpl")
public class RollBackCommandCacheServiceImpl implements RollBackCommandCacheService {

	@Resource(lookup = "java:app/fdpBusiness-1.0/MetaDataCache")
	private FDPCache<FDPMetaBag, FDPCacheable> fdpCache;

	@Inject
	private FDPCommandDAO commandDAO;

	@Override
	public boolean initializeRollBackCommandCache() throws FDPServiceException {

		try {
			Map<String, List<FDPCommand>> commandByRollBackCommandMap = new HashMap<String, List<FDPCommand>>();

			List<CommandDTO> comandDTOList = commandDAO.getCommandsWithTheirRollbackCommand();
			for (CommandDTO commandDTO : comandDTOList) {

				NonTransactionCommand nonTrCommand = (NonTransactionCommand) Class
						.forName(commandDTO.getClassNameForRollBackCommand()).getConstructor(String.class)
						.newInstance(commandDTO.getCommandDisplayName());
				nonTrCommand.setCommandDisplayName(commandDTO.getCommandDisplayName());
				nonTrCommand.setCommandExecutionType(commandDTO.getCommandExecutionType());
				nonTrCommand.setCommandName(commandDTO.getCommandName());
				nonTrCommand.setCommandType(commandDTO.getCommandDefinition());
				nonTrCommand.setSystem(commandDTO.getSystem());
				nonTrCommand.setIsSuccessAlways(commandDTO.getIsSuccessAlways());
				for (ParameterDTO paramDTO : commandDTO.getParameters()) {
					CommandParamInput commandParamInput = new CommandParamInput(paramDTO.getFeedType(),
							CommandCacheUtil.getDefValue(paramDTO.getFeedType(), paramDTO.getRollBackParamValue(),
									paramDTO.getType()));
					commandParamInput.setCommand(nonTrCommand);
					if (paramDTO.getXmlType().equals(CommandParameterType.ARRAY)) {

						commandParamInput.setType(CommandParameterType.ARRAY);
					} else {
						if (paramDTO.getXmlType().equals(CommandParameterType.STRUCT)) {
							commandParamInput.setType(CommandParameterType.STRUCT);
						} else {
							commandParamInput.setType(CommandParameterType.PRIMITIVE);
						}
					}
					if (!paramDTO.getXmlType().equals(CommandParameterType.ARRAY)
							&& !paramDTO.getXmlType().equals(CommandParameterType.STRUCT)) {
						commandParamInput.setPrimitiveValue(paramDTO.getXmlPrimitiveType());
					}
					commandParamInput.setName(paramDTO.getParameterName());
					if (nonTrCommand.getInputParam() != null) {
						nonTrCommand.getInputParam().add(commandParamInput);
					} else {
						List<CommandParam> commandParamInputList = new ArrayList<CommandParam>();
						commandParamInputList.add(commandParamInput);
						nonTrCommand.setInputParam(commandParamInputList);
					}
				}

				if (commandByRollBackCommandMap.get(commandDTO.getCommandDisplayNameForRollBack()) != null) {
					List<FDPCommand> commandList = commandByRollBackCommandMap.get(commandDTO
							.getCommandDisplayNameForRollBack());
					commandList.add(nonTrCommand);
				} else {
					List<FDPCommand> commandList = new ArrayList<FDPCommand>();
					commandList.add(nonTrCommand);
					commandByRollBackCommandMap.put(commandDTO.getCommandDisplayNameForRollBack(), commandList);
				}
			}
			for (String commandNameForRollBack : commandByRollBackCommandMap.keySet()) {

				FDPCircle fdpCircle = new FDPCircle();
				fdpCircle.setCircleCode("All");
				fdpCircle.setCircleName("All");
				fdpCircle.setCircleId(-1l);
				FDPMetaBag fdpMetaBag = new FDPMetaBag(fdpCircle, ModuleType.ROLLBACK_COMMAND, commandNameForRollBack);
				RollBackCacheBean rollBackCacheBean = new RollBackCacheBean();
				rollBackCacheBean.setRollBackCommandList(commandByRollBackCommandMap.get(commandNameForRollBack));
				fdpCache.putValue(fdpMetaBag, rollBackCacheBean);
			}
		} catch (InstantiationException | IllegalAccessException | IllegalArgumentException | InvocationTargetException
				| NoSuchMethodException | SecurityException | ClassNotFoundException e) {
			throw new FDPServiceException("Could not initialize rollback commands ", e);
		}
		return false;
	}
}
