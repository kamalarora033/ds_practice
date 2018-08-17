package com.ericsson.fdp.business.step.execution.impl;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.inject.Inject;

import com.ericsson.fdp.business.FDPRollbackable;
import com.ericsson.fdp.business.constants.FDPStepResponseConstants;
import com.ericsson.fdp.business.dto.emabatch.EMABatchRecordDTO;
import com.ericsson.fdp.business.enums.AuxRequestParam;
import com.ericsson.fdp.business.enums.EMAServiceMode;
import com.ericsson.fdp.business.enums.ServiceStepOptions;
import com.ericsson.fdp.business.enums.ThreeGActivationCommandMode;
import com.ericsson.fdp.business.exception.RollbackException;
import com.ericsson.fdp.business.request.impl.FulfillmentRequestImpl;
import com.ericsson.fdp.business.step.execution.FDPExecutionService;
import com.ericsson.fdp.business.step.execution.impl.activation.ActivationCommandFinderService;
import com.ericsson.fdp.business.step.execution.impl.activation.impl.ActivationServiceImpl;
import com.ericsson.fdp.business.util.RequestUtil;
import com.ericsson.fdp.core.command.FDPCommand;
import com.ericsson.fdp.core.exception.ExecutionFailedException;
import com.ericsson.fdp.core.request.FDPRequest;
import com.ericsson.fdp.core.request.FDPStepResponse;
import com.ericsson.fdp.dao.enums.EMAInterfaceTypeEnum;

/**
 * The execution service implementation.
 * 
 * @author Ericsson
 * 
 */
public abstract class AbstractFDPEMAServiceImpl implements FDPExecutionService {

	/** The activation service impl. */
	@Inject
	private ActivationServiceImpl activationServiceImpl;

	/** The activation command finder service. */
	@Inject
	private ActivationCommandFinderService activationCommandFinderService;

	/**
	 * Pre process.
	 * 
	 * @param fdpRequest
	 *            the fdp request
	 * @param additionalInformation
	 *            the additional information
	 * @return the map
	 * @throws ExecutionFailedException
	 *             the execution failed exception
	 */
	protected Map<EMAServiceMode, List<FDPCommand>> preProcess(final FDPRequest fdpRequest,
			final Object... additionalInformation) throws ExecutionFailedException {
		Map<EMAServiceMode, List<FDPCommand>> fdpCommandList=null;
		
		final ThreeGActivationCommandMode activationCommandMode = getActivationMode(fdpRequest, additionalInformation);
		final EMAServiceMode emaServiceMode = getEmaServiceMode(fdpRequest, additionalInformation);
		if(emaServiceMode==EMAServiceMode.BLACKBERRY_SERVICE_ACTIVATION)
		{
			fdpCommandList=activationCommandFinderService.findCommands(fdpRequest, activationCommandMode, emaServiceMode,additionalInformation);
		}
		else
		{
			fdpCommandList=activationCommandFinderService.findCommands(fdpRequest, activationCommandMode, emaServiceMode);
		}
		return fdpCommandList;
	}

	/**
	 * Gets the ema service mode.
	 * 
	 * @param fdpRequest
	 *            the fdp request
	 * @param additionalInformation
	 *            the additional information
	 * @return the ema service mode
	 * @throws ExecutionFailedException
	 *             the execution failed exception
	 */
	@SuppressWarnings("unchecked")
	protected EMAServiceMode getEmaServiceMode(final FDPRequest fdpRequest, final Object[] additionalInformation)
			throws ExecutionFailedException {
		EMAServiceMode emaServiceMode = (EMAServiceMode) fdpRequest
				.getAuxiliaryRequestParameter(AuxRequestParam.SERVICE_MODE);
		if (emaServiceMode == null) {
			EMAServiceMode emaServiceModeFound = EMAServiceMode.TWO_G;
			if (additionalInformation != null && additionalInformation[0] != null
					&& additionalInformation[0] instanceof Map<?, ?>) {
				final Map<ServiceStepOptions, String> additionalMap = (Map<ServiceStepOptions, String>) additionalInformation[0];
				if (additionalMap.get(ServiceStepOptions.MODE) != null) {
					emaServiceModeFound = EMAServiceMode.valueOf(additionalMap.get(ServiceStepOptions.MODE));
				}
				RequestUtil.putAuxiliaryValueInRequest(fdpRequest, AuxRequestParam.SERVICE_MODE, emaServiceModeFound);
				emaServiceMode = emaServiceModeFound;
				if (emaServiceMode == null) {
					throw new ExecutionFailedException("Cannot update for mode "
							+ additionalMap.get(ServiceStepOptions.MODE));
				}
			}
		}
		return emaServiceMode;
	}

