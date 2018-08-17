package com.ericsson.fdp.business.route.processor;

import org.apache.camel.Exchange;
import org.apache.camel.Message;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ericsson.fdp.business.enums.ivr.FulfillmentParameters;
import com.ericsson.fdp.business.enums.ivr.FulfillmentResponseCodes;
import com.ericsson.fdp.business.enums.ivr.FulfillmentRouteEnum;
import com.ericsson.fdp.business.route.processor.ivr.AbstractFulfillmentProcessor;
import com.ericsson.fdp.common.constants.FDPConstant;
import com.ericsson.fdp.common.vo.FDPCircle;
import com.ericsson.fdp.core.config.utils.FDPApplicationFeaturesEnum;
import com.ericsson.fdp.core.dsm.framework.service.FDPCache;
import com.ericsson.fdp.core.logging.FDPLogger;
import com.ericsson.fdp.core.serviceprovisioning.bean.FDPAppBag;
import com.ericsson.fdp.core.utils.ApplicationConfigUtil;
import com.ericsson.fdp.core.utils.CircleCodeFinder;
import com.ericsson.fdp.route.enumeration.FDPRouteHeaders;

public class FulfillmentChannelParameterProcessor extends AbstractFulfillmentProcessor {

	/** The Constant LOGGER. */
	private static final Logger LOGGER = LoggerFactory.getLogger(FulfillmentChannelParameterProcessor.class);

	@Override
	public void process(Exchange exchange) throws Exception {

		final FulfillmentParameters param = FulfillmentParameters.CHANNEL;
		final String channel = exchange.getIn().getHeader(param.getValue(), String.class);
		final Message in = exchange.getIn();
		final String circleCode = in.getHeader(FulfillmentParameters.COUNTRY_CODE.getValue(), String.class);
		final String msisdn = (String) in.getHeader("MSISDN");
		final String requestId = in.getHeader(FDPRouteHeaders.REQUEST_ID.getValue(), String.class);
		FDPCircle fdpCircle = null;
		final FDPCache<FDPAppBag, Object> applicationConfigCache = ApplicationConfigUtil.getApplicationConfigCache();
		if (FDPApplicationFeaturesEnum.isFeatureAllowed(
				FDPApplicationFeaturesEnum.FEATURE_IS_IMSI_BASED_CIRCLE_CHECK_ALLOW, applicationConfigCache)) {
			fdpCircle = CircleCodeFinder.getFDPCircleByCode(circleCode, applicationConfigCache);
		} else {
			fdpCircle = CircleCodeFinder.getFDPCircleByMsisdn(msisdn, applicationConfigCache);
		}
		in.setHeader(FDPRouteHeaders.FDP_CIRCLE.getValue(), fdpCircle);

		LOGGER.info("{} = {} found.", param.getValue(), channel);

		if (channel != null) {
			final FulfillmentRouteEnum routeInfo = getRouteInfo(exchange);
			LOGGER.debug("Validating {}.", param.getValue());
			if (!isValidListValueForKey(channel, routeInfo.getWhiteListINameKey())) {
				String errorDescription = new StringBuilder(FDPConstant.REQUEST_ID)
						.append(FDPConstant.LOGGER_KEY_VALUE_DELIMITER)
						.append(requestId)
						.append(FDPConstant.LOGGER_KEY_VALUE_DELIMITER)
						.append("ERROR_CODE")
						.append(FDPConstant.LOGGER_KEY_VALUE_DELIMITER)
						.append(FulfillmentResponseCodes.INVALID_PARAMETER.getResponseCode().toString())
						.append(FDPConstant.LOGGER_KEY_VALUE_DELIMITER)
						.append(String.format(FulfillmentResponseCodes.INVALID_PARAMETER.getDescription(),
								param.getValue(), fdpCircle.getCircleCode())).toString();
				FDPLogger.error(getCircleRequestLogger(fdpCircle), getClass(), "process()", errorDescription);
				LOGGER.warn("Invalid {} value : {}", param.getValue(), channel);
				sendResponse(exchange, FulfillmentResponseCodes.INVALID_PARAMETER, param.getValue());
			} else {
				LOGGER.debug("{} is valid.", param.getValue());
			}

		}
		else
		{
			final FulfillmentRouteEnum routeInfo = getRouteInfo(exchange);
			String errorDescription = new StringBuilder(FDPConstant.REQUEST_ID)
						.append(FDPConstant.LOGGER_KEY_VALUE_DELIMITER)
						.append(requestId)
						.append(FDPConstant.LOGGER_KEY_VALUE_DELIMITER)
						.append("ERROR_CODE")
						.append(FDPConstant.LOGGER_KEY_VALUE_DELIMITER)
						.append(FulfillmentResponseCodes.INVALID_PARAMETER.getResponseCode().toString())
						.append(FDPConstant.LOGGER_KEY_VALUE_DELIMITER)
						.append(String.format(FulfillmentResponseCodes.INVALID_PARAMETER.getDescription(),
								param.getValue(), fdpCircle.getCircleCode())).toString();
				FDPLogger.error(getCircleRequestLogger(fdpCircle), getClass(), "process()", errorDescription);
				LOGGER.warn("Invalid {} value : {}", param.getValue(), channel);
				sendResponse(exchange, FulfillmentResponseCodes.INVALID_PARAMETER, param.getValue());
				
		}
	}

}
