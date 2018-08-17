package com.ericsson.fdp.business.policy.policyrule.impl;

import java.util.List;

import com.ericsson.fdp.business.enums.AuxRequestParam;
import com.ericsson.fdp.business.enums.PolicyStatus;
import com.ericsson.fdp.business.enums.TLVOptions;
import com.ericsson.fdp.business.policy.policyrule.PolicyRule;
import com.ericsson.fdp.business.request.FDPPolicyResponse;
import com.ericsson.fdp.business.request.impl.FDPPolicyResponseImpl;
import com.ericsson.fdp.business.request.impl.FDPResponseImpl;
import com.ericsson.fdp.business.sharedaccount.ActiveAccountService;
import com.ericsson.fdp.business.util.ResponseUtil;
import com.ericsson.fdp.common.enums.Status;
import com.ericsson.fdp.core.exception.ExecutionFailedException;
import com.ericsson.fdp.core.request.FDPRequest;
import com.ericsson.fdp.core.request.FDPResponse;
import com.ericsson.fdp.core.request.impl.FDPSMPPRequestImpl;
import com.ericsson.fdp.core.utils.ApplicationConfigUtil;
import com.ericsson.fdp.dao.enums.DBActionClassName;

public class SubDeprovisionPolicyRuleImpl extends AbstractPolicyRule implements PolicyRule {

	/**
	 * 
	 */
	private static final long serialVersionUID = -6141588351678L;
	
	/** The ejb lookup name. */
	private final String ejbLookupName = DBActionClassName.RS_DE_PROVISIONING_SERVICE.getLookUpClass();

	@Override
	public FDPResponse displayRule(FDPRequest fdpRequest)
			throws ExecutionFailedException {
		if (ejbLookupName == null) {
			throw new ExecutionFailedException("Could not find ejb class for execution" + ejbLookupName);
		}
		try {
			final Object beanObject = ApplicationConfigUtil.getBean(ejbLookupName);
			List<String> productNames = null;
			FDPResponse fdpResponse = null;
			if (null != beanObject && (beanObject instanceof ActiveAccountService)) {
				final ActiveAccountService accountService = (ActiveAccountService) beanObject;
				productNames = accountService.getRsDeProvisionList(fdpRequest);
				
				//Setting product in response
				((FDPSMPPRequestImpl)fdpRequest).putAuxiliaryRequestParameter(AuxRequestParam.deprovisionP, "Y");
				fdpResponse = new FDPResponseImpl(Status.SUCCESS, false, ResponseUtil.createResponseMessageInList(
						fdpRequest.getChannel(), getNotificationText(fdpRequest, productNames), TLVOptions.SESSION_CONTINUE));

				return fdpResponse;
			}
		}
		catch(Exception e){
			e.printStackTrace();
		}
		return null;
	}
	
	private String getNotificationText(FDPRequest fdpRequest,
			List<String> productNames) {
		
		StringBuilder notificationText = new StringBuilder("");
		 for(int i = 1; i <= productNames.size(); i++){
			 notificationText.append(i +". "+ productNames.get(i-1) +"\n");
	    }
		 return notificationText.toString();
	}

	@Override
	public FDPPolicyResponse validatePolicyRule(Object input,
			FDPRequest fdpRequest, Object... otherParams)
			throws ExecutionFailedException {
		FDPPolicyResponse response = new FDPPolicyResponseImpl(PolicyStatus.SUCCESS, null, null, true, null);
		((FDPSMPPRequestImpl)fdpRequest).putAuxiliaryRequestParameter(AuxRequestParam.deprovisionP, "Y");
		
		return response;
	}

}
