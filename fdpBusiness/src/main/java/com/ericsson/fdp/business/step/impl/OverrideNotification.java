package com.ericsson.fdp.business.step.impl;

import java.util.HashMap;
import java.util.Map;

import com.ericsson.fdp.business.command.param.impl.CommandParamInput;
import com.ericsson.fdp.business.enums.AuxRequestParam;
import com.ericsson.fdp.core.command.FDPCommand;
import com.ericsson.fdp.core.command.param.CommandParam;
import com.ericsson.fdp.core.exception.ExecutionFailedException;
import com.ericsson.fdp.core.request.FDPRequest;
import com.ericsson.fdp.core.request.impl.FDPRequestImpl;
import com.ericsson.fdp.dao.enums.SPNotificationType;

public class OverrideNotification extends CommandStep{

	private static final long serialVersionUID = 4267772905583171168L;

	public OverrideNotification(final FDPCommand fdpCommand, final String commandDisplayNameToSet, final Long stepId,
			final String stepName) {
		super(fdpCommand, commandDisplayNameToSet, stepId, stepName);
	}
	

	/**
	 * This method will set the notifications that need be overriden
	 *
	 * @param fdpRequest
	 *            the request.
	 * @throws ExecutionFailedException
	 *             Exception if any.
	 */
	protected void updateCommand(final FDPRequest fdpRequest) throws ExecutionFailedException {
		super.updateCommand(fdpRequest);
		overrideSPNotifications(fdpRequest);
	}
	
	/**
	 * This method will override the success/failure notifications specified in SP
	 * @param httpRequest
	 * @return
	 * @throws ExecutionFailedException 
	 */
	private boolean overrideSPNotifications(FDPRequest fdpRequest) throws ExecutionFailedException {
		Map<SPNotificationType, Long> notifsToOverride = new HashMap<SPNotificationType, Long>();
		for(Map.Entry<SPNotificationType, String> notificationType : SPNotificationType.getSuppressNotificationIdentifierMap().entrySet()){				
			CommandParam notifParam = fdpCommand.getInputParam(notificationType.getValue());
			if(null != notifParam){
				CommandParamInput cpi = (CommandParamInput)notifParam;
				if(null != cpi.getDefinedValue() && !cpi.getDefinedValue().toString().isEmpty()){
					notifsToOverride.put(notificationType.getKey(), Long.parseLong(cpi.getDefinedValue().toString()));
				}
			}
		}
		((FDPRequestImpl)fdpRequest).putAuxiliaryRequestParameter(AuxRequestParam.NOTIFICATION_TO_OVERRIDE, notifsToOverride);
	    return true;
	}

}
