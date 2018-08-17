package com.ericsson.fdp.business.expression.impl;

import ch.qos.logback.classic.Logger;

import com.ericsson.fdp.business.exception.ExpressionFailedException;
import com.ericsson.fdp.business.expression.Expression;
import com.ericsson.fdp.business.util.LoggerUtil;
import com.ericsson.fdp.core.logging.FDPLogger;
import com.ericsson.fdp.core.request.FDPRequest;
import com.ericsson.fdp.dao.enums.ExpressionOperatorEnum;

/**
 * The class defines the expressions that are the nodes.
 * 
 * @author Ericsson
 * 
 */
public class ExpressionOperator implements Expression {

	/**
	 * 
	 */
	private static final long serialVersionUID = 4191778699853909905L;

	/** The left hand expression. */
	protected Expression leftHandExpression;

	/** The right hand expression. */
	protected Expression rightHandExpression;

	/** The operator for the node. */
	protected ExpressionOperatorEnum operator;

	/**
	 * The constructor for the expression operator.
	 * 
	 * @param leftHandExpressionToSet
	 *            The left hand expression.
	 * @param operatorToSet
	 *            The operator to set.
	 * @param rightHandExpressionToSet
	 *            The right hand expression.
	 */
	public ExpressionOperator(final Expression leftHandExpressionToSet, final ExpressionOperatorEnum operatorToSet,
			final Expression rightHandExpressionToSet) {
		this.leftHandExpression = leftHandExpressionToSet;
		this.operator = operatorToSet;
		this.rightHandExpression = rightHandExpressionToSet;
	}

	@Override
	public boolean evaluateExpression(final FDPRequest fdpRequest) throws ExpressionFailedException {
		Logger circleLogger = LoggerUtil.getSummaryLoggerFromRequest(fdpRequest);
		if (leftHandExpression == null || rightHandExpression == null) {
			FDPLogger.error(circleLogger, getClass(), "evaluateExpression()", LoggerUtil.getRequestAppender(fdpRequest)
					+ "The expression is not well formed. Found null elements. ");
			throw new ExpressionFailedException("The expression is not well formed. Found null elements. ");
		}
		FDPLogger.debug(circleLogger, getClass(), "evaluateExpression()", LoggerUtil.getRequestAppender(fdpRequest)
				+ "Executing expression operator");
		boolean result = false;
		switch (operator) {
		case AND:
			result = leftHandExpression.evaluateExpression(fdpRequest)
					&& rightHandExpression.evaluateExpression(fdpRequest);
			break;
		case OR:
			result = leftHandExpression.evaluateExpression(fdpRequest)
					|| rightHandExpression.evaluateExpression(fdpRequest);
			break;
		default:
			break;
		}
		FDPLogger.debug(circleLogger, getClass(), "evaluateExpression()", LoggerUtil.getRequestAppender(fdpRequest)
				+ "Result found as " + result);
		return result;
	}

	
	public Expression getLeftHandExpression() {
		return leftHandExpression;
	}

	public void setLeftHandExpression(Expression leftHandExpression) {
		this.leftHandExpression = leftHandExpression;
	}

	public Expression getRightHandExpression() {
		return rightHandExpression;
	}

	public void setRightHandExpression(Expression rightHandExpression) {
		this.rightHandExpression = rightHandExpression;
	}

	public ExpressionOperatorEnum getOperator() {
		return operator;
	}

	public void setOperator(ExpressionOperatorEnum operator) {
		this.operator = operator;
	}

	@Override
	public String toString() {
		return " (" + leftHandExpression.toString() + ") " + operator.toString() + " ("
				+ rightHandExpression.toString() + ") ";
	}

}
