package com.ericsson.fdp.business.cache.datageneration;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.SortedMap;
import java.util.SortedSet;
import java.util.TreeMap;
import java.util.TreeSet;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ericsson.fdp.business.command.AbstractCommand;
import com.ericsson.fdp.business.command.impl.NonTransactionCommand;
import com.ericsson.fdp.business.command.impl.TransactionCommand;
import com.ericsson.fdp.business.command.param.impl.CommandParamInput;
import com.ericsson.fdp.business.command.param.impl.CommandParamOutput;
import com.ericsson.fdp.business.enums.AuxRequestParam;
import com.ericsson.fdp.business.enums.Function;
import com.ericsson.fdp.common.constants.CommandCircleConstant;
import com.ericsson.fdp.common.constants.FDPConstant;
import com.ericsson.fdp.common.enums.ParameterFeedType;
import com.ericsson.fdp.common.enums.Primitives;
import com.ericsson.fdp.common.util.DateUtil;
import com.ericsson.fdp.core.command.FDPCommand;
import com.ericsson.fdp.core.command.param.CommandParam;
import com.ericsson.fdp.dao.dto.CommandDTO;
import com.ericsson.fdp.dao.dto.ParameterDTO;
import com.ericsson.fdp.dao.dto.serviceprov.ValidityValueDTO;
import com.ericsson.fdp.dao.enums.CommandParameterDataType;
import com.ericsson.fdp.dao.enums.CommandParameterType;
import com.ericsson.fdp.dao.enums.ProductAdditionalInfoEnum;
import com.ericsson.fdp.dao.enums.ValidityTypeEnum;

/**
 * The Class CommandCacheUtil provides utility methods for conversion of
 * List<CommandDTO>, commandDTO, ParameterDTO to List<FDPCommand>, FDPCommand,
 * CommandParam respectively.
 */
public final class CommandCacheUtil {

	/** The Constant LOGGER. */
	private final static Logger LOGGER = LoggerFactory.getLogger(CommandCacheUtil.class);

	/**
	 * private command cache util constructor so that class can't be
	 * initialized.
	 */
	private CommandCacheUtil() {
	}

	/**
	 * This method provides the conversion of Command dto list to fdp command
	 * list.
	 * 
	 * @param commandDTOList
	 *            the command dto list
	 * @return the list
	 */
	public static List<FDPCommand> commandDTOListToFDPCommandList(final List<CommandDTO> commandDTOList) {
		List<FDPCommand> commands = new ArrayList<FDPCommand>();
		if (commandDTOList != null && !commandDTOList.isEmpty()) {
			commands = new ArrayList<FDPCommand>();
			// Iterate through all the commandDTO's in the List,
			// Convert each parameterDTO to CommandParam by calling other
			// method,
			// Add the converted CommandParam to the List.
			for (final CommandDTO commandDTO : commandDTOList) {
				commands.add(commandDTOToFDPCommand(commandDTO));
			}
		}
		return commands;
	}

	/**
	 * This method provides the conversion of Command dto to fdp command with.
	 * 
	 * @param commandDTO
	 *            the command dto
	 * @return the fDP command
	 */
	public static FDPCommand commandDTOToFDPCommand(final CommandDTO commandDTO) {
		AbstractCommand command = null;
		if (commandDTO != null) {
			if (commandDTO.isRollback()) {
				// If command is a rollback command then create instance of
				// transactional command.
				command = new TransactionCommand(commandDTO.getCommandName());
				// TODO set rollback command
			} else {
				// If command is not a rollback command then create instance of
				// non-transactional command.
				command = new NonTransactionCommand(commandDTO.getCommandName());
			}
			command.setCommandDisplayName(commandDTO.getCommandDisplayName());
			// TODO : change name and display name in the parameterDTO
			command.setCommandName(commandDTO.getCommandName());
			command.setSystem(commandDTO.getSystem());
			command.setCommandType(commandDTO.getCommandDefinition());
			command.setCommandExecutionType(commandDTO.getCommandExecutionType());
			command.setInputParam(parameterDTOToListCommandParam(commandDTO.getParameters(), command, null));
			command.setParameterDTOList(commandDTO.getParameters());
			command.setIsSuccessAlways(commandDTO.getIsSuccessAlways());
		}
		return command;
	}

