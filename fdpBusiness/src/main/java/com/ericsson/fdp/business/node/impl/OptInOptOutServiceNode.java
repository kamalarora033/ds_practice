package com.ericsson.fdp.business.node.impl;

import java.util.List;

import com.ericsson.fdp.FDPCacheable;
import com.ericsson.fdp.business.enums.RequestMetaValuesKey;
import com.ericsson.fdp.business.enums.State;
import com.ericsson.fdp.business.node.FDPCustomNode;
import com.ericsson.fdp.business.product.Product;
import com.ericsson.fdp.business.serviceprovisioning.rule.ServiceProvisioningRule;
import com.ericsson.fdp.common.enums.ChannelType;
import com.ericsson.fdp.common.enums.Visibility;
import com.ericsson.fdp.common.vo.FDPCircle;
import com.ericsson.fdp.core.exception.ExecutionFailedException;
import com.ericsson.fdp.core.node.FDPNode;
import com.ericsson.fdp.core.request.FDPRequest;
import com.ericsson.fdp.core.request.impl.FDPRequestImpl;
import com.ericsson.fdp.dao.enums.FDPServiceProvSubType;

/**
 * Class for Opt In Opt Out Service.
 * 
 * @author eashtod
 *
 */
public class OptInOptOutServiceNode extends SpecialMenuNode implements FDPCustomNode{

	/**
	 * Class Serial Version UID.
	 */
	private static final long serialVersionUID = 1L;
	
	private ServiceProvisioningRule provisioningRule;
	
	private Product product; 
	
	public OptInOptOutServiceNode(final String displayNameToSet, final String shortCodeToSet,
			final String fullyQualifiedPathToSet, final ChannelType channelToSet, final FDPCircle circleToSet,
			final Long priorityToSet, final FDPNode parentToSet, final List<FDPNode> childrenToSet,
			final Long entityIdToSet, final String entityNameToSet, final FDPServiceProvSubType fdpServiceProvSubType, final ServiceProvisioningRule provisioningRule,final Product product) {
		super(displayNameToSet, shortCodeToSet, fullyQualifiedPathToSet,
				channelToSet, circleToSet, priorityToSet, parentToSet, childrenToSet, entityIdToSet, entityNameToSet
				,fdpServiceProvSubType,null,Visibility.VISIBLE_FOR_MENU,State.ACTIVE_FOR_MARKET);
		this.provisioningRule = provisioningRule;
		this.product = product;
	}

	@Override
	public FDPCacheable getServiceProvisioning(FDPRequest fdpRequest) throws ExecutionFailedException {
		/*return ApplicationConfigUtil.getMetaDataCache().getValue(
				new FDPMetaBag(fdpRequest.getCircle(), ModuleType.SP_PRODUCT, getSPId(fdpRequest)));*/
		((FDPRequestImpl)fdpRequest).addMetaValue(RequestMetaValuesKey.PRODUCT,this.product);
		return this.provisioningRule;
	}
	
/*	private String getSPId(final FDPRequest fdpRequest) {
		return ServiceProvDataConverterUtil.getKeyForServiceProvMetaBag(getEntityId(), FDPServiceProvType.PRODUCT,
				getSpecialMenuNodeType());
	}*/

}
