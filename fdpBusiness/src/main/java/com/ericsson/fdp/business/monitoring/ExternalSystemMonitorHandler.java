package com.ericsson.fdp.business.monitoring;

import java.util.List;

import javax.inject.Inject;

import org.apache.camel.cdi.CdiCamelContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ericsson.fdp.business.constants.BusinessConstants;
import com.ericsson.fdp.business.http.router.ADCHttpRoute;
import com.ericsson.fdp.business.http.router.AIRHttpRoute;
import com.ericsson.fdp.business.http.router.AbilityHttpRoute;
import com.ericsson.fdp.business.http.router.CGWHttpRoute;
import com.ericsson.fdp.business.http.router.CMSHttpRoute;
import com.ericsson.fdp.business.http.router.DMCHttpRoutes;
import com.ericsson.fdp.business.http.router.ESFHttpRoute;
import com.ericsson.fdp.business.http.router.EVDSHttpRoute;
import com.ericsson.fdp.business.http.router.EVDSRouteHttp;
import com.ericsson.fdp.business.http.router.EvdsTelnetRoute;
import com.ericsson.fdp.business.http.router.FDPManagmentRoute;
import com.ericsson.fdp.business.http.router.FDPMobileMoneyHttpsRoute;
import com.ericsson.fdp.business.http.router.FDPMobileMoneyRoute;
import com.ericsson.fdp.business.http.router.FDPSshRoute;
import com.ericsson.fdp.business.http.router.FulfillmentServiceRoute;
import com.ericsson.fdp.business.http.router.LoyaltyHttpRoute;
import com.ericsson.fdp.business.http.router.RSHttpRoute;
import com.ericsson.fdp.business.http.router.VASServiceRoute;
import com.ericsson.fdp.business.http.router.impl.MCLoanHttpRoutesImpl;
import com.ericsson.fdp.business.http.router.impl.MCarbonHttpRoutesImpl;
import com.ericsson.fdp.business.http.router.impl.ManhattanHttpRoutesImpl;
import com.ericsson.fdp.business.http.router.impl.SBBBHttpRouteImpl;
import com.ericsson.fdp.business.monitoring.component.ExternalSystemMonitorChain;
import com.ericsson.fdp.business.smsc.router.FDPSMSCRouter;
import com.ericsson.fdp.common.constants.FDPConstant;
import com.ericsson.fdp.common.enums.ExternalSystem;
import com.ericsson.fdp.core.config.utils.PropertyUtils;
import com.ericsson.fdp.dao.dto.FDPCircleDTO;
import com.ericsson.fdp.dao.fdpadmin.FDPCircleDAO;
import com.ericsson.fdp.http.adapter.impl.UssdHttpServiceRoute;
import com.ericsson.fdp.http.adapter.impl.UssdHttpServiceRouteZTE;
import com.ericsson.fdp.route.cdi.context.provider.CdiCamelContextProvider;

/**
 * The Class ExternalSystemMonitorHandler is responsible for monitoring all
 * external systems of all active circles.
 * 
 * @author Ericsson
 */
public class ExternalSystemMonitorHandler extends AbstractMonitorHandler {

	private static final Logger LOGGER = LoggerFactory.getLogger(ExternalSystemMonitorHandler.class);

	/** The fdp external system monitor chain. */
	private ExternalSystemMonitorChain fdpExternalSystemMonitorChain;
	
	private static final String evdsType = PropertyUtils
			.getProperty("evds.protocol.type");
	
	private static final String mmProtocolType = PropertyUtils
			.getProperty("ecw.protocol.type");
	
	@Inject
	private FDPCircleDAO fdpCircleDAO;

	@Inject
	private CdiCamelContextProvider cdiCamelContextProvider;

	@Inject
	private FDPMobileMoneyRoute fdpmobilemoneyRoute;
	
	@Inject
	private FDPMobileMoneyHttpsRoute fdpmobilemoneyHttpsRoute;
	
