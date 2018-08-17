package com.ericsson.fdp.business.constants;

/**
 * The Class NotificationConstants contains all constants that will used through
 * out the Business application.
 * 
 * @author Ericsson
 */
public final class BusinessConstants {

	public static final String DIRECT_COMPONENT_MOBILEMONEY_ENDPOINT = "direct:httpsMobileMoney";

	/** Instantiates a new business constants. */
	private BusinessConstants() {
		super();
	}

	public static final String HTTPS4_COMPONENT_TYPE = "https4://";

	/** HTTPS 4 component */

	/** The Constant CIRCLE_ID. */
	public static final String CIRCLE_ID = "CIRCLE_ID";

	/** The Constant CIRCLE_CODE. */
	public static final String CIRCLE_CODE = "CIRLCE_CODE";

	/** The Constant SERVICE_MODE_TYPE. */
	public static final String SERVICE_MODE_TYPE = "MSG_MODE_TYPE"; // SMS or
																	// USSD

	/** The Constant REQUEST_ID. */
	public static final String REQUEST_ID = "RID";

	/** The Constant MSISDN. */
	public static final String MSISDN = "MSISDN";

	/** The Constant DEST_ADDR. */
	public static final String DEST_ADDR = "DEST_ADDR";

	/** The Constant SESSION_ID. */
	public static final String SESSION_ID = "SESSION_ID";

	/** The Constant SOURCE_ADDR. */
	public static final String SOURCE_ADDR = "SOURCE_ADDR";

	/** The Constant JMS_COMPONENT_NAME. */
	public static final String JMS_COMPONENT_NAME = "jms";

	/** The Constant CIRCLE_QUEUE_PREFIX. */
	public static final String CIRCLE_QUEUE_PREFIX = "jms:queue:";

	/** The Constant CIRCLE_QUEUE_SUFFIX_INBOUND. */
	public static final String CIRCLE_QUEUE_SUFFIX_OUTBOUND = "_SMSQueueOut";

	/** The Constant CIRCLE_QUEUE_SUFFIX_OUTBOUND. */
	public static final String CIRCLE_QUEUE_SUFFIX_INBOUND = "_SMSQueueIn";

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
	public static final String BIND_NODE_TYPE_TRANSMITTER = "transmitter";

	/** The Constant BIND_NODE_TYPE_CONSUMER. */
	public static final String BIND_NODE_TYPE_RECEIVER = "receiver";

	/** The Constant CIRCLE_NAME. */
	public static final String CIRCLE_NAME = "CIRCLE_NAME";

	/** The Constant _RX. */
	public static final String ROUTE_RX = "_Rx";

	/** The Constant MAIN_ROUTE. */
	public static final String MAIN_ROUTE = "mainroute";

	/** The Constant ROUTE_TX. */
	public static final String ROUTE_TX = "Tx";

	/** The Constant ROUTE_TX_USSD. */
	public static final String ROUTE_TX_USSD = "Tx_USSD";

	/** The Constant ROUTE_TX_SMS. */
	public static final String ROUTE_TX_SMS = "Tx_SMS";

	/** The Constant ROUTE_RX_USSD. */
	public static final String ROUTE_RX_USSD = "Rx_USSD";

	/** The Constant ROUTE_RX_SMS. */
	public static final String ROUTE_RX_SMS = "Rx_SMS";

	/** The Constant ROUTE_. */
	public static final String ROUTE_STR = "route_";

	/** The Constant CAMEL_COMPONENT_TYPE. It can be vm , seda or direct */
	public static final String CAMEL_COMPONENT_TYPE = "seda:";

	/** The Constant CIRCLE_CODE_NAME_MAP. */
	public static final String CIRCLE_CODE_NAME_MAP = "circleCodeNameMap";

	/** The Constant THROTTLER_DATA_EXCHANGES_MAP. */
	public static final String THROTTLER_DATA_EXCHANGES_MAP = "thorttlerDataExchanges";

	/** The Constant MAX_INFLIGHT_EXCHANGES_MAP. */
	public static final String MAX_INFLIGHT_EXCHANGES_MAP = "maxInflightExchanges";