	/**
	 * This method provides the conversion of Parameter dto to list command
	 * param list.
	 * 
	 * @param parameters
	 *            the parameters
	 * @param command
	 *            the command
	 * @param parent
	 *            the parent
	 * @return the list
	 */
	public static List<CommandParam> parameterDTOToListCommandParam(final List<ParameterDTO> parameters,
			final AbstractCommand command, final CommandParam parent) {
		// Iterate through all the parameterDTO's in the List,
		// Convert each parameterDTO to CommandParam by calling other method,
		// Add the converted CommandParam to the List.
		// parameters with not null defaultSP value or not of primitive type and
		// of input type are added.
		final List<CommandParam> params = new ArrayList<CommandParam>();
		boolean childDef = false;
		for (final ParameterDTO parameterDTO : parameters) {
			final CommandParameterType type = parameterDTO.getXmlType();
			if (parameterDTO.getIsInput()
					&& (CommandParameterType.ARRAY.equals(type) || CommandParameterType.STRUCT.equals(type) || (parameterDTO
							.getDefaultValueSP() != null && !parameterDTO.getDefaultValueSP().equals("")))) {
				final CommandParamInput param = parameterDTOToCommandParamInput(parameterDTO, command, parent);
				if (param != null) {
					childDef = true;
					params.add(param);
				}
			}
		}
		if (childDef) {
			return params;
		} else {
			return null;
		}
	}

	/**
	 * This method provides the conversion of Parameter dto set to command param
	 * list.
	 * 
	 * @param parameters
	 *            the parameters
	 * @param command
	 *            the command
	 * @param parent
	 *            the parent
	 * @return the list
	 */
	public static List<CommandParam> parameterDTOSetToCommandParam(final Set<ParameterDTO> parameters,
			final AbstractCommand command, final CommandParam parent) {
		// Iterate through all the parameterDTO's in the Set,
		// Convert each parameterDTO to CommandParam by calling other method,
		// Add the converted CommandParam to the List.
		// parameters with not null defaultSP value or not of primitive type and
		// of input type are added.
		final List<CommandParam> params = new ArrayList<CommandParam>();
		boolean childDef = false;
		for (final ParameterDTO parameterDTO : parameters) {
			final CommandParameterType type = parameterDTO.getXmlType();
			if (parameterDTO.getIsInput()
					&& (CommandParameterType.ARRAY.equals(type) || CommandParameterType.STRUCT.equals(type) || (parameterDTO
							.getDefaultValueSP() != null && !parameterDTO.getDefaultValueSP().equals("")))) {
				final CommandParamInput param = parameterDTOToCommandParamInput(parameterDTO, command, parent);
				if (param != null) {
					childDef = true;
					params.add(param);
				}
			}
		}
		if (childDef) {
			return params;
		} else {
			return null;
		}
	}

