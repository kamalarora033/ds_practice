package com.ericsson.fdp.business.util;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import com.ericsson.fdp.core.command.FDPCommand;
import com.ericsson.fdp.core.command.param.CommandParam;
import com.ericsson.fdp.core.request.FDPRequest;

/**
 * @author ESIASAN
 *
 */
public class AdvancedConditionParamHandlingUtil {
	
	private static final String ATTRIBUTE_NAME_KEY = "attributeName";
	private static final String ATTRIBUTE_NAME_KEY_LOWERCASE = "attributename";
	private static final String ATTRIBUTE_VALUE = "segment";
	private static final String ATTRIBUTE_VALUE2 = "segmentid";
	private static final String ATTRIBUTE_VALUE_STRING_KEY = "attributeValueSTRING";
	private static final String ATTRIBUTE_VALUE_STRING_KEY_LOWERCASE = "attributevaluestring";

	/**
	 * This method returns the values for attributeValueSTRING param
	 * 
	 * @param fdpRequest
	 * @param executedCommand
	 * @param paramToEval
	 * @return
	 */
	public static List<Object> evaluateCommandParameter(final FDPRequest fdpRequest, FDPCommand executedCommand, final CommandParam paramToEval){
		List<Object> values = new ArrayList<Object>();	
		if(paramToEval.flattenParam().contains(ATTRIBUTE_NAME_KEY )||paramToEval.flattenParam().contains(ATTRIBUTE_VALUE_STRING_KEY )){
			ArrayList<String> params = (null != paramToEval && null != paramToEval.flattenParam()) ? 
					new ArrayList<String>(Arrays.asList(paramToEval.flattenParam().toLowerCase().replaceAll("\\d", "").split("\\.", 0))) : new ArrayList<String>();
			params.removeAll(Arrays.asList("", null));
			Map<String, CommandParam> mapValue = executedCommand.getOutputParams();
			for(int i = 0; i < mapValue.keySet().size(); i++){
				StringBuilder key = new StringBuilder();
				// key = offerInformation.0.attributeInformationList
				key.append(params.get(0) + "." + i + "." + params.get(1));
				for(int j = 0; ; j++){
					if(paramToEval.flattenParam().contains(ATTRIBUTE_NAME_KEY ) && mapValue.containsKey(key + "." + j + "." + ATTRIBUTE_NAME_KEY_LOWERCASE)){
						values.add(executedCommand.getOutputParam(key + "." + j + "." + ATTRIBUTE_NAME_KEY_LOWERCASE).getValue());
					}
					// key = offerInformation.0.attributeInformationList.0.attributeValueString
					else if(mapValue.containsKey(key + "." + j + "." + ATTRIBUTE_VALUE_STRING_KEY_LOWERCASE) && mapValue.containsKey(key + "." + j + "." + ATTRIBUTE_NAME_KEY_LOWERCASE)){
						if(executedCommand.getOutputParam(key + "." + j + "." + ATTRIBUTE_NAME_KEY_LOWERCASE).getValue().toString().equalsIgnoreCase(ATTRIBUTE_VALUE)
								|| executedCommand.getOutputParam(key + "." + j + "." + ATTRIBUTE_NAME_KEY).getValue().toString().equalsIgnoreCase(ATTRIBUTE_VALUE2)){
							values.add(executedCommand.getOutputParam(key + "." + j + "." + ATTRIBUTE_VALUE_STRING_KEY_LOWERCASE).getValue());
							continue;
						}
					}
					break;					
				}
			}
		}
		return values;
	}
}
	