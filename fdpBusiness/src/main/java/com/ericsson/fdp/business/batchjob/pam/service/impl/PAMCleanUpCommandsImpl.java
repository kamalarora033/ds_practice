/**
 *
 */
package com.ericsson.fdp.business.batchjob.pam.service.impl;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import javax.annotation.Resource;
import javax.ejb.Stateless;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ericsson.fdp.FDPCacheable;
import com.ericsson.fdp.business.batchjob.pam.service.PAMCleanUpCommands;
import com.ericsson.fdp.business.command.param.impl.CommandParamInput;
import com.ericsson.fdp.business.constants.NotificationConstants;
import com.ericsson.fdp.business.dto.pam.PAMCleanUpResponse;
import com.ericsson.fdp.business.dto.pam.PAMFileResponse;
import com.ericsson.fdp.business.dto.pam.PAMRecord;
import com.ericsson.fdp.business.enums.Command;
import com.ericsson.fdp.business.transaction.TransactionSequenceGeneratorService;
import com.ericsson.fdp.business.util.RequestUtil;
import com.ericsson.fdp.common.constants.JNDILookupConstant;
import com.ericsson.fdp.common.constants.PAMCleanUpConstant;
import com.ericsson.fdp.common.enums.ChannelType;
import com.ericsson.fdp.common.enums.ModuleType;
import com.ericsson.fdp.common.enums.ParameterFeedType;
import com.ericsson.fdp.common.enums.Primitives;
import com.ericsson.fdp.common.vo.CircleConfigParamDTO;
import com.ericsson.fdp.common.vo.FDPCircle;
import com.ericsson.fdp.core.command.FDPCommand;
import com.ericsson.fdp.core.command.param.CommandParam;
import com.ericsson.fdp.core.dsm.framework.service.FDPCache;
import com.ericsson.fdp.core.exception.ExecutionFailedException;
import com.ericsson.fdp.core.request.FDPRequest;
import com.ericsson.fdp.core.request.impl.FDPRequestImpl;
import com.ericsson.fdp.core.serviceprovisioning.bean.FDPAppBag;
import com.ericsson.fdp.core.serviceprovisioning.bean.FDPMetaBag;
import com.ericsson.fdp.core.utils.ApplicationConfigUtil;
import com.ericsson.fdp.core.utils.CircleCodeFinder;
import com.ericsson.fdp.dao.enums.CommandParameterType;

/**
 * 
 * PAMCleanUpCommandsImpl this class will remove the pam from the offers expires
 * 
 */
@Stateless
public class PAMCleanUpCommandsImpl implements PAMCleanUpCommands {
	/** The Constant LOGGER. */
	private static final Logger LOGGER = LoggerFactory.getLogger(PAMCleanUpCommandsImpl.class);

	/** The meta data cache. */
	@Resource(lookup = JNDILookupConstant.META_DATA_CACHE_JNDI_NAME)
	private FDPCache<FDPMetaBag, FDPCacheable> metaDataCache;

	/** The transaction sequence dao. */
	@Resource(lookup = JNDILookupConstant.TRANSACTION_ID_GENERATOR_SERVICE)
	private TransactionSequenceGeneratorService generatorService;

