package com.ericsson.fdp.business.product.impl;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.ericsson.fdp.business.charging.ProductCharging;
import com.ericsson.fdp.business.charging.impl.FixedCharging;
import com.ericsson.fdp.business.enums.ChargingTypeNotifications;
import com.ericsson.fdp.business.enums.State;
import com.ericsson.fdp.business.expression.Expression;
import com.ericsson.fdp.business.product.Product;
import com.ericsson.fdp.common.constants.FDPConstant;
import com.ericsson.fdp.common.enums.ChannelType;
import com.ericsson.fdp.common.enums.ExternalSystem;
import com.ericsson.fdp.common.enums.ProductType;
import com.ericsson.fdp.common.enums.ShareType;
import com.ericsson.fdp.core.command.FDPCommand;
import com.ericsson.fdp.dao.enums.ChargingType;
import com.ericsson.fdp.dao.enums.ProductAdditionalInfoEnum;

/**
 * This class implements the product interface.
 * 
 * @author Ericsson
 * 
 */
public class BaseProduct implements Product {

	/**
	 * 
	 */
	private static final long serialVersionUID = -2550448051537903308L;

	/**
	 * The constraints which are stored channel wise.
	 */
	private Map<ChannelType, Expression> constraints;

	/**
	 * The product id.
	 */
	private Long productId;

	/**
	 * The charging step which is channel wise.
	 */
	private Map<ChannelType, Map<ChargingType, List<ProductCharging>>> charging;

	/**
	 * The list of provisioning actions.
	 */
	private List<FDPCommand> provisioningActions;

	/**
	 * The product name.
	 */
	private String productName;

	/**
	 * The state of the product.
	 */
	private State state;

	/**
	 * The product type.
	 */
	private ProductType productType;

	/**
	 * The map defines the names for the channel.
	 */
	private Map<ChannelType, String> channelTypeName = null;

	/**
	 * The renewal count for the product.
	 */
	private Integer renewalCount;

	/**
	 * The renewal count for the product.
	 */
	
	
	/**changed by ashish**/
	/**
	 *  product description
	 */
	private String productDescription;
	
	/**
	 * additionalInfoMap as key-value pair e.g. Key = SHARED_ACC_OFFER_ID , Value = 12 
	 * */
	private Map<ProductAdditionalInfoEnum, String> additionalInfoMap;
	
	/** The product notifiction map. */
	private Map<String , Long> productNotifictionMap;
	
	private Boolean isSplit;
	
	private String recurringOrValidityUnit;
	
	private String shareType;
	
	private String chargesTypes;
	
	private String charges;
	
	private Boolean isFafType;
	
	private Integer onNet;
	
	private Integer offNet;
	
	private Integer international;
	
	private String productIdValue;
	
	/**
	 * Instantiates a new base product.
	 * 
	 * @param productId
	 *            the product id value
	 * @param productName
	 *            the name of the product
	 * @param constraints
	 *            a map of product constraints with key as {@link ChannelType}
	 *            and value as an {@link Expression} object.
	 * @param charging
	 *            a map of product charging with key as {@link ChannelType} and
	 *            value as list of {@link ProductCharging} objects
	 * @param productStatus
	 *            TODO
	 */
	/**changed by shankey**/
	
	private String connectionType;
	private String recurringOrValidityValue;

	private String loyaltyPoints;
	private String loyaltyItemCode;
			
