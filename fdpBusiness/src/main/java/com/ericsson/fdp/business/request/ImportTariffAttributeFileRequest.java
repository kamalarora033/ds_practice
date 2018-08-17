package com.ericsson.fdp.business.request;

import java.io.File;
import java.io.Serializable;

import com.ericsson.fdp.common.vo.FDPCircle;

/**
 * The import file request interface.
 * 
 * @author Ericsson
 * 
 */
public interface ImportTariffAttributeFileRequest extends Serializable{

	/**
	 * The method is used to get the Circle.
	 * @return
	 */
	FDPCircle getCircle();
	
	
	/**
	 * This method is used to get read the output read json and csv path.
	 * @return
	 */
	File getOutputCsvJsonPath();
	
	
	/**
	 * This method is used to get the configured CSV file name.
	 * @return
	 */
	String getCsvFileName();
	
	/**
	 * This method is used to get the JSON file name.
	 * @return
	 */
	String getJsonFileName();
	
	/**
	 * This method is used to get the da file name.
	 */
	public String getDaFileName();

	/**
	 * This method is used to get the PSO file name.
	 */
	public String getPsoFileName();

	/**
	 * This method is used to get the Community File name.
	 */
	public String getCommunityIDFileName() ;

	/**
	 * This method is used to get the offerId file name.
	 */
	public String getOfferIdFileName() ;
	
	
}
