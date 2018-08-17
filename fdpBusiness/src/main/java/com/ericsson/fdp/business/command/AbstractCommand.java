package com.ericsson.fdp.business.command;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import com.ericsson.fdp.business.adapter.Adapter;
import com.ericsson.fdp.business.builder.ExternalSystemThreadBuilder;
import com.ericsson.fdp.business.builder.FDPThreadUtil;
import com.ericsson.fdp.business.builder.IThreadBuilder;
import com.ericsson.fdp.business.command.param.AbstractCommandParam;
import com.ericsson.fdp.business.command.param.impl.CommandParamInput;
import com.ericsson.fdp.business.constants.BusinessConstants;
import com.ericsson.fdp.business.dto.Me2uProductDTO;
import com.ericsson.fdp.business.enums.AuxRequestParam;
import com.ericsson.fdp.business.enums.ConfigurationKey;
import com.ericsson.fdp.business.enums.ErrorTypes;
import com.ericsson.fdp.business.enums.ExecutionStatus;
import com.ericsson.fdp.business.enums.RequestMetaValuesKey;
import com.ericsson.fdp.business.enums.ivr.FulfillmentParameters;
import com.ericsson.fdp.business.product.Product;
import com.ericsson.fdp.business.product.impl.BaseProduct;
import com.ericsson.fdp.business.util.ChargingUtil;
import com.ericsson.fdp.business.util.CommandExecutionBrokerUtil;
import com.ericsson.fdp.business.util.CommandUtil;
import com.ericsson.fdp.business.util.FailureCommandReportGenerationUtil;
import com.ericsson.fdp.business.util.LoggerUtil;
import com.ericsson.fdp.business.vo.CommandExecutionStatus;
import com.ericsson.fdp.common.constants.FDPConstant;
import com.ericsson.fdp.common.constants.FDPLoggerConstants;
import com.ericsson.fdp.common.enums.ChannelType;
import com.ericsson.fdp.common.enums.SPCommandNameToDisplayEnum;
import com.ericsson.fdp.common.enums.Status;
import com.ericsson.fdp.common.util.StringUtil;
import com.ericsson.fdp.core.command.FDPCommand;
import com.ericsson.fdp.core.command.param.CommandParam;
import com.ericsson.fdp.core.exception.EvaluationFailedException;
import com.ericsson.fdp.core.exception.ExecutionFailedException;
import com.ericsson.fdp.core.logging.FDPLogger;
import com.ericsson.fdp.core.request.FDPRequest;
import com.ericsson.fdp.core.request.ResponseError;
import com.ericsson.fdp.core.request.impl.FDPRequestImpl;
import com.ericsson.fdp.core.request.impl.FDPSMPPRequestImpl;
import com.ericsson.fdp.dao.dto.ParameterDTO;
import com.ericsson.fdp.dao.enums.CommandDefinition;
import com.ericsson.fdp.dao.enums.CommandExecutionType;
import com.ericsson.fdp.dao.enums.CommandParameterType;
import com.ericsson.fdp.dao.enums.ExternalSystem;

import ch.qos.logback.classic.Logger;

/**
 * This class defines the commands that will be used to interact with the
 * external systems.
 *
 * @author Ericsson
 *
 */
public abstract class AbstractCommand implements FDPCommand {

	/**
	 *
	 */
	private static final long serialVersionUID = -1892370757321452382L;
	/** The command name that will be executed. */
	private String commandName;
	/** The display name of the command. */
	private String commandDisplayName;
	/** The input parameters for the command. **/
	private List<CommandParam> inputParam;
	/**
	 * The output parameter for the command. The map contains the fully
	 * qualified path of the parameter with the output parameter
	 */
	private Map<String, CommandParam> outputParam;
	/** The external system with which this command interacts. */
	private ExternalSystem system;
	/** The command type for the command. */
	private CommandDefinition commandType;
	/** The command execution type UCIP or ACIP. */
	private CommandExecutionType commandExecutionType;
	/**
	 * The response for the command.
	 */
	private String commandResponse;

	/**
	 * The response error.
	 */
	private ResponseError responseError;

	/**
	 * The status
	 */
	private Status status = Status.FAILURE;

	/**
	 * The timeout for the command in millisecond. If the timeout has been
	 * configured then the command will wait till this time else it will wait
	 * for termination.
	 */
	private Long timeout = 1 * 60 * 1000L;

	private Logger logger;

	
	private List<ParameterDTO> parameterDTOList;

	private static IThreadBuilder threadbuilder = new ExternalSystemThreadBuilder();	
	
	/** The is Rollback Command param */
	protected boolean isRollbackCommand = false;
	
