package com.ericsson.fdp.business.cache.datageneration.service.impl;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.Resource;
import javax.ejb.Stateless;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ericsson.fdp.FDPCacheable;
import com.ericsson.fdp.business.cache.datageneration.service.ProductDataService;
import com.ericsson.fdp.business.charging.Account;
import com.ericsson.fdp.business.charging.ChargingConditionStep;
import com.ericsson.fdp.business.charging.ProductCharging;
import com.ericsson.fdp.business.charging.impl.AIRCharging;
import com.ericsson.fdp.business.charging.impl.DABasedAirCharging;
import com.ericsson.fdp.business.charging.impl.DedicatedAccount;
import com.ericsson.fdp.business.charging.impl.FixedCharging;
import com.ericsson.fdp.business.charging.impl.MainAccount;
import com.ericsson.fdp.business.charging.impl.RecurringCharging;
import com.ericsson.fdp.business.charging.impl.VariableCharging;
import com.ericsson.fdp.business.command.param.impl.CommandParamInput;
import com.ericsson.fdp.business.enums.SharedType;
import com.ericsson.fdp.business.enums.State;
import com.ericsson.fdp.business.exception.ExpressionFailedException;
import com.ericsson.fdp.business.expression.Expression;
import com.ericsson.fdp.business.product.Product;
import com.ericsson.fdp.business.product.impl.BaseProduct;
import com.ericsson.fdp.business.product.impl.SharedAccountProduct;
import com.ericsson.fdp.business.util.ExpressionUtil;
import com.ericsson.fdp.business.vo.ProductAliasCode;
import com.ericsson.fdp.common.constants.FDPConstant;
import com.ericsson.fdp.common.dto.ProductChangeHistoryDTO;
import com.ericsson.fdp.common.enums.ChannelType;
import com.ericsson.fdp.common.enums.ExternalSystem;
import com.ericsson.fdp.common.enums.ModuleType;
import com.ericsson.fdp.common.enums.ParameterFeedType;
import com.ericsson.fdp.common.enums.Primitives;
import com.ericsson.fdp.common.enums.ProductType;
import com.ericsson.fdp.common.enums.ShareType;
import com.ericsson.fdp.common.exception.FDPServiceException;
import com.ericsson.fdp.common.vo.FDPCircle;
import com.ericsson.fdp.core.config.utils.PropertyUtils;
import com.ericsson.fdp.core.dsm.framework.service.FDPCache;
import com.ericsson.fdp.core.entity.service.EntityService;
import com.ericsson.fdp.core.serviceprovisioning.bean.FDPMetaBag;
import com.ericsson.fdp.dao.dto.UpdateCacheDTO;
import com.ericsson.fdp.dao.dto.common.charging.AIRChargingDTO;
import com.ericsson.fdp.dao.dto.common.charging.ChargingDTO;
import com.ericsson.fdp.dao.dto.common.charging.ChargingStepDTO;
import com.ericsson.fdp.dao.dto.common.charging.DeProvisionChargingDTO;
import com.ericsson.fdp.dao.dto.common.charging.FixedChargingDTO;
import com.ericsson.fdp.dao.dto.common.charging.RecurringChargingDTO;
import com.ericsson.fdp.dao.dto.common.charging.VariableChargingDTO;
import com.ericsson.fdp.dao.dto.common.constraint.ConstraintDTO;
import com.ericsson.fdp.dao.dto.dynamicMenu.BaseNodeDTO;
import com.ericsson.fdp.dao.dto.product.DAChargingDTO;
import com.ericsson.fdp.dao.dto.product.ProductDTO;
import com.ericsson.fdp.dao.entity.ExternalSystemType;
import com.ericsson.fdp.dao.enums.ActionTypeEnum;
import com.ericsson.fdp.dao.enums.ChargingActor;
import com.ericsson.fdp.dao.enums.ChargingType;
import com.ericsson.fdp.dao.enums.CommandParameterType;
import com.ericsson.fdp.dao.enums.ProductAdditionalInfoEnum;
import com.ericsson.fdp.dao.enums.ProductCategoryEnum;
import com.ericsson.fdp.dao.enums.ProductChargingType;
import com.ericsson.fdp.dao.enums.StatusCodeEnum;

/**
 * The Class ProductDataServiceImpl.
 */
@Stateless(mappedName = "productDataService")
public class ProductDataServiceImpl implements ProductDataService {

	/** The Constant LOGGER. */
	private static final Logger LOGGER = LoggerFactory
			.getLogger(ProductDataServiceImpl.class);
	
	private static final String evdsType = PropertyUtils
			.getProperty("evds.protocol.type");

	/** The fdp cache. */
	@Resource(lookup = "java:app/fdpBusiness-1.0/MetaDataCache")
	private FDPCache<FDPMetaBag, FDPCacheable> fdpCache;

	/** The entity service. */
	@Resource(lookup = "java:app/fdpCoreServices-1.0/EntityServiceImpl")
	private EntityService entityService;

