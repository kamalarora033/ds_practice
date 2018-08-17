package com.ericsson.fdp.business.cache.datageneration.service.impl;

import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.TreeMap;

import javax.annotation.Resource;
import javax.ejb.Stateless;
import javax.inject.Inject;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ericsson.fdp.FDPCacheable;
import com.ericsson.fdp.business.cache.datageneration.service.ProductAttributeMapService;
import com.ericsson.fdp.business.vo.ProductAttributeMapCacheDTO;
import com.ericsson.fdp.common.constants.FDPConstant;
import com.ericsson.fdp.common.enums.ModuleType;
import com.ericsson.fdp.common.exception.FDPServiceException;
import com.ericsson.fdp.common.vo.FDPCircle;
import com.ericsson.fdp.core.dsm.framework.service.FDPCache;
import com.ericsson.fdp.core.serviceprovisioning.bean.FDPMetaBag;
import com.ericsson.fdp.dao.dto.UpdateCacheDTO;
import com.ericsson.fdp.dao.dto.UpdateCacheProductAttributeDTO;
import com.ericsson.fdp.dao.dto.product.ProductAttributeCacheKeyValueMappingDTO;
import com.ericsson.fdp.dao.dto.product.ProductAttributeParamNameValuesDTO;
import com.ericsson.fdp.dao.dto.product.ProductAttributeScreenCacheInstanceMappingDTO;
import com.ericsson.fdp.dao.dto.product.ProductAttributeScreenNameDTO;
import com.ericsson.fdp.dao.fdpadmin.FDPProductAttributeDAO;



@Stateless(mappedName="productAttributeMapService")
public class ProductAttributeMapServiceImpl implements ProductAttributeMapService{

	private static final Logger LOGGER= LoggerFactory.getLogger(ProductAttributeMapServiceImpl.class);	
	
	@Inject
	FDPProductAttributeDAO productAttributeDAO;
	
	/** The fdp cache. */
	@Resource(lookup = "java:app/fdpBusiness-1.0/MetaDataCache")
	private FDPCache<FDPMetaBag, FDPCacheable> fdpCache;
	
