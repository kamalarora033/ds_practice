package com.ericsson.fdp.business.route.processor;

import java.util.List;

import javax.annotation.Resource;

import org.apache.camel.Exchange;
import org.apache.camel.Message;
import org.apache.camel.Processor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ericsson.fdp.business.constants.BusinessConstants;
import com.ericsson.fdp.business.enums.ConfigurationKey;
import com.ericsson.fdp.business.enums.DisplayArea;
import com.ericsson.fdp.business.enums.TLVOptions;
import com.ericsson.fdp.business.enums.VASErrorCodeEnum;
import com.ericsson.fdp.business.enums.ivr.FulfillmentParameters;
import com.ericsson.fdp.business.menu.FDPDynamicMenu;
import com.ericsson.fdp.business.menu.externalservice.AbstractFDPDynamicMenuExecutorService;
import com.ericsson.fdp.business.menu.externalservice.enums.FDPDynamicMenuExecutorServiceEnum;
import com.ericsson.fdp.business.request.ResponseStatus;
import com.ericsson.fdp.business.service.DynamicMenuItegrator;
import com.ericsson.fdp.business.util.RequestUtil;
import com.ericsson.fdp.common.enums.ChannelType;
import com.ericsson.fdp.common.enums.ExternalSystem;
import com.ericsson.fdp.common.enums.Status;
import com.ericsson.fdp.common.exception.FDPServiceException;
import com.ericsson.fdp.common.vo.FDPCircle;
import com.ericsson.fdp.core.cache.AppCacheSubStore;
import com.ericsson.fdp.core.config.utils.FDPApplicationFeaturesEnum;
import com.ericsson.fdp.core.display.ResponseMessage;
import com.ericsson.fdp.core.dsm.framework.service.FDPCache;
import com.ericsson.fdp.core.exception.ExecutionFailedException;
import com.ericsson.fdp.core.request.FDPResponse;
import com.ericsson.fdp.core.request.impl.FDPSMPPRequestImpl;
import com.ericsson.fdp.core.serviceprovisioning.bean.FDPAppBag;
import com.ericsson.fdp.core.utils.ApplicationConfigUtil;
import com.ericsson.fdp.core.utils.CircleCodeFinder;
import com.ericsson.fdp.dao.dto.ExchangeMessageResponse;
import com.ericsson.fdp.dao.dto.FDPSMSCConfigDTO;
import com.ericsson.fdp.dao.dto.SMPPServerMappingDTO;
import com.ericsson.fdp.dao.entity.ExternalSystemType;
import com.ericsson.fdp.route.constant.RoutingConstant;
import com.ericsson.fdp.smpp.util.SMPPUtil;
import com.google.gson.Gson;

/**
 * The Class VASServiceRequestProcessor processes the incoming request from VAS
 * external systems.
 * 
 * @author Ericsson
 */
public class VASServiceRequestProcessor implements Processor {

	/** The dynamic menu itegrator. */
	@Resource(lookup = "java:app/fdpBusiness-1.0/DynamicMenuItegratorImpl")
	private DynamicMenuItegrator dynamicMenuItegrator;

	/** The Constant LOGGER. */
	private static final Logger LOGGER = LoggerFactory
			.getLogger(VASServiceRequestProcessor.class);

	/** The application config cache. */
	private FDPCache<FDPAppBag, Object> applicationConfigCache;

	/** The fdp dynamic menu. */
	@Resource(lookup = "java:app/fdpBusiness-1.0/DynamicMenuImpl")
	private FDPDynamicMenu fdpDynamicMenu;

