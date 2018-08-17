package com.ericsson.fdp.business.batchjob.pam.service.impl;

import java.util.ArrayList;
import java.util.List;

import javax.annotation.Resource;

import ch.qos.logback.classic.Logger;

import com.ericsson.fdp.FDPCacheable;
import com.ericsson.fdp.business.batchjob.pam.service.PAMActiveServices;
import com.ericsson.fdp.business.constants.FDPCommandConstants;
import com.ericsson.fdp.business.product.Product;
import com.ericsson.fdp.business.util.LoggerUtil;
import com.ericsson.fdp.business.util.RequestUtil;
import com.ericsson.fdp.business.vo.FDPPamActiveServicesVO;
import com.ericsson.fdp.business.vo.ProductAliasCode;
import com.ericsson.fdp.common.constants.FDPConstant;
import com.ericsson.fdp.common.enums.ExternalSystem;
import com.ericsson.fdp.common.enums.ModuleType;
import com.ericsson.fdp.core.command.FDPCommand;
import com.ericsson.fdp.core.command.param.CommandParam;
import com.ericsson.fdp.core.dsm.framework.service.FDPCache;
import com.ericsson.fdp.core.exception.ExecutionFailedException;
import com.ericsson.fdp.core.logging.FDPLogger;
import com.ericsson.fdp.core.request.FDPRequest;
import com.ericsson.fdp.core.serviceprovisioning.bean.FDPMetaBag;
import com.ericsson.fdp.core.utils.ApplicationConfigUtil;

public abstract class AbstractPAMActiveAccounts implements PAMActiveServices {

	/** The fdp cache. */
	@Resource(lookup = "java:app/fdpBusiness-1.0/MetaDataCache")
	private FDPCache<FDPMetaBag, FDPCacheable> fdpCache;

	/**
	 * The circle logger.
	 */
	private Logger circleLogger = null;

	/**
	 * This method is sued to get the circle logger.
	 * 
	 * @param fdpRequest
	 *            the request to get the circle logger.
	 * @return the circle logger.
	 */
	protected Logger getCircleLogger(final FDPRequest fdpRequest) {
		if (circleLogger == null) {
			circleLogger = LoggerUtil.getSummaryLoggerFromRequest(fdpRequest);
		}
		return circleLogger;
	}

	/**
	 * Gets the PAM Ids.
	 * 
	 * @param fdpRequest
	 *            the fdp request
	 * @param commandGetAccountDetails
	 *            the command get account details
	 * @return the pAMI ds
	 */
	protected List<String> getPAMIDFromCommandOutputParam(final FDPRequest fdpRequest,
			final FDPCommand commandGetAccountDetails) {
		List<String> commandPamIdList = new ArrayList<String>();
		int index = 0;
		CommandParam value = null;
		do {
			// paminformationlist.0.pamserviceid
			String fullyQualifiedPath = FDPCommandConstants.GET_ACCOUNT_DETAILS_PAM_ID
					+ FDPConstant.PARAMETER_SEPARATOR + index + FDPConstant.PARAMETER_SEPARATOR + "pamServiceID";
			value = commandGetAccountDetails.getOutputParam(fullyQualifiedPath);
			if (value != null && value.getValue() != null) {
				String pamID = value.getValue().toString();
				FDPLogger.debug(
						getCircleLogger(fdpRequest),
						getClass(),
						"getPAMIDFromCommandOutputParam()",
						LoggerUtil.getRequestAppender(fdpRequest) + "The PAM for Susbscriber: "
								+ fdpRequest.getSubscriberNumber() + " is " + pamID);
				commandPamIdList.add(value.getValue().toString());
			}
			index++;
		} while (value != null);
		return commandPamIdList;
	}

	/**
	 * Gets the product id from pam id.
	 * 
	 * @param fdpRequest
	 *            the fdp request
	 * @param pamIds
	 *            the pam ids
	 * @return the product id from pam id
	 * @throws ExecutionFailedException
	 *             the execution failed exception
	 */
	protected List<FDPPamActiveServicesVO> getProductIDFromPAMId(final FDPRequest fdpRequest, final List<String> pamIds)
			throws ExecutionFailedException {
		List<FDPPamActiveServicesVO> fdpPamActiveServicesVOs = new ArrayList<FDPPamActiveServicesVO>();
		// TODO this need to checking as per confirmation from Lakhwinder that
		// how he is putting data in cache.
		// FDPMetaBag metaBag = new FDPMetaBag(fdpRequest.getCircle(),
		// ModuleType.PRODUCT_ALIAS, ProductAdditionalInfoEnum.PAM_ID.name());
		FDPMetaBag metaBag = new FDPMetaBag(fdpRequest.getCircle(), ModuleType.PRODUCT_ALIAS, ExternalSystem.AIR.name());

		FDPCacheable fdpCacheable = fdpCache.getValue(metaBag);
		if (fdpCacheable instanceof ProductAliasCode) {
			ProductAliasCode productAliasCode = (ProductAliasCode) fdpCacheable;
			for (String pamdId : pamIds) {
				String productId = productAliasCode.getProductIdForExternalSystemAlias(FDPConstant.PAM_ID_INCACHE
						+ pamdId);
				if (productId != null) {
					Product product = RequestUtil.getProductById(fdpRequest, productId);
					if (product != null) {
						FDPPamActiveServicesVO fdpPamActiveServicesVO = new FDPPamActiveServicesVO(product, pamdId);
						fdpPamActiveServicesVOs.add(fdpPamActiveServicesVO);

						FDPLogger.debug(getCircleLogger(fdpRequest), getClass(), "getProductIDFromPAMId()",
								LoggerUtil.getRequestAppender(fdpRequest) + " FDPPamActiveServicesVO: for msisdn "
										+ fdpRequest.getSubscriberNumber() + " is " + fdpPamActiveServicesVO);
					}
				}
			}
		}
		return fdpPamActiveServicesVOs;
	}
	
	/**
	 * This method fetches command from Cache if not executed.
	 * 
	 * @param fdpRequest
	 * @param commandName
	 * @return
	 * @throws ExecutionFailedException
	 */
	protected FDPCommand getCommand(final FDPRequest fdpRequest, final String commandName)
			throws ExecutionFailedException {
		FDPCommand fdpCommand = null;
		final FDPCacheable cachedCommand = ApplicationConfigUtil.getMetaDataCache().getValue(
				new FDPMetaBag(fdpRequest.getCircle(), ModuleType.COMMAND, commandName));
		if (cachedCommand instanceof FDPCommand) {
			fdpCommand = (FDPCommand) cachedCommand;
		}
		return fdpCommand;
	}

}
