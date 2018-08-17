package com.ericsson.fdp.business.util;

import java.util.List;
import java.util.Map;

import ch.qos.logback.classic.Logger;

import com.ericsson.fdp.FDPCacheable;
import com.ericsson.fdp.business.command.AbstractCommand;
import com.ericsson.fdp.business.constants.BusinessConstants;
import com.ericsson.fdp.business.enums.AuxRequestParam;
import com.ericsson.fdp.business.enums.Command;
import com.ericsson.fdp.business.enums.ConfigurationKey;
import com.ericsson.fdp.business.enums.RequestMetaValuesKey;
import com.ericsson.fdp.business.node.impl.ProductNode;
import com.ericsson.fdp.business.product.Product;
import com.ericsson.fdp.business.product.impl.SharedAccountProduct;
import com.ericsson.fdp.common.constants.FDPConstant;
import com.ericsson.fdp.common.constants.FDPLoggerConstants;
import com.ericsson.fdp.common.enums.BusinessModuleType;
import com.ericsson.fdp.common.enums.ChannelType;
import com.ericsson.fdp.common.util.StringUtil;
import com.ericsson.fdp.core.command.FDPCommand;
import com.ericsson.fdp.core.command.param.CommandParam;
import com.ericsson.fdp.core.config.utils.PropertyUtils;
import com.ericsson.fdp.core.exception.EvaluationFailedException;
import com.ericsson.fdp.core.logging.FDPLogger;
import com.ericsson.fdp.core.logging.FDPLoggerFactory;
import com.ericsson.fdp.core.node.FDPNode;
import com.ericsson.fdp.core.request.FDPRequest;
import com.ericsson.fdp.core.request.FDPSMPPRequest;
import com.ericsson.fdp.dao.enums.CommandExecutionType;
import com.ericsson.fdp.dao.enums.FDPServiceProvType;
import com.ericsson.fdp.dao.util.CommandDisplayNameComparator;
import com.ericsson.fdp.dao.util.ServiceProvDataConverterUtil;

/**
 * 
 * This class is the utility class for loggers.
 * 
 * @author Ericsson
 * 
 */
public class LoggerUtil {

	/**
	 * Instantiates a new logger util.
	 */
	private LoggerUtil() {

	}

	/**
	 * This method is used to get the logger from the request.
	 * 
	 * @param fdpRequest
	 *            The request parameter.
	 * @return The logger.
	 */
	public static Logger getSummaryLoggerFromRequest(final FDPRequest fdpRequest) {
		return FDPLoggerFactory.getRequestLogger(fdpRequest.getCircle().getCircleName(), getModuleName(fdpRequest));
	}

	/**
	 * Gets the module name.
	 *
	 * @param fdpRequest the fdp request
	 * @return the module name
	 */
	private static String getModuleName(final FDPRequest fdpRequest) {
		String moduleName = BusinessModuleType.SMSC_SOUTH.name();
		switch(fdpRequest.getChannel()) {
		case USSD :
			moduleName = BusinessModuleType.USSD_SOUTH.name();
			break;
		case IVR :
			moduleName = BusinessModuleType.IVR_NORTH.name();
			break;
		case COD:
			moduleName=BusinessModuleType.FDPOFFLINE_SOUTH.name();
			break;
		default :
			moduleName = BusinessModuleType.SMSC_SOUTH.name();
		}
		return moduleName;
	}

	/**
	 * This method is used to get the request appender for the request.
	 * 
	 * @param fdpRequest
	 *            The request for which the appender is required.
	 * @return The string to be appended.
	 */
	public static String getRequestAppender(final FDPRequest fdpRequest) {
		return getRequestAppender(fdpRequest.getRequestId());
	}

	/**
	 * This method is used to get the request appender for the request.
	 * 
	 * @param requestId
	 *            The request for which the appender is required.
	 * @return The string to be appended.
	 */
	public static String getRequestAppender(final String requestId) {
		return FDPConstant.REQUEST_ID + FDPConstant.LOGGER_KEY_VALUE_DELIMITER + requestId
				+ FDPConstant.LOGGER_DELIMITER;
	}

	/**
	 * This method is used to generate logs for user behaviour.
	 * 
	 * @param dynamicMenuRequest
	 *            The request containing information.
	 */
	public static void generatePreLogsForUserBehaviour(final FDPSMPPRequest dynamicMenuRequest) {

	}

