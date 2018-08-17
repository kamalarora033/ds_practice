package com.ericsson.fdp.business.builder;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadPoolExecutor;

import com.ericsson.fdp.common.util.StringUtil;
import com.ericsson.fdp.core.exception.ExecutionFailedException;
import com.ericsson.fdp.core.request.FDPRequest;
import com.ericsson.fdp.dao.enums.ExternalSystem;

public class ExternalSystemThreadBuilder implements IThreadBuilder {
	
	private static Map<ExternalSystem, ExecutorService> executorServiceMap = null;
	private static Map<ExternalSystem,ThreadPoolExecutor> threadPollExecutormap=null;

	@Override
	public synchronized ExecutorService getExecutorService(FDPRequest fdpRequest, ExternalSystem externalsystem) {
		if(null == executorServiceMap || executorServiceMap.isEmpty()){
			executorServiceMap = new HashMap<ExternalSystem, ExecutorService>(ExternalSystem.values().length);
			for(final ExternalSystem externalSystem : ExternalSystem.values()) {
				int threadCount = 0;
				if(!StringUtil.isNullOrEmpty(fdpRequest.getCircle().getConfigurationKeyValueMap()
						.get(ExternalSystem.getExternalSystemThreadCount(externalSystem)))) {
					
			
					threadCount = Integer.parseInt(fdpRequest.getCircle().getConfigurationKeyValueMap()
							.get(ExternalSystem.getExternalSystemThreadCount(externalsystem)).trim());
				}else{
					threadCount = externalsystem.getNumberOfThreads();
				}
				executorServiceMap.put(externalSystem, Executors.newFixedThreadPool(threadCount));			
			}
		}
		return executorServiceMap.get(externalsystem);
	}
	
	
	
	@Override
	public synchronized ExecutorService getThreadPoolExecutor(FDPRequest fdpRequest, ExternalSystem externalsystem)
			throws ExecutionFailedException {
		if (null == threadPollExecutormap || threadPollExecutormap.isEmpty()) {
			threadPollExecutormap = new HashMap<ExternalSystem, ThreadPoolExecutor>(ExternalSystem.values().length);
			for (final ExternalSystem externalSystem : ExternalSystem.values()) {
				if (!StringUtil.isNullOrEmpty(fdpRequest.getCircle().getConfigurationKeyValueMap()
						.get(ExternalSystem.externalSystemThreadCountConfigKey(externalSystem)))) {
					try{
						threadPollExecutormap.put(externalSystem, FDPThreadUtil.getThreadPoolExecutor(externalsystem, fdpRequest));
					} catch (Exception e){
						throw new ExecutionFailedException("Not Able To initialize Thread Pool Executor");
					}
				}
			}
		}
		return threadPollExecutormap.get(externalsystem);
	}
	
}

