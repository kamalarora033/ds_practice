package com.ericsson.fdp.business.step.impl;

import java.util.Map;

import javax.naming.NamingException;

import ch.qos.logback.classic.Logger;

import com.ericsson.fdp.business.constants.FDPStepResponseConstants;
import com.ericsson.fdp.business.exception.StepException;
import com.ericsson.fdp.business.request.impl.FDPStepResponseImpl;
import com.ericsson.fdp.business.sharedaccount.SharedAccountService;
import com.ericsson.fdp.business.step.FDPStep;
import com.ericsson.fdp.business.util.LoggerUtil;
import com.ericsson.fdp.core.exception.ExecutionFailedException;
import com.ericsson.fdp.core.logging.FDPLogger;
import com.ericsson.fdp.core.request.FDPRequest;
import com.ericsson.fdp.core.request.FDPStepResponse;
import com.ericsson.fdp.core.utils.ApplicationConfigUtil;
import com.ericsson.fdp.dao.enums.DatabaseStepType;
import com.ericsson.fdp.dao.enums.SharedAccountResponseType;

/**
 * The class defines the database step.
 * 
 * @author Ericsson
 * 
 */
public class DatabaseStep implements FDPStep {

	/**
	 * 
	 */
	private static final long serialVersionUID = -3791766754221041212L;

	// TODO: this should be an enum, same as used by Prateek.
	private final String jndiLookupName;

	/**
	 * The entity id for which this step is to be executed.
	 */
	private final Long entityId;

	/**
	 * The step name.
	 */
	private final String stepName;

	/**
	 * The step id.
	 */
	private final Long stepId;

	/** The step type. */
	private DatabaseStepType stepType;

	private boolean executed;

	/**
	 * Instantiates a new database step.
	 * 
	 * @param jndiLookupName
	 *            the jndi lookup name
	 * @param entityId
	 *            the entity id
	 * @param stepId
	 *            the step id
	 * @param stepName
	 *            the step name
	 * @param stepType
	 *            the step type
	 */
	public DatabaseStep(final String jndiLookupName, final Long entityId, final Long stepId, final String stepName,
			final DatabaseStepType stepType) {
		this.entityId = entityId;
		this.jndiLookupName = jndiLookupName;
		this.stepId = stepId;
		this.stepName = stepName;
		this.stepType = stepType;
	}

	@Override
	public FDPStepResponse executeStep(final FDPRequest fdpRequest) throws StepException {
		final Logger logger = LoggerUtil.getSummaryLoggerFromRequest(fdpRequest);
		final FDPStepResponseImpl fdpStepResponseImpl = new FDPStepResponseImpl();
		fdpStepResponseImpl.addStepResponseValue(FDPStepResponseConstants.STATUS_KEY, false);
		try {
			final Object beanObject = ApplicationConfigUtil.getBean(jndiLookupName);
			if (beanObject instanceof SharedAccountService) {
				final SharedAccountService sharedAccountService = (SharedAccountService) beanObject;
				final Map<SharedAccountResponseType, Object> stepExecutedValue = sharedAccountService
						.executeSharedAccountService(fdpRequest, entityId);
				for (final Map.Entry<SharedAccountResponseType, Object> entrySet : stepExecutedValue.entrySet()) {
					if (logger.isDebugEnabled()) {
						FDPLogger.debug(logger, getClass(), "executeStep()", "Values found for key :- "
								+ entrySet.getKey().name() + " value found :-" + entrySet.getValue());
					}
					fdpStepResponseImpl.addStepResponseValue(entrySet.getKey().name(), entrySet.getValue());
				}
			}
		} catch (final NamingException e) {
			throw new StepException("The bean could not be found " + jndiLookupName, e);
		} catch (final ExecutionFailedException e) {
			throw new StepException("The execution for step failed ", e);
		}
		return fdpStepResponseImpl;
	}

	@Override
	public String getStepName() {
		return stepName;
	}

	/**
	 * This method is used to get the step id.
	 * 
	 * @return the step id.
	 */
	public Long getStepId() {
		return stepId;
	}

	/**
	 * @return the stepType
	 */
	public DatabaseStepType getStepType() {
		return stepType;
	}

	/**
	 * @param stepType
	 *            the stepType to set
	 */
	public void setStepType(final DatabaseStepType stepType) {
		this.stepType = stepType;
	}

	@Override
	public boolean isStepExecuted() {
		return executed;
	}


	@Override
	public void setStepExecuted(boolean stepexecuted) {
		this.executed=stepexecuted;
	}
	
}
