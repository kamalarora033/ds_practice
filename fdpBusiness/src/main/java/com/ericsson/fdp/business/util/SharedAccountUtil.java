package com.ericsson.fdp.business.util;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.naming.NamingException;

import com.ericsson.fdp.FDPCacheable;
import com.ericsson.fdp.business.command.AbstractCommand;
import com.ericsson.fdp.business.constants.FDPStepResponseConstants;
import com.ericsson.fdp.business.enums.AuxRequestParam;
import com.ericsson.fdp.business.enums.Command;
import com.ericsson.fdp.business.enums.ConfigurationKey;
import com.ericsson.fdp.business.product.Product;
import com.ericsson.fdp.business.sharedaccount.constans.SharedAccountConstants;
import com.ericsson.fdp.common.constants.FDPConstant;
import com.ericsson.fdp.common.constants.JNDILookupConstant;
import com.ericsson.fdp.common.enums.ChannelType;
import com.ericsson.fdp.common.enums.ModuleType;
import com.ericsson.fdp.common.enums.Status;
import com.ericsson.fdp.common.vo.CircleConfigParamDTO;
import com.ericsson.fdp.common.vo.FDPCircle;
import com.ericsson.fdp.core.cache.AppCacheSubStore;
import com.ericsson.fdp.core.command.FDPCommand;
import com.ericsson.fdp.core.command.param.CommandParam;
import com.ericsson.fdp.core.dsm.framework.service.FDPCache;
import com.ericsson.fdp.core.exception.ExecutionFailedException;
import com.ericsson.fdp.core.request.FDPRequest;
import com.ericsson.fdp.core.request.impl.FDPRequestImpl;
import com.ericsson.fdp.core.serviceprovisioning.bean.FDPAppBag;
import com.ericsson.fdp.core.serviceprovisioning.bean.FDPMetaBag;
import com.ericsson.fdp.core.utils.ApplicationConfigUtil;
import com.ericsson.fdp.dao.dto.sharedaccount.ProductAddInfoAttributeDTO;
import com.ericsson.fdp.dao.enums.ProductAdditionalInfoEnum;
import com.ericsson.fdp.dao.enums.SharedAccountErrorCodeEnum;
import com.ericsson.fdp.dao.enums.SharedAccountResponseType;

/**
 * The class is utiltiy class for shared account util.
 * 
 * @author Ericsson
 * 
 */
public class SharedAccountUtil {

	/**
	 * Instantiates a new shared account util.
	 */
	private SharedAccountUtil() {

	}

	/** The Constant STATUS. */
	public static final String STATUS = "Status";

	/** The Constant COUNTER_VALUE. */
	public static final String COUNTER_VALUE = "counterValue";

	/** The Constant COUNTER_UNIT. */
	public static final String COUNTER_UNIT = "counterUnit";