	@Override
	public PAMCleanUpResponse processValidRecords(final List<PAMRecord> prevDayRecordList,
			final Map<String, String> offerPAMMap, final FDPCircle fdpCircle) {
		LOGGER.info(" | inside processValidRecords() .");
		PAMCleanUpResponse pamCleanUpResponse = null;
		Long recordsProcessed = new Long(0);
		Long recordsFailed = new Long(0);
		final List<PAMFileResponse> pamFileResponseList = new ArrayList<PAMFileResponse>();
		String pamStatus = NotificationConstants.FAIL;
		/* Processing previous date data */
		FDPRequest request = null;
		for (final PAMRecord pamRecord : prevDayRecordList) {
			final String msisdn = pamRecord.getMsisdn();
			try {
				 request = getCommandRequest(msisdn, fdpCircle);
				LOGGER.info(" | RID {} started with MSISDN {} ",request.getRequestId(),msisdn);
				if (!validateMsisdn(msisdn, fdpCircle.getCircleCode())) {
					recordsFailed = invalidMsisdnFailResponse(request,fdpCircle, recordsFailed, pamFileResponseList, pamStatus,
							pamRecord, msisdn);
					LOGGER.info(" | RID {} end with MSISDN {} for invalid Msisd",request.getRequestId(),msisdn);
					continue;
				}
				final String offerId = pamRecord.getOfferId();
				final String pamServiceId = offerPAMMap.get(offerId);
				if (null == pamServiceId) {
					recordsFailed = pamNotExistsResponse(fdpCircle, recordsFailed, pamFileResponseList, pamStatus,
							pamRecord, msisdn,request);
					LOGGER.info(" | RID {} ended for pam service id Not existsResponse. {} ",request.getRequestId(), msisdn);
					continue;
				}
				Map<String, CommandParam> outputParams;

				outputParams = executeGetOffer(offerPAMMap, fdpCircle, msisdn, offerId, request);
				if (outputParams != null) {
					recordsFailed = processAfterGetOfferExecuted(request, fdpCircle, recordsFailed,
							pamFileResponseList, pamStatus, pamRecord, msisdn, offerId, pamServiceId, outputParams);
				} else {
					recordsFailed = getOfferFailResponse(request,recordsFailed, pamFileResponseList, pamStatus, pamRecord,
							msisdn, offerId, pamServiceId);
				}
				LOGGER.info(" | RID {} end with MSISDN {} ",request.getRequestId(),msisdn);
			} catch (ExecutionFailedException e) {
				recordsFailed = commandFailResponse(request,fdpCircle, recordsFailed, pamFileResponseList, pamStatus,
						pamRecord, msisdn, e.getMessage());
			}
		}
		recordsProcessed = (prevDayRecordList.size() - recordsFailed);
		pamCleanUpResponse = new PAMCleanUpResponse(recordsProcessed, recordsFailed, pamFileResponseList);
		LOGGER.info(" | exiting from  processValidRecords() .");
		return pamCleanUpResponse;
	}

	/**
	 * This method will generate the response in case of command failed for a
	 * msisdn
	 * 
	 * @param fdpCircle
	 * @param recordsFailed
	 * @param pamFileResponseList
	 * @param pamStatus
	 * @param pamRecord
	 * @param msisdn
	 * @param failReason
	 * @return
	 */
	private Long commandFailResponse(final FDPRequest request, final FDPCircle fdpCircle, Long recordsFailed,
			final List<PAMFileResponse> pamFileResponseList, String pamStatus, final PAMRecord pamRecord,
			final String msisdn, String failReason) {
		recordsFailed++;
		LOGGER.info(" | processValidRecords() |command exection failed for msisdn {} and for  this circle: {}.",
				new Object[] { msisdn, fdpCircle });
		String reasonOfFail = "Command Execution failed for This msisdn=" + msisdn + "and for for given circle "
				+ fdpCircle.getCircleName() + " with given reason " + failReason;
		pamFileResponseList.add(getPamRecordResponse(pamRecord, pamStatus, reasonOfFail,request.getRequestId()));
		return recordsFailed;
	}

	/**
	 * This method will generate the response for invalid msisdn
	 * 
	 * @param fdpCircle
	 * @param recordsFailed
	 * @param pamFileResponseList
	 * @param pamStatus
	 * @param pamRecord
	 * @param msisdn
	 * @return
	 */
	private Long invalidMsisdnFailResponse(final FDPRequest request, final FDPCircle fdpCircle, Long recordsFailed,
			final List<PAMFileResponse> pamFileResponseList, String pamStatus, final PAMRecord pamRecord,
			final String msisdn) {
		recordsFailed++;
		LOGGER.info(" | processValidRecords() |checking MSISDN : {} is valid  for circle or not : {}.", new Object[] {
				msisdn, fdpCircle });
		String reasonOfFail = "This msisdn is not valid for given circle";
		pamFileResponseList.add(getPamRecordResponse(pamRecord, pamStatus, reasonOfFail,request.getRequestId()));
		return recordsFailed;
	}

