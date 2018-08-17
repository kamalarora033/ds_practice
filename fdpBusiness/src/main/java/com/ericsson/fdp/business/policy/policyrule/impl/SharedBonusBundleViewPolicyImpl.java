package com.ericsson.fdp.business.policy.policyrule.impl;

import java.text.DecimalFormat;
import java.util.Arrays;

import com.ericsson.fdp.FDPCacheable;
import com.ericsson.fdp.business.command.param.impl.CommandParamOutput;
import com.ericsson.fdp.business.enums.AuxRequestParam;
import com.ericsson.fdp.business.enums.Command;
import com.ericsson.fdp.business.enums.ConfigurationKey;
import com.ericsson.fdp.business.enums.PolicyStatus;
import com.ericsson.fdp.business.enums.RequestMetaValuesKey;
import com.ericsson.fdp.business.enums.TLVOptions;
import com.ericsson.fdp.business.enums.ivr.FulfillmentResponseCodes;
import com.ericsson.fdp.business.exception.NotificationFailedException;
import com.ericsson.fdp.business.node.impl.SpecialMenuNode;
import com.ericsson.fdp.business.policy.policyrule.PolicyRule;
import com.ericsson.fdp.business.request.FDPPolicyResponse;
import com.ericsson.fdp.business.request.impl.FDPPolicyResponseImpl;
import com.ericsson.fdp.business.request.impl.FDPResponseImpl;
import com.ericsson.fdp.business.util.FulfillmentUtil;
import com.ericsson.fdp.business.util.LoggerUtil;
import com.ericsson.fdp.business.util.NotificationUtil;
import com.ericsson.fdp.business.util.ResponseUtil;
import com.ericsson.fdp.business.util.TariffEnquiryNotificationUtil;
import com.ericsson.fdp.common.constants.FDPConstant;
import com.ericsson.fdp.common.enums.ChannelType;
import com.ericsson.fdp.common.enums.ModuleType;
import com.ericsson.fdp.common.enums.Status;
import com.ericsson.fdp.common.util.StringUtil;
import com.ericsson.fdp.core.command.FDPCommand;
import com.ericsson.fdp.core.exception.ExecutionFailedException;
import com.ericsson.fdp.core.request.FDPRequest;
import com.ericsson.fdp.core.request.FDPResponse;
import com.ericsson.fdp.core.request.impl.FDPRequestImpl;
import com.ericsson.fdp.core.serviceprovisioning.bean.FDPMetaBag;
import com.ericsson.fdp.core.utils.ApplicationConfigUtil;
import com.ericsson.fdp.dao.enums.FDPServiceProvSubType;

public class SharedBonusBundleViewPolicyImpl extends AbstractPolicyRule implements PolicyRule {

	/**
	 * The class serial version UID.
	 */
	private static final long serialVersionUID = -6505071481838056130L;

	@Override
	public FDPResponse displayRule(FDPRequest fdpRequest) throws ExecutionFailedException {
		FDPResponse fdpResponse = null;
		if (fdpRequest instanceof FDPRequestImpl && null != fdpRequest.getValueFromRequest(RequestMetaValuesKey.NODE)
				&& fdpRequest.getValueFromRequest(RequestMetaValuesKey.NODE) instanceof SpecialMenuNode) {
			final SpecialMenuNode specialMenuNode = (SpecialMenuNode) fdpRequest
					.getValueFromRequest(RequestMetaValuesKey.NODE);
			FDPRequestImpl fdpRequestImpl = (FDPRequestImpl) fdpRequest;
			fdpRequestImpl.putAuxiliaryRequestParameter(AuxRequestParam.PROVIDER_MSISDN, fdpRequest
					.getSubscriberNumber().toString());
			switch (specialMenuNode.getSpecialMenuNodeType()) {
			case SHARED_BUNDLE_VIEW_CONSUMER:
				fdpResponse = CommonSharedBonusBundlePolicyUtil.displayRule(fdpRequestImpl);
				break;
			case SHARED_BUNDLE_VIEW_PROVIDER:
				if (FulfillmentUtil.isSharedBonudBundleProviderOrNew(fdpRequest,
						Arrays.asList(FDPConstant.SHARED_BONUD_BUNDLE_CONSUMER_TYPE))) {
					updateAuxInRequest(fdpRequest, AuxRequestParam.PROVIDER_MSISDN, "provider");
				} else {
					fdpResponse = new FDPResponseImpl(Status.SUCCESS, false, ResponseUtil.createResponseMessageInList(
							fdpRequest.getChannel(), FulfillmentResponseCodes.CONSNUMER_MSISDN_NOT_VALID.getReason(),
							TLVOptions.SESSION_TERMINATE));
				}
				break;
			case SHARED_DATA_BALANCE:
				updateAuxInRequest(fdpRequestImpl, AuxRequestParam.SBB_BALANCE);
				break;
			case SHARED_BUNDLE_REQEUEST_TOPUP_REQUEST:
				updateAuxInRequest(fdpRequestImpl, AuxRequestParam.SBB_BALANCE);
				executeRequestTopUp(fdpRequestImpl);
				break;
			default:
				break;
			}
		}
		return fdpResponse;
	}

