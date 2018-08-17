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
 * This class defines the nodes which are of exit type.
 *
 * @author Ericsson
 *
 */
public class ExitMenuNode extends AbstractSystemMenuNode {

	/** The Constant serialVersionUID. */
	private static final long serialVersionUID = 3656969937356220640L;

	/**
	 * Instantiates a new exit menu node.
	 */
	public ExitMenuNode() {
	}

	/**
	 * Instantiates a new exit menu node.
	 *
	 * @param displayNameToSet
	 *            the display name to set
	 * @param shortCodeToSet
	 *            the short code to set
	 * @param fullyQualifiedPathToSet
	 *            the fully qualified path to set
	 * @param channelToSet
	 *            the channel to set
	 * @param circleToSet
	 *            the circle to set
	 * @param priorityToSet
	 *            the priority to set
	 * @param parentToSet
	 *            the parent to set
	 * @param childrenToSet
	 *            the children to set
	 */
	public ExitMenuNode(final String displayNameToSet, final String shortCodeToSet,
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
		return "ExitMenu";
	}

}
