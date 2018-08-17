package com.ericsson.fdp.business.util;

import java.io.StringReader;
import java.util.LinkedHashMap;
import java.util.Map;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ericsson.fdp.business.codResponse.CodResponse;
import com.ericsson.fdp.business.command.param.impl.CommandParamOutput;
import com.ericsson.fdp.business.enums.ErrorTypes;
import com.ericsson.fdp.business.vo.CommandExecutionStatus;
import com.ericsson.fdp.common.enums.Status;
import com.ericsson.fdp.core.command.FDPCommand;
import com.ericsson.fdp.core.command.param.CommandParam;
import com.ericsson.fdp.core.exception.EvaluationFailedException;
import com.ericsson.fdp.core.exception.ExecutionFailedException;


/**
 * The Class FDPOffLine CommandUtil.
 * 
 * @author eahmaim
 */
public class FDPOffLineCommandUtil {
	/** The Constant KEY_VALUE_SEPERATOR. */
	private static final String KEY_VALUE_SEPERATOR = "=";

	/** The Constant PARAMETER_SEPERATOR. */
	private static final String PARAMETER_SEPERATOR = "&";

	/** The Constant RESULT_CODE_VALUE. */
	private static final String RESULT_CODE_VALUE = "RESULT_CODE_VALUE";

	/** The Constant LOGGER. */
	private static final Logger LOGGER = LoggerFactory.getLogger(MCLoanCommandUtil.class);

	/**
	 * Instantiates a new FDP OFFLINE command util.
	 */
	private FDPOffLineCommandUtil() {

	}
	private static JAXBContext jaxbContext = null;
	static{
		try {
			jaxbContext = JAXBContext.newInstance(CodResponse.class);
		} catch (JAXBException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	/**
	 * To COD Request format.
	 * 
	 * @param command
	 *            the command
	 * @return the string
	 * @throws ExecutionFailedException
	 *             the execution failed exception
	 */
	public static String toFDPOffLineFormat(final FDPCommand command) throws ExecutionFailedException {
		final StringBuilder xmlFormat = new StringBuilder(512);
		xmlFormat.append("<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?>").append("\n")
		.append("<").append(command.getCommandName()).append(">").append("\n")
		.append(CGWCommandUtil.toCGWParametersXmlFormat(command.getInputParam()))
		.append("\n")
		.append("</").append(command.getCommandName()).append(">");
		return xmlFormat.toString();
	}


	/**
	 * Check for fdpOffLine command status.
	 * 
	 * @param fdpCommand
	 *            the fdp command
	 * @return the command execution status
	 */
	public static CommandExecutionStatus checkForFDPOffLineCommandStatus(final FDPCommand fdpCommand) {
		return new CommandExecutionStatus(Status.SUCCESS, 0, "SUCCESS", ErrorTypes.RESPONSE_CODE.name(),
				com.ericsson.fdp.common.enums.ExternalSystem.FDPOFFLINE);
	}

	/**
	 * From FDP OFFLine format  to parameters.
	 * 
	 * @param outputXmlAsString
	 *            the output xml as string
	 * @return the map
	 */
	public static Map<String, CommandParam> fromFDPOffLineToParameters(final String outputXmlAsString)
			throws EvaluationFailedException {
		final Map<String, CommandParam> outputParams = new LinkedHashMap<String, CommandParam>();
		StringReader sr = new StringReader(outputXmlAsString);
		final CommandParamOutput commandParamOutput = new CommandParamOutput();
		try {
			Unmarshaller jaxbUnmarshaller = jaxbContext.createUnmarshaller();
			CodResponse oCodResponse=(CodResponse)jaxbUnmarshaller.unmarshal(sr);
			commandParamOutput.setValue(oCodResponse);
			outputParams.put("FDPOFFLINE".toLowerCase(), commandParamOutput);
		
		}catch (final  JAXBException e) {
			throw new EvaluationFailedException("The input xml is not valid.", e);
		}
	
		return outputParams;
	}
	
		
}
