package com.ericsson.fdp.business.util;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

import com.ericsson.fdp.common.constants.FDPConstant;
import com.ericsson.fdp.common.util.DateUtil;
import com.ericsson.fdp.core.exception.ExecutionFailedException;
import com.ericsson.fdp.core.request.FDPRequest;
import com.ericsson.fdp.dao.dto.serviceprov.ValidityValueDTO;

/**
 * The Class CommandParamInputUtil.
 * 
 * @author Ericsson
 */
public class CommandParamInputUtil {

	/**
	 * Instantiates a new command param input util.
	 */
	private CommandParamInputUtil() {

	}

	/**
	 * This method is used to find the value in case of validity.
	 * 
	 * @param definedValue
	 *            the defined value.
	 * @return the value found.
	 * @throws ExecutionFailedException
	 *             the execution failed exception
	 */
	public static Object evaluateValidity(final ValidityValueDTO definedValue) throws ExecutionFailedException {
		Calendar validityDate = Calendar.getInstance();
		boolean enddtforEMA = false;
		try {
			switch (definedValue.getValidityType()) {
			case APPLY_FROM_TODAY:
				updateApplyFromToday(validityDate);
				break;

			case APPLY_FROM_TODAY_PLUS:
				updateApplyFromToday(validityDate);
				addValuesInDate(validityDate, definedValue.getDays(), definedValue.getHours(),
						definedValue.getMinutes());
				break;

			case DAYS_HRS_MIN_FROM_TODAY:
			case DAYS_HRS_MIN_FROM_TODAY_PLUS:
				addValuesInDate(validityDate, definedValue.getDays(), definedValue.getHours(),
						definedValue.getMinutes());
				break;

			case FIXED_DATE:
				validityDate = DateUtil.getDateTimeFromFDPDateTimeFormat(definedValue.getStartDate());
				break;

			case FIXED_DATE_FOR_NEXT_MONTH:
				validityDate.add(Calendar.MONTH, 1);
				validityDate.set(Calendar.DATE, definedValue.getStartDayNextMonth());
				break;

			case FIXED_DATE_FOR_NEXT_MONTH_PLUS:
				validityDate.add(Calendar.MONTH, 1);
				validityDate.set(Calendar.DATE, definedValue.getStartDayNextMonth());
				addValuesInDate(validityDate, definedValue.getDays(), definedValue.getHours(),
						definedValue.getMinutes());
				break;

			case FIXED_DATE_PLUS:
				validityDate = DateUtil.getDateTimeFromFDPDateTimeFormat(definedValue.getStartDate());
				addValuesInDate(validityDate, definedValue.getDays(), definedValue.getHours(),
						definedValue.getMinutes());
				break;

			case NEVER_EXPIRE:
				validityDate = DateUtil.getDateTimeFromFDPDateTimeFormat(FDPConstant.DATE_MAX);
				break;

			case NOW:
				break;

			case NOW_PLUS:
				addValuesInDate(validityDate, definedValue.getDays(), definedValue.getHours(),
						definedValue.getMinutes());
				break;

			case NOW_MINUS:
				addValuesInDate(validityDate, -1 * definedValue.getDays(), -1 * definedValue.getHours(), -1
						* definedValue.getMinutes());
				break;

			case APPLY_FROM_TODAY_MINUS:
				updateApplyFromToday(validityDate);
				addValuesInDate(validityDate, -1 * definedValue.getDays(), -1 * definedValue.getHours(), -1
						* definedValue.getMinutes());
				break;

			case DAYS_HRS_MIN_FROM_TODAY_MINUS:
				addValuesInDate(validityDate, -1 * definedValue.getDays(), -1 * definedValue.getHours(), -1
						* definedValue.getMinutes());
				break;

			case FIXED_DATE_MINUS:
				validityDate = DateUtil.getDateTimeFromFDPDateTimeFormat(definedValue.getStartDate());
				addValuesInDate(validityDate, -1 * definedValue.getDays(), -1 * definedValue.getHours(), -1
						* definedValue.getMinutes());
				break;

			case FIXED_DATE_FOR_NEXT_MONTH_MINUS:
				validityDate.add(Calendar.MONTH, 1);
				validityDate.set(Calendar.DATE, definedValue.getStartDayNextMonth());
				addValuesInDate(validityDate, -1 * definedValue.getDays(), -1 * definedValue.getHours(), -1
						* definedValue.getMinutes());
				break;
			
			case END_DATE:
				validityDate = DateUtil.getDateTimeFromDateTimeFormatssZ(validityDate);
				 enddtforEMA = true;
				
				break;
			case START_DATE_PCRF:
				addValuesInDate(validityDate, definedValue.getDays(), definedValue.getHours(),
						definedValue.getMinutes());
				enddtforEMA = true;
				
				break;
			case END_DATE_PCRF:
								
				addValuesInDate(validityDate, definedValue.getDays(), definedValue.getHours(),
						definedValue.getMinutes());
				 enddtforEMA = true;
				 break;
			case APPLY_FROM_TODAY_PLUS_EMA:
				
				updateApplyFromToday(validityDate);
				addValuesInDateEMA(validityDate, definedValue.getDays(), definedValue.getHours(),
						definedValue.getMinutes(),definedValue.getSeconds());
				 enddtforEMA = true;
				 break;
			case NOW_MINUS_ONE:
				 addValuesInDate(validityDate, definedValue.getDays()-1, definedValue.getHours(),
						definedValue.getMinutes());
				break;
				
			default:
				throw new ExecutionFailedException("Could not evaluate input" + definedValue.getValidityType());
			}
		} catch (final ParseException e) {
			throw new ExecutionFailedException("Could not parse input", e);
		}
		String Formator = enddtforEMA ? FDPConstant.PCR_DATE_PATTERN : FDPConstant.FDP_DB_SAVE_DATE_PATTERN;
		return DateUtil.convertCalendarDateToString(validityDate, Formator);
	}

