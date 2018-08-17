package com.ericsson.fdp.business.fulfillment.service.impl;

import javax.ejb.Stateless;

import com.ericsson.fdp.business.enums.AuxRequestParam;
import com.ericsson.fdp.business.enums.ivr.FulfillmentParameters;
import com.ericsson.fdp.business.request.impl.FulfillmentRequestImpl;
import com.ericsson.fdp.business.util.LoggerUtil;
import com.ericsson.fdp.common.constants.FDPConstant;
import com.ericsson.fdp.common.util.StringUtil;
import com.ericsson.fdp.core.exception.ExecutionFailedException;
import com.ericsson.fdp.core.logging.FDPLogger;
import com.ericsson.fdp.core.request.FDPRequest;
import com.ericsson.fdp.core.request.FDPResponse;
import com.ericsson.fdp.core.request.impl.FDPSMPPRequestImpl;
import com.ericsson.fdp.dao.enums.FDPServiceProvSubType;

@Stateless
public class ProductDeProvisioningFulFillmentServiceImpl extends AbstractFDPFulFillmentServiceImpl {

	
	@Override
	protected FDPResponse executeService(FDPRequest fdpRequest,
			Object... additionalInformations) throws NumberFormatException, ExecutionFailedException {
		FDPResponse fdpResponse = null;
		FDPLogger.debug(  getCircleLogger(fdpRequest),
	 			getClass(),
	 				"execute Product Provisioning through Fulfillment",
	 				LoggerUtil.getRequestAppender(fdpRequest) + "Start executing Service Action:"
	 						+ fdpRequest.getRequestId());
		final FulfillmentRequestImpl fulfillmentRequestImpl = (FulfillmentRequestImpl) fdpRequest;
		FDPLogger.debug(  getCircleLogger(fdpRequest),
	 			getClass(),
	 				"execute Product Provisioning through Fulfillment",
	 				LoggerUtil.getRequestAppender(fdpRequest) + "Transaction_id is:"
	 						+ fulfillmentRequestImpl.getCommandInputParams(FulfillmentParameters.TRANSACTION_ID));
		final String productId=fulfillmentRequestImpl.getRequestString();
		if(StringUtil.isNullOrEmpty(productId)) {
			fdpResponse = handleNodeNotFound(fulfillmentRequestImpl,fdpRequest);
		} else {
			
			//Getting product Id from Product Code RS issue resolved by eagarsh.
			 Long productIdLong=0L;
			 String regex = "[0-9]";
			 if(productId.matches(regex)){
				 productIdLong = Long.valueOf(productId);
			 }else{
					String[] split = productId.split("_");
					int productLength = split.length;
					String splitProductId = split[productLength-1];
					productIdLong = Long.valueOf(splitProductId);
				
			 }
			//final Long productIdLong = Long.valueOf(productId);
				updateSPInRequestForProductId(productIdLong, FDPServiceProvSubType.RS_DEPROVISION_PRODUCT,	fulfillmentRequestImpl);
			if(fulfillmentRequestImpl instanceof FDPSMPPRequestImpl && !FDPConstant.CAPITAL_Y.equals(fulfillmentRequestImpl.getCommandInputParams(FulfillmentParameters.SKIP_RS_CHARING))){
				((FDPSMPPRequestImpl)fulfillmentRequestImpl).putAuxiliaryRequestParameter(AuxRequestParam.MC_LOAN_PURCHASE_INPUT, fulfillmentRequestImpl.getIname());
				
			}
			fdpResponse=executeSP(fulfillmentRequestImpl);
		}
		return fdpResponse;
	}
	
/*	private Object[] getServiceProvisioning( final String productId, final FDPRequest fdpRequest)
			throws NumberFormatException, ExecutionFailedException, EvaluationFailedException {
		Object[] obj = new Object[2];
		FDPCacheable[] fdpCacheAble = null;
		FDPServiceProvSubType serviceProv = null;
		
			serviceProv = ExternalSystemActionServiceProvisingMapping.BUY.getServiceProvtype();
			fdpCacheAble = ServiceProvisioningUtil.getProductAndSP(fdpRequest, Long.valueOf(productId), serviceProv);
		
		obj = new Object[] { serviceProv, fdpCacheAble[1] };
		return obj;
	}*/
	

}