		public BaseProduct(Long productId, String productName, Map<ChannelType, Expression> constraints,

			Map<ChannelType, Map<ChargingType, List<ProductCharging>>> charging, ProductType productType,
			Map<ChannelType, String> channelTypeNameToSet, Integer reccuringTimes, State productStatus,
			Map<String , Long> productNotifictionMap) {
		this.productId = productId;
		this.productName = productName;
		this.constraints = constraints;
		this.charging = charging;
		this.productType = productType;
		this.channelTypeName = channelTypeNameToSet;
		this.renewalCount = reccuringTimes;
		this.state = productStatus;
		this.productNotifictionMap = productNotifictionMap;
		
	}
		
		
	public BaseProduct(Long productId, String productName, Map<ChannelType, Expression> constraints,
			Map<ChannelType, Map<ChargingType, List<ProductCharging>>> charging, ProductType productType,
			Map<ChannelType, String> channelTypeNameToSet, Integer reccuringTimes, State productStatus,
			Map<String , Long> productNotifictionMap, String connectionType, String recurringOrValidityValue, 
			String recurringOrValidityUnit,String productDescription,Boolean isSplit, String shareType, String chargesTypes, String charges, Boolean isFafType,
			Integer onNet, Integer offNet, Integer international) {
		this.productId = productId;
		this.productName = productName;
		this.constraints = constraints;
		this.charging = charging;
		this.productType = productType;
		this.channelTypeName = channelTypeNameToSet;
		this.renewalCount = reccuringTimes;
		this.state = productStatus;
		this.productNotifictionMap = productNotifictionMap;
		this.connectionType = connectionType;
		this.setRecurringOrValidityValue(recurringOrValidityValue);
		this.setRecurringOrValidityUnit(recurringOrValidityUnit);
		this.productDescription = productDescription;
		this.isSplit = isSplit;
		this.shareType = shareType;
		this.charges = charges;
		this.chargesTypes = chargesTypes;	
		this.isFafType=isFafType;
		this.onNet=onNet;
		this.offNet= offNet;
		this.international=international;
		}
	
	public BaseProduct(Long productId, String productName, Map<ChannelType, Expression> constraints,
			Map<ChannelType, Map<ChargingType, List<ProductCharging>>> charging, ProductType productType,
			Map<ChannelType, String> channelTypeNameToSet, Integer reccuringTimes, State productStatus,
			Map<String , Long> productNotifictionMap, String connectionType, String recurringOrValidityValue, 
			String recurringOrValidityUnit,String productDescription,Boolean isSplit, String shareType, String chargesTypes, String charges, Boolean isFafType,
			Integer onNet, Integer offNet, Integer international, String productIdValue, String loyaltyItemCode, String loyaltyPoints) {
		this.productId = productId;
		this.productName = productName;
		this.constraints = constraints;
		this.charging = charging;
		this.productType = productType;
		this.channelTypeName = channelTypeNameToSet;
		this.renewalCount = reccuringTimes;
		this.state = productStatus;
		this.productNotifictionMap = productNotifictionMap;
		this.connectionType = connectionType;
		this.setRecurringOrValidityValue(recurringOrValidityValue);
		this.setRecurringOrValidityUnit(recurringOrValidityUnit);
		this.productDescription = productDescription;
		this.isSplit = isSplit;
		this.shareType = shareType;
		this.charges = charges;
		this.chargesTypes = chargesTypes;	
		this.isFafType=isFafType;
		this.onNet=onNet;
		this.offNet= offNet;
		this.international=international;
		this.productIdValue = productIdValue;
		this.loyaltyItemCode =loyaltyItemCode;
		this.loyaltyPoints =  loyaltyPoints;
		}
	
	public BaseProduct(Long productId, String productName, Map<ChannelType, Expression> constraints,
			Map<ChannelType, Map<ChargingType, List<ProductCharging>>> charging, ProductType productType,
			Map<ChannelType, String> channelTypeNameToSet, Integer reccuringTimes, State productStatus,
			Map<String , Long> productNotifictionMap, String connectionType, String recurringOrValidityValue, 
			String recurringOrValidityUnit,String productDescription,Boolean isSplit, String shareType, String chargesTypes, String charges, Boolean isFafType,
			Integer onNet, Integer offNet, Integer international, String productIdValue) {
		this.productId = productId;
		this.productName = productName;
		this.constraints = constraints;
		this.charging = charging;
		this.productType = productType;
		this.channelTypeName = channelTypeNameToSet;
		this.renewalCount = reccuringTimes;
		this.state = productStatus;
		this.productNotifictionMap = productNotifictionMap;
		this.connectionType = connectionType;
		this.setRecurringOrValidityValue(recurringOrValidityValue);
		this.setRecurringOrValidityUnit(recurringOrValidityUnit);
		this.productDescription = productDescription;
		this.isSplit = isSplit;
		this.shareType = shareType;
		this.charges = charges;
		this.chargesTypes = chargesTypes;	
		this.isFafType=isFafType;
		this.onNet=onNet;
		this.offNet= offNet;
		this.international=international;
		this.productIdValue = productIdValue;
		}

