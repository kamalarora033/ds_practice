package com.ericsson.fdp.business.cache.datageneration.service.impl;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.Resource;
import javax.ejb.Stateless;
import javax.inject.Inject;

import com.ericsson.fdp.FDPCacheable;
import com.ericsson.fdp.business.cache.datageneration.service.TariffService;
import com.ericsson.fdp.business.convertor.Convertor;
import com.ericsson.fdp.business.convertor.conversion.ConversionRules;
import com.ericsson.fdp.business.convertor.conversion.impl.ConversionRulesImpl;
import com.ericsson.fdp.business.convertor.impl.ConverterImpl;
import com.ericsson.fdp.business.enums.ConversionOption;
import com.ericsson.fdp.business.tariffenquiry.command.impl.TariffEnquiryDisplayFormatImpl;
import com.ericsson.fdp.business.util.TariffEnquiryDisplayFormatUtil;
import com.ericsson.fdp.common.enums.ModuleType;
import com.ericsson.fdp.common.exception.FDPServiceException;
import com.ericsson.fdp.common.vo.FDPCircle;
import com.ericsson.fdp.core.dsm.framework.service.FDPCache;
import com.ericsson.fdp.core.entity.service.EntityService;
import com.ericsson.fdp.core.serviceprovisioning.bean.FDPMetaBag;
import com.ericsson.fdp.dao.dto.UpdateCacheDTO;
import com.ericsson.fdp.dao.dto.tariffenquiry.FDPDisplayFormatDTO;
import com.ericsson.fdp.dao.tariffenquiry.impl.FDPTariffEnquiryUnitDisplayImpl;

/**
 * The Class TariffUnitCommandCacheImpl.
 */
@Stateless(mappedName = "TariffEnquiryDisplayFormatCacheImpl")
public class TariffEnquiryDisplayFormatCacheImpl implements TariffService {

	/** The fdp cache. */
	@Resource(lookup = "java:app/fdpBusiness-1.0/MetaDataCache")
	private FDPCache<FDPMetaBag, FDPCacheable> fdpCache;

	/** The fdp tariff command dao. */
	@Inject
	private FDPTariffEnquiryUnitDisplayImpl fdpTariffEnquiryUnitDisplayDAO;

	/** The entity service. */
	@Resource(lookup = "java:app/fdpCoreServices-1.0/EntityServiceImpl")
	private EntityService entityService;

	@Override
	public boolean updateMetaCache(final UpdateCacheDTO updateCacheDTO) throws FDPServiceException {
		if (updateCacheDTO == null || updateCacheDTO.getCircle() == null) {
			return false;
		} else {
			this.initializeTariffEnquiryDisplayformatCache(updateCacheDTO.getCircle().getCircleId());
		}
		return true;
	}

	@Override
	public boolean initializeMetaCache(final FDPCircle fdpCircle) throws FDPServiceException {
		if (fdpCircle == null) {
			throw new FDPServiceException("Circle cannot be null");
		} else {
			this.initializeTariffEnquiryDisplayformatCache(fdpCircle.getCircleId());
		}
		return true;
	}

	/**
	 * Initialize tariff enquiry displayformat cache.
	 *
	 * @param circleId
	 *            the circle id
	 */
	private void initializeTariffEnquiryDisplayformatCache(final Long circleId) {
		Map<String, Map<String, Map<ConversionOption, Convertor>>> cacheMap = new HashMap<String, Map<String, Map<ConversionOption, Convertor>>>();
		List<FDPDisplayFormatDTO> displayformats = fdpTariffEnquiryUnitDisplayDAO
				.getCircleTariffDisplayFormats(circleId);

		// First Creating Temporary Map for Cache
		for (final FDPDisplayFormatDTO displayformat : displayformats) {
			ConversionOption conversionOptionType = ConversionOption.getConversionOptionValue(displayformat
					.getConditionType());
			if (null != displayformat && conversionOptionType != null) {
				TariffEnquiryDisplayFormatUtil.createUpdateCacheMap(displayformat, conversionOptionType, cacheMap);
			}
		}

		// COMMENT THIS METHOD WHEN NOT IN NEED.
		// printTempCacheMap(cacheMap);

		// Iterating Temporary Map and putting into Cache.
		for (final FDPDisplayFormatDTO displayformat : displayformats) {
			String key = displayformat.getFdpUnitType().getUnitValue().toString();
			FDPCircle fdpCircle = prepareFDPCircle(displayformat);
			Map<String, Map<ConversionOption, Convertor>> perCircleMap = cacheMap.get(fdpCircle.getCircleId()
					.toString());
			if (null != displayformat) {
				Map<ConversionOption, Convertor> convertorMap = perCircleMap.get(key);
				TariffEnquiryDisplayFormatImpl tariffEnquiryDisplayFormatImpl = new TariffEnquiryDisplayFormatImpl(
						convertorMap);
				insertToCache(key, fdpCircle, tariffEnquiryDisplayFormatImpl);
			}
		}
	}

	/**
	 * This method prepares the FDPCircle object.
	 *
	 * @param fdpDisplayFormat
	 * @return
	 */
	private FDPCircle prepareFDPCircle(final FDPDisplayFormatDTO fdpDisplayFormat) {
		return new FDPCircle(fdpDisplayFormat.getFdpCircle().getCircleId(), fdpDisplayFormat.getFdpCircle()
				.getCircleCode(), fdpDisplayFormat.getFdpCircle().getCircleName());
	}

	/**
	 * Insert into MetaCache
	 *
	 * @param unitValue
	 * @param fdpCircle
	 * @param displayFormat
	 */
	private void insertToCache(final String unitValue, final FDPCircle fdpCircle,
			final TariffEnquiryDisplayFormatImpl displayFormat) {
		FDPMetaBag metaBag = new FDPMetaBag(fdpCircle, ModuleType.UNIT_DISPLAY_FORMAT, unitValue);
		fdpCache.putValue(metaBag, displayFormat);
	}

	/**
	 * This method to use only to print the map before inserting into the Cache.
	 * ----COMMENT THIS METHOD WHEN NOT IN NEED.
	 *
	 * @param cacheMap
	 */
	private void printTempCacheMap(final Map<String, Map<String, Map<ConversionOption, Convertor>>> cacheMap) {
		for (Map.Entry<String, Map<String, Map<ConversionOption, Convertor>>> perCircleMap : cacheMap.entrySet()) {
			//System.out.println("-------------Configuration For Circle:" + perCircleMap.getKey());
			Map<String, Map<ConversionOption, Convertor>> tempPerCircleMap = perCircleMap.getValue();
			for (Map.Entry<String, Map<ConversionOption, Convertor>> displayValue : tempPerCircleMap.entrySet()) {
				//System.out.println("Display-Value:" + displayValue.getKey());
				Map<ConversionOption, Convertor> convertorMap = displayValue.getValue();
				for (Map.Entry<ConversionOption, Convertor> convertorMap1 : convertorMap.entrySet()) {
					//System.out.println("Conversion-Option:" + convertorMap1.getKey());
					ConverterImpl convertorImp = (ConverterImpl) convertorMap1.getValue();
					List<ConversionRules> conversionRules = convertorImp.getConversionRules();
					for (ConversionRules conversionRule : conversionRules) {
						ConversionRulesImpl ruleImp = (ConversionRulesImpl) conversionRule;
						//System.out.println("Condition:" + ruleImp);
					}
				}
			}
		}
	}

	@Override
	public ModuleType getModuleType() {
		return ModuleType.UNIT_DISPLAY_FORMAT;
	}
}
