package com.ericsson.fdp.business.enums;

/**
 * The Enum DynamicMenuFooterEnum.
 * 
 * @author Ericsson
 */
public enum DynamicMenuFooterEnum {

	/** The footer return to previous menu status. */
	FOOTER_RETURN_TO_PREVIOUS_MENU_STATUS(DynamicMenuPaginationKey.RETURN_TO_PREVIOUS_MENU_STATUS, 1),

	/** The footer return to main menu status. */
	FOOTER_RETURN_TO_MAIN_MENU_STATUS(DynamicMenuPaginationKey.RETURN_TO_MAIN_MENU_STATUS, 2),

	/** The footer exit status. */
	FOOTER_EXIT_STATUS(DynamicMenuPaginationKey.EXIT_STATUS, 3);

	/** The dynamic menu pagination key. */
	private DynamicMenuPaginationKey dynamicMenuPaginationKey;

	/** The priority. */
	private int priority;

	/**
	 * Instantiates a new dynamic menu footer enum.
	 * 
	 * @param dynamicMenuPaginationKey
	 *            the dynamic menu pagination key
	 * @param priority
	 *            the priority
	 */
	private DynamicMenuFooterEnum(final DynamicMenuPaginationKey dynamicMenuPaginationKey, final int priority) {
		this.dynamicMenuPaginationKey = dynamicMenuPaginationKey;
		this.priority = priority;
	}

	/**
	 * Gets the priority.
	 * 
	 * @return the priority
	 */
	public int getPriority() {
		return priority;
	}

	/**
	 * Gets the dynamic menu pagination key.
	 * 
	 * @return the dynamic menu pagination key
	 */
	public DynamicMenuPaginationKey getDynamicMenuPaginationKey() {
		return dynamicMenuPaginationKey;
	}

	/**
	 * Gets the dynamic menu footer key priority.
	 * 
	 * @param key
	 *            the key
	 * @return the dynamic menu footer key priority
	 */
	public static int getDynamicMenuFooterKeyPriority(final DynamicMenuPaginationKey key) {
		int result = -1;
		for (final DynamicMenuFooterEnum value : values()) {
			if (value.getDynamicMenuPaginationKey().equals(key)) {
				result = value.getPriority();
				break;
			}
		}
		return result;
	}
}
