package com.ericsson.fdp.business.util;

import java.io.Serializable;
import java.text.DateFormat;
import java.text.DecimalFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.Map;
import java.util.TimeZone;

import org.mvel2.MVEL;
import org.mvel2.ParserContext;
import org.mvel2.ast.ASTNode;
import org.mvel2.ast.BinaryOperation;
import org.mvel2.compiler.ExecutableAccessor;

import com.ericsson.fdp.FDPCacheable;
import com.ericsson.fdp.business.command.AbstractCommand;
import com.ericsson.fdp.business.enums.AuxRequestParam;
import com.ericsson.fdp.business.enums.ConfigurationKey;
import com.ericsson.fdp.common.constants.FDPConstant;
import com.ericsson.fdp.common.enums.FDPCSAttributeParam;
import com.ericsson.fdp.common.enums.FDPCSAttributeValue;
import com.ericsson.fdp.common.enums.ModuleType;
import com.ericsson.fdp.common.enums.Primitives;
import com.ericsson.fdp.common.enums.Status;
import com.ericsson.fdp.common.util.DateUtil;
import com.ericsson.fdp.common.util.StringUtil;
import com.ericsson.fdp.common.vo.FDPCircle;
import com.ericsson.fdp.core.cache.AppCacheSubStore;
import com.ericsson.fdp.core.command.FDPCommand;
import com.ericsson.fdp.core.exception.EvaluationFailedException;
import com.ericsson.fdp.core.exception.ExecutionFailedException;
import com.ericsson.fdp.core.request.FDPRequest;
import com.ericsson.fdp.core.request.impl.FDPRequestImpl;
import com.ericsson.fdp.core.serviceprovisioning.bean.FDPAppBag;
import com.ericsson.fdp.core.serviceprovisioning.bean.FDPMetaBag;
import com.ericsson.fdp.core.utils.ApplicationConfigUtil;

/**
 * This class is the Utility for MVEL Type Feed Param Evaluation.
 * 
 * @author eshanty
 *
 */
public class MVELUtil {
	
	final static String COMMAND_GET_UC_UT ="GetUsageThresholdsAndCounters";
	final static String COMMAND_GBAD ="GetBalanceAndDate";
	final static String APPLICABLE_CHARGING = "applicableCharging";
	final static String COMMAND_GETUA ="GetAccumulators";
	
	/**
	 * This method will evaluate the MVEL expression.
	 * @param fdpRequest
	 * @param expression
	 * @return
	 * @throws ExecutionFailedException 
	 */
	public static Object evaluateMvelExpression(final FDPRequest fdpRequest, String expression, final Primitives primitives) throws EvaluationFailedException, ExecutionFailedException {
		Object evalOutPut = null;
		ParserContext context = new ParserContext();
		if(expression.contains(APPLICABLE_CHARGING)){
			expression = expression.replace(APPLICABLE_CHARGING, ChargingUtil.getApplicableProductChargingForProvisioning(fdpRequest).toString());
		}
		Object outPut = null;
		final Serializable compiledExpression = MVEL.compileExpression(expression, context);
		//System.out.println("Expression:"+compiledExpression);
		final Map<String,Object> inputValuesMap = new HashMap<String,Object>();
		final Map<String,Object> userCommandOutPuts = new HashMap<String,Object>();
		if(!StringUtil.isNullOrEmpty(expression)) {
			switch (primitives) {
			case BOOLEAN:
			case INTEGER:
			case LONG:
				evalOutPut = evalExpression(fdpRequest, primitives, context,
						outPut, compiledExpression, inputValuesMap,
						userCommandOutPuts);
				break;
			case DATETIME:
				evalOutPut = evalDateTimeType(fdpRequest, primitives, context,
						outPut,compiledExpression,inputValuesMap, userCommandOutPuts);
				break;
			case STRING:
				evalOutPut = evalExpression(fdpRequest, Primitives.STRING, context,
						outPut, compiledExpression, inputValuesMap,
						userCommandOutPuts);
			default:
				break;
			}
		}
		
		updateMvelInputs(fdpRequest, expression, evalOutPut.toString());
		return evalOutPut;
	}

	
	/**
	 * @param fdpRequest
	 * @param primitives
	 * @param userCommandOutPuts
	 * @param entry
	 * @param outPut
	 * @param inputValueArray
	 * @return
	 * @throws ExecutionFailedException
	 */
	@SuppressWarnings("rawtypes")
	private static Object evaluateParam(final FDPRequest fdpRequest,
			final Primitives primitives,
			final Map<String, Object> userCommandOutPuts,
			Map.Entry<String, Class> entry, Object outPut,
			String[] inputValueArray) throws ExecutionFailedException {
		final FDPCSAttributeValue attributeValue = FDPCSAttributeValue.getValue(inputValueArray[0].toUpperCase());
		final String attributeId = inputValueArray[1];
		final FDPCSAttributeParam  attributeParam = FDPCSAttributeParam.getFDPCSAttributeParamForName(inputValueArray[2]);
		if(null != attributeValue && null != attributeId && null != attributeParam) {
			userCommandOutPuts.putAll(executeCommand(attributeValue, fdpRequest));
			switch (attributeParam) {
			case NAME:
				outPut = getName(fdpRequest, attributeValue, attributeId);
				break;
			case VALUE:
				outPut = getValue(fdpRequest, userCommandOutPuts, primitives, entry.getKey());
				break;
			case EXPIRY:
				outPut = getExpiry(fdpRequest, userCommandOutPuts, entry.getKey().toUpperCase());
				break;
			default:
				break;
			}
		}
		return outPut;
	}

