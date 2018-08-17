package com.ericsson.fdp.business.node.impl;

import java.util.List;
import java.util.Map;

import ch.qos.logback.classic.Logger;

import com.ericsson.fdp.business.enums.State;
import com.ericsson.fdp.business.node.AbstractMenuNode;
import com.ericsson.fdp.business.util.LoggerUtil;
import com.ericsson.fdp.common.enums.ChannelType;
import com.ericsson.fdp.common.enums.Visibility;
import com.ericsson.fdp.common.vo.FDPCircle;
import com.ericsson.fdp.core.display.DisplayObject;
import com.ericsson.fdp.core.exception.EvaluationFailedException;
import com.ericsson.fdp.core.exception.ExecutionFailedException;
import com.ericsson.fdp.core.logging.FDPLogger;
import com.ericsson.fdp.core.node.FDPNode;
import com.ericsson.fdp.core.request.FDPRequest;

public class RootNode extends AbstractMenuNode {

	/**
	 *
	 */
	private static final long serialVersionUID = 7731645115562418371L;

	/**
	 * This is the help text that is to be shown in case there is an error made
	 * by the user.
	 */
	private String helpText;

	/**
	 * @return the helpText
	 */
	public String getHelpText() {
		return helpText;
	}

	public RootNode() {

	}

	public RootNode(final String helpTextToSet, final String displayNameToSet, final String shortCodeToSet,
			final String fullyQualifiedPathToSet, final ChannelType channelToSet, final FDPCircle circleToSet,
			final Long priorityToSet,final State state) {
		this.helpText = helpTextToSet;
		this.setDisplayName(displayNameToSet);
		this.setShortCode(shortCodeToSet);
		this.setFullyQualifiedPath(fullyQualifiedPathToSet);
		this.setChannel(channelToSet);
		this.setCircle(circleToSet);
		this.setPriority(priorityToSet);
		this.setDmStatus(state);
	}

	public RootNode(final String helpTextToSet, final String displayNameToSet, final String shortCodeToSet,
			final String fullyQualifiedPathToSet, final ChannelType channelToSet, final FDPCircle circleToSet,
			final Long priorityToSet, final FDPNode parentToSet, final List<FDPNode> childrenToSet,
			final Map<String, Object> additionalInfo,final Visibility visibility,final State state) {
		this.helpText = helpTextToSet;
		super.setAbstractNode(displayNameToSet, shortCodeToSet, fullyQualifiedPathToSet, channelToSet, circleToSet,
				priorityToSet, parentToSet, childrenToSet, additionalInfo,visibility,state);
	}

	/**
	 * @param helpTextToSet
	 *            the helpText to set
	 */
	public void setHelpText(final String helpTextToSet) {
		this.helpText = helpTextToSet;
	}

	@Override
	public DisplayObject displayNode(final FDPRequest fdpRequest) throws ExecutionFailedException {
		final Logger circleLogger = LoggerUtil.getSummaryLoggerFromRequest(fdpRequest);
		// if (ChannelType.SMS.equals(fdpRequest.getChannel())) {
		// DisplayObjectImpl displayObject = new DisplayObjectImpl(this);
		// displayObject.setCurrDisplayText(helpText, DisplayArea.MIDDLE);
		// displayObject.addNodesToDisplay(this);
		// FDPLogger.debug(circleLogger, getClass(), "displayNode()",
		// LoggerUtil.getRequestAppender(fdpRequest)
		// + "Info node added, text :- " + helpText);
		// return displayObject;
		//
		// }
		try {
			final DisplayObject display = displayChildNodes(fdpRequest);
			FDPLogger.debug(circleLogger, getClass(), "displayNode()", LoggerUtil.getRequestAppender(fdpRequest)
					+ "Constraint node display object created as " + display);
			return display;
		} catch (final EvaluationFailedException e) {
			FDPLogger.error(circleLogger, getClass(), "displayNode()", LoggerUtil.getRequestAppender(fdpRequest)
					+ "The node could not be displayed", e);
			throw new ExecutionFailedException("The node could not be displayed", e);
		}
	}

	@Override
	public String generateNodeInfo(final FDPRequest fdpRequest) {
		return "RootNode";
	}

}
