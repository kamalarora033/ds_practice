package com.ericsson.fdp.business.enums;

import java.util.ArrayList;
import java.util.List;

/**
 * This enum is used to store the notification id's that are used for all
 * circles.
 * 
 * @author Ericsson
 * 
 */
public enum NotificationForAllCircle {

	/**
	 * This notification id is used for top n usage.
	 */
	TOP_N_USAGE(-2L),
	/**
	 * This notification is used for consumer usage.
	 */
	CONSUMER_USAGE_NOTIFICATION(-1L);

	/**
	 * The notification id.
	 */
	private Long id;

	/**
	 * The list of ids.
	 */
	private static List<Long> idList = new ArrayList<Long>();

	/**
	 * The constructor.
	 * 
	 * @param id
	 *            the id for the enum.
	 */
	private NotificationForAllCircle(final Long id) {
		this.id = id;
	}

	/**
	 * This block is used to initialize the id list, so that the contains check
	 * can be done using this list of id.
	 */
	static {
		for (NotificationForAllCircle notificationForAllCircle : NotificationForAllCircle.values()) {
			idList.add(notificationForAllCircle.getId());
		}
	}

	/**
	 * @return the id.
	 */
	public Long getId() {
		return id;
	}

	/**
	 * This method is used to check if the notification is for all circle or for
	 * a single circle.
	 * 
	 * @param id
	 *            the id to be checked.
	 * @return true if it contains the id, false otherwise.
	 */
	public static boolean containsId(final Long id) {
		return idList.contains(id);
	}

}
