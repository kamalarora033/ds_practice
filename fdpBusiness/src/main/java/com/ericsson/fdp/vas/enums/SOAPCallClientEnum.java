package com.ericsson.fdp.vas.enums;

import com.ericsson.fdp.business.adapter.impl.AbstractAdapterSOAPCallClient;
import com.ericsson.fdp.business.http.adapter.impl.SOAPCallClientAbility;
import com.ericsson.fdp.business.http.adapter.impl.SOAPCallClientESF;

/**
 * The Enum HttpCallClientEnum.
 */
public enum SOAPCallClientEnum {

	/** The Ability Type along with its CallClient class **/
	Ability("Ability", "Ability", new SOAPCallClientAbility()),
	ESF("ESF", "ESF", new SOAPCallClientESF());
	
	/** The system name. */
	private String systemName;

	/** The system value. */
	private String systemType;

	/** The adapter. */
	private AbstractAdapterSOAPCallClient adapter;

	/**
	 * Instantiates a new http call client enum.
	 * 
	 * @param systemName
	 *            the system name
	 * @param systemValue
	 *            the system value
	 * @param adapter
	 *            the adapter
	 */
	private SOAPCallClientEnum(String systemName, String systemValue, AbstractAdapterSOAPCallClient adapter) {
		this.systemName = systemName;
		this.systemType = systemValue;
		this.adapter = adapter;
	}

	/**
	 * Gets the object.
	 * 
	 * @param systemType
	 *            the system type
	 * @return the object
	 */
	public static AbstractAdapterSOAPCallClient getObject(final String systemType) {
		AbstractAdapterSOAPCallClient adapter = null;
		for (final SOAPCallClientEnum soapCallClientEnum : SOAPCallClientEnum.values()) {
			if (soapCallClientEnum.getSystemValue().equalsIgnoreCase(systemType)) {
				adapter = soapCallClientEnum.getAdapter();
				break;
			}
		}
		return adapter;
	}

	/**
	 * Gets the system name.
	 * 
	 * @return the system name
	 */
	public String getSystemName() {
		return systemName;
	}

	/**
	 * Sets the system name.
	 * 
	 * @param systemName
	 *            the new system name
	 */
	public void setSystemName(String systemName) {
		this.systemName = systemName;
	}

	/**
	 * Gets the system value.
	 * 
	 * @return the system value
	 */
	public String getSystemValue() {
		return systemType;
	}

	/**
	 * Sets the system value.
	 * 
	 * @param systemValue
	 *            the new system value
	 */
	public void setSystemValue(String systemValue) {
		this.systemType = systemValue;
	}

	/**
	 * Gets the adapter.
	 * 
	 * @return the adapter
	 */
	public AbstractAdapterSOAPCallClient getAdapter() {
		return adapter;
	}

	/**
	 * Sets the adapter.
	 * 
	 * @param adapter
	 *            the new adapter
	 */
	public void setAdapter(AbstractAdapterSOAPCallClient adapter) {
		this.adapter = adapter;
	}
}