	/**
	 * This method will generate the response for pam id not existed
	 * 
	 * @param fdpCircle
	 * @param recordsFailed
	 * @param pamFileResponseList
	 * @param pamStatus
	 * @param pamRecord
	 * @param msisdn
	 * @return
	 */
	private Long pamNotExistsResponse(final FDPCircle fdpCircle, Long recordsFailed,
			final List<PAMFileResponse> pamFileResponseList, String pamStatus, final PAMRecord pamRecord,
			final String msisdn,final FDPRequest request) {
		recordsFailed++;
		LOGGER.info("pam service id not defined for this msisdn {}", msisdn);
		String reasonOfFail = "Pam service id not defined for this msisdn";
		pamFileResponseList.add(getPamRecordResponse(pamRecord, pamStatus, reasonOfFail,request.getRequestId()));
		return recordsFailed;
	}

	/**
	 * This method will do the further processing when getOffer results
	 * something valid
	 * 
	 * @param fdpCircle
	 * @param recordsFailed
	 * @param pamFileResponseList
	 * @param pamStatus
	 * @param pamRecord
	 * @param msisdn
	 * @param offerId
	 * @param pamServiceId
	 * @param outputParams
	 * @return
	 * @throws ExecutionFailedException
	 */
	private Long processAfterGetOfferExecuted(final FDPRequest request, final FDPCircle fdpCircle, Long recordsFailed,
			final List<PAMFileResponse> pamFileResponseList, String pamStatus, PAMRecord pamRecord,
			final String msisdn, final String offerId, final String pamServiceId,
			final Map<String, CommandParam> outputParams) throws ExecutionFailedException {
		LOGGER.info("GET Offer response received {}", outputParams);
		LOGGER.info(
				" | processValidRecords() | GET OFFERS : SUCCESSFULLY Executed for MSISDN : {}, offerID : {},RID :{} and PamSeviceId {}.",
				new Object[] { msisdn, offerId,request.getRequestId(), pamServiceId });
		final Integer responseCode = getResponseCode(outputParams);
		if (responseCode.equals(PAMCleanUpConstant.NON_EXISTING_OFFER)) {
			recordsFailed = executeDeletePam(request, fdpCircle, recordsFailed, pamFileResponseList, pamStatus,
					pamRecord, msisdn, offerId, pamServiceId);
		} else if (responseCode.equals(PAMCleanUpConstant.EXISTING_OFFER)) {
			recordsFailed = offerStillExistedFailResponse(recordsFailed, pamFileResponseList, pamStatus, pamRecord,
					msisdn, offerId, responseCode);
		} else {
			recordsFailed = unknownGetOfferFailResponse(recordsFailed, pamFileResponseList, pamStatus, pamRecord,
					msisdn, offerId, responseCode);
		}
		return recordsFailed;
	}

	/**
	 * This method will execute the delete Pam commands on Pam record
	 * 
	 * @param fdpCircle
	 * @param recordsFailed
	 * @param pamFileResponseList
	 * @param pamStatus
	 * @param pamRecord
	 * @param msisdn
	 * @param offerId
	 * @param pamServiceId
	 * @return
	 * @throws ExecutionFailedException
	 */
	private Long executeDeletePam(final FDPRequest request, final FDPCircle fdpCircle, Long recordsFailed,
			final List<PAMFileResponse> pamFileResponseList, String pamStatus, PAMRecord pamRecord,
			final String msisdn, final String offerId, final String pamServiceId) throws ExecutionFailedException {
		final Map<String, CommandParam> deletePAMResp = executePAMCommand(request, msisdn, fdpCircle,
				Command.DELETE_PAM.getCommandName(), getCommandUpdateValue(pamServiceId));
		LOGGER.info(" | processValidRecords() | Delete PAM : response received : {}", deletePAMResp);
		if (deletePAMResp != null && getResponseCode(deletePAMResp).equals(PAMCleanUpConstant.DELETEPAM_SUCCESS)) {
			LOGGER.info(
					" | processValidRecords() | Delete PAM : SUCCESSFULLY Executed for MSISDN : {}, offerID : {},pamServiceID : {}.",
					new Object[] { msisdn, offerId, pamServiceId });
		} else {
			recordsFailed = deletePamFailResponse(request, recordsFailed, pamFileResponseList, pamStatus, pamRecord, msisdn,
					offerId, pamServiceId,getResponseCode(deletePAMResp));
		}
		return recordsFailed;
	}

