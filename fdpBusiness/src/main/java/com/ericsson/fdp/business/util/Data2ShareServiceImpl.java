package com.ericsson.fdp.business.util;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.ejb.Stateless;
import javax.inject.Inject;

import com.ericsson.fdp.business.dto.Me2uProductDTO;
import com.ericsson.fdp.business.enums.AuxRequestParam;
import com.ericsson.fdp.business.enums.ConfigurationKey;
import com.ericsson.fdp.business.product.Product;
import com.ericsson.fdp.business.vo.DAInstance;
import com.ericsson.fdp.business.vo.FDPOfferAttributeVO;
import com.ericsson.fdp.business.vo.OfferDTO;
import com.ericsson.fdp.common.constants.FDPConstant;
import com.ericsson.fdp.common.enums.ModuleType;
import com.ericsson.fdp.core.command.FDPCommand;
import com.ericsson.fdp.core.command.param.CommandParam;
import com.ericsson.fdp.core.exception.ExecutionFailedException;
import com.ericsson.fdp.core.request.FDPRequest;
import com.ericsson.fdp.core.request.impl.FDPSMPPRequestImpl;
import com.ericsson.fdp.core.serviceprovisioning.bean.FDPMetaBag;
import com.ericsson.fdp.core.utils.ApplicationConfigUtil;
import com.ericsson.fdp.dao.fdpadmin.FDPProductDAO;

@Stateless
public class Data2ShareServiceImpl implements Data2ShareService {
	
	@Inject
	FDPProductDAO fdpProductDAO;

