package com.ericsson.fdp.business.batchjob.reportGeneration.service.impl;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Scanner;
import java.util.regex.Pattern;

import javax.ejb.Stateless;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ericsson.fdp.business.batchjob.reportGeneration.service.CSVFileHandlerService;
import com.ericsson.fdp.business.constants.ReportGenerationConstant;
import com.ericsson.fdp.business.dto.reportGeneration.CSVFileDTO;
import com.ericsson.fdp.business.dto.reportGeneration.CSVFileDataDTO;
import com.ericsson.fdp.common.constants.FDPConstant;
import com.ericsson.fdp.common.exception.FDPException;

/**
 * The Class CSVFileHandlerServiceImpl.
 */
@Stateless(mappedName = "csvFileHandlerService")
public class CSVFileHandlerServiceImpl implements CSVFileHandlerService {

	/** The Constant LOGGER. */
	private static final Logger LOGGER = LoggerFactory.getLogger(CSVFileHandlerServiceImpl.class);

	/** The Constant pattern. */
	private static final Pattern PATTERN = Pattern.compile(ReportGenerationConstant.CSV_COLUMN_SEPARATOR);

	@Override
	public final CSVFileDataDTO importCSVFile(final CSVFileDTO csvFileDTO) {

		List<String> headers = null;
		final File file = csvFileDTO.getFile();
		final String fileName = file.getAbsolutePath();

		final CSVFileDataDTO csvFileDataDTO = new CSVFileDataDTO();
		csvFileDataDTO.setFileName(fileName);

		try {
			if (!this.isValidCsv(fileName)) {
				csvFileDataDTO.setErrors(Arrays.asList(" Only csv file supported " + fileName));
			} else {
				headers = getHeaders(file);
				final Boolean isValidHeader = this.validateHeader(headers, csvFileDTO.getExpectedHeaders());
				if (!isValidHeader) {
					csvFileDataDTO.setErrors(Arrays.asList(" Invalid header in csv file " + fileName));
				} else {
					csvFileDataDTO.setHeaders(headers);
					csvFileDataDTO.setNoOfFields(headers.size());
					this.prepareDataRows(file, csvFileDataDTO);
				}
			}

		} catch (final FDPException e) {
			LOGGER.error("Exception Occured.", e);
			csvFileDataDTO.setErrors(Arrays.asList(e.getMessage() + fileName));
		}

		return csvFileDataDTO;
	}

	/**
	 * This method prepares data rows for CSV file.
	 * 
	 * @param file
	 *            CSV File
	 * @param csvFileDataDTO
	 *            CSV file data DTO that contains all the data rows for CSV file
	 */
	private void prepareDataRows(final File file, final CSVFileDataDTO csvFileDataDTO) {

		String row = null;
		Scanner fileScanner = null;

		try {
			final List<List<String>> dataList = new ArrayList<List<String>>();
			csvFileDataDTO.setDataList(dataList);
			fileScanner = new Scanner(file);
			if (fileScanner.hasNextLine()) {
				// ignored header row
				fileScanner.nextLine().trim();
				// parse data rows
				while (fileScanner.hasNextLine()) {
					row = fileScanner.nextLine().trim();
					if (!row.isEmpty()) {
						final String[] rowElementsArray = PATTERN.split(row, csvFileDataDTO.getNoOfFields());
						if (rowElementsArray.length != csvFileDataDTO.getNoOfFields()) {
							csvFileDataDTO
									.setErrors(Arrays.asList("Invalid csv file, filename = " + file.getAbsolutePath()));
							break;
						} else {
							dataList.add(Arrays.asList(rowElementsArray));
						}
					}
				}
			}
		} catch (final FileNotFoundException e) {
			LOGGER.error("Exception Occured.", e);
			csvFileDataDTO
					.setErrors(Arrays.asList("Error in preparing data rows for file = " + file.getAbsolutePath()));
		} finally {
			fileScanner.close();
		}
	}

