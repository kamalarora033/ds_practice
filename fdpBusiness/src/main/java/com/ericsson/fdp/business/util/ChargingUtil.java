package com.ericsson.fdp.business.util;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.apache.poi.ss.usermodel.charts.AxisCrossBetween;

import ch.qos.logback.classic.Logger;

import com.ericsson.fdp.FDPCacheable;
import com.ericsson.fdp.business.charging.Account;
import com.ericsson.fdp.business.charging.Discount;
import com.ericsson.fdp.business.charging.ProductCharging;
import com.ericsson.fdp.business.charging.impl.DABasedAirCharging;
import com.ericsson.fdp.business.charging.impl.DedicatedAccount;
import com.ericsson.fdp.business.charging.impl.FixedCharging;
import com.ericsson.fdp.business.charging.impl.VariableCharging;
import com.ericsson.fdp.business.command.param.impl.CommandParamInput;
import com.ericsson.fdp.business.enums.AuxRequestParam;
import com.ericsson.fdp.business.enums.Command;
import com.ericsson.fdp.business.enums.ParamTransformationType;
import com.ericsson.fdp.business.enums.RequestMetaValuesKey;
import com.ericsson.fdp.business.exception.ExpressionFailedException;
import com.ericsson.fdp.business.exception.StepException;
import com.ericsson.fdp.business.product.Product;
import com.ericsson.fdp.business.request.impl.FulfillmentRequestImpl;
import com.ericsson.fdp.common.constants.FDPConstant;
import com.ericsson.fdp.common.enums.ExternalSystem;
import com.ericsson.fdp.common.enums.ModuleType;
import com.ericsson.fdp.common.enums.Status;
import com.ericsson.fdp.core.command.FDPCommand;
import com.ericsson.fdp.core.command.param.CommandParam;
import com.ericsson.fdp.core.exception.ExecutionFailedException;
import com.ericsson.fdp.core.logging.FDPLogger;
import com.ericsson.fdp.core.request.FDPRequest;
import com.ericsson.fdp.core.request.impl.FDPRequestImpl;
import com.ericsson.fdp.core.serviceprovisioning.bean.FDPMetaBag;
import com.ericsson.fdp.core.utils.ApplicationConfigUtil;
import com.ericsson.fdp.dao.entity.ExternalSystemType;
import com.ericsson.fdp.dao.enums.ChargingType;
import com.ericsson.fdp.dao.enums.ProductAdditionalInfoEnum;

public class ChargingUtil {
	
	/** The main account value **/
	public static final String ACCOUNT_VALUE_1 = "accountValue1";
	/** The dedicated account update information. **/
	public static final String DEDICATED_ACCOUNT_INFORMATION = "dedicatedAccountInformation";
	/** The dedicated account id **/
	public static final String DEDICATED_ACCOUNT_ID = "dedicatedAccountID";
	/** The relative amount attribute for main account **/
	public static final String ADJUSTMENT_AMOUNT_VALUE = "adjustmentAmountRelative";
	/** The dedicated account value **/
	public static final String DEDICATED_ACCOUNT_VALUE = "dedicatedAccountValue1";
	/** The dedicated account update information. */
	public static final String DEDICATED_ACCOUNT_UPDATE_INFORMATION = "dedicatedAccountUpdateInformation";
	/** The constant DOT SEPARATOR **/
	private static final String DOT_SEPARATOR = FDPConstant.PARAMETER_SEPARATOR;
	/** The dedicated account unit type **/
	public static final String DEDICATE_ACCOUNT_TYPE = "dedicatedAccountUnitType";
	/** The dedicated account money type value **/
	public static final String DEDICATE_ACCOUNT_MONEY_TYPE = "1";
	/** The dedicated account value new param**/
	public static final String DEDICATED_ACCOUNT_VALUE_NEW = "dedicatedAccountValueNew";
	/** The dedicated account product id **/
	public static final String PRODUCT_ID = "productID";
	