	/**
	 * Gets the usage value.
	 * 
	 * @param providerMsisdn
	 *            the provider msisdn
	 * @param consumerMsisdn
	 *            the consumer msisdn
	 * @param defaultUnit
	 *            the default unit
	 * @param transactionId
	 *            the transaction id
	 * @param fdpCircle
	 *            the fdp circle
	 * @return the usage value
	 * @throws ExecutionFailedException
	 *             the execution failed exception
	 */
	@SuppressWarnings("unchecked")
	public static Map<String, Object> getUsageValue(final Long providerMsisdn, final Long consumerMsisdn,
			final String defaultUnit, final Long transactionId, final FDPCircle fdpCircle)
			throws ExecutionFailedException {

		if (providerMsisdn == null) {
			throw new ExecutionFailedException("The provider msisdn cannot be null");
		}
		FDPCache<FDPMetaBag, FDPCacheable> metaDataCache;
		try {
			metaDataCache = (FDPCache<FDPMetaBag, FDPCacheable>) ApplicationConfigUtil
					.getBean(JNDILookupConstant.META_DATA_CACHE_JNDI_NAME);
		} catch (final NamingException e) {
			throw new ExecutionFailedException("Naming exception occured in getting meta cache");
		}

		final FDPRequestImpl fdpRequest = new FDPRequestImpl();
		// set circle parameters
		final CircleConfigParamDTO circleConfigParamDTO = RequestUtil.populateCircleConfigParamDTO(
				providerMsisdn.toString(), fdpCircle);
		fdpRequest.setOriginHostName(circleConfigParamDTO.getOriginHostName());
		fdpRequest.setOriginNodeType(circleConfigParamDTO.getOriginNodeType());
		fdpRequest.setSubscriberNumber(circleConfigParamDTO.getSubscriberNumber());
		fdpRequest.setIncomingSubscriberNumber(circleConfigParamDTO.getIncomingSubscriberNumber());
		fdpRequest.setSubscriberNumberNAI(circleConfigParamDTO.getSubscriberNumberNAI());
		fdpRequest.putAuxiliaryRequestParameter(AuxRequestParam.CONSUMER_MSISDN, consumerMsisdn.toString());
		fdpRequest.setChannel(ChannelType.SMS);
		fdpRequest.setCircle(fdpCircle);
		fdpRequest.setOriginTransactionID(transactionId);

		FDPCommand fdpCommand = null;
		final FDPCircle fdpCircleForCommand = new FDPCircle(-1L, "ALL", "ALL");
		final FDPCacheable fdpCommandCached = metaDataCache.getValue(new FDPMetaBag(fdpCircleForCommand,
				ModuleType.COMMAND, Command.GET_USAGE_THRESHOLDS_AND_COUNTERS_FOR_BATCH_JOB.getCommandDisplayName()));
		if (fdpCommandCached instanceof FDPCommand) {
			fdpCommand = (FDPCommand) fdpCommandCached;
			((AbstractCommand) fdpCommand).setCommandDisplayName(Command.GET_USAGE_THRESHOLDS_AND_COUNTERS
					.getCommandDisplayName());
		}

		final Status status = fdpCommand.execute(fdpRequest);
		final Object outputVal = fdpCommand.getOutputParam("usageCounterUsageThresholdInformation.0.usageCounterValue");
		final Object outputValForMonetary = fdpCommand
				.getOutputParam("usageCounterUsageThresholdInformation.0.usageCounterMonetaryValue1");

		final Map<String, Object> returnMap = new HashMap<String, Object>();
		if (Status.SUCCESS.equals(status) && (outputVal != null || outputValForMonetary != null)) {
			returnMap.put(STATUS, Boolean.TRUE);
			returnMap.put(COUNTER_VALUE, outputVal == null ? outputValForMonetary : outputVal);
			returnMap.put(COUNTER_UNIT, defaultUnit);
		} else {
			returnMap.put(STATUS, Boolean.FALSE);
		}
		return returnMap;
	}

	/**
	 * This method is used to find the usage value for a consumer or a group of
	 * consumers.
	 * 
	 * @param providerMsisdn
	 *            the provider msisdn.
	 * @param compareId
	 *            the id to be used, for consumer it is consumer msisdn, for
	 *            common usage it is counter id.
	 * @param defaultUnit
	 *            the default unit to be used for usage value.
	 * @param fdpRequest
	 *            the request object.
	 * @param isConsumerUsage
	 *            true, if consumer usage is to be found, false if common usage
	 *            is to be found.
	 * @return the map containing information of the usage.
	 * @throws ExecutionFailedException
	 *             Exception, if any.
	 */
	public static Map<SharedAccountResponseType, Object> getUsageValueForConsumer(final Long providerMsisdn,
			final Long compareId, final String defaultUnit, final FDPRequest fdpRequest, final boolean isConsumerUsage)
			throws ExecutionFailedException {
		final Map<SharedAccountResponseType, Object> statusMap = new HashMap<SharedAccountResponseType, Object>();
		if (providerMsisdn == null || compareId == null) {
			throw new ExecutionFailedException("The provider or compare id cannot be null");
		}
		final Map<String, Object> commandExecution = executeUsageCommandForConsumer(fdpRequest);
		final boolean commandExecuted = (Boolean) commandExecution.get(FDPStepResponseConstants.STATUS_KEY);
		if (!commandExecuted) {
			updateFailureForConsumerUsageValue(statusMap);
		} else {
			statusMap.put(SharedAccountResponseType.CONSUMER_THRESHOLD_UNIT, defaultUnit);
			if (isConsumerUsage) {
				updateConsumerUsage((FDPCommand) commandExecution.get(FDPConstant.COMMAND_OUTPUT), defaultUnit, 0,
						statusMap);
			} else {
				updateUsageThreshold((FDPCommand) commandExecution.get(FDPConstant.COMMAND_OUTPUT), defaultUnit, 0,
						statusMap);
			}
		}
		return statusMap;
	}

