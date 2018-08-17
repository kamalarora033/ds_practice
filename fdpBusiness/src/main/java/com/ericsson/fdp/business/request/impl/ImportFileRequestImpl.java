package com.ericsson.fdp.business.request.impl;

import java.io.File;

import com.ericsson.fdp.business.request.ImportFileRequest;

/**
 * This class implements the import file request.
 * 
 * @author Ericsson
 * 
 */
public class ImportFileRequestImpl implements ImportFileRequest {

	/**
	 * 
	 */
	private static final long serialVersionUID = -7473778302778710288L;

	/**
	 * The file to import.
	 */
	private File fileToImport;

	/**
	 * The error file path.
	 */
	private String errorFilePath;

	/**
	 * The constructor for import file.
	 * 
	 * @param fileToImport
	 *            the file to import.
	 */
	public ImportFileRequestImpl(final File fileToImport, final String errorFilePath) {
		this.fileToImport = fileToImport;
		this.errorFilePath = errorFilePath;
	}

	@Override
	public File getFileToImport() {
		return fileToImport;
	}

	public String getErrorFilePath() {
		return errorFilePath;
	}
}
