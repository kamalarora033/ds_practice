package com.ericsson.fdp.business.batchjob.sharedAccount.service.impl;

import java.net.Inet4Address;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import javax.annotation.Resource;
import javax.ejb.Stateless;
import javax.inject.Inject;

import org.slf4j.LoggerFactory;

import ch.qos.logback.classic.Logger;

import com.ericsson.fdp.FDPCacheable;
import com.ericsson.fdp.business.batchjob.sharedAccount.UsageObject;
import com.ericsson.fdp.business.batchjob.sharedAccount.service.SharedAccountBatchJobService;
import com.ericsson.fdp.business.command.AbstractCommand;
import com.ericsson.fdp.business.constants.FDPStepResponseConstants;
import com.ericsson.fdp.business.constants.NotificationConstants;
import com.ericsson.fdp.business.enums.AuxRequestParam;
import com.ericsson.fdp.business.enums.Command;
import com.ericsson.fdp.business.exception.NotificationFailedException;
import com.ericsson.fdp.business.request.impl.FDPStepResponseImpl;
import com.ericsson.fdp.business.transaction.TransactionSequenceGeneratorService;
import com.ericsson.fdp.business.util.LoggerUtil;
import com.ericsson.fdp.business.util.NotificationUtil;
import com.ericsson.fdp.business.util.RequestUtil;
import com.ericsson.fdp.business.util.SharedAccountUtil;
import com.ericsson.fdp.business.util.TopNNotificationUtil;
import com.ericsson.fdp.common.constants.FDPConstant;
import com.ericsson.fdp.common.constants.JNDILookupConstant;
import com.ericsson.fdp.common.enums.ChannelType;
import com.ericsson.fdp.common.enums.ModuleType;
import com.ericsson.fdp.common.enums.Status;
import com.ericsson.fdp.common.vo.CircleConfigParamDTO;
import com.ericsson.fdp.common.vo.FDPCircle;
import com.ericsson.fdp.core.cache.AppCacheSubStore;
import com.ericsson.fdp.core.command.FDPCommand;
import com.ericsson.fdp.core.dsm.framework.service.FDPCache;
import com.ericsson.fdp.core.exception.ExecutionFailedException;
import com.ericsson.fdp.core.logging.FDPLogger;
import com.ericsson.fdp.core.logging.FDPLoggerFactory;
import com.ericsson.fdp.core.request.FDPRequest;
import com.ericsson.fdp.core.request.impl.FDPRequestImpl;
import com.ericsson.fdp.core.serviceprovisioning.bean.FDPAppBag;
import com.ericsson.fdp.core.serviceprovisioning.bean.FDPMetaBag;
import com.ericsson.fdp.core.utils.CircleCodeFinder;
import com.ericsson.fdp.dao.dto.batchJob.BatchExecutionRequestDTO;
import com.ericsson.fdp.dao.dto.batchJob.BatchExecutionResponseDTO;
import com.ericsson.fdp.dao.dto.sharedaccount.SharedAccGpDTO;
import com.ericsson.fdp.dao.dto.sharedaccount.SharedAccountConsumerDTO;
import com.ericsson.fdp.dao.dto.sharedaccount.SharedAccountRequestDTO;
import com.ericsson.fdp.dao.enums.ConsumerRequestType;
import com.ericsson.fdp.dao.enums.ConsumerStatusEnum;
import com.ericsson.fdp.dao.enums.ProductAdditionalInfoEnum;
import com.ericsson.fdp.dao.enums.SharedAccountGroupStatus;
import com.ericsson.fdp.dao.enums.SharedAccountResponseType;
import com.ericsson.fdp.dao.enums.StepNameEnum;
import com.ericsson.fdp.dao.enums.appcache.AdminConfigurations;
import com.ericsson.fdp.dao.exception.FDPConcurrencyException;
import com.ericsson.fdp.dao.fdpadmin.FDPConfigurationDAO;
import com.ericsson.fdp.dao.fdpadmin.FDPOtpDAO;
import com.ericsson.fdp.dao.fdpadmin.FDPProductAdditionalInfoDAO;
import com.ericsson.fdp.dao.fdpadmin.FDPSharedAccountConsumerDAO;
import com.ericsson.fdp.dao.fdpadmin.FDPSharedAccountGroupDAO;
import com.ericsson.fdp.dao.fdpadmin.FDPSharedAccountReqDAO;

/**
 * The Class SharedAccountBatchJobServiceImpl.
 */
@Stateless
public class SharedAccountBatchJobServiceImpl implements SharedAccountBatchJobService {

	/** The Constant LOGGER. */
	// Admin log is used instead of circle( batch job is not specific to circle
	private static final org.slf4j.Logger LOGGER = LoggerFactory.getLogger(SharedAccountBatchJobServiceImpl.class);

	/** The configuration dao. */
	@Inject
	private FDPConfigurationDAO configurationDAO;

	/** The meta data cache. */
	@Resource(lookup = JNDILookupConstant.META_DATA_CACHE_JNDI_NAME)
	private FDPCache<FDPMetaBag, FDPCacheable> metaDataCache;

	/** The application config cache. */
	@Resource(lookup = JNDILookupConstant.APPLICATION_CONFIG_CACHE_JNDI_NAME)
	private FDPCache<FDPAppBag, Object> applicationConfigCache;

