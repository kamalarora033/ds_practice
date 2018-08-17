package com.ericsson.fdp.business.tariffenquiry.configimport.impl;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import javax.ejb.Stateless;
import javax.inject.Inject;
import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ericsson.fdp.business.batchjob.reportGeneration.service.CSVFileHandlerService;
import com.ericsson.fdp.business.dto.reportGeneration.CSVFileDTO;
import com.ericsson.fdp.business.dto.reportGeneration.CSVFileDataDTO;
import com.ericsson.fdp.business.request.ImportTariffAttributeFileRequest;
import com.ericsson.fdp.business.tariffenquiry.configimport.ImportTariffEnquiryAttributeFile;
import com.ericsson.fdp.business.tariffenquiry.configimport.constants.TariffConstants;
import com.ericsson.fdp.business.vo.FDPTariffEnquiryCsvAttributeNotificationMapVO;
import com.ericsson.fdp.common.constants.FDPConstant;
import com.ericsson.fdp.common.constants.JNDILookupConstant;
import com.ericsson.fdp.common.enums.Status;
import com.ericsson.fdp.common.enums.TariffEnquiryAttributeKeyType;
import com.ericsson.fdp.common.enums.TariffEnquiryAttributeKeysEnum;
import com.ericsson.fdp.common.enums.TariffEnquiryOption;
import com.ericsson.fdp.common.request.ImportTariffAttributeFileResponse;
import com.ericsson.fdp.common.request.ImportTariffAttributeFileResponseImpl;
import com.ericsson.fdp.common.vo.FDPCircle;
import com.ericsson.fdp.core.cache.AppCacheSubStore;
import com.ericsson.fdp.core.exception.ExecutionFailedException;
import com.ericsson.fdp.core.serviceprovisioning.bean.FDPAppBag;
import com.ericsson.fdp.core.utils.ApplicationConfigUtil;
import com.ericsson.fdp.dao.dto.tariffenquiry.FDPTariffEnquiryAttributeDTO;
import com.ericsson.fdp.dao.tariffenquiry.FDPTariffEnquiryAttributesDAO;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

/**
 * This class is used to import Tariff Enquiry Attribute file.
 * 
 * @author Ericsson
 * 
 */
@Stateless
public class ImportTariffEnquiryAttributeFileImpl implements ImportTariffEnquiryAttributeFile {

	@Inject
	private FDPTariffEnquiryAttributesDAO fDPTariffEnquiryAttributesDAO;

	/**
	 * The FDPTariffEnquiryAttributeDTO List.
	 */
	private List<FDPTariffEnquiryAttributeDTO> attributeDTOList = null;

	/**
	 * The logger.
	 */
	private Logger logger = LoggerFactory.getLogger(ImportTariffEnquiryAttributeFileImpl.class);

	/** The csv file handler service. */
	private CSVFileHandlerService csvFileHandlerService;

	/**
	 * Sets the csv file handler service.
	 * 
	 * @throws NamingException
	 *             the naming exception
	 */
	private void setCsvFileHandlerService() throws NamingException {
		if (null == csvFileHandlerService) {
			final Context initialContext = new InitialContext(); 
		csvFileHandlerService = (CSVFileHandlerService) initialContext
				.lookup(JNDILookupConstant.CSV_HANDLER_SERVICE_LOOK_UP);
	}
	}

	/**
	 * Gets the csv file handler service.
	 * 
	 * @return the csv file handler service
	 */
	private CSVFileHandlerService getCsvFileHandlerService() {
		return csvFileHandlerService;
	}

