package com.ericsson.fdp.business.batchjob.common.service.impl;

import java.io.IOException;

import javax.ejb.Stateless;
import javax.inject.Inject;

import org.slf4j.LoggerFactory;

import com.ericsson.fdp.business.batchjob.common.service.BatchExecutionInfoService;
import com.ericsson.fdp.business.batchjob.stateTransition.service.impl.EntityStateTransitionServiceImpl;
import com.ericsson.fdp.business.util.FulfillmentUtil;
import com.ericsson.fdp.common.vo.FDPCircle;
import com.ericsson.fdp.core.exception.ExecutionFailedException;
import com.ericsson.fdp.dao.batch.FDPBatchExecutionInfoDAO;
import com.ericsson.fdp.dao.dto.batchJob.BatchExecutionResponseDTO;
import com.ericsson.fdp.dao.dto.batchJob.FDPBatchExecutionInfoDTO;
import com.ericsson.fdp.dao.exception.FDPConcurrencyException;

/**
 * The Class BatchExecutionInfoServiceImpl.
 */
@Stateless
public class BatchExecutionInfoServiceImpl implements BatchExecutionInfoService {

	/** The Constant LOGGER. */
	private static final org.slf4j.Logger LOGGER = LoggerFactory.getLogger(EntityStateTransitionServiceImpl.class);

	/** The fdp batch execution info dao. */
	@Inject
	private FDPBatchExecutionInfoDAO fdpBatchExecutionInfoDAO;
	
	/** The fdp service provisioning. */
	/*@Resource(lookup = "java:app/fdpBusiness-1.0/ServiceProvisioningRule")
	private ServiceProvisioningRule serviceProvising;
*/
	@Override
	public Long initialiseBatchExecutionInfo(final FDPBatchExecutionInfoDTO batchExecutionInfoDTO) {
		return fdpBatchExecutionInfoDAO.saveBatchExecutionInfo(batchExecutionInfoDTO);
	}

	@Override
	public Boolean updateBatchExecutionInfo(final FDPBatchExecutionInfoDTO batchExecutionInfoDTO) {
		Boolean isUpdated = false;
		try {
			fdpBatchExecutionInfoDAO.updateBatchExecutionInfo(batchExecutionInfoDTO);
			isUpdated = true;

		} catch (FDPConcurrencyException e) {
			LOGGER.error("Batch job execution failed, batch job id = " + batchExecutionInfoDTO.getBatchJobId()
					+ "batch execution info id = " + batchExecutionInfoDTO.getBatchExecutionInfoId());
			isUpdated = false;
		}
		return isUpdated;
	}

	@Override
	public BatchExecutionResponseDTO sendESFData(FDPCircle fdpCircle) {
		try {
			FulfillmentUtil.sendESFData(fdpCircle);
		} catch (IOException e) {
			LOGGER.error("Some IOException is occurred");
		} catch (ExecutionFailedException e) {
			LOGGER.error("Some problem while sending data to ability :: "+e);
		}
		return null;
		
	}
}