	/** The is Success Always parameter */
	protected Boolean isSuccessAlways = false;
	
	/** The Write To Failure File */
	private boolean writeToFailureFile = false;

	@Override
	public CommandParam getOutputParam(final String parameterPath) {
		return outputParam.get(parameterPath.toLowerCase());
	}

	@Override
	public Map<String, CommandParam> getOutputParams() {
		return outputParam;
	}

	@Override
	public Status execute(final FDPRequest fdpRequest, final Object... otherParams) throws ExecutionFailedException {
		logger = LoggerUtil.getSummaryLoggerFromRequest(fdpRequest);
		final String requestID = fdpRequest.getRequestId();
		String infoDescription=null; 
		infoDescription=new StringBuilder(FDPConstant.REQUEST_ID)
		.append(FDPConstant.LOGGER_KEY_VALUE_DELIMITER)
		.append(requestID)
		.append(FDPConstant.LOGGER_DELIMITER).toString();
		final StringBuilder commandInfo = new StringBuilder();
		commandInfo.append(LoggerUtil.getRequestAppender(fdpRequest)).append("PROVCMD")
				.append(FDPConstant.LOGGER_KEY_VALUE_DELIMITER).append(commandDisplayName)
				.append(FDPConstant.LOGGER_DELIMITER).append("PARMMOD").append(FDPConstant.LOGGER_KEY_VALUE_DELIMITER);
		FDPLogger.debug(logger, getClass(), "execute()",
				LoggerUtil.getRequestAppender(fdpRequest) + "Executing command :- " + commandDisplayName);
		FDPLogger.info(logger, getClass(), "execute()", infoDescription+"PPFLOW:"+this.getSystem().name() + FDPConstant.LOGGER_KEY_VALUE_VALUE_DELIMITER + commandDisplayName
				+ FDPConstant.LOGGER_DELIMITER + FDPLoggerConstants.CORRELATION_ID + FDPConstant.LOGGER_KEY_VALUE_DELIMITER  + fdpRequest.getOriginTransactionID().toString());
	
		boolean toExecuteData2Share = false;
		String expiryParamtoEvaluate = null;
		try {
			expiryParamtoEvaluate = getExpiryParamToEvaluate(fdpRequest);			
			for (CommandParam commandParam : inputParam) {
				if (commandParam instanceof CommandParamInput) {
					if(commandParam.getName() != null){
						final CommandParamInput input = (CommandParamInput) commandParam;
						if(null!=expiryParamtoEvaluate && input.getName().equalsIgnoreCase(expiryParamtoEvaluate))
							input.evaluateExpiryOffer(fdpRequest);
						else
							input.evaluateValue(fdpRequest);						
						Object idData2Share = fdpRequest.getAuxiliaryRequestParameter(AuxRequestParam.isData2Share);
						if(null != idData2Share && idData2Share.equals("true") && commandDisplayName.equals("Balance/Date Update(Main)")) {

							if(commandParam.getName().equals("subscriberNumber")) {
								if(commandParam.getValue().equals(fdpRequest.getIncomingSubscriberNumber()))
									toExecuteData2Share = true;
							}
							if(toExecuteData2Share) {
								Product product = (Product) fdpRequest.getValueFromRequest(RequestMetaValuesKey.PRODUCT);
								BaseProduct baseProduct = (BaseProduct) product;
								if(baseProduct.getCharges() != null)
									if(commandParam.getName().equals("adjustmentAmountRelative")) {
										final Map<String, String> configurationMap = fdpRequest.getCircle().getConfigurationKeyValueMap();
										String negweeCSFactor = configurationMap.get(ConfigurationKey.NEGWEE_CS_FACTOR.getAttributeName());
										if(null == negweeCSFactor)
											negweeCSFactor = "1";
										if(baseProduct.getCharges() != null) {
											((CommandParamInput) commandParam).setValue(Long.parseLong(baseProduct.getCharges()) * (-1) * Long.parseLong(negweeCSFactor));
											((FDPSMPPRequestImpl)fdpRequest).putAuxiliaryRequestParameter(AuxRequestParam.TRANSACTIONAL_CHARGES, baseProduct.getCharges());
										}
									}

								if(commandParam.getName().equals("dedicatedAccountUpdateInformation")) {
									Me2uProductDTO me2uProductDTO = (Me2uProductDTO) ((FDPSMPPRequestImpl)fdpRequest).getAuxiliaryRequestParameter(AuxRequestParam.SELECTED_Me2UDATA2SHARE_PRODUCT);
									HashMap<String, Long> daMapToExecute = getDAMapToExecute(fdpRequest, me2uProductDTO);
									commandParam = updateDedicatedActParams(input, daMapToExecute, me2uProductDTO, fdpRequest);
								}
							}
						}
                        if (CommandParameterType.STRUCT.equals(input.getType()) || CommandParameterType.ARRAY.equals(input.getType())) {
                        	printCommandParameter(input, commandInfo);
                        } else {
                            commandInfo.append(input.getName()).append("=").append(input.getValue()).append("#");
                        }
					}
				}
			}
			// code start from here
			if(fdpRequest.getChannel().equals(ChannelType.FLYTXT)) {
				SPCommandNameToDisplayEnum displayEnum = SPCommandNameToDisplayEnum.getSPCommandNameToDisplayEnum(commandDisplayName);
				Object offerValidity=fdpRequest.getAuxiliaryRequestParameter(AuxRequestParam.OFFER_VALIDITY);
				if(SPCommandNameToDisplayEnum.UPDATE_OFFER==displayEnum && null!=offerValidity && !"0".equals(offerValidity))
				{
					 CommandParamInput commandParamInput =  (CommandParamInput) getInputParam(FulfillmentParameters.OFFER_TYPE.getValue());
					if(null!=commandParamInput && "0".equals(commandParamInput.getValue()))
					{
						commandParamInput = null;
						
					}
					else
					{
						
					}
				}
				
			}
			if (CommandDefinition.UPDATE.equals(commandType) || CommandDefinition.CHARGING.equals(commandType)
					|| CommandDefinition.REFILL.equals(commandType)||CommandDefinition.PAM.equals(commandType)) {
				FDPLogger.info(logger, getClass(), "execute()", commandInfo.toString());
			}
			// Generate XML file for the parameter and append it to xml.
			//return executeCommand(fdpRequest);
			Status status = executeCommandAndProcessResponse(fdpRequest);
			addCommandToStack(fdpRequest);
			return status;
		} catch (final EvaluationFailedException e) {
			FDPLogger.error(logger, getClass(), "execute()", LoggerUtil.getRequestAppender(fdpRequest)
					+ "Could not execute command", e);
			throw new ExecutionFailedException("Could not execute command", e);
		}
	}
	
