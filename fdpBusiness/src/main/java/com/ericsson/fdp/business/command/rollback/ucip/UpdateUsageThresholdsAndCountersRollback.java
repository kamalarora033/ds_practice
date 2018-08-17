package com.ericsson.fdp.business.command.rollback.ucip;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import com.ericsson.fdp.business.command.impl.NonTransactionCommand;
import com.ericsson.fdp.business.command.param.impl.CommandParamInput;
import com.ericsson.fdp.business.command.param.impl.CommandParamOutput;
import com.ericsson.fdp.business.command.rollback.helper.CommandRollbackHelper;
import com.ericsson.fdp.common.constants.FDPConstant;
import com.ericsson.fdp.common.enums.Status;
import com.ericsson.fdp.core.command.FDPCommand;
import com.ericsson.fdp.core.command.param.CommandParam;
import com.ericsson.fdp.core.exception.EvaluationFailedException;
import com.ericsson.fdp.core.exception.ExecutionFailedException;
import com.ericsson.fdp.core.request.FDPRequest;

/**
 * This class is used to implement rollback of updateUsageThresholdsAndCounters
 * command.
 * 
 * @author Ericsson
 * 
 */
public class UpdateUsageThresholdsAndCountersRollback extends NonTransactionCommand {

	/** The Constant serialVersionUID. */
	private static final long serialVersionUID = 763538084374901217L;

	/** The custom param usagecounterupdateinformation. */
	private final String CUSTOM_PARAM_USAGECOUNTERUPDATEINFORMATION = "usageCounterUpdateInformation";

	/** The custom param usagethresholdupdateinformation. */
	private final String CUSTOM_PARAM_USAGETHRESHOLDUPDATEINFORMATION = "usageThresholdUpdateInformation";

	/** The usage counter id parameter. */
	private final String USAGE_COUNTER_ID_PARAMETER = "usageCounterID";

	/** The usage counter value parameter. */
	private final String USAGE_COUNTER_VALUE_PARAMETER = "usageCounterValue";
	
	/** The usage counter monetary value1 parameter. */
	//private final String USAGE_COUNTER_MONETARY_VALUE_PARAMETER = "usageCounterMonetaryValue1";

	/** The usage counter value new parameter. */
	private final String USAGE_COUNTER_VALUE_NEW_PARAMETER = "usageCounterValueNew";
	
	/** The usage counter monetary value new parameter. */
	//private final String USAGE_COUNTER_MONETARY_VALUE_NEW_PARAMETER = "usageCounterMonetaryValueNew";

	/** The usage threshold id parameter. */
	private final String USAGE_THRESHOLD_ID_PARAMETER = "usageThresholdID";

	/** The usage threshold value parameter. */
	private final String USAGE_THRESHOLD_VALUE_PARAMETER = "usageThresholdValue";
	
	/** The usage threshold monetary value1 parameter. */
	//private final String USAGE_THRESHOLD_MONETARY_VALUE_PARAMETER = "usageThresholdMonetaryValue1";

	/** The usage threshold value new parameter. */
	private final String USAGE_THRESHOLD_VALUE_NEW_PARAMETER = "usageThresholdValueNew";
	
	/** The usages usage Threshold Monetary Value New parameter */
	//private final String USAGE_THRESHOLD_MONETARY_VALUE_NEW_PARAMETER = "usageThresholdMonetaryValueNew";

	/** The usage threshold information. */
	private final String USAGE_THRESHOLD_INFORMATION = "usageThresholdInformation";
	
	/** The transaction currency. */
	//private final String TRANSACTION_CURRENCY = "transactionCurrency";

	/** The dot separator. */
	private final String DOT_SEPARATOR = FDPConstant.PARAMETER_SEPARATOR;

	/**
	 * The constructor for the update usage threshold and counters rollback
	 * class.
	 * 
	 * @param commandDisplayName
	 *            The command display name.
	 */
	public UpdateUsageThresholdsAndCountersRollback(final String commandDisplayName) {
		super(commandDisplayName);
	}

