package com.ericsson.fdp.business.util;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import com.ericsson.fdp.business.cache.datageneration.CommandCacheUtil;
import com.ericsson.fdp.business.command.param.impl.CommandParamInput;
import com.ericsson.fdp.business.command.param.impl.CommandParamOutput;
import com.ericsson.fdp.business.condition.AbstractCondition;
import com.ericsson.fdp.business.condition.Condition;
import com.ericsson.fdp.business.condition.impl.ContainsCondition;
import com.ericsson.fdp.business.condition.impl.EqualsCondition;
import com.ericsson.fdp.business.condition.impl.EvaluateFunctionCondition;
import com.ericsson.fdp.business.condition.impl.GreaterThanCondition;
import com.ericsson.fdp.business.condition.impl.GreaterThanEqualsCondition;
import com.ericsson.fdp.business.condition.impl.InCondition;
import com.ericsson.fdp.business.condition.impl.LessThanCondition;
import com.ericsson.fdp.business.condition.impl.LessThanEqualsCondition;
import com.ericsson.fdp.business.condition.impl.NotEqualsCondition;
import com.ericsson.fdp.business.condition.impl.NotInCondition;
import com.ericsson.fdp.business.condition.impl.PatternCondition;
import com.ericsson.fdp.business.condition.impl.RangeCondition;
import com.ericsson.fdp.business.condition.impl.RangeWithExclusion;
import com.ericsson.fdp.business.condition.impl.RangeWithInclusion;
import com.ericsson.fdp.business.enums.AuxRequestParam;
import com.ericsson.fdp.business.enums.Function;
import com.ericsson.fdp.business.exception.ExpressionFailedException;
import com.ericsson.fdp.business.expression.Expression;
import com.ericsson.fdp.business.expression.impl.ExpressionOperator;
import com.ericsson.fdp.business.expression.impl.FDPCombinedAIRExpressionOperator;
import com.ericsson.fdp.business.expression.impl.FDPCommandLeftOperand;
import com.ericsson.fdp.business.expression.impl.FDPExpressionCondition;
import com.ericsson.fdp.business.expression.impl.FDPRequestCommandLeftOperand;
import com.ericsson.fdp.common.constants.FDPConstant;
import com.ericsson.fdp.common.enums.ParameterFeedType;
import com.ericsson.fdp.common.enums.Primitives;
import com.ericsson.fdp.dao.dto.common.charging.ChargingStepDTO;
import com.ericsson.fdp.dao.dto.common.constraint.ConstraintDTO;
import com.ericsson.fdp.dao.dto.common.constraint.ConstraintStepDTO;
import com.ericsson.fdp.dao.dto.common.constraint.SubStepConstraintDTO;
import com.ericsson.fdp.dao.dto.serviceprov.ValidityValueDTO;
import com.ericsson.fdp.dao.dto.tariffenquiry.FDPTarrifConstraintSubStepDTO;
import com.ericsson.fdp.dao.entity.FDPDiscountConstraint;
import com.ericsson.fdp.dao.entity.FDPDiscountConstraintStep;
import com.ericsson.fdp.dao.entity.FDPDiscountConstraintSubStep;
import com.ericsson.fdp.dao.enums.CommandParameterDataType;
import com.ericsson.fdp.dao.enums.CommandParameterType;
import com.ericsson.fdp.dao.enums.ExpressionOperatorEnum;
import com.ericsson.fdp.dao.enums.FDPCommandParameterConditionEnum;

/**
 * This class is a utility class that works on expression.
 * 
 * @author Ericsson
 */
public class ExpressionUtil {

	/**
	 * Instantiates a new expression util.
	 */
	private ExpressionUtil() {

	}

	

