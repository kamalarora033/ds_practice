package com.ericsson.fdp.business.policy.policyrule.impl;

import java.util.Map;

import com.ericsson.fdp.business.command.param.impl.CommandParamOutput;
import com.ericsson.fdp.business.enums.AuxRequestParam;
import com.ericsson.fdp.business.enums.ConfigurationKey;
import com.ericsson.fdp.business.enums.PolicyStatus;
import com.ericsson.fdp.business.enums.RequestMetaValuesKey;
import com.ericsson.fdp.business.enums.TLVOptions;
import com.ericsson.fdp.business.exception.NotificationFailedException;
import com.ericsson.fdp.business.node.impl.BonusBundleMenuRequestNode;
import com.ericsson.fdp.business.node.impl.SpecialMenuNode;
import com.ericsson.fdp.business.policy.policyrule.PolicyRule;
import com.ericsson.fdp.business.request.FDPPolicyResponse;
import com.ericsson.fdp.business.request.impl.FDPPolicyResponseImpl;
import com.ericsson.fdp.business.util.ClassUtil;
import com.ericsson.fdp.business.util.FulfillmentUtil;
import com.ericsson.fdp.business.util.LoggerUtil;
import com.ericsson.fdp.business.util.ResponseUtil;
import com.ericsson.fdp.business.util.TariffEnquiryNotificationUtil;
import com.ericsson.fdp.common.constants.FDPConstant;
import com.ericsson.fdp.core.exception.ExecutionFailedException;
import com.ericsson.fdp.core.node.FDPNode;
import com.ericsson.fdp.core.request.FDPRequest;
import com.ericsson.fdp.core.request.impl.FDPRequestImpl;
import com.ericsson.fdp.core.request.impl.FDPSMPPRequestImpl;

public class SharedBonusBundleDoubleConfPolicy extends ProductPolicyRuleImpl implements PolicyRule {
	
	/**
	 *  Class serial version UID
	 */
	private static final long serialVersionUID = -5190775639519831064L;

	@Override
	protected String getNotificationText(final FDPRequest fdpRequest) throws ExecutionFailedException {
		String notificationText = null;
		final FDPNode fdpNode = (FDPNode) fdpRequest.getValueFromRequest(RequestMetaValuesKey.NODE);
		if (null != fdpNode && fdpNode instanceof SpecialMenuNode) {
			final SpecialMenuNode specialMenuNode = (SpecialMenuNode) fdpNode;
			switch (specialMenuNode.getSpecialMenuNodeType()) {
			case SHARED_BUNDLE_ADD_CONSUMER:
			case SHARED_BUNDLE_CREATE_PACK:
				notificationText = getNotificationTextFromConfiguration(fdpRequest,
						ConfigurationKey.SHARED_BONUD_BUNDLE_ADD_DOUBLE_CONFIRMATION_TEXT);
				break;
			case SHARED_BUNDLE_UPDATE_CONSUMER_DATA:
				notificationText = getNotificationTextFromConfiguration(fdpRequest,
						ConfigurationKey.SHARED_BONUD_BUNDLE_UPGRADE_CONSUMER_DOUBLE_CONFIRMATION_TEXT);
				break;	
			case SHARED_BUNDLE_DELETE_CONSUMER:
				/*notificationText = getNotificationTextFromConfiguration(fdpRequest,
						ConfigurationKey.SHARED_BONUD_BUNDLE_DEL_DOUBLE_CONFIRMATION_TEXT);*/
				notificationText = getConsumerDeletionNotificationText(fdpRequest);
				break;
			case SHARED_BUNDLE_DELETE_SELF_CONSUMER:
				notificationText = getSelfDeletionNotificationText(fdpRequest);
				break;
			default:
				break;
			}
			notificationText = (null == notificationText) ? "Press 1 to confirm" : notificationText;
		}
		return notificationText;
	}
	
