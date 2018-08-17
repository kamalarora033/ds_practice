package com.ericsson.fdp.business.tariffenquiry.configimport;

import javax.ejb.Remote;

import com.ericsson.fdp.business.request.ImportTariffAttributeFileRequest;
import com.ericsson.fdp.common.enums.TariffEnquiryAttributeKeyType;
import com.ericsson.fdp.common.request.ImportTariffAttributeFileResponse;
import com.ericsson.fdp.core.exception.ExecutionFailedException;

/**
 * The import tariff enquiry attribute file interface.
 * 
 * @author Ericsson
 * 
 */
@Remote
public interface ImportTariffEnquiryAttributeFile {

	/**
	 * This method is used to import the provided file.
	 * 
	 * @param importRequest
	 *            the import request.
	 * @return the file response.
	 * @throws ExecutionFailedException
	 *             Exception, if any.
	 */
	public boolean importProvidedFile(ImportTariffAttributeFileRequest importRequest,
			TariffEnquiryAttributeKeyType attributeKeyType) throws ExecutionFailedException;

	/**
	 * This method is used for testing.
	 * 
	 * @throws ExecutionFailedException
	 *             Exception, if any.
	 */
	public void importFile() throws ExecutionFailedException;

	/**
	 * This method is used to update the database as per the configuration.
	 * 
	 * @throws ExecutionFailedException
	 *             Exception in execution.
	 */
	public boolean updateDatabaseValues(ImportTariffAttributeFileRequest importRequest) throws ExecutionFailedException;
	
	/**
	 * This method is used to import the provided JSon file.
	 * @throws ExecutionFailedException
	 */
	public boolean importJsonFile(ImportTariffAttributeFileRequest importRequest,
			TariffEnquiryAttributeKeyType attributeKeyType) throws ExecutionFailedException;
	
	/**
	 * This method will be the entry point for all the operation from Invocation Point.
	 * @param importRequest
	 * @return
	 */
	public ImportTariffAttributeFileResponse execute(ImportTariffAttributeFileRequest importRequest) throws ExecutionFailedException;
	
}