	// /**
	// * This method is used to create the expression from a list of condition
	// * step.
	// *
	// * @param conditionStepDTOs
	// * The list of condition step from which the expression is to be
	// * created.
	// * @return The expression formed.
	// * @throws ExpressionFailedException
	// * Exception, in forming the expression.
	// */
	// public static Expression createExpression(final List<ConditionStepDTO>
	// conditionStepDTOs)
	// throws ExpressionFailedException {
	// Expression fdpExpression = null;
	// if (conditionStepDTOs != null) {
	// // Create expression for the first condition.
	// ConditionStepDTO firstCondition =
	// conditionStepDTOs.get(FDPConstant.FIRST_INDEX);
	// fdpExpression =
	// createExpressionForSubStepTobeRemoved(firstCondition.getConditionSubStepDTOs());
	// Operator previousOperator = firstCondition.getOperator();
	// for (int stepLevel = 1; stepLevel < conditionStepDTOs.size();
	// stepLevel++) {
	// ConditionStepDTO conditionStepDTO = conditionStepDTOs.get(stepLevel);
	// Expression childExpression =
	// createExpressionForSubStepTobeRemoved(conditionStepDTO.getConditionSubStepDTOs());
	// // Increase the level of the tree.
	// fdpExpression = createExpressionOperatorNode(fdpExpression,
	// previousOperator, childExpression);
	// previousOperator = conditionStepDTO.getOperator();
	// }
	// }
	// return fdpExpression;
	// }

	/**
	 * This method is used to create the expression from a list of condition
	 * step.
	 * 
	 * @param conditionStepDTOs
	 *            The list of condition step from which the expression is to be
	 *            created.
	 * @return The expression formed.
	 * @throws ExpressionFailedException
	 *             Exception, in forming the expression.
	 */
	public static Expression createExpressionForSteps(final List<ConstraintStepDTO> conditionStepDTOs)
			throws ExpressionFailedException {
		Expression fdpExpression = null;
		if (conditionStepDTOs != null && !conditionStepDTOs.isEmpty()) {
			// Create expression for the first condition.
			final ConstraintStepDTO firstCondition = conditionStepDTOs.get(FDPConstant.FIRST_INDEX);
			fdpExpression = createExpressionForSubStep(firstCondition.getSubStepDTOList());
			ExpressionOperatorEnum previousOperator = null;// ExpressionOperatorEnum.valueOf(firstCondition.getOperator());

			for (ExpressionOperatorEnum expressionOperatorEnum : ExpressionOperatorEnum.values()) {
				if (expressionOperatorEnum.toString().equalsIgnoreCase(firstCondition.getOperator())) {
					previousOperator = expressionOperatorEnum;
					break;
				}	
			}
			
			for (int stepLevel = 1; stepLevel < conditionStepDTOs.size(); stepLevel++) {
				final ConstraintStepDTO ConstraintStepDTO = conditionStepDTOs.get(stepLevel);
				final Expression childExpression = createExpressionForSubStep(ConstraintStepDTO.getSubStepDTOList());
				// Increase the level of the tree.
				fdpExpression = createExpressionOperatorNode(fdpExpression, previousOperator, childExpression);
				previousOperator = ExpressionOperatorEnum.valueOf(ConstraintStepDTO.getOperator());
			}
		}
		return fdpExpression;
	}

	/**
	 * Creates the expression for steps product charging.
	 * 
	 * @param chargingStepDTOs
	 *            the charging step dt os
	 * @return the expression
	 * @throws ExpressionFailedException
	 *             the expression failed exception
	 */
	public static Expression createExpressionForStepsProductCharging(final List<ChargingStepDTO> chargingStepDTOs)
			throws ExpressionFailedException {
		Expression fdpExpression = null;
		if (null != chargingStepDTOs && !chargingStepDTOs.isEmpty()) {
			// Create expression for the first condition.
			final ChargingStepDTO firstCondition = chargingStepDTOs.get(FDPConstant.FIRST_INDEX);
			fdpExpression = createExpressionForSubStep(firstCondition.getSubStepsList());
			ExpressionOperatorEnum previousOperator = ExpressionOperatorEnum.valueOf(firstCondition.getOperator());
			for (int stepLevel = 1; stepLevel < chargingStepDTOs.size(); stepLevel++) {
				final ChargingStepDTO chargingStepDTO = chargingStepDTOs.get(stepLevel);
				final Expression childExpression = createExpressionForSubStep(chargingStepDTO.getSubStepsList());
				// Increase the level of the tree.
				fdpExpression = createExpressionOperatorNode(fdpExpression, previousOperator, childExpression);
				previousOperator = ExpressionOperatorEnum.valueOf(chargingStepDTO.getOperator());
			}
		}
		return fdpExpression;
	}

