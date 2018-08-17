package com.ericsson.fdp.business.charging;

import java.util.List;
import java.util.Map;

import com.ericsson.fdp.business.enums.ChargingCalculatorKey;
import com.ericsson.fdp.core.exception.ExecutionFailedException;
import com.ericsson.fdp.core.request.FDPRequest;

/**
 * Interface for Charging Value calculation.
 * 
 * @author ESIASAN
 *
 */
public interface ChargingCalculator {

	/**
	 * This method will calculate the Charging value.
	 * 
	 * @param fdpRequest
	 * @param isPartialChargingAllowed
	 * @param calculatorMap
	 * @return
	 */
	Map<ChargingCalculatorKey,Object> calculateCharging(final FDPRequest fdpRequest, final boolean isPartialChargingAllowed, final Map<ChargingCalculatorKey,Object> calculatorMap, final List<Account> productDefinedAccounts) throws ExecutionFailedException;
}
