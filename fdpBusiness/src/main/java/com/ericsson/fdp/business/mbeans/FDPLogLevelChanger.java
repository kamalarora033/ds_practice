package com.ericsson.fdp.business.mbeans;

import java.lang.management.ManagementFactory;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.annotation.Resource;
import javax.ejb.DependsOn;
import javax.ejb.Singleton;
import javax.ejb.Startup;
import javax.inject.Inject;
import javax.management.MBeanServer;
import javax.management.ObjectName;

import org.slf4j.LoggerFactory;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.Appender;
import ch.qos.logback.core.AsyncAppenderBase;

import com.ericsson.fdp.common.constants.FDPConstant;
import com.ericsson.fdp.common.exception.FDPServiceException;
import com.ericsson.fdp.common.logging.LoggingConstants;
import com.ericsson.fdp.core.config.utils.PropertyUtils;
import com.ericsson.fdp.core.dsm.framework.service.FDPCache;
import com.ericsson.fdp.core.logging.LogLevelChangerRequest;
import com.ericsson.fdp.core.logging.LoggerType;
import com.ericsson.fdp.core.logging.enums.AppenderType;
import com.ericsson.fdp.core.monitor.LogLevelChangerPublisher;
import com.ericsson.fdp.core.serviceprovisioning.bean.FDPAppBag;

/**
 * The Class FDPLogLevelChanger.
 */
@Singleton
@Startup
@DependsOn(value = "FDPCacheBuilder")
public class FDPLogLevelChanger implements FDPLogLevelChangerMXBean {
	
	
	/** The publisher. */
	@Inject
	private LogLevelChangerPublisher publisher;
	
	/** The platform m bean server. */
	private MBeanServer platformMBeanServer;

	/** The object name. */
	private ObjectName objectName = null;

	/** The Constant DSMSERVICE. */
	// private static final DSMService DSMSERVICE =
	// DSMServiceImpl.getInstance();

	/** The request cache. */
	@Resource(lookup = "java:app/fdpCoreServices-1.0/ApplicationConfigCache")
	private static FDPCache<FDPAppBag, Object> applicationConfigCache;

	private static final LoggerContext LOGGERCONTEXT = (LoggerContext) LoggerFactory.getILoggerFactory();

	public static final String SUCCESS_MESSAGE = "Logger level set successFully.";

	/**
	 * Register in jmx.
	 */
	@PostConstruct
	public final void registerInJMX() {
		try {
			objectName = new ObjectName("FDPLogMonitoring:type=" + this.getClass().getName());
			platformMBeanServer = ManagementFactory.getPlatformMBeanServer();
			platformMBeanServer.registerMBean(this, objectName);
		} catch (Exception e) {
			throw new IllegalStateException("Problem during registration of Monitoring into JMX:" + e);
		}
	}

	/**
	 * Unregister from jmx.
	 */
	@PreDestroy
	public final void unregisterFromJMX() {
		try {
			platformMBeanServer.unregisterMBean(this.objectName);
		} catch (Exception e) {
			throw new IllegalStateException("Problem during unregistration of Monitoring into JMX:" + e);
		}
	}

	@Override
	public final String setLoggerLevel(final String circle, final String module, final String appenderName,
			final String level) {
		String message = null;
		// validate circle, level, module and appender
		FDPRequestBeanValidator requestValidatorForCircle = this.validateCircleName(circle);
		FDPRequestBeanValidator requestValidatorForLevel = this.validateLevel(level);
		FDPRequestBeanValidator requestValidatorForModule = this.validateModuleName(module);
		FDPRequestBeanValidator requestValidatorForAppender = this.validateAppender(appenderName);

		if (requestValidatorForCircle.getIsValid() && requestValidatorForLevel.getIsValid()
				&& requestValidatorForModule.getIsValid() && requestValidatorForAppender.getIsValid()) {
			Logger logger = LOGGERCONTEXT.getLogger(circle + LoggingConstants.DOT + module + LoggingConstants.DOT
					+ appenderName);
			logger.setLevel(Level.valueOf(level));
			try {
				updateCacheLogger(circle, level, appenderName, module);
			} catch (FDPServiceException e) {
				message = new StringBuffer().append("Unable to set logger level ")
						.append(requestValidatorForCircle.getMessage()).append(requestValidatorForLevel.getMessage())
						.append(requestValidatorForModule.getMessage()).append(requestValidatorForAppender.getMessage())
						.toString();
				return message;
			}
			message = SUCCESS_MESSAGE;
		} else {
			message = new StringBuffer().append("Unable to set logger level ")
					.append(requestValidatorForCircle.getMessage()).append(requestValidatorForLevel.getMessage())
					.append(requestValidatorForModule.getMessage()).append(requestValidatorForAppender.getMessage())
					.toString();
		}
		return message;
	}

