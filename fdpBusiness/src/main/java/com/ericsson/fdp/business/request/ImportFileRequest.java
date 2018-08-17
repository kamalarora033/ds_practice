package com.ericsson.fdp.business.request;

import java.io.File;
import java.io.Serializable;

/**
 * The import file request interface.
 * 
 * @author Ericsson
 * 
 */
public interface ImportFileRequest extends Serializable{

	/**
	 * This method is used to get the file to import.
	 * 
	 * @return the file to import.
	 */
	File getFileToImport();

	/**
	 * The error file path.
	 * 
	 * @return the error file path.
	 */
	String getErrorFilePath();

}
