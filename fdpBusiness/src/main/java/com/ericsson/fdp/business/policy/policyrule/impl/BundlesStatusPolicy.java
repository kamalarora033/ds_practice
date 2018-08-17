package com.ericsson.fdp.business.policy.policyrule.impl;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.ericsson.fdp.FDPCacheable;
import com.ericsson.fdp.business.dto.Me2uProductDTO;
import com.ericsson.fdp.business.enums.Command;
import com.ericsson.fdp.business.enums.ConfigurationKey;
import com.ericsson.fdp.business.enums.PolicyStatus;
import com.ericsson.fdp.business.enums.TLVOptions;
import com.ericsson.fdp.business.policy.policyrule.PolicyRule;
import com.ericsson.fdp.business.product.Product;
import com.ericsson.fdp.business.request.FDPPolicyResponse;
import com.ericsson.fdp.business.request.impl.FDPPolicyResponseImpl;
import com.ericsson.fdp.business.request.impl.FDPResponseImpl;
import com.ericsson.fdp.business.util.RequestUtil;
import com.ericsson.fdp.business.util.ResponseUtil;
import com.ericsson.fdp.business.vo.DAInstance;
import com.ericsson.fdp.business.vo.FDPOfferAttributeVO;
import com.ericsson.fdp.business.vo.ProductAttributeMapCacheDTO;
import com.ericsson.fdp.common.constants.FDPConstant;
import com.ericsson.fdp.common.enums.ModuleType;
import com.ericsson.fdp.common.enums.Status;
import com.ericsson.fdp.core.command.FDPCommand;
import com.ericsson.fdp.core.command.param.CommandParam;
import com.ericsson.fdp.core.exception.ExecutionFailedException;
import com.ericsson.fdp.core.request.FDPRequest;
import com.ericsson.fdp.core.request.FDPResponse;
import com.ericsson.fdp.core.serviceprovisioning.bean.FDPMetaBag;
import com.ericsson.fdp.core.utils.ApplicationConfigUtil;

public class BundlesStatusPolicy extends AbstractPolicyRule implements PolicyRule {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 61415866666887L;

	@Override
	public FDPResponse displayRule(FDPRequest fdpRequest)
			throws ExecutionFailedException {
		try {
			FDPCommand fdpCommandGBAD = null;
			FDPResponse fdpResponse = null;
			
			    fdpCommandGBAD  = getGBADRequest(fdpRequest, fdpCommandGBAD);
				List<Me2uProductDTO> me2UProductDTO = getBundleStatusProducts(fdpCommandGBAD, fdpRequest);

				fdpResponse = new FDPResponseImpl(Status.SUCCESS, false, ResponseUtil.createResponseMessageInList(
						fdpRequest.getChannel(), getNotificationText(fdpRequest, me2UProductDTO), TLVOptions.SESSION_TERMINATE));

			
				return fdpResponse;

		}
		catch(Exception e){
			e.printStackTrace();
		}


		return null;
	}
	
	private String getNotificationText(FDPRequest fdpRequest,
			List<Me2uProductDTO> me2uDTOList) {
		StringBuilder notificationText = new StringBuilder("");
		int i=1;
		for(Me2uProductDTO me2uProductDTO : me2uDTOList) {
			if(null==me2uProductDTO)
				break;
			notificationText.append(i +". "+ me2uProductDTO.getProduct().getProductName()+FDPConstant.SPACE+ me2uProductDTO.getAvailableBalance()+FDPConstant.SPACE+ me2uProductDTO.getExpiryDate()+"\n");
			i++;
		}

		return notificationText.toString();
	}


