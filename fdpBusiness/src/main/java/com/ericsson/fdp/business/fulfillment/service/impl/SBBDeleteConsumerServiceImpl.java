package com.ericsson.fdp.business.fulfillment.service.impl;

import java.util.List;

import javax.ejb.Stateless;

import com.ericsson.fdp.business.command.param.impl.CommandParamOutput;
import com.ericsson.fdp.business.enums.AuxRequestParam;
import com.ericsson.fdp.business.enums.ivr.FulfillmentParameters;
import com.ericsson.fdp.business.enums.ivr.FulfillmentResponseCodes;
import com.ericsson.fdp.business.request.impl.FDPMetadataResponseImpl;
import com.ericsson.fdp.business.request.impl.FulfillmentRequestImpl;
import com.ericsson.fdp.business.util.FulfillmentUtil;
import com.ericsson.fdp.common.constants.FDPConstant;
import com.ericsson.fdp.common.enums.Status;
import com.ericsson.fdp.common.util.StringUtil;
import com.ericsson.fdp.core.exception.ExecutionFailedException;
import com.ericsson.fdp.core.request.FDPRequest;
import com.ericsson.fdp.core.request.FDPResponse;

@Stateless
public class SBBDeleteConsumerServiceImpl extends AbstractFDPFulFillmentServiceImpl{
	
	/** SBB_ADD_CONSUMER_COMMAND **/
	private final String SBB_DEL_CONSUMER_COMMAND="Delete_Consumer";

	private final String SBB_VIEW_CONSUMER_COMMAND=FDPConstant.SBB_GETDETAILS_COMMAND;