	/** The Constant SUBMIT_SM. */
	public static final String SUBMIT_SM = "SubmitSm";

	/** The Constant ROUTE. */
	public static final String ROUTE = "route";

	/** The Constant SMPP_COMPONENT. */
	public static final String SMPP_COMPONENT = "smpp://";

	/** The Constant AT_THE_RATE. */
	public static final String AT_THE_RATE = "@";

	/** The Constant AMPERSAND_PARAMETER_APPENDER. */
	public static final String AMPERSAND_PARAMETER_APPENDER = "&";

	/** The Constant COLON. */
	public static final String COLON = ":";
	
	public static final String USE_FIXED_DELAY = "useFixedDelay=";

	/** The Constant QUERY_STRING_SEPARATOR. */
	public static final String QUERY_STRING_SEPARATOR = "?";

	/** The Constant EQUALS. */
	public static final String EQUALS = "=";

	/** The Constant PASSWORD. */
	public static final String PASSWORD = "password";

	/** The Constant ENQUIRE_LINK_TIMER. */
	public static final String ENQUIRE_LINK_TIMER = "enquireLinkTimer";

	/** The Constant TRANSACTION_TIMER. */
	public static final String TRANSACTION_TIMER = "transactionTimer";

	/** The Constant SYSTEM_TYPE. */
	public static final String SYSTEM_TYPE = "systemType";

	/** The Constant SERVICE_TYPE. */
	public static final String SERVICE_TYPE = "serviceType";

	/** The Constant SOURCE_ADDRESS_TON. */
	public static final String SOURCE_ADDRESS_TON = "sourceAddrTon";

	/** The Constant SOURCE_ADD_NPI. */
	public static final String SOURCE_ADD_NPI = "sourceAddrNpi";

	/** The Constant HTTP_COMPONENT. */
	public static final String HTTP_COMPONENT = "http";

	/** The Constant NETTY_COMPONENT. */
	public static final String NETTY_COMPONENT = "netty";

	/** The Constant HTTP_COMPONENT_AIR_ENDPOINT. */
	public static final String HTTP_COMPONENT_AIR_ENDPOINT = "direct:httpAIR";
	
	/** The Constant HTTP_COMPONENT_DMC_ENDPOINT. */
	public static final String HTTP_COMPONENT_DMC_ENDPOINT = "direct:httpDMC";

	/** The Constant HTTP_COMPONENT_MM_ENDPOINT. */
	public static final String HTTP_COMPONENT_MM_ENDPOINT = "direct:httpMM";

	/** The Constant HTTP_COMPONENT_EVDS_ENDPOINT. */
	public static final String NETTY_COMPONENT_EVDS_ENDPOINT = "direct:nettyEVDS";

	/** The Constant HTTP_COMPONENT_TYPE. */
	public static final String HTTP_COMPONENT_TYPE = "http://";

	/** The Constant NETTY_COMPONENT_TYPE. */
	public static final String NETTY_COMPONENT_TYPE = "netty:tcp://";

	/** The Constant HTTP Client Socket Time out. */
	public static final String HTTP_CLIENT_SO_TIMEOUT = "httpClient.soTimeout";

	/** The Constant NETTY TCP Client Socket Time out. */
	public static final String NETTY_CLIENT_TIMEOUT = "connectTimeout";

	/** The Constant NETTY Client Sync mode. */
	public static final String NETTY_CLIENT_SYNC_MODE = "sync";
	
	/** The Constant NETTY Client Keep Alive mode. */
	public static final String NETTY_CLIENT_KEEP_ALIVE_MODE = "keepAlive";

	/** The Constant FORWARD_SLASH. */
	public static final String FORWARD_SLASH = "/";

	/** The Constant CGW_COMPONENT_AIR_ENDPOINT. */
	public static final String HTTP_COMPONENT_CGW_ENDPOINT = "direct:httpCGW";

	/** The Constant RS_COMPONENT_AIR_ENDPOINT. */
	public static final String HTTP_COMPONENT_RS_ENDPOINT = "direct:httpRS";

