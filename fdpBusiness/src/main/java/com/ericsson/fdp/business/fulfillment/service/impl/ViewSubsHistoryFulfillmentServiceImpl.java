package com.ericsson.fdp.business.fulfillment.service.impl;

import java.util.ArrayList;
import java.util.List;

import javax.ejb.Stateless;
import javax.inject.Inject;

import ch.qos.logback.classic.Logger;

import com.ericsson.fdp.business.command.param.impl.CommandParamOutput;
import com.ericsson.fdp.business.dto.ListProductDTO;
import com.ericsson.fdp.business.dto.ProductDTO;
import com.ericsson.fdp.business.enums.AuxRequestParam;
import com.ericsson.fdp.business.enums.Command;
import com.ericsson.fdp.business.enums.RequestMetaValuesKey;
import com.ericsson.fdp.business.enums.ivr.FulfillmentResponseCodes;
import com.ericsson.fdp.business.enums.ivr.FulfillmentSystemTypes;
import com.ericsson.fdp.business.product.Product;
import com.ericsson.fdp.business.request.impl.FDPMetadataResponseImpl;
import com.ericsson.fdp.business.step.execution.impl.rsdeprovision.RSDeprovisionServiceImpl;
import com.ericsson.fdp.business.util.LoggerUtil;
import com.ericsson.fdp.business.util.RequestUtil;
import com.ericsson.fdp.common.constants.FDPConstant;
import com.ericsson.fdp.common.enums.Status;
import com.ericsson.fdp.core.command.FDPCommand;
import com.ericsson.fdp.core.exception.ExecutionFailedException;
import com.ericsson.fdp.core.logging.FDPLogger;
import com.ericsson.fdp.core.request.FDPRequest;
import com.ericsson.fdp.core.request.FDPResponse;
import com.ericsson.fdp.core.request.ResponseError;
import com.ericsson.fdp.core.request.impl.FDPRequestImpl;
import com.ericsson.fdp.core.request.impl.FDPSMPPRequestImpl;

@Stateless
public class ViewSubsHistoryFulfillmentServiceImpl extends AbstractFDPFulFillmentServiceImpl {
	
	@Inject
	RSDeprovisionServiceImpl reDeprovisionServiceImpl;

	Logger logger = null;
	
	/**
	 * This method will create execution response for the request with action=BUNDLE_PRICE.  
	 */
	protected FDPResponse executeService(FDPRequest fdpRequest, Object... additionalInformations) throws ExecutionFailedException {
		logger = getCircleLogger(fdpRequest);
		FDPLogger.debug(logger, getClass(), "execute ViewSubsHistoryFulfillmentServiceImpl", LoggerUtil.getRequestAppender(fdpRequest)
				+ "Start executeService Called:" + fdpRequest.getRequestId());
		FDPMetadataResponseImpl fdpResponse = new FDPMetadataResponseImpl(Status.SUCCESS, true, null);
		
		((FDPSMPPRequestImpl)fdpRequest).putAuxiliaryRequestParameter(AuxRequestParam.distinctProduct, FDPConstant.TRUE);
		
		((FDPSMPPRequestImpl)fdpRequest).putAuxiliaryRequestParameter(AuxRequestParam.provisioningType, FDPConstant.PROVISIONING_TYPE_BOTH);
		final String commandName = FDPConstant.GET_SERVICE_DTLS_REQUEST_COMMAND;
		final FDPCommand fdpCommand = reDeprovisionServiceImpl.isCommandExecuted(fdpRequest, commandName);
		
		final FDPCommand fdpCommandGBAD = reDeprovisionServiceImpl.isCommandExecuted(fdpRequest, Command.GETACCOUNTDETAILS.getCommandDisplayName());
		String serviceClassCurrent= (null != fdpCommandGBAD)? fdpCommandGBAD.getOutputParam(FDPConstant.SERVICE_CLASS_CURRENT).getValue().toString() : null;
		
		List<ProductDTO> productDTOList = new ArrayList<ProductDTO>();
		final FDPRequestImpl requestImpl = (FDPRequestImpl) fdpRequest;
		if (fdpCommand != null) {
			int i = 0;
			boolean valueFound = false;
			
			String productIdFound = null;
			Product product = null;
			while (!valueFound) {
				final String usageValuePathProductId = "servicesDtls." + i + ".productId";
				final String usageValuePathactivationDate = "servicesDtls." + i + ".activationDate";
				final String usageValuePathprice = "servicesDtls." + i + ".price";
				final String usageValuePathrenewalDate = "servicesDtls." + i + ".renewalDate";
				final String usageValuePathactivatedBy = "servicesDtls." + i + ".activatedBy";
				final String usageValuePathpaySrc = "servicesDtls." + i + ".paySrc";
				final String usageValuePathsrcChannel = "servicesDtls." + i + ".srcChannel";
				final Object param = fdpCommand.getOutputParam(usageValuePathProductId);
				if (param == null) {
					valueFound = true;
				} else if (param instanceof CommandParamOutput) {
					productIdFound = ((CommandParamOutput) param).getValue().toString();
					product = RequestUtil.getProductById(fdpRequest, productIdFound);
					if(product==null){
						fdpResponse.setExecutionStatus(Status.FAILURE);
						break;
					} else {
					ProductDTO productDTO = new ProductDTO();
					productDTO.setProductId(productIdFound);
					productDTO.setProductName(product.getProductName());
					productDTO.setProductPrice(fdpCommand.getOutputParam(usageValuePathprice)==null?null:fdpCommand.getOutputParam(usageValuePathprice).getValue().toString());
					productDTO.setPaySrc(fdpCommand.getOutputParam(usageValuePathpaySrc)==null?null:fdpCommand.getOutputParam(usageValuePathpaySrc).getValue().toString());
					productDTO.setActivationDate(fdpCommand.getOutputParam(usageValuePathactivationDate)==null?null:fdpCommand.getOutputParam(usageValuePathactivationDate).getValue().toString());
					productDTO.setExpiryDate(fdpCommand.getOutputParam(usageValuePathrenewalDate)==null?null:fdpCommand.getOutputParam(usageValuePathrenewalDate).getValue().toString());
					productDTO.setSrcChannel(fdpCommand.getOutputParam(usageValuePathsrcChannel)==null?null:fdpCommand.getOutputParam(usageValuePathsrcChannel).getValue().toString());
					productDTO.setActivatedBy(fdpCommand.getOutputParam(usageValuePathactivatedBy)==null?null:fdpCommand.getOutputParam(usageValuePathactivatedBy).getValue().toString());
					productDTOList.add(productDTO);
					}
				}
				i++;
			}
		}
		
		ListProductDTO listProductDTO = new ListProductDTO();
		listProductDTO.setServiceClass(serviceClassCurrent);
		listProductDTO.setProductDTOList(productDTOList);
		requestImpl.addMetaValue(RequestMetaValuesKey.PRODUCTLIST, listProductDTO);
		if(fdpResponse.getExecutionStatus().equals(Status.FAILURE)) 
				fdpResponse = handleProductNotFound(fdpResponse, requestImpl);
		
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
}
