package com.ericsson.fdp.business.util;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import com.ericsson.fdp.business.cache.datageneration.CommandCacheUtil;
import com.ericsson.fdp.business.command.param.impl.CommandParamInput;
import com.ericsson.fdp.business.vo.FDPNotificationVO;
import com.ericsson.fdp.core.command.param.CommandParam;
import com.ericsson.fdp.dao.dto.FDPNotificationDTO;
import com.ericsson.fdp.dao.dto.NotificationParamDTO;
import com.ericsson.fdp.dao.enums.LanguageType;

public final class NotificationCacheUtil{

	private NotificationCacheUtil() {
	}

	public static FDPNotificationVO notificationDTOToNotificationVO(final FDPNotificationDTO noti) throws IOException {
		Map<String, CommandParam> params = null;
		params = getNotificationParameters(noti.getFdpnotificationParamMap().get(LanguageType.ENGLISH.getValue()));
		return new FDPNotificationVO(noti.getNotificationsId(), params, noti.getNotificationName(),
				noti.getNotificationText(), noti.getType(), noti.getSubType());
	}

	private static Map<String, CommandParam> getNotificationParameters(
			final Set<NotificationParamDTO> set) {
		Map<String, CommandParam> params=new HashMap<String, CommandParam>();
		for(NotificationParamDTO param : set) {
			CommandParamInput commandParamInput = getCommandParam(param);
			params.put(param.getParamName(), commandParamInput);
		}
		return params;
	}

	private static CommandParamInput getCommandParam(final NotificationParamDTO notificationParam) {
		CommandParamInput param=null;
		Object defValue = CommandCacheUtil.getDefValue(notificationParam.getFeedType(), notificationParam.getParamValue(), notificationParam.getTypeGUI());
		param = new CommandParamInput(notificationParam.getFeedType(), defValue);
		param.setName(notificationParam.getParamName());
		param.setType(notificationParam.getXmlType());
		param.setPrimitiveValue(notificationParam.getPrimitiveType());
		return param;
	}
	
	/**
	 * This method will prepare FDPNotificationVO (for english), which will contain map for <language and  FDPNotificationVO for other lang).
	 * 
	 * @param notificationDTOs
	 * @return
	 * @throws IOException
	 */
	public static FDPNotificationVO notificationDTOToNotificationVO(final Map<LanguageType,FDPNotificationDTO> notificationDTOs) throws IOException {
		FDPNotificationVO fdpNotificationVO = null;
		final FDPNotificationDTO notificationDTO = notificationDTOs.get(LanguageType.ENGLISH);
		if(null != notificationDTO) {
			fdpNotificationVO = notificationDTOToNotificationVO(notificationDTO,LanguageType.ENGLISH);
			for(final Map.Entry<LanguageType,FDPNotificationDTO> entry : notificationDTOs.entrySet()) {
				FDPNotificationVO fdpNotificationOtherLang = null; 
				if(!LanguageType.ENGLISH.equals(entry.getKey())) {
					fdpNotificationOtherLang = notificationDTOToNotificationVO(entry.getValue(),entry.getKey());
					fdpNotificationVO.addOtherLangNotificationMap(entry.getKey(), fdpNotificationOtherLang);
				}
			}
		}
		return fdpNotificationVO;
	}
	
	/**
	 * This method prepare the FDPNotificationVO to push in cache.
	 * 
	 * @param noti
	 * @param languageType
	 * @return
	 * @throws IOException
	 */
	public static FDPNotificationVO notificationDTOToNotificationVO(final FDPNotificationDTO noti, final LanguageType languageType) throws IOException {
		Map<String, CommandParam> params = null;
		params = getNotificationParameters(noti.getFdpnotificationParamMap().get(languageType.getValue()));
		return new FDPNotificationVO(noti.getNotificationsId(), params, noti.getNotificationName(),
				noti.getNotificationText(), noti.getType(), noti.getSubType());
	}
}
