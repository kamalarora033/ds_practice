package com.ericsson.fdp.business.util;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.List;
import java.util.Map;

import ch.qos.logback.classic.Logger;

import com.ericsson.fdp.FDPCacheable;
import com.ericsson.fdp.business.command.AbstractCommand;
import com.ericsson.fdp.business.enums.AuxRequestParam;
import com.ericsson.fdp.business.enums.Command;
import com.ericsson.fdp.business.enums.ConfigurationKey;
import com.ericsson.fdp.business.enums.RequestMetaValuesKey;
import com.ericsson.fdp.business.expression.Expression;
import com.ericsson.fdp.business.node.impl.ProductNode;
import com.ericsson.fdp.business.product.Product;
import com.ericsson.fdp.common.constants.FDPConstant;
import com.ericsson.fdp.common.constants.FDPLoggerConstants;
import com.ericsson.fdp.common.enums.ChannelType;
import com.ericsson.fdp.common.enums.ProductType;
import com.ericsson.fdp.core.command.FDPCommand;
import com.ericsson.fdp.core.command.param.CommandParam;
import com.ericsson.fdp.core.config.utils.PropertyUtils;
import com.ericsson.fdp.core.logging.FDPLogger;
import com.ericsson.fdp.core.node.FDPNode;
import com.ericsson.fdp.core.request.FDPRequest;
import com.ericsson.fdp.core.request.FDPSMPPRequest;
import com.ericsson.fdp.core.request.impl.FDPSMPPRequestImpl;
import com.ericsson.fdp.dao.enums.CommandExecutionType;
import com.ericsson.fdp.dao.enums.ExternalSystem;

public class PostLogsUtil {

	private static final String evdsType = PropertyUtils.getProperty("evds.protocol.type");
	
	/**
	 * This method returns the constraints attached to the requested product
	 * @param fdpRequest
	 * @return
	 */
	public static String getProductConstraint(FDPRequest fdpRequest, Product product){
		String productConstraints = FDPLoggerConstants.NOT_APPLICABLE;
		if(null != product){
			Expression expression = product.getConstraintForChannel(fdpRequest.getChannel());
			expression = (null == expression && !ChannelType.USSD.equals(fdpRequest.getChannel())) ? product.getConstraintForChannel(ChannelType.WEB)
					: expression;
			productConstraints = (null != expression)? expression.toString() : productConstraints;
		}
		return productConstraints;
	}
	
	/**
	 * This method will return the refill id attached during service provisioning
	 * @param fdpRequest
	 * @return
	 */
	public static String getRefillProfileId(FDPRequest fdpRequest){
		String refillProfileId = FDPLoggerConstants.NOT_APPLICABLE;
		if(null != fdpRequest.getExecutedCommand(Command.REFILL.getCommandDisplayName())){
			FDPCommand fdpCommand = fdpRequest.getExecutedCommand(Command.REFILL.getCommandDisplayName());
			if(null != fdpCommand){
				refillProfileId = (null != fdpCommand.getInputParam("refillProfileID")) ? fdpCommand.getInputParam("refillProfileID").getValue().toString():
					refillProfileId;
			}
		}
		return refillProfileId;
	}
	
	/**
	 * This method returns the originOperatorID used in commands
	 * @param fdpRequest
	 * @return
	 */
	public static String getOriginOperatorID(FDPRequest fdpRequest){
		String originOperatorID = FDPLoggerConstants.NOT_APPLICABLE;
		List<FDPCommand> commands = fdpRequest.getExecutedCommands();
		if(null != commands){
			for(FDPCommand fdpCommand : commands){
				if(ExternalSystem.AIR.equals(fdpCommand.getSystem()) && null != fdpCommand.getInputParam("originOperatorID")){
					originOperatorID = fdpCommand.getInputParam("originOperatorID").getValue().toString();
					break;
				}
			}
		}
		return originOperatorID;
	}
	
