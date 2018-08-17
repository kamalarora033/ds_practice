package com.ericsson.fdp.business.util;

import java.util.ArrayList;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;

import javax.naming.NamingException;
import javax.xml.bind.JAXBException;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.XML;
import org.slf4j.Logger;

import com.ericsson.fdp.FDPCacheable;
import com.ericsson.fdp.business.cache.datageneration.CommandCacheUtil;
import com.ericsson.fdp.business.command.AbstractCommand;
import com.ericsson.fdp.business.command.impl.NonTransactionCommand;
import com.ericsson.fdp.business.command.impl.TransactionCommand;
import com.ericsson.fdp.business.command.param.impl.CommandParamInput;
import com.ericsson.fdp.business.enums.AuxRequestParam;
import com.ericsson.fdp.business.enums.Command;
import com.ericsson.fdp.business.enums.ConfigurationKey;
import com.ericsson.fdp.business.enums.ErrorTypes;
import com.ericsson.fdp.business.vo.CommandExecutionStatus;
import com.ericsson.fdp.business.vo.FDPAsycCommandVO;
import com.ericsson.fdp.common.constants.FDPConstant;
import com.ericsson.fdp.common.enums.ModuleType;
import com.ericsson.fdp.common.enums.Status;
import com.ericsson.fdp.common.vo.FDPCircle;
import com.ericsson.fdp.core.cache.AppCacheSubStore;
import com.ericsson.fdp.core.command.FDPCommand;
import com.ericsson.fdp.core.command.param.CommandParam;
import com.ericsson.fdp.core.config.utils.PropertyUtils;
import com.ericsson.fdp.core.dsm.framework.service.FDPCache;
import com.ericsson.fdp.core.exception.EvaluationFailedException;
import com.ericsson.fdp.core.exception.ExecutionFailedException;
import com.ericsson.fdp.core.logging.FDPLogger;
import com.ericsson.fdp.core.request.FDPRequest;
import com.ericsson.fdp.core.request.impl.FDPRequestImpl;
import com.ericsson.fdp.core.serviceprovisioning.bean.FDPAppBag;
import com.ericsson.fdp.core.serviceprovisioning.bean.FDPMetaBag;
import com.ericsson.fdp.core.utils.ApplicationConfigUtil;
import com.ericsson.fdp.dao.dto.FDPResultCodesDTO;
import com.ericsson.fdp.dao.dto.ParameterDTO;
import com.ericsson.fdp.dao.enums.CommandExecutionType;
import com.ericsson.fdp.dao.enums.ExternalSystem;
import com.ericsson.fdp.dao.enums.LanguageType;

/**
 * This class is a utility class that works on command.
 * 
 * @author Ericsson
 * 
 */
public class CommandUtil {
	
	static Logger circleLogger=null;
		
	private static final String evdsType = PropertyUtils
			.getProperty("evds.protocol.type");

	/**
	 * Instantiates a new command util.
	 */
	private CommandUtil() {

	}

	/**
	 * public static void main(String[] args) throws Exception { BufferedReader
	 * reader = new BufferedReader(new
	 * FileReader("C://Users//jaiprakash1354//Desktop/1029.xml")); String line =
	 * null; StringBuilder stringBuilder = new StringBuilder(); String ls =
	 * System.getProperty("line.separator");
	 * 
	 * while ((line = reader.readLine()) != null) { stringBuilder.append(line);
	 * stringBuilder.append(ls); }
	 * 
	 * Map<String, CommandParam> string123 =
	 * fromXmlToParameters(stringBuilder.toString(), CommandExecutionType.RS);
	 * System.out.println(getRSServiceId(string123, null)); }
	 * 
	 * private static List<String> getRSServiceId(Map<String, CommandParam>
	 * fdpCommand, Logger circleLogger) { int i = 0; boolean valueFound = false;
	 * final List<String> serviceId = new ArrayList<String>(); while
	 * (!valueFound) { final String usageValuePath = "servicesDtls.service." + i
	 * + ".serviceId"; Object value =
	 * fdpCommand.get(usageValuePath.toLowerCase()); if (value == null) {
	 * valueFound = true; } else { serviceId.add(value.toString()); } i++; }
	 * return serviceId; }
	 */

