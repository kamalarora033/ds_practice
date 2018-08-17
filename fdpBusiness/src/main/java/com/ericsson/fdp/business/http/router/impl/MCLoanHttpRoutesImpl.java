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
import org.apache.camel.model.LoadBalanceDefinition;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ericsson.fdp.business.constants.BusinessConstants;
import com.ericsson.fdp.business.http.router.MCLoanHttpRoutes;
import com.ericsson.fdp.business.http.router.MCarbonHttpRoutes;
import com.ericsson.fdp.business.route.processor.HttpHeaderProcessor;
import com.ericsson.fdp.business.route.processor.LogOutgoingIpProcessor;
import com.ericsson.fdp.business.util.HttpUtil;
import com.ericsson.fdp.common.enums.BusinessModuleType;
import com.ericsson.fdp.common.enums.ExternalSystem;
import com.ericsson.fdp.core.exception.ExecutionFailedException;
import com.ericsson.fdp.core.utils.ApplicationConfigUtil;
import com.ericsson.fdp.dao.dto.FDPExternalSystemDTO;
import com.ericsson.fdp.dao.dto.FDPMCLoanConfigDTO;
import com.ericsson.fdp.dao.dto.FDPVASConfigDTO;
import com.ericsson.fdp.dao.entity.ExternalSystemType;
import com.ericsson.fdp.dao.fdpadmin.FDPCircleDAO;
import com.ericsson.fdp.dao.fdpadmin.FDPMCLoanConfigDAO;
import com.ericsson.fdp.dao.fdpadmin.FDPVASConfigDAO;
import com.ericsson.fdp.route.processors.LogRetryProcessor;

/**
 * The Class MCarbonHttpRoutesForSMSAndUSSD  for Loan responsible for creating VAS
 * External Http Routes.
 * 
 * @author evivbeh
 */

public class MCLoanHttpRoutesImpl implements MCLoanHttpRoutes {

	private static final long serialVersionUID = 2974458512723956120L;

	/** The context. */
	@Inject
	private CdiCamelContext context;

	/** The fdp circle dao. */
	@Inject
	private FDPCircleDAO fdpCircleDAO;

	/** The fdp ema config dao. */
	@Inject
	private FDPMCLoanConfigDAO fdpMCLoanConfigDAO;
	
	

	/** The log outgoing ip processor. */
	@Inject
	private LogOutgoingIpProcessor logOutgoingIpProcessor;

	/** The log retry processor. */
	@Inject
	private LogRetryProcessor logRetryProcessor;
	
	/**
	 * The headerProcessor is used to process the headers at run time mapped by
	 * ip and port.
	 */
	@Inject
	private HttpHeaderProcessor headerProcessor;

