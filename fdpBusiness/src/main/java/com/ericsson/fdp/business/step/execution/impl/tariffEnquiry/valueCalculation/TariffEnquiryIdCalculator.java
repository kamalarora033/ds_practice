package com.ericsson.fdp.business.step.execution.impl.tariffEnquiry.valueCalculation;

import java.util.ArrayList;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Repository;

import ch.qos.logback.classic.Logger;

import com.ericsson.fdp.business.util.RequestUtil;
import com.ericsson.fdp.business.util.TariffEnquiryUtil;
import com.ericsson.fdp.common.constants.FDPConstant;
import com.ericsson.fdp.common.enums.TariffEnquiryCalculationOptions;
import com.ericsson.fdp.common.enums.TariffEnquiryOption;
import com.ericsson.fdp.common.enums.TariffEnquiryOptionValues;
import com.ericsson.fdp.common.util.DateUtil;
import com.ericsson.fdp.core.command.FDPCommand;
import com.ericsson.fdp.core.exception.ExecutionFailedException;
import com.ericsson.fdp.core.logging.FDPLogger;
import com.ericsson.fdp.core.request.FDPRequest;

/**
 * This class is used for tariff enquiry.
 * 
 * @author Ericsson.
 * 
 */
@Repository
public class TariffEnquiryIdCalculator {

	
	
	
	/**
	 * This method will fetch Tariff-Command output.
	 * 
	 * @param tariffEnquiry
	 *            the tariff enquiry
	 * @param fdpRequest
	 *            the FDP request
	 * @param circleLogger
	 *            the logger.
	 * @return tariffDetails the tariff details
	 * @throws ExecutionFailedException
	 *             the execution exception
	 */
	public List<String> getTariffDetails(final TariffEnquiryOption tariffEnquiry, final FDPRequest fdpRequest,
			Logger circleLogger) throws ExecutionFailedException {
		List<String> tariffDetails = null;
		TariffEnquiryOptionValues values = TariffEnquiryOptionValues.getTariffEnquiryOptionValue(tariffEnquiry);
		String validityFormat = RequestUtil.getValidityFormat(fdpRequest);
		if (values != null) {
			FDPCommand executedCommand = fdpRequest.getExecutedCommand(values.getCommandName());
			FDPLogger.debug(
					circleLogger,
					getClass(),
					"getTariffDetails()",
					"Found Parameter Type:" + values.getParameterType() + ", for requestId:"
							+ fdpRequest.getRequestId() + ", validityFormat:" + validityFormat);
			if (executedCommand != null) {
				switch (values.getParameterType()) {
				case ARRAY_OF_STRUCT:
					tariffDetails = evaluateArrayStructValue(executedCommand, values, fdpRequest, validityFormat);
					break;
				case STRUCT:
					tariffDetails = getStructValue(values, executedCommand, fdpRequest, validityFormat);
					break;
				case ARRAY_OF_PRIMITIVES:
					tariffDetails = evaluateArrayPrimitiveValue(executedCommand, values, fdpRequest, validityFormat);
					break;
				case PRIMITIVE:
					tariffDetails = getPrimitiveValue(values, executedCommand, fdpRequest, validityFormat);
					break;
				default:
					throw new ExecutionFailedException("Not Supported Parameter Type for:" + values.getParameterType());
				}
				FDPLogger.debug(circleLogger, getClass(), "getTariffDetails()", "Exiting with tariffDetails:"
						+ tariffDetails + ", for requestId:" + fdpRequest.getRequestId());
				return tariffDetails;
			} else {
				throw new ExecutionFailedException("Command not executed with name: " + values.getCommandName());
			}
		} else {
			throw new ExecutionFailedException("Not able to fetch Tariff Option Detail Values for name:"
					+ tariffEnquiry.getName());
		}
	}

	
	/**
	 * This method will extract PRIMITIVE type parameterID list.
	 * 
	 * @param values
	 *            the TariffEnquiryOprtion value type
	 * @param executedCommand
	 *            the executed command
	 * @param fdpRequest
	 *            the FDP request
	 * @return tariffDetails list of values.
	 * @throws ExecutionFailedException
	 */
	private List<String> getPrimitiveValue(final TariffEnquiryOptionValues values, final FDPCommand executedCommand,
			final FDPRequest fdpRequest, final String validityFormat) throws ExecutionFailedException {
		List<String> tariffDetails = new ArrayList<String>();
		String id = executedCommand.getOutputParam(values.getParameterId()).getValue().toString();
		tariffDetails.add(id);
		if (TariffEnquiryCalculationOptions.REGULAR.equals(values.getTariffEnquiryCalculationOptions())) {
			String[] otherValues = new String[3];
			if (values.getParameterValue() != null) {
				otherValues[0] = executedCommand.getOutputParam(values.getParameterValue()).getValue().toString();
			}
			if (values.getParameterExpiry() != null) {
				String[] expiryString = values.getParameterExpiry().split(FDPConstant.COMMA);
				GregorianCalendar calenderObject = null;
				for(String expiry : expiryString) {
					if(null != executedCommand.getOutputParam(
							expiry.trim())) {
						calenderObject=  (GregorianCalendar) executedCommand.getOutputParam(
								expiry.trim()).getValue();
						if(null != calenderObject) {
							otherValues[1] = DateUtil.convertCalendarDateToString(calenderObject, validityFormat);
							break;
						} else {
							otherValues[1]=FDPConstant.EMPTY_STRING;
						}
					}
				}
			}
			if(values.getUnitType() != null) {
				otherValues[2] = executedCommand.getOutputParam(values.getUnitType()).getValue().toString();
			}
			Map<String, String[]> otherValuesMap = new HashMap<String, String[]>();
			otherValuesMap.put(id, otherValues);
			TariffEnquiryUtil.updateValuesInRequest(fdpRequest, otherValuesMap, values.getTariffEnquiryOption());
		}
		return tariffDetails;
	}

