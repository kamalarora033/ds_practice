package com.ericsson.fdp.business.expression.impl;

import java.util.LinkedList;
import java.util.List;

import ch.qos.logback.classic.Logger;

import com.ericsson.fdp.business.exception.ExpressionFailedException;
import com.ericsson.fdp.business.expression.CombinedExpressionContainer;
import com.ericsson.fdp.business.expression.Expression;
import com.ericsson.fdp.business.util.LoggerUtil;
import com.ericsson.fdp.core.exception.EvaluationFailedException;
import com.ericsson.fdp.core.logging.FDPLogger;
import com.ericsson.fdp.core.request.FDPRequest;
import com.ericsson.fdp.dao.enums.ExpressionOperatorEnum;

public class FDPCombinedAIRExpressionOperator extends ExpressionOperator {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public FDPCombinedAIRExpressionOperator(Expression leftHandExpressionToSet, ExpressionOperatorEnum operatorToSet,
			Expression rightHandExpressionToSet) {
		super(leftHandExpressionToSet, operatorToSet, rightHandExpressionToSet);
		// TODO Auto-generated constructor stub
	}

	@Override
	public boolean evaluateExpression(final FDPRequest fdpRequest) throws ExpressionFailedException {
		//added by rahul
		boolean result = false;
		Logger circleLogger = LoggerUtil.getSummaryLoggerFromRequest(fdpRequest);
        circleLogger.debug("FDPCombinedAIRExpressionOperator::evaluateExpression --In");
		if (leftHandExpression == null || rightHandExpression == null) {
			FDPLogger.error(circleLogger, getClass(), "evaluateExpression()", LoggerUtil.getRequestAppender(fdpRequest)
					+ "The expression is not well formed. Found null elements. ");
			throw new ExpressionFailedException("The expression is not well formed. Found null elements. ");
		}
		FDPLogger.debug(circleLogger, getClass(), "evaluateExpression()", LoggerUtil.getRequestAppender(fdpRequest)
				+ "Executing expression operator");	
		/**
		 * Retrieving the list of all the expressions
		 */
		List<CombinedExpressionContainer> CombinedExpressionContainers =  fetchAllExpression(this,new LinkedList<CombinedExpressionContainer>());

		int counter = 0;
		if(CombinedExpressionContainers!=null && CombinedExpressionContainers.size()>0)
		{
			for(CombinedExpressionContainer  expressionsContainer : CombinedExpressionContainers)
			{
				Expression expression = expressionsContainer.getExpression();
				ExpressionOperatorEnum expressionOperatorEnum = expressionsContainer.getExpressionOperator();
				if(counter==0)
				{
					if(expression instanceof FDPCombinedAIRExpressionOperator)
						result = ((FDPCombinedAIRExpressionOperator)expression).evaluateCombinedExpression((FDPCombinedAIRExpressionOperator)expression, fdpRequest, circleLogger);
					else
						result = expression.evaluateExpression(fdpRequest);
				}
				else
				{
					switch(expressionOperatorEnum)
					{
					case AND:

						if(expression instanceof FDPCombinedAIRExpressionOperator)
							result = result && ((FDPCombinedAIRExpressionOperator)expression).evaluateCombinedExpression((FDPCombinedAIRExpressionOperator)expression, fdpRequest, circleLogger);
						else
							result = result && expression.evaluateExpression(fdpRequest);
						break;
					case OR:

						if(expression instanceof FDPCombinedAIRExpressionOperator)
							result = result || ((FDPCombinedAIRExpressionOperator)expression).evaluateCombinedExpression((FDPCombinedAIRExpressionOperator)expression, fdpRequest, circleLogger);
						else
							result = result || expression.evaluateExpression(fdpRequest);
						break;
					default:
						break;


					}
				}
				counter++;

			}		
		}
		else
		{
			FDPLogger.error(circleLogger, getClass(), "evaluateExpression()", LoggerUtil.getRequestAppender(fdpRequest)
					+ "Unable to evaluate condtaints for the step ");
			throw new ExpressionFailedException("Unable to evaluate condtaints for the step. ");
		}
		circleLogger.debug("Constraint result for the step is : " + result);
        circleLogger.debug("FDPCombinedAIRExpressionOperator::evaluateExpression --Out");
		//end
		return result;
	}



