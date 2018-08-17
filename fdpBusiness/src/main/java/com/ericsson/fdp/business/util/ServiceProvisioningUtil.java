package com.ericsson.fdp.business.util;

import ch.qos.logback.classic.Logger;

import com.ericsson.fdp.FDPCacheable;
import com.ericsson.fdp.business.constants.FDPStepResponseConstants;
import com.ericsson.fdp.business.decorator.FDPDecorator;
import com.ericsson.fdp.business.decorator.impl.BaseResponseDecorator;
import com.ericsson.fdp.business.decorator.impl.FooterDecorator;
import com.ericsson.fdp.business.decorator.impl.PaginationDecorator;
import com.ericsson.fdp.business.enums.AuxRequestParam;
import com.ericsson.fdp.business.enums.ErrorTypes;
import com.ericsson.fdp.business.enums.RequestMetaValuesKey;
import com.ericsson.fdp.business.enums.SPServiceJNDILookupPath;
import com.ericsson.fdp.business.enums.TLVOptions;
import com.ericsson.fdp.business.exception.StepException;
import com.ericsson.fdp.business.node.FDPCustomNode;
import com.ericsson.fdp.business.node.FDPServiceProvisioningNode;
import com.ericsson.fdp.business.node.impl.ProductNode;
import com.ericsson.fdp.business.node.impl.SpecialMenuNode;
import com.ericsson.fdp.business.request.impl.FDPResponseImpl;
import com.ericsson.fdp.business.serviceprovisioning.ServiceProvisioning;
import com.ericsson.fdp.business.serviceprovisioning.rule.ServiceProvisioningRule;
import com.ericsson.fdp.business.step.FDPStep;
import com.ericsson.fdp.business.step.impl.ServiceStep;
import com.ericsson.fdp.common.constants.FDPConstant;
import com.ericsson.fdp.common.enums.ChannelType;
import com.ericsson.fdp.common.enums.ModuleType;
import com.ericsson.fdp.core.display.DisplayObject;
import com.ericsson.fdp.core.exception.EvaluationFailedException;
import com.ericsson.fdp.core.exception.ExecutionFailedException;
import com.ericsson.fdp.core.logging.FDPLogger;
import com.ericsson.fdp.core.node.FDPNode;
import com.ericsson.fdp.core.request.FDPRequest;
import com.ericsson.fdp.core.request.FDPResponse;
import com.ericsson.fdp.core.request.FDPSMPPRequest;
import com.ericsson.fdp.core.request.FDPStepResponse;
import com.ericsson.fdp.core.request.impl.FDPRequestImpl;
import com.ericsson.fdp.core.serviceprovisioning.bean.FDPMetaBag;
import com.ericsson.fdp.core.utils.ApplicationConfigUtil;
import com.ericsson.fdp.dao.dto.serviceprov.AbstractServiceProvDTO;
import com.ericsson.fdp.dao.enums.FDPServiceProvSubType;
import com.ericsson.fdp.dao.enums.FDPServiceProvType;
import com.ericsson.fdp.dao.util.ServiceProvDataConverterUtil;

/*
Feature Name: User can purchase bundle for self and others
Changes: New function 'updateSubscriberInRequestForBeneficiary()' created
Date: 28-10-2015
Singnum Id:ESIASAN
*/

public class ServiceProvisioningUtil {

