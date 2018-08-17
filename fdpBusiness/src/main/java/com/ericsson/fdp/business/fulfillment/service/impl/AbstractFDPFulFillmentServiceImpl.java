package com.ericsson.fdp.business.fulfillment.service.impl;

import java.util.Arrays;

import javax.annotation.Resource;
import javax.inject.Inject;

import ch.qos.logback.classic.Logger;

import com.ericsson.fdp.FDPCacheable;
import com.ericsson.fdp.business.constants.BusinessConstants;
import com.ericsson.fdp.business.display.impl.ResponseMessageImpl;
import com.ericsson.fdp.business.enums.AuxRequestParam;
import com.ericsson.fdp.business.enums.ConfigurationKey;
import com.ericsson.fdp.business.enums.TLVOptions;
import com.ericsson.fdp.business.enums.ivr.FulfillmentResponseCodes;
import com.ericsson.fdp.business.enums.ivr.FulfillmentSystemTypes;
import com.ericsson.fdp.business.exception.NotificationFailedException;
import com.ericsson.fdp.business.fulfillment.service.FDPFulfillmentService;
import com.ericsson.fdp.business.ivr.responsegenerator.IVRResponseGenerator;
import com.ericsson.fdp.business.node.FDPServiceProvisioningNode;
import com.ericsson.fdp.business.node.impl.ProductNode;
import com.ericsson.fdp.business.request.impl.FDPMetadataResponseImpl;
import com.ericsson.fdp.business.request.impl.FulfillmentRequestImpl;
import com.ericsson.fdp.business.response.fulfillment.xml.Consumer;
import com.ericsson.fdp.business.serviceprovisioning.ServiceProvisioning;
import com.ericsson.fdp.business.serviceprovisioning.rule.ServiceProvisioningRule;
import com.ericsson.fdp.business.transaction.TransactionSequenceGeneratorService;
import com.ericsson.fdp.business.util.DynamicMenuUtil;
import com.ericsson.fdp.business.util.FulfillmentUtil;
import com.ericsson.fdp.business.util.LoggerUtil;
import com.ericsson.fdp.business.util.NotificationUtil;
import com.ericsson.fdp.business.util.RequestUtil;
import com.ericsson.fdp.common.constants.FDPConstant;
import com.ericsson.fdp.common.constants.JNDILookupConstant;
import com.ericsson.fdp.common.enums.BusinessModuleType;
import com.ericsson.fdp.common.enums.ChannelType;
import com.ericsson.fdp.common.enums.ModuleType;
import com.ericsson.fdp.common.enums.Status;
import com.ericsson.fdp.common.util.StringUtil;
import com.ericsson.fdp.common.vo.FDPCircle;
import com.ericsson.fdp.core.command.FDPCommand;
import com.ericsson.fdp.core.display.ResponseMessage;
import com.ericsson.fdp.core.dsm.framework.service.FDPCache;
import com.ericsson.fdp.core.exception.EvaluationFailedException;
import com.ericsson.fdp.core.exception.ExecutionFailedException;
import com.ericsson.fdp.core.logging.FDPLogger;
import com.ericsson.fdp.core.node.FDPNode;
import com.ericsson.fdp.core.request.FDPRequest;
import com.ericsson.fdp.core.request.FDPResponse;
import com.ericsson.fdp.core.request.ResponseError;
import com.ericsson.fdp.core.request.impl.FDPRequestImpl;
import com.ericsson.fdp.core.serviceprovisioning.bean.FDPAppBag;
import com.ericsson.fdp.core.serviceprovisioning.bean.FDPMetaBag;
import com.ericsson.fdp.core.utils.ApplicationConfigUtil;
import com.ericsson.fdp.dao.enums.FDPServiceProvSubType;
import com.ericsson.fdp.dao.enums.FDPServiceProvType;
import com.ericsson.fdp.dao.util.ServiceProvDataConverterUtil;

/**
 * This is the abstract class for different fulfillement IVR services.
 * 
 * @author Ericsson
 * 
 */
public abstract class AbstractFDPFulFillmentServiceImpl implements FDPFulfillmentService {

	@Inject
	private IVRResponseGenerator ivrResponseGenerator;

	/** The application cache. */
	@Resource(lookup = "java:app/fdpCoreServices-1.0/ApplicationConfigCache")
	private FDPCache<FDPAppBag, Object> applicationConfigCache;