	@Override
	public FDPStepResponse executeService(final FDPRequest fdpRequest, final Object... additionalInformation)
			throws ExecutionFailedException {
		final Map<EMAServiceMode, List<FDPCommand>> commandsToExecute = preProcess(fdpRequest, additionalInformation);
		final ThreeGActivationCommandMode activationCommandMode = getActivationMode(fdpRequest, additionalInformation);
		RequestUtil.putAuxiliaryValueInRequest(fdpRequest, AuxRequestParam.ACTIVATION_ICR_MODE,
				getActivationICRMode(additionalInformation, fdpRequest));
		
		
//	RequestUtil.updateServiceIDInRequest((FulfillmentRequestImpl) fdpRequest,additionalInformation);
		
	final FDPStepResponse fdpStepResponseImpl = executeCommands(fdpRequest, commandsToExecute,
				activationCommandMode);
		postProcess(fdpRequest, fdpStepResponseImpl, additionalInformation, commandsToExecute);
		return fdpStepResponseImpl;
	}

	/**
	 * This method is used to check for activation ICR mode.
	 * 
	 * @param additionalInformation
	 *            the additional information.
	 * @param fdpRequest
	 *            the request object.
	 * @return the boolean found.
	 */
	@SuppressWarnings("unchecked")
	private Boolean getActivationICRMode(final Object[] additionalInformation, final FDPRequest fdpRequest) {
		Boolean threeGActivationICRMode = (Boolean) fdpRequest
				.getAuxiliaryRequestParameter(AuxRequestParam.ACTIVATION_ICR_MODE);
		if (threeGActivationICRMode == null) {
			Boolean threeGActivationICRModeFound = Boolean.TRUE;
			if (additionalInformation != null && additionalInformation[0] != null
					&& additionalInformation[0] instanceof Map<?, ?>) {
				final Map<ServiceStepOptions, String> additionalMap = (Map<ServiceStepOptions, String>) additionalInformation[0];
				if (additionalMap.get(ServiceStepOptions.CHECK_ICR_CIRCLE) != null) {
					threeGActivationICRModeFound = Boolean.valueOf(additionalMap
							.get(ServiceStepOptions.CHECK_ICR_CIRCLE));
				}
				RequestUtil.putAuxiliaryValueInRequest(fdpRequest, AuxRequestParam.ACTIVATION_ICR_MODE,
						threeGActivationICRModeFound);
				threeGActivationICRMode = threeGActivationICRModeFound;
			}

		}
		return threeGActivationICRMode;
	}

