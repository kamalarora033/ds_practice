package com.ericsson.fdp.business.dto.emabatch;

import java.io.Serializable;
import java.util.List;
import java.util.Map;

import com.ericsson.fdp.business.enums.EMAServiceMode;
import com.ericsson.fdp.dao.enums.EMAInterfaceTypeEnum;

/**
 * The Class EMABatchDTO.
 */
public class EMABatchRecordDTO implements Serializable {

	/** The Constant serialVersionUID. */
	private static final long serialVersionUID = 2333106242903845074L;

	/** The service action. */
	private String serviceAction;

	/** The interface type. */
	private EMAInterfaceTypeEnum interfaceType;

	/** The commands. */
	private Map<EMAServiceMode, List<String>> commands;

	/** The mode. */
	private EMAServiceMode mode;

	/** The msisdn. */
	private String msisdn;

	/** The batch counter. */
	private Integer batchCounter = 0;

	/** The check icr circle. */
	private Boolean checkIcrCircle = true;

	/**
	 * Gets the service action.
	 * 
	 * @return the serviceAction
	 */
	public String getServiceAction() {
		return serviceAction;
	}

	/**
	 * Sets the service action.
	 * 
	 * @param serviceAction
	 *            the serviceAction to set
	 */
	public void setServiceAction(final String serviceAction) {
		this.serviceAction = serviceAction;
	}

	/**
	 * @return the interfaceType
	 */
	public EMAInterfaceTypeEnum getInterfaceType() {
		return interfaceType;
	}

	/**
	 * @param interfaceType
	 *            the interfaceType to set
	 */
	public void setInterfaceType(final EMAInterfaceTypeEnum interfaceType) {
		this.interfaceType = interfaceType;
	}

	/**
	 * Gets the commands.
	 * 
	 * @return the commands
	 */
	public Map<EMAServiceMode, List<String>> getCommands() {
		return commands;
	}

	/**
	 * Sets the commands.
	 * 
	 * @param commands
	 *            the commands to set
	 */
	public void setCommands(final Map<EMAServiceMode, List<String>> commands) {
		this.commands = commands;
	}

	/**
	 * @return the mode
	 */
	public EMAServiceMode getMode() {
		return mode;
	}

	/**
	 * @param mode
	 *            the mode to set
	 */
	public void setMode(final EMAServiceMode mode) {
		this.mode = mode;
	}

	/**
	 * Gets the msisdn.
	 * 
	 * @return the msisdn
	 */
	public String getMsisdn() {
		return msisdn;
	}

	/**
	 * Sets the msisdn.
	 * 
	 * @param msisdn
	 *            the msisdn to set
	 */
	public void setMsisdn(final String msisdn) {
		this.msisdn = msisdn;
	}

	/**
	 * @return the batchCounter
	 */
	public Integer getBatchCounter() {
		return batchCounter;
	}

	/**
	 * @param batchCounter
	 *            the batchCounter to set
	 */
	public void setBatchCounter(final Integer batchCounter) {
		this.batchCounter = batchCounter;
	}

	/**
	 * @return the checkIcrCircle
	 */
	public Boolean getCheckIcrCircle() {
		return checkIcrCircle;
	}

	/**
	 * @param checkIcrCircle
	 *            the checkIcrCircle to set
	 */
	public void setCheckIcrCircle(final Boolean checkIcrCircle) {
		this.checkIcrCircle = checkIcrCircle;
	}

}
