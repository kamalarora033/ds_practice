package com.ericsson.fdp.business.route.processor;

import javax.annotation.Resource;

import org.apache.camel.Exchange;
import org.apache.camel.Message;
import org.apache.camel.Processor;
import org.apache.camel.component.smpp.SmppConstants;
import org.slf4j.Logger;

import com.ericsson.fdp.business.constants.BusinessConstants;
import com.ericsson.fdp.common.constants.FDPConstant;
import com.ericsson.fdp.common.vo.FDPCircle;
import com.ericsson.fdp.core.cache.AppCacheSubStore;
import com.ericsson.fdp.core.dsm.framework.service.FDPCache;
import com.ericsson.fdp.core.logging.FDPLogger;
import com.ericsson.fdp.core.logging.FDPLoggerFactory;
import com.ericsson.fdp.core.serviceprovisioning.bean.FDPAppBag;
import com.ericsson.fdp.dao.dto.FDPSMSCConfigDTO;

public class LogTransceiverProcessor implements Processor{

	/** The request cache. */
	@Resource(lookup = "java:app/fdpCoreServices-1.0/ApplicationConfigCache")
	private FDPCache<FDPAppBag, Object> applicationConfigCache;

	@Override
	public void process(Exchange exchange) throws Exception {
		Message in = exchange.getIn();
		String incomingIpPort = in.getHeader(BusinessConstants.INCOMING_TRX_IP_PORT, String.class);
		String outGoingIPaddress = incomingIpPort
				.substring(0, incomingIpPort.lastIndexOf(BusinessConstants.UNDERSCORE));
		String circleCode = exchange.getProperty(BusinessConstants.CIRCLE_CODE, String.class);
		String serviceType = in.getHeader(SmppConstants.SERVICE_TYPE, String.class);
		String channelType = "USSD";
		if (serviceType.equals(BusinessConstants.SERVICE_TYPE_SMS)) {
			channelType = "SMS";
		}
		String msisdn = in.getHeader(SmppConstants.DEST_ADDR, String.class);
		String circleCodeIncomingIpAddressPortUsername = in.getHeader(
				BusinessConstants.INCOMING_IP_ADDRESS_PORT_USERNAME, String.class);

		FDPAppBag appBag1 = new FDPAppBag();
		appBag1.setSubStore(AppCacheSubStore.SMSCONFEGURATION_MAP);
		appBag1.setKey(circleCodeIncomingIpAddressPortUsername + BusinessConstants.COLON
				+ BusinessConstants.BIND_NODE_TYPE_TRANSCEIVER);
		FDPSMSCConfigDTO fdpsmscConfigDTO = (FDPSMSCConfigDTO) applicationConfigCache.getValue(appBag1);
		String logicalName = fdpsmscConfigDTO.getLogicalName();
		String moduleName = fdpsmscConfigDTO.getModuleType().name();

		FDPAppBag appBag2 = new FDPAppBag();
		if (circleCode != null) {
			appBag2.setSubStore(AppCacheSubStore.CIRCLE_CODE_CIRCLE_NAME_MAP);
			appBag2.setKey(circleCode);
		}

		FDPCircle fdpCircle = (FDPCircle) applicationConfigCache.getValue(appBag2);
		String circleName = fdpCircle.getCircleName();
		if (circleName == null) {
			throw new IllegalArgumentException("circleName is null");
		}

		String requestId = exchange.getProperty(BusinessConstants.REQUEST_ID, String.class);
		if (requestId == null) {
			throw new IllegalArgumentException("requestId is null");
		}

		Logger circleLoggerRequest = FDPLoggerFactory.getRequestLogger(circleName, moduleName);
		FDPLogger.info(circleLoggerRequest, getClass(), "process()", BusinessConstants.REQUEST_ID
				+ FDPConstant.LOGGER_KEY_VALUE_DELIMITER + requestId + FDPConstant.LOGGER_DELIMITER
				+ BusinessConstants.OUTGOING_IP_ADDRESS + FDPConstant.LOGGER_KEY_VALUE_DELIMITER + outGoingIPaddress
				+ FDPConstant.LOGGER_DELIMITER + BusinessConstants.LOGICAL_NAME
				+ FDPConstant.LOGGER_KEY_VALUE_DELIMITER + logicalName + FDPConstant.LOGGER_DELIMITER
				+ FDPConstant.CHANNEL_TYPE + FDPConstant.LOGGER_KEY_VALUE_DELIMITER + channelType
				+ FDPConstant.LOGGER_DELIMITER + FDPConstant.MSISDN + FDPConstant.LOGGER_KEY_VALUE_DELIMITER + msisdn
				+ FDPConstant.LOGGER_DELIMITER + FDPConstant.OREQMODE + FDPConstant.LOGGER_KEY_VALUE_DELIMITER
				+ FDPConstant.RESULT_SUCCESS);
	}

}
