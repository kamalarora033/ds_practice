package com.ericsson.fdp.business.constants;

import com.ericsson.fdp.common.constants.FDPConstant;

/**
 * The Class ReportGenerationConstant.
 * 
 * @author Ericsson
 */
public class ReportGenerationConstant {

	/** The Constant FILENAME_SEPARATOR. */
	public static final String FILENAME_SEPARATOR = FDPConstant.UNDERSCORE;

	/** The Constant CGW_HEADERS. */
	public static final String CGW_HEADERS = "CGW Details,Start Time,End Time,# of Request Received,# of Requests Discarded ,# of Requests Served";

	/** The Constant THT_HEADERS. */
	public static final String THT_HEADERS = "Interface Type,IP,Logical Name,Start DateTime,End DateTime,TPS,"
			+ "Request Sent,Response Received ,No Response,Failover ,Retry";

	/** The Constant IHT_HEADERS. */
	public static final String IHT_HEADERS = "Channel,IP,Logical Name,Start Time,"
			+ "End time,TPS,Success,Failure,Total";

	/** The Constant PP_HEADERS. */
	public static final String PP_HEADERS = "MSISDN,Channel,Channel code,Short code,Request ID,Date/Time,Reponse Time (ms),"
			+ "Product ID,Product name,Product USSD name,Product SMS name,Product type,"
			+ "Product Constraint,Product Meta,Transaction Code,Refill ProfileID,Origin Operator ID,"
			+ "CS Correlation ID,Provisioning Parameters Modified,Amount charged (Paisa),Charging Node,"
			+ "Success/Failure,Reason,Notification Sent,Template Id";

	/** The Constant EMA_HEADERS. */
	public static final String EMA_HEADERS = "MSISDN,Channel,Channel code,Short code,Request ID,Date/Time,EMA Logical Name,EMA Command,Success/Failure,Reason";

	/** The Constant CSV_COLUMN_SEPARATOR. */
	public static final String CSV_COLUMN_SEPARATOR = FDPConstant.COMMA;

	/** The Constant REPORT_FOLDER_PATH. */
	public static final String REPORT_FOLDER_PATH = "report.file.path";

	/** The Constant REPORT_ARCHIVE_FOLDER_PATH. */
	public static final String REPORT_ARCHIVE_FOLDER_PATH = "report.archive.file.path";

	/** The Constant REVENUE_HEADERS. */
	public static final String REVENUE_REPORT_HEADERS = "Channel,Logical Name,Date/Time,Product ID,Product name,Product USSD name,Product SMS name,"
			+ "Product Web name,Amount charged,Product Meta";

	
	
	/** The Constant FAILURE_REPORT_COMMON_HEADERS */
	public static final String FAILURE_REPORT_COMMON_HEADERS = "TimeStamp,RID,MSISDN,ProductType,ProductID,ProductName,Interface,CommandName,FailedCommand,ResponseCode";

	/** The Constant TIMEOUT_MONETARY_REPORT_HEADERS */
	public static final String TIMEOUT_MONETARY_REPORT_HEADERS = "Amount,DAID#Type#Value,DAID#Type#Value,DAID#Type#Value";	
	
	
}