	/** The Constant MAIN_ROUTE_UNDERSCORE. */
	public static final String MAIN_ROUTE_UNDERSCORE = "mainroute_";

	/** The Constant UNDERSCORE_ROUTE. */
	public static final String UNDERSCORE_ROUTE = "_route";

	/** The Constant USER_AGENT_STRING. */
	public static final String USER_AGENT = "user-agent";

	/** The Constant SUB_ROUTE_UNDERSCORE. */
	public static final String SUB_ROUTE_UNDERSCORE = "subroute_";

	/** The Constant UNDERSCORE. */
	public static final String UNDERSCORE = "_";

	/** The Constant HYPHEN. */
	public static final String HYPHEN = "-";

	/** The Constant DEAD_LETTER_CHANNEL_ROUTE. */
	public static final String DEAD_LETTER_CHANNEL_ROUTE = "deadletter";

	/** The Constant FILE_COMPONENT. */
	public static final String FILE_COMPONENT = "file://";

	/** The Constant FILE_COMPONENT_FORM_OPTION. */
	public static final String FILE_COMPONENT_FORM_OPTION = "noop=false&delete=true";

	/** The Constant FILE_COMPONENT_TO_OPTION. */
	public static final String FILE_COMPONENT_TO_OPTION = "noop=true";

	/** The Constant FILE_OFFLINE_BASE_PATH_DEFAULT. */
	public static final String FILE_OFFLINE_BASE_PATH_DEFAULT = "airoffline/";

	/** The Constant FILE_OFFLINE_BACKUP_BASE_PATH_DEFAULT. */
	public static final String FILE_OFFLINE_BACKUP_BASE_PATH_DEFAULT = "airoffline/backup/";

	/** The Constant FILE_OFFLINE_ROUTE_ID. */
	public static final String FILE_OFFLINE_ROUTE_ID = "route_offline_";

	/** The Constant AUTHORIZATION. */
	public static final String AUTHORIZATION = "Authorization";

	/** The Constant HTTP_VERSION. */
	public static final String HTTP_VERSION = "HTTP/1.1";

	/** The Constant HTTP_CONTENT_TYPE. */
	public static final String HTTP_CONTENT_TYPE_AIR = "text/xml";

	/** The Constant HTTP_METHOD. */
	public static final String HTTP_METHOD = "POST";

	/** The Constant HTTP_AUTHORIZATION_KEY. */
	public static final String HTTP_AUTHORIZATION_KEY = "Basic ZmRwcHJvdnVzZXI6ZmRwcHJvdnVzZXI=";

	/** The Constant ERROR_CODE_RESPONSE. */
	public static final Long ERROR_CODE_RESPONSE = 200L;

	/** The Constant HTTP_RESPONSE_CODE. */
	public static final String HTTP_RESPONSE_CODE = "camelhttpresponsecode";

	/** The Constant HEADER_CONNECTION. */
	public static final String HEADER_CONNECTION = "Connection";

	/** The Constant HEADER_CONNECTION_VALUE. */
	public static final String HEADER_CONNECTION_VALUE = "Keep-alive";

	/** The Constant HEADER_ACCEPT. */
	public static final String HEADER_ACCEPT = "Accept";

	/** The Constant HEADER_ACCEPT_VALUE. */
	public static final String HEADER_ACCEPT_VALUE = "text/xml";

	/** The Constant AIR_OFFLINE_LOCATION. */
	public static final String AIR_OFFLINE_LOCATION = "air.offline.location";

	/** The Constant AIR_OFFLINE_BACKUP_LOCATION. */
	public static final String AIR_OFFLINE_BACKUP_LOCATION = "air.offline.backup.location";

	/** The Constant AIR_RECHARGE_STEP_ID. */
	public static final Long AIR_RECHARGE_STEP_ID = 1L;

	/** The Constant INCOMING_IP_ADDRESS. */
	public static final String INCOMING_IP_ADDRESS = "INCOMING_IP_ADDRESS";

	/** The Constant OUTGOING_IP_ADDRESS. */
	public static final String OUTGOING_IP_ADDRESS = "OGIP";

