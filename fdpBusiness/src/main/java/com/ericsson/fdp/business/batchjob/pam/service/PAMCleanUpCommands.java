package com.ericsson.fdp.business.batchjob.pam.service;

import java.util.List;
import java.util.Map;

import javax.ejb.Remote;

import com.ericsson.fdp.business.dto.pam.PAMCleanUpResponse;
import com.ericsson.fdp.business.dto.pam.PAMRecord;
import com.ericsson.fdp.common.vo.FDPCircle;
import com.ericsson.fdp.core.exception.ExecutionFailedException;

/**
 * The Interface PAMCleanUpCommands.
 * 
 * @author Ericsson
 */
@Remote
public interface PAMCleanUpCommands {

	/**
	 * Process prev day records.
	 * 
	 * @param prevDayRecordList
	 *            the prev day record list
	 * @param offerPAMMap
	 *            the offer pam map
	 * @param recordsFailed
	 *            the records failed
	 * @param fdpCircle
	 *            the fdp circle
	 * @return the pAM clean up response
	 * @throws ExecutionFailedException
	 */
	PAMCleanUpResponse processValidRecords(final List<PAMRecord> prevDayRecordList,
			final Map<String, String> offerPAMMap, final FDPCircle fdpCircle) throws ExecutionFailedException;

}
