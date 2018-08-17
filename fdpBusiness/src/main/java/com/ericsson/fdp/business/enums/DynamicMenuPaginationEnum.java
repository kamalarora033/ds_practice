package com.ericsson.fdp.business.enums;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

// TODO: Auto-generated Javadoc
/**
 * The Enum DynamicMenuPaginationEnum.
 * 
 * @author Ericsson
 */
public enum DynamicMenuPaginationEnum {

	/** The pagination previous. */
	PAGINATION_PREVIOUS(1),

	/** The pagination more. */
	PAGINATION_MORE(2),

	/** The footer. */
	FOOTER(3);

	/** The Constant paginationOptions. */
	private static final List<DynamicMenuPaginationEnum> paginationOptions = new ArrayList<DynamicMenuPaginationEnum>(
			Arrays.asList(values()));

	static {
		Collections.sort(paginationOptions, new Comparator<DynamicMenuPaginationEnum>() {

			@Override
			public int compare(final DynamicMenuPaginationEnum o1, final DynamicMenuPaginationEnum o2) {
				return o1.getPriority() - o2.getPriority();
			}
		});
	}

	/** The priority. */
	private int priority;

	/**
	 * Instantiates a new dynamic menu pagination enum.
	 * 
	 * @param priority
	 *            the priority
	 */
	private DynamicMenuPaginationEnum(final int priority) {
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
	 * Gets the dynamic menu pagination priority sorted list.
	 * 
	 * @return the dynamic menu pagination priority sorted list
	 */
	public static List<DynamicMenuPaginationEnum> getDynamicMenuPaginationPrioritySortedList() {
		return paginationOptions;
	}
}