	@Resource(lookup = JNDILookupConstant.TRANSACTION_ID_GENERATOR_SERVICE)
	private TransactionSequenceGeneratorService generatorService;

	/** The fdp cache. */
	@Resource(lookup = "java:app/fdpBusiness-1.0/MetaDataCache")
	private FDPCache<FDPMetaBag, FDPCacheable> fdpMetaDataCache;
	
	/** The fdp service provisioning. */
	@Resource(lookup = "java:app/fdpBusiness-1.0/FulfillmentServiceProvisioningImpl")
	private ServiceProvisioning fdpFulfillmentServiceProvisioning;
	
	/** The circle Logger **/
	protected Logger circleLogger = null;

	@Override
	public FDPResponse execute(final FDPRequest fdpRequest, final Object... additionalInformations)
			throws ExecutionFailedException {
		updateTransactionId(fdpRequest);
		//Added by Rajat : BugId 812568
		RequestUtil.updateSIMLangaugeInRequest(fdpRequest,applicationConfigCache);
		FDPLogger.debug(
				getCircleLogger(fdpRequest),
				getClass(),
				"execute",
				LoggerUtil.getRequestAppender(fdpRequest) + "Start executing Service Action:"
						+ fdpRequest.getRequestId());
		FDPResponse fdpResponse = executeService(fdpRequest, additionalInformations);
		FDPResponse fulfillmentResponse = checkInstanceBefore(fdpRequest, fdpResponse); 
		final String xmlResponseString = prepareFulFillmentResponse(fdpRequest, fulfillmentResponse);
		FDPLogger.debug(
				getCircleLogger(fdpRequest),
				getClass(),
				"execute",
				LoggerUtil.getRequestAppender(fdpRequest) + "Preparing Response:"
						+ fdpRequest.getRequestId()+", xmlResponseString:"+xmlResponseString);
		addXmlStringToResponse(fulfillmentResponse, xmlResponseString);
		return fulfillmentResponse;
	}

	/**
	 * This method will parse the service response.
	 * 
	 * @param fdpRequest
	 * @param fdpResponse
	 * @return
	 * @throws ExecutionFailedException
	 */
	private String prepareFulFillmentResponse(final FDPRequest fdpRequest, final FDPResponse fdpResponse)
			throws ExecutionFailedException {
		return ivrResponseGenerator.generateIVRResponse(fdpRequest, fdpResponse);
	}

	/**
	 * This method check the instance of the request and resonse before response
	 * generations.
	 * 
	 * @param fdpRequest
	 * @param fdpResponse
	 * @throws ExecutionFailedException
	 */
	protected FDPResponse checkInstanceBefore(final FDPRequest fdpRequest, final FDPResponse fdpResponse)
			throws ExecutionFailedException {
		FDPResponse fulfilmentResponse;
		if (null == fdpResponse) {
			throw new ExecutionFailedException("Found Service Execution Response NULL");
		}

		if ((null != fdpRequest) && (!(fdpRequest instanceof FulfillmentRequestImpl))) {
			throw new ExecutionFailedException("Request is not of FulfillmentRequestImpl Type");
		}
		
		if (!(fdpResponse instanceof FDPMetadataResponseImpl)) {
			fulfilmentResponse = prepareFulfillmentResponse(fdpResponse);
		} else {
			fulfilmentResponse = fdpResponse;
		}
		return fulfilmentResponse;
	}

	/**
	 * This method sets the xmlresponse string to response.
	 * 
	 * @param fdpResponse
	 * @param responseXml
	 * @throws ExecutionFailedException
	 */
	private void addXmlStringToResponse(final FDPResponse fdpResponse, final String responseXml)
			throws ExecutionFailedException {
		final FDPMetadataResponseImpl fdpMetadataResponseImpl = (FDPMetadataResponseImpl) fdpResponse;

		if (fdpMetadataResponseImpl.getResponseString() != null
				&& !fdpMetadataResponseImpl.getResponseString().isEmpty()
				&& fdpMetadataResponseImpl.getResponseString()
						.get(fdpMetadataResponseImpl.getResponseString().size() - 1).getTLVOptions()
						.contains(TLVOptions.SESSION_TERMINATE)) {
			fdpMetadataResponseImpl.setTerminateSession(true);
		}
		if (!StringUtil.isNullOrEmpty(responseXml)) {
			fdpMetadataResponseImpl.setFulfillmentResponse(responseXml);
			fdpMetadataResponseImpl.setTerminateSession(true);
		}
	}