	/** The shared account group dao. */
	@Inject
	private FDPSharedAccountGroupDAO sharedAccountGroupDAO;

	/** The shared account consumer dao. */
	@Inject
	private FDPSharedAccountConsumerDAO sharedAccountConsumerDAO;

	/** The transaction sequence dao. */
	@Resource(lookup = JNDILookupConstant.TRANSACTION_ID_GENERATOR_SERVICE)
	private TransactionSequenceGeneratorService generatorService;

	/** The shared account request dao. */
	@Inject
	private FDPSharedAccountReqDAO sharedAccountRequestDAO;

	/** The product additional info dao. */
	@Inject
	private FDPProductAdditionalInfoDAO productAdditionalInfoDAO;

	/** The fdp otpdao. */
	@Inject
	private FDPOtpDAO fdpOTPDAO;

	@Override
	public BatchExecutionResponseDTO deleteSharedAccountGroup(final BatchExecutionRequestDTO batchExecutionRequest) {

		final List<SharedAccountRequestDTO> sharedAccountRequestDTOList = sharedAccountRequestDAO
				.getSharedAccountRequestEntityIdByStatusAndRequestType(batchExecutionRequest.getCircle().getCircleId(),
						ConsumerStatusEnum.PENDING, ConsumerRequestType.DELETE_ACCOUNT);
		SharedAccGpDTO sharedAccountGroupDTO = null;
		List<SharedAccountConsumerDTO> sharedAccountConsumers = null;
		Integer failureCount = 0;
		final Logger circleLogger = FDPLoggerFactory.getCircleAdminLogger(batchExecutionRequest.getCircle()
				.getCircleName());
		Boolean isError = false;
		FDPRequestImpl fdpRequest = null;
		for (final SharedAccountRequestDTO requestDTO : sharedAccountRequestDTOList) {

			try {
				isError = false;
				sharedAccountGroupDTO = sharedAccountGroupDAO.getSharedAccountGroupById(requestDTO.getEntityId());

				if (SharedAccountGroupStatus.DORMENT.equals(sharedAccountGroupDTO.getStatus())) {
					sharedAccountConsumers = sharedAccountConsumerDAO
							.getSharedAccountConsumersBySharedAccountId(sharedAccountGroupDTO.getSharedAccID());
					fdpRequest = this.prepareFDPRequestObject(batchExecutionRequest.getCircle(), sharedAccountGroupDTO
							.getGroupProviderMSISDN().toString(), sharedAccountGroupDTO.getOfferId());

					// delete account information for group and consumers
					final Boolean isGroupDelete = this.deleteAccountGroupAndConsumers(sharedAccountGroupDTO,
							sharedAccountConsumers, batchExecutionRequest);

					if (isGroupDelete) {
						FDPLogger.info(circleLogger, this.getClass(), "deleteSharedAccountGroup",
								"Shared Account group deleted successfully, shared account id = "
										+ sharedAccountGroupDTO.getSharedAccID());
						sharedAccountRequestDAO.updateSharedRequestStatus(requestDTO.getSharedAccountReqId(),
								ConsumerStatusEnum.COMPLETED, batchExecutionRequest.getModifiedBy());

						// send notification for success
						final String notificationText = NotificationUtil.createNotificationText(fdpRequest,
								FDPConstant.DELETE_ACCOUNT_NOTIFICATION_SUCCESS_ID, circleLogger);
						NotificationUtil.sendNotification(Long.valueOf(sharedAccountGroupDTO.getGroupProviderMSISDN()),
								ChannelType.SMS, batchExecutionRequest.getCircle(), notificationText,
								fdpRequest.getRequestId(), false);
					} else {
						FDPLogger.error(
								circleLogger,
								this.getClass(),
								"deleteSharedAccountGroup",
								"Shared Account group failed to delete, shared account id = "
										+ sharedAccountGroupDTO.getSharedAccID());
						failureCount++;
						isError = true;
					}
				} else {
					sharedAccountRequestDAO.updateSharedRequestStatus(requestDTO.getSharedAccountReqId(),
							ConsumerStatusEnum.COMPLETED, batchExecutionRequest.getModifiedBy());
					FDPLogger.error(circleLogger, this.getClass(), "deleteSharedAccountGroup",
							"Data corrupted for shared account group " + sharedAccountGroupDTO.getSharedAccID());
				}
			} catch (final ExecutionFailedException e) {
				FDPLogger.error(circleLogger, this.getClass(), "deleteSharedAccountGroup",
						"Failed to delete shared account group, Delete offer command not found in cache", e);
				failureCount++;
				isError = true;
			} catch (final FDPConcurrencyException e) {
				failureCount++;
				FDPLogger.error(circleLogger, this.getClass(), "deleteSharedAccountGroup",
						"Failed to update shared account request status id = " + requestDTO.getSharedAccountReqId(), e);
				isError = true;
			} catch (final NotificationFailedException e) {
				FDPLogger.error(circleLogger, this.getClass(), "deleteSharedAccountGroup",
						"Could not send notification for shared account request " + requestDTO.getSharedAccountReqId(),
						e);
			}
			String notificationText;
			try {
				if (isError) {
					notificationText = NotificationUtil.createNotificationText(fdpRequest,
							FDPConstant.DELETE_ACCOUNT_NOTIFICATION_FAILURE_ID, circleLogger);
				} else {
					notificationText = NotificationUtil.createNotificationText(fdpRequest,
							FDPConstant.DELETE_ACCOUNT_NOTIFICATION_SUCCESS_ID, circleLogger);
				}
				NotificationUtil.sendNotification(Long.valueOf(sharedAccountGroupDTO.getGroupProviderMSISDN()),
						ChannelType.SMS, batchExecutionRequest.getCircle(), notificationText,
						fdpRequest.getRequestId(), false);
			} catch (final NotificationFailedException e) {
				FDPLogger.error(circleLogger, this.getClass(), "deleteSharedAccountGroup",
						"Could not send notification for shared account request " + requestDTO.getSharedAccountReqId(),
						e);
			}
		}

		// initialize batchExecutionResponse object
		final BatchExecutionResponseDTO executionResponse = new BatchExecutionResponseDTO(
				sharedAccountRequestDTOList.size(), failureCount, sharedAccountRequestDTOList.size() - failureCount,
				batchExecutionRequest.getBatchExecutionInfoId(), batchExecutionRequest.getModifiedBy());
		return executionResponse;
	}