	/**
	 * Execute commands.
	 * 
	 * @param fdpRequest
	 *            the fdp request
	 * @param commandsToExecute
	 *            the commands to execute
	 * @param activationCommandMode
	 *            the activation command mode
	 * @return the fDP step response
	 * @throws ExecutionFailedException
	 *             the execution failed exception
	 */
	private FDPStepResponse executeCommands(final FDPRequest fdpRequest,
			final Map<EMAServiceMode, List<FDPCommand>> commandsToExecute,
			final ThreeGActivationCommandMode activationCommandMode) throws ExecutionFailedException {
		FDPStepResponse fdpStepResponse=null;
		
		if(activationCommandMode!=ThreeGActivationCommandMode.BLACKBERRY_SERVICE_ACTIVATE)
		{
		RequestUtil.putAuxiliaryValueInRequest(fdpRequest, AuxRequestParam.ICR_NOTIFICATION_SENT, Boolean.FALSE);
		 fdpStepResponse = activationServiceImpl.executeActivationCommands(fdpRequest,
				commandsToExecute.get(EMAServiceMode.TWO_G), activationCommandMode, EMAServiceMode.TWO_G);
		if (RequestUtil.checkExecutionStatus(fdpStepResponse)) {
			fdpStepResponse = activationServiceImpl.executeActivationCommands(fdpRequest,
					commandsToExecute.get(EMAServiceMode.THREE_G), activationCommandMode, EMAServiceMode.THREE_G);
		}
		}
		else
		{

			RequestUtil.putAuxiliaryValueInRequest(fdpRequest, AuxRequestParam.ICR_NOTIFICATION_SENT, Boolean.FALSE);
			fdpStepResponse = activationServiceImpl.executeActivationCommands(fdpRequest,
					commandsToExecute.get(EMAServiceMode.BLACKBERRY_SERVICE_ACTIVATION), activationCommandMode, EMAServiceMode.BLACKBERRY_SERVICE_ACTIVATION);
			
				
		}
		return fdpStepResponse;
	}

	/**
	 * This method is used to get the additional information.
	 * 
	 * @param fdpRequest
	 *            the fdp request
	 * @param additionalInformation
	 *            the additional information.
	 * @return the activation mode.
	 */
	@SuppressWarnings("unchecked")
	protected ThreeGActivationCommandMode getActivationMode(final FDPRequest fdpRequest,
			final Object[] additionalInformation) {

		ThreeGActivationCommandMode threeGActivationCommandMode = (ThreeGActivationCommandMode) fdpRequest
				.getAuxiliaryRequestParameter(AuxRequestParam.ACTIVATION_MODE);
		if (threeGActivationCommandMode == null) {
			ThreeGActivationCommandMode threeGActivationCommandModeFound = ThreeGActivationCommandMode.QUERY_AND_UPDATE_ALL;
			
			if (additionalInformation != null && additionalInformation[0] != null
					&& additionalInformation[0] instanceof Map<?, ?>) {
				final Map<ServiceStepOptions, String> additionalMap = (Map<ServiceStepOptions, String>) additionalInformation[0];
				if(additionalMap.get(ServiceStepOptions.MODE).equals(ThreeGActivationCommandMode.BLACKBERRY_SERVICE_ACTIVATE.getCommandMode()))
				{
					threeGActivationCommandModeFound = this.getThreeGActivationCommandMode(additionalMap
							.get(ServiceStepOptions.MODE));
				}
				else if(additionalMap.get(ServiceStepOptions.ACTION) != null) {
					threeGActivationCommandModeFound = this.getThreeGActivationCommandMode(additionalMap
							.get(ServiceStepOptions.ACTION));
				}
				RequestUtil.putAuxiliaryValueInRequest(fdpRequest, AuxRequestParam.ACTIVATION_MODE,
						threeGActivationCommandModeFound);
				threeGActivationCommandMode = threeGActivationCommandModeFound;
			}

		}
		return threeGActivationCommandMode;
/*		
		ThreeGActivationCommandMode threeGActivationCommandMode = (ThreeGActivationCommandMode) fdpRequest
				.getAuxiliaryRequestParameter(AuxRequestParam.ACTIVATION_MODE);
 
		if (threeGActivationCommandMode == null) {
			ThreeGActivationCommandMode threeGActivationCommandModeFound = ThreeGActivationCommandMode.QUERY_AND_UPDATE_ALL;
			if (additionalInformation != null && additionalInformation[0] != null
					&& additionalInformation[0] instanceof Map<?, ?>) {
				final Map<ServiceStepOptions, String> additionalMap = (Map<ServiceStepOptions, String>) additionalInformation[0];
				if(additionalMap.get(ServiceStepOptions.MODE) .equals(ThreeGActivationCommandMode.BLACKBERRY_SERVICE_ACTIVATE))
				{
					threeGActivationCommandModeFound = this.getThreeGActivationCommandMode(additionalMap
							.get(ServiceStepOptions.MODE));
				}
				else if (additionalMap.get(ServiceStepOptions.ACTION) != null) {
					threeGActivationCommandModeFound = this.getThreeGActivationCommandMode(additionalMap
							.get(ServiceStepOptions.ACTION));
				}
				RequestUtil.putAuxiliaryValueInRequest(fdpRequest, AuxRequestParam.ACTIVATION_MODE,
						threeGActivationCommandModeFound);
				threeGActivationCommandMode = threeGActivationCommandModeFound;
			}

		}
		
		return threeGActivationCommandMode;*/
	}