	protected abstract FDPResponse executeService(final FDPRequest fdpRequest, final Object... additionalInformations)
			throws ExecutionFailedException;

	/**
	 * This method will prepare the request string as
	 * "IVR"+"SPACE"+actualRequestString.
	 * 
	 * @param fdpRequest
	 * @return
	 * @throws ExecutionFailedException
	 */
	protected String getIVRRequestString(final FulfillmentRequestImpl fdpRequest) throws ExecutionFailedException {
		FDPLogger.debug(
				getCircleLogger(fdpRequest),
				getClass(),
				"getIVRRequestString",
				LoggerUtil.getRequestAppender(fdpRequest) + "The request message is : "
						+ fdpRequest.getRequestString());
		return BusinessConstants.IVR_CHANNEL + FDPConstant.SPACE + fdpRequest.getRequestString();
	}

	
	/**
	 * This method will handle special response to be sent in case of when node
	 * not found on DM.
	 * 
	 * @param fulfillmentRequestImpl
	 * @return
	 * @throws ExecutionFailedException
	 */
	protected FDPResponse handleNodeNotFound(final FulfillmentRequestImpl fulfillmentRequestImpl, FDPRequest fdpRequest)
			throws ExecutionFailedException {
		FulfillmentResponseCodes fulfillmentResponseCodes = FulfillmentResponseCodes.INVALID_PARAMETER;
		final String responseCode = String.valueOf(fulfillmentResponseCodes.getResponseCode());
		Object[] parameters = new Object[1];
		parameters[0] = fulfillmentRequestImpl.getRequestString();
		final String responseErrorString = getResponseDesciption(fulfillmentResponseCodes.getDescription(), parameters);
		final String systemType = FulfillmentSystemTypes.FDP.getValue();
		final ResponseError responseError = new ResponseError(responseCode, responseErrorString, null, systemType);
		FDPResponse fdpResponse = new FDPMetadataResponseImpl(Status.FAILURE, true, null, responseError);
		String errorDescription = new StringBuilder(FDPConstant.REQUEST_ID)
				.append(FDPConstant.LOGGER_KEY_VALUE_DELIMITER).append(fdpRequest.getRequestId())
				.append(FDPConstant.LOGGER_KEY_VALUE_DELIMITER).append("ERROR_CODE")
				.append(FDPConstant.LOGGER_KEY_VALUE_DELIMITER).append(responseCode)
				.append(FDPConstant.LOGGER_KEY_VALUE_DELIMITER).append(responseErrorString).toString();
		FDPLogger.error(getCircleRequestLogger(fulfillmentRequestImpl.getCircle()), getClass(), "handleNodeNotFound()",
				errorDescription);
		return fdpResponse;
	}
	
	/**
	 * This method will prepare the response description based on error code type.
	 * 
	 * @param description
	 * @param parameters
	 * @return
	 */
	private static String getResponseDesciption(final String description, final Object[] parameters) {
		return parameters.length > 0 ? String.format(description, parameters) : description;
	}

	/**
	 * This method gets the provisioning node.
	 * 
	 * @param fulfillmentRequestImpl
	 * @return
	 * @throws ExecutionFailedException
	 */
	protected FDPServiceProvisioningNode getNodeProductSP(final FulfillmentRequestImpl fulfillmentRequestImpl)
			throws ExecutionFailedException {
		FDPServiceProvisioningNode fdpServiceProvisioningNode = null;
		String requestString = getIVRRequestString(fulfillmentRequestImpl);
		final FDPNode fdpNode = DynamicMenuUtil.getAndUpdateFDPNode(fulfillmentRequestImpl.getCircle(), requestString,
				fulfillmentRequestImpl);
		if (fdpNode instanceof FDPServiceProvisioningNode) {
			final FDPServiceProvisioningNode serviceProvisioningNode = (FDPServiceProvisioningNode) fdpNode;
			if (serviceProvisioningNode instanceof ProductNode) {
				fdpServiceProvisioningNode = serviceProvisioningNode;
			}
		}
		return fdpServiceProvisioningNode;
	}

