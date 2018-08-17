package com.ericsson.fdp.business.step.impl;

import java.util.HashMap;
import java.util.Map;

import javax.naming.NamingException;

import com.ericsson.fdp.business.constants.FDPStepResponseConstants;
import com.ericsson.fdp.business.enums.AuxRequestParam;
import com.ericsson.fdp.business.enums.ServiceStepOptions;
import com.ericsson.fdp.business.exception.StepException;
import com.ericsson.fdp.business.step.FDPStep;
import com.ericsson.fdp.business.step.execution.FDPExecutionService;
import com.ericsson.fdp.business.util.RequestUtil;
import com.ericsson.fdp.core.exception.ExecutionFailedException;
import com.ericsson.fdp.core.request.FDPRequest;
import com.ericsson.fdp.core.request.FDPStepResponse;
import com.ericsson.fdp.core.utils.ApplicationConfigUtil;

/**
 * The class defines the database step.
 * 
 * @author Ericsson
 * 
 */
public class ServiceStep implements FDPStep {

	/**
	 *
	 */
	private static final long serialVersionUID = -8145754457388092389L;

	private final String jndiLookupName;

	/**
	 * The step name.
	 */
	private final String stepName;

	/**
	 * The step id.
	 */
	private final Long stepId;

	/**
	 * This map is used to store the additional information on
	 */
	private Map<ServiceStepOptions, String> additionalInformation;

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
	public ServiceStep(final String jndiLookupName, final Long stepId, final String stepName) {
		this.jndiLookupName = jndiLookupName;
		this.stepId = stepId;
		this.stepName = stepName;
	}

	@SuppressWarnings("unchecked")
	@Override
	public FDPStepResponse executeStep(final FDPRequest fdpRequest) throws StepException {
		FDPStepResponse fdpStepResponseImpl = null;
		try {
			final Object beanObject = ApplicationConfigUtil.getBean(jndiLookupName);
			if (beanObject instanceof FDPExecutionService) {
				final FDPExecutionService executionService = (FDPExecutionService) beanObject;
				fdpStepResponseImpl = executionService.executeService(fdpRequest, additionalInformation);
				RequestUtil.putAuxiliaryValueInRequest(fdpRequest, (Map<AuxRequestParam, Object>) fdpStepResponseImpl
						.getStepResponseValue(FDPStepResponseConstants.AUX_PARAM_VALUES));
			}
		} catch (final NamingException e) {
			throw new StepException("The bean could not be found " + jndiLookupName, e);
		} catch (final ExecutionFailedException e) {
			throw new StepException("The execution for step failed ", e);
		}
		return fdpStepResponseImpl;
	}

	public String getJndiLookupName() {
		return jndiLookupName;
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
	 * @return the additionalInformation
	 */
	public Map<ServiceStepOptions, String> getAdditionalInformation() {
		return additionalInformation;
	}

	/**
	 * @param additionalInformation
	 *            the additionalInformation to set
	 * @param serviceStepOptions
	 *            the serviceStepOptions to set.
	 */
	public void putAdditionalInformation(final ServiceStepOptions serviceStepOptions, final String additionalInformation) {
		if (this.additionalInformation == null) {
			this.additionalInformation = new HashMap<ServiceStepOptions, String>();
		}
		this.additionalInformation.put(serviceStepOptions, additionalInformation);
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
