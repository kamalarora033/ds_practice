/**
 * 
 */
package com.ericsson.fdp.business.batchjob.mass.service.impl;

import java.util.TimerTask;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This class is timer class to execute the mass provision request at scheduled interval time 
 * 
 * @author Deepak Kumar
 * 
 */
public class MassProvisionTimerTask extends TimerTask {

	private MassLoadUrlServiceImpl serviceImpl = null;
	private static final Logger LOGGER = LoggerFactory
			.getLogger(MassProvisionTimerTask.class);

	/**]
	 * 
	 * @param serviceImpl
	 */
	MassProvisionTimerTask(MassLoadUrlServiceImpl serviceImpl) {
		this.serviceImpl = serviceImpl;
	}

	@Override
	public void run() {
		LOGGER.debug("Executing mass provision timer task ");
		try {
			serviceImpl.massProvisionExecution();
		} catch (Exception e) {
			LOGGER.error("Mass provision task failed ", e);
		}

	}

}
