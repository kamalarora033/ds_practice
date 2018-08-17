package com.ericsson.fdp.business.sharedaccount.impl;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.ejb.Stateless;
import javax.inject.Inject;

import com.ericsson.fdp.business.exception.NotificationFailedException;
import com.ericsson.fdp.business.node.impl.PendingRequestNode;
import com.ericsson.fdp.business.sharedaccount.AbstractSharedAccountService;
import com.ericsson.fdp.business.sharedaccount.constans.SharedAccountConstants;
import com.ericsson.fdp.business.util.PendingRequestUtil;
import com.ericsson.fdp.common.constants.FDPConstant;
import com.ericsson.fdp.core.exception.ExecutionFailedException;
import com.ericsson.fdp.core.node.FDPNode;
import com.ericsson.fdp.core.request.FDPRequest;
import com.ericsson.fdp.dao.dto.sharedaccount.SharedAccountRequestDTO;
import com.ericsson.fdp.dao.enums.ConsumerRequestType;
import com.ericsson.fdp.dao.enums.SharedAccountResponseType;
import com.ericsson.fdp.dao.fdpadmin.FDPSharedAccountReqDAO;

//import com.ericsson.fdp.business.notification.impl.PendingRequestNotificationTemplate;

@Stateless
public class PendingRequestServiceImpl extends AbstractSharedAccountService {

	@Inject
	private FDPSharedAccountReqDAO fdpSharedAccountReqDAO;

	@Override
	public Map<SharedAccountResponseType, Object> executeSharedAccountService(final FDPRequest fdpRequest,
			final Object... params) throws ExecutionFailedException {

		final List<SharedAccountRequestDTO> addConsumerList = fdpSharedAccountReqDAO.getPendingRequestForConsumer(
				fdpRequest.getSubscriberNumber(), fdpRequest.getCircle().getCircleId(),
				ConsumerRequestType.ADD_CONSUMER);
		final Map<SharedAccountResponseType, Object> statusDataMap = new HashMap<SharedAccountResponseType, Object>();
		List<FDPNode> fdpNodeList = null;
		try {
			if (addConsumerList != null && !addConsumerList.isEmpty()) {
				fdpNodeList = new ArrayList<FDPNode>();
				for (final SharedAccountRequestDTO addConsumerDTO : addConsumerList) {
					final PendingRequestNode pendingRequestNode = new PendingRequestNode(
							PendingRequestUtil.createNotificationText(fdpRequest, addConsumerDTO.getSenderMsisdn(),
									addConsumerDTO.getAccReqNumber()), null, null, fdpRequest.getChannel(),
							fdpRequest.getCircle(), null, null, null, addConsumerDTO.getAccReqNumber(), null, null);
					pendingRequestNode.setPolicyName(FDPConstant.PENDING_REQUEST_POLICY);
					fdpNodeList.add(pendingRequestNode);
				}
			}
		} catch (final NotificationFailedException e) {
			throw new ExecutionFailedException("Could not create node name", e);
		}
		statusDataMap.put(SharedAccountResponseType.STATUS, SharedAccountConstants.SUCCESS);
		statusDataMap.put(SharedAccountResponseType.CONSUMER_MSISDN, fdpRequest.getSubscriberNumber());
		statusDataMap.put(SharedAccountResponseType.NODES, fdpNodeList);
		return statusDataMap;
	}
}
