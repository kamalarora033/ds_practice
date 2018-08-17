package com.ericsson.fdp.business.route.controller;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import javax.annotation.Resource;
import javax.ejb.DependsOn;
import javax.ejb.Stateless;
import javax.inject.Inject;

import org.apache.camel.Endpoint;
import org.apache.camel.Exchange;
import org.apache.camel.ExchangePattern;
import org.apache.camel.Message;
import org.apache.camel.Producer;
import org.apache.camel.RoutesBuilder;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.cdi.CdiCamelContext;
import org.apache.camel.component.direct.DirectEndpoint;
import org.apache.camel.component.http.HttpOperationFailedException;
import org.apache.camel.model.LoadBalanceDefinition;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ericsson.fdp.business.constants.BusinessConstants;
import com.ericsson.fdp.business.http.router.DMCHttpRoutes;
import com.ericsson.fdp.business.http.router.EVDSHttpRoute;
import com.ericsson.fdp.business.http.router.EVDSRouteHttp;
import com.ericsson.fdp.business.http.router.EvdsTelnetRoute;
import com.ericsson.fdp.business.http.router.FDPMobileMoneyHttpsRoute;
import com.ericsson.fdp.business.http.router.FDPMobileMoneyRoute;
import com.ericsson.fdp.business.http.router.LoyaltyHttpRoute;
import com.ericsson.fdp.business.http.router.impl.FDPOfflineHttpRouteImpl;
import com.ericsson.fdp.business.http.router.impl.MCLoanHttpRoutesImpl;
import com.ericsson.fdp.business.http.router.impl.SBBBHttpRouteImpl;
import com.ericsson.fdp.business.route.processor.AbilitySyncRequestProcessor;
import com.ericsson.fdp.business.route.processor.HttpHeaderProcessor;
import com.ericsson.fdp.business.route.processor.LogOutgoingIpProcessor;
import com.ericsson.fdp.business.route.processor.RequestProcessor;
import com.ericsson.fdp.business.smsc.throtller.ThrotllerWaterGate;
import com.ericsson.fdp.business.telnet.ema.TelnetClientManager;
import com.ericsson.fdp.business.telnet.ema.TelnetProcessor;
import com.ericsson.fdp.business.util.HttpUtil;
import com.ericsson.fdp.common.constants.FDPConstant;
import com.ericsson.fdp.common.dto.TrapError;
import com.ericsson.fdp.common.enums.BusinessModuleType;
import com.ericsson.fdp.common.enums.EmaProtocolType;
import com.ericsson.fdp.common.enums.ExternalSystem;
import com.ericsson.fdp.common.enums.ModuleType;
import com.ericsson.fdp.common.enums.trap.TrapCISSystems;
import com.ericsson.fdp.common.enums.trap.TrapComponent;
import com.ericsson.fdp.common.enums.trap.TrapErrorCodes;
import com.ericsson.fdp.common.enums.trap.TrapSeverity;
import com.ericsson.fdp.common.logging.Event;
import com.ericsson.fdp.common.util.RouteUtil;
import com.ericsson.fdp.common.vo.FDPCircle;
import com.ericsson.fdp.core.cache.AppCacheSubStore;
import com.ericsson.fdp.core.cache.service.AppCacheInitializerService;
import com.ericsson.fdp.core.cache.service.SMPPServerMappingService;
import com.ericsson.fdp.core.config.utils.PropertyUtils;
import com.ericsson.fdp.core.dsm.framework.service.FDPCache;
import com.ericsson.fdp.core.exception.ExecutionFailedException;
import com.ericsson.fdp.core.logging.FDPLogger;
import com.ericsson.fdp.core.logging.FDPLoggerFactory;
import com.ericsson.fdp.core.serviceprovisioning.bean.FDPAppBag;
import com.ericsson.fdp.core.utils.ApplicationUtil;
import com.ericsson.fdp.core.utils.SNMPUtil;
import com.ericsson.fdp.dao.dto.FDPADCConfigDTO;
import com.ericsson.fdp.dao.dto.FDPAIRConfigDTO;
import com.ericsson.fdp.dao.dto.FDPAbilityConfigDTO;
import com.ericsson.fdp.dao.dto.FDPCGWConfigDTO;
import com.ericsson.fdp.dao.dto.FDPCMSConfigDTO;
import com.ericsson.fdp.dao.dto.FDPDMCConfigDTO;
import com.ericsson.fdp.dao.dto.FDPEMAConfigDTO;
import com.ericsson.fdp.dao.dto.FDPEVDSConfigDTO;
import com.ericsson.fdp.dao.dto.FDPExternalSystemDTO;
import com.ericsson.fdp.dao.dto.FDPLoyaltyConfigDTO;
import com.ericsson.fdp.dao.dto.FDPMCLoanConfigDTO;
import com.ericsson.fdp.dao.dto.FDPMobileMoneyConfigDTO;
import com.ericsson.fdp.dao.dto.FDPNodeExternalSystemMappingDTO;
import com.ericsson.fdp.dao.dto.FDPOfflineConfigDTO;
import com.ericsson.fdp.dao.dto.FDPRSConfigDTO;
import com.ericsson.fdp.dao.dto.FDPSMSCConfigDTO;
import com.ericsson.fdp.dao.dto.FDPVASConfigDTO;
import com.ericsson.fdp.dao.dto.SMPPServerMappingDTO;
import com.ericsson.fdp.dao.dto.UpdateCacheDTO;
import com.ericsson.fdp.dao.entity.ExternalSystemType;
import com.ericsson.fdp.dao.fdpadmin.FDPADCConfigDAO;
import com.ericsson.fdp.dao.fdpadmin.FDPAIRConfigDAO;
import com.ericsson.fdp.dao.fdpadmin.FDPAbilityConfigDAO;
import com.ericsson.fdp.dao.fdpadmin.FDPCGWConfigDAO;
import com.ericsson.fdp.dao.fdpadmin.FDPCMSConfigDAO;
import com.ericsson.fdp.dao.fdpadmin.FDPDMCConfigDAO;
import com.ericsson.fdp.dao.fdpadmin.FDPEMAConfigDAO;
import com.ericsson.fdp.dao.fdpadmin.FDPRSConfigDAO;
import com.ericsson.fdp.route.cdi.context.provider.CdiCamelContextProvider;
import com.ericsson.fdp.route.constant.RoutingConstant;
import com.ericsson.fdp.route.controller.FDPRouteControlManager;
import com.ericsson.fdp.route.processors.CircleFinder;
import com.ericsson.fdp.route.processors.IncomingRequestIPProcessor;
import com.ericsson.fdp.route.processors.LogRetryProcessor;
import com.ericsson.fdp.route.processors.ReceiptProcessor;
import com.ericsson.fdp.route.processors.RequestIdGenerator;
import com.ericsson.fdp.route.processors.ResponseProcessor;
import com.ericsson.fdp.route.processors.RouteCompletionProcessor;

/**
 * The Class FDPRouteControlManagerImpl is used for starting , stopping ,
 * removing and updating camel routes.
 * 
 * @author Ericsson
 */
@Stateless
@DependsOn(value = "FDPCacheBuilder")
public class FDPRouteControlManagerImpl implements FDPRouteControlManager {

	/** The cdi camel context provider. */
	@Inject
	private CdiCamelContextProvider cdiCamelContextProvider;

	/** The fdpair config dao. */
	@Inject
	private FDPAIRConfigDAO fdpairConfigDAO;
	
	
	@Inject
	private FDPDMCConfigDAO fdpdmcConfigDAO;
	
	/** The fdpcms config dao. */
	@Inject
	private FDPCMSConfigDAO fdpcmsConfigDAO;

	/** The fdp cgw config dao. */
	@Inject
	private FDPCGWConfigDAO fdpCGWConfigDAO;

	/** The log retry processor. */
	@Inject
	private LogRetryProcessor logRetryProcessor;
	
    @Inject
    private RouteCompletionProcessor completionProcessor;

    @Inject
    AbilitySyncRequestProcessor abilitySyncRequestProcessor;

	/** The log outgoing ip processor. */
	@Inject
	private LogOutgoingIpProcessor logOutgoingIpProcessor;

	/** The header processor. */
	@Inject
	private HttpHeaderProcessor headerProcessor;

	/** The fdp rs config dao. */
	@Inject
	private FDPRSConfigDAO fdpRsConfigDAO;
	
	@Inject
	private FDPADCConfigDAO fdpADCConfigDAO;

	/** The incoming request ip processor. */
	@Inject
	private IncomingRequestIPProcessor incomingRequestIPProcessor;

	/** The response processor. */
	@Inject
	private ResponseProcessor responseProcessor;

	/** The receipt processor. */
	@Inject
	private ReceiptProcessor receiptProcessor;

    @Inject
    private RequestIdGenerator requestIdGenerator;
    
    /** The circle finder. */
    @Inject
    private CircleFinder circleFinder;
    
    /** The in message processor. */
    @Inject
    private RequestProcessor requestProcessor;

	/** The fdpsmsc config dao. */
	@Resource(lookup = "java:app/fdpCoreServices-1.0/SMPPServerMappingServiceImpl")
	private SMPPServerMappingService serverMappingService;

	/** The request cache. */
	@Resource(lookup = "java:app/fdpCoreServices-1.0/ApplicationConfigCache")
	private FDPCache<FDPAppBag, Object> applicationConfigCache;

	/** The request cache. */
	@Resource(lookup = "java:app/fdpCoreServices-1.0/AppCacheInitializerServiceImpl")
	private AppCacheInitializerService appCacheInitializerService;

	/** The load balancer storage. */
	@Resource(lookup = "java:app/fdpBusiness-1.0/LoadBalancerStorage")
	private LoadBalancerStorage loadBalancerStorage;

	/** The fdp ema config dao. */
	@Inject
	private FDPEMAConfigDAO fdpEmaConfigDAO;

	@Inject 
	private MCLoanHttpRoutesImpl mcLoanhttpRoute;
	
	@Inject 
	private FDPOfflineHttpRouteImpl fdpOfflinehttpRoute;
	
	@Inject
	private FDPMobileMoneyRoute fdpMobileMoneyhttpRoute;
	
	@Inject
	private FDPMobileMoneyHttpsRoute fdpMobileMoneyhttpsRoute;
	@Inject
	private EvdsTelnetRoute fdpEvdsHttpRoute;
	
	@Inject
	private EVDSHttpRoute evdsHttpRoute;
	
	@Inject
	private EVDSRouteHttp evdsRouteHttp;
	
	@Inject
	private DMCHttpRoutes fdpDMCHttpRoute;
	
	@Inject
	private LoyaltyHttpRoute loyalityhttpRoute;
	
	@Inject
    private FDPAbilityConfigDAO fdpAbilityConfigDAO;
	
	
	@Inject
    private ThrotllerWaterGate routePolicyMainRoute, routePolicyCircleRoute;
	
	/** The Constant LOGGER. */
	private static final String evdsType = PropertyUtils
			.getProperty("evds.protocol.type");
	private static final Logger LOGGER = LoggerFactory.getLogger(FDPRouteControlManagerImpl.class);
	
	private static final String mmProtocolType = PropertyUtils

	        .getProperty("ecw.protocol.type");
	@Override
	public boolean addRoutes(final FDPExternalSystemDTO fdpExternalSystemDTO) throws Exception {
		boolean status = false;
		final CdiCamelContext cdiCamelContext = cdiCamelContextProvider.getContext();
		final UpdateCacheDTO updateCacheDTO = new UpdateCacheDTO();
		final FDPCircle circle = new FDPCircle();
		circle.setCircleCode(fdpExternalSystemDTO.getCircleDTO() == null ? null : fdpExternalSystemDTO.getCircleDTO().getCircleCode());
		updateCacheDTO.setCircle(circle);
		if (fdpExternalSystemDTO instanceof FDPCMSConfigDTO){
			final FDPCMSConfigDTO fdpCmsConfigDTO = (FDPCMSConfigDTO) fdpExternalSystemDTO;
			updateCacheDTO.setModuleType(ModuleType.CMS_CONFEGURATION);
			appCacheInitializerService.updateCache(updateCacheDTO);
			cdiCamelContext.addRoutes(createCmsRoutes(fdpCmsConfigDTO));
			status = true;
		} else if (fdpExternalSystemDTO instanceof FDPAIRConfigDTO) {
			final FDPAIRConfigDTO fdpAirConfigDTO = (FDPAIRConfigDTO) fdpExternalSystemDTO;
			updateCacheDTO.setModuleType(ModuleType.AIR_CONFEGURATION);
			appCacheInitializerService.updateCache(updateCacheDTO);
			cdiCamelContext.addRoutes(createAirRoutes(fdpAirConfigDTO));
			status = true;
		} else if (fdpExternalSystemDTO instanceof FDPCGWConfigDTO) {
			final FDPCGWConfigDTO fdpCgwConfigDTO = (FDPCGWConfigDTO) fdpExternalSystemDTO;
			updateCacheDTO.setModuleType(ModuleType.CGW_CONFEGURATION);
			appCacheInitializerService.updateCache(updateCacheDTO);
			cdiCamelContext.addRoutes(createCGWRoutes(fdpCgwConfigDTO));
			status = true;
		} else if (fdpExternalSystemDTO instanceof FDPRSConfigDTO) {
			final FDPRSConfigDTO fdpRSConfigDTO = (FDPRSConfigDTO) fdpExternalSystemDTO;
			updateCacheDTO.setModuleType(ModuleType.RS_CONFEGURATION);
			appCacheInitializerService.updateCache(updateCacheDTO);
			cdiCamelContext.addRoutes(createRSRoutes(fdpRSConfigDTO));
			status = true;
		} else if (fdpExternalSystemDTO instanceof FDPSMSCConfigDTO) {
			final String serverName = ApplicationUtil.getServerName();
			final FDPSMSCConfigDTO fdpsmscConfigDTO = (FDPSMSCConfigDTO) fdpExternalSystemDTO;
			final List<FDPNodeExternalSystemMappingDTO> externalSystemDTOs = fdpsmscConfigDTO.getSystemMappings();
			LOGGER.debug("Trying to update the system mapping for node {}", serverName);
			if (externalSystemDTOs != null) {
				for (final FDPNodeExternalSystemMappingDTO fdpNodeExternalSystemMappingDTO : externalSystemDTOs) {
					if (fdpNodeExternalSystemMappingDTO.getServerNodeId() != null
							&& serverName.equalsIgnoreCase(fdpNodeExternalSystemMappingDTO.getServerNodeId()
									.getNodeName())) {
						setUserNamePasswordForSMSC(fdpsmscConfigDTO);
						if (BusinessConstants.SERVICE_TYPE_SMS.equals(fdpsmscConfigDTO.getServiceType())) {
							updateCacheDTO.setModuleType(ModuleType.SMS_CONFEGURATION);
						}
						String smscBindMode = PropertyUtils.getProperty("smsc.bind.mode");
						smscBindMode = (smscBindMode != null && !"".equals(smscBindMode)) ? smscBindMode
								: BusinessConstants.BIND_MODE_TXRX;

						if ("receiver".equals(fdpsmscConfigDTO.getBindNode())) {
						    try{
						        cdiCamelContext.addRoutes(createReceiverRoutes(fdpsmscConfigDTO));
                            } catch (Exception e) {
                                LOGGER.error("Error occured in creating SMSC route : ", e);
                                FDPLoggerFactory.getGenerateAlarmLogger().warn(
                                        FDPConstant.EMPTY_STRING,
                                        new Event(TrapSeverity.INFORMATIONAL, new TrapError((TrapErrorCodes.UNABLE_TO_CREATE_ROUTES), Arrays.asList(
                                                fdpsmscConfigDTO.getIp(), fdpsmscConfigDTO.getBindNode()),
                                                TrapCISSystems.SMSC, TrapComponent.EXTERNAL_SYSTEM), SNMPUtil.getIPAddess()));
                            }

                        } else if ("transmitter".equals(fdpsmscConfigDTO.getBindNode())) {
                            try {
                                cdiCamelContext.addRoutes(createTransmitterRoutes(fdpsmscConfigDTO));
                            } catch (Exception e) {
                                LOGGER.error("Error occured in creating SMSC route : ", e);
                                FDPLoggerFactory.getGenerateAlarmLogger().warn(
                                        FDPConstant.EMPTY_STRING,
                                        new Event(TrapSeverity.INFORMATIONAL, new TrapError((TrapErrorCodes.UNABLE_TO_CREATE_ROUTES), Arrays
                                                .asList(fdpsmscConfigDTO.getIp(), fdpsmscConfigDTO.getBindNode()), TrapCISSystems.SMSC,
                                                TrapComponent.EXTERNAL_SYSTEM), SNMPUtil.getIPAddess()));
                            }
                        }
						appCacheInitializerService.updateCache(updateCacheDTO);
						LOGGER.debug("Update the system mapping for node {}");
						break;
					}
				}
			}
			status = true;
		} else if (fdpExternalSystemDTO instanceof FDPEMAConfigDTO) {
			final FDPEMAConfigDTO fdpemaConfigDTO = (FDPEMAConfigDTO) fdpExternalSystemDTO;
			updateCacheDTO.setModuleType(ModuleType.EMA_DETAILS);
			appCacheInitializerService.updateCache(updateCacheDTO);
			cdiCamelContext.addRoutes(createEmaCaiRoutes(fdpemaConfigDTO, "add"));
			status = true;
		} else if (fdpExternalSystemDTO instanceof FDPVASConfigDTO) {
			final FDPVASConfigDTO fdpVASConfigDTO = (FDPVASConfigDTO) fdpExternalSystemDTO;
			if (fdpVASConfigDTO.getVasType().equalsIgnoreCase(ExternalSystem.MCARBON.name())) {
				updateCacheDTO.setModuleType(ModuleType.MCARBON_STORE);
				appCacheInitializerService.updateCache(updateCacheDTO);
				cdiCamelContext.addRoutes(createRoutesForMCarbon(fdpVASConfigDTO, fdpVASConfigDTO.getCircleDTO()
						.getCircleCode()));
				LOGGER.info("MCarbon Routes has been added and started to Camel Context : {}", fdpVASConfigDTO);
			}
			if (fdpVASConfigDTO.getVasType().equalsIgnoreCase(ExternalSystem.MANHATTAN.name())) {
				updateCacheDTO.setModuleType(ModuleType.MANHATTAN_STORE);
				appCacheInitializerService.updateCache(updateCacheDTO);
				cdiCamelContext.addRoutes(createRoutesForManhattan(fdpVASConfigDTO, fdpVASConfigDTO.getCircleDTO()
						.getCircleCode()));
				LOGGER.info("Manhattan Routes has been added and started to Camel Context : {}", fdpVASConfigDTO);
			}
			if (fdpVASConfigDTO.getVasType().equalsIgnoreCase(ExternalSystem.SBBB.name())) {
				updateCacheDTO.setModuleType(ModuleType.SBB_STORE);
				appCacheInitializerService.updateCache(updateCacheDTO);
				cdiCamelContext.addRoutes(createRoutesForSBBB(fdpVASConfigDTO, fdpVASConfigDTO.getCircleDTO().getCircleCode()));
				LOGGER.info("SBBB Routes has been added and started to Camel Context : {}", fdpVASConfigDTO);
			}
		} else if(fdpExternalSystemDTO instanceof FDPMCLoanConfigDTO) {
			final FDPMCLoanConfigDTO fdpMcLoanDto = (FDPMCLoanConfigDTO) fdpExternalSystemDTO;
			final String circleCode = fdpMcLoanDto.getCircleDTO().getCircleCode();
			updateCacheDTO.setModuleType(ModuleType.MCLOAN_STORE);
			appCacheInitializerService.updateCache(updateCacheDTO);
			this.mcLoanhttpRoute.stopLbRouteForMcLoan(circleCode);
			this.mcLoanhttpRoute.stopAllRoutesForMcLoan(circleCode);
			this.mcLoanhttpRoute.startAllRoutesForMcLoan(circleCode);
			this.mcLoanhttpRoute.startLbRouteForMcLoan(circleCode);
			status = true;
		} else if(fdpExternalSystemDTO instanceof FDPOfflineConfigDTO) {
			final FDPOfflineConfigDTO fdpOfflineDto = (FDPOfflineConfigDTO) fdpExternalSystemDTO;
			final String circleCode = fdpOfflineDto.getCircleDTO().getCircleCode();
			updateCacheDTO.setModuleType(ModuleType.FDPOFFLINE_STORE);
			appCacheInitializerService.updateCache(updateCacheDTO);
			this.fdpOfflinehttpRoute.stopLbRouteForOffline(circleCode);
			this.fdpOfflinehttpRoute.stopAllRoutesForOffline(circleCode);
			this.fdpOfflinehttpRoute.startAllRoutesForOffline(circleCode);
			this.fdpOfflinehttpRoute.startLbRouteForOffline(circleCode);
			status = true;
		}else if(fdpExternalSystemDTO instanceof FDPMobileMoneyConfigDTO){
			final FDPMobileMoneyConfigDTO fdpOfflineDto = (FDPMobileMoneyConfigDTO) fdpExternalSystemDTO;
			final String circleCode = fdpOfflineDto.getCircleDTO().getCircleCode();
			updateCacheDTO.setModuleType(ModuleType.MOBILEMONEY_CONFEGURATION);
			appCacheInitializerService.updateCache(updateCacheDTO);
			createMobileMoneyRoutes(fdpOfflineDto);
			status = true;
		}else if(fdpExternalSystemDTO instanceof FDPEVDSConfigDTO){
			final FDPEVDSConfigDTO fdpEvdsDTO = (FDPEVDSConfigDTO)fdpExternalSystemDTO;
			updateCacheDTO.setModuleType(ModuleType.EVDS_CONFIGURATION);
			appCacheInitializerService.updateCache(updateCacheDTO);
			restartEvdsRoutes(fdpEvdsDTO);
			status = true;
		} else if (fdpExternalSystemDTO instanceof FDPDMCConfigDTO) {
			final FDPDMCConfigDTO fdpDMCConfigDTO = (FDPDMCConfigDTO) fdpExternalSystemDTO;
			updateCacheDTO.setModuleType(ModuleType.DMC_CONFEGURATION);
			appCacheInitializerService.updateCache(updateCacheDTO);
			cdiCamelContext.addRoutes(createDmcRoutes(fdpDMCConfigDTO));
			status = true;
		}
		else if (fdpExternalSystemDTO instanceof FDPAbilityConfigDTO) {
            final FDPAbilityConfigDTO fdpAbilityConfigDTO = (FDPAbilityConfigDTO) fdpExternalSystemDTO;
            updateCacheDTO.setModuleType(ModuleType.ABILITY_CONFIGURATION);
            appCacheInitializerService.updateCache(updateCacheDTO);
            cdiCamelContext.addRoutes(createAbilityRoutes(fdpAbilityConfigDTO));
            status = true;
        }else if (fdpExternalSystemDTO instanceof FDPADCConfigDTO) {
			final FDPADCConfigDTO fdpADCConfigDTO = (FDPADCConfigDTO) fdpExternalSystemDTO;
			updateCacheDTO.setModuleType(ModuleType.ADC_CONFEGURATION);
			appCacheInitializerService.updateCache(updateCacheDTO);
			cdiCamelContext.addRoutes(createADCRoutes(fdpADCConfigDTO));
			status = true;
		}
		else {
			throw new IllegalArgumentException("Invalid FDPExternal System.");
		}
		return status;
	}

