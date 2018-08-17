package com.ericsson.fdp.business.step.execution.impl.activation;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Repository;

import com.ericsson.fdp.FDPCacheable;
import com.ericsson.fdp.business.command.AbstractCommand;
import com.ericsson.fdp.business.enums.ActivationCommands;
import com.ericsson.fdp.business.enums.AuxRequestParam;
import com.ericsson.fdp.business.enums.Command;
import com.ericsson.fdp.business.enums.EMAServiceMode;
import com.ericsson.fdp.business.enums.ServiceStepOptions;
import com.ericsson.fdp.business.enums.ThreeGActivationCommandMode;
import com.ericsson.fdp.business.util.CommandUtil;
import com.ericsson.fdp.business.util.RequestUtil;
import com.ericsson.fdp.common.enums.ModuleType;
import com.ericsson.fdp.core.command.FDPCommand;
import com.ericsson.fdp.core.entity.FDPEMADetail;
import com.ericsson.fdp.core.exception.ExecutionFailedException;
import com.ericsson.fdp.core.request.FDPRequest;
import com.ericsson.fdp.core.serviceprovisioning.bean.FDPMetaBag;
import com.ericsson.fdp.core.utils.ApplicationConfigUtil;
import com.ericsson.fdp.dao.enums.EMAInterfaceTypeEnum;

/**
 * This class is used to find the commands for activation.
 * 
 * @author Ericsson
 * 
 */
@Repository
public class ActivationCommandFinderService {

	/**
	 * This method is used to find the activation commands based on input
	 * parameters.
	 * 
	 * @param fdpRequest
	 *            the request object.
	 * @param activationCommandMode
	 *            the activation mode.
	 * @param emaServiceMode
	 *            the service mode.
	 * @return the map of commands to be executed.
	 * @throws ExecutionFailedException
	 *             Exception in execution.
	 */
	public final Map<EMAServiceMode, List<FDPCommand>> findCommands(final FDPRequest fdpRequest,
			final ThreeGActivationCommandMode activationCommandMode, final EMAServiceMode emaServiceMode)
			throws ExecutionFailedException {
		final Map<EMAServiceMode, List<FDPCommand>> commands = new HashMap<EMAServiceMode, List<FDPCommand>>();
		switch (emaServiceMode) {
		case TWO_G:
			commands.put(EMAServiceMode.TWO_G, getTwoGCommands(fdpRequest, activationCommandMode));
			break;
		case THREE_G:
			commands.put(EMAServiceMode.TWO_G, getTwoGCommands(fdpRequest, activationCommandMode));
			commands.put(EMAServiceMode.THREE_G, getThreeGCommands(fdpRequest, activationCommandMode));
			break;
		case BLACKBERRY_SERVICE_ACTIVATION:
			
		commands.put(EMAServiceMode.BLACKBERRY_SERVICE_ACTIVATION, getServiceActivationCommand(fdpRequest));
		break;
		default:
			throw new ExecutionFailedException("Cannot find commands for service mode " + emaServiceMode);
		}
		return commands;
	}

	private List<FDPCommand> getServiceActivationCommand(FDPRequest fdpRequest) throws ExecutionFailedException {
		List<FDPCommand> fdpCommands = null;
		
		final FDPEMADetail fdpemaDetail = RequestUtil.getEmaDetails(fdpRequest);
		
		if (fdpemaDetail != null) {
			final List<String> commands = new ArrayList<String>();
			ActivationCommands.SERVICE_ACTIVATE_CAI.getCommandName();
		commands.add(ActivationCommands.SERVICE_ACTIVATE_CAI.getCommandName());
			fdpCommands = CommandUtil.getCommandsFromDisplayName(fdpRequest.getCircle(), commands);
		}
			return fdpCommands;
		

	}

