package com.ericsson.fdp.business.command.rollback.helper;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.ericsson.fdp.business.command.param.impl.CommandParamInput;
import com.ericsson.fdp.common.enums.ParameterFeedType;
import com.ericsson.fdp.common.enums.Primitives;
import com.ericsson.fdp.core.command.param.CommandParam;
import com.ericsson.fdp.dao.enums.CommandParameterType;

/**
 * The Class CommandRollbackHelper.
 * 
 * @author Ericsson
 */
public class CommandRollbackHelper {

	/**
	 * Instantiates a new command rollback helper.
	 */
	private CommandRollbackHelper() {

	}

	/**
	 * Transformation to roll back.
	 * 
	 * @param commandParam
	 *            the command param
	 * @param updatedParamMapForTransaction
	 *            the updated param map for transaction
	 * @param idParamName
	 *            the id param name
	 * @param idValueParamName
	 *            the id value param name
	 */
	public static void transformationToRollBack(final CommandParam commandParam,
			final Map<Object, CommandParam> updatedParamMapForTransaction, final String idParamName,
			final String idValueParamName) {
		final CommandParamInput commandParamInput = (CommandParamInput) commandParam;
		final List<CommandParam> usageCounterChildParams = new ArrayList<CommandParam>();
		for (final Map.Entry<Object, CommandParam> entry : updatedParamMapForTransaction.entrySet()) {

			final List<CommandParam> usageCounterArrayChildren = new ArrayList<CommandParam>();
			// Creating a struct type command param to wrap child objects
			final CommandParamInput commandParamInputStruct = new CommandParamInput(ParameterFeedType.INPUT, null);
			commandParamInputStruct.setType(CommandParameterType.STRUCT);
			commandParamInputStruct.setParent(commandParamInput);

			final Object idObject = entry.getKey();
			final CommandParam valueObject = entry.getValue();
			// Adding a id command param
			usageCounterArrayChildren.add(getCommandInputObjectForPrimitive(commandParamInputStruct, idParamName,
					Primitives.INTEGER, idObject));
			// Adding a value command param
			if (idValueParamName != null) {
				usageCounterArrayChildren.add(getCommandParamInputObject(commandParamInputStruct, idValueParamName,
						valueObject));
			}

			commandParamInputStruct.setChilderen(usageCounterArrayChildren);
			usageCounterChildParams.add(commandParamInputStruct);
		}
		commandParamInput.setChilderen(usageCounterChildParams);
	}

	/**
	 * Gets the command param for input.
	 * 
	 * @param parent
	 *            the parent
	 * @param paramName
	 *            the param name
	 * @param valueObject
	 *            the value object
	 * @return the command param for input
	 */
	public static CommandParamInput getCommandParamInputObject(final CommandParamInput parent, final String paramName,
			final Object valueObject) {
		final CommandParam commandParam = (CommandParam) valueObject;
		final CommandParamInput commandParamChild = new CommandParamInput(ParameterFeedType.INPUT, null);
		commandParamChild.setName(paramName);
		commandParamChild.setType(commandParam.getType());
		commandParamChild.setParent(parent);
		commandParamChild.setPrimitiveValue(commandParam.getPrimitiveValue());
		commandParamChild.setValue(commandParam.getValue());
		return commandParamChild;
	}

	/**
	 * Gets the command input object for primitive.
	 * 
	 * @param parent
	 *            the parent
	 * @param paramName
	 *            the param name
	 * @param primitive
	 *            the primitive
	 * @param valueObject
	 *            the value object
	 * @return the command input object for primitive
	 */
	public static CommandParamInput getCommandInputObjectForPrimitive(final CommandParam parent,
			final String paramName, final Primitives primitive, final Object valueObject) {
		final CommandParamInput commandParamChild = new CommandParamInput(ParameterFeedType.INPUT, null);
		commandParamChild.setName(paramName);
		commandParamChild.setType(CommandParameterType.PRIMITIVE);
		commandParamChild.setParent(parent);
		commandParamChild.setPrimitiveValue(primitive);
		commandParamChild.setValue(valueObject);
		return commandParamChild;
	}
}