	/*public BaseProduct(Long productId, String productName, Map<ChannelType, Expression> constraints,
			Map<ChannelType, Map<ChargingType, List<ProductCharging>>> charging, ProductType productType,
			Map<ChannelType, String> channelTypeNameToSet, Integer reccuringTimes, State productStatus,
			Map<String , Long> productNotifictionMap, String connectionType,String productDescription,
			String recurringOrValidityValue, String recurringOrValidityUnit, Boolean isSplit) {
		this.productId = productId;
		this.productName = productName;
		this.constraints = constraints;
		this.charging = charging;
		this.productType = productType;
		this.channelTypeName = channelTypeNameToSet;
		this.renewalCount = reccuringTimes;
		this.state = productStatus;
		this.productNotifictionMap = productNotifictionMap;
		this.connectionType = connectionType;
		this.productDescription=productDescription;
		this.setRecurringOrValidityValue(recurringOrValidityValue);
		this.setRecurringOrValidityUnit(recurringOrValidityUnit);
		this.setIsSplit(isSplit);
		
	}
	*/
	@Override
	public Expression getConstraintForChannel(final ChannelType channel) {
		return constraints.get(channel);
	}

	@Override
	public List<ProductCharging> getProductCharging(final ChannelType channel, final ChargingType chargingType) {
		List<ProductCharging> chargingList = null;
        Map<ChargingType, List<ProductCharging>> map = this.charging.get(channel);
        if (map != null) {
               chargingList = map.get(chargingType);
               if(chargingType.equals(ChargingType.RS_DEPROVISIONING))
               {
                     chargingList.get(0).setExternalSystem(ExternalSystem.AIR);
               }
        }
        return chargingList;

	}

	@Override
	public Long getProductId() {
		return productId;
	}

	/**
	 * @param productIdToSet
	 *            the productId to set
	 */
	public void setProductId(final Long productIdToSet) {
		this.productId = productIdToSet;
	}

	/**
	 * @param constraintsToSet
	 *            the constraints to set
	 */
	public void setConstraints(final Map<ChannelType, Expression> constraintsToSet) {
		this.constraints = constraintsToSet;
	}

	/**
	 * @param chargingToSet
	 *            the charging to set
	 */
	public void setCharging(final Map<ChannelType, Map<ChargingType, List<ProductCharging>>> chargingToSet) {
		this.charging = chargingToSet;
	}

	@Override
	public List<FDPCommand> getProvisioningActions() {
		return this.provisioningActions;
	}

	/**
	 * @param provisioningActionsToSet
	 *            the provisioning actions to set.
	 */
	public void setProvisioningActions(final List<FDPCommand> provisioningActionsToSet) {
		this.provisioningActions = provisioningActionsToSet;
	}

	@Override
	public String getProductName() {
		return productName;
	}

	/**
	 * @param productNameToSet
	 *            the productName to set
	 */
	public void setProductName(final String productNameToSet) {
		this.productName = productNameToSet;
	}
	
	@Override
	public State getState() {
		return state;
	}

	/**
	 * @param state
	 *            the state to set
	 */
	public void setState(final State state) {
		this.state = state;
	}

	@Override
	public ProductType getProductType() {
		return productType;
	}

	/**
	 * @param productType
	 *            the productType to set
	 */
	public void setProductType(final ProductType productType) {
		this.productType = productType;
	}

	@Override
	public String getProductNameForChannel(ChannelType channel) {
		return channelTypeName == null ? null : channelTypeName.get(channel);
	}

	@Override
	public Integer getRenewalCount() {
		return renewalCount;
	}

