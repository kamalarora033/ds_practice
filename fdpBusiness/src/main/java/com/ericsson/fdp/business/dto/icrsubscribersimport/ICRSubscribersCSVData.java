package com.ericsson.fdp.business.dto.icrsubscribersimport;

import java.util.List;

import com.ericsson.fdp.dao.dto.BaseDTO;

public class ICRSubscribersCSVData extends BaseDTO {

	/**
	 *
	 */
	private static final long serialVersionUID = -2478450867007028580L;

	/** The headers. */
	private List<String> headers;

	/** The data list. */
	private List<List<String>> dataList;

	/**
	 * @return the headers
	 */
	public List<String> getHeaders() {
		return headers;
	}

	/**
	 * @param headers the headers to set
	 */
	public void setHeaders(final List<String> headers) {
		this.headers = headers;
	}

	/**
	 * @return the dataList
	 */
	public List<List<String>> getDataList() {
		return dataList;
	}

	/**
	 * @param dataList the dataList to set
	 */
	public void setDataList(final List<List<String>> dataList) {
		this.dataList = dataList;
	}
}