	/**
	 * This method is used to create a map of fully qualified path of the
	 * parameter and its value from the string representation of a xml.
	 * 
	 * @param outputXmlAsString
	 *            The string representation which is to be converted to the map.
	 * @param commandExecutionType
	 *            The execution type for which the output is present.
	 * @return The map containing fully qualified path and the value.
	 * @throws EvaluationFailedException
	 *             Exception, if in creating the map.
	 * @throws JAXBException
	 */
	public static Map<String, CommandParam> fromXmlToParameters(final String outputXmlAsString,
			final CommandExecutionType commandExecutionType) throws EvaluationFailedException {
		Map<String, CommandParam> output = null;
		switch (commandExecutionType) {
		case ACIP:
			// If command is ACIP create ACIP xml.
			output = ACIPCommandUtil.fromACIPXmlToParameters(outputXmlAsString);
			break;
		case UCIP:
			// If command is UCIP create UCIP xml.
			output = UCIPCommandUtil.fromUCIPXmlToParameters(outputXmlAsString);
			break;
		case CGW:
			// If command is CGW create CGW xml.
			output = CGWCommandUtil.fromCGWXmlToParameters(outputXmlAsString);
			break;
		case RS:
			// If command is RS create RS xml.
			output = RSCommandUtil.fromRSXmlToParameters(outputXmlAsString);
			break;
		case AIR:
			output = AIRCommandUtil.fromAirXmlToParameters(outputXmlAsString);
			break;
		case CAI:
			output = CAICommandUtil.fromCAIToParameters(outputXmlAsString);
			break;
		case MML:
			output = MMLCommandUtil.fromMMLToParameters(outputXmlAsString);
			break;
		case MCARBON:
			output = MCarbonCommandUtil.fromMCarbonToParameters(outputXmlAsString);
			break;
		case MANHATTAN:
			output = ManhattanCommandUtil.fromManhattanToParameters(outputXmlAsString);
			break;
		case CMS:
			output = CMSCommandUtil.fromCMSXmlToParameters(outputXmlAsString);
			break;
		case MCLOAN:
			output = MCLoanCommandUtil.fromMCarbonToParameters(outputXmlAsString);
			break;
		case FDPOFFLINE:
			output = FDPOffLineCommandUtil.fromFDPOffLineToParameters(outputXmlAsString);
			break;
		case Loyalty:
			output = LoyaltyCommandUtil.fromLoyaltyXmlToParameters(outputXmlAsString);
			break;
		case MM:
			output = MobilemoneyCommandUtil.fromMobileMoneyXmlToParameters(outputXmlAsString);
			break;
		case EVDS:
			if(evdsType.contains((String) FDPConstant.EVDS_TYPE_HTTP) ||
					evdsType.contains((String) FDPConstant.EVDS_HTTP_TYPE)){
			output = EVDSHttpCommandUtil.fromEVDSXmlToParameters(outputXmlAsString);
			}else{
			output = EVDSCommandUtil.fromEvdsXmltoParameter(outputXmlAsString);
			}
			break;
		case Ability:
			output=AbilityCommandUtil.fromAbilityXmlToParameters(outputXmlAsString);
			break;			
		case CIS:
			output = CISCommandUtil.fromCisXmlToParameters(outputXmlAsString);
			break;
		case DMC:
			output = DMCCommandUtil.fromDMCXmlToParameters(outputXmlAsString);
			break;
		case SBBB:
			output = SBBCommandUtil.fromSBBXmlToParameters(outputXmlAsString);
			break;	
		case ADC:
			// If command is to GET Handset details create xml for ADC interface.
			output = ADCCommandUtil.fromADCXmlToParameters(outputXmlAsString);
			break;
		/*case ESF:
			output = ESFCommandUtil.fromESFXmlToParameters(outputXmlAsString);*/
		default:
			throw new EvaluationFailedException(
					"The parameters could not be found as command type is not UCIP or ACIP. The command type is "
							+ commandExecutionType);
		}
		return output;
	}

