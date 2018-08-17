package com.ericsson.fdp.business.step.execution.impl.activation.impl;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import com.ericsson.fdp.business.command.activation.FDPActivationCommand;
import com.ericsson.fdp.business.constants.FDPStepResponseConstants;
import com.ericsson.fdp.business.enums.ActivationCommands;
import com.ericsson.fdp.business.enums.EMAServiceMode;
import com.ericsson.fdp.business.enums.RequestMetaValuesKey;
import com.ericsson.fdp.business.enums.ServiceStepOptions;
import com.ericsson.fdp.business.enums.ThreeGActivationCommandMode;
import com.ericsson.fdp.business.product.Product;
import com.ericsson.fdp.business.request.impl.FDPStepResponseImpl;
import com.ericsson.fdp.business.serviceprovisioning.rule.ServiceProvisioningRule;
import com.ericsson.fdp.business.step.FDPStep;
import com.ericsson.fdp.business.step.execution.impl.ActivationService;
import com.ericsson.fdp.business.step.impl.ServiceStep;
import com.ericsson.fdp.common.enums.SPServices;
import com.ericsson.fdp.common.enums.Status;
import com.ericsson.fdp.core.command.FDPCommand;
import com.ericsson.fdp.core.config.utils.PropertyUtils;
import com.ericsson.fdp.core.exception.ExecutionFailedException;
import com.ericsson.fdp.core.request.FDPRequest;
import com.ericsson.fdp.core.request.FDPStepResponse;

/**
 * This class provides implementation for activation service.
 * 
 * @author Ericsson
 * 
 */
public class ActivationServiceImpl implements ActivationService {

	@Override
	public FDPStepResponse executeActivationCommands(final FDPRequest fdpRequest, final List<FDPCommand> fdpCommands,
			final ThreeGActivationCommandMode activationCommandMode, final EMAServiceMode emaServiceMode)
			throws ExecutionFailedException {
		final FDPStepResponseImpl fdpStepResponseImpl = new FDPStepResponseImpl();
		fdpStepResponseImpl.addStepResponseValue(FDPStepResponseConstants.STATUS_KEY, false);
		fdpStepResponseImpl.addStepResponseValue(FDPStepResponseConstants.EMA_SERVICE_MODE, emaServiceMode);
		Status status = Status.SUCCESS;
		FDPCommand exeCommand = null;
		if (fdpCommands != null && !fdpCommands.isEmpty()) {
			status = Status.FAILURE;
			for (final FDPCommand fdpCommand : fdpCommands) {
				exeCommand = fdpCommand;
				final ActivationCommands activationCommands = ActivationCommands.getValueFromCommandName(fdpCommand
						.getCommandDisplayName());
				if (activationCommands != null) {
					final FDPActivationCommand activationCommand = getActivationCommand(activationCommands, fdpCommand);
					status = activationCommand.execute(fdpRequest);
					if (!Status.SUCCESS.equals(status)) {
						break;
					}
				} else {
					throw new ExecutionFailedException("Cannot execute command " + fdpCommand.getCommandDisplayName()
							+ " as no execution method defined.");
				}
			}
		}
		updateStepResponse(fdpStepResponseImpl, status, fdpCommands, exeCommand, activationCommandMode, fdpRequest);
		return fdpStepResponseImpl;
	}

	/**
	 * This method is used to get the interceptor for the command.
	 * 
	 * @param activationCommands
	 *            the enum for the command.
	 * @param fdpCommand
	 *            the command.
	 * @return the interceptor
	 * @throws ExecutionFailedException
	 *             Exception in execution.
	 */
	private FDPActivationCommand getActivationCommand(final ActivationCommands activationCommands,
			final FDPCommand fdpCommand) throws ExecutionFailedException {
		final Class<? extends FDPActivationCommand> clazz = activationCommands.getClazz();
		try {
			final Constructor<? extends FDPActivationCommand> constructor = clazz.getConstructor(FDPCommand.class);
			return constructor.newInstance(fdpCommand);
		} catch (final NoSuchMethodException e) {
			throw new ExecutionFailedException("Could not instatiate interceptor class " + clazz, e);
		} catch (final SecurityException e) {
			throw new ExecutionFailedException("Could not instatiate interceptor class " + clazz, e);
		} catch (final InstantiationException e) {
			throw new ExecutionFailedException("Could not instatiate interceptor class " + clazz, e);
		} catch (final IllegalAccessException e) {
			throw new ExecutionFailedException("Could not instatiate interceptor class " + clazz, e);
		} catch (final IllegalArgumentException e) {
			throw new ExecutionFailedException("Could not instatiate interceptor class " + clazz, e);
		} catch (final InvocationTargetException e) {
			throw new ExecutionFailedException("Could not instatiate interceptor class " + clazz, e);
		}
	}

