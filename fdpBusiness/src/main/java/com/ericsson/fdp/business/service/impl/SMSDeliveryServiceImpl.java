package com.ericsson.fdp.business.service.impl;

import java.net.Inet4Address;
import java.net.UnknownHostException;
import java.util.UUID;

import javax.annotation.Resource;
import javax.ejb.Stateless;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ericsson.fdp.common.enums.ChannelType;
import com.ericsson.fdp.common.exception.FDPServiceException;
import com.ericsson.fdp.core.dsm.framework.service.FDPCache;
import com.ericsson.fdp.core.dsm.framework.service.impl.FDPCircleCacheProducer;
import com.ericsson.fdp.core.serviceprovisioning.bean.FDPAppBag;
import com.ericsson.fdp.core.utils.CircleCodeFinder;
import com.ericsson.fdp.dao.dto.ExchangeMessageResponse;
import com.ericsson.fdp.route.request.service.SMSDeliveryService;

/**
 * The Class SMSDeliveryServiceImpl.
 * 
 * @author Ericsson
 */
@Stateless
public class SMSDeliveryServiceImpl implements SMSDeliveryService {

	/** The circle cache producer. */
	@Resource(lookup = "java:app/fdpCoreServices-1.0/FDPCircleCacheProducer")
	private FDPCircleCacheProducer circleCacheProducer;

	/** The application config cache. */
	@Resource(lookup = "java:app/fdpCoreServices-1.0/ApplicationConfigCache")
	private FDPCache<FDPAppBag, Object> applicationConfigCache;

	/** The Constant LOGGER. */
	private static final Logger LOGGER = LoggerFactory.getLogger(SMSDeliveryServiceImpl.class);

	/**
	 * Send message.
	 * 
	 * @param msisdn
	 *            the msisdn
	 * @param msg
	 *            the msg
	 * @throws UnknownHostException
	 *             the unknown host exception
	 */
	@Override
	public void sendMessage(final String msisdn, final String msg) throws FDPServiceException {
		LOGGER.debug("Sending message to msisdn : {}", msisdn);
		try {
			final String circleCode = CircleCodeFinder.getCircleCode(msisdn, applicationConfigCache);
			final ExchangeMessageResponse message = new ExchangeMessageResponse();
			message.setExternalSystemType(ChannelType.SMS.getName());
			message.setMsisdn(msisdn);
			message.setServiceModeType("WAP");
			message.setBody(msg);
			message.setCircleId(circleCode);
			String requestId;
			requestId = ChannelType.SMS.getName() + "_" + Inet4Address.getLocalHost().getHostAddress() + "_"
					+ (String.valueOf(UUID.randomUUID()));
			message.setRequestId(requestId);
			message.setIncomingTrxIpPort(Inet4Address.getLocalHost().getHostAddress());
			circleCacheProducer.pushToQueue(message, circleCode, applicationConfigCache);
			LOGGER.debug("Sending message to msisdn : {} DONE.", msisdn);
		} catch (final UnknownHostException e) {
			LOGGER.error("Unable to send message to " + msisdn + ". Exception Occured : {}", e);
			throw new FDPServiceException(e);
		}
	}

}