	/**
	 * This method is used to generate the transaction id to be used.
	 * 
	 * @return the transaction id.
	 */
	private Long generateTransactionId() {
		return generatorService.generateTransactionId();
	}

	/**
	 * Gets the service provisioning key.
	 * 
	 * @param productId
	 *            the product id
	 * @param serviceProvSubType
	 *            the shared account add consumer
	 * @param fdpRequest
	 *            the fdp request
	 * @return the service provisioning key
	 * @throws EvaluationFailedException
	 *             the evaluation failed exception
	 */
	protected void updateSPInRequestForProductId(final Long productId,
			final FDPServiceProvSubType serviceProvSubType, final FDPRequest fdpRequest)
			throws ExecutionFailedException {
		final String value = ServiceProvDataConverterUtil.getKeyForServiceProvMetaBag(productId,
				FDPServiceProvType.PRODUCT, serviceProvSubType);
		final FDPCacheable fdpSPCacheable = fdpMetaDataCache.getValue(new FDPMetaBag(fdpRequest.getCircle(),
				ModuleType.SP_PRODUCT, value));
		if (fdpSPCacheable == null) {
			throw new ExecutionFailedException(serviceProvSubType + " is not found for product " + productId);
		}

		try {
			if (fdpSPCacheable instanceof ServiceProvisioningRule) {
				RequestUtil.updateProductAndSPInWebRequest(fdpRequest, productId.toString(), value);
			}
		} catch (EvaluationFailedException efe) {
			throw new ExecutionFailedException("Product Not found in cache", efe);
		}
	}

	/**
	 * This method converts the response for as per the fulfillment service.
	 * 
	 * @param fdpResponseImpl
	 * @return
	 * @throws ExecutionFailedException
	 */
	private FDPResponse prepareFulfillmentResponse(final FDPResponse fdpResponseImpl) throws ExecutionFailedException {
		return new FDPMetadataResponseImpl(
				fdpResponseImpl.getExecutionStatus(), fdpResponseImpl.isTerminateSession(),
				fdpResponseImpl.getResponseString(), fdpResponseImpl.getResponseError());
	}
	
	/**
	 * This method executes the SP.
	 * 
	 * @param fdpRequest
	 * @return
	 * @throws ExecutionFailedException
	 */
	protected FDPResponse executeSP(final FDPRequest fdpRequest) throws ExecutionFailedException {
		return fdpFulfillmentServiceProvisioning.executeServiceProvisioning(fdpRequest);
	}
	
	/**
	 * This method sets the originTransactionId in request.
	 * 
	 * @param fdpRequest
	 */
	private void updateTransactionId(final FDPRequest fdpRequest) {
		if(fdpRequest instanceof FulfillmentRequestImpl) {
			FulfillmentRequestImpl fulfillmentRequestImpl = (FulfillmentRequestImpl) fdpRequest;
			fulfillmentRequestImpl.setOriginTransactionID(generateTransactionId());
		}
	}
	
	/**
	 * This method puts the consumer in response.
	 * 
	 * @param fdpResponse
	 * @param consumer
	 * @throws ExecutionFailedException
	 */
	protected void updateConsumerInResponse(final FDPResponse fdpResponse, final Consumer consumer)
			throws ExecutionFailedException {
		FDPMetadataResponseImpl fdpMetadataResponseImpl = (FDPMetadataResponseImpl) fdpResponse;
		fdpMetadataResponseImpl.putAuxiliaryRequestParameter(AuxRequestParam.VIEW_UCUT_RESPONSE,
				consumer);
	}

