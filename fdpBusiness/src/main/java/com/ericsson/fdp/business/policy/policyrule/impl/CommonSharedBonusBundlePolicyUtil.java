package com.ericsson.fdp.business.policy.policyrule.impl;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import com.ericsson.fdp.business.command.param.impl.CommandParamOutput;
import com.ericsson.fdp.business.display.impl.DisplayObjectImpl;
import com.ericsson.fdp.business.display.impl.ResponseMessageImpl;
import com.ericsson.fdp.business.enums.AuxRequestParam;
import com.ericsson.fdp.business.enums.ConfigurationKey;
import com.ericsson.fdp.business.enums.DisplayArea;
import com.ericsson.fdp.business.enums.PolicyStatus;
import com.ericsson.fdp.business.enums.RequestMetaValuesKey;
import com.ericsson.fdp.business.enums.TLVOptions;
import com.ericsson.fdp.business.enums.ivr.FulfillmentResponseCodes;
import com.ericsson.fdp.business.node.impl.BonusBundleMenuRequestNode;
import com.ericsson.fdp.business.node.impl.SpecialMenuNode;
import com.ericsson.fdp.business.request.FDPPolicyResponse;
import com.ericsson.fdp.business.request.impl.FDPPolicyResponseImpl;
import com.ericsson.fdp.business.request.impl.FDPResponseImpl;
import com.ericsson.fdp.business.request.impl.FulfillmentRequestImpl;
import com.ericsson.fdp.business.sharedaccount.constans.SharedAccountConstants;
import com.ericsson.fdp.business.util.DynamicMenuUtil;
import com.ericsson.fdp.business.util.FulfillmentUtil;
import com.ericsson.fdp.business.util.RequestUtil;
import com.ericsson.fdp.business.util.ResponseUtil;
import com.ericsson.fdp.common.constants.FDPConstant;
import com.ericsson.fdp.common.enums.Status;
import com.ericsson.fdp.common.util.StringUtil;
import com.ericsson.fdp.core.command.FDPCommand;
import com.ericsson.fdp.core.config.utils.PropertyUtils;
import com.ericsson.fdp.core.display.ResponseMessage;
import com.ericsson.fdp.core.exception.ExecutionFailedException;
import com.ericsson.fdp.core.node.FDPNode;
import com.ericsson.fdp.core.request.FDPRequest;
import com.ericsson.fdp.core.request.FDPResponse;
import com.ericsson.fdp.core.request.impl.FDPRequestImpl;
import com.ericsson.fdp.dao.enums.FDPServiceProvSubType;

public class CommonSharedBonusBundlePolicyUtil {
	
	/** BONUS_BUNDLE_DELETE_CONSUMER_POLICY */
	private static final String BONUS_BUNDLE_DELETE_CONSUMER_POLICY = "SHARED_BONUS_BUNDLE_DC_POLICY";
	
	/**
	 * This method would display the text for View/Delete consumer operation.
	 */
	public static FDPResponse displayRule(final FDPRequest fdpRequest) throws ExecutionFailedException {
		FDPResponse fdpResponse = null;
		FDPCommand fdpCommand = fdpRequest.getExecutedCommand(FDPConstant.SBB_GETDETAILS_COMMAND);
		if (null == fdpCommand) {
			throw new ExecutionFailedException("Unable to execute the GetDetails");
		}
		updateAux(fdpRequest);
		final FDPNode fdpNode = (FDPNode) fdpRequest.getValueFromRequest(RequestMetaValuesKey.NODE);
		if (null != fdpNode && fdpNode instanceof SpecialMenuNode) {
			final SpecialMenuNode specialMenuNode = (SpecialMenuNode) fdpNode;
			switch (specialMenuNode.getSpecialMenuNodeType()) {
			case SHARED_BUNDLE_VIEW_CONSUMER:
				fdpResponse = createNodesFromOutput(fdpCommand, fdpRequest,
						FDPServiceProvSubType.SHARED_BUNDLE_VIEW_CONSUMER);
				break;
			case SHARED_BUNDLE_DELETE_CONSUMER:
				fdpResponse = createNodesFromOutput(fdpCommand, fdpRequest,
						FDPServiceProvSubType.SHARED_BUNDLE_DELETE_CONSUMER);
				break;
			case SHARED_BUNDLE_UPDATE_CONSUMER_DATA:	
				fdpResponse = createNodesFromOutput(fdpCommand, fdpRequest,
						FDPServiceProvSubType.SHARED_BUNDLE_UPDATE_CONSUMER_DATA);
				break;
			default:
				fdpResponse = null;
				break;
			}
		}
		return fdpResponse;
	}

