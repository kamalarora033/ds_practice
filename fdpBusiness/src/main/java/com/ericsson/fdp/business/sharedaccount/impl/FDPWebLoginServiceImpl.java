package com.ericsson.fdp.business.sharedaccount.impl;

import java.net.Inet4Address;
import java.net.UnknownHostException;
import java.util.Calendar;
import java.util.Random;
import java.util.UUID;

import javax.annotation.Resource;
import javax.ejb.Stateless;
import javax.inject.Inject;

import com.ericsson.fdp.business.request.FDPCheckConsumerResponse;
import com.ericsson.fdp.business.sharedaccount.service.CheckConsumerService;
import com.ericsson.fdp.business.sharedaccount.service.FDPWebLoginService;
import com.ericsson.fdp.business.transaction.TransactionSequenceGeneratorService;
import com.ericsson.fdp.business.util.RequestUtil;
import com.ericsson.fdp.business.util.SharedAccountUtil;
import com.ericsson.fdp.common.constants.FDPConstant;
import com.ericsson.fdp.common.constants.JNDILookupConstant;
import com.ericsson.fdp.common.enums.ChannelType;
import com.ericsson.fdp.common.enums.Status;
import com.ericsson.fdp.common.exception.FDPServiceException;
import com.ericsson.fdp.common.vo.FDPCircle;
import com.ericsson.fdp.core.cache.AppCacheSubStore;
import com.ericsson.fdp.core.config.utils.PropertyUtils;
import com.ericsson.fdp.core.dsm.framework.service.FDPCache;
import com.ericsson.fdp.core.dsm.framework.service.impl.FDPCircleCacheProducer;
import com.ericsson.fdp.core.exception.ExecutionFailedException;
import com.ericsson.fdp.core.request.impl.FDPRequestImpl;
import com.ericsson.fdp.core.serviceprovisioning.bean.FDPAppBag;
import com.ericsson.fdp.core.utils.CircleCodeFinder;
import com.ericsson.fdp.dao.dto.ExchangeMessageResponse;
import com.ericsson.fdp.dao.dto.FDPOtpDTO;
import com.ericsson.fdp.dao.enums.appcache.AdminConfigurations;
import com.ericsson.fdp.dao.exception.FDPConcurrencyException;
import com.ericsson.fdp.dao.exception.FDPDataNotFoundException;
import com.ericsson.fdp.dao.fdpadmin.FDPConfigurationDAO;
import com.ericsson.fdp.dao.fdpadmin.FDPOtpDAO;

@Stateless(mappedName = "fdpWebLoginService")
public class FDPWebLoginServiceImpl implements FDPWebLoginService {

/*	*//** The transaction sequence dao. *//*
	@Inject
	private TransactionSequenceDAO transactionSequenceDAO;*/
	
	@Resource(lookup = JNDILookupConstant.TRANSACTION_ID_GENERATOR_SERVICE)
	private TransactionSequenceGeneratorService generatorService;

	/** The fdp otpdao. */
	@Inject
	FDPOtpDAO fdpOTPDAO;

	/** The configuration dao. */
	@Inject
	private FDPConfigurationDAO configurationDAO;

	/** The circle cache producer. */
	@Resource(lookup = "java:app/fdpCoreServices-1.0/FDPCircleCacheProducer")
	private FDPCircleCacheProducer circleCacheProducer;

	@Resource(lookup = "java:app/fdpCoreServices-1.0/ApplicationConfigCache")
	private FDPCache<FDPAppBag, Object> applicationConfigCache;

	@Resource(lookup = "java:app/fdpBusiness-1.0/CheckConsumerServiceImpl")
	private CheckConsumerService checkConsumerService;

	// /** The interface definition service. */
	// @EJB(mappedName =
	// "java:app/fdpAdminServices/AdminAttributesListServiceImpl")
	// private AdminAttributesListService attributesListService;

