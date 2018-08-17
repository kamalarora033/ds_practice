package com.ericsson.fdp.business.step.execution.impl;

import java.util.Map;

import javax.ejb.Stateless;

import com.ericsson.fdp.business.dto.emabatch.EMABatchRecordDTO;
import com.ericsson.fdp.business.enums.ServiceStepOptions;
import com.ericsson.fdp.business.util.RequestUtil;
import com.ericsson.fdp.core.exception.ExecutionFailedException;
import com.ericsson.fdp.core.logging.FDPLogger;
import com.ericsson.fdp.core.logging.FDPLoggerFactory;
import com.ericsson.fdp.core.request.FDPRequest;
import com.ericsson.fdp.core.request.FDPStepResponse;
import com.google.gson.Gson;

/**
 * The execution service implementation.
 * 
 * @author Ericsson
 * 
 */
@Stateless
public class FDPLogEMAServiceImpl extends AbstractFDPEMAServiceImpl {

	@Override
	protected void postProcess(final FDPRequest fdpRequest, final FDPStepResponse fdpStepResponse,
			final Object... additionalInformation) throws ExecutionFailedException {
		if (additionalInformation.length < 2) {
			throw new ExecutionFailedException("Addtional info size is less than 2.");
		}
		final boolean stepExecuted = RequestUtil.checkIfLoggingRequired(fdpStepResponse);

		if (stepExecuted) {
			final EMABatchRecordDTO emaBatchRecordDTO = getEMABatchRecordDTO(fdpRequest, fdpStepResponse,
					additionalInformation);
			FDPLogger.info(FDPLoggerFactory.getEMALogger(fdpRequest.getCircle().getCircleName()), this.getClass(),
					"postProcess()", new Gson().toJson(emaBatchRecordDTO));
		}

	}
}
