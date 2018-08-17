package com.ericsson.fdp.business.util;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ericsson.fdp.FDPCacheable;
import com.ericsson.fdp.business.enums.AuxRequestParam;
import com.ericsson.fdp.business.enums.Command;
import com.ericsson.fdp.business.enums.ConfigurationKey;
import com.ericsson.fdp.business.enums.RequestMetaValuesKey;
import com.ericsson.fdp.business.fnf.impl.FnFOffer;
import com.ericsson.fdp.business.node.impl.ProductNode;
import com.ericsson.fdp.business.product.Product;
import com.ericsson.fdp.common.constants.FDPConstant;
import com.ericsson.fdp.common.enums.ModuleType;
import com.ericsson.fdp.common.util.StringUtil;
import com.ericsson.fdp.common.vo.FDPCircle;
import com.ericsson.fdp.core.command.FDPCommand;
import com.ericsson.fdp.core.config.utils.PropertyUtils;
import com.ericsson.fdp.core.dsm.framework.service.FDPCache;
import com.ericsson.fdp.core.exception.ExecutionFailedException;
import com.ericsson.fdp.core.exception.FafFailedException;
import com.ericsson.fdp.core.node.FDPNode;
import com.ericsson.fdp.core.request.FDPRequest;
import com.ericsson.fdp.core.request.impl.FDPRequestImpl;
import com.ericsson.fdp.core.serviceprovisioning.bean.FDPMetaBag;
import com.ericsson.fdp.core.utils.ApplicationConfigUtil;
import com.ericsson.fdp.dao.dto.faf.ServiceClassOffer;
import com.ericsson.fdp.dao.dto.sharebaseoffer.FDPFnFOfferDetailsDTO;
import com.ericsson.fdp.dao.enums.FDPServiceProvSubType;

/**
 * This is the utility class for friends and family feature.
 * 
 * @author evasaty
 * 
 */
public class FnfUtil {

	/** The Constant LOGGER. */
	private static final Logger LOGGER = LoggerFactory.getLogger(FnfUtil.class);
	private static final String COUNTRY_CODE = PropertyUtils.getProperty("COUNTRY_CODE");
	private static final Integer MSISDN_LENGTH = Integer.parseInt(PropertyUtils.getProperty("fdp.msisdn.length"));
	private static final Integer COUNTRY_CODE_LENGTH = COUNTRY_CODE.length();
	private static int flag = 0;

	/**
	 * This method is used to get friends and family list of the subscriber from charging system.
	 * @param fdpRequest
	 * @param fdpResponse
	 * @return  
	 * @throws ExecutionFailedException
	 */
	public static Map<Integer, String> getFriendsAndFamilyList(FDPRequest fdpRequest) throws ExecutionFailedException {
		return evaluateFafList(executeCommand(fdpRequest, Command.GET_FAF_LIST), fdpRequest);
	}

	/**
	 * This method is use to return FAFNumber list from getFafList response. 
	 * Appends International prefix for international numbers.
	 * @param executedCommand
	 * @return
	 * 
	 */
	private static Map<Integer, String> evaluateFafList(final FDPCommand executedCommand, FDPRequest fdpRequest) {
		String pathKey;
		int i = 0;
		final Map<String, String> configurationMap = fdpRequest.getCircle().getConfigurationKeyValueMap();
		
		final String parameterName = "fafInformationList";
		final String fafNumber = "fafNumber";
		final int fafIndicatorInternational = Integer.parseInt(configurationMap.get(
				ConfigurationKey.FAF_INDICATOR_INTERNATIONAL.getAttributeName()));
		final Map<Integer, String> fafNumberMap = new HashMap<>();
		if (null != executedCommand) {
			while (null != executedCommand.getOutputParam(pathKey = (parameterName + FDPConstant.PARAMETER_SEPARATOR + i
					+ FDPConstant.PARAMETER_SEPARATOR + fafNumber))) {
				if(null != executedCommand.getOutputParam(pathKey).getValue()){
					final String fafmsisdn = executedCommand.getOutputParam(pathKey).getValue().toString();
					if(fafIndicatorInternational == getIndicator(executedCommand, parameterName, i)){
						fafNumberMap.put(i + 1, FDPConstant.FAF_INTERNATIONAL_PREFIX+fafmsisdn.trim());
					} else {
						fafNumberMap.put(i + 1, fafmsisdn);
					}
					i++;
				}else{
					return fafNumberMap;
				}
				
			}
		}
		return fafNumberMap;
	}

	/**
	 * Returns Faf Indicator
	 * @param executedCommand
	 * @param parameterName
	 * @param i
	 * @return
	 */
	private static int getIndicator(FDPCommand executedCommand, String parameterName, int i) {
		String pathKey = null;

		final String fafIndicator = "fafIndicator";
		if (null != executedCommand.getOutputParam(pathKey = (parameterName + FDPConstant.PARAMETER_SEPARATOR + i
				+ FDPConstant.PARAMETER_SEPARATOR + fafIndicator))) {
			if (null != executedCommand.getOutputParam(pathKey).getValue()) {
				return Integer.parseInt(executedCommand.getOutputParam(pathKey).getValue().toString());
				}
		}
		return 0;
	}

	/**
	 * returns true if the number entered by subscriber is not exceeding the
	 * onnet, offnet, international limit defined in CIS.
	 * 
	 * @param fdpRequest
	 * @param fafNumber
	 * @return
	 * @throws ExecutionFailedException
	 */
	public Boolean isAddFafNumberAllowed(final FDPRequest fdpRequest, String fafNumber) throws FafFailedException,
			ExecutionFailedException {
		int limit = -1;
		int fafIndicator = 0;
		final Map<String, String> configurationMap = fdpRequest.getCircle().getConfigurationKeyValueMap();
		Product product = getProduct(fdpRequest);
		Long subscriberNumber = null;
		if (product != null) {
			if (fafNumber.startsWith(FDPConstant.FAF_INTERNATIONAL_PREFIX)) {
				fafIndicator = Integer.parseInt(configurationMap.get(
						ConfigurationKey.FAF_INDICATOR_INTERNATIONAL.getAttributeName()).toString());
				// limit = product.getInternational();
			} else {
				try {
					fafNumber = FnfUtil.normalizeFafMsisdnForCommand(fafNumber).toString();
					subscriberNumber = fdpRequest.getSubscriberNumber();
					((FDPRequestImpl) fdpRequest).setSubscriberNumber(Long.parseLong(fafNumber));
					FDPCommand fdpCommand = executeCommand(fdpRequest, Command.GETACCOUNTDETAILS);
					((FDPRequestImpl) fdpRequest).setSubscriberNumber(subscriberNumber);
					if (fdpCommand.getResponseError().getResponseCode().equals(FDPConstant.GAD_SUCCESS_RESPONSE102)) {
						fafIndicator = Integer.parseInt(configurationMap.get(
								ConfigurationKey.FAF_INDICATOR_OFFNET.getAttributeName()).toString());
						// limit = product.getOffNet();
					} else if (Integer.parseInt(fdpCommand.getResponseError().getResponseCode()) < 3) {
						fafIndicator = Integer.parseInt(configurationMap.get(
								ConfigurationKey.FAF_INDICATOR_ONNET.getAttributeName()).toString());
						// limit = product.getOnNet();
					} else {
						LOGGER.info("Msisdn is not found in system :: msmsidn : " + fafNumber);
						throw new FafFailedException("Msisdn is not exist in system");
					}
				} catch (ExecutionFailedException | FafFailedException e) {
					LOGGER.error("Msisdn is not found in system :: fafNumber : " + fafNumber);
					throw new FafFailedException("Msisdn is not exist in system");
				}
			}
		}

		return isFaFNumberAllowed(fdpRequest, fafIndicator, limit);
	}

