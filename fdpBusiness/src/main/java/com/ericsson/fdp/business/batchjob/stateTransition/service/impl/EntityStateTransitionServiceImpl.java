/**
 *
 */
package com.ericsson.fdp.business.batchjob.stateTransition.service.impl;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.ejb.Stateless;
import javax.inject.Inject;
import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;

import ch.qos.logback.classic.Logger;

import com.ericsson.fdp.business.batchjob.stateTransition.service.EntityStateTransitionService;
import com.ericsson.fdp.business.cache.MetaDataService;
import com.ericsson.fdp.business.cache.datageneration.service.DMDataService;
import com.ericsson.fdp.business.cache.datageneration.service.ProductDataService;
import com.ericsson.fdp.business.cache.datageneration.service.SPDataService;
import com.ericsson.fdp.common.enums.ErrorCodesEnum;
import com.ericsson.fdp.common.enums.ModuleType;
import com.ericsson.fdp.common.exception.FDPServiceException;
import com.ericsson.fdp.common.vo.FDPCircle;
import com.ericsson.fdp.common.vo.FDPUserDetail;
import com.ericsson.fdp.core.logging.FDPLogger;
import com.ericsson.fdp.core.logging.FDPLoggerFactory;
import com.ericsson.fdp.dao.batch.FDPBatchExecutionEntityInfoDAO;
import com.ericsson.fdp.dao.batch.FDPBatchExecutionInfoDAO;
import com.ericsson.fdp.dao.dto.UpdateCacheDTO;
import com.ericsson.fdp.dao.dto.batchJob.BatchExecutionRequestDTO;
import com.ericsson.fdp.dao.dto.batchJob.BatchExecutionResponseDTO;
import com.ericsson.fdp.dao.dto.batchJob.FDPBatchExecutionEntityInfoDTO;
import com.ericsson.fdp.dao.dto.dynamicMenu.BaseNodeDTO;
import com.ericsson.fdp.dao.dto.product.ProductDTO;
import com.ericsson.fdp.dao.dto.product.ProductGeneralInfoDTO;
import com.ericsson.fdp.dao.dto.serviceprov.ServiceProvProductDTO;
import com.ericsson.fdp.dao.enums.ActionTypeEnum;
import com.ericsson.fdp.dao.enums.DynamicMenuEntityType;
import com.ericsson.fdp.dao.enums.EntityType;
import com.ericsson.fdp.dao.enums.StatusCodeEnum;
import com.ericsson.fdp.dao.exception.FDPConcurrencyException;
import com.ericsson.fdp.dao.fdpadmin.FDPDynamicMenuCodeDAO;
import com.ericsson.fdp.dao.fdpadmin.FDPProductDAO;
import com.ericsson.fdp.dao.fdpadmin.FDPServiceProvDAO;
import com.ericsson.fdp.dao.fdpadmin.FDPSharedAccountReqDAO;

/**
 * This Class is used to activate/deactivate entity by batch job.
 *
 * @author Ericsson
 */
@Stateless
public class EntityStateTransitionServiceImpl implements EntityStateTransitionService {

	/** The fdp product dao. */
	@Inject
	private FDPProductDAO fdpProductDAO;

	/** The fdp batch execution info dao. */
	@Inject
	private FDPBatchExecutionInfoDAO fdpBatchExecutionInfoDAO;

	/** The fdp batch execution entity info dao. */
	@Inject
	private FDPBatchExecutionEntityInfoDAO fdpBatchExecutionEntityInfoDAO;

	/** The dynamic menu code dao. */
	@Inject
	private FDPDynamicMenuCodeDAO fdpDynamicMenuCodeDAO;

	/** The fdp service prov dao. */
	@Inject
	private FDPServiceProvDAO fdpServiceProvDAO;

	/** The shared account request dao. */
	@Inject
	private FDPSharedAccountReqDAO sharedAccountRequestDAO;

