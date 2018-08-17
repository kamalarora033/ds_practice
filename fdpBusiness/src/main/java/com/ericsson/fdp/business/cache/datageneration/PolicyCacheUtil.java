package com.ericsson.fdp.business.cache.datageneration;

import java.util.ArrayList;
import java.util.List;

import com.ericsson.fdp.business.enums.AuxRequestParam;
import com.ericsson.fdp.business.exception.ExpressionFailedException;
import com.ericsson.fdp.business.policy.enumeration.PolicyEnum;
import com.ericsson.fdp.business.policy.impl.AbstractPolicy;
import com.ericsson.fdp.business.policy.policyrule.PolicyRule;
import com.ericsson.fdp.business.policy.policyrule.impl.AbstractPolicyRule;
import com.ericsson.fdp.business.util.ExpressionUtil;
import com.ericsson.fdp.dao.dto.common.constraint.SubStepConstraintDTO;
import com.ericsson.fdp.dao.dto.policy.FDPPolicyRuleDTO;
import com.ericsson.fdp.dao.dto.policy.PolicyDTO;

/**
 * The Class PolicyCacheUtil.
 * 
 * @author Ericsson
 */
public class PolicyCacheUtil {

	/**
	 * Instantiates a new policy cache util.
	 */
	private PolicyCacheUtil() {

	}

	/**
	 * Gets the policy for cache.
	 * 
	 * @param policy
	 *            the policy
	 * @return the policy for cache
	 * @throws ExpressionFailedException
	 *             the expression failed exception
	 * @throws IllegalAccessException
	 * @throws InstantiationException
	 * @throws ClassNotFoundException
	 */
	public static AbstractPolicy getPolicyForCache(final PolicyDTO policy) throws ExpressionFailedException,
			InstantiationException, IllegalAccessException, ClassNotFoundException {
		final PolicyEnum policyType = PolicyEnum.getPolicy(policy.getPolicyType());
		AbstractPolicy result = null;
		if (policyType != null) {
			result = policyType.getPolicyClass().newInstance();
			final List<PolicyRule> policyRules = new ArrayList<PolicyRule>();
			for (final FDPPolicyRuleDTO rule : policy.getPolicyRuleSet()) {
				policyRules.add(getPolicyRuleImpl(rule));
			}
			result.setPolicyRules(policyRules);
			result.setPolicyName(policy.getPolicyName());
		}
		return result;
	}

	/**
	 * Gets the policy rule impl.
	 * 
	 * @param rule
	 *            the rule
	 * @return the policy rule impl
	 * @throws ExpressionFailedException
	 *             the expression failed exception
	 * @throws ClassNotFoundException
	 * @throws IllegalAccessException
	 * @throws InstantiationException
	 */
	private static PolicyRule getPolicyRuleImpl(final FDPPolicyRuleDTO rule) throws ExpressionFailedException,
			ClassNotFoundException, InstantiationException, IllegalAccessException {
		AbstractPolicyRule policyRule = null;
		final Class<?> clazz = Class.forName(rule.getPolicyRuleClass());
		final Object obj = clazz.newInstance();
		if (obj instanceof AbstractPolicyRule) {
			policyRule = (AbstractPolicyRule) obj;
			policyRule.setCommandParameterDataType(rule.getCommandParameterDataType());
			policyRule.setAuxRequestParam(AuxRequestParam.getAuxRequestParam(rule.getPolicyRuleKey()));
			policyRule.setClassToBeValidated(rule.getCustomValidatorClass());
			policyRule.setPolicyRuleText(rule.getPolicyRuleText());
			policyRule.setErrorStringForClass(rule.getErrorStringForClass());
			final SubStepConstraintDTO subStep = new SubStepConstraintDTO();
			subStep.setMinValue(rule.getMinValue());
			subStep.setMaxValue(rule.getMaxValue());
			subStep.setCondition(rule.getCondition());
			if (rule.getCondition() != null) {
				policyRule.setCondition(ExpressionUtil.createCondition(subStep, rule.getCommandParameterDataType()));
			}
		}
		return policyRule;
	}
}
