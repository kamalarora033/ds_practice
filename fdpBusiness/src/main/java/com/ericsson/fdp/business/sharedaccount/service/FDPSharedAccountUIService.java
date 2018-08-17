package com.ericsson.fdp.business.sharedaccount.service;

import java.util.List;

import javax.ejb.Remote;

import com.ericsson.fdp.business.dto.ResponseDTO;
import com.ericsson.fdp.business.exception.RuleException;
import com.ericsson.fdp.common.enums.ProductType;
import com.ericsson.fdp.common.exception.FDPServiceException;
import com.ericsson.fdp.core.exception.EvaluationFailedException;
import com.ericsson.fdp.core.exception.ExecutionFailedException;
import com.ericsson.fdp.dao.dto.product.ProductGeneralInfoDTO;
import com.ericsson.fdp.dao.dto.sharedaccount.ConsumerApprovalDTO;
import com.ericsson.fdp.dao.dto.sharedaccount.SharedAccGpDTO;
import com.ericsson.fdp.dao.dto.sharedaccount.SharedAccountConsumerDTO;
import com.ericsson.fdp.dao.dto.sharedaccount.SharedAccountConsumerUIDTO;
import com.ericsson.fdp.dao.dto.sharedaccount.SharedAccountDetailsDTO;
import com.ericsson.fdp.dao.dto.sharedaccount.SharedAccountProviderDTO;
import com.ericsson.fdp.dao.dto.sharedaccount.UsageDTO;
import com.ericsson.fdp.dao.enums.ConsumerRequestType;

/**
 * The Interface SharedAccountUIService.
 * 
 * @author Ericsson
 */
@Remote
public interface FDPSharedAccountUIService {

	/**
	 * Gets the all shared acc gp.
	 * 
	 * @param providerMSISDN
	 *            the provider msisdn
	 * @param productId
	 *            the product id
	 * @return the all shared acc gp
	 */
	List<SharedAccGpDTO> getAllSharedAccGp(Long providerMSISDN, Long productId);

	/**
	 * Update shared account group.
	 * 
	 * @param sharedAccGpDTO
	 *            the shared acc gp dto
	 * @throws FDPServiceException
	 *             the fDP service exception
	 */
	void updateSharedAccountGroup(SharedAccGpDTO sharedAccGpDTO) throws FDPServiceException;

	/**
	 * Gets the providers shared accounts.
	 * 
	 * @param providerId
	 *            the provider id
	 * @return the providers shared accounts
	 * @throws FDPServiceException
	 *             the fDP service exception
	 */
	List<SharedAccountProviderDTO> getProviderSharedAccounts(Long providerId) throws FDPServiceException;

	/**
	 * Gets the provider list for consumer.
	 * 
	 * @param consumerId
	 *            the consumer id
	 * @return the provider list for consumer
	 * @throws FDPServiceException
	 *             the fDP service exception
	 */
	List<SharedAccountConsumerUIDTO> getProviderListForConsumer(Long consumerId) throws FDPServiceException;

	/**
	 * Gets the consumer list for provider.
	 * 
	 * @param providerMobNo
	 *            the provider mob no
	 * @param productId
	 *            the product id
	 * @return the consumer list for provider
	 */
	List<SharedAccountConsumerDTO> getConsumersListForSharedAccount(Long providerMobNo, Long productId);

	/**
	 * Gets the consumer request list for provider.
	 * 
	 * @param providerMobNo
	 *            the provider mob no
	 * @param productId
	 *            the product id
	 * @return the consumer request list for provider
	 */
	List<SharedAccountConsumerDTO> getConsumerRequestListForApproval(Long providerMobNo, Long productId);

	/**
	 * Save consumer request.
	 * 
	 * @param consumerDTOList
	 *            the consumer dto list
	 * @param productId
	 *            the product id
	 * @param providerMsisdn
	 *            the provider msisdn
	 * @return
	 * @throws FDPServiceException
	 *             the fDP service exception
	 */
	List<String> saveConsumerRequest(List<SharedAccountConsumerDTO> consumerDTOList, Long productId, Long providerMsisdn)
			throws FDPServiceException;

	/**
	 * Removes the consumer request.
	 * 
	 * @param consumerList
	 *            the consumer list
	 * @param productId
	 *            the product id
	 * @param providerMsisdn
	 *            the provider msisdn
	 * @throws FDPServiceException
	 *             the fDP service exception
	 */
	void removeConsumerRequest(List<Long> consumerList, Long productId, Long providerMsisdn) throws FDPServiceException;

	/**
	 * View top n consumer request.
	 * 
	 * @param nValue
	 *            the n value
	 * @param productId
	 *            the product id
	 * @param providerMsisdn
	 *            the provider msisdn
	 * @throws FDPServiceException
	 *             the fDP service exception
	 */
	void viewTopNConsumerRequest(Integer nValue, Long productId, Long providerMsisdn) throws FDPServiceException;

	/**
	 * Gets the product general info dto.
	 * 
	 * @param providerMSISDN
	 *            the provider msisdn
	 * @param productType
	 *            the product type
	 * @return the product general info dto
	 */
	List<ProductGeneralInfoDTO> getProductGeneralInfoDto(Long providerMSISDN, ProductType productType);

	/**
	 * Update shared account approval request.
	 * 
	 * @param approvalDTO
	 *            the approval dto
	 * @param providerMsisdn
	 *            the provider msisdn
	 * @throws FDPServiceException
	 *             the fDP service exception
	 */
	void updateSharedAccountApprovalRequest(ConsumerApprovalDTO approvalDTO, Long providerMsisdn)
			throws FDPServiceException;

