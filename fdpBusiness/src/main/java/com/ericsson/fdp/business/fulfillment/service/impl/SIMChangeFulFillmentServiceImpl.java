package com.ericsson.fdp.business.fulfillment.service.impl;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.ejb.Stateless;
import com.ericsson.fdp.FDPCacheable;
import com.ericsson.fdp.business.enums.AuxRequestParam;
import com.ericsson.fdp.business.enums.Command;
import com.ericsson.fdp.business.enums.RequestMetaValuesKey;
import com.ericsson.fdp.business.enums.ivr.FulfillmentParameters;
import com.ericsson.fdp.business.node.FDPServiceProvisioningNode;
import com.ericsson.fdp.business.product.Product;
import com.ericsson.fdp.business.request.impl.FDPMetadataResponseImpl;
import com.ericsson.fdp.business.request.impl.FulfillmentRequestImpl;
import com.ericsson.fdp.business.serviceprovisioning.rule.ServiceProvisioningRule;
import com.ericsson.fdp.business.util.CommandUtil;
import com.ericsson.fdp.business.util.DynamicMenuUtil;
import com.ericsson.fdp.business.util.LoggerUtil;
import com.ericsson.fdp.business.util.ProductAttributeCacheUtil;
import com.ericsson.fdp.business.util.RequestUtil;
import com.ericsson.fdp.business.vo.ProductAttributeMapCacheDTO;
import com.ericsson.fdp.common.constants.FDPConstant;
import com.ericsson.fdp.common.enums.ModuleType;
import com.ericsson.fdp.common.enums.Status;
import com.ericsson.fdp.common.util.StringUtil;
import com.ericsson.fdp.core.command.FDPCommand;
import com.ericsson.fdp.core.command.param.CommandParam;
import com.ericsson.fdp.core.exception.EvaluationFailedException;
import com.ericsson.fdp.core.exception.ExecutionFailedException;
import com.ericsson.fdp.core.logging.FDPLogger;
import com.ericsson.fdp.core.node.FDPNode;
import com.ericsson.fdp.core.request.FDPRequest;
import com.ericsson.fdp.core.request.FDPResponse;
import com.ericsson.fdp.core.request.impl.FDPRequestImpl;
import com.ericsson.fdp.core.serviceprovisioning.bean.FDPMetaBag;
import com.ericsson.fdp.core.utils.ApplicationConfigUtil;
import com.ericsson.fdp.dao.enums.FDPServiceProvSubType;
import com.ericsson.fdp.dao.enums.FDPServiceProvType;
import com.ericsson.fdp.dao.util.ServiceProvDataConverterUtil;

@Stateless
public class SIMChangeFulFillmentServiceImpl extends AbstractFDPFulFillmentServiceImpl {
	
	private final String OFFER_ID_TEXT = "offerid";
	private final String OFFER_INFO_TEXT = "offerinformation.";
	private final String OFFER_INFO_LIST_TEXT = "offerinformationlist.";
	private final String START_DATE_TEXT = "startdate";
	private final String EXPIRY_DATE_TEXT = "expirydate";
	private final String OFFER_TYPE_TEXT = "offertype";
	private final String TIME_TEXT = "time";
	
	private final String DEDICATED_ACCOUNT_INFO_TEXT = "dedicatedaccountinformation.";
	private final String DEDICATED_ACCOUNT_ID_TEXT = "dedicatedaccountid";
	private final String DEDICATED_ACCOUNT_VALUE1_TEXT = "dedicatedaccountvalue1";
	private final String DEDICATED_ACCOUNT_ACTIVE_VALUE1_TEXT = "dedicatedaccountactivevalue1";
	
	protected FDPResponse executeService(FDPRequest fdpRequest, Object... additionalInformations) throws ExecutionFailedException {
		FDPResponse fdpResponse = new FDPMetadataResponseImpl(Status.FAILURE, true, null);
		FDPLogger.debug(circleLogger,	getClass(),	"execute SIMChangeFulFillmentServiceImpl Fulfillment ",	LoggerUtil.getRequestAppender(fdpRequest) + "Start executing Service Action:" + fdpRequest.getRequestId());
		
        fdpResponse = executeCommandsAndProcessResponse(fdpRequest);
		FDPLogger.debug(circleLogger, getClass(), "execute SIMChangeFulFillmentServiceImpl", LoggerUtil.getRequestAppender(fdpRequest)
				+ "executeService Completed:" + fdpRequest.getRequestId());
		return fdpResponse;
	}
	
