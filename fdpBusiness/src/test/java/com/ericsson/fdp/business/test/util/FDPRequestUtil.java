package com.ericsson.fdp.business.test.util;

import com.ericsson.fdp.business.request.impl.FulfillmentRequestImpl; //com.ericsson.fdp.core.request.impl.FDPRequestImpl;
import com.ericsson.fdp.business.request.requestString.impl.FDPIVRRequestStringImpl;
import com.ericsson.fdp.common.vo.FDPCircle;

public class FDPRequestUtil {

	private static FulfillmentRequestImpl fdpRequest = new FulfillmentRequestImpl();

	private static void init() {

		fdpRequest.setRequestId("WEB_10.206.82.85_a6d0e214-1795-47f1-9459-01b7e11009bf");

		// fulfilment requst and set the name of product
		fdpRequest.setRequestString(new FDPIVRRequestStringImpl("3G_Pack"));

		// set the circle
		FDPCircle fdpCircle = new FDPCircle(4l, "GH", "Ghana");
		fdpRequest.setCircle(fdpCircle);

		fdpRequest.setSessionId("WEB_10.206.82.85_3758fe7a-caa8-48bf-a105-cd16fb58aa70");
		fdpRequest.setSubscriberNumber(233670992029l);
		fdpRequest.setIncomingSubscriberNumber(233670992029l);
		fdpRequest.setOriginNodeType("CIS");
		fdpRequest.setOriginHostName("CIS");
		fdpRequest.setSubscriberNumberNAI(1l);

	}

	public static FulfillmentRequestImpl getInstance() {
		init();
		return fdpRequest;
	}
}
