package com.ericsson.fdp.business.sharedaccount.service.impl;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.List;
import java.util.Map;

import javax.annotation.Resource;
import javax.ejb.Stateless;
import javax.inject.Inject;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ericsson.fdp.FDPCacheable;
import com.ericsson.fdp.business.constants.FDPStepResponseConstants;
import com.ericsson.fdp.business.dto.ResponseDTO;
import com.ericsson.fdp.business.enums.AuxRequestParam;
import com.ericsson.fdp.business.enums.DisplayArea;
import com.ericsson.fdp.business.enums.ResponseType;
import com.ericsson.fdp.business.exception.RuleException;
import com.ericsson.fdp.business.node.AbstractNode;
import com.ericsson.fdp.business.node.impl.WebNode;
import com.ericsson.fdp.business.product.Product;
import com.ericsson.fdp.business.serviceprovisioning.rule.ServiceProvisioningRule;
import com.ericsson.fdp.business.sharedaccount.service.FDPSharedAccountUIService;
import com.ericsson.fdp.business.transaction.TransactionSequenceGeneratorService;
import com.ericsson.fdp.business.util.ProductUtil;
import com.ericsson.fdp.business.util.RequestUtil;
import com.ericsson.fdp.business.util.SharedAccountUtil;
import com.ericsson.fdp.common.constants.FDPConstant;
import com.ericsson.fdp.common.constants.JNDILookupConstant;
import com.ericsson.fdp.common.enums.ModuleType;
import com.ericsson.fdp.common.enums.ProductType;
import com.ericsson.fdp.common.enums.Status;
import com.ericsson.fdp.common.exception.FDPServiceException;
import com.ericsson.fdp.common.vo.FDPCircle;
import com.ericsson.fdp.core.dsm.framework.service.FDPCache;
import com.ericsson.fdp.core.exception.EvaluationFailedException;
import com.ericsson.fdp.core.exception.ExecutionFailedException;
import com.ericsson.fdp.core.policy.Policy;
import com.ericsson.fdp.core.request.FDPRequest;
import com.ericsson.fdp.core.request.FDPResponse;
import com.ericsson.fdp.core.request.impl.FDPRequestImpl;
import com.ericsson.fdp.core.serviceprovisioning.bean.FDPAppBag;
import com.ericsson.fdp.core.serviceprovisioning.bean.FDPMetaBag;
import com.ericsson.fdp.core.utils.ApplicationConfigUtil;
import com.ericsson.fdp.core.utils.CircleCodeFinder;
import com.ericsson.fdp.dao.constants.SharedAccountRequestConstant;
import com.ericsson.fdp.dao.dto.product.ProductGeneralInfoDTO;
import com.ericsson.fdp.dao.dto.sharedaccount.ConsumerApprovalDTO;
import com.ericsson.fdp.dao.dto.sharedaccount.ConsumerSearchDTO;
import com.ericsson.fdp.dao.dto.sharedaccount.SharedAccGpDTO;
import com.ericsson.fdp.dao.dto.sharedaccount.SharedAccountConsumerDTO;
import com.ericsson.fdp.dao.dto.sharedaccount.SharedAccountConsumerUIDTO;
import com.ericsson.fdp.dao.dto.sharedaccount.SharedAccountDetailsDTO;
import com.ericsson.fdp.dao.dto.sharedaccount.SharedAccountProviderDTO;
import com.ericsson.fdp.dao.dto.sharedaccount.SharedAccountRequestDTO;
import com.ericsson.fdp.dao.dto.sharedaccount.UsageDTO;
import com.ericsson.fdp.dao.enums.ChargingType;
import com.ericsson.fdp.dao.enums.ConsumerRequestType;
import com.ericsson.fdp.dao.enums.ConsumerStatusEnum;
import com.ericsson.fdp.dao.enums.EntityType;
import com.ericsson.fdp.dao.enums.FDPServiceProvSubType;
import com.ericsson.fdp.dao.enums.FDPServiceProvType;
import com.ericsson.fdp.dao.enums.ProductAdditionalInfoEnum;
import com.ericsson.fdp.dao.enums.SharedAccountGroupStatus;
import com.ericsson.fdp.dao.enums.SharedAccountResponseType;
import com.ericsson.fdp.dao.enums.StepNameEnum;
import com.ericsson.fdp.dao.enums.UtUpgradeType;
import com.ericsson.fdp.dao.exception.FDPConcurrencyException;
import com.ericsson.fdp.dao.fdpadmin.FDPProductAdditionalInfoDAO;
import com.ericsson.fdp.dao.fdpadmin.FDPProductDAO;
import com.ericsson.fdp.dao.fdpadmin.FDPSharedAccountConsumerDAO;
import com.ericsson.fdp.dao.fdpadmin.FDPSharedAccountGroupDAO;
import com.ericsson.fdp.dao.fdpadmin.FDPSharedAccountReqDAO;
import com.ericsson.fdp.dao.util.ServiceProvDataConverterUtil;

/**
 * The Class SharedAccountUIServiceImpl.
 * 
 * @author Ericsson
 */
@Stateless
public class FDPSharedAccountUIServiceImpl implements FDPSharedAccountUIService {

	/** The Constant LOGGER. */
	private static final Logger LOGGER = LoggerFactory.getLogger(FDPSharedAccountUIServiceImpl.class);

	/** The fdp shared account group dao. */
	@Inject
	private FDPSharedAccountGroupDAO fdpSharedAccountGroupDAO;

	/** The fdp product dao. */
	@Inject
	private FDPProductDAO fdpProductDAO;

	/** The fdp product additional info dao. */
	@Inject
	private FDPProductAdditionalInfoDAO fdpProductAdditionalInfoDAO;

	/** The fdp cache. */
	@Resource(lookup = "java:app/fdpBusiness-1.0/MetaDataCache")
	private FDPCache<FDPMetaBag, FDPCacheable> fdpMetaDataCache;

	/** The application cache. */
	@Resource(lookup = "java:app/fdpCoreServices-1.0/ApplicationConfigCache")
	private FDPCache<FDPAppBag, Object> applicationConfigCache;

/*	*//** The transaction sequence dao. *//*
	@Inject
	private TransactionSequenceDAO transactionSequenceDAO;
*/	
	@Resource(lookup = JNDILookupConstant.TRANSACTION_ID_GENERATOR_SERVICE)
	private TransactionSequenceGeneratorService generatorService;

	/** The shared account req dao. */
	@Inject
	private FDPSharedAccountReqDAO sharedAccountReqDAO;

	/** The shared acc group dao. */
	@Inject
	private FDPSharedAccountGroupDAO sharedAccGroupDAO;

	/** The shared account consumer dao. */
	@Inject
	private FDPSharedAccountConsumerDAO sharedAccountConsumerDAO;

	/** The product dao. */
	@Inject
	private FDPProductDAO productDAO;

	/** The product additional info dao. */
	@Inject
	private FDPProductAdditionalInfoDAO productAdditionalInfoDAO;

