package com.ericsson.fdp.business.serviceprovisioning.rule;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.Map;

import ch.qos.logback.classic.Logger;

import com.ericsson.fdp.business.enums.AuxRequestParam;
import com.ericsson.fdp.business.enums.RequestMetaValuesKey;
import com.ericsson.fdp.business.enums.TLVOptions;
import com.ericsson.fdp.business.exception.NotificationFailedException;
import com.ericsson.fdp.business.exception.RuleException;
import com.ericsson.fdp.business.notification.FDPServiceProvisioningNotification;
import com.ericsson.fdp.business.product.Product;
import com.ericsson.fdp.business.request.impl.FDPResponseImpl;
import com.ericsson.fdp.business.rule.impl.RuleImpl;
import com.ericsson.fdp.business.step.FDPStep;
import com.ericsson.fdp.business.util.LoggerUtil;
import com.ericsson.fdp.business.util.NotificationUtil;
import com.ericsson.fdp.business.util.ResponseUtil;
import com.ericsson.fdp.common.constants.FDPConstant;
import com.ericsson.fdp.common.enums.ChannelType;
import com.ericsson.fdp.common.enums.Status;
import com.ericsson.fdp.core.command.FDPCommand;
import com.ericsson.fdp.core.command.param.CommandParam;
import com.ericsson.fdp.core.config.utils.PropertyUtils;
import com.ericsson.fdp.core.exception.ExecutionFailedException;
import com.ericsson.fdp.core.logging.FDPLogger;
import com.ericsson.fdp.core.request.FDPRequest;
import com.ericsson.fdp.core.request.FDPResponse;
import com.ericsson.fdp.core.request.impl.FDPRequestImpl;
import com.ericsson.fdp.core.serviceprovisioning.bean.FDPRequestBag;
import com.ericsson.fdp.core.utils.ApplicationConfigUtil;
import com.ericsson.fdp.dao.dto.serviceprov.AbstractServiceProvDTO;
import com.ericsson.fdp.dao.dto.serviceprov.ServiceProvDTO;
import com.ericsson.fdp.dao.enums.FDPServiceProvSubType;
import com.ericsson.fdp.dao.enums.ProductAdditionalInfoEnum;

/**
 * This class defines the service provisioning rule.
 * 
 * @author Ericsson
 * 
 */
public class ServiceProvisioningRule extends RuleImpl {

	/**
	 * 
	 */
	private static final long serialVersionUID = -5907336118736931454L;
	/** The notification to be sent. */
	private FDPServiceProvisioningNotification notification;

	private ServiceProvDTO serviceProvDTO;
	
/*	need to be removed ehlnopu
 * private static final String abilityLogFilePath = PropertyUtils
			.getProperty("esf.file.path");
	private static final String abilityChannelList = PropertyUtils
			.getProperty("esf.channelList");
	private static final String abilityServerURL = PropertyUtils
			.getProperty("esf.URL");*/

//	private ESFClientServiceImpl abilityClient = new ESFClientServiceImpl();
	/**
	 * The constructor for service provisioning rule class.
	 * 
	 * @param fdpStepsToSet
	 *            The steps to be executed.
	 * @param notificationToSet
	 *            The notification to be sent.
	 * @param performRollbackToSet
	 *            True, if rollback is to be performed, false otherwise.
	 */
	public ServiceProvisioningRule(final List<FDPStep> fdpStepsToSet,
			final FDPServiceProvisioningNotification notificationToSet,
			final boolean performRollbackToSet,
			final ServiceProvDTO serviceProvDTO) {
		super(fdpStepsToSet, performRollbackToSet);
		this.notification = notificationToSet;
		this.serviceProvDTO = serviceProvDTO;
	}
	
