package com.ericsson.fdp.business.display.impl;

import java.util.List;

import com.ericsson.fdp.business.display.DisplayBucket;
import com.ericsson.fdp.core.display.ResponseMessage;
import com.ericsson.fdp.core.node.FDPNode;

/**
 * This class displays the paginated display object.
 * 
 * @author Ericsson
 * 
 */
public class PaginatedDisplayObject extends DisplayObjectImpl {

	/**
	 * The constructor to create paginated display object.
	 * 
	 * @param currentNode
	 *            the current node.
	 */
	public PaginatedDisplayObject(final FDPNode currentNode, final ResponseMessage responseMessage) {
		super(currentNode, responseMessage);
	}

	/**
	 * 
	 */
	private static final long serialVersionUID = -244859817469613641L;

	/**
	 * The list of display bucket.
	 */
	private List<DisplayBucket> displayBuckets;

	/**
	 * The currently displayed bucket.
	 */
	private int currentDisplayBucketIndex;

	/**
	 * @return the displayBuckets
	 */
	public List<DisplayBucket> getDisplayBuckets() {
		return displayBuckets;
	}

	/**
	 * @param displayBucketsToSet
	 *            the displayBuckets to set
	 */
	public void setDisplayBuckets(final List<DisplayBucket> displayBucketsToSet) {
		this.displayBuckets = displayBucketsToSet;
	}

	/**
	 * @return the currentDisplayBucket index
	 */
	public int getCurrentDisplayBucketIndex() {
		return currentDisplayBucketIndex;
	}

	/**
	 * @param currentDisplayBucketIndexToSet
	 *            the currentDisplayBucket to set
	 */
	public void setCurrentDisplayBucketIndex(final int currentDisplayBucketIndexToSet) {
		this.currentDisplayBucketIndex = currentDisplayBucketIndexToSet;
	}

	@Override
	public String toString() {
		return super.toString();
	}

}
