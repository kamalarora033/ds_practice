package com.ericsson.fdp.business.constants;

import java.util.Arrays;
import java.util.List;

import com.ericsson.fdp.common.enums.ChannelType;

/**
 * The Class NotificationConstants contains all constants that will used through
 * out the Business application.
 */
public final class NotificationConstants {

	/**
	 * Instantiates a new notification constants.
	 */
	private NotificationConstants() {

	}

	/** The Constant CIRCLE_ID. */
	public static final String CIRCLE_ID = "CIRCLE_ID";

	/** The Constant SERVICE_MODE_TYPE. */
	public static final String SERVICE_MODE_TYPE = "MSG_MODE_TYPE"; // SMS or
																	// USSD

	/** The Constant REQUEST_ID. */
	public static final String REQUEST_ID = "REQUEST_ID";

	/** The Constant MSISDN. */
	public static final String MSISDN = "MSISDN";

	/** The Constant DEST_ADDR. */
	public static final String DEST_ADDR = "DEST_ADDR";

	/** The Constant SOURCE_ADDR. */
	public static final String SOURCE_ADDR = "SOURCE_ADDR";

	/** The Constant JMS_COMPONENT_NAME. */
	public static final String JMS_COMPONENT_NAME = "jms";

	/** The Constant CIRCLE_QUEUE_PREFIX. */
	public static final String CIRCLE_QUEUE_PREFIX = "jms:queue:";

	/** The Constant CIRCLE_QUEUE_SUFFIX_INBOUND. */
	public static final String CIRCLE_QUEUE_SUFFIX_INBOUND = "_SMSQueueOut";

	/** The Constant CIRCLE_QUEUE_SUFFIX_OUTBOUND. */
	public static final String CIRCLE_QUEUE_SUFFIX_OUTBOUND = "_SMSQueueIn";

	/** The Constant DEAD_LETTER_QUEUE_NAME. */
	public static final String DEAD_LETTER_QUEUE_NAME = "jms:queue:deadletterqueue";

	/** The Constant CONNECTION_FACTORY. */
	public static final String CONNECTION_FACTORY = "java:/ConnectionFactory";

	/** The Constant PROPERTY_MSISDN_LOWER_BOUND. */
	public static final String PROPERTY_MSISDN_LOWER_BOUND = "fdp.msisdn.lowerbound";

	/** The Constant PROPERTY_MSISDN_UPPER_BOUND. */
	public static final String PROPERTY_MSISDN_UPPER_BOUND = "fdp.msisdn.upperbound";

	/** The Constant SERVICE_TYPE_USSD. */
	public static final String SERVICE_TYPE_USSD = "USSD";

	/** The Constant SERVICE_TYPE_SMS. */
	public static final String SERVICE_TYPE_SMS = "WAP";

	/** The Constant BIND_NODE_TYPE_PRODUCER. */
	public static final String BIND_NODE_TYPE_PRODUCER = "producer";

	/** The Constant BIND_NODE_TYPE_CONSUMER. */
	public static final String BIND_NODE_TYPE_CONSUMER = "consumer";

	/** The Constant CIRCLE_NAME. */
	public static final String CIRCLE_NAME = "CIRCLE_NAME";

	/** The Constant _RX. */
	public static final String _RX = "_Rx";

	/** The Constant MAIN_ROUTE. */
	public static final String MAIN_ROUTE = "mainroute";

	/** The Constant ROUTE_. */
	public static final String ROUTE_ = "route_";

	/** The Constant CAMEL_COMPONENT_TYPE. It can be vm , seda or direct */
	public static final String CAMEL_COMPONENT_TYPE = "vm:";

	/** The Constant for PAM Cleanup Module */
	public static final String SUCCESS = "SUCCESS";
	public static final String FAIL = "FAIL";
	public static final String PAM_REQUESTED_ID = "PAM_RequestId";
	public static final String PAM_TRANSACTION_ID = "PAM_TransactionId";

	/**
	 * The channels on which a product bought, notification is to be sent.
	 */
	public static final List<ChannelType> NOTIFICATION_PRODUCT_BUY_CHANNELS = Arrays.asList(ChannelType.USSD,
			ChannelType.WEB,ChannelType.IVR,ChannelType.SMS);

	public static final Long NOTIFICATION_FOR_NON_ICR_CIRCLE = -5L;
}
