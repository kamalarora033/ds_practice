package com.ericsson.fdp.business.route.processor;

import java.util.List;
import java.util.Map;

import javax.annotation.Resource;

import org.apache.camel.Exchange;
import org.apache.camel.Message;
import org.apache.camel.Processor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ericsson.fdp.FDPCacheable;
import com.ericsson.fdp.business.constants.BusinessConstants;
import com.ericsson.fdp.business.enums.AuxRequestParam;
import com.ericsson.fdp.business.enums.ConfigurationKey;
import com.ericsson.fdp.business.enums.DisplayArea;
import com.ericsson.fdp.business.enums.TLVOptions;
import com.ericsson.fdp.business.menu.FDPDynamicMenu;
import com.ericsson.fdp.business.request.requestString.impl.FDPSMSCRequestStringImpl;
import com.ericsson.fdp.business.request.requestString.impl.FDPUSSDRequestStringImpl;
import com.ericsson.fdp.business.service.DynamicMenuItegrator;
import com.ericsson.fdp.business.util.RequestUtil;
import com.ericsson.fdp.common.constants.FDPConstant;
import com.ericsson.fdp.common.constants.FDPLoggerConstants;
import com.ericsson.fdp.common.enums.BusinessModuleType;
import com.ericsson.fdp.common.enums.ChannelType;
import com.ericsson.fdp.common.vo.CircleConfigParamDTO;
import com.ericsson.fdp.common.vo.FDPCircle;
import com.ericsson.fdp.core.cache.AppCacheSubStore;
import com.ericsson.fdp.core.display.ResponseMessage;
import com.ericsson.fdp.core.dsm.framework.service.FDPCache;
import com.ericsson.fdp.core.exception.ExecutionFailedException;
import com.ericsson.fdp.core.logging.FDPLogger;
import com.ericsson.fdp.core.logging.FDPLoggerFactory;
import com.ericsson.fdp.core.logging.USSDTrafficLoggerPosition;
import com.ericsson.fdp.core.request.FDPRequest;
import com.ericsson.fdp.core.request.FDPResponse;
import com.ericsson.fdp.core.request.impl.FDPRequestImpl;
import com.ericsson.fdp.core.request.impl.FDPSMPPRequestImpl;
import com.ericsson.fdp.core.serviceprovisioning.bean.FDPAppBag;
import com.ericsson.fdp.core.serviceprovisioning.bean.FDPRequestBag;
import com.ericsson.fdp.core.utils.ApplicationUtil;
import com.ericsson.fdp.dao.dto.ExchangeMessageResponse;
import com.ericsson.fdp.dao.dto.FDPSMSCConfigDTO;
import com.ericsson.fdp.route.constant.RoutingConstant;
import com.ericsson.fdp.route.enumeration.FDPRouteHeaders;
import com.google.gson.Gson;

/**
 * A Processor based {@link org.apache.camel.Processor} which is capable of
 * processing the messages coming from multiple queues to call the Dynamic menu
 * for incoming request.
 * 
 * @author Ericsson
 */

public class RequestProcessor implements Processor {

	/** The dynamic Menu Itegrator. */
	@Resource(lookup = "java:app/fdpBusiness-1.0/DynamicMenuItegratorImpl")
	private DynamicMenuItegrator dynamicMenuItegrator;

	/** The fdp dynamic menu. */
	@Resource(lookup = "java:app/fdpBusiness-1.0/DynamicMenuImpl")
	private FDPDynamicMenu fdpDynamicMenu;

	/** The request cache. */
	@Resource(lookup = "java:app/fdpCoreServices-1.0/ApplicationConfigCache")
	private FDPCache<FDPAppBag, Object> applicationConfigCache;

	/**
	 * The fdp request cache constant.
	 */
	@Resource(lookup = "java:app/fdpCoreServices-1.0/RequestCacheForUSSD")
	private FDPCache<FDPRequestBag, FDPCacheable> fdpRequestCacheForUSSD;

	/** The fdp request cache for sms. */
	@Resource(lookup = "java:app/fdpCoreServices-1.0/RequestCacheForSMS")
	private FDPCache<FDPRequestBag, FDPCacheable> fdpRequestCacheForSMS;

