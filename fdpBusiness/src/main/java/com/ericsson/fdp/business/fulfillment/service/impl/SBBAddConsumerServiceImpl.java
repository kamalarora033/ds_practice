package com.ericsson.fdp.business.fulfillment.service.impl;

import javax.ejb.Stateless;

import com.ericsson.fdp.business.enums.AuxRequestParam;
import com.ericsson.fdp.business.enums.ivr.FulfillmentParameters;
import com.ericsson.fdp.business.enums.ivr.FulfillmentResponseCodes;
import com.ericsson.fdp.business.request.impl.FDPMetadataResponseImpl;
import com.ericsson.fdp.business.request.impl.FulfillmentRequestImpl;
import com.ericsson.fdp.business.util.FulfillmentUtil;
import com.ericsson.fdp.common.constants.FDPConstant;
import com.ericsson.fdp.common.enums.Status;
import com.ericsson.fdp.core.exception.ExecutionFailedException;
import com.ericsson.fdp.core.request.FDPRequest;
import com.ericsson.fdp.core.request.FDPResponse;

@Stateless
public class SBBAddConsumerServiceImpl extends AbstractFDPFulFillmentServiceImpl {

	private final String SBB_VIEW_CONSUMER_COMMAND = FDPConstant.SBB_GETDETAILS_COMMAND;

	/** SBB_ADD_CONSUMER_COMMAND **/
	private final String SBB_ADD_CONSUMER_COMMAND = "Add_Consumer";

