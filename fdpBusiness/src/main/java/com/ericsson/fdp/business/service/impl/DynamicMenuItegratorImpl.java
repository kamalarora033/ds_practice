package com.ericsson.fdp.business.service.impl;

import javax.annotation.Resource;
import javax.ejb.EJB;
import javax.ejb.Stateless;
import javax.inject.Inject;

import org.apache.activemq.ScheduledMessage;
import org.apache.camel.Endpoint;
import org.apache.camel.Exchange;
import org.apache.camel.ExchangePattern;
import org.apache.camel.Message;
import org.apache.camel.RuntimeCamelException;
import org.slf4j.Logger;

import com.ericsson.fdp.business.constants.BusinessConstants;
import com.ericsson.fdp.business.service.DynamicMenuItegrator;
import com.ericsson.fdp.common.constants.FDPConstant;
import com.ericsson.fdp.common.enums.BusinessModuleType;
import com.ericsson.fdp.common.vo.FDPCircle;
import com.ericsson.fdp.core.cache.AppCacheSubStore;
import com.ericsson.fdp.core.cache.service.SMPPServerMappingService;
import com.ericsson.fdp.core.config.utils.PropertyUtils;
import com.ericsson.fdp.core.dsm.framework.service.FDPCache;
import com.ericsson.fdp.core.logging.FDPLogger;
import com.ericsson.fdp.core.logging.FDPLoggerFactory;
import com.ericsson.fdp.core.logging.USSDTrafficLoggerPosition;
import com.ericsson.fdp.core.serviceprovisioning.bean.FDPAppBag;
import com.ericsson.fdp.dao.dto.ExchangeMessageResponse;
import com.ericsson.fdp.dao.fdpbusiness.FDPSMSCConfigTLVParametersDAO;
import com.ericsson.fdp.route.cdi.context.provider.CdiCamelContextProvider;
import com.ericsson.fdp.route.constant.RoutingConstant;
import com.ericsson.fdp.route.enumeration.SMPPBindNodeType;
import com.ericsson.fdp.route.request.service.RequestService;
import com.ericsson.fdp.smpp.util.SMPPUtil;

/**
 * The Class DynamicMenuItegrator.
 * 
 * @author Ericsson
 */
@Stateless
public class DynamicMenuItegratorImpl implements DynamicMenuItegrator {

	/** The cdi camel context provider. */
	@Inject
	private CdiCamelContextProvider cdiCamelContextProvider;

	/** The fdpsmsc config dao. */
	@Resource(lookup = "java:app/fdpCoreServices-1.0/SMPPServerMappingServiceImpl")
	private SMPPServerMappingService serverMappingService;

	/** The fdpsmsc config tlv parameters dao. */
	@Inject
	private FDPSMSCConfigTLVParametersDAO fdpsmscConfigTLVParametersDAO;

	/** The request service. */
	@EJB(beanName="PushToUSSDOutQueueServiceImpl")
	private RequestService requestService;

	/** The request cache. */
	@Resource(lookup = "java:app/fdpCoreServices-1.0/ApplicationConfigCache")
	private FDPCache<FDPAppBag, Object> applicationConfigCache;