	/** The Constant LOGGER. */
	private static final Logger LOGGER = LoggerFactory.getLogger(RequestProcessor.class);

	/** The log method name. */
	private final String logMethodName = "process()";

	/* (non-Javadoc)
	 * @see org.apache.camel.Processor#process(org.apache.camel.Exchange)
	 */
	@Override
	@SuppressWarnings("unchecked")
	public void process(final Exchange exchange) throws Exception {

		String circleName = null;
		Logger circleLoggerRequest = null;
		final Message inputExchange = exchange.getIn();
		String loggerAppender = null;
		try {
			final String circleCode = (String) inputExchange.getHeader(RoutingConstant.CIRCLE_ID);
			String serviceType = (String) inputExchange.getHeader(RoutingConstant.SERVICE_MODE_TYPE);
		
			final String routeId = (String) inputExchange.getHeader(FDPRouteHeaders.ROUTE_ID.getValue());
			final String msisdn = (String) inputExchange.getHeader(BusinessConstants.MSISDN);
			final String destAddr = (String) inputExchange.getHeader(BusinessConstants.DEST_ADDR);
			final String requestId = (String) inputExchange.getHeader(BusinessConstants.REQUEST_ID);
			final String bindModeFromExchange = (String) inputExchange.getHeader(BusinessConstants.BIND_MODE);
			final String incomingTrxIpPort = inputExchange.getHeader(BusinessConstants.INCOMING_TRX_IP_PORT,
					String.class);
			final String incomingIPAddress = inputExchange.getHeader(BusinessConstants.INCOMING_IP_ADDRESS,
					String.class);
			final String bindModeType = inputExchange.getHeader(BusinessConstants.BIND_MODE_TYPE, String.class);
			final String incomingIpAddressPortUsername = inputExchange.getHeader(
					BusinessConstants.INCOMING_IP_ADDRESS_PORT_USERNAME, String.class);
			final String circleCodeInIpPortUsername = ApplicationUtil.getServerName() + BusinessConstants.COLON
					+ incomingIpAddressPortUsername;
			FDPLoggerFactory.reportUssdTarfficReportLogger(getClass(), "process", requestId, msisdn, "Reached To FDP Business", USSDTrafficLoggerPosition.FROM_INBOUND_QUEUE_TO_BUSINESS);
			/* Getting Data from App Cache */
			final FDPAppBag appBag = new FDPAppBag();
			appBag.setSubStore(AppCacheSubStore.CIRCLE_CODE_CIRCLE_NAME_MAP);
			appBag.setKey(circleCode);
			final FDPCircle fdpCircle = (FDPCircle) applicationConfigCache.getValue(appBag);
			circleName = fdpCircle.getCircleName();
			final Logger circleLoggerTrace = FDPLoggerFactory.getTraceLogger(circleName,
					BusinessModuleType.SMSC_NORTH.name());
			circleLoggerRequest = FDPLoggerFactory.getRequestLogger(circleName, BusinessModuleType.SMSC_NORTH.name());
			FDPLogger.info(circleLoggerTrace, getClass(), logMethodName,
					String.format("circleCode from Header : %s", circleCode));
			FDPLogger.info(circleLoggerTrace, getClass(), logMethodName,
					String.format("Generated Request Id from Header : %s", requestId));
			FDPLogger.info(circleLoggerTrace, getClass(), logMethodName,
					String.format("Service Type (USSD/WAP)from Header : %s", serviceType));
			FDPLogger.info(circleLoggerTrace, getClass(), logMethodName,
					String.format("Source Address(MSISDN) from Header : %s", msisdn));
			FDPLogger.info(circleLoggerTrace, getClass(), logMethodName,
					String.format("Destination Address from Header : %s", destAddr));

			String sessionId = "", ussdValue = "";
			boolean isRequestedSessionTermination = false, isNewSessionInitiated = true;

			final Map<String, Object> optionalParameters = new Gson().fromJson(
					inputExchange.getHeader(FDPConstant.OPTIONALPARAMETER, String.class), Map.class);
			FDPLogger.debug(circleLoggerTrace, getClass(), logMethodName, "Optional Parameter map :"
					+ optionalParameters);
			if (optionalParameters != null && serviceType.equals(RoutingConstant.SERVICE_TYPE_USSD)) {
				for (final String key : optionalParameters.keySet()) {
					FDPLogger.info(circleLoggerTrace, getClass(), logMethodName, "Name :" + key + " | Value :"
							+ optionalParameters.get(key));
				}
				sessionId = getOptionalParameterForSessionId(optionalParameters);
				isRequestedSessionTermination = getOptionalParameterForUssdServiceOp(optionalParameters);
				ussdValue = getOptionalParameterForUSSDValues(optionalParameters);
				FDPLogger.info(circleLoggerTrace, getClass(), logMethodName, "isRequestedSessionTermination:"
						+ isRequestedSessionTermination);
			} else {
				FDPLogger.info(circleLoggerTrace, getClass(), logMethodName, String.format("No TLVs found."));
			}
			sessionId = (sessionId != null && !"".equals(sessionId)) ? sessionId : requestId;
			final String incomingIpAdress = inputExchange
					.getHeader(BusinessConstants.INCOMING_IP_ADDRESS, String.class);

			String channelType = BusinessConstants.SERVICE_TYPE_USSD;
			final String logicalName = getLogicalNameByServiceType(serviceType, circleCodeInIpPortUsername,
					bindModeType, incomingTrxIpPort);
			FDPLogger.info(circleLoggerTrace, getClass(), logMethodName, logicalName);
			boolean isSMSChannel = false;
			if (serviceType.equals(BusinessConstants.SERVICE_TYPE_SMS)) {
				channelType = "SMS";
				sessionId = destAddr + msisdn;
				isSMSChannel = true;
			}

			loggerAppender = FDPConstant.REQUEST_ID + FDPConstant.LOGGER_KEY_VALUE_DELIMITER + requestId
					+ FDPConstant.LOGGER_DELIMITER + FDPConstant.INCOMING_IP + FDPConstant.LOGGER_KEY_VALUE_DELIMITER
					+ incomingIpAdress + FDPConstant.LOGGER_DELIMITER + BusinessConstants.LOGICAL_NAME
					+ FDPConstant.LOGGER_KEY_VALUE_DELIMITER + logicalName + FDPConstant.LOGGER_DELIMITER
					+ FDPConstant.CHANNEL_TYPE + FDPConstant.LOGGER_KEY_VALUE_DELIMITER + channelType
					+ FDPConstant.LOGGER_DELIMITER + FDPConstant.MSISDN + FDPConstant.LOGGER_KEY_VALUE_DELIMITER
					+ msisdn;
			FDPLogger.info(circleLoggerRequest, getClass(), logMethodName, loggerAppender);

			FDPLogger.info(circleLoggerRequest, getClass(), logMethodName, FDPConstant.REQUEST_ID
					+ FDPConstant.LOGGER_KEY_VALUE_DELIMITER + requestId + FDPConstant.LOGGER_DELIMITER
					+ FDPConstant.SESSION_ID + FDPConstant.LOGGER_KEY_VALUE_DELIMITER + sessionId);
			
			final Map<String, String> configurationMap = fdpCircle.getConfigurationKeyValueMap();
			final String paramDisplay = configurationMap.get(ConfigurationKey.PP_REPORT_PARAM_DISPLAY.getAttributeName());
			if(null != paramDisplay && paramDisplay.equalsIgnoreCase(FDPLoggerConstants.TRUE)){
				FDPLogger.info(circleLoggerRequest, getClass(), logMethodName, "CMDREQ:"+FDPLoggerConstants.NOT_APPLICABLE);
			}else{
				FDPLogger.info(circleLoggerRequest, getClass(), logMethodName, "CMDREQ:"+FDPLoggerConstants.SKIPPED);
			}
			
			

			if (!isRequestedSessionTermination) {
				String body = "";
				FDPLogger.debug(circleLoggerRequest, getClass(), logMethodName, FDPConstant.REQUEST_ID
						+ FDPConstant.LOGGER_KEY_VALUE_DELIMITER + requestId + FDPConstant.LOGGER_DELIMITER
						+ "Request string " + exchange.getIn().getBody());

				if (BusinessConstants.SERVICE_TYPE_USSD.equals(serviceType)) {
					body = exchange.getIn().getBody().toString();
					if (body.indexOf("#") > 1) {
						body = body.substring(0, body.indexOf("#") + 1);
						body = body.replaceAll("^@", "");
					}
				} else {
					body = exchange.getIn().getBody().toString();
					body = body.replaceAll("^@", "");
				}
				
				final ExchangeMessageResponse exchangeMessage = new ExchangeMessageResponse();
				exchangeMessage.setCircleId(circleCode);
				exchangeMessage.setMsgType(serviceType);
				exchangeMessage.setMsisdn(msisdn);
				exchangeMessage.setSourceAddress(destAddr);
				exchangeMessage.setSessionId(sessionId);
				exchangeMessage.setBindMode(bindModeFromExchange);
				exchangeMessage.setIncomingTrxIpPort(incomingTrxIpPort);
				exchangeMessage.setIncomingIPAddress(incomingIPAddress);
				exchangeMessage.setIpPortSytemId(circleCodeInIpPortUsername);
				exchangeMessage.setBindModeType(bindModeType);
				exchangeMessage.setRouteId(routeId);

				FDPLogger.info(circleLoggerTrace, getClass(), logMethodName,
						String.format("Body Retrieved from MO : %s", body));
				FDPLogger.info(circleLoggerTrace, getClass(), logMethodName,
						String.format("MO Received Successfully , By passing the MO Request to Dynamic Menu."));
				LOGGER.debug("Body Received :{}", body);
				FDPSMPPRequestImpl fdpussdsmscRequestImpl = null;
				boolean isNewRequest = false;
				FDPRequestBag fdpRequestBag = new FDPRequestBag(sessionId);
				FDPRequestImpl fdpRequestImpl = null;
				LOGGER.debug("Getting value from cache for session id : {} service {}", new Object[] { sessionId,
						serviceType });
				if (serviceType.equals(BusinessConstants.SERVICE_TYPE_SMS)) {
					fdpRequestImpl = (FDPRequestImpl) fdpRequestCacheForSMS.getValue(fdpRequestBag);
				} else {
					fdpRequestBag = new FDPRequestBag(msisdn);
					fdpRequestImpl = (FDPRequestImpl) fdpRequestCacheForUSSD.getValue(fdpRequestBag);
				}

				if (fdpRequestImpl != null) {
					if (fdpRequestImpl instanceof FDPRequest) {
						isNewSessionInitiated = false;
						fdpussdsmscRequestImpl = (FDPSMPPRequestImpl) fdpRequestImpl;
						LOGGER.debug("Found value from cache for session id : {}", sessionId);
						if (BusinessConstants.SERVICE_TYPE_USSD.equals(serviceType)) {
							final String inputrequest = body;
							String finalRequest = "";
							for (int i = 0; i < inputrequest.length(); i++) {
								final String thisVal = String.valueOf(inputrequest.charAt(i));
								if (thisVal.matches(FDPConstant.USSD_REQUEST_PATTERN)) {
									finalRequest += thisVal;
								}
							}
							if (finalRequest != null) {
								LOGGER.debug("Setting body as " + finalRequest);
								body = finalRequest;
							}
						}
						if (fdpussdsmscRequestImpl.getSessionId() != null
								&& !fdpussdsmscRequestImpl.getSessionId().equals(sessionId)) {
							isNewSessionInitiated = true;
						}
					}
				} else {
					fdpussdsmscRequestImpl = new FDPSMPPRequestImpl();
					isNewRequest = true;
					LOGGER.debug("Creating new request impl");
				}

				fdpussdsmscRequestImpl.setRequestId(requestId);
				fdpussdsmscRequestImpl.setCircle(fdpCircle);
				fdpussdsmscRequestImpl.setSessionId(sessionId);
				final CircleConfigParamDTO circleConfigParamDTO = RequestUtil.populateCircleConfigParamDTO(msisdn,
						fdpCircle);
				fdpussdsmscRequestImpl.setOriginNodeType(circleConfigParamDTO.getOriginNodeType());
				fdpussdsmscRequestImpl.setSubscriberNumberNAI(circleConfigParamDTO.getSubscriberNumberNAI());
				fdpussdsmscRequestImpl.setOriginHostName(circleConfigParamDTO.getOriginHostName());
				fdpussdsmscRequestImpl.setSubscriberNumber(circleConfigParamDTO.getSubscriberNumber());
				fdpussdsmscRequestImpl.setIncomingSubscriberNumber(circleConfigParamDTO.getIncomingSubscriberNumber());
				if (serviceType.equals(BusinessConstants.SERVICE_TYPE_SMS)) {
					fdpussdsmscRequestImpl.setChannel(ChannelType.SMS);
					fdpussdsmscRequestImpl.setRequestString(new FDPSMSCRequestStringImpl((isNewRequest) ? destAddr
							: null, body.trim(), destAddr));
				} else {
					fdpussdsmscRequestImpl.setChannel(ChannelType.USSD);
					fdpussdsmscRequestImpl.setRequestString(new FDPUSSDRequestStringImpl(body.trim()));
				}

                fdpussdsmscRequestImpl.putAuxiliaryRequestParameter(AuxRequestParam.ORIGINAL_MSISDN,
                        circleConfigParamDTO.getSubscriberNumber());

				final FDPResponse fdpResponse = fdpDynamicMenu.executeDynamicMenu(fdpussdsmscRequestImpl);
				final List<ResponseMessage> messageList = fdpResponse.getResponseString();
				
				final String systemType = fdpResponse.getSystemType();
				if (isNewSessionInitiated) {
					updateValueForTLVsForSessionTerminate(fdpussdsmscRequestImpl, ussdValue);
				}
				Long delay = 0L;
				int count = 0;
				boolean flushSession = true;
				for (final ResponseMessage message : messageList) {
					flushSession = flushSession && message.getTLVOptions().contains(TLVOptions.SESSION_TERMINATE);
					final ChannelType chType = message.getChannelForMessage();
					final String responseBodyMessage = message.getCurrDisplayText(DisplayArea.COMPLETE);
					exchangeMessage.setBody(responseBodyMessage);
					final List<TLVOptions> tlvOptions = message.getTLVOptions();
					exchangeMessage.setTerminated(isSessionTerminated(tlvOptions));
					if (ChannelType.USSD.equals(chType)) {
						final String optionalParametersForUSSD = getOptionalParameterStringForUSSDOperation(tlvOptions,
								sessionId, message.getDelayForMessage(), fdpussdsmscRequestImpl,systemType).toString();
						exchangeMessage.setOptionalParameters(optionalParametersForUSSD);
						
						if (delay > 0 && count > 0) {
							exchangeMessage.setDelayTime(delay);
						}
					}
					exchangeMessage.setRequestId(requestId);
					removeFromRequest(isSMSChannel, fdpResponse, fdpussdsmscRequestImpl.getSessionId());
					dynamicMenuItegrator.sendSubmitSmInOut(exchangeMessage);
					if (message.getDelayForMessage() != null && message.getDelayForMessage() > 0) {
						delay = message.getDelayForMessage();
					}
					count++;
				}
				if(systemType !=null){					
					flushSession=true;					
				}
				if (flushSession && BusinessConstants.SERVICE_TYPE_USSD.equals(serviceType)) {
					fdpRequestBag = new FDPRequestBag(msisdn);
					fdpRequestCacheForUSSD.removeKey(fdpRequestBag);
				}
			} else {
				// Removing sessionId from Request Cache.
				final FDPRequestBag fdpRequestBag = new FDPRequestBag(msisdn);
				fdpRequestCacheForUSSD.removeKey(fdpRequestBag);
				FDPLogger.debug(
						circleLoggerRequest,
						getClass(),
						logMethodName,
						new StringBuilder(BusinessConstants.REQUEST_ID).append(FDPConstant.LOGGER_KEY_VALUE_DELIMITER)
								.append(requestId).append(FDPConstant.LOGGER_DELIMITER)
								.append("Recevied MO for USSD_SERVICE_OP as Unknown: Session ID ")
								.append(FDPConstant.LOGGER_KEY_VALUE_DELIMITER).append(sessionId).toString());
			}
		} catch (final Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * This method is used to remove request from cache if required.
	 * 
	 * @param isSMSChannel
	 *            the channel.
	 * @param fdpResponse
	 *            the response.
	 * @param requestId
	 *            the request id
	 */
	private void removeFromRequest(final boolean isSMSChannel, final FDPResponse fdpResponse, final String requestId) {
		if (isSMSChannel && fdpResponse.isTerminateSession()) {
			fdpRequestCacheForSMS.removeKey(new FDPRequestBag(requestId));
		}
	}

	/**
	 * Gets the optional parameter string for ussd operation.
	 *
	 * @param tlvOptions the tlv options
	 * @param sessionId the session id
	 * @param delay the delay
	 * @param fdpRequest the fdp request
	 * @return the optional parameter string for ussd operation
	 * @throws ExecutionFailedException the execution failed exception
	 */
	private StringBuilder getOptionalParameterStringForUSSDOperation(final List<TLVOptions> tlvOptions,
			final String sessionId, final Long delay, final FDPRequest fdpRequest, String systemType) throws ExecutionFailedException {
		StringBuilder optionalParameterString = new StringBuilder();
		for (TLVOptions option : tlvOptions) {
			switch (option) {
			case FLASH:
				optionalParameterString.append(getValueForTLVsForSessionTerminate(fdpRequest,systemType)).append(",");
				break;
			case SESSION_TERMINATE:
				optionalParameterString.append(getValueForTLVsForSessionTerminate(fdpRequest,null)).append(",");
				break;
			case SESSION_CONTINUE:
				optionalParameterString.append("USSD_SERVICE_OP,2").append(",");
				break;
			default:
				break;
			}

		}
		
		if (!tlvOptions.contains(TLVOptions.NO_SESSION_INFO)) {
			optionalParameterString.append("ITS_SESSION_INFO," + sessionId);
		}
		return optionalParameterString;
	}

	/**
	 * Update value for tl vs for session terminate.
	 *
	 * @param fdpRequest the fdp request
	 * @param ussrRequest the ussr request
	 */
	private void updateValueForTLVsForSessionTerminate(final FDPRequest fdpRequest, final String ussrRequest) {
		if (fdpRequest instanceof FDPRequestImpl) {
			((FDPRequestImpl) fdpRequest).putAuxiliaryRequestParameter(AuxRequestParam.SESSION_TLV, ussrRequest);
		}
	}

	/**
	 * Gets the value for tl vs for session terminate.
	 *
	 * @param fdpRequest the fdp request
	 * @return the value for tl vs for session terminate
	 */
	private String getValueForTLVsForSessionTerminate(final FDPRequest fdpRequest ,String systemType) {
		String currentSessionTLVValue = "USSD_SERVICE_OP,17";		
		if (systemType != null) {
			currentSessionTLVValue = "USSD_SERVICE_OP,3";
		}
		String previousTLVSessionValue = (String) fdpRequest.getAuxiliaryRequestParameter(AuxRequestParam.SESSION_TLV);		
		if (FDPConstant.USSR_REQUEST.equals(previousTLVSessionValue)) {
			currentSessionTLVValue = "USSD_SERVICE_OP,3";
		}
		return currentSessionTLVValue;
	}

	/**
	 * Checks if is session terminated.
	 * 
	 * @param tlvOptions
	 *            the tlv options
	 * @return true, if is session terminated
	 */
	private boolean isSessionTerminated(final List<TLVOptions> tlvOptions) {
		if (tlvOptions.contains(TLVOptions.SESSION_TERMINATE)) {
			return true;
		}
		return false;
	}

	/**
	 * Gets the optional parameter for session id.
	 * 
	 * @param optionalParameters
	 *            the optional parameters
	 * @return the optional parameter for session id
	 */
	private String getOptionalParameterForSessionId(final Map<String, Object> optionalParameters) {
		String sessionId = null;
		if (optionalParameters.containsKey("ITS_SESSION_INFO")) {
			final Object sessionIdShort = optionalParameters.get("ITS_SESSION_INFO");
			if (sessionIdShort != null) {
				if (sessionIdShort instanceof Double) {
					sessionId = Double.toString((Double) sessionIdShort);
					sessionId = sessionId.substring(0, sessionId.indexOf('.'));
				} else if (sessionIdShort instanceof Short) {
					sessionId = Short.toString((Short) sessionIdShort);
				}
			}
		}
		LOGGER.debug("getOptionalParameterForSessionId() :{}  === sessionId {}", optionalParameters, sessionId);
		return sessionId;
	}

	/**
	 * Gets the optional parameter for ussd values.
	 *
	 * @param optionalParameters the optional parameters
	 * @return the optional parameter for ussd values
	 */
	private String getOptionalParameterForUSSDValues(final Map<String, Object> optionalParameters) {
		String ussdValue = null;
		if (optionalParameters.containsKey("USSD_SERVICE_OP")) {
			Object ussdValueFromSMSC = (Object) optionalParameters.get("USSD_SERVICE_OP");
			if (ussdValueFromSMSC instanceof Double) {
				ussdValueFromSMSC = (Double) ussdValueFromSMSC;
			} else if (ussdValueFromSMSC instanceof Short) {
				ussdValueFromSMSC = (Short) ussdValueFromSMSC;
			} else if (ussdValueFromSMSC instanceof Byte) {
				ussdValueFromSMSC = (Byte) ussdValueFromSMSC;
			}
			ussdValue = String.valueOf(ussdValueFromSMSC);
		}
		return String.valueOf(ussdValue);
	}

	/**
	 * Gets the optional parameter for ussd service op.
	 * 
	 * @param optionalParameters
	 *            the optional parameters
	 * @return the optional parameter for ussd service op
	 */
	private boolean getOptionalParameterForUssdServiceOp(final Map<String, Object> optionalParameters) {
		boolean contains = false;
		if (optionalParameters.containsKey("USSD_SERVICE_OP")) {
			Object ussdValueFromSMSC = (Object) optionalParameters.get("USSD_SERVICE_OP");
			if (ussdValueFromSMSC instanceof Double) {
				ussdValueFromSMSC = (Double) ussdValueFromSMSC;
				contains = ussdValueFromSMSC.equals(new Double("5"));
			} else if (ussdValueFromSMSC instanceof Short) {
				ussdValueFromSMSC = (Short) ussdValueFromSMSC;
				contains = ussdValueFromSMSC.equals(new Short("5"));
			} else if (ussdValueFromSMSC instanceof Byte) {
				ussdValueFromSMSC = (Byte) ussdValueFromSMSC;
				contains = ussdValueFromSMSC.equals(new Byte("5"));
			}

		}
		LOGGER.debug("getOptionalParameterForUssdServiceOp() :{} , contains{}", optionalParameters, contains);
		return contains;
	}

	/**
	 * Gets the logical name by service type.
	 *
	 * @param serviceType the service type
	 * @param circleCodeInIpPortUsername the circle code in ip port username
	 * @param bindModeType the bind mode type
	 * @param incomingTrxIpPort the incoming trx ip port
	 * @return the logical name by service type
	 */
	private String getLogicalNameByServiceType(final String serviceType, final String circleCodeInIpPortUsername,
			final String bindModeType, final String incomingTrxIpPort) {
		String logicalName = null;
		final FDPAppBag appBag = new FDPAppBag();
		AppCacheSubStore appCacheSubStoreKey = null;
		if (serviceType.equals(BusinessConstants.SERVICE_TYPE_SMS)) {
			appCacheSubStoreKey = AppCacheSubStore.SMS_USSD_ROUTE_DETAIL;
			appBag.setSubStore(appCacheSubStoreKey);
			appBag.setKey(circleCodeInIpPortUsername + BusinessConstants.COLON + bindModeType);
			LOGGER.debug("Key Maked:" + circleCodeInIpPortUsername + BusinessConstants.COLON + bindModeType);
			final FDPSMSCConfigDTO externalSystemCacheBean = (FDPSMSCConfigDTO) applicationConfigCache.getValue(appBag);
			logicalName = externalSystemCacheBean.getLogicalName();
		} else if (serviceType.equals(BusinessConstants.SERVICE_TYPE_USSD)) {
			appCacheSubStoreKey = AppCacheSubStore.USSDCONFIGURATION_MAP;
			appBag.setSubStore(appCacheSubStoreKey);
			appBag.setKey(incomingTrxIpPort);
			final FDPSMSCConfigDTO externalSystemCacheBean = (FDPSMSCConfigDTO) applicationConfigCache.getValue(appBag);
			logicalName = externalSystemCacheBean.getLogicalName();
		}
		LOGGER.debug("LogicalName got By service Type :{}", logicalName);
		return logicalName;
	}

}
