package com.ericsson.fdp.business.batchjob.reportGeneration.service;

import java.io.File;

import javax.ejb.Remote;

import com.ericsson.fdp.dao.dto.batchJob.BatchExecutionRequestDTO;
import com.ericsson.fdp.dao.dto.batchJob.BatchExecutionResponseDTO;

/**
 * The Interface ReportGenerationService.
 */
@Remote
public interface ReportGenerationService {

	/**
	 * This method generate report for CGW report, Incoming Hourly TPS report ,
	 * Transaction Hourly TPS report, Product provisioning report and saves the
	 * records to database.
	 * 
	 * @param batchExecutionRequest
	 *            batchExecutionRequest object
	 * @param reportFolder
	 *            report folder to fetch list of CSV Files
	 * @return responseDTO
	 */
	BatchExecutionResponseDTO generateFDPReports(BatchExecutionRequestDTO batchExecutionRequest, File reportFolder);

}
