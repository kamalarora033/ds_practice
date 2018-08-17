package com.ericsson.fdp.business.fulfillment.service.impl;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.ejb.Stateless;

import com.ericsson.fdp.business.enums.AuxRequestParam;
import com.ericsson.fdp.business.enums.Command;
import com.ericsson.fdp.business.product.Product;
import com.ericsson.fdp.business.request.impl.FDPMetadataResponseImpl;
import com.ericsson.fdp.business.response.fulfillment.xml.DaDetails;
import com.ericsson.fdp.business.response.fulfillment.xml.OfferDetails;
import com.ericsson.fdp.business.response.fulfillment.xml.ProductDetails;
import com.ericsson.fdp.business.response.fulfillment.xml.SubscribedProducts;
import com.ericsson.fdp.business.util.CommandUtil;
import com.ericsson.fdp.business.util.MVELUtil;
import com.ericsson.fdp.business.util.RequestUtil;
import com.ericsson.fdp.common.constants.FDPConstant;
import com.ericsson.fdp.common.enums.FDPCSAttributeParam;
import com.ericsson.fdp.common.enums.FDPCSAttributeValue;
import com.ericsson.fdp.common.enums.Status;
import com.ericsson.fdp.common.util.DateUtil;
import com.ericsson.fdp.core.command.FDPCommand;
import com.ericsson.fdp.core.exception.ExecutionFailedException;
import com.ericsson.fdp.core.request.FDPRequest;
import com.ericsson.fdp.core.request.FDPResponse;

@Stateless
public class ViewHistoryServiceImpl extends AbstractFDPFulFillmentServiceImpl{
	
	private class SubscribedProductDetails{
		String productId;
		List<String> offerIds;
		List<String> daIds;
		
		SubscribedProductDetails(){
			offerIds = new ArrayList<String>();
			daIds = new ArrayList<String>();
		}
	}
	

