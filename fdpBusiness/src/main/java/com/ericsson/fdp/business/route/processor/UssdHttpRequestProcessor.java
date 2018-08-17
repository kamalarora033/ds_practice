package com.ericsson.fdp.business.route.processor;

import java.io.StringReader;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.annotation.Resource;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;

import org.apache.camel.Exchange;
import org.apache.camel.Message;
import org.apache.camel.Processor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ericsson.fdp.FDPCacheable;
import com.ericsson.fdp.business.constants.BusinessConstants;
import com.ericsson.fdp.business.constants.FDPStepResponseConstants;
import com.ericsson.fdp.business.enums.AuxRequestParam;
import com.ericsson.fdp.business.enums.ConfigurationKey;
import com.ericsson.fdp.business.enums.DisplayArea;
import com.ericsson.fdp.business.enums.TLVOptions;
import com.ericsson.fdp.business.menu.FDPDynamicMenu;
import com.ericsson.fdp.business.request.requestString.impl.FDPUSSDRequestStringImpl;
import com.ericsson.fdp.business.service.DynamicMenuItegrator;
import com.ericsson.fdp.business.util.LoggerUtil;
import com.ericsson.fdp.business.util.RequestUtil;
import com.ericsson.fdp.common.constants.FDPConstant;
import com.ericsson.fdp.common.enums.ChannelType;
import com.ericsson.fdp.common.util.xml.XmlUtil;
import com.ericsson.fdp.common.vo.CircleConfigParamDTO;
import com.ericsson.fdp.common.vo.FDPCircle;
import com.ericsson.fdp.core.cache.AppCacheSubStore;
import com.ericsson.fdp.core.display.ResponseMessage;
import com.ericsson.fdp.core.dsm.framework.service.FDPCache;
import com.ericsson.fdp.core.exception.ExecutionFailedException;
import com.ericsson.fdp.core.logging.FDPLogger;
import com.ericsson.fdp.core.request.FDPRequest;
import com.ericsson.fdp.core.request.FDPResponse;
import com.ericsson.fdp.core.request.FDPSMPPRequest;
import com.ericsson.fdp.core.request.impl.FDPRequestImpl;
import com.ericsson.fdp.core.request.impl.FDPSMPPRequestImpl;
import com.ericsson.fdp.core.serviceprovisioning.bean.FDPAppBag;
import com.ericsson.fdp.core.serviceprovisioning.bean.FDPRequestBag;
import com.ericsson.fdp.core.utils.ApplicationConfigUtil;
import com.ericsson.fdp.core.utils.CircleCodeFinder;
import com.ericsson.fdp.http.model.ParametersType.Param;
import com.ericsson.fdp.http.model.Request;
import com.ericsson.fdp.http.model.Response;
import com.ericsson.fdp.http.model.Response.Freeflow;
import com.ericsson.fdp.route.enumeration.FDPRouteHeaders;
import com.ericsson.fdp.smpp.util.SMPPUtil;

/**
 * A Processor based {@link org.apache.camel.Processor} which is capable of
 * processing the messages coming from multiple queues to call the Dynamic menu
 * for incoming request.
 * 
 * Comviava
 * 
 * @author Ericsson
 * 
 */

public class UssdHttpRequestProcessor implements Processor {

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
	private static final Logger LOGGER = LoggerFactory
			.getLogger(RequestProcessor.class);

	/** The log method name. */
	
	private static JAXBContext context=null;
	