	/**
	 * Gets the shared account details.
	 * 
	 * @param sharedAccountId
	 *            the shared account id
	 * @return the shared account details
	 * @throws ExecutionFailedException
	 *             the execution failed exception
	 * @throws FDPServiceException
	 */
	SharedAccountDetailsDTO getSharedAccountDetails(Long sharedAccountId) throws ExecutionFailedException,
			FDPServiceException;

	/**
	 * Delete account.
	 * 
	 * @param accountId
	 *            the account id
	 * @param providerMsisdn
	 *            the provider msisdn
	 * @param sharedAccountProductId
	 *            the shared account product id
	 * @throws FDPServiceException
	 *             the fDP service exception
	 */
	void deleteAccount(Long accountId, Long providerMsisdn, Long sharedAccountProductId) throws FDPServiceException;

	/**
	 * Detach provider.
	 * 
	 * @param accountId
	 *            the account id
	 * @param providerMsisdn
	 *            the provider msisdn
	 * @param sharedAccountProductId
	 *            the shared account product id
	 * @throws FDPServiceException
	 *             the fDP service exception
	 */
	void detachProvider(Long accountId, Long providerMsisdn, Long sharedAccountProductId) throws FDPServiceException;

	/**
	 * Update shared account details.
	 * 
	 * @param sharedAccId
	 *            the shared acc id
	 * @param newSharedAccName
	 *            the new shared acc name
	 * @param modifiedBy
	 *            the modified by
	 * @return true, if successful
	 */
	Boolean updateSharedAccountName(Long sharedAccId, String newSharedAccName, String modifiedBy);

	/**
	 * Buy product.
	 * 
	 * @param productId
	 *            the product id
	 * @param msisdn
	 *            the msisdn
	 * @param boughtBy
	 *            the bought by
	 * @param isConfirmed
	 *            the is confirmed
	 * @return the response dto
	 * @throws FDPServiceException
	 *             FDPServiceException
	 */
	ResponseDTO buyProduct(Long productId, Long msisdn, Object boughtBy, boolean isConfirmed)
			throws FDPServiceException;

	/**
	 * Gets the filtered consumers.
	 * 
	 * @param sharedAccountConsumerDTO
	 *            the shared account consumer dto
	 * @param providerMsisdn
	 *            the provider msisdn
	 * @return the filtered consumers
	 */
	List<SharedAccountConsumerDTO> getFilteredConsumers(SharedAccountConsumerDTO sharedAccountConsumerDTO,
			Long providerMsisdn);

	/**
	 * Gets the filtered consumers.
	 * 
	 * @param newLimit
	 *            the new limit
	 * @param provMSISDN
	 *            the prov msisdn
	 * @param consumerMSISDN
	 *            the consumer msisdn
	 * @param productId
	 *            the product id
	 * @return the filtered consumers
	 * @throws ExecutionFailedException
	 *             the execution failed exception
	 * @throws EvaluationFailedException
	 *             the evaluation failed exception
	 * @throws RuleException
	 *             the rule exception
	 * @throws FDPServiceException
	 *             the fDP service exception
	 */
	Boolean upgradeRequest(Long newLimit, Long provMSISDN, Long consumerMSISDN, Long productId)
			throws ExecutionFailedException, EvaluationFailedException, RuleException, FDPServiceException;

	/**
	 * Gets the pending req for consumer.
	 * 
	 * @param consumerMSISDN
	 *            the consumer msisdn
	 * @param reqType
	 *            the req type
	 * @return the pending req for consumer
	 * @throws FDPServiceException
	 *             the fDP service exception
	 * @throws ExecutionFailedException 
	 */
	List<SharedAccountConsumerUIDTO> getPendingReqForConsumer(Long consumerMSISDN, ConsumerRequestType reqType)
			throws FDPServiceException, ExecutionFailedException;

	/**
	 * Gets the consumer usage.
	 * 
	 * @param providerMSISDN
	 *            the provider msisdn
	 * @param consumerMSISDN
	 *            the consumer msisdn
	 * @param productId
	 *            the product id
	 * @return the consumer usage
	 * @throws FDPServiceException
	 *             the fDP service exception
	 */
	UsageDTO getConsumerUsage(Long providerMSISDN, Long consumerMSISDN, Long productId) throws FDPServiceException;

	/**
	 * Reject add consumer.
	 * 
	 * @param userMsisdn
	 *            the user msisdn
	 * @param requestId
	 *            the request id
	 * @param productId
	 *            the product id
	 * @param providerMsisdn
	 *            the provider msisdn
	 * @throws FDPServiceException
	 *             the fDP service exception
	 */
	void rejectAddConsumer(Long userMsisdn, Long requestId, Long productId, Long providerMsisdn)
			throws FDPServiceException;

	/**
	 * Approve add consumer.
	 * 
	 * @param userMsisdn
	 *            the user msisdn
	 * @param requestId
	 *            the request id
	 * @param productId
	 *            the product id
	 * @param providerMsisdn
	 *            the provider msisdn
	 * @throws FDPServiceException
	 *             the fDP service exception
	 */
	void approveAddConsumer(Long userMsisdn, Long requestId, Long productId, Long providerMsisdn)
			throws FDPServiceException;

	/**
	 * Accept consumer limit upgrade.
	 * 
	 * @param approvalDTO
	 *            the approval dto
	 * @param userMsisdn
	 *            the user msisdn
	 * @throws FDPServiceException
	 *             the fDP service exception
	 */
	void acceptConsumerLimitUpgrade(ConsumerApprovalDTO approvalDTO, Long userMsisdn) throws FDPServiceException;
}