	private HashMap<String, Long> getDAMapToExecute(FDPRequest fdpRequest, Me2uProductDTO me2uProductDTO) {
		Object dataAmtToTransferObj = ((FDPSMPPRequestImpl) fdpRequest)
				.getAuxiliaryRequestParameter(AuxRequestParam.ME2U_AMT_TO_TRANSFER);
		HashMap<String, Long> daInstanceToExecute = new HashMap<String, Long>();

		final Map<String, String> configurationMap = fdpRequest.getCircle().getConfigurationKeyValueMap();
		String amountConversionFactor = configurationMap
				.get(ConfigurationKey.Me2U_DATA2SHARE_AMOUNT_CONVERSION_FACTOR.getAttributeName());
		if (null == amountConversionFactor)
			amountConversionFactor = "1";

		// Calculating DA instances to transfer
		if (null != dataAmtToTransferObj) {
			Long dataAmtToTransfer = Long.parseLong(dataAmtToTransferObj + "");
			Long amtCounter = dataAmtToTransfer;
			if (null != me2uProductDTO.getDaInstance()) {
				SortedSet<Entry<String, Long>> sortedValues = entriesSortedByValues(me2uProductDTO.getDaInstance());
				for (Entry<String, Long> entry : sortedValues) {
					Long amtConverted = entry.getValue() / Long.parseLong(amountConversionFactor);
					if (amtCounter <= amtConverted) {
						daInstanceToExecute.put(entry.getKey(), amtCounter);
						break;
					} else {
						amtCounter = amtCounter - amtConverted;
						daInstanceToExecute.put(entry.getKey(), amtConverted);
					}
				}
			}
		}
		return daInstanceToExecute;
	}

