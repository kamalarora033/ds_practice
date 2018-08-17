package com.ericsson.fdp.business.step.execution.impl.blackberryserviceactivation;

import java.util.List;
import java.util.Map;

import javax.ejb.Stateless;
import javax.inject.Inject;

import com.ericsson.fdp.business.enums.AuxRequestParam;
import com.ericsson.fdp.business.enums.EMAServiceMode;
import com.ericsson.fdp.business.enums.ServiceStepOptions;
import com.ericsson.fdp.business.enums.ThreeGActivationCommandMode;
import com.ericsson.fdp.business.step.execution.FDPExecutionService;
import com.ericsson.fdp.business.step.execution.impl.activation.ActivationCommandFinderService;
import com.ericsson.fdp.business.step.execution.impl.activation.impl.ActivationServiceImpl;
import com.ericsson.fdp.business.util.RequestUtil;
import com.ericsson.fdp.core.command.FDPCommand;
import com.ericsson.fdp.core.exception.ExecutionFailedException;
import com.ericsson.fdp.core.request.FDPRequest;
import com.ericsson.fdp.core.request.FDPStepResponse;


/**
 * This class is used to find the tariff enquiry.
 * 
 * @author Ericsson
 * 
 */
@Stateless
public class BlackBerryServiceImpl implements FDPExecutionService {

	@Inject
	private ActivationServiceImpl activationServiceImpl;

	/** The activation command finder service. */
	@Inject
	private ActivationCommandFinderService activationCommandFinderService;
	
	
	@Override
	public FDPStepResponse executeService(FDPRequest fdpRequest, Object... additionalInformations)
			throws ExecutionFailedException {
		final Map<EMAServiceMode, List<FDPCommand>> commandsToExecute = preProcess(fdpRequest, additionalInformations);
		final FDPStepResponse fdpStepResponseImpl = executeCommands(fdpRequest, commandsToExecute);
		
		return fdpStepResponseImpl;
	}


	private FDPStepResponse executeCommands(FDPRequest fdpRequest,
			Map<EMAServiceMode, List<FDPCommand>> commandsToExecute) throws ExecutionFailedException {
		RequestUtil.putAuxiliaryValueInRequest(fdpRequest, AuxRequestParam.ICR_NOTIFICATION_SENT, Boolean.FALSE);
		FDPStepResponse fdpStepResponse = activationServiceImpl.executeActivationCommands(fdpRequest,
				commandsToExecute.get(EMAServiceMode.SERVICE_ACTIVATION), null, EMAServiceMode.SERVICE_ACTIVATION);
		
		return fdpStepResponse;
	}


	private Map<EMAServiceMode, List<FDPCommand>> preProcess(FDPRequest fdpRequest, Object[] additionalInformations) throws ExecutionFailedException {
		return activationCommandFinderService.findCommands(fdpRequest, null,EMAServiceMode.SERVICE_ACTIVATION );
	}


	@Override
	public FDPStepResponse performRollback(FDPRequest fdpRequest,
			Map<ServiceStepOptions, String> additionalInformation) {
		// TODO Auto-generated method stub
		return null;
	}

}