	/**
	 * Added by Rahul : This method is to retrieve all the expressions in a container , which contains the expression that
	 * could be expression or combined expression and the relative operator for the next expression evaluation .  
	 * It's a recursive method to create list of expression and the operator to be applied
	 * @param expresion
	 * @param combinedExpressionContainers
	 * @return The list of CombinedExpressionContainer that include both combined and FDPExpression
	 */
	private List<CombinedExpressionContainer> fetchAllExpression( Expression expresion , List<CombinedExpressionContainer> combinedExpressionContainers)
	{
		Expression leftExpression = null;
		Expression rightExpression = null;
		
		/**
		 * Recursive call till it is a combined expression
		 */
		if((leftExpression = ((FDPCombinedAIRExpressionOperator)expresion).leftHandExpression) instanceof FDPCombinedAIRExpressionOperator)
		{

			/**
			 * check if next left is also combined then make a recursive call for left expression
			 */
			if(((FDPCombinedAIRExpressionOperator)leftExpression).leftHandExpression instanceof FDPCombinedAIRExpressionOperator)
			{
				fetchAllExpression((FDPCombinedAIRExpressionOperator)leftExpression,combinedExpressionContainers);
			}
			/*
			 * If a combined expression and contains 'id' and 'value' then make save it as a combined expression 
			 */
			else if(isValidCombinedExpression(leftExpression))
			{
				combinedExpressionContainers.add(new CombinedExpressionContainer(leftExpression, ((FDPCombinedAIRExpressionOperator)leftExpression).operator));
			}
			//case where combined expression but not  include id and value in left and right expression respectively
			else
			{
				//combinedExpressionContainers.add(new CombinedExpressionContainer(((FDPCombinedAIRExpressionOperator)leftExpression).leftHandExpression, ((FDPCombinedAIRExpressionOperator)leftExpression).operator));
				//combinedExpressionContainers.add(new CombinedExpressionContainer(((FDPCombinedAIRExpressionOperator)leftExpression).rightHandExpression, ((FDPCombinedAIRExpressionOperator)leftExpression).operator));
				createCombinedExpressionIfApplicable(combinedExpressionContainers,((FDPCombinedAIRExpressionOperator)leftExpression).leftHandExpression,((FDPCombinedAIRExpressionOperator)leftExpression).operator);
				createCombinedExpressionIfApplicable(combinedExpressionContainers,((FDPCombinedAIRExpressionOperator)leftExpression).rightHandExpression,((FDPCombinedAIRExpressionOperator)leftExpression).operator);
			}

		}
		/**
		 * In case of Left expression is not of combined type then add it as a FdpExpression condition
		 */
		else
		{
			//combinedExpressionContainers.add(new CombinedExpressionContainer(((FDPCombinedAIRExpressionOperator)expresion).leftHandExpression, ((FDPCombinedAIRExpressionOperator)expresion).operator));
			createCombinedExpressionIfApplicable(combinedExpressionContainers,((FDPCombinedAIRExpressionOperator)expresion).leftHandExpression,((FDPCombinedAIRExpressionOperator)expresion).operator);
		}

		/**
		 * Recursive call till it is a combined expression
		 */

		if((rightExpression=((FDPCombinedAIRExpressionOperator)expresion).rightHandExpression) instanceof FDPCombinedAIRExpressionOperator)
		{
			/**
			 * check if next right is also combined then make a recursive call for right expression
			 */
			if(((FDPCombinedAIRExpressionOperator)rightExpression).rightHandExpression instanceof FDPCombinedAIRExpressionOperator)
			{
				fetchAllExpression((FDPCombinedAIRExpressionOperator)rightExpression,combinedExpressionContainers);
			}
			else if(isValidCombinedExpression(rightExpression))
			{
				combinedExpressionContainers.add(new CombinedExpressionContainer(rightExpression, ((FDPCombinedAIRExpressionOperator)rightExpression).operator));
			}
			//case where combined expression but not of include id and value in left and right expression respectively
			else
			{
				//combinedExpressionContainers.add(new CombinedExpressionContainer(((FDPCombinedAIRExpressionOperator)rightExpression).leftHandExpression, ((FDPCombinedAIRExpressionOperator)rightExpression).operator));
				//combinedExpressionContainers.add(new CombinedExpressionContainer(((FDPCombinedAIRExpressionOperator)rightExpression).rightHandExpression, ((FDPCombinedAIRExpressionOperator)rightExpression).operator));

				createCombinedExpressionIfApplicable(combinedExpressionContainers,((FDPCombinedAIRExpressionOperator)rightExpression).leftHandExpression,((FDPCombinedAIRExpressionOperator)rightExpression).operator);
				createCombinedExpressionIfApplicable(combinedExpressionContainers,((FDPCombinedAIRExpressionOperator)rightExpression).rightHandExpression,((FDPCombinedAIRExpressionOperator)rightExpression).operator);
			}
		}

		/**
		 * In case of right expression is not of combined type then add it as a FdpExpression condition
		 */
		else 
		{


			//combinedExpressionContainers.add(new CombinedExpressionContainer(((FDPCombinedAIRExpressionOperator)expresion).rightHandExpression, ((FDPCombinedAIRExpressionOperator)expresion).operator));
			createCombinedExpressionIfApplicable(combinedExpressionContainers,((FDPCombinedAIRExpressionOperator)expresion).rightHandExpression,((FDPCombinedAIRExpressionOperator)expresion).operator);
		}

		return combinedExpressionContainers;
	}



