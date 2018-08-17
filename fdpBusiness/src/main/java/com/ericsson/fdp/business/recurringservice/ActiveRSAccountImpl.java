package com.ericsson.fdp.business.recurringservice;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.ejb.Stateless;
import javax.inject.Inject;

import org.apache.commons.collections.CollectionUtils;

import ch.qos.logback.classic.Logger;

import com.ericsson.fdp.business.command.param.impl.CommandParamOutput;
import com.ericsson.fdp.business.constants.BusinessConstants;
import com.ericsson.fdp.business.product.Product;
import com.ericsson.fdp.business.step.execution.impl.rsdeprovision.AbstractRSActiveAccounts;
import com.ericsson.fdp.business.util.LoggerUtil;
import com.ericsson.fdp.business.util.RequestUtil;
import com.ericsson.fdp.business.vo.FDPActiveServicesVO;
import com.ericsson.fdp.common.constants.FDPConstant;
import com.ericsson.fdp.common.enums.ChannelType;
import com.ericsson.fdp.common.util.StringUtil;
import com.ericsson.fdp.common.vo.FDPCCAdminVO;
import com.ericsson.fdp.common.vo.FDPCircle;
import com.ericsson.fdp.core.cache.AppCacheSubStore;
import com.ericsson.fdp.core.command.FDPCommand;
import com.ericsson.fdp.core.dsm.framework.service.FDPCache;
import com.ericsson.fdp.core.exception.ExecutionFailedException;
import com.ericsson.fdp.core.logging.FDPLogger;
import com.ericsson.fdp.core.request.FDPRequest;
import com.ericsson.fdp.core.serviceprovisioning.bean.FDPAppBag;
import com.ericsson.fdp.core.utils.ApplicationConfigUtil;
import com.ericsson.fdp.dao.dto.FDPConfigurationDTO;
import com.ericsson.fdp.dao.enums.appcache.AdminConfigurations;
import com.ericsson.fdp.dao.fdpadmin.FDPConfigurationDAO;

/**
 * This Stateless bean is used for CC web active Rs products list fetching.
 * 
 * 
 */
@Stateless
public class ActiveRSAccountImpl extends AbstractRSActiveAccounts implements ActiveAccount {


	/** The configuration dao. */
	@Inject
	private FDPConfigurationDAO configurationDAO;

	public static final String COMMON_PATH = "servicesDtls.service.";

	@Override
	public List<FDPCCAdminVO> executeActiveAccountService(final String msisdn) throws ExecutionFailedException {
		FDPRequest fdpRequest = RequestUtil.createFDPRequest(msisdn, ChannelType.WEB);
		List<FDPActiveServicesVO> fdpActiveServicesVOs = executeActiveAccountService(fdpRequest);
		List<FDPCCAdminVO> fdpCCAdminVOs = convertToFDPCCAdminVO(fdpActiveServicesVOs);
		return fdpCCAdminVOs;
	}

	@Override
	public List<FDPActiveServicesVO> executeActiveAccountService(final FDPRequest fdpRequest, final Object... params)
			throws ExecutionFailedException {
		final Logger circleLogger = LoggerUtil.getSummaryLoggerFromRequest(fdpRequest);
		return getRsDeProvisionList(fdpRequest, circleLogger);
	}

	/**
	 * Get RS Products by executing a getRSDTLS command
	 */
	@Override
	public List<FDPActiveServicesVO> getRsDeProvisionList(final FDPRequest fdpRequest, final Logger circleLogger)
			throws ExecutionFailedException {
		final List<FDPActiveServicesVO> activeServicesVOs = new ArrayList<FDPActiveServicesVO>();
		final String commandName = this.getRSServiceDetailsCommandName();
		FDPLogger.debug(circleLogger, getClass(), "getRsDeProvisionList()", "Processing for commandName:" + commandName
				+ ", requestId:" + fdpRequest.getRequestId());
		final FDPCommand fdpCommand = isCommandExecuted(fdpRequest, commandName);
		if (fdpCommand != null) {
			setRSOutputParameters(fdpCommand, activeServicesVOs);
			FDPLogger.debug(circleLogger, getClass(), "getRsDeProvisionList()",
					", requestId:" + fdpRequest.getRequestId());
			if (CollectionUtils.isNotEmpty(activeServicesVOs)) {
				for (FDPActiveServicesVO fdpActiveServicesVO : activeServicesVOs) {
					addUserValuesToOutputList(fdpRequest, circleLogger, fdpActiveServicesVO,
							fdpActiveServicesVO.getServiceId(), fdpRequest.getCircle());
				}
			}
		}

		return activeServicesVOs;
	}

	private String getRSServiceDetailsCommandName() {
		FDPCache<FDPAppBag, Object> applicationConfigCache;
		try {
			applicationConfigCache = ApplicationConfigUtil
					.getApplicationConfigCache();
		} catch (ExecutionFailedException e) {
			return getRSServiceDetailsCommandNameFromOtherSources();
		}
		final String key = AdminConfigurations.RS_SERVICE_DETAILS_COMMAND_NAME.getKey();
		final FDPAppBag appBag = new FDPAppBag();
		appBag.setSubStore(AppCacheSubStore.CONFIGURATION_MAP);
		appBag.setKey(key);
		final String commandName = (String) applicationConfigCache.getValue(appBag);

		if (StringUtil.isNullOrEmpty(commandName)) {
			return getRSServiceDetailsCommandNameFromOtherSources();
		} else {
			return commandName;
		}
	}