	/**
	 * This method is used to find the usage threshold value for a consumer or a
	 * group of consumers.
	 * 
	 * @param providerMsisdn
	 *            the provider msisdn.
	 * @param compareId
	 *            the id to be used, for consumer it is consumer msisdn, for
	 *            common usage it is counter id.
	 * @param defaultUnit
	 *            the default unit to be used for usage value.
	 * @param fdpRequest
	 *            the request object.
	 * @param isConsumerUsage
	 *            true, if consumer usage is to be found, false if common usage
	 *            is to be found.
	 * @return the map containing information of the usage.
	 * @throws ExecutionFailedException
	 *             Exception, if any.
	 */
	public static Map<SharedAccountResponseType, Object> getUsageValueForAllConsumers(final Long providerMsisdn,
			final Long compareId, final String defaultUnit, final FDPRequest fdpRequest, final boolean isConsumerUsage,
			final ProductAddInfoAttributeDTO productAddInfoAttributeDTO,final boolean isTotalUsage)
			throws ExecutionFailedException {
		final Map<SharedAccountResponseType, Object> statusMap = new HashMap<SharedAccountResponseType, Object>();
		Map<String, Object> commandExecution = null;
		if (providerMsisdn == null
				|| compareId == null
				|| (!isTotalUsage && null == productAddInfoAttributeDTO.getUsageCounterID() && null == productAddInfoAttributeDTO
						.getUsageThresholdID())) {
			throw new ExecutionFailedException("The provider or compareId or usageThresholdCounter in case of total-usage cannot be null");
		}
		if(isTotalUsage) {
			commandExecution = executeUsageCommand(fdpRequest);
		} else {
			commandExecution = executeUsageCommandForConsumer(fdpRequest);
		}
		final boolean commandExecuted = (Boolean) commandExecution.get(FDPStepResponseConstants.STATUS_KEY);
		if (!commandExecuted) {
			updateFailureForConsumerUsageValue(statusMap);
		} else {
			Map<SharedAccountResponseType, Object> usageValue = null;
			usageValue = getUsageThresholdValueForAllConsumer(
					(FDPCommand) commandExecution.get(FDPConstant.COMMAND_OUTPUT), compareId.toString(),
					defaultUnit, isConsumerUsage,productAddInfoAttributeDTO,isTotalUsage);
			if (usageValue != null) {
				statusMap.putAll(usageValue);
			}
		}
		return statusMap;
	}

	/**
	 * This method updates the map in case of failure.
	 * 
	 * @param statusMap
	 *            the map to be updated.
	 */
	private static void updateFailureForConsumerUsageValue(final Map<SharedAccountResponseType, Object> statusMap) {
		statusMap.put(SharedAccountResponseType.ERROR_CODE, SharedAccountErrorCodeEnum.NO_USAGE_VALUE_FOUND.getErrorCode().toString());
		statusMap.put(SharedAccountResponseType.ERROR_MESSAGE,
				SharedAccountErrorCodeEnum.NO_USAGE_VALUE_FOUND.getErrorMessage());
		statusMap.put(SharedAccountResponseType.STATUS, SharedAccountConstants.FAILURE);
		statusMap.put(SharedAccountResponseType.ERROR_VALUE, SharedAccountErrorCodeEnum.NO_USAGE_VALUE_FOUND.getErrorMessage());
	}

	/**
	 * This method is used to get the usage value for a conusmer.
	 * 
	 * @param fdpCommand
	 *            the command to be used.
	 * @param consumerMsisdn
	 *            the consumer msisdn to be used.
	 * @param defaultUnit
	 *            the default unit.
	 * @param isConsumerUsage
	 *            true if usage is to found, false if threshold is to be found.
	 * @return map containing the data.
	 */
	private static Map<SharedAccountResponseType, Object> getUsageThresholdValueForAllConsumer(
			final FDPCommand fdpCommand, final String usageCounterId, final String defaultUnit,
			final boolean isConsumerUsage, final ProductAddInfoAttributeDTO productAddInfoAttributeDTO, final boolean isTotalUsage) {
		int i = 0;
		boolean valueFound = false;
		final Map<SharedAccountResponseType, Object> statusMap = new HashMap<SharedAccountResponseType, Object>();
		while (!valueFound) {
			final String usageValuePath = "usageCounterUsageThresholdInformation." + i + ".usageCounterID";
			if (fdpCommand.getOutputParam(usageValuePath) == null) {
				updateFailureForConsumerUsageValue(statusMap);
				valueFound = true;
			} else if ((fdpCommand.getOutputParam(usageValuePath).getValue().toString()).equals(usageCounterId)) {
				if (isConsumerUsage) {
					updateConsumerUsage(fdpCommand, defaultUnit, i, statusMap);
				} else {
					final Long usageThresholdCounterId = (isTotalUsage ? productAddInfoAttributeDTO
							.getCommonUsageThresholdCounterID() : productAddInfoAttributeDTO.getUsageThresholdID());
					updateUsageThresholdForId(fdpCommand, defaultUnit, i, statusMap, usageThresholdCounterId);
				}
				valueFound = true;
			}
			i++;
		}
		return statusMap;
	}