	@Override
	public List<SharedAccountProviderDTO> getProviderSharedAccounts(final Long providerId) throws FDPServiceException {
		final List<SharedAccGpDTO> shrAccGroupList = fdpSharedAccountGroupDAO.getAllSharedAccGpWithConsumerCount(
				providerId, SharedAccountGroupStatus.ACTIVE);
		final List<Long> productIdList = new ArrayList<Long>();
		for (final SharedAccGpDTO shrGrpDto : shrAccGroupList) {
			productIdList.add(shrGrpDto.getProduct().getProductId());
		}
		final Map<Long, ProductGeneralInfoDTO> productIdInfoMap = fdpProductDAO.getProductsWithIdMap(productIdList,
				Boolean.TRUE, Boolean.FALSE, null);
		final List<SharedAccountProviderDTO> result = new ArrayList<SharedAccountProviderDTO>();
		for (final SharedAccGpDTO shrAccGpDto : shrAccGroupList) {
			final SharedAccountProviderDTO shrAccProDto = new SharedAccountProviderDTO();
			shrAccProDto.setSharedAccName(shrAccGpDto.getGroupName());
			final ProductGeneralInfoDTO productInfo = productIdInfoMap.get(shrAccGpDto.getProduct().getProductId());
			shrAccProDto.setProductName(productInfo.getProductName());
			shrAccProDto.setProductType(productInfo.getProductType());

			FDPRequestImpl fdpRequest;
			try {
				fdpRequest = createFDPRequest(providerId);
				final Calendar expiryDate = (Calendar) SharedAccountUtil.executeGetOffer(fdpRequest,
						shrAccGpDto.getOfferId()).get(FDPStepResponseConstants.OFFER_EXPIRY_DATE);
				shrAccProDto.setExpiryDate(expiryDate);
			} catch (final ExecutionFailedException e) {
				throw new FDPServiceException("Get provider shared accounts failed.", e);
			}

			shrAccProDto.setNoOfConsumer(shrAccGpDto.getChildCount());
			shrAccProDto.setSelfConsumer(shrAccProDto.getSelfConsumer());
			shrAccProDto.setId(shrAccGpDto.getSharedAccID());
			shrAccProDto.setProductId(shrAccGpDto.getProduct().getProductId());

			final String productLimit = (String) fdpRequest.getValueFromStep(StepNameEnum.VALIDATION_STEP.getValue(),
					SharedAccountResponseType.CONSUMER_LIMIT.name());
			shrAccProDto.setProductLimit(productLimit);
			result.add(shrAccProDto);
		}

		return result;
	}

	@Override
	public List<SharedAccountConsumerUIDTO> getProviderListForConsumer(final Long consumerMSISDN)
			throws FDPServiceException {
		final List<SharedAccGpDTO> shrAccGroupList = fdpSharedAccountGroupDAO.getProvidersDetailsForConsumer(
				consumerMSISDN, SharedAccountGroupStatus.ACTIVE);
		final List<Long> productIdList = new ArrayList<Long>();
		for (final SharedAccGpDTO shrGrpDto : shrAccGroupList) {
			productIdList.add(shrGrpDto.getProduct().getProductId());
		}
		final Map<Long, ProductGeneralInfoDTO> productIdInfoMap = fdpProductDAO.getProductsWithIdMap(productIdList,
				Boolean.TRUE, Boolean.FALSE, null);
		List<SharedAccountConsumerUIDTO> result = null;
		if (shrAccGroupList != null && !shrAccGroupList.isEmpty()) {
			result = new ArrayList<SharedAccountConsumerUIDTO>();
			for (final SharedAccGpDTO shrAccGrp : shrAccGroupList) {
				final SharedAccountConsumerUIDTO shrAccConUiDto = new SharedAccountConsumerUIDTO();
				shrAccConUiDto.setProviderMobNo(shrAccGrp.getGroupProviderMSISDN().toString());
				shrAccConUiDto.setProductName(productIdInfoMap.get(shrAccGrp.getProduct().getProductId())
						.getProductName());
				shrAccConUiDto.setProductId(shrAccGrp.getProduct().getProductId());
				result.add(shrAccConUiDto);
			}
		}
		return result;
	}

	@Override
	public List<SharedAccountConsumerUIDTO> getPendingReqForConsumer(final Long consumerMSISDN,
			final ConsumerRequestType reqType) throws FDPServiceException {
		final FDPCircle circle = CircleCodeFinder.getFDPCircleByMsisdn(consumerMSISDN.toString(),
				applicationConfigCache);
		if (circle == null) {
			throw new FDPServiceException("Circle Not Found for msisdn " + consumerMSISDN);
		}
		final List<SharedAccountRequestDTO> requestDTOListList = sharedAccountReqDAO.getPendingRequestForConsumer(
				consumerMSISDN, circle.getCircleId(), reqType);
		List<SharedAccountConsumerUIDTO> pendingReqList = null;
		if (requestDTOListList != null && !requestDTOListList.isEmpty()) {
			pendingReqList = new ArrayList<SharedAccountConsumerUIDTO>();
			for (final SharedAccountRequestDTO requestDTO : requestDTOListList) {
				final SharedAccountConsumerUIDTO consumerReq = new SharedAccountConsumerUIDTO();
				consumerReq.setId(requestDTO.getAccReqNumber());
				consumerReq.setProviderMobNo(requestDTO.getSenderMsisdn().toString());
				Product product;
				try {
					product = SharedAccountUtil.getProduct(circle, requestDTO.getEntityId().toString());
				} catch (final ExecutionFailedException e) {
					throw new FDPServiceException(e);
				}
				consumerReq
						.setOfferId(product.getAdditionalInfo(ProductAdditionalInfoEnum.SHARED_ACC_OFFER_ID_MAPPING));
				consumerReq.setProductName(product.getProductName());
				consumerReq.setProductId(product.getProductId());
				pendingReqList.add(consumerReq);
			}
		}
		return pendingReqList;
	}

	// @Override
	// public List<SharedAccountConsumerUIDTO> getConsumerListForProvider(Long
	// providerMobNo) {
	// List<SharedAccountConsumerUIDTO> result = new
	// ArrayList<SharedAccountConsumerUIDTO>();
	// result.add(new SharedAccountConsumerUIDTO(1L, 100L, 30L, "+919888977969",
	// "consumer1", "Group1", "pending", true, 100L));
	// result.add(new SharedAccountConsumerUIDTO(2L, 200L, 20L, "+919888977969",
	// "consumer2", "Group2", "pending", true, 110L));
	// result.add(new SharedAccountConsumerUIDTO(3L, 300L, 40L, "+919888977969",
	// "consumer3", "Group3", "pending", true, 130L));
	// result.add(new SharedAccountConsumerUIDTO(4L, 400L, 340L,
	// "+919888977969", "consumer4", "Group4", "pending", true, 140L));
	// return result;
	// }