	/**
	 * This method will execute the whole batch in below sequence:- 1:- Read CSV
	 * File and Prepare DTO list. 2:- Read JSon File and Prepare DTO list.
	 * 3:-Insert DTO's list into the database.
	 */
	@Override
	public ImportTariffAttributeFileResponse execute(final ImportTariffAttributeFileRequest attributeFileRequest)
			throws ExecutionFailedException {
		logger.debug("Starting Processing For Request :" + attributeFileRequest);
		attributeDTOList = new ArrayList<FDPTariffEnquiryAttributeDTO>();
		ImportTariffAttributeFileResponseImpl attributeFileResponse = new ImportTariffAttributeFileResponseImpl();
		try {
			logger.debug("Parsing Process SucessFully Done.");
			if (importProvidedFile(attributeFileRequest,TariffEnquiryAttributeKeyType.SERVICE_CLASS)) {
				logger.debug("CSV File Processed SucessFully with total count:" + attributeDTOList.size());
				attributeFileResponse.setStatus(Status.SUCCESS);
				if (importJsonFile(attributeFileRequest,TariffEnquiryAttributeKeyType.ATTRIBUTES)) {
					logger.debug("Json File Processed SucessFully with total count:" + attributeDTOList.size());
					attributeFileResponse.setJsonStatus(Status.SUCCESS);
					importAttributeTariffs(attributeFileRequest, attributeFileResponse);
					if (updateDatabaseValues(attributeFileRequest)) {
						logger.debug("Database Updated SucessFully");
						attributeFileResponse.setDatabaseStatus(Status.SUCCESS);
						attributeFileResponse.setDatabaseUpdateRecordsCount(attributeDTOList.size());
					} else {
						logger.debug("Unable to updated Database.");
					}
				}
			}
		} catch (ExecutionFailedException ee) {
			logger.debug("Actual Error:" + ee.getMessage(), ee);
			throw ee;
		}
		return attributeFileResponse;
	}

	@Override
	public void importFile() throws ExecutionFailedException {

	}

	@Override
	public boolean importJsonFile(final ImportTariffAttributeFileRequest importRequest,
			final TariffEnquiryAttributeKeyType attributeKeyType) throws ExecutionFailedException {
		File jSonFileToImport = getCsvJsonFilePath(importRequest, TariffConstants.JSON_FILE_TYPE,attributeKeyType);
		if (!jSonFileToImport.isFile()) {
			throw new ExecutionFailedException("The Json-File is not a valid. Please provide a valid file");
		}
		logger.debug("Importing Json File :" + jSonFileToImport.getAbsolutePath());
		return readJsonFile(jSonFileToImport, importRequest.getCircle());
	}

	/**
	 * This method will import the .JSON file.
	 * 
	 * @param jSonFileToImport
	 * @return
	 * @throws ExecutionFailedException
	 */
	private boolean readJsonFile(final File jSonFileToImport, final FDPCircle fdpCircle)
			throws ExecutionFailedException {
		logger.debug("Entered Reading Json File");
		Map<String, List<String>> jsonFileValues = new HashMap<String, List<String>>();
		boolean isSuccessFull = false;
		final JsonParser parser = new JsonParser();
		try {
			final JsonElement jsonElement = parser.parse(new FileReader(jSonFileToImport));
			final JsonArray jsonObject = jsonElement.getAsJsonArray();
			for (int i = 0; i < jsonObject.size(); i++) {
				JsonElement element = jsonObject.get(i);
				JsonObject obj = (JsonObject) element.getAsJsonObject();
				String key = obj.get(TariffConstants.JSON_TAG_KEY).getAsString();
				JsonElement value = obj.get(TariffConstants.JSON_TAG_VALUES);
				JsonArray valueArray = value.getAsJsonArray();
				List<String> values = new ArrayList<String>();
				Iterator<JsonElement> it = valueArray.iterator();
				while (it.hasNext()) {
					String val = it.next().getAsString();
					values.add(val);
				}
				jsonFileValues.put(key, values);
			}
			logger.debug("Got Parsed Json File:-" + jsonFileValues);
			isSuccessFull = populateDataFromJsonFile(jsonFileValues, fdpCircle);
		} catch (FileNotFoundException fe) {
			isSuccessFull = false;
			logger.error("File not found - " + jSonFileToImport.getAbsolutePath(), fe);
			throw new ExecutionFailedException("File not found - " + jSonFileToImport.getAbsolutePath(), fe);
		} catch (ExecutionFailedException ee) {
			isSuccessFull = false;
			logger.error("Actual Error :" + ee.getMessage(), ee);
			throw ee;
		} catch (Exception e) {
			isSuccessFull = false;
			logger.error("Error in processing JSon File " + jSonFileToImport.getAbsolutePath(), e);
			throw new ExecutionFailedException("Error in processing JSon File " + jSonFileToImport.getAbsolutePath(), e);
		}
		return isSuccessFull;
	}

