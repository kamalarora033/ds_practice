package com.ericsson.fdp.business.util;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import com.ericsson.fdp.business.tariffenquiry.configimport.constants.TariffConstants;
import com.ericsson.fdp.common.enums.Status;
import com.ericsson.fdp.common.enums.TariffEnquiryAttributeKeyType;
import com.ericsson.fdp.common.enums.TariffEnquiryOption;
import com.ericsson.fdp.common.vo.FDPCircle;
import com.ericsson.fdp.dao.dto.tariffenquiry.FDPTariffEnquiryAttributeDTO;


/**
 * This is Util class for Tariff Enquiry Attributes Format Cache.
 * 
 * @author Ericsson
 * 
 */
public class TariffEnquiryAttributesUtil {
	
	/**
	 * This method will populate the CacheMap
	 * 	MAP<CIRCLE_ID, <KEY,VALUE>>
	 * 	IF TYPE IS ATTRIBUTE THEN VALUE WILL BE OF Set<String> TYPE.
	 * 	ELSE IF TYPE IS SERVICE_CLASS THEN VALUE WILL BE OF Map<String, String> TYPE.
	 * 
	 * @param fdpAttributesDto
	 * @param cacheMap
	 */
	public static void populateCacheMap(final FDPTariffEnquiryAttributeDTO fdpAttributesDto, Map<FDPCircle, Map<String,Object>> cacheMap) {
		TariffEnquiryAttributeKeyType keyType = TariffEnquiryAttributeKeyType.getTariffEnquiryAttributeKeyType(String.valueOf(fdpAttributesDto.getTariffKeyType()));
		if(keyType!= null) {
			switch(keyType) {
			case ATTRIBUTES: 
				prepareAttributeTypeCache(keyType, fdpAttributesDto, cacheMap);
				break;
			case SERVICE_CLASS:
			case DA_ATTRIBUTES_TARIFF_VALUE:
			case PSO_ATTRIBUTES_TARIFF_VALUE:
			case COMMUNITYID_ATTRIBUTES_TARIFF_VALUE:
			case OFFERID_ATTRIBUTES_TARIFF_VALUE:
				prepareServiceClassTypeCache(keyType, fdpAttributesDto, cacheMap);
				break;
			}
		}
	}
	
	/**
	 * This method prepares the FDPCircle object.
	 * 
	 * @param fdpDisplayFormat
	 * @return
	 */
	private static FDPCircle prepareFDPCircle(final FDPTariffEnquiryAttributeDTO attributeDTO) {
		return new FDPCircle(attributeDTO.getFdpCircle().getCircleId(), attributeDTO.getFdpCircle()
				.getCircleCode(), attributeDTO.getFdpCircle().getCircleName());
	}
	
	/**
	 * This method will prepare Cache for Attributes Type.
	 * 
	 * @param keyType
	 * @param fdpAttributesDto
	 * @param cacheMap
	 */
	@SuppressWarnings("unchecked")
	private static void prepareAttributeTypeCache(final TariffEnquiryAttributeKeyType keyType, final FDPTariffEnquiryAttributeDTO fdpAttributesDto, Map<FDPCircle, Map<String,Object>> cacheMap) {
		FDPCircle fdpCircle = prepareFDPCircle(fdpAttributesDto);
		Map<String,Object> perCircleCacheObject = cacheMap.get(fdpCircle);
		String key = prepareCacheKey(keyType, fdpAttributesDto);
		if(null == perCircleCacheObject) {
			perCircleCacheObject = new HashMap<String, Object>();
			Object value = prepareCacheValueObject(keyType, fdpAttributesDto);
			perCircleCacheObject.put(key, value);
			cacheMap.put(fdpCircle, perCircleCacheObject);
		} else {
			Set<String> attributeSet = (Set<String>) perCircleCacheObject.get(key);
			if( null == attributeSet) {
				attributeSet = new HashSet<String>();
				attributeSet.add(fdpAttributesDto.getValue());
				perCircleCacheObject.put(key, attributeSet);
			} else {
				attributeSet.add(fdpAttributesDto.getValue());
			}
		}
	}
	
	/**
	 * This method will prepare Cache for Service_class Type.
	 * 
	 * @param keyType
	 * @param fdpAttributesDto
	 * @param cacheMap
	 */
	@SuppressWarnings("unchecked")
	private static void prepareServiceClassTypeCache(final TariffEnquiryAttributeKeyType keyType,
			final FDPTariffEnquiryAttributeDTO fdpAttributesDto, Map<FDPCircle, Map<String, Object>> cacheMap) {
		FDPCircle fdpCircle = prepareFDPCircle(fdpAttributesDto);
		Map<String,Object> perCircleCacheObject = cacheMap.get(fdpCircle);
		String key = prepareCacheKey(keyType, fdpAttributesDto);
		if (null != key) {
			if (null == perCircleCacheObject) {
			perCircleCacheObject = new HashMap<String, Object>();
			Object value = prepareCacheValueObject(keyType, fdpAttributesDto);
			perCircleCacheObject.put(key, value);
			cacheMap.put(fdpCircle, perCircleCacheObject);
		} else {
				Map<String, Map<String, String>> serviceClassMap = (Map<String, Map<String, String>>) perCircleCacheObject
						.get(key);
				if (null == serviceClassMap) {
					serviceClassMap = new HashMap<String, Map<String, String>>();
					Map<String, String> valueMap = new HashMap<String, String>();
					valueMap.put(TariffConstants.TARIFF_SERVICE_CLASS_MAP_STATUS,
							Status.getStatusEnum(fdpAttributesDto.getStatus()).name());
				valueMap.put(TariffConstants.TARIFF_SERVICE_CLASS_MAP_VALUE, fdpAttributesDto.getValue());
				serviceClassMap.put(String.valueOf(fdpAttributesDto.getTariffOptions()), valueMap);
				perCircleCacheObject.put(key, serviceClassMap);
			} else {
					Map<String, String> valueMap = new HashMap<String, String>();
					valueMap.put(TariffConstants.TARIFF_SERVICE_CLASS_MAP_STATUS,
							Status.getStatusEnum(fdpAttributesDto.getStatus()).name());
				valueMap.put(TariffConstants.TARIFF_SERVICE_CLASS_MAP_VALUE, fdpAttributesDto.getValue());
				serviceClassMap.put(String.valueOf(fdpAttributesDto.getTariffOptions()), valueMap);
			}
		}
	}
	}

