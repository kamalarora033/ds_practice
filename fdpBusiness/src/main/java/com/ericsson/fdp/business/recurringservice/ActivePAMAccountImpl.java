package com.ericsson.fdp.business.recurringservice;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.GregorianCalendar;
import java.util.List;

import javax.annotation.Resource;
import javax.ejb.Stateless;

import org.apache.commons.collections.CollectionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ericsson.fdp.business.batchjob.pam.service.impl.AbstractPAMActiveAccounts;
import com.ericsson.fdp.business.constants.FDPCommandConstants;
import com.ericsson.fdp.business.enums.Command;
import com.ericsson.fdp.business.transaction.TransactionSequenceGeneratorService;
import com.ericsson.fdp.business.util.LoggerUtil;
import com.ericsson.fdp.business.util.RequestUtil;
import com.ericsson.fdp.business.vo.FDPPamActiveServicesVO;
import com.ericsson.fdp.common.constants.FDPConstant;
import com.ericsson.fdp.common.constants.JNDILookupConstant;
import com.ericsson.fdp.common.enums.ChannelType;
import com.ericsson.fdp.common.enums.Status;
import com.ericsson.fdp.common.util.DateUtil;
import com.ericsson.fdp.common.vo.FDPCCAdminVO;
import com.ericsson.fdp.core.command.FDPCommand;
import com.ericsson.fdp.core.command.param.CommandParam;
import com.ericsson.fdp.core.exception.ExecutionFailedException;
import com.ericsson.fdp.core.logging.FDPLogger;
import com.ericsson.fdp.core.request.FDPRequest;
import com.ericsson.fdp.core.request.impl.FDPRequestImpl;

/**
 * This Stateless bean is used for CC web active PAM products list fetching.
 * 
 * 
 */
@Stateless
public class ActivePAMAccountImpl extends AbstractPAMActiveAccounts implements ActiveAccount {

	/** The ActivePAMAccountImpl LOGGER. */
	private static final Logger LOGGER = LoggerFactory.getLogger(ActivePAMAccountImpl.class);

	/** The get account details. */
	private String getAccountDetails = Command.GETACCOUNTDETAILS.getCommandDisplayName();

	@Resource(lookup = JNDILookupConstant.TRANSACTION_ID_GENERATOR_SERVICE)
	private TransactionSequenceGeneratorService generatorService;

	@Override
	public List<FDPPamActiveServicesVO> getProductsFromPamServicesID(FDPRequest fdpRequest)
			throws ExecutionFailedException {
		List<FDPPamActiveServicesVO> fdpPamActiveServicesVO = null;
		FDPCommand commandGetAccountDetails = null;
		LOGGER.info("Entering inside getProductsFromPamServicesID with request object {}.", fdpRequest);
		FDPLogger.debug(getCircleLogger(fdpRequest), getClass(), "getProductsFromPamServicesID()",
				LoggerUtil.getRequestAppender(fdpRequest) + "The request message is to find PAM for USER: "
						+ fdpRequest.getSubscriberNumber());
		commandGetAccountDetails = getCommand(fdpRequest, getAccountDetails);
		if (commandGetAccountDetails == null) {
			LOGGER.info("getAccountDetails command not configured ");
			throw new ExecutionFailedException("Get account details is not configured for the circle."
					+ fdpRequest.getCircle().getCircleCode());
		}
		if (commandGetAccountDetails.execute(fdpRequest).equals(Status.SUCCESS)) {
			fdpPamActiveServicesVO = getProductIDFromPAMId(fdpRequest,
					getPAMIDFromCommandOutputParam(fdpRequest, commandGetAccountDetails));
			populateOtherParam(commandGetAccountDetails, fdpPamActiveServicesVO);
			LOGGER.info("Exiting from getProductsFromPamServicesID with fdpPamActiveServicesVO {}.",
					fdpPamActiveServicesVO);
		} else {
			LOGGER.info("getAccountDetails command execution failedfor the circle."
					+ fdpRequest.getCircle().getCircleCode());
			throw new ExecutionFailedException("getAccountDetails command execution failed for the circle."
					+ fdpRequest.getCircle().getCircleCode());
		}
		return fdpPamActiveServicesVO;
	}

