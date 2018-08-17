package com.ericsson.fdp.business.policy.policyrule.impl;

import java.util.List;

import com.ericsson.fdp.business.enums.AuxRequestParam;
import com.ericsson.fdp.business.enums.ConfigurationKey;
import com.ericsson.fdp.business.enums.PolicyStatus;
import com.ericsson.fdp.business.enums.RequestMetaValuesKey;
import com.ericsson.fdp.business.enums.TLVOptions;
import com.ericsson.fdp.business.enums.ivr.FulfillmentResponseCodes;
import com.ericsson.fdp.business.node.impl.SpecialMenuNode;
import com.ericsson.fdp.business.policy.policyrule.PolicyRule;
import com.ericsson.fdp.business.request.FDPPolicyResponse;
import com.ericsson.fdp.business.request.impl.FDPPolicyResponseImpl;
import com.ericsson.fdp.business.request.impl.FDPResponseImpl;
import com.ericsson.fdp.business.util.FulfillmentUtil;
import com.ericsson.fdp.business.util.LoggerUtil;
import com.ericsson.fdp.business.util.RequestUtil;
import com.ericsson.fdp.business.util.ResponseUtil;
import com.ericsson.fdp.common.constants.FDPConstant;
import com.ericsson.fdp.common.enums.Status;
import com.ericsson.fdp.core.exception.ExecutionFailedException;
import com.ericsson.fdp.core.node.FDPNode;
import com.ericsson.fdp.core.request.FDPRequest;
import com.ericsson.fdp.core.request.FDPResponse;
import com.ericsson.fdp.core.request.impl.FDPRequestImpl;

/**
 * This class is the policy for the Shared Bonus bundle user inputs.
 * 
 * @author eashtod
 * 
 */
public class SharedBonusBundlePolicyRuleImpl extends AbstractPolicyRule implements PolicyRule {

	/**
	 * The class serial version UID.
	 */
	private static final long serialVersionUID = -3581192591496113247L;

	@Override
	public FDPResponse displayRule(FDPRequest fdpRequest) throws ExecutionFailedException {
		FDPResponse fdpResponse = null;
		updateAux(fdpRequest);
		String notificationText = null;
		final FDPNode fdpNode = RequestUtil.getMetaValueFromRequest(fdpRequest, RequestMetaValuesKey.NODE,
				FDPNode.class);
		if (null != fdpNode && fdpNode instanceof SpecialMenuNode) {
			final SpecialMenuNode specialMenuNode = (SpecialMenuNode) fdpNode;
			switch (specialMenuNode.getSpecialMenuNodeType()) {
			case SHARED_BUNDLE_ADD_CONSUMER:
			case SHARED_BUNDLE_CREATE_PACK:
				notificationText = FulfillmentUtil.getSharedBonusBundleConsumerLimitReachText(fdpRequest);
				if(null != notificationText) {
					fdpResponse = new FDPResponseImpl(Status.SUCCESS, false, ResponseUtil.createResponseMessageInList(
							fdpRequest.getChannel(), notificationText, TLVOptions.SESSION_TERMINATE));
				} else {
					notificationText = getNotificationText(fdpRequest);
					if (notificationText != null) {
						fdpResponse = new FDPResponseImpl(Status.SUCCESS, false, ResponseUtil.createResponseMessageInList(
								fdpRequest.getChannel(), notificationText, TLVOptions.SESSION_CONTINUE));
					}
				}
			break;	
			case SHARED_BUNDLE_UPDATE_CONSUMER_DATA:	
				FulfillmentUtil.isSharedBonudBundleProviderOrNew(fdpRequest, FDPConstant.SHARED_BONUS_BUNDLE_PROVIDER_TYPE);
				fdpResponse = CommonSharedBonusBundlePolicyUtil.displayRule(fdpRequest);
				break;
			default:
				break;
			}
		}
	
		return fdpResponse;
	}

