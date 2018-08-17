package com.ericsson.fdp.business.step.impl;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import com.ericsson.fdp.FDPCacheable;
import com.ericsson.fdp.business.charging.impl.Offer;
import com.ericsson.fdp.business.command.param.impl.CommandParamInput;
import com.ericsson.fdp.business.enums.AuxRequestParam;
import com.ericsson.fdp.business.enums.Command;
import com.ericsson.fdp.business.util.FulfillmentUtil;
import com.ericsson.fdp.business.util.MVELUtil;
import com.ericsson.fdp.business.vo.FDPOfferAttributeVO;
import com.ericsson.fdp.common.constants.FDPConstant;
import com.ericsson.fdp.common.enums.FDPCSAttributeParam;
import com.ericsson.fdp.common.enums.FDPCSAttributeValue;
import com.ericsson.fdp.common.enums.ModuleType;
import com.ericsson.fdp.common.enums.ParameterFeedType;
import com.ericsson.fdp.core.command.FDPCommand;
import com.ericsson.fdp.core.command.param.CommandParam;
import com.ericsson.fdp.core.exception.EvaluationFailedException;
import com.ericsson.fdp.core.exception.ExecutionFailedException;
import com.ericsson.fdp.core.request.FDPRequest;
import com.ericsson.fdp.core.request.impl.FDPRequestImpl;
import com.ericsson.fdp.core.serviceprovisioning.bean.FDPMetaBag;
import com.ericsson.fdp.core.utils.ApplicationConfigUtil;

public class UpdateUsageThresholdAndCountersCommandStep extends CommandStep{

	private static final long serialVersionUID = -6973632623929404634L;
	
	/** The custom param usagecounterupdateinformation. */
	private static final String USAGE_COUNTER_UPDATE_INFORMATION = "usageCounterUpdateInformation";
	/** The custom param usagethresholdupdateinformation. */
	private static final String USAGE_THRESHOLD_UPDATE_INFORMATION = "usageThresholdUpdateInformation";
	/** The usage counter id parameter. */
	private static final String USAGE_COUNTER_ID_PARAMETER = "usageCounterID";
	/** The usage counter value new parameter. */
	private static final String USAGE_COUNTER_VALUE_NEW_PARAMETER = "usageCounterValueNew";
	/** The usage threshold id parameter. */
	private static final String USAGE_THRESHOLD_ID_PARAMETER = "usageThresholdID";
	/** The usage threshold value new parameter. */
	private static final String USAGE_THRESHOLD_VALUE_NEW_PARAMETER = "usageThresholdValueNew";
		
	public UpdateUsageThresholdAndCountersCommandStep(final FDPCommand fdpCommand, final String commandDisplayNameToSet, final Long stepId,
			final String stepName) {
		super(fdpCommand, commandDisplayNameToSet, stepId, stepName);
	}

	/**
	 * This method is used to find and update the command with UC/UT values for expired offers
	 *
	 * @param fdpRequest
	 *            the request.
	 * @throws ExecutionFailedException
	 *             Exception if any.
	 */
	protected void updateCommand(final FDPRequest fdpRequest) throws ExecutionFailedException {
		super.updateCommand(fdpRequest);
		updateCommandForOfferExpiry(fdpRequest);
	}

	/**
	 * This method updates the command with the new UC/UT values of offers that are expired
	 * 
	 * @param fdpRequest
	 * @throws ExecutionFailedException
	 * @throws EvaluationFailedException 
	 */
	private void updateCommandForOfferExpiry(FDPRequest fdpRequest) throws ExecutionFailedException{
		Map<String, Map<String, Long>> newUCUTValues = getUCUTMapForExpiry(fdpRequest);
		updateCommandParam(FDPCSAttributeValue.UC.name(), newUCUTValues.get(FDPCSAttributeValue.UC.name()));
		updateCommandParam(FDPCSAttributeValue.UT.name(), newUCUTValues.get(FDPCSAttributeValue.UT.name()));
	}
	