	/**
	 * This method will get the configuration.
	 * 
	 * @param fdpRequest
	 * @param configurationKey
	 * @return
	 */
	private String getNotificationTextFromConfiguration(final FDPRequest fdpRequest,
			final ConfigurationKey configurationKey) {
		return FulfillmentUtil.getNotificationTextFromConfiguration(fdpRequest, configurationKey);
	}
	
	@Override
	public FDPPolicyResponse validatePolicyRule(final Object input, final FDPRequest fdpRequest,
			final Object... otherParams) throws ExecutionFailedException {
		FDPPolicyResponse response = new FDPPolicyResponseImpl(PolicyStatus.EXECUTE_SP, null, null, true, null);
		LoggerUtil.generatePolicyBehaviourLogsForUserBehaviour(fdpRequest, AuxRequestParam.CONFIRMATION.getName());
		if ((input == null && getNotificationText(fdpRequest) != null)) {
			throw new ExecutionFailedException("Cannot validate policy value");
		} else if (input != null) {
			final Map<String, String> configurationMap = fdpRequest.getCircle().getConfigurationKeyValueMap();
			final String validResponsesForProductBuy = configurationMap
					.get(ConfigurationKey.PRODUCT_BUY_VALID_RESPONSES.getAttributeName());
			boolean isValid = false;
			if (validResponsesForProductBuy != null && !validResponsesForProductBuy.isEmpty()) {
				final String[] validResponses = validResponsesForProductBuy.split(FDPConstant.COMMA);
				for (final String validResponse : validResponses) {
					if (validResponse.equalsIgnoreCase(input.toString())) {
						isValid = true;
						updateAuxParams(fdpRequest);
						break;
					}
				}
			}
			if (!isValid) {
				response = getResponseForInvalidInput(input, fdpRequest, configurationMap);
			}
		}
		return response;
	}
	
	/**
	 * This method will update the AuX 
	 * @param fdpRequest
	 * @throws ExecutionFailedException 
	 */
	private void updateAuxParams(final FDPRequest fdpRequest) throws ExecutionFailedException {
		if (null != fdpRequest.getValueFromRequest(RequestMetaValuesKey.NODE)) {
			final FDPNode fdpNode = (FDPNode) fdpRequest.getValueFromRequest(RequestMetaValuesKey.NODE);
			if (null != fdpNode && fdpNode instanceof SpecialMenuNode && fdpRequest instanceof FDPSMPPRequestImpl) {
				final SpecialMenuNode specialMenuNode = (SpecialMenuNode) fdpNode;
				final FDPRequestImpl fdpRequestImpl = (FDPRequestImpl) fdpRequest;
				fdpRequestImpl.putAuxiliaryRequestParameter(AuxRequestParam.PROVIDER_MSISDN, fdpRequestImpl
						.getSubscriberNumber().toString());
				switch (specialMenuNode.getSpecialMenuNodeType()) {
				case SHARED_BUNDLE_ADD_CONSUMER:
					fdpRequestImpl.putAuxiliaryRequestParameter(AuxRequestParam.ACTION,
							FDPConstant.SHARED_BONUD_BUNDLE_CONSUMER_TYPE);
					break;
				case SHARED_BUNDLE_CREATE_PACK:
					fdpRequestImpl.putAuxiliaryRequestParameter(AuxRequestParam.ACTION,
							FDPConstant.SHARED_BONUS_BUNDLE_PROVIDER_TYPE.get(1));
					break;
				case SHARED_BUNDLE_UPDATE_CONSUMER_DATA:
					fdpRequestImpl.putAuxiliaryRequestParameter(AuxRequestParam.ACTION,
							FDPConstant.SHARED_BONUD_BUNDLE_UPGRADE_CONSUMER_TYPE);
					break;	
				case SHARED_BUNDLE_DELETE_CONSUMER:
					fdpRequestImpl.putAuxiliaryRequestParameter(AuxRequestParam.CONSUMER_MSISDN_LIST,
							fdpNode.getDisplayName());
					break;
				case SHARED_BUNDLE_DELETE_SELF_CONSUMER:
					final CommandParamOutput commandParamOutput = FulfillmentUtil.getCommandOutput(fdpRequestImpl, "provider", FDPConstant.SBB_GETDETAILS_COMMAND	, true);
					if(null == commandParamOutput) {
						throw new ExecutionFailedException("Unable to get provider from GetDetails");
					}
					fdpRequestImpl.putAuxiliaryRequestParameter(AuxRequestParam.PROVIDER_MSISDN, commandParamOutput.getValue().toString());
					fdpRequestImpl.putAuxiliaryRequestParameter(AuxRequestParam.CONSUMER_MSISDN,
							fdpRequest.getSubscriberNumber().toString());
					break;
				default:
					break;
				}
			}
			if (null != fdpNode && fdpNode instanceof BonusBundleMenuRequestNode
					&& fdpRequest instanceof FDPSMPPRequestImpl) {
				FDPSMPPRequestImpl fdpsmppRequestImpl = (FDPSMPPRequestImpl) fdpRequest;
				fdpsmppRequestImpl.putAuxiliaryRequestParameter(AuxRequestParam.PROVIDER_MSISDN, fdpsmppRequestImpl
						.getSubscriberNumber().toString());
				fdpsmppRequestImpl.putAuxiliaryRequestParameter(AuxRequestParam.CONSUMER_MSISDN,
						fdpNode.getDisplayName());
			}
		}
	}

