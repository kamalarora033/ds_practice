package com.ericsson.fdp.business.util;

import com.ericsson.fdp.business.adapter.Adapter;
import com.ericsson.fdp.business.adapter.impl.DefaultAdapter;
import com.ericsson.fdp.business.adapter.impl.EMATelnetAdapter;
import com.ericsson.fdp.business.adapter.impl.HTTPAdapter;
import com.ericsson.fdp.business.adapter.impl.SOAPAdapter;
import com.ericsson.fdp.business.adapter.impl.SSHAdapter;
import com.ericsson.fdp.business.bean.CMSHttpAdapterREquest;
import com.ericsson.fdp.business.bean.HttpAdapterRequest;
import com.ericsson.fdp.business.bean.RSHttpAdapterRequest;
import com.ericsson.fdp.business.bean.SOAPAdapterRequest;
import com.ericsson.fdp.business.bean.TelnetAdapterRequest;
import com.ericsson.fdp.common.constants.FDPConstant;
import com.ericsson.fdp.core.command.FDPCommand;
import com.ericsson.fdp.core.config.utils.PropertyUtils;
import com.ericsson.fdp.core.entity.ExternalSystemDetail;
import com.ericsson.fdp.core.entity.FDPEMADetail;
import com.ericsson.fdp.core.exception.ExecutionFailedException;
import com.ericsson.fdp.core.request.FDPRequest;
import com.ericsson.fdp.dao.enums.ExternalSystem;

/**
 * This class is the implementation of the command execution broker interface.
 * This class defines the method to execute a command.
 * 
 * @author Ericsson
 * 
 */
public class CommandExecutionBrokerUtil {
	private static final String evdsType = PropertyUtils
			.getProperty("evds.protocol.type");
	/**
	 * Instantiates a new command execution broker util.
	 */
	private CommandExecutionBrokerUtil() {

	}

