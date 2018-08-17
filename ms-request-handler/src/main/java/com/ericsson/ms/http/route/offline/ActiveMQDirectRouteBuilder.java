package com.ericsson.ms.http.route.offline;

import org.springframework.stereotype.Component;

import com.ericsson.ms.common.constants.RoutingConstant;
import com.ericsson.ms.http.route.framework.AbstractRouteBuilder;

/**
 * The Route Builder for Active MQ
 * 
 * @author Ericsson
 *
 */
@Component("ActiveMQDirectRouteBuilder")
public class ActiveMQDirectRouteBuilder extends AbstractRouteBuilder {

	@Override
	public void configure() throws Exception {
		from(RoutingConstant.DIRECT_ACTIVEMQ).to(getActivemqQueueName(header("queueName").toString()));
	}

	/**
	 * 
	 * @param queueName
	 * @return The Active Mq Endpoint
	 */
	private String getActivemqQueueName(String queueName) {
		return "activemq:" + queueName;
	}

}