	/**
	 * This method is sued to get the circle logger.
	 * 
	 * @param fdpRequest
	 *            the request to get the circle logger.
	 * @return the circle logger.
	 */
	protected Logger getCircleLogger(final FDPRequest fdpRequest) {
		if (circleLogger == null) {
			circleLogger = LoggerUtil.getSummaryLoggerFromRequest(fdpRequest);
		}
		return circleLogger;
	}
	/**
	 * Gets the circle request logger.
	 * 
	 * @param fdpCircle
	 *            the fdp circle
	 * @return the circle request logger
	 * @throws ExecutionFailedException
	 *             the execution failed exception
	 */
	protected ch.qos.logback.classic.Logger getCircleRequestLogger(final FDPCircle fdpCircle) {
		return LoggerUtil.getRequestLogger(fdpCircle.getCircleName(), BusinessModuleType.IVR_NORTH);
	}
	/**
	 * Gets the service provisioning key.
	 * 
	 * @param productId
	 *            the product id
	 * @param serviceProvSubType
	 *            the shared account add consumer
	 * @param fdpRequest
	 *            the fdp request
	 * @return the service provisioning key
	 * @throws EvaluationFailedException
	 *             the evaluation failed exception
	 */
	protected void updateSPOtherInRequest(final Long spId,
			final FDPServiceProvSubType serviceProvSubType, final FDPRequest fdpRequest)
			throws ExecutionFailedException {
		final FDPCacheable serviceProvisioning = fdpMetaDataCache.getValue(new FDPMetaBag(fdpRequest.getCircle(),
				ModuleType.SP_OTHERS, spId));
		if (null != serviceProvisioning && serviceProvisioning instanceof ServiceProvisioningRule) {
			RequestUtil.updateProductInRequest(fdpRequest, serviceProvisioning, null);
		} else {
			throw new ExecutionFailedException(serviceProvSubType + " is not found for OtherSPId " + spId);
		}
	}
	
	/**
	 * This method will check whether if consumer msisdn is prepaid or not by executing the GAD.
	 * 
	 * @param fdpRequest
	 * @param consumerMsisdn
	 * @return
	 * @throws ExecutionFailedException
	 */
	protected String checkIfConsumerMsisdnValid(final FDPRequest fdpRequest, final String consumerMsisdn)
			throws ExecutionFailedException {
		return FulfillmentUtil.checkIfInputMsisdnIsValid(fdpRequest, consumerMsisdn);
	}
	
	/**
	 * This method will prepare the notification text in response.
	 * 
	 * @param fdpResponse
	 * @param notificationText
	 */
	protected void updateNotificationTextInResponse(final FDPResponse fdpResponse, final String notificationText) {
		final FDPMetadataResponseImpl fdpMetadataResponseImpl = (FDPMetadataResponseImpl) fdpResponse;
		ResponseMessage responseMessage = new ResponseMessageImpl(ChannelType.IVR, null, Arrays.asList(TLVOptions.SESSION_TERMINATE), notificationText);
		fdpMetadataResponseImpl.setResponseString(Arrays.asList(responseMessage));
	}
	
	protected void pushSMSNotificationAgainstFulfillmentResponseCode(final FDPRequest fdpRequest, final FulfillmentResponseCodes fulfillmentResponseCode){
		final String notificationIdKey = fulfillmentResponseCode.name()+"_NOTIFICATION_ID";
		final String notificationIdString = fdpRequest.getCircle().getConfigurationKeyValueMap()
				.get(notificationIdKey);
		FDPLogger.info(this.getCircleLogger(fdpRequest), AbstractFDPFulFillmentServiceImpl.class, "pushSMSNotificationAgainstFulfillmentResponseCode()", 
				LoggerUtil.getRequestAppender(fdpRequest) + "NOTIF_KEY"
						+ FDPConstant.LOGGER_KEY_VALUE_DELIMITER + notificationIdKey
						+ FDPConstant.LOGGER_DELIMITER + "NOTIF_ID" + FDPConstant.LOGGER_KEY_VALUE_DELIMITER
						+ notificationIdString+FDPConstant.LOGGER_DELIMITER +"MSISDN"+FDPConstant.LOGGER_KEY_VALUE_DELIMITER
						+ fdpRequest.getSubscriberNumber());
		if(notificationIdString == null){
			return;
		}
		
		Long notificationId = null;
		try{
			notificationId = Long.parseLong(notificationIdString);
		} catch(NumberFormatException ex){
			FDPLogger.error(this.getCircleLogger(fdpRequest), AbstractFDPFulFillmentServiceImpl.class,
					"pushSMSNotificationAgainstFulfillmentResponseCode()", LoggerUtil.getRequestAppender(fdpRequest)+" ERROR: "+ex.getMessage(), ex);
			ex.printStackTrace();
			return;
		}
		
		try {
			String notificationText = NotificationUtil.createNotificationText(fdpRequest, notificationId, this.getCircleLogger(fdpRequest));
			NotificationUtil.sendNotification(fdpRequest.getSubscriberNumber(), ChannelType.SMS,
					fdpRequest.getCircle(), notificationText, fdpRequest.getRequestId(), false);
			FDPLogger.info(this.getCircleLogger(fdpRequest), AbstractFDPFulFillmentServiceImpl.class, "pushSMSNotificationAgainstFulfillmentResponseCode()", 
					LoggerUtil.getRequestAppender(fdpRequest) + "FULLFILMENT_SMS: "
							+ FDPConstant.LOGGER_KEY_VALUE_DELIMITER + notificationText
							+ FDPConstant.LOGGER_DELIMITER + "FULLFILMENT_SMS_STATUS" + FDPConstant.LOGGER_KEY_VALUE_DELIMITER
							+ "SUCCESS"+FDPConstant.LOGGER_DELIMITER +"MSISDN"+FDPConstant.LOGGER_KEY_VALUE_DELIMITER
							+ fdpRequest.getSubscriberNumber());
		} catch (NotificationFailedException e) {
			FDPLogger.info(this.getCircleLogger(fdpRequest), AbstractFDPFulFillmentServiceImpl.class, "pushSMSNotificationAgainstFulfillmentResponseCode()", 
					LoggerUtil.getRequestAppender(fdpRequest) + "FULLFILMENT_SMS_STATUS" + FDPConstant.LOGGER_KEY_VALUE_DELIMITER
							+ "FAILURE"+FDPConstant.LOGGER_DELIMITER +"MSISDN"+FDPConstant.LOGGER_KEY_VALUE_DELIMITER
							+ fdpRequest.getSubscriberNumber());
			FDPLogger.error(this.getCircleLogger(fdpRequest), AbstractFDPFulFillmentServiceImpl.class,
					"pushSMSNotificationAgainstFulfillmentResponseCode()", LoggerUtil.getRequestAppender(fdpRequest)+" ERROR: "+e.getMessage(), e);
			e.printStackTrace();
		}
	}
	
