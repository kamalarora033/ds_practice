package com.ericsson.fdp.business.util;

import java.io.StringReader;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.XML;

import com.ericsson.fdp.business.command.param.impl.CommandParamOutput;
import com.ericsson.fdp.business.constants.FDPCommandConstants;
import com.ericsson.fdp.business.enums.ErrorTypes;
import com.ericsson.fdp.business.mcoupons.CMSPerformCouponAction;
import com.ericsson.fdp.business.mcoupons.CmsCoupon;
import com.ericsson.fdp.business.mcoupons.CmsCouponListResponse;
import com.ericsson.fdp.business.vo.CommandExecutionStatus;
import com.ericsson.fdp.common.constants.FDPConstant;
import com.ericsson.fdp.common.enums.Primitives;
import com.ericsson.fdp.common.enums.Status;
import com.ericsson.fdp.core.command.FDPCommand;
import com.ericsson.fdp.core.command.param.CommandParam;
import com.ericsson.fdp.core.dsm.framework.service.FDPCache;
import com.ericsson.fdp.core.exception.EvaluationFailedException;
import com.ericsson.fdp.core.exception.ExecutionFailedException;
import com.ericsson.fdp.core.serviceprovisioning.bean.FDPAppBag;
import com.ericsson.fdp.core.utils.ApplicationConfigUtil;
import com.ericsson.fdp.dao.dto.FDPResultCodesDTO;

/**
 * This is a utility class that deals with CMS commands.
 * 
 * @author eahmaim
 * 
 */
public class CMSCommandUtil {

	/**
	 * Instantiates a new CMS command util.
	 */
	private CMSCommandUtil() {

	}
	private static JAXBContext jaxbContext1 = null;
	private static JAXBContext jaxbContext2 = null;
	static{
		try {
			jaxbContext1 = JAXBContext.newInstance(CMSPerformCouponAction.class);
			jaxbContext2 = JAXBContext.newInstance(CmsCouponListResponse.class);
		} catch (JAXBException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}	
	/**
	 * This method is used to create string representation of xml for an CMS
	 * command. The supported version is 5.0 for ACIP commands. The
	 * implementation will be changed to support a different version.
	 * 
	 * @param command
	 *            The command for which the xml representation is to be created.
	 * @return The string representation of the command.
	 * @throws ExecutionFailedException
	 *             Exception, if the xml could not be created.
	 */
	public static String toCMSXmlFormat(final FDPCommand command) throws ExecutionFailedException {
		final StringBuilder xmlFormat = new StringBuilder();
		xmlFormat.append("<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?>").append("\n")
		.append("<request>").append("\n")
		.append("<methodtype>").append(command.getCommandName()).append("</methodtype>").append("\n")
		.append("<params>").append("\n")
				.append(CGWCommandUtil.toCGWParametersXmlFormat(command.getInputParam()))
		.append("</params>").append("\n")
			.append("</request>");
		return xmlFormat.toString();
	}
	
	/**
	 * This method is used to create a map of fully qualified path of the
	 * parameter and its value from the string representation of a xml for RS
	 * parameters.
	 * 
	 * @param outputParam
	 *            The string representation which is to be converted to the map.
	 * 
	 * @return The map containing fully qualified path and the value.
	 * @throws EvaluationFailedException
	 *             Exception, if in creating the map.
	 * @throws JAXBException 
	 */
	public static Map<String, CommandParam> fromCMSXmlToParameters(final String outputParam)
			throws EvaluationFailedException {
		final Map<String, CommandParam> outputParams = new HashMap<String, CommandParam>();
		StringReader sr = new StringReader(outputParam);
		final CommandParamOutput commandParamOutput = new CommandParamOutput();
		final CommandParamOutput responseCodeParam = new CommandParamOutput();
			try {
				if (outputParam.contains("<cmscoupon>")) {
					Unmarshaller jaxbUnmarshaller = jaxbContext1.createUnmarshaller();
					CMSPerformCouponAction OCMSPerformCouponAction=(CMSPerformCouponAction)jaxbUnmarshaller.unmarshal(sr);
					commandParamOutput.setValue(OCMSPerformCouponAction);
					outputParams.put("USER_MCOUPON_CONSUME".toLowerCase(), commandParamOutput);
					
					responseCodeParam.setValue(OCMSPerformCouponAction.getResponseCode());
					outputParams.put(FDPCommandConstants.CMS_RESPONSE_CODE_PATH.toLowerCase(), responseCodeParam);
				}
				//JAXB is used because for the value of parameter clientTrancactionID, JSON is treating the string as octal and converting it into 
				//decimal equivalent. ex- 00000011 is getting populated as 9.
				if (outputParam.contains("<cmscouponResponse>")) {
					Unmarshaller jaxbUnmarshaller = jaxbContext2.createUnmarshaller();  
					CmsCouponListResponse oCmsCouponListResponse=(CmsCouponListResponse)jaxbUnmarshaller.unmarshal(sr);
					CmsCoupon[] oCmsCoupon=  oCmsCouponListResponse.getCmsCoupon();
					
					commandParamOutput.setValue(oCmsCoupon);
					outputParams.put("USER_MCOUPON_LIST".toLowerCase(), commandParamOutput);
					responseCodeParam.setValue(oCmsCouponListResponse.getResponseCode());
					outputParams.put(FDPCommandConstants.CMS_RESPONSE_CODE_PATH.toLowerCase(), responseCodeParam);
					}
				} catch (final  JAXBException e) {
				throw new EvaluationFailedException("The input xml is not valid.", e);
			}
			return outputParams;
		}
	
	
		
	
	/**
	 * This method is used to check if the CMS command executed successfully or
	 * failed.
	 * 
	 * @param fdpCommand
	 *            The command to be checked.
	 * @return True, if command executed successfully, false otherwise.
	 * @throws ExecutionFailedException
	 *             Exception, if the status could not be evaluated.
	 */
	public static CommandExecutionStatus checkForCMSCommandStatus(final FDPCommand fdpCommand)
			throws ExecutionFailedException {
		final CommandExecutionStatus commandExecutionStatus = new CommandExecutionStatus(Status.FAILURE, 0,
				FDPConstant.ERROR_CODE_MAPPING_MISSING, ErrorTypes.RESPONSE_CODE.name(),
				com.ericsson.fdp.common.enums.ExternalSystem.valueOf(fdpCommand.getSystem().name()));
		final FDPCache<FDPAppBag, Object> configCache = ApplicationConfigUtil.getApplicationConfigCache();
		FDPResultCodesDTO isFailure = null;
		final CommandParam responseCodeParam = fdpCommand.getOutputParam(FDPCommandConstants.CMS_RESPONSE_CODE_PATH);
		System.out.println("Reponse code Param : "+responseCodeParam);
		if (responseCodeParam != null) {
			isFailure = CommandUtil.checkResponseCode(configCache, responseCodeParam.getValue(),
					fdpCommand.getCommandDisplayName());
			commandExecutionStatus.setCode((Integer) responseCodeParam.getValue());
			if (isFailure != null) {
				commandExecutionStatus.setDescription(isFailure.getResultCodeDesc());
			}
		}
		commandExecutionStatus.setStatus(CommandUtil.getStatus(isFailure));
		return commandExecutionStatus;
	}

}
