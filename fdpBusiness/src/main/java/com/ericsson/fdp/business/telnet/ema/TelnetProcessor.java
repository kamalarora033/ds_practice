package com.ericsson.fdp.business.telnet.ema;

import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ericsson.fdp.business.constants.BusinessConstants;
import com.ericsson.fdp.dao.dto.FDPEMAConfigDTO;

public class TelnetProcessor implements Processor {
	
	private final TelnetClientManager telnetClientManager = TelnetClientManager.getInstance();
	private static final Logger LOGGER = LoggerFactory.getLogger(TelnetProcessor.class); 

	public TelnetProcessor() {}
	
	public TelnetProcessor(FDPEMAConfigDTO fDPEMAConfigDTO) {
		configureTelnetClient(fDPEMAConfigDTO);
	}
	
	@Override
	public void process(Exchange exchange) throws Exception {
		//List<String> commands = exchange.getIn().getBody(List.class);
		String commands = exchange.getIn().getBody(String.class);
		exchange.getProperty(BusinessConstants.OUTGOING_IP_ADDRESS);
		
		String response=telnetClientManager.send(commands, (String)exchange.getProperty(BusinessConstants.OUTGOING_IP_ADDRESS),
		        (String)exchange.getProperty(BusinessConstants.CIRCLE_CODE));
		exchange.getOut().setBody(response);
		
	}
	
	private void configureTelnetClient(FDPEMAConfigDTO fDPEMAConfigDTO){
		LOGGER.debug("Configuring EMA node for IP Address : " + fDPEMAConfigDTO.getIpAddress().getValue());
		if (fDPEMAConfigDTO.getIsActive()) {
			TelnetBean telnetBean = new TelnetBean();
			telnetBean.setIpaddress(fDPEMAConfigDTO.getIpAddress().getValue());
			telnetBean.setPort(fDPEMAConfigDTO.getPort());
			telnetBean.setUserName(fDPEMAConfigDTO.getUserName());
			telnetBean.setPassword(fDPEMAConfigDTO.getPassword());
			LOGGER.debug("Number of telnet sessions : " + fDPEMAConfigDTO.getNumberOfSessions());
			telnetBean.setNumberofsessions(fDPEMAConfigDTO.getNumberOfSessions());
			telnetBean.setTimeout(fDPEMAConfigDTO.getTimeout());
			telnetBean.setSotimeout(fDPEMAConfigDTO.getTimeout());
			telnetBean.setLogin(fDPEMAConfigDTO.getLogin());
			telnetBean.setLogout(fDPEMAConfigDTO.getLogout());
			telnetClientManager.populateTelnetClient(telnetBean);		
			LOGGER.debug("Configuration done for EMA node");
		}
		
	}

}
