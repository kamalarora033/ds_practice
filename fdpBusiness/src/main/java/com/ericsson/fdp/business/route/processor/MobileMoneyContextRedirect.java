package com.ericsson.fdp.business.route.processor;

import org.apache.camel.Exchange;
import org.apache.camel.Processor;

import com.ericsson.fdp.business.constants.BusinessConstants;
import com.ericsson.fdp.business.enums.ConfigurationKey;
import com.ericsson.fdp.core.utils.ApplicationCacheUtil;
/**
 * Processor for mobile Money command based context switch
 * @author ehlnopu
 *
 */

public class MobileMoneyContextRedirect implements Processor{

	
	@Override
	public void process(Exchange exchange) throws Exception {
		String message=exchange.getIn().getBody(String.class);
		String contextPath;
		if(null!=message)
		{
			if(message.contains(MobileMoneyComandConstant.REFUND.getCommandtext()))	
			{
				contextPath = ApplicationCacheUtil.getCircleConfigurationFromApplicationCache(exchange.getProperty(BusinessConstants.CIRCLE_CODE).toString(),ConfigurationKey.MM_CONTEXT_PATH_REFUND.getAttributeName());
				if (contextPath != null && !contextPath.isEmpty()) {
					exchange.getIn().setHeader(Exchange.HTTP_PATH, contextPath);
				} else {
					exchange.getIn().setHeader(Exchange.HTTP_PATH, MobileMoneyComandConstant.REFUND.getCommandurl());
				}
				
			}
			else if(message.contains(MobileMoneyComandConstant.DEBIT.getCommandtext()))
			{
				contextPath = ApplicationCacheUtil.getCircleConfigurationFromApplicationCache(exchange.getProperty(BusinessConstants.CIRCLE_CODE).toString(),ConfigurationKey.MM_CONTEXT_PATH_DEBIT.getAttributeName());
				if (contextPath != null && !contextPath.isEmpty()) {
					exchange.getIn().setHeader(Exchange.HTTP_PATH, contextPath);
				} else {
					exchange.getIn().setHeader(Exchange.HTTP_PATH, MobileMoneyComandConstant.DEBIT.getCommandurl());
				}
				
			}
			else if(message.contains(MobileMoneyComandConstant.GETTRANSACTIONSTATUS.getCommandtext())){
				contextPath = ApplicationCacheUtil.getCircleConfigurationFromApplicationCache(exchange.getProperty(BusinessConstants.CIRCLE_CODE).toString(),ConfigurationKey.MM_CONTEXT_PATH_GET_TRANSACTION.getAttributeName());
				if (contextPath != null && !contextPath.isEmpty()) {
					exchange.getIn().setHeader(Exchange.HTTP_PATH, contextPath);
				} else {
					exchange.getIn().setHeader(Exchange.HTTP_PATH, MobileMoneyComandConstant.GETTRANSACTIONSTATUS.getCommandurl());
				}
			}
		}
		
	}

	enum MobileMoneyComandConstant
	{
		REFUND(":refundrequest","/serviceprovider/refund"),DEBIT(":debitrequest","/serviceprovider/debit"),GETTRANSACTIONSTATUS("gettransactionstatusrequest","/serviceprovider/gettransactionstatus");
		
		private String commandtext;
		private String commandurl;
		
		MobileMoneyComandConstant(String commandtoken,String commandurl)
		{
			commandtext=commandtoken;
			this.commandurl=commandurl;
		}

		public String getCommandtext() {
			return commandtext;
		}

		public String getCommandurl() {
			return commandurl;
		}
	}
	
}