	static{
		try {
			context = JAXBContext.newInstance(Request.class);
		} catch (JAXBException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	/*
	 * (non-Javadoc)
	 * 
	 * @see org.apache.camel.Processor#process(org.apache.camel.Exchange)
	 */
	public void process(final Exchange exchange) throws Exception {

		String loggerAppender = null;
		String outputXML=null;
		Unmarshaller un=null;
		StringReader reader=null;
		Request ussdHttpRequest=null;
		String msisdn=null;
		FDPCircle fdpCircle=null;
		String requestId=null;
		boolean isnotfreeflow;
		FDPRequestImpl fdpRequestImpl = null;
		String sessionId=null;
		String requestType=null;
		Integer allowedLength =null;
		FDPSMPPRequestImpl fdpussdsmscRequestImpl = null;
		final FDPResponse fdpResponse;
		Freeflow freeflow = new Freeflow();
		String userInput = null;
		List<Param> params = null;
		
		final Map<String, String> configurationsMap;
		try {
			outputXML = (String) exchange.getIn().getBody();
			un = context.createUnmarshaller();
			reader = new StringReader(outputXML);
			ussdHttpRequest = (Request) un.unmarshal(reader);
			msisdn = ussdHttpRequest.getMsisdn();
			fdpCircle = CircleCodeFinder.getFDPCircleByMsisdn(msisdn,
					ApplicationConfigUtil.getApplicationConfigCache());
			
			//requestId = SMPPUtil.generateRequestId(FDPConstant.SERVICETYPE);
			
			
			loggerAppender = FDPConstant.REQUEST_ID
					+ FDPConstant.LOGGER_KEY_VALUE_DELIMITER + requestId
					+ FDPConstant.LOGGER_DELIMITER + FDPConstant.MSISDN
					+ FDPConstant.LOGGER_KEY_VALUE_DELIMITER + msisdn;
			
			sessionId = ussdHttpRequest.getSessionId();
			requestType= ussdHttpRequest.getType();
			configurationsMap = fdpCircle
					.getConfigurationKeyValueMap();

			allowedLength = Integer.parseInt(configurationsMap
					.get(ConfigurationKey.MSISDN_NUMBER_LENGTH
							.getAttributeName()));
			msisdn=(allowedLength!=null)?updateMsisdn(msisdn,allowedLength):null;
			//Changes done to create request id if request is not available in cache
			requestId = getCachedRequest(msisdn) != null ? getCachedRequest(msisdn).getRequestId()
					: SMPPUtil.generateRequestId(FDPConstant.SERVICETYPE);
			
			/** Adding country code to input msisdn if not already present **/
			if (requestType
					.equalsIgnoreCase(BusinessConstants.HTTP_REQUEST_TYPE_PULL)) {
				LOGGER.debug(
						"Getting value from cache for session id : {} service {}",
						new Object[] { sessionId, FDPConstant.SERVICETYPE });
				fdpRequestImpl=getCachedRequest(msisdn);

				
				if (fdpRequestImpl != null) {
					//handle Old Request
					fdpussdsmscRequestImpl = (FDPSMPPRequestImpl) fdpRequestImpl;
					userInput=getUserInputOld(fdpRequestImpl,fdpussdsmscRequestImpl,ussdHttpRequest,sessionId);
				
				} else {
					fdpussdsmscRequestImpl = new FDPSMPPRequestImpl();
					//Changes done to handle ECW menu redirection flow
					params = ussdHttpRequest.getParameters() != null ? ussdHttpRequest.getParameters().getParam() : null;
					if (params != null && params.size() > 0 && ussdHttpRequest.getNewRequest().intValue() == 1 ) {
						for (Param param : params) {
							if( param.getName().equals(configurationsMap.get(ConfigurationKey.ECW_MENU_REDIRECT_SHORT_CODE.getAttributeName()))) {
								userInput = param.getValue();
							}
						}
						
					} else {
						userInput = FDPConstant.STAR + ussdHttpRequest.getSubscriberInput() + FDPConstant.FDPHASH;
					}
					

					LOGGER.debug("Creating new request impl");
				}
				
				updateUSSDRequst(fdpussdsmscRequestImpl,requestId,fdpCircle,sessionId,msisdn,userInput);
				
				preProcessingOfLogs(fdpussdsmscRequestImpl, exchange);
				
				fdpResponse = fdpDynamicMenu
						.executeDynamicMenu(fdpussdsmscRequestImpl);

				/*
				 * if(checkfirstRequest(msisdn,ussdHttpRequest.getSubscriberInput
				 * ())) { fdpussdsmscRequestImpl .setRequestString(new
				 * FDPUSSDRequestStringImpl(
				 * FDPConstant.USSD_STRING_START_CHAR+ussdHttpRequest
				 * .getSubscriberInput()+FDPConstant.USSD_STRING_END_CHAR));
				 * fdpResponse=fdpDynamicMenu
				 * .executeDynamicMenu(fdpussdsmscRequestImpl); } else {
				 */
				
				// }


				Response response = new Response();
				// IF Block for comviva menu redirect
				
				if (null != fdpResponse && null != fdpResponse.getSystemType()
						&& fdpResponse.getSystemType().equals(
								FDPConstant.COMVIVAUSSD)) {
				
					String comvivamenuredirect = "ComvivaMenuRedirect";
					response.setAppDrivenMenuCode(fdpussdsmscRequestImpl
							.getValueFromStep(comvivamenuredirect,
									FDPStepResponseConstants.MENU_REDIRECT_CODE)
							.toString());
					
					/*freeflow.setFreeflowCharging(fdpussdsmscRequestImpl
							.getValueFromStep(comvivamenuredirect,
									FDPStepResponseConstants.FREE_FLOW_CHARGING)
							.toString());
					freeflow.setFreeflowChargingAmount(fdpussdsmscRequestImpl
							.getValueFromStep(
									comvivamenuredirect,
									FDPStepResponseConstants.FREE_FLOW_CHARGING_AMOUNT)
							.toString());*/
					freeflow.setFreeflowState(fdpussdsmscRequestImpl
							.getValueFromStep(comvivamenuredirect,
									FDPStepResponseConstants.FREE_FLOW_STATE)
							.toString());
					response.setFreeflow(freeflow);
					response.setMsisdn(fdpussdsmscRequestImpl.getSubscriberNumber()
							.toString());
					response.setApplicationResponse(fdpussdsmscRequestImpl
							.getValueFromStep(comvivamenuredirect,
									FDPStepResponseConstants.ERROR_CODE)
							.toString());
				}
				
				else {
					List<ResponseMessage> responseString = fdpResponse
							.getResponseString();
					Iterator it = responseString.iterator();
					if (it.hasNext()) {
						ResponseMessage message = (ResponseMessage) it.next();
						String responseMessage = message
								.getCurrDisplayText(DisplayArea.COMPLETE);
						if (responseMessage != null
								&& !responseMessage.isEmpty()) {
							response.setApplicationResponse(message
									.getCurrDisplayText(DisplayArea.COMPLETE));
							List<TLVOptions> sessionValue = message
									.getTLVOptions();
							if (sessionValue
									.contains(TLVOptions.SESSION_TERMINATE)) {
								final FDPRequestBag fdpRequest = new FDPRequestBag(
										msisdn);
								fdpRequestCacheForUSSD.removeKey(fdpRequest);

								freeflow.setFreeflowState("FB");
								response.setFreeflow(freeflow);
							} else {
								
									freeflow.setFreeflowState("FC");
									/*s	ParametersType parametertype = new ParametersType();
									List<ParametersType.Param> paramlist = new ArrayList<ParametersType.Param>();
									ParametersType.Param param = new Param();
									param.setName("SAMPLE");
									param.setValue("SAMPLE");
									paramlist.add(param);
									freeflow.setParameters(parametertype);*/
									response.setFreeflow(freeflow);
							}
						}
					}
				}
				String ussdResponse = XmlUtil.getXmlUsingMarshaller(response);
				Message out = exchange.getOut();
				exchange.setProperty(Exchange.CONTENT_TYPE, "text/xml");
				out.setBody(ussdResponse);
			} else if (requestType
					.equalsIgnoreCase(BusinessConstants.HTTP_REQUEST_TYPE_CLEANUP)) {
				// Removing sessionId from Request Cache.
				final FDPRequestBag fdpRequestBag = new FDPRequestBag(msisdn);
				fdpRequestCacheForUSSD.removeKey(fdpRequestBag);
				LOGGER.debug(new StringBuilder(BusinessConstants.REQUEST_ID)
						.append(FDPConstant.LOGGER_KEY_VALUE_DELIMITER)
						.append(requestId)
						.append(FDPConstant.LOGGER_DELIMITER)
						.append("Session has been completed for current request")
						.append(FDPConstant.LOGGER_KEY_VALUE_DELIMITER)
						.append(sessionId).toString());
			}
		} catch (final Exception e) {
			e.printStackTrace();
		}
	}

	private void updateUSSDRequst(FDPSMPPRequestImpl fdpussdsmscRequestImpl,
			String requestId, FDPCircle fdpCircle, String sessionId, String msisdn, String userInput) throws ExecutionFailedException {
		fdpussdsmscRequestImpl.setRequestTime(System.currentTimeMillis());
		fdpussdsmscRequestImpl.setRequestId(requestId);
		fdpussdsmscRequestImpl.setCircle(fdpCircle);
		fdpussdsmscRequestImpl.setSessionId(sessionId);
		final CircleConfigParamDTO circleConfigParamDTO = RequestUtil
				.populateCircleConfigParamDTO(msisdn, fdpCircle);
		fdpussdsmscRequestImpl.setOriginNodeType(circleConfigParamDTO
				.getOriginNodeType());
		fdpussdsmscRequestImpl
				.setSubscriberNumberNAI(circleConfigParamDTO
						.getSubscriberNumberNAI());
		fdpussdsmscRequestImpl.setOriginHostName(circleConfigParamDTO
				.getOriginHostName());
		fdpussdsmscRequestImpl.setSubscriberNumber(circleConfigParamDTO
				.getSubscriberNumber());
		fdpussdsmscRequestImpl
				.setIncomingSubscriberNumber(circleConfigParamDTO
						.getIncomingSubscriberNumber());

		fdpussdsmscRequestImpl.setChannel(ChannelType.USSD);

		fdpussdsmscRequestImpl
				.setRequestString(new FDPUSSDRequestStringImpl(
						userInput));
		
        fdpussdsmscRequestImpl.putAuxiliaryRequestParameter(AuxRequestParam.ORIGINAL_MSISDN, circleConfigParamDTO.getSubscriberNumber());

	}

	private String getUserInputOld(FDPRequestImpl fdpRequestImpl, FDPSMPPRequestImpl fdpussdsmscRequestImpl, Request ussdHttpRequest, String sessionId) {
		String userInput=new String();
		if (fdpRequestImpl instanceof FDPRequest) {
			fdpussdsmscRequestImpl = (FDPSMPPRequestImpl) fdpRequestImpl;
			String inputUser = ussdHttpRequest
					.getSubscriberInput()
					.substring(
							ussdHttpRequest.getSubscriberInput()
									.lastIndexOf(FDPConstant.STAR) + 1,
							ussdHttpRequest.getSubscriberInput()
									.trim().length());
			userInput = inputUser.toString();
			LOGGER.debug(
					"Found value from cache for session id : {}",
					sessionId);
		}
		return userInput;
	}

	private FDPRequestImpl getCachedRequest(String msisdn) {
		FDPRequestBag fdpRequestBag = new FDPRequestBag(msisdn);
		return (FDPRequestImpl) fdpRequestCacheForUSSD
				.getValue(fdpRequestBag);
	}

	private String updateMsisdn(String msisdn, Integer allowedLength) throws ExecutionFailedException {
		if (msisdn.length() <= allowedLength) {
			final FDPAppBag bag = new FDPAppBag();
			bag.setKey(ConfigurationKey.COUNTRY_CODE.getAttributeName());
			bag.setSubStore(AppCacheSubStore.CONFIGURATION_MAP);
			final FDPCache<FDPAppBag, Object> fdpCache = ApplicationConfigUtil
					.getApplicationConfigCache();
			final String countryCode = (String) fdpCache.getValue(bag);
			msisdn = countryCode + msisdn;
		}
		return msisdn;
	}

	/**
	 * This method will return change the input add * at begining and # at end
	 * 
	 * @param msisdn
	 * @param ussdinput
	 * @return
	 */
	private boolean checkfirstRequest(String msisdn, String ussdinput) {
		final FDPRequestBag fdpRequest = new FDPRequestBag(msisdn);
		boolean checkfirstrequest = false;
		if (ussdinput.trim().startsWith(FDPConstant.USSD_STRING_START_CHAR)
				&& ussdinput.trim().endsWith(FDPConstant.USSD_STRING_END_CHAR)) {
			return false;
		} else if (fdpRequestCacheForUSSD.getValue(fdpRequest) != null) {
			checkfirstrequest = true;
		}
		return checkfirstrequest;
	}

	/**
	 * This method is used to generate logs for user behaviour after processing
	 * has finished.
	 * 
	 * @param dynamicMenuRequest
	 *            the request.
	 * @param exchange
	 *            Exchange object.
	 *  @throws ExecutionFailedException
	 */
	
	private void preProcessingOfLogs(final FDPSMPPRequest dynamicMenuRequest, final Exchange exchange) throws ExecutionFailedException {
		    String inip = String.valueOf(exchange.getIn().getHeader(FDPRouteHeaders.INCOMING_IP.getValue()));
		    String channel = null;
			
			if (dynamicMenuRequest.getChannel() != null) {
				channel = dynamicMenuRequest.getChannel().name();
			}
			
			final String requestAppender = LoggerUtil.getRequestAppender(dynamicMenuRequest);
		    final StringBuilder appenderValue = new StringBuilder();
			appenderValue.append(requestAppender).append("ACTN").append(FDPConstant.LOGGER_KEY_VALUE_DELIMITER)
					.append(dynamicMenuRequest.getRequestStringInterface().getActionString())
					.append(FDPConstant.LOGGER_DELIMITER)
					.append(FDPConstant.INCOMING_IP).append(FDPConstant.LOGGER_KEY_VALUE_DELIMITER).append(inip)
					.append(FDPConstant.LOGGER_DELIMITER)
					.append(FDPConstant.LOGICAL_NAME).append(FDPConstant.LOGGER_KEY_VALUE_DELIMITER).append("USSD")
					.append(FDPConstant.LOGGER_DELIMITER)
					.append("CH").append(FDPConstant.LOGGER_KEY_VALUE_DELIMITER)
					.append(channel);
			appenderValue.append(FDPConstant.LOGGER_DELIMITER).append(FDPConstant.MSISDN).append(FDPConstant.LOGGER_KEY_VALUE_DELIMITER).
			append(dynamicMenuRequest.getSubscriberNumber());
			
			final Logger circleLogger = LoggerUtil.getSummaryLoggerFromRequest(dynamicMenuRequest);
			FDPLogger.info(circleLogger, getClass(), "preProcessingOfLogs()", appenderValue.toString());
			if (ChannelType.SMS.equals(dynamicMenuRequest.getChannel())) {
				FDPLogger.info(circleLogger, getClass(), "preProcessingOfLogs()", requestAppender
						+ "SCD" + FDPConstant.LOGGER_KEY_VALUE_DELIMITER
						+ dynamicMenuRequest.getRequestStringInterface().getNodeString());
			}
			
	}

}
