package com.ericsson.fdp.business.http.router;

import javax.annotation.PostConstruct;
import javax.ejb.Singleton;
import javax.ejb.Startup;
import javax.inject.Inject;

import org.apache.camel.Exchange;
import org.apache.camel.ExchangePattern;
import org.apache.camel.Processor;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.cdi.CdiCamelContext;

import com.ericsson.fdp.business.route.processor.EnrichRequestParam;
import com.ericsson.fdp.business.route.processor.ExecuteMMSPProcessor;
import com.ericsson.fdp.business.route.processor.MobileMoneyWhiteListIpProcessor;
import com.ericsson.fdp.business.route.processor.ivr.impl.MobileMoneyBasicAuthenticationProcessor;
import com.ericsson.fdp.common.enums.ExternalSystem;
import com.ericsson.fdp.core.utils.ApplicationConfigUtil;
import com.ericsson.fdp.route.cdi.context.provider.CdiCamelContextProvider;

@Startup
@Singleton(name = "FDPMobileMoneyHttpRoute")
public class FDPMobileMoneyHttpRoute {
	/** The cdi camel context provider. */
	@Inject
	private CdiCamelContextProvider cdiCamelContextProvider;

	private String MobileMoneyHttpSevlet = "servlet:///MobileMoneyHttpService?servletName=MobileMoneyHttpService&matchOnUriPrefix=true";

	@Inject
	private EnrichRequestParam enrichRequestParam;

	/** The MM whiteList IP processor. */
	@Inject
	private MobileMoneyWhiteListIpProcessor fulfillmentWhiteListIpProcessor;

	@Inject
	private ExecuteMMSPProcessor executemmspprocessor;
	
	@Inject
	private MobileMoneyBasicAuthenticationProcessor mobileMoneyBasicAuthenticationProcessor;

	/** The context. */
	private CdiCamelContext context;

	@PostConstruct
	public void createRoute() throws Exception {
		final CdiCamelContext context = cdiCamelContextProvider.getContext();
		context.addRoutes(createRoutes());
	}

	public RouteBuilder createRoutes() {
		return new RouteBuilder() {
		
			@Override
			public void configure() throws Exception {
				final boolean autostartup = true;

				onException(Exception.class).handled(true).process(
						new Processor() {

							@Override
							public void process(Exchange exchange)
									throws Exception {
								exchange.getOut().setBody("ERROR");

							}
						});

				// body after execution of request param will contain
				// FDPRequestImpl.java

				/***************
				 * Route that will listen on******************** Format>>
				 * IP:PORT/fdpBusiness/MobileMoneyHttpService/MobileMoneyHttp
				 */
				from(MobileMoneyHttpSevlet)
						.routeId(ExternalSystem.MM + "_MAIN")
						.autoStartup(autostartup)
						.setExchangePattern(ExchangePattern.InOut)

						.doTry()
						/**
						 * Check wheather URL hit come from valid MM IP that
						 * will get the cached request in RequestWEB cache
						 */
						.process(fulfillmentWhiteListIpProcessor)

						/**
						 * Check wheather URL hit come from valid MM username
						 * and password
						 */
						//.process(mobilemoneyAuthenticationProcessor) changes by rahul for basic authentication check
						.process(mobileMoneyBasicAuthenticationProcessor)

						.convertBodyTo(String.class)
						/**
						 * Processor that will get the cached request in
						 * RequestWEB cache
						 */
						
						.process(enrichRequestParam)

						/** Processor that will get the cached request in */
						.process(executemmspprocessor).doCatch(Exception.class)
						.process(new Processor() {

							@Override
							public void process(Exchange exchange)
									throws Exception {
								
								String response;
								if (exchange.getOut().getHeader(Exchange.HTTP_RESPONSE_CODE)!=null && ((Integer)exchange.getOut().getHeader(Exchange.HTTP_RESPONSE_CODE)).equals(new Integer(404)))
								{
									exchange.getOut().setHeader(Exchange.HTTP_RESPONSE_CODE,500);
									response= "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?><ns0:errorResponse errorcode=\"COMMUNICATION_ERROR\" xmlns:ns0=\"http://www.ericsson.com/em/emm/callback/v1_0\"/>";	
								}else if (exchange.getIn().getHeader(Exchange.HTTP_RESPONSE_CODE)!=null && ((Integer)exchange.getIn().getHeader(Exchange.HTTP_RESPONSE_CODE)).equals(new Integer(555)))
								{
									exchange.getOut().setHeader(Exchange.HTTP_RESPONSE_CODE, "200");
									
									response="<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?><ns2:debitcompletedresponse xmlns:ns2=\"http://www.ericsson.com/em/emm\"/>";
								}
								else
								{
									exchange.getOut().setHeader(Exchange.HTTP_RESPONSE_CODE,404);
									response="Excpetion Occured";
								}
								exchange.getOut().setBody(response);
								
							}
						}).end();
			}
		};
	}

	/**
	 * Description restart Route The route
	 * 
	 * @throws Exception
	 */

	public void stopAllRoutes() throws Exception {
		context = ApplicationConfigUtil.getCdiCamelContextProvider()
				.getContext();
		context.stopRoute(ExternalSystem.MM + "_MAIN");
		context.removeRoute(ExternalSystem.MM + "_MAIN");

	}
}