	/**
	 * This method is used to perform post process operations on the step
	 * response with the request.
	 * 
	 * @param fdpRequest
	 *            the request.
	 * @param fdpStepResponse
	 *            the step response.
	 * @param additionalInformation
	 *            the additional information
	 * @throws ExecutionFailedException
	 *             Execution failed exception.
	 */

	protected abstract void postProcess(FDPRequest fdpRequest, FDPStepResponse fdpStepResponse,
			Object... additionalInformation) throws ExecutionFailedException;

	/**
	 * This method is used to get the activation mode.
	 * 
	 * @param mode
	 *            the mode.
	 * @return the activation mode.
	 */
	private ThreeGActivationCommandMode getThreeGActivationCommandMode(final String mode) {
		ThreeGActivationCommandMode threeGActivationCommandMode = ThreeGActivationCommandMode.QUERY_AND_UPDATE_ALL;
		for (final ThreeGActivationCommandMode activationMode : ThreeGActivationCommandMode.values()) {
			if (activationMode.getCommandMode().equalsIgnoreCase(mode)) {
				threeGActivationCommandMode = activationMode;
				break;
			}
		}
		return threeGActivationCommandMode;
	}

	/**
	 * Gets the eMA batch record dto.
	 * 
	 * @param fdpRequest
	 *            the fdp request
	 * @param fdpStepResponse
	 *            the fdp step response
	 * @param additionalInformation
	 *            the additional information
	 * @return the eMA batch record dto
	 * @throws ExecutionFailedException
	 *             the execution failed exception
	 */
	protected EMABatchRecordDTO getEMABatchRecordDTO(final FDPRequest fdpRequest,
			final FDPStepResponse fdpStepResponse, final Object... additionalInformation)
			throws ExecutionFailedException {

		final EMABatchRecordDTO result = new EMABatchRecordDTO();
		final EMAInterfaceTypeEnum interfaceType = RequestUtil.getEmaDetails(fdpRequest).getInterfaceType();
		final EMAServiceMode mode = getEmaServiceMode(fdpRequest, (Object[]) additionalInformation[0]);
		// mode of executing service
		final ThreeGActivationCommandMode serviceAction = getActivationMode(fdpRequest, additionalInformation); // service
		// Action of executing service.
		final EMAServiceMode emaServiceMode = (EMAServiceMode) fdpStepResponse
				.getStepResponseValue(FDPStepResponseConstants.EMA_SERVICE_MODE);
		// mode in which command/service fails.
		@SuppressWarnings("unchecked")
		final Map<EMAServiceMode, List<FDPCommand>> commandListMap = (Map<EMAServiceMode, List<FDPCommand>>) additionalInformation[1];
		final FDPCommand failedCommand = (FDPCommand) fdpStepResponse
				.getStepResponseValue(FDPStepResponseConstants.EMA_COMMAND_FAILED);
		final Map<EMAServiceMode, List<String>> retryCommands = getRetryCommands(interfaceType, mode, emaServiceMode,
				failedCommand, commandListMap, serviceAction);
		final Boolean icrCheck = getActivationICRMode(additionalInformation, fdpRequest);
		result.setCheckIcrCircle(icrCheck);
		result.setInterfaceType(interfaceType);
		result.setMode(mode);
		result.setMsisdn(fdpRequest.getIncomingSubscriberNumber().toString());
		result.setServiceAction(serviceAction.name());
		result.setCommands(retryCommands);
		return result;
	}