	/**
	 * This method is used to check if the command executed successfully or
	 * failed.
	 * 
	 * @param fdpCommand
	 *            The command to be checked.
	 * @return True, if command executed successfully, false otherwise.
	 * @throws ExecutionFailedException
	 *             Exception, if command could not be checked.
	 * @throws NamingException
	 */
	public static CommandExecutionStatus checkForCommandStatus(final FDPCommand fdpCommand)
			throws ExecutionFailedException {
		CommandExecutionStatus commandExecutionStatus=null;
		switch (fdpCommand.getCommandExecutionType()) {
		// TODO: check if error code is returned.
		// TODO: check if result code is for rollback return failure
		// if (getOutputParam(parameterPath) == )
		case ACIP:
			// If command is ACIP create ACIP xml.
			commandExecutionStatus = ACIPCommandUtil.checkForACIPCommandStatus(fdpCommand);
			break;
		case UCIP:
			// If command is UCIP create UCIP xml.
			commandExecutionStatus = UCIPCommandUtil.checkForUCIPCommandStatus(fdpCommand);
			break;
		case CGW:
			// If command is CGW create CGW xml.
			commandExecutionStatus = CGWCommandUtil.checkForCGWCommandStatus(fdpCommand);
			break;
		case RS:
			// If command is RS create RS xml.
			commandExecutionStatus = RSCommandUtil.checkForRSCommandStatus(fdpCommand);
			break;
		case MML:
			// If command is RS create RS xml.
			commandExecutionStatus = MMLCommandUtil.checkForMMLCommandStatus(fdpCommand);
			break;
		case CAI:
			// If command is RS create RS xml.
			commandExecutionStatus = CAICommandUtil.checkForCIACommandStatus(fdpCommand);
			break;
		case MCARBON:
			// If command is RS create RS xml.
			commandExecutionStatus = MCarbonCommandUtil.checkForMCarbonCommandStatus(fdpCommand);
			break;
		case MANHATTAN:
			commandExecutionStatus = ManhattanCommandUtil.checkForManhattanCommandStatus(fdpCommand);
			break;
		case CMS:
			// If command is CMS create CMS xml.
			commandExecutionStatus = CMSCommandUtil.checkForCMSCommandStatus(fdpCommand);
			break;
		case MCLOAN:
			commandExecutionStatus = MCLoanCommandUtil.checkForMCarbonCommandStatus(fdpCommand);
			break;
		case Loyalty:
			commandExecutionStatus = LoyaltyCommandUtil.checkForLoyaltyCommandStatus(fdpCommand);
			break;
		case FDPOFFLINE:
			commandExecutionStatus = FDPOffLineCommandUtil.checkForFDPOffLineCommandStatus(fdpCommand);
			break;
		case MM:
			commandExecutionStatus = MobilemoneyCommandUtil.checkForMobileMoneyCommandStatus(fdpCommand);
			break;
		case EVDS:
			if(evdsType.contains((String) FDPConstant.EVDS_TYPE_HTTP) ||
					evdsType.contains((String) FDPConstant.EVDS_HTTP_TYPE)){
				commandExecutionStatus = EVDSHttpCommandUtil.checkForEVDSCommandStatus(fdpCommand);
			}else{
			commandExecutionStatus = EVDSCommandUtil.checkForEVDSCommandStatus(fdpCommand);
			}
			break;
		case Ability:
			commandExecutionStatus = AbilityCommandUtil.checkForAbilityCommandStatus(fdpCommand);
			break;	
		case CIS:
			commandExecutionStatus = CISCommandUtil.checkForCisCommandStatus(fdpCommand);
			break;
		case DMC:
			commandExecutionStatus = DMCCommandUtil.checkForDMCCommandStatus(fdpCommand);
		break;
		case SBBB:
			commandExecutionStatus = SBBCommandUtil.checkForSBBCommandStatus(fdpCommand);
			break;
		case ADC:
			commandExecutionStatus = ADCCommandUtil.checkForADCCommandStatus(fdpCommand);
			break;
		/*case ESF:
			commandExecutionStatus = ESFCommandUtil.checkForESFCommandStatus(fdpCommand);*/
		default:
			throw new ExecutionFailedException("The command type is not UCIP or ACIP. The command type is "
					+ fdpCommand.getCommandExecutionType());
		}
		return commandExecutionStatus == null ? new CommandExecutionStatus(Status.FAILURE, 0,
				FDPConstant.ERROR_CODE_MAPPING_MISSING, ErrorTypes.FAULT_CODE.name(),
				com.ericsson.fdp.common.enums.ExternalSystem.valueOf(fdpCommand.getSystem().name()))
				: commandExecutionStatus;
	}

	/**
	 * This method is used to check the cache for the provided fault code
	 * mapping.
	 * 
	 * @param configCache
	 *            The cache containing the mapping.
	 * @param faultCodeValue
	 *            The fault code value.
	 * @param externalSystem
	 *            The external system to which this fault code corresponds.
	 * @return True if the command failed, else false.
	 * @throws ExecutionFailedException
	 *             Exception, if the fault code is not valid.
	 */
	public static FDPResultCodesDTO checkFaultCode(final FDPCache<FDPAppBag, Object> configCache,
			final Object faultCodeValue, final ExternalSystem externalSystem) throws ExecutionFailedException {
		FDPResultCodesDTO isFailure = null;
		final Object faultCodeMapping = configCache.getValue(new FDPAppBag(AppCacheSubStore.RESULT_CODE_MAP,
				(externalSystem.name() + FDPConstant.PARAMETER_SEPARATOR + faultCodeValue)));
		if (faultCodeMapping instanceof FDPResultCodesDTO) {
			isFailure = (FDPResultCodesDTO) faultCodeMapping;
		} else {
			throw new ExecutionFailedException("The fault code is not recognized or the mapping is not present."
					+ faultCodeValue);
		}
		return isFailure;
	}