	@Override
	public String generateOTP(final Long msisdnNumber) {
		String result = PropertyUtils.getMessageProperty("otp.send.success");
		try {
			final FDPCheckConsumerResponse response = checkConsumerService
					.checkPrePaidConsumer(createFDPRequest(msisdnNumber));
			if (Status.SUCCESS.equals(response.getExecutionStatus())) {
				if (response.isPrePaidConsumer()) {
					// set an expiry date for OTP which will the number of
					// minutes
					// defined
					// in the configuration cache
					final FDPAppBag appBag = new FDPAppBag();
					appBag.setSubStore(AppCacheSubStore.CONFIGURATION_MAP);
					appBag.setKey(AdminConfigurations.OTP_EXPIRY_TIME_MINUTES.getKey());
					Integer expiryTime;
					final Object expTime = applicationConfigCache.getValue(appBag);
					if (expTime != null) {
						expiryTime = Integer.parseInt(expTime.toString());
					} else {
						expiryTime = Integer.parseInt(configurationDAO.getFDPConfigurationByName(
								AdminConfigurations.OTP_EXPIRY_TIME_MINUTES.getKey()).getAttributeValue());
					}

					final Calendar expiryDate = Calendar.getInstance();
					expiryDate.add(Calendar.MINUTE, expiryTime);

					final StringBuffer randomOTP = new StringBuffer();
					final Random generator = new Random();
					int randomNumber;
					for (int i = 0; i < 5; i++) {
						// This method will generate radom number ranging from
						// 48 to 57 which are ASCII caharacters of numbers 0-9
						randomNumber = 48 + generator.nextInt(10);
						randomOTP.append((char) randomNumber);
					}
					final String otp = randomOTP.toString();
					fdpOTPDAO.setOTP(msisdnNumber, otp, expiryDate);
					sendOtpMessage(msisdnNumber, otp);
				} else {
					result = PropertyUtils.getMessageProperty("otp.send.invalid.consumer");
				}
			} else {
				result = PropertyUtils.getMessageProperty("otp.send.fail");
			}
		} catch (final FDPServiceException e1) {
			result = PropertyUtils.getMessageProperty("otp.send.fail");
		} catch (final ExecutionFailedException e1) {
			result = PropertyUtils.getMessageProperty("otp.send.fail");
		}
		return result;
	}

	private void sendOtpMessage(final Long msisdn, final String otp) throws FDPServiceException {
		try {
			final String circleCode = CircleCodeFinder.getCircleCode(msisdn.toString(), applicationConfigCache);
			if (circleCode == null) {
				throw new FDPServiceException("Invalid number.");
			}
			final ExchangeMessageResponse message = new ExchangeMessageResponse();
			message.setExternalSystemType(ChannelType.SMS.getName());
			message.setMsisdn(msisdn.toString());
			message.setServiceModeType("WAP");
			message.setCircleId(circleCode);
			final String key = AdminConfigurations.OTP_NOTIFICATION_TEMPLETE.getKey();
			final FDPAppBag appBag = new FDPAppBag();
			appBag.setKey(circleCode);
			appBag.setSubStore(AppCacheSubStore.CIRCLE_CODE_CIRCLE_NAME_MAP);
			final FDPCircle circle = (FDPCircle) applicationConfigCache.getValue(appBag);
			final Object passwordTemplate = circle.getConfigurationKeyValueMap().get(key);
			String msg;
			if (passwordTemplate == null) {
				msg = "Your otp is " + otp;
			} else {
				msg = passwordTemplate.toString().replace(FDPConstant.OTP_IDENTIFIER, otp);
			}
			message.setBody(msg);
			final String requestId = ChannelType.SMS.getName() + "_" + Inet4Address.getLocalHost().getHostAddress()
					+ "_" + (String.valueOf(UUID.randomUUID()));
			message.setRequestId(requestId);
			message.setIncomingTrxIpPort(Inet4Address.getLocalHost().getHostAddress());
			circleCacheProducer.pushToQueue(message, circleCode, applicationConfigCache);
		} catch (final UnknownHostException e) {
			throw new FDPServiceException("Unable to send otp.", e);
		}
	}

	@Override
	public FDPOtpDTO getOTPLoginDetails(final Long msisdn) throws FDPDataNotFoundException {
		return fdpOTPDAO.getOTP(msisdn);
	}

	@Override
	public void setOTPExpired(final Long userMsisdn) throws FDPServiceException {
		try {
			fdpOTPDAO.setOTPExpired(userMsisdn);
		} catch (final FDPConcurrencyException e) {
			throw new FDPServiceException(e.getMessage(), e);
		}
	}

	/**
	 * Creates the fdp request.
	 * 
	 * @param providerMsisdn
	 *            the provider msisdn
	 * @return the fDP request impl
	 * @throws ExecutionFailedException
	 *             the execution failed exception
	 * @throws FDPServiceException
	 */
	private FDPRequestImpl createFDPRequest(final Long providerMsisdn) throws ExecutionFailedException,
			FDPServiceException {
		final FDPCircle fdpCircle = CircleCodeFinder.getFDPCircleByMsisdn(providerMsisdn.toString(),
				applicationConfigCache);
		if (fdpCircle == null) {
			throw new FDPServiceException("Circle Not Found for msisdn " + providerMsisdn);
		}
		final String msisdn = SharedAccountUtil.getFullMsisdn(providerMsisdn);
		return RequestUtil.getWebRequest(msisdn, generateTransactionId(), fdpCircle);
	}
	
	/**
	 * This method is used to generate the transaction id to be used.
	 * 
	 * @return the transaction id.
	 */
	private Long generateTransactionId() {
		return generatorService.generateTransactionId();
	}


}