	/**
	 * Initialize update product.
	 *
	 * @param productDTO
	 *            the product dto
	 * @param fdpCircle
	 *            the fdp circle
	 * @throws ExpressionFailedException
	 *             the expression failed exception
	 */
	private void initializeUpdateProduct(final ProductDTO productDTO,
			final FDPCircle fdpCircle) throws ExpressionFailedException {

		final int productCategory = productDTO.getProductInfoDTO()
				.getProductCategory();
		final Map<ChannelType, Map<ChargingType, List<ProductCharging>>> charging = new HashMap<ChannelType, Map<ChargingType, List<ProductCharging>>>(
				productDTO.getChannelChargingListMap().entrySet().size());
		final Long loyaltyPoints = productDTO.getLoyaltyPoints();
		final Long loyaltyItemCode = productDTO.getLoyaltyItemCode();
		final String connectionType = productDTO.getProductInfoDTO()
				.getConnectionType();

		State productState = null;
		if (StatusCodeEnum.ACTIVE_FOR_TEST.getStatus().equals(	
				productDTO.getProductInfoDTO().getProductStatus())) {
			productState = State.ACTIVE_FOR_TEST;
		} else if (StatusCodeEnum.ACTIVE.getStatus().equals(
				productDTO.getProductInfoDTO().getProductStatus())) {
			productState = State.ACTIVE_FOR_MARKET;
		}
		final Long productId = productDTO.getProductId();
		final String productName = productDTO.getProductInfoDTO()
				.getProductName();

		final ChannelType ussd = ChannelType.USSD;
		final ChannelType sms = ChannelType.SMS;
		final ChannelType web = ChannelType.WEB;
		final ChannelType ivr = ChannelType.IVR;
		

		//for FlyTxt and other
		final ChannelType flytxt = ChannelType.FLYTXT;
		final ChannelType toolbar = ChannelType.TOOLBAR;
		//final ChannelType lcms = ChannelType.LCMS;
		final ChannelType smartapp = ChannelType.SMARTAPP;
		final ChannelType concirgecrm = ChannelType.CRM;
		final ChannelType selfCare = ChannelType.SELFCARE;
		final ChannelType others = ChannelType.OTHERS;
		
		ProductChargingType productchargingType = getProductChargingType(productDTO);

		if (productchargingType == ProductChargingType.FIXED_AIR) {
			for (final Map.Entry<ChannelType, List<? extends ChargingDTO>> entry : productDTO
					.getChannelChargingListMap().entrySet()) {
				final ChannelType channelType = entry.getKey();
				final List<? extends ChargingDTO> channelChargingList = entry
						.getValue();
				if (null != channelChargingList
						&& !channelChargingList.isEmpty()) {
					for (final ChargingDTO chargingDTO : channelChargingList) {

						if (chargingDTO instanceof FixedChargingDTO) {
							((FixedChargingDTO) chargingDTO)
									.setPartialChargingAllowed(productDTO
											.getProductInfoDTO()
											.getIsPartialCharging());
							fixedChargingForProduct(chargingDTO, charging,
									channelType, loyaltyPoints, productDTO
									.getProductInfoDTO().getProductAdditionalInfoMap());
						} else if (chargingDTO instanceof VariableChargingDTO) {
							VariableChargingDTO variableChargingDTO = (VariableChargingDTO) chargingDTO;
							/*
							 * List<ChargingStepDTO> chargingStepList = new
							 * ArrayList<ChargingStepDTO>(); for(ChargingDTO
							 * chargingDTO2
							 * :productDTO.getChannelChargingList(channelType)){
							 * if(chargingDTO2 instanceof VariableChargingDTO){
							 * VariableChargingDTO variableChargingDTO =
							 * (VariableChargingDTO)chargingDTO2;
							 * chargingStepList
							 * .addAll(variableChargingDTO.getChargingStepsList
							 * ()); } }
							 */
							// final List<ChargingStepDTO> chargingStepList =
							// ((VariableChargingDTO)
							// productDTO.getChannelChargingList(channelType)).getChargingStepsList();

							variableChargingForProduct(chargingDTO, charging,
									channelType,
									variableChargingDTO.getChargingStepsList());
						} else if (chargingDTO instanceof RecurringChargingDTO) {
							recurringChargingForProduct(chargingDTO, charging,
									channelType);
						}
					}
				}
			}
		}
		// note in future we have to variable charging from above if statement
		// and make it more clear
		else if (productchargingType == ProductChargingType.VARIABLE_AIR) {
			variableChargingforProduct(productDTO, charging);

		}

		deProvisioningChargingForProduct(productDTO, charging);

		final Map<ChannelType, Expression> constraints = new HashMap<ChannelType, Expression>();
		final ConstraintDTO constraintDTOUssd = productDTO
				.getChannelConstraint(ussd);
		final ConstraintDTO constraintDTOSms = productDTO
				.getChannelConstraint(sms);
		final ConstraintDTO constraintDTOWeb = productDTO
				.getChannelConstraint(web);
		final ConstraintDTO constraintDTOIvr = productDTO
				.getChannelConstraint(ivr);
		
		//for FlyTxt and others
		final ConstraintDTO constraintDTOFlytxt = productDTO.getChannelConstraint(flytxt);
		final ConstraintDTO constraintDTOToolBar = productDTO.getChannelConstraint(toolbar);
		//final ConstraintDTO constraintDTOLCMS = productDTO.getChannelConstraint(lcms);
		final ConstraintDTO constraintDTOSmartApp = productDTO.getChannelConstraint(smartapp);
		final ConstraintDTO constraintDTOConcieregeCRM = productDTO.getChannelConstraint(concirgecrm);
		final ConstraintDTO constraintDTOSelfCare = productDTO.getChannelConstraint(selfCare);
		final ConstraintDTO constraintDTOOthers = productDTO.getChannelConstraint(others);
		
		if (constraintDTOUssd != null) {
			constraints.put(ussd, ExpressionUtil
					.createExpressionForSteps(constraintDTOUssd
							.getConstraintStepsList()));
		}
		if (constraintDTOSms != null) {
			constraints.put(sms, ExpressionUtil
					.createExpressionForSteps(constraintDTOSms
							.getConstraintStepsList()));
		}
		if (constraintDTOWeb != null) {
			constraints.put(web, ExpressionUtil
					.createExpressionForSteps(constraintDTOWeb
							.getConstraintStepsList()));
		}
		if (constraintDTOIvr != null) {
			constraints.put(ivr, ExpressionUtil
					.createExpressionForSteps(constraintDTOIvr
							.getConstraintStepsList()));
		}
		
		
		//for Flytxt and Others
		if(constraintDTOFlytxt != null)
		{
			constraints.put(flytxt, ExpressionUtil.createExpressionForSteps(constraintDTOFlytxt.getConstraintStepsList()));
		}
		if(constraintDTOToolBar != null)
		{
			constraints.put(toolbar, ExpressionUtil.createExpressionForSteps(constraintDTOToolBar.getConstraintStepsList()));
		}

		/*if(constraintDTOLCMS != null)
		{
			constraints.put(lcms, ExpressionUtil.createExpressionForSteps(constraintDTOLCMS.getConstraintStepsList()));
		}*/
		if(constraintDTOSmartApp != null)
		{
			constraints.put(smartapp, ExpressionUtil.createExpressionForSteps(constraintDTOSmartApp.getConstraintStepsList()));
		}
		if(constraintDTOConcieregeCRM != null)
		{
			constraints.put(concirgecrm, ExpressionUtil.createExpressionForSteps(constraintDTOConcieregeCRM.getConstraintStepsList()));
		}
		if(constraintDTOSelfCare != null)
		{
			constraints.put(selfCare, ExpressionUtil.createExpressionForSteps(constraintDTOOthers.getConstraintStepsList()));
		}		
		if(constraintDTOOthers != null)
		{
			constraints.put(others, ExpressionUtil.createExpressionForSteps(constraintDTOOthers.getConstraintStepsList()));
		}
		
		final Map<ChannelType, String> productNameOnChannel = new HashMap<ChannelType, String>();
		StringBuilder prodName = null;
		if (productDTO.getProductInfoDTO().getDynamicMenuCodeDTOForUSSD() != null
				&& productDTO.getProductInfoDTO()
						.getDynamicMenuCodeDTOForUSSD().size() > 0) {
			prodName = new StringBuilder();
			for (BaseNodeDTO product : productDTO.getProductInfoDTO()
					.getDynamicMenuCodeDTOForUSSD()) {
				prodName.append(",");
				prodName.append(product.getName());
			}
			productNameOnChannel.put(ussd, prodName.charAt(0) == ',' ? prodName
					.deleteCharAt(0).toString() : "");
		}
		if (productDTO.getProductInfoDTO().getDynamicMenuCodeDTOForSMS() != null
				&& productDTO.getProductInfoDTO().getDynamicMenuCodeDTOForSMS()
						.size() > 0) {
			prodName = new StringBuilder();
			for (BaseNodeDTO product : productDTO.getProductInfoDTO()
					.getDynamicMenuCodeDTOForSMS()) {
				prodName.append(",");
				prodName.append(product.getName());
			}
			productNameOnChannel.put(sms, prodName.charAt(0) == ',' ? prodName
					.deleteCharAt(0).toString() : "");
		}
		if (productDTO.getProductInfoDTO().getDynamicMenuCodeDTOForIVR() != null
				&& productDTO.getProductInfoDTO().getDynamicMenuCodeDTOForIVR()
						.size() > 0) {
			prodName = new StringBuilder();
			for (BaseNodeDTO product : productDTO.getProductInfoDTO()
					.getDynamicMenuCodeDTOForIVR()) {
				prodName.append(",");
				prodName.append(product.getName());
			}
			productNameOnChannel.put(ivr, prodName.charAt(0) == ',' ? prodName
					.deleteCharAt(0).toString() : "");
		}
		productNameOnChannel.put(web, productDTO.getProductInfoDTO()
				.getProductWebName());
		Product product = null;
		final ProductType productType = ProductType.getProductType(productDTO
				.getProductInfoDTO().getProductType());

		final Map<Integer, String> additionalInfoMap = productDTO
				.getProductInfoDTO().getProductAdditionalInfoMap();

		if (ProductCategoryEnum.NORMAL.getKey().intValue() == productCategory) {
			/*
			 * product = new BaseProduct(productId, productName, constraints,
			 * charging, productType, productNameOnChannel,
			 * productDTO.getProductInfoDTO().getRecurringTimes(), productState,
			 * productDTO.getProductNotificationMap(),connectionType,productDTO.
			 * getProductInfoDTO().getProductDescription(),
			 * productDTO.getProductInfoDTO
			 * ().getRecurringValue(),productDTO.getProductInfoDTO().getUnit(),
			 * productDTO.getProductInfoDTO().getIsSplit());
			 */
			
			if(null !=loyaltyItemCode && null!=loyaltyPoints)
			product = new BaseProduct(productId, productName, constraints,
					charging, productType, productNameOnChannel, productDTO
							.getProductInfoDTO().getRecurringTimes(),
					productState, productDTO.getProductNotificationMap(),
					connectionType, productDTO.getProductInfoDTO()
							.getRecurringValue(), productDTO
							.getProductInfoDTO().getUnit(), productDTO
							.getProductInfoDTO().getProductDescription(),
					productDTO.getProductInfoDTO().getIsSplit(), shareTypeProduct(productDTO.getProductInfoDTO().getShareType()), productDTO.getChargesType(), productDTO.getCharges(),
					productDTO.getProductInfoDTO().getIsFafType(),productDTO.getProductInfoDTO().getOnNet(), productDTO.getProductInfoDTO().getOffNet(),
					productDTO.getProductInfoDTO().getInternational(), productDTO.getProductInfoDTO().getProductIdValue(),String.valueOf(loyaltyItemCode),String.valueOf(loyaltyPoints));
			else
				product = new BaseProduct(productId, productName, constraints,
						charging, productType, productNameOnChannel, productDTO
								.getProductInfoDTO().getRecurringTimes(),
						productState, productDTO.getProductNotificationMap(),
						connectionType, productDTO.getProductInfoDTO()
								.getRecurringValue(), productDTO
								.getProductInfoDTO().getUnit(), productDTO
								.getProductInfoDTO().getProductDescription(),
						productDTO.getProductInfoDTO().getIsSplit(), shareTypeProduct(productDTO.getProductInfoDTO().getShareType()), productDTO.getChargesType(), productDTO.getCharges(),
						productDTO.getProductInfoDTO().getIsFafType(),productDTO.getProductInfoDTO().getOnNet(), productDTO.getProductInfoDTO().getOffNet(),
						productDTO.getProductInfoDTO().getInternational(), productDTO.getProductInfoDTO().getProductIdValue());
		} else {
			final String noOfConsumers = additionalInfoMap
					.get(ProductAdditionalInfoEnum.NO_OF_CONSUMERS.getKey());
			final String type = additionalInfoMap
					.get(ProductAdditionalInfoEnum.SHARED_TYPE.getKey());
			SharedType sharedType = null;
			if (type != null && !type.isEmpty()) {
				sharedType = SharedType.findBySharedType(type);
			}
			final SharedAccountProduct sharedAccountProduct = new SharedAccountProduct(
					productId, productName, constraints, charging,
					noOfConsumers != null && !noOfConsumers.isEmpty() ? Integer
							.valueOf(noOfConsumers) : null, sharedType,
					productDTO.getProductInfoDTO().getValidityPeriod(),
					productType, productNameOnChannel, productDTO
							.getProductInfoDTO().getRecurringTimes(),
					productState, productDTO.getProductNotificationMap());
			sharedAccountProduct.addProductMetaData("Shared", "True");
			sharedAccountProduct.addProductMetaData("Type",
					sharedType.getSharedType());

			product = sharedAccountProduct;
		}

		// Populate all other Add info in product DTO
		this.populateAddInfoMap(product, additionalInfoMap);

		final FDPCircle fdpCircleToSet = null != fdpCircle ? fdpCircle
				: productDTO.getFdpCircle();

		if (productDTO.getProductInfoDTO().getDynamicMenuCodeDTOForIVR() != null
				&& productDTO.getProductInfoDTO().getDynamicMenuCodeDTOForIVR()
						.size() > 0) {
			initializeUpdateProductAliasCache(fdpCircleToSet, productDTO
					.getProductInfoDTO().getDynamicMenuCodeDTOForIVR().get(0)
					.getCode(), productDTO.getProductId(),
					ExternalSystem.IVR_TYPE);
		}

		if (additionalInfoMap.get(ProductAdditionalInfoEnum.ALIAS_CODE_PRODUCT
				.getKey()) != null) {
			initializeUpdateProductAliasCache(fdpCircleToSet,
					additionalInfoMap
							.get(ProductAdditionalInfoEnum.ALIAS_CODE_PRODUCT
									.getKey()), productDTO.getProductId(),
					ExternalSystem.AIR);
		}

		if (additionalInfoMap.get(ProductAdditionalInfoEnum.RECURRING_PAM_ID
				.getKey()) != null) {
			final String pamID = FDPConstant.PAM_ID_INCACHE
					+ additionalInfoMap
							.get(ProductAdditionalInfoEnum.RECURRING_PAM_ID
									.getKey());
			initializeUpdateProductAliasCache(fdpCircleToSet, pamID,
					productDTO.getProductId(), ExternalSystem.AIR);
		}

		if (productDTO.getRsServiceId() != null) {
			initializeUpdateProductAliasCache(fdpCircleToSet,
					productDTO.getRsServiceId(), productDTO.getProductId(),
					ExternalSystem.RS);
		}

		final FDPMetaBag metaBag = new FDPMetaBag(fdpCircleToSet,
				ModuleType.PRODUCT, productDTO.getProductId().toString());
		fdpCache.putValue(metaBag, product);

		// logs added at info level
		final StringBuilder messageStr = new StringBuilder();
		messageStr
				.append(" Product information updated in cache successfully.")
				.append(System.lineSeparator())
				.append("General Information: Id = ")
				.append(product.getProductId()).append(" Product Name = ")
				.append(product.getProductName()).append(" Type = ")
				.append(product.getProductType()).append(" Category = ")
				.append(ProductCategoryEnum.getValueForKey(productCategory))
				.append(" State = ").append(productState);

		final List<ProductCharging> chargingListSMS = product
				.getProductCharging(ChannelType.SMS, ChargingType.NORMAL);
		if (chargingListSMS != null
				&& !chargingListSMS.isEmpty()
				&& chargingListSMS.get(FDPConstant.ZERO) instanceof VariableCharging) {
			messageStr.append(" Charging type : Variable ");
		} else if (chargingListSMS != null
				&& !chargingListSMS.isEmpty()
				&& chargingListSMS.get(FDPConstant.ZERO) instanceof FixedCharging) {
			messageStr.append(" Charging type : Fixed ");
		}
		LOGGER.info("{} : {} : {}", new Object[] { this.getClass().getName(),
				"initializeUpdateProduct", messageStr });
	}

