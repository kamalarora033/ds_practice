package com.ericsson.fdp.business.mbeans;

import java.lang.management.ManagementFactory;
import java.util.Map;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.ejb.Singleton;
import javax.management.MBeanServer;
import javax.management.ObjectName;

import com.ericsson.fdp.core.dsm.DSMService;
import com.ericsson.fdp.core.dsm.impl.DSMServiceImpl;
import com.ericsson.fdp.core.envers.bean.EntityMetaData;
import com.ericsson.fdp.core.envers.constants.EnversConstants;

/**
 * FDPAuditingManager is used to enable and disable the auditing on given
 * tables.
 */
@Singleton
//@Startup
public class FDPAuditingManager implements FDPAuditingManagerMXBean {

	/** The Constant DSMSERVICE. */
	private static final DSMService DSMSERVICE = DSMServiceImpl.getInstance();

	/** The envers map. */
	private Map<String, EntityMetaData> enversMap = null;

	/** The object name. */
	private ObjectName objectName = null;

	/** The platform m bean server. */
	private MBeanServer platformMBeanServer;

	/**
	 * Register in jmx.
	 */
	@PostConstruct
	public final void registerInJMX() {
		try {
			objectName = new ObjectName("FDPAuditingManager:type=" + this.getClass().getName());
			platformMBeanServer = ManagementFactory.getPlatformMBeanServer();
			platformMBeanServer.registerMBean(this, objectName);

		} catch (Exception e) {
			throw new IllegalStateException("Problem during registration of Monitoring into JMX:" + e);
		}
	}

	/**
	 * Unregister from jmx.
	 */
	@PreDestroy
	public final void unregisterFromJMX() {
		try {
			platformMBeanServer.unregisterMBean(this.objectName);
		} catch (Exception e) {
			throw new IllegalStateException("Problem during unregistration of Monitoring into JMX:" + e);
		}
	}

	@SuppressWarnings("unchecked")
	@Override
	public String getAuditing(final String entityName) {
		enversMap = (Map<String, EntityMetaData>) DSMSERVICE.getValue(EnversConstants.ENVERS_MAP);
		String entityNameInLowerCase = entityName.toLowerCase();
		if (validate(entityNameInLowerCase)) {
			EntityMetaData entityMetaData = enversMap.get(entityNameInLowerCase);
			return entityMetaData.getStatus();
		} else {
			return EnversConstants.FAILURE_STATUS;
		}
	}

	@SuppressWarnings("unchecked")
	@Override
	public String setAuditing(final String entityName, final String status) {
		enversMap = (Map<String, EntityMetaData>) DSMSERVICE.getValue(EnversConstants.ENVERS_MAP);
		String entityNameInLowerCase = entityName.toLowerCase();
		String statusInLowerCase = status.toLowerCase();

		if (validate(entityNameInLowerCase, statusInLowerCase)) {
			EntityMetaData entityMetaData = enversMap.get(entityNameInLowerCase);
			entityMetaData.setStatus(statusInLowerCase);
			enversMap.put(entityNameInLowerCase, entityMetaData);
			return EnversConstants.SUCCESS_STATUS;
		} else {
			return EnversConstants.FAILURE_STATUS;
		}
	}

	/**
	 * This method is used to validate the entityName and status(on/off) at the
	 * time of getting the status or setting the status.
	 *
	 * @param entityName the entity name
	 * @param status the status
	 * @return true, if successful
	 */
	private boolean validate(final String entityName, final String status) {
		boolean isValidated = false;
		if (validate(entityName)) {
			if ((status.toLowerCase().equals(EnversConstants.STATUS_ON) || (status.toLowerCase()
					.equals(EnversConstants.STATUS_OFF)))) {
				isValidated = true;
			}
		}
		return isValidated;
	}

	/**
	 * This method is used to check the entity name in stored entity map.
	 *
	 * @param entityName the entity name
	 * @return true, if successful
	 */
	private boolean validate(final String entityName) {
		return enversMap.containsKey(entityName.toLowerCase());
	}

}