	/**
	 * This method will execute the get Offer on pam records
	 * 
	 * @param offerPAMMap
	 * @param fdpCircle
	 * @param msisdn
	 * @param offerId
	 * @return
	 * @throws ExecutionFailedException
	 */
	private Map<String, CommandParam> executeGetOffer(final Map<String, String> offerPAMMap, final FDPCircle fdpCircle,
			final String msisdn, final String offerId, FDPRequest request) throws ExecutionFailedException {
		LOGGER.info(" | processValidRecords() | For offerId {} pamServiceId is {} ",
				new Object[] { offerId, offerPAMMap.get(offerId) });

		// Execute getOffers
		final Map<String, CommandParam> outputParams = executePAMCommand(request, msisdn, fdpCircle,
				Command.GET_OFFERS.getCommandName(), getCommandUpdateValue(offerId));
		return outputParams;
	}

	/**
	 * Helper method for command parameters
	 * 
	 * @param commandValue
	 * @return
	 */
	private List<String> getCommandUpdateValue(final String commandValue) {
		List<String> updateValues = new ArrayList<String>(1);
		updateValues.add(commandValue);
		return updateValues;
	}

	/**
	 * This method will generate the response of failure if delete pam failed
	 * for a pam record
	 * 
	 * @param recordsFailed
	 * @param pamFileResponseList
	 * @param pamStatus
	 * @param pamRecord
	 * @param msisdn
	 * @param offerId
	 * @param pamServiceId
	 * @return
	 */
	private long deletePamFailResponse(FDPRequest request, long recordsFailed, final List<PAMFileResponse> pamFileResponseList,
			String pamStatus, final PAMRecord pamRecord, final String msisdn, final String offerId,
			final String pamServiceId, final Integer response) {
		String reasonOfFail;
		recordsFailed++;
		LOGGER.info(" | processValidRecords() | Delete PAM : {} FAILED for MSISDN : {}, offerID : {}.", new Object[] {
				pamServiceId, msisdn, offerId });
		reasonOfFail = "Delete PAM FAILED   for this  msisdn";
		pamFileResponseList.add(getPamRecordResponse(pamRecord, pamStatus, reasonOfFail,request.getRequestId(),response,pamServiceId));
		return recordsFailed;
	}

	/**
	 * This method will generate the response of failure if get ffer returns
	 * unknown response
	 * 
	 * @param recordsFailed
	 * @param pamFileResponseList
	 * @param pamStatus
	 * @param pamRecordo
	 * @param msisdn
	 * @param offerId
	 * @param responseCode
	 * @return
	 */
	private long unknownGetOfferFailResponse(long recordsFailed, final List<PAMFileResponse> pamFileResponseList,
			String pamStatus, final PAMRecord pamRecord, final String msisdn, final String offerId,
			final Integer responseCode) {
		String reasonOfFail;
		recordsFailed++;
		LOGGER.info(
				" | processValidRecords() | GetOffer : responseCode = {}. Unknown response for offer {} and msisdn {}.",
				new Object[] { responseCode, offerId, msisdn, });
		reasonOfFail = "GET OFFERS returns unknown response for this  msisdn";
		pamFileResponseList.add(getPamRecordResponse(pamRecord, pamStatus, reasonOfFail));
		return recordsFailed;
	}

	/**
	 * This method will generate the response of failure if get offer returns
	 * that offer still not expired
	 * 
	 * @param recordsFailed
	 * @param pamFileResponseList
	 * @param pamStatus
	 * @param pamRecord
	 * @param msisdn
	 * @param offerId
	 * @param responseCode
	 * @return
	 */
	private long offerStillExistedFailResponse(long recordsFailed, final List<PAMFileResponse> pamFileResponseList,
			String pamStatus, final PAMRecord pamRecord, final String msisdn, final String offerId,
			final Integer responseCode) {
		String reasonOfFail;
		recordsFailed++;
		LOGGER.info(" | processValidRecords() | GetOffer : responseCode = {}. Existing offer {} for msisdn {}.",
				new Object[] { responseCode, offerId, msisdn, });
		reasonOfFail = "GET OFFERS return that this offer still existed for this  msisdn";
		pamFileResponseList.add(getPamRecordResponse(pamRecord, pamStatus, reasonOfFail));
		return recordsFailed;
	}