	@Override
	public FDPPolicyResponse validatePolicyRule(Object input,
			FDPRequest fdpRequest, Object... otherParams)
			throws ExecutionFailedException {
		FDPPolicyResponse response = new FDPPolicyResponseImpl(PolicyStatus.SUCCESS, null, null, true, null);
		return response;
	}

	
	public static  FDPCommand getGBADRequest(final FDPRequest fdpRequest,FDPCommand fdpCommand) {
		try {
			if(null == fdpCommand) {
				final FDPCacheable fdpCommandCached = ApplicationConfigUtil.getMetaDataCache().getValue(
						new FDPMetaBag(fdpRequest.getCircle(), ModuleType.COMMAND, Command.GET_BALANCE_AND_DATE.getCommandDisplayName()));
				if(null != fdpCommandCached && fdpCommandCached instanceof FDPCommand) {
					fdpCommand = (FDPCommand) fdpCommandCached;					
					if(Status.SUCCESS.equals(fdpCommand.execute(fdpRequest))) {
						fdpRequest.addExecutedCommand(fdpCommand);
					}
				}
			}

		} catch (final Exception e) {
				e.printStackTrace();
			}
		return fdpCommand;
	}
	
	
	public List<Me2uProductDTO> getBundleStatusProducts(FDPCommand fdpCommandGBAD, FDPRequest fdpRequest) {
		
		Map<String, String> offerIdsMap = new HashMap<String, String>();
		Map<String, List<DAInstance>> dedicatedMap = new HashMap<String, List<DAInstance>>();
		
		if(null != fdpCommandGBAD){
			Map<String, CommandParam> outputParamMap = fdpCommandGBAD.getOutputParams(); 
			int offerCounter = 0;
			int DACounter = 0;
			String offerExpiryDate = null;
			String offerId = null;
			String offerType = null;
			String dateStringName = null;
			while (outputParamMap.get(FDPConstant.offerInformationPath + offerCounter + ".offerid") != null) {
				try {
					CommandParam offerIdInt = outputParamMap.get(FDPConstant.offerInformationPath + offerCounter + ".offerid");
					if(null != offerIdInt) {
						offerId = offerIdInt != null ? offerIdInt.getValue().toString() : null;
						offerType = String.valueOf(outputParamMap.get(FDPConstant.offerInformationPath + offerCounter + ".offertype").getValue());
						if(offerType != null && "2".equals(offerType))
							dateStringName = ".expirydatetime";
						else
							dateStringName = ".expirydate";
						offerExpiryDate = outputParamMap.get(FDPConstant.offerInformationPath + offerCounter + dateStringName).getValue() == null? null:
							((GregorianCalendar)outputParamMap.get(FDPConstant.offerInformationPath + offerCounter + dateStringName).getValue()).getTime().toString();
						offerIdsMap.put(offerId, convertDateFormat(offerExpiryDate));
					}
					offerCounter++;
				}
				catch(Exception e) {
					offerCounter++;
					//e.printStackTrace();
				}
			}
				while(outputParamMap.get("dedicatedaccountinformation."+DACounter+".dedicatedaccountid") != null) {
					try {
					CommandParam dedicatedActIdInt = outputParamMap.get("dedicatedaccountinformation."+DACounter+".dedicatedaccountid");
					if(null != dedicatedActIdInt) {
						List<DAInstance> daInstanceList = new ArrayList<DAInstance>();
						String dedicatedActId = dedicatedActIdInt != null ? dedicatedActIdInt.getValue().toString() : null;
						Object dedicatedActValueObj = outputParamMap.get("dedicatedaccountinformation." + DACounter + ".dedicatedaccountvalue1").getValue();
						String dedicatedActValue = dedicatedActValueObj !=null ? dedicatedActValueObj.toString() : null;
						DAInstance daInstance = new DAInstance();
						daInstance.setAmount(dedicatedActValue);
						if(dedicatedMap.get(dedicatedActId) != null)
							daInstanceList = dedicatedMap.get(dedicatedActId);
						daInstanceList.add(daInstance);
						dedicatedMap.put(dedicatedActId, daInstanceList);
						}
					DACounter++;
				}
				catch (Exception e) {
					e.printStackTrace();
				}
			}
		}
		List<Me2uProductDTO> me2uDTOList = setRecordsInDTO(offerIdsMap, dedicatedMap,fdpRequest);
		return me2uDTOList;
		
	}
	
	private String convertDateFormat(String startDateFrmt) throws ParseException {
		String formatedDate = null;
		if(startDateFrmt != null) {
			DateFormat formatter = new SimpleDateFormat("E MMM dd HH:mm:ss Z yyyy");
			Date date = (Date)formatter.parse(startDateFrmt);

			Calendar cal = Calendar.getInstance();
			cal.setTime(date);
			formatedDate = cal.get(Calendar.DATE) + "/" + (cal.get(Calendar.MONTH) + 1) + "/" + cal.get(Calendar.YEAR);
		}
		return formatedDate;
	}
	
