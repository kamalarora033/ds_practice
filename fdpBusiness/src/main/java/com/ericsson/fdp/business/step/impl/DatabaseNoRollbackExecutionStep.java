package com.ericsson.fdp.business.step.impl;

import com.ericsson.fdp.business.NoRollbackOnFailure;
import com.ericsson.fdp.business.exception.StepException;
import com.ericsson.fdp.core.request.FDPRequest;
import com.ericsson.fdp.core.request.FDPStepResponse;
import com.ericsson.fdp.dao.enums.DatabaseStepType;

/**
 * The class defines the database step.
 * 
 * @author Ericsson
 * 
 */
public class DatabaseNoRollbackExecutionStep extends DatabaseStep implements NoRollbackOnFailure {

	/**
	 * 
	 */
	private static final long serialVersionUID = -1714210455543755565L;

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
	public DatabaseNoRollbackExecutionStep(final String jndiLookupName, final Long entityId, final Long stepId,
			final String stepName, final DatabaseStepType stepType) {
		super(jndiLookupName, entityId, stepId, stepName, stepType);
	}

	@Override
	public FDPStepResponse executeStep(final FDPRequest fdpRequest) throws StepException {
		return super.executeStep(fdpRequest);
	}

}