	/**
	 * This method returns the Account Balance 1 of Subscriber.
	 * @param fdpRequest
	 * @return
	 * @throws ExecutionFailedException
	 */
	public static Long getMainAccountBalanceofSubscriber(FDPRequest fdpRequest) throws ExecutionFailedException{
		FDPCommand getAccDetailCmd = fdpRequest.getExecutedCommand(Command.GETACCOUNTDETAILS.getCommandDisplayName());
		if(getAccDetailCmd == null){
			final FDPCacheable cachedCommand = ApplicationConfigUtil.getMetaDataCache().getValue(new FDPMetaBag(fdpRequest.getCircle(), ModuleType.COMMAND, Command.GETACCOUNTDETAILS.getCommandDisplayName()));
			if (cachedCommand instanceof FDPCommand) {
				getAccDetailCmd = (FDPCommand) cachedCommand;
				Status status = getAccDetailCmd.execute(fdpRequest);
				if (!status.equals(Status.SUCCESS)) {
					throw new ExecutionFailedException("Get Account Details could not be executed");
				}else{
					fdpRequest.addExecutedCommand(getAccDetailCmd);
				}
			} else {
				throw new ExecutionFailedException("Get Account Details not configured in cache");
			}			
		}
		CommandParam accoutBalanceParam = getAccDetailCmd.getOutputParam(ACCOUNT_VALUE_1);
		if(accoutBalanceParam == null){
			throw new ExecutionFailedException("Subscriber Account Balance 1 not found in the command output parameters.");
		}
		return Long.parseLong(accoutBalanceParam.getValue().toString());
	}
	
	/**
	 * This method returns a map of all DAs of a particular subscriber
	 * @param fdpRequest
	 * @return
	 * @throws ExecutionFailedException
	 */
	public static Map<String, String> getAllSubscriberDAs(final FDPRequest fdpRequest, final List<String> dalist) throws ExecutionFailedException{
		Map<String, String> allSubscriberDAs = new LinkedHashMap<String, String>();
		FDPCommand gbadCommand = fdpRequest.getExecutedCommand(Command.GET_BALANCE_AND_DATE.getCommandDisplayName());
		if(gbadCommand == null){
			final FDPCacheable cachedCommand = ApplicationConfigUtil.getMetaDataCache().getValue(new FDPMetaBag(fdpRequest.getCircle(), ModuleType.COMMAND, Command.GET_BALANCE_AND_DATE.getCommandDisplayName()));
			if (cachedCommand instanceof FDPCommand) {
				gbadCommand = (FDPCommand) cachedCommand;
				Status status = gbadCommand.execute(fdpRequest);
				if (!status.equals(Status.SUCCESS)) {
					throw new ExecutionFailedException("Get Balance and Date could not be executed");
				}else{
					fdpRequest.addExecutedCommand(gbadCommand);
				}	
			} else {
				throw new ExecutionFailedException("Get Balance and Date not configured in cache");
			}
		}
		for(int i = 0; gbadCommand.getOutputParam((DEDICATED_ACCOUNT_INFORMATION + DOT_SEPARATOR + i + DOT_SEPARATOR + DEDICATED_ACCOUNT_ID).toLowerCase()) != null; i++){
			CommandParam dedicatedAccountID = gbadCommand.getOutputParam((DEDICATED_ACCOUNT_INFORMATION + DOT_SEPARATOR + i + DOT_SEPARATOR + DEDICATED_ACCOUNT_ID).toLowerCase());
			CommandParam dedicatedAccountValue = gbadCommand.getOutputParam((DEDICATED_ACCOUNT_INFORMATION + DOT_SEPARATOR + i + DOT_SEPARATOR + DEDICATED_ACCOUNT_VALUE).toLowerCase());
			final CommandParam dedicatedAccountType = gbadCommand.getOutputParam((DEDICATED_ACCOUNT_INFORMATION + DOT_SEPARATOR + i + DOT_SEPARATOR + DEDICATE_ACCOUNT_TYPE).toLowerCase());
			if(null != dedicatedAccountType && null != dedicatedAccountID && null != dedicatedAccountValue && DEDICATE_ACCOUNT_MONEY_TYPE.equals(dedicatedAccountType.getValue().toString()) && (null !=dalist ? dalist.contains(dedicatedAccountID.getValue().toString()) : true)) {
				allSubscriberDAs.put(dedicatedAccountID.getValue().toString(), dedicatedAccountValue.getValue().toString());
			}
		}	
		return allSubscriberDAs;
	}
	