	@Override
	public List<SharedAccountConsumerDTO> getConsumerRequestListForApproval(final Long providerMobNo,
			final Long productId) {
		final List<SharedAccountRequestDTO> requestDTOList = sharedAccountReqDAO
				.getSharedAccountRequestForGroupByStatusAndRequestType(providerMobNo, productId,
						Arrays.asList(ConsumerStatusEnum.PENDING.getStatusCode()),
						ConsumerRequestType.CONSUMER_UT_UPGRADE, Boolean.FALSE);
		final List<SharedAccountConsumerDTO> consumerDTOList = new ArrayList<SharedAccountConsumerDTO>();
		// get product id offer id map
		final Map<Long, Long> productIdOfferIdValueMap = fdpProductAdditionalInfoDAO
				.getSharedAccountAdditionalInfoByProductIds(Arrays.asList(productId),
						ProductAdditionalInfoEnum.PROVIDER_OFFER_ID_MAPPING);
		final Long offerIdValue = productIdOfferIdValueMap.get(productId);
		for (final SharedAccountRequestDTO requestDTO : requestDTOList) {
			final SharedAccountConsumerDTO consumerDTO = sharedAccountConsumerDAO.getConsumer(
					requestDTO.getSenderMsisdn(), offerIdValue);
			consumerDTO.setConsumerNewLimit(Integer.valueOf(requestDTO.getConsumerAddInfo()));
			consumerDTO.setSharedAccountReqId(requestDTO.getSharedAccountReqId());
			consumerDTOList.add(consumerDTO);
		}
		return consumerDTOList;
	}

	@Override
	public List<ProductGeneralInfoDTO> getProductGeneralInfoDto(final Long providerMSISDN, final ProductType productType) {
		final List<SharedAccGpDTO> shrAccGroupList = fdpSharedAccountGroupDAO.getAllSharedAccGpWithConsumerCount(
				providerMSISDN, SharedAccountGroupStatus.ACTIVE);
		final List<Long> productIdList = new ArrayList<Long>();
		for (final SharedAccGpDTO shrGrpDto : shrAccGroupList) {
			productIdList.add(shrGrpDto.getProduct().getProductId());
		}
		Map<Long, ProductGeneralInfoDTO> productIdInfoMap;
		if (productType != null) {
			productIdInfoMap = fdpProductDAO.getProductsWithIdMap(productIdList, Boolean.FALSE, Boolean.TRUE,
					productType);
		} else {
			productIdInfoMap = fdpProductDAO.getProductsWithIdMap(productIdList, Boolean.FALSE, Boolean.FALSE, null);
		}

		final List<ProductGeneralInfoDTO> productInfoList = new ArrayList<ProductGeneralInfoDTO>();
		productInfoList.addAll(productIdInfoMap.values());
		return productInfoList;
	}

	@Override
	public List<String> saveConsumerRequest(final List<SharedAccountConsumerDTO> consumerDTOList, final Long productId,
			final Long providerMsisdn) throws FDPServiceException {
		final List<String> failedRecordList = new ArrayList<String>();
		try {
			final String value = ServiceProvDataConverterUtil.getKeyForServiceProvMetaBag(productId,
					FDPServiceProvType.PRODUCT, FDPServiceProvSubType.SHARED_ACCOUNT_ADD_CONSUMER);
			final FDPRequestImpl fdpRequest = createFDPRequest(providerMsisdn);
			final ServiceProvisioningRule serviceProvisioningRule = getServiceProvisioningKey(productId,
					FDPServiceProvSubType.SHARED_ACCOUNT_ADD_CONSUMER, fdpRequest);
			if (serviceProvisioningRule != null) {
				// create fdpRequest Object
				for (final SharedAccountConsumerDTO consumerDTO : consumerDTOList) {
					fdpRequest.putAuxiliaryRequestParameter(AuxRequestParam.CONSUMER_NAME,
							consumerDTO.getConsumerName());
					fdpRequest.putAuxiliaryRequestParameter(AuxRequestParam.CONSUMER_MSISDN, consumerDTO
							.getConsumerMsisdn().toString());
					fdpRequest.putAuxiliaryRequestParameter(AuxRequestParam.PROVIDER_MSISDN, providerMsisdn.toString());
					String message = null;
					String reason = null;
					try {
						RequestUtil.updateProductAndSPInWebRequest(fdpRequest, productId.toString(), value);
						message = consumerDTO.getConsumerName() + FDPConstant.COMMA + consumerDTO.getConsumerMsisdn();
						final FDPResponse response = serviceProvisioningRule.execute(fdpRequest);
						if (Status.FAILURE.equals(response.getExecutionStatus())) {
							reason = response.getResponseString().get(0).getCurrDisplayText(DisplayArea.COMPLETE);
							LOGGER.warn(reason);
						}
					} catch (final RuleException e) {
						LOGGER.warn(e.getMessage(), e);
						reason = e.getMessage();
					} catch (final EvaluationFailedException e) {
						LOGGER.warn(e.getMessage(), e);
						reason = e.getMessage();
					}
					if (reason != null) {
						failedRecordList.add(message + FDPConstant.REASON + reason);
					}
				}
			} else {
				LOGGER.debug("SP SHARED_ACCOUNT_ADD_CONSUMER not found for Product " + productId);
				throw new FDPServiceException("Service Provisioning Flow not configured for product");
			}
		} catch (final ExecutionFailedException e) {
			throw new FDPServiceException("Add consumer request failed.", e);
		} catch (final EvaluationFailedException e) {
			throw new FDPServiceException("Add consumer request failed.", e);
		}
		return failedRecordList;
	}

	@Override
	public void removeConsumerRequest(final List<Long> consumerList, final Long productId, final Long providerMsisdn)
			throws FDPServiceException {

		try {
			for (final Long consumerMsisdn : consumerList) {
				final String value = ServiceProvDataConverterUtil.getKeyForServiceProvMetaBag(productId,
						FDPServiceProvType.PRODUCT, FDPServiceProvSubType.SHARED_ACCOUNT_REMOVE_CONSUMER);
				final FDPRequestImpl fdpRequest = createFDPRequest(providerMsisdn);
				final ServiceProvisioningRule serviceProvisioningRule = getServiceProvisioningKey(productId,
						FDPServiceProvSubType.SHARED_ACCOUNT_REMOVE_CONSUMER, fdpRequest);
				if (serviceProvisioningRule != null) {
					fdpRequest.putAuxiliaryRequestParameter(AuxRequestParam.CONSUMER_MSISDN, consumerMsisdn.toString());
					RequestUtil.updateProductAndSPInWebRequest(fdpRequest, productId.toString(), value);
					final FDPResponse response = serviceProvisioningRule.execute(fdpRequest);
					if (Status.FAILURE.equals(response.getExecutionStatus())) {
						throw new FDPServiceException(response.getResponseString().get(0)
								.getCurrDisplayText(DisplayArea.COMPLETE));
					}
				} else {
					throw new FDPServiceException("Service Provisioning Flow not configured for product");
				}
			}
		} catch (final ExecutionFailedException e) {
			throw new FDPServiceException("Remove consumer request failed.", e);
		} catch (final RuleException e) {
			throw new FDPServiceException("Remove consumer request failed.", e);
		} catch (final EvaluationFailedException e) {
			throw new FDPServiceException("Remove consumer request failed.", e);
		}
	}

