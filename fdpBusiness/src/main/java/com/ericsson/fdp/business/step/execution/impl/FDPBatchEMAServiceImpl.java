package com.ericsson.fdp.business.step.execution.impl;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.ejb.Stateless;

import com.ericsson.fdp.business.constants.FDPStepResponseConstants;
import com.ericsson.fdp.business.dto.emabatch.EMABatchRecordDTO;
import com.ericsson.fdp.business.enums.AuxRequestParam;
import com.ericsson.fdp.business.enums.EMAServiceMode;
import com.ericsson.fdp.business.enums.ServiceStepOptions;
import com.ericsson.fdp.business.enums.ThreeGActivationCommandMode;
import com.ericsson.fdp.business.util.CommandUtil;
import com.ericsson.fdp.business.util.RequestUtil;
import com.ericsson.fdp.common.vo.FDPCircle;
import com.ericsson.fdp.core.command.FDPCommand;
import com.ericsson.fdp.core.entity.FDPEMADetail;
import com.ericsson.fdp.core.exception.ExecutionFailedException;
import com.ericsson.fdp.core.request.FDPRequest;
import com.ericsson.fdp.core.request.FDPStepResponse;

/**
 * The execution service implementation.
 * 
 * @author Ericsson
 */
@Stateless
public class FDPBatchEMAServiceImpl extends AbstractFDPEMAServiceImpl {

	@Override
	@SuppressWarnings("unchecked")
	protected Map<EMAServiceMode, List<FDPCommand>> preProcess(final FDPRequest fdpRequest,
			final Object... additionalInformation) throws ExecutionFailedException {
		if (additionalInformation != null && additionalInformation[0] != null
				&& additionalInformation[0] instanceof Map<?, ?>) {
			final Map<ServiceStepOptions, Object> additionalMap = (Map<ServiceStepOptions, Object>) additionalInformation[0];
			final FDPEMADetail fdpemaDetail = RequestUtil.getEmaDetails(fdpRequest);
			if (fdpemaDetail != null
					&& fdpemaDetail.getInterfaceType().getValue()
							.equalsIgnoreCase((String) additionalMap.get(ServiceStepOptions.INTERFACE))) {
				final Map<EMAServiceMode, List<String>> commands = (Map<EMAServiceMode, List<String>>) additionalMap
						.get(ServiceStepOptions.COMMANDS);
				RequestUtil.putAuxiliaryValueInRequest(fdpRequest, AuxRequestParam.SERVICE_MODE,
						additionalMap.get(ServiceStepOptions.MODE));
				RequestUtil
						.putAuxiliaryValueInRequest(fdpRequest, AuxRequestParam.ACTIVATION_MODE,
								ThreeGActivationCommandMode.getEnumValue((String) additionalMap
										.get(ServiceStepOptions.ACTION)));
				RequestUtil.putAuxiliaryValueInRequest(fdpRequest, AuxRequestParam.ACTIVATION_ICR_MODE,
						Boolean.valueOf(additionalMap.get(ServiceStepOptions.CHECK_ICR_CIRCLE).toString()));
				return getCommands(fdpRequest.getCircle(), commands);
			} else {
				throw new ExecutionFailedException("The interfaces do not match. Found " + fdpemaDetail + " requested "
						+ (String) additionalMap.get(ServiceStepOptions.INTERFACE));
			}
		} else {
			throw new ExecutionFailedException("Input parameters are missing.");
		}
	}

	private Map<EMAServiceMode, List<FDPCommand>> getCommands(final FDPCircle fdpCircle,
			final Map<EMAServiceMode, List<String>> commands) throws ExecutionFailedException {
		final Map<EMAServiceMode, List<FDPCommand>> commandMap = new HashMap<EMAServiceMode, List<FDPCommand>>();
		for (final Map.Entry<EMAServiceMode, List<String>> commandsAsString : commands.entrySet()) {
			if (commandsAsString.getValue() != null && !commandsAsString.getValue().isEmpty()) {
				final List<FDPCommand> fdpCommands = new ArrayList<FDPCommand>();
				fdpCommands.addAll(CommandUtil.getCommandsFromDisplayName(fdpCircle, commandsAsString.getValue()));
				commandMap.put(commandsAsString.getKey(), fdpCommands);
			}
		}
		return commandMap;
	}

	@Override
	protected void postProcess(final FDPRequest fdpRequest, final FDPStepResponse fdpStepResponse,
			final Object... additionalInformation) throws ExecutionFailedException {
		// TODO: make changes to make it in sync with the latest logging values.
		final boolean stepExecuted = RequestUtil.checkIfLoggingRequired(fdpStepResponse);

		if (stepExecuted) {
			final EMABatchRecordDTO emaBatchRecordDTO = getEMABatchRecordDTO(fdpRequest, fdpStepResponse,
					additionalInformation);
			RequestUtil.appendResponse(fdpStepResponse, FDPStepResponseConstants.EMA_LOG_VALUE, emaBatchRecordDTO);
		}

	}

	@Override
	public FDPStepResponse performRollback(FDPRequest fdpRequest,
			Map<ServiceStepOptions, String> additionalInformation) {
		// TODO Auto-generated method stub
		return null;
	}

}
