package com.ericsson.fdp.business.util;

import java.util.List;
import java.util.Map;

import com.ericsson.fdp.business.display.DisplayBucket;
import com.ericsson.fdp.business.display.impl.PaginatedDisplayObject;
import com.ericsson.fdp.business.enums.ConfigurationKey;
import com.ericsson.fdp.business.enums.DisplayArea;
import com.ericsson.fdp.business.enums.DynamicMenuPaginationKey;
import com.ericsson.fdp.business.node.AbstractNode;
import com.ericsson.fdp.common.constants.FDPConstant;
import com.ericsson.fdp.common.enums.ChannelType;
import com.ericsson.fdp.common.enums.ModuleType;
import com.ericsson.fdp.common.vo.FDPCircle;
import com.ericsson.fdp.core.display.DisplayObject;
import com.ericsson.fdp.core.exception.ExecutionFailedException;
import com.ericsson.fdp.core.logging.FDPLogger;
import com.ericsson.fdp.core.node.FDPNode;
import com.ericsson.fdp.core.request.FDPSMPPRequest;
import com.ericsson.fdp.core.serviceprovisioning.bean.FDPMetaBag;
import com.ericsson.fdp.core.utils.ApplicationConfigUtil;

/**
 * This class is a utility class that works on pagination.
 * 
 * @author Ericsson
 */
public class PaginationUtil {

	/**
	 * Instantiates a new pagination util.
	 */
	private PaginationUtil() {

	}

	/**
	 * This method is used to check the request for pagination.
	 * 
	 * @param requestString
	 *            the request string.
	 * @param channelType
	 *            the channel type used.
	 * @param fdpCircle
	 *            the circle used.
	 * @return the pagination request.
	 * @throws ExecutionFailedException
	 *             Exception in execution.
	 */
	public static DynamicMenuPaginationKey getPaginationOptionsForRequest(final String requestString,
			final ChannelType channelType, final FDPCircle fdpCircle) throws ExecutionFailedException {
		DynamicMenuPaginationKey dynamicMenuPaginationKeyUsed = null;
		for (final DynamicMenuPaginationKey dynamicMenuPaginationKey : DynamicMenuPaginationKey.values()) {
			if (requestString.equals(getCodeForPaginationRequest(channelType, dynamicMenuPaginationKey, fdpCircle))) {
				dynamicMenuPaginationKeyUsed = dynamicMenuPaginationKey;
				break;
			}
		}
		return dynamicMenuPaginationKeyUsed;
	}

	/**
	 * This method is used to check the code for pagination request.
	 * 
	 * @param channelType
	 *            the channel type to be used.
	 * @param dynamicMenuPaginationKey
	 *            the pagination type.
	 * @param fdpCircle
	 *            the circle to be used.
	 * @return the code for the defined pagination.
	 * @throws ExecutionFailedException
	 *             Exception in execution.
	 */
	public static String getCodeForPaginationRequest(final ChannelType channelType,
			final DynamicMenuPaginationKey dynamicMenuPaginationKey, final FDPCircle fdpCircle)
			throws ExecutionFailedException {
		String key = null;
		switch (channelType) {
		case USSD:
			key = fdpCircle.getConfigurationKeyValueMap().get(
							dynamicMenuPaginationKey.getConfigurationKeyForUSSD().getAttributeName());
			break;
		case SMS:
			key = fdpCircle.getConfigurationKeyValueMap().get(
							dynamicMenuPaginationKey.getConfigurationKeyForSMS().getAttributeName());
			break;
		default:
			throw new ExecutionFailedException("Channel not recognized" + channelType.name());
		}
		return key;

	}

	/**
	 * This method is used to get the main menu for the request.
	 * 
	 * @param dynamicMenuRequest
	 *            the dynamic menu request.
	 * @return the main menu.
	 * @throws ExecutionFailedException
	 *             Exception in execution.
	 */
	public static FDPNode getMainMenuNode(final FDPSMPPRequest dynamicMenuRequest) throws ExecutionFailedException {
		final DisplayObject lastDisplayObject = dynamicMenuRequest.getLastDisplayObject();
		FDPNode parentNode = null;
		if (lastDisplayObject != null) {
			parentNode = lastDisplayObject.getCurrentNode();
			while (parentNode.getParent() != null) {
				parentNode = parentNode.getParent();
			}
		}
		if (parentNode != null) {
			parentNode = getNode(parentNode, dynamicMenuRequest.getCircle());
		}
		return parentNode;
	}

