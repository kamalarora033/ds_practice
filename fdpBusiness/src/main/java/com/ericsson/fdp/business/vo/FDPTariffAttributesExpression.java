package com.ericsson.fdp.business.vo;

import com.ericsson.fdp.FDPCacheable;
import com.ericsson.fdp.business.expression.Expression;

public class FDPTariffAttributesExpression implements FDPCacheable{

	/**
	 * 
	 */
	private static final long serialVersionUID = 8407919495262929443L;

	private Expression expression;
	
	public FDPTariffAttributesExpression(final Expression expression) {
		this.expression = expression;
	}

	
	/**
	 * @return the expression
	 */
	public Expression getExpression() {
		return expression;
	}

	@Override
	public String toString() {
		return "FDPTariffAttributesExpression [expression=" + expression + "]";
	}
	
}
