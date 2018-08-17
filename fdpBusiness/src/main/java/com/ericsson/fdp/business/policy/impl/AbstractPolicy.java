package com.ericsson.fdp.business.policy.impl;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import com.ericsson.fdp.business.enums.AuxRequestParam;
import com.ericsson.fdp.business.enums.PolicyStatus;
import com.ericsson.fdp.business.enums.RequestMetaValuesKey;
import com.ericsson.fdp.business.node.impl.OptInOptOutServiceNode;
import com.ericsson.fdp.business.policy.FDPExecutesServiceProv;
import com.ericsson.fdp.business.policy.policyrule.PolicyRule;
import com.ericsson.fdp.business.policy.policyrule.impl.ActiveServicePolicyRuleImpl;
import com.ericsson.fdp.business.policy.policyrule.impl.OptInOptOutPolicyRuleImpl;
import com.ericsson.fdp.business.policy.policyrule.impl.PendingRequestPolicyRuleImpl;
import com.ericsson.fdp.business.product.Product;
import com.ericsson.fdp.business.request.FDPPolicyResponse;
import com.ericsson.fdp.business.request.impl.FDPResponseImpl;
import com.ericsson.fdp.business.serviceprovisioning.rule.ServiceProvisioningRule;
import com.ericsson.fdp.business.util.RequestUtil;
import com.ericsson.fdp.common.constants.FDPConstant;
import com.ericsson.fdp.common.enums.Status;
import com.ericsson.fdp.core.exception.ExecutionFailedException;
import com.ericsson.fdp.core.node.FDPNode;
import com.ericsson.fdp.core.policy.Policy;
import com.ericsson.fdp.core.request.FDPRequest;
import com.ericsson.fdp.core.request.FDPResponse;
import com.ericsson.fdp.core.request.impl.FDPRequestImpl;
import com.ericsson.fdp.dao.enums.FDPServiceProvSubType;
import com.ericsson.fdp.dao.enums.FDPServiceProvType;
import com.ericsson.fdp.dao.util.ServiceProvDataConverterUtil;

public abstract class AbstractPolicy implements Policy {

	private static final long serialVersionUID = 1358424932571140454L;

	/** The policy name. */
	private String policyName;

	/**
	 * The policy rules for this policy.
	 */
	protected List<PolicyRule> policyRules;

	/**
	 * The last executed index.
	 */
	protected Integer lastExecutedIndex;

	/**
	 * The rule values that have been evaluated.
	 */
	protected final Map<AuxRequestParam, Object> ruleValues = new HashMap<AuxRequestParam, Object>();

	boolean policyExecution = true;

	/**
	 * @return policy rules.
	 */
	public List<PolicyRule> getPolicyRules() {
		return policyRules;
	}

	public void setPolicyRules(final List<PolicyRule> policyRules) {
		this.policyRules = policyRules;
	}

	/**
	 * @return last executed index.
	 */
	public Integer getLastExecutedIndex() {
		return lastExecutedIndex;
	}

	/**
	 * @param lastExecutedIndex
	 *            the last executed index.
	 */
	public void setLastExecutedIndex(final Integer lastExecutedIndex) {
		this.lastExecutedIndex = lastExecutedIndex;
	}

	@Override
	public Map<AuxRequestParam, Object> getRuleValues() {
		return ruleValues;
	}

	/**
	 * @return the policyName
	 */
	@Override
	public String getPolicyName() {
		return policyName;
	}

	/**
	 * @param policyName
	 *            the policyName to set
	 */
	public void setPolicyName(final String policyName) {
		this.policyName = policyName;
	}