	/**
	 * This method populates the DTO's for JSON File Inputs
	 * 
	 * @param jsonFileValues
	 * @param fdpCircle
	 * @return
	 * @throws ExecutionFailedException
	 */
	private boolean populateDataFromJsonFile(final Map<String, List<String>> jsonFileValues, final FDPCircle fdpCircle)
			throws ExecutionFailedException {
		boolean isSucessFull = true;
		List<FDPTariffEnquiryAttributeDTO> attributeDTOs = new ArrayList<FDPTariffEnquiryAttributeDTO>();
		logger.debug("Starts Populating DTO from JSon File Data");
		try {
			for (final Entry<String, List<String>> entry : jsonFileValues.entrySet()) {
				String key = entry.getKey();
				List<String> valuesList = entry.getValue();
				String[] values = parseAttributeTypeFromKey(key);
				String option = TariffEnquiryOption.getCsvKey(values[1]);
				if(option == null) {
					logger.debug("Not able to map TariffOption type for:"+values[1]);
					continue;
				}
				Integer tariffOptions = Integer.parseInt(option);
				String tariffAttribute = values[0];
				TariffEnquiryAttributeKeysEnum tariffKeyFromEnum =TariffEnquiryAttributeKeysEnum.getAttributeFromKey(tariffAttribute);
				if (null == tariffKeyFromEnum) {
					logger.debug("Not able to map json key to enum:" + tariffAttribute);
					continue;
				}
				Integer tariffKey = Integer.parseInt(tariffKeyFromEnum.getAttributeKey());
				Integer tariffKeyType = Integer.parseInt(TariffEnquiryAttributeKeyType.ATTRIBUTES.getTypeId());
				Integer status = Status.SUCCESS.getStatus();
				for (final String value : valuesList) {
					attributeDTOs.add(prepareFDPTariffEnquiryAttributeDTO(tariffKey, tariffKeyType, tariffOptions,
							value, fdpCircle, status));
				}
			}
			List<FDPTariffEnquiryAttributeDTO> list = this.attributeDTOList;
			list.addAll(attributeDTOs);
		} catch (Exception e) {
			isSucessFull = false;
			logger.debug("Error while populating data from JSon File", e);
			throw new ExecutionFailedException("Error while populating data from JSon File", e);
		}
		return isSucessFull;
	}

	/**
	 * This method will parse the header KEY and ATTRIBUTE type.
	 * 
	 * 0=> Key (Mapped Key From ENUM like 011 for LOCAL_SMS_A2A) 1=> Attribute
	 * (Like DA, PSO etc)
	 * 
	 * @param key
	 * @return
	 */
	private String[] parseAttributeTypeFromKey(final String key) {
		String[] values = new String[2];
		int index = key.lastIndexOf("-");
		String temp = key.substring(0, index).trim();
		values[0] = prepareHeaderKeyForEnum(temp);
		values[1] = prepareHeaderKeyForEnum(key.substring(index + 1, key.length()));
		//logger.debug("For Json File Key:" + key + ", Got Options:-" + values[1] + ", Key:-" + values[0]);
		return values;
	}

	@Override
	public boolean importProvidedFile(final ImportTariffAttributeFileRequest importRequest,
			final TariffEnquiryAttributeKeyType attributeKeyType)
			throws ExecutionFailedException {
		boolean isSuccess = false;
		try {
			this.setCsvFileHandlerService();
		} catch (NamingException ne) {
			throw new ExecutionFailedException("Error during locating CSVFilehandler Service");
		}
		File fileToImport =getCsvJsonFilePath(importRequest, TariffConstants.CSV_FILE_TYPE,attributeKeyType);
		if (null != fileToImport && fileToImport.isFile()) {
			//throw new ExecutionFailedException("The file is not valid. Please enter a valid file.");
			logger.debug("Importing file " + fileToImport.getAbsolutePath());
			isSuccess = readCSVFile(fileToImport, importRequest.getCircle(),attributeKeyType);
		}
		return isSuccess;
	}

