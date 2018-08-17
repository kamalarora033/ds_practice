package com.ericsson.fdp.business.batchjob.mass.service.impl;

import java.io.File;
import java.io.FileOutputStream;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import javax.annotation.Resource;
import javax.ejb.Startup;
import javax.ejb.Stateless;
import javax.inject.Inject;
import javax.naming.NamingException;

import org.apache.poi.hssf.usermodel.HSSFRow;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ericsson.fdp.FDPCacheable;
import com.ericsson.fdp.business.batchjob.mass.service.MassLoadUrlService;
import com.ericsson.fdp.business.enums.AuxRequestParam;
import com.ericsson.fdp.business.enums.ExternalSystemActionServiceProvisingMapping;
import com.ericsson.fdp.business.product.Product;
import com.ericsson.fdp.business.product.impl.ProductNameCacheImpl;
import com.ericsson.fdp.business.serviceprovisioning.ServiceProvisioning;
import com.ericsson.fdp.business.transaction.TransactionSequenceGeneratorService;
import com.ericsson.fdp.business.util.LoggerUtil;
import com.ericsson.fdp.business.util.RequestUtil;
import com.ericsson.fdp.business.util.ServiceProvisioningUtil;
import com.ericsson.fdp.common.constants.FDPConstant;
import com.ericsson.fdp.common.constants.JNDILookupConstant;
import com.ericsson.fdp.common.constants.MassLoadConstant;
import com.ericsson.fdp.common.enums.ChannelType;
import com.ericsson.fdp.common.enums.ModuleType;
import com.ericsson.fdp.common.enums.Status;
import com.ericsson.fdp.common.exception.FDPServiceException;
import com.ericsson.fdp.common.util.FDPStringUtil;
import com.ericsson.fdp.common.vo.FDPCircle;
import com.ericsson.fdp.core.config.utils.PropertyUtils;
import com.ericsson.fdp.core.dsm.framework.service.FDPCache;
import com.ericsson.fdp.core.exception.ExecutionFailedException;
import com.ericsson.fdp.core.logging.FDPLogger;
import com.ericsson.fdp.core.request.FDPRequest;
import com.ericsson.fdp.core.request.impl.FDPRequestImpl;
import com.ericsson.fdp.core.serviceprovisioning.bean.FDPAppBag;
import com.ericsson.fdp.core.serviceprovisioning.bean.FDPMetaBag;
import com.ericsson.fdp.core.utils.ApplicationConfigUtil;
import com.ericsson.fdp.core.utils.CircleCodeFinder;
import com.ericsson.fdp.dao.dto.mass.ExcelData;
import com.ericsson.fdp.dao.dto.mass.MassDTO;
import com.ericsson.fdp.dao.dto.mass.MassDetailsDTO;
import com.ericsson.fdp.dao.entity.FDPMassLoad;
import com.ericsson.fdp.dao.enums.FDPServiceProvSubType;
import com.ericsson.fdp.dao.enums.ProductAdditionalInfoEnum;
import com.ericsson.fdp.dao.exception.FDPConcurrencyException;
import com.ericsson.fdp.dao.mass.business.MassLoadDao;
import com.ericsson.fdp.dao.mass.business.MassLoadDetailDao;

/**
 * 
 * This service class is responsible to read mass provision and de
 * provision records from database and provisioned those records. It also creates
 * result excel sheet
 * 
 * @author eagarsh
 * 
 */
@Stateless(name = "MassLoadUrlServiceImpl")
// @DependsOn(value = "ApplicationMonitor")
@Startup
public class MassLoadUrlServiceImpl implements MassLoadUrlService {

	private static final Logger LOGGER = LoggerFactory
			.getLogger(MassLoadUrlServiceImpl.class);
	/* The MassLoadDao. */
	@Inject
	MassLoadDao massLoadDao;
	/* The MassLoadDetailDao. */
	@Inject
	MassLoadDetailDao massLoadDetailDao;
	/** The generatorService. */
	@Resource(lookup = JNDILookupConstant.TRANSACTION_ID_GENERATOR_SERVICE)
	private TransactionSequenceGeneratorService generatorService;

	private MassProvisioningExecutor massProvisioningServiceImpl;

	private Map<String, List<MassDTO>> responseMap = new ConcurrentHashMap<String, List<MassDTO>>();
	