	/**
	 * This method is used to check the cache for the provided response code
	 * mapping.
	 * 
	 * @param configCache
	 *            The cache containing the mapping.
	 * @param responseCodeValue
	 *            The response code value.
	 * @param commandDisplayName
	 *            The command for which the response code is to be checked.
	 * @return True if the command failed, else false.
	 * @throws ExecutionFailedException
	 *             Exception, if the fault code is not valid.
	 */
	public static FDPResultCodesDTO checkResponseCode(final FDPCache<FDPAppBag, Object> configCache,
			final Object responseCodeValue, final String commandDisplayName) throws ExecutionFailedException {
		FDPResultCodesDTO isFailure = null;
		final Object responseCodeMapping = configCache.getValue(new FDPAppBag(
				AppCacheSubStore.COMMAND_RESULT_CODES_MAPPING,
				(commandDisplayName + FDPConstant.PARAMETER_SEPARATOR + responseCodeValue)));
		if (responseCodeMapping instanceof FDPResultCodesDTO) {
			isFailure = (FDPResultCodesDTO) responseCodeMapping;
		}
		return isFailure;
	}

	/**
	 * This method is used to check if fault code is returned or not.
	 * 
	 * @param methodResponse
	 *            The method response body.
	 * @return True if fault code is returned, false otherwise.
	 */
	public static boolean checkIfFaultCode(final JSONObject methodResponse) {
		if (methodResponse.isNull("fault")) {
			return false;
		}
		return true;
	}

	/**
	 * This method is used to get the status from the result code.
	 * 
	 * @param isFailure
	 *            the failure case.
	 * @return the status.
	 */
	public static Status getStatus(final FDPResultCodesDTO isFailure) {
		Status status = Status.FAILURE;
		if (isFailure != null) {
			status = isFailure.getRollbackStatus();
		}
		return status;
	}

	/**
	 * The commands from display name.
	 * 
	 * @param fdpRequest
	 *            the request.
	 * @param commands
	 *            the commands.
	 * @return the list of commands.
	 * @throws ExecutionFailedException
	 *             Exception in execution.
	 */
	public static List<FDPCommand> getCommandsFromDisplayName(final FDPCircle fdpCircle, final List<String> commands)
			throws ExecutionFailedException {
		final List<FDPCommand> fdpCommands = new ArrayList<FDPCommand>();
		if (commands != null) {
			for (final String commandName : commands) {
				final FDPCacheable cachedCommand = ApplicationConfigUtil.getMetaDataCache().getValue(
						new FDPMetaBag(fdpCircle, ModuleType.COMMAND, commandName));
				if (cachedCommand instanceof FDPCommand) {
					fdpCommands.add((FDPCommand) cachedCommand);
				} else {
					throw new ExecutionFailedException("Command " + commandName
							+ " is not configured. Hence activation cannot be done.");
				}
			}
		}
		return fdpCommands;
	}

	public static void postProcessForLogs(final CommandExecutionStatus commandExecutionStatus,
			final FDPRequest fdpRequest, final FDPCommand command) {
		switch (command.getCommandExecutionType()) {
		case CAI:
		case MML:
			final StringBuffer stringBuffer = new StringBuffer();
			stringBuffer.append(LoggerUtil.getRequestAppender(fdpRequest)).append(FDPConstant.EMARSLT)
					.append(FDPConstant.LOGGER_KEY_VALUE_DELIMITER).append(commandExecutionStatus.getStatus().name());
			if (commandExecutionStatus.getStatus().equals(Status.FAILURE)
					|| commandExecutionStatus.getStatus().equals(Status.LOG_ON_FAIL)) {
				stringBuffer.append(FDPConstant.LOGGER_DELIMITER).append(FDPConstant.EMARSN)
						.append(FDPConstant.LOGGER_KEY_VALUE_DELIMITER).append(commandExecutionStatus.getCode())
						.append("-").append(commandExecutionStatus.getDescription());
			}
			FDPLogger.info(LoggerUtil.getSummaryLoggerFromRequest(fdpRequest), CommandUtil.class,
					"postProcessForLogs()", stringBuffer.toString());
			break;

		default:
			break;
		}
	}

