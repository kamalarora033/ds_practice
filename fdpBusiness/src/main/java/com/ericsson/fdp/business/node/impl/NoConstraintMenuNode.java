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

/**
 * This class defines the no constraint menu node.
 *
 * @author Ericsson
 *
 */
public class NoConstraintMenuNode extends AbstractMenuNode {

	/** The Constant serialVersionUID. */
	private static final long serialVersionUID = 778209383557590058L;

	/**
	 * Instantiates a new no constraint menu node.
	 */
	public NoConstraintMenuNode() {
	}

	/**
	 * Instantiates a new no constraint menu node.
	 *
	 * @param displayNameToSet
	 *            the display name to set
	 * @param shortCodeToSet
	 *            the short code to set
	 * @param fullyQualifiedPathToSet
	 *            the fully qualified path to set
	 * @param channelToSet
	 *            the channel to set
	 * @param circleToSet
	 *            the circle to set
	 * @param priorityToSet
	 *            the priority to set
	 * @param parentToSet
	 *            the parent to set
	 * @param childrenToSet
	 *            the children to set
	 */
	public NoConstraintMenuNode(final String displayNameToSet, final String shortCodeToSet,
			final String fullyQualifiedPathToSet, final ChannelType channelToSet, final FDPCircle circleToSet,
			final Long priorityToSet, final FDPNode parentToSet, final List<FDPNode> childrenToSet,
			final Map<String, Object> additionalInfo,final Visibility visibility,final State state) {
		super.setAbstractNode(displayNameToSet, shortCodeToSet, fullyQualifiedPathToSet, channelToSet, circleToSet,
				priorityToSet, parentToSet, childrenToSet, additionalInfo,visibility,state);
	}

	@Override
	public DisplayObject displayNode(final FDPRequest fdpRequest) throws ExecutionFailedException {
		final Logger circleLogger = LoggerUtil.getSummaryLoggerFromRequest(fdpRequest);
		try {
			final DisplayObject display = displayChildNodes(fdpRequest);
			FDPLogger.debug(circleLogger, getClass(), "displayNode()", LoggerUtil.getRequestAppender(fdpRequest)
					+ "No Constraint node display object created as " + display);
			return display;
		} catch (final EvaluationFailedException e) {
			FDPLogger.error(circleLogger, getClass(), "displayNode()", LoggerUtil.getRequestAppender(fdpRequest)
					+ "The node could not be displayed", e);
			throw new ExecutionFailedException("The node could not be displayed", e);
		}
	}

	@Override
	public String generateNodeInfo(final FDPRequest fdpRequest) {
		return "NoConstraintMenu";
	}
}
