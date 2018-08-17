package com.ericsson.fdp.business.policy.policyrule.impl;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.ericsson.fdp.FDPCacheable;
import com.ericsson.fdp.business.command.AbstractCommand;
import com.ericsson.fdp.business.display.impl.DisplayObjectImpl;
import com.ericsson.fdp.business.display.impl.ResponseMessageImpl;
import com.ericsson.fdp.business.enums.AuxRequestParam;
import com.ericsson.fdp.business.enums.Command;
import com.ericsson.fdp.business.enums.DisplayArea;
import com.ericsson.fdp.business.enums.PolicyStatus;
import com.ericsson.fdp.business.enums.RequestMetaValuesKey;
import com.ericsson.fdp.business.enums.TLVOptions;
import com.ericsson.fdp.business.node.impl.OptInOptOutServiceNode;
import com.ericsson.fdp.business.node.impl.SpecialMenuNode;
import com.ericsson.fdp.business.product.Product;
import com.ericsson.fdp.business.request.FDPPolicyResponse;
import com.ericsson.fdp.business.request.impl.FDPPolicyResponseImpl;
import com.ericsson.fdp.business.request.impl.FDPResponseImpl;
import com.ericsson.fdp.business.serviceprovisioning.rule.ServiceProvisioningRule;
import com.ericsson.fdp.business.util.CommandUtil;
import com.ericsson.fdp.business.util.RequestUtil;
import com.ericsson.fdp.business.util.ResponseUtil;
import com.ericsson.fdp.common.constants.FDPConstant;
import com.ericsson.fdp.common.enums.ModuleType;
import com.ericsson.fdp.common.enums.Status;
import com.ericsson.fdp.core.command.FDPCommand;
import com.ericsson.fdp.core.command.param.CommandParam;
import com.ericsson.fdp.core.display.ResponseMessage;
import com.ericsson.fdp.core.exception.ExecutionFailedException;
import com.ericsson.fdp.core.node.FDPNode;
import com.ericsson.fdp.core.request.FDPRequest;
import com.ericsson.fdp.core.request.FDPResponse;
import com.ericsson.fdp.core.request.impl.FDPRequestImpl;
import com.ericsson.fdp.core.serviceprovisioning.bean.FDPMetaBag;
import com.ericsson.fdp.core.utils.ApplicationConfigUtil;
import com.ericsson.fdp.dao.enums.FDPServiceProvSubType;
import com.ericsson.fdp.dao.enums.FDPServiceProvType;
import com.ericsson.fdp.dao.util.ServiceProvDataConverterUtil;

/**
 * Class for OptIn Opt Out Policy Rule.
 * 
 * @author eashtod
 *
 */
public class OptInOptOutPolicyRuleImpl extends AbstractPolicyRule {

	/**
	 * Class Serial Version UID.
	 */
	private static final long serialVersionUID = 61415883516799L;

	private static Map<String, String> productIdMapping = null;

	@Override
	public FDPResponse displayRule(FDPRequest fdpRequest) throws ExecutionFailedException {
		FDPResponse fdpResponse = null;
		productIdMapping = new HashMap<String, String>();
		if (executeSingleProvisioningRequestCommand(fdpRequest)) {
			final FDPCommand fdpCommand = fdpRequest
					.getExecutedCommand(Command.GET_SERVICES_DETAILS_REQUEST.getCommandDisplayName());
			fdpResponse = prepareSucessResponse(fdpRequest, fdpCommand);
		} else {
			fdpResponse = prepareFailureResponse(fdpRequest, false);
		}
		return fdpResponse;
	}

	/**
	 * This method prepares the failure response.
	 * 
	 * @param fdpRequest
	 * @return
	 */
	private FDPResponse prepareFailureResponse(FDPRequest fdpRequest, final boolean isCommandExecuted) {
		String notificationText = null;
		String generalFailureText = "Unable to process request, please after some time.";
		final FDPServiceProvSubType fdpServiceProvSubType = RequestUtil.getFDPServiceProvSubType(fdpRequest);
		switch (fdpServiceProvSubType) {
		case OPT_IN_SERVICE:
			notificationText = isCommandExecuted ? "You don't have any One Time Product subscribed."
					: generalFailureText;
			break;
		case OPT_OUT_SERVICE:
			notificationText = isCommandExecuted ? "You don't have any Renewal Type Product subscribed."
					: generalFailureText;
			break;
		default:
			break;
		}
		return new FDPResponseImpl(Status.SUCCESS, true, null, ResponseUtil
				.createResponseMessageInList(fdpRequest.getChannel(), notificationText, TLVOptions.SESSION_TERMINATE));
	}