	/**
	 * This method creates the command copy to execute.
	 * 
	 * @param fdpCommand
	 * @return
	 */
	public static FDPCommand getExectuableFDPCommand(final FDPCommand fdpCommand) {
		AbstractCommand abstractCommand = null;
		if (null != fdpCommand) {
			final AbstractCommand command = (AbstractCommand) fdpCommand;
			if (fdpCommand instanceof NonTransactionCommand) {
				// System.out.println("New Command Object Created..... for NonTransactionCommand "+command.getCommandDisplayName());
				abstractCommand = new NonTransactionCommand(command.getCommandName());
				abstractCommand.setCommandName(command.getCommandName());
				abstractCommand.setCommandDisplayName(command.getCommandDisplayName());
				abstractCommand.setCommandExecutionType(command.getCommandExecutionType());
				abstractCommand.setCommandType(command.getCommandType());
				abstractCommand.setInputParam(getParamsToExecute(command.getParameterDTOList(), abstractCommand));
				abstractCommand.setSystem(command.getSystem());
			} else if (fdpCommand instanceof TransactionCommand) {
				// System.out.println("New Command Object Created..... for TransactionCommand "+command.getCommandDisplayName());
				abstractCommand = new TransactionCommand(command.getCommandName());
				abstractCommand.setCommandName(command.getCommandName());
				abstractCommand.setCommandDisplayName(command.getCommandDisplayName());
				abstractCommand.setCommandExecutionType(command.getCommandExecutionType());
				abstractCommand.setCommandType(command.getCommandType());
				abstractCommand.setInputParam(getParamsToExecute(command.getParameterDTOList(), abstractCommand));
				abstractCommand.setSystem(command.getSystem());
			}
		}
		return abstractCommand;
	}

	/**
	 * This method executes the specified input command
	 * 
	 * @param fdpRequest
	 * @param cmdToExecute
	 * @param forceExecution
	 * @throws ExecutionFailedException
	 */
	public static Status executeCommand(FDPRequest fdpRequest, Command cmdToExecute, boolean forceExecution) throws ExecutionFailedException{
		FDPCommand fdpCmdToExecute = (forceExecution) ? null :  fdpRequest.getExecutedCommand(cmdToExecute.getCommandDisplayName());
		Status status = Status.FAILURE;
		if(fdpCmdToExecute == null){
			final FDPCacheable cachedCommand = ApplicationConfigUtil.getMetaDataCache().getValue(new FDPMetaBag(fdpRequest.getCircle(), ModuleType.COMMAND, cmdToExecute.getCommandDisplayName()));
			if (cachedCommand instanceof FDPCommand) {
				fdpCmdToExecute = (FDPCommand) cachedCommand;
				status = fdpCmdToExecute.execute(fdpRequest);
				if (!status.equals(Status.SUCCESS)) {
					throw new ExecutionFailedException(cmdToExecute.getCommandDisplayName() + " could not be executed");
				}else{
					fdpRequest.addExecutedCommand(fdpCmdToExecute);
					//status = Status.SUCCESS;
				}
			} else {
				throw new ExecutionFailedException(cmdToExecute.getCommandDisplayName() + " not configured in cache");
			}			
		}
		return status;
	}
	
	/**
	 * This method executes the specified input FDPcommand
	 * @param fdpRequest
	 * @param cmdToExecute
	 * @param forceExecution
	 * @throws ExecutionFailedException
	 */
	
    public static Status executeCommand(FDPRequest fdpRequest, FDPCommand cmdToExecute, boolean forceExecution)
            throws ExecutionFailedException {
        Status status = Status.FAILURE;
        if (cmdToExecute != null && forceExecution) {
            status = cmdToExecute.execute(fdpRequest);
            if (!status.equals(Status.SUCCESS)) {
                throw new ExecutionFailedException(cmdToExecute.getCommandDisplayName() + " could not be executed");
            } else {
                fdpRequest.addExecutedCommand(cmdToExecute);
                status = Status.SUCCESS;
            }
        }
        return status;
    }

	private static List<CommandParam> getParamsToExecute(final List<ParameterDTO> parameterDTOs,
			final AbstractCommand command) {
		return CommandCacheUtil.parameterDTOToListCommandParam(parameterDTOs, command, null);
	}
	