	/**
	 * This method is used to get the adapter to be used to execute the command.
	 * 
	 * @param command
	 *            The command to be executed.
	 * @param fdpRequest
	 *            The request to be used.
	 * @return The adapter to be used.
	 * @throws ExecutionFailedException
	 *             Exception, if any in getting the adapter.
	 */
	public static Adapter getAdapter(final FDPCommand command, final FDPRequest fdpRequest)
			throws ExecutionFailedException {
		Adapter adapter = null;
		switch (command.getCommandExecutionType()) {
		case ACIP:
			// If command is ACIP create ACIP xml.
			adapter = getHTTPAdapter(fdpRequest, command.getSystem(), ACIPCommandUtil.toACIPXmlFormat(command), null);
			break;
		case UCIP:
			// If command is ACIP create UCIP xml.
			adapter = getHTTPAdapter(fdpRequest, command.getSystem(), UCIPCommandUtil.toUCIPXmlFormat(command), null);
			break;
		case CGW:
			// If command is ACIP create CGW xml.
			adapter = getHTTPAdapter(fdpRequest, command.getSystem(), CGWCommandUtil.toCGWXmlFormat(command), null);
			break;
		case RS:
			// If command is ACIP create RS xml.
			adapter = getRSHTTPAdapter(fdpRequest, command.getSystem(), RSCommandUtil.toRSXmlFormat(command), null, command.getCommandDisplayName());
			break;
		case AIR:
			adapter = getHTTPAdapter(fdpRequest, command.getSystem(), UCIPCommandUtil.toUCIPXmlFormat(command), null);
			break;
			
		case DMC:
			adapter = getHTTPAdapter(fdpRequest, command.getSystem(), DMCCommandUtil.toDMCFormat(command), null);
			break;
		case CAI:
			adapter = getAdapter(fdpRequest, CAICommandUtil.toCAIFormat(command), command.getSystem(), command,
					CAICommandUtil.toCAIFormatForLog(command));
			break;
		case MML:
			adapter = getAdapter(fdpRequest, MMLCommandUtil.toMMLFormat(command), command.getSystem(), command,
					MMLCommandUtil.toMMLFormatForLog(command));
			break;
		case MCARBON:
			// If command is MCARBON
			adapter = getHTTPAdapter(fdpRequest, command.getSystem(), MCarbonCommandUtil.toMCarbonFormat(command),
					MCarbonCommandUtil.getExternalSystem(fdpRequest));
			break;
		case MANHATTAN:
			adapter = getHTTPAdapter(fdpRequest, command.getSystem(), ManhattanCommandUtil.toManhattanFormat(command),
					ManhattanCommandUtil.getExternalSystem(fdpRequest));
			break;	
		case CMS:
			adapter = getCMSHTTPAdapter(fdpRequest, command.getSystem(), CMSCommandUtil.toCMSXmlFormat(command), null,command.getCommandDisplayName());
			break;
		case MCLOAN:
			adapter = getHTTPAdapter(fdpRequest, command.getSystem(), MCLoanCommandUtil.toMCarbonFormat(command), MCLoanCommandUtil.getExternalSystem(fdpRequest));
			break;	
			//change the request format once confirmed.
		case FDPOFFLINE:
			adapter = getHTTPAdapter(fdpRequest, command.getSystem(), FDPOffLineCommandUtil.toFDPOffLineFormat(command), null);
			break;
		case MM:
			adapter = getHTTPAdapter(fdpRequest, command.getSystem(), MobilemoneyCommandUtil.toMobileMoneyXmlFormat(command), null, command.getCommandDisplayName());
			break;
		case Loyalty:
			adapter = getHTTPAdapter(fdpRequest, command.getSystem(), LoyaltyCommandUtil.toLoyaltyXmlFormat(command), null);
			break;
		case EVDS:
			if(evdsType.contains((String) FDPConstant.EVDS_TYPE_HTTP) ||
					evdsType.contains((String) FDPConstant.EVDS_HTTP_TYPE)){
			adapter	= getHTTPAdapter(fdpRequest, command.getSystem(), EVDSHttpCommandUtil.toEVDSXmlFormat(command), null);
			}
			else{
			adapter = getHTTPAdapter(fdpRequest, command.getSystem(), EVDSCommandUtil.toEVDSXmlFormat(command), null);
			}
			break;
		case Ability:
			adapter = getSOAPAdapter(fdpRequest, command.getSystem(), AbilityCommandUtil.toAbilityXmlFormat(command, fdpRequest), null);
			break;	
		case CIS:
			adapter = getHTTPAdapter(fdpRequest, command.getSystem(), CISCommandUtil.toCisXmlFormat(command), null);
			break;
		case ESF:
			adapter = getSOAPAdapter(fdpRequest, command.getSystem(), AbilityCommandUtil.toAbilityXmlFormat(command, fdpRequest), null);
		case SBBB:
			adapter = getHTTPSBBAdapter(fdpRequest, command.getSystem(), SBBCommandUtil.toSBBFormat(command),
					SBBCommandUtil.getExternalSystem(fdpRequest), command);
			break;
		case ADC:
			adapter = getHTTPAdapter(fdpRequest, command.getSystem(), ADCCommandUtil.toADCXmlFormat(command), null);
			break;
		default:
			throw new ExecutionFailedException(
					"The execution could not be completed as command type is not UCIP or ACIP. The command type is "
							+ command.getCommandExecutionType());
		}
		return adapter;

	}

	private static Adapter getRSHTTPAdapter(final FDPRequest fdpRequest, final ExternalSystem externalSystem,
			final String xmlFormat, final ExternalSystemDetail extenalSystemDetail, final String commandDisplayName) {
		final RSHttpAdapterRequest httpAdapterRequest = new RSHttpAdapterRequest(fdpRequest.getRequestId(), fdpRequest
				.getCircle().getCircleCode(), fdpRequest.getCircle().getCircleName(), extenalSystemDetail, commandDisplayName);
		// Currently all the commands are to be executed on http. Change the
		// adapter as per the requirement in case need arises.
		return new HTTPAdapter<String>(httpAdapterRequest, externalSystem, xmlFormat,fdpRequest);
	}

