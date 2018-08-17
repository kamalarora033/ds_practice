package com.ericsson.fdp.business.decorator.impl;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.ericsson.fdp.business.decorator.FDPDecorator;
import com.ericsson.fdp.business.display.DisplayBucket;
import com.ericsson.fdp.business.display.impl.PaginatedDisplayObject;
import com.ericsson.fdp.business.enums.ConfigurationKey;
import com.ericsson.fdp.business.enums.DisplayArea;
import com.ericsson.fdp.business.enums.DynamicMenuPaginationEnum;
import com.ericsson.fdp.business.enums.DynamicMenuPaginationKey;
import com.ericsson.fdp.business.enums.TLVOptions;
import com.ericsson.fdp.business.util.PaginationUtil;
import com.ericsson.fdp.business.util.ResponseUtil;
import com.ericsson.fdp.common.constants.FDPConstant;
import com.ericsson.fdp.common.enums.ChannelType;
import com.ericsson.fdp.common.vo.FDPCircle;
import com.ericsson.fdp.core.cache.AppCacheSubStore;
import com.ericsson.fdp.core.display.DisplayObject;
import com.ericsson.fdp.core.dsm.framework.service.FDPCache;
import com.ericsson.fdp.core.exception.ExecutionFailedException;
import com.ericsson.fdp.core.node.FDPNode;
import com.ericsson.fdp.core.serviceprovisioning.bean.FDPAppBag;
import com.ericsson.fdp.core.utils.ApplicationConfigUtil;

/**
 * This class is a decorator to be used to decorate the text which permits
 * pagination.
 * 
 * @author Ericsson
 * 
 */
public class PaginationDecorator implements FDPDecorator {

	private static final int NUMBER_OF_DELIM = 1;

	/**
	 * The decorator which is further to be decorated with this decorator.
	 */
	private final FDPDecorator fdpDecorator;

	/**
	 * The delimiter to use.
	 */
	private final String delimiter;

	/**
	 * The channel on which pagination is to be performed.
	 */
	private final ChannelType channelType;

	/**
	 * The circle on which the pagination is to be performed.
	 */
	private final FDPCircle fdpCircle;

	/**
	 * The constructor.
	 * 
	 * @param fdpDecoratorToSet
	 *            The fdp decorator to be used.
	 * @param delimiterToSet
	 *            The delimiter to be used.
	 */
	public PaginationDecorator(final FDPDecorator fdpDecoratorToSet, final String delimiterToSet,
			final ChannelType channelType, final FDPCircle fdpCircle) {
		this.fdpDecorator = fdpDecoratorToSet;
		this.delimiter = delimiterToSet;
		this.channelType = channelType;
		this.fdpCircle = fdpCircle;
	}

	@Override
	public DisplayObject display() throws ExecutionFailedException {
		final DisplayObject displayObject = fdpDecorator.display();
		final List<DisplayBucket> buckets = generateBuckets(displayObject);
		final PaginatedDisplayObject paginatedDisplayObject = new PaginatedDisplayObject(
				displayObject.getCurrentNode(), ResponseUtil.getDisplayNodeMessage(channelType,
						getTLVOptions(displayObject, buckets.size())));
		for (final FDPNode fdpNode : displayObject.getNodesToDisplay()) {
			paginatedDisplayObject.addNodesToDisplay(fdpNode);
		}
		final DisplayBucket currentBucket = buckets.get(FDPConstant.FIRST_INDEX);
		paginatedDisplayObject.setCurrDisplayText(currentBucket.getDisplayString(), DisplayArea.COMPLETE);
		paginatedDisplayObject.setCurrentDisplayBucketIndex(FDPConstant.FIRST_INDEX);
		paginatedDisplayObject.setDisplayBuckets(buckets);
		return paginatedDisplayObject;
	}

	private List<TLVOptions> getTLVOptions(final DisplayObject displayObject, final int size) {
		if (size > 1) {
			final List<TLVOptions> tlvOptions = displayObject.getResponseMessage().getTLVOptions();
			tlvOptions.add(TLVOptions.SESSION_CONTINUE);
			tlvOptions.remove(TLVOptions.SESSION_TERMINATE);
		}
		return displayObject.getResponseMessage().getTLVOptions();
	}