	/**
	 * This method will return a map of all UC/UT ids and values that needs to be updated for expired offers
	 * @param fdpRequest
	 * @return
	 * @throws ExecutionFailedException 
	 */
	public Map<String, Map<String, Long>> getUCUTMapForExpiry(FDPRequest fdpRequest) throws ExecutionFailedException{
		Map<String, Map<String, Long>> newUCUTValues = new HashMap<String, Map<String, Long>>();
		newUCUTValues.put(FDPCSAttributeValue.UC.name(), new HashMap<String, Long>());
		newUCUTValues.put(FDPCSAttributeValue.UT.name(), new HashMap<String, Long>());
		Map<String, List<Offer>> expiredOffersMap = getExpiredOffersMapForUser(fdpRequest);
		Map<String, Object> UCUTDetailsOfUserMap = getUCUTDetailsForUser(fdpRequest);
		Long expiredBalance = 0L;
		for (Map.Entry<String, List<Offer>> offerMapEntry : expiredOffersMap.entrySet()) {
			Long globleUTValue = Long.valueOf(UCUTDetailsOfUserMap.get(FDPCSAttributeValue.UT.name()+FDPConstant.UNDERSCORE+offerMapEntry.getValue().get(0).getGlobalUCUTId()+FDPConstant.UNDERSCORE+FDPCSAttributeParam.VALUE.name()).toString());
			Long globleUCValue = Long.valueOf(UCUTDetailsOfUserMap.get(FDPCSAttributeValue.UC.name()+FDPConstant.UNDERSCORE+offerMapEntry.getValue().get(0).getGlobalUCUTId()+FDPConstant.UNDERSCORE+FDPCSAttributeParam.VALUE.name()).toString());
			expiredBalance += (globleUTValue - globleUCValue);
			for(Offer offer : offerMapEntry.getValue()){
				newUCUTValues.get(FDPCSAttributeValue.UC.name()).put(offer.getUsageCounterId(), 0L);
				newUCUTValues.get(FDPCSAttributeValue.UT.name()).put(offer.getUsageThresholdId(), 0L);
			}
			newUCUTValues.get(FDPCSAttributeValue.UC.name()).put(offerMapEntry.getKey(), 0L);
			newUCUTValues.get(FDPCSAttributeValue.UT.name()).put(offerMapEntry.getKey(), 0L);
		}
		((FDPRequestImpl)fdpRequest).putAuxiliaryRequestParameter(AuxRequestParam.EXPIRED_BITS, expiredBalance);
		((FDPRequestImpl)fdpRequest).putAuxiliaryRequestParameter(AuxRequestParam.EXPIRED_UC_UT_MAP, newUCUTValues);
		return newUCUTValues;
	}
	
	/**
	 * This method returns a map of groups(with offers) in which all the offers are expired
	 * 
	 * @param fdpRequest
	 * @return
	 * @throws ExecutionFailedException
	 */
	public static Map<String, List<Offer>> getExpiredOffersMapForUser(FDPRequest fdpRequest) throws ExecutionFailedException{
		final Map<String, List<Offer>> subscribedOffersMap = getSubscribedOffersMapForUser(fdpRequest);
		Iterator<Map.Entry<String, List<Offer>>> offerIterator = subscribedOffersMap.entrySet().iterator();
		while (offerIterator.hasNext()) {
			Map.Entry<String, List<Offer>> offerMapEntry = offerIterator.next();
			for(Offer offer : offerMapEntry.getValue()){
				if(!offer.isExpired()){
					offerIterator.remove();
					break;
				}
			}
		}		
		return subscribedOffersMap;
	}
	
