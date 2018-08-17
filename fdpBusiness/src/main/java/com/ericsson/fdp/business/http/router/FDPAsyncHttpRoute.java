package com.ericsson.fdp.business.http.router;

import java.util.List;

import javax.annotation.PostConstruct;
import javax.inject.Inject;

import org.apache.camel.Exchange;
import org.apache.camel.ExchangePattern;
import org.apache.camel.Processor;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.cdi.CdiCamelContext;
import org.apache.commons.httpclient.HostConfiguration;
import org.apache.commons.httpclient.MultiThreadedHttpConnectionManager;
import org.apache.commons.httpclient.params.HttpConnectionManagerParams;

import com.ericsson.fdp.business.route.processor.EnrichRequestParam;
import com.ericsson.fdp.business.route.processor.ExecuteMMSPProcessor;
import com.ericsson.fdp.business.route.processor.MobileMoneyWhiteListIpProcessor;
import com.ericsson.fdp.business.route.processor.ivr.impl.MobileMoneyAuthenticationProcessor;
import com.ericsson.fdp.common.enums.ExternalSystem;
import com.ericsson.fdp.core.config.utils.PropertyUtils;
import com.ericsson.fdp.core.utils.ApplicationConfigUtil;
import com.ericsson.fdp.dao.fdpadmin.FDPCircleDAO;
import com.ericsson.fdp.dao.fdpadmin.FDPMobileMoneyConfigDAO;
import com.ericsson.fdp.route.cdi.context.provider.CdiCamelContextProvider;

public class FDPAsyncHttpRoute {
	/** The cdi camel context provider. */
	@Inject
	private CdiCamelContextProvider cdiCamelContextProvider;

	private String FDPAsyncURL = "servlet:///FDPAsycService?servletName=FDPAsycService&matchOnUriPrefix=false";

	
	/** The fdp air config dao. */
	@Inject
	private FDPMobileMoneyConfigDAO fdpMobileMoneyConfigDAO;

	/** The MM whiteList IP processor. */
	@Inject
	private MobileMoneyWhiteListIpProcessor fulfillmentWhiteListIpProcessor;

	@Inject
	private MobileMoneyAuthenticationProcessor mobilemoneyAuthenticationProcessor;

	@Inject
	private ExecuteMMSPProcessor executemmspprocessor;

	/** The context. */
	private CdiCamelContext context;


	@Inject
	private EnrichRequestParam enrichRequestParam;
	
	
	@Inject
	private FDPCircleDAO fdpCircleDAO;
	
	@PostConstruct
	public void createRoute() throws Exception {
		final CdiCamelContext context = cdiCamelContextProvider.getContext();
		context.addRoutes(createRoutes());
	}

	public RouteBuilder createRoutes() {
		return new RouteBuilder() {
		
			
			//handled(true).transform().constant("ERROR");
			
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
				 * http://IP:port/cisBusiness/FDPAsycService/FDPAsyncHttpService?username=098f6bcd4621d373cade4e832627b4f6&password=0f359740bd1cda994f8b55330c86d845&transaction_id=0&iname=EMA
				 */
				from(FDPAsyncURL)
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
						.process(mobilemoneyAuthenticationProcessor)

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
								exchange.getOut().setHeader(Exchange.HTTP_RESPONSE_CODE,400);
								exchange.getOut().setBody("Fail to process the Request");

							}
						}).end();

			}

			private Integer readIntPropertyValue(final String propertyName,
					final String defaultValue) {
				String propertyValue = PropertyUtils.getProperty(propertyName);
				propertyValue = (null == propertyValue || ""
						.equals(propertyValue)) ? defaultValue : propertyValue;
				return Integer.valueOf(propertyValue);
			}

			private MultiThreadedHttpConnectionManager getHttpConnectionManager() {
				final int maxTotalConnection = readIntPropertyValue(
						"http.maxtotalconnections", "200");
				final int defaultMaxConnectionPerHost = readIntPropertyValue(
						"http.default.max.total.connections.per.host", "5");
				final int maxConnectionPerHost = readIntPropertyValue(
						"http.max.connection.per.host", "20");
				final int httpSoTimeOut = readIntPropertyValue(
						"http.socket.timeout", "10000");
				final int connectionIdleCloseTime = readIntPropertyValue(
						"http.close.idle.time", "6000");
				final int connectionTimeout = readIntPropertyValue(
						"http.connection.timeout", "10000");

				final HttpConnectionManagerParams httpConnectionManagerParams = new HttpConnectionManagerParams();
				httpConnectionManagerParams
						.setMaxTotalConnections(maxTotalConnection);
				httpConnectionManagerParams
						.setDefaultMaxConnectionsPerHost(defaultMaxConnectionPerHost);
				httpConnectionManagerParams.setMaxConnectionsPerHost(
						HostConfiguration.ANY_HOST_CONFIGURATION,
						maxConnectionPerHost);
				httpConnectionManagerParams
						.setConnectionTimeout(connectionTimeout);
				httpConnectionManagerParams.setSoTimeout(httpSoTimeOut);

				final MultiThreadedHttpConnectionManager httpConnectionManager = new MultiThreadedHttpConnectionManager();
				httpConnectionManager
						.closeIdleConnections(connectionIdleCloseTime);
				httpConnectionManager.setParams(httpConnectionManagerParams);
				return httpConnectionManager;
			}

		};
	}

	/**
	 * This method is used to return all available circle codes.
	 * 
	 * @return circle codes.
	 */
	private List<String> getAllCircleCodes() {
		return fdpCircleDAO.getAllCircleCodes();
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
