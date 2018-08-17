package com.ericsson.fdp.business.serviceprovisioning.impl;

import javax.ejb.Stateless;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ericsson.fdp.FDPCacheable;
import com.ericsson.fdp.business.enums.RequestMetaValuesKey;
import com.ericsson.fdp.business.exception.RuleException;
import com.ericsson.fdp.business.serviceprovisioning.rule.ServiceProvisioningRule;
import com.ericsson.fdp.business.util.LoggerUtil;
import com.ericsson.fdp.business.util.RequestUtil;
import com.ericsson.fdp.common.enums.ChannelType;
import com.ericsson.fdp.core.exception.ExecutionFailedException;
import com.ericsson.fdp.core.logging.FDPLogger;
import com.ericsson.fdp.core.node.FDPNode;
import com.ericsson.fdp.core.request.FDPRequest;
import com.ericsson.fdp.core.request.FDPResponse;
import com.ericsson.fdp.core.request.FDPSMPPRequest;

/**
 * MMServiceProvisioningImpl class
 * @author GUR21122
 *
 */
@Stateless
public class MMServiceProvisioningImpl {

	private static final Logger LOGGER = LoggerFactory.getLogger(MMServiceProvisioningImpl.class);
	
	/**
	 * This method will execute the service provisioning of the mobile money request
	 * @param fdpRequest
	 * @return
	 * @throws ExecutionFailedException
	 * @throws RuleException
	 */
	public FDPResponse executeServiceProvisioning(final FDPRequest fdpRequest)
			throws ExecutionFailedException, RuleException {
		final Logger circleLogger = LoggerUtil.getSummaryLoggerFromRequest(fdpRequest);
		ServiceProvisioningRule serviceProvisioningRule = null;
		FDPResponse fdpresponse=null;
		String response = null;
		final FDPCacheable serviceProvisioningObject = fdpRequest
				.getValueFromRequest(RequestMetaValuesKey.SERVICE_PROVISIONING);
		try {
			if (serviceProvisioningObject instanceof ServiceProvisioningRule) {
				serviceProvisioningRule = (ServiceProvisioningRule) serviceProvisioningObject;
				fdpresponse=serviceProvisioningRule.execute(fdpRequest);
				
			}
			else
			{
				LOGGER.error("INVALID REQUEST MOBILE WALLET "+fdpRequest.getRequestId() +" ,MSISDN :"+fdpRequest.getSubscriberNumber() );
			}
		} catch (Exception e) {
			LOGGER.error("Exception occurs while processing mobile money debit complete request:" + e);
		} finally {
			//Changes done to write transaction status in log
			response = RequestUtil.writeProvRslt(fdpRequest, fdpresponse, circleLogger);
			FDPLogger.info(circleLogger, getClass(), "executeServiceProvisioning()", response);
			FDPNode fdpNode = null;
			if (ChannelType.USSD.equals(fdpRequest.getChannel())) {
				fdpNode = ((FDPSMPPRequest)fdpRequest).getNode(fdpRequest.getLastServedString());
			}
			LoggerUtil.generatePostLogsForUserBehaviour((FDPSMPPRequest)fdpRequest, fdpNode, false); 
		}
		
		return fdpresponse;
	}
	
	
}