	@Override
	public void viewTopNConsumerRequest(final Integer nValue, final Long productId, final Long providerMsisdn)
			throws FDPServiceException {

		try {
			final String value = ServiceProvDataConverterUtil.getKeyForServiceProvMetaBag(productId,
					FDPServiceProvType.PRODUCT, FDPServiceProvSubType.SHARED_ACCOUNT_TOP_N_USAGE);
			final FDPRequestImpl fdpRequest = createFDPRequest(providerMsisdn);
			final ServiceProvisioningRule serviceProvisioningRule = getServiceProvisioningKey(productId,
					FDPServiceProvSubType.SHARED_ACCOUNT_TOP_N_USAGE, fdpRequest);
			if (serviceProvisioningRule != null) {
				fdpRequest.putAuxiliaryRequestParameter(AuxRequestParam.PROVIDER_MSISDN, providerMsisdn.toString());
				fdpRequest.putAuxiliaryRequestParameter(AuxRequestParam.N_VALUE, nValue.toString());
				RequestUtil.updateProductAndSPInWebRequest(fdpRequest, productId.toString(), value);
				final FDPResponse response = serviceProvisioningRule.execute(fdpRequest);
				if (Status.FAILURE.equals(response.getExecutionStatus())) {
					throw new FDPServiceException(response.getResponseString().get(0)
							.getCurrDisplayText(DisplayArea.COMPLETE));
				}
			} else {
				throw new FDPServiceException("Service Provisioning Flow not configured for product");
			}
		} catch (final RuleException e) {
			throw new FDPServiceException("top n consumer request failed.", e);
		} catch (final ExecutionFailedException e) {
			throw new FDPServiceException("top n consumer request failed.", e);
		} catch (final EvaluationFailedException e) {
			throw new FDPServiceException("top n consumer request failed.", e);
		}
	}

	@Override
	public List<SharedAccGpDTO> getAllSharedAccGp(final Long providerMSISDN, final Long productId) {
		final List<SharedAccGpDTO> groupDTOList = fdpSharedAccountGroupDAO.getAllSharedAccGp(providerMSISDN, productId);
		return groupDTOList;
	}

	@Override
	public void updateSharedAccountGroup(final SharedAccGpDTO sharedAccGpDTO) throws FDPServiceException {
		try {
			fdpSharedAccountGroupDAO.updateSharedAccountGroup(sharedAccGpDTO);
		} catch (final FDPConcurrencyException e) {
			throw new FDPServiceException("Shared account group update request failed", e);
		}
	}

	@Override
	public List<SharedAccountConsumerDTO> getConsumersListForSharedAccount(final Long providerMobNo,
			final Long productId) {
		final List<SharedAccountRequestDTO> requestDTOList = sharedAccountReqDAO
				.getSharedAccountRequestForGroupByStatusAndRequestType(
						providerMobNo,
						productId,
						Arrays.asList(ConsumerStatusEnum.PENDING.getStatusCode(),
								ConsumerStatusEnum.REJECTED.getStatusCode()), ConsumerRequestType.ADD_CONSUMER,
						Boolean.TRUE);

		final List<SharedAccountConsumerDTO> consumerList = sharedAccountConsumerDAO
				.getSharedAccountConsumersByProviderMsisdnAndProductId(providerMobNo, productId);
		SharedAccountConsumerDTO consumerDTO = null;
		for (final SharedAccountRequestDTO requestDTO : requestDTOList) {
			consumerDTO = populateConsumerDTOFromRequestDTO(requestDTO);
			consumerList.add(consumerDTO);
		}
		return consumerList;
	}

	@Override
	public void updateSharedAccountApprovalRequest(final ConsumerApprovalDTO approvalDTO, final Long providerMsisdn)
			throws FDPServiceException {
		final String fieldSeprator = SharedAccountRequestConstant.ADDITIONAL_INFO_FIELD_SEPARATOR;
		final String keyValueSeparator = SharedAccountRequestConstant.ADDITIONAL_INFO_KEY_VALUE_SEPARATOR;
		final SharedAccountRequestDTO requestDTO = new SharedAccountRequestDTO();
		requestDTO.setSharedAccountReqId(approvalDTO.getSharedAccountRequestId());
		requestDTO.setConsumerStatus(approvalDTO.getRequestStatus());
		requestDTO.setModifiedBy(providerMsisdn.toString());
		if (ConsumerStatusEnum.REJECTED.equals(approvalDTO.getRequestStatus())
				|| ConsumerStatusEnum.COMPLETED.equals(approvalDTO.getRequestStatus())) {
			requestDTO.setConsumerAddInfo(SharedAccountRequestConstant.CONSUMER_REMARKS + keyValueSeparator
					+ approvalDTO.getRemarks());
		} else {
			requestDTO.setConsumerAddInfo(SharedAccountRequestConstant.CONSUMER_LIMIT_UPGRADE_VALUE + keyValueSeparator
					+ approvalDTO.getConsumerNewLimit() + fieldSeprator + SharedAccountRequestConstant.CONSUMER_REMARKS
					+ keyValueSeparator + approvalDTO.getRemarks());
		}
		try {
			//System.out.println("Updating the request dto values " + requestDTO);
			sharedAccountReqDAO.updateSharedAccountRequest(requestDTO);
		} catch (final FDPConcurrencyException e) {
			throw new FDPServiceException("Shared account consumer approval/reject request failed", e);
		}
	}

	/**
	 * Gets the service provisioning key.
	 * 
	 * @param productId
	 *            the product id
	 * @param serviceProvSubType
	 *            the shared account add consumer
	 * @param fdpRequest
	 *            the fdp request
	 * @return the service provisioning key
	 * @throws EvaluationFailedException
	 *             the evaluation failed exception
	 */
	private ServiceProvisioningRule getServiceProvisioningKey(final Long productId,
			final FDPServiceProvSubType serviceProvSubType, final FDPRequest fdpRequest)
			throws EvaluationFailedException {
		final String value = ServiceProvDataConverterUtil.getKeyForServiceProvMetaBag(productId,
				FDPServiceProvType.PRODUCT, serviceProvSubType);
		final FDPCacheable fdpSPCacheable = fdpMetaDataCache.getValue(new FDPMetaBag(fdpRequest.getCircle(),
				ModuleType.SP_PRODUCT, value));
		if (fdpSPCacheable == null) {
			LOGGER.debug(serviceProvSubType + " is not found for product " + productId);
			throw new EvaluationFailedException(serviceProvSubType + " is not found for product " + productId);
		}
		ServiceProvisioningRule serviceProvisioningRule = null;
		if (fdpSPCacheable instanceof ServiceProvisioningRule) {
			LOGGER.debug(serviceProvSubType + " is found for product " + productId);
			serviceProvisioningRule = (ServiceProvisioningRule) fdpSPCacheable;
			RequestUtil.updateProductAndSPInWebRequest(fdpRequest, productId.toString(), value);
		}
		return serviceProvisioningRule;
	}

