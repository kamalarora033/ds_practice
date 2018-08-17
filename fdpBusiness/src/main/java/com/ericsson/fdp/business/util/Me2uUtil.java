package com.ericsson.fdp.business.util;

import java.util.Map;

import com.ericsson.fdp.FDPCacheable;
import com.ericsson.fdp.business.enums.AuxRequestParam;
import com.ericsson.fdp.business.enums.Command;
import com.ericsson.fdp.business.enums.ConfigurationKey;
import com.ericsson.fdp.business.enums.RequestMetaValuesKey;
import com.ericsson.fdp.business.product.Product;
import com.ericsson.fdp.business.product.impl.BaseProduct;
import com.ericsson.fdp.business.tts.impl.TimeToShare;
import com.ericsson.fdp.common.constants.FDPConstant;
import com.ericsson.fdp.common.enums.ModuleType;
import com.ericsson.fdp.common.enums.Primitives;
import com.ericsson.fdp.core.command.FDPCommand;
import com.ericsson.fdp.core.exception.EvaluationFailedException;
import com.ericsson.fdp.core.exception.ExecutionFailedException;
import com.ericsson.fdp.core.request.FDPRequest;
import com.ericsson.fdp.core.request.impl.FDPRequestImpl;
import com.ericsson.fdp.core.serviceprovisioning.bean.FDPMetaBag;
import com.ericsson.fdp.core.utils.ApplicationConfigUtil;
import com.ericsson.fdp.dao.enums.Me2UTransactionCharges;

public class Me2uUtil {

	/**
	 * This method will return true if all Time2share transaction limits are satisfied 
	 * namely : (oncetranslimit, dailymaxamt, monthlymaxamt, minamtafter)
	 * 
	 * @param fdpRequest
	 * @param totalAmtToTrans
	 * @param failureMsg
	 * @return
	 * @throws ExecutionFailedException 
	 * @throws EvaluationFailedException 
	 */
	public static boolean areTransAmtLimitsValid(FDPRequest fdpRequest,
			Long totalAmtToTrans) throws ExecutionFailedException, EvaluationFailedException {
		boolean isValid = true;
		CommandUtil.executeCommand(fdpRequest, Command.GET_OFFERS, false);
		Map<String, Boolean> subscriberOffers = MVELUtil.getSubscribedOffers
				(fdpRequest.getExecutedCommand(Command.GET_OFFERS.getCommandDisplayName()));
		for(String offerId : subscriberOffers.keySet()){
			FDPCacheable limitObj = ApplicationConfigUtil.getMetaDataCache().getValue(new FDPMetaBag
					(fdpRequest.getCircle(), ModuleType.TIME_TO_SHARE, offerId));
			if(null != limitObj && limitObj instanceof TimeToShare){
				String oneKwachaInNgweeObj =  getConfigurationMapValue(fdpRequest, ConfigurationKey.ONE_KWACHA_IN_NGWEE);
				// If conversion ratio is not defined in configuration, then use 1000 as default
				Long ngweesInOneKwacha = (oneKwachaInNgweeObj == null) ? 1000l : Long.parseLong(oneKwachaInNgweeObj.toString());
				TimeToShare tsLimit = (TimeToShare) limitObj;
				CommandUtil.executeCommand(fdpRequest, Command.GET_ACCUMULATORS, true);
				Map<String, String> accumulatorsMap = MVELUtil.getUsageAccumulatorsResponse
						(fdpRequest.getExecutedCommand(Command.GET_ACCUMULATORS.getCommandDisplayName()));
				isValid = (isOnceTransLimitValid(fdpRequest, totalAmtToTrans, tsLimit, ngweesInOneKwacha) 
						&& isDailyMaxLimitValid(fdpRequest, accumulatorsMap, totalAmtToTrans, tsLimit, ngweesInOneKwacha)
						&& isMonthlyMaxLimitValid(fdpRequest, accumulatorsMap, totalAmtToTrans, tsLimit, ngweesInOneKwacha)
						&& isMinAmtAfterLimitValid(fdpRequest, totalAmtToTrans, tsLimit, ngweesInOneKwacha));
				break;
			}
		}
		return isValid;
	}
	