	@Override
	public FDPPolicyResponse validatePolicyRule(Object input, FDPRequest fdpRequest, Object... otherParams)
			throws ExecutionFailedException {
		String responseText = null;
		FDPPolicyResponse fdpPolicyResponse = null;
		LoggerUtil.generatePolicyBehaviourLogsForUserBehaviour(fdpRequest, AuxRequestParam.CONFIRMATION.getName());
		if(null == input) {
			fdpPolicyResponse = generateResponse(fdpRequest, "Invalid input Received",PolicyStatus.FAILURE, TLVOptions.SESSION_TERMINATE);
		} else {
			final List<String> consumerList = FulfillmentUtil.getConsumerList(fdpRequest, false);
			final FDPNode fdpNode = RequestUtil.getMetaValueFromRequest(fdpRequest, RequestMetaValuesKey.NODE,
					FDPNode.class);
			if (null != fdpNode && fdpNode instanceof SpecialMenuNode) {
				final SpecialMenuNode specialMenuNode = (SpecialMenuNode) fdpNode;
				switch (specialMenuNode.getSpecialMenuNodeType()) {
				case SHARED_BUNDLE_ADD_CONSUMER:
				case SHARED_BUNDLE_CREATE_PACK:
					if(fdpRequest.getSubscriberNumber().toString().equals(input.toString())) {
						responseText = "you can not add yourself.";
					}
					if(null != consumerList && consumerList.contains(input.toString())) {
						responseText = FulfillmentResponseCodes.CONSUMER_ALREADY_PRESENT.getReason();
					}
					responseText = (null == responseText) ? FulfillmentUtil.checkIfInputMsisdnIsValid(fdpRequest,
							String.valueOf(input)) : responseText;
					responseText = (null == responseText && !FulfillmentUtil.isConsumerTypeNew(fdpRequest,
							String.valueOf(input))) ? FulfillmentResponseCodes.CONSUMER_ALREADY_PRESENT.getReason() : responseText; 
					break;		
				case SHARED_BUNDLE_UPDATE_CONSUMER_DATA:
					if(null != consumerList && !consumerList.contains(input.toString())) {
						responseText = FulfillmentResponseCodes.CONSNUMER_MSISDN_NOT_VALID.getReason();
					}
					responseText = (null == responseText) ? FulfillmentUtil.checkIfInputMsisdnIsValid(fdpRequest,
							String.valueOf(input)) : responseText;
					break;
				default:
					break;
				}
			}
		
			if(null == responseText && fdpRequest instanceof FDPRequestImpl) {
				final FDPRequestImpl fdpRequestImpl = (FDPRequestImpl) fdpRequest;
				fdpRequestImpl.putAuxiliaryRequestParameter(AuxRequestParam.CONSUMER_MSISDN, input.toString());
			}
 			PolicyStatus policyStatus = (null == responseText) ? PolicyStatus.SUCCESS : PolicyStatus.FAILURE;
			TLVOptions tlvOptions = (null == responseText) ? TLVOptions.SESSION_CONTINUE : TLVOptions.SESSION_TERMINATE;
			responseText = (null == responseText) ? FDPConstant.EMPTY_STRING : responseText;
			fdpPolicyResponse = generateResponse(fdpRequest,
					responseText, policyStatus,tlvOptions) ;
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
	private String getNotificationText(final FDPRequest fdpRequest) throws ExecutionFailedException {
		String notificationPromptText = null;
		final FDPNode fdpNode = RequestUtil.getMetaValueFromRequest(fdpRequest, RequestMetaValuesKey.NODE,
				FDPNode.class);
		if (null != fdpNode && fdpNode instanceof SpecialMenuNode) {
			final SpecialMenuNode specialMenuNode = (SpecialMenuNode) fdpNode;
			switch (specialMenuNode.getSpecialMenuNodeType()) {
			case SHARED_BUNDLE_ADD_CONSUMER:
			case SHARED_BUNDLE_CREATE_PACK:
				notificationPromptText = FulfillmentUtil.getNotificationTextFromConfiguration(fdpRequest,
						ConfigurationKey.SHARED_BONUS_BUNDLE_ADD_CONSUMER_CONFIRMATION_TEXT);
				notificationPromptText = (null == notificationPromptText) ? "Enter the consumer msisdn to add" : notificationPromptText;
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
	private FDPPolicyResponse generateResponse(final FDPRequest fdpRequest, final String responseString, final PolicyStatus policyStatus, final TLVOptions tlvOptions) {
		return new FDPPolicyResponseImpl(policyStatus, responseString, null, false,
				ResponseUtil.createResponseMessageInList(fdpRequest.getChannel(), responseString,
						tlvOptions));
	}
	
	/**
	 * This method will update the AUX.
	 * 
	 * @param fdpRequest
	 */
	private void updateAux(final FDPRequest fdpRequest) {
		if(fdpRequest instanceof FDPRequestImpl) {
			final FDPRequestImpl fdpRequestImpl = (FDPRequestImpl) fdpRequest;
			fdpRequestImpl.putAuxiliaryRequestParameter(AuxRequestParam.PROVIDER_MSISDN, fdpRequest.getSubscriberNumber().toString());
		}
	}
 }