	private CommandParam updateDedicatedActParams(CommandParamInput commandParamInput,
			HashMap<String, Long> daInstanceMap, Me2uProductDTO me2uProductDTO, FDPRequest fdpRequest)
			throws ExecutionFailedException {

		List<CommandParam> commandParamList = new ArrayList<CommandParam>();
		Object dataAmtToTransferObj = ((FDPSMPPRequestImpl) fdpRequest)
				.getAuxiliaryRequestParameter(AuxRequestParam.ME2U_AMT_TO_TRANSFER);

		final Map<String, String> configurationMap = fdpRequest.getCircle().getConfigurationKeyValueMap();
		String amountConversionFactor = configurationMap
				.get(ConfigurationKey.Me2U_DATA2SHARE_AMOUNT_CONVERSION_FACTOR.getAttributeName());
		if (null == amountConversionFactor)
			amountConversionFactor = "1";

		if (null != daInstanceMap && !daInstanceMap.isEmpty()) {
			for (String daInstanceKey : daInstanceMap.keySet()) {
				final CommandParamInput paramInput = (CommandParamInput) this.cloneObject(commandParamInput);
				CommandParam childParam = paramInput.getChilderen().get(0);
				for(CommandParam childParameters : childParam.getChilderen()){
					final CommandParamInput input = (CommandParamInput) childParameters;
					if(ChargingUtil.DEDICATED_ACCOUNT_ID.equals(childParameters.getName())){
						input.setValue(me2uProductDTO.getDAID());
					}
					if(ChargingUtil.ADJUSTMENT_AMOUNT_VALUE.equals(childParameters.getName())){
						input.setValue(daInstanceMap.get(daInstanceKey) * (-1)* (Long.parseLong(amountConversionFactor)));
					}
					if(ChargingUtil.PRODUCT_ID.equals(childParameters.getName())) {
						input.setValue(Integer.parseInt(daInstanceKey));
					}
				}


				commandParamList.add(childParam);
			}
		}
		else {
			final CommandParamInput paramInput = (CommandParamInput) this.cloneObject(commandParamInput);
			CommandParam childParam = paramInput.getChilderen().get(0);
			List<CommandParam> commandParamChld = new ArrayList<CommandParam>();
			
			for(CommandParam childParameters : childParam.getChilderen()){
				final CommandParamInput input = (CommandParamInput) childParameters;
				if(ChargingUtil.DEDICATED_ACCOUNT_ID.equals(childParameters.getName())){
					input.setValue(me2uProductDTO.getDAID());
					commandParamChld.add(childParameters);
				}
				if(ChargingUtil.ADJUSTMENT_AMOUNT_VALUE.equals(childParameters.getName())){
					input.setValue(Long.parseLong(dataAmtToTransferObj+"") * Long.parseLong(amountConversionFactor) * (-1));
					commandParamChld.add(childParameters);
				}
				if(ChargingUtil.DEDICATE_ACCOUNT_TYPE.equals(childParameters.getName())){
					input.setValue(FDPConstant.SIX);
					commandParamChld.add(childParameters);
				}
			}
			AbstractCommandParam abstractCommandParam = (AbstractCommandParam)(childParam);
			abstractCommandParam.setChilderen(commandParamChld);
			commandParamList.add(abstractCommandParam);
		}
		commandParamInput.setChilderen(commandParamList);
		return commandParamInput;
	}

		/**
		 * This method creates a clone of an object
		 * @param commandParamInput
		 * @return
		 * @throws ExecutionFailedException
		 */
		public Object cloneObject(final Object objectToClone) throws ExecutionFailedException{
			Object clonedObject = null;	
			try{
				// Make copy of command param
				ByteArrayOutputStream baos = new ByteArrayOutputStream();
				new ObjectOutputStream(baos).writeObject(objectToClone);
				ByteArrayInputStream bais = new ByteArrayInputStream(baos.toByteArray());
				clonedObject = new ObjectInputStream(bais).readObject();
			}catch(Exception e){
				throw new ExecutionFailedException("");
			}
			return clonedObject;
		}


	/**
	 * This method is used to execute the command.
	 *
	 * @param fdpRequest
	 *            The request.
	 * @return The status after execution.
	 * @throws ExecutionFailedException
	 *             Exception in execution.
	 * @throws EvaluationFailedException
	 *             Exception in evaluation.
	 */
	public final Status executeCommand(final FDPRequest fdpRequest) throws ExecutionFailedException,
			EvaluationFailedException {
		if (logger == null) {
			logger = LoggerUtil.getSummaryLoggerFromRequest(fdpRequest);
		}
		final StringBuilder commandInfo = new StringBuilder();
		commandInfo.append(LoggerUtil.getRequestAppender(fdpRequest)).append("PROVCMDRB")
				.append(FDPConstant.LOGGER_KEY_VALUE_DELIMITER).append(commandDisplayName)
				.append(FDPConstant.LOGGER_DELIMITER).append("PARMMOD").append(FDPConstant.LOGGER_KEY_VALUE_DELIMITER);
		FDPLogger.debug(logger, getClass(), "executeCommand()", LoggerUtil.getRequestAppender(fdpRequest)
				+ "Executing command :- " + commandDisplayName);
		for (final CommandParam commandParam : inputParam) {
			if (CommandParameterType.STRUCT.equals(commandParam.getType()) || CommandParameterType.ARRAY.equals(commandParam.getType())) {
            	printCommandParameter(commandParam, commandInfo);
            } else {
                commandInfo.append(commandParam.getName()).append("=").append(commandParam.getValue()).append("#");
            }
			/*if (commandParam instanceof CommandParamInput) {
				final CommandParamInput input = (CommandParamInput) commandParam;
				commandInfo.append(input.getName()).append("=").append(input.getValue()).append("#");
			}*/
		}
		FDPLogger.info(logger, getClass(), "executeCommand()", commandInfo.toString());
		Status status = executeCommandAndProcessResponse(fdpRequest);
		addCommandToStack(fdpRequest);
		return status;
	}

