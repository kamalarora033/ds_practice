package com.ericsson.fdp.business.batchjob.emabatch.impl;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ThreadLocalRandom;

import javax.annotation.Resource;
import javax.ejb.Stateless;
import javax.inject.Inject;
import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ericsson.fdp.business.batchjob.emabatch.EMABatchService;
import com.ericsson.fdp.business.constants.FDPStepResponseConstants;
import com.ericsson.fdp.business.dto.emabatch.EMABatchRecordDTO;
import com.ericsson.fdp.business.enums.EMABatchStatus;
import com.ericsson.fdp.business.enums.ServiceStepOptions;
import com.ericsson.fdp.business.step.execution.FDPExecutionService;
import com.ericsson.fdp.business.transaction.TransactionSequenceGeneratorService;
import com.ericsson.fdp.business.util.RequestUtil;
import com.ericsson.fdp.common.constants.FDPConstant;
import com.ericsson.fdp.common.constants.JNDILookupConstant;
import com.ericsson.fdp.common.dto.TrapError;
import com.ericsson.fdp.common.enums.ChannelType;
import com.ericsson.fdp.common.enums.trap.TrapErrorCodes;
import com.ericsson.fdp.common.enums.trap.TrapSeverity;
import com.ericsson.fdp.common.logging.Event;
import com.ericsson.fdp.common.vo.FDPCircle;
import com.ericsson.fdp.core.cache.AppCacheSubStore;
import com.ericsson.fdp.core.config.utils.PropertyUtils;
import com.ericsson.fdp.core.dsm.framework.service.FDPCache;
import com.ericsson.fdp.core.exception.ExecutionFailedException;
import com.ericsson.fdp.core.logging.FDPLogger;
import com.ericsson.fdp.core.logging.FDPLoggerFactory;
import com.ericsson.fdp.core.request.FDPRequest;
import com.ericsson.fdp.core.request.FDPStepResponse;
import com.ericsson.fdp.core.request.impl.FDPRequestImpl;
import com.ericsson.fdp.core.serviceprovisioning.bean.FDPAppBag;
import com.ericsson.fdp.core.utils.SNMPUtil;
import com.ericsson.fdp.dao.dto.batchJob.BatchExecutionResponseDTO;
import com.ericsson.fdp.dao.enums.appcache.AdminConfigurations;
import com.ericsson.fdp.dao.fdpbusiness.TransactionSequenceDAO;
import com.google.gson.Gson;

/**
 * The Class EMABatchServiceImpl.
 * 
 * @author Ericsson
 */
@Stateless
public class EMABatchServiceImpl implements EMABatchService {

	/** The Constant LOGGER. */
	private static final Logger LOGGER = LoggerFactory.getLogger(EMABatchServiceImpl.class);

	/** The Constant FAILED_FILE_NAME_KEY. */
	private final static String FAILED_RECORDS_FILE_NAME_KEY = "EMA.BATCH.FAILED.RECORDS.FILE.NAME";

	/** The transaction sequence dao. */
	@Resource(lookup = JNDILookupConstant.TRANSACTION_ID_GENERATOR_SERVICE)
	private TransactionSequenceGeneratorService generatorService;

	/** The fdp default ema service. */
	private FDPExecutionService fdpDefaultEMAService;

