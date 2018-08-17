package com.ericsson.fdp.business.charging;

import java.io.Serializable;

public interface Account extends Serializable{
	
	/**
	 * Gets Dedicated Account Value to Charge.
	 * 
	 * @param dedicatedAccountValue
	 */
	public Long getAccountValue();
	
	/**
	 * Gets priority.
	 * 
	 * @return
	 */
	public Integer getPriority();
	
}
