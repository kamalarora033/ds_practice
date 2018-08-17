package com.ericsson.fdp.business.util;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.ericsson.fdp.FDPCacheable;
import com.ericsson.fdp.business.charging.impl.ExpressionCondition;
import com.ericsson.fdp.business.command.param.impl.CommandParamInput;
import com.ericsson.fdp.business.command.param.impl.CommandParamOutput;
import com.ericsson.fdp.business.condition.AbstractCondition;
import com.ericsson.fdp.business.condition.impl.EqualsCondition;
import com.ericsson.fdp.business.condition.impl.GreaterThanCondition;
import com.ericsson.fdp.business.condition.impl.GreaterThanEqualsCondition;
import com.ericsson.fdp.business.condition.impl.LessThanCondition;
import com.ericsson.fdp.business.condition.impl.LessThanEqualsCondition;
import com.ericsson.fdp.business.condition.impl.NotEqualsCondition;
import com.ericsson.fdp.business.enums.AuxRequestParam;
import com.ericsson.fdp.business.enums.Command;
import com.ericsson.fdp.business.enums.ConditionType;
import com.ericsson.fdp.business.enums.ExecutionStatus;
import com.ericsson.fdp.business.enums.RequestMetaValuesKey;
import com.ericsson.fdp.business.expression.Expression;
import com.ericsson.fdp.business.expression.impl.FDPCombinedAIRExpressionOperator;
import com.ericsson.fdp.business.expression.impl.FDPCommandLeftOperand;
import com.ericsson.fdp.business.expression.impl.FDPExpressionCondition;
import com.ericsson.fdp.business.product.Product;
import com.ericsson.fdp.common.constants.FDPConstant;
import com.ericsson.fdp.common.enums.ModuleType;
import com.ericsson.fdp.common.enums.Primitives;
import com.ericsson.fdp.common.enums.ProductType;
import com.ericsson.fdp.common.exception.ConditionFailedException;
import com.ericsson.fdp.common.util.DateUtil;
import com.ericsson.fdp.common.util.StringUtil;
import com.ericsson.fdp.core.command.FDPCommand;
import com.ericsson.fdp.core.command.param.CommandParam;
import com.ericsson.fdp.core.config.utils.PropertyUtils;
import com.ericsson.fdp.core.exception.EvaluationFailedException;
import com.ericsson.fdp.core.exception.ExecutionFailedException;
import com.ericsson.fdp.core.request.FDPRequest;
import com.ericsson.fdp.core.request.impl.FDPRequestImpl;
import com.ericsson.fdp.core.serviceprovisioning.bean.FDPMetaBag;
import com.ericsson.fdp.core.utils.ApplicationConfigUtil;
import com.ericsson.fdp.dao.enums.CommandParameterDataType;
import com.ericsson.fdp.dao.enums.CommandParameterType;

/**
 * This class is a utility class that works on command parameters.
 * 
 * @author Ericsson
 */
public class CommandParamUtil {

	/**
	 * Instantiates a new command param util.
	 */
	private CommandParamUtil() {

	}

	private static final String serviceClassId = PropertyUtils
			.getProperty("esf.blackberry.serviceClassId");

	private static final String psoBit = PropertyUtils
			.getProperty("esf.blackberry.psoBit");

	private static final String offerId = PropertyUtils
			.getProperty("esf.blackberry.offerId");

	/**
	 * This method creates the string representation of xml for a parameter.
	 * 
	 * @param commandParam
	 *            The parameter for which the string representation is required.
	 * @return The string representation of the parameter.
	 * @throws ExecutionFailedException
	 *             Exception, if the string representation could not be created.
	 */
	public static String toXmlForParam(final CommandParam commandParam)
			throws ExecutionFailedException {
		final StringBuilder xmlFormat = new StringBuilder();
		switch (commandParam.getType()) {
		case ARRAY:
			xmlFormat.append(toXmlFromArrayParam(commandParam));
			break;
		case PRIMITIVE:
			xmlFormat.append(toXmlfromPrimitiveParam(commandParam));
			break;
		case STRUCT:
			xmlFormat.append(toXmlFromStructParam(commandParam));
			break;
		default:
			throw new ExecutionFailedException(
					"The command parameter type is not recognized. It is of type "
							+ commandParam.getType());
		}
		return xmlFormat.toString();
	}
	
	public static String toXmlForParamLoyalty(final CommandParam commandParam)
			throws ExecutionFailedException {
		final StringBuilder xmlFormat = new StringBuilder();
		switch (commandParam.getType()) {
		case PRIMITIVE:
			xmlFormat.append(toXmlfromPrimitiveParamLoyalty(commandParam));
			break;
		case STRUCT:
			xmlFormat.append(toXmlFromStructParamLoyalty(commandParam));
			break;
		default:
			throw new ExecutionFailedException("The command parameter type is not recognized. It is of type "+ commandParam.getType());
		}
		return xmlFormat.toString();
	}