	/**
	 * This method updates the values in the date.
	 * 
	 * @param validityDate
	 *            the date in which values are to be added.
	 * @param days
	 *            the days to add.
	 * @param hours
	 *            the hours to add.
	 * @param minutes
	 *            the minutes to add.
	 */
	private static void addValuesInDate(final Calendar validityDate, final Integer days, final Integer hours,
			final Integer minutes) {
		updateValuesInDate(validityDate, Calendar.DATE, days);
		updateValuesInDate(validityDate, Calendar.HOUR, hours);
		updateValuesInDate(validityDate, Calendar.MINUTE, minutes);
	}
	
	private static void addValuesInDateEMA(final Calendar validityDate, final Integer days, final Integer hours,
			final Integer minutes,final Integer seconds) {
		updateValuesInDate(validityDate, Calendar.DATE, days);
		updateValuesInDate(validityDate, Calendar.HOUR, hours);
		updateValuesInDate(validityDate, Calendar.MINUTE, minutes);
		updateValuesInDate(validityDate,Calendar.SECOND,seconds);
	}
	/**
	 * This method is used to update the values in date.
	 * 
	 * @param validityDate
	 *            the date to add.
	 * @param valueInWhichToAdd
	 *            the value in which on to add.
	 * @param valueToAdd
	 *            the value to add.
	 */
	public static void updateValuesInDate(final Calendar validityDate, final Integer valueInWhichToAdd,
			final Integer valueToAdd) {
		if (valueToAdd != null) {
			validityDate.add(valueInWhichToAdd, valueToAdd);
		}
	}

	/**
	 * Update validity in case of apply from today.
	 * 
	 * @param validityDate
	 *            the validity date.
	 */
	private static void updateApplyFromToday(final Calendar validityDate) {
		validityDate.set(Calendar.HOUR_OF_DAY, 0);
		validityDate.set(Calendar.MINUTE, 0);
		validityDate.set(Calendar.SECOND, 0);
		validityDate.set(Calendar.MILLISECOND, 0);
	}

	/**
	 * Gets the value from circle configuration.
	 * 
	 * @param string
	 *            the string
	 * @param fdpRequest
	 *            the fdp request
	 * @return the value from circle configuration
	 */
	public static String getValueFromCircleConfiguration(final String string, final FDPRequest fdpRequest) {
		return fdpRequest.getCircle().getConfigurationKeyValueMap().get(string);
	}

}
