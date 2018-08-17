package com.ericsson.fdp.business.command.activation.impl;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import com.ericsson.fdp.business.command.AbstractCommand;
import com.ericsson.fdp.business.command.param.impl.CommandParamOutput;
import com.ericsson.fdp.business.constants.MMLCommandConstants;
import com.ericsson.fdp.business.enums.PDPData;
import com.ericsson.fdp.common.enums.Primitives;
import com.ericsson.fdp.common.enums.Status;
import com.ericsson.fdp.core.command.FDPCommand;
import com.ericsson.fdp.core.command.param.CommandParam;
import com.ericsson.fdp.core.exception.ExecutionFailedException;
import com.ericsson.fdp.core.request.FDPRequest;
import com.ericsson.fdp.dao.enums.CommandParameterType;

/**
 * This class provides the interceptor for query command.
 * 
 * @author Ericsson
 * 
 */
public class FDPQueryMMLActivationCommandImpl extends FDPDefaultActivationCommandImpl {

	/**
	 * The constructor for the query command.
	 * 
	 * @param fdpCommand
	 *            the command to intercept.
	 */
	public FDPQueryMMLActivationCommandImpl(final FDPCommand fdpCommand) {
		super(fdpCommand);
	}

	@Override
	protected boolean preProcess(final FDPRequest fdpRequest) throws ExecutionFailedException {
		boolean execute = false;
		if (fdpRequest.getExecutedCommand(getFdpCommand().getCommandDisplayName()) == null) {
			execute = super.preProcess(fdpRequest);
		}
		return execute;
	}

	@Override
	protected void postProcess() throws ExecutionFailedException {
		final CommandParam response = getFdpCommand().getOutputParam(MMLCommandConstants.RESPONSE_VALUES);
		if (response != null) {
			final Map<String, CommandParam> outputParams = new LinkedHashMap<String, CommandParam>();
			final String reponseString = (String) response.getValue();
			final String[] outputArray = reponseString.split(MMLCommandConstants.MML_DELIMITER);
			int count = 0;
			for (final String string : outputArray) {
				if (string.contains(MMLCommandConstants.NAM) && (count + 1) < outputArray.length) {
					final CommandParam namValue = calculateNamValue(string, outputArray[count + 1]);
					outputParams.put(MMLCommandConstants.NAM_VALUE.toLowerCase(), namValue);
				}
				if (string.contains(MMLCommandConstants.APNID)) {
					final CommandParam apnidValues = calculateAPNIDValues(outputArray, count);
					outputParams.put(MMLCommandConstants.PACKET_DATA_PROTOCOL_CONTEXT_DATA.toLowerCase(), apnidValues);
				}
				count++;
			}
			if (getFdpCommand() instanceof AbstractCommand) {
				final AbstractCommand abstractCommand = (AbstractCommand) getFdpCommand();
				abstractCommand.addOutputParam(outputParams);
			}
		}
	}

	/**
	 * The method is used to calculate APNID values.
	 * 
	 * @param outputArray
	 *            the output array.
	 * @param count
	 *            the count.
	 * @return the command param.
	 * @throws ExecutionFailedException
	 *             Exception in execution.
	 */
	private CommandParam calculateAPNIDValues(final String[] outputArray, final int count)
			throws ExecutionFailedException {
		CommandParamOutput fdpCommandParamOutput = null;
		final String[] indexApnid = outputArray[count].trim().replaceAll("\\s{2,}", " ")
				.split(MMLCommandConstants.MML_LINE_DELIMITER);
		final List<String> valueStrings = new ArrayList<String>();
		for (int start = count + 1; start < outputArray.length; start++) {
			if (outputArray[start].isEmpty()) {
				break;
			}
			valueStrings.add(outputArray[start]);
		}
		if (!valueStrings.isEmpty()) {
			final Integer[] indexes = getIndexes(indexApnid);
			final List<Map<String, String>> values = new ArrayList<Map<String, String>>();
			for (final String string : valueStrings) {
				final List<String> valueIndexApnid = getIndexesForValues(string.trim().replaceAll("\\s{2,}", " ")
						.split(MMLCommandConstants.MML_LINE_DELIMITER), indexApnid);
				if (indexApnid.length != valueIndexApnid.size()) {
					throw new ExecutionFailedException("The length of apnid do not match " + outputArray[count]
							+ " and " + string);
				}
				values.add(getMapValues(indexes, valueIndexApnid));
			}
			fdpCommandParamOutput = new CommandParamOutput();
			fdpCommandParamOutput.setType(CommandParameterType.STRUCT);
			fdpCommandParamOutput.setValue(values);
		}

		return fdpCommandParamOutput;
	}