	/**
	 * This method perform the necessary steps of execution , reads the response and invokes the operations on new and old msisdn numbers.
	 * @param fdpRequest - This parameter takes the oldMSISDN command response map
	 * @param newMSISDNOutput - This parameter takes the newMSISDN command response map
	 *  
	 * @return List<Map<String,String>>
	 */
	private FDPResponse executeCommandsAndProcessResponse(FDPRequest fdpRequest) {
		FDPMetadataResponseImpl fdpResponse = new FDPMetadataResponseImpl(Status.FAILURE, true, null);
		try {
			final FulfillmentRequestImpl fulfillmentRequestImpl = (FulfillmentRequestImpl) fdpRequest;
			
			// Executing GetOffers for new Subscriber Number
			Map<String,CommandParam> outputGetOffers = executeCommand(fdpRequest, Command.GET_OFFERS);
			
			Long newMSISDN = fdpRequest.getSubscriberNumber();
			// Maintaining new subscriber number in Aux Param "SUBSCRIBER_NUMBER"
			fulfillmentRequestImpl.putAuxiliaryRequestParameter(AuxRequestParam.SUBSCRIBER_NUMBER, newMSISDN);

			// Setting old MSISDN as subscriber number to execute GetBalanceAndDate command
			Long subscriberNumberToSet = getSubsriberNumberFromAUX(fdpRequest, AuxRequestParam.OLD_MSISDN);
			fulfillmentRequestImpl.setSubscriberNumber(subscriberNumberToSet);
						
			// Executing GBAD for old Subscriber number
			Map<String,CommandParam> outputGBAD = executeCommand(fdpRequest, Command.GET_BALANCE_AND_DATE);
			
			FDPLogger.debug(circleLogger, getClass(), "execute SIMChangeFulFillmentServiceImpl Fulfillment ",	LoggerUtil.getRequestAppender(fdpRequest) + "Transaction_id is:" + fulfillmentRequestImpl.getCommandInputParams(FulfillmentParameters.TRANSACTION_ID));
			
			// Setting this AUX Param will allow same command step to be executed multiple times
			fulfillmentRequestImpl.putAuxiliaryRequestParameter(AuxRequestParam.FAF_EXECUTE_COMMAND_AGAIN, "true");
	
			// Perform updates on old and new MSISDN
			processUpdates(fdpRequest, fdpResponse, outputGBAD, outputGetOffers);
			
			// Updating the counters from old to new MSISDN - Can be implemented later
			//processThresholdCounters(fdpRequest);
			
			fdpResponse.setExecutionStatus(Status.SUCCESS);
			fdpResponse.setFulfillmentResponse("SIM Change request completed successfully");
		}
		catch(ExecutionFailedException efe){
			FDPLogger.error(circleLogger, getClass(), "SIMChangeFulFillmentServiceImpl", "executeCommandsAndProcessResponse Execution failed:" , efe);
			fdpResponse.setFulfillmentResponse("Some exception occured, unable to process the SIM Change request");
		}
		catch(Exception e){
			FDPLogger.error(circleLogger, getClass(), " SIMChangeFulFillmentServiceImpl", "executeCommandsAndProcessResponse Exception occured while processing SIM Change request:" + e);
			fdpResponse.setFulfillmentResponse("Some exception occured, unable to process the SIM Change request");
		}
		return fdpResponse;
	}

	/**
	 * This method filters the handset offers from the old MSISDN and then returns the map of filtered offer data which are not present on new MSISDN
	 * @param fdpRequest - FDPRequest object
	 * @param oldMSISDNOutPut - This parameter takes the oldMSISDN command response map
	 * @param newMSISDNOutput - This parameter takes the newMSISDN command response map
	 *  
	 * @return FDPResponse
	 * @throws ExecutionFailedException 
	 * @throws EvaluationFailedException 
	 */
	
