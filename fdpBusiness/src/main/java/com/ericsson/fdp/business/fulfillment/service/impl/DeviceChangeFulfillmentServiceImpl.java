package com.ericsson.fdp.business.fulfillment.service.impl;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import javax.annotation.Resource;
import javax.ejb.Stateless;
import com.ericsson.fdp.business.enums.AuxRequestParam;
import com.ericsson.fdp.business.enums.Command;
import com.ericsson.fdp.business.enums.RequestMetaValuesKey;
import com.ericsson.fdp.business.product.Product;
import com.ericsson.fdp.business.request.impl.FDPMetadataResponseImpl;
import com.ericsson.fdp.business.request.impl.FulfillmentRequestImpl;
import com.ericsson.fdp.business.serviceprovisioning.ServiceProvisioning;
import com.ericsson.fdp.business.serviceprovisioning.rule.ServiceProvisioningRule;
import com.ericsson.fdp.business.util.CommandUtil;
import com.ericsson.fdp.business.util.LoggerUtil;
import com.ericsson.fdp.business.util.ProductAttributeCacheUtil;
import com.ericsson.fdp.business.util.RequestUtil;
import com.ericsson.fdp.business.vo.ProductAttributeMapCacheDTO;
import com.ericsson.fdp.common.constants.FDPConstant;
import com.ericsson.fdp.common.enums.Status;
import com.ericsson.fdp.core.command.param.CommandParam;
import com.ericsson.fdp.core.exception.ExecutionFailedException;
import com.ericsson.fdp.core.logging.FDPLogger;
import com.ericsson.fdp.core.request.FDPRequest;
import com.ericsson.fdp.core.request.FDPResponse;
import com.ericsson.fdp.core.request.impl.FDPRequestImpl;
import com.ericsson.fdp.dao.enums.FDPServiceProvSubType;
import com.ericsson.fdp.dao.enums.FDPServiceProvType;
import com.ericsson.fdp.dao.util.ServiceProvDataConverterUtil;

@Stateless
public class DeviceChangeFulfillmentServiceImpl extends AbstractFDPFulFillmentServiceImpl{

	
	@Resource(lookup = "java:app/fdpBusiness-1.0/FulfillmentServiceProvisioningImpl")
	private ServiceProvisioning fdpFulfillmentServiceProvisioning;
	
	@Override
	protected FDPResponse executeService(FDPRequest fdpRequest,	Object... additionalInformations) throws ExecutionFailedException {
		FDPLogger.debug(circleLogger, getClass(), "execute DeviceChangeFulfillmentServiceImpl", LoggerUtil.getRequestAppender(fdpRequest)
				+ "Start executeService Called:" + fdpRequest.getRequestId());
		FDPMetadataResponseImpl fdpResponse = new FDPMetadataResponseImpl(Status.FAILURE, true, null);
        try {
    			if(deleteOfferFromOldDevice(fdpRequest, fdpResponse)){
    		        fdpResponse.setExecutionStatus(Status.SUCCESS);
    		        fdpResponse.setFulfillmentResponse("Device Change request completed successfully");
    			}
    
    		FDPLogger.debug(circleLogger, getClass(), "execute DeviceChangeFulfillmentServiceImpl", LoggerUtil.getRequestAppender(fdpRequest)
    				+ "executeService Completed:" + fdpRequest.getRequestId());	
		
		} catch (ExecutionFailedException e) {
			FDPLogger.error(circleLogger, getClass(), " DeviceChangeFulfillmentServiceImpl", "executeService Command execution failed:", e);
			fdpResponse.setFulfillmentResponse("Some exception occured, unable to process the Handset Change request");
		}
		return fdpResponse;
	}
	