	/**
	 * This method is used to execute the service provisioning.
	 * 
	 * @param serviceProvisioningNode
	 *            The service provisioning node which is to be executed.
	 * @param fdpussdsmscRequest
	 *            The request object.
	 * @return The response.
	 * @throws ExecutionFailedException
	 *             Exception, if execution fails.
	 * @throws EvaluationFailedException
	 *             Exception, if the object was not found in cache.
	 */
	public static FDPResponse executeServiceProvisioning(final FDPServiceProvisioningNode serviceProvisioningNode,
			final FDPRequest fdpRequest, final ServiceProvisioning fdpServiceProvisioning)
			throws ExecutionFailedException, EvaluationFailedException {
		FDPCacheable serviceProvisioning = null;
		FDPCacheable product = null;
		if (serviceProvisioningNode instanceof ProductNode) {
			FDPCacheable[] cached = getProductAndSP(fdpRequest, serviceProvisioningNode);
			product = cached[0];
			serviceProvisioning = cached[1];
		} else if (serviceProvisioningNode instanceof FDPCustomNode) {
			serviceProvisioning = ((FDPCustomNode) serviceProvisioningNode).getServiceProvisioning(fdpRequest);
		} else if (serviceProvisioningNode instanceof SpecialMenuNode) {
			final SpecialMenuNode specialMenuNode = (SpecialMenuNode) serviceProvisioningNode;
			// TODO: check special menu node. If it is of shared account ---
			// write to EJB.
			final String entityId = specialMenuNode.getEntityIdForCache(RequestMetaValuesKey.SERVICE_PROVISIONING);
			FDPLogger.debug(LoggerUtil.getSummaryLoggerFromRequest(fdpRequest), ServiceProvisioningUtil.class,
					"executeServiceProvisioning()", LoggerUtil.getRequestAppender(fdpRequest)
							+ "Service provisioning other case. SP id :- " + entityId);
			serviceProvisioning = ApplicationConfigUtil.getMetaDataCache().getValue(
					new FDPMetaBag(fdpRequest.getCircle(), ModuleType.SP_OTHERS, entityId));
		} else {
			FDPLogger.error(LoggerUtil.getSummaryLoggerFromRequest(fdpRequest), ServiceProvisioningUtil.class,
					"executeServiceProvisioning()", LoggerUtil.getRequestAppender(fdpRequest)
							+ "The service provisioning could not be identified");
			throw new ExecutionFailedException("The service provisioning could not be identified");
		}
		RequestUtil.updateProductInRequest(fdpRequest, serviceProvisioning, product);
		FDPResponse fdpResponse = null;
		if (serviceProvisioning != null) {
			fdpResponse = fdpServiceProvisioning.executeServiceProvisioning(fdpRequest);
		}
		
		// This code is to set skip charging flag back to false if multiple product buy done in same
		// USSD session
		Object skipCharging = fdpRequest.getAuxiliaryRequestParameter(AuxRequestParam.SKIP_CHARGING);
		if(null != skipCharging && skipCharging instanceof Boolean && Boolean.TRUE.equals((Boolean) skipCharging)){
			((FDPRequestImpl) fdpRequest).putAuxiliaryRequestParameter(AuxRequestParam.SKIP_CHARGING, Boolean.FALSE);
		}
		return fdpResponse;
	}

	public static FDPCacheable getServiceProvisioning(final FDPRequest fdpRequest, final Long productId,
			final FDPServiceProvSubType serviceProvType) throws ExecutionFailedException {

		FDPCacheable serviceProvisioning = null;
		FDPCacheable product = null;
		try {
			FDPCacheable[] cached = getProductAndSP(fdpRequest, productId, serviceProvType);
			product = cached[0];
			serviceProvisioning = cached[1];
		} catch (EvaluationFailedException e) {
			throw new ExecutionFailedException("Could not evaluate service provisioning", e);
		}
		RequestUtil.updateProductInRequest(fdpRequest, serviceProvisioning, product);
		return serviceProvisioning;

	}

	public static FDPCacheable[] getProductAndSP(FDPRequest fdpRequest, Long entityId,
			final FDPServiceProvSubType serviceProvType) throws ExecutionFailedException, EvaluationFailedException {

		return getProductAndSp(fdpRequest, entityId.toString(),
				ServiceProvDataConverterUtil.getKeyForServiceProvMetaBag(entityId, FDPServiceProvType.PRODUCT,
						serviceProvType));

	}

	private static FDPCacheable[] getProductAndSp(FDPRequest fdpRequest, String productEntityId,
			String serviceProvEntityId) throws EvaluationFailedException, ExecutionFailedException {
		FDPCacheable[] result = new FDPCacheable[2];
		if (fdpRequest instanceof FDPSMPPRequest) {
			final FDPSMPPRequest fdpsmppRequest = (FDPSMPPRequest) fdpRequest;
			FDPCacheable product = fdpsmppRequest.getMetaObject(RequestMetaValuesKey.PRODUCT, productEntityId);
			if (product == null) {
				product = RequestUtil.updateProductAndSPInRequest(fdpRequest, productEntityId, serviceProvEntityId);
			}
			final FDPCacheable serviceProvisioning = fdpsmppRequest.getMetaObject(
					RequestMetaValuesKey.SERVICE_PROVISIONING, serviceProvEntityId);
			FDPLogger.debug(LoggerUtil.getSummaryLoggerFromRequest(fdpRequest), ServiceProvisioningUtil.class,
					"executeServiceProvisioning()", LoggerUtil.getRequestAppender(fdpRequest)
							+ "Product node case. Product id " + productEntityId);
			result = new FDPCacheable[] { product, serviceProvisioning };
		} else {
			final FDPCacheable product = RequestUtil.getProductById(fdpRequest, productEntityId);
			final FDPCacheable serviceProvisioning = RequestUtil.getServiceProvisioningById(fdpRequest,
					serviceProvEntityId);
			if (fdpRequest instanceof FDPRequestImpl) {
				FDPRequestImpl fdpRequestImpl = (FDPRequestImpl) fdpRequest;
				fdpRequestImpl.addMetaValue(RequestMetaValuesKey.PRODUCT, product);
				fdpRequestImpl.addMetaValue(RequestMetaValuesKey.SERVICE_PROVISIONING, serviceProvisioning);
			}
			FDPLogger.debug(LoggerUtil.getSummaryLoggerFromRequest(fdpRequest), ServiceProvisioningUtil.class,
					"executeServiceProvisioning()", LoggerUtil.getRequestAppender(fdpRequest)
							+ "Product node case. Product id " + productEntityId);
			result = new FDPCacheable[] { product, serviceProvisioning };

		}
		return result;
	}

