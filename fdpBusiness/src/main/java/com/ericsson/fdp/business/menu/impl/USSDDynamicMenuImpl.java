package com.ericsson.fdp.business.menu.impl;

import java.util.ArrayList;
import java.util.List;

import javax.annotation.Resource;

import ch.qos.logback.classic.Logger;

import com.ericsson.fdp.business.enums.AuxRequestParam;
import com.ericsson.fdp.business.request.requestString.impl.FDPUSSDRequestStringImpl;
import com.ericsson.fdp.business.util.DynamicMenuUtil;
import com.ericsson.fdp.business.util.LoggerUtil;
import com.ericsson.fdp.business.util.RequestUtil;
import com.ericsson.fdp.common.constants.FDPConstant;
import com.ericsson.fdp.common.constants.JNDILookupConstant;
import com.ericsson.fdp.common.enums.ChannelType;
import com.ericsson.fdp.core.cache.AppCacheSubStore;
import com.ericsson.fdp.core.display.DisplayObject;
import com.ericsson.fdp.core.dsm.framework.service.FDPCache;
import com.ericsson.fdp.core.exception.ExecutionFailedException;
import com.ericsson.fdp.core.logging.FDPLogger;
import com.ericsson.fdp.core.node.FDPNode;
import com.ericsson.fdp.core.request.FDPRequest;
import com.ericsson.fdp.core.request.FDPSMPPRequest;
import com.ericsson.fdp.core.request.impl.FDPSMPPRequestImpl;
import com.ericsson.fdp.core.serviceprovisioning.bean.FDPAppBag;

/**
 * This class is used for methods that are used by requests from USSD.
 * 
 * @author Ericsson
 * 
 */
public class USSDDynamicMenuImpl {

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
	 * This method is used to get node from previous request and update the
	 * current serving string in the request.
	 * 
	 * @param dynamicMenuRequest
	 *            the dynamic menu request
	 * @return the node for previous request and update request value
	 * @throws ExecutionFailedException
	 *             Exception in execution.
	 */
	public FDPNode getNodeForPreviousRequestAndUpdateRequestValue(final FDPSMPPRequest dynamicMenuRequest)
			throws ExecutionFailedException {
		FDPNode fdpNode = getNodeFromPreviousRequest(dynamicMenuRequest);
		if (fdpNode == null && ChannelType.USSD.equals(dynamicMenuRequest.getChannel())) {
			// This method is changing the request string if it is from USSD and
			// the node has not been found.
			// Also, this will not work for SMS case. Currently, SMS is not
			// interactive. Changes should be made here in case of interactive
			// SMS.
			final String newString = createRequestString(dynamicMenuRequest.getRequestString(),
					dynamicMenuRequest.getLastServedString());
			if (dynamicMenuRequest instanceof FDPSMPPRequestImpl
					&& ChannelType.USSD.equals(dynamicMenuRequest.getChannel())) {
				final FDPSMPPRequestImpl fdpsmppRequestImpl = (FDPSMPPRequestImpl) dynamicMenuRequest;
				// In case the request is from USSD and the node is not found,
				// append the new request to the last one and then try to find
				// the node.
				fdpsmppRequestImpl.setRequestString(new FDPUSSDRequestStringImpl(newString));
			}
			fdpNode = DynamicMenuUtil
					.getAndUpdateFDPNode(dynamicMenuRequest.getCircle(), newString, dynamicMenuRequest);
			getCircleLogger(dynamicMenuRequest).debug(
					LoggerUtil.getRequestAppender(dynamicMenuRequest) + "Finding new node for previous request "
							+ fdpNode);
		}
		return fdpNode;
	}

