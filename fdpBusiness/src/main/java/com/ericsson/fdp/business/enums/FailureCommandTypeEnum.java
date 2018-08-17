package com.ericsson.fdp.business.enums;

import java.util.Arrays;
import java.util.List;

import com.ericsson.fdp.business.constants.ReportGenerationConstant;
import com.ericsson.fdp.common.constants.FDPConstant;

/**
 * This enum have the division of command type to write into different file on
 * command failure
 * 
 * @author GUR36857
 *
 */
public enum FailureCommandTypeEnum {

	/** The constant MONETARY */
	MONETARY(Arrays.asList(Command.UPDATE_BALACEANDATE.getCommandDisplayName(), Command.UPDATE_BALACEANDATE_MAIN.getCommandDisplayName(), 
			FDPConstant.MM_CHARGING_COMMAND, Command.LOYALTY_REDEEM.getCommandDisplayName(), Command.REFUND_MM.getCommandDisplayName(),
			Command.LOYALTY_REDEEM_ROLLBACK.getCommandDisplayName()), getFinalHeader("MONETARY"), "MONETARY_FAILURE_FILEPATH"),
	/** The constant EMA */
	EMA(Arrays.asList(Command.ADD_PCRF_SEVICE_V3.getCommandDisplayName(), "EXTEND_PCRF_SERVICE", Command.ADD_BB_BUNDLE.getCommandDisplayName(), Command.REMOVE_BB_BUNDLE.getCommandDisplayName(), Command.REMOVEPCRFSEVICE.getCommandDisplayName(),Command.ADD_PCRF_QUOTA.getCommandDisplayName()), getFinalHeader("EMA"), "EMA_FAILURE_FILEPATH"),
	/**The constant RS */
	RS(Arrays.asList(Command.UPDATE_PIN_DETAIL.getCommandDisplayName(),Command.MPR.getCommandDisplayName(),FDPConstant.RS_DEPROVISIONING_COMMAND),getFinalHeader("RS"),"RS_FAILURE_FILEPATH"),
	/** The constant GET */
	GET_EMA(Arrays.asList("GET_4G_SIM","GET_4G_HANDSET","GET_4G_HANDSET_ADC"),getFinalHeader("GET"),"GET_FAILURE_FILEPATH"),
	/** Mobile Money constant */
	GET_MM(Arrays.asList(Command.GET_TRANSACTION_STATUS.getCommandDisplayName()),getFinalHeader("GET"),"GET_MM_FAILURE_FILEPATH"),
	/** The constant NON_MONETARY */
	NON_MONETARY(null, getFinalHeader("NON_MONETARY"), "NON_MONETARY_FAILURE_FILEPATH");

	/** The Command List */
	private List<String> commandList;

	/** The report Headers */
	private String reportHeaders;

	/** The Property file path */
	private String propertyFilePath;

	/**
	 * 
	 * @param commandList
	 * @param reportHeaders
	 * @param propertyFilePath
	 */
	private FailureCommandTypeEnum(final List<String> commandList, String reportHeaders, String propertyFilePath) {
		this.commandList = commandList;
		this.reportHeaders = reportHeaders;
		this.propertyFilePath = propertyFilePath;
	}

	/**
	 * @return the commandList
	 */
	public List<String> getCommandList() {
		return commandList;
	}

	/**
	 * @return the reportHeaders
	 */
	public String getReportHeaders() {
		return reportHeaders;
	}

	/**
	 * @return the propertyFilePath
	 */
	public String getPropertyFilePath() {
		return propertyFilePath;
	}

	/**
	 * This method return the FailureCommandTypeEnum for the given command name	 * 
	 * @param commandName
	 * @return The FailureCommandTypeEnum Enum
	 */
	public static FailureCommandTypeEnum getFailureCommandTypeEnum(String commandName) {
		for (FailureCommandTypeEnum commandTypeEnum : FailureCommandTypeEnum.values()) 
			if (commandTypeEnum.getCommandList() != null && commandTypeEnum.getCommandList().contains(commandName))
				return commandTypeEnum;
		
		return NON_MONETARY;
	}
	
	/**
	 * This method check and return the GET type enum
	 * @param commandName
	 * @return the {@link FailureCommandTypeEnum}
	 */
	public static FailureCommandTypeEnum getFailureCommandTypeEnumForGETCommands(String commandName){
		if(FailureCommandTypeEnum.GET_EMA.getCommandList().contains(commandName))
			return FailureCommandTypeEnum.GET_EMA;
		else if (FailureCommandTypeEnum.GET_MM.getCommandList().contains(commandName)) {
			return FailureCommandTypeEnum.GET_MM;
		}
		return null;
	}

	/**
	 * This method return the headers based on the type
	 * 
	 * @param enumName
	 * @return The final CSV Headers
	 */
	private static String getFinalHeader(String enumName) {

		StringBuilder builder = new StringBuilder(ReportGenerationConstant.FAILURE_REPORT_COMMON_HEADERS);
		if ("MONETARY".equals(enumName))
			builder.append(",").append(ReportGenerationConstant.TIMEOUT_MONETARY_REPORT_HEADERS);
		return builder.toString();
	}

}