	/**
	 * This method will create the nodes.
	 * 
	 * @param fdpCommand
	 * @param fdpRequest
	 * @return
	 * @throws ExecutionFailedException
	 */
	private static FDPResponse createNodesFromOutput(final FDPCommand fdpCommand, final FDPRequest fdpRequest,
			final FDPServiceProvSubType fdpServiceProvSubType)
			throws ExecutionFailedException {
		
		FDPResponse fdpResponse = null;
		final FDPNode fdpNode = (FDPNode)fdpRequest.getValueFromRequest(RequestMetaValuesKey.NODE);
		
		final List<String> consumerMsisdnList = updateAuxInRequestAndGetConsumers(fdpRequest,
				AuxRequestParam.CONSUMER_MSISDN_LIST, "consumerList");

		if(FDPServiceProvSubType.SHARED_BUNDLE_DELETE_CONSUMER.equals(fdpServiceProvSubType) 
				&& fdpRequest instanceof FulfillmentRequestImpl) {
			final FulfillmentRequestImpl fulfillmentRequestImpl = (FulfillmentRequestImpl) fdpRequest;
			fulfillmentRequestImpl.putAuxiliaryRequestParameter(AuxRequestParam.SBB_DELETE_ALLOWED, Boolean.TRUE);
		}
		
		if(null != fdpNode && fdpNode instanceof SpecialMenuNode && null != consumerMsisdnList 
				&& !consumerMsisdnList.isEmpty()) {
			
			fdpResponse = getViewOrDeleteConsumerResponse(fdpRequest, fdpServiceProvSubType, fdpNode,
					consumerMsisdnList);
		} else {
			fdpResponse = new FDPResponseImpl(Status.SUCCESS, false, ResponseUtil.createResponseMessageInList(
					fdpRequest.getChannel(), getNoConsumerFoundResposeText(fdpRequest), TLVOptions.SESSION_TERMINATE));
		}
		return fdpResponse;
	}

	/**
	 * This method gets the response for View/Delete consumer operation.
	 */
	private static FDPResponse getViewOrDeleteConsumerResponse(final FDPRequest fdpRequest,
			final FDPServiceProvSubType fdpServiceProvSubType, final FDPNode fdpNode, 
			final List<String> consumerMsisdnList) throws ExecutionFailedException {

		List<FDPNode> fdpNodeList = new ArrayList<FDPNode>();
		FDPResponse fdpResponse = null;
		SpecialMenuNode specialMenuNode = (SpecialMenuNode) fdpNode;
		final Long spId = Long.valueOf(specialMenuNode.getEntityIdForCache(
				RequestMetaValuesKey.SERVICE_PROVISIONING));
		
		for (String consumer : consumerMsisdnList) {
			
			final BonusBundleMenuRequestNode bonusBundleMenuRequestNode = new BonusBundleMenuRequestNode(
					consumer, null, ((FDPNode) fdpRequest.getValueFromRequest(
							RequestMetaValuesKey.NODE)).getFullyQualifiedPath(), fdpRequest.getChannel(),
							fdpRequest.getCircle(), null, null, null, spId, null, fdpServiceProvSubType, null);
			if (FDPServiceProvSubType.SHARED_BUNDLE_DELETE_CONSUMER.equals(fdpServiceProvSubType)) {
				bonusBundleMenuRequestNode.setPolicyName(BONUS_BUNDLE_DELETE_CONSUMER_POLICY);
			}
			fdpNodeList.add(bonusBundleMenuRequestNode);
		}
		fdpResponse = RequestUtil.createResponseFromDisplayObject(
				new DisplayObjectImpl((FDPNode) fdpRequest.getValueFromRequest(RequestMetaValuesKey.NODE),
						fdpNodeList, getResponseMessage(fdpRequest, fdpServiceProvSubType)), fdpRequest,
						(FDPNode) fdpRequest.getValueFromRequest(RequestMetaValuesKey.NODE));
		return fdpResponse;
	}