	/**
	 *  Added by Rahul :  This method is to create the expression and add it to list , while adding the expression first
	 *  check if the last added expression in 'combinedExpressionContainers' list and the given expression are valid for
	 *  combined expression the create the a 'combined' expression and set/update it in the list. 
	 * @param combinedExpressionContainers
	 * @param expression
	 * @param operator
	 */
	private void createCombinedExpressionIfApplicable( List<CombinedExpressionContainer> combinedExpressionContainers , Expression expression , ExpressionOperatorEnum operator )
	{
		if(combinedExpressionContainers.size()>0)
		{
			Integer lastIndex = combinedExpressionContainers.size()-1;
			if(combinedExpressionContainers.get(lastIndex).getExpression() instanceof FDPExpressionCondition && expression instanceof FDPExpressionCondition)
			{

				Expression combinedExpression = new FDPCombinedAIRExpressionOperator(combinedExpressionContainers.get(lastIndex).getExpression(), ExpressionOperatorEnum.AND, expression);

				if(isValidCombinedExpression(combinedExpression))
				{
					//combinedExpressionContainers.add(counter, new CombinedExpressionContainer(combinedExpression, expressionOperator));
					ExpressionOperatorEnum expressionOperatorEnum = combinedExpressionContainers.get(lastIndex).getExpressionOperator();
					combinedExpressionContainers.set(lastIndex, new CombinedExpressionContainer(combinedExpression, expressionOperatorEnum));
					return;
				}


			}
		}

		combinedExpressionContainers.add(new CombinedExpressionContainer(expression, operator));		
	}

	/**
	 * Added by Rahul :  This method is to check if the combined expression is valid combined by 
	 * checking if left and right expression contains the 'id' and 'value' in left and right expression respectively
	 * @param expression
	 * @return
	 */
	private boolean isValidCombinedExpression(final Expression expression)
	{
		FDPCombinedAIRExpressionOperator combinedExpression = (FDPCombinedAIRExpressionOperator) expression;
		boolean validExpression = false;
		if(combinedExpression.operator.equals(ExpressionOperatorEnum.AND))
		{
			String expressionString = ((FDPExpressionCondition)combinedExpression.getLeftHandExpression()).getLeftOperand().toString();
			if (expressionString.endsWith("ID") || expressionString.endsWith("Id") || expressionString.endsWith("id")) {
				expressionString = ((FDPExpressionCondition)combinedExpression.getRightHandExpression()).getLeftOperand().toString();
				if (expressionString.endsWith("Value") || expressionString.endsWith("value") || expressionString.endsWith("VALUE")) {
					return true;
				}
			}
		}

		return validExpression;
	}

	/**
	 * Added by Rahul : This method is to evaluate the combined statement only , validate both 'id' and 'value'
	 * @param combinedExpression
	 * @param fdpRequest
	 * @param circleLogger
	 * @return
	 * @throws ExpressionFailedException
	 */
	private boolean evaluateCombinedExpression(FDPCombinedAIRExpressionOperator combinedExpression ,final FDPRequest fdpRequest ,final Logger circleLogger ) throws ExpressionFailedException
	{
		boolean result = false;
        circleLogger.debug("FDPCombinedAIRExpressionOperator::evaluateCombinedExpression --->In");
		if (combinedExpression == null)  {
			FDPLogger.error(circleLogger, getClass(), "evaluateCombinedExpression()", LoggerUtil.getRequestAppender(fdpRequest)
					+ "The expression is not well formed. Found null elements. ");
			throw new ExpressionFailedException("The expression is not well formed. Found null elements. ");
		}
		FDPLogger.debug(circleLogger, getClass(), "evaluateCombinedExpression()", LoggerUtil.getRequestAppender(fdpRequest)
				+ "Executing expression operator");
				
		FDPExpressionCondition fdpLeftExpressionCondition = null;
		FDPExpressionCondition fdpRightExpressionCondition = null;
		
		try
		{
			fdpLeftExpressionCondition = (FDPExpressionCondition) combinedExpression.getLeftHandExpression();
			fdpRightExpressionCondition = (FDPExpressionCondition)combinedExpression.getRightHandExpression();
			
			if(fdpLeftExpressionCondition.isForBeneficiary()){
				FDPExpressionCondition.updateSubscriberInRequestForBeneficiary(fdpRequest, false, circleLogger);
			}
			if(fdpRightExpressionCondition.isForBeneficiary())
			{
				FDPExpressionCondition.updateSubscriberInRequestForBeneficiary(fdpRequest, false, circleLogger);
			}
			
			result = (boolean)((FDPCommandLeftOperand)fdpLeftExpressionCondition.getLeftOperand()).evaluateCombinedValue(fdpRequest, combinedExpression);
		}
		catch (EvaluationFailedException e) {
			FDPLogger.error(circleLogger, getClass(), "evaluateCombinedExpression()",
					LoggerUtil.getRequestAppender(fdpRequest) + "Could not evaluate value", e);
			throw new ExpressionFailedException("Could not evaluate value", e);
		} finally{
			if(fdpLeftExpressionCondition.isForBeneficiary()){
				FDPExpressionCondition.updateSubscriberInRequestForBeneficiary(fdpRequest, false, circleLogger);
			}
			if(fdpRightExpressionCondition.isForBeneficiary())
			{
				FDPExpressionCondition.updateSubscriberInRequestForBeneficiary(fdpRequest, false, circleLogger);
			}
		}
		circleLogger.info("FDPCombinedAIRExpressionOperator::evaluateCombinedExpression --->Out");
		return result;
	}
}
