package com.ericsson.fdp.vas.enums;

import com.ericsson.fdp.business.adapter.impl.AbstractAdapterHttpCallClient;
import com.ericsson.fdp.business.http.adapter.impl.CallClientADC;
import com.ericsson.fdp.business.http.adapter.impl.EVDSCallClient;
import com.ericsson.fdp.business.http.adapter.impl.HttpCallClientAirCGWRS;
import com.ericsson.fdp.business.http.adapter.impl.HttpCallClientCMS;
import com.ericsson.fdp.business.http.adapter.impl.HttpCallClientFDPOffLine;
import com.ericsson.fdp.business.http.adapter.impl.HttpCallClientLoyalty;
import com.ericsson.fdp.business.http.adapter.impl.HttpCallClientMCLoan;
import com.ericsson.fdp.business.http.adapter.impl.HttpCallClientMCarbon;
import com.ericsson.fdp.business.http.adapter.impl.HttpCallClientManhattan;
import com.ericsson.fdp.business.http.adapter.impl.HttpCallClientSBBB;
import com.ericsson.fdp.business.http.adapter.impl.HttpsCallClientCIS;
import com.ericsson.fdp.business.http.adapter.impl.HttpsCallClientDMC;
import com.ericsson.fdp.business.http.adapter.impl.HttpsCallClientMobileMoney;

/**
 * The Enum HttpCallClientEnum.
 */
public enum HttpCallClientEnum {

	/** The air type. */
	AIR_TYPE("AIR", "AIR", new HttpCallClientAirCGWRS()),

	/** The cgw type. */
	CGW_TYPE("CGW", "CGW", new HttpCallClientAirCGWRS()),

	/** The rs type. */
	RS_TYPE("RS", "RS", new HttpCallClientAirCGWRS()),

	/** The mcarbon type. */
	MCARBON_TYPE("MCARBON", "MCARBON", new HttpCallClientMCarbon()),

	/** The manhattan type. */
	MANHATTAN_TYPE("MANHATTAN", "MANHATTAN", new HttpCallClientManhattan()),
	
	/** The CMS Type **/
	CMS("CMS","CMS",new HttpCallClientCMS()),
	
	/** The MCLoan Type **/
	MCLOAN("MCLoan","MCLoan",new HttpCallClientMCLoan()),
	
	/** The MCLoan Type **/
	FDPOFFLINE("FDPOFFLINE","FDPOFFLINE",new HttpCallClientFDPOffLine()),
	
	/** The Loyalty Type **/
	Loyalty("Loyalty","Loyalty",new HttpCallClientLoyalty()),
	
	/** The Mobile Money Type **/
	MM("MM","MM",new HttpsCallClientMobileMoney()),

	/** The EVDS Type along with its CallClient class **/
	EVDS("EVDS", "EVDS", new EVDSCallClient()),
	
	/** The CIS type for FAF international Number. **/
	CIS("CIS","CIS",new HttpsCallClientCIS()),
	
	DMC("DMC","DMC",new HttpsCallClientDMC()),
	
	/** The SBBB **/
	SBBB("SBBB", "SBBB", new HttpCallClientSBBB()),
	
	/** The ADC type. */
	ADC_TYPE("ADC", "ADC", new CallClientADC());
	
	/** The system name. */
	private String systemName;

	/** The system value. */
	private String systemType;

	/** The adapter. */
	private AbstractAdapterHttpCallClient adapter;

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
	private HttpCallClientEnum(String systemName, String systemValue, AbstractAdapterHttpCallClient adapter) {
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
	public static AbstractAdapterHttpCallClient getObject(final String systemType) {
		AbstractAdapterHttpCallClient adapter = null;
		for (final HttpCallClientEnum httpCallClientEnum : HttpCallClientEnum.values()) {
			if (httpCallClientEnum.getSystemValue().equalsIgnoreCase(systemType)) {
				adapter = httpCallClientEnum.getAdapter();
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
	public AbstractAdapterHttpCallClient getAdapter() {
		return adapter;
	}

	/**
	 * Sets the adapter.
	 * 
	 * @param adapter
	 *            the new adapter
	 */
	public void setAdapter(AbstractAdapterHttpCallClient adapter) {
		this.adapter = adapter;
	}
}
