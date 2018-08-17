package com.ericsson.fdp.business.step.execution.impl;

import java.util.Map;

import javax.ejb.Stateless;

import com.ericsson.fdp.business.constants.FDPStepResponseConstants;
import com.ericsson.fdp.business.dto.emabatch.EMABatchRecordDTO;
import com.ericsson.fdp.business.enums.ServiceStepOptions;
import com.ericsson.fdp.business.util.RequestUtil;
import com.ericsson.fdp.core.exception.ExecutionFailedException;
import com.ericsson.fdp.core.request.FDPRequest;
import com.ericsson.fdp.core.request.FDPStepResponse;

/**
 * The execution service implementation.
 * 
 * @author Ericsson
 * 
 */
@Stateless
public class FDPDefaultEMAServiceImpl extends AbstractFDPEMAServiceImpl {

	@Override
	protected void postProcess(final FDPRequest fdpRequest, final FDPStepResponse fdpStepResponse,
			final Object... additionalInformation) throws ExecutionFailedException {
		final boolean stepExecuted = RequestUtil.checkIfLoggingRequired(fdpStepResponse);

		if (stepExecuted) {
			final EMABatchRecordDTO emaBatchRecordDTO = getEMABatchRecordDTO(fdpRequest, fdpStepResponse,
					additionalInformation);
			RequestUtil.appendResponse(fdpStepResponse, FDPStepResponseConstants.EMA_LOG_VALUE, emaBatchRecordDTO);
		}

	}

	@Override
	public FDPStepResponse performRollback(FDPRequest fdpRequest,
			Map<ServiceStepOptions, String> additionalInformation) {
		// TODO Auto-generated method stub
		return null;
	}

}
