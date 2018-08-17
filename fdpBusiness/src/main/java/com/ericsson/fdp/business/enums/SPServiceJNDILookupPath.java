package com.ericsson.fdp.business.enums;

/**
 * The Enum SPServiceJNDILookupPath.
 */
public enum SPServiceJNDILookupPath {

	/** The threeg activation. */
	THREEG_ACTIVATION("THREE_G", "java:global/fdpBusiness-ear/fdpBusiness-1.0/FDPDefaultEMAServiceImpl"),

	/** The twog activation. */
	TWOG_ACTIVATION("TWO_G", "java:global/fdpBusiness-ear/fdpBusiness-1.0/FDPDefaultEMAServiceImpl"),

	BLACKBERRY_SERVICE_ACTIVATION("BLACKBERRY_SERVICE_ACTIVATION","java:global/fdpBusiness-ear/fdpBusiness-1.0/FDPLogEMAServiceImpl"),

	
	/** The tariff enquiry. */
	TARIFF_ENQUIRY("TARIFF_ENQUIRY", "java:global/fdpBusiness-ear/fdpBusiness-1.0/TariffEnquiryServiceImpl"),
	
	/** The CHECK BALANCE SERVICE. */
	CHECK_BALANCE_SERVICES("CHECK_BALANCE_SERVICES","java:global/fdpBusiness-ear/fdpBusiness-1.0/CheckBalanceServiceImpl"),
	
	COMVIVA_MENU_REDIRECT("COMVIVA_MENU_REDIRECT","java:global/fdpBusiness-ear/fdpBusiness-1.0/ComvivaMenuRedirectImpl");

	
	/** The service. */
	private String service;

	/** The jndi lookup path. */
	private String jndiLookupPath;

	/**
	 * Instantiates a new sP service jndi lookup path.
	 *
	 * @param service the service
	 * @param jndiLookupPath the jndi lookup path
	 */
	private SPServiceJNDILookupPath(final String service, final String jndiLookupPath) {
		this.service = service;
		this.jndiLookupPath = jndiLookupPath;
	}

	/**
	 * Gets the enum from service.
	 *
	 * @param service the service
	 * @return the enum from service
	 */
	public static SPServiceJNDILookupPath getEnumFromService(final String service) {
		SPServiceJNDILookupPath returnEnum = null;
		for (SPServiceJNDILookupPath currentEnum : SPServiceJNDILookupPath.values()) {
			if (currentEnum.getService().equals(service)) {
				returnEnum = currentEnum;
				break;
			}
		}
		return returnEnum;
	}

	/**
	 * Gets the service.
	 *
	 * @return the service
	 */
	public String getService() {
		return service;
	}

	/**
	 * Sets the service.
	 *
	 * @param service the service to set
	 */
	public void setService(final String service) {
		this.service = service;
	}

	/**
	 * Gets the jndi lookup path.
	 *
	 * @return the jndiLookupPath
	 */
	public String getJndiLookupPath() {
		return jndiLookupPath;
	}

	/**
	 * Sets the jndi lookup path.
	 *
	 * @param jndiLookupPath the jndiLookupPath to set
	 */
	public void setJndiLookupPath(final String jndiLookupPath) {
		this.jndiLookupPath = jndiLookupPath;
	}
}
