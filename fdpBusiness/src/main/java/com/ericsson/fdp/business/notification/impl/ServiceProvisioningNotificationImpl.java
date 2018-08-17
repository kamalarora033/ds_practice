package com.ericsson.fdp.business.notification.impl;

import java.util.Map;

import ch.qos.logback.classic.Logger;

import com.ericsson.fdp.business.enums.AuxRequestParam;
import com.ericsson.fdp.business.exception.NotificationFailedException;
import com.ericsson.fdp.business.notification.FDPServiceProvisioningNotification;
import com.ericsson.fdp.business.util.CommandUtil;
import com.ericsson.fdp.business.util.LoggerUtil;
import com.ericsson.fdp.business.util.NotificationUtil;
import com.ericsson.fdp.business.util.TariffEnquiryNotificationUtil;
import com.ericsson.fdp.common.constants.FDPConstant;
import com.ericsson.fdp.common.enums.ChannelType;
import com.ericsson.fdp.common.enums.FDPSPNotificationParamEnum;
import com.ericsson.fdp.common.enums.Status;
import com.ericsson.fdp.core.command.FDPCommand;
import com.ericsson.fdp.core.exception.ExecutionFailedException;
import com.ericsson.fdp.core.request.FDPRequest;
import com.ericsson.fdp.core.request.impl.FDPRequestImpl;
import com.ericsson.fdp.dao.enums.LanguageType;
import com.ericsson.fdp.dao.enums.SPNotificationType;

/**
 * This class implements the service provisioning notification. It provides the
 * method to send notification in case of service provisioning.
 *
 * @author Ericsson
 *
 */
public class ServiceProvisioningNotificationImpl extends NotificationImpl implements FDPServiceProvisioningNotification {

	/**
	 *
	 */
	private static final long serialVersionUID = 1251072143282714145L;

	/**
	 * The map containing status and the notification id.
	 */
	private final Map<SPNotificationType, Long> notifications;

	/**
	 * The channel type on which the notification is to be sent.
	 */
	
	private ChannelType channelType;
	
	/**
	 * The map containing language type and map which contains notification type and notification id.
	 * 
	 * 
	 * .
	 */
	private Map<LanguageType,Map<SPNotificationType, Long>> otherLanguageNotifications;

	/**
	 * The circle logger.
	 */
	private Logger circleLogger = null;
	
	/** the FDPSPNotificationParamEnum for selected SP notification param */
	private FDPSPNotificationParamEnum spNotificationParam;

	/**
	 * The constructor for service provisioning notification impl.
	 *
	 * @param notificationsToSet
	 *            The notification value to set
	 */
	/**
	 * The constructor for service provisioning notification impl.
	 * 
	 * @param notificationsToSet
	 *            The notification value to set
	 */
	public ServiceProvisioningNotificationImpl(final Map<SPNotificationType, Long> notificationsToSet,final Map<LanguageType,Map<SPNotificationType, Long>> otherLanguageNotifications) {
		this.notifications = notificationsToSet;
		this.otherLanguageNotifications=otherLanguageNotifications;
	}

	/*
	public ServiceProvisioningNotificationImpl(final Map<SPNotificationType, Long> notificationsToSet) {
		this.notifications = notificationsToSet;
	}
*/
	public ServiceProvisioningNotificationImpl(final Map<SPNotificationType, Long> notificationsToSet,
			final ChannelType channelType) {
		this.notifications = notificationsToSet;
		this.channelType = channelType;
	}

	/**
	 * This oreridden constructor is used for additional FDPSPNotificationParamEnum parameter
	 * @param notificationsToSet
	 * @param otherLanguageNotifications
	 * @param spNotificationParam
	 */
	public ServiceProvisioningNotificationImpl(final Map<SPNotificationType, Long> notificationsToSet,
			final Map<LanguageType,Map<SPNotificationType, Long>> otherLanguageNotifications , 
			FDPSPNotificationParamEnum spNotificationParam) {
		
		this.notifications = notificationsToSet;
		this.otherLanguageNotifications=otherLanguageNotifications;
		this.spNotificationParam = spNotificationParam;
	}
	
	@Override
	public String createNotificationText(final FDPRequest fdpRequest, final Status status)
			throws NotificationFailedException {
		return createNotificationText(fdpRequest, status, false);
	}

	public ChannelType getChannelType() {
		return channelType;
	}

