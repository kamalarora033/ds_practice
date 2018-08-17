package com.ericsson.fdp.business.policy.policyrule.impl;

import java.util.Map;

import com.ericsson.fdp.business.enums.AuxRequestParam;
import com.ericsson.fdp.business.enums.Command;
import com.ericsson.fdp.business.enums.ConfigurationKey;
import com.ericsson.fdp.business.enums.PolicyStatus;
import com.ericsson.fdp.business.enums.RequestMetaValuesKey;
import com.ericsson.fdp.business.enums.TLVOptions;
import com.ericsson.fdp.business.node.impl.SpecialMenuNode;
import com.ericsson.fdp.business.policy.policyrule.PolicyRule;
import com.ericsson.fdp.business.request.FDPPolicyResponse;
import com.ericsson.fdp.business.request.impl.FDPPolicyResponseImpl;
import com.ericsson.fdp.business.request.impl.FDPResponseImpl;
import com.ericsson.fdp.business.sharedaccount.constans.SharedAccountConstants;
import com.ericsson.fdp.business.util.FulfillmentUtil;
import com.ericsson.fdp.business.util.LoggerUtil;
import com.ericsson.fdp.business.util.MVELUtil;
import com.ericsson.fdp.business.util.RequestUtil;
import com.ericsson.fdp.business.util.ResponseUtil;
import com.ericsson.fdp.common.constants.FDPConstant;
import com.ericsson.fdp.common.enums.FDPCSAttributeParam;
import com.ericsson.fdp.common.enums.FDPCSAttributeValue;
import com.ericsson.fdp.common.enums.Status;
import com.ericsson.fdp.core.exception.ExecutionFailedException;
import com.ericsson.fdp.core.node.FDPNode;
import com.ericsson.fdp.core.request.FDPRequest;
import com.ericsson.fdp.core.request.FDPResponse;
import com.ericsson.fdp.core.request.impl.FDPRequestImpl;

/**
 * This class is the policy for the Shared Bonus bundle amount policy.
 * 
 * @author sauravgupta
 * 
 */
public class SharedBonusBundleAmountPolicyRuleImpl extends AbstractPolicyRule implements PolicyRule {

	/**
	 * The class serial version UID.
	 */
	private static final long serialVersionUID = -3581192591496113247L;

	@Override
	public FDPResponse displayRule(FDPRequest fdpRequest)throws ExecutionFailedException {
		FDPResponse fdpResponse = null;
		updateAux(fdpRequest);

		String notificationText = getNotificationText(fdpRequest);
		String headerText = getHeaderTextAmount(fdpRequest);
		
		if (null != notificationText && null != headerText && Integer.parseInt(headerText)>0) {
			String configText = FulfillmentUtil.getNotificationTextFromConfiguration(fdpRequest,ConfigurationKey.SHARED_BONUS_BUNDLE_AMOUNT_HEADER_TEXT);
			headerText = (null == configText) ? headerText+ FDPConstant.SHARED_BONUS_BUNDLE_AMOUNT_HEADER_TEXT: headerText + configText;
			fdpResponse = new FDPResponseImpl(Status.SUCCESS, false,ResponseUtil.createResponseMessageInList(fdpRequest.getChannel(), headerText+ notificationText,
							TLVOptions.SESSION_CONTINUE));

		} else {
			fdpResponse = new FDPResponseImpl(Status.SUCCESS, false,ResponseUtil.createResponseMessageInList(fdpRequest.getChannel(), notificationText,TLVOptions.SESSION_TERMINATE));
		}
		return fdpResponse;
	}

	@Override
	public FDPPolicyResponse validatePolicyRule(Object input,FDPRequest fdpRequest, Object... otherParams)throws ExecutionFailedException {
		String responseText = null;
		FDPPolicyResponse fdpPolicyResponse = null;
		LoggerUtil.generatePolicyBehaviourLogsForUserBehaviour(fdpRequest,AuxRequestParam.CONFIRMATION.getName());
		if (null == input) {
			fdpPolicyResponse = generateResponse(fdpRequest,"Invalid input Received", PolicyStatus.FAILURE,TLVOptions.SESSION_TERMINATE);
		} else {

			if (!isInteger(input.toString())) {
				responseText = SharedAccountConstants.NOT_INTEGER;
			} else if (Integer.parseInt(input.toString()) > Integer.parseInt(fdpRequest.getAuxiliaryRequestParameter(AuxRequestParam.DATA_THRESHOLD).toString())) {
				responseText = SharedAccountConstants.THRESHOLD_ERROR;
			}

			if (null == responseText && fdpRequest instanceof FDPRequestImpl) {
				final FDPRequestImpl fdpRequestImpl = (FDPRequestImpl) fdpRequest;
				fdpRequestImpl.putAuxiliaryRequestParameter(AuxRequestParam.DATA_AMOUNT, input.toString());
			}
			PolicyStatus policyStatus = (null == responseText) ? PolicyStatus.SUCCESS: PolicyStatus.FAILURE;
			TLVOptions tlvOptions = (null == responseText) ? TLVOptions.SESSION_CONTINUE: TLVOptions.SESSION_TERMINATE;
			responseText = (null == responseText) ? FDPConstant.EMPTY_STRING: responseText;
			fdpPolicyResponse = generateResponse(fdpRequest, responseText,policyStatus, tlvOptions);
		}
		return fdpPolicyResponse;
	}

