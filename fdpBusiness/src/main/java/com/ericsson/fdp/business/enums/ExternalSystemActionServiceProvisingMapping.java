package com.ericsson.fdp.business.enums;

import com.ericsson.fdp.dao.enums.FDPServiceProvSubType;

/**
 * The Enum IVRActionServiceProvisingMapping.
 */
public enum ExternalSystemActionServiceProvisingMapping {
	
	/** The buy. */
	BUY("PBY",FDPServiceProvSubType.PRODUCT_BUY),
	
	BUY_RECURRING("Buy Product - Recurring",FDPServiceProvSubType.PRODUCT_BUY_RECURRING),

	BUY_SPLIT("Buy Product - Split", FDPServiceProvSubType.PRODUCT_BUY_SPLIT),

   /** The y produc t_ deactivatio n_ rs. */
    PRODUCT_DEACTIVATION_RS("PDCT",FDPServiceProvSubType.RS_DEPROVISION_PRODUCT),

	/** The product deactivation pam. */
	PRODUCT_DEACTIVATION_PAM("PDCT",FDPServiceProvSubType.PAM_DEPROVISION_PRODUCT);

	
	/** The ivr action. */
	private String ivrAction;

	/** The service provtype. */
	private FDPServiceProvSubType serviceProvtype ;
	
	/**
	 * Instantiates a new iVR action service provising mapping.
	 *
	 * @param ivrAction the ivr action
	 * @param serviceProvtype the service provtype
	 */
	private ExternalSystemActionServiceProvisingMapping(String ivrAction,FDPServiceProvSubType serviceProvtype){
		this.ivrAction = ivrAction;
		this.serviceProvtype = serviceProvtype;
	}

	/**
	 * Gets the ivr action.
	 *
	 * @return the ivr action
	 */
	public String getIvrAction() {
		return ivrAction;
	}

	/**
	 * Gets the service provtype.
	 *
	 * @return the service provtype
	 */
	public FDPServiceProvSubType getServiceProvtype() {
		return serviceProvtype;
	}


	/**
	 * Gets the enum.
	 *
	 * @param ivrAction the ivr action
	 * @return the enum
	 */
	public static FDPServiceProvSubType getFDPServiceProvSubType(final String ivrAction) {
		FDPServiceProvSubType serviceType = FDPServiceProvSubType.PRODUCT_BUY;
		if (ivrAction != null && !ivrAction.isEmpty()) {
			for (final ExternalSystemActionServiceProvisingMapping subType : values()) {
				if (subType.getIvrAction().equalsIgnoreCase(ivrAction)) {
					serviceType = subType.getServiceProvtype();
					break;
				}
			}
		}
		return serviceType;
	}
	
}