	@Override
	public String createNotificationText(final FDPRequest fdpRequest, final Status executeStatus,
			final boolean checkChannel) throws NotificationFailedException {
		circleLogger = LoggerUtil.getSummaryLoggerFromRequest(fdpRequest);
		mergeNotificationMaps(fdpRequest);
		Long notificationId = notifications.get(SPNotificationType.FAILURE);
		try {
			if (Status.SUCCESS.equals(executeStatus)) {
				Object skipRsCharing = fdpRequest.getAuxiliaryRequestParameter(AuxRequestParam.SKIP_RS_CHARGING);
				if(null != skipRsCharing) {
					notificationId = FDPConstant.FDP_ONLINE_RS_RENEWAL_NOTIFICATION_ID-(fdpRequest.getCircle().getCircleId());
				} 
				else {
					if (checkChannel && ChannelType.USSD.equals(fdpRequest.getChannel())) {
					//	notificationId = notifications.get(SPNotificationType.SUCCESS_ON_USSD);
					notificationId = (null != fdpRequest.getSimLangauge() && fdpRequest.getSimLangauge().isOtherLang() && null != otherLanguageNotifications.get(fdpRequest.getSimLangauge()) && null != otherLanguageNotifications.get(fdpRequest.getSimLangauge()).get(SPNotificationType.SUCCESS_ON_USSD)) ? otherLanguageNotifications.get(fdpRequest.getSimLangauge()).get(SPNotificationType.SUCCESS_ON_USSD) : notifications.get(SPNotificationType.SUCCESS_ON_USSD);
					} else {
					//	notificationId = notifications.get(SPNotificationType.SUCCESS);
						notificationId = (null != fdpRequest.getSimLangauge() && fdpRequest.getSimLangauge().isOtherLang() && null != otherLanguageNotifications.get(fdpRequest.getSimLangauge()) && null != otherLanguageNotifications.get(fdpRequest.getSimLangauge()).get(SPNotificationType.SUCCESS)) ? otherLanguageNotifications.get(fdpRequest.getSimLangauge()).get(SPNotificationType.SUCCESS) : notifications.get(SPNotificationType.SUCCESS);
						// Set Beneficiary notification in fdprequest in case beneficiary number is set
						Object beneficiaryMsisdnObject = fdpRequest.getAuxiliaryRequestParameter(AuxRequestParam.BENEFICIARY_MSISDN);
						if(null != beneficiaryMsisdnObject){
							Long beneficiaryNotificationId = notifications.get(SPNotificationType.SUCCESS_OTHER_ON_USSD);
							if(beneficiaryNotificationId != null){
								String beneficiaryNotificationText = TariffEnquiryNotificationUtil.createNotificationText(fdpRequest, beneficiaryNotificationId, circleLogger);
								((FDPRequestImpl)fdpRequest).putAuxiliaryRequestParameter(AuxRequestParam.BENEFICIARY_NOTIFICATION, beneficiaryNotificationText);
							}else{
								circleLogger.debug(LoggerUtil.getRequestAppender(fdpRequest) + "Notification text not found for beneficiary Msisdn");
							}
						}
					}
					if(null != fdpRequest.getAuxiliaryRequestParameter(AuxRequestParam.isData2Share) ||
							null != fdpRequest.getAuxiliaryRequestParameter(AuxRequestParam.IS_TIME2SHARE)) {
						Long receiverNotifId = notifications.get(SPNotificationType.SUCCESS_ME2U_SUBSCRIBER);
						if(null != receiverNotifId) {
							String receiverNotifText = TariffEnquiryNotificationUtil.createNotificationText(fdpRequest, receiverNotifId, circleLogger);
							((FDPRequestImpl)fdpRequest).putAuxiliaryRequestParameter(AuxRequestParam.RECEIVER_NOTIFICATION, receiverNotifText);

						}
					}
					
				}
			} else if(Status.SUCESS_ON_MM.equals(executeStatus)){
				notificationId=FDPConstant.FDP_ONLINE_MM_NOTIFICATION_ID;
			}else {
				// The notification to send to subscriber in case normal notification is to be overridden
				Object notificationToOverride = fdpRequest.getAuxiliaryRequestParameter(AuxRequestParam.NOTIFICATION_TO_OVERRIDE);
				if(null != notificationToOverride && notificationToOverride instanceof Long){
					notificationId = getNotificationForOverrideToSupress(fdpRequest);
				}else{
					Long notificationIdUpdated = getNotificationId(fdpRequest);
					notificationId = (notificationIdUpdated == null ? notificationId : notificationIdUpdated);
				}
			}
            return TariffEnquiryNotificationUtil.createNotificationText(fdpRequest, notificationId, circleLogger, spNotificationParam);
		} catch (ExecutionFailedException e) {
			throw new NotificationFailedException("Cannot find notification", e);
		}
	}