	/**
	 * The constructor.
	 * 
	 * @param fdpStepsToSet
	 * @param notificationToSet
	 * @param performRollbackToSet
	 */
	public ServiceProvisioningRule(final List<FDPStep> fdpStepsToSet,
			final FDPServiceProvisioningNotification notificationToSet,
			final boolean performRollbackToSet) {
		super(fdpStepsToSet, performRollbackToSet);
		this.notification = notificationToSet;
	}

	@Override
	public FDPResponse execute(final FDPRequest fdpRequest)
			throws RuleException {
		Logger circleLogger = LoggerUtil
				.getSummaryLoggerFromRequest(fdpRequest);
		Status executeStatus = Status.FAILURE;
		FDPResponse response = null;
		try {
			FDPLogger.debug(circleLogger, getClass(), "execute()",
					LoggerUtil.getRequestAppender(fdpRequest)
							+ "Executing service provisioning rules");
			response = super.execute(fdpRequest);
			executeStatus = response.getExecutionStatus();
			FDPLogger.debug(circleLogger, getClass(), "execute()",
					LoggerUtil.getRequestAppender(fdpRequest)
							+ "Execution status for service provisioning rule "
							+ executeStatus);
		} catch (RuleException e) {
			executeStatus = Status.FAILURE;
			FDPLogger.error(circleLogger, getClass(), "execute()",
					LoggerUtil.getRequestAppender(fdpRequest) + "Rule failed",
					e);
			throw new RuleException("Rule failed", e);
		} finally {
			try {
				// Check for mass upload send notification( if
				// (fdpRequest.getToSendNotification() == null , it means true
				// else send not)
				if (fdpRequest.getToSendNotification() == null) {
					// TODO: notification is null at this moment. Check with
					// cache
					// to update it.
					// TODO: returning hard coded text. Please remove.
					FDPLogger.debug(circleLogger, getClass(), "execute()",
							LoggerUtil.getRequestAppender(fdpRequest)
									+ "Creating notification text for status "
									+ executeStatus);
					String notificationText = isAsyncNotificationCase ? (notification.createNotificationForAsyncSuccess(fdpRequest)) : (notification == null ? null
							: notification.createNotificationText(fdpRequest,
									executeStatus));
					FDPLogger.debug(circleLogger, getClass(), "execute()",
							LoggerUtil.getRequestAppender(fdpRequest)
									+ "Notification text is :- "
									+ notificationText);		
					
						if ((response!=null && null!=response.getExecutionStatus())?(response.getExecutionStatus()==
								Status.SUCESS_ON_MM ):false) {
							NotificationUtil.sendOfflineNotification(
									fdpRequest, notificationText);

							FDPResponseImpl fdpResponseImpl = (FDPResponseImpl) response;
							fdpResponseImpl.setExecutionStatus(Status.SUCCESS);
							fdpResponseImpl.setTerminateSession(true);
							fdpResponseImpl
									.addResponseString(ResponseUtil.createResponseMessage(
											fdpRequest.getChannel(),
											notificationText,
											fdpResponseImpl
													.isTerminateSession() ? TLVOptions.SESSION_TERMINATE
													: TLVOptions.SESSION_CONTINUE));
							// put the MM data in cache Web cache
							try {

									String key = (String)fdpRequest
													.getExecutedCommand(
															"MM DEBIT")
													.getOutputParams()
													.get("transactionid")
													.getValue();
									ApplicationConfigUtil
											.getRequestCacheForMMWeb()
											.putValue(new FDPRequestBag(key),
													fdpRequest);
									

							} catch (ExecutionFailedException e) {
								FDPLogger.error(circleLogger, getClass(), "execute()",
										LoggerUtil.getRequestAppender(fdpRequest)
												+ " Error while fetching data from mobile money cache"+e);
							}

							
						}
					
					/*
					 * String notificationText =
					 * executeStatus.equals(Status.SUCCESS) ?
					 * "Product buying successfull" : "Product buying failed.";
					 */
					else if (response instanceof FDPResponseImpl) {
						FDPResponseImpl responseImpl = (FDPResponseImpl) response;
						
						final Product product = (Product) fdpRequest.getValueFromRequest(RequestMetaValuesKey.PRODUCT);
						final ServiceProvisioningRule serviceProvisionRule = (ServiceProvisioningRule) fdpRequest.getValueFromRequest(RequestMetaValuesKey.SERVICE_PROVISIONING);
							//1st Notification comes to continue session for AutoRenewal
						String actionName=null;
						if (serviceProvisionRule.getServiceProvDTO() instanceof AbstractServiceProvDTO) {
                			AbstractServiceProvDTO abstractServiceProvDTO = (AbstractServiceProvDTO) serviceProvisionRule
                					.getServiceProvDTO();
                			FDPLogger.debug(circleLogger, getClass(), "getFDPProductProvisionType()",
                					LoggerUtil.getRequestAppender(fdpRequest) + "ActionName "
                							+ abstractServiceProvDTO.getSpSubType().name());
                			 actionName = abstractServiceProvDTO.getSpSubType().name();
                		}
                        if (null != product
                                && responseImpl.isTerminateSession()
                                && ChannelType.USSD.equals(fdpRequest.getChannel())
                                && (FDPConstant.TRUE.equalsIgnoreCase(product.getAdditionalInfo(ProductAdditionalInfoEnum.IS_AUTO_RENEWAL)))
	                        		&& FDPServiceProvSubType.PRODUCT_BUY_RECURRING.name()
	            					.equals(actionName)){
	                        	responseImpl.addResponseString(ResponseUtil.createResponseMessage(
	                                             fdpRequest.getChannel(),
	                                             notificationText, TLVOptions.SESSION_CONTINUE));
	                               ((FDPRequestImpl)fdpRequest).putAuxiliaryRequestParameter(AuxRequestParam.AUTO_RENEWAL_SESSION_TERMINATE, true);
	                               
	                              String  smsNotificationTextForAdhoc = isAsyncNotificationCase ? asyncSMSNotificationText(fdpRequest, notificationText) : ((notification == null || Status.FAILURE
	       								.equals(executeStatus)) ? null : notification
	       								.createNotificationText(fdpRequest,
	       										executeStatus, true));
	                              ((FDPRequestImpl)fdpRequest).putAuxiliaryRequestParameter(AuxRequestParam.ADHOC_FLASH_TEXT, smsNotificationTextForAdhoc);

	                        }else{

	    						responseImpl
	    								.addResponseString(ResponseUtil.createResponseMessage(
	    										fdpRequest.getChannel(),
	    										notificationText,
	    										responseImpl.isTerminateSession() ? TLVOptions.SESSION_TERMINATE
	    												: TLVOptions.SESSION_CONTINUE));
	                        }
					} else {
						boolean terminateSession = response == null ? true
								: response.isTerminateSession();
						response = new FDPResponseImpl(
								executeStatus,
								terminateSession,
								ResponseUtil.createResponseMessageInList(
										fdpRequest.getChannel(),
										notificationText,
										terminateSession ? TLVOptions.SESSION_TERMINATE
												: TLVOptions.SESSION_CONTINUE),
								response != null ? response.getResponseError()
										: null);
					}

				//	if (breakmm) {
						String smsNotificationText = isAsyncNotificationCase ? asyncSMSNotificationText(fdpRequest, notificationText) : ((notification == null || Status.FAILURE
								.equals(executeStatus)) ? null : notification
								.createNotificationText(fdpRequest,
										executeStatus, true));
						if (smsNotificationText != null) {
							FDPLogger
									.debug(circleLogger,
											getClass(),
											"execute()",
											LoggerUtil
													.getRequestAppender(fdpRequest)
													+ "Notification text was found for sms :- "
													+ smsNotificationText);
							NotificationUtil.sendOfflineNotification(
									fdpRequest, smsNotificationText);
						} else {
							FDPLogger
									.debug(circleLogger,
											getClass(),
											"execute()",
											LoggerUtil
													.getRequestAppender(fdpRequest)
													+ "Notification text was not found for sms :- "
													+ notificationText);
						}
				//	}
					
					
				}

			} catch (NotificationFailedException e) {
				FDPLogger.error(circleLogger, getClass(), "execute()",
						LoggerUtil.getRequestAppender(fdpRequest)
								+ "Notification could not be sent", e);
				throw new RuleException("Notification could not be sent", e);
			}
		}
		return response;
	}

