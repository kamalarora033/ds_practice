package com.ericsson.fdp.business.monitoring.component;

import java.util.List;

import javax.inject.Inject;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ericsson.fdp.dao.dto.FDPExternalSystemDTO;
import com.ericsson.fdp.dao.fdpadmin.FDPAIRConfigDAO;
import com.ericsson.fdp.dao.fdpadmin.FDPCGWConfigDAO;
import com.ericsson.fdp.dao.fdpadmin.FDPExternalSystemDAO;
import com.ericsson.fdp.dao.fdpadmin.FDPRSConfigDAO;
import com.ericsson.fdp.dao.fdpadmin.FDPSMSCConfigDAO;

/**
 * The Class AbstractExternalSystemChecker monitors External System circle
 * wise..
 */
public abstract class AbstractExternalSystemChecker implements ExternalSystemMonitorChain {

	/** The logger. */
	private static final Logger LOGGER = LoggerFactory.getLogger(AbstractExternalSystemChecker.class);

	/** The atleast one system up. */
	private boolean atleastOneSystemUp;

	/** The fdp air config dao. */
	@Inject
	private FDPAIRConfigDAO fdpAIRConfigDAO;

	/** The fdp cgw config dao. */
	@Inject
	private FDPCGWConfigDAO fdpCGWConfigDAO;

	/** The fdp rs config dao. */
	@Inject
	private FDPRSConfigDAO fdpRSConfigDAO;

	/** The fdp smsc config dao. */
	@Inject
	private FDPSMSCConfigDAO fdpSMSCConfigDAO;

	/** The external system component checker. */
	private ExternalSystemMonitorChain externalSystemComponentChecker;

	@Override
	public ExternalSystemMonitorChain next() {
		return externalSystemComponentChecker;
	}

	@Override
	public boolean monitorComponent(String circle) {
		List<FDPExternalSystemDTO> componentList = getFdpExternalSystemDAO(this).getFDPExternalSystems(circle);
		LOGGER.info("Starting {} component for circle : {}",this.getClass().getName(),circle);
		boolean componentUp = false;
		for (FDPExternalSystemDTO airComponent : componentList) {
			componentUp = monitorComponent(airComponent);
			if (componentUp && isAtleastOneSystemUp()) {
				componentUp = true;
				break;
			} else if (!isAtleastOneSystemUp() && !componentUp) {
				componentUp = false;
				LOGGER.warn("{} component is not up for circle : {}",this.getClass().getName(),circle);
				break;
			}
			// airComponent.
		}
		if (componentUp && next() != null) {
			return next().monitorComponent(circle);
		}
		return componentUp;
	}

	/**
	 * Monitor component.
	 *
	 * @param fdpExternalSystemDTO
	 *            the fdp external system dto
	 * @return true, if successful
	 */
	protected abstract boolean monitorComponent(FDPExternalSystemDTO fdpExternalSystemDTO);

	/**
	 * Sets the next.
	 *
	 * @param nextComponentChecker
	 *            the new next
	 */
	public void setNext(ExternalSystemMonitorChain nextComponentChecker) {
		this.externalSystemComponentChecker = nextComponentChecker;
	}

	/**
	 * Checks if is atleast one system up.
	 *
	 * @return true, if is atleast one system up
	 */
	public boolean isAtleastOneSystemUp() {
		return atleastOneSystemUp;
	}

	/**
	 * Sets the atleast one system up.
	 *
	 * @param atleastOneSystemUp
	 *            the new atleast one system up
	 */
	public void setAtleastOneSystemUp(boolean atleastOneSystemUp) {
		this.atleastOneSystemUp = atleastOneSystemUp;
	}

	/**
	 * Gets the fdp external system dao.
	 *
	 * @param abstractExternalSystemChecker
	 *            the abstract external system checker
	 * @return the fdp external system dao
	 */
	public FDPExternalSystemDAO getFdpExternalSystemDAO(AbstractExternalSystemChecker abstractExternalSystemChecker) {
		FDPExternalSystemDAO fdpExternalSystemDAO = null;
		if (abstractExternalSystemChecker instanceof AIRComponentChecker) {
			fdpExternalSystemDAO = fdpAIRConfigDAO;
		} else if (abstractExternalSystemChecker instanceof CGWComponentChecker) {
			fdpExternalSystemDAO = fdpCGWConfigDAO;
		} else if (abstractExternalSystemChecker instanceof RSComponentChecker) {
			fdpExternalSystemDAO = fdpRSConfigDAO;
		} else if (abstractExternalSystemChecker instanceof SMSCComponentChecker) {
			fdpExternalSystemDAO = fdpSMSCConfigDAO;
		}
		return fdpExternalSystemDAO;
	}

}
