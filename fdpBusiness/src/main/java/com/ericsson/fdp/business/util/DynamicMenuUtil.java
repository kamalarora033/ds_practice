package com.ericsson.fdp.business.util;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import ch.qos.logback.classic.Logger;

import com.ericsson.fdp.FDPCacheable;
import com.ericsson.fdp.business.dto.Me2uProductDTO;
import com.ericsson.fdp.business.enums.AuxRequestParam;
import com.ericsson.fdp.business.enums.ConfigurationKey;
import com.ericsson.fdp.business.enums.RequestMetaValuesKey;
import com.ericsson.fdp.business.enums.State;
import com.ericsson.fdp.business.node.FDPCustomNode;
import com.ericsson.fdp.business.node.impl.ProductNode;
import com.ericsson.fdp.business.product.Product;
import com.ericsson.fdp.business.product.impl.BaseProduct;
import com.ericsson.fdp.business.request.requestString.impl.FDPSMSCRequestStringImpl;
import com.ericsson.fdp.business.request.requestString.impl.FDPUSSDRequestStringImpl;
import com.ericsson.fdp.common.constants.FDPConstant;
import com.ericsson.fdp.common.enums.ChannelType;
import com.ericsson.fdp.common.enums.ModuleType;
import com.ericsson.fdp.common.enums.ProductType;
import com.ericsson.fdp.common.enums.Visibility;
import com.ericsson.fdp.common.vo.FDPCircle;
import com.ericsson.fdp.core.exception.ExecutionFailedException;
import com.ericsson.fdp.core.logging.FDPLogger;
import com.ericsson.fdp.core.node.FDPNode;
import com.ericsson.fdp.core.request.FDPRequest;
import com.ericsson.fdp.core.request.FDPResponse;
import com.ericsson.fdp.core.request.FDPSMPPRequest;
import com.ericsson.fdp.core.request.impl.FDPRequestImpl;
import com.ericsson.fdp.core.request.impl.FDPSMPPRequestImpl;
import com.ericsson.fdp.core.request.requestString.FDPRequestString;
import com.ericsson.fdp.core.serviceprovisioning.bean.FDPMetaBag;
import com.ericsson.fdp.core.serviceprovisioning.bean.FDPRequestBag;
import com.ericsson.fdp.core.utils.ApplicationConfigUtil;
import com.ericsson.fdp.dao.enums.FDPServiceProvSubType;
import com.ericsson.fdp.dao.enums.StatusCodeEnum;

/**
 * This class is used to provide utility methods for the dynamic menu.
 * 
 * @author Ericsson
 * 
 */
public class DynamicMenuUtil {

	/**
	 * Instantiates a new dynamic menu util.
	 */
	private DynamicMenuUtil() {

	}

	/**
	 * This method is used to update the last served string in the request.
	 * 
	 * @param dynamicMenuRequest
	 *            the request object.
	 * @param fdpNode
	 *            the node last served.
	 */
	private static void updateLastServedStringInRequest(final FDPSMPPRequest dynamicMenuRequest, final FDPNode fdpNode) {
		if (dynamicMenuRequest instanceof FDPRequestImpl && fdpNode != null) {
			final FDPRequestImpl fdpRequestImpl = (FDPRequestImpl) dynamicMenuRequest;
			if (ChannelType.USSD.equals(dynamicMenuRequest.getChannel())) {
				FDPRequestString requestString = dynamicMenuRequest.getRequestStringInterface();
				if (fdpNode != null) {
					requestString = new FDPUSSDRequestStringImpl(fdpNode.getFullyQualifiedPath());
				}
				fdpRequestImpl.setLastServedString(requestString);

				// Add node to traversed nodes.
				RequestUtil.updateNodeInRequest(dynamicMenuRequest, fdpNode);

				FDPLogger.debug(LoggerUtil.getSummaryLoggerFromRequest(dynamicMenuRequest), DynamicMenuUtil.class,
						"updateLastServedStringInRequest()", LoggerUtil.getRequestAppender(dynamicMenuRequest)
						+ "Setting last served string as :- " + requestString);
			} else if (ChannelType.SMS.equals(dynamicMenuRequest.getChannel())) {
				FDPRequestString requestString = dynamicMenuRequest.getRequestStringInterface();
				if (fdpNode != null) {
					requestString = new FDPSMSCRequestStringImpl(null, fdpNode.getFullyQualifiedPath());
				}
				fdpRequestImpl.setLastServedString(requestString);
			}

			if (fdpNode instanceof FDPCustomNode) {
				RequestUtil.putAuxiliaryValueInRequest(dynamicMenuRequest, AuxRequestParam.SPECIAL_MENU_NODE_SERVED, fdpNode);
			}
		}

	}

