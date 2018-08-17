package com.ericsson.fdp.business.route.processor;

import javax.annotation.Resource;

import org.apache.camel.Exchange;
import org.apache.camel.Message;
import org.apache.camel.Processor;
import org.apache.camel.component.http.HttpComponent;
import org.apache.commons.httpclient.params.HttpConnectionManagerParams;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ericsson.fdp.business.constants.BusinessConstants;
import com.ericsson.fdp.business.enums.Command;
import com.ericsson.fdp.common.constants.FDPConstant;
import com.ericsson.fdp.core.cache.AppCacheSubStore;
import com.ericsson.fdp.core.dsm.framework.service.FDPCache;
import com.ericsson.fdp.core.entity.MobileMoneySystemDetails;
import com.ericsson.fdp.core.serviceprovisioning.bean.FDPAppBag;
import com.ericsson.fdp.dao.dto.FDPADCConfigDTO;
import com.ericsson.fdp.dao.dto.FDPAIRConfigDTO;
import com.ericsson.fdp.dao.dto.FDPAbilityConfigDTO;
import com.ericsson.fdp.dao.dto.FDPCGWConfigDTO;
import com.ericsson.fdp.dao.dto.FDPCMSConfigDTO;
import com.ericsson.fdp.dao.dto.FDPDMCConfigDTO;
import com.ericsson.fdp.dao.dto.FDPExternalSystemDTO;
import com.ericsson.fdp.dao.dto.FDPMCLoanConfigDTO;
import com.ericsson.fdp.dao.dto.FDPOfflineConfigDTO;
import com.ericsson.fdp.dao.dto.FDPRSConfigDTO;
import com.ericsson.fdp.dao.enums.ExternalSystem;

/**
 * The Class HeaderProcessor used process headers on the basis of ip and port of
 * external system.
 */
public class HttpHeaderProcessor implements Processor {

	/** The request cache. */
	@Resource(lookup = "java:app/fdpCoreServices-1.0/ApplicationConfigCache")
	private FDPCache<FDPAppBag, Object> applicationConfigCache;
	
	private static final Logger LOGGER = LoggerFactory.getLogger(HttpHeaderProcessor.class);

