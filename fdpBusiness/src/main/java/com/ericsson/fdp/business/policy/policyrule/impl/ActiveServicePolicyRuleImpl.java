package com.ericsson.fdp.business.policy.policyrule.impl;

import java.util.ArrayList;
import java.util.List;

import javax.naming.NamingException;

import com.ericsson.fdp.business.display.impl.DisplayObjectImpl;
import com.ericsson.fdp.business.enums.ConfigurationKey;
import com.ericsson.fdp.business.enums.RequestMetaValuesKey;
import com.ericsson.fdp.business.enums.TLVOptions;
import com.ericsson.fdp.business.exception.NotificationFailedException;
import com.ericsson.fdp.business.node.impl.RSDeprovisioningRequestNode;
import com.ericsson.fdp.business.request.FDPPolicyResponse;
import com.ericsson.fdp.business.request.impl.FDPResponseImpl;
import com.ericsson.fdp.business.sharedaccount.ActiveAccountService;
import com.ericsson.fdp.business.util.ActiveServicesRequestUtil;
import com.ericsson.fdp.business.util.RequestUtil;
import com.ericsson.fdp.business.util.ResponseUtil;
import com.ericsson.fdp.business.vo.FDPActiveServicesVO;
import com.ericsson.fdp.common.constants.FDPConstant;
import com.ericsson.fdp.common.enums.Status;
import com.ericsson.fdp.common.vo.FDPCircle;
import com.ericsson.fdp.core.exception.ExecutionFailedException;
import com.ericsson.fdp.core.node.FDPNode;
import com.ericsson.fdp.core.request.FDPRequest;
import com.ericsson.fdp.core.request.FDPResponse;
import com.ericsson.fdp.core.utils.ApplicationConfigUtil;
import com.ericsson.fdp.dao.enums.DBActionClassName;

/**
 * This class implements the policy rule.
 * 
 * @author Ericsson
 */
public class ActiveServicePolicyRuleImpl extends PolicyRuleImpl {

	/** The Constant serialVersionUID. */
	private static final long serialVersionUID = 5999259668460963311L;

	/** The ejb lookup name. */
	private final String ejbLookupName = DBActionClassName.RS_DE_PROVISIONING_SERVICE.getLookUpClass();

	@Override
	public final FDPPolicyResponse validatePolicyRule(final Object input, final FDPRequest fdpRequest,
			final Object... otherParams) throws ExecutionFailedException {
		return null;
	}

	@Override
	public FDPResponse displayRule(final FDPRequest fdpRequest) throws ExecutionFailedException {
		if (ejbLookupName == null) {
			throw new ExecutionFailedException("Could not find ejb class for execution" + ejbLookupName);
		}
		try {
			final Object beanObject = ApplicationConfigUtil.getBean(ejbLookupName);
			List<FDPActiveServicesVO> activeServices = null;
			if (null != beanObject && (beanObject instanceof ActiveAccountService)) {
				final ActiveAccountService accountService = (ActiveAccountService) beanObject;
				activeServices = accountService.executeActiveAccountService(fdpRequest);
				return (activeServices == null || activeServices.isEmpty()) ? getNoPamActiveServiceResponse(fdpRequest)
						: createNodesFromOutput(activeServices, fdpRequest);
			} else {
				throw new ExecutionFailedException("Can't Locate Service" + beanObject);
			}
		} catch (final NamingException e) {
			throw new ExecutionFailedException("The bean could not be found " + ejbLookupName, e);
		}
	}

	/**
	 * Creates the nodes from output.
	 * 
	 * @param activeServices
	 *            the active services
	 * @param fdpRequest
	 *            the fdp request
	 * @return the fDP response
	 * @throws ExecutionFailedException
	 *             the execution failed exception
	 */
	private FDPResponse createNodesFromOutput(final List<FDPActiveServicesVO> activeServices,
			final FDPRequest fdpRequest) throws ExecutionFailedException {
		List<FDPNode> fdpNodeList = null;
		if (activeServices != null && !activeServices.isEmpty()) {
			fdpNodeList = new ArrayList<FDPNode>();
			try {
				for (final FDPActiveServicesVO activeServicesVO : activeServices) {
					RSDeprovisioningRequestNode deprovisioningRequestNode;
					deprovisioningRequestNode = new RSDeprovisioningRequestNode(
							ActiveServicesRequestUtil.createNotificationText(fdpRequest, activeServicesVO), null,
							((FDPNode) fdpRequest.getValueFromRequest(RequestMetaValuesKey.NODE))
									.getFullyQualifiedPath(), fdpRequest.getChannel(), fdpRequest.getCircle(), null,
							null, null, activeServicesVO.getProduct().getProductId(), null);
					deprovisioningRequestNode.setPolicyName(FDPConstant.RS_DEPORV_POLICY);
					fdpNodeList.add(deprovisioningRequestNode);
				}
			} catch (final NotificationFailedException e) {
				throw new ExecutionFailedException("Could not create virtual nodes name ", e);
			}
		}

		return RequestUtil.createResponseFromDisplayObject(
				new DisplayObjectImpl((FDPNode) fdpRequest.getValueFromRequest(RequestMetaValuesKey.NODE), fdpNodeList,
						ResponseUtil.getDisplayNodeMessage(fdpRequest.getChannel())), fdpRequest, (FDPNode) fdpRequest
						.getValueFromRequest(RequestMetaValuesKey.NODE));
	}

	/**
	 * Gets the notification text for consumer.
	 * 
	 * @param fdpRequest
	 *            the fdp request
	 * @return the notification text for consumer
	 * @throws ExecutionFailedException
	 *             the execution failed exception
	 */
	protected FDPResponseImpl getNoPamActiveServiceResponse(final FDPRequest fdpRequest)
			throws ExecutionFailedException {
		return new FDPResponseImpl(Status.SUCCESS, false, ResponseUtil.createResponseMessageInList(
				fdpRequest.getChannel(), getNotificationText(fdpRequest), TLVOptions.SESSION_TERMINATE));
	}

	/**
	 * Gets the notification text for post paid consumer.
	 * 
	 * @param fdpRequest
	 *            the fdp request
	 * @return the notification text for post paid consumer
	 */
	private String getNotificationText(final FDPRequest fdpRequest) {
		String notificationText = null;
		final FDPCircle fdpCircle = fdpRequest.getCircle();
		notificationText = fdpCircle.getConfigurationKeyValueMap().get(
				ConfigurationKey.NOTIFICATION_PAM_NOT_CONFIGURED_TEXT.getAttributeName());
		return notificationText;
	}

}