	@Inject
	private LoyaltyHttpRoute loyaltyHttpRoute;
	
	@Inject
	private EvdsTelnetRoute evdsTelnetRoute;
	
	@Inject
	private FDPSMSCRouter fdpSMSCRouter;

	@Inject
	private AIRHttpRoute airHttpRoute;
	
	@Inject
	private DMCHttpRoutes dmcHttpRoute;

	@Inject
	private RSHttpRoute rsHttpRoute;
	
	@Inject
	private ADCHttpRoute adcHttpRoute;

	@Inject
	private CGWHttpRoute cgwHttpRoute;

	@Inject
	private FDPSshRoute sshRoute;
	
	@Inject
	private FulfillmentServiceRoute ivrServiceRoute;
	
	@Inject
	private MCarbonHttpRoutesImpl mRoutesImpl;
	
	@Inject
	private VASServiceRoute vasServiceRoute;
	
	@Inject 
	private ManhattanHttpRoutesImpl manhattanHttpRoutesImpl;
	
	@Inject 
	private CMSHttpRoute cmshttpRoute;
	
	@Inject 
	private MCLoanHttpRoutesImpl mcLoanhttpRoute;
	
	@Inject
	private FDPManagmentRoute fdpManagmentRoute;
	
	@Inject
	private UssdHttpServiceRoute httpServiceRoute;
	
	@Inject
	private UssdHttpServiceRouteZTE httpServiceRouteZTE;
	
	@Inject
	private AbilityHttpRoute abilityHttpRoute;
	
	@Inject
	private EVDSHttpRoute evdsHttpRoute;
	
	@Inject
	private ESFHttpRoute esfHttpRoute;
	
	@Inject
	private EVDSRouteHttp evdsRouteHttp;
	
	@Inject
	private SBBBHttpRouteImpl sbbbHttpRouteImpl;
	
	
	@Override
	public boolean process() {
		boolean processed = false;
		createRoutes();
		cdiCamelContextProvider.getContext().start();
		// cdiCamelContextProvider.getContext().setStreamCaching(true);
		final List<FDPCircleDTO> circleCodeList = fdpCircleDAO.getAllAvailableCircles();
		for (final FDPCircleDTO fdpCircleDTO : circleCodeList) {
			if (fdpExternalSystemMonitorChain.monitorComponent(fdpCircleDTO.getCircleCode())) {
				startRoute(fdpCircleDTO.getCircleCode());
				processed = true;
				LOGGER.info("successfully started route for circle: {} ", fdpCircleDTO.getCircleCode());
				LOGGER.debug("successfully started route for circle: " + fdpCircleDTO.getCircleCode());
                /*
                 * FDPLoggerFactory.getGenerateAlarmLogger().warn( "", new Event(TrapSeverity.CLEAR, new
                 * TrapError(TrapErrorCodes.UNABLE_TO_CREATE_ROUTES), SNMPUtil .getIPAddess()));
                 */
			} else {
				LOGGER.warn("Unable to start routes for circle : {}", fdpCircleDTO.getCircleCode());
                /*
                 * FDPLoggerFactory.getGenerateAlarmLogger().warn( "", new Event(TrapSeverity.CRITICAL, new
                 * TrapError(TrapErrorCodes.UNABLE_TO_CREATE_ROUTES, Arrays .asList(fdpCircleDTO.getCircleCode())),
                 * SNMPUtil.getIPAddess()));
                 */
			}
		}
		if (getNextHandler() != null) {
			return getNextHandler().process();
		}
		return processed;
	}