	/**
	 * This method will return the defaul value.
	 * 
	 * @param primitives
	 * @return
	 */
	private static Object getMvelDefaulValue(final Primitives primitives) {
		Object defaultValue = null;
		switch (primitives) {
		case BOOLEAN:
			defaultValue = Boolean.FALSE;
			break;
		case DATETIME:
			//defaultValue = FDPConstant.EMPTY_STRING;
			final Calendar calendar = Calendar.getInstance();
			calendar.set(Calendar.HOUR_OF_DAY, 0);
			calendar.set(Calendar.MINUTE, 0);
			calendar.set(Calendar.SECOND, 0);
			defaultValue = calendar;
			break;
		case INTEGER:
			defaultValue = 0;
			break;
		case LONG:
			defaultValue = 0L;
			break;
		case STRING:
			defaultValue = FDPConstant.EMPTY_STRING;
			break;
		default:
			defaultValue = FDPConstant.EMPTY_STRING;
			break;
		}
		return defaultValue;
	}

	/**
	 * Calcualte the name.
	 * 
	 * @param fdpRequest
	 * @param attributeValue
	 * @param attributeId
	 * @return
	 * @throws ExecutionFailedException
	 */
	private static String getName(final FDPRequest fdpRequest, final FDPCSAttributeValue attributeValue, final String attributeId) throws ExecutionFailedException {
		String name = null;
		FDPCircle fdpCircleWithCSAttr = (FDPCircle) ApplicationConfigUtil.getApplicationConfigCache().getValue(
				new FDPAppBag(AppCacheSubStore.CS_ATTRIBUTES, fdpRequest.getCircle().getCircleCode()));
		Map<String, Map<String, String>> csAttrMap = fdpCircleWithCSAttr.getCsAttributesKeyValueMap();
		final Map<String,String> attributeNameMap = csAttrMap.get(attributeValue.getCsAttributeKey());
		name = (null != attributeNameMap) ? attributeNameMap.get(attributeId) : name;
		return null == name ? FDPConstant.EMPTY_STRING : name;
	}
	
	/**
	 * Calculates the Value.
	 * 
	 * @param fdpRequest
	 * @param userInputMap
	 * @param primitives
	 * @param valueKey
	 * @return
	 */
	private static Object getValue(final FDPRequest fdpRequest, final Map<String,Object> userInputMap, final Primitives primitives, final String valueKey) {
		Object value = null;
		switch (primitives) {
		case STRING:
		case BOOLEAN:
		case DATETIME:
			value = userInputMap.get(valueKey);
			break;
		case LONG:
			value = (null != userInputMap.get(valueKey)) ? Long.valueOf(userInputMap.get(valueKey).toString()) : value;
			break;
		case INTEGER:
			value = (null != userInputMap.get(valueKey)) ? Integer.parseInt(userInputMap.get(valueKey).toString()) : value;
			break;
		default:
			break;
		}
		return null == value ? getMvelDefaulValue(primitives) : value;
	}
	
	/**
	 * Gets the Expiry.
	 * 
	 * @param fdpRequest
	 * @param userCommandInputMap
	 * @param valueKey
	 * @return
	 */
	private static String getExpiry(final FDPRequest fdpRequest, final Map<String,Object> userCommandInputMap, final String valueKey) {
		//String expiry = null;
		GregorianCalendar calenderObject =(GregorianCalendar) userCommandInputMap.get(valueKey);
		/*if(null != calenderObject) {
			expiry = DateUtil.convertCalendarDateToString(calenderObject, RequestUtil.getValidityFormat(fdpRequest));
		}*/
		//return null == expiry ? "Not Applicable" : expiry;
		return DateUtil.convertCalendarDateToString((null == calenderObject) ? DateUtil.getStartDate() : calenderObject, RequestUtil.getValidityFormat(fdpRequest));
	}
	