	@Override
	public FDPPolicyResponse validatePolicyRule(Object input, FDPRequest fdpRequest, Object... otherParams)
			throws ExecutionFailedException {
		FDPPolicyResponse response = null;
		if (fdpRequest instanceof FDPRequestImpl && null != fdpRequest.getValueFromRequest(RequestMetaValuesKey.NODE)
				&& fdpRequest.getValueFromRequest(RequestMetaValuesKey.NODE) instanceof SpecialMenuNode) {
			final SpecialMenuNode specialMenuNode = (SpecialMenuNode) fdpRequest
					.getValueFromRequest(RequestMetaValuesKey.NODE);
			FDPRequestImpl fdpRequestImpl = (FDPRequestImpl) fdpRequest;
			switch (specialMenuNode.getSpecialMenuNodeType()) {
				case SHARED_BUNDLE_VIEW_CONSUMER:
					response = CommonSharedBonusBundlePolicyUtil.validatePolicyRule(input, fdpRequestImpl,
							FDPServiceProvSubType.SHARED_BUNDLE_VIEW_CONSUMER);
					break;
				default :
					response = new FDPPolicyResponseImpl(PolicyStatus.EXECUTE_SP, null, null, true, null);
			}
		}
		return response;
	}

	/**
	 * This method will update the AUX param for View Consumer.
	 * 
	 * @param fdpRequest
	 * @throws ExecutionFailedException
	 */
	private void updateAuxInRequest(final FDPRequest fdpRequest, final AuxRequestParam auxRequestParam,
			final String parameterPath) throws ExecutionFailedException {
		final CommandParamOutput commandParamOutput = FulfillmentUtil.getCommandOutput(fdpRequest, parameterPath,
				FDPConstant.SBB_GETDETAILS_COMMAND, true);
		final FDPRequestImpl fdpRequestImpl = (FDPRequestImpl) fdpRequest;
		if (null != commandParamOutput && !StringUtil.isNullOrEmpty(commandParamOutput.getValue().toString())) {
			fdpRequestImpl.putAuxiliaryRequestParameter(auxRequestParam, commandParamOutput.getValue().toString());
		} else {
			fdpRequestImpl.putAuxiliaryRequestParameter(auxRequestParam, FDPConstant.EMPTY_STRING);
		}
	}
	
	/**
	 * This method will update the AUX for balance.
	 * 
	 * @param fdpRequest
	 * @param auxRequestParam
	 * @throws ExecutionFailedException
	 */
	private void updateAuxInRequest(final FDPRequest fdpRequest, final AuxRequestParam auxRequestParam)
			throws ExecutionFailedException {
		Double processedBalance = 0.0;
		boolean isProviderType = FulfillmentUtil.isSharedBonudBundleProviderOrNew(fdpRequest,
				Arrays.asList(FDPConstant.SHARED_BONUS_BUNDLE_PROVIDER_TYPE.get(0)));
		
		final String sharedBonusBundleBalanceDAId = fdpRequest.getCircle().getConfigurationKeyValueMap()
				.get(ConfigurationKey.SHARED_BONUD_BUNDLE_BALANCE_DAID.getAttributeName());
		if(null == sharedBonusBundleBalanceDAId) {
			throw new ExecutionFailedException("Missing configuration for key SHARED_BONUD_BUNDLE_BALANCE_DAID");
		}
		final CommandParamOutput commandParamOutput = !isProviderType ? FulfillmentUtil.getCommandOutput(fdpRequest,
				"provider", FDPConstant.SBB_GETDETAILS_COMMAND, false) : null;
		String rawBalance = null;
		final Long requestorMsisdn = fdpRequest.getSubscriberNumber();
		try {
			if(!isProviderType && null != commandParamOutput) {
				FulfillmentUtil.swapMsisdn(Long.valueOf(commandParamOutput.getValue().toString()), fdpRequest);
			}
			rawBalance = getValueForDAId(fdpRequest, sharedBonusBundleBalanceDAId);
		} finally {
			if(!isProviderType && null != commandParamOutput) {
				FulfillmentUtil.swapMsisdn(requestorMsisdn, fdpRequest);
			}
		}
		if(null != rawBalance) {
			Double intBalance = Double.parseDouble(rawBalance);
			processedBalance = intBalance / getSharedBalanceDivisionFactor(fdpRequest);
			DecimalFormat df = new DecimalFormat("#.##");      
			processedBalance = Double.valueOf(df.format(processedBalance));
		}
		FDPRequestImpl fdpRequestImpl = (FDPRequestImpl) fdpRequest;
		fdpRequestImpl.putAuxiliaryRequestParameter(auxRequestParam, String.valueOf(processedBalance));
	}
	