	/**
	 * This method will prepare the Cache Key.
	 * @param keyType
	 * @param fdpAttributesDto
	 * @return
	 */
	private static String prepareCacheKey(final TariffEnquiryAttributeKeyType keyType,
			final FDPTariffEnquiryAttributeDTO fdpAttributesDto) {
		String key = null;
		StringBuffer sb = new StringBuffer();
		switch (keyType) {
		case ATTRIBUTES:
			sb.append(fdpAttributesDto.getTariffKey());
			sb.append(fdpAttributesDto.getTariffOptions());
			key = sb.toString();
			break;
		case SERVICE_CLASS:
			sb.append(fdpAttributesDto.getTariffKey());
			sb.append(TariffEnquiryOption.SC.getOptionId());
			key = sb.toString();
			break;
		case DA_ATTRIBUTES_TARIFF_VALUE:
			sb.append(fdpAttributesDto.getTariffKey());
			sb.append(TariffEnquiryOption.DA.getAttributeTariffValueId());
			key = sb.toString();
			break;
		case PSO_ATTRIBUTES_TARIFF_VALUE:
			sb.append(fdpAttributesDto.getTariffKey());
			sb.append(TariffEnquiryOption.PSO.getAttributeTariffValueId());
			key = sb.toString();
			break;
		case COMMUNITYID_ATTRIBUTES_TARIFF_VALUE:
			sb.append(fdpAttributesDto.getTariffKey());
			sb.append(TariffEnquiryOption.COMMUNITY_ID.getAttributeTariffValueId());
			key = sb.toString();
			break;
		case OFFERID_ATTRIBUTES_TARIFF_VALUE:
			sb.append(fdpAttributesDto.getTariffKey());
			sb.append(TariffEnquiryOption.OFFER_ID.getAttributeTariffValueId());
			key = sb.toString();
			break;
		}
		return key;
	}
	
	/**
	 * This method will prepare the Cache value Object.
	 *  
	 * 	IF TYPE IS ATTRIBUTE THEN VALUE WILL BE OF Set<String> TYPE.
	 * 	ELSE IF TYPE IS SERVICE_CLASS THEN VALUE WILL BE OF Map<String, String> TYPE.
	 * 
	 * @param keyType
	 * @param fdpAttributesDto
	 * @return
	 */
	private static Object prepareCacheValueObject(final TariffEnquiryAttributeKeyType keyType,
			final FDPTariffEnquiryAttributeDTO fdpAttributesDto) {
		Object value = null;
		if(TariffEnquiryAttributeKeyType.ATTRIBUTES.equals(keyType)) {
			Set<String> attributeSet = new HashSet<String>();
			attributeSet.add(fdpAttributesDto.getValue());
			value = attributeSet;
		} else if (TariffEnquiryAttributeKeyType.SERVICE_CLASS.equals(keyType)
				|| TariffEnquiryAttributeKeyType.DA_ATTRIBUTES_TARIFF_VALUE.equals(keyType)
				|| TariffEnquiryAttributeKeyType.PSO_ATTRIBUTES_TARIFF_VALUE.equals(keyType)
				|| TariffEnquiryAttributeKeyType.COMMUNITYID_ATTRIBUTES_TARIFF_VALUE.equals(keyType)
				|| TariffEnquiryAttributeKeyType.OFFERID_ATTRIBUTES_TARIFF_VALUE.equals(keyType)) {
			Map<String, Map<String,String>> serviceClassMap = new HashMap<String, Map<String,String>>();
			String key = String.valueOf(fdpAttributesDto.getTariffOptions());
			Map<String,String> valueMap = new HashMap<String, String>();
			valueMap.put(TariffConstants.TARIFF_SERVICE_CLASS_MAP_VALUE, fdpAttributesDto.getValue());
			valueMap.put(TariffConstants.TARIFF_SERVICE_CLASS_MAP_STATUS,
					((!TariffEnquiryAttributeKeyType.SERVICE_CLASS.equals(keyType) && null == fdpAttributesDto
							.getStatus()) ? Status.SUCCESS.name() : Status.getStatusEnum(fdpAttributesDto.getStatus())
							.name()));
			serviceClassMap.put(key, valueMap);
			value = serviceClassMap;
		}
		return value;
	}
}