	/**
	 * This method is used to get notification text from request.
	 * 
	 * @param fdpRequest
	 *            the request from which notification text is to be created.
	 * @return the notification text to be used.
	 * @throws ExecutionFailedException
	 *             Exception in execution.
	 */
	private String getNotificationText(final FDPRequest fdpRequest)throws ExecutionFailedException {
		String notificationPromptText = null;
		final FDPNode fdpNode = RequestUtil.getMetaValueFromRequest(fdpRequest,RequestMetaValuesKey.NODE, FDPNode.class);
		if (null != fdpNode && fdpNode instanceof SpecialMenuNode) {
			final SpecialMenuNode specialMenuNode = (SpecialMenuNode) fdpNode;
			switch (specialMenuNode.getSpecialMenuNodeType()) {
			case SHARED_BUNDLE_ADD_CONSUMER:
			case SHARED_BUNDLE_CREATE_PACK:
			case SHARED_BUNDLE_UPDATE_CONSUMER_DATA:
				notificationPromptText = FulfillmentUtil.getNotificationTextFromConfiguration(fdpRequest,	ConfigurationKey.SHARED_BONUS_BUNDLE_AMOUNT_INPUT_TEXT);
				notificationPromptText = (null == notificationPromptText) ? FDPConstant.SHARED_BONUS_BUNDLE_AMOUNT_CONFIRMATION_TEXT
						: notificationPromptText;
				break;
			default:
				break;
			}
		}
		return notificationPromptText;
	}

	/**
	 * This method will prepare the policy response.
	 * 
	 * @param fdpRequest
	 * @param responseString
	 * @return
	 */
	private FDPPolicyResponse generateResponse(final FDPRequest fdpRequest,final String responseString, final PolicyStatus policyStatus,final TLVOptions tlvOptions) {
		return new FDPPolicyResponseImpl(policyStatus, responseString, null,false, ResponseUtil.createResponseMessageInList(fdpRequest.getChannel(), responseString, tlvOptions));
	}

	/**
	 * This method will update the AUX.
	 * 
	 * @param fdpRequest
	 */
	private void updateAux(final FDPRequest fdpRequest) {
		if (fdpRequest instanceof FDPRequestImpl) {
			final FDPRequestImpl fdpRequestImpl = (FDPRequestImpl) fdpRequest;
			fdpRequestImpl.putAuxiliaryRequestParameter(AuxRequestParam.PROVIDER_MSISDN, fdpRequest.getSubscriberNumber().toString());
		}
	}

