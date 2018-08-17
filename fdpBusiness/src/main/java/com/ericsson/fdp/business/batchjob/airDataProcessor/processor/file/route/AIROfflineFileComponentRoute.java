package com.ericsson.fdp.business.batchjob.airDataProcessor.processor.file.route;

import java.io.File;
import java.util.List;

import javax.annotation.PostConstruct;
import javax.ejb.Singleton;
import javax.ejb.Startup;
import javax.inject.Inject;

import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.cdi.CdiCamelContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ericsson.fdp.business.batchjob.airDataProcessor.processor.HandleAIROfflineIncomingFileRecords;
import com.ericsson.fdp.business.constants.BusinessConstants;
import com.ericsson.fdp.core.config.utils.PropertyUtils;
import com.ericsson.fdp.core.exception.ExecutionFailedException;
import com.ericsson.fdp.dao.fdpadmin.FDPCircleDAO;
import com.ericsson.fdp.route.cdi.context.provider.CdiCamelContextProvider;

/**
 * The Class AIROfflineFileComponentRoute is used to create routes for off-line
 * processing of AIR requests.
 * 
 * @author Ericsson
 */
@Singleton(name = "AIROfflineFileComponentRoute")
// @DependsOn(value = "ApplicationMonitor")
@Startup
public class AIROfflineFileComponentRoute {

	/** The cdi camel context provider. */
	@Inject
	private CdiCamelContextProvider cdiCamelContextProvider;

	/** The context. */
	private CdiCamelContext context;

	/** The incoming file records. */
	@Inject
	private HandleAIROfflineIncomingFileRecords incomingFileRecords;

	/** The fdp circle dao. */
	@Inject
	private FDPCircleDAO fdpCircleDAO;

	/** The Constant LOGGER. */
	private static final Logger LOGGER = LoggerFactory.getLogger(AIROfflineFileComponentRoute.class);

	/**
	 * This method is used to add air offline process records to camel context.
	 */
	@PostConstruct
	public void createRoutes() {
		LOGGER.info("Creating routes for Air Recharge Offline...");
		context = cdiCamelContextProvider.getContext();
		try {
			context.addRoutes(createAIROfflineRoutes());
		} catch (final ExecutionFailedException e) {
			LOGGER.error("Unable to start routes for class {} because of {} ", getClass(), e.getCause());
		} catch (final Exception e) {
			LOGGER.error("Unable to start routes for class {} because of {} ", getClass(), e.getCause());
		}
	}

	/**
	 * Creates the air offline routes to process records.
	 * 
	 * @return the route builder
	 */
	public RouteBuilder createAIROfflineRoutes() {
		return new RouteBuilder() {

			@Override
			public void configure() throws Exception {
				LOGGER.info("Creating Air Recharge Offline routes...");
				final List<String> circleCodeList = getAllCircleCodes();

				/**
				 * Getting base location path from properties file.
				 */
				String basePath = PropertyUtils.getProperty(BusinessConstants.AIR_OFFLINE_LOCATION);
				basePath = (basePath != null && !basePath.isEmpty()) ? basePath
						: BusinessConstants.FILE_OFFLINE_BASE_PATH_DEFAULT;

				String backupPath = PropertyUtils.getProperty(BusinessConstants.AIR_OFFLINE_BACKUP_LOCATION);
				backupPath = (backupPath != null && !backupPath.isEmpty()) ? backupPath
						: BusinessConstants.FILE_OFFLINE_BACKUP_BASE_PATH_DEFAULT;

				String nextPollDelay = PropertyUtils.getProperty("air.recharge.file.polling.delay");
				nextPollDelay = (nextPollDelay != null && !"".equals(nextPollDelay)) ? nextPollDelay : "60000";
				LOGGER.debug("Air recharge Polling delay is {}", nextPollDelay);
				final int circleCodeListSize = circleCodeList.size();

				/**
				 * Configuring circle wise file transfer location source path to
				 * back up path.
				 */
				for (int i = 0; i < circleCodeListSize; i++) {

					final String circleCode = circleCodeList.get(i);
					final String fromUrl = BusinessConstants.FILE_COMPONENT + basePath + File.separator + circleCode
							+ BusinessConstants.QUERY_STRING_SEPARATOR + BusinessConstants.FILE_COMPONENT_FORM_OPTION
							+ "&consumer.delay=" + nextPollDelay;
					LOGGER.debug("From URL : {}", fromUrl);
					final String toUrl = BusinessConstants.FILE_COMPONENT + backupPath + File.separator + circleCode
							+ BusinessConstants.QUERY_STRING_SEPARATOR + BusinessConstants.FILE_COMPONENT_TO_OPTION;
					LOGGER.debug("To URL : {}");
					final String routeId = BusinessConstants.FILE_OFFLINE_ROUTE_ID + BusinessConstants.UNDERSCORE
							+ circleCodeList.get(i);

					from(fromUrl).setHeader(BusinessConstants.CIRCLE_CODE, constant(circleCode)).routeId(routeId)
							.process(incomingFileRecords).to(toUrl);
				}
			}
		};

	}

	/**
	 * This method is used to return all available circle codes.
	 * 
	 * @return circle codes.
	 */
	private List<String> getAllCircleCodes() {
		return fdpCircleDAO.getAllCircleCodes();
	}
}
