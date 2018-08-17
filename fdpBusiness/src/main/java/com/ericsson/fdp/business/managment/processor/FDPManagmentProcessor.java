package com.ericsson.fdp.business.managment.processor;

import java.util.HashMap;
import java.util.Map;

import org.apache.camel.Exchange;
import org.apache.camel.Message;
import org.apache.camel.Processor;

import com.ericsson.fdp.business.managment.services.impl.FDPManagmentMetaCacheServiceImpl;
import com.ericsson.fdp.managment.constants.FDPManagmentConstants;
import com.ericsson.fdp.managment.enums.FDPManagmentParams;
import com.ericsson.fdp.managment.enums.FDPManagmentRouteEnum;
import com.ericsson.fdp.managment.services.FDPManagmentService;

/**
 * This processor will call the corresponding service for different management processors.
 * 
 * @author eashtod
 *
 */
public class FDPManagmentProcessor implements Processor {

	@Override
	public void process(Exchange exchange) throws Exception {
		
		final String routeId = exchange.getFromRouteId();
		FDPManagmentRouteEnum fdpManagmentRouteEnum = null;
		if(null == routeId) {
			throw new Exception("NULL RouteId found in exchange ");
		} else {
			fdpManagmentRouteEnum = FDPManagmentRouteEnum.getFDPManagmentRouteEnumByRouteId(routeId);
			if (null == fdpManagmentRouteEnum) {
				throw new Exception("RouteId not found in exchange as :"+routeId);
			}
			exchange.getIn().setHeader(FDPManagmentConstants.REQUEST_TYPE, fdpManagmentRouteEnum);
		}
		final String response = getSpecialFDPManagmentService(fdpManagmentRouteEnum).execute(prepareRequestMap(fdpManagmentRouteEnum, exchange));
		final Message out = exchange.getOut();
		out.setBody(response);
		exchange.setProperty(Exchange.ROUTE_STOP, Boolean.TRUE);
	}
	
	/**
	 * This method will validate the input variable values.
	 * 
	 * @param fdpManagmentRouteEnum
	 * @param exchange
	 * @return
	 * @throws Exception
	 */
	private Map<FDPManagmentParams, String> prepareRequestMap(final FDPManagmentRouteEnum fdpManagmentRouteEnum, final Exchange exchange) throws Exception {
		final Map<FDPManagmentParams, String> map = new HashMap<FDPManagmentParams, String>();
		final Message in = exchange.getIn();
		if(null != fdpManagmentRouteEnum.getParamList()) {
			for(final FDPManagmentParams fdpManagmentParams : fdpManagmentRouteEnum.getParamList()) {
				final String requestParam = in.getHeader(fdpManagmentParams.getParamName(), String.class);
				if(null == requestParam && fdpManagmentParams.isMandatory()) {
					throw new Exception("Expected "+fdpManagmentParams.getParamName()+" in request URL.");
				} else {
					map.put(fdpManagmentParams, requestParam);
				}
			}
		}
		return map;
	}
	
	/**
	 * This method will provide the special handling for the business reload service.
	 * 
	 * @param managmentRouteEnum
	 * @return
	 */
	private FDPManagmentService getSpecialFDPManagmentService(final FDPManagmentRouteEnum managmentRouteEnum) {
		FDPManagmentService fdpManagmentService = null;
		switch (managmentRouteEnum) {
		case RELOAD_CIRCLE_CACHE:
			fdpManagmentService = new FDPManagmentMetaCacheServiceImpl();
			break;
		default:
			fdpManagmentService = managmentRouteEnum.getFdpManagmentService();
		}
		return fdpManagmentService;
	}

}
