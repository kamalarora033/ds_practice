package com.ericsson.fdp.business.comparator;

import java.util.Comparator;

import com.ericsson.fdp.business.enums.DynamicMenuFooterEnum;
import com.ericsson.fdp.business.enums.DynamicMenuPaginationKey;

/**
 * The Class FooterPriorityComparator.
 * 
 * @author Ericsson
 */
public class FooterPriorityComparator implements Comparator<DynamicMenuPaginationKey> {

	@Override
	public int compare(final DynamicMenuPaginationKey key1, final DynamicMenuPaginationKey key2) {
		return DynamicMenuFooterEnum.getDynamicMenuFooterKeyPriority(key1)
				- DynamicMenuFooterEnum.getDynamicMenuFooterKeyPriority(key2);
	}

}
