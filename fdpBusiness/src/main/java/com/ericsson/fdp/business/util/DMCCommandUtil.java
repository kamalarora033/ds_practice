package com.ericsson.fdp.business.util;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.XML;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.ericsson.fdp.business.command.param.impl.CommandParamOutput;
import com.ericsson.fdp.business.enums.ErrorTypes;
import com.ericsson.fdp.business.enums.RequestMetaValuesKey;
import com.ericsson.fdp.business.node.AbstractNode;
import com.ericsson.fdp.business.vo.CommandExecutionStatus;
import com.ericsson.fdp.common.constants.FDPConstant;
import com.ericsson.fdp.common.enums.FDPNodeAddInfoKeys;
import com.ericsson.fdp.common.enums.Primitives;
import com.ericsson.fdp.common.enums.Status;
import com.ericsson.fdp.core.command.FDPCommand;
import com.ericsson.fdp.core.command.param.CommandParam;
import com.ericsson.fdp.core.dsm.framework.service.FDPCache;
import com.ericsson.fdp.core.exception.EvaluationFailedException;
import com.ericsson.fdp.core.exception.ExecutionFailedException;
import com.ericsson.fdp.core.request.FDPRequest;
import com.ericsson.fdp.core.serviceprovisioning.bean.FDPAppBag;
import com.ericsson.fdp.core.utils.ApplicationConfigUtil;
import com.ericsson.fdp.dao.dto.FDPResultCodesDTO;
import com.ericsson.fdp.dao.enums.CommandParameterType;

/**
 * The Class MCarbonCommandUtil.
 * 
 * @author Ericsson
 */
public final class DMCCommandUtil {

	/** The Constant KEY_VALUE_SEPERATOR. */
	private static final String KEY_VALUE_SEPERATOR = "=";

	/** The Constant PARAMETER_SEPERATOR. */
	private static final String PARAMETER_SEPERATOR = "&";

	/** The response Code */
	private static final String RESPONSE_CODE = "responseCode";

	/** The Constant LOGGER. */
	private static final Logger LOGGER = LoggerFactory.getLogger(DMCCommandUtil.class);

	/**
	 * Instantiates a new m carbon command util.
	 */
	private DMCCommandUtil() {

	}

	/**
	 * To m carbon format.
	 * 
	 * @param command
	 *            the command
	 * @return the string
	 * @throws ExecutionFailedException
	 *             the execution failed exception
	 */
	public static String toDMCFormat(final FDPCommand command) throws ExecutionFailedException {
		final StringBuilder dmcFormat = new StringBuilder();
		StringBuilder dmcCommandName = new StringBuilder(command.getCommandName()).append(FDPConstant.QUESTIONMARK);
		for (final CommandParam commandParam : command.getInputParam()) {
			dmcFormat.append(getParamString(commandParam)).append(PARAMETER_SEPERATOR);
		}
		
		return dmcFormat.length() > PARAMETER_SEPERATOR.length() ? dmcCommandName.append(dmcFormat.substring(0,
				dmcFormat.length() - PARAMETER_SEPERATOR.length())).toString() : null;
	}

	/**
	 * Gets the param string.
	 * 
	 * @param commandParam
	 *            the command param
	 * @return the param string
	 * @throws ExecutionFailedException
	 *             the execution failed exception
	 */
	private static String getParamString(final CommandParam commandParam) throws ExecutionFailedException {
		final StringBuilder dmcFormat = new StringBuilder();
		switch (commandParam.getType()) {
		case PRIMITIVE:
			dmcFormat.append(commandParam.getName()).append(KEY_VALUE_SEPERATOR)
					.append(commandParam.getValue().toString());
			break;
		default:
			throw new ExecutionFailedException("The command parameter type is not recognized. It is of type "
					+ commandParam.getType());
		}
		return dmcFormat.toString();
	}