		/**
	 * This method will get the language Id from GAD response. 
	 * Updated for Airtel DRC FDP Project
	 * @param fdpCommand
	 * @return
	 */
	public static Integer getLanguageFromGAD(final FDPRequest fdpRequest) {
		Integer languaId = null;
		try {
			FDPCommand fdpCommand = (FDPCommand) fdpRequest.getExecutedCommand(Command.GETACCOUNTDETAILS.getCommandDisplayName());
			if(null == fdpCommand) {
				final FDPCacheable fdpCommandCached = ApplicationConfigUtil.getMetaDataCache().getValue(
						new FDPMetaBag(fdpRequest.getCircle(), ModuleType.COMMAND, Command.GETACCOUNTDETAILS.getCommandDisplayName()));
				if(null != fdpCommandCached && fdpCommandCached instanceof FDPCommand) {
					fdpCommand = (FDPCommand) fdpCommandCached;					
					if(Status.SUCCESS.equals(fdpCommand.execute(fdpRequest))) {
						fdpRequest.addExecutedCommand(fdpCommand);
					}
				}
			}
			String  langIdStr= (null != fdpCommand)? fdpCommand.getOutputParam(FDPConstant.GAD_LANGAUAGE_ID_CURRENT_KEY).getValue().toString() : null;
			languaId =  null != langIdStr ? Integer.parseInt(langIdStr) : LanguageType.ENGLISH.getValue();
		} catch (final Exception e) {
			languaId = 1;
			e.printStackTrace();
		}
		return languaId;
	}
	
	/**
	 * This method executes the specified input FDPcommand
	 * @param fdpRequest
	 * @param cmdToExecute
	 * @param forceExecution
	 * @throws ExecutionFailedException
	 */
	
	public static Status executeCommandAbility(FDPRequest fdpRequest, FDPCommand cmdToExecute, boolean forceExecution) throws ExecutionFailedException{
		Status status = Status.FAILURE;

		status = cmdToExecute.execute(fdpRequest);
		
		if (!status.equals(Status.SUCCESS)) {
			throw new ExecutionFailedException(cmdToExecute.getCommandDisplayName() + " could not be executed");
		}else{
			fdpRequest.addExecutedCommand(cmdToExecute);
			status = Status.SUCCESS;
		}
		return status;
	}
	
	/**
	 * THis method check if command is of Async type or not.
	 * @param fdpRequest
	 * @param commandDisplayName
	 * @return
	 * @throws ExecutionFailedException
	 */
	public static boolean isAyncCommand(final FDPRequest fdpRequest, final String commandDisplayName) {
		boolean isAync = false;
		FDPMetaBag metaBag = new FDPMetaBag(fdpRequest.getCircle(),
				ModuleType.ASYNC_COMMANDS, commandDisplayName);
		 try {
			if(null != ApplicationConfigUtil.getMetaDataCache().getValue(metaBag)) {
				 isAync = true;
			 }
		} catch (ExecutionFailedException e) {
			e.printStackTrace();
		}
		 return isAync;
	}
	
	
	/**
	 * THis method get Notification id command is of Async type or not.
	 * @param fdpRequest
	 * @param commandDisplayName
	 * @return
	 * @throws ExecutionFailedException
	 */
	public static Long getNotificationIdAsycCommand(final FDPRequest fdpRequest, final String commandDisplayName) throws ExecutionFailedException {
		Long notiID = 0L;
		FDPMetaBag metaBag = new FDPMetaBag(fdpRequest.getCircle(),
				ModuleType.ASYNC_COMMANDS, commandDisplayName);
		final FDPCacheable fdpCacheable =ApplicationConfigUtil.getMetaDataCache().getValue(metaBag);
		if(fdpCacheable instanceof FDPAsycCommandVO) {
			notiID = ((FDPAsycCommandVO)fdpCacheable).getNotificationid();
		}
		 return notiID;
	}
	
	/**
	 * This function removes the specified command param from the input command
	 * @param fdpCommand
	 * @param commandParam
	 * @return
	 */
	public static boolean removeCommandParam(final FDPCommand fdpCommand, final CommandParam commandParam){
		 boolean isRemoved = false;
		 if(null != commandParam){
			 fdpCommand.getInputParam().remove(commandParam);
			 isRemoved = true;
		 }
		 return isRemoved;
	}
	