	private void processUpdates(FDPRequest fdpRequest, FDPResponse fdpResponse, Map<String,CommandParam> oldMSISDNOutPut, Map<String,CommandParam> newMSISDNOutput) throws ExecutionFailedException {
		List<Map<String, String>> outputMap = new ArrayList<Map<String, String>>();
		List<Map<String, String>> deleteOffersListOldMSISDN = new ArrayList<Map<String, String>>();
		
		// Creating offerList for New MSISDN from GetOffers command response
		List <String> newMSISDNOfferIdList = getNewMSISDNOfferIdList(newMSISDNOutput);
		
		// Fetching hand set based Offer-DA mapping from cache on the basis of IMEI and Device type mapping
		Map<String,String[]> handsetOfferDAMap = generateHandsetOfferDAMap(fdpRequest);
		
		// Creating DA Map data from Old MSISDN output of GBAD command
		Map<String, Map<String, String>> oldMSISDNDAData = getOldMSISDNDAMap(oldMSISDNOutPut);
		
		int offerArrCounter = 0;
		String offerId = null;
		StringBuilder offerPath = new StringBuilder(OFFER_INFO_LIST_TEXT).append(offerArrCounter).append(FDPConstant.PARAMETER_SEPARATOR);
		CommandParam offerIdOutputParam = oldMSISDNOutPut.get(offerPath + OFFER_ID_TEXT);
		String[] daAndProductIdArr = null;
		Map<String,String> oldMSISDNOfferProductMap = null;
		while(offerIdOutputParam != null) {
			offerId = StringUtil.valueOf(offerIdOutputParam.getValue());
			
			daAndProductIdArr = handsetOfferDAMap.get(offerId);
			
			if(offerId != null && !newMSISDNOfferIdList.contains(offerId) && daAndProductIdArr != null && !StringUtil.isNullOrEmpty(daAndProductIdArr[0])) {
				oldMSISDNOfferProductMap = new HashMap<String,String>();
				oldMSISDNOfferProductMap.put(OFFER_ID_TEXT, offerId);
				oldMSISDNOfferProductMap.put(FDPConstant.PARAMETER_PRODUCT_ID, daAndProductIdArr[1]);
				Map<String,String> valueMap = new HashMap<String,String>();
				valueMap.putAll(oldMSISDNOfferProductMap);
				valueMap.put(OFFER_ID_TEXT + FDPConstant.UNDERSCORE + START_DATE_TEXT, getFormattedDate("2".equals(String.valueOf(oldMSISDNOutPut.get(offerPath + OFFER_TYPE_TEXT).getValue())) ? oldMSISDNOutPut.get(offerPath + START_DATE_TEXT + TIME_TEXT).getValue() : oldMSISDNOutPut.get(offerPath + START_DATE_TEXT).getValue()));
				valueMap.put(OFFER_ID_TEXT + FDPConstant.UNDERSCORE + EXPIRY_DATE_TEXT, getFormattedDate("2".equals(String.valueOf(oldMSISDNOutPut.get(offerPath + OFFER_TYPE_TEXT).getValue())) ? oldMSISDNOutPut.get(offerPath + EXPIRY_DATE_TEXT + TIME_TEXT).getValue() : oldMSISDNOutPut.get(offerPath + EXPIRY_DATE_TEXT).getValue()));
				valueMap.put(OFFER_TYPE_TEXT, oldMSISDNOutPut.get(offerPath + OFFER_TYPE_TEXT).getValue() != null ? oldMSISDNOutPut.get(offerPath + OFFER_TYPE_TEXT).getValue().toString() : "0");
				// Putting all DA information in the offer value map
				if(oldMSISDNDAData.get(daAndProductIdArr[0]) != null)
					valueMap.putAll(oldMSISDNDAData.get(daAndProductIdArr[0]));
				deleteOffersListOldMSISDN.add(oldMSISDNOfferProductMap);
				outputMap.add(valueMap);
			}
			else if(daAndProductIdArr != null && !StringUtil.isNullOrEmpty(daAndProductIdArr[0])){
				oldMSISDNOfferProductMap = new HashMap<String,String>();
				oldMSISDNOfferProductMap.put(OFFER_ID_TEXT, offerId);
				oldMSISDNOfferProductMap.put(FDPConstant.PARAMETER_PRODUCT_ID, daAndProductIdArr[1]);
				FDPLogger.info(circleLogger, getClass(), "filterHandSetOffersOldMSISDN()", "Skipping offerId : " + offerId + " as it already exists on new MSISDN");
				deleteOffersListOldMSISDN.add(oldMSISDNOfferProductMap);
			}
			else{
				FDPLogger.info(circleLogger, getClass(), "filterHandSetOffersOldMSISDN()", "Skipping offerId : " + offerId + " as no DA mapping found for this offer in cache");
			}
			offerArrCounter++;
			offerPath.setLength(0);
			offerPath.append(OFFER_INFO_LIST_TEXT).append(offerArrCounter).append(FDPConstant.PARAMETER_SEPARATOR);
			offerIdOutputParam = oldMSISDNOutPut.get(offerPath + OFFER_ID_TEXT);
		}
		
		//Invoking Deprovisioning SP to delete handset based uncommon offers between old and new MSISDN and call SPRs from old MSISDN
		deleteRecordsFromOldMSISDN(fdpRequest, fdpResponse, deleteOffersListOldMSISDN);
		
		//Invoking Product Buy SP to update DA balances, expiry dates, update offers and call SPRs for new MSISDN
		provisionUpdatesToNewMSISDN(fdpRequest, fdpResponse, outputMap);
	}
	
