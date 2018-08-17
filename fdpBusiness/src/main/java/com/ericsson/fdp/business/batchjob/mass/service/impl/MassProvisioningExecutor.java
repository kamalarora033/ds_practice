package com.ericsson.fdp.business.batchjob.mass.service.impl;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.RejectedExecutionHandler;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ericsson.fdp.core.request.FDPRequest;
import com.ericsson.fdp.dao.dto.mass.MassDTO;
import com.ericsson.fdp.dao.entity.FDPMassLoad;

/**
 * This class performs mass provisioning using executor service.
 * 
 * @author evasaty
 */
public class MassProvisioningExecutor {

	private static final Logger LOGGER = LoggerFactory
			.getLogger(MassProvisioningExecutor.class.toString());

	BlockingQueue<Runnable> requestQueue = null;

	MassProvisionWorker massProvisionWorker = null;

	MassLoadUrlServiceImpl massLoadUrlServiceImpl = null;

	private ExecutorService taskExecutor = null;
	
	private AtomicInteger atomicInteger = new AtomicInteger(0);
	

	/**
	 * 
	 * @param coreThread
	 * @param maxThread
	 * @param queueSize
	 * @param massLoadUrlServiceImpl
	 */
	MassProvisioningExecutor(int coreThread, int maxThread, int queueSize,
			MassLoadUrlServiceImpl massLoadUrlServiceImpl) {

		this.massLoadUrlServiceImpl = massLoadUrlServiceImpl;

		this.requestQueue = new ArrayBlockingQueue<Runnable>(queueSize);

		this.taskExecutor = new ThreadPoolExecutor(coreThread, maxThread, 5000,
				TimeUnit.MILLISECONDS, requestQueue,
				getMassProvisionThreadFactory(),
				new RejectedExecutionHandler() {

					@Override
					public void rejectedExecution(Runnable r,
							ThreadPoolExecutor executor) {
						LOGGER.info("Mass provision task reject , once again submitting to executor counter :"+atomicInteger.incrementAndGet());						
						executor.execute(r);
					}
				});

	}

	/**
	 * 
	 * @param fdpMasload
	 * @param responseMap
	 */
	public void executeMassProvisioning(FDPMassLoad fdpMasload,
			Map<String, List<MassDTO>> responseMap, FDPRequest fdpRequest) {

		LOGGER.info("Submitting a provision job to worker thread ");
		LOGGER.debug("Submitting a provision job to worker thread ");
		massProvisionWorker = new MassProvisionWorker(fdpMasload, responseMap,fdpRequest);
		taskExecutor.execute(massProvisionWorker);
	}

	/**
	 * This method is responsible to wait until all worker thread will not
	 * complete their task . Once all worker thread will complete the execution
	 * , it will call the service class method to update the database and create
	 * result sheet
	 * 
	 */
	public void addShutdownHook() {

		boolean shutDownStatus = false;
		taskExecutor.shutdown();
		try {
			shutDownStatus = taskExecutor.awaitTermination(Long.MAX_VALUE,
					TimeUnit.NANOSECONDS);
		} catch (InterruptedException e) {

		}
		LOGGER.info("Execution completion status of all worker thread ["
				+ shutDownStatus + "]");
		// Calling method to update the result sheet and database
		massLoadUrlServiceImpl.createMassProvisionResultSheet();

		LOGGER.info("Execution completion status and updated status in database ");
	}

	/**
	 * This class will be responsible to create worker thread for mass provision
	 * requests
	 * 
	 * @return
	 */
	private ThreadFactory getMassProvisionThreadFactory() {

		final AtomicLong count = new AtomicLong(0);
		final String namePrefix = "MassProvision Worker Thread";

		return new ThreadFactory() {
			@Override
			public Thread newThread(Runnable runnable) {
				Thread thread = new Thread(runnable);
				if (namePrefix != null) {
					thread.setName(namePrefix + "-" + count.getAndIncrement());
				}
				return thread;
			}
		};
	}

}
