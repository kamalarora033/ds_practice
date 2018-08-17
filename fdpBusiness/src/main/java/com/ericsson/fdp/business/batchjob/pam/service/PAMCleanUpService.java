/**
 * 
 */
package com.ericsson.fdp.business.batchjob.pam.service;

import java.util.Calendar;
import java.util.List;

import javax.ejb.Remote;

import com.ericsson.fdp.dao.dto.FDPBatchJobsDTO;
import com.ericsson.fdp.dao.dto.PAMCleanUpDTO;

/**
 * The Interface PAMCleanUpService.
 *
 * @author Ericsson
 */
@Remote
public interface PAMCleanUpService {
	
	/**
	 * Initialize pam job.
	 *
	 * @param pamCleanUpDTO the pam clean up dto
	 * @return the long
	 */
	Long initializePAMJob(PAMCleanUpDTO pamCleanUpDTO);
	
	/**
	 * Update pam job.
	 *
	 * @param pamCleanUpDTO the pam clean up dto
	 */
	void updatePAMJob(PAMCleanUpDTO pamCleanUpDTO);
	
	/**
	 * Gets the all batch jobs.
	 *
	 * @return the all batch jobs
	 */
	List<FDPBatchJobsDTO> getAllBatchJobs();
	
	/**
	 * Gets the all batch job circles.
	 *
	 * @return the all batch job circles
	 */
	List<String> getAllBatchJobCircles();
	
	/**
	 * Gets the all batch job names.
	 *
	 * @return the all batch job names
	 */
	List<String> getAllBatchJobNames();
	
	/**
	 * Update batch job cron expression.
	 *
	 * @param batchJobId the batch job id
	 * @param cronExpression the cron expression
	 * @param modifiedBy the modified by
	 * @param modifiedOn the modified on
	 */
	void updateBatchJobCronExpression(Long batchJobId,String cronExpression,String modifiedBy, Calendar modifiedOn);
	
	/**
	 * Gets the batch job details.
	 *
	 * @param circleCode the circle code
	 * @param batchJobName the batch job name
	 * @return the batch job details
	 */
	FDPBatchJobsDTO getBatchJobDetails(String circleCode,String batchJobName);
	

	
}