	/**
	 * This method will generate the response of failure if get offer commands
	 * fails
	 * 
	 * @param recordsFailed
	 * @param pamFileResponseList
	 * @param pamStatus
	 * @param pamRecord
	 * @param msisdn
	 * @param offerId
	 * @param pamServiceId
	 * @return
	 */
	private long getOfferFailResponse(final FDPRequest request, long recordsFailed, final List<PAMFileResponse> pamFileResponseList,
			String pamStatus, final PAMRecord pamRecord, final String msisdn, final String offerId,
			final String pamServiceId) {
		String reasonOfFail;
		recordsFailed++;
		LOGGER.info(" | processValidRecords() | GET OFFERS : {} FAILED for MSISDN : {}, offerID : {}.", new Object[] {
				pamServiceId, msisdn, offerId });
		reasonOfFail = "GET OFFERS Command Failed for this  msisdn";
		pamFileResponseList.add(getPamRecordResponse(pamRecord, pamStatus, reasonOfFail,request.getRequestId()));
		return recordsFailed;
	}

	/**
	 * This method will validate the msisdn for a circle
	 * 
	 * @param msisdn
	 * @param circleCode
	 * @return
	 */
	private Boolean validateMsisdn(final String msisdn, final String circleCode) {
		Boolean isValid = Boolean.TRUE;
		/*final String circleCodeFromMsisdn = getCircleCodeFromMsisdn(msisdn);
		if (!circleCode.equalsIgnoreCase(circleCodeFromMsisdn)) {
			isValid = Boolean.FALSE;
		}*/
		return isValid;
	}

	/**
	 * This method will return the circle code for which this msisdn existed
	 * 
	 * @param msisdn
	 * @return
	 */
	private String getCircleCodeFromMsisdn(final String msisdn) {
		FDPCache<FDPAppBag, Object> applicationConfigCache = null;
		try {
			applicationConfigCache = ApplicationConfigUtil.getApplicationConfigCache();
		} catch (ExecutionFailedException e) {
			LOGGER.error("Exceptions occured in fetching application cache object " + e.getMessage(), e);
		}
		return CircleCodeFinder.getCircleCode(msisdn, applicationConfigCache);
	}

	/**
	 * This method returns the response object for Pam
	 * 
	 * @param pamRecord
	 * @param status
	 * @param failureDescription
	 * @return
	 */
	private PAMFileResponse getPamRecordResponse(final PAMRecord pamRecord, final String status,
			final String failureDescription,final String requestId) {
		return new PAMFileResponse(pamRecord, false, status, failureDescription,requestId);
	}

	/**
	 * @param pamRecord
	 * @param status
	 * @param failureDescription
	 * @return
	 */
	private PAMFileResponse getPamRecordResponse(final PAMRecord pamRecord, final String status,
			final String failureDescription) {
		return new PAMFileResponse(pamRecord, false, status, failureDescription);
	}
	
	/**
	 * @param pamRecord
	 * @param status
	 * @param failureDescription
	 * @param requestId
	 * @param requestID
	 * @param responseCode
	 * @return
	 */
	private PAMFileResponse getPamRecordResponse(final PAMRecord pamRecord, final String status,
			final String failureDescription,final String requestID, final Integer responseCode,final String pamServiceID) {
		return new PAMFileResponse(pamRecord, false, status, failureDescription,requestID,responseCode,pamServiceID);
	}
	/**
	 * 
	 * @param outputParams
	 * @return
	 */
	private Integer getResponseCode(final Map<String, CommandParam> outputParams) {
		Integer responseCode = null;
		final CommandParam responseCodeObject = outputParams.get(PAMCleanUpConstant.RESPONSE_CODE.toLowerCase());
		if (responseCodeObject != null && responseCodeObject.getValue() instanceof Integer) {
			responseCode = (Integer) responseCodeObject.getValue();
		}
		return responseCode;
	}