	private Status executeCommandAndProcessResponse(final FDPRequest fdpRequest) throws ExecutionFailedException,
			EvaluationFailedException {
		// Get execution logic using factory pattern and execute xml.
		if (logger == null) {
			logger = LoggerUtil.getSummaryLoggerFromRequest(fdpRequest);
		}
		final Adapter adapter = CommandExecutionBrokerUtil.getAdapter(this, fdpRequest);
		FDPLogger.debug(logger, getClass(), "executeCommandAndProcessResponse()", LoggerUtil.getRequestAppender(fdpRequest)
				+ "Found adapter for command " + adapter);
		final Map<String, Object> commandOutput = executeCommand(adapter, fdpRequest);
		final Object commandOutputObject = commandOutput.get(FDPConstant.COMMAND_OUTPUT);
		Status commandStatus = Status.FAILURE;
		if (null != commandOutput.get(FDPConstant.RESPONSE_CODE)
				&& BusinessConstants.HTTP_ADAPTER_ERROR_CODE.equalsIgnoreCase((String) commandOutput
						.get(FDPConstant.RESPONSE_CODE))) {
			this.setResponseError(new ResponseError(BusinessConstants.HTTP_ADAPTER_ERROR_CODE,
					"External system is down", ErrorTypes.FAULT_CODE.name(), system.name()));
			writeToFailureFile = true;
			
		} else if (commandOutputObject instanceof String) {
			setCommandResponse((String) commandOutputObject);
			FDPLogger.debug(logger, getClass(), "executeCommandAndProcessResponse()", LoggerUtil.getRequestAppender(fdpRequest)
					+ "Command response found " + (String) commandOutputObject);
			setOutputParam(CommandUtil.fromXmlToParameters((String) commandOutputObject, getCommandExecutionType()));
			FDPLogger.debug(logger, getClass(), "executeCommandAndProcessResponse()", LoggerUtil.getRequestAppender(fdpRequest)
					+ "Command output parameters :- " + getOutputParam());
			final CommandExecutionStatus commandExecutionStatus = CommandUtil.checkForCommandStatus(this);
			CommandUtil.postProcessForLogs(commandExecutionStatus, fdpRequest, this);
			this.setResponseError(new ResponseError(commandExecutionStatus.getCode().toString(), commandExecutionStatus
					.getDescription(), commandExecutionStatus.getErrorType(),commandExecutionStatus.getExternalSystem().name()));
			commandStatus = commandExecutionStatus.getStatus();
			this.setStatus(commandStatus);
			FDPLogger.debug(logger, getClass(), "executeCommandAndProcessResponse()", LoggerUtil.getRequestAppender(fdpRequest)
					+ "Command status " + commandStatus.name());
			if (Status.FAILURE.equals(commandStatus) || Status.LOG_ON_FAIL.equals(commandStatus)) {
				FDPLogger.error(logger, getClass(), "executeCommandAndProcessResponse()", LoggerUtil.getRequestAppender(fdpRequest)
						+ commandExecutionStatus.toString());
				if (ExecutionStatus.BACKWARD.equals(fdpRequest.getExecutionStatus())) {
					FDPLogger.info(logger, getClass(), "executeCommandAndProcessResponse()", LoggerUtil.getRequestAppender(fdpRequest)
							+ "CMDRSLT" + FDPConstant.LOGGER_KEY_VALUE_DELIMITER + commandExecutionStatus.toString(true));
				}
			}
		} else {
			FDPLogger.error(logger, getClass(), "executeCommandAndProcessResponse()", LoggerUtil.getRequestAppender(fdpRequest)
					+ "The output parameters not found");
			throw new ExecutionFailedException("The output parameters not found");
		}
		//writing command in CSV for failure
		if((isRollbackCommand && status == Status.FAILURE) || (!isRollbackCommand && writeToFailureFile))			 
			FailureCommandReportGenerationUtil.writeFailureCommandCSV(fdpRequest,this,logger);
		//setting status success always if parameter set  true
		if(isSuccessAlways != null && isSuccessAlways)
			commandStatus= Status.SUCCESS;
		
		return commandStatus;
	}

