package com.ericsson.fdp.business.cache.datageneration.service.impl;

import java.util.ArrayList;
import java.util.List;

import javax.annotation.Resource;
import javax.ejb.Stateless;
import javax.inject.Inject;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ericsson.fdp.FDPCacheable;
import com.ericsson.fdp.business.cache.datageneration.service.TariffEnquiryAttributesExpressionService;
import com.ericsson.fdp.business.exception.ExpressionFailedException;
import com.ericsson.fdp.business.expression.Expression;
import com.ericsson.fdp.business.tariffenquiry.configimport.constants.TariffConstants;
import com.ericsson.fdp.business.util.ExpressionUtil;
import com.ericsson.fdp.business.vo.FDPTariffAttributesExpression;
import com.ericsson.fdp.common.enums.ModuleType;
import com.ericsson.fdp.common.enums.TariffEnquiryOption;
import com.ericsson.fdp.common.exception.FDPServiceException;
import com.ericsson.fdp.common.vo.FDPCircle;
import com.ericsson.fdp.core.dsm.framework.service.FDPCache;
import com.ericsson.fdp.core.exception.ExecutionFailedException;
import com.ericsson.fdp.core.serviceprovisioning.bean.FDPMetaBag;
import com.ericsson.fdp.dao.dto.UpdateCacheDTO;
import com.ericsson.fdp.dao.dto.common.constraint.ConstraintStepDTO;
import com.ericsson.fdp.dao.dto.common.constraint.SubStepConstraintDTO;
import com.ericsson.fdp.dao.dto.tariffenquiry.FDPTarrifConstraintStepDTO;
import com.ericsson.fdp.dao.dto.tariffenquiry.FDPTarrifConstraintSubStepDTO;
import com.ericsson.fdp.dao.tariffenquiry.FDPTariffEnquiryAttributesConstraintsDAO;

/**
 * The Class TariffEnquiryAttributesExpressionCacheImpl.
 */
@Stateless(mappedName = "TariffEnquiryAttributesExpressionCacheImpl")
public class TariffEnquiryAttributesExpressionCacheImpl implements TariffEnquiryAttributesExpressionService {

	/** The fdp cache. */
	@Resource(lookup = "java:app/fdpBusiness-1.0/MetaDataCache")
	private FDPCache<FDPMetaBag, FDPCacheable> fdpCache;

	@Inject
	private FDPTariffEnquiryAttributesConstraintsDAO attributesConstraintsDAO;

	/** The Logger */
	private static final Logger LOGGER = LoggerFactory.getLogger(TariffEnquiryAttributesExpressionCacheImpl.class);

	@Override
	public boolean updateMetaCache(final UpdateCacheDTO updateCacheDTO) throws FDPServiceException {
		if (updateCacheDTO == null || updateCacheDTO.getCircle() == null) {
			throw new FDPServiceException("UpdateCacheDTO cannot be null");
		} else {
				if ((null != updateCacheDTO.getUiObjectDTO()) && updateCacheDTO.getUiObjectDTO() instanceof String) {
					final String tariffOptionId = (String) updateCacheDTO.getUiObjectDTO();
					TariffEnquiryOption tariffEnquiryOption = TariffEnquiryOption
							.getTariffEnquiryOptionForId(tariffOptionId);
					final FDPCircle fdpCircle = updateCacheDTO.getCircle();
				try {
					deleteTariffEnquiryExpressionCache(fdpCircle, createKey(tariffEnquiryOption));
					initializeTariffEnquiryAttributeForTariffOptions(tariffEnquiryOption, fdpCircle);
				} catch (ExecutionFailedException e) {
					throw new FDPServiceException(e);
				} catch (ExpressionFailedException e) {
					throw new FDPServiceException(e);
				}
			}
		}
		return true;
	}

	@Override
	public boolean initializeMetaCache(final FDPCircle fdpCircle) throws FDPServiceException {
		if (fdpCircle == null) {
			throw new FDPServiceException("Circle cannot be null");
			} else {
				for (final TariffEnquiryOption tariffEnquiryOption : TariffEnquiryOption.values()) {
				try {
					initializeTariffEnquiryAttributeForTariffOptions(tariffEnquiryOption);
				} catch (ExecutionFailedException e) {
					throw new FDPServiceException(e);
				} catch (ExpressionFailedException e) {
					throw new FDPServiceException(e);
				}
			}
		}
		return true;
	}