	List<FDPMassLoad> listMassLoad = null; 

	/** The fdp service provisioning. */
	@Resource(lookup = "java:app/fdpBusiness-1.0/ServiceProvisioningImpl")
	private ServiceProvisioning serviceProvising;

	/**
	 * This method call for provision or deProvision and get all record of
	 * current date.
	 */
	@Override
	public void massProvisionExecution() {
		/** For Provisioning **/
		Integer maxRecordFetchSize;
		FDPCache<FDPAppBag, Object> applicationConfigCache = null;
		try {
			maxRecordFetchSize = Integer.parseInt(PropertyUtils.getProperty(FDPConstant.MASS_PROVISIONING_MAX_QUEUE_SIZE));
			LOGGER.info("Getting Massprovision records from database.. count " + maxRecordFetchSize);
			listMassLoad = massLoadDao.getAllMassInfoFromDB(maxRecordFetchSize);
			applicationConfigCache = ApplicationConfigUtil.getApplicationConfigCache();

		} catch (ParseException pe) {
			LOGGER.error(
					"Exception for getting mass data for bulk provision !!! ",
					pe);
			return;
		} catch (ExecutionFailedException efe) {
			LOGGER.error(
					"Not able to reterive application cache, mass provisioning failed ",
					efe);
			return;
		}

		FDPCircle fdpCircle = null;

		if (listMassLoad != null && listMassLoad.size() > 0
				&& applicationConfigCache != null) {
			LOGGER.info("Mass data to be provisioned ============ Total Records >>>>>>>>>>>>" + listMassLoad.size());
			int massListSize = listMassLoad.size();
			String circleCode;
			boolean isValid = true;
			StringBuilder validationError = new StringBuilder();
			FDPCacheable[] fdpCacheAble = null;
			FDPServiceProvSubType serviceProv = null;
			
			/*
			 *  Specifying construct argument values for Thread Pool Executor
			 *  Initial values are sufficient for records less than 2000 records
			 */
			int coreSize = 3;
			int maxSize = 10;
			int queueSize = 1000;
			queueSize = (maxRecordFetchSize > massListSize ? massListSize : maxRecordFetchSize);
			
			if(massListSize > 2000 && massListSize <= 5000){
				coreSize = 10;
				maxSize = 20;
			}
			else if (massListSize > 5000 && massListSize <= 10000) {
				coreSize = 10;
				maxSize = 20;
				//queueSize = 1000;
			} else if (massListSize > 10000) {
				coreSize = 20;
				maxSize = 30;
				//queueSize = 2000;
			}
			/*
			 * Initialization of MassProvision Executer service class based on
			 * load
			 */

			massProvisioningServiceImpl = new MassProvisioningExecutor(
					coreSize, maxSize, queueSize, this);

			for (int i = 0; i < massListSize; i++) {

					// Method call for provision or Deprovisions of Mass
					FDPRequest fdpRequest;
					Object[] serviceProvObj = new Object[2];
					try {
						boolean isBuyForOtherEnabled = false;
						fdpCacheAble = null;
						serviceProv = null;
						// MassProvisionWorker.postFulfillmentUrl(listMassLoad.get(i),
						// responseMap);
						String msisdn = listMassLoad.get(i).getMsisdn()
								.toString();
						fdpCircle = CircleCodeFinder.getFDPCircleByMsisdn(
								msisdn, applicationConfigCache);

						circleCode = fdpCircle.getCircleCode();

						String productCode = listMassLoad.get(i)
								.getProductCode().toString();
						Boolean sendSms = listMassLoad.get(i).getSendSMS();
						fdpRequest = this.createFDPRequest(msisdn, fdpCircle);				
						
						final Logger circleLogger = LoggerUtil
								.getSummaryLoggerFromRequest(fdpRequest);

						if (sendSms) {
							sendSms = null;
						}
						((FDPRequestImpl) fdpRequest)
								.setToSendNotification(sendSms);
						serviceProv = getOpertionType(productCode);
						String productId = generateProductId(productCode,fdpCircle);

						if (null != serviceProv && null != productId) {
							LOGGER.debug("Service Provisioning for the Product is :: "
									+ serviceProv);
							fdpCacheAble = ServiceProvisioningUtil
									.getProductAndSP(fdpRequest,
											Long.valueOf(productId),
											serviceProv);
						}

						if (fdpCacheAble != null) {
							Product cacheable = (Product) fdpCacheAble[0];
							if (cacheable instanceof Product) {
								final Product product = (Product) cacheable;
								isBuyForOtherEnabled = (null != product
										.getAdditionalInfo(ProductAdditionalInfoEnum.PRODUCT_FOR_OTHER)) ? Boolean
										.parseBoolean(product
												.getAdditionalInfo(ProductAdditionalInfoEnum.PRODUCT_FOR_OTHER))
										: false;
							}
						}
						
						if (listMassLoad.get(i).getBeneficiaryMsisdn() != null
								&& (!listMassLoad.get(i).getBeneficiaryMsisdn()
										.toString().equals("null"))
								&& listMassLoad.get(i).getBeneficiaryMsisdn()
										.toString().length() > 0) {
							if (isBuyForOtherEnabled) {
								((FDPRequestImpl) fdpRequest)
										.putAuxiliaryRequestParameter(
												AuxRequestParam.BENEFICIARY_MSISDN,
												listMassLoad.get(i)
														.getBeneficiaryMsisdn()
														.toString());
							} else {
								isValid = false;
								validationError = validationError
										.append("Buy for other is not enabled for this product");
							}
						}
						String requestDescription = new StringBuilder(
								FDPConstant.REQUEST_ID)
								.append(FDPConstant.LOGGER_KEY_VALUE_DELIMITER)
								.append(fdpRequest.getOriginTransactionID()
										.toString())
								.append(FDPConstant.LOGGER_DELIMITER)
								.append(FDPConstant.MSISDN)
								.append(FDPConstant.LOGGER_KEY_VALUE_DELIMITER)
								.append(msisdn)
								.append(FDPConstant.LOGGER_DELIMITER)
								.append(FDPConstant.PRODUCT_ID)
								.append(FDPConstant.LOGGER_KEY_VALUE_DELIMITER)
								.append(productCode)
								.append(FDPConstant.LOGGER_DELIMITER)
								.append(FDPConstant.CIRCLE_CODE)
								.append(FDPConstant.LOGGER_KEY_VALUE_DELIMITER)
								.append(circleCode)
								.append(FDPConstant.LOGGER_DELIMITER)
								.append("CHARGING_MODE")
								.append(FDPConstant.LOGGER_DELIMITER)
								.append(listMassLoad.get(i).getChargingMode())
								.toString();

						FDPLogger.debug(circleLogger, getClass(),
								"massLoadRequestLog()",
								requestDescription.toString());

						if (null != listMassLoad.get(i).getAction()) {
							((FDPRequestImpl) fdpRequest)
									.putAuxiliaryRequestParameter(
											AuxRequestParam.ACTION,
											listMassLoad.get(i).getAction());
							/*
							 * LOGGER.debug("ACTION:" +
							 * listMassLoad.get(i).getAction());
							 */
						}
						if (null != listMassLoad.get(i).getSplitNo()) {
							((FDPRequestImpl) fdpRequest)
									.putAuxiliaryRequestParameter(
											AuxRequestParam.SPLIT_NUMBER,
											listMassLoad.get(i).getSplitNo()
													.toString());
							/*
							 * LOGGER.debug("SPLIT_NUMBER:" +
							 * listMassLoad.get(i).getSplitNo());
							 */
						}
						if (null != listMassLoad.get(i).getSkipCharging()) {
							((FDPRequestImpl) fdpRequest)
									.putAuxiliaryRequestParameter(
											AuxRequestParam.SKIP_CHARGING,
											listMassLoad.get(i)
													.getSkipCharging());
							/*
							 * LOGGER.debug("SKIP_CHARGING:" +
							 * listMassLoad.get(i).getSkipCharging());
							 */
						}
						if (null != listMassLoad.get(i).getChargingMode()) {
							((FDPRequestImpl) fdpRequest)
									.putAuxiliaryRequestParameter(
											AuxRequestParam.PAYMENT_MODE,
											listMassLoad.get(i)
													.getChargingMode());
							/*
							 * LOGGER.debug("PAYMENT_SOURCE:" +
							 * listMassLoad.get(i).getChargingMode());
							 */
						}
						try {
							if (null != listMassLoad.get(i).getProductCost()) {
								((FDPRequestImpl) fdpRequest)
										.putAuxiliaryRequestParameter(
												AuxRequestParam.PRODUCT_COST,
												listMassLoad.get(i)
														.getProductCost()
														.toString());
								/*
								 * LOGGER.debug("PRODUCT_COST:" +
								 * listMassLoad.get(i).getProductCost());
								 */
							}
						} catch (Exception e) {
							LOGGER.error("Product cost is not present = ", e);
						}

						serviceProvObj = new Object[] { serviceProv,fdpCacheAble[1] };

						if (serviceProvObj[1] != null && isValid) {

							LOGGER.info("Calling executeMassProv ="+listMassLoad.get(i).toString());
							massProvisioningServiceImpl
									.executeMassProvisioning(
											listMassLoad.get(i), responseMap,fdpRequest);

						} else {
							LOGGER.error("SP for the product is not found or provision request is not valid >>"
									+ validationError.toString());
						}
					} catch (ExecutionFailedException e1) {
							LOGGER.error(
								"fdpResponse error while executing  mass provision :: ",
								e1);
				} catch (Exception e) {
					// e.printStackTrace();
					LOGGER.error(
							"Exception occurred for massData provisioning - deprovisioing request processing ",
							e);
				}
			}
			massProvisioningServiceImpl.addShutdownHook();
		} else {
			LOGGER.info("There is no data present in database to be provisioned.");
		}
	}