	/**
	 * This method will invoke the Buy Product SP on the new MSISDN thereby executing UpdateBalanceAndDate to update DA balances, expiry dates
	 * and UpdateOffer to update the handset based offers and SingleProvisioningRequest to provision the product in RS system for new MSISDN 
	 * @param fdpRequest
	 * @param fdpResponse
	 * @param outputMap
	 * @return
	 * @throws ExecutionFailedException 
	 * @throws EvaluationFailedException 
	 */
	private void provisionUpdatesToNewMSISDN(FDPRequest fdpRequest, FDPResponse fdpResponse, List<Map<String, String>> outputMap) throws ExecutionFailedException {
		FulfillmentRequestImpl fulfillmentRequestImpl = (FulfillmentRequestImpl) fdpRequest;
		
		// Updating Buy Product SP for updates on new MSISDN
		FDPNode fdpNode = DynamicMenuUtil.getAndUpdateFDPNode(fulfillmentRequestImpl.getCircle(), (fulfillmentRequestImpl.getIname() + FDPConstant.SPACE + fulfillmentRequestImpl.getRequestString()), fulfillmentRequestImpl);
        FDPServiceProvisioningNode fdpSPNode = (FDPServiceProvisioningNode) fdpNode;
        if(fdpSPNode != null){
	        Product product = RequestUtil.getProductById(fdpRequest, fdpSPNode.getEntityIdForCache(RequestMetaValuesKey.PRODUCT));
	        ServiceProvisioningRule sp = RequestUtil.getServiceProvisioningById(fdpRequest, 
	        		ServiceProvDataConverterUtil.getKeyForServiceProvMetaBag(product.getProductId(), FDPServiceProvType.PRODUCT, FDPServiceProvSubType.PRODUCT_BUY));
	        fulfillmentRequestImpl.addMetaValue(RequestMetaValuesKey.SERVICE_PROVISIONING, sp);
        }
        else{
        	throw new ExecutionFailedException("Unable to get the node value from dynamic menu cache for " + fulfillmentRequestImpl.getRequestString());
        }
        Long subscriberNumber = getSubsriberNumberFromAUX(fdpRequest, AuxRequestParam.SUBSCRIBER_NUMBER);
        // Setting the subscriber number for UpdateOffer
        fulfillmentRequestImpl.setSubscriberNumber(subscriberNumber);
        
        // Updating the new subscriber number for Product Charging step as it overwrites the subscriber number while updating beneficiary step
        fulfillmentRequestImpl.putAuxiliaryRequestParameter(AuxRequestParam.REQUESTOR_MSISDN, fdpRequest.getSubscriberNumber());
        
        //Skipping charging in case of Buy Product SP call as the charging values are computed from the actual product
        fulfillmentRequestImpl.putAuxiliaryRequestParameter(AuxRequestParam.SKIP_CHARGING, Boolean.TRUE);
        
		// Setting new MSISDN as subscriber number for UpdateBalanceAndDate command
		fulfillmentRequestImpl.setSubscriberNumberToCharge(subscriberNumber);
		// Setting new MSISDN as subscriber number for SingleProvisioningRequest command
		fulfillmentRequestImpl.setIncomingSubscriberNumber(subscriberNumber);
		for(Map<String,String> valueMap: outputMap){
			updateNewMSISDNValuesInRequest(fdpRequest, valueMap);
			fdpResponse = executeSP((FDPRequestImpl)fdpRequest);	
			if (!(Status.SUCCESS.equals(fdpResponse.getExecutionStatus()))) {
					throw new ExecutionFailedException("Failed to execute Product Buy SP, couldn't provision the udpates to NEW MSISDN " + fdpRequest.getSubscriberNumber());
			}
		}
	}

