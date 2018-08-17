package com.ericsson.fdp.business.sharedaccount.service;

import java.util.Map;

import javax.ejb.Remote;
import javax.naming.NamingException;

import com.ericsson.fdp.common.exception.FDPServiceException;
import com.ericsson.fdp.common.vo.FDPCircle;
import com.ericsson.fdp.core.command.param.CommandParam;

/**
 * The Interface UserAccountDetails.
 * 
 * @author Ericsson
 */
@Remote
public interface AccountDetailService {

	/**
	 * Run command.
	 * 
	 * @param subscriberNumber
	 *            the subscriber number
	 * @param commandDisplayName
	 *            the command display name
	 * @return the fDP command
	 * @throws FDPServiceException 
	 */
	Map<String, CommandParam> runCommand(Long subscriberNumber,
			String commandDisplayName) throws FDPServiceException;

	/**
	 * This method returns map of parameterNameToDisplay and
	 * parameterFullQualifiedName.
	 * 
	 * @param commandNameToDisplay
	 *            name of the command.
	 * 
	 * @return Map<String, String>
	 * */
	Map<String, String> getOutputFullParameterPath(String commandName);

	/**
	 * Gets the fDP circle from msisdn.
	 * 
	 * @param msisdnNumber
	 *            the msisdn number
	 * @return the fDP circle from msisdn
	 * @throws NamingException
	 */
	FDPCircle getFDPCircleFromMsisdn(Long msisdnNumber) throws NamingException;

	/**
	 * Gets the cS attribute value.
	 *
	 * @param msisdn the msisdn
	 * @param className the class name
	 * @param originalValue the original value
	 * @return the cS attribute value
	 */
	String getCSAttributeValue( String msisdn, String className, String originalValue);
}
