package com.ericsson.fdp.business.policy.policyrule.impl;

import com.ericsson.fdp.business.charging.value.ChargingValue;
import com.ericsson.fdp.business.enums.ChargingTypeNotifications;
import com.ericsson.fdp.business.policy.policyrule.PolicyRule;
import com.ericsson.fdp.business.product.Product;
import com.ericsson.fdp.core.exception.ExecutionFailedException;
import com.ericsson.fdp.core.request.FDPRequest;

/**
 * The Class ExternalSystemDoubleConfirmationPolicyRuleImpl.
 * 
 * @author Ericsson
 */
public class DeProvisioningDoubleConfirmationPolicyRuleImpl extends ProductPolicyRuleImpl implements PolicyRule {

	/** The Constant serialVersionUID. */
	private static final long serialVersionUID = -2367356085581079712L;

	/**
	 * This method is used to get the notification text.
	 * 
	 * @param product
	 *            the product.
	 * @param chargingAmount
	 *            the charging amount.
	 * @param fdpRequest
	 *            the request object to be used.
	 * @return the response.
	 * @throws ExecutionFailedException
	 */
	@Override
	protected String getNotificationText(final Product product, final ChargingValue chargingAmount,
			final FDPRequest fdpRequest) throws ExecutionFailedException {
		if (chargingAmount.getChargingValue() != null) {
			final ChargingTypeNotifications chargingTypeNotifications = (ZERO_CHARGING_AMOUNT.equals(chargingAmount
					.getChargingValue().toString())) ? ChargingTypeNotifications.ZERO_CHARGING_DEPROVISIONING
					: ChargingTypeNotifications.POSITIVE_CHARGING_DEPROVISIONING;
			return getNotificationTextForChargingType(product, fdpRequest, chargingTypeNotifications);
		} else {
			throw new ExecutionFailedException("Charging Value is null in charging Amount.");
		}
	}

}
