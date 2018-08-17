package com.ericsson.fdp.business.node;

import java.util.List;
import java.util.Map;

import com.ericsson.fdp.business.enums.AuxRequestParam;
import com.ericsson.fdp.business.enums.State;
import com.ericsson.fdp.common.enums.ChannelType;
import com.ericsson.fdp.common.enums.Visibility;
import com.ericsson.fdp.common.vo.FDPCircle;
import com.ericsson.fdp.core.exception.ExecutionFailedException;
import com.ericsson.fdp.core.node.FDPNode;
import com.ericsson.fdp.core.request.FDPRequest;
import com.ericsson.fdp.core.request.FDPResponse;

/**
 * This class stores information on service provisioning nodes.
 *
 * @author Ericsson
 *
 */
public abstract class AbstractServiceProvisioningNode extends AbstractNode implements FDPServiceProvisioningNode {

	/** The Constant serialVersionUID. */
	private static final long serialVersionUID = 4805706399471760192L;

	/** The entity id. */
	private Long entityId;
	// should be DB id of the product

	/** The entity name. */
	private String entityName;

	// ModuleType enum product

	/**
	 * Sets the abstract service provisioning node node.
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
	protected void setAbstractServiceProvisioningNodeNode(final String displayNameToSet, final String shortCodeToSet,
			final String fullyQualifiedPathToSet, final ChannelType channelToSet, final FDPCircle circleToSet,
			final Long priorityToSet, final FDPNode parentToSet, final List<FDPNode> childrenToSet,
			final Long entityIdToSet, final String entityNameToSet, final Map<String, Object> additionalInfo,final Visibility visibility, final State state) {
		super.setAbstractNode(displayNameToSet, shortCodeToSet, fullyQualifiedPathToSet, channelToSet, circleToSet,
				priorityToSet, parentToSet, childrenToSet, additionalInfo,visibility,state);
		this.entityId = entityIdToSet;
		this.entityName = entityNameToSet;
	}

	/**y
	 * Sets the entity id.
	 *
	 * @param entityIdToSet
	 *            the entityId to set
	 */
	public void setEntityId(final Long entityIdToSet) {
		this.entityId = entityIdToSet;
	}

	/**
	 * Gets the entity name.
	 *
	 * @return the entityName
	 */
	public String getEntityName() {
		return entityName;
	}

	/**
	 * Sets the entity name.
	 *
	 * @param entityNameToSet
	 *            the entityName to set
	 */
	public void setEntityName(final String entityNameToSet) {
		this.entityName = entityNameToSet;
	}

	/**
	 * Gets the entity id.
	 *
	 * @return the entity id
	 */
	protected Long getEntityId() {
		return entityId;
	}

	@Override
	public FDPResponse executePolicy(final FDPRequest fdpRequest) throws ExecutionFailedException {
		return super.executePolicy(fdpRequest);
	}

	@Override
	public Map<AuxRequestParam, Object> getPolicyRuleValues() {
		return super.getPolicyRuleValues();
	}

}