	private Boolean isFaFNumberAllowed(FDPRequest fdpRequest, Integer fafIndicator, int limit)
			throws FafFailedException {
		Map<Integer, Integer> fafListMap = new HashMap<>();
		try {
			fafListMap = evaluateFafIndicator(executeCommand(fdpRequest, Command.GET_FAF_LIST));
		} catch (ExecutionFailedException e) {
			LOGGER.error("Exception in isFaFNumberAllowed for evaluateFafIndicator " + e.getMessage());
			throw new FafFailedException("Entered number is not evaluated.");
		}
		if (null != fafListMap.get(fafIndicator)) {
			// if deleteFafMsisdn and addFafMsisdn have same fafIndicator.
			if (flag == 1) {
				flag = 0;
				if (fafListMap.get(fafIndicator) > limit) {
					return false;
				}
			}
			if (fafListMap.get(fafIndicator) >= limit) {
				return false;
			}
		}
		return true;
	}

	/**
	 * Returns true if the number entered by subscriber, for modification, lies
	 * in the onnet, offnet, international limit defined in CIS.
	 * 
	 * @param fdpRequest
	 * @param fafNumberAdd
	 * @param fafNumberDelete
	 * @return
	 * @throws ExecutionFailedException 
	 * @throws FafFailedException 
	 */
	public boolean isFafModifyAllowed(FDPRequest fdpRequest, String fafNumberAdd, String fafNumberDelete) throws FafFailedException, ExecutionFailedException {
		if (isAddFafNumberAllowed(fdpRequest, fafNumberAdd)) {
			return true;
		} else {
			flag = 1;
			return isAddFafNumberAllowed(fdpRequest, fafNumberDelete);
		}
	}

	/**
	 * Evaluates faf Indicator from getFafList response. returns a Map with
	 * fafIndicator as key, and number of faf members corresponding to every
	 * indicator as value.
	 * 
	 * @param executedCommand
	 * @return
	 */
	private static Map<Integer, Integer> evaluateFafIndicator(FDPCommand executedCommand) {
		String pathKey = null;
		int i = 0;
		final String parameterName = "fafInformationList";
		final String fafNumber = "fafIndicator";
		Map<Integer, Integer> fafIndicatorMap = new HashMap<>();
		if (null != executedCommand) {
			while (null != executedCommand.getOutputParam(pathKey = (parameterName + FDPConstant.PARAMETER_SEPARATOR + i
					+ FDPConstant.PARAMETER_SEPARATOR + fafNumber))) {
				if(null != executedCommand.getOutputParam(pathKey).getValue()){
					final Integer fafIndicator = Integer.parseInt(executedCommand.getOutputParam(pathKey).getValue()
							.toString());
					fafIndicatorMap = getFaFIndicatorMap(fafIndicatorMap, fafIndicator);
					i++;
				}else{
					return fafIndicatorMap;
				}
				
			}
		}
		return fafIndicatorMap;
	}

	/**
	 * Returns fafIndicatorMap
	 * 
	 * @param fafIndicatorMap
	 * @param fafIndicator
	 * @return
	 */
	private static Map<Integer, Integer> getFaFIndicatorMap(Map<Integer, Integer> fafIndicatorMap, Integer fafIndicator) {
		if (fafIndicatorMap.get(fafIndicator) != null) {
			int counter = fafIndicatorMap.get(fafIndicator);
			fafIndicatorMap.put(fafIndicator, counter + 1);
			counter = 0;
		} else {
			fafIndicatorMap.put(fafIndicator, 1);
		}
		return fafIndicatorMap;
	}

	/**
	 * This method executes Get Account Details command and returns the response
	 * code.
	 * 
	 * @param fdpRequest
	 * @return
	 * @throws ExecutionFailedException
	 */
	private static FDPCommand executeCommand(final FDPRequest fdpRequest, final Command command)
			throws ExecutionFailedException {
		FDPCommand fdpCommand = null;
		String commandName = command.getCommandDisplayName();
		LOGGER.info("commandDisplayName ::" + commandName);
		FDPCircle fdpCircle = fdpRequest.getCircle();
		final FDPCacheable fdpCommandCached = ApplicationConfigUtil.getMetaDataCache().getValue(
				new FDPMetaBag(fdpCircle, ModuleType.COMMAND, commandName));
		if (fdpCommandCached != null && fdpCommandCached instanceof FDPCommand) {
			fdpCommand = (FDPCommand) fdpCommandCached;
			fdpCommand.execute(fdpRequest);
			fdpRequest.addExecutedCommand(fdpCommand);
		}
		LOGGER.info("getAccountDetails response code::" + fdpCommand.getResponseError().getResponseCode());
		return fdpCommand;
	}

	/**
	 * This method is used to validate FAF no length. For Onnet, Offnet check
	 * for length & country code if entered. For international check for prefix
	 * "00", msisdn length should be 10 to 15. country code should not same.
	 * 
	 * @param fafMsisdn
	 * @return Boolean
	 */
	public static Boolean ValidateFafMsisdn(final String fafMsisdn) {

		if (fafMsisdn.startsWith(FDPConstant.FAF_INTERNATIONAL_PREFIX)
				&& (fafMsisdn.length() > FDPConstant.FAF_MIN_LENGTH && fafMsisdn.length() < FDPConstant.FAF_MAX_LENGTH)) {
			String internationalmsisdn = fafMsisdn.substring(2);
			if (internationalmsisdn.startsWith(COUNTRY_CODE)) {
				return false;
			} else {
				return true;
			}
		} else if (MSISDN_LENGTH == fafMsisdn.length()) {
			return true;
		} else if ((fafMsisdn.length() == (MSISDN_LENGTH + COUNTRY_CODE_LENGTH)) && fafMsisdn.startsWith(COUNTRY_CODE)) {
			return true;
		} else {
			LOGGER.info("FafMsisdn length is not valid FafMsisdn = " + fafMsisdn);
			return false;
		}
	}
	


	/**
	 * Returns product.
	 * 
	 * @param fdpRequest
	 * @return
	 */
	public static Product getProduct(final FDPRequest fdpRequest) {
		final FDPCacheable fdpCacheable = fdpRequest.getValueFromRequest(RequestMetaValuesKey.PRODUCT);
		if (fdpCacheable instanceof Product) {
			return (Product) fdpCacheable;
		} else {
			return null;
		}
	}