	@Override
	public boolean updateMetaCache(UpdateCacheDTO updateCacheDTO)throws FDPServiceException {
		try{
			
		FDPMetaBag metaBag = new FDPMetaBag(updateCacheDTO.getCircle(), updateCacheDTO.getModuleType(), null);
		
		LOGGER.debug("loadProductAttributeMapping:  updateMetaCache metabag "+metaBag);
		
		UpdateCacheProductAttributeDTO updateCacheProductAttribute = (UpdateCacheProductAttributeDTO)updateCacheDTO.getUiObjectDTO();
		
		LOGGER.debug("loadProductAttributeMapping:  updateMetaCache updateCacheProductAttribute object "+updateCacheProductAttribute);
		LOGGER.debug("loadProductAttributeMapping:  updateMetaCache fetching list of screen ");
		List<ProductAttributeScreenCacheInstanceMappingDTO> screenCacheList= productAttributeDAO.getScreenCacheInstanceMappingByScreenId(updateCacheProductAttribute.getId());
		LOGGER.debug("loadProductAttributeMapping:  updateMetaCache fetched list of screen with size "+screenCacheList.size());
		
		Map<Long, String > updatedParamMap= (Map<Long, String >)(updateCacheProductAttribute.getMap());
		
		if (updatedParamMap.isEmpty()){
			throw new Exception("updateCacheProductAttribute or updatedParamMap is null");
		}
		
		for(ProductAttributeScreenCacheInstanceMappingDTO screenCacheDTO: screenCacheList){
		
			LOGGER.debug("loadProductAttributeMapping:  updateMetaCache fetching list of ProductAttributeScreenCacheInstanceMappingDTO ");
			List<ProductAttributeCacheKeyValueMappingDTO> keyValueList= screenCacheDTO.getCacheKeyValueMappingDTOList();
			LOGGER.debug("loadProductAttributeMapping:  updateMetaCache fetched list of keyValueList with size "+ keyValueList.size());
			
			
			StringBuffer key=new StringBuffer(screenCacheDTO.getProductAttributeScreenName().getScreenName());
			
			
			Map<String, String> valueMap= new TreeMap<String, String>();
			
			Collections.sort(keyValueList, ProductAttributeCacheKeyValueMappingDTO.ProductAttributeCacheKeyValueMappingDTOComparator);
			
			LOGGER.debug("loadProductAttributeMapping:  updateMetaCache sorted list "+keyValueList);
			
			for(ProductAttributeCacheKeyValueMappingDTO keyValueMapDTO : keyValueList){
				
				if(updatedParamMap.containsKey(keyValueMapDTO.getProductAttributeParamName().getParamNameId())){
					
					if(FDPConstant.KEY.equalsIgnoreCase(keyValueMapDTO.getTypeInCache())){
						key.append(FDPConstant.UNDERSCORE).append(keyValueMapDTO.getProductAttributeParamName().getParamName()).append(FDPConstant.UNDERSCORE).append(updatedParamMap.get(keyValueMapDTO.getProductAttributeParamName().getParamNameId()));	
					}
					
					if(FDPConstant.VALUE.equalsIgnoreCase(keyValueMapDTO.getTypeInCache())){						
						valueMap.put(keyValueMapDTO.getProductAttributeParamName().getParamName(), updatedParamMap.get(keyValueMapDTO.getProductAttributeParamName().getParamNameId()));
					}					
				}	
			}
			LOGGER.debug("loadProductAttributeMapping:  updateMetaCache key "+key);
			LOGGER.debug("loadProductAttributeMapping:  updateMetaCache value "+valueMap);
			
			metaBag.setModuleId(key);
			
			if(0==updateCacheDTO.getAction().getValue()){
				ProductAttributeMapCacheDTO cacheDTO = new ProductAttributeMapCacheDTO();
				Map<Long, Map<String, String>> recordIdValueMap= new HashMap<Long, Map<String,String>>();
				recordIdValueMap.put(updateCacheProductAttribute.getRecordId(), valueMap);
				cacheDTO.setValueMap(recordIdValueMap);
				addUpdateValueInCache(metaBag, cacheDTO, updateCacheProductAttribute.getRecordId());
			}
			if(1==updateCacheDTO.getAction().getValue()){
				deleteValueFromCache(metaBag, updateCacheProductAttribute.getRecordId());
			}
			
		}
		
		}catch(Exception e){
			
			LOGGER.error(e.toString());
		}
		
	
		return true;
	}

	
	@Override
	public boolean initializeMetaCache(FDPCircle fdpCircle)throws FDPServiceException {

		try{
			if (fdpCircle == null) {
				throw new FDPServiceException("Circle is mandatory");
			} 
			LOGGER.debug("loadProductAttributeMapping: fetching all screen for product attribute mapping FOR CIRCLE "+fdpCircle.getCircleCode());	
	
			final List<ProductAttributeScreenNameDTO> screenNameList =productAttributeDAO.getAllProductAttributeType(fdpCircle.getCircleId());
	
			LOGGER.debug("loadProductAttributeMapping: list of all screen for product attribute mapping is fetched with size "+ screenNameList.size());	
		
				for(ProductAttributeScreenNameDTO screendto : screenNameList){
				
					LOGGER.debug("loadProductAttributeMapping: fetching all screen cache mapping for screen "+screendto.getScreenName());	
					
					List<ProductAttributeScreenCacheInstanceMappingDTO> screenCacheMappingList= productAttributeDAO.getScreenCacheInstanceMappingByScreenId(screendto.getScreenNameId());
					
					LOGGER.debug("loadProductAttributeMapping: list of all screen cache mapping with size "+screenCacheMappingList.size());
					
					LOGGER.debug("loadProductAttributeMapping: fetching all records for screen "+screendto.getScreenName());	
				
					List <List<ProductAttributeParamNameValuesDTO>> paramValueLists= productAttributeDAO.getProductAttributeParamValues(screendto.getScreenNameId());
				
					LOGGER.debug("loadProductAttributeMapping: list of all parameters for product attribute mapping is fetched with size "+paramValueLists.size());
				
							for(ProductAttributeScreenCacheInstanceMappingDTO screenCacheDto :screenCacheMappingList){
							
								Map<Long, String> paramIdCacheType = new LinkedHashMap<Long, String>();
								Map<Long, Long> paramIdKeySeq = new LinkedHashMap<Long, Long>();
								
								for(ProductAttributeCacheKeyValueMappingDTO keyValueDto : screenCacheDto.getCacheKeyValueMappingDTOList()){
									paramIdCacheType.put(keyValueDto.getProductAttributeParamName().getParamNameId(), keyValueDto.getTypeInCache());
									paramIdKeySeq.put(keyValueDto.getProductAttributeParamName().getParamNameId(), keyValueDto.getKeySequence());
								}
								
								Map<String , Map<Long, Map<String, String>>> keyValueMap= new HashMap<String , Map<Long, Map<String, String>>>();
								
								for(List<ProductAttributeParamNameValuesDTO> record: paramValueLists){
									
									Long recordId= null;
									Map<String, String> value= new LinkedHashMap<String, String>();
									StringBuffer key= new StringBuffer(screendto.getScreenName());
									TreeMap<Long ,ProductAttributeParamNameValuesDTO> keyMap= new TreeMap<Long ,ProductAttributeParamNameValuesDTO>(); 
										for(ProductAttributeParamNameValuesDTO parameter: record){
											
											recordId=parameter.getScreenNameValueId().getScreenNameValueId();
											
											if(null!=paramIdCacheType.get(parameter.getParamNameId().getParamNameId())   &&  FDPConstant.KEY.equalsIgnoreCase(paramIdCacheType.get(parameter.getParamNameId().getParamNameId())))
												keyMap.put(paramIdKeySeq.get(parameter.getParamNameId().getParamNameId()), parameter);
												
											if(null!=paramIdCacheType.get(parameter.getParamNameId().getParamNameId())   &&  FDPConstant.VALUE.equalsIgnoreCase(paramIdCacheType.get(parameter.getParamNameId().getParamNameId())))
												value.put(parameter.getParamNameId().getParamName(), parameter.getParamNameValue());
												//LOGGER.info("loadProductAttributeMapping: parameter value: "+parameter.getParamNameId().getParamName()+"  and  "+parameter.getParamNameValue());
										}
										getFullKeyInSequence(key, keyMap);
										
										if(keyValueMap.containsKey(key.toString())){
											Map<Long, Map<String, String>> map=	keyValueMap.get(key.toString());
											if(!map.containsKey(recordId)){
												map.put(recordId, value);	
											}
										}
										else{
											Map<Long, Map<String, String >> valuesMap = new HashMap<Long, Map<String, String >>();
											valuesMap.put(recordId, value);
											keyValueMap.put(key.toString(), valuesMap)	;
										}	
								}
								populateCacheForEachCacheStructure(fdpCircle, keyValueMap);
							}
			}
		
		}catch(Exception e){
			LOGGER.error(e.toString());
		}
		return true;
	}

	
	@Override
	public ModuleType getModuleType() {
		
		return ModuleType.PRODUCT_ATTRIBUTE_MAP;
	}

	
	private void populateCacheForEachCacheStructure(FDPCircle fdpCircle, Map<String , Map<Long, Map<String, String>>> keyValueMap)throws Exception{
		final FDPMetaBag metaBag = new FDPMetaBag(fdpCircle, ModuleType.PRODUCT_ATTRIBUTE_MAP, null);
		
		for(Map.Entry<String , Map<Long, Map<String, String>>> entry : keyValueMap.entrySet()){
			//LOGGER.info("loadProductAttributeMapping: key "+entry.getKey());
			metaBag.setModuleId(entry.getKey());
			ProductAttributeMapCacheDTO cacheDTO = new ProductAttributeMapCacheDTO();
			cacheDTO.setValueMap(entry.getValue());
			//LOGGER.info("loadProductAttributeMapping: value is >>>>>>>>" +entry.getValue() +"with size "+entry.getValue().size());
			fdpCache.putValue(metaBag, cacheDTO);
		}
		
	}
	
	

	
	private void addUpdateValueInCache(FDPMetaBag metaBag, ProductAttributeMapCacheDTO cacheDTO, Long recordId)throws Exception{
		
		ProductAttributeMapCacheDTO fetchedCacheDto=(ProductAttributeMapCacheDTO) (fdpCache.getValue(metaBag));
		if(fetchedCacheDto!=null){
			Map<Long, Map<String, String>> fetchedValueMap=(Map<Long, Map<String, String>>)(fetchedCacheDto.getValueMap());
			fetchedValueMap.put(recordId, cacheDTO.getValueMap().get(recordId));
			fdpCache.putValue(metaBag, fetchedCacheDto);
		}
		
		else{
			fdpCache.putValue(metaBag, cacheDTO);
		}
		
		LOGGER.debug("cache change for key {} after add/update {} ",metaBag,fdpCache.getValue(metaBag));
	}
	
