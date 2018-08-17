package com.ericsson.ms.http.route.offline;

import org.apache.activemq.camel.component.ActiveMQComponent;
import org.apache.camel.ExchangePattern;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import com.ericsson.ms.common.constants.RoutingConstant;
import com.ericsson.ms.common.util.ReqHandlerLoggerUtil;
import com.ericsson.ms.http.route.framework.AbstractRouteBuilder;
import com.ericsson.ms.http.route.processor.CheckMandatoryParamProcessor;
import com.ericsson.ms.http.route.processor.EventNotificationProcessor;
import com.ericsson.ms.http.route.processor.MSResponseProcessor;
import com.ericsson.ms.http.route.processor.RequestIdGeneratorProcessor;

/**
 * The Route builder for Offline Event Notification
 * 
 * @author Ericsson
 * 
 */
@Component("OfflineEventNotificationRouteBuilder")
public class OfflineEventNotificationRouteBuilder extends AbstractRouteBuilder {

	private static Logger objLogger = LoggerFactory.getLogger(OfflineEventNotificationRouteBuilder.class);
	/**
	 * host ip
	 */
	@Value("${host.ip}")
	private String hostIP;

	/**
	 * port number
	 */
	@Value("${host.port}")
	private String portNumber;

	/**
	 * context path
	 */
	@Value("${host.offline.contextPath}")
	private String contextPath;

	/**
	 * activemq queue name
	 */
	@Value("${offline.activemq.queue.name}")
	private String queueName;

	/**
	 * activemq broker URl
	 */
	@Value("${activemq.broker.url}")
	private String brokerUrl;

	/** The CheckMandatoryParamProcessor field */
	@Autowired
	private CheckMandatoryParamProcessor checkMandatoryParamProcessor;

	/** The RequestIdGeneratorProcessor field */
	@Autowired
	private RequestIdGeneratorProcessor requestIdGeneratorProcessor;

	/** The EventNotificationProcessor field */
	@Autowired
	private EventNotificationProcessor eventNotificationProcessor;
	
	/** The MS response Processor field */
	@Autowired
	private MSResponseProcessor msResponseProcessor;

	@SuppressWarnings("unchecked")
	@Override
	public void configure() {
		String endPoint = getOfflineEndpoint();
		configureActiveMQComponent();
		ReqHandlerLoggerUtil.debug(objLogger, getClass(), "configure()", "Configuring Offline route .... " + endPoint);
		from(endPoint).setExchangePattern(ExchangePattern.InOut).autoStartup(true)				
				.onException(org.apache.camel.http.common.HttpOperationFailedException.class,
						java.net.SocketException.class, javax.jms.JMSException.class)				
				.handled(true)
				.process(msResponseProcessor).end()
				.process(checkMandatoryParamProcessor).process(requestIdGeneratorProcessor).process(eventNotificationProcessor)
				.choice().when(header(RoutingConstant.PUSH_TO_QUEUE_HEADER).isEqualTo(true))
				.to(getActivemqQueueName())
				.endChoice().end()
				.process(msResponseProcessor).end();
	}

	/**
	 * Get the queue name
	 * 
	 * @return
	 */
	private String getActivemqQueueName() {
		return "activemq:" + queueName + "?exchangePattern=InOnly";
	}

	/**
	 * Get Offline Route endpoint
	 * 
	 * @return
	 */
	private String getOfflineEndpoint() {
		return RoutingConstant.JETTY_COMPONENT + hostIP + ":" + portNumber + "/" + contextPath;
	}

	/**
	 * Configure Active MQ Component
	 */
	private void configureActiveMQComponent() {
		ActiveMQComponent activeMQComponent = getContext().getComponent("activemq", ActiveMQComponent.class);
		activeMQComponent.setBrokerURL(brokerUrl);
	}

}