	/**
	 * This method will prepare the notification text for self deletion.
	 * 
	 * @param fdpRequest
	 * @return
	 * @throws ExecutionFailedException 
	 */
	private String getSelfDeletionNotificationText(final FDPRequest fdpRequest) throws ExecutionFailedException {
		String notificationText;
		try {
			if (fdpRequest instanceof FDPRequestImpl) {
				final FDPRequestImpl fdpRequestImpl = (FDPRequestImpl) fdpRequest;
				fdpRequestImpl.putAuxiliaryRequestParameter(AuxRequestParam.SBB_DELETE_ALLOWED, fdpRequestImpl
						.getSubscriberNumber().toString());
				fdpRequestImpl.putAuxiliaryRequestParameter(AuxRequestParam.ACTION, Boolean.TRUE.toString());
				final CommandParamOutput commandParamOutput = FulfillmentUtil.getCommandOutput(fdpRequest, "provider",
						FDPConstant.SBB_GETDETAILS_COMMAND, false);
				if (null == commandParamOutput) {
					throw new ExecutionFailedException("Unable to get provider msisdn from GetDetails command");
				}
				fdpRequestImpl.putAuxiliaryRequestParameter(AuxRequestParam.PROVIDER_MSISDN, commandParamOutput
						.getValue().toString());
			}
			notificationText = TariffEnquiryNotificationUtil.createNotificationText(fdpRequest,
					FDPConstant.SHARED_BONUS_BUNDLE_SELF_DELETE_NOTI_TEXT - (fdpRequest.getCircle().getCircleId()),
					LoggerUtil.getSummaryLoggerFromRequest(fdpRequest));
		} catch (NotificationFailedException e) {
			e.printStackTrace();
			throw new ExecutionFailedException("Unable to prepare notification text, Actual Error:", e);
		}
		return notificationText;
	}
	