	/**
	 * This method will load the Tariff-Enquiry Expression Cache for particular
	 * Tariff-Option.
	 *
	 * @param tariffEnquiryOption
	 * @throws ExecutionFailedException
	 * @throws ExpressionFailedException
	 */
	private void initializeTariffEnquiryAttributeForTariffOptions(final TariffEnquiryOption tariffEnquiryOption)
			throws ExecutionFailedException, ExpressionFailedException {
		final List<FDPTarrifConstraintStepDTO> constraintStepDTOs = attributesConstraintsDAO
				.getAllTariffAttributesConstraints(Integer.parseInt(tariffEnquiryOption.getOptionId()));
		loadUpdateCache(tariffEnquiryOption, constraintStepDTOs);
	}

	/**
	 * This method will load the Tariff-Enquiry Expression Cache for particular
	 * Tariff-Option.
	 *
	 * @param tariffEnquiryOption
	 * @throws ExecutionFailedException
	 * @throws ExpressionFailedException
	 */
	private void initializeTariffEnquiryAttributeForTariffOptions(final TariffEnquiryOption tariffEnquiryOption,
			final FDPCircle fdpCircle) throws ExecutionFailedException, ExpressionFailedException {
		final List<FDPTarrifConstraintStepDTO> constraintStepDTOs = attributesConstraintsDAO
				.getAllTariffAttributesConstraints(Integer.parseInt(tariffEnquiryOption.getOptionId()),
						fdpCircle.getCircleId());
		loadUpdateCache(tariffEnquiryOption, constraintStepDTOs);
		StringBuffer stringBuffer = new StringBuffer();
		stringBuffer.append("Cache loaded successfully for circle ");
		stringBuffer.append(fdpCircle.getCircleId());
		stringBuffer.append(", for TariffOptionType ");
		stringBuffer.append(tariffEnquiryOption.getName());
		LOGGER.info("{} : {} : {}", new Object[] { this.getClass().getName(),
				"initializeTariffEnquiryAttributeForTariffOptions", stringBuffer });
	}

	/**
	 * This method loads the cache at StartUp.
	 *
	 * @param tariffEnquiryOption
	 * @param constraintStepDTOs
	 * @throws ExecutionFailedException
	 * @throws ExpressionFailedException
	 */
	private void loadUpdateCache(final TariffEnquiryOption tariffEnquiryOption,
			final List<FDPTarrifConstraintStepDTO> constraintStepDTOs) throws ExecutionFailedException,
			ExpressionFailedException {
		final List<ConstraintStepDTO> listStepDTO = new ArrayList<ConstraintStepDTO>();
		FDPCircle fdpCircle = null;
		final String key = createKey(tariffEnquiryOption);
		for (final FDPTarrifConstraintStepDTO constraintStepDTO : constraintStepDTOs) {
			ConstraintStepDTO stepDTOs = populateConstraintStepDTO(constraintStepDTO, key);
			fdpCircle = (null == fdpCircle) ? populateFDPCircle(constraintStepDTO) : fdpCircle;
			listStepDTO.add(stepDTOs);
		}
		if (listStepDTO.size() > 0) {
			Expression expression = ExpressionUtil.createExpressionForSteps(listStepDTO);
			if (null != expression && null != key) {
				FDPTariffAttributesExpression fdpTariffAttributesExpression = new FDPTariffAttributesExpression(
						expression);
				if (null != fdpTariffAttributesExpression) {
					insertToCache(key, fdpCircle, fdpTariffAttributesExpression);
				}
			}
		}
	}

	/**
	 * This method creates the KEY for Cache.
	 *
	 * @param tariffEnquiryOption
	 * @return
	 */
	private String createKey(final TariffEnquiryOption tariffEnquiryOption) {
		final String key = ((TariffEnquiryOption.getTariffEnquiryOptionForId(tariffEnquiryOption.getOptionId()))
				.getName()) + TariffConstants.TARIFF_ATTRIBUTES_EXPRESSION_KEY;
		return key;
	}