	/**
	 * This method is used for execution of commands (getOffer/deletePam)
	 * required for pam clean up
	 * 
	 * @param msisdn
	 * @param fdpCircle
	 * @param commandName
	 * @param updateValues
	 * @return
	 * @throws ExecutionFailedException
	 */
	private Map<String, CommandParam> executePAMCommand(final FDPRequest request, final String msisdn,
			final FDPCircle fdpCircle, final String commandName, final List<String> updateValues)
			throws ExecutionFailedException {
		Map<String, CommandParam> result = null;
		final FDPCommand fdpCommand = getCachedCommand(fdpCircle, commandName);
		if (fdpCommand == null) {
			LOGGER.error(" | executePAMCommand() | command {} is not configured for circle {}.", commandName,
					fdpCircle.getCircleName());
			throw new ExecutionFailedException("command " + commandName + " is not configured for circle {}");
		} else {
			LOGGER.debug(" | executePAMCommand() | command {} found.\n" + "no of input parameters = {}\n command ={}.",
					new Object[] { commandName, fdpCommand.getInputParam().size(), fdpCommand });
			try {
				if (updateValues != null && !updateValues.isEmpty()) {
					updateCommandParameters(fdpCommand, updateValues);
				}
				fdpCommand.execute(request);
				result = fdpCommand.getOutputParams();
			} catch (final ExecutionFailedException e) {
				LOGGER.error("Exceptions occured in executing  {} command. Error : {}", commandName, e.getMessage());
				throw new ExecutionFailedException("Exceptions occured in executing   " + commandName
						+ " command. Error : " + e.getMessage());
			}
		}
		return result;
	}

	/**
	 * Update command parameters.
	 * 
	 * @param fdpCommand
	 *            the fdp command
	 * @param updateValues
	 *            the update values
	 * @throws ExecutionFailedException
	 */
	private void updateCommandParameters(final FDPCommand fdpCommand, final List<String> updateValues)
			throws ExecutionFailedException {
		LOGGER.debug(" | updateCommandParameters() | command = {}, updateValues = {}.", fdpCommand, updateValues);
		final String commandName = fdpCommand.getCommandDisplayName();
		if (Command.GET_OFFERS.getCommandDisplayName().equalsIgnoreCase(commandName)) {
			Boolean configured = false;
			for (final CommandParam inputParam : fdpCommand.getInputParam()) {
				if (inputParam.getName().equalsIgnoreCase(PAMCleanUpConstant.OFEFR_SELECTION)) {
					configured = updateGetOfferParameters(fdpCommand, updateValues, commandName, configured, inputParam);
				}
			}
			getOfferConfigurationError(commandName, configured);
		} else if (Command.DELETE_PAM.getCommandDisplayName().equalsIgnoreCase(commandName)) {
			Boolean configured = false;
			configured = updateDeletePamParameter(fdpCommand, updateValues, commandName, configured);
			deletePamConfigurationError(commandName, configured);
		}
	}

	/**
	 * This method throw and exception if delete pam will not be configured for
	 * this circle
	 * 
	 * @param commandName
	 * @param configured
	 * @throws ExecutionFailedException
	 */
	private void deletePamConfigurationError(final String commandName, Boolean configured)
			throws ExecutionFailedException {
		if (!configured) {
			LOGGER.error(
					" | updateCommandParameters() | command {} is NOT configured properly(pamServiceID not found).",
					commandName);
			throw new ExecutionFailedException(
					"command DeletePeriodicAccountManagementData is NOT configured properly(pamServiceID not found).");
		}
	}

	/**
	 * This method throw and exception if get offer will not be configured for
	 * this circle
	 * 
	 * @param commandName
	 * @param configured
	 * @throws ExecutionFailedException
	 */
	private void getOfferConfigurationError(final String commandName, Boolean configured)
			throws ExecutionFailedException {
		if (!configured) {
			LOGGER.error(
					" | updateCommandParameters() | command {} is NOT configured properly(OfferIDFirst not found).",
					commandName);
			throw new ExecutionFailedException("command GetOffers is NOT configured properly(OfferIDFirst not found).");
		}
	}

	/**
	 * This method will update the parameter required for delete pam
	 * 
	 * @param fdpCommand
	 * @param updateValues
	 * @param commandName
	 * @param configured
	 * @return
	 */
	private Boolean updateDeletePamParameter(final FDPCommand fdpCommand, final List<String> updateValues,
			final String commandName, Boolean configured) {
		for (final CommandParam inputParam : fdpCommand.getInputParam()) {
			if (inputParam.getName().equalsIgnoreCase(PAMCleanUpConstant.PAM_INFORMATION_LIST)) {
				for (final CommandParam arrayChildParam : inputParam.getChilderen()) {
					if (arrayChildParam.getName().equalsIgnoreCase(PAMCleanUpConstant.ARRAY_CHILD_0)) {
						final List<CommandParam> structChilds = new ArrayList<CommandParam>(1);
						final CommandParamInput param = new CommandParamInput(ParameterFeedType.INPUT,
								Integer.parseInt(updateValues.get(0)));
						param.setPrimitiveValue(Primitives.INTEGER);
						param.setType(CommandParameterType.PRIMITIVE);
						param.setCommand(fdpCommand);
						param.setName(PAMCleanUpConstant.PAM_SERVICE_ID);
						param.setParent(arrayChildParam);
						structChilds.add(param);
						((CommandParamInput) arrayChildParam).setChilderen(structChilds);
						configured = true;
						LOGGER.debug(" | updateCommandParameters() | command {} .Parameter {} updated to {}.",
								new Object[] { commandName, param.getName(), updateValues.get(0) });
					}
				}
			}
		}
		return configured;
	}

