package com.ericsson.fdp.business.sharedaccount.service.impl;

import java.util.regex.Pattern;

import javax.ejb.Stateless;

import org.slf4j.Logger;

import com.ericsson.fdp.FDPCacheable;
import com.ericsson.fdp.business.enums.Command;
import com.ericsson.fdp.business.enums.ConfigurationKey;
import com.ericsson.fdp.business.request.FDPCheckConsumerResponse;
import com.ericsson.fdp.business.request.FDPCheckConsumerResponseImpl;
import com.ericsson.fdp.business.sharedaccount.service.CheckConsumerService;
import com.ericsson.fdp.business.util.LoggerUtil;
import com.ericsson.fdp.common.constants.FDPConstant;
import com.ericsson.fdp.common.enums.ModuleType;
import com.ericsson.fdp.common.enums.Status;
import com.ericsson.fdp.common.vo.FDPCircle;
import com.ericsson.fdp.core.cache.AppCacheSubStore;
import com.ericsson.fdp.core.command.FDPCommand;
import com.ericsson.fdp.core.command.param.CommandParam;
import com.ericsson.fdp.core.exception.ExecutionFailedException;
import com.ericsson.fdp.core.logging.FDPLogger;
import com.ericsson.fdp.core.request.FDPRequest;
import com.ericsson.fdp.core.request.ResponseError;
import com.ericsson.fdp.core.serviceprovisioning.bean.FDPAppBag;
import com.ericsson.fdp.core.serviceprovisioning.bean.FDPMetaBag;
import com.ericsson.fdp.core.utils.ApplicationConfigUtil;

/**
 * The Class CheckConsumerServiceImpl.
 */
@Stateless
public class CheckConsumerServiceImpl implements CheckConsumerService {

	/**
	 * The circle logger.
	 */
	//private Logger circleLogger = null;
	
	@Override
	public FDPCheckConsumerResponse checkPrePaidConsumer(FDPRequest request) throws ExecutionFailedException {
		FDPCheckConsumerResponseImpl fdpCheckConsumerResponse = new FDPCheckConsumerResponseImpl();
		FDPCommand fdpCommand = null;
		String getAccountDetails = Command.GETACCOUNTDETAILS.getCommandDisplayName();
		FDPCircle fdpCircle = request.getCircle();

		final FDPCacheable fdpCommandCached = ApplicationConfigUtil.getMetaDataCache().getValue(
				new FDPMetaBag(fdpCircle, ModuleType.COMMAND, getAccountDetails));

		FDPLogger.debug(getCircleLogger(request),getClass(),
				"checkPrePaidConsumer()",
				LoggerUtil.getRequestAppender(request) + "The subscriber number is : "
						+ request.getSubscriberNumber()+"  command is "
						+ fdpCommandCached + " and circleCode is "+request.getCircle().getCircleCode());

		if (fdpCommandCached != null && fdpCommandCached instanceof FDPCommand) {
			fdpCommand = (FDPCommand) fdpCommandCached;
			checkConsumerTypeForRequest(request, fdpCheckConsumerResponse, fdpCommand); 
		} else {
			throw new ExecutionFailedException("Get account details is not configured for the circle."+request.getCircle().getCircleCode());
		}
		return fdpCheckConsumerResponse;
	}

