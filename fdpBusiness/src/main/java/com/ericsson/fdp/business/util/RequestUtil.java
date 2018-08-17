package com.ericsson.fdp.business.util;

import java.net.Inet4Address;
import java.net.UnknownHostException;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.UUID;
import java.util.regex.Pattern;

import javax.naming.NamingException;

import org.slf4j.Logger;

import com.ericsson.fdp.FDPCacheable;
import com.ericsson.fdp.business.NoRollbackOnFailure;
import com.ericsson.fdp.business.constants.FDPStepResponseConstants;
import com.ericsson.fdp.business.decorator.DelayDecorator;
import com.ericsson.fdp.business.decorator.FDPDecorator;
import com.ericsson.fdp.business.decorator.FDPResponseDecorator;
import com.ericsson.fdp.business.decorator.impl.BackSlashDecorator;
import com.ericsson.fdp.business.decorator.impl.BaseDecorator;
import com.ericsson.fdp.business.decorator.impl.FDPResponseDecoratorImpl;
import com.ericsson.fdp.business.decorator.impl.FlashDecorator;
import com.ericsson.fdp.business.decorator.impl.FooterDecorator;
import com.ericsson.fdp.business.decorator.impl.HeaderDecorator;
import com.ericsson.fdp.business.decorator.impl.PaginationDecorator;
import com.ericsson.fdp.business.enums.AuxRequestParam;
import com.ericsson.fdp.business.enums.Command;
import com.ericsson.fdp.business.enums.ConfigurationKey;
import com.ericsson.fdp.business.enums.ErrorTypes;
import com.ericsson.fdp.business.enums.RequestMetaValuesKey;
import com.ericsson.fdp.business.enums.TLVOptions;
import com.ericsson.fdp.business.node.AbstractServiceProvisioningNode;
import com.ericsson.fdp.business.node.FDPServiceProvisioningNode;
import com.ericsson.fdp.business.node.impl.InfoNode;
import com.ericsson.fdp.business.node.impl.ProductNode;
import com.ericsson.fdp.business.node.impl.RootNode;
import com.ericsson.fdp.business.node.impl.SpecialMenuNode;
import com.ericsson.fdp.business.product.Product;
import com.ericsson.fdp.business.request.FDPCheckConsumerResponse;
import com.ericsson.fdp.business.request.impl.FDPResponseImpl;
import com.ericsson.fdp.business.request.impl.FDPStepResponseImpl;
import com.ericsson.fdp.business.request.impl.FulfillmentRequestImpl;
import com.ericsson.fdp.business.serviceprovisioning.rule.ServiceProvisioningRule;
import com.ericsson.fdp.business.step.FDPStep;
import com.ericsson.fdp.business.step.impl.AttachProvisioningConditionStep;
import com.ericsson.fdp.business.step.impl.AttachProvisioningStep;
import com.ericsson.fdp.business.step.impl.CommandStep;
import com.ericsson.fdp.common.constants.FDPConstant;
import com.ericsson.fdp.common.constants.FDPLoggerConstants;
import com.ericsson.fdp.common.enums.ChannelType;
import com.ericsson.fdp.common.enums.ExternalSystem;
import com.ericsson.fdp.common.enums.ModuleType;
import com.ericsson.fdp.common.enums.Status;
import com.ericsson.fdp.common.util.StringUtil;
import com.ericsson.fdp.common.vo.CircleConfigParamDTO;
import com.ericsson.fdp.common.vo.FDPCircle;
import com.ericsson.fdp.core.cache.AppCacheSubStore;
import com.ericsson.fdp.core.command.FDPCommand;
import com.ericsson.fdp.core.config.utils.PropertyUtils;
import com.ericsson.fdp.core.display.DisplayObject;
import com.ericsson.fdp.core.dsm.framework.service.FDPCache;
import com.ericsson.fdp.core.entity.FDPEMADetail;
import com.ericsson.fdp.core.exception.EvaluationFailedException;
import com.ericsson.fdp.core.exception.ExecutionFailedException;
import com.ericsson.fdp.core.logging.FDPLogger;
import com.ericsson.fdp.core.node.FDPNode;
import com.ericsson.fdp.core.request.FDPRequest;
import com.ericsson.fdp.core.request.FDPResponse;
import com.ericsson.fdp.core.request.FDPSMPPRequest;
import com.ericsson.fdp.core.request.FDPStepResponse;
import com.ericsson.fdp.core.request.ResponseError;
import com.ericsson.fdp.core.request.impl.FDPRequestImpl;
import com.ericsson.fdp.core.request.impl.FDPSMPPRequestImpl;
import com.ericsson.fdp.core.request.impl.FDPWEBRequestImpl;
import com.ericsson.fdp.core.request.requestString.FDPRequestString;
import com.ericsson.fdp.core.serviceprovisioning.bean.FDPAppBag;
import com.ericsson.fdp.core.serviceprovisioning.bean.FDPMetaBag;
import com.ericsson.fdp.core.utils.ApplicationConfigUtil;
import com.ericsson.fdp.core.utils.CircleCodeFinder;
import com.ericsson.fdp.dao.dto.serviceprov.AbstractServiceProvDTO;
import com.ericsson.fdp.dao.dto.serviceprov.ServiceProvCommandDTO;
import com.ericsson.fdp.dao.dto.serviceprov.ServiceProvDTO;
import com.ericsson.fdp.dao.dto.serviceprov.ServiceProvSubStepDTO;
import com.ericsson.fdp.dao.dto.serviceprov.StepConstraintDTO;
import com.ericsson.fdp.dao.dto.serviceprov.StepDTO;
import com.ericsson.fdp.dao.enums.FDPServiceProvSubType;
import com.ericsson.fdp.dao.enums.LanguageType;

/*
Feature Name: User can purchase bundle for self and others
Changes: New function 'isProductBuyValidForBeneficiaryMsisdn()' created
Date: 28-10-2015
Singnum Id:ESIASAN
*/

/**
 * This class is a utility class that works on request.
 * 
 * @author Ericsson
 * 
 */
public class RequestUtil {
	
	private static String SIM_LANGUAGE_ENABLE="Y";
	
	/**
	 * Instantiates a new request util.
	 */
	private RequestUtil() {

	}

	/**
	 * This method is used to evaluate the node value.
	 * 
	 * @param fdpRequest
	 *            the fdp request
	 * @param fdpNode
	 *            The node which is to be evaluated.
	 * @return The evaluated value.
	 * @throws EvaluationFailedException
	 *             Exception, if the evaluation fails.
	 */
	public static boolean evaluateNodeValue(final FDPRequest fdpRequest, final FDPNode fdpNode)
			throws EvaluationFailedException {
		boolean nodeEvaluatedValue = false;
		if (fdpRequest instanceof FDPSMPPRequest) {
			final FDPSMPPRequest dynamicMenuRequest = (FDPSMPPRequest) fdpRequest;
			Boolean nodeValue = dynamicMenuRequest.getEvaluatedNodeValue(fdpNode.getFullyQualifiedPath());
			if (nodeValue == null || (((FDPSMPPRequestImpl)dynamicMenuRequest).getAuxiliaryRequestParameter(AuxRequestParam.isData2Share) != null)) {
				nodeValue = fdpNode.evaluateNode(dynamicMenuRequest);
				dynamicMenuRequest.addEvaluatedNodeValue(fdpNode.getFullyQualifiedPath(), nodeValue);
			}
			nodeEvaluatedValue = nodeValue;
		} else {
			nodeEvaluatedValue = fdpNode.evaluateNode(fdpRequest);
		}
		return nodeEvaluatedValue;
	}

	/**
	 * This method is used to update the product and service provisioning in
	 * request.
	 * 
	 * @param fdpRequest
	 *            The request object in which the value is to be set.
	 * @param productId
	 *            the product id
	 * @param spId
	 *            the sp id
	 * @return The product that has been set.
	 * @throws EvaluationFailedException
	 *             Exception, if the object is not found in the cache.
	 */
	public static Product updateProductAndSPInRequest(final FDPRequest fdpRequest, final String productId,
			final String spId) throws EvaluationFailedException {
		Product product = null;
		try {
			product = getProductById(fdpRequest, productId);
			final ServiceProvisioningRule serviceProvisioningRule = getServiceProvisioningById(fdpRequest, spId);
			if (product == null) {
				throw new EvaluationFailedException("The product was not found in cache" + productId);
			}
			if (fdpRequest instanceof FDPSMPPRequestImpl) {
				final FDPSMPPRequestImpl fdpussdsmscRequestImpl = (FDPSMPPRequestImpl) fdpRequest;
				fdpussdsmscRequestImpl.addMetaObject(RequestMetaValuesKey.PRODUCT, productId, product);
				fdpussdsmscRequestImpl.addMetaObject(RequestMetaValuesKey.SERVICE_PROVISIONING, spId,
						serviceProvisioningRule);
				if (serviceProvisioningRule.getServiceProvDTO() instanceof AbstractServiceProvDTO) {
					AbstractServiceProvDTO abstractServiceProvDTO = (AbstractServiceProvDTO) serviceProvisioningRule
							.getServiceProvDTO();
					String actionName = abstractServiceProvDTO.getSpSubType().name();
					if (FDPServiceProvSubType.PRODUCT_BUY_SPLIT.name()
							.equalsIgnoreCase(actionName)) {
						fdpussdsmscRequestImpl.addMetaValue(RequestMetaValuesKey.SERVICE_PROVISIONING, serviceProvisioningRule);
					}
					fdpussdsmscRequestImpl.addMetaValue(RequestMetaValuesKey.PRODUCT, product);
				}				
				fdpussdsmscRequestImpl.setDisplayObject(null);
			}
		} catch (final ExecutionFailedException e) {
			throw new EvaluationFailedException("Could not update product and SP", e);
		}
		return product;
	}