	/**
	 * This method will extract STRUCT type parameterID list.
	 * 
	 * @param values
	 *            the TariffEnquiryOprtion value type
	 * @param executedCommand
	 *            the executed command
	 * @param fdpRequest
	 *            the FDP request
	 * @return tariffDetails list of values.
	 */
	private List<String> getStructValue(final TariffEnquiryOptionValues values, final FDPCommand executedCommand,
			final FDPRequest fdpRequest, final String validityFormat) {
		List<String> tariffDetails = new ArrayList<String>();
		Map<String, String[]> tariffOptions = new HashMap<String, String[]>();
		String id = executedCommand
				.getOutputParam(values.getParameterName() + FDPConstant.PARAMETER_SEPARATOR + values.getParameterId())
				.getValue().toString();
		tariffDetails.add(id);
		if (TariffEnquiryCalculationOptions.REGULAR.equals(values.getTariffEnquiryCalculationOptions())) {
			Map<String, String[]> otherValues = new HashMap<String, String[]>();
			otherValues.put(id, getOtherValuesForStruct(values, executedCommand, validityFormat));
			TariffEnquiryUtil.updateValuesInRequest(fdpRequest, tariffOptions, values.getTariffEnquiryOption());
		}
		return tariffDetails;
	}

	/**
	 * This method will extract other values (validity and Expiry) for
	 * STRUCTtype parameterID list.
	 * 
	 * 
	 * @param values
	 *            the tariff enquiry values.
	 * @param executedCommand
	 *            the executed command
	 * @return otherValues array of String with other values.
	 * 
	 */
	private String[] getOtherValuesForStruct(final TariffEnquiryOptionValues values, final FDPCommand executedCommand,
			final String validityFormat) {
		String[] otherValues = new String[3];
		if (values.getParameterValue() != null) {
			otherValues[0] = executedCommand
					.getOutputParam(
							values.getParameterName() + FDPConstant.PARAMETER_SEPARATOR + values.getParameterValue())
					.getValue().toString();
		}
		if (values.getParameterExpiry() != null) {
			String[] expiryString = values.getParameterExpiry().split(FDPConstant.COMMA);
			GregorianCalendar calenderObject = null;
			for(String expiry : expiryString) {
				if(null != executedCommand.getOutputParam(
						values.getParameterName() + FDPConstant.PARAMETER_SEPARATOR + expiry.trim())) {
					calenderObject = (GregorianCalendar) executedCommand.getOutputParam(
							values.getParameterName() + FDPConstant.PARAMETER_SEPARATOR + expiry.trim())
							.getValue();
					if(null != calenderObject) {
						otherValues[1] = DateUtil.convertCalendarDateToString(calenderObject, validityFormat);
						break;
					} else {
						otherValues[1] = FDPConstant.EMPTY_STRING;
					}
				}
			}
		}
		if(values.getUnitType() != null) {
			otherValues[2] = executedCommand
					.getOutputParam(
							values.getParameterName() + FDPConstant.PARAMETER_SEPARATOR + values.getUnitType())
					.getValue().toString();
		}
		return otherValues;
	}