	/**
	 * This method is used to get response for invalid input.
	 * 
	 * @param input
	 *            the input.
	 * @param fdpRequest
	 *            the request.
	 * @param configurationMap
	 *            the configuration map.
	 * @return the response.
	 * @throws ExecutionFailedException
	 *             Exception in execution.
	 */
	protected FDPPolicyResponse getResponseForInvalidInput(final Object input, final FDPRequest fdpRequest,
			final Map<String, String> configurationMap) throws ExecutionFailedException {
		FDPPolicyResponse response = new FDPPolicyResponseImpl(PolicyStatus.SUCCESS, null, null, true, null);
		String responseString = configurationMap.get(ConfigurationKey.INVALID_INPUT_STRING.getAttributeName());
		if (responseString == null || responseString.isEmpty()) {
			responseString = "Input invalid";
		}
		final String invalidResponsesForProductBuy = configurationMap
				.get(ConfigurationKey.PRODUCT_BUY_INVALID_RESPONSES.getAttributeName());
		boolean isInValid = false;
		if (invalidResponsesForProductBuy != null && !invalidResponsesForProductBuy.isEmpty()) {
			final String[] invalidResponses = invalidResponsesForProductBuy.split(FDPConstant.COMMA);
			for (final String invalidResponse : invalidResponses) {
				if (invalidResponse.equalsIgnoreCase(input.toString())) {
					isInValid = true;
					String rejectedString = null;
					final FDPNode fdpNode = (FDPNode) fdpRequest.getValueFromRequest(RequestMetaValuesKey.NODE);
					if (null != fdpNode && fdpNode instanceof SpecialMenuNode) {
						final SpecialMenuNode specialMenuNode = (SpecialMenuNode) fdpNode;
						switch (specialMenuNode.getSpecialMenuNodeType()) {
						case SHARED_BUNDLE_DELETE_SELF_CONSUMER:
							rejectedString = configurationMap
									.get(ConfigurationKey.SHARED_BONUD_BUNDLE_DEL_SELF_DOUBLE_CONFIRM_TEXT
											.getAttributeName());
							break;
						default:
							rejectedString = configurationMap.get(ConfigurationKey.REJECTED_INPUT_STRING
									.getAttributeName());
							break;
						}
					}
					final String resultString = (rejectedString == null) ? responseString : rejectedString;
					response = new FDPPolicyResponseImpl(PolicyStatus.FAILURE, resultString, null, true,
							ResponseUtil.createResponseMessageInList(fdpRequest.getChannel(), resultString,
									TLVOptions.SESSION_TERMINATE));
					break;
				}
			}
		}
		if (!isInValid) {
			response = getResponse(responseString, fdpRequest, ClassUtil.getLongValue(configurationMap
					.get(ConfigurationKey.PRODUCT_RETRY_NUMBER.getAttributeName())));
		}
		return response;
	}
	
	/**
	 * This method will prepare the notification text for self deletion.
	 * 
	 * @param fdpRequest
	 * @return
	 * @throws ExecutionFailedException 
	 */
	private String getConsumerDeletionNotificationText(final FDPRequest fdpRequest) throws ExecutionFailedException {
		String notificationText;
		try {
			if (fdpRequest instanceof FDPRequestImpl) {
				final FDPRequestImpl fdpRequestImpl = (FDPRequestImpl) fdpRequest;
				final FDPNode fdpNode = (FDPNode) fdpRequest.getValueFromRequest(RequestMetaValuesKey.NODE);
				if (null != fdpNode && fdpNode instanceof BonusBundleMenuRequestNode
						&& fdpRequest instanceof FDPSMPPRequestImpl) {
					fdpRequestImpl.putAuxiliaryRequestParameter(AuxRequestParam.PROVIDER_MSISDN, fdpRequestImpl
							.getSubscriberNumber().toString());
					fdpRequestImpl.putAuxiliaryRequestParameter(AuxRequestParam.CONSUMER_MSISDN,
							fdpNode.getDisplayName());
					fdpRequestImpl.putAuxiliaryRequestParameter(AuxRequestParam.CONSUMER_MSISDN_LIST,
							fdpNode.getDisplayName());
				}
			}
			notificationText = TariffEnquiryNotificationUtil.createNotificationText(fdpRequest,
					FDPConstant.SHARED_BONUD_BUNDLE_DELETE_NOTI_TEXT - (fdpRequest.getCircle().getCircleId()),
					LoggerUtil.getSummaryLoggerFromRequest(fdpRequest));
		} catch (NotificationFailedException e) {
			e.printStackTrace();
			throw new ExecutionFailedException("Unable to prepare notification text, Actual Error:", e);
		}
		return notificationText;
	}
}