	/**
	 * This method is used to update the product and service provisioning in
	 * request.
	 * 
	 * @param fdpRequest
	 *            The request object in which the value is to be set.
	 * @param productId
	 *            the product id
	 * @param spId
	 *            the sp id
	 * @return The product that has been set.
	 * @throws EvaluationFailedException
	 *             Exception, if the object is not found in the cache.
	 */
	public static Product updateProductAndSPInWebRequest(final FDPRequest fdpRequest, final String productId,
			final String spId) throws EvaluationFailedException {
		Product product = null;
		try {
			product = getProductById(fdpRequest, productId);
			final ServiceProvisioningRule serviceProvisioningRule = getServiceProvisioningById(fdpRequest, spId);
			if (product == null) {
				throw new EvaluationFailedException("The product was not found in cache" + productId);
			}
			updateProductInRequest(fdpRequest, serviceProvisioningRule, product);
		} catch (final ExecutionFailedException e) {
			throw new EvaluationFailedException("Could not update product and SP", e);
		}
		return product;
	}

	/**
	 * Gets the service provisioning by id.
	 * 
	 * @param fdpRequest
	 *            the fdp request
	 * @param spId
	 *            the sp id
	 * @return the service provisioning by id
	 * @throws ExecutionFailedException
	 *             the execution failed exception
	 */
	public static ServiceProvisioningRule getServiceProvisioningById(final FDPRequest fdpRequest, final String spId)
			throws ExecutionFailedException {
		final FDPCacheable fdpSPCacheable = ApplicationConfigUtil.getMetaDataCache().getValue(
				new FDPMetaBag(fdpRequest.getCircle(), ModuleType.SP_PRODUCT, spId));
		ServiceProvisioningRule serviceProvisioningRule = null;
		if (fdpSPCacheable instanceof ServiceProvisioningRule) {
			serviceProvisioningRule = (ServiceProvisioningRule) fdpSPCacheable;
		}
		return serviceProvisioningRule;
	}

	/**
	 * Gets the product by id.
	 * 
	 * @param fdpRequest
	 *            the fdp request
	 * @param productId
	 *            the product id
	 * @return the product by id
	 * @throws ExecutionFailedException
	 *             the execution failed exception
	 */
	public static Product getProductById(final FDPRequest fdpRequest, final String productId)
			throws ExecutionFailedException {
		final FDPCache<FDPMetaBag, FDPCacheable> fdpMetaDataCache = ApplicationConfigUtil.getMetaDataCache();
		final FDPCacheable fdpProductCacheable = fdpMetaDataCache.getValue(new FDPMetaBag(fdpRequest.getCircle(),
				ModuleType.PRODUCT, productId));
		Product product = null;
		if (fdpProductCacheable instanceof Product) {
			product = (Product) fdpProductCacheable;
		}
		return product;
	}

	/**
	 * This method is used to update the product in request. This method if used
	 * for SMPP request, it resets the display objects.
	 * 
	 * @param fdpRequest
	 *            The request object.
	 * @param serviceProvisioning
	 *            The service provisioning object.
	 * @param product
	 *            The product object.
	 * @throws ExecutionFailedException
	 *             Exception, if execution fails.
	 */
	public static void updateProductInRequest(final FDPRequest fdpRequest, final FDPCacheable serviceProvisioning,
			final FDPCacheable product) throws ExecutionFailedException {
		if (serviceProvisioning != null && fdpRequest instanceof FDPRequestImpl) {
			final FDPRequestImpl requestImpl = (FDPRequestImpl) fdpRequest;
			requestImpl.addMetaValue(RequestMetaValuesKey.SERVICE_PROVISIONING, serviceProvisioning);
			if (product != null) {
				requestImpl.addMetaValue(RequestMetaValuesKey.PRODUCT, product);
			}
			if (fdpRequest instanceof FDPSMPPRequestImpl) {
				final FDPSMPPRequestImpl fdpussdsmscRequestImpl = (FDPSMPPRequestImpl) fdpRequest;
				fdpussdsmscRequestImpl.setDisplayObject(null);
			}
		} else {
			throw new ExecutionFailedException("The execution of service provisioning could not be completed.");
		}
	}

	/**
	 * This method is used to check for the node instance.
	 * 
	 * @param fdpCacheable
	 *            The cacheable object.
	 * @return The node object.
	 */
	public static FDPNode checkInstanceForNode(final FDPCacheable fdpCacheable) {
		FDPNode fdpNode = null;
		if (fdpCacheable instanceof FDPNode) {
			fdpNode = (FDPNode) fdpCacheable;
		}
		return fdpNode;
	}

	/**
	 * This method is used to find the circle for the provided subscriber
	 * number.
	 * 
	 * @param subscriberNumber
	 *            the subscriber number.
	 * @return the circle found.
	 * @throws NamingException
	 *             the naming exception
	 */
	@SuppressWarnings("unchecked")
	public static FDPCircle getFDPCircleFromMsisdn(final String subscriberNumber) throws NamingException {
		final FDPCache<FDPAppBag, Object> applicationConfigCache = (FDPCache<FDPAppBag, Object>) ApplicationConfigUtil
				.getBean("java:app/fdpCoreServices-1.0/ApplicationConfigCache");
		final FDPAppBag appBag2 = new FDPAppBag();
		appBag2.setSubStore(AppCacheSubStore.CIRCLE_CODE_CIRCLE_NAME_MAP);
		final String circleCode = CircleCodeFinder.getCircleCode(subscriberNumber, applicationConfigCache);
		if (circleCode != null) {
			appBag2.setKey(circleCode);
			return (FDPCircle) applicationConfigCache.getValue(appBag2);
		} else {
			return null;
		}

	}

	/**
	 * This method is used to put auxiliary value in the request.
	 * 
	 * @param dynamicMenuRequest
	 *            the request in which the value is to be put.
	 * @param key
	 *            the key to be put.
	 * @param value
	 *            the value to be put.
	 */
	public static void putAuxiliaryValueInRequest(final FDPRequest dynamicMenuRequest, final AuxRequestParam key,
			final Object value) {
		if (dynamicMenuRequest instanceof FDPRequestImpl) {
			final FDPRequestImpl fdpRequestImpl = (FDPRequestImpl) dynamicMenuRequest;
			fdpRequestImpl.putAuxiliaryRequestParameter(key, value);
		}
	}

	/**
	 * This method is used to put auxiliary values in the request.
	 * 
	 * @param dynamicMenuRequest
	 *            the request in which the value is to be put.
	 * @param serviceProvisioningNode
	 *            The node from which the values are to be put.
	 */
	public static void putPolicyRuleValuesInRequest(final FDPSMPPRequest dynamicMenuRequest,
			final FDPServiceProvisioningNode serviceProvisioningNode) {
		if (dynamicMenuRequest instanceof FDPRequestImpl
				&& serviceProvisioningNode instanceof AbstractServiceProvisioningNode) {
			final FDPRequestImpl fdpRequestImpl = (FDPRequestImpl) dynamicMenuRequest;
			final AbstractServiceProvisioningNode abstractServiceProvisioningNode = (AbstractServiceProvisioningNode) serviceProvisioningNode;
			if (abstractServiceProvisioningNode.getPolicyRuleValues() != null) {
				for (final Map.Entry<AuxRequestParam, Object> entrySet : abstractServiceProvisioningNode
						.getPolicyRuleValues().entrySet()) {
					fdpRequestImpl.putAuxiliaryRequestParameter(entrySet.getKey(), entrySet.getValue());
				}
			}
			fdpRequestImpl.setPolicyExecution(false);
		}
	}

	/**
	 * This method is used to update node in the request.
	 * 
	 * @param dynamicMenuRequest
	 *            the dynamic menu request in which the node is to be updated.
	 * @param fdpNode
	 *            the node to be added.
	 */
	public static void updateNodeInRequest(final FDPSMPPRequest dynamicMenuRequest, final FDPNode fdpNode) {
		if (dynamicMenuRequest instanceof FDPSMPPRequestImpl && fdpNode != null) {
			final FDPSMPPRequestImpl fdpsmppRequestImpl = (FDPSMPPRequestImpl) dynamicMenuRequest;
			fdpsmppRequestImpl.addNode(fdpNode.getFullyQualifiedPath(), fdpNode);
		}
	}