	@Override
	public FDPResponse executePolicy(final FDPRequest fdpRequest, final Object... params)
			throws ExecutionFailedException {
		FDPResponse fdpResponse = null;
		final Object currentInput = fdpRequest
				.getAuxiliaryRequestParameter(AuxRequestParam.CURRENT_SHARED_ACCOUNT_VALUE);
		//Integer lastExecutedIndex = fdpRequest.getLastExecutedIndex();
		if (lastExecutedIndex == null) {
			lastExecutedIndex = FDPConstant.FIRST_INDEX;
			//fdpRequest.setLastExecutedIndex(FDPConstant.FIRST_INDEX);
		}
		if (currentInput != null && currentInput instanceof List<?>) {
			fdpResponse = executePolicy(fdpRequest, currentInput, true, params);
		} else {
			fdpResponse = executeCurrentPolicyRule(fdpRequest, params);
		}
		return fdpResponse;

	}

	/**
	 * This method is used to execute policy.
	 * 
	 * @param fdpRequest
	 *            the request object.
	 * @param currentInput
	 *            the current input.
	 * @param params
	 *            the other parameters.
	 * @return teh response of executing policy.
	 * @throws ExecutionFailedException
	 *             exception in execution.
	 */
	@SuppressWarnings("unchecked")
	private FDPResponse executePolicy(final FDPRequest fdpRequest, final Object currentInput,
			final boolean continueEvaluation, final Object... params) throws ExecutionFailedException {
		final List<String> currentInputList = (List<String>) currentInput;
		FDPResponse fdpResponse = null;
		int currIndex = 0;
		boolean errorOccurred = false;
		while (/*fdpRequest.getLastExecutedIndex()*/lastExecutedIndex < policyRules.size()) {
			//final PolicyRule policyRule = policyRules.get(fdpRequest.getLastExecutedIndex());
			final PolicyRule policyRule = policyRules.get(lastExecutedIndex);
			final String currentRuleInput = currentInputList == null ? FDPConstant.EMPTY_STRING
					: (currIndex < currentInputList.size() ? currentInputList.get(currIndex) : null);
			final FDPPolicyResponse policyRuleResponse = policyRule.validatePolicyRule(currentRuleInput, fdpRequest,
					fdpRequest.getSubscriberNumber(), ruleValues.get(AuxRequestParam.CONFIRMATION),
					params != null ? params[0] : null);
			if (policyRuleResponse == null || PolicyStatus.SUCCESS.equals(policyRuleResponse.getPolicyStatus())) {
				policyRuleSuccess(policyRule, currentRuleInput);
				lastExecutedIndex++;
				//fdpRequest.setLastExecutedIndex(fdpRequest.getLastExecutedIndex()+1);
				break;
			} else if (PolicyStatus.POLICY_VALUE_NOT_FOUND.equals(policyRuleResponse.getPolicyStatus())) {
				break;
			} else if (PolicyStatus.FAILURE.equals(policyRuleResponse.getPolicyStatus())) {
				fdpResponse = new FDPResponseImpl(Status.SUCCESS, policyRuleResponse.terminateSession(),
						policyRuleResponse.getResponseMessages());
				errorOccurred = true;
				break;
			} else if (PolicyStatus.RETRY_POLICY.equals(policyRuleResponse.getPolicyStatus())) {
				fdpResponse = new FDPResponseImpl(Status.SUCCESS, policyRuleResponse.terminateSession(),
						policyRuleResponse.getResponseMessages());
				RequestUtil.putAuxiliaryValueInRequest(fdpRequest, AuxRequestParam.POLICY_RETRY_VALUE,
						policyRuleResponse.policyRetryNumber());
				break;
			} else if (PolicyStatus.EXECUTE_SP.equals(policyRuleResponse.getPolicyStatus())) {				
				if(null != fdpRequest.getAuxiliaryRequestParameter(AuxRequestParam.OPTINOPTOUT_NODE) && FDPConstant.OPTIN_OPTOUT_NODE_TYPE.equalsIgnoreCase(fdpRequest.getAuxiliaryRequestParameter(AuxRequestParam.OPTINOPTOUT_NODE).toString())){
					if(null != fdpRequest.getAuxiliaryRequestParameter(AuxRequestParam.RS_PRODUCT_ID)){
						final String productIds = fdpRequest.getAuxiliaryRequestParameter(AuxRequestParam.RS_PRODUCT_ID).toString();
						if(null!= productIds && ""!=productIds){
							fdpResponse = executeAndHandleServiceProvisioningResponseForOptinOptOutAllCase(fdpRequest, policyRuleResponse,productIds,fdpResponse);
							return fdpResponse;
						}						
					}
				}else{
					fdpResponse = executeAndHandleServiceProvisioningResponse(fdpRequest, policyRuleResponse);	
				}				
				if (Status.SUCCESS.equals(fdpResponse.getExecutionStatus()) && lastExecutedIndex < policyRules.size()-1)  {
					//fdpRequest.setLastExecutedIndex(fdpRequest.getLastExecutedIndex()+1);
					lastExecutedIndex++;
				} else {
					errorOccurred = true;
					break;
				}
			} else if (PolicyStatus.GOTO_RULE.equals(policyRuleResponse.getPolicyStatus())){
				if(policyRuleResponse.getNextRuleIndex() != null){
					//fdpRequest.setLastExecutedIndex(policyRuleResponse.getNextRuleIndex());
					lastExecutedIndex = policyRuleResponse.getNextRuleIndex();
				}
				break;
			} else if(PolicyStatus.SKIP_EXECUTION.equals(policyRuleResponse.getPolicyStatus())){
				//fdpRequest.setLastExecutedIndex(policyRules.size());
				lastExecutedIndex = policyRules.size();
			}
			if (!policyRule.isRuleNoInputRequired()) {
				currIndex++;
			}
			if (!continueEvaluation) {
				break;
			}
		}
		if (!errorOccurred) {
			fdpResponse = executeCurrentPolicyRule(fdpRequest, fdpResponse, params);
		}
		return fdpResponse;
	}