	/**
	 * This method returns the transactionCode used in commands
	 * @param fdpRequest
	 * @return
	 */
	public static String getTransactionCode(FDPRequest fdpRequest){
		StringBuilder transactionCode = new StringBuilder();
		List<FDPCommand> commands = fdpRequest.getExecutedCommands();
		if(null != commands){
			for(FDPCommand fdpCommand : commands){
				if(ExternalSystem.AIR.equals(fdpCommand.getSystem()) && null != fdpCommand.getInputParam("transactionCode")){
					transactionCode.append(fdpCommand.getInputParam("transactionCode").getValue().toString() + FDPConstant.SEMICOLON);
				}
			}
		}
		return (0 == transactionCode.length() ? FDPLoggerConstants.NOT_APPLICABLE : transactionCode.toString());
	}
	
	public static String getVMIP(){
		try {
			return InetAddress.getLocalHost().getHostAddress();
		} catch (UnknownHostException e) {
			return FDPLoggerConstants.NOT_APPLICABLE;
		}
		
	}
	
	
	/**
	 * This method returns the originTransactionID
	 * @param fdpRequest
	 * @return
	 */
	public static String getCorrelationID(FDPRequest fdpRequest){
		return fdpRequest.getOriginTransactionID().toString();
	}
	
	
	/**
	 * This method returns true if the use case executed was me2u, false otherwise
	 * @param fdpRequest
	 * @return
	 */
	public static String isMe2U(FDPRequest fdpRequest) {
		return (null != fdpRequest.getAuxiliaryRequestParameter(AuxRequestParam.IS_TIME2SHARE) 
				|| null != fdpRequest.getAuxiliaryRequestParameter(AuxRequestParam.isData2Share)
				|| isTime4U(fdpRequest))?
						FDPLoggerConstants.TRUE : FDPLoggerConstants.FALSE;
		
	}
	