	public static FDPResponse decorate(FDPResponse fdpResponse, FDPNode serviceProvisioningNode, FDPRequest fdpRequest)
			throws ExecutionFailedException {
		final FDPDecorator baseDecorator = new BaseResponseDecorator(fdpResponse.getResponseString().get(0),
				serviceProvisioningNode);
		FDPDecorator decorator = new FooterDecorator(baseDecorator, serviceProvisioningNode, fdpRequest.getCircle(),
				fdpRequest.getChannel());
		if (ChannelType.USSD.equals(fdpRequest.getChannel())) {
			decorator = new PaginationDecorator(decorator, FDPConstant.SPACE, fdpRequest.getChannel(),
					fdpRequest.getCircle());
		}
		DisplayObject displayObject = decorator.display();
		//FDPResponseImpl fdpResponseUpdated = new FDPResponseImpl(fdpResponse.getExecutionStatus(), false, null);
		FDPResponseImpl fdpResponseUpdated = new FDPResponseImpl(fdpResponse.getExecutionStatus(), false, fdpResponse.getSystemType(),null);
		RequestUtil.updateDisplayObjectInRequest(fdpRequest, displayObject);
		RequestUtil.addObjectsToRequest(fdpResponseUpdated, null, displayObject, fdpRequest);
		if (fdpResponseUpdated.getResponseString() != null
				&& !fdpResponseUpdated.getResponseString().isEmpty()
				&& fdpResponseUpdated.getResponseString().get(fdpResponseUpdated.getResponseString().size() - 1)
						.getTLVOptions().contains(TLVOptions.SESSION_TERMINATE)) {
			fdpResponseUpdated.setTerminateSession(true);
			//for menu redirect comviva start
			if(null!=fdpResponse.getResponseError() && ErrorTypes.MENU_REDIRECT.toString().equals(fdpResponse.getResponseError().getErrorType()))
			{
				fdpResponseUpdated.setSystemType(FDPConstant.COMVIVAUSSD.toString());
			
				/*fdpRequest.getValueFromStep("COMVIVA_MENU_REDIRECT", FDPStepResponseConstants.ERROR_VALUE);
				fdpRequest.getValueFromStep("COMVIVA_MENU_REDIRECT", FDPStepResponseConstants.FREE_FLOW_STATE);
				fdpRequest.getValueFromStep("COMVIVA_MENU_REDIRECT", FDPStepResponseConstants.FREE_FLOW_CHARGING);
				fdpRequest.getValueFromStep("COMVIVA_MENU_REDIRECT", FDPStepResponseConstants.FREE_FLOW_CHARGING_AMOUNT);*/
				
				
			}
			//for menu redirect comviva stop
			
		}
		return fdpResponseUpdated;
	}

	// this method should be private. 
	public static FDPCacheable[] getProductAndSP(final FDPRequest fdpRequest,
			final FDPServiceProvisioningNode serviceProvisioningNode) throws EvaluationFailedException,
			ExecutionFailedException {

		return getProductAndSp(fdpRequest, serviceProvisioningNode.getEntityIdForCache(RequestMetaValuesKey.PRODUCT),
				serviceProvisioningNode.getEntityIdForCache(RequestMetaValuesKey.SERVICE_PROVISIONING));

	}
	