	/**
	 * This method is used to create a string representation of the xml for a
	 * parameter of type struct.
	 * 
	 * @param commandParam
	 *            The parameter for which the string representation is to be
	 *            created.
	 * @return The string representation.
	 * @throws ExecutionFailedException
	 *             Exception, if the string representation could not be created.
	 */
	private static String toXmlFromStructParamLoyalty(final CommandParam commandParam)
			throws ExecutionFailedException {
		final StringBuilder xmlFormat = new StringBuilder();
		xmlFormat.append("<").append(commandParam.getName()).append(">");
		for (final CommandParam fdpCommandParam : commandParam.getChilderen()) {
			xmlFormat.append(toXmlForParamLoyalty(fdpCommandParam));
		}
		xmlFormat.append("</").append(commandParam.getName()).append(">");
		return xmlFormat.toString();
	}
	
	private static String toXmlFromStructParam(final CommandParam commandParam)
			throws ExecutionFailedException {
		final StringBuilder xmlFormat = new StringBuilder(256);
		xmlFormat.append("<struct>").append("\n");
		for (final CommandParam fdpCommandParam : commandParam.getChilderen()) {
			xmlFormat.append("<member>").append("\n").append("<name>")
					.append(fdpCommandParam.getName()).append("</name>")
					.append("\n").append("<value>").append("\n")
					.append(toXmlForParam(fdpCommandParam)).append("</value>")
					.append("\n").append("</member>").append("\n");
		}
		xmlFormat.append("</struct>").append("\n");
		return xmlFormat.toString();
	}

	/**
	 * This method is used to create a string representation of the xml for
	 * primitive type.
	 * 
	 * @param commandParam
	 *            The parameter for which the string representation is to be
	 *            created.
	 * @return The string representation.
	 */
	private static String toXmlfromPrimitiveParam(
			final CommandParam commandParam) {
		final StringBuilder xmlFormat = new StringBuilder();
		final String primitiveType = commandParam.getPrimitiveValue()
				.getValue();
		xmlFormat.append("<").append(primitiveType).append(">")
				.append(commandParam.getValue().toString()).append("</")
				.append(primitiveType).append(">").append("\n");
		return xmlFormat.toString();
	}
	
	private static String toXmlfromPrimitiveParamLoyalty(
			final CommandParam commandParam) {
		final StringBuilder xmlFormat = new StringBuilder();
		xmlFormat.append("<").append(commandParam.getName()).append(">").append(commandParam.getValue())
		.append("</").append(commandParam.getName()).append(">");
		return xmlFormat.toString();
	}

	/**
	 * This method is used to create a string representation of the xml for
	 * parameter of type array.
	 * 
	 * @param commandParam
	 *            The parameter for which the string representation is to be
	 *            created.
	 * @return The string representation.
	 * @throws ExecutionFailedException
	 *             Exception, if the string representation could not be created.
	 */
	private static String toXmlFromArrayParam(final CommandParam commandParam)
			throws ExecutionFailedException {
		final StringBuilder xmlFormat = new StringBuilder(256);
		xmlFormat.append("<array>").append("\n").append("<data>").append("\n");
		for (final CommandParam fdpCommandParam : commandParam.getChilderen()) {
			xmlFormat.append("<value>").append("\n")
					.append(toXmlForParam(fdpCommandParam)).append("</value>")
					.append("\n");
		}
		xmlFormat.append("</data>").append("\n").append("</array>")
				.append("\n");
		return xmlFormat.toString();
	}

	/**
	 * This method is used to create a map of fully qualified path of the
	 * parameter and its value from the json representation of a xml. This
	 * method is used to evaluate the parameters recursively.
	 * 
	 * @param outputParams
	 *            The map to which the parameters will be added.
	 * @param pathList
	 *            The path list till the last parameter. This is used to
	 *            evaluate the fully qualified path.
	 * @param value
	 *            The parameter object for which the evaluation is to be done.
	 * @throws EvaluationFailedException
	 *             Exception, if in creating the map.
	 * @throws JSONException
	 *             Exception if the xml is not valid.
	 */
	public static void fromXmlToParameters(
			final Map<String, CommandParam> outputParams,
			final List<String> pathList, final JSONObject value)
			throws JSONException, EvaluationFailedException {
		@SuppressWarnings("rawtypes")
		final Iterator iterator = value.keys();
		// System.out.println("inside fromXmlToParameters :: "+outputParams);
		while (iterator.hasNext()) {
			final String paramType = (String) iterator.next();
			if (paramType.equalsIgnoreCase(CommandParameterDataType.ARRAY
					.getValue())) {
				fromXmltoArrayParam(outputParams, pathList, value, paramType);
			} else if (paramType
					.equalsIgnoreCase(CommandParameterDataType.STRUCT
							.getValue())) {
				fromXmlToStructParam(outputParams, pathList, value, paramType);
			} else {
				fromXmlToPrimitiveParam(outputParams, pathList, value,
						paramType);
			}
		}
	}