	/**
	 * This method returns the Me2U transaction details
	 * @param fdpRequest
	 * @return
	 */
	public static String getMe2uInfo(FDPRequest fdpRequest){
		StringBuilder me2uInfo = new StringBuilder();
		if(null != fdpRequest.getAuxiliaryRequestParameter(AuxRequestParam.IS_TIME2SHARE)){
			Object amtToTrans = fdpRequest.getAuxiliaryRequestParameter(AuxRequestParam.ME2U_AMT_TO_TRANSFER);
			Object amtToRecieve = fdpRequest.getAuxiliaryRequestParameter(AuxRequestParam.ME2U_AMT_TO_BE_RECIEVED);
			Object beneficiaryMsisdn = fdpRequest.getAuxiliaryRequestParameter(AuxRequestParam.ME2U_SUBSCRIBER_TO_CREDIT);
			me2uInfo.append(FDPLoggerConstants.TIME2SHARE).append(FDPConstant.SEMICOLON)
			.append(ExternalSystem.AIR.name()).append(FDPConstant.SEMICOLON)
			.append(FDPLoggerConstants.APARTY).append(FDPConstant.SEMICOLON)
			.append(fdpRequest.getSubscriberNumber()).append(FDPConstant.SEMICOLON)
			.append(FDPLoggerConstants.MINUS).append(null!=amtToTrans?amtToTrans.toString():FDPLoggerConstants.NOT_APPLICABLE).append(FDPConstant.SEMICOLON)
			.append(FDPLoggerConstants.BPARTY).append(FDPConstant.SEMICOLON)
			.append(null != beneficiaryMsisdn ? beneficiaryMsisdn.toString() : FDPLoggerConstants.NOT_APPLICABLE).append(FDPConstant.SEMICOLON)
			.append(null != amtToRecieve ? amtToRecieve.toString() : FDPLoggerConstants.NOT_APPLICABLE);
		}else if(null != fdpRequest.getAuxiliaryRequestParameter(AuxRequestParam.isData2Share)){
			Object amtToTrans = fdpRequest.getAuxiliaryRequestParameter(AuxRequestParam.ME2U_AMT_TO_TRANSFER);
			Object transCharges = fdpRequest.getAuxiliaryRequestParameter(AuxRequestParam.TRANSACTIONAL_CHARGES);
			Object beneficiaryMsisdn = fdpRequest.getAuxiliaryRequestParameter(AuxRequestParam.ME2U_SUBSCRIBER_TO_CREDIT);
			me2uInfo.append(FDPLoggerConstants.DATA2SHARE).append(FDPConstant.SEMICOLON)
			.append(ExternalSystem.AIR.name()).append(FDPConstant.SEMICOLON)
			.append(FDPLoggerConstants.APARTY).append(FDPConstant.SEMICOLON)
			.append(fdpRequest.getSubscriberNumber()).append(FDPConstant.SEMICOLON)
			.append(null!=transCharges ? transCharges.toString():FDPLoggerConstants.NOT_APPLICABLE).append(FDPConstant.SEMICOLON)
			.append(FDPLoggerConstants.MINUS).append(null!=amtToTrans ? amtToTrans.toString():FDPLoggerConstants.NOT_APPLICABLE).append(FDPLoggerConstants.DATA2SHARE_MB).append(FDPConstant.SEMICOLON)
			.append(FDPLoggerConstants.BPARTY).append(FDPConstant.SEMICOLON)
			.append(null != beneficiaryMsisdn ? beneficiaryMsisdn.toString() : FDPLoggerConstants.NOT_APPLICABLE).append(FDPConstant.SEMICOLON)
			.append(null!=amtToTrans ? amtToTrans.toString():FDPLoggerConstants.NOT_APPLICABLE).append(FDPLoggerConstants.DATA2SHARE_MB);
		}else if(isTime4U(fdpRequest)){
			com.ericsson.fdp.common.enums.ExternalSystem externalSystem = fdpRequest.getExternalSystemToCharge();
			String externalSystemName = null!=externalSystem ? externalSystem.name() : FDPLoggerConstants.NOT_APPLICABLE;
			Object amtToTrans = fdpRequest.getAuxiliaryRequestParameter(AuxRequestParam.TIME4_PRODUCT_COST);
			String amtTrans = null!=amtToTrans ? amtToTrans.toString():FDPLoggerConstants.NOT_APPLICABLE;
			Object amtToTransEVDSMoMo = fdpRequest.getAuxiliaryRequestParameter(AuxRequestParam.TIME4_PRODUCT_COST_EVD_MOMO);
			String amtTransEVDSMoMo = null!=amtToTransEVDSMoMo ? amtToTransEVDSMoMo.toString():FDPLoggerConstants.NOT_APPLICABLE;
			Object beneficiaryMsisdn = fdpRequest.getAuxiliaryRequestParameter(AuxRequestParam.BENEFICIARY_MSISDN);
			me2uInfo.append(FDPLoggerConstants.TIME4U).append(FDPConstant.SEMICOLON)
			.append(externalSystemName).append(FDPConstant.SEMICOLON)
			.append(FDPLoggerConstants.TIME4U_APARTY).append(FDPConstant.SEMICOLON)
			.append(fdpRequest.getSubscriberNumber()).append(FDPConstant.SEMICOLON)
			.append(FDPLoggerConstants.MINUS).append(externalSystemName.equals(com.ericsson.fdp.common.enums.ExternalSystem.AIR.name())?amtTrans:amtTransEVDSMoMo)
			.append(FDPConstant.SEMICOLON)
			.append(FDPLoggerConstants.TIME4U_BPARTY).append(FDPConstant.SEMICOLON)
			.append(null != beneficiaryMsisdn ? beneficiaryMsisdn.toString() : FDPLoggerConstants.NOT_APPLICABLE).append(FDPConstant.SEMICOLON)
			.append(FDPConstant.SEMICOLON);
		}else{
			me2uInfo.append(FDPLoggerConstants.NOT_APPLICABLE);
		}
		return me2uInfo.toString();
	}
	
	

	
	/**
	 * Returns true if the request processed is for time4u product, false otherwise
	 * @param fdpRequest
	 * @return
	 */
	public static Boolean isTime4U(FDPRequest fdpRequest) {
		boolean isTime4U = false;
		if(null != fdpRequest.getAuxiliaryRequestParameter(AuxRequestParam.TIME4_PRODUCT_COST)){
			isTime4U = true;
		}else{
			final FDPCacheable fdpCacheable = fdpRequest.getValueFromRequest(RequestMetaValuesKey.PRODUCT);
			if (fdpCacheable instanceof Product) {
				Product fdpProduct = (Product) fdpCacheable;
				isTime4U = fdpProduct.getProductType().equals(ProductType.Time4U.getName());
			}
		}	
		return isTime4U;
	}