	/**
    * This method will fetch the DA value from .
    *
    * @param fdpRequest
    * @param daId
    * @return
    * @throws ExecutionFailedException
    */
   public static String getValueForDAId(final FDPRequest fdpRequest, final String daId) throws ExecutionFailedException {
       String daValue = null;
       FDPCommand command = (FDPCommand) fdpRequest.getExecutedCommand(Command.GET_BALANCE_AND_DATE.getCommandDisplayName());
       if(null == command) {
           final FDPCacheable fdpCommandCached = ApplicationConfigUtil.getMetaDataCache().getValue(
                   new FDPMetaBag(fdpRequest.getCircle(), ModuleType.COMMAND, Command.GET_BALANCE_AND_DATE.getCommandDisplayName()));
           if(null != fdpCommandCached && fdpCommandCached instanceof FDPCommand) {
               command = (FDPCommand) fdpCommandCached;
               if(Status.SUCCESS.equals(command.execute(fdpRequest))) {
                   fdpRequest.addExecutedCommand(command);
               }
           }
       }       
       String pathkey = null;
       int i = 0;
       final String paramterName = "dedicatedAccountInformation";
       final String dedicatedAccountId  = "dedicatedAccountId";
       final String dedicatedAccountValue1 = "dedicatedAccountValue1";
       while (command.getOutputParam(pathkey = (paramterName + FDPConstant.PARAMETER_SEPARATOR
               + i + FDPConstant.PARAMETER_SEPARATOR + dedicatedAccountId)) != null) {
           String fafIndicatorKey = paramterName + FDPConstant.PARAMETER_SEPARATOR
                   + i + FDPConstant.PARAMETER_SEPARATOR + dedicatedAccountValue1;
           final String userDaId = command.getOutputParam(pathkey).getValue().toString();
           final String userDaValue = command.getOutputParam(fafIndicatorKey).getValue().toString();
           if(null != userDaId &&  userDaId.equals(daId)) {
               daValue = userDaValue;
               break;
           }
           i = i+1;
       }       
       return daValue;
   }
   
   /**
    * This  method will execute the request top up.
    * 
    * @param fdpRequest
 * @throws ExecutionFailedException 
    */
	private void executeRequestTopUp(final FDPRequest fdpRequest) throws ExecutionFailedException {
		FDPCommand fdpCommand = fdpRequest.getExecutedCommand(FDPConstant.SBB_GETDETAILS_COMMAND);
		if (null == fdpCommand) {
			final FDPCacheable fdpCacheable = (FDPCommand) ApplicationConfigUtil.getMetaDataCache().getValue(
					new FDPMetaBag(fdpRequest.getCircle(), ModuleType.COMMAND, FDPConstant.SBB_GETDETAILS_COMMAND));
			if (null != fdpCacheable && fdpCacheable instanceof FDPCommand) {
				fdpCommand = (FDPCommand) fdpCacheable;
				if (!Status.SUCCESS.equals(fdpCommand.execute(fdpRequest))) {
					throw new ExecutionFailedException("Unable to execute the GetDetails");
				}
			}
		}
		final CommandParamOutput commandParamOutput = FulfillmentUtil.getCommandOutput(fdpRequest, "provider",
				FDPConstant.SBB_GETDETAILS_COMMAND, false);
		if (null == commandParamOutput) {
			throw new ExecutionFailedException("Unable to get provider detail from GetDetails");
		}
		final String providerMsisdn = commandParamOutput.getValue().toString();
		if (fdpRequest instanceof FDPRequestImpl) {
			final FDPRequestImpl fdpRequestImpl = (FDPRequestImpl) fdpRequest;
			fdpRequestImpl.putAuxiliaryRequestParameter(AuxRequestParam.PROVIDER_MSISDN, providerMsisdn);
		}
		try {
			NotificationUtil.sendNotification(Long.valueOf(providerMsisdn), ChannelType.SMS, fdpRequest.getCircle(),
					notificationProviderTopUp(fdpRequest), fdpRequest.getRequestId(), true);
		} catch (NumberFormatException e) {
			e.printStackTrace();
			throw new ExecutionFailedException("Got NumberFormat Execption, Actual Error:", e);
		} catch (NotificationFailedException e) {
			e.printStackTrace();
			throw new ExecutionFailedException("Got NotificationFailedExecution Exception, Actual Error:", e);
		}
	}
	
	/**
	 * This method prepare the notification text for provider during request top up.
	 * 
	 * @param fdpRequest
	 * @return
	 * @throws NotificationFailedException 
	 */
	private String notificationProviderTopUp(final FDPRequest fdpRequest) throws NotificationFailedException {
		return TariffEnquiryNotificationUtil.createNotificationText(fdpRequest,
				FDPConstant.SHARED_BONUS_BUNDLE_REQUEST_TOPUP_NOT_TEXT - (fdpRequest.getCircle().getCircleId()),
				LoggerUtil.getSummaryLoggerFromRequest(fdpRequest));
	}
	
	/**
	 * This method get the Shared Balance Division Factor.
	 * 
	 * @param fdpRequest
	 * @return
	 */
	private Double getSharedBalanceDivisionFactor(final FDPRequest fdpRequest) {
		final String config = fdpRequest
				.getCircle()
				.getConfigurationKeyValueMap()
				.get(ConfigurationKey.SHARED_BONUD_BUNDLE_BAL_DIVISION_FACTOR
						.getAttributeName());
		return (null == config) ? 102.4 : Double.parseDouble(config);
	}
}