	/**
	 * This method is responsible to crate the mass provision result sheet and
	 * update the database rows
	 * 
	 * @param listMassLoad
	 * @param listMassDTOResponseObj
	 */
	public void createMassProvisionResultSheet() {
		
		// Export response file excel files
		List<MassDetailsDTO> listMassDetailsDTO = new ArrayList<MassDetailsDTO>();
		List<MassDTO> listMassDTOResponseObj = new ArrayList<MassDTO>();
		Set<String> keySetFileNo = null;
		
		LOGGER.info("Mass provisioing execution completed and reponse received ["
				+ responseMap.size() + "]");
		
		
		try {
			keySetFileNo = responseMap.keySet();
			List<MassDTO> listMassDTO = null;
			MassDetailsDTO massDetailsDTOObj;
			for (String fileNo : keySetFileNo) {
				massDetailsDTOObj = new MassDetailsDTO();
				listMassDTO = new ArrayList<MassDTO>();
				listMassDTO = responseMap.get(fileNo);
				listMassDTOResponseObj.addAll(listMassDTO);
				/*
				 * LOGGER.info("excel export info File No = " + fileNo +
				 * " listMassDTO= " + listMassDTO.toString());
				 */
				massDetailsDTOObj = populateExcelData(listMassDTO, fileNo);
				
				if (massDetailsDTOObj != null)
					listMassDetailsDTO.add(massDetailsDTOObj);
			}
			responseMap.clear();
		} catch (Exception e) {
			// e.printStackTrace();
			LOGGER.error("Mass provision result sheet could not be created ", e);
		}
		// Save MassLoad response with success or fail
		try {
			/*
			 * LOGGER.debug("Request Entity = " + listMassLoad);
			 * LOGGER.debug("Response MassDTO = " + listMassDTOResponseObj);
			 */
			massLoadDao.updateResponseProvDeProv(listMassLoad,
					listMassDTOResponseObj);

		} catch (FDPConcurrencyException e1) {
			LOGGER.error("Mass provision result could not be updated into Database ", e1);
		}
		try {
			
			massLoadDetailDao.saveMassSuccessFailureDetails(listMassDetailsDTO);
			
			LOGGER.debug("Updating the status into database[fdp_mass_rebate_details]");
			
		} catch (Exception e) {
			LOGGER.error(
					"Error in writing Mass Provision/Deprovision result file. ",
					e);
		}
	}

