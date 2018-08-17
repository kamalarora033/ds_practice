package com.ericsson.fdp.vas.enums;

/**
 * The Enum VASExternalSystemType holds the lookup path vas external services.
 */
public enum VASExternalSystemType {
	
	/** The mcarbon. */
	MCARBON("MCARBON","java:app/fdpBusiness-1.0/MCarbonDynamicMenuExecutorServiceImpl"),
	
	/** The netxcell. */
	NETXCELL("NETXCELL","");
	
	/** The system type. */
	private String systemType;
	
	/** The lookup path. */
	private String lookupPath;
	
	/**
	 * Instantiates a new vAS external system type.
	 *
	 * @param systemType the system type
	 * @param lookUpPath the look up path
	 */
	private VASExternalSystemType(String systemType , String lookUpPath) {
		this.systemType = systemType;
		this.lookupPath = lookUpPath;
	}
	
	/**
	 * Gets the system type.
	 *
	 * @return the system type
	 */
	public String getSystemType() {
		return systemType;
	}
	
	/**
	 * Sets the system type.
	 *
	 * @param systemType the new system type
	 */
	public void setSystemType(String systemType) {
		this.systemType = systemType;
	}
	
	/**
	 * Gets the lookup path.
	 *
	 * @return the lookup path
	 */
	public String getLookupPath() {
		return lookupPath;
	}
	
	/**
	 * Sets the lookup path.
	 *
	 * @param lookupPath the new lookup path
	 */
	public void setLookupPath(String lookupPath) {
		this.lookupPath = lookupPath;
	}
	
	/**
	 * Gets the look up path by vas external system type.
	 *
	 * @param systemType the system type
	 * @return the look up path by vas external system type
	 */
	public static VASExternalSystemType getLookUpPathByVASExternalSystemType(String systemType) {
		VASExternalSystemType vasExternalSystemType = null;
		if(null != systemType) {
			for(final VASExternalSystemType externalSystemType :  VASExternalSystemType.values()) {
				if(systemType.equals(externalSystemType.name())) {
					vasExternalSystemType = externalSystemType;
					break;
				}
			}
		}
		return vasExternalSystemType;
	}
	
}