	/**
	 * 
	 * ehlnopu need to be removed 
	 * @param fdpRequest
	 * @param circleLogger
	 * @param response
	
	private void updateAbilityServer(final FDPRequest fdpRequest,
			Logger circleLogger, FDPResponse response) {
		
		Map<String, String> attributes = abilityAttributeMap(fdpRequest, response.getExecutionStatus());
		
		boolean result = abilityClient.updateAbility(attributes,fdpRequest.getCircle().getCircleCode(),abilityServerURL);
		
		if (result) {
			FDPLogger.debug(circleLogger, getClass(),
					"execute()",
					"Successfully update Ability Server");

		} else {
			try {
				ESFFileUtils file = new ESFFileUtils();
				file.writeESFLogFile(
						abilityLogFilePath+FDPConstant.HOURLY+".txt", attributes);
			} catch (FileNotFoundException e) {
				FDPLogger.debug(circleLogger, getClass(),
						"execute()",
						"File is not found at specified location"
								+ e);
			} catch (IOException e) {
				FDPLogger.debug(circleLogger, getClass(),
						"execute()", "" + e);
			}

		}
	}
 */
	
	/**
	 * @return the serviceProvDTO
	 */
	public ServiceProvDTO getServiceProvDTO() {
		return serviceProvDTO;
	}
	
