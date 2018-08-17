package com.ericsson.fdp.business.route.processor.ivr.impl;

import java.util.ArrayList;
import java.util.List;

import org.apache.camel.Exchange;
import org.apache.camel.Message;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ericsson.fdp.business.enums.AuxRequestParam;
import com.ericsson.fdp.business.enums.ivr.FulfillmentParameters;
import com.ericsson.fdp.business.enums.ivr.FulfillmentResponseCodes;
import com.ericsson.fdp.business.request.impl.FDPResponseImpl;
import com.ericsson.fdp.business.route.processor.ivr.AbstractFulfillmentProcessor;
import com.ericsson.fdp.business.util.FnfUtil;
import com.ericsson.fdp.common.constants.FDPConstant;
import com.ericsson.fdp.common.enums.Status;
import com.ericsson.fdp.common.vo.FDPCircle;
import com.ericsson.fdp.core.display.ResponseMessage;
import com.ericsson.fdp.core.dsm.framework.service.FDPCache;
import com.ericsson.fdp.core.exception.EvaluationFailedException;
import com.ericsson.fdp.core.logging.FDPLogger;
import com.ericsson.fdp.core.serviceprovisioning.bean.FDPAppBag;
import com.ericsson.fdp.core.utils.ApplicationConfigUtil;
import com.ericsson.fdp.core.utils.CircleCodeFinder;
import com.ericsson.fdp.route.enumeration.FDPRouteHeaders;

/**
 * IVRCircleAndMSISDNValidateProcessor checks the MSISDN and Circle provided in
 * the ivr request either for Buy Product or Command Service and send the
 * response back if any of them is invalid.
 * 
 * @author Ericsson
 */
public class FulfillmentCircleAndMSISDNValidateProcessor extends AbstractFulfillmentProcessor {

	/** The Constant LOGGER. */
	private static final Logger LOGGER = LoggerFactory.getLogger(FulfillmentCircleAndMSISDNValidateProcessor.class);

