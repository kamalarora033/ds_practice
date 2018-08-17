package com.ericsson.fdp.business.util;

import java.io.StringReader;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ericsson.fdp.FDPCacheable;
import com.ericsson.fdp.business.command.param.impl.CommandParamOutput;
import com.ericsson.fdp.business.enums.AuxRequestParam;
import com.ericsson.fdp.business.enums.ErrorTypes;
import com.ericsson.fdp.business.mcarbon.response.MCarbonLoanResponse;
import com.ericsson.fdp.business.vo.CommandExecutionStatus;
import com.ericsson.fdp.common.enums.Status;
import com.ericsson.fdp.core.cache.AppCacheSubStore;
import com.ericsson.fdp.core.command.FDPCommand;
import com.ericsson.fdp.core.command.param.CommandParam;
import com.ericsson.fdp.core.entity.ExternalSystemDetail;
import com.ericsson.fdp.core.exception.EvaluationFailedException;
import com.ericsson.fdp.core.exception.ExecutionFailedException;
import com.ericsson.fdp.core.request.FDPRequest;
import com.ericsson.fdp.core.serviceprovisioning.bean.FDPAppBag;
import com.ericsson.fdp.core.utils.ApplicationConfigUtil;
import com.ericsson.fdp.core.utils.MCarbonDetailUtil;


/**
 * The Class MCLoan CommandUtil.
 * 
 * @author evivbeh
 */
public final class MCLoanCommandUtil {

	/** The Constant KEY_VALUE_SEPERATOR. */
	private static final String KEY_VALUE_SEPERATOR = "=";

	/** The Constant PARAMETER_SEPERATOR. */
	private static final String PARAMETER_SEPERATOR = "&";

	/** The Constant RESULT_CODE_VALUE. */
	private static final String RESULT_CODE_VALUE = "RESULT_CODE_VALUE";

	/** The Constant LOGGER. */
	private static final Logger LOGGER = LoggerFactory.getLogger(MCLoanCommandUtil.class);

	/**
	 * Instantiates a new m carbon command util.
	 */
	private MCLoanCommandUtil() {

	}
	private static JAXBContext jaxbContext = null;
	static{
		try {
			jaxbContext = JAXBContext.newInstance(MCarbonLoanResponse.class);
		} catch (JAXBException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
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
	public static String toMCarbonFormat(final FDPCommand command) throws ExecutionFailedException {
		final StringBuilder mCarbonFormat = new StringBuilder();
		for (final CommandParam commandParam : command.getInputParam()) {
			mCarbonFormat.append(getParamString(commandParam)).append(PARAMETER_SEPERATOR);
		}
		return mCarbonFormat.length() > PARAMETER_SEPERATOR.length() ? mCarbonFormat.substring(0,
				mCarbonFormat.length() - PARAMETER_SEPERATOR.length()) : null;
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
		final StringBuilder mCarbonFormat = new StringBuilder();
		switch (commandParam.getType()) {
		case PRIMITIVE:
			mCarbonFormat.append(commandParam.getName()).append(KEY_VALUE_SEPERATOR)
					.append(commandParam.getValue().toString());
			break;
		default:
			throw new ExecutionFailedException("The command parameter type is not recognized. It is of type "
					+ commandParam.getType());
		}
		return mCarbonFormat.toString();
	}

	/**
	 * Gets the external system.
	 * 
	 * @param fdpRequest
	 *            the fdp request
	 * @return the external system
	 * @throws ExecutionFailedException
	 *             the execution failed exception
	 */
	public static ExternalSystemDetail getExternalSystem(final FDPRequest fdpRequest) throws ExecutionFailedException {
		ExternalSystemDetail externalSystemDetail = null;
		final String logicalName = (String) fdpRequest
				.getAuxiliaryRequestParameter(AuxRequestParam.EXTERNAL_SYSTEM_LOGICAL_NAME);
		LOGGER.debug("AuxRequestParam.EXTERNAL_SYSTEM_LOGICAL_NAME value in request is {} for requestId {}.",
				logicalName, fdpRequest.getRequestId());
		final FDPAppBag key = new FDPAppBag(AppCacheSubStore.MCLOAN_DETAILS, MCarbonDetailUtil.getMCLoanEndPoint(fdpRequest
				.getCircle().getCircleCode(), logicalName));
		LOGGER.debug("MCLoan cache key is {} for requestId {}.", key, fdpRequest.getRequestId());
		final FDPCacheable cacheable = (FDPCacheable) ApplicationConfigUtil.getApplicationConfigCache().getValue(key);
		if (cacheable instanceof ExternalSystemDetail) {
			externalSystemDetail = (ExternalSystemDetail) cacheable;
		}
		return externalSystemDetail;
	}

	/**
	 * Check for m carbon command status.
	 * 
	 * @param fdpCommand
	 *            the fdp command
	 * @return the command execution status
	 */
	public static CommandExecutionStatus checkForMCarbonCommandStatus(final FDPCommand fdpCommand) {
		return new CommandExecutionStatus(Status.SUCCESS, 0, "SUCCESS", ErrorTypes.RESPONSE_CODE.name(),
				com.ericsson.fdp.common.enums.ExternalSystem.MCARBON);
	}

	/**
	 * From m carbon to parameters.
	 * 
	 * @param outputXmlAsString
	 *            the output xml as string
	 * @return the map
	 */
	public static Map<String, CommandParam> fromMCarbonToParameters(final String outputXmlAsString)
			throws EvaluationFailedException {
		final Map<String, CommandParam> outputParams = new LinkedHashMap<String, CommandParam>();
		StringReader sr = new StringReader(outputXmlAsString);
		final CommandParamOutput commandParamOutput = new CommandParamOutput();
	//	final CommandParamOutput responseCodeParam = new CommandParamOutput();
		try {
			Unmarshaller jaxbUnmarshaller = jaxbContext.createUnmarshaller();
			MCarbonLoanResponse mcLoanResponse=(MCarbonLoanResponse)jaxbUnmarshaller.unmarshal(sr);
			commandParamOutput.setValue(mcLoanResponse);
			outputParams.put("MCLOAN".toLowerCase(), commandParamOutput);
			
		/*	responseCodeParam.setValue(mcLoanResponse..getResponseCode());
			outputParams.put(FDPCommandConstants.CMS_RESPONSE_CODE_PATH.toLowerCase(), responseCodeParam);*/
		}catch (final  JAXBException e) {
			throw new EvaluationFailedException("The input xml is not valid.", e);
		}
	
		return outputParams;
	}
	
		
}
