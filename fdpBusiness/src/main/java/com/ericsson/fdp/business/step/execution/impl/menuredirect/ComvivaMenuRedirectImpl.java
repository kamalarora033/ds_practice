package com.ericsson.fdp.business.step.execution.impl.menuredirect;

import java.util.Map;

import javax.ejb.Stateless;

import ch.qos.logback.classic.Logger;

import com.ericsson.fdp.business.constants.FDPStepResponseConstants;
import com.ericsson.fdp.business.enums.AuxRequestParam;
import com.ericsson.fdp.business.enums.ErrorTypes;
import com.ericsson.fdp.business.enums.ServiceStepOptions;
import com.ericsson.fdp.business.request.impl.FDPStepResponseImpl;
import com.ericsson.fdp.business.step.execution.FDPExecutionService;
import com.ericsson.fdp.business.util.LoggerUtil;
import com.ericsson.fdp.common.constants.FDPConstant;
import com.ericsson.fdp.core.exception.ExecutionFailedException;
import com.ericsson.fdp.core.logging.FDPLogger;
import com.ericsson.fdp.core.request.FDPRequest;
import com.ericsson.fdp.core.request.FDPStepResponse;
import com.ericsson.fdp.dao.enums.SPAddInfoKeyEnum;

/**
 * Comviva Menu Redirect Service.
 * 
 * @author ehlnopu
 *
 */
@Stateless
public class ComvivaMenuRedirectImpl implements FDPExecutionService {

	@Override
	public FDPStepResponse executeService(FDPRequest fdpRequest,
			Object... additionalInformations) throws ExecutionFailedException {
		final Logger circleLogger = LoggerUtil
				.getSummaryLoggerFromRequest(fdpRequest);
		FDPLogger.debug(circleLogger, getClass(), "executeService()",
				"Executing Service for requestId:" + fdpRequest.getRequestId());

		if (additionalInformations != null && additionalInformations[0] != null
				&& additionalInformations[0] instanceof Map<?, ?>) {
			Map<SPAddInfoKeyEnum, String> additionalInfo = (Map<SPAddInfoKeyEnum, String>) additionalInformations[0];
			FDPLogger.debug(circleLogger, getClass(), "executeService()",
					"additionalInfo:" + additionalInfo);
			// put the ServiceStepOptions key here to get the step information

			return getResponseForRequestedAttribute(fdpRequest, circleLogger,
					additionalInfo);
		} else {
			throw new ExecutionFailedException(
					"COMVIVA_MENU_REDIRECT_ATTRIBUTES configuration missing in SP");

		}

	}

	private FDPStepResponse getResponseForRequestedAttribute(
			FDPRequest fdpRequest, Logger circleLogger,
			Map<SPAddInfoKeyEnum, String> additionalInfo) {

		FDPStepResponseImpl stepResponseImpl = new FDPStepResponseImpl();

		/*
		 * SPAddInfoKeyEnum.SP_SERVICE_APPLICATION_RESPONSE;
		 * SPAddInfoKeyEnum.SP_SERVICE_FREE_FLOW_STATE;
		 * SPAddInfoKeyEnum.SP_SERVICE_FREE_FLOW_CHARGING;
		 * SPAddInfoKeyEnum.SP_SERVICE_FREE_FLOW_CHARGING_AMOUNT;
		 */

		stepResponseImpl.addStepResponseValue(
				FDPStepResponseConstants.ERROR_VALUE, additionalInfo
						.get(ServiceStepOptions.SP_SERVICE_APPLICATION_RESPONSE));
		stepResponseImpl.addStepResponseValue(
				FDPStepResponseConstants.ERROR_CODE, additionalInfo
						.get(ServiceStepOptions.SP_SERVICE_APPLICATION_RESPONSE));
		stepResponseImpl
				.addStepResponseValue(
						FDPStepResponseConstants.FREE_FLOW_STATE,
						additionalInfo
								.get(ServiceStepOptions.SP_SERVICE_FREE_FLOW_STATE));
		stepResponseImpl.addStepResponseValue(
				FDPStepResponseConstants.FREE_FLOW_CHARGING, additionalInfo
						.get(ServiceStepOptions.SP_SERVICE_FREE_FLOW_CHARGING));
		stepResponseImpl
				.addStepResponseValue(
						FDPStepResponseConstants.FREE_FLOW_CHARGING_AMOUNT,
						additionalInfo
								.get(ServiceStepOptions.SP_SERVICE_FREE_FLOW_CHARGING_AMOUNT));
		
		stepResponseImpl
		.addStepResponseValue(
				FDPStepResponseConstants.MENU_REDIRECT_CODE,
				additionalInfo
						.get(ServiceStepOptions.SP_SERVICE_MENU_CODE));
		
		stepResponseImpl
		.addStepResponseValue(
				FDPStepResponseConstants.STATUS_KEY,
				true);
		
		
		stepResponseImpl
		.addStepResponseValue(FDPStepResponseConstants.ERROR_TYPE,ErrorTypes.MENU_REDIRECT.toString());
		
		stepResponseImpl
		.addStepResponseValue(FDPStepResponseConstants.EXTERNAL_SYSTEM_TYPE,FDPConstant.COMVIVAUSSD);
		
		
		return stepResponseImpl;
	}

	@Override
	public FDPStepResponse performRollback(FDPRequest fdpRequest,
			Map<ServiceStepOptions, String> additionalInformation) {
		final Logger circleLogger = LoggerUtil
				.getSummaryLoggerFromRequest(fdpRequest);
		FDPLogger.debug(circleLogger, getClass(), "executeService()",
				"Executing Service Rollback is Defined for COMVIVA_MENU_REDIRECT_ATTRIBUTES :"
						+ fdpRequest.getRequestId());
		return null;
	}

}
