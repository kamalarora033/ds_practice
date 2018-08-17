/**
 * 
 */
package com.ericsson.fdp.business.policy.policyrule.impl;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import com.ericsson.fdp.FDPCacheable;
import com.ericsson.fdp.business.condition.Condition;
import com.ericsson.fdp.business.enums.AuxRequestParam;
import com.ericsson.fdp.business.enums.RequestMetaValuesKey;
import com.ericsson.fdp.business.policy.policyrule.PolicyRule;
import com.ericsson.fdp.business.product.Product;
import com.ericsson.fdp.common.constants.FDPConstant;
import com.ericsson.fdp.core.request.FDPRequest;
import com.ericsson.fdp.dao.enums.CommandParameterDataType;
import com.ericsson.fdp.dao.enums.ProductAdditionalInfoEnum;

/**
 * The Class AbstractPolicyRule.
 * 
 * @author Ericsson
 */
public abstract class AbstractPolicyRule implements PolicyRule {

	private static final long serialVersionUID = 1685511588534932370L;

	/**
	 * The command parameter data type.
	 */
	protected CommandParameterDataType commandParameterDataType;

	/**
	 * The policy rule text.
	 */
	protected String policyRuleText;

	/**
	 * The auxiliary request param.
	 */
	protected AuxRequestParam auxRequestParam;

	/**
	 * The condition to be evaluated for this rule.
	 */
	protected Condition condition;

	/**
	 * The validation class to be used.
	 */
	protected String classToBeValidated;
	
	/**
	 * The error string for this class.
	 */
	protected String errorStringForClass;

	/**
	 * The aux request param map.
	 */
	protected Map<AuxRequestParam, Object> auxRequestParamMap = new HashMap<AuxRequestParam, Object>();
	
	/** Policy Index **/
	private Integer index;
	
	/** Policy Sequence **/
	private Integer sequence;

	/**
	 * @return the commandParameterDataType
	 */
	public CommandParameterDataType getCommandParameterDataType() {
		return commandParameterDataType;
	}

	/**
	 * @param commandParameterDataType
	 *            the commandParameterDataType to set
	 */
	public void setCommandParameterDataType(final CommandParameterDataType commandParameterDataType) {
		this.commandParameterDataType = commandParameterDataType;
	}

	/**
	 * @return the policyRuleText
	 */
	public String getPolicyRuleText() {
		return policyRuleText;
	}

	/**
	 * @param policyRuleText
	 *            the policyRuleText to set
	 */
	public void setPolicyRuleText(final String policyRuleText) {
		this.policyRuleText = policyRuleText;
	}

	/**
	 * @return the auxRequestParam
	 */
	@Override
	public AuxRequestParam getAuxiliaryParam() {
		return auxRequestParam;
	}

	/**
	 * @param auxRequestParam
	 *            the auxRequestParam to set
	 */
	public void setAuxRequestParam(final AuxRequestParam auxRequestParam) {
		this.auxRequestParam = auxRequestParam;
	}

	/**
	 * @return the condition
	 */
	public Condition getCondition() {
		return condition;
	}

	/**
	 * @param condition
	 *            the condition to set
	 */
	public void setCondition(final Condition condition) {
		this.condition = condition;
	}

	/**
	 * @return the classToBeValidated
	 */
	public String getClassToBeValidated() {
		return classToBeValidated;
	}

	/**
	 * @param classToBeValidated
	 *            the classToBeValidated to set
	 */
	public void setClassToBeValidated(final String classToBeValidated) {
		this.classToBeValidated = classToBeValidated;
	}

	/**
	 * @return the errorStringForClass
	 */
	public String getErrorStringForClass() {
		return errorStringForClass;
	}

	/**
	 * @param errorStringForClass
	 *            the errorStringForClass to set
	 */
	public void setErrorStringForClass(final String errorStringForClass) {
		this.errorStringForClass = errorStringForClass;
	}

	/**
	 * @return the auxRequestParamMap
	 */
	@Override
	public Map<AuxRequestParam, Object> getRuleValues() {
		return auxRequestParamMap;
	}

	/**
	 * @param auxRequestParamMap
	 *            the auxRequestParamMap to set
	 */
	public void setAuxRequestParamMap(final Map<AuxRequestParam, Object> auxRequestParamMap) {
		this.auxRequestParamMap = auxRequestParamMap;
	}
	
	@Override
	public boolean isRuleNoInputRequired() {
		return false;
	}

	/**
	 * @return the index
	 */
	public Integer getIndex() {
		return index;
	}

	/**
	 * @param index the index to set
	 */
	public void setIndex(Integer index) {
		this.index = index;
	}

	/**
	 * @return the sequence
	 */
	public Integer getSequence() {
		return sequence;
	}

	/**
	 * @param sequence the sequence to set
	 */
	public void setSequence(Integer sequence) {
		this.sequence = sequence;
	}
	
	/**
	 * Method to evaluate the policy rules, in case of Additional Policy configured from Product Additional Notification Product screen.
	 * 
	 * @param fdpRequest
	 * @param productAdditionalInfoEnum
	 * @return
	 */
	protected boolean evaluateAdditionalPolicyRule(final FDPRequest fdpRequest, final ProductAdditionalInfoEnum productAdditionalInfoEnum) {
		boolean needToExecute = false;
		final FDPCacheable fdpCacheable = fdpRequest.getValueFromRequest(RequestMetaValuesKey.PRODUCT);
		if(fdpCacheable instanceof Product) {
			final Product product = (Product) fdpCacheable;
			needToExecute = (null != product.getAdditionalInfo(productAdditionalInfoEnum) && Boolean.valueOf(product.getAdditionalInfo(productAdditionalInfoEnum)));
			needToExecute = (!needToExecute && null != product.getAdditionalInfo(productAdditionalInfoEnum)) ? (Arrays.asList(product.getAdditionalInfo(productAdditionalInfoEnum).split(FDPConstant.PIPE_DELIMITER)).contains(fdpRequest.getExternalSystemToCharge().name())) : needToExecute;
		}
		return needToExecute;
	}
}
