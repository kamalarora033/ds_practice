package com.ericsson.fdp.business.decorator;

import com.ericsson.fdp.business.display.impl.ResponseMessageImpl;
import com.ericsson.fdp.business.util.LoggerUtil;
import com.ericsson.fdp.common.enums.ExternalSystem;
import com.ericsson.fdp.common.vo.FDPCircle;
import com.ericsson.fdp.core.display.DisplayObject;
import com.ericsson.fdp.core.display.ResponseMessage;
import com.ericsson.fdp.core.exception.ExecutionFailedException;
import com.ericsson.fdp.core.logging.FDPLogger;
import com.ericsson.fdp.core.utils.ApplicationCacheUtil;

/**
 * The Class DelayDecorator.
 * 
 * @author Ericsson
 */
public class DelayDecorator implements FDPDecorator {

	/** The Constant MCARBON_DELAY. */
	private static final String MCARBON_DELAY = "MCARBON_DELAY";

	/** The fdp decorator. */
	private final FDPDecorator fdpDecorator;

	/** The external system. */
	private final ExternalSystem externalSystem;

	/** The circle. */
	private final FDPCircle circle;

	/** The request id. */
	private final String requestId;

	/**
	 * Instantiates a new delay decorator.
	 * 
	 * @param decorator
	 *            the decorator
	 * @param externalSystemObj
	 *            the external system
	 * @param circle
	 *            the circle
	 */
	public DelayDecorator(final FDPDecorator decorator, final ExternalSystem externalSystemObj, final FDPCircle circle,
			final String requestId) {
		this.fdpDecorator = decorator;
		this.externalSystem = externalSystemObj;
		this.circle = circle;
		this.requestId = requestId;
	}

	@Override
	public DisplayObject display() throws ExecutionFailedException {
		final DisplayObject displayObject = fdpDecorator.display();
		if (externalSystem != null) {
			switch (externalSystem) {
			case MCARBON:
				setDelayForExternalSystem(MCARBON_DELAY, displayObject);
				break;
			default:
				break;
			}
		}
		return displayObject;
	}

	private void setDelayForExternalSystem(final String key, final DisplayObject displayObject)
			throws ExecutionFailedException {
		final Long delay = ApplicationCacheUtil.getCircleConfigurationLongFromApplicationCache(circle.getCircleCode(),
				key);
		if (delay != null) {
			setDelayInResponseMessage(displayObject, delay);
		} else {
			FDPLogger.warn(
					LoggerUtil.getRequestLogger(circle.getCircleName()),
					this.getClass(),
					"display()",
					"Request Id : " + requestId + ", Delay not found in circle configurations for circle = "
							+ circle.getCircleCode());
		}
	}

	/**
	 * Sets the delay in response message.
	 * 
	 * @param displayObject
	 *            the display object
	 * @param delay
	 *            the delay
	 * @throws ExecutionFailedException
	 *             the execution failed exception
	 */
	private void setDelayInResponseMessage(final DisplayObject displayObject, final Long delay)
			throws ExecutionFailedException {
		final ResponseMessage responseMessage = displayObject.getResponseMessage();
		if (responseMessage != null && responseMessage instanceof ResponseMessageImpl) {
			final ResponseMessageImpl responseMessageImpl = (ResponseMessageImpl) responseMessage;
			responseMessageImpl.setDelay(delay);
		} else {
			throw new ExecutionFailedException("Unable to set Delay in Response Message");
		}
	}

}