	@Override
	public BatchExecutionResponseDTO updateProductsStatus(final BatchExecutionRequestDTO executionRequestDTO) {
		List<UpdateCacheDTO> updateCacheDTOList = null;
		Logger circleLogger = FDPLoggerFactory.getCircleAdminLogger(executionRequestDTO.getCircle().getCircleName());
		List<ProductDTO> productDTOList = new ArrayList<ProductDTO>();
		List<Long> entityInfoIdList = null;
		// fetch list of product ids
		if (StatusCodeEnum.LAUNCH_FOR_MARKET.equals(executionRequestDTO.getInitialStatus())
				&& StatusCodeEnum.ACTIVATING.equals(executionRequestDTO.getFinalStatus())) {
			productDTOList = fdpProductDAO.getAllCircleProductDTOForLaunching(executionRequestDTO.getCircle()
					.getCircleId());
		} else if (StatusCodeEnum.ACTIVATING.equals(executionRequestDTO.getInitialStatus())
				&& StatusCodeEnum.ACTIVE.equals(executionRequestDTO.getFinalStatus())) {
			productDTOList = fdpProductDAO.getAllProductDTOByProductStatus(executionRequestDTO.getCircle()
					.getCircleId(), ((StatusCodeEnum) executionRequestDTO.getInitialStatus()).getStatus());
			entityInfoIdList = fdpBatchExecutionEntityInfoDAO
					.getExecutionEntityInfoListByBatchExecutionInfoId(executionRequestDTO.getBatchExecutionInfoId());
		} else if (StatusCodeEnum.EXPIRED.equals(executionRequestDTO.getFinalStatus())) {
			productDTOList = fdpProductDAO.getAllCircleProductDTOForExpiry(executionRequestDTO.getCircle()
					.getCircleId());
		}
		FDPLogger.info(circleLogger, this.getClass(), "updateProductsStatus", "Batch Job running, product fetched are "
				+ productDTOList);
		FDPBatchExecutionEntityInfoDTO entityInfoDTO = null;
		Integer failureCount = 0;
		Integer successCount = 0;
		Integer index = 0;
		UpdateCacheDTO updateCacheDTO = null;
		ProductGeneralInfoDTO productInfoDTO = null;
		for (ProductDTO productDTO : productDTOList) {
			try {
				productInfoDTO = (ProductGeneralInfoDTO) productDTO.getProductInfoDTO();
				entityInfoDTO = new FDPBatchExecutionEntityInfoDTO();
				entityInfoDTO.setEntityId(productDTO.getProductId());
				entityInfoDTO.setEntityType(EntityType.PRODUCT);
				entityInfoDTO.setBatchExecutionInfoId(executionRequestDTO.getBatchExecutionInfoId());
				entityInfoDTO.setInitialStatus(StatusCodeEnum.getStatusEnum(productInfoDTO.getProductStatus()));

				// update product status
				fdpProductDAO.updateProductStatus(productDTO.getProductId(),
						((StatusCodeEnum) executionRequestDTO.getFinalStatus()).getStatus(), null,
						executionRequestDTO.getModifiedBy());

				// update entity info final status if there is no error
				entityInfoDTO.setFinalStatus(executionRequestDTO.getFinalStatus());
				if (!StatusCodeEnum.ACTIVE.equals(executionRequestDTO.getFinalStatus())) {
					entityInfoDTO.setCreatedBy(executionRequestDTO.getModifiedBy());
					fdpBatchExecutionEntityInfoDAO.saveExecutionEntityInfo(entityInfoDTO);
				} else {
					entityInfoDTO.setBatchExecutionEntityInfoId(entityInfoIdList.get(index));
					entityInfoDTO.setModifiedBy(executionRequestDTO.getModifiedBy());
					fdpBatchExecutionEntityInfoDAO.updateExecutionEntityInfo(entityInfoDTO);
				}
				successCount++;
				if (StatusCodeEnum.EXPIRED.equals(executionRequestDTO.getFinalStatus())) {
					FDPUserDetail user = new FDPUserDetail(executionRequestDTO.getModifiedBy(), null,
							executionRequestDTO.getCircle(), null);
					updateCacheDTOList = this.updateSPAndDynamicMenuStatusToInactive(productDTO.getProductId(), user);
					for (UpdateCacheDTO cacheDTO : updateCacheDTOList) {
						this.updateCache(cacheDTO, executionRequestDTO.getCircle().getCircleName());
					}

				} else if (StatusCodeEnum.ACTIVE.equals(executionRequestDTO.getFinalStatus())) {
					List<UpdateCacheDTO> updateCacheDtoList = updateDmCodeStatus(productDTO.getProductId(),executionRequestDTO.getCircle(),StatusCodeEnum.OFFLINE);
					for (UpdateCacheDTO cacheDTO : updateCacheDtoList) {
						this.updateCache(cacheDTO, executionRequestDTO.getCircle().getCircleName());
					}
					updateCacheDTO = new UpdateCacheDTO(productDTO.getProductId(), ActionTypeEnum.ADD_UPDATE,
							ModuleType.PRODUCT, executionRequestDTO.getCircle());
					this.updateCache(updateCacheDTO, executionRequestDTO.getCircle().getCircleName());
				}
				FDPLogger.info(
						circleLogger,
						this.getClass(),
						"updateProductsStatus",
						"Product status updated successfully by batch job, batch job info id = "
								+ executionRequestDTO.getBatchExecutionInfoId() + " , product id = "
								+ productDTO.getProductId());
			} catch (FDPConcurrencyException e) {
				FDPLogger.error(circleLogger, this.getClass(), "updateProductsStatus",
						"Failed to update product status, productId = " + productDTO.getProductId(), e);
				failureCount++;
				try {
					fdpProductDAO.updateProductStatus(productDTO.getProductId(), StatusCodeEnum.FAILED.getStatus(),
							null, executionRequestDTO.getModifiedBy());
					entityInfoDTO.setFinalStatus(StatusCodeEnum.FAILED);
					if (!StatusCodeEnum.ACTIVE.equals(executionRequestDTO.getFinalStatus())) {
						entityInfoDTO.setCreatedBy(executionRequestDTO.getModifiedBy());
						fdpBatchExecutionEntityInfoDAO.saveExecutionEntityInfo(entityInfoDTO);
					} else {
						entityInfoDTO.setBatchExecutionEntityInfoId(entityInfoIdList.get(index));
						entityInfoDTO.setModifiedBy(executionRequestDTO.getModifiedBy());
						fdpBatchExecutionEntityInfoDAO.updateExecutionEntityInfo(entityInfoDTO);
					}

				} catch (FDPConcurrencyException e1) {
					FDPLogger.error(circleLogger, this.getClass(), "updateProductsStatus",
							"Failed to update product status, productId = " + productDTO.getProductId(), e);
				}
			} catch (FDPServiceException e) {
				failureCount++;
				FDPLogger.error(circleLogger, this.getClass(), "updateProductsStatus",
						"Failed to update product status, productId = " + productDTO.getProductId(), e);
			}
			index++;
		}

		BatchExecutionResponseDTO executionResponse = new BatchExecutionResponseDTO();
		executionResponse.setFailureCount(failureCount);
		executionResponse.setSuccessCount(successCount);
		executionResponse.setTotalBatchCount(productDTOList.size());
		return executionResponse;
	}