	/***/
	@Override
	public BatchExecutionResponseDTO executeEMACommands(final List<String> fileRecords, final FDPCircle fdpCircle,
			final String tempFilePath) throws ExecutionFailedException {
		LOGGER.debug(" | {} | executeEMACommands() | fileRecords.size = {}, tempFilePath = {}, circle = {}",
				new Object[] { this.getClass(), fileRecords.size(), tempFilePath, fdpCircle });
		BatchExecutionResponseDTO batchResponse = null;
		final Integer totalRecords = fileRecords.size();
		Integer success = 0;
		Integer fail = 0;
		Integer failedRetryNeeded = 0;
		Integer failedMaxRetriesDone = 0;
		if (!fileRecords.isEmpty()) {
			final Integer emaBatchMaxRetries = getEMABatchMaxRetries();
			try {
				if (fdpDefaultEMAService == null) {
					final Context initialContext = new InitialContext();
					this.fdpDefaultEMAService = (FDPExecutionService) initialContext
							.lookup("java:module/FDPBatchEMAServiceImpl");
				}
				final List<String> updatedRecords = new ArrayList<String>();
				final List<String> failedRecords = new ArrayList<String>();
				for (final String record : fileRecords) {
					final EMABatchRecordDTO emaBatchRecord = getEMABatchRecord(record);
					final Map<ServiceStepOptions, Object> additionalMap = getServiceStepOptionsMap(emaBatchRecord);
					final FDPRequest fdpRequest = getFDPServiceRequest(emaBatchRecord.getMsisdn());
					LOGGER.debug(" | {} | executeEMACommands() | executing record = {}.",
							new Object[] { this.getClass(), record });
					final FDPStepResponse response = fdpDefaultEMAService.executeService(fdpRequest, additionalMap);
					LOGGER.debug(" | {} | executeEMACommands() | response = {}, record = {}.",
							new Object[] { this.getClass(), response, record });
					final EMABatchStatus status = processResponse(response, emaBatchRecord.getBatchCounter(), record,
							emaBatchMaxRetries, fdpCircle);
					LOGGER.debug(" | {} | executeEMACommands() | responseStatus = {}.", new Object[] { this.getClass(),
							status });
					switch (status) {
					case SUCCESS:
						success++;
						break;
					case RETRY:
						fail++;
						failedRetryNeeded++;
						break;
					case FAILED_MAX_RETRY_REACHED:
						failedMaxRetriesDone++;
					case FAILED:
						fail++;
						failedRecords.add(record);
						break;
					default:
						break;
					}
					updatedRecords.add(updateRecordStatus(record, status));
				}
				/*if (failedMaxRetriesDone > 0) {
					FDPLoggerFactory.getGenerateAlarmLogger().warn(
							"",
							new Event(TrapSeverity.CRITICAL, new TrapError(TrapErrorCodes.EMA_MAX_RETRY_FAIL, Arrays
									.asList(fdpCircle.getCircleName())), SNMPUtil.getIPAddess()));
				}*/
				updateRecordsOfTempFile(tempFilePath, updatedRecords);
				writeFailedToFile(failedRecords, fdpCircle);
			} catch (final NamingException e) {
				throw new ExecutionFailedException(e.getMessage(), e);
			}
		}
		batchResponse = new BatchExecutionResponseDTO(totalRecords, fail, success, null, null);
		batchResponse.setRetryCount(failedRetryNeeded);
		return batchResponse;
	}

	/**
	 * Gets the eMA batch max retries.
	 * 
	 * @return the eMA batch max retries
	 * @throws ExecutionFailedException
	 *             the execution failed exception
	 */
	@SuppressWarnings("unchecked")
	private Integer getEMABatchMaxRetries() throws ExecutionFailedException {
		Integer emaBatchMaxRetries = null;
		FDPCache<FDPAppBag, Object> applicationConfigCache = null;
		try {
			final Context initialContext = new InitialContext();
			applicationConfigCache = (FDPCache<FDPAppBag, Object>) initialContext
					.lookup("java:app/fdpCoreServices-1.0/ApplicationConfigCache");
			final String key = AdminConfigurations.EMA_BATCH_MAX_RETRY_COUNTER.getKey();
			final FDPAppBag appBag = new FDPAppBag();
			appBag.setSubStore(AppCacheSubStore.CONFIGURATION_MAP);
			appBag.setKey(key);
			final Object value = applicationConfigCache.getValue(appBag);
			emaBatchMaxRetries = Integer.valueOf(value.toString());
		} catch (final NamingException e) {
			throw new ExecutionFailedException(e.getMessage(), e);
		}
		return emaBatchMaxRetries;
	}

