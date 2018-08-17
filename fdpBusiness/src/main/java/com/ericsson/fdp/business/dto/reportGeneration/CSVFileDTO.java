package com.ericsson.fdp.business.dto.reportGeneration;

import java.io.File;
import java.util.List;

import com.ericsson.fdp.dao.dto.BaseDTO;

/**
 * The Class CSVFileDTO.
 */
public class CSVFileDTO extends BaseDTO {

	/** The Constant serialVersionUID. */
	private static final long serialVersionUID = 8214518122609709776L;

	/** The file. */
	private File file;

	/** The expected headers. */
	private List<String> expectedHeaders;

	public CSVFileDTO() {
		
	}
	public CSVFileDTO(final File file, final List<String> expectedHeaders) {
		this.file = file;
		this.expectedHeaders = expectedHeaders;
	}
	/**
	 * Gets the file.
	 * 
	 * @return the file
	 */
	public File getFile() {
		return file;
	}

	/**
	 * Sets the file.
	 * 
	 * @param file
	 *            the new file
	 */
	public void setFile(final File file) {
		this.file = file;
	}

	/**
	 * Gets the expected headers.
	 * 
	 * @return the expected headers
	 */
	public List<String> getExpectedHeaders() {
		return expectedHeaders;
	}

	/**
	 * Sets the expected headers.
	 * 
	 * @param expectedHeaders
	 *            the new expected headers
	 */
	public void setExpectedHeaders(final List<String> expectedHeaders) {
		this.expectedHeaders = expectedHeaders;
	}

}