	@Override
	public BatchExecutionResponseDTO viewUsageForConsumers(final BatchExecutionRequestDTO batchExecutionRequest) {

		final List<SharedAccountRequestDTO> sharedAccountRequestDTOList = sharedAccountRequestDAO
				.getSharedAccountRequestEntityIdByStatusAndRequestType(batchExecutionRequest.getCircle().getCircleId(),
						ConsumerStatusEnum.PENDING, ConsumerRequestType.TOP_N_USAGE);
		final Logger circleLogger = FDPLoggerFactory.getCircleAdminLogger(batchExecutionRequest.getCircle()
				.getCircleName());
		// get offer id and provider msisdn for shared account request

		final List<Long> productIdList = new ArrayList<Long>();
		final List<Long> senderMsIsdnList = new ArrayList<Long>();

		for (final SharedAccountRequestDTO requestDTO : sharedAccountRequestDTOList) {
			productIdList.add(requestDTO.getEntityId());
			senderMsIsdnList.add(requestDTO.getSenderMsisdn());
		}
		// get product id offer id map
		final Map<Long, Long> productIdOfferIdValueMap = productAdditionalInfoDAO
				.getSharedAccountAdditionalInfoByProductIds(productIdList,
						ProductAdditionalInfoEnum.SHARED_ACC_OFFER_ID);

		Integer failureCount = 0;

		for (final SharedAccountRequestDTO requestDTO : sharedAccountRequestDTOList) {

			try {
				final String requestId = "BATCH_" + Inet4Address.getLocalHost().getHostAddress() + "_"
						+ (String.valueOf(UUID.randomUUID()));
				final Long offerId = productIdOfferIdValueMap.get(requestDTO.getEntityId());
				// get consumer list
				final List<SharedAccountConsumerDTO> consumerDTOList = this.getConsumersByOfferIdAndProviderMsIsdn(
						requestDTO.getSenderMsisdn(), offerId);
				// calculate top n usage
				final List<UsageObject> topNValues = this.getTopNUsage(requestDTO.getSenderMsisdn(),
						batchExecutionRequest.getCircle(), consumerDTOList,
						Integer.valueOf(requestDTO.getConsumerAddInfo()), requestId);
				FDPLogger.info(
						circleLogger,
						this.getClass(),
						"viewUsageForConsumers",
						"Provider= " + requestDTO.getSenderMsisdn() + " offer id = " + offerId + " top "
								+ requestDTO.getConsumerAddInfo() + "consumer usage = " + topNValues);

				final FDPRequestImpl fdpRequestImpl = SharedAccountUtil.getFDPRequestForTopNUsage(
						requestDTO.getSenderMsisdn(), null, batchExecutionRequest.getCircle());
				fdpRequestImpl.setRequestId(requestId);
				final String notificationText = TopNNotificationUtil.createNotificationText(fdpRequestImpl,
						FDPConstant.TOPNUSAGE_FINAL_NOT_ID, LoggerUtil.getSummaryLoggerFromRequest(fdpRequestImpl),
						topNValues);
				NotificationUtil.sendNotification(requestDTO.getSenderMsisdn(), ChannelType.SMS,
						batchExecutionRequest.getCircle(), notificationText, fdpRequestImpl.getRequestId(), false);

				// update status to complete
				sharedAccountRequestDAO.updateSharedRequestStatus(requestDTO.getSharedAccountReqId(),
						ConsumerStatusEnum.COMPLETED, batchExecutionRequest.getModifiedBy());

			} catch (final FDPConcurrencyException e) {
				failureCount++;
				FDPLogger.error(circleLogger, this.getClass(), "viewUsageForConsumers",
						"Failed to update shared account request status id = " + requestDTO.getSharedAccountReqId(), e);
			} catch (final ExecutionFailedException e) {
				failureCount++;
				FDPLogger.error(
						circleLogger,
						this.getClass(),
						"viewUsageForConsumers",
						"Failed to get top n usage, shared account request status id = "
								+ requestDTO.getSharedAccountReqId(), e);
			} catch (final NotificationFailedException e) {
				failureCount++;
				FDPLogger.error(
						circleLogger,
						this.getClass(),
						"viewUsageForConsumers",
						"Failed to create notification on shared account request status id = "
								+ requestDTO.getSharedAccountReqId(), e);
			} catch (final UnknownHostException e) {
				failureCount++;
				FDPLogger.error(
						circleLogger,
						this.getClass(),
						"viewUsageForConsumers",
						"Failed to create requestid on shared account request status id = "
								+ requestDTO.getSharedAccountReqId(), e);
			}
		}
		// initialize response object
		final BatchExecutionResponseDTO responseDTO = new BatchExecutionResponseDTO(sharedAccountRequestDTOList.size(),
				failureCount, sharedAccountRequestDTOList.size() - failureCount,
				batchExecutionRequest.getBatchExecutionInfoId(), batchExecutionRequest.getModifiedBy());
		return responseDTO;
	}