	/**
	 * This method is used to create the expression operator node.
	 * 
	 * @param leftExpression
	 *            The left expression of the node.
	 * @param operator
	 *            The operator of the node.
	 * @param rightExpression
	 *            The right expression of the node.
	 * @return The newly formed expression node.
	 */
	private static Expression createExpressionOperatorNode(final Expression leftExpression,
			final ExpressionOperatorEnum operator, final Expression rightExpression) {
		final Expression fdpExpression = new ExpressionOperator(leftExpression, operator, rightExpression);
		return fdpExpression;
	}

	public static Expression createExpressionForSubStep(final List<SubStepConstraintDTO> constraintDTOList)
			throws ExpressionFailedException {
		Expression fdpExpression = null;
 		if (constraintDTOList != null && !constraintDTOList.isEmpty()) {
			Collections.sort(constraintDTOList);
			final SubStepConstraintDTO firstConditionSubStep = constraintDTOList.get(FDPConstant.FIRST_INDEX);
			firstConditionSubStep.getPossibleValues();
			fdpExpression = createExpressionCondition(firstConditionSubStep);
			ExpressionOperatorEnum previousOperator = ExpressionOperatorEnum
					.valueOf(firstConditionSubStep.getOperator());
			// check if combined Constraint this only works for AIR constraint
			if (!checkifComninedConstraint(constraintDTOList)) {
				for (int stepLevel = 1; stepLevel < constraintDTOList.size(); stepLevel++) {
					final SubStepConstraintDTO subStepConstraintDTO = constraintDTOList.get(stepLevel);
					final Expression childExpression = createExpressionCondition(subStepConstraintDTO);
					// Increase the level of the tree.
					fdpExpression = createExpressionOperatorNode(fdpExpression, previousOperator, childExpression);
					previousOperator = ExpressionOperatorEnum.valueOf(subStepConstraintDTO.getOperator());
				}
			} else {
				for (int stepLevel = 1; stepLevel < constraintDTOList.size(); stepLevel++) {
					final SubStepConstraintDTO subStepConstraintDTO = constraintDTOList.get(stepLevel);
					final Expression childExpression = createExpressionCondition(subStepConstraintDTO);
					// Increase the level of the tree.
					fdpExpression = createCombinedExpressionOperatorNode(fdpExpression, previousOperator, childExpression);
					previousOperator = ExpressionOperatorEnum.valueOf(subStepConstraintDTO.getOperator());
				}
			}
		}
		return fdpExpression;
	}

	/**
	 * Combined expression Node
	 * @param fdpExpression
	 * @param previousOperator
	 * @param childExpression
	 * @return
	 */
	private static Expression createCombinedExpressionOperatorNode(final Expression leftExpression,
			final ExpressionOperatorEnum operator, final Expression rightExpression) {
		final Expression fdpExpression = new FDPCombinedAIRExpressionOperator(leftExpression, operator, rightExpression);
		return fdpExpression;
	}

	/**
	 * Check if Constraint is AIR Type combined constraint 
	 * @param constraintDTOList
	 * @return
	 */
	private static boolean checkifComninedConstraint(List<SubStepConstraintDTO> constraintDTOList) {
		boolean iscombinedconstraint=false;
		for(SubStepConstraintDTO substepConstrainDTO:constraintDTOList)
		{
			//check for id 
			if(checkforCombinedConstraintid(substepConstrainDTO))
			{
				if(checkforCombinedConstraintvalue(constraintDTOList,substepConstrainDTO))
				{
					iscombinedconstraint=true;
					break;
				}
			}
		}
		return iscombinedconstraint;
	}

