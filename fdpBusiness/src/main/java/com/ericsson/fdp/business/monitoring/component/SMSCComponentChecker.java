package com.ericsson.fdp.business.monitoring.component;

import java.util.List;

import javax.inject.Inject;

import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.cdi.CdiCamelContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ericsson.fdp.business.constants.BusinessConstants;
import com.ericsson.fdp.dao.dto.FDPExternalSystemDTO;
import com.ericsson.fdp.dao.dto.FDPSMSCConfigDTO;
import com.ericsson.fdp.dao.fdpadmin.FDPSMSCConfigDAO;

/**
 * The Class SMSCComponentChecker.
 * 
 * @author Ericsson
 */
// @Named("SMSCComponentChecker")
public class SMSCComponentChecker extends AbstractExternalSystemChecker {

	/** The fdp smsc config dao. */
	@Inject
	private FDPSMSCConfigDAO fdpSMSCConfigDAO;

	/** The context. */
	@Inject
	private CdiCamelContext context;

	/** The logger. */
	private static final Logger LOGGER = LoggerFactory
			.getLogger(SMSCComponentChecker.class);

	@Override
	public boolean monitorComponent(String circle) {
		// context = cdiCamelContextProvider.getContext();
		context.start();
		List<? extends FDPExternalSystemDTO> componentList = fdpSMSCConfigDAO
				.getSMSCByCircleCode(circle, BusinessConstants.SERVICE_TYPE_SMS);
		boolean componentUp = false;
		for (FDPExternalSystemDTO airComponent : componentList) {
			componentUp = monitorComponent(airComponent);
			if (componentUp && isAtleastOneSystemUp()) {
				componentUp = true;
				break;
			} else if (!isAtleastOneSystemUp() && !componentUp) {
				componentUp = false;
				break;
			}
			// airComponent.
		}
		if (componentUp && next() != null) {
			return next().monitorComponent(circle);
		}
		// context.stop();
		return componentUp;
	}

	@Override
	protected boolean monitorComponent(FDPExternalSystemDTO fdpExternalSystemDTO) {

		boolean flag = false;
		if (fdpExternalSystemDTO instanceof FDPSMSCConfigDTO) {
			FDPSMSCConfigDTO fdpSMSCConfigDTO = (FDPSMSCConfigDTO) fdpExternalSystemDTO;
			String smscUrl = getSmscEndPoint(fdpSMSCConfigDTO);
			String circleCode = fdpSMSCConfigDTO.getCircle().getCircleCode();
			String routeId = BusinessConstants.ROUTE + circleCode;
			try {
				context.addRoutes(createRouteForSMSC(smscUrl, circleCode));
				LOGGER.debug("starting SMSC route {} for circle {}:", routeId,
						circleCode);
				context.startRoute(routeId);
				context.stopRoute(routeId);
				flag = true;
			} catch (Exception e) {
				flag = false;
				LOGGER.error("Unable to start route :", e.getCause());
			} finally {
				try {
					context.stopRoute(routeId);
					context.removeRoute(routeId);
				} catch (Exception e) {
					LOGGER.error("Unable to stop/remove route :", e.getCause());
				}
			}
		}

		return flag;
	}

	/**
	 * Creates the route for smsc.
	 * 
	 * @param smscUrl
	 *            the smsc url
	 * @param circleCode
	 *            the circle code
	 * @return the route builder
	 */
	private RouteBuilder createRouteForSMSC(final String smscUrl,
			final String circleCode) {
		return new RouteBuilder() {
			@Override
			public void configure() throws Exception {
				from("direct:start")
						.routeId(BusinessConstants.ROUTE + circleCode)
						.autoStartup(true).to(smscUrl);
			}
		};
	}

	/**
	 * Gets the smsc end point.
	 * 
	 * @param fdpSMSCConfigDTO
	 *            the fdp smsc config dto
	 * @return the smsc end point
	 */
	private String getSmscEndPoint(FDPSMSCConfigDTO fdpSMSCConfigDTO) {
		String smscUrl = "smpp://" + fdpSMSCConfigDTO.getBindSystemId().trim()
				+ "@" + fdpSMSCConfigDTO.getIp() + ":"
				+ fdpSMSCConfigDTO.getPort() + "?password="
				+ fdpSMSCConfigDTO.getBindSystemPassword()
				+ "&amp;enquireLinkTimer="
				+ fdpSMSCConfigDTO.getLinkCheckPeriod()
				+ "&amp;transactionTimer="
				+ fdpSMSCConfigDTO.getResponseTimeout() + "&amp;systemType="
				+ fdpSMSCConfigDTO.getBindNode().trim() + "&amp;serviceType="
				+ fdpSMSCConfigDTO.getServiceType() + "&amp;sourceAddrTon="
				+ fdpSMSCConfigDTO.getBindTON() + "&amp;sourceAddrNpi="
				+ fdpSMSCConfigDTO.getBindNPI();
		LOGGER.debug("SMSC Endpoint URL is {}", smscUrl);
		return smscUrl;
	}

}
