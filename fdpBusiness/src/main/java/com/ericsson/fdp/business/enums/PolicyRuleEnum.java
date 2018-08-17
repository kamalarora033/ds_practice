package com.ericsson.fdp.business.enums;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import com.ericsson.fdp.business.policy.policyrule.PolicyRule;
import com.ericsson.fdp.business.policy.policyrule.impl.AbstractPolicyRule;
import com.ericsson.fdp.business.policy.policyrule.impl.FlexiRechargeTime4UInputAmount;
import com.ericsson.fdp.business.policy.policyrule.impl.FlexiRechargeTime4UPolicyRuleImpl;
import com.ericsson.fdp.business.policy.policyrule.impl.OptInBuyProductPolicyRuleImpl;
import com.ericsson.fdp.business.policy.policyrule.impl.UserInputThresholdPolicyImpl;
import com.ericsson.fdp.dao.enums.ProductAdditionalInfoEnum;

/**
 * The Class PolicyRuleEnum 
 * @author ericsson
 */
public enum PolicyRuleEnum {

	/** The User consent based confirmation policy */
	//USER_CONSENT_BASED_CONFIRMATION_POLICY_RULE(new UserConsentBasedConfirmationPolicyRuleImpl(),3, ProductAdditionalInfoEnum.TIME4U_INPUT_VOUCHER_PIN.name()),
	
	/** The TIME4U_INPUT_VOUCHER_PIN_POLICY_RULE Policy **/ 
	TIME4U_INPUT_VOUCHER_PIN_POLICY_RULE(new FlexiRechargeTime4UPolicyRuleImpl(),2,ProductAdditionalInfoEnum.TIME4U_INPUT_VOUCHER_PIN.name(),1),
	
	/** The TIME4U_INPUT_AMOUNT_POLICY_RULE **/
	TIME4U_INPUT_AMOUNT_POLICY_RULE(new FlexiRechargeTime4UInputAmount(),2,ProductAdditionalInfoEnum.TIME4U_INPUT_AMOUNT.name(),2),
	
	/** The USER_THRESHOLD_USER_INPUTPOLICY_RULE **/
	USER_THRESHOLD_USER_INPUTPOLICY_RULE(new UserInputThresholdPolicyImpl(),3,ProductAdditionalInfoEnum.USER_THRESHOLD_POLICY.name(),1),
	
	OPT_IN_OPT_OUT_PRODUCT(new OptInBuyProductPolicyRuleImpl(),3, ProductAdditionalInfoEnum.RS_OPT_IN_OPT_OUT.name(),1);
	
	
	/** The index. */
	private Integer index;
	
	/** The policy name. */
	private PolicyRule policyRule;
	
	/** The Policy Identifier **/
	private String identifier;
	
	/** The Policy Sequence **/
	private Integer sequence;
	
	/** The Constructor 
	 * @param index
	 * @param policyName
	 * @param index 
	 */
	private PolicyRuleEnum(final PolicyRule policyRule, final Integer index, final String identifier, final Integer sequence) {		
		this.policyRule = policyRule;
		this.index = index;
		this.identifier = identifier;
		this.sequence = sequence;
	}

	/**
	 * @return the index
	 */
	public Integer getIndex() {
		return index;
	}

	/**
	 * @return the policyRule
	 */
	public PolicyRule getPolicyRule() {
		return policyRule;
	}

	/**
	 * @return the identifier
	 */
	public String getIdentifier() {
		return identifier;
	}
	
	/**
	 * This method make customzed policy rules to execute.
	 * 
	 * @param policyRulesInput
	 * @param policyRuleIdentifiers
	 * @return
	 */
	public static List<PolicyRule> updatePolicyRule(final List<PolicyRule> policyRulesInput, final List<String> policyRuleIdentifiers) {
		final List<PolicyRule> rawRuleList = new ArrayList<>(policyRulesInput);
		updateMandatoryPolicyIndexAndSeq(rawRuleList);
		getCustomPolicyRules(policyRuleIdentifiers, rawRuleList);
		//rawRuleList.addAll(FDPConstant.POLICY_DEFAULT_CHANGE_OFFSET_POSITON, toAddPOlicyRules);
		Collections.sort(rawRuleList, formatePolicyRules());
		return rawRuleList;
	}
	
	/**
	 * This method checks the all applicable POlicy Rules as per passed policy Rules Identifiers.
	 * 
	 * @param policyRuleIdentifiers
	 * @return
	 */
	private static void getCustomPolicyRules(final List<String> policyRuleIdentifiers, final List<PolicyRule> mandatoryRuleList) {
		for(final PolicyRuleEnum policyRuleEnum : PolicyRuleEnum.values()) {
			if(null != policyRuleIdentifiers && policyRuleIdentifiers.contains(policyRuleEnum.getIdentifier())) {
				final PolicyRule policyRule = policyRuleEnum.getPolicyRule();
				((AbstractPolicyRule)policyRule).setIndex(policyRuleEnum.getIndex());
				((AbstractPolicyRule)policyRule).setSequence(policyRuleEnum.getSequence());
				mandatoryRuleList.add(policyRuleEnum.getPolicyRule());
			}
 		}
	}
	
	/**
	 * This method will set the index and Sequence.
	 * 
	 * @param policyRules
	 */
	private static void updateMandatoryPolicyIndexAndSeq(final List<PolicyRule> policyRules) {
		int index = 0;
		for(final PolicyRule policyRule : policyRules) {
			((AbstractPolicyRule)policyRule).setIndex(index);
			((AbstractPolicyRule)policyRule).setSequence(0);;
			index++;
		}
	}

	/**
	 * @return the sequence
	 */
	public Integer getSequence() {
		return sequence;
	}
	
	private static Comparator<PolicyRule> formatePolicyRules() {
		return new Comparator<PolicyRule>() {

			@Override
			public int compare(PolicyRule o1, PolicyRule o2) {
				AbstractPolicyRule ao1 = (AbstractPolicyRule) o1;
				AbstractPolicyRule ao2 = (AbstractPolicyRule) o2;
				int value = 0;
				if(ao1.getIndex() == ao2.getIndex()) {
					value = Integer.valueOf(ao1.getSequence()).compareTo(ao2.getSequence());
				} else {
					value = Integer.valueOf(ao1.getIndex()).compareTo(ao2.getIndex());
				}
				return value;
			}
		};
	}
}
