package com.ericsson.fdp.business.command.rollback.bean;

import java.util.List;

import com.ericsson.fdp.FDPCacheable;
import com.ericsson.fdp.core.command.FDPCommand;

/**
 * The Class RollBackCacheBean.
 */
public class RollBackCacheBean implements FDPCacheable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	/** The roll back command list. */
	private List<FDPCommand> rollBackCommandList;

	/**
	 * Gets the roll back command list.
	 * 
	 * @return the roll back command list
	 */
	public List<FDPCommand> getRollBackCommandList() {
		return rollBackCommandList;
	}

	/**
	 * Sets the roll back command list.
	 * 
	 * @param rollBackCommandList
	 *            the new roll back command list
	 */
	public void setRollBackCommandList(List<FDPCommand> rollBackCommandList) {
		this.rollBackCommandList = rollBackCommandList;
	}

}