	/**
	 * This method is used to generate logs for user behaviour after processing
	 * has finished.
	 * 
	 * @param dynamicMenuRequest
	 *            the request.
	 * @param fdpNode
	 *            the node that was furnished.
	 */
	public static void generatePostLogsForUserBehaviour(final FDPSMPPRequest dynamicMenuRequest, final FDPNode fdpNode, boolean isDirectLeg) {
		final String channelCode = getChannelCodeFromFdpNode(dynamicMenuRequest, fdpNode);
		String channel = null;
		
		if (dynamicMenuRequest.getChannel() != null) {
			channel = dynamicMenuRequest.getChannel().name();
		}
		
		final String requestAppender = getRequestAppender(dynamicMenuRequest);
		final StringBuilder appenderValue = new StringBuilder();
		appenderValue.append(requestAppender).append("ACTN").append(FDPConstant.LOGGER_KEY_VALUE_DELIMITER)
				.append(dynamicMenuRequest.getRequestStringInterface().getActionString())
				.append(FDPConstant.LOGGER_DELIMITER).append("CH").append(FDPConstant.LOGGER_KEY_VALUE_DELIMITER)
				.append(channel)
				.append(FDPConstant.LOGGER_DELIMITER).append("CHCODE").append(FDPConstant.LOGGER_KEY_VALUE_DELIMITER)
				.append(channelCode)
				.append(FDPConstant.LOGGER_DELIMITER).append("DMCODE").append(FDPConstant.LOGGER_KEY_VALUE_DELIMITER)
				.append(channelCode);
	//	appenderValue.append(FDPConstant.LOGGER_DELIMITER).append("TP").append(FDPConstant.LOGGER_KEY_VALUE_DELIMITER);
		appenderValue.append(FDPConstant.LOGGER_DELIMITER).append("NODETYPE").append(FDPConstant.LOGGER_KEY_VALUE_DELIMITER);
		if (fdpNode == null) {
			if (ChannelType.RS.name().equals(channel)) {
				appenderValue.append(getRSNodeInfo(dynamicMenuRequest));
			} else {
				appenderValue.append("Undefined");
			}
		} else {
			appenderValue.append(fdpNode.generateNodeInfo(dynamicMenuRequest));
		}
		/** Appending Display Name + Subscriber Number + Direct/Indirect **/
		appenderValue.append(FDPConstant.LOGGER_DELIMITER).append("DNAME").append(FDPConstant.LOGGER_KEY_VALUE_DELIMITER);
		if (fdpNode == null) {
			if (ChannelType.RS.name().equals(channel)) {
				appenderValue.append("NACT");
			} else {
				appenderValue.append("Undefined");
			}
		} else {
			appenderValue.append(fdpNode.getDisplayName());
		}
		
		appenderValue.append(FDPConstant.LOGGER_DELIMITER).append(FDPConstant.MSISDN).append(FDPConstant.LOGGER_KEY_VALUE_DELIMITER).
		append(dynamicMenuRequest.getIncomingSubscriberNumber());
		
		appenderValue.append(FDPConstant.LOGGER_DELIMITER).append("DIRECT").append(FDPConstant.LOGGER_KEY_VALUE_DELIMITER).
		append(isDirectLeg);
		
		
		/**Hard Coded value for MODE */ 
		appenderValue.append(FDPConstant.LOGGER_DELIMITER).append(FDPLoggerConstants.MODE).append(FDPConstant.LOGGER_KEY_VALUE_DELIMITER).
		append("RealTime");
		/**IP address for CIS Business Server*/ 
		appenderValue.append(FDPConstant.LOGGER_DELIMITER).append(FDPLoggerConstants.CIS_SERVER_IP).append(FDPConstant.LOGGER_KEY_VALUE_DELIMITER).
		append(PostLogsUtil.getVMIP());
		
		appenderValue.append(FDPConstant.LOGGER_DELIMITER).append(FDPLoggerConstants.REFILL_PROFILE_ID).append(FDPConstant.LOGGER_KEY_VALUE_DELIMITER).
		append(PostLogsUtil.getRefillProfileId(dynamicMenuRequest));
		
		appenderValue.append(FDPConstant.LOGGER_DELIMITER).append(FDPLoggerConstants.ORIGIN_OPERATOR_ID).append(FDPConstant.LOGGER_KEY_VALUE_DELIMITER).
		append(PostLogsUtil.getOriginOperatorID(dynamicMenuRequest));
		
		appenderValue.append(FDPConstant.LOGGER_DELIMITER).append(FDPLoggerConstants.TRANSACTION_CODE).append(FDPConstant.LOGGER_KEY_VALUE_DELIMITER).
		append(PostLogsUtil.getTransactionCode(dynamicMenuRequest));
		
		appenderValue.append(FDPConstant.LOGGER_DELIMITER).append(FDPLoggerConstants.CORRELATION_ID).append(FDPConstant.LOGGER_KEY_VALUE_DELIMITER).
		append(PostLogsUtil.getCorrelationID(dynamicMenuRequest));
		
		appenderValue.append(FDPConstant.LOGGER_DELIMITER).append(FDPLoggerConstants.ME2U).append(FDPConstant.LOGGER_KEY_VALUE_DELIMITER).
		append(PostLogsUtil.isMe2U(dynamicMenuRequest));
					
		appenderValue.append(FDPConstant.LOGGER_DELIMITER).append(FDPLoggerConstants.ME2U_OP).append(FDPConstant.LOGGER_KEY_VALUE_DELIMITER).
		append(PostLogsUtil.getMe2uInfo(dynamicMenuRequest));
		
		appenderValue.append(FDPConstant.LOGGER_DELIMITER).append(FDPLoggerConstants.FNF_OP).append(FDPConstant.LOGGER_KEY_VALUE_DELIMITER).
		append(PostLogsUtil.getFnFInfo(dynamicMenuRequest));
		
		appenderValue.append(FDPConstant.LOGGER_DELIMITER).append(FDPLoggerConstants.BENEFICIARY_MSISDN).append(FDPConstant.LOGGER_KEY_VALUE_DELIMITER).
		append(PostLogsUtil.getBenMsisdn(dynamicMenuRequest));
		
		appenderValue.append(FDPConstant.LOGGER_DELIMITER).append(FDPLoggerConstants.AUTO_RENEWAL).append(FDPConstant.LOGGER_KEY_VALUE_DELIMITER).
		append(PostLogsUtil.isAutoRenewal(dynamicMenuRequest));
		
		appenderValue.append(FDPConstant.LOGGER_DELIMITER).append(FDPLoggerConstants.SESSION_ID).append(FDPConstant.LOGGER_KEY_VALUE_DELIMITER).
		append(PostLogsUtil.getSessionId(dynamicMenuRequest));
		
		appenderValue.append(FDPConstant.LOGGER_DELIMITER).append(FDPLoggerConstants.RESPONSE_TIME).append(FDPConstant.LOGGER_KEY_VALUE_DELIMITER).
		append(PostLogsUtil.getresponseTime(dynamicMenuRequest));

		PostLogsUtil.appendCommandLogs(dynamicMenuRequest, appenderValue);
		
		final Logger circleLogger = LoggerUtil.getSummaryLoggerFromRequest(dynamicMenuRequest);
		FDPLogger.info(circleLogger, LoggerUtil.class, "generatePostLogsForUserBehaviour()", appenderValue.toString());
		if (ChannelType.SMS.equals(dynamicMenuRequest.getChannel())) {
			FDPLogger.info(circleLogger, LoggerUtil.class, "generatePostLogsForUserBehaviour()", requestAppender
					+ "SCD" + FDPConstant.LOGGER_KEY_VALUE_DELIMITER
					+ dynamicMenuRequest.getRequestStringInterface().getNodeString());
		}
	}