	/**
	 * Gets the retry commands.
	 * 
	 * @param interfaceType
	 *            the interface type
	 * @param mode
	 *            the mode
	 * @param failedMode
	 *            the failed mode
	 * @param failedCommand
	 *            the failed command
	 * @param commandListMap
	 *            the command list map
	 * @param serviceAction
	 *            the service action
	 * @return the retry commands
	 */
	private Map<EMAServiceMode, List<String>> getRetryCommands(final EMAInterfaceTypeEnum interfaceType,
			final EMAServiceMode mode, final EMAServiceMode failedMode, final FDPCommand failedCommand,
			final Map<EMAServiceMode, List<FDPCommand>> commandListMap, final ThreeGActivationCommandMode serviceAction) {
		Map<EMAServiceMode, List<String>> result = null;
		switch (interfaceType) {
		case CAI:
			result = new HashMap<EMAServiceMode, List<String>>();
			result.put(failedMode, getCmdNameListNextTo(failedCommand, commandListMap.get(mode)));
			break;
		case MML:
			result = getCmdNameListMML(mode, failedMode, failedCommand, commandListMap, serviceAction);
			break;
		default:
			break;
		}
		return result;
	}

	/**
	 * Gets the cmd name list mml.
	 * 
	 * @param mode
	 *            the mode
	 * @param failedMode
	 *            the failed mode
	 * @param failedCommand
	 *            the failed command
	 * @param commandListMap
	 *            the command list map
	 * @param serviceAction
	 *            the service action
	 * @return the cmd name list mml
	 */
	private Map<EMAServiceMode, List<String>> getCmdNameListMML(final EMAServiceMode mode,
			final EMAServiceMode failedMode, final FDPCommand failedCommand,
			final Map<EMAServiceMode, List<FDPCommand>> commandListMap, final ThreeGActivationCommandMode serviceAction) {
		Map<EMAServiceMode, List<String>> result = null;
		switch (serviceAction) {
		case UPDATE_ALL:
			result = getCmdNameListMMLUpdate(failedMode, failedCommand, commandListMap);
			break;
		case QUERY_AND_UPDATE_ALL:
			result = getCmdNameListMMLQnU(failedMode, failedCommand, commandListMap);
			break;
		default:
			break;
		}
		return result;
	}

	/**
	 * Gets the cmd name list mml qn u.
	 * 
	 * @param failedMode
	 *            the failed mode
	 * @param failedCommand
	 *            the failed command
	 * @param commandListMap
	 *            the command list map
	 * @return the cmd name list mml qn u
	 */
	private Map<EMAServiceMode, List<String>> getCmdNameListMMLQnU(final EMAServiceMode failedMode,
			final FDPCommand failedCommand, final Map<EMAServiceMode, List<FDPCommand>> commandListMap) {
		final Map<EMAServiceMode, List<String>> result = new HashMap<EMAServiceMode, List<String>>();
		switch (failedMode) {
		case TWO_G:
			result.put(EMAServiceMode.TWO_G, getCmdNameListQnUForMode(failedMode, failedCommand, commandListMap));
			result.put(EMAServiceMode.THREE_G, getCommandNameList(commandListMap.get(EMAServiceMode.THREE_G), null));
			break;
		case THREE_G:
			result.put(EMAServiceMode.THREE_G, getCmdNameListQnUForMode(failedMode, failedCommand, commandListMap));
			break;
		default:
			break;
		}
		return result;
	}