	/**
	 * Process.
	 * 
	 * @param exchange
	 *            the exchange
	 * @throws Exception
	 *             the exception
	 */
	@Override
	public void process(Exchange exchange) throws Exception {
		String circleCode = null;
		String responseString = null;
		FDPCircle fdpCircle = null;
		Message in = exchange.getIn();
		String msisdn = in.getHeader(BusinessConstants.MSISDN, String.class);
		String channel = in.getHeader(BusinessConstants.CHANNEL_TYPE,
				String.class);
		String input = in.getHeader(BusinessConstants.INPUT, String.class);
		String systemType = in.getHeader(BusinessConstants.SYSTEM_TYPE,
				String.class);
		String inComingIpAdress = in.getHeader(
				RoutingConstant.INCOMING_IP_ADDRESS, String.class);
		input = checkPrefixForInput(input);
		responseString = checkManadatoryParameter(msisdn, systemType, input,
				channel);
		applicationConfigCache = ApplicationConfigUtil
				.getApplicationConfigCache();
		if (FDPApplicationFeaturesEnum
				.isFeatureAllowed(
						FDPApplicationFeaturesEnum.FEATURE_IS_IMSI_BASED_CIRCLE_CHECK_ALLOW,
						applicationConfigCache)) {
			circleCode = getCircleCodeFromRequest(in);
			if (null == circleCode) {
				responseString = getCircleNotFoundResponse();
			} else {
				fdpCircle = CircleCodeFinder.getFDPCircleByCode(circleCode,
						applicationConfigCache);
			}
		} else {
			fdpCircle = RequestUtil.getFDPCircleFromMsisdn(msisdn.toString());
		}
		if (null == fdpCircle) {
			responseString = getCircleNotFoundResponse();
		}

		if ("null".equals(responseString)) {
			String requestId = in.getHeader(BusinessConstants.REQUEST_ID,
					String.class);
			ChannelType channelType = null;
			if (channel.equalsIgnoreCase(ChannelType.SMS.name())) {
				channelType = ChannelType.SMS;
			} else {
				channelType = ChannelType.USSD;
				validateUSSDInputString(input);
			}
			msisdn = checkNumberWithCouuntryCode(msisdn);
			AbstractFDPDynamicMenuExecutorService fdpDynamicMenuExecutorService = FDPDynamicMenuExecutorServiceEnum
					.getObject(ExternalSystemType.getSystemTypeEnum(systemType)
							.getValue());
			FDPSMPPRequestImpl fdpsmppRequestImpl = fdpDynamicMenuExecutorService
					.createRequestObject(input, Long.valueOf(msisdn),
							channelType, requestId, systemType, fdpCircle);
			fdpDynamicMenuExecutorService.preProcessingOfLogs(
					fdpsmppRequestImpl, inComingIpAdress);
			FDPResponse fdpResponse = fdpDynamicMenuExecutorService
					.executeDynamicMenu(fdpsmppRequestImpl, fdpDynamicMenu);
			ExchangeMessageResponse exchangeMessage = new ExchangeMessageResponse();
			// exchangeMessage.setCircleId(getCircleCode(msisdn));
			exchangeMessage.setCircleId(fdpCircle.getCircleCode());
			exchangeMessage.setMsgType(channel);
			exchangeMessage.setMsisdn(msisdn);
			String parsedShortCode = getSourceAddr(input, channelType);

			SMPPServerMappingDTO serverMappingDTO = getSmppServerMappingDTO(
					msisdn, fdpCircle);
			LOGGER.debug("Server Mapping DTO got from cache : {}",
					serverMappingDTO);
			exchangeMessage.setSourceAddress(parsedShortCode);
			// SMPPServerMappingDTO serverMappingDTO =
			// getConfigFromCache(parsedShortCode);
			if (null == serverMappingDTO) {
				throw new FDPServiceException(new StringBuilder("ShortCode ")
						.append(parsedShortCode).append("is not configured.")
						.toString());
			}
			LOGGER.debug("Object got from Cache: {}", serverMappingDTO);
			exchangeMessage.setIncomingTrxIpPort(getIpPort(serverMappingDTO));
			exchangeMessage.setIncomingIPAddress(serverMappingDTO.getIp());
			exchangeMessage
					.setIpPortSytemId(getIpPortSystemId(serverMappingDTO));
			exchangeMessage.setBindModeType(getBindModeType(channelType));
			exchangeMessage
					.setBindMode(getBindModeType(channelType).equals(
							BusinessConstants.BIND_NODE_TYPE_TRANSCEIVER) ? BusinessConstants.BIND_MODE_TRX
							: BusinessConstants.BIND_MODE_TXRX);
			String routeIdToPush = SMPPUtil.getUSSDEndpoint(
					(FDPSMSCConfigDTO) serverMappingDTO, 9,
					serverMappingDTO.getBindSystemId(),
					serverMappingDTO.getBindSystemPassword(), parsedShortCode);
			exchangeMessage.setRouteId(routeIdToPush);
			LOGGER.info("URL to Push :{}", routeIdToPush);
			List<ResponseMessage> messageList = fdpResponse.getResponseString();
			Status status = fdpResponse.getExecutionStatus();
			responseString = null;
			Gson gson = new Gson();
			switch (status) {
			case SUCCESS:
				// Long delay = 0L;
				// int count = 0;
				boolean flushSession = true;
				for (ResponseMessage message : messageList) {
					flushSession = flushSession
							&& message.getTLVOptions().contains(
									TLVOptions.SESSION_TERMINATE);
					ChannelType chType = message.getChannelForMessage();
					String responseBodyMessage = message
							.getCurrDisplayText(DisplayArea.COMPLETE);
					exchangeMessage.setBody(responseBodyMessage);
					List<TLVOptions> tlvOptions = message.getTLVOptions();
					exchangeMessage
							.setTerminated(isSessionTerminated(tlvOptions));
					if (ChannelType.USSD.equals(chType)) {
						String optionalParametersForUSSD = getOptionalParameterForUSSDMO(tlvOptions);
						exchangeMessage
								.setOptionalParameters(optionalParametersForUSSD);
						if (null != message.getDelayForMessage()
								&& message.getDelayForMessage() > 0) {
							// delay += message.getDelayForMessage();
							exchangeMessage.setDelayTime(message
									.getDelayForMessage());
						}
						/*
						 * if (delay > 0 && count > 0) {
						 * exchangeMessage.setDelayTime(delay); }
						 */
					}
					exchangeMessage.setRequestId(requestId);
					dynamicMenuItegrator.sendSubmitSmInOut(exchangeMessage);
					// count++;
				}
				responseString = gson.toJson(new ResponseStatus(
						BusinessConstants.VAS_SUCCESS_CODE,
						BusinessConstants.VAS_SUCCESS_STRING));
				break;
			case FAILURE:
				responseString = gson.toJson(fdpResponse.getResponseError());
				break;
			default:
				break;
			}
		}
		Message out = exchange.getOut();
		out.setBody(responseString);
	}

