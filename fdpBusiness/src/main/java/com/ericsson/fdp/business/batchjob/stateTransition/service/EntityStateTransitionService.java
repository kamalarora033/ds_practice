/**
 * 
 */
package com.ericsson.fdp.business.batchjob.stateTransition.service;

import javax.ejb.Remote;

import com.ericsson.fdp.dao.dto.batchJob.BatchExecutionRequestDTO;
import com.ericsson.fdp.dao.dto.batchJob.BatchExecutionResponseDTO;

/**
 * The Interface EntityStateTransitionService.
 *
 * @author Ericsson
 */
@Remote
public interface EntityStateTransitionService {
	
	/**
	 * Update products status.
	 *
	 * @param executionRequestDTO the execution request dto
	 * @return the batch execution response dto
	 */
	BatchExecutionResponseDTO updateProductsStatus(BatchExecutionRequestDTO executionRequestDTO);
	
	/**
	 * Update menu status.
	 *
	 * @param executionRequestDTO the execution request dto
	 * @return the batch execution response dto
	 */
	BatchExecutionResponseDTO updateMenuStatus(BatchExecutionRequestDTO executionRequestDTO);
}
