package com.ericsson.fdp.business.fulfillment.service.impl;

import com.ericsson.fdp.FDPCacheable;
import com.ericsson.fdp.business.enums.AuxRequestParam;
import com.ericsson.fdp.business.enums.Command;
import com.ericsson.fdp.business.fulfillment.service.impl.AbstractFDPFulFillmentServiceImpl;
import com.ericsson.fdp.business.product.Product;
import com.ericsson.fdp.business.request.impl.FDPMetadataResponseImpl;
//import com.ericsson.fdp.business.response.fulfillment.xml.OfferDetail;
import com.ericsson.fdp.business.util.MVELUtil;
import com.ericsson.fdp.business.util.RequestUtil;
import com.ericsson.fdp.business.vo.DAInstance;
import com.ericsson.fdp.business.vo.FDPOfferAttributeVO;
import com.ericsson.fdp.business.vo.FDPOfferDTO;
import com.ericsson.fdp.common.constants.FDPConstant;
import com.ericsson.fdp.common.enums.ModuleType;
import com.ericsson.fdp.common.enums.Status;
import com.ericsson.fdp.common.util.DateUtil;
import com.ericsson.fdp.common.vo.FDPCircle;
import com.ericsson.fdp.core.command.FDPCommand;
import com.ericsson.fdp.core.command.param.CommandParam;
import com.ericsson.fdp.core.exception.ExecutionFailedException;
import com.ericsson.fdp.core.request.FDPRequest;
import com.ericsson.fdp.core.request.FDPResponse;
import com.ericsson.fdp.core.serviceprovisioning.bean.FDPMetaBag;
import com.ericsson.fdp.core.utils.ApplicationConfigUtil;

import java.util.ArrayList;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.ejb.Stateless;

@Stateless
public class ProductProvisioningFulfillmentViewHistoryServiceImpl extends AbstractFDPFulFillmentServiceImpl {
    
