package com.ericsson.fdp.business.enums.ivr.commandservice;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.ericsson.fdp.business.enums.Command;
import com.ericsson.fdp.business.enums.ivr.FulfillmentParameters;

/**
 * The Enum IVRCommandEnum.
 * 
 * @author Ericsson
 */
public enum IVRCommandEnum {

	/** The gad. */
	GAD("GAD", Command.GETACCOUNTDETAILS,"methodResponse",null),

	/** The gbad */
	GBAD("GBAD", Command.GET_BALANCE_AND_DATE,"methodResponse",null),
	
	/** The gsdr */
	GSDR("GSDR", Command.GET_SERVICES_DETAILS_REQUEST,"GetServicesDtlsResponse",null),
	
	/** The udpate offer */
	UPDATE_OFFER("UPDATE_OFFER",Command.UPDATE_OFFER,"methodResponse",Arrays.asList(FulfillmentParameters.OFFER_ID,FulfillmentParameters.OFFER_TYPE,FulfillmentParameters.EXPIRY_DATE)),
	
	/** The delete offer */
	DELETE_OFFER("DELETE_OFFER",Command.DELETEOFFER,"methodResponse",Arrays.asList(FulfillmentParameters.OFFER_ID)),
	
	/**TO GET PRODUCT SUBSCRIBED:OFFER */
	VIEWHISTORY("VIEW_HISTORY",Command.VIEWHISTORY,"methodResponse",null),
	
	
	/** The udpate offer */
	SBB_UPDATE_OFFER("SBB_UPDATE_OFFER",Command.SBB_UPDATE_OFFER,"methodResponse",Arrays.asList(FulfillmentParameters.OFFER_ID, 
			FulfillmentParameters.OFFER_TYPE,FulfillmentParameters.EXPIRY_DATE)),
	
	/** The delete offer */
	SBB_DELETE_OFFER("SBB_DELETE_OFFER",Command.SBB_DELETE_OFFER,"methodResponse",Arrays.asList(FulfillmentParameters.OFFER_ID)),
	
	/** To Update Usage Threshold and Counter value. */
	SBB_UPDATE_UCUT("SBB_UPDATE_UCUT", Command.SBB_UPDATE_UCUT, "methodResponse", Arrays.asList(FulfillmentParameters.THRESHOLD_ID, 
			FulfillmentParameters.COUNTER_ID, FulfillmentParameters.THRESHOLD_VALUE, FulfillmentParameters.COUNTER_VALUE)),
	
	/** To Delete Usage Threshold and Counter value. */
	SBB_DELETE_UCUT("SBB_DELETE_UCUT", Command.SBB_DELETE_UCUT, "methodResponse", Arrays.asList(FulfillmentParameters.THRESHOLD_ID, 
			FulfillmentParameters.COUNTER_ID, FulfillmentParameters.THRESHOLD_VALUE, FulfillmentParameters.COUNTER_VALUE));
	
	/** The ivr name. */
	private String ivrName;

	/** The command. */
	private Command command;
	
	/** The responseXmlTagName **/
	private String responseXmlTagName;
	
	private List<FulfillmentParameters> parameterList ;
	
	/** The commandMap **/
	private static Map<String,IVRCommandEnum> commandMap;

	/**
	 * Instantiates a new iVR command enum.
	 * 
	 * @param ivrName
	 *            the ivr name
	 * @param command
	 *            the command
	 */
	private IVRCommandEnum(final String ivrName, final Command command, final String responseXmlTagName, final List<FulfillmentParameters> parameterList) {
		this.ivrName = ivrName;
		this.command = command;
		this.responseXmlTagName = responseXmlTagName;
		this.parameterList = parameterList;
	}

	/**
	 * Gets the iVR command enum.
	 * 
	 * @param ivrName
	 *            the ivr name
	 * @return the iVR command enum
	 */
	public static IVRCommandEnum getIVRCommandEnum(final String ivrName) {
		if(null == commandMap) {
			polpulateCommandsIntoCommandMap();
		}
		return commandMap.get(ivrName);
	}

	/**
	 * Gets the ivr name.
	 * 
	 * @return the ivr name
	 */
	public String getIvrName() {
		return ivrName;
	}

	/**
	 * Gets the command.
	 * 
	 * @return the command
	 */
	public Command getCommand() {
		return command;
	}

	/**
	 * @return the responseXmlTagName
	 */
	public String getResponseXmlTagName() {
		return responseXmlTagName;
	}
	
	/**
	 * This method populates the Parameters list.
	 */
	private static void polpulateCommandsIntoCommandMap() {
		commandMap = new HashMap<String, IVRCommandEnum>();
		for (final IVRCommandEnum val : values()) {
			commandMap.put(val.getIvrName(), val);
		}
	}

	/**
	 * @return the parameterList
	 */
	public List<FulfillmentParameters> getParameterList() {
		return parameterList;
	}
}
