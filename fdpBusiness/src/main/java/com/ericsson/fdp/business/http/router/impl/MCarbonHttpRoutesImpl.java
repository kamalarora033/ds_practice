package com.ericsson.fdp.business.http.router.impl;

import java.util.List;

import javax.inject.Inject;

import org.apache.camel.Endpoint;
import org.apache.camel.Exchange;
import org.apache.camel.ExchangePattern;
import org.apache.camel.Message;
import org.apache.camel.Producer;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.cdi.CdiCamelContext;
import org.apache.camel.component.direct.DirectEndpoint;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ericsson.fdp.business.constants.BusinessConstants;
import com.ericsson.fdp.business.http.router.MCarbonHttpRoutes;
import com.ericsson.fdp.business.route.processor.LogOutgoingIpProcessor;
import com.ericsson.fdp.business.util.HttpUtil;
import com.ericsson.fdp.common.enums.BusinessModuleType;
import com.ericsson.fdp.core.exception.ExecutionFailedException;
import com.ericsson.fdp.core.utils.ApplicationConfigUtil;
import com.ericsson.fdp.dao.dto.FDPVASConfigDTO;
import com.ericsson.fdp.dao.entity.ExternalSystemType;
import com.ericsson.fdp.dao.fdpadmin.FDPCircleDAO;
import com.ericsson.fdp.dao.fdpadmin.FDPVASConfigDAO;
import com.ericsson.fdp.route.processors.LogRetryProcessor;

/**
 * The Class MCarbonHttpRoutesForSMSAndUSSD responsible for creating VAS
 * External Http Routes.
 * 
 * @author Ericsson
 */

public class MCarbonHttpRoutesImpl implements MCarbonHttpRoutes {

	private static final long serialVersionUID = 2974458519723957120L;

	/** The context. */
	@Inject
	private CdiCamelContext context;

	/** The fdp circle dao. */
	@Inject
	private FDPCircleDAO fdpCircleDAO;

	/** The fdp ema config dao. */
	@Inject
	private FDPVASConfigDAO fdpVASConfigDAO;

	/** The log outgoing ip processor. */
	@Inject
	private LogOutgoingIpProcessor logOutgoingIpProcessor;

	/** The log retry processor. */
	@Inject
	private LogRetryProcessor logRetryProcessor;

	/** The Constant LOGGER. */
	private static final Logger LOGGER = LoggerFactory.getLogger(MCarbonHttpRoutes.class);

	/**
	 * Creates the routes.
	 * 
	 * @throws Exception
	 *             the exception
	 */
	public void createRoutesVAS() throws Exception {
		context = ApplicationConfigUtil.getCdiCamelContextProvider().getContext();
		final List<String> circleCodes = fdpCircleDAO.getAllCircleCodes();
		for (final String circleCode : circleCodes) {
			final List<FDPVASConfigDTO> fdpMCarbonConfigDTOs = getMCARBONDetails(circleCode);
			for (final FDPVASConfigDTO fdpMCarbonConfigDTO : fdpMCarbonConfigDTOs) {
				context.addRoutes(createRoutesForMCarbon(fdpMCarbonConfigDTO, circleCode));
			}
		}
	}