	/**
	 * Get all DA list applicable for charging.
	 * 
	 * @param accounts
	 * @return
	 */
	public static List<String> getChargingDAList(final List<Account> accounts) {
		final List<String> list = new ArrayList<String>();
		for(final Account account : accounts) {
			if(account instanceof DedicatedAccount) {
				list.add(((DedicatedAccount) account).getDedicatedAccountId());
			}
		}
		return list;
	}

	/**
	 * This method returns a list of eligible charging for the current request
	 * @param productChargings
	 * @param fdpRequest
	 * @return
	 * @throws StepException 
	 */
	public static List<ProductCharging> getEligibleChargings(final List<ProductCharging> productChargings, final FDPRequest fdpRequest, final Logger circleLogger) throws StepException {
		final List<ProductCharging> eligibleChargings =  new ArrayList<ProductCharging>();
		boolean isVariableType = false;
		if(null != productChargings) {
			for(final ProductCharging productCharging : productChargings) {
				if(productCharging instanceof VariableCharging) {
					isVariableType = true;
					break;
				}
				if((fdpRequest.getExternalSystemToCharge().equals(productCharging.getExternalSystem()) || ExternalSystem.RS.equals(productCharging.getExternalSystem())
						/*|| ExternalSystem.AIR.equals(productCharging.getExternalSystem())*/)) {
					eligibleChargings.add(productCharging);
				}
			}
		}
		if(!isVariableType && !isChargingValid(eligibleChargings, fdpRequest)){
			FDPLogger.error(circleLogger, ChargingUtil.class, "getEligibleChargings()", LoggerUtil.getRequestAppender(fdpRequest) + "RSN"
					+ FDPConstant.LOGGER_KEY_VALUE_DELIMITER + "Product charging not defined for charging type : " + fdpRequest.getExternalSystemToCharge().toString());
			throw new StepException("Product charging not defined for charging type : " + fdpRequest.getExternalSystemToCharge().toString());
		}
		return !isVariableType ? eligibleChargings : productChargings;
	}
	
	/**
	 * This method updates the fdpRequest for externalSystemToCharge in case no payment source is defined for fixed Charging
	 * @param fdpRequest
	 */
	public static void updateRequestForDefaultChargingFixed(FDPRequest fdpRequest, final List<ProductCharging> productChargings) {
		if(fdpRequest.getExternalSystemToCharge() == null){
			boolean isVariableType = false;
			for(final ProductCharging productCharging : productChargings) {
				if(productCharging instanceof VariableCharging) {
					isVariableType = true;
					break;
				}
			}
			if(!isVariableType){
					if (fdpRequest.getExternalSystemToCharge() == null) {
						final FDPCacheable fdpCacheable = fdpRequest.getValueFromRequest(RequestMetaValuesKey.PRODUCT);
						if (fdpCacheable instanceof Product) {
							final Product product = (Product) fdpCacheable;
							String defaultCharging = product.getAdditionalInfo(ProductAdditionalInfoEnum.DEFAULT_CHARGING);
							
							if(null != defaultCharging){
					            if(defaultCharging.equals((ExternalSystemType.AIR_TYPE.getKey()).toString())){
					            	((FDPRequestImpl)fdpRequest).setExternalSystemToCharge(ExternalSystem.AIR);
					            }else if(defaultCharging.equals((ExternalSystemType.LOYALTY_TYPE.getKey()).toString())){
					            	((FDPRequestImpl)fdpRequest).setExternalSystemToCharge(ExternalSystem.Loyalty);
					            }else if(defaultCharging.equals((ExternalSystemType.MOBILEMONEY_TYPE.getKey()).toString())){
					            	((FDPRequestImpl)fdpRequest).setExternalSystemToCharge(ExternalSystem.MM);
					            }else if(defaultCharging.equals((ExternalSystemType.EVDS_TYPE.getKey()).toString())){
					            	((FDPRequestImpl)fdpRequest).setExternalSystemToCharge(ExternalSystem.EVDS);
					            }
							}else{
				            	((FDPRequestImpl)fdpRequest).setExternalSystemToCharge(ExternalSystem.AIR);
							}
						}
				}
			}
		}
	}