	/**
	 * This method returns the shareType value on the basis of key.
	 * @param shareType
	 * @return
	 * 
	 * @author evasaty
	 */
	private String shareTypeProduct(Short shareType) {
		if(null!=ShareType.getValueForKey(shareType)){
			return ShareType.getValueForKey(shareType);
		}
		else{
			return "None";			
		}
	}

	/**
	 * @author ehlnopu ashish kumar e
	 * */
	private void variableChargingforProduct(ProductDTO productDTO,
			Map<ChannelType, Map<ChargingType, List<ProductCharging>>> productChargingTypeChargingListMap)
			throws ExpressionFailedException {

		Expression expressionForStepsProductCharging = null;
		//external system type used to hold the default charging
	
		/*VariableChargingDTO chargingDTO = (VariableChargingDTO) productDTO
				.getChannelChargingList(ChannelType.SMS).get(0);*/
		Map<ChannelType, List<? extends ChargingDTO>> channelbasedls = productDTO
				.getChannelChargingListMap();
	/*	List<ChargingStepDTO> chargingSteplst = chargingDTO
				.getChargingStepsList();*/
		List<ChargingConditionStep> chargingConditionSteps = new ArrayList<ChargingConditionStep>();
		List<ProductCharging> fixedcharginglist=new ArrayList<ProductCharging>();
		Boolean ispartialCharging;
		
		// BasedOn ChannelType We have to create charging
		for (Map.Entry<ChannelType, List<? extends ChargingDTO>> entry : channelbasedls
				.entrySet()) {
			
			if(entry!=null)
			{
			VariableChargingDTO chargingDTO = (VariableChargingDTO) productDTO
					.getChannelChargingList(entry.getKey()).get(0);
			List<ChargingStepDTO> chargingSteplst = chargingDTO
					.getChargingStepsList();
			// we use the chargingList to manage if more then one step is there
			List<ProductCharging> chargingList = this.getChannelChargingTypeList(entry.getKey(),
							productChargingTypeChargingListMap,
							ChargingType.NORMAL);
			ProductCharging productChargingVariable = isStepAlreadyCreated(chargingList);
			//	this will execute when there is only one substep
			if (productChargingVariable == null) {
				for (ChargingStepDTO chargingstepdto : chargingSteplst) {
					Long amountCharged = null;
					List<DAChargingDTO> chargingDTOs = null;
					ChargingConditionStep chargingConditionStep = null;
					String chargingsystemstr = null;
					ProductCharging fixedCharging = null;
					ExternalSystemType externalsystemtype = null;

					expressionForStepsProductCharging = ExpressionUtil.createExpressionForSubStep(chargingstepdto
									.getSubStepsList());

					chargingsystemstr = chargingstepdto.getChargingSystemStr();
					chargingDTOs = chargingstepdto.getDaChargingDTOs();
					chargingConditionStep = new ChargingConditionStep(
							expressionForStepsProductCharging, new AIRCharging(
									amountCharged), 1L,
							FDPConstant.AIR_CHARCHING_USSD);
					// for each type of charging system we have to create the
					// charging at present system us 0,13,14 defined in
					// ExternalSystemType.java
					final CommandParamInput commandParamInput = this.getCommandParamInputObject(ChargingActor.REQUEST_SUBSCRIBERNUMBER.getValue());
					if(chargingsystemstr!=null)
					for (String chargingstepid : chargingsystemstr.split(FDPConstant.COMMA)) {
						ispartialCharging=false;
					if (chargingstepid!=null && Integer.toString(ExternalSystemType.LOYALTY_TYPE.getKey()).equals(chargingstepid)) {
							externalsystemtype = ExternalSystemType.LOYALTY_TYPE;
							//amountCharged = chargingstepdto.getLoyalitypoint();
							amountCharged = chargingstepdto.getLoyalityItemCode();
							fixedCharging = new FixedCharging(new AIRCharging(amountCharged),getChargingCommand(externalsystemtype),
									commandParamInput,getExternalSystem(externalsystemtype));
					
						} else if (chargingstepid!=null && Integer.toString(ExternalSystemType.AIR_TYPE.getKey()).equals(chargingstepid)) {
							externalsystemtype = ExternalSystemType.AIR_TYPE;
							
							amountCharged = ((AIRChargingDTO) chargingstepdto.getChargingSystem()).getAmountCharged();
							if(chargingstepdto.getDaChargingDTOs()!=null)
							{//this is for DA Based Charging
							
								
								fixedCharging=new DABasedAirCharging(new AIRCharging(amountCharged), getChargingCommand(externalsystemtype), commandParamInput, 
										convertToAccountList(chargingDTOs), getPartialCharging(chargingstepdto.getIsPartial()), getExternalSystem(externalsystemtype));
							}
							else
							{
							fixedCharging = new FixedCharging(new AIRCharging(amountCharged),getChargingCommand(externalsystemtype),
									commandParamInput,getExternalSystem(externalsystemtype));
							}
						} else if (chargingstepid!=null && Integer.toString(ExternalSystemType.MOBILEMONEY_TYPE.getKey()).equals(chargingstepid)) {
							externalsystemtype = ExternalSystemType.MOBILEMONEY_TYPE;
							amountCharged = ((AIRChargingDTO) chargingstepdto.getChargingSystem()).getAmountCharged();
							fixedCharging = new FixedCharging(new AIRCharging(amountCharged),getChargingCommand(externalsystemtype),
									commandParamInput,getExternalSystem(externalsystemtype));
					
						} else if (chargingstepid!=null && Integer.toString(ExternalSystemType.EVDS_TYPE.getKey()).equals(chargingstepid)) {
							externalsystemtype = ExternalSystemType.EVDS_TYPE;
							amountCharged = ((AIRChargingDTO) chargingstepdto.getChargingSystem()).getAmountCharged();
							fixedCharging = new FixedCharging(new AIRCharging(amountCharged),getChargingCommand(externalsystemtype),
									commandParamInput,getExternalSystem(externalsystemtype));
					
						}

					fixedcharginglist.add(fixedCharging);
					}
					chargingstepdto.getDefaultCharging();
					
					//creating the charging Step 
					chargingConditionStep.setDefaultCharging(getVariableDefaultCharging(chargingstepdto.getDefaultCharging()));
					chargingConditionStep.getProductChargings().addAll(fixedcharginglist);
					chargingConditionSteps.add(chargingConditionStep);
					fixedcharginglist=new ArrayList<ProductCharging>();
				}
			}
			
			
			chargingList.add(new VariableCharging(chargingConditionSteps,
					null));
			//create new condition steps for each channel type
			chargingConditionSteps= new ArrayList<ChargingConditionStep>();
		}
		}
	}
		private boolean getPartialCharging(Boolean isPartial) {
		if(isPartial==null)
			return false;
		else
			return isPartial.booleanValue();
	}