	/**
	 * Executes the command.
	 * 
	 * @param attributeValue
	 * @param fdpRequest
	 * @return
	 * @throws ExecutionFailedException
	 */
	private static Map<String,Object> executeCommand(final FDPCSAttributeValue attributeValue, final FDPRequest fdpRequest) throws ExecutionFailedException {
		Map<String,Object> commandOutPut = null;
		if(null == fdpRequest.getExecutedCommand(attributeValue.getCommandName())) {
			 executeCachedCommand(fdpRequest, attributeValue.getCommandName());
		}
		switch (attributeValue.getCommandName()) {
		case COMMAND_GET_UC_UT:
			commandOutPut= evaluateUCUTDetailsForUser(fdpRequest, fdpRequest.getExecutedCommand(attributeValue.getCommandName()));
			break;
		case COMMAND_GBAD:
			commandOutPut = evaluateDetailsForUser(fdpRequest.getExecutedCommand(attributeValue.getCommandName()));
			break;
		case COMMAND_GETUA:
			commandOutPut = evaluateUAValues(fdpRequest.getExecutedCommand(attributeValue.getCommandName()));
			break;
		default:
			break;
		}
	
		
		return null == commandOutPut ? new HashMap<String,Object>() : commandOutPut;
	}
	
	/**
	 * This method fetch the command.
	 * 
	 * @param fdpRequest
	 * @param commandName
	 * @throws ExecutionFailedException
	 */
	public static boolean executeCachedCommand(final FDPRequest fdpRequest, final String commandName)  throws ExecutionFailedException{
		FDPCommand fdpCommand = null;
		boolean isSuccess = false;
		final FDPCacheable fdpCommandCached = ApplicationConfigUtil.getMetaDataCache().getValue(
				new FDPMetaBag(fdpRequest.getCircle(), ModuleType.COMMAND, commandName));
		if(null != fdpCommandCached && fdpCommandCached instanceof AbstractCommand) {
			fdpCommand = (AbstractCommand) fdpCommandCached;
			if (Status.SUCCESS.equals(fdpCommand.execute(fdpRequest))) {
				fdpRequest.addExecutedCommand(fdpCommand);
				isSuccess = true;
			} else {
				throw new ExecutionFailedException("Failed to execute command:"+commandName);
			}
		} else {
			throw new ExecutionFailedException("Command not found in cache"+ commandName);
		}
		return isSuccess;
	}
	
	/**
	 * This method calculate the DA related values from Comamnd.
	 * 
	 * @param executedCommand
	 * @return
	 */
	public static Map<String,Object> evaluateDetailsForUser(final FDPCommand executedCommand) {
        final Map<String,Object> responseMap = new HashMap<String, Object>();
        String pathkey = null;
        int i = 0;
        final String paramterName = "dedicatedAccountInformation";
        final String dedicatedAccountId  = "dedicatedAccountId";
        final String dedicatedAccountValue1 = "dedicatedAccountValue1";
        final String expiryDate = "expiryDate";
        String defaultExpiryDate = getDefaultExpiryDate();
        
        while (executedCommand.getOutputParam(pathkey = (paramterName + FDPConstant.PARAMETER_SEPARATOR
                + i + FDPConstant.PARAMETER_SEPARATOR +dedicatedAccountId)) != null) {
            final String dedicatedAccountValue1_Value = paramterName + FDPConstant.PARAMETER_SEPARATOR
                    + i + FDPConstant.PARAMETER_SEPARATOR + dedicatedAccountValue1;
            final String expiryDate_Value = paramterName + FDPConstant.PARAMETER_SEPARATOR
                    + i + FDPConstant.PARAMETER_SEPARATOR + expiryDate;
            
            final String userDaId = executedCommand.getOutputParam(pathkey).getValue().toString();
            final String userDaValue = executedCommand.getOutputParam(dedicatedAccountValue1_Value).getValue().toString();
            final Object userDaExpiry = (null != executedCommand.getOutputParam(expiryDate_Value)) ? executedCommand.getOutputParam(expiryDate_Value).getValue():
            	defaultExpiryDate;            
            responseMap.put(FDPCSAttributeValue.DA.name()+FDPConstant.UNDERSCORE+userDaId+FDPConstant.UNDERSCORE+FDPCSAttributeParam.VALUE.name(), userDaValue);
            responseMap.put(FDPCSAttributeValue.DA.name()+FDPConstant.UNDERSCORE+userDaId+FDPConstant.UNDERSCORE+FDPCSAttributeParam.EXPIRY.name(), userDaExpiry);
            i++;
        }
        evaluateOffersDetails(executedCommand, responseMap);
        //System.out.println("EvaluateDADetailsForUserr:responseMap=============>>"+responseMap);
        return responseMap;
    }
	
