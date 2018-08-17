package com.ericsson.fdp.business.route.processor;

import javax.annotation.Resource;

import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ericsson.fdp.FDPCacheable;
import com.ericsson.fdp.business.command.impl.NonTransactionCommand;
import com.ericsson.fdp.business.command.param.impl.CommandParamInput;
import com.ericsson.fdp.business.enums.AuxRequestParam;
import com.ericsson.fdp.business.enums.Command;
import com.ericsson.fdp.business.enums.RequestMetaValuesKey;
import com.ericsson.fdp.business.enums.TLVOptions;
import com.ericsson.fdp.business.enums.ivr.MOBILEMONEYSTATUSENUM;
import com.ericsson.fdp.business.request.impl.FDPResponseImpl;
import com.ericsson.fdp.business.serviceprovisioning.impl.MMServiceProvisioningImpl;
import com.ericsson.fdp.business.serviceprovisioning.rule.ServiceProvisioningRule;
import com.ericsson.fdp.business.util.LoggerUtil;
import com.ericsson.fdp.business.util.RequestUtil;
import com.ericsson.fdp.business.util.ResponseUtil;
import com.ericsson.fdp.common.enums.ChannelType;
import com.ericsson.fdp.common.enums.ModuleType;
import com.ericsson.fdp.common.enums.Status;
import com.ericsson.fdp.core.command.param.CommandParam;
import com.ericsson.fdp.core.dsm.framework.MomoTransactionExpiredRequest;
import com.ericsson.fdp.core.logging.FDPLogger;
import com.ericsson.fdp.core.node.FDPNode;
import com.ericsson.fdp.core.request.FDPResponse;
import com.ericsson.fdp.core.request.FDPSMPPRequest;
import com.ericsson.fdp.core.request.ResponseError;
import com.ericsson.fdp.core.request.impl.FDPRequestImpl;
import com.ericsson.fdp.core.serviceprovisioning.bean.FDPMetaBag;
import com.ericsson.fdp.core.utils.ApplicationConfigUtil;

public class MomoExpiredTransactionProcessor implements Processor{
	
	@Resource(lookup = "java:app/fdpBusiness-1.0/MMServiceProvisioningImpl")
	private MMServiceProvisioningImpl fdpMMServiceProvisioning;
	
	private static final Logger LOGGER = LoggerFactory.getLogger(MomoExpiredTransactionProcessor.class);
	
	@Override
	public void process(Exchange exchange) throws Exception {
		
		FDPResponse fdpresponse = null;
		FDPSMPPRequest fdpRequest = null;
		Logger circleLogger = null;
		String response = null;
		
		try {
			if(null != exchange.getIn().getBody()){
				Object request = exchange.getIn().getBody();
				MomoTransactionExpiredRequest momoRequest = (MomoTransactionExpiredRequest) request;
				fdpRequest = (FDPSMPPRequest) momoRequest.getFdpCacheable();
				
				fdpresponse = new FDPResponseImpl(Status.FAILURE, false, ResponseUtil.createResponseMessageInList(
						fdpRequest.getChannel(), "MM", TLVOptions.SESSION_TERMINATE), 
						new ResponseError(MOBILEMONEYSTATUSENUM.GETTRANSACTION.getValue().toString(), MOBILEMONEYSTATUSENUM.GETTRANSACTION.getKey(), "MM Authorization Pending of Failed", "MM"));
				
				circleLogger = LoggerUtil.getSummaryLoggerFromRequest(fdpRequest);
				LOGGER.debug("Mobile Money Expired transaction being processed (RID:" + fdpRequest.getRequestId() + ")");
				final FDPCacheable cachedCommand = ApplicationConfigUtil.getMetaDataCache().getValue(
						new FDPMetaBag(fdpRequest.getCircle(), ModuleType.COMMAND, Command.GET_TRANSACTION_STATUS.getCommandDisplayName()));
				 if (cachedCommand instanceof NonTransactionCommand) {
					 NonTransactionCommand fdpCmdToExecute = (NonTransactionCommand) cachedCommand;
	                 for (final CommandParam commandParam : fdpCmdToExecute.getInputParam()) {
	         			if (commandParam instanceof CommandParamInput) {
	         				final CommandParamInput input = (CommandParamInput) commandParam;
	         					input.evaluateValue(fdpRequest);
	         			}
	                 }
	         			CommandParamInput commandParamInput = (CommandParamInput) fdpCmdToExecute.getInputParam("referenceid");
	         			 commandParamInput.setValue(fdpRequest.getOriginTransactionID());
	                 Status status = fdpCmdToExecute.executeCommand(fdpRequest);
	                 if(Status.SUCCESS.equals(status)){
	                	 ((FDPRequestImpl) fdpRequest).putAuxiliaryRequestParameter(AuxRequestParam.SKIP_CHARGING, true);
	                	 fdpRequest.addExecutedCommand(fdpCmdToExecute);
	                	 final FDPCacheable serviceProvisioningObject = fdpRequest.getValueFromRequest(RequestMetaValuesKey.SERVICE_PROVISIONING);
		                 if(serviceProvisioningObject instanceof ServiceProvisioningRule) {
		                	 ServiceProvisioningRule serviceProvisioningRule = (ServiceProvisioningRule) serviceProvisioningObject;
		                	 fdpresponse = serviceProvisioningRule.execute(fdpRequest);	
		     				 LOGGER.debug("Mobile Money Expired transaction processed successfully (RID:" + fdpRequest.getRequestId() + ")");
		             	 }
	                 }else{
	     				 LOGGER.error("Mobile Money Expired transaction processing failed (RID:" + fdpRequest.getRequestId() + ")");
	                 }
				 }else{
	 				 LOGGER.error("Mobile Money Expired transaction processing failed (RID:" + fdpRequest.getRequestId() + ")");
				 }
			}
		
			
		} catch (Exception e) {
			LOGGER.error("Exception occurs while procesing the Mobile Money Expired or FAILED transaction:" + e);
		} finally {
			//Changes done to write transaction status in log
			response = RequestUtil.writeProvRslt(fdpRequest, fdpresponse, circleLogger);
			FDPLogger.info(circleLogger, getClass(), "executeServiceProvisioning()", response);
			FDPNode fdpNode = null;
			if (ChannelType.USSD.equals(fdpRequest.getChannel())) {
				fdpNode = fdpRequest.getNode(fdpRequest.getLastServedString());
				
			}
			LoggerUtil.generatePostLogsForUserBehaviour(fdpRequest, fdpNode, false); 
		}
		
	}
}