		/**
		 * Method will return the Default charging based on charging constraints
		 * @param defaultCharging
		 * @return ExternalSystem
		 */
	private ExternalSystem getVariableDefaultCharging(String defaultCharging) {
		ExternalSystem externalsystem = null;
		if(null != defaultCharging){              
            if(defaultCharging.equals((ExternalSystemType.AIR_TYPE.getValue()).toString())){
            	externalsystem=ExternalSystem.AIR;
            }else if(defaultCharging.equals((ExternalSystemType.LOYALTY_TYPE.getValue()).toString())){
            	externalsystem=ExternalSystem.Loyalty;
            }else if(defaultCharging.equals((ExternalSystemType.MOBILEMONEY_TYPE.getValue()).toString())){
            	externalsystem=ExternalSystem.MM;
            }else if(defaultCharging.equals((ExternalSystemType.EVDS_TYPE.getValue()).toString())){
            	externalsystem=ExternalSystem.EVDS;
            }
       };
       return externalsystem;
	}

	private ProductChargingType getProductChargingType(ProductDTO productDTO) {
		ChargingDTO chargingdto = productDTO.getChannelChargingList(
				ChannelType.SMS).get(0);
		if (chargingdto instanceof FixedChargingDTO) {
			return ProductChargingType.FIXED_AIR;
		} else {
			return ProductChargingType.VARIABLE_AIR;
		}

	}

	/**
	 * This method is used for updating the IVR code cache.
	 *
	 * @param fdpCircle
	 *            the fdp circle
	 * @param ivrName
	 *            the ivr name
	 * @param productId
	 *            the product id
	 */
	private void initializeUpdateProductAliasCache(final FDPCircle fdpCircle,
			final String name, final Long productId,
			final ExternalSystem externalSystem) {
		if (name != null) {
			final FDPMetaBag metaBag = new FDPMetaBag(fdpCircle,
					ModuleType.PRODUCT_ALIAS, externalSystem.name());

			if (fdpCache.getValue(metaBag) == null) {
				final ProductAliasCode productAliasCode = new ProductAliasCode();
				productAliasCode.setProductIdForExternalSystemAlias(name,
						productId.toString());
				fdpCache.putValue(metaBag, productAliasCode);
			} else {
				final FDPCacheable fdpCacheAble = fdpCache.getValue(metaBag);
				if (fdpCacheAble instanceof ProductAliasCode) {
					final ProductAliasCode productAliasCode = (ProductAliasCode) fdpCacheAble;
					productAliasCode.setProductIdForExternalSystemAlias(name,
							productId.toString());
					fdpCache.putValue(metaBag, productAliasCode);
				}
			}
		}
	}

