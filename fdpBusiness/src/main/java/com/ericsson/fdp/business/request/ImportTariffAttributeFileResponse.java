package com.ericsson.fdp.business.request;

import java.io.Serializable;

import com.ericsson.fdp.common.enums.Status;

/**
 * The import file response interface.
 * 
 * @author Ericsson
 * 
 */
public interface ImportTariffAttributeFileResponse extends Serializable {

	/**
	 * This method is used to get the execution status.
	 * 
	 * @return the execution status.
	 */
	Status getExecutionStatus();

	/**
	 * This method is used to get the execution status for JSon File upload.
	 * @return
	 */
	Status getJsonExecutionStatus();
	
	/**
	 * This method is used to get the execution status for Database update status.
	 * @return
	 */
	Status getDatabaseOperationStatus();
	
	/**
	 * This method is used to get the DA file import status.
	 * @return
	 */
	Status getDaFileStatus();
	
	/**
	 * This method is used to get the PSO file import status.
	 * @return
	 */
	Status getPsoFileStatus();
	
	/**
	 * This method is used to get the CommunitID file import status.
	 * @return
	 */
	Status getCommunityIdFileStatus();
	
	/**
	 * This mehthod is used to get the Offer File import status.
	 * @return
	 */
	Status getOfferIdFileStatus();
}