	public static List<String> parseReponseFromRsSingleProvisioningRequest(final FDPRequest fdpRequest,FDPCommand fdpCommand) {
		List<String> productList= new ArrayList<String>();
		try {
			if(null == fdpCommand) {
				final FDPCacheable fdpCommandCached = ApplicationConfigUtil.getMetaDataCache().getValue(
						new FDPMetaBag(fdpRequest.getCircle(), ModuleType.COMMAND, Command.GET_SERVICES_DETAILS_REQUEST.getCommandDisplayName()));
				if(null != fdpCommandCached && fdpCommandCached instanceof FDPCommand) {
					fdpCommand = (FDPCommand) fdpCommandCached;					
					if(Status.SUCCESS.equals(fdpCommand.execute(fdpRequest))) {
						fdpRequest.addExecutedCommand(fdpCommand);
					}
				}
			}
			final AbstractCommand abstractCommand = (AbstractCommand) fdpCommand;
			final String outputParam = abstractCommand.getCommandResponse();
			final JSONObject xmlJSONObj = XML.toJSONObject(outputParam);
			final JSONObject methodResponse = (JSONObject) xmlJSONObj.get("GetServicesDtlsResponse");
			productList = getProductIdParamsList(methodResponse, "servicesDtls", "service"); 
		} catch (final Exception e) {
				e.printStackTrace();
			}
			return productList;
	}
	
	
	private static List<String> getProductIdParamsList(final JSONObject methodResponse,
			final String rootTag, final String subTag) throws JSONException {
		List<String> productList = new ArrayList<String>();
		final JSONObject objectForMember = (JSONObject) methodResponse.get(rootTag);
		if (objectForMember != null && objectForMember.has(subTag)) {
			final Object serviceObject = objectForMember.get(subTag);
			if (serviceObject instanceof JSONArray) {
				final JSONArray jsonArray = (JSONArray) serviceObject;
				for (int arrayIndex = 0; arrayIndex < jsonArray.length(); arrayIndex++) {
					final JSONObject arrayObject = (JSONObject) jsonArray.get(arrayIndex);
					productList.add(arrayObject.getString(FDPConstant.PRODUCT_ID_IVR));
				}
			} else if (serviceObject instanceof JSONObject) {
				final JSONObject arrayObject = (JSONObject) serviceObject;
				productList.add(arrayObject.getString(FDPConstant.PRODUCT_ID_IVR));
			}
		}
		return productList;
	}

	public static void setHandsetBasedParametersInRequest(FDPRequestImpl fdpRequest)throws ExecutionFailedException {
		try{
			
			FDPCommand fdpCommand = (FDPCommand) fdpRequest.getExecutedCommand(Command.QUERY_SUSBSCRIBER_HANDSET.getCommandDisplayName());
			
			if(null == fdpCommand) {
			
				final FDPCacheable fdpCommandCached = ApplicationConfigUtil.getMetaDataCache().getValue(
						new FDPMetaBag(fdpRequest.getCircle(), ModuleType.COMMAND, Command.QUERY_SUSBSCRIBER_HANDSET.getCommandDisplayName()));
				
				if(null != fdpCommandCached && fdpCommandCached instanceof FDPCommand) {
					fdpCommand = (FDPCommand) fdpCommandCached;					
					if(Status.SUCCESS.equals(fdpCommand.execute(fdpRequest))) {
						fdpRequest.addExecutedCommand(fdpCommand);
						String brand = (null != fdpCommand ? fdpCommand.getOutputParam(FDPConstant.HANDSET_BRAND).getValue().toString() : null);
						String brandModel = (null != fdpCommand ? fdpCommand.getOutputParam(FDPConstant.HANDSET_BRANDMODEL).getValue().toString() : null);	
						String imei = (null != fdpCommand ? fdpCommand.getOutputParam(FDPConstant.IMEI_NUMBER).getValue().toString() : null);
						
						if(brand == null || brandModel == null || imei == null){
							throw new ExecutionFailedException(" Unable to update handset based parameters in the request");
						}
						
						((FDPRequestImpl)fdpRequest).putAuxiliaryRequestParameter(AuxRequestParam.IMEI, imei);
						((FDPRequestImpl)fdpRequest).putAuxiliaryRequestParameter(AuxRequestParam.HANDSET_BRAND, brand);
						((FDPRequestImpl)fdpRequest).putAuxiliaryRequestParameter(AuxRequestParam.HANDSET_BRANDMODEL, brandModel);
					}
					else{
						throw new ExecutionFailedException("Unable to update handset based parameters in the request");
					}
				}
				
			}
		}catch(Exception e){
			FDPLogger.error(getCircleLogger(fdpRequest), CommandUtil.class, "setHandsetBasedParametersInRequest()","Unable to update handset based parameters in the request "+ e.getMessage());
			throw new ExecutionFailedException(e.getMessage());
		}
	}
	
	
	public Map<String,CommandParam> getOutputOfGetOfferExecutedCommand(FDPRequest fdpRequest)throws ExecutionFailedException {
	
		
		Status status= CommandUtil.executeCommand(fdpRequest, Command.GET_OFFERS, true);
    	if (!status.equals(Status.SUCCESS)) {
			throw new ExecutionFailedException(Command.GET_OFFERS.getCommandDisplayName() + " could not be executed");
		}
    	return fdpRequest.getExecutedCommand(Command.GET_OFFERS.getCommandDisplayName()).getOutputParams();
	}

