package com.ericsson.fdp.business.sharedaccount.impl;

import java.util.HashMap;
import java.util.Map;

import javax.ejb.Stateless;
import javax.inject.Inject;

import com.ericsson.fdp.business.enums.AuxRequestParam;
import com.ericsson.fdp.business.sharedaccount.AbstractSharedAccountService;
import com.ericsson.fdp.business.sharedaccount.constans.SharedAccountConstants;
import com.ericsson.fdp.core.exception.ExecutionFailedException;
import com.ericsson.fdp.core.node.FDPNode;
import com.ericsson.fdp.core.request.FDPRequest;
import com.ericsson.fdp.dao.dto.sharedaccount.ProductAddInfoAttributeDTO;
import com.ericsson.fdp.dao.enums.EntityType;
import com.ericsson.fdp.dao.enums.SharedAccountResponseType;
import com.ericsson.fdp.dao.enums.StepNameEnum;
import com.ericsson.fdp.dao.enums.UtUpgradeType;
import com.ericsson.fdp.dao.exception.FDPConcurrencyException;
import com.ericsson.fdp.dao.fdpadmin.FDPSharedAccountConsumerDAO;

@Stateless
public class ProviderUTUpgradeServiceImpl extends AbstractSharedAccountService {

	@Inject
	private FDPSharedAccountConsumerDAO fdpSharedAccountConsumerDAO;

	@Override
	public Map<SharedAccountResponseType, Object> executeSharedAccountService(final FDPRequest fdpRequest,
			final Object... params) throws ExecutionFailedException {
		final Map<SharedAccountResponseType, Object> statusDataMap = new HashMap<SharedAccountResponseType, Object>();
		final ProductAddInfoAttributeDTO proAddInfoDto = getProviderAddInfo((Long) params[0]);
		final Long dbID = (Long) fdpRequest.getValueFromStep(StepNameEnum.VALIDATION_STEP.getValue(),
				SharedAccountResponseType.DB_ID.name());
		final FDPNode fdpNode = fdpRequest.getAuxiliaryRequestParameter(AuxRequestParam.CURRENT_SHARED_ACCOUNT_VALUE) instanceof FDPNode ? (FDPNode) fdpRequest
				.getAuxiliaryRequestParameter(AuxRequestParam.CURRENT_SHARED_ACCOUNT_VALUE) : null;
		if (fdpNode != null) {
			try {
				fdpSharedAccountConsumerDAO.updateConsumerUTValue(dbID,
						(Long) fdpNode.getAdditionalInfo(EntityType.NEW_LIMIT.getEntityType()),
						(UtUpgradeType) fdpNode.getAdditionalInfo(EntityType.UT_UPGRADE_TYPE.getEntityType()),
						(Integer) fdpNode.getAdditionalInfo(EntityType.NO_OF_DAYS.getEntityType()),
						proAddInfoDto.getCommonUsageThresholdCounterID(), fdpRequest.getSubscriberNumber().toString());
			} catch (final FDPConcurrencyException e) {
				throw new ExecutionFailedException("Could not update request", e);
			}
			statusDataMap.put(SharedAccountResponseType.STATUS, SharedAccountConstants.SUCCESS);
		} else {
			statusDataMap.put(SharedAccountResponseType.STATUS, SharedAccountConstants.FAILURE);
		}
		statusDataMap.put(SharedAccountResponseType.PROVIDER_MSISDN, fdpRequest.getSubscriberNumber());
		statusDataMap.put(SharedAccountResponseType.CONSUMER_MSISDN, fdpRequest.getValueFromStep(
				StepNameEnum.VALIDATION_STEP.getValue(), SharedAccountResponseType.CONSUMER_MSISDN.name()));

		return statusDataMap;
	}
}
