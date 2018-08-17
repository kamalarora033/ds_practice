package com.ericsson.fdp.business.sharedaccount.ecmsImport.impl;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.ejb.Stateless;
import javax.inject.Inject;

import org.apache.poi.hssf.usermodel.HSSFRow;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ericsson.fdp.business.enums.ImportErrorCode;
import com.ericsson.fdp.business.enums.SharedAccountConsumerValues;
import com.ericsson.fdp.business.enums.SharedAccountProviderValues;
import com.ericsson.fdp.business.request.ImportFileRequest;
import com.ericsson.fdp.business.request.ImportFileResponse;
import com.ericsson.fdp.business.request.impl.ImportFileRequestImpl;
import com.ericsson.fdp.business.request.impl.ImportFileResponseImpl;
import com.ericsson.fdp.business.sharedaccount.ecmsImport.ImportSharedAccountFile;
import com.ericsson.fdp.business.sharedaccount.ecmsImport.database.impl.SharedAccountConsumerDatabaseImportImpl;
import com.ericsson.fdp.business.sharedaccount.ecmsImport.database.impl.SharedAccountProviderDatabaseImportImpl;
import com.ericsson.fdp.common.constants.FDPConstant;
import com.ericsson.fdp.common.exception.ConditionFailedException;
import com.ericsson.fdp.common.util.ImportExcelUtil;
import com.ericsson.fdp.core.cache.AppCacheSubStore;
import com.ericsson.fdp.core.exception.EvaluationFailedException;
import com.ericsson.fdp.core.exception.ExecutionFailedException;
import com.ericsson.fdp.core.serviceprovisioning.bean.FDPAppBag;
import com.ericsson.fdp.core.utils.ApplicationConfigUtil;

/**
 * This class is used to import shared account file.
 * 
 * @author Ericsson
 * 
 */
@Stateless
public class ImportSharedAccountFileImpl implements ImportSharedAccountFile {

	/**
	 * The shared account consumer database import.
	 */
	@Inject
	private SharedAccountConsumerDatabaseImportImpl sharedAccountConsumerDatabaseImportImpl;

	/**
	 * The shared account provider database import.
	 */
	@Inject
	private SharedAccountProviderDatabaseImportImpl sharedAccountProviderDatabaseImportImpl;

	/**
	 * The logger.
	 */
	private final Logger logger = LoggerFactory.getLogger(ImportSharedAccountFileImpl.class);

	@Override
	public void importFile() throws ExecutionFailedException {
		final File fileToImport = new File("C:\\Users\\jaiprakash1354\\Desktop\\sharedAccountImportExcel.xls");
		final ImportFileRequestImpl importFileRequest = new ImportFileRequestImpl(fileToImport,
				"C:\\Users\\jaiprakash1354\\Desktop\\sharedAccountImportOutputExcel.xls");
		importProvidedFile(importFileRequest);
		// File fileToExport = importFileResponse.getFailedOutputFile();
		// fileToExport.renameTo(new
		// File("C:\\Users\\jaiprakash1354\\Desktop\\sharedAccountImportOutputExcel.xls"));
	}

	@Override
	public ImportFileResponse importProvidedFile(final ImportFileRequest importRequest) throws ExecutionFailedException {
		final File fileToImport = importRequest.getFileToImport();
		logger.info("Importing file " + fileToImport.getAbsolutePath());
		if (!fileToImport.isFile()) {
			throw new ExecutionFailedException("The file is not valid. Please enter a valid file.");
		}
		HSSFWorkbook workbookToImport;
		try {
			workbookToImport = ImportExcelUtil.getWorkBookForFile(fileToImport);
			return importWorkbook(workbookToImport.getSheetAt(0),
					ImportExcelUtil.getFileHeaders(workbookToImport, getHeaders()), importRequest.getErrorFilePath());
		} catch (final FileNotFoundException e) {
			logger.error("File not found - " + fileToImport.getAbsolutePath(), e);
			throw new ExecutionFailedException("File not found - " + fileToImport.getAbsolutePath(), e);
		} catch (final IOException e) {
			logger.error("Error retreiving input stream from document - " + fileToImport.getAbsolutePath(), e);
			throw new ExecutionFailedException("Error retreiving input stream from document - "
					+ fileToImport.getAbsolutePath(), e);
		} catch (final ConditionFailedException e) {
			logger.error("Error in excel. Could not import - " + fileToImport.getAbsolutePath(), e);
			throw new ExecutionFailedException("Error in excel. Could not import - " + fileToImport.getAbsolutePath(),
					e);
		} catch (final EvaluationFailedException e) {
			throw new ExecutionFailedException("Error in importing. Could not import - "
					+ fileToImport.getAbsolutePath(), e);
		}
	}