	/**
	 * Validate channel type.
	 *
	 * @param channel
	 *            the channel
	 * @return true, if successful
	 */
	private boolean validateChannelType(final String channel) {
		if (!channel.equalsIgnoreCase(RoutingConstant.SERVICE_TYPE_USSD)) {
			return false;
		}
		return true;
	}

	/**
	 * Gets the config object.
	 * 
	 * @param msisdn
	 *            the msisdn
	 * @return the config object
	 * @throws ExecutionFailedException
	 *             the execution failed exception
	 */
	private Object getConfigObject(final String msisdn,
			final FDPCircle fdpCircle) throws ExecutionFailedException {
		AppCacheSubStore appCacheSubStore = AppCacheSubStore.VAS_USSD_PUSH_MO;
		final String circleCode = fdpCircle.getCircleCode();
		LOGGER.debug("Circle Code Got By MSISDN:{}", circleCode);
		String key = new StringBuilder().append(circleCode)
				.append(BusinessConstants.UNDERSCORE)
				.append(ExternalSystemType.USSD_TYPE.getValue()).toString();
		return applicationConfigCache.getValue(new FDPAppBag(appCacheSubStore,
				key));
	}

	/**
	 * Gets the smpp server mapping dto.
	 * 
	 * @param msisdn
	 *            the msisdn
	 * @return the smpp server mapping dto
	 * @throws ExecutionFailedException
	 *             the execution failed exception
	 */
	@SuppressWarnings("unchecked")
	private SMPPServerMappingDTO getSmppServerMappingDTO(final String msisdn,
			final FDPCircle fdpCircle) throws ExecutionFailedException {
		Object configDTOObj = getConfigObject(msisdn, fdpCircle);
		if (configDTOObj != null && configDTOObj instanceof List<?>) {
			LOGGER.debug("Checking with config dto objects");
			final List<SMPPServerMappingDTO> configDTOs = (List<SMPPServerMappingDTO>) configDTOObj;
			for (final SMPPServerMappingDTO smppServerMappingDTO : configDTOs) {
				return smppServerMappingDTO;
			}
		}
		return null;
	}

