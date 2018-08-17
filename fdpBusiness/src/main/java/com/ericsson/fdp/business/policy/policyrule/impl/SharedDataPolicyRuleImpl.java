package com.ericsson.fdp.business.policy.policyrule.impl;

import java.util.List;

import ch.qos.logback.classic.Logger;

import com.ericsson.fdp.business.constants.BusinessConstants;
import com.ericsson.fdp.business.dto.Me2uProductDTO;
import com.ericsson.fdp.business.enums.AuxRequestParam;
import com.ericsson.fdp.business.enums.Command;
import com.ericsson.fdp.business.enums.PolicyStatus;
import com.ericsson.fdp.business.enums.RequestMetaValuesKey;
import com.ericsson.fdp.business.enums.TLVOptions;
import com.ericsson.fdp.business.policy.policyrule.PolicyRule;
import com.ericsson.fdp.business.request.FDPPolicyResponse;
import com.ericsson.fdp.business.request.impl.FDPPolicyResponseImpl;
import com.ericsson.fdp.business.request.impl.FDPResponseImpl;
import com.ericsson.fdp.business.step.execution.impl.rsdeprovision.AbstractRSActiveAccounts;
import com.ericsson.fdp.business.util.Data2ShareService;
import com.ericsson.fdp.business.util.LoggerUtil;
import com.ericsson.fdp.business.util.NotificationUtil;
import com.ericsson.fdp.business.util.ResponseUtil;
import com.ericsson.fdp.common.constants.FDPConstant;
import com.ericsson.fdp.common.enums.Status;
import com.ericsson.fdp.core.command.FDPCommand;
import com.ericsson.fdp.core.exception.ExecutionFailedException;
import com.ericsson.fdp.core.request.FDPRequest;
import com.ericsson.fdp.core.request.FDPResponse;
import com.ericsson.fdp.core.request.impl.FDPRequestImpl;
import com.ericsson.fdp.core.request.impl.FDPSMPPRequestImpl;
import com.ericsson.fdp.core.utils.ApplicationConfigUtil;
import com.ericsson.fdp.dao.enums.DBActionClassName;

public class SharedDataPolicyRuleImpl extends AbstractPolicyRule implements PolicyRule {
	
	/** The ejb lookup name. */
	private final String ejbLookupName = DBActionClassName.RS_DE_PROVISIONING_SERVICE.getLookUpClass();
	
	private final String data2shareLookUp = DBActionClassName.DATA2SHARE_SERVICE.getLookUpClass();

	
	/**
	 * 
	 */
	private static final long serialVersionUID = 61415883516887L;

	@Override
	public FDPResponse displayRule(FDPRequest fdpRequest)
			throws ExecutionFailedException {
		
		if (ejbLookupName == null) {
			throw new ExecutionFailedException("Could not find ejb class for execution" + ejbLookupName);
		}
		try {
			FDPCommand fdpCommandGBAD = null;
			final Object beanObject = ApplicationConfigUtil.getBean(ejbLookupName);

			FDPResponse fdpResponse = null;
			if (null != beanObject && (beanObject instanceof AbstractRSActiveAccounts)) {
				final AbstractRSActiveAccounts accountService = (AbstractRSActiveAccounts) beanObject;
				fdpCommandGBAD = accountService.isCommandExecuted(fdpRequest, Command.GET_BALANCE_AND_DATE.getCommandDisplayName());

				final Object data2shareObj = ApplicationConfigUtil.getBean(data2shareLookUp);
				if(data2shareObj instanceof Data2ShareService) {
					final Data2ShareService data2ShareService = (Data2ShareService) data2shareObj;
					List<Me2uProductDTO> me2UProductDTO = data2ShareService.getData2ShareProducts(fdpCommandGBAD, fdpRequest);

					((FDPSMPPRequestImpl)fdpRequest).putAuxiliaryRequestParameter(AuxRequestParam.Me2UOfferDetails, me2UProductDTO);
					if(null != me2UProductDTO && !me2UProductDTO.isEmpty())
					fdpResponse = new FDPResponseImpl(Status.SUCCESS, false, ResponseUtil.createResponseMessageInList(
							fdpRequest.getChannel(), getNotificationText(fdpRequest, me2UProductDTO), TLVOptions.SESSION_CONTINUE));
					else
						fdpResponse = new FDPResponseImpl(Status.SUCCESS, false, ResponseUtil.createResponseMessageInList(
								fdpRequest.getChannel(), getNotificationText(fdpRequest, me2UProductDTO), TLVOptions.SESSION_TERMINATE));

				}
				return fdpResponse;
			}

		}
		catch(Exception e){
			e.printStackTrace();
		}


		return null;
	}
	