	protected /* varargs */ FDPResponse executeService(FDPRequest fdpRequest, Object ... additionalInformations) throws ExecutionFailedException {
		
        FDPMetadataResponseImpl fdpResponse = new FDPMetadataResponseImpl(Status.FAILURE, true, null);
        //ArrayList<OfferDetail> subscribedOffers = new ArrayList<OfferDetail>();
        List<String> subscribedOffers = new ArrayList<String>();
        //Set<Long> offerIds = new HashSet<Long>();
        Map<Integer, List<DAInstance>> daInstancemap = null;
        Map<Long, List<FDPOfferDTO>> offerMap = null;
        FDPCommand fdpCommandOutput = executeCommand(fdpRequest, Command.GET_BALANCE_AND_DATE);
        
        if (Integer.parseInt(fdpCommandOutput.getResponseError().getResponseCode()) == Status.SUCCESS.getStatus()) {
        	 daInstancemap = this.getDaMap(fdpCommandOutput);
             offerMap = this.getOfferMap(fdpCommandOutput);
             
             Map<Long, List<Integer>> offerDaMap = this.getDaIdsFromCache(fdpRequest, offerMap.keySet());
             List<DAInstance> daInstances = null;
          
             // Iterate the offerDaMap for each offerId and get the DA ids associated with offer id
             for (Long offerIdInMap : offerDaMap.keySet()) {
             	List<Integer> daIds = offerDaMap.get(offerIdInMap);
             	
             	if (daIds.size() > 0 && !daIds.contains(-1)) {
             		
             		/*
             		  Iterate the daIds and check if product Id is associated with daId and same product Id is associated with offer Id that is linked with the daId then 
             		  append that daId with product Name and offer Id
             		  if product Id is not associated with daId then check associated offer Id has offer instance without product id, if it is present then 
             		  append that daId with product Name and offer Id 
             		*/
             		StringBuilder daAppenderWithoutProductId = new StringBuilder();
             		for (Integer daId : daIds) {
                 		daInstances = daInstancemap.get(daId);
                 		if (daInstances != null) {
                 			 
                 			for (DAInstance daInstance : daInstances) {
                 				List<FDPOfferDTO> offerDtoList = offerMap.get(offerIdInMap);
                 				StringBuilder daAppender = new StringBuilder();
                     			if (daInstance.getProductId() != null) {
                     				for (FDPOfferDTO offerDto : offerDtoList) {
                     					if(offerDto.getProductId() != null && offerDto.getProductId().equals(daInstance.getProductId())) {
                     						daAppender.append(daInstance.getDaId()).append(FDPConstant.COMMA).append(daInstance.getStartDate()).append(FDPConstant.COMMA)
                                     		.append(daInstance.getEndDate()).append(FDPConstant.COMMA).append(daInstance.getDaType()).append(FDPConstant.COMMA)
                                     		.append(daInstance.getAmount()).append(FDPConstant.LOGGER_DELIMITER);
                     						
                     						Long productId = this.getProductId(fdpRequest, offerIdInMap);
                                         	
                                         	if (productId != null) {
                                         		Product product =  RequestUtil.getProductById((FDPRequest)fdpRequest, productId.toString());
                                         		if (product != null) {
                                         			String endDate = offerDto.getEndDate() != null ? offerDto.getEndDate() : null;
                                         			subscribedOffers.add(product.getProductName() + FDPConstant.COMMA + offerIdInMap + FDPConstant.COMMA + endDate
                                     						+ FDPConstant.LOGGER_DELIMITER + daAppender + FDPConstant.SEMICOLON);
                                         		}
                                         	}
                     						break;
                     					}
                     				}
                     			} else if (daInstance.getProductId() == null) {
                     				for (FDPOfferDTO offerDto : offerDtoList) {
                     					if(offerDto.getProductId() == null) {
                     						daAppenderWithoutProductId.append(daInstance.getDaId()).append(FDPConstant.COMMA).append(daInstance.getStartDate()).append(FDPConstant.COMMA)
                                     		.append(daInstance.getEndDate()).append(FDPConstant.COMMA).append(daInstance.getDaType()).append(FDPConstant.COMMA)
                                     		.append(daInstance.getAmount()).append(FDPConstant.LOGGER_DELIMITER);
                     						break;
                     					}
                     				}
                     			}
                     			
                 			}
                 		} 
                 	}
             		if (daAppenderWithoutProductId != null && daAppenderWithoutProductId.length() > 0) {
             			Long productId = this.getProductId(fdpRequest, offerIdInMap);
                     	
                     	if (productId != null) {
                     		Product product =  RequestUtil.getProductById((FDPRequest)fdpRequest, productId.toString());
                     		if (product != null) {
                     			List<FDPOfferDTO> offerDtoList = offerMap.get(offerIdInMap);
                     			FDPOfferDTO offerDto = offerDtoList.get(0);
                     			String endDate = offerDto.getEndDate() != null ? offerDto.getEndDate() : null;
                     			subscribedOffers.add(product.getProductName() + FDPConstant.COMMA + offerIdInMap + FDPConstant.COMMA + endDate
                 						+ FDPConstant.LOGGER_DELIMITER + daAppenderWithoutProductId + FDPConstant.SEMICOLON);
                     		}
                     	}
             		}
             	}
             	// If OfferId does not contain any DAs then only log offer Information
             	if (daIds.size() == 1 && daIds.contains(-1)) {
             		Long productId = this.getProductId(fdpRequest, offerIdInMap);
                 	
                 	if (productId != null) {
                 		Product product =  RequestUtil.getProductById((FDPRequest)fdpRequest, productId.toString());
                 		if (product != null) {
                 			List<FDPOfferDTO> offerDtoList = offerMap.get(offerIdInMap);
                 			FDPOfferDTO offerDto = offerDtoList.get(0);
                 			String endDate = offerDto.getEndDate() != null ? offerDto.getEndDate() : null;
                 			subscribedOffers.add(product.getProductName() + FDPConstant.COMMA + offerIdInMap + FDPConstant.COMMA + endDate
             						+ FDPConstant.LOGGER_DELIMITER + FDPConstant.SEMICOLON);
                 		}
                 	}
             	}	
             }
             
             fdpResponse.setExecutionStatus(Status.SUCCESS);
             if (subscribedOffers.isEmpty()) {
                 //fdpResponse.setFulfillmentResponse("No offer subscribed");
            	 //fdpResponse.putAuxiliaryRequestParameter(AuxRequestParam.VIEW_HISTORY_OFFER_DETAILS, new OfferDetail());
             } else {
                 //fdpResponse.putAuxiliaryRequestParameter(AuxRequestParam.VIEW_HISTORY_OFFER_DETAILS, new OfferDetail(subscribedOffers));
             }
            
        } else {
        	fdpResponse.setResponseError(fdpCommandOutput.getResponseError());
        }
        return fdpResponse;
    }

