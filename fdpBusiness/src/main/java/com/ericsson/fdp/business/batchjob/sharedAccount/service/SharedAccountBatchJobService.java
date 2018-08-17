package com.ericsson.fdp.business.batchjob.sharedAccount.service;

import javax.ejb.Remote;

import com.ericsson.fdp.dao.dto.batchJob.BatchExecutionRequestDTO;
import com.ericsson.fdp.dao.dto.batchJob.BatchExecutionResponseDTO;

/**
 * The Interface SharedAccountBatchJobService.
 */
@Remote
public interface SharedAccountBatchJobService {

	/**
	 * This method delete shared account group where shared account request type is Delete Account.
	 * 
	 * @param batchExecutionRequest
	 *            the batch execution request
	 * @return the batch execution response DTO that store batch execution response batch/size/failures/success
	 */
	BatchExecutionResponseDTO deleteSharedAccountGroup(BatchExecutionRequestDTO batchExecutionRequest);

	/**
	 * This method is used to get the view usage for all the given provider and
	 * offer ids. where shared account request type is view total usage/ top n usage.
	 *
	 * @param batchExecutionRequest the batch execution request
	 * @return the batch execution response DTO that store batch execution response batch/size/failures/success
	 */
	BatchExecutionResponseDTO viewUsageForConsumers(BatchExecutionRequestDTO batchExecutionRequest);

	/**
	 * Delete OTP Where expiry date is before the current date.
	 *
	 * @param batchExecutionRequest the batch execution request
	 * @return the batch execution response DTO
	 */
	BatchExecutionResponseDTO deleteExpiredOTP(BatchExecutionRequestDTO batchExecutionRequest);

	/**
	 * This method update shared account request status from pending to expired for Add request type.
	 * @param executionRequestDTO executionRequestDTO object
	 * @return responseDTO for batch execution response parameters
	 */
	BatchExecutionResponseDTO updateSharedAccountRequestStatus(final BatchExecutionRequestDTO executionRequestDTO);

	/**
	 * Upgrade consumer limit to old val.
	 *
	 * @param executionRequestDTO the execution request dto
	 * @return the batch execution response dto
	 */
	BatchExecutionResponseDTO upgradeConsumerLimitToOldVal(BatchExecutionRequestDTO executionRequestDTO);

	/**
	 * Delete shared account expired req.
	 *
	 * @param executionRequestDTO the execution request dto
	 * @return the batch execution response dto
	 */
	BatchExecutionResponseDTO deleteSharedAccountExpiredReq(BatchExecutionRequestDTO executionRequestDTO);
}