	/**
	 * Creates the fdp request.
	 * 
	 * @param providerMsisdn
	 *            the provider msisdn
	 * @return the fDP request impl
	 * @throws ExecutionFailedException
	 *             the execution failed exception
	 * @throws FDPServiceException
	 */
	private FDPRequestImpl createFDPRequest(final Long providerMsisdn) throws ExecutionFailedException,
			FDPServiceException {
		final FDPCircle fdpCircle = CircleCodeFinder.getFDPCircleByMsisdn(providerMsisdn.toString(),
				applicationConfigCache);
		if (fdpCircle == null) {
			throw new FDPServiceException("Circle Not Found for msisdn " + providerMsisdn);
		}

		final String msisdn = SharedAccountUtil.getFullMsisdn(providerMsisdn);
		return RequestUtil.getWebRequest(msisdn, generateTransactionId(), fdpCircle);
	}

	@Override
	public void deleteAccount(final Long accountId, final Long providerMsisdn, final Long sharedAccountProductId)
			throws FDPServiceException {
		try {
			final String value = ServiceProvDataConverterUtil.getKeyForServiceProvMetaBag(sharedAccountProductId,
					FDPServiceProvType.PRODUCT, FDPServiceProvSubType.SHARED_ACCOUNT_DELETE_ACCOUNT);
			final FDPRequestImpl fdpRequest = createFDPRequest(providerMsisdn);
			final ServiceProvisioningRule serviceProvisioningRule = getServiceProvisioningKey(sharedAccountProductId,
					FDPServiceProvSubType.SHARED_ACCOUNT_DELETE_ACCOUNT, fdpRequest);
			if (serviceProvisioningRule != null) {
				// create fdpRequest Object

				fdpRequest.putAuxiliaryRequestParameter(AuxRequestParam.CONSUMER_MSISDN, providerMsisdn.toString());
				fdpRequest.putAuxiliaryRequestParameter(AuxRequestParam.PROVIDER_MSISDN, providerMsisdn.toString());
				RequestUtil.updateProductAndSPInWebRequest(fdpRequest, sharedAccountProductId.toString(), value);
				final FDPResponse response = serviceProvisioningRule.execute(fdpRequest);
				if (Status.FAILURE.equals(response.getExecutionStatus())) {
					throw new FDPServiceException(response.getResponseString().get(0)
							.getCurrDisplayText(DisplayArea.COMPLETE));
				}
			} else {
				throw new FDPServiceException("Service Provisioning Flow not configured for product");
			}
		} catch (final RuleException e) {
			throw new FDPServiceException("Delete account failed.", e);
		} catch (final ExecutionFailedException e) {
			throw new FDPServiceException("Delete account failed.", e);
		} catch (final EvaluationFailedException e) {
			throw new FDPServiceException("Delete account failed.", e);
		}
	}

	@Override
	public void detachProvider(final Long accountId, final Long providerMsisdn, final Long sharedAccountProductId)
			throws FDPServiceException {
		try {
			final String value = ServiceProvDataConverterUtil.getKeyForServiceProvMetaBag(sharedAccountProductId,
					FDPServiceProvType.PRODUCT, FDPServiceProvSubType.SHARED_ACCOUNT_DETACH_PROVIDER);
			final FDPRequestImpl fdpRequest = createFDPRequest(providerMsisdn);
			final ServiceProvisioningRule serviceProvisioningRule = getServiceProvisioningKey(sharedAccountProductId,
					FDPServiceProvSubType.SHARED_ACCOUNT_DETACH_PROVIDER, fdpRequest);
			if (serviceProvisioningRule != null) {
				// create fdpRequest Object

				fdpRequest.putAuxiliaryRequestParameter(AuxRequestParam.CONSUMER_MSISDN, providerMsisdn.toString());
				fdpRequest.putAuxiliaryRequestParameter(AuxRequestParam.PROVIDER_MSISDN, providerMsisdn.toString());
				RequestUtil.updateProductAndSPInWebRequest(fdpRequest, sharedAccountProductId.toString(), value);
				final FDPResponse response = serviceProvisioningRule.execute(fdpRequest);
				if (Status.FAILURE.equals(response.getExecutionStatus())) {
					throw new FDPServiceException(response.getResponseString().get(0)
							.getCurrDisplayText(DisplayArea.COMPLETE));
				}
			} else {
				LOGGER.debug("Service Provisioning Flow not configured for product");
				throw new FDPServiceException("Service Provisioning Flow not configured for product");
			}
		} catch (final RuleException e) {
			throw new FDPServiceException("Detach provider failed.", e);
		} catch (final ExecutionFailedException e) {
			throw new FDPServiceException("Detach provider failed.", e);
		} catch (final EvaluationFailedException e) {
			throw new FDPServiceException("Delete account failed.", e);
		}
	}

	@Override
	public SharedAccountDetailsDTO getSharedAccountDetails(final Long sharedAccountId) throws ExecutionFailedException,
			FDPServiceException {
		final SharedAccGpDTO sharedAccGroup = sharedAccGroupDAO.getSharedAccountGroupById(sharedAccountId);
		final String productName = productDAO.getProductName(sharedAccGroup.getProductId());
		final Map<Integer, String> additionalInfo = productAdditionalInfoDAO
				.getProductAdditionalInfoMapById(sharedAccGroup.getProductId());
		final Integer productConsumerLimit = Integer.parseInt(additionalInfo
				.get(ProductAdditionalInfoEnum.CONSUMER_LIMIT.getKey()));
		final Long offerId = Long.parseLong(additionalInfo.get(ProductAdditionalInfoEnum.SHARED_ACC_OFFER_ID.getKey()));
		final SharedAccountDetailsDTO sharedAccountDetails = new SharedAccountDetailsDTO(sharedAccGroup.getGroupName(),
				sharedAccGroup.getGroupLimit(), productName, sharedAccGroup.getNoOfConsumers(), productConsumerLimit);
		if (sharedAccountConsumerDAO.getConsumer(sharedAccGroup.getGroupProviderMSISDN(), offerId) != null) {
			sharedAccountDetails.setSelfConsumer(true);
		} else {
			sharedAccountDetails.setSelfConsumer(false);
		}
		final FDPRequest request = createFDPRequest(sharedAccGroup.getGroupProviderMSISDN());

		final Map<String, Object> result = SharedAccountUtil.executeGetOffer(request, offerId);
		final Long usageCounter = (Long) result.get(FDPStepResponseConstants.PRODUCT_USAGE_COUNTER);
		final Long usageThreshold = (Long) result.get(FDPStepResponseConstants.PRODUCT_USAGE_THRESHOLD);
		sharedAccountDetails.setUsageCounter(usageCounter);
		sharedAccountDetails.setUsageThreshold(usageThreshold);
		return sharedAccountDetails;
	}