	/**
	 * This method check the command status.
	 * 
	 * @param fdpCommand
	 *            the fdp command
	 * @return the command execution status
	 */
	public static CommandExecutionStatus checkForDMCCommandStatus(
			final FDPCommand fdpCommand) throws ExecutionFailedException {
		final FDPCache<FDPAppBag, Object> configCache = ApplicationConfigUtil.getApplicationConfigCache();
		FDPResultCodesDTO isFailure = null;
		final CommandExecutionStatus commandExecutionStatus = new CommandExecutionStatus(Status.FAILURE, 0,
				FDPConstant.ERROR_CODE_MAPPING_MISSING, ErrorTypes.FAULT_CODE.name(),
				com.ericsson.fdp.common.enums.ExternalSystem.valueOf(fdpCommand.getSystem().name()));
		final CommandParam commandParam = fdpCommand
				.getOutputParam(RESPONSE_CODE.toLowerCase());
		if(null != commandParam ) {
			isFailure = CommandUtil.checkResponseCode(configCache, commandParam.getValue(),
					fdpCommand.getCommandDisplayName());
			commandExecutionStatus.setCode((Integer) commandParam.getValue());
			commandExecutionStatus.setErrorType(ErrorTypes.RESPONSE_CODE.name());
			if (isFailure != null) {
				commandExecutionStatus.setDescription(isFailure.getResultCodeDesc());
			}
		}
		commandExecutionStatus.setStatus(CommandUtil.getStatus(isFailure));
		return commandExecutionStatus;
	}

	/**
	 * From m carbon to parameters.
	 * 
	 * @param outputXmlAsString
	 *            the output xml as string
	 * @return the map
	 */
	/*public static Map<String, CommandParam> fromDMCOutputFormatToParameters(final String outputXmlAsString) {
		final Map<String, CommandParam> outputParams = new LinkedHashMap<String, CommandParam>();
		final CommandParamOutput fdpCommandParamOutput = new CommandParamOutput();
		fdpCommandParamOutput.setPrimitiveValue(Primitives.STRING);
		fdpCommandParamOutput.setType(CommandParameterType.PRIMITIVE);

		fdpCommandParamOutput.setValue(outputXmlAsString);
		outputParams.put(RESULT_CODE_VALUE, fdpCommandParamOutput);
		return outputParams;
	}*/
	
	/**
	 * This method lookups the logical name from node directly.
	 * 
	 * @param fdpRequest
	 * @return
	 */
	private static String getLogicalNameFromNode(final FDPRequest fdpRequest) {
		String logicalName = null;
		Object object = fdpRequest.getValueFromRequest(RequestMetaValuesKey.NODE);
		if(null != object && object instanceof AbstractNode) {
			AbstractNode abstractNode = (AbstractNode) object;
			logicalName = (String) abstractNode.getAdditionalInfo(FDPNodeAddInfoKeys.EXTERNAL_SYSTEM_LOGICAL_NAME.name());
			LOGGER.debug(
					"AuxRequestParam.EXTERNAL_SYSTEM_LOGICAL_NAME value in node is {} for requestId {}.",
					logicalName, fdpRequest.getRequestId());
		}
		return logicalName; 
	}
	
	public static Map<String, CommandParam> fromDMCXmlToParameters(final String outputParam)
			throws EvaluationFailedException {
		final Map<String, CommandParam> outputParams = new LinkedHashMap<String, CommandParam>();
		final List<String> pathList = new ArrayList<String>();
		try {
			final JSONObject xmlJSONObj = XML.toJSONObject(outputParam);
			final JSONObject mdmResponse = (JSONObject) xmlJSONObj.get("mdm");
			final JSONObject response = (JSONObject) mdmResponse.get("response");
			final JSONObject result = (JSONObject) response.get("result");
			CommandParamOutput fdpCommandParamOutput = null;
			fdpCommandParamOutput = new CommandParamOutput();
			fdpCommandParamOutput.setPrimitiveValue(Primitives.INTEGER);
		    fdpCommandParamOutput.setName("responseCode");
			fdpCommandParamOutput.setValue(Integer.parseInt(String.valueOf(result.get("code"))));
			fdpCommandParamOutput.setType(CommandParameterType.PRIMITIVE);
			outputParams.put(RESPONSE_CODE.toLowerCase(), fdpCommandParamOutput);
			if(outputParam.contains("parm")){
				final JSONObject record = (JSONObject) result.get("record");
				final JSONArray param = (JSONArray) record.get("parm");
				fromXmlToParameters(outputParams, pathList, param);
			}
		} catch (final JSONException e) {
			throw new EvaluationFailedException("The input xml is not valid.", e);
		}
		return outputParams;
	}
	