	/**
	 * This method will updates the Beneficiary number in request.
	 * (NOTE:- For Charging and Notification steps, do not update beneficiary, they will be requestor msisdn only.)
	 * 
	 * @param fdpRequest
	 * @param toUdpateBeneficiary
	 */
	public static void updateSubscriberInRequestForBeneficiary(final FDPRequest fdpRequest, final boolean toUdpateBeneficiary, final Logger logger) {
		if(toUdpateBeneficiary) {
			((FDPRequestImpl)fdpRequest).putAuxiliaryRequestParameter(AuxRequestParam.REQUESTOR_MSISDN, fdpRequest.getSubscriberNumber());;
			Object beneficiaryMsisdnObject = fdpRequest.getAuxiliaryRequestParameter(AuxRequestParam.BENEFICIARY_MSISDN);
			if(null != beneficiaryMsisdnObject){
				FDPLogger.debug(logger, ServiceProvisioningUtil.class, "updateSubscriberInRequestForBeneficiary", "Updating Subscriber Number in request from "
						+ fdpRequest.getSubscriberNumber() + " to " + beneficiaryMsisdnObject.toString());
				((FDPRequestImpl)fdpRequest).setSubscriberNumber(Long.valueOf(beneficiaryMsisdnObject.toString()));
			}
		} else {
			if(fdpRequest.getAuxiliaryRequestParameter(AuxRequestParam.REQUESTOR_MSISDN) != null){
				((FDPRequestImpl)fdpRequest).setSubscriberNumber((Long)fdpRequest.getAuxiliaryRequestParameter(AuxRequestParam.REQUESTOR_MSISDN));
			}
		}
	}
	
	/**
	 * This method check if requested node is of type specified.
	 * 
	 * @param fdpRequest
	 * @return
	 */
	public static boolean isProductSpTypeValid(FDPRequest fdpRequest, FDPServiceProvSubType spProvSubTypeToMatch) {
		boolean isValid = false;
		try{
			if(fdpRequest.getValueFromRequest(RequestMetaValuesKey.NODE) instanceof FDPNode && spProvSubTypeToMatch != null) {
				final FDPNode fdpNode = (FDPNode)fdpRequest.getValueFromRequest(RequestMetaValuesKey.NODE);
				if(fdpNode instanceof ProductNode) {
					final ProductNode productNode = (ProductNode) fdpNode;
					isValid = spProvSubTypeToMatch.equals(productNode.getServiceProvSubType());
				}
			}
		}catch(Exception e){
			e.printStackTrace();
		}
		return isValid;
	}
	
	/**
	 * This method return the service provisioning type of fdpRequest
	 * @param fdpRequest
	 * @return
	 */
	public static FDPServiceProvSubType getFDPServiceProvSubType(FDPRequest fdpRequest){
		FDPServiceProvSubType serviceProvSubType = null;
		if(fdpRequest.getValueFromRequest(RequestMetaValuesKey.NODE) instanceof FDPNode) {
			final FDPNode fdpNode = (FDPNode)fdpRequest.getValueFromRequest(RequestMetaValuesKey.NODE);
			if(fdpNode instanceof ProductNode) {
				final ProductNode productNode = (ProductNode) fdpNode;
				serviceProvSubType = productNode.getServiceProvSubType();
			}
		}
		return serviceProvSubType;
	}

	/**
	 * This  method update the response as per step response.
	 * 
	 * @param fdpResponseImpl
	 * @param fdpStep
	 * @param fdpStepResponse
	 * @throws StepException
	 */
	public static void decorateSPResponse(final FDPResponseImpl fdpResponseImpl, final FDPStep fdpStep, final FDPStepResponse fdpStepResponse) throws StepException{
		if(null == fdpResponseImpl.getSystemType() && fdpStep instanceof ServiceStep && SPServiceJNDILookupPath.COMVIVA_MENU_REDIRECT.getJndiLookupPath().equals(((ServiceStep)fdpStep).getJndiLookupName())) {
			fdpResponseImpl.setSystemType(fdpStepResponse.getStepResponseValue(FDPStepResponseConstants.EXTERNAL_SYSTEM_TYPE).toString());
		}
	}
	
	/**
	 * This method return the service provisioning type of fdpRequest
	 * @param fdpRequest
	 * @return
	 */
	public static FDPServiceProvSubType getFDPServiceProvSubTypeBySP(FDPRequest fdpRequest){
		FDPServiceProvSubType serviceProvSubType = null;
		if(fdpRequest.getValueFromRequest(RequestMetaValuesKey.SERVICE_PROVISIONING) instanceof FDPCacheable) {
			final FDPCacheable sp = (FDPCacheable)fdpRequest.getValueFromRequest(RequestMetaValuesKey.SERVICE_PROVISIONING);
			if(sp instanceof ServiceProvisioningRule) {
				final ServiceProvisioningRule sprule = (ServiceProvisioningRule) sp;
				if (sprule.getServiceProvDTO() instanceof AbstractServiceProvDTO) {
					AbstractServiceProvDTO abstractServiceProvDTO = (AbstractServiceProvDTO) sprule
							.getServiceProvDTO();
					return abstractServiceProvDTO.getSpSubType();
				}

			}
		}
		return serviceProvSubType;
	}
}