	/**
	 * Update cache.
	 *
	 * @param updateCacheDTO
	 *            the update cache dto
	 * @param circleName
	 *            the circle name
	 * @throws FDPServiceException
	 *             the fDP service exception
	 */
	private void updateCache(final UpdateCacheDTO updateCacheDTO, final String circleName) throws FDPServiceException {

		Logger circleLogger = FDPLoggerFactory.getCircleAdminLogger(circleName);
		try {
			final Context initialContextPAMCleanUpService = new InitialContext();

			MetaDataService metaDataService = null;
			if (ModuleType.PRODUCT.equals(updateCacheDTO.getModuleType())) {
				metaDataService = (ProductDataService) initialContextPAMCleanUpService
						.lookup("java:global/fdpBusiness-ear/fdpBusiness-1.0/ProductDataServiceImpl");
			} else if (ModuleType.DM.equals(updateCacheDTO.getModuleType())) {
				metaDataService = (DMDataService) initialContextPAMCleanUpService
						.lookup("java:global/fdpBusiness-ear/fdpBusiness-1.0/DMDataServiceImpl");
			} else if (ModuleType.SP_PRODUCT.equals(updateCacheDTO.getModuleType())) {
				metaDataService = (SPDataService) initialContextPAMCleanUpService
						.lookup("java:global/fdpBusiness-ear/fdpBusiness-1.0/SPDataServiceImpl");
			}

			metaDataService.updateMetaCache(updateCacheDTO);
		} catch (NamingException e) {
			FDPLogger.error(circleLogger, this.getClass(), "updateCache",
					"NamingException error occured while updating cache", e);
			throw new FDPServiceException(e);
		}
	}