	private FDPResponse executeAndHandleServiceProvisioningResponse(final FDPRequest fdpRequest,
			final FDPPolicyResponse policyRuleResponse) throws ExecutionFailedException {
		FDPResponse fdpResponse = null;
		if (this instanceof FDPExecutesServiceProv) {
			final FDPResponse fdpResponseSP = ((FDPExecutesServiceProv) this).executeServiceProv(fdpRequest);
			if (Status.SUCCESS.equals(fdpResponseSP.getExecutionStatus())) {
				fdpResponse = new FDPResponseImpl(Status.SUCCESS, policyRuleResponse.terminateSession(),
						fdpResponseSP != null ? fdpResponseSP.getResponseString()
								: policyRuleResponse.getResponseMessages());
			} else {
				fdpResponse = new FDPResponseImpl(Status.FAILURE, true,
						(fdpResponseSP != null) ? fdpResponseSP.getResponseString()
								: policyRuleResponse.getResponseMessages(), fdpResponseSP.getResponseError());
			}
		}
		return fdpResponse;
	}

	private void policyRuleSuccess(final PolicyRule policyRule, final String currentRuleInput) {
		if (policyRule.getAuxiliaryParam() != null) {
			ruleValues.put(policyRule.getAuxiliaryParam(), currentRuleInput);
		} else if (policyRule.getRuleValues() != null && !policyRule.getRuleValues().isEmpty()) {
			ruleValues.putAll(policyRule.getRuleValues());
		}
	}

	/**
	 * This method executes the current policy rule.
	 * 
	 * @param fdpRequest
	 *            the request object.
	 * @return the response of executing the policy rule.
	 */
	private FDPResponse executeCurrentPolicyRule(final FDPRequest fdpRequest, final Object... params)
			throws ExecutionFailedException {
		FDPResponse fdpResponse = null;
		if (/*fdpRequest.getLastExecutedIndex()*/lastExecutedIndex < policyRules.size()) {
			//fdpResponse = policyRules.get(fdpRequest.getLastExecutedIndex()).displayRule(fdpRequest);
			fdpResponse = policyRules.get(lastExecutedIndex).displayRule(fdpRequest);
			if (fdpResponse == null) {
				//if (!policyRules.get(fdpRequest.getLastExecutedIndex()).isRuleNoInputRequired()) {
				if (!policyRules.get(lastExecutedIndex).isRuleNoInputRequired()) {
					// This policy does not take any input
					fdpResponse = executePolicy(fdpRequest, null, false, params);
				} else {
					//fdpRequest.setLastExecutedIndex(fdpRequest.getLastExecutedIndex()+1);
					lastExecutedIndex++;
					fdpResponse = executeCurrentPolicyRule(fdpRequest, params);
				}
			}
		}
		return fdpResponse;
	}