	/**
	 * Populate circle config param dto.
	 * 
	 * @param msisdn
	 *            the msisdn
	 * @param fdpCircle
	 *            the fdp circle
	 * @return the circle config param dto
	 * @throws ExecutionFailedException
	 *             the execution failed exception
	 */
	public static CircleConfigParamDTO populateCircleConfigParamDTO(final String msisdn, final FDPCircle fdpCircle)
			throws ExecutionFailedException {
		final FDPCache<FDPAppBag, Object> fdpCache = ApplicationConfigUtil.getApplicationConfigCache();

		if(null==msisdn || null==fdpCircle || fdpCache ==null|| StringUtil.isNullOrEmpty(msisdn)){
			throw new ExecutionFailedException("MSISDN :"+ msisdn +"| FDPCIRCLE :"+fdpCircle+ "| fdpCache :"+fdpCache);
		}
		
		final CircleConfigParamDTO circleConfigParamDTO = new CircleConfigParamDTO();
		String incomingMSISDN = msisdn;
		final Integer msisdnLength = msisdn.length();
		Long subsriberNAI = 0L;
		Long subscriberNumber;
		
		final FDPAppBag bag = new FDPAppBag();
		bag.setKey(ConfigurationKey.COUNTRY_CODE.getAttributeName());
		bag.setSubStore(AppCacheSubStore.CONFIGURATION_MAP);
		final String countryCode = (String) fdpCache.getValue(bag);


		final Map<String, String> configurationsMap = fdpCircle.getConfigurationKeyValueMap();
		circleConfigParamDTO
		.setOriginHostName(configurationsMap.get(ConfigurationKey.ORIGINHOSTNAME.getAttributeName()));
		circleConfigParamDTO
		.setOriginNodeType(configurationsMap.get(ConfigurationKey.ORIGINNODETYPE.getAttributeName()));

			
		final Integer allowedLength = Integer.parseInt(configurationsMap.get(ConfigurationKey.MSISDN_NUMBER_LENGTH
				.getAttributeName()));
		// get Parameter circle wise
		if(null==allowedLength || null==countryCode){
			throw new ExecutionFailedException("COUNTRY_CODE "+countryCode +"| MSISDN_NUMBER_LENGTH :"+allowedLength);
		}
	
		if (incomingMSISDN.length() <= allowedLength) {
			incomingMSISDN = countryCode + incomingMSISDN;
		}
		
		circleConfigParamDTO.setIncomingSubscriberNumber(Long.valueOf(incomingMSISDN));
		
		// check if the length of MSISDN is valid
		if (msisdnLength > allowedLength && msisdn.startsWith(countryCode)) {
			final String code = msisdn.substring(0, msisdnLength - allowedLength);
			subscriberNumber =Long.parseLong( msisdn);
			// Now check if the initial extra digits are Country code
			if (countryCode.equals(code)) {
				subsriberNAI = 1L;
			} 
		}/* else if (msisdnLength < allowedLength) {
			subsriberNAI = 0L;
			subscriberNumber = Long.valueOf(MSISDN);
		}*/ else {
			subscriberNumber = Long.valueOf(msisdn);
			final String code = msisdn.substring(0, msisdnLength - allowedLength);
			if (countryCode.equals(code)) {
				subsriberNAI = 1L;
			} else {
				subsriberNAI = 2L;
			}
		}
		circleConfigParamDTO.setSubscriberNumber(subscriberNumber);
		circleConfigParamDTO.setSubscriberNumberNAI(subsriberNAI);
		return circleConfigParamDTO;
	}

	/**
	 * This method is used to create step response.
	 * 
	 * @param stepExecuted
	 *            the value of step executed.
	 * @return the step response object.
	 */
	public static FDPStepResponse createStepResponse(final boolean stepExecuted) {
		final FDPStepResponseImpl fdpStepResponseImpl = new FDPStepResponseImpl();
		fdpStepResponseImpl.addStepResponseValue(FDPStepResponseConstants.STATUS_KEY, stepExecuted);
		return fdpStepResponseImpl;
	}

	/**
	 * This method is used to check the execution status for the step.
	 * 
	 * @param stepResponse
	 *            the step response.
	 * @return the status of the step.
	 */
	public static boolean checkExecutionStatus(final FDPStepResponse stepResponse) {
		boolean stepExecuted = false;
		final Object statusValue = stepResponse.getStepResponseValue(FDPStepResponseConstants.STATUS_KEY);
		if (statusValue instanceof Boolean) {
			stepExecuted = (Boolean) statusValue;
		}
		return stepExecuted;
	}

	/**
	 * This method is used to add the step response to the request.
	 * 
	 * @param fdpStepResponse
	 *            the step response to add.
	 * @param fdpRequest
	 *            the request in which the response is to be added.
	 * @param stepName
	 *            the name of the step.
	 */
	public static void putStepResponseInRequest(final FDPStepResponse fdpStepResponse, final FDPRequest fdpRequest,
			final String stepName) {
		if (fdpRequest instanceof FDPRequestImpl) {
			final FDPRequestImpl fdpRequestImpl = (FDPRequestImpl) fdpRequest;
			fdpRequestImpl.addStepInRequest(stepName, fdpStepResponse);
		}
	}

	/**
	 * This method is used to execute the dynamic menu request.
	 * 
	 * @param displayObject
	 *            the display object
	 * @param dynamicMenuRequest
	 *            The request.
	 * @param fdpNode
	 *            The current node.
	 * @return The response of execution.
	 * @throws ExecutionFailedException
	 *             Exception, if the node could not be executed.
	 */
	public static FDPResponse createResponseFromDisplayObject(final DisplayObject displayObject,
			final FDPRequest dynamicMenuRequest, final FDPNode fdpNode) throws ExecutionFailedException {
		FDPResponseImpl fdpResponse = null;
		if (displayObject.getNodesToDisplay() == null || displayObject.getNodesToDisplay().isEmpty()) {
			if (dynamicMenuRequest instanceof FDPSMPPRequest) {
				fdpResponse = getHelpText(((FDPSMPPRequest) dynamicMenuRequest).getRequestStringInterface(),
						dynamicMenuRequest.getCircle(), dynamicMenuRequest.getChannel());
				FDPLogger.debug(LoggerUtil.getSummaryLoggerFromRequest(dynamicMenuRequest), RequestUtil.class,
						"getHelpText()", LoggerUtil.getRequestAppender(dynamicMenuRequest) + "Help text found "
								+ fdpResponse.getResponseString());
			} else {
				fdpResponse = getHelpText(null, dynamicMenuRequest.getCircle(), dynamicMenuRequest.getChannel());
			}
		} else {
			final Logger circleLogger = LoggerUtil.getSummaryLoggerFromRequest(dynamicMenuRequest);
			FDPLogger.debug(circleLogger, RequestUtil.class, "executeDynamicMenuNode()",
					LoggerUtil.getRequestAppender(dynamicMenuRequest) + "Dynamic menu node");
			final DisplayObject displayObjectOther = addBaseDecorators(dynamicMenuRequest, fdpNode, displayObject);
			FDPLogger.debug(circleLogger, RequestUtil.class, "executeDynamicMenuNode()",
					LoggerUtil.getRequestAppender(dynamicMenuRequest) + "Display object created as "
							+ displayObjectOther);
			updateDisplayObjectInRequest(dynamicMenuRequest, displayObjectOther);
			fdpResponse = new FDPResponseImpl(Status.SUCCESS, false, null);
			DisplayObject responseMessage = null;
			// Add the flash message only if it not a pagination request.
			if (checkAuxParam(dynamicMenuRequest, AuxRequestParam.PAGINATION_REQUEST, true, Boolean.FALSE)
					&& checkAuxParam(dynamicMenuRequest, AuxRequestParam.REQUESTING_EXTERNAL_SYSTEM, false,
							ExternalSystem.MCARBON)) {
				responseMessage = addEnhancedDecorators(dynamicMenuRequest, fdpNode);
			}
			addObjectsToRequest(fdpResponse, responseMessage, displayObjectOther, dynamicMenuRequest);
		}
		return fdpResponse;
	}

	/**
	 * Check aux param.
	 * 
	 * Note : If aux param not found then it will be treated as success.
	 * 
	 * @param <T>
	 *            the generic type
	 * @param request
	 *            the request
	 * @param auxParam
	 *            the aux param
	 * @param isEqualTo
	 *            If isEqualTo true then return true if param value is equalto
	 *            the successValue Otherwise false
	 * @param successValue
	 *            the successValue
	 * @return true, if successful
	 * @throws ExecutionFailedException
	 *             the execution failed exception
	 */
	@SuppressWarnings("unchecked")
	public static <T> boolean checkAuxParam(final FDPRequest request, final AuxRequestParam auxParam,
			final boolean isEqualTo, final T successValue) throws ExecutionFailedException {
		if (successValue == null || request == null || auxParam == null) {
			throw new ExecutionFailedException("request, auxParam and successValue all are mandatory parameters.");
		}
		return (request.getAuxiliaryRequestParameter(auxParam) == null)
				|| (((T) request.getAuxiliaryRequestParameter(auxParam)).equals(successValue) == isEqualTo);
	}

	public static void updateDisplayObjectInRequest(final FDPRequest dynamicMenuRequest,
			final DisplayObject displayObjectOther) {
		if (dynamicMenuRequest instanceof FDPSMPPRequestImpl) {
			final FDPSMPPRequestImpl fdpussdsmscRequestImpl = (FDPSMPPRequestImpl) dynamicMenuRequest;
			fdpussdsmscRequestImpl.setDisplayObject(displayObjectOther);
		}
	}

	/**
	 * This method is used to add objects to request.
	 * 
	 * @param fdpResponse
	 *            the response formed.
	 * @param responseMessage
	 *            the response message.
	 * @param displayObjectOther
	 *            the display object.
	 * @param dynamicMenuRequest
	 *            the request.
	 */
	public static void addObjectsToRequest(final FDPResponseImpl fdpResponse, final DisplayObject responseMessage,
			final DisplayObject displayObjectOther, final FDPRequest dynamicMenuRequest) {
		if (responseMessage != null && responseMessage.getResponseMessage() != null) {
			fdpResponse.addResponseString(responseMessage.getResponseMessage());
		}
		fdpResponse.addResponseString(displayObjectOther.getResponseMessage());
	}