	/**
	 * This method will update the AUX param for View Consumer.
	 * 
	 * @param fdpRequest
	 * @throws ExecutionFailedException
	 */
	private static List<String> updateAuxInRequestAndGetConsumers(final FDPRequest fdpRequest,
			final AuxRequestParam auxRequestParam, final String parameterPath) 
					throws ExecutionFailedException {

		List<String> consumerList = null;
		final CommandParamOutput commandParamOutput = FulfillmentUtil.getCommandOutput(fdpRequest, parameterPath,
				FDPConstant.SBB_GETDETAILS_COMMAND, true);
		final FDPRequestImpl fdpRequestImpl = (FDPRequestImpl) fdpRequest;
		if (null != commandParamOutput && !StringUtil.isNullOrEmpty(commandParamOutput.getValue().toString())) {
			String commandSeperatedConsumerList = commandParamOutput.getValue().toString();
			fdpRequestImpl.putAuxiliaryRequestParameter(auxRequestParam, commandParamOutput.getValue().toString());
			consumerList = Arrays.asList(commandSeperatedConsumerList.split(FDPConstant.COMMA));
		} else {
			fdpRequestImpl.putAuxiliaryRequestParameter(auxRequestParam, FDPConstant.EMPTY_STRING);
			consumerList = new ArrayList<String>(FDPConstant.ZERO);
		}
		return consumerList;
	}

	/**
	 * This method validates the policy define for View/Delete consumer action.
	 */
	public static FDPPolicyResponse validatePolicyRule(final Object input, final FDPRequest fdpRequest,
			FDPServiceProvSubType fdpServiceProvSubType) throws ExecutionFailedException {
		FDPPolicyResponse response = null;
		if(fdpRequest instanceof FDPRequestImpl) {
			final FDPRequestImpl fdpRequestImpl = (FDPRequestImpl) fdpRequest;
			
			if (input != null && input.toString().equals(PropertyUtils.getProperty(SharedAccountConstants.VIEW_EXIT_CODE)) 
					&& FDPServiceProvSubType.SHARED_BUNDLE_VIEW_CONSUMER.equals(fdpServiceProvSubType)) {
				response = new FDPPolicyResponseImpl(PolicyStatus.FAILURE, null, null, true, 
						ResponseUtil.createResponseMessageInList(fdpRequest.getChannel(), 
								DynamicMenuUtil.getExitMenuResponse(fdpRequest.getCircle()), 
								TLVOptions.SESSION_TERMINATE));
			} else {
				List<String> consumerList = null;
				Object consumerMSISDN = fdpRequestImpl.getAuxiliaryRequestParameter(AuxRequestParam.CONSUMER_MSISDN_LIST);
				if (consumerMSISDN != null && !StringUtil.isNullOrEmpty(consumerMSISDN.toString())) {
					String commandSeperatedConsumerList = consumerMSISDN.toString();
					consumerList = Arrays.asList(commandSeperatedConsumerList.split(FDPConstant.COMMA));
					if (consumerList.contains(input.toString())) {
						response = getResponseForValidConsumer(input, fdpServiceProvSubType, fdpRequestImpl);
					} else {
						response = new FDPPolicyResponseImpl(PolicyStatus.FAILURE, null, null, true,
								ResponseUtil.createResponseMessageInList(fdpRequest.getChannel(),
										FulfillmentResponseCodes.CONSNUMER_MSISDN_NOT_VALID.getReason(),
								TLVOptions.SESSION_TERMINATE));
					}
				}
			}
		}
		return response;
	}

	/**
	 * This method gets the response of Correct consumer.
	 */
	private static FDPPolicyResponse getResponseForValidConsumer(final Object input, 
			FDPServiceProvSubType fdpServiceProvSubType, final FDPRequestImpl fdpRequestImpl) {
		FDPPolicyResponse response;
		fdpRequestImpl.putAuxiliaryRequestParameter(AuxRequestParam.CONSUMER_MSISDN, input);
		response = new FDPPolicyResponseImpl(PolicyStatus.EXECUTE_SP, null, null, true, null);
		if (FDPServiceProvSubType.SHARED_BUNDLE_VIEW_CONSUMER.equals(fdpServiceProvSubType)) {
			response = new FDPPolicyResponseImpl(PolicyStatus.EXECUTE_SP, null, null, true, null);
		} else {
			response = new FDPPolicyResponseImpl(PolicyStatus.SUCCESS, null, null, true, null);
		}
		return response;
	}

	
	/**
	 * This method will update the AUX.
	 * 
	 * @param fdpRequest
	 */
	private static void updateAux(final FDPRequest fdpRequest) {
		if(fdpRequest instanceof FDPRequestImpl) {
			final FDPRequestImpl fdpRequestImpl = (FDPRequestImpl) fdpRequest;
			fdpRequestImpl.putAuxiliaryRequestParameter(AuxRequestParam.PROVIDER_MSISDN,
					fdpRequest.getSubscriberNumber().toString());
		}
	}
	
