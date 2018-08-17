package com.ericsson.fdp.business.enums;

/**
 * This enum defines the types of shared product.
 * 
 * @author Ericsson
 * 
 */
public enum SharedType {

	/**
	 * The enterprise type of shared product.
	 */
	ENTERPRISE("Enterprise"),
	/**
	 * The family type of shared product.
	 */
	FAMILY("Family");

	/** The shared type. */
	String sharedType;
	
	/**
	 * Instantiates a new shared type.
	 *
	 * @param sharedType the shared type
	 */
	private SharedType(String sharedType) {
		this.sharedType = sharedType;
	}
	
	/**
	 * Find by shared type.
	 *
	 * @param sharedType the shared type
	 * @return the shared type
	 */
	public static SharedType findBySharedType(String sharedType){
	    for(SharedType v : values()){
	        if( v.getSharedType().equals(sharedType)){
	            return v;
	        }
	    }
	    return null;
	}

	/**
	 * Gets the shared type.
	 *
	 * @return the shared type
	 */
	public String getSharedType() {
		return sharedType;
	}

	/**
	 * Sets the shared type.
	 *
	 * @param sharedType the new shared type
	 */
	public void setSharedType(String sharedType) {
		this.sharedType = sharedType;
	}
}