	@Override
	public final String getLoggerLevel(final String circle, final String module, final String appenderName) {

		String returnValue = null;
		// validate circle and module
		FDPRequestBeanValidator requestValidatorForCircle = this.validateCircleName(circle);
		FDPRequestBeanValidator requestValidatorForModule = this.validateModuleName(module);
		FDPRequestBeanValidator requestValidatorForAppender = this.validateAppender(appenderName);

		if (requestValidatorForCircle.getIsValid() && requestValidatorForModule.getIsValid()
				&& requestValidatorForAppender.getIsValid()) {
			final String loggerName = circle + LoggingConstants.DOT + module + LoggingConstants.DOT + appenderName;
			Logger logger = LOGGERCONTEXT.getLogger(loggerName);
			returnValue = logger.getLevel() == null ? null : logger.getLevel().toString();
		} else {
			returnValue = new StringBuffer().append("Unable to get logger level ")
					.append(requestValidatorForCircle.getMessage()).append(requestValidatorForModule.getMessage())
					.append(requestValidatorForAppender.getMessage()).toString();
		}
		return returnValue;
	}

	@Override
	public final String setLoggerLevel(final String circle, final String module, final String level) {

		String message = null;
		// validate circle, level and module
		FDPRequestBeanValidator requestValidatorForCircle = this.validateCircleName(circle);
		FDPRequestBeanValidator requestValidatorForLevel = this.validateLevel(level);
		FDPRequestBeanValidator requestValidatorForModule = this.validateModuleName(module);
		if (requestValidatorForCircle.getIsValid() && requestValidatorForLevel.getIsValid()
				&& requestValidatorForModule.getIsValid()) {
			Logger logger = null;
			String appenderName = null;
			for (AppenderType appenderType : AppenderType.values()) {
				appenderName = appenderType.getAppenderName();
				try {
					updateCacheLogger(circle, level, appenderName, module);
				} catch (FDPServiceException e) {
					message = new StringBuffer().append("Unable to set logger level ")
							.append(requestValidatorForCircle.getMessage()).append(requestValidatorForLevel.getMessage())
							.append(requestValidatorForModule.getMessage()).toString();
					return message;
				}
			}
			logger = LOGGERCONTEXT.getLogger(circle + LoggingConstants.DOT + module);
			logger.setLevel(Level.toLevel(level));
			message = SUCCESS_MESSAGE;
		} else {
			message = new StringBuffer().append("Unable to set logger level ")
					.append(requestValidatorForCircle.getMessage()).append(requestValidatorForLevel.getMessage())
					.append(requestValidatorForModule.getMessage()).toString();
		}

		return message;
	}

	@Override
	public String setLoggerLevel(final String circle, final String level) {

		String message = null;
		// validate circle, level and module
		FDPRequestBeanValidator requestValidatorForCircle = this.validateCircleName(circle);
		FDPRequestBeanValidator requestValidatorForLevel = this.validateLevel(level);

		if (requestValidatorForCircle.getIsValid() && requestValidatorForLevel.getIsValid()) {

			Logger logger = null;
			String appenderName = null;
			final String[] circleWisemodules = PropertyUtils.getProperty(LoggingConstants.FDP_CIRCLE_MODULES).split(
					LoggingConstants.COMMA_SEPARATOR);
			for (AppenderType appenderType : AppenderType.values()) {
				appenderName = appenderType.getAppenderName();
				for (String module : circleWisemodules) {
					try {
						updateCacheLogger(circle, level, appenderName, module);
					} catch (FDPServiceException e) {
						message = new StringBuffer().append("Unable to set logger level ")
								.append(requestValidatorForCircle.getMessage()).append(requestValidatorForLevel.getMessage())
								.toString();
						return message;
					}
				}
			}
			logger = LOGGERCONTEXT.getLogger(circle);
			logger.setLevel(Level.toLevel(level));
			message = SUCCESS_MESSAGE;
		} else {
			message = new StringBuffer().append("Unable to set logger level ")
					.append(requestValidatorForCircle.getMessage()).append(requestValidatorForLevel.getMessage())
					.toString();
		}
		return message;
	}

