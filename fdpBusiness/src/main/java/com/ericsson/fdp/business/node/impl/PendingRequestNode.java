package com.ericsson.fdp.business.node.impl;

import java.util.List;
import java.util.Map;

import com.ericsson.fdp.FDPCacheable;
import com.ericsson.fdp.business.enums.AuxRequestParam;
import com.ericsson.fdp.business.enums.State;
import com.ericsson.fdp.business.node.FDPCustomNode;
import com.ericsson.fdp.common.constants.FDPConstant;
import com.ericsson.fdp.common.enums.ChannelType;
import com.ericsson.fdp.common.enums.ModuleType;
import com.ericsson.fdp.common.enums.Visibility;
import com.ericsson.fdp.common.vo.FDPCircle;
import com.ericsson.fdp.core.exception.EvaluationFailedException;
import com.ericsson.fdp.core.exception.ExecutionFailedException;
import com.ericsson.fdp.core.node.FDPNode;
import com.ericsson.fdp.core.request.FDPRequest;
import com.ericsson.fdp.core.serviceprovisioning.bean.FDPMetaBag;
import com.ericsson.fdp.core.utils.ApplicationConfigUtil;
import com.ericsson.fdp.dao.enums.FDPServiceProvSubType;
import com.ericsson.fdp.dao.enums.FDPServiceProvType;
import com.ericsson.fdp.dao.util.ServiceProvDataConverterUtil;

/**
 * This class defines the modes which are of type special menu.
 *
 * @author Ericsson
 *
 */
public class PendingRequestNode extends SpecialMenuNode implements FDPCustomNode {

	/** The Constant serialVersionUID. */
	private static final long serialVersionUID = 7544099193584143579L;

	/**
	 * Instantiates a new pending request node.
	 *
	 * @param displayNameToSet
	 *            the display name to set
	 * @param shortCodeToSet
	 *            the short code to set
	 * @param fullyQualifiedPathToSet
	 *            the fully qualified path to set
	 * @param channelToSet
	 *            the channel to set
	 * @param circleToSet
	 *            the circle to set
	 * @param priorityToSet
	 *            the priority to set
	 * @param parentToSet
	 *            the parent to set
	 * @param childrenToSet
	 *            the children to set
	 * @param entityIdToSet
	 *            the entity id to set
	 * @param entityNameToSet
	 *            the entity name to set
	 */
	public PendingRequestNode(final String displayNameToSet, final String shortCodeToSet,
			final String fullyQualifiedPathToSet, final ChannelType channelToSet, final FDPCircle circleToSet,
			final Long priorityToSet, final FDPNode parentToSet, final List<FDPNode> childrenToSet,
			final Long entityIdToSet, final String entityNameToSet, final Map<String, Object> additionalInfo) {
		super.setAbstractServiceProvisioningNodeNode(displayNameToSet, shortCodeToSet, fullyQualifiedPathToSet,
				channelToSet, circleToSet, priorityToSet, parentToSet, childrenToSet, entityIdToSet, entityNameToSet,
				additionalInfo,Visibility.VISIBLE_FOR_MENU,State.ACTIVE_FOR_MARKET);
	}

	@Override
	public boolean evaluateNode(final FDPRequest fdpRequest) throws EvaluationFailedException {
		return super.evaluateNode(fdpRequest);
	}

	@Override
	public FDPCacheable getServiceProvisioning(final FDPRequest fdpRequest) throws ExecutionFailedException {
		return ApplicationConfigUtil.getMetaDataCache().getValue(
				new FDPMetaBag(fdpRequest.getCircle(), ModuleType.SP_PRODUCT, getSPId(fdpRequest)));
	}

	/**
	 * Gets the sP id.
	 *
	 * @param fdpRequest
	 *            the fdp request
	 * @return the sP id
	 */
	private String getSPId(final FDPRequest fdpRequest) {
		final String confirmationVal = fdpRequest.getAuxiliaryRequestParameter(AuxRequestParam.CONFIRMATION).toString()
				.trim();
		return ServiceProvDataConverterUtil
				.getKeyForServiceProvMetaBag(
						getEntityId(),
						FDPServiceProvType.PRODUCT,
						FDPConstant.CONFIRMATION_VALUE_FOR_PENDING_REQUEST.equals(confirmationVal) ? FDPServiceProvSubType.SHARED_ACCOUNT_ACCEPT_CONSUMER
								: FDPServiceProvSubType.SHARED_ACCOUNT_REJECT_CONSUMER);
	}

}