	/**
	 * This method fetch the UC UT From command.
	 * 
	 * @param executedCommand
	 * @return
	 */
	public static Map<String,Object> evaluateUCUTDetailsForUser(final FDPRequest fdpRequest, final FDPCommand executedCommand) {
        final Map<String,Object> responseMap = new HashMap<String, Object>();
        String pathkey = null;
        int i = 0;
        final String paramterName = "usageCounterUsageThresholdInformation";
        final String usageCounterID  = "usageCounterID";
        final String usageCounterValue = "usageCounterValue";
        final String usageThresholdInformation = "usageThresholdInformation";
        final String usageThresholdID  = "usageThresholdID";
    	final String usageThresholdValue = "usageThresholdValue";
    	final String usageCounterMonetaryValue1 = "usageCounterMonetaryValue1";
    	final String usageThresholdMonetaryValue1  = "usageThresholdMonetaryValue1";
    	 
        		
        while (executedCommand.getOutputParam(pathkey = (paramterName + FDPConstant.PARAMETER_SEPARATOR
                + i + FDPConstant.PARAMETER_SEPARATOR +usageCounterID)) != null) {
        	//System.out.println("shank========>>"+paramterName + FDPConstant.PARAMETER_SEPARATOR+ i + FDPConstant.PARAMETER_SEPARATOR +usageCounterID);
        	//System.out.println("pathkey:=============>"+pathkey);
        	
            final String usageCounterValue_Value = paramterName + FDPConstant.PARAMETER_SEPARATOR
                    + i + FDPConstant.PARAMETER_SEPARATOR + usageCounterValue;
            //System.out.println("usageCounterValue_Value===========>>"+usageCounterValue_Value);
            
            final String usageCounterMonetaryValue1_Value = paramterName + FDPConstant.PARAMETER_SEPARATOR
                    + i + FDPConstant.PARAMETER_SEPARATOR + usageCounterMonetaryValue1;
            //System.out.println("usageCounterMonetaryValue1_Value===========>>"+usageCounterMonetaryValue1_Value);
            
            final String userUCId = executedCommand.getOutputParam(pathkey).getValue().toString();//(null!=executedCommand.getOutputParam(usageCounterValue_Value).getValue().toString() ?executedCommand.getOutputParam(usageCounterValue_Value).getValue().toString():"") ;
            		
            		
            		//executedCommand.getOutputParam(pathkey).getValue().toString();
            if(null!=executedCommand.getOutputParam(usageCounterValue_Value)){
            	final String userUCValue = executedCommand.getOutputParam(usageCounterValue_Value).getValue().toString();
            	responseMap.put(FDPCSAttributeValue.UC.name()+FDPConstant.UNDERSCORE+userUCId+FDPConstant.UNDERSCORE+FDPCSAttributeParam.VALUE.name(), userUCValue);
            }else{
            	final String userUCValue = executedCommand.getOutputParam(usageCounterMonetaryValue1_Value).getValue().toString();
                responseMap.put(FDPCSAttributeValue.UC.name()+FDPConstant.UNDERSCORE+userUCId+FDPConstant.UNDERSCORE+FDPCSAttributeParam.VALUE.name(), userUCValue);
            }
            
            int j=0;
            while(executedCommand.getOutputParam(pathkey = (paramterName + FDPConstant.PARAMETER_SEPARATOR
                    + i + FDPConstant.PARAMETER_SEPARATOR + usageThresholdInformation +FDPConstant.PARAMETER_SEPARATOR + j + FDPConstant.PARAMETER_SEPARATOR + usageThresholdID)) != null) {
            	final String userUTId = executedCommand.getOutputParam(pathkey).getValue().toString();
            	if(null!=executedCommand.getOutputParam(paramterName + FDPConstant.PARAMETER_SEPARATOR
                        + i + FDPConstant.PARAMETER_SEPARATOR + usageThresholdInformation +FDPConstant.PARAMETER_SEPARATOR + j + FDPConstant.PARAMETER_SEPARATOR + usageThresholdValue)){
            		final String userUTValue = executedCommand.getOutputParam(paramterName + FDPConstant.PARAMETER_SEPARATOR
                            + i + FDPConstant.PARAMETER_SEPARATOR + usageThresholdInformation +FDPConstant.PARAMETER_SEPARATOR + j + FDPConstant.PARAMETER_SEPARATOR + usageThresholdValue).getValue().toString();
                    responseMap.put(FDPCSAttributeValue.UT.name()+FDPConstant.UNDERSCORE+userUTId+FDPConstant.UNDERSCORE+FDPCSAttributeParam.VALUE.name(), userUTValue);	
            	}else{
            		final String userUTValue = executedCommand.getOutputParam(paramterName + FDPConstant.PARAMETER_SEPARATOR
                            + i + FDPConstant.PARAMETER_SEPARATOR + usageThresholdInformation +FDPConstant.PARAMETER_SEPARATOR + j + FDPConstant.PARAMETER_SEPARATOR + usageThresholdMonetaryValue1).getValue().toString();
                    responseMap.put(FDPCSAttributeValue.UT.name()+FDPConstant.UNDERSCORE+userUTId+FDPConstant.UNDERSCORE+FDPCSAttributeParam.VALUE.name(), userUTValue);
            	}
            	
            	
                
                j++;
            }
            i++;
        }
        updateResponseMapForExpiry(fdpRequest, responseMap);
        evaluateUADetails(executedCommand, responseMap);
        //System.out.println("EvaluateUCUTDetailsForUser:responseMap=============>>"+responseMap);
        return responseMap;
    }
	