	/**
	 * This method will invoke the Deprovisioning SP on the old MSISDN thereby executing DeleteOffers to delete the handset based offers 
	 * and SingleProvisioningRequest to RS system to deprovision product from old MSISDN
	 * @param fdpRequest
	 * @param fdpResponse
	 * @param deleteOffersListOldMSISDN
	 * @throws ExecutionFailedException
	 */
	private void deleteRecordsFromOldMSISDN(FDPRequest fdpRequest, FDPResponse fdpResponse, List<Map<String, String>> deleteOffersListOldMSISDN) throws ExecutionFailedException {
		FulfillmentRequestImpl fulfillmentRequestImpl = (FulfillmentRequestImpl) fdpRequest;
		
		// Setting MSISDN as subscriber number for RS SingleProvisioningRequest deprovision command
		fulfillmentRequestImpl.setIncomingSubscriberNumber(fulfillmentRequestImpl.getSubscriberNumber());
		
		Product product = null;
		for(Map<String, String> offerProductIdMap : deleteOffersListOldMSISDN){
			updateOfferAndProductInRequest(fdpRequest, offerProductIdMap);
			
			if((product = (Product)fdpRequest.getValueFromRequest(RequestMetaValuesKey.PRODUCT)) != null) {
		        ServiceProvisioningRule sp = RequestUtil.getServiceProvisioningById(fdpRequest, 
		        		ServiceProvDataConverterUtil.getKeyForServiceProvMetaBag(product.getProductId(), FDPServiceProvType.PRODUCT, FDPServiceProvSubType.RS_DEPROVISION_PRODUCT));
		        fulfillmentRequestImpl.addMetaValue(RequestMetaValuesKey.SERVICE_PROVISIONING, sp);
	        } else if(offerProductIdMap.get(FDPConstant.PARAMETER_PRODUCT_ID) == null) {
	        	throw new ExecutionFailedException("Product id: " + offerProductIdMap.get(FDPConstant.PARAMETER_PRODUCT_ID) + " not configured in Product Attribute Cache");
	        } else {
	        	throw new ExecutionFailedException("Product id: " + offerProductIdMap.get(FDPConstant.PARAMETER_PRODUCT_ID) + " not found in MetaCache");
	        }
			fdpResponse = executeSP((FDPRequestImpl)fdpRequest);
			if (!(Status.SUCCESS.equals(fdpResponse.getExecutionStatus()))) {
					throw new ExecutionFailedException("Failed to execute Deprovision SP, couldn't delete the offers from old MSISDN " + fdpRequest.getSubscriberNumber());
			}
		}
	}