	/**
	 * This method get headers for CSV file.
	 * 
	 * @param file
	 *            file object
	 * @return list of header elements
	 * @throws FDPException
	 *             the fDP exception
	 */
	private List<String> getHeaders(final File file) throws FDPException {
		Scanner fileScanner = null;
		final List<String> headers = new ArrayList<String>();
		try {
			fileScanner = new Scanner(file);
			if (fileScanner.hasNextLine()) {
				final String line = fileScanner.nextLine().trim();
				if (!line.isEmpty()) {
					final String[] rowElementsArray = PATTERN.split(line);
					headers.addAll(Arrays.asList(rowElementsArray));
				}
			} else {
				LOGGER.error("Error : Empty csv file " + file.getAbsolutePath());
				throw new FDPException("Error : Empty csv file " + file.getAbsolutePath());
			}
		} catch (final FileNotFoundException e) {
			LOGGER.error("File Parse error, File not found " + file.getAbsolutePath(), e);
			throw new FDPException("File Parse error, File not found " + file.getAbsolutePath());
		} finally {
			fileScanner.close();
		}
		return headers;
	}

	/**
	 * This method validate header in CSV file against expected headers.
	 * 
	 * @param inputHeaders
	 *            input header from CSV
	 * @param expectedHeaders
	 *            expected headers
	 * @return the boolean
	 */
	private Boolean validateHeader(final List<String> inputHeaders, final List<String> expectedHeaders) {
		int index = FDPConstant.ZERO;
		Boolean isValid = true;
		for (final String expectedHeader : expectedHeaders) {
			if (!expectedHeader.equals(inputHeaders.get(index))) {
				isValid = false;
				break;
			}
			index++;
		}
		return isValid;
	}

	/**
	 * This method check whether file name ends with CSV extension.
	 * 
	 * @param fileName
	 *            file name
	 * @return the boolean
	 */
	private Boolean isValidCsv(final String fileName) {
		Boolean isCSV = false;
		String fileExtension = null;
		final String[] fileNameArr = fileName.split("\\.");
		if (fileNameArr != null && fileNameArr.length == 2) {
			fileExtension = fileNameArr[1];
			if (("csv").equals(fileExtension)) {
				isCSV = true;
			}
		}
		return isCSV;
	}

	@Override
	public void exportCSVFile(CSVFileDataDTO csvFileDataDTO) {

		try (FileWriter fileWriter = checkAndWriteHeader(csvFileDataDTO)) {
			for (List<String> lines : csvFileDataDTO.getDataList())
				writeData(fileWriter, lines);

		} catch (IOException e) {
			LOGGER.error("exportCSVFile() | Error while writing file --> " + csvFileDataDTO.getFileName(), e);
		}

	}

	/**
	 * This method check if header exist and create the file
	 * 
	 * @param csvFileDataDTO
	 * @return The FileWriter
	 * @throws IOException
	 */
	private synchronized FileWriter checkAndWriteHeader(CSVFileDataDTO csvFileDataDTO) throws IOException {

		boolean writeHeader = new File(csvFileDataDTO.getFileName()).exists() ? false : true;
		FileWriter fileWriter = new FileWriter(csvFileDataDTO.getFileName(), true);
		
		if (writeHeader && csvFileDataDTO.getCommaSepHeaders() != null)
			fileWriter.write(csvFileDataDTO.getCommaSepHeaders());
		else if (writeHeader && csvFileDataDTO.getHeaders() != null && !csvFileDataDTO.getHeaders().isEmpty())
			writeHeader(fileWriter, csvFileDataDTO.getHeaders());

		return fileWriter;

	}

	/**
	 * This method would write the header in the given file
	 * 
	 * @param fileWriter
	 * @param columns
	 * @throws IOException
	 */
	private void writeHeader(final FileWriter fileWriter, final List<String> columns) throws IOException {
		if (columns != null && !columns.isEmpty()) {
			final Integer noOfColumns = columns.size();
			Integer currentColumn = 0;
			final StringBuilder columnRow = new StringBuilder();
			for (final String column : columns) {
				currentColumn++;
				columnRow.append(column);
				if (currentColumn != noOfColumns) {
					columnRow.append(FDPConstant.COMMA);
				}
			}
			fileWriter.write(columnRow.toString());
		}
	}

	/**
	 * This method would write the data in the given file
	 * 
	 * @param fileWriter
	 * @param line
	 * @throws IOException
	 */
	private void writeData(final FileWriter fileWriter, final List<String> line) throws IOException {

		fileWriter.append("\n");
		int column = line.size();
		int i = 1;
		for (String data : line) {
			fileWriter.append(data);
			if (i < column)
				fileWriter.append(FDPConstant.COMMA);
			i++;
		}
	}
}
