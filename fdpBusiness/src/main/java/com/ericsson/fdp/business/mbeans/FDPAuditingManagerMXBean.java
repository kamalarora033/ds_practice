package com.ericsson.fdp.business.mbeans;

/**
 * The Interface FDPAuditingManagerMXBean.
 */
public interface FDPAuditingManagerMXBean {

	/**
	 * This method returns status of audited table status whether auditing
	 * status is on/off.
	 *
	 * @param entityName the entity name
	 * @return the auditing
	 */
	String getAuditing(String entityName);

	/**
	 * This method is used to enable/disable the auditing on table.
	 *
	 * @param entityName the entity name
	 * @param status the status
	 * @return the string
	 */

	String setAuditing(String entityName, String status);
}
