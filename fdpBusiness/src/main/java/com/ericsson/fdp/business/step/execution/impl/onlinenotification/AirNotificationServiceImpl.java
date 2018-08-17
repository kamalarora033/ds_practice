package com.ericsson.fdp.business.step.execution.impl.onlinenotification;

/**
 * This service is for Online notification for the PAM renewal product
 */
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import javax.ejb.Stateless;

import org.json.JSONException;
import org.json.JSONObject;
import org.json.XML;

import ch.qos.logback.classic.Logger;

import com.ericsson.fdp.FDPCacheable;
import com.ericsson.fdp.business.constants.FDPStepResponseConstants;
import com.ericsson.fdp.business.enums.ServiceStepOptions;
import com.ericsson.fdp.business.product.impl.BaseProduct;
import com.ericsson.fdp.business.request.impl.FDPStepResponseImpl;
import com.ericsson.fdp.business.step.execution.FDPExecutionService;
import com.ericsson.fdp.business.util.FulfillmentUtil;
import com.ericsson.fdp.business.util.LoggerUtil;
import com.ericsson.fdp.business.vo.FDPOfferAttributeVO;
import com.ericsson.fdp.common.constants.FDPConstant;
import com.ericsson.fdp.common.enums.ModuleType;
import com.ericsson.fdp.common.enums.Status;
import com.ericsson.fdp.common.vo.FDPCircle;
import com.ericsson.fdp.core.command.FDPCommand;
import com.ericsson.fdp.core.command.param.CommandParam;
import com.ericsson.fdp.core.config.utils.PropertyUtils;
import com.ericsson.fdp.core.exception.ExecutionFailedException;
import com.ericsson.fdp.core.logging.FDPLogger;
import com.ericsson.fdp.core.request.FDPRequest;
import com.ericsson.fdp.core.request.FDPStepResponse;
import com.ericsson.fdp.core.serviceprovisioning.bean.FDPMetaBag;
import com.ericsson.fdp.core.utils.ApplicationConfigUtil;

/**
 * 
 * @author euyybcr
 * 
 */
@Stateless
public class AirNotificationServiceImpl implements FDPExecutionService {

	@Override
	public FDPStepResponse executeService(FDPRequest fdpRequest,
			Object... additionalInformations) throws ExecutionFailedException {
		// TODO Auto-generated method stub
		final Logger circleLogger = LoggerUtil
				.getSummaryLoggerFromRequest(fdpRequest);
		FDPLogger.debug(circleLogger, getClass(), "executeService()",
				"Executing Service for requestId:" + fdpRequest.getRequestId());
		return getResponseForRequestedAttribute(fdpRequest, circleLogger);
	}

	@Override
	public FDPStepResponse performRollback(FDPRequest fdpRequest,
			Map<ServiceStepOptions, String> additionalInformation) {
		// TODO Auto-generated method stub
		return null;
	}

