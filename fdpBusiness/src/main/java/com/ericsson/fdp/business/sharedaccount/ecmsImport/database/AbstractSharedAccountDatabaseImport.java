package com.ericsson.fdp.business.sharedaccount.ecmsImport.database;

import java.util.List;
import java.util.Map;

import javax.inject.Inject;

import com.ericsson.fdp.business.enums.ImportErrorCode;
import com.ericsson.fdp.business.util.ClassUtil;
import com.ericsson.fdp.business.util.SharedAccountUtil;
import com.ericsson.fdp.core.exception.EvaluationFailedException;
import com.ericsson.fdp.dao.dto.sharedaccount.ProductAddInfoAttributeDTO;
import com.ericsson.fdp.dao.dto.sharedaccount.SharedAccGpDTO;
import com.ericsson.fdp.dao.enums.ProductAdditionalInfoEnum;
import com.ericsson.fdp.dao.enums.SharedAccountGroupStatus;
import com.ericsson.fdp.dao.fdpadmin.FDPProductAdditionalInfoDAO;
import com.ericsson.fdp.dao.fdpadmin.FDPSharedAccountGroupDAO;

/**
 * This is an abstract class and implements the shared account database import
 * interface.
 * 
 * @author Ericsson
 * 
 * @param <T>
 *            the key to be used.
 * @param <K>
 *            the value to be used.
 */
public abstract class AbstractSharedAccountDatabaseImport<T, K> implements SharedAccountDatabaseImport<T, K> {

	/**
	 * The product additional info dao.
	 */
	@Inject
	private FDPProductAdditionalInfoDAO fdpProductAdditionalInfoDAO;

	/**
	 * The shared account group dao.
	 */
	@Inject
	private FDPSharedAccountGroupDAO fdpSharedAccountGroupDAO;

	/**
	 * This method checks if the row is unique for the provided offer id and
	 * msisdn.
	 * 
	 * @param offerId
	 *            the offer id to be used.
	 * @param msisdn
	 *            the msisdn to be used.
	 * @return true, if row is unique, false if row is already present and is
	 *         not unique.
	 */
	public abstract boolean isRowUnique(Long offerId, Long msisdn);

	/**
	 * This method is used to validate the parent information for null values.
	 * 
	 * @param groupId
	 *            the group offer id to be used.
	 * @param groupMsisdn
	 *            the group msisdn to be used.
	 * @return error, if present, null otherwise.
	 */
	public ImportErrorCode validateParentGroupForNullValues(final Object groupId, final Object groupMsisdn) {
		if (groupId == null ^ groupMsisdn == null) {
			return ImportErrorCode.PARTIAL_PARENT_INFORMATION_PRESENT;
		}
		try {
			if (groupId != null
					&& !(ClassUtil.getPrimitiveValueReturnNotNullObject(groupId, Long.class) instanceof Long)) {
				return ImportErrorCode.PARENT_OFFERID_NOT_VALID;
			}
		} catch (EvaluationFailedException e) {
			return ImportErrorCode.PARENT_OFFERID_NOT_VALID;
		}
		try {
			if (groupMsisdn != null
					&& !(ClassUtil.getPrimitiveValueReturnNotNullObject(groupMsisdn, Long.class) instanceof Long)) {
				return ImportErrorCode.PARENT_GROUPMSISDN_NOT_VALID;
			}
		} catch (EvaluationFailedException e) {
			return ImportErrorCode.PARENT_GROUPMSISDN_NOT_VALID;
		}
		return null;
	}

	/**
	 * This method is used to get the product information based on the offer id.
	 * 
	 * @param offerId
	 *            the offer id to be used.
	 * @return the product additional information.
	 * @throws EvaluationFailedException
	 *             Exception, if any.
	 */
	public ProductAddInfoAttributeDTO getProductByOfferId(final Object offerId) throws EvaluationFailedException {
		ProductAddInfoAttributeDTO productAddInfoAttributeDTO = null;
		if (offerId != null) {
			Object offerIdInLong = ClassUtil.getPrimitiveValueReturnNotNullObject(offerId, Long.class);
			if (offerIdInLong instanceof Long) {
				List<Long> productIds = fdpProductAdditionalInfoDAO.getProductIdByInfoKeyAndValue(
						ProductAdditionalInfoEnum.SHARED_ACC_OFFER_ID.getKey().toString(), offerIdInLong.toString());
				if (!(productIds == null || productIds.isEmpty())) {
					Long newProductId = productIds.get(0);
					productAddInfoAttributeDTO = getProviderAddInfo(newProductId);
				}
			}
		}
		return productAddInfoAttributeDTO;
	}

	/**
	 * This method is used to get the provider additional info based on the
	 * product id.
	 * 
	 * @param productId
	 *            the product id to be used.
	 * @return the product additional info.
	 */
	public ProductAddInfoAttributeDTO getProviderAddInfo(final Long productId) {
		Map<Integer, String> productInfoValueMap = fdpProductAdditionalInfoDAO
				.getProductAdditionalInfoMapById(productId);
		ProductAddInfoAttributeDTO productAddInfoAttributeDTO = SharedAccountUtil
				.getProductAdditionalInfo(productInfoValueMap);
		productAddInfoAttributeDTO.setProductId(productId);
		return productAddInfoAttributeDTO;
	}

	/**
	 * This method is used to get the parent for the provided offer id and
	 * msisdn.
	 * 
	 * @param offerId
	 *            the offer id to be used.
	 * @param msisdn
	 *            the msisdn to be used.
	 * @return the row containing the information. The row id at 0, and web
	 *         product id at 1.
	 */
	public Long[] getParent(final Long offerId, final Long msisdn) {
		List<SharedAccGpDTO> providerInfo = fdpSharedAccountGroupDAO.getSharedAccGroup(msisdn, offerId,
				SharedAccountGroupStatus.ACTIVE);
		Long[] returnVal = null;
		if (!(providerInfo == null || providerInfo.isEmpty())) {
			returnVal = new Long[] { providerInfo.get(0).getSharedAccID(), providerInfo.get(0).getWebProductId() };
		}
		return returnVal;
	}

	/**
	 * This method is used to get the product additional info dao.
	 * 
	 * @return the product additional info dao.
	 */
	public FDPProductAdditionalInfoDAO getFdpProductAdditionalInfoDAO() {
		return fdpProductAdditionalInfoDAO;
	}
}
