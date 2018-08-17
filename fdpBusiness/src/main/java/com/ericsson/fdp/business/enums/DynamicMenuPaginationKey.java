package com.ericsson.fdp.business.enums;

import com.ericsson.fdp.common.enums.DynamicMenuAdditionalInfoKey;

/**
 * The Enum DynamicMenuPaginationKey.
 */
public enum DynamicMenuPaginationKey {

	/**
	 * The previous option.
	 */
	PREVIOUS(null, ConfigurationKey.PREV_CODE_FOR_USSD, ConfigurationKey.PREV_CODE_FOR_SMS,
			ConfigurationKey.PREV_TEXT_FOR_USSD, ConfigurationKey.PREV_TEXT_FOR_SMS, 1, 1),

	/**
	 * The more option.
	 */
	MORE(null, ConfigurationKey.MORE_CODE_FOR_USSD, ConfigurationKey.MORE_CODE_FOR_SMS,
			ConfigurationKey.MORE_TEXT_FOR_USSD, ConfigurationKey.MORE_TEXT_FOR_SMS, 2, 2),

	/** The return to previous menu status. */
	RETURN_TO_PREVIOUS_MENU_STATUS(DynamicMenuAdditionalInfoKey.RETURN_TO_PREVIOUS_MENU_STATUS,
			ConfigurationKey.PREV_MENU_CODE_FOR_USSD, ConfigurationKey.PREV_MENU_CODE_FOR_SMS,
			ConfigurationKey.PREV_MENU_TEXT_FOR_USSD, ConfigurationKey.PREV_MENU_TEXT_FOR_SMS, 3, 3),

	/** The return to main menu status. */
	RETURN_TO_MAIN_MENU_STATUS(DynamicMenuAdditionalInfoKey.RETURN_TO_MAIN_MENU_STATUS,
			ConfigurationKey.MAIN_MENU_CODE_FOR_USSD, ConfigurationKey.MAIN_MENU_CODE_FOR_SMS,
			ConfigurationKey.MAIN_MENU_TEXT_FOR_USSD, ConfigurationKey.MAIN_MENU_TEXT_FOR_SMS, 4, 4),

	/** The exit status. */
	EXIT_STATUS(DynamicMenuAdditionalInfoKey.EXIT_STATUS, ConfigurationKey.EXIT_CODE_FOR_USSD,
			ConfigurationKey.EXIT_CODE_FOR_SMS, ConfigurationKey.EXIT_TEXT_FOR_USSD,
			ConfigurationKey.EXIT_TEXT_FOR_SMS, 5, 5);

	/**
	 * The additional info key to which this maps.
	 */
	private DynamicMenuAdditionalInfoKey additionalInfoKey;

	/**
	 * The configuration key for ussd.
	 */
	private ConfigurationKey configurationKeyForUSSD;

	/**
	 * The configuration key for sms.
	 */
	private ConfigurationKey configurationKeyForSMS;

	/**
	 * The configuration text key for ussd.
	 */
	private ConfigurationKey configurationTextKeyForUSSD;

	/**
	 * The configuration text key for sms.
	 */
	private ConfigurationKey configurationTextKeyForSMS;

	/** The ussd order value. */
	private int ussdOrderValue;

	/** The smsc order value. */
	private int smscOrderValue;

	/**
	 * The constructor.
	 * 
	 * @param dynamicMenuAdditionalInfoKey
	 *            dynamicMenuAdditionalInfoKey
	 * @param configurationKeyForUSSD
	 *            configurationKeyForUSSD
	 * @param configurationKeyForSMS
	 *            configurationKeyForSMS
	 */

	private DynamicMenuPaginationKey(final DynamicMenuAdditionalInfoKey dynamicMenuAdditionalInfoKey,
			final ConfigurationKey configurationKeyForUSSD, final ConfigurationKey configurationKeyForSMS,
			final ConfigurationKey configurationTextKeyForUSSD, final ConfigurationKey configurationTextKeyForSMS,
			final int ussdOrderValue, final int smscOrderValue) {
		this.additionalInfoKey = dynamicMenuAdditionalInfoKey;
		this.configurationKeyForUSSD = configurationKeyForUSSD;
		this.configurationKeyForSMS = configurationKeyForSMS;
		this.configurationTextKeyForUSSD = configurationTextKeyForUSSD;
		this.configurationTextKeyForSMS = configurationTextKeyForSMS;
		this.ussdOrderValue = ussdOrderValue;
		this.smscOrderValue = smscOrderValue;
	}

	/**
	 * @return the additionalInfoKey
	 */
	public DynamicMenuAdditionalInfoKey getAdditionalInfoKey() {
		return additionalInfoKey;
	}

	/**
	 * @param additionalInfoKey
	 *            the additionalInfoKey to set
	 */
	public void setAdditionalInfoKey(final DynamicMenuAdditionalInfoKey additionalInfoKey) {
		this.additionalInfoKey = additionalInfoKey;
	}

	/**
	 * @return the configurationKeyForUSSD
	 */
	public ConfigurationKey getConfigurationKeyForUSSD() {
		return configurationKeyForUSSD;
	}

	/**
	 * @param configurationKeyForUSSD
	 *            the configurationKeyForUSSD to set
	 */
	public void setConfigurationKeyForUSSD(final ConfigurationKey configurationKeyForUSSD) {
		this.configurationKeyForUSSD = configurationKeyForUSSD;
	}

	/**
	 * @return the configurationKeyForSMS
	 */
	public ConfigurationKey getConfigurationKeyForSMS() {
		return configurationKeyForSMS;
	}

	/**
	 * @param configurationKeyForSMS
	 *            the configurationKeyForSMS to set
	 */
	public void setConfigurationKeyForSMS(final ConfigurationKey configurationKeyForSMS) {
		this.configurationKeyForSMS = configurationKeyForSMS;
	}

	/**
	 * @return the configurationTextKeyForUSSD
	 */
	public ConfigurationKey getConfigurationTextKeyForUSSD() {
		return configurationTextKeyForUSSD;
	}

	/**
	 * @param configurationTextKeyForUSSD
	 *            the configurationTextKeyForUSSD to set
	 */
	public void setConfigurationTextKeyForUSSD(final ConfigurationKey configurationTextKeyForUSSD) {
		this.configurationTextKeyForUSSD = configurationTextKeyForUSSD;
	}

	/**
	 * @return the configurationTextKeyForSMS
	 */
	public ConfigurationKey getConfigurationTextKeyForSMS() {
		return configurationTextKeyForSMS;
	}

	/**
	 * @param configurationTextKeyForSMS
	 *            the configurationTextKeyForSMS to set
	 */
	public void setConfigurationTextKeyForSMS(final ConfigurationKey configurationTextKeyForSMS) {
		this.configurationTextKeyForSMS = configurationTextKeyForSMS;
	}

	/**
	 * @return the ussdOrderValue
	 */
	public int getUssdOrderValue() {
		return ussdOrderValue;
	}

	/**
	 * @return the smscOrderValue
	 */
	public int getSmscOrderValue() {
		return smscOrderValue;
	}

}