	/**
	 * This method will check if the subscriber has minimum leftover funds in his/her accounts
	 * after time2transfer transaction
	 * 
	 * @param totalAmtToTransfer
	 * @return
	 * @throws ExecutionFailedException 
	 * @throws EvaluationFailedException 
	 * @throws  
	 */
	public static boolean isMinAmtAfterLimitValid(FDPRequest fdpRequest, Long totalAmt, TimeToShare tsLimit, Long ngweesInOneKwacha) throws ExecutionFailedException, EvaluationFailedException {
		Boolean isValid = false;
		String accountToTransFrom = fdpRequest.getAuxiliaryRequestParameter(AuxRequestParam.TIME2SHARE_ACCOUNT_TO_SHARE_FROM).toString();
		if(null != accountToTransFrom){
			CommandUtil.executeCommand(fdpRequest, Command.GET_BALANCE_AND_DATE, false);
			FDPCommand fdpCommand = fdpRequest.getExecutedCommand(Command.GET_BALANCE_AND_DATE.getCommandDisplayName());
			String balanceStr = Integer.parseInt(accountToTransFrom) == 0 ? fdpCommand.getOutputParam("accountValue1").getValue().toString()
					: (String)MVELUtil.evaluateMvelExpression(fdpRequest, "DA_" + accountToTransFrom + "_VALUE", Primitives.INTEGER);
			isValid = (totalAmt + (tsLimit.getShareBaseOfferDTO().getMinamountafter() * ngweesInOneKwacha)) <= convertCSToNgweeAmt(fdpRequest, Long.parseLong(balanceStr));
			if (!isValid) {
				updateRequestWithThresholdDetails(fdpRequest, String.valueOf(balanceStr),
						String.valueOf(
								(totalAmt + (tsLimit.getShareBaseOfferDTO().getMinamountafter() * ngweesInOneKwacha))),
						FDPConstant.THREASHOLD_NOTIFICATION_PREFIX - 4);
			}
		}
		return isValid;
	}
	
	/**
	 * This method will return true if monthly max transaction limit of subscriber is not reached otherwise false
	 * @param fdpRequest
	 * @param tsLimit
	 * @return
	 */
	public static boolean isMonthlyMaxLimitValid(FDPRequest fdpRequest, Map<String, String> accumulatorsMap, Long totalAmtToTrans, TimeToShare tsLimit, Long ngweesInOneKwacha) {
		Boolean isValid = true;
		String monthMaxUAId = getConfigurationMapValue(fdpRequest, ConfigurationKey.TIME2SHARE_MONTHLY_MAX_UA_ID);
		if(null != monthMaxUAId){
			Long currentMonthlyUAValue = (null != accumulatorsMap && null != accumulatorsMap.get(monthMaxUAId)) ?
					convertCSToNgweeAmt(fdpRequest, Long.parseLong(accumulatorsMap.get(monthMaxUAId).toString())) : 0l;		
		    isValid	= (currentMonthlyUAValue + totalAmtToTrans) < (tsLimit.getShareBaseOfferDTO().getMonthmaxamount() * ngweesInOneKwacha) ? true : false;	
			if (!isValid) {
				updateRequestWithThresholdDetails(fdpRequest, String.valueOf(currentMonthlyUAValue + totalAmtToTrans),
						String.valueOf(tsLimit.getShareBaseOfferDTO().getMonthmaxamount() * ngweesInOneKwacha),
						FDPConstant.THREASHOLD_NOTIFICATION_PREFIX - 3);
			}
		}
		return isValid;
	}

	/**
	 * This method will return true if daily max transaction limit of subscriber is not reached otherwise false
	 * @param fdpRequest
	 * @param tsLimit
	 * @return
	 */
	public static boolean isDailyMaxLimitValid(FDPRequest fdpRequest, Map<String, String> accumulatorsMap, Long totalAmtToTrans, TimeToShare tsLimit, Long ngweesInOneKwacha) {
		Boolean isValid = true;
		String dailyMaxUAId = getConfigurationMapValue(fdpRequest, ConfigurationKey.TIME2SHARE_DAILY_MAX_UA_ID);
		if(null != dailyMaxUAId){
			Long currentDailyUAValue = (null != accumulatorsMap && null != accumulatorsMap.get(dailyMaxUAId)) ?
					convertCSToNgweeAmt(fdpRequest, Long.parseLong(accumulatorsMap.get(dailyMaxUAId).toString())) : 0l;		
		    isValid	= (currentDailyUAValue + totalAmtToTrans) < (tsLimit.getShareBaseOfferDTO().getDailymaxamount() * ngweesInOneKwacha) ? true : false;		
			if (!isValid) {
				updateRequestWithThresholdDetails(fdpRequest, String.valueOf(currentDailyUAValue + totalAmtToTrans),
						String.valueOf(tsLimit.getShareBaseOfferDTO().getDailymaxamount() * ngweesInOneKwacha),
						FDPConstant.THREASHOLD_NOTIFICATION_PREFIX - 2);
			}
		}
		return isValid;
	}

