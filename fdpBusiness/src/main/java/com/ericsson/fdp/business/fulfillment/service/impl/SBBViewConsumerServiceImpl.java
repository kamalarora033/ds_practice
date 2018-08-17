package com.ericsson.fdp.business.fulfillment.service.impl;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.ejb.Stateless;

import com.ericsson.fdp.FDPCacheable;
import com.ericsson.fdp.business.enums.AuxRequestParam;
import com.ericsson.fdp.business.enums.ivr.FulfillmentActionTypes;
import com.ericsson.fdp.business.enums.ivr.FulfillmentResponseCodes;
import com.ericsson.fdp.business.request.impl.FDPMetadataResponseImpl;
import com.ericsson.fdp.business.request.impl.FulfillmentRequestImpl;
import com.ericsson.fdp.business.response.fulfillment.xml.Consumer;
import com.ericsson.fdp.business.response.fulfillment.xml.Provider;
import com.ericsson.fdp.business.util.FulfillmentUtil;
import com.ericsson.fdp.common.constants.FDPConstant;
import com.ericsson.fdp.common.enums.ModuleType;
import com.ericsson.fdp.common.enums.Status;
import com.ericsson.fdp.common.util.StringUtil;
import com.ericsson.fdp.core.command.FDPCommand;
import com.ericsson.fdp.core.exception.ExecutionFailedException;
import com.ericsson.fdp.core.request.FDPRequest;
import com.ericsson.fdp.core.request.FDPResponse;
import com.ericsson.fdp.core.request.ResponseError;
import com.ericsson.fdp.core.serviceprovisioning.bean.FDPMetaBag;
import com.ericsson.fdp.core.utils.ApplicationConfigUtil;

@Stateless
public class SBBViewConsumerServiceImpl extends AbstractFDPFulFillmentServiceImpl{

	private final String SBB_VIEW_CONSUMER_COMMAND=FDPConstant.SBB_GETDETAILS_COMMAND;
	
	@Override
	protected FDPResponse executeService(FDPRequest fdpRequest, Object... additionalInformations)
			throws ExecutionFailedException {
		final FulfillmentRequestImpl fulfillmentRequestImpl = (FulfillmentRequestImpl) fdpRequest;
		final FDPMetadataResponseImpl metaResponse = new FDPMetadataResponseImpl(Status.FAILURE,
				true, null, null);
		final FDPCacheable fdpCacheable = (FDPCommand) ApplicationConfigUtil.getMetaDataCache().getValue(
				new FDPMetaBag(fdpRequest.getCircle(), ModuleType.COMMAND, SBB_VIEW_CONSUMER_COMMAND));
		if(null != fdpCacheable && fdpCacheable instanceof FDPCommand) {
			FDPCommand fdpCommand = (FDPCommand) fdpCacheable;
			updateAuxForLogicalName(fdpRequest);
			udpateAux(fdpRequest);
			if (Status.SUCCESS.equals(fdpCommand.execute(fdpRequest))) {
				fdpRequest.addExecutedCommand(fdpCommand);
				switch (fulfillmentRequestImpl.getActionTypes()) {
				case SHARED_BONUS_BUNDLE_VIEW_CONSUMER:
					 viewConsumerList(fdpRequest, metaResponse, fdpCommand);
					break;
				case SHARED_BONUS_BUNDLE_VIEW_PROVIDER:
					viewProvider(fdpRequest, metaResponse, fdpCommand);
					break;
				default:
					throw new ExecutionFailedException("Invalid Action:"+fulfillmentRequestImpl.getActionTypes());
				}
			} else {
				final ResponseError responseError = fdpCommand.getResponseError();
				metaResponse.setResponseError(responseError);
			}
 		}
		return metaResponse;
	}

	/**
	 * This method udpates the AUX params.
	 * 
	 * @param fdpRequest
	 */
	private void udpateAux(final FDPRequest fdpRequest) {
		if (null != fdpRequest && fdpRequest instanceof FulfillmentRequestImpl) {
			final FulfillmentRequestImpl fulfillmentRequestImpl = (FulfillmentRequestImpl) fdpRequest;
			fulfillmentRequestImpl.putAuxiliaryRequestParameter(AuxRequestParam.PROVIDER_MSISDN, fulfillmentRequestImpl
					.getSubscriberNumber().toString());
		}
	}
	
	/**
	 * This method will update the consumer details in aux for response creation.
	 * 
	 * @param fdpRequest
	 * @param fdpCommand
	 */
	private void updateConsumerInAux(final FDPResponse fdpResponse, final FDPCommand fdpCommand, final FulfillmentActionTypes fulfillmentActionTypes) {
		switch (fulfillmentActionTypes) {
		case SHARED_BONUS_BUNDLE_VIEW_CONSUMER:
				updateConsumerDetailsInAux(fdpResponse, fdpCommand);
			break;
		case SHARED_BONUS_BUNDLE_VIEW_PROVIDER:
			updateProviderDetailsInAux(fdpResponse, fdpCommand);
			break;
		default:
			break;
		}
	}
	