	private String getNotificationText(FDPRequest fdpRequest,
			List<Me2uProductDTO> me2uDTOList) {
		StringBuilder notificationText = new StringBuilder("");
		int i=1;
		if(null != me2uDTOList && !me2uDTOList.isEmpty()) {
			for(Me2uProductDTO me2uProductDTO : me2uDTOList) {
				if(null==me2uProductDTO) {
					notificationText.append("You are not eligible to share the data");
					break;
				}
				notificationText.append(i +". "+ me2uProductDTO.getProduct().getProductName()+FDPConstant.SPACE+ me2uProductDTO.getAvailableBalance()+"MB"+FDPConstant.SPACE+ "Expiry: "+me2uProductDTO.getExpiryDate()+"\n");
				i++;
			}
		}
		else {
			try {
				Long notificationId = Long.valueOf(BusinessConstants.NO_ACTIVATED_BUNDLE);
				Logger circleLogger = LoggerUtil.getSummaryLoggerFromRequest(fdpRequest);
				String notificationTxt = NotificationUtil.createNotificationText(fdpRequest, notificationId, circleLogger);
				if(null == notificationTxt)
					notificationText.append("You are not eligible to share the data");
				else
					notificationText.append(notificationTxt);
			}
			catch(Exception e) {
				
			}
		}

		return notificationText.toString();
	}


	@SuppressWarnings("unchecked")
	@Override
	public FDPPolicyResponse validatePolicyRule(Object input,
			FDPRequest fdpRequest, Object... otherParams)
			throws ExecutionFailedException {
		FDPPolicyResponse response = new FDPPolicyResponseImpl(PolicyStatus.SUCCESS, null, null, true, null);
		String requestString = ((FDPSMPPRequestImpl)fdpRequest).getRequestString();
		if(null != requestString) {
			try {
				int requestStringInt = Integer.parseInt(requestString);
				List<Me2uProductDTO> meuProductList = (List<Me2uProductDTO>) ((FDPSMPPRequestImpl)fdpRequest).getAuxiliaryRequestParameter(AuxRequestParam.Me2UOfferDetails);
				Me2uProductDTO me2uProductDTO = null;
				if(null != meuProductList.get(requestStringInt-1)) {
					me2uProductDTO = meuProductList.get(requestStringInt-1);
					((FDPSMPPRequestImpl)fdpRequest).putAuxiliaryRequestParameter(AuxRequestParam.SELECTED_Me2UDATA2SHARE_PRODUCT, me2uProductDTO);
				}
				else {
					return response = new FDPPolicyResponseImpl(PolicyStatus.FAILURE, requestString, null, true,
							ResponseUtil.createResponseMessageInList(fdpRequest.getChannel(), "Invalid input",
									TLVOptions.SESSION_TERMINATE));
				}

				final FDPRequestImpl fdpRequestImpl = (FDPRequestImpl) fdpRequest;

				fdpRequestImpl.addMetaValue(RequestMetaValuesKey.PRODUCT, me2uProductDTO.getProduct());
			}
			catch (Exception e) {
				return response = new FDPPolicyResponseImpl(PolicyStatus.FAILURE, requestString, null, true,
						ResponseUtil.createResponseMessageInList(fdpRequest.getChannel(), "Invalid input",
								TLVOptions.SESSION_TERMINATE));
			}
		}
		else
			return response = new FDPPolicyResponseImpl(PolicyStatus.FAILURE, requestString, null, true,
					ResponseUtil.createResponseMessageInList(fdpRequest.getChannel(), "Invalid input",
							TLVOptions.SESSION_TERMINATE));
		return response;
	}

}