	/**
	 * This method is used to generate product behavior logs for user behavior.
	 *
	 * @param dynamicMenuRequest the request.
	 * @param parameter the parameter
	 */
	public static void generatePolicyBehaviourLogsForUserBehaviour(final FDPRequest dynamicMenuRequest,
			final String parameter) {
		final String requestAppender = getRequestAppender(dynamicMenuRequest);
		final StringBuilder appenderValue = new StringBuilder();
		appenderValue.append(requestAppender).append("TBH").append(FDPConstant.LOGGER_KEY_VALUE_DELIMITER)
				.append(parameter);
		FDPLogger.info(getSummaryLoggerFromRequest(dynamicMenuRequest), LoggerUtil.class,
				"generateProductBehaviourLogsForUserBehaviour()", appenderValue.toString());
	}
	
	/**
	 * This method is used to generate product behavior logs for user behavior for Loan and COD.
	 *
	 * @param dynamicMenuRequest the request.
	 * @param parameter the parameter
	 */
	public static void generatePolicyBehaviourLogsForUserBehaviourLoanCod(final FDPRequest dynamicMenuRequest,
			final String parameter) {
		final String requestAppender = getRequestAppender(dynamicMenuRequest);
		final StringBuilder appenderValue = new StringBuilder();
		appenderValue.append(requestAppender).append("TBH").append(FDPConstant.LOGGER_KEY_VALUE_DELIMITER)
				.append(parameter);
		FDPLogger.info(getSummaryLoggerFromRequest(dynamicMenuRequest), LoggerUtil.class,
				"generateProductBehaviourLogsForUserBehaviour()", appenderValue.toString());
	}
	