	/**
	 * This method returns a map of all subscribed offers of a subscriber
	 * 
	 * @param fdpRequest
	 * @return
	 * @throws ExecutionFailedException
	 */
	public static Map<String, List<Offer>> getSubscribedOffersMapForUser(FDPRequest fdpRequest) throws ExecutionFailedException{
		Map<String, List<Offer>> subscribedOffersMap = new HashMap<String, List<Offer>>();
		MVELUtil.executeCachedCommand(fdpRequest, Command.GET_OFFERS.getCommandDisplayName());
		Map<String, Boolean> subscribedOffers =  MVELUtil.getSubscribedOffers(fdpRequest.getExecutedCommand(Command.GET_OFFERS.getCommandName()));
		if(!subscribedOffers.isEmpty()){
			for (Map.Entry<String, Boolean> offerEntry : subscribedOffers.entrySet()) {
				FDPOfferAttributeVO cachedOffer = getOfferDetailsFromCache(fdpRequest, offerEntry.getKey());
				if(cachedOffer != null){
					if(null == subscribedOffersMap.get(cachedOffer.getNotification_uc_ut().toString())){
						subscribedOffersMap.put(cachedOffer.getNotification_uc_ut().toString(), new ArrayList<Offer>());
					}
					Offer offer = new Offer();
					offer.setOfferId(cachedOffer.getFdp_offer_attribute_id().toString());
					offer.setUsageCounterId(cachedOffer.getUc_id().toString());
					offer.setUsageThresholdId(cachedOffer.getUt_id().toString());
					offer.setGlobalUCUTId(cachedOffer.getNotification_uc_ut().toString());
					offer.setExpired(offerEntry.getValue());
					subscribedOffersMap.get(cachedOffer.getNotification_uc_ut().toString()).add(offer);
				}
			}
		}
		return subscribedOffersMap;
	}
	
	
	/**
	 * This method returns the offer details from cache
	 * 
	 * @param fdpRequest
	 * @param offerId
	 * @return
	 * @throws ExecutionFailedException
	 */
	private static FDPOfferAttributeVO getOfferDetailsFromCache(FDPRequest fdpRequest, String offerId) throws ExecutionFailedException{
		FDPOfferAttributeVO cachedOffer = null;
		if(null != offerId){
			final FDPCacheable fdpOfferCached = ApplicationConfigUtil.getMetaDataCache().getValue(new FDPMetaBag(fdpRequest.getCircle(), ModuleType.OFFER_ATTRIBUTE, offerId));
			if (fdpOfferCached instanceof FDPOfferAttributeVO) {
				cachedOffer = (FDPOfferAttributeVO) fdpOfferCached;
			}
		}
		return cachedOffer;
	}
	
	
	/**
	 * This method will return list of id defined for UC/UT param in command 
	 * @return
	 */
	private List<String> getParamsDefinedInCommand(String infoParam, String idParam){
		List<String> values = new ArrayList<String>();
		if(null != fdpCommand.getInputParam(infoParam) && null != fdpCommand.getInputParam(infoParam).getChilderen()){
	        List<CommandParam> childNodes = fdpCommand.getInputParam(infoParam).getChilderen();
			for(int i = 0; i < childNodes.size(); i++){
		        List<CommandParam> grandChildNodes = childNodes.get(i).getChilderen();
		        if(null != grandChildNodes){
					for(int j = 0; j < grandChildNodes.size(); j++){
						if(grandChildNodes.get(j).getName().equals(idParam)){
							values.add(((CommandParamInput)grandChildNodes.get(j)).getDefinedValue().toString());
						}
		        	}
		        }
	        }
		}
		return values;
	}
       

	/**
	 * This method updates the UpdateUsageThresholdAndCounters command params with UT/UC values
	 * as specified in input
	 * 
	 * @param attr
	 * @param map
	 * @throws ExecutionFailedException
	 */
	private void updateCommandParam(String fdpCSAttributeName, Map<String, Long> map) throws ExecutionFailedException {
		String param = fdpCSAttributeName.equals(FDPCSAttributeValue.UC.name()) ? USAGE_COUNTER_UPDATE_INFORMATION : USAGE_THRESHOLD_UPDATE_INFORMATION;
		final CommandParam commandParam = fdpCommand.getInputParam(param);
		if(commandParam != null && commandParam instanceof CommandParamInput && commandParam.getChilderen() != null && null != commandParam.getChilderen().get(0)) {
			CommandParamInput parentCpi = (CommandParamInput)commandParam;
			final List<CommandParam> updatedParamInputs = updatedChildNodes(parentCpi.getChilderen().get(0), map, fdpCSAttributeName);
			if(null != updatedParamInputs && updatedParamInputs.size()>0) {
				parentCpi.getChilderen().addAll(updatedParamInputs);
			}
		}else{
			throw new ExecutionFailedException("Command : " + fdpCommand.getCommandDisplayName() + " not configured for " + param);
		}
	}

