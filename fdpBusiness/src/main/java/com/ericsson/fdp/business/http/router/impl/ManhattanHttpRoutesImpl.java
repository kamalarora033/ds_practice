package com.ericsson.fdp.business.http.router.impl;

import java.util.List;

import javax.inject.Inject;

import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.cdi.CdiCamelContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ericsson.fdp.business.constants.BusinessConstants;
import com.ericsson.fdp.business.http.router.ManhattanHttpRoutes;
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
 * The Class ManhattanHttpRoutesImpl responsible for creating VAS
 * External Http Routes.
 * 
 * @author Ericsson
 */

public class ManhattanHttpRoutesImpl implements ManhattanHttpRoutes {

	/** The context. */
	@Inject
	private CdiCamelContext context;

	/** The fdp circle dao. */
	@Inject
	private FDPCircleDAO fdpCircleDAO;

	/** The fdp ema config dao. */
	@Inject
	private FDPVASConfigDAO fdpVasConfigDAO;

	/** The log outgoing ip processor. */
	@Inject
	private LogOutgoingIpProcessor logOutgoingIpProcessor;

	/** The log retry processor. */
	@Inject
	private LogRetryProcessor logRetryProcessor;

	/** The Constant LOGGER. */
	private static final Logger LOGGER = LoggerFactory.getLogger(ManhattanHttpRoutes.class);

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
			final List<FDPVASConfigDTO> fdpVASConfigDTOList = getManhattanDetails(circleCode , ExternalSystemType.MANHATTAN_TYPE);
			for (final FDPVASConfigDTO fdpVASConfigDTO : fdpVASConfigDTOList) {
				context.addRoutes(createRoutesForManhattan(fdpVASConfigDTO, circleCode));
			}
		}
	}

	@Override
	public RouteBuilder createRoutesForManhattan(final FDPVASConfigDTO fdpVASConfigDTO, final String circleCode)
			throws ExecutionFailedException {
		return new RouteBuilder() {
			@SuppressWarnings("unchecked")
			@Override
			public void configure() throws Exception {
				final String logicalName = fdpVASConfigDTO.getLogicalName();
				final String fromEndpoint = HttpUtil.getEndpointForManhattan(circleCode, ExternalSystemType.MANHATTAN_TYPE,
						logicalName);
				LOGGER.debug("FromEndpoint:" + fromEndpoint);
				final String toEndpoint = HttpUtil.getHttpEndpointForManhattan(fdpVASConfigDTO);
				LOGGER.debug("ToEndpoint:" + toEndpoint);
				final String routeId = HttpUtil.getRouteIdForManhattanHttpRoutes(circleCode,
						ExternalSystemType.MANHATTAN_TYPE, logicalName);
				final String ipAddress = fdpVASConfigDTO.getIpAddress().getValue();
				final String circleCodeIpaddressPort = circleCode + BusinessConstants.COLON + ipAddress
						+ BusinessConstants.COLON + String.valueOf(fdpVASConfigDTO.getPort());
				final int retryInterval = fdpVASConfigDTO.getRetryInterval();
				final int numberOfRetry = fdpVASConfigDTO.getNumberOfRetry();
				from(fromEndpoint)
						.routeId(routeId)
						.setProperty(BusinessConstants.OUTGOING_IP_ADDRESS, constant(ipAddress))
						.setProperty(BusinessConstants.OUTGOING_CIRCLE_CODE_IP_PORT, constant(circleCodeIpaddressPort))
						.setProperty(BusinessConstants.LOGICAL_NAME, constant(fdpVASConfigDTO.getLogicalName()))
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
	 * Gets the Manhattan details.
	 * 
	 * @param circleCode
	 *            the circle code
	 * @return the Manhattan details
	 */
	private List<FDPVASConfigDTO> getManhattanDetails(final String circleCode , final ExternalSystemType externalSystemType) {
		return fdpVasConfigDAO.getVASConfigByCircleCodeAndSystemType(circleCode , externalSystemType.getValue());
	}
}
