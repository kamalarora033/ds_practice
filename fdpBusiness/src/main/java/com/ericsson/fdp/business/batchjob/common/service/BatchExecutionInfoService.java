package com.ericsson.fdp.business.batchjob.common.service;

import javax.ejb.Remote;

import com.ericsson.fdp.common.vo.FDPCircle;
import com.ericsson.fdp.dao.dto.batchJob.BatchExecutionResponseDTO;
import com.ericsson.fdp.dao.dto.batchJob.FDPBatchExecutionInfoDTO;

@Remote
public interface BatchExecutionInfoService {

	/**
	 * Initialise batch execution info.
	 * 
	 * @param batchExecutionInfoDTO
	 *            the batch execution info dto
	 * @return the long
	 */
	Long initialiseBatchExecutionInfo(FDPBatchExecutionInfoDTO batchExecutionInfoDTO);

	/**
	 * Update batch execution info.
	 * 
	 * @param batchExecutionInfoDTO
	 *            the batch execution info dto
	 * @return the boolean
	 */
	Boolean updateBatchExecutionInfo(FDPBatchExecutionInfoDTO batchExecutionInfoDTO);
	
	/**
	 * Read ESF data and call Ability
	 * @return 
	 * 
	 */
	BatchExecutionResponseDTO sendESFData(FDPCircle fdpCircle);
}
