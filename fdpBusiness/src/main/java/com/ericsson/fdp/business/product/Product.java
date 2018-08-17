package com.ericsson.fdp.business.product;

import java.util.List;

import com.ericsson.fdp.FDPCacheable;
import com.ericsson.fdp.business.charging.ProductCharging;
import com.ericsson.fdp.business.enums.ChargingTypeNotifications;
import com.ericsson.fdp.business.enums.State;
import com.ericsson.fdp.business.expression.Expression;
import com.ericsson.fdp.common.enums.ChannelType;
import com.ericsson.fdp.common.enums.ProductType;
import com.ericsson.fdp.core.command.FDPCommand;
import com.ericsson.fdp.dao.enums.ChargingType;
import com.ericsson.fdp.dao.enums.ProductAdditionalInfoEnum;

/**
 * The interface that provides access to methods required by product.
 * 
 * @author Ericsson
 * 
 */
public interface Product extends FDPCacheable {

	/**
	 * This method is used to get the constraints for a particular channel.
	 * 
	 * @param channel
	 *            The channel for which the constraints are required.
	 * @return The constraints for the channel.
	 */
	Expression getConstraintForChannel(ChannelType channel);

	/**
	 * This method returns the product charging for a particular channel.
	 * 
	 * @param channel
	 *            The channel for which the charging is required.
	 * @return The list of product charging.
	 */
	List<ProductCharging> getProductCharging(ChannelType channel, ChargingType chargingType);

	/**
	 * This method is used to get the list of provisioning actions.
	 * 
	 * @return The list of provisioning action.
	 */
	List<FDPCommand> getProvisioningActions();

	/**
	 * This method is used to return the product id.
	 * 
	 * @return the product id.
	 */
	Long getProductId();

	/**
	 * This method is used to get the state of the product.
	 * 
	 * @return The state of the product.
	 */
	State getState();

	/**
	 * This method is used to get the product type.
	 * 
	 * @return The product type.
	 */
	ProductType getProductType();

	/**
	 * This method is used to get the product name.
	 * 
	 * @return the product name.
	 */
	String getProductName();

	/**
	 * This method is used to get the product name for the channel.
	 * 
	 * @param channel
	 *            The channel for which the name is required.
	 * @return The name on the channel provided.
	 */
	String getProductNameForChannel(ChannelType channel);

	/**
	 * This is used to get the renewal count for the product.
	 * 
	 * @return the renewal count for the product.
	 */
	Integer getRenewalCount();

	/**
	 * This is used to get the IsSplit type of product.
	 * 
	 * @return true or false.
	 */
	Boolean getIsSplit();

	/**
	 * This is used to get product additional info based on key e.g. Key =
	 * SHARED_ACC_OFFER_ID, Value = 4.
	 */
	String getAdditionalInfo(ProductAdditionalInfoEnum additionalInfoKey);

	/**
	 * Sets the additional info.
	 * 
	 * @param additionalInfoKey
	 *            the additional info key
	 * @param value
	 *            the value
	 */
	void setAdditionalInfo(ProductAdditionalInfoEnum additionalInfoKey, String value);

	/**
	 * This method is used to get the notifications for the product for a
	 * channel and for a charging type.
	 * 
	 * @param channelType
	 *            the channel
	 * @param chargingTypeNotifications
	 *            the charging type.
	 * @return the notification id associated with this.
	 */
	Long getNotificationIdForChannel(ChannelType channelType, ChargingTypeNotifications chargingTypeNotifications);

	/**
	 * This method is used to get ProductDescription it may be null
	 * 
	 * */

	String getProductDescription();

	/**
	 * This is used to get the recurring or validity duration for the product.
	 * 
	 * @return the recurring or validity duration for the product.
	 */
	String getRecurringOrValidityValue();

	/**
	 * This is used to get the recurring or validity unit for the product.
	 * 
	 * @return the recurring or validity unit for the product.
	 */
	String getRecurringOrValidityUnit();

	/**
	 * The method is used for FAF type.
	 */
	public Boolean getIsFafType();

	/**
	 * This method is used to OnNet Count.
	 */
	public Integer getOnNet();

	/**
	 * This method is used to OffNet Count.
	 */
	public Integer getOffNet();

	/**
	 * This method is used to international Count.
	 */
	public Integer getInternational();
	
	public String getLoyaltyPoints();
	
	public String getLoyaltyItemCode();


}
