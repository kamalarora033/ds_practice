package com.ericsson.fdp.business.batchjob.airDataProcessor.processor;

import javax.inject.Inject;
import javax.naming.Context;
import javax.naming.InitialContext;

import org.apache.camel.Exchange;
import org.apache.camel.Message;
import org.apache.camel.Processor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ericsson.fdp.business.batchjob.airDataProcessor.service.AIRDataProcessorService;
import com.ericsson.fdp.business.constants.BusinessConstants;
import com.ericsson.fdp.common.constants.JNDILookupConstant;
import com.ericsson.fdp.route.cdi.context.provider.CdiCamelContextProvider;

/**
 * The Class HandleAIROfflineIncomingFileRecords is used to handle and process
 * incoming records and filter circle on basis of msisdn.
 * 
 * @author Ericsson
 */
public class HandleAIROfflineIncomingFileRecords implements Processor {

	/** The cdi camel context provider. */
	@Inject
	private CdiCamelContextProvider cdiCamelContextProvider;

	/** The logger. */
	private static final Logger LOGGER = LoggerFactory.getLogger(HandleAIROfflineIncomingFileRecords.class);

	@Override
	public void process(final Exchange exchange) throws Exception {

		final Message in = exchange.getIn();
		String msg = in.getBody(String.class);
		msg = msg.replaceAll("\n", "");
		msg = msg.replaceAll("\r", "");
		msg = msg.trim();
		msg = msg.replaceAll(" ", "");
		final String circleCode = in.getHeader(BusinessConstants.CIRCLE_CODE).toString();
		LOGGER.debug("File incoming records :", msg);
		LOGGER.debug("Circle Code :", circleCode);

		if (msg.indexOf("$") > 1) {
			final String[] xmlMessages = msg.split("\\$");
			final Context initialContext = new InitialContext();
			final AIRDataProcessorService airDataProcessor = (AIRDataProcessorService) initialContext
					.lookup(JNDILookupConstant.AIR_DATA_PROCESSOR_SERVICE_LOOK_UP);
			final Boolean isSaved = airDataProcessor.saveBatchExecutionInfo(xmlMessages, circleCode);
			if (isSaved) {
				LOGGER.info("Batch job info saved successfully.");
			} else {
				LOGGER.error("Batch job info save failed.");
			}

		}
	}
}