	private void deleteValueFromCache(FDPMetaBag metaBag, Long recordId)throws Exception{
		
		ProductAttributeMapCacheDTO fetchedCacheDto=(ProductAttributeMapCacheDTO) (fdpCache.getValue(metaBag));
		
		if(fetchedCacheDto!=null){
			Map<Long, Map<String, String>> fetchedValueMap=(Map<Long, Map<String, String>>)(fetchedCacheDto.getValueMap());
			if(fetchedValueMap.containsKey(recordId)){
				fetchedValueMap.remove(recordId);
				fdpCache.putValue(metaBag, fetchedCacheDto);
				if(fetchedValueMap.isEmpty()){
					fdpCache.removeKey(metaBag);
				}
			}
		}
		
		LOGGER.debug("cache change for key {} after delete  {} ",metaBag,fdpCache.getValue(metaBag));
	}
	
	
private StringBuffer getFullKeyInSequence(StringBuffer key, TreeMap<Long ,ProductAttributeParamNameValuesDTO> keyMap){
		
		for(Entry<Long, ProductAttributeParamNameValuesDTO> entry : keyMap.entrySet()) {
			
			ProductAttributeParamNameValuesDTO parameter= entry.getValue();
			key.append(FDPConstant.UNDERSCORE).append(parameter.getParamNameId().getParamName()).append(FDPConstant.UNDERSCORE).append(parameter.getParamNameValue());

		}
	//	LOGGER.info("loadProductAttributeMapping : key from getFullKeyInSequence >>>>>>>>>>>>>>" +key);
	//	System.out.println("loadProductAttributeMapping : key from getFullKeyInSequence >>>>>>>>>>>>>>" +key);
		
		return key;
	}

	
}
