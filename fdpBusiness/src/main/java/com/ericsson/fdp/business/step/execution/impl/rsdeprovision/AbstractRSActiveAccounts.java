package com.ericsson.fdp.business.step.execution.impl.rsdeprovision;

import java.util.List;
import java.util.Map;

import ch.qos.logback.classic.Logger;

import com.ericsson.fdp.FDPCacheable;
import com.ericsson.fdp.business.product.Product;
import com.ericsson.fdp.business.sharedaccount.ActiveAccountService;
import com.ericsson.fdp.business.util.CommandUtil;
import com.ericsson.fdp.business.util.RequestUtil;
import com.ericsson.fdp.business.vo.FDPActiveServicesVO;
import com.ericsson.fdp.business.vo.ProductAliasCode;
import com.ericsson.fdp.common.constants.FDPConstant;
import com.ericsson.fdp.common.enums.ExternalSystem;
import com.ericsson.fdp.common.enums.ModuleType;
import com.ericsson.fdp.common.enums.Status;
import com.ericsson.fdp.common.vo.FDPCircle;
import com.ericsson.fdp.core.cache.AppCacheSubStore;
import com.ericsson.fdp.core.command.FDPCommand;
import com.ericsson.fdp.core.exception.ExecutionFailedException;
import com.ericsson.fdp.core.logging.FDPLogger;
import com.ericsson.fdp.core.request.FDPRequest;
import com.ericsson.fdp.core.serviceprovisioning.bean.FDPAppBag;
import com.ericsson.fdp.core.serviceprovisioning.bean.FDPMetaBag;
import com.ericsson.fdp.core.utils.ApplicationConfigUtil;
/**
 * This abstract class will contain the all common functions used by all sub classes
 * @author rahul2065
 *
 */
public abstract class AbstractRSActiveAccounts implements ActiveAccountService {
	
	@Override
	public abstract List<FDPActiveServicesVO> executeActiveAccountService(FDPRequest fdpRequest, Object... params) throws ExecutionFailedException;
	
	
	
	public abstract List<FDPActiveServicesVO> getRsDeProvisionList(final FDPRequest fdpRequest, final Logger circleLogger) throws ExecutionFailedException;

	public abstract List<String> getRsDeProvisionList(final FDPRequest fdpRequest) throws ExecutionFailedException;
	
	
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
			final List<FDPActiveServicesVO> activeServicesVO, final String value, final FDPCircle fdpCircle)
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
			activeServicesVO.add(new FDPActiveServicesVO(product, serviceId != null ? serviceId : value));
		}

	}

	/**
	 * This method gets product for Option Type.
	 * 
	 * @param fdpRequest
	 * @param value
	 * @param optionType
	 * @return
	 * @throws ExecutionFailedException
	 */
	protected Product getProductForOption(final FDPRequest fdpRequest, final String value, final Logger circleLogger)
			throws ExecutionFailedException {
		Product product = null;
		String productId = null;
		final String key = (value).trim();
		FDPLogger.debug(circleLogger, getClass(), "getProductForOption()", "Entered with value:" + value + " key:"
				+ key + ", requestId:" + fdpRequest.getRequestId());
		final Object productAlias = ApplicationConfigUtil.getMetaDataCache().getValue(
				new FDPMetaBag(fdpRequest.getCircle(), ModuleType.PRODUCT_ALIAS, ExternalSystem.RS.name()));
		if (productAlias != null && productAlias instanceof ProductAliasCode) {
			final ProductAliasCode productAliasCode = (ProductAliasCode) productAlias;
			productId = productAliasCode.getProductIdForExternalSystemAlias(key);
			if (productId != null) {
				FDPLogger.debug(circleLogger, getClass(), "getProductForOption()", "Got ProductId from cache:"
						+ productId + ", for key:" + key);
				product = RequestUtil.getProductById(fdpRequest, productId);
			}
		}
		return product;
	}

	/**
	 * This method will execute the Command.
	 * 
	 * @param fdpRequest
	 * @param commandName
	 * @return
	 * @throws ExecutionFailedException
	 */
	public FDPCommand isCommandExecuted(final FDPRequest fdpRequest, final String commandName)
			throws ExecutionFailedException {
		FDPCommand fdpCommand = fdpRequest.getExecutedCommand(commandName);
		if (null == fdpCommand) {
			fdpCommand = getCommand(fdpRequest, commandName);
			if (null != fdpCommand && fdpCommand.execute(fdpRequest).equals(Status.SUCCESS)) {
				fdpRequest.addExecutedCommand(fdpCommand);
			} else {
				throw new ExecutionFailedException("Could not execute command " + fdpCommand);
			}
		}
		return fdpCommand;
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
			fdpCommand = CommandUtil.getExectuableFDPCommand(fdpCommand);
		}
		return fdpCommand;
	}

}