	@Override
	public Boolean updateSharedAccountName(final Long sharedAccId, final String newSharedAccName,
			final String modifiedBy) {
		return sharedAccGroupDAO.updateSharedAccountGroupName(sharedAccId, newSharedAccName, modifiedBy);
	}

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
	@Override
	public UsageDTO getConsumerUsage(final Long providerMSISDN, final Long consumerMSISDN, final Long productId)
			throws FDPServiceException {
		UsageDTO result = null;
		try {
			result = getConsumerUsageValue(providerMSISDN, consumerMSISDN, productId);
			final UsageDTO threshold = getConsumerThresholdValue(providerMSISDN, consumerMSISDN, productId);
			if (result != null && threshold != null) {
				result.setMaxUsageValue(threshold.getMaxUsageValue());
			} else {
				throw new FDPServiceException("Unable to find CONSUMER USAGE");
			}
		} catch (final ExecutionFailedException e) {
			LOGGER.error("Exception Occured : ", e);
			throw new FDPServiceException("Error", e);
		} catch (final RuleException e) {
			LOGGER.error("Exception Occured : ", e);
			throw new FDPServiceException("Error", e);
		} catch (final EvaluationFailedException e) {
			LOGGER.error("Exception Occured : ", e);
			throw new FDPServiceException("Error", e);
		} catch (final FDPServiceException e) {
			LOGGER.error("Exception Occured : ", e);
			throw new FDPServiceException(e.getMessage(), e);
		}catch (final Exception e) {
			LOGGER.error("Exception Occured : ", e);
			e.printStackTrace();
			throw new FDPServiceException("Error", e);
		}
		return result;
	}

	/**
	 * Gets the consumer usage value.
	 * 
	 * @param providerMSISDN
	 *            the provider msisdn
	 * @param consumerMSISDN
	 *            the consumer msisdn
	 * @param productId
	 *            the product id
	 * @return the consumer usage value
	 * @throws ExecutionFailedException
	 *             the execution failed exception
	 * @throws EvaluationFailedException
	 *             the evaluation failed exception
	 * @throws RuleException
	 *             the rule exception
	 * @throws FDPServiceException
	 *             the fDP service exception
	 */
	private UsageDTO getConsumerUsageValue(final Long providerMSISDN, final Long consumerMSISDN, final Long productId)
			throws ExecutionFailedException, EvaluationFailedException, RuleException, FDPServiceException {
		UsageDTO result = null;
		final FDPRequestImpl fdpRequest = createFDPRequest(providerMSISDN);
		final ServiceProvisioningRule serviceProvisioningRule = getServiceProvisioningKey(productId,
				FDPServiceProvSubType.SHARED_ACCOUNT_VIEW_USAGE_UC, fdpRequest);
		fdpRequest.putAuxiliaryRequestParameter(AuxRequestParam.CONSUMER_MSISDN, consumerMSISDN);
		fdpRequest.putAuxiliaryRequestParameter(AuxRequestParam.PROVIDER_MSISDN, providerMSISDN);
		final FDPResponse response = serviceProvisioningRule.execute(fdpRequest);
		if (Status.FAILURE.equals(response.getExecutionStatus())) {
			throw new FDPServiceException(response.getResponseString().get(0).getCurrDisplayText(DisplayArea.COMPLETE));
		}
		final Object consumerLimit = fdpRequest.getValueFromStep(StepNameEnum.VALIDATION_STEP.getValue(),
				SharedAccountResponseType.CONSUMER_LIMIT.name());
		final Object consumerThreshold = fdpRequest.getValueFromStep(StepNameEnum.VALIDATION_STEP.getValue(),
				SharedAccountResponseType.CONSUMER_THRESHOLD_UNIT.name());
		if (consumerLimit != null && consumerThreshold != null) {
			result = new UsageDTO(consumerLimit.toString(), consumerThreshold.toString(), null);
		} else {
			LOGGER.debug("unable to get CONSUMER_LIMIT and CONSUMER_THRESHOLD_UNIT from SP Step SHARED_ACCOUNT_VIEW_USAGE_UC.");
		}
		return result;
	}

	/**
	 * Gets the consumer threshold value.
	 * 
	 * @param providerMSISDN
	 *            the provider msisdn
	 * @param consumerMSISDN
	 *            the consumer msisdn
	 * @param productId
	 *            the product id
	 * @return the consumer threshold value
	 * @throws ExecutionFailedException
	 *             the execution failed exception
	 * @throws EvaluationFailedException
	 *             the evaluation failed exception
	 * @throws RuleException
	 *             the rule exception
	 * @throws FDPServiceException
	 *             the fDP service exception
	 */
	private UsageDTO getConsumerThresholdValue(final Long providerMSISDN, final Long consumerMSISDN,
			final Long productId) throws ExecutionFailedException, EvaluationFailedException, RuleException,
			FDPServiceException {
		UsageDTO result = null;
		final FDPRequestImpl fdpRequest = createFDPRequest(providerMSISDN);
		final ServiceProvisioningRule serviceProvisioningRule = getServiceProvisioningKey(productId,
				FDPServiceProvSubType.SHARED_ACCOUNT_VIEW_USAGE_UT, fdpRequest);
		fdpRequest.putAuxiliaryRequestParameter(AuxRequestParam.CONSUMER_MSISDN, consumerMSISDN);
		fdpRequest.putAuxiliaryRequestParameter(AuxRequestParam.PROVIDER_MSISDN, providerMSISDN);
		final FDPResponse response = serviceProvisioningRule.execute(fdpRequest);
		if (Status.FAILURE.equals(response.getExecutionStatus())) {
			throw new FDPServiceException(response.getResponseString().get(0).getCurrDisplayText(DisplayArea.COMPLETE));
		}
		final Object consumerLimit = fdpRequest.getValueFromStep(StepNameEnum.VALIDATION_STEP.getValue(),
				SharedAccountResponseType.CONSUMER_LIMIT.name());
		if (consumerLimit != null) {
			result = new UsageDTO(null, consumerLimit.toString(), null);
		} else {
			LOGGER.debug("unable to get CONSUMER_THRESHOLD_LIMIT from SP Step SHARED_ACCOUNT_VIEW_TOTAL_USAGE_UT.");
		}
		return result;
	}

	@Override
	public List<SharedAccountConsumerDTO> getFilteredConsumers(final SharedAccountConsumerDTO sharedAccountConsumerDTO,
			final Long providerMsisdn) {

		final ConsumerSearchDTO searchDTO = new ConsumerSearchDTO();
		searchDTO.setConsumerName(sharedAccountConsumerDTO.getConsumerName());
		searchDTO.setConsumerMsisdn(sharedAccountConsumerDTO.getConsumerMsisdn());
		searchDTO.setProviderMsisdn(providerMsisdn);
		searchDTO.setSharedAccountProductId(sharedAccountConsumerDTO.getSharedAccountProductId());
		List<SharedAccountConsumerDTO> consumerList = new ArrayList<SharedAccountConsumerDTO>();
		SharedAccountConsumerDTO consumerDTO = null;
		if (sharedAccountConsumerDTO.getStatus() != null && !sharedAccountConsumerDTO.getStatus().isEmpty()) {
			final ConsumerStatusEnum consumerStatus = ConsumerStatusEnum.valueOf(sharedAccountConsumerDTO.getStatus());
			if (ConsumerStatusEnum.PENDING.equals(consumerStatus) || ConsumerStatusEnum.REJECTED.equals(consumerStatus)) {
				searchDTO.setRequestType(ConsumerRequestType.ADD_CONSUMER);
				searchDTO.setStatusList(Arrays.asList(consumerStatus.getStatusCode()));
				final List<SharedAccountRequestDTO> requestDTOList = sharedAccountReqDAO
						.getFilteredSharedAccountConsumerRequest(searchDTO);
				for (final SharedAccountRequestDTO requestDTO : requestDTOList) {
					consumerDTO = this.populateConsumerDTOFromRequestDTO(requestDTO);
					consumerList.add(consumerDTO);
				}
			} else {
				consumerList = sharedAccountConsumerDAO.getFilteredConsumerList(searchDTO);
			}
		} else {
			consumerList = sharedAccountConsumerDAO.getFilteredConsumerList(searchDTO);
			searchDTO.setRequestType(ConsumerRequestType.ADD_CONSUMER);
			searchDTO.setStatusList(Arrays.asList(ConsumerStatusEnum.PENDING.getStatusCode(),
					ConsumerStatusEnum.REJECTED.getStatusCode()));
			final List<SharedAccountRequestDTO> requestDTOList = sharedAccountReqDAO
					.getFilteredSharedAccountConsumerRequest(searchDTO);
			for (final SharedAccountRequestDTO requestDTO : requestDTOList) {
				consumerDTO = this.populateConsumerDTOFromRequestDTO(requestDTO);
				consumerList.add(consumerDTO);
			}
		}

		return consumerList;
	}