	/**
	 * This method is used to update the threshold information.
	 * 
	 * @param fdpCommand
	 *            the command to be used.
	 * @param defaultUnit
	 *            the default unit to be used.
	 * @param startIndex
	 *            the index at which threshold is present.
	 * @param statusMap
	 *            the map to be updated.
	 */
	private static void updateUsageThreshold(final FDPCommand fdpCommand, final String defaultUnit,
			final int startIndex, final Map<SharedAccountResponseType, Object> statusMap) {
		final List<String> thresholdValues = getUsageThreshold(fdpCommand, defaultUnit, startIndex);
		if (thresholdValues == null || thresholdValues.isEmpty()) {
			updateFailureForConsumerUsageValue(statusMap);
		} else {
			updateSuccessForConsumerUsageValue(statusMap, thresholdValues);
		}
	}

	/**
	 * This method is used to get the usage threshold.
	 * 
	 * @param fdpCommand
	 *            the command to be used.
	 * @param defaultUnit
	 *            the default unit to be used.
	 * @param startIndex
	 *            the index at which the threshold information is present.
	 * @return the list of usage threshold values.
	 */
	private static List<String> getUsageThreshold(final FDPCommand fdpCommand, final String defaultUnit,
			final int startIndex) {
		List<String> thresholdValues = null;
		int startIndexForThreshold = 0;
		boolean valueFound = false;
		while (!valueFound) {
			final CommandParam usageValuePath = fdpCommand.getOutputParam("usageCounterUsageThresholdInformation."
					+ startIndex + ".usageThresholdInformation." + startIndexForThreshold + ".usageThresholdValue");
			final CommandParam thresholdUnitObj = fdpCommand.getOutputParam("usageCounterUsageThresholdInformation."
					+ startIndex + ".usageThresholdInformation." + startIndexForThreshold
					+ ".usagethresholdmonetaryvalue1");
			//System.out.println(thresholdUnitObj);
			if ((usageValuePath == null) && (thresholdUnitObj == null)) {
				valueFound = true;
			} else {
				final String thresholdValue = (thresholdUnitObj == null) ? usageValuePath.getValue().toString()
						: thresholdUnitObj.getValue().toString();
				//System.out.println(thresholdValue + "---------------");
				if (thresholdValues == null) {
					thresholdValues = new ArrayList<String>();
				}
				thresholdValues.add(thresholdValue + FDPConstant.SPACE + defaultUnit);
			}
			startIndexForThreshold++;
		}
		return thresholdValues;
	}

	/**
	 * This method is used to update the consumer usage values.
	 * 
	 * @param statusMap
	 *            the map to be updated.
	 * @param thresholdValues
	 *            the list of threshold values.
	 */
	private static void updateSuccessForConsumerUsageValue(final Map<SharedAccountResponseType, Object> statusMap,
			final List<String> thresholdValues) {
		statusMap.put(SharedAccountResponseType.STATUS, SharedAccountConstants.SUCCESS);
		final StringBuilder thresholdValuesAsString = new StringBuilder();
		boolean isFirst = true;
		for (final String string : thresholdValues) {
			if (!isFirst) {
				thresholdValuesAsString.append(FDPConstant.COMMA).append(FDPConstant.SPACE);
			}
			thresholdValuesAsString.append(string);
			isFirst = false;
		}
		statusMap.put(SharedAccountResponseType.CONSUMER_LIMIT, thresholdValuesAsString.toString());
		statusMap.put(SharedAccountResponseType.STATUS, SharedAccountConstants.SUCCESS);
	}

	/**
	 * This method is used to update the consumer usage.
	 * 
	 * @param fdpCommand
	 *            the command to be used.
	 * @param defaultUnit
	 *            the default unit to be used.
	 * @param i
	 *            the index at which the consumer information is present.
	 * @param statusMap
	 *            the map to be updated.
	 */
	private static void updateConsumerUsage(final FDPCommand fdpCommand, final String defaultUnit, final int i,
			final Map<SharedAccountResponseType, Object> statusMap) {
		statusMap.put(SharedAccountResponseType.CONSUMER_THRESHOLD_UNIT, defaultUnit);
		if (fdpCommand.getOutputParam("usageCounterUsageThresholdInformation." + i + ".usageCounterValue") != null) {
			statusMap.put(SharedAccountResponseType.CONSUMER_LIMIT,
					fdpCommand.getOutputParam("usageCounterUsageThresholdInformation." + i + ".usageCounterValue")
							.getValue().toString());

			statusMap.put(SharedAccountResponseType.STATUS, SharedAccountConstants.SUCCESS);
		} else if (fdpCommand.getOutputParam("usageCounterUsageThresholdInformation." + i
				+ ".usageCounterMonetaryValue1") != null) {
			statusMap.put(
					SharedAccountResponseType.CONSUMER_LIMIT,
					fdpCommand
							.getOutputParam(
									"usageCounterUsageThresholdInformation." + i + ".usageCounterMonetaryValue1")
							.getValue().toString());
			statusMap.put(SharedAccountResponseType.STATUS, SharedAccountConstants.SUCCESS);
		} else {
			updateFailureForConsumerUsageValue(statusMap);
		}
	}