	/**
	 * This method is used to update the step response.
	 * 
	 * @param fdpStepResponseImpl
	 *            the step response to update.
	 * @param status
	 *            the status found.
	 * @param activationCommandMode
	 *            the activation command mode.
	 * @param exeCommand
	 *            the executed command.
	 * @param fdpCommands
	 *            the commands.
	 * @param fdpRequest
	 *            the request.
	 * @throws ExecutionFailedException
	 *             Exception in execution.
	 */
	private void updateStepResponse(final FDPStepResponseImpl fdpStepResponseImpl, final Status status,
			final List<FDPCommand> fdpCommands, final FDPCommand exeCommand,
			final ThreeGActivationCommandMode activationCommandMode, final FDPRequest fdpRequest)
			throws ExecutionFailedException {
		if (Status.SUCCESS.equals(status)) {
			fdpStepResponseImpl.addStepResponseValue(FDPStepResponseConstants.STATUS_KEY, true);
		} else if (Status.LOG_ON_FAIL.equals(status)) {
			fdpStepResponseImpl.addStepResponseValue(FDPStepResponseConstants.COMMAND_STATUS_KEY, true);
			fdpStepResponseImpl.addStepResponseValue(FDPStepResponseConstants.EMA_COMMAND_FAILED, exeCommand);
			fdpStepResponseImpl.addStepResponseValue(FDPStepResponseConstants.EMA_ACTIVATION_MODE,
					activationCommandMode);
		}
	}

	public FDPStepResponse performRollback(FDPRequest fdpRequest,
			Map<ServiceStepOptions, String> additionalInformation)   {
		FileWriter filewriter=null;
		try
		{
	
		Product product=(Product) fdpRequest.getValueFromRequest(RequestMetaValuesKey.PRODUCT);
		File file=new File(PropertyUtils.getProperty("EMA_ROLLBACK_LOGFILE_PATH"));
		if(!file.exists())
		{
			file.createNewFile();
		}
		filewriter=new FileWriter(file);
		String actionType=getActionType(fdpRequest);
		filewriter.append(fdpRequest.getIncomingSubscriberNumber()+","+product.getProductName()+",BlackBerryAction:"+actionType);
		filewriter.append("\n\r"+"--------");
		filewriter.flush();
		}
		catch(IOException ioe)
		{
			System.out.println("Rollback File Doesn't exists for EMA");
		}
		finally
		{
			if(filewriter!=null)
				try
			{
			filewriter.close();
			}
			catch (IOException e) {
				System.out.println("Not Able to close writer for EMA");
				// TODO: handle exception
			}
		}
		return null;
	}


	private String getActionType(FDPRequest fdpRequest) {
		ServiceProvisioningRule serviceprovisioningrule = (ServiceProvisioningRule) fdpRequest
				.getValueFromRequest(RequestMetaValuesKey.SERVICE_PROVISIONING);

		List<FDPStep> spfdpSteps = serviceprovisioningrule.getFdpSteps();
		for (Iterator iterator = spfdpSteps.iterator(); iterator.hasNext();) {
			FDPStep fdpStep = (FDPStep) iterator.next();
			if (fdpStep instanceof ServiceStep) {
				ServiceStep servicestep = (ServiceStep) fdpStep;
				if (servicestep.getJndiLookupName().equals(
						SPServices.EMA_SERVICE.getValue())) {
					return servicestep.getAdditionalInformation().get(
							ServiceStepOptions.PROVISION_ACTION_EMA);
				}
			}
		}
		return null;
}
	

}
