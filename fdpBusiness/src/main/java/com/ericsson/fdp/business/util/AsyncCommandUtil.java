package com.ericsson.fdp.business.util;

import ch.qos.logback.classic.Logger;

import com.ericsson.fdp.FDPCacheable;
import com.ericsson.fdp.business.vo.FDPAsycCommandVO;
import com.ericsson.fdp.common.enums.ModuleType;
import com.ericsson.fdp.core.dsm.framework.service.FDPCache;
import com.ericsson.fdp.core.exception.ExecutionFailedException;
import com.ericsson.fdp.core.logging.FDPLogger;
import com.ericsson.fdp.core.request.FDPRequest;
import com.ericsson.fdp.core.serviceprovisioning.bean.FDPMetaBag;
import com.ericsson.fdp.core.serviceprovisioning.bean.FDPRequestBag;
import com.ericsson.fdp.core.utils.ApplicationConfigUtil;

public class AsyncCommandUtil {
	/**
	 * store complete command in cache
	 * 
	 * @param fdpRequest
	 * @param circleLogger
	 */
	public static void storeInAsyncCache(FDPRequest fdpRequest,
			Logger circleLogger) {
		FDPMetaBag metaBag = new FDPMetaBag(fdpRequest.getCircle(),
				ModuleType.ASYNC_COMMANDS, fdpRequest.getLastExecutedCommand()
						.getCommandDisplayName());
		FDPCache<FDPMetaBag, FDPCacheable> fdpCache = null;
		String transactionid = null;
		try {
			fdpCache = ApplicationConfigUtil.getMetaDataCache();
			FDPAsycCommandVO asynccommandvo = (FDPAsycCommandVO) fdpCache
					.getValue(metaBag);

			if (asynccommandvo != null) {
				if (asynccommandvo.getTransactionparamtype().trim()
						.equalsIgnoreCase("INPUT")) {
					if (fdpRequest.getLastExecutedCommand() != null
							&& fdpRequest.getLastExecutedCommand()
									.getInputParam() != null) {
						transactionid = fdpRequest
								.getLastExecutedCommand()
								.getInputParam(
										asynccommandvo.getTransactionparam())
								.getValue().toString();
						FDPRequestBag fdprequestBag = new FDPRequestBag(
								transactionid);
						ApplicationConfigUtil.getRequestCacheForMMWeb()
								.putValue(fdprequestBag, fdpRequest);
					}

				} else if (asynccommandvo.getTransactionparamtype().trim()
						.equalsIgnoreCase("OUTPUT")) {
					if (fdpRequest.getLastExecutedCommand() != null
							&& fdpRequest.getLastExecutedCommand()
									.getOutputParams() != null) {
						transactionid = fdpRequest
								.getLastExecutedCommand()
								.getOutputParam(
										asynccommandvo.getTransactionparam())
								.getValue().toString();

						if (null == transactionid) {
							throw new ExecutionFailedException(
									"No Output Parameter in response");
						}

						FDPRequestBag fdprequestBag = new FDPRequestBag(
								transactionid);
						ApplicationConfigUtil.getRequestCacheForMMWeb()
								.putValue(fdprequestBag, fdpRequest);
					}

				}
			}

		} catch (ExecutionFailedException e) {
			FDPLogger.error(circleLogger, AsyncCommandUtil.class,
					"storeInAsyncCache()", "Not able to find Meta Cache");
		}
	}

	/**
	 * Check wheather command is async or not .Note : this method should be put
	 * in util class
	 * 
	 * @param fdpRequest
	 * @param circleLogger
	 * @return
	 */
	public static boolean checkIsAsyncCommand(FDPRequest fdpRequest,
			Logger circleLogger) {
		if (fdpRequest.getLastExecutedCommand() != null) {
			FDPMetaBag metaBag = new FDPMetaBag(fdpRequest.getCircle(),
					ModuleType.ASYNC_COMMANDS, fdpRequest
							.getLastExecutedCommand().getCommandDisplayName());
			FDPCache<FDPMetaBag, FDPCacheable> fdpCache = null;
			try {
				fdpCache = ApplicationConfigUtil.getMetaDataCache();
				if (fdpCache.getValue(metaBag) != null)
					return true;
			} catch (ExecutionFailedException e) {
				FDPLogger.error(circleLogger, AsyncCommandUtil.class,
						"checkIsAsyncCommand()", "Not able to find Meta Cache");
			}
		}
		return false;
	}

}