	/**
	 * This method will get the header text Amount.
	 * 
	 * @param fdpRequest
	 * @return
	 */
	private String getHeaderTextAmount(final FDPRequest fdpRequest)throws ExecutionFailedException {
		final FDPNode fdpNode = RequestUtil.getMetaValueFromRequest(fdpRequest,RequestMetaValuesKey.NODE, FDPNode.class);
		String headerText = null;
		if (null != fdpNode && fdpNode instanceof SpecialMenuNode) {
			final SpecialMenuNode specialMenuNode = (SpecialMenuNode) fdpNode;
			switch (specialMenuNode.getSpecialMenuNodeType()) {
			case SHARED_BUNDLE_ADD_CONSUMER:
			case SHARED_BUNDLE_CREATE_PACK:
			case SHARED_BUNDLE_UPDATE_CONSUMER_DATA:
				if (fdpRequest.getAuxiliaryRequestParameter(AuxRequestParam.SHARED_BONUS_BUNDLE_USER_TYPE_PROVIDER)!=null &&fdpRequest.getAuxiliaryRequestParameter(AuxRequestParam.SHARED_BONUS_BUNDLE_USER_TYPE_PROVIDER).equals(SharedAccountConstants.PROVIDER_AUX_VALUE)) {
					String ucValue = FulfillmentUtil.getNotificationTextFromConfiguration(fdpRequest,ConfigurationKey.SHARED_BONUS_BUNDLE_PROVIDER_UC_ID) == null ? SharedAccountConstants.PROVIDER_UC_ID
							: FulfillmentUtil.getNotificationTextFromConfiguration(fdpRequest,ConfigurationKey.SHARED_BONUS_BUNDLE_PROVIDER_UC_ID);
					String utValue = FulfillmentUtil.getNotificationTextFromConfiguration(fdpRequest,ConfigurationKey.SHARED_BONUS_BUNDLE_PROVIDER_UT_ID) == null ? SharedAccountConstants.PROVIDER_UT_ID
							: FulfillmentUtil.getNotificationTextFromConfiguration(fdpRequest,ConfigurationKey.SHARED_BONUS_BUNDLE_PROVIDER_UT_ID);
					Map<String, Object> uMap = MVELUtil.evaluateUCUTDetailsForUser(fdpRequest,fdpRequest.getExecutedCommand(Command.GET_USAGE_THRESHOLDS_AND_COUNTERS_PROVIDER.getCommandName()));
					
					if(uMap!=null && uMap.containsKey(FDPCSAttributeValue.UC.name()+ FDPConstant.UNDERSCORE+ ucValue+ FDPConstant.UNDERSCORE+ FDPCSAttributeParam.VALUE.name().toString())
					&& uMap.containsKey(FDPCSAttributeValue.UT.name()+ FDPConstant.UNDERSCORE + utValue+ FDPConstant.UNDERSCORE+ FDPCSAttributeParam.VALUE.name())){
						
					Integer usageCounter = Integer.parseInt((String) uMap.get(FDPCSAttributeValue.UC.name()+ FDPConstant.UNDERSCORE+ ucValue+ FDPConstant.UNDERSCORE+ FDPCSAttributeParam.VALUE.name()
											.toString()));
					Integer usageThreshold = Integer.parseInt((String) uMap.get(FDPCSAttributeValue.UT.name()+ FDPConstant.UNDERSCORE + utValue+ FDPConstant.UNDERSCORE+ FDPCSAttributeParam.VALUE.name()));
					headerText = String.valueOf(usageThreshold - usageCounter);
					if (null != headerText&& fdpRequest instanceof FDPRequestImpl) {
						final FDPRequestImpl fdpRequestImpl = (FDPRequestImpl) fdpRequest;
						fdpRequestImpl.putAuxiliaryRequestParameter(AuxRequestParam.PROVIDER_USAGE_COUNTER_VALUE,usageCounter);
						fdpRequestImpl.putAuxiliaryRequestParameter(AuxRequestParam.PROVIDER_USAGE_THRESHOLD_VALUE,usageThreshold);
						fdpRequestImpl.putAuxiliaryRequestParameter(AuxRequestParam.DATA_THRESHOLD, headerText);
					}
				  }
				} else {
					String daValue = FulfillmentUtil.getNotificationTextFromConfiguration(fdpRequest,ConfigurationKey.SHARED_BONUS_BUNDLE_DA_ID) == null ? SharedAccountConstants.DA_ID
							: FulfillmentUtil.getNotificationTextFromConfiguration(fdpRequest,ConfigurationKey.SHARED_BONUS_BUNDLE_DA_ID);
					headerText = SharedBonusBundleViewPolicyImpl.getValueForDAId(fdpRequest, daValue);
					if (null != headerText&& fdpRequest instanceof FDPRequestImpl) {
						final FDPRequestImpl fdpRequestImpl = (FDPRequestImpl) fdpRequest;
						fdpRequestImpl.putAuxiliaryRequestParameter(AuxRequestParam.PROVIDER_USAGE_COUNTER_VALUE,0);
						fdpRequestImpl.putAuxiliaryRequestParameter(AuxRequestParam.PROVIDER_USAGE_THRESHOLD_VALUE,headerText);
						fdpRequestImpl.putAuxiliaryRequestParameter(AuxRequestParam.DATA_THRESHOLD, headerText);

					}
				}
				
				break;
			default:
				break;
			}
		}

		return headerText;
	}

	private boolean isInteger(String str) {
		try {
			Integer.parseInt(str);
			return true;
		} catch (NumberFormatException ex) {
			return false;
		}
	}

}
