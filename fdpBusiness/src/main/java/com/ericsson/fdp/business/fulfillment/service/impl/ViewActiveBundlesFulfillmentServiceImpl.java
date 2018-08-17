package com.ericsson.fdp.business.fulfillment.service.impl;

import javax.ejb.Stateless;

import com.ericsson.fdp.business.enums.Command;
import com.ericsson.fdp.business.request.impl.FDPMetadataResponseImpl;
import com.ericsson.fdp.business.util.CommandUtil;
import com.ericsson.fdp.business.util.LoggerUtil;
import com.ericsson.fdp.common.enums.Status;
import com.ericsson.fdp.core.exception.ExecutionFailedException;
import com.ericsson.fdp.core.logging.FDPLogger;
import com.ericsson.fdp.core.request.FDPRequest;
import com.ericsson.fdp.core.request.FDPResponse;
/**
 * This is View Active bundles based on msisdn fulfillemnt service impl class
 * @author GUR36857
 *
 */
@Stateless
public class ViewActiveBundlesFulfillmentServiceImpl extends AbstractFDPFulFillmentServiceImpl {

	@Override
	protected FDPResponse executeService(FDPRequest fdpRequest, Object... additionalInformations)
			throws ExecutionFailedException {
		FDPLogger.debug(circleLogger, getClass(), "execute ViewActiveBundlesFulfillmentServiceImpl", LoggerUtil.getRequestAppender(fdpRequest)
				+ "Start executeService Called:" + fdpRequest.getRequestId());	
		FDPResponse fdpResponse ;
		try{
			CommandUtil.executeCommand(fdpRequest, Command.GET_ACTIVE_BUNDLES_DETAILS_REQUEST, true);
			fdpResponse = new FDPMetadataResponseImpl(Status.SUCCESS, true, null,fdpRequest.getLastExecutedCommand().getResponseError());
		}
		catch(ExecutionFailedException e){
			FDPLogger.error(circleLogger, getClass(), "execute ViewActiveBundlesFulfillmentServiceImpl", LoggerUtil.getRequestAppender(fdpRequest)
					+ "error gettting view active bundle:" +e);
			fdpResponse =  new FDPMetadataResponseImpl(Status.FAILURE,true,null);
			if(fdpRequest.getLastExecutedCommand()!=null)
				fdpResponse =  new FDPMetadataResponseImpl(Status.FAILURE,true,null,fdpRequest.getLastExecutedCommand().getResponseError());
			
		}		 
		
		return fdpResponse;
	}

}
