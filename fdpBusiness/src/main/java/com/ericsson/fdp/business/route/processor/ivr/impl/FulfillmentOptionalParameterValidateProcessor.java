package com.ericsson.fdp.business.route.processor.ivr.impl;

import javax.xml.bind.JAXBException;

import org.apache.camel.Exchange;
import org.apache.camel.Message;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ericsson.fdp.business.constants.BusinessConstants;
import com.ericsson.fdp.business.enums.ivr.FulfillmentResponseCodes;
import com.ericsson.fdp.business.route.processor.ivr.AbstractFulfillmentProcessor;
import com.ericsson.fdp.common.constants.FDPConstant;
import com.ericsson.fdp.common.vo.FDPCircle;
import com.ericsson.fdp.core.config.utils.PropertyUtils;
import com.ericsson.fdp.core.logging.FDPLogger;

import com.ericsson.fdp.route.enumeration.FDPRouteHeaders;

/**
 * IVRCircleAndMSISDNValidateProcessor checks the MSISDN and Circle provided in
 * the ivr request either for Buy Product or Command Service and send the
 * response back if any of them is invalid.
 * 
 * @author Ericsson
 */
public class FulfillmentOptionalParameterValidateProcessor extends AbstractFulfillmentProcessor {

	/** The Constant LOGGER. */
	private static final Logger LOGGER = LoggerFactory.getLogger(FulfillmentCircleAndMSISDNValidateProcessor.class);
	//private static final String circleCode = PropertyUtils.getProperty("CIRCLE_CODE");

	@Override
	public void process(final Exchange exchange) throws Exception {
		final Message in = exchange.getIn();
		LOGGER.debug("Validating Optional Parameters");
		
		final String splitno = in.getHeader(BusinessConstants.SPLIT_NUMBER, String.class);
		final String productcost = in.getHeader(BusinessConstants.PRODUCT_COST, String.class);
		if(splitno!=null && !splitno.equals("") ){
			int splitNo = Integer.parseInt(splitno);
			
			if(splitNo<0){
				optionaParameterErrorMessage(exchange,BusinessConstants.SPLIT_NUMBER,splitNo);
			}
			
		}
		 if(productcost!=null && !productcost.equals("")){
			int productCost = Integer.parseInt(productcost);
			/*if(productCost<0){
				optionaParameterErrorMessage(exchange,BusinessConstants.PRODUCT_COST,productCost);
			}*/
		}
		
		LOGGER.debug("Optional Parameters validation is Done.");
	}
	
	private void optionaParameterErrorMessage(Exchange exchange,String paramName,Integer pararamValue) throws JAXBException{
		final Message in = exchange.getIn();
		final String msisdn = (String) in.getHeader("MSISDN");
		final FDPCircle fdpCircle = exchange.getIn().getHeader(FDPRouteHeaders.FDP_CIRCLE.getValue(), FDPCircle.class);
		String errorDescription = null;
		String requestId = exchange.getIn().getHeader(FDPRouteHeaders.REQUEST_ID.getValue(), String.class);
		errorDescription = new StringBuilder(FDPConstant.MSISDN)
		.append(FDPConstant.LOGGER_KEY_VALUE_DELIMITER)
		.append(requestId)
		.append(FDPConstant.LOGGER_DELIMITER)
		.append(FDPConstant.ERROR_CODE)
		.append(FDPConstant.LOGGER_KEY_VALUE_DELIMITER)
		.append(FulfillmentResponseCodes.INVALID_OPTIONAL_PARAMETER_ERROR.getResponseCode().toString())
		.append(FDPConstant.LOGGER_DELIMITER)
		.append(FDPConstant.ERROR_DESC)
		.append(FDPConstant.LOGGER_KEY_VALUE_DELIMITER)
		.append(String.format(FulfillmentResponseCodes.INVALID_OPTIONAL_PARAMETER_ERROR.getDescription(),
				pararamValue, fdpCircle.getCircleCode())).toString();
         FDPLogger.error(getCircleRequestLogger(fdpCircle), getClass(),
		"optionaParameterErrorMessage()", errorDescription);
            sendResponse(exchange, FulfillmentResponseCodes.INVALID_OPTIONAL_PARAMETER_ERROR,paramName+FDPConstant.SPACE+pararamValue);
	
		
	}

}