	public static void appendCommandLogs(FDPSMPPRequest dynamicMenuRequest, StringBuilder appenderValue) {
		final Map<String, String> configurationMap = dynamicMenuRequest.getCircle().getConfigurationKeyValueMap();
		final String paramDisplay = configurationMap.get(ConfigurationKey.PP_REPORT_PARAM_DISPLAY.getAttributeName());
		
		StringBuilder getcommandBuilder = new StringBuilder();
		StringBuilder setCommandBuilder = new StringBuilder();
		StringBuilder uaCommandBuilder = new StringBuilder();
		StringBuilder rollbackCommandBuilder = new StringBuilder();
		
		getcommandBuilder.append(FDPLoggerConstants.GET_COMMAND).append(FDPConstant.LOGGER_KEY_VALUE_DELIMITER);
		setCommandBuilder.append(FDPLoggerConstants.SET_COMMAND).append(FDPConstant.LOGGER_KEY_VALUE_DELIMITER);
		uaCommandBuilder.append(FDPLoggerConstants.COMMAND_UA_99).append(FDPConstant.LOGGER_KEY_VALUE_DELIMITER);
		rollbackCommandBuilder.append(FDPLoggerConstants.ROLLBACK_COMMAND).append(FDPConstant.LOGGER_KEY_VALUE_DELIMITER);
		
		if(null != paramDisplay && paramDisplay.equalsIgnoreCase(FDPLoggerConstants.TRUE)){
			List<FDPCommand> commandList = dynamicMenuRequest.getExecutedCommands();
			
			if(commandList != null) {
				for (FDPCommand command : commandList) {
					AbstractCommand abstractCommand = (AbstractCommand) command;
					String commandRequest = getCommandString(abstractCommand.getCommandExecutionType(), command, dynamicMenuRequest);
					String commandResponse = abstractCommand.getCommandResponse();
					if ((command.getCommandDisplayName().startsWith("Get") || command.getCommandDisplayName().startsWith("GET")) && 
							!command.getCommandDisplayName().equals("GetAccumulators")) {
						getcommandBuilder.append(command.getCommandDisplayName()).append("=").append("{").append(commandRequest.length() > 0 ? commandRequest : "")
						.append("}{").append(null != commandResponse && commandResponse.length() > 0 ? commandResponse : FDPLoggerConstants.NOT_APPLICABLE).append("}").append("#");
					}
					else if (command.getCommandDisplayName().equals("GetAccumulators")) {
						uaCommandBuilder.append(command.getCommandDisplayName()).append("=").append("{").append(commandRequest.length() > 0 ? commandRequest : "")
						.append("}{").append(null != commandResponse && commandResponse.length() > 0 ? commandResponse : FDPLoggerConstants.NOT_APPLICABLE).append("}").append("#");
					}
					else if (command.getCommandDisplayName().equals("Rollback")) {
						rollbackCommandBuilder.append(command.getCommandDisplayName()).append("=").append("{").append(commandRequest.length() > 0 ? commandRequest : "")
							.append("}{").append(null != commandResponse && commandResponse.length() > 0 ? commandResponse : FDPLoggerConstants.NOT_APPLICABLE).append("}").append("#");
					}
					else {
						setCommandBuilder.append(command.getCommandDisplayName()).append("=").append("{").append(commandRequest.length() > 0 ? commandRequest : "")
						.append("}{").append(null != commandResponse && commandResponse.length() > 0 ? commandResponse : FDPLoggerConstants.NOT_APPLICABLE).append("}").append("#");
					}
						
				}
			}
			
			if (getcommandBuilder.length() <= 7) {
				getcommandBuilder.append(FDPLoggerConstants.NOT_APPLICABLE);
			}
			
			if (setCommandBuilder.length() <= 7) {
				setCommandBuilder.append(FDPLoggerConstants.NOT_APPLICABLE);
			}
			
			if (uaCommandBuilder.length() <= 6) {
				uaCommandBuilder.append(FDPLoggerConstants.NOT_APPLICABLE);
			}
			if (rollbackCommandBuilder.length() <= 5) {
				rollbackCommandBuilder.append(FDPLoggerConstants.NOT_APPLICABLE);
			}

			appenderValue.append(FDPConstant.LOGGER_DELIMITER).append(getcommandBuilder);
			appenderValue.append(FDPConstant.LOGGER_DELIMITER).append(setCommandBuilder);
			appenderValue.append(FDPConstant.LOGGER_DELIMITER).append(uaCommandBuilder);
			appenderValue.append(FDPConstant.LOGGER_DELIMITER).append(rollbackCommandBuilder);
			
		}else{
			
			/*getcommandBuilder.append(FDPLoggerConstants.GET_COMMAND).append(FDPConstant.LOGGER_KEY_VALUE_DELIMITER).append(FDPLoggerConstants.SKIPPED);
			setCommandBuilder.append(FDPLoggerConstants.SET_COMMAND).append(FDPConstant.LOGGER_KEY_VALUE_DELIMITER).append(FDPLoggerConstants.SKIPPED);
			uaCommandBuilder.append(FDPLoggerConstants.COMMAND_UA_99).append(FDPConstant.LOGGER_KEY_VALUE_DELIMITER).append(FDPLoggerConstants.SKIPPED);
			rollbackCommandBuilder.append(FDPLoggerConstants.ROLLBACK_COMMAND).append(FDPConstant.LOGGER_KEY_VALUE_DELIMITER).append(FDPLoggerConstants.SKIPPED);*/
			getcommandBuilder.append(FDPLoggerConstants.SKIPPED);
			setCommandBuilder.append(FDPLoggerConstants.SKIPPED);
			uaCommandBuilder.append(FDPLoggerConstants.SKIPPED);
			rollbackCommandBuilder.append(FDPLoggerConstants.SKIPPED);
			
			appenderValue.append(FDPConstant.LOGGER_DELIMITER).append(getcommandBuilder);
			appenderValue.append(FDPConstant.LOGGER_DELIMITER).append(setCommandBuilder);
			appenderValue.append(FDPConstant.LOGGER_DELIMITER).append(uaCommandBuilder);
			appenderValue.append(FDPConstant.LOGGER_DELIMITER).append(rollbackCommandBuilder);
		}

	}
	
