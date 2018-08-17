package com.ericsson.fdp.business.https.mobileMoney;

import java.util.List;

import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ericsson.fdp.business.constants.BusinessConstants;
import com.ericsson.fdp.business.enums.ConfigurationKey;
import com.ericsson.fdp.business.https.evds.HTTPSServerDetailsDTO;
import com.ericsson.fdp.business.https.evds.HttpsProcessor;
import com.ericsson.fdp.core.utils.ApplicationCacheUtil;

public class HttpsProcessorMobileMoney implements Processor {
	
	private static final Logger LOGGER = LoggerFactory.getLogger(HttpsProcessor.class);
	HTTPSManagerMobileMoney httpsmanager;

	public HttpsProcessorMobileMoney() {}

	public HttpsProcessorMobileMoney(List<HTTPSServerDetailsDTO> httpsserverdetailsdtolst) {
		httpsmanager=new HTTPSManagerMobileMoney(httpsserverdetailsdtolst);
		}

	@Override
	public void process(Exchange exchange) throws Exception {
		String context=getcontext(exchange);
		String response=httpsmanager.httpsHit(exchange.getIn().getBody(String.class),context);
		LOGGER.debug("content getting from https server :"+response);
		exchange.getOut().setBody(response);
	}
	
	
	private String getcontext(Exchange exchange) throws Exception{
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
		}
		return null;
	}
	
	enum MobileMoneyComandConstant
	{
		REFUND(":refundrequest","/serviceprovider/refund"),DEBIT(":debitrequest","/serviceprovider/debit");
		
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