	/**
	 * Gets the optional parameter for ussdmo.
	 * 
	 * @param tlvOptions
	 *            the tlv options
	 * @return the optional parameter for ussdmo
	 */
	private String getOptionalParameterForUSSDMO(List<TLVOptions> tlvOptions) {
		StringBuilder optionalParameterString = new StringBuilder();
		for (TLVOptions option : tlvOptions) {
			switch (option) {
			case FLASH:
				optionalParameterString.append("USSD_SERVICE_OP,17,");
				break;
			case SESSION_TERMINATE:
				optionalParameterString.append("USSD_SERVICE_OP,17,");
				break;
			case SESSION_CONTINUE:
				optionalParameterString.append("USSD_SERVICE_OP,2,");
				break;
			default:
				optionalParameterString.append("USSD_SERVICE_OP,2,");
				break;
			}

		}
		return optionalParameterString.toString();
	}

	/**
	 * Gets the circle code.
	 * 
	 * @param msisdn
	 *            the msisdn
	 * @return the circle code
	 * @throws ExecutionFailedException
	 *             the execution failed exception
	 */
	private String getCircleCode(String msisdn) throws ExecutionFailedException {
		FDPCache<FDPAppBag, Object> fdpCache = ApplicationConfigUtil
				.getApplicationConfigCache();
		String circleCode = CircleCodeFinder.getCircleCode(msisdn, fdpCache);
		return circleCode;
	}

	/**
	 * Gets the source addr.
	 * 
	 * @param input
	 *            the input
	 * @param channelType
	 *            the channel type
	 * @return the source addr
	 */
	private String getSourceAddr(final String input,
			final ChannelType channelType) {
		String destAddr = null;
		switch (channelType) {
		case USSD:
			destAddr = input.split("\\*")[1].trim();
			if (destAddr.indexOf("#") > 0) {
				destAddr = destAddr.substring(0, destAddr.indexOf("#"));
			}
			break;
		case SMS:
			destAddr = input.split(" ")[0].trim();
		default:
			break;
		}

		return destAddr;
	}

	/**
	 * Checks if is session terminated.
	 * 
	 * @param tlvOptions
	 *            the tlv options
	 * @return true, if is session terminated
	 */
	private boolean isSessionTerminated(List<TLVOptions> tlvOptions) {
		if (tlvOptions.contains(TLVOptions.SESSION_TERMINATE)) {
			return true;
		}
		return false;
	}

	/**
	 * Gets the bind mode type.
	 * 
	 * @param channelType
	 *            the channel type
	 * @return the bind mode type
	 */
	private String getBindModeType(final ChannelType channelType) {
		String bindModeType = null;
		switch (channelType) {
		case USSD:
			bindModeType = BusinessConstants.BIND_NODE_TYPE_TRANSCEIVER;
			break;
		case SMS:
			bindModeType = BusinessConstants.BIND_NODE_TYPE_TRANSMITTER;
			break;
		default:
			break;
		}
		return bindModeType;
	}

