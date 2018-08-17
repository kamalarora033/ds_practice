/**
 * 
 */
package com.ericsson.fdp.business.batchjob.pam.service.impl;

import java.util.Calendar;
import java.util.List;

import javax.ejb.Stateless;
import javax.inject.Inject;

import com.ericsson.fdp.business.batchjob.pam.service.PAMCleanUpService;
import com.ericsson.fdp.dao.batch.FDPBatchJobsDAO;
import com.ericsson.fdp.dao.batch.FDPPAMCleanUpDAO;
import com.ericsson.fdp.dao.dto.FDPBatchJobsDTO;
import com.ericsson.fdp.dao.dto.PAMCleanUpDTO;

/**
 * The Class PAMCleanUpServiceImpl.
 * 
 * @author Ericsson
 */
@Stateless
public class PAMCleanUpServiceImpl implements PAMCleanUpService {

	/** The fdp pam clean up dao. */
	@Inject
	FDPPAMCleanUpDAO fdpPAMCleanUpDAO;

	/** The fdp batch jobs dao. */
	@Inject
	FDPBatchJobsDAO fdpBatchJobsDAO;

	@Override
	public Long initializePAMJob(PAMCleanUpDTO pamCleanUpDTO) {
		return fdpPAMCleanUpDAO.insertPAMCleanUpJob(pamCleanUpDTO);
	}

	@Override
	public void updatePAMJob(PAMCleanUpDTO pamCleanUpDTO) {
		fdpPAMCleanUpDAO.updatePAMCleanUpJob(pamCleanUpDTO);
	}

	@Override
	public List<FDPBatchJobsDTO> getAllBatchJobs() {
		List<FDPBatchJobsDTO> allBatchJobs = fdpBatchJobsDAO.getAllBatchJobs();
		return allBatchJobs;
	}

	@Override
	public List<String> getAllBatchJobCircles() {
		List<String> circleList = fdpBatchJobsDAO.getAllBatchJobCircles();
		return circleList;
	}

	@Override
	public List<String> getAllBatchJobNames() {
		List<String> jobNameList = fdpBatchJobsDAO.getAllBatchJobNames();
		return jobNameList;
	}

	@Override
	public FDPBatchJobsDTO getBatchJobDetails(String circleCode, String batchJobName) {
		return fdpBatchJobsDAO.getBatchJobDetails(circleCode, batchJobName);
	}

	@Override
	public void updateBatchJobCronExpression(Long batchJobId, String cronExpression, String modifiedBy,
			Calendar modifiedOn) {
		fdpBatchJobsDAO.updateBatchJobCronExpression(batchJobId, cronExpression, modifiedBy, modifiedOn);
	}
	
}
