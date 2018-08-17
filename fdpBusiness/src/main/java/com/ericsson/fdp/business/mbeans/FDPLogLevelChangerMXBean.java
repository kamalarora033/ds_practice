package com.ericsson.fdp.business.mbeans;

/**
 * The Interface FDPLogLevelChangerMXBean.
 */
public interface FDPLogLevelChangerMXBean {

	/**
	 * Sets the logger level.
	 * 
	 * @param circle
	 *            the circle
	 * @param module
	 *            the module
	 * @param appenderName
	 *            the appender name
	 * @param level
	 *            the level
	 */
	String setLoggerLevel(String circle, String module, String appenderName, String level);

	/**
	 * Sets the logger level.
	 * 
	 * @param circle
	 *            the circle
	 * @param module
	 *            the module
	 * @param level
	 *            the level
	 * @return the string
	 */
	String setLoggerLevel(String circle, String module, String level);

	/**
	 * Sets the logger level.
	 * 
	 * @param circle
	 *            the circle
	 * @param level
	 *            the level
	 */
	String setLoggerLevel(String circle, String level);

	/**
	 * Gets the logger level.
	 * 
	 * @param circle
	 *            the circle
	 * @param module
	 *            the module
	 * @param appenderName
	 *            the appender name
	 * @return the logger level
	 */
	String getLoggerLevel(String circle, String module, String appenderName);

	/**
	 * Gets the logger level.
	 * 
	 * @param circle
	 *            the circle
	 * @param module
	 *            the module
	 * @return the logger level
	 */
	String getLoggerLevel(String circle, String module);

	/**
	 * Gets the logger level.
	 * 
	 * @param circle
	 *            the circle
	 * @return the logger level
	 */
	String getLoggerLevel(String circle);

	/**
	 * Gets the logger appender queue info.
	 *
	 * @param circle the circle
	 * @return the logger appender queue info
	 */
	String getLoggerAppenderQueueInfo(String circle);

	/**
	 * Gets the appender queue info.
	 *
	 * @param circle the circle
	 * @param module the module
	 * @param appenderName the appender name
	 * @return the appender queue info
	 */
	String getAppenderQueueInfo(String circle, String module,
			String appenderName);
}