	/**
	 * Gets the operationType on the basis of productId.
	 * 
	 * @param productId
	 * @return
	 */
	private FDPServiceProvSubType getOpertionType(String productId) {
		
		FDPServiceProvSubType serviceProv = null;
		
		if (null != productId && productId.length() > 0) {
			if (productId.contains("NACT")) {
				serviceProv = ExternalSystemActionServiceProvisingMapping.BUY
						.getServiceProvtype();
			} else if (productId.contains("RACT")) {
				serviceProv = ExternalSystemActionServiceProvisingMapping.BUY_RECURRING
						.getServiceProvtype();
			} else if (productId.contains("SACT")) {
				serviceProv = ExternalSystemActionServiceProvisingMapping.BUY_SPLIT
						.getServiceProvtype();
			} else if (productId.contains("DACT")) {
				serviceProv = ExternalSystemActionServiceProvisingMapping.PRODUCT_DEACTIVATION_RS
						.getServiceProvtype();
				if (serviceProv == null) {
					serviceProv = ExternalSystemActionServiceProvisingMapping.PRODUCT_DEACTIVATION_PAM
							.getServiceProvtype();
				}
			} else {
				LOGGER.error("Invalid Product Code :: " + productId);
				return null;
			}
		}
		return serviceProv;
	}

