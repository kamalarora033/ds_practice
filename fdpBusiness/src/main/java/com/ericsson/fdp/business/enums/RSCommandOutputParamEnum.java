package com.ericsson.fdp.business.enums;

import com.ericsson.fdp.common.enums.Primitives;

/**
 * This enum would contains the output parameters name and their primitive types
 * 
 * @author GUR36857
 *
 */
public enum RSCommandOutputParamEnum {

	SERVICENAME("serviceName", Primitives.STRING), SERVICEDESCRIPTION("serviceDescription",
			Primitives.STRING), SUBSCRIPTIONDATE("subscriptionDate", Primitives.DATETIME), SHORTCODE("shortCode",
					Primitives.STRING), CHARGEMODE("chargeMode", Primitives.STRING), PAYMENTCHANNEL("paymentChannel",
							Primitives.STRING), CHARGEAMOUNT("chargeAmount",
									Primitives.LONG), RENEWALDATE("renewalDate", Primitives.DATETIME);

	/** The Output Param Name */
	private String paramName;

	/** The Primitive type */
	private Primitives primitive;

	private RSCommandOutputParamEnum(String paramName, Primitives primitive) {
		this.paramName = paramName;
		this.primitive = primitive;
	}

	/**
	 * @return the paramName
	 */
	public String getParamName() {
		return paramName;
	}

	/**
	 * @return the primitive
	 */
	public Primitives getPrimitive() {
		return primitive;
	}

}
