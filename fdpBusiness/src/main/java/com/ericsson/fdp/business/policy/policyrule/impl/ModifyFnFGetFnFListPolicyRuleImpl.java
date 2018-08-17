package com.ericsson.fdp.business.policy.policyrule.impl;

import java.util.Map;

import javax.inject.Inject;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ericsson.fdp.business.enums.AuxRequestParam;
import com.ericsson.fdp.business.enums.ConfigurationKey;
import com.ericsson.fdp.business.enums.PolicyStatus;
import com.ericsson.fdp.business.enums.PolicyValidationMessageEnum;
import com.ericsson.fdp.business.enums.TLVOptions;

import com.ericsson.fdp.business.product.Product;
import com.ericsson.fdp.business.request.FDPPolicyResponse;
import com.ericsson.fdp.business.request.impl.FDPPolicyResponseImpl;
import com.ericsson.fdp.business.request.impl.FDPResponseImpl;
import com.ericsson.fdp.business.util.FnfUtil;
import com.ericsson.fdp.business.util.ResponseUtil;
import com.ericsson.fdp.common.constants.FDPConstant;
import com.ericsson.fdp.common.enums.Status;
import com.ericsson.fdp.common.util.StringUtil;
import com.ericsson.fdp.core.exception.ExecutionFailedException;
import com.ericsson.fdp.core.request.FDPRequest;
import com.ericsson.fdp.core.request.FDPResponse;
import com.ericsson.fdp.core.request.impl.FDPRequestImpl;

/**
 * This policy class will get friends and family list of subscriber. And will
 * ask subscriber input for faf number update.
 * 
 * @author evasaty
 * 
 */
public class ModifyFnFGetFnFListPolicyRuleImpl extends PolicyRuleImpl {

	private static final long serialVersionUID = 1L;

	/** The Constant LOGGER. */
	private static final Logger LOGGER = LoggerFactory.getLogger(ModifyFnFGetFnFListPolicyRuleImpl.class);

	private Map<Integer, String> fafList;
	
	
	

	@Override
	public FDPResponse displayRule(FDPRequest fdpRequest) throws ExecutionFailedException {
		FDPResponse fdpResponse = null;
		boolean isFaFEnabled = false;
		FnfUtil fnfUtil = new FnfUtil();
		final Product product = fnfUtil.getProduct(fdpRequest);
		if (product != null) {
			isFaFEnabled = product.getIsFafType();
			if (isFaFEnabled && fnfUtil.isProductSpTypeValid(fdpRequest)) {
				fafList = fnfUtil.getFriendsAndFamilyList(fdpRequest);
				// Check is user register or not.
				if (FDPConstant.FAF_MSISDN_ALREADY_REGISTER == fnfUtil.IsSubscriberAlreadyRegisterForFaf(fdpRequest)) {
					if (null == fafList || fafList.isEmpty()) {
						LOGGER.error(fdpRequest.getIncomingSubscriberNumber() + FDPConstant.LOGGER_KEY_VALUE_DELIMITER
								+ "FAF List is empty. Terminating session.");
						fdpResponse = new FDPResponseImpl(Status.SUCCESS, false,
								ResponseUtil.createResponseMessageInList(fdpRequest.getChannel(),
										getNotificationText(fdpRequest), TLVOptions.SESSION_TERMINATE));
					} else {
						fdpResponse = new FDPResponseImpl(Status.SUCCESS, false,
								ResponseUtil.createResponseMessageInList(fdpRequest.getChannel(),
										getNotificationText(fdpRequest), TLVOptions.SESSION_CONTINUE));
					}
				} else {
					LOGGER.error("Product" + FDPConstant.LOGGER_KEY_VALUE_DELIMITER + product.getProductDescription()
							+ FDPConstant.LOGGER_METHOD_PARAMETER_SEPA + "User is not register for FAF.");
					fdpResponse = new FDPResponseImpl(Status.SUCCESS, false, ResponseUtil.createResponseMessageInList(
							fdpRequest.getChannel(), "User is not register for FAF.", TLVOptions.SESSION_TERMINATE));
				}

			} else {
				LOGGER.error("Product:" + product.getProductDescription() + FDPConstant.LOGGER_KEY_VALUE_DELIMITER
						+ "Product is not of FAF type.");
				fdpResponse = new FDPResponseImpl(Status.SUCCESS, false, ResponseUtil.createResponseMessageInList(
						fdpRequest.getChannel(), "Product is not of FAF type.", TLVOptions.SESSION_TERMINATE));
			}
		} else {
			LOGGER.error("Product not found in cache." + "RID" + FDPConstant.LOGGER_KEY_VALUE_DELIMITER
					+ fdpRequest.getRequestId());
			fdpResponse = new FDPResponseImpl(Status.SUCCESS, false, ResponseUtil.createResponseMessageInList(
					fdpRequest.getChannel(), "Some exception occured. Please try after some time.",
					TLVOptions.SESSION_TERMINATE));
		}
		return fdpResponse;
	}