	/**
	 * This method is execute the run pam online notification
	 * 
	 * @param fdpRequest
	 * @param circleLogger
	 * @return
	 * @throws ExecutionFailedException
	 */
	private FDPStepResponse getResponseForRequestedAttribute(
			final FDPRequest fdpRequest, final Logger circleLogger)
			throws ExecutionFailedException {
		FDPStepResponseImpl fdpStepResponse = null;
		String fulfillmentResponse = null;
		Set<Integer> products = null;
		if (fdpRequest != null) {
			List<Long> offerId = getRenewalOfferId(
					fdpRequest
							.getExecutedCommand(FDPConstant.AIR_TRAFFIC_COMMAND_NAME),
					circleLogger);
			
			for (Iterator iterator = offerId.iterator(); iterator.hasNext();) {
				Long offer = (Long) iterator.next();
				FDPCacheable cacheable = ApplicationConfigUtil.getMetaDataCache()
						.getValue(
								new FDPMetaBag(fdpRequest.getCircle(),
										ModuleType.OFFER_ATTRIBUTE, offer
												.intValue()));
				FDPOfferAttributeVO fdpOfferAttributeVO = null;
				if (cacheable != null && cacheable instanceof FDPOfferAttributeVO) {
					fdpOfferAttributeVO = (FDPOfferAttributeVO) cacheable;
					products = fdpOfferAttributeVO.getProductid();
					FDPCircle fdpCircle = fdpRequest.getCircle();
					for (Integer productId : products) {
						cacheable = ApplicationConfigUtil.getMetaDataCache()
								.getValue(
										new FDPMetaBag(fdpRequest.getCircle(),
												ModuleType.PRODUCT, productId));
						BaseProduct baseProduct = null;
						if (cacheable != null && cacheable instanceof BaseProduct) {
							baseProduct = (BaseProduct) cacheable;
							try {
								//changes for product ID/name mapping
								String PRODUCT_NAME_ID_MAPPING = PropertyUtils.getProperty("PRODUCT_NAME_ID_MAPPING");
								String productCode = null;
								if (PRODUCT_NAME_ID_MAPPING != null && PRODUCT_NAME_ID_MAPPING.equalsIgnoreCase("Y")) {
									productCode = baseProduct.getProductName();
								}
								else
								 productCode =fdpCircle.getCircleCode()+FDPConstant.UNDERSCORE+baseProduct.getProductType().getName()+FDPConstant.UNDERSCORE+baseProduct.getProductId() ;
								
								fulfillmentResponse =  FulfillmentUtil.postFulfillmentAirNotification(
										productCode,
										fdpRequest.getSubscriberNumber(),
										fdpCircle.getCircleCode(), "AIR",
										circleLogger);
								if(null!=fulfillmentResponse)
								fdpStepResponse = getFDPStepResponse(fulfillmentResponse);							
								
							} catch (IOException e) {

								FDPLogger.error(circleLogger, getClass(),
										"getResponseForRequestedAttribute()",
										"Executing Service for requestId:"
												+ fdpRequest.getRequestId());		
								fdpStepResponse = new FDPStepResponseImpl();
								fdpStepResponse.addStepResponseValue(FDPStepResponseConstants.STATUS_KEY, false);
								fdpStepResponse.addStepResponseValue(FDPStepResponseConstants.ERROR_CODE, "400");
								fdpStepResponse.addStepResponseValue(FDPStepResponseConstants.ERROR_VALUE, "Error on Air Notification");

							}
						}
					}
				}				
			}
			// offerId = (long)90;//for testing purpose
			
			


		}
		//need improvement here ehlnopu
		if(fdpStepResponse==null)
		{
			fdpStepResponse = new FDPStepResponseImpl();
			fdpStepResponse.addStepResponseValue(FDPStepResponseConstants.STATUS_KEY, false);
			fdpStepResponse.addStepResponseValue(FDPStepResponseConstants.ERROR_CODE, "400");
			fdpStepResponse.addStepResponseValue(FDPStepResponseConstants.ERROR_VALUE, "Error on Air Notification");
		}
		return fdpStepResponse;
	}

	/**
	 * This method return the actual offer id required for renewal
	 * 
	 * @param executedCommand
	 * @param circleLogger
	 * @return the actual renewal offer Id
	 */
	private List<Long> getRenewalOfferId(final FDPCommand executedCommand,
			final Logger circleLogger) {
List<Long> offerslist=new ArrayList<Long>();
		for (Entry<String, CommandParam> entry : executedCommand
				.getOutputParams().entrySet()) {

			if (entry.getKey().contains(
					FDPConstant.AIR_NOTIFICATION_TREE_DEFINED_FIELDS)
					&& entry.getValue().getValue() != "") {
				offerslist.add(Long.valueOf(entry.getValue().getValue().toString()));
				
			}
		}
		return offerslist;
	}
	
	/**
	 *  this method return the fdp step response object based on fulfillment response
	 * @param fulfillmentResponse
	 * @return The fdpstep response object
	 */
	private FDPStepResponseImpl getFDPStepResponse(String fulfillmentResponse) throws ExecutionFailedException
	{
		FDPStepResponseImpl fdpStepResponseImpl = null;
		if(fulfillmentResponse!=null)
		{
			try
			{
				fdpStepResponseImpl = new FDPStepResponseImpl();
				JSONObject xmlJSONObj = XML.toJSONObject(fulfillmentResponse);
				//final JSONObject statusJSONObj = (JSONObject) ((JSONObject) xmlJSONObj.get("fulfillmentService")).get("status");
				String status = (String) ((JSONObject) xmlJSONObj.get("fulfillmentService")).get("status");
				fdpStepResponseImpl.addStepResponseValue(FDPStepResponseConstants.STATUS_KEY, true);
				if(status.equalsIgnoreCase(Status.FAILURE.getStatusText()))
				{
					fdpStepResponseImpl.addStepResponseValue(FDPStepResponseConstants.STATUS_KEY, false);
					fdpStepResponseImpl.addStepResponseValue(FDPStepResponseConstants.ERROR_CODE, "400");
					fdpStepResponseImpl.addStepResponseValue(FDPStepResponseConstants.ERROR_VALUE, "Error while calling fulfillment ");
				}
			}
			catch (final JSONException e) {
				throw new ExecutionFailedException("The input xml is not valid.", e);
			}

		}
		return fdpStepResponseImpl;
	}

	}