	/**
	 * Removes the ivr code cache.
	 *
	 * @param fdpCircle
	 *            the fdp circle
	 * @param name
	 *            the name
	 * @param externalSystem
	 *            the external system
	 */
	private void removeFromIvrCodeCache(final FDPCircle fdpCircle,
			final String name, final ExternalSystem externalSystem) {
		final FDPMetaBag metaBag = new FDPMetaBag(fdpCircle,
				ModuleType.PRODUCT_ALIAS, externalSystem.name());
		final FDPCacheable fdpCacheable = fdpCache.getValue(metaBag);
		if (fdpCacheable instanceof ProductAliasCode) {
			final ProductAliasCode productAliasCode = (ProductAliasCode) fdpCacheable;
			productAliasCode.removeProductIdForExternalSystemAlias(name);
			fdpCache.putValue(metaBag, productAliasCode);
		}
	}

	private void variableChargingForProduct(
			final ChargingDTO chargingDTO,
			final Map<ChannelType, Map<ChargingType, List<ProductCharging>>> productChargingTypeChargingListMap,
			final ChannelType channelType,
			final List<ChargingStepDTO> chargingStepList)
			throws ExpressionFailedException {
		final VariableChargingDTO variableChargingDTO = (VariableChargingDTO) chargingDTO;
		try {
			List<ProductCharging> chargingList = this
					.getChannelChargingTypeList(channelType,
							productChargingTypeChargingListMap,
							ChargingType.NORMAL);
			ProductCharging productChargingVariable = isStepAlreadyCreated(chargingList);
			if (null != productChargingVariable) {
				final VariableCharging variableCharging = (VariableCharging) productChargingVariable;
				final ChargingConditionStep chargingConditionStep = getChargingConditionStep(
						variableCharging.getConditionStep(), chargingStepList);
				final List<ProductCharging> productChargings = chargingConditionStep
						.getProductChargings();
				final CommandParamInput commandParamInput = this
						.getCommandParamInputObject(ChargingActor.REQUEST_SUBSCRIBERNUMBER
								.getValue());
				final ChargingStepDTO chargingStepDTO = getChargingStepDTO(chargingStepList);
				List<DAChargingDTO> chargingDTOs = null;
				boolean isPartialAllowed = false;
				if (null != chargingStepDTO
						&& null != chargingStepDTO.getDaChargingDTOs()
						&& null != chargingStepDTO.getIsPartial()) {
					chargingDTOs = chargingStepDTO.getDaChargingDTOs();
					isPartialAllowed = chargingStepDTO.getIsPartial();
				}

				if (variableChargingDTO.getExternalSystemType().equals(
						ExternalSystemType.LOYALTY_TYPE)) {
					productChargings
							.add(prepareChargingConditionStepProductCharging(
									/*chargingStepDTO.getLoyalitypoint()*/ chargingStepDTO.getLoyalityItemCode(),
									variableChargingDTO.getExternalSystemType(),
									commandParamInput, chargingDTOs,
									isPartialAllowed));
				} else {
					productChargings
							.add(prepareChargingConditionStepProductCharging(
									Long.valueOf(chargingConditionStep
											.getChargingType()
											.getChargingValue().toString()),
									variableChargingDTO.getExternalSystemType(),
									commandParamInput, chargingDTOs,
									isPartialAllowed));
				}
			} else {
				final List<ChargingConditionStep> chargingConditionSteps = new ArrayList<ChargingConditionStep>();
				for (final ChargingStepDTO chargingStepDTO : chargingStepList) {
					if (chargingStepDTO.getChargingSystem() instanceof AIRChargingDTO) {
						final Expression expressionForStepsProductCharging = ExpressionUtil
								.createExpressionForSubStep(chargingStepDTO
										.getSubStepsList());
						final Long amountCharged = ((AIRChargingDTO) chargingStepDTO
								.getChargingSystem()).getAmountCharged();
						final CommandParamInput commandParamInput = this
								.getCommandParamInputObject(ChargingActor.REQUEST_SUBSCRIBERNUMBER
										.getValue());
						final List<DAChargingDTO> chargingDTOs = chargingStepDTO
								.getDaChargingDTOs();
						boolean isPartialAllowed = false;
						if (null != chargingStepDTO.getIsPartial()
								&& chargingStepDTO.getIsPartial())
							isPartialAllowed = true;
						// final boolean isPartialAllowed =
						// chargingStepDTO.getIsPartial();
						final ChargingConditionStep chargingConditionStep = new ChargingConditionStep(
								expressionForStepsProductCharging,
								new AIRCharging(amountCharged), 1L,
								FDPConstant.AIR_CHARCHING_USSD);
						if (variableChargingDTO.getExternalSystemType().equals(
								ExternalSystemType.LOYALTY_TYPE)) {
							chargingConditionStep
									.getProductChargings()
									.add(prepareChargingConditionStepProductCharging(
											/*chargingStepDTO.getLoyalitypoint()*/ chargingStepDTO.getLoyalityItemCode(),
											variableChargingDTO
													.getExternalSystemType(),
											commandParamInput, chargingDTOs,
											isPartialAllowed));
						} else {
							chargingConditionStep
									.getProductChargings()
									.add(prepareChargingConditionStepProductCharging(
											amountCharged, variableChargingDTO
													.getExternalSystemType(),
											commandParamInput, chargingDTOs,
											isPartialAllowed));
						}
						if (null != chargingStepDTO.getDefaultCharging()) {
							if (chargingStepDTO.getDefaultCharging().equals(
									(ExternalSystemType.AIR_TYPE.getValue())
											.toString())) {
								chargingConditionStep
										.setDefaultCharging(ExternalSystem.AIR);
							} else if (chargingStepDTO.getDefaultCharging()
									.equals((ExternalSystemType.LOYALTY_TYPE
											.getValue()).toString())) {
								chargingConditionStep
										.setDefaultCharging(ExternalSystem.Loyalty);
							} else if (chargingStepDTO
									.getDefaultCharging()
									.equals((ExternalSystemType.MOBILEMONEY_TYPE
											.getValue()).toString())) {
								chargingConditionStep
										.setDefaultCharging(ExternalSystem.MM);
							}
						}
						chargingConditionSteps.add(chargingConditionStep);
					}
				}
				chargingList.add(new VariableCharging(chargingConditionSteps,
						getChargingCommand(variableChargingDTO
								.getExternalSystemType())));
			}
		} catch (final Exception e) {
			e.printStackTrace();
			throw new ExpressionFailedException(
					"Got exception in variableChargingForProduct, Actual Error:",
					e);
		}
	}

	/**
	 * Prepare the charging object.
	 * 
	 * @param amount
	 * @param externalSystemType
	 * @param commandParamInput
	 * @param chargingDTOs
	 * @param isPartialAllowed
	 * @return
	 */
	private ProductCharging prepareChargingConditionStepProductCharging(
			final Long amount, final ExternalSystemType externalSystemType,
			final CommandParamInput commandParamInput,
			final List<DAChargingDTO> chargingDTOs,
			final boolean isPartialAllowed) {
		ProductCharging productCharging = null;
		if (null != chargingDTOs) {
			productCharging = new DABasedAirCharging(new AIRCharging(amount),
					getChargingCommand(externalSystemType), commandParamInput,
					convertToAccountList(chargingDTOs), isPartialAllowed,
					getExternalSystem(externalSystemType));
		} else {
			productCharging = new FixedCharging(new AIRCharging(amount),
					getChargingCommand(externalSystemType), commandParamInput,
					getExternalSystem(externalSystemType));
		}
		return productCharging;
	}