	/**
	 * This method is used to generate logs for rollback commands
	 * 
	 * @param dynamicMenuRequest
	 *            the request.
	 * @param fdpNode
	 *            the node that was furnished.
	 */
	public static void generateLogsForRollbackCommand(final FDPRequest fdpRequest, final FDPCommand command) {
		final String requestAppender = LoggerUtil.getRequestAppender(fdpRequest);
		final StringBuilder appenderValue = new StringBuilder();
		appenderValue.append(requestAppender);
		StringBuilder rollbackCommandBuilder = new StringBuilder();
		
		rollbackCommandBuilder.append("RBCK:");
		
		AbstractCommand abstractCommand = (AbstractCommand) command;
		String commandRequest = getCommandString(abstractCommand.getCommandExecutionType(), command, fdpRequest);
		String commandResponse = abstractCommand.getCommandResponse();
		
		rollbackCommandBuilder.append(command.getCommandDisplayName()).append("=").append("{").append(commandRequest.length() > 0 ? commandRequest : "")
		.append("}{").append(null != commandResponse && commandResponse.length() > 0 ? commandResponse : FDPLoggerConstants.NOT_APPLICABLE).append("}");
		
		appenderValue.append(rollbackCommandBuilder);
		final Logger circleLogger = LoggerUtil.getSummaryLoggerFromRequest(fdpRequest);
		FDPLogger.info(circleLogger, LoggerUtil.class, "generateLogsForRollbackCommand()", appenderValue.toString());
	}
	