	/**
	 * This method will fetch the CSV and JSON File paths as per the configuraiton.
	 * @param importRequest
	 * @param fileType
	 * @return
	 * @throws ExecutionFailedException
	 */
	private File getCsvJsonFilePath(final ImportTariffAttributeFileRequest importRequest, final String fileType,
			final TariffEnquiryAttributeKeyType attributeKeyType)
			throws ExecutionFailedException {
		StringBuffer path = new StringBuffer();
		path.append(importRequest.getOutputCsvJsonPath());
		path.append(File.separator);
		String fullDirectory = path.toString();
		File readDirectory = new File(fullDirectory);
		File toReadFile = null;
		if(!readDirectory.isDirectory()) {
			logger.debug("No Directory to read at location:"+readDirectory);
			throw new ExecutionFailedException("No Directory to read at location:"+readDirectory);
		} else {
			File[] files = readDirectory.listFiles();
			if (TariffConstants.CSV_FILE_TYPE.equals(fileType)) {
				final String csvFilename = getJsonCsvFileNameForType(attributeKeyType, importRequest);
				for(File file : files) {
					if(file.getName().startsWith(csvFilename) &&  file.getName().toLowerCase().endsWith(fileType)) {
						toReadFile = file;
						logger.debug("Got File:" + toReadFile.getAbsolutePath() + " , for type:" + fileType);
						break;
					}
				}
			} else if (TariffConstants.JSON_FILE_TYPE.equals(fileType)) {
				final String jsonFileName = getJsonCsvFileNameForType(attributeKeyType, importRequest);
				for(File file : files) {
					if(file.getName().startsWith(jsonFileName)  &&  file.getName().toLowerCase().endsWith(fileType)) {
						toReadFile = file;
						logger.debug("Got File:" + toReadFile.getAbsolutePath() + " , for type:" + fileType);
						break;
					}
				}
			}
		}
		return toReadFile;
	}

	/**
	 * This method will read the CSV File.
	 * 
	 * @param fileToImport
	 * @return
	 * @throws ExecutionFailedException
	 */
	private boolean readCSVFile(final File fileToImport, final FDPCircle fdpCircle,
			final TariffEnquiryAttributeKeyType attributeKeyType) throws ExecutionFailedException {
		logger.debug("Starts Processing CSV File:"+fileToImport+" , for attributeType:"+attributeKeyType);
		boolean isSuccessFull = false;
		try {
			final CSVFileHandlerService csvHandlerService = this.getCsvFileHandlerService();
			// final CSVFileHandlerService csvHandlerService = new
			// CSVFileHandlerServiceImpl(); --> For Local Testing.
			final CSVFileDTO csvFileDTO = new CSVFileDTO();
			csvFileDTO.setCreatedBy(FDPConstant.SYSTEM);
			csvFileDTO.setFile(fileToImport);
			List<String> csvHeaders = TariffEnquiryAttributeKeyType.getHeadersList(attributeKeyType);
			//logger.debug("Got CSV File Headers:" + csvHeaders);
			csvFileDTO.setExpectedHeaders(csvHeaders);
			final CSVFileDataDTO csvFileDataDTO = csvHandlerService.importCSVFile(csvFileDTO);
			final Map<Integer, String> headerIndexMap = prepareCsvFileHeaderMapping(csvFileDataDTO.getHeaders());
			isSuccessFull = populateDataFromCsvFile(csvFileDataDTO.getDataList(), headerIndexMap, fdpCircle,attributeKeyType);
		} catch (Exception e) {
			isSuccessFull = false;
			logger.error("Getting Error while processing CSV File:" + fileToImport.getAbsolutePath(), e);
			throw new ExecutionFailedException("Getting Error while processing CSV File:"
					+ fileToImport.getAbsolutePath(), e);
		}
		logger.debug("Ends Processing CSV File:"+fileToImport+" , for attributeType:"+attributeKeyType+", with status:"+isSuccessFull);
		return isSuccessFull;
	}