	public String getNotificationText(final FDPRequest fdpRequest) throws ExecutionFailedException {
		StringBuffer notificationText = new StringBuffer();
		int i = 1;
		final Map<String, String> configurationMap = fdpRequest.getCircle().getConfigurationKeyValueMap();
		if (!fafList.isEmpty()) {
			if (null != configurationMap.get(ConfigurationKey.VIEW_FNF_LIST_TEXT.getAttributeName())) {
				notificationText.append(configurationMap.get(ConfigurationKey.VIEW_FNF_LIST_TEXT.getAttributeName()));
			} else {
				notificationText.append("Friends & Family list: ");
			}
			while (null != fafList.get(i)) {
				notificationText.append(FDPConstant.SPACE + i + FDPConstant.PARAMETER_SEPARATOR + fafList.get(i));
				i++;
			}
		} else {
			notificationText.append(configurationMap.get(ConfigurationKey.FAF_LIST_IS_EMPTY.getAttributeName()));
		}
		return notificationText.toString();
	}

	@Override
	public FDPPolicyResponse validatePolicyRule(Object input, FDPRequest fdpRequest, Object... otherParams)
			throws ExecutionFailedException {
		FDPPolicyResponse response = new FDPPolicyResponseImpl(PolicyStatus.SUCCESS, null, null, true, null);
		if ((input == null && condition != null)) {
			response = new FDPPolicyResponseImpl(PolicyStatus.POLICY_VALUE_NOT_FOUND, null, null, false, null);
		} else {
			try {
				
				boolean isValid = PolicyRuleValidateImpl.isInteger(input);
				if(isValid){
					isValid = false;
					int inputValue = Integer.parseInt(input.toString());
					if (null != fafList.get(inputValue)) {
						isValid = true;
						((FDPRequestImpl) fdpRequest).putAuxiliaryRequestParameter(AuxRequestParam.FAF_NUMBER_GAD,
								fafList.get(inputValue));
						((FDPRequestImpl) fdpRequest).putAuxiliaryRequestParameter(AuxRequestParam.FAF_MSISDN_DELETE,
								fafList.get(inputValue));
						((FDPRequestImpl) fdpRequest).putAuxiliaryRequestParameter(
								AuxRequestParam.FAF_EXECUTE_COMMAND_AGAIN, "true");
					} 
				}
				if(!isValid) {
					String msg = PolicyRuleValidateImpl.errorMsg(PolicyValidationMessageEnum.ERROR_MSG, "Please provide Numeric value");
					response = new FDPPolicyResponseImpl(PolicyStatus.FAILURE,msg , null, true,
							ResponseUtil.createResponseMessageInList(fdpRequest.getChannel(), msg,
									TLVOptions.SESSION_TERMINATE));
				}
				fafList.clear();
			} catch (final Exception e) {
				LOGGER.error("Policy rule could not be evaluated" + FDPConstant.LOGGER_KEY_VALUE_DELIMITER
						+ this.getClass());
				throw new ExecutionFailedException("The policy rule could not be evaluated.", e);
			}
		}
		return response;
	}
}