	/**
	 * This method is used to add enhanced decoration.
	 * 
	 * @param dynamicMenuRequest
	 *            the request object.
	 * @param fdpNode
	 *            the node.
	 * @return the display object.
	 * @throws ExecutionFailedException
	 *             Exception in execution.
	 */
	private static DisplayObject addEnhancedDecorators(final FDPRequest dynamicMenuRequest, final FDPNode fdpNode)
			throws ExecutionFailedException {
		return (new FlashDecorator(fdpNode, dynamicMenuRequest.getCircle(), dynamicMenuRequest.getChannel())).display();
	}

	/**
	 * This method is used to add decorators.
	 * 
	 * @param dynamicMenuRequest
	 *            the request.
	 * @param fdpNode
	 *            the node.
	 * @param displayObject
	 *            the display object.
	 * @return the display object formed.
	 * @throws ExecutionFailedException
	 *             Exception in execution.
	 */
	private static DisplayObject addBaseDecorators(final FDPRequest dynamicMenuRequest, final FDPNode fdpNode,
			final DisplayObject displayObject) throws ExecutionFailedException {
		final FDPDecorator baseDecorator = new BaseDecorator(displayObject, (String) ApplicationConfigUtil
				.getApplicationConfigCache().getValue(
						new FDPAppBag(AppCacheSubStore.CONFIGURATION_MAP, FDPConstant.USSD_MENU_SEPARATOR)),dynamicMenuRequest);
		final FDPDecorator headerDecorator = new HeaderDecorator(baseDecorator, fdpNode, dynamicMenuRequest.getSimLangauge());  // Add for French support on 19/12/16
		final FDPDecorator footerDecorator = new FooterDecorator(headerDecorator, fdpNode,
				dynamicMenuRequest.getCircle(), dynamicMenuRequest.getChannel());
		FDPDecorator decorator = new BackSlashDecorator(footerDecorator,dynamicMenuRequest);
		if (ChannelType.USSD.equals(dynamicMenuRequest.getChannel())) {
			final PaginationDecorator paginationDecorator = new PaginationDecorator(decorator, FDPConstant.NEWLINE,
					dynamicMenuRequest.getChannel(), dynamicMenuRequest.getCircle());
			decorator = paginationDecorator;
		}
		decorator = new DelayDecorator(decorator,
				(ExternalSystem) dynamicMenuRequest
						.getAuxiliaryRequestParameter(AuxRequestParam.REQUESTING_EXTERNAL_SYSTEM),
				dynamicMenuRequest.getCircle(), dynamicMenuRequest.getRequestId());
		return decorator.display();
	}

	/**
	 * This method is used to get the help text for the request string for the
	 * circle.
	 * 
	 * @param fdpRequestString
	 *            The request string interface
	 * @param fdpCircle
	 *            the circle.
	 * @param channelType
	 *            the channel type.
	 * @return the help text.
	 * @throws ExecutionFailedException
	 *             the execution failed exception
	 */
	public static FDPResponseImpl getHelpText(final FDPRequestString fdpRequestString, final FDPCircle fdpCircle,
			final ChannelType channelType) throws ExecutionFailedException {
		// find the node.
		String helpText = null;
		final FDPCacheable fdpCacheable = (fdpRequestString != null) ? getRootNode(fdpRequestString.getNodeString(),
				fdpCircle) : null;
		if (fdpCacheable instanceof RootNode) {
			final RootNode fdpNode = (RootNode) fdpCacheable;
			helpText = fdpNode.getHelpText();
		} else if (fdpCacheable instanceof InfoNode) {
			final InfoNode fdpNode = (InfoNode) fdpCacheable;
			helpText = fdpNode.getDisplayText();
		}
		// If help text has still not been found, get from the system menu.
		if (helpText == null) {
			helpText = (String) ApplicationConfigUtil.getApplicationConfigCache().getValue(
					new FDPAppBag(AppCacheSubStore.CONFIGURATION_MAP, FDPConstant.SYSTEM_HELP_DYNAMIC_MENU));
		}
		// Send help text and terminate session.
		return new FDPResponseImpl(Status.SUCCESS, true, ResponseUtil.createResponseMessageInList(channelType,
				helpText, TLVOptions.SESSION_TERMINATE));
	}

	/**
	 * This method is used to get the root node from the request.
	 * 
	 * @param fdpRequestString
	 *            the resquest string.
	 * @param fdpCircle
	 *            the circle.
	 * @return the root node.
	 * @throws ExecutionFailedException
	 *             Exception in execution.
	 */
	public static FDPCacheable getRootNode(final String fdpRequestString, final FDPCircle fdpCircle)
			throws ExecutionFailedException {
		FDPCacheable fdpCacheable = null;
		if (fdpRequestString != null) {
			fdpCacheable = ApplicationConfigUtil.getMetaDataCache().getValue(
					new FDPMetaBag(fdpCircle, ModuleType.DM, fdpRequestString));
		}
		return fdpCacheable;
	}

	/**
	 * This method is used to create step response.
	 * 
	 * @param stepExecuted
	 *            the value of step executed.
	 * @param commandFailed
	 *            the value of command failed.
	 * @return the step response object.
	 */
	public static FDPStepResponse createStepResponse(final boolean stepExecuted, final boolean commandFailed) {
		final FDPStepResponseImpl fdpStepResponseImpl = new FDPStepResponseImpl();
		fdpStepResponseImpl.addStepResponseValue(FDPStepResponseConstants.STATUS_KEY, stepExecuted);
		fdpStepResponseImpl.addStepResponseValue(FDPStepResponseConstants.COMMAND_STATUS_KEY, commandFailed);
		return fdpStepResponseImpl;
	}

	/**
	 * This method is used to check the execution status for the step.
	 * 
	 * @param stepResponseValue
	 *            the step response value.
	 * @return the status of the step.
	 */
	public static boolean checkExecutionStatus(final Object stepResponseValue) {
		boolean stepExecuted = false;
		if (stepResponseValue instanceof Boolean) {
			stepExecuted = (Boolean) stepResponseValue;
		}
		return stepExecuted;
	}

	/**
	 * This method is sued to get the ema details.
	 * 
	 * @param fdpRequest
	 *            the request object.
	 * @return the ema details.
	 * @throws ExecutionFailedException
	 *             Exception in execution.
	 */
	@SuppressWarnings("unchecked")
	public static FDPEMADetail getEmaDetails(final FDPRequest fdpRequest) throws ExecutionFailedException {
		FDPEMADetail fdpemaDetail = (FDPEMADetail) fdpRequest.getAuxiliaryRequestParameter(AuxRequestParam.EMA_DETAILS);
        if (fdpemaDetail == null) {
            final Map<Integer, FDPEMADetail> circleMap = (Map<Integer, FDPEMADetail>) ApplicationConfigUtil.getApplicationConfigCache()
                    .getValue(new FDPAppBag(AppCacheSubStore.EMA_DETAILS, fdpRequest.getCircle().getCircleCode()));

            if (circleMap != null) {
                for (Entry<Integer, FDPEMADetail> iterable_element : circleMap.entrySet()) {
                    //System.out.println(iterable_element.getKey());
                    //System.out.println(iterable_element.getValue());
                }
            }

            String numberSeries = CircleCodeFinder.getNumberSeries(fdpRequest.getSubscriberNumber().toString(),
                    ApplicationConfigUtil.getApplicationConfigCache());
            //System.out.println(numberSeries);
            fdpemaDetail = (circleMap != null) ? circleMap.get(numberSeries) : null;
            if (fdpRequest instanceof FDPRequestImpl) {
                ((FDPRequestImpl) fdpRequest).putAuxiliaryRequestParameter(AuxRequestParam.EMA_DETAILS, fdpemaDetail);
            }
        }
		if(fdpemaDetail==null)
		{
			throw  new ExecutionFailedException("EMA detail is not found for number");
			
			
		}
		return fdpemaDetail;
	}

	/**
	 * This method is used to check if logging is required.
	 * 
	 * @param fdpStepResponse
	 *            the step response.
	 */
	public static boolean checkIfLoggingRequired(final FDPStepResponse fdpStepResponse) {
		boolean stepExecuted = false;
		final Object statusValue = fdpStepResponse.getStepResponseValue(FDPStepResponseConstants.COMMAND_STATUS_KEY);
		if (statusValue instanceof Boolean) {
			stepExecuted = (Boolean) statusValue;
		}
		return stepExecuted;
	}

	/**
	 * This method is used to check the execution status.
	 * 
	 * @param fdpStepResponse
	 *            the step response.
	 * @param fdpStep
	 *            the step.
	 * @return true if the step executed successfully else false.
	 */
	public static boolean checkExecutionStatus(final FDPStepResponse fdpStepResponse, final FDPStep fdpStep) {
		return ((fdpStep instanceof NoRollbackOnFailure) || checkExecutionStatus(fdpStepResponse));
	}

	/**
	 * This method is used to decorate the response.
	 * 
	 * @param fdpResponse
	 *            the response to be decorated.
	 * @return the decorated response.
	 * @throws ExecutionFailedException
	 *             Exception in execution.
	 */
	public static FDPResponse addResponseDecorators(final FDPResponse fdpResponse) throws ExecutionFailedException {
		final FDPResponseDecorator fdpResponseDecorator = new FDPResponseDecoratorImpl(fdpResponse);
		return fdpResponseDecorator.decorateResponse();
	}

	/**
	 * This method is used to append to the response.
	 * 
	 * @param fdpStepResponse
	 *            the response to which to add.
	 * @param key
	 *            the key to use.
	 * @param value
	 *            the value to use.
	 */
	public static void appendResponse(final FDPStepResponse fdpStepResponse, final String key, final Object value) {
		if (fdpStepResponse instanceof FDPStepResponseImpl) {
			((FDPStepResponseImpl) fdpStepResponse).addStepResponseValue(key, value);
		}
	}