	/**
	 * Combined Constraint for air check value exists or not 
	 * @param constraintDTOList
	 * @param substepConstrainDTO
	 * @return
	 */
	private static boolean checkforCombinedConstraintvalue(List<SubStepConstraintDTO> constraintDTOList,
			SubStepConstraintDTO substepConstrainDTO) {
		String constraintidlowercase=substepConstrainDTO.getFullyQualifiedPath().toLowerCase();
		String constraintidstr=substepConstrainDTO.getFullyQualifiedPath().substring(0,constraintidlowercase.lastIndexOf("id"));
		
		for(SubStepConstraintDTO substepconstraintdto: constraintDTOList)
	
		{
			if(substepconstraintdto.getFullyQualifiedPath().contains(constraintidstr))
			{
				return true;
			}
		}
		return false;
	}

	private static boolean checkforCombinedConstraintid(SubStepConstraintDTO substepConstrainDTO) {
		
		
		// Note ehlnopu the below condition should be there but the will have to backtrace why interface in substepConstrainDTO is not commind
		// should be rectified in future.
		//if(substepConstrainDTO.getCommandInterface()!=null &&substepConstrainDTO.getCommandInterface().equals("AIR"))
	//	{
		return (null!=substepConstrainDTO && substepConstrainDTO.getFullyQualifiedPath()!=null)?
						(((substepConstrainDTO.getFullyQualifiedPath().endsWith("ID")||
						substepConstrainDTO.getFullyQualifiedPath().endsWith("Id")||
						substepConstrainDTO.getFullyQualifiedPath().endsWith("id")))?true:false):false;
		//}
		
	}

	/**
	 * Method to handle the return CombinedAirExpression 
	 * @param constraintDTO
	 * @return
	 * @throws ExpressionFailedException
	 */
	private static Expression createAirCombinedExpressionCondition(final SubStepConstraintDTO constraintDTO)throws ExpressionFailedException
	{
		return null;
		
	}
	/**
	 * This method is used to create a expression based on the input.
	 * 
	 * @param constraintDTO
	 *            the constraint object based on which expression is created.
	 * @return the condition created.
	 * @throws ExpressionFailedException
	 *             Exception if expression could not be created.
	 */
	private static Expression createExpressionCondition(final SubStepConstraintDTO constraintDTO)
			throws ExpressionFailedException {

		Expression fdpExpression = null;
		final Condition fdpCondition = createCondition(constraintDTO,
				CommandParameterDataType.valueOf(constraintDTO.getDataType()));

		if(constraintDTO instanceof FDPTarrifConstraintSubStepDTO ) {
			final CommandParamInput commandParamInput = new CommandParamInput(ParameterFeedType.getValue(constraintDTO
					.getCommandName()), AuxRequestParam.getAuxRequestParam(constraintDTO.getFullyQualifiedPath()));
			commandParamInput.setType(CommandParameterType.PRIMITIVE);
			commandParamInput.setPrimitiveValue(Primitives.getValue(constraintDTO.getDataType()));
			commandParamInput.setChilderen(null);
			commandParamInput.setName(constraintDTO.getFullyQualifiedPath());
			final FDPRequestCommandLeftOperand commandLeftOperand = new FDPRequestCommandLeftOperand(commandParamInput);
			fdpExpression = new FDPExpressionCondition(fdpCondition, commandLeftOperand);
		} else {
			final CommandParamOutput commandParamOutput = new CommandParamOutput(constraintDTO.getCommandName(),
					constraintDTO.getFullyQualifiedPath());
			final FDPCommandLeftOperand leftOperand = new FDPCommandLeftOperand(commandParamOutput);
			final Long errorNotificationId = constraintDTO.getErrorNotificationTemplateId();

			// constraintDTO.getErrorNotificationTemplateId() == 0 ? null :

			// errorNotificationType : 0 for False (Sender) and 1 for true
			// (Recipient)
			Boolean errorNotificationType = false;
			final Integer errorNotificationTypeVal = constraintDTO.getErrorNotificationType();

			if (errorNotificationTypeVal != null) {
				if (constraintDTO.getErrorNotificationType() == 0 || constraintDTO.getErrorNotificationType() == 1) {
					errorNotificationType = constraintDTO.getErrorNotificationType() == 0 ? false : true;
				}
			}

			fdpExpression = new FDPExpressionCondition(fdpCondition, leftOperand, errorNotificationId,
					errorNotificationType);
		}

		return fdpExpression;

	}