	/**
	 * Process response.
	 * 
	 * @param response
	 *            the response
	 * @param batchCounter
	 *            the batch counter
	 * @param record
	 *            the record
	 * @param emaBatchMaxRetries
	 *            the ema batch max retries
	 * @param fdpCircle
	 *            the fdp circle
	 * @return the boolean
	 * @throws ExecutionFailedException
	 *             the execution failed exception
	 */
	private EMABatchStatus processResponse(final FDPStepResponse response, final Integer batchCounter,
			final String record, final Integer emaBatchMaxRetries, final FDPCircle fdpCircle)
			throws ExecutionFailedException {
		EMABatchStatus status = null;
		final Object isSuccess = response.getStepResponseValue(FDPStepResponseConstants.STATUS_KEY);
		if (isSuccess != null && (Boolean) isSuccess) {
			status = EMABatchStatus.SUCCESS;

		} else {
			final Object isLogOnFail = response.getStepResponseValue(FDPStepResponseConstants.COMMAND_STATUS_KEY);
			if (isLogOnFail != null && (Boolean) isLogOnFail) {
				final EMABatchRecordDTO failRecord = (EMABatchRecordDTO) response
						.getStepResponseValue(FDPStepResponseConstants.EMA_LOG_VALUE);
				failRecord.setBatchCounter(batchCounter + 1);
				if (isRetry(failRecord.getBatchCounter(), emaBatchMaxRetries)) {
					FDPLogger.info(FDPLoggerFactory.getEMALogger(fdpCircle.getCircleName()), this.getClass(),
							"processResponse()", new Gson().toJson(failRecord));
					status = EMABatchStatus.RETRY;
				} else {
					status = EMABatchStatus.FAILED_MAX_RETRY_REACHED;
				}
			} else {
				status = EMABatchStatus.FAILED;
			}
		}
		return status;
	}

	/**
	 * Gets the fDP service request.
	 * 
	 * @param msisdn
	 *            the msisdn
	 * @return the fDP service request
	 * @throws ExecutionFailedException
	 *             the execution failed exception
	 */
	private FDPRequestImpl getFDPServiceRequest(final String msisdn) throws ExecutionFailedException {
		final FDPRequestImpl fdpRequest = RequestUtil.createFDPRequest(msisdn, ChannelType.AIR_RECHARGE_BATCH);
		fdpRequest.setOriginTransactionID(generatorService.generateTransactionId());
		fdpRequest.setRequestId(ChannelType.AIR_RECHARGE_BATCH.name() + "_" + fdpRequest.getOriginTransactionID() + "_"
				+ ThreadLocalRandom.current().nextLong());
		return fdpRequest;
	}

	/**
	 * Gets the service step options map.
	 * 
	 * @param emaBatchRecord
	 *            the ema batch record
	 * @return the service step options map
	 */
	private Map<ServiceStepOptions, Object> getServiceStepOptionsMap(final EMABatchRecordDTO emaBatchRecord) {
		final Map<ServiceStepOptions, Object> additionalMap = new HashMap<ServiceStepOptions, Object>();
		additionalMap.put(ServiceStepOptions.COMMANDS, emaBatchRecord.getCommands());
		additionalMap.put(ServiceStepOptions.INTERFACE, emaBatchRecord.getInterfaceType().name());
		additionalMap.put(ServiceStepOptions.ACTION, emaBatchRecord.getServiceAction());
		additionalMap.put(ServiceStepOptions.MODE, emaBatchRecord.getMode());
		additionalMap.put(ServiceStepOptions.CHECK_ICR_CIRCLE, emaBatchRecord.getCheckIcrCircle());
		return additionalMap;
	}

	/**
	 * Write records to file.
	 * 
	 * @param fileRecords
	 *            the file records
	 * @param filePath
	 *            the file path
	 * @param isAppend
	 *            the is append
	 * @throws ExecutionFailedException
	 *             the execution failed exception
	 * @throws IOException
	 *             Signals that an I/O exception has occurred.
	 */
	private void writeRecordsToFile(final List<String> fileRecords, final String filePath, final boolean isAppend)
			throws ExecutionFailedException, IOException {
		final File file = new File(filePath);
		final FileWriter fileWriter = new FileWriter(file, isAppend);
		for (final String fileRecord : fileRecords) {
			fileWriter.write(fileRecord + "\n");
		}
		fileWriter.close();
	}