	/**
	 * This method provides the conversion of Parameter dto to command param
	 * Input.
	 * 
	 * @param parameterDTO
	 *            the parameter dto
	 * @param command
	 *            the command
	 * @param parent
	 *            the parent
	 * @return the command param input
	 */
	public static CommandParamInput parameterDTOToCommandParamInput(final ParameterDTO parameterDTO,
			final AbstractCommand command, final CommandParam parent) {
		ParameterFeedType feedType = parameterDTO.getFeedType();
		final CommandParameterDataType GUIType = parameterDTO.getType();
		final Object defValue = getDefValue(feedType, parameterDTO.getDefaultValueSP(), GUIType);

		CommandParamInput param = null;
		final CommandParameterType type = parameterDTO.getXmlType();
		
		param = new CommandParamInput(feedType, defValue);
		if (defValue != null || !CommandParameterType.PRIMITIVE.equals(type)) { 
			final Primitives xmlType = parameterDTO.getXmlPrimitiveType();
			if (CommandParameterType.ARRAY.equals(type)) {
				// call the method to convert all the children of the
				// parameterDTO
				// to CommandParam,
				// then add all to current CommandParam as children.
				final List<CommandParam> childerns = parameterDTOToListCommandParam(parameterDTO.getArray(), command,
						param);
				if (childerns != null && !childerns.isEmpty()) {
					param.setChilderen(childerns);
				} else {
					param = null;
				}
			} else if (CommandParameterType.STRUCT.equals(type)) {
				// call the method to convert all the children of the
				// parameterDTO
				// to CommandParam,
				// then add all to current CommandParam as children.
				final List<CommandParam> childerns = parameterDTOSetToCommandParam(parameterDTO.getStruct(), command,
						param);
				if (childerns != null && !childerns.isEmpty()) {
					param.setChilderen(childerns);
				} else {
					param = null;
				}
			} else if (CommandParameterType.COMMAND_IDENTIFIER.equals(type)
					|| CommandParameterType.PARAM_IDENTIFIER.equals(type)) {
				Primitives.STRING.equals(xmlType);
			}
			// Added check to allow only visible parameters to be saved in cache
			if (param != null && parameterDTO.getIsVisible()==true) {
				param.setType(type);
				param.setPrimitiveValue(xmlType);
				param.setCommand(command);
				param.setName(parameterDTO.getParameterName());
				param.setParent(parent);
			}else{
				param = null;
			}
		}
		LOGGER.debug("param : {} \nparameterDTO = {}", param, parameterDTO);
		return param;
	}

	public static Object getDefValue(ParameterFeedType feedType, final String defaultValueSP,
			final CommandParameterDataType gUIType) {
		Object defValue = null;
		// calculate the defaultValue depending on the conditions.
		if (ParameterFeedType.COMMAND_OUTPUT.equals(feedType)) {
			// if the parameter feedtype is Command_Output
			// then DefaultValueSP contains
			// COMMAND_DISPLAY_NAME{SEPARATOR}PARAMETER_FULL_QUALIFIED_NAME
			final String value = defaultValueSP;
			final Integer sepIndex = value.indexOf(CommandCircleConstant.FULL_QUALIFIED_NAME_SEPARATOR);
			final String commandDispName = value.substring(0, sepIndex);
			final String fullQualifiedName = value.substring(sepIndex + 1);
			defValue = new CommandParamOutput(commandDispName, fullQualifiedName);
		} else if (feedType.equals(ParameterFeedType.AUX_REQUEST_PARAM)) {
			defValue = AuxRequestParam.getAuxRequestParam(defaultValueSP);
		} else if (feedType.equals(ParameterFeedType.VALIDITY)) {
			defValue = getValidityValueDTO(defaultValueSP);
		} else if (feedType.equals(ParameterFeedType.PRODUCT)) {
			defValue = getProductValueDTO(defaultValueSP);
		} else if(feedType.equals(ParameterFeedType.MVEL)){
			defValue = defaultValueSP;
		} else if (feedType.equals(ParameterFeedType.FUNCTION)) {
			defValue = Function.getFunction(defaultValueSP);
			if(null==defValue) {
				if (CommandParameterDataType.DATE.equals(gUIType) || CommandParameterDataType.DATETIME.equals(gUIType)) {
					defValue = getDateTimeObject(defaultValueSP, gUIType);
				}
			}
		}
		else {
			// parameterDTO contains date/datetime in the gui format so need to
			// convert to fdp format
			if (CommandParameterDataType.DATE.equals(gUIType) || CommandParameterDataType.DATETIME.equals(gUIType)) 
				defValue = getDateTimeObject(defaultValueSP, gUIType);
			else {
				defValue = defaultValueSP;
			}
		}
		return defValue;
	}
	