	/**
	 * This method will set the external system logical name.
	 * 
	 * @param fdpRequest
	 */
	protected void updateAuxForLogicalName(final FDPRequest fdpRequest) {
		final String auxLogicalName = fdpRequest.getCircle().getConfigurationKeyValueMap()
				.get(ConfigurationKey.SBB_EXTERNAL_SYSTEM_FULFILLEMENT_LOGICAL_NAME);
		if(null != auxLogicalName && fdpRequest instanceof FDPRequestImpl) {
			FDPRequestImpl fdpRequestImpl = (FDPRequestImpl) fdpRequest;
			fdpRequestImpl.putAuxiliaryRequestParameter(AuxRequestParam.EXTERNAL_SYSTEM_LOGICAL_NAME, auxLogicalName);
		}
	}
	
	/**
	 * This method will update the response.
	 * 
	 * @param fdpResponse
	 * @param fulfillmentResponseCodes
	 */
	protected void updateErrorInResponse(final FDPResponse fdpResponse,
			final FulfillmentResponseCodes fulfillmentResponseCodes) {
		FDPMetadataResponseImpl fdpMetadataResponseImpl = (FDPMetadataResponseImpl) fdpResponse;
		ResponseError responseError = new ResponseError(fulfillmentResponseCodes.getResponseCode().toString(),
				fulfillmentResponseCodes.getDescription(), fulfillmentResponseCodes.getSystemType(),
				fulfillmentResponseCodes.getSystemType());
		fdpMetadataResponseImpl.setResponseError(responseError);
	}
	
	/**
	 * This method will execute the command and save in request object.
	 * 
	 * @param fdpRequest
	 * @param commandName
	 * @return
	 * @throws ExecutionFailedException
	 */
	protected boolean executeCommand(final FDPRequest fdpRequest, final String commandName)
			throws ExecutionFailedException {
		boolean isExecuted = false;
		final FDPCacheable fdpCacheable = (FDPCommand) ApplicationConfigUtil.getMetaDataCache().getValue(
				new FDPMetaBag(fdpRequest.getCircle(), ModuleType.COMMAND, commandName));
		if (null != fdpCacheable && fdpCacheable instanceof FDPCommand) {
			final FDPCommand fdpCommand = (FDPCommand) fdpCacheable;
			isExecuted = (Status.SUCCESS.equals(fdpCommand.execute(fdpRequest)));
			fdpRequest.addExecutedCommand(fdpCommand);
		}
		return isExecuted;
	}
}
