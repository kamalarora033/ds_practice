package com.ericsson.fdp.business.vo;

import java.util.List;

import com.ericsson.fdp.business.enums.Operator;
import com.ericsson.fdp.business.expression.LeftOperand;
import com.ericsson.fdp.dao.enums.FDPCommandParameterConditionEnum;

public class ConditionSubStepDTO {

	/** The operator with which this sub step is linked to the one below it. */
	private Operator operator;

	/**
	 * The condition type with which this sub step is linked to the one below
	 * it.
	 */
	private FDPCommandParameterConditionEnum conditionType;

	/** The value of the left operand. */
	private LeftOperand leftOperand;

	/** The minimum value of the right operand. */
	private Object minValue;

	/** The maximum value of the right operand. */
	private Object maxValue;

	/** The possible values of the right operand. */
	private List<Object> possibleValues;

	/** The value of the right operand. */
	private Object value;

	/**
	 * The constructor for the the sub step dto.
	 * 
	 * @param conditionTypeToSet
	 *            The condition type to set.
	 * @param leftOperandToSet
	 *            The left hand operand to set.
	 * @param operatorToSet
	 *            The operator to set.
	 */
	public ConditionSubStepDTO(final FDPCommandParameterConditionEnum conditionTypeToSet, final LeftOperand leftOperandToSet,
			final Operator operatorToSet) {
		this.conditionType = conditionTypeToSet;
		this.leftOperand = leftOperandToSet;
		this.operator = operatorToSet;
	}

	/**
	 * @return the operator
	 */
	public Operator getOperator() {
		return operator;
	}

	/**
	 * @return the conditionType
	 */
	public FDPCommandParameterConditionEnum getConditionType() {
		return conditionType;
	}

	/**
	 * @return the leftOperand
	 */
	public LeftOperand getLeftOperand() {
		return leftOperand;
	}

	/**
	 * @return the minValue
	 */
	public Object getMinValue() {
		return minValue;
	}

	/**
	 * @param minValueToSet the minValue to set
	 */
	public void setMinValue(final Object minValueToSet) {
		this.minValue = minValueToSet;
	}

	/**
	 * @return the maxValue
	 */
	public Object getMaxValue() {
		return maxValue;
	}

	/**
	 * @param maxValueToSet the maxValue to set
	 */
	public void setMaxValue(final Object maxValueToSet) {
		this.maxValue = maxValueToSet;
	}

	/**
	 * @return the possibleValues
	 */
	public List<Object> getPossibleValues() {
		return possibleValues;
	}

	/**
	 * @param possibleValuesToSet the possibleValues to set
	 */
	public void setPossibleValues(final List<Object> possibleValuesToSet) {
		this.possibleValues = possibleValuesToSet;
	}

	/**
	 * @return the value
	 */
	public Object getValue() {
		return value;
	}

	/**
	 * @param valueToSet the value to set
	 */
	public void setValue(final Object valueToSet) {
		this.value = valueToSet;
	}

}
