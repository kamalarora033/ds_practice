package com.ericsson.fdp.business.menu.impl;

import java.util.ArrayList;
import java.util.List;

import javax.annotation.Resource;

import ch.qos.logback.classic.Logger;

import com.ericsson.fdp.business.enums.AuxRequestParam;
import com.ericsson.fdp.business.node.AbstractMenuNode;
import com.ericsson.fdp.business.request.requestString.impl.FDPSMSCRequestStringImpl;
import com.ericsson.fdp.business.util.DynamicMenuUtil;
import com.ericsson.fdp.business.util.LoggerUtil;
import com.ericsson.fdp.business.util.RequestUtil;
import com.ericsson.fdp.common.constants.FDPConstant;
import com.ericsson.fdp.common.constants.JNDILookupConstant;
import com.ericsson.fdp.core.cache.AppCacheSubStore;
import com.ericsson.fdp.core.dsm.framework.service.FDPCache;
import com.ericsson.fdp.core.exception.ExecutionFailedException;
import com.ericsson.fdp.core.logging.FDPLogger;
import com.ericsson.fdp.core.node.FDPNode;
import com.ericsson.fdp.core.request.FDPRequest;
import com.ericsson.fdp.core.request.FDPSMPPRequest;
import com.ericsson.fdp.core.request.requestString.FDPRequestString;
import com.ericsson.fdp.core.serviceprovisioning.bean.FDPAppBag; 
import com.ericsson.fdp.business.util.NotificationUtil;
import com.ericsson.fdp.core.utils.ApplicationConfigUtil;



/**
 * This class is used for methods that are used by requests from SMSC.
 * 
 * @author Ericsson
 * 
 */
public class SMSCDynamicMenuImpl {

	/** The application config cache. */
	@Resource(lookup = JNDILookupConstant.APPLICATION_CONFIG_CACHE_JNDI_NAME)
	private FDPCache<FDPAppBag, Object> applicationConfigCache;

	/**
	 * The circle logger.
	 */
	private static Logger circleLogger = null;

	/**
	 * This method is sued to get the circle logger.
	 * 
	 * @param fdpRequest
	 *            the request to get the circle logger.
	 * @return the circle logger.
	 */
	private Logger getCircleLogger(final FDPRequest fdpRequest) {
		if (circleLogger == null) {
			circleLogger = LoggerUtil.getSummaryLoggerFromRequest(fdpRequest);
		}
		return circleLogger;
	}

	/**
	 * This method is used to check for SMS cases.
	 * 
	 * @param dynamicMenuRequest
	 *            the request.
	 * @return the node found.
	 * @throws ExecutionFailedException
	 *             Exception in execution.
	 */
	public FDPNode checkSMSCase(final FDPSMPPRequest dynamicMenuRequest) throws ExecutionFailedException {
		FDPNode fdpNode = checkIfFlexiRechargeOrSharedAccountForSMS(dynamicMenuRequest);
		if (fdpNode == null) {
			fdpNode = checkForSMSMainNode(dynamicMenuRequest);
		}
		if(fdpNode == null){			
			NotificationUtil.sendOfflineNotification(dynamicMenuRequest,(String) ApplicationConfigUtil.getApplicationConfigCache().getValue(
					new FDPAppBag(AppCacheSubStore.CONFIGURATION_MAP, FDPConstant.SYSTEM_HELP_DYNAMIC_MENU)));
		}
		return fdpNode;
	}

	/**
	 * This method checks for other cases of sms node.
	 * 
	 * @param dynamicMenuRequest
	 *            the request object.
	 * @return the node found.
	 * @throws ExecutionFailedException
	 *             Exception in execution.
	 */
	private FDPNode checkForSMSOtherNode(final FDPSMPPRequest dynamicMenuRequest) throws ExecutionFailedException {
		if (dynamicMenuRequest.getLastServedString() != null) {
			final FDPNode fdpNode = DynamicMenuUtil.getAndUpdateFDPNode(dynamicMenuRequest.getCircle(),
					dynamicMenuRequest.getLastServedString(), dynamicMenuRequest);
			if (fdpNode instanceof AbstractMenuNode) {
				return fdpNode;
			}
		}
		return null;
	}

	/**
	 * This method checks for the main node on sms.
	 * 
	 * @param dynamicMenuRequest
	 *            the request node.
	 * @return the main node.
	 * @throws ExecutionFailedException
	 *             Exception in execution.
	 */
	private FDPNode checkForSMSMainNode(final FDPSMPPRequest dynamicMenuRequest) throws ExecutionFailedException {
		FDPNode fdpNode = null;
		if (dynamicMenuRequest.getRequestStringInterface() instanceof FDPSMSCRequestStringImpl) {
			fdpNode = DynamicMenuUtil.getAndUpdateFDPNode(
					dynamicMenuRequest.getCircle(),
					((FDPSMSCRequestStringImpl) dynamicMenuRequest.getRequestStringInterface())
							.getDesitinationAddressOfRequest()
							+ FDPConstant.SPACE
							+ dynamicMenuRequest.getRequestString(), dynamicMenuRequest);
		}
		return fdpNode;
	}

