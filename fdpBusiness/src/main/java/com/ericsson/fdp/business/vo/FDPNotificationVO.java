package com.ericsson.fdp.business.vo;

import java.io.IOException;
import java.io.StringReader;
import java.util.HashMap;
import java.util.Map;

import com.ericsson.fdp.FDPCacheable;
import com.ericsson.fdp.core.command.param.CommandParam;
import com.ericsson.fdp.dao.enums.LanguageType;

import freemarker.template.Configuration;
import freemarker.template.Template;

/**
 * This class is used to store the notification information of a product.
 *
 * @author Ericsson
 *
 */
public class FDPNotificationVO implements FDPCacheable {

	/** The Constant serialVersionUID. */
	private static final long serialVersionUID = -3511405404797255479L;

	/** The notification id. */
	private Long notificationId;

	/**
	 * The parameters to replace in the template.
	 */
	private Map<String, CommandParam> parametersToReplace;

	/**
	 * The message to be sent as notification.
	 */
	// private Template message;

	private final String name;

	/** The text. */
	private final String text;

	/** The type. */
	private Integer type;

	/** The sub type. */
	private Integer subType;
	
	/** The  NotificationMap for other Language. */
	private Map<LanguageType, FDPNotificationVO> otherLangNotificationMap;

	/**
	 * The constructor for notification dto.
	 *
	 * @param notificationId
	 *            the notification id
	 * @param parametersToReplaceToSet
	 *            The parameters to replace value.
	 * @param name
	 *            the name
	 * @param text
	 *            the text
	 */
	public FDPNotificationVO(final Long notificationId, final Map<String, CommandParam> parametersToReplaceToSet,
			final String name, final String text, final Integer type, final Integer subType) {
		this.notificationId = notificationId;
		this.parametersToReplace = parametersToReplaceToSet;
		this.name = name;
		this.text = text;
		this.type = type;
		this.subType = subType;
	}

	/**
	 * Gets the parameters to replace.
	 *
	 * @return the parametersToReplace
	 */
	public Map<String, CommandParam> getParametersToReplace() {
		return parametersToReplace;
	}

	/**
	 * Sets the parameters to replace.
	 *
	 * @param parametersToReplaceToSet
	 *            the parametersToReplace to set
	 */
	public void setParametersToReplace(final Map<String, CommandParam> parametersToReplaceToSet) {
		this.parametersToReplace = parametersToReplaceToSet;
	}

	/**
	 * Gets the message.
	 *
	 * @return the message
	 * @throws IOException
	 *             Exception if any.
	 */
	public Template getMessage() throws IOException {
		return new Template(name, new StringReader(text), new Configuration());
	}

	@Override
	public String toString() {
		return " notification message " + name + text + " notification parameters :- " + parametersToReplace;
	}

	/**
	 * Gets the notification id.
	 *
	 * @return the notification id
	 */
	public Long getNotificationId() {
		return notificationId;
	}

	/**
	 * Sets the notification id.
	 *
	 * @param notificationId
	 *            the new notification id
	 */
	public void setNotificationId(final Long notificationId) {
		this.notificationId = notificationId;
	}

	/**
	 * Gets the name.
	 *
	 * @return the name
	 */
	public String getName() {
		return name;
	}

	/**
	 * @return the type
	 */
	public Integer getType() {
		return type;
	}

	/**
	 * @param type the type to set
	 */
	public void setType(final Integer type) {
		this.type = type;
	}

	/**
	 * @return the subType
	 */
	public Integer getSubType() {
		return subType;
	}

	/**
	 * @param subType the subType to set
	 */
	public void setSubType(final Integer subType) {
		this.subType = subType;
	}

	/**
	 * @return the text
	 */
	public String getText() {
		return text;
	}

	/**
	 * @return the otherLangNotificationMap
	 */
	public Map<LanguageType, FDPNotificationVO> getOtherLangNotificationMap() {
		return otherLangNotificationMap;
	}

	/**
	 * @param otherLangNotificationMap the otherLangNotificationMap to set
	 */
	public void addOtherLangNotificationMap(final LanguageType languageType, final FDPNotificationVO fdpNotificationVO) {
		if(null == this.otherLangNotificationMap) {
			this.otherLangNotificationMap = new HashMap<LanguageType, FDPNotificationVO>(LanguageType.values().length);
		}
		this.otherLangNotificationMap.put(languageType, fdpNotificationVO);
	}
	
	
	
}
