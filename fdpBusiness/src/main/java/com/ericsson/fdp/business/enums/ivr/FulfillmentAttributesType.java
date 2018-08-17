package com.ericsson.fdp.business.enums.ivr;

import com.ericsson.fdp.common.enums.TariffEnquiryOption;

public enum FulfillmentAttributesType {

	DA("DA",TariffEnquiryOption.DA),

	PSO("PSO",TariffEnquiryOption.PSO),

	COMMUNITY("Community",TariffEnquiryOption.COMMUNITY_ID),

	OFFERS("Offers",TariffEnquiryOption.OFFER_ID);

	/** The value **/
	private String value;

	/** The TariffEnquiryOption **/
	private TariffEnquiryOption tariffEnquiryOption;
	/**
	 * Instantiates a new iVR attributes.
	 * 
	 * @param value
	 *            the value
	 */
	private FulfillmentAttributesType(final String value, final TariffEnquiryOption tariffEnquiryOption) {
		this.value = value;
		this.tariffEnquiryOption = tariffEnquiryOption;
	}

	/**
	 * Gets the value.
	 * 
	 * @return the value
	 */
	public String getValue() {
		return value;
	}

	/**
	 * This method will provide the FulfillmentAttributesType based on attribute
	 * from request.
	 * 
	 * @param tariffAttributeType
	 * @return
	 */
	public static FulfillmentAttributesType getFulfillmentAttributesType(final String tariffAttributeType) {
		FulfillmentAttributesType fulfillmentAttributesType = null;
		for (final FulfillmentAttributesType attributesType : FulfillmentAttributesType.values()) {
			if (attributesType.getTariffEnquiryOption().getOptionId().equalsIgnoreCase(tariffAttributeType)) {
				fulfillmentAttributesType = attributesType;
				break;
			}
		}
		return fulfillmentAttributesType;
	}

	public TariffEnquiryOption getTariffEnquiryOption() {
		return tariffEnquiryOption;
	}
	
}