	/**
	 * This method will check if product charging is defined for selected payment method
	 * @param productChargings
	 * @param fdpRequest
	 * @return
	 */
	private static boolean isChargingValid(final List<ProductCharging> productChargings, final FDPRequest fdpRequest){
		boolean isChargingValid = false;
		for(final ProductCharging productCharging : productChargings) {
			if(fdpRequest.getExternalSystemToCharge().equals(productCharging.getExternalSystem())){
				isChargingValid = true;
				break;
			}	
		}
		return isChargingValid;
	}
	
	/**
	 * This method update the applicable chanrging in case of split buy
	 * applicableCharging = (no. of split) * (product charging amt)
	 * 
	 * @param fdpRequest
	 * @param applicableCharging
	 * @return
	 * @throws ExecutionFailedException 
	 */
	public static Long updateChargingIfSplit(FDPRequest fdpRequest,
			Long applicableCharging) throws ExecutionFailedException {
		final FDPCacheable fdpCacheable = fdpRequest.getValueFromRequest(RequestMetaValuesKey.PRODUCT);
		if (fdpCacheable instanceof Product) {
			final Product product = (Product) fdpCacheable;
			boolean isSplitDone = false;
			if(null != product.getAdditionalInfo(ProductAdditionalInfoEnum.IS_SPLIT)){
				Boolean isSplit =  Boolean.parseBoolean(product.getAdditionalInfo(ProductAdditionalInfoEnum.IS_SPLIT));
				isSplitDone = (boolean) ((null!=fdpRequest.getAuxiliaryRequestParameter(AuxRequestParam.IS_SPLIT_DONE))?(fdpRequest.getAuxiliaryRequestParameter(AuxRequestParam.IS_SPLIT_DONE)):false);
				if(isSplit){
					if(!isSplitDone){
						Integer numOfSplit = product.getRenewalCount();
						if(null != fdpRequest.getAuxiliaryRequestParameter(AuxRequestParam.SPLIT_NUMBER)){
							numOfSplit = Integer.parseInt(fdpRequest.getAuxiliaryRequestParameter(AuxRequestParam.SPLIT_NUMBER).toString());
						}
						applicableCharging *= numOfSplit;
					    FDPRequestImpl fdpRequestImpl= (FDPRequestImpl)fdpRequest;
						fdpRequestImpl.putAuxiliaryRequestParameter(AuxRequestParam.IS_SPLIT_DONE, true);
					}
				}else{
					if(null != fdpRequest.getAuxiliaryRequestParameter(AuxRequestParam.SPLIT_NUMBER)){
						throw new ExecutionFailedException("Invalid Product buy. Product is not of split type.");
					}
				}
			}
		}
		return applicableCharging;
	}
	