	/**
	 * This method is used to get the adapter.
	 * 
	 * @param fdpRequest
	 *            the request.
	 * @param command
	 *            the command.
	 * @return the adapater.
	 * @throws ExecutionFailedException
	 *             Exception in execution.
	 */
	private static Adapter getAdapter(final FDPRequest fdpRequest, final String command,
			final ExternalSystem externalSystem, final FDPCommand commandToExecute, final String logValue)
			throws ExecutionFailedException {
		final TelnetAdapterRequest telnetAdapterRequest = new TelnetAdapterRequest(fdpRequest.getRequestId(),
				fdpRequest.getCircle().getCircleCode(), fdpRequest.getCircle().getCircleName(), null,
				commandToExecute.getCommandDisplayName(), logValue);
		Adapter adapter = new DefaultAdapter<String>(telnetAdapterRequest, externalSystem, command);
		final FDPEMADetail emaDetail = RequestUtil.getEmaDetails(fdpRequest);
		if (emaDetail != null) {
			telnetAdapterRequest.setEndpoint(emaDetail.getEndpoint());
			telnetAdapterRequest.setEmaDetail(emaDetail);
			//System.out.println("Setting endpoint as " + emaDetail.getEndpoint());
			switch (emaDetail.getRouteDefinitionType()) {
			case SSH:
				adapter = new SSHAdapter<String>(telnetAdapterRequest, externalSystem, command, fdpRequest);
				break;
			case TELNET:
				adapter = new EMATelnetAdapter<String>(telnetAdapterRequest, externalSystem, command, fdpRequest);
				break;
			default:
				throw new ExecutionFailedException("Could not create adapter");
			}
		}
		return adapter;
	}

	/**
	 * This method is used to get the http adapter.
	 * 
	 * @param fdpRequest
	 *            the request object.
	 * @param externalSystem
	 *            the external system for the http adapter.
	 * @param xmlFormat
	 *            the xml to be used.
	 * @param extenalSystemDetail
	 * 			  the external system details
	 * @return the adapter.
	 * @throws ExecutionFailedException
	 */
	private static Adapter getHTTPAdapter(final FDPRequest fdpRequest, final ExternalSystem externalSystem,
			final String xmlFormat, final ExternalSystemDetail extenalSystemDetail) throws ExecutionFailedException {
		final HttpAdapterRequest httpAdapterRequest = new HttpAdapterRequest(fdpRequest.getRequestId(), fdpRequest
				.getCircle().getCircleCode(), fdpRequest.getCircle().getCircleName(), extenalSystemDetail);
		// Currently all the commands are to be executed on http. Change the
		// adapter as per the requirement in case need arises.
		return new HTTPAdapter<String>(httpAdapterRequest, externalSystem, xmlFormat,fdpRequest);
	}
	
	/**
	 * This method is used to get the http adapter.
	 * 
	 * @param fdpRequest
	 *            the request object.
	 * @param externalSystem
	 *            the external system for the http adapter.
	 * @param xmlFormat
	 *            the xml to be used.
	 * @param extenalSystemDetail
	 * 			  the external system details
	 * @param commandName
	 * 			  the command name to execute
	 * @return the adapter.
	 * @throws ExecutionFailedException
	 */
	private static Adapter getHTTPAdapter(final FDPRequest fdpRequest, final ExternalSystem externalSystem,
			final String xmlFormat, final ExternalSystemDetail extenalSystemDetail, String commandName) throws ExecutionFailedException {
		final HttpAdapterRequest httpAdapterRequest = new HttpAdapterRequest(fdpRequest.getRequestId(), fdpRequest
				.getCircle().getCircleCode(), fdpRequest.getCircle().getCircleName(), extenalSystemDetail, commandName);
		return new HTTPAdapter<String>(httpAdapterRequest, externalSystem, xmlFormat,fdpRequest);
	}
	
