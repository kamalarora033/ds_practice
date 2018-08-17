package com.ericsson.fdp.business.charging.impl;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

import com.ericsson.fdp.business.charging.Account;
import com.ericsson.fdp.business.charging.ChargingCalculator;
import com.ericsson.fdp.business.enums.ChargingCalculatorKey;
import com.ericsson.fdp.business.util.ChargingUtil;
import com.ericsson.fdp.core.exception.ExecutionFailedException;
import com.ericsson.fdp.core.request.FDPRequest;

/**
 * This class calculates the charging 
 * 
 * @author ESIASAN
 *
 */
public class ChargingCalculatorImpl implements ChargingCalculator {
	
	private List<Account> selectedAccountsForDeduction = new ArrayList<Account>();
	private Long mainAccountDeduction = 0L;
	
	@Override
	public Map<ChargingCalculatorKey, Object> calculateCharging(
			FDPRequest fdpRequest, boolean isPartialChargingAllowed,
			Map<ChargingCalculatorKey, Object> calculatorMap, List<Account> productDefinedAccounts)
			throws ExecutionFailedException {
		// Get the product amount to charge
		Object amtToChargeObj = calculatorMap.get(ChargingCalculatorKey.PRODUCT_AMOUNT_TO_CHARGE);
		if(amtToChargeObj != null && amtToChargeObj instanceof Long){
			Long amtToCharge = (Long) amtToChargeObj;
			sortAccountsByPriority(productDefinedAccounts);
			// Get the user accounts that can be used for deduction
			List<Account> deductibleAccounts = getDeductibleAccounts(fdpRequest, productDefinedAccounts);
			// Check if sum of all account values is greater than amount to charge
			if(sumOf(deductibleAccounts) >= amtToCharge){		
			   if(isPartialChargingAllowed){
					doPartialCharging(deductibleAccounts, amtToCharge);
			   }else{
					doNonPartialCharging(deductibleAccounts, amtToCharge);
			   }
			   // Update DA details in chargingMap in case any DA is selected for deduction
			   if(selectedAccountsForDeduction.size() > 0L){
				   calculatorMap.put(ChargingCalculatorKey.DA_DEDUCTION_DETAILS, selectedAccountsForDeduction);
				   calculatorMap.put(ChargingCalculatorKey.REMAINING_AMOUNT_TO_CHARGE, 0L);
			   }
			   if(mainAccountDeduction > 0L){
				   calculatorMap.put(ChargingCalculatorKey.MAIN_ACCOUNT_DEDUCTION_DETAIL, (-1 * mainAccountDeduction));
				   calculatorMap.put(ChargingCalculatorKey.REMAINING_AMOUNT_TO_CHARGE, 0L);
			   }
			}
		}
		return calculatorMap;
	}
	
	/**
	 * This method does partial charging on subscriber accounts
	 * @param deductibleAccounts
	 * @param amtToCharge
	 * @return
	 */
	private Boolean doPartialCharging(List<Account> deductibleAccounts, Long amtToCharge){
		Boolean isPartialChargingSuccessful = false;
		// try non-partial charging first
		// isPartialChargingSuccessful = doNonPartialCharging(deductibleAccounts, amtToCharge);
		// do partial charging if non-partial charging is failed
		//if(!isPartialChargingSuccessful){
			for(Account account : deductibleAccounts){
				if(amtToCharge > account.getAccountValue()){					
					if(account instanceof MainAccount){
						mainAccountDeduction = account.getAccountValue();	
					}else{
						String daId = ((DedicatedAccount) account).getDedicatedAccountId();
						selectedAccountsForDeduction.add(new DedicatedAccount(daId, (-1 * account.getAccountValue()), account.getPriority()));
					}
					amtToCharge -= account.getAccountValue();
				}else{
					if(account instanceof MainAccount){
						mainAccountDeduction = amtToCharge;
					}else{
						String daId = ((DedicatedAccount) account).getDedicatedAccountId();
						selectedAccountsForDeduction.add(new DedicatedAccount(daId, (-1 * amtToCharge), account.getPriority()));
					}
					isPartialChargingSuccessful = true;
					amtToCharge = 0L;	
					break;
				}
			}
		//}
		return isPartialChargingSuccessful;
	}
	
	/**
	 * This method does non-partial charging on subscriber accounts
	 * @param deductibleAccounts
	 * @param amtToCharge
	 * @return
	 */
	private Boolean doNonPartialCharging(List<Account> deductibleAccounts, Long amtToCharge){
		Boolean isNonPartialChargingSuccessful = false;
		for(Account account : deductibleAccounts){
			if(amtToCharge <= account.getAccountValue()){
				if(account instanceof MainAccount){
					mainAccountDeduction = amtToCharge;
				}else{
					String daId = ((DedicatedAccount) account).getDedicatedAccountId();
					selectedAccountsForDeduction.add(new DedicatedAccount(daId, (-1 * amtToCharge), account.getPriority()));
				}
				isNonPartialChargingSuccessful = true;
				amtToCharge = 0L;	
				break;
			}
		}
		return isNonPartialChargingSuccessful;
	}
	
	/**
	 * This method returns a list of all accounts that can be used for charging
	 * @param fdpRequest
	 * @param productDefinedAccounts
	 * @return
	 * @throws ExecutionFailedException
	 */
	private List<Account> getDeductibleAccounts(final FDPRequest fdpRequest, List<Account> productDefinedAccounts) throws ExecutionFailedException{
		List<Account> deductibleAccounts = new ArrayList<Account>();
		Map<String, String> allSubscriberDAs = ChargingUtil.getAllSubscriberDAs(fdpRequest,ChargingUtil.getChargingDAList(productDefinedAccounts));
		Long mainAccountBalance = ChargingUtil.getMainAccountBalanceofSubscriber(fdpRequest);
		for(Account account: productDefinedAccounts){
			if(account instanceof DedicatedAccount){
				String daId = ((DedicatedAccount) account).getDedicatedAccountId();
				if(allSubscriberDAs.containsKey(daId)){
					Long daValue = Long.parseLong(allSubscriberDAs.get(daId));
					if(daValue > 0L){
						deductibleAccounts.add(new DedicatedAccount(daId, daValue, account.getPriority()));
					}
				}
			}else if(account instanceof MainAccount && mainAccountBalance > 0L){
				deductibleAccounts.add(new MainAccount(mainAccountBalance, account.getPriority()));
			}
		}
		return deductibleAccounts;
	}
	

	 /**
    * This method returns the sum of all input account values
    * @param allDeductibleDAs
    * @return
    */
	public Long sumOf(List<Account> accounts) {
		Long total = 0L;
		if(accounts != null && !accounts.isEmpty()){
			for(Account account : accounts){
				total += account.getAccountValue();
			}
		}
		return total;
	}
	
	/**
	 * This method will sort the accounts list based on priority
	 * @param accounts
	 */
	public void sortAccountsByPriority(List<Account> accounts){
		Collections.sort(accounts, new Comparator<Account>() {
		    public int compare(Account account1, Account account2) {
		        return account1.getPriority() - account2.getPriority();
		    }
		});
	}

}
