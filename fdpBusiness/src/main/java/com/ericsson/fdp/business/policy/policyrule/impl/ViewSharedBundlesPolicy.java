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

public class ViewSharedBundlesPolicy extends AbstractPolicyRule implements PolicyRule {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 61415866666887L;

	/** The ejb lookup name. */
	private final String ejbLookupName = DBActionClassName.RS_DE_PROVISIONING_SERVICE.getLookUpClass();
	
	private final String data2shareLookUp = DBActionClassName.DATA2SHARE_SERVICE.getLookUpClass();

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
					notificationText.append("No activated shared data bundle");
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
					notificationText.append("No activated shared data bundle");
				else
					notificationText.append(notificationTxt);
			}
			catch(Exception e) {
				System.out.println("ERROR :: NO_ACTIVATED_BUNDLE");
			}
		}
			

		return notificationText.toString();
	}


	@Override
	public FDPPolicyResponse validatePolicyRule(Object input,
			FDPRequest fdpRequest, Object... otherParams)
			throws ExecutionFailedException {
		
		return new FDPPolicyResponseImpl(PolicyStatus.FAILURE, null, null, true,
				ResponseUtil.createResponseMessageInList(fdpRequest.getChannel(), "Invalid input",
						TLVOptions.SESSION_TERMINATE));
	}

}