	/**
	 * This method is used to convert primitive parameters from the json
	 * representation to the objects.
	 * 
	 * @param outputParams
	 *            The map to which the parameters will be added.
	 * @param pathList
	 *            The path list till the last parameter. This is used to
	 *            evaluate the fully qualified path.
	 * @param value
	 *            The parameter object for which the evaluation is to be done.
	 * @param paramType
	 *            The parameter primitive type.
	 * @throws JSONException
	 *             Exception, if the xml is not well formed.
	 * @throws EvaluationFailedException
	 *             Exception, if the parameter could not be identified.
	 */
	private static void fromXmlToPrimitiveParam(
			final Map<String, CommandParam> outputParams,
			final List<String> pathList, final JSONObject value,
			final String paramType) throws JSONException,
			EvaluationFailedException {
		final CommandParamOutput commandOutput = createOutputParameter(
				paramType, value);
		if (commandOutput != null) {
			final String path = StringUtil.toStringFromList(pathList,
					FDPConstant.PARAMETER_SEPARATOR);
			outputParams.put(path.toLowerCase(), commandOutput);
			commandOutput.setName(path);
		} else {
			throw new EvaluationFailedException(
					"The param type is not defined.");
		}
	}

	/**
	 * This method is used to convert parameters of type struct from the json
	 * representation to the objects.
	 * 
	 * @param outputParams
	 *            The map to which the parameters will be added.
	 * @param pathList
	 *            The path list till the last parameter. This is used to
	 *            evaluate the fully qualified path.
	 * @param value
	 *            The parameter object for which the evaluation is to be done.
	 * @param paramType
	 *            The parameter primitive type.
	 * @throws JSONException
	 *             Exception, if the xml is not well formed.
	 * @throws EvaluationFailedException
	 *             Exception, if the parameter could not be identified.
	 */
	private static void fromXmlToStructParam(
			final Map<String, CommandParam> outputParams,
			final List<String> pathList, final JSONObject value,
			final String paramType) throws JSONException,
			EvaluationFailedException {
		final JSONObject arrayList = (JSONObject) value.get(paramType);
		final Object objectForMember = arrayList.get("member");
		if (objectForMember instanceof JSONArray) {
			final JSONArray jsonArray = (JSONArray) objectForMember;
			for (int arrayIndex = 0; arrayIndex < jsonArray.length(); arrayIndex++) {
				final JSONObject arrayObject = (JSONObject) jsonArray
						.get(arrayIndex);
				pathList.add((String) arrayObject.get("name"));
				/*
				 * if(((String)
				 * arrayObject.get("name")).equalsIgnoreCase("offerID")){
				 * //System.out.println("offer Id found :: "+(JSONObject)
				 * arrayObject.get("value")); } if(((String)
				 * arrayObject.get("name"
				 * )).equalsIgnoreCase("offerInformationList")){
				 * System.out.println("offer information found"); }
				 */
				fromXmlToParameters(outputParams, pathList,
						(JSONObject) arrayObject.get("value"));
				pathList.remove(pathList.size() - 1);
			}
		} else if (objectForMember instanceof JSONObject) {
			final JSONObject arrayObject = (JSONObject) objectForMember;
			pathList.add((String) arrayObject.get("name"));
			fromXmlToParameters(outputParams, pathList,
					(JSONObject) arrayObject.get("value"));
			pathList.remove(pathList.size() - 1);
		}
	}

	/**
	 * This method is used to convert parameters of type array from the json
	 * representation to the objects.
	 * 
	 * @param outputParams
	 *            The map to which the parameters will be added.
	 * @param pathList
	 *            The path list till the last parameter. This is used to
	 *            evaluate the fully qualified path.
	 * @param value
	 *            The parameter object for which the evaluation is to be done.
	 * @param paramType
	 *            The parameter primitive type.
	 * @throws JSONException
	 *             Exception, if the xml is not well formed.
	 * @throws EvaluationFailedException
	 *             Exception, if the parameter could not be identified.
	 */
	private static void fromXmltoArrayParam(
			final Map<String, CommandParam> outputParams,
			final List<String> pathList, final JSONObject value,
			final String paramType) throws JSONException,
			EvaluationFailedException {
		final JSONObject arrayList = (JSONObject) value.get(paramType);
		final JSONObject name = (JSONObject) arrayList.get("data");
		final Object object = name.has("value") ? name.get("value") : null;
		if (object instanceof JSONObject) {
			fromXmlToArrayPrimitiveParam(outputParams, pathList,
					(JSONObject) object);
		}
		if (object instanceof JSONArray) {
			fromXmlToArrayStructParam(outputParams, pathList,
					(JSONArray) object);
		}
	}