	/**
	 * This method checks if requested node is of Friends and family type only.
	 * 
	 * @param fdpRequest
	 * @return
	 */
	public static boolean isProductSpTypeValid(FDPRequest fdpRequest) {
		boolean isValid = false;
		try {
			if (fdpRequest.getValueFromRequest(RequestMetaValuesKey.NODE) instanceof FDPNode) {
				final FDPNode fdpNode = (FDPNode) fdpRequest.getValueFromRequest(RequestMetaValuesKey.NODE);
				if (fdpNode instanceof ProductNode) {
					final ProductNode productNode = (ProductNode) fdpNode;
					isValid = FDPServiceProvSubType.FAF_ADD.equals(productNode.getServiceProvSubType())
							|| FDPServiceProvSubType.FAF_DELETE.equals(productNode.getServiceProvSubType())
							|| FDPServiceProvSubType.FAF_MODIFY.equals(productNode.getServiceProvSubType())
							|| FDPServiceProvSubType.FAF_VIEW.equals(productNode.getServiceProvSubType())
							|| FDPServiceProvSubType.FAF_REGISTER.equals(productNode.getServiceProvSubType())
							|| FDPServiceProvSubType.FAF_UNREGISTER.equals(productNode.getServiceProvSubType());
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return isValid;
	}

	/**
	 * Normalize msisdn remove CC for onnet, offnet.
	 * 
	 * @param fafMsisdn
	 * @return
	 */
	public static Object normalizeFafMsisdnForCommand(String fafMsisdn) {
		Object normalizeMsisdn = null;
		if (MSISDN_LENGTH == fafMsisdn.length()) {
			normalizeMsisdn = COUNTRY_CODE + fafMsisdn;
		} else if ((MSISDN_LENGTH + COUNTRY_CODE_LENGTH) == fafMsisdn.length()) {
			normalizeMsisdn = fafMsisdn;
		} else {
			LOGGER.info("fafMsisdn length is not valid fafMsisdn: " + fafMsisdn);

		}
		return normalizeMsisdn;
	}

	/**
	 * Check subscriber is not adding same no for FAF.
	 * 
	 * @param msisdn
	 * @param fafMsisdn
	 * @return
	 */
	public static Boolean isBothMsisdnSame(String msisdn, String fafMsisdn) {
		if (!StringUtil.isNullOrEmpty(msisdn) && !StringUtil.isNullOrEmpty(fafMsisdn)) {
			if ((MSISDN_LENGTH + COUNTRY_CODE_LENGTH) == fafMsisdn.length()) {
				fafMsisdn = fafMsisdn.substring(COUNTRY_CODE_LENGTH);
			}
			if (msisdn.endsWith(fafMsisdn) || fafMsisdn.endsWith(msisdn)) {
				return true;
			} else {
				return false;
			}
		}
		return false;
	}

	/**
	 * The method is used to get subscriber serviceClassCurrent and offerIDs.
	 * 
	 * @param fdpRequest
	 * @return
	 * @throws ExecutionFailedException
	 */
	public static Map<Integer, List<Integer>> executeGetAccountDetail(final FDPRequest fdpRequest)
			throws ExecutionFailedException {
		((FDPRequestImpl)fdpRequest).putAuxiliaryRequestParameter(AuxRequestParam.SUBSCRIBER_NUMBER, fdpRequest.getSubscriberNumber());
		FDPCommand fdpCommandOutput = executeCommand(fdpRequest, Command.GETACCOUNTDETAILS);
		Integer serviceClassCurrent;
		if (null != fdpCommandOutput.getOutputParam("serviceClassCurrent").getValue()) {
			String strServiceClassCurrent = fdpCommandOutput.getOutputParam("serviceClassCurrent").getValue()
					.toString();
			serviceClassCurrent = Integer.parseInt(strServiceClassCurrent);
		}else{
			return null;
		}
		
		final String parentParameterName = "offerInformationList";
		final String childParameterName = "offerID";
		String strPathKey;
		int i = 0;
		String offerID;
		List<Integer> subscriberOfferIdList = new ArrayList<Integer>();
		//if (null != fdpCommandOutput) {
			while (null != fdpCommandOutput.getOutputParam(strPathKey = (parentParameterName
					+ FDPConstant.PARAMETER_SEPARATOR + i + FDPConstant.PARAMETER_SEPARATOR + childParameterName))) {
				if (null != fdpCommandOutput.getOutputParam(strPathKey).getValue()) {
					offerID = fdpCommandOutput.getOutputParam(strPathKey).getValue().toString();
					subscriberOfferIdList.add(Integer.parseInt(offerID));
					i++;
				}else{
					return null;
				}

			}
		//}
		Map<Integer, List<Integer>> serviceClassOfferIDs = new HashMap<Integer, List<Integer>>();
		serviceClassOfferIDs.put(serviceClassCurrent, subscriberOfferIdList);
		return serviceClassOfferIDs;
	}

	/**
	 * The method is used for sunscriber onnet, offnet, International limit
	 * details from AIR by eagarsh
	 * 
	 * @param fdpRequest
	 * @throws ExecutionFailedException
	 */
	public static Map<Integer, Integer> getSubscriberMaxLimit(final FDPRequest fdpRequest) throws ExecutionFailedException {
		FDPCommand fdpCommandOutput = executeCommand(fdpRequest, Command.GET_FAF_LIST);

		Map<Integer, Integer> fafIndicatorValueCount = evaluateFafIndicator(fdpCommandOutput);
		if (fafIndicatorValueCount != null && fafIndicatorValueCount.size() > 0) {
			return fafIndicatorValueCount;
		}
		return null;
	}

	public Integer getFaFIndicator(final FDPRequest fdpRequest, String fafNumber) throws FafFailedException,
			ExecutionFailedException {
		int fafIndicator = 0;
		final Map<String, String> configurationMap = fdpRequest.getCircle().getConfigurationKeyValueMap();
		Product product = getProduct(fdpRequest);
		Long subscriberNumber = null;
		if (product != null) {
			if (fafNumber.startsWith(FDPConstant.FAF_INTERNATIONAL_PREFIX)) {
				fafIndicator = Integer.parseInt(configurationMap.get(
						ConfigurationKey.FAF_INDICATOR_INTERNATIONAL.getAttributeName()).toString());
				// limit = product.getInternational();
			} else {
				try {
					subscriberNumber = fdpRequest.getSubscriberNumber();
					((FDPRequestImpl) fdpRequest).setSubscriberNumber(Long.parseLong(fafNumber));
					FDPCommand fdpCommand = executeCommand(fdpRequest, Command.GETACCOUNTDETAILS);
					((FDPRequestImpl) fdpRequest).setSubscriberNumber(subscriberNumber);
					if (fdpCommand.getResponseError().getResponseCode().equals(FDPConstant.GAD_SUCCESS_RESPONSE102)) {
						fafIndicator = Integer.parseInt(configurationMap.get(
								ConfigurationKey.FAF_INDICATOR_OFFNET.getAttributeName()).toString());
						// limit = product.getOffNet();
					} else if (Integer.parseInt(fdpCommand.getResponseError().getResponseCode()) < 3) {
						fafIndicator = Integer.parseInt(configurationMap.get(
								ConfigurationKey.FAF_INDICATOR_ONNET.getAttributeName()).toString());
						// limit = product.getOnNet();
					} else {
						LOGGER.info("Msisdn is not found in system :: msmsidn : " + fafNumber);
						throw new FafFailedException("Msisdn is not exist in system");
					}
				} catch (ExecutionFailedException | FafFailedException e) {
					LOGGER.error("Msisdn is not found in system :: fafNumber : " + fafNumber);
					throw new FafFailedException("Msisdn is not exist in system");
				}
			}
		}

		return fafIndicator;
	}

	public Integer isAddMoreFafNumberToAddUSSD(final FDPRequest fdpRequest, final String fafNumber)
			throws ExecutionFailedException {
		((FDPRequestImpl) fdpRequest).putAuxiliaryRequestParameter(AuxRequestParam.FAF_MSISDN_ADD, fafNumber);
		Integer isAddFafUssdResult = isAddMoreFafNumberToAdd(fdpRequest);
		return isAddFafUssdResult;
	}

	/**
	 * By eagarsh This method is use get count for onnnet, offnet, international
	 * for FAF Add
	 * 
	 * @throws FafFailedException
	 */
	public static Integer isAddMoreFafNumberToAdd(final FDPRequest fdpRequest) throws ExecutionFailedException {
		Integer isAddFaFMsisdnResult = 0;
		//Map<Integer, List<Integer>> serviceClassOfferId = executeGetAccountDetail(fdpRequest);
		Integer intServiceClass = (Integer) fdpRequest.getAuxiliaryRequestParameter(AuxRequestParam.SERVICE_CLASS);
		@SuppressWarnings("unchecked")
		List<Integer> gadOfferIDS = (List<Integer>) fdpRequest.getAuxiliaryRequestParameter(AuxRequestParam.GAD_OFFER_ID);
		//System.out.println(" serviceClass = "+intServiceClass +" gadOfferIDS = "+gadOfferIDS.toString());
		FDPFnFOfferDetailsDTO fnfOfferDetailsDTO = null;
		if (null != intServiceClass && intServiceClass > 0 && null != gadOfferIDS) {
			//Set<Integer> keySet = serviceClassOfferId.keySet();
			FDPCache<FDPMetaBag, FDPCacheable> metaDataCache = ApplicationConfigUtil.getMetaDataCache();
			//for (Integer serviceClassKey : gadOfferIDS) {
			//	List<Integer> offerIDs = serviceClassOfferId.get(serviceClassKey);
				ServiceClassOffer serviceClassOfferObj =null;
				for (Integer offerID : gadOfferIDS) {
					serviceClassOfferObj = new ServiceClassOffer();
					serviceClassOfferObj.setServiceClass(intServiceClass);
					serviceClassOfferObj.setBaseOfferId(offerID);

					// check if cache contains serviceClass and base offer
					final FDPCacheable fafCacheData = metaDataCache.getValue(new FDPMetaBag(fdpRequest.getCircle(),
							ModuleType.FAF_OFFER, serviceClassOfferObj));
					if (fafCacheData != null && fafCacheData instanceof FnFOffer) {
						FnFOffer fafOffers = (FnFOffer) fafCacheData;
						fnfOfferDetailsDTO = fafOffers.getFnFOfferDTO();
						break;
					}

				}
		//	}
		}else{       
			LOGGER.info("Subscriber ServiceClass & offerId is not Found. msisdn : " + fdpRequest.getSubscriberNumber());
			isAddFaFMsisdnResult = FDPConstant.FAF_MAX_ADD_FAILURE;
		}

			// Get subscriber existing details for FAF onnet,Offnet,
			// International count.
			Map<Integer, Integer> subscriberFafIndicator = getSubscriberMaxLimit(fdpRequest);
			if (null != fnfOfferDetailsDTO && null != subscriberFafIndicator && subscriberFafIndicator.size() > 0) {

				final Map<String, String> configurationMap = fdpRequest.getCircle().getConfigurationKeyValueMap();

				Object fafMsisdn = fdpRequest.getAuxiliaryRequestParameter(AuxRequestParam.FAF_MSISDN_ADD);

				// Get FAF indicator for FAFMsisdn
				Integer fafIndicator;
				Integer maxfafIndicatorSubscriber;
				if (fafMsisdn.toString().startsWith(FDPConstant.FAF_INTERNATIONAL_PREFIX)) {
					fafIndicator = Integer.parseInt(configurationMap.get(
							ConfigurationKey.FAF_INDICATOR_INTERNATIONAL.getAttributeName()).toString());
					// If Cache max faf indicator is greater than subscriber faf
					// indicator let them add
					//maxfafIndicatorSubscriber = subscriberFafIndicator.get(fafIndicator);
					// If user come 1st time map is null return true. Configured
					// count less than or equal to current count return false
					// user will not add faf number.
					if(subscriberFafIndicator.containsKey(fafIndicator)){
                    	maxfafIndicatorSubscriber = subscriberFafIndicator.get(fafIndicator);
                    }else{
                    	maxfafIndicatorSubscriber=0;
                    }
					/*if (null == maxfafIndicatorSubscriber) {
						return true;
					}*/
					if (fnfOfferDetailsDTO.getMaxInternational() < (maxfafIndicatorSubscriber + 1)) {
						//Max intenatonal reached
						isAddFaFMsisdnResult = FDPConstant.FAF_MAX_INTERNATIONAL_LIMIT_REACHED;
						
					} else {
						isAddFaFMsisdnResult = FDPConstant.FAF_MAX_ADD_TRUE;
					}
				} else {
					// Call GAD command for FAF
					Long subscriberNumber = fdpRequest.getSubscriberNumber();
					fafMsisdn = FnfUtil.normalizeFafMsisdnForCommand(fafMsisdn.toString());
					((FDPRequestImpl)fdpRequest).putAuxiliaryRequestParameter(AuxRequestParam.SUBSCRIBER_NUMBER, fafMsisdn);
					((FDPRequestImpl) fdpRequest).setSubscriberNumber(Long.parseLong(fafMsisdn.toString()));
					FDPCommand fdpCommandOutput = executeCommand(fdpRequest, Command.GETACCOUNTDETAILS);
					((FDPRequestImpl) fdpRequest).setSubscriberNumber(subscriberNumber);

					if (fdpCommandOutput.getResponseError().getResponseCode()
							.equals(FDPConstant.GAD_SUCCESS_RESPONSE102)) {
						fafIndicator = Integer.parseInt(configurationMap.get(
								ConfigurationKey.FAF_INDICATOR_OFFNET.getAttributeName()).toString());

						/*maxfafIndicatorSubscriber = subscriberFafIndicator.get(fafIndicator);
						if (null == maxfafIndicatorSubscriber) {
							return true;
						}*/
						if(subscriberFafIndicator.containsKey(fafIndicator)){
	                    	maxfafIndicatorSubscriber = subscriberFafIndicator.get(fafIndicator);
	                    }else{
	                    	maxfafIndicatorSubscriber=0;
	                    }
						if (fnfOfferDetailsDTO.getMaxOffnet() < (maxfafIndicatorSubscriber + 1)) {
							// Max offnet limit reached
							isAddFaFMsisdnResult = FDPConstant.FAF_MAX_OFFNET_LIMIT_REACHED;
						} else {
							isAddFaFMsisdnResult = FDPConstant.FAF_MAX_ADD_TRUE;
						}

					} else if (Integer.parseInt(fdpCommandOutput.getResponseError().getResponseCode()) < 3) {
						fafIndicator = Integer.parseInt(configurationMap.get(
								ConfigurationKey.FAF_INDICATOR_ONNET.getAttributeName()).toString());

						/*maxfafIndicatorSubscriber = subscriberFafIndicator.get(fafIndicator);
						if (null == maxfafIndicatorSubscriber) {
							return true;
						}*/
						if(subscriberFafIndicator.containsKey(fafIndicator)){
	                    	maxfafIndicatorSubscriber = subscriberFafIndicator.get(fafIndicator);
	                    }else{
	                    	maxfafIndicatorSubscriber=0;
	                    }
						if (fnfOfferDetailsDTO.getMaxOnnet() < (maxfafIndicatorSubscriber + 1)) {
							//Max onnet limit reached.
							isAddFaFMsisdnResult = FDPConstant.FAF_MAX_ONNET_LIMIT_REACHED;
						} else {
							isAddFaFMsisdnResult = FDPConstant.FAF_MAX_ADD_TRUE;
						}
					} else {
						LOGGER.info("Msisdn is not found in system :: msisdn : " + fafMsisdn.toString());
						
						isAddFaFMsisdnResult = FDPConstant.FAF_MAX_ADD_FAILURE;

					}
				}

			}

		else {
			LOGGER.info("FAF list is empty " + fdpRequest.getSubscriberNumber());
			Object fafMsisdnAdd = fdpRequest.getAuxiliaryRequestParameter(AuxRequestParam.FAF_MSISDN_ADD);

			// If getFafList is empty
			if (fafMsisdnAdd.toString().startsWith(FDPConstant.FAF_INTERNATIONAL_PREFIX)) {
				if(null != fnfOfferDetailsDTO.getMaxInternational() && fnfOfferDetailsDTO.getMaxInternational() >0){
					isAddFaFMsisdnResult = FDPConstant.FAF_MAX_ADD_TRUE;
				}else{
					isAddFaFMsisdnResult = FDPConstant.FAF_MAX_INTERNATIONAL_LIMIT_REACHED;
				}
			}else{
				if(null != fnfOfferDetailsDTO.getMaxOnnet() && null != fnfOfferDetailsDTO.getMaxOffnet() && fnfOfferDetailsDTO.getMaxOnnet()>0 && fnfOfferDetailsDTO.getMaxOffnet()>0){
					isAddFaFMsisdnResult = FDPConstant.FAF_MAX_ADD_TRUE;
				}else{
					isAddFaFMsisdnResult = FDPConstant.FAF_MAX_OFFNET_LIMIT_REACHED;
				}
			}
			
		}
			((FDPRequestImpl)fdpRequest).putAuxiliaryRequestParameter(AuxRequestParam.FAF_NUMBER,null);
		return isAddFaFMsisdnResult;
	}

	/**
	 * This method is using to get dynamic offerId for FAF registration from
	 * Utilities--> base Offer sharing.
	 * 
	 * @param fdpRequest
	 * @return
	 * @throws ExecutionFailedException
	 */
	public static Integer getFafOfferIdForUpdateOffer(final FDPRequest fdpRequest) throws ExecutionFailedException {
		Map<Integer, List<Integer>> serviceClassOfferId = executeGetAccountDetail(fdpRequest);
		if (null != serviceClassOfferId && serviceClassOfferId.size() > 0) {
			FDPFnFOfferDetailsDTO fnfOfferDetailsDtoObj = null;
			Set<Integer> keySet = serviceClassOfferId.keySet();
			FDPCache<FDPMetaBag, FDPCacheable> metaDataCache = ApplicationConfigUtil.getMetaDataCache();
			for (Integer serviceClassKey : keySet) {
				List<Integer> offerIDs = serviceClassOfferId.get(serviceClassKey);
				ServiceClassOffer serviceClassOfferObj;
				for (Integer offerID : offerIDs) {
					serviceClassOfferObj = new ServiceClassOffer();
					serviceClassOfferObj.setServiceClass(serviceClassKey);
					serviceClassOfferObj.setBaseOfferId(offerID);

					// Read data from cache if it exist.
					final FDPCacheable fafCacheData = metaDataCache.getValue(new FDPMetaBag(fdpRequest.getCircle(),
							ModuleType.FAF_OFFER, serviceClassOfferObj));
					if (fafCacheData != null && fafCacheData instanceof FnFOffer) {
						FnFOffer fafOffers = (FnFOffer) fafCacheData;
						fnfOfferDetailsDtoObj = fafOffers.getFnFOfferDTO();
						break;
					}
				}
			}
			if (null != fnfOfferDetailsDtoObj && null != fnfOfferDetailsDtoObj.getFnfOffer())
				return fnfOfferDetailsDtoObj.getFnfOffer();
			else
				return 0;
		} else {
			return 0;
		}
	}

	/**
	 * By eagarsh This method is use get count for onnnet, offnet, international
	 * for FAF Modify.
	 * 
	 * @throws FafFailedException
	 */
	public static Integer isAddMoreFafNumberToModify(final FDPRequest fdpRequest) throws ExecutionFailedException {
		Integer isAddFaFMsisdnResult = 0;
		//Map<Integer, List<Integer>> serviceClassOfferId = executeGetAccountDetail(fdpRequest);
		Integer intServiceClass = (Integer) fdpRequest.getAuxiliaryRequestParameter(AuxRequestParam.SERVICE_CLASS);
		@SuppressWarnings("unchecked")
		List<Integer> gadOfferIDS = (List<Integer>) fdpRequest.getAuxiliaryRequestParameter(AuxRequestParam.GAD_OFFER_ID);
		
		if (null != intServiceClass && intServiceClass > 0 && gadOfferIDS.size()>0) {
			FDPFnFOfferDetailsDTO fnfOfferDetailsDTO = null;
			//Set<Integer> keySet = serviceClassOfferId.keySet();
			FDPCache<FDPMetaBag, FDPCacheable> metaDataCache = ApplicationConfigUtil.getMetaDataCache();
		//	for (Integer serviceClassKey : keySet) {
			//	List<Integer> offerIDs = serviceClassOfferId.get(serviceClassKey);
				ServiceClassOffer serviceClassOfferObj=null;
				for (Integer offerID : gadOfferIDS) {
					serviceClassOfferObj = new ServiceClassOffer();
					serviceClassOfferObj.setServiceClass(intServiceClass);
					serviceClassOfferObj.setBaseOfferId(offerID);

					// check if cache contains serviceClass and base Offer get
					// it from cache.
					final FDPCacheable fafCacheData = metaDataCache.getValue(new FDPMetaBag(fdpRequest.getCircle(),
							ModuleType.FAF_OFFER, serviceClassOfferObj));
					if (fafCacheData != null && fafCacheData instanceof FnFOffer) {
						FnFOffer fafOffers = (FnFOffer) fafCacheData;
						fnfOfferDetailsDTO = fafOffers.getFnFOfferDTO();
						break;
					}
				//}
			}

			// Get subscriber current FAF onnet,Offnet, International count from
			// MAP
			Map<Integer, Integer> subscriberFafIndicator = getSubscriberMaxLimit(fdpRequest);
			if (null != fnfOfferDetailsDTO && null != subscriberFafIndicator && subscriberFafIndicator.size() > 0) {

				final Map<String, String> configurationMap = fdpRequest.getCircle().getConfigurationKeyValueMap();

				Object fafMsisdn = fdpRequest.getAuxiliaryRequestParameter(AuxRequestParam.FAF_MSISDN_ADD);
				//Object fafMsisdn = fdpRequest.getAuxiliaryRequestParameter(AuxRequestParam.FAF_NUMBER);
				Integer fafIndicatorDelete;
				Integer fafDeleteCount;
				// Get FAFIndicator for delete no for modify.
				Object fafMsisdnDelete = fdpRequest.getAuxiliaryRequestParameter(AuxRequestParam.FAF_MSISDN_DELETE);
				if (fafMsisdnDelete.toString().startsWith(FDPConstant.FAF_INTERNATIONAL_PREFIX)) {
					fafIndicatorDelete = Integer.parseInt(configurationMap.get(
							ConfigurationKey.FAF_INDICATOR_INTERNATIONAL.getAttributeName()).toString());
					
					//fafDeleteCount = subscriberFafIndicator.get(fafIndicatorDelete);
					if(subscriberFafIndicator.containsKey(fafIndicatorDelete)){
						fafDeleteCount = subscriberFafIndicator.get(fafIndicatorDelete);
					}else{
						fafDeleteCount=0;
					}
					if (null != fafDeleteCount && fafDeleteCount > 0)
						subscriberFafIndicator.put(fafIndicatorDelete, (fafDeleteCount - 1));
				} else {
					Long subscriberNumberDelete = fdpRequest.getSubscriberNumber();
					fafMsisdnDelete =  FnfUtil.normalizeFafMsisdnForCommand(fafMsisdnDelete.toString());
					((FDPRequestImpl)fdpRequest).putAuxiliaryRequestParameter(AuxRequestParam.SUBSCRIBER_NUMBER, fafMsisdnDelete);
					((FDPRequestImpl) fdpRequest).setSubscriberNumber(Long.parseLong(fafMsisdnDelete.toString()));
					FDPCommand fdpCommandOutputToDelete = executeCommand(fdpRequest, Command.GETACCOUNTDETAILS);
					((FDPRequestImpl) fdpRequest).setSubscriberNumber(subscriberNumberDelete);

					if (fdpCommandOutputToDelete.getResponseError().getResponseCode()
							.equals(FDPConstant.GAD_SUCCESS_RESPONSE102)) {
						fafIndicatorDelete = Integer.parseInt(configurationMap.get(
								ConfigurationKey.FAF_INDICATOR_OFFNET.getAttributeName()).toString());

						//fafDeleteCount = subscriberFafIndicator.get(fafIndicatorDelete);
						if(subscriberFafIndicator.containsKey(fafIndicatorDelete)){
							fafDeleteCount = subscriberFafIndicator.get(fafIndicatorDelete);
						}else{
							fafDeleteCount=0;
						}
						if (null != fafDeleteCount && fafDeleteCount > 0)
							subscriberFafIndicator.put(fafIndicatorDelete, (fafDeleteCount - 1));
					} else if (Integer.parseInt(fdpCommandOutputToDelete.getResponseError().getResponseCode()) < 3) {
						fafIndicatorDelete = Integer.parseInt(configurationMap.get(
								ConfigurationKey.FAF_INDICATOR_ONNET.getAttributeName()).toString());
						
						//fafDeleteCount = subscriberFafIndicator.get(fafIndicatorDelete);
						if(subscriberFafIndicator.containsKey(fafIndicatorDelete)){
							fafDeleteCount = subscriberFafIndicator.get(fafIndicatorDelete);
						}else{
							fafDeleteCount=0;
						}
						if (null != fafDeleteCount && fafDeleteCount > 0)
							subscriberFafIndicator.put(fafIndicatorDelete, (fafDeleteCount - 1));

					} else {
						// To delete number is not exist in our system.
						LOGGER.info("Msisdn is not found in system :: Response code ="
								+ fdpCommandOutputToDelete.getResponseError().getResponseCode() + " msisdn : "
								+ fafMsisdnDelete.toString());
						isAddFaFMsisdnResult = FDPConstant.FAF_MSISDN_DELETE_NOT_FOUND;
					}
				}
				// Get FAF indicator for FAFMsisdn when user coming to add.
				Integer fafIndicator;
				Integer maxfafIndicatorSubscriber;
				if (fafMsisdn.toString().startsWith(FDPConstant.FAF_INTERNATIONAL_PREFIX)) {
					fafIndicator = Integer.parseInt(configurationMap.get(
							ConfigurationKey.FAF_INDICATOR_INTERNATIONAL.getAttributeName()).toString());
					// If Cache max faf indicator is greater than subscriber faf
					// indicator let them add
					if(subscriberFafIndicator.containsKey(fafIndicator)){
						maxfafIndicatorSubscriber = subscriberFafIndicator.get(fafIndicator);	
					}else{
						maxfafIndicatorSubscriber= 0;
					}
					

					/*if (null == maxfafIndicatorSubscriber) {
						return true;
					}*/
					if (fnfOfferDetailsDTO.getMaxInternational() < (maxfafIndicatorSubscriber + 1)) {
						isAddFaFMsisdnResult = FDPConstant.FAF_MAX_INTERNATIONAL_LIMIT_REACHED;
						// If max is not allowed for International. check delete
						// no is international.
					} else {
						isAddFaFMsisdnResult = FDPConstant.FAF_MAX_ADD_TRUE;
					}
				} else {
					// Call GAD command for FAF
					Long subscriberNumber = fdpRequest.getSubscriberNumber();
					fafMsisdn = FnfUtil.normalizeFafMsisdnForCommand(fafMsisdn.toString());
					((FDPRequestImpl)fdpRequest).putAuxiliaryRequestParameter(AuxRequestParam.SUBSCRIBER_NUMBER, fafMsisdn);
					((FDPRequestImpl) fdpRequest).setSubscriberNumber(Long.parseLong(fafMsisdn.toString()));
					FDPCommand fdpCommandOutput = executeCommand(fdpRequest, Command.GETACCOUNTDETAILS);
					((FDPRequestImpl) fdpRequest).setSubscriberNumber(subscriberNumber);

					if (fdpCommandOutput.getResponseError().getResponseCode()
							.equals(FDPConstant.GAD_SUCCESS_RESPONSE102)) {
						fafIndicator = Integer.parseInt(configurationMap.get(
								ConfigurationKey.FAF_INDICATOR_OFFNET.getAttributeName()).toString());

						if(subscriberFafIndicator.containsKey(fafIndicator)){
							maxfafIndicatorSubscriber = subscriberFafIndicator.get(fafIndicator);	
						}else{
							maxfafIndicatorSubscriber= 0;
						}
					/*	maxfafIndicatorSubscriber = subscriberFafIndicator.get(fafIndicator);
						if (null == maxfafIndicatorSubscriber) {
							return true;
						}*/					
						if (fnfOfferDetailsDTO.getMaxOffnet() < (maxfafIndicatorSubscriber + 1)) {
							isAddFaFMsisdnResult = FDPConstant.FAF_MAX_OFFNET_LIMIT_REACHED;
						} else {
							isAddFaFMsisdnResult = FDPConstant.FAF_MAX_ADD_TRUE;
						}

					} else if (Integer.parseInt(fdpCommandOutput.getResponseError().getResponseCode()) < 3) {
						fafIndicator = Integer.parseInt(configurationMap.get(
								ConfigurationKey.FAF_INDICATOR_ONNET.getAttributeName()).toString());

						/*maxfafIndicatorSubscriber = subscriberFafIndicator.get(fafIndicator);
						if (null == maxfafIndicatorSubscriber) {
							return true;
						}*/
						if(subscriberFafIndicator.containsKey(fafIndicator)){
							maxfafIndicatorSubscriber = subscriberFafIndicator.get(fafIndicator);	
						}else{
							maxfafIndicatorSubscriber= 0;
						}
						if (fnfOfferDetailsDTO.getMaxOnnet() < (maxfafIndicatorSubscriber + 1)) {
							isAddFaFMsisdnResult = FDPConstant.FAF_MAX_ONNET_LIMIT_REACHED;
						} else {
							isAddFaFMsisdnResult = FDPConstant.FAF_MAX_ADD_TRUE;
						}
					} else {
						LOGGER.info("Msisdn is not found in system :: Response code ="
								+ fdpCommandOutput.getResponseError().getResponseCode() + " msisdn : "
								+ fafMsisdn.toString());
						isAddFaFMsisdnResult = FDPConstant.FAF_ADD_MSISDN_DETAILS_NOT_FOUND;

					}
				}

			}else{
				LOGGER.info("Faf List is empty :: cannot modify fafList " + fdpRequest.getSubscriberNumber());
				isAddFaFMsisdnResult = FDPConstant.FAF_LIST_IS_EMPTY;
			}

		} else {
			LOGGER.info("Subscriber ServiceClass & offerId is not Found msisdn : " + fdpRequest.getSubscriberNumber());
			isAddFaFMsisdnResult = FDPConstant.FAF_SC_BO_NOT_FOUND;
		}
		((FDPRequestImpl)fdpRequest).putAuxiliaryRequestParameter(AuxRequestParam.FAF_NUMBER,null);
		return isAddFaFMsisdnResult;
	}

	/**
	 * This method check that subscriber is already register for FAF service.
	 * @param fdpRequest
	 * @return
	 * @throws ExecutionFailedException
	 */
	public static Integer IsSubscriberAlreadyRegisterForFaf(final FDPRequest fdpRequest) throws ExecutionFailedException {
		Map<Integer, List<Integer>> serviceClassOfferId = executeGetAccountDetail(fdpRequest);
		if (null != serviceClassOfferId && serviceClassOfferId.size() > 0) {
			FDPFnFOfferDetailsDTO fnfOfferDetailsDtoObj = null;
			Set<Integer> keySet = serviceClassOfferId.keySet();
			
			FDPCache<FDPMetaBag, FDPCacheable> metaDataCache = ApplicationConfigUtil.getMetaDataCache();
			List<String>fafServiceClassCurrentList;
			String fafServiceClassCurrent;
			ServiceClassOffer serviceClassOfferObj;
			Boolean baseOfferFound;
			FnFOffer fafOffers;
			List<Integer> offerIDs ;
			
			for (Integer serviceClassKey : keySet) {
				offerIDs = serviceClassOfferId.get(serviceClassKey);
				((FDPRequestImpl)fdpRequest).putAuxiliaryRequestParameter(AuxRequestParam.SERVICE_CLASS, serviceClassKey);
				((FDPRequestImpl)fdpRequest).putAuxiliaryRequestParameter(AuxRequestParam.GAD_OFFER_ID, offerIDs);
				fafServiceClassCurrent = null;
				final Map<String, String> configurationMap = fdpRequest.getCircle().getConfigurationKeyValueMap();
				fafServiceClassCurrent = configurationMap.get(ConfigurationKey.FAF_SERVICE_CLASS.getAttributeName());
				fafServiceClassCurrentList = getServiceClassList(fafServiceClassCurrent);
				
				if(null != fafServiceClassCurrentList && null != serviceClassKey){
					//if(Integer.parseInt(fafServiceClassCurrent) != serviceClassKey){
					if(!fafServiceClassCurrentList.contains(String.valueOf(serviceClassKey))){
						LOGGER.info("Subscriber is not registered for FAF. msisdn : " + fdpRequest.getSubscriberNumber()
								+ "Service class: " + serviceClassKey + " Subscriber service class : " + fafServiceClassCurrent);
						return FDPConstant.SERVICE_CLASS_IS_NOT_ELIGIBLE;
					}
				}
				baseOfferFound=false;
				serviceClassOfferObj = null;
				for (Integer offerID : offerIDs) {
					serviceClassOfferObj = new ServiceClassOffer();
					serviceClassOfferObj.setServiceClass(serviceClassKey);
					serviceClassOfferObj.setBaseOfferId(offerID);

					// Read data from cache if it exist.
					final FDPCacheable fafCacheData = metaDataCache.getValue(new FDPMetaBag(fdpRequest.getCircle(),
							ModuleType.FAF_OFFER, serviceClassOfferObj));
					if (fafCacheData != null && fafCacheData instanceof FnFOffer) {
                        fafOffers = (FnFOffer) fafCacheData;
						fnfOfferDetailsDtoObj = fafOffers.getFnFOfferDTO();
						baseOfferFound=true;
						break;
					}
				}

				if (null != fnfOfferDetailsDtoObj && null != fnfOfferDetailsDtoObj.getFnfOffer()
						&& offerIDs.contains(fnfOfferDetailsDtoObj.getFnfOffer())) {
					LOGGER.info("Subscriber is already registered for FAF. msisdn : " + fdpRequest.getSubscriberNumber()
							+ " FAF offerID : " + fnfOfferDetailsDtoObj.getFnfOffer());
					return FDPConstant.FAF_MSISDN_ALREADY_REGISTER;
				} else {
					if(baseOfferFound){
						LOGGER.info("Subscriber is not registered for FAF msisdn : " + fdpRequest.getSubscriberNumber()
								+ "Service class: " + serviceClassOfferObj.getServiceClass()
								+ " Subscriber FAF offerIDs : " + offerIDs.toString());
						return FDPConstant.FAF_MSISDN_IS_NOT_REGISTER;
					}else{
						//Base offer is not found
						LOGGER.info("Subscriber base offer is not found to registered FAF msisdn : " + fdpRequest.getSubscriberNumber()
								+ "Service class: " + serviceClassOfferObj.getServiceClass()
								+ " Subscriber FAF offerIDs : " + offerIDs.toString());
						return FDPConstant.FAF_MSISDN_BASE_OFFER_NOT_FOUND;
					}
					
				}
			}
		} else {
			LOGGER.info("Subscriber service class is not found for FAF + msisdn : " + fdpRequest.getSubscriberNumber());
			return FDPConstant.FAF_SC_BO_NOT_FOUND;
		}
		return FDPConstant.FAF_SC_BO_NOT_FOUND;
	}
	
	private static List<String> getServiceClassList(String fafServiceClassCurrent) {
		if (null != fafServiceClassCurrent) {
			String[] arr = fafServiceClassCurrent.split(FDPConstant.COMMA);
			List<String> serviceClassList = new ArrayList<>();
			for (int i = 0; i < arr.length; i++) {
				if (arr[i].trim().isEmpty())
					continue;
				serviceClassList.add(arr[i].trim());
			}
			return serviceClassList;
		}
		return null;

	}
	
	
	/**
	 * This method is use to identify MSISDN format.
	 * @param fafMsisdn
	 * @param fdpRequest
	 * @return
	 */
	public static boolean validateMagicMsisdn(final String fafMsisdn, final FDPRequest fdpRequest){
		boolean bolValidationOfMsisdn;
		/*if(MSISDN_LENGTH == fafMsisdn.length()){
			bolValidationOfMsisdn=true;
			((FDPRequestImpl) fdpRequest).putAuxiliaryRequestParameter(AuxRequestParam.WITH_CC_ADD_MAGICNO,(COUNTRY_CODE+fafMsisdn));
			((FDPRequestImpl) fdpRequest).putAuxiliaryRequestParameter(AuxRequestParam.WITHOUT_CC_ADD_MAGICNO,fafMsisdn);
			((FDPRequestImpl) fdpRequest).putAuxiliaryRequestParameter(AuxRequestParam.WITH_ZERO_ADD_MAGICNO,(FDPConstant.FAF_INTERNATIONAL_PREFIX +fafMsisdn));
			((FDPRequestImpl)fdpRequest).putAuxiliaryRequestParameter(AuxRequestParam.UPDATE_MSISDN_FOR_MAGIC_NUMBER,(COUNTRY_CODE+fafMsisdn));
			
		}else if(fafMsisdn.startsWith(COUNTRY_CODE) && (fafMsisdn.length() == (MSISDN_LENGTH + COUNTRY_CODE_LENGTH))){
			bolValidationOfMsisdn=true;
			((FDPRequestImpl) fdpRequest).putAuxiliaryRequestParameter(AuxRequestParam.WITH_CC_ADD_MAGICNO,fafMsisdn);
			((FDPRequestImpl) fdpRequest).putAuxiliaryRequestParameter(AuxRequestParam.WITHOUT_CC_ADD_MAGICNO,fafMsisdn.substring(COUNTRY_CODE_LENGTH));
			((FDPRequestImpl) fdpRequest).putAuxiliaryRequestParameter(AuxRequestParam.WITH_ZERO_ADD_MAGICNO,(FDPConstant.FAF_INTERNATIONAL_PREFIX +fafMsisdn.substring(COUNTRY_CODE_LENGTH)));
			((FDPRequestImpl)fdpRequest).putAuxiliaryRequestParameter(AuxRequestParam.UPDATE_MSISDN_FOR_MAGIC_NUMBER,fafMsisdn);
		}else*/ if (fafMsisdn.startsWith(FDPConstant.MSISDN_WITH_PREFIX_ZERO) && (fafMsisdn.length() == MSISDN_LENGTH+ FDPConstant.MSISDN_WITH_PREFIX_ZERO.length())){
			//int lenghtInternational = FDPConstant.MSISDN_WITH_PREFIX_ZERO.length();
			bolValidationOfMsisdn=true;
			((FDPRequestImpl) fdpRequest).putAuxiliaryRequestParameter(AuxRequestParam.WITH_CC_ADD_MAGICNO,(COUNTRY_CODE + fafMsisdn.substring( FDPConstant.MSISDN_WITH_PREFIX_ZERO.length())));
			((FDPRequestImpl) fdpRequest).putAuxiliaryRequestParameter(AuxRequestParam.WITHOUT_CC_ADD_MAGICNO, fafMsisdn);
			((FDPRequestImpl) fdpRequest).putAuxiliaryRequestParameter(AuxRequestParam.WITH_ZERO_ADD_MAGICNO, FDPConstant.FAF_INTERNATIONAL_PREFIX + COUNTRY_CODE + fafMsisdn.substring(FDPConstant.MSISDN_WITH_PREFIX_ZERO.length()));
			((FDPRequestImpl)fdpRequest).putAuxiliaryRequestParameter(AuxRequestParam.UPDATE_MSISDN_FOR_MAGIC_NUMBER, COUNTRY_CODE +fafMsisdn.substring( FDPConstant.MSISDN_WITH_PREFIX_ZERO.length()));
		}else{
			bolValidationOfMsisdn=false;
		}
		return bolValidationOfMsisdn;
	}

	/**
	 * This method is use to execute GAD command if response is 0,1,2 then Msisdn is onnet.
	 * @param fdpRequest
	 * @return
	 * @throws ExecutionFailedException
	 */
	public static Boolean executeCommandForMagicValidation(FDPRequest fdpRequest) throws ExecutionFailedException {
		FDPCommand fdpCommandOutput = executeCommand(fdpRequest, Command.GETACCOUNTDETAILS);
		Boolean result=false;
		 if (Integer.parseInt(fdpCommandOutput.getResponseError().getResponseCode()) < 3) {
			 result = true;
		 }
		return result;
	}
	
	/**
	 * The method is use to check Msisdn is onnet or offnet.
	 * @param fdpRequest
	 * @return
	 * @throws ExecutionFailedException 
	 */
	public static boolean validationAndexecutionForMagicNumber(FDPRequest fdpRequest){
			boolean result = false;
			//Call GAD command for friend No
			Long subscriberNoLong = fdpRequest.getSubscriberNumber();
			((FDPRequestImpl) fdpRequest).setSubscriberNumber(Long.parseLong(fdpRequest.getAuxiliaryRequestParameter(AuxRequestParam.UPDATE_MSISDN_FOR_MAGIC_NUMBER).toString()));
			try {
				if(FnfUtil.executeCommandForMagicValidation(fdpRequest)){
					result=true;
				}else{
					result= false;
				}
			} catch (ExecutionFailedException e) {
				LOGGER.error("validationAndexecutionForMagicNumber :: "+e);
			}
			((FDPRequestImpl) fdpRequest).setSubscriberNumber(subscriberNoLong);
			return result;
		
	}
}