	/**
	 * This method is used to execute the usage command.
	 * 
	 * @param fdpRequest
	 *            the request to be used.
	 * @return the map containing status and the command.
	 * @throws ExecutionFailedException
	 *             Exception, if any.
	 */
	private static Map<String, Object> executeUsageCommand(final FDPRequest fdpRequest) throws ExecutionFailedException {
		final Map<String, Object> returnMap = new HashMap<String, Object>();
		returnMap.put(FDPStepResponseConstants.STATUS_KEY, Boolean.FALSE);
		FDPCommand fdpCommand = fdpRequest.getExecutedCommand(Command.GET_USAGE_THRESHOLDS_AND_COUNTERS_FOR_BATCH_JOB
				.getCommandDisplayName());
		if (fdpCommand == null) {
			final FDPCircle fdpCircleForCommand = new FDPCircle(-1L, "ALL", "ALL");
			final FDPCacheable fdpCommandCached = ApplicationConfigUtil.getMetaDataCache().getValue(
					new FDPMetaBag(fdpCircleForCommand, ModuleType.COMMAND,
							Command.GET_USAGE_THRESHOLDS_AND_COUNTERS_FOR_BATCH_JOB.getCommandDisplayName()));
			if (fdpCommandCached instanceof FDPCommand) {
				fdpCommand = (FDPCommand) fdpCommandCached;
				((AbstractCommand) fdpCommand).setCommandDisplayName(Command.GET_USAGE_THRESHOLDS_AND_COUNTERS
						.getCommandDisplayName());
			}
			if (Status.SUCCESS.equals(fdpCommand.execute(fdpRequest))) {
				returnMap.put(FDPStepResponseConstants.STATUS_KEY, Boolean.TRUE);
				if (fdpRequest instanceof FDPRequestImpl) {
					final FDPRequestImpl fdpRequestImpl = (FDPRequestImpl) fdpRequest;
					((AbstractCommand) fdpCommand)
							.setCommandDisplayName(Command.GET_USAGE_THRESHOLDS_AND_COUNTERS_FOR_BATCH_JOB
									.getCommandDisplayName());
					fdpRequestImpl.addExecutedCommand(fdpCommand);
				}
			}
		} else {
			returnMap.put(FDPStepResponseConstants.STATUS_KEY, Boolean.TRUE);
		}
		returnMap.put(FDPConstant.COMMAND_OUTPUT, fdpCommand);
		return returnMap;
	}

	/**
	 * This method is used to execute the usage command.
	 * 
	 * @param fdpRequest
	 *            the request to be used.
	 * @return the map containing status and the command.
	 * @throws ExecutionFailedException
	 *             Exception, if any.
	 */
	private static Map<String, Object> executeUsageCommandForConsumer(final FDPRequest fdpRequest)
			throws ExecutionFailedException {
		final Map<String, Object> returnMap = new HashMap<String, Object>();
		returnMap.put(FDPStepResponseConstants.STATUS_KEY, Boolean.FALSE);
		FDPCommand fdpCommand = null;
		final FDPCircle fdpCircleForCommand = new FDPCircle(-1L, "ALL", "ALL");
		final FDPCacheable fdpCommandCached = ApplicationConfigUtil.getMetaDataCache().getValue(
				new FDPMetaBag(fdpCircleForCommand, ModuleType.COMMAND,
						Command.GET_USAGE_THRESHOLDS_AND_COUNTERS_FOR_CONSUMER.getCommandDisplayName()));
		if (fdpCommandCached instanceof FDPCommand) {
			fdpCommand = (FDPCommand) fdpCommandCached;
			((AbstractCommand) fdpCommand).setCommandDisplayName(Command.GET_USAGE_THRESHOLDS_AND_COUNTERS
					.getCommandDisplayName());
		}
		if (fdpCommand != null && Status.SUCCESS.equals(fdpCommand.execute(fdpRequest))) {
			returnMap.put(FDPStepResponseConstants.STATUS_KEY, Boolean.TRUE);
		}
		returnMap.put(FDPConstant.COMMAND_OUTPUT, fdpCommand);
		return returnMap;
	}