	/**
	 * This method is used to get the node from the cache and update it in
	 * request..
	 * 
	 * @param fdpCircle
	 *            the fdp circle
	 * @param requestString
	 *            the request string
	 * @param dynamicMenuRequest
	 *            The request object.
	 * @return The node object.
	 * @throws ExecutionFailedException
	 *             Exception in execution.
	 */
	public static FDPNode getAndUpdateFDPNode(final FDPCircle fdpCircle, final String requestString,
			final FDPSMPPRequest dynamicMenuRequest) throws ExecutionFailedException {
		final Logger circleLogger = LoggerUtil.getSummaryLoggerFromRequest(dynamicMenuRequest);
		FDPLogger.debug(circleLogger, DynamicMenuUtil.class, "getAndUpdateFDPNode()",
				LoggerUtil.getRequestAppender(dynamicMenuRequest) + "Getting node for request string " + requestString.trim());
		FDPNode fdpNode = dynamicMenuRequest.getNode(requestString);


		if (fdpNode == null) {
			final FDPCacheable fdpCacheable = ApplicationConfigUtil.getMetaDataCache().getValue(
					new FDPMetaBag(fdpCircle, ModuleType.DM, requestString));
			fdpNode = RequestUtil.checkInstanceForNode(fdpCacheable);
			RequestUtil.updateNodeInRequest(dynamicMenuRequest, fdpNode);
			FDPLogger.debug(circleLogger, DynamicMenuUtil.class, "getAndUpdateFDPNode()",
					LoggerUtil.getRequestAppender(dynamicMenuRequest) + "Found node as :- "
							+ (fdpNode == null ? "null" : fdpNode.getDisplayName()));
		}
		//(AuxRequestParam.deprovisionP, "Y")
		Map<String, Long> productMappingMap = new HashMap<String, Long>();
		Object value = ((FDPSMPPRequestImpl)dynamicMenuRequest).getAuxiliaryRequestParameter(AuxRequestParam.deprovisionP);
		Object object = ((FDPSMPPRequestImpl)dynamicMenuRequest).getAuxiliaryRequestParameter(AuxRequestParam.productMap);
		Object isData2Share = ((FDPSMPPRequestImpl)dynamicMenuRequest).getAuxiliaryRequestParameter(AuxRequestParam.isData2Share);
		if(object != null) {
			productMappingMap =  (Map<String, Long>) object;
		}
		if(value != null) {
			ProductNode productNode = new ProductNode(fdpNode.getDisplayName(), null,
					fdpNode.getFullyQualifiedPath(), null, null,
					fdpNode.getPriority(), fdpNode.getParent(), fdpNode.getChildren(),
					productMappingMap.get(((FDPSMPPRequestImpl)dynamicMenuRequest).getRequestString()), FDPConstant.PRODUCT, FDPServiceProvSubType.RS_DEPROVISION_PRODUCT,
					null, null,null,null);
			fdpNode = (ProductNode)productNode;
		}
		else if(isData2Share != null){
			Me2uProductDTO me2uProductDTO = (Me2uProductDTO) ((FDPSMPPRequestImpl)dynamicMenuRequest).getAuxiliaryRequestParameter(AuxRequestParam.SELECTED_Me2UDATA2SHARE_PRODUCT);
			if(null != me2uProductDTO) {
			Product product = me2uProductDTO.getProduct();
			BaseProduct baseProduct = (BaseProduct) product;
			String charges = "0";
			if(null != baseProduct.getCharges()) {
				charges = baseProduct.getCharges();
			}
			if(Long.parseLong(charges) <= Long.parseLong(me2uProductDTO.getAccountValue()) &&
					(dynamicMenuRequest.getRequestString().equals("1") || dynamicMenuRequest.getRequestString().contains(FDPConstant.DATA2Share))) {
				ProductNode productNode = new ProductNode(me2uProductDTO.getProduct().getProductName(), null,
						fdpNode.getFullyQualifiedPath(), null, null,
						fdpNode.getPriority(), fdpNode.getParent(), fdpNode.getChildren(),
						me2uProductDTO.getProduct().getProductId(), FDPConstant.PRODUCT, FDPServiceProvSubType.Me2U,
						null, null,null,null);
				fdpNode = (ProductNode)productNode;
			}
			else {
				 ((FDPSMPPRequestImpl)dynamicMenuRequest).putAuxiliaryRequestParameter(AuxRequestParam.isData2Share, null);
			}
			}
			
			
		}



		return fdpNode;
	}

