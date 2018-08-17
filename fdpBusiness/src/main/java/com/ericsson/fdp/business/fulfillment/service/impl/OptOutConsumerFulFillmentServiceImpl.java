package com.ericsson.fdp.business.fulfillment.service.impl;

import java.util.HashSet;
import java.util.Set;

import javax.ejb.Stateless;

import ch.qos.logback.classic.Logger;

import com.ericsson.fdp.FDPCacheable;
import com.ericsson.fdp.business.command.param.impl.CommandParamInput;
import com.ericsson.fdp.business.enums.Command;
import com.ericsson.fdp.business.enums.RequestMetaValuesKey;
import com.ericsson.fdp.business.node.FDPServiceProvisioningNode;
import com.ericsson.fdp.business.product.Product;
import com.ericsson.fdp.business.request.impl.FDPMetadataResponseImpl;
import com.ericsson.fdp.business.request.impl.FulfillmentRequestImpl;
import com.ericsson.fdp.business.serviceprovisioning.rule.ServiceProvisioningRule;
import com.ericsson.fdp.business.util.CommandUtil;
import com.ericsson.fdp.business.util.DynamicMenuUtil;
import com.ericsson.fdp.business.util.FulfillmentUtil;
import com.ericsson.fdp.business.util.LoggerUtil;
import com.ericsson.fdp.business.util.RequestUtil;
import com.ericsson.fdp.common.constants.FDPConstant;
import com.ericsson.fdp.common.enums.ModuleType;
import com.ericsson.fdp.common.enums.ParameterFeedType;
import com.ericsson.fdp.common.enums.Status;
import com.ericsson.fdp.core.command.FDPCommand;
import com.ericsson.fdp.core.exception.ExecutionFailedException;
import com.ericsson.fdp.core.logging.FDPLogger;
import com.ericsson.fdp.core.node.FDPNode;
import com.ericsson.fdp.core.request.FDPRequest;
import com.ericsson.fdp.core.request.FDPResponse;
import com.ericsson.fdp.core.serviceprovisioning.bean.FDPMetaBag;
import com.ericsson.fdp.core.utils.ApplicationConfigUtil;
import com.ericsson.fdp.dao.enums.FDPServiceProvSubType;
import com.ericsson.fdp.dao.enums.FDPServiceProvType;
import com.ericsson.fdp.dao.util.ServiceProvDataConverterUtil;

@Stateless
public class OptOutConsumerFulFillmentServiceImpl extends AbstractFDPFulFillmentServiceImpl {

	Logger logger = null;
	protected FDPResponse executeService(FDPRequest fdpRequest, Object... additionalInformations) throws ExecutionFailedException {
		logger = getCircleLogger(fdpRequest);
		FDPLogger.debug(logger, getClass(), "execute OptOutConsumerFulFillmentServiceImpl", LoggerUtil.getRequestAppender(fdpRequest)
				+ " Start executeService Called:" + fdpRequest.getRequestId());
		
        FDPMetadataResponseImpl fdpResponse = new FDPMetadataResponseImpl(Status.FAILURE, true, null);
        FulfillmentRequestImpl fulfillmentRequestImpl = (FulfillmentRequestImpl) fdpRequest;
        FDPNode fdpNode = DynamicMenuUtil.getAndUpdateFDPNode(fulfillmentRequestImpl.getCircle(), (fulfillmentRequestImpl.getIname() + FDPConstant.SPACE + fulfillmentRequestImpl.getRequestString()), fulfillmentRequestImpl);
        FDPServiceProvisioningNode fdpSPNode = (FDPServiceProvisioningNode) fdpNode;
        Product product = RequestUtil.getProductById(fdpRequest, fdpSPNode.getEntityIdForCache(RequestMetaValuesKey.PRODUCT));
        ServiceProvisioningRule sp = RequestUtil.getServiceProvisioningById(fdpRequest, 
        		ServiceProvDataConverterUtil.getKeyForServiceProvMetaBag(Long.valueOf(product.getProductId().toString()), FDPServiceProvType.PRODUCT, FDPServiceProvSubType.PRODUCT_BUY_RECURRING));
        fulfillmentRequestImpl.addMetaValue(RequestMetaValuesKey.PRODUCT, product);
        fulfillmentRequestImpl.addMetaValue(RequestMetaValuesKey.SERVICE_PROVISIONING, sp);
		if(isRequestValid(fdpRequest, fdpResponse)){
			if(doOptOut(fdpRequest, fdpResponse)){
			//	sp.execute(fdpRequest);
		        fdpResponse.setExecutionStatus(Status.SUCCESS);
		        fdpResponse.setFulfillmentResponse("Optout request successful");
			}
		}
		FDPLogger.debug(logger, getClass(), "execute OptOutConsumerFulFillmentServiceImpl", LoggerUtil.getRequestAppender(fdpRequest)
				+ "executeService Completed:" + fdpRequest.getRequestId());
		
		return fdpResponse;
	}

