package com.ericsson.fdp.business.sharedaccount.ecmsImport.database.impl;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.Resource;
import javax.ejb.Stateless;
import javax.inject.Inject;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ericsson.fdp.business.enums.ImportErrorCode;
import com.ericsson.fdp.business.enums.SharedAccountProviderValues;
import com.ericsson.fdp.business.sharedaccount.constans.SharedAccountConstants;
import com.ericsson.fdp.business.sharedaccount.ecmsImport.database.AbstractSharedAccountDatabaseImport;
import com.ericsson.fdp.business.util.ClassUtil;
import com.ericsson.fdp.common.constants.JNDILookupConstant;
import com.ericsson.fdp.common.enums.Status;
import com.ericsson.fdp.core.cache.AppCacheSubStore;
import com.ericsson.fdp.core.dsm.framework.service.FDPCache;
import com.ericsson.fdp.core.exception.EvaluationFailedException;
import com.ericsson.fdp.core.exception.ExecutionFailedException;
import com.ericsson.fdp.core.serviceprovisioning.bean.FDPAppBag;
import com.ericsson.fdp.dao.dto.product.ProductDTO;
import com.ericsson.fdp.dao.dto.sharedaccount.BuySharedProductDTO;
import com.ericsson.fdp.dao.dto.sharedaccount.ProductAddInfoAttributeDTO;
import com.ericsson.fdp.dao.dto.sharedaccount.SharedAccGpDTO;
import com.ericsson.fdp.dao.dto.sharedaccount.SharedAccountDTO;
import com.ericsson.fdp.dao.enums.SharedAccountGroupStatus;
import com.ericsson.fdp.dao.fdpadmin.FDPSharedAccountGroupDAO;
import com.ericsson.fdp.dao.fdpadmin.FDPSharedAccountReqDAO;
import com.ericsson.fdp.dao.fdpadmin.FDPWebUserProductDAO;

/**
 * This class implements the methods required to import shared account provider
 * values.
 * 
 * @author Ericsson
 */