	/**
	 * This method set dynamic menu and SP Status to inactive which are linked
	 * to product.
	 *
	 * @param productId
	 *            product id
	 * @param user
	 *            user details
	 * @return UpdateCacheDTO List to be updated in cache
	 * @throws FDPServiceException
	 *             if any error occurs
	 */
	private List<UpdateCacheDTO> updateSPAndDynamicMenuStatusToInactive(final Long productId, final FDPUserDetail user)
			throws FDPServiceException {
		Logger circleLogger = FDPLoggerFactory.getCircleAdminLogger(user.getLoggedInCircle().getCircleName());
		List<BaseNodeDTO> baseNodeDTOList = fdpDynamicMenuCodeDAO.getDynamicMenuCodesDTOByEntityId(
				DynamicMenuEntityType.PRODUCT, productId, Arrays.asList(StatusCodeEnum.ACTIVE_FOR_TEST, StatusCodeEnum.ACTIVE), circleLogger);
		List<UpdateCacheDTO> updateCacheDTOList = new ArrayList<UpdateCacheDTO>();
		try {
			for (BaseNodeDTO nodeDTO : baseNodeDTOList) {
				nodeDTO.setStatus(StatusCodeEnum.INACTIVE.getStatus());
				nodeDTO.setCircle(user.getLoggedInCircle());
				fdpDynamicMenuCodeDAO.updateStatus(nodeDTO, circleLogger);
				updateCacheDTOList.add(new UpdateCacheDTO(fdpDynamicMenuCodeDAO.getFirstAncestorDynamicMenuIdForNodeId(nodeDTO.getId()), ActionTypeEnum.DELETE, ModuleType.DM, user
						.getLoggedInCircle()));
			}
			List<ServiceProvProductDTO> servProvList = fdpServiceProvDAO.updateSPStatusByProductId(productId,
					user.getUserName(), StatusCodeEnum.INACTIVE);
			for (ServiceProvProductDTO servProv : servProvList) {
				UpdateCacheDTO updateCacheDTO = new UpdateCacheDTO(productId, ActionTypeEnum.DELETE,
						ModuleType.SP_PRODUCT, user.getLoggedInCircle());
				updateCacheDTO.setSpSubType(servProv.getSpSubType());
				updateCacheDTOList.add(updateCacheDTO);
			}
			updateCacheDTOList.add(new UpdateCacheDTO(productId, ActionTypeEnum.DELETE, ModuleType.PRODUCT, user
					.getLoggedInCircle()));
		} catch (FDPConcurrencyException e) {
			FDPLogger.error(circleLogger, this.getClass(), "updateSPAndDynamicMenuStatusToInactive",
					"Concurrency error occured in updatiing dynamic menu and sp status to inactive ", e);
			throw new FDPServiceException(e);
		}
		return updateCacheDTOList;
	}