	/**
	 * This method will populate the DTo list.
	 * 
	 * @param csvDataList
	 * @param csvHeaderIndex
	 */
	private boolean populateDataFromCsvFile(List<List<String>> csvDataList, Map<Integer, String> csvHeaderIndex,
			final FDPCircle fdpCircle, final TariffEnquiryAttributeKeyType attributeKeyType) throws ExecutionFailedException {
		logger.debug("Starts Populating DTO's from CSV File");
		List<FDPTariffEnquiryAttributeDTO> attributeDTOs = new ArrayList<FDPTariffEnquiryAttributeDTO>();
		boolean isSucessFull = false;
		Set<String> csvAttributeSet = getCacheValueForCsvAttributes();
		try {
			if(null == csvAttributeSet) {
				logger.debug("Tariff-Enquiry CSV Attributes Cache Not Found.");
				throw new ExecutionFailedException("Tariff-Enquiry CSV Attributes Cache Not Found.");
			}
			for (List<String> csvLine : csvDataList) {
				Integer tariffOptions = Integer.parseInt(csvLine.get(0));
				int startIndex = 0;
				if(TariffEnquiryAttributeKeyType.SERVICE_CLASS.equals(attributeKeyType)) {
					startIndex=2;
				} else {
					startIndex=1;
				}
				for (int i = startIndex; i < csvLine.size(); i++) {
					String tariffAttribute = prepareHeaderKeyForEnum(csvHeaderIndex.get(i));
					//logger.debug("CSV Header:" + tariffAttribute);
					TariffEnquiryAttributeKeysEnum tariffKeyFromEnum = TariffEnquiryAttributeKeysEnum
							.getAttributeFromKey(tariffAttribute);
					if (null == tariffKeyFromEnum) {
						logger.debug("Not able to map csv key to enum:" + tariffAttribute);
						continue;
					}
					Integer tariffKey = Integer.parseInt(tariffKeyFromEnum.getAttributeKey());
					//Integer tariffKeyType = Integer.parseInt(TariffEnquiryAttributeKeyType.SERVICE_CLASS.getTypeId());
					Integer tariffKeyType = Integer.parseInt(attributeKeyType.getTypeId());
					String value = csvLine.get(i);
					if(null == value  || value.length() == 0) {
						logger.debug("Found NULL value inside CSV File in Column:"+i);
						continue;
					}
					Integer status = checkCsvStatus(value.trim(),csvAttributeSet);
					attributeDTOs.add(prepareFDPTariffEnquiryAttributeDTO(tariffKey, tariffKeyType, tariffOptions,
							value.trim(), fdpCircle, status));
				}
			}
			isSucessFull = true;
			List<FDPTariffEnquiryAttributeDTO> list = this.attributeDTOList;
			list.addAll(attributeDTOs);
		} catch (Exception e) {
			isSucessFull = false;
			logger.error("Error in Processing CSV File", e);
			throw new ExecutionFailedException("Error in Processing CSV File", e);
		}
		return isSucessFull;
	}

	/**
	 * This method prepares the header as per ENUM.
	 * 
	 * @param headerKey
	 * @return
	 */
	private String prepareHeaderKeyForEnum(final String headerKey) {
		String enumHeaderKey = (headerKey.trim()).replace(FDPConstant.TARIFF_ENQUIRY_ATTRIBUTE_FILE_SEPERATOR,
				FDPConstant.TARIFF_ENQUIRY_ATTRIBUTE_ENUM_SEPERATOR);
		return enumHeaderKey.toUpperCase();
	}