	/**
	 * Check manadatory parameter.
	 *
	 * @param msisdn
	 *            the msisdn
	 * @param systemType
	 *            the system type
	 * @param input
	 *            the input
	 * @param channel
	 *            the channel
	 * @return the string
	 */
	private String checkManadatoryParameter(final String msisdn,
			final String systemType, final String input, final String channel) {
		ResponseStatus responseError = null;
		if (null == msisdn || "".equals(msisdn) || !validateMSISDN(msisdn)) {
			VASErrorCodeEnum vasErrorCodeEnum = VASErrorCodeEnum
					.getErrorCodeAndErrorMessage("msisdn");
			responseError = new ResponseStatus(vasErrorCodeEnum.getErrorCode(),
					vasErrorCodeEnum.getErrorMessage());
		} else if (null == systemType || "".equals(systemType)
				|| !validateSystemType(systemType)) {
			VASErrorCodeEnum vasErrorCodeEnum = VASErrorCodeEnum
					.getErrorCodeAndErrorMessage("systemtype");
			responseError = new ResponseStatus(vasErrorCodeEnum.getErrorCode(),
					vasErrorCodeEnum.getErrorMessage());
		} else if (null == input || "".equals(input)) {
			VASErrorCodeEnum vasErrorCodeEnum = VASErrorCodeEnum
					.getErrorCodeAndErrorMessage("input");
			responseError = new ResponseStatus(vasErrorCodeEnum.getErrorCode(),
					vasErrorCodeEnum.getErrorMessage());
		} else if (null == channel || "".equals(channel)) {
			VASErrorCodeEnum vasErrorCodeEnum = VASErrorCodeEnum
					.getErrorCodeAndErrorMessage("channeltype");
			responseError = new ResponseStatus(vasErrorCodeEnum.getErrorCode(),
					vasErrorCodeEnum.getErrorMessage());
		} else if (!validateChannelType(channel)) {
			VASErrorCodeEnum vasErrorCodeEnum = VASErrorCodeEnum
					.getErrorCodeAndErrorMessage("unsupported_channel_type");
			responseError = new ResponseStatus(vasErrorCodeEnum.getErrorCode(),
					vasErrorCodeEnum.getErrorMessage());
		} else {
			LOGGER.debug(
					"Parameter Got MSISDN : {} , input : {} , channelType : {} , systemType : {} ",
					new Object[] { msisdn, input, channel, systemType });
		}
		return new Gson().toJson(responseError);
	}

	/**
	 * Validate msisdn.
	 *
	 * @param msisdn
	 *            the msisdn
	 * @return true, if successful
	 */
	private boolean validateMSISDN(final String msisdn) {
		boolean flag = false;
		final Integer allowedLength = Integer.parseInt((String) applicationConfigCache.getValue(new FDPAppBag(AppCacheSubStore.CONFIGURATION_MAP,
				ConfigurationKey.MSISDN_NUMBER_LENGTH.getAttributeName())));

		String countryCode = (String) applicationConfigCache.getValue(new FDPAppBag(AppCacheSubStore.CONFIGURATION_MAP,
						ConfigurationKey.COUNTRY_CODE.getAttributeName()));
		
		Integer countryCodeLength = Integer.parseInt(countryCode);
		
		if (msisdn.startsWith(countryCode) && msisdn.length() == (allowedLength + countryCodeLength)) {
			flag = true;
		}
		if (msisdn.length() == allowedLength) {
			flag = true;
		}
		return flag;
	}

	/**
	 * Validate system type.
	 *
	 * @param systemType
	 *            the system type
	 * @return true, if successful
	 */
	private boolean validateSystemType(final String systemType) {
		boolean flag = false;
		if (systemType.equalsIgnoreCase(ExternalSystem.MANHATTAN.name())
				|| systemType.equalsIgnoreCase(ExternalSystem.MCARBON.name())) {
			flag = true;
		}
		return flag;
	}