	@Override
	protected FDPResponse executeService(FDPRequest fdpRequest,
			Object... additionalInformations) throws ExecutionFailedException {
		final FDPMetadataResponseImpl metaResponse = new FDPMetadataResponseImpl(Status.FAILURE,
				true, null, null);
		final FulfillmentRequestImpl fulfillmentRequestImpl = (FulfillmentRequestImpl) fdpRequest;
		updateAuxForLogicalName(fdpRequest);
		final boolean isConsumerSelfDeleteRequest  = FulfillmentUtil.isConsumerSelfDeleteRequest(fdpRequest);
		final String consumerMsisdn = fulfillmentRequestImpl
				.getCommandInputParams(FulfillmentParameters.CONSUMER_MSISDN);
		fulfillmentRequestImpl.putAuxiliaryRequestParameter(AuxRequestParam.PROVIDER_MSISDN, fulfillmentRequestImpl
				.getSubscriberNumber().toString());
		fulfillmentRequestImpl.putAuxiliaryRequestParameter(AuxRequestParam.SBB_DELETE_ALLOWED, Boolean.TRUE);
		// Execute GetDetails..
		boolean stopExecution = false;
		boolean isGetDetailExecute = executeCommand(fdpRequest, SBB_VIEW_CONSUMER_COMMAND);
		if (!isGetDetailExecute) {
			metaResponse.setResponseError(fdpRequest.getExecutedCommand(SBB_VIEW_CONSUMER_COMMAND).getResponseError());
			stopExecution = true;
		}
		// Check for Provider Type.
		if (!stopExecution
				&& !(isConsumerSelfDeleteRequest ? FDPConstant.SHARED_BONUD_BUNDLE_CONSUMER_TYPE
						: FDPConstant.SHARED_BONUS_BUNDLE_PROVIDER_TYPE.get(0)).equals(
						FulfillmentUtil.getCommandOutput(fdpRequest, "type", SBB_VIEW_CONSUMER_COMMAND, false)
								.getValue().toString())) {
			FulfillmentResponseCodes code = isConsumerSelfDeleteRequest ? FulfillmentResponseCodes.CONSNUMER_MSISDN_NOT_VALID
					: FulfillmentResponseCodes.MSISDN_NOT_PROVIDER_TYPE;
			updateErrorInResponse(metaResponse, code);
			stopExecution = true;
			//Send SMS Notification
			pushSMSNotificationAgainstFulfillmentResponseCode(fdpRequest, code);
		}
		// Check if consumer already present.
		if (!stopExecution && !isDeleteAllowed(fdpRequest, consumerMsisdn,isConsumerSelfDeleteRequest)) {
			updateErrorInResponse(metaResponse, FulfillmentResponseCodes.CONSUMER_DELETE_NOT_ALLOWED);
			stopExecution = true;
			//Send SMS notification
			pushSMSNotificationAgainstFulfillmentResponseCode(fdpRequest, FulfillmentResponseCodes.CONSUMER_DELETE_NOT_ALLOWED);
		}
		// Check for consumer.
		/*String consumerValidationText = null;
		if (!stopExecution
				&& (null != (consumerValidationText = checkIfConsumerMsisdnValid(fdpRequest, consumerMsisdn)))) {
			updateErrorInResponse(metaResponse, FulfillmentResponseCodes.CONSNUMER_MSISDN_NOT_VALID);
			updateNotificationTextInResponse(metaResponse, consumerValidationText);
			stopExecution = true;
		}*/
		// Execute ADD Consumer.
		if (!stopExecution && udpateAux(fdpRequest,isConsumerSelfDeleteRequest)) {
			if (executeCommand(fdpRequest, SBB_DEL_CONSUMER_COMMAND)) {
				updateNotificationTextInResponse(metaResponse, fdpRequest.getExecutedCommand(SBB_DEL_CONSUMER_COMMAND)
						.getOutputParam("responseDesc").getValue().toString());
				metaResponse.setExecutionStatus(Status.SUCCESS);
			} else {
				metaResponse.setResponseError(fdpRequest.getExecutedCommand(SBB_DEL_CONSUMER_COMMAND)
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
	private boolean udpateAux(final FDPRequest fdpRequest, final boolean isConsumerSelfDeleteRequest) {
		boolean isUpdate = false;
		if (null != fdpRequest && fdpRequest instanceof FulfillmentRequestImpl) {
			final FulfillmentRequestImpl fulfillmentRequestImpl = (FulfillmentRequestImpl) fdpRequest;
			if (isConsumerSelfDeleteRequest) {
				fulfillmentRequestImpl.putAuxiliaryRequestParameter(AuxRequestParam.CONSUMER_MSISDN,
						fulfillmentRequestImpl.getSubscriberNumber().toString());
				fulfillmentRequestImpl.putAuxiliaryRequestParameter(AuxRequestParam.PROVIDER_MSISDN,
						fdpRequest.getExecutedCommand(FDPConstant.SBB_GETDETAILS_COMMAND).getOutputParam("provider").getValue().toString());
			} else {
				fulfillmentRequestImpl.putAuxiliaryRequestParameter(AuxRequestParam.CONSUMER_MSISDN,
						fulfillmentRequestImpl.getCommandInputParams(FulfillmentParameters.CONSUMER_MSISDN));
				fulfillmentRequestImpl.putAuxiliaryRequestParameter(AuxRequestParam.PROVIDER_MSISDN, fulfillmentRequestImpl
						.getSubscriberNumber().toString());
			}
			isUpdate = true;
		}
		return isUpdate;
	} 
	
	/**
	 * This method will check the deletion allowed.
	 * 
	 * @param fdpRequest
	 * @return
	 * @throws ExecutionFailedException
	 */
	private boolean isDeleteAllowed(final FDPRequest fdpRequest, final String consumerMsisdn,
			final boolean isConsumerSelfDeleteRequest) throws ExecutionFailedException {
		boolean isDeletAllowed = true;
		final CommandParamOutput commandParamOutput = FulfillmentUtil.getCommandOutput(fdpRequest, "type",
				FDPConstant.SBB_GETDETAILS_COMMAND, true);
		if (null != commandParamOutput
				&& !StringUtil.isNullOrEmpty(commandParamOutput.getValue().toString())
				&& (isConsumerSelfDeleteRequest ? FDPConstant.SHARED_BONUD_BUNDLE_CONSUMER_TYPE
						: FDPConstant.SHARED_BONUS_BUNDLE_PROVIDER_TYPE.get(0)).equals(commandParamOutput.getValue()
						.toString())) {
			final List<String> consumerList = !isConsumerSelfDeleteRequest ? FulfillmentUtil.getConsumerList(
					fdpRequest, true) : null;
			isDeletAllowed = (null != consumerList) ? consumerList.contains(consumerMsisdn)
					: (isConsumerSelfDeleteRequest ? true : isDeletAllowed);
		}
		return isDeletAllowed;
	}
}
