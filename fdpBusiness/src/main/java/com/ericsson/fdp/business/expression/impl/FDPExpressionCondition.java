package com.ericsson.fdp.business.expression.impl;

import java.util.ArrayList;
import java.util.List;

import ch.qos.logback.classic.Logger;

import com.ericsson.fdp.business.charging.impl.ExpressionCondition;
import com.ericsson.fdp.business.condition.Condition;
import com.ericsson.fdp.business.condition.impl.EqualsCondition;
import com.ericsson.fdp.business.condition.impl.GreaterThanCondition;
import com.ericsson.fdp.business.condition.impl.GreaterThanEqualsCondition;
import com.ericsson.fdp.business.condition.impl.LessThanCondition;
import com.ericsson.fdp.business.condition.impl.LessThanEqualsCondition;
import com.ericsson.fdp.business.condition.impl.NotEqualsCondition;
import com.ericsson.fdp.business.enums.AuxRequestParam;
import com.ericsson.fdp.business.enums.ConditionType;
import com.ericsson.fdp.business.exception.ExpressionFailedException;
import com.ericsson.fdp.business.expression.Expression;
import com.ericsson.fdp.business.expression.LeftOperand;
import com.ericsson.fdp.business.util.LoggerUtil;
import com.ericsson.fdp.business.util.ServiceProvisioningUtil;
import com.ericsson.fdp.common.exception.ConditionFailedException;
import com.ericsson.fdp.core.command.param.CommandParam;
import com.ericsson.fdp.core.exception.EvaluationFailedException;
import com.ericsson.fdp.core.logging.FDPLogger;
import com.ericsson.fdp.core.request.FDPRequest;
import com.ericsson.fdp.core.request.impl.FDPRequestImpl;

/**
 * The class defines the expressions that are the leaf nodes and have condition
 * to be evaluated.
 * 
 * @author Ericsson
 * 
 */
public class FDPExpressionCondition implements Expression {

	/**
	 * 
	 */
	private static final long serialVersionUID = -8490847533368571837L;

	/** The condition that is to be evaluated. */
	private Condition fdpCondition;

	/** The left hand operand. */
	private LeftOperand leftOperand;
	
	/** The failure notification Id to be used **/
	private Long failNotificationId = null;
	
	/** The boolean flag that indicates the MSISDN to use for evaluating condition **/
	private boolean forBeneficiary = false;

	/**
	 * The constructor for the class.
	 * 
	 * @param fdpConditionToSet
	 *            The fdp condition to set.
	 * @param leftOperandToSet
	 *            The left operand to set.
	 */
	public FDPExpressionCondition(final Condition fdpConditionToSet,
			final LeftOperand leftOperandToSet) {
		this.fdpCondition = fdpConditionToSet;
		this.leftOperand = leftOperandToSet;
	}
	
	public FDPExpressionCondition(final Condition fdpConditionToSet, final LeftOperand leftOperandToSet,
			final Long failNotificationId, final boolean forBeneficiary) {
		this.fdpCondition = fdpConditionToSet;
		this.leftOperand = leftOperandToSet;
		this.failNotificationId = failNotificationId;
		this.forBeneficiary = forBeneficiary;
	}
	
	
	public Condition getFdpCondition() {
		return fdpCondition;
	}
	
	

	public LeftOperand getLeftOperand() {
		return leftOperand;
	}

	
	
	/**
	 * @return the forBeneficiary
	 */
	public boolean isForBeneficiary() {
		return forBeneficiary;
	}

	@Override
	public boolean evaluateExpression(final FDPRequest fdpRequest) throws ExpressionFailedException {
		Logger circleLogger = LoggerUtil.getSummaryLoggerFromRequest(fdpRequest);
		boolean result = false;
		try {
			// if forBeneficiary is true, then execute the constraint for beneficiary number
			if(forBeneficiary){
				updateSubscriberInRequestForBeneficiary(fdpRequest, true, circleLogger);
			}
			Object leftOperandValue = leftOperand.evaluateValue(fdpRequest);
			FDPLogger.debug(circleLogger, getClass(), "evaluateExpression()", LoggerUtil.getRequestAppender(fdpRequest)
									+ "Evaluating the expression using left operand value "
									+ leftOperandValue + " for condition "
									+ fdpCondition);
			
			if(leftOperand instanceof FDPCommandLeftOperand){
				if(((FDPCommandLeftOperand)leftOperand).toString().toLowerCase().contains("accumulatorid") && fdpCondition instanceof EqualsCondition){
						leftOperandValue = ((EqualsCondition)fdpCondition).getRightHandOperand().getDefinedValue();
				}else if((null == leftOperandValue) && (((FDPCommandLeftOperand)leftOperand).toString().toLowerCase().contains("accumulatorvalue")
						|| ((FDPCommandLeftOperand)leftOperand).toString().toLowerCase().contains("offerinformationlist")) ){
					leftOperandValue = "0";
				} 
			}
			
			if (null != leftOperandValue) {
				result = fdpCondition.evaluate(leftOperandValue, fdpRequest, false);
				if (result) {
					updateValidatedExpressionConditions(fdpRequest);
					((FDPRequestImpl)fdpRequest).putAuxiliaryRequestParameter(AuxRequestParam.NOTIFICATION_TO_OVERRIDE, null);
				} else{
					if(null != failNotificationId){
						((FDPRequestImpl)fdpRequest).putAuxiliaryRequestParameter(AuxRequestParam.NOTIFICATION_TO_OVERRIDE, failNotificationId);
					}
				}
			}
		} catch (ConditionFailedException e) {
			FDPLogger.error(circleLogger, getClass(), "evaluateExpression()",
					LoggerUtil.getRequestAppender(fdpRequest) + "Invalid expression formed", e);
			throw new ExpressionFailedException("Invalid expression formed", e);
		} catch (EvaluationFailedException e) {
			FDPLogger.error(circleLogger, getClass(), "evaluateExpression()",
					LoggerUtil.getRequestAppender(fdpRequest) + "Could not evaluate value", e);
			throw new ExpressionFailedException("Could not evaluate value", e);
		} finally{
			if(forBeneficiary){
				updateSubscriberInRequestForBeneficiary(fdpRequest, false, circleLogger);
			}
		}
		return result;
	}