	/**
	 * Delete account group and consumers.
	 * 
	 * @param sharedAccountGroupDTO
	 *            sharedAccountGroupDTO
	 * @param sharedAccountConsumers
	 *            the shared account consumers
	 * @param batchExecutionRequest
	 *            the batch execution request
	 * @return the boolean
	 * @throws ExecutionFailedException
	 *             the execution failed exception
	 */
	private Boolean deleteAccountGroupAndConsumers(final SharedAccGpDTO sharedAccountGroupDTO,
			final List<SharedAccountConsumerDTO> sharedAccountConsumers,
			final BatchExecutionRequestDTO batchExecutionRequest) throws ExecutionFailedException {

		final List<Long> failedSharedAccountConsumers = new ArrayList<Long>();
		final Logger circleLogger = FDPLoggerFactory.getCircleAdminLogger(batchExecutionRequest.getCircle()
				.getCircleName());
		Boolean isGroupDelete = this.executeDeleteOffer(sharedAccountGroupDTO, sharedAccountConsumers,
				batchExecutionRequest.getCircle(), failedSharedAccountConsumers);

		// remove all consumers
		for (final SharedAccountConsumerDTO consumerDTO : sharedAccountConsumers) {
			try {
				if (!failedSharedAccountConsumers.contains(consumerDTO.getSharedAccountConsumerId())) {
					sharedAccountConsumerDAO.deleteSharedAccountConsumer(consumerDTO.getSharedAccountConsumerId());
				}
			} catch (final FDPConcurrencyException e) {
				failedSharedAccountConsumers.add(consumerDTO.getSharedAccountConsumerId());
				FDPLogger.error(circleLogger, this.getClass(), "deleteAccountGroupAndConsumers",
						"Failed to delete shared account consumer, " + consumerDTO.getSharedAccountConsumerId()
								+ "Concurrency error occured.", e);
				if (isGroupDelete) {
					isGroupDelete = false;
				}
			}
		}

		// remove group
		try {
			if (isGroupDelete) {
				// set parent to null for sub group
				sharedAccountGroupDAO.updateSharedAccountGroupParent(sharedAccountGroupDTO.getSharedAccID(), null,
						batchExecutionRequest.getModifiedBy());
				// remove group when all the consumers are deleted
				sharedAccountGroupDAO.deleteSharedAccGroupById(sharedAccountGroupDTO.getSharedAccID());
			}
		} catch (final FDPConcurrencyException e) {
			FDPLogger.error(circleLogger, this.getClass(), "deleteAccountGroupAndConsumers",
					"Failed to delete shared account group," + sharedAccountGroupDTO.getSharedAccID()
							+ " Concurrency error occured.", e);
			isGroupDelete = false;
		}
		return isGroupDelete;
	}

