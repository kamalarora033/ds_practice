package com.ericsson.fdp.business.batchjob.productconfigurationsexport;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.List;

import javax.ejb.Stateless;
import javax.inject.Inject;

import ch.qos.logback.classic.Logger;

import com.ericsson.fdp.common.constants.FDPConstant;
import com.ericsson.fdp.common.dto.ProductExcelDTO;
import com.ericsson.fdp.common.exception.FDPServiceException;
import com.ericsson.fdp.common.vo.FDPCircle;
import com.ericsson.fdp.core.helper.ProductExcelDTOHelper;
import com.ericsson.fdp.core.logging.FDPLoggerFactory;
import com.ericsson.fdp.dao.dto.product.ProductReportDTO;
import com.ericsson.fdp.dao.dto.serviceprov.ServiceProvCommandDTO;
import com.ericsson.fdp.dao.dto.serviceprov.ServiceProvCommandParamDTO;
import com.ericsson.fdp.dao.entity.FDPServiceProvAddInfo;
import com.ericsson.fdp.dao.enums.EntityType;
import com.ericsson.fdp.dao.enums.SPAddInfoKeyEnum;
import com.ericsson.fdp.dao.fdpadmin.FDPProductDAO;
import com.ericsson.fdp.dao.fdpadmin.FDPServiceProvAddInfoDAO;
import com.ericsson.fdp.dao.fdpadmin.FDPServiceProvCommandDAO;

@Stateless
public class ProductConfigurationsExportServiceImpl implements ProductConfigurationsExportService {

	/** The fdp product dao. */
	@Inject
	private FDPProductDAO fdpProductDAO;

	/** The fdp service prov command dao. */
	@Inject
	private FDPServiceProvCommandDAO fdpServiceProvCommandDAO;

	/** The sp add info dao. */
	@Inject
	private FDPServiceProvAddInfoDAO spAddInfoDAO;

