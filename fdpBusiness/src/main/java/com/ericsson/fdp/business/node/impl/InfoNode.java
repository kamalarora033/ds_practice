package com.ericsson.fdp.business.node.impl;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import ch.qos.logback.classic.Logger;

import com.ericsson.fdp.business.display.impl.DisplayObjectImpl;
import com.ericsson.fdp.business.enums.DisplayArea;
import com.ericsson.fdp.business.enums.State;
import com.ericsson.fdp.business.node.AbstractNode;
import com.ericsson.fdp.business.util.LoggerUtil;
import com.ericsson.fdp.business.util.ResponseUtil;
import com.ericsson.fdp.common.enums.ChannelType;
import com.ericsson.fdp.common.enums.DynamicMenuAdditionalInfoKey;
import com.ericsson.fdp.common.enums.Visibility;
import com.ericsson.fdp.common.vo.FDPCircle;
import com.ericsson.fdp.core.display.DisplayObject;
import com.ericsson.fdp.core.exception.ExecutionFailedException;
import com.ericsson.fdp.core.logging.FDPLogger;
import com.ericsson.fdp.core.node.FDPNode;
import com.ericsson.fdp.core.request.FDPRequest;
import com.ericsson.fdp.dao.enums.LanguageType;

public class InfoNode extends AbstractNode {

	/**
	 *
	 */
	private static final long serialVersionUID = 8032524810186106251L;
	/**
	 * This is the help text that is to be shown in case there is an error made
	 * by the user.
	 */
	private String displayText;

	public InfoNode() {

	}

	public InfoNode(final String displayTextToSet, final String displayNameToSet, final String shortCodeToSet,
			final String fullyQualifiedPathToSet, final ChannelType channelToSet, final FDPCircle circleToSet,
			final Long priorityToSet) {
		this.displayText = displayTextToSet;
		this.setDisplayName(displayNameToSet);
		this.setShortCode(shortCodeToSet);
		this.setFullyQualifiedPath(fullyQualifiedPathToSet);
		this.setChannel(channelToSet);
		this.setCircle(circleToSet);
		this.setPriority(priorityToSet);
	}

	public InfoNode(final String displayTextToSet, final String displayNameToSet, final String shortCodeToSet,
			final String fullyQualifiedPathToSet, final ChannelType channelToSet, final FDPCircle circleToSet,
			final Long priorityToSet, final FDPNode parentToSet, final List<FDPNode> childrenToSet,
			final Map<String, Object> additionalInfo,final Visibility visibility, final State state) {
		this.displayText = displayTextToSet;
		super.setAbstractNode(displayNameToSet, shortCodeToSet, fullyQualifiedPathToSet, channelToSet, circleToSet,
				priorityToSet, parentToSet, childrenToSet, additionalInfo,visibility,state);
		if(additionalInfo.get(DynamicMenuAdditionalInfoKey.FRENCH_DISPLAY_NAME)!=null){
			Map<LanguageType, String> otherLanguageMap = new HashMap<LanguageType, String> ();
			otherLanguageMap.put(LanguageType.FRENCH, additionalInfo.get(DynamicMenuAdditionalInfoKey.FRENCH_DISPLAY_NAME).toString());
			this.setOtherLangMap(otherLanguageMap);
		}
	}

	/**
	 * @param displayTextToSet
	 *            the displayText to set
	 */
	public void setDisplayText(final String displayTextToSet) {
		this.displayText = displayTextToSet;
	}

	/**
	 * @return the display text.
	 */
	public String getDisplayText() {
		return displayText;
	}

	@Override
	public DisplayObject displayNode(final FDPRequest fdpRequest) throws ExecutionFailedException {
		final Logger circleLogger = LoggerUtil.getSummaryLoggerFromRequest(fdpRequest);
		final DisplayObjectImpl displayObject = new DisplayObjectImpl(this,
				ResponseUtil.getDisplayNodeMessage(fdpRequest.getChannel()));
		displayObject.setCurrDisplayText(displayText, DisplayArea.MIDDLE);
		displayObject.addNodesToDisplay(this);
		FDPLogger.debug(circleLogger, getClass(), "displayNode()", LoggerUtil.getRequestAppender(fdpRequest)
				+ "Info node added, text :- " + displayText);
		return displayObject;
	}

	@Override
	public String toString() {
		return " info node. Name :- " + getDisplayName();
	}

	@Override
	public String generateNodeInfo(final FDPRequest fdpRequest) {
		return "Info";
	}

}
