package com.ericsson.fdp.business.cache.datageneration.service.impl;

import java.util.List;

import javax.annotation.Resource;
import javax.ejb.Stateless;
import javax.inject.Inject;

import com.ericsson.fdp.FDPCacheable;
import com.ericsson.fdp.business.cache.datageneration.PolicyCacheUtil;
import com.ericsson.fdp.business.cache.datageneration.service.PolicyDataService;
import com.ericsson.fdp.business.exception.ExpressionFailedException;
import com.ericsson.fdp.common.enums.ModuleType;
import com.ericsson.fdp.common.exception.FDPServiceException;
import com.ericsson.fdp.common.vo.FDPCircle;
import com.ericsson.fdp.core.dsm.framework.service.FDPCache;
import com.ericsson.fdp.core.policy.Policy;
import com.ericsson.fdp.core.serviceprovisioning.bean.FDPMetaBag;
import com.ericsson.fdp.dao.dto.policy.PolicyDTO;
import com.ericsson.fdp.dao.policy.FDPPolicyDAO;

/**
 * The Class PolicyDataServiceImpl.
 */
@Stateless(mappedName = "policyDataService")
public class PolicyDataServiceImpl implements PolicyDataService {

	/** The fdp cache. */
	@Resource(lookup = "java:app/fdpBusiness-1.0/MetaDataCache")
	private FDPCache<FDPMetaBag, FDPCacheable> fdpCache;

	/** The fdp policy dao. */
	@Inject
	private FDPPolicyDAO fdpPolicyDAO;

	@Override
	public void initializeUpdatePolicyCache() throws FDPServiceException {

		final List<PolicyDTO> policies = fdpPolicyDAO.getAllPolicies();
		for (final PolicyDTO policy : policies) {
			try {
				initializeUpdatePolicy(PolicyCacheUtil.getPolicyForCache(policy), new FDPCircle(-1L, "ALL", "ALL"));
			} catch (InstantiationException | IllegalAccessException | ClassNotFoundException
					| ExpressionFailedException e) {
				throw new FDPServiceException("Could not initialize policy cache ", e);
			}
		}
	}

	/**
	 * Initialize Or updates the policy and its policy rules.
	 *
	 * @param policy
	 *            the policy
	 * @param fdpCircle
	 *            FDPCircle.
	 */
	private void initializeUpdatePolicy(final Policy policy, final FDPCircle fdpCircle) {
		final FDPMetaBag metaBag = new FDPMetaBag(fdpCircle, ModuleType.POLICY, policy.getPolicyName());
		fdpCache.putValue(metaBag, policy);
	}
}