	/**
	 * This method will update the parameter required for get offer
	 * 
	 * @param fdpCommand
	 * @param updateValues
	 * @param commandName
	 * @param configured
	 * @param inputParam
	 * @return
	 */
	private Boolean updateGetOfferParameters(final FDPCommand fdpCommand, final List<String> updateValues,
			final String commandName, Boolean configured, final CommandParam inputParam) {
		for (final CommandParam arrayChildParam : inputParam.getChilderen()) {
			if (arrayChildParam.getName().equalsIgnoreCase(PAMCleanUpConstant.ARRAY_CHILD_0)) {
				final List<CommandParam> structChilds = new ArrayList<CommandParam>(1);
				final CommandParamInput param = new CommandParamInput(ParameterFeedType.INPUT,
						Integer.parseInt(updateValues.get(0)));
				param.setPrimitiveValue(Primitives.INTEGER);
				param.setType(CommandParameterType.PRIMITIVE);
				param.setCommand(fdpCommand);
				param.setName(PAMCleanUpConstant.OFFER_ID_FIRST);
				param.setParent(arrayChildParam);
				structChilds.add(param);
				((CommandParamInput) arrayChildParam).setChilderen(structChilds);
				configured = true;
				LOGGER.debug(" | updateCommandParameters() | command {} .Parameter {} updated to {}.", new Object[] {
						commandName, param.getName(), updateValues.get(0) });
			}
		}
		return configured;
	}

	/**
	 * This method willl populate the request object for execution of commands
	 * 
	 * @param msisdn
	 * @param fdpCircle
	 * @return
	 * @throws ExecutionFailedException
	 */
	private FDPRequestImpl getCommandRequest(final String msisdn, final FDPCircle fdpCircle)
			throws ExecutionFailedException {
		final FDPRequestImpl fdpRequest = new FDPRequestImpl();
		fdpRequest.setChannel(ChannelType.PAM);
		fdpRequest.setRequestId(NotificationConstants.PAM_REQUESTED_ID + (String.valueOf(UUID.randomUUID())));
		fdpRequest.setCircle(fdpCircle);
		// set circle parameters
		final CircleConfigParamDTO circleConfigParamDTO = RequestUtil.populateCircleConfigParamDTO(msisdn, fdpCircle);
		fdpRequest.setOriginHostName(circleConfigParamDTO.getOriginHostName());
		fdpRequest.setOriginNodeType(circleConfigParamDTO.getOriginNodeType());
		fdpRequest.setSubscriberNumber(circleConfigParamDTO.getSubscriberNumber());
		fdpRequest.setIncomingSubscriberNumber(circleConfigParamDTO.getIncomingSubscriberNumber());
		fdpRequest.setSubscriberNumberNAI(circleConfigParamDTO.getSubscriberNumberNAI());
		fdpRequest.setOriginTransactionID(generatorService.generateTransactionId());
		return fdpRequest;
	}

	/**
	 * This method will return the command by name from cache
	 * 
	 * @param fdpCircle
	 * @param commandName
	 * @return
	 */
	private FDPCommand getCachedCommand(final FDPCircle fdpCircle, final String commandName) {
		FDPCommand fdpCommand = null;
		final FDPCacheable fdpCommandCached = metaDataCache.getValue(new FDPMetaBag(fdpCircle, ModuleType.COMMAND,
				commandName));
		if (fdpCommandCached instanceof FDPCommand) {
			fdpCommand = (FDPCommand) fdpCommandCached;
		}
		return fdpCommand;
	}
}