	@Override
	public void updateDatabaseValues() throws ExecutionFailedException {
		logger.info("Updating database as per configuration");
		final boolean deleteRows = FDPConstant.ECMS_DATABASE_DELETE_VALUE
				.equalsIgnoreCase((String) ApplicationConfigUtil.getApplicationConfigCache().getValue(
						new FDPAppBag(AppCacheSubStore.CONFIGURATION_MAP, FDPConstant.ECMS_DATABASE_CONF)));
		logger.info("Updating value for database " + deleteRows);
		sharedAccountConsumerDatabaseImportImpl.updateDatabaseAsPerConfiguration(deleteRows);
		sharedAccountProviderDatabaseImportImpl.updateDatabaseAsPerConfiguration(deleteRows);

	}

	/**
	 * This method is used to import the workbook.
	 * 
	 * @param sheetToImport
	 *            the sheet to import.
	 * @param headerPlaces
	 *            the values of the headers.
	 * @param errorFilePath
	 *            the error file path.
	 * @return the response after import.
	 * @throws EvaluationFailedException
	 *             Exception, if any.
	 */
	private ImportFileResponse importWorkbook(final HSSFSheet sheetToImport, final Map<String, Integer> headerPlaces,
			final String errorFilePath) throws EvaluationFailedException {
		final List<Map<SharedAccountProviderValues, Object>> sharedAccountProviderValues = new ArrayList<Map<SharedAccountProviderValues, Object>>();
		final List<Map<SharedAccountConsumerValues, Object>> sharedAccountConsumerValues = new ArrayList<Map<SharedAccountConsumerValues, Object>>();
		final Map<HSSFRow, ImportErrorCode> errorRows = new HashMap<HSSFRow, ImportErrorCode>();
		final int rowsToImport = sheetToImport.getPhysicalNumberOfRows();
		for (int row = 1; row < rowsToImport; row++) {
			final HSSFRow currentRow = sheetToImport.getRow(row);
			if (isProviderRow(currentRow, headerPlaces)) {
				sharedAccountProviderValues.add(getSharedAccountProviderValues(currentRow, headerPlaces));
			} else if (isConsumerRow(currentRow, headerPlaces)) {
				sharedAccountConsumerValues.add(getSharedAccountConsumerValues(currentRow, headerPlaces));
			} else {
				errorRows.put(currentRow, ImportErrorCode.ROW_SHARED_TYPE_UNDEFINED);
			}
		}
		final List<Map<SharedAccountProviderValues, Object>> failedValuesForProvider = sharedAccountProviderDatabaseImportImpl
				.importValues(sharedAccountProviderValues);
		final List<Map<SharedAccountConsumerValues, Object>> failedValuesForConsumer = sharedAccountConsumerDatabaseImportImpl
				.importValues(sharedAccountConsumerValues);
		File outputFile = null;
		try {
			outputFile = new File(errorFilePath + File.separator + "ImportSharedAccountFile"
					+ Calendar.getInstance().getTimeInMillis() + ".xls");
			if (!outputFile.exists()) {
				outputFile.createNewFile();
			}
			final HSSFWorkbook workbook = new HSSFWorkbook();
			final HSSFSheet sheet = workbook.createSheet("Error Values");
			writeDataToSheet(sheet, failedValuesForConsumer, failedValuesForProvider);
			final FileOutputStream out = new FileOutputStream(outputFile);
			workbook.write(out);
			out.close();

		} catch (final FileNotFoundException e) {
			throw new EvaluationFailedException("Could not create output file", e);
		} catch (final IOException e) {
			throw new EvaluationFailedException("Could not create output file", e);
		}
		final ImportFileResponseImpl importFileResponseImpl = new ImportFileResponseImpl();
		importFileResponseImpl.setOutputFile(outputFile);
		importFileResponseImpl.setFailureValues(new Long(failedValuesForConsumer.size()
				+ failedValuesForProvider.size()));
		importFileResponseImpl.setSuccessfulValues(sharedAccountProviderValues.size()
				+ sharedAccountConsumerValues.size() - importFileResponseImpl.getFailureValues());
		return importFileResponseImpl;
	}

	/**
	 * This method is used to write the data to the sheet.
	 * 
	 * @param sheet
	 *            the sheet to which data is to be written.
	 * @param failedValuesForConsumers
	 *            the failed consumer values.
	 * @param failedValuesForProviders
	 *            the failed provider values.
	 */
	private void writeDataToSheet(final HSSFSheet sheet,
			final List<Map<SharedAccountConsumerValues, Object>> failedValuesForConsumers,
			final List<Map<SharedAccountProviderValues, Object>> failedValuesForProviders) {
		int rowNum = 0;
		for (final Map<SharedAccountProviderValues, Object> failedValuesForProvider : failedValuesForProviders) {
			final Row row = sheet.createRow(rowNum++);
			Cell cell = row.createCell(0);
			cell.setCellValue("Row Num "
					+ ((HSSFRow) failedValuesForProvider.get(SharedAccountProviderValues.RowNum)).getRowNum());
			cell = row.createCell(1);
			cell.setCellValue(((ImportErrorCode) failedValuesForProvider.get(SharedAccountProviderValues.ERROR_CODE))
					.getErrorMessage());
		}
		for (final Map<SharedAccountConsumerValues, Object> failedValuesForConsumer : failedValuesForConsumers) {
			final Row row = sheet.createRow(rowNum++);
			Cell cell = row.createCell(0);
			cell.setCellValue("Row Num "
					+ ((HSSFRow) failedValuesForConsumer.get(SharedAccountConsumerValues.RowNum)).getRowNum());
			cell = row.createCell(1);
			cell.setCellValue(((ImportErrorCode) failedValuesForConsumer.get(SharedAccountConsumerValues.ERROR_CODE))
					.getErrorMessage());
		}
	}