	/**
	 * Gets the fDP check consumer.
	 *
	 * @param request the request
	 * @param fdpCheckConsumerResponse the fdp check consumer response
	 * @param fdpCommand the fdp command
	 * @return the fDP check consumer
	 * @throws ExecutionFailedException the execution failed exception
	 */
	private void checkConsumerTypeForRequest(FDPRequest request, FDPCheckConsumerResponseImpl fdpCheckConsumerResponse,
			FDPCommand fdpCommand) throws ExecutionFailedException {

		if (fdpCommand != null) {
			Status status = fdpCommand.execute(request);
			FDPLogger.debug(getCircleLogger(request),getClass(),
					"checkConsumerTypeForRequest()",
					LoggerUtil.getRequestAppender(request) + " The status of command executed is "
							+ status + " circle is "+request.getCircle().getCircleCode());
			fdpCheckConsumerResponse.addExecutedCommand(fdpCommand);
			if (status.equals(Status.SUCCESS)) {
				fdpCheckConsumerResponse.setStatus(Status.SUCCESS);
				//fdpCheckConsumerResponse.setPrePaidConsumer(true);
				fdpCheckConsumerResponse.setPrePaidConsumer(!isPostPaidServiceClass(request, fdpCommand));
			} else {
				ResponseError responseError = fdpCommand.getResponseError();
				String responseCode = responseError.getResponseCode();
				FDPLogger.debug(getCircleLogger(request),getClass(),
						"checkConsumerTypeForRequest()",
						LoggerUtil.getRequestAppender(request) + " The response code of executed command is : "+responseCode);

				String postPaidCode = getErrorCodeForPostPaidConsumer();

				FDPLogger.debug(getCircleLogger(request),getClass(),
						"checkConsumerTypeForRequest()",
						LoggerUtil.getRequestAppender(request) + "  The postPaid code from FDPAdmin configuration : "+postPaidCode);

				if (responseCode != null && postPaidCode!=null && !responseCode.isEmpty() && responseCode.equals(postPaidCode)) {
					fdpCheckConsumerResponse.setStatus(Status.SUCCESS);
					fdpCheckConsumerResponse.setPrePaidConsumer(false);
					fdpCheckConsumerResponse.setResponseError(responseError);
				} else {
					fdpCheckConsumerResponse.setPrePaidConsumer(false);
					fdpCheckConsumerResponse.setStatus(Status.FAILURE);
					fdpCheckConsumerResponse.setResponseError(responseError);
				}
			}
		} else {
			throw new ExecutionFailedException("Get account details is not configured for the circle."+request.getCircle().getCircleCode());
		}
	}

	/**
	 * Gets the post paid code from admin.
	 *
	 * @return the post paid code from admin
	 * @throws ExecutionFailedException the execution failed exception
	 */
	private String getErrorCodeForPostPaidConsumer() throws ExecutionFailedException {
		String postPaidCode = null;
		FDPAppBag appBag = new FDPAppBag();
		appBag.setSubStore(AppCacheSubStore.CONFIGURATION_MAP);
		appBag.setKey(FDPConstant.POST_PAID_CONSUMER_ERROR_CODE);
		Object object = ApplicationConfigUtil.getApplicationConfigCache().getValue(appBag);
		if(object != null && object instanceof String) {
			postPaidCode = (String) object;
		} 
		return postPaidCode;
	}


	/**
	 * This method is sued to get the circle logger.
	 * 
	 * @param fdpRequest
	 *            the request to get the circle logger.
	 * @return the circle logger.
	 */
	private Logger getCircleLogger(final FDPRequest fdpRequest) {
		/*if (circleLogger == null) {
			circleLogger = LoggerUtil.getSummaryLoggerFromRequest(fdpRequest);
		}
		return circleLogger;*/
		return LoggerUtil.getSummaryLoggerFromRequest(fdpRequest);
	}
	
	/**
	 * This method checks the service class present in postpaid circle configuration.
	 * @param fdpRequest
	 * @param fdpCommand
	 * @return
	 */
	private boolean isPostPaidServiceClass(final FDPRequest fdpRequest, final FDPCommand fdpCommand) {
		boolean isPostPaidServiceClass = false;
		String serviceClass = null;
		final String cachePostPaidServiceClass = fdpRequest.getCircle().getConfigurationKeyValueMap().get(ConfigurationKey.POSTPAID_SERVICE_CLASS.getAttributeName());
		if(null != cachePostPaidServiceClass && cachePostPaidServiceClass.length()>0) {
			for(final String regex  : cachePostPaidServiceClass.trim().split(FDPConstant.COMMA)) {
				serviceClass = getServiceClass(fdpCommand);
				if(null != serviceClass & Pattern.matches(regex, serviceClass)) {
					isPostPaidServiceClass = true;
					break;
				}
			}
		}
		FDPLogger.info(getCircleLogger(fdpRequest),getClass(),
				"isPostPaidServiceClass()",
				LoggerUtil.getRequestAppender(fdpRequest) + "RequestId:"+fdpRequest.getRequestId()+", msisdn:"+fdpRequest.getSubscriberNumber()+", serviceClass:"+serviceClass+", isPostpaid:"+isPostPaidServiceClass);
		return isPostPaidServiceClass;
	}

	/**
	 * This method gets the user service class.
	 * 
	 * @param fdpCommand
	 * @return
	 */
	private String getServiceClass(FDPCommand fdpCommand) {
		CommandParam commandParam = fdpCommand.getOutputParam("serviceClassCurrent");
		return (null != commandParam) ? commandParam.getValue().toString() : null;
	}
}