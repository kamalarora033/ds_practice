package com.ericsson.fdp.business.http.router.impl;

import java.util.Arrays;
import java.util.List;

import javax.inject.Inject;

import org.apache.camel.ExchangePattern;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.cdi.CdiCamelContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ericsson.fdp.business.constants.BusinessConstants;
import com.ericsson.fdp.business.http.router.SBBBHttpRoute;
import com.ericsson.fdp.business.route.processor.LogOutgoingIpProcessor;
import com.ericsson.fdp.business.util.HttpUtil;
import com.ericsson.fdp.common.constants.FDPConstant;
import com.ericsson.fdp.common.enums.BusinessModuleType;
import com.ericsson.fdp.core.exception.ExecutionFailedException;
import com.ericsson.fdp.core.utils.ApplicationConfigUtil;
import com.ericsson.fdp.dao.dto.FDPVASConfigDTO;
import com.ericsson.fdp.dao.entity.ExternalSystemType;
import com.ericsson.fdp.dao.fdpadmin.FDPCircleDAO;
import com.ericsson.fdp.dao.fdpadmin.FDPVASConfigDAO;
import com.ericsson.fdp.route.processors.LogRetryProcessor;

public class SBBBHttpRouteImpl implements SBBBHttpRoute {
	
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
	private static final Logger LOGGER = LoggerFactory.getLogger(SBBBHttpRoute.class);
	
	public static List<String> commandNames = Arrays.asList("AddConsumer","DeleteConsumer","GetDetails");
	
	@Override
	public RouteBuilder createRoutesForSBBB(final FDPVASConfigDTO fdpVASConfigDTO,
			final String circleCode, final String commandName) throws ExecutionFailedException {
		return new RouteBuilder() {
			@SuppressWarnings("unchecked")
			@Override
			public void configure() throws Exception {
				final String logicalName = fdpVASConfigDTO.getLogicalName();
				final String fromEndpoint = HttpUtil.getEndpointForSBBB(circleCode, ExternalSystemType.SBBB,
						logicalName,commandName);
				LOGGER.debug("FromEndpoint:" + fromEndpoint);
				final String toEndpoint = HttpUtil.getHttpEndpointForSBBB(fdpVASConfigDTO, commandName);
				LOGGER.debug("ToEndpoint:" + toEndpoint);
				final String routeId = HttpUtil.getRouteIdForSBBBHttpRoutes(circleCode,
						ExternalSystemType.SBBB, logicalName)+FDPConstant.UNDERSCORE+commandName;
				final String ipAddress = fdpVASConfigDTO.getIpAddress().getValue();
				final String circleCodeIpaddressPort = circleCode + BusinessConstants.COLON + ipAddress
						+ BusinessConstants.COLON + String.valueOf(fdpVASConfigDTO.getPort());
				final int retryInterval = fdpVASConfigDTO.getRetryInterval();
				final int numberOfRetry = fdpVASConfigDTO.getNumberOfRetry();
				System.out.println("From :"+fromEndpoint+", to:"+toEndpoint);
				from(fromEndpoint)
						.routeId(routeId).setExchangePattern(ExchangePattern.InOut)
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

	public void createSbbbRoutes() throws Exception {
		context = ApplicationConfigUtil.getCdiCamelContextProvider().getContext();
		final List<String> circleCodes = fdpCircleDAO.getAllCircleCodes();
		for (final String circleCode : circleCodes) {
			final List<FDPVASConfigDTO> fdpVASConfigDTOList = getSbbbDetails(circleCode , ExternalSystemType.SBBB);
			for (final FDPVASConfigDTO fdpVASConfigDTO : fdpVASConfigDTOList) {
				for(final String commandName : commandNames) {
					context.addRoutes(createRoutesForSBBB(fdpVASConfigDTO, circleCode, commandName));
				}
			}
		}
		
	}

	private List<FDPVASConfigDTO> getSbbbDetails(String circleCode,
			ExternalSystemType externalSystemType) {
		
		return fdpVasConfigDAO.getVASConfigByCircleCodeAndSystemType(circleCode , externalSystemType.getValue());

	}

}