	/**
	 * This method is used to check for flexi recharge.
	 * 
	 * @param dynamicMenuRequest
	 *            The request object.
	 * @return The node for flexi recharge.
	 * @throws ExecutionFailedException
	 *             Exception in execution.
	 */
	private FDPNode checkIfFlexiRechargeOrSharedAccountForSMS(final FDPSMPPRequest dynamicMenuRequest)
			throws ExecutionFailedException {
		final Integer flexiRechargelength = Integer.parseInt((String) applicationConfigCache.getValue(new FDPAppBag(
				AppCacheSubStore.CONFIGURATION_MAP, FDPConstant.FLEXI_RECHARGE_CODE_LENGTH)));
		final Integer sharedAccountlength = Integer.parseInt((String) applicationConfigCache.getValue(new FDPAppBag(
				AppCacheSubStore.CONFIGURATION_MAP, FDPConstant.SHARED_ACCOUNT_CODE_LENGTH)));
		final FDPRequestString requestString = dynamicMenuRequest.getRequestStringInterface();
		if (requestString instanceof FDPSMSCRequestStringImpl) {
			final FDPSMSCRequestStringImpl fdpsmscRequestStringImpl = (FDPSMSCRequestStringImpl) requestString;
			final String requestingString = fdpsmscRequestStringImpl.getRequestStringWithSpace().trim();
			final String[] requestSplitString = requestingString.split(FDPConstant.SPACE);
			return getNodeForFlexiRechargeOrSharedAccountForSMS(dynamicMenuRequest, flexiRechargelength,
					sharedAccountlength, requestSplitString);
		}
		return null;
	}

	/**
	 * This method is used to get the flexi recharge or shared account node.
	 * 
	 * @param dynamicMenuRequest
	 *            the request object.
	 * @param flexiRechargelength
	 *            the flexi recharge configured length.
	 * @param sharedAccountlength
	 *            the shared account configured length.
	 * @param requestSplitString
	 *            the splitted string.
	 * @return the node.
	 * @throws ExecutionFailedException
	 *             Exception in execution.
	 */
	private FDPNode getNodeForFlexiRechargeOrSharedAccountForSMS(final FDPSMPPRequest dynamicMenuRequest,
			final Integer flexiRechargelength, final Integer sharedAccountlength, final String[] requestSplitString)
			throws ExecutionFailedException {
		FDPNode fdpNode = null;
		boolean flexiRechargeCase = false;
		final List<String> inputValues = new ArrayList<String>();
		final StringBuilder flexiRechargeNodeString = new StringBuilder();
		for (final String requestPartString : requestSplitString) {
			if (requestPartString.length() >= flexiRechargelength) {
				inputValues.add(requestPartString);
				flexiRechargeCase = true;
			} else if (!requestPartString.isEmpty()) {
				flexiRechargeNodeString.append(requestPartString);
			}
		}
		RequestUtil.putAuxiliaryValueInRequest(dynamicMenuRequest, AuxRequestParam.CURRENT_SHARED_ACCOUNT_VALUE,
				inputValues);
		if (flexiRechargeCase) {
			final String finalString = flexiRechargeNodeString.toString();
			FDPLogger.debug(getCircleLogger(dynamicMenuRequest), getClass(),
					"getNodeForFlexiRechargeOrSharedAccount()", LoggerUtil.getRequestAppender(dynamicMenuRequest)
							+ "Flexi recharge case found for string " + finalString);
			fdpNode = DynamicMenuUtil.getFlexiRechargeNode(getCircleLogger(dynamicMenuRequest), DynamicMenuUtil
					.getAndUpdateFDPNode(dynamicMenuRequest.getCircle(), finalString, dynamicMenuRequest), finalString,
					dynamicMenuRequest.getCircle());
		}
		if (fdpNode == null) {
			fdpNode = getSharedAccountNodeForSMS(requestSplitString, dynamicMenuRequest);
		}
		return fdpNode;
	}

	/**
	 * This method is used to get the shared account node.
	 * 
	 * @param requestSplitString
	 *            the request string.
	 * @param dynamicMenuRequest
	 *            the dynamic menu request.
	 * @return the node.
	 * @throws ExecutionFailedException
	 *             Exception in execution.
	 */
	private FDPNode getSharedAccountNodeForSMS(final String[] requestSplitString,
			final FDPSMPPRequest dynamicMenuRequest) throws ExecutionFailedException {
		final StringBuilder sharedAccountNodeString = new StringBuilder();
		final List<String> inputValues = new ArrayList<String>();
		FDPNode node = null;
		for (final String requestPartString : requestSplitString) {
			if (node == null) {
				if (!requestPartString.isEmpty()) {
					sharedAccountNodeString.append(requestPartString);
				}
				node = DynamicMenuUtil.getSharedAccountNodeOrPolicyNode(
						DynamicMenuUtil.getAndUpdateFDPNode(dynamicMenuRequest.getCircle(),
								sharedAccountNodeString.toString(), dynamicMenuRequest),
						dynamicMenuRequest.isPolicyExecution());
			} else {
				inputValues.add(requestPartString);
			}
		}
		RequestUtil.putAuxiliaryValueInRequest(dynamicMenuRequest, AuxRequestParam.CURRENT_SHARED_ACCOUNT_VALUE,
				inputValues);
		return node;
	}
}