	/**
	 * Execute delete offer.
	 * 
	 * @param sharedAccountGroupDTO
	 *            the shared account group dto
	 * @param sharedAccountConsumers
	 *            the shared account consumers
	 * @param fdpCircle
	 *            the fdp circle
	 * @param failedSharedAccountConsumers
	 *            the failed shared account consumers
	 * @return the boolean
	 * @throws ExecutionFailedException
	 *             the execution failed exception
	 */
	private Boolean executeDeleteOffer(final SharedAccGpDTO sharedAccountGroupDTO,
			final List<SharedAccountConsumerDTO> sharedAccountConsumers, final FDPCircle fdpCircle,
			final List<Long> failedSharedAccountConsumers) throws ExecutionFailedException {
		final Logger circleLogger = FDPLoggerFactory.getCircleAdminLogger(fdpCircle.getCircleName());
		boolean executed = false;
		Boolean isDeleteGroup = true;
		FDPCommand fdpCommand = null;
		final FDPCircle fdpCircleForCommand = new FDPCircle(-1L, "ALL", "ALL");
		final FDPCacheable fdpCommandCached = metaDataCache.getValue(new FDPMetaBag(fdpCircleForCommand,
				ModuleType.COMMAND, Command.DELETE_OFFER_FOR_BATCH_JOB.getCommandDisplayName()));
		if (fdpCommandCached instanceof FDPCommand) {
			fdpCommand = (FDPCommand) fdpCommandCached;
			((AbstractCommand) fdpCommand).setCommandDisplayName(Command.DELETEOFFER.getCommandDisplayName());
		}

		if (fdpCommand != null) {

			// execute command on consumers offers
			for (final SharedAccountConsumerDTO consumerDTO : sharedAccountConsumers) {
				final FDPRequestImpl fdpRequest = this.prepareFDPRequestObject(fdpCircle, consumerDTO
						.getConsumerMsisdn().toString(), consumerDTO.getOfferId());
				executed = Status.SUCCESS.equals(fdpCommand.execute(fdpRequest));
				if (!executed) {
					failedSharedAccountConsumers.add(consumerDTO.getSharedAccountConsumerId());
					if (isDeleteGroup) {
						isDeleteGroup = false;
					}
					FDPLogger.error(
							circleLogger,
							this.getClass(),
							"executeDeleteOffer",
							"Failed to execute delete offer command on consumer "
									+ consumerDTO.getSharedAccountConsumerId());
				}
			}

			// execute command on group
			if (isDeleteGroup) {
				final FDPRequestImpl fdpRequest = this.prepareFDPRequestObject(fdpCircle, sharedAccountGroupDTO
						.getGroupProviderMSISDN().toString(), sharedAccountGroupDTO.getOfferId());
				executed = Status.SUCCESS.equals(fdpCommand.execute(fdpRequest));
				if (!executed) {
					FDPLogger
							.error(circleLogger,
									this.getClass(),
									"executeDeleteOffer",
									"Failed to execute delete offer command on group "
											+ sharedAccountGroupDTO.getSharedAccID());
					isDeleteGroup = false;
				}
			}
		} else {
			FDPLogger.error(circleLogger, this.getClass(), "executeDeleteOffer",
					"Failed to get delete offer command from cache ");
			throw new ExecutionFailedException("Failed to get delete offer command from cache ");
		}
		return isDeleteGroup;
	}

	/**
	 * Prepare fdp request object.
	 * 
	 * @param fdpCircle
	 *            the fdp circle
	 * @param msIsdn
	 *            the ms isdn
	 * @param offerId
	 *            the offer id
	 * @return the fDP request impl
	 * @throws ExecutionFailedException
	 *             the execution failed exception
	 */
	private FDPRequestImpl prepareFDPRequestObject(final FDPCircle fdpCircle, final String msIsdn, final Long offerId)
			throws ExecutionFailedException {
		final FDPRequestImpl fdpRequest = new FDPRequestImpl();
		fdpRequest.setChannel(ChannelType.SHARED_ACCOUNT_BATCH);
		fdpRequest.setRequestId(NotificationConstants.PAM_REQUESTED_ID + (String.valueOf(UUID.randomUUID())));
		fdpRequest.setCircle(fdpCircle);
		// set circle parameters
		final CircleConfigParamDTO circleConfigParamDTO = RequestUtil.populateCircleConfigParamDTO(msIsdn, fdpCircle);
		fdpRequest.setOriginHostName(circleConfigParamDTO.getOriginHostName());
		fdpRequest.setOriginNodeType(circleConfigParamDTO.getOriginNodeType());
		fdpRequest.setSubscriberNumber(circleConfigParamDTO.getSubscriberNumber());
		fdpRequest.setIncomingSubscriberNumber(circleConfigParamDTO.getIncomingSubscriberNumber());
		fdpRequest.setSubscriberNumberNAI(circleConfigParamDTO.getSubscriberNumberNAI());
		fdpRequest.setOriginTransactionID(generateTransactionId());
		fdpRequest.putAuxiliaryRequestParameter(AuxRequestParam.OFFER_ID, offerId);
		return fdpRequest;
	}

	/**
	 * This method get consumers based on provider msisdn and offer id.
	 * 
	 * @param providerMsisdn
	 *            provider msIsdn
	 * @param offerId
	 *            offer id
	 * @return list of shared account consumer DTO
	 */
	private List<SharedAccountConsumerDTO> getConsumersByOfferIdAndProviderMsIsdn(final Long providerMsisdn,
			final Long offerId) {
		List<SharedAccountConsumerDTO> consumerDTOList = null;
		final List<SharedAccGpDTO> sharedAccountGroupDTOList = sharedAccountGroupDAO.getSharedAccGroup(providerMsisdn,
				offerId, SharedAccountGroupStatus.ACTIVE);
		if (!sharedAccountGroupDTOList.isEmpty()) {
			consumerDTOList = sharedAccountConsumerDAO
					.getSharedAccountConsumersBySharedAccountId(sharedAccountGroupDTOList.get(FDPConstant.ZERO)
							.getSharedAccID());
		} else {
			consumerDTOList = new ArrayList<SharedAccountConsumerDTO>();
		}
		return consumerDTOList;
	}