    /*private List<FDPOfferAttributeVO> getSubscribedOffersFromCache(FDPRequest fdpRequest, Map<String, Object> subscribedOffersMap) throws ExecutionFailedException {
        ArrayList<FDPOfferAttributeVO> subscribedOffersList = new ArrayList<FDPOfferAttributeVO>();
        for (Map.Entry<String, Object> offerEntry : subscribedOffersMap.entrySet()) {
            FDPOfferAttributeVO tmpOfferAttributeVOObj = (FDPOfferAttributeVO)ApplicationConfigUtil.getMetaDataCache().getValue(new FDPMetaBag(fdpRequest.getCircle(), ModuleType.OFFER_ATTRIBUTE, Long.valueOf(offerEntry.getKey().toString())));
            if (tmpOfferAttributeVOObj == null || tmpOfferAttributeVOObj.getProductid() == null) continue;
            subscribedOffersList.add(tmpOfferAttributeVOObj);
        }
        return subscribedOffersList;
    }*/

   /* private Map<String, Object> getSubscribedOffersMap(FDPRequest fdpRequest) throws ExecutionFailedException {
        MVELUtil.executeCachedCommand((FDPRequest)fdpRequest, (String)Command.GET_OFFERS.getCommandDisplayName());
        return MVELUtil.getSubscribedOffersNew((FDPCommand)fdpRequest.getExecutedCommand(Command.GET_OFFERS.getCommandName()));
    }
    */
	
	private Long getProductId (FDPRequest fdpRequest, Long offerId) throws ExecutionFailedException {
		Long productId = null;
		FDPOfferAttributeVO offerDTO = (FDPOfferAttributeVO) ApplicationConfigUtil.getMetaDataCache().getValue(new FDPMetaBag(fdpRequest.getCircle(), 
				ModuleType.OFFER_ATTRIBUTE, offerId));
		
		if (offerDTO != null) {
			Set<Integer> productIdList = offerDTO.getProductid();
			Iterator<Integer> ite = productIdList.iterator();
			while (ite.hasNext()) {
				productId = Long.valueOf(ite.next().longValue());
				break;
			}
		}
		
		return productId;
	}
	
	private Map<Long, List<Integer>> getDaIdsFromCache (FDPRequest fdpRequest,  Set<Long> offerIds) throws ExecutionFailedException {
		Map<Long, List<Integer>> daIdList = new HashMap<Long, List<Integer>>();
		
		for(Long offerId : offerIds) {
			FDPOfferAttributeVO offerDtoVo = (FDPOfferAttributeVO) ApplicationConfigUtil.getMetaDataCache().getValue(new FDPMetaBag(fdpRequest.getCircle(), 
					ModuleType.OFFER_ATTRIBUTE, offerId));
			if (offerDtoVo != null) {
				daIdList.put(offerId, offerDtoVo.getDa_id());
			}
		}
		return daIdList;
	}
    