	@Override
	protected FDPResponse executeService(FDPRequest fdpRequest,
			Object... additionalInformations) throws ExecutionFailedException {
        FDPMetadataResponseImpl fdpResponse = new FDPMetadataResponseImpl(Status.FAILURE, true, null);
        List<SubscribedProductDetails> subscribedProducts = getSubscribedProducts(fdpRequest);
    	SubscribedProducts sp = new SubscribedProducts();
    	sp.setProductDetails(new ArrayList<ProductDetails>());
        if(subscribedProducts.size() > 0){
        	Product product = null;
        	CommandUtil.executeCommand(fdpRequest, Command.GET_BALANCE_AND_DATE, true);
        	Map<String, Object> offersDaMap = new HashMap<String, Object>();
        	MVELUtil.evaluateOffersDetails(fdpRequest.getExecutedCommand(Command.GET_BALANCE_AND_DATE.getCommandDisplayName()), offersDaMap);
        	evaluateDetailsForUser(fdpRequest.getExecutedCommand(Command.GET_BALANCE_AND_DATE.getCommandDisplayName()), offersDaMap);
        	for(SubscribedProductDetails spd : subscribedProducts){
				product = RequestUtil.getProductById(fdpRequest, spd.productId);
    			if(null != product){
    				ProductDetails productDetails = new ProductDetails();
    				productDetails.setProductName(product.getProductName());
    				productDetails.setOfferDetails(new ArrayList<OfferDetails>());
    				productDetails.setDaDetails(new ArrayList<DaDetails>());
	    			for(int i = 0; i < spd.offerIds.size(); i++){
	    				OfferDetails od = new OfferDetails();
	    				GregorianCalendar expiry = (GregorianCalendar) offersDaMap.get(FDPCSAttributeValue.OFFER.name()+FDPConstant.UNDERSCORE
	    						+ spd.offerIds.get(i)+FDPConstant.UNDERSCORE+FDPCSAttributeParam.EXPIRY.name());
	    				if(null != expiry){
	    					od.setOfferId(spd.offerIds.get(i));
	    					od.setExpiryDate(DateUtil.convertCalendarDateToString(expiry, 
		    						FDPConstant.VIEW_HISTPRY_DATE_PATTERN_WITH_FULL_TIME));
	    					productDetails.getOfferDetails().add(od);
	    				}
	    			}
	    			for(int i = 0; i < spd.daIds.size(); i++){
	    				GregorianCalendar daStartDate = (GregorianCalendar) offersDaMap.get(FDPCSAttributeValue.DA.name()+FDPConstant.UNDERSCORE
								+ spd.daIds.get(i)+FDPConstant.UNDERSCORE+FDPCSAttributeParam.STARTDATE.name());
	    				GregorianCalendar daExpiryDate = (GregorianCalendar) offersDaMap.get(FDPCSAttributeValue.DA.name()+FDPConstant.UNDERSCORE
								+ spd.daIds.get(i)+FDPConstant.UNDERSCORE+FDPCSAttributeParam.EXPIRY.name());
	    				Object daUnitType = offersDaMap.get(FDPCSAttributeValue.DA.name()+FDPConstant.UNDERSCORE
								+ spd.daIds.get(i)+FDPConstant.UNDERSCORE+"UNIT");
	    				Object daValue = offersDaMap.get(FDPCSAttributeValue.DA.name()+FDPConstant.UNDERSCORE
	    						+ spd.daIds.get(i)+FDPConstant.UNDERSCORE+FDPCSAttributeParam.VALUE.name());
	    				if(null != daStartDate && null != daExpiryDate){
	    					DaDetails daDetails = new DaDetails();
	    					daDetails.setDaId(spd.daIds.get(i));
	    					daDetails.setDaValue(daValue.toString());
	    					daDetails.setUnitType(daUnitType.toString());
	    					daDetails.setStartDate(DateUtil.convertCalendarDateToString(daStartDate, 
		    						FDPConstant.VIEW_HISTPRY_DATE_PATTERN_WITH_FULL_TIME));
	    					daDetails.setEndDate(DateUtil.convertCalendarDateToString(daExpiryDate, 
				    						FDPConstant.VIEW_HISTPRY_DATE_PATTERN_WITH_FULL_TIME));
	    					productDetails.getDaDetails().add(daDetails);
	    				}
	    			}
	    			sp.getProductDetails().add(productDetails);
    			}
    		}
        }
        fdpResponse.putAuxiliaryRequestParameter(AuxRequestParam.VIEW_HISTORY_OFFER_DETAILS, sp);
        fdpResponse.setExecutionStatus(Status.SUCCESS);
		return fdpResponse;
	}
	
	
	/**
	 * Returns the list of subscribed products of user by fetching the details from RS
	 * @param fdpRequest
	 * @return
	 * @throws ExecutionFailedException
	 */
	private List<SubscribedProductDetails> getSubscribedProducts(FDPRequest fdpRequest)  throws ExecutionFailedException {
		List<SubscribedProductDetails> subscribedProducts = new ArrayList<SubscribedProductDetails>();
		CommandUtil.executeCommand(fdpRequest, Command.GET_SERVICES_DETAILS_REQUEST, true);
		FDPCommand getServiceDetailsCommand = fdpRequest.getExecutedCommand(Command.GET_SERVICES_DETAILS_REQUEST.getCommandDisplayName());
		if(null != getServiceDetailsCommand){
			String productIdParam = "productid";
			String offerIdsParam = "offerids";
			String daIdsParam = "das";
			String serviceDtlsParam = "servicesdtls";
			try
			{
			for(int i = 0; null != getServiceDetailsCommand.getOutputParam(
					serviceDtlsParam + FDPConstant.DOT + i + FDPConstant.DOT + productIdParam); i++){					
				
				String expiryDate = getServiceDetailsCommand.getOutputParam(
						serviceDtlsParam+FDPConstant.DOT+i+FDPConstant.DOT+"renewalDate")!=null?getServiceDetailsCommand.getOutputParam(
								serviceDtlsParam+FDPConstant.DOT+i+FDPConstant.DOT+"renewalDate").getValue().toString() : null;
				
				if( null== expiryDate || DateUtil.compareWithCurrentDate(DateUtil.getDateTimeForExpiryDate(expiryDate))>=0 )
				{
					String productId = getServiceDetailsCommand.getOutputParam(
							serviceDtlsParam + FDPConstant.DOT + i + FDPConstant.DOT + productIdParam).getValue().toString();
					String offerIds = getServiceDetailsCommand.getOutputParam(
							serviceDtlsParam + FDPConstant.DOT + i + FDPConstant.DOT + offerIdsParam).getValue().toString();
					String daIds = getServiceDetailsCommand.getOutputParam(
							serviceDtlsParam + FDPConstant.DOT + i + FDPConstant.DOT + daIdsParam).getValue().toString();
				
					SubscribedProductDetails spd = new SubscribedProductDetails();
					spd.productId = productId;
					
					if(!offerIds.isEmpty()){
						String[] offers = offerIds.split("\\|");
						for(int j = 0; j < offers.length; j++){
							spd.offerIds.add(offers[j]);
						}
					}
					
					if(!daIds.isEmpty()){
						String[] das = daIds.split("\\|");
						for(int j = 0; j < das.length; j++){
							spd.daIds.add(das[j]);
						}
					}
					subscribedProducts.add(spd);
				}
				}
			}
			catch(ParseException ex)
			{
				throw new ExecutionFailedException(ex.getMessage());
			}
		}
		return subscribedProducts;
	}
	