	/**
	 * This method prepares the Step DTO.
	 *
	 * @param fdpTarrifConstraintStepDTO
	 * @return
	 * @throws ExecutionFailedException
	 */
	private ConstraintStepDTO populateConstraintStepDTO(final FDPTarrifConstraintStepDTO fdpTarrifConstraintStepDTO,
			final String name) throws ExecutionFailedException {
		ConstraintStepDTO constraintStepDTO = new ConstraintStepDTO();
		constraintStepDTO.setName(name);
		constraintStepDTO.setOperator(fdpTarrifConstraintStepDTO.getOperator());
		constraintStepDTO.setOrderValue(fdpTarrifConstraintStepDTO.getOrderValue());
		constraintStepDTO.setStepId(Long.valueOf(fdpTarrifConstraintStepDTO.getTarrifAttributeId()));
		constraintStepDTO.setSubStepDTOList(populateFDPTarrifConstraintSubStepDTO(fdpTarrifConstraintStepDTO
				.getFdpTariffConstraintSubSteps()));
		return constraintStepDTO;
	}

	/**
	 * This method will prepare the Sub-Step DTO.
	 *
	 * @param fdpTarrifConstraintSubStepDTOs
	 * @return
	 * @throws ExecutionFailedException
	 */
	private List<SubStepConstraintDTO> populateFDPTarrifConstraintSubStepDTO(
			final List<FDPTarrifConstraintSubStepDTO> fdpTarrifConstraintSubStepDTOs) throws ExecutionFailedException {
		List<SubStepConstraintDTO> subStepConstraintDTOs = new ArrayList<SubStepConstraintDTO>();
		for (final FDPTarrifConstraintSubStepDTO fdpTarrifConstraintSubStepDTO : fdpTarrifConstraintSubStepDTOs) {
			subStepConstraintDTOs.add(fdpTarrifConstraintSubStepDTO);
		}
		return subStepConstraintDTOs;
	}

	/**
	 * This method extracts the FDPCircle.
	 *
	 * @param constraintStepDTO
	 * @return
	 * @throws ExecutionFailedException
	 */
	private FDPCircle populateFDPCircle(final FDPTarrifConstraintStepDTO constraintStepDTO)
			throws ExecutionFailedException {
		FDPCircle fdpCircle = constraintStepDTO.getFdpCircle();
		return fdpCircle;
	}

	/**
	 * This method will finally put the VALUE in cache against the KEY.
	 *
	 * @param key
	 * @param fdpCircle
	 * @param attributesImpl
	 */
	private void insertToCache(final String key, final FDPCircle fdpCircle,
			final FDPTariffAttributesExpression fdpTariffAttributesExpression) {
		FDPMetaBag metaBag = new FDPMetaBag(fdpCircle, ModuleType.TARIFF_ENQUIRY_ATTRIBUTE_EXPRESSION, key);
		fdpCache.putValue(metaBag, fdpTariffAttributesExpression);
	}

	/**
	 * This method removes the Circle-wise Cache for a particular Tariff-Option
	 * Type.
	 *
	 * @param fdpCircle
	 * @param key
	 * @throws ExecutionFailedException
	 */
	private void deleteTariffEnquiryExpressionCache(final FDPCircle fdpCircle, final String key)
			throws ExecutionFailedException {
		FDPMetaBag fdpMetaBag = new FDPMetaBag(fdpCircle, ModuleType.TARIFF_ENQUIRY_ATTRIBUTE_EXPRESSION, key);
		fdpCache.removeKey(fdpMetaBag);
		StringBuffer stringBuffer = new StringBuffer();
		stringBuffer.append("Deleted Cache Key for Circle ");
		stringBuffer.append(fdpCircle.getCircleId());
		stringBuffer.append(", for Key");
		stringBuffer.append(key);
		LOGGER.info("{} : {} : {}", new Object[] { this.getClass().getName(), "deleteTariffEnquiryExpressionCache",
				stringBuffer.toString() });
	}

	@Override
	public ModuleType getModuleType() {
		return ModuleType.TARIFF_ENQUIRY_ATTRIBUTE_EXPRESSION;
	}
}