	public static void fromXmlToParameters(
			final Map<String, CommandParam> outputParams,
			final List<String> pathList, final JSONArray value)
			throws JSONException, EvaluationFailedException {
	//@SuppressWarnings("rawtypes")
		//final Iterator iterator = value.keys();
		// System.out.println("inside fromXmlToParameters :: "+outputParams);
		//while (iterator.hasNext()) {
		 for(int i=0;i<value.length();i++){
			 JSONObject json=value.getJSONObject(i);
			final Iterator it=json.keys();
				while(it.hasNext()){
					final String paramType = (String) it.next();
				fromXmlToPrimitiveParam(outputParams, pathList, json,paramType);
			
					
				}
			
	}
		 
	
}
	private static void fromXmlToPrimitiveParam(
			final Map<String, CommandParam> outputParams,
			final List<String> pathList, final JSONObject value,
			final String paramType) throws JSONException,
			EvaluationFailedException {
		final CommandParamOutput commandOutput = createOutputParameter(
				paramType, value);
		if (commandOutput != null) {
			
			outputParams.put(value.getString("name"), commandOutput);
			
		} else {
			throw new EvaluationFailedException(
					"The param type is not defined.");
		}
	}
	
	private static CommandParamOutput createOutputParameter(
				final String outputVariable, final JSONObject value)
				throws JSONException, EvaluationFailedException {
					CommandParamOutput fdpCommandParamOutput = null;
					fdpCommandParamOutput = new CommandParamOutput();
					fdpCommandParamOutput.setPrimitiveValue(Primitives.STRING);
				    fdpCommandParamOutput.setName(value.getString("name"));
					fdpCommandParamOutput.setValue(String.valueOf(value.get("value")));
					fdpCommandParamOutput.setType(CommandParameterType.PRIMITIVE);
			return fdpCommandParamOutput;
		} 
	/*public static void main(String[] args) throws EvaluationFailedException {
	//	String st="<?xml version=\"1.0\" encoding=\"UTF-8\"?><methodResponse><params><param><value><struct><member><name>accountFlags</name><value><struct><member><name>activationStatusFlag</name><value><boolean>1</boolean></value></member><member><name>negativeBarringStatusFlag</name><value><boolean>0</boolean></value></member><member><name>serviceFeePeriodExpiryFlag</name><value><boolean>0</boolean></value></member><member><name>serviceFeePeriodWarningActiveFlag</name><value><boolean>0</boolean></value></member><member><name>supervisionPeriodExpiryFlag</name><value><boolean>0</boolean></value></member><member><name>supervisionPeriodWarningActiveFlag</name><value><boolean>0</boolean></value></member></struct></value></member><member><name>accountFlagsBefore</name><value><struct><member><name>activationStatusFlag</name><value><boolean>1</boolean></value></member><member><name>negativeBarringStatusFlag</name><value><boolean>0</boolean></value></member><member><name>serviceFeePeriodExpiryFlag</name><value><boolean>0</boolean></value></member><member><name>serviceFeePeriodWarningActiveFlag</name><value><boolean>0</boolean></value></member><member><name>supervisionPeriodExpiryFlag</name><value><boolean>0</boolean></value></member><member><name>supervisionPeriodWarningActiveFlag</name><value><boolean>0</boolean></value></member></struct></value></member></struct></value></param></params></methodResponse>";
		String xmlStr = "<?xml version=\"1.0\" encoding=\"utf-8\"?><mdm><response><result code=\"200\" desc=\"OK\"><record><param name=\"msisdn\" value=\"+99999999\"/><param name=\"handset.brandmodel\" value=\"nokia\"/><param name=\"sub.imei\" value=\"34343433434\"/></record></result></response></mdm>";
        
      //  JSONObject soapDatainJsonObject = XML.toJSONObject(xmlStr);
     //  System.out.println(soapDatainJsonObject);
		Map<String, CommandParam> outputParams = fromDMCXmlToParameters(xmlStr);
		System.out.println(outputParams);
       
       


	}*/
}