	/** The Constant MODULE_NAME. */
	public static final String EXTERNAL_SYSTEM_TYPE = "EXTERNAL_SYSTEM_TYPE";

	/** The Constant LOGICAL_NAME. */
	public static final String LOGICAL_NAME = "LNAME";

	/** The Constant USER_AGENT_STRING_RS. */
	public static final String USER_AGENT_STRING_RS = "Jakarta Commons-HttpClient/2.0.1";
	
	/** The Constant USER_AGENT_STRING_ADC. */
	public static final String USER_AGENT_STRING_ADC = "Jakarta Commons-HttpClient/2.0.1";

	/** The Constant USER_AGENT_STRING_CGW. */
	public static final String USER_AGENT_STRING_CGW = "Jakarta Commons-HttpClient/2.0.1";

	/** The Constant HEADER_ACCEPT_VALUE_CGW. */
	public static final String HEADER_ACCEPT_VALUE_RS = "application/xml";
	
	/** The Constant HEADER_ACCEPT_VALUE_ADC. */
	public static final String HEADER_ACCEPT_VALUE_ADC = "application/text";

	/** The Constant HEADER_ACCEPT_VALUE_CGW. */
	public static final String HEADER_ACCEPT_VALUE_CGW = "text/xml";

	/** The Constant HTTP_CONTENT_TYPE_CGW. */
	public static final String HTTP_CONTENT_TYPE_CGW = "text/xml";

	/** The Constant HTTP_CONTENT_TYPE_RS. */
	public static final String HTTP_CONTENT_TYPE_RS = "application/xml";
	
	/** The Constant HTTP_CONTENT_TYPE_ADC. */
	public static final String HTTP_CONTENT_TYPE_ADC = "application/xml";

	/** The Constant HTTP_VERSION_AIR. */
	public static final String HTTP_VERSION_AIR = "Air HTTP/1.1";
	
	/** The Constant HTTP_VERSION_ADC. */
	public static final String HTTP_VERSION_ADC = "webservices HTTP/1.1";

	/** The Constant HTTP_VERSION_RS. */
	public static final String HTTP_VERSION_RS = "cgw/SingleProvision HTTP/1.1";

	/** The Constant HTTP_VERSION_CGW. */
	public static final String HTTP_VERSION_CGW = "httpfe HTTP/1.1";

	/** The Constant RESPONSE_CODE. */
	public static final String RESPONSE_CODE = "RESPONSE_CODE";

	/** The Constant COMMAND_OUTPUT. */
	public static final String COMMAND_OUTPUT = "COMMAND_OUTPUT";

	/** The Constant BIND_MODE. */
	public static final String BIND_MODE = "BIND_MODE";

	/** The Constant BIND_MODE_TRX. */
	public static final String BIND_MODE_TRX = "TRX";

	/** The Constant BIND_MODE_TXRX. */
	public static final String BIND_MODE_TXRX = "TXRX";

	/** The Constant INCOMING_TRX_IP_PORT. */
	public static final String INCOMING_TRX_IP_PORT = "INCOMING_TRX_IP_PORT";

	/** The Constant EXCHANGE_RESPONSE. */
	public static final String EXCHANGE_RESPONSE = "ExchangeResposnse";

	/** The Constant OUTGOING_IP_PORT. */
	public static final String OUTGOING_CIRCLE_CODE_IP_PORT = "OUTGOING_IP_PORT";

	/** The Constant BREAD_CRUMB_ID. */
	public static final String BREAD_CRUMB_ID = "breadcrumbId";

	/** The Constant HOST. */
	public static final String HOST = "HOST";

	/** The Constant INCOMING_IP_ADDRESS_PORT_USERNAME. */
	public static final String INCOMING_IP_ADDRESS_PORT_USERNAME = "incomingIpAddressPortUsername";

	/** The Constant BIND_NODE_TYPE_TRANSCEIVER. */
	public static final String BIND_NODE_TYPE_TRANSCEIVER = "transceiver";