	/**
	 * Return productId on the basis of productCode.
	 * 
	 * @param productCode
	 * @return
	 * @throws ExecutionFailedException 
	 */
	private String generateProductId(String productCode, FDPCircle fdpCircle) throws ExecutionFailedException {
		
		if (null != productCode && productCode.contains("_")) {
			String productDMCode=null;
			String[] productArr = productCode.split("_");
			if (null != productArr && productArr.length>3){
				return productArr[3];
			}
			else if(null != productArr && productArr.length>1){
				//If product is provisioning using product name
				
				final FDPCacheable fdpCacheable = ApplicationConfigUtil.getMetaDataCache().getValue(
						new FDPMetaBag(fdpCircle, ModuleType.PRODUCT_NAME_ID_MAP, productArr[1]));
				
				//FDPNode fdpNode = RequestUtil.checkInstanceForNode(fdpCacheable);
				
				if (null != fdpCacheable && fdpCacheable instanceof ProductNameCacheImpl) {
					final ProductNameCacheImpl productNameCache = (ProductNameCacheImpl) fdpCacheable;
					productDMCode = productNameCache.getProductIdValue();
				}
				if(null != productDMCode && productDMCode.contains("_"))
				{
					String[] productNameArr = productDMCode.split("_");
					int arrLength = productNameArr.length;
					LOGGER.debug("Product ID :: " + productNameArr[arrLength-1]);
					return productNameArr[arrLength-1];
				}
		
			}
			return null;
		} else {
			LOGGER.debug("Invalid Product Code :: " + productCode);
			return null;
		}
	}

	/**
	 * create excel file with header.
	 * 
	 * @param listMassDTO
	 *            the listMassDTO
	 * @param fileNo
	 *            the fileNo
	 * @throws FDPServiceException
	 */
	private MassDetailsDTO populateExcelData(List<MassDTO> listMassDTO,
			String fileNo) throws FDPServiceException {

		LOGGER.info("Inside populateExcelData for fileNo = " + fileNo);

		final ExcelData excelData = new ExcelData();
		excelData.setRecordsList(listMassDTO);
		final List<String> columnNames = FDPStringUtil
				.exportHeaders(FDPConstant.HEADERS_MASS_LOADING,
						FDPConstant.HEADERS_DELIMITER);
		excelData.setColumnNames(columnNames);
		excelData.setFileName(FDPConstant.MASS_LOADING_FILENAME);
		MassDetailsDTO massDetailsDTOObj = createExcel(excelData, fileNo);

		return massDetailsDTOObj;
	}