	private boolean deleteOfferFromOldDevice(FDPRequest fdpRequest, FDPResponse response)throws ExecutionFailedException {
		FulfillmentRequestImpl fulfillmentRequestImpl = (FulfillmentRequestImpl) fdpRequest;
		//RequestUtil.updateHandsetBasedParametersInRequest(fulfillmentRequestImpl);
		Status status= CommandUtil.executeCommand(fdpRequest, Command.GET_OFFERS, true);
    	if (!status.equals(Status.SUCCESS)) {
			throw new ExecutionFailedException(Command.GET_OFFERS.getCommandDisplayName() + " could not be executed");
		}
		
		Map<String,CommandParam> commandParamMap= fdpRequest.getExecutedCommand(Command.GET_OFFERS.getCommandDisplayName()).getOutputParams();
    	List <String> offerIdList= new ArrayList<String>();
    	int i=0;
    	String paramName=  "offerinformation"+FDPConstant.PARAMETER_SEPARATOR+i+FDPConstant.PARAMETER_SEPARATOR+"offerid";
    	while(commandParamMap.get(paramName)!=null){
    		offerIdList.add(commandParamMap.get(paramName).getValue().toString());
    		i++;
    		paramName = "offerinformation" + FDPConstant.PARAMETER_SEPARATOR + i + FDPConstant.PARAMETER_SEPARATOR + "offerid";
    	}
		
    	ProductAttributeMapCacheDTO fdpCacheableObject=	ProductAttributeCacheUtil.getOfferIdByDeviceTypeAndImei(fdpRequest, circleLogger);
    	
    	if(fdpCacheableObject == null){
    		FDPLogger.debug(circleLogger, getClass(), "deleteOfferFromOldDevice()",	"Data not found in Product Attribute Cache");
    		return false;
    	}
    	fulfillmentRequestImpl.putAuxiliaryRequestParameter(AuxRequestParam.SKIP_CHARGING, Boolean.TRUE);
    	// Setting this AUX Param will allow same command step to be executed multiple times
    	fulfillmentRequestImpl.putAuxiliaryRequestParameter(AuxRequestParam.FAF_EXECUTE_COMMAND_AGAIN, "true");
    	Product product = null;
    	for(Map.Entry<Long, Map<String, String>> entry : fdpCacheableObject.getValueMap().entrySet())
    	{
    		Map<String, String> valueMap = entry.getValue();
    		if (offerIdList.contains(valueMap.get(FDPConstant.PARAMETER_OFFER_ID)))
    		{
				fulfillmentRequestImpl.putAuxiliaryRequestParameter(AuxRequestParam.OFFER_ID_VAL, valueMap.get(FDPConstant.PARAMETER_OFFER_ID));
				product = RequestUtil.getProductById(fdpRequest, valueMap.get(FDPConstant.PARAMETER_PRODUCT_ID));
				fulfillmentRequestImpl.addMetaValue(RequestMetaValuesKey.PRODUCT, product);
				if((product = (Product)fdpRequest.getValueFromRequest(RequestMetaValuesKey.PRODUCT)) != null) {
			        ServiceProvisioningRule sp = RequestUtil.getServiceProvisioningById(fdpRequest, 
			        		ServiceProvDataConverterUtil.getKeyForServiceProvMetaBag(product.getProductId(), FDPServiceProvType.PRODUCT, FDPServiceProvSubType.RS_DEPROVISION_PRODUCT));
			        fulfillmentRequestImpl.addMetaValue(RequestMetaValuesKey.SERVICE_PROVISIONING, sp);
		        } else if(valueMap.get(FDPConstant.PARAMETER_PRODUCT_ID) == null) {
		        	throw new ExecutionFailedException("Product id: " + valueMap.get(FDPConstant.PARAMETER_PRODUCT_ID) + " not configured in Product Attribute Cache");
		        } else {
		        	throw new ExecutionFailedException("Product id: " + valueMap.get(FDPConstant.PARAMETER_PRODUCT_ID) + " not found in MetaCache");
		        }
				response=	executeSP((FDPRequestImpl)fdpRequest);	
				if (!(Status.SUCCESS.equals(response.getExecutionStatus()))) {
					throw new ExecutionFailedException("Failed to execute Deprovision SP, couldn't deprovision the offer/product from MSISDN " + fdpRequest.getSubscriberNumber());
				}
    		}
    	}
		return true;
	}
}