	/** The Constant BIND_MODE_TYPE. */
	public static final String BIND_MODE_TYPE = "BIND_MODE_TYPE";

	/** The Constant RESULT_STATUS_COUNT. */
	public static final String RESULT_STATUS_COUNT = "RESULT_STATUS_COUNT";

	/** The Constant HTTP_COMPONENT_TYPE. */
	public static final String HTTPS_COMPONENT_TYPE = "https://";

	/** The outgoing ip for trx. */
	public static final String OUTGOING_TRX_IP_PORT = "OUTGOING_TRX_IP_PORT";

	/** The Constant EMA_CAMEL_ENDPOINT. */
	public static final String EMA_CAMEL_ENDPOINT = "direct:ema";

	/** The Constant EMA_SSH_COMPONENT_TYPE. */
	public static final String EMA_SSH_COMPONENT_TYPE = "ssh://";

	/** The Constant EMA_TELNET_COMPONENT_TYPE. */
	public static final String EMA_TELNET_COMPONENT_TYPE = "netty:tcp://";

	/** The Constant IS_SMS_SESSION_TERMINATED. */
	public static final String IS_SMS_SESSION_TERMINATED = "IS_SMS_SESSION_TERMINATED";

	/** The Constant _HQ_SCHED_DELIVERY. */
	public static final String _HQ_SCHED_DELIVERY = "_HQ_SCHED_DELIVERY";

	/** The Constant CAMEL_DIRECT. */
	public static final String CAMEL_DIRECT = "direct:";

	/** The Constant NETTY_PART_ENDPOINT_URL. */
	public static final String NETTY_PART_ENDPOINT_URL = "?textline=true";

	/** The Constant MODULE_NAME. */
	public static final String MODULE_NAME = "MODULE_NAME";

	/** The Constant EMA_COMMAND_END_DELIMITER. */
	public static final String EMA_COMMAND_END_DELIMITER = "\n";

	/** The Constant LB_ROUTEID_PREFIX. */
	public static final String LB_ROUTEID_PREFIX = "loadbalancer";

	/** The Constant CHANNEL_TYPE. */
	public static final String CHANNEL_TYPE = "channelType";

	/** The Constant INPUT. */
	public static final String INPUT = "input";
	
	/** The Constant INPUT. */
	public static final String UTValue = "UTValue";

	/** The Constant FDP_RESPONSE. */
	public static final String FDP_RESPONSE = "fdpResponse";

	/** The Constant VAS_SUCCESS_CODE. */
	public static final String VAS_SUCCESS_CODE = "0";

	/** The Constant VAS_SUCCESS_STRING. */
	public static final String VAS_SUCCESS_STRING = "SUCCESS";

	/** The Constant FORBIDDEN_ERROR_CODE. */
	public static final String FORBIDDEN_ERROR_CODE = "403";

	/** The Constant USERNAME. */
	public static final String USERNAME = "username";

	/** The Constant CIRCLE_CODE. */
	public static final String REQUEST_CIRCLE_CODE = "circlecode";

	/** The Constant CIRCLE_CODE. */
	public static final String REQUEST_ACTION = "action";

	/** The Constant SYSTEM_TYPE_FDP. */
	public static final String SYSTEM_TYPE_FDP = "CIS";

	/** The Constant USER_ID. */
	public static final String USER_ID = "userid";

	/** The Constant IVR_URL. */
	public static final String IVR_URL = "IVR_URL";

	/** The Constant IVR_CHANNEL. */
	public static final String IVR_CHANNEL = "IVR";

	/** The Constant IS_RS_DEPROV. */
	public static final String IS_RS_DEPROV = "isrsdeprov";

	/** The Constant RS_COMMAND_NAME. */
	public static final String RS_COMMAND_NAME = "RS_COMMAND_NAME";

	/** The Constant HTTP_ADAPTER_ERROR_CODE. */
	public static final String HTTP_ADAPTER_ERROR_CODE = "-200";

	/** The Constant IVR_PRODUCT_BUY_MAX_LIMIT. */
	public static final String THROTTLER_IVR_PRODUCTBUY_MAX_TPS = "THROTTLER_IVR_PRODUCTBUY_MAX_TPS";