	/**
	 * This method is used to get the threeG commands.
	 * 
	 * @param fdpRequest
	 *            the request object.
	 * @param activationCommandMode
	 *            the activation mode.
	 * @return the list of threeG commands to be executed.
	 * @throws ExecutionFailedException
	 *             Exception in execution.
	 */
	private List<FDPCommand> getThreeGCommands(final FDPRequest fdpRequest,
			final ThreeGActivationCommandMode activationCommandMode) throws ExecutionFailedException {
		List<FDPCommand> fdpCommands = null;
		final FDPEMADetail fdpemaDetail = RequestUtil.getEmaDetails(fdpRequest);
		if (fdpemaDetail != null) {
			final List<String> commands = new ArrayList<String>();
			if (EMAInterfaceTypeEnum.MML.equals(fdpemaDetail.getInterfaceType())) {
				commands.addAll(activationCommandMode.getCommandsForMMLThreeG());
			} else if (EMAInterfaceTypeEnum.CAI.equals(fdpemaDetail.getInterfaceType())) {
				commands.addAll(activationCommandMode.getCommandsForCAIThreeG());
			}
			fdpCommands = CommandUtil.getCommandsFromDisplayName(fdpRequest.getCircle(), commands);
		}
		return fdpCommands;
	}

	/**
	 * This method is used to get the twoG commands.
	 * 
	 * @param fdpRequest
	 *            the request object.
	 * @param activationCommandMode
	 *            the activation mode.
	 * @return the list of twoG commands to be executed.
	 * @throws ExecutionFailedException
	 *             Exception in execution.
	 */
	private List<FDPCommand> getTwoGCommands(final FDPRequest fdpRequest,
			final ThreeGActivationCommandMode activationCommandMode) throws ExecutionFailedException {
		List<FDPCommand> fdpCommands = null;
		final FDPEMADetail fdpemaDetail = RequestUtil.getEmaDetails(fdpRequest);
		if (fdpemaDetail != null) {
			final List<String> commands = new ArrayList<String>();
			if (EMAInterfaceTypeEnum.MML.equals(fdpemaDetail.getInterfaceType())) {
				commands.addAll(activationCommandMode.getCommandsForMMLTwoG());
			} else if (EMAInterfaceTypeEnum.CAI.equals(fdpemaDetail.getInterfaceType())) {
				commands.addAll(activationCommandMode.getCommandsForCAITwoG());
			}
			fdpCommands = CommandUtil.getCommandsFromDisplayName(fdpRequest.getCircle(), commands);
		}
		return fdpCommands;
	}

	public Map<EMAServiceMode, List<FDPCommand>> findCommands(
			FDPRequest fdpRequest,
			ThreeGActivationCommandMode activationCommandMode,
			EMAServiceMode emaServiceMode, Object[] additionalInformation) throws ExecutionFailedException {
		EMAServiceMode emaServiceModeFound = EMAServiceMode.BLACKBERRY_SERVICE_ACTIVATION;
		String action=null;
		List<FDPCommand> commandlst=null;
		FDPCommand fdpCommand=null;
		Map<EMAServiceMode, List<FDPCommand>> commandList=new HashMap<EMAServiceMode, List<FDPCommand>>();;
		
		if (additionalInformation != null && additionalInformation[0] != null
				&& additionalInformation[0] instanceof Map<?, ?>) {
			final Map<ServiceStepOptions, String> additionalMap = (Map<ServiceStepOptions, String>) additionalInformation[0];
			if (additionalMap.get(ServiceStepOptions.PROVISION_ACTION_EMA) != null) {
				action = additionalMap.get(ServiceStepOptions.PROVISION_ACTION_EMA);
				if(action.equals("ACTIVATION"))
				{
					final FDPCacheable fdpCommandCached = ApplicationConfigUtil.getMetaDataCache().getValue(
							new FDPMetaBag(fdpRequest.getCircle(), ModuleType.COMMAND, Command.SERVICE_ACTIVATE_CAI));
					if(null != fdpCommandCached && fdpCommandCached instanceof AbstractCommand) 
					{
						fdpCommand = (FDPCommand) fdpCommandCached;
						commandlst=new ArrayList<FDPCommand>();
						commandlst.add(fdpCommand);
						commandList.put(emaServiceModeFound, commandlst);
					}
					
				}
				else if(action.equals("DEACTIVATION"))
				{
					final FDPCacheable fdpCommandCached = ApplicationConfigUtil.getMetaDataCache().getValue(
							new FDPMetaBag(fdpRequest.getCircle(), ModuleType.COMMAND, Command.SERVICE_DEACTIVATE_CAI));
					
					if(null != fdpCommandCached && fdpCommandCached instanceof AbstractCommand) 
					{
						fdpCommand = (FDPCommand) fdpCommandCached;
						commandlst=new ArrayList<FDPCommand>();
						commandlst.add(fdpCommand);
						commandList.put(emaServiceModeFound, commandlst);
					}
				}
			}
		}
		return commandList;
	}
}