	private void updateCacheLogger(final String circle, final String level,
			String appenderName, String module) throws FDPServiceException {
		LoggerType loggerType = new LoggerType();
		loggerType.setCircleName(circle);
		loggerType.setModuleName(module);
		loggerType.setAppenderName(appenderName);
		/*FDPAppBag fdpAppBag = new FDPAppBag();
		fdpAppBag.setSubStore(AppCacheSubStore.LOGGER);
		fdpAppBag.setKey(loggerType);
		Logger logger = (Logger) applicationConfigCache.getValue(fdpAppBag);
		// logger = (Logger) DSMSERVICE.getValue(loggerType);
		logger.setLevel(Level.toLevel(level));
		applicationConfigCache.putValue(fdpAppBag, logger);*/
		LogLevelChangerRequest requestToBePublished = new LogLevelChangerRequest();
		requestToBePublished.setLevel(level);
		requestToBePublished.setLoggerType(loggerType);
		publisher.pushToTopic(requestToBePublished);
	}

	@Override
	public final String getLoggerLevel(final String circle, final String module) {
		String returnValue = null;
		// validate circle and module
		FDPRequestBeanValidator requestValidatorForCircle = this.validateCircleName(circle);
		FDPRequestBeanValidator requestValidatorForModule = this.validateModuleName(module);
		if (requestValidatorForCircle.getIsValid() && requestValidatorForModule.getIsValid()) {
			final String loggerName = circle + LoggingConstants.DOT + module;
			Logger logger = LOGGERCONTEXT.getLogger(loggerName);
			returnValue = logger.getLevel() == null ? null : logger.getLevel().toString();
		} else {
			returnValue = new StringBuffer().append("Unable to get logger level ")
					.append(requestValidatorForCircle.getMessage()).append(requestValidatorForModule.getMessage())
					.toString();
		}
		return returnValue;
	}

	@Override
	public final String getLoggerLevel(final String circle) {
		String returnVal = null;
		final String loggerName = circle;
		// validate circle
		FDPRequestBeanValidator requestValidatorForCircle = this.validateCircleName(circle);
		if (requestValidatorForCircle.getIsValid()) {
			Logger logger = LOGGERCONTEXT.getLogger(loggerName);
			returnVal = logger.getLevel() == null ? null : logger.getLevel().toString();
		} else {
			returnVal = "Unable to get logger level " + requestValidatorForCircle.getMessage();
		}
		return returnVal;
	}
	
	@Override
	public String getLoggerAppenderQueueInfo(final String circle) {

		StringBuilder message = new StringBuilder();
		// validate circle
		FDPRequestBeanValidator validationResult = this.validateCircleName(circle);
		if (validationResult.getIsValid()) {
			String appenderName = null;
			final String[] circleWisemodules = PropertyUtils.getProperty(LoggingConstants.FDP_CIRCLE_MODULES).split(
					LoggingConstants.COMMA_SEPARATOR);
			for (AppenderType appenderType : AppenderType.values()) {
				appenderName = appenderType.getAppenderName();
				for (String module : circleWisemodules) {
						message.append(getAppenQueueInfo(circle, module, appenderName)).append(FDPConstant.NEWLINE);
				}
			}
		} else {
			message.append("Invalid Circle")
					.append(validationResult.getMessage()).toString();
		}
		return message.toString();
	}

	@Override
	public String getAppenderQueueInfo(String circle, String module, String appenderName) {
		String message = null;
		// validate circle
		FDPRequestBeanValidator validationResult = this.validateCircleName(circle);
		FDPRequestBeanValidator requestValidatorForModule = this.validateModuleName(module);
		if (validationResult.getIsValid() && requestValidatorForModule.getIsValid()) {
			message = getAppenQueueInfo(circle, module, appenderName).toString();
		} else {
			message = new StringBuilder(requestValidatorForModule.getMessage()).append(FDPConstant.NEWLINE)
			.append(validationResult.getMessage()).toString();
		}
		return message;
	}