	/**
	 * This method is used to convert parameters of type array of struct from
	 * the json representation to the objects.
	 * 
	 * @param outputParams
	 *            The map to which the parameters will be added.
	 * @param pathList
	 *            The path list till the last parameter. This is used to
	 *            evaluate the fully qualified path.
	 * @param jsonArray
	 *            The array of struct object for which the evaluation is to be
	 *            done.
	 * @throws JSONException
	 *             Exception, if the xml is not well formed.
	 * @throws EvaluationFailedException
	 *             Exception, if the parameter could not be identified.
	 */
	private static void fromXmlToArrayStructParam(
			final Map<String, CommandParam> outputParams,
			final List<String> pathList, final JSONArray jsonArray)
			throws JSONException, EvaluationFailedException {
		for (int arrayIndex = 0; arrayIndex < jsonArray.length(); arrayIndex++) {
			final JSONObject arrayObject = (JSONObject) jsonArray
					.get(arrayIndex);
			pathList.add(String.valueOf(arrayIndex));
			fromXmlToParameters(outputParams, pathList, arrayObject);
			pathList.remove(pathList.size() - 1);
		}
	}

	/**
	 * This method is used to convert parameters of type array of primitives
	 * from the json representation to the objects.
	 * 
	 * @param outputParams
	 *            The map to which the parameters will be added.
	 * @param pathList
	 *            The path list till the last parameter. This is used to
	 *            evaluate the fully qualified path.
	 * @param jsonObjects
	 *            The array of primitive object for which the evaluation is to
	 *            be done.
	 * @throws JSONException
	 *             Exception, if the xml is not well formed.
	 * @throws EvaluationFailedException
	 *             Exception, if the parameter could not be identified.
	 */
	private static void fromXmlToArrayPrimitiveParam(
			final Map<String, CommandParam> outputParams,
			final List<String> pathList, final JSONObject jsonObjects)
			throws JSONException, EvaluationFailedException {
		pathList.add(String.valueOf(0));
		fromXmlToParameters(outputParams, pathList, jsonObjects);
		pathList.remove(pathList.size() - 1);
	}

	/**
	 * This method is used to create an instance of the FDPCommandParamOutput
	 * based on the variable.
	 * 
	 * @param outputVariable
	 *            The variable for which the instance is to be created.
	 * @return The object instance if the variable is valid, null otherwise.
	 * @throws JSONException
	 * @throws EvaluationFailedException
	 *             Exception, if evaluation fails.
	 */
	private static CommandParamOutput createOutputParameter(
			final String outputVariable, final JSONObject value)
			throws JSONException, EvaluationFailedException {
		CommandParamOutput fdpCommandParamOutput = null;
		for (final Primitives primitive : Primitives.values()) {
			if (outputVariable.contains(primitive.getValue())) {
				fdpCommandParamOutput = new CommandParamOutput();
				fdpCommandParamOutput.setPrimitiveValue(primitive);
				fdpCommandParamOutput.setType(CommandParameterType.PRIMITIVE);
				final Object valueObject = value.get(outputVariable);
				final Object valueFound = ClassUtil.getPrimitiveValue(
						valueObject, primitive.getClazz());
				if (valueFound == null) {
					fdpCommandParamOutput.setValue(primitive.getClazz().cast(
							valueObject));
				} else {
					fdpCommandParamOutput.setValue(valueFound);
				}
				break;
			}
		}
		return fdpCommandParamOutput;
	}