	/**
	 * This method will update the UC/UT map with expired UC/UT value if they are present
	 * @param fdpRequest
	 * @param responseMap
	 */
	@SuppressWarnings("unchecked")
	private static void updateResponseMapForExpiry(FDPRequest fdpRequest, Map<String, Object> responseMap) {
		Object expiredUCUTMapObj = ((FDPRequestImpl)fdpRequest).getAuxiliaryRequestParameter(AuxRequestParam.EXPIRED_UC_UT_MAP);
		if(null != expiredUCUTMapObj && expiredUCUTMapObj instanceof Map){
			Map<String, Map<String, Long>> expiredUCUTMap = (Map<String, Map<String, Long>>)expiredUCUTMapObj;
			for (Map.Entry<String, Map<String, Long>> entry : expiredUCUTMap.entrySet()) {
				if(entry.getKey().equalsIgnoreCase(FDPCSAttributeValue.UC.name())){
					for (Map.Entry<String, Long> param : entry.getValue().entrySet()) {
			            responseMap.put(FDPCSAttributeValue.UC.name()+FDPConstant.UNDERSCORE+param.getKey()+FDPConstant.UNDERSCORE+FDPCSAttributeParam.VALUE.name(), param.getValue());
					}
				}else if(entry.getKey().equalsIgnoreCase(FDPCSAttributeValue.UT.name())){
					for (Map.Entry<String, Long> param : entry.getValue().entrySet()) {
			            responseMap.put(FDPCSAttributeValue.UT.name()+FDPConstant.UNDERSCORE+param.getKey()+FDPConstant.UNDERSCORE+FDPCSAttributeParam.VALUE.name(), param.getValue());
					}
				}
			}
		}
	}

	/**
	 * This method will update the MVEL inputs in request.
	 * 
	 * @param fdpRequest
	 * @param key
	 * @param value
	 */
	@SuppressWarnings("unchecked")
	private static void updateMvelInputs(final FDPRequest fdpRequest, final String key, final String value) {
		Map<String,String> mvelInputMap = (Map<String, String>) fdpRequest.getAuxiliaryRequestParameter(AuxRequestParam.MVEL_INPUT);
		if(null == mvelInputMap) {
			mvelInputMap = new HashMap<String,String>();
		}
		mvelInputMap.put(key, value);
		if(fdpRequest instanceof FDPRequestImpl) {
			final FDPRequestImpl fdpRequestImpl = (FDPRequestImpl) fdpRequest;
			fdpRequestImpl.putAuxiliaryRequestParameter(AuxRequestParam.MVEL_INPUT, mvelInputMap);
		}
	}
	
	/**
	 * This method will parse all offers details from response.
	 * 
	 * @param fdpCommand
	 * @param map
	 */
	public static void evaluateOffersDetails(final FDPCommand executedCommand, final Map<String,Object> responseMap) {
		String pathkey = null;
        int i = 0;
        final String paramterName = "offerInformationList";
        final String offerId  = "offerID";
        final String startDate1 = "startDate";
        final String startDate2 = "startDateTime";
        final String expiryDate1 = "expiryDate";
        final String expiryDate2 = "expiryDateTime";

        while (executedCommand.getOutputParam(pathkey = (paramterName + FDPConstant.PARAMETER_SEPARATOR
                + i + FDPConstant.PARAMETER_SEPARATOR +offerId)) != null) {
        	final String startDate_value1 = paramterName+ FDPConstant.PARAMETER_SEPARATOR
                    + i + FDPConstant.PARAMETER_SEPARATOR + startDate1;
            final String startDate_value2 = paramterName+ FDPConstant.PARAMETER_SEPARATOR
                    + i + FDPConstant.PARAMETER_SEPARATOR + startDate2;
            final String expiryDate_Value1 = paramterName + FDPConstant.PARAMETER_SEPARATOR
                    + i + FDPConstant.PARAMETER_SEPARATOR + expiryDate1;
            final String expiryDate_Value2 = paramterName + FDPConstant.PARAMETER_SEPARATOR
                    + i + FDPConstant.PARAMETER_SEPARATOR + expiryDate2;
            
            final String userOfferId = executedCommand.getOutputParam(pathkey).getValue().toString();
            final Object userStartTime = (null != executedCommand.getOutputParam(startDate_value1)) ? executedCommand.getOutputParam(startDate_value1).getValue():
            	executedCommand.getOutputParam(startDate_value2).getValue();
            final Object userOfferExpiry = (null != executedCommand.getOutputParam(expiryDate_Value1)) ? executedCommand.getOutputParam(expiryDate_Value1).getValue():
            	executedCommand.getOutputParam(expiryDate_Value2).getValue();
            
            responseMap.put(FDPCSAttributeValue.OFFER.name()+FDPConstant.UNDERSCORE+userOfferId+FDPConstant.UNDERSCORE+FDPCSAttributeParam.NAME.name(), userOfferId);
            responseMap.put(FDPCSAttributeValue.OFFER.name()+FDPConstant.UNDERSCORE+userOfferId+FDPConstant.UNDERSCORE+FDPCSAttributeParam.EXPIRY.name(), userOfferExpiry);
            responseMap.put(FDPCSAttributeValue.OFFER.name()+FDPConstant.UNDERSCORE+userOfferId+FDPConstant.UNDERSCORE+FDPCSAttributeParam.STARTDATE.name(), userStartTime);
            
            i++;
        }
	}
	