	@Override
	public void process(Exchange exchange) throws Exception {

        // exchange.setProperty(name, value);
		Message in = exchange.getIn();
		String outgoingcircleCodeIpPort = exchange.getProperty(BusinessConstants.OUTGOING_CIRCLE_CODE_IP_PORT,
				String.class);
		String externalSystemType = exchange.getProperty(BusinessConstants.EXTERNAL_SYSTEM_TYPE, String.class);
		
		if(null == externalSystemType){
			externalSystemType = ExternalSystem.Ability.name();
		}

        //HttpComponent httpComponent = exchange.getContext().getComponent(BusinessConstants.HTTP_COMPONENT, HttpComponent.class);

		FDPAppBag appBag = new FDPAppBag();

		AppCacheSubStore appCacheSubStoreKey = null;
		if (externalSystemType.equals(ExternalSystem.AIR.name())) {
			appCacheSubStoreKey = AppCacheSubStore.AIRCONFEGURATION_MAP;
			appBag.setSubStore(appCacheSubStoreKey);
			appBag.setKey(outgoingcircleCodeIpPort);
			FDPAIRConfigDTO fdpairConfigDTO = (FDPAIRConfigDTO) applicationConfigCache.getValue(appBag);
			in.setHeader(BusinessConstants.AUTHORIZATION, fdpairConfigDTO.getAuthenticationKey());
			in.setHeader(BusinessConstants.USER_AGENT, fdpairConfigDTO.getUserAgent());
			in.setHeader(BusinessConstants.HOST, fdpairConfigDTO.getIpAddress() + BusinessConstants.COLON
					+ fdpairConfigDTO.getPort());
			in.setHeader(Exchange.HTTP_PATH, fdpairConfigDTO.getContextPath());
			in.setHeader(Exchange.CONTENT_TYPE, BusinessConstants.HTTP_CONTENT_TYPE_AIR);
			in.setHeader(BusinessConstants.HEADER_CONNECTION, BusinessConstants.HEADER_CONNECTION_VALUE);
			in.setHeader(BusinessConstants.HEADER_ACCEPT, BusinessConstants.HEADER_ACCEPT_VALUE);
			in.setHeader(Exchange.HTTP_METHOD, BusinessConstants.HTTP_METHOD);
            /*if (httpComponent != null) {
                HttpConnectionManagerParams params = httpComponent.getHttpConnectionManager().getParams();
                params.setConnectionTimeout(fdpairConfigDTO.getConnectionTimeout());
                params.setSoTimeout(fdpairConfigDTO.getResponseTimeout());
            }*/
			//updateHttpTimeoutParameters(httpComponent, fdpairConfigDTO.getConnectionTimeout(), fdpairConfigDTO.getResponseTimeout());
			

		} else if (externalSystemType.equals(ExternalSystem.CGW.name())) {
			appCacheSubStoreKey = AppCacheSubStore.CGWCONFEGURATION_MAP;
			appBag.setSubStore(appCacheSubStoreKey);
			appBag.setKey(outgoingcircleCodeIpPort);
			FDPCGWConfigDTO fdpcgwConfigDTO = (FDPCGWConfigDTO) applicationConfigCache.getValue(appBag);
			in.setHeader(Exchange.HTTP_METHOD, BusinessConstants.HTTP_METHOD);
			in.setHeader(Exchange.HTTP_PATH, fdpcgwConfigDTO.getContextPath());
			in.setHeader(Exchange.CONTENT_TYPE, BusinessConstants.HTTP_CONTENT_TYPE_CGW);
			in.setHeader(BusinessConstants.HEADER_ACCEPT, BusinessConstants.HEADER_ACCEPT_VALUE_CGW);
			in.setHeader(BusinessConstants.USER_AGENT, BusinessConstants.USER_AGENT_STRING_CGW);
			in.setHeader(BusinessConstants.HOST, fdpcgwConfigDTO.getIpAddress() + BusinessConstants.COLON
					+ fdpcgwConfigDTO.getPort());

		} else if (externalSystemType.equals(ExternalSystem.RS.name())) {
			appCacheSubStoreKey = AppCacheSubStore.RSCONFEGURATION_MAP;
			appBag.setSubStore(appCacheSubStoreKey);
			appBag.setKey(outgoingcircleCodeIpPort);
			FDPRSConfigDTO fdprsConfigDTO = (FDPRSConfigDTO) applicationConfigCache.getValue(appBag);
			String commandName = exchange.getProperty(BusinessConstants.RS_COMMAND_NAME, String.class);
			LOGGER.debug("Command Name got from Exchange : {}",commandName);
			if (FDPConstant.RS_DEPROVISIONING_COMMAND.equals(commandName)) {
				in.setHeader(Exchange.HTTP_PATH, "cgw/Deprovision");
			} else if (FDPConstant.GET_SERVICE_DTLS_REQUEST_COMMAND.equals(commandName)) {
				in.setHeader(Exchange.HTTP_PATH, "cgw/GetServicesDtls");
			}  else if (FDPConstant.GET_SERVICE_DETAILS_REQUEST_COMMAND.equals(commandName)) {
				in.setHeader(Exchange.HTTP_PATH, "cgw/GetServicesDtls");
			} else if (Command.SPR.getCommandDisplayName().equals(commandName)) {
				in.setHeader(Exchange.HTTP_PATH, "cgw/SingleProvision");
			} else if (Command.GET_ME2U_SUBSCRIBER.getCommandDisplayName().equals(commandName)) {
				in.setHeader(Exchange.HTTP_PATH, "cgw/GetMe2uSubscriber");
			} else if (Command.GET_PIN_DETAIL.getCommandDisplayName().equals(commandName)) {
				in.setHeader(Exchange.HTTP_PATH, "cgw/Me2uPinValidation");
			} else if (Command.UPDATE_PIN_DETAIL.getCommandDisplayName().equals(commandName)) {
				in.setHeader(Exchange.HTTP_PATH, "cgw/Me2uUpdatePin");
			} 
			else if(Command.GET_ACTIVE_BUNDLES_DETAILS_REQUEST.getCommandDisplayName().equals(commandName)){
				in.setHeader(Exchange.HTTP_PATH, "cgw/getActiveBundles");
			}
			else {
				in.setHeader(Exchange.HTTP_PATH, "cgw/MultiProvision");
			}
			LOGGER.debug("Http Version passed to Exhange : {}",in.getHeader(Exchange.HTTP_PATH,String.class));
			in.setHeader(Exchange.CONTENT_TYPE, BusinessConstants.HTTP_CONTENT_TYPE_RS);
			in.setHeader(BusinessConstants.HEADER_ACCEPT, BusinessConstants.HEADER_ACCEPT_VALUE_RS);
			in.setHeader(BusinessConstants.HOST, fdprsConfigDTO.getIpAddress() + BusinessConstants.COLON
					+ fdprsConfigDTO.getPort());
			in.setHeader(Exchange.HTTP_METHOD, BusinessConstants.HTTP_METHOD);

           /* if (httpComponent != null) {
                HttpConnectionManagerParams params = httpComponent.getHttpConnectionManager().getParams();
                params.setConnectionTimeout(Integer.parseInt(fdprsConfigDTO.getConnectionTimeout()));
                params.setSoTimeout(Integer.parseInt(fdprsConfigDTO.getResponseTimeout()));
            }*/
			//updateHttpTimeoutParameters(httpComponent, Integer.parseInt(fdprsConfigDTO.getConnectionTimeout()), Integer.parseInt(fdprsConfigDTO.getResponseTimeout()));
		}
		
		 else if (externalSystemType.equals(ExternalSystem.CMS.name())) {
				appCacheSubStoreKey = AppCacheSubStore.CMSCONFEGURATION_MAP;
				appBag.setSubStore(appCacheSubStoreKey);
				appBag.setKey(outgoingcircleCodeIpPort);
				FDPCMSConfigDTO fdpCMSConfigDTO = (FDPCMSConfigDTO) applicationConfigCache.getValue(appBag);
				in.setHeader(Exchange.HTTP_PATH,fdpCMSConfigDTO.getContextPath());
				//in.setHeader(BusinessConstants.CAMEL_HTTP_METHOD_TYPE, BusinessConstants.CAMEL_HTTP_METHOD_POST);
				in.setHeader(Exchange.HTTP_METHOD, BusinessConstants.CAMEL_HTTP_METHOD_POST);
				in.setHeader(BusinessConstants.AUTHORIZATION, fdpCMSConfigDTO.getAuthenticationKey());
				in.setHeader(Exchange.CONTENT_TYPE, BusinessConstants.HTTP_CONTENT_TYPE_CMS);
				//in.setHeader(Exchange.HTTP_METHOD, BusinessConstants.HTTP_METHOD);
				in.setHeader(BusinessConstants.HEADER_CONNECTION, BusinessConstants.HEADER_CONNECTION_VALUE);
				//in.setHeader(BusinessConstants.CAMEL_HTTP_METHOD_TYPE, BusinessConstants.CAMEL_HTTP_METHOD_POST);
			}
		 else if (externalSystemType.equals(ExternalSystem.FDPOFFLINE.name())) {
				appCacheSubStoreKey = AppCacheSubStore.FDPOFFLINE_DETAILS;
				appBag.setSubStore(appCacheSubStoreKey);
				appBag.setKey(outgoingcircleCodeIpPort);
				FDPOfflineConfigDTO fdpOfflineConfigDTO = (FDPOfflineConfigDTO) applicationConfigCache.getValue(appBag);
				in.setHeader(Exchange.HTTP_PATH,fdpOfflineConfigDTO.getContextPath());
				//in.setHeader(BusinessConstants.CAMEL_HTTP_METHOD_TYPE, BusinessConstants.CAMEL_HTTP_METHOD_POST);
				in.setHeader(Exchange.HTTP_METHOD, BusinessConstants.CAMEL_HTTP_METHOD_POST);
				in.setHeader(BusinessConstants.AUTHORIZATION, fdpOfflineConfigDTO.getAuthenticationKey());
				in.setHeader(Exchange.CONTENT_TYPE, BusinessConstants.HEADER_ACCEPT_VALUE_FDPOFFLINE);
				//in.setHeader(Exchange.HTTP_METHOD, BusinessConstants.HTTP_METHOD);
				in.setHeader(BusinessConstants.HEADER_CONNECTION, BusinessConstants.HEADER_CONNECTION_VALUE);
				//in.setHeader(BusinessConstants.CAMEL_HTTP_METHOD_TYPE, BusinessConstants.CAMEL_HTTP_METHOD_POST);
			}
		
		 else if (externalSystemType.equals(ExternalSystem.MCLOAN.name())) {
				appCacheSubStoreKey = AppCacheSubStore.MCLOAN_DETAILS;
				appBag.setSubStore(appCacheSubStoreKey);
				appBag.setKey(outgoingcircleCodeIpPort);
				FDPMCLoanConfigDTO fdpMcLoanConfigDTO = (FDPMCLoanConfigDTO) applicationConfigCache.getValue(appBag);
				final String compliedURI = getCompiledHttpUri(in.getHeader(Exchange.HTTP_QUERY).toString(), fdpMcLoanConfigDTO.getUserName(),
						fdpMcLoanConfigDTO.getPassword());
				in.setHeader(Exchange.HTTP_QUERY, compliedURI);
				in.setHeader(Exchange.HTTP_PATH,fdpMcLoanConfigDTO.getContextPath());
				in.setHeader(Exchange.HTTP_METHOD, BusinessConstants.CAMEL_HTTP_METHOD_GET);
				in.setHeader(BusinessConstants.HEADER_CONNECTION, BusinessConstants.HEADER_CONNECTION_VALUE);
			}
		 else if(externalSystemType.equals(ExternalSystem.MM.name()))
		 {
			 appCacheSubStoreKey=AppCacheSubStore.MOBILEMONEY_DETAILS;
			 appBag.setSubStore(appCacheSubStoreKey);
			 appBag.setKey(outgoingcircleCodeIpPort);
			 MobileMoneySystemDetails fdpMobileMoneySystemDetails=(MobileMoneySystemDetails)applicationConfigCache.getValue(appBag);
			 in.setHeader(BusinessConstants.AUTHORIZATION, fdpMobileMoneySystemDetails.getAuthenticationKey());
			 in.setHeader(Exchange.HTTP_PATH, fdpMobileMoneySystemDetails.getContextPath());
			 in.setHeader(BusinessConstants.HEADER_CONNECTION, BusinessConstants.HEADER_CONNECTION_VALUE);
			 in.setHeader(Exchange.HTTP_METHOD, BusinessConstants.CAMEL_HTTP_METHOD_POST);
			 
		 } else if(externalSystemType.equals(ExternalSystem.Ability.name())) {
				appCacheSubStoreKey = AppCacheSubStore.ABILITY_DETAILS;
				appBag.setSubStore(appCacheSubStoreKey);
				appBag.setKey(outgoingcircleCodeIpPort);
				FDPAbilityConfigDTO fdpAbilityConfigDTO = (FDPAbilityConfigDTO) applicationConfigCache.getValue(appBag);
				in.setHeader(BusinessConstants.AUTHORIZATION, fdpAbilityConfigDTO.getAuthenticationKey());
				in.setHeader(BusinessConstants.USER_AGENT, fdpAbilityConfigDTO.getUserAgent());
				in.setHeader(BusinessConstants.HOST, fdpAbilityConfigDTO.getIpAddress() + BusinessConstants.COLON + fdpAbilityConfigDTO.getPort());
				in.setHeader(Exchange.HTTP_PATH, fdpAbilityConfigDTO.getContextPath());
				in.setHeader(Exchange.CONTENT_TYPE, BusinessConstants.HTTP_CONTENT_TYPE_AIR);
				in.setHeader(BusinessConstants.HEADER_CONNECTION, BusinessConstants.HEADER_CONNECTION_VALUE);
				in.setHeader(BusinessConstants.HEADER_ACCEPT, BusinessConstants.HEADER_ACCEPT_VALUE);
				in.setHeader(Exchange.HTTP_METHOD, BusinessConstants.HTTP_METHOD);
		}
		 else if (externalSystemType.equals(ExternalSystem.DMC.name())) {
				// changes by EOBEMAN
				appCacheSubStoreKey = AppCacheSubStore.DMCCONFEGURATION_MAP;
				appBag.setSubStore(appCacheSubStoreKey);
				appBag.setKey(outgoingcircleCodeIpPort);
				final FDPDMCConfigDTO fdpDMCConfigDTO = (FDPDMCConfigDTO) applicationConfigCache.getValue(appBag);
				in.setHeader(BusinessConstants.AUTHORIZATION, fdpDMCConfigDTO.getAuthenticationKey());
				in.setHeader(BusinessConstants.USER_AGENT, fdpDMCConfigDTO.getUserAgent());
				in.setHeader(BusinessConstants.HOST, fdpDMCConfigDTO.getIpAddress() + BusinessConstants.COLON + fdpDMCConfigDTO.getPort());
				in.setHeader(Exchange.HTTP_PATH, fdpDMCConfigDTO.getContextPath());
				//in.setHeader(Exchange.CONTENT_TYPE, BusinessConstants.HTTP_CONTENT_TYPE_AIR);
				in.setHeader(BusinessConstants.HEADER_CONNECTION, BusinessConstants.HEADER_CONNECTION_VALUE);
				in.setHeader(BusinessConstants.HEADER_ACCEPT, BusinessConstants.HEADER_ACCEPT_VALUE);
				in.setHeader(Exchange.HTTP_METHOD, BusinessConstants.HTTP_METHOD);
			}else if (externalSystemType.equals(ExternalSystem.ADC.name())) {
				appCacheSubStoreKey = AppCacheSubStore.ADCCONFEGURATION_MAP;
				appBag.setSubStore(appCacheSubStoreKey);
				appBag.setKey(outgoingcircleCodeIpPort);
				FDPADCConfigDTO fdpaadcConfigDTO = (FDPADCConfigDTO) applicationConfigCache.getValue(appBag);
				//in.setHeader(BusinessConstants.AUTHORIZATION, fdpairConfigDTO.getAuthenticationKey());
			//	in.setHeader(BusinessConstants.USER_AGENT, fdpairConfigDTO.getUserAgent());
				in.setHeader(BusinessConstants.HOST, fdpaadcConfigDTO.getIpAddress() + BusinessConstants.COLON
						+ fdpaadcConfigDTO.getPort());
				in.setHeader(Exchange.HTTP_PATH, fdpaadcConfigDTO.getContextPath()+in.getBody());
				in.setBody("");
				//in.setHeader(Exchange.CONTENT_TYPE, BusinessConstants.HTTP_CONTENT_TYPE_AIR);
				in.setHeader(BusinessConstants.HEADER_CONNECTION, BusinessConstants.HEADER_CONNECTION_VALUE);
				in.setHeader(BusinessConstants.HEADER_ACCEPT, BusinessConstants.HEADER_ACCEPT_VALUE);
				in.setHeader(Exchange.HTTP_METHOD, "GET");
				//updateHttpTimeoutParameters(httpComponent, Integer.parseInt(fdpaadcConfigDTO.getConnectionTimeout()), Integer.parseInt(fdpaadcConfigDTO.getResponseTimeout()));
			} 
	}
	

	/**
	 * Gets the compiled http uri.
	 * 
	 * @param command
	 *            the command
	 * @param userName
	 *            the user name
	 * @param password
	 *            the password
	 * @return the compiled http uri
	 */
	private String getCompiledHttpUri(final String command, final String userName, final String password) {
		return new StringBuilder(command).append(BusinessConstants.AMPERSAND_PARAMETER_APPENDER)
				.append(BusinessConstants.USER_ID).append(BusinessConstants.EQUALS).append(userName)
				.append(BusinessConstants.AMPERSAND_PARAMETER_APPENDER).append(BusinessConstants.PASSWORD)
				.append(BusinessConstants.EQUALS).append(password).toString();
	}
	
	/**
	 * This method will update the http connect timeout and response timeout parameters
	 * @param httpComponent
	 * @param connectTimeout
	 * @param responseTimeout
	 *//*
	private void updateHttpTimeoutParameters(HttpComponent httpComponent, Integer connectTimeout, Integer responseTimeout) {
		
		if (httpComponent != null) {
            HttpConnectionManagerParams params = httpComponent.getHttpConnectionManager().getParams();
            params.setConnectionTimeout(connectTimeout);
            params.setSoTimeout(responseTimeout);
        }
	}*/
}