	/**
	 * This method will return a list of chargings that are applicable for a specific subscriber
	 * In case of variable charging, the chargings associated with satisfied condition is returned
	 * 
	 * @param fdpRequest
	 * @return
	 * @throws ExecutionFailedException
	 */
	public static Long getApplicableProductChargingForProvisioning(final FDPRequest fdpRequest) throws ExecutionFailedException{
		Long applicableCharging = 0L;
		/* If product cost parameter is set, then override the product charging value */
		Object productCost = fdpRequest.getAuxiliaryRequestParameter(AuxRequestParam.PRODUCT_COST);
		if(productCost != null){
			applicableCharging = Long.valueOf(productCost.toString());
		}else{
			final FDPCacheable fdpCacheable = fdpRequest.getValueFromRequest(RequestMetaValuesKey.PRODUCT);
			if (fdpCacheable instanceof Product) {
				final Product product = (Product) fdpCacheable;
				List<ProductCharging> applicableProductChargings = product.getProductCharging(fdpRequest.getChannel(), ChargingType.NORMAL);
				for(ProductCharging productCharging : applicableProductChargings){
					if(productCharging instanceof VariableCharging){
						final VariableCharging variableCharging = (VariableCharging) productCharging;
						final VariableCharging newVariableCharging = new VariableCharging(variableCharging.getConditionStep(), variableCharging.getCommandDisplayName(), variableCharging.getExternalSystem());
						applicableProductChargings = newVariableCharging.getApplicableChargings(fdpRequest);
						break;
					}
				}
				for(ProductCharging productCharging : applicableProductChargings){
					if(!DABasedAirCharging.class.isInstance(productCharging) && ExternalSystem.AIR.equals(productCharging.getExternalSystem())){
						applicableCharging = (Long) TransformationUtil.evaluateTransformation(Long.valueOf(((FixedCharging)productCharging).getChargingValue().getChargingValue().toString()),
								ParamTransformationType.NEGATIVE);
					}else if(ExternalSystem.MM.equals(productCharging.getExternalSystem()) || ExternalSystem.Loyalty.equals(productCharging.getExternalSystem())
							|| ExternalSystem.EVDS.equals(productCharging.getExternalSystem())){
						applicableCharging = 0L;
						break;
					}
				}
			}
		}
		applicableCharging = (applicableCharging == 0L) ? applicableCharging : getDiscountedChargingValue(fdpRequest, applicableCharging);
		return applicableCharging;
	}
	

	/**
	 * This method return the discounted value, if discount is applicable
	 * @param fdpRequest
	 * @return
	 * @throws ExecutionFailedException
	 */
	public static Long getDiscountedChargingValue(final FDPRequest fdpRequest, Long chargingValue) throws ExecutionFailedException{
		try {
			Discount chargingDiscount = ChargingUtil.getDiscountFromCache(fdpRequest);
			if(null != chargingDiscount && chargingDiscount.isDiscountApplicable(fdpRequest)){
				chargingValue = chargingDiscount.calculateDiscount(chargingValue);
			}			
		} catch (NumberFormatException e) {
			e.printStackTrace();
			throw new ExecutionFailedException("Error Number Format Exception while calculating discount", e);
		} catch (ExpressionFailedException e) {
			e.printStackTrace();
			throw new ExecutionFailedException("Error ExpressionFailedException while calculating discount", e);
		}
		return chargingValue;
	}
	
	/**
	 * Method gets the discount from cache.
	 * 
	 * @param fdpRequest
	 * @param productId
	 * @return
	 * @throws ExecutionFailedException
	 */
	public static Discount getDiscountFromCache(final FDPRequest fdpRequest) throws ExecutionFailedException {
		final Product product = (Product) fdpRequest.getValueFromRequest(RequestMetaValuesKey.PRODUCT);
		Discount discount = null;
		if(null != product) {
			final Long productId = product.getProductId();
			final FDPCacheable fdpDiscountCached = ApplicationConfigUtil.getMetaDataCache().getValue(
					new FDPMetaBag(fdpRequest.getCircle(), ModuleType.PRODUCT_CHARGING_DISCOUNT, productId));
			if (fdpDiscountCached instanceof Discount) {
				discount = (Discount) fdpDiscountCached;
			}
		}
		return discount;
	}

	public static Long updateChargingAmountChannelWise() {
		// TODO Auto-generated method stub
		return null;
	}

	public static Long updateChargingAmountChannelWise(FDPRequest fdpRequest,
			Long applicableCharging) {
		if(fdpRequest instanceof FulfillmentRequestImpl)
		{
			String iname=((FulfillmentRequestImpl)fdpRequest).getIname();
		}
		return applicableCharging;
	}


}