	/**
	 * This method checks if the node is shared account node. Returns the node
	 * if shared account, null otherwise.
	 * 
	 * @param fdpNode
	 *            the node to be checked.
	 * @param isPolicyExecution
	 *            the is policy execution
	 * @return shared account node.
	 */
	public static FDPNode getSharedAccountNodeOrPolicyNode(final FDPNode fdpNode, final boolean isPolicyExecution) {
		FDPNode productSharedNode = null;
		if (fdpNode instanceof ProductNode) {
			final ProductNode productNode = (ProductNode) fdpNode;
			if (productNode.isSharedAccountType() || isPolicyExecution) {
				productSharedNode = productNode;
			}
		}
		return productSharedNode;
	}

	/**
	 * This method is used to get the flexi recharge node.
	 * 
	 * @param fdpNode
	 *            The node to be checked for flexi recharge.
	 * @param requestString
	 *            the request string
	 * @param fdpCircle
	 *            the fdp circle
	 * @param circleLogger
	 *            the circle logger.
	 * @return flexi recharge node if found, else null;
	 * @throws ExecutionFailedException
	 *             Exception in execution.
	 */
	public static FDPNode getFlexiRechargeNode(final Logger circleLogger, final FDPNode fdpNode,
			final String requestString, final FDPCircle fdpCircle) throws ExecutionFailedException {
		FDPNode flexiNode = null;
		if (fdpNode instanceof ProductNode) {
			final ProductNode productNode = (ProductNode) fdpNode;
			final String entityId = productNode.getEntityIdForCache(RequestMetaValuesKey.PRODUCT);
			FDPLogger.debug(circleLogger, DynamicMenuUtil.class, "getNodeForFlexiRechargeOrSharedAccount()",
					LoggerUtil.getRequestAppender(requestString) + "Finding product id " + entityId
					+ " for flexi recharge");
			final FDPCacheable productCached = ApplicationConfigUtil.getMetaDataCache().getValue(
					new FDPMetaBag(fdpCircle, ModuleType.PRODUCT, entityId));
			if (productCached instanceof Product) {
				final Product product = (Product) productCached;
				if (ProductType.FLEXI_RECHARGE.equals(product.getProductType())) {
					flexiNode = fdpNode;
				}
			}
		}
		return flexiNode;
	}

	/**
	 * This method is used to get the help text response.
	 * 
	 * @param dynamicMenuRequest
	 *            the request object.
	 * @return the response using help text.
	 * @throws ExecutionFailedException
	 *             Exception in execution.
	 */
	public static FDPResponse getHelpTextResponse(final FDPSMPPRequest dynamicMenuRequest)
			throws ExecutionFailedException {
		final Logger circleLogger = LoggerUtil.getSummaryLoggerFromRequest(dynamicMenuRequest);
		FDPLogger.debug(circleLogger, DynamicMenuUtil.class, "getResponse()",
				LoggerUtil.getRequestAppender(dynamicMenuRequest) + "No defined node found.");
		final FDPResponse fdpResponse = RequestUtil.getHelpText(dynamicMenuRequest.getRequestStringInterface(),
				dynamicMenuRequest.getCircle(), dynamicMenuRequest.getChannel());
		FDPLogger.debug(
				circleLogger,
				DynamicMenuUtil.class,
				"getHelpText()",
				LoggerUtil.getRequestAppender(dynamicMenuRequest) + "Help text found "
						+ fdpResponse.getResponseString());
		return fdpResponse;
	}

	/**
	 * This method is used to update values in request.
	 * 
	 * @param dynamicMenuRequest
	 *            the request object.
	 * @param fdpNode
	 *            the node.
	 * @throws ExecutionFailedException
	 *             Exception in execution.
	 */
	public static void updateValueInCache(final FDPSMPPRequest dynamicMenuRequest, final FDPNode fdpNode)
			throws ExecutionFailedException {
		if (dynamicMenuRequest.getSessionId() != null) {
			DynamicMenuUtil.updateLastServedStringInRequest(dynamicMenuRequest, fdpNode);
			if (ChannelType.USSD.equals(dynamicMenuRequest.getChannel())) {
				ApplicationConfigUtil.getRequestCacheForUSSD().putValue(
						new FDPRequestBag(dynamicMenuRequest.getIncomingSubscriberNumber().toString()),
						dynamicMenuRequest);
			} else if (ChannelType.SMS.equals(dynamicMenuRequest.getChannel())) {
				ApplicationConfigUtil.getRequestCacheForSMS().putValue(
						new FDPRequestBag(dynamicMenuRequest.getSessionId()), dynamicMenuRequest);
			}
		}
	}