	/**
	 * This method calculated top N usage among all the consumers for a
	 * provider.
	 * 
	 * @param providerMsisdn
	 *            the provider msisdn
	 * @param fdpCircle
	 *            the fdp circle
	 * @param consumerDTOList
	 *            consumer DTO List
	 * @param n
	 *            the n
	 * @param fdpRequest
	 *            the fdp request
	 * @return list for top N usage of consumers
	 * @throws ExecutionFailedException
	 *             the execution failed exception
	 */
	private List<UsageObject> getTopNUsage(final Long providerMsisdn, final FDPCircle fdpCircle,
			final List<SharedAccountConsumerDTO> consumerDTOList, final Integer n, final String requestId)
			throws ExecutionFailedException {
		final List<UsageObject> consumerUsageValues = new ArrayList<UsageObject>();
		Long usageAmount = 0L;
		final Long transactionId = generateTransactionId();
		if (!consumerDTOList.isEmpty()) {
			for (final SharedAccountConsumerDTO consumerDTO : consumerDTOList) {
				final FDPRequestImpl fdpRequest = SharedAccountUtil.getFDPRequestForTopNUsage(providerMsisdn, null,
						fdpCircle);
				fdpRequest.setOriginTransactionID(transactionId);
				fdpRequest.putAuxiliaryRequestParameter(AuxRequestParam.CONSUMER_MSISDN,
						consumerDTO.getConsumerMsisdn());
				fdpRequest.setRequestId(requestId);
				final Map<SharedAccountResponseType, Object> statusMap = SharedAccountUtil.getUsageValueForConsumer(
						providerMsisdn, Long.valueOf(consumerDTO.getConsumerMsisdn()),
						consumerDTO.getConsumerThresholdUnit(), fdpRequest, true);
				statusMap.put(SharedAccountResponseType.CONSUMER_MSISDN, consumerDTO.getConsumerMsisdn());
				if (statusMap.get(SharedAccountResponseType.STATUS) != null
						&& ((Boolean) statusMap.get(SharedAccountResponseType.STATUS))) {
					usageAmount = Long.valueOf(statusMap.get(SharedAccountResponseType.CONSUMER_LIMIT).toString());
					final FDPStepResponseImpl fdpStepResponseImpl = new FDPStepResponseImpl();
					fdpStepResponseImpl.addStepResponseValue(FDPStepResponseConstants.STATUS_KEY, false);
					for (final Map.Entry<SharedAccountResponseType, Object> entrySet : statusMap.entrySet()) {
						fdpStepResponseImpl.addStepResponseValue(entrySet.getKey().name(), entrySet.getValue());
					}
					RequestUtil.putStepResponseInRequest(fdpStepResponseImpl, fdpRequest,
							StepNameEnum.VALIDATION_STEP.getValue());
					try {
						consumerUsageValues.add(new UsageObject(createNotificationTextForTopNUsage(fdpRequest),
								usageAmount));
					} catch (final NotificationFailedException e) {
						throw new ExecutionFailedException("Unable to create notification value for consumer "
								+ consumerDTO.getConsumerMsisdn(), e);
					}
				} else {
					throw new ExecutionFailedException("Unable to fetch usage value for consumer "
							+ consumerDTO.getConsumerMsisdn());
				}
			}
		}
		Collections.sort(consumerUsageValues, Collections.reverseOrder());
		if (consumerUsageValues.size() > n) {
			return consumerUsageValues.subList(0, n);
		} else {
			return consumerUsageValues;
		}

	}

	@Override
	public BatchExecutionResponseDTO deleteExpiredOTP(final BatchExecutionRequestDTO batchExecutionRequest) {

		final List<Long> otpIdList = fdpOTPDAO.getExpiredOTP(getExpiryParamByConfigKey());
		LOGGER.info("Batch Job running, OTP fetched are " + otpIdList);
		// update shared account request status to expired
		if (!otpIdList.isEmpty()) {
			fdpOTPDAO.deleteExpiredOTP(otpIdList);
			LOGGER.info(" Expired OTP deleted successfully by batch job, batch job info id = "
					+ batchExecutionRequest.getBatchExecutionInfoId());
		}
		final BatchExecutionResponseDTO executionResponse = new BatchExecutionResponseDTO(otpIdList.size(),
				FDPConstant.ZERO, otpIdList.size(), batchExecutionRequest.getBatchExecutionInfoId(),
				batchExecutionRequest.getModifiedBy());
		return executionResponse;
	}

	@Override
	public BatchExecutionResponseDTO updateSharedAccountRequestStatus(final BatchExecutionRequestDTO executionRequestDTO) {

		// fetch list of shared Account request id(s)
		final List<Long> sharedAccountRequestIdList = sharedAccountRequestDAO
				.getSharedAccountRequestForExpiry(executionRequestDTO.getCircle().getCircleId());
		final Logger circleLogger = FDPLoggerFactory.getCircleAdminLogger(executionRequestDTO.getCircle()
				.getCircleName());
		FDPLogger.info(circleLogger, this.getClass(), "deleteSharedAccountGroup",
				"Batch Job running, shared account request fetched are " + sharedAccountRequestIdList);
		final BatchExecutionResponseDTO executionResponse = new BatchExecutionResponseDTO();
		executionResponse.setTotalBatchCount(sharedAccountRequestIdList.size());

		// update shared account request status to expired
		try {
			if (!sharedAccountRequestIdList.isEmpty()) {
				sharedAccountRequestDAO.updateSharedAccountRequestStatus(sharedAccountRequestIdList,
						ConsumerStatusEnum.EXPIRED.getStatusCode());
				FDPLogger.info(circleLogger, this.getClass(), "updateSharedAccountRequestStatus",
						"Shared Account request status updated successfully by batch job, batch job info id = "
								+ executionRequestDTO.getBatchExecutionInfoId());
			}
			executionResponse.setFailureCount(FDPConstant.ZERO);
			executionResponse.setSuccessCount(sharedAccountRequestIdList.size());
		} catch (final FDPConcurrencyException e) {
			FDPLogger.error(circleLogger, this.getClass(), "updateSharedAccountRequestStatus",
					"Failed to update shared account request status to expired", e);
			executionResponse.setFailureCount(sharedAccountRequestIdList.size());
			executionResponse.setSuccessCount(FDPConstant.ZERO);
		}
		executionResponse.setBatchExecutionInfoId(executionRequestDTO.getBatchExecutionInfoId());
		executionResponse.setModifiedBy(executionRequestDTO.getModifiedBy());
		return executionResponse;
	}