	/**
	 * This method is used to generate Low Balance logs during product buy.
	 *
	 * @param dynamicMenuRequest the request.
	 * @param parameter the parameter
	 */
	public static void generatePolicyBehaviourLogsForLowBalance(final FDPRequest dynamicMenuRequest,
			final String parameter) {
		final String requestAppender = getRequestAppender(dynamicMenuRequest);
		final StringBuilder appenderValue = new StringBuilder();
		appenderValue.append(requestAppender).append("ISLOWBAL").append(FDPConstant.LOGGER_KEY_VALUE_DELIMITER)
				.append(parameter);
		FDPLogger.info(getSummaryLoggerFromRequest(dynamicMenuRequest), LoggerUtil.class,
				"generatePolicyBehaviourLogsForLowBalance()", appenderValue.toString());
	}
	
	/**
	 * This method is used to generate logs for Loan Eligibility.
	 *
	 * @param dynamicMenuRequest the request.
	 * @param parameter the parameter
	 */
	public static void generatePolicyBehaviourLogsForLoanEligibility(final FDPRequest dynamicMenuRequest,
			final String parameter) {
		final String requestAppender = getRequestAppender(dynamicMenuRequest);
		final StringBuilder appenderValue = new StringBuilder();
		appenderValue.append(requestAppender).append("LOANELIGIBLE").append(FDPConstant.LOGGER_KEY_VALUE_DELIMITER)
				.append(parameter);
		FDPLogger.info(getSummaryLoggerFromRequest(dynamicMenuRequest), LoggerUtil.class,
				"generatePolicyBehaviourLogsForLoanEligibility()", appenderValue.toString());
	}
	
	/**
	 * This method is used to generate logs for Loan Eligibility.
	 *
	 * @param dynamicMenuRequest the request.
	 * @param parameter the parameter
	 */
	public static void generatePolicyBehaviourLogsForLoanPurchase(final FDPRequest dynamicMenuRequest,
			final String parameter) {
		final String requestAppender = getRequestAppender(dynamicMenuRequest);
		final StringBuilder appenderValue = new StringBuilder();
		appenderValue.append(requestAppender).append("LOANPURCHASE").append(FDPConstant.LOGGER_KEY_VALUE_DELIMITER)
				.append(parameter);
		FDPLogger.info(getSummaryLoggerFromRequest(dynamicMenuRequest), LoggerUtil.class,
				"generatePolicyBehaviourLogsForLoanPurchase()", appenderValue.toString());
	}

	/**
	 * Gets the channel code from fdp node.
	 * 
	 * @param dynamicMenuRequest
	 *            the dynamic menu request
	 * @param fdpNode
	 *            the fdp node
	 * @return the channel code from fdp node
	 */
	public static String getChannelCodeFromFdpNode(final FDPSMPPRequest dynamicMenuRequest, final FDPNode fdpNode) {
		String channelCode = null;
		if (fdpNode instanceof ProductNode) {
			final ProductNode productNode = (ProductNode) fdpNode;
			if (productNode.getAliasCode() != null
					&& (!productNode.getAliasCode().isEmpty() && productNode.getAliasCode()
							.get(FDPConstant.FIRST_INDEX) != null)) {
				channelCode = StringUtil.toStringFromList(productNode.getAliasCode(), ",");
			} else {
				channelCode = dynamicMenuRequest.getRequestStringInterface().getActionString(fdpNode);
			}
		} else {
			channelCode = dynamicMenuRequest.getRequestStringInterface().getActionString(fdpNode);
		}
		return channelCode;
	}

	/**
	 * Gets the summary logger for vas.
	 *
	 * @param fdpRequest the fdp request
	 * @return the summary logger for vas
	 */
	public static Logger getSummaryLoggerForVAS(final FDPRequest fdpRequest) {
		final String moduleName = BusinessModuleType.VAS_NORTH.name();
		return FDPLoggerFactory.getRequestLogger(fdpRequest.getCircle().getCircleName(), moduleName);
	}

	/**
	 * Gets the request logger.
	 * 
	 * @param circleName
	 *            the circle name
	 * @return the request logger
	 */
	public static Logger getRequestLogger(final String circleName) {
		final String moduleName = BusinessModuleType.VAS_NORTH.name();
		return FDPLoggerFactory.getRequestLogger(circleName, moduleName);
	}