	/**
	 * This method is used to get the exit menu response.
	 * 
	 * @param circle
	 *            the circle for which the response is required.
	 * @return the exit menu response.
	 */
	public static String getExitMenuResponse(final FDPCircle circle) {
		final Map<String, String> conf = circle.getConfigurationKeyValueMap();
		return conf.get(ConfigurationKey.EXIT_RESPONSE.getAttributeName());
	}

	public static void updateValueInCache(FDPRequest fdpRequest) throws ExecutionFailedException {
		if (fdpRequest instanceof FDPSMPPRequest) {
			updateValueInCache((FDPSMPPRequest) fdpRequest, null);
		}
	}

	/**
	 * This method checks for requested Msisdn is whitelisted.
	 * 
	 * @param dynamicMenuRequest
	 * @return
	 * @throws ExecutionFailedException
	 */
	public static boolean isWhiteListedForActiveForTestDMNode(final FDPRequest dynamicMenuRequest, final FDPNode fdpNode) throws ExecutionFailedException {
		boolean isAllowed=true;
		final Logger circleLogger = LoggerUtil.getSummaryLoggerFromRequest(dynamicMenuRequest);
		FDPLogger.debug(circleLogger, DynamicMenuUtil.class, "isWhiteListedForActiveForTestDMNode()",
				LoggerUtil.getRequestAppender(dynamicMenuRequest) + "DM node Status :"+fdpNode.getDmStatus());
		if (null != fdpNode && State.ACTIVE_FOR_TEST.equals(fdpNode.getDmStatus())) {
			isAllowed = false;
			final Map<String, String> configurationMap = dynamicMenuRequest.getCircle().getConfigurationKeyValueMap();
			final String whiteListedUsers = configurationMap.get(ConfigurationKey.WHITELISTED_NUMBER_TESTING
					.getAttributeName());
			final List<Long> whiteListedUsersList = new ArrayList<Long>();
			try {
				if (!(whiteListedUsers == null || whiteListedUsers.isEmpty())) {
					for (final String whiteListesUser : Arrays.asList(whiteListedUsers.split(FDPConstant.COMMA))) {
						whiteListedUsersList.add(Long.valueOf(whiteListesUser));
					}
				}
				if (whiteListedUsersList.contains(dynamicMenuRequest.getSubscriberNumber())) {
					FDPLogger.debug(circleLogger, DynamicMenuUtil.class, "isWhiteListedForActiveForTestDMNode()",
							LoggerUtil.getRequestAppender(dynamicMenuRequest) + "User is WhiteListed with DM node ACTIVE_FOR_TEST");
					isAllowed = true;
				}
			} catch (final NumberFormatException e) {
				FDPLogger.error(circleLogger, DynamicMenuUtil.class, "isWhiteListedForActiveForTestDMNode()",
						LoggerUtil.getRequestAppender(dynamicMenuRequest)
						+ "Error !! While loading white listed user in cache.");
				throw new ExecutionFailedException("Error !! While loading white listed user in cache.", e);
			}
		}
		FDPLogger.debug(circleLogger, DynamicMenuUtil.class, "isWhiteListedForActiveForTestDMNode()",
				LoggerUtil.getRequestAppender(dynamicMenuRequest) + "Returning isWhiteListedForActiveForTestDMNode Status: "+isAllowed);
		return isAllowed;
	}

	/**
	 * This method return the DM State.
	 * 
	 * @param status
	 * @return
	 */
	public static State getDMState(final Integer status) {
		State state = State.ACTIVE_FOR_MARKET;
		if(null != status) {
			StatusCodeEnum statusCodeNum = StatusCodeEnum.getStatusEnum(status);
			if(null != statusCodeNum) {
				if(StatusCodeEnum.ACTIVE_FOR_TEST.equals(statusCodeNum)) {
					state = State.ACTIVE_FOR_TEST;
				}
			}
		}
		return state;
	}

	
}
