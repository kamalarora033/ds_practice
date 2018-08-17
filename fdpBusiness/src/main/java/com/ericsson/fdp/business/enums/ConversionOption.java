package com.ericsson.fdp.business.enums;

public enum ConversionOption {

	/**
	 * The No Conversion Option.
	 */
	NO_CONVERSION(1), 
	
	/**
	 * The DO Conversion Option.
	 */
	DO_CONVERSION(0);

	private int pos;

	private ConversionOption(int pos) {
		this.setPos(pos);
	}

	/**
	 * This method will provide the ConversionOption.
	 * 
	 * @param conversionType
	 * @return
	 */
	public static ConversionOption getConversionOptionValue(final int conversionType) {
		ConversionOption cOption = ConversionOption.DO_CONVERSION;
		for (final ConversionOption options : ConversionOption.values()) {
			if (options.getPos() == conversionType) {
				cOption = options;
				break;
			}
		}
		return cOption;
	}

	/**
	 * @return the pos
	 */
	public int getPos() {
		return pos;
	}

	/**
	 * @param pos
	 *            the pos to set
	 */
	public void setPos(int pos) {
		this.pos = pos;
	}

	/**
	 * This method will provide the ConversionOption against the configuration.
	 * 
	 * @param configurationValue
	 * @return
	 */
	public static ConversionOption getConversionOptionValueForConfiguration(final String configurationValue) {
		ConversionOption cOption = ConversionOption.NO_CONVERSION;
		if (null != configurationValue
				&& ("true".equalsIgnoreCase(configurationValue) || "yes".equalsIgnoreCase(configurationValue))) {
			cOption = ConversionOption.DO_CONVERSION;
		}
		return cOption;
	}
}