	public List<Me2uProductDTO> getData2ShareProducts(FDPCommand fdpCommandGBAD, FDPRequest fdpRequest) {
		
		Map<String, OfferDTO> offerIdsMap = new HashMap<String, OfferDTO>();
		Map<String, List<DAInstance>> dedicatedMap = new HashMap<String, List<DAInstance>>();
		
		if(null != fdpCommandGBAD){
			Map<String, CommandParam> outputParamMap = fdpCommandGBAD.getOutputParams(); 
			int offerCounter = 0;
			int DACounter = 0;
			String offerExpiryDate = null;
			String offerId = null;
			String accountValueA = null;
			CommandParam accountValue = (null == outputParamMap.get(FDPConstant.ACCOUNT_VALUE) ? outputParamMap.get(FDPConstant.ACCOUNT_VALUE_1) :outputParamMap.get(FDPConstant.ACCOUNT_VALUE));
			if(null != accountValue) {
				accountValueA = accountValue.getValue().toString();
			}
			while (outputParamMap.get(FDPConstant.offerInformationPath+offerCounter+".offerid") != null) {
				try {

					CommandParam offerIdInt = outputParamMap.get(FDPConstant.offerInformationPath+offerCounter+".offerid");
					if(null != offerIdInt) {
						offerId = offerIdInt!=null?offerIdInt.getValue().toString():null;

						offerExpiryDate = outputParamMap.get(FDPConstant.offerInformationPath+offerCounter+".expirydatetime").getValue()==null?null:
							((GregorianCalendar)outputParamMap.get(FDPConstant.offerInformationPath+offerCounter+".expirydatetime").getValue()).getTime().toString();
						
						OfferDTO offerDTOToSet = convertDateFormat(offerExpiryDate);
						
						CommandParam offerProductId = outputParamMap.get(FDPConstant.offerInformationPath+offerCounter+".productid");
						String offerProductIdStr = null;
						if(null != offerProductId) {
							offerProductIdStr = offerProductId.getValue() == null?null:offerProductId.getValue().toString();
							if(null != offerProductIdStr) {
								List<String> offerInstance = null;
								if(null != offerIdsMap.get(offerId)) {
									OfferDTO offerDTO = offerIdsMap.get(offerId);
									if(null != offerDTO) {
										if(offerDTO.getExpiryDateAPI().compareTo(offerDTOToSet.getExpiryDateAPI()) > 0 ) {
											offerDTOToSet.setExpiryDate(offerDTO.getExpiryDate());
											offerDTOToSet.setExpiryDateAPI(offerDTO.getExpiryDateAPI());
										}
										
										if(null != offerDTO.getInstanceIds()) {
											offerInstance = offerDTO.getInstanceIds();
											if(null == offerInstance || offerInstance.isEmpty()) {
												offerInstance = new ArrayList<String>();
											}
											offerInstance.add(offerProductIdStr);
										}
										else {
											offerInstance = new ArrayList<String>();
											offerInstance.add(offerProductIdStr);
										}
									}
								}
								else {
									offerInstance = new ArrayList<String>();
									offerInstance.add(offerProductIdStr);
								}
								if (null != offerInstance)
									offerDTOToSet.setInstanceIds(offerInstance);
							}
						}
						
						
						offerIdsMap.put(offerId, offerDTOToSet);
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
						String daInstace = null;
						List<DAInstance> daInstanceList = new ArrayList<DAInstance>();
						String dedicatedActId = dedicatedActIdInt!=null?dedicatedActIdInt.getValue().toString():null;
						if(outputParamMap.get("dedicatedaccountinformation."+DACounter+".dedicatedaccountunittype").getValue().toString().equals(FDPConstant.DATA2ShareUnitType)) {
							Object dedicatedActValueObj = outputParamMap.get("dedicatedaccountinformation."+DACounter+".dedicatedaccountvalue1").getValue();
							String dedicatedActValue = dedicatedActValueObj!=null?dedicatedActValueObj.toString():null;

							CommandParam daInstanceID = outputParamMap.get("dedicatedaccountinformation."+DACounter+".productid");

							if(daInstanceID != null) {
								daInstace = daInstanceID.getValue().toString();
							}
							else {
								daInstace = "-1"; // To store data having no sub instances
							}
								DAInstance daInstance = new DAInstance();
								daInstance.setAmount(dedicatedActValue);
								daInstance.setProductId(daInstace);
								
								if(dedicatedMap.get(dedicatedActId) != null) {
									daInstanceList = dedicatedMap.get(dedicatedActId);
								}
								
								daInstanceList.add(daInstance);
							

							dedicatedMap.put(dedicatedActId, daInstanceList);
						}
					}
					DACounter++;
				}
					
				catch (Exception e) {
					e.printStackTrace();
				}
			}
				List<Me2uProductDTO> me2uDTOList = setRecordsInDTO(offerIdsMap, dedicatedMap,fdpRequest, accountValueA);
				return me2uDTOList;
		}
		return null;
		
	}
	
	private OfferDTO convertDateFormat(String startDateFrmt) throws ParseException {
		String dateStr = null;
		OfferDTO offerDTO = new OfferDTO();
		if(startDateFrmt != null) {
			
			DateFormat formatter = new SimpleDateFormat("E MMM dd HH:mm:ss Z yyyy");
			DateFormat formatter2 = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.S");
			Date date = (Date)formatter.parse(startDateFrmt);
			dateStr = formatter2.format(date);

			Calendar cal = Calendar.getInstance();
			cal.setTime(date);
			String formatedDate = cal.get(Calendar.DATE) + "/" + (cal.get(Calendar.MONTH) + 1) + "/" + cal.get(Calendar.YEAR);
			offerDTO.setExpiryDate(formatedDate);
			offerDTO.setExpiryDateAPI(dateStr);
		}
		return offerDTO;
	}
	