	private List<String> getIndexesForValues(final String[] split, final String[] indexApnid) {
		final List<String> values = new ArrayList<String>();
		int currentIndex = 0;
		for (final String string : indexApnid) {
			if (currentIndex > split.length) {
				break;
			}
			final PDPData pdpData = PDPData.getValue(string);
			if (pdpData == null || pdpData.getPdpPattern().matcher(split[currentIndex]).matches()) {
				values.add(split[currentIndex]);
				currentIndex++;
			} else {
				values.add("");
			}
		}

		return values;
	}

	/**
	 * This method is used to get map values.
	 * 
	 * @param indexes
	 *            the indexes.
	 * @param valueIndexApnid
	 *            the values to get from.
	 * @return the map values.
	 */
	private Map<String, String> getMapValues(final Integer[] indexes, final List<String> valueIndexApnid) {
		final Map<String, String> values = new HashMap<String, String>();
		if (indexes[0] != null) {
			values.put(MMLCommandConstants.APNID, valueIndexApnid.get(indexes[0]));
		}
		if (indexes[1] != null) {
			values.put(MMLCommandConstants.EQOSID, valueIndexApnid.get(indexes[1]));
		}
		if (indexes[2] != null) {
			values.put(MMLCommandConstants.PDPID, valueIndexApnid.get(indexes[2]));
		}
		return values;
	}

	/**
	 * This method is used to get the indexes.
	 * 
	 * @param indexApnid
	 *            the indexes to be fetched from the string.
	 * @return the indexes.
	 */
	private Integer[] getIndexes(final String[] indexApnid) {
		final Integer[] indexes = new Integer[3];
		int count = 0;
		for (final String string : indexApnid) {
			if (string.equals(MMLCommandConstants.APNID)) {
				indexes[0] = count;
			}
			if (string.equals(MMLCommandConstants.EQOSID)) {
				indexes[1] = count;
			}
			if (string.equals(MMLCommandConstants.PDPID)) {
				indexes[2] = count;
			}
			count++;
		}
		return indexes;
	}

	/**
	 * This method is used to find the NAM value.
	 * 
	 * @param namString
	 *            the string from which value is to be extracted.
	 * @param valueString
	 *            the value string.
	 * @return the NAM value.
	 * @throws ExecutionFailedException
	 *             Exception in execution.
	 */
	private CommandParam calculateNamValue(final String namString, final String valueString)
			throws ExecutionFailedException {
		final String[] indexNam = namString.replaceAll("\\s{2,}", " ").split(MMLCommandConstants.MML_LINE_DELIMITER);
		final String[] indexValue = valueString.replaceAll("\\s{2,}", " ")
				.split(MMLCommandConstants.MML_LINE_DELIMITER);
		if (indexNam.length != indexValue.length) {
			throw new ExecutionFailedException("The indexes for NAM and value do not match");
		}
		int index = 0;
		for (final String string : indexNam) {
			if (string.equals(MMLCommandConstants.NAM)) {
				break;
			}
			index++;
		}
		final CommandParamOutput fdpCommandParamOutput = new CommandParamOutput();
		fdpCommandParamOutput.setPrimitiveValue(Primitives.STRING);
		fdpCommandParamOutput.setType(CommandParameterType.PRIMITIVE);
		fdpCommandParamOutput.setValue(indexValue[index]);
		return fdpCommandParamOutput;
	}

	@Override
	protected Status process(final FDPRequest input, final Object... otherParams) throws ExecutionFailedException {
		Status status = Status.SUCCESS;
		if (input.getExecutedCommand(getFdpCommand().getCommandDisplayName()) == null) {
			status = super.process(input, otherParams);
		}
		return status;
	}

}
