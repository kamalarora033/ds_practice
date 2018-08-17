package com.ericsson.fdp.business.menu.externalservice;

import java.net.UnknownHostException;

import javax.naming.NamingException;

import org.slf4j.Logger;

import com.ericsson.fdp.business.enums.AuxRequestParam;
import com.ericsson.fdp.business.menu.FDPDynamicMenu;
import com.ericsson.fdp.business.request.requestString.impl.FDPSMSCRequestStringImpl;
import com.ericsson.fdp.business.request.requestString.impl.FDPUSSDRequestStringImpl;
import com.ericsson.fdp.business.util.LoggerUtil;
import com.ericsson.fdp.business.util.RequestUtil;
import com.ericsson.fdp.common.constants.FDPConstant;
import com.ericsson.fdp.common.enums.ChannelType;
import com.ericsson.fdp.common.enums.ExternalSystem;
import com.ericsson.fdp.common.vo.CircleConfigParamDTO;
import com.ericsson.fdp.common.vo.FDPCircle;
import com.ericsson.fdp.core.exception.ExecutionFailedException;
import com.ericsson.fdp.core.request.FDPRequest;
import com.ericsson.fdp.core.request.FDPResponse;
import com.ericsson.fdp.core.request.impl.FDPSMPPRequestImpl;
import com.ericsson.fdp.dao.entity.ExternalSystemType;

public abstract class AbstractFDPDynamicMenuExecutorService implements FDPDynamicMenuExecutorService{

	/**
	 * The circle logger.
	 */
	private Logger circleLogger = null;
	
	
	@Override
	public FDPResponse executeDynamicMenu(final FDPSMPPRequestImpl fdpsmppRequestImpl, final FDPDynamicMenu dynamicMenu) throws ExecutionFailedException {
		final FDPResponse fdpResponse = dynamicMenu.executeDynamicMenu(fdpsmppRequestImpl);
		return fdpResponse;
	}
	
	public abstract void preProcessingOfLogs(final FDPSMPPRequestImpl fdpsmppRequestImpl, final String incomingIP);

	/**
	 * This method is used to create request object.
	 * 
	 * @param input
	 *            the input to be used.
	 * @param msisdn
	 *            the msisdn.
	 * @param channel
	 *            the channel.
	 * @return the request object.
	 * @throws UnknownHostException
	 *             Exception for unknown host.
	 * @throws NamingException
	 *             Exception in naming.
	 * @throws ExecutionFailedException
	 *             Exception in execution.
	 */
	public FDPSMPPRequestImpl createRequestObject(final String input, final Long msisdn, final ChannelType channel,
			final String requestId , final String systemType, final FDPCircle fdpCircle) throws UnknownHostException, NamingException, ExecutionFailedException {
		final FDPSMPPRequestImpl fdpussdsmscRequestImpl = new FDPSMPPRequestImpl();
		ExternalSystemType externalSystemType = ExternalSystemType.getSystemTypeEnum(systemType);
		ExternalSystem externalSystem = getExternalSystem(externalSystemType);
		if(null == externalSystem) {
			throw new ExecutionFailedException("Illegal System Type for externalSystemType:"+systemType);
		}
		fdpussdsmscRequestImpl.putAuxiliaryRequestParameter(AuxRequestParam.REQUESTING_EXTERNAL_SYSTEM,
				externalSystem);
		/*
		 * requestId = channel.name() + "_" +
		 * Inet4Address.getLocalHost().getHostAddress() + "_" +
		 * (String.valueOf(UUID.randomUUID()));
		 */
		fdpussdsmscRequestImpl.setRequestId(requestId);
		//final FDPCircle fdpCircle = RequestUtil.getFDPCircleFromMsisdn(msisdn.toString());

		fdpussdsmscRequestImpl.setCircle(fdpCircle);
		final CircleConfigParamDTO circleConfigParamDTO = RequestUtil.populateCircleConfigParamDTO(msisdn.toString(),
				fdpCircle);
		fdpussdsmscRequestImpl.setCircle(fdpCircle);
		String sessionId = requestId;
		if (ChannelType.SMS.equals(channel)) {
			final String destAddr = input.substring(0, input.indexOf(FDPConstant.SPACE));
			sessionId = destAddr + msisdn;
			fdpussdsmscRequestImpl.setChannel(ChannelType.SMS);
			fdpussdsmscRequestImpl.setRequestString(new FDPSMSCRequestStringImpl(destAddr, input.substring(input
					.indexOf(FDPConstant.SPACE) + 1), destAddr));
		} else {
			fdpussdsmscRequestImpl.setChannel(ChannelType.USSD);
			fdpussdsmscRequestImpl.setRequestString(new FDPUSSDRequestStringImpl(input));
		}
		fdpussdsmscRequestImpl.setSessionId(sessionId);
		fdpussdsmscRequestImpl.setOriginNodeType(circleConfigParamDTO.getOriginNodeType());
		fdpussdsmscRequestImpl.setSubscriberNumberNAI(circleConfigParamDTO.getSubscriberNumberNAI());
		fdpussdsmscRequestImpl.setOriginHostName(circleConfigParamDTO.getOriginHostName());
		fdpussdsmscRequestImpl.setSubscriberNumber(circleConfigParamDTO.getSubscriberNumber());
		fdpussdsmscRequestImpl.setIncomingSubscriberNumber(circleConfigParamDTO.getIncomingSubscriberNumber());
		return fdpussdsmscRequestImpl;
	}

	/**
	 * Gets the circle logger.
	 * 
	 * @param fdpRequest
	 *            the fdp request
	 * @return the circle logger
	 */
	protected Logger getCircleLogger(final FDPRequest fdpRequest) {
		if (circleLogger == null) {
			circleLogger = LoggerUtil.getSummaryLoggerForVAS(fdpRequest);
		}
		return circleLogger;
	}

	/**
	 * This method return the ExernalSytem on baseis on externalSytemType
	 * 
	 * @param externalSystemType
	 * @return
	 */
	private ExternalSystem getExternalSystem(final ExternalSystemType externalSystemType) {
		switch (externalSystemType) {
		case MCARBON_TYPE :
			return ExternalSystem.MCARBON;
		case MANHATTAN_TYPE:
			return ExternalSystem.MANHATTAN;
		}
		return null;
	}
	

}
