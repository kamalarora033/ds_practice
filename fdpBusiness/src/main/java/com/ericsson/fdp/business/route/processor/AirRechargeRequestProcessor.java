package com.ericsson.fdp.business.route.processor;

import java.net.Inet4Address;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.UUID;

import org.apache.camel.Exchange;
import org.apache.camel.Message;
import org.apache.camel.Processor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ericsson.fdp.business.airrecharge.service.AirRechargeProcessor;
import com.ericsson.fdp.business.enums.OperatingMode;
import com.ericsson.fdp.common.enums.ExternalSystem;
import com.ericsson.fdp.core.utils.ApplicationConfigUtil;

/**
 * The Class AirRechargeRequestProcessor is used to create routes for air
 * recharge request which comes from external systems.
 * 
 * @author Ericsson
 */
public class AirRechargeRequestProcessor implements Processor {

	/** The logger. */
	private static final Logger LOGGER = LoggerFactory.getLogger(AirRechargeRequestProcessor.class);

	@Override
	public void process(final Exchange exchange) throws Exception {

		final Message in = exchange.getIn();
		final Message out = exchange.getOut();

		final String contentType = in.getHeader(Exchange.CONTENT_TYPE, String.class);
		String path = in.getHeader(Exchange.HTTP_URI, String.class);
		String incomingIPAddress = in.getHeader("host", String.class);
		if(incomingIPAddress.indexOf(":") > 0) {
			incomingIPAddress = incomingIPAddress.substring(0, incomingIPAddress.lastIndexOf(":"));
		}
		path = path.substring(path.lastIndexOf("/"));
		final String inputRequestXML = in.getBody(String.class);
		final String charsetEncoding = in.getHeader(Exchange.HTTP_CHARACTER_ENCODING, String.class);
		LOGGER.debug("CharsetEncoding :", charsetEncoding);
		LOGGER.debug("Request from client :", inputRequestXML);
		final String requestId = ExternalSystem.AIR.name() + "_" + Inet4Address.getLocalHost().getHostAddress() + "_"
				+ (String.valueOf(UUID.randomUUID()));
		LOGGER.info("Generated Request Id:" + requestId);
		LOGGER.info("Request XML :" + inputRequestXML);
		final AirRechargeProcessor airRechargeProcessor = (AirRechargeProcessor) ApplicationConfigUtil
				.getBean("java:global/fdpBusiness-ear/fdpBusiness-1.0/AirRechargeProcessorImpl");
		final String responseXML = airRechargeProcessor.executeAirRecharge(inputRequestXML, requestId,
				OperatingMode.ONLINE, incomingIPAddress);
		final int contentLength = responseXML.length();
		final String reponsePrefixHeader = "HTTP/1.0 200 OK\nServer: BaseHTTP/0.3 Python/2.4.6\nDate: "
				+ getFormattedCurrentDateTime() + "\nContent-Type: text/xml\nContent-Length: " + contentLength + "\n\n";
		LOGGER.debug("Resposne XML :" + responseXML);
		out.setBody(reponsePrefixHeader + responseXML);
		out.setHeader(Exchange.CONTENT_TYPE, contentType);

	}

	/**
	 * Gets the formatted current date time.
	 * 
	 * @return the formatted current date time
	 */
	private static String getFormattedCurrentDateTime() {
		return new SimpleDateFormat("EEE, dd MMM yyyy HH:mm:ss ").format(new Date()).toString() + "GMT";

	}

}