	private String getAppenQueueInfo(String circle, String module,
			String appenderName) {
		StringBuilder result = new StringBuilder(circle).append(FDPConstant.UNDERSCORE).append(module)
				.append(FDPConstant.UNDERSCORE).append(appenderName).append(FDPConstant.EQUAL);
		final String loggerName = circle + LoggingConstants.DOT + module + LoggingConstants.DOT + appenderName;
		Logger logger = LOGGERCONTEXT.getLogger(loggerName);
		Appender<ILoggingEvent> appender = logger.getAppender(appenderName);
		if (appender == null) {
			result.append(FDPConstant.NOT_FOUND);
		} else {
			if (appender instanceof AsyncAppenderBase<?>) {
				AsyncAppenderBase<?> asyncAppender = (AsyncAppenderBase<?>) appender;
				result.append(FDPConstant.QUEUE_SIZE).append(asyncAppender.getQueueSize())
				.append(FDPConstant.REMAINING).append(asyncAppender.getRemainingCapacity())
				.append(FDPConstant.USED).append(asyncAppender.getNumberOfElementsInQueue())
				.append(FDPConstant.DISCARDING_THRESHOLD).append(asyncAppender.getNumberOfElementsInQueue());
			} else {
				result.append(FDPConstant.IS_SYNCHRONOUS);
			}
		}
		return result.toString();
	}

	private FDPRequestBeanValidator validateCircleName(String circleName) {

		FDPRequestBeanValidator requestValidator = new FDPRequestBeanValidator();
		List<String> fdpCircleNames = new ArrayList<String>();
		String circles = PropertyUtils.getProperty("fdp.circles");

		if (circles != null) {
			fdpCircleNames.addAll(Arrays.asList(circles.split(FDPConstant.COMMA)));
		}
		requestValidator.setIsValid(fdpCircleNames.contains(circleName));
		if (!requestValidator.getIsValid()) {
			requestValidator.setMessage(new StringBuffer().append(" Valid Circles: ").append(fdpCircleNames)
					.append("Input Circle: ").append(circleName).toString());
		} else {
			requestValidator.setMessage(FDPConstant.EMPTY_STRING);
		}
		return requestValidator;
	}

	/**
	 * This method validate logger level.
	 * 
	 * @param level
	 *            level
	 * @return requestValidator object.
	 */
	private FDPRequestBeanValidator validateLevel(String level) {

		FDPRequestBeanValidator requestValidator = new FDPRequestBeanValidator();
		List<String> possibleLevelList = new ArrayList<String>();
		possibleLevelList.addAll(Arrays.asList(new String[] { Level.OFF.levelStr, Level.ERROR.levelStr,
				Level.WARN.levelStr, Level.INFO.levelStr, Level.DEBUG.levelStr, Level.TRACE.levelStr,
				Level.ALL.levelStr }));
		requestValidator.setIsValid(possibleLevelList.contains(level));
		if (!requestValidator.getIsValid()) {
			requestValidator.setMessage(new StringBuffer().append(" Possible Levels: ").append(possibleLevelList)
					.append(" Input Level: ").append(level).toString());
		} else {
			requestValidator.setMessage(FDPConstant.EMPTY_STRING);
		}
		return requestValidator;
	}

	/**
	 * This method validates module name
	 * 
	 * @param moduleName
	 *            module name
	 * @return requestValidator object.
	 */
	private FDPRequestBeanValidator validateModuleName(String moduleName) {

		String moduleNames = PropertyUtils.getProperty("fdp.circle.modules");
		List<String> moduleList = new ArrayList<String>();
		if (moduleNames != null) {
			moduleList.addAll(Arrays.asList(moduleNames.split(FDPConstant.COMMA)));
		}
		FDPRequestBeanValidator requestValidator = new FDPRequestBeanValidator();
		requestValidator.setIsValid(moduleList.contains(moduleName));
		if (!requestValidator.getIsValid()) {
			requestValidator.setMessage(new StringBuffer().append(" Valid modules: ").append(moduleList)
					.append(" Input Module name: ").append(moduleName).toString());
		} else {
			requestValidator.setMessage(FDPConstant.EMPTY_STRING);
		}
		return requestValidator;
	}

	/**
	 * This method validates appender
	 * 
	 * @param appender
	 *            appender
	 * @return requestValidator object.
	 */
	private FDPRequestBeanValidator validateAppender(String appender) {

		List<String> appenderList = new ArrayList<String>();
		for (AppenderType appenderType : AppenderType.values()) {
			appenderList.add(appenderType.getAppenderName());
		}
		FDPRequestBeanValidator requestValidator = new FDPRequestBeanValidator();
		requestValidator.setIsValid(appenderList.contains(appender));
		if (!requestValidator.getIsValid()) {
			requestValidator.setMessage(new StringBuffer().append(" Valid appender: ").append(appenderList)
					.append(" Input appender name: ").append(appender).toString());
		} else {
			requestValidator.setMessage(FDPConstant.EMPTY_STRING);
		}
		return requestValidator;
	}
}
