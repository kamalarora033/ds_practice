package com.ericsson.fdp.business.sharedaccount.impl;

import java.util.HashMap;
import java.util.Map;

import javax.annotation.Resource;
import javax.ejb.Stateless;
import javax.inject.Inject;

import com.ericsson.fdp.business.sharedaccount.AbstractSharedAccountService;
import com.ericsson.fdp.business.sharedaccount.constans.SharedAccountConstants;
import com.ericsson.fdp.common.constants.JNDILookupConstant;
import com.ericsson.fdp.core.dsm.framework.service.FDPCache;
import com.ericsson.fdp.core.exception.ExecutionFailedException;
import com.ericsson.fdp.core.request.FDPRequest;
import com.ericsson.fdp.core.serviceprovisioning.bean.FDPAppBag;
import com.ericsson.fdp.dao.entity.FDPSharedAccountReq;
import com.ericsson.fdp.dao.enums.ConsumerStatusEnum;
import com.ericsson.fdp.dao.enums.SharedAccountResponseType;
import com.ericsson.fdp.dao.enums.StepNameEnum;
import com.ericsson.fdp.dao.exception.FDPConcurrencyException;
import com.ericsson.fdp.dao.fdpadmin.FDPSharedAccountGroupDAO;
import com.ericsson.fdp.dao.fdpadmin.FDPSharedAccountReqDAO;
import com.ericsson.fdp.dao.fdpadmin.FDPWebUserProductDAO;

@Stateless
public class RejectConsumerServiceImpl extends AbstractSharedAccountService {

	@Inject
	private FDPSharedAccountReqDAO fdpSharedAccountReqDao;

	@Inject
	private FDPWebUserProductDAO fdpWebUserProductDao;

	@Inject
	private FDPSharedAccountGroupDAO fdpSharedAccountGroupDao;

	/** The application cache. */
	@Resource(lookup = JNDILookupConstant.APPLICATION_CONFIG_CACHE_JNDI_NAME)
	private FDPCache<FDPAppBag, Object> applicationConfigCache;

	@SuppressWarnings("unchecked")
	@Override
	public Map<SharedAccountResponseType, Object> executeSharedAccountService(final FDPRequest fdpRequest,
			final Object... params) throws ExecutionFailedException {
		final Long sharAccReqId = (Long) fdpRequest.getValueFromStep(StepNameEnum.VALIDATION_STEP.getValue(),
				SharedAccountResponseType.PARENT_ID.name());
		final Map<SharedAccountResponseType, Object> statusDataMap = new HashMap<SharedAccountResponseType, Object>();

		FDPSharedAccountReq shareAccGroupId;
		try {
			shareAccGroupId = fdpSharedAccountReqDao.updateSharedRequestStatus(sharAccReqId,
					ConsumerStatusEnum.REJECTED, fdpRequest.getSubscriberNumber().toString());
		} catch (final FDPConcurrencyException e) {
			throw new ExecutionFailedException("Could not update request", e);
		}
		statusDataMap.put(SharedAccountResponseType.STATUS, SharedAccountConstants.SUCCESS);
		statusDataMap.put(SharedAccountResponseType.DB_ID, shareAccGroupId.getSharedAccountReqId());
		statusDataMap.put(SharedAccountResponseType.CONSUMER_MSISDN, fdpRequest.getSubscriberNumber());
		statusDataMap.put(SharedAccountResponseType.PROVIDER_MSISDN, fdpRequest.getValueFromStep(
				StepNameEnum.VALIDATION_STEP.getValue(), SharedAccountResponseType.PROVIDER_MSISDN.name()));

		return statusDataMap;
	}

}
