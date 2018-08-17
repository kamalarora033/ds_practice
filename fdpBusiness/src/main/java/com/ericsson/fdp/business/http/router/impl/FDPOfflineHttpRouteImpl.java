package com.ericsson.fdp.business.http.router.impl;

import java.util.List;

import javax.annotation.Resource;
import javax.inject.Inject;

import org.apache.camel.ExchangePattern;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.cdi.CdiCamelContext;
import org.apache.camel.model.LoadBalanceDefinition;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ericsson.fdp.business.constants.BusinessConstants;
import com.ericsson.fdp.business.http.router.FDPOfflineHttpRoute;
import com.ericsson.fdp.business.route.controller.ChoiceDefinitionStorage;
import com.ericsson.fdp.business.route.controller.LoadBalancerStorage;
import com.ericsson.fdp.business.route.processor.HttpHeaderProcessor;
import com.ericsson.fdp.business.route.processor.LogOutgoingIpProcessor;
import com.ericsson.fdp.business.util.HttpUtil;
import com.ericsson.fdp.common.enums.BusinessModuleType;
import com.ericsson.fdp.common.enums.ExternalSystem;
import com.ericsson.fdp.core.dsm.framework.service.FDPCache;
import com.ericsson.fdp.core.exception.ExecutionFailedException;
import com.ericsson.fdp.core.serviceprovisioning.bean.FDPAppBag;
import com.ericsson.fdp.core.utils.ApplicationConfigUtil;
import com.ericsson.fdp.dao.dto.FDPOfflineConfigDTO;
import com.ericsson.fdp.dao.entity.ExternalSystemType;
import com.ericsson.fdp.dao.fdpadmin.FDPCircleDAO;
import com.ericsson.fdp.dao.fdpadmin.FDPOfflineConfigDAO;
import com.ericsson.fdp.route.processors.LogRetryProcessor;

/**
 * The Class FDPOfflineHttpRoute used to create FDP Offline routes over the Http Protocol and uses
 * camel http component.
 * 
 * @author ericsson
 */

public class FDPOfflineHttpRouteImpl implements FDPOfflineHttpRoute {

	/**
	 * 
	 */
	private static final long serialVersionUID = 731412407465705066L;

	/** The context. */
	private CdiCamelContext context;

	/** The fdp circle dao. */
	@Inject
	private FDPCircleDAO fdpCircleDAO;
	
	/** The fdp offline config dao. */
	@Inject
	private FDPOfflineConfigDAO fdpOfflineConfigDAO;
	
	
	/** The Constant LOGGER. */
	private static final Logger LOGGER = LoggerFactory.getLogger(FDPOfflineHttpRoute.class);

	/** The logOutgoingIpProcessor is used to log out ip addresses for exchange */
	@Inject
	private LogOutgoingIpProcessor logOutgoingIpProcessor;

	/**
	 * The headerProcessor is used to process the headers at run time mapped by
	 * ip and port.
	 */
	@Inject
	private HttpHeaderProcessor headerProcessor;

	@Inject
	private LogRetryProcessor logRetryProcessor;

	/** The request cache. */
	@Resource(lookup = "java:app/fdpCoreServices-1.0/ApplicationConfigCache")
	private FDPCache<FDPAppBag, Object> applicationConfigCache;

	@Resource(lookup = "java:app/fdpBusiness-1.0/LoadBalancerStorage")
	private LoadBalancerStorage loadBalancerStorage;

	@Resource(lookup = "java:app/fdpBusiness-1.0/ChoiceDefinitionStorage")
	private ChoiceDefinitionStorage choiceDefinitionStorage;
	
	/**
	 * Creates the routes.
	 */
	public void createRoutes() throws Exception {
		context = ApplicationConfigUtil.getCdiCamelContextProvider().getContext();
		final List<String> circleCodes = fdpCircleDAO.getAllCircleCodes();
		for (final String circleCode : circleCodes) {
			final List<FDPOfflineConfigDTO> fdpOfflineConfigDTOs = getFDPOfflineByCircleCode(circleCode);
			for (final FDPOfflineConfigDTO fdpOfflineConfigDTO : fdpOfflineConfigDTOs) {
				context.addRoutes(createFDPOfflineHttpRoutes(fdpOfflineConfigDTO, circleCode));
			}
			context.addRoutes(createLbRouteForOffline(fdpOfflineConfigDTOs, circleCode));
		}
	}
	
