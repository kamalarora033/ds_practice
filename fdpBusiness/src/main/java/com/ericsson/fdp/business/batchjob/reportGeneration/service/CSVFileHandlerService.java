package com.ericsson.fdp.business.batchjob.reportGeneration.service;

import javax.ejb.Remote;

import com.ericsson.fdp.business.dto.reportGeneration.CSVFileDTO;
import com.ericsson.fdp.business.dto.reportGeneration.CSVFileDataDTO;

/**
 * The Interface CSVFileHandlerService.
 */
@Remote
public interface CSVFileHandlerService {

	/**
	 * This method prepares data rows from csvFileDTO.
	 * 
	 * @param csvFileDTO
	 *            CSV file DTO contains file object and expected headers.
	 * @return CSV file data DTO - if any error in parsing file errors list will
	 *         be non empty otherwise data list will have all the rows found in
	 *         CSV file.
	 */
	CSVFileDataDTO importCSVFile(CSVFileDTO csvFileDTO);
	
	/**
	 * This method export the CSV file
	 * @param csvFileDataDTO
	 */
	void exportCSVFile(CSVFileDataDTO csvFileDataDTO );
	

}
