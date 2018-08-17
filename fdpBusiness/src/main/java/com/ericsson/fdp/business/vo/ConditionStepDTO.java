package com.ericsson.fdp.business.vo;

import java.util.List;

import com.ericsson.fdp.business.enums.Operator;

/**
 * This class defines the condition step.
 * 
 * @author Ericsson
 * 
 */
public class ConditionStepDTO {

	/** The sub steps present in this step. */
	private List<ConditionSubStepDTO> conditionSubStepDTOs;
	/**
	 * The operator with which this condition step is linked to the one below
	 * it.
	 */
	private Operator operator;

	/**
	 * The constructor for condition step.
	 * 
	 * @param conditionSubStepDTOToSet
	 *            The condition sub step dto to set.
	 * @param operatorToSet
	 *            The operator to set.
	 */
	public ConditionStepDTO(final List<ConditionSubStepDTO> conditionSubStepDTOToSet, final Operator operatorToSet) {
		this.conditionSubStepDTOs = conditionSubStepDTOToSet;
		this.operator = operatorToSet;
	}

	/**
	 * @return the conditionSubStepDTOs
	 */
	public List<ConditionSubStepDTO> getConditionSubStepDTOs() {
		return conditionSubStepDTOs;
	}

	/**
	 * @return the operator
	 */
	public Operator getOperator() {
		return operator;
	}

}