	/**
	 * This method gets all UA details.
	 * 
	 * @param executedCommand
	 * @param responseMap
	 */
	private static void evaluateUADetails(final FDPCommand executedCommand, final Map<String,Object> responseMap) {
		String pathkey = null;
        int i = 0;
        final String paramterName = "counterInformation";
        final String counterId  = "counterID";
        final String counterValue = "totalCounterValue";
        
        while (executedCommand.getOutputParam(pathkey = (paramterName + FDPConstant.PARAMETER_SEPARATOR
                + i + FDPConstant.PARAMETER_SEPARATOR +counterId)) != null) {
            final String counterID_Value = paramterName + FDPConstant.PARAMETER_SEPARATOR
                    + i + FDPConstant.PARAMETER_SEPARATOR + counterValue;
            
            final String userCounterId = executedCommand.getOutputParam(pathkey).getValue().toString();
            final Object userConterValue = executedCommand.getOutputParam(counterID_Value).getValue();
            responseMap.put(FDPCSAttributeValue.UA.name()+FDPConstant.UNDERSCORE+userCounterId+FDPConstant.UNDERSCORE+FDPCSAttributeParam.NAME.name(), userCounterId);
            responseMap.put(FDPCSAttributeValue.UA.name()+FDPConstant.UNDERSCORE+userCounterId+FDPConstant.UNDERSCORE+FDPCSAttributeParam.VALUE.name(), userConterValue);
            i++;
        }
	}
	
	/**
	 * This method will return a map of all offer(IDs) of a subscriber along with boolean indicating whether it is expired or not
	 * 
	 * @param fdpCommand
	 * @param map
	 */
/*	public static Map<String, Boolean> getSubscribedOffers(final FDPCommand executedCommand) {
		String pathkey = null;
        int i = 0;
        final String paramterName = "offerInformation";
        final String offerId  = "offerID";
        final String expiryDate = "expiryDate";
        final Map<String, Boolean> offersMap = new HashMap<String, Boolean>();

        while (executedCommand.getOutputParam(pathkey = (paramterName + FDPConstant.PARAMETER_SEPARATOR
                + i + FDPConstant.PARAMETER_SEPARATOR +offerId)) != null) {
            final String expiryDate_Value = paramterName + FDPConstant.PARAMETER_SEPARATOR
                    + i + FDPConstant.PARAMETER_SEPARATOR + expiryDate;
            
            final String userOfferId = executedCommand.getOutputParam(pathkey).getValue().toString();
            final Object userOfferExpiry = executedCommand.getOutputParam(expiryDate_Value).getValue();
            offersMap.put(userOfferId, ((GregorianCalendar) userOfferExpiry)
					.compareTo(new GregorianCalendar()) < 0 ? true : false);
            i++;
        }
        return offersMap;
	}*/
	
	public static Map<String, Boolean> getSubscribedOffers(final FDPCommand executedCommand) {
		String pathkey = null;
        int i = 0;
        final String paramterName = "offerInformation";
        final String offerId  = "offerID";
        final String expiryDate1 = "expiryDate";
        final String expiryDate2 = "expiryDateTime";
        final Map<String, Boolean> offersMap = new HashMap<String, Boolean>();

        while (executedCommand.getOutputParam(pathkey = (paramterName + FDPConstant.PARAMETER_SEPARATOR
                + i + FDPConstant.PARAMETER_SEPARATOR +offerId)) != null) {
            final String expiryDate1_Value = paramterName + FDPConstant.PARAMETER_SEPARATOR
                    + i + FDPConstant.PARAMETER_SEPARATOR + expiryDate1;
            final String expiryDate2_Value = paramterName + FDPConstant.PARAMETER_SEPARATOR
                    + i + FDPConstant.PARAMETER_SEPARATOR + expiryDate2;
            
            final String userOfferId = executedCommand.getOutputParam(pathkey).getValue().toString();
            final Object userOfferExpiry = (null != executedCommand.getOutputParam(expiryDate1_Value)) ? executedCommand.getOutputParam(expiryDate1_Value).getValue():
            	executedCommand.getOutputParam(expiryDate2_Value).getValue();            
            offersMap.put(userOfferId, ((GregorianCalendar) userOfferExpiry)
					.compareTo(new GregorianCalendar()) < 0 ? true : false);
            i++;
        }
        return offersMap;
	}
	