	/**
	 * Creates the ema cai routes.
	 * 
	 * @param fdpemaConfigDTO
	 *            the fdpema config dto
	 * @param operation
	 *            the operation
	 * @return the routes builder
	 */
	private RoutesBuilder createEmaCaiRoutes(final FDPEMAConfigDTO fdpemaConfigDTO, final String operation) {
		return new RouteBuilder() {
			@Override
			public void configure() throws Exception {
				final String circleCode = fdpemaConfigDTO.getCircleDTO().getCircleCode();
				final String fromEndpoint = getDirectEndpointForCircle(fdpemaConfigDTO, circleCode);
				LOGGER.debug("From Endpoint :" + fromEndpoint);
				final String toEndpoint = getEMAEndpointURLForCircle(fdpemaConfigDTO);
				LOGGER.debug("To Endpoint :" + toEndpoint);
				final String routeId = compiledRouteId(fdpemaConfigDTO, circleCode);
				final String ipAddress = fdpemaConfigDTO.getIpAddress().getValue();
				final String circleCodeIpaddressPort = circleCode + BusinessConstants.COLON + ipAddress
						+ BusinessConstants.COLON + String.valueOf(fdpemaConfigDTO.getPort());
				final boolean autostartup = fdpemaConfigDTO.getIsActive();
				TelnetProcessor telnetProcessor = new TelnetProcessor(fdpemaConfigDTO);
				final int retryInterval = fdpemaConfigDTO.getRetryInterval();
				final int numberOfRetry = fdpemaConfigDTO.getNumberOfRetries();
				if (EmaProtocolType.TELNET.equals(fdpemaConfigDTO.getProtocolType())) {
					from(fromEndpoint)
							.autoStartup(autostartup)
							.delay(1000)
							.routeId(routeId)
.onCompletion().onCompleteOnly()
                            .process(completionProcessor).end()
							.setProperty(BusinessConstants.OUTGOING_IP_ADDRESS, constant(ipAddress))
							.setProperty(BusinessConstants.OUTGOING_CIRCLE_CODE_IP_PORT,
									constant(circleCodeIpaddressPort))
							.setProperty(BusinessConstants.LOGICAL_NAME, constant(fdpemaConfigDTO.getLogicalName()))
							.setProperty(BusinessConstants.MODULE_NAME, constant(BusinessModuleType.EMA_SOUTH.name()))
							.process(logOutgoingIpProcessor).onException(java.lang.Exception.class).handled(false)
							.redeliveryDelay(retryInterval).maximumRedeliveries(numberOfRetry)
							.onRedelivery(logRetryProcessor)
							.to(BusinessConstants.COMPONENT_EXCEPTION_ENDPOINT).end()
							.to(toEndpoint);
				} else {
					from(fromEndpoint)
							.autoStartup(autostartup)
							.routeId(routeId)
							.setProperty(BusinessConstants.OUTGOING_IP_ADDRESS, constant(ipAddress))
							.setProperty(BusinessConstants.OUTGOING_CIRCLE_CODE_IP_PORT,
									constant(circleCodeIpaddressPort))
							.setProperty(BusinessConstants.LOGICAL_NAME, constant(fdpemaConfigDTO.getLogicalName()))
							.setProperty(BusinessConstants.MODULE_NAME, constant(BusinessModuleType.EMA_SOUTH.name()))
							.process(logOutgoingIpProcessor).onException(java.lang.Exception.class)
							.handled(false)
							.redeliveryDelay(retryInterval).maximumRedeliveries(numberOfRetry)
							.onRedelivery(logRetryProcessor)
							.to(BusinessConstants.COMPONENT_EXCEPTION_ENDPOINT).end()
							.process(telnetProcessor);
				}
			}
		};
	}

	@Override
	public boolean editRoutes(final FDPExternalSystemDTO oldFDPExternalSystemDTO,
			final FDPExternalSystemDTO newFDPExternalSystemDTO) throws Exception {
		boolean status = false;
		final CdiCamelContext cdiCamelContext = cdiCamelContextProvider.getContext();
		final UpdateCacheDTO updateCacheDTO = new UpdateCacheDTO();
		final FDPCircle circle = new FDPCircle();
		circle.setCircleCode(newFDPExternalSystemDTO.getCircleDTO() == null ? null : newFDPExternalSystemDTO
				.getCircleDTO().getCircleCode());
		updateCacheDTO.setCircle(circle);
		if (oldFDPExternalSystemDTO instanceof FDPAIRConfigDTO && newFDPExternalSystemDTO instanceof FDPAIRConfigDTO) {

			final FDPAIRConfigDTO newFDPAirConfigDTO = (FDPAIRConfigDTO) newFDPExternalSystemDTO;
			final FDPAIRConfigDTO oldFdpairConfigDTO = (FDPAIRConfigDTO) oldFDPExternalSystemDTO;
			updateCacheDTO.setModuleType(ModuleType.AIR_CONFEGURATION);
			appCacheInitializerService.updateCache(updateCacheDTO);
			cdiCamelContext.addRoutes(createAirRoutes(oldFdpairConfigDTO));

            if (newFDPAirConfigDTO.getIsActive().equals(false)) {
                String routeIdToBeStopped = HttpUtil
                        .getAirSubRouteId(newFDPAirConfigDTO, newFDPAirConfigDTO.getCircleDTO().getCircleCode());
                cdiCamelContext.stopRoute(routeIdToBeStopped);
                FDPLoggerFactory.getGenerateAlarmLogger().error(
                        FDPConstant.EMPTY_STRING,
                        new Event(TrapSeverity.CRITICAL, new TrapError((TrapErrorCodes.SYSTEM_INACTIVE), Arrays.asList(newFDPAirConfigDTO
                                .getIpAddress().getValue(), newFDPAirConfigDTO.getCircleDTO().getCircleCode()), TrapCISSystems.AIR,
                                TrapComponent.EXTERNAL_SYSTEM), SNMPUtil.getIPAddess()));
            }
			if (!oldFDPExternalSystemDTO.getIsActive() && newFDPAirConfigDTO.getIsActive()) {
                FDPLoggerFactory.getGenerateAlarmLogger().warn(
                        FDPConstant.EMPTY_STRING,
                        new Event(TrapSeverity.CLEAR, new TrapError((TrapErrorCodes.SYSTEM_INACTIVE), Arrays.asList(newFDPAirConfigDTO
                                .getIpAddress().getValue(), newFDPAirConfigDTO.getCircleDTO().getCircleCode()), TrapCISSystems.AIR,
                                TrapComponent.EXTERNAL_SYSTEM), SNMPUtil.getIPAddess()));
			}
			status = true;
		}
		else if (oldFDPExternalSystemDTO instanceof FDPCMSConfigDTO && newFDPExternalSystemDTO instanceof FDPCMSConfigDTO) {

			final FDPCMSConfigDTO newFDPCmsConfigDTO = (FDPCMSConfigDTO) newFDPExternalSystemDTO;
			final FDPCMSConfigDTO oldFdpcmsConfigDTO = (FDPCMSConfigDTO) oldFDPExternalSystemDTO;
			updateCacheDTO.setModuleType(ModuleType.CMS_CONFEGURATION);
			appCacheInitializerService.updateCache(updateCacheDTO);
			cdiCamelContext.addRoutes(createCmsRoutes(oldFdpcmsConfigDTO));
			if (newFDPCmsConfigDTO.getIsActive().equals(false)) {
				String routeIdToBeStopped = HttpUtil.getCmsSubRouteId(newFDPCmsConfigDTO, newFDPCmsConfigDTO
						.getCircleDTO().getCircleCode());
				cdiCamelContext.stopRoute(routeIdToBeStopped);
                FDPLoggerFactory.getGenerateAlarmLogger().error(
                        FDPConstant.EMPTY_STRING,
                        new Event(TrapSeverity.CRITICAL, new TrapError((TrapErrorCodes.SYSTEM_INACTIVE), Arrays.asList(
                                newFDPExternalSystemDTO.getIpAddress().getValue(), newFDPExternalSystemDTO.getCircleDTO().getCircleCode()),
                                TrapCISSystems.CMS, TrapComponent.EXTERNAL_SYSTEM), SNMPUtil.getIPAddess()));
			}
			if (!oldFDPExternalSystemDTO.getIsActive() && newFDPCmsConfigDTO.getIsActive()) {
                FDPLoggerFactory.getGenerateAlarmLogger().warn(
                        FDPConstant.EMPTY_STRING,
                        new Event(TrapSeverity.CLEAR, new TrapError((TrapErrorCodes.SYSTEM_INACTIVE), Arrays.asList(newFDPExternalSystemDTO
                                .getIpAddress().getValue(), newFDPExternalSystemDTO.getCircleDTO().getCircleCode()), TrapCISSystems.CMS,
                                TrapComponent.EXTERNAL_SYSTEM), SNMPUtil.getIPAddess()));

			}
			status = true;
		}
		
		
		else if (oldFDPExternalSystemDTO instanceof FDPCGWConfigDTO
				&& newFDPExternalSystemDTO instanceof FDPCGWConfigDTO) {

			final FDPCGWConfigDTO newFDPCgwConfigDTO = (FDPCGWConfigDTO) newFDPExternalSystemDTO;
			final FDPCGWConfigDTO oldFdpcgwConfigDTO = (FDPCGWConfigDTO) oldFDPExternalSystemDTO;
			updateCacheDTO.setModuleType(ModuleType.CGW_CONFEGURATION);
			appCacheInitializerService.updateCache(updateCacheDTO);
			cdiCamelContext.addRoutes(createCGWRoutes(oldFdpcgwConfigDTO));
            if (newFDPCgwConfigDTO.getIsActive().equals(false)) {
                String routeIdToBeStopped = HttpUtil
                        .getCgwSubRouteId(newFDPCgwConfigDTO, newFDPCgwConfigDTO.getCircleDTO().getCircleCode());
                cdiCamelContext.stopRoute(routeIdToBeStopped);
                FDPLoggerFactory.getGenerateAlarmLogger().error(
                        FDPConstant.EMPTY_STRING,
                        new Event(TrapSeverity.CRITICAL, new TrapError((TrapErrorCodes.SYSTEM_INACTIVE), Arrays.asList(newFDPCgwConfigDTO
                                .getIpAddress().getValue(), newFDPCgwConfigDTO.getCircleDTO().getCircleCode()), TrapCISSystems.CGW,
                                TrapComponent.EXTERNAL_SYSTEM), SNMPUtil.getIPAddess()));
            }
            if (!oldFDPExternalSystemDTO.getIsActive() && newFDPCgwConfigDTO.getIsActive()) {
                FDPLoggerFactory.getGenerateAlarmLogger().warn(
                        FDPConstant.EMPTY_STRING,
                        new Event(TrapSeverity.CLEAR, new TrapError((TrapErrorCodes.SYSTEM_INACTIVE), Arrays.asList(newFDPExternalSystemDTO
                                .getIpAddress().getValue(), newFDPExternalSystemDTO.getCircleDTO().getCircleCode()), TrapCISSystems.CGW,
                                TrapComponent.EXTERNAL_SYSTEM), SNMPUtil.getIPAddess()));
            }
			status = true;

		} else if (oldFDPExternalSystemDTO instanceof FDPRSConfigDTO
				&& newFDPExternalSystemDTO instanceof FDPRSConfigDTO) {

			final FDPRSConfigDTO newFDPRSConfigDTO = (FDPRSConfigDTO) newFDPExternalSystemDTO;
			final FDPRSConfigDTO oldFdpRSConfigDTO = (FDPRSConfigDTO) oldFDPExternalSystemDTO;
			updateCacheDTO.setModuleType(ModuleType.RS_CONFEGURATION);
			appCacheInitializerService.updateCache(updateCacheDTO);
			cdiCamelContext.addRoutes(createRSRoutes(oldFdpRSConfigDTO));
			if (newFDPRSConfigDTO.getIsActive().equals(false)) {
				String routeIdToBeStopped = HttpUtil.getRSSubRouteId(newFDPRSConfigDTO, newFDPRSConfigDTO
						.getCircleDTO().getCircleCode());
				cdiCamelContext.stopRoute(routeIdToBeStopped);
                FDPLoggerFactory.getGenerateAlarmLogger().error(
                        FDPConstant.EMPTY_STRING,
                        new Event(TrapSeverity.CRITICAL, new TrapError((TrapErrorCodes.SYSTEM_INACTIVE), Arrays.asList(newFDPRSConfigDTO
                                .getIpAddress().getValue(), newFDPRSConfigDTO.getCircleDTO().getCircleCode()), TrapCISSystems.RS,
                                TrapComponent.EXTERNAL_SYSTEM), SNMPUtil.getIPAddess()));
			}
			if (!oldFDPExternalSystemDTO.getIsActive() && newFDPRSConfigDTO.getIsActive()) {
                FDPLoggerFactory.getGenerateAlarmLogger().warn(
                        FDPConstant.EMPTY_STRING,
                        new Event(TrapSeverity.CLEAR, new TrapError((TrapErrorCodes.SYSTEM_INACTIVE), Arrays.asList(newFDPExternalSystemDTO
                                .getIpAddress().getValue(), newFDPExternalSystemDTO.getCircleDTO().getCircleCode()), TrapCISSystems.RS,
                                TrapComponent.EXTERNAL_SYSTEM), SNMPUtil.getIPAddess()));
			}
			status = true;
			// callClient();

		} else if (oldFDPExternalSystemDTO instanceof FDPSMSCConfigDTO
				&& newFDPExternalSystemDTO instanceof FDPSMSCConfigDTO) {
			final FDPSMSCConfigDTO newfdpsmscConfigDTO = (FDPSMSCConfigDTO) newFDPExternalSystemDTO;
			final FDPSMSCConfigDTO oldFdpsmscConfigDTO = (FDPSMSCConfigDTO) oldFDPExternalSystemDTO;
			String smscBindMode = PropertyUtils.getProperty("smsc.bind.mode");
			smscBindMode = (smscBindMode != null && !"".equals(smscBindMode)) ? smscBindMode
					: BusinessConstants.BIND_MODE_TXRX;
			
			smscBindMode = (smscBindMode != null && !"".equals(smscBindMode)) ? smscBindMode
					: BusinessConstants.BIND_MODE_TXRX;
			boolean routeStopped = false;
			setUserNamePasswordForSMSC(newfdpsmscConfigDTO);
			removeRoute(oldFDPExternalSystemDTO);
			updateRouteDetailForSMSCandUSSD(oldFdpsmscConfigDTO, newfdpsmscConfigDTO);
			if (newfdpsmscConfigDTO.getServiceType().equals(BusinessConstants.SERVICE_TYPE_SMS)) {
				updateCacheDTO.setModuleType(ModuleType.SMS_CONFEGURATION);
			}
			appCacheInitializerService.updateCache(updateCacheDTO);
			if ("receiver".equals(newfdpsmscConfigDTO.getBindNode())) {

				cdiCamelContext.addRoutes(createReceiverRoutes(newfdpsmscConfigDTO));
				if (newfdpsmscConfigDTO.getIsActive().equals(false)) {
					stopRoute(newFDPExternalSystemDTO);
					routeStopped = true;
				}

			} else if ("transmitter".equals(newfdpsmscConfigDTO.getBindNode())) {

				cdiCamelContext.addRoutes(createTransmitterRoutes(newfdpsmscConfigDTO));
				if (newfdpsmscConfigDTO.getIsActive().equals(false)) {
					stopRoute(newFDPExternalSystemDTO);
					routeStopped = true;
				}

			}
			if (routeStopped) {
				FDPLoggerFactory.getGenerateAlarmLogger()
						.warn(newfdpsmscConfigDTO.getIp(),
								new Event(TrapSeverity.CRITICAL, new TrapError((TrapErrorCodes.SMSC_INACTIVE), Arrays
										.asList(newfdpsmscConfigDTO.getIp(), ApplicationUtil.getServerName())),
										SNMPUtil.getIPAddess()));
			}
			if (/*!oldFDPExternalSystemDTO.getIsActive() &&*/ newfdpsmscConfigDTO.getIsActive()) {
				FDPLoggerFactory.getGenerateAlarmLogger()
						.warn(newfdpsmscConfigDTO.getIp(),
								new Event(TrapSeverity.CLEAR, new TrapError((TrapErrorCodes.SMSC_INACTIVE), Arrays
										.asList(newfdpsmscConfigDTO.getIp(), ApplicationUtil.getServerName())),
										SNMPUtil.getIPAddess()));
			}
			status = true;
			

		} else if (oldFDPExternalSystemDTO instanceof FDPEMAConfigDTO
				&& newFDPExternalSystemDTO instanceof FDPEMAConfigDTO) {
			final FDPEMAConfigDTO fdpemaConfigDTO = (FDPEMAConfigDTO) newFDPExternalSystemDTO;
			removeRoute(oldFDPExternalSystemDTO);
			updateCacheDTO.setModuleType(ModuleType.EMA_DETAILS);
			appCacheInitializerService.updateCache(updateCacheDTO);
			cdiCamelContext.addRoutes(createEmaCaiRoutes(fdpemaConfigDTO, "edit"));
			status = true;
			LOGGER.info("Routes for EMA DTO {} has edited successfully for Circle :{}", fdpemaConfigDTO,
					fdpemaConfigDTO.getCircleDTO().getCircleCode());
			if (fdpemaConfigDTO.getIsActive().equals(false)) {
				stopRoute(fdpemaConfigDTO);
                FDPLoggerFactory.getGenerateAlarmLogger().error(
                        FDPConstant.EMPTY_STRING,
                        new Event(TrapSeverity.CRITICAL, new TrapError((TrapErrorCodes.SYSTEM_INACTIVE), Arrays.asList(fdpemaConfigDTO
                                .getIpAddress().getValue(), fdpemaConfigDTO.getCircleDTO().getCircleCode()), TrapCISSystems.EMA,
                                TrapComponent.EXTERNAL_SYSTEM), SNMPUtil.getIPAddess()));
            } else if (!oldFDPExternalSystemDTO.getIsActive() && fdpemaConfigDTO.getIsActive()) {
                FDPLoggerFactory.getGenerateAlarmLogger().warn(
                        FDPConstant.EMPTY_STRING,
                        new Event(TrapSeverity.CLEAR, new TrapError((TrapErrorCodes.SYSTEM_INACTIVE), Arrays.asList(newFDPExternalSystemDTO
                                .getIpAddress().getValue(), newFDPExternalSystemDTO.getCircleDTO().getCircleCode()), TrapCISSystems.EMA,
                                TrapComponent.EXTERNAL_SYSTEM), SNMPUtil.getIPAddess()));
			}
		} else if (oldFDPExternalSystemDTO instanceof FDPVASConfigDTO
				&& newFDPExternalSystemDTO instanceof FDPVASConfigDTO) {
			final FDPVASConfigDTO fdpVASConfigDTO = (FDPVASConfigDTO) newFDPExternalSystemDTO;
			removeRoute(oldFDPExternalSystemDTO);
			if (fdpVASConfigDTO.getVasType().equalsIgnoreCase(ExternalSystem.MCARBON.name())) {
				updateCacheDTO.setModuleType(ModuleType.MCARBON_STORE);
				appCacheInitializerService.updateCache(updateCacheDTO);
				cdiCamelContext.addRoutes(createRoutesForMCarbon(fdpVASConfigDTO, fdpVASConfigDTO.getCircleDTO()
						.getCircleCode()));
				LOGGER.info("Routes for MCarbon DTO {} has edited successfully for Circle :{}", fdpVASConfigDTO,
						fdpVASConfigDTO.getCircleDTO().getCircleCode());
			}
			if (fdpVASConfigDTO.getVasType().equalsIgnoreCase(ExternalSystem.MANHATTAN.name())) {
				updateCacheDTO.setModuleType(ModuleType.MANHATTAN_STORE);
				appCacheInitializerService.updateCache(updateCacheDTO);
				cdiCamelContext.addRoutes(createRoutesForManhattan(fdpVASConfigDTO, fdpVASConfigDTO.getCircleDTO()
						.getCircleCode()));
				LOGGER.info("Routes for Manhattan DTO {} has edited successfully for Circle :{}", fdpVASConfigDTO,
						fdpVASConfigDTO.getCircleDTO().getCircleCode());
			}
			if (fdpVASConfigDTO.getVasType().equalsIgnoreCase(ExternalSystem.SBBB.name())) {
				updateCacheDTO.setModuleType(ModuleType.SBB_STORE);
				appCacheInitializerService.updateCache(updateCacheDTO);
				cdiCamelContext.addRoutes(createRoutesForSBBB(fdpVASConfigDTO, fdpVASConfigDTO.getCircleDTO()
						.getCircleCode()));
				LOGGER.info("Routes for SBBB DTO {} has edited successfully for Circle :{}", fdpVASConfigDTO,
						fdpVASConfigDTO.getCircleDTO().getCircleCode());
			}
			if (fdpVASConfigDTO.getIsActive().equals(false)) {
				stopRoute(fdpVASConfigDTO);
			}
		} else if (oldFDPExternalSystemDTO instanceof FDPMCLoanConfigDTO && newFDPExternalSystemDTO instanceof FDPMCLoanConfigDTO) {

			final FDPMCLoanConfigDTO newMcLoanDto = (FDPMCLoanConfigDTO) newFDPExternalSystemDTO;
			final FDPMCLoanConfigDTO oldMcLoanDto = (FDPMCLoanConfigDTO) oldFDPExternalSystemDTO;
			final String circleCode = newMcLoanDto.getCircleDTO().getCircleCode();
			
			updateCacheDTO.setModuleType(ModuleType.MCLOAN_STORE);
			appCacheInitializerService.updateCache(updateCacheDTO);
			
			if(oldMcLoanDto.getIsActive()){
				this.mcLoanhttpRoute.stopRouteforMcLoan(oldMcLoanDto);
			}
			
			this.mcLoanhttpRoute.stopLbRouteForMcLoan(circleCode);
			this.mcLoanhttpRoute.stopAllRoutesForMcLoan(circleCode);
			
			this.mcLoanhttpRoute.startAllRoutesForMcLoan(circleCode);
			this.mcLoanhttpRoute.startLbRouteForMcLoan(circleCode);
			
			if (newMcLoanDto.getIsActive().equals(false) && !oldMcLoanDto.getIsActive()) {
				FDPLoggerFactory.getGenerateAlarmLogger().warn(
						newMcLoanDto.getIpAddress().getValue(),
						new Event(TrapSeverity.CRITICAL, new TrapError((TrapErrorCodes.CMS_INACTIVE), Arrays.asList(
								newMcLoanDto.getIpAddress().getValue(), newMcLoanDto.getCircleDTO()
										.getCircleCode())), SNMPUtil.getIPAddess()));
			}
			if (!oldMcLoanDto.getIsActive() && newMcLoanDto.getIsActive()) {
				FDPLoggerFactory.getGenerateAlarmLogger().warn(
						newMcLoanDto.getIpAddress().getValue(),
						new Event(TrapSeverity.CLEAR, new TrapError((TrapErrorCodes.CMS_INACTIVE), Arrays.asList(
								newMcLoanDto.getIpAddress().getValue(), newMcLoanDto.getCircleDTO()
										.getCircleCode())), SNMPUtil.getIPAddess()));
			}
			status = true;
			// callClient();

		} 
		else if (oldFDPExternalSystemDTO instanceof FDPOfflineConfigDTO && newFDPExternalSystemDTO instanceof FDPOfflineConfigDTO) {

			final FDPOfflineConfigDTO newOfflineDto = (FDPOfflineConfigDTO) newFDPExternalSystemDTO;
			final FDPOfflineConfigDTO oldOfflineDto = (FDPOfflineConfigDTO) oldFDPExternalSystemDTO;
			final String circleCode = newOfflineDto.getCircleDTO().getCircleCode();
			
			updateCacheDTO.setModuleType(ModuleType.FDPOFFLINE_STORE);
			appCacheInitializerService.updateCache(updateCacheDTO);
			
			if(oldOfflineDto.getIsActive()){
				this.fdpOfflinehttpRoute.stopRouteforOffline(oldOfflineDto);
			}
			
			this.fdpOfflinehttpRoute.stopLbRouteForOffline(circleCode);
			this.fdpOfflinehttpRoute.stopAllRoutesForOffline(circleCode);
			
			this.fdpOfflinehttpRoute.startAllRoutesForOffline(circleCode);
			this.fdpOfflinehttpRoute.startLbRouteForOffline(circleCode);
			
			if (newOfflineDto.getIsActive().equals(false) && !oldOfflineDto.getIsActive()) {
				FDPLoggerFactory.getGenerateAlarmLogger().warn(
						newOfflineDto.getIpAddress().getValue(),
						new Event(TrapSeverity.CRITICAL, new TrapError((TrapErrorCodes.FDPOFFLINE_INACTIVE), Arrays.asList(
								newOfflineDto.getIpAddress().getValue(), newOfflineDto.getCircleDTO()
										.getCircleCode())), SNMPUtil.getIPAddess()));
			}
			if (!newOfflineDto.getIsActive() && newOfflineDto.getIsActive()) {
				FDPLoggerFactory.getGenerateAlarmLogger().warn(
						newOfflineDto.getIpAddress().getValue(),
						new Event(TrapSeverity.CLEAR, new TrapError((TrapErrorCodes.FDPOFFLINE_INACTIVE), Arrays.asList(
								newOfflineDto.getIpAddress().getValue(), newOfflineDto.getCircleDTO()
										.getCircleCode())), SNMPUtil.getIPAddess()));
			}
			status = true;
			// callClient();

		}
		else if(oldFDPExternalSystemDTO instanceof FDPMobileMoneyConfigDTO && newFDPExternalSystemDTO instanceof FDPMobileMoneyConfigDTO)
		{
			/*
			

			final FDPAIRConfigDTO newFDPAirConfigDTO = (FDPAIRConfigDTO) newFDPExternalSystemDTO;
			final FDPAIRConfigDTO oldFdpairConfigDTO = (FDPAIRConfigDTO) oldFDPExternalSystemDTO;
			updateCacheDTO.setModuleType(ModuleType.AIR_CONFEGURATION);
			appCacheInitializerService.updateCache(updateCacheDTO);
			cdiCamelContext.addRoutes(createAirRoutes(oldFdpairConfigDTO));
			if (newFDPAirConfigDTO.getIsActive().equals(false)) {
				String routeIdToBeStopped = HttpUtil.getAirSubRouteId(newFDPAirConfigDTO, newFDPAirConfigDTO
						.getCircleDTO().getCircleCode());
				cdiCamelContext.stopRoute(routeIdToBeStopped);
				FDPLoggerFactory.getGenerateAlarmLogger().warn(
						newFDPAirConfigDTO.getIpAddress().getValue(),
						new Event(TrapSeverity.CRITICAL, new TrapError((TrapErrorCodes.AIR_INACTIVE), Arrays.asList(
								newFDPAirConfigDTO.getIpAddress().getValue(), newFDPAirConfigDTO.getCircleDTO()
										.getCircleCode())), SNMPUtil.getIPAddess()));
			}
			if (!oldFDPExternalSystemDTO.getIsActive() && newFDPAirConfigDTO.getIsActive()) {
				FDPLoggerFactory.getGenerateAlarmLogger().warn(
						newFDPAirConfigDTO.getIpAddress().getValue(),
						new Event(TrapSeverity.CLEAR, new TrapError((TrapErrorCodes.AIR_INACTIVE), Arrays.asList(
								newFDPAirConfigDTO.getIpAddress().getValue(), newFDPAirConfigDTO.getCircleDTO()
										.getCircleCode())), SNMPUtil.getIPAddess()));
			}
			status = true;
			// callClient();


		 
			
			 * */

			final FDPMobileMoneyConfigDTO newFDPMMConfigDTO = (FDPMobileMoneyConfigDTO) newFDPExternalSystemDTO;
			final FDPMobileMoneyConfigDTO oldFdpMMConfigDTO = (FDPMobileMoneyConfigDTO) oldFDPExternalSystemDTO;

            if (newFDPMMConfigDTO.getIsActive().equals(false)) {
                FDPLoggerFactory.getGenerateAlarmLogger().error(
                        FDPConstant.EMPTY_STRING,
                        new Event(TrapSeverity.CRITICAL, new TrapError((TrapErrorCodes.SYSTEM_INACTIVE), Arrays.asList(
                                newFDPExternalSystemDTO.getIpAddress().getValue(), newFDPExternalSystemDTO.getCircleDTO().getCircleCode()),
                                TrapCISSystems.MM, TrapComponent.EXTERNAL_SYSTEM), SNMPUtil.getIPAddess()));
            }
            if (!oldFDPExternalSystemDTO.getIsActive() && newFDPMMConfigDTO.getIsActive()) {
                FDPLoggerFactory.getGenerateAlarmLogger().warn(
                        newFDPMMConfigDTO.getIpAddress().getValue(),
                        new Event(TrapSeverity.CLEAR, new TrapError((TrapErrorCodes.SYSTEM_INACTIVE), Arrays.asList(newFDPExternalSystemDTO
                                .getIpAddress().getValue(), newFDPExternalSystemDTO.getCircleDTO().getCircleCode()), TrapCISSystems.MM,
                                TrapComponent.EXTERNAL_SYSTEM), SNMPUtil.getIPAddess()));
            }

			updateCacheDTO.setModuleType(ModuleType.MOBILEMONEY_CONFEGURATION);
			appCacheInitializerService.updateCache(updateCacheDTO);
			createMobileMoneyRoutes(newFDPMMConfigDTO);
			status = true;
			
		}else if(oldFDPExternalSystemDTO instanceof FDPEVDSConfigDTO && newFDPExternalSystemDTO instanceof FDPEVDSConfigDTO){
			final FDPEVDSConfigDTO newFDPEVDSConfigDTO = (FDPEVDSConfigDTO) newFDPExternalSystemDTO;
			updateCacheDTO.setModuleType(ModuleType.EVDS_CONFIGURATION);
			appCacheInitializerService.updateCache(updateCacheDTO);
			if(evdsType.equalsIgnoreCase((String) FDPConstant.EVDS_TYPE_HTTP)){
				restartEvdsHttpRoutes(newFDPEVDSConfigDTO);
			}
			else if(evdsType.equalsIgnoreCase((String) FDPConstant.EVDS_HTTP_TYPE)){
				restartHttpRoutes(newFDPEVDSConfigDTO);
			}else{
				restartEvdsRoutes(newFDPEVDSConfigDTO);
			}
			status = true;
		}
		else if(oldFDPExternalSystemDTO instanceof FDPLoyaltyConfigDTO && newFDPExternalSystemDTO instanceof FDPLoyaltyConfigDTO) 
		{
			final FDPLoyaltyConfigDTO fdployaltyconfigdto=new FDPLoyaltyConfigDTO();
			updateCacheDTO.setModuleType(ModuleType.LOYALTY_CONFIGURATION);
			appCacheInitializerService.updateCache(updateCacheDTO);
			restartLoyaltyRoutes(newFDPExternalSystemDTO);
            if (newFDPExternalSystemDTO.getIsActive().equals(false)) {
                FDPLoggerFactory.getGenerateAlarmLogger().error(
                        FDPConstant.EMPTY_STRING,
                        new Event(TrapSeverity.CRITICAL, new TrapError((TrapErrorCodes.SYSTEM_INACTIVE), Arrays.asList(
                                newFDPExternalSystemDTO.getIpAddress().getValue(), newFDPExternalSystemDTO.getCircleDTO().getCircleCode()),
                                TrapCISSystems.Loyalty, TrapComponent.EXTERNAL_SYSTEM), SNMPUtil.getIPAddess()));
            }
            if (!oldFDPExternalSystemDTO.getIsActive() && newFDPExternalSystemDTO.getIsActive()) {
                FDPLoggerFactory.getGenerateAlarmLogger().warn(
                        newFDPExternalSystemDTO.getIpAddress().getValue(),
                        new Event(TrapSeverity.CLEAR, new TrapError((TrapErrorCodes.SYSTEM_INACTIVE), Arrays.asList(newFDPExternalSystemDTO
                                .getIpAddress().getValue(), newFDPExternalSystemDTO.getCircleDTO().getCircleCode()),
                                TrapCISSystems.Loyalty,
                                TrapComponent.EXTERNAL_SYSTEM), SNMPUtil.getIPAddess()));
            }
			status=true;
			
		} else if (oldFDPExternalSystemDTO instanceof FDPDMCConfigDTO && newFDPExternalSystemDTO instanceof FDPDMCConfigDTO) {

			final FDPDMCConfigDTO newFDPDmcConfigDTO = (FDPDMCConfigDTO) newFDPExternalSystemDTO;
			final FDPDMCConfigDTO oldFdpDmcConfigDTO = (FDPDMCConfigDTO) oldFDPExternalSystemDTO;
			updateCacheDTO.setModuleType(ModuleType.DMC_CONFEGURATION);
			appCacheInitializerService.updateCache(updateCacheDTO);
			cdiCamelContext.addRoutes(createDmcRoutes(oldFdpDmcConfigDTO));
			if (newFDPDmcConfigDTO.getIsActive().equals(false)) {
				String routeIdToBeStopped = HttpUtil.getDmcSubRouteId(newFDPDmcConfigDTO, newFDPDmcConfigDTO
						.getCircleDTO().getCircleCode());
				cdiCamelContext.stopRoute(routeIdToBeStopped);
				FDPLoggerFactory.getGenerateAlarmLogger().warn(
						newFDPDmcConfigDTO.getIpAddress().getValue(),
						new Event(TrapSeverity.CRITICAL, new TrapError((TrapErrorCodes.DMC_INACTIVE), Arrays.asList(
								newFDPDmcConfigDTO.getIpAddress().getValue(), newFDPDmcConfigDTO.getCircleDTO()
										.getCircleCode())), SNMPUtil.getIPAddess()));
			}
			if (!oldFDPExternalSystemDTO.getIsActive() && newFDPDmcConfigDTO.getIsActive()) {
				FDPLoggerFactory.getGenerateAlarmLogger().warn(
						newFDPDmcConfigDTO.getIpAddress().getValue(),
						new Event(TrapSeverity.CLEAR, new TrapError((TrapErrorCodes.DMC_INACTIVE), Arrays.asList(
								newFDPDmcConfigDTO.getIpAddress().getValue(), newFDPDmcConfigDTO.getCircleDTO()
										.getCircleCode())), SNMPUtil.getIPAddess()));
			}
			status = true;
			// callClient();

		} 
		else if (oldFDPExternalSystemDTO instanceof FDPAbilityConfigDTO && newFDPExternalSystemDTO instanceof FDPAbilityConfigDTO) {

            final FDPAbilityConfigDTO newFDPAbilityConfigDTO = (FDPAbilityConfigDTO) newFDPExternalSystemDTO;
            final FDPAbilityConfigDTO oldFdpAbilityConfigDTO = (FDPAbilityConfigDTO) oldFDPExternalSystemDTO;
            
            updateCacheDTO.setModuleType(ModuleType.ABILITY_CONFIGURATION);
            appCacheInitializerService.updateCache(updateCacheDTO);
            cdiCamelContext.addRoutes(createAbilityRoutes(oldFdpAbilityConfigDTO));
            
            if (newFDPAbilityConfigDTO.getIsActive().equals(false)) {
                String routeIdToBeStopped = HttpUtil.getAbilitySubRouteId(newFDPAbilityConfigDTO, newFDPAbilityConfigDTO
                        .getCircleDTO().getCircleCode());
                cdiCamelContext.stopRoute(routeIdToBeStopped);
                FDPLoggerFactory.getGenerateAlarmLogger().error(
                        FDPConstant.EMPTY_STRING,
                        new Event(TrapSeverity.CRITICAL, new TrapError((TrapErrorCodes.SYSTEM_INACTIVE), Arrays.asList(
                                newFDPExternalSystemDTO.getIpAddress().getValue(), newFDPExternalSystemDTO.getCircleDTO().getCircleCode()),
                                TrapCISSystems.Ability, TrapComponent.EXTERNAL_SYSTEM), SNMPUtil.getIPAddess()));
            }
            if (!oldFDPExternalSystemDTO.getIsActive() && newFDPAbilityConfigDTO.getIsActive()) {
                FDPLoggerFactory.getGenerateAlarmLogger().error(
                        FDPConstant.EMPTY_STRING,
                        new Event(TrapSeverity.CLEAR, new TrapError((TrapErrorCodes.SYSTEM_INACTIVE), Arrays.asList(
                                newFDPExternalSystemDTO.getIpAddress().getValue(), newFDPExternalSystemDTO.getCircleDTO().getCircleCode()),
                                TrapCISSystems.Ability, TrapComponent.EXTERNAL_SYSTEM), SNMPUtil.getIPAddess()));
            }
            status = true;
        }else if (oldFDPExternalSystemDTO instanceof FDPADCConfigDTO
				&& newFDPExternalSystemDTO instanceof FDPADCConfigDTO) {

			final FDPADCConfigDTO newFDPADCConfigDTO = (FDPADCConfigDTO) newFDPExternalSystemDTO;
			final FDPADCConfigDTO oldFdpADCConfigDTO = (FDPADCConfigDTO) oldFDPExternalSystemDTO;
			updateCacheDTO.setModuleType(ModuleType.ADC_CONFEGURATION);
			appCacheInitializerService.updateCache(updateCacheDTO);
			cdiCamelContext.addRoutes(createADCRoutes(oldFdpADCConfigDTO));
			if (newFDPADCConfigDTO.getIsActive().equals(false)) {
				String routeIdToBeStopped = HttpUtil.getADCSubRouteId(newFDPADCConfigDTO, newFDPADCConfigDTO
						.getCircleDTO().getCircleCode());
				cdiCamelContext.stopRoute(routeIdToBeStopped);
                FDPLoggerFactory.getGenerateAlarmLogger().error(
                        FDPConstant.EMPTY_STRING,
                        new Event(TrapSeverity.CRITICAL, new TrapError((TrapErrorCodes.SYSTEM_INACTIVE), Arrays.asList(newFDPADCConfigDTO
                                .getIpAddress().getValue(), newFDPADCConfigDTO.getCircleDTO().getCircleCode()), TrapCISSystems.ADC,
                                TrapComponent.EXTERNAL_SYSTEM), SNMPUtil.getIPAddess()));
			}
			if (!oldFDPExternalSystemDTO.getIsActive() && newFDPADCConfigDTO.getIsActive()) {
                FDPLoggerFactory.getGenerateAlarmLogger().warn(
                        FDPConstant.EMPTY_STRING,
                        new Event(TrapSeverity.CLEAR, new TrapError((TrapErrorCodes.SYSTEM_INACTIVE), Arrays.asList(newFDPExternalSystemDTO
                                .getIpAddress().getValue(), newFDPExternalSystemDTO.getCircleDTO().getCircleCode()), TrapCISSystems.ADC,
                                TrapComponent.EXTERNAL_SYSTEM), SNMPUtil.getIPAddess()));
			}
			status = true;
			// callClient();

		} 
		
		else {
			throw new IllegalArgumentException("Invalid FDPExternal System.");
		}
		return status;
	}

