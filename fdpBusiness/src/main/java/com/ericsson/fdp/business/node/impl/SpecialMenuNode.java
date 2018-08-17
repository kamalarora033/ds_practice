package com.ericsson.fdp.business.node.impl;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.ericsson.fdp.business.enums.AuxRequestParam;
import com.ericsson.fdp.business.enums.RequestMetaValuesKey;
import com.ericsson.fdp.business.enums.State;
import com.ericsson.fdp.business.exception.ExpressionFailedException;
import com.ericsson.fdp.business.expression.Expression;
import com.ericsson.fdp.business.node.AbstractServiceProvisioningNode;
import com.ericsson.fdp.common.enums.ChannelType;
import com.ericsson.fdp.common.enums.DynamicMenuAdditionalInfoKey;
import com.ericsson.fdp.common.enums.Visibility;
import com.ericsson.fdp.common.vo.FDPCircle;
import com.ericsson.fdp.core.exception.EvaluationFailedException;
import com.ericsson.fdp.core.node.FDPNode;
import com.ericsson.fdp.core.request.FDPRequest;
import com.ericsson.fdp.core.request.impl.FDPRequestImpl;
import com.ericsson.fdp.dao.enums.FDPServiceProvSubType;
import com.ericsson.fdp.dao.enums.FDPServiceProvType;
import com.ericsson.fdp.dao.enums.LanguageType;
import com.ericsson.fdp.dao.util.ServiceProvDataConverterUtil;

/**
 * This class defines the modes which are of type special menu.
 *
 * @author Ericsson
 *
 */
public class SpecialMenuNode extends AbstractServiceProvisioningNode {

	/**
	 * The serial version id.
	 */
	private static final long serialVersionUID = 5632870146410401468L;

	/**
	 * Instantiates a new special menu node.
	 */
	public SpecialMenuNode() {
	}

	/**
	 * The expression or constraint for the node.
	 */
	private Expression fdpExpression;

	/**
	 * The special menu node type.
	 */
	private FDPServiceProvSubType specialMenuNodeType;


	@Override
	public String getEntityIdForCache(final RequestMetaValuesKey key) {
		return ServiceProvDataConverterUtil.getKeyForServiceProvMetaBag(getEntityId(), FDPServiceProvType.OTHER, null);
	}

	/**
	 * Instantiates a new special menu node.
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
	 * @param specialMenuNodeTypeToSet
	 *            the special menu node type to set
	 */
	public SpecialMenuNode(final String displayNameToSet, final String shortCodeToSet,
			final String fullyQualifiedPathToSet, final ChannelType channelToSet, final FDPCircle circleToSet,
			final Long priorityToSet, final FDPNode parentToSet, final List<FDPNode> childrenToSet,
			final Long entityIdToSet, final String entityNameToSet, final FDPServiceProvSubType specialMenuNodeTypeToSet,
			final Map<String, Object> additionalInfo,final Visibility visibility,final State state) {
		super.setAbstractServiceProvisioningNodeNode(displayNameToSet, shortCodeToSet, fullyQualifiedPathToSet,
				channelToSet, circleToSet, priorityToSet, parentToSet, childrenToSet, entityIdToSet, entityNameToSet, additionalInfo,visibility,state);
		this.specialMenuNodeType = specialMenuNodeTypeToSet;
		if(null!= additionalInfo  && additionalInfo.get(DynamicMenuAdditionalInfoKey.FRENCH_DISPLAY_NAME.name())!=null){
			Map<LanguageType, String> otherLanguageMap = new HashMap<LanguageType, String> ();
			otherLanguageMap.put(LanguageType.FRENCH, additionalInfo.get(DynamicMenuAdditionalInfoKey.FRENCH_DISPLAY_NAME.name()).toString());
			this.setOtherLangMap(otherLanguageMap);
		}
	}


	@Override
	public String generateNodeInfo(final FDPRequest fdpRequest) {
		return "SpecialMenu";
	}


	@Override
	public boolean evaluateNode(final FDPRequest fdpRequest) throws EvaluationFailedException {
		boolean nodeValue = false;
		if (fdpExpression == null) {
			nodeValue = super.evaluateNode(fdpRequest);
		} else {
			try {
				udpateAuxInRequest(fdpRequest);
				nodeValue = fdpExpression.evaluateExpression(fdpRequest);
			} catch (ExpressionFailedException e) {
				throw new EvaluationFailedException("The expression could not be evaluated", e);
			}
		}
		return nodeValue;
	}

	/**
	 * Sets the fdp expression.
	 *
	 * @param fdpExpression
	 *            the new fdp expression
	 */
	public void setFdpExpression(final Expression fdpExpression) {
		this.fdpExpression = fdpExpression;
	}

	/**
	 * This method is used to check if this node is used for shared account.
	 *
	 * @return true if used for shared account, false otherwise.
	 */
	public boolean isSharedAccountType() {
		return specialMenuNodeType.isSharedAccountType();
	}

	/**
	 * This method is used to get the special menu node type.
	 *
	 * @return the special menu node type.
	 */
	public FDPServiceProvSubType getSpecialMenuNodeType() {
		return specialMenuNodeType;
	}

	/**
	 * This method will update the AUX Param for Bundle node ("ONLY").
	 * 
	 * @param fdpRequest
	 */
	private void udpateAuxInRequest(final FDPRequest fdpRequest) {
		if ((FDPServiceProvSubType.SHARED_BUNDLE_ADD_CONSUMER.equals(this.getSpecialMenuNodeType())
				|| FDPServiceProvSubType.SHARED_BUNDLE_DELETE_CONSUMER.equals(this.getSpecialMenuNodeType())
				|| FDPServiceProvSubType.SHARED_BUNDLE_VIEW_CONSUMER.equals(this.getSpecialMenuNodeType())
				|| FDPServiceProvSubType.SHARED_BUNDLE_VIEW_PROVIDER.equals(this.getSpecialMenuNodeType())
				|| FDPServiceProvSubType.SHARED_BUNDLE_UPDATE_CONSUMER_DATA.equals(this.getSpecialMenuNodeType())
				|| FDPServiceProvSubType.SHARED_BUNDLE_DELETE_SELF_CONSUMER.equals(this.getSpecialMenuNodeType())
				|| FDPServiceProvSubType.SHARED_BUNDLE_CREATE_PACK.equals(this.getSpecialMenuNodeType())
				|| FDPServiceProvSubType.SHARED_DATA_BALANCE.equals(this.getSpecialMenuNodeType())
				|| FDPServiceProvSubType.TERMS_AND_CONDITION.equals(this.getSpecialMenuNodeType())
				|| FDPServiceProvSubType.VIEW_SELF_BALANCE.equals(this.getSpecialMenuNodeType())
				|| FDPServiceProvSubType.SHARED_BUNDLE_REQEUEST_TOPUP_REQUEST.equals(this.getSpecialMenuNodeType())) && fdpRequest instanceof FDPRequestImpl
				) {
			final FDPRequestImpl fdpRequestImpl = (FDPRequestImpl) fdpRequest;
			fdpRequestImpl.putAuxiliaryRequestParameter(AuxRequestParam.PROVIDER_MSISDN, fdpRequest.getSubscriberNumber().toString());
		}
	}
}
