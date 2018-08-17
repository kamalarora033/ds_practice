package com.ericsson.fdp.business.display;

import java.io.Serializable;

/**
 * This class is a display bucket.
 * 
 * @author Ericsson
 * 
 */
public class DisplayBucket implements Serializable{

	/**
	 * 
	 */
	private static final long serialVersionUID = -109563993763772406L;

	/**
	 * The display string.
	 */
	private String displayString;

	/**
	 * The start index of the bucket.
	 */
	private int startIndex;

	/**
	 * The end index of the bucket which is NOT inclusive. The end index is one
	 * greater than the the inclusive end index.
	 */
	private int endIndex;

	public DisplayBucket(final String displayStringToSet, final int startIndexToSet, final int endIndexToSet) {
		this.displayString = displayStringToSet;
		this.endIndex = endIndexToSet;
		this.startIndex = startIndexToSet;
	}

	/**
	 * @return the displayString
	 */
	public String getDisplayString() {
		return displayString;
	}

	/**
	 * @param displayStringToSet
	 *            the displayString to set
	 */
	public void setDisplayString(final String displayStringToSet) {
		this.displayString = displayStringToSet;
	}

	/**
	 * @return the startIndex
	 */
	public int getStartIndex() {
		return startIndex;
	}

	/**
	 * @param startIndexToSet
	 *            the startIndex to set
	 */
	public void setStartIndex(final int startIndexToSet) {
		this.startIndex = startIndexToSet;
	}

	/**
	 * @return the endIndex
	 */
	public int getEndIndex() {
		return endIndex;
	}

	/**
	 * @param endIndexToSet
	 *            the endIndex to set
	 */
	public void setEndIndex(final int endIndexToSet) {
		this.endIndex = endIndexToSet;
	}

}
