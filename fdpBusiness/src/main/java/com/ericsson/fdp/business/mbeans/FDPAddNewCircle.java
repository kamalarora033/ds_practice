package com.ericsson.fdp.business.mbeans;

import java.lang.management.ManagementFactory;
import java.util.Map;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.annotation.Resource;
import javax.ejb.DependsOn;
import javax.ejb.Singleton;
import javax.ejb.Startup;
import javax.inject.Inject;
import javax.management.MBeanServer;
import javax.management.ObjectName;

import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.cdi.CdiCamelContext;
import org.apache.camel.model.ChoiceDefinition;

import com.ericsson.fdp.business.constants.BusinessConstants;
import com.ericsson.fdp.business.route.controller.ChoiceDefinitionStorage;
import com.ericsson.fdp.business.route.processor.RequestProcessor;
import com.ericsson.fdp.business.smsc.throtller.ThrotllerWaterGate;
import com.ericsson.fdp.business.smsc.throtller.ThrottlingScope;
import com.ericsson.fdp.common.enums.ExternalSystem;
import com.ericsson.fdp.core.config.utils.PropertyUtils;
import com.ericsson.fdp.dao.dto.FDPCircleDTO;
import com.ericsson.fdp.dao.entity.ExternalSystemType;
import com.ericsson.fdp.dao.fdpadmin.FDPCircleDAO;
import com.ericsson.fdp.route.cdi.context.provider.CdiCamelContextProvider;
import com.ericsson.fdp.route.processors.LogRetryProcessor;
import com.ericsson.fdp.route.processors.OutBoundQueueProcessor;
import com.ericsson.fdp.route.processors.ReceiptProcessor;
import com.ericsson.fdp.route.processors.ResponseProcessor;

/**
 * The Class FDPAddNewCircle is used add dynamic circle into camel running
 * routes for AIR , CGW , RS , SMSC and USSD external Systems.
 */
//@Singleton
//@Startup
//@DependsOn(value = "ApplicationMonitor")
public class FDPAddNewCircle implements FDPAddNewCircleMXBean {

	/** The object name. */
	private ObjectName objectName = null;

	/** The platform m bean server. */
	private MBeanServer platformMBeanServer;

	/** The choice definition storage. */
	@Resource(lookup = "java:app/fdpBusiness-1.0/ChoiceDefinitionStorage")
	private ChoiceDefinitionStorage choiceDefinitionStorage;

	@Inject
	private FDPCircleDAO fdpCircleDAO;

	@Inject
	private CdiCamelContextProvider cdiCamelContextProvider;

	@Inject
	private ThrotllerWaterGate routePolicyCircleRoute;

	@Inject
	private RequestProcessor requestProcessor;

	@Inject
	private ResponseProcessor responseProcessor;

	/** The log retry processor */
	@Inject
	private LogRetryProcessor logRetryProcessor;

	/** The out bound queue processor. */
	@Inject
	private OutBoundQueueProcessor outBoundQueueProcessor;

	/** The receipt processor. */
	@Inject
	private ReceiptProcessor receiptProcessor;

	/**
	 * Register in jmx.
	 */
	//@PostConstruct
	public final void registerInJMX() {
		try {
			objectName = new ObjectName("FDPAddNewCircle:type=" + this.getClass().getName());
			platformMBeanServer = ManagementFactory.getPlatformMBeanServer();
			platformMBeanServer.registerMBean(this, objectName);

		} catch (final Exception e) {
			throw new IllegalStateException("Problem during registration of Monitoring into JMX:" + e);
		}
	}

	/**
	 * Unregister from jmx.
	 */
//	@PreDestroy
	public final void unregisterFromJMX() {
		try {
			platformMBeanServer.unregisterMBean(this.objectName);
		} catch (final Exception e) {
			throw new IllegalStateException("Problem during unregistration of Monitoring into JMX:" + e);
		}
	}