	/**
	 * This method will extract all the values for ARRAY_OF_PRIMITIVES
	 * parameters type.
	 * 
	 * @param executedCommand
	 *            the executed command
	 * @param values
	 *            the tariff detail values
	 * @param fdpRequest
	 *            the FDP request
	 * @return tariffDetails list of values.
	 */
	private List<String> evaluateArrayPrimitiveValue(final FDPCommand executedCommand,
			final TariffEnquiryOptionValues values, final FDPRequest fdpRequest, final String validityFormat) {
		List<String> tariffDetails = new ArrayList<String>();
		String pathkey = null;
		int i = 0;
		Map<String, String[]> otherValues = new HashMap<String, String[]>();
		while (executedCommand
				.getOutputParam(pathkey = (values.getParameterName() + FDPConstant.PARAMETER_SEPARATOR + i)) != null) {
			String id = executedCommand.getOutputParam(pathkey).getValue().toString();
			tariffDetails.add(id);
			if (TariffEnquiryCalculationOptions.REGULAR.equals(values.getTariffEnquiryCalculationOptions())) {
				otherValues.put(id, getOtherValuesForArrayOfPrimitives(values, i, executedCommand, validityFormat));
			}
			i++;
		}
		TariffEnquiryUtil.updateValuesInRequest(fdpRequest, otherValues, values.getTariffEnquiryOption());
		return tariffDetails;
	}

	/**
	 * This method will extract all the other values (validity and Expiry) for
	 * ARRAY_OF_PRIMITIVES parameters type.
	 * 
	 * @param values
	 *            the tariff enquiry option values
	 * @param i
	 *            the index for array
	 * @param executedCommand
	 *            the executed command
	 * @return otherValues array of String with other values.
	 */
	private String[] getOtherValuesForArrayOfPrimitives(final TariffEnquiryOptionValues values, final int i,
			final FDPCommand executedCommand, final String validityFormat) {
		String[] otherValues = new String[3];
		if (values.getParameterValue() != null) {
			otherValues[0] = executedCommand
					.getOutputParam(values.getParameterValue() + FDPConstant.PARAMETER_SEPARATOR + i).getValue()
					.toString();
		}
		if (values.getParameterExpiry() != null) {
			String[] expiryString = values.getParameterExpiry().split(FDPConstant.COMMA); 
			GregorianCalendar calenderObject = null;
			for(String expiry : expiryString) {
				if(null != executedCommand.getOutputParam(expiry.trim() + FDPConstant.PARAMETER_SEPARATOR + i)) {
					calenderObject = (GregorianCalendar) executedCommand.getOutputParam(
							expiry.trim() + FDPConstant.PARAMETER_SEPARATOR + i).getValue();
					if(null != calenderObject) {
						otherValues[1] = DateUtil.convertCalendarDateToString(calenderObject, validityFormat);
						break;
					} else {
						otherValues[1] = FDPConstant.EMPTY_STRING;
					}
				}
				
			}
		}
		if(values.getUnitType() != null) {
			otherValues[2] = executedCommand
					.getOutputParam(values.getUnitType() + FDPConstant.PARAMETER_SEPARATOR + i).getValue()
					.toString();
		}
		return otherValues;
	}