	/**
	 * Gets the circle code by msisdn.
	 * 
	 * @param msisdn
	 *            the msisdn
	 * @return the circle code by msisdn
	 * @throws ExecutionFailedException
	 *             the execution failed exception
	 */
	private String getCircleCodeByMSISDN(final String msisdn)
			throws ExecutionFailedException {
		return CircleCodeFinder.getCircleCode(msisdn, applicationConfigCache);
	}

	/**
	 * Validate ussd input string.
	 * 
	 * @param inputString
	 *            the input string
	 */
	private void validateUSSDInputString(final String inputString) {
		if (!(inputString.startsWith("*") && inputString.endsWith("#"))) {
			throw new IllegalArgumentException(
					"Input String must start with * and ends with #");
		}
	}

	/**
	 * Gets the ip port system id.
	 * 
	 * @param smppServerMappingDTO
	 *            the smpp server mapping dto
	 * @return the ip port system id
	 */
	private String getIpPortSystemId(SMPPServerMappingDTO smppServerMappingDTO) {
		return new StringBuilder(smppServerMappingDTO.getIp())
				.append(BusinessConstants.COLON)
				.append(smppServerMappingDTO.getPort())
				.append(BusinessConstants.COLON)
				.append(smppServerMappingDTO.getBindSystemId()).toString();
	}

	/**
	 * Gets the ip port.
	 * 
	 * @param serverMappingDTO
	 *            the server mapping dto
	 * @return the ip port
	 */
	private String getIpPort(SMPPServerMappingDTO serverMappingDTO) {
		return new StringBuilder(serverMappingDTO.getIp())
				.append(BusinessConstants.COLON)
				.append(serverMappingDTO.getPort()).toString();
	}

	/**
	 * Check number with couuntry code.
	 * 
	 * @param msisdn
	 *            the msisdn
	 * @return the string
	 */
	private String checkNumberWithCouuntryCode(String msisdn) {
		String countryCode = (String) applicationConfigCache
				.getValue(new FDPAppBag(AppCacheSubStore.CONFIGURATION_MAP,
						ConfigurationKey.COUNTRY_CODE.getAttributeName()));
		final Integer allowedLength = Integer.parseInt((String) applicationConfigCache.getValue(new FDPAppBag(AppCacheSubStore.CONFIGURATION_MAP,
				ConfigurationKey.MSISDN_NUMBER_LENGTH.getAttributeName())));

		if (null != msisdn && !"".equals(msisdn)) {
			if (msisdn.length() == allowedLength) {
				msisdn = countryCode + msisdn;
				// msisdn = "91" + msisdn;
			}
		}
		return msisdn;
	}

	/**
	 * Check prefix for input.getCircleNotFoundResponser
	 * 
	 * 
	 * @param inputString
	 *            the input string
	 * @return the string
	 */
	private static String checkPrefixForInput(String inputString) {
		if (!inputString.startsWith("*")) {
			inputString = "*" + inputString;
		}
		if (inputString.contains("%23")) {
			inputString = inputString.replace("%23", "#");
		}
		return inputString;
	}

	/**
	 * This method fetches the circle code from request.
	 * 
	 * @param message
	 * @return
	 */
	private String getCircleCodeFromRequest(final Message message) {
		String circleCode = null;
		circleCode = (String) message.getHeader(VASErrorCodeEnum.CIRCLEID
				.getKey());
		return circleCode;
	}

	/**
	 * This method will prepare the resposne message when circle is invalid or
	 * not found.
	 * 
	 * @return
	 */
	private String getCircleNotFoundResponse() {
		final VASErrorCodeEnum vasErrorCodeEnum = VASErrorCodeEnum.CIRCLEID;
		return new Gson().toJson(new ResponseStatus(vasErrorCodeEnum
				.getErrorCode(), vasErrorCodeEnum.getErrorMessage()));
	}
}