	/**
	 * Get particular variable condition step to add further fixed/DA based
	 * charging in the list.
	 * 
	 * <USSD,Variable(List of ChargingConditionStep[Expression and List of
	 * ProductCharging])>
	 * 
	 * @param chargingConditionSteps
	 * @param chargingStepList
	 * @return
	 */
	private ChargingConditionStep getChargingConditionStep(
			final List<ChargingConditionStep> chargingConditionSteps,
			final List<ChargingStepDTO> chargingStepList) {
		ChargingConditionStep chargingConditionStep = null;
		final String key = getChargingConditionStepKey(chargingStepList);
		for (final ChargingConditionStep conditionStep : chargingConditionSteps) {
			// if(null != conditionStep.getKey() && null != key /**&&
			// conditionStep.getKey().equals(key)**/) {
			// if(null != conditionStep.getKey() && null != key &&
			// conditionStep.getKey().equals(key)) {
			chargingConditionStep = conditionStep;
			break;
			// }
		}
		return chargingConditionStep;
	}

	/**
	 * Prepare dummy key to identify particular ChargingConditionStep in list
	 * variable object list.
	 * 
	 * @param chargingStepList
	 * @return
	 */
	private String getChargingConditionStepKey(
			final List<ChargingStepDTO> chargingStepList) {
		StringBuffer key = new StringBuffer();
		for (final ChargingStepDTO chargingStepDTO : chargingStepList) {
			key.append(chargingStepDTO.getStepId());
			key.append(FDPConstant.UNDERSCORE);
		}
		return key.toString();
	}

	/**
	 * Check if VariableCharging step exist of not.
	 * 
	 * @param productChargings
	 * @return
	 */
	private ProductCharging isStepAlreadyCreated(
			List<ProductCharging> productChargings) {
		ProductCharging productChargingVariable = null;
		for (final ProductCharging productCharging : productChargings) {
			if (productCharging instanceof VariableCharging) {
				productChargingVariable = productCharging;
			}
		}
		return productChargingVariable;
	}

	private ChargingStepDTO getChargingStepDTO(
			List<ChargingStepDTO> productChargings) {
		ChargingStepDTO productChargingDTO = null;
		for (final ChargingStepDTO chargingStepDTO : productChargings) {
			if (chargingStepDTO.getChargingSystem() instanceof AIRChargingDTO) {
				productChargingDTO = chargingStepDTO;
				break;
			}
		}
		return productChargingDTO;
	}

	private void recurringChargingForProduct(
			final ChargingDTO chargingDTO,
			final Map<ChannelType, Map<ChargingType, List<ProductCharging>>> productChargingTypeChargingListMap,
			final ChannelType channelType) {
		final RecurringChargingDTO recurringChargingDTO = (RecurringChargingDTO) chargingDTO;
		final RecurringCharging recurringCharging = new RecurringCharging(
				recurringChargingDTO.getParam(),
				FDPConstant.RS_CHARGING_COMMAND,
				recurringChargingDTO.getRsCharginAmt());
		final ChargingType chargingType = ChargingType.NORMAL;
		List<ProductCharging> chargingList = this.getChannelChargingTypeList(
				channelType, productChargingTypeChargingListMap, chargingType);
		chargingList.add(recurringCharging);
		productChargingTypeChargingListMap.get(channelType).put(chargingType,
				chargingList);
	}

	private List<ProductCharging> getChannelChargingTypeList(
			final ChannelType channelType,
			final Map<ChannelType, Map<ChargingType, List<ProductCharging>>> productChargingTypeChargingListMap,
			final ChargingType chargingType) {
		Map<ChargingType, List<ProductCharging>> map = productChargingTypeChargingListMap
				.get(channelType);
		boolean isEmpty = false;
		if (null == map) {
			map = new HashMap<ChargingType, List<ProductCharging>>();
			isEmpty = true;
		}
		List<ProductCharging> chargingList = map.get(chargingType);
		if (null == chargingList) {
			chargingList = new ArrayList<ProductCharging>();
			map.put(chargingType, chargingList);
		}
		if (isEmpty) {
			productChargingTypeChargingListMap.put(channelType, map);
		}
		return productChargingTypeChargingListMap.get(channelType).get(
				chargingType);
	}

	private void fixedChargingForProduct(
			final ChargingDTO chargingDTO,
			final Map<ChannelType, Map<ChargingType, List<ProductCharging>>> productChargingTypeChargingListMap,
			final ChannelType channelType, final Long loyaltyPoints, Map<Integer, String> productAdditionalInfoMap)
			throws ExpressionFailedException {
		final FixedChargingDTO fixedChargingDTO = (FixedChargingDTO) chargingDTO;
		try {
			if (fixedChargingDTO.getChargingSystem() instanceof AIRChargingDTO) {
				final Long amount = ((AIRChargingDTO) fixedChargingDTO
						.getChargingSystem()).getAmountCharged();
				final ChargingType chargingType = fixedChargingDTO
						.getChargingType();
				List<ProductCharging> chargingList = this
						.getChannelChargingTypeList(channelType,
								productChargingTypeChargingListMap,
								chargingType);
				if (!(ChargingActor.STEP_OUTPUT_VALIDATON_STEP_CONSUMER_MSISIDN
						.equals(fixedChargingDTO.getChargingActor()) && (ChargingType.DELETE_ACCOUNT
						.equals(fixedChargingDTO.getChargingType())
						|| ChargingType.DETACH_PROVIDER.equals(fixedChargingDTO
								.getChargingType())
						|| ChargingType.TOP_N_USAGE.equals(fixedChargingDTO
								.getChargingType())
						|| ChargingType.VIEW_TOTAL_USAGE_UC
								.equals(fixedChargingDTO.getChargingType()) || ChargingType.VIEW_TOTAL_USAGE_UT
							.equals(fixedChargingDTO.getChargingType())))) {
					final CommandParamInput commandParamInput = this
							.getCommandParamInputObject(fixedChargingDTO
									.getChargingActor().getValue());
					if (ExternalSystemType.AIR_TYPE.equals(fixedChargingDTO
							.getExternalSystemType())
							&& null != fixedChargingDTO.getDaChargingDTOs()
							&& fixedChargingDTO.getDaChargingDTOs().size() > 0 && Boolean.valueOf(productAdditionalInfoMap.get(ProductAdditionalInfoEnum.IS_CHARGING_FROM_DA.getKey()))) {
						chargingList.add(new DABasedAirCharging(
								new AIRCharging(amount),
								getChargingCommand(fixedChargingDTO
										.getExternalSystemType()),
								commandParamInput,
								convertToAccountList(fixedChargingDTO
										.getDaChargingDTOs()), fixedChargingDTO
										.isPartialChargingAllowed(),
								getExternalSystem(fixedChargingDTO
										.getExternalSystemType())));
					} else if (ExternalSystemType.LOYALTY_TYPE
							.equals(fixedChargingDTO.getExternalSystemType())) {
			chargingList.add(new FixedCharging(new AIRCharging(loyaltyPoints),getChargingCommand(fixedChargingDTO.getExternalSystemType()),
								commandParamInput,getExternalSystem(fixedChargingDTO.getExternalSystemType())));
					} else if (ExternalSystemType.MOBILEMONEY_TYPE
							.equals(fixedChargingDTO.getExternalSystemType())) {
						chargingList.add(new FixedCharging(new AIRCharging(
								amount), getChargingCommand(fixedChargingDTO
								.getExternalSystemType()), commandParamInput,
								getExternalSystem(fixedChargingDTO
										.getExternalSystemType())));
					} else if (ExternalSystemType.AIR_TYPE
							.equals(fixedChargingDTO.getExternalSystemType())
							&& (null == fixedChargingDTO.getDaChargingDTOs() || (fixedChargingDTO.getDaChargingDTOs().size() > 0 && !Boolean.valueOf(productAdditionalInfoMap.get(ProductAdditionalInfoEnum.IS_CHARGING_FROM_DA.getKey()))))) {
						chargingList.add(new FixedCharging(new AIRCharging(
								amount), getChargingCommand(fixedChargingDTO
								.getExternalSystemType()), commandParamInput,
								getExternalSystem(fixedChargingDTO
										.getExternalSystemType())));
					} else if (ExternalSystemType.EVDS_TYPE
							.equals(fixedChargingDTO.getExternalSystemType())) {
						chargingList.add(new FixedCharging(new AIRCharging(
								amount), getChargingCommand(fixedChargingDTO
								.getExternalSystemType()), commandParamInput,
								getExternalSystem(fixedChargingDTO
										.getExternalSystemType())));
					} 

					productChargingTypeChargingListMap.get(channelType).put(
							chargingType, chargingList);
				}
			}
		} catch (final Exception e) {
			e.printStackTrace();
			throw new ExpressionFailedException(
					"Got exception in fixedChargingForProduct, Actual Error:",
					e);
		}
	}