	/**
	 * This method will return true in case once transaction limit is satisfied
	 * and false otherwise.
	 * @param tsLimit 
	 * @param totalAmtToTrans 
	 * 
	 * @return 
	 */
	public static boolean isOnceTransLimitValid(final FDPRequest fdpRequest, Long totalAmtToTrans, TimeToShare tsLimit, Long ngweesInOneKwacha){
		boolean value = totalAmtToTrans <= (tsLimit.getShareBaseOfferDTO().getOncetransmaxamount() * ngweesInOneKwacha);
		if (!value) {
			updateRequestWithThresholdDetails(fdpRequest, String.valueOf(totalAmtToTrans),
					String.valueOf(tsLimit.getShareBaseOfferDTO().getOncetransmaxamount() * ngweesInOneKwacha),
					FDPConstant.THREASHOLD_NOTIFICATION_PREFIX - 1);
		}
		return value;
	}
	
	/**
	 * This method will return the value of input configuration key as defined in fdpCircle
	 * @return
	 */
	public static String getConfigurationMapValue(FDPRequest fdpRequest, ConfigurationKey key){
		final Map<String, String> configurationMap = fdpRequest.getCircle().getConfigurationKeyValueMap();
		return configurationMap.get(key.getAttributeName());		
	}
	
	/**
	   * This method returns the amount of transaction charges applicable for Me2u
	   * @param fdpRequest
	   * @return
	   * @throws ExecutionFailedException
	   */
	public static Long getTransCharges(final FDPRequest fdpRequest, Long amtToTransfer) throws ExecutionFailedException{
			Long transCharges = 0l;
		    final Product product = (Product) fdpRequest.getValueFromRequest(RequestMetaValuesKey.PRODUCT);
			if(null != product && product instanceof BaseProduct) {
				final BaseProduct bp = (BaseProduct) product;
				String transType = bp.getChargesTypes();
				String transChargesStr = bp.getCharges();
				Long amtToTransMod = (amtToTransfer < 0L) ? (-1) * amtToTransfer : amtToTransfer;
				if(null != transType && null != transChargesStr){
					if(transType.contentEquals(Me2UTransactionCharges.FIXED_DISCOUNT.getDisplayName())){
						transCharges = Long.parseLong(transChargesStr);
					} else if(transType.contentEquals(Me2UTransactionCharges.PERCENTAGE_DISCOUNT.getDisplayName())){
						transCharges = (long)((double)(Long.parseLong(transChargesStr) * amtToTransMod) * 0.01);
					}
				}
				((FDPRequestImpl)fdpRequest).putAuxiliaryRequestParameter(AuxRequestParam.ME2U_TRANS_CHARGES, getAmtNgweeTokwacha(fdpRequest, transCharges));
			}
			return transCharges;
	  }
	
	/**
	 * This method will populate the notification information in request.
	 * 
	 * @param fdpRequest
	 * @param userThresholdValue
	 * @param thresholdVale
	 * @param notificationId
	 */
	private static void updateRequestWithThresholdDetails(final FDPRequest fdpRequest, final String userThresholdValue,
			final String thresholdVale, final Long notificationId) {
		((FDPRequestImpl) fdpRequest).putAuxiliaryRequestParameter(AuxRequestParam.NOTIFICATION_TO_OVERRIDE,
				notificationId);
		((FDPRequestImpl) fdpRequest).putAuxiliaryRequestParameter(AuxRequestParam.USER_THRESHHOLD_VALUE,
				userThresholdValue);
		((FDPRequestImpl) fdpRequest).putAuxiliaryRequestParameter(AuxRequestParam.THRESHOLD_VALUE, thresholdVale);
	}
	
