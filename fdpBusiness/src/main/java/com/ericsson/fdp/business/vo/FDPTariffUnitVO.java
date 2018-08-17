package com.ericsson.fdp.business.vo;

import java.io.IOException;
import java.io.StringReader;
import java.util.Map;

import com.ericsson.fdp.FDPCacheable;
import com.ericsson.fdp.business.convertor.displayConvertor.UnitDisplayFormat;

import freemarker.template.Configuration;
import freemarker.template.Template;

/**
 * This class is used to store the notification information of a product.
 * 
 * @author Ericsson
 * 
 */
public class FDPTariffUnitVO implements FDPCacheable {
	
	/** The Constant serialVersionUID. */
	private static final long serialVersionUID = -3511405404797255479L;

	/**
	 * The parameters to replace in the template.
	 */
	private Map<String, UnitDisplayFormat> parametersToReplace;

	/**
	 * The message to be sent as notification.
	 */
	// private Template message;

	private final String name;

	/** The text. */
	private final String text;

	/**
	 * The constructor for notification dto.
	 * 
	 * @param notificationId
	 *            the notification id
	 * @param parametersToReplaceToSet
	 *            The parameters to replace value.
	 * @param name
	 *            the name
	 * @param text
	 *            the text
	 */
	public FDPTariffUnitVO(final Map<String, UnitDisplayFormat> parametersToReplaceToSet,
			String name, String text) {
		this.parametersToReplace = parametersToReplaceToSet;
		this.name = name;
		this.text = text;
	}

	/**
	 * Gets the parameters to replace.
	 * 
	 * @return the parametersToReplace
	 */
	public Map<String, UnitDisplayFormat> getParametersToReplace() {
		return parametersToReplace;
	}

	/**
	 * Sets the parameters to replace.
	 * 
	 * @param parametersToReplaceToSet
	 *            the parametersToReplace to set
	 */
	public void setParametersToReplace(final Map<String, UnitDisplayFormat> parametersToReplaceToSet) {
		this.parametersToReplace = parametersToReplaceToSet;
	}

	/**
	 * Gets the message.
	 * 
	 * @return the message
	 * @throws IOException
	 *             Exception if any.
	 */
	public Template getMessage() throws IOException {
		return new Template(name, new StringReader(text), new Configuration());
	}

	@Override
	public String toString() {
		return " notification message " + name + text + " notification parameters :- " + parametersToReplace;
	}

	/**
	 * Gets the name.
	 * 
	 * @return the name
	 */
	public String getName() {
		return name;
	}

}
