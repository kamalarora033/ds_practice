package com.ericsson.fdp.business.util;

import org.slf4j.Logger;

import com.ericsson.fdp.FDPCacheable;
import com.ericsson.fdp.business.enums.AuxRequestParam;
import com.ericsson.fdp.business.enums.Command;
import com.ericsson.fdp.common.enums.ModuleType;
import com.ericsson.fdp.common.enums.Status;
import com.ericsson.fdp.core.command.FDPCommand;
import com.ericsson.fdp.core.exception.ExecutionFailedException;
import com.ericsson.fdp.core.logging.FDPLogger;
import com.ericsson.fdp.core.request.FDPRequest;
import com.ericsson.fdp.core.request.FDPResponse;
import com.ericsson.fdp.core.serviceprovisioning.bean.FDPMetaBag;
import com.ericsson.fdp.core.utils.ApplicationConfigUtil;

/**
 * This Class is used to send coupon consumption request to CMS.
 * @author evarrao
 *
 */
public class CouponConsumptionUtil {
	
	
	/**
	 * This method is used to check the SP response (Success/Failure) and the check if coupon is applied by user.
	 * @param fdpRequest
	 * @param fdpResponse
	 * @param logger
	 */
	public static void checkAndSendCouponConsumeRequest(FDPRequest fdpRequest, FDPResponse fdpResponse, Logger logger){
		FDPLogger.debug(logger, CouponConsumptionUtil.class, "checkAndSendCouponConsumeRequest()", "checking Service Provisioning Response...");
		
		if(fdpRequest.getAuxiliaryRequestParameter(AuxRequestParam.CONSUMER_APPLIED_COUPON_CODE) != null){
			FDPLogger.debug(logger, CouponConsumptionUtil.class, "checkAndSendCouponConsumeRequest()", "Coupon Applied by user: "+fdpRequest
					.getAuxiliaryRequestParameter(AuxRequestParam.CONSUMER_APPLIED_COUPON_CODE));
			
			if(Status.SUCCESS.equals(fdpResponse.getExecutionStatus())){
				FDPLogger.debug(logger, CouponConsumptionUtil.class, "executeConsumeCouponCommand()", LoggerUtil.getRequestAppender(fdpRequest)
						+ "Going to execute coupon consumption request.");
				try{
					boolean status = executeConsumeCouponCommand(fdpRequest, logger);
					FDPLogger.debug(logger, CouponConsumptionUtil.class, "executeConsumeCouponCommand()", LoggerUtil.getRequestAppender(fdpRequest)
								+ "Consumption Command Status : "+status);
				}
				catch(ExecutionFailedException ex){
					FDPLogger.error(logger, CouponConsumptionUtil.class, "executeConsumeCouponCommand()", "Error while execution of coupon consumption command.", ex);
				}
			}
		}
	}
	
	
	/**
	 * This method is used to send Coupon Consumption Request to CMS.
	 * @param fdpRequest
	 * @return
	 * @throws ExecutionFailedException
	 */
	private static boolean executeConsumeCouponCommand(FDPRequest fdpRequest, Logger logger) throws ExecutionFailedException{
		boolean status = false;
		FDPCommand cmsFdpCommand = null;
		final String getCouponCommandName = Command.CMS_CONSUME_SUBSCRIBER_COUPON.getCommandDisplayName();
		
		final FDPCacheable fdpCommandCached = ApplicationConfigUtil.getMetaDataCache().getValue(
				new FDPMetaBag(fdpRequest.getCircle(), ModuleType.COMMAND, getCouponCommandName));
		
		System.out.println("FDP command object : "+fdpCommandCached);
		if(fdpCommandCached != null && fdpCommandCached instanceof FDPCommand){
			cmsFdpCommand = (FDPCommand)fdpCommandCached;
			Status commandStatus = cmsFdpCommand.execute(fdpRequest);
			System.out.println("Consume Coupon Status : "+commandStatus);
			if(commandStatus.equals(Status.SUCCESS)){
				status = true;
			}
		}
		else{
			FDPLogger.error(logger, CouponConsumptionUtil.class, "executeConsumeCouponCommand()", "coupon consumption command not configured for "+fdpRequest.getCircle().getCircleCode());
		}
		
		return status;
	}

}