	/**
	 * This method will return the conversion factor for Ngwee to Kwachaa as defined in configuration
	 * @param fdpRequest
	 * @return
	 */
	public static Long getNgweeInOneKwachaa(final FDPRequest fdpRequest){
		// Get the Kwacha to Ngwee conversion ratio
		String oneKwachaInNgweeObj =  getConfigurationMapValue(fdpRequest, ConfigurationKey.ONE_KWACHA_IN_NGWEE);
		// If conversion ratio is not defined in configuration, then use 1000 as default
		return (oneKwachaInNgweeObj == null) ? 1000l : Long.parseLong(oneKwachaInNgweeObj.toString());
	}
	
	/**
	 * This method will convert the input amount in Ngwees to Kwachaa
	 * @param fdpRequest
	 * @param amtInNgwee
	 * @return
	 */
	public static Double getAmtNgweeTokwacha(final FDPRequest fdpRequest, Long amtInNgwee){
		Double NgweeInOneKwachaa = Double.parseDouble(getNgweeInOneKwachaa(fdpRequest).toString());
		return amtInNgwee/NgweeInOneKwachaa;
	}
	
	/**
	 * This method returns the once max trans time2share limit for user
	 * @param fdpRequest
	 * @throws ExecutionFailedException 
	 */
	public static Integer getOnceMaxTransLimit(final FDPRequest fdpRequest) throws ExecutionFailedException{
		CommandUtil.executeCommand(fdpRequest, Command.GET_OFFERS, true);
		Map<String, Boolean> subscriberOffers = MVELUtil.getSubscribedOffers
				(fdpRequest.getExecutedCommand(Command.GET_OFFERS.getCommandDisplayName()));
		for(String offerId : subscriberOffers.keySet()){
			FDPCacheable limitObj = ApplicationConfigUtil.getMetaDataCache().getValue(new FDPMetaBag(fdpRequest.getCircle(), ModuleType.TIME_TO_SHARE, offerId));
			if(null != limitObj && limitObj instanceof TimeToShare){
				TimeToShare tsLimit = (TimeToShare) limitObj;
				return tsLimit.getShareBaseOfferDTO().getOncetransmaxamount();
			}
		}
		throw new ExecutionFailedException("No Base offer found for time2share");
	}
	
	/**
	 * This method converts the ngwee amount to CS amount
	 * @param fdpRequest
	 * @param value
	 * @return
	 */
	public static Long convertNgweeToCSAmt(final FDPRequest fdpRequest, final Object value) {
		if(null != value){
			Long amt = Long.parseLong(value.toString());
			String oneNgeeInCsAmtObj =  getConfigurationMapValue(fdpRequest, ConfigurationKey.NEGWEE_CS_FACTOR);
			return (oneNgeeInCsAmtObj == null) ? amt : Long.parseLong(oneNgeeInCsAmtObj) * amt;
		}
		return null;
	}
	
	/**
	 * This method converts the CS amount to Ngwee amount
	 * @param fdpRequest
	 * @param value
	 * @return
	 */
	public static Long convertCSToNgweeAmt(final FDPRequest fdpRequest, final Object value) {
		if(null != value){
			Long amt = Long.parseLong(value.toString());
			String oneNgeeInCsAmtObj =  getConfigurationMapValue(fdpRequest, ConfigurationKey.NEGWEE_CS_FACTOR);
			return (oneNgeeInCsAmtObj == null) ? amt : amt/Long.parseLong(oneNgeeInCsAmtObj);
		}
		return null;
	}
	
	/**
	 * This method is check Subscriber and beneficiary msisdn both are same or not.
	 * 
	 * @param beneficiaryMsisdn
	 * @param fdpRequest
	 * @return
	 * @throws ExecutionFailedException 
	 */
	public static boolean isSubscriberMsisdnSameAsBeneficiary(FDPRequest fdpRequest, String beneficiaryMsisdn) throws ExecutionFailedException{
		final String validBeneficiaryMsisdn = RequestUtil.validateBeneficiaryMsisdn(fdpRequest, beneficiaryMsisdn);
		final String subscriberMsisdn = fdpRequest.getSubscriberNumber().toString();

		return validBeneficiaryMsisdn.equals(subscriberMsisdn) ? true : false;
		
	}
}