package com.ericsson.fdp.business.charging.impl;

import java.io.Serializable;

import com.ericsson.fdp.business.enums.ConditionType;
import com.ericsson.fdp.dao.enums.CommandParameterDataType;

public class ExpressionCondition implements Serializable{

	private static final long serialVersionUID = 8011847262727247092L;
	private String leftOperand;
	private ConditionType conditionType;
	private Object rightHandOperandValue;
	private CommandParameterDataType commandParamDataType;

	public ExpressionCondition(String leftOperand, ConditionType conditionType, Object rightHandOperandValue, CommandParameterDataType commandParamDataType){
		this.leftOperand = leftOperand;
		this.conditionType = conditionType;
		this.rightHandOperandValue = rightHandOperandValue;
		this.commandParamDataType = commandParamDataType;
	}
	
	public String getLeftOperand() {
		return leftOperand;
	}

	public void setLeftOperand(String leftOperand) {
		this.leftOperand = leftOperand;
	}

	public ConditionType getConditionType() {
		return conditionType;
	}

	public void setConditionType(ConditionType conditionType) {
		this.conditionType = conditionType;
	}

	public Object getRightHandOperandValue() {
		return rightHandOperandValue;
	}

	public void setRightHandOperandValue(Object rightHandOperandValue) {
		this.rightHandOperandValue = rightHandOperandValue;
	}

	public CommandParameterDataType getCommandParamDataType() {
		return commandParamDataType;
	}

	public void setCommandParamDataType(CommandParameterDataType commandParamDataType) {
		this.commandParamDataType = commandParamDataType;
	}
	
}
