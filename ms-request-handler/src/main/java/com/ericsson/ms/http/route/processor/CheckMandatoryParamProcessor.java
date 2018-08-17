package com.ericsson.ms.http.route.processor;

import org.apache.camel.Exchange;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;

import com.ericsson.ms.common.constants.NBRequestHandlerConstant;
import com.ericsson.ms.common.enums.NorthBoundResponseCode;
import com.ericsson.ms.common.enums.RequestValuesEnum;
import com.ericsson.ms.common.util.ReqHandlerLoggerUtil;
import com.ericsson.ms.http.route.framework.AbstractNorthBoundProcessor;

/**
 * The Update Mandatory Param Processor class
 * 
 * @author Ericsson
 *
 */
@Service
@Scope(value = "prototype")
public class CheckMandatoryParamProcessor extends AbstractNorthBoundProcessor {

	private static final Logger logger = LoggerFactory.getLogger(CheckMandatoryParamProcessor.class);

	@Override
	public void process(Exchange exchange) throws Exception {
		for (RequestValuesEnum requestValuesEnum : RequestValuesEnum.values()) {
			String value = exchange.getIn().getHeader(requestValuesEnum.getValue(), String.class);
			if (requestValuesEnum.isMandatory() && (value == null || value.isEmpty())) {
				ReqHandlerLoggerUtil.debug(logger, getClass(), "process()",
						NorthBoundResponseCode.INVALID_PARAMETER.getResponseDesc()+exchange.getIn().getHeaders());
				sendResponseAndStopRoute(exchange, NorthBoundResponseCode.INVALID_PARAMETER, value,NBRequestHandlerConstant.FAILURE);
				break;
			}
		}

	}

}