	/** The Constant IVR_COMMAND_SERVICE_MAX_LIMIT. */
	public static final String THROTTLER_IVR_COMMANDSERVICE_MAX_TPS = "THROTTLER_IVR_COMMANDSERVICE_MAX_TPS";

	/** The Constant IVR_PRODUCT_BUY_MAX_LIMIT. */
	public static final String THROTTLER_IVR_PRODUCTBUY_RESUME_PERCENTAGE = "THROTTLER_IVR_PRODUCTBUY_RESUME_PERCENTAGE";

	/** The Constant IVR_COMMAND_SERVICE_MAX_LIMIT. */
	public static final String THROTTLER_IVR_COMMANDSERVICE_RESUME_PERCENTAGE = "THROTTLER_IVR_COMMANDSERVICE_RESUME_PERCENTAGE";

	/** The Constant THROTTLER_FULLFILLMENT_SERVICE_RESUME_PERCENTAGE. */
	public static final String THROTTLER_FULFILLMENT_SERVICE_RESUME_PERCENTAGE = "THROTTLER_FULLFILLMENT_SERVICE_RESUME_PERCENTAGE";

	/** The Constant THROTTLER_FULLFILLMENT_SERVICE_MAX_TPS. */
	public static final String THROTTLER_FULFILLMENT_SERVICE_MAX_TPS = "THROTTLER_FULLFILLMENT_SERVICE_MAX_TPS";

	/**
	 * Adding constants for CMS The Constant HTTP_COMPONENT_CMS_ENDPOINT.
	 */
	public static final String HTTP_COMPONENT_CMS_ENDPOINT = "direct:httpCMS";

	/**
	 * Adding constants for MCLOAN The Constant HTTP_COMPONENT_MCLOAN_ENDPOINT.
	 */
	public static final String HTTP_COMPONENT_MCLOAN_ENDPOINT = "direct:httpMCLOAN";

	/** The Constant HTTP_CONTENT_TYPE. */
	public static final String HTTP_CONTENT_TYPE_CMS = "application/xml";

	/** The Constant CAMEL_HTTP_METHOD_POST. */
	public static final String CAMEL_HTTP_METHOD_POST = "POST";

	/** The Constant CAMEL_HTTP_METHOD_POST. */
	public static final String CAMEL_HTTP_METHOD_GET = "GET";

	/** The Constant CAMEL_HTTP_METHOD_TYPE. */
	public static final String CAMEL_HTTP_METHOD_TYPE = "CamelHttpMethod";

	/** The Constant CMS_COMMAND_NAME. */
	public static final String CMS_COMMAND_NAME = "CMS_COMMAND_NAME";

	/** The Constant DOT. */
	public static final String DOT = ".";

	/** The Constant Root tag of RS xml. */
	public static final String ROOT_TAG = "servicesDtlsTwo";

	/** The Constant SUB tag of RS xml. */
	public static final String SUB_TAG = "serviceTwo";

	/**
	 * Adding constants for FDP Offline The Constant
	 * HTTP_COMPONENT_FDP_OFFLINE_ENDPOINT.
	 */
	public static final String HTTP_COMPONENT_FDP_OFFLINE_ENDPOINT = "direct:httpFDPOffline";

	/** The Constant HEADER_ACCEPT_VALUE_FDPOFFLINE. */
	public static final String HEADER_ACCEPT_VALUE_FDPOFFLINE = "application/xml";

	/** The Constant HTTP_COMPONENT_LOYALTY_ENDPOINT. */
	public static final String HTTP_COMPONENT_LOYALTY_ENDPOINT = "direct:httpLoyalty";

	/** The Constant INVOCATOR_NAME_ABILITY. */
	public static final String INVOCATOR_NAME_ABILITY = "ABILITY";

	/** The Constant FULFILLMENT_CIRCLE_CODE. */
	public static final String FULFILLMENT_CIRCLE_CODE = "DEL";

	/** The Constant SPLIT_NUMBER. */
	public static final String SPLIT_NUMBER = "splitno";

