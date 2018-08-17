package com.ericsson.fdp.business.node.impl;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.ericsson.fdp.business.enums.State;
import com.ericsson.fdp.business.node.AbstractSystemMenuNode;
import com.ericsson.fdp.common.enums.ChannelType;
import com.ericsson.fdp.common.enums.DynamicMenuAdditionalInfoKey;
import com.ericsson.fdp.common.enums.Visibility;
import com.ericsson.fdp.common.vo.FDPCircle;
import com.ericsson.fdp.core.node.FDPNode;
import com.ericsson.fdp.core.request.FDPRequest;
import com.ericsson.fdp.dao.enums.LanguageType;

/**
 * This class defines the return menu node.
 *
 * @author Ericsson
 *
 */
public class ReturnMenuNode extends AbstractSystemMenuNode {

	/**
	 *
	 */
	private static final long serialVersionUID = 744036449637131702L;

	/**
	 *
	 */
	public ReturnMenuNode() {
	}

	/**
	 *
	 * @param displayNameToSet
	 * @param shortCodeToSet
	 * @param fullyQualifiedPathToSet
	 * @param channelToSet
	 * @param circleToSet
	 * @param priorityToSet
	 * @param parentToSet
	 * @param childrenToSet
	 */
	public ReturnMenuNode(final String displayNameToSet, final String shortCodeToSet,
			final String fullyQualifiedPathToSet, final ChannelType channelToSet, final FDPCircle circleToSet,
			final Long priorityToSet, final FDPNode parentToSet, final List<FDPNode> childrenToSet,
			final Map<String, Object> additionalInfo,final Visibility visibility,final State state) {
		super.setAbstractNode(displayNameToSet, shortCodeToSet, fullyQualifiedPathToSet, channelToSet, circleToSet,
				priorityToSet, parentToSet, childrenToSet, additionalInfo,visibility,state);
		if(additionalInfo.get(DynamicMenuAdditionalInfoKey.FRENCH_DISPLAY_NAME)!=null){
			Map<LanguageType, String> otherLanguageMap = new HashMap<LanguageType, String> ();
			otherLanguageMap.put(LanguageType.FRENCH, additionalInfo.get(DynamicMenuAdditionalInfoKey.FRENCH_DISPLAY_NAME).toString());
			this.setOtherLangMap(otherLanguageMap);
		}
	}

	@Override
	public String generateNodeInfo(final FDPRequest fdpRequest) {
		return "ReturnMenu";
	}

}
