package com.ericsson.fdp.business.request.impl;

import java.io.File;

import com.ericsson.fdp.business.request.ImportTariffAttributeFileRequest;
import com.ericsson.fdp.common.vo.FDPCircle;

/**
 * This class implements the import file request.
 * 
 * @author Ericsson
 * 
 */
public class ImportTariffAttributeFileRequestImpl implements ImportTariffAttributeFileRequest {

	/**
	 *  The class serial version UID
	 */
	private static final long serialVersionUID = 7683515559570735536L;

	/**
	 * The FDP Circle for which bactch job execute;
	 */
	private FDPCircle fdpCircle;
	
	/**
	 * The outputDirectory.
	 */
	private File outputDirectory;
	
	/**
	 * The csvFileName.
	 */
	private String csvFileName;
	
	/**
	 * The jsonFileName;
	 */
	private String jsonFileName;
	
	/**
	 * The daFileName
	 */
	private String daFileName;
	
	/**
	 * The psoFileName
	 */
	private String psoFileName;
	
	/**
	 * The communityIDFileName
	 */
	private String communityIDFileName;
	
	/**
	 * The offerIdFileName
	 */
	private String offerIdFileName;
	
	/**
	 * The constructor for import file.
	 * 
	 * @param fileToImport
	 *            the file to import.
	 */
	public ImportTariffAttributeFileRequestImpl(final FDPCircle fdpCircle, final File outputDirectory,
			final String csvFileName, final String jsonFileName, final String daFileName, final String psoFileName,
			final String communityIDFileName, final String offerIdFileName) {
		this.fdpCircle = fdpCircle;
		this.outputDirectory = outputDirectory;
		this.csvFileName  = csvFileName;
		this.jsonFileName = jsonFileName;
		this.daFileName = daFileName;
		this.psoFileName = psoFileName;
		this.communityIDFileName = communityIDFileName;
		this.offerIdFileName = offerIdFileName;
	}

	@Override
	public FDPCircle getCircle() {
		return fdpCircle;
	}

	@Override
	public File getOutputCsvJsonPath() {
		return outputDirectory;
	}

	@Override
	public String getCsvFileName() {
		return csvFileName;
	}

	@Override
	public String getJsonFileName() {
		return jsonFileName;
	}


	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return "ImportTariffAttributeFileRequestImpl [fdpCircle=" + fdpCircle + ", outputDirectory=" + outputDirectory
				+ ", csvFileName=" + csvFileName + ", jsonFileName=" + jsonFileName + ", daFileName=" + daFileName
				+ ", psoFileName=" + psoFileName + ", communityIDFileName=" + communityIDFileName
				+ ", offerIdFileName=" + offerIdFileName + "]";
	}

	/**
	 * @return the outputDirectory
	 */
	public File getOutputDirectory() {
		return outputDirectory;
	}

	/**
	 * @return the daFileName
	 */
	@Override
	public String getDaFileName() {
		return daFileName;
	}

	/**
	 * @return the psoFileName
	 */
	@Override
	public String getPsoFileName() {
		return psoFileName;
	}

	/**
	 * @return the communityIDFileName
	 */
	@Override
	public String getCommunityIDFileName() {
		return communityIDFileName;
	}

	/**
	 * @return the offerIdFileName
	 */
	@Override
	public String getOfferIdFileName() {
		return offerIdFileName;
	}
	

}
