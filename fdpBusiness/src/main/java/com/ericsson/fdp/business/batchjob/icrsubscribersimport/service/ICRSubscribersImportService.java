package com.ericsson.fdp.business.batchjob.icrsubscribersimport.service;

import javax.ejb.Remote;

import com.ericsson.fdp.business.dto.icrsubscribersimport.ICRSubscribersCSVData;

/**
 * The Interface ICRSubscribersImportService.
 *
 * @author Ericsson
 */
@Remote
public interface ICRSubscribersImportService {

	/**
	 * Process ics subscriber data.
	 *
	 * @param icrSubscribersCSVData the icr subscribers csv data
	 * @return the iCR subscribers csv data
	 */
	ICRSubscribersCSVData processICSSubscriberData(final ICRSubscribersCSVData icrSubscribersCSVData);
}