	@Override
	protected FDPResponse executeService(FDPRequest fdpRequest, Object... additionalInformations)
			throws ExecutionFailedException {
		final FDPMetadataResponseImpl metaResponse = new FDPMetadataResponseImpl(Status.FAILURE, true, null, null);
		final FulfillmentRequestImpl fulfillmentRequestImpl = (FulfillmentRequestImpl) fdpRequest;
		updateAuxForLogicalName(fdpRequest);
		fulfillmentRequestImpl.putAuxiliaryRequestParameter(AuxRequestParam.PROVIDER_MSISDN, fulfillmentRequestImpl
				.getSubscriberNumber().toString());
		final String consumerMsisdn = fulfillmentRequestImpl
				.getCommandInputParams(FulfillmentParameters.CONSUMER_MSISDN);
		boolean stopExecution = false;
		boolean isNewType = false;
		stopExecution = consumerMsisdn.equals(fulfillmentRequestImpl
				.getSubscriberNumber().toString());
		//Check if consumer and provider msisdn are same.
		if(stopExecution) {
			updateErrorInResponse(metaResponse, FulfillmentResponseCodes.CONSNUMER_MSISDN_NOT_VALID);
			//Send Notification through SMS
			pushSMSNotificationAgainstFulfillmentResponseCode(fdpRequest, FulfillmentResponseCodes.CONSNUMER_MSISDN_NOT_VALID);
		}
		// Execute GetDetails..
		if (!stopExecution && !executeCommand(fdpRequest, SBB_VIEW_CONSUMER_COMMAND)) {
			metaResponse.setResponseError(fdpRequest.getExecutedCommand(SBB_VIEW_CONSUMER_COMMAND).getResponseError());
			stopExecution = true;
		}
		// Check for Provider Type.
		if (!stopExecution
				&& FDPConstant.SHARED_BONUD_BUNDLE_CONSUMER_TYPE.equals(
						FulfillmentUtil.getCommandOutput(fdpRequest, "type", SBB_VIEW_CONSUMER_COMMAND, false)
								.getValue().toString())) {
			updateErrorInResponse(metaResponse, FulfillmentResponseCodes.MSISDN_NOT_PROVIDER_TYPE);
			stopExecution = true;
			//isProviderType = true;
			//Send Notification through SMS
			pushSMSNotificationAgainstFulfillmentResponseCode(fdpRequest, FulfillmentResponseCodes.MSISDN_NOT_PROVIDER_TYPE);
		}
		//Check if new user or not.
		if(!stopExecution
				&& FDPConstant.SHARED_BONUS_BUNDLE_PROVIDER_TYPE.get(1).equals(
						FulfillmentUtil.getCommandOutput(fdpRequest, "type", SBB_VIEW_CONSUMER_COMMAND, false)
								.getValue().toString())) {
			isNewType = true;
		}
		// Check if consumer already present.
		if (!stopExecution && FulfillmentUtil.getConsumerList(fdpRequest, false).contains(consumerMsisdn)) {
			updateErrorInResponse(metaResponse, FulfillmentResponseCodes.CONSUMER_ALREADY_PRESENT);
			stopExecution = true;
			//Send Notification through SMS
			pushSMSNotificationAgainstFulfillmentResponseCode(fdpRequest, FulfillmentResponseCodes.CONSUMER_ALREADY_PRESENT);
		}
		//Check consumer limit.
		if(!stopExecution && FulfillmentUtil.isSharedBonusBundleConsumerLimitReached(fdpRequest)) {
			updateErrorInResponse(metaResponse, FulfillmentResponseCodes.CONSUMER_LIMIT_REACHED);
			stopExecution = true;
			//Send Notification through SMS
			pushSMSNotificationAgainstFulfillmentResponseCode(fdpRequest, FulfillmentResponseCodes.CONSUMER_LIMIT_REACHED);
		}
		// Check for consumer.
		String consumerValidationText = null;
		if (!stopExecution
				&& (null != (consumerValidationText = checkIfConsumerMsisdnValid(fdpRequest, consumerMsisdn)))) {
			updateErrorInResponse(metaResponse, FulfillmentResponseCodes.CONSNUMER_MSISDN_NOT_VALID);
			updateNotificationTextInResponse(metaResponse, consumerValidationText);
			stopExecution = true;
			//Send Notification through SMS
			pushSMSNotificationAgainstFulfillmentResponseCode(fdpRequest, FulfillmentResponseCodes.CONSNUMER_MSISDN_NOT_VALID);
		}
		//Check if consumer is new only.
		if (!stopExecution && !FulfillmentUtil.isConsumerTypeNew(fdpRequest, consumerMsisdn)) {
			updateErrorInResponse(metaResponse, FulfillmentResponseCodes.CONSUMER_ALREADY_PRESENT);
			stopExecution = true;
			//Send Notification through SMS
			pushSMSNotificationAgainstFulfillmentResponseCode(fdpRequest, FulfillmentResponseCodes.CONSUMER_ALREADY_PRESENT);
		}
		// Execute ADD Consumer.
		if (!stopExecution && udpateAux(fdpRequest,isNewType)) {
			if (executeCommand(fdpRequest, SBB_ADD_CONSUMER_COMMAND)) {
				updateNotificationTextInResponse(metaResponse, fdpRequest.getExecutedCommand(SBB_ADD_CONSUMER_COMMAND)
						.getOutputParam("responseDesc").getValue().toString());
				metaResponse.setExecutionStatus(Status.SUCCESS);
			} else {
				metaResponse.setResponseError(fdpRequest.getExecutedCommand(SBB_ADD_CONSUMER_COMMAND)
						.getResponseError());
			}
		}
		return metaResponse;
	}

	/**
	 * This method udpates the AUX params.
	 * 
	 * @param fdpRequest
	 */
	private boolean udpateAux(final FDPRequest fdpRequest, final boolean isNewType) {
		boolean isUpdated = false;
		if (null != fdpRequest && fdpRequest instanceof FulfillmentRequestImpl) {
			final FulfillmentRequestImpl fulfillmentRequestImpl = (FulfillmentRequestImpl) fdpRequest;
			if(!isNewType) {
			fulfillmentRequestImpl.putAuxiliaryRequestParameter(AuxRequestParam.ACTION,
					FDPConstant.SHARED_BONUD_BUNDLE_CONSUMER_TYPE);
			} else {
				fulfillmentRequestImpl.putAuxiliaryRequestParameter(AuxRequestParam.ACTION,
						FDPConstant.SHARED_BONUS_BUNDLE_PROVIDER_TYPE.get(1));
			}
			fulfillmentRequestImpl.putAuxiliaryRequestParameter(AuxRequestParam.CONSUMER_MSISDN,
					fulfillmentRequestImpl.getCommandInputParams(FulfillmentParameters.CONSUMER_MSISDN));
			fulfillmentRequestImpl.putAuxiliaryRequestParameter(AuxRequestParam.PROVIDER_MSISDN, fulfillmentRequestImpl
					.getSubscriberNumber().toString());
			isUpdated = true;
		}
		return isUpdated;
	}
}
