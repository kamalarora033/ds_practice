package com.ericsson.fdp.business.fulfillment.service.impl;

import java.util.List;

import javax.ejb.Stateless;
import javax.naming.NamingException;

import ch.qos.logback.classic.Logger;

import com.ericsson.fdp.business.dto.ListMe2uDTO;
import com.ericsson.fdp.business.dto.Me2uProductDTO;
import com.ericsson.fdp.business.enums.Command;
import com.ericsson.fdp.business.enums.RequestMetaValuesKey;
import com.ericsson.fdp.business.enums.ivr.FulfillmentResponseCodes;
import com.ericsson.fdp.business.enums.ivr.FulfillmentSystemTypes;
import com.ericsson.fdp.business.request.impl.FDPMetadataResponseImpl;
import com.ericsson.fdp.business.step.execution.impl.rsdeprovision.AbstractRSActiveAccounts;
import com.ericsson.fdp.business.util.Data2ShareService;
import com.ericsson.fdp.business.util.LoggerUtil;
//import com.ericsson.fdp.business.dto.Me2uProductDTO;
import com.ericsson.fdp.common.constants.FDPConstant;
import com.ericsson.fdp.common.enums.Status;
import com.ericsson.fdp.core.command.FDPCommand;
import com.ericsson.fdp.core.exception.ExecutionFailedException;
import com.ericsson.fdp.core.logging.FDPLogger;
import com.ericsson.fdp.core.request.FDPRequest;
import com.ericsson.fdp.core.request.FDPResponse;
import com.ericsson.fdp.core.request.ResponseError;
import com.ericsson.fdp.core.request.impl.FDPRequestImpl;
import com.ericsson.fdp.core.utils.ApplicationConfigUtil;
import com.ericsson.fdp.dao.enums.DBActionClassName;

@Stateless
public class GetMe2uListFulfillmentServiceImpl extends AbstractFDPFulFillmentServiceImpl{
	
	Logger logger = null;
	
	/** The ejb lookup name. */
	private final String ejbLookupName = DBActionClassName.RS_DE_PROVISIONING_SERVICE.getLookUpClass();
	
	private final String data2shareLookUp = DBActionClassName.DATA2SHARE_SERVICE.getLookUpClass();
	
	/**
	 * This method will create execution response for the request with action=ME2ULIST.  
	 */
	
	protected FDPResponse executeService(FDPRequest fdpRequest, Object... additionalInformations) throws ExecutionFailedException {
		logger = getCircleLogger(fdpRequest);
		FDPLogger.debug(logger, getClass(), "execute GetMe2uListFulfillmentServiceImpl", LoggerUtil.getRequestAppender(fdpRequest)
				+ "Start executeService Called:" + fdpRequest.getRequestId());

		FDPMetadataResponseImpl fdpResponse = new FDPMetadataResponseImpl(Status.SUCCESS, true, null);
		FDPCommand fdpCommandGBAD = null;
		ListMe2uDTO listMe2uDTO = new ListMe2uDTO();
		Object beanObject;
		try {
			beanObject = ApplicationConfigUtil.getBean(ejbLookupName);

			if (null != beanObject && (beanObject instanceof AbstractRSActiveAccounts)) {
				final AbstractRSActiveAccounts accountService = (AbstractRSActiveAccounts) beanObject;
				fdpCommandGBAD = accountService.isCommandExecuted(fdpRequest, Command.GET_BALANCE_AND_DATE.getCommandDisplayName());

				final Object data2shareObj = ApplicationConfigUtil.getBean(data2shareLookUp);
				if(data2shareObj instanceof Data2ShareService) {
					final Data2ShareService data2ShareService = (Data2ShareService) data2shareObj;
					List<Me2uProductDTO> me2UProductDTO = data2ShareService.getData2ShareProducts(fdpCommandGBAD, fdpRequest);
					listMe2uDTO.setMe2uList(me2UProductDTO);
				}
			}
		} catch (NamingException e) {
			FDPLogger.error(logger, getClass(), "executeService", "Something went wrong while fetching data wrt to offerId");
		}

		final FDPRequestImpl requestImpl = (FDPRequestImpl) fdpRequest;
		requestImpl.addMetaValue(RequestMetaValuesKey.ME2U_PRODUCT_LIST, listMe2uDTO);
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
