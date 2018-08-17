package com.ericsson.fdp.business.recurringservice;

import java.util.List;

import javax.ejb.Stateless;

import org.apache.commons.collections.CollectionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ericsson.fdp.business.fulfillment.service.impl.AbstractFDPFulFillmentServiceImpl;
import com.ericsson.fdp.business.util.RequestUtil;
import com.ericsson.fdp.common.enums.ChannelType;
import com.ericsson.fdp.core.exception.ExecutionFailedException;
import com.ericsson.fdp.core.request.FDPRequest;
import com.ericsson.fdp.core.request.FDPResponse;
import com.ericsson.fdp.dao.enums.FDPServiceProvSubType;

@Stateless
public class RSDeprovisioningForCCWebServiceImpl extends AbstractFDPFulFillmentServiceImpl implements DeprovisioningForCCWebService{
	
	/** The RSDeprovisioningForCCWebServiceImpl LOGGER. */
	private static final Logger LOGGER = LoggerFactory.getLogger(RSDeprovisioningForCCWebServiceImpl.class);

	@Override
	public String executeDeprovisioning(List<Long> productIds,String msisdn) throws ExecutionFailedException {
		LOGGER.info("Inside executeDeprovisioning of RSDeprovisioningForCCWebServiceImpl with productIds and msisdn {} and {} :",productIds,msisdn);
		FDPResponse responseOfExecution=null;
		FDPRequest fdpRequest=RequestUtil.createFDPRequest(msisdn,ChannelType.WEB);
		if(CollectionUtils.isNotEmpty(productIds)){
			for(Long productId: productIds){
				responseOfExecution=executeService(fdpRequest,productId);
			}
		}
		LOGGER.info("Exiting from executeDeprovisioning of RSDeprovisioningForCCWebServiceImpl with response",responseOfExecution);
		return responseOfExecution.getExecutionStatus().getStatusText();
	}

	@Override
	protected FDPResponse executeService(FDPRequest fdpRequest, Object... additionalInformations)
			throws ExecutionFailedException {
		Long productIdLong = null;
		FDPResponse fdpResponse=null;
		if(null !=additionalInformations){
			if(additionalInformations[0] instanceof Long){
				productIdLong=(Long) additionalInformations[0];
			}
		}
		updateSPInRequestForProductId(productIdLong, FDPServiceProvSubType.RS_DEPROVISION_PRODUCT,
				fdpRequest);
		fdpResponse = executeSP(fdpRequest);
		return fdpResponse;
	}

}