	/**
	 * This method will extract all values for ARRAY_OF_STRUCT parameters type.
	 * 
	 * @param executedCommand
	 *            the executed command
	 * @param values
	 *            the tariff enquiry option value
	 * @param fdpRequest
	 *            the FDP request
	 * @return tariffDetails the list of string.
	 */
	private List<String> evaluateArrayStructValue(final FDPCommand executedCommand,
			final TariffEnquiryOptionValues values, final FDPRequest fdpRequest, final String validityFormat) {
		List<String> tariffDetails = new ArrayList<String>();
		String pathkey = null;
		int i = 0;
		Map<String, String[]> tariffOptions = new HashMap<String, String[]>();
		while (executedCommand.getOutputParam(pathkey = (values.getParameterName() + FDPConstant.PARAMETER_SEPARATOR
				+ i + FDPConstant.PARAMETER_SEPARATOR + values.getParameterId())) != null) {
			String id = executedCommand.getOutputParam(pathkey).getValue().toString();
			tariffDetails.add(id);
			if (TariffEnquiryCalculationOptions.REGULAR.equals(values.getTariffEnquiryCalculationOptions())) {
				tariffOptions.put(id, getOtherValuesForArrayOfStruct(executedCommand, values, i, validityFormat));
			}
			i++;
		}
		TariffEnquiryUtil.updateValuesInRequest(fdpRequest, tariffOptions, values.getTariffEnquiryOption());

		return tariffDetails;
	}

	/**
	 * This method will extract all other(validity and Expiry) values for
	 * ARRAY_OF_STRUCT parameters type.
	 * 
	 * @param executedCommand
	 *            the executed command
	 * @param values
	 *            the tariffEnquiryOption values
	 * @param i
	 *            the index of array
	 * @return otherValues array of String with other values.
	 */
	private String[] getOtherValuesForArrayOfStruct(final FDPCommand executedCommand,
			final TariffEnquiryOptionValues values, final int i, final String validityFormat) {
		String[] otherValues = new String[3];
		if (values.getParameterValue() != null) {
			otherValues[0] = executedCommand
					.getOutputParam(
							values.getParameterName() + FDPConstant.PARAMETER_SEPARATOR + i
									+ FDPConstant.PARAMETER_SEPARATOR + values.getParameterValue()).getValue()
					.toString();
		}
		if (values.getParameterExpiry() != null) {
			String[] expiryString = values.getParameterExpiry().split(FDPConstant.COMMA);
			GregorianCalendar calenderObject = null;
			for (String expiry : expiryString) {
				if (null != executedCommand.getOutputParam(values.getParameterName() + FDPConstant.PARAMETER_SEPARATOR
						+ i + FDPConstant.PARAMETER_SEPARATOR + expiry.trim())) {
					calenderObject = (GregorianCalendar) executedCommand.getOutputParam(
							values.getParameterName() + FDPConstant.PARAMETER_SEPARATOR + i
									+ FDPConstant.PARAMETER_SEPARATOR + expiry.trim()).getValue();
					if (null != calenderObject) {
						otherValues[1] = DateUtil.convertCalendarDateToString(calenderObject, validityFormat);
						break;
					} else {
						otherValues[1] = FDPConstant.EMPTY_STRING;
					}
				}

			}
		}
		if (values.getUnitType() != null) {
			otherValues[2] = executedCommand
					.getOutputParam(
							values.getParameterName() + FDPConstant.PARAMETER_SEPARATOR + i
									+ FDPConstant.PARAMETER_SEPARATOR + values.getUnitType()).getValue().toString();
		}
		return otherValues;
	}
}