	/**
	 * This method fetches the threshold counters for the old MSISDN and updates on new MSISDN
	 * @param fdpRequest
	 * @throws EvaluationFailedException 
	 * @throws ExecutionFailedException 
	 */
	private void processThresholdCounters(FDPRequest fdpRequest) throws ExecutionFailedException, EvaluationFailedException {
		
		FulfillmentRequestImpl fulfillmentRequestImpl = (FulfillmentRequestImpl) fdpRequest;
		Long subscriberNumber = getSubsriberNumberFromAUX(fdpRequest, AuxRequestParam.OLD_MSISDN);
		
		// Setting old MSISDN as subscriber number for GetUsageThresholdsAndCounters command
		fulfillmentRequestImpl.setSubscriberNumber(subscriberNumber);
		fulfillmentRequestImpl.setIncomingSubscriberNumber(subscriberNumber);
		
		executeCommand(fdpRequest, Command.GET_USAGE_THRESHOLDS_AND_COUNTERS);
		
		subscriberNumber = getSubsriberNumberFromAUX(fdpRequest, AuxRequestParam.SUBSCRIBER_NUMBER);
		// Setting new MSISDN as subscriber number for GetUsageThresholdsAndCounters command
		fulfillmentRequestImpl.setSubscriberNumber(subscriberNumber);
		fulfillmentRequestImpl.setIncomingSubscriberNumber(subscriberNumber);
		
		// Prepare resultant data to update from old MSISDN to new MSISDN.
		
		//Updating the counter related values in FDPRequest instance
        executeCommand(fdpRequest, Command.UPDATE_USAGE_THRESHOLDS_AND_COUNTERS);
	}
	
	/**
	 * This method generates DA data with all DA information like id, value, start, expiry date
	 * @param oldMSISDNOutPut - Output of a command which contains dedicated information data
	 * @return
	 */
	private Map<String, Map<String, String>> getOldMSISDNDAMap(Map<String, CommandParam> oldMSISDNOutPut) {
		Map<String, Map<String, String>> daDataMap = new HashMap<String, Map<String, String>>();
		int daArrCounter = 0;
		StringBuilder daInfoPath = new StringBuilder(DEDICATED_ACCOUNT_INFO_TEXT).append(daArrCounter).append(FDPConstant.PARAMETER_SEPARATOR);
		CommandParam daIdOutputParam = oldMSISDNOutPut.get(daInfoPath + DEDICATED_ACCOUNT_ID_TEXT);
		String daId = null;
		while(daIdOutputParam != null) {
			Map<String, String> daInstance = new HashMap<String, String>();
			daInstance.put(DEDICATED_ACCOUNT_ID_TEXT, daId = StringUtil.valueOf(daIdOutputParam.getValue()));
			daInstance.put(DEDICATED_ACCOUNT_ACTIVE_VALUE1_TEXT, StringUtil.valueOf(oldMSISDNOutPut.get(daInfoPath + DEDICATED_ACCOUNT_ACTIVE_VALUE1_TEXT).getValue()));
			daInstance.put(DEDICATED_ACCOUNT_VALUE1_TEXT, StringUtil.valueOf(oldMSISDNOutPut.get(daInfoPath + DEDICATED_ACCOUNT_VALUE1_TEXT).getValue()));
			daInstance.put(DEDICATED_ACCOUNT_ID_TEXT + FDPConstant.UNDERSCORE + START_DATE_TEXT, getFormattedDate(oldMSISDNOutPut.get(daInfoPath + START_DATE_TEXT).getValue()));
			daInstance.put(DEDICATED_ACCOUNT_ID_TEXT + FDPConstant.UNDERSCORE + EXPIRY_DATE_TEXT, getFormattedDate(oldMSISDNOutPut.get(daInfoPath + EXPIRY_DATE_TEXT).getValue()));
			daArrCounter++;
			daInfoPath.setLength(0);
			daInfoPath.append(DEDICATED_ACCOUNT_INFO_TEXT).append(daArrCounter).append(FDPConstant.PARAMETER_SEPARATOR);
			daIdOutputParam = oldMSISDNOutPut.get(daInfoPath + DEDICATED_ACCOUNT_ID_TEXT);
			daDataMap.put(daId, daInstance);
		}
		return daDataMap;
	}

	/**
	 * This method filters out all the offers from the parameter map.
	 * @param newMSISDNOutput - A command output map which contains offer Information
	 * @return
	 */
	private List<String> getNewMSISDNOfferIdList(Map<String, CommandParam> newMSISDNOutput) {
		List<String> offerIdList = new ArrayList<String>();
		int offerArrCounter = 0;
		StringBuilder offerPath = new StringBuilder(OFFER_INFO_TEXT).append(offerArrCounter).append(FDPConstant.PARAMETER_SEPARATOR).append(OFFER_ID_TEXT);
		CommandParam offerIdOutputParam = newMSISDNOutput.get(offerPath.toString());
		while(offerIdOutputParam != null) {
			offerIdList.add(StringUtil.valueOf(offerIdOutputParam.getValue()));
			offerArrCounter++;
			offerPath.setLength(0);
			offerPath.append(OFFER_INFO_TEXT).append(offerArrCounter).append(FDPConstant.PARAMETER_SEPARATOR).append(OFFER_ID_TEXT);
			offerIdOutputParam = newMSISDNOutput.get(offerPath.toString());
		}
		return offerIdList;
	}