	private List<UpdateCacheDTO> updateDmCodeStatus(final Long productId,final FDPCircle circleName,final StatusCodeEnum status) throws FDPServiceException{
		Logger circleLogger = FDPLoggerFactory.getCircleAdminLogger(circleName.getCircleName());
		List<BaseNodeDTO> baseNodeDTOList = fdpDynamicMenuCodeDAO.getDynamicMenuCodesDTOByEntityId(
				DynamicMenuEntityType.PRODUCT, productId, Arrays.asList(status), circleLogger);
		List<UpdateCacheDTO> updateCacheDTOList = new ArrayList<UpdateCacheDTO>();
		try {
			for (BaseNodeDTO nodeDTO : baseNodeDTOList) {
				nodeDTO.setStatus(StatusCodeEnum.ACTIVE.getStatus());
				nodeDTO.setCircle(circleName);
				fdpDynamicMenuCodeDAO.updateStatus(nodeDTO, circleLogger);
				updateCacheDTOList.add(this.prepareCacheDTO(fdpDynamicMenuCodeDAO.getFirstAncestorDynamicMenuIdForNodeId(nodeDTO.getId()), ActionTypeEnum.ADD_UPDATE, ModuleType.DM,
						circleName));
			}

		}catch(FDPConcurrencyException e){

			FDPLogger.error(circleLogger, this.getClass(), "updateDynamicMenuStatusOffLine", e.getMessage(), e);
			throw new FDPServiceException(ErrorCodesEnum.ERROR_CONCURRENCY, e.getMessage());
		}

		return updateCacheDTOList;
	}

	private UpdateCacheDTO prepareCacheDTO(final Long id, final ActionTypeEnum action, final ModuleType moduleType,
			final FDPCircle circle) {
		UpdateCacheDTO cacheDTO = new UpdateCacheDTO();
		cacheDTO.setAction(action);
		cacheDTO.setModuleType(moduleType);
		cacheDTO.setId(id);
		cacheDTO.setCircle(circle);
		return cacheDTO;
	}