	/**
	 * This method hits the RS for optOut(Adhoc to Recurring)
	 * @param fdpRequest
	 * @param fdpResponse
	 * @throws ExecutionFailedException 
	 */
	private Boolean doOptOut(FDPRequest fdpRequest, FDPMetadataResponseImpl fdpResponse) throws ExecutionFailedException {
		return Status.SUCCESS.equals(CommandUtil.executeCommand(fdpRequest, Command.SPR, true));
	}

	/**
	 * This method validates if OptOUT request is a valid request
	 * 	1. Checks if the product is of type Adhoc
	 *  2. Checks if the product is subscribed by user
	 * @param fdpRequest
	 * @param fdpResponse 
	 * @return
	 * @throws ExecutionFailedException 
	 */
	private boolean isRequestValid(FDPRequest fdpRequest, FDPMetadataResponseImpl fdpResponse) throws ExecutionFailedException {
		return (isRecurring(fdpRequest, fdpResponse) && isAdhocProvAvailable(fdpRequest, fdpResponse) && isProductSubscribedByUser(fdpRequest, fdpResponse));
	}
	
	/**
	 * This method will return true if the requested product is of type Adhoc and return false otherwise
	 * @param fdpRequest
	 * @param fdpResponse
	 * @return
	 */
	private boolean isRecurring(FDPRequest fdpRequest, FDPMetadataResponseImpl fdpResponse) {
		boolean isRecur = false;
		FulfillmentRequestImpl fulfillmentRequestImpl = (FulfillmentRequestImpl) fdpRequest;
		String requestString = fulfillmentRequestImpl.getRequestString();
		if(null != requestString && requestString.contains(FDPConstant.RECURRING_PRODUCT_PREFIX)){
			isRecur = true;
		}else{
			fdpResponse.setFulfillmentResponse("Invalid Request. OptOut valid only for Recurring products");
		}
		return isRecur;
	}

	/**
	 * This method checks if the product has SP for Adhoc 
	 * @param fdpRequest
	 * @param fdpResponse
	 * @return
	 * @throws ExecutionFailedException
	 */
	private boolean isAdhocProvAvailable(FDPRequest fdpRequest, FDPMetadataResponseImpl fdpResponse) throws ExecutionFailedException {
		boolean isAdhocAvailable = false;
		final FDPCacheable serviceProvisioning = RequestUtil.getServiceProvisioningById(fdpRequest,
				ServiceProvDataConverterUtil.getKeyForServiceProvMetaBag(getProductId(fdpRequest), FDPServiceProvType.PRODUCT,
						FDPServiceProvSubType.PRODUCT_BUY));
		if (serviceProvisioning != null){
			isAdhocAvailable = true;
		}else {
			fdpResponse.setFulfillmentResponse("Invalid Request. No Adhoc provisioning available for this request");
		}
		return isAdhocAvailable;
	}

