package com.ericsson.fdp.business.route.processor;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;

import org.apache.camel.Exchange;
import org.apache.camel.Message;
import org.apache.camel.Processor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ericsson.fdp.business.constants.BusinessConstants;
import com.ericsson.fdp.common.constants.FDPConstant;
import com.ericsson.fdp.core.cache.AppCacheSubStore;
import com.ericsson.fdp.core.dsm.framework.service.FDPCache;
import com.ericsson.fdp.core.serviceprovisioning.bean.FDPAppBag;
import com.ericsson.fdp.route.constant.RoutingConstant;

/**
 * The Class IVRWhiteListIpProcessor.
 */
public class VASWhiteListIpProcessor implements Processor {

	/** The Constant LOGGER. */
	private static final Logger LOGGER = LoggerFactory.getLogger(VASWhiteListIpProcessor.class);

	/** The application cache. */
	@Resource(lookup = "java:app/fdpCoreServices-1.0/ApplicationConfigCache")
	private FDPCache<FDPAppBag, Object> applicationConfigCache;

	@Override
	public void process(Exchange exchange) {
		Message in = exchange.getIn();
		HttpServletRequest request = in.getBody(HttpServletRequest.class);
		String incomingIpAddress = request.getRemoteAddr();
		String requestId = in.getHeader(BusinessConstants.REQUEST_ID, String.class);
		
		if(incomingIpAddress.indexOf(":") > 0) {
			incomingIpAddress = incomingIpAddress.substring(0, incomingIpAddress.lastIndexOf(":"));
		}
		in.setHeader(RoutingConstant.INCOMING_IP_ADDRESS, incomingIpAddress);
		String whiteListIpsStr = getWhiteListIpStringFromAdmin();
		LOGGER.info("IP {} of "+BusinessConstants.REQUEST_ID+requestId+" is " +incomingIpAddress);

		if(whiteListIpsStr == null){
			exchange.setProperty(Exchange.ROUTE_STOP, Boolean.TRUE);
			LOGGER.info("IP {} is not whitelisted.", incomingIpAddress);
		}else {
			List<String> whiteListIpsArr = getWhiteListIpsForVAS(exchange, incomingIpAddress, whiteListIpsStr);
			if (!whiteListIpsArr.contains(incomingIpAddress)) {
				exchange.setProperty(Exchange.ROUTE_STOP, Boolean.TRUE);
				LOGGER.info("IP {} is not whitelisted.", incomingIpAddress);
			}
		}
	}


	/**
	 * Gets the white list ips from admin.
	 *
	 * @return the white list ips from admin
	 */
	private String getWhiteListIpStringFromAdmin() {
		String whiteListIpsStr = null;
		FDPAppBag appBag = new FDPAppBag();
		appBag.setSubStore(AppCacheSubStore.CONFIGURATION_MAP);
		appBag.setKey(FDPConstant.VAS_CONFIGURATION_IP);
		Object object =	applicationConfigCache.getValue(appBag);
		if(object != null && object instanceof String) {
			whiteListIpsStr = (String) object;
		}
		return whiteListIpsStr;
	}


	/**
	 * Gets the white list ips for vas.
	 *
	 * @param exchange the exchange
	 * @param incomingIpAddress the incoming ip address
	 * @param whiteListIpsStr the white list ips str
	 * @return the white list ips for vas
	 */
	private List<String> getWhiteListIpsForVAS(Exchange exchange, String incomingIpAddress, String whiteListIpsStr) {
		List<String> whiteListIpsArr = new ArrayList<String>();
		Pattern pattern = Pattern.compile(FDPConstant.IP_PATTERN);
		Matcher matcher = pattern.matcher(whiteListIpsStr);
		while (matcher.find()) {
			whiteListIpsArr.add(matcher.group());
		}
		return whiteListIpsArr;
	}
}