	/**
	 * Gets the fDP request for top n usage.
	 * 
	 * @param providerMsisdn
	 *            the provider msisdn
	 * @param transactionId
	 *            the transaction id
	 * @param fdpCircle
	 *            the fdp circle
	 * @return the fDP request for top n usage
	 * @throws ExecutionFailedException
	 *             the execution failed exception
	 */
	public static FDPRequestImpl getFDPRequestForTopNUsage(final Long providerMsisdn, final Long transactionId,
			final FDPCircle fdpCircle) throws ExecutionFailedException {

		if (providerMsisdn == null) {
			throw new ExecutionFailedException("The provider msisdn cannot be null");
		}
		final FDPRequestImpl fdpRequest = new FDPRequestImpl();
		// set circle parameters
		final CircleConfigParamDTO circleConfigParamDTO = RequestUtil.populateCircleConfigParamDTO(
				providerMsisdn.toString(), fdpCircle);
		fdpRequest.setOriginHostName(circleConfigParamDTO.getOriginHostName());
		fdpRequest.setOriginNodeType(circleConfigParamDTO.getOriginNodeType());
		fdpRequest.setSubscriberNumber(circleConfigParamDTO.getSubscriberNumber());
		fdpRequest.setIncomingSubscriberNumber(circleConfigParamDTO.getIncomingSubscriberNumber());
		fdpRequest.setSubscriberNumberNAI(circleConfigParamDTO.getSubscriberNumberNAI());
		fdpRequest.setChannel(ChannelType.SHARED_ACCOUNT_BATCH);
		fdpRequest.setCircle(fdpCircle);
		fdpRequest.setOriginTransactionID(transactionId);
		return fdpRequest;
	}

	/**
	 * Gets the product additional info.
	 * 
	 * @param productInfoValueMap
	 *            the product info value map
	 * @return the product additional info
	 */
	public static ProductAddInfoAttributeDTO getProductAdditionalInfo(final Map<Integer, String> productInfoValueMap) {

		final ProductAddInfoAttributeDTO productAddInfoAttr = new ProductAddInfoAttributeDTO();
		productAddInfoAttr.setNoOfConsumer(Long.parseLong(productInfoValueMap
				.get(ProductAdditionalInfoEnum.NO_OF_CONSUMERS.getKey())));
		productAddInfoAttr.setShrAccOfferId(Long.parseLong(productInfoValueMap
				.get(ProductAdditionalInfoEnum.SHARED_ACC_OFFER_ID.getKey())));
		productAddInfoAttr.setSharedAccOfferIdMapping(Long.parseLong(productInfoValueMap
				.get(ProductAdditionalInfoEnum.SHARED_ACC_OFFER_ID_MAPPING.getKey())));
		productAddInfoAttr.setProviderOfferIdMapping(Long.parseLong(productInfoValueMap
				.get(ProductAdditionalInfoEnum.PROVIDER_OFFER_ID_MAPPING.getKey())));
		productAddInfoAttr.setConsumerLimit(Integer.parseInt(productInfoValueMap
				.get(ProductAdditionalInfoEnum.CONSUMER_LIMIT.getKey())));
		productAddInfoAttr.setConsumerThresholdUnit(productInfoValueMap
				.get(ProductAdditionalInfoEnum.CONSUMER_THRESHOLD_UNIT.getKey()));
		productAddInfoAttr.setGroupLimit(Integer.parseInt(productInfoValueMap.get(ProductAdditionalInfoEnum.GROUP_LIMIT
				.getKey())));
		productAddInfoAttr.setGroupThresholdUnit(productInfoValueMap.get(ProductAdditionalInfoEnum.GROUP_THRESHOLD_UNIT
				.getKey()));
		productAddInfoAttr.setCommonUsageCounterID(Long.parseLong(productInfoValueMap
				.get(ProductAdditionalInfoEnum.COMMON_USAGE_COUNTER_ID.getKey())));
		productAddInfoAttr.setCommonUsageThresholdCounterID(Long.parseLong(productInfoValueMap
				.get(ProductAdditionalInfoEnum.DEFAULT_THRESHOLD_COUNTER_ID.getKey())));
		
		if(null != productInfoValueMap
				.get(ProductAdditionalInfoEnum.USAGE_COUNTER_ID.getKey())) {
			productAddInfoAttr.setUsageCounterID(Long.parseLong(productInfoValueMap
					.get(ProductAdditionalInfoEnum.USAGE_COUNTER_ID.getKey())));
		}
		
		if(null != productInfoValueMap
				.get(ProductAdditionalInfoEnum.THRESHOLD_COUNTER_ID.getKey())) {
			productAddInfoAttr.setUsageThresholdID(Long.parseLong(productInfoValueMap
					.get(ProductAdditionalInfoEnum.THRESHOLD_COUNTER_ID.getKey())));
		}
		return productAddInfoAttr;
	}

