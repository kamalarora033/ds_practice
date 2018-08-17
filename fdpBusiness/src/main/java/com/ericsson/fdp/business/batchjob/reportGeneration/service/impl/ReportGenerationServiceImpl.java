package com.ericsson.fdp.business.batchjob.reportGeneration.service.impl;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

import javax.ejb.Stateless;
import javax.inject.Inject;
import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ericsson.fdp.business.batchjob.reportGeneration.service.CSVFileHandlerService;
import com.ericsson.fdp.business.batchjob.reportGeneration.service.ReportGenerationService;
import com.ericsson.fdp.business.constants.ReportGenerationConstant;
import com.ericsson.fdp.business.dto.reportGeneration.CSVFileDTO;
import com.ericsson.fdp.business.dto.reportGeneration.CSVFileDataDTO;
import com.ericsson.fdp.common.constants.FDPConstant;
import com.ericsson.fdp.common.constants.JNDILookupConstant;
import com.ericsson.fdp.common.util.DateUtil;
import com.ericsson.fdp.common.vo.FDPCircle;
import com.ericsson.fdp.core.config.utils.PropertyUtils;
import com.ericsson.fdp.core.logging.FDPLogger;
import com.ericsson.fdp.core.logging.FDPLoggerFactory;
import com.ericsson.fdp.dao.dto.batchJob.BatchExecutionRequestDTO;
import com.ericsson.fdp.dao.dto.batchJob.BatchExecutionResponseDTO;
import com.ericsson.fdp.dao.fdpadmin.FDPCGWReportDAO;
import com.ericsson.fdp.dao.fdpadmin.FDPEMADataDAO;
import com.ericsson.fdp.dao.fdpadmin.FDPProductProvisioningReportDAO;
import com.ericsson.fdp.dao.fdpadmin.FDPRevenueReportDAO;
import com.ericsson.fdp.dao.fdpadmin.FDPTransactionTPSDataDAO;
import com.ericsson.fdp.dao.fdpadmin.IncomingTPSDAO;

/**
 * The Class ReportGenerationServiceImpl.
 * 
 * @author Ericsson
 */
@Stateless
public class ReportGenerationServiceImpl implements ReportGenerationService {

	/**
	 * CGW report Incoming Hourly TPS report Transaction Hourly TPS report
	 * Product provisioning report.
	 */
	private enum ReportShortName {

		/** The cgws. */
		CGWS,
		/** The tht. */
		THT,
		/** The iht. */
		IHT,
		/** The pp. */
		PP,
		/** The ema. */
		EMA,
		/** The revenue Report. */
		R
	};

	/** The Constant LOGGER. */
	private static final Logger LOGGER = LoggerFactory.getLogger(ReportGenerationServiceImpl.class);

	/** The cgw report dao. */
	@Inject
	private FDPCGWReportDAO cgwReportDAO;

	/** The tps data dao. */
	@Inject
	private FDPTransactionTPSDataDAO tpsDataDAO;

	/** The incoming tpsdao. */
	@Inject
	private IncomingTPSDAO incomingTPSDAO;

	/** The product provisioning report dao. */
	@Inject
	private FDPProductProvisioningReportDAO productProvisioningReportDAO;

	/** The fdp revenue report dao. */
	@Inject
	private FDPRevenueReportDAO fdpRevenueReportDAO;

	/** The fdp ema data dao. */
	@Inject
	private FDPEMADataDAO fdpEMADataDAO;

	/** The csv file handler service. */
	private CSVFileHandlerService csvFileHandlerService;

	/**
	 * Sets the csv file handler service.
	 * 
	 * @throws NamingException
	 *             the naming exception
	 */
	private void setCsvFileHandlerService() throws NamingException {
		final Context initialContext = new InitialContext();
		csvFileHandlerService =
				(CSVFileHandlerService) initialContext.lookup(JNDILookupConstant.CSV_HANDLER_SERVICE_LOOK_UP);
	}

	/**
	 * Gets the csv file handler service.
	 * 
	 * @return the csv file handler service
	 */
	private CSVFileHandlerService getCsvFileHandlerService() {
		return csvFileHandlerService;
	}

