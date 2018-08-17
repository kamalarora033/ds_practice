package com.ericsson.fdp.business.route.processor;

import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.slf4j.Logger;

import com.ericsson.fdp.business.enums.DisplayArea;
import com.ericsson.fdp.business.node.FDPServiceProvisioningNode;
import com.ericsson.fdp.business.serviceprovisioning.ServiceProvisioning;
import com.ericsson.fdp.business.util.LoggerUtil;
import com.ericsson.fdp.business.util.NotificationUtil;
import com.ericsson.fdp.business.util.ServiceProvisioningUtil;
import com.ericsson.fdp.common.enums.ChannelType;
import com.ericsson.fdp.common.enums.Status;
import com.ericsson.fdp.core.logging.FDPLogger;
import com.ericsson.fdp.core.request.FDPResponse;
import com.ericsson.fdp.core.request.FDPSMPPRequest;
import com.ericsson.fdp.dao.dto.UpdateRequestDTO;

/**
 * MobileMoneyDebitRequestProcessor class
 * @author GUR21122
 *
 */
public class MobileMoneyDebitRequestProcessor implements Processor {
	
	@Override
	public void process(Exchange exchange) throws Exception {
		
		FDPResponse fdpresponse = null;
		FDPSMPPRequest fdpRequest = null;
		FDPServiceProvisioningNode serviceProvisioningNode = null;
		ServiceProvisioning fdpServiceProvisioning = null;
		Logger circleLogger = null;
		
		if(null != exchange.getIn().getBody()){
			Object request = exchange.getIn().getBody();
			UpdateRequestDTO momoRequest = (UpdateRequestDTO) request;
			fdpRequest = (FDPSMPPRequest) momoRequest.getFdpRequest();
			serviceProvisioningNode = (FDPServiceProvisioningNode) momoRequest.getServiceProvisioningNode();
			fdpServiceProvisioning = (ServiceProvisioning) momoRequest.getServiceProvisioning();
			circleLogger = LoggerUtil.getSummaryLoggerFromRequest(fdpRequest);
			try {
				if (fdpRequest != null && serviceProvisioningNode != null && fdpServiceProvisioning != null) {
					fdpresponse = ServiceProvisioningUtil.executeServiceProvisioning(serviceProvisioningNode, fdpRequest, fdpServiceProvisioning);
				} else {
					FDPLogger.info(circleLogger, getClass(), "process()",
							LoggerUtil.getRequestAppender(fdpRequest) + "FDPRequest is not properly pushed in the Queue");
				}
			} catch (Exception e) {
				FDPLogger.info(circleLogger, getClass(), "process()",
						LoggerUtil.getRequestAppender(fdpRequest) + "Exception occurs while processing mobile money debit request:" + e);
			} finally {
				//Changes done to Send SMS to subscriber if failure occurs
					if (fdpresponse != null && fdpresponse.getExecutionStatus().getStatusText().equals(Status.FAILURE.getStatusText())) {
					String failureMessage = fdpresponse.getResponseString().get(0).getCurrDisplayText(DisplayArea.COMPLETE);
					NotificationUtil.sendNotification(fdpRequest.getSubscriberNumber(), ChannelType.SMS, fdpRequest.getCircle(),
							failureMessage, fdpRequest.getRequestId(), true);
				} 
			}
		}	
	}
}