	/**
	 * Gets the request logger.
	 * 
	 * @param circleName
	 *            the circle name
	 * @param moduleType
	 *            the module type
	 * @return the request logger
	 */
	public static Logger getRequestLogger(final String circleName, final BusinessModuleType moduleType) {
		return FDPLoggerFactory.getRequestLogger(circleName, moduleType.name());
	}
	
	/**
	 * Returns true if UA log is enabled.
	 * @param fdpRequest
	 * @return
	 */
	public static Boolean isUALogEnabled(final FDPRequest fdpRequest){
		if(null != fdpRequest.getCircle().getConfigurationKeyValueMap().get(FDPConstant.UA_LOG_ENABLED) &&
				fdpRequest.getCircle().getConfigurationKeyValueMap().get(FDPConstant.UA_LOG_ENABLED).equalsIgnoreCase(FDPConstant.TRUE) &&
				null != fdpRequest.getCircle().getConfigurationKeyValueMap().get(FDPConstant.UA_LOGGING_ID)){
			return true;
		} else {
			return false;
		}
		
	}

	public static void generateLogsForRollbackCommand(FDPRequest fdpRequest, FDPCommand rollbackCommand) {
		PostLogsUtil.generateLogsForRollbackCommand(fdpRequest, rollbackCommand);
		
	}

	private static String getRSNodeInfo(final FDPSMPPRequest fdpRequest) {
		final StringBuilder userBehaviourLoggString = new StringBuilder();
		Product product = null;
		final FDPCacheable fdpCacheable = fdpRequest.getValueFromRequest(RequestMetaValuesKey.PRODUCT);
		if (fdpCacheable instanceof Product) {
			product = (Product) fdpCacheable;
		}
		userBehaviourLoggString.append("Product").append(FDPConstant.LOGGER_DELIMITER).append("PRODUCT")
				.append(FDPConstant.LOGGER_KEY_VALUE_DELIMITER).append("Buy Product")
				.append(FDPConstant.LOGGER_DELIMITER).append("PID").append(FDPConstant.LOGGER_KEY_VALUE_DELIMITER);

		if (product == null) {
			userBehaviourLoggString.append("Undefined");
		} else {
			userBehaviourLoggString.append(product.getProductId()).append(FDPConstant.LOGGER_DELIMITER).append("PNAME")
					.append(FDPConstant.LOGGER_KEY_VALUE_DELIMITER).append(product.getProductName())
					.append(FDPConstant.LOGGER_DELIMITER).append("PCNAME")
					.append(FDPConstant.LOGGER_KEY_VALUE_DELIMITER)
					.append(product.getProductNameForChannel(fdpRequest.getChannel()))
					.append(FDPConstant.LOGGER_DELIMITER).append("PTYPE").append(FDPConstant.LOGGER_KEY_VALUE_DELIMITER)
					// .append(FDPConstant.LOGGER_DELIMITER).append("PTP").append(FDPConstant.LOGGER_KEY_VALUE_DELIMITER)
					.append(product.getProductType()).append(FDPConstant.LOGGER_DELIMITER).append("PUSNAME")
					.append(FDPConstant.LOGGER_KEY_VALUE_DELIMITER)
					.append(product.getProductNameForChannel(ChannelType.USSD)).append(FDPConstant.LOGGER_DELIMITER)
					.append("PSMSNAME").append(FDPConstant.LOGGER_KEY_VALUE_DELIMITER)
					.append(product.getProductNameForChannel(ChannelType.SMS)).append(FDPConstant.LOGGER_DELIMITER)
					.append("PWBNAME").append(FDPConstant.LOGGER_KEY_VALUE_DELIMITER)
					.append(product.getProductNameForChannel(ChannelType.WEB)).append(FDPConstant.LOGGER_DELIMITER)
					.append("PCNSTRNT").append(FDPConstant.LOGGER_KEY_VALUE_DELIMITER)
					.append(PostLogsUtil.getProductConstraint(fdpRequest, product));
			if (product instanceof SharedAccountProduct) {
				final SharedAccountProduct sharedAccountProduct = (SharedAccountProduct) product;
				userBehaviourLoggString.append(FDPConstant.LOGGER_DELIMITER).append("PMETA")
						.append(FDPConstant.LOGGER_KEY_VALUE_DELIMITER)
						.append(StringUtil.convertMapToStringWithDelimiter(sharedAccountProduct.getProductMetaData(),
								FDPConstant.LOGGER_KEY_VALUE_VALUE_DELIMITER));
			}
		}
		return userBehaviourLoggString.toString();
	}
	
}
