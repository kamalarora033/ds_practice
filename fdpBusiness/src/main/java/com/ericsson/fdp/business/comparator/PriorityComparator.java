package com.ericsson.fdp.business.comparator;

import java.util.Comparator;

import com.ericsson.fdp.core.node.FDPNode;

/**
 * This class compares nodes based on priority.
 * 
 * @author Ericsson
 * 
 */
public class PriorityComparator implements Comparator<FDPNode> {

	@Override
	public int compare(final FDPNode node1, final FDPNode node2) {
		Long priority1 = node1.getPriority();
		Long priority2 = node2.getPriority();
		int comparedValue = 0;
		if (priority1 != null && priority2 != null) {
			// if both priority are not null compare priority.
			comparedValue = priority1.compareTo(priority2);
			if(comparedValue == 0) {
				comparedValue = node1.getDisplayName().compareTo(node2.getDisplayName()); 
			}
		} else if (priority1 != null) {
			// if priority2 is null and priority1 is not null.
			comparedValue = -1;
		} else if (priority2 != null) {
			// if priority1 is null and priority2 is not null.
			comparedValue = 1;
		}
		return comparedValue;
	}

}
