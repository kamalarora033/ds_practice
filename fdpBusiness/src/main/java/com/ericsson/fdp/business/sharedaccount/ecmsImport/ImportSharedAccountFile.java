package com.ericsson.fdp.business.sharedaccount.ecmsImport;

import javax.ejb.Remote;

import com.ericsson.fdp.business.request.ImportFileRequest;
import com.ericsson.fdp.business.request.ImportFileResponse;
import com.ericsson.fdp.core.exception.ExecutionFailedException;

/**
 * The import shared account file interface.
 * 
 * @author Ericsson
 * 
 */
@Remote
public interface ImportSharedAccountFile {

	/**
	 * This method is used to import the provided file.
	 * 
	 * @param importRequest
	 *            the import request.
	 * @return the file response.
	 * @throws ExecutionFailedException
	 *             Exception, if any.
	 */
	ImportFileResponse importProvidedFile(ImportFileRequest importRequest) throws ExecutionFailedException;

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
	public void updateDatabaseValues() throws ExecutionFailedException;

}
