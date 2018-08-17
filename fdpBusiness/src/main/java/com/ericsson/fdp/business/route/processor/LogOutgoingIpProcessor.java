package com.ericsson.fdp.business.route.processor;

import javax.annotation.Resource;

import org.apache.camel.Exchange;
import org.apache.camel.Message;
import org.apache.camel.Processor;
import org.apache.camel.component.smpp.SmppConstants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ericsson.fdp.common.constants.FDPConstant;
import com.ericsson.fdp.common.enums.BusinessModuleType;
import com.ericsson.fdp.common.vo.FDPCircle;
import com.ericsson.fdp.core.cache.AppCacheSubStore;
import com.ericsson.fdp.core.dsm.framework.service.FDPCache;
import com.ericsson.fdp.core.entity.MobileMoneySystemDetails;
import com.ericsson.fdp.core.logging.FDPLogger;
import com.ericsson.fdp.core.logging.FDPLoggerFactory;
import com.ericsson.fdp.core.serviceprovisioning.bean.FDPAppBag;
import com.ericsson.fdp.dao.dto.FDPADCConfigDTO;
import com.ericsson.fdp.dao.dto.FDPAIRConfigDTO;
import com.ericsson.fdp.dao.dto.FDPAbilityConfigDTO;
import com.ericsson.fdp.dao.dto.FDPCGWConfigDTO;
import com.ericsson.fdp.dao.dto.FDPCMSConfigDTO;
import com.ericsson.fdp.dao.dto.FDPDMCConfigDTO;
import com.ericsson.fdp.dao.dto.FDPLoyaltyConfigDTO;
import com.ericsson.fdp.dao.dto.FDPRSConfigDTO;
import com.ericsson.fdp.dao.dto.FDPSMSCConfigDTO;
import com.ericsson.fdp.dao.enums.ExternalSystem;
import com.ericsson.fdp.route.constant.RoutingConstant;
import com.ericsson.fdp.route.enumeration.SMPPBindNodeType;

public class LogOutgoingIpProcessor implements Processor {

	/** The request cache. */
	@Resource(lookup = "java:app/fdpCoreServices-1.0/ApplicationConfigCache")
	private FDPCache<FDPAppBag, Object> applicationConfigCache;

	private static final Logger LOGGER = LoggerFactory.getLogger(LogOutgoingIpProcessor.class);

	// Thread local variable containing each thread's ID
	private static final ThreadLocal<ManageRequestCount> REQUEST_ID_LOCAL = new ThreadLocal<ManageRequestCount>();

	private class ManageRequestCount {
		private int successCount;
		private String requestId;

		public int getSuccessCount() {
			return successCount;
		}

		public void setSuccessCount(final int successCount) {
			this.successCount = successCount;
		}

		public String getRequestId() {
			return requestId;
		}

		public void setRequestId(final String requestId) {
			this.requestId = requestId;
		}

	}