	@Override
	public BatchExecutionResponseDTO generateFDPReports(final BatchExecutionRequestDTO batchExecutionRequest,
			final File reportFolder) {

		final String circleName = batchExecutionRequest.getCircle().getCircleName();
		final File[] listOfFiles = reportFolder.listFiles();
		final List<File> cgwReports = new ArrayList<File>();
		final List<File> transactionHourlyTPSReports = new ArrayList<File>();
		final List<File> incomingHourlyTPSReports = new ArrayList<File>();
		final List<File> productProvisioningReports = new ArrayList<File>();
		final List<File> emaReports = new ArrayList<File>();
		final List<File> revenueReports = new ArrayList<File>();
		LOGGER.debug("Report Files found are : {} ", listOfFiles);
		// Prepare list of report files.
		for (int index = 0; index < listOfFiles.length; index++) {
			final String fileName = listOfFiles[index].getName();
			if (isFileNameMatch(fileName, circleName)) {
				if (fileName.contains(ReportGenerationConstant.FILENAME_SEPARATOR + ReportShortName.CGWS.toString()
						+ ReportGenerationConstant.FILENAME_SEPARATOR)) {
					this.addFileToList(cgwReports, listOfFiles[index]);
				} else if (fileName.contains(ReportGenerationConstant.FILENAME_SEPARATOR
						+ ReportShortName.THT.toString() + ReportGenerationConstant.FILENAME_SEPARATOR)) {
					this.addFileToList(transactionHourlyTPSReports, listOfFiles[index]);
				} else if (fileName.contains(ReportGenerationConstant.FILENAME_SEPARATOR
						+ ReportShortName.IHT.toString() + ReportGenerationConstant.FILENAME_SEPARATOR)) {
					this.addFileToList(incomingHourlyTPSReports, listOfFiles[index]);
				} else if (fileName.contains(ReportGenerationConstant.FILENAME_SEPARATOR
						+ ReportShortName.PP.toString() + ReportGenerationConstant.FILENAME_SEPARATOR)) {
					this.addFileToList(productProvisioningReports, listOfFiles[index]);
				} else if (fileName.contains(ReportGenerationConstant.FILENAME_SEPARATOR
						+ ReportShortName.EMA.toString() + ReportGenerationConstant.FILENAME_SEPARATOR)) {
					this.addFileToList(emaReports, listOfFiles[index]);
				} else if (fileName.contains(ReportGenerationConstant.FILENAME_SEPARATOR + ReportShortName.R.toString()
						+ ReportGenerationConstant.FILENAME_SEPARATOR)) {
					this.addFileToList(revenueReports, listOfFiles[index]);
				}
			}
		}
		Integer failureCount = 0;
		final Integer totalBatchCount =
				cgwReports.size() + transactionHourlyTPSReports.size() + incomingHourlyTPSReports.size()
						+ productProvisioningReports.size() + emaReports.size() + revenueReports.size();

		// save the report files to database
		try {
			this.setCsvFileHandlerService();
			failureCount =
					this.saveReports(cgwReports, batchExecutionRequest.getCircle(),
							batchExecutionRequest.getModifiedBy(), ReportShortName.CGWS);
			failureCount =
					failureCount
							+ this.saveReports(transactionHourlyTPSReports, batchExecutionRequest.getCircle(),
									batchExecutionRequest.getModifiedBy(), ReportShortName.THT);
			failureCount =
					failureCount
							+ this.saveReports(incomingHourlyTPSReports, batchExecutionRequest.getCircle(),
									batchExecutionRequest.getModifiedBy(), ReportShortName.IHT);
			failureCount =
					failureCount
							+ this.saveReports(productProvisioningReports, batchExecutionRequest.getCircle(),
									batchExecutionRequest.getModifiedBy(), ReportShortName.PP);
			failureCount =
					failureCount
							+ this.saveReports(emaReports, batchExecutionRequest.getCircle(),
									batchExecutionRequest.getModifiedBy(), ReportShortName.EMA);
			failureCount =
					failureCount
							+ this.saveReports(revenueReports, batchExecutionRequest.getCircle(),
									batchExecutionRequest.getModifiedBy(), ReportShortName.R);
		} catch (final NamingException e) {
			final Logger circleLogger =
					FDPLoggerFactory.getCircleAdminLogger(batchExecutionRequest.getCircle().getCircleName());
			FDPLogger.error(circleLogger, this.getClass(), "generateFDPReports",
					"Naming excpetion occured in getting csvFileHandlerService");
			failureCount = totalBatchCount;
		}
		final BatchExecutionResponseDTO executionResponseDTO =
				new BatchExecutionResponseDTO(totalBatchCount, failureCount, totalBatchCount - failureCount,
						batchExecutionRequest.getBatchExecutionInfoId(), batchExecutionRequest.getModifiedBy());
		return executionResponseDTO;
	}

