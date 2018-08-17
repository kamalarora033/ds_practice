package com.ericsson.fdp.business.util;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.json.JSONException;
import org.json.JSONObject;
import org.json.XML;

import com.ericsson.fdp.core.command.param.CommandParam;
import com.ericsson.fdp.core.exception.EvaluationFailedException;
import com.ericsson.fdp.core.exception.ExecutionFailedException;

/**
 * This is a utility class that deals with AIR Traffic commands.
 * 
 * @author Ericsson
 * 
 */
public class AIRCommandUtil {

	/**
	 * Instantiates a new aIR command util.
	 */
	private AIRCommandUtil() {

	}

	/**
	 * This method is used to create a map of fully qualified path of the
	 * parameter and its value from the string representation of a xml for Air
	 * parameters.
	 * 
	 * @param outputParam
	 *            The string representation which is to be converted to the map.
	 * 
	 * @return The map containing fully qualified path and the value.
	 * @throws EvaluationFailedException
	 *             Exception, if in creating the map.
	 */

	public static Map<String, CommandParam> fromAirXmlToParameters(final String outputParam)
			throws EvaluationFailedException {
		final Map<String, CommandParam> outputParams = new LinkedHashMap<String, CommandParam>();
		final List<String> pathList = new ArrayList<String>();
		try {
			final JSONObject xmlJSONObj = XML.toJSONObject(outputParam);
			final JSONObject methodResponse = (JSONObject) xmlJSONObj.get("methodCall");
			final JSONObject params = (JSONObject) methodResponse.get("params");
			final JSONObject param = (JSONObject) params.get("param");
			final JSONObject value = (JSONObject) param.get("value");
			CommandParamUtil.fromXmlToParameters(outputParams, pathList, value);
		} catch (final JSONException e) {
			throw new EvaluationFailedException("The input xml is not valid.", e);
		}
		return outputParams;
	}

	/**
	 * This method is used to create the response xml.
	 * 
	 * @param commandParam
	 *            the command param containing the output status.
	 * @return the response xml.
	 * @throws ExecutionFailedException
	 *             Exception, if execution could not be done.
	 */
	public static String createResponseXML(final CommandParam commandParam) throws ExecutionFailedException {
		final StringBuilder stringBuilder = new StringBuilder();
		stringBuilder.append("<?xml version='1.0'?>").append("\n").append("<methodResponse>").append("\n")
				.append("<params>").append("\n").append("<param>").append("\n").append("<value>").append("\n")
				.append("<struct>").append("\n").append("<member>").append("\n").append("<name>")
				.append(commandParam.getName()).append("</name>").append("\n").append("<value>").append("\n")
				.append(CommandParamUtil.toXmlForParam(commandParam)).append("</value>").append("\n")
				.append("</member>").append("\n").append("</struct>").append("\n").append("</value>").append("\n")
				.append("</param>").append("\n").append("</params>").append("\n").append("</methodResponse>");
		return stringBuilder.toString();
	}

	/*
	 * public static void main(String[] args) throws EvaluationFailedException,
	 * IOException { System.out.println(fromAirXmlToParameters(readFile())); }
	 * 
	 * private static String readFile() throws IOException { BufferedReader
	 * reader = new BufferedReader(new
	 * FileReader("C:\\Users\\jaiprakash1354\\Desktop\\asd.xml")); String line =
	 * null; StringBuilder stringBuilder = new StringBuilder(); String ls =
	 * System.getProperty("line.separator"); while ((line = reader.readLine())
	 * != null) { stringBuilder.append(line); stringBuilder.append(ls); } return
	 * stringBuilder.toString(); }
	 */

}
