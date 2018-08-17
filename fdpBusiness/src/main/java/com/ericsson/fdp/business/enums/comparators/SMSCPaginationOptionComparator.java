package com.ericsson.fdp.business.enums.comparators;

import java.util.Comparator;

import com.ericsson.fdp.business.enums.DynamicMenuPaginationKey;

/**
 * The Class SMSCPaginationOptionComparator.
 */
public class SMSCPaginationOptionComparator implements Comparator<DynamicMenuPaginationKey> {

	@Override
	public int compare(DynamicMenuPaginationKey o1, DynamicMenuPaginationKey o2) {
		return o1.getSmscOrderValue() - o2.getSmscOrderValue();
	}

}
