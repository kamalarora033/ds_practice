package com.ericsson.fdp.business.util;

import ch.qos.logback.classic.Logger;

import com.ericsson.fdp.business.enums.AuxRequestParam;
import com.ericsson.fdp.business.enums.ivr.FulfillmentActionTypes;
import com.ericsson.fdp.business.request.impl.FulfillmentRequestImpl;
import com.ericsson.fdp.business.vo.ProductAttributeMapCacheDTO;
import com.ericsson.fdp.common.constants.FDPConstant;
import com.ericsson.fdp.common.enums.ModuleType;
import com.ericsson.fdp.core.config.utils.PropertyUtils;
import com.ericsson.fdp.core.exception.ExecutionFailedException;
import com.ericsson.fdp.core.logging.FDPLogger;
import com.ericsson.fdp.core.request.FDPRequest;
import com.ericsson.fdp.core.serviceprovisioning.bean.FDPMetaBag;
import com.ericsson.fdp.core.utils.ApplicationConfigUtil;

public class ProductAttributeCacheUtil {

	public static ProductAttributeMapCacheDTO getOfferIdByDeviceTypeAndImei(FDPRequest fdpRequest, Logger circleLogger) {
		ProductAttributeMapCacheDTO fdpCacheableObject = null;
  		StringBuilder key = new StringBuilder(FDPConstant.HANDSET_BASED_CHARGING);
			
		key.append(FDPConstant.UNDERSCORE).append(FDPConstant.PARAMETER_IMEI_NUMBER).append(FDPConstant.UNDERSCORE);
		String imei = null;
		String devType = null;
		if(fdpRequest instanceof FulfillmentRequestImpl && FulfillmentActionTypes.DEVICE_CHANGE.equals(((FulfillmentRequestImpl)fdpRequest).getActionTypes())){
			imei = (String) fdpRequest.getAuxiliaryRequestParameter(AuxRequestParam.OLD_IMEI);
			devType = (String) fdpRequest.getAuxiliaryRequestParameter(AuxRequestParam.OLD_DEVICE_TYPE);
		}
		else{
			imei = (String) fdpRequest.getAuxiliaryRequestParameter(AuxRequestParam.IMEI);
			devType = (String) fdpRequest.getAuxiliaryRequestParameter(AuxRequestParam.DEVICE_TYPE);
		}
		
		String imeiLengthStr = PropertyUtils.getProperty(FDPConstant.IMEI_MAX_LENGTH);
		Integer imeiLength = imeiLengthStr != null ? Integer.parseInt(imeiLengthStr) : FDPConstant.ZERO;
		FDPLogger.debug(circleLogger, ProductAttributeCacheUtil.class, "getOfferIdByDeviceTypeAndImei()", LoggerUtil.getRequestAppender(fdpRequest) + "Max length of IMEI from config.properties file :" + imeiLength);
		FDPLogger.debug(circleLogger, ProductAttributeCacheUtil.class, "getOfferIdByDeviceTypeAndImei()", LoggerUtil.getRequestAppender(fdpRequest) + "Getting Value for the Key:: " + key);
		imei = (imei != null && imei.length() > imeiLength && imeiLengthStr != null ? imei.substring(FDPConstant.ZERO, imeiLength) : imei);
		
		key.append(imei);
		key.append(FDPConstant.UNDERSCORE).append(FDPConstant.PARAMETER_DEVICE_TYPE).append(FDPConstant.UNDERSCORE);
		key.append(devType);
			
		FDPLogger.info(circleLogger, ProductAttributeCacheUtil.class, "getOfferIdByDeviceTypeAndImei()", "Scratching map with Key : " + key);
		 try {
			fdpCacheableObject = (ProductAttributeMapCacheDTO)ApplicationConfigUtil.getMetaDataCache().getValue(
					new FDPMetaBag(fdpRequest.getCircle(), ModuleType.PRODUCT_ATTRIBUTE_MAP, key));
		} catch (ExecutionFailedException e) {
			FDPLogger.info(circleLogger, ProductAttributeCacheUtil.class, "getOfferIdByDeviceTypeAndImei()", "Error occured while fetching from cache, key " + key);
		}
		
		return fdpCacheableObject;
	}
}
