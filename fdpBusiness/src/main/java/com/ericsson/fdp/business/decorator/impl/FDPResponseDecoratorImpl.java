package com.ericsson.fdp.business.decorator.impl;

import com.ericsson.fdp.business.decorator.FDPResponseDecorator;
import com.ericsson.fdp.business.display.impl.ResponseMessageImpl;
import com.ericsson.fdp.business.enums.TLVOptions;
import com.ericsson.fdp.core.display.ResponseMessage;
import com.ericsson.fdp.core.exception.ExecutionFailedException;
import com.ericsson.fdp.core.request.FDPResponse;

/**
 * This class is used to provide decoration to response.
 * 
 * @author Ericsson
 * 
 */
public class FDPResponseDecoratorImpl implements FDPResponseDecorator {

	/**
	 * The response object which is to be decorated.
	 */
	private final FDPResponse fdpResponse;

	/**
	 * The constructor for response decorator.
	 */
	public FDPResponseDecoratorImpl(final FDPResponse fdpResponse) {
		this.fdpResponse = fdpResponse;
	}

	@Override
	public FDPResponse decorateResponse() throws ExecutionFailedException {
		if (fdpResponse != null && fdpResponse.getResponseString() != null) {
			boolean isLastFlash = false;
			for (final ResponseMessage responseMessage : fdpResponse.getResponseString()) {
				if (isLastFlash && responseMessage instanceof ResponseMessageImpl) {
					((ResponseMessageImpl) responseMessage).addTLVOption(TLVOptions.NO_SESSION_INFO);
				}
				isLastFlash = false;
				if (responseMessage.getTLVOptions().contains(TLVOptions.FLASH)) {
					isLastFlash = true;
				}
			}
		}
		return fdpResponse;
	}

}