	public static String getCommandString(CommandExecutionType executionType, FDPCommand command, FDPRequest fdpRequest) {
		String commandString = null;
		try {
			switch(executionType) {
			case ACIP:
				// If command is ACIP create ACIP xml.
				commandString = ACIPCommandUtil.toACIPXmlFormat(command);
				break;
			case UCIP:
				// If command is ACIP create UCIP xml.
				commandString = UCIPCommandUtil.toUCIPXmlFormat(command);
				break;
			case CGW:
				// If command is ACIP create CGW xml.
				commandString = CGWCommandUtil.toCGWXmlFormat(command);
				break;
			case RS:
				// If command is ACIP create RS xml.
				commandString = RSCommandUtil.toRSXmlFormat(command);
				break;
			case AIR:
				commandString = UCIPCommandUtil.toUCIPXmlFormat(command);
				break;
			case CAI:
				commandString = CAICommandUtil.toCAIFormat(command);
				break;
			case MML:
				commandString = MMLCommandUtil.toMMLFormat(command);
				break;
			case MCARBON:
				// If command is MCARBON
				commandString = MCarbonCommandUtil.toMCarbonFormat(command);
				break;
			case MANHATTAN:
				commandString = ManhattanCommandUtil.toManhattanFormat(command);
				break;	
			case CMS:
				commandString = CMSCommandUtil.toCMSXmlFormat(command);
				break;
			case MCLOAN:
				commandString = MCLoanCommandUtil.toMCarbonFormat(command);
				break;	
				//change the request format once confirmed.
			case FDPOFFLINE:
				commandString = FDPOffLineCommandUtil.toFDPOffLineFormat(command);
				break;
			case MM:
				commandString = MobilemoneyCommandUtil.toMobileMoneyXmlFormat(command);
				break;
			case Loyalty:
				commandString = LoyaltyCommandUtil.toLoyaltyXmlFormat(command);
				break;
			case EVDS:
				if(evdsType.contains((String) FDPConstant.EVDS_TYPE_HTTP) ||
						evdsType.contains((String) FDPConstant.EVDS_HTTP_TYPE)){
					commandString	= EVDSHttpCommandUtil.toEVDSXmlFormat(command);
				}
				else{
					commandString = EVDSCommandUtil.toEVDSXmlFormat(command);
				}
				break;
			case Ability:
				commandString = AbilityCommandUtil.toAbilityXmlFormat(command, fdpRequest);
				break;	
			case CIS:
				commandString = CISCommandUtil.toCisXmlFormat(command);
				break;
			case ADC:
				// If command is GET Handset, create XML for ADC interface
				commandString = ADCCommandUtil.toADCXmlFormat(command);
				break;
			default:
				break;
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		return commandString;
	}
	
	/**
	 * FNFOP:ADD;
	 * 
	 * @param dynamicMenuRequest
	 * @return
	 */
     public static String getFnFInfo(FDPRequest fdpRequest){
            StringBuilder fnfInfo = new StringBuilder();
            if (fdpRequest.getValueFromRequest(RequestMetaValuesKey.NODE) instanceof FDPNode) {
                   final FDPNode fdpNode = (FDPNode) fdpRequest.getValueFromRequest(RequestMetaValuesKey.NODE);
                   if (fdpNode instanceof ProductNode) {
                         final ProductNode productNode = (ProductNode) fdpNode;
                         switch(productNode.getServiceProvSubType()){
                                case FAF_ADD: 
                                       fnfInfo.append(FDPLoggerConstants.FNF_ADD)
                                       .append(FDPConstant.SEMICOLON)
                                       .append(FDPLoggerConstants.MSISDN)
                                       .append(FDPLoggerConstants.EQUALS)
                                       .append(fdpRequest.getSubscriberNumber())
                                       .append(FDPConstant.SEMICOLON)
                                       .append(FDPLoggerConstants.FAF_MSISDN)
                                       .append(FDPLoggerConstants.EQUALS)
                                        .append(null != fdpRequest.getAuxiliaryRequestParameter(AuxRequestParam.FAF_MSISDN_ADD) ? 
                                        		fdpRequest.getAuxiliaryRequestParameter(AuxRequestParam.FAF_MSISDN_ADD).toString() : FDPLoggerConstants.NOT_APPLICABLE);
                                       break;
                                case FAF_MODIFY:
                                       fnfInfo.append(FDPLoggerConstants.FNF_UPDATE)
                                       .append(FDPConstant.SEMICOLON).append(FDPLoggerConstants.MSISDN)
                                       .append(FDPLoggerConstants.EQUALS)
                                       .append(fdpRequest.getSubscriberNumber())
                                       .append(FDPConstant.SEMICOLON)
                                       .append(FDPLoggerConstants.FAF_MSISDN)
                                       .append(FDPLoggerConstants.EQUALS)
                                       .append(null != fdpRequest.getAuxiliaryRequestParameter(AuxRequestParam.FAF_MSISDN_ADD) ? 
                                       		fdpRequest.getAuxiliaryRequestParameter(AuxRequestParam.FAF_MSISDN_ADD).toString() : FDPLoggerConstants.NOT_APPLICABLE)
                                       .append(FDPConstant.SEMICOLON)
                                       .append(FDPLoggerConstants.FNF_DELETE)
                                       .append(FDPLoggerConstants.EQUALS)
                                       .append(null != fdpRequest.getAuxiliaryRequestParameter(AuxRequestParam.FAF_MSISDN_DELETE) ? 
                                       		fdpRequest.getAuxiliaryRequestParameter(AuxRequestParam.FAF_MSISDN_DELETE).toString() : FDPLoggerConstants.NOT_APPLICABLE);
                                       break;
                                case FAF_DELETE:
                                       fnfInfo.append(FDPLoggerConstants.FNF_DELETE)
                                       .append(FDPConstant.SEMICOLON).append(FDPLoggerConstants.MSISDN)
                                       .append(FDPLoggerConstants.EQUALS)
                                       .append(fdpRequest.getSubscriberNumber())
                                       .append(FDPConstant.SEMICOLON)
                                       .append(FDPLoggerConstants.FAF_MSISDN)
                                       .append(FDPLoggerConstants.EQUALS)
                                       .append(null != fdpRequest.getAuxiliaryRequestParameter(AuxRequestParam.FAF_NUMBER) ? 
                                          		fdpRequest.getAuxiliaryRequestParameter(AuxRequestParam.FAF_NUMBER).toString() : FDPLoggerConstants.NOT_APPLICABLE);;
                                       break;
                                case FAF_VIEW:
                                       fnfInfo.append(FDPLoggerConstants.FNF_VIEW)
                                       .append(FDPConstant.SEMICOLON).append(FDPLoggerConstants.MSISDN)
                                       .append(FDPLoggerConstants.EQUALS)
                                       .append(fdpRequest.getSubscriberNumber());
                                       break;
                                case FAF_REGISTER:
                                       fnfInfo.append(FDPLoggerConstants.FNF_REGISTER)
                                       .append(FDPConstant.SEMICOLON).append(FDPLoggerConstants.MSISDN)
                                       .append(FDPLoggerConstants.EQUALS)
                                       .append(fdpRequest.getSubscriberNumber());
                                       break;
                                case FAF_UNREGISTER:
                                       fnfInfo.append(FDPLoggerConstants.FNF_UNREGISTER)
                                       .append(FDPConstant.SEMICOLON).append(FDPLoggerConstants.MSISDN)
                                       .append(FDPLoggerConstants.EQUALS)
                                       .append(fdpRequest.getSubscriberNumber());
                                       break;
                                default:
                                       fnfInfo.append(FDPLoggerConstants.NOT_APPLICABLE);
                         }
                   }
            }
            return (0 == fnfInfo.length())? FDPLoggerConstants.NOT_APPLICABLE : fnfInfo.toString();
     }
     
     /**
      * Return the Beneficiary MSISDN
      * @param fdpRequest
      * @return
      */
     public static String getBenMsisdn(FDPRequest fdpRequest) {
    	 Object beneficiaryMsisdn = fdpRequest.getAuxiliaryRequestParameter(AuxRequestParam.BENEFICIARY_MSISDN);
    	 
    	 if (null == beneficiaryMsisdn)
    		 beneficiaryMsisdn = fdpRequest.getAuxiliaryRequestParameter(AuxRequestParam.ME2U_SUBSCRIBER_TO_CREDIT);
    	 
    	 return (null != beneficiaryMsisdn) ? beneficiaryMsisdn.toString() : FDPLoggerConstants.NOT_APPLICABLE;
    	 
     }
     
     /**
 	 * This function will check whether the product is purchase as Adhoc or Recurring
 	 * @param fdpRequest
 	 * @return
 	 */
 	public static String isAutoRenewal(FDPRequest fdpRequest) {
 		FDPCommand command = fdpRequest.getExecutedCommand(Command.MPR.getCommandDisplayName());
 		CommandParam commandParam = null;
 		String renewalType = null;
 		Integer renewalCount = null;
 		String autoRenewal = "NO";
 		if (command != null) {
 			commandParam = command.getInputParam("provisioningType");
 			renewalType = (String)commandParam.getValue();
 			commandParam = command.getInputParam("renewalCount");
 			if (commandParam.getValue() instanceof String)
 				renewalCount = Integer.parseInt((String)commandParam.getValue());
 			else if (commandParam.getValue() instanceof Integer)
 				renewalCount = (Integer)commandParam.getValue();
 			if (renewalType == "R" && renewalCount > 1) {
 				autoRenewal = "YES";
 			}
 		}
 		
 		return autoRenewal;
 	}
 	/**
 	 * Get sessionId of the request
 	 * @param fdpRequest
 	 * @return
 	 */
 	public static String getSessionId(FDPRequest fdpRequest) {
 		String sessionId = FDPLoggerConstants.NOT_APPLICABLE;
 		if (fdpRequest.getChannel().getName().equalsIgnoreCase(FDPLoggerConstants.USSD))
 			sessionId = ((FDPSMPPRequestImpl)fdpRequest).getSessionId();
 		return sessionId;
 	}
 	
 	/**
 	 * Get response time of the request
 	 * @param fdpRequest
 	 * @return
 	 */
 	public static String getresponseTime(FDPRequest fdpRequest) {
 		String responseTime = FDPLoggerConstants.NOT_APPLICABLE;
 		if (fdpRequest.getChannel().getName().equalsIgnoreCase(FDPLoggerConstants.USSD)) {
 			Long requestTime = ((FDPSMPPRequestImpl)fdpRequest).getRequestTime();
 			Long currentTime = System.currentTimeMillis();
 			responseTime = Long.toString((currentTime - requestTime));
 		}
 		return responseTime;
 	}


}