	/**
	 * This method is used to find the node from the previous request.
	 * 
	 * @param dynamicMenuRequest
	 *            The request object.
	 * @return The node for which the request has been received.
	 * @throws ExecutionFailedException
	 *             Exception in execution.
	 */
	private FDPNode getNodeFromPreviousRequest(final FDPSMPPRequest dynamicMenuRequest) throws ExecutionFailedException {
		final String currentString = dynamicMenuRequest.getRequestString();
		FDPLogger.debug(getCircleLogger(dynamicMenuRequest), getClass(), "getNodeFromPreviousRequest()",
				"Getting node from previous request served for string " + currentString);
		FDPNode fdpNode = null;
		// check if current string contains last string.
		// check if string matches pattern.
		if (!dynamicMenuRequest.isPolicyExecution()) {
			Integer index = null;
			try {
				index = Integer.valueOf(currentString);
			} catch (final NumberFormatException e) {
				FDPLogger.debug(getCircleLogger(dynamicMenuRequest), getClass(), "getNodeFromPreviousRequest()",
						LoggerUtil.getRequestAppender(dynamicMenuRequest) + "Could not parse index for string "
								+ currentString);
				if (currentString.matches(FDPConstant.USSD_PATTERN)) {
					// find the integer sent.
					index = Integer.valueOf(currentString.substring(
							currentString.lastIndexOf(FDPConstant.USSD_STRING_START_CHAR),
							currentString.lastIndexOf(FDPConstant.USSD_STRING_END_CHAR)));
					// if index not found, check in cache.
				}
			}
			FDPLogger.debug(getCircleLogger(dynamicMenuRequest), getClass(), "getNodeFromPreviousRequest()",
					LoggerUtil.getRequestAppender(dynamicMenuRequest) + "Index found as " + index);
			final DisplayObject lastDisplayObject = dynamicMenuRequest.getLastDisplayObject();
			if (index != null && lastDisplayObject != null && index > 0) {
				if (index <= lastDisplayObject.getNodesToDisplay().size()) {
					fdpNode = lastDisplayObject.getNodesToDisplay().get(index - 1);
					getCircleLogger(dynamicMenuRequest).debug(
							LoggerUtil.getRequestAppender(dynamicMenuRequest) + "Found node from previous request "
									+ fdpNode.getDisplayName());
				}
			}
		} else {
			//if fdpSpecialNode is found from the AuxRequestParam then it a PAM and RS deprovising Request
			Object fdpSpecialNode = dynamicMenuRequest.getAuxiliaryRequestParameter(AuxRequestParam.SPECIAL_MENU_NODE_SERVED);
			if (fdpSpecialNode != null && fdpSpecialNode instanceof FDPNode) {
				fdpNode = (FDPNode) fdpSpecialNode;
				RequestUtil.putAuxiliaryValueInRequest(dynamicMenuRequest, AuxRequestParam.SPECIAL_MENU_NODE_SERVED, null);
			} else {
				fdpNode = DynamicMenuUtil.getAndUpdateFDPNode(dynamicMenuRequest.getCircle(),
					dynamicMenuRequest.getLastServedString(), dynamicMenuRequest);
			}
			final List<String> inputValues = new ArrayList<String>();
			inputValues.add(currentString);
			RequestUtil.putAuxiliaryValueInRequest(dynamicMenuRequest, AuxRequestParam.CURRENT_SHARED_ACCOUNT_VALUE,
					inputValues);
		}
		return fdpNode;
	}

	/**
	 * Creates the request string.
	 * 
	 * @param requestString
	 *            the request string
	 * @param lastServedString
	 *            the last served string
	 * @return the string
	 */
	private String createRequestString(final String requestString, final String lastServedString) {
		return lastServedString.substring(0, lastServedString.indexOf(FDPConstant.USSD_STRING_END_CHAR))
				+ FDPConstant.USSD_STRING_START_CHAR + requestString + FDPConstant.USSD_STRING_END_CHAR;
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
	public FDPNode getSharedAccountNode(final String[] requestSplitString, final FDPSMPPRequest dynamicMenuRequest)
			throws ExecutionFailedException {
		final StringBuilder sharedAccountNodeString = new StringBuilder();
		final List<String> inputValues = new ArrayList<String>();
		FDPNode node = null;
		for (final String requestPartString : requestSplitString) {
			if (node == null) {
				if (!requestPartString.isEmpty()) {
					sharedAccountNodeString.append(FDPConstant.USSD_STRING_START_CHAR).append(requestPartString);
				}
				node = DynamicMenuUtil.getSharedAccountNodeOrPolicyNode(DynamicMenuUtil.getAndUpdateFDPNode(
						dynamicMenuRequest.getCircle(), sharedAccountNodeString.toString()
								+ FDPConstant.USSD_STRING_END_CHAR, dynamicMenuRequest), dynamicMenuRequest
						.isPolicyExecution());
			} else {
				inputValues.add(requestPartString);
			}
		}
		RequestUtil.putAuxiliaryValueInRequest(dynamicMenuRequest, AuxRequestParam.CURRENT_SHARED_ACCOUNT_VALUE,
				inputValues);
		return node;
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
	public FDPNode checkIfFlexiRechargeOrSharedAccount(final FDPSMPPRequest dynamicMenuRequest)
			throws ExecutionFailedException {
		// TODO: The flexi recharge is handled for ussd string only. To be
		// checked for SMS case as well.
		final Integer flexiRechargelength = Integer.parseInt((String) applicationConfigCache.getValue(new FDPAppBag(
				AppCacheSubStore.CONFIGURATION_MAP, FDPConstant.FLEXI_RECHARGE_CODE_LENGTH)));
		final Integer sharedAccountlength = Integer.parseInt((String) applicationConfigCache.getValue(new FDPAppBag(
				AppCacheSubStore.CONFIGURATION_MAP, FDPConstant.SHARED_ACCOUNT_CODE_LENGTH)));
		String requestString = dynamicMenuRequest.getRequestString();
		requestString = requestString.replaceAll(FDPConstant.USSD_STRING_END_CHAR, FDPConstant.EMPTY_STRING);
		final String[] requestSplitString = requestString.split(FDPConstant.USSD_STRING_START_CHAR_WITH_ESCAPE_CHAR);

		return getNodeForFlexiRechargeOrSharedAccount(dynamicMenuRequest, flexiRechargelength, sharedAccountlength,
				requestSplitString);
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
	private FDPNode getNodeForFlexiRechargeOrSharedAccount(final FDPSMPPRequest dynamicMenuRequest,
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
				flexiRechargeNodeString.append(FDPConstant.USSD_STRING_START_CHAR).append(requestPartString);
			}
		}
		flexiRechargeNodeString.append(FDPConstant.USSD_STRING_END_CHAR);
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
			fdpNode = getSharedAccountNode(requestSplitString, dynamicMenuRequest);
		}
		return fdpNode;
	}

}