	/**
	 * This method will get the consumer list.
	 * 
	 * @param fdpRequest
	 * @param metaResponse
	 * @param fdpCommand
	 * @throws ExecutionFailedException
	 */
	private void viewConsumerList(FDPRequest fdpRequest, final FDPMetadataResponseImpl metaResponse,
			FDPCommand fdpCommand) throws ExecutionFailedException {
		if (FulfillmentUtil.isSharedBonudBundleProviderOrNew(fdpRequest,
				Arrays.asList(FDPConstant.SHARED_BONUS_BUNDLE_PROVIDER_TYPE.get(0)))) {
			updateNotificationTextInResponse(metaResponse, fdpCommand.getOutputParam("responseDesc").getValue()
					.toString());
			metaResponse.setExecutionStatus(Status.SUCCESS);
			final FulfillmentRequestImpl fulfillmentRequestImpl = (FulfillmentRequestImpl) fdpRequest;
			updateConsumerInAux(metaResponse, fdpCommand,fulfillmentRequestImpl.getActionTypes());
		} else {
			updateErrorInResponse(metaResponse, FulfillmentResponseCodes.MSISDN_NOT_PROVIDER_TYPE);
			//Send SMS Notification
			//pushSMSNotificationAgainstFulfillmentResponseCode(fdpRequest, FulfillmentResponseCodes.MSISDN_NOT_PROVIDER_TYPE);
		}
	}

	/**
	 * This method will get the provider.
	 * 
	 * @param fdpRequest
	 * @param metaResponse
	 * @param fdpCommand
	 * @throws ExecutionFailedException 
	 */
	private void viewProvider(FDPRequest fdpRequest, FDPMetadataResponseImpl metaResponse, FDPCommand fdpCommand) throws ExecutionFailedException {
		if (FulfillmentUtil.isSharedBonudBundleProviderOrNew(fdpRequest,
				Arrays.asList(FDPConstant.SHARED_BONUD_BUNDLE_CONSUMER_TYPE))) {
			updateNotificationTextInResponse(metaResponse, fdpCommand.getOutputParam("responseDesc").getValue()
					.toString());
			metaResponse.setExecutionStatus(Status.SUCCESS);
			final FulfillmentRequestImpl fulfillmentRequestImpl = (FulfillmentRequestImpl) fdpRequest;
			updateConsumerInAux(metaResponse, fdpCommand,fulfillmentRequestImpl.getActionTypes());
		} else {
			updateErrorInResponse(metaResponse, FulfillmentResponseCodes.CONSNUMER_MSISDN_NOT_VALID);
			//Send SMS Notification
			//pushSMSNotificationAgainstFulfillmentResponseCode(fdpRequest, FulfillmentResponseCodes.CONSNUMER_MSISDN_NOT_VALID);
		}
	}
	
	/**
	 * This method will update the consumer details in aux.
	 * 
	 * @param fdpResponse
	 * @param fdpCommand
	 */
	private void updateConsumerDetailsInAux(final FDPResponse fdpResponse, final FDPCommand fdpCommand) {
		if(null != fdpCommand.getOutputParam("consumerList")) {
			final String consumerList = fdpCommand.getOutputParam("consumerList").getValue().toString();
			if (!StringUtil.isNullOrEmpty(consumerList)) {
				final List<Consumer> consumers = new ArrayList<Consumer>();
				for (final String consumerMsisdn : consumerList.split(FDPConstant.COMMA)) {
					final Consumer consumer = new Consumer();
					consumer.setMsisdn(consumerMsisdn);
					consumers.add(consumer);
				}
				final FDPMetadataResponseImpl fdpMetadataResponseImpl = (FDPMetadataResponseImpl) fdpResponse;
				fdpMetadataResponseImpl.putAuxiliaryRequestParameter(AuxRequestParam.CONSUMER_MSISDN_LIST, consumers);
			}
		}
	}

	/**
	 * This method will update the provider details in AUX.
	 * 
	 * @param fdpResponse
	 * @param fdpCommand
	 */
	private void updateProviderDetailsInAux(final FDPResponse fdpResponse, final FDPCommand fdpCommand) {
		if (null != fdpCommand.getOutputParam("provider")) {
			final String providerMsisdn = fdpCommand.getOutputParam("provider").getValue().toString();
			if (!StringUtil.isNullOrEmpty(providerMsisdn)) {
				final List<Provider> providers = new ArrayList<Provider>();
				final Provider provider = new Provider();
				provider.setMsisdn(providerMsisdn);
				providers.add(provider);
				final FDPMetadataResponseImpl fdpMetadataResponseImpl = (FDPMetadataResponseImpl) fdpResponse;
				fdpMetadataResponseImpl.putAuxiliaryRequestParameter(AuxRequestParam.PROVIDER_MSISDN_LIST, providers);
			}
		}
	}
}