	private void restartLoyaltyRoutes(
			FDPExternalSystemDTO newFDPExternalSystemDTO) {

		try {
			loyalityhttpRoute.stopAllRoutes();
			loyalityhttpRoute.stopRoute(newFDPExternalSystemDTO);
			loyalityhttpRoute.createRoutes();
			
		} catch (Exception e) {
			LOGGER.error("Not Able to initialize the route ",e);
		}
	
		
	}

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
				LOGGER.info("Route {} has been started sucessfully for circle {}.", routeId, fdpMCarbonConfigDTO
						.getCircleDTO().getCircleCode());
			}
		};
	}

	public RouteBuilder createRoutesForManhattan(final FDPVASConfigDTO fdpVASConfigDTO, final String circleCode)
			throws ExecutionFailedException {
		return new RouteBuilder() {
			@SuppressWarnings("unchecked")
			@Override
			public void configure() throws Exception {
				final String logicalName = fdpVASConfigDTO.getLogicalName();
				final String fromEndpoint = HttpUtil.getEndpointForManhattan(circleCode,
						ExternalSystemType.MANHATTAN_TYPE, logicalName);
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
				LOGGER.info("Route {} has been started sucessfully for circle {}.", routeId, fdpVASConfigDTO
						.getCircleDTO().getCircleCode());
			}
		};
	}

	/**
	 * Creates the receiver routes.
	 * 
	 * @param fdpsmscConfigDTO
	 *            the fdpsmsc config dto
	 * @return the route builder
	 */
	private RouteBuilder createReceiverRoutes(final FDPSMSCConfigDTO fdpsmscConfigDTO) {
		return new RouteBuilder() {

			@Override
			public void configure() throws Exception {
				final String serverName = ApplicationUtil.getServerName();
				final String concurrentConsumers = PropertyUtils.getProperty("route.main.concurrentconsumer.rx");
				final String endpoint = getSMSCEndPointsRx(fdpsmscConfigDTO, serverName);
				final String routeId = serverName + BusinessConstants.UNDERSCORE + fdpsmscConfigDTO.getLogicalName();
				from(endpoint)
				.autoStartup(true)
						.process(incomingRequestIPProcessor)
						.setHeader(BusinessConstants.BIND_MODE_TYPE,
								constant(BusinessConstants.BIND_NODE_TYPE_RECEIVER)).routeId(routeId)
						.setHeader(BusinessConstants.BIND_MODE, constant(BusinessConstants.BIND_MODE_TXRX))
						.to(BusinessConstants.CAMEL_COMPONENT_TYPE + "Rx?concurrentConsumers=" + concurrentConsumers);
				
				
                /*
	            final ChoiceDefinition routeDefinition = from(
                        BusinessConstants.CAMEL_COMPONENT_TYPE
                                + "Rx?concurrentConsumers="
                                + concurrentConsumers)
                        .routeId(
                                BusinessConstants.MAIN_ROUTE
                                        + BusinessConstants.ROUTE_RX)
                        .routePolicy(routePolicyMainRoute)
                        .process(requestIdGenerator).process(circleFinder)
                        .choice();
                
	            
	            routeDefinition
                .when(header(BusinessConstants.CIRCLE_ID).isEqualTo(fdpsmscConfigDTO.getCircleDTO().getCircleCode()))
                .to(BusinessConstants.CAMEL_COMPONENT_TYPE
                        + "Rx"
                        + fdpsmscConfigDTO.getCircleDTO().getCircleCode()
                        + "?concurrentConsumers="
                        + PropertyUtils
                                .getProperty("route.subroute.concurrentconsumer.rx"));
	            
	            
	            from(
                        BusinessConstants.CAMEL_COMPONENT_TYPE
                                + "Rx"
                                + fdpsmscConfigDTO.getCircleDTO().getCircleCode()
                                + "?concurrentConsumers="
                                + PropertyUtils
                                        .getProperty("route.subroute.concurrentconsumer.rx"))

                        .routePolicy(routePolicyCircleRoute)
                        .routeId(
                                BusinessConstants.ROUTE_STR
                                        + fdpsmscConfigDTO.getCircleDTO().getCircleCode()
                                        + BusinessConstants.ROUTE_RX)
                        .process(requestProcessor);
	            */
				storeRouteIdForSMSC(fdpsmscConfigDTO, serverName, Arrays.asList(routeId));
			}
		};
	}

	/**
	 * Creates the transmitter routes.
	 * 
	 * @param fdpsmscConfigDTO
	 *            the fdpsmsc config dto
	 * @param action
	 *            the action
	 * @return the route builder
	 */
	@SuppressWarnings("unchecked")
	private RouteBuilder createTransmitterRoutes(final FDPSMSCConfigDTO fdpsmscConfigDTO) {
		return new RouteBuilder() {

			@Override
			public void configure() throws Exception {
				final String serverName = ApplicationUtil.getServerName();
				final int maximumRedeiveryAttempt = Integer.parseInt(PropertyUtils
						.getProperty(RoutingConstant.ROUTE_MAX_REDELIVER_ATTEMPT));
				final int maximumRedeliveryInterval = Integer.parseInt(PropertyUtils
						.getProperty(RoutingConstant.ROUTE_REDELIVER_DELAY));
				
				final CdiCamelContext cdiCamelContext = cdiCamelContextProvider.getContext();
				if (BusinessConstants.SERVICE_TYPE_SMS.equals(fdpsmscConfigDTO.getServiceType())) {
					/**
					 * SMS(WAP) Sub Route Defining routes for End Point Tx1 to
					 * Circles SMSC's , if we find two or more SMSC's for the
					 * Circle then load-balancer will manages the load in
					 * round-robin manner in case of fail-over otherwise there
					 * will no load-balancer in case of single SMSC.
					 */

					if (fdpsmscConfigDTO.getBindNode().trim().equals(BusinessConstants.BIND_NODE_TYPE_TRANSMITTER)) {
						final String routeIdForLBRouteStr = serverName + BusinessConstants.UNDERSCORE
								+ BusinessConstants.ROUTE_TX_SMS;
						cdiCamelContext.stopRoute(routeIdForLBRouteStr);
						cdiCamelContext.removeRoute(routeIdForLBRouteStr);
						final List<SMPPServerMappingDTO> fdpSmscConfigDTOList = getSMSCByCircleCode(serverName,
								ExternalSystemType.SMSC_TYPE);
						final int counter = getCountForSmscByServiceType(fdpsmscConfigDTO.getServiceType(),
								fdpSmscConfigDTOList, fdpsmscConfigDTO.getBindNode());

						if (counter > 1) {

							final LoadBalanceDefinition fromSmsDefinition = from(
									BusinessConstants.CAMEL_COMPONENT_TYPE + serverName
											+ BusinessConstants.ROUTE_TX_SMS)
									.log("Message came to Load balancer.")
									.routeId(serverName + BusinessConstants.UNDERSCORE + BusinessConstants.ROUTE_TX_SMS)
									.autoStartup(true)
									.loadBalance()
									.failover(1, false, true, org.jsmpp.PDUException.class,
											org.jsmpp.InvalidResponseException.class,
											org.jsmpp.extra.ResponseTimeoutException.class,
											org.jsmpp.extra.NegativeResponseException.class,
											java.net.ConnectException.class, java.io.IOException.class);
							final Map<String, Object> loadBalancerMap = loadBalancerStorage.getLoadBalancerMap();
							final String lbKey = ExternalSystemType.SMSC_TYPE.name() + BusinessConstants.COLON
									+ serverName;
							loadBalancerMap.put(lbKey, fromSmsDefinition);
							for (final FDPSMSCConfigDTO fdpsmscConfigDTO : fdpSmscConfigDTOList) {
								if (fdpsmscConfigDTO.getBindNode().trim()
										.equals(BusinessConstants.BIND_NODE_TYPE_TRANSMITTER)) {

									final List<String> routeIdList = new ArrayList<String>();
									final String routeIdLBVirtualEndpoint = "virtual" + BusinessConstants.UNDERSCORE
											+ fdpsmscConfigDTO.getLogicalName() + BusinessConstants.UNDERSCORE
											+ serverName;
									fromSmsDefinition
											.routeId(routeIdLBVirtualEndpoint)
											.to(BusinessConstants.CAMEL_COMPONENT_TYPE + serverName
													+ fdpsmscConfigDTO.getLogicalName()).end();

									final String smsUrl = getSMSCEndPointsTx(fdpsmscConfigDTO);
									final String ipAddress = fdpsmscConfigDTO.getIp();
									final String circleCodeIpaddressPort = serverName + BusinessConstants.COLON
											+ ipAddress + BusinessConstants.COLON
											+ String.valueOf(fdpsmscConfigDTO.getPort()) + BusinessConstants.COLON
											+ fdpsmscConfigDTO.getBindSystemId();
									final String routeId = fdpsmscConfigDTO.getLogicalName()
											+ BusinessConstants.UNDERSCORE + serverName;
									routeIdList.add(routeIdLBVirtualEndpoint);
									routeIdList.add(routeId);
									from(
											BusinessConstants.CAMEL_COMPONENT_TYPE + serverName
													+ fdpsmscConfigDTO.getLogicalName())
											.onCompletion().onCompleteOnly().process(completionProcessor).end()
											.setProperty(BusinessConstants.OUTGOING_IP_ADDRESS, constant(ipAddress))
											.setProperty(BusinessConstants.OUTGOING_CIRCLE_CODE_IP_PORT,
													constant(circleCodeIpaddressPort))
											.routeId(routeId)
											.process(responseProcessor)
											.process(logOutgoingIpProcessor)
											.onException(org.jsmpp.InvalidResponseException.class,
													org.jsmpp.extra.ResponseTimeoutException.class,
													org.jsmpp.extra.NegativeResponseException.class,
													java.net.ConnectException.class, java.io.IOException.class)
													.handled(false)
											.maximumRedeliveries(maximumRedeiveryAttempt)
											.redeliveryDelay(maximumRedeliveryInterval).onRedelivery(logRetryProcessor)
											.to(BusinessConstants.COMPONENT_EXCEPTION_ENDPOINT).end()
											.to(smsUrl);
									storeRouteIdForSMSC(fdpsmscConfigDTO, serverName, routeIdList);
								}

							}
						} else {
							for (final FDPSMSCConfigDTO fdpsmscConfigDTO : fdpSmscConfigDTOList) {
								if (fdpsmscConfigDTO.getBindNode().trim()
										.equals(BusinessConstants.BIND_NODE_TYPE_TRANSMITTER)) {
									final String ipAddress = fdpsmscConfigDTO.getIp();
									final String circleCodeIpaddressPort = serverName + BusinessConstants.COLON
											+ ipAddress + BusinessConstants.COLON
											+ String.valueOf(fdpsmscConfigDTO.getPort()) + BusinessConstants.COLON
											+ fdpsmscConfigDTO.getBindSystemId();
									final String smsUrl = getSMSCEndPointsTx(fdpsmscConfigDTO);
									final String routeId = fdpsmscConfigDTO.getLogicalName()
											+ BusinessConstants.UNDERSCORE + serverName;
									final List<String> routeIdList = new ArrayList<String>();
									from(
											BusinessConstants.CAMEL_COMPONENT_TYPE + serverName
													+ BusinessConstants.ROUTE_TX_SMS)
											.autoStartup(true)
											 .onCompletion().onCompleteOnly().process(completionProcessor).end()
											.setProperty(BusinessConstants.OUTGOING_IP_ADDRESS, constant(ipAddress))
											.setProperty(BusinessConstants.OUTGOING_CIRCLE_CODE_IP_PORT,
													constant(circleCodeIpaddressPort))
											.routeId(routeId)
											.process(responseProcessor)
											.process(logOutgoingIpProcessor)
											.onException(org.jsmpp.InvalidResponseException.class,
													org.jsmpp.extra.ResponseTimeoutException.class,
													org.jsmpp.extra.NegativeResponseException.class,
													java.net.ConnectException.class, java.io.IOException.class)
													.handled(false)
											.maximumRedeliveries(maximumRedeiveryAttempt)
											.redeliveryDelay(maximumRedeliveryInterval).onRedelivery(logRetryProcessor)
											.to(BusinessConstants.COMPONENT_EXCEPTION_ENDPOINT).end()
											.to(smsUrl);
									routeIdList.add(routeId);
									storeRouteIdForSMSC(fdpsmscConfigDTO, serverName, routeIdList);
								}
							}
						}
					}
				}
			}
		};
	}


	/**
	 * Creates MobileMoney Routes
	 * @param fdpAirConfigDTO
	 * @return the route builder
	 */
	private void createMobileMoneyRoutes(final FDPMobileMoneyConfigDTO fdpMMConfigDTO){
		try {
			if (mmProtocolType.equalsIgnoreCase(FDPConstant.MM_HTTP_TYPE)) {
				
			fdpMobileMoneyhttpRoute.stopAllRoutes();
			fdpMobileMoneyhttpRoute.stopRoute(fdpMMConfigDTO);
			fdpMobileMoneyhttpRoute.createRoutes();
				
			} else if (mmProtocolType.equalsIgnoreCase(FDPConstant.MM_HTTPS_TYPE)) {
				fdpMobileMoneyhttpsRoute.stopAllRoutes();
				fdpMobileMoneyhttpsRoute.stopRoute(fdpMMConfigDTO);
				fdpMobileMoneyhttpsRoute.createRoutes();
			} 
			
		} catch (Exception e) {	
			LOGGER.error("Not Able to initialize the route ",e);
		}
	}
	
	/**
	 * Restarts EVDS Routes
	 * @param fdpAirConfigDTO
	 * @return the route builder
	 */
	private void restartEvdsRoutes(final FDPEVDSConfigDTO fdpEvdsConfigDTO){
		try {
			fdpEvdsHttpRoute.stopAllRoutes();
			fdpEvdsHttpRoute.stopRoute(fdpEvdsConfigDTO);
			fdpEvdsHttpRoute.createRoutes();
		} catch (Exception e) {
			LOGGER.error("Not Able to initialize the route ",e);
		}
	}
	
	/**
	 * Creates the air routes.
	 * 
	 * @param fdpAirConfigDTO
	 *            the fdp air config dto
	 * @param action
	 *            the action
	 * @return the route builder
	 */
	private RouteBuilder createDmcRoutes(final FDPDMCConfigDTO fdpDmcConfigDTO) {
		return new RouteBuilder() {

			@SuppressWarnings("unchecked")
			@Override
			public void configure() throws Exception {

				final String circleCode = fdpDmcConfigDTO.getCircleDTO().getCircleCode();
				final List<FDPDMCConfigDTO> fdpDMCConfigDTOList = getEndpointURLsForDMCByCircleCode(circleCode);
				final boolean autostartup = true;
				final CdiCamelContext cdiCamelContext = cdiCamelContextProvider.getContext();
				final String routeIdForLB = ExternalSystem.DMC.name() + BusinessConstants.UNDERSCORE + circleCode;
				cdiCamelContext.stopRoute(routeIdForLB);
				cdiCamelContext.removeRoute(routeIdForLB);
				removeLastNodeDMCRoute(fdpDmcConfigDTO, circleCode, cdiCamelContext);
				final String[] endpoints = getCircleDmcEndPointsForLB(fdpDMCConfigDTOList, circleCode);
				for (final String endpoint : endpoints) {
					cdiCamelContext.stopRoute(endpoint);
					cdiCamelContext.removeRoute(endpoint);
				}
				for (FDPDMCConfigDTO fdpdmcConfigDTO : fdpDMCConfigDTOList) {

					String routeIdToBeRemove = HttpUtil.getDmcSubRouteId(fdpdmcConfigDTO, circleCode);
					cdiCamelContext.stopRoute(routeIdToBeRemove);
					cdiCamelContext.removeRoute(routeIdToBeRemove);
				}
				int fdpDMCConfigDTOListSize = fdpDMCConfigDTOList.size();
				LoadBalanceDefinition loadBalanceDefinition = null;
				if (fdpDMCConfigDTOList != null && fdpDMCConfigDTOListSize > 1) {

					loadBalanceDefinition = from(BusinessConstants.HTTP_COMPONENT_DMC_ENDPOINT + "_" + circleCode)
							.routeId(ExternalSystem.DMC.name() + BusinessConstants.UNDERSCORE + circleCode)
							.setExchangePattern(ExchangePattern.InOut).loadBalance()
							.failover(fdpDMCConfigDTOListSize - 1, false, true, java.lang.Exception.class);
					for (final String endpoint : endpoints) {

						loadBalanceDefinition.routeId(endpoint).to(endpoint);
					}
				} else {
					if (fdpDMCConfigDTOList != null && fdpDMCConfigDTOListSize == 1) {
						from(BusinessConstants.HTTP_COMPONENT_DMC_ENDPOINT + BusinessConstants.UNDERSCORE + circleCode)
								.setExchangePattern(ExchangePattern.InOut)
								.routeId(ExternalSystem.DMC.name() + BusinessConstants.UNDERSCORE + circleCode)
								.to(endpoints);
					}
				}
				for (int j = 0; fdpDMCConfigDTOList != null && j < fdpDMCConfigDTOListSize; j++) {

					final FDPDMCConfigDTO fdpDMCConfigDTO = fdpDMCConfigDTOList.get(j);
					final List<String> routeIdList = new ArrayList<String>();
					/**
					 * Manages the re-delivery attempt and re-delivery delay.
					 */
					final String circleCodeEndpoint = circleCode + fdpDMCConfigDTO.getExternalSystemId()
							+ BusinessConstants.UNDERSCORE + fdpDMCConfigDTO.getLogicalName();
					final String ipAddress = fdpDMCConfigDTO.getIpAddress().getValue();
					final String circleCodeIpaddressPort = circleCode + BusinessConstants.COLON + ipAddress
							+ BusinessConstants.COLON + String.valueOf(fdpDMCConfigDTO.getPort());
					final String routeId2 = BusinessConstants.SUB_ROUTE_UNDERSCORE + circleCodeEndpoint
							+ BusinessConstants.UNDERSCORE + ExternalSystem.DMC.name();
					routeIdList.add(routeId2);
					from(BusinessConstants.HTTP_COMPONENT_DMC_ENDPOINT + circleCodeEndpoint)
							.routeId(routeId2)
							.setProperty(BusinessConstants.OUTGOING_IP_ADDRESS, constant(ipAddress))
							.setProperty(BusinessConstants.OUTGOING_CIRCLE_CODE_IP_PORT,
									constant(circleCodeIpaddressPort))
							.process(logOutgoingIpProcessor)
							.process(headerProcessor)
							.autoStartup(autostartup)
							.onException(java.lang.Exception.class, java.net.ConnectException.class,
									java.net.SocketTimeoutException.class,
									org.apache.camel.component.http.HttpOperationFailedException.class)
							.maximumRedeliveries(fdpDMCConfigDTO.getNumberOfRetry())
							.redeliveryDelay(fdpDMCConfigDTO.getRetryInterval()).onRedelivery(logRetryProcessor).end()
							.to(getDMCEndpointURLForCircle(fdpDMCConfigDTO));
					addDMCRouteIdInCache(fdpDMCConfigDTO, routeIdList, circleCode, loadBalanceDefinition);
				}
			}
			
			
			

			private void removeLastNodeDMCRoute(FDPDMCConfigDTO fdpDmcConfigDTO, final String circleCode,
					final CdiCamelContext cdiCamelContext) throws Exception {
				List<FDPDMCConfigDTO> configDTOs = new ArrayList<FDPDMCConfigDTO>();
				configDTOs.add(fdpDmcConfigDTO);
				final String[] endpoints = getCircleDmcEndPointsForLB(configDTOs, circleCode);
				for (final String endpoint : endpoints) {
					cdiCamelContext.stopRoute(endpoint);
					cdiCamelContext.removeRoute(endpoint);
				}
				for (FDPDMCConfigDTO configDTO : configDTOs) {

					String routeIdToBeRemove = HttpUtil.getDmcSubRouteId(configDTO, circleCode);
					cdiCamelContext.stopRoute(routeIdToBeRemove);
					cdiCamelContext.removeRoute(routeIdToBeRemove);
				}
			}
			
			
		};
	}
	
	
	private RouteBuilder createAirRoutes(final FDPAIRConfigDTO fdpAirConfigDTO) {
		return new RouteBuilder() {

			@SuppressWarnings("unchecked")
			@Override
			public void configure() throws Exception {
				final String circleCode = fdpAirConfigDTO.getCircleDTO().getCircleCode();
				final List<FDPAIRConfigDTO> fdpAIRConfigDTOList = getEndpointURLsForAIRByCircleCode(circleCode);
				final boolean autostartup = true;
				final CdiCamelContext cdiCamelContext = cdiCamelContextProvider.getContext();
				final String routeIdForLB = ExternalSystem.AIR.name() + BusinessConstants.UNDERSCORE + circleCode;
				cdiCamelContext.stopRoute(routeIdForLB);
				cdiCamelContext.removeRoute(routeIdForLB);
				removeLastNodeRoute(fdpAirConfigDTO, circleCode, cdiCamelContext);
				final String[] endpoints = getCircleAirEndPointsForLB(fdpAIRConfigDTOList, circleCode);
				for (final String endpoint : endpoints) {
					cdiCamelContext.stopRoute(endpoint);
					cdiCamelContext.removeRoute(endpoint);
				}
				for (FDPAIRConfigDTO fdpairConfigDTO : fdpAIRConfigDTOList) {

					String routeIdToBeRemove = HttpUtil.getAirSubRouteId(fdpairConfigDTO, circleCode);
					cdiCamelContext.stopRoute(routeIdToBeRemove);
					cdiCamelContext.removeRoute(routeIdToBeRemove);
				}
				int fdpAIRConfigDTOListSize = fdpAIRConfigDTOList.size();
				LoadBalanceDefinition loadBalanceDefinition = null;
				if (fdpAIRConfigDTOList != null && fdpAIRConfigDTOListSize > 1) {

					loadBalanceDefinition = from(BusinessConstants.HTTP_COMPONENT_AIR_ENDPOINT + "_" + circleCode)
							.routeId(ExternalSystem.AIR.name() + BusinessConstants.UNDERSCORE + circleCode)
							.setExchangePattern(ExchangePattern.InOut).loadBalance()
							.failover(fdpAIRConfigDTOListSize - 1, false, true, java.lang.Exception.class);
					for (final String endpoint : endpoints) {

						loadBalanceDefinition.routeId(endpoint).to(endpoint);
					}
				} else {
					if (fdpAIRConfigDTOList != null && fdpAIRConfigDTOListSize == 1) {
						from(BusinessConstants.HTTP_COMPONENT_AIR_ENDPOINT + BusinessConstants.UNDERSCORE + circleCode)
								.setExchangePattern(ExchangePattern.InOut)
								.routeId(ExternalSystem.AIR.name() + BusinessConstants.UNDERSCORE + circleCode)
								.to(endpoints);
					}
				}
				for (int j = 0; fdpAIRConfigDTOList != null && j < fdpAIRConfigDTOListSize; j++) {

					final FDPAIRConfigDTO fdpAIRConfigDTO = fdpAIRConfigDTOList.get(j);
					final List<String> routeIdList = new ArrayList<String>();
					/**
					 * Manages the re-delivery attempt and re-delivery delay.
					 */
					final String circleCodeEndpoint = circleCode + fdpAIRConfigDTO.getExternalSystemId()
							+ BusinessConstants.UNDERSCORE + fdpAIRConfigDTO.getLogicalName();
					final String ipAddress = fdpAIRConfigDTO.getIpAddress().getValue();
					final String circleCodeIpaddressPort = circleCode + BusinessConstants.COLON + ipAddress
							+ BusinessConstants.COLON + String.valueOf(fdpAIRConfigDTO.getPort());
					final String routeId2 = BusinessConstants.SUB_ROUTE_UNDERSCORE + circleCodeEndpoint
							+ BusinessConstants.UNDERSCORE + ExternalSystem.AIR.name();
					routeIdList.add(routeId2);
					from(BusinessConstants.HTTP_COMPONENT_AIR_ENDPOINT + circleCodeEndpoint)
					.autoStartup(autostartup)
							.routeId(routeId2)
							.onCompletion().onCompleteOnly().process(completionProcessor).end()
							.setProperty(BusinessConstants.OUTGOING_IP_ADDRESS, constant(ipAddress))
							.setProperty(BusinessConstants.OUTGOING_CIRCLE_CODE_IP_PORT,
									constant(circleCodeIpaddressPort))
							.process(logOutgoingIpProcessor)
							.process(headerProcessor)
							.onException(java.lang.Exception.class, java.net.ConnectException.class,
									java.net.SocketTimeoutException.class,
									org.apache.camel.component.http.HttpOperationFailedException.class).handled(false)
							.maximumRedeliveries(fdpAIRConfigDTO.getNumberOfRetry())
							.redeliveryDelay(fdpAIRConfigDTO.getRetryInterval()).onRedelivery(logRetryProcessor)
							.to(BusinessConstants.COMPONENT_EXCEPTION_ENDPOINT).end()
							.to(getAIREndpointURLForCircle(fdpAIRConfigDTO));
					addRouteIdInCache(fdpAIRConfigDTO, routeIdList, circleCode, loadBalanceDefinition);
				}
			}
			
			
			

			private void removeLastNodeRoute(FDPAIRConfigDTO fdpAirConfigDTO, final String circleCode,
					final CdiCamelContext cdiCamelContext) throws Exception {
				List<FDPAIRConfigDTO> configDTOs = new ArrayList<FDPAIRConfigDTO>();
				configDTOs.add(fdpAirConfigDTO);
				final String[] endpoints = getCircleAirEndPointsForLB(configDTOs, circleCode);
				for (final String endpoint : endpoints) {
					cdiCamelContext.stopRoute(endpoint);
					cdiCamelContext.removeRoute(endpoint);
				}
				for (FDPAIRConfigDTO configDTO : configDTOs) {

					String routeIdToBeRemove = HttpUtil.getAirSubRouteId(configDTO, circleCode);
					cdiCamelContext.stopRoute(routeIdToBeRemove);
					cdiCamelContext.removeRoute(routeIdToBeRemove);
				}
			}

		};
	}
	
	/**
	 * Creates the cms routes.
	 * 
	 * @param fdpCmsConfigDTO
	 *            the fdp cms config dto
	 * @param action
	 *            the action
	 * @return the route builder
	 */
	private RouteBuilder createCmsRoutes(final FDPCMSConfigDTO fdpCmsConfigDTO) {
		return new RouteBuilder() {

			@SuppressWarnings("unchecked")
			@Override
			public void configure() throws Exception {

				final String circleCode = fdpCmsConfigDTO.getCircleDTO().getCircleCode();
				final List<FDPCMSConfigDTO> fdpCMSConfigDTOList = getEndpointURLsForCMSByCircleCode(circleCode);
				final boolean autostartup = true;
				final CdiCamelContext cdiCamelContext = cdiCamelContextProvider.getContext();
				final String routeIdForLB = ExternalSystem.CMS.name() + BusinessConstants.UNDERSCORE + circleCode;
				cdiCamelContext.stopRoute(routeIdForLB);
				cdiCamelContext.removeRoute(routeIdForLB);
				removeLastNodeRoute(fdpCmsConfigDTO, circleCode, cdiCamelContext);
				final String[] endpoints = getCircleCmsEndPointsForLB(fdpCMSConfigDTOList, circleCode);
				for (final String endpoint : endpoints) {
					cdiCamelContext.stopRoute(endpoint);
					cdiCamelContext.removeRoute(endpoint);
				}
				for (FDPCMSConfigDTO fdpcmsConfigDTO : fdpCMSConfigDTOList) {

					String routeIdToBeRemove = HttpUtil.getCmsSubRouteId(fdpcmsConfigDTO, circleCode);
					cdiCamelContext.stopRoute(routeIdToBeRemove);
					cdiCamelContext.removeRoute(routeIdToBeRemove);
				}
				int fdpCMSConfigDTOListSize = fdpCMSConfigDTOList.size();
				LoadBalanceDefinition loadBalanceDefinition = null;
				if (fdpCMSConfigDTOList != null && fdpCMSConfigDTOListSize > 1) {

					loadBalanceDefinition = from(BusinessConstants.HTTP_COMPONENT_CMS_ENDPOINT + "_" + circleCode)
							.routeId(ExternalSystem.CMS.name() + BusinessConstants.UNDERSCORE + circleCode)
							.setExchangePattern(ExchangePattern.InOut).loadBalance()
							.failover(fdpCMSConfigDTOListSize - 1, false, true, java.lang.Exception.class);
					for (final String endpoint : endpoints) {

						loadBalanceDefinition.routeId(endpoint).to(endpoint);
					}
				} else {
					if (fdpCMSConfigDTOList != null && fdpCMSConfigDTOListSize == 1) {
						from(BusinessConstants.HTTP_COMPONENT_CMS_ENDPOINT + BusinessConstants.UNDERSCORE + circleCode)
								.setExchangePattern(ExchangePattern.InOut)
								.routeId(ExternalSystem.CMS.name() + BusinessConstants.UNDERSCORE + circleCode)
								.to(endpoints);
					}
				}
				for (int j = 0; fdpCMSConfigDTOList != null && j < fdpCMSConfigDTOListSize; j++) {

					final FDPCMSConfigDTO fdpCMSConfigDTO = fdpCMSConfigDTOList.get(j);
					final List<String> routeIdList = new ArrayList<String>();
					/**
					 * Manages the re-delivery attempt and re-delivery delay.
					 */
					final String circleCodeEndpoint = circleCode + fdpCMSConfigDTO.getExternalSystemId()
							+ BusinessConstants.UNDERSCORE + fdpCMSConfigDTO.getLogicalName();
					final String ipAddress = fdpCMSConfigDTO.getIpAddress().getValue();
					final String circleCodeIpaddressPort = circleCode + BusinessConstants.COLON + ipAddress
							+ BusinessConstants.COLON + String.valueOf(fdpCMSConfigDTO.getPort());
					final String routeId2 = BusinessConstants.SUB_ROUTE_UNDERSCORE + circleCodeEndpoint
							+ BusinessConstants.UNDERSCORE + ExternalSystem.CMS.name();
					routeIdList.add(routeId2);
					from(BusinessConstants.HTTP_COMPONENT_CMS_ENDPOINT + circleCodeEndpoint)
					.autoStartup(autostartup)
							.routeId(routeId2)
							.onCompletion().onCompleteOnly().process(completionProcessor).end()
							.setProperty(BusinessConstants.OUTGOING_IP_ADDRESS, constant(ipAddress))
							.setProperty(BusinessConstants.OUTGOING_CIRCLE_CODE_IP_PORT,
									constant(circleCodeIpaddressPort))
							.process(logOutgoingIpProcessor)
							.process(headerProcessor)
							.onException(java.lang.Exception.class, java.net.ConnectException.class,
									java.net.SocketTimeoutException.class,
									org.apache.camel.component.http.HttpOperationFailedException.class)
									.handled(false)
							.maximumRedeliveries(fdpCMSConfigDTO.getNumberOfRetry())
							.redeliveryDelay(fdpCMSConfigDTO.getRetryInterval()).onRedelivery(logRetryProcessor)
							.to(BusinessConstants.COMPONENT_EXCEPTION_ENDPOINT).end()
							.to(getCMSEndpointURLForCircle(fdpCMSConfigDTO));
					addCMSRouteIdInCache(fdpCMSConfigDTO, routeIdList, circleCode, loadBalanceDefinition);
				}
			}
			
			
			

			private void removeLastNodeRoute(FDPCMSConfigDTO fdpCmsConfigDTO, final String circleCode,
					final CdiCamelContext cdiCamelContext) throws Exception {
				List<FDPCMSConfigDTO> configDTOs = new ArrayList<FDPCMSConfigDTO>();
				configDTOs.add(fdpCmsConfigDTO);
				final String[] endpoints = getCircleCmsEndPointsForLB(configDTOs, circleCode);
				for (final String endpoint : endpoints) {
					cdiCamelContext.stopRoute(endpoint);
					cdiCamelContext.removeRoute(endpoint);
				}
				for (FDPCMSConfigDTO configDTO : configDTOs) {

					String routeIdToBeRemove = HttpUtil.getCmsSubRouteId(configDTO, circleCode);
					cdiCamelContext.stopRoute(routeIdToBeRemove);
					cdiCamelContext.removeRoute(routeIdToBeRemove);
				}
			}

		};
	}

	/**
	 * Call client.
	 * 
	 * @throws Exception
	 *             the exception
	 */
	public void callClient() throws Exception {

		final String httpRequest = "<?xml version=\"1.0\"?>\n<methodCall>\n<methodName>"
				+ "GetAccountDetails</methodName>\n<params>\n<param>\n<value>\n<struct>"
				+ "\n<member>\n<name>originNodeType</name>\n<value>\n<string>EXT</string>\n"
				+ "</value>\n</member>\n<member>\n<name>originHostName</name>\n<value>\n"
				+ "<string>jaiprakash13540</string>\n</value>\n</member>\n<member>\n"
				+ "<name>originTransactionID</name>\n<value>\n<string>123</string>"
				+ "\n</value>\n</member>\n<member>\n<name>originTimeStamp</name>"
				+ "\n<value>\n<dateTime.iso8601>20130610T11:20:59+0530</dateTime.iso8601>\n</value>"
				+ "\n</member>\n<member>\n<name>subscriberNumberNAI</name>\n<value>\n<i4>2</i4>\n</value>"
				+ "\n</member>\n<member>\n<name>subscriberNumber</name>\n<value>\n<string>9971224554</string>"
				+ "\n</value>\n</member>\n<member>\n<name>requestPamInformationFlag</name>"
				+ "\n<value>\n<boolean>1</boolean>\n</value>\n</member>\n</struct>\n</value>"
				+ "\n</param>\n</params>\n</methodCall>";
		final String httpRequest1 = "GetAccountDetails";
		final Endpoint endpoint = cdiCamelContextProvider.getContext().getEndpoint(
				BusinessConstants.HTTP_COMPONENT_AIR_ENDPOINT, DirectEndpoint.class);
		final Exchange exchange = endpoint.createExchange();
		exchange.setPattern(ExchangePattern.InOut);
		final Message in = exchange.getIn();
		in.setHeader(Exchange.HTTP_PROTOCOL_VERSION, "AirSampleApp/HandleAir");
		in.setHeader(Exchange.CONTENT_TYPE, "text/xml");
		in.setHeader(Exchange.HTTP_METHOD, "POST");
		in.setHeader(Exchange.HTTP_METHOD, BusinessConstants.HTTP_METHOD);
		in.setHeader(Exchange.CONTENT_LENGTH, httpRequest.length());
		in.setHeader("Accept", "text/xml");
		in.setHeader("Connection", "Keep-alive");
		exchange.setProperty(BusinessConstants.CIRCLE_CODE, "DEL");
		exchange.setProperty(BusinessConstants.EXTERNAL_SYSTEM_TYPE, "AIR");
		String requestId = "123";
		exchange.setProperty(BusinessConstants.REQUEST_ID, requestId);
		in.setBody(httpRequest);
		String outputXML = "";
		final Producer producer = endpoint.createProducer();
		producer.process(exchange);
		final Message out = exchange.getOut();
		outputXML = out.getBody(String.class);
		final String responseCode = out.getHeader("camelhttpresponsecode", String.class);
		final Logger circleLoggerRequest = FDPLoggerFactory.getRequestLogger("Delhi",
				BusinessModuleType.AIR_SOUTH.name());
		if (responseCode != null && "200".equals(responseCode) && outputXML != null) {
			FDPLogger.info(circleLoggerRequest, getClass(), "process()",
					new StringBuilder(BusinessConstants.REQUEST_ID).append(FDPConstant.LOGGER_KEY_VALUE_DELIMITER)
							.append(requestId).append(FDPConstant.LOGGER_DELIMITER).append(FDPConstant.IFRESMODE)
							.append(FDPConstant.LOGGER_KEY_VALUE_DELIMITER).append(FDPConstant.RESULT_SUCCESS)
							.toString());
		} else {
			final Throwable caused = exchange.getProperty(Exchange.EXCEPTION_CAUGHT, Throwable.class);
			int statusCode = 0;
			String statusText = null;
			if (caused instanceof HttpOperationFailedException) {
				statusCode = ((HttpOperationFailedException) caused).getStatusCode();
				statusText = ((HttpOperationFailedException) caused).getStatusText();
			}
			FDPLogger.info(
					circleLoggerRequest,
					getClass(),
					"process()",
					new StringBuilder(BusinessConstants.REQUEST_ID).append(FDPConstant.LOGGER_KEY_VALUE_DELIMITER)
							.append(requestId).append(FDPConstant.LOGGER_DELIMITER).append(FDPConstant.IFRESMODE)
							.append(FDPConstant.LOGGER_KEY_VALUE_DELIMITER).append(FDPConstant.RESULT_FAILURE)
							.append(FDPConstant.LOGGER_DELIMITER).append(FDPConstant.IFRESRSN)
							.append(FDPConstant.LOGGER_KEY_VALUE_DELIMITER).append(statusText).toString());
			final String outGoingcircleCodeIPaddressPort = exchange.getProperty(
					BusinessConstants.OUTGOING_CIRCLE_CODE_IP_PORT, String.class);
			final StringBuilder stringBuilder = new StringBuilder();
			stringBuilder.append(BusinessConstants.REQUEST_ID).append(FDPConstant.LOGGER_KEY_VALUE_DELIMITER)
					.append(requestId).append(FDPConstant.LOGGER_DELIMITER);
			if (outGoingcircleCodeIPaddressPort != null) {
				stringBuilder.append(FDPConstant.CHARGING_NODE_IP).append(FDPConstant.LOGGER_KEY_VALUE_DELIMITER)
						.append(outGoingcircleCodeIPaddressPort)
						.append(FDPConstant.LOGGER_DELIMITER + FDPConstant.INTERFACE_TYPE)
						.append(FDPConstant.LOGGER_KEY_VALUE_DELIMITER).append("AIR")
						.append(FDPConstant.LOGGER_DELIMITER).append(FDPConstant.CHARGING_NODE)
						.append(FDPConstant.LOGGER_KEY_VALUE_DELIMITER).append("AIR_DELHI")
						.append(FDPConstant.LOGGER_DELIMITER).append(FDPConstant.IFRESRSN)
						.append(FDPConstant.LOGGER_KEY_VALUE_DELIMITER).append("Error in exchange is ")
						.append(exchange.getException());
			} else {
				stringBuilder.append("Could not get out goind circle code and ip.");
			}
			stringBuilder.append(" Response code found ").append(statusCode).append(" with status text :")
					.append(statusText);
			// responseCode = BusinessConstants.HTTP_ADAPTER_ERROR_CODE;
			FDPLogger.error(circleLoggerRequest, getClass(), "process()", stringBuilder.toString());
		}
	}

	/**
	 * Creates the cgw routes.
	 * 
	 * @param fdpCgwConfigDTO
	 *            the fdp cgw config dto
	 * @return the route builder
	 */
	private RouteBuilder createCGWRoutes(final FDPCGWConfigDTO fdpCgwConfigDTO) {
		return new RouteBuilder() {

			@SuppressWarnings("unchecked")
			@Override
			public void configure() throws Exception {

				final String circleCode = fdpCgwConfigDTO.getCircleDTO().getCircleCode();
				final int maximumRedeiveryAttempt = Integer.parseInt(PropertyUtils
						.getProperty(RoutingConstant.ROUTE_MAX_REDELIVER_ATTEMPT));
				final int maximumRedeliveryInterval = Integer.parseInt(PropertyUtils
						.getProperty(RoutingConstant.ROUTE_REDELIVER_DELAY));
				final List<FDPCGWConfigDTO> fdpCGWConfigDTOList = getEndpointURLsForCGWByCircleCode(circleCode);
				final boolean autostartup = true;
				final CdiCamelContext cdiCamelContext = cdiCamelContextProvider.getContext();
				final String routeIdForLB = ExternalSystem.CGW.name() + BusinessConstants.UNDERSCORE + circleCode;
				cdiCamelContext.stopRoute(routeIdForLB);
				cdiCamelContext.removeRoute(routeIdForLB);
				removeLastNodeRoute(fdpCgwConfigDTO, circleCode, cdiCamelContext);
				final String[] endpoints = getCircleCGWEndPointsForLB(fdpCGWConfigDTOList, circleCode);
				for (final String endpoint : endpoints) {
					cdiCamelContext.stopRoute(endpoint);
					cdiCamelContext.removeRoute(endpoint);
				}
				for (FDPCGWConfigDTO fdpcgwConfigDTO : fdpCGWConfigDTOList) {

					String routeIdToBeRemove = HttpUtil.getCgwSubRouteId(fdpcgwConfigDTO, circleCode);
					cdiCamelContext.stopRoute(routeIdToBeRemove);
					cdiCamelContext.removeRoute(routeIdToBeRemove);
				}
				int fdpCgwConfigDTOListSize = fdpCGWConfigDTOList.size();
				LoadBalanceDefinition loadBalanceDefinition = null;
				if (fdpCGWConfigDTOList != null && fdpCgwConfigDTOListSize > 1) {

					loadBalanceDefinition = from(BusinessConstants.HTTP_COMPONENT_CGW_ENDPOINT + "_" + circleCode)
							.routeId(ExternalSystem.CGW.name() + BusinessConstants.UNDERSCORE + circleCode)
							.setExchangePattern(ExchangePattern.InOut).loadBalance()
							.failover(fdpCgwConfigDTOListSize - 1, false, true, java.lang.Exception.class);
					for (final String endpoint : endpoints) {

						loadBalanceDefinition.routeId(endpoint).to(endpoint);
					}
				} else {
					if (fdpCGWConfigDTOList != null && fdpCgwConfigDTOListSize == 1) {
						from(BusinessConstants.HTTP_COMPONENT_CGW_ENDPOINT + BusinessConstants.UNDERSCORE + circleCode)
								.setExchangePattern(ExchangePattern.InOut)
								.routeId(ExternalSystem.CGW.name() + BusinessConstants.UNDERSCORE + circleCode)
								.to(endpoints);
					}
				}
				for (int j = 0; fdpCGWConfigDTOList != null && j < fdpCgwConfigDTOListSize; j++) {

					final FDPCGWConfigDTO fdpCGWConfigDTO = fdpCGWConfigDTOList.get(j);
					final List<String> routeIdList = new ArrayList<String>();
					/**
					 * Manages the re-delivery attempt and re-delivery delay.
					 */
					final String circleCodeEndpoint = circleCode + fdpCGWConfigDTO.getExternalSystemId()
							+ BusinessConstants.UNDERSCORE + fdpCGWConfigDTO.getLogicalName();
					final String ipAddress = fdpCGWConfigDTO.getIpAddress().getValue();
					final String circleCodeIpaddressPort = circleCode + BusinessConstants.COLON + ipAddress
							+ BusinessConstants.COLON + String.valueOf(fdpCGWConfigDTO.getPort());
					final String routeId2 = BusinessConstants.SUB_ROUTE_UNDERSCORE + circleCodeEndpoint
							+ BusinessConstants.UNDERSCORE + ExternalSystem.CGW.name();
					routeIdList.add(routeId2);
					from(BusinessConstants.HTTP_COMPONENT_CGW_ENDPOINT + circleCodeEndpoint)
					.autoStartup(autostartup)
							.routeId(routeId2)
							.onCompletion().onCompleteOnly().process(completionProcessor).end()
							.setProperty(BusinessConstants.OUTGOING_IP_ADDRESS, constant(ipAddress))
							.setProperty(BusinessConstants.OUTGOING_CIRCLE_CODE_IP_PORT,
									constant(circleCodeIpaddressPort))
							.process(logOutgoingIpProcessor)
							.process(headerProcessor)
							.onException(java.lang.Exception.class, java.net.ConnectException.class,
									java.net.SocketTimeoutException.class,
									org.apache.camel.component.http.HttpOperationFailedException.class)
									.handled(false)
							.maximumRedeliveries(maximumRedeiveryAttempt).redeliveryDelay(maximumRedeliveryInterval)
							.onRedelivery(logRetryProcessor)
							.to(BusinessConstants.COMPONENT_EXCEPTION_ENDPOINT).end()
							.to(getCGWEndpointURLForCircle(fdpCGWConfigDTO));
				}
			}

			private void removeLastNodeRoute(FDPCGWConfigDTO fdpCgwConfigDTO, String circleCode,
					CdiCamelContext cdiCamelContext) throws Exception {
				List<FDPCGWConfigDTO> configDTOs = new ArrayList<FDPCGWConfigDTO>();
				configDTOs.add(fdpCgwConfigDTO);
				final String[] endpoints = getCircleCGWEndPointsForLB(configDTOs, circleCode);
				for (final String endpoint : endpoints) {
					cdiCamelContext.stopRoute(endpoint);
					cdiCamelContext.removeRoute(endpoint);
				}
				for (FDPCGWConfigDTO configDTO : configDTOs) {
					String routeIdToBeRemove = HttpUtil.getCgwSubRouteId(configDTO, circleCode);
					cdiCamelContext.stopRoute(routeIdToBeRemove);
					cdiCamelContext.removeRoute(routeIdToBeRemove);
				}
			}

		};
	}

	/**
	 * Creates the rs routes.
	 * 
	 * @param fdpRSConfigDTO
	 *            the fdp rs config dto
	 * @param action
	 *            the action
	 * @return the route builder
	 */
	private RouteBuilder createRSRoutes(final FDPRSConfigDTO fdpRSConfigDTO) {
		return new RouteBuilder() {

			@SuppressWarnings("unchecked")
			@Override
			public void configure() throws Exception {

				/*final int maximumRedeiveryAttempt = Integer.parseInt(PropertyUtils
						.getProperty(RoutingConstant.ROUTE_MAX_REDELIVER_ATTEMPT));
				final int maximumRedeliveryInterval = Integer.parseInt(PropertyUtils
						.getProperty(RoutingConstant.ROUTE_REDELIVER_DELAY));*/

				final String circleCode = fdpRSConfigDTO.getCircleDTO().getCircleCode();
				final List<FDPRSConfigDTO> fdpRSConfigDTOList = getEndpointURLsForRSByCircleCode(circleCode);
				final boolean autostartup = true;
				final CdiCamelContext cdiCamelContext = cdiCamelContextProvider.getContext();
				final String routeIdForLB = ExternalSystem.RS.name() + BusinessConstants.UNDERSCORE + circleCode;
				cdiCamelContext.stopRoute(routeIdForLB);
				cdiCamelContext.removeRoute(routeIdForLB);
				removeLastNodeRoute(fdpRSConfigDTO, circleCode, cdiCamelContext);
				final String[] endpoints = getCircleRSEndPointsForLB(fdpRSConfigDTOList, circleCode);
				for (final String endpoint : endpoints) {
					cdiCamelContext.stopRoute(endpoint);
					cdiCamelContext.removeRoute(endpoint);
				}
				for (FDPRSConfigDTO fdpRsConfigDTO : fdpRSConfigDTOList) {

					String routeIdToBeRemove = HttpUtil.getRSSubRouteId(fdpRsConfigDTO, circleCode);
					cdiCamelContext.stopRoute(routeIdToBeRemove);
					cdiCamelContext.removeRoute(routeIdToBeRemove);
				}
				int fdpRSConfigDTOListSize = fdpRSConfigDTOList.size();
				LoadBalanceDefinition loadBalanceDefinition = null;
				if (fdpRSConfigDTOList != null && fdpRSConfigDTOListSize > 1) {

					loadBalanceDefinition = from(BusinessConstants.HTTP_COMPONENT_RS_ENDPOINT + "_" + circleCode)
							.routeId(ExternalSystem.RS.name() + BusinessConstants.UNDERSCORE + circleCode)
							.setExchangePattern(ExchangePattern.InOut).loadBalance()
							.failover(fdpRSConfigDTOListSize - 1, false, true, java.lang.Exception.class);
					for (final String endpoint : endpoints) {

						loadBalanceDefinition.routeId(endpoint).to(endpoint);
					}
				} else {
					if (fdpRSConfigDTOList != null && fdpRSConfigDTOListSize == 1) {
						from(BusinessConstants.HTTP_COMPONENT_RS_ENDPOINT + BusinessConstants.UNDERSCORE + circleCode)
								.setExchangePattern(ExchangePattern.InOut)
								.routeId(ExternalSystem.RS.name() + BusinessConstants.UNDERSCORE + circleCode)
								.to(endpoints);
					}
				}
				for (int j = 0; fdpRSConfigDTOList != null && j < fdpRSConfigDTOListSize; j++) {

					final FDPRSConfigDTO fdpRSConfigDTO = fdpRSConfigDTOList.get(j);
					final List<String> routeIdList = new ArrayList<String>();
					/**
					 * Manages the re-delivery attempt and re-delivery delay.
					 */
					final String circleCodeEndpoint = circleCode + fdpRSConfigDTO.getExternalSystemId()
							+ BusinessConstants.UNDERSCORE + fdpRSConfigDTO.getLogicalName();
					final String ipAddress = fdpRSConfigDTO.getIpAddress().getValue();
					final String circleCodeIpaddressPort = circleCode + BusinessConstants.COLON + ipAddress
							+ BusinessConstants.COLON + String.valueOf(fdpRSConfigDTO.getPort());
					final String routeId2 = HttpUtil.getRSSubRouteId(fdpRSConfigDTO, circleCode);
					routeIdList.add(routeId2);
					from(BusinessConstants.HTTP_COMPONENT_RS_ENDPOINT + circleCodeEndpoint)
					.autoStartup(autostartup)
							.routeId(routeId2)
							.onCompletion().onCompleteOnly().process(completionProcessor).end()
							.setProperty(BusinessConstants.OUTGOING_IP_ADDRESS, constant(ipAddress))
							.setProperty(BusinessConstants.OUTGOING_CIRCLE_CODE_IP_PORT,
									constant(circleCodeIpaddressPort))
							.process(logOutgoingIpProcessor)
							.process(headerProcessor)
							.onException(java.lang.Exception.class, java.net.ConnectException.class,
									java.net.SocketTimeoutException.class,
									org.apache.camel.component.http.HttpOperationFailedException.class)
									.handled(false)
                            .maximumRedeliveries(fdpRSConfigDTO.getNumberOfRetry()).redeliveryDelay(fdpRSConfigDTO.getRetryInterval())
							.onRedelivery(logRetryProcessor)
							 .to(BusinessConstants.COMPONENT_EXCEPTION_ENDPOINT).end()
							.to(getRSEndpointURLForCircle(fdpRSConfigDTO));
				}
			}

			private void removeLastNodeRoute(FDPRSConfigDTO fdpRSConfigDTO, final String circleCode,
					final CdiCamelContext cdiCamelContext) throws Exception {
				List<FDPRSConfigDTO> configDTOs = new ArrayList<FDPRSConfigDTO>();
				configDTOs.add(fdpRSConfigDTO);
				final String[] endpoints = getCircleRSEndPointsForLB(configDTOs, circleCode);
				for (final String endpoint : endpoints) {
					cdiCamelContext.stopRoute(endpoint);
					cdiCamelContext.removeRoute(endpoint);
				}
				for (FDPRSConfigDTO fdpRsConfigDTO : configDTOs) {

					String routeIdToBeRemove = HttpUtil.getRSSubRouteId(fdpRsConfigDTO, circleCode);
					cdiCamelContext.stopRoute(routeIdToBeRemove);
					cdiCamelContext.removeRoute(routeIdToBeRemove);
				}
			}

		};
	}
	
	
	private RouteBuilder createADCRoutes(final FDPADCConfigDTO fdpADCConfigDTO) {
		return new RouteBuilder() {

			@SuppressWarnings("unchecked")
			@Override
			public void configure() throws Exception {

				final String circleCode = fdpADCConfigDTO.getCircleDTO().getCircleCode();
				final List<FDPADCConfigDTO> fdpADCConfigDTOList = getEndpointURLsForADCByCircleCode(circleCode);
				final boolean autostartup = true;
				final CdiCamelContext cdiCamelContext = cdiCamelContextProvider.getContext();
				final String routeIdForLB = ExternalSystem.ADC.name() + BusinessConstants.UNDERSCORE + circleCode;
				cdiCamelContext.stopRoute(routeIdForLB);
				cdiCamelContext.removeRoute(routeIdForLB);
				removeLastNodeRoute(fdpADCConfigDTO, circleCode, cdiCamelContext);
				final String[] endpoints = getCircleADCEndPointsForLB(fdpADCConfigDTOList, circleCode);
				for (final String endpoint : endpoints) {
					cdiCamelContext.stopRoute(endpoint);
					cdiCamelContext.removeRoute(endpoint);
				}
				for (FDPADCConfigDTO fdpADCConfigDTO : fdpADCConfigDTOList) {

					String routeIdToBeRemove = HttpUtil.getADCSubRouteId(fdpADCConfigDTO, circleCode);
					cdiCamelContext.stopRoute(routeIdToBeRemove);
					cdiCamelContext.removeRoute(routeIdToBeRemove);
				}
				int fdpADCConfigDTOListSize = fdpADCConfigDTOList.size();
				LoadBalanceDefinition loadBalanceDefinition = null;
				if (fdpADCConfigDTOList != null && fdpADCConfigDTOListSize > 1) {

					loadBalanceDefinition = from(BusinessConstants.HTTP_COMPONENT_ADC_ENDPOINT + "_" + circleCode)
							.routeId(ExternalSystem.ADC.name() + BusinessConstants.UNDERSCORE + circleCode)
							.setExchangePattern(ExchangePattern.InOut).loadBalance()
							.failover(fdpADCConfigDTOListSize - 1, false, true, java.lang.Exception.class);
					for (final String endpoint : endpoints) {

						loadBalanceDefinition.routeId(endpoint).to(endpoint);
					}
				} else {
					if (fdpADCConfigDTOList != null && fdpADCConfigDTOListSize == 1) {
						from(BusinessConstants.HTTP_COMPONENT_ADC_ENDPOINT + BusinessConstants.UNDERSCORE + circleCode)
								.setExchangePattern(ExchangePattern.InOut)
								.routeId(ExternalSystem.ADC.name() + BusinessConstants.UNDERSCORE + circleCode)
								.to(endpoints);
					}
				}
				for (int j = 0; fdpADCConfigDTOList != null && j < fdpADCConfigDTOListSize; j++) {

					final FDPADCConfigDTO fdpADCConfigDTO = fdpADCConfigDTOList.get(j);
					final List<String> routeIdList = new ArrayList<String>();
					/**
					 * Manages the re-delivery attempt and re-delivery delay.
					 */
					final String circleCodeEndpoint = circleCode + fdpADCConfigDTO.getExternalSystemId()
							+ BusinessConstants.UNDERSCORE + fdpADCConfigDTO.getLogicalName();
					final String ipAddress = fdpADCConfigDTO.getIpAddress().getValue();
					final String circleCodeIpaddressPort = circleCode + BusinessConstants.COLON + ipAddress
							+ BusinessConstants.COLON + String.valueOf(fdpADCConfigDTO.getPort());
					final String routeId2 = HttpUtil.getADCSubRouteId(fdpADCConfigDTO, circleCode);
					routeIdList.add(routeId2);
					from(BusinessConstants.HTTP_COMPONENT_ADC_ENDPOINT + circleCodeEndpoint)
					.autoStartup(autostartup)
							.routeId(routeId2)
							.onCompletion().onCompleteOnly().process(completionProcessor).end()
							.setProperty(BusinessConstants.OUTGOING_IP_ADDRESS, constant(ipAddress))
							.setProperty(BusinessConstants.OUTGOING_CIRCLE_CODE_IP_PORT,
									constant(circleCodeIpaddressPort))
							.process(logOutgoingIpProcessor)
							.process(headerProcessor)
							.onException(java.lang.Exception.class, java.net.ConnectException.class,
									java.net.SocketTimeoutException.class,
									org.apache.camel.component.http.HttpOperationFailedException.class)
									.handled(false)
                            .maximumRedeliveries(fdpADCConfigDTO.getNumberOfRetry()).redeliveryDelay(fdpADCConfigDTO.getRetryInterval())
							.onRedelivery(logRetryProcessor)
							 .to(BusinessConstants.COMPONENT_EXCEPTION_ENDPOINT).end()
							.to(getADCEndpointURLForCircle(fdpADCConfigDTO));
				}
			}

			private void removeLastNodeRoute(FDPADCConfigDTO fdpADCConfigDTO, final String circleCode,
					final CdiCamelContext cdiCamelContext) throws Exception {
				List<FDPADCConfigDTO> configDTOs = new ArrayList<FDPADCConfigDTO>();
				configDTOs.add(fdpADCConfigDTO);
				final String[] endpoints = getCircleADCEndPointsForLB(configDTOs, circleCode);
				for (final String endpoint : endpoints) {
					cdiCamelContext.stopRoute(endpoint);
					cdiCamelContext.removeRoute(endpoint);
				}
				for (FDPADCConfigDTO fdpAdcConfigDTO : configDTOs) {

					String routeIdToBeRemove = HttpUtil.getADCSubRouteId(fdpAdcConfigDTO, circleCode);
					cdiCamelContext.stopRoute(routeIdToBeRemove);
					cdiCamelContext.removeRoute(routeIdToBeRemove);
				}
			}

		};
	}

	/**
	 * Gets the aIR endpoint url for circle.
	 * 
	 * @param fdpAIRConfigDTO
	 *            the fdp air config dto
	 * @return the aIR endpoint url for circle
	 */
	private String getAIREndpointURLForCircle(final FDPAIRConfigDTO fdpAIRConfigDTO) {
		final String airUrl = BusinessConstants.HTTP_COMPONENT_TYPE + fdpAIRConfigDTO.getIpAddress().getValue() + BusinessConstants.COLON
                + fdpAIRConfigDTO.getPort() + BusinessConstants.QUERY_STRING_SEPARATOR + BusinessConstants.HTTP_CLIENT_SO_TIMEOUT
                + BusinessConstants.EQUALS + fdpAIRConfigDTO.getResponseTimeout();
		LOGGER.debug("Air Url:" + airUrl);
		return airUrl;
	}
	
	
	private String getDMCEndpointURLForCircle(final FDPDMCConfigDTO fdpDMCConfigDTO) {
		final String dmcUrl = BusinessConstants.HTTP_COMPONENT_TYPE + fdpDMCConfigDTO.getIpAddress().getValue()
				+ BusinessConstants.COLON + fdpDMCConfigDTO.getPort() + BusinessConstants.FORWARD_SLASH;
		LOGGER.debug("Dmc Url:" + dmcUrl);
		return dmcUrl;
	}
	
	/**
	 * Gets the CMS endpoint url for circle.
	 * 
	 * @param fdpCMSConfigDTO
	 *            the fdp cms config dto
	 * @return the CMS endpoint url for circle
	 */
	private String getCMSEndpointURLForCircle(final FDPCMSConfigDTO fdpCMSConfigDTO) {
		final String CMSUrl = BusinessConstants.HTTP_COMPONENT_TYPE + fdpCMSConfigDTO.getIpAddress().getValue()
				+ BusinessConstants.COLON + fdpCMSConfigDTO.getPort() + BusinessConstants.FORWARD_SLASH;
		LOGGER.debug("CMS Url:" + CMSUrl);
		return CMSUrl;
	}

	/**
	 * Gets the cGW endpoint url for circle.
	 * 
	 * @param fdpCGWConfigDTO
	 *            the fdp cgw config dto
	 * @return the cGW endpoint url for circle
	 */
	private String getCGWEndpointURLForCircle(final FDPCGWConfigDTO fdpCGWConfigDTO) {
		final String cgwUrl = BusinessConstants.HTTP_COMPONENT_TYPE + fdpCGWConfigDTO.getIpAddress().getValue()
				+ BusinessConstants.COLON + fdpCGWConfigDTO.getPort() + BusinessConstants.FORWARD_SLASH;
		LOGGER.debug("CGW Url:" + cgwUrl);
		return cgwUrl;
	}

	/**
	 * Gets the rS endpoint url for circle.
	 * 
	 * @param fdpRSConfigDTO
	 *            the fdp rs config dto
	 * @return the rS endpoint url for circle
	 */
	private String getRSEndpointURLForCircle(final FDPRSConfigDTO fdpRSConfigDTO) {
		final String rsUrl = BusinessConstants.HTTP_COMPONENT_TYPE + fdpRSConfigDTO.getIpAddress().getValue()
				 + BusinessConstants.COLON + fdpRSConfigDTO.getPort() + BusinessConstants.QUERY_STRING_SEPARATOR + BusinessConstants.HTTP_CLIENT_SO_TIMEOUT
	             + BusinessConstants.EQUALS + fdpRSConfigDTO.getResponseTimeout();
		LOGGER.debug("RS Url:" + rsUrl);
		return rsUrl;
	}
	
	private String getADCEndpointURLForCircle(final FDPADCConfigDTO fdpADCConfigDTO) {
		final String adcUrl = BusinessConstants.HTTP_COMPONENT_TYPE + fdpADCConfigDTO.getIpAddress().getValue()
				 + BusinessConstants.COLON + fdpADCConfigDTO.getPort() + BusinessConstants.QUERY_STRING_SEPARATOR + BusinessConstants.HTTP_CLIENT_SO_TIMEOUT
	             + BusinessConstants.EQUALS + fdpADCConfigDTO.getResponseTimeout();
		LOGGER.debug("ADC Url:" + adcUrl);
		return adcUrl;
	}

	/**
	 * Gets the endpoint ur ls for air by circle code.
	 * 
	 * @param circleCode
	 *            the circle code
	 * @return the endpoint ur ls for air by circle code
	 */
	private List<FDPAIRConfigDTO> getEndpointURLsForAIRByCircleCode(final String circleCode) {
		return fdpairConfigDAO.getAIREndpointByCircleCode(circleCode);
	}
	
	private List<FDPDMCConfigDTO> getEndpointURLsForDMCByCircleCode(final String circleCode) {
		return fdpdmcConfigDAO.getDMCEndpointByCircleCode(circleCode);
	}
	
	private List<FDPAbilityConfigDTO> getEndpointURLsForAbilityByCircleCode(final String circleCode) {
	    return fdpAbilityConfigDAO.getAbilityEndpointByCircleCode(circleCode);
    }
	
	/**
	 * Gets the endpoint ur ls for CMS by circle code.
	 * 
	 * @param circleCode
	 *            the circle code
	 * @return the endpoint ur ls for CMS by circle code
	 */
	private List<FDPCMSConfigDTO> getEndpointURLsForCMSByCircleCode(final String circleCode) {
		return fdpcmsConfigDAO.getCMSEndpointByCircleCode(circleCode);
	}


	/**
	 * Gets the endpoint ur ls for cgw by circle code.
	 * 
	 * @param circleCode
	 *            the circle code
	 * @return the endpoint ur ls for cgw by circle code
	 */
	private List<FDPCGWConfigDTO> getEndpointURLsForCGWByCircleCode(final String circleCode) {
		return fdpCGWConfigDAO.getCGWEndpointByCircleCode(circleCode);
	}

	/**
	 * Gets the endpoint ur ls for rs by circle code.
	 * 
	 * @param circleCode
	 *            the circle code
	 * @return the endpoint ur ls for rs by circle code
	 */
	private List<FDPRSConfigDTO> getEndpointURLsForRSByCircleCode(final String circleCode) {
		return fdpRsConfigDAO.getRSEndpointByCircleCode(circleCode);
	}
	
	private List<FDPADCConfigDTO> getEndpointURLsForADCByCircleCode(final String circleCode) {
		return fdpADCConfigDAO.getADCEndpointByCircleCode(circleCode);
	}

	/**
	 * Gets the SMSC end points TRx(Transceiver).
	 * 
	 * @param serverName
	 *            the server name
	 * @param fdpsmscConfigDTO
	 *            the fdpsmsc config dto
	 * @return the sMSC end points Rx.
	 */
	private String getSMSCEndPointsTRx(final String serverName, final FDPSMSCConfigDTO fdpsmscConfigDTO) {

		String smscUrl = null;
		if (fdpsmscConfigDTO.getBindNode().trim().equals(BusinessConstants.BIND_NODE_TYPE_TRANSCEIVER)) {
			smscUrl = BusinessConstants.SMPP_COMPONENT + fdpsmscConfigDTO.getBindSystemId().trim()
					+ BusinessConstants.AT_THE_RATE + fdpsmscConfigDTO.getIp() + BusinessConstants.COLON
					+ fdpsmscConfigDTO.getPort() + BusinessConstants.QUERY_STRING_SEPARATOR
					+ BusinessConstants.PASSWORD + BusinessConstants.EQUALS + fdpsmscConfigDTO.getBindSystemPassword()
					+ BusinessConstants.AMPERSAND_PARAMETER_APPENDER + BusinessConstants.ENQUIRE_LINK_TIMER
					+ BusinessConstants.EQUALS
					+ fdpsmscConfigDTO.getLinkCheckPeriod()
					+ BusinessConstants.AMPERSAND_PARAMETER_APPENDER
					+ BusinessConstants.TRANSACTION_TIMER
					+ BusinessConstants.EQUALS
					+ fdpsmscConfigDTO.getResponseTimeout()
					+ BusinessConstants.AMPERSAND_PARAMETER_APPENDER
					+ BusinessConstants.SYSTEM_TYPE
					+ BusinessConstants.EQUALS
					+ "consumer"// smscDetail.getBindNode()
					+ BusinessConstants.AMPERSAND_PARAMETER_APPENDER + BusinessConstants.SERVICE_TYPE
					+ BusinessConstants.EQUALS + fdpsmscConfigDTO.getServiceType()
					+ BusinessConstants.AMPERSAND_PARAMETER_APPENDER + BusinessConstants.SOURCE_ADDRESS_TON
					+ BusinessConstants.EQUALS + fdpsmscConfigDTO.getBindTON()
					+ BusinessConstants.AMPERSAND_PARAMETER_APPENDER + BusinessConstants.SOURCE_ADD_NPI
					+ BusinessConstants.EQUALS + fdpsmscConfigDTO.getBindNPI() + "&bindType=9";
			LOGGER.debug("SMSC Url : Transciver :" + smscUrl);
		}

		return smscUrl;
	}

	/**
	 * Gets the SMSC end points Rx(consumer).
	 * 
	 * @param fdpsmscConfigDTO
	 *            the fdpsmsc config dto
	 * @param serverName
	 *            the server name
	 * @return the sMSC end points Rx.
	 */
	private String getSMSCEndPointsRx(final FDPSMSCConfigDTO fdpsmscConfigDTO, final String serverName) {

		String smscUrl = null;
		if (fdpsmscConfigDTO.getBindNode().trim().equals(BusinessConstants.BIND_NODE_TYPE_RECEIVER)) {

			smscUrl = BusinessConstants.SMPP_COMPONENT + fdpsmscConfigDTO.getBindSystemId().trim()
					+ BusinessConstants.AT_THE_RATE + fdpsmscConfigDTO.getIp() + BusinessConstants.COLON
					+ fdpsmscConfigDTO.getPort() + BusinessConstants.QUERY_STRING_SEPARATOR
					+ BusinessConstants.PASSWORD + BusinessConstants.EQUALS + fdpsmscConfigDTO.getBindSystemPassword()
					+ BusinessConstants.AMPERSAND_PARAMETER_APPENDER + BusinessConstants.ENQUIRE_LINK_TIMER
					+ BusinessConstants.EQUALS
					+ fdpsmscConfigDTO.getLinkCheckPeriod()
					+ BusinessConstants.AMPERSAND_PARAMETER_APPENDER
					+ BusinessConstants.TRANSACTION_TIMER
					+ BusinessConstants.EQUALS
					+ fdpsmscConfigDTO.getResponseTimeout()
					+ BusinessConstants.AMPERSAND_PARAMETER_APPENDER
					+ BusinessConstants.SYSTEM_TYPE
					+ BusinessConstants.EQUALS
					+ "consumer"// smscDetail.getBindNode()
					+ BusinessConstants.AMPERSAND_PARAMETER_APPENDER + BusinessConstants.SERVICE_TYPE
					+ BusinessConstants.EQUALS + fdpsmscConfigDTO.getServiceType()
					+ BusinessConstants.AMPERSAND_PARAMETER_APPENDER + BusinessConstants.SOURCE_ADDRESS_TON
					+ BusinessConstants.EQUALS + fdpsmscConfigDTO.getBindTON()
					+ BusinessConstants.AMPERSAND_PARAMETER_APPENDER + BusinessConstants.SOURCE_ADD_NPI
					+ BusinessConstants.EQUALS + fdpsmscConfigDTO.getBindNPI() + "&bindType=1";

			LOGGER.debug("SMSC Url : consumer :" + smscUrl);
		}

		return smscUrl;
	}

	/**
	 * Returns List of SMS SMSCs Urls Gets the SMSC end points Tx.
	 * 
	 * @param fdpsmscConfigDTO
	 *            the fdpsmsc config dto
	 * @return the sMSC end points Tx.
	 */
	private String getSMSCEndPointsTx(final FDPSMSCConfigDTO fdpsmscConfigDTO) {
		final int registeredDelivery = Integer
				.valueOf(PropertyUtils
						.getProperty("smsc.route.registeredDelivery"));
		final String smscUrl = BusinessConstants.SMPP_COMPONENT
				+ fdpsmscConfigDTO.getBindSystemId()// .trim()
				+ BusinessConstants.AT_THE_RATE + fdpsmscConfigDTO.getIp() + BusinessConstants.COLON
				+ fdpsmscConfigDTO.getPort() + BusinessConstants.QUERY_STRING_SEPARATOR + BusinessConstants.PASSWORD
				+ BusinessConstants.EQUALS + fdpsmscConfigDTO.getBindSystemPassword()
				+ BusinessConstants.AMPERSAND_PARAMETER_APPENDER + BusinessConstants.ENQUIRE_LINK_TIMER
				+ BusinessConstants.EQUALS
				+ fdpsmscConfigDTO.getLinkCheckPeriod()
				+ BusinessConstants.AMPERSAND_PARAMETER_APPENDER
				+ BusinessConstants.TRANSACTION_TIMER
				+ BusinessConstants.EQUALS
				+ fdpsmscConfigDTO.getResponseTimeout()
				+ BusinessConstants.AMPERSAND_PARAMETER_APPENDER
				+ BusinessConstants.SYSTEM_TYPE
				+ BusinessConstants.EQUALS
				+ "consumer"// smscDetail.getBindNode()
				+ BusinessConstants.AMPERSAND_PARAMETER_APPENDER + BusinessConstants.SERVICE_TYPE
				+ BusinessConstants.EQUALS + fdpsmscConfigDTO.getServiceType()
				+ BusinessConstants.AMPERSAND_PARAMETER_APPENDER + BusinessConstants.SOURCE_ADDRESS_TON
				+ BusinessConstants.EQUALS + fdpsmscConfigDTO.getBindTON()
				+ BusinessConstants.AMPERSAND_PARAMETER_APPENDER + BusinessConstants.SOURCE_ADD_NPI
				+ BusinessConstants.EQUALS + fdpsmscConfigDTO.getBindNPI() + "&bindType=2"
				+ BusinessConstants.AMPERSAND_PARAMETER_APPENDER
				+ BusinessConstants.REGISTERED_DELIVERY
				+ BusinessConstants.EQUALS + registeredDelivery;;
		LOGGER.debug("SMSC Url : producer :" + smscUrl);
		return smscUrl;
	}

	@Override
	public boolean startRoute(final FDPExternalSystemDTO fdpExternalSystemDTO) throws Exception {
		final CdiCamelContext cdiCamelContext = cdiCamelContextProvider.getContext();
		boolean status = false;
		if (fdpExternalSystemDTO instanceof FDPAIRConfigDTO) {
			final FDPAIRConfigDTO fdpAirConfigDTO = (FDPAIRConfigDTO) fdpExternalSystemDTO;
			final String circleCode = fdpAirConfigDTO.getCircleDTO().getCircleCode();
			final String key = RouteUtil.prepareExternalSystemAppBagKey(circleCode, fdpAirConfigDTO.getIpAddress()
					.getValue(), fdpAirConfigDTO.getPort(), null, null);
			final FDPAppBag appBag = new FDPAppBag();
			appBag.setSubStore(AppCacheSubStore.AIRCONFEGURATION_MAP);
			appBag.setKey(key);
			final FDPAIRConfigDTO externalSystemDTO = (FDPAIRConfigDTO) applicationConfigCache.getValue(appBag);
			final List<String> routeIdList = externalSystemDTO.getRouteId();
			for (final String routeId : routeIdList) {
				cdiCamelContext.startRoute(routeId);
				LOGGER.info("Route {} has been started.", routeId);
			}
			status = true;
		} 
		else if(fdpExternalSystemDTO instanceof FDPDMCConfigDTO) {
			final FDPDMCConfigDTO fdpDmcConfigDTO = (FDPDMCConfigDTO) fdpExternalSystemDTO;
			final String circleCode = fdpDmcConfigDTO.getCircleDTO().getCircleCode();
			final String key = RouteUtil.prepareExternalSystemAppBagKey(circleCode, fdpDmcConfigDTO.getIpAddress()
					.getValue(), fdpDmcConfigDTO.getPort(), null, null);
			final FDPAppBag appBag = new FDPAppBag();
			appBag.setSubStore(AppCacheSubStore.DMCCONFEGURATION_MAP);
			appBag.setKey(key);
			final FDPDMCConfigDTO externalSystemDTO = (FDPDMCConfigDTO) applicationConfigCache.getValue(appBag);
			final List<String> routeIdList = externalSystemDTO.getRouteId();
			for (final String routeId : routeIdList) {
				cdiCamelContext.startRoute(routeId);
				LOGGER.info("Route {} has been started.", routeId);
			}
			status = true;
		}
		
		else if (fdpExternalSystemDTO instanceof FDPCMSConfigDTO) {
			final FDPCMSConfigDTO fdpCmsConfigDTO = (FDPCMSConfigDTO) fdpExternalSystemDTO;
			final String circleCode = fdpCmsConfigDTO.getCircleDTO().getCircleCode();
			final String key = RouteUtil.prepareExternalSystemAppBagKey(circleCode, fdpCmsConfigDTO.getIpAddress()
					.getValue(), fdpCmsConfigDTO.getPort(), null, null);
			final FDPAppBag appBag = new FDPAppBag();
			appBag.setSubStore(AppCacheSubStore.CMSCONFEGURATION_MAP);
			appBag.setKey(key);
			final FDPCMSConfigDTO externalSystemDTO = (FDPCMSConfigDTO) applicationConfigCache.getValue(appBag);
			final List<String> routeIdList = externalSystemDTO.getRouteId();
			for (final String routeId : routeIdList) {
				cdiCamelContext.startRoute(routeId);
				LOGGER.info("Route {} has been started.", routeId);
			}
			status = true;
		}else if (fdpExternalSystemDTO instanceof FDPCGWConfigDTO) {
			final FDPCGWConfigDTO fdpCgwConfigDTO = (FDPCGWConfigDTO) fdpExternalSystemDTO;
			final String circleCode = fdpCgwConfigDTO.getCircleDTO().getCircleCode();
			final String key = RouteUtil.prepareExternalSystemAppBagKey(circleCode, fdpCgwConfigDTO.getIpAddress()
					.getValue(), fdpCgwConfigDTO.getPort(), null, null);
			final FDPAppBag appBag = new FDPAppBag();
			appBag.setSubStore(AppCacheSubStore.CGWCONFEGURATION_MAP);
			appBag.setKey(key);
			final FDPCGWConfigDTO externalSystemDTO = (FDPCGWConfigDTO) applicationConfigCache.getValue(appBag);
			final List<String> routeIdList = externalSystemDTO.getRouteId();
			for (final String routeId : routeIdList) {
				cdiCamelContext.startRoute(routeId);
				LOGGER.info("Route {} has been started.", routeId);
			}
			status = true;

		} else if (fdpExternalSystemDTO instanceof FDPRSConfigDTO) {

			final FDPRSConfigDTO fdpRSConfigDTO = (FDPRSConfigDTO) fdpExternalSystemDTO;
			final String circleCode = fdpRSConfigDTO.getCircleDTO().getCircleCode();
			final String key = RouteUtil.prepareExternalSystemAppBagKey(circleCode, fdpRSConfigDTO.getIpAddress()
					.getValue(), fdpRSConfigDTO.getPort(), null, null);
			final FDPAppBag appBag = new FDPAppBag();
			appBag.setSubStore(AppCacheSubStore.RSCONFEGURATION_MAP);
			appBag.setKey(key);
			final FDPRSConfigDTO externalSystemDTO = (FDPRSConfigDTO) applicationConfigCache.getValue(appBag);
			final List<String> routeIdList = externalSystemDTO.getRouteId();
			for (final String routeId : routeIdList) {
				cdiCamelContext.startRoute(routeId);
				LOGGER.info("Route {} has been started.", routeId);
			}
			status = true;
		} else if (fdpExternalSystemDTO instanceof FDPSMSCConfigDTO) {

			final FDPSMSCConfigDTO fdpsmscConfigDTO = (FDPSMSCConfigDTO) fdpExternalSystemDTO;
			final String key = RouteUtil.prepareExternalSystemAppBagKey(ApplicationUtil.getServerName(),
					fdpsmscConfigDTO.getIp(), fdpsmscConfigDTO.getPort(), fdpsmscConfigDTO.getBindSystemId(),
					fdpsmscConfigDTO.getBindNode());
			final FDPAppBag appBag = new FDPAppBag();
			appBag.setSubStore(AppCacheSubStore.SMS_USSD_ROUTE_DETAIL);
			appBag.setKey(key);
			final FDPSMSCConfigDTO externalSystemDTO = (FDPSMSCConfigDTO) applicationConfigCache.getValue(appBag);
			final List<String> routeIdList = externalSystemDTO.getRouteId();
			for (final String routeId : routeIdList) {
				cdiCamelContext.startRoute(routeId);
				LOGGER.info("Route {} has been started.", routeId);
			}
			status = true;
		} else if (fdpExternalSystemDTO instanceof FDPEMAConfigDTO) {
			final FDPEMAConfigDTO fdpemaConfigDTO = (FDPEMAConfigDTO) fdpExternalSystemDTO;
			final String circleCode = fdpemaConfigDTO.getCircleDTO().getCircleCode();
			final String routeId = circleCode + BusinessConstants.UNDERSCORE + fdpemaConfigDTO.getInterfaceType()
					+ BusinessConstants.UNDERSCORE + fdpemaConfigDTO.getProtocolType() + BusinessConstants.UNDERSCORE
					+ fdpemaConfigDTO.getLogicalName();
			cdiCamelContext.startRoute(routeId);
			LOGGER.debug("Route {} has been started for EMA circle {}.", routeId, circleCode);
		} else if (fdpExternalSystemDTO instanceof FDPVASConfigDTO) {
			final FDPVASConfigDTO fdpMCarbonConfigDTO = (FDPVASConfigDTO) fdpExternalSystemDTO;
			final String routeId = HttpUtil.getRouteIdForMCarbonHttpRoutes(fdpMCarbonConfigDTO.getCircleDTO()
					.getCircleCode(), ExternalSystemType.MCARBON_TYPE, fdpMCarbonConfigDTO.getLogicalName());
			cdiCamelContext.startRoute(routeId);
			LOGGER.debug("Route {} has been started for MCarbon circle {}.", routeId, fdpMCarbonConfigDTO
					.getCircleDTO().getCircleCode());
		}else if (fdpExternalSystemDTO instanceof FDPADCConfigDTO) {

			final FDPADCConfigDTO fdpADCConfigDTO = (FDPADCConfigDTO) fdpExternalSystemDTO;
			final String circleCode = fdpADCConfigDTO.getCircleDTO().getCircleCode();
			final String key = RouteUtil.prepareExternalSystemAppBagKey(circleCode, fdpADCConfigDTO.getIpAddress()
					.getValue(), fdpADCConfigDTO.getPort(), null, null);
			final FDPAppBag appBag = new FDPAppBag();
			appBag.setSubStore(AppCacheSubStore.ADCCONFEGURATION_MAP);
			appBag.setKey(key);
			final FDPRSConfigDTO externalSystemDTO = (FDPRSConfigDTO) applicationConfigCache.getValue(appBag);
			final List<String> routeIdList = externalSystemDTO.getRouteId();
			for (final String routeId : routeIdList) {
				cdiCamelContext.startRoute(routeId);
				LOGGER.info("Route {} has been started.", routeId);
			}
			status = true;
		} 
		return status;
	}

	@Override
	public boolean stopRoute(final FDPExternalSystemDTO fdpExternalSystemDTO) throws Exception {
		boolean status = false;
		final CdiCamelContext cdiCamelContext = cdiCamelContextProvider.getContext();

		if (fdpExternalSystemDTO instanceof FDPAIRConfigDTO) {
			final FDPAIRConfigDTO fdpAirConfigDTO = (FDPAIRConfigDTO) fdpExternalSystemDTO;
			final String circleCode = fdpAirConfigDTO.getCircleDTO().getCircleCode();
			final String key = RouteUtil.prepareExternalSystemAppBagKey(circleCode, fdpAirConfigDTO.getIpAddress()
					.getValue(), fdpAirConfigDTO.getPort(), null, null);
			final FDPAppBag appBag = new FDPAppBag();
			appBag.setSubStore(AppCacheSubStore.AIRCONFEGURATION_MAP);
			appBag.setKey(key);
			final FDPAIRConfigDTO externalSystemDTO = (FDPAIRConfigDTO) applicationConfigCache.getValue(appBag);
			final List<String> routeIdList = externalSystemDTO.getRouteId();
			for (final String routeId : routeIdList) {
				cdiCamelContext.stopRoute(routeId);
				LOGGER.info("Route {} has been stopped.", routeId);
			}
			status = true;

		} 
		
		else if (fdpExternalSystemDTO instanceof FDPDMCConfigDTO) {
			final FDPDMCConfigDTO fdpDmcConfigDTO = (FDPDMCConfigDTO) fdpExternalSystemDTO;
			final String circleCode = fdpDmcConfigDTO.getCircleDTO().getCircleCode();
			final String key = RouteUtil.prepareExternalSystemAppBagKey(circleCode, fdpDmcConfigDTO.getIpAddress()
					.getValue(), fdpDmcConfigDTO.getPort(), null, null);
			final FDPAppBag appBag = new FDPAppBag();
			appBag.setSubStore(AppCacheSubStore.DMCCONFEGURATION_MAP);
			appBag.setKey(key);
			final FDPDMCConfigDTO externalSystemDTO = (FDPDMCConfigDTO) applicationConfigCache.getValue(appBag);
			final List<String> routeIdList = externalSystemDTO.getRouteId();
			for (final String routeId : routeIdList) {
				cdiCamelContext.stopRoute(routeId);
				LOGGER.info("Route {} has been stopped.", routeId);
			}
			status = true;

		} 
		else if (fdpExternalSystemDTO instanceof FDPCMSConfigDTO) {
			final FDPCMSConfigDTO fdpCmsConfigDTO = (FDPCMSConfigDTO) fdpExternalSystemDTO;
			final String circleCode = fdpCmsConfigDTO.getCircleDTO().getCircleCode();
			final String key = RouteUtil.prepareExternalSystemAppBagKey(circleCode, fdpCmsConfigDTO.getIpAddress()
					.getValue(), fdpCmsConfigDTO.getPort(), null, null);
			final FDPAppBag appBag = new FDPAppBag();
			appBag.setSubStore(AppCacheSubStore.CMSCONFEGURATION_MAP);
			appBag.setKey(key);
			final FDPCMSConfigDTO externalSystemDTO = (FDPCMSConfigDTO) applicationConfigCache.getValue(appBag);
			final List<String> routeIdList = externalSystemDTO.getRouteId();
			for (final String routeId : routeIdList) {
				cdiCamelContext.stopRoute(routeId);
				LOGGER.info("Route {} has been stopped.", routeId);
			}
			status = true;

		}
		else if (fdpExternalSystemDTO instanceof FDPCGWConfigDTO) {

			final FDPCGWConfigDTO fdpCgwConfigDTO = (FDPCGWConfigDTO) fdpExternalSystemDTO;
			final String circleCode = fdpCgwConfigDTO.getCircleDTO().getCircleCode();
			final String key = RouteUtil.prepareExternalSystemAppBagKey(circleCode, fdpCgwConfigDTO.getIpAddress()
					.getValue(), fdpCgwConfigDTO.getPort(), null, null);
			final FDPAppBag appBag = new FDPAppBag();
			appBag.setSubStore(AppCacheSubStore.CGWCONFEGURATION_MAP);
			appBag.setKey(key);
			final FDPCGWConfigDTO externalSystemDTO = (FDPCGWConfigDTO) applicationConfigCache.getValue(appBag);
			final List<String> routeIdList = externalSystemDTO.getRouteId();
			for (final String routeId : routeIdList) {
				cdiCamelContext.stopRoute(routeId);
				LOGGER.info("Route {} has been stopped.", routeId);
			}
			status = true;
		} else if (fdpExternalSystemDTO instanceof FDPRSConfigDTO) {

			final FDPRSConfigDTO fdpRSConfigDTO = (FDPRSConfigDTO) fdpExternalSystemDTO;
			final String circleCode = fdpRSConfigDTO.getCircleDTO().getCircleCode();
			final String key = RouteUtil.prepareExternalSystemAppBagKey(circleCode, fdpRSConfigDTO.getIpAddress()
					.getValue(), fdpRSConfigDTO.getPort(), null, null);
			final FDPAppBag appBag = new FDPAppBag();
			appBag.setSubStore(AppCacheSubStore.RSCONFEGURATION_MAP);
			appBag.setKey(key);
			final FDPRSConfigDTO externalSystemDTO = (FDPRSConfigDTO) applicationConfigCache.getValue(appBag);
			final List<String> routeIdList = externalSystemDTO.getRouteId();
			for (final String routeId : routeIdList) {
				cdiCamelContext.stopRoute(routeId);
				LOGGER.info("Route {} has been stopped.", routeId);
			}
			status = true;
		} else if (fdpExternalSystemDTO instanceof FDPSMSCConfigDTO) {

			final FDPSMSCConfigDTO fdpsmscConfigDTO = (FDPSMSCConfigDTO) fdpExternalSystemDTO;
			final String key = RouteUtil.prepareExternalSystemAppBagKey(ApplicationUtil.getServerName(),
					fdpsmscConfigDTO.getIp(), fdpsmscConfigDTO.getPort(), fdpsmscConfigDTO.getBindSystemId(),
					fdpsmscConfigDTO.getBindNode());
			final FDPAppBag appBag = new FDPAppBag();
			appBag.setSubStore(AppCacheSubStore.SMS_USSD_ROUTE_DETAIL);
			appBag.setKey(key);
			final FDPSMSCConfigDTO externalSystemDTO = (FDPSMSCConfigDTO) applicationConfigCache.getValue(appBag);
			if (externalSystemDTO != null) {
				final List<String> routeIdList = externalSystemDTO.getRouteId();
				if (routeIdList != null) {
					for (final String routeId : routeIdList) {
						cdiCamelContext.stopRoute(routeId);
						LOGGER.info("Route {} has been stopped.", routeId);
					}
				}
			}
			status = true;
		} else if (fdpExternalSystemDTO instanceof FDPEMAConfigDTO) {
			final FDPEMAConfigDTO fdpemaConfigDTO = (FDPEMAConfigDTO) fdpExternalSystemDTO;
			final String circleCode = fdpemaConfigDTO.getCircleDTO().getCircleCode();
			final String routeId = circleCode + BusinessConstants.UNDERSCORE + fdpemaConfigDTO.getInterfaceType()
					+ BusinessConstants.UNDERSCORE + fdpemaConfigDTO.getProtocolType() + BusinessConstants.UNDERSCORE
					+ fdpemaConfigDTO.getLogicalName();
			cdiCamelContext.stopRoute(routeId);
			LOGGER.debug("Route {} has been stopped for EMA circle {}.", routeId, circleCode);
		} else if (fdpExternalSystemDTO instanceof FDPVASConfigDTO) {
			final FDPVASConfigDTO fdpVASConfigDTO = (FDPVASConfigDTO) fdpExternalSystemDTO;
			String routeId = null;
			if (fdpVASConfigDTO.getVasType().equalsIgnoreCase(ExternalSystem.MCARBON.name())) {
				routeId = HttpUtil.getRouteIdForMCarbonHttpRoutes(fdpVASConfigDTO.getCircleDTO().getCircleCode(),
						ExternalSystemType.MCARBON_TYPE, fdpVASConfigDTO.getLogicalName());
			}
			if (fdpVASConfigDTO.getVasType().equalsIgnoreCase(ExternalSystem.MANHATTAN.name())) {
				routeId = HttpUtil.getRouteIdForManhattanHttpRoutes(fdpVASConfigDTO.getCircleDTO().getCircleCode(),
						ExternalSystemType.MANHATTAN_TYPE, fdpVASConfigDTO.getLogicalName());
			}
			cdiCamelContext.stopRoute(routeId);
			LOGGER.debug("Route {} has been stopped for {} circle {}.",
					new Object[] { routeId, fdpVASConfigDTO.getVasType(),
							fdpVASConfigDTO.getCircleDTO().getCircleCode() });
		}else if (fdpExternalSystemDTO instanceof FDPADCConfigDTO) {

			final FDPADCConfigDTO fdpADCConfigDTO = (FDPADCConfigDTO) fdpExternalSystemDTO;
			final String circleCode = fdpADCConfigDTO.getCircleDTO().getCircleCode();
			final String key = RouteUtil.prepareExternalSystemAppBagKey(circleCode, fdpADCConfigDTO.getIpAddress()
					.getValue(), fdpADCConfigDTO.getPort(), null, null);
			final FDPAppBag appBag = new FDPAppBag();
			appBag.setSubStore(AppCacheSubStore.ADCCONFEGURATION_MAP);
			appBag.setKey(key);
			final FDPRSConfigDTO externalSystemDTO = (FDPRSConfigDTO) applicationConfigCache.getValue(appBag);
			final List<String> routeIdList = externalSystemDTO.getRouteId();
			for (final String routeId : routeIdList) {
				cdiCamelContext.stopRoute(routeId);
				LOGGER.info("Route {} has been stopped.", routeId);
			}
			status = true;
		} 

		return status;
	}

	@Override
	public boolean removeRoute(final FDPExternalSystemDTO fdpExternalSystemDTO) throws Exception {
		boolean status = false;
		final CdiCamelContext cdiCamelContext = cdiCamelContextProvider.getContext();
		final UpdateCacheDTO updateCacheDTO = new UpdateCacheDTO();
		final FDPCircle circle = new FDPCircle();
		circle.setCircleCode(fdpExternalSystemDTO.getCircleDTO() == null ? null : fdpExternalSystemDTO.getCircleDTO().getCircleCode());
		updateCacheDTO.setCircle(circle);
		if(fdpExternalSystemDTO instanceof FDPAIRConfigDTO) {
			final FDPAIRConfigDTO fdpAirConfigDTO = (FDPAIRConfigDTO) fdpExternalSystemDTO;
			updateCacheDTO.setModuleType(ModuleType.AIR_CONFEGURATION);
			appCacheInitializerService.updateCache(updateCacheDTO);
			cdiCamelContext.addRoutes(createAirRoutes(fdpAirConfigDTO));
			LOGGER.info("Route has been stopped and removed.");
			status = true;
		} 
		else if(fdpExternalSystemDTO instanceof FDPDMCConfigDTO) {
			final FDPDMCConfigDTO fdpDmcConfigDTO = (FDPDMCConfigDTO) fdpExternalSystemDTO;
			updateCacheDTO.setModuleType(ModuleType.DMC_CONFEGURATION);
			appCacheInitializerService.updateCache(updateCacheDTO);
			cdiCamelContext.addRoutes(createDmcRoutes(fdpDmcConfigDTO));
			LOGGER.info("Route has been stopped and removed.");
			status = true;
		}
		else if (fdpExternalSystemDTO instanceof FDPCMSConfigDTO) {
			final FDPCMSConfigDTO fdpCmsConfigDTO = (FDPCMSConfigDTO) fdpExternalSystemDTO;
			updateCacheDTO.setModuleType(ModuleType.CMS_CONFEGURATION);
			appCacheInitializerService.updateCache(updateCacheDTO);
			cdiCamelContext.addRoutes(createCmsRoutes(fdpCmsConfigDTO));
			LOGGER.info("Route has been stopped and removed.");
			status = true;
		} else if (fdpExternalSystemDTO instanceof FDPCGWConfigDTO) {
			final FDPCGWConfigDTO fdpCgwConfigDTO = (FDPCGWConfigDTO) fdpExternalSystemDTO;
			updateCacheDTO.setModuleType(ModuleType.CGW_CONFEGURATION);
			appCacheInitializerService.updateCache(updateCacheDTO);
			cdiCamelContext.addRoutes(createCGWRoutes(fdpCgwConfigDTO));
			LOGGER.info("Route has been stopped and removed.");
			status = true;
		} else if (fdpExternalSystemDTO instanceof FDPRSConfigDTO) {
			final FDPRSConfigDTO fdpRSConfigDTO = (FDPRSConfigDTO) fdpExternalSystemDTO;
			updateCacheDTO.setModuleType(ModuleType.RS_CONFEGURATION);
			appCacheInitializerService.updateCache(updateCacheDTO);
			cdiCamelContext.addRoutes(createRSRoutes(fdpRSConfigDTO));
			LOGGER.info("Route has been stopped and removed.");
			status = true;
		} else if (fdpExternalSystemDTO instanceof FDPSMSCConfigDTO) {
			final FDPSMSCConfigDTO fdpsmscConfigDTO = (FDPSMSCConfigDTO) fdpExternalSystemDTO;
			setUserNamePasswordForSMSC(fdpsmscConfigDTO);
			final String key = RouteUtil.prepareExternalSystemAppBagKey(ApplicationUtil.getServerName(),
					fdpsmscConfigDTO.getIp(), fdpsmscConfigDTO.getPort(), fdpsmscConfigDTO.getBindSystemId(),
					fdpsmscConfigDTO.getBindNode());
			LOGGER.info("SMSC Key: " + key);
			final FDPAppBag appBag = new FDPAppBag();
			appBag.setSubStore(AppCacheSubStore.SMS_USSD_ROUTE_DETAIL);
			appBag.setKey(key);
			final FDPSMSCConfigDTO externalSystemDTO = (FDPSMSCConfigDTO) applicationConfigCache.getValue(appBag);
			if (externalSystemDTO != null) {
				final List<String> routeIdList = externalSystemDTO.getRouteId();
				if (routeIdList != null) {
					for (final String routeId : routeIdList) {
						cdiCamelContext.stopRoute(routeId);
						cdiCamelContext.removeRoute(routeId);
						LOGGER.info("Route {} has been stopped and removed.", routeId);
					}
				}
			}
			if (fdpsmscConfigDTO.getServiceType().equals(BusinessConstants.SERVICE_TYPE_SMS)) {
				updateCacheDTO.setModuleType(ModuleType.SMS_CONFEGURATION);
			} /*
			 * else {
			 * updateCacheDTO.setModuleType(ModuleType.USSD_CONFEGURATION); }
			 */
			appCacheInitializerService.updateCache(updateCacheDTO);
						
			/*if (fdpsmscConfigDTO.getBindNode().equals(BusinessConstants.BIND_NODE_TYPE_TRANSMITTER)) {
			        cdiCamelContext.addRoutes(createTransmitterRoutes(fdpsmscConfigDTO));
			}*/
			
			
			status = true;
		} else if (fdpExternalSystemDTO instanceof FDPEMAConfigDTO) {
			final FDPEMAConfigDTO fdpemaConfigDTO = (FDPEMAConfigDTO) fdpExternalSystemDTO;
			final String circleCode = fdpemaConfigDTO.getCircleDTO().getCircleCode();
			final TelnetClientManager telnetClientManager = TelnetClientManager.getInstance();
			final String routeId = circleCode + BusinessConstants.UNDERSCORE + fdpemaConfigDTO.getInterfaceType()
					+ BusinessConstants.UNDERSCORE + fdpemaConfigDTO.getProtocolType() + BusinessConstants.UNDERSCORE
					+ fdpemaConfigDTO.getLogicalName();
			cdiCamelContext.stopRoute(routeId);
			cdiCamelContext.removeRoute(routeId);
			LOGGER.debug("Route {} has been stopped and Removed for circle {}.", routeId, circleCode);
			updateCacheDTO.setModuleType(ModuleType.EMA_DETAILS);
			appCacheInitializerService.updateCache(updateCacheDTO);
			if (fdpemaConfigDTO.getIpAddress() != null) {
				telnetClientManager.removeTelnetConnection(fdpemaConfigDTO.getIpAddress().getValue());
			}
			status = true;
		} else if (fdpExternalSystemDTO instanceof FDPVASConfigDTO) {
			final FDPVASConfigDTO fdpVASConfigDTO = (FDPVASConfigDTO) fdpExternalSystemDTO;
			if (fdpVASConfigDTO.getVasType().equalsIgnoreCase(ExternalSystem.MCARBON.name())) {
				final String routeId = HttpUtil.getRouteIdForMCarbonHttpRoutes(fdpVASConfigDTO.getCircleDTO()
						.getCircleCode(), ExternalSystemType.MCARBON_TYPE, fdpVASConfigDTO.getLogicalName());
				cdiCamelContext.stopRoute(routeId);
				cdiCamelContext.removeRoute(routeId);
				LOGGER.debug("MCARBON Route {} has been stopped and Removed for circle {}.", routeId, fdpVASConfigDTO
						.getCircleDTO().getCircleCode());
				updateCacheDTO.setModuleType(ModuleType.MCARBON_STORE);
			}
			if (fdpVASConfigDTO.getVasType().equalsIgnoreCase(ExternalSystem.MANHATTAN.name())) {
				final String routeId = HttpUtil.getRouteIdForManhattanHttpRoutes(fdpVASConfigDTO.getCircleDTO()
						.getCircleCode(), ExternalSystemType.MANHATTAN_TYPE, fdpVASConfigDTO.getLogicalName());
				cdiCamelContext.stopRoute(routeId);
				cdiCamelContext.removeRoute(routeId);
				LOGGER.debug("Manhattan Route {} has been stopped and Removed for circle {}.", routeId, fdpVASConfigDTO
						.getCircleDTO().getCircleCode());
				updateCacheDTO.setModuleType(ModuleType.MANHATTAN_STORE);
			}
			appCacheInitializerService.updateCache(updateCacheDTO);
		} else if (fdpExternalSystemDTO instanceof FDPMCLoanConfigDTO) {
			final FDPMCLoanConfigDTO fdpMcLoanDto = (FDPMCLoanConfigDTO) fdpExternalSystemDTO;
			final String circleCode = fdpMcLoanDto.getCircleDTO().getCircleCode();
			
			this.mcLoanhttpRoute.stopLbRouteForMcLoan(circleCode);
			this.mcLoanhttpRoute.stopRouteforMcLoan(fdpMcLoanDto);
			this.mcLoanhttpRoute.stopAllRoutesForMcLoan(circleCode);
			
			this.mcLoanhttpRoute.startAllRoutesForMcLoan(circleCode);
			this.mcLoanhttpRoute.startLbRouteForMcLoan(circleCode);
			
			updateCacheDTO.setModuleType(ModuleType.MCLOAN_STORE);
			appCacheInitializerService.updateCache(updateCacheDTO);
			status = true;
		}else if (fdpExternalSystemDTO instanceof FDPEVDSConfigDTO) {
			final FDPEVDSConfigDTO fdpEVDSConfigDTO = (FDPEVDSConfigDTO) fdpExternalSystemDTO;
			updateCacheDTO.setModuleType(ModuleType.EVDS_CONFIGURATION);
			appCacheInitializerService.updateCache(updateCacheDTO);
			if(evdsType.equalsIgnoreCase((String) FDPConstant.EVDS_TYPE_HTTP)){
				restartEvdsHttpRoutes(fdpEVDSConfigDTO);
			}
			else if(evdsType.equalsIgnoreCase((String) FDPConstant.EVDS_TYPE_HTTP)){
				restartHttpRoutes(fdpEVDSConfigDTO);
			}else{
				restartEvdsRoutes(fdpEVDSConfigDTO);
			}
			LOGGER.info("Route has been stopped and removed.");
			status = true;
		}
		if(fdpExternalSystemDTO instanceof FDPAbilityConfigDTO) {
            final FDPAbilityConfigDTO fdpAbilityConfigDTO = (FDPAbilityConfigDTO) fdpExternalSystemDTO;
            updateCacheDTO.setModuleType(ModuleType.ABILITY_CONFIGURATION);
            appCacheInitializerService.updateCache(updateCacheDTO);
            cdiCamelContext.addRoutes(createAbilityRoutes(fdpAbilityConfigDTO));
            LOGGER.info("Route has been stopped and removed.");
            status = true;
        } 
		return status;
	}

	/**
	 * Gets the sMSC by circle code.
	 * 
	 * @param serverName
	 *            the server name
	 * @param serviceType
	 *            the service type
	 * @return the sMSC by circle code
	 */
	private List<SMPPServerMappingDTO> getSMSCByCircleCode(final String serverName, final ExternalSystemType serviceType) {
		return serverMappingService.getSMSCServerMappings(serviceType, serverName);
	}

	/**
	 * Gets the count for smsc by service type.
	 * 
	 * @param serviceType
	 *            the service type
	 * @param fdpsmscConfigDTOList
	 *            the fdpsmsc config dto list
	 * @param bindNode
	 *            the bind node
	 * @return the count for smsc by service type
	 */
	private int getCountForSmscByServiceType(final String serviceType,
			final List<SMPPServerMappingDTO> fdpsmscConfigDTOList, final String bindNode) {
		int count = 0;
		for (final SMPPServerMappingDTO fdpsmscConfigDTO : fdpsmscConfigDTOList) {
			if (serviceType.equals(fdpsmscConfigDTO.getServiceType())
					&& bindNode.equals(fdpsmscConfigDTO.getBindNode())) {
				count++;
			}
		}
		return count;
	}

	/**
	 * Store route id for smsc.
	 * 
	 * @param fdpsmscConfigDTO
	 *            the fdpsmsc config dto
	 * @param serverName
	 *            the server name
	 * @param routeIdList
	 */
	private void storeRouteIdForSMSC(final FDPSMSCConfigDTO fdpsmscConfigDTO, final String serverName,
			final List<String> routeIdList) {
		final FDPAppBag appBag = new FDPAppBag();
		appBag.setSubStore(AppCacheSubStore.SMS_USSD_ROUTE_DETAIL);
		final String key = RouteUtil.prepareExternalSystemAppBagKey(ApplicationUtil.getServerName(),
				fdpsmscConfigDTO.getIp(), fdpsmscConfigDTO.getPort(), fdpsmscConfigDTO.getBindSystemId(),
				fdpsmscConfigDTO.getBindNode());
		appBag.setKey(key);
		FDPSMSCConfigDTO configDTO = (FDPSMSCConfigDTO) applicationConfigCache.getValue(appBag);
		if (configDTO == null) {
			configDTO = fdpsmscConfigDTO;
		}
		configDTO.setRouteId(routeIdList);
		applicationConfigCache.putValue(appBag, configDTO);
	}

	/**
	 * Get Circlewise endpoints to weld load balancer.
	 * 
	 * @param fdpAIRConfigDTOList
	 *            contains all air configurations
	 * @param circleCode
	 *            the circle code
	 * @return the circle end points for lb
	 */
	private String[] getCircleAirEndPointsForLB(final List<FDPAIRConfigDTO> fdpAIRConfigDTOList, final String circleCode) {
		final int noOfEndpoints = fdpAIRConfigDTOList.size();

		final List<String> airCircleEndpointList = new ArrayList<String>();
		for (int i = 0; i < noOfEndpoints; i++) {
			final FDPAIRConfigDTO fdpAIRConfigDTO = fdpAIRConfigDTOList.get(i);
			final String circleCodeEndpoint = circleCode + fdpAIRConfigDTO.getExternalSystemId()
					+ BusinessConstants.UNDERSCORE + fdpAIRConfigDTO.getLogicalName();
			airCircleEndpointList.add(BusinessConstants.HTTP_COMPONENT_AIR_ENDPOINT + circleCodeEndpoint);
		}
		return airCircleEndpointList.toArray(new String[airCircleEndpointList.size()]);

	}
	
	
	private String[] getCircleDmcEndPointsForLB(final List<FDPDMCConfigDTO> fdpDMCConfigDTOList, final String circleCode) {
		final int noOfEndpoints = fdpDMCConfigDTOList.size();

		final List<String> dmcCircleEndpointList = new ArrayList<String>();
		for (int i = 0; i < noOfEndpoints; i++) {
			final FDPDMCConfigDTO fdpDMCConfigDTO = fdpDMCConfigDTOList.get(i);
			final String circleCodeEndpoint = circleCode + fdpDMCConfigDTO.getExternalSystemId()
					+ BusinessConstants.UNDERSCORE + fdpDMCConfigDTO.getLogicalName();
			dmcCircleEndpointList.add(BusinessConstants.HTTP_COMPONENT_DMC_ENDPOINT + circleCodeEndpoint);
		}
		return dmcCircleEndpointList.toArray(new String[dmcCircleEndpointList.size()]);

	}
	/**
	 * Get Circlewise endpoints to weld load balancer.
	 * 
	 * @param fdpCMSConfigDTOList
	 *            contains all cms configurations
	 * @param circleCode
	 *            the circle code
	 * @return the circle end points for lb
	 */
	private String[] getCircleCmsEndPointsForLB(final List<FDPCMSConfigDTO> fdpCMSConfigDTOList, final String circleCode) {
		final int noOfEndpoints = fdpCMSConfigDTOList.size();

		final List<String> airCircleEndpointList = new ArrayList<String>();
		for (int i = 0; i < noOfEndpoints; i++) {
			final FDPCMSConfigDTO fdpCMSConfigDTO = fdpCMSConfigDTOList.get(i);
			final String circleCodeEndpoint = circleCode + fdpCMSConfigDTO.getExternalSystemId()
					+ BusinessConstants.UNDERSCORE + fdpCMSConfigDTO.getLogicalName();
			airCircleEndpointList.add(BusinessConstants.HTTP_COMPONENT_CMS_ENDPOINT + circleCodeEndpoint);
		}
		return airCircleEndpointList.toArray(new String[airCircleEndpointList.size()]);

	}

	/**
	 * Get Circlewise endpoints to weld load balancer.
	 * 
	 * @param fdpCGWConfigDTOList
	 *            contains all cgw configurations.
	 * @param circleCode
	 *            the circle code.
	 * @return the circle end points for lb.
	 */
	private String[] getCircleCGWEndPointsForLB(final List<FDPCGWConfigDTO> fdpCGWConfigDTOList, final String circleCode) {
		final int noOfEndpoints = fdpCGWConfigDTOList.size();

		final List<String> cgwCircleEndpointList = new ArrayList<String>();
		for (int i = 0; i < noOfEndpoints; i++) {
			final FDPCGWConfigDTO fdpCGWConfigDTO = fdpCGWConfigDTOList.get(i);
			final String circleCodeEndpoint = circleCode + fdpCGWConfigDTO.getExternalSystemId()
					+ BusinessConstants.UNDERSCORE + fdpCGWConfigDTO.getLogicalName();
			cgwCircleEndpointList.add(BusinessConstants.HTTP_COMPONENT_CGW_ENDPOINT + circleCodeEndpoint);
		}
		return cgwCircleEndpointList.toArray(new String[cgwCircleEndpointList.size()]);

	}

	/**
	 * Get Circlewise endpoints to weld load balancer.
	 * 
	 * @param fdpRSConfigDTOList
	 *            contains all rs configurations
	 * @param circleCode
	 *            the circle code
	 * @return the circle end points for lb
	 */
	private String[] getCircleRSEndPointsForLB(final List<FDPRSConfigDTO> fdpRSConfigDTOList, final String circleCode) {
		final int noOfEndpoints = fdpRSConfigDTOList.size();

		final List<String> rsCircleEndpointList = new ArrayList<String>();
		for (int i = 0; i < noOfEndpoints; i++) {
			final FDPRSConfigDTO fdpRSConfigDTO = fdpRSConfigDTOList.get(i);
			final String circleCodeEndpoint = circleCode + fdpRSConfigDTO.getExternalSystemId()
					+ BusinessConstants.UNDERSCORE + fdpRSConfigDTO.getLogicalName();
			rsCircleEndpointList.add(BusinessConstants.HTTP_COMPONENT_RS_ENDPOINT + circleCodeEndpoint);
		}
		return rsCircleEndpointList.toArray(new String[rsCircleEndpointList.size()]);

	}
	
	private String[] getCircleADCEndPointsForLB(final List<FDPADCConfigDTO> fdpADCConfigDTOList, final String circleCode) {
		final int noOfEndpoints = fdpADCConfigDTOList.size();

		final List<String> adcCircleEndpointList = new ArrayList<String>();
		for (int i = 0; i < noOfEndpoints; i++) {
			final FDPADCConfigDTO fdpADCConfigDTO = fdpADCConfigDTOList.get(i);
			final String circleCodeEndpoint = circleCode + fdpADCConfigDTO.getExternalSystemId()
					+ BusinessConstants.UNDERSCORE + fdpADCConfigDTO.getLogicalName();
			adcCircleEndpointList.add(BusinessConstants.HTTP_COMPONENT_ADC_ENDPOINT + circleCodeEndpoint);
		}
		return adcCircleEndpointList.toArray(new String[adcCircleEndpointList.size()]);

	}

	/**
	 * Gets the direct endpoint for circle.
	 * 
	 * @param fdpemaConfigDTO
	 *            the fdpema config dto
	 * @param circleCode
	 *            the circle code
	 * @return the direct endpoint for circle
	 */
	private String getDirectEndpointForCircle(final FDPEMAConfigDTO fdpemaConfigDTO, final String circleCode) {
		final String endpoint = BusinessConstants.CAMEL_DIRECT + circleCode + BusinessConstants.UNDERSCORE
				+ fdpemaConfigDTO.getInterfaceType() + BusinessConstants.UNDERSCORE + fdpemaConfigDTO.getProtocolType()
				+ BusinessConstants.UNDERSCORE + fdpemaConfigDTO.getLogicalName();
		return endpoint;
	}

	/**
	 * Compiled route id.
	 * 
	 * @param fdpemaConfigDTO
	 *            the fdpema config dto
	 * @param circleCode
	 *            the circle code
	 * @return the string
	 */
	private String compiledRouteId(final FDPEMAConfigDTO fdpemaConfigDTO, final String circleCode) {
		String routeId = null;
		routeId = circleCode + BusinessConstants.UNDERSCORE + fdpemaConfigDTO.getInterfaceType()
				+ BusinessConstants.UNDERSCORE + fdpemaConfigDTO.getProtocolType() + BusinessConstants.UNDERSCORE
				+ fdpemaConfigDTO.getLogicalName();
		return routeId;
	}

	/**
	 * Gets the eMA endpoint url for circle.
	 * 
	 * @param fdpEMAConfigDTO
	 *            the fdp ema config dto
	 * @return the eMA endpoint url for circle
	 */
	private String getEMAEndpointURLForCircle(final FDPEMAConfigDTO fdpEMAConfigDTO) {
		final String protocolType = fdpEMAConfigDTO.getProtocolType().toUpperCase();
		String emaUrl = "";
		LOGGER.debug("Protocol Type:" + protocolType);
		if (protocolType.equals(EmaProtocolType.SSH.name())) {
			emaUrl = BusinessConstants.EMA_SSH_COMPONENT_TYPE + fdpEMAConfigDTO.getUserName() + BusinessConstants.COLON
					+ fdpEMAConfigDTO.getPassword() + BusinessConstants.AT_THE_RATE
					+ fdpEMAConfigDTO.getIpAddress().getValue() + BusinessConstants.COLON + fdpEMAConfigDTO.getPort()
					+ BusinessConstants.QUERY_STRING_SEPARATOR + "timeout=" + fdpEMAConfigDTO.getTimeout();
		}
		if (protocolType.equals(EmaProtocolType.TELNET.name())) {
			emaUrl = BusinessConstants.EMA_TELNET_COMPONENT_TYPE + fdpEMAConfigDTO.getIpAddress().getValue()
					+ BusinessConstants.COLON + fdpEMAConfigDTO.getPort() + BusinessConstants.NETTY_PART_ENDPOINT_URL;
		}

		LOGGER.debug("EMA Url:" + emaUrl);
		return emaUrl;
	}

	/**
	 * Sets the user name password for smsc.
	 * 
	 * @param fdpsmscConfigDTO
	 *            the new user name password for smsc
	 */
	private void setUserNamePasswordForSMSC(final FDPSMSCConfigDTO fdpsmscConfigDTO) {
		final List<FDPNodeExternalSystemMappingDTO> systemMappingDTOList = fdpsmscConfigDTO.getSystemMappings();
		for (final FDPNodeExternalSystemMappingDTO mappingDTO : systemMappingDTOList) {
			if (mappingDTO.getServerNodeId() != null) {
				final String nodeName = mappingDTO.getServerNodeId().getNodeName();
				if (ApplicationUtil.getServerName().equals(nodeName)) {
					fdpsmscConfigDTO.setBindSystemId(mappingDTO.getSystemId());
					fdpsmscConfigDTO.setBindSystemPassword(mappingDTO.getSystemPassword());
					break;
				}
			}
		}
	}

	private void addRouteIdInCache(final FDPAIRConfigDTO fdpairConfigDTO, final List<String> routeIdList,
			final String circleCode, final LoadBalanceDefinition loadBalanceDefinition) {
		final FDPAppBag appBag = new FDPAppBag();
		appBag.setSubStore(AppCacheSubStore.AIRCONFEGURATION_MAP);
		final String key = this.prepareExternalSystemAppBagKey(circleCode, fdpairConfigDTO.getIpAddress().getValue(),
				fdpairConfigDTO.getPort(), null, null);
		appBag.setKey(key);
		final FDPAIRConfigDTO externalSystemDTO = (FDPAIRConfigDTO) applicationConfigCache.getValue(appBag);
		externalSystemDTO.setRouteId(routeIdList);
		if (loadBalanceDefinition != null) {
			final Map<String, Object> loadBalancerMap = loadBalancerStorage.getLoadBalancerMap();
			final String lbKey = ExternalSystem.AIR.name() + BusinessConstants.COLON + circleCode;
			loadBalancerMap.put(lbKey, loadBalanceDefinition);
		}
		applicationConfigCache.putValue(appBag, externalSystemDTO);
	}
	
	
	private void addDMCRouteIdInCache(final FDPDMCConfigDTO fdpdmcConfigDTO, final List<String> routeIdList,
			final String circleCode, final LoadBalanceDefinition loadBalanceDefinition) {
		final FDPAppBag appBag = new FDPAppBag();
		appBag.setSubStore(AppCacheSubStore.DMCCONFEGURATION_MAP);
		final String key = this.prepareExternalSystemAppBagKey(circleCode, fdpdmcConfigDTO.getIpAddress().getValue(),
				fdpdmcConfigDTO.getPort(), null, null);
		appBag.setKey(key);
		final FDPDMCConfigDTO externalSystemDTO = (FDPDMCConfigDTO) applicationConfigCache.getValue(appBag);
		externalSystemDTO.setRouteId(routeIdList);
		if (loadBalanceDefinition != null) {
			final Map<String, Object> loadBalancerMap = loadBalancerStorage.getLoadBalancerMap();
			final String lbKey = ExternalSystem.DMC.name() + BusinessConstants.COLON + circleCode;
			loadBalancerMap.put(lbKey, loadBalanceDefinition);
		}
		applicationConfigCache.putValue(appBag, externalSystemDTO);
	}
	
	private void addCMSRouteIdInCache(final FDPCMSConfigDTO fdpcmsConfigDTO, final List<String> routeIdList,
			final String circleCode, final LoadBalanceDefinition loadBalanceDefinition) {
		final FDPAppBag appBag = new FDPAppBag();
		appBag.setSubStore(AppCacheSubStore.CMSCONFEGURATION_MAP);
		final String key = this.prepareExternalSystemAppBagKey(circleCode, fdpcmsConfigDTO.getIpAddress().getValue(),
				fdpcmsConfigDTO.getPort(), null, null);
		appBag.setKey(key);
		final FDPCMSConfigDTO externalSystemDTO = (FDPCMSConfigDTO) applicationConfigCache.getValue(appBag);
		externalSystemDTO.setRouteId(routeIdList);
		if (loadBalanceDefinition != null) {
			final Map<String, Object> loadBalancerMap = loadBalancerStorage.getLoadBalancerMap();
			final String lbKey = ExternalSystem.CMS.name() + BusinessConstants.COLON + circleCode;
			loadBalancerMap.put(lbKey, loadBalanceDefinition);
		}
		applicationConfigCache.putValue(appBag, externalSystemDTO);
	}

	private String prepareExternalSystemAppBagKey(final String circleCode, final String ipAddress, final Integer port,
			final String bindSystemId, final String bindNode) {
		final StringBuilder strBuilder = new StringBuilder();
		strBuilder.append(circleCode).append(FDPConstant.IP_PORT_SEPARATOR).append(ipAddress)
				.append(FDPConstant.IP_PORT_SEPARATOR).append(port);
		if (bindSystemId != null) {
			strBuilder.append(FDPConstant.IP_PORT_SEPARATOR).append(bindSystemId);
		}
		if (bindNode != null) {
			strBuilder.append(FDPConstant.IP_PORT_SEPARATOR).append(bindNode);
		}
		return strBuilder.toString();
	}

	/**
	 * Update route detail for sms cand ussd.
	 * 
	 * @param oldFdpsmscConfigDTO
	 *            the old fdpsmsc config dto
	 * @param newFdpsmscConfigDTO
	 *            the new fdpsmsc config dto
	 */
	private void updateRouteDetailForSMSCandUSSD(final FDPSMSCConfigDTO oldFdpsmscConfigDTO,
			final FDPSMSCConfigDTO newFdpsmscConfigDTO) {

		final FDPAppBag appBag = new FDPAppBag();

		// Removing old stored object.
		appBag.setSubStore(AppCacheSubStore.SMS_USSD_ROUTE_DETAIL);
		final String keyToBeRemove = RouteUtil.prepareExternalSystemAppBagKey(ApplicationUtil.getServerName(),
				oldFdpsmscConfigDTO.getIp(), oldFdpsmscConfigDTO.getPort(), oldFdpsmscConfigDTO.getBindSystemId(),
				oldFdpsmscConfigDTO.getBindNode());
		appBag.setKey(keyToBeRemove);
		applicationConfigCache.removeKey(appBag);

		final String newKeyToBeStore = RouteUtil.prepareExternalSystemAppBagKey(ApplicationUtil.getServerName(),
				newFdpsmscConfigDTO.getIp(), newFdpsmscConfigDTO.getPort(), newFdpsmscConfigDTO.getBindSystemId(),
				newFdpsmscConfigDTO.getBindNode());

		appBag.setKey(newKeyToBeStore);
		FDPSMSCConfigDTO configDTO = (FDPSMSCConfigDTO) applicationConfigCache.getValue(appBag);
		if (configDTO == null) {
			configDTO = newFdpsmscConfigDTO;
		}
		applicationConfigCache.putValue(appBag, configDTO);
	}
	
	/**
	 * Restarts DMC Routes
	 * @param fdpDMCConfigDTO
	 * @return the route builder
	 */
	private void restartDMCRoutes(final FDPDMCConfigDTO fdpDMCConfigDTO){
		try {
			/*fdpDMCHttpRoute.stopAllRoutes();
			fdpDMCHttpRoute.stopRoute(fdpDMCConfigDTO);*/
			fdpDMCHttpRoute.createRoutes();
		} catch (Exception e) {
			LOGGER.error("Not Able to initialize the route ",e);
		}
	}
	
	/**
	 * Restarts EVDS HTTP Routes
	 * @param fdpEVDSConfigDTO
	 * @return the route builder
	 */
	private void restartEvdsHttpRoutes(final FDPEVDSConfigDTO fdpEvdsConfigDTO){
		try {
			evdsHttpRoute.stopAllRoutes();
			evdsHttpRoute.stopRoute(fdpEvdsConfigDTO);
			evdsHttpRoute.createRoutes();
		} catch (Exception e) {
			LOGGER.error("Not Able to initialize the route ",e);
		}
	}
	
	/**
	 * Restarts EVDS HTTP Routes
	 * @param fdpEVDSConfigDTO
	 * @return the route builder
	 */
	private void restartHttpRoutes(final FDPEVDSConfigDTO fdpEvdsConfigDTO){
		try {
			evdsRouteHttp.stopAllRoutes();
			evdsRouteHttp.stopRoute(fdpEvdsConfigDTO);
			evdsRouteHttp.createRoutes();
		} catch (Exception e) {
			LOGGER.error("Not Able to initialize the route ",e);
		}
	}
	
	public RouteBuilder createRoutesForSBBB(final FDPVASConfigDTO fdpVASConfigDTO, final String circleCode)
			throws ExecutionFailedException {
		return new RouteBuilder() {
			@SuppressWarnings("unchecked")
			@Override
			public void configure() throws Exception {
				final String logicalName = fdpVASConfigDTO.getLogicalName();
				for(final String commandName : SBBBHttpRouteImpl.commandNames) {
					final String fromEndpoint = HttpUtil.getEndpointForSBBB(circleCode,
							ExternalSystemType.SBBB, logicalName,commandName);
					LOGGER.debug("FromEndpoint:" + fromEndpoint);
					final String toEndpoint = HttpUtil.getHttpEndpointForSBBB(fdpVASConfigDTO, commandName);
					LOGGER.debug("ToEndpoint:" + toEndpoint);
					final String routeId = HttpUtil.getRouteIdForSBBBHttpRoutes(circleCode,
							ExternalSystemType.SBBB, logicalName);
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
					LOGGER.info("Route {} has been started sucessfully for circle {}.", routeId, fdpVASConfigDTO
							.getCircleDTO().getCircleCode());
				}
			}
		};
	}

	
	private boolean stopStartSMSCSubRoutes(FDPSMSCConfigDTO newfdpsmscConfigDTO, boolean stop){
	    final CdiCamelContext cdiCamelContext = cdiCamelContextProvider.getContext();
	    
	    boolean returnFlag = true;
	   
        try {
            if(stop){
                cdiCamelContext.stopRoute(BusinessConstants.MAIN_ROUTE + BusinessConstants.ROUTE_RX);
                cdiCamelContext.stopRoute(BusinessConstants.ROUTE_STR + newfdpsmscConfigDTO.getCircleDTO().getCircleCode()
                        + BusinessConstants.ROUTE_RX);
                cdiCamelContext.removeRoute(BusinessConstants.MAIN_ROUTE + BusinessConstants.ROUTE_RX);
                cdiCamelContext.removeRoute(BusinessConstants.ROUTE_STR + newfdpsmscConfigDTO.getCircleDTO().getCircleCode()
                        + BusinessConstants.ROUTE_RX);
            }
            else{
                cdiCamelContext.startRoute(BusinessConstants.MAIN_ROUTE + BusinessConstants.ROUTE_RX);
                cdiCamelContext.startRoute(BusinessConstants.ROUTE_STR + newfdpsmscConfigDTO.getCircleDTO().getCircleCode()
                        + BusinessConstants.ROUTE_RX);
            }
        } catch (Exception e) {
            LOGGER.error("stopStartSMSCSubRoutes: ", e);
            returnFlag = false;
        }
        return returnFlag;
        
	}
	
	private RouteBuilder createAbilityRoutes(final FDPAbilityConfigDTO fdpAbilityConfigDTO) {
        return new RouteBuilder() {

            @SuppressWarnings("unchecked")
            @Override
            public void configure() throws Exception {
                final String circleCode = fdpAbilityConfigDTO.getCircleDTO().getCircleCode();
                final List<FDPAbilityConfigDTO> fdpAbilityConfigDTOs = getEndpointURLsForAbilityByCircleCode(circleCode);
                final boolean autostartup = true;
                final CdiCamelContext cdiCamelContext = cdiCamelContextProvider.getContext();
                final String routeIdForLB = ExternalSystem.Ability.name() + BusinessConstants.UNDERSCORE + circleCode;
                cdiCamelContext.stopRoute(routeIdForLB);
                cdiCamelContext.removeRoute(routeIdForLB);
                removeLastNodeRoute(fdpAbilityConfigDTO, circleCode, cdiCamelContext);
                final String[] endpoints = getCircleAbilityEndPointsForLB(fdpAbilityConfigDTOs, circleCode);
                for (final String endpoint : endpoints) {
                    cdiCamelContext.stopRoute(endpoint);
                    cdiCamelContext.removeRoute(endpoint);
                }
                for (FDPAbilityConfigDTO fdpAbilityConfigDTO : fdpAbilityConfigDTOs) {

                    String routeIdToBeRemove = HttpUtil.getAbilitySubRouteId(fdpAbilityConfigDTO, circleCode);
                    cdiCamelContext.stopRoute(routeIdToBeRemove);
                    cdiCamelContext.removeRoute(routeIdToBeRemove);
                }
                int fdpAbilityConfigDTOListSize = fdpAbilityConfigDTOs.size();
                LoadBalanceDefinition loadBalanceDefinition = null;
                if (fdpAbilityConfigDTOs != null && fdpAbilityConfigDTOListSize > 1) {

                    loadBalanceDefinition = from(BusinessConstants.HTTP_COMPONENT_ABILITY_ENDPOINT + "_" + circleCode)
                            .routeId(ExternalSystem.Ability.name() + BusinessConstants.UNDERSCORE + circleCode)
                            .setExchangePattern(ExchangePattern.InOut).loadBalance()
                            .failover(fdpAbilityConfigDTOListSize - 1, false, true, java.lang.Exception.class);
                    for (final String endpoint : endpoints) {

                        loadBalanceDefinition.routeId(endpoint).to(endpoint);
                    }
                } else {
                    if (fdpAbilityConfigDTOs != null && fdpAbilityConfigDTOListSize == 1) {
                        from(BusinessConstants.HTTP_COMPONENT_ABILITY_ENDPOINT + BusinessConstants.UNDERSCORE + circleCode)
                                .setExchangePattern(ExchangePattern.InOut)
                                .routeId(ExternalSystem.Ability.name() + BusinessConstants.UNDERSCORE + circleCode)
                                .to(endpoints);
                    }
                }
                for (int j = 0; fdpAbilityConfigDTOs != null && j < fdpAbilityConfigDTOListSize; j++) {

                    final FDPAbilityConfigDTO fdpAbilityConfigDTO = fdpAbilityConfigDTOs.get(j);
                    final List<String> routeIdList = new ArrayList<String>();
                    /**
                     * Manages the re-delivery attempt and re-delivery delay.
                     */
                    final String circleCodeEndpoint = circleCode + fdpAbilityConfigDTO.getExternalSystemId() + BusinessConstants.UNDERSCORE
                            + fdpAbilityConfigDTO.getLogicalName();
                    final String ipAddress = fdpAbilityConfigDTO.getIpAddress().getValue();
                    final String circleCodeIpaddressPort = circleCode + BusinessConstants.COLON + ipAddress + BusinessConstants.COLON
                            + String.valueOf(fdpAbilityConfigDTO.getPort());
                    final String routeId2 = BusinessConstants.SUB_ROUTE_UNDERSCORE + circleCodeEndpoint + BusinessConstants.UNDERSCORE
                            + ExternalSystem.Ability.name();
                    routeIdList.add(routeId2);
                    from(BusinessConstants.HTTP_COMPONENT_ABILITY_ENDPOINT + circleCodeEndpoint)
                            .autoStartup(autostartup)
                            .routeId(routeId2)
                            .onCompletion()
                            .onCompleteOnly()
                            .process(completionProcessor)
                            .end()
                            .setProperty(BusinessConstants.OUTGOING_IP_ADDRESS, constant(ipAddress))
                            .setProperty(BusinessConstants.OUTGOING_CIRCLE_CODE_IP_PORT, constant(circleCodeIpaddressPort))
                            .process(logOutgoingIpProcessor)
                            // .process(headerProcessor)
                            .onException(java.lang.Exception.class).handled(false)
                            .maximumRedeliveries(fdpAbilityConfigDTO.getNumberOfRetry())
                            .redeliveryDelay(fdpAbilityConfigDTO.getRetryInterval()).onRedelivery(logRetryProcessor)
                            .to(BusinessConstants.COMPONENT_EXCEPTION_ENDPOINT).end()
                            .to(getAbilityEndpointURLForCircle(fdpAbilityConfigDTO)).process(abilitySyncRequestProcessor);
                    addRouteIdInCache(fdpAbilityConfigDTO, routeIdList, circleCode, loadBalanceDefinition);
                }
            }
            private void removeLastNodeRoute(FDPAbilityConfigDTO fdpAbilityConfigDTO, final String circleCode,
                    final CdiCamelContext cdiCamelContext) throws Exception {
                List<FDPAbilityConfigDTO> configDTOs = new ArrayList();
                configDTOs.add(fdpAbilityConfigDTO);
                final String[] endpoints = getCircleAbilityEndPointsForLB(configDTOs, circleCode);
                for (final String endpoint : endpoints) {
                    cdiCamelContext.stopRoute(endpoint);
                    cdiCamelContext.removeRoute(endpoint);
                }
                for (FDPAbilityConfigDTO fdpRsConfigDTO : configDTOs) {
                    String routeIdToBeRemove = HttpUtil.getAbilitySubRouteId(fdpRsConfigDTO, circleCode);
                    cdiCamelContext.stopRoute(routeIdToBeRemove);
                    cdiCamelContext.removeRoute(routeIdToBeRemove);
                }
            }
        };
        
        
	}

	   private String[] getCircleAbilityEndPointsForLB(final List<FDPAbilityConfigDTO> fdpAbilityConfigDTOs, final String circleCode) {
	        final int noOfEndpoints = fdpAbilityConfigDTOs.size();
	        final List<String> abilityCircleEndpointList = new ArrayList();
	        for (int i = 0; i < noOfEndpoints; i++) {
	            final FDPAbilityConfigDTO fdpAbilityConfigDTO = fdpAbilityConfigDTOs.get(i);
	            final String circleCodeEndpoint = circleCode + fdpAbilityConfigDTO.getExternalSystemId()
	                    + BusinessConstants.UNDERSCORE + fdpAbilityConfigDTO.getLogicalName();
	            abilityCircleEndpointList.add(BusinessConstants.HTTP_COMPONENT_ABILITY_ENDPOINT + circleCodeEndpoint);
	        }
	        return abilityCircleEndpointList.toArray(new String[abilityCircleEndpointList.size()]);

	    }

	    private void addRouteIdInCache(final FDPAbilityConfigDTO fdpAbilityConfigDTO, final List<String> routeIdList,
	            final String circleCode, final LoadBalanceDefinition loadBalanceDefinition) {
	        final FDPAppBag appBag = new FDPAppBag();
	        appBag.setSubStore(AppCacheSubStore.ABILITY_DETAILS);
	        final String key = this.prepareExternalSystemAppBagKey(circleCode, fdpAbilityConfigDTO.getIpAddress().getValue(),
	                fdpAbilityConfigDTO.getPort(), null, null);
	        appBag.setKey(key);
	        final FDPAbilityConfigDTO externalSystemDTO = (FDPAbilityConfigDTO) applicationConfigCache.getValue(appBag);
	        externalSystemDTO.setRouteId(routeIdList);
	        if (loadBalanceDefinition != null) {
	            final Map<String, Object> loadBalancerMap = loadBalancerStorage.getLoadBalancerMap();
	            final String lbKey = ExternalSystem.Ability.name() + BusinessConstants.COLON + circleCode;
	            loadBalancerMap.put(lbKey, loadBalanceDefinition);
	        }
	        applicationConfigCache.putValue(appBag, externalSystemDTO);
	    }


    private String getAbilityEndpointURLForCircle(final FDPAbilityConfigDTO fdpAbilityConfigDTO) {
        final String abilityUrl = BusinessConstants.HTTP_COMPONENT_TYPE + fdpAbilityConfigDTO.getIpAddress().getValue()
                + BusinessConstants.COLON + fdpAbilityConfigDTO.getPort() + "/" + fdpAbilityConfigDTO.getContextPath()
                + BusinessConstants.QUERY_STRING_SEPARATOR + BusinessConstants.HTTP_CLIENT_SO_TIMEOUT + BusinessConstants.EQUALS
                + fdpAbilityConfigDTO.getResponseTimeout();
        LOGGER.debug("Ability Url:" + abilityUrl);
        return abilityUrl;
    }
	    
}
