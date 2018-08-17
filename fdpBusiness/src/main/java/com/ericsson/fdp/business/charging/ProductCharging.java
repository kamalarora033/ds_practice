package com.ericsson.fdp.business.charging;

import java.io.Serializable;

import com.ericsson.fdp.business.FDPExecutable;
import com.ericsson.fdp.business.FDPRollbackable;
import com.ericsson.fdp.business.charging.value.ChargingValue;
import com.ericsson.fdp.business.exception.StepException;
import com.ericsson.fdp.business.vo.CommandExecutionStatus;
import com.ericsson.fdp.common.enums.ExternalSystem;
import com.ericsson.fdp.core.exception.ExecutionFailedException;
import com.ericsson.fdp.core.request.FDPRequest;

/*
Feature Name: Discounted Charging
Changes: Added setter for charging Discount
Date: 18-12-2015
Singnum Id:ESIASAN
*/

/**
 * This interface provides the method to execute product charging.
 * 
 * @author Ericsson
 * 
 */
public interface ProductCharging extends FDPExecutable<FDPRequest, CommandExecutionStatus>, FDPRollbackable, Serializable {
	
	/**
	 * This method is used to evaluate parameter value for the charging.
	 * 
	 * @param fdpRequest
	 *            The request object.
	 * @return true, if charging is required, false otherwise.
	 * @throws StepException
	 *             Exception, if any in the execution of parameter evaluation.
	 */
	ChargingValue evaluateParameterValue(FDPRequest fdpRequest) throws ExecutionFailedException;
	
	/**
	 * This method will get the external System type for charging.
	 * @return
	 */
	ExternalSystem getExternalSystem();
	
	/**
	 * Sets the Discount to the charging.
	 * @param chargingDiscount
	 */
	public void setChargingDiscount(Discount chargingDiscount);	
	
	
	/**
	 * This method will get the external System type for charging.
	 * @return
	 */
	public void setExternalSystem(ExternalSystem externalsystem);
	

}
