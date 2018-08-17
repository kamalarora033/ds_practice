package com.ericsson.fdp.business.command.rollback.evds;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.List;
import java.util.Map;

import com.ericsson.fdp.business.command.impl.NonTransactionCommand;
import com.ericsson.fdp.business.command.param.impl.CommandParamInput;
import com.ericsson.fdp.business.enums.ConfigurationKey;
import com.ericsson.fdp.business.util.FulfillmentUtil;
import com.ericsson.fdp.common.constants.FDPConstant;
import com.ericsson.fdp.common.enums.Status;
import com.ericsson.fdp.core.command.FDPCommand;
import com.ericsson.fdp.core.command.param.CommandParam;
import com.ericsson.fdp.core.exception.EvaluationFailedException;
import com.ericsson.fdp.core.exception.ExecutionFailedException;
import com.ericsson.fdp.core.request.FDPRequest;

/**
 * This class is used to implement rollback of requestTransfer command.
 * 
 * @author Ericsson
 */
public class EVDSRequestTransferRollback extends NonTransactionCommand {

	private static final long serialVersionUID = 7731131873200815605L;
	
	private static final String ROLLBACK = "ROLLBACK_";
	
	private static final String INITIATOR_ID = "id";
	private static final String USER_ID = "userId";
	private static final String SENDER_ID = "id";
	private static final String SENDER_TYPE = "type";
	private static final String PASSWORD = "password";
	private static final String RECEIVER_ID = "id";
	private static final String RECEIVER_TYPE = "type";
	private static final String CLIENT_REFERENCE = "clientReference";
	/**
	 * Instantiates REQUEST TRANSFER rollback.
	 * 
	 * @param commandDisplayName
	 *            the command display name
	 */
	public EVDSRequestTransferRollback(final String commandDisplayName) {
		super(commandDisplayName);
	}

	/**
	 * This method will change the TRANS_TYPE from type debit(113) to
	 * credit(112) and execute the wallet adjustment command
	 */
	@Override
	public Status execute(final FDPRequest fdpRequest,
			final Object... otherParams) throws ExecutionFailedException {
		if (otherParams != null && otherParams[0] != null && otherParams[0] instanceof FDPCommand) {
			 //CommandParamInput senderId=null;
			try {
				CommandParamInput initiatorid = null;
				CommandParamInput userid = null;
				CommandParamInput clientreference = null;
				CommandParamInput password = null;
				CommandParamInput senderid = null;
				CommandParamInput receiverid = null;
				CommandParamInput senderType = null;
				CommandParamInput receiverType = null;
				final FDPCommand executedCommand = (FDPCommand) otherParams[0];
				if (executedCommand.getInputParam("productId") != null) {
					CommandParam context=executedCommand.getInputParam("context");
					if(context!=null){
						List<CommandParam> contextChildren = context.getChilderen();
						for (final CommandParam child : contextChildren) {
							final CommandParamInput initiatorParamInput = (CommandParamInput) child;
							final String paramName = child.getName();
							if(null!=initiatorParamInput.getChilderen() && initiatorParamInput.getChilderen().size()>0){
								List<CommandParam> initiatorChildren =initiatorParamInput.getChilderen();
								for (final CommandParam initiatorChilds : initiatorChildren) {
									final String param = initiatorChilds.getName();
									if (INITIATOR_ID.equals(param)) {
										 initiatorid= (CommandParamInput) initiatorChilds;
									} else if (USER_ID.equals(param)) {
										 userid= (CommandParamInput) initiatorChilds;
								}
							}
							}else{
							if (CLIENT_REFERENCE.equals(paramName)) {
								 clientreference = (CommandParamInput) child;
							} else if (PASSWORD.equals(paramName)) {
								 password = (CommandParamInput) child;
							} 
						}
					}
					}
					CommandParam senderPrincipalId=executedCommand.getInputParam("senderPrincipalId");
					if(null!=senderPrincipalId){
						List<CommandParam> senderPrincipalIdChildren  = senderPrincipalId.getChilderen();
						for (final CommandParam child : senderPrincipalIdChildren) {
							final String paramName = child.getName();
							if (SENDER_ID.equals(paramName)) {
								 senderid = (CommandParamInput) child;
							}
							else if (SENDER_TYPE.equals(paramName)) {
								 senderType = (CommandParamInput) child;
							}
						}
					}
					CommandParam receiverPrincipalId = executedCommand.getInputParam("receiverPrincipalId");
					
					if(null!=receiverPrincipalId){
						List<CommandParam> receiverPrincipalIdChildren  = receiverPrincipalId.getChilderen();
						for (final CommandParam child : receiverPrincipalIdChildren) {
							final String paramName = child.getName();
							if (RECEIVER_ID.equals(paramName)) {
								 receiverid = (CommandParamInput) child;
							}
							else if (RECEIVER_TYPE.equals(paramName)) {
								 receiverType = (CommandParamInput) child;
							}
						}
						
					}
					
					final Map<String, String> configurationMap = fdpRequest.getCircle().getConfigurationKeyValueMap();
					final String refundUserId = configurationMap.get(ConfigurationKey.EVDS_ROLLBACK_USERID.getAttributeName());
					final Map<String, String> configurationMappass = fdpRequest.getCircle().getConfigurationKeyValueMap();
					final String refundPassword = configurationMappass.get(ConfigurationKey.EVDS_ROLLBACK_PASSWORD.getAttributeName());
					initiatorid.setValue(receiverid.getValue());
					senderid.setValue(receiverid.getValue());
					receiverid.setValue(fdpRequest.getSubscriberNumber());
					Object sender = senderType.getValue();
					Object receiver = receiverType.getValue();
					senderType.setValue(receiver);
					receiverType.setValue(sender);
					clientreference.setValue(ROLLBACK+clientreference.getValue());
					userid.setValue(refundUserId);
					password.setValue(refundPassword);
					this.setInputParam(executedCommand.getInputParam());
				}
				return executeCommand(fdpRequest);
			} catch (final EvaluationFailedException e) {
				throw new ExecutionFailedException("Could not execute command", e);
			}
		} else {
			throw new ExecutionFailedException("Could not find executed command");
		}
	}
	
	
}
