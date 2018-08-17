package com.ericsson.fdp.business.http.router;

import javax.inject.Inject;

import org.apache.camel.LoggingLevel;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.cdi.CdiCamelContext;
import org.apache.camel.impl.ThrottlingInflightRoutePolicy;
import org.apache.camel.model.RouteDefinition;
import org.apache.camel.util.CamelLogger;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ericsson.fdp.business.enums.ivr.FulfillmentRouteEnum;
import com.ericsson.fdp.business.route.processor.EnrichRequestParam;
import com.ericsson.fdp.business.route.processor.ExecuteMMSPProcessor;
import com.ericsson.fdp.business.route.processor.FulfillmentChannelParameterProcessor;
import com.ericsson.fdp.business.route.processor.FulfillmentServiceRequestProcessor;
import com.ericsson.fdp.business.route.processor.FulfillmentWhiteListIpProcessor;
import com.ericsson.fdp.business.route.processor.IVRServiceRequestProcessor;
import com.ericsson.fdp.business.route.processor.ivr.impl.AsyncAuthenticationProcessor;
import com.ericsson.fdp.business.route.processor.ivr.impl.FulfillmentActionMandatoryParameterProcessor;
import com.ericsson.fdp.business.route.processor.ivr.impl.FulfillmentAuthenticationProcessor;
import com.ericsson.fdp.business.route.processor.ivr.impl.FulfillmentCircleAndMSISDNValidateProcessor;
import com.ericsson.fdp.business.route.processor.ivr.impl.FulfillmentExceptionProcessor;
import com.ericsson.fdp.business.route.processor.ivr.impl.FulfillmentMandatoryParameterProcessor;
import com.ericsson.fdp.business.route.processor.ivr.impl.FulfillmentOptionalParameterValidateProcessor;
import com.ericsson.fdp.business.route.processor.ivr.impl.SMSCActivationServiceProcessor;
import com.ericsson.fdp.business.route.processor.ivr.impl.commandservice.FulfillmentINameParameterProcessor;
import com.ericsson.fdp.business.route.processor.ivr.impl.commandservice.IVRCommandServiceProcessor;
import com.ericsson.fdp.business.route.processor.requestidgenerator.BuyProductRequestIdGenerator;
import com.ericsson.fdp.business.route.processor.requestidgenerator.CommandServiceRequestIdGenerator;
import com.ericsson.fdp.business.route.processor.requestidgenerator.RequestIDGeneratorAbility;
import com.ericsson.fdp.business.route.processor.requestidgenerator.RequestIdGeneratorForFulfillment;
import com.ericsson.fdp.route.cdi.context.provider.CdiCamelContextProvider;

/**
 * The Class FullfillmentServiceRoute is responsible for creating FDP Public for
 * Product buy , Command Service and fullfillment engine.
 * 
 * @author Ericsson
 */
public class FulfillmentServiceRoute {

	/** The cdi camel context provider. */
	@Inject
	private CdiCamelContextProvider cdiCamelContextProvider;

	/** The Constant LOGGER. */
	private static final Logger LOGGER = LoggerFactory.getLogger(FulfillmentServiceRoute.class);

	/** The validate user processor. */
	@Inject
	private IVRServiceRequestProcessor ivrServiceRequestProcessor;

	/** The ivr whiteList IP processor. */
	@Inject
	private FulfillmentWhiteListIpProcessor fulfillmentWhiteListIpProcessor;

	/** The ivri name parameter processor. */
	@Inject
	private FulfillmentINameParameterProcessor fulfillmentInameParameterProcessor;

	/** The ivr mandatory parameter processor. */
	@Inject
	private FulfillmentMandatoryParameterProcessor fulfillmentMandatoryParameterProcessor;
	
	@Inject
	private FulfillmentChannelParameterProcessor fulfillmentChannelParameterProcessor;

	/** The ivr authentication processor. */
	@Inject
	private FulfillmentAuthenticationProcessor fulfillmentAuthenticationProcessor;
	
	/** The async command authentication processor. */
	@Inject
	private AsyncAuthenticationProcessor asyncAuthenticationProcessor;

	/** The ivr circle and msisdn validate processor. */
	@Inject
	private FulfillmentCircleAndMSISDNValidateProcessor fulfillmentCircleAndMSISDNValidateProcessor;

	/** The buy product request id generator. */
	@Inject
	private BuyProductRequestIdGenerator buyProductRequestIdGenerator;

	
	@Inject
	private RequestIDGeneratorAbility requestIdGeneratorForAbility;
	
	
	/** The command service request id generator. */
	@Inject
	private CommandServiceRequestIdGenerator commandServiceRequestIdGenerator;

	/** The ivr command service processor. */
	@Inject
	private IVRCommandServiceProcessor ivrCommandServiceProcessor;

	/** The ivr exception processor. */
	@Inject
	private FulfillmentExceptionProcessor fulfillmentExceptionProcessor;

	/** The throttling inflight route policy. */
	private ThrottlingInflightRoutePolicy throttlingInflightRoutePolicy = null;

	@Inject
	private RequestIdGeneratorForFulfillment requestIdGeneratorForFullfillment;

