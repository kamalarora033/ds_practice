package com.ericsson.fdp.business.route.processor;

import javax.annotation.Resource;

import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;





import com.ericsson.fdp.business.constants.BusinessConstants;
import com.ericsson.fdp.business.enums.AuxRequestParam;
import com.ericsson.fdp.business.enums.DisplayArea;
import com.ericsson.fdp.business.request.impl.FulfillmentRequestImpl;
import com.ericsson.fdp.business.serviceprovisioning.impl.MMServiceProvisioningImpl;
import com.ericsson.fdp.common.enums.Status;
import com.ericsson.fdp.core.request.FDPResponse;
import com.ericsson.fdp.core.request.impl.FDPSMPPRequestImpl;

public class ExecuteMMSPProcessor implements Processor{

	@Resource(lookup = "java:app/fdpBusiness-1.0/MMServiceProvisioningImpl")
	private MMServiceProvisioningImpl fdpMMServiceProvisioning;
	
	private static final Logger LOGGER = LoggerFactory
			.getLogger(ExecuteMMSPProcessor.class);
	

	@Override
	public void process(Exchange exchange) throws Exception {
		
		
		Object object=exchange.getIn()
		.getBody();
		FDPResponse fdpResponse=null;
		//Long subscribernumber = null;
		if(object.getClass()==FulfillmentRequestImpl.class)
		{
			FulfillmentRequestImpl fulfilmentRequestImpl=(FulfillmentRequestImpl)object;
			fulfilmentRequestImpl.putAuxiliaryRequestParameter(
					AuxRequestParam.SKIP_CHARGING, true);
			//subscribernumber=fulfilmentRequestImpl.getSubscriberNumber();
			fdpResponse = fdpMMServiceProvisioning
					.executeServiceProvisioning(fulfilmentRequestImpl);
		}
		else if(object.getClass()==FDPSMPPRequestImpl.class)
		{
			FDPSMPPRequestImpl  smpprequestImpl=(FDPSMPPRequestImpl)object;
			smpprequestImpl.putAuxiliaryRequestParameter(
					AuxRequestParam.SKIP_CHARGING, true);
			//subscribernumber=smpprequestImpl.getSubscriberNumber();
			fdpResponse = fdpMMServiceProvisioning
					.executeServiceProvisioning(smpprequestImpl);
		}
		

		// com.ericsson.fdp.common.enums.

		if (fdpResponse.getExecutionStatus() == Status.SUCCESS) {
			exchange.getOut()
			.setHeader(BusinessConstants.HTTP_RESPONSE_CODE, BusinessConstants.ERROR_CODE_RESPONSE);
		/*	exchange.getOut()
					.setBody(fdpResponse.getResponseString().get(0).getCurrDisplayText(DisplayArea.COMPLETE));
		*/
			exchange.getOut()
			.setBody("<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?><ns0:debitcompletedresponse xmlns:ns0=\"http://www.ericsson.com/em/emm/callback/v1_0\"/>");
	} else if (fdpResponse.getExecutionStatus() == Status.FAILURE) {
			exchange.getOut().setHeader(Exchange.HTTP_RESPONSE_CODE, "400L");
			// exchange.getIn().setHeader("MM_TRANSACTION",transactionid);
			
			exchange.getOut()
			.setBody("<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?><ns2:debitcompletedresponse xmlns:ns2=\"http://www.ericsson.com/em/emm/callback/v1_0\"/>");
			/*exchange.getOut().setBody(
					subscribernumber
							+ ":"
							+ exchange.getIn().getHeader("MM_TRANSACTION")
							+ ":"
							+ fdpResponse.getResponseError().getResponseCode()
							+ ":"
							+ fdpResponse.getResponseError()
									.getResponseErrorString());*/
		} else {
			LOGGER.error("Error Occured Request "+exchange.getIn().getBody(String.class));
		}

	}
}
