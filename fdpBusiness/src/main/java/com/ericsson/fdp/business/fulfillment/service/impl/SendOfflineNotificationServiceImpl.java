package com.ericsson.fdp.business.fulfillment.service.impl;

import java.util.Map;

import javax.ejb.Stateless;

import com.ericsson.fdp.business.enums.ConfigurationKey;
import com.ericsson.fdp.business.request.impl.FDPMetadataResponseImpl;
import com.ericsson.fdp.business.request.impl.FulfillmentRequestImpl;
import com.ericsson.fdp.business.util.LoggerUtil;
import com.ericsson.fdp.business.util.NotificationUtil;
import com.ericsson.fdp.common.enums.Status;
import com.ericsson.fdp.common.util.StringUtil;
import com.ericsson.fdp.core.exception.ExecutionFailedException;
import com.ericsson.fdp.core.logging.FDPLogger;
import com.ericsson.fdp.core.request.FDPRequest;
import com.ericsson.fdp.core.request.FDPResponse;

@Stateless
public class SendOfflineNotificationServiceImpl extends AbstractFDPFulFillmentServiceImpl{

	@Override
	protected FDPResponse executeService(FDPRequest fdpRequest,
			Object... additionalInformations) throws ExecutionFailedException,
			NumberFormatException {
		final String notificationPlaceHolder = "$product_name";
		String notificationText = null;
		FulfillmentRequestImpl fulfillmentRequestImpl=(FulfillmentRequestImpl)fdpRequest;
		FDPResponse fdpResponse = new FDPMetadataResponseImpl(Status.FAILURE, true, null) ;
		final Map<String, String> configurationMap = fdpRequest.getCircle().getConfigurationKeyValueMap();
		
		final String smsNotificationText = configurationMap
				.get(ConfigurationKey.SMS_FAIL_NOTIFICATION_COD.getAttributeName());
		if(null != smsNotificationText && !StringUtil.isNullOrEmpty(fulfillmentRequestImpl.getRequestString()) && smsNotificationText.contains(notificationPlaceHolder)) {
			notificationText=smsNotificationText.replace(notificationPlaceHolder, fulfillmentRequestImpl.getRequestString());
		} else {
			notificationText = "Unable to process your COD request for product.Please buy the product using *129# store.Sorry for inconvenience.";
		}
		FDPLogger.debug(
				getCircleLogger(fdpRequest),
				 			getClass(),
				 				"executeService",
				 				LoggerUtil.getRequestAppender(fdpRequest) + "Final Notification text:" +notificationText +" , requestId:"
				 						+ fdpRequest.getRequestId());
		NotificationUtil.sendOfflineNotification(fdpRequest, notificationText);
		FDPMetadataResponseImpl metadataResponseImpl = (FDPMetadataResponseImpl) fdpResponse;
		metadataResponseImpl.setExecutionStatus(Status.SUCCESS);
		return fdpResponse;
	}

}
