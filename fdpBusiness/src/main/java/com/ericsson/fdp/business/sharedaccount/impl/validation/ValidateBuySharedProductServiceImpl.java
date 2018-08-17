/**
 * 
 */
package com.ericsson.fdp.business.sharedaccount.impl.validation;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.ejb.Stateless;
import javax.inject.Inject;

import com.ericsson.fdp.business.sharedaccount.AbstractSharedAccountService;
import com.ericsson.fdp.business.sharedaccount.constans.SharedAccountConstants;
import com.ericsson.fdp.core.exception.ExecutionFailedException;
import com.ericsson.fdp.core.request.FDPRequest;
import com.ericsson.fdp.dao.dto.sharedaccount.ProductAddInfoAttributeDTO;
import com.ericsson.fdp.dao.dto.sharedaccount.SharedAccGpDTO;
import com.ericsson.fdp.dao.enums.SharedAccountErrorCodeEnum;
import com.ericsson.fdp.dao.enums.SharedAccountGroupStatus;
import com.ericsson.fdp.dao.enums.SharedAccountResponseType;
import com.ericsson.fdp.dao.fdpadmin.FDPSharedAccountGroupDAO;

@Stateless
public class ValidateBuySharedProductServiceImpl extends AbstractSharedAccountService {

	@Inject
	private FDPSharedAccountGroupDAO fdpSharedAccountGroupDAO;

	@Override
	public Map<SharedAccountResponseType, Object> executeSharedAccountService(final FDPRequest fdpRequest,
			final Object... params) throws ExecutionFailedException {

		final ProductAddInfoAttributeDTO productAddInfo = getProviderAddInfo((Long) params[0]);
		final List<SharedAccGpDTO> acceptConsumerList = fdpSharedAccountGroupDAO.getSharedAccGroup(
				fdpRequest.getSubscriberNumber(), productAddInfo.getShrAccOfferId(), null);
		final Map<SharedAccountResponseType, Object> statusDataMap = new HashMap<SharedAccountResponseType, Object>();
		if (acceptConsumerList != null && !acceptConsumerList.isEmpty()) {
			final SharedAccGpDTO shareAccGroup = acceptConsumerList.get(0);
			if (shareAccGroup.getStatus().equals(SharedAccountGroupStatus.DORMENT)) {
				statusDataMap.put(SharedAccountResponseType.STATUS, SharedAccountConstants.FAILURE);
				statusDataMap.put(SharedAccountResponseType.PROVIDER_MSISDN, fdpRequest.getSubscriberNumber());
				statusDataMap.put(SharedAccountResponseType.ERROR_CODE, SharedAccountErrorCodeEnum.ACCOUNT_DORMENT.getErrorCode().toString());
				statusDataMap.put(SharedAccountResponseType.ERROR_MESSAGE,
						SharedAccountErrorCodeEnum.ACCOUNT_DORMENT.getErrorMessage());
			} else {
				statusDataMap.put(SharedAccountResponseType.STATUS, SharedAccountConstants.SUCCESS);
				statusDataMap.put(SharedAccountResponseType.PROVIDER_MSISDN, fdpRequest.getSubscriberNumber());
			}

		} else {
			statusDataMap.put(SharedAccountResponseType.STATUS, SharedAccountConstants.SUCCESS);
			statusDataMap.put(SharedAccountResponseType.PROVIDER_MSISDN, fdpRequest.getSubscriberNumber());

		}

		return statusDataMap;
	}
}