	@Override
	public List<ProductExcelDTO> getProductExcelDTOs(final FDPCircle fdpCircle) throws FDPServiceException {
		final Logger circleLogger = FDPLoggerFactory.getCircleAdminLogger(fdpCircle.getCircleName());
		final List<ProductReportDTO> products = fdpProductDAO.getCircleProductsForExcel(fdpCircle.getCircleId(),
				circleLogger);
		List<ProductExcelDTO> productExcelDTOCompleteList = new ArrayList<ProductExcelDTO>();
		List<ProductExcelDTO> productExcelDTOList = null;
		try {
			int sno = 0;
			for (final ProductReportDTO productReportDTO : products) {
				productExcelDTOList = ProductExcelDTOHelper.populateExcelDTO(productReportDTO, sno);
				//ProductExcelDTO productExcelDTO = ProductExcelDTOHelper.populateExcelDTO(productReportDTO, sno);

				final StringBuilder others = new StringBuilder();
				if (productReportDTO.getSpConstraintSubStepList().size() > 0) {
					final List<ServiceProvCommandDTO> spCommandDTOList = fdpServiceProvCommandDAO
							.getAllServiceProvStepCommand(productReportDTO.getSpConstraintSubStepList(),
									EntityType.SERVICE_PROV_SUB_STEP.getEntityType(), null);
					for (final ServiceProvCommandDTO spCommandDTO : spCommandDTOList) {
						final String commandName = spCommandDTO.getCommandCircleDTO().getCommandDTO().getCommandName();
						if (commandName.equalsIgnoreCase("Perform Refill")) {
							for (final ServiceProvCommandParamDTO spParam : spCommandDTO.getSpCommandParamsList()) {
								for(ProductExcelDTO productExcelDTO : productExcelDTOList){
									/*if (spParam.getFullQualifiedPath().equalsIgnoreCase("transactionCode")) {
										productExcelDTO.setTransactionCode(spParam.getDefaultValueSp());
									} else*/ /*if (spParam.getFullQualifiedPath().equalsIgnoreCase("transactionAmount")) {
										productExcelDTO.setTransactionAmount(spParam.getDefaultValueSp());
									}
									else if(spParam.getFullQualifiedPath().equalsIgnoreCase("transactionCurrency"))
									{
										productExcelDTO.setTransactionCurrency(spParam.getDefaultValueSp());
									}*/
									
									/*else if (spParam.getFullQualifiedPath().equalsIgnoreCase("refillProfileID")) {
										productExcelDTO.setRefillProfileID(spParam.getDefaultValueSp());
									}*/
								}
							}
						} else {
							if (commandName.equalsIgnoreCase("PAM")) {
								for (final ServiceProvCommandParamDTO spParam : spCommandDTO.getSpCommandParamsList()) {
									if (spParam.getFullQualifiedPath()
											.matches("pamInformationList.[0-9]+.pamServiceID")) {
										others.append(spParam.getFullQualifiedPath()).append(FDPConstant.EQUAL)
										.append(spParam.getDefaultValueSp()).append(FDPConstant.NEWLINE);
									}
								}
							} else if (commandName.equalsIgnoreCase("Offer Attributes")) {
								for (final ServiceProvCommandParamDTO spParam : spCommandDTO.getSpCommandParamsList()) {
									if (spParam.getFullQualifiedPath().equalsIgnoreCase("offerId")) {
										others.append(spParam.getFullQualifiedPath()).append(FDPConstant.EQUAL)
										.append(spParam.getDefaultValueSp()).append(FDPConstant.NEWLINE);
									}
								}
							} else if (commandName.equalsIgnoreCase("FaF List")) {
								for (final ServiceProvCommandParamDTO spParam : spCommandDTO.getSpCommandParamsList()) {
									if (spParam.getFullQualifiedPath().matches("fafInformation.fafIndicator.[0-9]+")) {
										others.append(spParam.getFullQualifiedPath()).append(FDPConstant.EQUAL)
										.append(spParam.getDefaultValueSp()).append(FDPConstant.NEWLINE);
									}
								}
							} else if (commandName.equalsIgnoreCase("Service Offering")) {
								for (final ServiceProvCommandParamDTO spParam : spCommandDTO.getSpCommandParamsList()) {
									if (spParam.getFullQualifiedPath().matches(
											"serviceOfferings.[0-9]+.serviceOfferingID")) {
										others.append(spParam.getFullQualifiedPath()).append(FDPConstant.EQUAL)
										.append(spParam.getDefaultValueSp()).append(FDPConstant.NEWLINE);
									}
								}
							} else if (commandName.equalsIgnoreCase("Community")) {
								for (final ServiceProvCommandParamDTO spParam : spCommandDTO.getSpCommandParamsList()) {
									if (spParam.getFullQualifiedPath().matches(
											"communityInformationNew.[0-9]+.communityID")) {
										others.append(spParam.getFullQualifiedPath()).append(FDPConstant.EQUAL)
										.append(spParam.getDefaultValueSp()).append(FDPConstant.NEWLINE);
									}
								}
							}
						}
					}
				}
				if (!productReportDTO.getSpServiceStepList().isEmpty()) {
					final List<FDPServiceProvAddInfo> addInfoList = spAddInfoDAO
							.getSPAddInfosForStepIds(productReportDTO.getSpServiceStepList());
					for (final FDPServiceProvAddInfo addInfo : addInfoList) {
						if (SPAddInfoKeyEnum.SP_SERVICE_MODE.getValue().equals(addInfo.getKey())) {
							String value = addInfo.getValue();
							if (FDPConstant.TWO_G.equals(value)) {
								value = FDPConstant.TWO_G_NUM;
							} else if (FDPConstant.THREE_G.equals(value)) {
								value = FDPConstant.THREE_G_NUM;
							}
							others.append(SPAddInfoKeyEnum.SP_SERVICE_MODE.name()).append(FDPConstant.EQUAL)
							.append(value).append(FDPConstant.NEWLINE);
							break;
						}
					}
				}

				/*for(ProductExcelDTO productExcelDTO : productExcelDTOList){
					productExcelDTO.setOthers(others.toString());
				}

				sno = productExcelDTOList.get(productExcelDTOList.size()-1).getSrlNo();*/
				productExcelDTOCompleteList.addAll(productExcelDTOList);
			}
		} catch (final ParseException e) {
			throw new FDPServiceException();
		}
		return productExcelDTOCompleteList;
	}
}