@Stateless
public class SharedAccountProviderDatabaseImportImpl extends
		AbstractSharedAccountDatabaseImport<SharedAccountProviderValues, Object> {

	/**
	 * The shared account group dao.
	 */
	@Inject
	private FDPSharedAccountGroupDAO fdpSharedAccountGroupDAO;

	@Inject
	private FDPSharedAccountReqDAO fdpSharedAccountReqDAO;

	/**
	 * The application config cache.
	 */
	@Resource(lookup = JNDILookupConstant.APPLICATION_CONFIG_CACHE_JNDI_NAME)
	private FDPCache<FDPAppBag, Object> applicationConfigCache;

	/** The Constant LOGGER. */
	private static final Logger LOGGER = LoggerFactory.getLogger(SharedAccountProviderDatabaseImportImpl.class);

	/**
	 * The web user product dao.
	 */
	@Inject
	private FDPWebUserProductDAO fdpWebUserProductDAO;

	@Override
	public boolean updateDatabaseAsPerConfiguration(final boolean deleteRows) throws ExecutionFailedException {
		if (deleteRows) {
			LOGGER.info("deleteing shared account group");
			fdpSharedAccountGroupDAO.deleteAll();
			LOGGER.info("deleteing shared account request");
			fdpSharedAccountReqDAO.deleteAll();
		}
		return true;
	}

	@Override
	public Map<Status, Object> validateValue(final Map<SharedAccountProviderValues, Object> valueToValidate)
			throws EvaluationFailedException {
		final Map<Status, Object> validatedValue = new HashMap<Status, Object>();
		// validate if parent information is present.
		final Object providerMsisdn = valueToValidate.get(SharedAccountProviderValues.GroupParentOfferId);
		final Object offerId = valueToValidate.get(SharedAccountProviderValues.GroupParentMSISDN);
		ImportErrorCode errorCode = validateParentGroupForNullValues(providerMsisdn, offerId);
		ProductAddInfoAttributeDTO productInfo = null;
		final SharedAccGpDTO sharedAccGpDTO = new SharedAccGpDTO();
		Long[] parentGroupId = null;
		if (errorCode == null) {
			if (providerMsisdn != null && offerId != null) {
				parentGroupId =
						getParent((Long) ClassUtil.getPrimitiveValueReturnNotNullObject(providerMsisdn, Long.class),
								(Long) ClassUtil.getPrimitiveValueReturnNotNullObject(offerId, Long.class));
				if (parentGroupId == null) {
					errorCode = ImportErrorCode.PARENT_NOT_PRESENT;
				}
			} else {
				errorCode = validateMandatoryParameters(valueToValidate);
				if (errorCode == null) {
					productInfo = getProductByOfferId(valueToValidate.get(SharedAccountProviderValues.GroupOfferId));
					if (productInfo == null) {
						errorCode = ImportErrorCode.OFFER_INVALID;
					}
				}
			}
		}
		if (errorCode != null) {
			validatedValue.put(Status.FAILURE, errorCode);
		} else {
			if (parentGroupId != null) {
				sharedAccGpDTO.setParentSharedAccGpId(parentGroupId[0]);
				sharedAccGpDTO.setWebProductId(parentGroupId[1]);
			}
			populateSharedAccGrpDto(sharedAccGpDTO, valueToValidate, productInfo);
			validatedValue.put(Status.SUCCESS, sharedAccGpDTO);
		}
		return validatedValue;
	}

	/**
	 * This method is used to create shared account product dto.
	 * 
	 * @param productId
	 *            the product id to use.
	 * @param productAddInfo
	 *            the product additional info to use.
	 * @param subscriberNumber
	 *            the subscriber number to use.
	 * @return the shared account product dto.
	 */
	private BuySharedProductDTO getSharedAccountDTO(final Long productId,
			final ProductAddInfoAttributeDTO productAddInfo, final Long subscriberNumber) {

		final BuySharedProductDTO buySharedProductDto = new BuySharedProductDTO();
		buySharedProductDto.setProductId(productId);
		buySharedProductDto.setBoughtFor(subscriberNumber);
		buySharedProductDto.setBoughtBy(subscriberNumber.toString());
		buySharedProductDto.setGroupProviderName(subscriberNumber.toString());
		buySharedProductDto.setGroupProviderMsisdn(subscriberNumber);
		final FDPAppBag fdpAppBag = new FDPAppBag();
		fdpAppBag.setSubStore(AppCacheSubStore.CONFIGURATION_MAP);
		fdpAppBag.setKey(SharedAccountConstants.DEFAULT_ACCOUNT_NAME);
		buySharedProductDto.setAccountName(applicationConfigCache.getValue(fdpAppBag).toString());

		fdpAppBag.setSubStore(AppCacheSubStore.CONFIGURATION_MAP);
		fdpAppBag.setKey(SharedAccountConstants.DEFAULT_GROUP_NAME);
		buySharedProductDto.setGroupName(applicationConfigCache.getValue(fdpAppBag).toString());
		buySharedProductDto.setGroupLimit(productAddInfo.getGroupLimit());

		buySharedProductDto.setOfferId(productAddInfo.getShrAccOfferId());

		buySharedProductDto.setProviderThresholdUnit(productAddInfo.getGroupThresholdUnit());

		return buySharedProductDto;

	}

	/**
	 * This method is used to populate shared account group dto.
	 * 
	 * @param sharedAccGpDTO
	 *            the shared account group dto to populate.
	 * @param valueToValidate
	 *            the value to validate.
	 * @param productInfo
	 *            the product info to use.
	 * @throws EvaluationFailedException
	 *             Exception, if any.
	 */
	private void populateSharedAccGrpDto(final SharedAccGpDTO sharedAccGpDTO,
			final Map<SharedAccountProviderValues, Object> valueToValidate, final ProductAddInfoAttributeDTO productInfo)
			throws EvaluationFailedException {
		final Long providerMsisdn =
				(Long) ClassUtil.getPrimitiveValueReturnNotNullObject(
						valueToValidate.get(SharedAccountProviderValues.GroupMSISDN), Long.class);
		sharedAccGpDTO.setGroupProviderMSISDN(providerMsisdn);
		sharedAccGpDTO.setStatus(SharedAccountGroupStatus.ACTIVE);
		if (valueToValidate.get(SharedAccountProviderValues.GroupName) != null) {
			sharedAccGpDTO.setGroupName(valueToValidate.get(SharedAccountProviderValues.GroupName).toString());
		} else {
			final FDPAppBag fdpAppBag = new FDPAppBag();
			fdpAppBag.setSubStore(AppCacheSubStore.CONFIGURATION_MAP);
			fdpAppBag.setKey(SharedAccountConstants.DEFAULT_GROUP_NAME);
			sharedAccGpDTO.setGroupName(applicationConfigCache.getValue(fdpAppBag).toString());
		}
		if (valueToValidate.get(SharedAccountProviderValues.GroupProviderName) != null) {
			sharedAccGpDTO.setGroupProviderName(valueToValidate.get(SharedAccountProviderValues.GroupProviderName)
					.toString());
		} else {
			sharedAccGpDTO.setGroupProviderName(providerMsisdn.toString());
		}
		sharedAccGpDTO.setGroupLimit(productInfo.getGroupLimit());
		sharedAccGpDTO.setGroupThresholdUnit(valueToValidate.get(SharedAccountProviderValues.ProviderThresholdUnit)
				.toString());
		sharedAccGpDTO.setProduct(new ProductDTO(productInfo.getProductId()));
		sharedAccGpDTO.setOfferId(productInfo.getShrAccOfferId());
		sharedAccGpDTO.setCreatedBy(providerMsisdn.toString());
		if (sharedAccGpDTO.getWebProductId() == null) {
			sharedAccGpDTO.setWebProductId(fdpWebUserProductDAO.saveWebUserProduct(getSharedAccountDTO(
					productInfo.getProductId(), productInfo, providerMsisdn)));
		}
	}

	/**
	 * This method is used to validate the mandatory parameters.
	 * 
	 * @param valueToValidate
	 *            the map containing the values.
	 * @return error if any, null otherwise.
	 * @throws EvaluationFailedException
	 *             Exception, if any.
	 */
	private ImportErrorCode validateMandatoryParameters(final Map<SharedAccountProviderValues, Object> valueToValidate)
			throws EvaluationFailedException {
		ImportErrorCode errorCode = null;
		Object groupMsisdn = valueToValidate.get(SharedAccountProviderValues.GroupMSISDN);
		Object groupOfferId = valueToValidate.get(SharedAccountProviderValues.GroupOfferId);
		final Object providerThresholdUnit = valueToValidate.get(SharedAccountProviderValues.ProviderThresholdUnit);
		if (groupMsisdn == null
				|| !((groupMsisdn = ClassUtil.getPrimitiveValueReturnNotNullObject(groupMsisdn, Long.class)) instanceof Long)) {
			errorCode = ImportErrorCode.MSISDN_INVALID;
		} else if (groupOfferId == null
				|| !((groupOfferId = ClassUtil.getPrimitiveValueReturnNotNullObject(groupOfferId, Long.class)) instanceof Long)) {
			errorCode = ImportErrorCode.OFFER_INVALID;
		} else if (providerThresholdUnit == null) {
			errorCode = ImportErrorCode.THRESHOLD_INVALID;
		} else if (!(isRowUnique((Long) groupOfferId, (Long) groupMsisdn))) {
			errorCode = ImportErrorCode.ALREADY_EXIST;
		}

		return errorCode;
	}

	@Override
	public List<Map<SharedAccountProviderValues, Object>> importValues(
			final List<Map<SharedAccountProviderValues, Object>> valuesToImport) throws EvaluationFailedException {
		final List<Map<SharedAccountProviderValues, Object>> failedValues =
				new ArrayList<Map<SharedAccountProviderValues, Object>>();
		for (final Map<SharedAccountProviderValues, Object> valueToValidate : valuesToImport) {
			final Map<Status, Object> validatedValue = validateValue(valueToValidate);
			final Object sharedAccountProviderValue = validatedValue.get(Status.SUCCESS);
			if (sharedAccountProviderValue != null) {
				if (sharedAccountProviderValue instanceof SharedAccountDTO) {
					final SharedAccountDTO sharedAccountDTO = (SharedAccountDTO) sharedAccountProviderValue;
					fdpSharedAccountGroupDAO.saveSharedAccountGroup(sharedAccountDTO, sharedAccountDTO.getCreatedBy());
				}
			} else {
				valueToValidate.put(SharedAccountProviderValues.ERROR_CODE, validatedValue.get(Status.FAILURE));
				failedValues.add(valueToValidate);
			}
		}
		return failedValues;
	}

	@Override
	public boolean isRowUnique(final Long offerId, final Long msisdn) {
		boolean isUnique = false;
		final Long[] parentId = getParent(offerId, msisdn);
		if (parentId == null || parentId[0] == null) {
			isUnique = true;
		}
		return isUnique;
	}

}