	@Override
	public FDPPolicyResponse validatePolicyRule(Object input, FDPRequest fdpRequest, Object... otherParams)
			throws ExecutionFailedException {
		String productId = null;
		productId = productIdMapping.get((String) input);
		FDPPolicyResponse response = null;
		if (productId != null) {
			final Product product = (Product) RequestUtil.getProductById(fdpRequest, productId);
			final FDPCacheable serviceProvisioning = RequestUtil.getServiceProvisioningById(fdpRequest,
					ServiceProvDataConverterUtil.getKeyForServiceProvMetaBag(Long.valueOf(productId),
							FDPServiceProvType.PRODUCT, FDPServiceProvSubType.PRODUCT_BUY_RECURRING));
			if (fdpRequest instanceof FDPRequestImpl) {
				FDPRequestImpl fdpRequestImpl = (FDPRequestImpl) fdpRequest;
				fdpRequestImpl.addMetaValue(RequestMetaValuesKey.PRODUCT, product);
				fdpRequestImpl.addMetaValue(RequestMetaValuesKey.SERVICE_PROVISIONING, serviceProvisioning);
			}
			response = new FDPPolicyResponseImpl(PolicyStatus.SUCCESS, null, null, true, null);
		} else {
			response = new FDPPolicyResponseImpl(PolicyStatus.FAILURE, "The input is incorrect", null, true,
					ResponseUtil.createResponseMessageInList(fdpRequest.getChannel(), "The input is incorrect",
							TLVOptions.SESSION_TERMINATE));
		}
		return response;
	}

	/**
	 * Method executes SingleProvisioningRequest.
	 * 
	 * @param fdpRequest
	 * @return
	 * @throws ExecutionFailedException
	 */
	private Boolean executeSingleProvisioningRequestCommand(final FDPRequest fdpRequest)
			throws ExecutionFailedException {
		FDPCommand fdpCommand = null;
		Boolean isSucess = false;
		final FDPCacheable fdpCommandCached = ApplicationConfigUtil.getMetaDataCache()
				.getValue(new FDPMetaBag(fdpRequest.getCircle(), ModuleType.COMMAND,
						Command.GET_SERVICES_DETAILS_REQUEST.getCommandDisplayName()));
		if (null != fdpCommandCached && fdpCommandCached instanceof AbstractCommand) {
			updateCommandInputParams(fdpRequest);
			fdpCommand = (AbstractCommand) fdpCommandCached;
			removeProductIDParam(fdpCommand);
			isSucess = (Status.SUCCESS.equals(fdpCommand.execute(fdpRequest))) ? true : false;
		}
		return isSucess;
	}

	/**
	 * Update command Input Param values.
	 * 
	 * @param fdpRequest
	 */
	private void updateCommandInputParams(final FDPRequest fdpRequest) {
		final FDPServiceProvSubType fdpServiceProvSubType = RequestUtil.getFDPServiceProvSubType(fdpRequest);
		((FDPRequestImpl) fdpRequest).putAuxiliaryRequestParameter(AuxRequestParam.OPTINOPTOUT_TYPE,
				fdpServiceProvSubType);
		switch (fdpServiceProvSubType) {
		case OPT_IN_SERVICE:
			// Udpate for OPT IN TYPE
			((FDPRequestImpl) fdpRequest).putAuxiliaryRequestParameter(AuxRequestParam.OPTINOPTOUT_TYPE,
					fdpServiceProvSubType);
			break;
		case OPT_OUT_SERVICE:

			break;
		default:
			break;
		}
	}

	/**
	 * This method gets the SP ID.
	 * 
	 * @param fdpRequest
	 * @return
	 */
	private Long getSPId(final FDPRequest fdpRequest) {
		Long spId = null;
		final FDPNode fdpNode = (FDPNode) fdpRequest.getValueFromRequest(RequestMetaValuesKey.NODE);
		if (null != fdpNode && fdpNode instanceof SpecialMenuNode) {
			SpecialMenuNode specialMenuNode = (SpecialMenuNode) fdpNode;
			spId = Long.valueOf(specialMenuNode.getEntityIdForCache(RequestMetaValuesKey.SERVICE_PROVISIONING));
		}
		return spId;
	}