	@Override
	public BatchExecutionResponseDTO upgradeConsumerLimitToOldVal(final BatchExecutionRequestDTO executionRequestDTO) {

		final Logger circleLogger = FDPLoggerFactory.getCircleAdminLogger(executionRequestDTO.getCircle()
				.getCircleName());
		final List<SharedAccountConsumerDTO> sharedAccountConsumerList = this
				.getConsumersListForLimitUpgradationForCircle(executionRequestDTO.getCircle());
		Integer failureCount = 0;
		for (final SharedAccountConsumerDTO consumerDTO : sharedAccountConsumerList) {
			try {

				FDPCommand fdpCommand = null;
				final FDPCircle fdpCircleForCommand = new FDPCircle(-1L, "ALL", "ALL");
				final FDPCacheable fdpCommandCached = metaDataCache.getValue(new FDPMetaBag(fdpCircleForCommand,
						ModuleType.COMMAND, Command.UPDATE_USAGE_THRESHOLDS_AND_COUNTERS_FOR_BATCH_JOB
								.getCommandDisplayName()));
				if (fdpCommandCached instanceof FDPCommand) {
					fdpCommand = (FDPCommand) fdpCommandCached;
					((AbstractCommand) fdpCommand).setCommandDisplayName(Command.UPDATE_USAGE_THRESHOLDS_AND_COUNTERS
							.getCommandDisplayName());
				}
				Boolean isSuccess = Boolean.FALSE;
				if (fdpCommand != null) {
					final FDPRequestImpl fdpRequest = this.createFDPRequest(consumerDTO.getGroupMsisdn());
					fdpRequest.putAuxiliaryRequestParameter(AuxRequestParam.VALID_ID,
							consumerDTO.getDefaultThresholdCounterId());
					fdpRequest.putAuxiliaryRequestParameter(AuxRequestParam.CONSUMER_LIMIT_UPGRADE_VALUE,
							consumerDTO.getConsumerLimit());
					fdpRequest.putAuxiliaryRequestParameter(AuxRequestParam.CONSUMER_MSISDN,
							consumerDTO.getConsumerMsisdn());
					isSuccess = Status.SUCCESS.equals(fdpCommand.execute(fdpRequest));
				}
				if (isSuccess) {
					sharedAccountConsumerDAO.upgradeConsumerLimitToOld(consumerDTO.getSharedAccountConsumerId(),
							executionRequestDTO.getModifiedBy());
				} else {
					failureCount++;
					FDPLogger.error(
							circleLogger,
							this.getClass(),
							"upgradeConsumerLimitToOldVal",
							"Failed to update consumer limit to old value, consumer id = "
									+ consumerDTO.getSharedAccountConsumerId());
				}
			} catch (final FDPConcurrencyException e) {
				failureCount++;
				FDPLogger.error(
						circleLogger,
						this.getClass(),
						"upgradeConsumerLimitToOldVal",
						"Failed to update consumer limit to old value, consumer id = "
								+ consumerDTO.getSharedAccountConsumerId(), e);
			} catch (final ExecutionFailedException e) {
				failureCount++;
				FDPLogger.error(
						circleLogger,
						this.getClass(),
						"upgradeConsumerLimitToOldVal",
						"Failed to update consumer limit to old value, consumer id = "
								+ consumerDTO.getSharedAccountConsumerId(), e);
			}
		}
		final BatchExecutionResponseDTO executionResponse = new BatchExecutionResponseDTO(
				sharedAccountConsumerList.size(), failureCount, sharedAccountConsumerList.size() - failureCount,
				executionRequestDTO.getBatchExecutionInfoId(), executionRequestDTO.getModifiedBy());
		return executionResponse;
	}

	@Override
	public BatchExecutionResponseDTO deleteSharedAccountExpiredReq(final BatchExecutionRequestDTO executionRequestDTO) {
		final List<Long> sharedAccountReqIdList = sharedAccountRequestDAO
				.getSharedAccountReqExpired(getExpiryParamByConfigKey());
		LOGGER.info("Batch Job running, shared Account request fetched are " + sharedAccountReqIdList);
		// update shared account request status to expired
		if (!sharedAccountReqIdList.isEmpty()) {
			sharedAccountRequestDAO.deleteSharedAccountExpiredReq(sharedAccountReqIdList);
			LOGGER.info(" Expired shared account request deleted successfully by batch job, batch job info id = "
					+ executionRequestDTO.getBatchExecutionInfoId());
		}
		final BatchExecutionResponseDTO executionResponse = new BatchExecutionResponseDTO(
				sharedAccountReqIdList.size(), FDPConstant.ZERO, sharedAccountReqIdList.size(),
				executionRequestDTO.getBatchExecutionInfoId(), executionRequestDTO.getModifiedBy());
		return executionResponse;
	}