	/**
	 * @param fdpRequest
	 *            the request object.
	 * @param externalSystem
	 *            the external system for the CMShttp adapter.
	 * @param xmlFormat
	 *            the xml to be used.
	 * @return the cmshttpadapter.
	 * 
	 */
	private static Adapter getCMSHTTPAdapter(final FDPRequest fdpRequest, final ExternalSystem externalSystem,
			final String xmlFormat, final ExternalSystemDetail extenalSystemDetail, final String commandDisplayName) {
		final CMSHttpAdapterREquest httpAdapterRequest = new CMSHttpAdapterREquest(fdpRequest.getRequestId(), fdpRequest
				.getCircle().getCircleCode(), fdpRequest.getCircle().getCircleName(), extenalSystemDetail, commandDisplayName);
		// Currently all the commands are to be executed on http. Change the
		// adapter as per the requirement in case need arises.
		return new HTTPAdapter<String>(httpAdapterRequest, externalSystem, xmlFormat,fdpRequest);
	}
	
	/**
	 * @param fdpRequest
	 *            the request object.
	 * @param externalSystem
	 *            the external system for the CMShttp adapter.
	 * @param xmlFormat
	 *            the xml to be used.
	 * @return the cmshttpadapter.
	 * 
	 */
	private static Adapter getMCLoanHTTPAdapter(final FDPRequest fdpRequest, final ExternalSystem externalSystem,
			final String xmlFormat, final ExternalSystemDetail extenalSystemDetail, final String commandDisplayName) {
		final CMSHttpAdapterREquest httpAdapterRequest = new CMSHttpAdapterREquest(fdpRequest.getRequestId(), fdpRequest
				.getCircle().getCircleCode(), fdpRequest.getCircle().getCircleName(), extenalSystemDetail, commandDisplayName);
		// Currently all the commands are to be executed on http. Change the
		// adapter as per the requirement in case need arises.
		return new HTTPAdapter<String>(httpAdapterRequest, externalSystem, xmlFormat,fdpRequest);
	}
	
	/**
	 * This method will create the http adapter for SBBB interface.
	 * 
	 * @param fdpRequest
	 * @param externalSystem
	 * @param xmlFormat
	 * @param extenalSystemDetail
	 * @param fdpCommand
	 * @return
	 * @throws ExecutionFailedException
	 */
	private static Adapter getHTTPSBBAdapter(final FDPRequest fdpRequest, final ExternalSystem externalSystem,
			final String xmlFormat, final ExternalSystemDetail extenalSystemDetail, final FDPCommand fdpCommand) throws ExecutionFailedException {
		final HttpAdapterRequest httpAdapterRequest = new HttpAdapterRequest(fdpRequest.getRequestId(), fdpRequest
				.getCircle().getCircleCode(), fdpRequest.getCircle().getCircleName(), extenalSystemDetail, fdpCommand.getCommandName());
		// Currently all the commands are to be executed on http. Change the
		// adapter as per the requirement in case need arises.
		return new HTTPAdapter<String>(httpAdapterRequest, externalSystem, xmlFormat,fdpRequest);
	}

	private static Adapter getSOAPAdapter(final FDPRequest fdpRequest, final ExternalSystem externalSystem,
			final String xmlFormat, final ExternalSystemDetail extenalSystemDetail) throws ExecutionFailedException {
		final SOAPAdapterRequest soapAdapterRequest = new SOAPAdapterRequest(fdpRequest.getRequestId(), fdpRequest
				.getCircle().getCircleCode(), fdpRequest.getCircle().getCircleName(), extenalSystemDetail);
		// Currently all the commands are to be executed on http. Change the
		// adapter as per the requirement in case need arises.
		return new SOAPAdapter<String>(soapAdapterRequest, externalSystem, xmlFormat,fdpRequest);
	}
}