	/** The Constant PRODUCT_COST. */
	public static final String PRODUCT_COST = "productcost";

	/** The Constant HTTPS_ADAPTER_ERROR_CODE. */
	public static final String HTTPS_ADAPTER_ERROR_CODE = "-200";

	/** The Constant HTTP_REQUEST_TYPE_PULL. */
	public static final String HTTP_REQUEST_TYPE_PULL = "pull";

	/** The Constant HTTP_REQUEST_TYPE_CLEANUP. */
	public static final String HTTP_REQUEST_TYPE_CLEANUP = "cleanup";
	
	/** The Constant HTTP_COMPONENT_ABILITY_ENDPOINT. */
	public static final String HTTP_COMPONENT_ABILITY_ENDPOINT = "direct:httpAbility";
	
	public static final String HTTP_COMPONENT_ESF_ENDPOINT="direct:httpESF";
	
	public static final String ABILITY_QUEUE = "jms:queue:Ability_Queue";
	
	public static final String ESF_Queue = "jms:queue:ESF_Queue";
	
	/** The Constant HTTP_COMPONENT_AIR_ENDPOINT. */
	public static final String HTTP_COMPONENT_EVDS_ENDPOINT = "direct:httpEVDS";
	
	/** The Notification ID for same subscribers */
	public static final String SAME_SUBSCRIBER_NOTIFICATION = "-4400";
	
	/** The MAX DATA CONSTRAINT */
	public static final String MAX_DATA_CONSTRAINT = "-4401";
	
	/** The INVALID DATA AMOUNT */
	public static final String INVALID_DATA_AMOUNT = "-4402";

	public static final String TIME_4_U_VOUCHER_CODE_LENGTH_MIN = "time_4_u_voucher_code_length_min";
	
	public static final String TIME_4_U_VOUCHER_CODE_LENGTH_MAX = "time_4_u_voucher_code_length_max";

	public static final String NO_ACTIVATED_BUNDLE = "-4410";
	
	/** THROTTLER_HTTP_USSD_CMV_SERVICE_MAX_TPS **/
	public static final String THROTTLER_HTTP_USSD_CMV_SERVICE_MAX_TPS = "THROTTLER_HTTP_USSD_CMV_SERVICE_MAX_TPS";
	
	/** THROTTLER_HTTP_USSD_CMV_SERVICE_RESUME_PERCENTAGE **/
	public static final String THROTTLER_HTTP_USSD_CMV_SERVICE_RESUME_PERCENTAGE = "THROTTLER_HTTP_USSD_CMV_SERVICE_RESUME_PERCENTAGE";	
	
	/** Constant TEXT_USER **/
	public static final String TEXT_USER="user";
	
	/** Constant SBBBINPUT_PARAM_IS_SELF **/
	public static final String SBBBINPUT_PARAM_IS_SELF="is_self=TRUE";
	
	   /** The Constant COMPONENT_EXCEPTION_ENDPOINT. */
    public static final String COMPONENT_EXCEPTION_ENDPOINT = "direct:excptionMessageSNMP";
    
    public static final String REGISTERED_DELIVERY = "registeredDelivery";
    
    public static final String ACTIVE_TRAFFIC_MANAGER_FQDN = "ACTIVE_TRAFFIC_MANAGER_FQDN";
    
    public static final String ACTIVE_TRAFFIC_MANAGER_IP = "ACTIVE_TRAFFIC_MANAGER_IP";
    
    public static final String IS_GEORED_ENABLED = "IS_GEORED_ENABLED";
    
    public static final String YES = "Y";
    
    public static final String GET_TRANSACTION_STATUS_COMMAND_NAME = "GETTransactionStatus";
	
	public static final String MM_DEBIT = "MM DEBIT"; 
	
	/** The Constant ADC_COMPONENT_ADC_ENDPOINT. */
	public static final String HTTP_COMPONENT_ADC_ENDPOINT = "direct:httpADC";
	
	public static final String MM_COMMAND_NAME = "MM_COMMAND_NAME";
}
