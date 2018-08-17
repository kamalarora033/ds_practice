package com.ericsson.fdp.business.util;

import javax.annotation.Resource;

import com.ericsson.fdp.business.enums.RequestMetaValuesKey;
import com.ericsson.fdp.business.exception.NotificationFailedException;
import com.ericsson.fdp.business.request.impl.FDPStepResponseImpl;
import com.ericsson.fdp.business.vo.FDPActiveServicesVO;
import com.ericsson.fdp.business.vo.FDPPamActiveServicesVO;
import com.ericsson.fdp.common.constants.FDPConstant;
import com.ericsson.fdp.common.util.DateUtil;
import com.ericsson.fdp.core.dsm.framework.service.FDPCache;
import com.ericsson.fdp.core.request.FDPRequest;
import com.ericsson.fdp.core.request.impl.FDPRequestImpl;
import com.ericsson.fdp.core.serviceprovisioning.bean.FDPAppBag;
import com.ericsson.fdp.dao.enums.SharedAccountResponseType;
import com.ericsson.fdp.dao.enums.StepNameEnum;


/**
 * This class is the utility class for pending request.
 * 
 * @author Ericsson
 * 
 */
public class ActiveServicesRequestUtil {

	/**
	 * Instantiates a new pending request util.
	 */
	private ActiveServicesRequestUtil() {

	}
	
	/** The application cache. */
	@Resource(lookup = "java:app/fdpCoreServices-1.0/ApplicationConfigCache")
	private FDPCache<FDPAppBag, Object> applicationConfigCache;


	/**
	 * This method is used to create the notification text.
	 *
	 * @param fdpRequest the request.
	 * @param activeService the active service
	 * @return the notification created.
	 * @throws NotificationFailedException Exception, if any
	 */
	public static String createNotificationText(final FDPRequest fdpRequest, final FDPActiveServicesVO activeService)
			throws NotificationFailedException {
		if (fdpRequest instanceof FDPRequestImpl) {
			final FDPRequestImpl fdpRequestImpl = (FDPRequestImpl) fdpRequest;
			fdpRequestImpl.addMetaValue(RequestMetaValuesKey.PRODUCT, activeService.getProduct());
		}
		final FDPStepResponseImpl stepResponse = new FDPStepResponseImpl();
		//stepResponse.addStepResponseValue(SharedAccountResponseType.SERVICE_ID.name(),activeService.getServiceId());
		RequestUtil.putStepResponseInRequest(stepResponse, fdpRequest, StepNameEnum.VALIDATION_STEP.getValue());
		return NotificationUtil.createNotificationText(fdpRequest, FDPConstant.ACTIVE_SERVICES_REQUEST_NOT_ID

		- fdpRequest.getCircle().getCircleId(), LoggerUtil.getSummaryLoggerFromRequest(fdpRequest));

	}

	/**
	 * This method is used to create the notification text.
	 *
	 * @param fdpRequest the request.
	 * @param fdpPamActiveServicesVO the fdp pam active services vo
	 * @return the notification created.
	 * @throws NotificationFailedException Exception, if any
	 */
	public static String createNotificationText(final FDPRequest fdpRequest, final FDPPamActiveServicesVO fdpPamActiveServicesVO)
			throws NotificationFailedException {
		if (fdpRequest instanceof FDPRequestImpl) {
			final FDPRequestImpl fdpRequestImpl = (FDPRequestImpl) fdpRequest;
			fdpRequestImpl.addMetaValue(RequestMetaValuesKey.PRODUCT, fdpPamActiveServicesVO.getProduct());
		}
		return NotificationUtil.createNotificationText(fdpRequest, FDPConstant.PAM_ACTIVE_SERVICES_REQUEST_NOT_ID
		- fdpRequest.getCircle().getCircleId(), LoggerUtil.getSummaryLoggerFromRequest(fdpRequest));

	}
}
