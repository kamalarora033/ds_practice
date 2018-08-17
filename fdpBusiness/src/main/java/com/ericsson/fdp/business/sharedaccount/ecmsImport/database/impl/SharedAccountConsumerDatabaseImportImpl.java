package com.ericsson.fdp.business.sharedaccount.ecmsImport.database.impl;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.ejb.Stateless;
import javax.inject.Inject;

import com.ericsson.fdp.business.enums.ImportErrorCode;
import com.ericsson.fdp.business.enums.SharedAccountConsumerValues;
import com.ericsson.fdp.business.sharedaccount.ecmsImport.database.AbstractSharedAccountDatabaseImport;
import com.ericsson.fdp.business.util.ClassUtil;
import com.ericsson.fdp.common.enums.Status;
import com.ericsson.fdp.core.exception.EvaluationFailedException;
import com.ericsson.fdp.core.exception.ExecutionFailedException;
import com.ericsson.fdp.dao.dto.sharedaccount.ProductAddInfoAttributeDTO;
import com.ericsson.fdp.dao.dto.sharedaccount.SharedAccountConsumerDTO;
import com.ericsson.fdp.dao.dto.sharedaccount.SharedAccountDTO;
import com.ericsson.fdp.dao.enums.ConsumerStatusEnum;
import com.ericsson.fdp.dao.enums.ConsumerUpgradeType;
import com.ericsson.fdp.dao.enums.ProductAdditionalInfoEnum;
import com.ericsson.fdp.dao.fdpadmin.FDPSharedAccountConsumerDAO;

/**
 * This class implements the methods required to import shared account consumer
 * values.
 * 
 * @author Ericsson
 * 
 */
