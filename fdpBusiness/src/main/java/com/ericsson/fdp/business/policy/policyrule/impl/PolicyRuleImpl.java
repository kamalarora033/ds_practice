package com.ericsson.fdp.business.policy.policyrule.impl;

import java.util.HashMap;
import java.util.Map;

import javax.inject.Inject;

import com.ericsson.fdp.business.enums.AuxRequestParam;
import com.ericsson.fdp.business.enums.PolicyStatus;
import com.ericsson.fdp.business.enums.PolicyValidationMessageEnum;
import com.ericsson.fdp.business.enums.TLVOptions;
import com.ericsson.fdp.business.policy.policyrule.PolicyRule;

import com.ericsson.fdp.business.policy.policyrule.validators.RuleValidator;
import com.ericsson.fdp.business.request.FDPPolicyResponse;
import com.ericsson.fdp.business.request.impl.FDPPolicyResponseImpl;
import com.ericsson.fdp.business.request.impl.FDPResponseImpl;
import com.ericsson.fdp.business.util.LoggerUtil;
import com.ericsson.fdp.business.util.ResponseUtil;
import com.ericsson.fdp.common.constants.FDPConstant;
import com.ericsson.fdp.common.enums.Status;
import com.ericsson.fdp.common.exception.ConditionFailedException;
import com.ericsson.fdp.core.exception.EvaluationFailedException;
import com.ericsson.fdp.core.exception.ExecutionFailedException;
import com.ericsson.fdp.core.request.FDPRequest;
import com.ericsson.fdp.core.request.FDPResponse;

/**
 * This class implements the policy rule.
 * 
 * @author Ericsson
 * 
 */
public class PolicyRuleImpl extends AbstractPolicyRule implements PolicyRule {

	/**
	 * The serial version id.
	 */
	private static final long serialVersionUID = -1038556695782960482L;
	
	
	

	@Override
	public FDPResponse displayRule(final FDPRequest fdpRequest) throws ExecutionFailedException {
		return new FDPResponseImpl(Status.SUCCESS, false, ResponseUtil.createResponseMessageInList(
				fdpRequest.getChannel(), policyRuleText, TLVOptions.SESSION_CONTINUE));
	}

	@Override
	public FDPPolicyResponse validatePolicyRule(final Object input, final FDPRequest fdpRequest,
			final Object... otherParams) throws ExecutionFailedException {
		FDPPolicyResponse response = new FDPPolicyResponseImpl(PolicyStatus.SUCCESS, null, null, true, null);
		LoggerUtil.generatePolicyBehaviourLogsForUserBehaviour(fdpRequest, getAuxiliaryParam().getName());
		// In case only validation is for class and no input is required.
		if (input == null && condition != null) {
			response = new FDPPolicyResponseImpl(PolicyStatus.POLICY_VALUE_NOT_FOUND, null, null, false, null);
		} else {
			
			boolean isValid = PolicyRuleValidateImpl.isNullorEmpty(input);
			try {
				if (isValid) {
					isValid = false;
					auxRequestParamMap.put(auxRequestParam, input);
					if ((condition == null) || condition.evaluate(input, fdpRequest, false)) {
						isValid = true;
						response = validateClass(input, otherParams, fdpRequest);
					}
				}

				if (!isValid) {
					response = new FDPPolicyResponseImpl(PolicyStatus.FAILURE, PolicyRuleValidateImpl.errorMsg(PolicyValidationMessageEnum.ERROR_MSG, null), null, true,
							ResponseUtil.createResponseMessageInList(fdpRequest.getChannel(), PolicyRuleValidateImpl.errorMsg(PolicyValidationMessageEnum.ERROR_MSG, null),
									TLVOptions.SESSION_TERMINATE));
				}
			} catch (final ConditionFailedException e) {
				throw new ExecutionFailedException("The policy rule could not be evaluated.", e);
			}
		}
		return response;
	}

	/**
	 * This method is used to validate the input against the class provided.
	 * 
	 * @param input
	 *            The input parameter.
	 * @param otherParams
	 *            The other parameters provided.
	 * @param fdpRequest
	 * @return the response string, if validation fails, null otherwise.
	 * @throws ExecutionFailedException
	 *             Exception in execution.
	 */
	private FDPPolicyResponse validateClass(final Object input, final Object[] otherParams, final FDPRequest fdpRequest)
			throws ExecutionFailedException {
		FDPPolicyResponse response = null;
		if (otherParams == null || otherParams.length == 0) {
			throw new ExecutionFailedException("The validation of policy rule cannot be done as inputs are missing");
		}
		if (classToBeValidated != null) {
			try {
				final Class<?> clazz = Class.forName(classToBeValidated);
				final RuleValidator classInstance = (RuleValidator) clazz.newInstance();
				final Map<Object, Object> from = new HashMap<Object, Object>();
				from.put(FDPConstant.MSISDN, input);
				from.put(FDPConstant.CONFIRMATION_KEY, otherParams[1]);
				final Map<Object, Object> to = new HashMap<Object, Object>();
				to.put(FDPConstant.MSISDN, otherParams[FDPConstant.FIRST_INDEX]);
				if (!classInstance.validate(from, to)) {
					response = new FDPPolicyResponseImpl(PolicyStatus.FAILURE, errorStringForClass, null, true,
							ResponseUtil.createResponseMessageInList(fdpRequest.getChannel(), errorStringForClass,
									TLVOptions.SESSION_TERMINATE));
				}
			} catch (final ClassNotFoundException e) {
				throw new ExecutionFailedException("The validation of policy rule cannot be done as class is missing",
						e);
			} catch (final InstantiationException e) {
				throw new ExecutionFailedException(
						"The validation of policy rule cannot be done as class instantiation could not be done", e);
			} catch (final IllegalAccessException e) {
				throw new ExecutionFailedException(
						"The validation of policy rule cannot be done as class access could not be done", e);
			} catch (final EvaluationFailedException e) {
				throw new ExecutionFailedException(
						"The validation of policy rule cannot be done as rule could not be validated", e);
			}
		}
		return response;
	}

	public void addAuxRequestParamValue(final AuxRequestParam auxRequestParam, final Object object) {
		if (auxRequestParamMap == null) {
			auxRequestParamMap = new HashMap<AuxRequestParam, Object>();
		}
		auxRequestParamMap.put(auxRequestParam, object);
	}
}
