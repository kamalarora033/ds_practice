package com.ericsson.fdp.business.fuzzy;

import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.annotation.Resource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ericsson.fdp.FDPCacheable;
import com.ericsson.fdp.AirConfig.pojo.Offer;
import com.ericsson.fdp.AirConfig.pojo.Refill;
import com.ericsson.fdp.business.cache.datageneration.service.impl.AirConfigCacheImpl;
import com.ericsson.fdp.business.constants.MMLCommandConstants;
import com.ericsson.fdp.business.enums.Command;
import com.ericsson.fdp.business.enums.ConfigurationKey;
import com.ericsson.fdp.business.enums.RequestMetaValuesKey;
import com.ericsson.fdp.business.product.Product;
import com.ericsson.fdp.business.serviceprovisioning.ServiceProvisioning;
import com.ericsson.fdp.business.util.CommandUtil;
import com.ericsson.fdp.business.util.MVELUtil;
import com.ericsson.fdp.common.constants.FDPConstant;
import com.ericsson.fdp.common.enums.FDPCSAttributeParam;
import com.ericsson.fdp.common.enums.FDPCSAttributeValue;
import com.ericsson.fdp.common.enums.ModuleType;
import com.ericsson.fdp.common.enums.Primitives;
import com.ericsson.fdp.common.vo.FDPCircle;
import com.ericsson.fdp.core.cache.AppCacheSubStore;
import com.ericsson.fdp.core.command.FDPCommand;
import com.ericsson.fdp.core.command.param.CommandParam;
import com.ericsson.fdp.core.dsm.framework.service.FDPCache;
import com.ericsson.fdp.core.exception.EvaluationFailedException;
import com.ericsson.fdp.core.exception.ExecutionFailedException;
import com.ericsson.fdp.core.request.FDPRequest;
import com.ericsson.fdp.core.serviceprovisioning.bean.FDPAppBag;
import com.ericsson.fdp.core.serviceprovisioning.bean.FDPMetaBag;
import com.ericsson.fdp.core.utils.ApplicationConfigUtil;
import com.ericsson.fdp.dao.dto.FDPResultCodesDTO;

public class PerformRefillEr100 implements FuzzyCheck {
	
	@Resource(lookup = "java:app/fdpBusiness-1.0/MetaDataCache")
	private FDPCache<FDPMetaBag, FDPCacheable> fdpCache;

	/** The Constant LOGGER. */
	private static final Logger LOGGER = LoggerFactory.getLogger(PerformRefillEr100.class);
	
	@Override
	public boolean execute(FDPRequest fdpRequest, FDPCommand fdpCommand, FDPResultCodesDTO fdpResultCodeDto) throws EvaluationFailedException {
		Boolean needRollback = true;
		try {
			String refillProfileId = getRefillIdAttached(fdpRequest);
			Offer offer = getOfferIdAttatched(fdpRequest.getCircle(),refillProfileId);// get the offer id from cache
			//Refill refill=getRefill(fdpRequest.getCircle(), refillProfileId);
			
	        final Map<String,Object> responseMapOffer = new HashMap<String, Object>();
	        Map<String,Object> responseMapDA = new HashMap<String, Object>();
			CommandUtil.executeCommand(fdpRequest, Command.GET_BALANCE_AND_DATE, true);
			
			MVELUtil.evaluateOffersDetails(fdpRequest.getExecutedCommand(Command.GET_BALANCE_AND_DATE.getCommandDisplayName()), responseMapOffer);
			
			String offerName = (String)responseMapOffer.get(FDPCSAttributeValue.OFFER.name()+FDPConstant.UNDERSCORE+offer.get_offerID()+FDPConstant.UNDERSCORE+FDPCSAttributeParam.NAME.name());
			Object offerStartDate = responseMapOffer.get(FDPCSAttributeValue.OFFER.name()+FDPConstant.UNDERSCORE+offer.get_offerID()+FDPConstant.UNDERSCORE+FDPCSAttributeParam.STARTDATE.name());
			
			if(null != offerName && !offerName.isEmpty()){
				responseMapDA=MVELUtil.evaluateDetailsForUser(fdpRequest.getExecutedCommand(Command.GET_BALANCE_AND_DATE.getCommandDisplayName()));
				String daId=(String)responseMapDA.get(FDPCSAttributeValue.DA.name()+FDPConstant.UNDERSCORE+offer.get_daID()+FDPConstant.UNDERSCORE+FDPCSAttributeParam.VALUE.name());
				if(null != daId && !daId.isEmpty()){
					if(!isDateNearBy((GregorianCalendar)offerStartDate, getTimeOffset(fdpRequest))){
						needRollback= false;
					}
				}
			}
		
			
			
		}catch(ExecutionFailedException ex){
			ex.printStackTrace();
		}	
		return needRollback;
	}
	private Long getTimeOffset(FDPRequest fdpRequest) {
		final Map<String, String> conf = fdpRequest.getCircle().getConfigurationKeyValueMap();
		return Long.parseLong(conf.get(ConfigurationKey.TIME_OFFSET_FOR_ERROR100.getAttributeName()));
	}
	private String getRefillIdAttached(FDPRequest fdpRequest) {
		String name=null;
		final FDPCommand fdpCommand = fdpRequest.getExecutedCommand(Command.REFILL.getCommandDisplayName());
		if(null != fdpCommand){
			final CommandParam commandParam = fdpCommand.getInputParam("refillProfileID");
			name = (String)commandParam.getValue();
			
		}
		return name;
	}