	/** The Constant LOGGER. */
	private static final Logger LOGGER = LoggerFactory.getLogger(MCLoanHttpRoutesImpl.class);

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
			final List<FDPMCLoanConfigDTO> fdpMCLoanConfigDTOs = getMCLoanConfigDTOList(circleCode);
			for (final FDPMCLoanConfigDTO fdpMCLoanConfigDTO : fdpMCLoanConfigDTOs) {
				context.addRoutes(createRoutesForMCLoan(fdpMCLoanConfigDTO, circleCode));
			}
			context.addRoutes(createLbRouteForMCLoan(fdpMCLoanConfigDTOs, circleCode));
		}
	}

	@Override
	public RouteBuilder createRoutesForMCLoan(final FDPMCLoanConfigDTO fdpMCLoanConfigDTO, final String circleCode)
			throws ExecutionFailedException {
		return new RouteBuilder() {
			@SuppressWarnings("unchecked")
			@Override
			public void configure() throws Exception {
				
				if(!fdpMCLoanConfigDTO.getIsActive()){
					return;
				}
				
				final String logicalName = fdpMCLoanConfigDTO.getLogicalName();
				final String fromEndpoint = HttpUtil.getEndpointForMCarbonLoan(circleCode, ExternalSystemType.MCLOAN_TYPE,
						logicalName, fdpMCLoanConfigDTO.getExternalSystemId());
				LOGGER.debug("FromEndpoint:" + fromEndpoint);
				final String toEndpoint = HttpUtil.getHttpEndpointForMCLoan(fdpMCLoanConfigDTO);
				LOGGER.debug("ToEndpoint:" + toEndpoint);
				final String routeId = HttpUtil.getRouteIdForMCarbonLoanHttpRoutes(circleCode,
						ExternalSystemType.MCLOAN_TYPE, logicalName, fdpMCLoanConfigDTO.getExternalSystemId());
				final String ipAddress = fdpMCLoanConfigDTO.getIpAddress().getValue();
				final String circleCodeIpaddressPort = circleCode + BusinessConstants.COLON + ipAddress
						+ BusinessConstants.COLON + String.valueOf(fdpMCLoanConfigDTO.getPort());
				final int retryInterval = fdpMCLoanConfigDTO.getRetryInterval();
				final int numberOfRetry = fdpMCLoanConfigDTO.getNumberOfRetry();
				from(fromEndpoint)
						.routeId(routeId)
						.setProperty(BusinessConstants.OUTGOING_IP_ADDRESS, constant(ipAddress))
						.setProperty(BusinessConstants.OUTGOING_CIRCLE_CODE_IP_PORT, constant(circleCodeIpaddressPort))
						.setProperty(BusinessConstants.LOGICAL_NAME, constant(fdpMCLoanConfigDTO.getLogicalName()))
						.setProperty(BusinessConstants.MODULE_NAME, constant(BusinessModuleType.MCLOAN_SOUTH.name()))
						.process(logOutgoingIpProcessor)
						.process(headerProcessor)
						.onException(java.lang.Exception.class, java.net.ConnectException.class,
								java.net.SocketTimeoutException.class,
								org.apache.camel.component.http.HttpOperationFailedException.class)
						.maximumRedeliveries(numberOfRetry).redeliveryDelay(retryInterval)
						.onRedelivery(logRetryProcessor).end().to(toEndpoint);
			}
		};
	}
	
	@Override
	public RouteBuilder createLbRouteForMCLoan(final List<FDPMCLoanConfigDTO> mcLoanDTOList, final String circleCode){
		return new RouteBuilder(){

			@Override
			public void configure() throws Exception {
				LoadBalanceDefinition loadBalanceDefinition = null;
				String fromEndpoint = BusinessConstants.HTTP_COMPONENT_MCLOAN_ENDPOINT + BusinessConstants.UNDERSCORE + circleCode;
				LOGGER.debug("from endpoint: "+fromEndpoint);
				if (mcLoanDTOList != null && mcLoanDTOList.size() > 1) {

					loadBalanceDefinition = from(fromEndpoint)
							.routeId(ExternalSystem.MCLOAN.name() + BusinessConstants.UNDERSCORE + circleCode)
							.setExchangePattern(ExchangePattern.InOut)
							.loadBalance()
							.failover(mcLoanDTOList.size() - 1, false, true, java.lang.Exception.class);
					
					for (final FDPMCLoanConfigDTO mcLoanDto : mcLoanDTOList) {
						final String logicalName = mcLoanDto.getLogicalName();
						final String toEndpoint = HttpUtil.getEndpointForMCarbonLoan(circleCode, ExternalSystemType.MCLOAN_TYPE,
								logicalName, mcLoanDto.getExternalSystemId());
						LOGGER.debug("ToEndpoint:" + toEndpoint);
						loadBalanceDefinition.routeId(toEndpoint).to(toEndpoint);
					}
				}
				else if(mcLoanDTOList != null && mcLoanDTOList.size() == 1){
					final String logicalName = mcLoanDTOList.get(0).getLogicalName();
					final String toEndpoint = HttpUtil.getEndpointForMCarbonLoan(circleCode, ExternalSystemType.MCLOAN_TYPE,
							logicalName, mcLoanDTOList.get(0).getExternalSystemId());
					from(BusinessConstants.HTTP_COMPONENT_MCLOAN_ENDPOINT + BusinessConstants.UNDERSCORE + circleCode)
					.setExchangePattern(ExchangePattern.InOut)
					.routeId(ExternalSystem.MCLOAN.name() + BusinessConstants.UNDERSCORE + circleCode)
					.to(toEndpoint);
				}
			}
			
		};
	}
	

	/**
	 * Gets the mCARBON Loan details.
	 * 
	 * @param circleCode
	 *            the circle code
	 * @return the mCARBON loan details
	 */
	private List<FDPMCLoanConfigDTO> getMCLoanConfigDTOList(final String circleCode) {
		return fdpMCLoanConfigDAO.getMCLoanConfigDTOListByCircleCode(circleCode);
	}

	@Override
	public void stopRouteforMcLoan(FDPMCLoanConfigDTO fdpMCLoanConfigDTO)
			throws Exception {
		context = ApplicationConfigUtil.getCdiCamelContextProvider().getContext();
		final String routeId = HttpUtil.getRouteIdForMCarbonLoanHttpRoutes(fdpMCLoanConfigDTO.getCircleDTO().getCircleCode(),
				ExternalSystemType.MCLOAN_TYPE, fdpMCLoanConfigDTO.getLogicalName(), fdpMCLoanConfigDTO.getExternalSystemId());
		context.stopRoute(routeId);
		context.removeRoute(routeId);
		LOGGER.debug("Stopped Route: "+routeId);
	}

	@Override
	public void stopLbRouteForMcLoan(String circleCode) throws Exception {
		context = ApplicationConfigUtil.getCdiCamelContextProvider().getContext();
		final String routeId = ExternalSystem.MCLOAN.name() + BusinessConstants.UNDERSCORE + circleCode;
		context.stopRoute(routeId);
		context.removeRoute(routeId);
		LOGGER.debug("Stopped LB Route: "+routeId);
	}
	
	@Override
	public void stopAllRoutesForMcLoan(String circleCode) throws Exception{
		final List<FDPMCLoanConfigDTO> fdpMCLoanConfigDTOs = getMCLoanConfigDTOList(circleCode);
		context = ApplicationConfigUtil.getCdiCamelContextProvider().getContext();
		for(FDPMCLoanConfigDTO mcLoanDto : fdpMCLoanConfigDTOs){
			final String routeId = HttpUtil.getRouteIdForMCarbonLoanHttpRoutes(circleCode,
					ExternalSystemType.MCLOAN_TYPE, mcLoanDto.getLogicalName(), mcLoanDto.getExternalSystemId());
			context.stopRoute(routeId);
			context.removeRoute(routeId);
			LOGGER.debug("Stopped Route: "+routeId);
		}
		
		LOGGER.debug("Stopped All Routes for circle: "+circleCode);
	}
	
	@Override
	public void startAllRoutesForMcLoan(String circleCode) throws Exception{
		final List<FDPMCLoanConfigDTO> fdpMCLoanConfigDTOs = getMCLoanConfigDTOList(circleCode);
		for (final FDPMCLoanConfigDTO fdpMCLoanConfigDTO : fdpMCLoanConfigDTOs) {
			context.addRoutes(createRoutesForMCLoan(fdpMCLoanConfigDTO, circleCode));
		}
	}
	
	@Override
	public void startLbRouteForMcLoan(String circleCode) throws Exception{
		final List<FDPMCLoanConfigDTO> fdpMCLoanConfigDTOs = getMCLoanConfigDTOList(circleCode);
		context.addRoutes(this.createLbRouteForMCLoan(fdpMCLoanConfigDTOs, circleCode));
	}

}