	@Override
	public void process(final Exchange exchange) throws Exception {
		final Message in = exchange.getIn();
		final String requestId = in.getHeader(FDPRouteHeaders.REQUEST_ID.getValue(), String.class);
		LOGGER.debug("Validating MSISDN and circle.");
		final String msisdn = getMsisdn(in);
		// String circleCode =
		// in.getHeader(FulfillmentParameters.CIRCLE_CODE.getValue(),
		// String.class);
		FDPCircle fdpCircle = exchange.getIn().getHeader(FDPRouteHeaders.FDP_CIRCLE.getValue(), FDPCircle.class);
		String fafMsisdn = exchange.getIn().getHeader(FulfillmentParameters.FAF_MSISDN.getValue(), String.class);
		String fafOldMsisdn = exchange.getIn().getHeader(FulfillmentParameters.OLD_FAF_MSISDN.getValue(), String.class);

		if (null == fdpCircle) {
			FDPCache<FDPAppBag, Object> applicationConfigCache = ApplicationConfigUtil.getApplicationConfigCache();
			/*
			 * finalif(FDPApplicationFeaturesEnum.isFeatureAllowed(
			 * FDPApplicationFeaturesEnum
			 * .FEATURE_IS_IMSI_BASED_CIRCLE_CHECK_ALLOW
			 * ,applicationConfigCache)) { fdpCircle =
			 * CircleCodeFinder.getFDPCircleByCode(circleCode,
			 * applicationConfigCache); } else {
			 */
			fdpCircle = CircleCodeFinder.getFDPCircleByMsisdn(msisdn, applicationConfigCache);
			// }
		}
		String errorDescription = null;
		if (null != fafMsisdn) {
			// FAF msisdn validation and create error response
			Boolean isValidMsisdn = FnfUtil.ValidateFafMsisdn(fafMsisdn);
			if (!isValidMsisdn) {
				LOGGER.info("FAF MSISDN is not valid fafMsisdn = " + fafMsisdn);
				errorDescription = new StringBuilder(FDPConstant.REQUEST_ID)
						.append(FDPConstant.LOGGER_KEY_VALUE_DELIMITER).append(requestId)
						.append(FDPConstant.LOGGER_DELIMITER).append(FDPConstant.ERROR_CODE)
						.append(FDPConstant.LOGGER_KEY_VALUE_DELIMITER)
						.append(FulfillmentResponseCodes.INVALID_PARAMETER.getResponseCode().toString())
						.append(FDPConstant.LOGGER_DELIMITER).append(FDPConstant.ERROR_DESC)
						.append(FDPConstant.LOGGER_KEY_VALUE_DELIMITER)
						.append(String.format(FulfillmentResponseCodes.FAF_MSISDN_INVALID.getDescription(), msisdn))
						.toString();

				FDPLogger.error(getCircleRequestLogger(fdpCircle), getClass(), "process()", errorDescription);
				sendResponse(exchange, FulfillmentResponseCodes.FAF_MSISDN_INVALID,
						FulfillmentParameters.FAF_MSISDN.getValue());
			}

		}
		if (null != fafOldMsisdn) {
			Boolean isOldMsisdn = FnfUtil.ValidateFafMsisdn(fafOldMsisdn);
			if (!isOldMsisdn) {
				LOGGER.info("FafOldMsisdn is not valid FafOldMsisdn = " + fafOldMsisdn);
				errorDescription = new StringBuilder(FDPConstant.REQUEST_ID)
						.append(FDPConstant.LOGGER_KEY_VALUE_DELIMITER).append(requestId)
						.append(FDPConstant.LOGGER_DELIMITER).append(FDPConstant.ERROR_CODE)
						.append(FDPConstant.LOGGER_KEY_VALUE_DELIMITER)
						.append(FulfillmentResponseCodes.INVALID_PARAMETER.getResponseCode().toString())
						.append(FDPConstant.LOGGER_DELIMITER).append(FDPConstant.ERROR_DESC)
						.append(FDPConstant.LOGGER_KEY_VALUE_DELIMITER)
						.append(String.format(FulfillmentResponseCodes.FAF_MSISDN_INVALID.getDescription(), msisdn))
						.toString();

				FDPLogger.error(getCircleRequestLogger(fdpCircle), getClass(), "process()", errorDescription);
				sendResponse(exchange, FulfillmentResponseCodes.FAF_MSISDN_INVALID,
						FulfillmentParameters.OLD_FAF_MSISDN.getValue());
			}
		}

		if (fdpCircle == null) {
			errorDescription = new StringBuilder(FDPConstant.REQUEST_ID).append(FDPConstant.LOGGER_KEY_VALUE_DELIMITER)
					.append(requestId).append(FDPConstant.LOGGER_DELIMITER).append(FDPConstant.ERROR_CODE)
					.append(FDPConstant.LOGGER_KEY_VALUE_DELIMITER)
					.append(FulfillmentResponseCodes.INVALID_PARAMETER.getResponseCode().toString())
					.append(FDPConstant.LOGGER_DELIMITER).append(FDPConstant.ERROR_DESC)
					.append(FDPConstant.LOGGER_KEY_VALUE_DELIMITER)
					.append(String.format(FulfillmentResponseCodes.INVALID_PARAMETER.getDescription(), msisdn))
					.toString();
			FDPLogger.error(getCircleRequestLogger(fdpCircle), getClass(), "process()", errorDescription);
			sendResponse(exchange, FulfillmentResponseCodes.INVALID_PARAMETER, FulfillmentParameters.MSISDN.getValue());
		}/*
		 * else if (!fdpCircle.getCircleCode().equals(circleCode)) {
		 * errorDescription = new StringBuilder(FDPConstant.REQUEST_ID)
		 * .append(FDPConstant.LOGGER_KEY_VALUE_DELIMITER) .append(requestId)
		 * .append(FDPConstant.LOGGER_DELIMITER) .append(FDPConstant.ERROR_CODE)
		 * .append(FDPConstant.LOGGER_KEY_VALUE_DELIMITER)
		 * .append(FulfillmentResponseCodes
		 * .INVALID_CIRCLE_REQUEST.getResponseCode().toString())
		 * .append(FDPConstant.LOGGER_DELIMITER) .append(FDPConstant.ERROR_DESC)
		 * .append(FDPConstant.LOGGER_KEY_VALUE_DELIMITER)
		 * .append(String.format(
		 * FulfillmentResponseCodes.INVALID_CIRCLE_REQUEST.getDescription(),
		 * msisdn, fdpCircle.getCircleCode())).toString();
		 * FDPLogger.error(getCircleRequestLogger(fdpCircle), getClass(),
		 * "process()", errorDescription); sendResponse(exchange,
		 * FulfillmentResponseCodes.INVALID_CIRCLE_REQUEST, msisdn, circleCode);
		 * }
		 */else {
			LOGGER.debug(
					"MSISDN : {} and circle: {} are valid.  Now all the logs of this request to circle request Logs.",
					msisdn, fdpCircle.getCircleCode());
			in.setHeader(FDPRouteHeaders.FDP_CIRCLE.getValue(), fdpCircle);
			in.setHeader(FDPRouteHeaders.MSISDN.getValue(), msisdn);
		}
		LOGGER.debug("Validation of MSISDN and circle DONE.");
	}

}