	/**
	 * This method is used to generate buckets when the string is greater than
	 * the maximum permissible length.
	 * 
	 * @param stringToDecorate
	 *            The string from which the buckets are to be created.
	 * @return The list of buckets.
	 * @throws ExecutionFailedException
	 *             Exception in generating buckets.
	 */
	private List<DisplayBucket> generateBuckets(final DisplayObject displayObject) throws ExecutionFailedException {
		final FDPCache<FDPAppBag, Object> applicationConfigCache = ApplicationConfigUtil.getApplicationConfigCache();
		final FDPAppBag fdpAppBag = new FDPAppBag(AppCacheSubStore.CONFIGURATION_MAP, FDPConstant.MAX_LENGTH_FOR_USSD);
		final int maxLengthOfString = Integer.parseInt((String) applicationConfigCache.getValue(fdpAppBag));
		if (maxLengthOfString < (displayObject.getCurrDisplayText(DisplayArea.HEADER).length() + displayObject
				.getCurrDisplayText(DisplayArea.FOOTER).length())) {
			throw new ExecutionFailedException("The length is greater than the header and footer combined.");
		}
		List<DisplayBucket> displayBuckets = checkIfOnePageRequired(maxLengthOfString, displayObject);
		if (displayBuckets == null) {
			displayBuckets = createBuckets(maxLengthOfString, displayObject,
					getPaginatingText(DynamicMenuPaginationKey.MORE),
					getPaginatingText(DynamicMenuPaginationKey.PREVIOUS));
		}
		return displayBuckets;
	}

	/**
	 * This method is used to create the paginating text.
	 * 
	 * @param dynamicMenuPaginationKey
	 *            the key for which text is to be created.
	 * @return the paginating text to be used.
	 */
	private String getPaginatingText(final DynamicMenuPaginationKey dynamicMenuPaginationKey) {
		final Map<String, String> circleConfig = fdpCircle.getConfigurationKeyValueMap();
		final StringBuffer stringBuffer = new StringBuffer();
		switch (channelType) {
		case USSD:
			stringBuffer.append(circleConfig.get(dynamicMenuPaginationKey.getConfigurationKeyForUSSD().name()))
					.append(circleConfig.get(ConfigurationKey.FOOTER_CODE_TEXT_SEPERATOR.name()))
					.append(circleConfig.get(dynamicMenuPaginationKey.getConfigurationTextKeyForUSSD().name()));
			break;
		case SMS:
			stringBuffer.append(circleConfig.get(dynamicMenuPaginationKey.getConfigurationKeyForSMS().name()))
					.append(circleConfig.get(ConfigurationKey.FOOTER_CODE_TEXT_SEPERATOR.name()))
					.append(circleConfig.get(dynamicMenuPaginationKey.getConfigurationTextKeyForSMS().name()));
			break;
		default:
			break;
		}
		return stringBuffer.toString();
	}

	/**
	 * This method is used to check if only one page is required.
	 * 
	 * @param maxLengthOfString
	 *            the maximum length of the string.
	 * @param displayObject
	 *            the display object.
	 * @return the list of display object.
	 */
	private List<DisplayBucket> checkIfOnePageRequired(final int maxLengthOfString, final DisplayObject displayObject) {
		List<DisplayBucket> displayBuckets = null;
		if (displayObject.getCurrDisplayText(DisplayArea.COMPLETE).length() <= maxLengthOfString) {
			final String[] stringToPaginate = displayObject.getCurrDisplayText(DisplayArea.MIDDLE).split(delimiter);
			// no need for pagination. Only one bucket present.
			displayBuckets = new ArrayList<DisplayBucket>();
			displayBuckets.add(createDisplayBucket(displayObject.getCurrDisplayText(DisplayArea.COMPLETE), 1,
					stringToPaginate.length - 1));
		}
		
		return displayBuckets;
	}