@Stateless
public class SharedAccountConsumerDatabaseImportImpl extends
		AbstractSharedAccountDatabaseImport<SharedAccountConsumerValues, Object> {

	/**
	 * The shared account consumer dao.
	 */
	@Inject
	private FDPSharedAccountConsumerDAO fdpSharedAccountConsumerDAO;

	@Override
	public boolean updateDatabaseAsPerConfiguration(final boolean deleteRows) throws ExecutionFailedException {
		return true;
	}

	@Override
	public Map<Status, Object> validateValue(final Map<SharedAccountConsumerValues, Object> valueToValidate)
			throws EvaluationFailedException {
		final Map<Status, Object> validatedValue = new HashMap<Status, Object>();
		// validate if parent information is present.
		final Object parentMsisdn = valueToValidate.get(SharedAccountConsumerValues.GroupParentMSISDN);
		final Object parentOfferId = valueToValidate.get(SharedAccountConsumerValues.GroupParentOfferId);
		ImportErrorCode errorCode = validateParentGroupForNullValues(parentMsisdn, parentOfferId);
		ProductAddInfoAttributeDTO productInfo = null;
		final SharedAccountConsumerDTO sharedAccountConsumerDTO = new SharedAccountConsumerDTO();
		Long[] parentGroupId = null;
		if (errorCode == null) {
			if (parentMsisdn != null && parentOfferId != null) {
				parentGroupId = getParent(
						(Long) ClassUtil.getPrimitiveValueReturnNotNullObject(parentOfferId, Long.class),
						(Long) ClassUtil.getPrimitiveValueReturnNotNullObject(parentMsisdn, Long.class));
				if (parentGroupId == null || parentGroupId[0] == null) {
					errorCode = ImportErrorCode.PARENT_NOT_PRESENT;
				}
			}
			errorCode = validateMandatoryParameters(valueToValidate);
			if (errorCode == null) {
				productInfo = getProductByOfferId(valueToValidate.get(SharedAccountConsumerValues.ConsumerOfferId));
				if (productInfo == null) {
					errorCode = ImportErrorCode.OFFER_INVALID;
				}
			}
		}
		if (errorCode != null) {
			validatedValue.put(Status.FAILURE, errorCode);
		} else {
			sharedAccountConsumerDTO.setProviderGroupId(parentGroupId[0]);
			populateSharedAccConsumerDto(sharedAccountConsumerDTO, valueToValidate, productInfo);
			validatedValue.put(Status.SUCCESS, sharedAccountConsumerDTO);
		}
		return validatedValue;
	}

	@Override
	public ProductAddInfoAttributeDTO getProductByOfferId(final Object offerId) throws EvaluationFailedException {
		ProductAddInfoAttributeDTO productAddInfoAttributeDTO = null;
		if (offerId != null) {
			final Object offerIdInLong = ClassUtil.getPrimitiveValueReturnNotNullObject(offerId, Long.class);
			if (offerIdInLong instanceof Long) {
				final List<Long> productIds = getFdpProductAdditionalInfoDAO().getProductIdByInfoKeyAndValue(
						ProductAdditionalInfoEnum.PROVIDER_OFFER_ID_MAPPING.getKey().toString(),
						offerIdInLong.toString());
				if (!(productIds == null || productIds.isEmpty())) {
					final Long newProductId = productIds.get(0);
					productAddInfoAttributeDTO = getProviderAddInfo(newProductId);
				}
			}
		}
		return productAddInfoAttributeDTO;
	}

	/**
	 * This method is used to populate the shared account consumer dto.
	 * 
	 * @param sharedAccountConsumerDTO
	 *            the shared account consumer dto to populate.
	 * @param valueToValidate
	 *            the values from which the dto is to be populated.
	 * @param productInfo
	 *            the product info to be used.
	 * @throws EvaluationFailedException
	 *             Exception, if any.
	 */
	private void populateSharedAccConsumerDto(final SharedAccountConsumerDTO sharedAccountConsumerDTO,
			final Map<SharedAccountConsumerValues, Object> valueToValidate, final ProductAddInfoAttributeDTO productInfo)
			throws EvaluationFailedException {
		final Long consumerMSISDN = (Long) ClassUtil.getPrimitiveValueReturnNotNullObject(
				valueToValidate.get(SharedAccountConsumerValues.ConsumerMSISDN), Long.class);
		final Integer consumerlimit = (Integer) ClassUtil.getPrimitiveValueReturnNotNullObject(
				valueToValidate.get(SharedAccountConsumerValues.ConsumerLimit), Integer.class);
		sharedAccountConsumerDTO.setConsumerNewLimit(consumerlimit);
		sharedAccountConsumerDTO.setConsumerMsisdn(consumerMSISDN.toString());
		sharedAccountConsumerDTO.setConsumerThresholdUnit(valueToValidate.get(
				SharedAccountConsumerValues.ConsumerThresholdUnit).toString());
		sharedAccountConsumerDTO.setOfferId(productInfo.getProviderOfferIdMapping());
		sharedAccountConsumerDTO.setStatus(ConsumerStatusEnum.ACTIVE.toString());
		if (valueToValidate.get(SharedAccountConsumerValues.ConsumerName) == null) {
			sharedAccountConsumerDTO.setConsumerName(consumerMSISDN.toString());
		} else {
			sharedAccountConsumerDTO.setConsumerName(valueToValidate.get(SharedAccountConsumerValues.ConsumerName)
					.toString());
		}
		final ConsumerUpgradeType consumerUpgradeType = getUpgradeType(valueToValidate
				.get(SharedAccountConsumerValues.UpgradeType));
		if (ConsumerUpgradeType.TEMPORARY.equals(consumerUpgradeType)) {
			final Integer consumerlimitOld = (Integer) ClassUtil.getPrimitiveValueReturnNotNullObject(
					valueToValidate.get(SharedAccountConsumerValues.ConsumerLimitOld), Integer.class);
			sharedAccountConsumerDTO.setConsumerLimit(consumerlimitOld);
			sharedAccountConsumerDTO.setUpgradeExpiredDate(getExpiryDate(valueToValidate
					.get(SharedAccountConsumerValues.UpgradeExpiryDate)));
			sharedAccountConsumerDTO.setUpgradeType(ConsumerUpgradeType.TEMPORARY);
		} else {
			sharedAccountConsumerDTO.setConsumerLimit(null);
			sharedAccountConsumerDTO.setUpgradeExpiredDate(Calendar.getInstance());
			sharedAccountConsumerDTO.setUpgradeType(ConsumerUpgradeType.PERMANENT);
		}
		sharedAccountConsumerDTO.setDefaultThresholdCounterId(productInfo.getCommonUsageThresholdCounterID());
	}

	/**
	 * This method is used to validate the mandatory parameters.
	 * 
	 * @param valueToValidate
	 *            the map containing the values.
	 * @return error, if any error is present, null otherwise.
	 * @throws EvaluationFailedException
	 *             Exception, if any.
	 */
	private ImportErrorCode validateMandatoryParameters(final Map<SharedAccountConsumerValues, Object> valueToValidate)
			throws EvaluationFailedException {
		ImportErrorCode errorCode = null;
		final Object parentMsisdn = valueToValidate.get(SharedAccountConsumerValues.GroupParentMSISDN);
		final Object groupParentOfferId = valueToValidate.get(SharedAccountConsumerValues.GroupParentOfferId);
		Object consumerMsisdn = valueToValidate.get(SharedAccountConsumerValues.ConsumerMSISDN);
		Object consumerOfferId = valueToValidate.get(SharedAccountConsumerValues.ConsumerOfferId);
		final Object consumerThresholdUnit = valueToValidate.get(SharedAccountConsumerValues.ConsumerThresholdUnit);
		final Object consumerLimit = valueToValidate.get(SharedAccountConsumerValues.ConsumerLimit);
		if (parentMsisdn == null
				|| !(ClassUtil.getPrimitiveValueReturnNotNullObject(parentMsisdn, Long.class) instanceof Long)) {
			errorCode = ImportErrorCode.PARENT_GROUPMSISDN_NOT_VALID;
		} else if (groupParentOfferId == null
				|| !(ClassUtil.getPrimitiveValueReturnNotNullObject(groupParentOfferId, Long.class) instanceof Long)) {
			errorCode = ImportErrorCode.PARENT_OFFERID_NOT_VALID;
		} else if (consumerMsisdn == null
				|| !((consumerMsisdn = ClassUtil.getPrimitiveValueReturnNotNullObject(consumerMsisdn, Long.class)) instanceof Long)) {
			errorCode = ImportErrorCode.MSISDN_INVALID;
		} else if (groupParentOfferId == null
				|| !((consumerOfferId = ClassUtil.getPrimitiveValueReturnNotNullObject(consumerOfferId, Long.class)) instanceof Long)) {
			errorCode = ImportErrorCode.OFFER_INVALID;
		} else if (consumerThresholdUnit == null) {
			errorCode = ImportErrorCode.THRESHOLD_INVALID;
		} else if (!(isRowUnique((Long) consumerOfferId, (Long) consumerMsisdn))) {
			errorCode = ImportErrorCode.ALREADY_EXIST;
		} else if (consumerLimit == null
				|| !(ClassUtil.getPrimitiveValueReturnNotNullObject(consumerLimit, Integer.class) instanceof Integer)) {
			errorCode = ImportErrorCode.CONSUMER_LIMIT_INVALID;
		} else {
			errorCode = validateUpgradeTypes(valueToValidate);
		}
		return errorCode;
	}

	/**
	 * This method is used to validate the upgrade types.
	 * 
	 * @param valueToValidate
	 *            the map containing the values.
	 * @return error if present, null other wise.
	 * @throws EvaluationFailedException
	 *             Exception, if any.
	 */
	private ImportErrorCode validateUpgradeTypes(final Map<SharedAccountConsumerValues, Object> valueToValidate)
			throws EvaluationFailedException {
		final Object upgradeType = valueToValidate.get(SharedAccountConsumerValues.UpgradeType);
		ImportErrorCode errorCode = null;
		final ConsumerUpgradeType consumerUpgradeType = getUpgradeType(upgradeType);
		if (upgradeType != null && consumerUpgradeType == null) {
			errorCode = ImportErrorCode.UPGRADE_TYPE_INVALID;
		}
		if (consumerUpgradeType != null && ConsumerUpgradeType.TEMPORARY.equals(consumerUpgradeType)) {
			// validate the consumer limit old and expiry date.
			final Object consumerLimitOld = valueToValidate.get(SharedAccountConsumerValues.ConsumerLimitOld);
			final Object expiryDate = valueToValidate.get(SharedAccountConsumerValues.UpgradeExpiryDate);
			if (consumerLimitOld == null
					|| !(ClassUtil.getPrimitiveValueReturnNotNullObject(consumerLimitOld, Long.class) instanceof Long)) {
				errorCode = ImportErrorCode.CONSUMER_LIMIT_OLD_INVALID;
			} else if (expiryDate == null || getExpiryDate(expiryDate) == null) {
				errorCode = ImportErrorCode.EXPIRY_DATE_INVALID;
			}
		}
		return errorCode;
	}

	/**
	 * This method is used to get the expiry date.
	 * 
	 * @param expiryDate
	 *            the expiry date.
	 * @return the expiry date.
	 */
	private Calendar getExpiryDate(final Object expiryDate) {
		Calendar date = null;
		try {
			if (expiryDate instanceof Date) {
				final Calendar cal = Calendar.getInstance();
				cal.setTime((Date) expiryDate);
				date = cal;
			} else {
				final Object dateVal = ClassUtil.getPrimitiveValueReturnNotNullObject(expiryDate, Date.class);
				if (dateVal instanceof Calendar) {
					date = (Calendar) dateVal;
				}
			}
		} catch (final EvaluationFailedException e) {
			// The date is invalid.
			date = null;
		}
		return date;
	}

	/**
	 * This method is used to find the consumer upgrade type from the provided
	 * value.
	 * 
	 * @param upgradeType
	 *            the upgrade type.
	 * @return the consumer upgrade type.
	 */
	private ConsumerUpgradeType getUpgradeType(final Object upgradeType) {
		ConsumerUpgradeType consumerUpgradeType = null;
		if (upgradeType.toString().trim().equalsIgnoreCase(ConsumerUpgradeType.PERMANENT.name())) {
			consumerUpgradeType = ConsumerUpgradeType.PERMANENT;
		} else if (upgradeType.toString().trim().equalsIgnoreCase(ConsumerUpgradeType.TEMPORARY.name())) {
			consumerUpgradeType = ConsumerUpgradeType.TEMPORARY;
		} else {
			try {
				consumerUpgradeType = ConsumerUpgradeType
						.getUpgradeTypeByCode(Integer.parseInt(upgradeType.toString()));
			} catch (final NumberFormatException e) {
				// The upgrade type is undefined. Handled in error code.
				consumerUpgradeType = null;
			}
		}
		return consumerUpgradeType;
	}

	@Override
	public List<Map<SharedAccountConsumerValues, Object>> importValues(
			final List<Map<SharedAccountConsumerValues, Object>> valuesToImport) throws EvaluationFailedException {
		final List<Map<SharedAccountConsumerValues, Object>> failedValues = new ArrayList<Map<SharedAccountConsumerValues, Object>>();
		for (final Map<SharedAccountConsumerValues, Object> valueToValidate : valuesToImport) {
			final Map<Status, Object> validatedValue = validateValue(valueToValidate);
			final Object sharedAccountConsumerValue = validatedValue.get(Status.SUCCESS);
			if (sharedAccountConsumerValue != null) {
				if (sharedAccountConsumerValue instanceof SharedAccountDTO) {
					final SharedAccountDTO sharedAccountDTO = (SharedAccountDTO) sharedAccountConsumerValue;
					fdpSharedAccountConsumerDAO.saveConsumer(sharedAccountDTO);
				}
			} else {
				valueToValidate.put(SharedAccountConsumerValues.ERROR_CODE, validatedValue.get(Status.FAILURE));
				failedValues.add(valueToValidate);
			}
		}
		return failedValues;
	}

	@Override
	public boolean isRowUnique(final Long offerId, final Long msisdn) {
		boolean isUnique = false;
		final SharedAccountConsumerDTO sharedAccountConsumerDTO = fdpSharedAccountConsumerDAO.getConsumer(msisdn,
				offerId);
		if (sharedAccountConsumerDTO == null) {
			isUnique = true;
		}
		return isUnique;
	}

}