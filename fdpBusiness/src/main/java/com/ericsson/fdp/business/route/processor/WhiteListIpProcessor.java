package com.ericsson.fdp.business.route.processor;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.annotation.Resource;

import org.apache.camel.Exchange;
import org.apache.camel.Message;
import org.apache.camel.Processor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ericsson.fdp.common.constants.FDPConstant;
import com.ericsson.fdp.core.cache.AppCacheSubStore;
import com.ericsson.fdp.core.dsm.framework.service.FDPCache;
import com.ericsson.fdp.core.serviceprovisioning.bean.FDPAppBag;

/**
 * The Class WhiteListIpProcessor is used to filter the request from incoming
 * IPs , if incoming is whitelisted then it will continue the processing other
 * it will stop the exchange.
 */
// @Named
public class WhiteListIpProcessor implements Processor {

	/** The Constant LOGGER. */
	private static final Logger LOGGER = LoggerFactory
			.getLogger(WhiteListIpProcessor.class);

	/** The whitelisted ip. */
	private final String WHITELISTED_IP = "CGW_WHITELISTED_IP";

	/** The application config cache. */
	@Resource(lookup = "java:app/fdpCoreServices-1.0/ApplicationConfigCache")
	private FDPCache<FDPAppBag, Object> applicationConfigCache;

	@Override
	public void process(Exchange exchange) throws Exception {
		Message in = exchange.getIn();
		String host = in.getHeader("host", String.class);
		String incomingIpAddress = null;
		if(host.indexOf(":") > 0) {
			incomingIpAddress =  host.substring(0, host.lastIndexOf(":"));
		} else {
			incomingIpAddress = host;
		}

		FDPAppBag appBag = new FDPAppBag();
		appBag.setSubStore(AppCacheSubStore.CONFIGURATION_MAP);
		appBag.setKey(WHITELISTED_IP);
		String whiteListIpsStr = (String) applicationConfigCache
				.getValue(appBag);

		// Extracting all the ip addresses from the Whitelist string, which
		// contains IP addresses as 'circleName1=ip1,ip2;circleName2=ip1,ip2;'
		List<String> whiteListIpsArr = new ArrayList<String>();

		Pattern pattern = Pattern.compile(FDPConstant.IP_PATTERN);
		if (whiteListIpsStr != null) {
				Matcher matcher = pattern.matcher(whiteListIpsStr);

				while (matcher.find()) {
					whiteListIpsArr.add(matcher.group());
				}

				if (!whiteListIpsArr.contains(incomingIpAddress)) {
					exchange.setProperty(Exchange.ROUTE_STOP, Boolean.TRUE);
					LOGGER.info("IP {} is not whitelisted.", incomingIpAddress);
				}
			} else {
				exchange.setProperty(Exchange.ROUTE_STOP, Boolean.TRUE);
				LOGGER.info("IP {} is not whitelisted.", incomingIpAddress);
			}
		
	}
}
