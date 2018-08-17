package com.ericsson.fdp.business.node.impl;

import java.util.List;
import java.util.Map;

import ch.qos.logback.classic.Logger;

import com.ericsson.fdp.business.enums.State;
import com.ericsson.fdp.business.exception.ExpressionFailedException;
import com.ericsson.fdp.business.expression.Expression;
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
 * This class defines the nodes of type constraint.
 *
 * @author Ericsson
 *
 */
public class ConstraintNode extends AbstractMenuNode {

	/** The Constant serialVersionUID. */
	private static final long serialVersionUID = -4361768979964002561L;

	/**
	 * The expression or constraint for the node.
	 */
	private Expression fdpExpression;

	/**
	 * Instantiates a new constraint node.
	 */
	public ConstraintNode() {
	}

	/**
	 * Instantiates a new constraint node.
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
	 * @param fdpExpressionToSet
	 *            the fdp expression to set
	 */
	public ConstraintNode(final String displayNameToSet, final String shortCodeToSet,
			final String fullyQualifiedPathToSet, final ChannelType channelToSet, final FDPCircle circleToSet,
			final Long priorityToSet, final FDPNode parentToSet, final List<FDPNode> childrenToSet,
			final Expression fdpExpressionToSet, final Map<String, Object> additionalInfo,final Visibility visibility,final State state) {
		super.setAbstractNode(displayNameToSet, shortCodeToSet, fullyQualifiedPathToSet, channelToSet, circleToSet,
				priorityToSet, parentToSet, childrenToSet, additionalInfo,visibility,state);
		this.fdpExpression = fdpExpressionToSet;
	}

	@Override
	public DisplayObject displayNode(final FDPRequest fdpRequest) throws ExecutionFailedException {
		final Logger circleLogger = LoggerUtil.getSummaryLoggerFromRequest(fdpRequest);
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
	public boolean evaluateNode(final FDPRequest fdpRequest) throws EvaluationFailedException {
		final Logger circleLogger = LoggerUtil.getSummaryLoggerFromRequest(fdpRequest);
		try {
			final boolean expressionValue = fdpExpression == null ? true : fdpExpression.evaluateExpression(fdpRequest);
			FDPLogger.debug(circleLogger, getClass(), "evaluateNode()", LoggerUtil.getRequestAppender(fdpRequest)
					+ "Constraint node evaluation value " + expressionValue);
			return expressionValue;
		} catch (final ExpressionFailedException e) {
			FDPLogger.error(circleLogger, getClass(), "evaluateNode()", LoggerUtil.getRequestAppender(fdpRequest)
					+ "The expression could not be evaluated", e);
			throw new EvaluationFailedException("The expression could not be evaluated", e);
		}
	}

	@Override
	public String generateNodeInfo(final FDPRequest fdpRequest) {
		return "ConstraintMenu";
	}
}