	/**
	 * This method is used to evaluate the command parameter value.
	 * 
	 * @param fdpRequest
	 *            The request object.
	 * @param fdpMetaDataCache
	 *            The cache object.
	 * @param fdpCommandParam
	 *            The parameter to evaluate.
	 * @return The parameter value.
	 * @throws EvaluationFailedException
	 *             Exception, if the parameter could not be evaluated.
	 */
	public static Object evaluateCommandParameter(final FDPRequest fdpRequest, final CommandParam fdpCommandParam) throws EvaluationFailedException {
		final String commandDisplayName = fdpCommandParam.getCommand().getCommandDisplayName();
		Boolean beneficiaryFlag = (null != fdpRequest.getAuxiliaryRequestParameter(AuxRequestParam.BENEFICIARY_FLAG)) ? (Boolean)fdpRequest.getAuxiliaryRequestParameter(AuxRequestParam.BENEFICIARY_FLAG) : false;
		FDPCommand executedCommand = (beneficiaryFlag) ? fdpRequest.getExecutedCommandForBeneficiary(commandDisplayName) : fdpRequest.getExecutedCommand(commandDisplayName);
		StringBuilder key = null;
		StringBuilder key2 = null;
		List<Object> idValues = new ArrayList<Object>();
		CommandParam outputParam = null;
		Object value = null;
		// Added by Shikha for GetAccountDetails command.
		String productName=null;
		final FDPCacheable product = fdpRequest.getValueFromRequest(RequestMetaValuesKey.PRODUCT);
		if (product instanceof Product) {
			Product fdpProduct = (Product) product;
			productName = fdpProduct.getProductType().getName();
		}
		if (null != executedCommand && ((executedCommand.getCommandDisplayName().contains(FDPConstant.GET_SERVICE_DTLS_REQUEST_COMMAND)) || (executedCommand.getCommandDisplayName().equalsIgnoreCase(Command.GETACCOUNTDETAILS.getCommandDisplayName()) && ProductType.FAF.getName().equalsIgnoreCase(productName)))) {
			executedCommand = null;
			FDPRequestImpl fdpRequestImpl = (FDPRequestImpl) fdpRequest;
			fdpRequestImpl.setExecutionStatus(ExecutionStatus.FORWARD);
		}
		try {
			if (executedCommand == null && ExecutionStatus.FORWARD.equals(fdpRequest.getExecutionStatus())) {
				final FDPCacheable cachedCommand = ApplicationConfigUtil.getMetaDataCache().getValue(
						new FDPMetaBag(fdpRequest.getCircle(), ModuleType.COMMAND, commandDisplayName));
				if (cachedCommand instanceof FDPCommand) {
					executedCommand = (FDPCommand) cachedCommand;
					executedCommand = CommandUtil.getExectuableFDPCommand(executedCommand);
				} else {
					throw new EvaluationFailedException("Could not evaluate value");
				}
				executedCommand.execute(fdpRequest);
				if(beneficiaryFlag){
					fdpRequest.addExecutedCommandForBeneficiary(executedCommand);
				}else{
					fdpRequest.addExecutedCommand(executedCommand);
				}
			}
		} catch (final ExecutionFailedException e) {
			throw new EvaluationFailedException("Could not evaluate value as command did not execute", e);
		}
		if (executedCommand != null && executedCommand.getOutputParams() != null) {
			outputParam = executedCommand.getOutputParam(fdpCommandParam.flattenParam().toLowerCase());			
 			if (Character.isDigit(fdpCommandParam.flattenParam().toLowerCase()
					.charAt(fdpCommandParam.flattenParam().toLowerCase().indexOf(FDPConstant.PARAMETER_SEPARATOR) + 1))) {
				if (Character.getNumericValue(fdpCommandParam.flattenParam().toLowerCase()
						.charAt(fdpCommandParam.flattenParam().toLowerCase().indexOf(FDPConstant.PARAMETER_SEPARATOR) + 1)) > 0) {
					String firstPart = fdpCommandParam.flattenParam().toLowerCase()
							.substring(0, fdpCommandParam.flattenParam().toLowerCase().indexOf(FDPConstant.PARAMETER_SEPARATOR) + 1);
					String lastPart = fdpCommandParam
							.flattenParam()
							.toLowerCase()
							.substring(fdpCommandParam.flattenParam().toLowerCase().lastIndexOf(FDPConstant.PARAMETER_SEPARATOR),
									fdpCommandParam.flattenParam().toLowerCase().length());
					String index = fdpCommandParam.flattenParam().toLowerCase().substring(firstPart.length(), firstPart.length() + 1);
					Map<String, CommandParam> mapValue = executedCommand.getOutputParams();
					@SuppressWarnings("unchecked")
					List<ExpressionCondition> validatedExpressions = (List<ExpressionCondition>) (beneficiaryFlag ? fdpRequest.getAuxiliaryRequestParameter(AuxRequestParam.VALIDATED_EXPRESSIONS_BENEFICIARY) : 
						fdpRequest.getAuxiliaryRequestParameter(AuxRequestParam.VALIDATED_EXPRESSIONS_REQUESTER));

					for (int i = 0; i < mapValue.keySet().size(); i++) {
						key = new StringBuilder();
						key.append(firstPart).append(i).append(lastPart);
						if (mapValue.containsKey(key.toString())) {
							key2 = new StringBuilder(firstPart + i);
							if (isValidForCombinedConstraints(validatedExpressions, mapValue, key2)) {
								idValues.add(executedCommand.getOutputParam(key.toString()).getValue());
							}
						} else {
							break;
						}
					}
				} else if (Character.getNumericValue(fdpCommandParam.flattenParam().toLowerCase()
						.charAt(fdpCommandParam.flattenParam().toLowerCase().indexOf(FDPConstant.PARAMETER_SEPARATOR) + 1)) == 0) {
			//		System.out.println("equals to zero");
					String firstPart = fdpCommandParam.flattenParam().toLowerCase()
							.substring(0, fdpCommandParam.flattenParam().toLowerCase().indexOf(FDPConstant.PARAMETER_SEPARATOR) + 1);
					String lastPart = fdpCommandParam
							.flattenParam()
							.toLowerCase()
							.substring(fdpCommandParam.flattenParam().toLowerCase().lastIndexOf(FDPConstant.PARAMETER_SEPARATOR),
									fdpCommandParam.flattenParam().toLowerCase().length());
					String index = fdpCommandParam.flattenParam().toLowerCase().substring(firstPart.length(), firstPart.length() + 1);
					Map<String, CommandParam> mapValue = executedCommand.getOutputParams();
					@SuppressWarnings("unchecked")
					List<ExpressionCondition> validatedExpressions = (List<ExpressionCondition>) (beneficiaryFlag ? fdpRequest.getAuxiliaryRequestParameter(AuxRequestParam.VALIDATED_EXPRESSIONS_BENEFICIARY) : 
						fdpRequest.getAuxiliaryRequestParameter(AuxRequestParam.VALIDATED_EXPRESSIONS_REQUESTER));

					for (int i = 0; i < mapValue.keySet().size(); i++) {
						key = new StringBuilder();
						key.append(firstPart).append(i).append(lastPart);
						if (mapValue.containsKey(key.toString())) {
							key2 = new StringBuilder(firstPart + i);
							if (lastPart.equalsIgnoreCase(".serviceOfferingID")) {
								final String tempLastPart = ".serviceOfferingActiveFlag";
								StringBuilder key1 = new StringBuilder();
								key1.append(firstPart).append(i).append(tempLastPart);
								Object leftOperandToCheck = executedCommand.getOutputParam(key1.toString()).getValue();
								if (leftOperandToCheck instanceof Boolean && !(Boolean) leftOperandToCheck) {
									continue;
								}
							}
							if (isValidForCombinedConstraints(validatedExpressions, mapValue, key2)) {
									idValues.add(executedCommand.getOutputParam(key.toString()).getValue());
							}
						}else if(checkAnyIndexKey(mapValue,fdpCommandParam)){
							key2=new StringBuilder(fdpCommandParam.flattenParam().toLowerCase());
							if (isValidForCombinedConstraints(validatedExpressions, mapValue, key2)) {
								if(executedCommand.getOutputParam(key.toString())!=null){
									idValues.add(executedCommand.getOutputParam(key.toString()).getValue());
								}
							}
						}
						else {
							break;
						}
					}
				}
				outputParam = null;
			}
			if(idValues.isEmpty()){
				idValues = AdvancedConditionParamHandlingUtil.evaluateCommandParameter(fdpRequest, executedCommand, fdpCommandParam);
			}
		}
		if (outputParam != null) {
			if (Primitives.DATETIME.equals(outputParam.getPrimitiveValue()) && outputParam.getValue() instanceof Calendar) {
				value = DateUtil.convertCalendarDateToString((Calendar) outputParam.getValue(), FDPConstant.FDP_DB_SAVE_DATE_PATTERN);
			} else {
				value = outputParam.getValue();
			}
		}
		if (!idValues.isEmpty()) {
			return idValues;
		} else {
			return value;
		}
	}