	@Override
	public Status execute(final FDPRequest fdpRequest, final Object... otherParams) throws ExecutionFailedException {
		try {
			// extracting command parameters from two source commands
			extractionFromSourceCommands(fdpRequest, otherParams);

			// Generate XML file for the parameter and append it to xml.
			return executeCommand(fdpRequest);
		} catch (final EvaluationFailedException e) {
			throw new ExecutionFailedException("Could not execute command", e);
		}
	}

	/**
	 * Extraction from source commands.
	 * 
	 * @param fdpRequest
	 *            the fdp request
	 * @param otherParams
	 *            the other params
	 * @throws EvaluationFailedException
	 *             the evaluation failed exception
	 * @throws ExecutionFailedException
	 *             the execution failed exception
	 */
	private void extractionFromSourceCommands(final FDPRequest fdpRequest, final Object... otherParams)
			throws EvaluationFailedException, ExecutionFailedException {
		boolean usageThresholdUpdateInformationFound = false;
		boolean usageCounterUpdateInformationFound = false;
		boolean isCurrencyExist = false;
		
		final Iterator<CommandParam> commandParamIterator = getInputParam().iterator();
		while (commandParamIterator.hasNext()) {
			final CommandParam commandParam = commandParamIterator.next();
			if (CUSTOM_PARAM_USAGECOUNTERUPDATEINFORMATION.equals(commandParam.getName())) {
				usageCounterUpdateInformationFound = updateUsageCounterUpdateInformation(commandParam, fdpRequest,
						otherParams != null ? otherParams[0] : null);
				if (!usageCounterUpdateInformationFound) {
					commandParamIterator.remove();
				}
			} else if (CUSTOM_PARAM_USAGETHRESHOLDUPDATEINFORMATION.equals(commandParam.getName())) {
				usageThresholdUpdateInformationFound = updateUsageThresholdUpdateInformation(commandParam, fdpRequest,
						otherParams != null ? otherParams[0] : null);
				if (!usageThresholdUpdateInformationFound) {
					commandParamIterator.remove();
				}
			} else if ("transactionCurrency".equals(commandParam.getName())) {
				if (otherParams[0] != null && otherParams[0] instanceof FDPCommand) {
					FDPCommand command = (FDPCommand) otherParams[0];
					List<CommandParam> commandParamList = command.getInputParam();
					for (CommandParam param : commandParamList) {
						if (param.getName().equals("transactionCurrency")) {
							Object value = param.getValue();
							CommandParamInput commandParamInput = (CommandParamInput)commandParam;
							commandParamInput.setValue(value);
							isCurrencyExist = true;
							break;
						}
					}
					if (!isCurrencyExist) {
						commandParamIterator.remove();
					}
				}
			}
			else if (commandParam instanceof CommandParamInput) {
				final CommandParamInput input = (CommandParamInput) commandParam;
				input.evaluateValue(fdpRequest);
			}
		}
		
		if (!usageThresholdUpdateInformationFound && !usageCounterUpdateInformationFound) {
			// Could not find any of the required parameter.
			throw new ExecutionFailedException("Could not find counter update or threshold update information");
		}
	}

