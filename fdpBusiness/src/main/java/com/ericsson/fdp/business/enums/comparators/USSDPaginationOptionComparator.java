package com.ericsson.fdp.business.enums.comparators;

import java.util.Comparator;

import com.ericsson.fdp.business.enums.DynamicMenuPaginationKey;

/**
 * The Class USSDPaginationOptionComparator.
 */
public class USSDPaginationOptionComparator implements Comparator<DynamicMenuPaginationKey> {

	@Override
	public int compare(DynamicMenuPaginationKey o1, DynamicMenuPaginationKey o2) {
		return o1.getUssdOrderValue() - o2.getUssdOrderValue();
	}

}