	@Override
	public String addCircle(final String circleName) throws Exception {
		final String circleCode = getCircleCodeByCircleName(circleName);
		final CdiCamelContext cdiCamelContext = cdiCamelContextProvider.getContext();
		/**
		 * Welding new Circle for Air
		 */
		cdiCamelContext.addRoutes(addCircleRouteForAir(circleCode));
		/**
		 * Welding new Circle for CGW
		 */
		cdiCamelContext.addRoutes(addCircleRouteForCGW(circleCode));
		/**
		 * Welding new Circle for RS
		 */
		cdiCamelContext.addRoutes(addCircleRouteForRS(circleCode));
		/**
		 * Welding new Circle for
		 */
		cdiCamelContext.addRoutes(addCircleRouteForSMSAndUSSD(circleCode));

		return "Circle Added Successfully.";

	}

	private RouteBuilder addCircleRouteForAir(final String circleCode) {
		return new RouteBuilder() {

			@Override
			public void configure() throws Exception {
				final Map<String, Object> choiceDefinitionMap = choiceDefinitionStorage.getchoiceDefinitionMap();
				final ChoiceDefinition airChoiceDefinition = (ChoiceDefinition) choiceDefinitionMap
						.get(ExternalSystem.AIR.name());

				final String routeId = BusinessConstants.SUB_ROUTE_UNDERSCORE + circleCode
						+ BusinessConstants.UNDERSCORE + ExternalSystem.AIR.name();
				airChoiceDefinition.when(header(BusinessConstants.CIRCLE_CODE).isEqualTo(circleCode)).routeId(routeId)
						.to(BusinessConstants.HTTP_COMPONENT_AIR_ENDPOINT + "_" + circleCode).endChoice();

			}
		};
	}

	private RouteBuilder addCircleRouteForCGW(final String circleCode) {
		return new RouteBuilder() {

			@Override
			public void configure() throws Exception {
				final Map<String, Object> choiceDefinitionMap = choiceDefinitionStorage.getchoiceDefinitionMap();
				final ChoiceDefinition cgwChoiceDefinition = (ChoiceDefinition) choiceDefinitionMap
						.get(ExternalSystem.CGW.name());

				final String routeId = BusinessConstants.SUB_ROUTE_UNDERSCORE + circleCode
						+ BusinessConstants.UNDERSCORE + ExternalSystem.CGW.name();
				cgwChoiceDefinition.when(header(BusinessConstants.CIRCLE_CODE).isEqualTo(circleCode)).routeId(routeId)
						.to(BusinessConstants.HTTP_COMPONENT_CGW_ENDPOINT + "_" + circleCode).endChoice();

			}
		};
	}

	private RouteBuilder addCircleRouteForRS(final String circleCode) {
		return new RouteBuilder() {

			@Override
			public void configure() throws Exception {
				final Map<String, Object> choiceDefinitionMap = choiceDefinitionStorage.getchoiceDefinitionMap();
				final ChoiceDefinition rsChoiceDefinition = (ChoiceDefinition) choiceDefinitionMap
						.get(ExternalSystem.RS.name());

				final String routeId = BusinessConstants.SUB_ROUTE_UNDERSCORE + circleCode
						+ BusinessConstants.UNDERSCORE + ExternalSystem.RS.name();
				rsChoiceDefinition.when(header(BusinessConstants.CIRCLE_CODE).isEqualTo(circleCode)).routeId(routeId)
						.to(BusinessConstants.HTTP_COMPONENT_RS_ENDPOINT + "_" + circleCode).endChoice();

			}
		};
	}