	private static Object getDateTimeObject(final String defaultValueSP,
			final CommandParameterDataType gUIType) {
		Object defValue = null;
			try {
				if (defaultValueSP != null) {
					defValue = DateUtil.getFdpFormatDateWithHoursMinutesANDSeconds(defaultValueSP);
				}
			} catch (final ParseException e) {
				LOGGER.error("Exception Occured.", e);
			}
		
		return defValue;
	}

	private static Object getProductValueDTO(final String defaultValueSP) {
		Object value = ProductAdditionalInfoEnum.valueOfName(defaultValueSP);
		if (value == null) {
			value = defaultValueSP;
		}
		return value;
	}

	public static ValidityValueDTO getValidityValueDTO(final String defaultValueSP) {
		final ValidityValueDTO validityValueDTO = new ValidityValueDTO();
		final String[] validityValueArray = defaultValueSP.split(FDPConstant.DOT_WITH_ESCAPE_CHAR);
		final ValidityTypeEnum validityTypeEnum = ValidityTypeEnum.valueOf(validityValueArray[0]);
		validityValueDTO.setValidityType(validityTypeEnum);
		switch (validityTypeEnum) {
		case NOW:
		case NOW_MINUS:
		case NEVER_EXPIRE:
		case APPLY_FROM_TODAY:
			break;

		case NOW_PLUS:
		case APPLY_FROM_TODAY_PLUS:
		case DAYS_HRS_MIN_FROM_TODAY:
		case DAYS_HRS_MIN_FROM_TODAY_PLUS:
		case APPLY_FROM_TODAY_MINUS:
		case DAYS_HRS_MIN_FROM_TODAY_MINUS:
		case START_DATE_PCRF:
		case END_DATE_PCRF:
			validityValueDTO.setDays(Integer.valueOf(validityValueArray[1]));
			validityValueDTO.setHours(Integer.valueOf(validityValueArray[2]));
			validityValueDTO.setMinutes(Integer.valueOf(validityValueArray[3]));
			break;
		case APPLY_FROM_TODAY_PLUS_EMA:
			validityValueDTO.setDays(Integer.valueOf(validityValueArray[1]));
			validityValueDTO.setHours(Integer.valueOf(validityValueArray[2]));
			validityValueDTO.setMinutes(Integer.valueOf(validityValueArray[3]));
			validityValueDTO.setSeconds(Integer.valueOf(validityValueArray[4]));
			break;

		case FIXED_DATE:
			try {
				validityValueDTO.setStartDate(DateUtil
						.getFdpFormatDateWithHoursMinutesANDSeconds(validityValueArray[1]));
			} catch (final ParseException e) {
				LOGGER.error("Exception Occured.", e);
			}
			break;

		case FIXED_DATE_PLUS:
		case FIXED_DATE_MINUS:
			try {
				validityValueDTO.setStartDate(DateUtil
						.getFdpFormatDateWithHoursMinutesANDSeconds(validityValueArray[1]));
			} catch (final ParseException e) {
				LOGGER.error("Exception Occured.", e);
			}
			validityValueDTO.setDays(Integer.valueOf(validityValueArray[2]));
			validityValueDTO.setHours(Integer.valueOf(validityValueArray[3]));
			validityValueDTO.setMinutes(Integer.valueOf(validityValueArray[4]));
			break;

		case FIXED_DATE_FOR_NEXT_MONTH:
			validityValueDTO.setStartDayNextMonth(Integer.valueOf(validityValueArray[1]));
			break;

		case FIXED_DATE_FOR_NEXT_MONTH_PLUS:
		case FIXED_DATE_FOR_NEXT_MONTH_MINUS:
			validityValueDTO.setStartDayNextMonth(Integer.valueOf(validityValueArray[1]));
			validityValueDTO.setDays(Integer.valueOf(validityValueArray[2]));
			validityValueDTO.setHours(Integer.valueOf(validityValueArray[3]));
			validityValueDTO.setMinutes(Integer.valueOf(validityValueArray[4]));
			break;
		default:
			break;
		}
		return validityValueDTO;
	}
}