	/*private Map<String, String> abilityAttributeMap(FDPRequest fdpRequest, Status executionStatus) {

		Map<String, String> mp = new HashMap<String, String>();

		ServiceProvisioningRule serviceProv = (ServiceProvisioningRule)fdpRequest.getValueFromRequest(RequestMetaValuesKey.SERVICE_PROVISIONING);
		
		if(CommandParamUtil.isBlackBerryUser(fdpRequest)){		
			if(serviceProv.getServiceProvDTO().getName().startsWith("ACT")){
				mp.put("blackberry", "ACT");
			}else if(serviceProv.getServiceProvDTO().getName().startsWith("DEACT")){
				mp.put("blackberry", "DEACT");
			}	

		}

		if(fdpRequest instanceof FulfillmentRequestImpl){
			FulfillmentRequestImpl fulfillmentRequestImpl = (FulfillmentRequestImpl)fdpRequest;
			mp.put("clientTransactionId", fulfillmentRequestImpl.getTransaction_id());			
		}
		mp.put("requestId", fdpRequest.getRequestId());
		Product product = (Product)fdpRequest.getValueFromRequest(RequestMetaValuesKey.PRODUCT);		

		ChargingValue chargingValue =  (ChargingValue)fdpRequest.getValueFromRequest(RequestMetaValuesKey.CHARGING_STEP);
		mp.put("productId", product.getProductId().toString());
		mp.put("productName", product.getProductName());
		mp.put("amtCharged", chargingValue.getChargingValue()==null?null:chargingValue.getChargingValue().toString());
		mp.put("paySource", chargingValue.getExternalSystemToUse().name());

		FDPCommand  fdpCommand = fdpRequest.getExecutedCommand(Command.REFILL.getCommandDisplayName());
		String refillProfileId = getRefillId(fdpRequest, fdpCommand);


		if(refillProfileId != null) {
			String offerId = getOfferId(fdpRequest, refillProfileId);
			if(offerId !=null) {
				mp.put("offerId", offerId);
				getOfferStartEndDate(fdpRequest, offerId, fdpCommand, mp);
			}
		}

		mp.put("SourceChannel", fdpRequest.getChannel().getName());

		// Setting product buy status
		mp.put("status", executionStatus.getStatusText());

		return mp;

	}*/