	private void addCommandToStack(final FDPRequest fdpRequest) {
		if (ExecutionStatus.FORWARD.equals(fdpRequest.getExecutionStatus()) && fdpRequest instanceof FDPRequestImpl) {
			((FDPRequestImpl) fdpRequest).addExecutedCommandInStack(this);
			((FDPRequestImpl) fdpRequest).addExecutedCommand(this);
		} else if (ExecutionStatus.BACKWARD.equals(fdpRequest.getExecutionStatus()) && fdpRequest instanceof FDPRequestImpl) {
			((FDPRequestImpl)fdpRequest).putAuxiliaryRequestParameter(AuxRequestParam.LAST_EXECUTED_ROLLBACK_COMMAND, this);
		}
	}

	/**
	 * This method is used to execute the command in a thread.
	 *
	 * @param adapter
	 *            The adapter to use to execute the command.
	 * @return true if command executed successfully, false if thread was
	 *         interrupted.
	 * @throws ExecutionFailedException
	 *             If there was an exception in execution.
	 */
	private Map<String, Object> executeCommand(final Adapter adapter, final FDPRequest fdpRequest)
			throws ExecutionFailedException {
		Map<String, Object> returnMap = null;
		ExecutorService executor = null;

		if (FDPThreadUtil.isObjectBasesdModel(fdpRequest)) {
			returnMap = getResponse(adapter);
			FDPLogger.debug(logger,getClass(),"executeCommand(): IsThreadPoolEnabled",ConfigurationKey.IS_OBJECT_BASED_MODEL.getAttributeName());
			} 
		else {
			/*executor = FDPThreadUtil.isOldThreadModel(fdpRequest) ? Executors.newSingleThreadExecutor()
					: threadbuilder.getExecutorService(fdpRequest, this.getSystem());*/
			//used the thread
			executor = FDPThreadUtil.isOldThreadModel(fdpRequest) ? Executors.newSingleThreadExecutor()
					: threadbuilder.getThreadPoolExecutor(fdpRequest, this.getSystem());
			

			FDPLogger.debug(logger,getClass(),"executeCommand() Singleton Thread ",Boolean.toString(FDPThreadUtil.isOldThreadModel(fdpRequest)));
			
			final Callable<Map<String, Object>> callable = new Callable<Map<String, Object>>() {
				@Override
				public Map<String, Object> call() throws ExecutionFailedException {
					return adapter.callClient();
				}
			};
			final Future<Map<String, Object>> submit = executor.submit(callable);
			try {
				// if (timeout != null) {
				if (getTimeOutInterfaceWise(fdpRequest) != null) {
					FDPLogger.debug(logger, getClass(), "executeCommand()", "Using timeout as " + timeout);
					returnMap = submit.get(timeout, TimeUnit.MILLISECONDS);
				} /*
					 * else { returnMap = submit.get(); }
					 */
			} catch (final TimeoutException timeoutException) {
				FDPLogger.error(logger, getClass(), "executeCommand()",
						"The command was not executed in the provided time for RID " + fdpRequest.getRequestId()
								+ getLoggerForCommand(),
						timeoutException);
				submit.cancel(true);
				throw new ExecutionFailedException("The command was not executed in the provided time",
						timeoutException);
			} catch (final ExecutionException e) {
				FDPLogger.error(logger, getClass(), "executeCommand()",
						"The command was not executed successfully  for RID " + fdpRequest.getRequestId()
								+ getLoggerForCommand(),
						e);
				submit.cancel(true);
				throw new ExecutionFailedException("The command was not executed successfully", e);
			} catch (final InterruptedException e) {
				FDPLogger.error(logger, getClass(), "executeCommand()", "The command thread was interrupted. for RID "
						+ fdpRequest.getRequestId() + getLoggerForCommand(), e);
				submit.cancel(true);
				throw new ExecutionFailedException("The command thread was interrupted.", e);
			} finally {
				if (FDPThreadUtil.isOldThreadModel(fdpRequest)) {
					executor.shutdownNow();
				}
				// executor.shutdownNow();
			}
		}
		FDPLogger.info(logger, getClass(), "executeCommand()", BusinessConstants.REQUEST_ID + FDPConstant.LOGGER_KEY_VALUE_DELIMITER + 
				fdpRequest.getRequestId() + FDPConstant.LOGGER_DELIMITER +
				"Response status found " + (returnMap!=null?returnMap.get(FDPConstant.RESPONSE_CODE):" :Return MAP IS NULL"));
		return returnMap;
	}

