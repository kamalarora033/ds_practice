package com.ericsson.fdp.business.managment.processor;

import java.util.List;

import org.apache.camel.Exchange;
import org.apache.camel.Message;
import org.apache.camel.Processor;

import com.ericsson.fdp.managment.constants.FDPManagmentConstants;
import com.ericsson.fdp.managment.enums.FDPManagmentParams;
import com.ericsson.fdp.managment.enums.FDPManagmentRouteEnum;

public class FDPManagmentOnExceptionProcessor implements Processor{

	@Override
	public void process(Exchange exchange) throws Exception {
		try {
			final Throwable throwable = exchange.getProperty(Exchange.EXCEPTION_CAUGHT, Throwable.class);
			if(null != throwable) {
				throwable.printStackTrace();
			}
			final FDPManagmentRouteEnum fdpManagmentRouteEnum = exchange.getIn().getHeader(FDPManagmentConstants.REQUEST_TYPE,FDPManagmentRouteEnum.class);
			final String response = (null == fdpManagmentRouteEnum) ? getResponse() : getResponse(fdpManagmentRouteEnum);
			final Message out = exchange.getIn();
			exchange.setProperty(Exchange.EXCEPTION_CAUGHT, null);
			out.setBody(response);
			exchange.setProperty(Exchange.ROUTE_STOP, Boolean.TRUE);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * This method prepares the response.
	 * 
	 * @param fdpManagmentRouteEnum
	 * @return
	 */
	private String getResponse(final FDPManagmentRouteEnum fdpManagmentRouteEnum) {
		final StringBuffer stringBuffer = new StringBuffer();
		final List<FDPManagmentParams> list = fdpManagmentRouteEnum.getParamList();
		for (final FDPManagmentParams params : list) {
			stringBuffer.append(params.getParamName());
			if (!params.isMandatory()) {
				stringBuffer.append(FDPManagmentConstants.IS_OPTIONAL);
			}
			stringBuffer.append(FDPManagmentConstants.EQUAL);
			stringBuffer.append(FDPManagmentParams.getDefaultValues(params));
			stringBuffer.append(FDPManagmentConstants.NEW_LINE);
			stringBuffer.append(FDPManagmentConstants.NEW_LINE);
		}
		stringBuffer.append(getResponse());
		return stringBuffer.toString();
	}
	
	/**
	 * This method prepares the response.
	 * 
	 * @return
	 */
	private String getResponse() {
		StringBuffer response = new StringBuffer();
		response.append("++++++++++++++++ FDP MANAGMENT SERVICE URL ++++++++++++++++");
		for(final FDPManagmentRouteEnum routeEnum : FDPManagmentRouteEnum.values()) {
			response.append(FDPManagmentConstants.NEW_LINE);
			response.append(FDPManagmentRouteEnum.getUrl(routeEnum));
			response.append(FDPManagmentConstants.NEW_LINE);
			final List<FDPManagmentParams> list = routeEnum.getParamList();
			for(final FDPManagmentParams params : list) {
				response.append(params.getParamName());
				if(!params.isMandatory()) {
					response.append(FDPManagmentConstants.IS_OPTIONAL);
				}
				response.append(FDPManagmentConstants.EQUAL);
				response.append(FDPManagmentParams.getDefaultValues(params));
				response.append(FDPManagmentConstants.NEW_LINE);
			}
			response.append("\n ++++++++++++++++++++++++++++++++++++++++++++++++++++");
		}
		return response.toString();
	}
	
	public static void main(String[] args) {
		FDPManagmentOnExceptionProcessor o = new FDPManagmentOnExceptionProcessor();
		
	}

}