	/**
	 * Update usage counter update information.
	 * 
	 * @param commandParam
	 *            the command param
	 * @param fdpRequest
	 *            the fdp request
	 * @param otherParam
	 *            the other param
	 * @return true, if successful
	 * @throws ExecutionFailedException
	 *             the execution failed exception
	 */
	private boolean updateUsageCounterUpdateInformation(final CommandParam commandParam, final FDPRequest fdpRequest,
			final Object otherParam) throws ExecutionFailedException {
		final Map<Object, CommandParam> updatedParamMapForTransaction = new LinkedHashMap<Object, CommandParam>();
		Object value = null;
		Boolean isCounterValueNew = false;
		Boolean isCounterMonetaryValueNew = false;
		boolean isPresent = false;
		if (commandParam instanceof CommandParamInput) {
			
			final CommandParamInput commandParamInput = (CommandParamInput) commandParam;
			final CommandParamOutput paramOutput = (CommandParamOutput) commandParamInput.getDefinedValue();
			final FDPCommand fdpGetCommand = fdpRequest.getExecutedCommand(paramOutput.getCommand()
					.getCommandDisplayName());
			// Getting the flatten params string from the command output param
			final String flattenParam = paramOutput.flattenParam();
			
			if (null == fdpGetCommand) {
				throw new ExecutionFailedException("The dependent command "
						+ paramOutput.getCommand().getCommandDisplayName() + " was not executed");
			}
			// Creating a map to maintain the key-value pair of parameterId and
			// commandparam object
			final Map<Object, List<CommandParam>> commandParameterMap = new LinkedHashMap<Object, List<CommandParam>>();
			int i = 0;
			// Iterating till the output param from the get command is not null
			while (fdpGetCommand
					.getOutputParam((flattenParam + DOT_SEPARATOR + i + DOT_SEPARATOR + USAGE_COUNTER_ID_PARAMETER)
							.toLowerCase()) != null) {
				
				List<CommandParam> paramList = new ArrayList<CommandParam>();
				
				final CommandParam outputParam = fdpGetCommand.getOutputParam((flattenParam + DOT_SEPARATOR + i
						+ DOT_SEPARATOR + USAGE_COUNTER_ID_PARAMETER).toLowerCase());
				
				final CommandParam commandParamUsageCounterValue = fdpGetCommand.getOutputParam((flattenParam
						+ DOT_SEPARATOR + i + DOT_SEPARATOR + USAGE_COUNTER_VALUE_PARAMETER).toLowerCase());
				
				if (commandParamUsageCounterValue != null) {
					paramList.add(commandParamUsageCounterValue);
				}
				
				
				final CommandParam commandParamMonetaryValue = fdpGetCommand.getOutputParam((flattenParam
						+ DOT_SEPARATOR + i + DOT_SEPARATOR + "usageCounterMonetaryValue1").toLowerCase());
				
				if (commandParamMonetaryValue != null) {
					paramList.add(commandParamMonetaryValue);
				}
				commandParameterMap.put(outputParam.getValue(), paramList);
				i = i + 1;
			}
			if (otherParam instanceof FDPCommand) {
				final FDPCommand executedUpdateCommand = (FDPCommand) otherParam;
				// Getting the command param for counter usage information
				// element from the update command fired
				final CommandParam commandParamForCounterUsage = executedUpdateCommand
						.getInputParam(CUSTOM_PARAM_USAGECOUNTERUPDATEINFORMATION);
				// usageCounterUpdateInformation element was not found in update
				// command, as it is not mandatory.
				if (null == commandParamForCounterUsage) {
					return isPresent;
				}
				final List<CommandParam> counterUsageChildren = commandParamForCounterUsage.getChilderen();
				// Iterating on children to find the usageCounterID element from
				// the command params
				for (final CommandParam child : counterUsageChildren) {
					final List<CommandParam> commandParams = child.getChilderen();
					for (final CommandParam commandParamObject : commandParams) {
						// Checking if the usageCounterID is found while
						// traversing the command params.
						if (USAGE_COUNTER_ID_PARAMETER.equals(commandParamObject.getName())) {
							value = commandParamObject.getValue();
							updatedParamMapForTransaction.put(value, commandParamObject);
							final List<CommandParam> commandParamValueObjList = commandParameterMap.get(Integer.parseInt(value.toString()));
							// Checking if usageCounterID value is present in
							// the map container holding the params from Get
							// command.
							if (commandParamValueObjList != null) {
								
								for (int inputCount = 0; inputCount <  commandParamValueObjList.size(); inputCount++) {
									CommandParam param = commandParamValueObjList.get(inputCount);
									
									if (param.getName().contains(USAGE_COUNTER_VALUE_PARAMETER)) {
										for (int count = 0; count < commandParams.size(); count ++) {
											if (commandParams.get(count).getName().equalsIgnoreCase(USAGE_COUNTER_VALUE_NEW_PARAMETER)) {
												
												updatedParamMapForTransaction.put(value, param);
												isCounterValueNew = true;
											}
										}
									} else if (param.getName().contains("usageCounterMonetaryValue1")) {
										for (int count = 0; count < commandParams.size(); count ++) {
											if (commandParams.get(count).getName().equalsIgnoreCase("usageCounterMonetaryValueNew")) {
												
												updatedParamMapForTransaction.put(value, param);
												isCounterMonetaryValueNew = true;
											}
										}
									}
								}
							}
							
						}
					}
				}
			} else {
				throw new ExecutionFailedException("");
			}
		}
		// Checking if the new updatedParamMapForTransaction is empty or not to
		// change the element present flag
		// and transforming the param for rollback command
		if (!updatedParamMapForTransaction.isEmpty()) {
			if (isCounterValueNew ) {
				CommandRollbackHelper.transformationToRollBack(commandParam, updatedParamMapForTransaction,
						USAGE_COUNTER_ID_PARAMETER, USAGE_COUNTER_VALUE_NEW_PARAMETER);
				isPresent = true;
			} else if (isCounterMonetaryValueNew) {
				CommandRollbackHelper.transformationToRollBack(commandParam, updatedParamMapForTransaction,
						USAGE_COUNTER_ID_PARAMETER, "usageCounterMonetaryValueNew");
				isPresent = true;
			} else {
				CommandRollbackHelper.transformationToRollBack(commandParam, updatedParamMapForTransaction,
						USAGE_COUNTER_ID_PARAMETER, null);
				isPresent = true;
			}
		}
		
		return isPresent;
	}