	/**
	 * This method generates the map of offer and DA values from cache on the basis of device type and IMEI from the FDPRequest object
	 * @param fdpRequest
	 * 
	 * @return Map<String, String>
	 */
	private Map<String, String[]> generateHandsetOfferDAMap(FDPRequest fdpRequest) {
		Map<String, String[]> offerDAMap = new HashMap<String,String[]>();
		ProductAttributeMapCacheDTO fdpCacheableObject = ProductAttributeCacheUtil.getOfferIdByDeviceTypeAndImei(fdpRequest, circleLogger);
		if(fdpCacheableObject != null){
			for(Map<String, String> entry : fdpCacheableObject.getValueMap().values())
	         offerDAMap.put(entry.get(FDPConstant.PARAMETER_OFFER_ID), new String[]{entry.get(FDPConstant.PARAMETER_DA),entry.get(FDPConstant.PARAMETER_PRODUCT_ID)});
		}
		else
			FDPLogger.info(circleLogger, getClass(), "generateHandsetOfferDAMap()",
					"Data not found in Product Attribute Cache");
	    return offerDAMap;
	}
	
	/**
	 * This method converts the date in a specific format understood by CS
	 * @param startDateFrmt
	 * 
	 * @return String - converted date in "E MMM dd HH:mm:ss Z yyyy" format
	 */
	private String getFormattedDate(Object dateStr) {
		String formatedDate = null;
		if(dateStr != null){ 
			try {
				DateFormat formatter = new SimpleDateFormat("E MMM dd HH:mm:ss Z yyyy");
				Date date = (Date)formatter.parse(((GregorianCalendar) dateStr).getTime().toString());
				Calendar cal = Calendar.getInstance();
				cal.setTime(date);
				formatedDate = cal.get(Calendar.DATE) + "/" + (cal.get(Calendar.MONTH) + 1) + "/" + cal.get(Calendar.YEAR);
			} catch (ParseException e) {
				FDPLogger.error(circleLogger, getClass(), "execute()",
						"Problem persist while formatting date");
			}
		}
		return formatedDate;
	}

	/**
	 * This method is to find out the subscriberNumber from FDPRequest AUX parameter
	 * @param startDateFrmt
	 * 
	 * @return Long
	 */
	private Long getSubsriberNumberFromAUX(FDPRequest fdpRequest, AuxRequestParam auxParam) {
		Object msisdn = fdpRequest.getAuxiliaryRequestParameter(auxParam);
		return (msisdn != null && msisdn instanceof Long) ? Long.parseLong(msisdn.toString()) : null;
	}

	/**
	 * This method executes the commands and returns the output param map
	 * @param fdpRequest
	 * @param command
	 * @throws ExecutionFailedException 
	 * @throws EvaluationFailedException 
	 * 
	 * @return Map<String,CommandParam>
	 */
	private Map<String,CommandParam> executeCommand(FDPRequest fdpRequest, Command command) throws ExecutionFailedException {
		final FDPCacheable cachedCommand = ApplicationConfigUtil.getMetaDataCache().getValue(new FDPMetaBag(fdpRequest.getCircle(), ModuleType.COMMAND, command.getCommandDisplayName()));
		if (cachedCommand instanceof FDPCommand) {
			FDPCommand cmdToExecute = CommandUtil.getExectuableFDPCommand((FDPCommand) cachedCommand);
			Status status = cmdToExecute.execute(fdpRequest);
			if(Status.SUCCESS.equals(status)){
				fdpRequest.addExecutedCommand(cmdToExecute);
				return fdpRequest.getExecutedCommand(command.getCommandName()).getOutputParams();
			} else{
				throw new ExecutionFailedException("Command : " + command.getCommandDisplayName() + " execution failed, unable to get response.");
			}
		}else{
			throw new ExecutionFailedException("Command : " + command.getCommandDisplayName() + " not found in cache.");
		}
	}
	