	@Override
	public void process(final Exchange exchange) throws Exception {
		try {
			final Message in = exchange.getIn();
			if (in == null) {
				throw new IllegalArgumentException("In Message Body is null.");
			}
			final String outGoingcircleCodeIPaddressPort = exchange.getProperty(
					RoutingConstant.OUTGOING_CIRCLE_CODE_IP_PORT, String.class);
			if (outGoingcircleCodeIPaddressPort == null) {
				throw new IllegalArgumentException("outGoingcircleCodeIPaddressPort is null");
			}
			final String outgoingIpAddress = exchange.getProperty(RoutingConstant.OUTGOING_IP_ADDRESS, String.class);
			if (outgoingIpAddress == null) {
				throw new IllegalArgumentException("outgoingIpAddress is null");
			}
			final String circleCode = exchange.getProperty(RoutingConstant.CIRCLE_CODE, String.class);
			if (circleCode == null) {
				throw new IllegalArgumentException("circleCode is null");
			}
			final String externalSystemType = exchange.getProperty(RoutingConstant.EXTERNAL_SYSTEM_TYPE, String.class);
			if (externalSystemType == null) {
				throw new IllegalArgumentException("externalSystemType is null");
			}
			final String requestId = exchange.getProperty(RoutingConstant.REQUEST_ID, String.class);
			if (requestId == null) {
				throw new IllegalArgumentException("requestId is null");
			}
			String msisdn = in.getHeader(SmppConstants.DEST_ADDR, String.class);

			final String channelType = "SMS";
			String isSessionTerminated = in.getHeader(RoutingConstant.IS_SMS_SESSION_TERMINATED, String.class);
			LOGGER.debug("isSessionTerminated :" + isSessionTerminated);
			isSessionTerminated = (isSessionTerminated == null || "".equals(isSessionTerminated)) ? "true"
					: isSessionTerminated;
			/* Getting Data from App Cache */
			final FDPAppBag appBag1 = new FDPAppBag();
			if (circleCode != null) {
				appBag1.setSubStore(AppCacheSubStore.CIRCLE_CODE_CIRCLE_NAME_MAP);
				appBag1.setKey(circleCode);
			}
			final FDPCircle fdpCircle = (FDPCircle) applicationConfigCache.getValue(appBag1);
			final String circleName = fdpCircle.getCircleName();
			if (circleName == null) {
				throw new IllegalArgumentException("circleName is null");
			}

			String logicalName = null;
			String moduleName = null;
			final FDPAppBag appBag2 = new FDPAppBag();
			if (outGoingcircleCodeIPaddressPort != null) {
				AppCacheSubStore appCacheSubStoreKey ;
				if (externalSystemType.equals(ExternalSystem.AIR.name())) {
					appCacheSubStoreKey = AppCacheSubStore.AIRCONFEGURATION_MAP;
					appBag2.setSubStore(appCacheSubStoreKey);
					appBag2.setKey(outGoingcircleCodeIPaddressPort);
					LOGGER.debug("Key Maked :: " + outGoingcircleCodeIPaddressPort +" RID:"+requestId);
					final FDPAIRConfigDTO externalSystemCacheBean = (FDPAIRConfigDTO) applicationConfigCache
							.getValue(appBag2);
					logicalName = externalSystemCacheBean.getLogicalName();
					LOGGER.debug("Logical Name found:" + logicalName);
					moduleName = externalSystemCacheBean.getModuleType().name();
				} else if (externalSystemType.equals(ExternalSystem.CGW.name())) {
					appCacheSubStoreKey = AppCacheSubStore.CGWCONFEGURATION_MAP;
					appBag2.setSubStore(appCacheSubStoreKey);
					appBag2.setKey(outGoingcircleCodeIPaddressPort);
					final FDPCGWConfigDTO externalSystemCacheBean = (FDPCGWConfigDTO) applicationConfigCache
							.getValue(appBag2);
					logicalName = externalSystemCacheBean.getLogicalName();
					moduleName = externalSystemCacheBean.getModuleType().name();
				} else if (externalSystemType.equals(ExternalSystem.RS.name())) {
					appCacheSubStoreKey = AppCacheSubStore.RSCONFEGURATION_MAP;
					appBag2.setSubStore(appCacheSubStoreKey);
					appBag2.setKey(outGoingcircleCodeIPaddressPort);
					final FDPRSConfigDTO externalSystemCacheBean = (FDPRSConfigDTO) applicationConfigCache
							.getValue(appBag2);
					logicalName = externalSystemCacheBean.getLogicalName();
					moduleName = externalSystemCacheBean.getModuleType().name();
				}else if (externalSystemType.equals(ExternalSystem.CMS.name())) {
					appCacheSubStoreKey = AppCacheSubStore.CMSCONFEGURATION_MAP;
					appBag2.setSubStore(appCacheSubStoreKey);
					appBag2.setKey(outGoingcircleCodeIPaddressPort);
					final FDPCMSConfigDTO externalSystemCacheBean = (FDPCMSConfigDTO) applicationConfigCache
							.getValue(appBag2);
					logicalName = externalSystemCacheBean.getLogicalName();
					moduleName = externalSystemCacheBean.getModuleType().name();
				} else if (externalSystemType.equals(ExternalSystem.Loyalty.name())){
					appCacheSubStoreKey = AppCacheSubStore.LOYALTY_DETAILS;
					appBag2.setSubStore(appCacheSubStoreKey);
	                appBag2.setKey(outGoingcircleCodeIPaddressPort);
	                final FDPLoyaltyConfigDTO externalSystemCacheBean = (FDPLoyaltyConfigDTO) applicationConfigCache
							.getValue(appBag2);
	                logicalName = externalSystemCacheBean.getLogicalName();
	                moduleName = externalSystemCacheBean.getModuleType().name();
				
				} else if(externalSystemType.equals(ExternalSystem.MM.name())){

					appCacheSubStoreKey = AppCacheSubStore.MOBILEMONEY_DETAILS;
					appBag2.setSubStore(appCacheSubStoreKey);
	                appBag2.setKey(outGoingcircleCodeIPaddressPort);
	                final MobileMoneySystemDetails externalSystemCacheBean = (MobileMoneySystemDetails) applicationConfigCache
							.getValue(appBag2);
	                logicalName = externalSystemCacheBean.getLogicalName();
	                moduleName = externalSystemCacheBean.getExternalSystem().name();
				}
				
				else if (externalSystemType.equals(RoutingConstant.SERVICE_TYPE_SMS)) {
					appCacheSubStoreKey = AppCacheSubStore.SMS_USSD_ROUTE_DETAIL;
					LOGGER.debug("Fetching for Key:"
									+ (outGoingcircleCodeIPaddressPort + RoutingConstant.COLON + SMPPBindNodeType.TX
											.getName()));
					appBag2.setSubStore(appCacheSubStoreKey);
					appBag2.setKey(outGoingcircleCodeIPaddressPort + RoutingConstant.COLON
							+ SMPPBindNodeType.TX.getName());
					final FDPSMSCConfigDTO externalSystemCacheBean = (FDPSMSCConfigDTO) applicationConfigCache
							.getValue(appBag2);
					logicalName = externalSystemCacheBean.getLogicalName();
					moduleName = BusinessModuleType.SMSC_SOUTH.name();
					if (externalSystemType.equals(RoutingConstant.SERVICE_TYPE_SMS)) {
						if ("true".equals(isSessionTerminated)) {
							LOGGER.debug("Mt Originating address is : {}",
									externalSystemCacheBean.getMtOriginatingAddress());
							in.setHeader(SmppConstants.SOURCE_ADDR, externalSystemCacheBean.getMtOriginatingAddress());
						}
					}
				} else if (externalSystemType.equals(ExternalSystem.EMA.name())
						||externalSystemType.equals(ExternalSystem.MCARBON.name())
						||externalSystemType.equals(ExternalSystem.MANHATTAN.name())
						||externalSystemType.equals(ExternalSystem.SBBB.name())) {
					msisdn = exchange.getProperty(RoutingConstant.MSISDN, String.class);
					logicalName = exchange.getProperty(RoutingConstant.LOGICAL_NAME, String.class);
					moduleName = exchange.getProperty(RoutingConstant.MODULE_NAME, String.class);
				} else if (externalSystemType.equals(ExternalSystem.DMC.name())) {
					// changes by EOBEMAN
					appCacheSubStoreKey = AppCacheSubStore.DMCCONFEGURATION_MAP;
					appBag2.setSubStore(appCacheSubStoreKey);
					appBag2.setKey(outGoingcircleCodeIPaddressPort);
					final FDPDMCConfigDTO externalSystemCacheBean = (FDPDMCConfigDTO) applicationConfigCache
							.getValue(appBag2);
					logicalName = externalSystemCacheBean.getLogicalName();
					moduleName = externalSystemCacheBean.getModuleType().name();
					msisdn = exchange.getProperty(RoutingConstant.MSISDN, String.class);
                } else if (externalSystemType.equals(ExternalSystem.Ability.name())) {
                    appCacheSubStoreKey = AppCacheSubStore.ABILITY_DETAILS;
                    appBag2.setSubStore(appCacheSubStoreKey);
                    appBag2.setKey(outGoingcircleCodeIPaddressPort);
                    final FDPAbilityConfigDTO externalSystemCacheBean = (FDPAbilityConfigDTO) applicationConfigCache.getValue(appBag2);
                    logicalName = externalSystemCacheBean.getLogicalName();
                    moduleName = externalSystemCacheBean.getModuleType().name();
                    msisdn = exchange.getProperty(RoutingConstant.MSISDN, String.class);
                }else if (externalSystemType.equals(ExternalSystem.ADC.name())) {
					appCacheSubStoreKey = AppCacheSubStore.ADCCONFEGURATION_MAP;
					appBag2.setSubStore(appCacheSubStoreKey);
					appBag2.setKey(outGoingcircleCodeIPaddressPort);
					final FDPADCConfigDTO externalSystemCacheBean = (FDPADCConfigDTO) applicationConfigCache
							.getValue(appBag2);
					logicalName = externalSystemCacheBean.getLogicalName();
					moduleName = externalSystemCacheBean.getModuleType().name();
				}

				if (moduleName != null && !moduleName.isEmpty()) {
					final Logger circleLoggerRequest = FDPLoggerFactory.getRequestLogger(circleName, moduleName);
					String outputResultSuccessStatus;
					String outputResultStatusSmsUssd;
					ManageRequestCount requestCountFromThreadLocal = REQUEST_ID_LOCAL.get();

					if (requestCountFromThreadLocal == null) {

						requestCountFromThreadLocal = new ManageRequestCount();
						requestCountFromThreadLocal.setRequestId(requestId);
						REQUEST_ID_LOCAL.set(requestCountFromThreadLocal);
					}

					if (requestCountFromThreadLocal.getRequestId().equals(requestId)
							&& requestCountFromThreadLocal.getSuccessCount() > 0) {
						outputResultSuccessStatus = FDPConstant.IFRQMODE + FDPConstant.LOGGER_KEY_VALUE_DELIMITER
								+ FDPConstant.OUTPUT_FAILOVER;
						outputResultStatusSmsUssd = FDPConstant.ORESPRSLT + FDPConstant.LOGGER_KEY_VALUE_DELIMITER
								+ FDPConstant.OUTPUT_FAILOVER;
						requestCountFromThreadLocal.setSuccessCount(requestCountFromThreadLocal.getSuccessCount()+1);
					} else {
						int successCount = 0;
						requestCountFromThreadLocal.setRequestId(requestId);
						requestCountFromThreadLocal.setSuccessCount(successCount++);
						REQUEST_ID_LOCAL.set(requestCountFromThreadLocal);

						outputResultSuccessStatus = FDPConstant.IFRQMODE + FDPConstant.LOGGER_KEY_VALUE_DELIMITER
								+ FDPConstant.RESULT_REQUEST;
						outputResultStatusSmsUssd = FDPConstant.OREQMODE + FDPConstant.LOGGER_KEY_VALUE_DELIMITER
								+ FDPConstant.RESULT_REQUEST;
					}

					LOGGER.debug("External System Type is {}", externalSystemType);
					final StringBuilder finalLogs = new StringBuilder();
					if (externalSystemType.equals(RoutingConstant.SERVICE_TYPE_SMS)) {
						finalLogs.append(RoutingConstant.REQUEST_ID).append(FDPConstant.LOGGER_KEY_VALUE_DELIMITER)
								.append(requestId).append(FDPConstant.LOGGER_DELIMITER)
								.append(RoutingConstant.OUTGOING_IP_ADDRESS)
								.append(FDPConstant.LOGGER_KEY_VALUE_DELIMITER).append(outgoingIpAddress)
								.append(FDPConstant.LOGGER_DELIMITER).append(RoutingConstant.LOGICAL_NAME)
								.append(FDPConstant.LOGGER_KEY_VALUE_DELIMITER).append(logicalName)
								.append(FDPConstant.LOGGER_DELIMITER).append(FDPConstant.CHANNEL_TYPE)
								.append(FDPConstant.LOGGER_KEY_VALUE_DELIMITER).append(channelType)
								.append(FDPConstant.LOGGER_DELIMITER).append(FDPConstant.MSISDN)
								.append(FDPConstant.LOGGER_KEY_VALUE_DELIMITER).append(msisdn)
								.append(FDPConstant.LOGGER_DELIMITER).append(outputResultStatusSmsUssd);
						FDPLogger.info(circleLoggerRequest, getClass(), "process()", finalLogs.toString());
						FDPLogger.info(
								circleLoggerRequest,
								getClass(),
								"process()",
								new StringBuilder(RoutingConstant.REQUEST_ID)
										.append(FDPConstant.LOGGER_KEY_VALUE_DELIMITER).append(requestId)
										.append(FDPConstant.LOGGER_DELIMITER).append(FDPConstant.INRESULT)
										.append(FDPConstant.LOGGER_KEY_VALUE_DELIMITER)
										.append(FDPConstant.RESULT_SUCCESS).toString());
					} else {
						finalLogs.append(RoutingConstant.REQUEST_ID).append(FDPConstant.LOGGER_KEY_VALUE_DELIMITER)
								.append(requestId).append(FDPConstant.LOGGER_DELIMITER)
								.append(FDPConstant.CHARGING_NODE_IP).append(FDPConstant.LOGGER_KEY_VALUE_DELIMITER)
								.append(outgoingIpAddress).append(FDPConstant.LOGGER_DELIMITER)
								.append(FDPConstant.INTERFACE_TYPE).append(FDPConstant.LOGGER_KEY_VALUE_DELIMITER)
								.append(externalSystemType).append(FDPConstant.LOGGER_DELIMITER)
								.append(FDPConstant.CHARGING_NODE).append(FDPConstant.LOGGER_KEY_VALUE_DELIMITER)
								.append(logicalName).append(FDPConstant.LOGGER_DELIMITER)
								.append(outputResultSuccessStatus);
						FDPLogger.info(circleLoggerRequest, getClass(), "process()", finalLogs.toString());
					}
				}
			}
		} catch (final Exception e) {
			LOGGER.error(e.getMessage(), e);
		}
	}
}
