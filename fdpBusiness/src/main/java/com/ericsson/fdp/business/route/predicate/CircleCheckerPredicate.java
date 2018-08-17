package com.ericsson.fdp.business.route.predicate;

import java.util.List;

import org.apache.camel.Exchange;
import org.apache.camel.Message;
import org.apache.camel.Predicate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ericsson.fdp.business.constants.BusinessConstants;
import com.ericsson.fdp.core.cache.AppCacheSubStore;
import com.ericsson.fdp.core.dsm.framework.service.FDPCache;
import com.ericsson.fdp.core.exception.ExecutionFailedException;
import com.ericsson.fdp.core.serviceprovisioning.bean.FDPAppBag;
import com.ericsson.fdp.core.utils.ApplicationConfigUtil;
import com.ericsson.fdp.core.utils.ApplicationUtil;
import com.ericsson.fdp.dao.dto.FDPSMSCConfigDTO;
import com.ericsson.fdp.dao.entity.ExternalSystemType;

public class CircleCheckerPredicate implements Predicate {

	private final String circleCode;

	public CircleCheckerPredicate(final String circleCode) {
		this.circleCode = circleCode;
	}

	@Override
	@SuppressWarnings("unchecked")
	public boolean matches(final Exchange exchange) {
		
		
		return true;
		/*
		final Logger logger = LoggerFactory.getLogger(getClass());
		final Message message = exchange.getIn();
		System.out.println("Checking circle checker for circle :- " + circleCode);
		System.out.println("The header in message for circle :- " + message.getHeader(BusinessConstants.CIRCLE_ID));
		if (message.getHeader(BusinessConstants.CIRCLE_ID).equals(circleCode)) {
			System.out.println("The circle codes match. Moving forward.");
			try {
				final FDPCache<FDPAppBag, Object> applicationConfigCache = ApplicationConfigUtil
						.getApplicationConfigCache();
				final String serviceType = (String) message.getHeader(BusinessConstants.SERVICE_MODE_TYPE);
				System.out.println("The service type is " + serviceType);
				final String key = ApplicationUtil.getKeyForUSSDAndSMSC(getCircleCode(),
						BusinessConstants.SERVICE_TYPE_SMS.equals(serviceType) ? ExternalSystemType.SMSC_TYPE
								: ExternalSystemType.USSD_TYPE);
				System.out.println("The key formed is " + key);
				final AppCacheSubStore cacheSubStore = BusinessConstants.SERVICE_TYPE_SMS.equals(serviceType) ? AppCacheSubStore.CIRCLE_SMSC_MAPPING
						: AppCacheSubStore.CIRCLE_USSD_MAPPING;
				System.out.println("Cache Substore is " + cacheSubStore);
				final Object configDTOObj = applicationConfigCache.getValue(new FDPAppBag(cacheSubStore, key));
				final String incomingIp = (String) message.getHeader(BusinessConstants.INCOMING_TRX_IP_PORT);
				System.out.println("The incoming ip is " + incomingIp);
				System.out.println("configDTOObj is " + configDTOObj);
				if (configDTOObj != null && configDTOObj instanceof List<?>) {
					System.out.println("Checking with config dto objects/");
					final List<FDPSMSCConfigDTO> configDTOs = (List<FDPSMSCConfigDTO>) configDTOObj;
					for (final FDPSMSCConfigDTO fdpsmscConfigDTO : configDTOs) {
						System.out.println("Checking for " + fdpsmscConfigDTO.getIp() + "_"
								+ fdpsmscConfigDTO.getPort());
						if (incomingIp.equals(fdpsmscConfigDTO.getIp() + "_" + fdpsmscConfigDTO.getPort())) {
							System.out.println("Returning true");
							return true;
						}
					}
				}
			} catch (final ExecutionFailedException e) {
				logger.error("Exception in execution ", e);
				return false;
			}
		}
		System.out.println("returning false");
		return false;
	*/}

	public String getCircleCode() {
		return circleCode;
	}

}