	/**
	 * This method is used to create a condition based on the input constraints.
	 * 
	 * @param constraintDTO
	 *            the constraint object based on which condition is created.
	 * @param commandParameterDataType
	 *            the data type for which this condition is to be evaluated.
	 * @return the condition formed.
	 * @throws ExpressionFailedException
	 *             Exception if condition could not be created.
	 */
	public static Condition createCondition(final SubStepConstraintDTO constraintDTO,
			final CommandParameterDataType commandParameterDataType) throws ExpressionFailedException {

		AbstractCondition fdpCondition;

		final FDPCommandParameterConditionEnum conditionEnum =
				FDPCommandParameterConditionEnum.getEnumValue(constraintDTO.getCondition().toString());
		switch (conditionEnum) {
		case BETWEEN:
		case FROM_TO:
			fdpCondition =
					new RangeCondition(getCommandParamInput(commandParameterDataType, constraintDTO.getMinValue()),
							getCommandParamInput(commandParameterDataType, constraintDTO.getMaxValue()));
			break;
		case IN:
		case CONTAINS_FOR_AIR_RECHARGE:
			fdpCondition =
					new ContainsCondition(getCommandParameterForList(commandParameterDataType,
							constraintDTO.getPossibleValuesList()));
			break;
		case NOT_IN:
			fdpCondition =
			new NotInCondition(getCommandParameterForList(commandParameterDataType,
					constraintDTO.getPossibleValuesList()));
			break;
		case IS_EQUAL_TO:
			fdpCondition =
					new EqualsCondition(getCommandParamInput(commandParameterDataType,
							constraintDTO.getPossibleValues()));
			break;
		case NOT_EQUAL_TO:
			fdpCondition =
					new NotEqualsCondition(getCommandParamInput(commandParameterDataType,
							constraintDTO.getPossibleValues()));
			break;
		case IS_GREATER_THAN:
			fdpCondition =
					new GreaterThanCondition(
							getCommandParamInput(commandParameterDataType, constraintDTO.getMinValue()));
			break;
		case IS_GREATER_THAN_EQUAL_TO:
			fdpCondition =
					new GreaterThanEqualsCondition(getCommandParamInput(commandParameterDataType,
							constraintDTO.getMinValue()));
			break;
		case IS_LESS_THAN:
			fdpCondition =
					new LessThanCondition(getCommandParamInput(commandParameterDataType, constraintDTO.getMaxValue()));
			break;
		case IS_LESS_THAN_EQUAL_TO:
			fdpCondition =
					new LessThanEqualsCondition(getCommandParamInput(commandParameterDataType,
							constraintDTO.getMaxValue()));
			break;
		case BETWEEN_WITH_EXCLUSION:
			fdpCondition =
					new RangeWithExclusion(getCommandParamInput(commandParameterDataType, constraintDTO.getMinValue()),
							getCommandParamInput(commandParameterDataType, constraintDTO.getMaxValue()),
							getCommandParameterForList(commandParameterDataType, constraintDTO.getPossibleValuesList()));
			break;
		case BETWEEN_WITH_INCLUSION:
			fdpCondition =
					new RangeWithInclusion(getCommandParamInput(commandParameterDataType, constraintDTO.getMinValue()),
							getCommandParamInput(commandParameterDataType, constraintDTO.getMaxValue()),
							getCommandParameterForList(commandParameterDataType, constraintDTO.getPossibleValuesList()));
			break;
		case PATTERN:
			fdpCondition =
					new PatternCondition(getCommandParamInput(commandParameterDataType,
							constraintDTO.getPossibleValues()));
			break;
		case IN_FOR_AIR_RECHARGE:
			final CommandParamInput commandParamInput = new CommandParamInput(ParameterFeedType.FUNCTION, Function.getFunction(constraintDTO
							.getPossibleValues()));
			commandParamInput.setType(CommandParameterType.PRIMITIVE);
			fdpCondition = new InCondition(commandParamInput);
			break;		
		case EVAL_FUNC:
		case EVAL_FUNCTION:
			fdpCondition =
					new EvaluateFunctionCondition(getCommandParamInput(commandParameterDataType,
							constraintDTO.getPossibleValues()), conditionEnum);
			break;
		default:
			throw new ExpressionFailedException("The operator " + constraintDTO.getCondition() + " is not defined.");
		}
		fdpCondition.setCommandParameterDataType(commandParameterDataType);
		return fdpCondition;

	}