	/**
	 * This method is used to check the current row if it is consumer.
	 * 
	 * @param currentRow
	 *            the current row.
	 * @param headerPlaces
	 *            the header places.
	 * @return true, if it is consumer row, false otherwise.
	 */
	private boolean isConsumerRow(final HSSFRow currentRow, final Map<String, Integer> headerPlaces) {
		return headerPlaces.get(SharedAccountConsumerValues.ConsumerMSISDN.getHeaderValue()) != null
				&& headerPlaces.get(SharedAccountConsumerValues.ConsumerOfferId.getHeaderValue()) != null
				&& ImportExcelUtil.getCellValue(currentRow.getCell(headerPlaces
						.get(SharedAccountConsumerValues.ConsumerMSISDN.getHeaderValue()))) != null
				&& ImportExcelUtil.getCellValue(currentRow.getCell(headerPlaces
						.get(SharedAccountConsumerValues.ConsumerOfferId.getHeaderValue()))) != null;
	}

	/**
	 * This method is used to get the shared account consumer values.
	 * 
	 * @param currentRow
	 *            the current row.
	 * @param headerPlaces
	 *            the header places.
	 * @return the map of values.
	 */
	private Map<SharedAccountConsumerValues, Object> getSharedAccountConsumerValues(final HSSFRow currentRow,
			final Map<String, Integer> headerPlaces) {
		final Map<SharedAccountConsumerValues, Object> sharedAccountConsumerVal = new HashMap<SharedAccountConsumerValues, Object>();
		for (final SharedAccountConsumerValues sharedAccountConsumerValues : SharedAccountConsumerValues.values()) {
			final Integer index = headerPlaces.get(sharedAccountConsumerValues.getHeaderValue());
			if (index != null) {
				sharedAccountConsumerVal.put(sharedAccountConsumerValues,
						ImportExcelUtil.getCellValue(currentRow.getCell(index)));
			}
		}
		sharedAccountConsumerVal.put(SharedAccountConsumerValues.RowNum, currentRow);
		return sharedAccountConsumerVal;
	}

	/**
	 * This method is used to get the shared account provider values.
	 * 
	 * @param currentRow
	 *            the current row.
	 * @param headerPlaces
	 *            the header places.
	 * @return the map of values.
	 */
	private Map<SharedAccountProviderValues, Object> getSharedAccountProviderValues(final HSSFRow currentRow,
			final Map<String, Integer> headerPlaces) {
		final Map<SharedAccountProviderValues, Object> sharedAccountProviderVal = new HashMap<SharedAccountProviderValues, Object>();
		for (final SharedAccountProviderValues sharedAccountProviderValues : SharedAccountProviderValues.values()) {
			final Integer index = headerPlaces.get(sharedAccountProviderValues.getHeaderValue());
			if (index != null) {
				sharedAccountProviderVal.put(sharedAccountProviderValues,
						ImportExcelUtil.getCellValue(currentRow.getCell(index)));
			}
		}
		sharedAccountProviderVal.put(SharedAccountProviderValues.RowNum, currentRow);
		return sharedAccountProviderVal;
	}

	/**
	 * This method is used to check the current row if it is provider.
	 * 
	 * @param currentRow
	 *            the current row.
	 * @param headerPlaces
	 *            the header places.
	 * @return true, if it is consumer row, false otherwise.
	 */
	private boolean isProviderRow(final HSSFRow currentRow, final Map<String, Integer> headerPlaces) {
		return headerPlaces.get(SharedAccountProviderValues.GroupOfferId.getHeaderValue()) != null
				&& headerPlaces.get(SharedAccountProviderValues.GroupMSISDN.getHeaderValue()) != null
				&& ImportExcelUtil.getCellValue(currentRow.getCell(headerPlaces
						.get(SharedAccountProviderValues.GroupOfferId.getHeaderValue()))) != null
				&& ImportExcelUtil.getCellValue(currentRow.getCell(headerPlaces
						.get(SharedAccountProviderValues.GroupMSISDN.getHeaderValue()))) != null;
	}

	/**
	 * This method is used to find the headers.
	 * 
	 * @return the list of headers.
	 */
	private List<String> getHeaders() {
		final List<String> headerList = new ArrayList<String>();
		headerList.addAll(SharedAccountProviderValues.getHeaders());
		headerList.addAll(SharedAccountConsumerValues.getHeaders());
		return headerList;
	}

}