	private static boolean checkAnyIndexKey(Map<String, CommandParam> mapValue, CommandParam fdpCommandParam) {
		
		return mapValue.get(fdpCommandParam.flattenParam().toLowerCase())!=null?true:false;
	}

	/**
	 * 
	 * @param fdpRequest
	 * @return
	 */
	public static Boolean isBlackBerryUser(final FDPRequest fdpRequest) {

		try {

			FDPCommand getAccountDetail = fdpRequest
					.getExecutedCommand(Command.GETACCOUNTDETAILS
							.getCommandDisplayName());

			String serviceClassCurrent = (null != getAccountDetail) ? getAccountDetail
					.getOutputParam(FDPConstant.SERVICE_CLASS_CURRENT)
					.getValue().toString()
					: null;

			FDPCommand refillCommand = fdpRequest
					.getExecutedCommand(Command.REFILL.getCommandDisplayName());

			if (serviceClassCurrent != null && serviceClassCurrent.equals(serviceClassId)
					&& isOfferIDForBlackberryUser(refillCommand, offerId)
					&& isPSOBitForBlacberryUser(getAccountDetail, psoBit)) {
				return true;
			}
		} catch (ExecutionFailedException e) {
			e.printStackTrace();
		}
		return false;
	}

	/**
	 * This method get OfferID
	 * 
	 * @param executedCommand
	 * @return
	 */
	public static Boolean isOfferIDForBlackberryUser(
			final FDPCommand executedCommand, final String offerId) {

		String offerPath = "offerinformationlist" + FDPConstant.DOT;
		int offerArrCounter = 0;
		String pathkey = null;
		String offerID = "offerID";
		while (executedCommand.getOutputParam(pathkey = (offerPath
				+ FDPConstant.PARAMETER_SEPARATOR + offerArrCounter
				+ FDPConstant.PARAMETER_SEPARATOR + offerID)) != null) {
			final String offerIDCurrent = executedCommand
					.getOutputParam(pathkey).getValue().toString();
			if (offerIDCurrent.equals(offerId))
				return true;
			offerArrCounter++;
		}
		return false;

		/*
		 * String pathkey = null; int i = 0; final String paramterName =
		 * "offerInformation"; final String offerID = "offerID"; while
		 * (executedCommand.getOutputParam(pathkey = (paramterName +
		 * FDPConstant.PARAMETER_SEPARATOR + i + FDPConstant.PARAMETER_SEPARATOR
		 * + offerID)) != null) { final String offerIDCurrent =
		 * executedCommand.getOutputParam(pathkey).getValue().toString();
		 * if(offerIDCurrent.equals(offerId)) return true; i++; } return false;
		 */
	}