	/**
	 * Update records of temp file.
	 * 
	 * @param tempFilePath
	 *            the temp file path
	 * @param updatedRecords
	 *            the updated records
	 * @throws ExecutionFailedException
	 *             the execution failed exception
	 */
	private void updateRecordsOfTempFile(final String tempFilePath, final List<String> updatedRecords)
			throws ExecutionFailedException {
		LOGGER.debug("Updating records to temp file {} START.", tempFilePath);
		try {
			writeRecordsToFile(updatedRecords, tempFilePath, false);
		} catch (final IOException e) {
			throw new ExecutionFailedException(e.getMessage(), e);
		}
		LOGGER.debug("Updating records to temp file {} DONE.", tempFilePath);
	}

	/**
	 * Gets the file path from config.
	 * 
	 * @param key
	 *            the key
	 * @param replaceValList
	 *            the replace val list
	 * @return the file path from config
	 * @throws ExecutionFailedException
	 *             the execution failed exception
	 */
	private String getFilePathFromConfig(final String key, final List<String> replaceValList)
			throws ExecutionFailedException {
		String exactFilePath = null;
		String fileName = PropertyUtils.getProperty(key);
		if (fileName == null) {
			LOGGER.debug("Property {} not Found in config file.", key);
			throw new ExecutionFailedException("Property " + key + " not Found in config file.");
		} else {
			if (replaceValList != null && !replaceValList.isEmpty()) {
				for (final String replaceVal : replaceValList) {
					fileName = fileName.replaceFirst(FDPConstant.EMA_BATCH_FILE_CIRCLE_NAME_HOLDER_REGEX, replaceVal);
				}
			}
			exactFilePath = fileName;
			LOGGER.debug("Property {} Found in config file. File Path is : {}.", key, fileName);
		}
		return exactFilePath;
	}

	/**
	 * Gets the eMA batch record.
	 * 
	 * @param record
	 *            the record
	 * @return the eMA batch record
	 */
	private EMABatchRecordDTO getEMABatchRecord(final String record) {
		final String valuesOnly = record.substring(record.lastIndexOf("| ") + 2);
		return new Gson().fromJson(valuesOnly, EMABatchRecordDTO.class);
	}

	/**
	 * Update record status.
	 * 
	 * @param record
	 *            the record
	 * @param status
	 *            the status
	 * @return the string
	 */
	private String updateRecordStatus(final String record, final EMABatchStatus status) {
		return record + FDPConstant.EMA_STATUS_SEPA + status.name();
	}

	/**
	 * Write failed to file.
	 * 
	 * @param failedRecords
	 *            the failed records
	 * @param fdpCircle
	 *            the fdp circle
	 * @throws ExecutionFailedException
	 *             the execution failed exception
	 */
	private void writeFailedToFile(final List<String> failedRecords, final FDPCircle fdpCircle)
			throws ExecutionFailedException {
		try {
			final String ouputFailedFileName = getFilePathFromConfig(FAILED_RECORDS_FILE_NAME_KEY, Arrays.asList(
					fdpCircle.getCircleName(), Long.valueOf(Calendar.getInstance().getTimeInMillis()).toString()));
			LOGGER.info("Writing records = {} to failed file {} START.", failedRecords, ouputFailedFileName);
			writeRecordsToFile(failedRecords, ouputFailedFileName, true);
			LOGGER.info("Writing records to failed file {} Done.", ouputFailedFileName);
		} catch (final IOException e) {
			throw new ExecutionFailedException(e.getMessage(), e);
		}
	}

	/**
	 * Checks if is retry.
	 * 
	 * @param counter
	 *            the counter
	 * @param emaBatchMaxRetries
	 *            the ema batch max retries
	 * @return the boolean
	 * @throws ExecutionFailedException
	 *             the execution failed exception
	 */
	private Boolean isRetry(final Integer counter, final Integer emaBatchMaxRetries) throws ExecutionFailedException {
		Boolean result = null;
		if (counter <= emaBatchMaxRetries) {
			result = true;
		} else {
			result = false;
		}
		return result;
	}

}
