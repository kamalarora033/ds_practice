package com.ericsson.ms.common.util;

import org.slf4j.Logger;

/**
 * This is logger implementation class for North bound Request handler
 * 
 * @author Ericsson
 *
 */
public class ReqHandlerLoggerUtil {

	private static final String LOGGER_CONSTANT = "{} {} | {}";

	/**
	 * 
	 */
	private ReqHandlerLoggerUtil() {

	}

	/**
	 * <p>
	 * Provides a standard process for logging debug messages. Debug messages
	 * should be used to display variable values and additional data throughout
	 * execution of methods to aid in debugging.
	 * </p>
	 * 
	 * @param log
	 *            The configuration of the current log to be used to write the
	 *            message to
	 * @param className
	 *            the class name
	 * @param methodName
	 *            The name of the method the message originated from
	 * @param message
	 *            The message to be logged
	 */
	public static void debug(final Logger log, final Class<?> className, final String methodName,
			final String message) {
		validateLogger(log, className);
		log.debug(LOGGER_CONSTANT, new Object[] { className.getName(), methodName, message });

	}

	private static void validateLogger(final Logger logger, final Class<?> className) {
		if (logger == null || className == null) {
			throw new IllegalArgumentException("Logger or className can't be null");
		}
	}

	/**
	 * <p>
	 * Provides a standard process for logging info messages. Info messages
	 * should be used to indicate important milestones within program execution
	 * </p>
	 * 
	 * @param log
	 *            The configuration of the current log to be used to write the
	 *            message to
	 * @param className
	 *            the class name
	 * @param methodName
	 *            The name of the method the message originated from
	 * @param message
	 *            The message to be logged
	 */
	public static void info(final Logger log, final Class<?> className, final String methodName, final String message) {
		validateLogger(log, className);
		if (log.isInfoEnabled()) {
			log.info(LOGGER_CONSTANT, new Object[] { className.getName(), methodName, message });
		}
	}

	/**
	 * <p>
	 * Provides a standard process for logging warning messages. Warning
	 * messages should be used for non-critical warnings that do not constitute
	 * an error message
	 * </p>
	 * 
	 * @param log
	 *            The configuration of the current log to be used to write the
	 *            message to
	 * @param className
	 *            the class name
	 * @param methodName
	 *            The name of the method the message originated from
	 * @param message
	 *            The message to be logged
	 */
	public static void warn(final Logger log, final Class<?> className, final String methodName, final String message) {
		validateLogger(log, className);
		if (log.isWarnEnabled()) {
			log.warn(LOGGER_CONSTANT, new Object[] { className.getName(), methodName, message });
		}
	}

	/**
	 * <p>
	 * Provides a standard process for logging error messages. Error messages
	 * should be used when an operation fails. All <code>Exceptions</code> are
	 * logged automatically, so errors should only be used where there is
	 * additional information to provide additional clarity as to why the
	 * exception was thrown
	 * </p>
	 * 
	 * @param log
	 *            The configuration of the current log to be used to write the
	 *            message to
	 * @param className
	 *            the class name
	 * @param methodName
	 *            The name of the method the message originated from
	 * @param message
	 *            The message to be logged
	 */
	public static void error(final Logger log, final Class<?> className, final String methodName,
			final String message) {
		validateLogger(log, className);
		if (log.isErrorEnabled()) {
			log.error(LOGGER_CONSTANT, new Object[] { className.getName(), methodName, message });
		}
	}

	/**
	 * <p>
	 * Provides a standard process for logging error messages. Error messages
	 * should be used when an operation fails. All <code>Exceptions</code> are
	 * logged automatically, so errors should only be used where there is
	 * additional information to provide additional clarity as to why the
	 * exception was thrown
	 * </p>
	 * 
	 * @param log
	 *            The configuration of the current log to be used to write the
	 *            message to
	 * @param className
	 *            the class name
	 * @param methodName
	 *            The name of the method the message originated from
	 * @param message
	 *            The message to be logged
	 * @param throwable
	 *            the throwable
	 */
	public static void error(final Logger log, final Class<?> className, final String methodName, final String message,
			final Throwable throwable) {
		validateLogger(log, className);
		if (log.isErrorEnabled()) {
			final StringBuilder errorMethodLog = new StringBuilder();
			errorMethodLog.append(className.getName());
			errorMethodLog.append(" ");
			errorMethodLog.append(methodName);
			errorMethodLog.append("|");
			errorMethodLog.append(message);
			log.error(errorMethodLog.toString(), throwable);
		}
	}

}