	@Override
	public RouteBuilder createRoutesForMCarbon(final FDPVASConfigDTO fdpMCarbonConfigDTO, final String circleCode)
			throws ExecutionFailedException {
		return new RouteBuilder() {
			@SuppressWarnings("unchecked")
			@Override
			public void configure() throws Exception {
				final String logicalName = fdpMCarbonConfigDTO.getLogicalName();
				final String fromEndpoint = HttpUtil.getEndpointForMCarbon(circleCode, ExternalSystemType.MCARBON_TYPE,
						logicalName);
				LOGGER.debug("FromEndpoint:" + fromEndpoint);
				final String toEndpoint = HttpUtil.getHttpEndpointForMCarbon(fdpMCarbonConfigDTO);
				LOGGER.debug("ToEndpoint:" + toEndpoint);
				final String routeId = HttpUtil.getRouteIdForMCarbonHttpRoutes(circleCode,
						ExternalSystemType.MCARBON_TYPE, logicalName);
				final String ipAddress = fdpMCarbonConfigDTO.getIpAddress().getValue();
				final String circleCodeIpaddressPort = circleCode + BusinessConstants.COLON + ipAddress
						+ BusinessConstants.COLON + String.valueOf(fdpMCarbonConfigDTO.getPort());
				final int retryInterval = fdpMCarbonConfigDTO.getRetryInterval();
				final int numberOfRetry = fdpMCarbonConfigDTO.getNumberOfRetry();
				from(fromEndpoint)
						.routeId(routeId)
						.setProperty(BusinessConstants.OUTGOING_IP_ADDRESS, constant(ipAddress))
						.setProperty(BusinessConstants.OUTGOING_CIRCLE_CODE_IP_PORT, constant(circleCodeIpaddressPort))
						.setProperty(BusinessConstants.LOGICAL_NAME, constant(fdpMCarbonConfigDTO.getLogicalName()))
						.setProperty(BusinessConstants.MODULE_NAME, constant(BusinessModuleType.VAS_SOUTH.name()))
						.process(logOutgoingIpProcessor)
						.onException(java.lang.Exception.class, java.net.ConnectException.class,
								java.net.SocketTimeoutException.class,
								org.apache.camel.component.http.HttpOperationFailedException.class)
						.maximumRedeliveries(numberOfRetry).redeliveryDelay(retryInterval)
						.onRedelivery(logRetryProcessor).end().to(toEndpoint);
			}
		};
	}

	/**
	 * Gets the mCARBON details.
	 * 
	 * @param circleCode
	 *            the circle code
	 * @return the mCARBON details
	 */
	private List<FDPVASConfigDTO> getMCARBONDetails(final String circleCode) {
		return fdpVASConfigDAO.getVASConfigByCircleCodeAndSystemType(circleCode, ExternalSystemType.MCARBON_TYPE.getValue());
	}

	/**
	 * Used for testing.
	 */
	private void callClient() throws Exception {

		final String httpRequest = "MSISDN=918447192920&channelType=USSD&systemType=MCARBON&input=*123%23";
		final Endpoint endpoint = context.getEndpoint("direct:DEL_MCarbon_MCARBON_DELHI", DirectEndpoint.class);
		final Exchange exchange = endpoint.createExchange();
		exchange.setPattern(ExchangePattern.InOut);
		final Message in = exchange.getIn();
		in.setHeader(Exchange.HTTP_PROTOCOL_VERSION, "VAS/service/MOPush");
		in.setHeader(Exchange.CONTENT_TYPE, "text/html");
		in.setHeader(Exchange.HTTP_METHOD, BusinessConstants.HTTP_METHOD);
		in.setHeader(Exchange.CONTENT_LENGTH, httpRequest.length());
		exchange.setProperty(BusinessConstants.CIRCLE_CODE, "DEL");
		exchange.setProperty(BusinessConstants.EXTERNAL_SYSTEM_TYPE, "MCARBON");
		exchange.setProperty(BusinessConstants.REQUEST_ID, "123");
		exchange.setProperty(BusinessConstants.MSISDN, "8447192920");
		in.setBody(httpRequest);
		in.setHeader(Exchange.HTTP_QUERY, "input=" + httpRequest);
		String outputXML = "";
		final Producer producer = endpoint.createProducer();
		producer.process(exchange);
		final Message out = exchange.getOut();
		outputXML = out.getBody(String.class);
		final String responseCode = out.getHeader("camelhttpresponsecode", String.class);
		if (outputXML != null) {
			LOGGER.debug("Response Code :  | Response XML from callClient :" + responseCode, outputXML);
			LOGGER.debug("outputXML :" + outputXML);
		} else {
			LOGGER.debug("Response Code :  | output xml is null ");
		}
	}

}