	private void getOfferStartEndDate(FDPRequest fdpRequest,
			String offerId, FDPCommand fdpCommand, Map<String, String> mp) {
		
		Logger circleLogger = LoggerUtil
				.getSummaryLoggerFromRequest(fdpRequest);
		if(offerId != null) {
			Map<String, CommandParam> outputParamMap = fdpCommand.getOutputParams(); 
			String offerPath = "offerinformationlist"+FDPConstant.DOT;
			int offerArrCounter = 0;
			String offerPathNew = offerPath+offerArrCounter+FDPConstant.DOT;
			CommandParam offerIdOutputParam = outputParamMap.get(offerPathNew+"offerid");
			while(offerIdOutputParam != null) {
				if(offerIdOutputParam.getValue().toString().equals(offerId)) {
					String startDate = offerPathNew+"startdate";
					String endDate = offerPathNew+"expirydate";

					String startDateFrmt = outputParamMap.get(startDate).getValue()==null?null:((GregorianCalendar)outputParamMap.get(startDate).getValue()).getTime().toString();
					String expiryDateFrmt = outputParamMap.get(endDate).getValue()==null?null:((GregorianCalendar)outputParamMap.get(endDate).getValue()).getTime().toString();

					try {
						mp.put("offerStartDate", convertDateFormat(startDateFrmt));
						mp.put("offerExpiryDate", convertDateFormat(expiryDateFrmt));
					} catch (ParseException e) {
						FDPLogger.info(circleLogger, getClass(), "execute()",
								"Problem persist while formatting offer start and expiry date");
					}

					break;
				}
				offerArrCounter++;
				offerPathNew = offerPath+offerArrCounter+FDPConstant.DOT;
				offerIdOutputParam = outputParamMap.get(offerPathNew+"offerid");
			}
		}	
	}

/*	private String getOfferId(FDPRequest fdpRequest, String refillProfileId) {
		String offerId = null;
		FDPMetaBag fdpmetabag = new FDPMetaBag(fdpRequest.getCircle(), ModuleType.AIR_CONFIG, "REFILL:" + refillProfileId);
		
		FDPCache<FDPMetaBag, FDPCacheable> configCache=null;
	
		try {
			configCache = ApplicationConfigUtil.getMetaDataCache();
		} catch (ExecutionFailedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		Refill refill=(configCache.getValue(fdpmetabag)!=null)?((Refill)configCache.getValue(fdpmetabag)):null;
		if(refill != null) {
			offerId = refill.getOfferid();
		}
		return offerId;
	}*/

/*	private String getRefillId(FDPRequest fdpRequest, FDPCommand fdpCommand) {
		String refillProfileId = null;
		for(CommandParam commandParams:fdpCommand.getInputParam()){
				if(commandParams.getName().equals("refillProfileID")){
					refillProfileId = commandParams.getValue().toString();
					break;
			}
		}
		return refillProfileId;
	}*/

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

	/**
	 * @param serviceProvDTO
	 *            the serviceProvDTO to set
	 */
	public void setServiceProvDTO(ServiceProvDTO serviceProvDTO) {
		this.serviceProvDTO = serviceProvDTO;
	}
	
	/**
	 * To Check if Allowed or not.
	 * @param fdpRequest
	 * @param isAsyncNotificationCase
	 * @return
	 */
	private String asyncSMSNotificationText(final FDPRequest fdpRequest, final String  notificationText) {
		String text = null;
		try {
			if (null != PropertyUtils.getProperty("ASYNC_SMS_ALLOW")
					&& Boolean.TRUE.toString().equalsIgnoreCase(PropertyUtils.getProperty("ASYNC_SMS_ALLOW"))) {
				text = notificationText;
			}
		} catch (final Exception e) {
			e.printStackTrace();
		}
		return text;
	}
}
