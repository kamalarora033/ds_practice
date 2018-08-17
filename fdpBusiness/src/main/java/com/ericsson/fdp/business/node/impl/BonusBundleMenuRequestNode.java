package com.ericsson.fdp.business.node.impl;

import java.util.List;
import java.util.Map;

import com.ericsson.fdp.FDPCacheable;
import com.ericsson.fdp.business.enums.State;
import com.ericsson.fdp.business.node.FDPCustomNode;
import com.ericsson.fdp.common.enums.ChannelType;
import com.ericsson.fdp.common.enums.ModuleType;
import com.ericsson.fdp.common.enums.Visibility;
import com.ericsson.fdp.common.vo.FDPCircle;
import com.ericsson.fdp.core.exception.EvaluationFailedException;
import com.ericsson.fdp.core.exception.ExecutionFailedException;
import com.ericsson.fdp.core.node.FDPNode;
import com.ericsson.fdp.core.request.FDPRequest;
import com.ericsson.fdp.core.request.FDPResponse;
import com.ericsson.fdp.core.serviceprovisioning.bean.FDPMetaBag;
import com.ericsson.fdp.core.utils.ApplicationConfigUtil;
import com.ericsson.fdp.dao.enums.FDPServiceProvSubType;
import com.ericsson.fdp.dao.enums.FDPServiceProvType;
import com.ericsson.fdp.dao.util.ServiceProvDataConverterUtil;

public class BonusBundleMenuRequestNode extends SpecialMenuNode implements FDPCustomNode{

	/**
	 * The Class serial version UID.
	 */
	private static final long serialVersionUID = 7155584286786789519L;
	
	public BonusBundleMenuRequestNode(final String displayNameToSet, final String shortCodeToSet,
			final String fullyQualifiedPathToSet, final ChannelType channelToSet, final FDPCircle circleToSet,
			final Long priorityToSet, final FDPNode parentToSet, final List<FDPNode> childrenToSet,
			final Long entityIdToSet, final String entityNameToSet, final FDPServiceProvSubType fdpServiceProvSubType,Map<String, Object> additionalInfo) {
		super(displayNameToSet, shortCodeToSet, fullyQualifiedPathToSet,
				channelToSet, circleToSet, priorityToSet, parentToSet, childrenToSet, entityIdToSet, entityNameToSet
				,fdpServiceProvSubType,additionalInfo,Visibility.VISIBLE_FOR_MENU,State.ACTIVE_FOR_MARKET);
	}

	@Override
	public boolean evaluateNode(FDPRequest fdpRequest) throws EvaluationFailedException {
		return super.evaluateNode(fdpRequest);
	}

	@Override
	public FDPCacheable getServiceProvisioning(final FDPRequest fdpRequest) throws ExecutionFailedException {
		return ApplicationConfigUtil.getMetaDataCache().getValue(
				new FDPMetaBag(fdpRequest.getCircle(), ModuleType.SP_OTHERS, getSPId(fdpRequest)));
	}

	@Override
	public FDPResponse executePolicy(FDPRequest fdpRequest) throws ExecutionFailedException {
		/*try {
			ProductUtil.updateForProductBuyPolicy(fdpRequest, getEntityIdForCache(RequestMetaValuesKey.PRODUCT),
					getEntityIdForCache(RequestMetaValuesKey.SERVICE_PROVISIONING),ChargingType.RS_DEPROVISIONING);
		} catch (final EvaluationFailedException e) {
			throw new ExecutionFailedException("Could not update policy ", e);
		}*/
		return super.executePolicy(fdpRequest);
	}

	/**
	 * Gets the sP id.
	 * 
	 * @param fdpRequest
	 *            the fdp request
	 * @return the sP id
	 */
	private String getSPId(final FDPRequest fdpRequest) {
		return ServiceProvDataConverterUtil.getKeyForServiceProvMetaBag(getEntityId(), FDPServiceProvType.OTHER,
				FDPServiceProvSubType.SHARED_BUNDLE_DELETE_CONSUMER);
	}

}