	/**
	 * This method updates the commands with UC/UT information
	 * 
	 * @param childNode
	 * @param valuesMap
	 * @param attr
	 * @return
	 * @throws ExecutionFailedException
	 */
	private List<CommandParam> updatedChildNodes(CommandParam childNode, Map<String, Long> valuesMap, String fdpCSAttributeName) throws ExecutionFailedException {
		List<CommandParam> paramInputs = new ArrayList<CommandParam>();
		String infoParamName = fdpCSAttributeName.equals(FDPCSAttributeValue.UC.name()) ? USAGE_COUNTER_UPDATE_INFORMATION : USAGE_THRESHOLD_UPDATE_INFORMATION;
		String idParamName = fdpCSAttributeName.equals(FDPCSAttributeValue.UC.name()) ? USAGE_COUNTER_ID_PARAMETER : USAGE_THRESHOLD_ID_PARAMETER;
		String valueParamName = fdpCSAttributeName.equals(FDPCSAttributeValue.UC.name()) ? USAGE_COUNTER_VALUE_NEW_PARAMETER : USAGE_THRESHOLD_VALUE_NEW_PARAMETER;
		List<String> paramsToSkipList = getParamsDefinedInCommand(infoParamName, idParamName);
		for (Map.Entry<String, Long> entry : valuesMap.entrySet()) {
			if(!paramsToSkipList.contains(entry.getKey())){
				CommandParam commandParam = createCommandParamForUCUT(
						(CommandParamInput) childNode, entry.getKey(), entry
								.getValue().toString(), idParamName, valueParamName);
				paramInputs.add(commandParam);
			}
		}
		return paramInputs;
	}

	/**
	 * This method returns a map of UC/UT associated with a subscriber with their values
	 * 
	 * @param fdpRequest
	 * @return
	 * @throws ExecutionFailedException
	 */
	public static Map<String, Object> getUCUTDetailsForUser(FDPRequest fdpRequest) throws ExecutionFailedException{
		MVELUtil.executeCachedCommand(fdpRequest, Command.GET_USAGE_THRESHOLDS_AND_COUNTERS.getCommandDisplayName());
		return MVELUtil.evaluateUCUTDetailsForUser(fdpRequest, fdpRequest.getExecutedCommand(Command.GET_USAGE_THRESHOLDS_AND_COUNTERS.getCommandName()));
	}
	
	/**
	 * This method creates a clone of an object
	 * @param commandParamInput
	 * @return
	 * @throws ExecutionFailedException
	 */
	private Object cloneObject(final Object objectToClone) throws ExecutionFailedException{
		Object clonedObject = null;	
		try{
			ByteArrayOutputStream baos = new ByteArrayOutputStream();
			new ObjectOutputStream(baos).writeObject(objectToClone);
			ByteArrayInputStream bais = new ByteArrayInputStream(baos.toByteArray());
			clonedObject = new ObjectInputStream(bais).readObject();
		}catch(Exception e){
			throw new ExecutionFailedException("");
		}
		return clonedObject;
	}
	
	/**
	 * Creates copy of Param Input and prepare parameter for DA based charging.
	 * 
	 * @param commandParamInput
	 * @param dedicatedAccountId
	 * @param value
	 * @return
	 * @throws ExecutionFailedException
	 */
	private CommandParam createCommandParamForUCUT(
			final CommandParamInput commandParamInput, final String id,
			final String value, final String idParamName,
			final String valueParamName) throws ExecutionFailedException {
		final CommandParamInput paramInput = (CommandParamInput) this.cloneObject(commandParamInput);
		for(CommandParam childParam : paramInput.getChilderen()){
			if( idParamName.equals(childParam.getName())){
				FulfillmentUtil.updateValueInInputCmdObject(CommandParamInput.class, "commandParameterSource",
						childParam, ParameterFeedType.INPUT);
				FulfillmentUtil.updateValueInInputCmdObject(CommandParamInput.class, FDPConstant.DEFINED_VALUE_TEXT,
						childParam, id);
			}
			if(valueParamName.equals(childParam.getName())){
				FulfillmentUtil.updateValueInInputCmdObject(CommandParamInput.class, "commandParameterSource",
						childParam, ParameterFeedType.INPUT);
				FulfillmentUtil.updateValueInInputCmdObject(CommandParamInput.class, FDPConstant.DEFINED_VALUE_TEXT,
						childParam, value);
			}
		}
		return paramInput;
	}

}
