package com.ericsson.fdp.business.sharedaccount.service.impl;

import java.util.Map;
import java.util.concurrent.ThreadLocalRandom;

import javax.annotation.Resource;
import javax.ejb.Stateless;
import javax.inject.Inject;
import javax.naming.NamingException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ericsson.fdp.FDPCacheable;
import com.ericsson.fdp.business.sharedaccount.service.AccountDetailService;
import com.ericsson.fdp.business.transaction.TransactionSequenceGeneratorService;
import com.ericsson.fdp.business.util.RequestUtil;
import com.ericsson.fdp.business.util.SharedAccountUtil;
import com.ericsson.fdp.common.constants.JNDILookupConstant;
import com.ericsson.fdp.common.enums.ChannelType;
import com.ericsson.fdp.common.enums.ModuleType;
import com.ericsson.fdp.common.enums.Status;
import com.ericsson.fdp.common.exception.FDPServiceException;
import com.ericsson.fdp.common.vo.CircleConfigParamDTO;
import com.ericsson.fdp.common.vo.FDPCircle;
import com.ericsson.fdp.core.cache.AppCacheSubStore;
import com.ericsson.fdp.core.command.FDPCommand;
import com.ericsson.fdp.core.command.param.CommandParam;
import com.ericsson.fdp.core.dsm.framework.service.FDPCache;
import com.ericsson.fdp.core.exception.ExecutionFailedException;
import com.ericsson.fdp.core.request.impl.FDPWEBRequestImpl;
import com.ericsson.fdp.core.serviceprovisioning.bean.FDPAppBag;
import com.ericsson.fdp.core.serviceprovisioning.bean.FDPMetaBag;
import com.ericsson.fdp.core.utils.ApplicationConfigUtil;
import com.ericsson.fdp.core.utils.CircleCodeFinder;
import com.ericsson.fdp.dao.fdpadmin.FDPWEBCommandParameterMappingDAO;

@Stateless(name = "AccountDetailService")
public class AccountDetailServiceImpl implements AccountDetailService {

	/** The Constant LOGGER. */
	private static final Logger LOGGER = LoggerFactory.getLogger(FDPSharedAccountUIServiceImpl.class);

/*	@Inject
	private TransactionSequenceDAO transactionSequenceDAO;
*/	
	@Resource(lookup = JNDILookupConstant.TRANSACTION_ID_GENERATOR_SERVICE)
	private TransactionSequenceGeneratorService generatorService;

	@Inject
	private FDPWEBCommandParameterMappingDAO paramDAO;

	@Override
	public Map<String, CommandParam> runCommand(final Long subscriberNumber, final String commandDisplayName)
			throws FDPServiceException {
		FDPCommand fdpCommand = null;
		try {
			final FDPCircle circle = RequestUtil.getFDPCircleFromMsisdn(subscriberNumber.toString());
			if (circle != null) {
				final FDPWEBRequestImpl webRequest = new FDPWEBRequestImpl();

				final String fullMsisdn = SharedAccountUtil.getFullMsisdn(subscriberNumber);

				final CircleConfigParamDTO paramDTO = RequestUtil.populateCircleConfigParamDTO(fullMsisdn, circle);
				webRequest.setChannel(ChannelType.WEB);
				webRequest.setCircle(circle);
				webRequest.setSubscriberNumberNAI(paramDTO.getSubscriberNumberNAI());
				webRequest.setSubscriberNumber(paramDTO.getSubscriberNumber());
				webRequest.setIncomingSubscriberNumber(paramDTO.getIncomingSubscriberNumber());
				webRequest.setOriginNodeType(paramDTO.getOriginNodeType());
				webRequest.setOriginHostName(paramDTO.getOriginHostName());
				webRequest.setOriginTransactionID(generateTransactionId());
				webRequest
						.setRequestId("Web_" + generateTransactionId() + "_" + ThreadLocalRandom.current().nextLong());
				final FDPCacheable fdpCommandCached = ApplicationConfigUtil.getMetaDataCache().getValue(
						new FDPMetaBag(webRequest.getCircle(), ModuleType.COMMAND, commandDisplayName));
				// If command is null it means command is not configured for the
				// circle.
				if (fdpCommandCached instanceof FDPCommand) {
					fdpCommand = (FDPCommand) fdpCommandCached;
				}
				if (fdpCommand != null) {
					fdpCommand.execute(webRequest).equals(Status.SUCCESS);
				} else {
					throw new FDPServiceException("Command " + commandDisplayName + " is not found;");
				}
			}
		} catch (final NamingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (final ExecutionFailedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return fdpCommand.getOutputParams();
	}// end of getGeneralInfo

/*	*//**
	 * This method is used to generate the transaction id to be used
	 * 
	 * @return the transaction id.
	 *//*
	private Long generateTransactionId() {
		return transactionSequenceDAO.getNextTransactionNumber();
	}*/

	@Override
	public Map<String, String> getOutputFullParameterPath(final String commandNameToDisplay) {
		return paramDAO.getParameterFullPath(commandNameToDisplay);
	}

	@Override
	public FDPCircle getFDPCircleFromMsisdn(final Long msisdnNumber) throws NamingException {
		return RequestUtil.getFDPCircleFromMsisdn(msisdnNumber.toString());
	}

	@Override
	public String getCSAttributeValue(final String msisdn, final String className, final String originalValue) {
		String result = originalValue;
		try {
			final FDPCache<FDPAppBag, Object> appCache = ApplicationConfigUtil.getApplicationConfigCache();
			final String circleCode = CircleCodeFinder.getCircleCode(msisdn, appCache);
			final FDPCircle fdpCircle = (FDPCircle) appCache.getValue(new FDPAppBag(AppCacheSubStore.CS_ATTRIBUTES,
					circleCode));
			final Map<String, Map<String, String>> csAttrMap = fdpCircle.getCsAttributesKeyValueMap();
			final Map<String, String> csAttrClass = csAttrMap.get(className);
			final String value = csAttrClass.get(originalValue);
			if (value != null) {
				result = value;
			}
		} catch (final Exception e) {
			LOGGER.debug("Exception Occured while getting csAttribute for msisdn = {} class = {} and value = {}",
					new Object[] { msisdn, className, originalValue });
		}
		return result;
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