	private List<Me2uProductDTO> setRecordsInDTO(Map<String, String> offerIdsMap,
			Map<String, List<DAInstance>> dedicatedMap, FDPRequest fdpRequest) {

		List<Me2uProductDTO> me2uDTOList = new ArrayList<Me2uProductDTO>();
		final Map<String, String> configurationMap = fdpRequest.getCircle().getConfigurationKeyValueMap();
		String amountConversionFactor = configurationMap.get(ConfigurationKey.Me2U_DATA2SHARE_AMOUNT_CONVERSION_FACTOR.getAttributeName());

		if(null == amountConversionFactor)
			amountConversionFactor = "1";
		for(String offerId : offerIdsMap.keySet()) {
			try {
				StringBuffer cacheKey = new StringBuffer(FDPConstant.BUNDLE_STATUS) ;
				cacheKey.append(FDPConstant.UNDERSCORE).append(FDPConstant.PARAMETER_OFFER_ID).append(FDPConstant.UNDERSCORE).append(offerId);				 
				ProductAttributeMapCacheDTO cacheObject = (ProductAttributeMapCacheDTO)ApplicationConfigUtil.getMetaDataCache().
					 getValue(new FDPMetaBag(fdpRequest.getCircle(), ModuleType.PRODUCT_ATTRIBUTE_MAP, cacheKey));
				FDPOfferAttributeVO tmpOfferAttributeVOObj = getproductDAMapObject(cacheObject); 					
				if(null != tmpOfferAttributeVOObj) {
					if(null != tmpOfferAttributeVOObj.getDa_id() && !tmpOfferAttributeVOObj.getDa_id().isEmpty()) {
						 Me2uProductDTO me2uProductDTO = new Me2uProductDTO();
						 me2uProductDTO.setDAID(tmpOfferAttributeVOObj.getDa_id().get(0) + "");
						 me2uProductDTO.setOfferID(offerId);
						 me2uProductDTO.setExpiryDate(offerIdsMap.get(offerId));
						 
						 HashMap<String, Long> daMap = convertListToMap(dedicatedMap.get(me2uProductDTO.getDAID()),tmpOfferAttributeVOObj.getBundleStatusProductid());
						 me2uProductDTO.setDaInstance(daMap);
						 // Counting available balance
						 Map<String, Long> daInstanceMap = me2uProductDTO.getDaInstance();
						 Long availableBalance = 0l;
						 if(null != daInstanceMap) {
							 for(String key : daInstanceMap.keySet()) {
								 availableBalance = availableBalance + daInstanceMap.get(key);
							 }
						 }
						 if(availableBalance == 0) {
							 availableBalance = Long.parseLong(dedicatedMap.get(me2uProductDTO.getDAID()).get(0).getAmount());
						 }
						 if(availableBalance > 0) {
							 if(amountConversionFactor != null) {
								 availableBalance = availableBalance / Integer.parseInt(amountConversionFactor);							 
							 }
							
							String productIdtmp =tmpOfferAttributeVOObj.getBundleStatusProductid(); 
							if(null != productIdtmp) {
						    	Product product = RequestUtil.getProductById(fdpRequest, productIdtmp);
					    	    me2uProductDTO.setProduct(product);
							}
							me2uProductDTO.setAvailableBalance(availableBalance+"");
							me2uDTOList.add(me2uProductDTO);
						 }
					}
				}	
			} catch (ExecutionFailedException e) {
				e.printStackTrace();
			}
		}
	 return me2uDTOList;
	}

	private HashMap<String, Long> convertListToMap(List<DAInstance> list, String productId) {
		HashMap<String, Long> daMap = new HashMap<String, Long>();
		if(list.size()>0 && null!= list){
			for(DAInstance daInstance : list) {
				daMap.put(productId, daInstance.getAmount()==null?null:Long.parseLong(daInstance.getAmount()));
			}
		}
		return daMap;
	}

	private static FDPOfferAttributeVO getproductDAMapObject(ProductAttributeMapCacheDTO cache){
		FDPOfferAttributeVO tmpOfferAttributeVOObj = null;
		if(null!= cache){
			tmpOfferAttributeVOObj = new FDPOfferAttributeVO();
			
		       Map<Long, Map<String, String>> valueMap= cache.getValueMap();
	              
		       for (Map<String, String> value : valueMap.values()){
		    	   Integer daId = null != value.get(FDPConstant.PARAMETER_DA) ? Integer.parseInt(value.get(FDPConstant.PARAMETER_DA)) : null;
		    	   tmpOfferAttributeVOObj.setDa_id(Arrays.asList(daId));
		    	   tmpOfferAttributeVOObj.setBundleStatusProductid(value.get(FDPConstant.PARAMETER_PRODUCT_ID));
		       }		
			
/*			Map<String,String>map = cache.getValueMap().get(FDPConstant.PARAMETER_DA);
			Integer daId = null!=cache.getValueMap().get(FDPConstant.PARAMETER_DA)?Integer.parseInt(cache.getValueMap().get(FDPConstant.PARAMETER_DA)):null;
			tmpOfferAttributeVOObj.setDa_id(daId);
			tmpOfferAttributeVOObj.setBundleStatusProductid(cache.getValueMap().get(FDPConstant.PARAMETER_PRODUCT_ID));	*/
		}
		return tmpOfferAttributeVOObj;
	}


}
