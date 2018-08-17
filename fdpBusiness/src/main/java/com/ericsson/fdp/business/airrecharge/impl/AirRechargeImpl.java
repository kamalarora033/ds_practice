package com.ericsson.fdp.business.airrecharge.impl;

import java.io.FileNotFoundException;

import javax.annotation.Resource;

import ch.qos.logback.classic.Logger;

import com.ericsson.fdp.FDPCacheable;
import com.ericsson.fdp.business.airrecharge.AirRecharge;
import com.ericsson.fdp.business.constants.BusinessConstants;
import com.ericsson.fdp.business.exception.RuleException;
import com.ericsson.fdp.business.rule.Rule;
import com.ericsson.fdp.business.util.LoggerUtil;
import com.ericsson.fdp.common.enums.ModuleType;
import com.ericsson.fdp.common.enums.Status;
import com.ericsson.fdp.core.dsm.framework.service.FDPCache;
import com.ericsson.fdp.core.exception.ExecutionFailedException;
import com.ericsson.fdp.core.logging.FDPLogger;
import com.ericsson.fdp.core.request.FDPRequest;
import com.ericsson.fdp.core.request.FDPResponse;
import com.ericsson.fdp.core.serviceprovisioning.bean.FDPMetaBag;

/**
 * This class provides the air recharge implementation.
 * 
 * @author Ericsson
 * 
 */
public class AirRechargeImpl implements AirRecharge {
	
	/**
	 * The meta data cache.
	 */
	@Resource(lookup = "java:app/fdpBusiness-1.0/MetaDataCache")
	private FDPCache<FDPMetaBag, FDPCacheable> fdpMetaDataCache;

	@Override
	public Status executeAirRecharge(final FDPRequest fdpRequest) throws ExecutionFailedException {
		Status executionStatus = Status.FAILURE;
		final Logger circleLogger = LoggerUtil.getSummaryLoggerFromRequest(fdpRequest);
		final FDPCacheable cachedRule = fdpMetaDataCache.getValue(new FDPMetaBag(fdpRequest.getCircle(),
				ModuleType.AIR_RECHARGE, BusinessConstants.AIR_RECHARGE_STEP_ID));
		if (cachedRule instanceof Rule) {
			if (circleLogger.isDebugEnabled()) {
				FDPLogger.debug(circleLogger, getClass(), "executeAirRecharge()", "Executing rule.");
			}
			final Rule airRechargeRule = (Rule) cachedRule;
			try {
				final FDPResponse response = airRechargeRule.execute(fdpRequest);
				executionStatus = response == null ? Status.FAILURE : response.getExecutionStatus();
			} catch (final RuleException e) {
				throw new ExecutionFailedException("The requested recharge could not be done.", e);
			} catch (FileNotFoundException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		} else {
			if (circleLogger.isDebugEnabled()) {
				FDPLogger.debug(circleLogger, getClass(), "executeAirRecharge()",
						"The requested rule could not be found");
			}
		}
		return executionStatus;
	}
}
