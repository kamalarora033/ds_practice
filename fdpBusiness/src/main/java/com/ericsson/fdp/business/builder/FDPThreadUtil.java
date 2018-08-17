package com.ericsson.fdp.business.builder;

import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import com.ericsson.fdp.business.enums.ConfigurationKey;
import com.ericsson.fdp.common.constants.FDPConstant;
import com.ericsson.fdp.common.util.StringUtil;
import com.ericsson.fdp.core.exception.ExecutionFailedException;
import com.ericsson.fdp.core.request.FDPRequest;
import com.ericsson.fdp.dao.enums.ExternalSystem;

public class FDPThreadUtil {

	public static boolean isOldThreadModel(FDPRequest fdpRequest) {
	final String threadpoolenabled=fdpRequest.getCircle().getConfigurationKeyValueMap()
		.get(ConfigurationKey.IS_FIXED_THREADPOOL_ENABLED.getAttributeName());
		return !StringUtil.isNullOrEmpty(threadpoolenabled)?
				(threadpoolenabled.equalsIgnoreCase(FDPConstant.TRUE)?true:false):
					false;
	}
	
	public static boolean isObjectBasesdModel(FDPRequest fdprequest)
	{
		final String objectbasedmodel=fdprequest.getCircle().getConfigurationKeyValueMap().get(ConfigurationKey.IS_OBJECT_BASED_MODEL.getAttributeName());
		return !StringUtil.isNullOrEmpty(objectbasedmodel)?
				(objectbasedmodel.equalsIgnoreCase(FDPConstant.TRUE)?true:false):
					false;
	}

	private static ThreadConfigurationBean getThreadConfigurationBean(ExternalSystem externalsystem,
			FDPRequest fdpRequest) throws ExecutionFailedException {
		ThreadConfigurationBean threadconfigurationbean = new ThreadConfigurationBean();
		final String threadconfiguration = fdpRequest.getCircle().getConfigurationKeyValueMap()
				.get(externalsystem.name() + FDPConstant.UNDERSCORE + "THREADCONFIG");

		if (threadconfiguration != null) {
			try {
				final String[] configuration = threadconfiguration.split(FDPConstant.COLON);
				final String minstr = configuration[0].split(FDPConstant.EQUAL)[1].trim();
				final String maxstr = configuration[1].split(FDPConstant.EQUAL)[1].trim();
				final String ttl = configuration[2].split(FDPConstant.EQUAL)[1].trim();

				threadconfigurationbean.setMin(Integer.parseInt(minstr));
				threadconfigurationbean.setMax(Integer.parseInt(maxstr));
				threadconfigurationbean.setTtl(Integer.parseInt(ttl));
			} catch (NumberFormatException nupe) {
				new ExecutionFailedException("Number Parsing error on THREADCONFIG CircleConfig param");
			} catch (IndexOutOfBoundsException iooe) {
				new ExecutionFailedException("IndexOutOfBoundsException error on THREADCONFIG CircleConfig param");
			} catch (Exception e) {
				new ExecutionFailedException(e.getClass() + " error on THREADCONFIG  CircleConfig param");
			}

		}
		return threadconfigurationbean;
	}

	public static synchronized ThreadPoolExecutor getThreadPoolExecutor(final ExternalSystem externalsystem,
			FDPRequest fdpRequest) throws ExecutionFailedException {
		final ThreadConfigurationBean threadconfigurationbean = getThreadConfigurationBean(externalsystem, fdpRequest);
		return new ThreadPoolExecutor(threadconfigurationbean.getMin(), threadconfigurationbean.getMax(),
				threadconfigurationbean.getTtl(), TimeUnit.SECONDS, new LinkedBlockingQueue<Runnable>(),
				new ThreadFactory() {
				int n = 0;
				@Override
				public Thread newThread(Runnable r) {
					Thread t = new Thread(r, externalsystem + "-Thread" + (n++));
					return t;
				}
		});
		
	}
}