	/**
	 * Creates the routes.
	 */
	private void createRoutes() {
		try {
			LOGGER.debug("Creating routes....");
			fdpSMSCRouter.createRoutes(false);
			airHttpRoute.createRoutes();
			rsHttpRoute.createRoutes();
			cgwHttpRoute.createRoutes();
			sshRoute.createRoutes();
			ivrServiceRoute.createRoutes();
			mRoutesImpl.createRoutesVAS();
			vasServiceRoute.createRoutes();
			manhattanHttpRoutesImpl.createRoutesVAS();
			cmshttpRoute.createRoutes();
			mcLoanhttpRoute.createRoutesVAS();
			fdpManagmentRoute.createRoutes();
			//Route for sending request for Mobile Money 
			//fdpmobilemoneyRoute.createRoutes();
			mmRouteStart();
			//Route for receiving Message from Mo
			loyaltyHttpRoute.createRoutes();
			//Route for EVDS payment source
			evdsRouteStart();
			httpServiceRoute.createRoutes();
			httpServiceRouteZTE.createRoutes();
			sbbbHttpRouteImpl.createSbbbRoutes();
			esfHttpRoute.createRoutes();
			abilityHttpRoute.createRoutes();
			dmcHttpRoute.createRoutes();
			adcHttpRoute.createRoutes();
		} catch (final Exception e) {
			throw new RuntimeException("Unable to create Routes", e);
		}
	}

	/**
	 * Start route.
	 * 
	 * @param circleCode
	 *            the circle code
	 */
	private void startRoute(final String circleCode) {
		startSMSCRoute(circleCode);
		startAIRRoute(circleCode);
		startCGWRoute(circleCode);
		startCMSRoute(circleCode);
		startMCLoanRoute(circleCode);
		startDMCRoute(circleCode);
	}

	/**
	 * Start cgw route.
	 * 
	 * @param circleCode
	 *            the circle code
	 */
	private void startCGWRoute(final String circleCode) {
		final String cgwRouteId = BusinessConstants.SUB_ROUTE_UNDERSCORE + circleCode + BusinessConstants.UNDERSCORE
				+ ExternalSystem.CGW.name();
		final CdiCamelContext camelContext = cdiCamelContextProvider.getContext();
		try {
			camelContext.startRoute(cgwRouteId);
		} catch (final Exception e) {
			throw new RuntimeException("Unable to start CGW Routes for circle code: " + circleCode, e);
		}

	}

	private void startAIRRoute(final String circleCode) {
		LOGGER.debug("Starting Air Routes for circle : " + circleCode);
		final String airRouteId = BusinessConstants.SUB_ROUTE_UNDERSCORE + circleCode + BusinessConstants.UNDERSCORE
				+ ExternalSystem.AIR.name();
		final CdiCamelContext camelContext = cdiCamelContextProvider.getContext();
		try {
			camelContext.startRoute(airRouteId);
		} catch (final Exception e) {
			throw new RuntimeException("Unable to start AIR Routes for circle code: " + circleCode, e);
		}

	}
	
	private void startDMCRoute(final String circleCode) {
		LOGGER.debug("Starting Dmc Routes for circle : " + circleCode);
		final String dmcRouteId = BusinessConstants.SUB_ROUTE_UNDERSCORE + circleCode + BusinessConstants.UNDERSCORE
				+ ExternalSystem.DMC.name();
		final CdiCamelContext camelContext = cdiCamelContextProvider.getContext();
		try {
			camelContext.startRoute(dmcRouteId);
		} catch (final Exception e) {
			throw new RuntimeException("Unable to start DMC Routes for circle code: " + circleCode, e);
		}

	}
	
	private void startCMSRoute(final String circleCode) {
		LOGGER.debug("Starting cms Routes for circle : " + circleCode);
		final String cmsRouteId = BusinessConstants.SUB_ROUTE_UNDERSCORE + circleCode + BusinessConstants.UNDERSCORE
				+ ExternalSystem.CMS.name();
		final CdiCamelContext camelContext = cdiCamelContextProvider.getContext();
		try {
			camelContext.startRoute(cmsRouteId);
		} catch (final Exception e) {
			throw new RuntimeException("Unable to start CMS Routes for circle code: " + circleCode, e);
		}

	}