	@Override
	public String toString() {
		return leftOperand.toString() + " " + fdpCondition.toString();
	}
	
	/**
	 * This method adds the conditions that validates as true in validated
	 * @param fdpRequest
	 */
	private void updateValidatedExpressionConditions(FDPRequest fdpRequest){
		@SuppressWarnings("unchecked")
		List<ExpressionCondition> validatedExpressions = (List<ExpressionCondition>) (forBeneficiary ? fdpRequest.getAuxiliaryRequestParameter(AuxRequestParam.VALIDATED_EXPRESSIONS_BENEFICIARY) : 
			fdpRequest.getAuxiliaryRequestParameter(AuxRequestParam.VALIDATED_EXPRESSIONS_REQUESTER));
		if (null != validatedExpressions) {
			if(fdpCondition instanceof EqualsCondition){
				validatedExpressions.add(new ExpressionCondition(leftOperand.toString().toLowerCase(), ConditionType.EQUALS,
						((EqualsCondition)fdpCondition).getRightHandOperand().getDefinedValue(), ((EqualsCondition)fdpCondition).getCommandParameterDataType()));
			}else if(fdpCondition instanceof LessThanCondition){
				validatedExpressions.add(new ExpressionCondition(leftOperand.toString().toLowerCase(), ConditionType.LESSER_THAN,
						((LessThanCondition)fdpCondition).getRightHandOperand().getDefinedValue(), ((LessThanCondition)fdpCondition).getCommandParameterDataType()));
			}else if(fdpCondition instanceof GreaterThanCondition){
				validatedExpressions.add(new ExpressionCondition(leftOperand.toString().toLowerCase(), ConditionType.GREATER_THAN,
						((GreaterThanCondition)fdpCondition).getRightHandOperand().getDefinedValue(), ((GreaterThanCondition)fdpCondition).getCommandParameterDataType()));
			}else if(fdpCondition instanceof LessThanEqualsCondition){
				validatedExpressions.add(new ExpressionCondition(leftOperand.toString().toLowerCase(), ConditionType.LESSER_THAN_OR_EQUALS,
						((LessThanEqualsCondition)fdpCondition).getRightHandOperand().getDefinedValue(), ((LessThanEqualsCondition)fdpCondition).getCommandParameterDataType()));
			}else if(fdpCondition instanceof GreaterThanEqualsCondition){
				validatedExpressions.add(new ExpressionCondition(leftOperand.toString().toLowerCase(), ConditionType.GREATER_THAN_OR_EQUALS,
						((GreaterThanEqualsCondition)fdpCondition).getRightHandOperand().getDefinedValue(), ((GreaterThanEqualsCondition)fdpCondition).getCommandParameterDataType()));
			}
			else if(fdpCondition instanceof NotEqualsCondition){
				validatedExpressions.add(new ExpressionCondition(leftOperand.toString().toLowerCase(), ConditionType.NOT_EQUALS_TO,
						((NotEqualsCondition)fdpCondition).getRightHandOperand().getDefinedValue(), ((NotEqualsCondition)fdpCondition).getCommandParameterDataType()));
			}
		}
	}
	
	/**
	 * This method will updates the Beneficiary number in request.
	 * 
	 * @param fdpRequest
	 * @param toUdpateBeneficiary
	 */
	public static void updateSubscriberInRequestForBeneficiary(final FDPRequest fdpRequest, final boolean toUdpateBeneficiary, final Logger logger) {
		if(toUdpateBeneficiary) {
			((FDPRequestImpl)fdpRequest).putAuxiliaryRequestParameter(AuxRequestParam.REQUESTOR_MSISDN, fdpRequest.getSubscriberNumber());;
			Object beneficiaryMsisdnObject = fdpRequest.getAuxiliaryRequestParameter(AuxRequestParam.ME2U_SUBSCRIBER_TO_CREDIT);
			if(null != beneficiaryMsisdnObject){
				((FDPRequestImpl)fdpRequest).putAuxiliaryRequestParameter(AuxRequestParam.BENEFICIARY_FLAG, true);
				FDPLogger.debug(logger, ServiceProvisioningUtil.class, "updateSubscriberInRequestForBeneficiary", "Updating Subscriber Number in request from "
						+ fdpRequest.getSubscriberNumber() + " to " + beneficiaryMsisdnObject.toString());
				((FDPRequestImpl)fdpRequest).setSubscriberNumber(Long.valueOf(beneficiaryMsisdnObject.toString()));
			}
		} else {
			((FDPRequestImpl)fdpRequest).putAuxiliaryRequestParameter(AuxRequestParam.BENEFICIARY_FLAG, false);
			if(fdpRequest.getAuxiliaryRequestParameter(AuxRequestParam.REQUESTOR_MSISDN) != null){
				((FDPRequestImpl)fdpRequest).setSubscriberNumber((Long)fdpRequest.getAuxiliaryRequestParameter(AuxRequestParam.REQUESTOR_MSISDN));
			}
		}
	}
	
	
	
}