	/**
	 * This method will update the new MSISDN related values in the FDPRequest instance
	 * @param fdpRequest
	 * @param valueMap
	 * @throws ExecutionFailedException 
	 */
	private void updateNewMSISDNValuesInRequest(FDPRequest fdpRequest,
			Map<String, String> valueMap) throws ExecutionFailedException {
		
		// For UpdateOffer, update the offer_id in AuxRequestParam in FDPRequest instance
		// For SingleProvisioningRequest, update the product in MetakeyValues in FDPRequest instance
		updateOfferAndProductInRequest(fdpRequest, valueMap);
		
		// For UpdateBalanceAndDate, update the DA ID, DA Expiry Date, DA Start Date, DA Amount in AuxRequestParam in FDPRequest instance
		final FulfillmentRequestImpl fulfillmentRequestImpl = (FulfillmentRequestImpl) fdpRequest;
		
		fulfillmentRequestImpl.putAuxiliaryRequestParameter(AuxRequestParam.DA_ID, valueMap.get(DEDICATED_ACCOUNT_ID_TEXT));
		fulfillmentRequestImpl.putAuxiliaryRequestParameter(AuxRequestParam.DA_AMOUNT, valueMap.get(DEDICATED_ACCOUNT_VALUE1_TEXT));
		fulfillmentRequestImpl.putAuxiliaryRequestParameter(AuxRequestParam.DA_START_DATE, valueMap.get(DEDICATED_ACCOUNT_ID_TEXT + FDPConstant.UNDERSCORE + START_DATE_TEXT));
		fulfillmentRequestImpl.putAuxiliaryRequestParameter(AuxRequestParam.DA_EXPIRY_DATE, valueMap.get(DEDICATED_ACCOUNT_ID_TEXT + FDPConstant.UNDERSCORE + EXPIRY_DATE_TEXT));
		//FulfillmentUtil.updateValueInInputCmdObject(CommandParamInput.class, "commandParameterSource", cmdToExecute.getInputParam("provisioningType"), ParameterFeedType.INPUT);
				//FulfillmentUtil.updateValueInInputCmdObject(CommandParamInput.class, FDPConstant.DEFINED_VALUE_TEXT, cmdToExecute.getInputParam("provisioningType"), "A");
	}
	
	/**
	 * This method will update the old MSISDN related values in the FDPRequest instance
	 * @param fdpRequest
	 * @param valueMap
	 * @throws ExecutionFailedException 
	 */
	private void updateOfferAndProductInRequest(FDPRequest fdpRequest,
			Map<String, String> valueMap) throws ExecutionFailedException {
		final FulfillmentRequestImpl fulfillmentRequestImpl = (FulfillmentRequestImpl) fdpRequest;
		// Updating Product and Deprovisioning SP for the product id
        Product product = RequestUtil.getProductById(fdpRequest, valueMap.get(FDPConstant.PARAMETER_PRODUCT_ID));
        fulfillmentRequestImpl.addMetaValue(RequestMetaValuesKey.PRODUCT, product);
                
        // For DeleteOffer, update the offer_id in AuxRequestParam in FDPRequest instance
        fulfillmentRequestImpl.putAuxiliaryRequestParameter(AuxRequestParam.OFFER_ID_VAL, valueMap.get(OFFER_ID_TEXT));
        fulfillmentRequestImpl.putAuxiliaryRequestParameter(AuxRequestParam.OFFER_START_DATE, valueMap.get(OFFER_ID_TEXT + FDPConstant.UNDERSCORE + START_DATE_TEXT));
        fulfillmentRequestImpl.putAuxiliaryRequestParameter(AuxRequestParam.OFFER_EXPIRY_DATE, valueMap.get(OFFER_ID_TEXT + FDPConstant.UNDERSCORE + EXPIRY_DATE_TEXT));
        fulfillmentRequestImpl.putAuxiliaryRequestParameter(AuxRequestParam.OFFER_TYPE, valueMap.get(OFFER_TYPE_TEXT));
	}
}