	private Map<String, Object> getResponse(Adapter adapter) throws ExecutionFailedException {
		
		return adapter.callClient();
	}

	@Override
	public String getCommandName() {
		return commandName;
	}

	/**
	 * @param commandNameToSet
	 *            the commandName to set
	 */
	public void setCommandName(final String commandNameToSet) {
		this.commandName = commandNameToSet;
	}

	@Override
	public String getCommandDisplayName() {
		return commandDisplayName;
	}

	/**
	 * @param commandDisplayNameToSet
	 *            the commandDisplayName to set
	 */
	public void setCommandDisplayName(final String commandDisplayNameToSet) {
		this.commandDisplayName = commandDisplayNameToSet;
	}

	@Override
	public List<CommandParam> getInputParam() {
		return inputParam;
	}

	/**
	 * @param inputParamToSet
	 *            the inputParam to set
	 */
	public void setInputParam(final List<CommandParam> inputParamToSet) {
		this.inputParam = inputParamToSet;
	}

	/**
	 * @return the outputParam
	 */
	public Map<String, CommandParam> getOutputParam() {
		return outputParam;
	}

	/**
	 * @param outputParamToSet
	 *            the outputParam to set
	 */
	public void setOutputParam(final Map<String, CommandParam> outputParamToSet) {
		this.outputParam = outputParamToSet;
	}

	/**
	 * @param outputParamToSet
	 *            the outputParam to set
	 */
	public void addOutputParam(final Map<String, CommandParam> outputParamToSet) {
		if (this.outputParam == null) {
			this.outputParam = new HashMap<String, CommandParam>();
		}
		this.outputParam.putAll(outputParamToSet);
	}

	@Override
	public ExternalSystem getSystem() {
		return system;
	}

	/**
	 * @param systemToSet
	 *            the system to set
	 */
	public void setSystem(final ExternalSystem systemToSet) {
		this.system = systemToSet;
	}

	/**
	 * @return the commandType
	 */
	public CommandDefinition getCommandType() {
		return commandType;
	}

	/**
	 * @param commandTypeToSet
	 *            the commandType to set
	 */
	public void setCommandType(final CommandDefinition commandTypeToSet) {
		this.commandType = commandTypeToSet;
	}

	@Override
	public CommandExecutionType getCommandExecutionType() {
		return commandExecutionType;
	}

	/**
	 * @param commandExecutionTypeToSet
	 *            the commandExecutionType to set
	 */
	public void setCommandExecutionType(final CommandExecutionType commandExecutionTypeToSet) {
		this.commandExecutionType = commandExecutionTypeToSet;
	}

	/**
	 * @param timeoutToSet
	 *            The timeout to set.
	 */
	public void setTimeout(final Long timeoutToSet) {
		this.timeout = timeoutToSet;
	}

	/**
	 * @return the timeout.
	 */
	public Long getTimeout() {
		return timeout;
	}

	/**
	 * @return the commandResponse
	 */
	public String getCommandResponse() {
		return commandResponse;
	}

	/**
	 * @param commandResponseToSet
	 *            the commandResponse to set
	 */
	public void setCommandResponse(final String commandResponseToSet) {
		this.commandResponse = commandResponseToSet;
	}

	@Override
	public String toString() {
		return " command display name " + commandDisplayName + " interface " + system.name();
	}

	@Override
	public CommandParam getInputParam(final String name) {
		for (final CommandParam inputCommandParam : getInputParam()) {
			if(inputCommandParam.getName() != null){
				if (inputCommandParam.getName().equals(name)) {
					return inputCommandParam;
				}
			}
		}
		return null;
	}

	/**
	 * @return the responseError
	 */
	@Override
	public ResponseError getResponseError() {
		return responseError;
	}

	/**
	 * @param responseError
	 *            the responseError to set
	 */
	public void setResponseError(final ResponseError responseError) {
		this.responseError = responseError;
	}
	
	@Override
	public Status getExecutionStatus() {
		return status;
	}

	/**
	 * @param status the status to set
	 */
	public void setStatus(Status status) {
		this.status = status;
	}


	
	/**
	 * @return the parameterDTOList
	 */
	public List<ParameterDTO> getParameterDTOList() {
		return parameterDTOList;
	}

	
	
	/**
	 * @return the isRollbackCommand
	 */
	public boolean isRollbackCommand() {
		return isRollbackCommand;
	}

	/**
	 * @param isRollbackCommand the isRollbackCommand to set
	 */
	public void setRollbackCommand(boolean isRollbackCommand) {
		this.isRollbackCommand = isRollbackCommand;
	}
	
	