	/**
	 * Update usage threshold update information.
	 * 
	 * @param commandParam
	 *            the command param
	 * @param fdpRequest
	 *            the fdp request
	 * @param otherParam
	 *            the other param
	 * @return true, if successful
	 * @throws ExecutionFailedException
	 *             the execution failed exception
	 */
	private boolean updateUsageThresholdUpdateInformation(final CommandParam commandParam, final FDPRequest fdpRequest,
			final Object otherParam) throws ExecutionFailedException {
		final Map<Object, List<CommandParam>> getParamMapForUTTransaction = new LinkedHashMap<Object, List<CommandParam>>();
		final Map<Object, CommandParam> updatedParamMapForTransaction = new LinkedHashMap<Object, CommandParam>();
		boolean isPresent = false;
		boolean isThresholdValueNew = false;
		boolean isThreasholdMonetaryValue = false;
		
		if (commandParam instanceof CommandParamInput) {
			
			final CommandParamInput commandParamInput = (CommandParamInput) commandParam;
			final CommandParamOutput paramOutput = (CommandParamOutput) commandParamInput.getDefinedValue();
			final FDPCommand fdpGetCommand = fdpRequest.getExecutedCommand(paramOutput.getCommand()
					.getCommandDisplayName());
			// Getting the flatten params string from the command output param
			final String flattenParam = paramOutput.flattenParam();
			
			if (null == fdpGetCommand) {
				throw new ExecutionFailedException("The dependent command "
						+ paramOutput.getCommand().getCommandDisplayName() + " was not executed");
			}
			// Creating a map to maintain the key-value pair of parameterId and
			// commandparam object
			
			int i = 0;
			
			// Iterating till the output param for usageCounterID from the get
			// command is not null
			while (fdpGetCommand
					.getOutputParam((flattenParam + DOT_SEPARATOR + i + DOT_SEPARATOR + USAGE_COUNTER_ID_PARAMETER)
							.toLowerCase()) != null) {
				int j = 0;
				
				// Iterating till the output param for usageThresholdID from the
				// get command is not null
				while (fdpGetCommand
						.getOutputParam((flattenParam + DOT_SEPARATOR + i + DOT_SEPARATOR + USAGE_THRESHOLD_INFORMATION
								+ DOT_SEPARATOR + j + DOT_SEPARATOR + USAGE_THRESHOLD_ID_PARAMETER).toLowerCase()) != null) {
					
					List<CommandParam> paramList = new ArrayList<CommandParam>();
					
					final CommandParam outputParam = fdpGetCommand.getOutputParam((flattenParam + DOT_SEPARATOR + i + DOT_SEPARATOR
							+ USAGE_THRESHOLD_INFORMATION + DOT_SEPARATOR + j + DOT_SEPARATOR + USAGE_THRESHOLD_ID_PARAMETER).toLowerCase());
					
					final CommandParam commandParamUsageThresholdValue = fdpGetCommand.getOutputParam((flattenParam
							+ DOT_SEPARATOR + i + DOT_SEPARATOR + USAGE_THRESHOLD_INFORMATION + DOT_SEPARATOR + j
							+ DOT_SEPARATOR + USAGE_THRESHOLD_VALUE_PARAMETER).toLowerCase());
					if (commandParamUsageThresholdValue != null) {
						paramList.add(commandParamUsageThresholdValue);
					}
					
					final CommandParam commandParamUsageThresholdMonetaryValue = fdpGetCommand.getOutputParam((flattenParam
							+ DOT_SEPARATOR + i + DOT_SEPARATOR + USAGE_THRESHOLD_INFORMATION + DOT_SEPARATOR + j
							+ DOT_SEPARATOR + "usageThresholdMonetaryValue1").toLowerCase());
					if (commandParamUsageThresholdMonetaryValue != null) {
						paramList.add(commandParamUsageThresholdMonetaryValue);
					}
					
					getParamMapForUTTransaction.put(outputParam.getValue(), paramList);
					j = j + 1;
				}
				
				i = i + 1;
				
			}
			
			if (otherParam instanceof FDPCommand) {
				final FDPCommand executedUpdateCommand = (FDPCommand) otherParam;
				// Getting the command param for threshold usage information
				// element from the update command fired
				final CommandParam commandParamForThresholdUsage = executedUpdateCommand
						.getInputParam(CUSTOM_PARAM_USAGETHRESHOLDUPDATEINFORMATION);
				
				// usageThresholdUpdateInformation element was not found in
				// update command, as it is not mandatory.
				if (null == commandParamForThresholdUsage) {
					return isPresent;
				}
				
				final List<CommandParam> thresholdParamChildren = commandParamForThresholdUsage.getChilderen();
				
				// Iterating on children to find the usageCounterID element from
				// the command params
				for (final CommandParam child : thresholdParamChildren) {
					final List<CommandParam> commandParamsList = child.getChilderen();
					for (final CommandParam commandParamObject : commandParamsList) {
						
						// Checking if the usageThresholdID is found while
						// traversing the command params.
						if (USAGE_THRESHOLD_ID_PARAMETER.equals(commandParamObject.getName())) {
							
							final Object value = commandParamObject.getValue();
							final List<CommandParam> commandParamValueObjList = getParamMapForUTTransaction.get(Integer.parseInt(value.toString()));
							
							// Checking if usageThresholdID value is present in
							// the map container holding the params from Get
							// command.
							if (commandParamValueObjList != null) {
								
								for (int inputCount = 0; inputCount <  commandParamValueObjList.size(); inputCount++) {
									CommandParam param = commandParamValueObjList.get(inputCount);
									
									if (param.getName().contains(USAGE_THRESHOLD_VALUE_PARAMETER)) {
										for (int count = 0; count < commandParamsList.size(); count ++) {
											if (commandParamsList.get(count).getName().equalsIgnoreCase(USAGE_THRESHOLD_VALUE_NEW_PARAMETER)) {
												
												updatedParamMapForTransaction.put(value, param);
												isThresholdValueNew = true;
											}
										}
									} else if (param.getName().contains("usageThresholdMonetaryValue1")) {
										for (int count = 0; count < commandParamsList.size(); count ++) {
											if (commandParamsList.get(count).getName().equalsIgnoreCase("usageThresholdMonetaryValueNew")) {
												
												updatedParamMapForTransaction.put(value, param);
												isThreasholdMonetaryValue = true;
											}
										}
									}
								}
							}			
						}
					}
				}
			}
			else {
				throw new ExecutionFailedException("");
			}
		}
		// Checking if the new updatedParamMapForTransaction is empty or not to
		// change the element present flag
		// and transforming the param for rollback command
		if (!updatedParamMapForTransaction.isEmpty()) {
			if (isThresholdValueNew) {
				CommandRollbackHelper.transformationToRollBack(commandParam, updatedParamMapForTransaction,
						USAGE_THRESHOLD_ID_PARAMETER, USAGE_THRESHOLD_VALUE_NEW_PARAMETER);
				isPresent = true;
			} else if (isThreasholdMonetaryValue) {
				CommandRollbackHelper.transformationToRollBack(commandParam, updatedParamMapForTransaction,
						USAGE_THRESHOLD_ID_PARAMETER, "usageThresholdMonetaryValueNew");
				isPresent = true;
			}
		}
			
		return isPresent;
		}
	}