	private String getRSServiceDetailsCommandNameFromOtherSources() {

		FDPConfigurationDTO configuration = configurationDAO
				.getFDPConfigurationByName(AdminConfigurations.RS_SERVICE_DETAILS_COMMAND_NAME.getKey());

		if (StringUtil.isNullOrEmpty(configuration.getAttributeValue())) {
			return FDPConstant.GET_SERVICE_DTLS_REQUEST_COMMAND;
		} else {
			return configuration.getAttributeValue();
		}
	}

	/**
	 * This method checks for each user values to be map with cached values, if
	 * found then add to the response list.
	 * 
	 * @param deltaPeriod
	 * @param fdpRequest
	 * @param circleLogger
	 * @param activeServicesVO
	 * @param parseDatePattern
	 * @param optionValues
	 * @param value
	 * @throws ExecutionFailedException
	 */
	protected void addUserValuesToOutputList(final FDPRequest fdpRequest, final Logger circleLogger,
			final FDPActiveServicesVO activeServicesVO, final String value, final FDPCircle fdpCircle)
			throws ExecutionFailedException {

		final Product product = getProductForOption(fdpRequest, value, circleLogger);
		if (product != null) {

			final FDPCircle fdpCircleWithCSAttr = (FDPCircle) ApplicationConfigUtil.getApplicationConfigCache()
					.getValue(new FDPAppBag(AppCacheSubStore.CS_ATTRIBUTES, fdpCircle.getCircleCode()));
			final Map<String, Map<String, String>> csAttrMap = fdpCircleWithCSAttr.getCsAttributesKeyValueMap();
			final Map<String, String> csAttrClass = csAttrMap.get(FDPConstant.RS_SERVICE_ID);
			String serviceId = value;
			if (csAttrClass != null) {
				serviceId = csAttrClass.get(value);
			}
			activeServicesVO.setProduct(product);
			activeServicesVO.setServiceId(serviceId != null ? serviceId : value);
		}
	}

	/**
	 * This method will extract the output parameters of command
	 * <command>GetServicesDtlsRequestTwo</command>
	 * 
	 * @param fdpCommand
	 * @param activeServicesVO
	 * @return
	 */
	protected void setRSOutputParameters(final FDPCommand fdpCommand, final List<FDPActiveServicesVO> activeServicesVO) {
		int i = 0;
		boolean valueFound = false;
		while (!valueFound) {
			FDPActiveServicesVO fdpActiveServicesVO = new FDPActiveServicesVO();
			final String usageValuePath = COMMON_PATH + i + BusinessConstants.DOT + "serviceId";
			final Object param = fdpCommand.getOutputParam(usageValuePath);
			if (param == null) {
				valueFound = true;
				break;
			} else if (param instanceof CommandParamOutput) {
				fdpActiveServicesVO.setServiceId(((CommandParamOutput) param).getValue().toString());
			}
			fdpActiveServicesVO.setActivationDate(getParamValueByIndex(fdpCommand, i, "activationDate"));
			fdpActiveServicesVO.setLastRenewalDate(getParamValueByIndex(fdpCommand, i, "lastRenewalDate"));
			fdpActiveServicesVO.setNextRenewalDate(getParamValueByIndex(fdpCommand, i, "renewalDate"));
			fdpActiveServicesVO.setRenewalPeriod(getParamValueByIndex(fdpCommand, i, "renewalCount"));
			activeServicesVO.add(fdpActiveServicesVO);
			i++;
		}
	}

	/**
	 * This is a helper method for fetching the output value
	 * 
	 * @param fdpCommand
	 * @param i
	 * @param CommandName
	 * @return
	 */
	private String getParamValueByIndex(final FDPCommand fdpCommand, final int i, final String CommandName) {
		String outputParamValue = null;
		final String usageValuePath = COMMON_PATH + i + BusinessConstants.DOT + CommandName;
		final Object param = fdpCommand.getOutputParam(usageValuePath);
		if (param instanceof CommandParamOutput) {
			outputParamValue = ((CommandParamOutput) param).getValue().toString();
		}
		return outputParamValue;
	}

	/**
	 * This method will polpulate the <code>FDPCCAdminVO</code> from
	 * <code>FDPActiveServicesVO</code>
	 * 
	 * @param fdpActiveServicesVOs
	 * @return
	 */
	private List<FDPCCAdminVO> convertToFDPCCAdminVO(final List<FDPActiveServicesVO> fdpActiveServicesVOs) {
		List<FDPCCAdminVO> fdpCCAdminVOs = new ArrayList<FDPCCAdminVO>();
		if (CollectionUtils.isNotEmpty(fdpActiveServicesVOs)) {
			for (FDPActiveServicesVO fdpServiceVO : fdpActiveServicesVOs) {
				FDPCCAdminVO fdpCCAdminVO = new FDPCCAdminVO();
				fdpCCAdminVO.setProductId(fdpServiceVO.getProduct().getProductId());
				fdpCCAdminVO.setProductName(fdpServiceVO.getProduct().getProductName());
				fdpCCAdminVO.setActivationDate(fdpServiceVO.getActivationDate());
				fdpCCAdminVO.setRenewalPeriod(fdpServiceVO.getRenewalPeriod());
				fdpCCAdminVO.setNextRenewalDate(fdpServiceVO.getNextRenewalDate());
				fdpCCAdminVO.setLastRenewalDate(fdpServiceVO.getLastRenewalDate());
				fdpCCAdminVO.setProductType(ActiveProductType.RS.getProductType());
				fdpCCAdminVOs.add(fdpCCAdminVO);
			}
		}
		return fdpCCAdminVOs;
	}

	@Override
	public List<String> getRsDeProvisionList(FDPRequest fdpRequest)
			throws ExecutionFailedException {
		// TODO Auto-generated method stub
		return null;
	}

}