	/**
	 * Send submit sm in out.
	 * 
	 * @param exchangeMessageResponse
	 *            is used to Messages Class object.
	 * @throws Exception .
	 */
	@Override
	public void sendSubmitSmInOut(final ExchangeMessageResponse exchangeMessageResponse) throws Exception {

		Exchange exchange = null;
		String circleName = null;
		String requestId = null;
		try {
			final String circleCode = exchangeMessageResponse.getCircleId();
			final String msisdn = exchangeMessageResponse.getMsisdn();
			final String routeId = exchangeMessageResponse.getRouteId();
			final String serviceType = exchangeMessageResponse.getMsgType();
			final String sourceAdd = exchangeMessageResponse.getSourceAddress();
			final String exchangeBodyResponse = exchangeMessageResponse.getBody();
			final boolean isSessionTerminated = exchangeMessageResponse.isTerminated();
			requestId = exchangeMessageResponse.getRequestId();
			final String bindModeFromExchange = exchangeMessageResponse.getBindMode();
			final String optionalParameterString = exchangeMessageResponse.getOptionalParameters();
			final Long delay = exchangeMessageResponse.getDelayTime();
			/**
			 * Getting appcache for circle name according to the code for circle
			 * level logging.
			 */
			final FDPAppBag appBag = new FDPAppBag();
			appBag.setSubStore(AppCacheSubStore.CIRCLE_CODE_CIRCLE_NAME_MAP);
			appBag.setKey(circleCode);
			final FDPCircle fdpCircle = (FDPCircle) applicationConfigCache.getValue(appBag);
			circleName = fdpCircle.getCircleName();
			final Logger circleLoggerTrace = FDPLoggerFactory.getTraceLogger(circleName,
					BusinessModuleType.SMSC_SOUTH.name());
			final Logger circleLoggerRequest = FDPLoggerFactory.getRequestLogger(circleName,
					BusinessModuleType.SMSC_SOUTH.name());

			String compiledQueueName = null;
			if (serviceType.equals(BusinessConstants.SERVICE_TYPE_SMS)) {
				compiledQueueName = SMPPUtil.getSMSOutQueue(circleCode);
			} else {
				compiledQueueName = SMPPUtil.getUSSDOutQueueEndpoint(circleCode);
			}
			FDPLogger.info(circleLoggerTrace, getClass(), "process()",
					String.format("From Class %s | Complied Queue Name: %s", this.getClass(), compiledQueueName));

			final Endpoint start = cdiCamelContextProvider.getContext().getEndpoint(compiledQueueName);
			exchange = start.createExchange(ExchangePattern.InOnly);
			final Message in = exchange.getIn();

			// TODO : move this to processor
			if (serviceType.equals(BusinessConstants.SERVICE_TYPE_USSD)) {
				in.setHeader(RoutingConstant.ROUTE_ID, routeId);
				in.setHeader(RoutingConstant.BIND_MODE, SMPPBindNodeType.TRX.getName());
			}

			exchangeMessageResponse.setCommand(BusinessConstants.SUBMIT_SM);
			exchangeMessageResponse.setSourceAddress(sourceAdd);
			exchangeMessageResponse.setDestinationAddress(msisdn);
			exchangeMessageResponse.setServiceModeType(serviceType);
			exchangeMessageResponse.setBindMode(bindModeFromExchange);
			exchangeMessageResponse.setBody(exchangeBodyResponse);
			exchangeMessageResponse.setCircleId(circleCode);
			in.setHeader(BusinessConstants.BIND_MODE, bindModeFromExchange);
			in.setHeader(BusinessConstants.SERVICE_TYPE, serviceType);
			if (delay != null && delay > 0L) {
				//in.setHeader(BusinessConstants._HQ_SCHED_DELIVERY, System.currentTimeMillis()+delay);
				in.setHeader(ScheduledMessage.AMQ_SCHEDULED_DELAY, delay);
			}
			if (isSessionTerminated && serviceType.equals(BusinessConstants.SERVICE_TYPE_SMS)) {
				exchangeMessageResponse.setIsSessionTerminated("true");
			} else {
				exchangeMessageResponse.setIsSessionTerminated("false");
			}
			exchangeMessageResponse.setOptionalParameters(optionalParameterString);
			FDPLogger.info(circleLoggerTrace, getClass(), "process()",
					String.format("Seesion Termination : %s", isSessionTerminated));

			// Moved to Request Processor
			/*
			 * String ussdValue = "2"; if (isSessionTerminated) { ussdValue =
			 * "17"; } if ("USSD".equals(serviceType)) { attachTLVs(exchange,
			 * circleCode, ExternalSystemType.USSD_TYPE, circleLoggerTrace,
			 * isSessionTerminated); String tlvStr = "ITS_SESSION_INFO," +
			 * sessionId + ",USSD_SERVICE_OP,"+ussdValue;
			 * exchangeMessageResponse.setOptionalParameters(tlvStr); }
			 */
			in.setBody(exchangeMessageResponse);
			in.setHeader(RoutingConstant.ROUTE_ID, routeId);
            // in.setHeader(RoutingConstant.BIND_MODE, SMPPBindNodeType.TRX.getName());
            // in.setHeader(RoutingConstant.BIND_MODE, SMPPBindNodeType.TX.getBindMode().name());

            String smscBindMode = PropertyUtils.getProperty("smsc.bind.mode");
            smscBindMode = (smscBindMode != null && !"".equals(smscBindMode)) ? smscBindMode : BusinessConstants.BIND_MODE_TXRX;

            in.setHeader(RoutingConstant.BIND_MODE, smscBindMode);
			in.setHeader(RoutingConstant.REQUEST_ID, requestId);
			FDPLogger.info(circleLoggerTrace, getClass(), "process()",
					String.format("Is Conetext started : %s", cdiCamelContextProvider.getContext().isStarted()));
			FDPLogger.info(circleLoggerTrace, getClass(), "process()", "Optional Parameter Got from Request Processor:"
					+ optionalParameterString);
			// producer.start();
			FDPLogger.info(circleLoggerTrace, getClass(), "process()", "Invoking the smsc with message");
			FDPLoggerFactory.reportUssdTarfficReportLogger(getClass(), "sendSubmitSmInOut", requestId, msisdn, "Before Sending to OutBound Queue towards ussdAdapter", USSDTrafficLoggerPosition.BEFORE_EJB_LOOKUP_FROM_BUSINESS_TO_USSD_ADAPTER);
			if (serviceType.equals(BusinessConstants.SERVICE_TYPE_USSD)) {
				in.setHeader(RoutingConstant.CIRCLE_ID, circleCode);
				requestService.request(in.getBody(), in.getHeaders());
				FDPLoggerFactory.reportUssdTarfficReportLogger(getClass(), "sendSubmitSmInOut", requestId, msisdn, "After Sending to OutBound Queue towards ussdAdapter", USSDTrafficLoggerPosition.AFTER_EJB_LOOKUP_FROM_BUSINESS_TO_USSD_ADAPTER);
			} else {
				start.createProducer().process(exchange);
			}
			//System.out.println("Message Written " + exchange.getIn().getBody());
			//System.out.println("Queue " + compiledQueueName);
			// producer.stop();
			FDPLogger.info(circleLoggerTrace, getClass(), "process()", "Message sent Successfully | Exchange :"
					+ exchange.toString());
			/*
			 * FDPLogger.info(circleLoggerRequest, getClass(), "process()",
			 * FDPConstant.REQUEST_ID + FDPConstant.LOGGER_KEY_VALUE_DELIMITER +
			 * requestId + FDPConstant.LOGGER_DELIMITER + "OutTime" +
			 * FDPConstant.LOGGER_KEY_VALUE_DELIMITER + DateUtil.getDateTime());
			 */
		} catch (final Exception e) {
			//System.out.println("Exception at DynamicMenuItegratorImpl: "+e.getMessage());
			e.printStackTrace();
			final Logger circleLoggerRequest = FDPLoggerFactory.getRequestLogger(circleName,
					BusinessModuleType.SMSC_SOUTH.name());
			FDPLogger.error(circleLoggerRequest, getClass(), "process()", FDPConstant.REQUEST_ID
					+ FDPConstant.LOGGER_KEY_VALUE_DELIMITER + requestId + FDPConstant.LOGGER_DELIMITER
					+ FDPConstant.LOGGER_RESULT + FDPConstant.LOGGER_KEY_VALUE_DELIMITER + FDPConstant.RESULT_FAILURE);
			throw new RuntimeCamelException(e);
		}

	}

	/*private RequestService getRequestService() throws NamingException {
		if (requestService == null) {
			requestService = ApplicationConfigUtil.getRemoteUSSDAdapterBean("PushToUSSDOutQueueServiceImpl",
					RequestService.class);
		}
		return requestService;
	}*/
}