	/**
	 * This method will prepare KEY/VALUE pair for CSV File headers to parse the
	 * data.
	 * 
	 * @param fileheaders
	 * @return
	 */
	private Map<Integer, String> prepareCsvFileHeaderMapping(final List<String> fileheaders) {
		logger.debug("Preparing CSV File Headers Map.");
		Map<Integer, String> headerMap = new HashMap<Integer, String>();
		int headerIndex = 0;
		for (String header : fileheaders) {
			headerMap.put(headerIndex, header.trim());
			headerIndex++;
		}
		logger.debug("Prepared CSV File Header Map:" + headerMap);
		return headerMap;
	}

	/**
	 * This method prepares the FDPTariffEnquiryAttributeDTO.
	 * 
	 * @param tariffKey
	 * @param tariffKeyType
	 * @param tariffOptions
	 * @param value
	 * @return
	 */
	private FDPTariffEnquiryAttributeDTO prepareFDPTariffEnquiryAttributeDTO(final Integer tariffKey,
			final Integer tariffKeyType, final Integer tariffOptions, final String value, final FDPCircle fdpCircle,
			final Integer status) {
		FDPTariffEnquiryAttributeDTO attributeDTO = new FDPTariffEnquiryAttributeDTO();
		attributeDTO.setCreatedBy(FDPConstant.SYSTEM);
		attributeDTO.setFdpCircle(fdpCircle);
		attributeDTO.setModifiedBy(FDPConstant.SYSTEM);
		attributeDTO.setTariffKey(tariffKey);
		attributeDTO.setTariffKeyType(tariffKeyType);
		attributeDTO.setTariffOptions(tariffOptions);
		attributeDTO.setValue(value);
		Calendar cal = Calendar.getInstance();
		attributeDTO.setCreatedOn(cal);
		attributeDTO.setModifiedOn(cal);
		attributeDTO.setStatus(status);
		return attributeDTO;
	}

	/**
	 * This method will update the database with loaded values from file.
	 */
	@Override
	public boolean updateDatabaseValues(final ImportTariffAttributeFileRequest importRequest)
			throws ExecutionFailedException {
		logger.debug("Updating database as per configuration");
		boolean isSucessFul = true;
		try {
			fDPTariffEnquiryAttributesDAO.deleteAll(this.attributeDTOList, importRequest.getCircle());
			logger.debug("Database updated Sucessfully.");
		} catch (Exception e) {
			isSucessFul = false;
			logger.error("Getting Error while updating records in database, with Actual Error:" + e.getMessage(), e);
			throw new ExecutionFailedException("Getting Error while updating records in database, with Actual Error:"
					+ e.getMessage(), e);
		}
		return isSucessFul;
	}

	/**
	 * This method will fetch the CSV Attributes from Cache.
	 * 
	 * @return
	 * @throws ExecutionFailedException
	 */
	@SuppressWarnings("unchecked")
	private Set<String> getCacheValueForCsvAttributes() throws ExecutionFailedException {
		String key = AppCacheSubStore.TARIFF_ENQUIRY_CSV_ATTRIBUTES_NOTI_MAP.getSubStore();
		Map<String, FDPTariffEnquiryCsvAttributeNotificationMapVO> attributeMap = (Map<String, FDPTariffEnquiryCsvAttributeNotificationMapVO>) ApplicationConfigUtil
				.getApplicationConfigCache().getValue(
						new FDPAppBag(AppCacheSubStore.TARIFF_ENQUIRY_CSV_ATTRIBUTES_NOTI_MAP, key));
		if(null == attributeMap) {
			logger.debug("CSV-Attribute Cache not Found.");
			throw new ExecutionFailedException("CSV-Attribute Cache not Found.");
		}
		return attributeMap.keySet();
	}
	
	/**
	 * This method will check for each CSV read value to check if it is Fail or Not.
	 *  
	 * @param csvValues
	 * @param csvAttributeSet
	 * @return
	 * @throws ExecutionFailedException
	 */
	private Integer checkCsvStatus(final String csvValues, final Set<String> csvAttributeSet) throws ExecutionFailedException {
		Status status = Status.SUCCESS;
		if(csvAttributeSet.contains(csvValues)) {
			status = Status.FAILURE;
		}
		return status.getStatus();
	}
	