	@Override
	public BatchExecutionResponseDTO updateMenuStatus(BatchExecutionRequestDTO executionRequestDTO) {
		Logger circleLogger = FDPLoggerFactory.getCircleAdminLogger(executionRequestDTO.getCircle().getCircleName());
		//System.out.println("created by: circle: "+executionRequestDTO.getCircle().getCircleName()+"***"+executionRequestDTO.getCreatedBy());
		List<BaseNodeDTO> menuDTOList = new ArrayList<BaseNodeDTO>();
		List<Long> entityInfoIdList = null;
		BatchExecutionResponseDTO executionResponse = null;
		// fetch list of product ids

		try{
			if (StatusCodeEnum.OFFLINE.equals(executionRequestDTO.getInitialStatus())
					&& StatusCodeEnum.ACTIVATING.equals(executionRequestDTO.getFinalStatus())) {
				menuDTOList = fdpDynamicMenuCodeDAO.updateAllCircleMenuNodeDTOForLaunching(executionRequestDTO.getCircle().getCircleId(),
						((StatusCodeEnum) executionRequestDTO.getFinalStatus()).getStatus(),circleLogger);
			} else if (StatusCodeEnum.ACTIVATING.equals(executionRequestDTO.getInitialStatus())
					&& StatusCodeEnum.ACTIVE.equals(executionRequestDTO.getFinalStatus())) {
				menuDTOList = fdpDynamicMenuCodeDAO.updateAllCircleMenuNodeDTOForLaunching(executionRequestDTO.getCircle().getCircleId(),
						((StatusCodeEnum) executionRequestDTO.getFinalStatus()).getStatus(),circleLogger);
				entityInfoIdList = fdpBatchExecutionEntityInfoDAO
						.getExecutionEntityInfoListByBatchExecutionInfoId(executionRequestDTO.getBatchExecutionInfoId());
			} 
			FDPLogger.info(circleLogger, this.getClass(), "updateMenuStatus", "Batch Job running, menu updated are "
					+ menuDTOList);
			FDPBatchExecutionEntityInfoDTO entityInfoDTO = null;
			Integer failureCount = 0;
			Integer successCount = 0;
			Integer index = 0;
			UpdateCacheDTO updateCacheDTO = null;
			for (BaseNodeDTO baseNodeDTO : menuDTOList) {
				try{
					entityInfoDTO = new FDPBatchExecutionEntityInfoDTO();
					entityInfoDTO.setEntityId(baseNodeDTO.getId());
					entityInfoDTO.setEntityType(EntityType.PRODUCT);
					entityInfoDTO.setBatchExecutionInfoId(executionRequestDTO.getBatchExecutionInfoId());
					entityInfoDTO.setInitialStatus(StatusCodeEnum.getStatusEnum(baseNodeDTO.getStatus()));
					// update entity info final status if there is no error
					entityInfoDTO.setFinalStatus(executionRequestDTO.getFinalStatus());
					if (!StatusCodeEnum.ACTIVE.equals(executionRequestDTO.getFinalStatus())) {
						entityInfoDTO.setCreatedBy(executionRequestDTO.getModifiedBy());
						fdpBatchExecutionEntityInfoDAO.saveExecutionEntityInfo(entityInfoDTO);
					} else {
						entityInfoDTO.setBatchExecutionEntityInfoId(entityInfoIdList.get(index));
						entityInfoDTO.setModifiedBy(executionRequestDTO.getModifiedBy());
						fdpBatchExecutionEntityInfoDAO.updateExecutionEntityInfo(entityInfoDTO);
					}
					successCount++;
					if (StatusCodeEnum.ACTIVE.equals(executionRequestDTO.getFinalStatus())) {
						updateCacheDTO = new UpdateCacheDTO(baseNodeDTO.getId(), ActionTypeEnum.ADD_UPDATE, ModuleType.DM,
								executionRequestDTO.getCircle());
						this.updateCache(updateCacheDTO, executionRequestDTO.getCircle().getCircleName());
					}
					FDPLogger.info(
							circleLogger,
							this.getClass(),
							"updateMenuStatus",
							"Menu status updated successfully by batch job, batch job info id = "
									+ executionRequestDTO.getBatchExecutionInfoId() + " , menu id = "
									+ baseNodeDTO.getId());

				}catch (FDPConcurrencyException e) {
					FDPLogger.error(circleLogger, this.getClass(), "updateMenuStatus",
							"Failed to update menu status, menuId = " + baseNodeDTO.getId(), e);
					failureCount++;
					try {
						fdpProductDAO.updateProductStatus(baseNodeDTO.getId(), StatusCodeEnum.FAILED.getStatus(),
								null, executionRequestDTO.getModifiedBy());
						entityInfoDTO.setFinalStatus(StatusCodeEnum.FAILED);
						if (!StatusCodeEnum.ACTIVE.equals(executionRequestDTO.getFinalStatus())) {
							entityInfoDTO.setCreatedBy(executionRequestDTO.getModifiedBy());
							fdpBatchExecutionEntityInfoDAO.saveExecutionEntityInfo(entityInfoDTO);
						} else {
							entityInfoDTO.setBatchExecutionEntityInfoId(entityInfoIdList.get(index));
							entityInfoDTO.setModifiedBy(executionRequestDTO.getModifiedBy());
							fdpBatchExecutionEntityInfoDAO.updateExecutionEntityInfo(entityInfoDTO);
						}

					} catch (FDPConcurrencyException e1) {
						FDPLogger.error(circleLogger, this.getClass(), "updateMenuStatus",
								"Failed to update menu status, menuId = " + baseNodeDTO.getId(), e);
					}
				}catch (FDPServiceException e) {
					failureCount++;
					FDPLogger.error(circleLogger, this.getClass(), "updateMenuStatus",
							"Failed to update menu status, menuId = " + baseNodeDTO.getId(), e);
				}
				index++;
			}
			executionResponse = new BatchExecutionResponseDTO();
			executionResponse.setFailureCount(failureCount);
			executionResponse.setSuccessCount(successCount);
			executionResponse.setTotalBatchCount(menuDTOList.size());
		}catch (FDPConcurrencyException e1) {
			FDPLogger.error(circleLogger, this.getClass(), "updateMenuStatus",
					"Failed to update menu status, circle = " + executionRequestDTO.getCircle(), e1);
		}
		return executionResponse;
	}
}