	@SuppressWarnings("unchecked")
	private RouteBuilder addCircleRouteForSMSAndUSSD(final String circleCode) {
		return new RouteBuilder() {

			@Override
			public void configure() throws Exception {
				/**
				 * from routes for SMSC / USSD
				 */
				routePolicyCircleRoute.setScope(ThrottlingScope.Route);
				routePolicyCircleRoute.setMaxInflightExchanges(Integer.valueOf(PropertyUtils
						.getProperty("throtller.maxInflightExchangesForCircle")));

				final Map<String, Object> choiceDefinitionMap = choiceDefinitionStorage.getchoiceDefinitionMap();
				final ChoiceDefinition smsChoiceDefinition = (ChoiceDefinition) choiceDefinitionMap
						.get(ExternalSystemType.SMSC_TYPE.name());

				smsChoiceDefinition.when(header(BusinessConstants.CIRCLE_ID).isEqualTo(circleCode)).to(
						BusinessConstants.CAMEL_COMPONENT_TYPE + "Rx" + circleCode + "?concurrentConsumers="
								+ PropertyUtils.getProperty("route.subroute.concurrentconsumer.rx"));

				from(
						BusinessConstants.CAMEL_COMPONENT_TYPE + "Rx" + circleCode + "?concurrentConsumers="
								+ PropertyUtils.getProperty("route.subroute.concurrentconsumer.rx"))

						.routePolicy(routePolicyCircleRoute)
						.routeId(BusinessConstants.ROUTE_STR + circleCode + BusinessConstants.ROUTE_RX)
						.to(BusinessConstants.CIRCLE_QUEUE_PREFIX + circleCode
								+ BusinessConstants.CIRCLE_QUEUE_SUFFIX_INBOUND).process(requestProcessor);

				/**
				 * to routes for SMSC / USSD
				 * 
				 */

				from(
						BusinessConstants.CIRCLE_QUEUE_PREFIX + circleCode
								+ BusinessConstants.CIRCLE_QUEUE_SUFFIX_OUTBOUND).choice()
						.when(header(BusinessConstants.BIND_MODE).isEqualTo(BusinessConstants.BIND_MODE_TRX))
						.to(BusinessConstants.CAMEL_COMPONENT_TYPE + circleCode + "_routeTRx")
						.when(header(BusinessConstants.BIND_MODE).isEqualTo(BusinessConstants.BIND_MODE_TXRX))
						.to(BusinessConstants.CAMEL_COMPONENT_TYPE + circleCode + "Tx");

				final int maximumRedeiveryAttempt = Integer.valueOf(PropertyUtils
						.getProperty("route.maximum.redelivery.attempt"));
				final int maximumRedeliveryInterval = Integer.valueOf(PropertyUtils
						.getProperty("route.redelivery.delay"));

				from(BusinessConstants.CAMEL_COMPONENT_TYPE + circleCode + "_routeTRx")
						.process(responseProcessor)
						.onException(org.jsmpp.InvalidResponseException.class,
								org.jsmpp.extra.ResponseTimeoutException.class,
								org.jsmpp.extra.NegativeResponseException.class, java.net.ConnectException.class,
								java.io.IOException.class).maximumRedeliveries(maximumRedeiveryAttempt)
						.redeliveryDelay(maximumRedeliveryInterval).onRedelivery(logRetryProcessor).end()
						.process(outBoundQueueProcessor).process(receiptProcessor);

				from(BusinessConstants.CAMEL_COMPONENT_TYPE + circleCode + "Tx").choice()
						.when(header(BusinessConstants.SERVICE_TYPE).isEqualTo(BusinessConstants.SERVICE_TYPE_SMS))
						.log("Got Message in SMS(SMS) Case")
						.to(BusinessConstants.CAMEL_COMPONENT_TYPE + circleCode + BusinessConstants.ROUTE_TX_SMS)
						.when(header(BusinessConstants.SERVICE_TYPE).isEqualTo(BusinessConstants.SERVICE_TYPE_USSD))
						.log("Got Message in USSD Case")
						.to(BusinessConstants.CAMEL_COMPONENT_TYPE + circleCode + BusinessConstants.ROUTE_TX_USSD);

			}
		};
	}

	private String getCircleCodeByCircleName(final String circleName) {
		final FDPCircleDTO fdpCircleDTO = fdpCircleDAO.findCircleByCircleName(circleName);
		return fdpCircleDTO.getCircleCode();
	}

}