	/**
	 * This method is used to create the buckets.
	 * 
	 * @param maxLengthOfString
	 *            The maximum length of a bucket.
	 * @param displayObject
	 *            The displayObject to be used to create buckets.
	 * @param moreString
	 *            The more string to be used.
	 * @param prevString
	 *            The previous string to be used.
	 * @return The list of display buckets.
	 */
	private List<DisplayBucket> createBuckets(final int maxLengthOfString, final DisplayObject displayObject,
			final String moreString, final String prevString) {
		final List<DisplayBucket> displayBuckets = new ArrayList<DisplayBucket>();
		final String delimiterForMore = PaginationUtil.getSeperator(fdpCircle);
		final String middleString = displayObject.getCurrDisplayText(DisplayArea.MIDDLE), footerString = getFooterString(
				displayObject.getCurrDisplayText(DisplayArea.FOOTER), delimiterForMore), headerString = displayObject
				.getCurrDisplayText(DisplayArea.HEADER);
		StringBuffer displayText = new StringBuffer().append(headerString);
		int lengthAdded = 0, index = 1, lastIndex = 1, totalLengthAdded = 0;
		final int footerLength = footerString.length();
		int permissibleLength = maxLengthOfString - moreString.length() - headerString.length() - footerLength;
		boolean addMore = true, addPrevious = false;
		final int totalLengthOfString = middleString.length();
		for (final String stringToAdd : middleString.split(delimiter)) {
			if (!stringToAdd.isEmpty()) {
				final String stringToBeAdded = stringToAdd.concat(delimiter);
				if (lengthAdded + stringToBeAdded.length() > permissibleLength) {
					if (!FDPConstant.NEWLINE.equals(delimiter)) {
						final String displayTextAsString = displayText.substring(0, displayText.lastIndexOf(delimiter));
						displayText = new StringBuffer();
						displayText.append(displayTextAsString).append(FDPConstant.NEWLINE);
					}
					displayBuckets.add(createIndividualBucket(displayText, footerString, moreString, prevString,
							addMore, addPrevious, lastIndex, index));
					displayText = new StringBuffer();
					lastIndex = index;
					totalLengthAdded += lengthAdded;
					lengthAdded = 0;
					// create the bucket with more.
					// The calculation will be total length left + previous
					// option + footer option + delimiters between footer and
					// options

					if (totalLengthOfString + prevString.length() + footerLength
							+ (NUMBER_OF_DELIM * delimiterForMore.length()) - totalLengthAdded > maxLengthOfString) {
						addMore = true;
						addPrevious = true;
						permissibleLength = maxLengthOfString - moreString.length() - prevString.length()
								- (NUMBER_OF_DELIM * delimiter.length()) - footerLength;
					} else {
						addMore = false;
						addPrevious = true;
						permissibleLength = maxLengthOfString - prevString.length() - footerLength;
					}
				}
				displayText.append(stringToBeAdded);
				lengthAdded += stringToBeAdded.length();
				index++;
			}
		}
		if (displayText.length() > 0) {
			if (!FDPConstant.NEWLINE.equals(delimiter)) {
				final String displayTextAsString = displayText.substring(0, displayText.lastIndexOf(delimiter));
				displayText = new StringBuffer();
				displayText.append(displayTextAsString).append(FDPConstant.NEWLINE);
			}
			displayBuckets.add(createIndividualBucket(displayText, footerString, moreString, prevString, addMore,
					addPrevious, lastIndex, index));
		}
		return displayBuckets;
	}

	private String getFooterString(final String footerString, final String delimiterForMore) {
		return footerString != null && !footerString.isEmpty() ? footerString + delimiterForMore
				: FDPConstant.EMPTY_STRING;
	}

	/**
	 * This method is used to create a individual bucket.
	 * 
	 * @param displayText
	 *            The display text to be used.
	 * @param moreString
	 *            The more string to be used.
	 * @param prevString
	 *            The previous string to be used.
	 * @param footerText
	 * @param addMore
	 *            True, if more string is to be added.
	 * @param addPrevious
	 *            True, if previous string is to be added.
	 * @param startIndex
	 *            The start index of the bucket.
	 * @param endindex
	 *            The end index of the bucket.
	 * @return The display bucket.
	 */
	private DisplayBucket createIndividualBucket(final StringBuffer displayText, final String footerText,
			final String moreString, final String prevString, final boolean addMore, final boolean addPrevious,
			final int startIndex, final int endindex) {
		boolean previousAdded = false;
		boolean footerAdded = false;
		boolean moreAdded = false;
		
		for (final DynamicMenuPaginationEnum key : DynamicMenuPaginationEnum
				.getDynamicMenuPaginationPrioritySortedList()) {
			switch (key) {
			case FOOTER:
				displayText.append(footerText);
				footerAdded = true;
				break;
			case PAGINATION_MORE:
				if (addMore) {
					displayText.append(moreString);
					if ((addPrevious && !previousAdded) || !footerAdded) {
						displayText.append(PaginationUtil.getSeperator(fdpCircle));
					}
				}
				moreAdded = true;
				break;
			case PAGINATION_PREVIOUS:
				if (addPrevious) {
					/*
					 * Changes for handling double back option coming in
					 * RootNode->Menu1->Menu2->more option , with multiple
					 * products added in USSD DM
					 */
					if (footerText.isEmpty() && !footerAdded) {
						displayText.append(prevString);

						if ((addMore && !moreAdded) || !footerAdded) {
							displayText.append(PaginationUtil.getSeperator(fdpCircle));
						}
					}
				}
				previousAdded = true;
				break;
			}
		}
		return createDisplayBucket(displayText.toString(), startIndex, endindex);
	}

	/**
	 * This method is used to create a display object.
	 * 
	 * @param stringToDecorate
	 *            The string to be used.
	 * @param startIndex
	 *            The start index.
	 * @param endIndex
	 *            The end index.
	 * @return The display bucket.
	 */
	private DisplayBucket createDisplayBucket(final String stringToDecorate, final int startIndex, final int endIndex) {
		return new DisplayBucket(stringToDecorate, startIndex, endIndex);
	}
}