	// TODO : FOR GETTING PRODUCT EXPIRY
	/**
	 * Execute get offer.
	 * 
	 * @param fdpRequest
	 *            the fdp request
	 * @param offerId
	 *            the offer id
	 * @return the map
	 * @throws ExecutionFailedException
	 *             the execution failed exception
	 */
	public static Map<String, Object> executeGetOffer(final FDPRequest fdpRequest, final Long offerId)
			throws ExecutionFailedException {
		final Map<String, Object> returnMap = new HashMap<String, Object>();
		returnMap.put(FDPStepResponseConstants.STATUS_KEY, Boolean.FALSE);
		Boolean suceesCheck = true;
		FDPCommand fdpCommand = fdpRequest.getExecutedCommand(Command.GET_OFFER_FOR_SHARED_ACC.getCommandDisplayName());
		try {
			if (fdpCommand == null) {
				final FDPCircle fdpCircleForCommand = new FDPCircle(-1L, "ALL", "ALL");
				fdpCommand = (FDPCommand) ApplicationConfigUtil.getMetaDataCache().getValue(
						new FDPMetaBag(fdpCircleForCommand, ModuleType.COMMAND, Command.GET_OFFER_FOR_SHARED_ACC
								.getCommandDisplayName()));
				((AbstractCommand) fdpCommand).setCommandDisplayName(Command.GET_OFFER_FOR_SHARED_ACC.getCommandName());

				if (Status.SUCCESS.equals(fdpCommand.execute(fdpRequest))) {
					if (fdpRequest instanceof FDPRequestImpl) {
						final FDPRequestImpl fdpRequestImpl = (FDPRequestImpl) fdpRequest;
						((AbstractCommand) fdpCommand).setCommandDisplayName(Command.GET_OFFER_FOR_SHARED_ACC
								.getCommandDisplayName());
						fdpRequestImpl.addExecutedCommand(fdpCommand);
					}
					returnMap.put(FDPStepResponseConstants.STATUS_KEY, Boolean.TRUE);
				} else {
					suceesCheck = false;
					// TODO: return failure.
				}
			}
			final Calendar expiryDate = getProductExpiryDate(fdpCommand, offerId);
			if (expiryDate == null) {
				suceesCheck = false;
			}
			if (suceesCheck) {
				returnMap.put(FDPStepResponseConstants.OFFER_EXPIRY_DATE, expiryDate);
				returnMap.put(FDPStepResponseConstants.PRODUCT_USAGE_COUNTER, getProductUC(fdpCommand, offerId));
				returnMap.put(FDPStepResponseConstants.PRODUCT_USAGE_THRESHOLD, getProductUT(fdpCommand, offerId));
			}

		} catch (final ParseException e) {
			throw new ExecutionFailedException("Expiry Date Can't parse");
		}
		return returnMap;
	}

	/**
	 * Gets the product expiry date.
	 * 
	 * @param fdpCommand
	 *            the fdp command
	 * @param offerId
	 *            the offer id
	 * @return the product expiry date
	 * @throws ParseException
	 *             the parse exception
	 */
	private static Calendar getProductExpiryDate(final FDPCommand fdpCommand, final Long offerId) throws ParseException {

		int i = 0;
		boolean valueFound = false;
		Calendar expiryDate = null;
		while (!valueFound) {
			final String paramPath = ("offerInformation." + i + ".offerID").toLowerCase();
			final CommandParam param = fdpCommand.getOutputParam(paramPath);
			if (param != null) {
				if (((param.getValue().toString()).equals(offerId.toString()))) {
					final String expDatePath = ("offerInformation." + i + ".expiryDate").toLowerCase();
					expiryDate = (Calendar) fdpCommand.getOutputParam(expDatePath).getValue();
					valueFound = true;
				}
			} else {
				valueFound = true;
			}
			i++;
		}
		return expiryDate;
	}

	/**
	 * Gets the product uc.
	 * 
	 * @param fdpCommand
	 *            the fdp command
	 * @param offerId
	 *            the offer id
	 * @return the product uc
	 */
	private static Long getProductUC(final FDPCommand fdpCommand, final Long offerId) {

		int i = 0;
		boolean valueFound = false;
		Long usage = null;
		while (!valueFound) {
			if (fdpCommand.getOutputParam("offerInformation." + i + ".offerID") != null) {

				if (((fdpCommand.getOutputParam("offerInformation." + i + ".offerID").getValue().toString())
						.equals(offerId.toString()))) {
					final Object value = fdpCommand
							.getOutputParam("offerInformation.usageCounterUsageThresholdInformation.usageCounterValue");
					if (value != null) {
						usage = Long.parseLong(value.toString());
						valueFound = true;
					}
				}
			} else {
				valueFound = true;
			}
			i++;
		}
		return usage;
	}