	private String getRefillID(FDPCommand refillcommand,FDPCircle circle) {
		FDPCache<FDPMetaBag, FDPCacheable> configCache=null;
		String refillName=null;
		try {
			configCache = ApplicationConfigUtil.getMetaDataCache();
			List<CommandParam> commandparamlst=refillcommand.getInputParam();
			for(CommandParam commandparam:commandparamlst)
			{
				if(commandparam.getName().equals("refillProfileID"))
				{
					refillName=(String)commandparam.getValue();
					break;
				}
				
			}
		//if(configCache.getValue(key))
		} catch (ExecutionFailedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return refillName;
		
	}
	
	private Refill getRefill(FDPCircle circle, String refillName) {
		FDPMetaBag metaBag=null; 
		FDPCache<FDPMetaBag, FDPCacheable> configCache=null;
		Refill refill=null;
		try {
			configCache = ApplicationConfigUtil.getMetaDataCache();
			metaBag=new FDPMetaBag(circle, ModuleType.AIR_CONFIG,"REFILL"+FDPConstant.COLON+refillName);
			refill=(Refill)configCache.getValue(metaBag);
		} catch (ExecutionFailedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	
		return refill;
	}

	private FDPCommand getRefillCommand(
			List<FDPCommand> fdpPovisionActionCommands) {
		FDPCommand refillCommand=null;
		for(FDPCommand fdpcommand:fdpPovisionActionCommands)
		{
			if(fdpcommand.getCommandDisplayName().equals(Command.REFILL.getCommandDisplayName()))
			refillCommand=fdpcommand;
		}
		return refillCommand;
	}


	/**
	 * This method will determine whether the input time is nearby the offset time given
	 * @param date
	 * @param count
	 * @param timeType
	 * @return
	 */
	private boolean isDateNearBy(GregorianCalendar offerExpiryTime, Long timeoffset) {//1458627466924 -253402257600000
		return (System.currentTimeMillis()-offerExpiryTime.getTimeInMillis()) > timeoffset ? true:false;
	}

	/**Get the offer ID on the basis of refill ID */
	private Offer getOfferIdAttatched(FDPCircle fdpCircle, String refillID) {
		FDPMetaBag fdpmetabag = new FDPMetaBag(fdpCircle, ModuleType.AIR_CONFIG, "REFILL:" + refillID);
		Offer tmpoffer=null;
		
		FDPCache<FDPMetaBag, FDPCacheable> configCache=null;
		try {
			configCache = ApplicationConfigUtil.getMetaDataCache();
		} catch (ExecutionFailedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		Refill refill=(configCache.getValue(fdpmetabag)!=null)?((Refill)configCache.getValue(fdpmetabag)):null;
		if(refill!=null)
		{
			tmpoffer=getOfferFromCache(fdpCircle,refill.getOfferid());
			return tmpoffer;
		}
		return null;
	}

	private Offer getOfferFromCache(FDPCircle fdpcircle, String offerid) {
		FDPCache<FDPMetaBag, FDPCacheable> configCache=null;
		FDPMetaBag fdpmetabag = new FDPMetaBag(fdpcircle,ModuleType.AIR_CONFIG, "OFFER:" + offerid);
		try {
			configCache = ApplicationConfigUtil.getMetaDataCache();
		} catch (ExecutionFailedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return (Offer)configCache.getValue(fdpmetabag);
	}

}