    /**
     * This method calculate the DA related values from Comamnd.
     * 
      * @param executedCommand
     * @return
     */
	public static Map<String, String> getDaDetailsForUser(final FDPCommand executedCommand) {
		final Map<String, String> responseMap = new HashMap<String, String>();
		String pathkey = null;
		int i = 0;
		final String paramterName = "dedicatedAccountInformation";
		final String dedicatedAccountId  = "dedicatedAccountId";
		final String dedicatedAccountValue1 = "dedicatedAccountValue1";
		final String dedicatedAccountUnitType = "dedicatedAccountUnitType";
		final String startDate = "startDate";
		final String expiryDate = "expiryDate";
		
		String defaultExpiryDate = MVELUtil.getDefaultExpiryDate();
		
		while (executedCommand.getOutputParam(pathkey = (paramterName + FDPConstant.PARAMETER_SEPARATOR
		     + i + FDPConstant.PARAMETER_SEPARATOR +dedicatedAccountId)) != null) {
		    
			 final String dedicatedAccountValue1_Value = paramterName + FDPConstant.PARAMETER_SEPARATOR + i + FDPConstant.PARAMETER_SEPARATOR + dedicatedAccountValue1;
			 final String ddedicatedAccountUnitType_Value = paramterName + FDPConstant.PARAMETER_SEPARATOR + i + FDPConstant.PARAMETER_SEPARATOR + dedicatedAccountUnitType;
			 final String startDate_Value = paramterName + FDPConstant.PARAMETER_SEPARATOR + i + FDPConstant.PARAMETER_SEPARATOR + startDate;
			 final String expiryDate_Value = paramterName + FDPConstant.PARAMETER_SEPARATOR + i + FDPConstant.PARAMETER_SEPARATOR + expiryDate;
			 
			 final String userDaId = executedCommand.getOutputParam(pathkey).getValue().toString();
			 final String userDaValue = executedCommand.getOutputParam(dedicatedAccountValue1_Value).getValue().toString();
			 final String userDaUnitType = executedCommand.getOutputParam(ddedicatedAccountUnitType_Value).getValue().toString();
			 
			 final Object userDaStartDate = (null != executedCommand.getOutputParam(startDate_Value)) ? executedCommand.getOutputParam(startDate_Value).getValue():
			     defaultExpiryDate;
			 final Object userDaExpiry = (null != executedCommand.getOutputParam(expiryDate_Value)) ? executedCommand.getOutputParam(expiryDate_Value).getValue():
			     defaultExpiryDate;
		 
			 responseMap.put(userDaId, "|"+userDaId+","+userDaStartDate+","+userDaExpiry+","+userDaUnitType+","+userDaValue+"|");
			 i++;
		}
		return responseMap;
		     
		}
	
	/**
	 * The method is used to get the DA List.
	 * 
	 * @param fdpRequest
	 * @return
	 * @throws ExecutionFailedException
	 */
	private Map<Integer, List<DAInstance>> getDaMap(final FDPCommand fdpCommandOutput)
			throws ExecutionFailedException {
		
		DAInstance daInstance = null;
		Map<Integer, List<DAInstance>> daInstanceMap = new HashMap<Integer, List<DAInstance>>();
		List<DAInstance> daInstanceList = null;
		
		if (fdpCommandOutput != null) {
			
			Map<String, CommandParam> outputParamMap = fdpCommandOutput.getOutputParams();
			int counter = 0;
			
			while (outputParamMap.get("dedicatedaccountinformation."+ counter +".dedicatedaccountid") != null) {
				
				CommandParam dedicatedActIdInt = outputParamMap.get("dedicatedaccountinformation."+ counter +".dedicatedaccountid");
				
				if(null != dedicatedActIdInt) {
					
					String dedicatedActValue = null;
					String dedicatedActType = null;
					Object dedicatedActStartDate = null;
					Object dedicatedActEndDate = null;
					String productId = null;
					
					String dedicatedActId = dedicatedActIdInt.getValue().toString();
					
					if (outputParamMap.get("dedicatedaccountinformation."+ counter +".dedicatedaccountvalue1") != null) {
						dedicatedActValue = outputParamMap.get("dedicatedaccountinformation."+ counter +".dedicatedaccountvalue1").getValue().toString();
					}
					
					if (outputParamMap.get("dedicatedaccountinformation."+ counter +".dedicatedaccountunittype") != null) {
						dedicatedActType = outputParamMap.get("dedicatedaccountinformation."+ counter +".dedicatedaccountunittype").getValue().toString();
					}
					
					if (outputParamMap.get("dedicatedaccountinformation."+ counter +".startdate") != null) {
						dedicatedActStartDate = outputParamMap.get("dedicatedaccountinformation."+ counter +".startdate").getValue();
					}
					
					if (outputParamMap.get("dedicatedaccountinformation."+ counter +".expirydate") != null) {
						dedicatedActEndDate = outputParamMap.get("dedicatedaccountinformation."+ counter +".expirydate").getValue();
					}
					
					if (outputParamMap.get("dedicatedaccountinformation."+ counter +".productid") != null) {
						productId = outputParamMap.get("dedicatedaccountinformation."+ counter +".productid").getValue().toString();
					}
					
					daInstance = new DAInstance();
					
					daInstance.setDaId(dedicatedActId);
					if (dedicatedActValue != null) {
						daInstance.setAmount(dedicatedActValue);
					}
					if (dedicatedActType != null) {
						daInstance.setDaType(dedicatedActType);
					}
					if (dedicatedActStartDate != null) {
						daInstance.setStartDate(DateUtil.gregorianCalendarToSimpleDateConvertoer(((GregorianCalendar)dedicatedActStartDate).getTime().getTime(), FDPConstant.VIEW_HISTPRY_DATE_PATTERN_WITH_FULL_TIME));
					}
					if (dedicatedActEndDate != null) {
						daInstance.setEndDate(DateUtil.gregorianCalendarToSimpleDateConvertoer(((GregorianCalendar)dedicatedActEndDate).getTime().getTime(), FDPConstant.VIEW_HISTPRY_DATE_PATTERN_WITH_FULL_TIME));
					}
					if (productId != null) {
						daInstance.setProductId(productId);
					}
					
					Integer daId = Integer.parseInt(dedicatedActId);
					if (daInstanceMap.containsKey(daId)) {
						List<DAInstance> daInstanceList1 = daInstanceMap.get(daId);
						daInstanceList1.add(daInstance);
						daInstanceMap.put(daId, daInstanceList1);
					} else {
						daInstanceList = new ArrayList<DAInstance>();
						daInstanceList.add(daInstance);
						daInstanceMap.put(daId, daInstanceList);
					}
				}
				counter++;
			}		
		}
		return daInstanceMap;
	}
	