	/**
	 * This method populate consumerDTO from requestDTO.
	 * 
	 * @param requestDTO
	 *            requestDTO
	 * @return consumerDTO
	 */
	private SharedAccountConsumerDTO populateConsumerDTOFromRequestDTO(final SharedAccountRequestDTO requestDTO) {
		SharedAccountConsumerDTO consumerDTO;
		consumerDTO = new SharedAccountConsumerDTO();
		consumerDTO.setConsumerMsisdn(requestDTO.getReciverMsisdn().toString());
		consumerDTO.setStatus(requestDTO.getConsumerStatus().toString());
		consumerDTO.setIsProvider(Boolean.FALSE);
		consumerDTO.setConsumerName(requestDTO.getConsumerAddInfo());
		return consumerDTO;
	}

	@Override
	public ResponseDTO buyProduct(final Long productId, final Long providerMsisdn, final Object boughtBy,
			final boolean isConfirmed) throws FDPServiceException {
		ResponseDTO responseDTO = null;
		try {
			final String value = ServiceProvDataConverterUtil.getKeyForServiceProvMetaBag(productId,
					FDPServiceProvType.PRODUCT, FDPServiceProvSubType.PRODUCT_BUY);
			final FDPRequestImpl fdpRequest = createFDPRequest(providerMsisdn);
			final String spId = ServiceProvDataConverterUtil.getKeyForServiceProvMetaBag(productId,
					FDPServiceProvType.PRODUCT, FDPServiceProvSubType.PRODUCT_BUY);
			Boolean executeSP = Boolean.TRUE;
			if (!isConfirmed) {
				ProductUtil.updateForProductBuyPolicy(fdpRequest, productId.toString(), spId, ChargingType.NORMAL);
				final Policy productBuyPolicy = (Policy) ApplicationConfigUtil.getMetaDataCache().getValue(
						new FDPMetaBag(new FDPCircle(-1L, "ALL", "ALL"), ModuleType.POLICY,
								FDPConstant.POLICY_FOR_PRODUCT_BUY));
				;
				if (productBuyPolicy != null) {
					LOGGER.debug("POLICY_FOR_PRODUCT_BUY Found. Executing policy");
					final FDPResponse fdpResponse = productBuyPolicy.executePolicy(fdpRequest);
					if (fdpResponse != null) {
						String msg = null;
						if (fdpResponse.getResponseString() != null && !fdpResponse.getResponseString().isEmpty()) {
							msg = fdpResponse.getResponseString().get(0).getCurrDisplayText(DisplayArea.COMPLETE);
						}
						LOGGER.debug("Message in Buy Product Policy Response is {} and isTerminateSession = {}", msg,
								fdpResponse.isTerminateSession());
						if (!fdpResponse.isTerminateSession() && msg != null) {
							responseDTO = new ResponseDTO(ResponseType.CONFIRMATION, msg);
							executeSP = Boolean.FALSE;
						} else {
							LOGGER.debug("POLICY response is NULL.");
						}
					}
				} else {
					LOGGER.debug("POLICY_FOR_PRODUCT_BUY not Found.");
				}
			}
			if (executeSP) {
				final ServiceProvisioningRule serviceProvisioningRule = getServiceProvisioningKey(productId,
						FDPServiceProvSubType.PRODUCT_BUY, fdpRequest);
				if (serviceProvisioningRule != null) {
					// create fdpRequest Object
					fdpRequest.putAuxiliaryRequestParameter(AuxRequestParam.BOUGHT_BY, boughtBy);
					fdpRequest.putAuxiliaryRequestParameter(AuxRequestParam.CONSUMER_MSISDN, providerMsisdn.toString());
					RequestUtil.updateProductAndSPInWebRequest(fdpRequest, productId.toString(), value);
					final FDPResponse response = serviceProvisioningRule.execute(fdpRequest);
					if (Status.FAILURE.equals(response.getExecutionStatus())) {
						throw new FDPServiceException(response.getResponseString().get(0)
								.getCurrDisplayText(DisplayArea.COMPLETE));
					}
					final Object errorMsg = fdpRequest.getValueFromStep(StepNameEnum.VALIDATION_STEP.getValue(),
							SharedAccountResponseType.ERROR_MESSAGE.name());
					if (errorMsg != null) {
						throw new FDPServiceException(errorMsg.toString());
					}
				} else {
					throw new FDPServiceException("Service Provisioning Flow not configured for product");
				}
			}

		} catch (final RuleException e) {
			throw new FDPServiceException("Buy product failed.", e);
		} catch (final ExecutionFailedException e) {
			throw new FDPServiceException("Buy product failed.", e);
		} catch (final EvaluationFailedException e) {
			throw new FDPServiceException("Buy product failed.", e);
		}
		return responseDTO;
	}

	@Override
	public Boolean upgradeRequest(final Long newLimit, final Long provMSISDN, final Long consumerMSISDN,
			final Long productId) throws ExecutionFailedException, EvaluationFailedException, RuleException,
			FDPServiceException {
		final FDPRequestImpl fdpRequest = createFDPRequest(provMSISDN);
		final ServiceProvisioningRule serviceProvisioningRule = getServiceProvisioningKey(productId,
				FDPServiceProvSubType.SHARED_ACCOUNT_CONSUMER_UT_UPGRADE, fdpRequest);
		fdpRequest.putAuxiliaryRequestParameter(AuxRequestParam.CONSUMER_MSISDN, consumerMSISDN);
		fdpRequest.putAuxiliaryRequestParameter(AuxRequestParam.PROVIDER_MSISDN, provMSISDN);
		fdpRequest.putAuxiliaryRequestParameter(AuxRequestParam.CONSUMER_LIMIT_UPGRADE_VALUE, newLimit);
		fdpRequest.putAuxiliaryRequestParameter(AuxRequestParam.CURRENT_SHARED_ACCOUNT_VALUE,
				Arrays.asList(consumerMSISDN.toString()));
		final FDPResponse response = serviceProvisioningRule.execute(fdpRequest);
		if (Status.FAILURE.equals(response.getExecutionStatus())) {
			throw new FDPServiceException(response.getResponseString().get(0).getCurrDisplayText(DisplayArea.COMPLETE));
		}
		return true;
	}

