package com.ericsson.fdp.business.policy.policyrule.impl;

import java.util.Map;

import com.ericsson.fdp.business.dto.Me2uProductDTO;
import com.ericsson.fdp.business.enums.AuxRequestParam;
import com.ericsson.fdp.business.enums.ConfigurationKey;
import com.ericsson.fdp.business.enums.PolicyStatus;
import com.ericsson.fdp.business.enums.RequestMetaValuesKey;
import com.ericsson.fdp.business.enums.TLVOptions;
import com.ericsson.fdp.business.policy.policyrule.PolicyRule;
import com.ericsson.fdp.business.product.Product;
import com.ericsson.fdp.business.product.impl.BaseProduct;
import com.ericsson.fdp.business.request.FDPPolicyResponse;
import com.ericsson.fdp.business.request.impl.FDPPolicyResponseImpl;
import com.ericsson.fdp.business.request.impl.FDPResponseImpl;
import com.ericsson.fdp.business.util.ResponseUtil;
import com.ericsson.fdp.common.constants.FDPConstant;
import com.ericsson.fdp.common.enums.Status;
import com.ericsson.fdp.core.exception.ExecutionFailedException;
import com.ericsson.fdp.core.request.FDPRequest;
import com.ericsson.fdp.core.request.FDPResponse;
import com.ericsson.fdp.core.request.impl.FDPSMPPRequestImpl;

public class ConfirmData2SharePolicyRuleImpl extends AbstractPolicyRule implements PolicyRule {

	/**
	 * 
	 */
	private static final long serialVersionUID = 464158835888L;

	@Override
	public FDPResponse displayRule(FDPRequest fdpRequest)
			throws ExecutionFailedException {
		FDPResponse fdpResponse = null;
		String transCharges = "0";
		Product product = (Product) fdpRequest.getValueFromRequest(RequestMetaValuesKey.PRODUCT);
		BaseProduct baseProduct = (BaseProduct) product;
		if(baseProduct.getCharges() != null)
			transCharges	=	baseProduct.getCharges();
		
		
		
		 final Map<String, String> configurationMap = fdpRequest.getCircle().getConfigurationKeyValueMap();
		 String notificationText = configurationMap.get(ConfigurationKey.DATA2SHARE_CONFIRM_NOTIF.getAttributeName());
		 String kwachaNegweeFactor = configurationMap.get(ConfigurationKey.ONE_KWACHA_IN_NGWEE.getAttributeName());
		 if(null == kwachaNegweeFactor)
			 kwachaNegweeFactor = "1";
		
		 if (notificationText == null || notificationText.isEmpty()) {
			 notificationText = "You will be charged with "+transCharges+" Negwee for sharing the data. Press 1 to proceed.";
		 }
		 else {
			 if(notificationText.contains("${transChargesNegwee}")) {
				 notificationText= notificationText.replace("${transChargesNegwee}", transCharges);
			 }
			 else if(notificationText.contains("${transChargesKwacha}")) {
				 notificationText= notificationText.replace("${transChargesKwacha}", transCharges.equals("0")?"0":(Long.parseLong(transCharges)/Long.parseLong(kwachaNegweeFactor))+"");
			 }
		 }
		
		 if (notificationText != null) {
				fdpResponse = new FDPResponseImpl(Status.SUCCESS, false, ResponseUtil.createResponseMessageInList(
						fdpRequest.getChannel(), notificationText, TLVOptions.SESSION_CONTINUE));
			}
		
		
		
		
		((FDPSMPPRequestImpl)fdpRequest).putAuxiliaryRequestParameter(AuxRequestParam.isData2Share, FDPConstant.TRUE);
		return fdpResponse;
	}

	public FDPPolicyResponse validatePolicyRule(Object input,
			FDPRequest fdpRequest, Object... otherParams)
			throws ExecutionFailedException {
		FDPPolicyResponse response = new FDPPolicyResponseImpl(PolicyStatus.SUCCESS, null, null, true, null);
		String requestString = ((FDPSMPPRequestImpl)fdpRequest).getRequestString();
		try {

			if(null==requestString || !requestString.equals("1")) {
				response = new FDPPolicyResponseImpl(PolicyStatus.FAILURE, requestString, null, true,
						ResponseUtil.createResponseMessageInList(fdpRequest.getChannel(), "Invalid input",
								TLVOptions.SESSION_TERMINATE));
			}
			Me2uProductDTO me2uProdDTO = (Me2uProductDTO) fdpRequest.getAuxiliaryRequestParameter(AuxRequestParam.SELECTED_Me2UDATA2SHARE_PRODUCT);
			Product product = (Product) fdpRequest.getValueFromRequest(RequestMetaValuesKey.PRODUCT);
			BaseProduct baseProduct = (BaseProduct) product;
			if(null != baseProduct.getCharges()) {
				if(Long.parseLong(baseProduct.getCharges()) > Long.parseLong(me2uProdDTO.getAccountValue())) {
					response = new FDPPolicyResponseImpl(PolicyStatus.FAILURE, requestString, null, true,
							ResponseUtil.createResponseMessageInList(fdpRequest.getChannel(), "User cannot proceed as subscriber has low balance",
									TLVOptions.SESSION_TERMINATE));
				}
			}
		}
		catch (Exception e) {
			response = new FDPPolicyResponseImpl(PolicyStatus.FAILURE, requestString, null, true,
					ResponseUtil.createResponseMessageInList(fdpRequest.getChannel(), "Invalid input",
							TLVOptions.SESSION_TERMINATE));
		}
		return response;
	}
	
	
}