	/**
	 * This method save report files to the database.
	 * 
	 * @param reportFiles
	 *            list of report files
	 * @param fdpCircle
	 *            fdp Circle object
	 * @param modifiedBy
	 *            modified by user
	 * @param shortName
	 *            short name CGW/THT/IHT/PP
	 * @return count for number of failures.
	 */
	private Integer saveReports(final List<File> reportFiles, final FDPCircle fdpCircle, final String modifiedBy,
			final ReportShortName shortName) {
		LOGGER.debug("Files for {} report are : {} ", shortName.name(), reportFiles);

		final Logger circleLogger = FDPLoggerFactory.getCircleAdminLogger(fdpCircle.getCircleName());
		final CSVFileHandlerService csvHandlerService = this.getCsvFileHandlerService();
		Integer failureCount = 0;
		for (final File file : reportFiles) {
			LOGGER.debug("Processing report File : {} ", file.getPath());
			final String fileDate = this.getDateFromFileName(file.getName());
			final CSVFileDTO csvFileDTO = new CSVFileDTO();
			csvFileDTO.setFile(file);
			csvFileDTO.setExpectedHeaders(getHeaderByShortName(shortName));
			final CSVFileDataDTO csvFileDataDTO = csvHandlerService.importCSVFile(csvFileDTO);

			// if there is no error in CSV file then save the records
			if (csvFileDataDTO.getErrors() != null && !csvFileDataDTO.getErrors().isEmpty()) {
				FDPLogger.error(circleLogger, this.getClass(), "saveReports",
						csvFileDataDTO.getErrors().get(FDPConstant.ZERO));
				failureCount++;
			} else {
				final Boolean isValidData =
						this.saveDataRows(csvFileDataDTO.getDataList(), shortName, fdpCircle.getCircleId(), modifiedBy,
								fileDate, circleLogger);
				if (!isValidData) {
					failureCount++;
					FDPLogger.error(circleLogger, this.getClass(), "saveReports",
							"Error in CSV file" + csvFileDataDTO.getFileName());
				} else {
					final String archiveFolder =
							PropertyUtils.getProperty(ReportGenerationConstant.REPORT_ARCHIVE_FOLDER_PATH);
					try {
						Files.move(Paths.get(file.getPath()),
								Paths.get(archiveFolder + fdpCircle.getCircleName() + "/" + file.getName()));
						FDPLogger.info(circleLogger, this.getClass(), "saveReports",
								" File " + csvFileDataDTO.getFileName() + "moved successfully to archive.");
					} catch (final IOException e) {
						FDPLogger.error(circleLogger, this.getClass(), "saveReports",
								" File " + csvFileDataDTO.getFileName() + " failed to move.", e);
					}
				}
			}
		}
		return failureCount;
	}

	/**
	 * This method check whether file name matches with the pattern specified
	 * for report file.
	 * 
	 * @param fileName
	 *            file name
	 * @param circleName
	 *            circle name
	 * @return true if file name matches
	 */
	private Boolean isFileNameMatch(final String fileName, final String circleName) {

		final Boolean isCircleMatch =
				fileName.contains(ReportGenerationConstant.FILENAME_SEPARATOR + circleName
						+ ReportGenerationConstant.FILENAME_SEPARATOR);

		Boolean isDateMatch = false;

		try {
			final String fileDateStr = this.getDateFromFileName(fileName);
			if (fileDateStr != null) {
				final String previousDate = DateUtil.getPrevDate(FDPConstant.REPORT_FILE_NAME_DATE_PATTERN);
				final SimpleDateFormat format = new SimpleDateFormat(FDPConstant.REPORT_FILE_NAME_DATE_PATTERN);

				final Date date1 = format.parse(previousDate);
				final Date date2 = format.parse(fileDateStr);
				// parse file for yesterday and before that
				if (date1.compareTo(date2) >= 0) {
					isDateMatch = true;
				}
			}
		} catch (final ParseException e) {
			isDateMatch = false;
		}

		return isCircleMatch && isDateMatch;
	}

	/**
	 * This method add file to list of file.
	 * 
	 * @param reportFiles
	 *            report files
	 * @param newFile
	 *            new file to be added to the list.
	 */
	private void addFileToList(final List<File> reportFiles, final File newFile) {
		Boolean isDuplicate = false;
		Boolean isLatest = false;
		int index = 0;
		for (final File file : reportFiles) {
			if (file.getName().equals(newFile.getName())) {
				isDuplicate = true;
				if (file.lastModified() < newFile.lastModified()) {
					isLatest = true;
				}
				break;
			}
			index++;
		}
		if (isDuplicate) {
			if (isLatest) {
				reportFiles.remove(index);
				reportFiles.add(newFile);
			}
		} else {
			reportFiles.add(newFile);
		}
	}

