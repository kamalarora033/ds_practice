package com.ericsson.fdp.business.fulfillment.service.impl;

import java.util.List;

import javax.ejb.Stateless;

import ch.qos.logback.classic.Logger;

import com.ericsson.fdp.business.charging.ProductCharging;
import com.ericsson.fdp.business.charging.impl.AIRCharging;
import com.ericsson.fdp.business.charging.impl.FixedCharging;
import com.ericsson.fdp.business.charging.impl.VariableCharging;
import com.ericsson.fdp.business.charging.value.ChargingValue;
import com.ericsson.fdp.business.charging.value.ChargingValueImpl;
import com.ericsson.fdp.business.enums.RequestMetaValuesKey;
import com.ericsson.fdp.business.enums.ivr.FulfillmentResponseCodes;
import com.ericsson.fdp.business.enums.ivr.FulfillmentSystemTypes;
import com.ericsson.fdp.business.node.FDPServiceProvisioningNode;
import com.ericsson.fdp.business.product.Product;
import com.ericsson.fdp.business.request.impl.FDPMetadataResponseImpl;
import com.ericsson.fdp.business.request.impl.FulfillmentRequestImpl;
import com.ericsson.fdp.business.util.ChargingUtil;
import com.ericsson.fdp.business.util.DynamicMenuUtil;
import com.ericsson.fdp.business.util.LoggerUtil;
import com.ericsson.fdp.business.util.RequestUtil;
import com.ericsson.fdp.common.constants.FDPConstant;
import com.ericsson.fdp.common.enums.Status;
import com.ericsson.fdp.core.exception.ExecutionFailedException;
import com.ericsson.fdp.core.logging.FDPLogger;
import com.ericsson.fdp.core.node.FDPNode;
import com.ericsson.fdp.core.request.FDPRequest;
import com.ericsson.fdp.core.request.FDPResponse;
import com.ericsson.fdp.core.request.ResponseError;
import com.ericsson.fdp.core.request.impl.FDPRequestImpl;
import com.ericsson.fdp.dao.enums.ChargingType;

@Stateless
public class GetBundlePriceFulfillmentServiceImpl extends AbstractFDPFulFillmentServiceImpl {
	
	Logger logger = null;
	
	/**
	 * This method will create execution response for the request with action=BUNDLE_PRICE.  
	 */
	protected FDPResponse executeService(FDPRequest fdpRequest, Object... additionalInformations) throws ExecutionFailedException {
		logger = getCircleLogger(fdpRequest);
		FDPLogger.debug(logger, getClass(), "execute GetBundlePriceFulFillmentServiceImpl", LoggerUtil.getRequestAppender(fdpRequest)
				+ "Start executeService Called:" + fdpRequest.getRequestId());
		FDPMetadataResponseImpl fdpResponse = new FDPMetadataResponseImpl(Status.FAILURE, true, null);
		ChargingValue chargingValue = null;
		FulfillmentRequestImpl fulfillmentRequestImpl = (FulfillmentRequestImpl) fdpRequest;
        FDPNode fdpNode = DynamicMenuUtil.getAndUpdateFDPNode(fulfillmentRequestImpl.getCircle(), (fulfillmentRequestImpl.getIname() + FDPConstant.SPACE + fulfillmentRequestImpl.getRequestString()), fulfillmentRequestImpl);
        if(fdpNode==null){
        	return handleNodeNotFound(fulfillmentRequestImpl, fdpRequest);
        }
        FDPServiceProvisioningNode fdpSPNode = (FDPServiceProvisioningNode) fdpNode;
        Product product = RequestUtil.getProductById(fdpRequest, fdpSPNode.getEntityIdForCache(RequestMetaValuesKey.PRODUCT));
        final FDPRequestImpl requestImpl = (FDPRequestImpl) fdpRequest;
        if(null!=product){
        	chargingValue = getApplicableProductChargingForProvisioning(fdpRequest, product);
        	
        	requestImpl.addMetaValue(RequestMetaValuesKey.PRODUCT, product);
        	Long amountCharged=((AIRCharging)chargingValue.getChargingValue()).getChargingValue();
        	if(amountCharged!=null && amountCharged<0){
        	amountCharged =  (amountCharged * FDPConstant.MINUS_ONE_LONG);
        	}
        	Long amount=(ChargingUtil.getDiscountedChargingValue(fdpRequest, amountCharged));
        	((AIRCharging)chargingValue.getChargingValue()).setChargingValue(amount);
        	
        	
        	if (fdpRequest instanceof FDPRequestImpl) {
        		requestImpl.addMetaValue(RequestMetaValuesKey.CHARGING_STEP, chargingValue);
        	}
        	fdpResponse.setExecutionStatus(Status.SUCCESS);
        }else{
        	fdpResponse = handleProductNotFound(fdpResponse, requestImpl);
        	
        }
		return fdpResponse;
	}
	