	private FDPResponse executeCurrentPolicyRule(final FDPRequest fdpRequest,final FDPResponse fdpResponse, final Object... params )
			throws ExecutionFailedException {
		FDPResponse response = executeCurrentPolicyRule(fdpRequest, params);
		if(null != fdpRequest.getAuxiliaryRequestParameter(AuxRequestParam.TYPE) && FDPConstant.NORMAL_PRODUCT_TYPE.toString().equals(fdpRequest.getAuxiliaryRequestParameter(AuxRequestParam.TYPE).toString())){
		return fdpResponse;
		}else{
			return response;	
		}		
	}
	

	private FDPResponse executeAndHandleServiceProvisioningResponseForOptinOptOutAllCase(final FDPRequest fdpRequest,
			final FDPPolicyResponse policyRuleResponse , final String productIdStr, FDPResponse fdpResponse) throws ExecutionFailedException {
		final FDPServiceProvSubType fdpServiceProvSubType = RequestUtil.getFDPServiceProvSubType(fdpRequest);	
		String [] productStrArray = productIdStr.split(FDPConstant.PIPE_DELIMITER);
			for (String productId : productStrArray) {
				if(null!=productId && productId.length()>0) {									
						final Product product = (Product)RequestUtil.getProductById(fdpRequest, productId);
						ServiceProvisioningRule sp = RequestUtil.getServiceProvisioningById(fdpRequest,ServiceProvDataConverterUtil.getKeyForServiceProvMetaBag(
								Long.valueOf(product.getProductId().toString()),FDPServiceProvType.PRODUCT, RequestUtil.getFDPServiceProvSubTypeAction(fdpServiceProvSubType)));
						//removeChargingStep(sp);
						RequestUtil.removeSteps(sp);
						if (fdpRequest instanceof FDPRequestImpl) {
							((FDPRequestImpl)fdpRequest).addMetaValue(RequestMetaValuesKey.PRODUCT, product);
							((FDPRequestImpl)fdpRequest).addMetaValue(RequestMetaValuesKey.SERVICE_PROVISIONING, sp);
							
							final OptInOptOutServiceNode fdpNode = new OptInOptOutServiceNode(product.getProductName(), null,
									((FDPNode) fdpRequest.getValueFromRequest(RequestMetaValuesKey.NODE)).getFullyQualifiedPath(),
									fdpRequest.getChannel(), fdpRequest.getCircle(), null, null, null, sp.getServiceProvDTO().getId(), null,fdpServiceProvSubType,sp,product);
							((FDPRequestImpl)fdpRequest).addMetaValue(RequestMetaValuesKey.NODE, fdpNode);
							//System.out.println("node set" + fdpNode);
					}
						fdpResponse = executeAndHandleServiceProvisioningResponse(fdpRequest, policyRuleResponse);
				}
			}
			return fdpResponse;		
	}
	
	@Override
	public boolean isPolicyExecution(FDPRequest fdpRequest) {
		if (/*fdpRequest.getLastExecutedIndex()*/lastExecutedIndex < policyRules.size()) {
			//if (policyRules.get(fdpRequest.getLastExecutedIndex()) instanceof ActiveServicePolicyRuleImpl
			if (policyRules.get(lastExecutedIndex) instanceof ActiveServicePolicyRuleImpl
					//|| policyRules.get(fdpRequest.getLastExecutedIndex()) instanceof PendingRequestPolicyRuleImpl) {
					|| policyRules.get(lastExecutedIndex) instanceof PendingRequestPolicyRuleImpl || policyRules.get(lastExecutedIndex) instanceof OptInOptOutPolicyRuleImpl) {
				return false;
			}
		}
		return policyExecution;
	}

}