	private void startMCLoanRoute(final String circleCode) {
		LOGGER.debug("Starting MCLoan Routes for circle : " + circleCode);
		final String mcLoanRouteId = BusinessConstants.SUB_ROUTE_UNDERSCORE + circleCode + BusinessConstants.UNDERSCORE
				+ ExternalSystem.MCLOAN.name();
		final CdiCamelContext camelContext = cdiCamelContextProvider.getContext();
		try {
			camelContext.startRoute(mcLoanRouteId);
		} catch (final Exception e) {
			throw new RuntimeException("Unable to start MCLoan Routes for circle code: " + circleCode, e);
		}

	}
	
	private void startSMSCRoute(final String circleCode) {
		LOGGER.debug("Starting route for circleCode: " + circleCode);
		final String smsRouteIdRx = BusinessConstants.MAIN_ROUTE + BusinessConstants.UNDERSCORE + circleCode
				+ BusinessConstants.UNDERSCORE + BusinessConstants.ROUTE_RX_SMS;
		final String ussdRouteIdRx = BusinessConstants.MAIN_ROUTE + BusinessConstants.UNDERSCORE + circleCode
				+ BusinessConstants.UNDERSCORE + BusinessConstants.ROUTE_RX_USSD;
		final String smscRouteIdTx = BusinessConstants.MAIN_ROUTE + BusinessConstants.UNDERSCORE + circleCode
				+ BusinessConstants.UNDERSCORE + BusinessConstants.ROUTE_TX;

		final CdiCamelContext camelContext = cdiCamelContextProvider.getContext();
		try {
			camelContext.startRoute(smsRouteIdRx);
			camelContext.startRoute(ussdRouteIdRx);
			camelContext.startRoute(smscRouteIdTx);
			LOGGER.debug("Context started for routes : " + smsRouteIdRx + " ussdRouteIdRx: " + ussdRouteIdRx);
		} catch (final Exception e) {
			throw new RuntimeException("Unable to start SMSC/USSD Routes for circle code: " + circleCode, e);
		}

	}

	/**
	 * Gets the fdp external system monitor chain.
	 * 
	 * @return the fdp external system monitor chain
	 */
	public ExternalSystemMonitorChain getFdpExternalSystemMonitorChain() {
		return fdpExternalSystemMonitorChain;
	}

	/**
	 * Sets the fdp external system monitor chain.
	 * 
	 * @param fdpExternalSystemMonitorChain
	 *            the new fdp external system monitor chain
	 */
	public void setFdpExternalSystemMonitorChain(final ExternalSystemMonitorChain fdpExternalSystemMonitorChain) {
		this.fdpExternalSystemMonitorChain = fdpExternalSystemMonitorChain;
	}
	
	public void evdsRouteStart() throws Exception{
		if(evdsType.equalsIgnoreCase((String) FDPConstant.EVDS_TYPE_HTTP)){
			evdsHttpRoute.createRoutes();
		}
		else if(evdsType.equalsIgnoreCase((String) FDPConstant.EVDS_HTTP_TYPE)){
			evdsRouteHttp.createRoutes();
		}
		else{
		evdsTelnetRoute.createRoutes();
		}
	}
	
	public void mmRouteStart() throws Exception {
		LOGGER.debug("Mobile Money Protocol Type:" + mmProtocolType);
		if (mmProtocolType.equalsIgnoreCase(FDPConstant.MM_HTTP_TYPE)) {
			LOGGER.debug("Creating Mobile Money route for HTTP request");
			fdpmobilemoneyRoute.createRoutes();
			LOGGER.debug("Mobile Money route for HTTP request created successfully");
		} else if (mmProtocolType.equalsIgnoreCase(FDPConstant.MM_HTTPS_TYPE)) {
			LOGGER.debug("Creating Mobile Money route for HTTPS request");
			fdpmobilemoneyHttpsRoute.createRoutes();
			LOGGER.debug("Mobile Money route for HTTPS request created successfully");
		} 
	}

}