	/**
	 * This method is used to create the expression from the sub step.
	 * 
	 * @param dataTypeEnum
	 *            the data type enum
	 * @param possibleValuesList
	 *            the possible values list
	 * @return The newly formed expression.
	 */
	// private static Expression createExpressionCondition(final
	// ConditionSubStepDTO conditionSubStepDTO)
	// throws ExpressionFailedException {
	// Expression fdpExpression = null;
	// AbstractCondition fdpCondition = null;
	// switch (conditionSubStepDTO.getConditionType()) {
	// case RANGE:
	// fdpCondition = new RangeCondition(conditionSubStepDTO.getMinValue(),
	// conditionSubStepDTO.getMaxValue());
	// break;
	// case CONTAINS:
	// fdpCondition = new
	// ContainsCondition(conditionSubStepDTO.getPossibleValues());
	// break;
	// case EQUALS:
	// fdpCondition = new EqualsCondition(conditionSubStepDTO.getValue());
	// break;
	// case GREATER_THAN:
	// fdpCondition = new
	// GreaterThanCondition(conditionSubStepDTO.getMinValue());
	// break;
	// case GREATER_THAN_OR_EQUALS:
	// fdpCondition = new
	// GreaterThanCondition(conditionSubStepDTO.getMinValue());
	// break;
	// case LESSER_THAN:
	// fdpCondition = new LessThanCondition(conditionSubStepDTO.getMaxValue());
	// break;
	// case LESSER_THAN_OR_EQUALS:
	// fdpCondition = new LessThanCondition(conditionSubStepDTO.getMaxValue());
	// break;
	// default:
	// throw new ExpressionFailedException("The operator " +
	// conditionSubStepDTO.getConditionType().toString()
	// + " is not defined.");
	// }
	// fdpExpression = new FDPExpressionCondition(fdpCondition,
	// conditionSubStepDTO.getLeftOperand());
	// return fdpExpression;
	// }

	private static List<CommandParamInput> getCommandParameterForList(final CommandParameterDataType dataTypeEnum,
			final List<Object> possibleValuesList) {
		final List<CommandParamInput> commandParamInput = new ArrayList<CommandParamInput>();
		for (final Object possibleValue : possibleValuesList) {
			commandParamInput.add(getCommandParamInput(dataTypeEnum, (String) possibleValue));
		}
		return commandParamInput;
	}

	/**
	 * Gets the command param input.
	 *            the value to set
	 * @return the command param input
	 */
	private static CommandParamInput getCommandParamInput(final CommandParameterDataType dataTypeEnum,
			final String valueToSet) {
		ParameterFeedType parameterFeedType = null;
		CommandParamInput commandParamInput = null;
		if (CommandParameterDataType.DATE == dataTypeEnum || CommandParameterDataType.DATETIME == dataTypeEnum) {
			parameterFeedType = ParameterFeedType.VALIDITY;
			final ValidityValueDTO validityValueDTO = CommandCacheUtil.getValidityValueDTO(valueToSet);
			commandParamInput = new CommandParamInput(parameterFeedType, validityValueDTO);
		} else {
			parameterFeedType = ParameterFeedType.INPUT;
			commandParamInput = new CommandParamInput(parameterFeedType, valueToSet);
		}
		commandParamInput.setType(CommandParameterType.PRIMITIVE);
		return commandParamInput;
	}
	