	/**
	 * The method is used to get the Offer Information.
	 * 
	 * @param fdpRequest
	 * @return
	 * @throws ExecutionFailedException
	 */
	private Map<Long, List<FDPOfferDTO>> getOfferMap(final FDPCommand fdpCommandOutput)
			throws ExecutionFailedException {
		
		FDPOfferDTO offerInstance = null;
		List<FDPOfferDTO> offerList = null;
		Map<Long, List<FDPOfferDTO>> offerMap = new HashMap<Long, List<FDPOfferDTO>>();
		
		if (fdpCommandOutput != null) {
			
			Map<String, CommandParam> outputParamMap = fdpCommandOutput.getOutputParams();
			int counter = 0;
			
			while (outputParamMap.get("offerinformationlist."+ counter +".offerid") != null) {
				
				CommandParam offerIdInt = outputParamMap.get("offerinformationlist."+ counter +".offerid");
				String offerId = null;
				String productId = null;
				Object expiryDate = null;
				Object startDate = null;
				/*String dedicatedActId = null;
				String dedicatedActType = null;
				String dedicatedActBalance = null;
				String dedicatedActStartDate = null;
				String dedicatedActEndDate = null;*/
				
				
				if(null != offerIdInt) {
					
					offerId = offerIdInt.getValue().toString();
					
					if (outputParamMap.get("offerinformationlist."+ counter +".productid") != null) {
						productId = outputParamMap.get("offerinformationlist."+ counter +".productid").getValue() != null ?
								outputParamMap.get("offerinformationlist."+ counter +".productid").getValue().toString() : null;
					}
					
					if (outputParamMap.get("offerinformationlist."+ counter +".expirydatetime") != null) {
						expiryDate = outputParamMap.get("offerinformationlist."+ counter +".expirydatetime").getValue();
					}
					
					if (outputParamMap.get("offerinformationlist."+ counter +".startdatetime") != null) {
						startDate = outputParamMap.get("offerinformationlist."+ counter +".startdatetime").getValue();
					}
					
					/*if (outputParamMap.get("offerinformationlist."+ counter +".dedicatedaccountinformation"+".0"+"dedicatedaccountid") != null) {
						dedicatedActId = outputParamMap.get("offerinformationlist."+ counter +".dedicatedaccountinformation"+".0"+"dedicatedaccountid")
								.getValue().toString();
					}
					
					if (outputParamMap.get("offerinformationlist."+ counter +".dedicatedaccountinformation"+".0"+"dedicatedaccountactivevalue1") != null) {
						dedicatedActBalance = outputParamMap.get("offerinformationlist."+ counter +".dedicatedaccountinformation"+".0"+"dedicatedaccountactivevalue1")
								.getValue().toString();
					}
					
					if (outputParamMap.get("offerinformationlist."+ counter +".dedicatedaccountinformation"+".0"+"dedicatedaccountunittype") != null) {
						dedicatedActType = outputParamMap.get("offerinformationlist."+ counter +".dedicatedaccountinformation"+".0"+"dedicatedaccountunittype")
								.getValue().toString();
					}
					
					if (outputParamMap.get("offerinformationlist."+ counter +".dedicatedaccountinformation"+".0"+"startdate") != null) {
						dedicatedActStartDate = outputParamMap.get("offerinformationlist."+ counter +".dedicatedaccountinformation"+".0"+"startdate")
								.getValue().toString();
					}
					
					if (outputParamMap.get("offerinformationlist."+ counter +".dedicatedaccountinformation"+".0"+"expirydate") != null) {
						dedicatedActEndDate = outputParamMap.get("offerinformationlist."+ counter +".dedicatedaccountinformation"+".0"+"expirydate")
								.getValue().toString();
					}
					*/
					
					offerInstance = new FDPOfferDTO();
					
					offerInstance.setOfferId(offerId);
					
					if (productId != null) {
						offerInstance.setProductId(productId);
					}
					
					if (expiryDate != null) {
						offerInstance.setEndDate(DateUtil.gregorianCalendarToSimpleDateConvertoer(((GregorianCalendar)expiryDate).getTime().getTime(), FDPConstant.VIEW_HISTPRY_DATE_PATTERN_WITH_FULL_TIME));
					}
					
					if (startDate != null) {
						offerInstance.setStartDate(DateUtil.gregorianCalendarToSimpleDateConvertoer(((GregorianCalendar)startDate).getTime().getTime(), FDPConstant.VIEW_HISTPRY_DATE_PATTERN_WITH_FULL_TIME));
					}
					
					/*if (dedicatedActId != null) {
						offerInstance.setDedicatedAccId(dedicatedActId);
					}
					
					if (dedicatedActType != null) {
						offerInstance.setDedicatedAccType(dedicatedActType);
					}
					
					if (dedicatedActBalance != null) {
						offerInstance.setDedicatedAccBalance(dedicatedActBalance);
					}
					
					if (dedicatedActStartDate != null) {
						offerInstance.setDedicatedAccStartDate(DateUtil.gregorianCalendarToSimpleDateConvertoer(dedicatedActStartDate, FDPConstant.VIEW_HISTPRY_DATE_PATTERN_WITH_FULL_TIME));
					} else if (startDate != null) {
						offerInstance.setDedicatedAccStartDate(startDate);
					}
					
					if (dedicatedActEndDate != null) {
						offerInstance.setDedicatedAccEndDate(DateUtil.gregorianCalendarToSimpleDateConvertoer(dedicatedActEndDate, FDPConstant.VIEW_HISTPRY_DATE_PATTERN_WITH_FULL_TIME));
					} else if (startDate != null) {
						offerInstance.setDedicatedAccEndDate(startDate);
					}*/
					
					Long offerIdInLong = Long.valueOf(offerId);
					if (offerMap.containsKey(offerIdInLong)) {
						List<FDPOfferDTO> offerDtoList = offerMap.get(offerIdInLong);
						offerDtoList.add(offerInstance);
						offerMap.put(Long.valueOf(offerId), offerDtoList);
					} else {
						offerList = new ArrayList<FDPOfferDTO>();
						offerList.add(offerInstance);
						offerMap.put(offerIdInLong, offerList);
						
					}
				}
				counter++;
			}		
		}
		return offerMap;
	}
	
	
	/**
	 * This method executes the given command and returns the response
	 * code.
	 * 
	 * @param fdpRequest
	 * @param command
	 * @return
	 * @throws ExecutionFailedException
	 */
	private FDPCommand executeCommand(final FDPRequest fdpRequest, final Command command)
			throws ExecutionFailedException {
		FDPCommand fdpCommand = null;
		String commandName = command.getCommandDisplayName();
		FDPCircle fdpCircle = fdpRequest.getCircle();
		final FDPCacheable fdpCommandCached = ApplicationConfigUtil.getMetaDataCache().getValue(
				new FDPMetaBag(fdpCircle, ModuleType.COMMAND, commandName));
		if (fdpCommandCached != null && fdpCommandCached instanceof FDPCommand) {
			fdpCommand = (FDPCommand) fdpCommandCached;
			fdpCommand.execute(fdpRequest);
			fdpRequest.addExecutedCommand(fdpCommand);
		}
		return fdpCommand;
	}
	
}
 