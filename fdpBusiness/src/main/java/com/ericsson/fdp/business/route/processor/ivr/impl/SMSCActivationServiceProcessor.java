package com.ericsson.fdp.business.route.processor.ivr.impl;

import javax.inject.Inject;

import org.apache.camel.Exchange;
import org.apache.camel.Message;
import org.apache.camel.Processor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ericsson.fdp.core.monitor.SMSCMonitoringPublisher;
import com.ericsson.fdp.dao.dto.SMSCUpdateDTO;

/**
 * This class is used to intercept the request coming from Global Traffic Manager and process it 
 * @author GUR21122
 *
 */
public class SMSCActivationServiceProcessor implements Processor{
	
	/** The Constant LOGGER. */
	private static final Logger LOGGER = LoggerFactory.getLogger(SMSCActivationServiceProcessor.class);
	
	@Inject
	private SMSCMonitoringPublisher smscMonitoringPublisher;
	
	@Override
	public void process(final Exchange exchange) throws Exception {
		
		LOGGER.info("Request received from GTP/LTM");
		final Message in = exchange.getIn();
		final String action = (String) in.getHeader("ACTION");
		SMSCUpdateDTO smscUpdateDTO = new SMSCUpdateDTO();
		smscUpdateDTO.setAction(action);
		smscMonitoringPublisher.pushToTopic(smscUpdateDTO);
	}

}