	/**
	 * This method will set error response for product not found in cache.
	 * @param fdpResponse
	 * @param requestImpl
	 * @return
	 */
	private FDPMetadataResponseImpl handleProductNotFound(FDPMetadataResponseImpl fdpResponse,FDPRequestImpl requestImpl) {
		final String responseErrorString = "Product not found in cache.";
    	FulfillmentResponseCodes fulfillmentResponseCodes = FulfillmentResponseCodes.FDP_EXCEPTION;
		final String responseCode = String.valueOf(fulfillmentResponseCodes.getResponseCode());
		final String systemType = FulfillmentSystemTypes.CIS.getValue();
		final ResponseError responseError = new ResponseError(responseCode, responseErrorString, null, systemType);
		fdpResponse = new FDPMetadataResponseImpl(Status.FAILURE, true, null, responseError);
		String errorDescription = new StringBuilder(FDPConstant.REQUEST_ID)
				.append(FDPConstant.LOGGER_KEY_VALUE_DELIMITER).append(requestImpl.getRequestId())
				.append(FDPConstant.LOGGER_KEY_VALUE_DELIMITER).append("ERROR_CODE")
				.append(FDPConstant.LOGGER_KEY_VALUE_DELIMITER).append(responseCode.toString())
				.append(FDPConstant.LOGGER_KEY_VALUE_DELIMITER).append(responseErrorString).toString();
		FDPLogger.error(getCircleRequestLogger(requestImpl.getCircle()), getClass(), "handleNodeNotFound()",
				errorDescription);
    	fdpResponse.setResponseError(responseError);
		return fdpResponse;
	}


	/**
	 * This method will return a list of chargings that are applicable for a specific subscriber
	 * In case of variable charging, the chargings associated with satisfied condition is returned
	 * 
	 * @param fdpRequest
	 * @return
	 * @throws ExecutionFailedException
	 */
	public static ChargingValue getApplicableProductChargingForProvisioning(final FDPRequest fdpRequest, final Product product) throws ExecutionFailedException{
		ChargingValueImpl chargingValue = new ChargingValueImpl();;
		List<ProductCharging> applicableProductChargings = product.getProductCharging(fdpRequest.getChannel(), ChargingType.NORMAL);
		ChargingUtil.updateRequestForDefaultChargingFixed(fdpRequest, applicableProductChargings);
		for(ProductCharging productCharging : applicableProductChargings){
			if(productCharging instanceof VariableCharging){
				final VariableCharging variableCharging = (VariableCharging) productCharging;
				final VariableCharging newVariableCharging = new VariableCharging(variableCharging.getConditionStep(), variableCharging.getCommandDisplayName(), variableCharging.getExternalSystem());
				applicableProductChargings = newVariableCharging.getApplicableChargings(fdpRequest);
				break;
			}
		}
		for(ProductCharging productCharging : applicableProductChargings){
			chargingValue.setChargingValue(((FixedCharging)productCharging).getChargingValue());			
			break;
		}
		return chargingValue;
	}
}