	/**
	 * Create excel file with fileNo and populated data.
	 * 
	 * @param populateExcelData
	 *            the populateExcelData.
	 * @param fileNo
	 *            the fileNo.
	 */
	private MassDetailsDTO createExcel(ExcelData populateExcelData,
			String fileNo) {

		LOGGER.debug("In method createExcel excel file !!! ");

		HSSFWorkbook workbook = new HSSFWorkbook();
		HSSFSheet sheet = workbook.createSheet("Sheet");
		int rownum = 0, colnum = 0;
		MassDetailsDTO massDetailsDTOObj = new MassDetailsDTO();
		int statusFail = 0, statusSuccess = 0;

		try {
			List<String> columnNames = populateExcelData.getColumnNames();
			// Add header
			HSSFRow header = sheet.createRow(rownum++);
			for (String colName : columnNames) {
				header.createCell(colnum++).setCellValue(colName);
				;
			}

			int totalRow = populateExcelData.getRecordsList().size();
			LOGGER.debug("Total row = " + totalRow + " total column = "
					+ colnum);

			@SuppressWarnings("unchecked")
			List<MassDTO> recordsList = (List<MassDTO>) populateExcelData
					.getRecordsList();
			for (MassDTO massDTO : recordsList) {
				HSSFRow row = sheet.createRow(rownum++);
				row.createCell(0).setCellValue(massDTO.getMassId());
				row.createCell(1).setCellValue(massDTO.getMsisdn());
				row.createCell(2).setCellValue(massDTO.getProductCode());
				row.createCell(3).setCellValue(massDTO.getChargingMode());
				row.createCell(4).setCellValue(massDTO.getSendSMS());
				// New added field for response file
				if (massDTO.getSplitNumber() != null
						&& massDTO.getSplitNumber().length() > 0)
					row.createCell(5).setCellValue(massDTO.getSplitNumber());
				else
					row.createCell(5).setCellValue("");

				if (massDTO.getBeneficiaryMsisdn() != null
						&& massDTO.getBeneficiaryMsisdn().length() > 0)
					row.createCell(6).setCellValue(
							massDTO.getBeneficiaryMsisdn());
				else
					row.createCell(6).setCellValue("");

				if (massDTO.getProductCost() != null
						&& massDTO.getProductCost().length() > 0)
					row.createCell(7).setCellValue(massDTO.getProductCost());
				else
					row.createCell(7).setCellValue("");

				if (massDTO.getSkipCharging() != null
						&& massDTO.getSkipCharging().length() > 0)
					row.createCell(8).setCellValue(massDTO.getSkipCharging());
				else
					row.createCell(8).setCellValue("");

				if (massDTO.getAction() != null
						&& massDTO.getAction().length() > 0)
					row.createCell(9).setCellValue(massDTO.getAction());
				else
					row.createCell(9).setCellValue("");

				if (massDTO.getStatus().equalsIgnoreCase(
						Status.FAILURE.getStatusText())) {
					statusFail++;
				} else if (massDTO.getStatus().equalsIgnoreCase(
						Status.SUCCESS.getStatusText())) {
					statusSuccess++;
				}
				row.createCell(10).setCellValue(massDTO.getStatus());
				row.createCell(11).setCellValue(
						massDTO.getResultCodeExtension());

			}
			final String massLoadingFilePath = PropertyUtils
					.getProperty(MassLoadConstant.MASS_LOADING_FILE_PATH);

			String responseFilePath = massLoadingFilePath
					+ populateExcelData.getFileName() + "_" + fileNo + ".xls";

			LOGGER.debug("Response file path :: " + responseFilePath);

			FileOutputStream out = new FileOutputStream(new File(
					responseFilePath));
			workbook.write(out);
			out.close();

			LOGGER.debug("Mass response file created for file No :: " + fileNo);

			massDetailsDTOObj.setFileNo(fileNo);
			massDetailsDTOObj.setNoOfSuccess(statusSuccess);
			massDetailsDTOObj.setNoOfFailure(statusFail);

		} catch (Exception e) {
			// e.printStackTrace();
			LOGGER.error(
					"Exception in creating mass provision/deprovision response file >> ",
					e);
			massDetailsDTOObj = null;
		}
		return massDetailsDTOObj;
	}

	/**
	 * generateTransactionId
	 * 
	 * @return Long.
	 */
	protected Long generateTransactionId() {
		return generatorService.generateTransactionId();
	}

	/**
	 * Creates the fdp request.
	 * 
	 * @param msisdn
	 *            the msisdn
	 * @param fdpCircle
	 *            the fdp circle
	 * @return the fDP request
	 * @throws ExecutionFailedException
	 *             the execution failed exception
	 * @throws NamingException
	 *             the naming exception
	 */
	public FDPRequest createFDPRequest(final String msisdn,
			final FDPCircle fdpCircle) throws ExecutionFailedException,
			NamingException {
		final Long transactionNumber = generateTransactionId();
		return RequestUtil.getRequest(msisdn, transactionNumber, fdpCircle,
				ChannelType.USSD);
	}
}
