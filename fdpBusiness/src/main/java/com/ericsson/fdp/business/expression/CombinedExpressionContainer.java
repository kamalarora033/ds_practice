package com.ericsson.fdp.business.expression;

import com.ericsson.fdp.dao.enums.ExpressionOperatorEnum;

/**
 * This class contains all the expressions in case of combined expression 
 * @author euyybcr
 *
 */
public class CombinedExpressionContainer {
	
	/** The Expression */
	private Expression expression;
	/** The ExpressionOperatorEnum */
	private ExpressionOperatorEnum expressionOperator;
	/** The orderId */
	private Integer orderId;	
	
	
	/**
	 * 
	 * @param expression
	 * @param expressionOperator
	 */
	public CombinedExpressionContainer(Expression expression,
			ExpressionOperatorEnum expressionOperator) {
		super();
		this.expression = expression;
		this.expressionOperator = expressionOperator;
	}


	/**
	 * @return the expression
	 */
	public Expression getExpression() {
		return expression;
	}


	/**
	 * @return the expressionOperator
	 */
	public ExpressionOperatorEnum getExpressionOperator() {
		return expressionOperator;
	}


	/**
	 * @return the orderId
	 */
	public Integer getOrderId() {
		return orderId;
	}

	
		

}