	/**
	 * Gets the product ut.
	 * 
	 * @param fdpCommand
	 *            the fdp command
	 * @param offerId
	 *            the offer id
	 * @return the product ut
	 */
	private static Long getProductUT(final FDPCommand fdpCommand, final Long offerId) {

		int i = 0;
		boolean valueFound = false;
		Long usage = null;
		while (!valueFound) {
			if (fdpCommand.getOutputParam("offerInformation." + i + ".offerID") != null) {

				if (((fdpCommand.getOutputParam("offerInformation." + i + ".offerID").getValue().toString())
						.equals(offerId.toString()))) {
					final Object value = fdpCommand
							.getOutputParam("offerInformation.usageCounterUsageThresholdInformation.usageThresholdInformation.0.usageThresholdValue");
					if (value != null) {
						usage = Long.parseLong(value.toString());
						valueFound = true;
					}
				}
			} else {
				valueFound = true;
			}
			i++;
		}
		return usage;
	}

	/**
	 * Gets the full msisdn.
	 * 
	 * @param subscriberNumber
	 *            the subscriber number
	 * @return the full msisdn
	 * @throws ExecutionFailedException
	 *             the execution failed exception
	 */
	public static String getFullMsisdn(final Long subscriberNumber) throws ExecutionFailedException {
		String msisdnWithCountryCode = null;
		// Appending country code to MSISDN
		final FDPAppBag bag = new FDPAppBag();
		bag.setKey(ConfigurationKey.COUNTRY_CODE.getAttributeName());
		bag.setSubStore(AppCacheSubStore.CONFIGURATION_MAP);
		final FDPCache<FDPAppBag, Object> fdpCache = ApplicationConfigUtil.getApplicationConfigCache();
		final String countryCode = (String) fdpCache.getValue(bag);

		if (countryCode == null) {
			throw new ExecutionFailedException("Country code is null");
		}
		msisdnWithCountryCode = countryCode + subscriberNumber.toString();
		return msisdnWithCountryCode;
	}

	/**
	 * Gets the product.
	 * 
	 * @param circle
	 *            the circle
	 * @param productId
	 *            the product id
	 * @return the product
	 * @throws ExecutionFailedException
	 *             the execution failed exception
	 */
	public static Product getProduct(final FDPCircle circle, final String productId) throws ExecutionFailedException {
		final FDPCache<FDPMetaBag, FDPCacheable> fdpMetaDataCache = ApplicationConfigUtil.getMetaDataCache();
		final FDPCacheable fdpProductCacheable = fdpMetaDataCache.getValue(new FDPMetaBag(circle, ModuleType.PRODUCT,
				productId));
		Product product = null;
		if (fdpProductCacheable instanceof Product) {
			product = (Product) fdpProductCacheable;
		}
		return product;
	}

	/**
	 * This method extract the threshold values based on thresholdId as per the product configuration.
	 * 
	 * @param fdpCommand
	 * @param defaultUnit
	 * @param startIndex
	 * @param statusMap
	 * @param usageThresholdCounterId
	 */
	private static void updateUsageThresholdForId(final FDPCommand fdpCommand, final String defaultUnit,
			final int startIndex, final Map<SharedAccountResponseType, Object> statusMap, final Long usageThresholdCounterId) {
		boolean isThresholdFound = false;
		int startIndexForThreshold = 0;
		boolean valueFound = false;
		while(!valueFound) {
			final CommandParam usageIdPath = fdpCommand.getOutputParam("usageCounterUsageThresholdInformation."
					+ startIndex + ".usageThresholdInformation." + startIndexForThreshold + ".usageThresholdID");

			if (null == usageIdPath ) {
				valueFound = true;
			} else {
				if ((usageIdPath.getValue().toString()).equals(usageThresholdCounterId.toString())) {
					final CommandParam usageValuePath = fdpCommand.getOutputParam("usageCounterUsageThresholdInformation."
							+ startIndex + ".usageThresholdInformation." + startIndexForThreshold + ".usageThresholdValue");
					final CommandParam thresholdUnitObj = fdpCommand.getOutputParam("usageCounterUsageThresholdInformation."
							+ startIndex + ".usageThresholdInformation." + startIndexForThreshold
							+ ".usagethresholdmonetaryvalue1");
					if ((usageValuePath == null) && (thresholdUnitObj == null)) {
						valueFound = true;
					} else {
						final String thresholdValue = (thresholdUnitObj == null) ? usageValuePath.getValue().toString()
								: thresholdUnitObj.getValue().toString();
						statusMap.put(SharedAccountResponseType.STATUS, SharedAccountConstants.SUCCESS);
						statusMap.put(SharedAccountResponseType.CONSUMER_LIMIT, thresholdValue);
						statusMap.put(SharedAccountResponseType.CONSUMER_THRESHOLD_UNIT, defaultUnit);
						valueFound = true;
						isThresholdFound = true;
					}
					
				}
			}
			startIndexForThreshold++;
		}
		
		if(!isThresholdFound) {
			updateFailureForConsumerUsageValue(statusMap);
		}
	}
}