	/**
	 * This method fetch the all PSO for a subscriber.
	 * 
	 * @param fdpRequest
	 * @return
	 * @throws ExecutionFailedException
	 */
	public static Boolean isPSOBitForBlacberryUser(
			final FDPCommand executedCommand, final String psoBit)
			throws ExecutionFailedException {
		String pathkey = null;
		int i = 0;
		final String paramterName = "serviceOfferings";
		final String parameterId = "serviceOfferingID";
		while (executedCommand.getOutputParam(pathkey = (paramterName
				+ FDPConstant.PARAMETER_SEPARATOR + i
				+ FDPConstant.PARAMETER_SEPARATOR + parameterId)) != null) {
			final String pso = executedCommand.getOutputParam(pathkey)
					.getValue().toString();
			if (pso.equals(psoBit)) {
				return true;
			}
			i++;
		}
		return false;
	}
	
	/**
	 * This method validates the params for combined constraint criterion
	 * @param validatedExpressions
	 * @param outputParams
	 * @param key
	 * @return
	 */
	private static Boolean isValidForCombinedConstraints(List<ExpressionCondition> validatedExpressions, Map<String, CommandParam> outputParams, StringBuilder key){
		Boolean isValid = true;
		if (null != validatedExpressions) {
			try{
				AbstractCondition condition = null;
				for(ExpressionCondition ec : validatedExpressions){
					String lastPart1 = ec.getLeftOperand().substring(ec.getLeftOperand().lastIndexOf(FDPConstant.PARAMETER_SEPARATOR), ec.getLeftOperand().length());
					if(ec.getConditionType().equals(ConditionType.EQUALS)){
						condition = new EqualsCondition(new CommandParamInput(null, null));	
					} else if(ec.getConditionType().equals(ConditionType.LESSER_THAN)){
						condition = new LessThanCondition(new CommandParamInput(null, null));
					} else if(ec.getConditionType().equals(ConditionType.LESSER_THAN_OR_EQUALS)){
						condition = new LessThanEqualsCondition(new CommandParamInput(null, null));
					} else if(ec.getConditionType().equals(ConditionType.GREATER_THAN)){
						condition = new GreaterThanCondition(new CommandParamInput(null, null));
					} else if(ec.getConditionType().equals(ConditionType.GREATER_THAN_OR_EQUALS)){
						condition = new GreaterThanEqualsCondition(new CommandParamInput(null, null));
					}
					else if(ec.getConditionType().equals(ConditionType.NOT_EQUALS_TO)){
						condition = new NotEqualsCondition(new CommandParamInput(null, null));
					}
					
					condition.setCommandParameterDataType(ec.getCommandParamDataType());
					Object[] rightOperands = {ec.getRightHandOperandValue()};
					if(outputParams.containsKey(key + lastPart1)){
						isValid = condition.evaluateConditionValueForProvidedInput(outputParams.get(key + lastPart1).getValue(), rightOperands);
					}					
				}
			} catch(Exception ex){
				isValid = false;
			}
		}
		return isValid;
	}
	