	/**
	 * This method get header based on short name. Each CSV file has different
	 * headers
	 * 
	 * @param shortName
	 *            short name CGW/THT/IHT/PP
	 * @return list of headers for CSV file specific to short name
	 */
	private List<String> getHeaderByShortName(final ReportShortName shortName) {
		List<String> headers = null;
		if (ReportShortName.CGWS.equals(shortName)) {
			headers =
					Arrays.asList(ReportGenerationConstant.CGW_HEADERS
							.split(ReportGenerationConstant.CSV_COLUMN_SEPARATOR));
		} else if (ReportShortName.THT.equals(shortName)) {
			headers =
					Arrays.asList(ReportGenerationConstant.THT_HEADERS
							.split(ReportGenerationConstant.CSV_COLUMN_SEPARATOR));
		} else if (ReportShortName.IHT.equals(shortName)) {
			headers =
					Arrays.asList(ReportGenerationConstant.IHT_HEADERS
							.split(ReportGenerationConstant.CSV_COLUMN_SEPARATOR));
		} else if (ReportShortName.PP.equals(shortName)) {
			headers =
					Arrays.asList(ReportGenerationConstant.PP_HEADERS
							.split(ReportGenerationConstant.CSV_COLUMN_SEPARATOR));
		} else if (ReportShortName.EMA.equals(shortName)) {
			headers =
					Arrays.asList(ReportGenerationConstant.EMA_HEADERS
							.split(ReportGenerationConstant.CSV_COLUMN_SEPARATOR));
		} else if (ReportShortName.R.equals(shortName)) {
			headers =
					Arrays.asList(ReportGenerationConstant.REVENUE_REPORT_HEADERS
							.split(ReportGenerationConstant.CSV_COLUMN_SEPARATOR));
		}
		return headers;
	}

	/**
	 * This method save report data based on short name CGW/THT/IHT/PP.
	 * 
	 * @param dataList
	 *            rows for reports
	 * @param shortName
	 *            short name CGW/THT/IHT/PP
	 * @param circleId
	 *            circle id
	 * @param modifiedBy
	 *            modified by
	 * @param fileDate
	 *            date in csv file name.
	 * @param circleLogger
	 *            circle logger
	 * @return true if data rows saves successfully.
	 */
	private Boolean saveDataRows(final List<List<String>> dataList, final ReportShortName shortName,
			final Long circleId, final String modifiedBy, final String fileDate, final Logger circleLogger) {
		Boolean isValidData = false;
		if (ReportShortName.CGWS.equals(shortName)) {
			isValidData = cgwReportDAO.saveCgwReports(dataList, fileDate, circleId, modifiedBy, circleLogger);
		} else if (ReportShortName.THT.equals(shortName)) {
			isValidData = tpsDataDAO.saveTransactionReports(dataList, fileDate, circleId, modifiedBy, circleLogger);
		} else if (ReportShortName.IHT.equals(shortName)) {
			isValidData = incomingTPSDAO.saveIncomingTPSReports(dataList, fileDate, circleId, modifiedBy, circleLogger);
		} else if (ReportShortName.PP.equals(shortName)) {
			isValidData =
					productProvisioningReportDAO.saveProductProvisingReports(dataList, fileDate, circleId, modifiedBy,
							circleLogger);
		} else if (ReportShortName.EMA.equals(shortName)) {
			isValidData = fdpEMADataDAO.saveEMAData(dataList, fileDate, circleId, modifiedBy, circleLogger);
		} else if (ReportShortName.R.equals(shortName)) {
			isValidData = fdpRevenueReportDAO.saveRevenueReport(dataList, fileDate, circleId, modifiedBy, circleLogger);
		}
		return isValidData;
	}

	/**
	 * Gets the date from file name pattern for CSV file.
	 * 
	 * @param fileName
	 *            the file name
	 * @return the date string from file name
	 */
	private String getDateFromFileName(final String fileName) {

		final String[] arr = fileName.split(FDPConstant.UNDERSCORE);
		String fileDateStr = null;
		if (arr.length >= 3) {
			fileDateStr = arr[3];
		}
		return fileDateStr;
	}
}
