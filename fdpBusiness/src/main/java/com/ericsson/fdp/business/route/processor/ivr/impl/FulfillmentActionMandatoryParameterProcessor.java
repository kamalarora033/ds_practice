package com.ericsson.fdp.business.route.processor.ivr.impl;

import javax.xml.bind.JAXBException;

import org.apache.camel.Exchange;
import org.apache.camel.Message;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ericsson.fdp.business.enums.ivr.FulfillmentActionTypes;
import com.ericsson.fdp.business.enums.ivr.FulfillmentParameters;
import com.ericsson.fdp.business.enums.ivr.FulfillmentResponseCodes;
import com.ericsson.fdp.business.enums.ivr.commandservice.IVRCommandEnum;
import com.ericsson.fdp.business.route.processor.ivr.AbstractFulfillmentProcessor;
import com.ericsson.fdp.common.constants.FDPConstant;
import com.ericsson.fdp.common.util.StringUtil;
import com.ericsson.fdp.common.vo.FDPCircle;
import com.ericsson.fdp.core.exception.ExecutionFailedException;
import com.ericsson.fdp.core.logging.FDPLogger;
import com.ericsson.fdp.route.enumeration.FDPRouteHeaders;

/**
 * FulfillmentActionMandatoryParameterProcessor checks the mandatory parameters
 * based on the IVR request action and send the response back if any of the
 * mandatory parameter is missing.
 * 
 * @author Ericsson
 */
public class FulfillmentActionMandatoryParameterProcessor extends AbstractFulfillmentProcessor {

	/** The Constant LOGGER. */
	private static final Logger LOGGER = LoggerFactory.getLogger(FulfillmentActionMandatoryParameterProcessor.class);
	//private static final String circleCode = PropertyUtils.getProperty("CIRCLE_CODE");

	@Override
	public void process(final Exchange exchange) throws Exception {
		final String action = getAction(exchange);
		final String msisdn = (String) exchange.getIn().getHeader("MSISDN");
		final String requestId = exchange.getIn().getHeader(FDPRouteHeaders.REQUEST_ID.getValue(), String.class);
		/*String circleCode = exchange.getIn().getHeader(FulfillmentParameters.CIRCLE_CODE.getValue(), String.class);
		if(circleCode==null)
		{
			circleCode= BusinessConstants.FULFILLMENT_CIRCLE_CODE;
		}*/
		LOGGER.debug("Checking for FulFillemt-Action-wise Mandatory Parameters.");
		boolean isConsumerCheckAllow = false;
		
		/**
		 * Action not mandatory field, but if present checks for parameters for
		 * corresponding action value.
		 */
		if ((!StringUtil.isNullOrEmpty(action))
				&& (null != FulfillmentActionTypes.getFulfillmentActionTypesByName(action))) {
			isConsumerCheckAllow = checkForActionParams(exchange, action);
		} else {
			checkForCommandInputParams(exchange);
		}
		
		/**
		 * Check CONSUMER_MSISDN from URL only if action is valid.
		 */
		if(isConsumerCheckAllow) {
			LOGGER.debug("Checking for CONSUMER_MSISDN");
			final Message in = exchange.getIn();
			final String consumerMsisdn = in.getHeader(FulfillmentParameters.CONSUMER_MSISDN.getValue(), String.class);
			if(null != consumerMsisdn) {
				/*final FDPCircle fdpCircle = CircleCodeFinder.getFDPCircleByMsisdn(consumerMsisdn,
						ApplicationConfigUtil.getApplicationConfigCache());*/
				final FDPCircle fdpCircle = exchange.getIn().getHeader(FDPRouteHeaders.FDP_CIRCLE.getValue(), FDPCircle.class);
				if (null == fdpCircle) {
					String errorDescription = new StringBuilder(FDPConstant.REQUEST_ID)
							.append(FDPConstant.LOGGER_KEY_VALUE_DELIMITER)
							.append(requestId)
							.append(FDPConstant.LOGGER_KEY_VALUE_DELIMITER)
							.append("ERROR_CODE")
							.append(FDPConstant.LOGGER_KEY_VALUE_DELIMITER)
							.append(FulfillmentResponseCodes.INVALID_PARAMETER.getResponseCode().toString())
							.append(FDPConstant.LOGGER_KEY_VALUE_DELIMITER)
							.append(String.format(FulfillmentResponseCodes.INVALID_PARAMETER.getDescription(), msisdn)).toString();
					FDPLogger.error(getCircleRequestLogger(fdpCircle), getClass(), "process()", errorDescription);
					sendResponse(exchange, FulfillmentResponseCodes.INVALID_PARAMETER,
							FulfillmentParameters.CONSUMER_MSISDN.getValue());
				}
			}
		}
		
	}

	/**
	 * This is the method for checking params for the action type.
	 * 
	 * @param exchange
	 * @param action
	 * @throws ExecutionFailedException 
	 * @throws JAXBException 
	 */
	private boolean checkForActionParams(final Exchange exchange, final String action) throws JAXBException, ExecutionFailedException {
		boolean isConsumerCheckAllow = false;
		LOGGER.debug("Found Action:" + action);
		final FulfillmentActionTypes fulfillmentActionTypes = FulfillmentActionTypes
				.getFulfillmentActionTypesByName(action);
		if ((null != fulfillmentActionTypes) && (null != fulfillmentActionTypes.getParameters())
				&& (fulfillmentActionTypes.getParameters().size() > 0)) {
			checkMandatoryParametersAndSendResponse(exchange, fulfillmentActionTypes.getParameters());
			isConsumerCheckAllow = true;
		}
		return isConsumerCheckAllow;
	}
	/**
	 * @param exchange
	 * @throws JAXBException
	 * @throws ExecutionFailedException
	 */
	private void checkForCommandInputParams(final Exchange exchange) throws JAXBException, ExecutionFailedException {
		final IVRCommandEnum commandEnum = getInputIVRCommand(exchange);
		if(null != commandEnum && null != commandEnum.getParameterList()) {
			LOGGER.debug("Command Found:"+commandEnum.getIvrName());
			checkMandatoryParametersAndSendResponse(exchange, commandEnum.getParameterList());
		}
	}
}
