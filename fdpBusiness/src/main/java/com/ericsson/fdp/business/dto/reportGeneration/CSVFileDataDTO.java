package com.ericsson.fdp.business.dto.reportGeneration;

import java.util.List;

import com.ericsson.fdp.dao.dto.BaseDTO;

/**
 * The Class CSVFileDataDTO.
 */
public class CSVFileDataDTO extends BaseDTO {

	/** The Constant serialVersionUID. */
	private static final long serialVersionUID = -8753508059073246798L;

	/** The file name. */
	private String fileName;
	
	/** The headers. */
	private List<String> headers;

	/** The data list. */
	private List<List<String>> dataList;

	/** The no of fields. */
	private Integer noOfFields;

	/** The errors. */
	private List<String> errors;
	
	/** The Comma Seprated Headers */
	private String commaSepHeaders;

	/**
	 * Gets the file name.
	 *
	 * @return the file name
	 */
	public String getFileName() {
		return fileName;
	}

	/**
	 * Sets the file name.
	 *
	 * @param fileName the new file name
	 */
	public void setFileName(final String fileName) {
		this.fileName = fileName;
	}

	/**
	 * Gets the headers.
	 *
	 * @return the headers
	 */
	public List<String> getHeaders() {
		return headers;
	}

	/**
	 * Sets the headers.
	 *
	 * @param headers the new headers
	 */
	public void setHeaders(final List<String> headers) {
		this.headers = headers;
	}

	/**
	 * Gets the data list.
	 *
	 * @return the data list
	 */
	public List<List<String>> getDataList() {
		return dataList;
	}

	/**
	 * Sets the data list.
	 *
	 * @param dataList the new data list
	 */
	public void setDataList(final List<List<String>> dataList) {
		this.dataList = dataList;
	}

	/**
	 * Gets the no of fields.
	 *
	 * @return the no of fields
	 */
	public Integer getNoOfFields() {
		return noOfFields;
	}

	/**
	 * Sets the no of fields.
	 *
	 * @param noOfFields the new no of fields
	 */
	public void setNoOfFields(final Integer noOfFields) {
		this.noOfFields = noOfFields;
	}

	/**
	 * Gets the errors.
	 *
	 * @return the errors
	 */
	public List<String> getErrors() {
		return errors;
	}

	/**
	 * Sets the errors.
	 *
	 * @param errors the new errors
	 */
	public void setErrors(final List<String> errors) {
		this.errors = errors;
	}

	/**
	 * @return the commaSepHeaders
	 */
	public String getCommaSepHeaders() {
		return commaSepHeaders;
	}

	/**
	 * @param commaSepHeaders the commaSepHeaders to set
	 */
	public void setCommaSepHeaders(String commaSepHeaders) {
		this.commaSepHeaders = commaSepHeaders;
	}

	
}