	/**
	 * Gets the notification id.
	 *
	 * @param fdpRequest            the fdp request
	 * @return the notification id
	 * @throws ExecutionFailedException the execution failed exception
	 */
	private Long getNotificationId(final FDPRequest fdpRequest) throws ExecutionFailedException {
		FDPCommand lastExecutedCommand = fdpRequest.getLastExecutedCommand();
		return lastExecutedCommand == null ? null : NotificationUtil.getNotificationIdForCommand(fdpRequest.getCircle(), lastExecutedCommand);
	}

	/**
	 * @return the otherLanguageNotifications
	 */
	public Map<LanguageType, Map<SPNotificationType, Long>> getOtherLanguageNotifications() {
		return otherLanguageNotifications;
	}

	/**
	 * @param otherLanguageNotifications the otherLanguageNotifications to set
	 */
	public void setOtherLanguageNotifications(
			Map<LanguageType, Map<SPNotificationType, Long>> otherLanguageNotifications) {
		this.otherLanguageNotifications = otherLanguageNotifications;
	}
	
	
	public String createNotificationTextBeneficiary(final FDPRequest fdpRequest) throws NotificationFailedException{
		try {
			Long notificationId = notifications.get(SPNotificationType.SUCCESS_OTHER_ON_USSD);
			return TariffEnquiryNotificationUtil.createNotificationText(fdpRequest, notificationId, circleLogger);
		}catch (Exception e) {
			throw new NotificationFailedException("Cannot find notification", e);
		}
	}
	
	/**
	 * Create Notification for Async Commands for Success.
	 * 
	 * @param fdpRequest
	 * @return
	 * @throws NotificationFailedException
	 */
	public String createNotificationForAsyncSuccess(final FDPRequest fdpRequest) throws NotificationFailedException {
		circleLogger = LoggerUtil.getSummaryLoggerFromRequest(fdpRequest);
		return TariffEnquiryNotificationUtil.createNotificationText(fdpRequest, notificationIdForAysncCommand(fdpRequest), circleLogger);
	}
	
	/**
	 * Find the notificationId.
	 * 
	 * @param fdpRequest
	 * @return
	 * @throws NotificationFailedException
	 */
	private Long notificationIdForAysncCommand(final FDPRequest fdpRequest) throws NotificationFailedException {
		Long notificationId =0L;
		try {
			final FDPCommand command = fdpRequest.getLastExecutedCommand();
			notificationId = CommandUtil.getNotificationIdAsycCommand(fdpRequest, command.getCommandDisplayName());
			
		} catch (final ExecutionFailedException e) {
			throw new NotificationFailedException("Unable to find notification Id for command.getCommandDisplayName(), Actual Error:",e);
		}
		return notificationId;
	}
	private Long getNotificationForOverrideToSupress(final FDPRequest fdpRequest) {
		return ((Long)(fdpRequest.getAuxiliaryRequestParameter(AuxRequestParam.NOTIFICATION_TO_OVERRIDE)));
	}
	
	/**
	 * Override the notifications.
	 * 
	 * @param fdpRequest
	 */
	@SuppressWarnings("unchecked")
	private void mergeNotificationMaps(final FDPRequest fdpRequest) {
		if(null != fdpRequest.getAuxiliaryRequestParameter(AuxRequestParam.NOTIFICATION_TO_OVERRIDE) && fdpRequest.getAuxiliaryRequestParameter(AuxRequestParam.NOTIFICATION_TO_OVERRIDE) instanceof Map<?, ?>) {
			final Map<SPNotificationType, Long> toOverrideNotificationMap = ((Map<SPNotificationType, Long>) fdpRequest.getAuxiliaryRequestParameter(AuxRequestParam.NOTIFICATION_TO_OVERRIDE));
			for(final SPNotificationType notificationType : notifications.keySet()) {
				if(null != toOverrideNotificationMap.get(notificationType)) {
					notifications.put(notificationType, toOverrideNotificationMap.get(notificationType));
				}
			}
		}
	}
}