	/**
	 * This method prepares the Success Response.
	 * 
	 * @param fdpRequest
	 * @param fdpCommand
	 * @return
	 * @throws ExecutionFailedException
	 */
	private FDPResponse prepareSucessResponse(final FDPRequest fdpRequest, final FDPCommand fdpCommand)
			throws ExecutionFailedException {
		List<FDPNode> fdpNodeList = null;
		FDPResponse fdpResponse = null;
		final String OPT_IN_OUT_DC_POLICY = "OPT_IN_OUT_POLICY_DC";
		final List<String> parsedResponse = parseCommandResponse(fdpRequest, fdpCommand);
		StringBuilder productIdStr = new StringBuilder();
		String loopDelim = "";
		if (null != parsedResponse && parsedResponse.size() > 0) {
			fdpNodeList = new ArrayList<>();
			// Long spId = getSPId(fdpRequest);
			final FDPServiceProvSubType fdpServiceProvSubType = RequestUtil.getFDPServiceProvSubType(fdpRequest);
			int index = 0;
			for (final String response : parsedResponse) {
				Product product = RequestUtil.getProductById(fdpRequest, response);
				if (null != product) {
					ServiceProvisioningRule sp = RequestUtil.getServiceProvisioningById(fdpRequest,
							ServiceProvDataConverterUtil.getKeyForServiceProvMetaBag(
									Long.valueOf(product.getProductId().toString()), FDPServiceProvType.PRODUCT,
									RequestUtil.getFDPServiceProvSubTypeAction(fdpServiceProvSubType)));
					// removeChargingStep(sp);
					RequestUtil.removeSteps(sp);
					final OptInOptOutServiceNode fdpNode = new OptInOptOutServiceNode(product.getProductName(), null,
							((FDPNode) fdpRequest.getValueFromRequest(RequestMetaValuesKey.NODE))
									.getFullyQualifiedPath(),
							fdpRequest.getChannel(), fdpRequest.getCircle(), null, null, null,
							sp.getServiceProvDTO().getId(), null, fdpServiceProvSubType, sp, product);
					fdpNode.setPolicyName(OPT_IN_OUT_DC_POLICY);
					fdpNodeList.add(fdpNode);
					index++;
					productIdMapping.put(String.valueOf(index), response);
					productIdStr.append(loopDelim);
					productIdStr.append(response);
					loopDelim = FDPConstant.LOGGER_DELIMITER;
				}

			}
			((FDPRequestImpl) fdpRequest).putAuxiliaryRequestParameter(AuxRequestParam.RS_PRODUCT_ID,
					productIdStr.toString());

			if (index != 0) {
				final OptInOptOutServiceNode allFdpNode = new OptInOptOutServiceNode("All", null,
						((FDPNode) fdpRequest.getValueFromRequest(RequestMetaValuesKey.NODE)).getFullyQualifiedPath(),
						fdpRequest.getChannel(), fdpRequest.getCircle(), null, null, null, null, null,
						fdpServiceProvSubType, null, null);
				allFdpNode.setPolicyName(OPT_IN_OUT_DC_POLICY);
				fdpNodeList.add(allFdpNode);
			}

			fdpResponse = RequestUtil.createResponseFromDisplayObject(
					new DisplayObjectImpl((FDPNode) fdpRequest.getValueFromRequest(RequestMetaValuesKey.NODE),
							fdpNodeList, getResponseMessage(fdpRequest)),
					fdpRequest, (FDPNode) fdpRequest.getValueFromRequest(RequestMetaValuesKey.NODE));
		} else {
			fdpResponse = prepareFailureResponse(fdpRequest, true);
		}
		return fdpResponse;
	}

	private List<String> parseCommandResponse(final FDPRequest fdpRequest, final FDPCommand fdpCommand) {
		/*
		 * List<String> productList= new ArrayList<String>();
		 * productList.add(CommandUtil.
		 * parseReponseFromRsSingleProvisioningRequest(fdpRequest,fdpCommand));
		 */ return CommandUtil.parseReponseFromRsSingleProvisioningRequest(fdpRequest, fdpCommand);
	}

	/**
	 * This method will prepare the menu header footer.
	 * 
	 * @param fdpRequest
	 * @return
	 */
	private ResponseMessage getResponseMessage(final FDPRequest fdpRequest) {
		final ResponseMessage responseMessage = ResponseUtil.getDisplayNodeMessage(fdpRequest.getChannel());
		if (null != responseMessage && responseMessage instanceof ResponseMessageImpl) {
			final ResponseMessageImpl responseMessageImpl = (ResponseMessageImpl) responseMessage;
			responseMessageImpl.setCurrDisplayText(getHeaderText(fdpRequest), DisplayArea.HEADER);
			responseMessageImpl.setCurrDisplayText(getFooterText(fdpRequest), DisplayArea.FOOTER);
		}
		return responseMessage;
	}

	/**
	 * This method will get the header text.
	 * 
	 * @param fdpRequest
	 * @return
	 */
	private String getHeaderText(final FDPRequest fdpRequest) {
		return "Available bundles";
	}

	/**
	 * This method will get the footer text.
	 * 
	 * @param fdpRequest
	 * @return
	 */
	private String getFooterText(final FDPRequest fdpRequest) {
		return "Enter #. ";
	}


	/**
	 * Remove the product id param from RS get Service Details Command
	 * */
	private void removeProductIDParam(FDPCommand fdpCommand) {
		List<CommandParam> commandparam = fdpCommand.getInputParam();
		for (int i = 0; i < fdpCommand.getInputParam().size(); i++) {
			if (fdpCommand.getInputParam().get(i).getName().equals(FDPConstant.PARAMETER_PRODUCT_ID_RS)) {
				fdpCommand.getInputParam().remove(i);
			}
		}
	}

	
}
