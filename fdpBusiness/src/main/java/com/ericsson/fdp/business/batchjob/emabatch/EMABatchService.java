package com.ericsson.fdp.business.batchjob.emabatch;

import java.util.List;

import javax.ejb.Remote;

import com.ericsson.fdp.common.vo.FDPCircle;
import com.ericsson.fdp.core.exception.ExecutionFailedException;
import com.ericsson.fdp.dao.dto.batchJob.BatchExecutionResponseDTO;

/**
 * The Interface EMABatch.
 */
@Remote
public interface EMABatchService {

	/**
	 * Execute ema commands.
	 * 
	 * @param fileRecords
	 *            the file records
	 * @param fdpCircle
	 * @param tempFilePath
	 * @return the batch execution response dto
	 * @throws ExecutionFailedException
	 *             the execution failed exception
	 */
	BatchExecutionResponseDTO executeEMACommands(final List<String> fileRecords, FDPCircle fdpCircle,
			String tempFilePath) throws ExecutionFailedException;
}