	/**
	 * This method create a command param input object based on chargingActor
	 * e.g. REQUEST_SUBSCRIBERNUMBER etc.
	 *
	 * @param chargingActorValue
	 *            chargingActor value e.g. REQUEST_SUBSCRIBERNUMBER etc.
	 * @return commandParamInput
	 */
	private CommandParamInput getCommandParamInputObject(
			final String chargingActorValue) {
		final ParameterFeedType feedType = ParameterFeedType
				.valueOf(chargingActorValue.substring(FDPConstant.ZERO,
						chargingActorValue.indexOf(FDPConstant.DOT)));
		final String definedValueToSet = chargingActorValue
				.substring(chargingActorValue.indexOf(FDPConstant.DOT) + 1);
		final CommandParamInput commandParamInput = new CommandParamInput(
				feedType, definedValueToSet);
		commandParamInput.setType(CommandParameterType.PRIMITIVE);
		commandParamInput.setPrimitiveValue(Primitives.LONG);
		return commandParamInput;
	}

	/**
	 * Populate add info map.
	 *
	 * @param product
	 *            the product
	 * @param additionalInfoMap
	 *            the additional info map
	 */
	private void populateAddInfoMap(final Product product,
			final Map<Integer, String> additionalInfoMap) {
		for (final Integer key : additionalInfoMap.keySet()) {
			product.setAdditionalInfo(
					ProductAdditionalInfoEnum.getEnumForKey(key),
					additionalInfoMap.get(key));
		}
	}

	private void deProvisioningChargingForProduct(
			final ProductDTO productDTO,
			final Map<ChannelType, Map<ChargingType, List<ProductCharging>>> productChargingTypeChargingListMap) {
		if (productDTO.getDeProvisioningList() != null
				&& productDTO.getDeProvisioningList().size() > 0) {
			for (final DeProvisionChargingDTO deProvisionChargingDTO : productDTO
					.getDeProvisioningList()) {
				for (final Map.Entry<ChannelType, Map<ChargingType, List<ProductCharging>>> entry : productChargingTypeChargingListMap
						.entrySet()) {
					final ChannelType channelType = entry.getKey();
					final List<ProductCharging> chargingList = this
							.getChannelChargingTypeList(channelType,
									productChargingTypeChargingListMap,
									deProvisionChargingDTO.getChargingType());
					if (FDPConstant.CHARGING_SYSTEM_AIR
							.equals(deProvisionChargingDTO.getChannelType())) {
						Long amount = 0L;
						switch (channelType) {
						case USSD:
							amount = deProvisionChargingDTO
									.getUssdDeProvisioning();
							break;
						case ABILITY:
							amount = deProvisionChargingDTO
									.getSmsDeProvisioning();
							break;
						case EVD:
							amount = deProvisionChargingDTO
									.getIvrDeProvisioning();
							break;
							
						case WEB:
							amount = deProvisionChargingDTO
									.getWebDeProvisioning();
							break;
						/*case LCMS:
							amount = deProvisionChargingDTO
									.getLoyalityDeProvisioning();*/
							//break;
						case CRM:
							amount = deProvisionChargingDTO
									.getCrmDeProvisioning();
							break;
						case SMARTAPP:
							amount = deProvisionChargingDTO
									.getSmartAppDeProvisioning();
							break;
						case TOOLBAR:
							amount = deProvisionChargingDTO
									.getToolBarDeProvisioning();
							break;
						case FLYTXT:
							amount = deProvisionChargingDTO
									.getFlyTxtDeProvisioning();
							break;
						case SELFCARE:
							amount = deProvisionChargingDTO
									.getSelfCareDeProvisioning();
							break;
						case SMS:
							amount = deProvisionChargingDTO
									.getSmsDeProvisioning();
							break;
						default:
							break;
						}
						if (!(ChargingActor.STEP_OUTPUT_VALIDATON_STEP_CONSUMER_MSISIDN
								.equals(deProvisionChargingDTO
										.getChargingActor()) && (ChargingType.DELETE_ACCOUNT
								.equals(deProvisionChargingDTO
										.getChargingType())
								|| ChargingType.DETACH_PROVIDER
										.equals(deProvisionChargingDTO
												.getChargingType())
								|| ChargingType.TOP_N_USAGE
										.equals(deProvisionChargingDTO
												.getChargingType())
								|| ChargingType.VIEW_TOTAL_USAGE_UC
										.equals(deProvisionChargingDTO
												.getChargingType()) || ChargingType.VIEW_TOTAL_USAGE_UT
									.equals(deProvisionChargingDTO
											.getChargingType())))) {
							final CommandParamInput commandParamInput = this
									.getCommandParamInputObject(deProvisionChargingDTO
											.getChargingActor().getValue());
							chargingList.add(new FixedCharging(new AIRCharging(
									amount), FDPConstant.AIR_CHARGING_COMMAND,
									commandParamInput));
						}
					}
					if ((null != deProvisionChargingDTO.getSendToRS() && deProvisionChargingDTO
							.getSendToRS())
							|| null != deProvisionChargingDTO
									.getDeProvisioningServiceID()) {
						String rsCharginAmt = null;
						switch (channelType) {
						case USSD:
							rsCharginAmt = deProvisionChargingDTO
									.getUssdDeProvisioning().toString();
							break;
						case ABILITY:
							rsCharginAmt = deProvisionChargingDTO
									.getSmsDeProvisioning().toString();
							break;
						case EVD:
							rsCharginAmt = deProvisionChargingDTO
									.getIvrDeProvisioning().toString();
							break;
						case WEB:
							rsCharginAmt = deProvisionChargingDTO
									.getWebDeProvisioning().toString();
							break;
					/*	case LCMS:
							rsCharginAmt = deProvisionChargingDTO
									.getLoyalityDeProvisioning().toString();
							break;*/
						case CRM:
							rsCharginAmt = deProvisionChargingDTO
									.getCrmDeProvisioning().toString();
							break;
						case SMARTAPP:
							rsCharginAmt = deProvisionChargingDTO
									.getSmartAppDeProvisioning().toString();
							break;
						case FLYTXT:
							rsCharginAmt = deProvisionChargingDTO
									.getFlyTxtDeProvisioning().toString();
							break;
						case TOOLBAR:
							rsCharginAmt = deProvisionChargingDTO
									.getToolBarDeProvisioning().toString();
							break;
						case SELFCARE:
							rsCharginAmt = deProvisionChargingDTO
									.getSelfCareDeProvisioning().toString();
							break;
						case SMS:
							rsCharginAmt = deProvisionChargingDTO
									.getSmsDeProvisioning().toString();
							break;
						default:
							break;
						}
						final RecurringCharging recurringCharging = new RecurringCharging(
								deProvisionChargingDTO
										.getDeProvisioningServiceID(),
								FDPConstant.RS_DEPROVISIONING_COMMAND,
								rsCharginAmt);
						chargingList.add(recurringCharging);
					}
				}
			}
		}
	}