	/**
	 * This method calculate the DA related values from Comamnd.
	 * @param executedCommand
	 */
	public static void evaluateDetailsForUser(final FDPCommand executedCommand, final Map<String,Object> responseMap) {
        String pathkey = null;
        int i = 0;
        final String paramterName = "dedicatedAccountInformation";
        final String dedicatedAccountId  = "dedicatedAccountId";
        final String dedicatedAccountValue1 = "dedicatedAccountValue1";
        final String dedicatedAccountUnitType = "dedicatedAccountUnitType";
        final String expiryDate = "expiryDate";
        final String startDate = "startDate";
        String defaultExpiryDate = MVELUtil.getDefaultExpiryDate();
        
        while (executedCommand.getOutputParam(pathkey = (paramterName + FDPConstant.PARAMETER_SEPARATOR
                + i + FDPConstant.PARAMETER_SEPARATOR +dedicatedAccountId)) != null) {
            final String dedicatedAccountValue1_Value = paramterName + FDPConstant.PARAMETER_SEPARATOR
                    + i + FDPConstant.PARAMETER_SEPARATOR + dedicatedAccountValue1;
            final String dedicatedAccountUnitType_Value = paramterName + FDPConstant.PARAMETER_SEPARATOR
                    + i + FDPConstant.PARAMETER_SEPARATOR + dedicatedAccountUnitType;
            final String startDate_Value = paramterName + FDPConstant.PARAMETER_SEPARATOR
                    + i + FDPConstant.PARAMETER_SEPARATOR + startDate;
            final String expiryDate_Value = paramterName + FDPConstant.PARAMETER_SEPARATOR
                    + i + FDPConstant.PARAMETER_SEPARATOR + expiryDate;
            
            final String userDaId = executedCommand.getOutputParam(pathkey).getValue().toString();
            final String userDaValue = executedCommand.getOutputParam(dedicatedAccountValue1_Value).getValue().toString();
            final String userDaUnitType = executedCommand.getOutputParam(dedicatedAccountUnitType_Value).getValue().toString();
            final Object userDaStartDate = executedCommand.getOutputParam(startDate_Value).getValue();
            final Object userDaExpiry = (null != executedCommand.getOutputParam(expiryDate_Value)) ? executedCommand.getOutputParam(expiryDate_Value).getValue():
            	defaultExpiryDate;            
            responseMap.put(FDPCSAttributeValue.DA.name()+FDPConstant.UNDERSCORE+userDaId+FDPConstant.UNDERSCORE+FDPCSAttributeParam.VALUE.name(), userDaValue);
            responseMap.put(FDPCSAttributeValue.DA.name()+FDPConstant.UNDERSCORE+userDaId+FDPConstant.UNDERSCORE+"UNIT", userDaUnitType);
            responseMap.put(FDPCSAttributeValue.DA.name()+FDPConstant.UNDERSCORE+userDaId+FDPConstant.UNDERSCORE+FDPCSAttributeParam.STARTDATE.name(), userDaStartDate);
            responseMap.put(FDPCSAttributeValue.DA.name()+FDPConstant.UNDERSCORE+userDaId+FDPConstant.UNDERSCORE+FDPCSAttributeParam.EXPIRY.name(), userDaExpiry);
            i++;
        }
    }
	
}