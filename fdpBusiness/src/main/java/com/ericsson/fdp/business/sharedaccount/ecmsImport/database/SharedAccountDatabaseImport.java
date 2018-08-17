package com.ericsson.fdp.business.sharedaccount.ecmsImport.database;

import java.util.List;
import java.util.Map;

import com.ericsson.fdp.common.enums.Status;
import com.ericsson.fdp.core.exception.EvaluationFailedException;
import com.ericsson.fdp.core.exception.ExecutionFailedException;

/**
 * This interface defines the methods to be used for shared account database
 * import.
 * 
 * @author Ericsson
 * 
 * @param <T>
 *            the key to be used.
 * @param <K>
 *            the value to be used.
 */
public interface SharedAccountDatabaseImport<T, K> {

	/**
	 * This method is used to update the database as per the configuration.
	 * 
	 * @return true, if database was updated, false otherwise.
	 * @param deleteRows
	 *            true, if all the rows from the database are to be deleted.
	 * @throws ExecutionFailedException
	 *             Exception, if any.
	 */
	boolean updateDatabaseAsPerConfiguration(boolean deleteRows) throws ExecutionFailedException;

	/**
	 * This method is used to validate the provided value.
	 * 
	 * @param valueToValidate
	 *            the map containing the value to validate.
	 * @return the map containing the status and the value corresponding the
	 *         status.
	 * @throws EvaluationFailedException
	 *             Exception, if any.
	 */
	Map<Status, Object> validateValue(Map<T, K> valueToValidate) throws EvaluationFailedException;

	/**
	 * This method is used to import values.
	 * 
	 * @param valuesToImport
	 *            the list of values to import.
	 * @return the list of failed values.
	 * @throws EvaluationFailedException
	 *             Exception, if any.
	 */
	List<Map<T, K>> importValues(List<Map<T, K>> valuesToImport) throws EvaluationFailedException;

}