	/**
	 * This method will push the notification text if consumer list is found null.
	 * 
	 * @param fdpRequest
	 * @return
	 * @throws ExecutionFailedException
	 */
	private static String getNoConsumerFoundResposeText(final FDPRequest fdpRequest) throws ExecutionFailedException {
		String notificationText = null;
		final CommandParamOutput commandParamOutput = FulfillmentUtil.getCommandOutput(fdpRequest, "responseDesc",
				FDPConstant.SBB_GETDETAILS_COMMAND, false);
		if (null != commandParamOutput) {
			notificationText = commandParamOutput.getValue().toString();
		}
		return notificationText;
	}
	
	/**
	 * This method will prepare the menu header footer.
	 * 
	 * @param fdpRequest
	 * @return
	 */
	private static ResponseMessage getResponseMessage(final FDPRequest fdpRequest,
			final FDPServiceProvSubType fdpServiceProvType) {
		
		final ResponseMessage responseMessage = ResponseUtil.getDisplayNodeMessage(fdpRequest.getChannel());
		if (null != responseMessage && responseMessage instanceof ResponseMessageImpl) {
			final ResponseMessageImpl responseMessageImpl = (ResponseMessageImpl) responseMessage;
			responseMessageImpl.setCurrDisplayText(getHeaderText(fdpRequest, fdpServiceProvType), DisplayArea.HEADER);
			responseMessageImpl.setCurrDisplayText(getFooterText(fdpRequest, fdpServiceProvType), DisplayArea.FOOTER);
		}
		return responseMessage;
	}
	
	/**
	 * This method will get the header text.
	 * 
	 * @param fdpRequest
	 * @return
	 */
	private static String getHeaderText(final FDPRequest fdpRequest, final FDPServiceProvSubType fdpServiceProvType) {
		String headerText = null;
		
		switch(fdpServiceProvType) {
			case SHARED_BUNDLE_VIEW_CONSUMER:
				headerText = fdpRequest.getCircle().getConfigurationKeyValueMap()
				.get(ConfigurationKey.SHARED_BONUS_BUNDLE_VIEW_HEADER_TEXT.getAttributeName());
			break;
			case SHARED_BUNDLE_DELETE_CONSUMER:
				headerText = fdpRequest.getCircle().getConfigurationKeyValueMap()
				.get(ConfigurationKey.SHARED_BONUS_BUNDLE_DEL_HEADER_TEXT.getAttributeName());
			break;	
			case SHARED_BUNDLE_UPDATE_CONSUMER_DATA:	
				headerText = fdpRequest.getCircle().getConfigurationKeyValueMap()
				.get(ConfigurationKey.SHARED_BONUS_BUNDLE_UPGRADE_HEADER_TEXT.getAttributeName());
			break;	
			default:
				break;
		}
		return (null != headerText) ? headerText : "Available Family Members";
	}
	
	/**
	 * This method will get the footer text.
	 * 
	 * @param fdpRequest
	 * @return
	 */
	private static String getFooterText(final FDPRequest fdpRequest, final FDPServiceProvSubType fdpServiceProvType) {
		String footerText = null;
		
		switch(fdpServiceProvType) {
			case SHARED_BUNDLE_VIEW_CONSUMER:
				footerText = fdpRequest.getCircle().getConfigurationKeyValueMap()
				.get(ConfigurationKey.SHARED_BONUS_BUNDLE_VIEW_FOOTER_TEXT.getAttributeName());
				footerText = (null != footerText) ? footerText : "Enter Consumer number to check data of Consumer. "
						+ "Otherwise Press "+ PropertyUtils.getProperty(SharedAccountConstants.VIEW_EXIT_CODE) +" to Exit.";
			break;
			case SHARED_BUNDLE_DELETE_CONSUMER:
				footerText = fdpRequest.getCircle().getConfigurationKeyValueMap()
				.get(ConfigurationKey.SHARED_BONUS_BUNDLE_DEL_FOOTER_TEXT.getAttributeName());
				footerText = (null != footerText) ? footerText : "Enter S No to remove the member";
				break;	
			case SHARED_BUNDLE_UPDATE_CONSUMER_DATA:	
				footerText = fdpRequest.getCircle().getConfigurationKeyValueMap()
						.get(ConfigurationKey.SHARED_BONUS_BUNDLE_UPGRADE_FOOTER_TEXT.getAttributeName());
				footerText = (null != footerText) ? footerText: "Enter the consumer msisdn to upgrade";	
				break;	
			default:
				break;
		}
		return footerText;

	}
 }