	/**
	 * Creates the fdp request without OriginTransactionID.
	 * 
	 * @param msisdn
	 *            the msisdn
	 * @param channelType
	 *            the channel type
	 * @return the fDP request impl
	 * @throws ExecutionFailedException
	 *             the execution failed exception
	 */
	public static FDPRequestImpl createFDPRequest(final String msisdn, final ChannelType channelType)
			throws ExecutionFailedException {
		FDPCircle fdpCircle;
		try {
			fdpCircle = getFDPCircleFromMsisdn(msisdn);
			if (fdpCircle == null) {
				throw new ExecutionFailedException("Unable to find the circle for msisdn = " + msisdn);
			}
			final CircleConfigParamDTO circleConfigParamDTO = populateCircleConfigParamDTO(msisdn, fdpCircle);
			final FDPRequestImpl fdpRequest = new FDPRequestImpl();
			fdpRequest.setOriginHostName(circleConfigParamDTO.getOriginHostName());
			fdpRequest.setOriginNodeType(circleConfigParamDTO.getOriginNodeType());
			fdpRequest.setSubscriberNumber(circleConfigParamDTO.getSubscriberNumber());
			fdpRequest.setSubscriberNumberNAI(circleConfigParamDTO.getSubscriberNumberNAI());
			fdpRequest.setChannel(channelType);
			fdpRequest.setCircle(fdpCircle);
			fdpRequest.setIncomingSubscriberNumber(circleConfigParamDTO.getIncomingSubscriberNumber());
			return fdpRequest;
		} catch (final NamingException e) {
			throw new ExecutionFailedException("Unable to find the circle for msisdn = " + msisdn, e);
		}
	}

	/**
	 * This method is used to update the request values for a new request.
	 * 
	 * @param dynamicMenuRequest
	 *            the request to be updated.
	 */
	public static void updateRequestValues(final FDPSMPPRequest dynamicMenuRequest) {
		RequestUtil.putAuxiliaryValueInRequest(dynamicMenuRequest, AuxRequestParam.PAGINATION_REQUEST, Boolean.FALSE);
	}

	public static FDPStepResponse createStepResponse(final boolean stepExecuted, final boolean commandFailed,
			final FDPStepResponse stepResponse) {
		final FDPStepResponseImpl fdpStepResponseImpl = new FDPStepResponseImpl();
		if (stepResponse != null) {
			fdpStepResponseImpl.addStepResponse(stepResponse);
		}
		fdpStepResponseImpl.addStepResponseValue(FDPStepResponseConstants.STATUS_KEY, stepExecuted);
		fdpStepResponseImpl.addStepResponseValue(FDPStepResponseConstants.COMMAND_STATUS_KEY, commandFailed);
		return fdpStepResponseImpl;
	}

	/* update method for bug number artf784450 */
	public static FDPStepResponse createStepResponse(final boolean stepExecuted, final FDPStepResponse stepResponse, final FDPRequest fdpRequest) {
		final FDPStepResponseImpl fdpStepResponseImpl = new FDPStepResponseImpl();
		if (stepResponse != null) {
			fdpStepResponseImpl.addStepResponse(stepResponse);
		}
		fdpStepResponseImpl.addStepResponseValue(FDPStepResponseConstants.STATUS_KEY, stepExecuted);
		if (!stepExecuted && (fdpStepResponseImpl.getStepResponseValue(FDPStepResponseConstants.ERROR_CODE) == null)) {
			if(fdpRequest.getCircle().getConfigurationKeyValueMap().containsKey(FDPStepResponseConstants.NO_CONSTRAINT_STEP_EXECUTED)){
			    fdpStepResponseImpl.addStepResponseValue(FDPStepResponseConstants.ERROR_CODE,
			    		fdpRequest.getCircle().getConfigurationKeyValueMap().get(FDPStepResponseConstants.NO_CONSTRAINT_STEP_EXECUTED));
			}else{
				fdpStepResponseImpl.addStepResponseValue(FDPStepResponseConstants.ERROR_CODE,
						 FDPStepResponseConstants.NO_CONSTRAINT_STEP_EXECUTED_ERROR_CODE);
			}
			fdpStepResponseImpl.addStepResponseValue(FDPStepResponseConstants.ERROR_VALUE,
					"No constraint step executed.");
			fdpStepResponseImpl.addStepResponseValue(FDPStepResponseConstants.ERROR_TYPE, ErrorTypes.UNKNOWN.name());
			fdpStepResponseImpl.addStepResponseValue(FDPStepResponseConstants.EXTERNAL_SYSTEM_TYPE,
					FDPConstant.EXTERNAL_SYSTEM_FDP);

		}
		return fdpStepResponseImpl;
	}

	/**
	 * This method is used to create step response.
	 * 
	 * @param stepExecuted
	 *            the value of step executed.
	 * @return the step response object.
	 */
	public static FDPStepResponse createStepResponse(final boolean stepExecuted, final String errorCode,
			final String errorValue, final String errorType, final String systemType) {
		final FDPStepResponseImpl fdpStepResponseImpl = new FDPStepResponseImpl();
		fdpStepResponseImpl.addStepResponseValue(FDPStepResponseConstants.STATUS_KEY, stepExecuted);
		fdpStepResponseImpl.addStepResponseValue(FDPStepResponseConstants.ERROR_CODE, errorCode);
		fdpStepResponseImpl.addStepResponseValue(FDPStepResponseConstants.ERROR_VALUE, errorValue);
		fdpStepResponseImpl.addStepResponseValue(FDPStepResponseConstants.ERROR_TYPE, errorType);
		fdpStepResponseImpl.addStepResponseValue(FDPStepResponseConstants.EXTERNAL_SYSTEM_TYPE, systemType);
		return fdpStepResponseImpl;
	}

	/**
	 * This method is used to update the request with request meta values.
	 * 
	 * @param fdpussdsmscRequest
	 *            the request.
	 * @param node
	 *            the key.
	 * @param serviceProvisioningNode
	 *            the node value.
	 */
	public static void updateMetaValuesInRequest(final FDPSMPPRequest fdpussdsmscRequest,
			final RequestMetaValuesKey node, final FDPNode serviceProvisioningNode) {
		if (fdpussdsmscRequest instanceof FDPRequestImpl) {
			final FDPRequestImpl fdpRequestImpl = (FDPRequestImpl) fdpussdsmscRequest;
			fdpRequestImpl.addMetaValue(node, serviceProvisioningNode);
		}
	}

	/**
	 * This method is used to put auxiliary value in the request.
	 * 
	 * @param dynamicMenuRequest
	 *            the request in which the value is to be put.
	 * @param stepResponseValue
	 *            the values to put.
	 */
	public static void putAuxiliaryValueInRequest(final FDPRequest fdpRequest,
			final Map<AuxRequestParam, Object> stepResponseValue) {

		if (fdpRequest instanceof FDPRequestImpl && stepResponseValue != null) {
			final FDPRequestImpl fdpRequestImpl = (FDPRequestImpl) fdpRequest;
			for (final Map.Entry<AuxRequestParam, Object> response : stepResponseValue.entrySet()) {
				fdpRequestImpl.putAuxiliaryRequestParameter(response.getKey(), response.getValue());
			}
		}

	}

	/**
	 * Gets the web request.
	 * 
	 * @param msisdn
	 *            the msisdn
	 * @param transactionNumber
	 *            the transaction number
	 * @param fdpCircle
	 *            the fdp circle
	 * @return the web request
	 * @throws ExecutionFailedException
	 *             the execution failed exception
	 */
	public static FDPWEBRequestImpl getWebRequest(final String msisdn, final Long transactionNumber,
			final FDPCircle fdpCircle) throws ExecutionFailedException {
		final FDPWEBRequestImpl fdpRequest = new FDPWEBRequestImpl();
		updateRequestParams(transactionNumber, fdpCircle, msisdn, fdpRequest, ChannelType.WEB);
		return fdpRequest;
	}

	/**
	 * Gets the iVR request.
	 * 
	 * @param msisdn
	 *            the msisdn
	 * @param transactionNumber
	 *            the transaction number
	 * @param fdpCircle
	 *            the fdp circle
	 * @return the iVR request
	 * @throws ExecutionFailedException
	 *             the execution failed exception
	 */
	public static FDPWEBRequestImpl getIVRRequest(final String msisdn, final Long transactionNumber,
			final FDPCircle fdpCircle) throws ExecutionFailedException {
		final FDPWEBRequestImpl fdpRequest = new FDPWEBRequestImpl();
		updateRequestParams(transactionNumber, fdpCircle, msisdn, fdpRequest, ChannelType.IVR);
		return fdpRequest;
	}

	
	public static FDPWEBRequestImpl getIVRandWebRequest(final String msisdn, final Long transactionNumber,
			final FDPCircle fdpCircle,final String requestId ,final ChannelType channel ) throws ExecutionFailedException {
		final FDPWEBRequestImpl fdpRequest = new FDPWEBRequestImpl();
		updateRequestParamsForIvrandWeb(transactionNumber, fdpCircle, msisdn, fdpRequest,requestId, channel);
		return fdpRequest;
	}

	
	
