package com.ericsson.fdp.business.request;

import java.io.File;
import java.io.Serializable;

import com.ericsson.fdp.common.enums.Status;

/**
 * The import file response interface.
 * 
 * @author Ericsson
 * 
 */
public interface ImportFileResponse extends Serializable {

	/**
	 * This method is used to get the execution status.
	 * 
	 * @return the execution status.
	 */
	Status getExecutionStatus();

	/**
	 * This method is used to get the count of successful values.
	 * 
	 * @return the count of successful values.
	 */
	Long getSucessfullValues();

	/**
	 * This method is used to get the count of failed values.
	 * 
	 * @return the count of failed values.
	 */
	Long getFailureValues();

	/**
	 * This method is used to get the file containing failed values information.
	 * 
	 * @return the file containing failed values.
	 */
	File getFailedOutputFile();

}
