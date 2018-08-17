package com.ericsson.fdp.business.policy.policyrule.impl;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.naming.NamingException;

import com.ericsson.fdp.business.batchjob.pam.service.PAMActiveServices;
import com.ericsson.fdp.business.display.impl.DisplayObjectImpl;
import com.ericsson.fdp.business.enums.AuxRequestParam;
import com.ericsson.fdp.business.enums.RequestMetaValuesKey;
import com.ericsson.fdp.business.exception.NotificationFailedException;
import com.ericsson.fdp.business.node.impl.PAMDeprovisioningRequestNode;
import com.ericsson.fdp.business.util.ActiveServicesRequestUtil;
import com.ericsson.fdp.business.util.RequestUtil;
import com.ericsson.fdp.business.util.ResponseUtil;
import com.ericsson.fdp.business.vo.FDPPamActiveServicesVO;
import com.ericsson.fdp.common.constants.FDPConstant;
import com.ericsson.fdp.common.vo.FDPCircle;
import com.ericsson.fdp.core.cache.AppCacheSubStore;
import com.ericsson.fdp.core.exception.ExecutionFailedException;
import com.ericsson.fdp.core.node.FDPNode;
import com.ericsson.fdp.core.request.FDPRequest;
import com.ericsson.fdp.core.request.FDPResponse;
import com.ericsson.fdp.core.serviceprovisioning.bean.FDPAppBag;
import com.ericsson.fdp.core.utils.ApplicationConfigUtil;
import com.ericsson.fdp.dao.enums.DBActionClassName;

/**
 * The Class PAMActiveServicePolicyRuleImpl.
 */
public class PAMActiveServicePolicyRuleImpl extends ActiveServicePolicyRuleImpl {

	/** The Constant serialVersionUID. */
	private static final long serialVersionUID = 401847752660853085L;

	/** The ejb lookup name. */
	private final String ejbLookupName = DBActionClassName.PAM_DE_PROVISIONING_SERVICE.getLookUpClass();


	@Override
	public final FDPResponse displayRule(final FDPRequest fdpRequest) throws ExecutionFailedException {
		if (ejbLookupName == null) {
			throw new ExecutionFailedException("Could not find ejb class for execution" + ejbLookupName);
		}
		try {
			final Object beanObject = ApplicationConfigUtil.getBean(ejbLookupName);
			List<FDPPamActiveServicesVO> fdpPamActServVO = null;
			if (null != beanObject && (beanObject instanceof PAMActiveServices)) {
				final PAMActiveServices pamAccountService = (PAMActiveServices) beanObject;
				fdpPamActServVO = pamAccountService.getProductsFromPamServicesID(fdpRequest);
			} else {
				throw new ExecutionFailedException("Can't Locate Service" + beanObject);
			}
			return (fdpPamActServVO == null || fdpPamActServVO.isEmpty()) ? getNoPamActiveServiceResponse(fdpRequest)
					: createNodesFromOutput(fdpPamActServVO, fdpRequest);
		} catch (final NamingException e) {
			throw new ExecutionFailedException("The bean could not be found " + ejbLookupName, e);
		}
	}

	/**
	 * Creates the nodes from output.
	 * 
	 * @param fdpPamActServVOs
	 *            the fdp pam active services v os
	 * @param fdpRequest
	 *            the fdp request
	 * @return the fDP response
	 * @throws ExecutionFailedException
	 *             the execution failed exception
	 */
	private FDPResponse createNodesFromOutput(final List<FDPPamActiveServicesVO> fdpPamActServVOs,
			final FDPRequest fdpRequest) throws ExecutionFailedException {
		List<FDPNode> fdpNodeList = null;
		if (fdpPamActServVOs != null && !fdpPamActServVOs.isEmpty()) {
			fdpNodeList = new ArrayList<FDPNode>();
			try {
				for (final FDPPamActiveServicesVO fdpPamActServVO : fdpPamActServVOs) {
					RequestUtil.putAuxiliaryValueInRequest(fdpRequest, AuxRequestParam.PAMIDCSNAME,
							getValueForPAMID(fdpRequest, fdpPamActServVO.getPamId()));
					final PAMDeprovisioningRequestNode pamProvReqNode = new PAMDeprovisioningRequestNode(
							ActiveServicesRequestUtil.createNotificationText(fdpRequest, fdpPamActServVO), null,
							((FDPNode) fdpRequest.getValueFromRequest(RequestMetaValuesKey.NODE))
									.getFullyQualifiedPath(), fdpRequest.getChannel(), fdpRequest.getCircle(), null,
							null, null, fdpPamActServVO.getProduct().getProductId(), null);
					pamProvReqNode.setPolicyName(FDPConstant.PAM_DEPORV_POLICY);
					fdpNodeList.add(pamProvReqNode);
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
	 * Gets the value for pamid.
	 * 
	 * @param fdpRequest
	 *            the fdp request
	 * @param pamId
	 *            the pam id
	 * @return the value for pamid
	 * @throws ExecutionFailedException
	 *             the execution failed exception
	 */
	private String getValueForPAMID(final FDPRequest fdpRequest, final String pamId) throws ExecutionFailedException {
		String pamCsAttribute = null;
		final FDPAppBag appBag = new FDPAppBag();
		appBag.setKey(fdpRequest.getCircle().getCircleCode());
		appBag.setSubStore(AppCacheSubStore.CS_ATTRIBUTES);
		final Object obj = ApplicationConfigUtil.getApplicationConfigCache().getValue(appBag);
		if (obj != null) {
			final FDPCircle fdpCircle = (FDPCircle) obj;
			Map<String, Map<String, String>> csAttributesKeyValue = fdpCircle.getCsAttributesKeyValueMap();
			if (csAttributesKeyValue != null && !csAttributesKeyValue.isEmpty()) {
				Map<String, String> pamKeyValueMap = csAttributesKeyValue.get(FDPConstant.CS_ATTRIBUTE_PAM);
				if (pamKeyValueMap != null && !pamKeyValueMap.isEmpty()) {
					pamCsAttribute = pamKeyValueMap.get(pamId);
				}
			}
		}
		return (pamCsAttribute == null)? pamId :pamCsAttribute;
	}

}