	/**
	 * This method is sued to get the circle logger.
	 * 
	 * @param fdpRequest
	 *            the request to get the circle logger.
	 * @return the circle logger.
	 */
	protected static Logger getCircleLogger(final FDPRequest fdpRequest) {
		if (circleLogger == null) {
			circleLogger = LoggerUtil.getSummaryLoggerFromRequest(fdpRequest);
		}
		return circleLogger;
	} 
	

	/**
	 * The method is use to execute MPR command for AutoRenewal If subscriber confirm by Press 1.
	 * @param fdpRequest
	 * @return
	 */
    public static Status executeCommandForAutoRenewal(FDPRequest fdpRequest) {
        Status tempS = Status.SUCCESS;
        try {
            tempS = CommandUtil.executeCommand(fdpRequest, fdpRequest.getExecutedCommand(Command.MPR.getCommandDisplayName()), true);
            if (Status.SUCCESS.equals(tempS)) {
                CommandParam offerIdParam = fdpRequest.getExecutedCommand(Command.MPR.getCommandDisplayName()).getInputParam(
                        FDPConstant.OFFER_ID);
                final FDPCacheable cachedCommand = ApplicationConfigUtil.getMetaDataCache().getValue(
                        new FDPMetaBag(fdpRequest.getCircle(), ModuleType.COMMAND, Command.UPDATE_ACCUMULATORS.getCommandDisplayName()));
                AbstractCommand updateAcc = (AbstractCommand) cachedCommand;
                CommandParamInput iParam = (CommandParamInput) updateAcc.getInputParam(FDPConstant.ACC_UPDATE_INFO);

                if (iParam.getChilderen() != null) {
                    ListIterator<CommandParam> childIterator = iParam.getChilderen().listIterator();
                    while (childIterator.hasNext()) {
                        CommandParam child = childIterator.next();
                        if (FDPConstant.MSISDN_WITH_PREFIX_ZERO.equals(child.getName())) {
                        	 ListIterator<CommandParam> childIterator1=child.getChilderen().listIterator();
                            while (childIterator1.hasNext()) {
                            	CommandParam accInfoChild = childIterator1.next();
                                if (FDPConstant.ACCUMULATOR_ID.equals(accInfoChild.getName())) {
                                    FulfillmentUtil.updateValueInInputCmdObject(CommandParamInput.class, FDPConstant.DEFINED_VALUE_TEXT,
                                            accInfoChild,
                                            RequestUtil.getConfigurationKeyValue(fdpRequest, ConfigurationKey.AUTO_RENEWAL_UA_ID));
                                } else if (FDPConstant.ACCUMULATOR_VALUE_ABSOLUTE.equals(accInfoChild.getName())) {
                                    FulfillmentUtil.updateValueInInputCmdObject(CommandParamInput.class, FDPConstant.DEFINED_VALUE_TEXT,
                                            accInfoChild, offerIdParam.getValue());
                                } else if (FDPConstant.ACCUMULATOR_VALUE_RELATIVE.equals(accInfoChild.getName()) || 
                                		FDPConstant.ACCUMULATOR_START_DATE.equals(accInfoChild.getName())) {
                                	childIterator1.remove();
                                }
                            }
                        } else {
                            childIterator.remove();
                        }
                    }
                    ListIterator<CommandParam> itParams = updateAcc.getInputParam().listIterator();
                    while (itParams.hasNext()) {
                        String paramName = itParams.next().getName();
                        if (FDPConstant.SERVICE_CLASS_CURRENT.equals(paramName) || FDPConstant.NEGOTIATED_CAPABILITIES.equals(paramName)) {
                            itParams.remove();
                        }
                    }
                }
                tempS = CommandUtil.executeCommand(fdpRequest, updateAcc, true);
            }
        } catch (ExecutionFailedException e) {
            tempS = Status.FAILURE;
            FDPLogger.error(getCircleLogger(fdpRequest), CommandUtil.class, "execute()", LoggerUtil.getRequestAppender(fdpRequest)
                    + "Auto Renewal Product Execute MPR command :- " + e.getMessage(), e);
        }
        return tempS;

    }
}