	/**
	 * This method is used to get the root node from cache.
	 * 
	 * @param parentNode
	 *            the parent node.
	 * @param fdpCircle
	 *            the circle.
	 * @return the root node.
	 * @throws ExecutionFailedException
	 *             Exception in execution.
	 */
	private static FDPNode getNode(final FDPNode parentNode, final FDPCircle fdpCircle) throws ExecutionFailedException {
		// This is required because while initializing the cache, the root
		// node children are not saved in the node, which is saved in the cache.
		// I.e., the root node is saved in the children, which at the time does
		// not contain any children. Hence the one saved in the node, does not
		// contain any children. This will not be required if the fix in cache
		// is done.
		return (FDPNode) ApplicationConfigUtil.getMetaDataCache().getValue(
				new FDPMetaBag(fdpCircle, ModuleType.DM, parentNode.getFullyQualifiedPath()));
	}

	/**
	 * This method is used to get the previous menu for this node.
	 * 
	 * @param dynamicMenuRequest
	 *            the request object.
	 * @return the previous node.
	 * @throws ExecutionFailedException
	 *             Exception in execution.
	 */
	public static FDPNode getPreviousMenuNode(final FDPSMPPRequest dynamicMenuRequest) throws ExecutionFailedException {
		final DisplayObject lastDisplayObject = dynamicMenuRequest.getLastDisplayObject();
		FDPNode parentNode = null;
		if (lastDisplayObject != null) {
			if (lastDisplayObject.getCurrentNode() != null) {
				parentNode = lastDisplayObject.getCurrentNode().getParent();
			}
		}
		if (parentNode != null) {
			parentNode = getNode(parentNode, dynamicMenuRequest.getCircle());
		}
		return parentNode;
	}

	/**
	 * This method is used to get the seperator.
	 * 
	 * @return the seperator for this circle.
	 */
	public static String getSeperator(final FDPCircle fdpCircle) {
		final Map<String, String> circleConfig = fdpCircle.getConfigurationKeyValueMap();
		String sep = circleConfig.get(ConfigurationKey.FOOTER_OPTIONS_SEPERATOR.name());
		if (sep.equals("\\n")) {
			sep = FDPConstant.NEWLINE;
		}
		return sep;
	}

	/**
	 * This will check if the node contains the pagination request.
	 * 
	 * @param currentNode
	 *            the current node.
	 * @param dynamicMenuPaginationKey
	 *            the pagination key.
	 * @return true if pagination request, false otherwise.
	 */
	public static boolean checkIfPaginationOnRequest(final FDPNode currentNode,
			final DynamicMenuPaginationKey dynamicMenuPaginationKey) {
		boolean isPagination = false;
		if (currentNode instanceof AbstractNode) {
			final AbstractNode abstractMenuNode = (AbstractNode) currentNode;
			if (dynamicMenuPaginationKey.getAdditionalInfoKey() == null
					|| abstractMenuNode.getPaginationOptions().contains(dynamicMenuPaginationKey)) {
				isPagination = true;
			}
		}
		return isPagination;
	}

	/**
	 * This method is used to handle pagination cases.
	 * 
	 * @param isMoreCase
	 *            True, if it is a case for more, false if for previous.
	 * @param dynamicMenuRequest
	 *            The request.
	 * @return the display string if pagination is valid.
	 */
	public static String handlePagination(final boolean isMoreCase, final FDPSMPPRequest dynamicMenuRequest) {
		String displayString = null;
		final DisplayObject displayObject = dynamicMenuRequest.getLastDisplayObject();
		if (displayObject instanceof PaginatedDisplayObject) {
			final PaginatedDisplayObject paginatedDisplayObject = (PaginatedDisplayObject) displayObject;
			final int currentIndex = paginatedDisplayObject.getCurrentDisplayBucketIndex();
			final List<DisplayBucket> displayBuckets = paginatedDisplayObject.getDisplayBuckets();
			if (isMoreCase) {
				if (currentIndex + 1 < displayBuckets.size()) {
					final DisplayBucket currentBucket = displayBuckets.get(currentIndex + 1);
					paginatedDisplayObject.setCurrDisplayText(currentBucket.getDisplayString(), DisplayArea.COMPLETE);
					paginatedDisplayObject.setCurrentDisplayBucketIndex(currentIndex + 1);
					displayString = currentBucket.getDisplayString();
				}
			} else {
				if (currentIndex > 0) {
					final DisplayBucket currentBucket = displayBuckets.get(currentIndex - 1);
					paginatedDisplayObject.setCurrDisplayText(currentBucket.getDisplayString(), DisplayArea.COMPLETE);
					paginatedDisplayObject.setCurrentDisplayBucketIndex(currentIndex - 1);
					displayString = currentBucket.getDisplayString();
				}
			}
		}
		FDPLogger.debug(LoggerUtil.getSummaryLoggerFromRequest(dynamicMenuRequest), PaginationUtil.class,
				"handlePagination()", "The display has been set as :- " + displayString);
		return displayString;
	}

}
