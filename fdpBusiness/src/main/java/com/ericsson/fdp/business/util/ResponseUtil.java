package com.ericsson.fdp.business.util;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import com.ericsson.fdp.business.constants.FDPStepResponseConstants;
import com.ericsson.fdp.business.display.impl.ResponseMessageImpl;
import com.ericsson.fdp.business.enums.TLVOptions;
import com.ericsson.fdp.business.request.impl.FDPStepResponseImpl;
import com.ericsson.fdp.common.enums.ChannelType;
import com.ericsson.fdp.core.display.ResponseMessage;
import com.ericsson.fdp.core.request.FDPStepResponse;

/**
 * This class is a utility class that works on response.
 * 
 * @author Ericsson
 * 
 */
public class ResponseUtil {

	/**
	 * Instantiates a new response util.
	 */
	private ResponseUtil() {

	}

	/**
	 * This method is used to create the response message.
	 * 
	 * @param channel
	 *            the channel type.
	 * @param displayString
	 *            the display string.
	 * @param tlvOptions
	 *            the tlv options.
	 * @return the response message.
	 */
	public static ResponseMessage createResponseMessage(final ChannelType channel, final String displayString,
			final TLVOptions... tlvOptions) {
		final List<TLVOptions> tlvOptionsList = new ArrayList<TLVOptions>(Arrays.asList(tlvOptions));
		return new ResponseMessageImpl(channel, null, tlvOptionsList, displayString);
	}

	/**
	 * This method is used for creating response list.
	 * 
	 * @param messages
	 *            the messages that have to be added.
	 * @return the response message list.
	 */
	public static List<ResponseMessage> createResponseList(final ResponseMessage... messages) {
		final List<ResponseMessage> responseMessages = new ArrayList<ResponseMessage>();
		if (messages != null && messages.length > 0) {
			for (final ResponseMessage responseMessage : messages) {
				responseMessages.add(responseMessage);
			}
		}
		return responseMessages;
	}

	/**
	 * This method is used to get the response message as list.
	 * 
	 * @param channel
	 *            the channel type.
	 * @param displayString
	 *            the display string.
	 * @param tlvOptions
	 *            the tlv options.
	 * @return the response message.
	 */
	public static List<ResponseMessage> createResponseMessageInList(final ChannelType channel,
			final String displayString, final TLVOptions... tlvOptions) {
		return createResponseList(createResponseMessage(channel, displayString, tlvOptions));
	}

	/**
	 * The default display node message.
	 * 
	 * @param channel
	 *            the channel type.
	 * @return the response message.
	 */
	public static ResponseMessage getDisplayNodeMessage(final ChannelType channel) {
		return createResponseMessage(channel, null, TLVOptions.SESSION_CONTINUE);
	}

	/**
	 * The default display node message.
	 * 
	 * @param channel
	 *            the channel type.
	 * @return the response message.
	 */
	public static ResponseMessage getDisplayNodeMessage(final ChannelType channel, final List<TLVOptions> sessionContinue) {
		return new ResponseMessageImpl(channel, null, sessionContinue, null);
	}
	
	/**
	 * Check if contains Async response type.
	 * @param fdpStepResponse
	 * @return
	 */
	public static Boolean isReponseContainsAsyn(final FDPStepResponse fdpStepResponse) {
		final FDPStepResponseImpl fdpStepResponseImpl = (FDPStepResponseImpl) fdpStepResponse;
		return (null != fdpStepResponseImpl && null != fdpStepResponseImpl.getStepResponseValue(FDPStepResponseConstants.IS_CURRENT_COMMAND_ASYNC)) ? Boolean.valueOf(fdpStepResponseImpl.getStepResponseValue(FDPStepResponseConstants.IS_CURRENT_COMMAND_ASYNC).toString()) : false;
	}
}
