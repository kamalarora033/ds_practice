package com.ericsson.fdp.business.policy.enumeration;

import com.ericsson.fdp.business.policy.impl.AbstractPolicy;
import com.ericsson.fdp.business.policy.impl.PolicyImpl;
import com.ericsson.fdp.business.policy.impl.ServiceProvPolicyImpl;

/**
 * The Enum PolicyEnum.
 * 
 * @author Ericsson
 */
public enum PolicyEnum {

	/** The policy. */
	POLICY(1, PolicyImpl.class),

	/** The sp policy. */
	SP_POLICY(2, ServiceProvPolicyImpl.class);

	/** The policy type. */
	private Integer policyType;

	/** The policy class. */
	private Class<? extends AbstractPolicy> policyClass;

	/**
	 * Instantiates a new policy enum.
	 * 
	 * @param policyType
	 *            the policy type
	 */
	private PolicyEnum(final Integer policyType, final Class<? extends AbstractPolicy> clazz) {
		this.policyType = policyType;
		this.policyClass = clazz;
	}

	/**
	 * Gets the policy type.
	 * 
	 * @return the policy type
	 */
	public Integer getPolicyType() {
		return policyType;
	}

	/**
	 * Gets the policy class.
	 * 
	 * @return the policy class
	 */
	public Class<? extends AbstractPolicy> getPolicyClass() {
		return policyClass;
	}

	/**
	 * Gets the policy.
	 * 
	 * @param policyType
	 *            the policy type
	 * @return the policy
	 */
	public static PolicyEnum getPolicy(final Integer policyType) {
		PolicyEnum result = null;
		for (final PolicyEnum value : PolicyEnum.values()) {
			if (value.getPolicyType().equals(policyType)) {
				result = value;
				break;
			}
		}
		return result;
	}
}