	/**
	 * Gets the request.
	 * 
	 * @param msisdn
	 *            the msisdn
	 * @param transactionNumber
	 *            the transaction number
	 * @param fdpCircle
	 *            the fdp circle
	 * @param channelType
	 *            the channel type
	 * @return the request
	 * @throws ExecutionFailedException
	 *             the execution failed exception
	 */
	public static FDPRequest getRequest(final String msisdn, final Long transactionNumber, final FDPCircle fdpCircle,
			final ChannelType channelType) throws ExecutionFailedException {
		final FDPRequestImpl fdpRequest = new FDPRequestImpl();
		updateRequestParams(transactionNumber, fdpCircle, msisdn, fdpRequest, channelType);
		return fdpRequest;

	}

	/**
	 * Update request params.
	 * 
	 * @param transactionNumber
	 *            the transaction number
	 * @param fdpCircle
	 *            the fdp circle
	 * @param msisdn
	 *            the msisdn
	 * @param fdpRequest
	 *            the fdp request
	 * @param channel
	 *            the channel
	 * @throws ExecutionFailedException
	 *             the execution failed exception
	 */
	private static void updateRequestParams(final Long transactionNumber, final FDPCircle fdpCircle,
			final String msisdn, final FDPRequestImpl fdpRequest, final ChannelType channel)
			throws ExecutionFailedException {
		final CircleConfigParamDTO circleConfigParamDTO = populateCircleConfigParamDTO(msisdn, fdpCircle);
		try {
			fdpRequest.setRequestId(channel.name() + FDPConstant.UNDERSCORE + Inet4Address.getLocalHost().getHostAddress()  
					+ FDPConstant.UNDERSCORE + (String.valueOf(UUID.randomUUID())));
		} catch (final UnknownHostException e) {
			throw new ExecutionFailedException("Unable to create requestId :" + e);
		}
		fdpRequest.setOriginHostName(circleConfigParamDTO.getOriginHostName());
		fdpRequest.setOriginNodeType(circleConfigParamDTO.getOriginNodeType());
		fdpRequest.setSubscriberNumber(circleConfigParamDTO.getSubscriberNumber());
		fdpRequest.setIncomingSubscriberNumber(circleConfigParamDTO.getIncomingSubscriberNumber());
		fdpRequest.setSubscriberNumberNAI(circleConfigParamDTO.getSubscriberNumberNAI());
		fdpRequest.setChannel(channel);
		fdpRequest.setCircle(fdpCircle);
		fdpRequest.setOriginTransactionID(transactionNumber);
	}
	
	
	private static void updateRequestParamsForIvrandWeb(final Long transactionNumber, final FDPCircle fdpCircle,
			final String msisdn, final FDPRequestImpl fdpRequest,final String requestId, final ChannelType channel)
			throws ExecutionFailedException {
		final CircleConfigParamDTO circleConfigParamDTO = populateCircleConfigParamDTO(msisdn, fdpCircle);
		//try {
			fdpRequest.setRequestId(requestId);
		/*} catch (final UnknownHostException e) {
			throw new ExecutionFailedException("Unable to create requestId :" + e);
		}*/
		fdpRequest.setOriginHostName(circleConfigParamDTO.getOriginHostName());
		fdpRequest.setOriginNodeType(circleConfigParamDTO.getOriginNodeType());
		fdpRequest.setSubscriberNumber(circleConfigParamDTO.getSubscriberNumber());
		fdpRequest.setIncomingSubscriberNumber(circleConfigParamDTO.getIncomingSubscriberNumber());
		fdpRequest.setSubscriberNumberNAI(circleConfigParamDTO.getSubscriberNumberNAI());
		fdpRequest.setChannel(channel);
		fdpRequest.setCircle(fdpCircle);
		fdpRequest.setOriginTransactionID(transactionNumber);
	}

	/**
	 * Adds the executed commands to request.
	 * 
	 * @param dynamicMenuRequest
	 *            the dynamic menu request
	 * @param fdpCheckConsumerResp
	 *            the fdp check consumer resp
	 */
	public static void addExecutedCommandsToRequestImpl(final FDPRequest dynamicMenuRequest,
			final FDPCheckConsumerResponse fdpCheckConsumerResp) {
		if (dynamicMenuRequest instanceof FDPRequestImpl) {
			final FDPRequestImpl fdpRequestImpl = (FDPRequestImpl) dynamicMenuRequest;
			final List<FDPCommand> fdpCommands = fdpCheckConsumerResp.getExecutedCommands();
			if (fdpCommands != null) {
				for (final FDPCommand fdpCommand : fdpCommands) {
					fdpRequestImpl.addExecutedCommand(fdpCommand);
					fdpRequestImpl.addExecutedCommandInStack(fdpCommand);
				}
			}

		}
	}

	/**
	 * Gets the meta value from request.
	 * 
	 * @param <T>
	 *            the generic type
	 * @param fdpRequest
	 *            the fdp request
	 * @param key
	 *            the key
	 * @param clazz
	 *            the clazz
	 * @return the meta value from request
	 * @throws ExecutionFailedException
	 *             the execution failed exception
	 */
	@SuppressWarnings("unchecked")
	public static <T extends FDPCacheable> T getMetaValueFromRequest(final FDPRequest fdpRequest,
			final RequestMetaValuesKey key, final Class<T> clazz) throws ExecutionFailedException {
		T result = null;
		final FDPCacheable valueCacheable = fdpRequest.getValueFromRequest(key);
		if (valueCacheable != null) {
			try {
				result = (T) valueCacheable;
			} catch (final ClassCastException e) {
				throw new ExecutionFailedException(e.getMessage(), e);
			}
		} else {
			throw new ExecutionFailedException(key + " was not found.");
		}
		return result;
	}

	/**
	 * This method will fetch the Tariff Enquiry Valadity Date Format from
	 * Configuration. Default => dd/MM/yy, will be applicable if
	 * no-configuration is set for the circle.
	 * 
	 * @param fdpRequest
	 * @return
	 */
	public static String getValidityFormat(final FDPRequest fdpRequest) {
		String tariffValidityFormat = null;
		try {
//			tariffValidityFormat = fdpRequest.getCircle().getConfigurationKeyValueMap().get(ConfigurationKey.TARIFF_VALIDITY_FORMAT.getAttributeName());
//			changes for expiry date format in balance check
			tariffValidityFormat = fdpRequest.getCircle().getConfigurationKeyValueMap().get(ConfigurationKey.BALANCE_CHECK_DATE_FORMAT.getAttributeName());

		} catch (final Exception e) {
			tariffValidityFormat = FDPConstant.DATE_PATTERN;
		}
		tariffValidityFormat = (tariffValidityFormat == null) ? FDPConstant.DATE_PATTERN : tariffValidityFormat;
		return tariffValidityFormat;
	}
	
		/**
	 * This method will set the language sim language in request.
	 * Updated for Airtel DRC FDP Project
	 * @param fdpRequest
	 * @throws ExecutionFailedException 
	 */
	public static void updateSIMLangaugeInRequest(final FDPRequest fdpRequest,final FDPCache<FDPAppBag, Object> applicationConfigCache) throws ExecutionFailedException {
		if (fdpRequest instanceof FDPRequestImpl) {
			 LanguageType languageType = null;
			 
			 //boolean flagValue = FDPApplicationFeaturesEnum.isFeatureAllowed(FDPApplicationFeaturesEnum.FEATURE_USE_SUBSCRIBER_LANGUAGE,applicationConfigCache);
			 if(SIM_LANGUAGE_ENABLE.equalsIgnoreCase(PropertyUtils.getProperty(FDPConstant.SIM_LANGUAGE_ENABLE))){
				final Integer simLangInt = CommandUtil.getLanguageFromGAD(fdpRequest);
				if(null != simLangInt ) {
					languageType = LanguageType.getLanguageType(simLangInt);
				}
			} else {
				languageType =getDefaultLang();
			}
				
			if (null != languageType) {
				final FDPRequestImpl fdpRequestImpl = (FDPRequestImpl) fdpRequest;
				fdpRequestImpl.setSimLangauge(languageType);				
			}
		}
	}
	
	/**
	 * Validates if the product constraints are valid for beneficiary msisdn
	 * 
	 * @param fdpRequest
	 * @param msisdn
	 * @return
	 * @throws EvaluationFailedException
	 * @throws ExecutionFailedException
	 * @throws NamingException
	 */
	public static boolean isProductBuyValidForBeneficiaryMsisdn(final FDPRequest fdpRequest, String msisdn){
		boolean isValid = false;
		try{
			Object fdpNodeObject = fdpRequest.getValueFromRequest(RequestMetaValuesKey.NODE);
			Long transactionId = ((FDPRequestImpl)fdpRequest).getOriginTransactionID();
			FDPRequest fdpRequestTemp = getRequest(msisdn, transactionId, fdpRequest.getCircle(), fdpRequest.getChannel());
			isValid = RequestUtil.evaluateNodeValue(fdpRequestTemp, (FDPNode)fdpNodeObject);
		}catch(Exception e){
			e.printStackTrace();
		}
		return isValid;
	}
	
	/**
	 * Update the service ID in request for EMA service Activation
	 * 
	 */
/*	public static void updateServiceIDInRequest(final FulfillmentRequestImpl fdpRequest, Object... additionalInformation){
		try{
			if (additionalInformation != null && additionalInformation[0] != null
					&& additionalInformation[0] instanceof Map<?, ?>) {
				Map<ServiceStepOptions, String> additionalMap;
			for (int i = 0; i < additionalInformation.length; i++) {
				
				additionalMap= (Map<ServiceStepOptions, String>) additionalInformation[i];
				additionalMap.keySet();
				if(additionalMap.get(ServiceStepOptions.SERVICE_ID)!=null)
				{
					fdpRequest.setServiceId(additionalMap.get(ServiceStepOptions.SERVICE_ID));	
				}
			}
		}
		}catch(Exception e){
			e.printStackTrace();
		}
	}*/
	public static LanguageType getDefaultLang() {
		final LanguageType languageType = LanguageType.getLanguageType(Integer.parseInt(PropertyUtils.getProperty(FDPConstant.DEFAULT_LANG_ID).trim()));
		return (null == languageType) ? LanguageType.ENGLISH : languageType;
	}
	