	@Override
	public void rejectAddConsumer(final Long userMsisdn, final Long requestId, final Long productId,
			final Long providerMsisdn) throws FDPServiceException {
		try {
			final String value = ServiceProvDataConverterUtil.getKeyForServiceProvMetaBag(productId,
					FDPServiceProvType.PRODUCT, FDPServiceProvSubType.SHARED_ACCOUNT_REJECT_CONSUMER);
			final FDPRequestImpl fdpRequest = createFDPRequest(userMsisdn);
			final ServiceProvisioningRule serviceProvisioningRule = getServiceProvisioningKey(productId,
					FDPServiceProvSubType.SHARED_ACCOUNT_REJECT_CONSUMER, fdpRequest);
			fdpRequest.putAuxiliaryRequestParameter(AuxRequestParam.CONSUMER_MSISDN, userMsisdn.toString());
			fdpRequest.putAuxiliaryRequestParameter(AuxRequestParam.CURRENT_SHARED_ACCOUNT_VALUE,
					Arrays.asList(requestId.toString()));
			RequestUtil.updateProductAndSPInWebRequest(fdpRequest, productId.toString(), value);
			final FDPResponse response = serviceProvisioningRule.execute(fdpRequest);
			if (Status.FAILURE.equals(response.getExecutionStatus())) {
				throw new FDPServiceException(response.getResponseString().get(0)
						.getCurrDisplayText(DisplayArea.COMPLETE));
			}
		} catch (final RuleException e) {
			throw new FDPServiceException("Detach provider failed.", e);
		} catch (final ExecutionFailedException e) {
			throw new FDPServiceException("Detach provider failed.", e);
		} catch (final EvaluationFailedException e) {
			throw new FDPServiceException("Delete account failed.", e);
		}
	}

	@Override
	public void approveAddConsumer(final Long userMsisdn, final Long requestId, final Long productId,
			final Long providerMsisdn) throws FDPServiceException {
		try {
			final String offerId = getOfferId(userMsisdn, productId.toString());
			final String value = ServiceProvDataConverterUtil.getKeyForServiceProvMetaBag(productId,
					FDPServiceProvType.PRODUCT, FDPServiceProvSubType.SHARED_ACCOUNT_ACCEPT_CONSUMER);
			final FDPRequestImpl fdpRequest = createFDPRequest(userMsisdn);
			final ServiceProvisioningRule serviceProvisioningRule = getServiceProvisioningKey(productId,
					FDPServiceProvSubType.SHARED_ACCOUNT_ACCEPT_CONSUMER, fdpRequest);
			fdpRequest.putAuxiliaryRequestParameter(AuxRequestParam.CONSUMER_MSISDN, userMsisdn.toString());
			fdpRequest.putAuxiliaryRequestParameter(AuxRequestParam.OFFER_ID, offerId);
			fdpRequest.putAuxiliaryRequestParameter(AuxRequestParam.CURRENT_SHARED_ACCOUNT_VALUE,
					Arrays.asList(requestId.toString()));

			RequestUtil.updateProductAndSPInWebRequest(fdpRequest, productId.toString(), value);
			final FDPResponse response = serviceProvisioningRule.execute(fdpRequest);
			if (Status.FAILURE.equals(response.getExecutionStatus())) {
				throw new FDPServiceException(response.getResponseString().get(0)
						.getCurrDisplayText(DisplayArea.COMPLETE));
			}
		} catch (final RuleException e) {
			throw new FDPServiceException("Detach provider failed.", e);
		} catch (final ExecutionFailedException e) {
			throw new FDPServiceException("Detach provider failed.", e);
		} catch (final EvaluationFailedException e) {
			throw new FDPServiceException("Delete account failed.", e);
		}
	}

	/**
	 * Gets the offer id.
	 * 
	 * @param consumerMSISDN
	 *            the consumer msisdn
	 * @param productId
	 *            the product id
	 * @return the offer id
	 * @throws FDPServiceException
	 *             the fDP service exception
	 */
	private String getOfferId(final Long consumerMSISDN, final String productId) throws FDPServiceException {
		final FDPCircle circle = CircleCodeFinder.getFDPCircleByMsisdn(consumerMSISDN.toString(),
				applicationConfigCache);
		if (circle == null) {
			throw new FDPServiceException("Circle Not Found for msisdn " + consumerMSISDN);
		}
		Product product;
		try {
			product = SharedAccountUtil.getProduct(circle, productId);
		} catch (final ExecutionFailedException e) {
			throw new FDPServiceException(e);
		}
		return product.getAdditionalInfo(ProductAdditionalInfoEnum.SHARED_ACC_OFFER_ID_MAPPING);
	}

	@Override
	public void acceptConsumerLimitUpgrade(final ConsumerApprovalDTO approvalDTO, final Long userMsisdn)
			throws FDPServiceException {
		FDPRequestImpl fdpRequest;
		try {
			fdpRequest = createFDPRequest(userMsisdn);
			final ServiceProvisioningRule serviceProvisioningRule = getServiceProvisioningKey(
					approvalDTO.getProductId(), FDPServiceProvSubType.SHARED_ACCOUNT_PROVIDER_UT_UPGRADE, fdpRequest);
			//System.out.println(approvalDTO);
			fdpRequest.putAuxiliaryRequestParameter(AuxRequestParam.CONSUMER_MSISDN,
					approvalDTO.getSharedAccountConsumerId());
			fdpRequest.putAuxiliaryRequestParameter(AuxRequestParam.PROVIDER_MSISDN, userMsisdn);
			final AbstractNode node = new WebNode();
			node.addAdditionalInfo(EntityType.NEW_LIMIT.getEntityType(), approvalDTO.getConsumerNewLimit());
			node.addAdditionalInfo(EntityType.NO_OF_DAYS.getEntityType(), 0);
			node.addAdditionalInfo(EntityType.UT_UPGRADE_TYPE.getEntityType(), UtUpgradeType.Permanent);

			fdpRequest.putAuxiliaryRequestParameter(AuxRequestParam.CURRENT_SHARED_ACCOUNT_VALUE, node);
			final FDPResponse response = serviceProvisioningRule.execute(fdpRequest);
			if (Status.FAILURE.equals(response.getExecutionStatus())) {
				throw new FDPServiceException(response.getResponseString().get(0)
						.getCurrDisplayText(DisplayArea.COMPLETE));
			}
			approvalDTO.setRequestStatus(ConsumerStatusEnum.COMPLETED);
			//System.out.println("Updating approval dto " + approvalDTO);
			updateSharedAccountApprovalRequest(approvalDTO, userMsisdn);
		} catch (final ExecutionFailedException e) {
			throw new FDPServiceException(e);
		} catch (final RuleException e) {
			throw new FDPServiceException(e);
		} catch (final EvaluationFailedException e) {
			throw new FDPServiceException(e);
		}
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