	/**
	 * This method gets the CSV file name for attributes.
	 * 
	 * @param attributeKeyType
	 * @param importTariffAttributeFileRequest
	 * @return
	 * @throws ExecutionFailedException
	 */
	private String getJsonCsvFileNameForType(final TariffEnquiryAttributeKeyType attributeKeyType,
			final ImportTariffAttributeFileRequest importTariffAttributeFileRequest) throws ExecutionFailedException {
		String fileName = null;
		logger.debug("Entered getJsonCsvFileNameForType with attributeKeyType:"+attributeKeyType);
		switch (attributeKeyType) {
		case SERVICE_CLASS:
			fileName = importTariffAttributeFileRequest.getCsvFileName();
			break;
		case DA_ATTRIBUTES_TARIFF_VALUE:
			fileName = importTariffAttributeFileRequest.getDaFileName();
			break;
		case PSO_ATTRIBUTES_TARIFF_VALUE:
			fileName = importTariffAttributeFileRequest.getPsoFileName();
			break;
		case COMMUNITYID_ATTRIBUTES_TARIFF_VALUE:
			fileName = importTariffAttributeFileRequest.getCommunityIDFileName();
			break;
		case OFFERID_ATTRIBUTES_TARIFF_VALUE:
			fileName = importTariffAttributeFileRequest.getOfferIdFileName();
			break;
		case ATTRIBUTES:
			fileName = importTariffAttributeFileRequest.getJsonFileName();
		}
		logger.debug("Exited getJsonCsvFileNameForType with fileName:"+fileName);
		return fileName;
	}
	
	/**
	 * This method load the attributes tariff details to the database.
	 * @param attributeFileRequest
	 * @param attributeFileResponse 
	 * @throws ExecutionFailedException
	 */
	private void importAttributeTariffs(
			final ImportTariffAttributeFileRequest attributeFileRequest,
			final ImportTariffAttributeFileResponseImpl attributeFileResponse)
			throws ExecutionFailedException {
		logger.debug("Importing Attributes Tariff Details started with input request as:"+attributeFileRequest);

		attributeFileResponse
				.setDaFileStatus(importProvidedFile(
						attributeFileRequest,
						TariffEnquiryAttributeKeyType.DA_ATTRIBUTES_TARIFF_VALUE) ? Status.SUCCESS
						: Status.FAILURE);

		logger.debug("DA CSV File Processed SucessFully with total count:"
				+ attributeDTOList.size()+", response:"+attributeFileResponse);

		attributeFileResponse
				.setPsoFileStatus(importProvidedFile(
						attributeFileRequest,
						TariffEnquiryAttributeKeyType.PSO_ATTRIBUTES_TARIFF_VALUE) ? Status.SUCCESS
						: Status.FAILURE);

		logger.debug("PSO CSV File Processed SucessFully with total count:"
				+ attributeDTOList.size()+", response:"+attributeFileResponse);

		attributeFileResponse
				.setCommunityIdFileStatus(importProvidedFile(
						attributeFileRequest,
						TariffEnquiryAttributeKeyType.COMMUNITYID_ATTRIBUTES_TARIFF_VALUE) ? Status.SUCCESS
						: Status.FAILURE);

		logger.debug("COMMUNITYID CSV File Processed SucessFully with total count:"
				+ attributeDTOList.size()+", response:"+attributeFileResponse);

		attributeFileResponse
				.setOfferIdFileStatus(importProvidedFile(
						attributeFileRequest,
						TariffEnquiryAttributeKeyType.OFFERID_ATTRIBUTES_TARIFF_VALUE) ? Status.SUCCESS
						: Status.FAILURE);

		logger.debug("OFFERID CSV File Processed SucessFully with total count:"
				+ attributeDTOList.size()+", response:"+attributeFileResponse);
		logger.debug("Importing Attributes Tariff Details ends with response:"+attributeFileResponse);
	}
}