	@Override
	public RouteBuilder createFDPOfflineHttpRoutes(final FDPOfflineConfigDTO fdpOfflineConfigDTO, final String circleCode)
			throws ExecutionFailedException {
		return new RouteBuilder() {
			@SuppressWarnings("unchecked")
			@Override
			public void configure() throws Exception {
				
				if(!fdpOfflineConfigDTO.getIsActive()){
					return;
				}
				
				final String logicalName = fdpOfflineConfigDTO.getLogicalName();
				final String fromEndpoint = HttpUtil.getEndpointForOffline(circleCode, ExternalSystemType.FDPOFFLINE_TYPE,
						logicalName, fdpOfflineConfigDTO.getExternalSystemId());
				LOGGER.debug("FromEndpoint:" + fromEndpoint);
				final String toEndpoint = HttpUtil.getEndpointForOffline(fdpOfflineConfigDTO);
				LOGGER.debug("ToEndpoint:" + toEndpoint);
				final String routeId = HttpUtil.getRouteIdForOfflineHttpRoutes(circleCode,
						ExternalSystemType.FDPOFFLINE_TYPE, logicalName, fdpOfflineConfigDTO.getExternalSystemId());
				final String ipAddress = fdpOfflineConfigDTO.getIpAddress().getValue();
				final String circleCodeIpaddressPort = circleCode + BusinessConstants.COLON + ipAddress
						+ BusinessConstants.COLON + String.valueOf(fdpOfflineConfigDTO.getPort());
				final int retryInterval = fdpOfflineConfigDTO.getRetryInterval();
				final int numberOfRetry = fdpOfflineConfigDTO.getNumberOfRetry();
				from(fromEndpoint)
						.routeId(routeId)
						.setProperty(BusinessConstants.OUTGOING_IP_ADDRESS, constant(ipAddress))
						.setProperty(BusinessConstants.OUTGOING_CIRCLE_CODE_IP_PORT, constant(circleCodeIpaddressPort))
						.setProperty(BusinessConstants.LOGICAL_NAME, constant(fdpOfflineConfigDTO.getLogicalName()))
						.setProperty(BusinessConstants.MODULE_NAME, constant(BusinessModuleType.FDPOFFLINE_SOUTH.name()))
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
	public RouteBuilder createLbRouteForOffline(final List<FDPOfflineConfigDTO> offlineDTOList, final String circleCode){
		return new RouteBuilder(){

			@Override
			public void configure() throws Exception {
				//LoadBalanceDefinition loadBalanceDefinition = null;
				String fromEndpoint = BusinessConstants.HTTP_COMPONENT_FDP_OFFLINE_ENDPOINT + BusinessConstants.UNDERSCORE + circleCode;
				LOGGER.debug("from endpoint: "+fromEndpoint);
				if (offlineDTOList != null && offlineDTOList.size() > 1) {

					 LoadBalanceDefinition loadBalanceDefinition = from(fromEndpoint)
							.routeId(ExternalSystem.FDPOFFLINE.name() + BusinessConstants.UNDERSCORE + circleCode)
							.setExchangePattern(ExchangePattern.InOut)
							.loadBalance()
							.failover(offlineDTOList.size() - 1, false, true, java.lang.Exception.class);
					
					for (final FDPOfflineConfigDTO offlineDto : offlineDTOList) {
						final String logicalName = offlineDto.getLogicalName();
						final String toEndpoint = HttpUtil.getEndpointForOffline(circleCode, ExternalSystemType.FDPOFFLINE_TYPE,
								logicalName, offlineDto.getExternalSystemId());
						LOGGER.debug("ToEndpoint:" + toEndpoint);
						loadBalanceDefinition.routeId(toEndpoint).to(toEndpoint);
					}
				}
				else if(offlineDTOList != null && offlineDTOList.size() == 1){
					final String logicalName = offlineDTOList.get(0).getLogicalName();
					final String toEndpoint = HttpUtil.getEndpointForOffline(circleCode, ExternalSystemType.FDPOFFLINE_TYPE,
							logicalName, offlineDTOList.get(0).getExternalSystemId());
					from(BusinessConstants.HTTP_COMPONENT_FDP_OFFLINE_ENDPOINT + BusinessConstants.UNDERSCORE + circleCode)
					.setExchangePattern(ExchangePattern.InOut)
					.routeId(ExternalSystem.FDPOFFLINE.name() + BusinessConstants.UNDERSCORE + circleCode)
					.to(toEndpoint);
				}
			}
			
		};
	}
	
	/**
	 * Gets the endpoint ur ls for FDP Offline by circle code.
	 * 
	 * @param circleCode
	 *            the circle code
	 * @return the endpoint ur ls for FDP Offline by circle code
	 */
	private List<FDPOfflineConfigDTO> getFDPOfflineByCircleCode(final String circleCode) {
		return fdpOfflineConfigDAO.getFDPOfflineEndpointByCircleCode(circleCode);
	}

	@Override
	public void stopRouteforOffline(FDPOfflineConfigDTO fdpOfflineConfigDTO)
			throws Exception {
		context = ApplicationConfigUtil.getCdiCamelContextProvider().getContext();
		final String routeId = HttpUtil.getRouteIdForOfflineHttpRoutes(fdpOfflineConfigDTO.getCircleDTO().getCircleCode(),
				ExternalSystemType.FDPOFFLINE_TYPE, fdpOfflineConfigDTO.getLogicalName(), fdpOfflineConfigDTO.getExternalSystemId());
		context.stopRoute(routeId);
		context.removeRoute(routeId);
		LOGGER.debug("Stopped Route: "+routeId);
	}

	@Override
	public void stopLbRouteForOffline(String circleCode) throws Exception {
		context = ApplicationConfigUtil.getCdiCamelContextProvider().getContext();
		final String routeId = ExternalSystem.FDPOFFLINE.name() + BusinessConstants.UNDERSCORE + circleCode;
		context.stopRoute(routeId);
		context.removeRoute(routeId);
		LOGGER.debug("Stopped LB Route: "+routeId);
	}

	@Override
	public void stopAllRoutesForOffline(String circleCode) throws Exception {
		final List<FDPOfflineConfigDTO> fdpOfflineConfigDTOs = getFDPOfflineByCircleCode(circleCode);
		context = ApplicationConfigUtil.getCdiCamelContextProvider().getContext();
		for(FDPOfflineConfigDTO offlineDto : fdpOfflineConfigDTOs){
			final String routeId = HttpUtil.getRouteIdForOfflineHttpRoutes(circleCode,
					ExternalSystemType.FDPOFFLINE_TYPE, offlineDto.getLogicalName(), offlineDto.getExternalSystemId());
			context.stopRoute(routeId);
			context.removeRoute(routeId);
			LOGGER.debug("Stopped Route: "+routeId);
		}
		
		LOGGER.debug("Stopped All Routes for circle: "+circleCode);
		
	}

	@Override
	public void startAllRoutesForOffline(String circleCode) throws Exception {
		final List<FDPOfflineConfigDTO> fdpOfflineConfigDTOs = getFDPOfflineByCircleCode(circleCode);
		//context.addRoutes(createLbRouteForOffline(fdpOfflineConfigDTOs, circleCode));
		for (final FDPOfflineConfigDTO fdpOfflineConfigDTO : fdpOfflineConfigDTOs) {
			context.addRoutes(createFDPOfflineHttpRoutes(fdpOfflineConfigDTO, circleCode));
		}
	}

	@Override
	public void startLbRouteForOffline(String circleCode) throws Exception {
		final List<FDPOfflineConfigDTO> fdpOfflineConfigDTOs = getFDPOfflineByCircleCode(circleCode);
		context.addRoutes(createLbRouteForOffline(fdpOfflineConfigDTOs, circleCode));
		//context.addRoutes(this.createLbRouteForOffline(fdpOfflineConfigDTOs, circleCode));
	}
	
	
}