	/**
	 * Prepare product discount expression.
	 * 
	 * @param fdpDiscountConstraint
	 * @return
	 * @throws ExpressionFailedException
	 */
	public static Expression prepareProductDiscountExpression(final List<FDPDiscountConstraint> fdpDiscountConstraints) throws ExpressionFailedException {
		final ConstraintDTO constraintDTO = new ConstraintDTO();
		//constraintDTO.setConstraintId(fdpDiscountConstraint.getChargingDiscountId());
		final List<ConstraintStepDTO> constraintStepsList = new ArrayList<ConstraintStepDTO>();
		for(FDPDiscountConstraint fdpDiscountConstraint:fdpDiscountConstraints){
		for(final FDPDiscountConstraintStep discountConstraintStep : fdpDiscountConstraint.getFdpDiscountConstraintSteps()) {
			//final Set<FDPDiscountConstraintStep> fdpDiscountConstraintSteps = fdpDiscountConstraint.getFdpDiscountConstraintSteps();
			final ConstraintStepDTO constraintStepDTO = new ConstraintStepDTO();
			constraintStepDTO.setStepId(discountConstraintStep.getDiscountConstraintStepId());
			constraintStepDTO.setOperator(discountConstraintStep.getOperator());
			constraintStepDTO.setOrderValue(discountConstraintStep.getOrderValue());
			
			final List<SubStepConstraintDTO> subStepDTOList = new ArrayList<SubStepConstraintDTO>();
			
			final Set<FDPDiscountConstraintSubStep> discountConstraintSubSteps = discountConstraintStep.getFdpDiscountConstraintSubSteps();
			for(final FDPDiscountConstraintSubStep fdpDiscountConstraintSubStep :  discountConstraintSubSteps) {
				final SubStepConstraintDTO subStepConstraintDTO = new SubStepConstraintDTO();
				
				subStepConstraintDTO.setSubStepConstraintId(fdpDiscountConstraintSubStep.getDiscountConstraintSubStep());
				subStepConstraintDTO.setStepId(fdpDiscountConstraintSubStep.getDiscountConstraintStepId().getDiscountConstraintStepId());
				subStepConstraintDTO.setCommandCircleId(fdpDiscountConstraintSubStep.getCommandCircleId().getCommandCircleId());
				subStepConstraintDTO.setCommandCircleParameterId(fdpDiscountConstraintSubStep.getCommandCircleParamId().getCommandCircleParamId());
				subStepConstraintDTO.setCondition(Integer.valueOf(fdpDiscountConstraintSubStep.getCondition()));
				subStepConstraintDTO.setMaxValue(fdpDiscountConstraintSubStep.getMaxValue());
				subStepConstraintDTO.setMinValue(fdpDiscountConstraintSubStep.getMinValue());
				subStepConstraintDTO.setOperator(fdpDiscountConstraintSubStep.getOperator());
				subStepConstraintDTO.setOrderValue(fdpDiscountConstraintSubStep.getOrderValue());
				subStepConstraintDTO.setPossibleValues(fdpDiscountConstraintSubStep.getPossibleValues());
				subStepConstraintDTO.setCommandName(fdpDiscountConstraintSubStep.getCommandCircleId()
						.getCommandNameId().getCommandNameToDisplay());
				subStepConstraintDTO.setFullyQualifiedPath(fdpDiscountConstraintSubStep.getCommandCircleParamId()
						.getFullQualifiedName());
				subStepConstraintDTO.setDataType(fdpDiscountConstraintSubStep.getCommandCircleParamId()
						.getCommandParameterId().getGuiElementFormat());
				subStepDTOList.add(subStepConstraintDTO);
			}
			constraintStepDTO.setSubStepDTOList(subStepDTOList);
			constraintStepsList.add(constraintStepDTO);
			constraintDTO.setConstraintStepsList(constraintStepsList);
		}
		}
		return ExpressionUtil.createExpressionForSteps(constraintDTO.getConstraintStepsList());	
	}
}