	/**
	 * Removes the previous values from cache.
	 *
	 * @param prevProductValues
	 *            the prev product values
	 */
	private void removePreviousValuesFromCache(final FDPCircle fdpCircle,
			final ProductChangeHistoryDTO prevProductValues) {
		if (prevProductValues != null) {

			if (prevProductValues.getPrevIVRCode() != null) {
				removeFromIvrCodeCache(fdpCircle,
						prevProductValues.getPrevIVRCode(),
						ExternalSystem.IVR_TYPE);
			}

			if (prevProductValues.getPrevPAMId() != null) {
				removeFromIvrCodeCache(fdpCircle, FDPConstant.PAM_ID_INCACHE
						+ prevProductValues.getPrevPAMId(), ExternalSystem.AIR);
			}

			if (prevProductValues.getPrevAliasCode() != null) {
				removeFromIvrCodeCache(fdpCircle,
						prevProductValues.getPrevAliasCode(),
						ExternalSystem.AIR);
			}
		}
	}

	@Override
	public boolean updateMetaCache(final UpdateCacheDTO updateCacheDTO)
			throws FDPServiceException {
		try {
			Long productId = null;
			FDPCircle fdpCircle = null;
			// Checking updateCacheDTO is not null
			ProductChangeHistoryDTO prevProductValues = null;
			if (null != updateCacheDTO) {
				prevProductValues = getPreviousProductValues(updateCacheDTO,
						prevProductValues);
				productId = updateCacheDTO.getId();
				fdpCircle = updateCacheDTO.getCircle();
				removePrevValueFromCache(updateCacheDTO, productId, fdpCircle,
						prevProductValues);
			}
			updateCache(productId, fdpCircle);
		} catch (final ExpressionFailedException efe) {
			LOGGER.error(efe.getMessage());
			throw new FDPServiceException(efe);
		}
		return true;
	}

	private ProductChangeHistoryDTO getPreviousProductValues(
			final UpdateCacheDTO updateCacheDTO,
			ProductChangeHistoryDTO prevProductValues) {
		if (updateCacheDTO.getUiObjectDTO() instanceof ProductDTO) {
			ProductDTO prevProductData = (ProductDTO) updateCacheDTO
					.getUiObjectDTO();
			prevProductValues = prevProductData.getProductInfoDTO()
					.getPreviousValues();
		}
		return prevProductValues;
	}

	private void removePrevValueFromCache(final UpdateCacheDTO updateCacheDTO,
			final Long productId, final FDPCircle fdpCircle,
			final ProductChangeHistoryDTO prevProductValues) {
		if (ActionTypeEnum.DELETE.equals(updateCacheDTO.getAction())) {
			fdpCache.removeKey(new FDPMetaBag(fdpCircle, ModuleType.PRODUCT,
					productId));
			this.removePreviousValuesFromCache(fdpCircle, prevProductValues);
		} else if (ActionTypeEnum.ADD_UPDATE.equals(updateCacheDTO.getAction())) {
			this.removePreviousValuesFromCache(fdpCircle, prevProductValues);
		}
	}

	private void updateCache(final Long productId, final FDPCircle fdpCircle)
			throws FDPServiceException, ExpressionFailedException {
		final List<ProductDTO> productDTOs;
		if (productId == null) {
			productDTOs = entityService.getLaunchedProducts(fdpCircle);
		} else {
			productDTOs = entityService.getLaunchedProducts(productId);
		}
		
		for (final ProductDTO productDTO : productDTOs) {
			try{
				this.initializeUpdateProduct(productDTO, fdpCircle);
				LOGGER.debug("Cache updated for Product : {}", productDTO.getProductId());
			}
			catch(Exception ex){
				LOGGER.debug("Cache not updated for Product : {}", productDTO.getProductId());
			}
			//this.initializeUpdateProduct(productDTO, fdpCircle);
		}
	}

	@Override
	public boolean initializeMetaCache(final FDPCircle fdpCircle)
			throws FDPServiceException {
		if (fdpCircle == null) {
			throw new FDPServiceException("Circle is mandatory");
		} else {
			try {
				this.updateCache(null, fdpCircle);
			} catch (ExpressionFailedException e) {
				LOGGER.error(e.getMessage());
				throw new FDPServiceException(e);
			}
		}
		return true;
	}

	@Override
	public ModuleType getModuleType() {
		return ModuleType.PRODUCT;
	}

	/**
	 * Method to External System Type with External Type.
	 * 
	 * @param externalSystemType
	 * @return
	 */
	private ExternalSystem getExternalSystem(
			final ExternalSystemType externalSystemType) {
		ExternalSystem externalSystem = null;
		switch (externalSystemType) {
		case AIR_TYPE:
			externalSystem = ExternalSystem.AIR;
			break;
		case LOYALTY_TYPE:
			externalSystem = ExternalSystem.Loyalty;
			break;
		case RS_TYPE:
			externalSystem = ExternalSystem.RS;
			break;
		case MOBILEMONEY_TYPE:
			externalSystem = ExternalSystem.MM;
			break;
		case EVDS_TYPE:
			externalSystem = ExternalSystem.EVDS;
			break;
		default:
			break;
		}
		return externalSystem;
	}

	/**
	 * This method converts account list.
	 * 
	 * @param chargingDTOs
	 * @return
	 */
	private List<Account> convertToAccountList(
			final List<DAChargingDTO> chargingDTOs) {
		final List<Account> accounts = new ArrayList<Account>();
		for (final DAChargingDTO chargingDTO : chargingDTOs) {
			Account account = null;
			if ("MAIN".equalsIgnoreCase(chargingDTO.getCsAttributeType())) {
				account = new MainAccount(0L, chargingDTO.getPriority()
						.intValue());
			} else if ("DA".equalsIgnoreCase(chargingDTO.getCsAttributeType())) {
				account = new DedicatedAccount(chargingDTO.getCsAttributeId()
						.toString(), 0L, chargingDTO.getPriority().intValue());
			}
			accounts.add(account);
		}
		return accounts;
	}

	private String getChargingCommand(
			final ExternalSystemType externalSystemType) {
		String command = null;
		switch (externalSystemType) {
		case AIR_TYPE:
			command = FDPConstant.AIR_CHARGING_COMMAND;
			break;
		case LOYALTY_TYPE:
			command = FDPConstant.LOYALTY_CHARGING_COMMAND;
			break;
		case MOBILEMONEY_TYPE:
			command = FDPConstant.MM_CHARGING_COMMAND;
			break;
		case EVDS_TYPE :
			if(evdsType.contains((String) FDPConstant.EVDS_TYPE_HTTP)||
					evdsType.contains((String) FDPConstant.EVDS_HTTP_TYPE)){
				command = FDPConstant.EVDS_CHARGING_COMMAND_HTTP;
			}else{
			command = FDPConstant.EVDS_CHARGING_COMMAND;
			}
			break;
		default:
			break;
		}
		return command;
	}
}
