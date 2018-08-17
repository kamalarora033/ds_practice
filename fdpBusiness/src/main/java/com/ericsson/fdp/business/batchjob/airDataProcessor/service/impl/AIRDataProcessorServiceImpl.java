/**
 * 
 */
package com.ericsson.fdp.business.batchjob.airDataProcessor.service.impl;

import java.net.Inet4Address;
import java.net.UnknownHostException;
import java.util.Calendar;
import java.util.UUID;

import javax.ejb.Stateless;
import javax.inject.Inject;
import javax.naming.NamingException;

import org.slf4j.LoggerFactory;

import com.ericsson.fdp.business.airrecharge.service.AirRechargeProcessor;
import com.ericsson.fdp.business.batchjob.airDataProcessor.service.AIRDataProcessorService;
import com.ericsson.fdp.business.constants.BusinessConstants;
import com.ericsson.fdp.business.enums.OperatingMode;
import com.ericsson.fdp.common.constants.JNDILookupConstant;
import com.ericsson.fdp.common.enums.ExternalSystem;
import com.ericsson.fdp.core.exception.ExecutionFailedException;
import com.ericsson.fdp.core.utils.ApplicationConfigUtil;
import com.ericsson.fdp.dao.batch.FDPBatchExecutionInfoDAO;
import com.ericsson.fdp.dao.batch.FDPBatchJobsDAO;
import com.ericsson.fdp.dao.dto.FDPBatchJobsDTO;
import com.ericsson.fdp.dao.dto.batchJob.FDPBatchExecutionInfoDTO;
import com.ericsson.fdp.dao.enums.BatchJobGroupEnum;
import com.ericsson.fdp.dao.enums.BatchStatusEnum;
import com.ericsson.fdp.dao.exception.FDPConcurrencyException;

/**
 * The Class AIRDataProcessorServiceImpl.
 * 
 * @author Ericsson
 */
@Stateless
public class AIRDataProcessorServiceImpl implements AIRDataProcessorService {

	/** The Constant LOGGER. */
	private static final org.slf4j.Logger LOGGER = LoggerFactory.getLogger(AIRDataProcessorServiceImpl.class);

	/** The fdp batch jobs dao. */
	@Inject
	private FDPBatchJobsDAO fdpBatchJobsDAO;

	/** The fdp batch execution info dao. */
	@Inject
	private FDPBatchExecutionInfoDAO fdpBatchExecutionInfoDAO;

	@Override
	public Boolean saveBatchExecutionInfo(final String[] xmlMessages, final String circleCode)
			throws ExecutionFailedException, UnknownHostException {

		final FDPBatchJobsDTO fdpBatchJobsDTO = fdpBatchJobsDAO.getBatchJobDTOByGroupNameAndCircleCode(
				BatchJobGroupEnum.AIR_DATA_PROCESSOR.getBatchJobGroupName(), circleCode);
		Boolean isUpdated = false;

		if (fdpBatchJobsDTO != null) {
			FDPBatchExecutionInfoDTO batchExecutionInfoDTO = null;
			final Long batchJobId = fdpBatchJobsDTO.getFdpBatchJobId();
			final String batchJobName = fdpBatchJobsDTO.getFdpBatchJobName();
			try {
				final AirRechargeProcessor airRechargeProcessor = (AirRechargeProcessor) ApplicationConfigUtil
						.getBean(JNDILookupConstant.AIR_RECHARGE_PROCESSOR_LOOK_UP);
				// save batch execution info object.
				final Integer totalBatchCount = xmlMessages.length;
				Integer successCount = 0;
				Integer failureCount = 0;

				batchExecutionInfoDTO = new FDPBatchExecutionInfoDTO(null, batchJobId, Calendar.getInstance(), null,
						BatchStatusEnum.STARTED, null, null, totalBatchCount, null, batchJobName, null);
				final Long batchJobInfoId = fdpBatchExecutionInfoDAO.saveBatchExecutionInfo(batchExecutionInfoDTO);
				final String requestId = ExternalSystem.AIR.name() + "_" + Inet4Address.getLocalHost().getHostAddress()
						+ "_" + (String.valueOf(UUID.randomUUID()));
				// prepare response XML
				for (final String xmlMessage : xmlMessages) {
					if (xmlMessage != null && !xmlMessage.isEmpty()) {
						final String responseXml = airRechargeProcessor.executeAirRecharge(xmlMessage, requestId,
								OperatingMode.OFFLINE, "");
						LOGGER.debug("Output after processing XmlRequest :", responseXml);
						if (responseXml.contains(BusinessConstants.ERROR_CODE_RESPONSE.toString())) {
							failureCount++;
						} else {
							successCount++;
						}
					}
				}

				// update batch job DTO
				BatchStatusEnum finalStatus = null;
				if (totalBatchCount != 0 && totalBatchCount.equals(failureCount)) {
					finalStatus = BatchStatusEnum.FAILED;
				} else {
					finalStatus = BatchStatusEnum.COMPLETED;
				}
				batchExecutionInfoDTO = new FDPBatchExecutionInfoDTO(batchJobInfoId, batchJobId, null,
						Calendar.getInstance(), finalStatus, successCount, failureCount, totalBatchCount, null, null,
						batchJobName);
				fdpBatchExecutionInfoDAO.updateBatchExecutionInfo(batchExecutionInfoDTO);
				isUpdated = true;
			} catch (final FDPConcurrencyException e) {
				LOGGER.error("Batch job execution failed, batch job id = " + batchJobId + "batch execution info id = "
						+ batchExecutionInfoDTO.getBatchExecutionInfoId());
				isUpdated = false;
			} catch (final NamingException e1) {
				LOGGER.error("Batch job execution failed, batch job id = " + batchJobId, e1);
				isUpdated = false;
			}
		}

		return isUpdated;
	}
}