	/**
	 * Creates the notification text for top n usage.
	 * 
	 * @param fdpRequest
	 *            the fdp request
	 * @return the string
	 * @throws NotificationFailedException
	 *             the notification failed exception
	 */
	private String createNotificationTextForTopNUsage(final FDPRequest fdpRequest) throws NotificationFailedException {
		return NotificationUtil.createNotificationText(fdpRequest, FDPConstant.TOPNUSAGE_NOT_ID,
				LoggerUtil.getSummaryLoggerFromRequest(fdpRequest));
	}

	/**
	 * Gets the consumers list for limit upgradation for circle.
	 * 
	 * @param fdpCircle
	 *            the fdp circle
	 * @return the consumers list for limit upgradation for circle
	 */
	private List<SharedAccountConsumerDTO> getConsumersListForLimitUpgradationForCircle(final FDPCircle fdpCircle) {
		final String batchJobCircle = fdpCircle.getCircleCode();
		final List<SharedAccountConsumerDTO> sharedAccountConsumerList = sharedAccountConsumerDAO
				.getConsumersForUpgradeReqExpired();
		final List<SharedAccountConsumerDTO> sharedAccountConsumerListCircle = new ArrayList<SharedAccountConsumerDTO>();
		for (final SharedAccountConsumerDTO consumer : sharedAccountConsumerList) {
			final String circleCode = CircleCodeFinder.getCircleCode(consumer.getConsumerMsisdn().toString(),
					applicationConfigCache);
			if (circleCode.equals(batchJobCircle)) {
				sharedAccountConsumerListCircle.add(consumer);
			}
		}
		return sharedAccountConsumerListCircle;
	}

	/**
	 * Creates the fdp request.
	 * 
	 * @param providerMsisdn
	 *            the provider msisdn
	 * @return the fDP request impl
	 * @throws ExecutionFailedException
	 *             the execution failed exception
	 */
	private FDPRequestImpl createFDPRequest(final Long providerMsisdn) throws ExecutionFailedException {
		final FDPCircle fdpCircle = getFDPCircleByMsisdn(providerMsisdn);
		final CircleConfigParamDTO circleConfigParamDTO = RequestUtil.populateCircleConfigParamDTO(
				providerMsisdn.toString(), fdpCircle);
		final FDPRequestImpl fdpRequest = new FDPRequestImpl();
		fdpRequest.setOriginHostName(circleConfigParamDTO.getOriginHostName());
		fdpRequest.setOriginNodeType(circleConfigParamDTO.getOriginNodeType());
		fdpRequest.setSubscriberNumber(circleConfigParamDTO.getSubscriberNumber());
		fdpRequest.setIncomingSubscriberNumber(circleConfigParamDTO.getIncomingSubscriberNumber());
		fdpRequest.setSubscriberNumberNAI(circleConfigParamDTO.getSubscriberNumberNAI());
		fdpRequest.setChannel(ChannelType.SHARED_ACCOUNT_BATCH);
		fdpRequest.setCircle(fdpCircle);
		fdpRequest.setOriginTransactionID(generateTransactionId());
		return fdpRequest;
	}

	/**
	 * Gets the fDP circle by msisdn.
	 * 
	 * @param providerMsisdn
	 *            the provider msisdn
	 * @return the fDP circle by msisdn
	 */
	private FDPCircle getFDPCircleByMsisdn(final Long providerMsisdn) {
		final String circleCode = CircleCodeFinder.getCircleCode(providerMsisdn.toString(), applicationConfigCache);
		final FDPAppBag appBag = new FDPAppBag();
		appBag.setKey(circleCode);
		appBag.setSubStore(AppCacheSubStore.CIRCLE_CODE_CIRCLE_NAME_MAP);
		final FDPCircle fdpCircle = (FDPCircle) applicationConfigCache.getValue(appBag);
		return fdpCircle;
	}

	/**
	 * Gets the expiry param by config key.
	 * 
	 * @return the expiry param by config key
	 */
	private Calendar getExpiryParamByConfigKey() {

		final FDPAppBag appBag = new FDPAppBag();
		appBag.setSubStore(AppCacheSubStore.CONFIGURATION_MAP);
		appBag.setKey(AdminConfigurations.OTP_EXPIRY_TIME_MINUTES.getKey());
		Integer deleteTime;
		final Object delTime = applicationConfigCache.getValue(appBag);
		if (delTime != null) {
			deleteTime = Integer.parseInt(delTime.toString());
		} else {
			deleteTime = Integer.parseInt(configurationDAO.getFDPConfigurationByName(
					AdminConfigurations.OTP_EXPIRY_TIME_MINUTES.getKey()).getAttributeValue());
		}

		// subtract leased time (minutes) from current date to get EXPIRY DATE
		// PARAM
		final Calendar expiryDateParam = Calendar.getInstance();
		expiryDateParam.add(Calendar.MINUTE, FDPConstant.MINUS_ONE_LONG.intValue() * deleteTime);
		return expiryDateParam;
	}
	
	/**
	 * This method is used to generate the transaction id to be used.
	 * 
	 * @return the transaction id.
	 */
	private Long generateTransactionId() {
		return generatorService.generateTransactionId();
	}


}
