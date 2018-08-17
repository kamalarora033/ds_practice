package com.ericsson.fdp.business.constants;

import java.util.Arrays;
import java.util.List;

/**
 * The Class NotificationConstants contains all constants that will used through
 * out the Business application.
 * 
 * @author Ericsson
 */
public final class MMLCommandConstants {

	/** The Constant NAM_VALUE. */
	public static final String NAM_VALUE = "NAM_VALUE";

	/** The Constant EQOSID. */
	public static final String EQOSID = "EQOSID";

	/** The Constant APNID. */
	public static final String APNID = "APNID";

	/** The Constant PDPID. */
	public static final String PDPID = "PDPID";

	/** The Constant PACKET_DATA_PROTOCOL_CONTEXT_DATA. */
	public static final String PACKET_DATA_PROTOCOL_CONTEXT_DATA = "PACKET_DATA_PROTOCOL_CONTEXT_DATA";

	/**
	 * The field to store the response.
	 */
	public static final String RESPONSE = "RESPONSE";

	/** The Constant ICR_CIRCLE_YES_OPTIONS. */
	public static final List<String> ICR_CIRCLE_YES_OPTIONS = Arrays.asList(new String[] { "yes", "y" });

	/** The Constant RESPONSE_VALUES. */
	public static final String RESPONSE_VALUES = "RESPONSE_VALUES";

	/**
	 * The result code delimiter.
	 */
	public static final String MML_DELIMITER = "\n";

	/** The Constant NAM. */
	public static final String NAM = "NAM";

	/** The Constant MML_LINE_DELIMITER. */
	public static final String MML_LINE_DELIMITER = " ";
}
