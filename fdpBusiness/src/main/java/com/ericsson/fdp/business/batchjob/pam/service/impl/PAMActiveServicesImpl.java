package com.ericsson.fdp.business.batchjob.pam.service.impl;

import java.util.List;

import javax.ejb.Stateless;

import com.ericsson.fdp.business.batchjob.pam.service.PAMActiveServices;
import com.ericsson.fdp.business.enums.Command;
import com.ericsson.fdp.business.util.LoggerUtil;
import com.ericsson.fdp.business.vo.FDPPamActiveServicesVO;
import com.ericsson.fdp.core.command.FDPCommand;
import com.ericsson.fdp.core.exception.ExecutionFailedException;
import com.ericsson.fdp.core.logging.FDPLogger;
import com.ericsson.fdp.core.request.FDPRequest;
import com.ericsson.fdp.core.request.impl.FDPRequestImpl;

/**
 * The Class PAMActiveServicesImpl.
 */
@Stateless(mappedName = "PAMActiveServicesImpl")
public class PAMActiveServicesImpl extends AbstractPAMActiveAccounts implements PAMActiveServices {

	/** The get account details. */
	private String getAccountDetails = Command.GETACCOUNTDETAILS.getCommandDisplayName();

	@Override
	public List<FDPPamActiveServicesVO> getProductsFromPamServicesID(final FDPRequest fdpRequest)
			throws ExecutionFailedException {
		FDPCommand commandGetAccountDetails = null;
		FDPLogger.debug(getCircleLogger(fdpRequest), getClass(), "getProductsFromPamServicesID()",
				LoggerUtil.getRequestAppender(fdpRequest) + "The request message is to find PAM for USER: "
						+ fdpRequest.getSubscriberNumber());

		if (fdpRequest instanceof FDPRequestImpl) {
			final FDPRequestImpl fdpRequestImpl = (FDPRequestImpl) fdpRequest;
			commandGetAccountDetails = fdpRequestImpl.getExecutedCommand(getAccountDetails);
			if (commandGetAccountDetails == null) {
				// As Discussed Assuming GetAccountDetails will be there in
				// fdpRequestImpl object,If
				// not available in the fdpRequest object the throw an Exception
				throw new ExecutionFailedException("Get account details is not configured for the circle."
						+ fdpRequest.getCircle().getCircleCode());
			}
		}
		return getProductIDFromPAMId(fdpRequest, getPAMIDFromCommandOutputParam(fdpRequest, commandGetAccountDetails));
	}

}
