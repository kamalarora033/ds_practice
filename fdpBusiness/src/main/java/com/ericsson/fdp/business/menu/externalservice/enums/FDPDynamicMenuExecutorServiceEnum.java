package com.ericsson.fdp.business.menu.externalservice.enums;

import com.ericsson.fdp.business.menu.externalservice.AbstractFDPDynamicMenuExecutorService;
import com.ericsson.fdp.business.menu.externalservice.impl.MCarbonDynamicMenuExecutorServiceImpl;
import com.ericsson.fdp.business.menu.externalservice.impl.ManhattanDynamicMenuExecutorServiceImpl;

/**
 * The Enum FDPDynamicMenuExecutorServiceEnum holds FDPDynamicMenuExecutorService implementation class objects.
 */
public enum FDPDynamicMenuExecutorServiceEnum {
	
	/** The mcarbon type. */
	MCARBON_TYPE("MCARBON" , "MCARBON" , new MCarbonDynamicMenuExecutorServiceImpl()),
	
	/** The manhattan type. */
	MANHATTAN_TYPE("MANHATTAN", "MANHATTAN" , new ManhattanDynamicMenuExecutorServiceImpl());
	
	/** The system name. */
	private String systemName;
	
	/** The system value. */
	private String systemValue;
	
	/** The fdp dynamic menu executor service. */
	private AbstractFDPDynamicMenuExecutorService fdpDynamicMenuExecutorService;
	
	/**
	 * Instantiates a new FDP dynamic menu executor service enum.
	 *
	 * @param systemName the system name
	 * @param systemValue the system value
	 * @param fdpDynamicMenuExecutorService the fdp dynamic menu executor service
	 */
	private FDPDynamicMenuExecutorServiceEnum(final String systemName , final String systemValue , final AbstractFDPDynamicMenuExecutorService fdpDynamicMenuExecutorService) {
		this.systemName = systemName;
		this.systemValue = systemValue;
		this.fdpDynamicMenuExecutorService = fdpDynamicMenuExecutorService;
	}

	/**
	 * Gets the system name.
	 *
	 * @return the systemName
	 */
	public String getSystemName() {
		return systemName;
	}

	/**
	 * Sets the system name.
	 *
	 * @param systemName the systemName to set
	 */
	public void setSystemName(String systemName) {
		this.systemName = systemName;
	}

	/**
	 * Gets the system value.
	 *
	 * @return the systemValue
	 */
	public String getSystemValue() {
		return systemValue;
	}

	/**
	 * Sets the system value.
	 *
	 * @param systemValue the systemValue to set
	 */
	public void setSystemValue(String systemValue) {
		this.systemValue = systemValue;
	}

	/**
	 * Gets the fdp dynamic menu executor service.
	 *
	 * @return the fdpDynamicMenuExecutorService
	 */
	public AbstractFDPDynamicMenuExecutorService getFdpDynamicMenuExecutorService() {
		return fdpDynamicMenuExecutorService;
	}

	/**
	 * Sets the fdp dynamic menu executor service.
	 *
	 * @param fdpDynamicMenuExecutorService the fdpDynamicMenuExecutorService to set
	 */
	public void setFdpDynamicMenuExecutorService(AbstractFDPDynamicMenuExecutorService fdpDynamicMenuExecutorService) {
		this.fdpDynamicMenuExecutorService = fdpDynamicMenuExecutorService;
	}
	
	
	/**
	 * Gets the object.
	 *
	 * @param type the type
	 * @return the object
	 */
	public static AbstractFDPDynamicMenuExecutorService getObject(final String type) {
		AbstractFDPDynamicMenuExecutorService fdpDynamicMenuExecutorService = null;
		for(final FDPDynamicMenuExecutorServiceEnum dynamicMenuExecutorServiceEnum : FDPDynamicMenuExecutorServiceEnum.values()) {
			if(dynamicMenuExecutorServiceEnum.getSystemName().equalsIgnoreCase(type)) {
				fdpDynamicMenuExecutorService = dynamicMenuExecutorServiceEnum.getFdpDynamicMenuExecutorService();
				break;
			}
		}
		
		
		return fdpDynamicMenuExecutorService;
	}

}