	/**
	 * Gets the cmd name list qn u for mode.
	 * 
	 * @param failedMode
	 *            the failed mode
	 * @param failedCommand
	 *            the failed command
	 * @param commandListMap
	 *            the command list map
	 * @return the cmd name list qn u for mode
	 */
	private List<String> getCmdNameListQnUForMode(final EMAServiceMode failedMode, final FDPCommand failedCommand,
			final Map<EMAServiceMode, List<FDPCommand>> commandListMap) {
		List<String> result = null;
		List<String> queryCommands = null;
		if (EMAServiceMode.TWO_G.equals(failedMode)) {
			queryCommands = ThreeGActivationCommandMode.QUERY.getCommandsForMMLTwoG();
		} else {
			queryCommands = ThreeGActivationCommandMode.QUERY.getCommandsForMMLThreeG();
		}
		if (!queryCommands.contains(failedCommand.getCommandDisplayName())) {
			result = new ArrayList<String>();
			result.addAll(queryCommands);
			result.addAll(getCmdNameListNextTo(failedCommand, commandListMap.get(failedMode)));
		} else {
			result = getCmdNameListNextTo(failedCommand, commandListMap.get(failedMode));
		}
		return result;
	}

	/**
	 * Gets the cmd name list mml update.
	 * 
	 * @param failedMode
	 *            the failed mode
	 * @param failedCommand
	 *            the failed command
	 * @param commandListMap
	 *            the command list map
	 * @return the cmd name list mml update
	 */
	private Map<EMAServiceMode, List<String>> getCmdNameListMMLUpdate(final EMAServiceMode failedMode,
			final FDPCommand failedCommand, final Map<EMAServiceMode, List<FDPCommand>> commandListMap) {
		final Map<EMAServiceMode, List<String>> result = new HashMap<EMAServiceMode, List<String>>();
		switch (failedMode) {
		case TWO_G:
			result.put(EMAServiceMode.TWO_G,
					getCmdNameListNextTo(failedCommand, commandListMap.get(EMAServiceMode.TWO_G)));
			result.put(EMAServiceMode.THREE_G, getCommandNameList(commandListMap.get(EMAServiceMode.THREE_G), null));
			break;
		case THREE_G:
			result.put(EMAServiceMode.THREE_G,
					getCmdNameListNextTo(failedCommand, commandListMap.get(EMAServiceMode.THREE_G)));
			break;
		default:
			break;
		}
		return result;
	}

	/**
	 * Gets the cmd name list next to.
	 * 
	 * @param command
	 *            the command
	 * @param commandList
	 *            the command list
	 * @return the cmd name list next to
	 */
	private List<String> getCmdNameListNextTo(final FDPCommand command, final List<FDPCommand> commandList) {
		List<String> result = null;
		int index = 0;
		for (final FDPCommand cmd : commandList) {
			if (cmd.getCommandDisplayName().equals(command.getCommandDisplayName())) {
				result = getCommandNameList(commandList.subList(index, commandList.size()), null);
				break;
			}
			index++;
		}
		return result;
	}

	/**
	 * Gets the command name list.
	 * 
	 * @param commandList
	 *            the command list
	 * @param addToList
	 *            the add to list
	 * @return the command name list
	 */
	private List<String> getCommandNameList(final List<FDPCommand> commandList, List<String> addToList) {
		if (addToList == null) {
			addToList = new ArrayList<String>();
		}
		if (commandList != null) {
			for (final FDPCommand retryCommand : commandList) {
				addToList.add(retryCommand.getCommandDisplayName());
			}
		}
		return addToList;
	}

	@Override
public FDPStepResponse performRollback(FDPRequest fdpRequest,
		Map<ServiceStepOptions, String> additionalInformation) {
	// TODO Auto-generated method stub
	return activationServiceImpl.performRollback(fdpRequest, additionalInformation);
}


}