	/**
	 * This method is used to evaluate the command parameter value.
	 * 
	 * @param fdpRequest
	 *            The request object.
	 * @param fdpCommandParam
	 *            The cache object.
	 * @param combinedExpression
	 *            The parameter to evaluate.
	 * @return The parameter value.
	 * @throws EvaluationFailedException
	 *             Exception, if the parameter could not be evaluated.
	 */
	public static Object evaluateCombinedCommandParameter(final FDPRequest fdpRequest, final CommandParam fdpCommandParam, final Expression combinedExpression) throws EvaluationFailedException{
		Object value = null;
		
		final String commandDisplayName = fdpCommandParam.getCommand().getCommandDisplayName();
		Boolean beneficiaryFlag = (null != fdpRequest.getAuxiliaryRequestParameter(AuxRequestParam.BENEFICIARY_FLAG)) ? (Boolean)fdpRequest.getAuxiliaryRequestParameter(AuxRequestParam.BENEFICIARY_FLAG) : false;
		FDPCommand executedCommand = (beneficiaryFlag) ? fdpRequest.getExecutedCommandForBeneficiary(commandDisplayName) : fdpRequest.getExecutedCommand(commandDisplayName);
				
		CommandParam outputParam = null;
		
		if (null != executedCommand && executedCommand.getCommandDisplayName().contains(FDPConstant.GET_SERVICE_DTLS_REQUEST_COMMAND)) {
			executedCommand = null;
			FDPRequestImpl fdpRequestImpl = (FDPRequestImpl) fdpRequest;
			fdpRequestImpl.setExecutionStatus(ExecutionStatus.FORWARD);
		}
		try {
			if (executedCommand == null && ExecutionStatus.FORWARD.equals(fdpRequest.getExecutionStatus())) {
				final FDPCacheable cachedCommand = ApplicationConfigUtil.getMetaDataCache().getValue(
						new FDPMetaBag(fdpRequest.getCircle(), ModuleType.COMMAND, commandDisplayName));
				if (cachedCommand instanceof FDPCommand) {
					executedCommand = (FDPCommand) cachedCommand;
					executedCommand = CommandUtil.getExectuableFDPCommand(executedCommand);
				} else {
					throw new EvaluationFailedException("Could not evaluate value");
				}
				executedCommand.execute(fdpRequest);
				if(beneficiaryFlag){
					fdpRequest.addExecutedCommandForBeneficiary(executedCommand);
				}else{
					fdpRequest.addExecutedCommand(executedCommand);
				}
			}
			
			if (executedCommand != null) {
				outputParam = executedCommand.getOutputParam(fdpCommandParam.flattenParam().toLowerCase());
				//coding from here
				String inputParam = fdpCommandParam.flattenParam().toLowerCase();
				@SuppressWarnings({ "unchecked", "rawtypes" })
				Map<String, CommandParam> mapValue = new LinkedHashMap(executedCommand.getOutputParams());
				//Map<String, CommandParam> mapValue = executedCommand.getOutputParams();
				if(inputParam.contains(FDPConstant.PARAMETER_SEPARATOR))
				{
					inputParam = inputParam.replaceAll("\\d", "");
					
					Set<String> keySet = mapValue.keySet();
					Iterator<String> it = keySet.iterator();
					while(it.hasNext())
					{
						String key = it.next();
						if(key.contains(FDPConstant.PARAMETER_SEPARATOR))
						{
							String temp = key.replaceAll("\\d", "").toLowerCase();
							if(!temp.equalsIgnoreCase(inputParam))
							{
								//mapValue.remove(key);
								it.remove();
							}							
						}
						else
						{
							it.remove();
						}
					}
					/*for(String key :keySet)
					{
						if(key.contains(FDPConstant.PARAMETER_SEPARATOR))
						{
							String temp = key.replaceAll("\\d", "").toLowerCase();
							if(!temp.equalsIgnoreCase(inputParam))
							{
								mapValue.remove(key);
							}							
						}
					}*/
				}
				
				
				//till here
				
				/*if (outputParam != null) {
					if (Primitives.DATETIME.equals(outputParam.getPrimitiveValue()) && outputParam.getValue() instanceof Calendar) {
						value = DateUtil.convertCalendarDateToString((Calendar) outputParam.getValue(), FDPConstant.FDP_DB_SAVE_DATE_PATTERN);
					} else {
						value = outputParam.getValue();
					}
				}*/
				if(combinedExpression instanceof FDPCombinedAIRExpressionOperator)
				{
					FDPCombinedAIRExpressionOperator fdpCombinedExpression = (FDPCombinedAIRExpressionOperator) combinedExpression;	
					FDPExpressionCondition fdpExpressionCondition = (FDPExpressionCondition) fdpCombinedExpression.getLeftHandExpression();	
					
					//coding from here
					boolean expResult = false;
					String matchedKey = null;
					for(Entry<String,CommandParam> entry : mapValue.entrySet())
					{
						outputParam = entry.getValue();
						if (outputParam != null) {
							if (Primitives.DATETIME.equals(outputParam.getPrimitiveValue()) && outputParam.getValue() instanceof Calendar) {
								value = DateUtil.convertCalendarDateToString((Calendar) outputParam.getValue(), FDPConstant.FDP_DB_SAVE_DATE_PATTERN);
							} else {
								value = outputParam.getValue();
							}
						}
						expResult = fdpExpressionCondition.getFdpCondition().evaluate(value, fdpRequest, false);
						if(expResult)
						{
							matchedKey = entry.getKey();
							break;
						}
					}
					//end here
					//check for id condition match 
				//	boolean expResult = fdpExpressionCondition.getFdpCondition().evaluate(value, fdpRequest, false);
					//once id is matched will check for value 
					if(expResult && matchedKey!=null)
					{
						fdpExpressionCondition = (FDPExpressionCondition) fdpCombinedExpression.getRightHandExpression();
						CommandParam rightCommandParam = ((FDPCommandLeftOperand)fdpExpressionCondition.getLeftOperand()).getFdpCommandParamOutput();						
						outputParam = executedCommand.getOutputParam(rightCommandParam.flattenParam().toLowerCase());
						String tempString = matchedKey.toUpperCase();
						if(tempString.endsWith("ID"))
						{
							int lastIndex = tempString.lastIndexOf("ID");
							matchedKey = matchedKey.substring(0, lastIndex);
							matchedKey = matchedKey+"Value";
							outputParam = executedCommand.getOutputParam(matchedKey.toLowerCase());
							
						}
						if (outputParam != null) {
							if (Primitives.DATETIME.equals(outputParam.getPrimitiveValue()) && outputParam.getValue() instanceof Calendar) {
								value = DateUtil.convertCalendarDateToString((Calendar) outputParam.getValue(), FDPConstant.FDP_DB_SAVE_DATE_PATTERN);
							} else {
								value = outputParam.getValue();
							}
						}
						
						expResult = fdpExpressionCondition.getFdpCondition().evaluate(value, fdpRequest, false);
						return expResult;
					}
				}
				
			}
		} catch (final ExecutionFailedException | ConditionFailedException e) {
			throw new EvaluationFailedException("Could not evaluate value as command did not execute", e);
		}
		return false;
	}
}
