package com.ericsson.fdp.business.enums.ivr;

import com.ericsson.fdp.dao.enums.FDPServiceProvSubType;

public enum FulfillmentActionServiceProvMappingEnum {

	/** The product buy. */
	PRODUCT_BUY(FDPServiceProvSubType.PRODUCT_BUY, FulfillmentActionTypes.PRODUCT_BUY),
	/** The balance enquiry. */
	BALANCE_ENQUIRY(FDPServiceProvSubType.BALANCE_ENQUIRY, FulfillmentActionTypes.BALANCE_CHECK),

	TARIFF_ENQUIRY(FDPServiceProvSubType.TARIFF_ENQUIRY, FulfillmentActionTypes.TARIFF_ENQUIRY),

	/** The rs deprovision product. */
	RS_DEPROVISION_PRODUCT(FDPServiceProvSubType.RS_DEPROVISION_PRODUCT, FulfillmentActionTypes.RS_DEPROVISION_PRODUCT),
	
	PRODUCT_BUY_RECURRING(FDPServiceProvSubType.PRODUCT_BUY_RECURRING, FulfillmentActionTypes.PRODUCT_BUY),

	PRODUCT_BUY_SPLIT(FDPServiceProvSubType.PRODUCT_BUY_SPLIT,FulfillmentActionTypes.PRODUCT_BUY),
	
	FAF_ADD(FDPServiceProvSubType.FAF_ADD,FulfillmentActionTypes.FAF),
	
	FAF_MODIFY(FDPServiceProvSubType.FAF_MODIFY,FulfillmentActionTypes.FAF),
	
	FAF_DELETE(FDPServiceProvSubType.FAF_DELETE,FulfillmentActionTypes.FAF),
	
	FAF_VIEW(FDPServiceProvSubType.FAF_VIEW,FulfillmentActionTypes.FAF),
	
	FAF_REGISTER(FDPServiceProvSubType.FAF_REGISTER,FulfillmentActionTypes.FAF),
	
	FAF_UNREGISTER(FDPServiceProvSubType.FAF_UNREGISTER,FulfillmentActionTypes.FAF),
	
	ME2U_PRODUCT_BUY(FDPServiceProvSubType.Me2U, FulfillmentActionTypes.PRODUCT_BUY),
	
	NUMBER_RESERVATION(FDPServiceProvSubType.NUMBER_RESERVATION, FulfillmentActionTypes.PRODUCT_BUY);
					

	private FDPServiceProvSubType fdpServiceProvSubType;

	private FulfillmentActionTypes actionTypes;

	private FulfillmentActionServiceProvMappingEnum(final FDPServiceProvSubType fdpServiceProvSubType,
			final FulfillmentActionTypes actionTypes) {
		this.fdpServiceProvSubType = fdpServiceProvSubType;
		this.actionTypes = actionTypes;
	}

	public FDPServiceProvSubType getFdpServiceProvSubType() {
		return fdpServiceProvSubType;
	}

	public void setFdpServiceProvSubType(FDPServiceProvSubType fdpServiceProvSubType) {
		this.fdpServiceProvSubType = fdpServiceProvSubType;
	}

	public FulfillmentActionTypes getActionTypes() {
		return actionTypes;
	}

	public void setActionTypes(FulfillmentActionTypes actionTypes) {
		this.actionTypes = actionTypes;
	}

	/**
	 * This method will provide the action type mapping.
	 * 
	 * @param fdpServiceProvSubType
	 * @return
	 */
	public static FulfillmentActionTypes getFulfillmentActionTypes(final FDPServiceProvSubType fdpServiceProvSubType) {
		FulfillmentActionTypes actionTypes = null;
		for (final FulfillmentActionServiceProvMappingEnum actionServiceProvMappingEnum : FulfillmentActionServiceProvMappingEnum
				.values()) {
			if (actionServiceProvMappingEnum.getFdpServiceProvSubType().equals(fdpServiceProvSubType)) {
				actionTypes = actionServiceProvMappingEnum.getActionTypes();
				break;
			}
		}
		return actionTypes;
	}
}
