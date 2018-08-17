package com.ericsson.fdp.business.step.execution.impl.rsdeprovision;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.ejb.Stateless;

import ch.qos.logback.classic.Logger;

import com.ericsson.fdp.business.command.param.impl.CommandParamOutput;
import com.ericsson.fdp.business.enums.AuxRequestParam;
import com.ericsson.fdp.business.enums.RequestMetaValuesKey;
import com.ericsson.fdp.business.product.Product;
import com.ericsson.fdp.business.util.LoggerUtil;
import com.ericsson.fdp.business.util.RequestUtil;
import com.ericsson.fdp.business.vo.FDPActiveServicesVO;
import com.ericsson.fdp.common.constants.FDPConstant;
import com.ericsson.fdp.core.command.FDPCommand;
import com.ericsson.fdp.core.exception.ExecutionFailedException;
import com.ericsson.fdp.core.logging.FDPLogger;
import com.ericsson.fdp.core.node.FDPNode;
import com.ericsson.fdp.core.request.FDPRequest;
import com.ericsson.fdp.core.request.impl.FDPSMPPRequestImpl;

/**
 * This class is used for RS-De-provisioning Service.
 * 
 * @author Ericsson
 * 
 */
@Stateless
public class RSDeprovisionServiceImpl extends AbstractRSActiveAccounts {

	@Override
	public List<FDPActiveServicesVO> executeActiveAccountService(final FDPRequest fdpRequest,
			final Object... additionalInformations) throws ExecutionFailedException {
		final Logger circleLogger = LoggerUtil.getSummaryLoggerFromRequest(fdpRequest);
		FDPLogger.debug(circleLogger, getClass(), "executeService()",
				"Executing Service for requestId:" + fdpRequest.getRequestId());
		final FDPNode fdpNode = (FDPNode) fdpRequest.getValueFromRequest(RequestMetaValuesKey.NODE);
		if ((null != fdpNode)) {
			return getRsDeProvisionList(fdpRequest, circleLogger);
		} else {
			throw new ExecutionFailedException("Node not found in Request");
		}
	}

	public List<FDPActiveServicesVO> getRsDeProvisionList(final FDPRequest fdpRequest, final Logger circleLogger)
			throws ExecutionFailedException {
		final List<FDPActiveServicesVO> activeServicesVO = new ArrayList<FDPActiveServicesVO>();
		final String commandName = FDPConstant.GET_SERVICE_DTLS_REQUEST_COMMAND;
		FDPLogger.debug(circleLogger, getClass(), "getRsDeProvisionList()", "Processing for commandName:" + commandName
				+ ", requestId:" + fdpRequest.getRequestId());
		final FDPCommand fdpCommand = isCommandExecuted(fdpRequest, commandName);
		if (fdpCommand != null) {
			final List<String> valuesFromUser = getRSServiceId(fdpCommand);
			FDPLogger.debug(circleLogger, getClass(), "getRsDeProvisionList()", "Got userValues" + valuesFromUser
					+ " for optionType:" + valuesFromUser + ", requestId:" + fdpRequest.getRequestId());
			for (final String value : valuesFromUser) {
				addUserValuesToOutputList(fdpRequest, circleLogger, activeServicesVO, value, fdpRequest.getCircle());
			}
		}
		return activeServicesVO;
	}

	
	protected List<String> getRSServiceId(final FDPCommand fdpCommand) {
		int i = 0;
		boolean valueFound = false;
		final List<String> serviceId = new ArrayList<String>();
		while (!valueFound) {
			final String usageValuePath = "servicesDtls.service." + i + ".serviceId";
			final Object param = fdpCommand.getOutputParam(usageValuePath);
			if (param == null) {
				valueFound = true;
			} else if (param instanceof CommandParamOutput) {
				serviceId.add(((CommandParamOutput) param).getValue().toString());
			}
			i++;
		}
		return serviceId;
	}
	

	public List<String> getRsDeProvisionList(final FDPRequest fdpRequest)
			throws ExecutionFailedException {
	
		final List<String> productName = new ArrayList<String>();
		Map<String, Long> productMappingMap;
		final String commandName = FDPConstant.GET_SERVICE_DTLS_REQUEST_COMMAND;
	
		//Set Aux Request Param
		String input = ((FDPSMPPRequestImpl)fdpRequest).getRequestString();
		if(input.equals(FDPConstant.ONE+""))
			input = FDPConstant.ADHOC;
		else
			input = FDPConstant.RECURRING;
		((FDPSMPPRequestImpl)fdpRequest).putAuxiliaryRequestParameter(AuxRequestParam.provisioningType, input);
		((FDPSMPPRequestImpl)fdpRequest).putAuxiliaryRequestParameter(AuxRequestParam.distinctProduct, FDPConstant.TRUE);
		
		final FDPCommand fdpCommand = isCommandExecuted(fdpRequest, commandName);
		if (fdpCommand != null) {
			int i = 0;
			boolean valueFound = false;
			
			String productIdFound = null;
			Product product = null;
			while (!valueFound) {
				final String usageValuePath = "servicesDtls." + i + ".productId";
				final Object param = fdpCommand.getOutputParam(usageValuePath);
				if (param == null) {
					valueFound = true;
				} else if (param instanceof CommandParamOutput) {
					productIdFound = ((CommandParamOutput) param).getValue().toString();
					product = RequestUtil.getProductById(fdpRequest, productIdFound);
					productName.add(product.getProductName());
					
					Object object = ((FDPSMPPRequestImpl)fdpRequest).getAuxiliaryRequestParameter(AuxRequestParam.productMap);
					if(null==object) {
						productMappingMap = new HashMap<String, Long>();
					}
					else 
						productMappingMap =  (Map<String, Long>) object;
					productMappingMap.put((i+1)+"", Long.parseLong(productIdFound));
					((FDPSMPPRequestImpl)fdpRequest).putAuxiliaryRequestParameter(AuxRequestParam.productMap, productMappingMap);
					
				}
				i++;
			}
		}
		return productName;
	}
	

	}