	/**
	 * @param renewalCount
	 *            the renewalCount to set
	 */
	public void setRenewalCount(Integer renewalCount) {
		this.renewalCount = renewalCount;
	}

	public String getAdditionalInfo(ProductAdditionalInfoEnum additionalInfoKey) {
		String value = null;
		if (additionalInfoMap != null) {
			value = additionalInfoMap.get(additionalInfoKey);
		}
		return value;
	}

	public void setAdditionalInfo(ProductAdditionalInfoEnum additionalInfoKey, String value) {
		if (additionalInfoMap == null) {
			additionalInfoMap = new HashMap<ProductAdditionalInfoEnum, String>();
		}
		this.additionalInfoMap.put(additionalInfoKey, value);
	}

	@Override
	public Long getNotificationIdForChannel(ChannelType channelType,
			ChargingTypeNotifications chargingTypeNotifications) {
		String key = channelType.getName();
		/*if (chargingTypeNotifications == ChargingTypeNotifications.ZERO_CHARGING_BUY_PRODUCT) {
			key += "_" + 1;
		} else if (chargingTypeNotifications == ChargingTypeNotifications.POSITIVE_CHARGING_BUY_PRODUCT) {
			key += "_" + 2;
		} else if (chargingTypeNotifications == ChargingTypeNotifications.ZERO_CHARGING_DEPROVISIONING) {
			key += "_" + 3;
		} else if (chargingTypeNotifications == ChargingTypeNotifications.POSITIVE_CHARGING_DEPROVISIONING) {
			key += "_" + 4;
		} */
		key = key+FDPConstant.UNDERSCORE+chargingTypeNotifications.getChargingType();
		return productNotifictionMap.get(key);
	}
	

	@Override
	public String getProductDescription() {
		
		return productDescription;
	}

	public String getRecurringOrValidityValue() {
		return recurringOrValidityValue;
	}

	public void setRecurringOrValidityValue(String recurringOrValidityValue) {
		this.recurringOrValidityValue = recurringOrValidityValue;
	}

	public String getRecurringOrValidityUnit() {
		return recurringOrValidityUnit;
	}

	public void setRecurringOrValidityUnit(String recurringOrValidityUnit) {
		this.recurringOrValidityUnit = recurringOrValidityUnit;
	}

	public Boolean getIsSplit() {
		return isSplit;
	}

	public void setIsSplit(Boolean isSplit) {
		this.isSplit = isSplit;
	}


	public String getShareType() {
		return shareType;
	}


	public void setShareType(String shareType) {
		this.shareType = shareType;
	}

	public String getChargesTypes() {
		return chargesTypes;
	}


	public void setChargesTypes(String chargesTypes) {
		this.chargesTypes = chargesTypes;
	}


	public String getCharges() {
		return charges;
	}


	public void setCharges(String charges) {
		this.charges = charges;
	}


	public Boolean getIsFafType() {
		return isFafType;
	}


	public void setIsFafType(Boolean isFafType) {
		this.isFafType = isFafType;
	}


	public Integer getOnNet() {
		return onNet;
	}


	public void setOnNet(Integer onNet) {
		this.onNet = onNet;
	}


	public Integer getOffNet() {
		return offNet;
	}


	public void setOffNet(Integer offNet) {
		this.offNet = offNet;
	}


	public Integer getInternational() {
		return international;
	}


	public void setInternational(Integer international) {
		this.international = international;
	}


	public String getProductIdValue() {
		return productIdValue;
	}


	public void setProductIdValue(String productIdValue) {
		this.productIdValue = productIdValue;
	}


	public String getLoyaltyPoints() {
		return loyaltyPoints;
	}


	public void setLoyaltyPoints(String loyaltyPoints) {
		this.loyaltyPoints = loyaltyPoints;
	}


	public String getLoyaltyItemCode() {
		return loyaltyItemCode;
	}


	public void setLoyaltyItemCode(String loyaltyItemCode) {
		this.loyaltyItemCode = loyaltyItemCode;
	}
	
	
	
}