	/**
	 * This method will check on RS if the product is subscriber by user
	 * @param fdpRequest
	 * @return
	 * @throws ExecutionFailedException 
	 */
	private boolean isProductSubscribedByUser(FDPRequest fdpRequest, FDPMetadataResponseImpl fdpResponse) throws ExecutionFailedException {
		boolean isProductSubscribed = false;
		final FDPCacheable cachedCommand = ApplicationConfigUtil.getMetaDataCache().getValue(new FDPMetaBag(fdpRequest.getCircle(), ModuleType.COMMAND, Command.GET_SERVICES_DETAILS_REQUEST.getCommandDisplayName()));
		if (cachedCommand instanceof FDPCommand) {
			FDPCommand cmdToExecute = CommandUtil.getExectuableFDPCommand((FDPCommand) cachedCommand);
			FulfillmentUtil.updateValueInInputCmdObject(CommandParamInput.class, "commandParameterSource", cmdToExecute.getInputParam("provisioningType"), ParameterFeedType.INPUT);
			FulfillmentUtil.updateValueInInputCmdObject(CommandParamInput.class, FDPConstant.DEFINED_VALUE_TEXT, cmdToExecute.getInputParam("provisioningType"), "R");
			Status status = cmdToExecute.execute(fdpRequest);
			if(Status.SUCCESS.equals(status)){
				fdpRequest.addExecutedCommand(cmdToExecute);
				Set<String> subscribedProducts = getSubscribedProducts(fdpRequest.getExecutedCommand(Command.GET_SERVICES_DETAILS_REQUEST.getCommandName()));
				isProductSubscribed = subscribedProducts.contains(getProductId(fdpRequest).toString()) ? true : false;
				FDPLogger.info(logger, getClass(), " OptOutConsumerFulFillmentServiceImpl", "isProductSubscribedByUser listSize :"
				+ isProductSubscribed);
				if(!isProductSubscribed){
					fdpResponse.setFulfillmentResponse("Invalid Request. Product not already subscribed by user");
				}
			}
		}else{
			throw new ExecutionFailedException("Command : " + Command.GET_SERVICES_DETAILS_REQUEST.getCommandDisplayName() + " not found in cache.");
		}
		return isProductSubscribed;
	}

	/**
	 * This method extracts the list of products returned by RS command GetServicesDtlsRequest
	 * @param executedCommand
	 * @return
	 */
	private Set<String> getSubscribedProducts(final FDPCommand executedCommand) {
		String pathkey = null;
		final String servicesDtls = "servicesDtls";
		final String service = "service";
		final String productId  = "productId";
		final Set<String> subscribedProducts = new HashSet<String>();
		for(int i = 0; executedCommand.getOutputParam(pathkey = (servicesDtls + FDPConstant.PARAMETER_SEPARATOR + service + FDPConstant.PARAMETER_SEPARATOR
				+ i + FDPConstant.PARAMETER_SEPARATOR + productId)) != null; i++) {
			subscribedProducts.add(executedCommand.getOutputParam(pathkey).getValue().toString());
		}
		FDPLogger.info(logger, getClass(), " OptOutConsumerFulFillmentServiceImpl", "getSubscribedProducts listSize :"
				+ subscribedProducts.size());
		return subscribedProducts;
	}
	
	/**
	 * This method returns the product id
	 * @param fdpRequest
	 * @return
	 * @throws ExecutionFailedException
	 */
	private Long getProductId(final FDPRequest fdpRequest) throws ExecutionFailedException{
		final FDPCacheable cachedProduct = fdpRequest.getValueFromRequest(RequestMetaValuesKey.PRODUCT);
		if(null != cachedProduct && cachedProduct instanceof Product){
			Product product = (Product) cachedProduct;
			FDPLogger.info(logger, getClass(), " OptOutConsumerFulFillmentServiceImpl", "getProductId() Product ID :"
					+ product.getProductId());
			return product.getProductId();
		}
		throw new ExecutionFailedException("Product not found in fdpRequest");
	}
	
}