	public static Map<String, Object> getSubscribedOffersNew(final FDPCommand executedCommand) {
		String pathkey = null;
        int i = 0;
        final String paramterName = "offerInformation";
        final String offerId  = "offerID";
        final String expiryDate1 = "expiryDate";
        final String expiryDate2 = "expiryDateTime";
        final Map<String, Object> offersMap = new HashMap<String, Object>();

        while (executedCommand.getOutputParam(pathkey = (paramterName + FDPConstant.PARAMETER_SEPARATOR
                + i + FDPConstant.PARAMETER_SEPARATOR +offerId)) != null) {
            final String expiryDate1_Value = paramterName + FDPConstant.PARAMETER_SEPARATOR
                    + i + FDPConstant.PARAMETER_SEPARATOR + expiryDate1;
            final String expiryDate2_Value = paramterName + FDPConstant.PARAMETER_SEPARATOR
                    + i + FDPConstant.PARAMETER_SEPARATOR + expiryDate2;
            
            final String userOfferId = executedCommand.getOutputParam(pathkey).getValue().toString();
            final Object userOfferExpiry = (null != executedCommand.getOutputParam(expiryDate1_Value)) ? executedCommand.getOutputParam(expiryDate1_Value).getValue():
            	executedCommand.getOutputParam(expiryDate2_Value).getValue();    
            offersMap.put(userOfferId, userOfferExpiry);
            i++;
        }
        return offersMap;
	}
	
	public static String evaluateAccumulatorFromResponse(final FDPCommand executedCommand) {
        String pathkey = null;
        int i = 0;
        final String paramterName = "accumulatorInformation";
        final String accumulatorValue  = "accumulatorValue";
        final String accumulatorID = "accumulatorID";
        while (executedCommand.getOutputParam(pathkey = (paramterName + FDPConstant.PARAMETER_SEPARATOR
                     + i + FDPConstant.PARAMETER_SEPARATOR + accumulatorID)) != null) {
               String accumulatorValue_V = paramterName + FDPConstant.PARAMETER_SEPARATOR
                            + i + FDPConstant.PARAMETER_SEPARATOR + accumulatorValue;
               final String accumulatorID_Value = executedCommand.getOutputParam(pathkey).getValue().toString();
               if(accumulatorID_Value.equals("30")){
            	   return executedCommand.getOutputParam(accumulatorValue_V).getValue().toString();
               }
              // responseMap.put(accumulatorID_Value, accumulatorValue_Value);
               i++;
        }
        return null;
    }
	
	/**
	 * This method return end of the century date to be used as default expiry for DA
	 */
	public static String getDefaultExpiryDate(){
		DateFormat df = new SimpleDateFormat("yyyyMMdd'T'HH:mm'Z'"); // Quoted "Z" to indicate UTC, no timezone offset
        df.setTimeZone(TimeZone.getTimeZone("UTC"));
        Calendar cal = Calendar.getInstance();
        cal.set(Calendar.YEAR, 2998);
        cal.set(Calendar.MONTH, 12);
        cal.set(Calendar.DAY_OF_MONTH, 31);
        return df.format(cal.getTime());
	}
	
	/**
	 * This method will return map of UA ID and UA Value
	 * 
	 * @param executedCommand
	 */
	public static Map<String, String> getUsageAccumulatorsResponse(final FDPCommand executedCommand){
		String pathkey = null;
        int i = 0;
        final String paramterName = "accumulatorInformation";
        final String accumulatorIDStr = "accumulatorID";
        final String accumulatorValueStr  = "accumulatorValue";
        final Map<String, String> accumulatorsMap = new HashMap<String, String>();

        while (executedCommand.getOutputParam(pathkey = (paramterName + FDPConstant.PARAMETER_SEPARATOR
                     + i + FDPConstant.PARAMETER_SEPARATOR + accumulatorIDStr)) != null) {
               String accumulatorValue_V = paramterName + FDPConstant.PARAMETER_SEPARATOR
                            + i + FDPConstant.PARAMETER_SEPARATOR + accumulatorValueStr;
               final String accumulatorID = executedCommand.getOutputParam(pathkey).getValue().toString();
               final String accumulatorValue = executedCommand.getOutputParam(accumulatorValue_V).getValue().toString();
               accumulatorsMap.put(accumulatorID, accumulatorValue);
               i++;
        }
        return accumulatorsMap;
	}
	
	/**
	 * This method gets all UA details.
	 * 
	 * @param executedCommand
	 * @param responseMap
	 * @return 
	 */
	private static Map<String, Object> evaluateUAValues(final FDPCommand executedCommand) {
		String pathkey = null;
        int i = 0;
        final String paramterName = "accumulatorInformation";
        final String counterId  = "accumulatorID";
        final String counterValue = "accumulatorValue";
        final Map<String,Object> responseMap = new HashMap<String,Object> ();
        while (executedCommand.getOutputParam(pathkey = (paramterName + FDPConstant.PARAMETER_SEPARATOR
                + i + FDPConstant.PARAMETER_SEPARATOR +counterId)) != null) {
            final String counterID_Value = paramterName + FDPConstant.PARAMETER_SEPARATOR
                    + i + FDPConstant.PARAMETER_SEPARATOR + counterValue;
            
            final String userCounterId = executedCommand.getOutputParam(pathkey).getValue().toString();
            final Object userConterValue = executedCommand.getOutputParam(counterID_Value).getValue();
            responseMap.put(FDPCSAttributeValue.UA.name()+FDPConstant.UNDERSCORE+userCounterId+FDPConstant.UNDERSCORE+FDPCSAttributeParam.NAME.name(), userCounterId);
            responseMap.put(FDPCSAttributeValue.UA.name()+FDPConstant.UNDERSCORE+userCounterId+FDPConstant.UNDERSCORE+FDPCSAttributeParam.VALUE.name(), userConterValue);
            i++;
        }
        return responseMap;
	}	
	

