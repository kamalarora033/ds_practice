package com.ericsson.fdp.business.menu.impl;

import javax.annotation.Resource;
import javax.ejb.Stateless;

import com.ericsson.fdp.FDPCacheable;
import com.ericsson.fdp.business.menu.FDPDynamicMenuAliasCode;
import com.ericsson.fdp.business.util.RequestUtil;
import com.ericsson.fdp.business.vo.NodeAliasCode;
import com.ericsson.fdp.common.constants.JNDILookupConstant;
import com.ericsson.fdp.common.enums.ModuleType;
import com.ericsson.fdp.common.vo.FDPCircle;
import com.ericsson.fdp.core.dsm.framework.service.FDPCache;
import com.ericsson.fdp.core.exception.ExecutionFailedException;
import com.ericsson.fdp.core.node.FDPNode;
import com.ericsson.fdp.core.request.FDPRequest;
import com.ericsson.fdp.core.request.FDPSMPPRequest;
import com.ericsson.fdp.core.serviceprovisioning.bean.FDPMetaBag;
import com.ericsson.fdp.core.utils.ApplicationConfigUtil;

/**
 * The Class FDPDynamicMenuAliasCodeImpl.
 */
@Stateless
public class FDPDynamicMenuAliasCodeImpl implements FDPDynamicMenuAliasCode {

	/** The meta data cache. */
	@Resource(lookup = JNDILookupConstant.META_DATA_CACHE_JNDI_NAME)
	private FDPCache<FDPMetaBag, FDPCacheable> metaDataCache;

	@Override
	public FDPNode getFDPDynamicMenuAliasCode(FDPRequest fdpRequest) throws ExecutionFailedException {
		FDPCircle fdpCircle = fdpRequest.getCircle();
		ModuleType moduleType = ModuleType.DYNAMIC_MENU_CODE_ALIAS;
		FDPNode fdpNode = null;
		if (fdpRequest instanceof FDPSMPPRequest) {
			FDPSMPPRequest fdpSmppReq = (FDPSMPPRequest) fdpRequest;
			String requestString = fdpSmppReq.getRequestString();

			if ("SMS".equalsIgnoreCase(fdpRequest.getChannel().toString())) {
				requestString = requestString.toLowerCase();
			}

			FDPMetaBag fdpMetaBag = new FDPMetaBag(fdpCircle, moduleType, requestString);

			FDPCacheable fdpCacheableObject = metaDataCache.getValue(fdpMetaBag);
			if (fdpCacheableObject != null) {
				NodeAliasCode nodeAlaisCode = (NodeAliasCode) fdpCacheableObject;
				String actualNodeString = nodeAlaisCode.getActualCode();
				FDPCacheable fdpCacheable = ApplicationConfigUtil.getMetaDataCache().getValue(
						new FDPMetaBag(fdpCircle, ModuleType.DM, actualNodeString));
				fdpNode = RequestUtil.checkInstanceForNode(fdpCacheable);
			}
		}
		return fdpNode;
	}

}