	/**
	 * Prepare Response step with async status.
	 * 
	 * @param stepExecuted
	 * @param commandFailed
	 * @param stepResponse
	 * @param isLastCommandAsync
	 * @return
	 */
	public static FDPStepResponse createStepResponse(final boolean stepExecuted, final boolean commandFailed,
			final FDPStepResponse stepResponse, final Boolean isLastCommandAsync) {
		final FDPStepResponse fdpStepResponse = createStepResponse(stepExecuted, commandFailed, stepResponse);
		final FDPStepResponseImpl fdpStepResponseImpl = (FDPStepResponseImpl) fdpStepResponse;
		fdpStepResponseImpl.addStepResponseValue(FDPStepResponseConstants.IS_CURRENT_COMMAND_ASYNC,isLastCommandAsync);
		return fdpStepResponseImpl;
	}

	/**
	 * Prepare Response step with async status.
	 * 
	 * @param stepExecuted
	 * @param commandFailed
	 * @param stepResponse
	 * @param isLastCommandAsync
	 * @return
	 */
	public static FDPServiceProvSubType getFDPServiceProvSubType(final FDPRequest  fdpRequest) {
		FDPServiceProvSubType fdpServiceProvSubType = null;
		final FDPNode fdpNode = (FDPNode)fdpRequest.getValueFromRequest(RequestMetaValuesKey.NODE);
		if(null != fdpNode && fdpNode instanceof SpecialMenuNode) {
			SpecialMenuNode specialMenuNode = (SpecialMenuNode) fdpNode;
			fdpServiceProvSubType = specialMenuNode.getSpecialMenuNodeType();
		}
		else if(null != fdpNode && fdpNode instanceof ProductNode) {
			ProductNode productNode = (ProductNode) fdpNode;
			fdpServiceProvSubType = productNode.getServiceProvSubType();
		}
		return fdpServiceProvSubType;
	}
	
	public static void removeSteps(final ServiceProvisioningRule provisioningRule) {
		ServiceProvDTO serviceProvDTO = provisioningRule.getServiceProvDTO();
		final Iterator<StepDTO> stepDTOIterator = serviceProvDTO.getServiceProvStepList().iterator();
		while(stepDTOIterator.hasNext()) {
			StepDTO step = stepDTOIterator.next();
			if(isStepRequired(step)) {
				stepDTOIterator.remove();
			}
		}
		final Iterator<FDPStep> fdpStepIterator = provisioningRule.getFdpSteps().iterator();
		while(fdpStepIterator.hasNext()) {
			final FDPStep fdpStep = fdpStepIterator.next();
			if(isRequired(fdpStep)) {
				fdpStepIterator.remove();
			}
		}
	}
	
	public static String  getConfigurationKeyValue(FDPRequest fdpRequest, ConfigurationKey configurationKey){
		final Map<String, String> configurationsMap = fdpRequest.getCircle().getConfigurationKeyValueMap();
		return configurationsMap.get(configurationKey.getAttributeName());
	}
	
	/**
	 * Validates beneficiary MSISDN according to the country code and allowed number length
	 * 
	 * @param beneficiaryMsisdn contains beneficiary phone number.  
	 * @param fdpRequest
	 * @return 
	 * @throws ExecutionFailedException when MSISDN_NUMBER_LENGTH property is null or 
	 * 									COUNTRY_CODE property is null.
	 */
	public static boolean isBeneficiaryMsisdnValid(String beneficiaryMsisdn, FDPRequest fdpRequest) throws ExecutionFailedException {
		if(null==getConfigurationKeyValue(fdpRequest,ConfigurationKey.MSISDN_NUMBER_LENGTH))
			throw new ExecutionFailedException("MSISDN_NUMBER_LENGTH property is null..please provide a value!");
		Integer allowedLength = Integer.parseInt(getConfigurationKeyValue(fdpRequest,ConfigurationKey.MSISDN_NUMBER_LENGTH));
		final FDPAppBag bag = new FDPAppBag();
		bag.setKey(ConfigurationKey.COUNTRY_CODE.getAttributeName());
		bag.setSubStore(AppCacheSubStore.CONFIGURATION_MAP);
		final FDPCache<FDPAppBag, Object> fdpCache = ApplicationConfigUtil.getApplicationConfigCache();
		if(null==fdpCache.getValue(bag))
			throw new ExecutionFailedException("COUNTRY_CODE or CONFIGURATION_MAP property is null..please provide a value!");
		final String countryCode = (String) fdpCache.getValue(bag);
		Integer msisdnLength = beneficiaryMsisdn.length();
		if (msisdnLength > allowedLength && (beneficiaryMsisdn.startsWith(countryCode) || beneficiaryMsisdn.startsWith(FDPConstant.MSISDN_WITH_PREFIX_ZERO)) && beneficiaryMsisdn.matches("[0-9]+")) {
			final String code = beneficiaryMsisdn.substring(0, msisdnLength - allowedLength);
			if (countryCode.equals(code) || beneficiaryMsisdn.startsWith(FDPConstant.MSISDN_WITH_PREFIX_ZERO)) 
				return true;
		}
		return false;
	}
	/**
	 * Validates beneficiary MSISDN according to the country code and Start with zero
	 * 
	 * @param beneficiaryMsisdn contains beneficiary phone number.  
	 * @param alternateMsisdn
	 * @return 
	 * @throws ExecutionFailedException when MSISDN_NUMBER_LENGTH property is null or 
	 * 									COUNTRY_CODE property is null.
	 */
	
	public static String startsWithZeroAltMsisdn(String alternateMsisdn) throws ExecutionFailedException {
		final FDPAppBag bag = new FDPAppBag();
		bag.setKey(ConfigurationKey.COUNTRY_CODE.getAttributeName());
		bag.setSubStore(AppCacheSubStore.CONFIGURATION_MAP);
		final FDPCache<FDPAppBag, Object> fdpCache = ApplicationConfigUtil.getApplicationConfigCache();
		if(null==fdpCache.getValue(bag))
			throw new ExecutionFailedException("COUNTRY_CODE or CONFIGURATION_MAP property is null..please provide a value!");
		if (alternateMsisdn.startsWith(FDPConstant.MSISDN_WITH_PREFIX_ZERO)) 
			return alternateMsisdn.replaceFirst(FDPConstant.MSISDN_WITH_PREFIX_ZERO, (String) fdpCache.getValue(bag));
		
		return alternateMsisdn;
	}
	
	
	/**
	 * Validates Product service provisioning type
	 * 
	 * @param fdpRequest
	 * @param fdpServiceProvSubType
	 * @return 
	 * 
	 */	
	public static boolean isProductSpTypeValid(FDPRequest fdpRequest, FDPServiceProvSubType fdpServiceProvSubType) {
		if (fdpRequest.getValueFromRequest(RequestMetaValuesKey.NODE) instanceof FDPNode) {
				final FDPNode fdpNode = (FDPNode) fdpRequest.getValueFromRequest(RequestMetaValuesKey.NODE);
				if(fdpNode == null)
					return false;
				if (fdpNode instanceof ProductNode) {
					final ProductNode productNode = (ProductNode) fdpNode;
					if (productNode.getServiceProvSubType() != null) {
						return fdpServiceProvSubType.equals(productNode.getServiceProvSubType());
					}
				}
			}
		return false;
	}
	
	public static String getNotificationText(FDPRequest fdpRequest,ConfigurationKey configurationKey, String staticMsg) {
		String notificationText = getConfigurationKeyValue(fdpRequest,configurationKey);
		StringBuilder stringBuilder = new StringBuilder();
		if (notificationText == null || notificationText.isEmpty()) 
			notificationText = staticMsg;
		if(notificationText.contains(FDPConstant.COMMA)){
			for (String string : notificationText.split(FDPConstant.COMMA)) {
				stringBuilder.append(string).append(FDPConstant.NEWLINE);
			}
		}
		return stringBuilder.toString().length()!=0? stringBuilder.toString():notificationText;
	}
	
	
	private static boolean isStepRequired(final StepDTO step){
		boolean isStepConstraintStep = (null != step && step instanceof StepConstraintDTO);
		boolean isMPRCommand = false;
		if(isStepConstraintStep) {
			StepConstraintDTO stepConstraintDTO = (StepConstraintDTO) step;
			Iterator<ServiceProvSubStepDTO> iterator = stepConstraintDTO.getSubstepDTOList().iterator();
			while(iterator.hasNext()) {
				final ServiceProvSubStepDTO serviceProvSubStepDTO = iterator.next();
				if(!isStepContainsMPRCommmand(serviceProvSubStepDTO)) {
					iterator.remove();
				}
				else {
					isMPRCommand = true;
				}
			}
			isStepConstraintStep = !isMPRCommand ? false : isStepConstraintStep; 
		}
		return !isStepConstraintStep;
	}
	