	@SuppressWarnings("rawtypes")
	private static Object evalExpression(final FDPRequest fdpRequest,
			final Primitives primitives, ParserContext context, Object outPut,
			final Serializable compiledExpression,
			final Map<String, Object> inputValuesMap,
			final Map<String, Object> userCommandOutPuts)
			throws ExecutionFailedException {
		for(Map.Entry<String,Class> entry : context.getInputs().entrySet()) {
			
			String[] inputValueArray = null;
			if(null != entry.getKey() && entry.getKey().contains(FDPConstant.UNDERSCORE)) {
				inputValueArray = entry.getKey().split(FDPConstant.UNDERSCORE);
				if(null != inputValueArray && inputValueArray.length==3) {
					outPut = evaluateParam(fdpRequest, primitives,
							userCommandOutPuts, entry, outPut,
							inputValueArray);
				}
			}
			
			inputValuesMap.put(entry.getKey(), (outPut.toString().isEmpty()) ? 0 : outPut);
		}
		
		Object mvelVal =  MVEL.executeExpression(compiledExpression, inputValuesMap, primitives.getClazz());
		try{
			if(outPut.toString().isEmpty()){
				return FDPConstant.EMPTY_STRING;
				
			}
			String decimalFormatBalEnquiry = RequestUtil.getConfigurationKeyValue(fdpRequest, ConfigurationKey.DECIMAL_FORMAT_BALANCE_ENQUIRY);
			DecimalFormat decimalFormat;
			if(null == decimalFormatBalEnquiry || decimalFormatBalEnquiry.isEmpty()){
			decimalFormat = new DecimalFormat("#.##"); 
			return decimalFormat.format(Double.parseDouble(mvelVal.toString())); 
			}
			decimalFormat = new DecimalFormat(decimalFormatBalEnquiry); 
			return decimalFormat.format(Double.parseDouble(mvelVal.toString())); 
			
			
			
		}catch(Exception e){
			return mvelVal;
		}
		
	}
	
	/** This Method will evaluate MVEL Date Expression **/
	@SuppressWarnings("rawtypes")
	private static String evalDateTimeType(final FDPRequest fdpRequest, final Primitives primitives,
			ParserContext context, Object outPut, final Serializable compiledExpression,
			final Map<String, Object> inputValuesMap, final Map<String, Object> userCommandOutPuts)
			throws ExecutionFailedException {
		String evalOutPut = null;
		Calendar calendar = null;
		String output = null;
		Integer literalValue = 0;
		for (Map.Entry<String, Class> entry : context.getInputs().entrySet()) {
			//System.out.println("Entry Key:" + entry.getKey());
			if (null != entry.getKey() && entry.getKey().contains(FDPConstant.UNDERSCORE)) {
				String[] inputValueArray = entry.getKey().split(FDPConstant.UNDERSCORE);
				if (null != inputValueArray && inputValueArray.length == 3) {
					output = (String) evaluateParam(fdpRequest, primitives, userCommandOutPuts, entry, outPut,
							inputValueArray);
					//System.out.println("EvalOutput:" + output);

					try {
						calendar = DateUtil.convertStringToCalendarDate(output, FDPConstant.FDP_DB_SAVE_DATE_PATTERN);
					} catch (ParseException e) {
						//System.out.println("Error Occured while converting Date");
						e.printStackTrace();
					}
				}
			}
			inputValuesMap.put(entry.getKey(), (null == outPut) ? FDPConstant.EMPTY_STRING : calendar);
		}
		//System.out.println("Checking Expression");
		if (compiledExpression instanceof ExecutableAccessor) {
			ExecutableAccessor ea = (ExecutableAccessor) compiledExpression;
			ASTNode node = ea.getNode();
			if (node instanceof BinaryOperation) {
				BinaryOperation binaryNode = (BinaryOperation) node;

				ASTNode leftNode = binaryNode.getLeft();
				ASTNode rightNode = binaryNode.getRight();
				literalValue = (Integer) rightNode.getLiteralValue();
				//System.out.println("Literal Value:" + literalValue);

			}

			if (literalValue > 0) {
				calendar.add(Calendar.DAY_OF_MONTH, literalValue);
			}

		}

		evalOutPut = DateUtil.convertCalendarDateToString(((Calendar) calendar), FDPConstant.FDP_DB_SAVE_DATE_PATTERN);
		((FDPRequestImpl) fdpRequest).putAuxiliaryRequestParameter(AuxRequestParam.CUMULATIVE_OFFER_EXPIRY, evalOutPut);
		//System.out.println(" Output Expiry Date is:" + evalOutPut);
		return evalOutPut;
	}
	
}