	private List<Me2uProductDTO> setRecordsInDTO(Map<String, OfferDTO> offerIdsMap,
			Map<String, List<DAInstance>> dedicatedMap, FDPRequest fdpRequest, String accountValue) {
		
		List<Me2uProductDTO> me2uDTOList = new ArrayList<Me2uProductDTO>();
		final Map<String, String> configurationMap = fdpRequest.getCircle().getConfigurationKeyValueMap();
		String data2shareSubscriberDA = configurationMap.get(ConfigurationKey.Me2U_DATA2SHARE_SUBSCRIBER_DA.getAttributeName());
		String amountConversionFactor = configurationMap.get(ConfigurationKey.Me2U_DATA2SHARE_AMOUNT_CONVERSION_FACTOR.getAttributeName());
		((FDPSMPPRequestImpl)fdpRequest).putAuxiliaryRequestParameter(AuxRequestParam.DATA2SHARE_SUBSCRIBER_DAID, data2shareSubscriberDA);
		String excludedOfferId = configurationMap.get(ConfigurationKey.Me2U_DATA2SHARE_EXCLUDED_OFFERID.getAttributeName());
		int daID = 0;
		if(null != data2shareSubscriberDA) {
			daID = Integer.parseInt(data2shareSubscriberDA);
		}
		if(null == amountConversionFactor)
			amountConversionFactor = "1";
		if(null==excludedOfferId)
			excludedOfferId = "0";
		for(String offerId : offerIdsMap.keySet()) {
			try {
				FDPOfferAttributeVO tmpOfferAttributeVOObj = (FDPOfferAttributeVO)ApplicationConfigUtil.getMetaDataCache().
						getValue(new FDPMetaBag(fdpRequest.getCircle(), ModuleType.OFFER_ATTRIBUTE, offerId));
		
				if(null != tmpOfferAttributeVOObj && !excludedOfferId.contains(offerId)) {
					if(null != tmpOfferAttributeVOObj.getDa_id()) {
						List<Integer> DAList = tmpOfferAttributeVOObj.getDa_id();
						for(Integer DAID : DAList) {
							if(null != DAID && null != dedicatedMap.get(DAID.toString())) {
								if(DAID != daID) {
									Me2uProductDTO me2uProductDTO = new Me2uProductDTO();
									int productId = 0;
									Set<Integer> productIds = tmpOfferAttributeVOObj.getProductid();

									if(null != productIds) {
										Iterator<Integer> it = productIds.iterator();
										while(it.hasNext()){
											int tempProduct = it.next();
											Product product = RequestUtil.getProductById(fdpRequest, tempProduct+"");
											if(null!= product && (product.getProductType().toString()).equals(FDPConstant.DATA2Share))
											{
												productId = tempProduct;
												me2uProductDTO.setProduct(product);
												break;
											}
										}
									}

									if(productId > 0) {
										me2uProductDTO.setDAID(DAID+"");
										me2uProductDTO.setOfferID(offerId);
										me2uProductDTO.setExpiryDate(offerIdsMap.get(offerId).getExpiryDate());
										me2uProductDTO.setExpiryDateAPI(offerIdsMap.get(offerId).getExpiryDateAPI());

										HashMap<String, Long> daMap = convertListToMap(dedicatedMap.get(me2uProductDTO.getDAID()), offerIdsMap.get(offerId).getInstanceIds());
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

											me2uProductDTO.setAvailableBalance(availableBalance+"");
											me2uProductDTO.setAccountValue(accountValue);
											me2uDTOList.add(me2uProductDTO);
										}
									}
								}
								break;
							}
						}
					}
				}

			} catch (ExecutionFailedException e) {
				e.printStackTrace();
			}
		}

		return me2uDTOList;
	}

	private HashMap<String, Long> convertListToMap(List<DAInstance> list, List<String> offerInstance) {
		HashMap<String, Long> daMap = new HashMap<String, Long>();
		if(null != list && null != offerInstance) {
			for(String offerIns : offerInstance) {
				for(DAInstance daInstance : list) {
					if(!daInstance.getProductId().equals("-1") && offerIns.equals(daInstance.getProductId())) {
						daMap.put(daInstance.getProductId(), daInstance.getAmount()==null?null:Long.parseLong(daInstance.getAmount()));
						break;
					}
				}
			}
		}
		return daMap;
	}

	@Override
	public Integer getOfferIdOnProductId(String productId, String data2ShareOfferId) {
		Integer offerId = fdpProductDAO.getOfferIdOnProductId(productId, data2ShareOfferId);
		return offerId;
	}

}