	private static boolean isStepContainsMPRCommmand(final ServiceProvSubStepDTO serviceProvSubStepDTO) {
		boolean isPresent = false;
		final Iterator<ServiceProvCommandDTO> iterator = serviceProvSubStepDTO.getSpCommandDTOList().iterator();
		while(iterator.hasNext()) {
			final ServiceProvCommandDTO commandDTO = iterator.next();
			if(commandDTO != null  && commandDTO.getCommandCircleDTO() != null && commandDTO.getCommandCircleDTO().getCommandDTO() != null && Command.MPR.getCommandDisplayName().equals(commandDTO.getCommandCircleDTO().getCommandDTO().getCommandDisplayName())){
				isPresent = true;
			}
		}
		return isPresent;
	}
	
	private static boolean isRequired(final FDPStep fdpStep) {
		boolean isAttachProvisioningStep = (null != fdpStep && fdpStep instanceof AttachProvisioningStep);
		if(isAttachProvisioningStep) {
			final AttachProvisioningStep attachProvisioningStep = (AttachProvisioningStep) fdpStep;
			final Iterator<AttachProvisioningConditionStep> iterator = attachProvisioningStep.getAttachProvisioning().iterator();
			while(iterator.hasNext()) {
				final AttachProvisioningConditionStep attachProvisioningConditionStep = iterator.next();
				if(!isConditionStepContainsSingleProvCmd(attachProvisioningConditionStep)) {
					iterator.remove();
				}
			}
		}
		return !isAttachProvisioningStep;
	}
	
	private static boolean isConditionStepContainsSingleProvCmd(final AttachProvisioningConditionStep attachProvisioningConditionStep) {
		boolean isPresent = false;
		final Iterator<FDPStep> iterator = attachProvisioningConditionStep.getSteps().iterator();
		while(iterator.hasNext()) {
			final FDPStep fdpStep = iterator.next();
			if(isSingleProvCmd(fdpStep)) {
				isPresent = true;
			}
		}
		return isPresent;
	}
	
	private static boolean isSingleProvCmd(final FDPStep fdpStep) {
		return (null != fdpStep && fdpStep instanceof CommandStep && Command.MPR.getCommandDisplayName().equals(((CommandStep)fdpStep).getCommandDisplayName()));
	}

	public static FDPServiceProvSubType getFDPServiceProvSubTypeAction(final FDPServiceProvSubType fdpServiceProvSubType) {
		FDPServiceProvSubType serviceProvSubType = null;
		switch (fdpServiceProvSubType) {
		//OPTIN -- > Normal(Adhoc) to Renewal.
		// Normal Product for Product Purchase, then PRODUCT_BUY_RECURRING SP with SingleProvRequest to OPTOUT.
		case OPT_IN_SERVICE:
			serviceProvSubType = FDPServiceProvSubType.PRODUCT_BUY; // Ad
			break;
		//OPTOUT --> Renewal to Normal(Adhoc).
		case OPT_OUT_SERVICE:
			serviceProvSubType = FDPServiceProvSubType.PRODUCT_BUY_RECURRING;    // RS
			break;
		default:
			break;
		}
		return serviceProvSubType;
	}

	public static void updateHandsetBasedParametersInRequest(FDPRequestImpl fulfillmentRequest) throws ExecutionFailedException{
		try{
		if(fulfillmentRequest instanceof FulfillmentRequestImpl){	
			CommandUtil.setHandsetBasedParametersInRequest(fulfillmentRequest);
		}
		}catch(ExecutionFailedException e){
			throw new ExecutionFailedException(e.getMessage());
			
		}
		
	}
	
	/***
	 * This method valid the incoming MSISDN and return valid MSISDN 
	 * 
	 * @param fdpRequest
	 * @param beneficiaryMsisdn
	 * @return
	 * @throws ExecutionFailedException
	 */
	
	public static String validateBeneficiaryMsisdn(FDPRequest fdpRequest, String beneficiaryMsisdn)
			throws ExecutionFailedException {
		final FDPCircle benMsisdnCircle;

		if (null == beneficiaryMsisdn || !Pattern.matches(FDPConstant.INTEGER_PATTERN, beneficiaryMsisdn)
				/*|| !(beneficiaryMsisdn.length() >= Integer
						.parseInt(PropertyUtils.getProperty(BusinessConstants.PROPERTY_MSISDN_LOWER_BOUND))
						&& beneficiaryMsisdn.length() <= Integer
								.parseInt(PropertyUtils.getProperty(BusinessConstants.PROPERTY_MSISDN_UPPER_BOUND)))*/) {
			FDPLogger.info(LoggerUtil.getSummaryLoggerFromRequest(fdpRequest), RequestUtil.class,
					"validateBeneficiaryMsisdn", "Request is invalid for beneficiary msisdn : " + beneficiaryMsisdn);
			return null;
		}

		benMsisdnCircle = CircleCodeFinder.getFDPCircleByMsisdn(beneficiaryMsisdn,
				ApplicationConfigUtil.getApplicationConfigCache());

		if (!(null != benMsisdnCircle
				&& benMsisdnCircle.getCircleName().contentEquals(fdpRequest.getCircle().getCircleName()))) {
			FDPLogger.info(LoggerUtil.getSummaryLoggerFromRequest(fdpRequest), RequestUtil.class,
					"validateBeneficiaryMsisdn", "Request is invalid for beneficiary msisdn : " + beneficiaryMsisdn);
			return null;

		} else {

			final Map<String, String> configurationsMap = benMsisdnCircle.getConfigurationKeyValueMap();
			final Integer allowedLength = Integer
					.parseInt(configurationsMap.get(ConfigurationKey.MSISDN_NUMBER_LENGTH.getAttributeName()));

			return updateMsisdn(beneficiaryMsisdn, allowedLength);

		}
	}
	
	/**
	 * This method will added the country code in given msisdn 
	 * 
	 * @param msisdn
	 * @param allowedLength
	 * @return
	 * @throws ExecutionFailedException
	 */
	public static String updateMsisdn(String msisdn, Integer allowedLength) throws ExecutionFailedException {
		String updatedMsisdn = null;
		final FDPAppBag bag = new FDPAppBag();
		bag.setKey(ConfigurationKey.COUNTRY_CODE.getAttributeName());
		bag.setSubStore(AppCacheSubStore.CONFIGURATION_MAP);
		
		final FDPCache<FDPAppBag, Object> fdpCache = ApplicationConfigUtil.getApplicationConfigCache();
		final String countryCode = (String) fdpCache.getValue(bag);
		
		if (msisdn.length() == allowedLength) {
			updatedMsisdn = countryCode + msisdn;
		} else if (msisdn.length() == allowedLength + 1 && msisdn.startsWith("0")) {
			updatedMsisdn = msisdn.replaceFirst("0", countryCode);
		}else if ((msisdn.length() == allowedLength + countryCode.length()) && msisdn.startsWith(countryCode)){
			updatedMsisdn=msisdn;
		}
		return updatedMsisdn;
	}

	/**
	 * This method will write FDPCommand execution status
	 * @param fdpRequest
	 * @param fdpresponse
	 * @param circleLogger
	 * @return
	 */
	public static String writeProvRslt(FDPRequest fdpRequest, FDPResponse fdpresponse, Logger circleLogger) {
		 final Status responseStatus = fdpresponse == null ? Status.FAILURE : fdpresponse.getExecutionStatus();
			final ResponseError responseError = getResponseError(fdpresponse);
			final StringBuilder stringBuilder = new StringBuilder();
			
			if (responseStatus!=null && Status.FAILURE.equals(responseStatus)) {
				 
				 stringBuilder.append(LoggerUtil.getRequestAppender(fdpRequest)).append("PROVRSLT")
					.append(FDPConstant.LOGGER_KEY_VALUE_DELIMITER).append(responseStatus);
				 
				stringBuilder.append(FDPConstant.LOGGER_DELIMITER).append("PROVRSN")
						.append(FDPConstant.LOGGER_KEY_VALUE_DELIMITER).append("responseCode=")
						.append((!StringUtil.isNullOrEmpty(responseError.getResponseCode()) ? responseError.getResponseCode() : FDPConstant.UNKOWN_RESPONSE_CODE)).append(";Desc=")
						.append((!StringUtil.isNullOrEmpty(responseError.getResponseErrorString()) ? responseError.getResponseErrorString() : FDPConstant.UNKOWN_RESPONSE_DESC))
						.append(";External Node=").append((!StringUtil.isNullOrEmpty(responseError.getSystemType()) ? responseError.getSystemType() : FDPConstant.UNKOWN_SYSTEM));
			}
			if (stringBuilder.length() == 0) {
				stringBuilder.append(LoggerUtil.getRequestAppender(fdpRequest)).append("PROVRSLT")
				.append(FDPConstant.LOGGER_KEY_VALUE_DELIMITER).append(responseStatus);
			}
			
			stringBuilder.append(FDPConstant.LOGGER_DELIMITER).append(FDPLoggerConstants.CORRELATION_ID).append(FDPConstant.LOGGER_KEY_VALUE_DELIMITER)
			.append(fdpRequest.getOriginTransactionID().toString());
			
			return stringBuilder.toString();
	}
	
	/**
	 * This method will return the Error response code
	 * @param fdpResponse
	 * @return
	 */
	public static ResponseError getResponseError(final FDPResponse fdpResponse) {
		return (fdpResponse == null || (null == fdpResponse.getResponseError())) ? new ResponseError("NA", "Unknown Error occurred", ErrorTypes.UNKNOWN.name(),FDPConstant.EXTERNAL_SYSTEM_FDP)
				: fdpResponse.getResponseError();
	}

}