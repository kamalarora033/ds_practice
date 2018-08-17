package com.ericsson.fdp.business.command.rollback.mm;

import java.util.ArrayList;
import java.util.List;

import com.ericsson.fdp.FDPCacheable;
import com.ericsson.fdp.business.command.AbstractCommand;
import com.ericsson.fdp.business.command.impl.NonTransactionCommand;
import com.ericsson.fdp.business.command.param.impl.CommandParamInput;
import com.ericsson.fdp.business.enums.Command;
import com.ericsson.fdp.business.util.CommandUtil;
import com.ericsson.fdp.common.constants.FDPConstant;
import com.ericsson.fdp.common.enums.ModuleType;
import com.ericsson.fdp.common.enums.ParameterFeedType;
import com.ericsson.fdp.common.enums.Status;
import com.ericsson.fdp.core.command.FDPCommand;
import com.ericsson.fdp.core.command.param.CommandParam;
import com.ericsson.fdp.core.exception.ExecutionFailedException;
import com.ericsson.fdp.core.request.FDPRequest;
import com.ericsson.fdp.core.serviceprovisioning.bean.FDPMetaBag;
import com.ericsson.fdp.core.utils.ApplicationConfigUtil;
import com.ericsson.fdp.dao.enums.CommandParameterType;

/**
 * 
 * @author GUR21122
 * This will reverse the amount deducted from Mobile Money account if error occurs during provisioning  
 */
public class MOMODebitRollback extends NonTransactionCommand {

	/**
	 * The Constant serialVersionUID.
	 */
	private static final long serialVersionUID = -6877315817618544002L;
	private static final String GETAMOUNT = "amount";
	private static final String XMLNS="xmlns:p";
	private static final String TRANSACTION="transactionid";
	private static final String FINANCIALTRANSACTION="financialtransactionid";
	
	/**
	 * Instantiates a new Mobile money rollback. refund command is used
	 * 
	 * @param commandDisplayName
	 *            the command display name
	 */
	public MOMODebitRollback(final String commandDisplayName) {
		super(commandDisplayName);
	}

	/**
	 * Execute MM Rollback Refund command is fired
	 */
	@Override
	public Status execute(final FDPRequest fdpRequest, final Object... otherParams) throws ExecutionFailedException{

		FDPCacheable cachedCommand = ApplicationConfigUtil.getMetaDataCache()
				.getValue(new FDPMetaBag(fdpRequest.getCircle(), ModuleType.COMMAND, Command.REFUND_MM.getCommandDisplayName()));

		AbstractCommand refundcommand = (AbstractCommand) cachedCommand;
		final FDPCommand mmCommand = fdpRequest.getExecutedCommand(FDPConstant.MM_CHARGING_COMMAND);
		final FDPCommand getTransactionCommand = fdpRequest.getExecutedCommand(Command.GET_TRANSACTION_STATUS.getCommandDisplayName());
		List<CommandParam> commandparamlst=new ArrayList<>();
		if (otherParams != null && otherParams[0] != null && otherParams[0] instanceof FDPCommand) {
			refundcommand.setInputParam(commandparamlst);
			List<CommandParam> commandinputparam = mmCommand.getInputParam();
			setOtherParams(refundcommand, commandinputparam);
			setTransactionParam(refundcommand, mmCommand, getTransactionCommand);
		}
		refundcommand.setRollbackCommand(true);
		return CommandUtil.executeCommand(fdpRequest, refundcommand, true);
	}

	/**
	 * Set the Financial 
	 * @param refundcommand
	 * @param mmCommand
	 * @param getTransactionStatusCommand
	 */
	private void setTransactionParam(AbstractCommand refundcommand, FDPCommand mmCommand, FDPCommand getTransactionStatusCommand) {
		FDPCommand command = null;
		
		if (getTransactionStatusCommand == null) {
			command = mmCommand;
		} else {
			command = getTransactionStatusCommand;
		}
		CommandParamInput commandparaminput = new CommandParamInput(ParameterFeedType.INPUT,
				command.getOutputParam(TRANSACTION).getValue());
		commandparaminput.setName(FINANCIALTRANSACTION);
		commandparaminput.setType(CommandParameterType.PRIMITIVE);
		commandparaminput.setValue(command.getOutputParam(TRANSACTION).getValue());
		commandparaminput.setCommand(mmCommand);
		refundcommand.getInputParam().add(commandparaminput);
	}

	private void setOtherParams(FDPCommand command, List<CommandParam> inputParam) {
		
		for (CommandParam tempcommandparam : inputParam) {
			replaceRefundCommandParam(tempcommandparam, command);
		}
	
	}
	
	/**
	 * Set Parameter xmlns and amount for refund command
	 * @param tempcommandparam
	 * @param command
	 */
	private void replaceRefundCommandParam(CommandParam tempcommandparam, FDPCommand command) {

		AbstractCommand abstractcommand=(AbstractCommand)command;
		if (tempcommandparam.getName().equals(XMLNS) || tempcommandparam.getName().equals(GETAMOUNT)) 
		{
				abstractcommand.getInputParam().add(tempcommandparam);
		}
		
	}

}