	@Inject
	private FulfillmentServiceRequestProcessor fulfillmentServiceRequestProcessor;
	
	@Inject
	private FulfillmentActionMandatoryParameterProcessor fulfillmentActionMandatoryParameterProcessor;
	
	@Inject
	private	FulfillmentOptionalParameterValidateProcessor fulfillmentOptionalParameterValidateProcessor;

	
	@Inject
	private ExecuteMMSPProcessor executemmspprocessor;

	@Inject
	private EnrichRequestParam enrichRequestParam;
	
	@Inject
	private SMSCActivationServiceProcessor smscActivationServiceProcessor;

	
	
	/**
	 * Creates the routes.
	 * 
	 * @throws Exception
	 *             the exception
	 */
	public void createRoutes() throws Exception {
		final CdiCamelContext context = cdiCamelContextProvider.getContext();
		for (final FulfillmentRouteEnum ivr : FulfillmentRouteEnum.values()) {
			LOGGER.debug("creating {} route.", ivr.getRouteId());
			context.addRoutes(createIVRServiceRoute(ivr));
			LOGGER.debug("{} route created.", ivr.getRouteId());
		}
	}

	/**
	 * Creates the ivr service route.
	 * 
	 * @param ivr
	 *            the ivr
	 * @return the route builder
	 */
	private RouteBuilder createIVRServiceRoute(final FulfillmentRouteEnum ivr) {
		throttlingInflightRoutePolicy = new ThrottlingInflightRoutePolicy();
		final RouteBuilder routeBuilder = getRouteBuilder();
		routeBuilder.onException(Exception.class).process(fulfillmentExceptionProcessor).end();
		final RouteDefinition route = routeBuilder.from(ivr.getFromURL()).routeId(ivr.getRouteId());
		// As per confirmation from Manmeet,expecting configuration to be always
		// OK.
		// So no need to handle NumberFormatException
		throttlingInflightRoutePolicy.setMaxInflightExchanges(Integer.parseInt(ivr.getMaxInFlightNumber()));
		throttlingInflightRoutePolicy.setResumePercentOfMax(Integer.parseInt(ivr.getMaxInflighPercentage()));
		CamelLogger camelLogger = throttlingInflightRoutePolicy.getLogger();
		camelLogger.setLevel(LoggingLevel.WARN);
		throttlingInflightRoutePolicy.setLogger(camelLogger);
		route.routePolicy(throttlingInflightRoutePolicy);

		switch (ivr) {
		case IVR_PRODUCT_BUY_SERVICE:
			route.process(fulfillmentWhiteListIpProcessor).process(fulfillmentInameParameterProcessor)
					.process(buyProductRequestIdGenerator).process(fulfillmentMandatoryParameterProcessor)
					.process(fulfillmentAuthenticationProcessor).process(fulfillmentCircleAndMSISDNValidateProcessor)
					.process(ivrServiceRequestProcessor);
			break;
		case IVR_COMMAND_SERVICE:
			route.process(fulfillmentWhiteListIpProcessor).process(fulfillmentInameParameterProcessor)
					.process(commandServiceRequestIdGenerator).process(fulfillmentMandatoryParameterProcessor)
					.process(fulfillmentAuthenticationProcessor).process(fulfillmentCircleAndMSISDNValidateProcessor)
					.process(ivrCommandServiceProcessor);
			break;
		case FULFILLMENT_SERVICE:
					 route
					.process(fulfillmentWhiteListIpProcessor)
					//.process(fulfillmentInameParameterProcessor)
					.process(requestIdGeneratorForFullfillment).process(fulfillmentMandatoryParameterProcessor).process(fulfillmentActionMandatoryParameterProcessor)
					.process(fulfillmentAuthenticationProcessor).process(fulfillmentCircleAndMSISDNValidateProcessor)
					.process(fulfillmentOptionalParameterValidateProcessor).process(fulfillmentServiceRequestProcessor);
					 
		break;
		case ASYCREQUEST_ROUTE:
			route
			.process(fulfillmentWhiteListIpProcessor)
			.process(requestIdGeneratorForFullfillment)
			.process(fulfillmentMandatoryParameterProcessor)
			.process(asyncAuthenticationProcessor)
			.process(enrichRequestParam)
			.process(executemmspprocessor);
		break;
		//	.process(fulfillmentAuthenticationProcessor)
		/*case ABILITY_SERVICE:
			route
			.process(fulfillmentWhiteListIpProcessor)
			.process(requestIdGeneratorForAbility)
			.process(fulfillmentChannelParameterProcessor)
			.process(fulfillmentMandatoryParameterProcessor)
			.process(fulfillmentAuthenticationProcessor)
			.process(fulfillmentCircleAndMSISDNValidateProcessor)
			.process(fulfillmentServiceRequestProcessor)
			;
			
			break;*/
		case SMSC_ACTIVATION_SERVICE:
			route
			.process(smscActivationServiceProcessor);
		break;
		default:
			break;
		}
		return routeBuilder;
	}

	/**
	 * Gets the route builder.
	 * 
	 * @return the route builder
	 */
	private RouteBuilder getRouteBuilder() {
		return new RouteBuilder() {
			@Override
			public void configure() throws Exception {
			}
		};
	}

}