	/**
	 * @return the isSuccessAlways
	 */
	public Boolean getIsSuccessAlways() {
		return isSuccessAlways;
	}

	/**
	 * @param isSuccessAlways the isSuccessAlways to set
	 */
	public void setIsSuccessAlways(Boolean isSuccessAlways) {
		this.isSuccessAlways = isSuccessAlways;
	}
	
	
	/**
	 * @return the writeToFailureFile
	 */
	public boolean isWriteToFailureFile() {
		return writeToFailureFile;
	}

	/**
	 * @param writeToFailureFile the writeToFailureFile to set
	 */
	public void setWriteToFailureFile(boolean writeToFailureFile) {
		this.writeToFailureFile = writeToFailureFile;
	}

	/**
	 * @param parameterDTOList the parameterDTOList to set
	 */
	public void setParameterDTOList(List<ParameterDTO> parameterDTOList) {
		this.parameterDTOList = parameterDTOList;
	}

	static <K, V extends Comparable<? super V>> SortedSet<Map.Entry<K, V>> entriesSortedByValues(Map<K, V> map) {
		SortedSet<Map.Entry<K, V>> sortedEntries = new TreeSet<Map.Entry<K, V>>(new Comparator<Map.Entry<K, V>>() {
			@Override
			public int compare(Map.Entry<K, V> e1, Map.Entry<K, V> e2) {
				int res = e1.getValue().compareTo(e2.getValue());
				return res != 0 ? res : 1;
			}
		});
		sortedEntries.addAll(map.entrySet());
		return sortedEntries;
	}

	private Object getTimeOutInterfaceWise(FDPRequest fdpRequest) {
		Long timeOut = 0L;
		if (!StringUtil.isNullOrEmpty(fdpRequest.getCircle().getConfigurationKeyValueMap()
				.get(ExternalSystem.getExternalSystemTimeOutKey(this.getSystem())))) {
			timeOut = Long.valueOf(fdpRequest.getCircle().getConfigurationKeyValueMap()
					.get(ExternalSystem.getExternalSystemTimeOutKey(this.getSystem())).trim());
		}
		this.timeout = (timeOut == 0L) ? ExternalSystem.getExternalSystemTimeOut(this.getSystem()) : timeOut;
		return timeout;
	}

	private String getLoggerForCommand() {
		return FDPConstant.LOGGER_DELIMITER + "CMD_NAME:" + this.commandDisplayName
				+ FDPConstant.LOGGER_KEY_VALUE_DELIMITER + " CMD_INTFC:" + getSystem()
				+ FDPConstant.LOGGER_KEY_VALUE_DELIMITER + "CMD_THD_TOUT:" + this.timeout;

	}
	/**
	 * This method is evaluate the actual parameter whether its offerDate or offerDateTime based on the offerType
	 * @param fdpRequest
	 * @return
	 */
	private String getExpiryParamToEvaluate(FDPRequest fdpRequest)
	{
		String expiryParamtoEvaluate = null;
		if(fdpRequest.getChannel().equals(ChannelType.FLYTXT)) {
			SPCommandNameToDisplayEnum displayEnum = SPCommandNameToDisplayEnum.getSPCommandNameToDisplayEnum(commandDisplayName);
			Object offerValidity=fdpRequest.getAuxiliaryRequestParameter(AuxRequestParam.OFFER_VALIDITY);
			if(SPCommandNameToDisplayEnum.UPDATE_OFFER==displayEnum && null!=offerValidity && !"0".equals(offerValidity))
				{
					
					CommandParamInput commandParamInput =  (CommandParamInput) getInputParam(FulfillmentParameters.OFFER_TYPE.getValue());
					if(null!=commandParamInput && "2".equals(commandParamInput.getDefinedValue()))	
						expiryParamtoEvaluate =FDPConstant.EXPIRY_DATE_TIME;						
					else
						expiryParamtoEvaluate = FDPConstant.EXPIRY_DATE;
					
				}
			}
		return expiryParamtoEvaluate;
	}
	
	private void printCommandParameter(CommandParam commandParam, StringBuilder commandInfo) {
		List<CommandParam> childs = commandParam.getChilderen();
		 for (CommandParam childParam : childs) {
             if (childParam instanceof CommandParamInput && childParam.getName() != null) {
            	 if (childParam.getType().equals(CommandParameterType.STRUCT) || childParam.getType().equals(CommandParameterType.ARRAY))
            		 printCommandParameter(childParam, commandInfo);
            	 else
            		 commandInfo.append(childParam.getName()).append("=").append(childParam.getValue()).append("#");
             }
         }
	}
	
	
	
}
