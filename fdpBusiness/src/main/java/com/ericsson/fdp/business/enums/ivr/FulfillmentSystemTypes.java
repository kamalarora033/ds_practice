package com.ericsson.fdp.business.enums.ivr;

import com.ericsson.fdp.common.constants.FDPConstant;
import com.ericsson.fdp.common.enums.ExternalSystem;

public enum FulfillmentSystemTypes {

	/** The FDP system. */
	//commented by EHLNOPU
	//FDP("FDP", FDPConstant.FDP),
	
	/** The FDP system. */
	FDP("FDP", "FDP"),

	/** The AIR system. */
	AIR("AIR", ExternalSystem.AIR.name()),

	/** The CGW system. */
	CGW("CGW", ExternalSystem.CGW.name()),

	/** The RS system */
	RS("RS", ExternalSystem.RS.name()),
	
	/** The CIS system. */
	CIS("CIS", FDPConstant.CIS),
	
	MM("MM",ExternalSystem.MM.name()),
	
	/** The EMA system. */
	EMA("EMA",ExternalSystem.EMA.name()),
	
	/** The SBB system **/
	SBB("SBBB",ExternalSystem.SBBB.name());

	/** The value. */
	private String value;

	/** The external System */
	private String externalSystem;

	/**
	 * Instantiates a new iVR system type.
	 * 
	 * @param value
	 *            the value
	 */
	private FulfillmentSystemTypes(final String value, final String externalSystem) {
		this.value = value;
		this.externalSystem = externalSystem;
	}

	/**
	 * Gets the value.
	 * 
	 * @return the value
	 */
	public String getValue() {
		return value;
	}

	public String getExternalSystem() {
		return externalSystem;
	}

	/**
	 * This method will provide the fulfillment external system type.
	 * 
	 * @param externalSystemType
	 * @return
	 */
	public static FulfillmentSystemTypes getFulfillmentSystemTypes(final String externalSystemType) {
		FulfillmentSystemTypes fulfillmentSystemTypes = null;
		for (final FulfillmentSystemTypes types : FulfillmentSystemTypes.values()) {
			if (types.getExternalSystem().equalsIgnoreCase(externalSystemType)) {
				fulfillmentSystemTypes = types;
				break;
			}
		}
		return fulfillmentSystemTypes;
	}

	/**
	 * This method provide the FulfillmentSystemTypes for value.
	 * 
	 * @param value
	 * @return
	 */
	public static FulfillmentSystemTypes getFulfillmentSystemForValue(final String value) {
		FulfillmentSystemTypes fulfillmentSystemTypes = null;
		for (final FulfillmentSystemTypes types : FulfillmentSystemTypes.values()) {
			if (types.getValue().equalsIgnoreCase(value)) {
				fulfillmentSystemTypes = types;
				break;
			}
		}
		return fulfillmentSystemTypes;
	}
}