	/**
	 * This method will populate other required parameter from response of
	 * commandGetAccountDetails
	 * 
	 * @param commandGetAccountDetails
	 * @param fdpPamActiveServicesVOs
	 */
	private void populateOtherParam(FDPCommand commandGetAccountDetails,
			List<FDPPamActiveServicesVO> fdpPamActiveServicesVOs) {
		if (CollectionUtils.isNotEmpty(fdpPamActiveServicesVOs)) {
			int index = 0;
			CommandParam value = null;
			do {
				String fullyQualifiedPath = FDPCommandConstants.GET_ACCOUNT_DETAILS_PAM_ID
						+ FDPConstant.PARAMETER_SEPARATOR + index + FDPConstant.PARAMETER_SEPARATOR
						+ FDPConstant.LAST_EVALUATION_DATE;
				value = commandGetAccountDetails.getOutputParam(fullyQualifiedPath);
				if (value != null && value.getValue() != null) {
					FDPPamActiveServicesVO fdpPamActiveServiceVO = fdpPamActiveServicesVOs.get(index);
					GregorianCalendar lastRenewalDate = (GregorianCalendar) value.getValue();
					fdpPamActiveServiceVO.setLastRenewalDate(DateUtil.convertCalendarDateToString(lastRenewalDate,
							FDPConstant.DATE_PATTERN_WITH_DATE_ONLY));
				}
				index++;
			} while (value != null);
		}
	}

	@Override
	public List<FDPCCAdminVO> executeActiveAccountService(String msisdn) throws ExecutionFailedException {
		LOGGER.info("Entering inside executeActiveAccountService with msisdn {}.", msisdn);
		FDPRequest fdpRequest = RequestUtil.createFDPRequest(msisdn, ChannelType.WEB);
		if (fdpRequest instanceof FDPRequestImpl) {
			FDPRequestImpl fdpRequestImpl = (FDPRequestImpl) fdpRequest;
			fdpRequestImpl.setOriginTransactionID(generatorService.generateTransactionId());
		}
		List<FDPPamActiveServicesVO> fdpPamServicesVOs = getProductsFromPamServicesID(fdpRequest);
		List<FDPCCAdminVO> fdpCCAdminVOs = convertToFDPCCAdminVO(fdpPamServicesVOs);
		LOGGER.info("Exiting from  executeActiveAccountService with fdpCCAdminVOs {}.", fdpCCAdminVOs);
		return fdpCCAdminVOs;
	}

	/**
	 * This method will polpulate the <code>FDPCCAdminVO</code> from
	 * <code>FDPPamActiveServicesVO</code>
	 * 
	 * @param fdpPamServicesVOs
	 * @return
	 */
	private List<FDPCCAdminVO> convertToFDPCCAdminVO(List<FDPPamActiveServicesVO> fdpPamServicesVOs) {
		List<FDPCCAdminVO> fdpCCAdminVOs = new ArrayList<FDPCCAdminVO>();
		if (CollectionUtils.isNotEmpty(fdpPamServicesVOs)) {
			for (FDPPamActiveServicesVO fdpServiceVO : fdpPamServicesVOs) {
				try {
					FDPCCAdminVO fdpCCAdminVO = new FDPCCAdminVO();
					fdpCCAdminVO.setProductId(fdpServiceVO.getProduct().getProductId());
					fdpCCAdminVO.setProductName(fdpServiceVO.getProduct().getProductName());
					fdpCCAdminVO.setActivationDate(null);
					fdpCCAdminVO.setRenewalPeriod(fdpServiceVO.getProduct().getRenewalCount().toString());
					fdpCCAdminVO.setLastRenewalDate(fdpServiceVO.getLastRenewalDate());
					if (null != fdpCCAdminVO.getLastRenewalDate() && null != fdpCCAdminVO.getRenewalPeriod()) {
						fdpCCAdminVO.setNextRenewalDate(DateUtil.getNextIntervalDate(
								FDPConstant.DATE_PATTERN_WITH_DATE_ONLY, fdpCCAdminVO.getLastRenewalDate(),
								Integer.parseInt(fdpCCAdminVO.getRenewalPeriod())));
					}
					fdpCCAdminVO.setProductType(ActiveProductType.PAM.getProductType());
					fdpCCAdminVOs.add(fdpCCAdminVO);
				} catch (ParseException pe) {
					LOGGER.info("some error occured in populating the next renewal date for pam " + pe.getMessage());
				}
			}
		}
		return fdpCCAdminVOs;
	}

}