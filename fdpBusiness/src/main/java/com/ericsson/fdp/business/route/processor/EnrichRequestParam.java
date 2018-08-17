package com.ericsson.fdp.business.route.processor;

import java.io.IOException;

import javax.annotation.Resource;

import org.apache.camel.Exchange;
import org.slf4j.Logger;

import com.ericsson.fdp.FDPCacheable;
import com.ericsson.fdp.business.enums.ivr.FulfillmentResponseCodes;
import com.ericsson.fdp.business.route.processor.ivr.AbstractFulfillmentProcessor;
import com.ericsson.fdp.business.util.LoggerUtil;
import com.ericsson.fdp.common.constants.QueueConstant;
import com.ericsson.fdp.core.dsm.framework.MomoTransactionExpiredRequest;
import com.ericsson.fdp.core.dsm.framework.service.FDPCache;
import com.ericsson.fdp.core.dsm.framework.service.impl.FDPCircleCacheProducer;
import com.ericsson.fdp.core.exception.ExecutionFailedException;
import com.ericsson.fdp.core.logging.FDPLogger;
import com.ericsson.fdp.core.request.impl.FDPRequestImpl;
import com.ericsson.fdp.core.serviceprovisioning.bean.FDPRequestBag;
import com.ericsson.fdp.core.utils.ApplicationConfigUtil;
import com.ericsson.fdp.dao.dto.FDPCacheRequest;


public class EnrichRequestParam extends AbstractFulfillmentProcessor {

	@Resource(lookup = "java:app/fdpCoreServices-1.0/RequestCacheForWeb")
	private FDPCache<FDPRequestBag, FDPCacheable> fdpRequestCacheForWeb;

	private static Logger logger;
	
	@Override
	public void process(Exchange exchange) throws Exception {
		FDPRequestImpl fdpCacheable = null;
		String transactionid=getXMLTransactionID(exchange);
		if(exchange.getIn().getBody()!=null){
			fdpCacheable = (FDPRequestImpl) ApplicationConfigUtil.getRequestCacheForMMWeb().getValue(new FDPRequestBag(transactionid.trim()));
			if(fdpCacheable!=null){
				logger = LoggerUtil.getSummaryLoggerFromRequest(fdpCacheable);
				FDPLogger.info(logger, getClass(),"process() ",  LoggerUtil.getRequestAppender(fdpCacheable)
						+ "Debit complete Request  "+ (String) exchange.getIn().getBody());
				}
			}
		String status=getXMLStatus(exchange);
		if(status!=null && status.equals("FAILED")) {
			exchange.getOut().setHeader(Exchange.HTTP_RESPONSE_CODE, 555); 
			if (transactionid != null && fdpCacheable != null) {
				MomoTransactionExpiredRequest momoRequest = new MomoTransactionExpiredRequest(transactionid, (FDPCacheable) fdpCacheable);
				pushToMomoExpiredTransQueue(momoRequest);
				ApplicationConfigUtil.getRequestCacheForMMWeb().removeKey(new FDPRequestBag(transactionid.trim()));
			}
		} else {
		
			String requestXML=getRequestXML(exchange);
			try {			
				if (null != transactionid) {
				
					FDPRequestImpl fdpcacbeble = (FDPRequestImpl) ApplicationConfigUtil.getRequestCacheForMMWeb().getValue(new FDPRequestBag(transactionid.trim()));
					exchange.getIn().setHeader("MM_TRANSACTION",transactionid);
					exchange.getIn().setHeader("SUCESS_RESP", requestXML);
					exchange.getIn().setBody(fdpcacbeble);
					if(fdpcacbeble==null)
					{
						sendResponse(exchange, FulfillmentResponseCodes.ASYNC_SPNOTFOUND, null,null);
						exchange.getOut().setHeader(Exchange.HTTP_RESPONSE_CODE, 200); 
					}			
				}else {
						sendResponse(exchange, FulfillmentResponseCodes.ASYNC_SP_TRANSACTION_ID, null,null);
						exchange.getOut().setHeader(Exchange.HTTP_RESPONSE_CODE, 404); 
				}
			} catch(Exception e ) {
					sendResponse(exchange, FulfillmentResponseCodes.ASYNC_SPNOTFOUND, null,null);
					exchange.getOut().setHeader(Exchange.HTTP_RESPONSE_CODE, 404);
					e.printStackTrace();
			}
			finally {
				if(transactionid!=null)
					ApplicationConfigUtil.getRequestCacheForMMWeb().removeKey(new FDPRequestBag(transactionid.trim()));
			}
		}
	}

	private String getRequestXML(Exchange exchange) throws IOException {
		String myString = (String) exchange.getIn().getBody();
		String response=null;
		if(myString!=null && myString.contains("debitcompletedrequest"))
		{
			response= "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?><ns2:debitcompletedresponse xmlns:ns2=\"http://www.ericsson.com/em/emm\"/>";
		}
		return response; 
	}

	private String getXMLTransactionID(Exchange exchange) throws IOException {
		String myString = (String) exchange.getIn().getBody();
		StringBuilder stringbuffer;
		
		String status;
		if(myString!=null)
		{
			stringbuffer=new StringBuilder(myString);
			status=stringbuffer.substring(stringbuffer.indexOf("<status>")+"<status>".length(),stringbuffer.indexOf("</status>"));
			
			// need to change this logic after validation
			if("SUCCESSFUL".equalsIgnoreCase(status.toUpperCase().trim()) || "FAILED".equalsIgnoreCase(status.toUpperCase().trim())){
			    return stringbuffer.substring(stringbuffer.indexOf("<transactionid>")+"<transactionid>".length(), stringbuffer.indexOf("</transactionid>"));
			}else if(!("SUCCESSFUL".equalsIgnoreCase(status.toUpperCase().trim()))){
			    return null;
			}
		}
		
		return (String) exchange.getIn().getHeader("transaction_id");
		
		
	}
	
	private String getXMLStatus(Exchange exchange) throws IOException {
		String myString = (String) exchange.getIn().getBody();
		StringBuilder stringbuffer;
		String status;
		if(myString!=null)
		{
			stringbuffer=new StringBuilder(myString);
			status=stringbuffer.substring(stringbuffer.indexOf("<status>")+"<status>".length(),stringbuffer.indexOf("</status>"));
			return status;
		}
		return null;	
	}
	
	/**
	 * This method will push the failed MM DEBIT Complete request to MomoExpiredTransactionsQueue to check the transaction status
	 * @param fdpCacheableObject
	 */
	public void pushToMomoExpiredTransQueue(final FDPCacheRequest fdpCacheableObject) {
		try{
			final FDPCircleCacheProducer circleCacheProducer = ApplicationConfigUtil.getFDPCircleCacheProducer();
			circleCacheProducer.pushToQueue(fdpCacheableObject, QueueConstant.JMS_QUEUE + QueueConstant.MOMO_EXPIRED_TRANSACTION_QUEUE);
		} catch (final ExecutionFailedException e) {
			FDPLogger.error(logger, getClass(),"pushToMomoExpiredTransQueue() ",  
					LoggerUtil.getRequestAppender((FDPRequestImpl)((MomoTransactionExpiredRequest)fdpCacheableObject).getFdpCacheable())
					+ "Execption Occurs while pushing the request into Queue." + e.getMessage());
		}
	}

}
